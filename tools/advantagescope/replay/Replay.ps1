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
$SourcesDir = Join-Path $ReplayHome "sources"   # each match's stretch of the event video, unmarked, downloaded once
$CacheDir = Join-Path $ReplayHome "cache"
$BinDir = Join-Path $ReplayHome "bin"
# AdvantageScope's own folder (Electron's userData): userAssets, prefs, window state, ffmpeg.exe.
$AsData = Env-Or "BIOBUZZ_AS_DATA" (Join-Path $env:APPDATA "AdvantageScope")
$AsUserAssets = Join-Path $AsData "userAssets"
$AssetFolders = @("Robot_BIOBUZZ", "Field3d_BIOBUZZHiveSim", "Robot_BIOBUZZHive")

# The video clip: from PreS before Auto starts to PostS after (Auto 30 s, transition 8 s, TeleOp 120 s).
$FieldAutoS = 30.0   # an FTC Auto, by the field's clock
# The FiM overlay shows the transition's hand this long after the field's Auto really ended: at Belding our light
# went off (our PLAY) 0.35-0.55 s before the hand said Auto started, in every match where it was in shot.
$ScoreboardLagS = 0.45
$PreS = 10.0
$PostS = 170.0
# A match's source keeps more on each side, so the clip can move (the scoreboard, a -Shift) without a new download.
$SourcePreS = 40.0
$SourcePostS = 200.0

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

$onWindows = $env:OS -eq "Windows_NT"

# Writes the team layout into AdvantageScope's saved window state: match-layout.json's tabs in the main window with
# Video showing, and the 3D tab in a second window beside it. AdvantageScope reads this only at start and rewrites
# it four times a second while it runs, so it must be closed (Stop-AdvantageScope) first.
function Write-TeamLayout($pins) {
    $layout = Read-Json (Join-Path $ReplayDir "match-layout.json")
    $hub = $layout.hubs[0]
    $tabs = @($hub.state.tabs.tabs)
    $video = -1
    for ($i = 0; $i -lt $tabs.Count; $i++) { if ([int]$tabs[$i].type -eq 7) { $video = $i } }   # 7: Video
    $threeD = $tabs | Where-Object { [int]$_.type -eq 3 -and $_.title -eq "3D" } | Select-Object -First 1
    if ($video -lt 0 -or -not $threeD) { throw "match-layout.json has no Video tab or no 3D tab" }

    # The screen, in the units AdvantageScope uses (Windows' scaled pixels); a common laptop size without it.
    $area = @{ X = 0; Y = 0; Width = 1536; Height = 816 }
    if ($onWindows) {
        try {
            Add-Type -AssemblyName System.Windows.Forms
            $wa = [System.Windows.Forms.Screen]::PrimaryScreen.WorkingArea
            $area = @{ X = $wa.X; Y = $wa.Y; Width = $wa.Width; Height = $wa.Height }
        } catch { }
    }
    $hubWidth = [int]($area.Width * 0.55)
    $hub.x = $area.X; $hub.y = $area.Y; $hub.width = $hubWidth; $hub.height = $area.Height
    $hub.state.tabs.selected = $video
    $satellite = [pscustomobject]@{
        x = $area.X + $hubWidth; y = $area.Y; width = $area.Width - $hubWidth; height = $area.Height
        state = [pscustomobject]@{ type = 3; visualizer = $threeD.renderer }
        uuid = $threeD.controllerUUID
    }
    $out = [pscustomobject]@{ hubs = @($hub); satellites = @($satellite) }
    New-Item -ItemType Directory -Force $AsData | Out-Null
    # Windows PowerShell 5.1's type data for arrays makes ConvertTo-Json write {"value": [...], "Count": n}.
    Remove-TypeData System.Array -ErrorAction SilentlyContinue
    [IO.File]::WriteAllText((Get-StateFile $pins), ($out | ConvertTo-Json -Depth 100 -Compress))
}

