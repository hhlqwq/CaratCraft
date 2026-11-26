[CmdletBinding()]
param(
    [ValidateSet('configure', 'build', 'init-db', 'upgrade-db', 'reset-db', 'start', 'stop', 'restart', 'status', 'logs', 'miniapp', 'diagnose', 'check-config', 'repair-config')]
    [string]$Action = 'start',
    [ValidateSet('staff-password', 'admin-jobs')]
    [string]$Upgrade,
    [switch]$NoBuild
)

$ErrorActionPreference = 'Stop'
$WorkspaceRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$ProjectRoot = $WorkspaceRoot
$ComposeFile = Join-Path $ProjectRoot 'deploy/docker-compose.yml'
$EnvFile = Join-Path $ProjectRoot 'deploy\.env'
$EnvExample = Join-Path $ProjectRoot 'deploy\.env.example'

# 校验 Docker Desktop 和 Compose / Validates Docker Desktop and Compose.
function Assert-DockerReady {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw '未检测到 Docker。Windows/macOS 请安装 Docker Desktop，Ubuntu 请安装 Docker Engine 和 Compose Plugin。'
    }
    & docker compose version | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw '未检测到 docker compose，请安装 Compose V2。'
    }
    & docker info | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw 'Docker 服务未运行，请先启动 Docker Desktop。'
    }
}

# 创建本地环境变量文件 / Creates the local environment file.
function Initialize-Environment {
    if (-not (Test-Path -LiteralPath $EnvFile)) {
        $Template = Get-Content -LiteralPath $EnvExample -Raw -Encoding UTF8
        foreach ($Key in @('MYSQL_ROOT_PASSWORD', 'MYSQL_APP_PASSWORD', 'REDIS_PASSWORD', 'JWT_SECRET')) {
            if ([regex]::Matches($Template, "(?m)^$Key=[^\r\n]*").Count -ne 1) {
                throw "环境模板 $EnvExample 必须且只能包含一项 $Key=, 未生成本机配置。"
            }
            $RandomBytes = New-Object byte[] 32
            $Generator = [Security.Cryptography.RandomNumberGenerator]::Create()
            try { $Generator.GetBytes($RandomBytes) } finally { $Generator.Dispose() }
            $Secret = [Convert]::ToBase64String($RandomBytes)
            $Template = [regex]::Replace($Template, "(?m)^$Key=[^\r\n]*", "$Key=$Secret")
        }
        [IO.File]::WriteAllText($EnvFile, $Template, [Text.UTF8Encoding]::new($false))
        Write-Host "已生成本机独立随机配置：$EnvFile, 密钥不在终端输出。"
    }
}

# 汇总公开启动规则与配置错误. 仅打印键名及操作建议.
function Assert-StartupConfiguration {
    $Values = @{}
    foreach ($Line in (Get-Content -LiteralPath $EnvFile -Encoding UTF8)) {
        if (-not $Line.Trim() -or $Line.TrimStart().StartsWith('#')) { continue }
        if ($Line -cnotmatch '^([A-Z][A-Z0-9_]*)=(.*)$') { throw 'deploy/.env 存在无效配置行。' }
        if ($Values.ContainsKey($Matches[1])) { throw "重复配置项 $($Matches[1])。" }
        $Values[$Matches[1]] = $Matches[2].Trim()
    }
    $Failures = @()
    $Ports = @{}
    $Rule = ''
    foreach ($Line in (Get-Content -LiteralPath $EnvExample -Encoding UTF8)) {
        if ($Line -cmatch '^# @startup (default|port|required|secret)$') {
            $Rule = $Matches[1]
            continue
        }
        if ($Rule -and $Line -cmatch '^([A-Z][A-Z0-9_]*)=') {
            $Key = $Matches[1]
            $Value = $Values[$Key]
            if (-not $Value) {
                $Hint = if (-not $Values.ContainsKey($Key) -and $Rule -in @('default','port')) {
                    '可执行 -Action repair-config 补齐安全默认值'
                } else {
                    '请在 deploy/.env 手工填写，沿用现有环境配置'
                }
                $Failures += "$Key 缺失。$Hint。"
            } elseif ($Rule -eq 'secret' -and $Value.Length -lt 24) {
                $Failures += "$Key 不足24个字符。请核对原有凭据。"
            } elseif ($Rule -eq 'port') {
                $Number = 0
                if ($Value -cnotmatch '^[0-9]{1,5}$' -or -not [int]::TryParse($Value, [ref]$Number) -or
                        $Number -lt 1 -or $Number -gt 65535) {
                    $Failures += "$Key 端口无效。"
                } elseif ($Ports.ContainsKey($Number)) {
                    $Failures += "$Key 与 $($Ports[$Number]) 端口重复。"
                } else {
                    $Ports[$Number] = $Key
                }
            }
        }
        $Rule = ''
    }
    if ($Values['JWT_SECRET'] -and $Values['JWT_SECRET'].Length -ge 24) {
        try {
            $JwtKey = [Convert]::FromBase64String($Values['JWT_SECRET'])
            if ($JwtKey.Length -lt 32 -or [Convert]::ToBase64String($JwtKey) -cne $Values['JWT_SECRET']) {
                throw 'JWT_SECRET 格式无效'
            }
        } catch { $Failures += 'JWT_SECRET 应为至少32字节随机数的标准 Base64 编码。' }
    }
    if ($Values['MYSQL_APP_USER'] -eq 'root') { $Failures += 'MYSQL_APP_USER 不得使用 root。' }
    if ($Values['CARATCRAFT_ENV'] -and $Values['CARATCRAFT_ENV'] -cnotin @('local', 'lan')) {
        $Failures += 'CARATCRAFT_ENV 必须是 local 或 lan。'
    }
    if ($Values['CARATCRAFT_ENV'] -ceq 'lan' -and -not $Values['MINIAPP_LAN_ORIGIN']) {
        $Failures += 'MINIAPP_LAN_ORIGIN 缺失。请填写手机可访问的局域网地址。'
    }
    if ($Failures.Count) { throw ($Failures -join [Environment]::NewLine) }
    return $Values
}

