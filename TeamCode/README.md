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
compiles to an inert set of classes. That is expected on a toolchain-only
branch; it is not a sign the copy went wrong.

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

Ordered roughly by what unblocks what:

1. **Measure the two tag-row heights** and put them in `CellStateTracker.Geometry`.
   Until this happens the state classifier returns UNKNOWN for everything and no
   field pose is derivable. Cheapest possible task — settle a cell, read the number
   off the diagnostics OpMode, repeat with it tipped.
2. **Find out whether a chassis-mounted camera can see the DOWN-state tags at all.**
   The tags are on the undersides of the cells; when a cell drops, its tags may face
   the floor. If they are never readable the classification problem collapses — a
   visible tag would imply UP. Worth answering early, because it could delete most
   of the work below.
3. **Measure the eight cluster poses** — four cells, two states each — in field
   coordinates. Neither pose is published, so this comes from the field CAD, checked
   against the real field. This is the actual input the field-pose layer needs.
4. **Build the field-pose layer.** Robot-in-field from cell-in-field composed with
   the measured cell-relative geometry, solving from whichever cells are visible and
   settled, each with its own state. Gate every candidate against odometry
   (innovation gating) so a misclassified state or a mid-tip reading is rejected
   before it reaches the estimator.
5. **Measure tip-to-tip repeatability.** Point **Vision: Noise Tuner** at a cell and
   tip it by hand between samples. The spread across tips — not the frame-to-frame
   noise — is what sets the covariance a hive-derived pose deserves.
6. **Add orientation as a confirming discriminator**, once the Limelight's Euler
   convention has been established on the robot.

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
