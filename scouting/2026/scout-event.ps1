# Partner scouting clips from an event's stream, for a Claude session to trace: each played match of the
# teams below, cut from 3 s before PLAY to 33 s after, 480p, with a .json saying where PLAY is. Built on the
# match-replay tools (open-match), so run the match-replay setup first. Windows PowerShell 5.1.
#
#   .\scout-event.ps1 -Video <YouTube URL>                     # Fruitport (USMIFRQ2): every match played so far, once
#   .\scout-event.ps1 -Video <YouTube URL> -Record -Watch      # at the event: record the live stream, add matches as they are played
#   .\scout-event.ps1 -Event USMIKEQ -Video <URL> -Teams 10255 # any event, any teams (a finished stream needs no -Record)
#   .\scout-event.ps1 -Event USMIBCQ -Video <URL> -Teams 15104,10538 -Only M-11   # one match (FTCScout calls Playoff 11 "M-11")
#
# Where each match comes from, first that works:
#   1. a video open-match already has (sources\ here, or the team Drive's Replay\sources): no download;
#   2. -Record's recording of the live stream (while a YouTube stream is live, its earlier minutes can't be fetched);
#   3. the stream itself, once it is a finished video (as get-scout-clips.ps1 did for Kentwood and Coloma).
# A match of ours that it has to download is saved under open-match's own name, so open-match never downloads it again.
# PLAY: the scoreboard's transition hand (the field's end of Auto) less 30 s, as open-match finds it. FTCScout lists a
# match a few minutes after it is played; -Watch looks again every -EverySeconds.
# Clips go to -Out and, unless -NoPush, to the throwaway branch scout-clips/<EVENT> (never merged; delete after the event).
# Once FTCScout has the schedule it also scouts every partner of 19429 and 20245 (-NoPartners: -Teams only) and pushes
# <EVENT>_trace-order.txt beside the clips: our next partners, soonest first, and which to trace first.
param(
    [string]$Event = "USMIFRQ2",                  # FTCScout's code: Fruitport is USMIFRQ2 (USMIFRQ is Freeland)
    [string[]]$Video = @(),                       # the event's YouTube stream(s); default: matches.json's video for this event
    [string[]]$Teams = @("10255", "11846", "11228", "24371"),
    [int]$Season = 2026,
    [switch]$Record,
    [switch]$Watch,
    [int]$EverySeconds = 180,
    [switch]$NoPush,
    [string]$Out = (Join-Path ([Environment]::GetFolderPath("Desktop")) "scout-clips"),
    [string]$CookiesFromBrowser = "",
    [string[]]$Only = @(),                        # just these matches, FTCScout's names (-Only M-11: Playoff 11)
    [switch]$NoPartners                           # scout only -Teams, not also our partners from the schedule
)
# powershell -File hands "-Teams 15104,10538" over as one string: split lists on commas and spaces here.
$TeamNums = [int[]]@($Teams | ForEach-Object { $_ -split '[,\s]+' } | Where-Object { $_ } | ForEach-Object { [int]$_ })
$Only = @($Only | ForEach-Object { $_ -split '[,\s]+' } | Where-Object { $_ })
$replayLib = Join-Path $env:LOCALAPPDATA "BIOBUZZ-Replay\scripts\Replay.ps1"
if (-not (Test-Path $replayLib)) { throw "The match-replay tools are not installed: run their setup first (tools/advantagescope/replay/README.md, step 1)" }
. $replayLib
$inv = [Globalization.CultureInfo]::InvariantCulture
$OurTeams = @(19429, 20245)
$Remote = "https://github.com/Mona-Shores-FTC-Robotics/biobuzz.git"
$Branch = "scout-clips/$Event"
$ffmpeg = Join-Path $Tools "ffmpeg.exe"; $ytdlp = Join-Path $Tools "yt-dlp.exe"; $deno = Join-Path $Tools "deno.exe"
foreach ($t in $ffmpeg, $ytdlp, $deno) { if (-not (Test-Path $t)) { throw "$(Split-Path -Leaf $t) is missing: run update-replay first" } }
New-Item -ItemType Directory -Force $Out, $SourcesDir, $CacheDir | Out-Null

