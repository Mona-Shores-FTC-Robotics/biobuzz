# BIOBUZZ — Mona Shores FTC (teams 19429 & 20245)

FTC 2026–27, game **BIOBUZZ**. Two competition robots with identical hardware, plus test rigs,
from one codebase. A fork of the stock `FtcRobotController` v12.0 SDK project.

**The stack is Sloth + Pedro + Ivy + Panels, and nothing else.** Use them the way this file
describes, so every OpMode looks like every other one. Longer reasoning for anything here is in
`TeamCode/README.md`; follow the `→` pointers.

> **About this file.** Claude reads all of it at the start of every session and treats it as
> current and binding. So it holds only rules a session would otherwise get wrong, stated in the
> present tense. No history, no status, no counts: history lives in git and the README, status
> lives in issues. Change a rule here in the same PR that changes it in the code.

## How the code is structured

Paths below are under `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.

| Piece | Rule |
|---|---|
| `Robot` | Owns every subsystem, in one list. That list is the only registration: it is how a subsystem is initialized, updated and stopped. |
| `subsystems/Subsystem` | `initialize()` / `update()` / `stop()`. `update()` is one non-blocking step; `stop()` is final teardown only — a mechanism that pauses needs its own name (`idle()`, `spinDown()`). `health()` is one line for the match log on what it found at init; a motor-only subsystem says "presence unknown", never "present". |
| `opmodes/RobotOpMode` | Owns the loop. OpModes that run the robot extend it and fill in `onInit`/`onLoop`; `LoopContractTest` fails any that set bulk caching or call `Scheduler`/`robot.stop()` themselves. Standalone diagnostics (`ValidateHardware`, `LoopTimeBaseline`) and rigs are the exception. Only match OpModes are `@TeleOp` / `@Autonomous`; every diagnostic, bench and tuner is `@Utility`, so the Driver Station's Utility menu holds them and the TeleOp list holds what a drive team runs. |
| `subsystems/DriveSubsystem` | The drivetrain, and the worked example: the OpMode says what it wants (`drive(...)`), `update()` does it. Drives robot-centric if the Pinpoint is missing. Also the Auto Builder's `AutoDrive`: a path or hold hands the loop to Pedro's `Follower` until the next `drive(...)`. |
| `controls/` | `ControlScheme` is the only place an input is chosen: it maps each `Action` to an `Input`, one layout per scheme (A, B; picked in Panels by `TeleOpConfig.scheme`, read once at INIT). The TeleOp asks `controls.held(Action.FIRE)` / `controls.on(Action.X)`, never a raw button; a new scheme is one new layout. `Input` cannot name Share, Options, the touchpad or PS. `Bindings` underneath labels every binding, and the DS Controls page and the printed cheat sheet (`CheatSheet`, written by `CheatSheetTest` to `TeamCode/build/cheat-sheets/`) are generated from the same layout, so never document controls anywhere else. `Display`: the DS pages; write the Match page in `onLoop()`, give a subsystem a Robot-page block by overriding `describe()`. |
| `localization/` | CELL sighting → gated position fix (`TagGate`, `CellFix`), field points, start and reset positions, the start check. The filter is Pedro's. See "Localization" below. |
| `hardware/` | Device names, robot identity, active config — see below. |
| `pedro/` | `Constants.java`, `Tuning.java`, `RobotConstants.java` and `robots/` (tuned values, one file per robot) are ours. `pedro/procedures/**` is upstream: never edit it. |
| `autokit/` | The Auto Builder's runtime: the cards a generated Auto calls — commands, paths, the one branching block (`firstOf`: wait for a trigger, at most some time, optionally while a command runs) and the endgame guard, which cuts a wait or a command when time runs short but never a path. Depends only on Ivy and Pedro, never on `Robot` or the SDK. |
| `opmodes/auto/` | `BuiltAuto` runs a generated Auto; each Auto is a five-line `*OpMode extends BuiltAuto`. `AutoRegistration` is the only list of command and trigger names an Auto may use; a name whose mechanism does not exist yet is an explicit placeholder in `PLACEHOLDERS`, never a silent stub. `opmodes/auto/generated/**` is written by the Auto Builder's export: never edit it, change the `.pp` and export again. |
| `util/` | Shared helpers: `LoopTimer`, `FieldView`, `AccelLimiter`, `Alliance`, `WelfordVariance`. Reuse before writing another. |
| `launcher2/` | **Test rig**, deliberately standalone: no `Robot`, no `Subsystem`. Don't copy its pattern into robot code. |

**Adding a mechanism:** copy `subsystems/ExampleSubsystem`, add a `public final` field on `Robot`,
build it in the constructor, add it to the list. If `Subsystem` doesn't fit your mechanism, change
the interface in your PR — don't work around it.

## Hardware names and robot configs

Three layers, each written in exactly one place:

| Layer | Question | Lives in | Differs per robot? |
|---|---|---|---|
| Name | What does the code call it? (`frontLeft`) | `hardware/DeviceNames` | No |
| Port | Where is it plugged in? | `TeamCode/src/main/res/xml/robot_<name>.xml` | Yes — the only place wiring is written |
| Identity | Which robot is this, and what should it have? | `hardware/RobotIdentity` (config name → device list) | One line per robot |

- **A device name appears only in `DeviceNames`.** Never a string literal, never a field on an
  `@Configurable` object. A name is identity, not tuning.
- **Java never knows a port.** Two robots wired differently differ only in their XML.
- **Adding a device:** the `DeviceNames` constant, the device list of every robot that has it
  (`COMPETITION_ROBOT` covers both), and the element in each of those robots' XML — same PR.
- **Adding a robot or rig:** a `robot_<name>.xml` plus a `RobotIdentity` line. A rig with
  different hardware also gets its own device list (`LAUNCHER_RIG` is the example).
- **Per-robot numbers key off `ActiveConfig.requireIdentity()`** — never the Wi-Fi name, hub
  serial, or a fallback default. An unknown config fails loudly.
- **Look up hardware in the subsystem constructor, and never swallow a missing device.** No
  catch-and-return-null helpers. An optional device is an explicit, named decision.
- A continuous-rotation servo is `Kind.CR_SERVO` and `<ContinuousRotationServo>`, asked for as
  `CRServo`; it shares the servo ports. The turret's encoder has no device of its own: it is read
  through the encoder socket of `DeviceNames.TURRET_ENCODER_HOST` (a motor whose own encoder the
  code never uses for control), and only turret code may reset that count.
  → README § "The turret: a servo on a servo port, an encoder on a motor's encoder socket"
- XML: no `name` attribute on `<Robot>` (the filename is the identity). For I2C, `bus` is what
  the SDK reads; keep `port` equal to it.
- The Limelight is an `<EthernetDevice>` directly under `<Robot>` (`Kind.ETHERNET`, checked by
  `ipAddress`, no port). The SDK builds it from the XML whether or not a camera is plugged in, so
  "in the hardware map" never means "connected": check `robot.vision.isConnected()`.
- `RobotConfigXmlTest` enforces all of this in CI. On a robot: Activate the config once per hub,
  then run **Validate Hardware**.
  → README § "Robot configuration"

## The libraries

**Pedro** (`com.pedropathing` 3.x, tuning via AutoTune). All drivetrain and localizer
construction goes through `Constants.createDrivetrain/createLocalizer/createAlgorithm/create`.
Tuner output is pasted into **that robot's** file, `pedro/robots/Robot<team>.java` — the active
config picks which runs. Values are measured, never guessed.
`createAlgorithm()` throws until the Foresight Tuner has run, so an OpMode that must work on an
untuned robot uses the drivetrain and localizer directly (`BasicDriveTeleOp` is the pattern).
→ README § "The `pedro` package"

**Ivy** (commands). The `Scheduler` is always on, in TeleOp and Autonomous: every subsystem
updates through `periodic()`, and behaviour beyond that is commands. Each loop runs clear bulk
cache → `onLoop()` → `Scheduler.execute()`, so inputs reach hardware in the same loop. No default
commands (not in Ivy 1.1.1). Subsystems share state through read-only accessors, never statics —
the single exception is `controls/Handoff`, which carries alliance, pose and the turret's zero from
one OpMode to the next and is written and read only by `RobotOpMode`: the Auto's final record, and
the latest loop's state every loop, so a crashed Auto or a restarted TeleOp resumes where the robot
is. The turret zeroes itself: a carried zero is taken, and otherwise the turret is assumed at home
and zeroes at PLAY; the zero button on a gamepad always wins (`TurretZeroPolicy`).
`RobotOpMode` logs an uncaught exception to the match log and rethrows it; it never swallows one.

**Localization.** The Pinpoint carries the pose, through Pedro's `FusionLocalizer`. The camera
sets it and may correct it, only through `localization/`: every frame passes `TagGate` (height,
range, outlier, tag count, field bounds) before `CellFix` uses it. Two or more gated tags on settled
CELLs give the whole pose (each tag's field position is known, since a TIP turns the row about its
own line), which seeds an unreferenced pose. Once the pose is referenced the camera gives position
only, with the Pinpoint's heading: `relocalizeFromCamera()` moves x and y and keeps the heading.
Stationary fixes (`stillFixes`) and continuous fixes (`continuousFixes`) into the filter are both
off until a measurement shows they help. The reset spot (`ResetPositions`, NaN until measured) is a
fixed, measured pose per alliance, never "the nearest corner". Relocalizing or resetting never moves
the driver's field-centric forward. Read the pose from `robot.drive`, never from the
Pinpoint or the Limelight directly. Anything that drives or aims from field coordinates checks
`robot.drive.poseReferenced()` first and degrades without it; driving itself never needs it.
`HiveFieldPoints` is the field CAD's rocker model (axle on the centre line, one row radius),
checked against the field once; `FlowerFieldPoints` is the official field model's four FLOWER
centres; `StartPositions`, `FieldFrame` and `LocalizationTuning` hold
measured field facts — NaN or empty until measured, never guessed. An Autonomous declares its start by
overriding `startPosition()`; `RobotOpMode` sets the pose and runs the start check. A TeleOp with
no handoff declares its alliance's start too (`StartPositions.forAlliance`), so a test drive starts
from a known pose with the right field-centric forward; after an Auto it keeps the Auto's pose. The drive team
places by the marks (wall and seam, the manual's tile names), and the marks win: the start check
never tells anyone to move the robot by the camera. It warns only past `startMarginIn` (a real
mistake), and logs the camera's gap at PLAY as a calibration sample.

**Autos from the Visualizer.** A generated Auto lives in `opmodes/auto/generated/`, and its `.pp`
source is committed in `TeamCode/autos/` in the same PR, under the name its `SOURCE` constant
gives. The `.pp` is the source: change the Auto in the Visualizer and export again, never edit the
generated Java by hand. `AutoSourcesTest` fails a generated class whose `.pp` is missing.
→ `TeamCode/autos/README.md`

**Cameras.** The Limelight is for AprilTags only (pitched up at the HIVE). Game pieces come from
the webcam (`vision/PieceVisionSubsystem`, the SDK's colour-blob processor), enabled only while
intaking because it costs Control Hub CPU. Never switch the Limelight to a colour pipeline — it
starves the fusion of tag fixes.
There is one vision OpMode, **Vision: HIVE & Pose**: a camera check or experiment goes into it,
never into a new OpMode. It needs no Panels setup to be useful (known start, its own buttons, its
numbers in the match log); keep it that way.

**Field frame and units.** Pedro's frame everywhere: origin at a field corner, inches, radians CCW.
Degrees only on screens. It is the only frame a person reads or types — on the DS, in Panels, in
the Visualizer, in docs, issues and conversation. A library that works in another frame (Panels'
canvas, Limelight camera space) is converted inside the one class that talks to it, and nothing
downstream sees the other frame; never add a frame or unit toggle anywhere. The field is
`FieldFrame.FIELD_SIZE_INCHES` (141.5, wall face to wall face — not 144) and is rotationally
symmetric, not a mirror: blue's side is red's turned half a turn about the centre
(`FIELD_CENTRE_INCHES` on both axes; manual Figure 9-2); `FieldFrameTest` fails any other file that states the size (generated
Autos, written by the Visualizer on the same field, are exempt). Field facts
that depend on the frame live in `util/FieldFrame`.

**Driver Station** — the human view: Match / Controls / Robot pages, HTML, cycled with gamepad 1
Back/Share (see `controls/Display`). Before PLAY, `RobotOpMode.setup` settles the alliance — vision
proposes, X/B on either gamepad overrides, Auto's handoff is inherited. An Autonomous never guesses
one (with none it does not move); any other OpMode defaults to red, shown as "RED (default)".
Read it as `setup.alliance()`; never add another way to choose it. One-shot buttons use `onPress(robot.x::method)`, not a command
that requires the subsystem — that would interrupt its `periodic()`.

**Panels** — the only dashboard, `http://192.168.43.1:8001`. Numbers and graphs.
- Telemetry: `PanelsTelemetry.INSTANCE.getTelemetry()`, `addData` per key, then **one** update
  per loop. A standalone OpMode uses `update(telemetry)`, which mirrors to the Driver Station; one
  on `RobotOpMode` writes the Driver Station through `Display` and calls `update()` with no
  argument, so Panels' lines do not land on the Match page.
- Wrap publishing in a `try`/`catch` that swallows: telemetry never takes a mechanism down.
- Keys are flat, prefixed strings (`left_rpm`), not `a/b/c` paths.
- Tunables: `@Configurable` static fields. Guard them against bad input (see `BasicDriveTeleOp`'s
  use of `TeleOpConfig`, the TeleOp's driver-facing knobs).
- Field drawing: `util/FieldView`, not Pedro's `Drawing` helper.
  → README § "The Panels stack"

**Sloth** (hot reload). Deploy with the **Sloth Load** run config; do a full **TeamCode** install
after changing any `.gradle` file, a dependency, `FtcRobotController/` or anything under `res/`
(a config added or removed), or the robot silently keeps the old code. Bundled-config discovery
depends on Sloth, not the SDK, and survives a Sloth Load only because `TeamCode/build.gradle` adds
TeamCode's `R$xml` to the Sloth dex (`dexSlothR`) — never remove it. It reaches into Load's
`dexSloth` task and AGP's R jar, so a Load or AGP bump re-checks it; after any such bump, configs
can vanish from the DS list with no compile error.
→ README § "Risk: bundled configs depend on Sloth"

## Loop time

Every millisecond in the loop is a millisecond Pedro isn't correcting. The rules:

- **Nothing blocks.** No `sleep()`, no waiting loops, in `update()` or `onLoop()`. Slow work is
  a state machine advanced one step per call.
- **Read hardware once per loop.** `RobotOpMode` sets bulk caching to `MANUAL` and clears it at the
  top of every loop; read each sensor once and pass the value around.
- **Nothing is looked up or allocated per loop** that could be done once in init: hardware
  lookups, lists, formatters, commands.
- **Don't wrap motors in CachingHardware.** A stale cache drops the repeat `setPower` that would
  recover a motor a hub has zeroed behind its back.
- **Telemetry is lean**: one Panels `update` per loop, and `FieldView.shouldDraw()` for drawing.
- **Measure, don't argue.** `RobotOpMode.loopTimer` times every loop; run the `LoopTimeBaseline`
  OpMode before and after adding a subsystem and put both numbers in the PR.

## Hard constraints

- **Dependency versions are a locked set:** Sloth `0.3.2`, Pedro 3.0.2 + AutoTune, Panels
  `com.bylazar:fullpanels` 1.0.19 (upstream, Sloth-native; not the frozen `com.bylazar.sloth`
  fork), Ivy 1.1.x, `androidx.appcompat {strictly 1.2.0}`. Never bump one alone; close
  Dependabot PRs that raise `appcompat`. A Load or AGP bump also re-checks `dexSlothR` in
  `TeamCode/build.gradle` (see Sloth above).
  → README § "Why the versions are locked together"
- **Never add:** FTC Dashboard or AdvantageScope Lite (their NanoHTTPD servers collide with
  Panels and the RC crash-loops on `BindException` — it compiles fine, only a robot catches it),
  NextFTC (v2 included: it is built on Ivy, so the objection is its hardware layer, its OpMode
  loop and its telemetry, not its scheduler), Road Runner, Marrow, `com.pedropathing:telemetry`,
  `maven.pedropathing.com`.
  → README § "Deliberately excluded"
- **Never patch `FtcRobotController/`.** It is stock v12.0 and gets replaced wholesale.
- **SDK 12 split `AprilTagDetection`** into `AprilTagSingleDetection` /
  `AprilTagClusterDetection`; old `.id`/`.metadata`/`.center` code does not compile.
- **BIOBUZZ AprilTags move** (they sit on the tipping HIVE). Never use Limelight MegaTag
  (`getBotpose*`) or any fixed field map: one position per tag is wrong whenever its CELL has
  tipped. Tag fixes enter the pose only through `localization/`, using the CELL's settled UP/DOWN
  state and `HiveFieldPoints`.

## Commands, and working without a robot

```
./gradlew assembleDebug            # compile
./gradlew :TeamCode:lintDebug      # lint (TeamCode only, deliberately)
./gradlew testDebugUnitTest        # unit tests
```

CI runs all three on every push; they are required checks on `master`. `SDK location not found`
means `local.properties` lacks `sdk.dir` — an environment problem, not a broken build. Gradle needs
network; if the sandbox blocks it, say so.

**No robot is attached to a Claude session.** Anything needing hardware — tuning values, motor
directions, port checks — stops and produces a checklist for the next meeting instead of a guess.
Remote containers have no `gh`; use the GitHub MCP tools.

## Process

`.github/CONTRIBUTING.md` is the full version. What a session must not miss:

- **`master` changes only through a PR with CI green.** Never commit to `master`.
- **Student work** goes through the board: an issue first, the PR says `Closes #N`, the branch is
  `<prefix>/<issue>-<slug>` (`feat/ fix/ tune/ chore/ docs/ spike/`), and the issue carries one
  `team:*`, `needs:robot` if it does, and one of `good-first-task` / `student-ready` /
  `mentor-only`. No priority labels: board order is priority.
- **Mentor work** (a mentor, or a session a mentor started) needs none of that: any branch name,
  including the session's own `claude/*` one; an issue only when it helps; labels optional. The PR
  description says what changed and why, and CI must be green.
- **Students' issues stay theirs to build.** A session asked by a mentor to implement a student's
  issue says so once, then does what the mentor decides. Asked by a student, it coaches
  (`first-pr-coach`) rather than writes it for them.
- Rename a branch that is already on GitHub with GitHub's button, never by delete-and-repush: that
  strands its open PR.
- A red CI is investigated, not re-run.
