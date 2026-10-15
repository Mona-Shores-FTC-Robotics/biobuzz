# G409: route fixes (15 Oct 2026)

The Q&A (`doc/game/qa-review.md`, Q17 and Q23) reads G409 strictly: a robot touching a spilled piece before it reaches
the tiles is a foul every time, and STRATEGIC when it's routine. So a design that surrounds falling pieces (a hook) is
out (user, 15 Oct), and every Auto has to stay out of a spill while it's in the air.

## Where the touches were

Our own robot, every time, 0.3-0.5 s after a TIP: waiting at a catch spot inside the landing area, or driving through
it. 60 runs each on the merged simulator (0.4 restitution), rigid V, `tools/auto-routes/batch` (partners rigid V at
50 in/s, not the published rows' spring hood at 40, so these are patterns, not the published numbers).

| Auto | Touch runs | Where |
|---|---|---|
| R-Quals | 14 | TIP 1's catch wait at `S_CATCH` (57.5, 28) |
| L-Quals | 2 | the same spot |
| Stages, angled | 20 | (57.5, 28) after TIP 1; (57.5, 114) after TIP 2 |
| Stages, wall | 18 | the same two spots |
| Flower first | 22 | driving N_FIRE → S_FIRE through (57.5, 113) 0.46 s after TIP 2 |
| Sister | 2 | L's waits at (59, 22) and (55, 116) |
| Sister five (`sister5p`) | 1 | L at (59, 18), body |

The simulator chat measured the spill's reach (60 seeds, no robot in the way): above a 4 in flap, pieces reach 41 in
from the HIVE's centre line (y 70.75), so a robot facing the HIVE keeps its centre at y <= 19 south / >= 122 north;
the last piece lands 1.55 s after the TIP starts at the latest. A catch spot more than 25 in off the HIVE's axis
(x < 33) is outside the falling pieces altogether, but on these routes it puts the robot into the HIVE frame on the way
north (every run at x 33 and 40), so it needs its own route.

## The fixes

- **Catch spots** move out of the falling spill: south y 28 → **18** (`alone.S_CATCH`, `helpers.CATCH_R`), north
  y 113.5-116 → **123.5** (`qual.N_CATCH`, `helpers.CATCH_L`, `sister.L_N`; `sister.L_S` 22 → 18). The Stages keep
  TIP 1's catch at **y 21** (`qual.S_CATCH`): that catch feeds their TIP 2 (median 15.8 s at y 28, 18.1 at 21, 21.3
  at 18).
- **The Stages fire TIP 2 from N_LOW (y 114) as before, then back straight off to `N_WAIT` (y 123.5)** before its
  spill comes down (`shape_matrix.stages_for`). Moving the firing spot itself cost every 3-TIP run.
- **Flower first** waits 1.2 s after TIP 2 settles before driving through its landing area (`stream.py`).

## After (same class names; 60 runs)

| Auto | Touch runs (was) | Result (was) |
|---|---|---|
| R-Quals | **1** (14) | 3 TIPs 54 (53), 72.7 pts (73.0) |
| L-Quals | **0** (2) | 3 TIPs 55 (55), 73.7 (73.3) |
| Stages, angled | **0** (20) | 3 TIPs 3 (12), 53.7 (56.7) |
| Stages, wall | **2** (18) | 3 TIPs 0 (0), 53.0 (53.3) |
| Flower first | **8** (22) | 3 TIPs 40 (about 48), 68.0 |
| Sister | **0** (2) | 4 TIPs 45 (42), 89.0 (88.0) |

**The cost is small, except the angled Stages:** its third TIP comes from catching TIP 1's spill close in, which is
exactly what G409 rules out.

## Left to do

- **R-Quals, 1 run:** its park path crosses (41, 30) as a late TIP 1 spills.
- **Wall Stages, 2-3 runs:** the late-TIP-2 branch's park path crosses (57.5, 112) as the spill falls.
- **Flower first, 8 runs:** its park path at (57.6, 30) just after TIP 3 (4); a wait at (57.5, 114) after TIP 2 (3);
  its TIP 3 catch spot at y 24 (1).
- The general rule for anything new: no robot within 41 in of the centre line, facing the HIVE, until 1.6 s after a
  TIP starts; or more than 25 in off the HIVE's axis.
- All of this is the simulator's spill model. The real field test should film a TIP with a robot parked at the new
  catch spots.
