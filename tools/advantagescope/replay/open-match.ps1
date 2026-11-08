# Opens one BIOBUZZ match in AdvantageScope: its log, and a clip of the event stream that syncs itself to it.
#
#   open-match                 pick from a list
#   open-match Q-14            (or Q14, q14, M-7) one robot played it: that one; both: it asks which
#   open-match M-7 19429       that robot's log of the match
#   open-match Q-14 -Shift 1.5 the video 1.5 s later than matches.json says (to try a correction; see the README)
#
# The log opens first (in the AdvantageScope already open, if one is). The clip is cut from the stream the first
# time a match is opened (about a minute) and kept. AdvantageScope cannot be told to load a video, so the clip's
# path goes on the clipboard: Video tab, file icon, Ctrl+V, Enter. The clip carries a countdown where
# AdvantageScope reads a match timer, so it locks the video to the log by itself; nobody scrubs or syncs by hand.
param(
    [Parameter(Position = 0)][string]$Match = "",
    [Parameter(Position = 1)][string]$Team = "",
    [double]$Shift = 0,                  # seconds added to this match's video_start, for a try
    [string]$Event = "",                 # an event code (USMIBEQ) when two events have the same match name
    [switch]$NoVideo,                    # the log only
    [switch]$Rebuild,                    # cut the clip again even if it is kept
    [string]$CookiesFromBrowser = ""     # firefox / edge / chrome: when YouTube says "sign in to confirm"
)

. (Join-Path $PSScriptRoot "Replay.ps1")
$menu = -not $Match

function Finish([int]$code) {
    if ($menu) { Read-Host "Press Enter to close" | Out-Null }
    exit $code
}

