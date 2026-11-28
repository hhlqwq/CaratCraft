$ErrorActionPreference = 'Stop'
$Root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$Tokens = $null
$Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $Root 'deploy/scripts/dev.ps1'), [ref]$Tokens, [ref]$Errors)
if ($Errors.Count) { throw '启动脚本存在语法错误' }
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
# 默认遵循临时目录约定, 沙箱验证可显式指定工作区内的测试目录.
$FixtureParent = if ($env:CARATCRAFT_TEST_TEMP) { $env:CARATCRAFT_TEST_TEMP } else { 'D:/tmp/hailongcodex' }
$FixtureParent = Join-Path $FixtureParent (Get-Date -Format 'yyyy-MM-dd')
$FixtureRoot = Join-Path $FixtureParent ('env-config-' + [Guid]::NewGuid().ToString('N'))
$Utf8 = [Text.UTF8Encoding]::new($false)
$Template = [IO.File]::ReadAllText((Join-Path $Root 'deploy/.env.example'), $Utf8)

# 断言配置操作仅按预期拒绝, 不输出密钥内容.
function Assert-ConfigurationRejected {
    param([scriptblock]$Action, [string]$Expected)
    $Failure = $null
    try { & $Action } catch { $Failure = $_.Exception.Message }
    if (-not $Failure -or $Failure -notmatch $Expected) { throw "配置未按预期拒绝: $Expected" }
}

# 阻断任何意外Docker调用, 此回归只操作本次生成的配置夹具.
function docker { throw '配置回归不得调用Docker或启动环境' }

[void][IO.Directory]::CreateDirectory($FixtureRoot)
try {
    # 随机配置生成已统一在容器内执行, 字节保留与排他创建由Node专项覆盖.
    $EnvExample = Join-Path $FixtureRoot 'template.env'
    $EnvFile = Join-Path $FixtureRoot 'generated.env'
    [IO.File]::WriteAllText($EnvExample, $Template, $Utf8)
    # 现有文件缺项与弱值必须分别报错, 不能被解释成数据库迁移失败.
    $Valid = "MYSQL_ROOT_PASSWORD=$('r' * 44)`nMYSQL_APP_PASSWORD=$('a' * 44)`nREDIS_PASSWORD=$('d' * 44)`nJWT_SECRET=$('j' * 44)`nMYSQL_APP_USER=caratcraft`n"
    [IO.File]::WriteAllText($EnvFile, ($Valid -replace '(?m)^JWT_SECRET=.*\n', ''), $Utf8)
    Assert-ConfigurationRejected { Assert-StartupConfiguration } 'JWT_SECRET 缺失'
    [IO.File]::WriteAllText($EnvFile, ($Valid -replace '(?m)^JWT_SECRET=.*', 'JWT_SECRET=short'), $Utf8)
    Assert-ConfigurationRejected { Assert-StartupConfiguration } 'JWT_SECRET.*不足24个字符'
    Remove-Item -LiteralPath $EnvFile

    # 端口读取也必须按UTF-8解释带中文的配置文件.
    [IO.File]::WriteAllText($EnvFile, ($Valid + "# 本机管理端口`nNGINX_PORT=9123`n"), $Utf8)
    $Info = Show-AccessInfo 6>&1 | Out-String
    if ($Info -notmatch 'localhost:9123') { throw '未正确读取中文注释后的管理端口' }
    Write-Host '配置回归通过: 缺项/弱值诊断及端口读取; 未连接Docker或数据库.'
} finally {
    # 只清理本次GUID目录, 不读取或改动真实deploy/.env.
    Get-ChildItem -LiteralPath $FixtureRoot -File | Remove-Item
    Remove-Item -LiteralPath $FixtureRoot
}
