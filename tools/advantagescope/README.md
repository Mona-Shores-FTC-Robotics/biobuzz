# AdvantageScope for match logs: the robot model and the layout

`tools/pull-logs/` gets a `.wpilog` off the robot. This folder makes AdvantageScope show it well:
the team's robot drawn from its CAD on the 2026-2027 field, and a layout with the tabs a match
review needs. Both came from the simulator branch (`claude/simulator`), which built them for
simulated matches; this is the part that applies to a real log.

## Once per laptop

1. Install [AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope/releases)
   v27 alpha 6 or newer.
2. Double-click `setup-advantagescope.cmd`, or:

   ```bash
   powershell -NoProfile -ExecutionPolicy Bypass -File tools/advantagescope/setup-advantagescope.ps1
   ```

   It copies `cad/advantagescope/Robot_BIOBUZZ` into AdvantageScope's `userAssets` (replacing any
   older BIOBUZZ asset) and puts `match-layout.json` in `Downloads\biobuzz-match-layout.json`.
3. Start AdvantageScope. **File → Import Layout…** → that file. Run the script again whenever the
   model or the layout changes in git; it says when the layout needs importing again.

The field is AdvantageScope's own **2026-2027 Field**; it downloads it the first time a Field tab
asks for it, so the first open needs internet.

## Sending logs to someone without the repo

Double-click `make-viewer-zip.cmd`. It writes `Downloads\BIOBUZZ-AdvantageScope.zip`: the robot model as committed,
the match layout, `install.cmd`, and a `README.txt` that says which commit and CAD build it holds. Send that zip once
to anyone who should watch the logs. They unzip it, double-click `install.cmd` and import the layout, and from then on
any `.wpilog` you send them plays as-is, robot and moving parts included.

The model changes when the CAD does. After a rebuild (`cad/advantagescope/from_onshape.py`) or a layout change, make a
new zip and send it; older logs still play with it.

## The layout

| Tab | Shows |
|---|---|
| **Field** (2D) | `/Odometry/Robot`, the Auto's full path in grey (`/Path/Full`), the path being driven in yellow (`/Path/Active`) and its end as a ghost (`/Path/Target`). Turn on the trail in the tab's options to see where the robot went. |
| **3D** | The same pose as **BIOBUZZ Robot** (the CAD) on the 3D field, `/Odometry/Robot3d`. |
| **Limelight** | The 3D field seen from the Limelight's lens, as mounted on the model (right-click → Orbit Field to leave). |
| **Loop** | `/Robot/LoopMs` and the `/Loop/*` split on the left, battery volts on the right, with the robot mode shaded. |
| **Launcher** | Target, left and right flywheel speed and the feeder's on the left; their currents on the right. |

Add a Table tab with `/Events` for the Auto's card-by-card lines, and the Joysticks tab for
`/DriverStation/Joystick0`; AdvantageScope keeps what you add when you save the layout.

## The model

`cad/advantagescope/Robot_BIOBUZZ/` is the robot as the Onshape CAD shows it, built by
`cad/advantagescope/from_onshape.py` straight from the mentor's Robot assembly. The turret turns and
the FLOWER stick swings: the match log writes both as `/Internals/Components`, and both 3D tabs show
them (`cad/advantagescope/README.md`, "Moving parts"). Its
origin is on the floor under the middle of the four drive axles; if the robot sits off its logged
pose by a fixed offset, that is the point to check against what Pedro tracks.
