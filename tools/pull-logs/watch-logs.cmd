@echo off
rem Double-click at the start of a meeting: watch the robot and open each log as its OpMode stops.
rem Its own window, so it outlives Android Studio. Close the window (or Ctrl+C) to stop.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Pull-MatchLogs.ps1" -Watch %*
pause
