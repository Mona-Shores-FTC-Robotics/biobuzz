# Inside the robot, in AdvantageScope

Issue #166. In the simulator's logs you can follow each game piece:
- it comes in under the roller, and the roller floats up over a NECTAR;
- it queues in the lane, then climbs the J up the turret axis and leaves the turret as a shot;
- the turret turns to aim, and the FLOWER extractor swings.

Before this, a piece vanished into the robot and reappeared as a shot.

The drawing is `RobotInternalsLog` (test code, `TeamCode/src/test/.../logging/`). It reads the simulator each loop and
writes its keys once the match is over. It draws what the simulator decided and changes no outcome: the same seeds
score the same with it as without it.

## What you see

### Held pieces: `/Sim/GamePieces/Held/{Pollen,RedNectar,BlueNectar}`

These are the keys the layout already draws as game pieces. Each held piece is placed on the centre line, at its point
on the transfer's path (`doc/transfer.md` on `spike/164-transfer`, robot frame: X forward from the chassis centre,
z up, inches). It rolls as it moves.

| Stage | Where | When |
|---|---|---|
| Taken in | From X 10.0 on the tiles, under the roller (axle at X 8.56), up the ramp (X 8.0 → 5.8, onto the lane floor 0.9 in up) and back along the lane | At the lane's 27 in/s, until it reaches its place in the queue |
| Queued | Nose to tail. The front piece's centre is at X −0.64 for a POLLEN or +0.73 for a NECTAR, and each further piece sits half of each neighbour's diameter on (`FieldSim.hasRoom`'s numbers). The 4th POLLEN sits at 7.76, in the roller's grip | Each piece moves up at 27 in/s when the one ahead leaves |
| Firing | The front piece waits 0.05 s while the J spins up. It then goes round the outer J (its centre on a circle about the J-wheel's axle), up the turret axis (NECTAR at X −3.16, POLLEN at −3.56) and over to the launcher's exit (the design's, 4 in behind the centre and 12 in up) | It arrives at the exit as the simulator launches it, 0.15 s after it started |

Preloads start in their places. A robot without the transfer (any design without `laneCapacity`) has its held pieces
drawn single file on the lane floor, as before.

### Moving parts: `<robot>/Internals/Components`

A Pose3d[] of the `Robot_BIOBUZZ` model's components, in the order agreed with the CAD chat:

| # | Component | Pose | Driven by |
|---|---|---|---|
| 0 | FLOWER extractor | About its shaft (0.25298, 0, 0.1143) m. 0° down, 150° stowed | The simulator's `Extractor/Down` (`AutoSim.Bot.extractor`: down on the approach to a FLOWER, up as the robot leaves) |
| 1 | Roller, motor and carriage | Straight up, 0 to 1.3 in | Rises until it clears the pieces passing under it, less the 0.4 in a POLLEN squeezes the tread. So only a NECTAR lifts it, by about 0.8 in, as `doc/transfer.md` gives (0.85). |
| 2 | Turret | About +Z through X −3.17 in | Turns toward the raised CELL's aim point while the launcher is spun up or firing, and holds its last angle otherwise. 0 (straight ahead) at the start |
| 3 | J arm and J-wheel | About +Y through the arm's pivot (X 0.72, z 3.36 in; a 60 mm arm at 30°). Positive lifts the wheel, 29.1° at most (1.2 in at the axle) | Lifts until the wheel clears a piece, less its 0.1 in grip on a POLLEN. So a NECTAR going round the J lifts it about 26°, and a POLLEN barely moves it |

`/Internals/Components` replaces `/BodyShape/Components` for the CAD model. The simulator still writes
`BodyShape/Components` for the older layouts.

`Robot_BIOBUZZ` has all four (the CAD chat's commit 04324c1). Component 2 is only the turret's bearing for now: the
launcher is drawn once it has an outline, on the same axis.

### Readouts: `<robot>/Internals/*`

These are graphed on the layout's **Internals** tab. Each is written only when it changes.

| Key | Unit | What |
|---|---|---|
| `ExtractorDeg` | ° | 0 down, 150 stowed |
| `RollerRiseIn` | in | The roller's float |
| `TurretYawDeg` | ° | The turret's angle from straight ahead, positive to the left |
| `TurretErrorDeg` | ° | How far the drawn turret lags the aim (see below) |
| `JArmDeg` | ° | The J arm's lift |
| `Climbing` | count | Pieces in the J and turret now (0 or 1) |

`<robot>` is empty for our robot, and `/Partner` for the partner.

## What the simulator doesn't track, and how it's drawn

The simulator knows which pieces a robot holds and in what order, when each one comes in, and when it's launched. It
doesn't know where a piece is inside the robot. Everything between those events is interpolated from the transfer's
timings, as above.

- **The climb starts before the fire command.** The simulator launches a piece the moment the launcher allows it,
  with no transfer delay. The drawing puts the 0.15 s climb before that launch, so the first piece of a volley starts
  climbing 0.15 s before `LaunchAll` appears in the events. The shots leave at the simulator's times.
- **The turret's speed is a placeholder,** 360°/s (`PLACEHOLDER_TURRET_DEG_PER_S`). The simulator aims instantly; the
  drawing turns at a finite rate, so you can see it turn. `TurretErrorDeg` shows the lag. It's large only for about
  0.4 s after the raised CELL changes, which on the three qualifier Autos is well before the next shot.
- **The roller and the J arm** come from the pieces' drawn places, not from any physics. The simulator's own intake
  rule decides which pieces get in.
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
| **Inside the robot** | Orbits the robot, close in. Drag to look in under the turret, at the lane and the J |
| **Limelight** | The camera's view: the robot's first camera (`config.json`, 4.0 in ahead of the centre, 14 in up, 45° up) |
| **Internals** | The readouts above, with `/Sim/Robot/Held` |

## Sample logs

One per qualifier Auto, on the "rigid V, transfer" design (0.25 s shots, the lane's capacity), seed 3 for each. Made with:

```
BIOBUZZ_AUTO_STUDY="QualRightVAuto,PartnerPreloadsRightAuto@50;QualStagesAngledVAuto,PartnerAngledParkAuto@50;QualStagesWallVAuto,PartnerStage19SideParkAuto@50" \
BIOBUZZ_AUTO_DESIGNS="rigid V, transfer" BIOBUZZ_AUTO_SEEDS=3 BIOBUZZ_AUTO_LOGS=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*'
```

The logs land in `TeamCode/build/sim-logs/`. These were made without Gradle, by compiling the test sources with plain
`javac` against Pedro core 3.0.1, Ivy 1.1.1 and JUnit 4.13.2 from Maven Central, with a one-line stub for Panels'
`@Configurable`.

## Tests

`RobotInternalsLogTest` checks the drawing against the transfer's numbers:
- the path's ends, and the queue's front on the lane floor;
- the columns up the turret axis;
- a NECTAR lifts the J arm, and a POLLEN or a queued piece doesn't;
- each component is the identity at rest and turns about its own axis;
- the extractor's pose matches `AutoSim.cadComponents`.