# Closes every AdvantageScope window and waits until it has gone, so it cannot save its state over ours.
function Stop-AdvantageScope {
    $running = Get-AdvantageScopeProcesses
    if ($running.Count -eq 0) { return $false }
    $running | Stop-Process -Force -ErrorAction SilentlyContinue
    $running | Wait-Process -Timeout 10 -ErrorAction SilentlyContinue
    return $true
}

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
    /// <summary>Every value of one numeric entry (boolean, int64, float, double) as { t0, v0, t1, v1, ... }, seconds.</summary>
    public static double[] Series(string path, string key) {
        byte[] b = File.ReadAllBytes(path);
        if (b.Length < 12 || Encoding.ASCII.GetString(b, 0, 6) != "WPILOG") throw new Exception(path + " is not a .wpilog");
        int i = 12 + (int)Le(b, 8, 4);
        long id = -1; string type = "";
        List<double> outv = new List<double>();
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
                    int tn = (int)Le(b, i + 9 + n, 4);
                    if (name == key) { id = e; type = Encoding.UTF8.GetString(b, i + 13 + n, tn); }
                }
            } else if (eid == id && size >= 1) {
                double v = double.NaN;
                if (type == "boolean") v = b[i] != 0 ? 1 : 0;
                else if (type == "double" && size == 8) v = BitConverter.ToDouble(b, i);
                else if (type == "float" && size == 4) v = BitConverter.ToSingle(b, i);
                else if (type == "int64" && size == 8) v = BitConverter.ToInt64(b, i);
                if (!double.IsNaN(v)) { outv.Add(ts / 1e6); outv.Add(v); }
            }
            i += size;
        }
        return outv.ToArray();
    }
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

# What our light did from the log, for Find-OurLight: @{ Ok; Blue; OnS; OffS; Why }. Blue from the alliance
# station at PLAY (1-3 red, 4-6 blue); OnS, OffS the first lane-full green after PLAY, as seconds after it.
function Get-LightPattern([string]$logPath) {
    $e = Get-AutoEdges $logPath
    $play = $e[0]
    $st = [BiobuzzWpilog]::Series($logPath, "/DriverStation/AllianceStation")
    $station = 0
    for ($i = 0; $i -lt $st.Length; $i += 2) { if ($st[$i] -le $play) { $station = $st[$i + 1] } }
    if ($station -lt 1 -or $station -gt 6) { return @{ Ok = $false; Why = "the log has no alliance" } }
    $full = [BiobuzzWpilog]::Series($logPath, "/Lane/Full")
    $on = $null; $off = $null; $last = 0
    for ($i = 0; $i -lt $full.Length; $i += 2) {
        $t = $full[$i]; $v = $full[$i + 1]
        if ($t -le $play) { $last = $v; continue }
        if ($null -eq $on -and $v -ge 0.5 -and $last -lt 0.5) { $on = $t - $play }
        elseif ($null -ne $on -and $v -lt 0.5) { $off = $t - $play; break }
        $last = $v
    }
    if ($null -eq $on -or $null -eq $off -or $on -gt 3 -or ($off - $on) -lt 0.15) {
        return @{ Ok = $false; Why = "the lane was not full for a moment in the first 3 s, so there was no green flash to find" }
    }
    return @{ Ok = $true; Blue = ($station -ge 4); OnS = $on; OffS = $off }
}

function Get-AutoEdges([string]$logPath) {
    $e = [BiobuzzWpilog]::AutoEdges($logPath, "/DriverStation/Enabled")
    if ([double]::IsNaN($e[0]) -or [double]::IsNaN($e[1])) { throw "$logPath has no Auto (never enabled, then disabled): not a match log?" }
    return $e
}

# --- Video ----------------------------------------------------------------------------------------------------

