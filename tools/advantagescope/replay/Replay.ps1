# Shared by setup.ps1 and open-match.ps1 (dot-sourced). Windows PowerShell 5.1: no ?? or ?:, no 3-argument
# Join-Path. Every path can be overridden by an environment variable, so the scripts can be tried off Windows.

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"   # Invoke-WebRequest is many times slower with its progress bar
try { [Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12 } catch { }

function Env-Or([string]$name, [string]$default) {
    $v = [Environment]::GetEnvironmentVariable($name)
    if ($v) { return $v } else { return $default }
}

# The folder these scripts are in: the laptop's installed copy, or a checkout's tools\advantagescope\replay.
$ReplayDir = $PSScriptRoot
$MarkerDir = Join-Path $ReplayDir "marker"

# Where things live on the laptop.
$ReplayHome = Env-Or "BIOBUZZ_REPLAY_HOME" (Join-Path $env:LOCALAPPDATA "BIOBUZZ-Replay")
$Scripts = Join-Path $ReplayHome "scripts"
$Tools = Env-Or "BIOBUZZ_TOOLS" (Join-Path $ReplayHome "tools")
$LogsDir = Join-Path $ReplayHome "logs"
$ClipsDir = Join-Path $ReplayHome "clips"
$CacheDir = Join-Path $ReplayHome "cache"
$BinDir = Join-Path $ReplayHome "bin"
# AdvantageScope's own folder (Electron's userData): userAssets, prefs, window state, ffmpeg.exe.
$AsData = Env-Or "BIOBUZZ_AS_DATA" (Join-Path $env:APPDATA "AdvantageScope")
$AsUserAssets = Join-Path $AsData "userAssets"
# A running AdvantageScope opens any log path written here within a quarter second (its AdvantageKit hook).
$AsOpenRequest = Join-Path ([IO.Path]::GetTempPath()) "ascope-log-path.txt"
$AssetFolders = @("Robot_BIOBUZZ", "Field3d_BIOBUZZHiveSim", "Robot_BIOBUZZHive")

# The video clip: from PreS before Auto starts to PostS after (Auto 30 s, transition 8 s, TeleOp 120 s).
$PreS = 10.0
$PostS = 170.0

function Say([string]$text) { Write-Host $text }
function Ok([string]$text) { Write-Host "  OK      $text" -ForegroundColor Green }
function Warn([string]$text) { Write-Host "  NOTE    $text" -ForegroundColor Yellow }
function Bad([string]$text) { Write-Host "  FAILED  $text" -ForegroundColor Red }

function Read-Json([string]$path) { return (Get-Content -Raw -Encoding UTF8 $path | ConvertFrom-Json) }

function Get-Sha256([string]$path) { return (Get-FileHash -Algorithm SHA256 $path).Hash.ToLowerInvariant() }
function Get-Sha256Text([string]$text) {
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return (-join ($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($text)) | ForEach-Object { $_.ToString("x2") })) } finally { $sha.Dispose() }
}

# Downloads $url to $dest, checks its sha256 against $sha, and only then moves it into place.
function Get-Pinned([string]$url, [string]$sha, [string]$dest) {
    $dir = Split-Path -Parent $dest
    New-Item -ItemType Directory -Force $dir | Out-Null
    $part = "$dest.part"
    if (Test-Path $part) { Remove-Item -Force $part }
    $tries = 0
    while ($true) {
        try {
            Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $part
            break
        } catch {
            $tries++
            if ($tries -ge 4) { throw "could not download $url ($($_.Exception.Message))" }
            Start-Sleep -Seconds ([Math]::Pow(2, $tries))
        }
    }
    $got = Get-Sha256 $part
    if ($sha -and $got -ne $sha.ToLowerInvariant()) {
        Remove-Item -Force $part
        throw "$url is not the pinned file (sha256 $got, replay.json says $sha). Nothing was installed from it."
    }
    if (Test-Path $dest) { Remove-Item -Force $dest }
    Move-Item $part $dest
}

