[CmdletBinding()]
param(
    [string]$Root,
    [string]$Revision = 'workspace',
    [string]$NodePath,
    [string]$MavenCache,
    [switch]$CheckOnly
)

$ErrorActionPreference = 'Stop'
if (-not $Root) { $Root = Split-Path $PSScriptRoot -Parent }
$Root = (Resolve-Path -LiteralPath $Root).Path
$Project = Join-Path $Root 'vendor/platform-wxshop'
$Frontend = Join-Path $Project 'platform-admin-ui'

# 调用Docker并检查退出码, 构建容器禁止联网且从不挂载业务数据卷.
function Invoke-CachedDocker {
    param([string[]]$Arguments)
    & docker @Arguments
    if ($LASTEXITCODE -ne 0) { throw "离线构建失败: docker $($Arguments -join ' ')" }
}

# 按项目声明检查稳定版Node范围, 异常版本输出按不兼容处理而不是触发类型转换错误.
function Test-CachedNodeVersion {
    param([string]$Version)
    [version]$ParsedVersion = $null
    if ($Version -notmatch '^v\d+\.\d+\.\d+$' -or
        -not [version]::TryParse($Version.Substring(1), [ref]$ParsedVersion)) {
        return $false
    }
    return $ParsedVersion -ge [version]'22.13' -and $ParsedVersion -lt [version]'25.0'
}

# 从PATH或本机Codex运行时查找Node, 也允许显式指定只读可执行文件.
function Find-CachedNode {
    if ($NodePath) {
        if (-not (Test-Path -LiteralPath $NodePath -PathType Leaf)) { throw "Node文件不存在: $NodePath" }
        return (Resolve-Path -LiteralPath $NodePath).Path
    }
    $Candidates = @()
    $Available = Get-Command node -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($Available) { $Candidates += $Available.Source }
    $Candidates += Join-Path $env:USERPROFILE '.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin/node.exe'
    foreach ($Candidate in $Candidates) {
        if (Test-Path -LiteralPath $Candidate -PathType Leaf) {
            $Version = & $Candidate --version
            if ($LASTEXITCODE -eq 0 -and (Test-CachedNodeVersion -Version $Version)) { return $Candidate }
        }
    }
    throw '缺少兼容Node, 请准备Node >=22.13且<25, 或通过-NodePath指定node.exe。'
}

# 验证基础镜像和依赖缓存, 不自动拉取镜像或安装依赖.
function Assert-CachedPrerequisites {
    $Images = @(& docker image ls --format '{{.Repository}}:{{.Tag}}')
    if ($LASTEXITCODE -ne 0) { throw '无法读取本地镜像清单。' }
    $Missing = @('maven:3.9.11-eclipse-temurin-21', 'eclipse-temurin:21-jre', 'nginx:1.27-alpine') |
        Where-Object { $_ -notin $Images }
    if ($Missing) { throw "缺少本地基础镜像: $($Missing -join ', ')。请先联网拉取或使用docker load导入。" }
    if (-not (Test-Path -LiteralPath $MavenCache -PathType Container)) { throw "缺少Maven缓存: $MavenCache" }
    $Version = & $NodePath --version
    if ($LASTEXITCODE -ne 0 -or -not (Test-CachedNodeVersion -Version $Version)) {
        throw 'Node版本不符合>=22.13且<25的要求。'
    }
    & $NodePath (Join-Path $Root 'tools/check-cached-frontend.cjs') $Frontend
    if ($LASTEXITCODE -ne 0) { throw '前端缓存不完整或直接依赖版本不符, 请联网执行pnpm install --frozen-lockfile --ignore-scripts后重试。' }
}

