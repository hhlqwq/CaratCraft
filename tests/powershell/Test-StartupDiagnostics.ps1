$ErrorActionPreference = 'Stop'
$Root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$Tokens = $null
$Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $Root 'deploy/scripts/dev.ps1'), [ref]$Tokens, [ref]$Errors)
if ($Errors.Count) { throw '启动脚本语法错误' }
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
$EnvFile = 'fake.env'
$ComposeFile = 'fake.yml'
$env:BUILD_REVISION = 'expected'
$script:Mode = 'missing-image'
$script:Calls = @()

# 真正启动cmd原生命令产生stderr和非零退出码, 不用纯PowerShell替身掩盖5.1的异常行为.
function docker {
    $script:Calls += ($args -join ' ')
    $global:LASTEXITCODE = 0
    if ($script:Mode -eq 'daemon-error' -or
        ($args[0] -eq 'image' -and $script:Mode -eq 'missing-image') -or
        ($args[0] -eq 'container' -and $script:Mode -eq 'missing-container')) {
        & $env:ComSpec /d /c 'echo simulated-object-missing 1>&2 & exit /b 1'
        $global:LASTEXITCODE = $LASTEXITCODE
        return
    }
    if ($args[0] -eq 'image') {
        return '[{"Config":{"Labels":{"org.opencontainers.image.revision":"expected"}}}]'
    }
    if ($args[0] -eq 'container') { return '[{"State":{"Status":"created"}}]' }
    return 'NAME STATUS'
}

# 检查最终异常保留业务失败原因, 不被诊断错误替换.
function Assert-Rejected {
    param([scriptblock]$Action, [string]$Expected)
    $Failure = $null
    try { & $Action } catch { $Failure = $_.Exception.Message }
    if (-not $Failure -or $Failure -notmatch $Expected) { throw "原始异常未保留: $Expected" }
}

Assert-Rejected { Assert-LocalImages } '缺少镜像'
$script:Mode = 'missing-container'
Show-StartupDiagnostics
if (-not ($script:Calls -match '^container inspect platform-admin$')) { throw '未用完整JSON读取容器状态' }
if ($script:Calls -match 'inspect --format') { throw '诊断不应依赖PowerShell5.1易损的Go模板引号' }
$script:Mode = 'daemon-error'
Show-StartupDiagnostics
$script:Mode = 'ready'
Show-StartupDiagnostics
Write-Host '诊断回归通过: 原生stderr缺镜像、未创建容器、引擎诊断失败及未配置健康检查.'

# 镜像预检失败时没有容器可诊断, 不应继续调用任何启动动作.
function Assert-LocalImages { throw 'primary-image-failure' }
function Show-StartupDiagnostics { throw 'secondary-diagnostic-failure' }
function Invoke-Compose { throw 'primary-compose-failure' }
Assert-Rejected { Start-LocalEnvironment } 'primary-image-failure'

# 即使诊断函数自身抛异常, 原始数据服务启动失败仍是最终返回原因.
function Assert-LocalImages { }
function Assert-DataVolumes { }
Assert-Rejected { Start-LocalEnvironment } 'primary-compose-failure'
Write-Host '诊断回归通过: 镜像预检只返回原错, 后续阶段的诊断异常不能覆盖启动原错; 未调用真实Docker.'