try {
    $pins = Get-Pins
    # matches.json: the newest on GitHub (so a fixed offset reaches every laptop), else the copy here. A checkout
    # always uses its own file, which is what someone editing it wants to try.
    $matchesFile = Join-Path $ReplayDir "matches.json"
    $inCheckout = Test-Path (Join-Path $ReplayDir "..\..\..\.git")
    if (-not $inCheckout) {
        $refFile = Join-Path $ReplayHome "ref.txt"
        $ref = "master"; if (Test-Path $refFile) { $ref = (Get-Content -Raw $refFile).Trim() }
        try {
            $fresh = (Invoke-WebRequest -UseBasicParsing -TimeoutSec 10 -Uri (Get-RawUrl $pins $ref "tools/advantagescope/replay/matches.json")).Content
            if ($fresh -is [byte[]]) { $fresh = [Text.Encoding]::UTF8.GetString($fresh) }
            $null = $fresh | ConvertFrom-Json
            [IO.File]::WriteAllText($matchesFile, $fresh)
        } catch { Warn "could not check GitHub for a newer matches.json; using the one on this laptop" }
    }
    $doc = Read-Json $matchesFile
    $all = New-Object System.Collections.ArrayList
    foreach ($e in $doc.events) { foreach ($m in $e.matches) { [void]$all.Add([pscustomobject]@{ Event = $e; Row = $m }) } }
    if ($Event) { $all = @($all | Where-Object { $_.Event.event -eq $Event }) }

    function Key([string]$s) { return ($s.ToUpperInvariant() -replace '[^A-Z0-9]', '') }

    if ($menu) {
        Say ""
        Say "BIOBUZZ matches"
        for ($i = 0; $i -lt $all.Count; $i++) {
            $c = $all[$i]
            $when = [DateTimeOffset]::FromUnixTimeMilliseconds([long](1000 * (ConvertTo-UnixSeconds $c.Row.start_utc))).ToLocalTime().ToString("ddd HH:mm")
            $v = "video estimated"; if ($c.Row.video_start) { $v = "video checked" }
            Say ("  {0,3}  {1,-6} {2}  {3,-4}  {4}  {5,-10} {6}" -f ($i + 1), $c.Row.match, $c.Row.team, $c.Row.alliance, $when, $c.Event.event, $v)
        }
        $pick = Read-Host "Number of the match to open"
        $n = 0
        if (-not [int]::TryParse($pick, [ref]$n) -or $n -lt 1 -or $n -gt $all.Count) { Bad "no match $pick"; Finish 1 }
        $chosen = $all[$n - 1]
    } else {
        $hits = @($all | Where-Object { (Key $_.Row.match) -eq (Key $Match) })
        if ($Team) { $hits = @($hits | Where-Object { "$($_.Row.team)" -eq $Team }) }
        if ($hits.Count -eq 0) {
            Bad "no match '$Match'$(if ($Team) { " for $Team" }) in matches.json. Run open-match with no match for the list."
            Finish 1
        }
        if (@($hits | ForEach-Object { $_.Event.event } | Select-Object -Unique).Count -gt 1) {
            Bad "'$Match' is in more than one event: add -Event (one of $((@($hits | ForEach-Object { $_.Event.event } | Select-Object -Unique)) -join ', '))"
            Finish 1
        }
        if ($hits.Count -gt 1) {
            Say "Both robots played $($hits[0].Row.match):"
            for ($i = 0; $i -lt $hits.Count; $i++) { Say "  $($i + 1)  $($hits[$i].Row.team) ($($hits[$i].Row.alliance))" }
            $pick = Read-Host "Which one (1-$($hits.Count))"
            $n = 0
            if (-not [int]::TryParse($pick, [ref]$n) -or $n -lt 1 -or $n -gt $hits.Count) { Bad "no choice $pick"; Finish 1 }
            $chosen = $hits[$n - 1]
        } else { $chosen = $hits[0] }
    }
    $ev = $chosen.Event; $m = $chosen.Row
    $label = "$($m.match) $($m.team) ($($ev.name))"
    Say ""
    Say "Opening $label"

    # 1. The log, opened at once so AdvantageScope loads it while the clip is cut.
    $log = Get-MatchLog $pins $m
    $edges = Get-AutoEdges $log
    $autoS = $edges[1] - $edges[0]
    $as = Find-AdvantageScope
    if (-not $as) { throw "AdvantageScope is not installed: run update-replay (or setup) first" }
    if ((Get-AdvantageScopeProcesses).Count -gt 0) {
        Set-Content -NoNewline -Path $AsOpenRequest -Value $log   # the open AdvantageScope picks this up and opens it
    } else {
        Start-Process -FilePath $as.Exe -ArgumentList "`"$log`""
    }
    Ok "log $(Split-Path -Leaf $log) (Auto $($autoS.ToString('0.00')) s) is opening in AdvantageScope"
    if ($NoVideo) { Finish 0 }

    # 2. Where Auto starts in the video: matches.json's video_start, else estimated from the stream's start time.
    $url = $ev.video; if ($m.PSObject.Properties["video"] -and $m.video) { $url = $m.video }
    if (-not $url) { throw "matches.json has no video for $($ev.event)" }
    $ffmpeg = Join-Path $Tools "ffmpeg.exe"; $ytdlp = Join-Path $Tools "yt-dlp.exe"; $deno = Join-Path $Tools "deno.exe"
    foreach ($t in $ffmpeg, $ytdlp, $deno) { if (-not (Test-Path $t)) { throw "$(Split-Path -Leaf $t) is missing: run update-replay (or setup) first" } }
    $ytArgs = @("--no-playlist", "--no-warnings", "--js-runtimes", "deno:$deno", "-f",
        "bv*[height<=720][vcodec^=avc1]/bv*[height<=720][vcodec!^=av01]/b[height<=720]/b")
    if ($CookiesFromBrowser) { $ytArgs += @("--cookies-from-browser", $CookiesFromBrowser) }
    $info = $null
    # Only the three fields needed, one per line (yt-dlp's full JSON is large, and Windows PowerShell's
    # ConvertFrom-Json is fragile with it): when the stream started, the chosen format's URL, and its HTTP headers.
    function Get-StreamInfo {
        $lines = @(& $ytdlp @ytArgs -O "%(release_timestamp,timestamp)s" -O "%(url)s" -O "%(http_headers)j" $url)
        if ($LASTEXITCODE -ne 0 -or $lines.Count -lt 3) { throw "yt-dlp could not read $url (see above). Troubleshooting: the README's 'Video won't download'." }
        $start = $null
        if ($lines[0] -match '^\d+(\.\d+)?$') { $start = [double]::Parse($lines[0], [Globalization.CultureInfo]::InvariantCulture) }
        return @{ Start = $start; Url = $lines[1]; Headers = ($lines[2] | ConvertFrom-Json) }
    }

    $estimated = -not $m.video_start
    if ($estimated) {
        $startFile = Join-Path $CacheDir ("stream-start-" + (Get-Sha256Text $url) + ".txt")
        if (Test-Path $startFile) { $streamStart = [double](Get-Content -Raw $startFile) } else {
            $info = Get-StreamInfo
            $streamStart = $info.Start
            if (-not $streamStart) { throw "YouTube gives no start time for ${url}: type this match's video_start into matches.json" }
            Set-Content -NoNewline -Path $startFile -Value $streamStart
        }
        $matchStart = ConvertTo-UnixSeconds $m.start_utc
        $videoStart = $matchStart - [double]$streamStart + [double]$ev.video_shift_s
    } else {
        $videoStart = ConvertFrom-VideoTime $m.video_start
    }
    $videoStart += $Shift
    $from = [Math]::Max([double]0, $videoStart - $PreS)
    $autoInClip = $videoStart - $from
    $how = "from matches.json"; if ($estimated) { $how = "estimated from the stream's start time" }
    Say "  ...     Auto starts at $(ConvertTo-VideoTime $videoStart) in the video ($how$(if ($Shift) { ", shifted $Shift s" }))"

    # 3. The clip, kept per match and per offset.
    $inv = [Globalization.CultureInfo]::InvariantCulture
    $key = Get-Sha256Text ("v2|$url|" + $from.ToString("0.000", $inv) + "|" + $autoInClip.ToString("0.000", $inv) + "|" + $autoS.ToString("0.000", $inv) + "|$PreS|$PostS")
    $clip = Join-Path $ClipsDir ("{0}_{1}_{2}_{3}.mp4" -f $ev.event, ($m.match -replace '[^A-Za-z0-9-]', ''), $m.team, $key.Substring(0, 8))
    if ($Rebuild -or -not (Test-Path $clip)) {
        Say "  ...     cutting the clip from the stream (about a minute; kept for next time)"
        if (-not $info) { $info = Get-StreamInfo }
        $headers = ""
        if ($info.Headers) { foreach ($p in $info.Headers.PSObject.Properties) { $headers += "$($p.Name): $($p.Value)`r`n" } }
        New-Item -ItemType Directory -Force $ClipsDir | Out-Null
        New-Clip $ffmpeg $info.Url $headers $from ($PreS + $PostS) $autoInClip $autoS $clip
    }
    Ok "clip $(Split-Path -Leaf $clip)"

    # 4. The one thing AdvantageScope will not take from a script: choosing the video.
    try { Set-Clipboard -Value $clip } catch { }
    Say ""
    Write-Host "  In AdvantageScope, on the Video tab: click the gray file icon, press Ctrl+V, then Enter." -ForegroundColor Cyan
    Write-Host "  (The clip's path is on the clipboard.) About 45 s after it starts loading, the video locks to the log" -ForegroundColor Cyan
    Write-Host "  by itself: its controls go gray, and from then on it follows the timeline." -ForegroundColor Cyan
    Say ""
    Say "  If the video's robots move later than the 3D robot, by d seconds: open-match $($m.match) $($m.team) -Shift d"
    Say "  (earlier: a negative d). When it looks right, make it permanent in matches.json, for this match:"
    Say "      `"video_start`": `"$(ConvertTo-VideoTime $videoStart)`""
    if ($estimated) {
        Say "  or, for every match of this event that has no video_start yet (one stream, one correction):"
        Say "      `"video_shift_s`": $(([double]$ev.video_shift_s + $Shift).ToString('0.0', $inv))"
    }
    Finish 0
} catch {
    Bad $_.Exception.Message
    Finish 1
}