# 清除同名宿主环境变量, Compose 统一使用显式指定的配置文件.
function Clear-ComposeOverrides {
    $env:CARATCRAFT_DB_INIT = 'false'
    foreach ($Line in (Get-Content -LiteralPath $EnvExample -Encoding UTF8)) {
        if ($Line -match '^([A-Z][A-Z0-9_]*)=') {
            [Environment]::SetEnvironmentVariable($Matches[1], $null, 'Process')
        }
    }
}

# 检查仓库磁盘和端口, 允许本项目现有容器继续使用其端口.
function Assert-HostResources {
    param([hashtable]$Values)
    $Drive = [IO.DriveInfo]::new([IO.Path]::GetPathRoot($ProjectRoot))
    if ($Drive.AvailableFreeSpace -lt 2GB) { throw '仓库所在磁盘剩余空间不足 2 GiB。' }
    if (-not $Values) { return }
    $Bound = [Net.NetworkInformation.IPGlobalProperties]::GetIPGlobalProperties().GetActiveTcpListeners().Port
    $Targets = @{ NGINX_PORT = 'nginx:80'; MYSQL_PORT = 'mysql:3306'; REDIS_PORT = 'redis:6379' }
    foreach ($Key in $Targets.Keys) {
        $PortNumber = [int]$Values[$Key]
        if ($PortNumber -notin $Bound) { continue }
        $Service, $Target = $Targets[$Key].Split(':')
        $Result = Invoke-DockerMetadata -Arguments @('compose', '-p', 'platform-wxshop', '--env-file', $EnvFile, '-f', $ComposeFile, 'port', $Service, $Target)
        if ($Result.ExitCode -ne 0 -or ($Result.Output -join ' ') -notmatch ":$PortNumber(?:\\s|$)") {
            throw "端口 $PortNumber 已被占用, 请检查 $Key。"
        }
    }
}

# 读取真实 Git 提交, 没有 Git 元数据的离线源码包明确标记 archive.
function Set-GitRevision {
    $env:BUILD_GIT_SHA = 'archive'
    if ((Test-Path -LiteralPath (Join-Path $ProjectRoot '.git')) -and (Get-Command git -ErrorAction SilentlyContinue)) {
        $Sha = & git -C $ProjectRoot rev-parse --verify HEAD
        if ($LASTEXITCODE -eq 0 -and $Sha -match '^[0-9a-f]{40}$') { $env:BUILD_GIT_SHA = $Sha }
    }
    Write-Host "Git: $env:BUILD_GIT_SHA"
}

