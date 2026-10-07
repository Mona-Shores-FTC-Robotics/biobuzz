# Piece and robot physics measured off the Saline Preview stream

Measured 7 Oct 2026 from the same eleven 720p60 AUTO clips as [saline-tip-measurements.md](saline-tip-measurements.md)
(Day 2 stream, <https://www.youtube.com/watch?v=lr6iMORuxMs>), by one person with ffmpeg, numpy and Pillow and no
robot. The TIP timing is in that document and is not repeated here. This one checks the simulator's other guesses:
how spilled pieces roll, how the launcher misses, how fast the launcher and the robots are, and where a spill goes.
Scripts: `tools/saline-stream/` (listed at the end). Per-piece tracks: `tools/saline-stream/saline-rolling-tracks.csv`.
Frames: `doc/media/saline/`, named by clip and time.

## How positions were read

The camera is fixed, high behind the audience wall. The four floor corners of the field were read by eye on each
clip's pre-START frame (`tools/saline-stream/saline-corners.json`, ±5 px) and a homography maps the frame to
field inches: x across the field as the camera sees it, y from the audience wall (0) to the far wall (141.5).
The red HIVE and alliance are on the left, blue on the right; the red CELL spills toward the audience wall,
the blue CELL toward the far wall. Pieces are found by colour (POLLEN yellow, NECTAR red or blue) at 20 fps and
linked into tracks.

Two limits matter for every number below:

- **Scale.** One pixel is 0.16–0.19 in across the field but 0.55–0.9 in in depth (the camera is only about 15°
  above the floor). A ±6 px error in a corner changes a fitted deceleration by under 0.3 in/s² (tested).
- **Height.** A piece is seen 1.4 in (POLLEN) or 1.8 in (NECTAR) above the tiles, so its floor position reads
  5–7 in further from the camera than it is. A piece in the air reads much further still (20 in of height reads
  as about 75 in). Differences along a track (speed, deceleration) are unaffected; absolute positions of pieces
  on the floor are corrected by 5–7 in below, and positions of pieces in the air are not used. Pieces near the
  far wall are foreshortened, so the blue spills give only rough positions.

## 1. Rolling deceleration on the tiles

**Result: 3.9 in/s² (interquartile 3.1–4.9) for a POLLEN rolling freely on the event tiles; NECTAR about 1.5.**
The event tiles behave like the simulator's normal setting (`FILMED_ROLLING_DECEL_IN_PER_S2` = 4.0), not its
"slow" ×3 setting (12). [saline-tip-measurements.md](saline-tip-measurements.md) said the opposite from the
Playoff M3 spill alone; that spill came to rest early because it landed on a robot and ran into the wall, not
because of drag (see "How it ended" below).

**How.** Every piece in nine TIP spills (the nine listed in the TIP document minus Q9, whose clip starts at the
TIP) was tracked for 4.5 s after the rocker stopped. 24 tracks rolled 6 in or more on open floor with no other
piece, robot or wall in the way for at least 0.8 s (checked on trail overlays, e.g.
`saline-31340-23.9-m10-blue-spill-rolling-tracks.jpg`). For each, distance along the path was fitted with
s = v₀t − ½at² over the steady part of the roll: from the last bounce or kick to the moment the smoothed speed fell
under 5 in/s or dropped by more than a third in 0.15 s (a collision). The error is the fit's 1σ combined with the
corner sensitivity.

| Spill | Track | Piece | v₀ in/s | Rolled | Decel in/s² | How it ended |
|---|---|---|---|---|---|---|
| P3 red | 52 | POLLEN | 18 | 2.6 s, 33 in | 3.9 ± 0.3 | hit a robot |
| P3 red | 57 | POLLEN | 40 | 1.1 s, 30 in | 23.3 ± 2.0 | into the wall at speed |
| M10 red | 17 | red NECTAR | 8 | 1.2 s, 8 in | 1.5 ± 0.2 | rolled to rest |
| M10 red | 61 | POLLEN | 26 | 3.1 s, 58 in | 4.3 ± 0.1 | hit a piece |
| M10 blue | 9 | POLLEN | 15 | 2.6 s, 28 in | 3.1 ± 0.1 | hit a piece |
| M10 blue | 11 | POLLEN | 9 | 1.6 s, 12 in | 1.4 ± 0.1 | rolled to rest |
| M10 blue | 23 | red NECTAR | 6 | 0.9 s, 6 in | −0.4 ± 0.4 | rolled to rest |
| M10 blue | 37 | POLLEN | 17 | 1.5 s, 20 in | 4.2 ± 0.4 | hit the far wall |
| M10 blue | 65 | blue NECTAR | 28 | 1.4 s, 34 in | 6.5 ± 0.6 | hit the side wall |
| Q16 blue | 68 | POLLEN | 36 | 2.4 s, 70 in | 5.3 ± 0.4 | hit a robot |
| Q20 blue | 43 | POLLEN | 12 | 3.1 s, 29 in | 1.6 ± 0.1 | rolled to rest |
| Q20 blue | 50 | POLLEN | 16 | 2.8 s, 32 in | 3.1 ± 0.1 | rolled to rest |
| Q20 blue | 67 | POLLEN | 26 | 1.6 s, 35 in | 5.1 ± 0.4 | still rolling at the clip's end |
| Q20 blue | 69 | blue NECTAR | 14 | 0.9 s, 14 in | −1.0 ± 0.7 | hit a piece |
| Q25 blue | 48 | POLLEN | 32 | 1.1 s, 35 in | 2.1 ± 1.1 | still rolling at the clip's end |
| Q25 blue | 50 | blue NECTAR | 25 | 1.4 s, 25 in | 11.4 ± 1.2 | bouncing, then hit a piece |
| Q25 blue | 55 | POLLEN | 28 | 1.7 s, 29 in | 12.7 ± 0.6 | into the far wall |
| Q25 blue | 61 | POLLEN | 17 | 1.5 s, 21 in | 3.8 ± 0.3 | hit the far wall |
| Q26 red | 78 | POLLEN | 13 | 1.1 s, 11 in | 5.6 ± 0.5 | hit the far wall |
| Q26 red | 92 | POLLEN | 31 | 1.2 s, 31 in | 9.8 ± 0.9 | along the audience wall |
| Q26 red | 105 | red NECTAR | 10 | 1.1 s, 9 in | 2.3 ± 0.5 | hit a piece |
| Q29 red | 109 | POLLEN | 12 | 1.3 s, 11 in | 6.4 ± 0.8 | hit a piece |
| Q29 red | 121 | red NECTAR | 15 | 1.2 s, 18 in | 1.1 ± 0.4 | hit a piece |

(Q25 track 78 in the CSV rolled under 1 s at 1 in/s and is left out, so 23 remain.)

- **The ten rolls of 1.5 s or longer, where the fit is good: median 3.9, mean 4.4 ± 1.0, IQR 3.1–4.9 in/s².**
  All 23: median 3.9. Fits with rms residuals under 0.5 in. Speed traces are in the CSV.
- **POLLEN** (17 tracks): median 4.2. The six free POLLEN rolls that ended at rest: 1.4, 1.6, 3.1, 3.9, 4.3, 5.1.
- **NECTAR** rolls with less drag: the four red NECTAR give 1.1, 1.5, 2.3 and about 0; the one clean blue NECTAR
  6.5. Few and short, so NECTAR is "about 1.5 in/s², between 0 and 3".
- The values above 9 (P3 57, Q25 50 and 55, Q26 92) are pieces whose speed fell in a few frames at the end: a wall
  or a glancing hit the end-detector did not catch. They are not rolling drag and are left out of the medians.
- A 20 fps track has ±0.5 px of jitter, so the deceleration of a short slow roll (under 1.2 s, under 10 in/s) is
  only good to about ±1 in/s²; the long rolls are good to ±0.3.

**What the simulator does with it.** Nothing to change: 4 in/s² with the ±35% per-piece spread
(`PLACEHOLDER_ROLL_SPREAD`) covers 3.1–4.9. NECTAR could roll with about a third of the POLLEN drag; the
simulator uses one value for both. Pieces on the event floor stopped against walls, robots and each other far more
often than they rolled to rest on their own (16 of 23 tracks), which is what the 4 in/s² model predicts for a spill.

## 2. Launcher misses

**Result: Infinity Tech, 5 shots seen clearly in Playoff M3: 3 in, 1 hit the rim and fell out, 1 went over.
In the Final (red) its first 3 shots all stayed in. No shot was seen to enter the CELL and bounce back out.**

Only two of the four volleys could be read. In Q16 and Q25 (Infinity Tech on blue) the robot shoots from directly
under the HIVE and the ball's flight is behind the HIVE's sign and score display
(`saline-14690-17.6-q16-shooter-under-hive.jpg`); nothing of it is visible from this camera.

**How.** Moving yellow pixels in the HIVE band were detected at 60 fps and linked into tracks; each track was
drawn on the median frame (`saline-26900-18.8-22.6-infinity-tech-shot-tracks.jpg`) and plotted as row against
time (`saline-26900-18.6-22.2-shot-kymograph.png`). A shot is a track rising from the robot's rows to the CELL
opening. One that ends there stayed in; one that comes back down did not.

| Volley | Shot | Leaves launcher (clip s) | After START | Outcome |
|---|---|---|---|---|
| P3 red (26900, START 16.18) | 1 | 19.18 | 3.00 | in, stayed |
| | 2 | 19.32 | 3.14 | in, stayed (visible in the CELL to 21.8) |
| | 3 | 19.56 | 3.38 | in, stayed |
| | 4 | ~19.95 (rise hidden; at the rim 20.01) | ~3.8 | hit the rim, fell to the tiles by 20.68 |
| | 5 | 20.66 | 4.48 | rose above the opening, came down outside by 21.9 |
| M10 red (31340, START 8.05) | 1 | 10.86 | 2.81 | in, stayed |
| | 2 | 11.06 | 3.01 | in, stayed |
| | 3 | 11.31 | 3.26 | in, stayed |

P3 agrees with the mentor's hand reading in the TIP document (first piece at 2.94 s; a fourth shot hit the rim).
No further shots followed in P3 before the rocker moved at 23.56. In M10 the robot drove to the audience wall and
shot from there between 14.5 and 16.5 s; those balls arrive at the CELL at a flat angle and several bounce around
its top (`saline-31340-12.5-17.0-m10-red-late-shots-10fps.jpg`), but at this resolution they could not be counted
or followed in and out. The TIP document notes pieces still arriving until 8.4 s after START.

**Against the simulator.** 6 of 8 countable shots scored (the simulator: about 5 in 6, and 92–96% from a still
robot). The two misses were a rim hit and an over-shot, both of which the simulator has. An "in then out" bounce
was not seen in 8 shots, so the commentary's bounce-outs (if that is what they were) are not in this sample; the
simulator's lack of that mode is not contradicted here, but 8 shots is too few to rule it out.

## 3. Launch cadence and first shot

**Result: Infinity Tech fires its first POLLEN 2.8–3.0 s after START and the next two 0.15–0.25 s apart.**

| Volley | First shot after START | Intervals between shots |
|---|---|---|
| P3 red | 3.00 s | 0.14, 0.24, ~0.4, 0.7 s |
| M10 red | 2.81 s | 0.20, 0.25 s |

Read from the track start times above, ±0.03 s. The simulator's spring hood is 2 s of spin-up and 0.45 s per
shot, so its first shot is at about 2.5 s (slightly early) and its volley is about twice as slow as Infinity Tech's
first three shots. Three shots in half a second is the event's best robot; the other teams' launchers were not
visible enough to time.

## 4. Robot driving speed

**Result: the fastest straight run seen was about 57 in/s cruise, peaking at 65–78 in/s; other robots moved at
20–25 in/s.**

Robot 17039 (Q29, clip 19450) drove 84 in along the far wall from its start, from 11.3 to 13.0 s (0.4–2.1 s after
START). Its silhouette against the pre-START frame was tracked at 20 fps and its centre column converted to
inches. Speeds over 0.25 s windows: 15, 28, 49, 65, 78, 71, 63, 49, 56, 62, 58, 53, 45, 41, 49, 40, 17, 7. The
middle second (11.7–12.7 s) covers 57 in: **57 in/s**, with 0.4 s of acceleration before and 0.3 s of braking
after. Good to about ±10% (the far wall is 0.19 in/px across). Other runs: the robot at the near-right corner in
Q29 drove 20 in in 0.9 s at a steady 22 in/s; the right robot in M10 left its wall at 25 in/s; Infinity Tech in P3
moved about 20 in off its wall in 0.6 s and stopped to shoot (the silhouette's bottom edge is too jittery for a
better number). No robot was seen holding 40–50 in/s on a long run except 17039.

The simulator's 40 in/s (`AutoSim.MAX_SPEED_IN_PER_S`, 30 in/s² accel) and the studies' 50 are between the two
kinds of robot seen. 17039's 0→65 in/s in about 0.5 s is 130 in/s², four times the simulated acceleration.

## 5. The spill

**First touch: about 35–42 in from the alliance wall** on the two clean red spills, agreeing with the simulator's
35–47. Read on the first frame with pieces on the tiles (Q26 at 20.75–20.85 s, Q29 at 17.90–18.00 s,
`saline-18070-20.65-20.85-q26-first-touch.jpg`, `saline-19450-17.80-18.00-q29-first-touch.jpg`): five pieces each,
raw 44 and 37 in median, corrected 5–6 in for piece height to 39 (36–44) and 31 (22–41), plus 2–3 in because they
were already rolling toward the wall at about 25 in/s when read. In P3 the pieces landed on and around the robot
parked under the CELL, 34–44 in out 0.25 s after touching. The first touch came 0.37 s (Q26) and 0.6 s (Q29) after
the rocker reached its stop, later than P3's 0.28 s, where the robot's top was 14 in nearer.

**Spread 0.5 s after landing.** The pieces leave the landing zone at 25–40 in/s (the starting speeds of the rolls in
section 1 and the frame-to-frame motion in the first-touch sheets), so they are 12–20 in from where they landed
0.5 s later. The simulator's `FILMED_BOUNCE_SCATTER` puts the median piece 21 in away: the size is right. The
direction is not random: nearly every piece heads for the alliance wall, carried by the rocker's throw; only a
few scatter sideways (Q26 track 78 and Q25's went the other way, toward the HIVE).

**Against a wall.** Three seconds after the TIP, 12 of 28 detected new pieces in the four red spills (P3, M10, Q26,
Q29) were within 5 in of the alliance wall, the rest 20–60 in out on the tiles; rough, because pieces right behind
the wall rail are hard to detect and the baseline of pieces already there is subtracted automatically. Of the 23
tracked rolls, 7 ended at a wall.

**The human player's NECTAR.** In P3 a red NECTAR rolls into the red LOADING ZONE from 25.8 s, 2.3 s after the
rocker started to move and 0.7 s before the spill landed (`saline-26900-26.0-35.5-red-loading-zone-2fps.jpg`,
track 17 in the P3 CSV: 11 in from the wall, 17–20 in/s). That is the simulator's `HUMAN_DELAY_S` of 2 s. In M10
(red) no NECTAR entered the zone within 10 s of the TIP; the partner robot parked there 5.5 s after it. One of two.

## What could not be measured

- Shots from under the HIVE (Infinity Tech on blue; Q16, Q25): hidden behind the HIVE's signage.
- Long-range shots arriving at the CELL (M10 after 14.5 s): too small to follow in and out.
- Bounce restitution off tiles, walls, robots and the HIVE (`PLACEHOLDER_*_RESTITUTION`): pieces in the air
  cannot be placed from this camera.
- Blue spills' landing distance: foreshortened at the far wall.
- Launcher spread (`PLACEHOLDER_SPEED_SPREAD`, `PLACEHOLDER_ANGLE_SPREAD_RAD`): 8 shots.

## Summary against the simulator

| Assumption | Simulator | Measured | Verdict |
|---|---|---|---|
| Rolling drag on tiles | 4 in/s² ± 35% per piece | POLLEN 3.9 (IQR 3.1–4.9), NECTAR ~1.5 | Confirmed; NECTAR rolls freer |
| "Slow tiles" (×3 = 12) | alternative setting | no free roll above 6.5 | Not the event tiles |
| Shots scored | ~5 in 6 | 6 of 8 (3 of 5, 3 of 3) | Consistent |
| In-then-out bounce | none | none in 8 shots | Not seen; too few to rule out |
| First shot | ~2.5 s after START | 2.8–3.0 s | Simulator 0.3–0.5 s quick |
| Shot interval | 0.45 s | 0.15–0.25 s | Simulator twice too slow |
| Drive speed | 40 (studies 50) in/s, 30 in/s² | 57 in/s cruise, ~130 in/s², one robot; others 20–25 | In range; best robot faster |
| Spill first touch | 35–47 in from the wall | ~35–42 in (two spills) | Confirmed |
| Spread at 0.5 s | median 21 in, random direction | 12–20 in, toward the wall | Size right, direction not |
| Human NECTAR | 2 s after the TIP | 2.3 s (one case); none in 10 s (one case) | Consistent, once |

## Scripts (`tools/saline-stream/`)

`fieldmap.py` (homography), `frames.py` (ffmpeg decode), `blobs.py` (colour blobs), `track.py` and `trails.py`
(pieces on the floor), `rollsteady.py` (the deceleration table), `kymo.py` and `shotplot.py` (shots),
`robotrun.py` (a robot's run), `spillstats.py` (first touch, spread, walls), `nectarin.py` (LOADING ZONE),
`grid.py` (a pixel grid on a frame, for reading corners). `saline-corners.json` is the corners read for each clip.
