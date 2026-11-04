# Drafts: the 3-TIP swap pairs (4 Nov 2026)

Starting points for the mentor to refine in the Visualizer, not Autos we run: no OpMode, so the
Driver Station never lists them, and the Visualizer's Team Autos list (top-level files only) does
not show them. Open one from its link below. Each pair is two Autos that only work together: **our
R next to a partner running the matching L**, or the same two drafts as ours when we are the L.

**The idea.** R makes TIP 1 with its preloads. L fires its preloads into the LEFT CELL as soon as it
rises; R brings 4 more across, so LEFT gets 8 early and tips at about 17–18 s (TIP 2). Then both
put 4 into the RIGHT CELL from the right half (TIP 3). Each CELL is scored only from its own half,
so each robot crosses once, and the two pairs differ only in **who takes which lane**:

- **The centre lane** runs under the HIVE, between its foot bar on our side (x 46–48.6, y 50–91.4)
  and the centre line (x 70.75). A 16 in robot fits only square to the field (centre x 56.6–62.7),
  so a centre-lane drive is one curve whose heading turns *outside* the lane and holds 90° or 270°
  under the HIVE (a piecewise heading). Every centre-lane path here was checked against the foot
  bar and the centre line, corners included.
- **The wall lane** is everything between the foot bar and the alliance wall (x < 46): roomy, and
  R-Solo's own route.

| | Pair A | Pair B |
|---|---|---|
| R | **centre lane** both ways (`r-3tip-a.pp`) | **wall lane** both ways: R-Solo's route (`r-3tip-b.pp`) |
| L | **wall lane** (`l-3tip-a.pp`) | **centre lane** (`l-3tip-b.pp`) |
| R's TIP 3 load | catches the TIP 2 spill at LEFT_LAUNCH_CATCH (the spot is built for it) | the SCORING FLOWER |
| L's TIP 3 load | the GARDEN | the GARDEN |
| Lane drive time | centre 3.4–3.7 s, wall 2.6–3.1 s | |

Links (this branch):

- <https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/quirky-allen-etn5xp/TeamCode/autos/drafts/r-3tip-a.pp>
- <https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/quirky-allen-etn5xp/TeamCode/autos/drafts/l-3tip-a.pp>
- <https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/quirky-allen-etn5xp/TeamCode/autos/drafts/r-3tip-b.pp>
- <https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/quirky-allen-etn5xp/TeamCode/autos/drafts/l-3tip-b.pp>

## Timelines

From the drawn drive times and the typical times in `TeamCode/auto-registry.json` (LaunchAll 4.0 s,
PickFlower 4.4, GardenGather 6.0), with the last ball leaving about 1.1 s before a LaunchAll ends
and a TIP's swing starting about 1 s after it (`timing.json`). **Real 4 Nov launches took 4.5 s and
a FLOWER pick 3.4 s**: each volley is about 0.5 s later than this, each pick 1 s sooner.

### Pair A: R in the centre, L by the wall

| Time (s) | R (`r-3tip-a`) | L (`l-3tip-a`) | HIVE |
|---|---|---|---|
| 0–4.1 | preloads → RIGHT from the start | to LEFT_LAUNCH_OFFSET, waits for LEFT up | |
| ≈4.6 | | | **TIP 1** (RIGHT down, LEFT up ≈5.5) |
| 4.1–10.6 | WALL FLOWER pick | ≈6–10: preloads → LEFT (4) | |
| 10–13.1 | | **crosses: wall lane** south to the GARDEN | |
| 10.6–14.0 | **crosses: centre lane** north to LEFT_LAUNCH_CATCH | | |
| 14.0–18.0 | 4 → LEFT (8), last ball ≈16.9 | 13.1–19.1 GARDEN gather | **TIP 2** ≈18–19 |
| 18–21.7 | sees the TIP (≤2.5 s), catches the spill (≤2 s) | 19.1–20.7 to RIGHT_LAUNCH_OFFSET | RIGHT up ≈19.7 |
| 20.7–24.7 | | 4 → RIGHT, last ball ≈23.6 | |
| 21.7–25.2 | **crosses back: centre lane** south to RIGHT_LAUNCH_CATCH | 24.7–27.8 parks at LEFT_PARK (wall lane) | |
| 25.2–29.2 | its catch → RIGHT, last ball ≈28.1 | | **TIP 3** ≈29–30 if RIGHT has 8 |