# 根据交付源码计算稳定摘要, ZIP解压环境无需安装Git且修改源码后必须重新构建.
function Get-BuildRevision {
    param([string]$Scope)
    $Prefix = "$Scope "
    $Inputs = [IO.File]::ReadAllLines((Join-Path $ProjectRoot 'deploy/build-inputs.txt')) |
        Where-Object { $_.StartsWith($Prefix, [StringComparison]::Ordinal) } |
        ForEach-Object { $_.Substring($Prefix.Length) }
    if (-not $Inputs) { throw "构建输入清单为空: $Scope" }
    $Files = foreach ($Relative in $Inputs) {
        Get-BuildSourceFiles -Path (Join-Path $ProjectRoot $Relative)
    }
    $Manifest = New-Object Text.StringBuilder
    $Hasher = [Security.Cryptography.SHA256]::Create()
    $BuildFiles = @($Files | Sort-Object FullName -Unique)
    [Array]::Sort($BuildFiles, [System.Comparison[object]]{ param($Left, $Right) [StringComparer]::Ordinal.Compare($Left.FullName, $Right.FullName) })
    $Processed = 0
    try {
        foreach ($File in $BuildFiles) {
            $Stream = [IO.File]::OpenRead($File.FullName)
            try { $Digest = [BitConverter]::ToString($Hasher.ComputeHash($Stream)).Replace('-', '') }
            finally { $Stream.Dispose() }
            $Relative = $File.FullName.Substring($WorkspaceRoot.Length + 1).Replace('\', '/')
            [void]$Manifest.Append($Relative).Append(':').Append($Digest).Append("`n")
            $Processed++
            if ($Processed % 250 -eq 0 -or $Processed -eq $BuildFiles.Count) {
                Write-Host "$Scope 构建输入: $Processed/$($BuildFiles.Count)"
            }
        }
        $Bytes = [Text.Encoding]::UTF8.GetBytes($Manifest.ToString())
        return [BitConverter]::ToString($Hasher.ComputeHash($Bytes)).Replace('-', '').ToLowerInvariant()
    } finally {
        $Hasher.Dispose()
    }
}

# 为Compose设置两个镜像各自的构建摘要.
function Set-BuildRevisions {
    $env:BACKEND_BUILD_REVISION = Get-BuildRevision -Scope 'backend'
    $env:ADMIN_UI_BUILD_REVISION = Get-BuildRevision -Scope 'admin-ui'
}

# 枚举构建输入, 不逐文件调用PowerShell文件提供程序; 私有文件和工具缓存不参与摘要.
function Get-BuildSourceFiles {
    param([string]$Path)
    $Pending = [Collections.Generic.Stack[IO.FileSystemInfo]]::new()
    $Pending.Push((Get-Item -LiteralPath $Path -Force))
    while ($Pending.Count) {
        $Item = $Pending.Pop()
        $Directory = $Item -is [IO.DirectoryInfo]
        if ($Directory -and $Item.Name -in @('node_modules', 'target', 'dist', '.git', '.idea', '.vscode', '.vite', '.cache', 'coverage', '.tools', '.pnpm-store', '__pycache__')) {
            continue
        }
        if ($Item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
            throw "构建输入不允许使用链接文件或目录: $($Item.FullName)"
        }
        if ($Directory) {
            foreach ($Child in $Item.EnumerateFileSystemInfos()) { $Pending.Push($Child) }
        } elseif ($Item.Name -notmatch '^(application-local.*|environment\.local\.js|project(\.private)?\.config\.json|\.env(\..*)?|.*\.(log|tmp|key|pem|p12))$' -or
            $Item.FullName -eq $EnvExample) {
            $Item
        }
    }
}

# 仅重建本项目数据库和Redis卷, 人工确认后才执行.
function Reset-TestDataVolumes {
    $ConfigText = & docker compose -p platform-wxshop --env-file $EnvFile -f $ComposeFile config --format json
    if ($LASTEXITCODE -ne 0) { throw '无法解析Compose数据卷。' }
    $Config = ($ConfigText -join "`n") | ConvertFrom-Json
    $Volumes = @($Config.volumes.'mysql-data'.name, $Config.volumes.'redis-data'.name)
    foreach ($Volume in $Volumes) {
        if (-not $Volume -or $Volume -notmatch '^[a-zA-Z0-9][a-zA-Z0-9_.-]*$') { throw '数据卷名称不合法。' }
        # 完整JSON避免Windows PowerShell5.1剥离Go模板中的双引号, 读取失败不得继续删除.
        $Metadata = & docker volume inspect $Volume
        if ($LASTEXITCODE -ne 0) { throw "无法核对数据卷归属: $Volume" }
        $Details = ($Metadata -join "`n") | ConvertFrom-Json
        $Owner = $Details[0].Labels.'com.docker.compose.project'
        if ($Owner -cne $Config.name) { throw '拒绝操作其他项目的数据卷。' }
    }
    Write-Warning "将删除数据库和缓存卷: $($Volumes -join ', '), 上传图片卷保留。此入口仅用于当前可丢弃的本地开发数据。"
    if ((Read-Host '输入 RESET_DATA 确认删除数据库和缓存') -cne 'RESET_DATA') { throw '已取消数据重置。' }
    Invoke-Compose -Arguments @('down', '--remove-orphans')
    foreach ($Volume in $Volumes) {
        & docker volume inspect $Volume 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            & docker volume rm $Volume
            if ($LASTEXITCODE -ne 0) { throw "数据卷删除失败: $Volume" }
        }
    }
}