# The FiM stream's scoreboard shows a hand above the timer for the whole 8 s transition, so the first frame with the
# hand is the moment the field's Auto ended. This is that hand (marker/scoreboard-hand.gray, cut from a Belding frame
# with this same crop), and where it sits, as fractions of the frame so any resolution works.
$HandCrop = "crop=iw*0.036458:ih*0.052778:iw*0.481771:ih*0.834259,scale=28:24:flags=area,format=gray"
$HandW = 28; $HandH = 24
if (-not ("BiobuzzScoreboard" -as [type])) {
    Add-Type -TypeDefinition @"
using System;
public static class BiobuzzScoreboard {
    static double Ncc(byte[] a, int ao, byte[] b, int n) {
        double ma = 0, mb = 0;
        for (int i = 0; i < n; i++) { ma += a[ao + i]; mb += b[i]; }
        ma /= n; mb /= n;
        double ab = 0, aa = 0, bb = 0;
        for (int i = 0; i < n; i++) { double x = a[ao + i] - ma, y = b[i] - mb; ab += x * y; aa += x * x; bb += y * y; }
        return (aa < 1e-6 || bb < 1e-6) ? 0 : ab / Math.Sqrt(aa * bb);
    }
    /// <summary>The first frame where the template appears and stays (after at least `before` frames without it):
    /// { frame index, its match score }, or { -1, best score seen }.</summary>
    public static double[] Onset(byte[] frames, byte[] template, double threshold, int before, int stay) {
        int n = template.Length, count = frames.Length / n;
        double[] score = new double[count];
        double best = -1;
        for (int f = 0; f < count; f++) { score[f] = Ncc(frames, f * n, template, n); if (score[f] > best) best = score[f]; }
        for (int f = before; f + stay <= count; f++) {
            if (score[f] < threshold) continue;
            bool absent = true, stays = true;
            for (int k = f - before; k < f; k++) if (score[k] >= threshold) { absent = false; break; }
            int held = 0;
            for (int k = f; k < f + stay; k++) if (score[k] >= threshold) held++;
            if (held < stay * 0.8) stays = false;
            if (absent && stays) return new double[] { f, score[f] };
        }
        return new double[] { -1, best };
    }
}
"@
}