if ($Video.Count -eq 0) {
    $mj = Join-Path $ReplayDir "matches.json"
    if (Test-Path $mj) {
        foreach ($e in (Read-Json $mj).events) { if ($e.event -eq $Event -and $e.video) { $Video = @($e.video) } }
    }
    if ($Video.Count -eq 0) { throw "No stream for $Event in matches.json: give it with -Video <YouTube URL> (the FiM live page links it)" }
}
$ytArgs = @("--no-playlist", "--no-warnings", "--js-runtimes", "deno:$deno")
if ($CookiesFromBrowser) { $ytArgs += @("--cookies-from-browser", $CookiesFromBrowser) }
$vodFormat = "bv*[height<=720][vcodec^=avc1]/bv*[height<=720][vcodec!^=av01]/b[height<=720]/b"

# ---- FTCScout: every played match with one of the scouted teams in it. The scouted teams are -Teams plus, unless
# -NoPartners, every team the schedule puts on an alliance with 19429 or 20245. $script:Plan is our unplayed matches,
# soonest first, each with its partner: the order to trace them in.
$script:Plan = @()
function Get-PlayedMatches {
    $q = '{ eventByCode(season: ' + $Season + ', code: "' + $Event + '") { name matches { description scheduledStartTime actualStartTime teams { teamNumber alliance station } } } }'
    $body = @{ query = $q } | ConvertTo-Json -Compress
    $r = Invoke-RestMethod -Method Post -Uri "https://api.ftcscout.org/graphql" -ContentType "application/json" -Body $body -TimeoutSec 30
    if (-not $r.data.eventByCode) { throw "FTCScout has no event $Event in season $Season" }
    $all = @($r.data.eventByCode.matches)
    $scout = @($TeamNums)
    $plan = @()
    for ($i = 0; $i -lt $all.Count; $i++) {
        $m = $all[$i]
        foreach ($o in $OurTeams) {
            $me = @($m.teams | Where-Object { [int]$_.teamNumber -eq $o })
            if ($me.Count -eq 0) { continue }
            foreach ($t in @($m.teams | Where-Object { $_.alliance -eq $me[0].alliance -and [int]$_.teamNumber -ne $o })) {
                $partner = [int]$t.teamNumber
                if ($OurTeams -contains $partner) { continue }   # our two robots together: nobody to scout
                if (-not $NoPartners -and $scout -notcontains $partner) { $scout += $partner }
                if ($m.actualStartTime) { continue }
                $when = $null; if ($m.scheduledStartTime) { $when = ConvertTo-UnixSeconds $m.scheduledStartTime }
                $plan += [pscustomobject]@{ Partner = $partner; Ours = $o; Match = [string]$m.description; When = $when; Order = $i }
            }
        }
    }
    # Soonest first: by scheduled time where FTCScout has one, else by the schedule's own order
    $script:Plan = @($plan | Sort-Object @{ Expression = { if ($null -ne $_.When) { $_.When } else { [double]::MaxValue } } }, Order)
    $rows = @()
    foreach ($m in $all) {
        if (-not $m.actualStartTime) { continue }
        $nums = @($m.teams | ForEach-Object { [int]$_.teamNumber })
        $mine = @($scout | Where-Object { $nums -contains $_ })
        if ($mine.Count -eq 0) { continue }
        if ($Only.Count -gt 0 -and $Only -notcontains [string]$m.description) { continue }
        $ours = @($OurTeams | Where-Object { $nums -contains $_ })
        $start = ConvertTo-UnixSeconds $m.actualStartTime   # a string in Windows PowerShell, a DateTime in pwsh 7
        $utc = [DateTimeOffset]::FromUnixTimeMilliseconds([long](1000 * $start)).ToString("yyyy-MM-ddTHH:mm:ss.fffZ", $inv)
        $rows += [pscustomobject]@{ Match = $m.description; StartUtc = $utc; Start = $start; Scouted = $mine; Ours = $ours; Teams = $m.teams }
    }
    return $rows
}

