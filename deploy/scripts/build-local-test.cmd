@echo off
setlocal

cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0dev.ps1" -Action build %*
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
  echo.
  echo Build failed. Check the errors above and network access to image and dependency registries, then retry.
) else (
  echo.
  echo Build completed. Run start-local-test.cmd to start the environment.
)

pause
exit /b %EXIT_CODE%
