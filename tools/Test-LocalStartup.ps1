$ErrorActionPreference = 'Stop'
$Root = Split-Path $PSScriptRoot -Parent
$Tokens = $null
$Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $Root 'dev.ps1'), [ref]$Tokens, [ref]$Errors)
if ($Errors.Count) { throw ($Errors | Out-String) }
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
$script:Mode = 'ready'
$script:Calls = @()
$global:LocalStartupDockerCalls = New-Object 'System.Collections.Generic.List[string]'
$EnvFile = 'unused.env'
$ComposeFile = 'unused.yml'
$env:BUILD_REVISION = 'startup-test'
$OnlineBuild = $true

# 用隔离替身提供Docker响应, 不连接业务容器或修改数据库.
function docker {
    $global:LASTEXITCODE = 0
    $Sql = $input -join "`n"
    $script:Calls += ,(($args -join ' ') + ' ' + $Sql)
    $global:LocalStartupDockerCalls.Add((($args -join ' ') + ' ' + $Sql))
    if ($args[0] -eq 'image') {
        if ($script:Mode -eq 'missing-image') { $global:LASTEXITCODE = 1; return }
        $Revision = if ($script:Mode -eq 'old-image') { 'old' } else { 'startup-test' }
        return ('[{"Config":{"Labels":{"org.opencontainers.image.revision":"' + $Revision + '"}}}]')
    }
    if ($Sql -match 'SELECT TABLE_NAME') {
        if ($script:Mode -eq 'query-error') { $global:LASTEXITCODE = 1; return }
        if ($script:Mode -ne 'partial-database') { 'sys_region' }
        'mall_order', 'mall_user', 'mall_goods', 'CC_SCHEMA_MIGRATION'
    } elseif ($Sql -match 'SELECT COUNT') {
        if ($script:Mode -eq 'old-schema') { '0' } else { '1' }
    }
}

# 验证失败条件确实中止, 不把预期异常当作用例自身失败.
function Assert-Rejected {
    param([scriptblock]$Action, [string]$Expected)
    $Failure = $null
    try { & $Action } catch { $Failure = $_.Exception.Message }
    if (-not $Failure -or $Failure -notmatch $Expected) { throw "未按预期拒绝: $Expected, 实际: $Failure" }
}

Assert-LocalImages
Assert-DatabaseReady
$script:Mode = 'old-image'
Assert-Rejected { Assert-LocalImages } 'NoBuild'
$script:Mode = 'missing-image'
Assert-Rejected { Assert-LocalImages } 'NoBuild'
$script:Mode = 'partial-database'
Assert-Rejected { Assert-DatabaseReady } 'sys_region'
if ($script:Calls[-1] -notmatch 'SELECT TABLE_NAME') { throw '缺基础表后仍查询迁移台账' }
$script:Mode = 'query-error'
Assert-Rejected { Assert-DatabaseReady } 'MySQL'
$script:Mode = 'old-schema'
Assert-Rejected { Assert-DatabaseReady } 'V20261022_01'

# 替换外部副作用, 检查完整启动阶段的先后顺序和失败退出.
function Invoke-Compose {
    param([string[]]$Arguments)
    $script:Calls += ,($Arguments -join ' ')
    if ($script:Mode -eq 'build-error' -and $Arguments[0] -eq 'build') {
        throw 'simulated-build-error'
    }
}

# 启动成功只记录展示动作, 不读取本机私有配置.
function Show-AccessInfo { $script:Calls += 'access-ready' }

# 启动失败只记录诊断动作, 不读取真实容器日志.
function Show-StartupDiagnostics { $script:Calls += 'diagnostics' }

$script:Mode = 'ready'
$script:Calls = @()
$NoBuild = $false
Start-LocalEnvironment
if ($script:Calls -contains 'build' -or -not ($script:Calls -match '300 mysql redis') -or
    $script:Calls[-2] -ne 'up --detach --no-build --wait --wait-timeout 300' -or
    $script:Calls[-1] -ne 'access-ready') { throw '启动顺序错误' }
