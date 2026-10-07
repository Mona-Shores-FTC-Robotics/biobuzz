# TIPs measured off the Saline Preview stream

Frame-stepped on the Day 2 stream (<https://www.youtube.com/watch?v=lr6iMORuxMs>, 60 fps) by a mentor on 7 Oct
2026, reading the player's `currentTime` for each frame. Times are seconds into the stream; a frame is 0.017 s.
What each number feeds is in [saline-preview-day2.md](saline-preview-day2.md).

## Playoff M3: Infinity Tech + CyBugs (red) vs Hornet Hackers + CyberSmiths (blue), 128–41

| Event | Stream time | After START |
|---|---|---|
| AUTO starts (first movement on the field) | 26916.245 | 0 |
| First piece leaves Infinity Tech's launcher | 26919.181 | **2.94 s** |
| Third POLLEN lands in the raised red CELL (3 NECTAR + 3 POLLEN: the §12.3 threshold); a fourth shot, a little late, hits the rim and stays out | 26920.362 | 4.12 s |
| Rocker first seen moving | 26923.628 | **7.38 s** |
| Rocker about halfway through its swing | 26924.978 | 8.73 s (1.35 s into the swing) |
| Rocker resting on its far stop; the spill already leaving the CELL, coming down on the robot parked under it | 26925.828 | **9.58 s** (2.20 s swing) |
| First spilled piece touches the tiles, around and just beyond the robot in front of the CELL | 26926.112 | 9.87 s (2.48 s after the rocker started, 0.28 s after it stopped) |

The simulated spring hood's first shot is at about 2.5 s (2 s spin-up + 0.45 s a shot), so the simulator's
launcher is slightly quicker off the line than Infinity Tech, the event's top-ranked robot.
Three shots in 1.18 s is one every 0.4 s, close to the simulated 0.45 s.

**The wait.** 3.27 s passed between the third POLLEN landing and the rocker's first movement. The simulator has no
such wait: its rocker moves the instant the torque crosses the threshold, and its TIP 1 starts at about 4.6 s
against 7.4 s here. To confirm on the frames: nothing else entered the CELL in that gap, and whether the three
POLLEN were rolling toward the back skin during it (the calibration is done with pieces against the back skin,
so a piece landing near the front gives less torque until it rolls back).

**The swing.** 2.20 s from first movement to the stop: 1.35 s to halfway, 0.85 s for the second half. An
accelerating swing, as a rocker past its balance point should be, so the simulator's 1.0 s (`ASSUMED_TIP_SECONDS`,
`HiveTracker.Tuning.tipSeconds` NaN) is the wrong size, not the wrong shape. One TIP so far; a second (Q26 at
5:01:19, Frost RoboFalcons, also exactly three POLLEN) before the constants change.

**What it adds up to.** The event's best robot had TIP 1 complete 9.6 s after START. The simulator has the same
TIP complete at about 5.6 s (start 4.6 s, swing 1.0 s). Of the 4 s difference, 3.3 s is the wait between the
third POLLEN landing and the rocker moving, 1.2 s the slower swing, and the launcher's slower start is offset by
the simulated spin-up. The wait is the part the simulator does not model at all, and it moves every "wait for the
TIP" branch, the spill catch spots and the chance of a third TIP inside AUTO.

**The spill's first touch** came 0.28 s after the rocker reached its stop, as the 3 Oct films showed (pieces pour
off the lip as the rocker lands). The simulator's first touch at 1.1–1.4 s after the TIP starts is that same
mechanism on a 1.0 s swing, so it follows the swing once that is corrected; nothing separate to refit. The camera is
too oblique to measure the landing distance, so the filmed 35–47 in stands.

**Frames kept** (`doc/media/saline/`, 720p stream captures, the red CELL seen from the alliance wall end):

| File | Stream time | What it shows |
|---|---|---|
| `m3-tip1-26925.828-rocker-on-stop.png` | 26925.828 | The rocker on its stop; the spill leaving the lip above the robot |
| `m3-tip1-26926.112-first-touch.png` | 26926.112 | The first piece on the tiles; the rest in the air around the robot |
| `m3-tip1-26926.178-spreading.png` | 26926.178 | Four frames later: pieces already spread a robot's width to each side |
| `m3-tip1-26926.428-spread.png` | 26926.428 | 0.32 s after the first touch: pieces from the alliance wall to past the robot on both sides, 2–3 ft of spread, heading back toward the wall; the HIVE end still landing |
| `m3-tip1-26928.195-at-rest.webp` | 26928.195 | 2.1 s after the first touch, wider view: the spill at rest (one piece at the far right still creeping). A row of POLLEN against the alliance wall at both ends, two NECTAR and a few POLLEN within a foot or two of the robot, a couple of strays toward the HIVE lane |

