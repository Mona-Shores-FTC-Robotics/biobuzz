# Inside the robot, in AdvantageScope

Issue #166. In the simulator's logs you can follow each game piece:
- it comes in under the roller, and the roller floats up over a NECTAR and spins while the intake runs;
- it queues along the flat lane, is held between the feeder and a sprung pad, and is driven straight up through the spinning flywheels;
- the turret ring turns to aim, and the FLOWER extractor swings.

Before this, a piece vanished into the robot and reappeared as a shot.

The drawing is `RobotInternalsLog` (test code, `TeamCode/src/test/.../logging/`). It reads the simulator each loop and
writes its keys once the match is over. It draws what the simulator decided and changes no outcome: the same seeds
score the same with it as without it.

The robot is the mentor's CAD with transfer v3, from the CAD chat's commit 2f78001. The launcher is fixed to the robot,
and only the turret ring turns: it will carry the hood that directs the shot.

## What you see

### Held pieces: `/Sim/GamePieces/Held/{Pollen,RedNectar,BlueNectar}`

These are the keys the layout already draws as game pieces. Each held piece is placed on the centre line, at its point
on the transfer's path (robot frame: X forward from the chassis centre, z up, inches). It rolls as it moves.

| Stage | Where | When |
|---|---|---|
| Taken in | From X 10.0 on the tiles, under the roller (axle at X 8.56), up the ramp (X 8.0 → 5.7, 0.05 → 1.3 in up), then along the flat lane (ball-bottom 1.3 in up) | At the lane's 27 in/s, until it reaches its place in the queue |
| Queued | The lead piece held between the feeder and the pad against the backstop: a POLLEN's centre at X −2.455, 0.15 in left of the centre line; a NECTAR's at −2.045 on the turret's axis, 0.21 in right (2.70 / 3.10 in up). It moves over to that side over the last inch. The rest sit nose to tail behind it, each the two radii further forward: 4 POLLEN at −2.455, 0.345, 3.145, 5.945; 3 NECTAR at −2.045, 1.555, 5.155 | Each piece moves up at 27 in/s when the one ahead leaves. While a piece is being fed, the next waits right behind the hold and rolls in when it has gone |
| Firing | The held piece waits 0.05 s while the feeders spin up. It then rises up the column on the turret's axis (X −2.045) through the flywheels' nip (6.65 in up) and the turret's bore to the exit's height, 12 in. The simulator's shot starts there | It arrives at the exit as the simulator launches it, 0.15 s after it started |

Preloads start in their places. A robot drawn without the CAD model (any design other than "rigid V" and its variants) has its held pieces
placed on the same path, without the motion.

### Moving parts: `<robot>/Internals/Components`

A Pose3d[] of the `Robot_BIOBUZZ` model's eight components, in the order agreed with the CAD chat. The spinning parts
turn at a display rate, 2 turns a second, while they run: real roller and flywheel speeds would alias at the log's
50 Hz, so the spin only shows that the part is running.

