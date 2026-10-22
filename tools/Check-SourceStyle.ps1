param([string]$Base = 'HEAD', [switch]$Incremental, [switch]$Format, [switch]$Docker)

$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
$taskUi = Join-Path $taskRoot 'vendor/platform-wxshop/platform-admin-ui'
$taskFormatter = Join-Path $taskRoot '.tools/copyright/google-java-format.jar'
$taskJava = Join-Path $taskRoot '.tools/jdk21/jdk-21.0.8+9/bin/java.exe'
if (-not $Docker -and -not (Test-Path -LiteralPath $taskJava)) { $taskJava = (Get-Command java -ErrorAction Stop).Source }

# 默认检查全量Java及两端前端, 增量模式仅供开发时快速检查.
Push-Location $taskRoot
try {
    node tools/check-project-conventions.cjs
    if ($LASTEXITCODE -ne 0) { throw '项目统一约定检查失败' }
    $taskHasGit = Test-Path -LiteralPath (Join-Path $taskRoot '.git')
    if (-not $taskHasGit) {
        if ($Incremental) { throw '无Git源码包只支持全量检查' }
        $taskFiles = @(Get-Content -LiteralPath (Join-Path $taskRoot 'source-files.json') -Raw | ConvertFrom-Json |
            Where-Object { $_ -match '^vendor/platform-wxshop/[^/]+/src/.+\.java$' -and $_ -notmatch '(^|[\\/])\.\.([\\/]|$)' })
    } elseif ($Incremental) {
        $taskFiles = @(git -c core.quotepath=false diff --name-only --diff-filter=AM $Base -- '*.java') +
            @(git -c core.quotepath=false ls-files --others --exclude-standard -- '*.java')
    } else {
        $taskFiles = @(git -c core.quotepath=false ls-files --cached --others --exclude-standard -- 'vendor/platform-wxshop/*/src/**/*.java')
    }
    if ($taskHasGit -and $LASTEXITCODE -ne 0) { throw '无法读取Git文件范围' }
    $taskFiles = @($taskFiles | Sort-Object -Unique | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf })
    if ($taskFiles.Count -gt 0) {
        if (-not (Test-Path -LiteralPath $taskFormatter)) { throw '请按docs/项目规范文档.md准备官方Java格式化工具' }
        # 分批传参避免Windows命令行长度限制, 每批报告关键节点.
        for ($taskOffset = 0; $taskOffset -lt $taskFiles.Count; $taskOffset += 40) {
            $taskEnd = [Math]::Min($taskOffset + 39, $taskFiles.Count - 1)
            $taskBatch = @($taskFiles[$taskOffset..$taskEnd])
            $taskArguments = @('--aosp') + @(if ($Format) { '--replace' } else { '--dry-run'; '--set-exit-if-changed' })
            if ($Docker) {
                & docker run --rm --pull=never --mount "type=bind,source=$taskRoot,target=/workspace" --workdir /workspace maven:3.9.11-eclipse-temurin-21 java -jar /workspace/.tools/copyright/google-java-format.jar @taskArguments @taskBatch
            } else {
                & $taskJava -jar $taskFormatter @taskArguments @taskBatch
            }
            if ($LASTEXITCODE -ne 0) { throw 'Java格式检查失败' }
            Write-Output ('Java格式进度: ' + ($taskEnd + 1) + '/' + $taskFiles.Count)
        }
    }
    Write-Output ('Java格式检查通过: ' + $taskFiles.Count + '个文件, 增量模式=' + $Incremental)
    $taskFrontendArguments = @(if ($Format) { '--write' })
    node tools/format-business-source.cjs @taskFrontendArguments
    if ($LASTEXITCODE -ne 0) { throw '前端格式检查失败' }
    node vendor/platform-wxshop/platform-admin-ui/node_modules/eslint/bin/eslint.js tools --config tools/eslint-tools.config.mjs --max-warnings 0
    if ($LASTEXITCODE -ne 0) { throw '工具脚本规范检查失败' }
    node vendor/platform-wxshop/platform-admin-ui/node_modules/eslint/bin/eslint.js vendor/platform-wxshop/wx-mall --config tools/eslint-miniprogram.config.mjs --max-warnings 0
    if ($LASTEXITCODE -ne 0) { throw '小程序规范检查失败' }
    Push-Location $taskUi
    try {
        node node_modules/eslint/bin/eslint.js src --max-warnings 0
        if ($LASTEXITCODE -ne 0) { throw '业务前端规范检查失败' }
    } finally { Pop-Location }
    if ($taskHasGit) {
        git -c core.safecrlf=false diff --check
        if ($LASTEXITCODE -ne 0) { throw '变更空白符检查失败' }
    } else {
        Write-Output '无Git交付目录: 已按源码清单检查全量格式, 不执行Git差异检查'
    }
    Write-Output '源代码规范检查通过'
} finally { Pop-Location }
