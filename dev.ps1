[CmdletBinding()]
param(
    [ValidateSet('build', 'start', 'stop', 'restart', 'status', 'logs')]
    [string]$Action = 'start',
    [switch]$ResetData,
    [switch]$NoBuild,
    [switch]$LegacyBuild,
    [switch]$OnlineBuild,
    [string]$NodePath,
    [string]$MavenCache
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Join-Path $PSScriptRoot 'vendor\platform-wxshop'
$ComposeFile = Join-Path $ProjectRoot 'docker-compose.yml'
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

# 单独记录源码版本, 构建不依赖数据库、Redis或JWT运行密钥.
function Set-BuildRevision {
    $Revision = & git -C $PSScriptRoot rev-parse --short HEAD 2>$null
    if ($LASTEXITCODE -eq 0) {
        $env:BUILD_REVISION = $Revision.Trim()
        $Dirty = & git -C $PSScriptRoot status --porcelain --untracked-files=normal 2>$null
        if ($Dirty) { $env:BUILD_REVISION += '-dirty' }
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
        $Owner = & docker volume inspect --format '{{index .Labels "com.docker.compose.project"}}' $Volume 2>$null
        if ($LASTEXITCODE -eq 0 -and $Owner.Trim() -ne $Config.name) { throw '拒绝操作其他项目的数据卷。' }
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

# 启用传统构建器兼容模式 / Enables legacy builder compatibility mode.
function Enable-LegacyBuild {
    $env:COMPOSE_BAKE = 'false'
    $env:DOCKER_BUILDKIT = '0'
    Write-Warning '已启用传统构建器兼容模式：本次构建不会使用 BuildKit 缓存。'
}

# 执行统一的 Compose 命令 / Runs a unified Compose command.
function Invoke-Compose {
    param([Parameter(Mandatory = $true)][string[]]$Arguments)

    & docker compose --env-file $EnvFile -f $ComposeFile @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose 执行失败：$($Arguments -join ' ')"
    }
}

# 核对免构建模式的镜像来源, 拒绝缺少新版探针的历史镜像.
function Assert-LocalImages {
    foreach ($Name in @('carat-craft-backend:local', 'carat-craft-admin-ui:local')) {
        $Metadata = & docker image inspect $Name 2>$null
        if ($LASTEXITCODE -ne 0) { throw "缺少镜像 $Name, 请先运行 build-local-test.cmd 构建镜像, 或使用 .\dev.ps1 -Action build; -NoBuild 无法创建镜像。" }
        $Image = ($Metadata -join "`n") | ConvertFrom-Json
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
        throw "数据库初始化或升级不完整, 缺少: $($Missing -join ', ')。请先备份并按use.md修复, 不要直接重置数据卷。"
    }
    $Version = Invoke-DatabaseQuery -Query "SELECT COUNT(*) FROM CC_SCHEMA_MIGRATION WHERE version='V20261021_04_menu_encoding.sql';"
    if ($LASTEXITCODE -ne 0 -or ($Version -join '').Trim() -ne '1') {
        throw '数据库尚未完成V20261021_04迁移, 请先按use.md备份并执行版本迁移。'
    }
}

# 汇总有限的状态与健康探针错误, 保留原始启动异常供调用者判断失败.
function Show-StartupDiagnostics {
    & docker compose --env-file $EnvFile -f $ComposeFile ps --all
    foreach ($Name in @('platform-admin', 'platform-api')) {
        & docker inspect --format '{{json .State.Health}}' $Name 2>$null
    }
    Write-Warning '后端日志: .\dev.ps1 -Action logs. 缺表请先备份, 旧镜像请重新构建, 详见use.md。'
}

# 只构建镜像, 保留原始构建错误, 不操作运行中的容器或数据库.
function Build-LocalEnvironment {
    try {
        if ($OnlineBuild -or $LegacyBuild) {
            Invoke-Compose -Arguments @('build')
        } else {
            $Options = @{Root = $PSScriptRoot; Revision = $env:BUILD_REVISION}
            if ($NodePath) { $Options.NodePath = $NodePath }
            if ($MavenCache) { $Options.MavenCache = $MavenCache }
            & (Join-Path $PSScriptRoot 'tools/Build-CachedImages.ps1') @Options
        }
        Write-Host '镜像构建完成, 请运行 start-local-test.cmd 启动环境。'
    } catch {
        Write-Warning '镜像构建失败, 请查看上方具体错误. 默认使用本机缓存, 缺少缓存时请联网准备依赖或运行build-local-test.cmd -OnlineBuild. 本操作未启动或重启服务, 请修复构建错误后重试。'
        throw
    }
}

# 只启动已校验的本地镜像, 检查数据服务与结构并等待全部应用就绪.
function Start-LocalEnvironment {
    try {
        Assert-LocalImages
        Invoke-Compose -Arguments @('up', '--detach', '--wait', '--wait-timeout', '300', 'mysql', 'redis')
        Assert-DatabaseReady
        Invoke-Compose -Arguments @('up', '--detach', '--no-build', '--wait', '--wait-timeout', '300')
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
if ($LegacyBuild -and $Action -ne 'build') { throw '-LegacyBuild 仅用于构建, 请运行 build-local-test.cmd -LegacyBuild。' }
if (($OnlineBuild -or $NodePath -or $MavenCache) -and $Action -ne 'build') { throw '构建参数只能与 -Action build 一起使用。' }

Assert-DockerReady
if ($Action -eq 'build') {
    # Compose构建只需要变量模板, 不为编译生成或校验本机运行秘密.
    if (-not (Test-Path -LiteralPath $EnvFile)) { $EnvFile = $EnvExample }
} else {
    Initialize-Environment
}

if ($LegacyBuild) {
    Enable-LegacyBuild
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
