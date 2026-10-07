# Inside the robot, in AdvantageScope

Issue #166. In the simulator's logs you can follow each game piece:
- it comes in under the roller, and the roller floats up over a NECTAR and spins while the intake runs;
- it queues up the rising lane, seats in the feeder cup, and is popped straight up through the spinning flywheels;
- the turret ring turns to aim, and the FLOWER extractor swings.

Before this, a piece vanished into the robot and reappeared as a shot.

The drawing is `RobotInternalsLog` (test code, `TeamCode/src/test/.../logging/`). It reads the simulator each loop and
writes its keys once the match is over. It draws what the simulator decided and changes no outcome: the same seeds
score the same with it as without it.

The robot is the mentor's CAD with transfer v2, from the CAD chat's commit 12d34bb. The launcher is fixed to the robot,
and only the turret ring turns: it will carry the hood that directs the shot.

## What you see

### Held pieces: `/Sim/GamePieces/Held/{Pollen,RedNectar,BlueNectar}`

These are the keys the layout already draws as game pieces. Each held piece is placed on the centre line, at its point
on the transfer's path (robot frame: X forward from the chassis centre, z up, inches). It rolls as it moves.

| Stage | Where | When |
|---|---|---|
| Taken in | From X 10.0 on the tiles, under the roller (axle at X 8.56), up the ramp (X 8.0 → 5.8, 0.05 → 0.9 in up), then up the lane: its ball-bottom line rises 17° to X −0.545, z 2.873, over the front feeder | At the lane's 27 in/s, until it reaches its place in the queue |
| Queued | The front piece seated in the cup at X −2.845 (centre 3.00 in up for a POLLEN, 3.67 for a NECTAR), the rest nose to tail behind it down the lane | Each piece moves up at 27 in/s when the one ahead leaves. While a piece is being fed, the front feeder holds the next one back at the lane's end |
| Firing | The piece in the cup waits 0.05 s while the feeders spin up. It then rises straight up the turret's axis through the flywheels, and over to the launcher's exit (the design's, 4 in behind the centre and 12 in up) | It arrives at the exit as the simulator launches it, 0.15 s after it started |

Preloads start in their places. A robot without the transfer (any design without `laneCapacity`) has its held pieces
placed on the same path, without the motion.

### Moving parts: `<robot>/Internals/Components`

A Pose3d[] of the `Robot_BIOBUZZ` model's eight components, in the order agreed with the CAD chat. The spinning parts
turn at a display rate, 2 turns a second, while they run: real roller and flywheel speeds would alias at the log's
50 Hz, so the spin only shows that the part is running.

| # | Component | Pose | Driven by |
|---|---|---|---|
| 0 | FLOWER extractor | About its shaft (0.25298, 0, 0.1143) m. 0° down, 146° stowed | The simulator's `Extractor/Down`: down on the way to a FLOWER, up as the robot leaves |
| 1 | Roller carriage, motor, float plates | Straight up, 0 to 1.3 in | Rises until the roller clears the pieces passing under it, less the 0.4 in a POLLEN squeezes the tread. So only a NECTAR lifts it, by about 0.8 in |
| 2 | Turret ring (the bearing's inner race and its gear; the hood later) | About +Z through (−0.072215, 0.004) m, 4 mm left of the centre line | Turns toward the raised CELL's aim point while the launcher is spun up or firing, and holds its last angle otherwise. Straight ahead at the start |
| 3 | Front feeder wheel | About +Y through (−0.013843, 0, 0.039624) m | Spins while a piece is being fed |
| 4 | Intake roller | About +Y through its axle (0.217424, 0, 0.084963) m, plus the carriage's rise | Spins while the intake runs; the bottom moves rearward |
| 5, 6 | Left and right flywheel axles, two 96 mm wheels each | About −X and +X through (−0.0762, ±0.0925, 0.1688) m | Spin while the launcher is spun up, both throwing the piece up |
| 7 | Rear feeder wheel | About −Y through (−0.130683, 0, 0.039624) m | Spins while a piece is being fed |

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

1. **The model.** Run `tools/advantagescope/setup-advantagescope.ps1` on this branch. It installs `Robot_BIOBUZZ`
   (the CAD robot, with the Limelight as its camera) and puts both layouts in **Downloads**. Or, from the zip:
   copy the `Robot_BIOBUZZ` folder into AdvantageScope's `userAssets` folder, replacing the old one.
2. **The layout.** In AdvantageScope, **File → Import Layout…** → `advantagescope-layout-internals.json`.
3. **A log.** **File → Open Log(s)…** → a `.wpilog`, then press Space to play.

The layout's tabs:

| Tab | What |
|---|---|
| **Field** | The whole field, as before, with the robot posed from `/Internals/Components` |
| **Inside the robot** | Orbits the robot, close in. Drag to look in under the turret, at the lane, the cup and the feeders |
| **Limelight** | The camera's view: the robot's first camera (`config.json`, 4.0 in ahead of the centre, 14 in up, 45° up) |
| **Internals** | The readouts above, with `/Sim/Robot/Held` |

## Sample logs

`sim-review/advantagescope-internals.zip` has the shoot-while-extracting routes on "rigid V, turret transfer", seed 3,
with the transfer's 0.5 s feed:
- `qual-right-v-flower-first-l2600`: the user's plan, straight to the far FLOWER and all 8 fired from the seat;
- `qual-right-v-stream-x200-leave1800`: the partner shoots, and we stream at the far FLOWER;
- `qual-stages-angled-v-stream-leave`;
- `qual-stages-wall-v-stream-x700-leave`. **Its robots collide at about 10 s, in all 60 runs:** the simulator chat moved
  the wall partner's PARK (both robots PARK), and this route isn't refitted to it yet.

The extractor seats with the FLOWER's centre 4.59 in ahead of the face. It comes down on the way to the FLOWER, so
it's fully down before the robot drives in (the mentor's review, 7 Oct). The zip's README has the scores.
Over 60 runs, flower-first scores 67.6 (TIP 3 in 37 of 60) and the right-side stream 70.9 (TIP 3 in 47).

Made with:

```
BIOBUZZ_AUTO_STUDY="QualRightVFlowerFirstL2600Auto,PartnerPreloadsRightAuto@50;QualRightVStreamX200Leave1800Auto,PartnerPreloadsRightAuto@50;QualStagesAngledVStreamLeaveAuto,PartnerAngledParkAuto@50;QualStagesWallVStreamX700LeaveAuto,PartnerStage19SideParkAuto@50" \
BIOBUZZ_AUTO_DESIGNS="rigid V, turret transfer" BIOBUZZ_AUTO_PARTNER_DESIGN="spring hood" BIOBUZZ_AUTO_PARTNER_SPEED=40 BIOBUZZ_AUTO_SEEDS=3 BIOBUZZ_AUTO_LOGS=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*'
```

The partner's design and speed are the baselines' (`baselines_v.py`). Without them the partner takes our design, and
its start no longer touches the wall. The logs land in `TeamCode/build/sim-logs/`.

They were made without Gradle: the test sources compiled with plain `javac` against Pedro core 3.0.1, Ivy 1.1.1 and
JUnit 4.13.2 from Maven Central, with a one-line stub for Panels' `@Configurable`.

## Tests

`RobotInternalsLogTest` checks the drawing against the transfer's numbers:
- the path's ends, the lane's end, and a seated piece's height in the cup;
- the column straight up the turret's axis;
- each component is the identity at rest and turns about its own axis;
- the intake roller spins about its axle and rises with the carriage;
- each flywheel and feeder spins about its own axle;
- the extractor's pose matches `AutoSim.cadComponents`.
