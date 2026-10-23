$ErrorActionPreference = 'Stop'
$Script:Invocations = [Collections.Generic.List[string]]::new()
$Script:Owner = 'caratcraft-test'
$Script:Confirmation = 'RESET_DATA'
$Script:InspectFailure = $false
$EnvFile = 'FAKE-ENV'
$ComposeFile = 'FAKE-COMPOSE'

# 所有Docker调用都在当前测试进程内替换, 不访问真实Docker引擎.
function docker {
    $global:LASTEXITCODE = 0
    $Arguments = @($args)
    $Script:Invocations.Add(($Arguments -join ' '))
    if (($Arguments -join ' ') -match 'config --format json') {
        return '{"name":"caratcraft-test","volumes":{"mysql-data":{"name":"test_mysql"},"redis-data":{"name":"test_redis"},"upload-data":{"name":"test_upload"}}}'
    }
    if (($Arguments -join ' ') -match '^volume inspect ') {
        if ($Script:InspectFailure) { $global:LASTEXITCODE = 1; return }
        return ('[{"Labels":{"com.docker.compose.project":"' + $Script:Owner + '"}}]')
    }
}

# 只记录停机意图, 不执行任何程序控制动作.
function Invoke-Compose {
    param([string[]]$Arguments)
    $Script:Invocations.Add('COMPOSE ' + ($Arguments -join ' '))
}

# 模拟显式确认或取消, 不阻塞测试等待输入.
function Read-Host {
    param([string]$Prompt)
    return $Script:Confirmation
}

# 从工作区脚本只加载指定函数定义, 不运行脚本顶层的启动代码.
function Import-ResetFunction {
    $Tokens = $null
    $Errors = $null
    $Source = Join-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) 'deploy/scripts/dev.ps1'
    $Ast = [Management.Automation.Language.Parser]::ParseFile($Source, [ref]$Tokens, [ref]$Errors)
    if ($Errors.Count) { throw '启动脚本语法检查失败。' }
    $Definition = $Ast.Find({ param($Node)
        $Node -is [Management.Automation.Language.FunctionDefinitionAst] -and $Node.Name -eq 'Reset-TestDataVolumes'
    }, $true)
    if (-not $Definition) { throw '找不到待测试函数。' }
    . ([scriptblock]::Create($Definition.Extent.Text.Replace('function Reset-TestDataVolumes', 'function script:Reset-TestDataVolumes')))
}

# 断言失败立即结束, 不把失败结果当作检查通过.
function Assert-True {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}

Assert-True ((Get-Command docker).CommandType -eq 'Function') 'Docker未被测试替身隔离。'
Import-ResetFunction
Reset-TestDataVolumes
$Deletes = @($Script:Invocations | Where-Object { $_ -like 'volume rm *' })
Assert-True ($Deletes.Count -eq 2) '重置必须只删除两个数据卷。'
Assert-True ($Deletes -contains 'volume rm test_mysql') '缺少MySQL卷。'
Assert-True ($Deletes -contains 'volume rm test_redis') '缺少Redis卷。'
Assert-True (-not ($Deletes -match 'upload')) '不得删除上传卷。'

# 无法读取卷归属时必须在任何停机或删除之前失败, 不当作无主数据卷处理.
$Script:Invocations.Clear()
$Script:InspectFailure = $true
$Rejected = $false
try { Reset-TestDataVolumes } catch { $Rejected = $true }
Assert-True $Rejected '无法读取卷归属时必须拒绝重置。'
Assert-True (-not ($Script:Invocations -match 'COMPOSE down|volume rm')) '卷元数据读取失败后不得停机或删卷。'
$Script:InspectFailure = $false

$Script:Invocations.Clear()
$Script:Owner = 'another-project'
$Rejected = $false
try { Reset-TestDataVolumes } catch { $Rejected = $true }
Assert-True $Rejected '必须拒绝其他项目的数据卷。'
Assert-True (-not ($Script:Invocations -match 'COMPOSE down|volume rm')) '归属检查失败前不得停机或删卷。'

$Script:Invocations.Clear()
$Script:Owner = 'caratcraft-test'
$Script:Confirmation = 'NO'
$Rejected = $false
try { Reset-TestDataVolumes } catch { $Rejected = $true }
Assert-True $Rejected '取消确认后必须拒绝重置。'
Assert-True (-not ($Script:Invocations -match 'COMPOSE down|volume rm')) '确认前不得停机或删卷。'
Write-Host '数据重置安全回归通过: 上传卷保留、跨项目拒绝、取消确认无副作用. Docker调用全部使用替身.'

# 用户已确认开发数据可丢弃, 自动恢复不再提示确认, 但仍只操作归属正确的两个卷.
$Script:Invocations.Clear()
Reset-TestDataVolumes -Development
$Deletes = @($Script:Invocations | Where-Object { $_ -like 'volume rm *' })
Assert-True ($Deletes.Count -eq 2 -and -not ($Deletes -match 'upload')) '开发自动恢复必须只重建本项目MySQL和Redis卷。'
$Script:Invocations.Clear()
$Script:Owner = 'another-project'
$Rejected = $false
try { Reset-TestDataVolumes -Development } catch { $Rejected = $true }
Assert-True $Rejected '开发自动恢复也必须拒绝其他项目卷。'
Assert-True (-not ($Script:Invocations -match 'COMPOSE down|volume rm')) '开发自动恢复不得绕过卷归属检查。'
Write-Host '开发自动重建回归通过: 无需重复确认, 不触及上传卷或其他项目. 全部为替身.'
