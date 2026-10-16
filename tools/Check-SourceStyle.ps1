param([string]$Base = 'HEAD')

$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
$taskUi = Join-Path $taskRoot 'vendor/platform-wxshop/platform-admin-ui'
$taskFormatter = Join-Path $taskRoot '.tools/copyright/google-java-format.jar'
$taskJava = Join-Path $taskRoot '.tools/jdk21/jdk-21.0.8+9/bin/java.exe'
if (-not (Test-Path -LiteralPath $taskJava)) { $taskJava = (Get-Command java -ErrorAction Stop).Source }

# 检查增量Java文件及完整业务前端, 不格式化第三方源码或修改版权头.
Push-Location $taskRoot
try {
  $taskFiles = @(git -c core.quotepath=false diff --name-only --diff-filter=AM $Base -- '*.java') +
    @(git -c core.quotepath=false ls-files --others --exclude-standard -- '*.java')
  if ($LASTEXITCODE -ne 0) { throw '无法读取Git变更范围' }
  $taskFiles = @($taskFiles | Sort-Object -Unique)
  if ($taskFiles.Count -gt 0) {
    if (-not (Test-Path -LiteralPath $taskFormatter)) { throw '请按docs/source-style.md准备官方Java格式化工具' }
    & $taskJava -jar $taskFormatter --dry-run --set-exit-if-changed $taskFiles
    if ($LASTEXITCODE -ne 0) { throw 'Java格式检查失败' }
  }
  Write-Output ('Java格式检查通过: ' + $taskFiles.Count + '个增量文件')
  Push-Location $taskUi
  try {
    node node_modules/eslint/bin/eslint.js src --max-warnings 0
    if ($LASTEXITCODE -ne 0) { throw '业务前端规范检查失败' }
  } finally { Pop-Location }
  git diff --check
  if ($LASTEXITCODE -ne 0) { throw '变更空白符检查失败' }
  Write-Output '源代码规范检查通过'
} finally { Pop-Location }
