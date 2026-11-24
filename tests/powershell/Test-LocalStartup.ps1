$ErrorActionPreference = 'Stop'
$Root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$Tokens = $null
$Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $Root 'deploy/scripts/dev.ps1'), [ref]$Tokens, [ref]$Errors)
if ($Errors.Count) { throw ($Errors | Out-String) }
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
$script:Mode = 'ready'
$script:Calls = @()
$global:LocalStartupDockerCalls = New-Object 'System.Collections.Generic.List[string]'
$EnvFile = 'unused.env'
$ComposeFile = 'unused.yml'
$ProjectRoot = $Root
$env:BACKEND_BUILD_REVISION = 'backend-input'
$env:ADMIN_UI_BUILD_REVISION = 'admin-ui-input'
$env:BUILD_GIT_SHA = 'test-head'

# 用隔离替身提供Docker响应, 不连接业务容器或修改数据库.
function docker {
    $global:LASTEXITCODE = 0
    $Sql = $input -join "`n"
    $script:Calls += ,(($args -join ' ') + ' ' + $Sql)
    $global:LocalStartupDockerCalls.Add((($args -join ' ') + ' ' + $Sql))
    if ($args[0] -eq 'volume') { return '[{"Labels":{"com.docker.compose.project":"platform-wxshop"}}]' }
    if ($args[0] -eq 'container') {
        $Revision = if ($args[-1] -eq 'platform-nginx') { $env:ADMIN_UI_BUILD_REVISION } else { $env:BACKEND_BUILD_REVISION }
        return ('[{"Config":{"Labels":{"org.opencontainers.image.revision":"older-build","io.caratcraft.source-digest":"' + $Revision + '"}}}]')
    }
    if ($args[0] -eq 'image') {
        if ($script:Mode -eq 'missing-image' -or ($script:Mode -eq 'pull-error' -and $args[-1] -like '*/mysql:*')) {
            $global:LASTEXITCODE = 1
            return
        }
        $Revision = if ($args[-1] -eq 'carat-craft-admin-ui:local') { $env:ADMIN_UI_BUILD_REVISION } else { $env:BACKEND_BUILD_REVISION }
        if ((($script:Mode -in @('old-image', 'build-error', 'java-updated') -or $global:LocalStartupExpectJavaUpdate) -and $args[-1] -eq 'carat-craft-backend:local') -or
            ($script:Mode -eq 'ui-updated' -and $args[-1] -eq 'carat-craft-admin-ui:local')) { $Revision = 'old' }
        return ('[{"Config":{"Labels":{"org.opencontainers.image.revision":"older-build","io.caratcraft.source-digest":"' + $Revision + '"}}}]')
    }
    if ($Sql -match 'SELECT TABLE_NAME') {
        if ($script:Mode -eq 'query-error') { $global:LASTEXITCODE = 1; return }
        if ($script:Mode -ne 'partial-database') { 'sys_region' }
        'mall_order', 'mall_user', 'mall_goods', 'CC_SCHEMA_VERSION'
    } elseif ($Sql.Trim() -eq 'SELECT 1;') {
        '1'
    } elseif ($Sql -match 'SELECT version FROM CC_SCHEMA_VERSION') {
        if ($script:Mode -eq 'old-schema') { '0' }
        elseif ($script:Mode -eq 'unknown-version') { '2' }
        else { '1' }
    } elseif ($Sql -match 'SELECT CONCAT\(needed') {
        if ($script:Mode -eq 'missing-tier-columns') { 'sys_user.account_type', 'sys_role.direct_user_id' }
    } elseif ($Sql -match 'ACCOUNT_TYPE') {
        if ($script:Mode -eq 'unmapped-account') { '0' } else { '1' }
    }
}

# 验证失败条件确实中止, 不把预期异常当作用例自身失败.
function Assert-Rejected {
    param([scriptblock]$Action, [string]$Expected)
    $Failure = $null
    try { & $Action } catch { $Failure = $_.Exception.Message }
    if (-not $Failure -or $Failure -notmatch $Expected) { throw "未按预期拒绝: $Expected, 实际: $Failure" }
}

