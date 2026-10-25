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
  4. opens the newest new file in AdvantageScope.

With -Watch it repeats every few seconds and opens a log each time it stops growing, which is
when the OpMode stopped. The workflow at the robot is then: stop the OpMode, look at the laptop.

Nothing here runs on the robot. A hub that is not reachable is reported, not retried, unless
-Watch is on.

.PARAMETER Robot
The hub's address. 192.168.43.1 is a Control Hub; a phone Robot Controller is 192.168.49.1.

.PARAMETER Dest
Where logs go on this laptop. A folder per robot network is created inside it, so logs from
19429 and 20245 do not mix. Default: %USERPROFILE%\biobuzz-logs.

.PARAMETER Watch
Keep running. Every -EverySeconds: reconnect if needed, pull what is new, open what has settled.
Ctrl+C stops it.

.PARAMETER NoOpen
Pull only; never start AdvantageScope.

.PARAMETER RemoveFromRobot
After a file is pulled and its size matches, delete it from the hub. The newest file is never
deleted (it may still be being written). Off by default: the hub has room for a season of logs.

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
    [string]$Dest = (Join-Path $env:USERPROFILE 'biobuzz-logs'),
    [switch]$Watch,
    [int]$EverySeconds = 5,
    [switch]$NoOpen,
    [switch]$RemoveFromRobot,
    [string]$Adb,
    [string]$AdvantageScope
)

Set-StrictMode -Version 2
$ErrorActionPreference = 'Continue'

$RemoteFolder = '/sdcard/FIRST/logs'
$Target = "${Robot}:${Port}"

function Say($text) {
    Write-Host ("[{0}] {1}" -f (Get-Date -Format 'HH:mm:ss'), $text)
}

# ------------------------------------------------------------------ find the tools

function Find-Adb {
    if ($Adb) { return $Adb }
    $studio = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
    if (Test-Path $studio) { return $studio }
    $onPath = Get-Command adb -ErrorAction SilentlyContinue
    if ($onPath) { return $onPath.Source }
    throw "adb not found. Install Android Studio's SDK platform-tools, or pass -Adb <path to adb.exe>."
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
    if ($reply -match 'connected to') {
        Say "adb connected to $Target."
        return $true
    }
    if ($reply -match "doesn't match this client") {
        # Two copies of adb fighting (README: "Two copies of adb"). Restart ours and try once more.
        Say 'adb server version mismatch; restarting the adb server.'
        Invoke-Adb @('kill-server') | Out-Null
        $reply = (Invoke-Adb @('connect', $Target)) -join ' '
        if ($reply -match 'connected to') { return $true }
    }
    Say "adb could not connect: $reply"
    return $false
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

# The Wi-Fi network this laptop is on, as a folder name: the robot's network is named after the
# robot, so logs from the two robots land in two folders. 'robot' if it cannot be read (USB).
function Get-RobotLabel {
    try {
        foreach ($line in (& netsh wlan show interfaces 2>$null)) {
            if ($line -match '^\s*SSID\s*:\s*(.+?)\s*$') {
                return ($Matches[1] -replace '[^A-Za-z0-9._-]', '_')
            }
        }
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
    if ($NoOpen) { return }
    $exe = $script:AsExe
    if ($exe) {
        Say "opening in AdvantageScope: $Path"
        Start-Process -FilePath $exe -ArgumentList "`"$Path`"" | Out-Null
    } else {
        Say "AdvantageScope not found at the usual place; open this by hand: $Path"
    }
}

# ------------------------------------------------------------------ main

$script:AdbExe = Find-Adb
$script:AsExe = Find-AdvantageScope
$Folder = Join-Path $Dest (Get-RobotLabel)
Say "adb: $script:AdbExe"
if ($script:AsExe) { Say "AdvantageScope: $script:AsExe" } else { Say 'AdvantageScope: not found (logs are still pulled)' }
Say "logs go to $Folder"

if (-not $Watch) {
    if (-not (Connect-Robot)) { exit 1 }
    $remote = Get-RobotLogs
    if ($remote.Count -eq 0) {
        Say "No .wpilog files in $RemoteFolder on the robot. Has a RobotOpMode run since the install?"
        exit 0
    }
    $pulled = Sync-Logs -Remote $remote -Folder $Folder
    if ($RemoveFromRobot) { Remove-PulledFromRobot -Remote $remote -Folder $Folder }
    if ($pulled.Count -eq 0) {
        Say "Nothing new: all $($remote.Count) logs are already here."
        exit 0
    }
    Open-Log ($pulled | Sort-Object | Select-Object -Last 1)
    exit 0
}

# Watch mode. A log is opened once it has settled: it was pulled, and its size on the hub has not
# changed for a tick. That is the moment the OpMode stopped (or an Autonomous paused it for
# TeleOp; the file settles again after TeleOp, and opens again with the whole match in it).
Say "Watching $Target every $EverySeconds s. Ctrl+C to stop."
$lastSize = @{}        # name -> size on the hub last tick
$changed = @{}         # name -> $true while a file has grown since it was last opened
$connected = $false
$first = $true
while ($true) {
    if (Connect-Robot) {
        if (-not $connected) { Say 'connected.'; $connected = $true }
        $remote = Get-RobotLogs
        $pulled = Sync-Logs -Remote $remote -Folder $Folder
        if ($RemoveFromRobot) { Remove-PulledFromRobot -Remote $remote -Folder $Folder }
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
            }
            $lastSize[$f.Name] = $f.Size
        }
        if ($first) { Say "$($remote.Count) logs on the robot; new ones open as they finish." }
        $first = $false
    } elseif ($connected) {
        Say 'lost the robot; will keep trying.'
        $connected = $false
    }
    Start-Sleep -Seconds $EverySeconds
}
