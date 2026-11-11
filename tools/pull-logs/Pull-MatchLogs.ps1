<#
.SYNOPSIS
Copies the robot's match logs (.wpilog) off the Control Hub over Wi-Fi and opens the newest in
AdvantageScope. Run it once, or leave it running with -Watch during a meeting.

.DESCRIPTION
Every OpMode run writes /sdcard/FIRST/logs/<match>_<OpMode>.wpilog on the hub (RobotOpMode,
logging/MatchLog). This script:

  1. finds adb (Android Studio's copy) and AdvantageScope on this laptop,
  2. connects adb to the hub over Wi-Fi, fixing the stale/offline states TeamCode/README.md
     describes (Sloth Load's own "adb disconnect" drops the connection after every deploy),
  3. copies every .wpilog this laptop does not have yet, or whose size changed, into
     <Dest>\<robot network>\,
  4. opens the newest new file in AdvantageScope,
  5. sends today's logs to GitHub, one branch per day and robot (logs/<date>-<robot network>), so
     a Claude session can read them when asked to "pull logs". Only when GitHub answers: on the
     robot's Wi-Fi there is no internet, so they go up on the next run that has it (double-click
     again at home or school, robot or no robot). Your checkout is never touched: the branch is
     written with git's plumbing, not by switching branches.

A Driver Hub plugged into this laptop by USB is pulled too (never written to, never deleted
from): its Driver Station log and Android's battery history, into the same robot folder, so they
go up on the same logs/<date>-<robot network> branch. read_logs.py dsbattery reads them. The robot
cannot log the Driver Hub's battery itself: the Driver Station never sends it.

With -Watch it repeats every few seconds and opens a log each time it stops growing, which is
when the OpMode stopped. The workflow at the robot is then: stop the OpMode, look at the laptop.

Nothing here runs on the robot. A hub that is not reachable is reported, not retried, unless
-Watch is on.

.PARAMETER Robot
The hub's address. 192.168.43.1 is a Control Hub; a phone Robot Controller is 192.168.49.1.

.PARAMETER Dest
Where logs go. A folder per robot network is created inside it, so logs from 19429 and 20245 do
not mix. Default: the team's shared Google Drive folder, "Shared drives\Robotics Team 3572 -
Wavelength\2026-2027 FTC Biobuzz\Logs", on whichever drive letter Google Drive for Desktop
mounts (G: usually), so every pull is on Drive for the whole team as soon as the laptop is back
on the internet. A laptop without Drive falls back to %USERPROFILE%\biobuzz-logs.

.PARAMETER Watch
Keep running. Every -EverySeconds: reconnect if needed, pull what is new, open what has settled.
Ctrl+C stops it.

.PARAMETER NoOpen
Pull only; never start AdvantageScope.

.PARAMETER RemoveFromRobot
After a file is pulled and its size matches, delete it from the hub. The newest file is never
deleted (it may still be being written). Off by default: the hub has room for a season of logs.

.PARAMETER NoPublish
Do not send logs to GitHub (and so remove nothing from the robot).

.PARAMETER KeepOnRobot
Leave logs on the robot after they reach GitHub. By default a log is deleted from the hub once its
upload to GitHub has succeeded (it is then on Drive and on GitHub), so the hub holds only what has
not gone up yet and each pull stays small. The newest log is never deleted: it may still be
being written.

.PARAMETER PublishDays
Logs from this many days before today (by the date in the file name) are sent to GitHub too.
Default 0: today's only. Older logs are already on Drive, and sending them costs minutes.

.PARAMETER DriverHub
A Driver Hub to pull from: its adb serial, or host[:port] if its adb listens on Wi-Fi. By default
every device adb lists that holds the Driver Station app's log (in practice, a Driver Hub on USB).

.PARAMETER NoDriverHub
Do not look for a Driver Hub.

.PARAMETER RobotNetwork
The robot folder a Driver Hub's files go in when neither it nor this laptop is on a robot's
network (pulled at home after an event), e.g. 20245-RC. Otherwise: the Driver Hub's own network,
if it is a robot's, else this laptop's.

.PARAMETER Adb
The adb.exe to use. By default: the one behind an adb server that is already running (so a
laptop that talks to the hub through REV Hardware Client keeps using that adb), else Android
Studio's, else the one on PATH.

.EXAMPLE
.\Pull-MatchLogs.ps1
Pull everything new and open the newest log.

.EXAMPLE
.\Pull-MatchLogs.ps1 -Watch
At the start of a meeting. Leave the window open.
#>
[CmdletBinding()]
param(
    [string]$Robot = '192.168.43.1',
    [int]$Port = 5555,
    [string]$Dest,
    [switch]$Watch,
    [int]$EverySeconds = 5,
    [switch]$NoOpen,
    [switch]$RemoveFromRobot,
    [switch]$NoPublish,
    [switch]$KeepOnRobot,
    [int]$PublishDays = 0,
    [string]$DriverHub,
    [switch]$NoDriverHub,
    [string]$RobotNetwork,
    [string]$Adb,
    [string]$AdvantageScope
)

Set-StrictMode -Version 2
$ErrorActionPreference = 'Continue'

$RemoteFolder = '/sdcard/FIRST/logs'
# The Driver Station app's own log (RobotLog in SDK 12.0: "logcat -f <sdcard>/driverStationLog.txt
# -r4096 -n4"): this file plus .1 (newer) to .4 (oldest), 4 MB each, so about 20 MB of history.
$DsLogRemote = '/sdcard/driverStationLog.txt'
# The team's folder on Google Drive, relative to a drive root. Drive for Desktop mounts shared
# drives under "<letter>:\Shared drives\"; the letter is per laptop, so every drive is tried.
$SharedDriveLogs = 'Shared drives\Robotics Team 3572 - Wavelength\2026-2027 FTC Biobuzz\Logs'
$LocalLogs = Join-Path $env:USERPROFILE 'biobuzz-logs'
$Target = "${Robot}:${Port}"

function Say($text) {
    Write-Host ("[{0}] {1}" -f (Get-Date -Format 'HH:mm:ss'), $text)
}

# ------------------------------------------------------------------ find the tools

function Find-Adb {
    if ($Adb) { return $Adb }
    # An adb server is already running: use the very binary that started it. Two different adb
    # builds on one laptop (Android Studio's, REV Hardware Client's) each kill the other's server
    # on sight ("adb server version doesn't match this client"), and that fight is what makes a
    # hub flap between connected and offline. Whichever one is live owns the hub for now.
    # One exception: a Microsoft Store app's adb (REV Hardware Client 2 from the Store lives under
    # C:\Program Files\WindowsApps) cannot be run by anything outside the app ("Access is denied").
    # Its server is still the one to talk to, and any adb client of the same version does that, so
    # fall through to Android Studio's binary.
    $running = Get-Process adb -ErrorAction SilentlyContinue |
        Where-Object { $_.Path -and $_.Path -notlike '*\WindowsApps\*' } | Select-Object -First 1
    if ($running -and (Test-Path $running.Path)) {
        $usable = $false
        try {
            $ErrorActionPreference = 'Stop'   # a launch failure ("Access is denied") must be caught, not printed
            & $running.Path version 2>&1 | Out-Null
            $usable = ($LASTEXITCODE -eq 0)
        } catch {
        } finally {
            $ErrorActionPreference = 'Continue'
        }
        if ($usable) { return $running.Path }
    }
    $studio = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
    if (Test-Path $studio) { return $studio }
    $onPath = Get-Command adb -ErrorAction SilentlyContinue
    if ($onPath) { return $onPath.Source }
    throw "adb not found. Install Android Studio's SDK platform-tools, or pass -Adb <path to adb.exe>."
}

# Where logs go when -Dest is not given: the team's Google Drive folder if this laptop has it,
# else a local folder. The Drive folder is only used if it already exists, so a laptop that has
# Drive but is not a member of the shared drive, or has it on another letter, still gets its logs.
function Find-Dest {
    if ($Dest) { return $Dest }
    foreach ($drive in (Get-PSDrive -PSProvider FileSystem)) {
        $candidate = Join-Path $drive.Root $SharedDriveLogs
        if (Test-Path -LiteralPath $candidate -PathType Container) { return $candidate }
    }
    return $LocalLogs
}

function Find-AdvantageScope {
    if ($AdvantageScope) { return $AdvantageScope }
    $usual = Join-Path $env:LOCALAPPDATA 'Programs\advantagescope\AdvantageScope.exe'
    if (Test-Path $usual) { return $usual }
    return $null
}

# Runs adb and returns its output lines (stdout and stderr together) as strings.
function Invoke-Adb {
    param([string[]]$Arguments)
    $out = & $script:AdbExe @Arguments 2>&1 | ForEach-Object { "$_" }
    return @($out)
}

# ------------------------------------------------------------------ connect

# True if something answers on the hub's adb port within a second and a half. Cheaper and more
# honest than "adb connect", which hangs for a while on a hub that is not there.
function Test-Reachable {
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $async = $client.BeginConnect($Robot, $Port, $null, $null)
        if (-not $async.AsyncWaitHandle.WaitOne(1500, $false)) { return $false }
        $client.EndConnect($async)
        return $true
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

# The state adb has for our hub: 'device', 'offline', 'unauthorized' or '' (not listed).
function Get-AdbState {
    $pattern = '^\s*' + [regex]::Escape($Target) + '\s+(\S+)'
    foreach ($line in (Invoke-Adb @('devices'))) {
        if ($line -match $pattern) { return $Matches[1] }
    }
    return ''
}

# Gets adb to a 'device' connection with the hub, or returns $false with the reason said.
function Connect-Robot {
    $state = Get-AdbState
    if ($state -eq 'device') { return $true }
    if (-not (Test-Reachable)) {
        Say "Nothing answers at $Target. Is this laptop on the robot's Wi-Fi?"
        return $false
    }
    if ($state -ne '') {
        # 'offline' is adb holding a dead connection from before the hub rebooted or the laptop
        # slept. Drop just this one; a bare "adb disconnect" would drop a USB robot too.
        Invoke-Adb @('disconnect', $Target) | Out-Null
    }
    $reply = (Invoke-Adb @('connect', $Target)) -join ' '
    if ($reply -match "doesn't match this client") {
        # Two copies of adb fighting (README: "Two copies of adb"). Restart ours and try once more.
        Say 'adb server version mismatch; restarting the adb server.'
        Invoke-Adb @('kill-server') | Out-Null
        $reply = (Invoke-Adb @('connect', $Target)) -join ' '
    }
    if ($reply -notmatch 'connected to') {
        Say "adb could not connect: $reply"
        return $false
    }
    # "connected to" is adb's word for "the socket opened". After a Wi-Fi drop the hub's adb
    # daemon can accept the socket and still list the device as offline, in which case nothing
    # works until it recovers (or the hub reboots). Say so once; do not claim a connection.
    if ((Get-AdbState) -ne 'device') {
        Say "the hub answers but adb lists it as '$(Get-AdbState)'; waiting for it to recover."
        return $false
    }
    Say "adb connected to $Target."
    return $true
}

# ------------------------------------------------------------------ list, pull, remove

# The .wpilog files on the hub: name and size. Toybox "ls -l" prints
#   -rw-rw---- 1 root sdcard_rw 123456 2026-10-25 14:32 2026-10-25_14-32-10_Auto_R-Simple.wpilog
function Get-RobotLogs {
    $files = @()
    $lines = Invoke-Adb @('-s', $Target, 'shell', "ls -l $RemoteFolder 2>/dev/null")
    foreach ($line in $lines) {
        if ($line -match '^\S+\s+\d+\s+\S+\s+\S+\s+(\d+)\s+\S+\s+\S+\s+(\S+\.wpilog)\s*$') {
            $files += [pscustomobject]@{ Name = $Matches[2]; Size = [long]$Matches[1] }
        }
    }
    return @($files | Sort-Object Name)
}

# The robot's Wi-Fi network, as a folder name: the robot's network is named after the robot, so
# logs from the two robots land in two folders. A laptop with two adapters (one on the robot,
# one on the building's Wi-Fi) is labelled by the adapter that reaches the hub, not the first
# one netsh lists. 'robot' if the hub is reached some other way (USB, Ethernet) or nothing can
# be read.
function Get-RobotLabel {
    try {
        $alias = $null
        try {
            $route = Find-NetRoute -RemoteIPAddress $Robot -ErrorAction Stop | Select-Object -First 1
            if ($route) { $alias = $route.InterfaceAlias }
        } catch { }
        $name = $null
        $firstSsid = $null
        $ssidByName = @{}
        foreach ($line in (& netsh wlan show interfaces 2>$null)) {
            if ($line -match '^\s*Name\s*:\s*(.+?)\s*$') {
                $name = $Matches[1]
            } elseif ($line -match '^\s*SSID\s*:\s*(.+?)\s*$') {
                if (-not $firstSsid) { $firstSsid = $Matches[1] }
                if ($name) { $ssidByName[$name] = $Matches[1] }
            }
        }
        $ssid = $null
        if ($alias) {
            if ($ssidByName.ContainsKey($alias)) { $ssid = $ssidByName[$alias] }
        } else {
            $ssid = $firstSsid
        }
        if ($ssid) { return ($ssid -replace '[^A-Za-z0-9._-]', '_') }
    } catch { }
    return 'robot'
}

# Copies every file the laptop lacks, or holds at a different size. Returns the local paths pulled.
function Sync-Logs {
    param([object[]]$Remote, [string]$Folder)
    New-Item -ItemType Directory -Force -Path $Folder | Out-Null
    $pulled = @()
    foreach ($f in $Remote) {
        $local = Join-Path $Folder $f.Name
        if ((Test-Path $local) -and ((Get-Item $local).Length -eq $f.Size)) { continue }
        $reply = (Invoke-Adb @('-s', $Target, 'pull', "$RemoteFolder/$($f.Name)", $local)) -join ' '
        if ((Test-Path $local) -and ((Get-Item $local).Length -eq $f.Size)) {
            Say ("pulled {0} ({1:N0} KB)" -f $f.Name, ($f.Size / 1KB))
            $pulled += $local
        } else {
            Say "pull of $($f.Name) did not complete: $reply"
        }
    }
    return @($pulled)
}

# Deletes from the hub every file (but the newest) that is already on GitHub at this very size: in
# the published list under this robot's folder. A log that failed to go up, or grew since, stays.
function Remove-PublishedFromRobot {
    param([object[]]$Remote, [string]$Label)
    if ($NoPublish -or $KeepOnRobot -or $Remote.Count -lt 2) { return }
    if (-not (Test-Path -LiteralPath $PublishedList)) { return }
    $done = @{}
    foreach ($line in (Get-Content -LiteralPath $PublishedList)) { $done[$line] = $true }
    $removed = 0
    foreach ($f in $Remote[0..($Remote.Count - 2)]) {
        if (-not $done.ContainsKey("$Label/$($f.Name)/$($f.Size)")) { continue }
        Invoke-Adb @('-s', $Target, 'shell', "rm '$RemoteFolder/$($f.Name)'") | Out-Null
        $removed++
    }
    if ($removed -gt 0) { Say "removed $removed log(s) from the robot: they are on Drive and on GitHub." }
}

# Deletes from the hub every file (but the newest) whose local copy has the same size.
function Remove-PulledFromRobot {
    param([object[]]$Remote, [string]$Folder)
    if ($Remote.Count -lt 2) { return }
    foreach ($f in $Remote[0..($Remote.Count - 2)]) {
        $local = Join-Path $Folder $f.Name
        if ((Test-Path $local) -and ((Get-Item $local).Length -eq $f.Size)) {
            Invoke-Adb @('-s', $Target, 'shell', "rm $RemoteFolder/$($f.Name)") | Out-Null
            Say "removed $($f.Name) from the robot (local copy matches)."
        }
    }
}

function Open-Log {
    param([string]$Path)
    if ($NoOpen -or -not $Path) { return }
    $exe = $script:AsExe
    if ($exe) {
        Say "opening in AdvantageScope: $Path"
        # AdvantageScope is a Chromium app. A second copy started while one is open logs a burst
        # of "Unable to move the cache: Access is denied" to stderr before handing the file to the
        # first copy. Harmless, but it buries the watcher's own lines, so it goes to a file.
        $noise = Join-Path $env:TEMP 'advantagescope-launch.log'
        Start-Process -FilePath $exe -ArgumentList "`"$Path`"" -RedirectStandardError $noise | Out-Null
    } else {
        Say "AdvantageScope not found at the usual place; open this by hand: $Path"
    }
}

# ------------------------------------------------------------------ the Driver Hub

# The Driver Hubs adb can see: every device in the 'device' state, other than the Control Hub,
# whose storage holds the Driver Station app's log. With -DriverHub, that one only.
function Get-DriverHubSerials {
    if ($DriverHub) {
        $serial = $DriverHub
        if ($serial -match '^\d+\.\d+\.\d+\.\d+$') { $serial = "${serial}:5555" }
        if ($serial -match ':\d+$') {
            # A Driver Hub on Wi-Fi: connect it the way the hub is connected.
            $reply = (Invoke-Adb @('connect', $serial)) -join ' '
            if ($reply -notmatch 'connected to') { Say "Driver Hub ${serial}: adb could not connect: $reply"; return @() }
        }
        return @($serial)
    }
    $candidates = @()
    foreach ($line in (Invoke-Adb @('devices'))) {
        if ($line -match '^(\S+)\s+device\s*$' -and $Matches[1] -ne $Target) { $candidates += $Matches[1] }
    }
    $found = @()
    foreach ($serial in $candidates) {
        $probe = (Invoke-Adb @('-s', $serial, 'shell', "ls $DsLogRemote 2>/dev/null")) -join ''
        if ($probe -match 'driverStationLog') { $found += $serial }
    }
    return @($found)
}

# Which robot folder a Driver Hub's files go in: -RobotNetwork; else the network the Driver Hub is
# on, if it is a robot's ("<team>-RC", "<team>-<letter>-RC"); else the laptop's robot folder; else
# DriverHub (and it says so).
function Get-DriverHubLabel {
    param([string]$Serial, [string]$Fallback)
    if ($RobotNetwork) { return ($RobotNetwork -replace '[^A-Za-z0-9._-]', '_') }
    foreach ($line in (Invoke-Adb @('-s', $Serial, 'shell', 'dumpsys wifi | grep -m 1 "mWifiInfo SSID"'))) {
        if ($line -match 'SSID:\s*"?([^",]+?)"?\s*,') {
            $ssid = $Matches[1]
            if ($ssid -match '-RC$') { return ($ssid -replace '[^A-Za-z0-9._-]', '_') }
        }
    }
    if ($Fallback -and $Fallback -ne 'robot') { return $Fallback }
    Say 'Driver Hub: not on a robot network and neither is this laptop; its files go in DriverHub (pass -RobotNetwork 20245-RC to file them with that robot).'
    return 'DriverHub'
}

# Copies one Driver Hub's log and battery history into its robot folder, as two files a day:
#   <date>_DriverHub-<serial>.dslog.txt    that day's lines of the Driver Station log, merged with
#                                          what earlier pulls kept (the hub's own log rolls over)
#   <date>_DriverHub-<serial>.battery.txt  today's: one snapshot per pull of "dumpsys battery" and
#                                          "dumpsys batterystats --history" (Android's record of
#                                          every level change since the last full charge)
# Nothing on the Driver Hub is changed or deleted.
function Sync-DriverHub {
    param([string]$Serial, [string]$Root, [string]$Fallback)
    $label = Get-DriverHubLabel -Serial $Serial -Fallback $Fallback
    $hubId = ((Invoke-Adb @('-s', $Serial, 'shell', 'getprop ro.serialno')) -join '').Trim()
    if (-not $hubId) { $hubId = $Serial }
    $hubId = $hubId -replace '[^A-Za-z0-9._-]', '_'
    $folder = Join-Path $Root $label
    New-Item -ItemType Directory -Force -Path $folder | Out-Null

    $tmp = Join-Path $env:TEMP ('biobuzz-ds-' + [guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Force -Path $tmp | Out-Null
    try {
        # Oldest first: logcat's -r moves the file to .1, .1 to .2 and so on, so .4 is the oldest.
        $lines = New-Object System.Collections.Generic.List[string]
        foreach ($suffix in @('.4', '.3', '.2', '.1', '')) {
            $local = Join-Path $tmp "ds$suffix.txt"
            Invoke-Adb @('-s', $Serial, 'pull', "$DsLogRemote$suffix", $local) | Out-Null
            if (Test-Path -LiteralPath $local) { $lines.AddRange([string[]][System.IO.File]::ReadAllLines($local)) }
        }
        if ($lines.Count -eq 0) { Say "Driver Hub ${hubId}: no Driver Station log could be pulled." }

        # logcat's "threadtime" lines start "MM-dd HH:mm:ss.fff": no year, so the day is the laptop's.
        # New lines are appended after what earlier pulls kept, oldest file first, so the file stays
        # in time order without sorting (read_logs.py sorts its samples anyway).
        for ($back = 0; $back -le $PublishDays; $back++) {
            $day = (Get-Date).Date.AddDays(-$back)
            $prefix = $day.ToString('MM-dd') + ' '
            $path = Join-Path $folder ("{0}_DriverHub-{1}.dslog.txt" -f $day.ToString('yyyy-MM-dd'), $hubId)
            $kept = New-Object System.Collections.Generic.List[string]
            $seen = New-Object System.Collections.Generic.HashSet[string]
            if (Test-Path -LiteralPath $path) {
                foreach ($l in [System.IO.File]::ReadAllLines($path)) { if ($seen.Add($l)) { $kept.Add($l) } }
            }
            $before = $kept.Count
            foreach ($l in $lines) {
                if ($l.StartsWith($prefix) -and $seen.Add($l)) { $kept.Add($l) }
            }
            if ($kept.Count -eq $before) { continue }
            [System.IO.File]::WriteAllLines($path, [string[]]$kept.ToArray())
            Say ("Driver Hub {0}: {1} new Driver Station log line(s) for {2} in {3}" -f $hubId, ($kept.Count - $before), $day.ToString('yyyy-MM-dd'), $label)
        }
    } finally {
        Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue
    }

    $now = Get-Date
    $stats = @(Invoke-Adb @('-s', $Serial, 'shell', 'dumpsys battery; dumpsys batterystats --history'))
    $path = Join-Path $folder ("{0}_DriverHub-{1}.battery.txt" -f $now.ToString('yyyy-MM-dd'), $hubId)
    # read_logs.py starts a snapshot at a line beginning "=== <date> <time>": keep that shape.
    $header = "=== {0} Driver Hub {1}: dumpsys battery; dumpsys batterystats --history ===" -f $now.ToString('yyyy-MM-dd HH:mm:ss', [Globalization.CultureInfo]::InvariantCulture), $hubId
    Add-Content -LiteralPath $path -Value (@($header) + $stats)
    $level = ($stats | Where-Object { $_ -match '^\s+level:\s*\d+' } | Select-Object -First 1)
    if ($level) { $level = ($level -replace '\D', '') + ' %' } else { $level = 'level unknown' }
    Say "Driver Hub ${hubId}: battery $level now; battery history saved in $label."
}

# Every Driver Hub adb can see. Never fails the pull: anything wrong is said and the pull goes on.
function Sync-DriverHubs {
    param([string]$Root, [string]$Fallback)
    if ($NoDriverHub) { return }
    $serials = @(Get-DriverHubSerials)
    if ($serials.Count -eq 0) {
        if ($DriverHub) { Say "Driver Hub $DriverHub does not answer." }
        return
    }
    foreach ($serial in $serials) {
        try {
            Sync-DriverHub -Serial $serial -Root $Root -Fallback $Fallback
        } catch {
            Say "Driver Hub ${serial}: could not pull its log: $_"
        }
    }
}

# ------------------------------------------------------------------ send to GitHub

# Which logs have gone up already: "<robot folder>/<file name>/<size>", one per line. A log that
# grew (an Auto, then its TeleOp) has a new size and goes up again, replacing the old copy.
$PublishedList = Join-Path $env:LOCALAPPDATA 'biobuzz-logs-published.txt'
# GitHub refuses a file over 100 MB. Logs go up gzipped (about a third of the size), so this is
# the compressed size; a whole afternoon's LaunchTest (over 95 MB raw on 1 Nov) fits.
$MaxPublishBytes = 95MB

# A gzip copy of $Source at $Target. .NET's own GZipStream: nothing to install.
function New-GzipCopy {
    param([string]$Source, [string]$Target)
    $in = [System.IO.File]::OpenRead($Source)
    $out = [System.IO.File]::Create($Target)
    $gz = New-Object System.IO.Compression.GZipStream($out, [System.IO.Compression.CompressionLevel]::Optimal)
    try { $in.CopyTo($gz) } finally { $gz.Dispose(); $out.Dispose(); $in.Dispose() }
}

# Runs git in the repo and returns its output lines; $LASTEXITCODE says how it went.
function Invoke-Git {
    param([string[]]$Arguments)
    $out = & git -C $script:Repo @Arguments 2>&1 | ForEach-Object { "$_" }
    return @($out)
}

# Sends every recent log not sent yet to its day's branch, logs/<date>-<robot folder>, adding to
# what the branch already holds. Each robot folder under $Dest is its own robot, whatever network
# this laptop is on now. Never fails the pull: anything wrong is said and left for the next run.
function Publish-Logs {
    param([string]$Root)
    if ($NoPublish) { return }
    if (-not (Get-Command git -ErrorAction SilentlyContinue)) { Say 'git not found: logs not sent to GitHub.'; return }
    $top = & git -C $PSScriptRoot rev-parse --show-toplevel 2>$null
    if ($LASTEXITCODE -ne 0 -or -not $top) { Say 'this script is not inside the biobuzz repo: logs not sent to GitHub.'; return }
    $script:Repo = "$top".Trim()

    $done = @{}
    if (Test-Path -LiteralPath $PublishedList) {
        foreach ($line in (Get-Content -LiteralPath $PublishedList)) { $done[$line] = $true }
    }
    $since = (Get-Date).Date.AddDays(-$PublishDays).ToString('yyyy-MM-dd')
    $todo = @()
    foreach ($dir in @(Get-ChildItem -LiteralPath $Root -Directory -ErrorAction SilentlyContinue)) {
        # Match logs, and a Driver Hub's log and battery history (Sync-DriverHub).
        $files = @(Get-ChildItem -LiteralPath $dir.FullName -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like '*.wpilog' -or $_.Name -like '*.dslog.txt' -or $_.Name -like '*.battery.txt' })
        foreach ($f in $files) {
            if ($f.Name.Length -lt 10 -or $f.Name.Substring(0, 10) -lt $since) { continue }
            $key = "$($dir.Name)/$($f.Name)/$($f.Length)"
            if ($done.ContainsKey($key)) { continue }
            $todo += [pscustomobject]@{ Robot = $dir.Name; File = $f; Key = $key; Day = $f.Name.Substring(0, 10) }
        }
    }
    if ($todo.Count -eq 0) { return }

    # On the robot's Wi-Fi this fails fast: nothing is lost, the next run with internet sends them.
    Invoke-Git @('ls-remote', '--exit-code', 'origin', 'HEAD') | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Say "$($todo.Count) log(s) not on GitHub yet (no internet now); the next run that has it sends them."
        return
    }

    $index = Join-Path $env:TEMP ('biobuzz-logs-index-' + [guid]::NewGuid().ToString('N'))
    try {
        foreach ($group in ($todo | Group-Object { "$($_.Day)-$($_.Robot)" })) {
            $branch = "logs/$($group.Name)"
            # What the branch holds already, if it exists: the new logs are added beside it.
            Invoke-Git @('fetch', '--quiet', 'origin', "+refs/heads/${branch}:refs/remotes/origin/${branch}") | Out-Null
            $parent = $null
            if ($LASTEXITCODE -eq 0) { $parent = ((Invoke-Git @('rev-parse', "refs/remotes/origin/${branch}")) -join '').Trim() }

            # A private index, so the checkout's own index and files are never touched.
            $env:GIT_INDEX_FILE = $index
            if ($parent) { Invoke-Git @('read-tree', $parent) | Out-Null } else { Invoke-Git @('read-tree', '--empty') | Out-Null }
            $ok = ($LASTEXITCODE -eq 0)
            $sent = @()
            foreach ($item in $group.Group) {
                if (-not $ok) { break }
                # Gzipped: a third of the size to push, and under GitHub's 100 MB for a long session.
                $gzPath = Join-Path $env:TEMP ($item.File.Name + '.gz')
                try { New-GzipCopy -Source $item.File.FullName -Target $gzPath } catch { Say "could not compress $($item.File.Name): $_"; continue }
                if ((Get-Item -LiteralPath $gzPath).Length -gt $MaxPublishBytes) {
                    Say "$($item.File.Name) is over 95 MB even compressed, too big for GitHub: not sent."
                    Remove-Item -LiteralPath $gzPath -Force -ErrorAction SilentlyContinue
                    continue
                }
                $blob = ((Invoke-Git @('hash-object', '-w', '--', $gzPath)) -join '').Trim()
                $hashed = ($LASTEXITCODE -eq 0)
                Remove-Item -LiteralPath $gzPath -Force -ErrorAction SilentlyContinue
                if (-not $hashed) { $ok = $false; break }
                Invoke-Git @('update-index', '--add', '--cacheinfo', "100644,$blob,$($item.File.Name).gz") | Out-Null
                if ($LASTEXITCODE -ne 0) { $ok = $false } else { $sent += $item }
            }
            if ($ok -and $sent.Count -eq 0) {
                Remove-Item Env:\GIT_INDEX_FILE -ErrorAction SilentlyContinue
                Remove-Item -LiteralPath $index -Force -ErrorAction SilentlyContinue
                continue
            }
            $tree = $null
            if ($ok) { $tree = ((Invoke-Git @('write-tree')) -join '').Trim(); $ok = ($LASTEXITCODE -eq 0) }
            Remove-Item Env:\GIT_INDEX_FILE -ErrorAction SilentlyContinue
            Remove-Item -LiteralPath $index -Force -ErrorAction SilentlyContinue
            if (-not $ok) { Say "could not build $branch; the next run tries again."; continue }

            $message = "Logs: $($group.Name) ($($sent.Count) file(s), gzipped, from tools/pull-logs; throwaway, never merge)"
            $commitArgs = @('commit-tree', $tree, '-m', $message)
            if ($parent) { $commitArgs += @('-p', $parent) }
            $commit = ((Invoke-Git $commitArgs) -join '').Trim()
            if ($LASTEXITCODE -ne 0) { Say "could not commit $branch (is git's user.name/user.email set?)."; continue }
            $reply = (Invoke-Git @('push', '--quiet', 'origin', "${commit}:refs/heads/${branch}")) -join ' '
            if ($LASTEXITCODE -ne 0) { Say "could not push ${branch}: $reply"; continue }
            Add-Content -LiteralPath $PublishedList -Value @($sent | ForEach-Object { $_.Key })
            Say "sent $($sent.Count) log(s) to GitHub branch $branch"
        }
    } finally {
        Remove-Item Env:\GIT_INDEX_FILE -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $index -Force -ErrorAction SilentlyContinue
    }
}

# ------------------------------------------------------------------ main

$script:AdbExe = Find-Adb
$script:AsExe = Find-AdvantageScope
$Dest = Find-Dest
$Folder = Join-Path $Dest (Get-RobotLabel)
Say "adb: $script:AdbExe"
if ($script:AsExe) { Say "AdvantageScope: $script:AsExe" } else { Say 'AdvantageScope: not found (logs are still pulled)' }
if ($Dest -eq $LocalLogs) {
    Say "logs go to $Folder (this laptop only: the team's Google Drive folder was not found)"
} else {
    Say "logs go to $Folder"
}

# A Driver Hub on USB first: it needs no robot, so this also works at home after an event.
Sync-DriverHubs -Root $Dest -Fallback (Split-Path -Leaf $Folder)

if (-not $Watch) {
    # No robot is fine: this is also how logs pulled on the robot's Wi-Fi reach GitHub later.
    if (-not (Connect-Robot)) {
        Publish-Logs -Root $Dest
        exit 0
    }
    $remote = @(Get-RobotLogs)
    if ($remote.Count -eq 0) {
        Say "No .wpilog files in $RemoteFolder on the robot. Has a RobotOpMode run since the install?"
        Publish-Logs -Root $Dest
        exit 0
    }
    $pulled = @(Sync-Logs -Remote $remote -Folder $Folder)
    if ($RemoveFromRobot) { Remove-PulledFromRobot -Remote $remote -Folder $Folder }
    if ($pulled.Count -eq 0) {
        Say "Nothing new: all $($remote.Count) logs are already here."
    } else {
        Open-Log ($pulled | Sort-Object | Select-Object -Last 1)
    }
    Publish-Logs -Root $Dest
    Remove-PublishedFromRobot -Remote $remote -Label (Split-Path -Leaf $Folder)
    exit 0
}

# Whatever an earlier run pulled without internet goes up now, if this laptop has it.
Publish-Logs -Root $Dest

# Watch mode. A log is opened once it has settled: it was pulled, and its size on the hub has not
# changed for a tick. That is the moment the OpMode stopped (or an Autonomous paused it for
# TeleOp; the file settles again after TeleOp, and opens again with the whole match in it).
Say "Watching $Target every $EverySeconds s. Ctrl+C to stop."
$lastSize = @{}        # name -> size on the hub last tick
$changed = @{}         # name -> $true while a file has grown since it was last opened
$connected = $false
$first = $true
$failures = 0          # consecutive ticks without the hub; the retry interval grows with it
while ($true) {
    if (Connect-Robot) {
        if (-not $connected) { Say 'connected.'; $connected = $true }
        $failures = 0
        $remote = @(Get-RobotLogs)
        $pulled = @(Sync-Logs -Remote $remote -Folder $Folder)
        if ($RemoveFromRobot) { Remove-PulledFromRobot -Remote $remote -Folder $Folder }
        $settled = $false
        foreach ($f in $remote) {
            $prev = -1
            if ($lastSize.ContainsKey($f.Name)) { $prev = $lastSize[$f.Name] }
            if ($f.Size -ne $prev) {
                # Growing (or new). Opening the backlog found on the first tick would flood the
                # screen, so only files that change while we watch count.
                if (-not $first) { $changed[$f.Name] = $true }
            } elseif ($changed.ContainsKey($f.Name)) {
                $changed.Remove($f.Name)
                Open-Log (Join-Path $Folder $f.Name)
                $settled = $true
            }
            $lastSize[$f.Name] = $f.Size
        }
        # A log just finished: up to GitHub with it, if this laptop has internet too (a second
        # Wi-Fi adapter); otherwise it waits for a later run.
        if ($settled) {
            Sync-DriverHubs -Root $Dest -Fallback (Split-Path -Leaf $Folder)
            Publish-Logs -Root $Dest
            Remove-PublishedFromRobot -Remote $remote -Label (Split-Path -Leaf $Folder)
        }
        if ($first) { Say "$($remote.Count) logs on the robot; new ones open as they finish." }
        $first = $false
    } else {
        if ($connected) { Say 'lost the robot; will keep trying.' }
        $connected = $false
        $failures++
    }
    # While the hub is away, retry less and less often (up to a minute) rather than hammering
    # its adb daemon every tick while it is trying to come back.
    $wait = $EverySeconds * [Math]::Min([Math]::Pow(2, $failures), 12)
    Start-Sleep -Seconds ([int][Math]::Min($wait, 60))
}
