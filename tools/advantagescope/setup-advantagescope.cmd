@echo off
rem Double-click: install the BIOBUZZ robot model into AdvantageScope and put the match layout in Downloads.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-advantagescope.ps1" %*
pause
