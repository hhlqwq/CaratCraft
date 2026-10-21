param([string]$Date = (Get-Date -Format 'yyyy-MM-dd'))

$ErrorActionPreference = 'Stop'
$Day = [datetime]::ParseExact($Date, 'yyyy-MM-dd', [Globalization.CultureInfo]::InvariantCulture).ToString('yyyy-MM-dd')
$TemporaryRoot = [IO.Path]::GetFullPath('D:\tmp\hailongcodex')
$Temporary = [IO.Path]::GetFullPath((Join-Path $TemporaryRoot ("inventory-$Day-" + [guid]::NewGuid().ToString('N'))))
if (-not $Temporary.StartsWith($TemporaryRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
    throw '测试临时目录超出约定范围'
}
$Tokens = $null
$Errors = $null
$Ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $PSScriptRoot 'Build-CachedImages.ps1'), [ref]$Tokens, [ref]$Errors)
if ($Errors.Count) { throw ($Errors | Out-String) }
foreach ($Definition in $Ast.FindAll({ param($Node) $Node -is [Management.Automation.Language.FunctionDefinitionAst] }, $false)) {
    . ([scriptblock]::Create($Definition.Extent.Text))
}
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

# 写入最小合成JAR, 可让第二个应用的common模块不同以验证封装前拒绝.
function Write-TestModuleArchive {
    param([string]$Path, [switch]$Different)
    $File = [IO.File]::Create($Path)
    $Archive = [IO.Compression.ZipArchive]::new($File, [IO.Compression.ZipArchiveMode]::Create)
    try {
        foreach ($Module in @('platform-common', 'platform-biz')) {
            $Entry = $Archive.CreateEntry("BOOT-INF/lib/$Module-2.0.0.jar")
            $Payload = if ($Different -and $Module -eq 'platform-common') { '不同字节' } else { "$Module-合成内容" }
            $Bytes = [Text.Encoding]::UTF8.GetBytes($Payload)
            $Stream = $Entry.Open()
            try { $Stream.Write($Bytes, 0, $Bytes.Length) }
            finally { $Stream.Dispose() }
        }
    } finally { $Archive.Dispose(); $File.Dispose() }
}

try {
    $Backend = Join-Path $Temporary 'backend'
    $Frontend = Join-Path $Temporary 'frontend'
    $Relative = 'legal/generated/runtime-components.json'
    $Records = @(
        @{file = 'platform-common-2.0.0.jar'; coordinates = 'com.platform:platform-common:2.0.0'; sha256 = 'old'},
        @{file = 'platform-biz-2.0.0.jar'; coordinates = 'com.platform:platform-biz:2.0.0'; sha256 = 'old'},
        @{file = 'third-party.jar'; coordinates = 'test:third-party:1'; sha256 = 'unchanged'; status = '原有中文许可说明'}
    )
    foreach ($Context in @($Backend, $Frontend)) {
        $Destination = Join-Path $Context $Relative
        New-Item -ItemType Directory -Path (Split-Path $Destination -Parent) -Force | Out-Null
        $Json = @{backend = $Records; frontend = @(); note = '无BOM的中文索引'} | ConvertTo-Json -Depth 20
        [IO.File]::WriteAllText($Destination, $Json, [Text.UTF8Encoding]::new($false))
    }
    Write-TestModuleArchive -Path (Join-Path $Backend 'platform-admin.jar')
    Write-TestModuleArchive -Path (Join-Path $Backend 'platform-api.jar')
    Update-CachedModuleInventory -Backend $Backend -Frontend $Frontend
    $First = [IO.File]::ReadAllText((Join-Path $Backend $Relative), [Text.Encoding]::UTF8)
    $Second = [IO.File]::ReadAllText((Join-Path $Frontend $Relative), [Text.Encoding]::UTF8)
    $Result = $First | ConvertFrom-Json
    if ($First -ne $Second -or $Result.note -ne '无BOM的中文索引' -or
        $Result.backend[2].sha256 -ne 'unchanged' -or $Result.backend[2].status -ne '原有中文许可说明' -or
        $Result.backend[0].sha256 -notmatch '^[0-9a-f]{64}$') {
        throw '中文索引、第三方记录或本项目摘要不符合要求'
    }
    Write-TestModuleArchive -Path (Join-Path $Backend 'platform-api.jar') -Different
    $Failure = $null
    try { Update-CachedModuleInventory -Backend $Backend -Frontend $Frontend }
    catch { $Failure = $_.Exception.Message }
    if (-not $Failure -or $Failure -notmatch '字节不一致') { throw '未拒绝两个应用的模块冲突' }
    Write-Host '构建索引回归通过: 无BOM的UTF-8中文、第三方记录保留、模块摘要同步及冲突阻断。'
} finally {
    if (Test-Path -LiteralPath $Temporary) { Remove-Item -LiteralPath $Temporary -Recurse -Force }
}
