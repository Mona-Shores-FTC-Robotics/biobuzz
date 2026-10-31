# Sets AdvantageScope up to play BIOBUZZ match logs, from the zip this file came in (no repository needed).
# Double-click install.cmd beside it. Run it again when you get a newer zip.
#
# It installs the robot model (Robot_BIOBUZZ, beside this script) and, when the zip has them, the HIVE field and the
# HIVE (Field3d_BIOBUZZHiveSim, Robot_BIOBUZZHive) into AdvantageScope's userAssets, replacing the older copy of each
# (and nothing else), and copies the layout to Downloads. Then, in AdvantageScope: File > Import Layout...
# once, and restart it if it was open (it reads its assets at start).

$ErrorActionPreference = "Stop"
$scope = Join-Path $env:APPDATA "AdvantageScope"

# 1. The robot model, beside this script.
$cad = Get-Item (Join-Path $PSScriptRoot "Robot_BIOBUZZ")
if (-not (Test-Path (Join-Path $cad.FullName "model.glb"))) { throw "Robot_BIOBUZZ has no model.glb: unzip the whole zip, then run this again." }

# 2. Replace the older copy of this model, and only that: the simulator's assets (the HIVE field, the HIVE that tips,
# the designs model) are BIOBUZZ folders too, built on this laptop from AdvantageScope's own field, and can't be put
# back from here.
$user = Join-Path $scope "userAssets"
New-Item -ItemType Directory -Force $user | Out-Null
$assets = @($cad) + @(Get-ChildItem $PSScriptRoot -Directory | Where-Object { $_.Name -in @("Field3d_BIOBUZZHiveSim", "Robot_BIOBUZZHive") })
foreach ($a in $assets) {
    $old = Join-Path $user $a.Name
    if (Test-Path $old) {
        Remove-Item -Recurse -Force $old
        Write-Host "Removed old $($a.Name)"
    }
    Copy-Item -Recurse -Force $a.FullName $user
    Write-Host "Installed $($a.Name)"
}
if ($assets.Count -lt 3) { Write-Host "This zip has no HIVE field: the 3D tabs will show the robot but no field or HIVE." }

# 3. The layout, where AdvantageScope's Import Layout can find it.
$layout = Join-Path $HOME "Downloads\biobuzz-match-layout.json"
Copy-Item -Force (Join-Path $PSScriptRoot "biobuzz-match-layout.json") $layout

Write-Host ""
Write-Host "Now in AdvantageScope: File > Import Layout... > $layout"
Write-Host "Restart AdvantageScope if it was open, then open a .wpilog."