# ---- A finished stream: when it started (cached, as open-match caches it), how long it is, how to read it
$streams = @{}
function Get-Vod([string]$url) {
    if ($streams.ContainsKey($url)) { return $streams[$url] }
    $lines = @(& $ytdlp @ytArgs -f $vodFormat -O "%(release_timestamp,timestamp)s" -O "%(duration)s" -O "%(is_live)s" -O "%(url)s" -O "%(http_headers)j" $url)
    if ($LASTEXITCODE -ne 0 -or $lines.Count -lt 5) { throw "yt-dlp could not read $url (try -CookiesFromBrowser edge)" }
    $s = @{ Url = $url; Live = ($lines[2] -eq "True"); Start = $null; Duration = $null; Media = $lines[3]; Headers = "" }
    if ($lines[0] -match '^\d+(\.\d+)?$') { $s.Start = [double]::Parse($lines[0], $inv) }
    if ($lines[1] -match '^\d+(\.\d+)?$') { $s.Duration = [double]::Parse($lines[1], $inv) }
    $startFile = Join-Path $CacheDir ("stream-start-" + (Get-Sha256Text $url) + ".txt")
    if ($s.Start) { if (-not (Test-Path $startFile)) { Set-Content -NoNewline -Path $startFile -Value $s.Start } }
    elseif (Test-Path $startFile) { $s.Start = [double](Get-Content -Raw $startFile) }
    foreach ($p in ($lines[4] | ConvertFrom-Json).PSObject.Properties) { $s.Headers += "$($p.Name): $($p.Value)`r`n" }
    $streams[$url] = $s
    return $s
}

