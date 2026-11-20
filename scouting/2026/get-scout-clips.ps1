# Cuts the Auto of each scouted partner match out of its event's YouTube stream, for drawing their paths.
# Uses the tools the match-replay setup installed (tools/advantagescope/replay/setup.ps1):
# yt-dlp, Deno and ffmpeg in %LOCALAPPDATA%\BIOBUZZ-Replay\tools. Windows PowerShell 5.1.
#
#   .\get-scout-clips.ps1                                  # every match below, to Desktop\scout-clips
#   .\get-scout-clips.ps1 -Team 10255                      # one team's matches
#   .\get-scout-clips.ps1 -Out "G:\My Drive\scout-clips"   # straight into a Google Drive folder
#   .\get-scout-clips.ps1 -CookiesFromBrowser edge         # when YouTube says "Sign in to confirm you're not a bot"
#
# Each clip runs from 40 s before the match's FTCScout start time to 50 s after it: the start time is only
# an estimate (the same one open-match makes), so the Auto is somewhere inside it. Clips already made are skipped.
# Names: <EVENT>_<MATCH>_<teams>.mp4, plus a .json beside each with the stream URL and the second it starts at.
param(
    [int]$Team = 0,
    [string]$Out = (Join-Path ([Environment]::GetFolderPath("Desktop")) "scout-clips"),
    [string]$CookiesFromBrowser = "",
    [double]$PreS = 40,
    [double]$PostS = 50
)
$ErrorActionPreference = "Stop"
$inv = [Globalization.CultureInfo]::InvariantCulture
$Tools = Join-Path $env:LOCALAPPDATA "BIOBUZZ-Replay\tools"
$ffmpeg = Join-Path $Tools "ffmpeg.exe"; $ytdlp = Join-Path $Tools "yt-dlp.exe"; $deno = Join-Path $Tools "deno.exe"
foreach ($t in $ffmpeg, $ytdlp, $deno) {
    if (-not (Test-Path $t)) { throw "$(Split-Path -Leaf $t) is missing: run the match-replay setup first (tools/advantagescope/replay/README.md, step 1)" }
}

# Each event's streams (from its FiM live page). Which stream holds a match is worked out from the stream's
# start time and length.
$Streams = @{
    "USMIKEQ" = @("https://www.youtube.com/watch?v=_1CwAGHePmw", "https://www.youtube.com/watch?v=TUcEUMmldo8")
    "USMICOQ" = @("https://www.youtube.com/watch?v=ghEe-5fH_Kw", "https://www.youtube.com/watch?v=vJkg_vun-nw")
}
# Each team's latest qualifier, from FTCScout: match, field start time (UTC), teams of ours in it.
$Scouted = @(
    @("USMIKEQ", "Q-4",  "2026-11-14T16:16:52Z", "11846"),
    @("USMIKEQ", "Q-8",  "2026-11-14T16:37:06Z", "10255"),
    @("USMIKEQ", "Q-15", "2026-11-14T18:11:23Z", "10255 11846"),
    @("USMIKEQ", "Q-20", "2026-11-14T18:48:01Z", "11846"),
    @("USMIKEQ", "Q-23", "2026-11-14T19:06:47Z", "10255"),
    @("USMIKEQ", "Q-25", "2026-11-14T19:19:15Z", "11846"),
    @("USMIKEQ", "Q-29", "2026-11-14T19:43:16Z", "10255"),
    @("USMIKEQ", "Q-33", "2026-11-14T20:03:12Z", "11846"),
    @("USMIKEQ", "Q-34", "2026-11-14T20:09:45Z", "10255"),
    @("USMIKEQ", "M-2",  "2026-11-14T21:38:29Z", "11846"),
    @("USMIKEQ", "M-3",  "2026-11-14T21:43:56Z", "10255"),
    @("USMIKEQ", "M-4",  "2026-11-14T21:50:27Z", "11846"),
    @("USMIKEQ", "M-7",  "2026-11-14T22:08:07Z", "10255 11846"),
    @("USMIKEQ", "M-9",  "2026-11-14T22:30:52Z", "11846"),
    @("USMIKEQ", "M-10", "2026-11-14T22:46:09Z", "10255"),
    @("USMICOQ", "Q-3",  "2026-11-07T16:18:51Z", "11228"),
    @("USMICOQ", "Q-9",  "2026-11-07T16:52:09Z", "11228"),
    @("USMICOQ", "Q-19", "2026-11-07T18:33:36Z", "11228"),
    @("USMICOQ", "Q-26", "2026-11-07T19:28:06Z", "11228"),
    @("USMICOQ", "Q-39", "2026-11-07T20:41:05Z", "11228"),
    @("USMICOQ", "M-4",  "2026-11-07T21:55:25Z", "11228"),
    @("USMICOQ", "M-7",  "2026-11-07T22:13:28Z", "11228"),
    @("USMICOQ", "M-10", "2026-11-07T22:59:32Z", "11228")
)

