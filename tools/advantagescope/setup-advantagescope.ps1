# Sets a laptop's AdvantageScope up for the robot's match logs, in one command:
#
#   tools\advantagescope\setup-advantagescope.cmd            (double-click), or
#   powershell -ExecutionPolicy Bypass -File tools\advantagescope\setup-advantagescope.ps1 [-Open <file.wpilog>] [-Hive]
#
# Every run: installs the team's robot model (cad/advantagescope/Robot_BIOBUZZ, committed) into AdvantageScope's
# userAssets, replacing its older copy and nothing else; copies the match layout (match-layout.json, beside this
# script) to Downloads and says so when it changed (then File > Import Layout... once); and with -Open, starts
# AdvantageScope on that log. Restart AdvantageScope after an asset change: it reads userAssets at start.
#
# The 3D tabs draw the HIVE on the HIVE field ("2026-2027 Field (HIVE sim)", "BIOBUZZ HIVE"). No FIRST CAD is committed,
# so those are cut on each laptop from the field AdvantageScope downloaded (HiveAssetsTest): this script builds and
# installs them when they are missing, or always with -Hive (after AdvantageScope updates its field). That needs, once,
# AdvantageScope to have shown the 2026-2027 field in a 3D tab, and Android Studio (its Java builds them); the first
# build downloads Gradle's tools and takes a few minutes. If it can't build, it says why and installs the rest anyway.
param([string]$Open = "", [switch]$Hive)

$ErrorActionPreference = "Stop"
$repo = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$scope = Join-Path $env:APPDATA "AdvantageScope"

# 1. Our robot from the CAD, committed.
$cad = Get-Item (Join-Path $repo "cad\advantagescope\Robot_BIOBUZZ")
if (-not (Test-Path (Join-Path $cad.FullName "model.glb"))) { throw "cad\advantagescope\Robot_BIOBUZZ has no model.glb: is the checkout complete?" }

# 2. Replace the older copy of this model, and only that: other BIOBUZZ folders (the HIVE field and the HIVE, step 3;
# the simulator's designs model) are built on this laptop and are slow or impossible to put back.
$user = Join-Path $scope "userAssets"
New-Item -ItemType Directory -Force $user | Out-Null
$old = Join-Path $user $cad.Name
if (Test-Path $old) {
    Remove-Item -Recurse -Force $old
    Write-Host "Removed old $($cad.Name)"
}
Copy-Item -Recurse -Force $cad.FullName $user
Write-Host "Installed $($cad.Name) as 'BIOBUZZ Robot'"

# 3. The HIVE field and the HIVE that tips, built from AdvantageScope's own field when missing (or with -Hive).
$hiveNames = @("Field3d_BIOBUZZHiveSim", "Robot_BIOBUZZHive")
$missing = $hiveNames | Where-Object { -not (Test-Path (Join-Path $user "$_\config.json")) }
if ($Hive -or $missing) {
    try {
        # The stock field: the Field3d_... folder in autoAssets whose config.json is "2026-2027 Field" (a Field2d_ one has
        # the same name).
        $auto = Join-Path $scope "autoAssets"
        $config = $null
        if (Test-Path $auto) {
            $config = Get-ChildItem $auto -Directory -Filter "Field3d_*" |
                ForEach-Object { Get-ChildItem $_.FullName -Recurse -Filter config.json } |
                Where-Object { (Get-Content $_.FullName -Raw) -match '"name"\s*:\s*"2026-2027 Field"' } |
                Where-Object { Test-Path (Join-Path $_.DirectoryName "model.glb") } |
                Select-Object -First 1
        }
        if (-not $config) {
            throw "AdvantageScope hasn't downloaded the 3D 2026-2027 field yet. Open AdvantageScope, add a 3D Field tab, pick 2026-2027 Field, wait until it appears, close AdvantageScope, then run this again."
        }
        $env:BIOBUZZ_FIELD3D = $config.DirectoryName
        # Java and the Android SDK, from Android Studio unless already set.
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
        Write-Host ""
        Write-Host "Building the HIVE field and the HIVE from $($config.DirectoryName) ..."
        $built = Join-Path $repo "TeamCode\build\advantagescope"
        Push-Location $repo
        try {
            & .\gradlew.bat :TeamCode:testDebugUnitTest --tests "*HiveAssetsTest*" --rerun
            if ($LASTEXITCODE -ne 0) { throw "The build failed (see above)." }
        } finally {
            Pop-Location
        }
        foreach ($name in $hiveNames) {
            $dir = Join-Path $built $name
            if (-not (Test-Path (Join-Path $dir "config.json"))) { throw "The build didn't write $name into $built." }
            $old = Join-Path $user $name
            if (Test-Path $old) { Remove-Item -Recurse -Force $old }
            Copy-Item -Recurse -Force $dir $user
            Write-Host "Installed $name"
        }
    } catch {
        Write-Host ""
        Write-Host "The HIVE field and the HIVE were not installed: $($_.Exception.Message)"
        Write-Host "The robot and the layout are; the 3D tabs need the HIVE field. Fix that and run this again."
    }
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