# 将源码或许可复制到新的持久构建目录, 不沿用旧class并排除本机秘密配置.
function Copy-CachedBuildTree {
    param([string]$Source, [string]$Destination)
    $Source = (Resolve-Path -LiteralPath $Source).Path.TrimEnd('\', '/')
    New-Item -ItemType Directory -Path $Destination -Force | Out-Null
    foreach ($File in (Get-ChildItem -LiteralPath $Source -Recurse -File)) {
        if ($File.Name -eq 'application-local.yml') { continue }
        $Relative = $File.FullName.Substring($Source.Length + 1)
        $Target = Join-Path $Destination $Relative
        New-Item -ItemType Directory -Path (Split-Path $Target -Parent) -Force | Out-Null
        Copy-Item -LiteralPath $File.FullName -Destination $Target
    }
}

# 将前端输出固定到本次构建目录, 防止外部环境变量覆盖输出位置或沿用旧dist.
function Build-CachedFrontend {
    param([string]$Destination)
    New-Item -ItemType Directory -Path (Split-Path $Destination -Parent) -Force | Out-Null
    $PreviousOutput = $env:CARATCRAFT_UI_OUTPUT
    $PreviousStats = $env:CARATCRAFT_UI_STATS
    Push-Location $Frontend
    try {
        $env:CARATCRAFT_UI_OUTPUT = $Destination
        $env:CARATCRAFT_UI_STATS = Join-Path (Split-Path $Destination -Parent) 'frontend-stats.json'
        & $NodePath (Join-Path $Frontend 'node_modules/vite/bin/vite.js') build
        if ($LASTEXITCODE -ne 0) { throw '前端离线编译失败, 请检查上方错误, 不复用旧dist。' }
    } finally {
        Pop-Location
        $env:CARATCRAFT_UI_OUTPUT = $PreviousOutput
        $env:CARATCRAFT_UI_STATS = $PreviousStats
    }
}

# 计算调用者提供的流摘要, 流的关闭仍由打开它的函数负责.
function Get-CachedStreamHash {
    param([IO.Stream]$Stream)
    $Hasher = [Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($Hasher.ComputeHash($Stream)).Replace('-', '').ToLowerInvariant() }
    finally { $Hasher.Dispose() }
}

# 直接使用.NET计算文件校验值, 不依赖可选PowerShell模块的Get-FileHash命令.
function Get-CachedFileHash {
    param([string]$Path)
    $Stream = [IO.File]::OpenRead($Path)
    try { return Get-CachedStreamHash -Stream $Stream }
    finally { $Stream.Dispose() }
}

# 核对两个应用内的本项目模块, 仅更新构建副本的模块摘要, 保留第三方准入与源文件.
function Update-CachedModuleInventory {
    param([string]$Backend, [string]$Frontend)
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $Relative = 'legal/generated/runtime-components.json'
    $Inventory = Get-Content -LiteralPath (Join-Path $Backend $Relative) -Raw -Encoding UTF8 | ConvertFrom-Json
    foreach ($Module in @('platform-common', 'platform-biz')) {
        $Filename = "$Module-2.0.0.jar"
        $Records = @($Inventory.backend | Where-Object file -eq $Filename)
        if ($Records.Count -ne 1 -or $Records[0].coordinates -ne "com.platform:${Module}:2.0.0") {
            throw "项目模块索引缺失或坐标不一致: $Module"
        }
        $Expected = $null
        foreach ($Application in @('platform-admin', 'platform-api')) {
            $Archive = [IO.Compression.ZipFile]::OpenRead((Join-Path $Backend "$Application.jar"))
            try {
                $Entry = $Archive.GetEntry("BOOT-INF/lib/$Filename")
                if (-not $Entry) { throw "应用内缺少项目模块: $Application/$Filename" }
                $Stream = $Entry.Open()
                try { $Actual = Get-CachedStreamHash -Stream $Stream }
                finally { $Stream.Dispose() }
            } finally { $Archive.Dispose() }
            if ($Expected -and $Actual -ne $Expected) { throw "两个应用的项目模块字节不一致: $Filename" }
            $Expected = $Actual
        }
        $Records[0].sha256 = $Expected
    }
    $Json = ($Inventory | ConvertTo-Json -Depth 100) + "`n"
    foreach ($Context in @($Backend, $Frontend)) {
        [IO.File]::WriteAllText((Join-Path $Context $Relative), $Json, [Text.UTF8Encoding]::new($false))
    }
}

# 先记录构建证据再发布local标签, 写入失败时不报告整个构建成功.
function Write-CachedBuildRecord {
    param([string]$Destination)
    $Record = @{revision = $Revision; node = (& $NodePath --version); mavenCache = $MavenCache; output = $Destination; frontendLockSha256 = (Get-CachedFileHash -Path (Join-Path $Frontend 'pnpm-lock.yaml'))}
    $Record | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $Destination 'build.json') -Encoding UTF8
}