# ---- -Record: one yt-dlp per stream while it is live, writing a growing file beside open-match's sources. Each start
# is its own recording, <EVENT>-live-<stream>-<date-time>, with a .json holding its wall-clock start.
function Test-Live([string]$url) {
    $l = @(& $ytdlp @ytArgs -O "%(is_live)s" $url)
    return ($LASTEXITCODE -eq 0 -and $l.Count -gt 0 -and $l[-1] -eq "True")
}
function Start-Recording([string]$url) {
    $tag = "{0}-live-{1}" -f $Event, (Get-Sha256Text $url).Substring(0, 8)
    $pidFile = Join-Path $SourcesDir "$tag.pid"
    if (Test-Path $pidFile) {
        $old = Get-Process -Id ([int](Get-Content -Raw $pidFile)) -ErrorAction SilentlyContinue
        if ($old -and -not $old.HasExited) {
            # Alive is not recording: a yt-dlp whose stream URL went stale stays open and writes nothing. Restart one
            # whose newest file has not grown for 2 minutes (a new recording, with its own clock, beside the old).
            $cur = @(Get-ChildItem -Path $SourcesDir -Filter "$tag-*.ts" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending)
            if ($cur.Count -gt 0 -and ((Get-Date) - $cur[0].LastWriteTime).TotalSeconds -lt 120) { return }
            if ($cur.Count -eq 0 -and ((Get-Date) - $old.StartTime).TotalSeconds -lt 120) { return }
            $since = "its start"; if ($cur.Count -gt 0) { $since = $cur[0].LastWriteTime.ToString("HH:mm:ss") }
            Warn "the recording of $url has written nothing since ${since}: restarting it"
            & taskkill.exe /PID $old.Id /T /F 2>&1 | Out-Null
        }
    }
    # Only a live stream: a finished one would come down from its very start, under the wrong clock.
    if (-not (Test-Live $url)) { Warn "$url is not live now: nothing to record (a finished stream is read directly)"; return }
    $base = Join-Path $SourcesDir ("{0}-{1}" -f $tag, (Get-Date).ToString("yyyyMMdd-HHmmss"))
    # Video only: a live YouTube stream has no combined video+audio format (so "b" alone finds nothing), and the
    # clips have no sound anyway.
    $liveFormat = "bv*[height<=720][vcodec^=avc1]/bv*[height<=720]/b[height<=720]/b"
    $a = @($ytArgs + @("-f", $liveFormat, "--no-part", "--hls-use-mpegts", "-o", "$base.ts", $url)) | ForEach-Object { '"' + $_ + '"' }
    $now = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() / 1000.0
    $p = Start-Process -FilePath $ytdlp -ArgumentList $a -WindowStyle Minimized -PassThru
    $null = $p.Handle   # Windows PowerShell 5.1 reports no ExitCode later unless the handle was taken now
    # A yt-dlp that fails closes its window at once, error and all: wait until the file is growing, or say so.
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 1
        if ($p.HasExited) { break }
        if ((Test-Path "$base.ts") -and (Get-Item "$base.ts").Length -gt 0) { break }
    }
    if ($p.HasExited) {
        Bad ("yt-dlp stopped at once (exit {0}): nothing is recording. To see why, run in a window: `"{1}`" {2}" -f $p.ExitCode, $ytdlp, ($a -join " "))
        return
    }
    Set-Content -NoNewline -Path $pidFile -Value $p.Id
    Set-Content -NoNewline -Path "$base.json" -Value (@{ url = $url; started = $now } | ConvertTo-Json -Compress)
    Ok "recording $url into $base.ts (a minimized yt-dlp window: leave it open)"
}
function Get-Recordings {
    $out = @()
    foreach ($j in @(Get-ChildItem -Path $SourcesDir -Filter "$Event-live-*.json" -ErrorAction SilentlyContinue)) {
        $base = $j.FullName.Substring(0, $j.FullName.Length - 5)
        # yt-dlp may add to the name it was given: take the biggest file that starts with it
        $media = @(Get-ChildItem -Path "$base*" -ErrorAction SilentlyContinue | Where-Object { $_.Extension -notin @(".json", ".pid") } |
            Sort-Object Length -Descending)
        if ($media.Count -eq 0) { continue }
        $rec = $null; try { $rec = Read-Json $j.FullName } catch { }
        # Only a recording of one of this run's streams: a test recording of some other stream is not this event.
        if (-not $rec -or $Video -notcontains [string]$rec.url) { continue }
        $out += @{ File = $media[0].FullName; Started = [double]$rec.started }
    }
    return $out
}

# ---- Where a match is: @{ File; At = seconds into File of the FTCScout start; How } or $null for "not yet"
function Find-MatchVideo($row) {
    $key = ($row.Match -replace '[^A-Za-z0-9-]', '')
    $shared = Find-SharedReplay
    # 1. open-match's sources, here or on the team Drive (their .json's "from" is the stream second they start at)
    $dirs = @($SourcesDir); if ($shared) { $dirs += (Join-Path $shared "sources") }
    foreach ($d in $dirs) {
        foreach ($f in @(Get-ChildItem -Path $d -Filter ("{0}_{1}_*.mp4" -f $Event, $key) -ErrorAction SilentlyContinue)) {
            $side = "$($f.FullName).json"
            if (-not (Test-Path $side)) { continue }
            $j = Read-Json $side
            foreach ($url in $Video) {
                if ($j.url -ne $url) { continue }
                $startFile = Join-Path $CacheDir ("stream-start-" + (Get-Sha256Text $url) + ".txt")
                if ($shared -and -not (Test-Path $startFile)) {
                    $sf = Join-Path $shared (Split-Path -Leaf $startFile)
                    if (Test-Path $sf) { Copy-Item -Force $sf $startFile }
                }
                if (-not (Test-Path $startFile)) { continue }
                $at = $row.Start - [double](Get-Content -Raw $startFile) - [double]$j.from
                return @{ File = $f.FullName; At = $at; How = "open-match's video"; StreamFrom = [double]$j.from; Url = $url }
            }
        }
    }
    # 2. the recording: the field's start is (start - recording start) into it, plus the stream's lag
    $now = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() / 1000.0
    foreach ($r in (Get-Recordings | Sort-Object { $_.Started } -Descending)) {
        if ($row.Start -lt $r.Started + 5) { continue }
        if ($now -lt $row.Start + 100) { return $null }   # its Auto may not be on disk yet
        # A recording that stopped (stalled, or the window was closed) before this match's Auto does not have it:
        # skip it, and once the stream is a finished video the match comes from there.
        $end = ([DateTimeOffset](Get-Item $r.File).LastWriteTimeUtc).ToUnixTimeMilliseconds() / 1000.0
        if ($end -lt $row.Start + 80) { continue }
        return @{ File = $r.File; At = $row.Start - $r.Started; How = "the recording" }
    }
    # 3. a finished stream
    foreach ($url in $Video) {
        $s = Get-Vod $url
        if ($s.Live -or -not $s.Start) { continue }
        $at = $row.Start - $s.Start
        if ($at -lt 0 -or ($s.Duration -and $at -gt $s.Duration)) { continue }
        $useName = $row.Scouted[0]; if ($row.Ours.Count -gt 0) { $useName = $row.Ours[0] }
        $name = "{0}_{1}_{2}" -f $Event, $key, $useName
        $src = Join-Path $SourcesDir "$name.mp4"
        $from = [Math]::Max([double]0, $at - $SourcePreS)
        $post = 70.0; if ($row.Ours.Count -gt 0) { $post = $SourcePostS }   # a match of ours: all open-match keeps
        Say "  ...     $($row.Match): downloading its stretch of the stream (a minute or two)"
        New-Source $ffmpeg $s.Media $s.Headers $from ($at - $from + $post) $src
        Set-Content -NoNewline -Path "$src.json" -Value (@{ url = $url; from = $from; marked = $false } | ConvertTo-Json -Compress)
        return @{ File = $src; At = $at - $from; How = "the stream"; StreamFrom = $from; Url = $url }
    }
    if ($Record) { return $null }
    throw "no video covers $($row.Match): the stream is still live (use -Record), or -Video is not this event's stream"
}

# ---- One clip: find PLAY from the hand, cut 3 s before to 33 s after, 480p
function New-ScoutClip($row) {
    $teamsTxt = ($row.Scouted | ForEach-Object { "$_" }) -join "-"
    $name = "{0}_{1}_{2}" -f $Event, ($row.Match -replace '[^A-Za-z0-9-]', ''), $teamsTxt
    $clip = Join-Path $Out "$name.mp4"
    # Done only if this script made it (its .json has play_s): get-scout-clips.ps1 wrote the same names without one.
    if (Test-Path "$clip.json") {
        $old = $null; try { $old = Read-Json "$clip.json" } catch { }
        if ($old -and $null -ne $old.play_s) { return $null }
        Say "  ...     $($row.Match): $name.mp4 is an older clip with no play_s; replacing it"
    }
    $v = Find-MatchVideo $row
    if (-not $v) { Say "  wait    $($row.Match): not on disk yet (the stream is live and the recording has not reached it)"; return $null }
    $look = [Math]::Max([double]0, $v.At + 30 - 45)
    $hand = Find-ScoreboardHand $ffmpeg $v.File $look 110
    if ($null -ne $hand.At) { $play = $hand.At - $FieldAutoS; $how = "scoreboard hand (match {0:N2})" -f $hand.Score }
    else { $play = $v.At; $how = "FTCScout start time (no scoreboard hand found, best {0:N2}): PLAY may be off by several seconds" -f $hand.Score }
    $from = [Math]::Max([double]0, $play - 3)
    & $ffmpeg -hide_banner -loglevel error -y -ss $from.ToString("0.000", $inv) -t "36" -i $v.File `
        -vf "scale=-2:480" -an -c:v libx264 -preset veryfast -crf 26 -pix_fmt yuv420p "$clip.part.mp4"
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path "$clip.part.mp4") -or (Get-Item "$clip.part.mp4").Length -lt 20KB) {
        Remove-Item -Force -ErrorAction SilentlyContinue "$clip.part.mp4"
        Bad "$($row.Match): ffmpeg could not cut it from $($v.How)"; return $null
    }
    Move-Item -Force "$clip.part.mp4" $clip
    $teams = @($row.Teams | ForEach-Object { @{ team = [int]$_.teamNumber; alliance = [string]$_.alliance; station = [string]$_.station } })
    $meta = [ordered]@{ event = $Event; match = $row.Match; field_start_utc = $row.StartUtc; play_s = [Math]::Round($play - $from, 2)
        play_from = $how; video_from = $v.How; teams = $teams; scouted = $row.Scouted }
    # PLAY as a second of the YouTube video (play_s is 3 by construction, so this is the number to check against it)
    if ($null -ne $v.StreamFrom) { $meta.play_stream_s = [Math]::Round($v.StreamFrom + $play, 2); $meta.video = [string]$v.Url }
    Set-Content -Path "$clip.json" -Value ($meta | ConvertTo-Json -Depth 4 -Compress)
    Ok ("{0}: PLAY at {1:N1} s into the clip, from the {2}; video from {3}" -f $row.Match, ($play - $from), $how, $v.How)
    return $name
}