$ytArgs = @("--no-playlist", "--no-warnings", "--js-runtimes", "deno:$deno", "-f",
    "bv*[height<=720][vcodec^=avc1]/bv*[height<=720][vcodec!^=av01]/b[height<=720]/b")
if ($CookiesFromBrowser) { $ytArgs += @("--cookies-from-browser", $CookiesFromBrowser) }

$info = @{}
function Get-Stream([string]$url) {
    if ($info.ContainsKey($url)) { return $info[$url] }
    Write-Host "  ...     reading $url"
    $lines = @(& $ytdlp @ytArgs -O "%(release_timestamp,timestamp)s" -O "%(duration)s" -O "%(url)s" -O "%(http_headers)j" $url)
    if ($LASTEXITCODE -ne 0 -or $lines.Count -lt 4) { throw "yt-dlp could not read $url (see above; try -CookiesFromBrowser edge)" }
    $s = @{ Start = [double]::Parse($lines[0], $inv); Duration = [double]::Parse($lines[1], $inv); Url = $lines[2]; Headers = "" }
    foreach ($p in ($lines[3] | ConvertFrom-Json).PSObject.Properties) { $s.Headers += "$($p.Name): $($p.Value)`r`n" }
    $info[$url] = $s
    return $s
}

New-Item -ItemType Directory -Force $Out | Out-Null
$epoch = [DateTimeOffset]::new(1970, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
foreach ($m in $Scouted) {
    $ev, $match, $utc, $teams = $m
    if ($Team -and -not (($teams -split " ") -contains "$Team")) { continue }
    $name = "{0}_{1}_{2}" -f $ev, $match, ($teams -replace " ", "-")
    $file = Join-Path $Out "$name.mp4"
    if (Test-Path $file) { Write-Host "  OK      $name (already made)" -ForegroundColor Green; continue }
    $t = ([DateTimeOffset]::Parse($utc, $inv) - $epoch).TotalSeconds
    $found = $null
    foreach ($url in $Streams[$ev]) {
        $s = Get-Stream $url
        $at = $t - $s.Start
        if ($at -ge 0 -and $at -le $s.Duration) { $found = @{ Url = $url; S = $s; At = $at }; break }
    }
    if (-not $found) { Write-Host "  FAILED  $name : no stream covers $utc" -ForegroundColor Red; continue }
    $from = [Math]::Max([double]0, $found.At - $PreS)
    Write-Host "  ...     $name : $([TimeSpan]::FromSeconds($from).ToString('h\:mm\:ss')) into $($found.Url)"
    $a = @("-hide_banner", "-loglevel", "error", "-y")
    if ($found.S.Headers) { $a += @("-headers", $found.S.Headers) }
    $a += @(
        "-ss", $from.ToString("0.000", $inv), "-t", ($PreS + $PostS).ToString("0.000", $inv), "-i", $found.S.Url,
        "-vf", "scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,setsar=1",
        "-an", "-c:v", "libx264", "-preset", "veryfast", "-crf", "20", "-pix_fmt", "yuv420p", "$file.part.mp4")
    & $ffmpeg @a
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path "$file.part.mp4") -or (Get-Item "$file.part.mp4").Length -lt 20KB) {
        Remove-Item -Force -ErrorAction SilentlyContinue "$file.part.mp4"
        Write-Host "  FAILED  $name : ffmpeg could not cut it" -ForegroundColor Red; continue
    }
    Move-Item -Force "$file.part.mp4" $file
    Set-Content -Path "$file.json" -Value (@{ url = $found.Url; from = $from; field_start_utc = $utc } | ConvertTo-Json -Compress)
    Write-Host "  OK      $name" -ForegroundColor Green
}
Write-Host "Clips in $Out. Share that folder: Anyone with the link, Viewer, and send the link."
