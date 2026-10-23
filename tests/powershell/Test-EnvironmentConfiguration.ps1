$ErrorActionPreference = 'Stop'
$Root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$Tokens = $null
$Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $Root 'deploy/scripts/dev.ps1'), [ref]$Tokens, [ref]$Errors)
if ($Errors.Count) { throw '启动脚本存在语法错误' }
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
$FixtureRoot = Join-Path 'D:/tmp/hailongcodex/2026-10-23' ('env-config-' + [Guid]::NewGuid().ToString('N'))
$Utf8 = [Text.UTF8Encoding]::new($false)
$Template = [IO.File]::ReadAllText((Join-Path $Root 'deploy/.env.example'), $Utf8)
$Keys = @('MYSQL_ROOT_PASSWORD', 'MYSQL_APP_PASSWORD', 'REDIS_PASSWORD', 'JWT_SECRET')

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
    foreach ($Newline in @("`n", "`r`n")) {
        $EnvExample = Join-Path $FixtureRoot 'template.env'
        $EnvFile = Join-Path $FixtureRoot 'generated.env'
        $Source = $Template.Replace("`r`n", "`n").Replace("`n", $Newline)
        [IO.File]::WriteAllText($EnvExample, $Source, $Utf8)
        Initialize-Environment
        $Generated = [IO.File]::ReadAllText($EnvFile, $Utf8)
        $Expected = $Source
        foreach ($Key in $Keys) {
            $Match = [regex]::Matches($Generated, "(?m)^$Key=([^\r\n]*)")
            if ($Match.Count -ne 1 -or $Match[0].Groups[1].Value.Length -ne 44) { throw "生成密钥格式异常: $Key" }
            $Expected = [regex]::Replace($Expected, "(?m)^$Key=[^\r\n]*", "$Key=" + $Match[0].Groups[1].Value)
        }
        if ($Generated -cne $Expected) { throw '配置生成改写了中文、换行或非密钥内容' }
        Assert-StartupConfiguration
        $Before = [Convert]::ToBase64String([IO.File]::ReadAllBytes($EnvFile))
        Initialize-Environment
        if ($Before -cne [Convert]::ToBase64String([IO.File]::ReadAllBytes($EnvFile))) { throw '已存在的配置被覆盖' }
        Remove-Item -LiteralPath $EnvFile
    }
    Write-Host '配置回归通过: LF/CRLF、中文无BOM UTF-8模板、随机密钥和旧配置不覆盖.'

    # 现有文件缺项与弱值必须分别报错, 不能被解释成数据库迁移失败.
    $Valid = "MYSQL_ROOT_PASSWORD=$('r' * 44)`nMYSQL_APP_PASSWORD=$('a' * 44)`nREDIS_PASSWORD=$('d' * 44)`nJWT_SECRET=$('j' * 44)`nMYSQL_APP_USER=caratcraft`n"
    [IO.File]::WriteAllText($EnvFile, ($Valid -replace '(?m)^JWT_SECRET=.*\n', ''), $Utf8)
    Assert-ConfigurationRejected { Assert-StartupConfiguration } '缺少配置项 JWT_SECRET'
    [IO.File]::WriteAllText($EnvFile, ($Valid -replace '(?m)^JWT_SECRET=.*', 'JWT_SECRET=short'), $Utf8)
    Assert-ConfigurationRejected { Assert-StartupConfiguration } 'JWT_SECRET 至少需要24个字符'
    Remove-Item -LiteralPath $EnvFile

    # 模板本身缺项或重复时在创建本机配置前中止, 不生成半份配置.
    [IO.File]::WriteAllText($EnvExample, ($Template -replace '(?m)^JWT_SECRET=.*\n?', ''), $Utf8)
    Assert-ConfigurationRejected { Initialize-Environment } '模板.*JWT_SECRET'
    if (Test-Path -LiteralPath $EnvFile) { throw '缺项模板仍创建了本机配置' }
    [IO.File]::WriteAllText($EnvExample, ($Template + "`nJWT_SECRET=`n"), $Utf8)
    Assert-ConfigurationRejected { Initialize-Environment } '模板.*JWT_SECRET'
    if (Test-Path -LiteralPath $EnvFile) { throw '重复项模板仍创建了本机配置' }

    # 端口读取也必须按UTF-8解释带中文的配置文件.
    [IO.File]::WriteAllText($EnvFile, ($Valid + "# 本机管理端口`nNGINX_PORT=9123`n"), $Utf8)
    $Info = Show-AccessInfo 6>&1 | Out-String
    if ($Info -notmatch 'localhost:9123') { throw '未正确读取中文注释后的管理端口' }
    Write-Host '配置回归通过: 缺项/弱值诊断、模板缺项/重复拒绝及端口读取; 未连接Docker或数据库.'
} finally {
    # 只清理本次GUID目录, 不读取或改动真实deploy/.env.
    Get-ChildItem -LiteralPath $FixtureRoot -File | Remove-Item
    Remove-Item -LiteralPath $FixtureRoot
}