# ---- Which partner to trace first: <EVENT>_trace-order.txt beside the clips, pushed with them. One line per partner,
# in the order we meet them; a partner we meet again later is listed once, at its soonest match.
function Write-TraceOrder {
    if ($script:Plan.Count -eq 0) { return $null }
    $clipsOf = @{}
    foreach ($j in @(Get-ChildItem -Path $Out -Filter "${Event}_*.mp4.json" -ErrorAction SilentlyContinue)) {
        $c = $null; try { $c = Read-Json $j.FullName } catch { }
        if (-not $c -or $null -eq $c.play_s) { continue }
        foreach ($t in @($c.teams)) {
            $k = [int]$t.team
            if (-not $clipsOf.ContainsKey($k)) { $clipsOf[$k] = @() }
            $clipsOf[$k] += [string]$c.match
        }
    }
    $seen = @{}; $lines = @()
    foreach ($p in $script:Plan) {
        if ($seen.ContainsKey($p.Partner)) { continue }
        $seen[$p.Partner] = $true
        $when = "time not posted"
        if ($null -ne $p.When) { $when = [DateTimeOffset]::FromUnixTimeMilliseconds([long](1000 * $p.When)).ToLocalTime().ToString("ddd HH:mm", $inv) }
        $have = @(); if ($clipsOf.ContainsKey($p.Partner)) { $have = @($clipsOf[$p.Partner] | Sort-Object -Unique) }
        $clipTxt = "no clips yet"; if ($have.Count -gt 0) { $clipTxt = "clips: " + ($have -join ", ") }
        $lines += ("{0,2}. {1,-6} with {2} in {3} ({4}); {5}" -f ($lines.Count + 1), $p.Partner, $p.Ours, $p.Match, $when, $clipTxt)
    }
    $first = @($script:Plan | Where-Object { $clipsOf.ContainsKey($_.Partner) } | Select-Object -First 1)
    $head = "Trace first: none of our next partners has a clip yet"
    if ($first.Count -gt 0) { $head = "Trace first: {0} (with {1} in {2}, the soonest partner with a clip)" -f $first[0].Partner, $first[0].Ours, $first[0].Match }
    $file = Join-Path $Out "${Event}_trace-order.txt"
    $text = (@($head, "", "Our partners, soonest first (FTCScout schedule; times are this PC's):") + $lines) -join "`r`n"
    $old = $null; if (Test-Path $file) { $old = Get-Content -Raw $file }
    if ($old -ne "$text`r`n") { Set-Content -Path $file -Value $text; Say "  $head" }
    return $file
}

