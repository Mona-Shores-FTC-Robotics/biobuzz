# What the simulator knows, and what it guesses

The simulator (`TeamCode/src/test/.../logging/FieldSim.java` and `AutoSim.java`) plays a whole AUTO: both
robots drive their Auto Builder routes, every piece flies, bounces and rolls, and the HIVE tips. It is good for
comparing two ideas under the same assumptions. It is not a prediction of a real match: where it rests on a
guess, the real number can differ, and so can which idea wins. Written 6 Oct 2026.

## Measured or official

| What | Where it comes from |
|---|---|
| When a HIVE tips (3 NECTAR + 3 POLLEN; 8 POLLEN; holds on one fewer) | FIRST's Event Field Setup Guide §12.3, the test field staff run on every real HIVE (`HiveCalibration`, checked by `HiveCalibrationTest`) |
| Piece sizes and weights (POLLEN 2.8 in, 0.055 lb; NECTAR 3.6 in, 0.091 lb) | The game manual and AndyMark's listings |
| The field: 141.5 in, the HIVE, FLOWERs, GARDEN, LOADING ZONE, starts | The game manual and field CAD |
| Scoring, LEAVE, PARK, G409 (touching a falling spilled piece) | The game manual, applied the way `AutoSim` judges them |
| Where a spill lands (first touch 35–47 in from the CELL's wall, 1.1–1.4 s after the TIP starts) | Fitted to the team's 3 Oct films of a TIP (`FILMED_*` in `FieldSim`). Two films, so a rough fit |

## Guessed (marked `PLACEHOLDER_` or "assumed" in the code)

| What | The guess | Why it matters |
|---|---|---|
| How long a TIP takes | 1.0 s; videos show 0.5–1.2 s, most often about 1 s ([tip timing](tip-timing.md)) | When the spill lands, and how long we wait |
| How pieces bounce off tiles, walls, the HIVE, robots | restitution 0.45 / 0.5 / 0.2 / 0.1 | Where a spill ends up; how much a flap or hook keeps |
| How fast pieces stop rolling | 12 in/s² (×3 on "slow tiles") | Whether a spill is still near us when we drive in. "Slow tiles" exists because this is a guess |
| Our robot's size and shape | Read off the 5 Oct CAD screenshots, ±15% | Every route spot; what the robot can reach |
| The intake | Takes a piece only when it touches the front, top under 5 in, one every 0.35 s | **The biggest lever found.** A faster or wider real intake changes most results |
| The launcher | 2 s spin-up, 0.45 s a shot, 75°, a little spread | When TIPs happen; how many shots score |
| Flaps, hooks, side walls | Thin plates of the drawn size, bouncing pieces with the guesses above | The rigid V's gain is pieces bouncing off its flaps into the intake: plausible, untested |
| Driving | Pedro following the route at speed 50 | How long each leg takes |

Nothing about our robot has been measured on a robot yet. The quickest numbers to measure, in order of how much
they move the results: the intake's width and time per piece, the TIP time (film one), and how far a POLLEN
rolls on our tiles. (The TIP time is now roughly checked against video: [tip timing](tip-timing.md).)

## How AdvantageScope shows the right robot

A `.wpilog` only stores numbers over time: where each robot is, where every piece is, the HIVE's angle, and
for some studies the position of each part of the robot. AdvantageScope draws a robot model at the logged
position, and the model is chosen in the layout (`sim-review/advantagescope-layout*.json`), not by the log.

- **The baselines** use the model **BIOBUZZ Robot** (the Flat Intake). The setup script builds it from the same
  numbers the simulator uses (`RobotAssets.java`), so the drawing and the simulation can't disagree.
- **The shape studies** use one model, **BIOBUZZ Robot (match shapes)**, that contains every shape as a
  separate part. Each log says where each part goes: the shape that was simulated at the robot, the others
  20 m under the field. That's why loading a log "just shows the right robot". The hook swinging down is the
  same trick: the log moves that part as the simulator moved it.

So what you see in AdvantageScope is what the simulator did, frame by frame. What it can't tell you is whether
the simulator's guesses are right; the table above is where to look for that.
