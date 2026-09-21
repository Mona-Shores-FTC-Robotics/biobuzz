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

## Robot configuration

The hardware configuration lives in this repository, ships inside the APK, and
is read-only on the Driver Station. Nobody hand-edits a configuration on the DS,
and nothing is pushed over adb.

| | |
|---|---|
| The configs | `TeamCode/src/main/res/xml/robot_19429.xml`, `robot_20245.xml` — one per robot |
| The names in Java | `hardware/DeviceNames.java` — the only place a device name may be written |
| The build-time check | `RobotConfigXmlTest` — fails CI if the XML and `DeviceNames` disagree |
| The run-time check | the **Validate Hardware** OpMode (Diagnostics group) |

### How it reaches the robot

Install the app. That's it — a bundled `res/xml` config appears in the DS
configuration list automatically.

**One manual step survives, once per robot: select the config and Activate it.**
The active configuration is a `SharedPreferences` value on the Robot Controller.
An APK cannot set it, so this cannot be automated — the design works around it
rather than pretending otherwise. It survives reinstalls, so it really is once
per robot (until a factory reset or a new hub).

### Changing ports, and adding a robot

**Ports in these files are a specification, not a description.** If a robot is
wired differently, either rewire it to match the file or edit the file to match
the robot. Either is fine. What is not fine is the two disagreeing silently,
which is what the test prevents.

Adding a device is three edits — a constant in `DeviceNames`, an entry in
`DeviceNames.ALL`, and the element in **every** `robot_*.xml`. Miss the third
and the test names the file.

Adding a robot is two edits — a `res/xml/robot_<name>.xml` and a matching
`RobotIdentity` constant. The test asserts those two sets match exactly. At that
price there is no reason to cap the number of robots: a spare chassis, or last
season's bot kept as a test mule, costs one file.

One known limit: the test requires every robot to declare every device. A
stripped prototype missing a subsystem would fail it. With only a drivetrain and
odometry today, any rollable chassis has all five, so this hasn't bitten yet —
but it is a real boundary, not an oversight.

### Device names are never string literals

`DeviceNames` is the only place a hardware name may be written. Not a literal in
an OpMode, and above all not a field on a tunable config object.

Last season's project had a five-constant registry (`Constants.HardwareNames`)
that covered five of twenty-one devices. Every other name lived as a mutable
`public String motorName = "launcher_left"` on a config object held by an
`@Configurable` static field — which meant **the device name was live-editable
from the Panels dashboard**, two clicks from a launcher gain. Two OpModes
bypassed the registry with raw `"lf"` / `"rf"` literals anyway. A device name is
identity, not tuning.

### No silent hardware lookups

Never write a lookup helper that swallows a missing device. Last season's
`CachedHardware.tryMotor` caught `IllegalArgumentException` and returned `null`,
and every subsystem used it. Three lane colour sensors were commented out of
both robot XMLs during a driver rollback and never restored — the Java kept
asking for `lane_left_color`, got `null`, and degraded quietly for weeks. The
"could not find device" crash this pattern was meant to avoid is *more*
informative than what replaced it. If a device is genuinely optional, make that
an explicit, named decision.

### Robot identity comes from the active config

`ActiveConfig.requireIdentity()` reads the active configuration's name and maps
it to a `RobotIdentity`. Somebody already has to pick a config once per robot,
so that pick does double duty — and wiring and per-robot constants can never
disagree about which robot this is.

An unrecognised config **fails loudly**. Last season had four disagreeing
fallbacks: an initial `"UNKNOWN"`, a `setRobotName(null)` that became 19429, a
log line announcing 19429, and a profile builder that actually used 20245. A
robot could run one robot's tuning while telling you it was using the other's.

Rejected:

- **Control Hub WiFi SSID** (what DECODE used) — needs a hidden Android API via
  reflection, and resolves *after* the class load that needs it. That ordering
  bug is why that project needed `RobotProfile.invalidate()` plus a
  hand-maintained list of `reloadProfileConfigs()` calls; forget to extend the
  list when you add a subsystem and you silently get stale values.
- **Hub serial number** — stable but opaque; a hub swap becomes a hex-string
  hunt.

### Why bundled, and not DECODE's `adb push`

