# Packs everything a laptop needs to play BIOBUZZ match logs into one zip, to send to anyone:
#
#   tools\advantagescope\make-viewer-zip.cmd            (double-click), or
#   powershell -ExecutionPolicy Bypass -File tools\advantagescope\make-viewer-zip.ps1
#
# Writes Downloads\BIOBUZZ-AdvantageScope.zip with the robot model as committed (cad/advantagescope/Robot_BIOBUZZ),
# the match layout, install.cmd (viewer/, beside this script) and a README.txt that says which version it is.
# Whoever gets it: unzip, double-click install.cmd, import the layout, open a log. Make a new zip whenever the model
# or the layout changes (after from_onshape.py, or a layout edit) and send that one; the old logs still play.

$ErrorActionPreference = "Stop"
$repo = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$cad = Get-Item (Join-Path $repo "cad\advantagescope\Robot_BIOBUZZ")
if (-not (Test-Path (Join-Path $cad.FullName "model.glb"))) { throw "cad\advantagescope\Robot_BIOBUZZ has no model.glb: is the checkout complete?" }

# Which model this is: the commit, and the CAD version the model was built from.
$commit = "unknown"
try { $commit = (git -C $repo log -1 --format="%h %cd" --date=short).Trim() } catch { }
$source = Get-Content -Raw (Join-Path $repo "cad\advantagescope\onshape_source.json") | ConvertFrom-Json

# Stage the zip's contents in a fresh temp folder.
$stage = Join-Path $env:TEMP "BIOBUZZ-AdvantageScope"
if (Test-Path $stage) { Remove-Item -Recurse -Force $stage }
New-Item -ItemType Directory -Force $stage | Out-Null
Copy-Item -Recurse $cad.FullName $stage
Copy-Item (Join-Path $PSScriptRoot "match-layout.json") (Join-Path $stage "biobuzz-match-layout.json")
Copy-Item (Join-Path $PSScriptRoot "viewer\install.cmd") $stage
Copy-Item (Join-Path $PSScriptRoot "viewer\install.ps1") $stage
@"
BIOBUZZ match logs in AdvantageScope (Mona Shores FTC 19429 and 20245)

Once per laptop, and again when you get a newer zip:
  1. Install AdvantageScope (v27 alpha 6 or newer): https://github.com/Mechanical-Advantage/AdvantageScope/releases
  2. Unzip this whole zip somewhere, then double-click install.cmd.
  3. In AdvantageScope: File > Import Layout... > Downloads\biobuzz-match-layout.json

Then open any BIOBUZZ .wpilog (File > Open Logs...). The 3D tab shows the robot, its turret and FLOWER stick moving.
The field downloads the first time a Field tab opens it, so that first open needs internet.

Without install.cmd: AdvantageScope > Help > Show Assets Folder, copy the Robot_BIOBUZZ folder into the userAssets
folder there, restart AdvantageScope, and import the layout as in step 3.

This zip: commit $commit; robot model from the Onshape CAD as built $($source.built).
"@ | Set-Content -Encoding UTF8 (Join-Path $stage "README.txt")

$zip = Join-Path $HOME "Downloads\BIOBUZZ-AdvantageScope.zip"
if (Test-Path $zip) { Remove-Item -Force $zip }
Compress-Archive -Path (Join-Path $stage "*") -DestinationPath $zip
Remove-Item -Recurse -Force $stage
Write-Host "Wrote $zip (commit $commit). Send it to anyone who should play the logs."