$script:Mode = 'partial-database'
$script:Calls = @()
Assert-Rejected { Start-LocalEnvironment } 'sys_region'
if ($script:Calls[-1] -ne 'diagnostics' -or $script:Calls -contains 'access-ready') { throw '失败诊断或退出错误' }
$script:Mode = 'build-error'
$script:Calls = @()
Assert-Rejected { Build-LocalEnvironment } 'simulated-build-error'
if ($script:Calls.Count -ne 1 -or $script:Calls[0] -ne 'build') { throw '构建失败后仍继续启动或输出后端诊断' }
$script:Mode = 'ready'
$script:Calls = @()
Build-LocalEnvironment
if ($script:Calls.Count -ne 1 -or $script:Calls[0] -ne 'build') { throw '构建操作启动了容器或访问了数据库' }
$Wrapper = Get-Content (Join-Path $Root 'start-local-test.cmd') -Raw
if ($Wrapper -notmatch 'dev\.ps1" -Action start %\*' -or $Wrapper -match 'docker compose|Action build') { throw '启动入口未固定为仅启动操作' }
$BuildWrapper = Get-Content (Join-Path $Root 'build-local-test.cmd') -Raw
if ($BuildWrapper -notmatch 'dev\.ps1" -Action build %\*' -or $BuildWrapper -match 'Action start') { throw '构建入口未固定为仅构建操作' }
$script:Calls = @()
$global:LocalStartupDockerCalls.Clear()
& (Join-Path $Root 'dev.ps1') -Action build -OnlineBuild
$DispatchCalls = $global:LocalStartupDockerCalls.ToArray()
if ($DispatchCalls.Count -ne 3 -or $DispatchCalls[0] -notmatch 'compose version' -or
    $DispatchCalls[1].Trim() -ne 'info' -or $DispatchCalls[2] -notmatch ' build\s*$') {
    throw "构建Action调度出现启动、数据库或其他非构建操作: $($DispatchCalls -join ' | ')"
}
Assert-Rejected { & (Join-Path $Root 'dev.ps1') -Action build -NoBuild } 'NoBuild'

# 模拟不存在或不可读取的私有配置, 构建均不得生成、读取或校验运行密钥.
function Test-Path {
    param([string]$LiteralPath)
    if ($LiteralPath -eq (Join-Path $Root 'vendor/platform-wxshop/deploy/.env')) {
        return $global:LocalStartupBuildEnvironmentExists
    }
    Microsoft.PowerShell.Management\Test-Path -LiteralPath $LiteralPath
}

# 构建调度不应读取私有配置或模板内容, 旧流程会在写文件前被替身中止.
function Get-Content {
    param([string]$LiteralPath)
    throw '构建不应读取运行密钥或生成私有配置'
}

try {
    foreach ($Exists in @($false, $true)) {
        $global:LocalStartupBuildEnvironmentExists = $Exists
        $global:LocalStartupDockerCalls.Clear()
        & (Join-Path $Root 'dev.ps1') -Action build -OnlineBuild
        $BuildCall = $global:LocalStartupDockerCalls.ToArray()[-1]
        $ExpectedEnvironment = if ($Exists) { '.env' } else { '.env.example' }
        if ($BuildCall -notmatch ([regex]::Escape($ExpectedEnvironment) + '"?\s+-f\s+') -or $BuildCall -notmatch ' build\s*$') {
            throw "构建未使用预期配置来源: $ExpectedEnvironment"
        }
    }
} finally {
    Remove-Item Function:\Test-Path, Function:\Get-Content
    Remove-Variable LocalStartupBuildEnvironmentExists -Scope Global
}
Remove-Variable LocalStartupDockerCalls -Scope Global
Write-Host '本地启动回归通过: 就绪结构、缺表、旧版本、认证失败、旧镜像、缺镜像、阶段顺序、构建失败诊断、构建启动分离、构建不读取运行密钥及两个脚本入口。'
