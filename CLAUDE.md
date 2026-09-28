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
| `subsystems/Subsystem` | `initialize()` / `update()` / `stop()`. `update()` is one non-blocking step; `stop()` is final teardown only — a mechanism that pauses needs its own name (`idle()`, `spinDown()`). |
| `opmodes/RobotOpMode` | Owns the loop. OpModes that run the robot extend it and fill in `onInit`/`onLoop`; `LoopContractTest` fails any that set bulk caching or call `Scheduler`/`robot.stop()` themselves. Standalone diagnostics (`ValidateHardware`, `LoopTimeBaseline`) and rigs are the exception. |
| `subsystems/DriveSubsystem` | The drivetrain, and the worked example: the OpMode says what it wants (`drive(...)`), `update()` does it. Drives robot-centric if the Pinpoint is missing. |
| `controls/` | `Bindings`: bind gamepads in `onInit()` via `driver`/`operator`, every binding labelled (`when("Y", "Reset heading", ...)`); the DS Controls page is generated from the labels, so never document controls anywhere else. `Display`: the DS pages; write the Match page in `onLoop()`, give a subsystem a Robot-page block by overriding `describe()`. |
| `localization/` | CELL sighting → position fix (`CellFix`), field points, start positions and the start check. The filter is Pedro's. See "Localization" below. |
| `hardware/` | Device names, robot identity, active config — see below. |
| `pedro/` | `Constants.java`, `Tuning.java`, `RobotConstants.java` and `robots/` (tuned values, one file per robot) are ours. `pedro/procedures/**` is upstream: never edit it. |
| `util/` | Shared helpers: `LoopTimer`, `FieldView`, `AccelLimiter`, `Alliance`, `WelfordVariance`. Reuse before writing another. |
| `shooter/`, `launcher2/` | **Test rigs**, deliberately standalone: no `Robot`, no `Subsystem`. Don't copy their pattern into robot code. |

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
- XML: no `name` attribute on `<Robot>` (the filename is the identity). For I2C, `bus` is what
  the SDK reads; keep `port` equal to it.
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
the single exception is `controls/Handoff`, which carries alliance and pose from Autonomous to
TeleOp and is written and read only by `RobotOpMode`.

**Localization.** Pedro's `FusionLocalizer` fuses the Pinpoint with AprilTag fixes; we add only
`CellFix` (sighting → position) and assume Pedro's filter works — no extra error-tracking layer
until a real problem asks for one. Read the pose from `robot.drive`, never from the Pinpoint or
the Limelight directly. Anything that drives or aims from field coordinates checks
`robot.drive.poseReferenced()` first and degrades without it; driving itself never needs it.
`HiveFieldPoints`, `StartPositions`, `FieldFrame` and `LocalizationTuning` hold measured field
facts — NaN or empty until measured, never guessed. An Autonomous declares its start by
overriding `startPosition()`; `RobotOpMode` sets the pose and runs the start check.

