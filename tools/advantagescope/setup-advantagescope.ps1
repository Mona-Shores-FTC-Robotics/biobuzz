# Keeps a laptop's AdvantageScope current with this branch, in one command, and opens a log:
#
#   powershell -ExecutionPolicy Bypass -File tools\advantagescope\setup-advantagescope.ps1 [-Open right|flowerfirst|angled|wall|alone|seatfire|<file>] [-Typical]
#
# What it does, every run: pulls nothing itself (the README's one-liner does the git part); installs this branch's
# robot model (cad/advantagescope/Robot_BIOBUZZ, committed) and the generated assets (the field "2026-2027 Field
# (HIVE sim)", the robot "BIOBUZZ HIVE" whose CELLs tip, "BIOBUZZ Robot (designs)") into AdvantageScope's userAssets,
# building the generated ones with Gradle only when the code that makes them changed since the last build (a stamp
# in TeamCode\build\advantagescope); downloads the README table's latest logs to Downloads\biobuzz-logs; copies the
# layout to Downloads and says so only when it changed (import it then: File > Import Layout...); and with -Open,
# starts AdvantageScope on that log (best, or -Typical). Restart AdvantageScope after an asset change: it reads
# userAssets at start.
#
# Needs, once: AdvantageScope opened with the 2026-2027 field shown (so it has downloaded the stock field), Git, and
# Android Studio (its JDK and Android SDK build the generated assets; no FIRST CAD is committed: the HIVE is cut from
# the field AdvantageScope downloaded, on your laptop).
param([string]$Open = "", [switch]$Typical)

$ErrorActionPreference = "Stop"
$repo = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$scope = Join-Path $env:APPDATA "AdvantageScope"

# 1. The stock field AdvantageScope downloaded: the Field3d_... folder whose config.json is "2026-2027 Field".
$auto = Join-Path $scope "autoAssets"
$config = $null
if (Test-Path $auto) {
    # Only a Field3d_ folder: AdvantageScope also keeps a 2D field (Field2d_...) with the same name.
    $config = Get-ChildItem $auto -Directory -Filter "Field3d_*" |
        ForEach-Object { Get-ChildItem $_.FullName -Recurse -Filter config.json } |
        Where-Object { (Get-Content $_.FullName -Raw) -match '"name"\s*:\s*"2026-2027 Field"' } |
        Where-Object { Test-Path (Join-Path $_.DirectoryName "model.glb") } |
        Select-Object -First 1
}
if (-not $config) {
    throw "AdvantageScope hasn't downloaded the 3D 2026-2027 field yet. Open AdvantageScope, click + at the top, choose 3D Field, pick 2026-2027 Field in its field menu, wait until the field appears, close AdvantageScope, then run this again."
}
Write-Host "Stock 3D field: $($config.DirectoryName)"
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

# 3. Build the generated assets, only when the code that makes them changed (the stamp is git's hash of that code and
# of the stock field's folder): the first run downloads build tools, a few minutes; after that, seconds.
$built = Join-Path $repo "TeamCode\build\advantagescope"
$stampFile = Join-Path $built "stamp.txt"
$stamp = (git -C $repo rev-parse "HEAD:TeamCode/src/test/java/org/firstinspires/ftc/teamcode/logging") + " " +
         (git -C $repo rev-parse "HEAD:TeamCode/src/test/resources/advantagescope") + " " + $config.DirectoryName
$have = @("Field3d_BIOBUZZHiveSim", "Robot_BIOBUZZHive", "Robot_BIOBUZZDesigns") | ForEach-Object { Test-Path (Join-Path $built "$_\config.json") }
if ((Test-Path $stampFile) -and ((Get-Content $stampFile -Raw).Trim() -eq $stamp) -and ($have -notcontains $false)) {
    Write-Host "Generated assets are current (built from this code before): not rebuilding."
} else {
    if (Test-Path $built) { Remove-Item -Recurse -Force $built }
    Push-Location $repo
    try {
        & .\gradlew.bat :TeamCode:testDebugUnitTest --tests "*HiveAssetsTest*" --tests "*RobotAssetsTest*" --rerun
        if ($LASTEXITCODE -ne 0) { throw "The build failed (see above)." }
    } finally {
        Pop-Location
    }
    Set-Content -Path $stampFile -Value $stamp
}
# Only what the layout uses: the field, the HIVE, and the designs model for logs of other designs. The build also
# writes the study models (Prototype, FullWidth, Walls, Shapes, MatchShapes); copy one by hand if a study needs it.
$assets = @(Get-ChildItem $built -Directory | Where-Object { $_.Name -in @("Field3d_BIOBUZZHiveSim", "Robot_BIOBUZZHive", "Robot_BIOBUZZDesigns") })
if (-not ($assets | Where-Object Name -eq "Field3d_BIOBUZZHiveSim")) { throw "The build didn't write the HIVE assets into $built." }
# Our robot from the CAD is committed, not built.
$cad = Get-Item (Join-Path $repo "cad\advantagescope\Robot_BIOBUZZ")
if (-not (Test-Path (Join-Path $cad.FullName "model.glb"))) { throw "cad\advantagescope\Robot_BIOBUZZ has no model.glb: is the checkout complete?" }
$assets += $cad

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

