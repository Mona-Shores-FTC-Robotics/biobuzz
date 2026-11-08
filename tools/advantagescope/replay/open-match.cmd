@echo off
rem BIOBUZZ match replay from a checkout: open-match Q-14, or no match to pick from a list.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0open-match.ps1" %*
