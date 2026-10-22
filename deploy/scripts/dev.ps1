[CmdletBinding()]
param(
    [ValidateSet('build', 'start', 'stop', 'restart', 'status', 'logs')]
    [string]$Action = 'start',
    [switch]$ResetData,
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
        $Template = Get-Content -LiteralPath $EnvExample -Raw
        foreach ($Key in @('MYSQL_ROOT_PASSWORD', 'MYSQL_APP_PASSWORD', 'REDIS_PASSWORD', 'JWT_SECRET')) {
            $RandomBytes = New-Object byte[] 32
            $Generator = [Security.Cryptography.RandomNumberGenerator]::Create()
            try { $Generator.GetBytes($RandomBytes) } finally { $Generator.Dispose() }
            $Secret = [Convert]::ToBase64String($RandomBytes)
            $Template = [regex]::Replace($Template, "(?m)^$Key=.*$", "$Key=$Secret")
        }
        [IO.File]::WriteAllText($EnvFile, $Template, [Text.UTF8Encoding]::new($false))
        Write-Host "已生成本机独立随机配置：$EnvFile, 密钥不在终端输出。"
    }
}

# 启动前检查秘密配置, 已有数据库需要人工创建/授权应用账号而不是直接改旧卷密码.
function Assert-StartupConfiguration {
    $Values = @{}
    foreach ($Line in (Get-Content -LiteralPath $EnvFile)) {
        if ($Line -match '^([A-Z_]+)=(.*)$') { $Values[$Matches[1]] = $Matches[2].Trim() }
    }
    foreach ($Key in @('MYSQL_ROOT_PASSWORD', 'MYSQL_APP_PASSWORD', 'REDIS_PASSWORD', 'JWT_SECRET')) {
        if (-not $Values[$Key] -or $Values[$Key].Length -lt 24 -or $Values[$Key] -in @('REDACTED_CREDENTIAL', '123456')) {
            throw "$Key 缺失或过弱。已有数据卷请先按迁移说明建立独立应用账号和轮换密码, 不要直接重置数据。"
        }
    }
    if ($Values['MYSQL_APP_USER'] -eq 'root') { throw '应用账号不得使用root。' }
}

# 根据交付源码计算稳定摘要, ZIP解压环境无需安装Git且修改源码后必须重新构建.
function Set-BuildRevision {
    Write-Host '正在计算项目源码标识...'
    $Inputs = [IO.File]::ReadAllLines((Join-Path $ProjectRoot 'deploy/build-inputs.txt'))
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
                Write-Host "源码摘要进度: $Processed/$($BuildFiles.Count)"
            }
        }
        $Bytes = [Text.Encoding]::UTF8.GetBytes($Manifest.ToString())
        $env:BUILD_REVISION = [BitConverter]::ToString($Hasher.ComputeHash($Bytes)).Replace('-', '').ToLowerInvariant()
    } finally {
        $Hasher.Dispose()
    }
}

# 递归收集构建输入, 在进入目录前排除本机依赖、输出、私人配置和开发工具缓存.
function Get-BuildSourceFiles {
    param([string]$Path)
    $Item = Get-Item -LiteralPath $Path -Force
    if ($Item.PSIsContainer -and
        $Item.Name -in @('node_modules', 'target', 'dist', '.git', '.idea', '.vite', '.cache', 'coverage')) { return }
    if ($Item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw "构建输入不允许使用链接文件或目录: $Path"
    }
    if ($Item.PSIsContainer) {
        foreach ($Child in (Get-ChildItem -LiteralPath $Path -Force)) {
            Get-BuildSourceFiles -Path $Child.FullName
        }
    } elseif ($Item.Name -notmatch '^(application-local.*|\.env(\..*)?|.*\.(log|tmp|key|pem|p12))$' -or
        $Item.FullName -eq $EnvExample) {
        $Item
    }
}

