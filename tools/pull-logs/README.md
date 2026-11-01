# Pull match logs: robot → laptop → AdvantageScope

Every OpMode run writes a `.wpilog` to `/sdcard/FIRST/logs/` on the hub (`RobotOpMode`,
`logging/MatchLog`). This folder gets them onto the laptop and open in AdvantageScope without
anyone typing an adb command.

## At a meeting

1. Join the robot's Wi-Fi on the laptop that deploys, with REV Hardware Client 2 open (see
   "What it does about adb" for why that order).
2. Whenever you want to look, double-click `pull-logs.cmd`. It connects, copies every log the
   laptop does not have yet into the team's Google Drive folder (below), opens the newest in
   AdvantageScope, and exits. Once at the end of the meeting gets everything; the logs wait on
   the hub until then.

In a PowerShell window in this folder, type `.\pull-logs.cmd` (PowerShell does not run a file
from the current folder without the `.\`). From anywhere in the repo it is:

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
| `-RemoveFromRobot` | delete from the hub what has been copied to this laptop, before it is on GitHub (never the newest file) |
| `-KeepOnRobot` | do not delete from the hub what has reached GitHub (deleting is the default) |
| `-Robot 192.168.49.1` | a phone Robot Controller instead of a Control Hub |
| `-Dest <folder>` | somewhere other than the Drive folder |

## Sent to GitHub, so a Claude session can read them

After pulling, the script sends today's logs (by the date in the file name) to
GitHub, one branch per day and robot: `logs/<date>-<robot network>`, e.g.
`logs/2026-11-01-20245-RC`. Then "pull logs" to a Claude session is enough: it fetches those
branches and reads them with `read_logs.py` (below). Nothing to type.

- **No internet on the robot's Wi-Fi?** Nothing is lost. The logs wait in the Drive folder and go up
  on the next run that has internet: double-click again at home or school, robot or no robot. A
  laptop with a second Wi-Fi adapter on the internet sends them straight away, and the watcher sends
  each log as it finishes.
- **Your checkout is never touched.** The branch is written with git's plumbing (a private index,
  `commit-tree`, `push <commit>:refs/heads/...`), not by switching branches, so whatever you have
  checked out and half-edited stays exactly as it was.
- A log that grows (an Auto, then its TeleOp) goes up again and replaces its earlier copy. What has
  gone up is listed in `%LOCALAPPDATA%\biobuzz-logs-published.txt`; delete a line to send that
  file again.
- These branches are throwaway: never merged, never based on `master`. Each log goes up gzipped
  (`<name>.wpilog.gz`, about a third of the size), so a long session fits under GitHub's 100 MB per
  file; one over 95 MB even compressed is skipped with a message.
- **Once a log is on GitHub it is deleted from the robot**, so the hub only holds what has not gone
  up and each pull copies just the new runs (a full hub took minutes to pull and slowed the robot's
  Wi-Fi on 1 Nov). Deleted only after the upload succeeded, at that file's exact size, and never
  the newest log, which may still be recording. `-KeepOnRobot` leaves everything on the hub.
- `-NoPublish` skips the upload (and so deletes nothing); `-PublishDays 2` also sends the two days before today (older logs are on Drive already).

`read_logs.py` reads a `.wpilog` with plain Python (standard library only): `summary` gives, per
run, the build, battery, every launch knob at PLAY, the key events and every feed (gate open:
launcher speed at the open and its lowest, feeder speed, shots counted, battery low). It is for
reading logs without AdvantageScope; nobody at the robot needs Python.

## Where logs go

Logs go to the team's shared Google Drive, so nobody has to fetch them from a particular laptop:

```
G:\Shared drives\Robotics Team 3572 - Wavelength\2026-2027 FTC Biobuzz\Logs\<robot network>\
```

That needs **Google Drive for Desktop** on the laptop, signed in as a member of the
`Robotics Team 3572 - Wavelength` shared drive. The script looks for the folder on every drive
letter, so it does not matter which one Drive mounts. While the laptop is on the robot's Wi-Fi it
has no internet; Drive holds the files and uploads them when the laptop is back on a network, so
check the folder on drive.google.com after a meeting before trusting `-RemoveFromRobot`.

A laptop without the folder (no Drive, or not a member) falls back to
`%USERPROFILE%\biobuzz-logs\<robot network>\` and says so in its first lines. Those logs are on
that laptop only; copy them into the Drive folder by hand, or pass `-Dest` next time.

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
- **The turret**: `/Turret/AngleDeg`, `TargetDeg`, `ErrorDeg` on a Line Graph; `Zeroed` says
  whether anyone zeroed it before PLAY.
- **Which robot, config and code, and what was attached**: the Metadata tab. `RobotConfig`,
  `OpMode`, `MatchId`; `Build` (branch and commit, `-dirty` for uncommitted edits);
  `Hardware/<Subsystem>` for what each found at init (the Pinpoint, the camera, the turret's
  zero; motors cannot be known and say so); `BatteryAtInit`; every Panels knob as
  `Tunable/<Class>.<field>` at PLAY; for an Auto, `AutoSource` (the `.pp` drawing) and
  `AutoPlaceholders`. A motor that was commanded and never moved is a line in `/Events`
  (`intake: commanded for 1.0 s, encoder never moved`, `launcher: …`), as is a low battery.

## Why a script here, and not something on the robot

Panels is the live dashboard and stays the only one. This is the post-run review path: AdvantageScope
does the 3D field, joystick tab and event timeline that Panels does not, and reading a file after
the run costs the robot nothing. An on-robot AdvantageScope server is ruled out in
`TeamCode/README.md` (its port collides and the RC crash-loops).

Why PowerShell: it is on every Windows laptop the team deploys from, needs no install, and the
only tool it drives is adb. A Python or Node script would add a runtime to the laptop checklist.

Decided in #214.