# 5. This branch's layout, where AdvantageScope's Import Layout can find it; a word only when it changed.
$layout = Join-Path $HOME "Downloads\advantagescope-layout.json"
$source = Join-Path $repo "sim-review\advantagescope-layout.json"
$layoutChanged = -not (Test-Path $layout) -or ((Get-FileHash $layout).Hash -ne (Get-FileHash $source).Hash)
Copy-Item -Force $source $layout

# 6. The README table's latest logs, from the sim-results branch, into one folder (the names in the README).
$logs = Join-Path $HOME "Downloads\biobuzz-logs"
New-Item -ItemType Directory -Force $logs | Out-Null
$autos = [ordered]@{ right = "qual-right-v"; flowerfirst = "qual-right-v-flower-first-carry-settle-b2"; angled = "qual-stages-angled-v"; wall = "qual-stages-wall-v"; alone = "qual-alone-flower-f45-s500"; seatfire = "qual-right-v-seatfire-west" }
$raw = "https://raw.githubusercontent.com/Mona-Shores-FTC-Robotics/biobuzz/sim-results"
$named = @{}
foreach ($k in $autos.Keys) {
    $auto = $autos[$k]
    try {
        $latest = Invoke-RestMethod -Uri "$raw/$auto/latest.json" -TimeoutSec 30
    } catch {
        Write-Host "No published log for $auto yet ($($_.Exception.Message))"
        continue
    }
    foreach ($label in @("best", "typical")) {
        $name = $latest.namedLogs.$label
        if (-not $name) { continue }
        $target = Join-Path $logs $name
        # A log's name carries only the day its route last changed, so the same name is published again whenever the
        # simulator changes: a sidecar remembers which commit the copy here came from, and a different one is fetched
        # again (7 Oct 2026: the mentor watched the morning's log all day).
        $stamp = "$target.commit"
        $have = if (Test-Path $stamp) { Get-Content $stamp -Raw } else { "" }
        if (-not (Test-Path $target) -or $have.Trim() -ne $latest.commit) {
            Invoke-WebRequest -Uri "$raw/$auto/$name" -OutFile $target -TimeoutSec 120
            Set-Content -NoNewline $stamp $latest.commit
            Write-Host "Downloaded $name (simulated at $($latest.commit.Substring(0, 7)))"
        }
        $named["$k-$label"] = $target
    }
}

$branch = git -C $repo rev-parse --abbrev-ref HEAD
Write-Host ""
Write-Host "Done, for branch $branch. Logs: $logs"
if ($layoutChanged) { Write-Host "The layout changed: in AdvantageScope do File > Import Layout... > $layout" }
Write-Host "Restart AdvantageScope if it was open (it reads the assets at start)."

# 7. -Open: start AdvantageScope on a log (a key from the table, or a file).
if ($Open) {
    $file = $Open
    if ($named.ContainsKey("$Open-best")) { $file = $named[$(if ($Typical) { "$Open-typical" } else { "$Open-best" })] }
    if (-not (Test-Path $file)) { throw "No log to open: $Open (keys: $($autos.Keys -join ', '), or a .wpilog path)." }
    $exe = Get-ChildItem (Join-Path $env:LOCALAPPDATA "Programs") -Recurse -Filter "AdvantageScope*.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $exe) { $exe = Get-ChildItem $env:ProgramFiles -Recurse -Filter "AdvantageScope*.exe" -ErrorAction SilentlyContinue | Select-Object -First 1 }
    if (-not $exe) { throw "AdvantageScope's exe not found under $env:LOCALAPPDATA\Programs or $env:ProgramFiles; open the log by hand: $file" }
    Get-Process -Name "AdvantageScope*" -ErrorAction SilentlyContinue | Stop-Process
    Start-Process -FilePath $exe.FullName -ArgumentList "`"$file`""
    Write-Host "Opened $file"
}