| # | Component | Pose | Driven by |
|---|---|---|---|
| 0 | FLOWER extractor | About its shaft (0.25298, 0, 0.1143) m. 0° down, 146° stowed | The simulator's `Extractor/Down`: down on the way to a FLOWER, up as the robot leaves |
| 1 | Roller carriage, motor, float plates | Straight up, 0 to 1.3 in | Rises until the roller clears the pieces passing under it, less the 0.4 in a POLLEN squeezes the tread. So only a NECTAR lifts it, by about 0.8 in |
| 2 | Turret ring (the bearing's inner race and its gear; the hood later) | About +Z through (−0.051895, 0.004) m, 4 mm left of the centre line | Turns toward the raised CELL's aim point while the launcher is spun up or firing, and holds its last angle otherwise. Straight ahead at the start |
| 3 | Feeder: two 72 mm wheels on a shaft along X, left of the held piece | About −X through (−0.051943, 0.072822, 0.082550) m | Spins while a piece is being fed, driving it up |
| 4 | Intake roller | About +Y through its axle (0.217424, 0, 0.084963) m, plus the carriage's rise | Spins while the intake runs; the bottom moves rearward |
| 5, 6 | Left and right flywheel axles, two 96 mm wheels each | About −X and +X through (−0.05588, ±0.0925, 0.1688) m | Spin while the launcher is spun up, both throwing the piece up |
| 7 | Sprung foam pad opposite the feeder, hinged along X at its foot | About +X through (−0.051943, −0.04445, 0.03683) m | Doesn't spin. Swings out 26° while a NECTAR is in the feeder (held, or in its first 2 in up), otherwise at rest |

`/Internals/Components` replaces `/BodyShape/Components` for the CAD model. The simulator still writes
`BodyShape/Components` for the older layouts.

### Readouts: `<robot>/Internals/*`

These are graphed on the layout's **Internals** tab. Each is written only when it changes.

| Key | Unit | What |
|---|---|---|
| `ExtractorDeg` | ° | 0 down, 146 stowed |
| `RollerRiseIn` | in | The roller's float |
| `TurretYawDeg` | ° | The turret's angle from straight ahead, positive to the left |
| `TurretErrorDeg` | ° | How far the drawn turret lags the aim (see below) |
| `Climbing` | count | Pieces being fed up to the launcher now (0 or 1) |

`<robot>` is empty for our robot, and `/Partner` for the partner.

## What the simulator doesn't track, and how it's drawn

The simulator knows which pieces a robot holds and in what order, when each one comes in, and when it's launched. It
doesn't know where a piece is inside the robot. Everything between those events is interpolated from the transfer's
timings, as above.

- **The feed starts before the fire command.** The simulator launches a piece the moment the launcher allows it,
  with no transfer delay. The drawing puts the 0.15 s feed before that launch, so the first piece of a volley starts
  rising 0.15 s before `LaunchAll` appears in the events. The shots leave at the simulator's times.
- **The turret's speed is a placeholder,** 360°/s (`PLACEHOLDER_TURRET_DEG_PER_S`). The simulator aims instantly; the
  drawing turns at a finite rate, so you can see it turn. `TurretErrorDeg` shows the lag. It's large only for about
  0.4 s after the raised CELL changes, which on the routes here is well before the next shot.
- **The roller's float** comes from the pieces' drawn places, not from any physics. The simulator's own intake rule
  decides which pieces get in.
- **A piece shot, missed and taken in again** joins the queue again, and is drawn rising to the launcher again before
  its next launch.
- **Pieces leaving another way** (setting a preload down, outtake) vanish from the robot and appear on the field where
  the simulator puts them.

## Install the layout

You need AdvantageScope set up as in the root README's *How to watch*.

1. **The model and the logs.** The double-click launcher, or `tools/advantagescope/setup-advantagescope.ps1 -Open <key>`
   (the root README): it installs `Robot_BIOBUZZ` (the CAD robot, with the Limelight as its camera), fetches the
   published logs and opens the one picked.
2. **The layout.** Since 7 Oct 2026 these tabs are in the one layout, `sim-review/advantagescope-layout.json`, which the
   script copies to **Downloads** and says when it changed: **File → Import Layout…** → `advantagescope-layout.json`.

The layout's tabs:

| Tab | What |
|---|---|
| **Field** | The whole field, as before, with the robot posed from `/Internals/Components` |
| **Inside the robot** | Orbits the robot, close in. Drag to look in under the turret, at the lane and the feeders |
| **Limelight** | The camera's view: the robot's first camera (`config.json`, 4.0 in ahead of the centre, 14 in up, 45° up) |
| **Internals** | The readouts above, with `/Sim/Robot/Held` |

## Sample logs

Every Auto in the root README's table is published to `sim-results` with these channels (the Simulate Auto
workflow runs the same simulator), so the launcher's logs are the samples; the review zip this branch carried
until 7 Oct 2026 is gone with it.

## Tests

`RobotInternalsLogTest` checks the drawing against the transfer's numbers:
- the path's ends, and a held piece's height between the feeders;
- the queue's places, against the CAD chat's own;
- the column straight up the turret's axis;
- each component is the identity at rest and turns about its own axis;
- the intake roller spins about its axle and rises with the carriage;
- each flywheel and feeder spins about its own axle;
- the extractor's pose matches `AutoSim.cadComponents`.