# ---- Push new clips onto scout-clips/<EVENT>, adding to what is there (a private repo folder; no checkout touched)
function Publish-Clips([string[]]$names, [string[]]$extra = @()) {
    if ($NoPush -or ($names.Count + $extra.Count) -eq 0) { return }
    $ErrorActionPreference = "Continue"   # git talks on stderr; Windows PowerShell would make that fatal under Stop
    $repo = Join-Path $ReplayHome "scout-git"
    if (-not (Test-Path (Join-Path $repo "HEAD"))) { & git init --quiet --bare $repo | Out-Null }
    $index = Join-Path $env:TEMP ("scout-index-" + [Guid]::NewGuid().ToString("n"))
    try {
        $parent = $null
        $there = @(& git ls-remote $Remote "refs/heads/$Branch")
        if ($LASTEXITCODE -ne 0) { Bad "could not reach GitHub; the clips are in $Out and go up on the next round"; return $false }
        if ($there.Count -gt 0 -and $there[0]) {
            & git --git-dir $repo fetch --quiet $Remote "+refs/heads/${Branch}:refs/heads/parent"
            if ($LASTEXITCODE -ne 0) { Bad "could not fetch $Branch; trying again next round"; return $false }
            $parent = (& git --git-dir $repo rev-parse refs/heads/parent).Trim()
        }
        $env:GIT_INDEX_FILE = $index
        if ($parent) { & git --git-dir $repo read-tree $parent | Out-Null } else { & git --git-dir $repo read-tree --empty | Out-Null }
        foreach ($n in $names) {
            foreach ($f in @("$n.mp4", "$n.mp4.json")) {
                $blob = (& git --git-dir $repo hash-object -w -- (Join-Path $Out $f)).Trim()
                & git --git-dir $repo update-index --add --cacheinfo "100644,$blob,$f" | Out-Null
            }
        }
        foreach ($x in $extra) {
            $blob = (& git --git-dir $repo hash-object -w -- $x).Trim()
            & git --git-dir $repo update-index --add --cacheinfo ("100644,$blob," + (Split-Path -Leaf $x)) | Out-Null
        }
        # Nothing new (the trace order unchanged, no clips): no commit
        if ($parent -and (& git --git-dir $repo write-tree).Trim() -eq (& git --git-dir $repo rev-parse "${parent}^{tree}").Trim()) { return $true }
        $tree = (& git --git-dir $repo write-tree).Trim()
        Remove-Item Env:\GIT_INDEX_FILE
        $msg = "Scout clips: $Event, $($names.Count) match(es) (scouting/2026/scout-event.ps1; throwaway, never merge)"
        $ca = @("-c", "user.name=scout-clips", "-c", "user.email=scout-clips@users.noreply.github.com", "--git-dir", $repo, "commit-tree", $tree, "-m", $msg)
        if ($parent) { $ca += @("-p", $parent) }
        $commit = (& git @ca).Trim()
        & git --git-dir $repo push --quiet $Remote "${commit}:refs/heads/$Branch" | Out-Null
        if ($LASTEXITCODE -ne 0) { Bad "could not push $Branch; the clips are in $Out and go up on the next round"; return $false }
        Ok "sent $($names.Count) clip(s) and the trace order to GitHub branch $Branch. Tell the Claude session."
        return $true
    } finally {
        Remove-Item Env:\GIT_INDEX_FILE -ErrorAction SilentlyContinue
        Remove-Item -Force -ErrorAction SilentlyContinue $index
    }
}