# A file installed from a pin: present, and its stamp says it came from that pin.
function Test-Stamp([string]$path, [string]$sha) {
    $stamp = "$path.sha256"
    return (Test-Path $path) -and (Test-Path $stamp) -and ((Get-Content -Raw $stamp).Trim() -eq $sha)
}
function Set-Stamp([string]$path, [string]$sha) { Set-Content -NoNewline -Path "$path.sha256" -Value $sha }

function Get-Pins { return Read-Json (Join-Path $ReplayDir "replay.json") }

function Get-RawUrl($pins, [string]$ref, [string]$path) {
    return "https://raw.githubusercontent.com/$($pins.repo)/$ref/$path"
}

# --- AdvantageScope -------------------------------------------------------------------------------------------

# The installed AdvantageScope: @{ Exe; Version } or $null. The per-user installer writes an Uninstall key.
function Find-AdvantageScope {
    $override = [Environment]::GetEnvironmentVariable("BIOBUZZ_AS_EXE")
    if ($override) { return @{ Exe = $override; Version = (Env-Or "BIOBUZZ_AS_VERSION" "unknown") } }
    $keys = @(
        "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*")
    foreach ($k in $keys) {
        $entries = Get-ItemProperty $k -ErrorAction SilentlyContinue | Where-Object { $_.DisplayName -like "AdvantageScope*" }
        foreach ($e in $entries) {
            $dir = $e.InstallLocation
            if (-not $dir -and $e.DisplayIcon) { $dir = Split-Path -Parent ($e.DisplayIcon -replace '"', '' -replace ',\d+$', '') }
            if ($dir) {
                $exe = Join-Path $dir "AdvantageScope.exe"
                if (Test-Path $exe) { return @{ Exe = $exe; Version = $e.DisplayVersion } }
            }
        }
    }
    $default = Join-Path $env:LOCALAPPDATA "Programs\advantagescope\AdvantageScope.exe"
    if (Test-Path $default) {
        return @{ Exe = $default; Version = (Get-Item $default).VersionInfo.ProductVersion }
    }
    return $null
}

function Get-AdvantageScopeProcesses { return @(Get-Process -ErrorAction SilentlyContinue | Where-Object { $_.ProcessName -like "AdvantageScope*" }) }

# The window-state file AdvantageScope restores at start; one per version.
function Get-StateFile($pins) { return Join-Path $AsData ("state-" + $pins.advantagescope.version.Replace(".", "_") + ".json") }

# --- Match logs -----------------------------------------------------------------------------------------------

# matches.json's "log" is "<logs branch>/<file>.wpilog.gz"; the laptop keeps it unzipped as logs\<branch>\<file>.wpilog.
function Get-LogPath([string]$log) {
    $parts = $log -split "/"
    return Join-Path (Join-Path $LogsDir $parts[0]) ($parts[1] -replace '\.gz$', '')
}

function Get-MatchLog($pins, $match) {
    $path = Get-LogPath $match.log
    if (Test-Path $path) { return $path }
    $parts = $match.log -split "/"
    $url = "https://raw.githubusercontent.com/$($pins.repo)/refs/heads/logs/$($parts[0])/$($parts[1])"
    $gz = "$path.gz"
    New-Item -ItemType Directory -Force (Split-Path -Parent $path) | Out-Null
    try {
        Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $gz
    } catch {
        throw "could not download $($match.log) from GitHub ($($_.Exception.Message)). Is the logs/$($parts[0]) branch still there?"
    }
    $in = [IO.File]::OpenRead($gz)
    try {
        $out = [IO.File]::Create("$path.part")
        try {
            $unzip = New-Object IO.Compression.GZipStream($in, [IO.Compression.CompressionMode]::Decompress)
            $unzip.CopyTo($out)
        } finally { $out.Dispose() }
    } finally { $in.Dispose() }
    Remove-Item -Force $gz
    Move-Item "$path.part" $path
    return $path
}

