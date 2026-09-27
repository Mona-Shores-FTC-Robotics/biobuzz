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
| `DriveSubsystem` | Mecanum drive from gamepad input; field-centric with the Pinpoint, robot-centric without. |
| `hardware/` | Names (`DeviceNames`), ports (`robot_*.xml`), identity (`RobotIdentity`). |
| `pedro/Constants` | Pedro values per `RobotIdentity`; untuned values never stop the robot driving. |

## The loop contract

Every robot OpMode gets this from `RobotOpMode`, and never does it itself:

- Lynx bulk caching in `MANUAL`, cleared once at the top of every loop.
- The Ivy `Scheduler` always on: reset at init, executed every loop, reset at stop. Behaviour is
  commands; subsystems update through `periodic()`.
- A `LoopTimer` lapped once per loop, available to the OpMode to publish.
- Teardown that is safe even when init failed partway.

## Decided

| Decision | Why | Rejected |
|---|---|---|
| Scheduler always on | TeleOp will have interruptible macros; one execution model for TeleOp and Auto; its cost is microseconds | Default-off for TeleOp — two ways to run, and an off path that can silently stop updating subsystems |
| Bulk caching owned by `RobotOpMode` | Hardware read once per loop without every OpMode remembering | Per-OpMode setup (today's `BasicDriveTeleOp`) |
| Per-robot Pedro values keyed by `RobotIdentity` | Two robots, identical hardware, different measured numbers | One shared set — tuning one robot overwrites the other |
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

## Open questions

- Should `useScheduler()` exist at all, or is the scheduler unconditional?
- Does `LoopTimeBaseline` move onto `RobotOpMode`, or stay a standalone instrument?
