# Opens one BIOBUZZ match in AdvantageScope: its log, and a clip of the event stream that syncs itself to it.
#
#   open-match                 pick from a list
#   open-match Q-14            (or Q14, q14, M-7) one robot played it: that one; both: it asks which
#   open-match M-7 19429       that robot's log of the match
#   open-match Q-14 -Shift 1.5 the video 1.5 s later than matches.json says (to try a correction; see the README)
#   open-match -All            get every match ready (logs and clips) without opening any: before a meeting
#   open-match Q-14 -Frames    pictures every 0.1 s around Auto's start, named by offset: the first with our lights
#                              off is the -Shift to use (our driver's PLAY lag)
#
# The clip is cut from the stream the first time a match is opened (about three minutes) and kept in
# %LOCALAPPDATA%\BIOBUZZ-Replay\clips, so it never downloads twice. Then AdvantageScope opens, fresh, with the team
# layout (an open one is closed and reopened). It cannot be told to load a video, so the clip's path goes on the
# clipboard: Video tab, file icon, Ctrl+V, Enter. The clip carries a countdown where
# AdvantageScope reads a match timer, so it locks the video to the log by itself; nobody scrubs or syncs by hand.
param(
    [Parameter(Position = 0)][string]$Match = "",
    [Parameter(Position = 1)][string]$Team = "",
    [double]$Shift = 0,                  # seconds added to this match's video_start, for a try
    [string]$Event = "",                 # an event code (USMIBEQ) when two events have the same match name
    [switch]$NoVideo,                    # the log only
    [switch]$Rebuild,                    # cut the clip again from this match's video (to download that too, delete it from sources\)
    [switch]$All,                        # every match's log and clip, ready for later; opens nothing
    [switch]$Frames,                     # pictures around Auto's start, to read our driver's PLAY lag off the lights
    [string]$CookiesFromBrowser = ""     # firefox / edge / chrome: when YouTube says "sign in to confirm"
)

. (Join-Path $PSScriptRoot "Replay.ps1")
# Everything this run prints also goes to last-run.log, so a window that closed too soon can still be read.
try { New-Item -ItemType Directory -Force $ReplayHome | Out-Null; Start-Transcript -Path (Join-Path $ReplayHome "last-run.log") -Force | Out-Null } catch { }
$menu = -not $Match -and -not $All
$pause = -not $Match   # started from a shortcut: keep the window open to read

function Finish([int]$code) {
    if ($pause) { Read-Host "Press Enter to close" | Out-Null }
    exit $code
}