Last season shipped `deploy_config19429.bat` / `deploy_config20245.bat`, which
`adb push`ed an XML from the repo root to `/sdcard/FIRST/`. It worked. Bundling
beats it on every axis: no adb, no Wi-Fi-to-robot step, no Windows-only script
with a hardcoded `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`, no "did
anyone re-run the bat after pulling?", and adding a third robot no longer means
copy-pasting a third 58-line script.

The one that matters most: a config in `/sdcard/FIRST/` **is editable in the DS
editor**, and an edit there silently diverges from the repo copy until someone
re-runs the script and clobbers it. A bundled config is read-only, so that drift
path is closed rather than discouraged. Run **Validate Hardware** to find out
whether the active config is bundled (`RESOURCE`) or somebody's hand-made one
(`LOCAL_STORAGE`).

### Why a test, and not code generation

Generating the XML from Java (or Java from the XML) would make drift impossible
rather than merely detected. It was rejected: it needs a Gradle codegen task
wired into generated source or resource directories, against a build whose
Sloth / Load / AGP versions are locked together as described above. A plain JVM
test gets the same practical guarantee with no build-tooling risk, better error
messages, and it runs in a `testDebugUnitTest` CI job that already existed with
nothing in it. Cheap beat clever.

### `<Robot>` carries no `name` attribute — deliberately

`RobotConfigFileManager.getXMLFiles()` names a bundled config from the `<Robot>`
element's `name` attribute, **falling back to the resource entry name**:

```java
RobotConfigResFilter.getRootAttribute(parser, "Robot", "name",
                                      resources.getResourceEntryName(id))
```

Omitting `name` means the filename is the one and only string identifying a
robot, it is visible in the repo, and it is greppable. Adding one would create a
second identity string free to disagree with the first — a new drift surface in
a change whose whole point is closing them. The test enforces its absence.

### I2C: `bus` is the attribute that matters

`LynxI2cDeviceConfiguration.deserializeAttributes` reads `bus`, and only falls
back to `port` when `bus` is absent. `port` is otherwise vestigial for I2C
devices. DECODE's two robots declared the Pinpoint as `port="1" bus="1"` and
`port="0" bus="1"` and behaved identically, which is why — the `port` difference
was cosmetic. These files keep the two equal so the file cannot be misread.

### Risk: bundled configs depend on Sloth, not just the SDK

**This is load-bearing and completely non-obvious.** Sloth reflectively
overwrites `ClassManager`'s `filters` field with
`dev.frozenmilk.sinister.sdk.FalseEmptySet` (see
`dev.frozenmilk.sinister.sdk.SDKClassFilterRemover`), which drops **all** SDK
class filters — including `RobotConfigResFilter`, the thing that discovers
app-bundled configs.

Bundled configs work anyway only because Sloth ships its own replacement:
`dev.frozenmilk.sinister.sdk.RobotConfigResScanner`, whose `IdResFilter` and
`IdTemplateResFilter` call `RobotConfigFileManager.setXmlResourceIdSupplier` /
`setXmlResourceTemplateIdSupplier`. Sloth also ships `ConfigurationTypeScanner`,
which is what keeps custom `@DeviceProperties` drivers registering.

So config discovery is a **Sloth 0.3.2** feature here, not an SDK one. A Sloth
bump can break it with no compile error. **Symptom: the configs simply stop
appearing in the Driver Station list.** If that happens after a dependency
change, look here first, and add it to the version-lock constraints above.

