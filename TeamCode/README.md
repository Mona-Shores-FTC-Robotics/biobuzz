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

### When a Wi-Fi deploy fails

Deploying over Wi-Fi has worked only some of the time, both last season
(DECODE) and now, and nobody has pinned down why. USB-C has always worked. This
section records what we know, what we only suspect, and what we ruled out, so
the next person does not start from zero. **Nothing below has been confirmed as
*the* cause on our robots yet.** When one of these fixes clearly works (or
clearly doesn't), update this section and say which.

#### Checklist — do these in order, stop when it works

Run the commands in the Android Studio **Terminal** tab. If `adb` is "not
recognized", use the full path:
`%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`.

1. **Is the laptop on the robot's network?** Check the Wi-Fi menu: the robot
   adapter should show the robot's own network as *Connected*, even though it says "No internet". If it has dropped off, click
   **Connect** on it by hand (see *Windows drops the robot network* below for
   why by hand matters).
2. **Can the laptop reach the hub at all?** In a PowerShell terminal:
   `Test-NetConnection 192.168.43.1 -Port 5555`.
   `TcpTestSucceeded : True` means the network is fine and the problem is adb —
   go to step 3. `False` means it is a network problem — go to step 5.
3. **Look at what adb thinks:** `adb devices`.
   - `192.168.43.1:5555  device` — adb is fine; re-run the deploy.
   - `… offline`, or nothing listed — continue.
4. **Reset the connection:** `adb disconnect`, then
   `adb connect 192.168.43.1:5555`, then `adb devices` again. If it still says
   `offline`, or you saw a message like `adb server version (…) doesn't match
   this client (…); killing…`, run `adb kill-server`, then connect again.
   Deploy.
5. **Check Windows is using the right adapter:**
   `Find-NetRoute -RemoteIPAddress 192.168.43.1 | Select InterfaceAlias`.
   It must name the adapter joined to the robot. If it names the other adapter,
   that adapter is also on a `192.168.43.x` network (a phone hotspot is the
   usual culprit) — join a different internet network, or turn the second
   adapter off while you deploy.
6. **Still failing:** power-cycle the robot, wait for its Wi-Fi to come back,
   rejoin it, and start at step 1. If it fails again, plug in USB-C and move
   on — then note what you saw in the tracking issue.

#### Candidate causes

| Cause | Status | Why it would look intermittent | Fix |
|---|---|---|---|
| Sloth Load disconnects Wi-Fi adb after it deploys | **Confirmed in Sloth's code**, not yet watched happening on the robot | Depends on whether adb was already connected when you pressed the button | Run `adb connect` yourself before deploying (checklist step 4) |
| Windows drops the robot network ("soft disconnect") | Hypothesis — documented Windows behaviour | Only happens once the network has been idle for a while, and after sleep/restart | Connect to the robot network by hand each session |
| adb holding a dead connection | Hypothesis — very common adb behaviour | Appears after the robot reboots or the laptop sleeps | `adb disconnect` / `adb kill-server` (checklist step 4) |
| Two copies of adb restarting each other | **Partly tested** — see below | Only when the other program is running | Use one adb; close other adb tools while deploying |
| Laptop routes `192.168.43.1` out the wrong adapter | Hypothesis | Depends on which network the second adapter is on that day | Checklist step 5 |
| Wi-Fi congestion | Hypothesis — likely at events, unlikely in our room | Worse when many robots are powered on | Move the hub to 5 GHz, if our USB adapter supports it |
| USB Wi-Fi adapter power-saving | Hypothesis | Adapter naps when the laptop thinks it is idle | Turn power-saving off (below) |

**Sloth Load disconnects Wi-Fi adb after it deploys.** Read from the compiled
`dev.frozenmilk:Load:0.3.2` plugin (not its docs — it has none on this). Its
default `autoconnect` mode is `MAINTAIN`, aimed at `192.168.43.1`. Before
deploying it runs `adb devices`; if nothing is listed it runs
`adb connect 192.168.43.1` itself, deploys, and then runs `adb disconnect`
**with no address** — which drops *every* Wi-Fi adb connection, not just its
own. So a Sloth Load that started with nothing connected leaves nothing
connected, and the **TeamCode** install you try next finds no robot. It also
means that if `adb devices` lists anything at all — even a stale `offline`
entry — Sloth assumes it is connected and does not reconnect. Connecting by
hand first (checklist step 4) avoids both. The setting can be changed in
`TeamCode/build.gradle`, but that is a Gradle change and deliberately not made
here; try the habit first.

**Windows drops the robot network.** With two Wi-Fi adapters, Windows'
connection manager by default tries to keep only the connections it needs.
Microsoft documents that it keeps networks you **connected to by hand this
session** and the preferred internet connection, and "soft-disconnects" the
rest: it stops sending new connections over that network, then disconnects it
once traffic drops below a threshold (checked every 30 seconds). The robot
network has no internet, so if Windows joined it *automatically* — at startup
or after sleep — it is a candidate to be dropped as soon as nobody is using it.
That fits the pattern of deploys working while you are actively deploying and
failing after a break. It also fits the observation that **having the REV
Hardware Client open seemed to help**: the Hardware Client keeps talking to
the hub, so the network never goes idle. That link is a guess, not a
measurement. The laptop-wide fix is the Group Policy setting *Minimize the
number of simultaneous connections to the Internet or a Windows Domain* set to
`0`; that changes Windows behaviour for the whole laptop, so it is a
mentor decision, not a checklist step.
([Microsoft: Windows Connection Manager](https://learn.microsoft.com/en-us/windows-hardware/drivers/mobilebroadband/understanding-and-configuring-windows-connection-manager))

**Two copies of adb.** Only one adb *server* runs per laptop, and every adb
program talks to it. If a program with an **older** adb starts, it kills the
server and starts its own, dropping every connection — the telltale is the
`doesn't match this client … killing` message. Candidates on our laptops:
Android Studio's copy (the one Sloth and the TeamCode install use), the copy
DECODE committed at `adb/adb.exe`, and any copy the REV Hardware Client or
tools like scrcpy bring along. **Tested on one mentor laptop:** DECODE's copy
(platform-tools 36.0.0) and Android Studio's (37.0.1) share a server without
killing each other — both speak adb protocol 41 — so that pair is not the
problem there. The REV Hardware Client's copy has not been checked; run
`adb version` on each copy you find and compare the first line. Note this
cause would make things *worse* with the Hardware Client open, so it does not
explain the "Hardware Client helps" observation.

**The hub's adb port.** The Control Hub listens for adb on port 5555 out of
the box; REV's docs say it "is configured to support ADB wireless connections
on port 5555". We found nothing saying the REV Hardware Client turns that port
on or off, so do not open the Hardware Client as a ritual — if it helps, it is
more likely for the keep-the-network-busy reason above.
([REV: Deploying code wirelessly](https://docs.revrobotics.com/duo-control/managing-the-control-system/android-studio-using-wireless-adb))

**Wrong route.** Every Control Hub hands out `192.168.43.x` addresses, and so
do many Android phone hotspots. If the internet adapter is on a phone hotspot,
Windows has two networks claiming `192.168.43.1` and may send adb to the
phone. Checklist step 5 shows which one it picked.

**Congestion and power-saving.** Busy 2.4 GHz channels at events can make
connections slow enough to time out. The hub's band and channel can be changed
from its Manage page (`http://192.168.43.1:8080`); only move to 5 GHz if the USB
adapter can see it. For power-saving: Device Manager → Network adapters → the
USB Wi-Fi adapter → *Power Management*, untick *Allow the computer to turn off
this device to save power*; also set *USB selective suspend* to *Disabled* in
the power plan's advanced settings.

**Ruled out or out of scope.**
- *Static electricity* — it is a real cause of robots dropping out *on the
  field*, but it would not make a laptop on a desk fail to reach a hub that is
  otherwise running normally. Not pursued.
- *DECODE's robot-identity code*, which read the hub's own Wi-Fi name through a
  hidden Android API, ran on the robot and never touched the laptop's
  connection. It is being replaced separately.

#### When to just use USB-C

Use the cable, and don't debug, when:

- you are at a competition, or a meeting is about to end — debug Wi-Fi on
  practice time, not match time;
- the checklist has failed once already this session;
- you are doing a full **TeamCode** install and Wi-Fi has been flaky today — a
  half-finished reinstall over a dropping link can leave the robot without a
  working app until the next good install.

USB-C needs no `adb connect`: plug in, check `adb devices` shows a serial
number, and deploy.

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
