# What the simulator knows, and what it guesses

The simulator (`TeamCode/src/test/.../logging/FieldSim.java` and `AutoSim.java`) plays a whole AUTO: both
robots drive their Auto Builder routes, every piece flies, bounces and rolls, and the HIVE tips. It is good for
comparing two ideas under the same assumptions. It is not a prediction of a real match: where it rests on a
guess, the real number can differ, and so can which idea wins. Updated 6 Oct 2026 12:00 UTC; checked against
the first real event on 7 Oct ([saline-preview-day2.md](saline-preview-day2.md)).

## Measured or official

| What | Where it comes from |
|---|---|
| When a HIVE tips (3 NECTAR + 3 POLLEN; 8 POLLEN; holds on one fewer) | FIRST's Event Field Setup Guide §12.3, the test field staff run on every real HIVE (`HiveCalibration`, checked by `HiveCalibrationTest`) |
| Piece sizes and weights (POLLEN 2.8 in, 0.055 lb; NECTAR 3.6 in, 0.091 lb) | The game manual and AndyMark's listings |
| The field: 141.5 in wall face to wall face (the outer ring of 24 in tiles is cut flush to the perimeter, as the event photos show; AdvantageScope's field asset draws six whole tiles with their loops, a drawing choice), the HIVE, FLOWERs, GARDEN, LOADING ZONE, starts | The game manual and field CAD |
| Scoring, LEAVE, PARK, G409 (touching a falling spilled piece) | The game manual, applied the way `AutoSim` judges them |
| Where a spill lands (first touch 35–47 in from the CELL's wall, 1.1–1.4 s after the TIP starts) | Fitted to the team's 3 Oct films of a TIP (`FILMED_*` in `FieldSim`). Two films, so a rough fit; two Saline spills land at about 35–42 in and spread 12–20 in in the next 0.5 s, nearly all toward the wall ([saline-piece-physics.md](saline-piece-physics.md)) |
| The dwell before a TIP: a CELL at its tipping weight sits 0.25–3.4 s (median about 2 s) before the rocker leaves its stop; one a full POLLEN past it at most 0.5 s; pieces arriving during it shorten it | Eleven AUTO TIPs on the Saline stream ([saline-tip-measurements.md](saline-tip-measurements.md)), issue #167: `FieldSim.FILMED_TIP_DWELL_SECONDS`, drawn afresh per TIP. The split by load is the shape of those eleven (volleys 0.25–0.5 s, exactly three POLLEN 2–3.4 s); whether it is the pieces settling to the back skin or pivot friction is not known |
| What an alliance really scores in AUTO (a good one: one TIP and parking, 28–36; the best seen: 36, no alliance made two TIPs; most: 8–16) | The Saline Preview Event, 3 Oct 2026, 45 matches: [saline-preview-day2.md](saline-preview-day2.md). The simulator's 51–92 assume a partner that TIPs and shots that never bounce back out |
| How long a TIP takes: each one drawn from 0.58–1.12 s, from the rocker leaving its stop; the robot assumes 0.8 s | Match and test videos ([tip timing](tip-timing.md)); eleven event TIPs on the Saline stream agree, 0.65–0.9 s ([saline-tip-measurements.md](saline-tip-measurements.md)), hence `HiveTracker.Tuning.tipSeconds` 0.8 |
| How far pieces roll: 4 in/s² of drag on the tiles for POLLEN, 1.5 for NECTAR; a bounce on landing that kicks a piece on along its throw (within 60° of it), 0.1 of the landing speed | A match video and the 3 Oct films ([rolling](rolling.md)); at Saline 23 tracked pieces on the event tiles, POLLEN 3.9 in/s² (IQR 3.1–4.9), NECTAR about 1.5 (five tracks, low confidence), and nearly every spilled piece heading for the alliance wall ([saline-piece-physics.md](saline-piece-physics.md), issue #168; the kick was in any direction until 7 Oct 2026). The event tiles are the normal setting, not "slow" |
| The field is a half turn between the alliances, and so is the simulator: the same shots from the turned-around spot give the turned-around run through a TIP and its spill, to the field model's own 0.007 in | Event Field Setup Guide §8.3; `FieldSymmetryTest` fails any world-frame step that creeps in. A seed's variety (tile slopes, spill kicks, each piece's rolling resistance) is drawn in the field frame on purpose, one field for both alliances, so the same seed is a different run for blue; `BIOBUZZ_AUTO_VARIETY=0` for an exact comparison |
| Pieces, launcher and robots at the first event: rolling drag, shots in/rim/over (6 of 8 in), first shot 2.8–3.0 s, 0.15–0.25 s a shot, 57 in/s cruise, spill first touch and spread, the human NECTAR at 2.3 s | Nine AUTO spills and two volleys on the Saline stream, tracked by colour ([saline-piece-physics.md](saline-piece-physics.md)) |

## Guessed (marked `PLACEHOLDER_` or "assumed" in the code)

| What | The guess | Why it matters |
|---|---|---|
| How pieces bounce off tiles, walls, the HIVE, robots | restitution 0.45 / 0.5 / 0.2 / 0.1 | Where a spill ends up; how much a flap or hook keeps |
| Our robot's size and shape | The Rigid V as drawn in the 6 Oct CAD (`AutoStudyTest.drawnV`): body 15.12 × 15.24 in, 13.8 in roller, flap tips 17.8 in apart, 4 in tall | Every route spot; what the robot can reach. The extractor and scorer are not drawn yet |
| The intake | Takes a piece only when it touches the front, top under 5 in, one every 0.35 s, 85% of the time; holds what the transfer's lane fits, 12.43 in of pieces less the last one's radius (4 POLLEN, 3 NECTAR, or 3 NECTAR and a POLLEN; `FieldSim.LANE_LENGTH_IN`, Transfer v3, adopted 7 Oct 2026); a piece that arrives while it is busy bounces off the body | **The biggest lever found.** Every study line now counts the misses by why ("height", "interval", "beside", "chance"); in the Autos 15 a run are too high and 16–25 arrive while it is busy. Vectored rollers that hold pieces against the front would queue them instead; the roller height sets the 5 in |
| The turret | Turns at 240 deg/s (90 deg in 0.4 s). While the flywheels spin it pre-aims at the CELL of the end the robot is in (the mentor's rule, 9 Oct 2026), switching sides only past the HIVE frame's span (y 51 and 90.5, where nobody fires), so a launch, which always goes at the raised CELL, finds it on target; a launch waits until it is within 2 deg, and the timeline says how long each fire step waited for the turret and how soon after a side switch it was on target. Its travel is 720 deg centred on straight ahead (the mentor's margin inside the two-encoder turret's 1178 deg absolute window, 9 Oct 2026): between fire steps it unwinds on the drive once wound past 200 deg, so the long way round never lands inside a fire step; `BIOBUZZ_AUTO_TURRET_TRAVEL_DEG` tries another window, 0 a slip ring's unlimited turn | The 8 Oct 2026 design meeting put the turret on a continuous servo (`doc/motors-and-servos.md`, `RobotDesign.turretSlewRadPerS`); the 312 rpm motor it replaced did about 680. The simulation aimed at once until 9 Oct 2026; `BIOBUZZ_AUTO_TURRET_DEG_PER_S=0` gives that back for comparison. The fixed-turret Autos (L-Quals, R-Quals, Sister) have no turret to slew |
| The launcher | 2 s spin-up, 0.2 s a shot (0.45 until 7 Oct 2026, a guess; the "spring hood, slow feed" study design keeps it), 75°, a little spread (about 5 shots in 6 score); fires once within 2° of the CELL | When TIPs happen; how many shots score. With the spread set to zero and firing only when still and within 0.5°, shots still score only 92–96% from the firing spots; every study line reports the shots scored and why the misses missed. Saline: Infinity Tech's first shot 2.8–3.0 s after START, then one every 0.15–0.25 s, 6 of 8 in ([saline-piece-physics.md](saline-piece-physics.md)): the spin-up is about right, and the interval is now theirs |
| The wait before the rocker moves once the threshold is crossed | None | Measured at ~2 s at Saline; every TIP in the simulator is that much early until it is modelled |
| Shots that go in and bounce back out | None: a simulated miss never enters the CELL | At Saline the commentary called bounce-outs in most matches ([saline-preview-day2.md](saline-preview-day2.md)). The 8 shots that could be followed on the stream show a rim hit and an over-shot but no in-then-out ([saline-piece-physics.md](saline-piece-physics.md)); too few to settle it |
| Flaps, hooks, side walls | Thin plates of the drawn size, bouncing pieces with the guesses above | The rigid V's gain is pieces bouncing off its flaps into the intake: plausible, untested |
| Driving | Pedro following the route at speed 50 | How long each leg takes. Saline: the fastest robot seen cruised at 57 in/s with about 130 in/s² of acceleration; most moved at 20–25 ([saline-piece-physics.md](saline-piece-physics.md)) |

Nothing about our robot has been measured on a robot yet. The quickest numbers to measure, in order of how much
they move the results: the intake's width and time per piece, and the launcher. The TIP time and how far pieces
roll are now checked against video ([tip timing](tip-timing.md), [rolling](rolling.md)) and against the first event
([saline-piece-physics.md](saline-piece-physics.md)).

## How AdvantageScope shows the right robot

A `.wpilog` only stores numbers over time: where each robot is, where every piece is, the HIVE's angle, and
the position of each part of the robot. The layout (`sim-review/advantagescope-layout.json`) draws our robot as
**BIOBUZZ Robot**, the team's CAD (`cad/advantagescope/Robot_BIOBUZZ`, committed) with four moving parts: the FLOWER
extractor, the floating roller, the turret and the transfer's J arm. A baseline log ("rigid V") swings the extractor
down as the robot starts its path in to a FLOWER and up once it has backed 6 in clear, and turns the turret to the
CELL while the launcher spins; the roller and the J arm stay at rest (the simulator doesn't lift them yet). The simulator's body for that design is
the CAD's measurements (`doc/cad-6-oct.md`: 15.12 × 15.24 in, the V's plates), so the drawing and the simulation
agree to the extent the measurements do. Every other simulated design is a part of **BIOBUZZ Robot (designs)**,
generated from the simulator's own numbers (`RobotAssets.java`): a log of one says which part to show at the
robot (the others go 20 m under the field) and its file name says which (`<Auto>_<robot>_<date>`). A hook
swinging down is the same trick: the log moves that part as the simulator moved it.

So what you see in AdvantageScope is what the simulator did, frame by frame. What it can't tell you is whether
the simulator's guesses are right; the table above is where to look for that.
