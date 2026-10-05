# Sets up AdvantageScope to watch this branch's simulated logs: builds the custom assets the logs use
# (the field "2026-2027 Field (HIVE sim)", the robot "BIOBUZZ HIVE" whose CELLs tip, and our robot
# "BIOBUZZ Robot" with its Limelight where it is measured), replaces any older BIOBUZZ assets in
# AdvantageScope's userAssets folder with them, and puts this branch's layout in Downloads.
#
# Run it once per laptop, again after switching to another branch (each branch may draw the robot
# differently), and after AdvantageScope updates its field. Windows PowerShell:
#
#   powershell -ExecutionPolicy Bypass -File tools\advantagescope\setup-advantagescope.ps1
#
# Needs: AdvantageScope opened once with the 2026-2027 field shown (so it has downloaded the stock
# field), and Android Studio (its JDK and Android SDK build this, as for the robot code). The HIVE
# assets are cut from the field AdvantageScope downloaded, on your laptop: no FIRST CAD is committed.

$ErrorActionPreference = "Stop"
$repo = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$scope = Join-Path $env:APPDATA "AdvantageScope"

# 1. The stock field AdvantageScope downloaded: the Field3d_... folder whose config.json is "2026-2027 Field".
$auto = Join-Path $scope "autoAssets"
$config = $null
if (Test-Path $auto) {
    $config = Get-ChildItem $auto -Recurse -Filter config.json |
        Where-Object { (Get-Content $_.FullName -Raw) -match '"name"\s*:\s*"2026-2027 Field"' } |
        Select-Object -First 1
}
if (-not $config) {
    throw "AdvantageScope hasn't downloaded the 2026-2027 field yet. Open AdvantageScope, add a 3D Field tab, pick 2026-2027 Field, wait for it to appear, close AdvantageScope, then run this again."
}
$env:BIOBUZZ_FIELD3D = $config.DirectoryName

# 2. Java and the Android SDK, from Android Studio unless already set.
if (-not $env:JAVA_HOME) {
    $jbr = Join-Path $env:ProgramFiles "Android\Android Studio\jbr"
    if (-not (Test-Path $jbr)) { throw "Android Studio isn't installed (no $jbr). Install it, or set JAVA_HOME to a JDK 17 or newer." }
    $env:JAVA_HOME = $jbr
}
if (-not $env:ANDROID_HOME -and -not (Test-Path (Join-Path $repo "local.properties"))) {
    $sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
    if (-not (Test-Path $sdk)) { throw "No Android SDK at $sdk. Open this folder in Android Studio once, let it sync, then run this again." }
    $env:ANDROID_HOME = $sdk
}

# 3. Build this branch's assets, from scratch (the first run downloads build tools: a few minutes).
$built = Join-Path $repo "TeamCode\build\advantagescope"
if (Test-Path $built) { Remove-Item -Recurse -Force $built }
Push-Location $repo
try {
    & .\gradlew.bat :TeamCode:testDebugUnitTest --tests "*HiveAssetsTest*" --tests "*RobotAssetsTest*" --rerun
    if ($LASTEXITCODE -ne 0) { throw "The build failed (see above)." }
} finally {
    Pop-Location
}
$assets = Get-ChildItem $built -Directory | Where-Object { $_.Name -like "Field3d_*" -or $_.Name -like "Robot_*" }
if (-not ($assets | Where-Object Name -eq "Field3d_BIOBUZZHiveSim")) { throw "The build didn't write the HIVE assets into $built." }

# 4. Replace every older BIOBUZZ asset in userAssets with this branch's.
$user = Join-Path $scope "userAssets"
New-Item -ItemType Directory -Force $user | Out-Null
Get-ChildItem $user -Directory | Where-Object { $_.Name -like "*BIOBUZZ*" } | ForEach-Object {
    Remove-Item -Recurse -Force $_.FullName
    Write-Host "Removed old $($_.Name)"
}
foreach ($a in $assets) {
    Copy-Item -Recurse -Force $a.FullName $user
    Write-Host "Installed $($a.Name)"
}

# 5. This branch's layout, where AdvantageScope's Import Layout can find it.
$layout = Join-Path $HOME "Downloads\advantagescope-layout.json"
Copy-Item -Force (Join-Path $repo "sim-review\advantagescope-layout.json") $layout

$branch = git -C $repo rev-parse --abbrev-ref HEAD
Write-Host ""
Write-Host "Done, for branch $branch. Now start AdvantageScope and do File > Import Layout... > $layout"
