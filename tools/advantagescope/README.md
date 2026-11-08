# AdvantageScope for match logs: the robot model and the layout

`tools/pull-logs/` gets a `.wpilog` off the robot. This folder makes AdvantageScope show it well:
the team's robot drawn from its CAD on the 2026-2027 field, and a layout with the tabs a match
review needs. Both came from the simulator branch (`claude/simulator`), which built them for
simulated matches; this is the part that applies to a real log.

## Once per laptop, and watching a match

**`replay/`** does it all: one pasted line installs the pinned AdvantageScope, the robot and HIVE models, this
layout (with the Video tab beside a 3D window, no import) and the event's logs; then `open-match Q-14` opens a
match with its video synced. See [`replay/README.md`](replay/README.md). It replaces the old
`setup-advantagescope` and `make-viewer-zip` scripts: the models zip it installs is the one to send anyone without
the repository, and it is published as a GitHub release by the Replay assets workflow, so nobody builds the HIVE
on their laptop or needs Android Studio for it.

The 2D field is AdvantageScope's own **2026-2027 Field**; it downloads it the first time a Field tab asks for it,
so the first open needs internet. The 3D tabs use the simulator's **2026-2027 Field (HIVE sim)** and **BIOBUZZ
HIVE**, cut from that field (no FIRST CAD is committed) by `HiveAssets` in `replay/build/build-assets.sh`.

## The layout

| Tab | Shows |
|---|---|
| **Field** (2D) | `/Odometry/Robot`, the Auto's full path in grey (`/Path/Full`), the path being driven in yellow (`/Path/Active`) and its end as a ghost (`/Path/Target`). Turn on the trail in the tab's options to see where the robot went. |
| **3D** | The same pose as **BIOBUZZ Robot** (the CAD) on the 3D field, `/Odometry/Robot3d`, with both HIVEs as the robot believed them (below). A simulated log also draws the simulator's HIVE (`/Sim/Hive/*`), its POLLEN and NECTAR rolling (`/Sim/GamePieces/*`), and the partner and opponents as ghosts; each source draws only when its key is in the log, so one layout plays both kinds. |
| **Video** | The match video, synced to the log by `replay/open-match`. The 3D tab opens in its own window beside it. |
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
