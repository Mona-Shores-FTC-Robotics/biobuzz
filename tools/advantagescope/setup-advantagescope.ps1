# Sets a laptop's AdvantageScope up for the robot's match logs, in one command:
#
#   tools\advantagescope\setup-advantagescope.cmd            (double-click), or
#   powershell -ExecutionPolicy Bypass -File tools\advantagescope\setup-advantagescope.ps1 [-Open <file.wpilog>]
#
# Every run: installs the team's robot model (cad/advantagescope/Robot_BIOBUZZ, committed) into AdvantageScope's
# userAssets, replacing its older copy and nothing else; copies the match layout (match-layout.json, beside this
# script) to Downloads and says so when it changed (then File > Import Layout... once); and with -Open, starts
# AdvantageScope on that log. Restart AdvantageScope after an asset change: it reads userAssets at start.
#
# Nothing is built and nothing is downloaded. The 3D tabs draw the HIVE as the robot believed it, on the simulator's
# HIVE field ("2026-2027 Field (HIVE sim)", "BIOBUZZ HIVE"). Those are cut from AdvantageScope's own field on each
# laptop, since no FIRST CAD is committed: this script says when they are missing and how to get them.
param([string]$Open = "")

$ErrorActionPreference = "Stop"
$repo = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$scope = Join-Path $env:APPDATA "AdvantageScope"

# 1. Our robot from the CAD, committed.
$cad = Get-Item (Join-Path $repo "cad\advantagescope\Robot_BIOBUZZ")
if (-not (Test-Path (Join-Path $cad.FullName "model.glb"))) { throw "cad\advantagescope\Robot_BIOBUZZ has no model.glb: is the checkout complete?" }

# 2. Replace the older copy of this model, and only that: the simulator's assets (the HIVE field, the HIVE that tips,
# the designs model) are BIOBUZZ folders too, built on this laptop from AdvantageScope's own field, and can't be put
# back from here.
$user = Join-Path $scope "userAssets"
New-Item -ItemType Directory -Force $user | Out-Null
$old = Join-Path $user $cad.Name
if (Test-Path $old) {
    Remove-Item -Recurse -Force $old
    Write-Host "Removed old $($cad.Name)"
}
Copy-Item -Recurse -Force $cad.FullName $user
Write-Host "Installed $($cad.Name) as 'BIOBUZZ Robot'"

# 3. The HIVE field and the HIVE that tips: only checked, never built or removed here.
$hive = @("Field3d_BIOBUZZHiveSim", "Robot_BIOBUZZHive") | Where-Object { -not (Test-Path (Join-Path $user "$_\config.json")) }
if ($hive) {
    Write-Host ""
    Write-Host "Not installed: $($hive -join ', '). The 3D tabs draw the field and the HIVE from them."
    Write-Host "Build them once with the simulator branch's tools\advantagescope\watch-biobuzz.cmd (claude/simulator),"
    Write-Host "or run install.cmd from a viewer zip made on a laptop that has them."
}

# 4. The match layout, where AdvantageScope's Import Layout can find it; a word only when it changed.
$layout = Join-Path $HOME "Downloads\biobuzz-match-layout.json"
$source = Join-Path $PSScriptRoot "match-layout.json"
$layoutChanged = -not (Test-Path $layout) -or ((Get-FileHash $layout).Hash -ne (Get-FileHash $source).Hash)
Copy-Item -Force $source $layout

Write-Host ""
if ($layoutChanged) { Write-Host "The layout is new or changed: in AdvantageScope do File > Import Layout... > $layout" }
Write-Host "Restart AdvantageScope if it was open (it reads the assets at start)."

# 5. -Open: start AdvantageScope on a log.
if ($Open) {
    if (-not (Test-Path $Open)) { throw "No such log: $Open" }
    $exe = Join-Path $env:LOCALAPPDATA "Programs\advantagescope\AdvantageScope.exe"
    if (-not (Test-Path $exe)) {
        $found = Get-ChildItem (Join-Path $env:LOCALAPPDATA "Programs") -Recurse -Filter "AdvantageScope*.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) { $exe = $found.FullName } else { throw "AdvantageScope not found under $env:LOCALAPPDATA\Programs; open the log by hand: $Open" }
    }
    Get-Process -Name "AdvantageScope*" -ErrorAction SilentlyContinue | Stop-Process
    Start-Process -FilePath $exe -ArgumentList "`"$Open`""
    Write-Host "Opened $Open"
}
