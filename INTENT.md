# Foundation intent

What the thin layer under all robot code is for, what it must do, and what it deliberately does
not do. The code on this branch is written to this file; when they disagree, one of them changes
on purpose. At merge, the rules here are distilled into `CLAUDE.md`.

## Purpose

Give students a structure to build mechanisms on that is **thin** — little enough to read in one
sitting — and **always fallback-able**: the robot inits and drives no matter what a mechanism does.

## Principles

1. **Thin beats clever.** Add machinery only when a real failure at a meeting demands it.
2. **Fallback is removing a line.** A mechanism is one entry in `Robot`'s subsystem list. If it
   breaks, comment it out and Sloth Load; the robot runs without it. Git holds the last good one.
3. **The drivetrain is never optional.** It is in the layer like everything else, and it drives
   on an untuned robot and without the Pinpoint.
4. **One way to do each thing.** One way to add a mechanism, one loop, one telemetry path, one
   place for each name and number.
5. **Only designed hardware gets code.** No subsystem for a mechanism that does not exist yet.

## The layer

| Piece | Responsibility |
|---|---|
| `Robot` | Builds every subsystem and holds them in one list — the only registration. |
| `Subsystem` | `initialize()` / `update()` / `stop()`. `update()` is one non-blocking step. |
| `RobotOpMode` | Owns the loop: bulk caching, loop timing, the Ivy scheduler, teardown. OpModes fill in `onInit` / `onLoop`. |
| Subsystem accessors | How the rest of the code learns a subsystem's state (`robot.drive.heading()`, `robot.vision.state(cell)`). Read-only; no global or static robot state. |
| `controls/Bindings` | One gamepad's bindings, each with a label: `driver.when("Y", "Reset heading", () -> gamepad1.y).onPress(...)`. Polled after PLAY only. |
| `controls/Display` | The Driver Station: Match (the OpMode's lines), Controls (generated from the labels), Robot (each subsystem's `describe()`). Back/Share on gamepad 1 cycles them. |
| `DriveSubsystem` | Mecanum drive from the OpMode's `drive(...)` command; field-centric with the Pinpoint, robot-centric without. |
| `hardware/` | Names (`DeviceNames`), ports (`robot_*.xml`), identity (`RobotIdentity`). |
| `pedro/Constants` | Pedro values per `RobotIdentity`; untuned values never stop the robot driving. |

## The loop contract

Every robot OpMode gets this from `RobotOpMode`, and never does it itself:

- Lynx bulk caching in `MANUAL`, set before `Robot` is built, cleared once at the top of every loop.
- Then the gamepad bindings fire (after PLAY only — the robot may not move in INIT).
- Then `onLoop()`: read the sticks, tell subsystems what you want, write the Match page.
- Then `Scheduler.execute()`: every subsystem updates through `periodic()`, so an input reaches
  the hardware in the same loop. The scheduler is unconditional — there is no opt-out.
- Then the Driver Station page: Controls and Robot are drawn by `RobotOpMode`, replacing the
  OpMode's lines; Match shows them.
- A `LoopTimer` lapped once per loop, available to the OpMode to publish.
- Teardown that is safe even when init failed partway.

## Decided

| Decision | Why | Rejected |
|---|---|---|
| Scheduler always on, no opt-out | TeleOp will have interruptible macros; one execution model for TeleOp and Auto; its cost is microseconds | `useScheduler()` opt-out — two ways to run, and an off path that could silently stop updating subsystems |
| `onLoop()` before `Scheduler.execute()` | Inputs reach the motors in the same loop | After — a loop of added latency |
| The loop contract is tested | `LoopContractTest` fails if a `RobotOpMode` subclass or a subsystem clears the cache or drives the scheduler itself | Trusting the javadoc |
| Bulk caching owned by `RobotOpMode` | Hardware read once per loop without every OpMode remembering | Per-OpMode setup (how `BasicDriveTeleOp` used to do it) |
| Per-robot Pedro values keyed by `RobotIdentity` | Two robots, identical hardware, different measured numbers | One shared set — tuning one robot overwrites the other |
| Bindings carry their own labels; Controls page generated from them | The help the drivers read is the code that runs | DECODE's hand-written `controlsSummary()`, kept in step by nobody |
| Three DS pages, HTML, one class | Glanceable match view, controls on demand, subsystem health | DECODE's telemetry service + 10 data classes + 3 formatters (~2,900 lines) |
| Page button is gamepad 1 Back/Share | Leaves the d-pad free for game controls | D-pad (DECODE) |
| One-shot button actions call a method, not a command | A command that requires a subsystem would interrupt that subsystem's own `periodic()` | Everything as a command |
| Fallback by removing a list entry | Zero code, instant with Sloth | Health states, per-subsystem try/catch, Panels kill switches — revisit only after a real failure |

## Out of scope for this branch

- Mechanism subsystems (launcher, turret, intake) until their hardware is designed.
- Autonomous routines and paths.
- A shared telemetry helper beyond what `RobotOpMode` needs.
- Test rigs (`shooter/`, `launcher2/`) — standalone by design.

## Done when

- `BasicDriveTeleOp` extends `RobotOpMode` and its drive lives in `DriveSubsystem`, behaving as before.
- No OpMode sets a bulk caching mode or calls `Scheduler` itself.
- Pedro values resolve per robot, with a test that fails if a drivetrain robot has none.
- All three CI checks green; `CLAUDE.md` describes exactly this code.
- A meeting checklist exists for what only a robot can confirm.

## Status

| Done-when item | State |
|---|---|
| `BasicDriveTeleOp` on `RobotOpMode`, drive in `DriveSubsystem` | Done — behaviour unchanged, not yet run on a robot |
| No OpMode sets caching or calls `Scheduler` itself | Done — enforced by `LoopContractTest` |
| Pedro values per robot | Not started — running as a separate piece of work |
| CI green, `CLAUDE.md` matches the code | Green locally; `CLAUDE.md` updated with this branch |
| Bindings and DS pages | Done — `BindingsTest` covers edges; rendering unverified until a DS shows it |
| Meeting checklist | Below |

## Meeting checklist

1. Deploy with a full **TeamCode** install (new classes), Activate the robot's config.
2. **Basic Drive**: drives as before — speeds, turbo, slow, Y resets heading, B toggles mode.
3. Unplug the Pinpoint, re-init: robot drives robot-centric and the DS says NO PINPOINT.
4. Note the `Loop` line; compare with **LoopTimeBaseline** on the same robot.
5. Back/Share cycles Match → Controls → Robot in INIT and after PLAY. Controls lists every driver
   input. HTML renders: bold headers, coloured dots, one item per line. No button moves the robot
   during INIT.
6. Run each Vision calibration OpMode: camera state still updates in INIT and after PLAY. They now
   build the drivetrain too, so they need the drive motors present.

## Open questions

- How commands claim subsystems. `periodic()` holds each subsystem, so a macro that also requires
  it interrupts the subsystem's update. Needs a rule (priorities, or `periodic()` not requiring)
  before the first BIOBUZZ macro.
- Does `LoopTimeBaseline` move onto `RobotOpMode`, or stay a standalone instrument? (Leaning:
  stay — it measures the loop, so it should not be inside the thing it measures.)
