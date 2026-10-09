# From the simulator to the robot: how to structure the Auto code

**Status: review and proposal, 9 Oct 2026.** Nothing here is built. It compares what the routes and Smart Auto
(`doc/smart-auto.md`) assume with the Auto code on `master` today, and proposes a structure so a route that scores in
the simulator does the same on the robot, and so a robot match log can be read with the same tools as a simulated one.

## What we have, and what is already right

**One Auto runtime, shared by the robot and the simulator.** A route is drawn in the Visualizer, saved as a `.pp`, and
exported to Java that calls `autokit/AutoKit`: commands, paths, `firstOf` (wait for the first of some triggers or a
time limit, then run that row's cards, optionally while a command runs) and the endgame guard. The simulator
(`logging/AutoSim`) runs **the same generated class through the same `AutoKit`**. It only swaps in its own
`AutoRegistry`, with simulated commands and triggers. That shared runtime is the most valuable piece we have: the card
logic, timeouts, branch choices and park guard are identical in both. Keep it that way.

**Names are the contract.** A generated Auto lists the commands and triggers it uses (`COMMANDS`, `TRIGGERS`).
`BuiltAuto` checks them against `AutoRegistration` at INIT and refuses to run half an Auto. `auto-registry.json` gives
the editor the same list, and `AutoRegistrationTest` keeps the two in step.

**The rest is in place:** the half-turn for the other alliance, the start pose, the match log's AdvantageScope keys
(the simulator writes the same `AdvantageScopeKeys`), `.pp` files committed beside their Java (`AutoSourcesTest`),
and the `Handoff` from Auto to TeleOp.

## The gaps

### 1. The robot offers two names; the routes use thirteen

| Name | Kind | Used by | In the simulator | On the robot today |
|---|---|---|---|---|
| `Tip` | trigger | all | the rocker started to move | `HiveTracker` (camera) |
| `CameraBlind` | trigger | backups | camera-down mode | yes |
| `LeftCellUp`, `RightCellUp` | trigger | sister, sister5, L-Quals | the rocker's settled state | **missing** (the tracker has the state) |
| `IntakeFull` | trigger | nearly all | the lane holds 4 POLLEN / 3 NECTAR | **missing**: no intake subsystem |
| `Empty` | trigger | all | nothing held | **missing** |
| `LauncherReady` | trigger | some | flywheel at speed | **missing** |
| `SpinUp`, `SpinDown` | command | all | flywheel on/off | **missing** |
| `LaunchAll`, `LaunchOne` | command | all | fire every 0.2 s while aimed within 2° and at speed | **missing** |
| `StreamOn`, `StreamOff` | command | sister, sister5 | fire as pieces arrive | **missing** |
| `IntakeOn`, `IntakeOff` | command | some | intake on/off | **missing** |
| `CollectSeen` | command | sister5 | drive to pieces the webcam sees | **missing**: a behaviour over `PieceVisionSubsystem` and the drive |
| `HeldWorth4` | trigger | parked (sister5i-worth) | held pieces worth 4 POLLEN | needs a sensor; parked by the mentor |
| `HookDown`, `HookUp`, `SetDown`, `Outtake` | command | old studies | — | not planned |

The simulator's registry (`AutoSim`, around line 1485) can define any name, and the experiments use names the robot
will never have. Nothing checks that. **A route must not be promoted until every name it uses has a robot meaning.**

### 2. Each name means something precise in the simulator, and the robot must match it

The routes' timing rests on those meanings. Three that matter most:

- **`IntakeFull`** is the lane's geometry (Transfer v3: 4 POLLEN, or 3 NECTAR, or 3 NECTAR and a POLLEN). On the robot
  that needs a sensor: a beam break or distance sensor at the backstop, or a count of pieces in minus shots out. Sister
  five's whole decision ("Holding 4? Then 5 TIPs") is this trigger.
- **`LaunchAll`** fires one piece every 0.2 s, only while the flywheel is at speed and the turret is within 2° of the
  CELL, and ends when `Empty`. The turret's aim policy (below) is part of this meaning.
- **`Tip` / `LeftCellUp` / `RightCellUp`** come from ground truth in the simulator and from the Limelight on the
  robot, with latency, missed frames and a blind camera. The simulator is optimistic here.

So each name needs a written contract: what it means, in what units, how long it typically takes, how it fails.
The robot implementation and the simulator implementation then both answer to it. The table above is the start of
that contract.

### 3. Smart Auto needs to choose between routes; `BuiltAuto` runs one

`BuiltAuto` takes one generated Auto in its constructor. Smart Auto (`doc/smart-auto.md`) decides at INIT among SMART
routes (L-Quals or R-Quals, by start and partner), BACKUP routes (Backup-L, Backup-R) and PARK ONLY, then runs the
chosen one. Today that would mean copying `BuiltAuto`'s INIT and PLAY logic into a second OpMode.

### 4. Decisions happen at two levels, and that split should be explicit

- **Before PLAY, in the OpMode:** which plan, from health, overrides, partner and start (`decide(health, overrides)`,
  pure and unit-tested).
- **During the match, in the route:** a `firstOf` on a trigger the robot can sense ("Holding 4? Then 5 TIPs",
  "TIP 3 done?", "No TIP 2 by 6.5 s: rescue"). These belong in the `.pp`, where the simulator measures them.

Rule from `doc/smart-auto.md` that applies to every route: **no wait without a time limit that leads somewhere
sensible**. A lost camera then costs a branch, not the match.

### 5. Behaviours that are policies, not route steps

Some things the robot should simply always do, whatever the route says:

- **Turret aim** (mentor, 9 Oct 2026): while the flywheels spin, track the CELL at the robot's own end, with
  hysteresis (switch to the left CELL above y 90, back to the right below y 51). Swinging only when a TIP raises the
  other CELL cost Sister five about 2 points a match in the simulator (240°/s servo).
- **Piece accounting:** pieces in (lane sensor) minus shots out (flywheel speed dip), reset when known empty.
- **The endgame guard:** already a policy in `AutoKit`.

If these live in routes, every route repeats them and the simulator has to guess them. They belong in subsystems,
as small pure classes the simulator can call too (see the proposal).

### 6. A robot log can't be read like a simulated one

The analysis tools (`tools/auto-routes/tiptimes.py`, `catchcount.py`, the simulator chat's seed rows) read the
`/Events` lines the simulator writes, `auto: wait …`, `auto: path …`, `auto: … : IntakeFull after 1.20 s`, and keys
such as held count, intake on and turret yaw. On the robot, **`BuiltAuto` sends the same `AutoKit` trace only to the
Driver Station** (`trace(this::remember)`): it never reaches the match log. And the robot logs none of the mechanism
state, because the mechanisms don't exist yet.

## Proposal

### A. One route runner, used by `BuiltAuto` and Smart Auto

Pull `BuiltAuto`'s start logic (alliance check, rotation, start pose, `AutoKit`, schedule) into one small class, say
`opmodes/auto/RouteRunner`, that takes a route descriptor:

```java
/** What a generated Auto offers, as one value: Smart Auto can hold a table of them. */
record AutoRoute(String name, String source, String[] commands, String[] triggers, String drawnFor,
                 Function<Boolean, Pose> startPose, BiFunction<AutoKit, Boolean, Command> build) {}
```

Generated classes would expose `public static final AutoRoute ROUTE = …` (an exporter change in the Visualizer, or a
one-line hand-written holder per route until then). `BuiltAuto` becomes a thin wrapper around one `AutoRoute`. Smart
Auto holds the table (`R-Quals`, `L-Quals`, `Backup-L`, `Backup-R`, `Park-L`, `Park-R`), checks the union of their
names at INIT, and at PLAY runs `decide()`'s choice. The emergency parks stay separate OpModes that share none of
this.

### B. One registry contract, checked both ways

- Keep `AutoRegistration` the robot's list, and add each name only when the mechanism exists.
- Write the contract for each name: meaning, typical time, failure mode. It could live as a table in
  `opmodes/auto/README` or as Javadoc on the registration.
- **A test that every name in a promoted route** (`TeamCode/autos/*.pp`, `opmodes/auto/generated/`) **is in
  `AutoRegistration`.** Experiments under `src/test/.../generated` may use simulator-only names; promoted routes may
  not.
- **A test that the simulator registers every robot name.** Then a route that runs on the robot also runs in the
  simulator.

### C. Shared pure logic, so the simulator runs the robot's code

Wherever a behaviour is a decision rather than physics, write it once in `main` as a pure class and have both the
robot subsystem and `AutoSim` call it:

| Logic | Pure class (main) | Robot calls it from | Simulator calls it from |
|---|---|---|---|
| Turret target | `TurretAim.target(pose, current)` with the y 51/90 hysteresis | turret subsystem | `AutoSim`'s turret |
| Fire gate | `FireGate.ready(flywheelOk, aimErrorDeg, sinceLastShot)` | launcher subsystem | `AutoSim.launcher` |
| Piece count | `PieceCount` (in, out, reset) | intake/launcher | `AutoSim`, with simulated events |
| Plan | `SmartPlan.decide(health, overrides)` | Smart Auto | a study of plans by health |
| HIVE state | `HiveTracker` (exists) | vision | fed simulated tag sightings, with latency |

The last row is the honest one. Today the simulator's `Tip` reads the rocker directly. Feeding `HiveTracker` simulated
sightings, with the Limelight's latency and dropouts, would make every route's waits as late on the screen as they
will be on the field.

### D. Logs that read the same

- **Send the `AutoKit` trace to the match log** as `/Events` lines in the simulator's form (`auto: wait …`). That is
  one line in `BuiltAuto` / `RouteRunner`: `trace(line -> { remember(line); log.event("auto: " + line); })`.
- **Log the plan and its reasons at PLAY** (`plan: SMART, R-Quals; camera ON, partner Launch & Park, …`), as
  `doc/smart-auto.md` says.
- **Log mechanism state under the keys the simulator already writes**: held count, intake on, launcher spinning,
  turret yaw and aim error. One list of key names in `AdvantageScopeKeys`, used by both. Simulator-only ground truth
  (`/Sim/GamePieces/...`, the rocker's angle) stays under `/Sim/`.
- Then `tiptimes.py` and the simulator chat's seed rows work on a robot log unchanged. A match can be lined up against
  its simulated twin, card by card.

### E. A promotion path for routes

1. **Experiment:** `tools/auto-routes/*.py` writes the `.pp` to `src/test/resources/auto-builder/experiments/` and
   Java to `src/test/.../generated/`. Any simulator name is allowed. It is measured in the simulator.
2. **Candidate:** every name it uses is in `AutoRegistration`. The simulator chat's 60-run study is the bar.
3. **Promoted:** the `.pp` is copied to `TeamCode/autos/` and exported to `opmodes/auto/generated/` (main). It is
   added to Smart Auto's table or given its own `BuiltAuto` wrapper. It is tested on the robot.

Today every route is at step 1. The first to promote are the ones Smart Auto needs: L-Quals, R-Quals, Backup-L,
Backup-R, the park routes.

## What it takes, in order

Each step is useful on its own.

1. **Trace to the match log** (D, first bullet). One line, no hardware. Every robot test from then on is comparable.
2. **`LeftCellUp` / `RightCellUp` on the robot**: `HiveTracker` already has the state. Two lines in
   `AutoRegistration`.
3. **`AutoRoute` and `RouteRunner`** (A), with `BuiltAuto` rewritten on top. No behaviour change, so the existing
   test covers it.
4. **The registry tests** (B).
5. **The mechanisms, as they are built:** intake → `IntakeOn/Off`, `IntakeFull`, `Empty`; launcher → `SpinUp/Down`,
   `LauncherReady`, `LaunchAll/One`, `StreamOn/Off`; turret with `TurretAim`. Each lands with its contract line, its
   log keys and, where it is a decision, its pure class shared with the simulator (C).
6. **Smart Auto** on top: `decide()`, the INIT screen, the table of routes.
7. **`HiveTracker` in the simulator** (C, last row), when the camera timing matters more than the next route idea.

## Who owns what

Per CLAUDE.md's split: the runner, registry tests, log keys and shared pure classes are substrate (mentor). `decide()`,
Smart Auto's screen and the routes are students' work, with the simulator chat measuring them.

## Open questions

- **Exporter change or hand-written holders** for `AutoRoute`? The exporter is cleaner but touches the Visualizer.
- **How much of `CollectSeen` is worth building?** Sister five uses it for R's TIP 1 top-up. It is a behaviour (webcam
  → drive) with real failure modes: the simulator chat just fixed a case where it drove into the HIVE frame.
- **The lane sensor:** the transfer chat's design keeps "4 aboard" geometric and adds an exit sensor only for
  flow-through. `IntakeFull` needs one at the backstop either way.
