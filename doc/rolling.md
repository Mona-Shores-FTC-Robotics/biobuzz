# How far spilled pieces roll

Measured 6 Oct 2026 from two videos, no new testing. The simulator's rolling model changed to match
(`FieldSim.FILMED_ROLLING_DECEL_IN_PER_S2`, `FILMED_BOUNCE_SCATTER`, checked by `FieldSimTest` and
`SpillLandingTest`). No sims have been rerun on it yet: the published numbers predate it.

## NECTAR, from a match video

The 124-point World Record match, 19049 & 24909 against 25538 & 24964 ([YouTube](https://www.youtube.com/watch?v=495akYrSr2U);
copy in the team's [Drive folder](https://drive.google.com/drive/folders/1rOUkQG-33c22QKkhaK9vAbaH6GRpNq1T)).
Tripod at the side of the field, 1080p, 30 fps. Two TIPs had spills on open floor:

- **Blue TIP, 1:16** (spill away from the camera). Two NECTAR rolled side by side across open tiles. Tracked
  by colour at 10 fps, they moved about 17 px every 0.1 s for 1.3 s, steady to within the noise, then stopped
  against something. The ball itself (26 px across = 3.6 in) gives the scale: **about 23 in/s, with no slowing
  the video can show**.
  ![Blue TIP](../sim-review/roll-blue-tip-77s.jpg)
- **Red TIP, 0:49** (spill toward the camera). The NECTAR landed past the HIVE's feet and rolled to the wall
  within about 1.5 s. Two seconds after the TIP, none was left on the open floor.
  ![Red TIP, 2 s later](../sim-review/roll-red-tip-51s.jpg)

## A POLLEN, measured (second video)

Team Orange 16409's test Autonomous ([YouTube](https://www.youtube.com/watch?v=CBCioC3x-vc), from 1:00; the clip is
in the Drive folder), filmed from high behind one alliance wall: a much better view of the floor. Positions
were turned into field inches using the HIVE's foot bars (49.5 in apart, ending 51.3 in from the wall) and the
far corners, good to about ±15%.

- **One POLLEN rolling alone on the far half**, nothing touching it: about 10 in/s at the start, then a steady
  slowing to rest over about 3.4 s, 18 in in all. That is **about 3 in/s²**, inside the simulator's spread
  around 4 (each piece's resistance varies by ±35%). The old drag would have stopped it in under 0.5 s, 3 in on.
  ![POLLEN rolling to a stop](../sim-review/roll-pollen-orange.jpg)
- **The spill** (the blue CELL at the start of the Auto, toward the camera): NECTAR and POLLEN reach the near
  wall's zone about 0.6–0.8 s after landing, and one POLLEN bounced the other way to the far wall. Pieces in the
  air make single positions unreliable from this angle, so this only says the spread is as fast as the
  simulator's, or a little faster.

## What changed in the simulator

| | Before | Now |
|---|---|---|
| Drag while rolling on the tiles | 2.5 × speed per second, plus 12 in/s² | 4 in/s² only |
| Drag touching a robot, wall, the HIVE or a CELL | 2.5 × speed per second, plus 12 in/s² | unchanged |
| A piece at 23 in/s, 1.3 s later | about 1 in/s (stopped) | about 18 in/s |
| A piece landing at 60 in/s rolls | about 19 in | to a wall, unless something stops it |
| Sideways kick on a hard landing (bounce scatter) | 0.45 | 0.3, refitted to the same 3 Oct films |
| Tile unevenness | up to 3 in/s² | up to 1.5 in/s², under the rolling drag, so a slope steers a piece but never keeps it rolling |

The old drag was meant for sliding, but it applied while rolling too, so spills stopped two to three times too
short and bunched up near the HIVE. With it gone, the bounce scatter fitted against it spread spills too far,
so it was refitted to the same targets: 0.5 s after landing, pieces 21 in from where they landed (films: about
24, p90 41 against 44); 3 s after the TIP, anywhere from the wall to 86 in out (films: to 85).

**What it means for the Autos:** spilled pieces end up further away and more of them against walls, so they are
harder to pick up, and they cross the Drop Zone faster. Expect lower pickup counts when the sims are rerun.

## Limits

- The match video is from a low side angle: good for where pieces end up and roughly how fast, not a precise
  number. 4 in/s² is an upper bound (at 12, the blue pieces would have lost over half their speed in that 1.3 s).
- NECTAR gives an upper bound (4 in/s²), one POLLEN a measurement (about 3 in/s²). Two videos, three pieces:
  enough to rule out the old drag, not to tell 3 from 4.
- A match field has robots in the way; the 3 Oct films had none. Both agree on the spread.
