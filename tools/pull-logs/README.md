# Pull match logs: robot → laptop → AdvantageScope

Every OpMode run writes a `.wpilog` to `/sdcard/FIRST/logs/` on the hub (`RobotOpMode`,
`logging/MatchLog`). This folder gets them onto the laptop and open in AdvantageScope without
anyone typing an adb command.

## At a meeting

1. Join the robot's Wi-Fi on the laptop that deploys, with REV Hardware Client 2 open (see
   "What it does about adb" for why that order).
2. Whenever you want to look, double-click `pull-logs.cmd`. It connects, copies every log the
   laptop does not have yet into `%USERPROFILE%\biobuzz-logs\<robot network>\`, opens the newest
   in AdvantageScope, and exits. Once at the end of the meeting gets everything; the logs wait
   on the hub until then.

From a terminal it is:

```bash
powershell -NoProfile -ExecutionPolicy Bypass -File tools/pull-logs/Pull-MatchLogs.ps1
```

`watch-logs.cmd` is the live version for when someone wants each log on screen seconds after
the OpMode stops: it keeps running in its own window and opens each log as it finishes. An
Autonomous that hands off to TeleOp opens twice: after the Auto, and again with the whole match
once TeleOp stops. Optional; the one-shot pull is the normal way.

| Flag | What |
|---|---|
| `-Watch` | keep running; open each log as it finishes |
| `-NoOpen` | copy only |
| `-RemoveFromRobot` | delete from the hub what has been copied (never the newest file) |
| `-Robot 192.168.49.1` | a phone Robot Controller instead of a Control Hub |
| `-Dest <folder>` | somewhere other than `%USERPROFILE%\biobuzz-logs` |

## What it does about adb

The connection problems `TeamCode/README.md` ("When a Wi-Fi deploy fails") describes are handled
the way that section says, so the watcher survives a meeting:

- **Sloth Load disconnects Wi-Fi adb after every deploy** (a bare `adb disconnect` in Sloth's
  code). The watcher notices on its next tick and reconnects. Nothing to do.
- **A stale `offline` entry** after the hub rebooted or the laptop slept: it disconnects that one
  address and connects again. It never runs a bare `adb disconnect`, which would also drop a
  robot on USB.
- **"adb server version doesn't match this client"**: two copies of adb. It restarts the server
  once and tries again. If that keeps happening, close the other adb (REV Hardware Client, an old
  SDK) rather than fighting it.
- **Nothing answers at the address**: the laptop is not on the robot's network. It says so
  instead of hanging in `adb connect`.

It uses whichever adb already has a server running on the laptop, so it joins the adb that is
holding the hub rather than starting a rival, unless that adb cannot be run from outside its
app (REV Hardware Client 2 installed from the Microsoft Store keeps its adb under
`C:\Program Files\WindowsApps`, which Windows refuses to run: "Access is denied"); then, and
with nothing running, it uses Android Studio's (`%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`),
which talks to REV's server all the same. `-Adb <path>` overrides. The first line it prints says
which one it picked.

**Open REV Hardware Client 2 before anything else that talks adb.** Seen 25 Oct on 20245: with
Android Studio's own adb server holding the hub, the hub's adb daemon wedged within a minute of
the watcher joining (adb listed the hub `offline`, REV Hardware Client said "debug connection
unavailable, power cycle needed"), and only a power cycle brought it back. With REV Hardware
Client open first, its adb server owned the hub, Android Studio connected instantly through it,
and the watcher ran beside both for the rest of the session. The watcher sides with whichever
adb server is already running, so the order is what matters:

1. REV Hardware Client 2, and leave it open.
2. Android Studio.
3. The pull (`pull-logs.cmd`) whenever wanted, or the watcher in its own window
   (`watch-logs.cmd`) if someone wants logs live.

Sloth Load in that arrangement has not been checked yet; if it fails, stop the watcher for the
deploy and start it again after.

If the hub does wedge: close everything that talks adb, power-cycle the hub, then
`adb kill-server`, `adb connect 192.168.43.1:5555`, and `adb devices` must say `device`, not
`offline`, before starting the watcher.

## Reading a log in AdvantageScope

- **First, once per laptop:** `tools/advantagescope/` installs the team's robot model and a match
  layout with the Field, 3D, Limelight, Loop and Launcher tabs already set up. With it, the rest
  of this list is already on screen.
- **Where the robot was**: drag `/Odometry/Robot` onto a 2D or 3D Field. Pick the 2026-27 FTC
  field in the tab's settings, and `BIOBUZZ Robot` as the robot model.
  `/Odometry/PedroInches` is the same pose in Pedro's own numbers.
- **What the Auto planned**: `/Path/Full` (every path of the Auto, grey in the layout) and
  `/Path/Active` (the one being driven, yellow) as trajectories on the 2D Field, `/Path/Target`
  (where it ends) as a ghost. Written by `BuiltAuto` as the Auto is built and as each path starts
  and ends; a TeleOp-only log has none of them.
- **What the Auto did**: `/Events` on a timeline or in the Table. Every `auto: …` line is a card
  starting, a wait ending and why, or the endgame guard's decision, exactly as the Match page
  showed them.
- **Where the time went**: `/Loop/*` on a Line Graph. `/DriverStation/RobotMode` shows the
  Auto / disabled / TeleOp stretches.
- **The gamepads**: the Joysticks tab, `/DriverStation/Joystick0` and `1`.
- **Which robot and config**: the metadata pane (`/RealMetadata/RobotConfig`, `OpMode`, `MatchId`).

## Why a script here, and not something on the robot

Panels is the live dashboard and stays the only one. This is the post-run review path: AdvantageScope
does the 3D field, joystick tab and event timeline that Panels does not, and reading a file after
the run costs the robot nothing. An on-robot AdvantageScope server is ruled out in
`TeamCode/README.md` (its port collides and the RC crash-loops).

Why PowerShell: it is on every Windows laptop the team deploys from, needs no install, and the
only tool it drives is adb. A Python or Node script would add a runtime to the laptop checklist.

Decided in #214.
