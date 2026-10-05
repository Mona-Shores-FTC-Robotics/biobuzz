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


FTC Dashboard used to be the tenth row of that table. It is gone — not because it
is unwanted, but because it and Panels cannot both be installed on this robot.
See [Why FTC Dashboard is not in the dependency set](#why-ftc-dashboard-is-not-in-the-dependency-set).

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
| The configs | `TeamCode/src/main/res/xml/robot_19429.xml`, `robot_20245.xml` — one per robot — plus `robot_launcher_rig.xml` for the two-wheel launcher bench rig (hub FTC-EoM3) |
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

Adding a device is three edits — a constant in `DeviceNames`, an entry in the
device list of each robot that has it (`DeviceNames.COMPETITION_ROBOT` for both
competition robots), and the element in each of those robots' `robot_*.xml`.
Miss the third and the test names the file.

Adding a robot is two edits — a `res/xml/robot_<name>.xml` and a matching
`RobotIdentity` constant, which names the `DeviceNames` list it carries. The test asserts those two sets match exactly.
A robot that carries the drivetrain also needs its Pedro values — one file and one
line, see [One file of tuned values per robot](#one-file-of-tuned-values-per-robot) —
and CI fails until it has them. At that
price there is no reason to cap the number of robots: a spare chassis, or last
season's bot kept as a test mule, costs one file.

Each configuration is checked against **its own robot's** device list: it must
declare every device on that list and nothing else. `DeviceNames.ALL` is now the
union of the lists — every name the code knows — and is a requirement on no one
file.

> **History note (26 Sep 2026).** This paragraph used to say "the test requires
> every robot to declare every device", and called that a known limit that
> "hasn't bitten yet". It bit with the first bench rig: the two-wheel pinch
> launcher on its own Control Hub (FTC-EoM3), which has one motor and nothing
> else. Rejected alternatives: naming the rig's file something other than
> `robot_*.xml` so the tests skip it (smallest change, but then a typo in its
> motor name is found at a meeting, which is the thing this whole section
> exists to prevent); and adding `launcher2` to both competition robots' XML
> (would declare a device neither robot has). `Validate Hardware` likewise now
> checks the live hardware against the active robot's list.

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

### The Limelight: an Ethernet device, not a hub device

A Limelight 3A has no hub port. SDK 12 models it as an `<EthernetDevice>`
(`BuiltInConfigurationType.ETHERNET_OVER_USB_DEVICE`), which
`ReadXMLFileHandler.parseRobot` reads only as a **direct child of `<Robot>`**,
beside `<LynxUsbDevice>`. `HardwareFactory.mapEthernetOverUsb` then builds the
`Limelight3A` from two attributes: `name`, and `ipAddress`, where the RC sends
its HTTP requests. `172.29.0.1` is the 3A's factory address on a Linux/Android
USB-Ethernet link. `serialNumber` is only a hardware-map key: it has to start
`EthernetOverUsb:` (the `Limelight3A` constructor stores it as an
`EthernetOverUsbSerialNumber`), and nothing compares it with what is plugged in.

So `DeviceNames.Kind` has a fourth value, `ETHERNET`, and `RobotConfigXmlTest`
checks it in its own way: the element sits under `<Robot>`, `ipAddress` is a
dotted IPv4 address no other Ethernet device uses, and `serialNumber` parses.
No port range check (#64).

**Consequence: a configured Limelight is always "found".** The SDK builds it from
the file, so an unplugged camera, or one moved to a static IP that the file does
not name, is in the hardware map and never answers. `LIMELIGHT NOT FOUND` now
means the active config is wrong. A dead cable shows as **Link: NO RESPONSE** in
every Vision OpMode, and as a FAULT on the Robot page.
`LimelightVisionSubsystem.isConnected()` is the accessor (the SDK's own
250 ms test).

Rejected: `USB` or `PORTLESS` as the kind. The webcam is also portless, but it
is a different element (`<Webcam>`) with a per-unit serial number, so one kind
for both would mean "not on a hub", not "checked like this". The webcam is still
on `DeviceNameLiteralTest`'s allowlist, waiting on its own decision.

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

## The subsystem convention

Every mechanism is a class in `subsystems/` implementing `Subsystem`, built in `Robot`,
and reached from an OpMode that extends `RobotOpMode`. Four files carry the whole pattern:

| File | What it is |
|---|---|
| `subsystems/Subsystem.java` | The contract: `initialize()`, `update()`, `stop()`, plus a `default periodic()` you get free |
| `subsystems/ExampleSubsystem.java` | An empty subsystem to copy. No hardware, on purpose |
| `Robot.java` | The parts list — every subsystem, built once |
| `opmodes/RobotOpMode.java` | The base OpMode. Owns the loop: bulk caching, loop timing, the scheduler, teardown |
| `subsystems/DriveSubsystem.java` | The drivetrain — the worked example of a real subsystem |

The point is not elegance, it is having an answer to *"where does my code go?"*. Adding a method
to `IntakeSubsystem` is a task a beginner can take; writing an intake is not.

### `update()` is the contract, `periodic()` is an adapter

> **History note.** This section used to say "prefer calling `update()` directly in TeleOp", and
> `RobotOpMode.useScheduler()` let an OpMode switch the scheduler off. Both are gone: the scheduler
> is always on. The reasoning below replaces the old advice; the rejected option is kept.

`update()` is one step of the mechanism's work, called once per loop. `periodic()` wraps that same
method as an Ivy `Command`, and `RobotOpMode` schedules every subsystem's `periodic()` and runs the
`Scheduler` every loop, TeleOp and Autonomous alike. An OpMode never calls `update()`; it tells a
subsystem what it wants (`robot.drive.drive(...)`) and the scheduler does the rest.

`RobotOpMode.loop()` runs in a fixed order: clear the bulk cache, `onLoop()`, then
`Scheduler.execute()`. Inputs are read and intents set *before* subsystems update, so a stick
movement reaches the motors in the same loop rather than the next.

**Why always on.** BIOBUZZ TeleOp will have macros a driver can interrupt, which is arbitration —
the scheduler's job. One execution model means a command written for Autonomous binds to a button
unchanged, and students learn one way things run.

**Rejected: an opt-out for TeleOp.** `Scheduler.execute()` allocates roughly three objects on every
call even when nothing is scheduled — it copies its running-command deque and iterates the copy,
then does an `O(n·m)` `removeAll`. That is **microseconds, not milliseconds**, and it is not a
loop-time problem. The opt-out cost more than it saved: a second way for an OpMode to run, and a trap
in it — an OpMode that turned the scheduler off had to update every subsystem itself, or look alive
while updating nothing. If `LoopTimer` ever shows the scheduler mattering, revisit with the number.

### The lifecycle is on the interface

> **History note.** The first version of #63 put only `update()` on `Subsystem` and said, here,
> that `initialize()` and `stop()` were deliberately left off because they meant different things
> in different classes, so `Robot` named each subsystem by hand. That was stale before it merged:
> review found the add-a-subsystem steps said "add it to the list", and a subsystem that was only in
> the list got stepped but never started or stopped. The mentor's call was to put the whole
> lifecycle on the interface. The old reasoning is kept below as a rejected option.

| Method | Called | Put here |
|---|---|---|
| constructor | once, when `Robot` is built | `hardwareMap` lookups, nothing else |
| `initialize()` | once, in OpMode init, after every subsystem is built | starting state: motor modes, starting a camera |
| `update()` | every loop | one step of the work |
| `stop()` | once, when the OpMode ends | teardown: cut power, stop devices, clear state |

All three are abstract, so the compiler makes a beginner decide each one, even if the answer is an
empty body. `Robot.initialize()` and `Robot.stop()` walk the `subsystems` list (stop in reverse), so
the list is the single place a subsystem is registered.

**`stop()` means teardown and only teardown.** The disagreement that kept it off the interface was
real: `FlywheelBank.stop()` cuts power and keeps the object live, where `LimelightVisionSubsystem.stop()`
ends it. The resolution is to name it, not paper over it — "stop the mechanism for a moment" gets its
own name (`idle()`, `spinDown()`). `FlywheelBank` is not a `Subsystem` and the shooter rig stays
standalone, so nothing had to change; if it ever becomes one, its `stop()` gets renamed.

**If the interface makes your subsystem awkward, change the interface — do not work around it.** It
was designed against one existing subsystem and will meet its second one soon.

### This does not supersede "No command framework"

The [shooter rig](#no-command-framework) stays a plain `OpMode` with no scheduler. That section
already anticipated this: *"when the real BIOBUZZ launcher lands and shots have to be sequenced
against an intake, that is the point to introduce Ivy commands."* The launcher has not landed. A rig
with one behaviour still has nothing for a scheduler to arbitrate.

### Why this shape, and what was rejected

Checked against `core-1.1.1-sources.jar` rather than DECODE's Ivy 1.0.0 usage:

| | Finding |
|---|---|
| A `Subsystem` type in Ivy | **None** — 20 source files, no base type. Ours is not a reinvention |
| `requiring()` | `requiring(Object...)`; `requirements()` is `Set<Object>` — so a `default periodic()` on an interface compiles |
| Default commands in `Scheduler` | **Not in 1.1.1** — [Ivy PR #7](https://github.com/Pedro-Pathing/Ivy/pull/7), unlanded. DECODE's priority-0 `defaultDrive().schedule()` is a workaround for that gap, and copying it here would be cargo-culting a missing feature |

**Ivy itself recommends none of this.** Its docs say *"a requirement can be any object"* and
[Scheduling and OpMode use](https://pedropathing.com/docs/ivy/creating-opmodes) shows
`Scheduler.reset()`/`execute()` written inline in a `LinearOpMode`, with no base class. We follow the
pattern Ivy's [example repos](https://pedropathing.com/docs/ivy/example-repos) converged on instead —
#22131 Traffic Cones has exactly this `Robot` + `RobotOpMode` pair — and we do it knowingly.

Rejected along the way:

- **Porting DECODE's architecture wholesale.** Its skeleton is ~260 lines, but its subsystems are
  6,098 and `DriveSubsystem` alone is 1,713 — a season of aim assist and vision relocalisation
  accreted onto one class. And you cannot port subsystems for mechanisms that do not exist: BIOBUZZ
  has no intake, launcher or lighting designed yet, so `IntakeSubsystem`'s 1,307 lines would be
  inherited assumptions about last season's hardware.
- **A throwaway experiment branch, deleted either way.** It wastes the work, and rebuilding from
  scratch afterwards is precisely what a green student cannot do.
- **`update()` alone on the interface**, with `Robot` calling each subsystem's `initialize()`/`stop()`
  by name. That was the first version. It avoided forcing one meaning of `stop()` on classes that
  disagreed, but it gave a subsystem two places to register, and the docs got it wrong on the first
  review. Superseded by the lifecycle above.
- **No interface at all**, which is what both Ivy and DECODE do. Most honest to the library — but
  then the compiler tells a beginner nothing, which is the one thing this is for.

## The `pedro` package

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedro/` is copied verbatim
from the official Pedro Pathing Quickstart at tag `v3.0.1`. It is upstream code,
not ours — when Pedro releases a new Quickstart, re-copy it rather than patching
it in place.

Two of those files are deliberately stubs upstream, and are where our robot
configuration goes. Both are now filled in. Beside them are two more pieces
that are ours and have no upstream counterpart — `RobotConstants.java` and the
`robots/` directory, which hold the per-robot tuned values. Everything else in
the package is still untouched upstream code.

- **`Constants.java`** — picks the running robot's tuned values and holds the
  factory methods AutoTune and our OpModes call (`createDrivetrain`,
  `createLocalizer`, `createAlgorithm`, `create`). **The tuned values themselves
  are no longer in it** — they are one file per robot, see
  [One file of tuned values per robot](#one-file-of-tuned-values-per-robot).

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

Three groups of values in **each robot's file** (`pedro/robots/Robot19429.java`,
`pedro/robots/Robot20245.java`) are **placeholders that AutoTune replaces**, and
that robot will not drive correctly until it has:

1. Motor directions — currently the conventional left-reversed guess.
2. Pinpoint pod directions and X/Y offsets — currently `FORWARD` and `0.0`.
   Zero offsets treat the pods as sitting on the tracking centre, so heading
   changes corrupt the position estimate.
3. All of `foresightConfig` — currently empty, see below.

Both robots' files start with the same placeholders. They are expected to diverge:
the robots have identical hardware, but motor directions can differ with wiring,
and pod offsets and every Foresight value are measurements of one physical robot.

### One file of tuned values per robot

> **History note (27 Sep 2026).** Until #88 this section, and `Constants.java`,
> held **one** `drivetrainConfig` / `localizerConfig` / `foresightConfig` for
> both robots, so tuning the second robot would have overwritten the first
> robot's numbers. Anything saying "paste into `Constants.java`" or naming
> `Constants.foresightConfig` is stale.

| Piece | What it is |
|---|---|
| `pedro/robots/Robot19429.java`, `Robot20245.java` | That robot's three blocks, in exactly the shape the tuners emit. **The only place tuned numbers go.** |
| `Constants.ROBOTS` | One line per robot: `RobotIdentity` → its file |
| `Constants.forRobot(identity)` | Pure lookup, unit tested. Throws, naming the identity, for a robot with no drivetrain (`LAUNCHER_RIG`) or with no file |
| `createDrivetrain` / `createLocalizer` / `createAlgorithm` / `create` | Unchanged signatures; call `forRobot(ActiveConfig.requireIdentity())` |
| `RobotConstants` | Wraps one robot's three blocks, and sets the **device names** on them from `DeviceNames` |

**Which file is used is decided by the active Driver Station configuration** —
the same pick that selects the robot's wiring (see
[Robot identity comes from the active config](#robot-identity-comes-from-the-active-config)).
Activate `robot_19429` and 19429's numbers are used. There is no default: an
unknown or drivetrain-less configuration stops the OpMode at init with a message
naming it. Last season a silent 19429/20245 fallback ran one robot on the other's
tuning, and that is the bug this layout exists to prevent.

**Pasting a tuner's output.** Each tuner's generated block starts
`public static <Type> <field> = ...`. Paste it over the field of the same name in
**the file of the robot you ran the tuner on**, **exactly as the tuner shows it**
— name lines included, nothing to delete. Device names are the same on every
robot and are set once, from `DeviceNames`, in `RobotConstants`, whatever the
robot file says. `PedroRobotsTest` fails the build only if a pasted name
*differs* from `DeviceNames`, which means the tuner was run with a mistyped name
and its result should not be trusted. The Foresight imports (`Controller`, `Matrix`, `Vector2D`) are already
in each robot file, so that paste compiles as-is.

**Adding a robot with a drivetrain** (a test mule, a spare chassis) is the two
edits under [Changing ports, and adding a robot](#changing-ports-and-adding-a-robot),
plus **one file and one line**: copy `Robot19429.java` to `Robot<name>.java` in
`pedro/robots/` (Android Studio renames the class references for you), and add
`ROBOTS.put(RobotIdentity.<NAME>, Robot<name>::constants);` to `Constants`.
Skip either and CI fails: `ConstantsTest` requires values for every identity whose
device list includes a drive motor or the Pinpoint, and `PedroRobotsTest` rejects
a robot file no entry points at.

**What the tests pin.** Both competition robots resolve to their own file and to
*different* config objects (a copied file still pointing at the other robot's
fields passes everything else); `LAUNCHER_RIG` is refused by name; every
drivetrain robot has values; every robot carries the shared device names; and
each untuned robot's `createAlgorithm` still fails with the robot and file in
its message.

**Rejected alternatives.**

- *Both robots' blocks side by side in `Constants.java`* (`drivetrainConfig19429`
  …). The tuners emit `drivetrainConfig`, so every paste would need a hand rename,
  and pasting into the wrong-numbered block is a one-character mistake nobody sees.
- *Only per-robot differences, as overrides on a shared base.* Smaller files, but a
  tuner's block can no longer be pasted whole, and "what is this robot actually
  running?" takes two files to answer.
- *A method on `RobotIdentity` returning the configs.* Puts Pedro types into
  `hardware/`, which is deliberately free of them so it stays unit testable.
- *Name lines banned from robot files* (what #89 first shipped). It made every
  Mecanum and Pinpoint paste a two-step job — paste, then find and delete the
  name lines — and a missed deletion turned CI red for no real reason. That is
  the wrong trade on a robot at a meeting. Since #119 the lines may stay, and
  the test checks them against `DeviceNames` instead of rejecting them, so the
  drift the ban was meant to stop is still caught.

### Reaching AutoTune

There is no OpMode to select. AutoTune's `Hooks` class starts a web server when
the Robot Controller's event loop initializes, so it is running as soon as the
app is: connect to the robot's wifi and open **`http://192.168.43.1:10158`**
(`192.168.49.1` if the RC is a phone rather than a Control Hub). The procedures
registered in `Tuning.java` are listed there.

Run the tuners in this order; each produces the values the next one needs.
**Mecanum Tuner → Pinpoint Tuner → Foresight Tuner → Tests**, on **each** robot.
Each ends on a page of generated Java to paste over the field of the same name
in *that robot's* file in `pedro/robots/` — see
[One file of tuned values per robot](#one-file-of-tuned-values-per-robot). Check
which configuration is active before you start: it decides both which robot
the tuner drives with and which file's values it starts from.

### Foresight cannot be configured off the robot

Each robot file's `foresightConfig` is intentionally an empty lambda. Twelve of `ForesightConfig`'s
variables are declared `ConfigVar.required(…)` with **no default** —
`headingFeedback`, `forwardTranslational`, `strafeTranslational`, `brake`,
`coast`, the linear/quadratic/heading brake coefficients, both
`maxAchievable*Velocity` and both `natural*Deceleration`. Reading an unset one
throws `IllegalStateException("Config variable has not been set")`.

Those twelve are exactly what the Foresight Tuner measures. There is no
"defaults for now" option and nothing here should be guessed: they are
properties of one robot's mass, wheels and battery. `Constants.createAlgorithm()`
probes one of them and fails at init with a message naming the robot and the
file to paste into, because the
bare library error surfaces partway through following a path with no field name
and no hint.

The drivetrain and localizer configs are complete, so the Mecanum, Pinpoint and
Foresight tuners all run today, as do the Tests procedure's localization,
odometry, pose and driving tests. Only its hold, line and curve tests need a
tuned Foresight, since only those build a `Follower`.

### When the robot strafes in an arc: Drive Motor Check

The Foresight Tuner's **Max Strafe Velocity** step sends full power straight
left, robot-centric, with nothing correcting heading. So it shows the robot's own
strafe, and a robot that arcs there (a "rainbow") has a mechanical problem, not a
tuning one. Two things hide it in everyday driving. Basic Drive runs at 60%, and
it is **field-centric** by default, so the Pinpoint heading quietly steers out
the drift. To see what the tuner sees, open Basic Drive, press **B** for
robot-centric, hold the **right bumper** (turbo) and push straight left.

If it arcs there too, run **Drive Motor Check** (Diagnostics group, #121) with
**the robot lifted**. It takes about 20 s:

| Step | What it shows |
|---|---|
| Init: press A, then turn each wheel exactly one turn by hand | Encoder counts per turn. Their sizes should match (the sign can differ on reversed motors). One that differs has a **different gearbox**, the one fault the RPM steps cannot see: goBILDA's encoder sits before the gearbox |
| Each wheel alone, full power | Its own top speed (motor-shaft RPM) and current |
| All four: forward, strafe left, strafe right | The same Pedro `Mecanum` call the tuner makes, using this robot's motor directions |

After each step it names any wheel that is off. Every wheel's RPM is also on
Panels as `<wheel>_rpm`, so you can graph them.

| It says | Measured | Likely cause |
|---|---|---|
| `SLOW_DRAG` | ≥5% slower **and** ≥1.25× the others' current | Something resists it: bearing, rubbing wheel, bent shaft |
| `SLOW_WEAK` | ≥5% slower, normal current | Worn motor or gearbox. Swap it |
| `WRONG_WAY` | Spins against its command | Direction in that robot's `pedro/robots` file, or motor wiring |
| `NO_ENCODER` | ~0 RPM while commanded | Encoder cable unplugged. Driving is unaffected, but the wheel isn't measured |

The thresholds, power and timings are `@Configurable` under `DriveMotorCheck` in
Panels. **If every step comes back clean,** the cause is one a lifted robot can't
show: mecanum rollers on the wrong corners (seen from above they should make an
**X** pointing at the centre), weight balance front to back, or the floor.

**Rejected: running it on the floor.** Loaded numbers would be closer to the real
arc, but at full power the robot covers several feet per step. Lifted, a dragging
wheel still draws more current and a weak motor still runs slower, and that is
what separates the causes.

## The `shooter` package

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/shooter/` is a flywheel
speed test rig: spin the three launcher wheels up, read the RPM, and tune the
gains. It registers one OpMode, **Flywheel Speed Test** (TeleOp, group
`Shooter`).

It exists to characterize shooting speeds on **last season's DECODE robot**
before BIOBUZZ hardware is ready, so it is deliberately standalone — no
drivetrain, no pathing, no intake, no feeder, and no dependency on
Pedro's tuned values. Point it at a robot with the three
launcher motors in its config and it runs.

### Zero recompiles

The whole point is that a student meeting never has to touch Android Studio.
Everything numeric is a live Panels field under `FlywheelBank → config`:

| Group | What's in it |
|---|---|
| `measurement` | `ticksPerRev`, `gearRatio` — how encoder ticks become RPM |
| `target` | `targetRpm`, `maxRpm` ceiling, and the two gamepad nudge steps |
| `readiness` | `rpmToleranceRpm` (to become READY), `dropOutToleranceRpm` (to stop being READY — wider, so jitter does not flicker the state), `atSpeedHoldMs` |
| `voltageCompensation` | `enabled`, `nominalVoltage`, `minVoltage` |
| `left` / `center` / `right` | `enabled`, `reversed`, `rpmTrim`, `kS`, `kV`, `kP` |

The motor names are deliberately **not** in that tree. They live in
`DeviceNames`, and `RobotConfigXmlTest` holds them to `robot_*.xml`, so a wrong
name is a failed build rather than something to discover at a meeting. An
earlier version of this rig carried an editable `motorName` ported forward from
DECODE — the very pattern `DeviceNames` exists to end.

On the gamepad: **A** spins up, **B** stops, **dpad up/down** moves the target
by the fine step, **dpad left/right** by the coarse step. The Driver Station
shows the same lane table as Panels, so the rig is still usable with no laptop
connected.

### What it publishes, and where

> **History note.** This said *three* places, the third being an FTC Dashboard
> packet stream keyed `shooter/<lane>/…` that desktop AdvantageScope read as a
> browsable tree. Dashboard is no longer installed — see [Why FTC Dashboard is
> not in the dependency set](#why-ftc-dashboard-is-not-in-the-dependency-set).
> **No measurement was lost**: the four series only Dashboard carried are now
> published to Panels alongside the rest. The keys are flat and prefixed
> `left_` / `center_` / `right_` rather than slash-delimited, because Panels
> does not render a tree.

Two places at once, and you can use either alone:

| Where | What you get |
|---|---|
| **Panels** | Every series below as a graphable trace, plus the lane table as text |
| **Driver Station** | The lane table — works with no laptop at all |

Per lane, prefixed `left_`, `center_`, `right_`:

| Key | Why you care |
|---|---|
| `_rpm` / `_target` / `_error` | The basic picture. Plot measured against target. |
| `_tps` | Raw ticks/sec, before the ticks-per-rev maths — check here first if RPM looks wrong by a constant factor |
| `_power` | What actually reached the motor, after voltage compensation and clipping |
| `_ff` / `_fb` | **The tuning signal.** See below. |
| `_amps` / `_watts` | Load. A binding wheel or over-tight belt shows here long before it shows as a speed you can't hold. |
| `_at_speed` / `_spin_up_ms` | Readiness, and time-to-first-in-tolerance |

Plus `battery_volts`, `voltage_multiplier`, `loop_ms` and `spinning`.

### Reading the feedforward / feedback split

This is the part worth understanding, because it turns tuning from guesswork
into reading a graph.

The control law is `power = (kS + kV × target) + kP × error`. The rig publishes
those two halves separately:

- **`_ff` (feedforward) should be carrying nearly all the power** once the wheel
  is at speed. That is the whole point of feedforward — it predicts the power
  needed rather than reacting to being wrong.
- **`_fb` (feedback) should settle near zero.** If it is doing real work at
  steady state, **kV is wrong**, and kP is quietly papering over it. Fix kV
  rather than raising kP.

At steady state, kV ≈ `power_feedforward` ÷ `target_rpm` — both numbers are on
the graph, so you can read the correct value off directly instead of bisecting.

One trap: `shooter/voltage_multiplier` scales the applied power. Derive kV from
`power_feedforward` (before compensation), not from `power_applied` (after), or
your answer is off by exactly that factor.

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
4. **Is any lane showing a negative RPM?** Look at the wheel before touching
   anything. Two different faults read the same:
   - **The wheel spins the wrong way** → tick `reversed`. That flips the
     motor and its encoder together, so the RPM goes positive. (DECODE's
     robot 20245 ran its right lane reversed; 19429 ran all three forward.)
   - **The wheel spins the right way** → tick `encoderReversed`. The encoder
     counts backwards relative to its own motor, so `reversed` cannot fix it:
     the RPM stays negative whichever way that box is set. Seen on the right
     lane, 26 Sep 2026, where it read about -4000.

   Until the sign is right, the lane runs on feedforward only. Before that
   guard existed, a negative reading made the feedback term demand ever more
   power and the wheel ran away to full speed.
   **If ticking it changes nothing,** check the lane's `dir` column (and the
   `<lane>_reversed` graph key). That is the direction the running code
   actually applied. If Panels says `reversed` is ticked but `dir` still says
   `FWD`, the edit landed in a copy of the config that this OpMode does not
   read. The suspected cause is Sloth Load: Panels can end up holding fields
   from both the installed APK's classes and the hot-reloaded ones, and editing
   the stale set. The cure is a full **TeamCode** install, a power-cycle, and a
   reload of the Panels page. Seen 26 Sep 2026 on the right lane; the cause was
   inferred from the Panels `configurables-0.3.2+1.0.5` bytecode (it keeps fields
   per class loader and groups them by class name for the browser), not yet
   confirmed on the robot.
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

- Limelight in the hardware map as `DeviceNames.LIMELIGHT` (`limelight`), which
  both competition robots' `robot_*.xml` declare. See
  [The Limelight: an Ethernet device](#the-limelight-an-ethernet-device-not-a-hub-device).
  Check **Link: ok** before reading anything else.
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
   > **Update:** built. The estimator is Pedro 3's own `FusionLocalizer` (it ships in
   > `core`); the `localization` package adds only `CellFix` — a settled CELL sighting
   > plus the Pinpoint heading gives the robot position — and `HiveFieldPoints`, the
   > eight row-centre points, NaN until item 2 fills them. No tag orientation convention
   > is needed. Until item 2, the robot runs on the Pinpoint alone. The gate against
   > odometry described above was deliberately left to Pedro's filter: add one only if a
   > robot shows it is needed.
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


## AdvantageScope

> **Superseded, 24 Sep 2026 — read this first.** This section describes running
> AdvantageScope on your laptop against FTC Dashboard's packet stream. **FTC
> Dashboard is no longer installed**, so that stream does not exist and none of
> the instructions below currently work. See [Why FTC Dashboard is not in the
> dependency set](#why-ftc-dashboard-is-not-in-the-dependency-set). Getting
> The section is kept because the reasoning in it is still correct about
> everything except whether the data path exists.
>
> **Settled, 25 Sep 2026.** This banner previously said getting AdvantageScope
> back "is wanted and is tracked separately". It is not being got back. The
> stack is Sloth + Panels + Pedro + Ivy, and Panels' Graph and Capture views do
> this job. See [The Panels stack](#the-panels-stack-seeing-data-and-changing-values-at-a-meeting).
>
> **History note.** From #38 until #56 this section told you to open AdvantageScope
> **on the robot** at `http://192.168.43.1:8080/as/`, via the
> `page.j5155.AdvantageScope:lite` dependency. That dependency wants port 8080,
> which FTC Dashboard already binds — see below. It is gone. AdvantageScope still
> works; it runs on your laptop instead.
>
> **Correction, same day.** #56 was first written up as the *cause* of a Control
> Hub that would not start. It was not. With the dependency removed and a full
> reinstall confirmed on the hub, the Robot Controller kept dying in the same
> `BindException` restart loop. Two NanoHTTPD servers wanting 8080 is a real
> conflict and the removal stands on that alone — but it did not fix the outage,
> and the cause of that outage was still open when this was written. Do not read
> the section below as a closed case.

Run AdvantageScope on a laptop and point it at the robot's **FTC Dashboard**
stream. It is not automatic — you have to tell it where the robot is and which
kind of source it is:

1. In AdvantageScope's preferences, set the **robot address** to `192.168.43.1`
   (the Control Hub) and the **live source** to **FTC Dashboard**.
2. **File → Connect to Robot.**

Once connected, anything published as a `TelemetryPacket` shows up — Pedro
Pathing's output and the whole `shooter/` tree included.

> Nobody on the team has run this end to end yet, so treat the menu names as
> approximate. If your build words them differently, correct this list rather
> than working around it.

Keys are slash-delimited on purpose: AdvantageScope renders `shooter/left/…` as a
browsable tree. Publish flat keys and you get thirty loose series instead of one
node per lane.

Field assets are a one-time download from Mechanical-Advantage's
[AdvantageScopeAssets](https://github.com/Mechanical-Advantage/AdvantageScopeAssets/releases/tag/bundles-v1)
(`AllAssetsDefaultFTC.zip`, refreshed 2026-09-12). **It does contain the BIOBUZZ
field** — `Field2d_20262027FTCFieldV1` and `Field3d_20262027FTCFieldV1`, declaring
`"isFTC": true`, `"coordinateSystem": "center-rotated"` and a 143.182-inch square.
DECODE and 2024-2025 are in there too.

It also replays logs: **File → Open Logs** reads Road Runner and PsiKit logs,
including ones recorded before you installed it. Worth knowing for
characterization — nobody reads a spin-up curve well while three wheels are
spinning next to them.

### Game pieces and the HIVE in a simulated `.wpilog`

The desk-check log `SimulatedMatchLogTest` writes (`TeamCode/build/sim-logs/sim-match.wpilog`,
from the #112 logging work) also simulates the game (#140). Every POLLEN and NECTAR moves under
physics: launched in arcs, bouncing off tiles, walls, the robot and each other, and rolling to the
back of a raised CELL. Both HIVE rockers tip when enough pieces land in their raised CELL, and the
CELL that goes down spills what is in it. The simulated robot picks up real pieces, shoots into its
raised CELL, and drives round to the other side when the HIVE tips. It runs in the test sources
only and never on a robot, so it costs no loop time. Open it in desktop AdvantageScope **27.0.0-alpha-6
or later**.

**Where it comes from.** The approach is FuelSim's (FRC Team 5000, MIT licence), as Wavelength 3572
used it in FRC 2026 (`wavelength3572/Robot-2026`, `util/FuelSim`): point-mass spheres, gravity,
restitution and friction in fixed sub-steps, the robot as a moving box and the intake as a capture
zone. The HIVE is ours. Each rocker is two open-ended boxes on an axle, built from `HiveGeometry`
(the Competition Manual) and checked against AdvantageScope's field CAD; they agree to about 0.1 in,
and the CAD's axle bearings sit exactly at the manual's 43.95 in. The code is `FieldSim`
(physics), `SimDriver` (the robot's choices) and `SimulatedMatch` (the log).

**Calibrated the way FIRST calibrates a real HIVE.** Field staff tune every HIVE at an event with
ballast washers until it passes the table in the 2026-2027 *Event Field Setup Guide* V1.0, §12.3
([PDF](https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide)). Pieces are gently
placed against the CELL's back skin:

| HIVE state | Test | Result | |
|---|---|---|---|
| 3 NECTAR + 1 POLLEN | 2nd POLLEN tossed in | no tip | necessary |
| 3 NECTAR + 2 POLLEN | 3rd POLLEN gently placed | tip | preferred |
| 3 NECTAR + 2 POLLEN | 3rd POLLEN tossed in | tip | necessary |
| 0 NECTAR + 6 POLLEN | 7th POLLEN tossed in | no tip | necessary |
| 0 NECTAR + 7 POLLEN | 8th POLLEN gently placed | tip | preferred |
| 0 NECTAR + 7 POLLEN | 8th POLLEN tossed in | tip | necessary |

A match starts with 3 NECTAR in each upward CELL (§11.1), so the 3rd POLLEN tips it. Once it has
tipped and emptied, it takes about 8 to tip it back. `HiveCalibration` holds that table, and
AndyMark's piece weights (POLLEN 0.055 lb, NECTAR 0.091 lb). The simulation fits itself to them by
running FIRST's procedure: with the rocker held, it places each case's pieces against the back skin
and reads the resting torque one POLLEN short and at the count. The holding torque goes in the
middle of the range that satisfies every case: 7 POLLEN must hold at 67.8 POLLEN-inches, 3 NECTAR
+ 3 POLLEN must tip at 75.1, and the fit puts it at 71.5. `HiveCalibrationTest` runs all six rows.

Two things are not published, so they are ours to measure. Each is NaN until measured, and the log's
**Calibration** metadata says which are still assumed:

- [ ] **Tip time** → `HiveTracker.Tuning.tipSeconds`, the robot's own value (HIVE lesson 3). Film a
      tip in slow motion 3 times, from first movement to resting, and average. Until then: 1.0 s.
      The fit scales the swing speed so a simulated tip takes exactly this long.
- [ ] **Bounce** → `HiveCalibration.MEASURED_DROP_IN`, `MEASURED_REBOUND_IN`. Drop a POLLEN onto the
      tiles from 40 in, measured to the ball's bottom, beside a tape measure. Film the first bounce and
      read its top, also to the ball's bottom. Do it 3 times. Until then: restitution 0.35.

Friction and the simulated robot stay `PLACEHOLDER_` constants in `FieldSim`. An event's HIVE that
is calibrated off-spec will tip differently; the simulation is the HIVE as §12.3 says it should be.

**One-time setup: the HIVE assets.** AdvantageScope fields have no moving parts, but robots can
have articulated components (docs: *Custom Assets → Articulated Components*). So the HIVE is drawn
as a "robot" whose base is the frame and whose two components are the red and blue rockers,
standing on a copy of the field with those three parts removed. `HiveAssets` cuts both out of the
stock field AdvantageScope already downloaded. No FIRST CAD is committed.

1. Find the stock field: in AdvantageScope's settings folder, open `autoAssets` and find the
   `Field3d_…` folder whose `config.json` says `"name": "2026-2027 Field"`. **Show Assets
   Folder** in the app menu (**AdvantageScope** on a Mac, **App** elsewhere) opens the
   `userAssets` folder next to it.
2. Build:
   ```
   BIOBUZZ_FIELD3D=/path/to/that/Field3d_folder ./gradlew :TeamCode:testDebugUnitTest --tests '*HiveAssetsTest*'
   ```
   It writes `TeamCode/build/advantagescope/Field3d_BIOBUZZHiveSim` and `Robot_BIOBUZZHive`.
   On Windows (PowerShell), where AdvantageScope keeps `autoAssets` and `userAssets` under
   `%APPDATA%\AdvantageScope`:
   ```
   Get-ChildItem "$env:APPDATA\AdvantageScope\autoAssets" -Recurse -Filter config.json |
     Select-String '2026-2027 Field' | Select-Object -ExpandProperty Path
   $env:BIOBUZZ_FIELD3D = "<the folder holding that config.json>"
   .\gradlew.bat :TeamCode:testDebugUnitTest --tests "*HiveAssetsTest*" -i
   Copy-Item -Recurse -Force TeamCode\build\advantagescope\Field3d_BIOBUZZHiveSim, TeamCode\build\advantagescope\Robot_BIOBUZZHive "$env:APPDATA\AdvantageScope\userAssets\"
   ```
   Without `BIOBUZZ_FIELD3D` the test skips itself and the build still says it succeeded; look for
   the `Wrote …` line.
3. Copy both folders into `userAssets` and restart AdvantageScope. A field called
   **2026-2027 Field (HIVE sim)** and a robot called **BIOBUZZ HIVE** appear.

**One-time setup: our robot, with its Limelight.** `RobotAssets` draws a simple robot (chassis, an
orange bar on the front, the Limelight on a post with a green rod along where it looks) and declares
the Limelight as an AdvantageScope *fixed camera*, both from `CameraMount`'s measured numbers. No
download needed:

```
./gradlew :TeamCode:testDebugUnitTest --tests '*RobotAssetsTest*'
```

Copy `TeamCode/build/advantagescope/Robot_BIOBUZZ` into `userAssets` and restart AdvantageScope. A
robot called **BIOBUZZ Robot** appears. **Rebuild and recopy it whenever `CameraMount` changes**:
the folder holds the numbers it was built with, not live ones, and a value changed only in Panels
never reaches it.

- **See where the camera is:** pick **BIOBUZZ Robot** as the model for `/Odometry/Robot3d`
  (the robot row's model dropdown).
- **Look through it:** right-click the 3D view and choose **Limelight**. The view sits at the lens,
  aimed as mounted, at the Limelight 3A's 54.5° field of view and 4:3 shape, and follows the logged
  pose. Right-click → **Orbit Field** to get back. The menu lists the cameras of the **first Robot
  row** only. The HIVE (`/Sim/Hive/Structure`) is also a Robot row, with no cameras, so if it sits
  above `/Odometry/Robot3d` the menu has no **Limelight**. Keep `Robot3d` at the top; the layout
  file below already does. With the HIVE assets loaded, a CELL's tags should
  be in frame wherever the robot really saw them; if they are not, suspect the mount numbers or the
  pose before the tag code.

**Side walls.** The same build also writes `Robot_BIOBUZZWalls` (**BIOBUZZ Robot (side walls)**):
an 18 in square robot whose side walls slide forward 6 in, making it 24 in long (R105 allows
18 × 24 in, 29 in tall). Each wall has a one-way flap at the bottom; the opening is 3.2 in high, so
POLLEN (2.8 in) rolls in under it and NECTAR (3.6 in) is stopped. The two walls are the model's
articulated components, so a simulated log slides them out and in. Copy it to `userAssets` with
the robot. Anything trapped by the walls is CONTROLLED (the manual's definition: "stuck in, on, or
under the ROBOT", and herding counts too), so G407's limit of 4 counts it together with whatever
the robot already holds.

In the simulator the walls are a robot design, **spring hood, full-width intake, side walls**
(`RobotDesign.sideWallsSlideIn`), and the robot runs them itself, not the Auto (`AutoSim`
`sideWalls`): out when our CELL starts to TIP (`HiveTracker.tipsStarted()` on the real robot)
while the robot is waiting near the HIVE, so they are out before the spill lands about 1.15 s
later; in once it holds 4, when it drives off, or 4 s later. They take 0.3 s to slide, a guess. The
log has `/SideWalls/Out` (0 in, 1 out) and `/SideWalls/Components`. To watch: **File → Import
Layout** with `sim-review/advantagescope-layout-walls.json`, which is the usual layout with our robot
drawn as **BIOBUZZ Robot (side walls)** and its walls moving.

**Which shape (`SideWallSpillTest.whichShapeGathersTheSpill`, 5 Oct 2026).** R105 lets the robot grow to
18 × 24 in after the start, so a U can be long (arms slide 6 in forward, 18 in mouth) or wide (wings
at the front corners swing 3 in out each side, 24 in mouth, nothing forward), not both
(`RobotDesign.checked` refuses a design that is). Each parked with its front-most point 35 in from the
wall, short of the landing, walls out as the TIP starts; the spill gathered within 24 in across, from
15 in behind that point to 8 in past it, 3 s later. 40 TIPs each, no G409 touches in any:

| Shape | Standing | Creeping 8 in once the spill is on the tiles |
|---|---|---|
| No walls | 13% (0.8 a TIP) | 18% (1.1) |
| **Long U** | **33% (2.0)** | **39% (2.3)** |
| Wide U | 14% (0.8) | 19% (1.2) |

The arms that reach forward are what stop the spill scattering sideways from just ahead of the robot;
a wider mouth at the frame's front adds almost nothing. With the creep the long U held 4 at most at
once, G407's limit: count what the robot already holds before building it bigger.

Every simulated run now also counts **G409**: a spilled piece a robot touched before it touched
anything else (the tiles, a wall, the HIVE's feet, another robot, or a piece that already had).
Each is a `sim: G409: ...` event on the Console and `g409` in `result.json`.

`SideWallSpillTest` tries the walls in the simulation: the same TIP as `SpillLandingTest`, with a
robot parked short of the landing, facing the HIVE, walls out, over 20 TIPs at three parking spots.
With the filmed spill (pieces fan out in every direction where they land), the walls help: with the
robot centred 20 in from the alliance wall, 31% of the spill ends within 6 in of it, against 17% for
the same robot without walls and 13% with no robot, and nothing touches it before the tiles. At 25 in
it is 36%, but 7 of 120 pieces hit a wall before the tiles, which G409 forbids: park so the walls stop
short of where pieces land. How pieces roll after landing is the least measured part of the
simulation (friction is still a placeholder), so treat this as a reason to try cardboard walls at a
meeting, not a verdict.

**Opening the log** (a 3D Field tab):

| Drag this key | As |
|---|---|
| *(field dropdown)* | **2026-2027 Field (HIVE sim)** |
| `/Odometry/Robot3d` | Robot, model **BIOBUZZ Robot** |
| `/Sim/Hive/Structure` | Robot, model **BIOBUZZ HIVE** |
| `/Sim/Hive/Components` | onto that robot, as **Component** |
| `/Sim/GamePieces/Pollen`, `/Sim/GamePieces/Held/Pollen` | Game Piece, **Pollen** |
| `/Sim/GamePieces/RedNectar`, `…/Held/RedNectar` | Game Piece, **Nectar (Red)** |
| `/Sim/GamePieces/BlueNectar`, `…/Held/BlueNectar` | Game Piece, **Nectar (Blue)** |
| `/Sim/Shot/Trajectory` | Trajectory |

Two things trip people up. Pick **2026-2027 Field (HIVE sim)** in the field dropdown; the stock
field still has its own fixed HIVE, so you see two. And drop `/Sim/Hive/Components` *onto the
`/Sim/Hive/Structure` row* (it indents under it); dropped anywhere else, AdvantageScope offers only
game-piece types. Or skip the dragging: **File → Import Layout** with `sim-review/advantagescope-layout.json`
(saved from AdvantageScope 27.0.0-alpha-6) sets up all of it.

As soon as any game piece is shown, AdvantageScope hides the field's own staged pieces, so
the logged ones replace them rather than doubling them. Without component poses the rockers are
drawn as built, which is the match start. `/Sim/Hive/Red/State`, `…/Tips`, `…/AngleDeg` and
`…/RaisedCellPieces` graph the same story, and every score, spill and tip is on the Console
(`sim: …`).

**Frames.** Everything is simulated in Pedro inches and converted only as it is logged, through
`AdvantageScopeFrame`. A rocker's component pose is a turn about Center/Rotated's y axis (Pedro's
x) through the axle point `(0, 0, 43.95 in)`. The field model's `(x, y, z)` lands at Center/Rotated
`(z, x, y)`, and the asset builder bakes that into the robot models' root node so the robot config
needs no rotations.

### Simulating an Auto

> **Where things stand (3 Oct 2026).** The Autos still worth running, what each needs and what it
> scores are in `tools/auto-routes/README.md`, with a link that opens each one in the Visualizer;
> their `.pp` files are in `TeamCode/autos/`. `ReviewPackageTest` builds them into a review package
> to watch in AdvantageScope. Ideas that lost, and the study tests that ran them
> (`AllianceAutoTest`, `BestAutosTest`, `DecisionStudyTest`, `RobustnessTest`, `SpillStudyTest`),
> are archived: see `tools/auto-routes/experiments/README.md`. The sections below keep the history
> of how we got here, so some of what they name is now only in git history.
>
> **The spill was filmed on 3 Oct 2026, and it changed the answer**: see "The spill, filmed" below.
> Everything written before it that catches or sweeps a spill at the wall assumed physics the films
> disproved.

`AutoSimTest` runs every Auto the Auto Builder has exported (each class in
`opmodes/auto/generated` with a `SOURCE`) against the same simulated field, for both alliances. It
writes one log per run to `TeamCode/build/sim-logs/auto-<name>-<alliance>.wpilog`:

```
./gradlew :TeamCode:testDebugUnitTest --tests '*AutoSimTest*'
```

It prints one line per run, e.g. `right-start-tip RED: launched 8, scored 8, HIVE tipped at 3.3 s;
finished at 12.2 s`. Open the log exactly as above. The Auto's own decisions are on the Console as
`auto: …` (which branch of a "wait for" won, and when), next to every shot, score, spill and tip.

**What is real and what is simulated.** The Auto is the generated class itself, built by its own
`build(kit, rotated)` and run through autokit and Ivy's real `Scheduler`, as `BuiltAuto` runs it on
the robot. The other alliance runs it rotated, as on the robot. Underneath, `AutoSim` simulates:

- **Driving.** Each path is Pedro's own `Path`, driven along its length on a rest-to-rest
  trapezoidal profile (40 in/s, 30 in/s²; `AutoSim.speed` changes it). Turning is rate-limited too, so
  a path ends when the robot is there and facing its way. The real follower carries speed through
  joins, so real timing differs a little.
- **The robot** is a `RobotDesign`: where its intake is and how wide, what kind of launcher, how fast
  each mechanism works. The default is a front intake and a turret. It starts holding its 4
  preloaded POLLEN (Competition Manual §10.3.4) and never holds more than 4 (G407). Its intake runs
  whenever there is room, unless the Auto calls `IntakeOff`, and takes one piece at a time. Out of a
  FLOWER it can take only the bottom POLLEN, through the 3.55 in retrieval opening (§9.7, G418), and
  each one takes time to drag out. `LaunchAll`/`ShootAll` spin up (1 s) and fire everything held at the
  alliance's raised CELL; `LaunchOne` fires one. A turret aims from wherever the robot is; a launcher
  fixed to the frame waits for the drivetrain to turn the robot to face the CELL. None of the three
  commands is in `AutoRegistration` yet: they are the names the launcher's commands should take when
  it has them. Whether a shot goes in is the physics' business: from beside the HIVE it hits a
  CELL's closed side.
- **The rest of the field**, when a run asks for it: a partner that stands still with its 4 POLLEN
  staged on the tiles beside it (`AutoSim.partner`), and the drive team entering one NECTAR into the
  LOADING ZONE 2 s after each TIP (`AutoSim.humanNectar`, G426/G427).
- **Time.** The log runs 8 s past AUTO, through the transition before TELEOP: a TIP that completes
  then still counts for AUTO (§10.5 B), so a shot launched just before 30 s can still earn its TIP.
- **Triggers.** `IntakeFull`, `LauncherReady`, `Tip` (the alliance's HIVE has started to tip since
  the wait began, like `HiveTracker.tipsStarted()`, but from the simulation's truth instead of a
  camera), `HiveTipped`, and `CameraBlind` (always false).

An Auto that uses a command or trigger the simulation does not know fails at once and names it, as
`BuiltAuto` does at INIT. Add the name to `AutoSim.registry()` with what it should do.

### Simulating from the Visualizer ("Save to GitHub")

No setup on a laptop: open an Auto in the Visualizer from a link that names its whole path, e.g.
`…/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/recycle5-left.pp`,
change it, and press **Save to GitHub** (the cloud button). One commit on that branch carries the
`.pp`, its generated Java and `TeamCode/sim-request.json` (partner Auto, robot designs, seeds,
alliance). The **Simulate Auto** workflow runs the request through `SimRunTest` and publishes on the
`sim-results` branch `<auto>/<commit>/result.json` and the median seed's `.wpilog`; the dialog shows
every seed and has **Download WPILOG**. Without a token the dialog still shows the last result and
downloads its log; saving needs a fine-grained token (Contents: read and write, Actions: read on
biobuzz), which the dialog explains. `sim-results` is rewritten each run and keeps the newest 40.

Every simulated log names what it simulated on its Metadata tab (`SourceCommit`, `SourcePath`,
`SimSpec`, `SimDesign`, `SimSeed`, `SimRun`), so it can be matched with the real match log of the
same Auto. Locally, `SimRunTest` runs one request from a folder: see its Javadoc.

### Solo Autos

Four Autos for a robot working its HIVE alone, drawn in the Auto Builder and tried in the
simulation. Their sources are in `src/test/resources/auto-builder/`; open them in the editor to
see and change the routes. All start at (59, 9.5) facing the HIVE. They open by launching 3 of the 4
preloads one at a time, which with the 3 NECTAR already in the CELL tips the HIVE (§12.3). The
three-tip Autos ask whether each TIP happened and recover if not. `SoloAutosTest` checks that each
still does what its name says.

| Auto | Route | 2nd TIP | 3rd TIP |
|---|---|---|---|
| **solo-two-tip** | wall FLOWER and far FLOWER, shoot from the north; then the GARDEN, and park | 16 s | — |
| **solo-three-tip** | as solo-two-tip; the 3rd TIP from NECTAR TIP 1 spilled plus the GARDEN | 16 s | 28 s |
| **spill-three-tip** | waits for TIP 1's spill to roll into its intake, so it needs one FLOWER, not two | 17 s | 30 s |
| **partner-three-tip** | as spill-three-tip, with a partner's staged POLLEN in place of the far FLOWER | 18 s | 31 s |

Times are at 50 in/s with the standard design. A three-tip Auto needs a drivetrain that fast: at
40 in/s its last volley comes after 30 s. solo-two-tip works at 40 in/s, ends parked in the
LOADING ZONE, and is the one to try first. The three-tip Autos use the whole 30 s and do not park:
a third TIP is worth 20, an AUTO PARK 5. On the robot we would actually build, and with real
partners, see "Autos on the spring-hood robot" below: it adds **three-tip-adaptive**, which this
table's routes now feed into.

Things the routes rely on, found by watching the logs:

- **Pieces get pushed.** A robot driving off its start pushes TIP 1's spill across the field, and a
  robot turning near staged POLLEN swats them away. spill-three-tip leaves along the wall to keep the
  spill in a row it can sweep later, and partner-three-tip comes at the partner's POLLEN straight on.
- **Two robots cannot overlap**, so a partner's POLLEN must sit where our intake can reach them
  head-on: in a row along the partner's front, not tucked against its side.

### Robot design questions

`DesignComparisonTest` runs these Autos on different robots: 10 runs each, at 50 in/s. It is slow,
so it runs only when asked:

```
BIOBUZZ_DESIGN_STUDY=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*DesignComparisonTest*' -i
```

Third TIPs out of 10, with the simulation's placeholder friction and shot spread:

| Design | solo-three-tip | spill-three-tip | partner-three-tip |
|---|---|---|---|
| Turret (the standard) | 10 at 28.4 s | 10 at 29.8 s | 10 at 31.1 s |
| Launcher fixed to the frame | 10 at 28.5 s | 10 at 29.9 s | 9 at 31.1 s |
| Two fixed launchers side by side | 10 at 25.3 s | 9 at 26.9 s | 9 at 28.2 s |
| Turret firing every 0.25 s, not 0.45 s | 10 at 25.9 s | 9 at 27.5 s | 9 at 28.7 s |
| Catapult (whole load at once) | 3 | 3 | 2 |
| Launches POLLEN only | 0 | 0 | 0 |
| Throws NECTAR 7% short | 9 | 1 | 1 |
| 1 s to pull each FLOWER POLLEN (not 0.5 s) | 0 | 10 | 10 |
| Intake 8 in wide (not 14 in) | 10 | 10 | 0 |

What that says, as far as a simulation full of guesses can:

- **Turret or fixed launcher: hardly matters for Autos.** Turning to face the CELL costs about 0.1 s
  over a whole Auto, because these shot spots already face the HIVE. A turret's real advantage, shooting while driving, is not simulated.
- **Shooting faster is worth about 3 s**, by two launchers or a quicker one. Those 3 s would be the
  margin for a missed shot or a slower drivetrain. Rapid volleys sometimes knock pieces back out of the
  CELL, though (9/10, and 5–7/10 with less friction); a real HIVE would settle that.
- **A catapult throws badly** *(superseded: see "Which results hold" below; at 60° it was firing
  from too close)*.
- **Launch NECTAR, and launch it as well as POLLEN.** No solo three-tip works without it: tip 3 is
  built from spilled NECTAR. The launcher has to handle both sizes (2.8 in and 3.6 in) and NECTAR's 1.65×
  weight. One that throws NECTAR short ruins the spill routes.
- **FLOWER pickup speed decides which route works.** solo-three-tip pulls 7 POLLEN from two FLOWERs and
  dies at 1 s each. spill-three-tip pulls 4 from one FLOWER and partner-three-tip none, and neither
  cares.
- **Intake width** matters only where pieces sit in a row, like a partner's staged POLLEN.
- **Partner POLLEN don't beat a FLOWER for time.** They are about a second slower than the far FLOWER,
  because of the careful approach. What they buy is independence from FLOWER pickup.
- **Every placeholder is a guess.** Times, spread and friction are `PLACEHOLDER_` constants and
  `RobotDesign` defaults, not measurements. With 1.5× the shot spread, or half the friction, the
  third-TIP counts drop: by one to three in ten for most designs, by up to five for the fast shooters.
  Treat the table as a comparison, not a prediction.

Rules the routes and designs lean on, from the Competition Manual:

- Size: 18 in cube at the start (R102); at most 18 × 24 × 29 in once moving (R105), so an intake
  can reach 6 in past an 18 in frame.
- A robot starts touching the wall, on its own side, out of the LOADING ZONE and clear of FLOWERs,
  touching its 4 preloads, which may sit on the tiles beside it (G304, §10.3.4). A partner can stage
  its preloads for us that way.
- POLLEN comes out of a FLOWER only from the bottom; NECTAR never does (G418). NECTAR goes into a FLOWER
  only in the last 60 s (G410), so not in AUTO.
- Spilled pieces must hit the tiles before a robot takes them, and waiting under the HIVE to catch
  them is a foul (G409).
- Each TIP lets the drive team enter one NECTAR into the LOADING ZONE (G426, G427). Nothing restricts
  that to TELEOP; check with the Q&A before an Auto counts on it. A run that picked it up for TIP 2
  made no difference: the runs that miss a TIP miss the third.
- A TIP that completes before TELEOP starts counts for AUTO (§10.5 B).

### Two robots

`AutoSim.alsoRun` puts the alliance's second robot on the same field, running its own exported
Auto at the same time. `AllianceAutoTest` runs the pair below and writes
`TeamCode/build/sim-logs/alliance-duo-<alliance>.wpilog`. The Console has both Autos' decisions
(`auto:` and `auto2:`), and the run says if the robots ever overlap or one reaches into the other
alliance's half (G402). Both are judged for LEAVE (off the wall) and AUTO PARK (partly in the
LOADING ZONE) when AUTO ends (§10.5.4). A partner that stands still (`AutoSim.partner`) is logged as
a robot too.

**Seeing both robots in AdvantageScope.** Every log with more than one robot uses the same keys
(`logging/FieldRobot`), and its Metadata tab says which robot is which:

| Robot | Pose (3D field) | Path being driven | Mechanisms |
|---|---|---|---|
| Ours | `/Odometry/Robot3d` | `/Path/Active` | at the root (`/Intake/On`) |
| Our partner | `/Odometry/Partner3d` | `/Path/Partner` | under `/Partner` |
| The other alliance's | `/Odometry/OpponentA3d`, `/Odometry/OpponentB3d` | `/Path/OpponentA` | under `/OpponentA` |
| All of them | `/Odometry/AllRobots3d` (one array) | | |

On the 3D Field tab, drag `/Odometry/AllRobots3d` onto the field as a Robot: every robot appears at
once. To tell them apart, drag `/Odometry/Robot3d` as a Robot and `/Odometry/Partner3d` as a Ghost
(pick its colour). On the 2D Field tab, `/Odometry/AllRobots` does the same. Save that as a layout
once and every duo log opens the same way.

**Two `.pp` files as one log, without the simulation.** A text file in
`src/test/resources/pedro-paths/together/` names `.pp` files (relative to `src/test/resources/`) that
run at the same time: our robot first, then our partner, then up to two opponents. Each robot drives
its own paths, as the Visualizer times them, from the moment AUTO starts.

```
./gradlew :TeamCode:testDebugUnitTest --tests '*VisualizerPathLogTest*'
```

writes `TeamCode/build/sim-logs/pedro-paths/together/<name>.wpilog`. This shows where two Autos'
robots are at each moment (do they cross?), not what the HIVE does: a wait for a trigger is drawn as
the Visualizer times it. `AutoSim` is the one that answers whether the plan works.

**duo-south + duo-north** (`src/test/resources/auto-builder/duo-*.pp`) split the HIVE between them:

1. duo-south starts where the solo Autos do and fires 3 preloads for TIP 1. duo-north starts mirrored
   against the north wall.
2. When the south CELL goes down, the north one is up. duo-north fires its 4 preloads, pulls 4 from
   the far FLOWER and fires again: TIP 2.
3. A robot waiting in front of its CELL gets about half of what that CELL spills rolled into its
   intake. So each robot, when its CELL comes up again, fires those, sweeps along the wall for the
   rest of the spill and fires again.
4. Both end parked side by side in the LOADING ZONE, off the wall, through the Auto Builder's park
   card.

They wait on `LeftCellUp` / `RightCellUp` (which CELL is up, as `HiveTracker` reports it) rather than
`Tip`, because a state can be waited for in short pieces (see the park below).

At 60 in/s, 10 runs: TIP 1 at 3.4 s, TIP 2 at 10.9 s, TIP 3 at 17.0 s in 8–9 of 10, and both robots
LEAVE and PARK in 18 of 20. That is about 70 AUTO points. A fourth TIP did not happen; with two fixed
launchers firing together it happened in 3 of 10, at 24.5 s.

Why not 5–7 TIPs in AUTO, as far as this simulation can say:

- **Supply, not shooting, is the limit.** A TIP takes about 8 POLLEN-weights, and a robot carries 4,
  so every TIP is two loads. Only about half of a spill rolls back into a waiting robot; the rest
  scatters, and the robot can sweep up only some of it in time. Faster launchers barely helped.
- **A CELL can only be loaded from its own side**, so one robot works each end and they take turns.
  That makes a TIP about every 6 s after the first: 3 TIPs reliably, a 4th if everything goes
  right, and then the robots have to leave time to park.
- **7 TIPs is the POLLINATOR 2 RP threshold for the whole match**, AUTO and TELEOP together.

Things the runs turned up:

- **The park has to fit the real robot.** The export times the park path at the Auto Builder's
  60 in/s. A robot that only does 50 in/s parked in 4 of 20 runs.
- **The endgame guard now cuts a running card short on the robot, as the preview does.** When
  these runs were made it checked only between cards, so a long "wait for the other robot" could
  run past the park deadline. In one run the guard fired at 27.2 s with 2.8 s left for a 3.3 s
  park. These Autos wait in pieces of at most 2.5 s to work round that. *History, 1 Oct 2026:*
  `AutoKit.guarded` now checks on every loop. It ends the running card (`INTERRUPTED`) once time
  left drops below the park time plus `GUARD_MARGIN_S`, then drives the park path. The 2.5 s
  pieces are no longer needed. They are left as they are so the numbers above still match.
- **Rapid volleys knock pieces back out.** A piece that hits the back of the CELL rebounds toward the
  opening and can meet the next one at the lip; the HIVE's bounce is a placeholder.
- **The SWARM RP does not need AUTO PARK.** It takes LEAVE + PARK points ≥ 16: both robots leaving
  (6) and parking at the end of the match (10) reach it too. AUTO PARK adds 5 a robot.

### Autos on the spring-hood robot

The Autos above were drawn for a generic robot with a turret. `RobotDesign.springHood()` is the one
in `cad/spring-hood-launcher/`: fixed to the frame, its pitch built in at 75°, NECTAR at 99% of
POLLEN's speed, and 2 s to spin four steel flywheels up from rest (one 6000 rpm motor; measure it).
`AutoStudyTest` runs any Autos on any designs, 10 runs each, and prints TIPs, AUTO points, how
often each robot parks, and the head start it leaves TELEOP:

```
BIOBUZZ_AUTO_STUDY="SoloTwoTipAuto@40;DuoLzSouthAuto,DuoLzNorthAuto@50" BIOBUZZ_AUTO_DESIGNS="spring hood" \
  ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*' -i
```

The new routes were written with `tools/auto-routes/` (a few lines of Python per route, exported
with the Auto Builder's own exporter); their `.pp` files are the source as usual.

**What AUTO can score.** Only TIPs (20), LEAVE (3) and AUTO PARK (5) score in AUTO (Table 10-2).
Pieces in a CELL, a FLOWER or the GARDEN count only at the end of the match. So an Auto with time
to spare can still do three things: get its last TIP to complete during the transition (a TIP
that completes before TELEOP counts for AUTO, §10.5 B), load the raised CELL so TELEOP's first TIP
comes sooner, and end parked holding pieces. Every run now reports that head start ("TELEOP
starts with the raised CELL 45% loaded and 2 held").

**Where it scores from.** `ShotMapTest` (opt in with `BIOBUZZ_SHOT_MAP=1`) fires POLLEN and
NECTAR at each raised CELL from every spot on our half where an 18 in robot fits. Each CELL scores
from a wedge in front of its opening: below about y = 37 (the south CELL) or above about y = 100
(the north), and the spring hood's 75° arc stretches each wedge along the west wall, to about
y = 53 and y = 85. So it can shoot the south CELL from the wall FLOWER, and the north CELL from inside the
LOADING ZONE (x 0–11, y 94–118). Both launchers score from about 83 of 330 spots; nothing scores
from beside the HIVE.

**The spring hood needs its speeds close.** With its pitch fixed, the speed window from the wall
spots is −5.5% to +4%, so one setting serves NECTAR/POLLEN ratios of 0.97 to 1.03 (angled spots:
0.92 to 1.08; `LauncherStudyTest`, arc −75). The model predicts 0.99. At 0.95 every Auto that starts
from the wall falls apart (duo-lz: 54 points instead of 80). The opening volleys are all preloaded
POLLEN, so the launcher can use a POLLEN setting for them; check the ratio on the bench before
trusting a mixed setting from the wall.

**Results, 10 runs each, spring hood unless stated:**

| Auto(s) | in/s | TIPs (runs) | AUTO points | Parked |
|---|---|---|---|---|
| solo-two-tip | 40 | 2 (10/10), TIP 2 at 18 s | 48 | 10/10; CELL 52% loaded |
| solo-three-tip | 50 | 3 (10/10), TIP 3 at 30 s | 63 | no |
| spill-three-tip | 50 | 3 (5/10) | 53 | no |
| **three-tip-adaptive** | 50 | 3 (10/10) | 63 | no |
| three-tip-adaptive + a partner that fires its preloads | 50 | 3 (10/10), TIP 3 at 26 s | 75 | 18/20 |
| duo-south + duo-north (the old pair) | 60 | 3 (8/10) | 70.5 | 13/20 |
| **duo-lz-south + duo-lz-north** | 50 | 3 (9/10), 4 (3/10) | 80 | 20/20 |
| duo-lz, two spring hoods on each robot | 60 | 3 (9/10), 4 (9/10) | 92 | 20/20 |

*History, 1 Oct 2026:* this table was run before the endgame guard cut running cards short (see
"Things the runs turned up" above). Solo **three-tip-adaptive** now gets 2 TIPs and parks. Its
third TIP came at about 30.5 s, during the TIP 3 volley, which runs inside the guard. The old guard
let that volley run past the park deadline. Rows that park by an ordinary path, outside a guard
(for example solo-three-tip), are unaffected. The other rows have not been re-run.

- **2 s of spin-up costs 1.2 s on TIP 1** (4.6 s instead of 3.4 s), and the three-tip routes have
  no slack for it: spill-three-tip and partner-three-tip lose their third TIP. A 3 s spin-up costs
  duo-lz 2 points. Spin up once and never down; whether the flywheel may spin before START is a
  question for the Q&A.
- **three-tip-adaptive** is solo-three-tip with one decision. After its far-FLOWER pickup it checks
  which CELL is up. If a partner already made TIP 2, it takes its 4 pieces south for an early TIP 3
  instead of throwing them at a CELL out of range, and parks. Alone it is solo-three-tip.
- **duo-lz** is duo-south + duo-north with a new end. Both robots finish by parking in the LOADING
  ZONE and firing from there at the north CELL: the LOADING ZONE is inside the north CELL's wedge,
  so a robot can score its last volley already parked. Before its TIP 3 sweep the south robot
  collects the GARDEN (only 3 of its 4 POLLEN: the corner one is out of a 14 in intake's reach). Two
  launchers per robot is worth about 10 points.
- **Real partners.** `TeamCode/autos/partner-*.pp` has a partner that only leaves and
  parks, and one that fires its preloads first. Both park at the north end of the LOADING ZONE,
  around (16, 122). Agree that with partners: the corridor between the west wall and the HIVE
  frame's foot is only 18–33 in wide for a robot's centre, so a partner parked at the south end of
  the LOADING ZONE blocks every route south. A partner that fires its 4 preloads at the north CELL
  brings TIP 2 forward from 17 s to 13 s.
- **Fixed paths.** The solo Autos drove a corner through the HIVE frame's foot (x ≈ 46, y 51–90)
  or reached over the centre line; they go round now. At double friction solo-three-tip's third
  TIP now falls just short (the CELL ends 96% loaded); it only made it before by driving through
  the frame.

**The endgame guard re-parks a robot that is already parked** (autokit, robot code). The guard's
park is the park card's path from its start point. If an Auto parks and then keeps working (waits,
fires), the guard can still decide time is short and drive the whole park path again, out of the
LOADING ZONE and here into the HIVE frame. The duo-lz Autos work round it: they drive to the park
spot with an ordinary path and end with a 2 in park card, so a late guard barely moves the robot.
The guard should know the park card has already run.

### The spill, filmed (3 Oct 2026)

Mentors filmed TIPs on the practice field at 120 fps (IMG_1957–1960, 4K; IMG_1965–1966 are 30 fps
slow-motion exports, fine for where pieces go but not for timing). What they showed:

| | Filmed | Simulated before |
|---|---|---|
| The rocker's swing, first movement to its stop | about 0.5–0.75 s | 1.0 s (assumed) |
| Pieces leave the lowered CELL | as the rocker reaches its stop, pouring off the lip and arcing out a little | rolled down the CELL floor and flew off it at about 60 in/s |
| First touch on the tiles | **just under 2 tiles, about 42 in, out from the alliance wall** (a mentor's estimate from the films, 4 Oct) | about 36 in out |
| First touch, after the rocker starts moving | about 1.15–1.2 s | about 1.09 s |
| After that | they bounce back toward the wall and spread | |

The simulated pieces now leave a tipping or lowered CELL at 0.6 of the speed they gather rolling down
its floor (`FieldSim.FILMED_SPILL_EXIT_SCALE`), so the first touch lands 36–43 in out, median 42, at
1.11 s. `SpillLandingTest` checks it (`BIOBUZZ_SPILL_EXIT=1,0.6,0.25` reprints the fit). 3 s after the
TIP starts the pieces lie 17–33 in out, x 49–74. The roll and bounce after landing are still
placeholders. *History:* on 3 Oct the fit was 0.25, from a photo of a box that caught the pieces
(first touch 44–48 in); on 4 Oct a mentor judged that about 5 in too near the CELL.

**How it scatters (4 Oct 2026).** In the films the spill fans out fast in every direction from where it
lands, 2–3 ft in under half a second, and is spread across the field within 3 s. A holey ball on foam
bounces off at an angle, so a hard landing now adds sideways speed in a random direction
(`FieldSim.FILMED_BOUNCE_SCATTER`, 0.45 of the landing speed, times 0.5–1.5): 0.5 s after the first touch
the pieces are a median 24 in from where they landed, and 3 s after the TIP they lie from the wall to 85
in out, x 19–92. Fitted by eye; `BIOBUZZ_BOUNCE_SCATTER=0,0.3,0.45` reprints it. Scattered pieces are much
harder to collect: on one launcher PartnerShoots-ThreeTip fell from 74 to 59 points. The catch spots
below were tuned before the scatter.

**What it changed.** The Autos that caught a spill standing against the wall, or swept the wall for
it, had been written for the old physics. They now wait where the bounce comes back to them, facing
the HIVE (`helpers.CATCH_R` / `CATCH_L`: centre 28 in out, front at 37, x 57.5 so it can turn; swept
22–33 in), catch what comes, top up with the webcam (`helpers.catch_spill`), then back off to their
usual shooting spot. three-tip-adaptive and staged-three-tip look for TIP 1's spilled NECTAR along the
wall, facing east (facing the HIVE they saw none). recycle5's right robot is now recycle3's. The
scores are in `tools/auto-routes/README.md`.

**The webcam pickup near the HIVE's feet.** The spill lands right in front of the ends of the HIVE's
foot bars (x 46, y 51.3 on red's side), so `CollectSeen` now refuses a piece unless the robot can get to
it, and turn back to the heading it started with from anywhere on the way, with 2.5 in to spare all
round; its look-around turn checks the HIVE's feet and the centre line too, and so does driving onto a
piece near the centre line. A real `CollectSeen` needs the same rules.

**Still to measure:** the first touch with a tape in the shot, where pieces come to rest (film from
above), and the swing time per CELL (`HiveTracker.Tuning.tipSeconds`, still the assumed 1.0 s, so the
simulated swing is slower than filmed).

### Two robots and five or more TIPs

**Where a spill goes** (`SpillStudyTest`, opt in with `BIOBUZZ_SPILL_STUDY=1`). A TIP's pieces hit
the tiles about 1.3 s after the TIP starts, in front of that CELL's opening (x 48–72), about a
dozen of them. With nobody there they roll to the wall (south: y 0–8; north: y 136–140). A robot
standing in front of the CELL, facing it (the start spots, (59, 9.5) and (59, 132.25)), fills to
4 within about a second, and the rest stop in a line against its front, about 30 in wide, half of
it across the centre line. Standing in the roll path catches better than driving through it on
cue: a timed sweep caught about one piece a spill.

**The pieces never need to cross the field.** The CELLs alternate, so a TIP's spill lands at the
end whose CELL is up again two TIPs later. Each robot can stay home at its own CELL: catch its
spill, and when its CELL rises, fire. The limit is how many of a spill a robot can take: 4 at a
time (G407), and only what reaches its intake.

**What the mechanisms are worth** (10 runs each at 50 in/s, `AutoStudyTest`):

| Robot | duo-lz (both park) | lean (stays home, no AUTO PARK) |
|---|---|---|
| one spring hood, 14 in intake | 80 pts; 4 TIPs in 3/10 | 56 pts |
| two spring hoods | 88; 4 TIPs in 7/10 | — |
| one spring hood, 24 in catcher | 90; 4 TIPs in 8/10 | — |
| **two spring hoods, 24 in catcher** | **90**; 4 TIPs in 8/10, TIP 3 at 14 s | **92**; 4 TIPs 8/10, 5 TIPs 3/10, 6 TIPs 2/10 |

- **A 24 in catcher matters most.** R105 allows 24 in across once the match starts, so an intake
  that folds out sideways can be 24 in wide; it takes the line of pieces stopped against the robot.
  An 18 in intake with corners that steer pieces in (which reaches pieces against a wall) gained
  little here, though it is what takes the GARDEN's corner piece.
- **Two launchers** halve a volley's time; worth 8–10 points.
- **Two launchers dedicated to POLLEN and NECTAR** lose: every opening volley is all POLLEN, so
  one launcher sits idle. They would remove the shared-setting problem, though.
- **A catapult** at 60° lost (56 points patterned, 42 plain), but not because its volley collides:
  it was firing from spots tuned for the spring hood. At 72° it is a contender; see "Which results
  hold" below.
- **Firing while intaking** (`StreamOn`/`StreamOff`, `experiments/home-stream-*.pp`) did not help
  as tried: shots fired on the move carry the robot's velocity unless the software allows for it,
  and even allowing for it the gain was nothing.
- **With a weaker partner** in the north robot's job (one spring hood, 40 in/s, following
  duo-lz-north), duo-lz still makes 88 points.

`SnapshotTest` writes the field at chosen moments as JSON and `tools/auto-routes/snapshots.py`
draws it, for seeing where pieces are without AdvantageScope.

**The best Autos so far** (`BestAutosTest` writes one log of each to `build/sim-logs/best/`):

| | Auto(s) | Partner needed | Points |
|---|---|---|---|
| 1 | **duo-lz-south + duo-lz-north** | one that runs duo-lz-north (level 2 below) | 80; 90 with two launchers and a catcher |
| 2 | **lean-opp-south + lean-opp-north** | the same, our robot design on both | 89 with two launchers and a catcher, no AUTO PARK; lean's 92 did not survive other physics (below) |
| 3 | **three-tip-adaptive** alone | none | 63 |
| 4 | **three-tip-adaptive** + a partner that fires its preloads | level 1 | 75 |
| 5 | **solo-two-tip** at 40 in/s | none | 48, parked |

The two-robot pairs are also in `pedro-paths/together/` (`duo-lz.txt`, `lean.txt`,
`lean-opp.txt`, `adaptive-with-partner.txt`). To watch two robots: AdvantageScope 3D Field, drag
`/Odometry/AllRobots3d` onto the field as a Robot to see both at once, or `/Odometry/Robot3d` as a
Robot plus `/Odometry/Partner3d` as a Ghost in another colour to tell them apart.

**An alliance rubric** to agree with a partner whose Auto we do not control. Pick the level the
partner can really do; we run the matching Auto.

- *Level 0, partner only drives:* it leaves and parks at the north end of the LOADING ZONE, around
  (16, 122), early, and stays. We run three-tip-adaptive (71 points).
- *Level 1, partner fires its preloads:* it starts against the north wall in front of the north
  CELL, fires its 4 POLLEN when the north CELL rises (after our TIP 1, about 4.5 s), then parks
  as level 0. We run three-tip-adaptive (75).
- *Level 2, partner runs the north job:* as level 1, then refills at the far FLOWER, fires again
  for TIP 2, stays in front of the north CELL to catch every spill and fires each time it rises,
  and ends parked at the north end of the LOADING ZONE. We run duo-lz-south (80–92).
- *Traffic rules at every level:* each robot owns one end: we take the south, the partner the
  north. Neither enters the other's end; the pieces don't need to. The only way between the ends is
  the west corridor (robot centre x 9–33 past the HIVE frame's foot), and only northbound, at the
  end, by the south robot going to park; the HIVE's tunnel (centre x 55–62, square to the field)
  is the southbound lane if a partner ever needs one, so travel goes clockwise and nobody meets
  head-on. Turn only with the robot's centre 12.7 in or more from the centre line. Nobody parks at
  the south end of the LOADING ZONE before the south robot arrives: it blocks the corridor.
- An Auto that only works if the partner does its part (duo-lz-south with a level-1 partner gets
  40–50 points) is the risk to avoid: confirm the level in the queue, not on the field.

The other alliance's spill can roll onto our side; the simulation ignores it, and so does the plan.

**Where a partner that only fires its preloads should start** (`tools/auto-routes/partner_start.py`;
10 runs each at 50 in/s, partner one spring hood at 40 in/s). The south CELL (right, from our drive
station) is the raised one at the start.

| Partner | We run | Points | If the partner misses |
|---|---|---|---|
| **North (left), fires when the north CELL rises (camera)** | three-tip-adaptive from the south | 76 | 71–76 |
| **North (left), fires on a 5 s timer**, from (30, 132) beside the CELL | three-tip-adaptive | 75–76 | 71–76 |
| South (right), fires at once and makes TIP 1 | north-first, mirrored | 72–76 | 56 (we have to make TIP 1 from the wrong end) |

Ask such a partner to start **north** and either watch for the north CELL or wait 5 s. Our Auto
then does not depend on them: their 4 POLLEN only make TIP 2 sooner, and 3 TIPs is where our solo
Autos stop either way. A partner who fires at the start from the north wastes its preloads but costs
us nothing. A partner who can only fire at once and only from the south is the one case for
north-first; it scores the same while they hit, and 20 points less when they miss. A 5.5 s timer
partner in front of the north CELL collided with our robot; from (30, 132) it is out of the way.


### FLOWERs are solid (1 Oct 2026), and what that cost

Watching the logs showed robots driving partly through FLOWER holders. `FieldSim.hitsFlower` now
flags it like the HIVE frame (`DRIVES INTO A FLOWER`), with a placeholder holder radius of 2 in
(`PLACEHOLDER_FLOWER_RADIUS_IN`; measure one). The routes use `helpers.flower` / `leave_flower`:
slide clear, turn where the robot's corners (12.7 in out) cannot reach the FLOWER, drive straight in
with the front against the tube, back straight out. That honest pickup costs about 3 s, and the
plans that refill at a FLOWER lost a TIP:

| Auto, robot (10 runs) | Before | Solid FLOWERs |
|---|---|---|
| duo-lz, two spring hoods + catcher | 90 | 66 |
| duo-lz, catapult + catcher | 86 | 86 |
| lean-opp, two spring hoods + catcher | 86 | 74 |
| three-tip-adaptive alone, one spring hood | 3 TIPs | 2 TIPs |
| three-tip-adaptive, partner only leaves, two spring hoods + catcher | 71 | 55 |

So every result above that leans on FLOWER refills is optimistic by about a TIP; the catapult,
which needs fewer pieces per TIP, lost least. Re-tuning the Autos for this is open work, and so are
the intake knobs: today's intake takes any piece that touches it at any speed.

### Which results hold, and timing a robot to a TIP

**Two rules every Auto here assumes** (our reading; confirm in the Game Manual Q&A): the launcher
may not spin before the match starts, so every Auto pays the spin-up (2.0 s on the spring hood)
before its first shot; and nobody adds NECTAR to the field during AUTO, so the simulated AUTO has
no human player.

**Which conclusions survive unmeasured physics.** `RobustnessTest` (opt in with
`BIOBUZZ_ROBUSTNESS=1`; `BIOBUZZ_ROBUSTNESS_ONLY` picks candidates by name) reruns the candidate
Autos, 4 seeds each, with the physics nobody has measured scaled up and down: tile friction
×0.5/×2, bounce ×0.6/×1.5, shot scatter ×1.5/×2, TIP swing speed ×0.7/×1.4, and three mixtures.

| Auto, robot | mean of 12 | worst |
|---|---|---|
| **duo-lz, two spring hoods + 24 in catcher** | **78** | 51 |
| duo-lz, one spring hood + catcher | 76 | 51 |
| duo-lz, two spring hoods | 75 | 51 |
| duo-lz, one spring hood | 72 | 51 |
| lean-opp, two spring hoods + catcher | 68 | 36 |
| lean, two spring hoods + catcher | 61 | 36 |
| three-tip-adaptive (alone), two spring hoods + catcher | 61 | 43 |
| three-tip-adaptive (alone), one spring hood | 51 | 38 |
| lean, one spring hood | 51 | 31 |

- **duo-lz beats lean** in 11 or 12 of the 12 variants for every robot. Lean's 92 points was the
  baseline physics only; it depends on the roll, which is the least trustworthy part.
- **Shot scatter decides the most.** Every Auto falls to its worst when the scatter doubles (to
  about 3% in speed and 1.6° in direction). So the first thing to measure on a real launcher is
  how consistent it is, before how fast it is.
- **The catcher helps everywhere** (lean 8–0; duo-lz 7–2 on one launcher, 4–1 on two). **A second
  launcher pays off only with the catcher**: without one, two launchers lost to one in lean 0–8.

**lean-opp** (`lean_opportunist.py`) adds one decision after each volley: if the CELL has not
tipped within 1.5 s, fetch more pieces (south robot: the GARDEN; north robot: the far FLOWER),
come back and fire again; if it has, stay in the roll path and catch. It beats plain lean on
robots without a catcher (9–2), and averages 68 against 61 on the best robot, but the catcher
already fills the robot, so there it is about even (6–5). It is the better Auto if both robots
must stay home; duo-lz, which also parks, is still better.

**A convoy loses** (`convoy.py`, `experiments/convoy-*.pp`): both robots fire into whichever CELL
is up, ours through the tunnel and our partner up the west corridor. 26–30 points. Only the robot
standing in the roll path catches (4 pieces); the one beside it catches almost none, so the CELL
at the far end, emptied by its own TIP and needing about 8 POLLEN, rarely tips. (The relay
experiment found the same.) Two robots each owning one end, with the spill feeding the same CELL
two TIPs later, stays the plan.

**When the pieces land.** The fall from the lip to the tiles is gravity plus the rocker's swing;
the roll after it depends on friction and bounce. `SpillStudyTest` now prints percentiles, and
`BIOBUZZ_SPILL_PHYSICS=friction,bounce,spread,swing` reruns it with the scales above. Measured from
the moment the rocker is 5° off its stop (the robot's `Tip` trigger):

| Physics | first touch, p10 / p50 / p90 | where (RED, south CELL) |
|---|---|---|
| baseline | 1.06 / 1.34 / 1.40 s | x 50–67, y 22–36 |
| friction ×0.5, ×2 | 1.02–1.04 / 1.14–1.18 / 1.38–1.44 s | x 50–67, y 20–40 |
| bounce ×0.6, ×1.5 | 1.04–1.06 / 1.12–1.48 / 1.28–1.72 s | x 46–67, y 18–36 |
| TIP swing ×0.7 (slow) | 1.18 / 1.44 / 1.64 s | x 50–67, y 23–37 |
| TIP swing ×1.4 (fast) | 0.88 / 1.16 / 1.22 s | x 50–67, y 20–35 |

The place repeats under every variant: a box in front of the opening about 18 in wide and 15 in
deep (north CELL: y 105–123). The time repeats within about ±0.2 s, except for the TIP's own
speed, which moves it by 0.3 s either way. That is why filming a TIP (below) comes first: once the
tip time is measured, a robot knows it has at least 0.8 s, minus the camera's latency, after the
`Tip` trigger before the first piece lands. At 50 in/s that is about 25–30 in of travel, enough to
react rather than predict.

**Knowing a CELL has tipped, or is about to**, from earliest to latest:

| Signal | When | How sure |
|---|---|---|
| **Counting**: the robot knows what it put in the raised CELL (3 NECTAR at the start, then 3 POLLEN tip it; an emptied CELL takes about 8) | as the deciding shot leaves, about 0.7 s before it arrives | as sure as its shots. It can't count a partner's or an opponent's |
| **HIVE camera** (`HiveTracker`, the `Tip` trigger) | about 0.8–1.3 s before the first touch | sure: it sees every TIP, whoever caused it |
| **Webcam, pieces falling** in front of the opening | a fraction of a second before the first touch | sure but late, and the webcam runs only while intaking |
| **The other CELL up** (`LeftCellUp`/`RightCellUp`) | when the rocker settles | sure, and latest |

So: **commit on counting, confirm with the camera.** Start toward the catch spot when the deciding
shot leaves; if `Tip` has not fired by the time that shot should have landed plus a margin, do
something useful instead (lean-opp's "Did it tip?"). A robot standing in the roll path before the
pieces touch catches 4 in about a second; driving through on cue caught about one.

**Filming a TIP** (240 fps, phone on a tripod square to the HIVE's side, a tape measure on the
tiles in front of the opening and one upright at the CELL's lip):

1. Load a raised CELL to one POLLEN short, then toss the tipping POLLEN in. 5 times per CELL.
2. Count frames from the first movement to: the rocker 5° off its stop (mark the angle on the
   screen); the rocker on its stop (the tip time → `HiveTracker.Tuning.tipSeconds`); the first and
   last pieces leaving the lip; the first three touching the tiles, and where on the tape.
3. A second phone above, or the 24 in tiles as a grid: where each piece is 2 s after it touches.
4. Drop a POLLEN and a NECTAR from 40 in onto the tiles, 3 times each, and read the first rebound
   (→ `HiveCalibration.MEASURED_DROP_IN`, `MEASURED_REBOUND_IN`).

If the video's first-touch times match `SpillStudyTest`'s within about 0.1 s, Autos built on the
fall can be trusted even while the roll is not.

**The robot's own `.wpilog`.** A practice run of an exported Auto already writes one
(`logging/MatchLog`): pose, path and loop time. Open it in AdvantageScope beside the simulated log of
the same `.pp` (same keys) and the real path speed (→ AutoSim's `speed(...)`) and timing are on
one timeline; `WpiLogReader` reads it in a test, so the comparison can be automated. Spin-up and
shot timing need the launcher logging its RPM and each shot, and the HIVE tracker's state needs
the open logging issue.

**The loop at a meeting:** film and log → measured numbers into `HiveCalibration` and `RobotDesign`
→ rerun `RobustnessTest` and the Auto studies → the ranking holds (build on it) or flips (whatever
flipped it is the next thing to measure).

**The catapult, revisited.** `CatapultVolleyTest` (opt in with `BIOBUZZ_VOLLEY_STUDY=1`) fires
one volley of 4 POLLEN into a CELL held still and counts what stays in. The old 60° catapult kept
3.4–4 of 4 from 50–65 in back but only 1.5–2 from 38 in, where these Autos fire. Collisions were
not the problem: even the old plain catapult, whose pieces started overlapping, kept 3.6 from 50 in.
The *clump catapult* (`RobotDesign.catapultClump`) throws as a real arm does: 2 by 2, one shared
error for the throw plus 0.3 of a flywheel's scatter per piece. Its angle decides where it works:

| Launch angle | 24 in | 31 in | 38 in | 44 in | 50 in | 65 in |
|---|---|---|---|---|---|---|
| 60° | 0 | 0.7 | 2.0 | 3.6 | 4.0 | 3.9 |
| 70° | 1.2 | 3.5 | 4.0 | 4.0 | 4.0 | 0.8 |
| 75° | 2.8 | 4.0 | 4.0 | 3.7 | 3.4 | 0.1 |

At 72° in whole Autos (10 runs, 50 in/s, 24 in catcher on both robots):

| Check | duo-lz | lean-opp |
|---|---|---|
| clump catapult, 72° | 88 (TIP 3 at 13.2 s in 10/10) | **105** (4 TIPs in 9/10) |
| the clump comes apart (full scatter per piece) | 76 | 86 |
| re-cock 1.5 s instead of 0.8 s | 88 | 92 |
| angle 68° / 76° | 82 / 86 | 96 / 90 |
| two spring hoods instead | 90 | 89 |
| partner with one spring hood at 40 in/s: catapult | 78 | 72 |
| partner with one spring hood at 40 in/s: two spring hoods | 88 | 71 |

Across the 12 physics variants: duo-lz on the catapult mean 80, worst 51; lean-opp on the
catapult mean 76, worst 46 (two spring hoods: 78/51 and 68/36). It is the least hurt by shot
scatter, since the clump shares one error. It needs no spin-up, which matters because a flywheel
may not spin before the match starts (whether a catapult may start cocked is for the Q&A).

**What flips it:** a clump that comes apart in the air (−12 to −19 points), and a weaker
partner, where two spring hoods do better (88 against 78). A slow re-cock or a few degrees of
build error barely matter.

**A direction to lock in.** These hold whichever launcher is chosen, so they can be built now:

1. **A 24 in catcher** (folds out sideways after the start, R105). It helped in every comparison.
2. **Fire from 31–44 in in front of the opening at about 72–75°.** Both launchers work there, and
   it is where the robot stands to catch the spill, so it fires and catches without moving.
3. **duo-lz as the Auto,** with lean-opp when the partner can run its half (rubric level 2).
   duo-lz parks both robots, which also counts toward SWARM.
4. **The camera's `Tip` trigger plus counting shots** (above) to time the catch.

The launcher: **two spring hoods is the safe choice.** Robust (mean 78), best with the partners
we will usually have, and the CAD exists (`cad/spring-hood-launcher`). The 72° catapult is the
higher ceiling (105 with a level-2 partner) and the most scatter-tolerant, but only if its
clump stays together. That can be settled in one meeting: a plywood arm at about 72°, a cup that
holds 4 POLLEN 2 by 2, filmed at 240 fps from the side, 10 throws from 38 in. The model's tight
clump has the 4 pieces within about an inch of each other as they pass the CELL's lip; a loose
one spreads them 2–3 in. Tight in 9 of 10 throws: build the catapult. Otherwise, the spring hoods,
and don't reopen it.

### One launcher, the webcam, and driving under the HIVE

**One launcher for POLLEN and NECTAR.** `LauncherStudyTest` (opt in with
`BIOBUZZ_LAUNCHER_STUDY=1`) finds, for each shooting spot, the launch speeds that land each piece in
the raised CELL. Then it works out how far apart NECTAR's and POLLEN's speeds can be at one launcher
setting and still both score, leaving room for the shot-to-shot spread:

| Spot | POLLEN scores at | NECTAR scores at | NECTAR/POLLEN one setting serves |
|---|---|---|---|
| Against the wall, straight on | −5% to +7% | −5% to +7.5% | 0.94 to 1.07 |
| Angled, 30 in out | −7.5% to +7% | −7% to +7% | 0.92 to 1.09 |
| Straight on, 20 in from the opening | −6.5% to +7.5% | −6.5% to +8% | 0.92 to 1.09 |
| 12 in from the opening | nothing | nothing | — |

So **one launcher works if, at one setting, NECTAR leaves within about 5% of POLLEN's speed**, and
the setting is tuned between the two, not for POLLEN alone. Checked in whole Autos: NECTAR 7% slow
with the setting tuned for POLLEN gives spill-three-tip its second TIP in 4 of 10 runs; tuned
between, 10 of 10. At 4% apart, tuned between, every Auto does as well as with a perfect launcher.
A steeper arc than the flattest one into the opening plus 6° only narrows the window from the wall,
so it does not help. Sorting the pieces so each gets its own setting is not needed if the launcher
meets this, and it is risky anyway: holding a 5th piece while sorting is a G407 foul. No air drag
is simulated, and drag would slow the light POLLEN more than NECTAR; measure the real speeds.

**Picking up with the webcam.** The sim command `CollectSeen` stands in for a pickup guided by
`PieceVisionSubsystem`'s colour blobs. It sees loose pieces on the tiles within 60 in and 35° either
side of where the intake faces, and drives the intake onto the nearest. It repeats until the robot
is full. If it sees nothing, it turns on the spot to look around once. It takes only POLLEN and our
own NECTAR (G408), stays on our half and out of the HIVE frame, and can be given a zone so two robots
split a spill between them (`AutoSim.collectZone`). In duo-south + duo-north it replaced the fixed
sweep along the wall (`experiments/duo-vision-*.pp`). The third TIP came in 10 of 10 runs instead of
8, but at 23 s instead of 17 s: looking around and driving to each piece costs more than a
well-placed sweep. Turning on the spot at x = 59 also swings a corner over the centre line, since
an 18 in robot needs its centre at least 12.7 in from the line to turn freely. A smarter pickup
(sweeps past several pieces, no spin) is where vision would pay.

**Driving under the HIVE.** Its lowest point is 25.5 in above the tiles (§9.6), so a robot shorter
than that fits underneath. The frame stands only on its two side feet, and G409 lists driving under
the HIVE as fine; stopping there to catch a spill is not. On red's half there is one lane: robot
centre between x = 55 and 61.75, square to the field. Turned, its corners reach the frame's feet or
the centre line; the sim flags both. `experiments/relay-*.pp` tried your relay idea. After each TIP,
both robots pick up its spill and go to the other end, one under the HIVE and one round the west
side, and fire together. TIP 2 came at 9.2–10.1 s instead of 10.9 s. But the robots got in each
other's way collecting, and much of each spill rolls in under the HIVE, so later TIPs did not hold
together. It is a starting point, not a working Auto.

**Corralling.** The manual counts herding as CONTROL (Glossary: "intentionally pushes a SCORING
ELEMENT to a desired location"). So a corral that gathers loose pieces while the robot holds others
risks G407. A spill that rolls into a robot waiting with its intake facing the HIVE is "deflecting",
which is not CONTROL. Robots cannot talk to each other during a match, so splitting a spill means
agreeing zones beforehand.

### Choosing a launcher for POLLEN and NECTAR

A launcher that throws POLLEN well but NECTAR nowhere near (or the reverse) is what the physics
predicts for the launcher most teams build first, and the simulation reproduces it. This section is
what `LauncherModel` and `LauncherDesignStudyTest` found, and how to check it on a prototype.

**What the pieces are** (AndyMark): POLLEN 2.80 in, 24.9 g; NECTAR 3.62 in, 41.3 g. Both are
26-hole pickleball-style plastic balls. A free-flight study of 26-hole pickleballs measured a drag
coefficient of about 0.45 and, with backspin, a lift coefficient of about 0.2 (Tennis Warehouse
University, "Pickleball Aerodynamics"). Pickleballs are stiff: they must take less than 43 lbf to
squeeze by 0.25 in (USA Pickleball, ASTM F1888).

**Why both can fly alike.** Drag and lift slow or lift a ball in proportion to its area over its mass:
0.159 cm²/g for POLLEN, 0.161 for NECTAR. So two pieces that leave at the same speed, angle and spin
follow the same path; with air on (`FieldSim.air`), their scoring speed windows match to within a
few hundredths of a m/s. **Any difference comes from inside the launcher.**

**Why the usual launcher fails.** One wheel, a fixed hood, the gap set for one piece:

- gap set for POLLEN (2.5 in): NECTAR would have to be squeezed 24%, and these pieces cannot be, so
  it jams or cracks;
- gap set for NECTAR (3.3 in): POLLEN never touches the wheel.

Even when both fit, the heavier NECTAR pulls a light flywheel down further during the shot (about
twice the droop), so it leaves about 10% slower. Scoring speed windows are only about ±7% wide.

**The model.** `LauncherModel` squeezes the piece between a driven wheel and a hood, or a second
wheel. The squeeze is shared between the piece's stiffness, the tread's and, on a spring-loaded hood
or wheel, the spring's. That force sets how hard friction can drive the piece. Speed and spin are
integrated through the contact, the wheel loses what the piece gains, and the motor pulls it back up
between shots. It reproduces the rules of thumb: one wheel against a hood throws at 0.40–0.45 of the
wheel's surface speed, two wheels at 0.8–0.9.

**The sweep.** 5832 designs, run by `BIOBUZZ_LAUNCHER_DESIGN=1 ... --tests '*LauncherDesignStudyTest*'`.
They cover four kinds (fixed hood, spring hood, two wheels, two wheels with one sprung), 72 mm, 96 mm
and 4 in wheels, firm to soft tread, gaps, spring preloads and rates, three flywheel sizes, one or two
motors, and launch angles of 60–75°. Each design gets one wheel speed per shooting spot for both
pieces. Each shot carries realistic sloppiness:

- wheel speed ±1.5%;
- piece size ±1.5% (the manual says pieces vary);
- piece stiffness ±20%;
- launch angle ±1°;
- the robot's distance to the HIVE off by ±1.5 in;
- the wheel slowing over a burst of 4 shots 0.45 s apart.

The worst of three spots (against the wall, 20 in out and angled 30 in out), as the chance that both
pieces score:

| Launcher | Worst spot | When the guesses are wrong* |
|---|---|---|
| **One wheel, spring-loaded hood**, heavy flywheel | **86%** | **85% worst, 85% mean** |
| Two wheels, one spring-loaded, heavy flywheel | 85% | 0% worst, 69% mean |
| One wheel, fixed hood, soft compliant tread, heavy flywheel | 82% | 0% worst, 66% mean |
| Two wheels, fixed gap, soft tread, heavy flywheel | 80% | 48% worst, 70% mean |
| One wheel, fixed hood, firm tread, light flywheel (the usual first try) | 0% | — |

\* 54 combinations of the things the model had to guess: the pieces' stiffness ×0.5 to ×2, NECTAR
0.5–1.1× as stiff as POLLEN, wheel grip 0.6–0.9, and squeezing loss ×0.3 to ×2.

**The launcher to prototype first**, and what each part is for:

- **One wheel, 72 mm or 96 mm, firm or medium tread**, on one or two 6000 rpm (1:1) motors. It runs
  around 2600–3000 rpm (72 mm) or about 2000–2200 rpm (96 mm), half the motor's free speed, which
  leaves torque to recover between shots.
- **A hood that follows the wheel's curve and is spring-loaded off a hard stop.**
  - Hard stop: 2.2–2.6 in from the wheel surface, so POLLEN is lightly squeezed.
  - Spring: about 1–9 lbf of preload, and soft, about 3 lbf per inch, so NECTAR pushes the hood out
    about an inch with little more force. Surgical tubing, a constant-force spring or a long
    extension spring all fit. A stiff spring squeezes NECTAR harder and costs about 5%.
  - Give it at least 1.2 in of travel.
  - Hood surface: polycarbonate. Grip tape made no difference in the model.
- **A heavy flywheel on the wheel shaft: at least about 6e-4 kg·m², better 1e-3.** A 4 in steel disc
  0.5 in thick is about 1e-3. With the wheel alone, NECTAR leaves about 5% slower than POLLEN and the
  worst spot drops to 68%. Spin-up to speed takes about 1.5 s with two motors, 3 s with one.
- **Launch at about 75°.** At 65° the wall shots still score 98–99%, but at 20 in from the opening
  nothing does: steep lobs work from everywhere.
- **Backspin comes free:** the wheel under the piece and the hood over it give a spin number of
  about 1, which steadies the flight (lift about 0.2).
- **A concentric hood keeps the launch angle the same for both sizes.** A piece leaves along the
  tangent at the end of the hood. A flat or off-centre hood launches NECTAR and POLLEN at different
  angles.

**Built from goBILDA parts and seven printed parts:** [`cad/spring-hood-launcher/`](../cad/spring-hood-launcher/README.md)
has the printable STLs, the full parts list with SKUs, the assembly steps and the script that
generates and checks them. This build scores 83% at the worst spot with one motor, NECTAR leaving
at 98–99% of POLLEN's speed. In short:

- One 1:1 Yellow Jacket drives the wheel shaft through two 30-tooth gears (1:1, 24 mm apart).
- On the shaft: two 72 mm Gecko wheels in the middle and two 82 mm steel flywheels each side,
  about 7e-4 kg·m² in all.
- The hood is a 90° arc concentric with the wheel shaft, behind the wheel, from 75° below
  horizontal to 15° above. At its stop it is 97 mm from the shaft centre, a 2.4 in gap to the
  wheel.
- Each side of the hood hangs on two parallel 80 mm arms, so it slides 31 mm back and down for
  NECTAR without turning. A goBILDA extension spring on one of three pegs sets the force.
- Pieces are fed from below. They ride up the back of the wheel, which moves upward, and leave at
  75° toward the HIVE with backspin. The model predicts POLLEN at 5.04 m/s and NECTAR at 4.98 m/s
  at 2700 rpm. Check that with a slow-motion video.

**What is not modelled.** Air drag differences from hole patterns, real spring hysteresis, the
pieces' out-of-roundness beyond size, feeding (a jammed feed is a jammed launcher), and wear. The
stiffness, grip and squeeze loss are guesses, which is why the table above shows what happens when
they are wrong.

**Check a prototype against the model**, and feed what you measure back in:

1. **Piece stiffness.** Press each piece on a kitchen scale with a flat block until it squeezes
   0.25 in. The force in lbf × 700 is its stiffness in N/m.
2. **Exit speed and angle.** Film the launch in slow motion (240 fps) beside a tape measure. Speed =
   distance between two frames × 240. Do both pieces at the same wheel speed, read from the encoder
   in Panels. The goal is NECTAR within about 5% of POLLEN.
3. **Wheel droop.** Graph the flywheel's velocity in Panels during a 4-shot burst.
4. **Run your launcher in the model:**
   ```
   BIOBUZZ_LAUNCHER_TRY="kind=HOOD_SPRING wheel=2.83 tread=20000 gap=2.6 preload=15 rate=500 \
     inertia=1e-3 motors=2 angle=75 pollenStiffness=17500 nectarStiffness=14000" \
     ./gradlew :TeamCode:testDebugUnitTest --tests '*LauncherDesignStudyTest.tryOne*' -i
   ```
   It prints each piece's exit speed, spin and squeeze across wheel speeds, the NECTAR/POLLEN ratio,
   and the chance each scores from each spot. If its exit speeds disagree with your video, adjust
   `friction` and `rolling` until they match. Then trust its comparisons between designs.

**Robot speeds, for comparison.** goBILDA rates its Strafer chassis (312 rpm motors, 104 mm mecanum
wheels) at 66.8 in/s; with 435 rpm motors that is about 93 in/s. Real robots reach roughly 70–90% of
that. So the 50–60 in/s used for the Autos above is realistic for a 312 rpm build; measure yours with
the Foresight tuner (`pedro/Tuning`) and pass it to `AutoSim.speed`.

### Why the on-robot build is not here: it takes port 8080

`page.j5155.AdvantageScope:lite` serves its UI from **port 8080**, which is
already FTC Dashboard's port. Both use NanoHTTPD, and the second one to start
cannot bind:

```
FATAL EXCEPTION: Thread-13
java.net.BindException: Address already in use
    at fi.iki.elonen.NanoHTTPD$ServerRunnable.run(NanoHTTPD.java:1763)
```

That kills the thread, the app ANRs, and the Robot Controller never reaches
ready. **What you see on the Driver Station is "no heartbeat" and an empty OpMode
list** — both tabs, every OpMode, including ones that have nothing to do with
AdvantageScope. Nothing points at a port conflict, and it compiles and installs
perfectly.

Worse, `http://192.168.43.1:8080/` still answers, because Dashboard won the race
and is serving normally. So the robot looks half alive, which sends you looking
at deploys and OpMode registration rather than at a bound socket.

**If you try this again**, the two servers have to stop colliding first — drop
FTC Dashboard, or move one of them to another port. Do not just re-add the
dependency; it will take the robot down the same way, and the symptom will not
tell you why.

> **What this section does and does not establish.** The port-8080 collision is
> real and is reason enough to keep the dependency out. What is *not* established
> is that it caused the September 2026 outage: the same `BindException` loop
> continued after the dependency was removed and the hub fully reinstalled. If
> you are debugging a "no heartbeat" RC, this page tells you the shape of the
> failure — one NanoHTTPD losing a socket race — not which library is losing it.
> The stack trace is a bare thread entry point and names nobody, so identify the
> loser from what initialises immediately before the crash.

## The Panels stack: seeing data and changing values at a meeting

**The decision, 24 Sep 2026: Panels is the whole dashboard.** Sloth, Pedro, Ivy and Panels; no
FTC Dashboard, no AdvantageScope Lite, no desktop AdvantageScope. Design for Panels rather than
treating it as one of several outputs.

Two things forced it and one thing makes it comfortable. Panels and Dashboard cannot both be
installed (see the next section). AdvantageScope's FTC support is labelled *experimental* by its
own documentation and is not officially supported until the Systemcore transition in 2027-28,
which is not this season. And Panels already covers every job we were splitting across three
tools.

### Getting to it

Join the robot's Wi-Fi, then **`http://192.168.43.1:8001`** on a Control Hub. (Phone RC would be
`192.168.49.1:8001`.) Port 8001 is Panels' own; the `8080` you may remember is the SDK's web
server, which is still there and still serves the RC's own pages.

### Seeing data

| Want | Use | Entry point |
|---|---|---|
| Numeric series, graphable | Telemetry + Graph View | `PanelsTelemetry.INSTANCE.getTelemetry()` → `addData(key, value)` |
| Text lines | Telemetry | same manager → `debug(line)` |
| Robot pose and paths | Field View | `util/FieldView.java` — `shouldDraw()` / `drawRobot(x, y, heading)` / `send()` |
| Replay a run afterwards | Capture | browser-side, nothing to write |
| Start/stop OpModes from the laptop | OpMode Control | browser-side |
| Limelight pipeline tuning without a USB cable | Limelight proxy | browser-side |

Call `update()` once per loop after the `addData`/`debug` calls, and wrap the whole block in a
`try`/`catch` that swallows. `FlywheelSpeedTestOpMode.publishPanels()` is the reference
implementation: telemetry must never take a mechanism down mid-run.

**`update(telemetry)` publishes to both.** `TelemetryManager` has a second overload that takes the
SDK's `Telemetry` and mirrors the same lines to the Driver Station, so one set of `addData` calls
feeds the graph on the laptop and the text on the phone. `LoopTimeBaseline.publish()` uses it.
There is no reason to maintain two parallel sets of telemetry calls, and the older OpModes that do
predate this being known.

Keys are flat strings. Panels does not render a `a/b/c` key tree the way AdvantageScope did, so
prefix instead — `left_rpm`, `center_rpm`, `right_rpm`.

### Changing values live

`@Configurable` on the class, and the fields it exposes must be **`public`, `static`, and
non-`final`**. Primitives, enums, strings, arrays, lists, maps and custom objects all work.

The pattern this repo uses, and the one to copy, is a single static root holding plain nested
objects — `FlywheelBank` declares:

```java
@Configurable
public class FlywheelBank {
    public static FlywheelTuningConfig config = new FlywheelTuningConfig();
```

and everything under `FlywheelTuningConfig` is ordinary instance fields. Panels reflects through
the static root into the tree, so one annotation exposes the whole structure and the sub-configs
stay readable as normal Java.

Three things that will cost you a meeting if you forget them:

- **A field with no annotation above it simply never appears.** No error, no warning — it is
  just absent from the tree. `VisionSightingDiagnostics` carries a note about exactly this
  happening once already.
- **It is one-way unless code asks otherwise.** Edits in Panels reach the robot immediately.
  Values changed *in code* reach the browser only when a tab connects, after a browser edit, or
  when the code calls `PanelsConfigurables.refreshClass(TheConfigurableClass.class)`. So any code
  that writes a configurable must call `refreshClass` right after, or the page shows a stale number
  and an edit typed over it looks like it did nothing. `FlywheelSpeedTestOpMode.refreshPanelsConfig()`
  is the example — its d-pad nudges write `targetRpm`.
  > **History note, 26 Sep 2026.** This bullet used to say only "refresh the browser". That was
  > the workaround, not the answer: `refreshClass` is Panels' own call for this (checked in
  > `configurables-0.3.2+1.0.5`, which sends values on connect, on a browser edit, and on
  > `refreshClass`, and at no other time).
- **Read from the source every time.** Do not copy a configurable into a local field at init and
  use the local afterwards — you will be reading the value from before the edit, and it will
  look like the edit did nothing.
  The same goes for values sent to hardware once: `LimelightVisionSubsystem` used to call
  `pipelineSwitch(Tuning.pipelineIndex)` at init only, so editing the pipeline in Panels did
  nothing until a restart. It now re-applies the index whenever it changes.

### The Field view, and why Pedro's drawing helper is not how we get there

> **History note, 25 Sep 2026 — this section replaces a wrong one.** For a few hours this file said
> "Pedro 3 ships a `Drawing` class that prepares Panels Field in Pedro units, and
> `Follower.telemetryDebug()` uses it", and listed calling it as the work to do. That class is not
> in anything we depend on. It was written from Pedro's documentation rather than from the jars.

`Drawing` and `Follower.telemetryDebug()` live in **`com.pedropathing:telemetry`**, which is on the
[deliberately excluded](#deliberately-excluded) list — still published at `1.0.0`, never updated for
Pedro 3. Checked by listing every class in `core-3.0.1`, `revhub-3.0.1`, `tuning-1.0.1`,
`ivy:core-1.1.1` and `ivy:pedro-1.1.1`: no `Drawing`, and no reference to Panels anywhere in the
set. So the choice was re-adding an excluded, Pedro-2-era dependency, or drawing two shapes
ourselves.

Neither, as it turns out. **Panels already knows Pedro's coordinate frame.**
`FieldPresets.PEDRO_PATHING` is one of its four built-in presets, next to the default FTC and Road
Runner frames, so the conversion Pedro's helper would have done is done by a library we already
have. `util/FieldView.java` sets that preset and draws the robot; it is about forty lines.

> **Update, 28 Sep 2026 (#116).** `FieldView` now uses a copy of that preset, not the preset
> itself. The built-in one centres on (72, 72), which assumes a 144 in field. Our field is 141.5 in
> wall face to wall face, the same size the Visualizer uses, so the drawn robot sat 1.25 in off.
> The copy keeps the rotation and flip and centres on `FieldFrame.FIELD_CENTRE_INCHES` (70.75).
>
> Rejected alternatives:
> - Leaving the error in, because it is too small to see. A small disagreement between tools is
>   exactly how last season's frame bugs started.
> - Scaling the drawing so the whole field fits Panels' 144 in canvas. That changes what a
>   position means; re-centring only moves the drawing.
>
> The one-size rule is in CLAUDE.md, and `FieldFrameTest` enforces it.

**The throttle is the part that will bite you.** `FieldManager.update()` sends only when
`canvasUpdateInterval` has elapsed — 100 ms by default. When it is not time yet it returns having
done *nothing*, and that includes not clearing the canvas, so shapes added since the last send stay
in the list. A 200 Hz loop that draws unconditionally ships twenty overlapping robots in one
canvas. Gate on `shouldDraw()`:

```java
if (fieldView.shouldDraw()) {
    fieldView.drawRobot(pose.x(), pose.y(), pose.heading());
    fieldView.send();
}
```

Panels' `Rectangle` takes no rotation, so an oriented chassis outline is not available at all. A
circle at the footprint radius plus a line along the heading is what `drawRobot` draws, and it is
the honest shape rather than a compromise.

### Bringing the field view up on a robot

In order. Most of a lost morning is steps 1 and 2.

| # | Do | If it goes wrong |
|---|---|---|
| 1 | Deploy with the **TeamCode** run config, not Sloth Load | This branch changes `build.gradle`. Sloth Load hot-reloads classes only, so the robot keeps the old dependency set and every symptom below is a lie |
| 2 | If the upload hangs or is refused, run **Remove Sloth Remote**, then deploy again | A stale payload in `/storage/emulated/0/FIRST/dairy/sloth` can make upload impossible. Sloth's own README calls this common this season |
| 3 | Power-cycle. Wait for the RC to reach `Robot Status: running` and the DS to show a heartbeat | No heartbeat and empty OpMode lists means the `BindException` crash loop is back — check that nothing re-added FTC Dashboard, and that step 1 was a full install |
| 4 | Laptop onto the robot's Wi-Fi → `http://192.168.43.1:8001` → Field plugin | Port **8001**, not 8080. 8080 is the SDK's own web server and will happily serve you an unrelated page |
| 5 | Run **Loop Time Baseline**. Watch `field_frames_sent` | Climbing means the drawing path works. Stuck at 0 means it does not, and nothing further down is worth debugging |
| 6 | Look for the white cross at field centre | Visible means canvas, background and coordinate frame are all fine. Absent with frames climbing means the browser, not the robot |
| 7 | Push the robot by hand. The blue circle should move | Does not move, cross is visible, telemetry says `pose: NO LOCALIZER` → the Pinpoint is not in the active config. Run `Validate Hardware` |

**Expect the pose to be wrong, and do not treat that as a broken field view.** The Pinpoint pod
offsets and directions in each robot's `localizerConfig` (`pedro/robots/`) are still zeros and placeholders. A dot that
moves but drifts, or strafes when you push forward, or spins the wrong way, is the *expected* state
before the Pinpoint Tuner runs — it is the thing the field view exists to show you. The field view
is working as soon as the dot responds to the robot moving at all.

That is also the order to work in: field view up first, *then* tune, because watching the dot is how
you tell whether the offsets you just pasted in were right.

### Loop time: `LoopTimeBaseline`

The reference TeleOp, under **Diagnostics**. It does the four things every real loop does — read
gamepads, read odometry, write motor powers, publish telemetry — and times each separately, so a
regression next month points at a culprit instead of at "the loop".

| Key | What it is for |
|---|---|
| `loop_hz`, `loop_mean_ms` | The headline. Note it at the start of a build session and again at the end. |
| `loop_max_ms` | Stalls. A 4 ms loop with one 180 ms hitch per match loses a path segment and moves the mean by almost nothing. |
| `loop_sd_ms` | Separates "steady but slow" (usually a blocking read to cache) from "fast but spiky" (usually GC). |
| `loop_slow_pct` | Fraction of loops over 10 ms. Over a whole match, 2% and 40% are different problems. |
| `cost_odometry_ms`, `cost_drive_ms`, `cost_field_ms`, `cost_telemetry_ms` | Where the time went. |

Graph these rather than reading them as text — a stall is obvious as a spike and nearly invisible
as a number that flickers once.

Two design decisions worth knowing before you change it:

- **It does not use the follower.** `Constants.create()` needs Foresight, and
  `Constants.createAlgorithm()` deliberately throws until the Foresight Tuner has run. A baseline
  OpMode that cannot init on an untuned robot is useless in September. This one takes the
  drivetrain and localizer directly and drives open-loop, which also makes it the right tool
  *during* Pinpoint tuning — the field view shows whether the offsets you just pasted track when
  you push the robot around.
- **Missing hardware degrades it rather than stopping it.** No Pinpoint means no pose, but the
  loop is still measurable. `ValidateHardware` is the OpMode whose job is to name what is missing.

The statistics live in `util/LoopTimer.java`, which has no SDK dependency and is unit-tested
against a fake clock. The rule that test exists to protect: the first `lap()` records nothing,
because the interval spanning init is usually the largest of the run and would own `maxMs()`
permanently.

### What is still not wired up

Recorded here so it is a choice rather than a surprise:

| Gap | Cost |
|---|---|
| Only `FlywheelSpeedTestOpMode` and `LoopTimeBaseline` publish to Panels. The vision and calibration OpModes use Driver Station `telemetry` only. | No graphs for the thing most in need of them. |
| Nothing draws game elements on the field — `FieldView.drawMarker` exists and has no callers. | Vision sightings are numbers in a list rather than dots where the robot thinks they are. |
| No shared telemetry helper, so each OpMode still publishes in its own style. | Key names and update cadence will drift between OpModes. |

## Why FTC Dashboard is not in the dependency set

> **History note.** Everything in this file that predates 24 Sep 2026 assumes FTC
> Dashboard is installed: the `Included` table listed it, the `shooter` package
> published to it, and the AdvantageScope section treated it as the data path.
> Those passages have been corrected in place where they were load-bearing, but
> if you find one that still assumes it, this section is what supersedes it.

**Panels and FTC Dashboard each work perfectly alone. With both installed, the
Robot Controller does not start.** It crash-loops:

```
CoreRobotWebServer      started port=8080
TooTallWebSocketServer  Started WebSocket server on port 8081
... ~3.5 s later ...
FATAL EXCEPTION: Thread-13
java.net.BindException: Address already in use
    at fi.iki.elonen.NanoHTTPD$ServerRunnable.run(NanoHTTPD.java:1763)
```

The thread dies, the RC never reaches `Robot Status: running`, `FtcAccessPointService`
relaunches it about ten seconds later, and it repeats until the battery comes out.
The Driver Station shows **no heartbeat and empty OpMode lists**, which is a
symptom that looks like a code problem and is not one.

| Panels | FTC Dashboard | Result |
|---|---|---|
| yes | yes | crash loop |
| yes | **no** | **boots** |
| **no** | yes | **boots** |

Established on a Control Hub v1.0 (SDK 12.0, Sloth 0.3.2, Panels
`0.3.2+1.0.13`, Dashboard `0.3.2+0.6.0`) on 24 Sep 2026, with **no team-authored
Java in the APK at all** — so nothing of ours is involved. It survives a cold
boot, so it is not an install-time race. Full working in `SPIKE.md` and
`LADDER.md`, including the readings that were wrong on the way. They were
never merged; they live on the git tag `archive/panels-dashboard-ladder`
(`git show archive/panels-dashboard-ladder:SPIKE.md`).

**What is known about the mechanism:** port **8001** — Panels' documented port —
is bound and then requested again *inside a single Robot Controller process*
(`ps` shows one process, and 8001's lifetime tracks it). Neither artifact
declares a dependency on the other; both pull
`org.nanohttpd:nanohttpd-websocket:2.3.1` and both exclude the core `nanohttpd`,
expecting the SDK's bundled `fi.iki.elonen` classes — which is the class in the
crash stack. Beyond that it needs the maintainers, and it is worth reporting
upstream to Dairy Foundation (who build both Sloth variants) and bylazar.

**Why Dashboard rather than Panels was the one dropped:** seven files import
`com.bylazar.*`; one imported `com.acmerobotics.*`. The cost was one OpMode's
telemetry block against seven files of migration.

**What this costs, and it is not nothing:** the desktop AdvantageScope stream.
AdvantageScope reads Dashboard's packet stream, and with no Dashboard there is
no stream to read. That is a real loss and is not meant to be permanent — see
[AdvantageScope](#advantagescope).

## Deliberately excluded

- **NextFTC** — the previous command framework. Migrated off it onto Ivy;
  reintroducing it would mean maintaining two overlapping command schedulers.
- **`com.pedropathing:telemetry`** — only existed as a crutch for one
  unmigrated file in the source project this was based on. Don't carry it
  forward; if a file needs it, port that file to the current telemetry API
  instead of re-adding the dependency. (It still exists on Maven Central at
  `1.0.0` and was never updated for Pedro 3.)

  **Also, 25 Sep 2026:** this is where Pedro's `Drawing` class and
  `Follower.telemetryDebug()` live, which is the reason someone will reach for
  it. They are not worth the dependency: Panels ships a `PEDRO_PATHING` field
  preset, so the coordinate conversion is already available, and
  `util/FieldView.java` does the drawing in about forty lines. See [The Field
  view](#the-field-view-and-why-pedros-drawing-helper-is-not-how-we-get-there).
- **Road Runner / `maven.brott.dev`** — not in use; the maven repo isn't
  declared here to avoid an unused, unexplained entry.
- **Marrow** (`io.github.skeleton-army.marrow`) — a newer reactive-behavior
  library that layers on NextFTC/FTCLib/SolversLib. No evidence yet of use by
  competitive teams; worth revisiting, not a default include.
- **AdvantageScope Lite** (`page.j5155.AdvantageScope:lite`) — **excluded
  again, and this time on the merits.** It serves its UI from port 8080, which
  FTC Dashboard already binds; the loser throws `BindException`, the RC ANRs,
  and the robot never reaches ready. See
  [AdvantageScope](#advantagescope) for the full failure and what it looks like
  on the Driver Station.

  **History note, 25 Sep 2026.** This bullet used to end "Desktop AdvantageScope
  covers the same workflow with no port to fight over." That is no longer true:
  desktop AdvantageScope read FTC Dashboard's stream, and Dashboard is gone too.
  Neither *mechanism* in this bullet changed — the port collision is still real —
  but the consolation prize it offered does not exist. Panels' Graph and Capture
  views are the replacement. See [The Panels stack](#the-panels-stack-seeing-data-and-changing-values-at-a-meeting).
- **FTC Dashboard** (`com.acmerobotics.slothboard:dashboard`) — cannot coexist
  with Panels; the RC crash-loops on `BindException` with both installed. Panels
  won because seven files import `com.bylazar.*` and one imported
  `com.acmerobotics.*`. See [Why FTC Dashboard is not in the dependency
  set](#why-ftc-dashboard-is-not-in-the-dependency-set).
- **Desktop AdvantageScope** — not a dependency, but worth recording as a
  rejected *tool*: it consumed Dashboard's packet stream, so it fell with
  Dashboard. Its own documentation calls FTC support experimental and dates
  official support to the Systemcore transition in 2027–28, which is not this
  season. Settled 25 Sep 2026 rather than left open.
  *History:* this entry originally said to add it back "only if that debugging
  workflow is actually resumed"; #38 did exactly that, and #56 took it back out
  on the port collision. #56 was initially written up as the fix for a dead
  Control Hub — it was not; the RC kept crashing identically once it was gone.
  Both reversals are kept visible rather than overwritten. Re-adding it needs the
  port collision solved first.
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
