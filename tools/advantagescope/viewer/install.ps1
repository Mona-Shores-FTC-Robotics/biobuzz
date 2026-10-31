# Sets AdvantageScope up to play BIOBUZZ match logs, from the zip this file came in (no repository needed).
# Double-click install.cmd beside it. Run it again when you get a newer zip.
#
# It installs the robot model (Robot_BIOBUZZ, beside this script) into AdvantageScope's userAssets, replacing any
# older BIOBUZZ model there, and copies the layout to Downloads. Then, in AdvantageScope: File > Import Layout...
# once, and restart it if it was open (it reads its assets at start).

$ErrorActionPreference = "Stop"
$scope = Join-Path $env:APPDATA "AdvantageScope"

# 1. The robot model, beside this script.
$cad = Get-Item (Join-Path $PSScriptRoot "Robot_BIOBUZZ")
if (-not (Test-Path (Join-Path $cad.FullName "model.glb"))) { throw "Robot_BIOBUZZ has no model.glb: unzip the whole zip, then run this again." }

# 2. Replace every older BIOBUZZ asset in userAssets with this one.
$user = Join-Path $scope "userAssets"
New-Item -ItemType Directory -Force $user | Out-Null
Get-ChildItem $user -Directory | Where-Object { $_.Name -like "*BIOBUZZ*" } | ForEach-Object {
    Remove-Item -Recurse -Force $_.FullName
    Write-Host "Removed old $($_.Name)"
}
Copy-Item -Recurse -Force $cad.FullName $user
Write-Host "Installed $($cad.Name) as 'BIOBUZZ Robot'"

# 3. The layout, where AdvantageScope's Import Layout can find it.
$layout = Join-Path $HOME "Downloads\biobuzz-match-layout.json"
Copy-Item -Force (Join-Path $PSScriptRoot "biobuzz-match-layout.json") $layout

Write-Host ""
Write-Host "Now in AdvantageScope: File > Import Layout... > $layout"
Write-Host "Restart AdvantageScope if it was open, then open a .wpilog."
