# What the simulator knows, and what it guesses

The simulator (`TeamCode/src/test/.../logging/FieldSim.java` and `AutoSim.java`) plays a whole AUTO: both
robots drive their Auto Builder routes, every piece flies, bounces and rolls, and the HIVE tips. It is good for
comparing two ideas under the same assumptions. It is not a prediction of a real match: where it rests on a
guess, the real number can differ, and so can which idea wins. Updated 6 Oct 2026 12:00 UTC.

## Measured or official

| What | Where it comes from |
|---|---|
| When a HIVE tips (3 NECTAR + 3 POLLEN; 8 POLLEN; holds on one fewer) | FIRST's Event Field Setup Guide §12.3, the test field staff run on every real HIVE (`HiveCalibration`, checked by `HiveCalibrationTest`) |
| Piece sizes and weights (POLLEN 2.8 in, 0.055 lb; NECTAR 3.6 in, 0.091 lb) | The game manual and AndyMark's listings |
| The field: 141.5 in, the HIVE, FLOWERs, GARDEN, LOADING ZONE, starts | The game manual and field CAD |
| Scoring, LEAVE, PARK, G409 (touching a falling spilled piece) | The game manual, applied the way `AutoSim` judges them |
| Where a spill lands (first touch 35–47 in from the CELL's wall, 1.1–1.4 s after the TIP starts) | Fitted to the team's 3 Oct films of a TIP (`FILMED_*` in `FieldSim`). Two films, so a rough fit |
| How long a TIP takes: each one drawn from 0.58–1.12 s | Match and test videos ([tip timing](tip-timing.md)) |
| How far pieces roll: 4 in/s² of drag on the tiles, a bounce on landing that spreads them | A match video and the 3 Oct films ([rolling](rolling.md)). One match, so an upper bound, not a measurement |

## Guessed (marked `PLACEHOLDER_` or "assumed" in the code)

| What | The guess | Why it matters |
|---|---|---|
| How pieces bounce off tiles, walls, the HIVE, robots | restitution 0.45 / 0.5 / 0.2 / 0.1 | Where a spill ends up; how much a flap or hook keeps |
| Our robot's size and shape | Read off the 5 Oct CAD screenshots, ±15% | Every route spot; what the robot can reach |
| The intake | Takes a piece only when it touches the front, one every 0.35 s, 85% of the time; with a roller drawn (`rollerBottomIn`, the 6 Oct CAD) only a piece the roller can bite; a piece that arrives while it is busy bounces off the body, or with vectored rollers (`intakeHoldsAtMouth`) is held at the mouth and queued | Every study line counts the misses by why ("low", "height", "beside", "interval", "flower", "full", "chance"). The roller height decides whether a POLLEN is taken at all; the mouth width and the queue are worth 2–4 points on ShootsRight and 3 with the Rigid V; the "height" misses are a spill still in the air (not takeable) and the time per ball changes nothing in AUTO: [intake design](intake-design.md) |
| The launcher | 2 s spin-up, 0.45 s a shot, 75°, a little spread (about 5 shots in 6 score); fires once within 2° of the CELL | When TIPs happen; how many shots score. With the spread set to zero and firing only when still and within 0.5°, shots still score only 92–96% from the firing spots; every study line reports the shots scored and why the misses missed |
| Flaps, hooks, side walls | Thin plates of the drawn size, bouncing pieces with the guesses above | The rigid V's gain is pieces bouncing off its flaps into the intake: plausible, untested |
| Driving | Pedro following the route at speed 50 | How long each leg takes |

Nothing about our robot has been measured on a robot yet. The quickest numbers to measure, in order of how much
they move the results: the intake's width and time per piece, and the launcher. The TIP time and how far pieces
roll are now checked against video ([tip timing](tip-timing.md), [rolling](rolling.md)).

## How AdvantageScope shows the right robot

A `.wpilog` only stores numbers over time: where each robot is, where every piece is, the HIVE's angle, and
the position of each part of the robot. There is one robot model, **BIOBUZZ Robot**, and one layout
(`sim-review/advantagescope-layout.json`). The model holds every design as a separate part; each log says where
each part goes: the design that was simulated at the robot, the others 20 m under the field. So any log shows
its own robot, and its file name says which (`<Auto>_<robot>_<date>`). The setup script builds the model from
the same numbers the simulator uses (`RobotAssets.java`), so the drawing and the simulation can't disagree. A
hook swinging down is the same trick: the log moves that part as the simulator moved it.

So what you see in AdvantageScope is what the simulator did, frame by frame. What it can't tell you is whether
the simulator's guesses are right; the table above is where to look for that.