# 只删除当前项目的数据库和Redis卷, 上传卷始终保留, 执行前要求明确确认.
function Reset-TestDataVolumes {
    $ConfigText = & docker compose --env-file $EnvFile -f $ComposeFile config --format json
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
    Write-Warning "将删除数据库和缓存卷: $($Volumes -join ', '), 上传图片卷保留。请先完成备份。"
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

    & docker compose --env-file $EnvFile -f $ComposeFile @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose 执行失败：$($Arguments -join ' ')"
    }
}

# 检查全部运行镜像并核对应用源码标识, 缺失时提示先运行构建脚本.
function Assert-LocalImages {
    foreach ($Name in @('m.daocloud.io/docker.io/library/mysql:8.0.36', 'm.daocloud.io/docker.io/library/redis:7.2-alpine', 'carat-craft-backend:local', 'carat-craft-admin-ui:local')) {
        $Metadata = & docker image inspect $Name 2>$null
        if ($LASTEXITCODE -ne 0) { throw "缺少镜像 $Name, 请先运行 build-local-test.cmd 构建镜像, 或使用 .\dev.ps1 -Action build; -NoBuild 无法创建镜像。" }
        $Image = ($Metadata -join "`n") | ConvertFrom-Json
        if ($Name -in @('m.daocloud.io/docker.io/library/mysql:8.0.36', 'm.daocloud.io/docker.io/library/redis:7.2-alpine')) { continue }
        $Revision = $Image[0].Config.Labels.'org.opencontainers.image.revision'
        if (-not $Revision -or $Revision -ne $env:BUILD_REVISION) {
            throw "镜像 $Name 的源码标识与当前代码不一致, 请先运行 build-local-test.cmd 更新镜像; -NoBuild 不会更新源码。"
        }
    }
}

# 经标准输入传递只读SQL, 避免Windows PowerShell 5.1丢失原生命令参数中的引号.
function Invoke-DatabaseQuery {
    param([string]$Query)
    $Command = 'export MYSQL_PWD=$MYSQL_ROOT_PASSWORD; exec mysql -uroot --batch --skip-column-names --database=$MYSQL_DATABASE'
    $Query | & docker compose --env-file $EnvFile -f $ComposeFile exec -T mysql sh -c $Command
    if ($LASTEXITCODE -ne 0) { throw '无法检查数据库结构, 请检查MySQL认证和日志。' }
}

# 在启动应用前检查基础表和迁移版本, 不以MySQL可连接代替初始化完成.
function Assert-DatabaseReady {
    $Query = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('sys_region','mall_order','mall_user','mall_goods','CC_SCHEMA_MIGRATION');"
    $Tables = Invoke-DatabaseQuery -Query $Query
    if ($LASTEXITCODE -ne 0) { throw '无法检查数据库结构, 请检查MySQL认证和日志。' }
    $Missing = @('sys_region', 'mall_order', 'mall_user', 'mall_goods', 'CC_SCHEMA_MIGRATION') |
        Where-Object { $_ -cnotin $Tables }
    if ($Missing) {
        throw "数据库初始化或升级不完整, 缺少: $($Missing -join ', ')。请先备份并按docs/项目维护文档.md修复, 不要直接重置数据卷。"
    }
    $Version = Invoke-DatabaseQuery -Query "SELECT COUNT(*) FROM CC_SCHEMA_MIGRATION WHERE version='V20261022_01_sku_contract.sql';"
    if ($LASTEXITCODE -ne 0 -or ($Version -join '').Trim() -ne '1') {
        throw '数据库尚未完成V20261022_01迁移, 请先按docs/项目维护文档.md备份并执行版本迁移。'
    }
}

# 汇总有限的状态与健康探针错误, 保留原始启动异常供调用者判断失败.
function Show-StartupDiagnostics {
    & docker compose --env-file $EnvFile -f $ComposeFile ps --all
    foreach ($Name in @('platform-admin', 'platform-api')) {
        & docker inspect --format '{{json .State.Health}}' $Name 2>$null
    }
    Write-Warning '后端日志: .\dev.ps1 -Action logs. 缺表请先备份, 旧镜像请重新构建, 详见docs/项目维护文档.md。'
}