# 从真实Compose配置提取镜像名称, 防止国内来源变更后启动校验仍寻找旧名称.
$PublicCompose = [IO.File]::ReadAllText((Join-Path $Root 'deploy/docker-compose.yml'))
$ExpectedImages = @([regex]::Matches($PublicCompose, '(?m)^    image: ([^\r\n]+)') |
    ForEach-Object { $_.Groups[1].Value.Trim() } | Sort-Object -Unique)
$script:Calls = @()
Assert-LocalImages
foreach ($ExpectedImage in $ExpectedImages) {
    if (-not ($script:Calls | Where-Object { $_.Trim() -eq "image inspect $ExpectedImage" })) {
        throw "启动镜像校验与Compose不一致: $ExpectedImage"
    }
}
if ($script:Calls.Count -ne $ExpectedImages.Count) { throw '启动镜像校验包含旧来源或遗漏镜像' }
Assert-DatabaseReady
$script:Mode = 'old-image'
Assert-Rejected { Assert-LocalImages } 'build-local-test.cmd'
$script:Mode = 'missing-image'
Assert-Rejected { Assert-LocalImages } 'build-local-test.cmd'
$script:Mode = 'partial-database'
Assert-Rejected { Assert-DatabaseReady } 'sys_region'
if ($script:Calls[-1] -notmatch 'SELECT TABLE_NAME') { throw '缺基础表后仍查询迁移台账' }
$script:Mode = 'query-error'
Assert-Rejected { Assert-DatabaseReady } 'MySQL'
$script:Mode = 'old-schema'
Assert-Rejected { Assert-DatabaseReady } '完整初始化'
$script:Mode = 'unknown-version'
Assert-Rejected { Assert-DatabaseReady } '未知'
$script:Mode = 'missing-tier-columns'
Assert-Rejected { Assert-DatabaseReady } 'sys_user.account_type'
$script:Mode = 'unmapped-account'
Assert-Rejected { Assert-DatabaseReady } '账号层级'

# 替换外部副作用, 检查完整启动阶段的先后顺序和失败退出.
function Invoke-Compose {
    param([string[]]$Arguments)
    $script:Calls += ,($Arguments -join ' ')
    if ($script:Mode -eq 'pull-error' -and $Arguments[0] -eq 'pull') {
        throw 'simulated-pull-error'
    }
    if ($script:Mode -eq 'build-error' -and $Arguments[0] -eq 'build') {
        throw 'simulated-build-error'
    }
}

# 任意标准启动路径调用重置即标记测试失败.
function Reset-TestDataVolumes { $script:Calls += 'unexpected-reset' }

# 启动成功只记录展示动作, 不读取本机私有配置.
function Show-AccessInfo { $script:Calls += 'access-ready' }

# 启动失败只记录诊断动作, 不读取真实容器日志.
function Show-StartupDiagnostics { $script:Calls += 'diagnostics' }

$script:Mode = 'ready'
$script:Calls = @()
$NoBuild = $false
Start-LocalEnvironment
if ($script:Calls -contains 'build' -or -not ($script:Calls -match '300 mysql redis') -or
    -not ($script:Calls -match 'up --detach --no-deps --no-build --pull never --wait --wait-timeout 300 platform-admin platform-api nginx') -or
    $script:Calls[-1] -ne 'access-ready') { throw '启动顺序错误' }
