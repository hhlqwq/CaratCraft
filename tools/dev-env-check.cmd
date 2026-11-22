@echo off
setlocal
node "%~dp0maintenance\dev-env.cjs" diagnose
exit /b %ERRORLEVEL%
