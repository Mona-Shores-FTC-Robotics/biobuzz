# Changing the robot: a one-page map for mentors

You are at the robot, something is wrong or needs a number, and you have not read the code. This
page says where to go. Everything else (why it is built this way) is in `CLAUDE.md` and
`TeamCode/README.md`; you do not need either to make the changes below.

Paths are under `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.

## Want to … go to

| Want to | Go to | Notes |
|---|---|---|
| Flip a motor that runs the wrong way | the `reversed` field at the top of that subsystem: `IntakeSubsystem.reversed`, `FeederSubsystem.reversed`, `PrefeedSubsystem.reversed`, `LauncherSubsystem.leftReversed` / `rightReversed` | Change it in Panels first to prove it, then write it into the file |
| Change a power or a speed | `IntakeSubsystem.inPower` / `outPower`, `FeederSubsystem.feedPower` / `reversePower`, `PrefeedSubsystem.inPower` / `outPower`, `LauncherSubsystem.targetTicksPerSecond`, `TeleOpConfig.normalSpeed` / `turboSpeed` / `slowSpeed` | Same: Panels, then the file |
| Set the FLOWER stick's two positions | `FlowerStickSubsystem.RETRACTED` / `EXTENDED` | Servo positions, 0 to 1 |
| Say how long the Auto feeds when it fires | `LaunchCards.launchAllFeedS` / `launchOneFeedS` | Seconds the feeder runs after the launcher is at speed |
| Make fire wait for the launcher or the aim | `TeleOpConfig.enforceAtSpeedGate` / `enforceAimGate` (`aimToleranceDeg`) | Off: fire feeds at once and the match log says when a gate would have blocked. Turn one on only once the log shows it helps |
| Show RPM instead of ticks per second | `IntakeSubsystem.ticksPerRev`, `LauncherSubsystem.ticksPerRev` | Read off the motor's label; the field's comment has the goBILDA table |
| Tell the camera where it sits | `vision/CameraMount`: `mountForwardIn`, `mountLeftIn`, `mountUpIn`, `pitchDeg` | Inches from the robot centre and degrees up; measured with a tape, never guessed |
| Move a button or add one | `controls/ControlScheme.layout()` for which input; a new `controls/Action` and its line in `opmodes/BasicDriveTeleOp.onInit()` for a new job | The Driver Station's Controls page and the printed cheat sheet (`TeamCode/build/cheat-sheets/`, after `./gradlew testDebugUnitTest`) are generated from the scheme, so there is nowhere else to document a control |
| See what the robot is doing | the Driver Station's Match page; gamepad 1 Share (Back) cycles Match / Controls / Robot | Panels at `http://192.168.43.1:8001` has the same numbers as graphs |
| Choose the alliance | Square (X) = blue, Circle (B) = red on either gamepad during INIT | The camera proposes one; nothing moves in an Auto until one is chosen |
| Add a mechanism | copy `subsystems/ExampleSubsystem`, add a `public final` field in `Robot`, build it in the constructor, add it to the list | The list is the only registration |
| Move a wire to another port | that robot's `TeamCode/src/main/res/xml/robot_<team>.xml` | Java never knows a port; two robots wired differently differ only in their XML |
| Add a device | `hardware/DeviceNames` (the name), the `COMPETITION_ROBOT` list there, and both robots' XML | The build fails if the XML and the list disagree, which is the point |
| Check what is plugged in | **Validate Hardware**, in the Driver Station's **Utility** menu | Lists every device the active config expects and whether it answered |
| Change an Auto's path | its `.pp` in `TeamCode/autos/`, in the Visualizer, then export | Never edit the generated Java in `opmodes/auto/generated/` |
| Make an Auto card do something new | `opmodes/auto/AutoRegistration` (which names exist) and `opmodes/auto/LaunchCards` (what the launcher ones do) | A card the Auto uses but the robot has not registered stops the Auto at INIT and says so |

## The loop, in five lines

Every OpMode that drives the robot extends `RobotOpMode`. Each loop:

1. Clear the hubs' sensor cache, so every sensor is read once this loop.
2. Run the OpMode's `onLoop()`: read the gamepads, tell subsystems what you want (`robot.intake.intake()`, `robot.drive.drive(f, s, t)`), write the Match page.
3. Run the scheduler, which calls every subsystem's `update()` once. That is where motors are actually written.
4. Time the loop. The Match page shows it; anything that blocks (`sleep`, waiting loops) shows up here.

So a subsystem has two halves: the methods the OpMode calls to *ask* for something, and `update()`, which *does* it once per loop. If a mechanism is not moving, check both: was it asked, and did `update()` run (is it in `Robot`'s list)?

## Where the OpModes are

- **TeleOp list:** Basic Drive, and Basic Drive (robot-centric) for a match where the Pinpoint has failed. Only what a drive team runs at a competition.
- **Autonomous list:** the six Autos and Hold Still.
- **Utility menu** (Driver Station 11.2 or later): everything else. Validate Hardware, Drive Motor Check, Loop Time Baseline, Launcher Bench, Turret Bench, and the Vision tools. Pedro's tuners are in the TeleOp list because they are Pedro's code, and their screens say A, B, X, Y: Cross, Circle, Square, Triangle.

## Deploying

- **Sloth Load** (the run configuration of that name in Android Studio): a few seconds, the normal way. Use it for Java changes.
- **Full TeamCode install** (the ordinary Run): required after any change to a `.gradle` file, a dependency, `FtcRobotController/`, or any `robot_*.xml`. Sloth Load after one of those silently keeps the old code or loses the configs.
- After a full install, the active config can need re-activating: Driver Station → Configure Robot → the robot's config → Activate.

## Where numbers get written back

Every number you tune today is a `public static` field marked `@Configurable`. Panels lets you change it live; **Panels forgets on restart**. The rule:

1. Change it in Panels at the robot until it is right.
2. Write the number on the issue for that mechanism, with the robot and the date.
3. Put it in the field in the file, in a PR, with the same robot and date in the comment.

A number nobody has measured is `NaN`, or is marked "not measured" in its comment. That is deliberate. Do not replace it with a guess; replace it with what the robot said.

## When something fails

| Symptom | First thing to check |
|---|---|
| A wheel or roller runs the wrong way | its `reversed` field, in Panels |
| A launcher wheel reads 0 speed and the Match page shows a red Launcher line | the encoder cable; the code has already cut that wheel. Spin down (operator B) and up again after fixing it |
| Validate Hardware lists a device as missing | it is unplugged, on the wrong port, or the active config is not this robot's |
| An Auto does not move at PLAY | no alliance chosen (X or B at INIT), or the Match page says what it is waiting for |
| Everything is slow | the loop time on the Match page; something is blocking in a subsystem's `update()` |
| Configs vanished from the Driver Station list | a Sloth Load after an XML change; do a full install |

## Not on this page

Pedro tuning (`pedro/robots/Robot<team>.java`, pasted from the tuners), the HIVE tracker, the camera's field points, the match log and the simulator. Each has a section in `TeamCode/README.md` and an issue on the board; none of them is a thing to change at the robot without reading first.