**How fast it spreads.** 0.32 s after the first touch the pieces cover 2–3 ft either side of where they landed and
are moving back toward the alliance wall. The simulator's `FILMED_BOUNCE_SCATTER` puts the median piece 24 in from
its landing point 0.5 s after touchdown; the event spill is at least that fast.

**Where it stops.** 2.1 s after the first touch the pieces are at rest, most of them back against the alliance
wall or within a foot or two of the robot that scored. On the simulator's normal tiles (`PLACEHOLDER_ROLLING_DECEL`
12 in/s²) pieces are still rolling 3 s after the TIP and reach 85 in out; stopping this soon is the "slow tiles"
setting (×3), the alternative every study ran. The event tiles behave like the slow ones.

## Every AUTO TIP in the clips (7 Oct 2026, 20 fps read, ±0.05 s)

The mentor pulled eleven 720p60 clips of the stream's AUTO periods with yt-dlp; the review stepped them with
ffmpeg (`tools/saline-stream/`). START is the first tick of the overlay clock minus 1 s. "Pollen in" is the
last of the three POLLEN seen settling in the raised CELL (±0.2 s; a volley bounces around before it settles).
"Moves" is the first frame the rocker has visibly left its stop; "on stop" the first frame it rests on the far
one. All times are seconds after START.

| TIP | Robot | Pollen in | Moves | On stop | Wait | Swing |
|---|---|---|---|---|---|---|
| Q9, blue | Clague GearCats | 6.6 | 7.1 | 8.0 | 0.5 | 0.9 |
| Q16, blue | Infinity Tech | 11.0 | 13.4 | 14.1 | 2.4 | 0.7 |
| Q19, blue | Team KUDOS | 10.3 | 12.4 | 13.1 | 2.1 | 0.75 |
| Q20, blue | CyBugs | 4.2 | 7.6 | 8.3 | 3.4 | 0.7 |
| Q25, blue | Infinity Tech | 10.3 | 11.0 | 11.8 | 0.75 | 0.8 |
| Q26, red | Frost RoboFalcons | 11.2 | 11.5 | 12.2 | 0.25 | 0.7 |
| Q29, red, TIP 1 | CyBugs | 3.7 | 5.6 | 6.3 | 1.9 | 0.75 |
| Q29, red, TIP 2 | CyBugs | – | 9.4 | 10.1 | – | 0.65 |
| Final M10, red | Infinity Tech | 5.0 (more arrived until ~8.4) | 8.8 | 9.6 | ≤ 3.8 | 0.8 |
| Final M10, blue | Team KRASH | 8.9 | 12.0 | 12.9 | 3.1 | 0.9 |
| Playoff M3, red (above, by hand) | Infinity Tech | 4.1 | 7.4 | 9.6 | 3.3 | 2.2 |

**The swing is 0.65–0.9 s, median 0.75 s**, on ten of eleven TIPs. Playoff M3's 2.2 s is 1.3 s of visible
creep (first half of the arc) and then a 0.9 s swing, so the swing proper matches there too. This agrees with
the practice-field films ([tip-timing.md](tip-timing.md)): the simulator already draws each TIP from 0.58–1.12 s
(`FieldSim.FILMED_TIP_SECONDS`), and the event sits in the short half of that range. Only the HIVE calibration's
1.0 s (`ASSUMED_TIP_SECONDS`) and the robot's NaN `HiveTracker.Tuning.tipSeconds` are still to set; 0.8 s fits both.

**The wait is the real finding: 0.25–3.4 s, median about 2 s**, between the third POLLEN settling and the
rocker leaving its stop, on every TIP. Sometimes it is invisible (the rocker sits, then goes), sometimes it is a
slow creep (M3). The simulator has none. A likely mechanism: the calibration (§12.3) is done with pieces
resting against the CELL's back skin, so a POLLEN that lands near the front gives less torque until it rolls
back, and a CELL loaded to exactly the threshold sits until the pieces have settled. A CELL loaded past the
threshold (Q26's volley, Q9's) goes almost at once. Either way the model needs a hesitation after the threshold
is crossed, drawn from roughly 0–3.5 s, shorter the further past the threshold the load is.

**What the fastest AUTO TIPs looked like.** TIP 1 complete at 6.3 s (CyBugs, Q29), 8.0, 8.3, 9.6, 9.6 s; the
rest 11–14 s. The simulator's 5.6 s is quicker than the quickest seen. CyBugs' second TIP of Q29 came 3.8 s
after the first (they fired into the newly raised CELL at once), at 10.1 s: the only two-TIP AUTO of the day.
