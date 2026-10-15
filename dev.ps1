[CmdletBinding()]
param(
    [ValidateSet('start', 'stop', 'restart', 'status', 'logs')]
    [string]$Action = 'start',
    [switch]$ResetData,
    [switch]$NoBuild,
    [switch]$LegacyBuild
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

Assert-DockerReady
Initialize-Environment

if ($LegacyBuild) {
    Enable-LegacyBuild
}

if ($ResetData -and $Action -notin @('start', 'restart')) {
    throw '-ResetData 只能与 start 或 restart 一起使用。'
}

switch ($Action) {
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
        if ($ResetData) { Reset-TestDataVolumes } else { Invoke-Compose -Arguments @('down', '--remove-orphans') }
        $UpArguments = @('up', '--detach', '--wait', '--wait-timeout', '300')
        if (-not $NoBuild) { $UpArguments += '--build' }
        Invoke-Compose -Arguments $UpArguments
        Show-AccessInfo
    }
    default {
        Assert-StartupConfiguration
        if ($ResetData) { Reset-TestDataVolumes }
        $UpArguments = @('up', '--detach', '--wait', '--wait-timeout', '300')
        if (-not $NoBuild) { $UpArguments += '--build' }
        Invoke-Compose -Arguments $UpArguments
        Show-AccessInfo
    }
}