**Cameras.** The Limelight is for AprilTags only (pitched up at the HIVE). Game pieces come from
the webcam (`vision/PieceVisionSubsystem`, the SDK's colour-blob processor), enabled only while
intaking because it costs Control Hub CPU. Never switch the Limelight to a colour pipeline — it
starves the fusion of tag fixes.

**Field frame and units.** Pedro's frame everywhere: origin at a field corner, inches, radians CCW.
Degrees only on screens. It is the only frame a person reads or types — on the DS, in Panels, in
the Visualizer, in docs, issues and conversation. A library that works in another frame (Panels'
canvas, Limelight camera space) is converted inside the one class that talks to it, and nothing
downstream sees the other frame; never add a frame or unit toggle anywhere. The field is
`FieldFrame.FIELD_SIZE_INCHES` (141.5, wall face to wall face — not 144) and mirrors across
`FIELD_CENTRE_INCHES`; `FieldFrameTest` fails any other file that states the size. Field facts
that depend on the frame live in `util/FieldFrame`.

**Driver Station** — the human view: Match / Controls / Robot pages, HTML, cycled with gamepad 1
Back/Share (see `controls/Display`). Before PLAY, `RobotOpMode.setup` settles the alliance — vision
proposes, X/B on either gamepad overrides, Auto's handoff is inherited — and never guesses one.
Read it as `setup.alliance()`; never add another way to choose it. One-shot buttons use `onPress(robot.x::method)`, not a command
that requires the subsystem — that would interrupt its `periodic()`.

**Panels** — the only dashboard, `http://192.168.43.1:8001`. Numbers and graphs.
- Telemetry: `PanelsTelemetry.INSTANCE.getTelemetry()`, `addData` per key, then **one**
  `update(telemetry)` per loop, which mirrors to the Driver Station. No parallel DS-only calls.
- Wrap publishing in a `try`/`catch` that swallows: telemetry never takes a mechanism down.
- Keys are flat, prefixed strings (`left_rpm`), not `a/b/c` paths.
- Tunables: `@Configurable` static fields. Guard them against bad input (see `BasicDriveTeleOp`).
- Field drawing: `util/FieldView`, not Pedro's `Drawing` helper.
  → README § "The Panels stack"

**Sloth** (hot reload). Deploy with the **Sloth Load** run config; do a full **TeamCode** install
after changing any `.gradle` file, a dependency, or `FtcRobotController/`, or the robot silently
keeps the old code. Bundled-config discovery depends on Sloth, not the SDK: after a Sloth bump,
configs can vanish from the DS list with no compile error.
→ README § "Risk: bundled configs depend on Sloth"

## Loop time

Every millisecond in the loop is a millisecond Pedro isn't correcting. The rules:

- **Nothing blocks.** No `sleep()`, no waiting loops, in `update()` or `onLoop()`. Slow work is
  a state machine advanced one step per call.
- **Read hardware once per loop.** `RobotOpMode` sets bulk caching to `MANUAL` and clears it at the
  top of every loop; read each sensor once and pass the value around.
- **Nothing is looked up or allocated per loop** that could be done once in init: hardware
  lookups, lists, formatters, commands.
- **Don't wrap motors in CachingHardware.** A stale cache suppresses recovery writes (see
  `shooter/FlywheelBank`).
- **Telemetry is lean**: one Panels `update` per loop, and `FieldView.shouldDraw()` for drawing.
- **Measure, don't argue.** `RobotOpMode.loopTimer` times every loop; run the `LoopTimeBaseline`
  OpMode before and after adding a subsystem and put both numbers in the PR.

## Hard constraints

- **Dependency versions are a locked set:** Sloth `0.3.2`, Pedro 3.0.1 + AutoTune, the
  `0.3.2+…` Sloth build of Panels, Ivy 1.1.x, `androidx.appcompat {strictly 1.2.0}`. Never bump
  one alone; close Dependabot PRs that raise `appcompat`.
  → README § "Why the versions are locked together"
- **Never add:** FTC Dashboard or AdvantageScope Lite (their NanoHTTPD servers collide with
  Panels and the RC crash-loops on `BindException` — it compiles fine, only a robot catches it),
  NextFTC, Road Runner, Marrow, `com.pedropathing:telemetry`, `maven.pedropathing.com`.
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

- An issue exists before a branch; every PR says `Closes #N`. Never commit to `master`.
- Branch prefixes `feat/ fix/ tune/ chore/ docs/ spike/`; rename a `claude/*` branch with
  GitHub's rename button before opening the PR, never by delete-and-repush.
- Labels: exactly one `team:*`, `needs:robot` if it does, and one of `good-first-task` /
  `student-ready` / `mentor-only`. No priority labels — board order is priority.
- **Mentor/student split.** Substrate (versions, CI, hardware abstraction, rigs, tuning harnesses)
  is mentor-only; robot behaviour belongs to students. Asked to implement something assigned to a
  student, decline and improve the issue instead.
- A red CI is investigated, not re-run.
