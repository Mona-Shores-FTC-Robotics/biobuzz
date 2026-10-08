@echo off
rem Watch BIOBUZZ: pick a simulated Auto and open its best (or typical) log in AdvantageScope.
rem Run from anywhere; it finds the repo from its own folder, pulls the branch (so this menu and the
rem robot model stay current), then hands the key to setup-advantagescope.ps1 -Open, which fetches the log.
rem The pull and the launch share one line on purpose: cmd reads a batch file as it runs, and this
rem file may change in the pull.
setlocal
set "REPO=%~dp0..\.."
echo.
echo  BIOBUZZ - which Auto?
echo    1  L-Quals        the qualifier Auto from the drive team's left start (no turret)
echo    2  R-Quals        the qualifier Auto from the right start, 3 TIPs on its own (no turret)
echo    3  A whole match  L-Quals pair on red against R-Quals pair on blue, all four robots
echo    4  Flower first   ShootsRight option, fires from the FLOWER seat (needs the turret)
echo    5  Stages, angled partner
echo    6  Stages, wall partner
echo    7  Seat fire      shelved (needs the turret)
echo    8  Sister         both of our robots, 4 TIPs and 2 PARK (no turret)
echo.
set /p "N=Number [1]: "
if "%N%"=="" set N=1
set KEY=
if "%N%"=="1" set KEY=l
if "%N%"=="2" set KEY=r
if "%N%"=="3" set KEY=match
if "%N%"=="4" set KEY=flowerfirst
if "%N%"=="5" set KEY=angled
if "%N%"=="6" set KEY=wall
if "%N%"=="7" set KEY=seatfire
if "%N%"=="8" set KEY=sister
if "%KEY%"=="" echo Not a choice: %N% & exit /b 1
set /p "T=Typical run instead of the best one? [n]: "
set TYP=
if /i "%T%"=="y" set TYP=-Typical
git -C "%REPO%" fetch -q origin claude/simulator && git -C "%REPO%" checkout -q claude/simulator && git -C "%REPO%" pull -q origin claude/simulator & powershell -ExecutionPolicy Bypass -File "%REPO%\tools\advantagescope\setup-advantagescope.ps1" -Open %KEY% %TYP%
endlocal