# Our own indicator light, found in the video by the pattern the log says it made: our alliance's colour until our
# driver pressed PLAY, then off, then green for exactly the stretch the lane was full (/Lane/Full), about a second
# later. Candidates are steady lights in the alliance colour above the overlay; the one whose colour drops and
# that flashes green at those offsets is ours, and the frame its colour drops is our PLAY. Measured at Belding: the
# right light and moment in every match where our robot was in shot (score 85-90), nothing above 20 when the
# camera showed the other field. The pressed-PLAY moment is what the log's Auto start is, so this replaces both
# the stream's delay and the scoreboard's own lag (0.4-0.6 s at Belding).
if (-not ("BiobuzzLight" -as [type])) {
    Add-Type -TypeDefinition @"
using System;
public static class BiobuzzLight {
    /// <summary>rgb: frames of w*h*3 bytes at fps, the first at second t0 of the video. Returns { PLAY (video
    /// seconds), x, y (cell, of w*h), score, green bump, colour drop }, PLAY -1 when no light scores minScore.</summary>
    public static double[] Find(byte[] rgb, int w, int h, int fps, double t0, bool blue, double onS, double offS,
                                double searchFrom, double searchTo, double minScore) {
        int n = w * h, frames = rgb.Length / (n * 3);
        // Per-pixel alliance colour and green, as prefix sums over time.
        float[] col = new float[(frames + 1) * n], grn = new float[(frames + 1) * n];
        for (int f = 0; f < frames; f++) {
            int src = f * n * 3, dst = (f + 1) * n, prev = f * n;
            for (int p = 0; p < n; p++) {
                int r = rgb[src + 3 * p], g = rgb[src + 3 * p + 1], b = rgb[src + 3 * p + 2];
                int c = blue ? b - Math.Max(r, g) : r - Math.Max(g, b);
                col[dst + p] = col[prev + p] + c;
                grn[dst + p] = grn[prev + p] + (g - Math.Max(r, b));
            }
        }
        Func<float[], int, int, int, float> mean = (a, p, i, j) => (a[j * n + p] - a[i * n + p]) / Math.Max(1, j - i);
        Func<double, int> fr = s => (int)Math.Round(s * fps);
        // Candidates: steady and bright in the alliance colour for the first second (before anyone can have pressed),
        // above the overlay.
        bool[] cand = new bool[n];
        int pre = Math.Min(frames, fr(1.0));
        for (int p = 0; p < n; p++) {
            if (p / w >= (int)(h * 0.83)) continue;
            double sum = 0, sq = 0, min = double.MaxValue;
            for (int f = 0; f < pre; f++) {
                double v = col[(f + 1) * n + p] - col[f * n + p];
                sum += v; sq += v * v; if (v < min) min = v;
            }
            double m = sum / pre, sd = Math.Sqrt(Math.Max(0, sq / pre - m * m));
            cand[p] = min >= 60 && sd < 12;
        }
        double best = -1, bt = -1, bg = 0, bd = 0; int bp = -1, bi = -1;
        int iFrom = Math.Max(fr(0.6), fr(searchFrom - t0)), iTo = fr(searchTo - t0);
        for (int i = iFrom; i <= iTo; i++) {
            int a0 = i + fr(onS), b0 = i + fr(offS);
            if (b0 + fr(0.30) > frames || i - fr(0.4) < 0) break;
            for (int p = 0; p < n; p++) {
                if (!cand[p]) continue;
                float inside = mean(grn, p, a0 + 2, b0 - 2);
                float g = inside - Math.Max(mean(grn, p, a0 - fr(0.30), a0 - 2), mean(grn, p, b0 + 2, b0 + fr(0.30)));
                float d = mean(col, p, i - fr(0.4), i - 2) - mean(col, p, i + 3, i + fr(0.5));
                double score = Math.Min(2 * g, d);
                if (score > best) { best = score; bp = p; bi = i; bg = g; bd = d; }
            }
        }
        if (bp < 0 || best < minScore) return new double[] { -1, -1, -1, best, bg, bd };
        // The exact frame: the brightest of the light's 3x3 cells, where its colour crosses halfway down.
        int px = bp % w, py = bp / w;
        Func<int, double> light = f => {
            double v = double.MinValue;
            for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                int x = px + dx, y = py + dy;
                if (x < 0 || y < 0 || x >= w || y >= h) continue;
                int q = y * w + x;
                double c = col[(f + 1) * n + q] - col[f * n + q];
                if (c > v) v = c;
            }
            return v;
        };
        double before = 0, after = 0; int nb = 0, na = 0;
        for (int f = bi - fr(0.5); f < bi - fr(0.15); f++) if (f >= 0) { before += light(f); nb++; }
        for (int f = bi + fr(0.15); f < bi + fr(0.5); f++) if (f < frames) { after += light(f); na++; }
        before /= Math.Max(1, nb); after /= Math.Max(1, na);
        double half = (before + after) / 2;
        int edge = bi;
        for (int f = bi - fr(0.25); f <= bi + fr(0.25); f++) {
            if (f > 0 && f < frames && light(f) < half) { edge = f; break; }
        }
        bt = t0 + (double)edge / fps;
        return new double[] { bt, px, py, best, bg, bd };
    }
}
"@
}