# ---- Main
Say "Scouting $Event for $($TeamNums -join ', ') from $($Video -join ', ')"
# -Watch runs all day: ask Windows not to idle-sleep while this window is open (what a video player does; no
# setting changes, and it ends with the process). A closed lid or Sleep from the menu still sleeps.
if ($Watch) {
    try {
        Add-Type -Namespace Scout -Name Power -MemberDefinition '[DllImport("kernel32.dll")] public static extern uint SetThreadExecutionState(uint f);'
        # ES_CONTINUOUS | ES_SYSTEM_REQUIRED
        if ([Scout.Power]::SetThreadExecutionState([uint32]"0x80000001") -ne 0) { Say "  keeping this PC awake while it watches (keep the lid open)" }
    } catch { Warn "could not keep the PC awake: $($_.Exception.Message); turn sleep off by hand" }
}
$unsent = @()
while ($true) {
    if ($Record) { foreach ($url in $Video) { try { Start-Recording $url } catch { Warn "could not start recording ${url}: $($_.Exception.Message)" } } }
    try { $rows = @(Get-PlayedMatches) } catch { Warn "FTCScout: $($_.Exception.Message)"; $rows = @() }
    Say ("  {0} played match(es) with these teams so far" -f $rows.Count)
    if (-not $NoPartners -and $script:Plan.Count -eq 0) { Say "  no unplayed match of ours on FTCScout yet: scouting -Teams only, no trace order" }
    foreach ($row in $rows) {
        try { $n = New-ScoutClip $row; if ($n) { $unsent += $n } }
        catch { Bad "$($row.Match): $($_.Exception.Message)" }
    }
    $order = $null; try { $order = Write-TraceOrder } catch { Warn "trace order: $($_.Exception.Message)" }
    $extra = @(); if ($order) { $extra = @($order) }
    if ($unsent.Count -gt 0 -or $extra.Count -gt 0) {
        if ($NoPush) { $unsent = @() }
        else { try { if (Publish-Clips $unsent $extra) { $unsent = @() } } catch { Bad "push: $($_.Exception.Message)" } }
    }
    if (-not $Watch) { break }
    Say ("  next look in {0} s (Ctrl+C stops; a recording keeps going in its own window)" -f $EverySeconds)
    Start-Sleep -Seconds $EverySeconds
}