# 执行统一的 Compose 命令 / Runs a unified Compose command.
function Invoke-Compose {
    param([Parameter(Mandatory = $true)][string[]]$Arguments)

    & docker compose -p platform-wxshop --env-file $EnvFile -f $ComposeFile @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose 执行失败：$($Arguments -join ' ')"
    }
}

# 捕获短小的只读Docker元数据, 按退出码处理原生stderr而不是让5.1提前抛错.
function Invoke-DockerMetadata {
    param([Parameter(Mandatory = $true)][string[]]$Arguments)
    $ErrorActionPreference = 'Continue'
    $Text = @(& docker @Arguments 2>$null)
    return [pscustomobject]@{ ExitCode = $LASTEXITCODE; Output = $Text }
}

# 两个Java服务共用同一镜像, 只选一个服务构建.
function Get-LocalImages {
    [pscustomobject]@{ Service = 'mysql'; Name = 'm.daocloud.io/docker.io/library/mysql:8.0.36'; Digest = $null }
    [pscustomobject]@{ Service = 'redis'; Name = 'm.daocloud.io/docker.io/library/redis:7.2-alpine'; Digest = $null }
    [pscustomobject]@{ Service = 'platform-admin'; Name = 'carat-craft-backend:local'; Digest = $env:BACKEND_BUILD_REVISION }
    [pscustomobject]@{ Service = 'nginx'; Name = 'carat-craft-admin-ui:local'; Digest = $env:ADMIN_UI_BUILD_REVISION }
}

# 根据构建输入摘要校验镜像, Git提交仅用于追溯.
function Assert-LocalImages {
    foreach ($Target in Get-LocalImages) {
        $Result = Invoke-DockerMetadata -Arguments @('image', 'inspect', $Target.Name)
        if ($Result.ExitCode -ne 0) { throw "缺少镜像 $($Target.Name), 请先运行 build-local-test.cmd 构建镜像。" }
        if ($Target.Service -in @('mysql', 'redis')) { continue }
        $Image = ($Result.Output -join "`n") | ConvertFrom-Json
        if ($Image[0].Config.Labels.'io.caratcraft.source-digest' -cne $Target.Digest) {
            throw "镜像 $($Target.Name) 的构建输入已变更, 请先运行 build-local-test.cmd。"
        }
    }
}

# 检查卷的存在性和项目归属, 初始化与标准启动采取相反条件.
function Assert-DataVolumes {
    param([switch]$New)
    foreach ($Kind in @('mysql-data', 'redis-data')) {
        $Name = "platform-wxshop_$Kind"
        $Result = Invoke-DockerMetadata -Arguments @('volume', 'inspect', $Name)
        if ($New) {
            if ($Result.ExitCode -eq 0) { throw "数据卷 $Name 已存在, 初始化已停止。" }
            continue
        }
        if ($Result.ExitCode -ne 0) { throw "缺少数据卷 $Name, 新环境请先执行 init-db。" }
        $Volume = ($Result.Output -join "`n") | ConvertFrom-Json
        if ($Volume[0].Labels.'com.docker.compose.project' -cne 'platform-wxshop') { throw "数据卷归属不匹配: $Name" }
    }
}

# 仅为尚无数据库和缓存卷的环境初始化服务.
function Initialize-LocalDatabase {
    Assert-DataVolumes -New
    $env:CARATCRAFT_DB_INIT = 'true'
    try {
        Invoke-Compose -Arguments @('up', '--detach', '--no-build', '--pull', 'never', '--wait', '--wait-timeout', '300', 'mysql', 'redis')
        Assert-DatabaseReady
        Invoke-AutomaticMigrations -Baseline
    } finally {
        $env:CARATCRAFT_DB_INIT = 'false'
        # 只移除本次初始化容器, 保留数据库卷和初始化结果.
        Invoke-Compose -Arguments @('rm', '--stop', '--force', 'mysql')
    }
}

# 执行选定的独立增量脚本, 执行前明确确认目标库和备份.
function Invoke-LocalDatabaseUpgrade {
    if (-not $Upgrade) { throw '请选择 -Upgrade admin-jobs 或 staff-password。' }
    Assert-DataVolumes
    if ((Read-Host "确认已备份并核对目标库, 输入 UPGRADE_DB 执行 $Upgrade") -cne 'UPGRADE_DB') { throw '已取消数据库升级。' }
    $Sql = Get-Content -LiteralPath (Join-Path $ProjectRoot "database/upgrade/$Upgrade/apply.sql") -Raw -Encoding UTF8
    $OutputEncoding = [Text.UTF8Encoding]::new($false)
    $Sql | & docker compose -p platform-wxshop --env-file $EnvFile -f $ComposeFile exec -T mysql sh -c 'export MYSQL_PWD=$MYSQL_ROOT_PASSWORD; exec mysql --default-character-set=utf8mb4 -uroot --database=$MYSQL_DATABASE'
    if ($LASTEXITCODE -ne 0) { throw '升级失败, 请核对增量 SQL 输出。' }
}

