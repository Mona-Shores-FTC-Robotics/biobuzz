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

   It copies `cad/advantagescope/Robot_BIOBUZZ` into AdvantageScope's `userAssets` (replacing its
   older copy and nothing else) and puts `match-layout.json` in `Downloads\biobuzz-match-layout.json`.
   It never removes the simulator's assets: they are built on the laptop and can't be put back from here.
3. Start AdvantageScope. **File → Import Layout…** → that file. Run the script again whenever the
   model or the layout changes in git; it says when the layout needs importing again.

The 2D field is AdvantageScope's own **2026-2027 Field**; it downloads it the first time a Field tab
asks for it, so the first open needs internet. The 3D tabs use the simulator's **2026-2027 Field (HIVE
sim)** and **BIOBUZZ HIVE**, which are cut from that field on each laptop (no FIRST CAD is committed):
build them once with the simulator branch's `tools\advantagescope\watch-biobuzz.cmd` (`claude/simulator`),
or install a viewer zip that carries them. The setup script says when they are missing.

## Sending logs to someone without the repo

Double-click `make-viewer-zip.cmd`. It writes `Downloads\BIOBUZZ-AdvantageScope.zip`: the robot model as committed,
this laptop's HIVE field and HIVE (when it has them; it says when it doesn't), the match layout, `install.cmd`, and a
`README.txt` that says which commit and CAD build it holds. Send that zip once
to anyone who should watch the logs. They unzip it, double-click `install.cmd` and import the layout, and from then on
any `.wpilog` you send them plays as-is, robot and moving parts included.

The model changes when the CAD does. After a rebuild (`cad/advantagescope/from_onshape.py`) or a layout change, make a
new zip and send it; older logs still play with it.

## The layout

| Tab | Shows |
|---|---|
| **Field** (2D) | `/Odometry/Robot`, the Auto's full path in grey (`/Path/Full`), the path being driven in yellow (`/Path/Active`) and its end as a ghost (`/Path/Target`). Turn on the trail in the tab's options to see where the robot went. |
| **3D** | The same pose as **BIOBUZZ Robot** (the CAD) on the 3D field, `/Odometry/Robot3d`, with both HIVEs as the robot believed them (below). |
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

## The HIVEs: what the robot believed

The match log draws both alliances' HIVEs as the robot's `HiveTracker` had them, not as they were:
`/Hive/Components` turns each rocker of **BIOBUZZ HIVE** (at the origin, `/Hive/Structure`) about the
axle (`logging/HiveComponents`). Beside a match video, that shows what the robot knew.

- **Seen settled**: drawn at that end stop.
- **Tipping**: swung from one end stop to the other over the tracker's assumed TIP time
  (`HiveTracker.Tuning.tipSeconds`); if the camera sees it settle sooner, it jumps there.
- **Assumed**: a TIP that finished out of view; drawn at the other end stop.
- **Unseen**: drawn where the robot last had it. It is not a claim it is still there.

To tell those apart, graph `/Hive/Red/State` and `/Hive/Blue/State` (0 right CELL up, 1 tipping,
2 left CELL up, 3 unseen) and `/Hive/Red/Assumed`, `/Hive/Blue/Assumed`; every change is also an
`/Events` line. A simulated log writes the same keys from the simulator's own HIVE, so one layout
plays both.
