# Pull match logs: robot → laptop → AdvantageScope

Every OpMode run writes a `.wpilog` to `/sdcard/FIRST/logs/` on the hub (`RobotOpMode`,
`logging/MatchLog`). This folder gets them onto the laptop and open in AdvantageScope without
anyone typing an adb command.

## At a meeting

1. Join the robot's Wi-Fi on the laptop that deploys.
2. Double-click `pull-logs.cmd`, or from a terminal:

   ```bash
   powershell -NoProfile -ExecutionPolicy Bypass -File tools/pull-logs/Pull-MatchLogs.ps1 -Watch
   ```

3. Leave the window open. Every time an OpMode stops, its log is copied to
   `%USERPROFILE%\biobuzz-logs\<robot network>\` and AdvantageScope opens it a few seconds later.
   An Autonomous that hands off to TeleOp opens twice: after the Auto, and again with the whole
   match once TeleOp stops.

Without `-Watch` it runs once: connect, pull what is new, open the newest, exit. That is the
double-click default.

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

It uses Android Studio's adb (`%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`), the same one
the deploy uses, so the two never kill each other's server.

## Reading a log in AdvantageScope

- **Where the robot was**: drag `/Odometry/Robot` onto a 2D or 3D Field. Pick the 2026-27 FTC
  field in the tab's settings (the FTC assets bundle; see `TeamCode/README.md` § AdvantageScope),
  and `BIOBUZZ Robot` as the robot model if `cad/advantagescope/` is in your assets folder.
  `/Odometry/PedroInches` is the same pose in Pedro's own numbers.
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