# 经标准输入传递只读SQL, 错误时保留所有数据.
function Invoke-DatabaseQuery {
    param([string]$Query, [switch]$ApplicationUser)
    $ErrorActionPreference = 'Continue'
    $Command = if ($ApplicationUser) {
        'export MYSQL_PWD=$MYSQL_PASSWORD; exec mysql -u$MYSQL_USER --default-character-set=utf8mb4 --batch --skip-column-names --database=$MYSQL_DATABASE'
    } else {
        'export MYSQL_PWD=$MYSQL_ROOT_PASSWORD; exec mysql -uroot --default-character-set=utf8mb4 --batch --skip-column-names --database=$MYSQL_DATABASE'
    }
    $Text = @($Query | & docker compose -p platform-wxshop --env-file $EnvFile -f $ComposeFile exec -T mysql sh -c $Command 2>&1)
    if ($LASTEXITCODE -ne 0) {
        if (($Text -join "`n") -match 'ERROR\s+(1044|1045|1049)\b') {
            throw '本地数据库凭据或库名不匹配, 请检查原配置。'
        }
        $Detail = ($Text -join ' ')
        if ($Detail.Length -gt 500) { $Detail = $Detail.Substring(0, 500) }
        throw "无法检查MySQL开发库: $Detail。本次不自动重建, 请检查连接或SQL执行错误。"
    }
    return $Text
}

# 核对唯一完整初始化版本, 遇到旧结构时停止.
function Assert-DatabaseSchema {
    $Rows = @(Invoke-DatabaseQuery -Query 'SELECT version FROM CC_SCHEMA_VERSION WHERE id=1;')
    if ($Rows.Count -eq 0 -or ($Rows.Count -eq 1 -and $Rows[0] -eq '0')) {
        throw '本地开发库尚未完成当前完整初始化。'
    }
    if ($Rows.Count -ne 1 -or $Rows[0] -cne '1') { throw '未知数据库结构版本, 停止启动。' }
}

# 在启动应用前检查基础表和完整初始化标识, 不以MySQL可连接代替初始化完成.
function Assert-DatabaseReady {
    $Query = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('sys_region','mall_order','mall_user','mall_goods','CC_SCHEMA_VERSION');"
    $Tables = Invoke-DatabaseQuery -Query $Query
    if ($LASTEXITCODE -ne 0) { throw '无法检查数据库结构, 请检查MySQL认证和日志。' }
    $Missing = @('sys_region', 'mall_order', 'mall_user', 'mall_goods', 'CC_SCHEMA_VERSION') |
        Where-Object { $_ -cnotin $Tables }
    if ($Missing) {
        throw "本地开发库结构不是当前版本, 缺少: $($Missing -join ', ')。"
    }
    Assert-DatabaseSchema
    $RequiredColumns = Get-Content -LiteralPath (Join-Path $ProjectRoot 'deploy/schema-preflight.sql') -Raw -Encoding UTF8
    $MissingColumns = @(Invoke-DatabaseQuery -Query $RequiredColumns)
    if ($MissingColumns.Count) {
        throw "本地库缺少必需字段: $($MissingColumns -join ', ')。请参照项目维护文档备份并执行对应增量, 数据卷已保留。"
    }
    $AccountTiers = Invoke-DatabaseQuery -Query "SELECT IF(EXISTS(SELECT 1 FROM SYS_USER WHERE USER_ID='1' AND ACCOUNT_TYPE='PLATFORM_ADMIN') AND NOT EXISTS(SELECT 1 FROM SYS_USER WHERE ACCOUNT_TYPE IS NULL OR (USER_ID<>'1' AND ACCOUNT_TYPE NOT IN ('MERCHANT_ADMIN','EMPLOYEE'))),1,0);"
    if (($AccountTiers -join '').Trim() -ne '1') { throw '本地库的账号层级尚未完成迁移, 停止应用启动。' }
    $Application = Invoke-DatabaseQuery -Query 'SELECT 1;' -ApplicationUser
    if (($Application -join '').Trim() -ne '1') {
        throw 'MySQL应用账号连通性检查未返回预期结果。'
    }
}

