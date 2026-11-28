@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0..\deploy\scripts\dev.ps1" -Action diagnose
exit /b %ERRORLEVEL%
