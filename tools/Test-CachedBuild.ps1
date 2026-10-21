$ErrorActionPreference = 'Stop'
$Root = Split-Path $PSScriptRoot -Parent
$Tokens = $null
$Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $PSScriptRoot 'Build-CachedImages.ps1'), [ref]$Tokens, [ref]$Errors)
if ($Errors.Count) { throw ($Errors | Out-String) }
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
$script:Mode = 'ready'
$MavenCache = $Root
$NodePath = 'Get-CachedNodeStub'
$Frontend = 'unused-frontend'

# 模拟本地镜像清单, 不调用Docker或修改容器.
function docker {
    $global:LASTEXITCODE = 0
    if ($script:Mode -ne 'missing-image') { 'maven:3.9.11-eclipse-temurin-21' }
    'eclipse-temurin:21-jre', 'nginx:1.27-alpine'
}

# 用可控制版本与退出码的替身代替Node及依赖检查器.
function Get-CachedNodeStub {
    $global:LASTEXITCODE = 0
    if ($args[0] -eq '--version') {
        if ($script:Mode -eq 'old-node') { 'v20.20.0' } else { 'v24.19.0' }
    } elseif ($script:Mode -eq 'missing-frontend') { $global:LASTEXITCODE = 1 }
}

# 核验前置检查能准确阻止缺少缓存的构建, 保留原始提示.
function Assert-CacheRejected {
    param([string]$Expected)
    $Failure = $null
    try { Assert-CachedPrerequisites } catch { $Failure = $_.Exception.Message }
    if (-not $Failure -or $Failure -notmatch $Expected) { throw "预期拒绝$Expected, 实际: $Failure" }
}

Assert-CachedPrerequisites
# 覆盖版本上下界及异常输出, 无需执行真实Node或启动构建容器.
foreach ($Case in @(
    @{Version = 'v22.12.9'; Accepted = $false},
    @{Version = 'v22.13.0'; Accepted = $true},
    @{Version = 'v23.0.0'; Accepted = $true},
    @{Version = 'v24.19.0'; Accepted = $true},
    @{Version = 'v25.0.0'; Accepted = $false},
    @{Version = 'v24.invalid'; Accepted = $false},
    @{Version = ''; Accepted = $false}
)) {
    if ((Test-CachedNodeVersion -Version $Case.Version) -ne $Case.Accepted) {
        throw "Node版本边界判断错误: $($Case.Version)"
    }
}
$script:Mode = 'missing-image'
Assert-CacheRejected 'maven:3.9.11-eclipse-temurin-21'
$script:Mode = 'ready'
$MavenCache = Join-Path $Root ('missing-cache-' + [guid]::NewGuid().ToString('N'))
Assert-CacheRejected 'Maven'
$MavenCache = $Root
$script:Mode = 'old-node'
Assert-CacheRejected 'Node'
$script:Mode = 'missing-frontend'
Assert-CacheRejected '前端缓存'
$Frontend = $Root
$PreviousOutput = $env:CARATCRAFT_UI_OUTPUT
$PreviousStats = $env:CARATCRAFT_UI_STATS
try {
    $env:CARATCRAFT_UI_OUTPUT = 'original-output'
    $env:CARATCRAFT_UI_STATS = 'original-stats'
    $Failure = $null
    try { Build-CachedFrontend -Destination (Join-Path $Root 'unused-output') } catch { $Failure = $_.Exception.Message }
    if (-not $Failure -or $env:CARATCRAFT_UI_OUTPUT -ne 'original-output' -or $env:CARATCRAFT_UI_STATS -ne 'original-stats') { throw '前端编译失败未恢复输出环境变量' }
} finally {
    $env:CARATCRAFT_UI_OUTPUT = $PreviousOutput
    $env:CARATCRAFT_UI_STATS = $PreviousStats
}
$script:Mode = 'ready'
$Frontend = Join-Path $Root 'vendor/platform-wxshop/platform-admin-ui'
$Revision = 'record-test'
$script:RecordedBuild = $null

# ZIP条目与普通文件共用摘要实现, 校验标准样本且流的生命周期仍归调用者.
$HashStream = [IO.MemoryStream]::new([Text.Encoding]::UTF8.GetBytes('abc'))
try {
    if ((Get-CachedStreamHash -Stream $HashStream) -ne 'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad') {
        throw '构建模块SHA256计算错误'
    }
    $HashStream.Position = 0
    if ($HashStream.ReadByte() -ne 97) { throw '摘要函数关闭了调用者持有的流' }
} finally { $HashStream.Dispose() }

# 在内存接收记录, 验证.NET校验值和JSON写入链路, 不产生测试文件.
function Set-Content {
    param([string]$LiteralPath, [string]$Encoding)
    $script:RecordedBuild = ($input -join "`n") | ConvertFrom-Json
}

Write-CachedBuildRecord -Destination $Root
if ($script:RecordedBuild.revision -ne 'record-test' -or $script:RecordedBuild.frontendLockSha256 -notmatch '^[0-9a-f]{64}$') { throw '构建记录或.NET SHA256验证失败' }
Write-Host '离线构建前置回归通过: 缓存完整、缺少镜像、缺少Maven、Node版本边界与异常输出及前端缓存不完整。'