# The first enable and the first disable after it, in log seconds: the Auto that AdvantageScope's video sync
# locks to (it aligns the video to the log's first enabled -> disabled change). A tiny WPILib DataLog reader.
if (-not ("BiobuzzWpilog" -as [type])) {
    Add-Type -TypeDefinition @"
using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
public static class BiobuzzWpilog {
    static long Le(byte[] b, int at, int n) { long v = 0; for (int i = n - 1; i >= 0; i--) v = (v << 8) | b[at + i]; return v; }
    /// <summary>{ first enabled time, first disabled time after it } in seconds; NaN where there is none.</summary>
    public static double[] AutoEdges(string path, string key) {
        byte[] b = File.ReadAllBytes(path);
        if (b.Length < 12 || Encoding.ASCII.GetString(b, 0, 6) != "WPILOG") throw new Exception(path + " is not a .wpilog");
        int i = 12 + (int)Le(b, 8, 4);
        long id = -1;
        double on = double.NaN, off = double.NaN;
        while (i < b.Length) {
            int h = b[i++];
            int il = (h & 3) + 1, sl = ((h >> 2) & 3) + 1, tl = ((h >> 4) & 7) + 1;
            long eid = Le(b, i, il); i += il;
            int size = (int)Le(b, i, sl); i += sl;
            long ts = Le(b, i, tl); i += tl;
            if (i + size > b.Length) break;
            if (eid == 0) {
                if (size > 0 && b[i] == 0) {
                    long e = Le(b, i + 1, 4);
                    int n = (int)Le(b, i + 5, 4);
                    string name = Encoding.UTF8.GetString(b, i + 9, n);
                    if (name == key) id = e;
                }
            } else if (eid == id && size >= 1) {
                bool v = b[i] != 0;
                double t = ts / 1e6;
                if (v && double.IsNaN(on)) on = t;
                else if (!v && !double.IsNaN(on)) { off = t; break; }
            }
            i += size;
        }
        return new double[] { on, off };
    }
}
"@
}

function Get-AutoEdges([string]$logPath) {
    $e = [BiobuzzWpilog]::AutoEdges($logPath, "/DriverStation/Enabled")
    if ([double]::IsNaN($e[0]) -or [double]::IsNaN($e[1])) { throw "$logPath has no Auto (never enabled, then disabled): not a match log?" }
    return $e
}

# --- Video ----------------------------------------------------------------------------------------------------

# "1:23:45.6", "83:45.6" or "5025.6" -> seconds; "" -> $null.
function ConvertFrom-VideoTime([string]$text) {
    if (-not $text) { return $null }
    $total = 0.0
    foreach ($p in ($text.Trim() -split ":")) { $total = $total * 60 + [double]::Parse($p, [Globalization.CultureInfo]::InvariantCulture) }
    return $total
}
# A matches.json time (ISO 8601, UTC) as Unix seconds. Windows PowerShell keeps it a string; pwsh 7 turns it into a
# DateTime on its own, and that DateTime's text drops the milliseconds, so take it as it comes.
function ConvertTo-UnixSeconds($value) {
    if ($value -is [DateTime]) { return ([DateTimeOffset]$value.ToUniversalTime()).ToUnixTimeMilliseconds() / 1000.0 }
    return ([DateTimeOffset]::Parse([string]$value, [Globalization.CultureInfo]::InvariantCulture)).ToUnixTimeMilliseconds() / 1000.0
}
function ConvertTo-VideoTime([double]$s) {
    $h = [Math]::Floor($s / 3600); $m = [Math]::Floor(($s - 3600 * $h) / 60); $r = $s - 3600 * $h - 60 * $m
    return [string]::Format([Globalization.CultureInfo]::InvariantCulture, "{0}:{1:00}:{2:00.00}", $h, $m, $r)
}

