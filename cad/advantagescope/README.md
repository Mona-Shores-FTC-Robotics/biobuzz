# The whole robot for AdvantageScope

`Robot_BIOBUZZ/` is the team's robot from its Onshape CAD, with the decided front (`cad/intake-b/`): the outer wheel
plates, the odometry pods, the Rigid V plates, and two articulated components: the FLOWER extractor, and the floating
roller with its motor. It is the robot the simulator's logs draw: `tools/advantagescope/setup-advantagescope.ps1` installs it
(the root README's *Watch a match*), the layout uses it for `/Odometry/Robot3d`, and a log of the baseline design
("rigid V") poses its components (`AutoSim.putShape`, `AutoSim.cadComponents`: the extractor swings down on the approach
to a FLOWER and up as the robot leaves, `Extractor/Down` in the log; the turret turns to the CELL while the launcher
spins; the roller and the J arm stay at rest). The generated model of
every other simulated design is **BIOBUZZ Robot (designs)**, from `RobotAssets`; pick it in that row's model menu for a log
of another design.

| File | What |
|---|---|
| `model.glb` | The robot and everything fixed: goBILDA's odometry pods and Limelight's own 3A (with `VENDOR_PKL`), the camera on a stand-in mount (a beam between the front towers' tops and a 45° wedge), and the transfer (`cad/transfer/`) at rest. Left out: the old intake roller and motor (the new ones replace them) and the NECTARs staged in the CAD (the simulator draws the pieces the robot holds). The transfer doesn't fit the launcher yet (`cad/transfer/README.md`) |
| `model_0.glb` | Component 0, the FLOWER extractor, drawn deployed |
| `model_1.glb` | Component 1, the roller's motor, carriage and float plates (not the roller), drawn down |
| `model_2.glb` | Component 2, what turns on the turret: the goBILDA bearing's inner race and its 176T gear (the hood joins it later), facing forward. It turns about +Z through the inner race's centre, (−0.07221, 0.00400) m. The designer's launcher under it is fixed: it shoots up into the hood |
| `model_3.glb`, `model_7.glb` | Components 3 and 7, the front and rear feeders (66.7 mm foam wheels across the robot under the flywheels, with their shafts and pulleys). Each spins about its axle; `extractor_poses.json` gives the axis and which way feeds |
| `model_4.glb` | Component 4, the intake roller: its shaft, wheels and pulleys. It spins about its axle (+Y through X 8.56 in, z 3.35 in at rest) and floats with component 1 |
| `model_5.glb`, `model_6.glb` | Components 5 and 6, the launcher's left (+Y) and right (−Y) flywheel axles, each two 96 mm Gecko wheels with their shaft, hub and 41T pulley. Axles along X at Y +3.643 / −3.328 in, z 6.646 in |
| `config.json` | FTC robot, `disableSimplification` (see below), no rotations, eight components, and the Limelight as a camera: lens on the centreline 4.0 in ahead of the origin and 14.0 in up, pitched 45° up (`[y: −45, z: 0]`), Limelight 3A, 640 × 480, 54.5° |
| `extractor_poses.json` | The extractor's component pose, deployed and stowed, and the roller's |

**Frame:** +X forward, +Y left, +Z up, metres, origin on the floor under the chassis frame's centre (7.56 in behind the
front face). That's the point the simulator's `RobotAssets` assumes Pedro tracks; if Pedro's pose is elsewhere,
change `CENTRE_BACK_IN` in `build_model.py` and rebuild.

**The extractor's pose:** it turns about its own shaft, an axis along +Y through (0.2530, 0, 0.1143) m (2.4 in ahead
of the face, 4.5 in up). Front up is a rotation about +Y by −angle: 0° deployed, 146° stowed. For an angle θ the component pose is that rotation about the
pivot, i.e. translation = pivot − R·pivot. `extractor_poses.json` has both ends.

**The roller's pose:** a translation of (0, 0, rise), with rise from 0 (down, as drawn) to 0.033 m (1.3 in). It lifts
when a NECTAR passes under it.

The rear FLOWER scorer is dropped (FLOWER scorer chat, 6 Oct 2026). A front NECTAR-capping assist is being looked at; it
would be component 2 when its outline exists.

**What AdvantageScope needs from the files** (both learnt the hard way on 6 Oct 2026, from its `OptimizeGeometries.ts`): every mesh must carry vertex normals, or it is dropped silently, and the config sets `disableSimplification`, or small parts are culled by rendering mode. `build_model.py` does both; check a rebuilt model with a glTF viewer before committing.

**Rebuild:** `VENDOR_PKL=vendor_mesh.pkl python3 cad/advantagescope/build_model.py keep.pkl front_mesh.pkl [addons_mesh.pkl [transfer_mesh.pkl]]`, where `keep.pkl` now comes from `python3 cad/full-robot/build.py --mesh Robot.step keep.pkl` (the mentor's CAD lined up and edited as the full-robot STEP has it). `VENDOR_PKL` comes from `cad/full-robot/real_parts.py`. The first
comes from `tools/robot-cad/slim.py` run on the robot's STEP (in Drive), the second from `cad/intake-b/build.py` with
`MESH_OUT` set, the third (for the real odometry pods) from `cad/robot-addons/build.py` with `POD_DIR` and `MESH_OUT`.

**The same robot as a STEP:** `cad/full-robot/` builds one STEP of the whole robot as drawn, in this model's frame
(millimetres). The file is too big for git; it lives in the team's Drive (`biobuzz/BIOBUZZ-robot.step.xz`).
