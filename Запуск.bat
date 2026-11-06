@echo off
chcp 65001 >nul
echo Searching passwords from the assignment hashes...
echo (first run is slower - GPU kernels are being compiled)
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run_crack.ps1"
if errorlevel 1 pause
