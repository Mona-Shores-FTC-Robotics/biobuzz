@echo off
rem Double-click: pull the robot's match logs over Wi-Fi and open the newest in AdvantageScope.
rem Pass -Watch to keep it running during a meeting:   pull-logs.cmd -Watch
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Pull-MatchLogs.ps1" %*
if errorlevel 1 pause