$script:Mode = 'partial-database'
$script:Calls = @()
Assert-Rejected { Start-LocalEnvironment } 'sys_region'
if ($script:Calls -match 'unexpected-reset|volume rm|^down') { throw '数据库检查失败后不得清理数据' }
if ($script:Calls[-1] -ne 'diagnostics' -or $script:Calls -contains 'access-ready') { throw '失败诊断或退出错误' }
$script:Mode = 'build-error'
$script:Calls = @()
Assert-Rejected { Build-LocalEnvironment } 'simulated-build-error'
if (@($script:Calls | Where-Object { $_ -match '^(build|pull|up) ' }) -join '|' -cne 'build platform-admin') { throw 'Build failure did not stop' }
$script:Mode = 'pull-error'
$script:Calls = @()
Assert-Rejected { Build-LocalEnvironment } 'simulated-pull-error'
if (@($script:Calls | Where-Object { $_ -match '^(build|pull|up) ' }) -join '|' -cne 'pull mysql') { throw 'Pull failure did not stop' }
foreach ($Case in @(@('ready', ''), @('java-updated', 'build platform-admin'), @('ui-updated', 'build nginx'))) {
    $script:Mode = $Case[0]
    $script:Calls = @()
    Build-LocalEnvironment
    if (@($script:Calls | Where-Object { $_ -match '^(build|pull|up) ' }) -join '|' -cne $Case[1]) { throw "Wrong build target: $($Case[0])" }
}
$script:Mode = 'ready'
$Wrapper = Get-Content (Join-Path $Root 'deploy/scripts/start-local-test.cmd') -Raw
if ($Wrapper -notmatch 'dev\.ps1" -Action start %\*' -or $Wrapper -match 'docker compose|Action build') { throw '启动入口未固定为仅启动操作' }
$BuildWrapper = Get-Content (Join-Path $Root 'deploy/scripts/build-local-test.cmd') -Raw
if ($BuildWrapper -notmatch 'dev\.ps1" -Action build %\*' -or $BuildWrapper -match 'Action start') { throw '构建入口未固定为仅构建操作' }
$script:Calls = @()
$global:LocalStartupDockerCalls.Clear()
& (Join-Path $Root 'deploy/scripts/dev.ps1') -Action build
$DispatchCalls = $global:LocalStartupDockerCalls.ToArray()
if ($DispatchCalls.Count -ne 6 -or $DispatchCalls[0] -notmatch 'compose version' -or
    $DispatchCalls[1].Trim() -ne 'info' -or $DispatchCalls[2..5] -match '^(?!image inspect)') {
    throw "Unexpected build dispatch: $($DispatchCalls -join ' | ')"
}
Assert-Rejected { & (Join-Path $Root 'deploy/scripts/dev.ps1') -Action build -NoBuild } 'NoBuild'

# 模拟不存在或不可读取的私有配置, 构建均不得生成、读取或校验运行密钥.
function Test-Path {
    param([string]$LiteralPath)
    if ($LiteralPath -eq (Join-Path $Root 'deploy/.env')) {
        return $global:LocalStartupBuildEnvironmentExists
    }
    Microsoft.PowerShell.Management\Test-Path -LiteralPath $LiteralPath
}

# 读取公开模板用于屏蔽宿主覆盖, 构建不得读取私有配置.
function Get-Content {
    param([string]$LiteralPath, [string]$Encoding)
    if ($LiteralPath -like '*.env.example') { return Microsoft.PowerShell.Management\Get-Content @PSBoundParameters }
    throw '构建不应读取运行密钥或生成私有配置'
}

$global:LocalStartupExpectJavaUpdate = $true
try {
    foreach ($Exists in @($false, $true)) {
        $global:LocalStartupBuildEnvironmentExists = $Exists
        $global:LocalStartupDockerCalls.Clear()
        & (Join-Path $Root 'deploy/scripts/dev.ps1') -Action build
        $BuildCall = @($global:LocalStartupDockerCalls | Where-Object { $_ -match ' build platform-admin\s*$' })
        if ($BuildCall.Count -ne 1 -or $BuildCall[0] -notmatch '\.env\.example"?\s+-f\s+') {
            throw '构建必须使用公开模板且在容器内联网安装依赖'
        }
    }
} finally {
    Remove-Item Function:\Test-Path, Function:\Get-Content
    Remove-Variable LocalStartupBuildEnvironmentExists, LocalStartupExpectJavaUpdate -Scope Global
}
Remove-Variable LocalStartupDockerCalls -Scope Global
Write-Host '本地启动回归通过: 就绪结构、缺表、旧版本、认证失败、旧镜像、缺镜像、阶段顺序、下载失败中止、构建失败诊断、构建启动分离、构建使用公开模板及两个脚本入口。'
