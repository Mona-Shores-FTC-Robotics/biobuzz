# Watch a match: log, robot, HIVEs and video, in sync

For any Windows laptop, no admin rights, nothing installed beforehand.

## 1. Set the laptop up (once)

Open **PowerShell** (Start menu → type `powershell` → Enter), paste this line, press Enter:

```powershell
irm https://raw.githubusercontent.com/Mona-Shores-FTC-Robotics/biobuzz/master/tools/advantagescope/replay/setup.ps1 | iex
```

It takes a few minutes and ends with a green **Setup finished**. Anything red says what to fix; running the
line again keeps what worked and retries the rest. (In a checkout, double-clicking `setup.cmd` here does the same.)

## 2. Pick a match

Double-click **BIOBUZZ Match Replay** on the desktop and type the number of a match from the list, or in a new
terminal type `open-match Q-14`. The first time a match is opened, its stretch of the event stream is downloaded
(about a minute) and kept, so it never downloads again. The clip is lined up from the stream's own scoreboard: the
hand it shows during the 8 s transition marks the end of the field's 30 s Auto, so the log's Auto start is put
30 s before it. Then
AdvantageScope opens on the match's log, with the Video tab and the 3D window side by side. That layout is put
back every time a match opens, however the last one was left.

On AdvantageScope's **Video** tab, click the gray file icon, press **Ctrl+V** (the clip's path is on the
clipboard), then **Enter**. About 45 s into loading, the video locks to the log by itself: robot and HIVEs move in
the 3D window beside the video, in step with it.

**Before a meeting**, double-click **BIOBUZZ Prepare All Matches** (or run `open-match -All`): it downloads
every match's log and clip without opening anything, so each match opens at once later, with no internet needed.

## Keeping it current

When the robot model, the HIVE model, the layout, the match list or AdvantageScope's pinned version changes,
every laptop runs `update-replay` (or the line from step 1 again). Matches and video offsets are re-read from
GitHub each time `open-match` runs, so a fixed offset reaches every laptop without even that.

## Troubleshooting

**Models missing** (a gray box instead of the robot, no HIVE, or "2026-2027 Field (HIVE sim)" not in the list):
run `update-replay`, then restart AdvantageScope; it reads models only at start. Still missing: check
`%APPDATA%\AdvantageScope\userAssets` has `Robot_BIOBUZZ`, `Robot_BIOBUZZHive` and `Field3d_BIOBUZZHiveSim`.
The 2D field downloads the first time a Field tab shows it, so that needs internet once.

**Wrong orientation** (robot backwards, sideways, or off its spot): first check the 3D tab's field is
**2026-2027 Field (HIVE sim)** and the robot model is **BIOBUZZ Robot**; the layout sets both, and
opening any match puts the layout back. If it is still wrong, it is the model or the log, not this setup: the
model's origin and axes are in `cad/advantagescope/README.md`, and the log writes `/Odometry/Robot3d` in
AdvantageScope's frame (`logging/AdvantageScopeFrame`).

**Video out of sync**: `open-match` says how it placed Auto. "From the scoreboard" puts the log's start of Auto
30 s before the frame where the transition's hand appears (the field's Auto start): that is right to within how
fast our driver pressed PLAY, a few tenths of a second, even when our Auto ended early. To fix that for a match you study closely, compare one moment of *our* robot in both views
(it leaving its start, a first shot): if the video's robot moves *d* seconds after the 3D one, run
`open-match Q-14 -Shift d` (before it: a negative *d*). That re-cuts the clip on the laptop in about half a
minute, no download. When it looks right, put the `video_start` line it prints into `matches.json` and commit it.
"Estimated from the stream's start time" means the hand was not found (another overlay, or the match is not where
expected): that can be seconds off; `video_shift_s` moves every estimate of an event at once.

**The video never locks**: wait until the blue loading bar passes Auto's end (about 45 s of video). If it never
locks, run `open-match Q-14 -Rebuild` and load the new clip; the clip must be the one `open-match` made, since the
lock reads a countdown it draws over the stream's bottom-centre until Auto ends. To download a match's video
again, delete it from `%LOCALAPPDATA%\BIOBUZZ-Replay\sources` first.

**Video won't download**: YouTube or the school network may be refusing. Try another network. If yt-dlp says
"Sign in to confirm you're not a bot", add `-CookiesFromBrowser firefox` (or `edge`) while signed in to YouTube in
that browser. If it says the format or the player changed, YouTube moved on: bump `ytdlp` in `replay.json` to the
newest [yt-dlp release](https://github.com/yt-dlp/yt-dlp/releases) (version, URL, sha256) in a PR, then
`update-replay`.

**Log won't download**: the logs come from the `logs/<date>-<robot>` branches on GitHub; they are throwaway
branches, so keep an event's branches until its matches are reviewed.

**Something else**: `-NoVideo` opens only the log. Everything lives in `%LOCALAPPDATA%\BIOBUZZ-Replay` (delete it
to start over; setup puts it back). Setup keeps a laptop's earlier AdvantageScope layout as
`state-…-before-biobuzz-….json` in `%APPDATA%\AdvantageScope`.

## For whoever maintains it

| File | What |
|---|---|
| `matches.json` | Each event: its stream, `video_shift_s`, and per match the robot, its log, the field's start time and `video_start`. `maintain/find_matches.py <EVENT>` fills an event from FTCScout and the logs branches. |
| `replay.json` | Every download, pinned with its sha256: AdvantageScope, ffmpeg, yt-dlp, Deno, the field bundle and the models zip. |
| `setup.ps1`, `open-match.ps1`, `Replay.ps1` | The laptop side (Windows PowerShell 5.1). |
| `maintain/build-assets.sh` | Builds the models zip, byte for byte the same each time, from `cad/advantagescope/Robot_BIOBUZZ` and the HIVE cut from the pinned field bundle (`HiveAssets`, compiled without Gradle). |
| `../match-layout.json` | The tabs. Setup writes it into AdvantageScope's saved window state, so nobody imports it. |

**New robot model** (after `cad/advantagescope/from_onshape.py`): push. The **Replay assets** workflow builds the
zip, publishes it as a release and fails until `replay.json`'s `assets.tag` and `assets.sha256` match; its summary
gives the two lines to paste. Merge that, then `update-replay` on each laptop. The models go to laptops as the
release zip, not from git, so the repo's history only grows when the CAD model itself is committed.

**New event**: `python3 tools/advantagescope/replay/maintain/find_matches.py <EVENT CODE> --video <stream URL>`
after the logs are pulled, then commit `matches.json`.

**Why the clip carries a countdown**: AdvantageScope has no way to be told to load a video or to apply an offset
(its Video tab saves no state). It does lock a video by itself when its OCR reads a match timer go from 0:04 to
0:03, at fixed spots on screen. The clip blacks those spots out until Auto ends and draws that countdown in one,
timed from `video_start` and the log's own Auto length, so the lock lands exactly on the log. Tied to
AdvantageScope's code at the pinned version: a new AdvantageScope version means checking `VideoProcessor.ts`
still reads the same spots. AdvantageScope's OCR cannot read FiM's own timer (it sits across its spots), which is
why the scoreboard is read here instead, by matching the transition's hand (`marker/scoreboard-hand.gray`, cut
from a Belding frame) frame by frame. Another overlay needs another template: cut it the same way, with the crop
in `Replay.ps1` (`$HandCrop`).

**What is kept on a laptop**: `sources\` holds each match's stretch of the stream, unmarked, 40 s either side
(roughly 50-100 MB each): the one download, and what to keep if the stream goes away. `clips\` holds the marked clip
cut from it, only the newest per match.
