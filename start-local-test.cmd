@echo off
setlocal

cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; $sourcePath = Join-Path (Get-Location) 'dev.ps1'; $temporaryPath = Join-Path (Split-Path -Parent $sourcePath) ('.start-local-test-' + [guid]::NewGuid().ToString('N') + '.ps1'); $utf8NoBom = New-Object System.Text.UTF8Encoding($false); $utf8Bom = New-Object System.Text.UTF8Encoding($true); $exitCode = 1; try { [System.IO.File]::WriteAllText($temporaryPath, [System.IO.File]::ReadAllText($sourcePath, $utf8NoBom), $utf8Bom); ^& $temporaryPath -NoBuild; if ($LASTEXITCODE -eq 0) { $exitCode = 0 } } catch { Write-Host $_ -ForegroundColor Red } finally { if (Test-Path -LiteralPath $temporaryPath) { Remove-Item -LiteralPath $temporaryPath -Force } }; exit $exitCode"
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
  echo.
  echo Startup failed. Please check the output above.
) else (
  echo.
  echo Local test environment is ready: http://localhost:8888/
)

pause
exit /b %EXIT_CODE%
