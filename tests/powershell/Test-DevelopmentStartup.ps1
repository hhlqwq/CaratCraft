$ErrorActionPreference = 'Stop'
$Root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$Tokens = $null; $Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $Root 'deploy/scripts/dev.ps1'), [ref]$Tokens, [ref]$Errors)
if ($Errors.Count) { throw '启动脚本语法错误' }
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
$EnvFile = 'fake.env'; $ComposeFile = 'fake.yml'
$ProjectRoot = $Root
$env:BUILD_REVISION = 'test'

# 所有Docker和数据操作仅记录调用, 不访问真实引擎或数据库.
function docker {
    $global:LASTEXITCODE = 0
    if ($args[0] -eq 'image') { return '[{"Config":{"Labels":{"org.opencontainers.image.revision":"test"}}}]' }
    $Sql = ($input -join "`n").Trim()
    $Application = ($args -join ' ') -match '\$MYSQL_PASSWORD;'
    if (($Script:Mode -eq 'root-auth' -and -not $Application) -or
        ($Script:Mode -eq 'app-auth' -and $Application)) {
        Write-Error 'ERROR 1045 (28000): Access denied'
        $global:LASTEXITCODE = 1
        return
    }
    if ($Script:Mode -eq 'sql-error') {
        Write-Error 'ERROR 1064: invalid SQL'
        $global:LASTEXITCODE = 1
        return
    }
    if ($Sql -match 'SELECT TABLE_NAME') {
        if ($Script:Mode -ne 'missing-table') { 'sys_region' }
        'mall_order', 'mall_user', 'mall_goods', 'CC_SCHEMA_VERSION'
    } elseif ($Sql -match 'SELECT version FROM CC_SCHEMA_VERSION') {
        if ($script:Mode -eq 'old-schema') { '0' }
        elseif ($script:Mode -eq 'unknown-version') { '2' }
        else { '1' }
    } elseif ($Sql -match 'SELECT CONCAT\(needed') {
        if ($script:Mode -eq 'missing-tier-columns') { 'sys_user.account_type' }
    } elseif ($Sql -match 'ACCOUNT_TYPE') {
        if ($script:Mode -eq 'unmapped-account') { '0' } else { '1' }
    } elseif ($Sql -eq 'SELECT 1;') { '1' }
}

# 只记录Compose动作, 应用启动失败不能被误判为需要清空开发库.
function Invoke-Compose {
    param([string[]]$Arguments)
    $Script:Actions += ($Arguments -join ' ')
    if ($Script:Mode -eq 'application-error' -and $Arguments[-1] -ne 'redis') { throw 'application-start-failure' }
}

# 记录重置调用, 用例无论何种故障都必须保持零次.
function Reset-TestDataVolumes { $Script:Resets++ }

# 隔离镜像和数据卷的 Docker 读取, 专注数据库失败场景.
function Assert-LocalImages { }
function Assert-DataVolumes { }

# 只标记完整启动成功, 不读取本机配置或真实状态.
function Show-AccessInfo { $Script:Ready = $true }

# 只记录诊断请求, 不调用真实Docker.
function Show-StartupDiagnostics { $Script:Diagnostics++ }

foreach ($Case in @('root-auth', 'app-auth', 'missing-table', 'old-schema', 'sql-error', 'application-error', 'unknown-version', 'missing-tier-columns', 'unmapped-account')) {
    $Script:Mode = $Case
    $Script:Actions = @(); $Script:Resets = 0; $Script:Ready = $false; $Script:Diagnostics = 0
    $Rejected = $false
    try { Start-LocalEnvironment } catch { $Rejected = $true }
    if (-not $Rejected -or $Script:Resets -ne 0 -or $Script:Ready -or $Script:Diagnostics -ne 1) {
        throw "失败边界错误: $Case"
    }
    if (@($Script:Actions | Where-Object { $_ -match 'mysql redis$' }).Count -ne 1 -or
        $Script:Actions -match '^(down|build|pull)|volume rm') {
        throw "启动故障包含重试或破坏性操作: $Case"
    }
}
Write-Host '开发启动回归通过: 九类失败立即停止, 数据库和 Redis 卷始终保留. Docker 调用均为替身.'