$NodePath = Find-CachedNode
if (-not $MavenCache) { $MavenCache = Join-Path $Root '.tools/m2/repository' }
Assert-CachedPrerequisites
if ($CheckOnly) { Write-Host '离线构建前置检查通过, 未编译或修改镜像。'; return }
$MavenCache = (Resolve-Path -LiteralPath $MavenCache).Path
$RunId = (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8)
$Output = Join-Path $Root ".tools/offline-build/$RunId"
$Source = Join-Path $Output 'source'
$BackendContext = Join-Path $Output 'backend'
$FrontendContext = Join-Path $Output 'frontend'
New-Item -ItemType Directory -Path $Source, $BackendContext, $FrontendContext -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $Project 'pom.xml') -Destination $Source
foreach ($Module in @('platform-common', 'platform-biz', 'platform-admin', 'platform-api')) {
    $ModuleOutput = Join-Path $Source $Module
    New-Item -ItemType Directory -Path $ModuleOutput -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $Project "$Module/pom.xml") -Destination $ModuleOutput
    Copy-CachedBuildTree -Source (Join-Path $Project "$Module/src") -Destination (Join-Path $ModuleOutput 'src')
}
Copy-Item -LiteralPath (Join-Path $Project 'deploy/maven-settings.xml') -Destination $Source
Copy-Item -LiteralPath (Join-Path $Project 'deploy/HealthCheck.java') -Destination $BackendContext
Write-Host "使用本机缓存编译当前源码, 持久构建记录: $Output"
# class和打包中间文件写入本次容器的独立文件系统, 只将最终JAR写回构建目录.
$Compile = 'cp -R /source/. /build/ && mvn -o -B -s maven-settings.xml -Pdev -pl platform-admin,platform-api -am package -DskipTests && cp platform-admin/target/platform-admin.jar /output/platform-admin.jar && cp platform-api/target/platform-api.jar /output/platform-api.jar'
Invoke-CachedDocker -Arguments @('run', '--rm', '--pull=never', '--network=none', '--mount', "type=bind,source=$Source,target=/source,readonly", '--mount', "type=bind,source=$BackendContext,target=/output", '--mount', "type=bind,source=$MavenCache,target=/root/.m2/repository,readonly", '--workdir', '/build', 'maven:3.9.11-eclipse-temurin-21', 'sh', '-c', $Compile)
Invoke-CachedDocker -Arguments @('run', '--rm', '--pull=never', '--network=none', '--mount', "type=bind,source=$BackendContext,target=/output", 'maven:3.9.11-eclipse-temurin-21', 'javac', '-d', '/output', '/output/HealthCheck.java')
Build-CachedFrontend -Destination (Join-Path $FrontendContext 'dist')
Copy-CachedBuildTree -Source (Join-Path $Project 'wx-mall/static/images') -Destination (Join-Path $FrontendContext 'images')
Copy-Item -LiteralPath (Join-Path $Project 'deploy/nginx/default.conf') -Destination $FrontendContext
Copy-Item -LiteralPath (Join-Path $Project 'Dockerfile.backend.cached') -Destination (Join-Path $BackendContext 'Dockerfile')
Copy-Item -LiteralPath (Join-Path $Project 'Dockerfile.admin-ui.cached') -Destination (Join-Path $FrontendContext 'Dockerfile')
foreach ($Context in @($BackendContext, $FrontendContext)) { Copy-CachedBuildTree -Source (Join-Path $Project 'legal') -Destination (Join-Path $Context 'legal') }
Update-CachedModuleInventory -Backend $BackendContext -Frontend $FrontendContext
$PreviousBuilder = $env:DOCKER_BUILDKIT
try {
    # 传统构建器直接使用已校验的本地基础镜像, 避免BuildKit元数据认证访问.
    $env:DOCKER_BUILDKIT = '0'
    foreach ($Kind in @('backend', 'admin-ui')) {
        $Context = if ($Kind -eq 'backend') { $BackendContext } else { $FrontendContext }
        Invoke-CachedDocker -Arguments @('build', '--pull=false', '--network=none', '--build-arg', "BUILD_REVISION=$Revision", '-t', "carat-craft-${Kind}:$RunId", $Context)
    }
} finally { $env:DOCKER_BUILDKIT = $PreviousBuilder }
# 两个镜像均构建成功后才更新local标签, 编译或封装失败时保留既有可用镜像.
Write-CachedBuildRecord -Destination $Output
foreach ($Kind in @('backend', 'admin-ui')) { Invoke-CachedDocker -Arguments @('image', 'tag', "carat-craft-${Kind}:$RunId", "carat-craft-${Kind}:local") }
Write-Host '离线编译与镜像封装完成, 未启动或重启业务容器。'
