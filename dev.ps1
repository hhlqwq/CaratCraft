[CmdletBinding()]
param(
    [ValidateSet('start', 'stop', 'restart', 'status', 'logs')]
    [string]$Action = 'start',
    [switch]$ResetData,
    [switch]$NoBuild
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
        Copy-Item -LiteralPath $EnvExample -Destination $EnvFile
        Write-Host "已生成默认配置：$EnvFile"
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
        $DownArguments = @('down', '--remove-orphans')
        if ($ResetData) {
            $DownArguments += '--volumes'
            Write-Warning '将删除 CaratCraft 的 MySQL 和 Redis 测试数据卷。'
        }
        Invoke-Compose -Arguments $DownArguments
        $UpArguments = @('up', '--detach', '--wait', '--wait-timeout', '300')
        if (-not $NoBuild) { $UpArguments += '--build' }
        Invoke-Compose -Arguments $UpArguments
        Show-AccessInfo
    }
    default {
        if ($ResetData) {
            Write-Warning '将删除 CaratCraft 的 MySQL 和 Redis 测试数据卷。'
            Invoke-Compose -Arguments @('down', '--volumes', '--remove-orphans')
        }
        $UpArguments = @('up', '--detach', '--wait', '--wait-timeout', '300')
        if (-not $NoBuild) { $UpArguments += '--build' }
        Invoke-Compose -Arguments $UpArguments
        Show-AccessInfo
    }
}