R's fallback (no TIP 2 within 2.5 s: L missed or is dead): the SCORING FLOWER and back into LEFT,
last ball ≈30.4 at typical times (≈29.4 at real pick times). TIP 2 on our own, just.
L's fallback (no LEFT up by 10 s on the match clock, adjustable in INIT: R did not make TIP 1):
wall lane to RIGHT_LAUNCH_OFFSET, preloads → RIGHT (TIP 1 itself), park.

### Pair B: R by the wall, L in the centre

| Time (s) | R (`r-3tip-b`) | L (`l-3tip-b`) | HIVE |
|---|---|---|---|
| 0–4.1 | preloads → RIGHT | to LEFT_LAUNCH_CATCH, waits for LEFT up | **TIP 1** ≈4.6 |
| 4.1–10.6 | WALL FLOWER pick | ≈6–10: preloads → LEFT (4) | |
| 10–13.7 | | **crosses: centre lane** south to the GARDEN | |
| 10.6–13.2 | **crosses: wall lane** north to LEFT_LAUNCH_OFFSET (R-Solo's path) | | |
| 13.2–17.2 | 4 → LEFT (8), last ball ≈16.1 | 13.7–19.7 GARDEN gather | **TIP 2** ≈17–18 |
| 18.2–23.7 | sees the TIP, SCORING FLOWER pick | 19.7–21.7 to RIGHT_LAUNCH_CATCH | RIGHT up ≈18.9 |
| 21.7–25.7 | | 4 → RIGHT, last ball ≈24.6 | |
| 23.7–26.8 | **crosses back: wall lane** south to RIGHT_LAUNCH_OFFSET | 25.7–29.3 parks at LEFT_PARK via the centre lane | |
| 26.8–30.8 | 4 → RIGHT, last ball ≈29.7 | | **TIP 3** ≈30.5 (counts: within the 8 s after AUTO) |

R's fallback: the SCORING FLOWER and back into LEFT, last ball ≈29.2. L's fallback: centre lane to
RIGHT_LAUNCH_CATCH, preloads → RIGHT, park.

## What did not fit, and the open questions

1. **Both R drafts end on a launch, not a park** (a TIP is 20 points and a ranking-point step; the
   park is 5 and neither). Their last ball leaves at 28–30 s at typical times, **later than 30 s at
   4 Nov's real 4.5 s launches** unless launch-on-approach and the feed trim land. That is the
   number to watch in the simulation.
2. **R-3Tip-A's TIP 3 rests on catching the TIP 2 spill** (about half a spill rolls back into a
   waiting robot in the simulator). It is the only load that fits after a centre-lane crossing; if
   the catch takes fewer than 4, RIGHT does not reach 8. Pair B uses the SCORING FLOWER instead and
   is about 1.6 s later for it.
3. **The GARDEN gather (6 s typical) is unmeasured**, and both L drafts lean on it.
4. **Collisions the timing avoids, not the geometry.** Pair A: L passes the WALL FLOWER staging
   about 1 s after R leaves it (a slow R pick, timeout 8 s, would meet L there). Both pairs: the two
   RIGHT launch spots are 0.5 in apart when both robots are on them, and the lanes cross at the
   right end only after one robot has left. The simulator's collision column says whether the
   margins hold.
5. **The decisions.** R's one Wait-with-Otherwise is "Did TIP 2 come?" (`Tip`, 2.5 s) right after
   its LEFT volley, while the camera faces the HIVE; L's is its first wait (`CellUp`, the partner
   wait, until 10 s on the match clock). L's second wait (RIGHT up, 4 s) has no Otherwise: if TIP 2
   never comes it fires at a down CELL.
6. **Parks:** L's guarded routes park at LEFT_PARK; R's have no park, so no guard. Each spot has
   no park of its own (`parkFrom`), so a guard that parks early drives the route's park path from
   wherever the robot is: draw them (P in the Visualizer) before relying on it.

## Simulation

`TeamCode/sim-requests/r-3tip-a.json` and `r-3tip-b.json` run each R alone (the floor), beside its
L on time, beside its L missing every shot, and beside a dead partner on LEFT_START. A draft's
request may name its .pp here and its partner may be the other draft (`SimRequestsTest`).