# 只构建镜像, 保留原始构建错误, 不操作运行中的容器或数据库.
function Build-LocalEnvironment {
    try {
        Write-Host '[1/2] 从国内来源下载MySQL和Redis镜像...'
        Invoke-Compose -Arguments @('pull', 'mysql', 'redis')
        Write-Host '[2/2] 从国内来源获取构建镜像和依赖, 在容器内编译前后端...'
        Invoke-Compose -Arguments @('build', '--pull', '--no-cache')
        Write-Host '镜像构建完成, 请运行 start-local-test.cmd 启动环境。'
    } catch {
        Write-Warning '构建失败, 已停止后续步骤。请查看上方具体错误; 下载失败请检查m.daocloud.io、maven.aliyun.com及registry.npmmirror.com的网络访问或限流提示, 然后重新运行build-local-test.cmd。'
        throw
    }
}

# 只启动已校验的本地镜像, 检查数据服务与结构并等待全部应用就绪.
function Start-LocalEnvironment {
    try {
        Write-Host '[1/3] 检查已构建镜像与源码标识...'
        Assert-LocalImages
        Write-Host '[2/3] 启动MySQL和Redis, 首次启动自动初始化空数据库...'
        Invoke-Compose -Arguments @('up', '--detach', '--no-build', '--pull', 'never', '--wait', '--wait-timeout', '300', 'mysql', 'redis')
        Assert-DatabaseReady
        Write-Host '[3/3] 启动后端和管理端, 等待健康检查通过...'
        Invoke-Compose -Arguments @('up', '--detach', '--no-build', '--pull', 'never', '--wait', '--wait-timeout', '300')
        Show-AccessInfo
    } catch {
        Show-StartupDiagnostics
        throw
    }
}

# 输出本地访问地址 / Prints local access URLs.
function Show-AccessInfo {
    $PortLine = Get-Content -LiteralPath $EnvFile |
        Where-Object { $_ -match '^NGINX_PORT=' } |
        Select-Object -Last 1
    $Port = if ($PortLine) { ($PortLine -split '=', 2)[1] } else { '8888' }
    Write-Host ''
    Write-Host 'CaratCraft 已启动：'
    Write-Host "  管理台：http://localhost:$Port"
    Write-Host "  后台接口：http://localhost:$Port/platform-framework"
    Write-Host "  商城接口：http://localhost:$Port/platform-framework-api"
    Write-Host "  小程序 API：http://localhost:$Port/platform-framework-api/app/"
}

if ($Action -eq 'build' -and $NoBuild) { throw 'build 不能与 -NoBuild 同时使用。' }

Assert-DockerReady
if ($Action -eq 'build') {
    # Compose构建只需要变量模板, 不为编译生成或校验本机运行秘密.
    $EnvFile = $EnvExample
} else {
    Initialize-Environment
}

if ($ResetData -and $Action -notin @('start', 'restart')) {
    throw '-ResetData 只能与 start 或 restart 一起使用。'
}

switch ($Action) {
    'build' {
        Set-BuildRevision
        Build-LocalEnvironment
    }
    'stop' {
        Invoke-Compose -Arguments @('down', '--remove-orphans')
    }
    'status' {
        Invoke-Compose -Arguments @('ps')
    }
    'logs' {
        Invoke-Compose -Arguments @('logs', '--follow', '--tail=200')
    }
    'restart' {
        Assert-StartupConfiguration
        Set-BuildRevision
        Assert-LocalImages
        if ($ResetData) { Reset-TestDataVolumes } else { Invoke-Compose -Arguments @('down', '--remove-orphans') }
        Start-LocalEnvironment
    }
    default {
        Assert-StartupConfiguration
        Set-BuildRevision
        if ($ResetData) { Reset-TestDataVolumes }
        Start-LocalEnvironment
    }
}