# 编排显式清单中的SQL和指纹, 没有目标库副作用.
function New-AutomaticMigrationBatch {
    param([string[]]$Completed = @(), [switch]$Baseline)
    $Root = Join-Path $ProjectRoot 'database/upgrade'
    $Registry = Join-Path $Root 'local'
    $Entries = @([IO.File]::ReadAllLines((Join-Path $Registry 'manifest.txt')))
    $Ids = @()
    $Previous = 0
    foreach ($Entry in $Entries) {
        if ($Entry -cnotmatch '^([0-9]{4}) ([a-z][a-z0-9-]{0,48})$' -or [int]$Matches[1] -le $Previous) {
            throw "本机迁移清单条目无效或顺序错误: $Entry"
        }
        $Previous = [int]$Matches[1]
        $Ids += "$($Matches[1])-$($Matches[2])"
    }
    if (!$Ids) { throw '本机迁移清单不能为空。' }
    $IdList = ($Ids | ForEach-Object { "'$_'" }) -join ','
    $Sql = [Collections.Generic.List[string]]::new()
    $Pending = [Collections.Generic.List[string]]::new()
    $Before = [IO.File]::ReadAllText((Join-Path $Registry "before.sql"))
    $Sql.Add([IO.File]::ReadAllText((Join-Path $Registry 'begin.sql')).Replace('__IDS__', $IdList))
    for ($Index = 0; $Index -lt $Entries.Count; $Index++) {
        $Id = $Ids[$Index]
        $Directory = Join-Path $Root ($Entries[$Index] -split ' ', 2)[1]
        $Check = Join-Path $Directory 'check.sql'
        $Apply = Join-Path $Directory 'apply.sql'
        $Hasher = [Security.Cryptography.SHA256]::Create()
        try {
            $Hashes = foreach ($Source in @($Check, $Apply)) {
                [BitConverter]::ToString($Hasher.ComputeHash([IO.File]::ReadAllBytes($Source))).Replace('-', '')
            }
            $Digest = [BitConverter]::ToString($Hasher.ComputeHash([Text.Encoding]::ASCII.GetBytes(($Hashes -join '')))).Replace('-', '')
        } finally { $Hasher.Dispose() }
        $IsComplete = $Id -cin $Completed
        $Sql.Add($Before.Replace('__ID__', $Id).Replace('__SHA256__', $Digest).Replace('__COMPLETED__', [string][int]$IsComplete))
        if ($IsComplete) {
            $Sql.Add("SELECT '${Id}: verified';")
            continue
        }
        if ($Baseline) {
            $Pending.Add("INSERT INTO CC_SCHEMA_MIGRATION VALUES ('$Id', '$Digest', CURRENT_TIMESTAMP);")
            $Pending.Add("SELECT '${Id}: baseline';")
            continue
        }
        $Pending.Add('SET @cc_compatible = NULL, @cc_complete = NULL;')
        foreach ($File in @($Check, 'apply.sql', $Apply, $Check, 'after.sql')) {
            $Source = if ([IO.Path]::IsPathRooted($File)) { $File } else { Join-Path $Registry $File }
            $Pending.Add([IO.File]::ReadAllText($Source).Replace('__ID__', $Id).Replace('__SHA256__', $Digest))
        }
    }
    $Contract = [IO.File]::ReadAllText((Join-Path $Registry 'current-schema.sql'))
    if ($Baseline) {
        $Sql.Add($Contract)
        $Sql.Add('START TRANSACTION;')
        $Sql.AddRange($Pending)
        $Sql.Add('COMMIT;')
    } else {
        $Sql.AddRange($Pending)
        $Sql.Add($Contract)
    }
    return ($Sql -join [char]10)
}

# 用同一MySQL会话校验、执行并登记安全结构增量, 断开连接时释放锁.
function Invoke-AutomaticMigrations {
    param([switch]$Baseline)
    $Encoding = $OutputEncoding
    try {
        $OutputEncoding = [Text.UTF8Encoding]::new($false)
        $Snapshot = [IO.File]::ReadAllText((Join-Path $ProjectRoot 'database/upgrade/local/snapshot.sql'))
        $Completed = @(Invoke-DatabaseQuery -Query $Snapshot)
        $Rows = @(Invoke-DatabaseQuery -Query (New-AutomaticMigrationBatch -Completed $Completed -Baseline:$Baseline))
        foreach ($Row in $Rows) {
            if ($Row -match '^([0-9]{4}-[a-z0-9-]+): (verified|baseline|applying)$') {
                $Message = @{ verified = '已应用，指纹核对通过'; baseline = '结构已齐全，已登记'; applying = '增量已执行并核对结构' }[$Matches[2]]
                Write-Host "数据库迁移 $($Matches[1])：$Message"
            }
        }
    } finally { $OutputEncoding = $Encoding }
}

