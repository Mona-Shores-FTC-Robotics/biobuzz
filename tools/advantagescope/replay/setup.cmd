@echo off
rem Double-click: set this laptop up for BIOBUZZ match replay (AdvantageScope, models, layout, logs). Safe to run again.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup.ps1" %*
pause