# Our light in $video: $playEstimate (seconds into the file) is where PLAY should be give or take a second and a
# half; $onS, $offS when the lane-full green came on and went off after PLAY (from the log). @{ At = PLAY in the
# file, or $null; X; Y (in 1280x720); Score }.
function Find-OurLight([string]$ffmpeg, [string]$video, [double]$playEstimate, [bool]$blue, [double]$onS, [double]$offS) {
    $inv = [Globalization.CultureInfo]::InvariantCulture
    $w = 320; $h = 180; $fps = 60
    $t0 = [Math]::Max([double]0, $playEstimate - 2.1)
    $dur = ($playEstimate + 1.0 + $offS + 0.8) - $t0
    $raw = Join-Path $CacheDir ("light-" + [Guid]::NewGuid().ToString("n") + ".rgb")
    New-Item -ItemType Directory -Force $CacheDir | Out-Null
    & $ffmpeg -hide_banner -loglevel error -y -ss $t0.ToString("0.000", $inv) -t $dur.ToString("0.000", $inv) -i $video `
        -vf "fps=$fps,scale=${w}:${h}:flags=area" -f rawvideo -pix_fmt rgb24 $raw
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path $raw)) { throw "ffmpeg could not read our light from $video" }
    try {
        $r = [BiobuzzLight]::Find([IO.File]::ReadAllBytes($raw), $w, $h, $fps, $t0, $blue, $onS, $offS,
            ($playEstimate - 1.5), ($playEstimate + 1.0), 40.0)
    } finally { Remove-Item -Force $raw -ErrorAction SilentlyContinue }
    $at = $null; if ($r[0] -ge 0) { $at = $r[0] }
    return @{ At = $at; X = [int]($r[1] * 1280 / $w); Y = [int]($r[2] * 720 / $h); Score = $r[3] }
}

# Where the scoreboard's hand first appears in $video between $fromS and $fromS + $durS (seconds into that file):
# @{ At = seconds into the file; Score = how well it matched (0..1) }, At $null when it never appears there.
function Find-ScoreboardHand([string]$ffmpeg, [string]$video, [double]$fromS, [double]$durS) {
    $inv = [Globalization.CultureInfo]::InvariantCulture
    $raw = Join-Path $CacheDir ("hand-" + [Guid]::NewGuid().ToString("n") + ".gray")
    New-Item -ItemType Directory -Force $CacheDir | Out-Null
    & $ffmpeg -hide_banner -loglevel error -y -ss $fromS.ToString("0.000", $inv) -t $durS.ToString("0.000", $inv) -i $video `
        -vf "fps=30,$HandCrop" -f rawvideo $raw
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path $raw)) { throw "ffmpeg could not read the scoreboard from $video" }
    try {
        $templateFile = Join-Path $MarkerDir "scoreboard-hand.gray"
        if (-not (Test-Path $templateFile)) { throw "this laptop's scripts are missing marker\scoreboard-hand.gray: run update-replay again" }
        $template = [IO.File]::ReadAllBytes($templateFile)
        # Present at least 6 of the next 7.5 s (the transition is 8), absent the 2 s before.
        $r = [BiobuzzScoreboard]::Onset([IO.File]::ReadAllBytes($raw), $template, 0.75, 60, 225)
    } finally { Remove-Item -Force $raw -ErrorAction SilentlyContinue }
    if ($r[0] -lt 0) { return @{ At = $null; Score = $r[1] } }
    return @{ At = $fromS + $r[0] / 30.0; Score = $r[1] }
}

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