## The `pedro` package

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedro/` is copied verbatim
from the official Pedro Pathing Quickstart at tag `v3.0.1`. It is upstream code,
not ours — when Pedro releases a new Quickstart, re-copy it rather than patching
it in place.

Two of those files are deliberately stubs upstream, and are where our robot
configuration goes. Both are now filled in; everything else in the package is
still untouched upstream code.

- **`Constants.java`** — holds `drivetrainConfig` (mecanum), `localizerConfig`
  (Pinpoint) and `foresightConfig`, plus the factory methods AutoTune needs.

  **The argument order in the old stub comment was wrong.** It suggested
  `new Follower(drivetrain, localizer, foresight)`; the real 3.0.1 signature is
  `Follower(Localizer, Drivetrain, Algorithm)` — localizer first. Upstream's own
  `procedures/Tests.java` builds it in the correct order, so the comment was the
  outlier, not the library.

- **`Tuning.java`** — registers the procedures that match our hardware.

  **`@Tuner` goes on a method, not a field.** The earlier wording here said
  "`@Tuner`-annotated fields", which does not work: the annotation is
  `@Target(METHOD)`, and `TunerScanner.scan` walks `getDeclaredMethods()`
  requiring each one to be **static, zero-argument, and to return `Procedure`**.
  It throws `IllegalArgumentException` otherwise — *"Method %s.%s is annotated
  with @Tuner, but is not static."* — during OpMode discovery, so a mistake here
  takes down robot startup rather than failing quietly.

**Nothing in this package registers an OpMode**, and that is still true now that
the stubs are filled in — there is not a single `@TeleOp` or `@Autonomous`
annotation in it. AutoTune creates its tuning OpModes at runtime from the
registered procedures; they never exist as annotated classes here.
The module's one annotated OpMode lives in `shooter/`, documented below.

### Our hardware, and what still needs measuring

| | |
|---|---|
| Drivetrain | Mecanum — `frontLeft`, `frontRight`, `backLeft`, `backRight` |
| Localizer | goBILDA Pinpoint (`pinpoint`), goBILDA 4-bar odometry pods |

Three groups of values in `Constants.java` are **placeholders that AutoTune
replaces**, and the robot will not drive correctly until it has:

1. Motor directions — currently the conventional left-reversed guess.
2. Pinpoint pod directions and X/Y offsets — currently `FORWARD` and `0.0`.
   Zero offsets treat the pods as sitting on the tracking centre, so heading
   changes corrupt the position estimate.
3. All of `foresightConfig` — currently empty, see below.

### Reaching AutoTune

There is no OpMode to select. AutoTune's `Hooks` class starts a web server when
the Robot Controller's event loop initializes, so it is running as soon as the
app is: connect to the robot's wifi and open **`http://192.168.43.1:10158`**
(`192.168.49.1` if the RC is a phone rather than a Control Hub). The procedures
registered in `Tuning.java` are listed there.

Run the tuners in this order; each produces the values the next one needs.
**Mecanum Tuner → Pinpoint Tuner → Foresight Tuner → Tests.** Each ends on a page
of generated Java to paste over the matching block in `Constants.java`.

### Foresight cannot be configured off the robot

`foresightConfig` is intentionally an empty lambda. Twelve of `ForesightConfig`'s
variables are declared `ConfigVar.required(…)` with **no default** —
`headingFeedback`, `forwardTranslational`, `strafeTranslational`, `brake`,
`coast`, the linear/quadratic/heading brake coefficients, both
`maxAchievable*Velocity` and both `natural*Deceleration`. Reading an unset one
throws `IllegalStateException("Config variable has not been set")`.

Those twelve are exactly what the Foresight Tuner measures. There is no
"defaults for now" option and nothing here should be guessed: they are
properties of this robot's mass, wheels and battery. `Constants.createAlgorithm()`
probes one of them and fails at init with a message naming the fix, because the
bare library error surfaces partway through following a path with no field name
and no hint.

The drivetrain and localizer configs are complete, so the Mecanum, Pinpoint and
Foresight tuners all run today, as do the Tests procedure's localization,
odometry, pose and driving tests. Only its hold, line and curve tests need a
tuned Foresight, since only those build a `Follower`.

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

### First time on the robot

**Nobody has run this on a robot yet.** Before you rely on it at a meeting, put
the robot somewhere the flywheels can spin free and walk through this once:

1. **Does the OpMode show up?** Look for **Flywheel Speed Test** in the TeleOp
   list on the Driver Station. If it isn't there, do a full **TeamCode**
   install rather than a Sloth Load.
2. **Does Panels come up?** Open Panels and look for `FlywheelBank → config`.
   Nobody has run Panels on the Sloth `0.3.2` stack yet, so this is the step
   most likely to surprise you. If it doesn't appear, the Driver Station
   readout still works and the gamepad still tunes the target speed.
3. **Press A.** The target starts at 1500 RPM. All three lanes should climb and
   land on `[READY]`.
4. **Is any lane showing a negative RPM?** That wheel is spinning backwards —
   tick `reversed` for it in Panels and it should flip positive. (DECODE's
   robot 20245 ran its right lane reversed; 19429 ran all three forward.)
5. **Does the RPM look believable?** If it's off by a constant factor — double,
   half, ten times — the encoder maths is wrong, not the motor. Fix
   `measurement.ticksPerRev` first, then `gearRatio`.

Once those five pass, the rig works. Press B to stop; the wheels coast down.

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