# 汇总有限的状态与健康探针错误, 保留原始启动异常供调用者判断失败.
function Show-StartupDiagnostics {
    try {
        $Result = Invoke-DockerMetadata -Arguments @('compose', '-p', 'platform-wxshop', '--env-file', $EnvFile, '-f', $ComposeFile, 'ps', '--all')
        if ($Result.ExitCode -eq 0) { $Result.Output | Write-Host }
        else { Write-Warning '暂时无法读取Compose状态, 原始启动错误仍保留。' }
    } catch { Write-Warning 'Compose诊断不可用, 原始启动错误仍保留。' }
    foreach ($Name in @('platform-mysql', 'platform-redis', 'platform-admin', 'platform-api', 'platform-nginx')) {
        try {
            $Result = Invoke-DockerMetadata -Arguments @('container', 'inspect', $Name)
            if ($Result.ExitCode -ne 0) {
                Write-Host "$Name 尚未创建或状态暂不可读, 跳过健康检查。"
                continue
            }
            $Metadata = ($Result.Output -join "`n") | ConvertFrom-Json
            $State = $Metadata[0].State
            $Health = if ($State.Health) { $State.Health.Status } else { '未配置健康检查' }
            Write-Host "$Name 状态: $($State.Status), 健康: $Health"
        } catch { Write-Warning "$Name 状态诊断不可用, 原始启动错误仍保留。" }
    }
    Write-Warning '后端日志: .\dev.ps1 -Action logs. 按原始失败阶段排查, 详见docs/项目维护文档.md。'
}

# 只构建镜像, 保留原始构建错误, 不操作运行中的容器或数据库.
function Build-LocalEnvironment {
    try {
        foreach ($Target in Get-LocalImages) {
            $Result = Invoke-DockerMetadata -Arguments @('image', 'inspect', $Target.Name)
            if ($Target.Service -in @('mysql', 'redis')) {
                if ($Result.ExitCode -ne 0) { Invoke-Compose -Arguments @('pull', $Target.Service) }
                continue
            }
            if ($Result.ExitCode -eq 0) {
                $Image = ($Result.Output -join "`n") | ConvertFrom-Json
                if ($Image[0].Config.Labels.'io.caratcraft.source-digest' -ceq $Target.Digest) {
                    Write-Host "复用镜像 $($Target.Name), 构建输入未变更。"
                    continue
                }
            }
            Write-Host "正在构建 $($Target.Name)..."
            Invoke-Compose -Arguments @('build', $Target.Service)
        }
        Write-Host '镜像检查和按需构建完成。'
    } catch {
        Write-Warning '构建失败, 已停止后续步骤。请查看上方具体错误; 下载失败请检查m.daocloud.io、maven.aliyun.com及registry.npmmirror.com的网络访问或限流提示, 然后重新运行build-local-test.cmd。'
        throw
    }
}

# 启动已验证镜像, 保留数据容器和数据卷, 逐项核对运行镜像.
function Start-LocalEnvironment {
    param([switch]$RestartApps)
    $Stage = '检查构建输入和镜像'
    try {
        Assert-DataVolumes
        if (!$NoBuild) { Build-LocalEnvironment }
        Assert-LocalImages
        $Stage = 'MySQL和Redis'
        Invoke-Compose -Arguments @('up', '--detach', '--no-recreate', '--no-build', '--pull', 'never', '--wait', '--wait-timeout', '300', 'mysql', 'redis')
        $Stage = '数据库结构及账号检查'
        Assert-DatabaseReady
        $Stage = '数据库安全结构增量'
        Invoke-AutomaticMigrations
        $Stage = '后端和管理端'
        if ($RestartApps) { Invoke-Compose -Arguments @('restart', 'platform-admin', 'platform-api', 'nginx') }
        Invoke-Compose -Arguments @('up', '--detach', '--no-deps', '--no-build', '--pull', 'never', '--wait', '--wait-timeout', '300', 'platform-admin', 'platform-api', 'nginx')
        $Stage = '运行镜像、健康检查和 Nginx 代理'
        $Names = @('platform-admin', 'platform-api', 'platform-nginx')
        $ContainerResult = Invoke-DockerMetadata -Arguments (@('container', 'inspect') + $Names)
        if ($ContainerResult.ExitCode -ne 0) { throw '无法核对应用容器。' }
        $Containers = ($ContainerResult.Output -join "`n") | ConvertFrom-Json
        $ImageResult = Invoke-DockerMetadata -Arguments @('image', 'inspect', 'carat-craft-backend:local', 'carat-craft-admin-ui:local')
        if ($ImageResult.ExitCode -ne 0) { throw '无法核对构建后的本地镜像。' }
        $Images = ($ImageResult.Output -join "`n") | ConvertFrom-Json
        foreach ($Container in $Containers) {
            $Ui = $Container.Name -eq '/platform-nginx'
            $Expected = if ($Ui) { $Images[1] } else { $Images[0] }
            $Digest = if ($Ui) { $env:ADMIN_UI_BUILD_REVISION } else { $env:BACKEND_BUILD_REVISION }
            if ($Container.Image -cne $Expected.Id -or $Container.Config.Labels.'io.caratcraft.source-digest' -cne $Digest -or
                $Container.State.Health.Status -cne 'healthy') {
                throw "$($Container.Name) 的运行镜像或健康状态不符合预期。"
            }
            Write-Host "$($Container.Name.TrimStart('/'))：镜像一致，健康"
        }
        $Probe = Invoke-DockerMetadata -Arguments @('exec', 'platform-nginx', 'sh', '-ec',
            'wget -q -T 5 -t 1 -O /dev/null http://127.0.0.1/; status=$(wget -S -T 5 -t 1 -O /dev/null http://127.0.0.1/platform-framework/ 2>&1) || :; case $status in *401\ Unauthorized*) ;; *) exit 1;; esac; api=$(wget -q -T 5 -t 1 -O - http://127.0.0.1/platform-framework-api/); case $api in *platform-framework-api/doc.html) ;; *) exit 1;; esac')
        if ($Probe.ExitCode -ne 0) {
            throw 'Nginx 静态入口、后台根路径或 API 根路径探测失败。'
        }
        Write-Host '代理验证通过：管理台、后台与 API 根路径。'
        Show-AccessInfo
    } catch {
        Write-Warning "启动失败阶段: $Stage。数据卷已保留。"
        try { Show-StartupDiagnostics }
        catch { Write-Warning '附加诊断失败, 以下仍返回原始启动错误。' }
        throw
    }
}