# The match's clip, in three steps:
#   1. the source: this match's stretch of the event video, unmarked, from 40 s before Auto's estimated start to 40 s
#      after the match. Downloaded once and kept in sources\ (a clip made before sources existed is adopted instead);
#      every later step works from it, without the internet.
#   2. where Auto is in it: matches.json's video_start if someone checked it; otherwise our robot's own light (it
#      leaves the alliance colour the instant our driver pressed PLAY, which is the log's Auto start), searched for
#      around the scoreboard's start (the FiM overlay's 8 s transition hand is the field's end of Auto, 30 s after its
#      start). When the light is out of shot, the scoreboard alone, less the overlay's lag. Only if no hand is found does it fall back to the estimate from the stream's
#      start time.
#   3. the clip: cut from the source with the sync countdown burnt in. Kept per offset, so a -Shift re-cuts locally.
# Returns @{ Clip; VideoStart; How }. Throws when there can be no video.
function Get-MatchClip($pins, $ev, $m, [double]$autoS, [string]$log) {
    $inv = [Globalization.CultureInfo]::InvariantCulture
    $url = $ev.video; if ($m.PSObject.Properties["video"] -and $m.video) { $url = $m.video }
    if (-not $url) { throw "matches.json has no video for $($ev.event)" }
    $ffmpeg = Join-Path $Tools "ffmpeg.exe"; $ytdlp = Join-Path $Tools "yt-dlp.exe"; $deno = Join-Path $Tools "deno.exe"
    foreach ($t in $ffmpeg, $ytdlp, $deno) { if (-not (Test-Path $t)) { throw "$(Split-Path -Leaf $t) is missing: run update-replay (or setup) first" } }
    $ytArgs = @("--no-playlist", "--no-warnings", "--js-runtimes", "deno:$deno", "-f",
        "bv*[height<=720][vcodec^=avc1]/bv*[height<=720][vcodec!^=av01]/b[height<=720]/b")
    if ($CookiesFromBrowser) { $ytArgs += @("--cookies-from-browser", $CookiesFromBrowser) }
    $script:streamInfo = $null
    # Only the three fields needed, one per line (yt-dlp's full JSON is large, and Windows PowerShell's
    # ConvertFrom-Json is fragile with it): when the stream started, the chosen format's URL, and its HTTP headers.
    function Get-StreamInfo {
        if ($script:streamInfo) { return $script:streamInfo }
        $lines = @(& $ytdlp @ytArgs -O "%(release_timestamp,timestamp)s" -O "%(url)s" -O "%(http_headers)j" $url)
        if ($LASTEXITCODE -ne 0 -or $lines.Count -lt 3) { throw "yt-dlp could not read $url (see above). Troubleshooting: the README's 'Video won't download'." }
        $start = $null
        if ($lines[0] -match '^\d+(\.\d+)?$') { $start = [double]::Parse($lines[0], $inv) }
        $script:streamInfo = @{ Start = $start; Url = $lines[1]; Headers = ($lines[2] | ConvertFrom-Json) }
        return $script:streamInfo
    }

    # Where Auto starts in the video, estimated: the field's start time (FTCScout) after the stream's own start.
    $shared = Find-SharedReplay
    $startName = "stream-start-" + (Get-Sha256Text $url) + ".txt"
    $startFile = Join-Path $CacheDir $startName
    New-Item -ItemType Directory -Force $CacheDir | Out-Null
    if (-not (Test-Path $startFile) -and $shared -and (Test-Path (Join-Path $shared $startName))) {
        Copy-Item -Force (Join-Path $shared $startName) $startFile
    }
    if (Test-Path $startFile) { $streamStart = [double](Get-Content -Raw $startFile) } else {
        $streamStart = (Get-StreamInfo).Start
        if (-not $streamStart) { throw "YouTube gives no start time for ${url}: type this match's video_start into matches.json" }
        Set-Content -NoNewline -Path $startFile -Value $streamStart
    }
    if ($shared -and -not (Test-Path (Join-Path $shared $startName))) {
        try { Copy-Whole $startFile (Join-Path $shared $startName) } catch { }
    }
    $estimate = (ConvertTo-UnixSeconds $m.start_utc) - [double]$streamStart + [double]$ev.video_shift_s
    if ($m.video_start) { $estimate = ConvertFrom-VideoTime $m.video_start }
    if ($estimate -lt 0) { throw "Auto would start $((-$estimate).ToString('0')) s before the video does: is this event's video right?" }

    # 1. The source.
    $name = "{0}_{1}_{2}" -f $ev.event, ($m.match -replace '[^A-Za-z0-9-]', ''), $m.team
    $source = Join-Path $SourcesDir "$name.mp4"
    $sidecar = "$source.json"
    $sharedSource = $null; $sharedSidecar = $null
    if ($shared) {
        $sharedSource = Join-Path (Join-Path $shared "sources") "$name.mp4"
        $sharedSidecar = "$sharedSource.json"
    }
    if (-not ((Test-Path $source) -and (Test-Path $sidecar)) -and $sharedSource -and (Test-Path $sharedSidecar) -and (Test-Path $sharedSource)) {
        # On the team Drive (its .json is written last, so the video beside it is whole): copy it, no YouTube.
        New-Item -ItemType Directory -Force $SourcesDir | Out-Null
        Say "  ...     copying this match's video from the team Google Drive"
        Copy-Whole $sharedSource $source
        Copy-Whole $sharedSidecar $sidecar
        Ok "this match's video came from the team Drive: nothing to download"
    }
    if (-not ((Test-Path $source) -and (Test-Path $sidecar))) {
        New-Item -ItemType Directory -Force $SourcesDir | Out-Null
        # A clip from before sources existed (cut at the estimate, 10 s early, 180 s long) holds the whole match:
        # adopt it rather than download again. Its countdown is covered over when the clip is cut from it.
        $oldFrom = [Math]::Max([double]0, $estimate - $PreS)
        $oldKey = Get-Sha256Text ("v2|$url|" + $oldFrom.ToString("0.000", $inv) + "|" + ($estimate - $oldFrom).ToString("0.000", $inv) + "|" + $autoS.ToString("0.000", $inv) + "|$PreS|$PostS")
        $old = Join-Path $ClipsDir ("{0}_{1}.mp4" -f $name, $oldKey.Substring(0, 8))
        $marked = $false
        if (Test-Path $old) {
            Move-Item -Force $old $source
            $sourceFrom = $oldFrom
            $marked = $true   # it carries the old countdown and black boxes: fine here, never shared
            Ok "kept the clip made earlier as this match's source: nothing to download"
        } else {
            $sourceFrom = [Math]::Max([double]0, $estimate - $SourcePreS)
            Say "  ...     downloading this match's video (about three minutes, once per match; nothing prints until it is done)"
            $info = Get-StreamInfo
            $headers = ""
            if ($info.Headers) { foreach ($p in $info.Headers.PSObject.Properties) { $headers += "$($p.Name): $($p.Value)`r`n" } }
            New-Source $ffmpeg $info.Url $headers $sourceFrom ($estimate - $sourceFrom + $SourcePostS) $source
        }
        Set-Content -NoNewline -Path $sidecar -Value (@{ url = $url; from = $sourceFrom; marked = $marked } | ConvertTo-Json -Compress)
    }
    # Share it, once: the first laptop to have a match's video puts it on the team Drive for the rest. The .json last,
    # since it is what tells another laptop the video is there. A full or offline Drive costs only the sharing.
    # Only clean video: a source adopted from an old clip has the countdown and black boxes drawn on it.
    # An info file from before "marked" existed: an adopted old clip starts $PreS (10 s) before the estimate, a
    # downloaded video $SourcePreS (40 s) before it.
    $isMarked = $true
    try {
        $j = Get-Content -Raw $sidecar | ConvertFrom-Json
        if ($j.PSObject.Properties["marked"]) { $isMarked = [bool]$j.marked }
        else { $isMarked = [Math]::Abs([double]$j.from - [Math]::Max([double]0, $estimate - $PreS)) -lt 0.5 }
    } catch { }
    # A marked video can't be shared, so with the team Drive here, swap it for a clean one: the Drive's copy if it
    # has one, else a fresh download (a minute). If neither works, keep the marked one: it still plays here.
    if ($isMarked -and $sharedSource) {
        try {
            $cleanOnDrive = $false
            if ((Test-Path $sharedSidecar) -and (Test-Path $sharedSource)) {
                $sj = Get-Content -Raw $sharedSidecar | ConvertFrom-Json
                $cleanOnDrive = -not ($sj.PSObject.Properties["marked"] -and [bool]$sj.marked)
            }
            if ($cleanOnDrive) {
                Say "  ...     this match's video here has the old sync marks drawn on it: taking the team Drive's clean copy"
                Copy-Whole $sharedSource $source
                Copy-Whole $sharedSidecar $sidecar
            } else {
                Say "  ...     this match's video here has the old sync marks drawn on it: downloading a clean copy to share (about three minutes)"
                $cleanFrom = [Math]::Max([double]0, $estimate - $SourcePreS)
                $info = Get-StreamInfo
                $headers = ""
                if ($info.Headers) { foreach ($p in $info.Headers.PSObject.Properties) { $headers += "$($p.Name): $($p.Value)`r`n" } }
                New-Source $ffmpeg $info.Url $headers $cleanFrom ($estimate - $cleanFrom + $SourcePostS) "$source.clean.mp4"
                Move-Item -Force "$source.clean.mp4" $source
                Set-Content -NoNewline -Path $sidecar -Value (@{ url = $url; from = $cleanFrom; marked = $false } | ConvertTo-Json -Compress)
            }
            $isMarked = $false
            Ok "this match's video is now clean"
        } catch {
            Remove-Item -Force "$source.clean.mp4", "$source.clean.mp4.part.mp4" -ErrorAction SilentlyContinue
            Warn "kept the marked copy, which still plays here but is not shared: $($_.Exception.Message)"
        }
    }
    if ($sharedSource -and -not $isMarked -and -not (Test-Path $sharedSidecar)) {
        try {
            Say "  ...     putting this match's video on the team Google Drive for the other laptops"
            Copy-Whole $source $sharedSource
            Copy-Whole $sidecar $sharedSidecar
        } catch { Warn "could not put this match's video on the team Drive ($($_.Exception.Message)); it is still here" }
    }
    $meta = Get-Content -Raw $sidecar | ConvertFrom-Json
    $sourceFrom = [double]$meta.from

    # 2. Where Auto starts in the source.
    if ($m.video_start) {
        $autoAt = $estimate - $sourceFrom
        $how = "from matches.json"
    } else {
        # The field's Auto ends about 30 s after the estimate; look 25 s either side.
        $look = [Math]::Max([double]0, $estimate + 30 - 25 - $sourceFrom)
        $hand = Find-ScoreboardHand $ffmpeg $source $look 50
        if ($null -ne $hand.At) {
            # The field's Auto is always 30 s, so it started 30 s before the hand (its end is no use: an Auto stopped
            # early, Q-27 at Belding, ends early). The overlay shows the hand $ScoreboardLagS late, so our PLAY is about
            # there; our own light then says exactly where.
            $fieldStart = $hand.At - $FieldAutoS
            $autoAt = $fieldStart - $ScoreboardLagS
            $how = "from the scoreboard (match {0:N2}): our light was not checked" -f $hand.Score
            $pattern = Get-LightPattern $log
            if (-not $pattern.Ok) {
                $how = "from the scoreboard (match {0:N2}); not from our light: {1}" -f $hand.Score, $pattern.Why
            } else {
                $light = Find-OurLight $ffmpeg $source $autoAt $pattern.Blue $pattern.OnS $pattern.OffS
                if ($null -ne $light.At) {
                    $autoAt = $light.At
                    $how = "from our robot's light at ({0},{1}), score {2:N0}: it went off {3:0.00} s before the scoreboard's start" -f `
                        $light.X, $light.Y, $light.Score, ($fieldStart - $light.At)
                } else {
                    $how = "from the scoreboard (match {0:N2}); our light was not found (best {1:N0} of 40 needed: our robot out of shot, or hidden)" -f `
                        $hand.Score, $light.Score
                }
            }
        } else {
            $autoAt = $estimate - $sourceFrom
            $how = "estimated from the stream's start time: the scoreboard's transition hand was not found (best match {0:N2})" -f $hand.Score
        }
    }
    $autoAt += $Shift
    $videoStart = $sourceFrom + $autoAt
    Say "  ...     Auto starts at $(ConvertTo-VideoTime $videoStart) in the video ($how$(if ($Shift) { ", shifted $Shift s" }))"

    # 3. The clip.
    $from = [Math]::Max([double]0, $autoAt - $PreS)
    $autoInClip = $autoAt - $from
    $key = Get-Sha256Text ("v3|$url|" + $sourceFrom.ToString("0.000", $inv) + "|" + $from.ToString("0.000", $inv) + "|" + $autoInClip.ToString("0.000", $inv) + "|" + $autoS.ToString("0.000", $inv) + "|$PreS|$PostS")
    $clip = Join-Path $ClipsDir ("{0}_{1}.mp4" -f $name, $key.Substring(0, 8))
    if ($Rebuild -or -not (Test-Path $clip)) {
        Say "  ...     cutting the clip from this match's video (no download)"
        New-Item -ItemType Directory -Force $ClipsDir | Out-Null
        New-Clip $ffmpeg $source "" $from ($PreS + $PostS) $autoInClip $autoS $clip
        # This match's earlier clips (other offsets) go: the source can cut any of them again in seconds.
        Get-ChildItem $ClipsDir -Filter "${name}_*.mp4" | Where-Object { $_.FullName -ne $clip } | Remove-Item -Force -ErrorAction SilentlyContinue
        Ok "clip $(Split-Path -Leaf $clip)"
    } else {
        Ok "clip $(Split-Path -Leaf $clip) (already made)"
    }
    return @{ Clip = $clip; VideoStart = $videoStart; How = $how; Source = $source; SourceAutoAt = $autoAt }
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
    $rows = New-Object System.Collections.ArrayList
    foreach ($e in $doc.events) { foreach ($m in $e.matches) { [void]$rows.Add([pscustomobject]@{ Event = $e; Row = $m }) } }
    if ($Event) { $rows = @($rows | Where-Object { $_.Event.event -eq $Event }) }

    if ($All) {
        Say ""
        Say "Getting every match ready: $($rows.Count) logs and clips (about three minutes for each match whose video is not downloaded yet)"
        $ready = 0; $failed = 0
        foreach ($c in $rows) {
            Say ""
            Say "$($c.Row.match) $($c.Row.team)"
            try {
                $log = Get-MatchLog $pins $c.Row $c.Event
                $e = Get-AutoEdges $log
                [void](Get-MatchClip $pins $c.Event $c.Row ($e[1] - $e[0]) $log)
                $ready++
            } catch {
                Bad $_.Exception.Message
                $failed++
            }
        }
        Say ""
        if ($failed -eq 0) {
            Write-Host "All $ready matches are ready: opening any of them downloads nothing." -ForegroundColor Green
            Finish 0
        }
        Write-Host "$ready ready, $failed not (above). Run this again to retry those; the ready ones are kept." -ForegroundColor Red
        Finish 1
    }

    function Key([string]$s) { return ($s.ToUpperInvariant() -replace '[^A-Z0-9]', '') }

    if ($menu) {
        Say ""
        Say "BIOBUZZ matches"
        for ($i = 0; $i -lt $rows.Count; $i++) {
            $c = $rows[$i]
            $when = [DateTimeOffset]::FromUnixTimeMilliseconds([long](1000 * (ConvertTo-UnixSeconds $c.Row.start_utc))).ToLocalTime().ToString("ddd HH:mm")
            $v = "synced: scoreboard"; if ($c.Row.video_start) { $v = "synced: checked" }
            Say ("  {0,3}  {1,-6} {2}  {3,-4}  {4}  {5,-10} {6}" -f ($i + 1), $c.Row.match, $c.Row.team, $c.Row.alliance, $when, $c.Event.event, $v)
        }
        $pick = Read-Host "Number of the match to open"
        $n = 0
        if (-not [int]::TryParse($pick, [ref]$n) -or $n -lt 1 -or $n -gt $rows.Count) { Bad "no match $pick"; Finish 1 }
        $chosen = $rows[$n - 1]
    } else {
        $hits = @($rows | Where-Object { (Key $_.Row.match) -eq (Key $Match) })
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

    # 1. The log. AdvantageScope opens last, once the clip is ready, so its window and the instructions come together.
    $log = Get-MatchLog $pins $m $ev
    $edges = Get-AutoEdges $log
    $autoS = $edges[1] - $edges[0]
    $as = Find-AdvantageScope
    if (-not $as) { throw "AdvantageScope is not installed: run update-replay (or setup) first" }
    Ok "log $(Split-Path -Leaf $log) (Auto $($autoS.ToString('0.00')) s)"

    # Every match starts from the team layout (Video beside the 3D window), however the last one was left: a closed
    # 3D window or a moved tab would otherwise stick, since AdvantageScope saves its windows as they are.
    function Open-AdvantageScope {
        if (Stop-AdvantageScope) { Say "  ...     closing the open AdvantageScope, to reopen it with the team layout" }
        Write-TeamLayout $pins
        Start-Process -FilePath $as.Exe -ArgumentList "`"$log`""
        Ok "AdvantageScope is opening on $label"
    }
    if ($NoVideo) { Open-AdvantageScope; Finish 0 }

    try {
        $made = Get-MatchClip $pins $ev $m $autoS $log
    } catch {
        # No clip: still show the log, then say why.
        Bad "no video: $($_.Exception.Message)"
        Open-AdvantageScope
        Finish 1
    }
    if ($Frames) {
        # From the unmarked source (the clip's black boxes could hide our robot). Named by the -Shift each one
        # would mean: the scoreboard's placement is 0, and the first picture with our lights changed is the shift.
        $dir = Join-Path (Join-Path $ReplayHome "frames") ("{0}_{1}_{2}" -f $ev.event, ($m.match -replace '[^A-Za-z0-9-]', ''), $m.team)
        $base = $made.SourceAutoAt - $Shift
        Export-Frames (Join-Path $Tools "ffmpeg.exe") $made.Source ($base - 0.5) 31 0.1 -0.5 $dir
        Ok "pictures every 0.1 s from 0.5 s before to 2.5 s after where the scoreboard puts Auto's start: $dir"
        Say ""
        Write-Host "  Flip through them (Photos, arrow keys). The first one where our lights have changed (alliance colour" -ForegroundColor Cyan
        Write-Host "  to off, or to green) is named by the shift to use: +0.40s means  open-match $($m.match) $($m.team) -Shift 0.4" -ForegroundColor Cyan
        if ($onWindows) { Start-Process explorer.exe $dir }
        Finish 0
    }
    Open-AdvantageScope
    $clip = $made.Clip; $videoStart = $made.VideoStart
    $inv = [Globalization.CultureInfo]::InvariantCulture

    # 4. The one thing AdvantageScope will not take from a script: choosing the video.
    try { Set-Clipboard -Value $clip } catch { }
    Say ""
    Write-Host "  In AdvantageScope, on the Video tab: click the gray file icon, press Ctrl+V, then Enter." -ForegroundColor Cyan
    Write-Host "  (The clip's path is on the clipboard.) About 45 s after it starts loading, the video locks to the log" -ForegroundColor Cyan
    Write-Host "  by itself: its controls go gray, and from then on it follows the timeline." -ForegroundColor Cyan
    Say ""
    Say "  Synced from the scoreboard, it can still be a few tenths off: how fast our driver pressed PLAY. If the"
    Say "  video's robot moves d seconds after the 3D robot: open-match $($m.match) $($m.team) -Shift d (before it: -d)."
    Say "  That re-cuts on this laptop, with no download. To keep it, put this match's line in matches.json:"
    Say "      `"video_start`": `"$(ConvertTo-VideoTime $videoStart)`""
    Finish 0
} catch {
    Bad $_.Exception.Message
    Finish 1
}