## The `vision` package

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/vision/` tracks the BIOBUZZ
HIVE CELLs with a Limelight 3A. It reports where a cell is **relative to the
robot** — range, bearing and elevation in the chassis frame — and deliberately
produces no field pose.

### Why it does not produce a field pose yet

BIOBUZZ publishes no AprilTag field positions. Not imprecise ones — none:

```
AprilTagGameDatabase.getBioBuzzTagLibrary()
  getAllTags()      -> empty
  lookupTag(30..45) -> null for every id
  getAllClusters()  -> 4 clusters, every fieldPosition = (0, 0, 0)
```

Those zeros are deliberate rather than an unfilled TODO — the same convention FIRST
used in DECODE, where the fixed goal tags 20/24 carried real coordinates while the
Obelisk tags 21-23, repositioned between matches, were published as zeros. The SDK
12.0 release notes say why, in bold: *"Unfortunately, since BIOBUZZ AprilTags move,
they are not suitable for absolute Field Localization."*

**Read that statement carefully — it is narrower than it first looks.** FIRST cannot
ship one field position per tag in a library every team shares, so their library does
not support localization. That is not the same as the poses being unknowable. The
HIVE CELLs are bistable: they pivot between two mechanically-defined positions and
dwell there (hence `Goal Pivot Assembly` and the flanged bearings in the field CAD).
Two known poses per cluster is a perfectly tractable thing to model — it is just
something a team has to measure and maintain itself, which is exactly why FIRST
cannot do it for everyone.

So the plan is a field pose derived from **eight** measured cluster poses — four
cells, two states each — plus a state classifier and a transition filter. See
"Open work" below. Until those poses are measured, this package reports only
robot-relative geometry, which is what a turret needs anyway and is the substrate
the field-pose layer will sit on.

#### Why we will compute it ourselves rather than use MegaTag

`Limelight3A.uploadFieldmap()` exists, so uploading a per-state `.fmap` and letting
MegaTag solve looks tempting. Don't: **the four cells can be in different states at
the same time**, so no single field map is ever correct. Solving from whichever cells
are visible, each with its own state, handles mixed states naturally. Runtime map
swapping would also be slow and would still be wrong half the time.

Nothing here calls `getBotpose()`, `getBotpose_MT2()` or `updateRobotOrientation()`,
and the field-pose layer should not either.

#### Localization from these tags is a correction, not a reset

A damped, pivoting, volunteer-assembled game element will not return to precisely the
same pose on every tip. A hive-derived pose deserves considerably larger covariance
than a wall tag would, should be gated against odometry, and should never hard-reset
the pose estimate. The spread across many tips is worth measuring — point **Vision:
Noise Tuner** at a cell and tip it by hand between samples.

### What the SDK does publish, and how we use it

Cluster-internal geometry is real and useful. Each cell carries four tags at
published offsets from the cluster origin, and that origin sits at the centre of
the cell opening rather than on the tags themselves:

| Cell | Tag ids | Member offsets (in, cluster frame) |
|---|---|---|
| `RED_SCORING` | 30-33 | x = -6.5 / -2.75 / +2.75 / +6.5 |
| `RED_AUDIENCE` | 34-37 | y = +7.1874 (all members) |
| `BLUE_AUDIENCE` | 38-41 | z = -5.6220 (all members) |
| `BLUE_SCORING` | 42-45 | tag size 3.25" |

So one visible tag locates the cell, and two or more locate the centre of the tag
row *exactly* — the lateral axis falls out of the tag positions, no orientation
convention needed. That is what keeps the aim point from jumping as members drop
in and out of view, which is the failure mode of picking a single best tag each
frame. With only one tag visible the lateral direction is unknowable and the point
can sit up to 6.5" off along the row; `CellSighting.lateralCorrectionApplied()`
reports that case rather than hiding it.

Cell *identity* is read from the SDK at runtime through the public
`AprilTagLibrary.lookupCluster(int)`, so a future correction is picked up for free.
The member offsets are not reachable through public API — `clusterMembers` is
package-private — so they are transcribed in `BiobuzzTags` and `BiobuzzTagsTest`
reads the SDK's real values reflectively and fails if the two ever drift apart.

### Two conventions that need ten minutes on the robot

Everything above is verified against the SDK and covered by unit tests. Two things
cannot be settled without hardware, and both are isolated to one place each:

1. **The camera's axis convention.** Limelight documents `targetpose_cameraspace`
   as +X right, +Y down, +Z forward, but the FTC SDK wrapper passes the three
   numbers straight through from the camera's JSON without normalising them, so
   nothing in the SDK proves it. Run **Vision: Raw Tag Dump** and check. If it is
   different, fix `CameraMount.cameraAxesToRobotAxes` — one method, and
   `CameraMountTest` covers the rest of the chain.
2. **The offset from the tag row out to the cell opening.** A fixed
   `(0, +7.187, -5.622)` inches in the cluster plane, but applying it needs the
   cluster's 3D orientation, which needs the Euler convention the Limelight reports
   yaw/pitch/roll in — also not pinned down by the SDK. It is mostly vertical and
   depthward, so it barely affects bearing. **A turret aiming on bearing can ignore
   it; a shooter solving for elevation cannot.**

### Setup this depends on

- Limelight in the hardware map as `limelight`.
- An AprilTag pipeline with tag size set to **3.25"**. Get this wrong and every
  range is off by a constant factor, with nothing about the output looking broken.
- That pipeline emitting **full 3D pose**. Without it no sighting can be built at
  all; `LimelightVisionSubsystem.framesMissing3dPose()` counts the case and both
  diagnostic OpModes display it, so the failure says what it is instead of just
  reporting "no target".

### The OpModes, in the order to run them

All three are under the **Vision** group and are enabled, not `@Disabled`.

1. **Vision: Raw Tag Dump** — raw per-fiducial numbers before any of our maths.
   Settles the axis convention above.
2. **Vision: Sighting Diagnostics** — live range/bearing/elevation per cell, with
   camera mounting live-tunable in Panels. The tape-measure check.
3. **Vision: Noise Tuner** — running mean and standard deviation of a stationary
   sighting. Re-measure at a few distances; AprilTag noise grows quickly with range.

### Published HIVE geometry

`HiveGeometry` transcribes what the Competition Manual does publish, each value
tagged with the figure it came from. `HiveGeometryTest` cross-checks the numbers
against each other, because the manual over-determines the geometry and that
redundancy catches a misread figure at build time rather than on the field.

| | | source |
|---|---|---|
| Pivot axis above tiles | 43.95 in | §9.6.1, Fig 9-8 |
| CELL tilt from horizontal | 30° | Fig 9-10 |
| CELL spacing within a HIVE | 18.84 in | Fig 9-9 |
| Red↔blue HIVE centre spacing | 25.5 in | Fig 9-10 |
| Bottom of HIVE above tiles | 25.5 in | Fig 9-10 |
| Raised opening, bottom / top | 53.5 / 65.6 in | Fig 9-10 |
| Frame width × depth | 49.46 × 38.95 in | §9.6.1 |
| CELL opening | 20 × 14 × 12 in | §9.6.2, Fig 9-11 |

The one derived number that matters: **a given cell's tags rise about 9.42 in
between its lowered and raised positions.** For any point rigidly attached to the
rocker the difference works out to `2 · dx · sin(tilt)`, where `dx` is its
horizontal distance from the pivot with the rocker level — the vertical component
cancels — so it comes out to `CELL_SPACING_IN · sin(30°)` without needing to know
where the tag plane sits. That is what makes height a usable state discriminator
before anything has been measured, and it is what sets the classifier tolerance.

A useful self-check fell out of this: the manual's own opening heights span
65.6 − 53.5 = 12.1 in, and a 14 in opening tilted 30° from horizontal spans
14·cos(30°) = 12.12 in. That agreement is what confirms the 30° is measured from
horizontal rather than from vertical. It is a test, not a comment.

**What the manual does not publish is any XY coordinate for the clusters.** §9.9
is explicit that the sticker's Reference Holes "can be used to measure the location
of the AprilTag Cluster relative to the rest of the FIELD" — FIRST expects teams to
measure it, and the Initial Field Element Assembly Guide likewise locates the
sticker by hole alignment with no numeric offsets. So the eight cluster poses are
still an open measurement task.

### Cell state: UP, DOWN, or don't ask

Because the cells are bistable, "where is this cell" reduces to "which of two poses,
and what are the two". `CellStateTracker` answers the first half.

It classifies on the **height of the tag row above the floor**, which falls straight
out of the measurement and depends only on the camera's axis convention — not on the
Euler convention for yaw/pitch/roll, which nothing in the SDK pins down. Orientation
would work too and is worth adding as a confirming second opinion later, but height
needs one fewer unverified assumption in the path to a decision that gates
localization.

Two properties are deliberate and worth not "fixing":

- **A height matching neither nominal classifies as UNKNOWN.** That is the transition
  signal — it means the tracker never has to detect motion directly.
- **Confirming a state is slow, losing one is instant.** Establishing UP or DOWN takes
  several consecutive agreeing frames spanning a dwell time; a single contrary frame
  drops it. Briefly reporting UNKNOWN during a real tip costs nothing. Confidently
  reporting a stale UP for a cell that has already dropped injects a badly wrong pose.

`CellStateTracker.Geometry` defaults both nominal heights to NaN, which makes every
cell report UNKNOWN until someone measures them. That is intentional; an unmeasured
field is not a reason to guess. **Vision: Sighting Diagnostics** prints the measured
row height per cell, labelled, so settling a cell and reading it off is the whole
procedure.

### Open work

Ordered roughly by what unblocks what.

1. **Measure the two tag-row heights** and put them in `CellStateTracker.Geometry`.
   Until this happens the state classifier returns UNKNOWN for everything and no
   field pose is derivable. The manual gives the *difference* (~9.42 in, above) but
   not the absolutes. Cheapest possible task — settle a cell, read the labelled row
   height off the diagnostics OpMode, tip it, read it again. The two readings should
   come out about 9.4 in apart; if they don't, something upstream is wrong and worth
   chasing before going further.
2. **Measure the eight cluster poses** — four cells, two states each — in field
   coordinates. Not published, so this comes from the field CAD checked against a
   real field, or from the Reference Holes as §9.9 suggests. This is the actual
   input the field-pose layer needs. `HiveGeometry` constrains the answer: the HIVE
   structure is centred on the field, the two HIVES are 25.5 in apart laterally, and
   each cell sits about 8.2 in horizontally and 4.7 in vertically from its pivot at
   43.95 in.
3. **Build the field-pose layer.** Robot-in-field from cell-in-field composed with
   the measured cell-relative geometry, solving from whichever cells are visible and
   settled, each with its own state. Gate every candidate against odometry so a
   misclassified state or a mid-tip reading is rejected before it reaches the
   estimator.
4. **Measure tip-to-tip repeatability.** Point **Vision: Noise Tuner** at a cell and
   tip it by hand between samples. The spread across tips — not the frame-to-frame
   noise — is what sets the covariance a hive-derived pose deserves.
5. **Add orientation as a confirming discriminator**, once the Limelight's Euler
   convention has been established on the robot.

#### Settled: can the camera see the lowered cell's tags?

Yes, most likely — so there is no shortcut here, and height classification is
genuinely needed.

The question was whether a lowered cell's tags rotate out of view, which would have
meant "a visible tag implies UP" and deleted most of the work above. They do not.
Figure 9-7 labels the cluster "**AprilTag Cluster under each CELL**" and shows both
the raised and the lowered cell's clusters in the same side elevation. The geometry
agrees: the rocker swings ±30°, so a cell bottom that faces straight down at rest
stays within 30° of vertical in both positions rather than rotating away.

Read off a figure rather than measured, so confirm it on a real field — actual
sightlines also depend on camera height and range. But plan for both cells being
readable, which is the harder case and also the more useful one.

### What was dropped from the DECODE port, and why

| Dropped | Why |
|---|---|
| MegaTag1/MegaTag2 pose, `PoseFrames` MT converters | No single field map is ever correct, since cells can be in different states at once. The field-pose layer will compute this itself |
| `shouldUpdateOdometry()` / relocalization gate | Will return, gated on cell state and innovation rather than on tag freshness alone |
| `setRobotHeading()` / `updateRobotOrientation()` | Only feeds MegaTag2 |
| `FieldConstants` | DECODE game geometry — goals, motif tags, incenter/Chebyshev aim points. None of it transfers |
| `RobotState` global statics | Process-global mutable state written from inside a constructor |
| Mecanum drive code in the pattern-recognition OpMode | Out of scope; there is no drivetrain yet |
| Reflective `getTargetArea` / `getPoseAmbiguity` lookups | `getTargetArea()` is ordinary public API, and `getPoseAmbiguity()` does not exist on `LLResultTypes.FiducialResult` at all — that fallback silently returned 0.0 every time |

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
