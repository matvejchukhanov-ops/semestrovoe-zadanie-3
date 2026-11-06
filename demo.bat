@echo off
REM Запуск живой демонстрации (обходит политику выполнения PowerShell)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0demo.ps1"
pause
