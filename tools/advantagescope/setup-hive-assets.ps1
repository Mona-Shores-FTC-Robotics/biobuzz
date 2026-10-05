# Builds the AdvantageScope assets our simulated logs use, "2026-2027 Field (HIVE sim)" and the robot
# "BIOBUZZ HIVE" (the HIVE whose CELLs tip), plus our own robots "BIOBUZZ Robot" (its Limelight as a
# camera view) and "BIOBUZZ Robot (side walls)" (walls that slide in a log), and copies them into
# AdvantageScope's userAssets folder. Once per laptop, and again after AdvantageScope updates its field. Windows PowerShell.
#
# Needs: AdvantageScope opened once with the 2026-2027 field shown (so it has downloaded the stock
# field), Android Studio (its JDK and Android SDK build this, as for the robot code), and this repository.
# The assets are cut from the field AdvantageScope downloaded, on your laptop: no FIRST CAD is committed.
#
#   powershell -ExecutionPolicy Bypass -File tools\advantagescope\setup-hive-assets.ps1

$ErrorActionPreference = "Stop"
$repo = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$scope = Join-Path $env:APPDATA "AdvantageScope"

# 1. The stock field AdvantageScope downloaded: the Field3d_... folder whose config.json is "2026-2027 Field".
$auto = Join-Path $scope "autoAssets"
if (-not (Test-Path $auto)) {
    throw "No $auto. Open AdvantageScope, open a 3D Field tab, pick the 2026-2027 Field, wait for it to load, then run this again."
}
$config = Get-ChildItem $auto -Recurse -Filter config.json |
    Where-Object { (Get-Content $_.FullName -Raw) -match '"name"\s*:\s*"2026-2027 Field"' } |
    Select-Object -First 1
if (-not $config) {
    throw "AdvantageScope hasn't downloaded the 2026-2027 field yet. Open a 3D Field tab, pick 2026-2027 Field, wait for it to load, then run this again."
}
$env:BIOBUZZ_FIELD3D = $config.DirectoryName
Write-Host "Stock field: $env:BIOBUZZ_FIELD3D"

# 2. Java and the Android SDK, from Android Studio unless already set.
if (-not $env:JAVA_HOME) {
    $jbr = Join-Path $env:ProgramFiles "Android\Android Studio\jbr"
    if (-not (Test-Path $jbr)) { throw "No JAVA_HOME and no $jbr. Install Android Studio (or set JAVA_HOME to a JDK 17 or newer)." }
    $env:JAVA_HOME = $jbr
}
if (-not $env:ANDROID_HOME -and -not (Test-Path (Join-Path $repo "local.properties"))) {
    $sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
    if (-not (Test-Path $sdk)) { throw "No Android SDK at $sdk. Open this project in Android Studio once and let it sync." }
    $env:ANDROID_HOME = $sdk
}

# 3. Build (the first run downloads Gradle and the project's libraries: a few minutes).
Push-Location $repo
try {
    & .\gradlew.bat :TeamCode:testDebugUnitTest --tests "*HiveAssetsTest*" --tests "*RobotAssetsTest*" --rerun
    if ($LASTEXITCODE -ne 0) { throw "The build failed (see above)." }
} finally {
    Pop-Location
}
$built = Join-Path $repo "TeamCode\build\advantagescope"
$folders = "Field3d_BIOBUZZHiveSim", "Robot_BIOBUZZHive", "Robot_BIOBUZZ", "Robot_BIOBUZZWalls"
foreach ($f in $folders) {
    if (-not (Test-Path (Join-Path $built $f))) { throw "The build didn't write $built\$f." }
}

# 4. Into AdvantageScope's userAssets.
$user = Join-Path $scope "userAssets"
New-Item -ItemType Directory -Force $user | Out-Null
Copy-Item -Recurse -Force ($folders | ForEach-Object { Join-Path $built $_ }) $user
Write-Host "Done: copied into $user. Restart AdvantageScope; the field '2026-2027 Field (HIVE sim)' and the robots 'BIOBUZZ HIVE', 'BIOBUZZ Robot' and 'BIOBUZZ Robot (side walls)' now appear."
