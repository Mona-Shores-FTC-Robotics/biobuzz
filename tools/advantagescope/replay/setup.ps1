# BIOBUZZ match replay: sets a Windows laptop up to watch a match log in AdvantageScope with the robot, the HIVEs
# and the match video in sync. No admin rights; safe to run again (that is also how it updates).
#
# On a laptop with nothing (PowerShell window, paste):
#   irm https://raw.githubusercontent.com/Mona-Shores-FTC-Robotics/biobuzz/master/tools/advantagescope/replay/setup.ps1 | iex
# From a checkout: double-click setup.cmd beside this file. Later, to update everything: update-replay
#
# What it does, each step skipped when already done:
#   1. puts these scripts in %LOCALAPPDATA%\BIOBUZZ-Replay\scripts (from GitHub, or from the checkout it runs in)
#   2. installs the pinned AdvantageScope (replay.json) for this user, unless that version is installed
#   3. fetches ffmpeg, yt-dlp and Deno (to cut match clips from the event stream) into ...\BIOBUZZ-Replay\tools
#   4. installs the robot and HIVE models (the release zip replay.json pins) into AdvantageScope's userAssets
#   5. sets AdvantageScope's window up from match-layout.json: the tabs, with Video beside a 3D window
#   6. downloads the match logs matches.json lists (GitHub's logs/* branches)
#   7. adds the commands open-match and update-replay, and the desktop shortcuts "BIOBUZZ Match Replay" and
#      "BIOBUZZ Prepare All Matches" (every clip cut ahead of time)
param(
    [string]$Ref = "",          # the branch or tag to install from (default: the one used last time, else master)
    [switch]$ResetLayout,       # put the team layout back even if this laptop's AdvantageScope layout was changed
    [switch]$SkipLogs,          # do not download the logs now (open-match fetches each when it is opened)
    [switch]$Installed          # internal: stage 2, running from the installed copy
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
try { [Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12 } catch { }

# The files that make the installed copy: files.txt, read from wherever the files come from, so a file added later
# arrives on the first update. This list is only the fallback for a source without files.txt (match-layout.json
# comes from the folder above).
$Files = @("Replay.ps1", "setup.ps1", "setup.cmd", "open-match.ps1", "open-match.cmd", "replay.json", "matches.json",
    "marker/0-05.png", "marker/0-04.png", "marker/0-03.png", "marker/0-02.png", "marker/scoreboard-hand.gray",
    "../match-layout.json")

# files.txt's lines, without comments and blanks; the built-in list when there are none.
function Read-FileList($lines) {
    $l = @($lines | ForEach-Object { "$_".Trim() } | Where-Object { $_ -and -not $_.StartsWith("#") })
    if ($l.Count -gt 0) { return $l } else { return $Files }
}

if (-not $Installed) {
    # ---- Stage 1: get the scripts, then run the installed setup.ps1 (it may be newer than this one). ----
    $home_ = [Environment]::GetEnvironmentVariable("BIOBUZZ_REPLAY_HOME")
    if (-not $home_) { $home_ = Join-Path $env:LOCALAPPDATA "BIOBUZZ-Replay" }
    $scripts = Join-Path $home_ "scripts"
    $refFile = Join-Path $home_ "ref.txt"
    if (-not $Ref) { if (Test-Path $refFile) { $Ref = (Get-Content -Raw $refFile).Trim() } else { $Ref = "master" } }
    $stage = Join-Path $home_ "scripts.new"
    if (Test-Path $stage) { Remove-Item -Recurse -Force $stage }
    New-Item -ItemType Directory -Force (Join-Path $stage "marker") | Out-Null

    $here = $PSScriptRoot
    $checkout = $here -and (Test-Path (Join-Path $here "..\..\..\.git"))
    Write-Host ""
    Write-Host "BIOBUZZ match replay setup"
    try {
        if ($checkout) {
            Write-Host "  Scripts from this checkout: $here"
            $Files = Read-FileList (Get-Content (Join-Path $here "files.txt") -ErrorAction SilentlyContinue)
            try {
                $list = (Invoke-WebRequest -UseBasicParsing -Uri "https://raw.githubusercontent.com/Mona-Shores-FTC-Robotics/biobuzz/$Ref/tools/advantagescope/replay/files.txt").Content
                if ($list -is [byte[]]) { $list = [Text.Encoding]::UTF8.GetString($list) }
                $Files = Read-FileList ($list -split "`r?`n")
            } catch { }   # an older ref has no files.txt: the list above
            foreach ($f in $Files) { Copy-Item (Join-Path $here $f) (Join-Path $stage ($f -replace '^\.\./', '')) }
        } else {
            Write-Host "  Scripts from GitHub, Mona-Shores-FTC-Robotics/biobuzz at '$Ref'"
            foreach ($f in $Files) {
                $path = ("tools/advantagescope/replay/" + $f) -replace 'replay/\.\./', ''
                $url = "https://raw.githubusercontent.com/Mona-Shores-FTC-Robotics/biobuzz/$Ref/$path"
                Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile (Join-Path $stage ($f -replace '^\.\./', ''))
            }
        }
    } catch {
        Write-Host "  FAILED  could not get the scripts: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "          Check the internet connection (GitHub must be reachable) and run this again."
        Remove-Item -Recurse -Force $stage -ErrorAction SilentlyContinue
        if ($PSCommandPath) { exit 1 } else { return }   # pasted into a window (irm | iex): leave it open
    }
    if (Test-Path $scripts) { Remove-Item -Recurse -Force $scripts }
    Move-Item $stage $scripts
    Set-Content -NoNewline -Path $refFile -Value $Ref
    Write-Host "  OK      scripts in $scripts" -ForegroundColor Green

    # The same PowerShell this runs in: Windows PowerShell on a team laptop, pwsh where that is what ran it.
    if ($PSVersionTable.PSEdition -eq "Core") { $shell = (Get-Command pwsh).Source } else { $shell = Join-Path $PSHOME "powershell.exe" }
    $next = @("-NoProfile", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $scripts "setup.ps1"), "-Installed")
    if ($ResetLayout) { $next += "-ResetLayout" }
    if ($SkipLogs) { $next += "-SkipLogs" }
    & $shell @next
    if ($PSCommandPath) { exit $LASTEXITCODE } else { return }
}

# ---- Stage 2: install, from the installed copy. ----
. (Join-Path $PSScriptRoot "Replay.ps1")
$pins = Get-Pins
$failed = New-Object System.Collections.ArrayList
foreach ($d in @($Tools, $LogsDir, $ClipsDir, $CacheDir, $BinDir, (Join-Path $ReplayHome "downloads"))) { New-Item -ItemType Directory -Force $d | Out-Null }

function Step([string]$name, [scriptblock]$body) {
    try { & $body } catch {
        Bad "$name`: $($_.Exception.Message)"
        [void]$failed.Add($name)
    }
}

# AdvantageScope rewrites its layout and reads its models only at start: it must be closed for this.
$running = Get-AdvantageScopeProcesses
if ($running.Count -gt 0) {
    Warn "Closing AdvantageScope so its models and layout can be updated."
    $running | Stop-Process -Force
    Start-Sleep -Seconds 2
}

# 2. AdvantageScope, the pinned version.
Step "AdvantageScope" {
    $want = $pins.advantagescope.version
    $stamp = Join-Path $ReplayHome "advantagescope.txt"
    $found = Find-AdvantageScope
    $stamped = (Test-Path $stamp) -and ((Get-Content -Raw $stamp).Trim() -eq $want) -and $found
    if ($found -and ($found.Version -eq $want -or $stamped)) {
        Ok "AdvantageScope $want is installed"
        return
    }
    if ($found) { Warn "AdvantageScope $($found.Version) is installed; installing $want, the version this replay is built for." }
    $installer = Join-Path $ReplayHome "downloads\advantagescope-$want.exe"
    if (-not ((Test-Path $installer) -and (Get-Sha256 $installer) -eq $pins.advantagescope.sha256)) {
        Say "  ...     downloading AdvantageScope $want (about 150 MB)"
        Get-Pinned $pins.advantagescope.url $pins.advantagescope.sha256 $installer
    }
    Say "  ...     installing AdvantageScope $want for this user (no admin needed)"
    $p = Start-Process -FilePath $installer -ArgumentList "/S" -Wait -PassThru
    if ($p.ExitCode -ne 0) { throw "the AdvantageScope installer stopped with code $($p.ExitCode)" }
    $found = Find-AdvantageScope
    if (-not $found) { throw "the installer ran, but AdvantageScope.exe is not where it should be" }
    Set-Content -NoNewline -Path $stamp -Value $want
    Remove-Item -Force $installer
    Ok "AdvantageScope $want installed: $($found.Exe)"
}

# 3. ffmpeg (cuts the clips; also given to AdvantageScope so it never asks to download its own), yt-dlp, Deno.
Step "Video tools" {
    $ffmpeg = Join-Path $Tools "ffmpeg.exe"
    if (-not (Test-Stamp $ffmpeg $pins.ffmpeg.sha256)) {
        Say "  ...     downloading ffmpeg ($($pins.ffmpeg.version))"
        Get-Pinned $pins.ffmpeg.url $pins.ffmpeg.sha256 $ffmpeg
        Set-Stamp $ffmpeg $pins.ffmpeg.sha256
    }
    New-Item -ItemType Directory -Force $AsData | Out-Null
    $asFfmpeg = Join-Path $AsData "ffmpeg.exe"
    if (-not ((Test-Path $asFfmpeg) -and (Get-Sha256 $asFfmpeg) -eq $pins.ffmpeg.sha256)) { Copy-Item -Force $ffmpeg $asFfmpeg }

    $ytdlp = Join-Path $Tools "yt-dlp.exe"
    if (-not (Test-Stamp $ytdlp $pins.ytdlp.sha256)) {
        Say "  ...     downloading yt-dlp $($pins.ytdlp.version)"
        Get-Pinned $pins.ytdlp.url $pins.ytdlp.sha256 $ytdlp
        Set-Stamp $ytdlp $pins.ytdlp.sha256
    }

    $deno = Join-Path $Tools "deno.exe"
    if (-not (Test-Stamp $deno $pins.deno.sha256)) {
        Say "  ...     downloading Deno $($pins.deno.version) (yt-dlp needs it for YouTube)"
        $zip = Join-Path $ReplayHome "downloads\deno.zip"
        Get-Pinned $pins.deno.url $pins.deno.sha256 $zip
        $x = Join-Path $ReplayHome "downloads\deno"
        if (Test-Path $x) { Remove-Item -Recurse -Force $x }
        Expand-Archive -Path $zip -DestinationPath $x
        Copy-Item -Force (Join-Path $x "deno.exe") $deno
        Remove-Item -Recurse -Force $x, $zip
        Set-Stamp $deno $pins.deno.sha256
    }
    Ok "ffmpeg, yt-dlp $($pins.ytdlp.version) and Deno $($pins.deno.version) in $Tools"
}

# 4. The robot and HIVE models.
Step "Robot and HIVE models" {
    $tag = $pins.assets.tag
    $stamp = Join-Path $AsUserAssets "biobuzz-replay-assets.txt"
    $complete = -not ($AssetFolders | Where-Object { -not (Test-Path (Join-Path (Join-Path $AsUserAssets $_) "config.json")) })
    if ($complete -and (Test-Path $stamp) -and ((Get-Content -Raw $stamp).Trim() -eq $tag)) {
        Ok "robot and HIVE models are current ($tag)"
        return
    }
    $zip = Join-Path $ReplayHome "downloads\$tag.zip"
    Say "  ...     downloading the models ($tag, about 15 MB)"
    Get-Pinned "https://github.com/$($pins.repo)/releases/download/$tag/$($pins.assets.file)" $pins.assets.sha256 $zip
    $x = Join-Path $ReplayHome "downloads\assets"
    if (Test-Path $x) { Remove-Item -Recurse -Force $x }
    Expand-Archive -Path $zip -DestinationPath $x
    New-Item -ItemType Directory -Force $AsUserAssets | Out-Null
    foreach ($name in $AssetFolders) {
        # Replace only these three folders: anything else in userAssets is someone's own.
        $old = Join-Path $AsUserAssets $name
        if (Test-Path $old) { Remove-Item -Recurse -Force $old }
        Copy-Item -Recurse (Join-Path $x $name) $AsUserAssets
    }
    Remove-Item -Recurse -Force $x, $zip
    Set-Content -NoNewline -Path $stamp -Value $tag
    Ok "robot and HIVE models installed ($tag): 'BIOBUZZ Robot', 'BIOBUZZ HIVE', '2026-2027 Field (HIVE sim)'"
}

# 5. The window: the team layout in the main window with the Video tab showing, and the 3D view in a second window
# beside it. Written into AdvantageScope's saved state, so nobody imports anything.
Step "Layout" {
    $layoutFile = Join-Path $ReplayDir "match-layout.json"
    $state = Get-StateFile $pins
    $stamp = Join-Path $ReplayHome "layout.sha256"
    $hash = Get-Sha256 $layoutFile
    $same = (Test-Path $state) -and (Test-Path $stamp) -and ((Get-Content -Raw $stamp).Trim() -eq $hash)
    if ($same -and -not $ResetLayout) {
        Ok "layout is current (open-match also puts it back each time it opens a match)"
        return
    }
    if (Test-Path $state) {
        $backup = $state -replace '\.json$', ("-before-biobuzz-" + (Get-Date -Format "yyyyMMdd-HHmmss") + ".json")
        Copy-Item $state $backup
        Warn "saved this laptop's previous AdvantageScope layout as $(Split-Path -Leaf $backup)"
    }
    Write-TeamLayout $pins
    Copy-Item -Force $layoutFile (Join-Path $ReplayHome "biobuzz-match-layout.json")
    Set-Content -NoNewline -Path $stamp -Value $hash
    Ok "layout set: Field, 3D, Video, Limelight, Loop and Launcher tabs, with 3D in its own window beside Video"
}

# 6. The logs.
Step "Match logs" {
    if ($SkipLogs) { Warn "logs skipped (-SkipLogs): open-match fetches each one when it is opened"; return }
    $doc = Read-Json (Join-Path $ReplayDir "matches.json")
    $have = 0; $got = 0
    foreach ($e in $doc.events) {
        foreach ($m in $e.matches) {
            if (Test-Path (Get-LogPath $m.log)) { $have++ } else { [void](Get-MatchLog $pins $m); $got++ }
        }
    }
    Ok "match logs: $got downloaded, $have already here ($LogsDir)"
}

# 7. The commands and the shortcut.
Step "Commands" {
    $ps = "powershell -NoProfile -ExecutionPolicy Bypass -File"
    Set-Content -Encoding ASCII -Path (Join-Path $BinDir "open-match.cmd") -Value @(
        "@echo off", "rem BIOBUZZ match replay: open-match Q-14 (or no match to pick one from a list).",
        "$ps `"$(Join-Path $Scripts 'open-match.ps1')`" %*",
        "rem If it stopped with an error, keep the window open to read it (also in last-run.log).",
        "if errorlevel 1 pause")
    Set-Content -Encoding ASCII -Path (Join-Path $BinDir "update-replay.cmd") -Value @(
        "@echo off", "rem BIOBUZZ match replay: update the scripts, AdvantageScope, the models, the layout and the logs.",
        "$ps `"$(Join-Path $Scripts 'setup.ps1')`" %*", "pause")
    if (-not $onWindows) { Ok "commands written to $BinDir (not on Windows: PATH and shortcut skipped)"; return }
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    if (-not $userPath) { $userPath = "" }
    if (-not (($userPath -split ";") -contains $BinDir)) {
        [Environment]::SetEnvironmentVariable("Path", (($userPath.TrimEnd(";") + ";" + $BinDir).TrimStart(";")), "User")
        Warn "added $BinDir to your PATH: open a new terminal window to use open-match there"
    }
    $desktop = [Environment]::GetFolderPath("Desktop")
    $lnk = (New-Object -ComObject WScript.Shell).CreateShortcut((Join-Path $desktop "BIOBUZZ Match Replay.lnk"))
    $lnk.TargetPath = Join-Path $BinDir "open-match.cmd"
    $lnk.WorkingDirectory = $ReplayHome
    $as = Find-AdvantageScope
    if ($as) { $lnk.IconLocation = "$($as.Exe),0" }
    $lnk.Description = "Pick a BIOBUZZ match and watch it in AdvantageScope with its video"
    $lnk.Save()
    $prep = (New-Object -ComObject WScript.Shell).CreateShortcut((Join-Path $desktop "BIOBUZZ Prepare All Matches.lnk"))
    $prep.TargetPath = Join-Path $BinDir "open-match.cmd"
    $prep.Arguments = "-All"
    $prep.WorkingDirectory = $ReplayHome
    if ($as) { $prep.IconLocation = "$($as.Exe),0" }
    $prep.Description = "Download every match's log and video clip now, so each opens at once later"
    $prep.Save()
    Ok "commands open-match and update-replay, and desktop shortcuts 'BIOBUZZ Match Replay' and 'BIOBUZZ Prepare All Matches'"
}

Say ""
if ($failed.Count -eq 0) {
    Write-Host "Setup finished. Next: double-click 'BIOBUZZ Match Replay' on the desktop and pick a match." -ForegroundColor Green
    exit 0
}
Write-Host "Setup finished with problems in: $($failed -join ', '). Fix what it says above and run it again;" -ForegroundColor Red
Write-Host "the steps that worked are kept. Troubleshooting: tools/advantagescope/replay/README.md" -ForegroundColor Red
exit 1
