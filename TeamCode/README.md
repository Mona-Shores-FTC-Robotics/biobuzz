# TeamCode — external tooling

This module builds on the stock FTC SDK (`12.0.0`) with a small set of external
libraries. This doc records what's included, what was deliberately left out,
and why — so the reasoning survives past whoever added it.

> **History note.** An earlier version of this file documented a Pedro Pathing
> `2.1.2` stack and presented it as a considered choice. It wasn't — it was
> copied forward from the previous season's project before Pedro 3 shipped, and
> then never revisited. Pedro 3 is a re-architecture, not a version bump, so
> treat anything you remember from that doc as stale rather than as a decision
> someone made.

## Included

| Library | Artifact | Purpose |
|---|---|---|
| Sloth | `dev.frozenmilk.sinister:Sloth:0.3.2` (+ `dev.frozenmilk:Load:0.3.2` Gradle plugin) | Hot code reload — pushes only TeamCode to the robot, turning a ~40s reinstall into under a second (see [Deploying to the robot](#deploying-to-the-robot)). Also gives fast dex scanning at OpMode discovery, roughly 50% faster init than the SDK's reflective scan, and is the discovery mechanism AutoTune is built on, so it is no longer optional. |
| Pedro Pathing (core) | `com.pedropathing:core:3.0.1` | Pathing, geometry and math. Pure Java — no Android or FTC SDK types. |
| Pedro Pathing (revhub) | `com.pedropathing:revhub:3.0.1` | The REV hardware layer: mecanum/swerve drivetrains, Pinpoint/OTOS/OctoQuad/dead-wheel localizers, hub IMU. Brings `core` transitively; `core` is still declared explicitly so its version is pinned in one place. |
| Pedro AutoTune | `com.pedropathing:tuning:1.0.1` | Robot-hosted tuning webpage. Backs the procedures in `org.firstinspires.ftc.teamcode.pedro`. |
| Ivy | `com.pedropathing.ivy:pedro:1.1.1` | Command-based control flow (scheduler, `Command`/`CommandBuilder`, subsystem requirements/priority). Pedro Pathing's own command framework — used in place of NextFTC. |
| CachingHardware | `dev.frozenmilk.dairy:CachingHardware:1.0.0` | Wraps motor/servo writes to skip redundant `setPower`/`setPosition` calls when the new value is within tolerance of the cached one. |
| Panels | `com.bylazar.sloth:fullpanels:0.3.2+1.0.13` | Live dashboard: real-time tuning of constants, field/pose overlay, wireless Limelight pipeline tuning, telemetry graphs. Sloth-compatible build variant of `com.bylazar:fullpanels`. |
| FTC Dashboard | `com.acmerobotics.slothboard:dashboard:0.3.2+0.6.0` | Passive telemetry/field monitoring, run alongside Panels. Sloth-compatible build variant of `com.acmerobotics.dashboard:dashboard`. Kept in case we resume AdvantageScope integration (it piggybacks on FTC Dashboard's packet stream). |

Limelight3A support (`com.qualcomm.hardware.limelightvision`) needs no separate
dependency — it ships as part of the SDK's `Hardware` artifact in
`build.dependencies.gradle`.

### No `maven.pedropathing.com`

Pedro 3.x publishes `core`, `revhub` and `tuning` to **Maven Central**, as does
Ivy. The `https://maven.pedropathing.com` repository entry that 2.x required is
gone from `TeamCode/build.gradle`; don't re-add it.

### Why the versions are locked together

Three constraints pin this stack. Bump any one of them and you have to move the
others in the same commit:

1. **AutoTune requires Sloth `0.3.2`.** `com.pedropathing:tuning` discovers
   `@Tuner`-annotated fields via `TunerScanner`, which is a Sinister `Scanner`
   — i.e. AutoTune does not work without the Sloth runtime.
2. **The Sloth-variant builds of Panels and FTC Dashboard are version-locked to
   the Sloth runtime**, which is what the `0.3.2+…` prefix in their version
   strings means. They must track the Sloth version, and `dev.frozenmilk:Load`
   (the Gradle plugin) must match it too.
3. **Panels `0.3.2+1.0.5` declares `androidx.appcompat:appcompat` as
   `{strictly 1.2.0}`.** That is why `build.dependencies.gradle` pins appcompat
   at `1.2.0` and not something newer. A Dependabot PR raising appcompat will
   fail to resolve — close it rather than trying to force the version, since
   `strictly` is Panels telling us it means it. `1.2.0` is also what the stock
   FTC SDK and the Pedro Quickstart both declare.

   **The `1.7.1` this replaced was not fixing anything** — checked, because a
   silent downgrade of something load-bearing would be a nasty surprise later.
   The trail: DECODE `5b9446e` (2026-05-16) raised it `1.2.0` → `1.7.1` as one
   bullet in a six-line bulk dependency refresh, no reason given, "not yet
   verified on robot"; this repo's `f71afce` then copied it across "to match",
   and the README that commit added — which documented everything else, down to
   libraries deliberately *not* included — never mentions appcompat at all.
   Nothing in either stack ever asked for it: `CachingHardware:1.0.0` and
   Panels `0.2.4+1.0.5` both declare appcompat **`1.2.0`**, as ordinary
   (non-strict) `requires`, so Gradle silently resolved them up to `1.7.1`
   because higher wins. Building `master` with appcompat forced to `1.2.0`
   succeeds. So there is no latent bug waiting to resurface: the downgrade
   restores the version two of our own dependencies were asking for.

One non-obvious consequence: `Load:0.3.2` pulls `com.android.tools.build:gradle`
transitively, so the `buildscript` block in `TeamCode/build.gradle` needs
`google()` in its repositories. `Load:0.2.4` did not.

## Deploying to the robot

Two buttons, from the run-configuration dropdown next to the green ▶ in Android
Studio. You do not need a terminal for either.

| Dropdown entry | What it does | Speed |
|---|---|---|
| **TeamCode** | Full APK rebuild, uninstall, reinstall | ~40s |
| **Sloth Load** | Hot-reloads only your TeamCode classes | under 1s |

`Sloth Load` comes from `.run/Sloth Load.run.xml`, committed at the repo root, so
it appears for everyone who clones — no per-laptop setup. (It runs
`:TeamCode:deploySloth` underneath, if you ever want it from a terminal.)

### First-time setup, once per laptop

1. Open the **TeamCode** run configuration and set **Module** to the real
   TeamCode module. On a fresh clone it defaults to `<no module>`, and deploys
   fail in a way that does not obviously point at this.
2. Connect the robot — USB-C, or over Wi-Fi with
   `adb connect 192.168.43.1:5555` (Control Hub) or `192.168.49.1:5555`
   (phone RC).
3. Run **TeamCode** once. Sloth reloads code into an app that is already
   installed; it cannot do the first install itself.

After that, **Sloth Load** is the everyday button.

### When Sloth is not enough

Sloth only reloads classes under `org.firstinspires.ftc.teamcode`. Use the full
**TeamCode** install whenever you have changed:

- any `.gradle` file
- dependencies (added, removed, or version-bumped)
- anything in `FtcRobotController/`

Symptom of getting this wrong: the robot keeps running the *old* behaviour and
nothing looks broken. If a change seems to have had no effect, do a full install
before debugging anything else.

## The `pedro` package

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedro/` is copied verbatim
from the official Pedro Pathing Quickstart at tag `v3.0.1`. It is upstream code,
not ours — when Pedro releases a new Quickstart, re-copy it rather than patching
it in place.

Two of those files are deliberately stubs upstream, and are where our robot
configuration will eventually go:

- **`Constants.java`** — `create(HardwareMap)` currently returns `null`. It
  needs to return `new Follower(drivetrain, localizer, foresight)` once the
  drivetrain and localizer are configured.
- **`Tuning.java`** — empty. Tuners are registered by adding `@Tuner`-annotated
  fields holding the `Procedure` subclasses from `pedro/procedures/`.

Until those are filled in, **nothing in this package registers an OpMode** —
there is not a single `@TeleOp` or `@Autonomous` annotation in it, and it
compiles to an inert set of classes. That is expected — it is not a sign the
copy went wrong. (The one OpMode this module does register lives in
`shooter/`, below.)

## The `shooter` package

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/shooter/` is a flywheel
speed test rig: spin the three launcher wheels up, read the RPM, and tune the
gains. It registers one OpMode, **Flywheel Speed Test** (TeleOp, group
`Shooter`).

It exists to characterize shooting speeds on **last season's DECODE robot**
before BIOBUZZ hardware is ready, so it is deliberately standalone — no
drivetrain, no pathing, no intake, no feeder, and no dependency on
`pedro/Constants.java` being filled in. Point it at a robot with the three
launcher motors in its config and it runs.

### Zero recompiles

The whole point is that a student meeting never has to touch Android Studio.
Everything numeric is a live Panels field under `FlywheelBank → config`:

| Group | What's in it |
|---|---|
| `measurement` | `ticksPerRev`, `gearRatio` — how encoder ticks become RPM |
| `target` | `targetRpm`, `maxRpm` ceiling, and the two gamepad nudge steps |
| `readiness` | `rpmToleranceRpm`, `atSpeedHoldMs` — what counts as "at speed" |
| `voltageCompensation` | `enabled`, `nominalVoltage`, `minVoltage` |
| `left` / `center` / `right` | `motorName`, `enabled`, `reversed`, `rpmTrim`, `kS`, `kV`, `kP` |

`motorName` is live too: change it and the rig powers the old motor down and
binds the new one on the next loop. That is the escape hatch for a robot config
that spells a name differently — no recompile, no OpMode restart.

On the gamepad: **A** spins up, **B** stops, **dpad up/down** moves the target
by the fine step, **dpad left/right** by the coarse step. The Driver Station
shows the same lane table as Panels, so the rig is still usable with no laptop
connected.

### What was ported from DECODE, and what wasn't

Ported forward from `subsystems/LauncherSubsystem.java` and
`subsystems/launcher/config/LauncherFlywheelConfig.java`:

- the control law, unchanged —
  `power = (kS + kV * targetRpm) + kP * (targetRpm - measuredRpm)`, scaled by
  battery voltage and clipped to `[0, 1]`;
- the starting gains (kS `0.10`, kV `0.00017`, kP `0.001`), which were identical
  across both DECODE robots, so they are a real starting point and not a guess;
- voltage compensation, unchanged;
- the "don't wrap flywheels in `CachingHardware`" rule, and DECODE's reason for
  it: a wheel held at near-constant power gets every repeat `setPower` dropped by
  the cache, so a hub that zeroes the motor behind the cache's back stays dead.

Two things were deliberately **not** ported, both because a measurement rig has
different duties than a match robot:

- **`Math.abs()` on the measured velocity.** DECODE took the absolute value,
  which makes a backwards-spinning wheel look perfectly healthy. Here RPM keeps
  its sign, a backwards wheel never reaches speed, and the readout says which
  lane to flip.
- **`fallbackReadyMs`.** DECODE declared a lane ready on a timer when the encoder
  looked dead — the right call when a match is running. On a rig whose whole job
  is measurement, a lane that never reaches speed has to keep saying so.

`RobotProfile`'s two-robot machinery did not come across either. DECODE needed
`invalidate()` / `reloadProfileConfigs()` because its config defaults were
resolved per robot from the WiFi SSID, which is not known when Panels' boot-time
`@Configurable` scan runs. These defaults are plain constants, so none of that
applies.

### No command framework

The rig is a plain `OpMode` — no Ivy `Command`, no scheduler. DECODE was already
on Ivy (at `com.pedropathing:ivy:1.0.0`, the Pedro 2 coordinates; this repo has
`com.pedropathing.ivy:pedro:1.1.1`), but its flywheel control lived in the
subsystem, not in a command, so there was no command class to port in the first
place. A rig with one behaviour has nothing for a scheduler to arbitrate. When
the real BIOBUZZ launcher lands and shots have to be sequenced against an intake,
that is the point to introduce Ivy commands.

## Deliberately excluded

- **NextFTC** — the previous command framework. Migrated off it onto Ivy;
  reintroducing it would mean maintaining two overlapping command schedulers.
- **`com.pedropathing:telemetry`** — only existed as a crutch for one
  unmigrated file in the source project this was based on. Don't carry it
  forward; if a file needs it, port that file to the current telemetry API
  instead of re-adding the dependency. (It still exists on Maven Central at
  `1.0.0` and was never updated for Pedro 3.)
- **Road Runner / `maven.brott.dev`** — not in use; the maven repo isn't
  declared here to avoid an unused, unexplained entry.
- **AdvantageScope Lite** — not currently wired in. Add it back only if that
  debugging workflow is actually resumed.
- **Marrow** (`io.github.skeleton-army.marrow`) — a newer reactive-behavior
  library that layers on NextFTC/FTCLib/SolversLib. No evidence yet of use by
  competitive teams; worth revisiting, not a default include.
- **`exportPaths` Gradle task** — a nice-to-have that exports autonomous
  paths as `.pp` files for the Pedro Pathing visualizer. Not added here
  because it depends on a `util.ExportAutoPaths` utility class that doesn't
  exist in this project yet. Add both together when autonomous path code
  exists to export.

## SDK 12.0 compatibility notes

The SDK moved 11.2.1 → 12.0 alongside the Pedro 3 migration. Pedro's `revhub`
artifact is itself compiled (`compileOnly`) against `org.firstinspires.ftc:*`
**12.0.0**, so this keeps us on the SDK combination Pedro is built and tested
against.

- **The one breaking SDK change is AprilTag clusters.** SDK 12 splits
  `AprilTagDetection` into `AprilTagSingleDetection` and
  `AprilTagClusterDetection`; code that iterates detections and reads `.id`,
  `.metadata` or `.center` directly no longer compiles. Six stock samples in
  `FtcRobotController/` were affected and were replaced with the official v12.0
  versions. The `FtcRobotController` module is now byte-identical to the stock
  v12.0 module, so it can be refreshed wholesale on the next SDK bump — we carry
  no local patches in it. **This will bite us again** the first time we write
  vision code: see https://ftc-docs.firstinspires.org/apriltag-clusters.
- Also worth knowing: BIOBUZZ AprilTags move, so per the SDK release notes they
  are **not suitable for absolute field localization**. Don't plan a localizer
  around them.
- **Sloth** is a Gradle plugin as well as a runtime library, so it's the
  dependency most exposed to build-tooling bumps. The project is on Gradle 9.5.1
  + AGP 8.13.2, which requires Android Studio Narwhal 3 Feature Drop or later to
  sync at all.
- **Panels and FTC Dashboard each moved a minor version** (1.0.12 → 1.0.13 and
  0.5.1 → 0.6.0) as part of the Sloth 0.3.2 lock. They compile, but no one has
  run them against a real robot on this stack yet — do a deploy-and-smoke-test
  before relying on either mid-season.
- **CachingHardware** is a plain runtime library, unchanged, and its source repo
  has had no commits in some time. No reported breakage, no explicit
  re-validation either.