# Saves $count pictures from $video, $stepS apart from $fromS, into $outDir (emptied first), each named by its offset:
# $labelStart, $labelStart + $stepS, ... as "+0.30s.jpg" / "-0.20s.jpg", so they sort and read as times.
function Export-Frames([string]$ffmpeg, [string]$video, [double]$fromS, [int]$count, [double]$stepS, [double]$labelStart, [string]$outDir) {
    $inv = [Globalization.CultureInfo]::InvariantCulture
    if (Test-Path $outDir) { Remove-Item -Recurse -Force $outDir }
    New-Item -ItemType Directory -Force $outDir | Out-Null
    $rate = (1.0 / $stepS).ToString("0.###", $inv)
    & $ffmpeg -hide_banner -loglevel error -y -ss ([Math]::Max([double]0, $fromS)).ToString("0.000", $inv) -i $video `
        -vf "fps=$rate" -frames:v $count -q:v 2 (Join-Path $outDir "%03d.jpg")
    if ($LASTEXITCODE -ne 0) { throw "ffmpeg could not save the pictures (exit $LASTEXITCODE)" }
    for ($i = 1; $i -le $count; $i++) {
        $f = Join-Path $outDir ("{0:000}.jpg" -f $i)
        if (-not (Test-Path $f)) { break }
        $t = $labelStart + ($i - 1) * $stepS
        $sign = "+"; if ($t -lt -0.0001) { $sign = "-" }
        # A leading index keeps them in time order however a viewer sorts names with signs.
        Rename-Item $f ("{0:00} {1}{2}s.jpg" -f $i, $sign, [Math]::Abs($t).ToString("0.00", $inv))
    }
}

# Downloads a match's source: $durS seconds of the stream from $fromS, 1280x720, no sound, nothing drawn on it.
function New-Source([string]$ffmpeg, [string]$inputUrl, [string]$headers, [double]$fromS, [double]$durS, [string]$out) {
    $inv = [Globalization.CultureInfo]::InvariantCulture
    $a = @("-hide_banner", "-loglevel", "error", "-stats", "-y")
    if ($headers -and $inputUrl -match '^https?://') { $a += @("-headers", $headers) }
    $a += @("-ss", $fromS.ToString("0.000", $inv), "-t", $durS.ToString("0.000", $inv), "-i", $inputUrl,
        "-vf", "scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,setsar=1",
        "-an", "-c:v", "libx264", "-preset", "veryfast", "-crf", "18", "-pix_fmt", "yuv420p", "$out.part.mp4")
    & $ffmpeg @a
    if ($LASTEXITCODE -ne 0) { throw "ffmpeg could not download this match's video (exit $LASTEXITCODE; see above)" }
    if ((Get-Item "$out.part.mp4").Length -lt 20KB) {
        Remove-Item -Force "$out.part.mp4"
        throw "the video came out empty: the match is not in that part of the video (check the event's video and video_start)"
    }
    if (Test-Path $out) { Remove-Item -Force $out }
    Move-Item "$out.part.mp4" $out
}

# Writes the clip: video from $inputUrl (a direct stream URL with its HTTP headers, or a local file) from
# $fromS for $durS seconds, 1280x720, no sound (AdvantageScope plays none), with the sync marker burnt in.
function New-Clip([string]$ffmpeg, [string]$inputUrl, $headers, [double]$fromS, [double]$durS, [double]$autoStartS, [double]$autoS, [string]$out) {
    $inv = [Globalization.CultureInfo]::InvariantCulture
    $g = Get-ClipFilter $autoStartS $autoS
    $a = @("-hide_banner", "-loglevel", "error", "-stats", "-y")
    if ($headers -and $inputUrl -match '^https?://') { $a += @("-headers", $headers) }
    $a += @("-ss", $fromS.ToString("0.000", $inv), "-t", $durS.ToString("0.000", $inv), "-i", $inputUrl)
    # Each countdown image lasts the clip's length: looped forever, ffmpeg never ends when the video has no frames
    # there (a video_start past the end of the stream).
    foreach ($sec in 5, 4, 3, 2) { $a += @("-loop", "1", "-t", $durS.ToString("0.000", $inv), "-i", (Join-Path $MarkerDir "0-0$sec.png")) }
    $a += @("-filter_complex", $g.Filter, "-map", $g.Out, "-an", "-c:v", "libx264", "-preset", "veryfast", "-crf", "20",
        "-pix_fmt", "yuv420p", "-movflags", "+faststart", "$out.part.mp4")
    & $ffmpeg @a
    if ($LASTEXITCODE -ne 0) { throw "ffmpeg could not make the clip (exit $LASTEXITCODE; see above)" }
    if ((Get-Item "$out.part.mp4").Length -lt 20KB) {
        Remove-Item -Force "$out.part.mp4"
        throw "the clip came out empty: the match is not in that part of the video (check the event's video and video_start)"
    }
    if (Test-Path $out) { Remove-Item -Force $out }
    Move-Item "$out.part.mp4" $out
}
