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
    } elseif ($Sql -eq 'SELECT 1;') { '1' }
}

# 只记录Compose动作, 应用启动失败不能被误判为需要清空开发库.
function Invoke-Compose {
    param([string[]]$Arguments)
    $Script:Actions += ($Arguments -join ' ')
    if ($Script:Mode -eq 'application-error' -and $Arguments[-1] -ne 'redis') { throw 'application-start-failure' }
}

# 验证自动重建明确使用开发模式, 不反复要求人工确认或执行真实重建.
function Reset-TestDataVolumes {
    param([switch]$Development)
    if (-not $Development) { throw '开发恢复必须明确传入Development' }
    $Script:Resets++
    if ($Script:Recover) { $Script:Mode = 'ready' }
}

# 只标记完整启动成功, 不读取本机配置或真实状态.
function Show-AccessInfo { $Script:Ready = $true }

# 只记录诊断请求, 不调用真实Docker.
function Show-StartupDiagnostics { $Script:Diagnostics++ }

foreach ($Case in @('root-auth', 'app-auth', 'missing-table', 'old-schema')) {
    $Script:Mode = $Case; $Script:Recover = $true
    $Script:Actions = @(); $Script:Resets = 0; $Script:Ready = $false; $Script:Diagnostics = 0
    Start-LocalEnvironment
    if ($Script:Resets -ne 1 -or -not $Script:Ready) { throw "开发恢复未按预期完成: $Case" }
    if (@($Script:Actions | Where-Object { $_ -match 'mysql redis$' }).Count -ne 2) { throw '开发恢复必须重试一次数据服务' }
    if ($Script:Actions -match '^(build|pull)\b') { throw '开发恢复不得触发下载或构建' }
}
foreach ($Case in @('root-auth', 'sql-error', 'application-error', 'unknown-version')) {
    $Script:Mode = $Case; $Script:Recover = $false
    $Script:Actions = @(); $Script:Resets = 0; $Script:Ready = $false; $Script:Diagnostics = 0
    $Rejected = $false
    try { Start-LocalEnvironment } catch { $Rejected = $true }
    $Expected = if ($Case -eq 'root-auth') { 1 } else { 0 }
    if (-not $Rejected -or $Script:Resets -ne $Expected -or $Script:Ready) { throw "失败边界错误: $Case" }
}
Write-Host '开发启动回归通过: root/应用账号失配、缺表和旧结构自动重建一次; 持续失败、SQL错误及应用故障不循环清库. 所有Docker调用为替身.'
