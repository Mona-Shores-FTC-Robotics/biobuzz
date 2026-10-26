# Sets a laptop's AdvantageScope up for the robot's match logs, in one command:
#
#   tools\advantagescope\setup-advantagescope.cmd            (double-click), or
#   powershell -ExecutionPolicy Bypass -File tools\advantagescope\setup-advantagescope.ps1 [-Open <file.wpilog>]
#
# Every run: installs the team's robot model (cad/advantagescope/Robot_BIOBUZZ, committed) into AdvantageScope's
# userAssets, replacing any older BIOBUZZ asset there; copies the match layout (match-layout.json, beside this
# script) to Downloads and says so when it changed (then File > Import Layout... once); and with -Open, starts
# AdvantageScope on that log. Restart AdvantageScope after an asset change: it reads userAssets at start.
#
# Nothing is built and nothing is downloaded. The field is AdvantageScope's own 2026-2027 FTC field, which it
# fetches the first time a Field tab selects it.
param([string]$Open = "")

$ErrorActionPreference = "Stop"
$repo = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$scope = Join-Path $env:APPDATA "AdvantageScope"

# 1. Our robot from the CAD, committed.
$cad = Get-Item (Join-Path $repo "cad\advantagescope\Robot_BIOBUZZ")
if (-not (Test-Path (Join-Path $cad.FullName "model.glb"))) { throw "cad\advantagescope\Robot_BIOBUZZ has no model.glb: is the checkout complete?" }

# 2. Replace every older BIOBUZZ asset in userAssets with this one.
$user = Join-Path $scope "userAssets"
New-Item -ItemType Directory -Force $user | Out-Null
Get-ChildItem $user -Directory | Where-Object { $_.Name -like "*BIOBUZZ*" } | ForEach-Object {
    Remove-Item -Recurse -Force $_.FullName
    Write-Host "Removed old $($_.Name)"
}
Copy-Item -Recurse -Force $cad.FullName $user
Write-Host "Installed $($cad.Name) as 'BIOBUZZ Robot'"

# 3. The match layout, where AdvantageScope's Import Layout can find it; a word only when it changed.
$layout = Join-Path $HOME "Downloads\biobuzz-match-layout.json"
$source = Join-Path $PSScriptRoot "match-layout.json"
$layoutChanged = -not (Test-Path $layout) -or ((Get-FileHash $layout).Hash -ne (Get-FileHash $source).Hash)
Copy-Item -Force $source $layout

Write-Host ""
if ($layoutChanged) { Write-Host "The layout is new or changed: in AdvantageScope do File > Import Layout... > $layout" }
Write-Host "Restart AdvantageScope if it was open (it reads the assets at start)."

# 4. -Open: start AdvantageScope on a log.
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
