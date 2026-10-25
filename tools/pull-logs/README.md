# Pull match logs: robot → laptop → AdvantageScope

Every OpMode run writes a `.wpilog` to `/sdcard/FIRST/logs/` on the hub (`RobotOpMode`,
`logging/MatchLog`). This folder gets them onto the laptop and open in AdvantageScope without
anyone typing an adb command.

## At a meeting

1. Join the robot's Wi-Fi on the laptop that deploys.
2. Double-click `watch-logs.cmd`. It opens its own window, which is the point: a watcher started
   in Android Studio's terminal dies when Android Studio closes, and Android Studio has to close
   while the robot runs (below). From a standalone PowerShell window it is the same thing:

   ```bash
   powershell -NoProfile -ExecutionPolicy Bypass -File tools/pull-logs/Pull-MatchLogs.ps1 -Watch
   ```

3. Leave the window open. Every time an OpMode stops, its log is copied to
   `%USERPROFILE%\biobuzz-logs\<robot network>\` and AdvantageScope opens it a few seconds later.
   An Autonomous that hands off to TeleOp opens twice: after the Auto, and again with the whole
   match once TeleOp stops.

`pull-logs.cmd` is the one-shot version: connect, pull what is new, open the newest, exit.

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
holding the hub rather than starting a rival: REV Hardware Client's if that is open, Android
Studio's if that is. With nothing running it starts Android Studio's
(`%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`). `-Adb <path>` overrides. The first line it
prints says which one it picked.

**Open REV Hardware Client 2 before anything else that talks adb.** Seen 25 Oct on 20245: with
Android Studio's own adb server holding the hub, the hub's adb daemon wedged within a minute of
the watcher joining (adb listed the hub `offline`, REV Hardware Client said "debug connection
unavailable, power cycle needed"), and only a power cycle brought it back. With REV Hardware
Client open first, its adb server owned the hub, Android Studio connected instantly through it,
and the watcher ran beside both for the rest of the session. The watcher sides with whichever
adb server is already running, so the order is what matters:

1. REV Hardware Client 2, and leave it open.
2. Android Studio.
3. The watcher, in its own window (`watch-logs.cmd`).

Sloth Load in that arrangement has not been checked yet; if it fails, stop the watcher for the
deploy and start it again after.

If the hub does wedge: close everything that talks adb, power-cycle the hub, then
`adb kill-server`, `adb connect 192.168.43.1:5555`, and `adb devices` must say `device`, not
`offline`, before starting the watcher.

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
