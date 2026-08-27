@echo off
setlocal

cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; $sourcePath = Join-Path (Get-Location) 'dev.ps1'; $temporaryPath = Join-Path (Split-Path -Parent $sourcePath) ('.stop-local-test-' + [guid]::NewGuid().ToString('N') + '.ps1'); $utf8NoBom = New-Object System.Text.UTF8Encoding($false); $utf8Bom = New-Object System.Text.UTF8Encoding($true); $exitCode = 1; try { [System.IO.File]::WriteAllText($temporaryPath, [System.IO.File]::ReadAllText($sourcePath, $utf8NoBom), $utf8Bom); & $temporaryPath -Action stop; if ($LASTEXITCODE -eq 0) { $exitCode = 0 } } catch { Write-Host $_ -ForegroundColor Red } finally { if (Test-Path -LiteralPath $temporaryPath) { Remove-Item -LiteralPath $temporaryPath -Force } }; exit $exitCode"
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
  echo.
  echo Stop failed. Please check the output above.
) else (
  echo.
  echo Local test environment has stopped.
)

pause
exit /b %EXIT_CODE%
