@echo off
rem Double-click: pack the robot model, the match layout and an installer into Downloads\BIOBUZZ-AdvantageScope.zip.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0make-viewer-zip.ps1"
pause
