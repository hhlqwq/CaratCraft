@echo off
setlocal

cd /d "%~dp0"

if not exist "vendor\platform-wxshop\deploy\.env" (
  copy /y "vendor\platform-wxshop\deploy\.env.example" "vendor\platform-wxshop\deploy\.env" >nul
)

docker compose --env-file "vendor\platform-wxshop\deploy\.env" -f "vendor\platform-wxshop\docker-compose.yml" up --detach --no-build
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
  echo.
  echo Startup failed. Please check the output above.
) else (
  echo.
  echo Startup submitted. Open http://localhost:8888/ after the containers become healthy.
)

pause
exit /b %EXIT_CODE%