# AdvantageScope's sync (VideoProcessor.ts, v27.0.0-alpha-6) reads the timer at fixed spots of a 1920x1080
# frame and locks where it changes from 0:04 to 0:03, 3 s before the log's Auto ends. The clip is 1280x720, so
# each spot is two thirds of AdvantageScope's numbers. All eight are blacked out until Auto ends (the FTC overlay
# or the field could put digits there), and the countdown goes in the 2024/2025 lower-banner spot.
$TimerSpots1080 = @(
    @(1040, 760, 80, 40), @(895, 905, 130, 80), @(895, 80, 130, 80), @(925, 1005, 75, 30),
    @(865, 980, 200, 90), @(865, 160, 200, 90), @(900, 960, 120, 68), @(900, 65, 120, 68))
$MarkerSpot1080 = @(865, 980, 200, 90)

# The ffmpeg -filter_complex for a clip whose Auto starts at $autoStartS (clip seconds) and lasts $autoS.
function Get-ClipFilter([double]$autoStartS, [double]$autoS) {
    $inv = [Globalization.CultureInfo]::InvariantCulture
    $end = $autoStartS + $autoS
    $hide = ($end + 1).ToString("0.000", $inv)
    $f = "[0:v]setpts=PTS-STARTPTS,scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,setsar=1"
    foreach ($r in $TimerSpots1080) {
        $x = [Math]::Floor($r[0] * 2 / 3) - 2; $y = [Math]::Floor($r[1] * 2 / 3) - 2
        $w = [Math]::Ceiling($r[2] * 2 / 3) + 4; $h = [Math]::Ceiling($r[3] * 2 / 3) + 4
        $f += ",drawbox=x=${x}:y=${y}:w=${w}:h=${h}:color=black:t=fill:enable='lt(t,$hide)'"
    }
    $f += "[v0]"
    # 120x52 images, centred in the marker spot.
    $mx = [Math]::Round(($MarkerSpot1080[0] + $MarkerSpot1080[2] / 2) * 2 / 3 - 60)
    $my = [Math]::Round(($MarkerSpot1080[1] + $MarkerSpot1080[3] / 2) * 2 / 3 - 26)
    $last = "v0"
    $n = 1
    foreach ($sec in 5, 4, 3, 2) {
        $from = ($end - $sec).ToString("0.000", $inv); $to = ($end - $sec + 1).ToString("0.000", $inv)
        $f += ";[$last][${n}:v]overlay=x=${mx}:y=${my}:shortest=1:enable='gte(t,$from)*lt(t,$to)'[v$n]"
        $last = "v$n"
        $n++
    }
    return @{ Filter = $f; Out = "[$last]" }
}

# Writes the clip: video from $inputUrl (a direct stream URL with its HTTP headers, or a local file) from
# $fromS for $durS seconds, 1280x720, no sound (AdvantageScope plays none), with the sync marker burnt in.
function New-Clip([string]$ffmpeg, [string]$inputUrl, $headers, [double]$fromS, [double]$durS, [double]$autoStartS, [double]$autoS, [string]$out) {
    $inv = [Globalization.CultureInfo]::InvariantCulture
    $g = Get-ClipFilter $autoStartS $autoS
    $a = @("-hide_banner", "-loglevel", "error", "-stats", "-y")
    if ($headers -and $inputUrl -match '^https?://') { $a += @("-headers", $headers) }
    $a += @("-ss", $fromS.ToString("0.000", $inv), "-t", $durS.ToString("0.000", $inv), "-i", $inputUrl)
    foreach ($sec in 5, 4, 3, 2) { $a += @("-loop", "1", "-i", (Join-Path $MarkerDir "0-0$sec.png")) }
    $a += @("-filter_complex", $g.Filter, "-map", $g.Out, "-an", "-c:v", "libx264", "-preset", "veryfast", "-crf", "20",
        "-pix_fmt", "yuv420p", "-movflags", "+faststart", "$out.part.mp4")
    & $ffmpeg @a
    if ($LASTEXITCODE -ne 0) { throw "ffmpeg could not make the clip (exit $LASTEXITCODE; see above)" }
    if (Test-Path $out) { Remove-Item -Force $out }
    Move-Item "$out.part.mp4" $out
}