# 输出本地访问地址 / Prints local access URLs.
function Show-AccessInfo {
    $PortLine = Get-Content -LiteralPath $EnvFile -Encoding UTF8 |
        Where-Object { $_ -match '^NGINX_PORT=' } |
        Select-Object -Last 1
    $Port = if ($PortLine) { ($PortLine -split '=', 2)[1] } else { '8888' }
    Write-Host ''
    Write-Host 'CaratCraft 已启动：'
    Write-Host "  管理台：http://localhost:$Port"
    Write-Host "  后台接口：http://localhost:$Port/platform-framework"
    Write-Host "  商城接口：http://localhost:$Port/platform-framework-api"
    Write-Host '  小程序配置：执行 dev.ps1 -Action miniapp 生成。'
}

if ($Action -eq 'build' -and $NoBuild) { throw 'build 不能与 -NoBuild 同时使用。' }
if ($Action -eq 'configure') {
    Initialize-Environment
    return
}
if ($Action -in @('miniapp', 'diagnose', 'check-config', 'repair-config')) {
    & node (Join-Path $ProjectRoot 'tools/maintenance/dev-env.cjs') $Action
    if ($LASTEXITCODE -ne 0) { throw "环境工具执行失败: $Action" }
    return
}

Assert-DockerReady
Clear-ComposeOverrides
if ($Action -eq 'build') {
    $EnvFile = $EnvExample
    $Watch = [Diagnostics.Stopwatch]::StartNew()
    try {
        Assert-HostResources
        Set-BuildRevisions
        Set-GitRevision
        Build-LocalEnvironment
    } finally { Write-Host ('构建耗时：{0:N2} 秒' -f $Watch.Elapsed.TotalSeconds) }
    return
}
if (-not (Test-Path -LiteralPath $EnvFile)) { throw '缺少 deploy/.env, 请先执行 configure。' }
switch ($Action) {
    'stop' { Invoke-Compose -Arguments @('down', '--remove-orphans') }
    'status' { Invoke-Compose -Arguments @('ps') }
    'logs' { Invoke-Compose -Arguments @('logs', '--follow', '--tail=200') }
    'upgrade-db' { [void](Assert-StartupConfiguration); Invoke-LocalDatabaseUpgrade }
    'reset-db' { [void](Assert-StartupConfiguration); Reset-TestDataVolumes }
    'init-db' { Assert-HostResources -Values (Assert-StartupConfiguration); Initialize-LocalDatabase }
    default {
        $Watch = [Diagnostics.Stopwatch]::StartNew()
        try {
            Assert-HostResources -Values (Assert-StartupConfiguration)
            Set-BuildRevisions
            Set-GitRevision
            Start-LocalEnvironment -RestartApps:($Action -eq 'restart')
        } finally { Write-Host ('启动及验收耗时：{0:N2} 秒' -f $Watch.Elapsed.TotalSeconds) }
    }
}
