# The whole robot for AdvantageScope

`Robot_BIOBUZZ/` is the team's robot from its Onshape CAD, with the decided front (`cad/intake-b/`): the outer wheel
plates, the odometry pods, the Rigid V plates, the transfer, and eight articulated components (0 to 7). Copy the
folder into AdvantageScope's custom assets folder and pick "BIOBUZZ Robot".

| File | What |
|---|---|
| `model.glb` | The robot and everything fixed: goBILDA's odometry pods and Limelight's own 3A (with `VENDOR_PKL`), the camera on its goBILDA mount (from `VENDOR_PKL`; without its mount meshes, a stand-in: a beam between the front towers' tops and a 45° wedge), and the transfer (`cad/transfer/`) at rest, with the launcher's changes (the moved 6000 RPM flywheel motors, the turret's motor and belt). The feeder's yoke and the gate servo are here, swung in: the yoke's swing isn't animated. Left out: the old intake roller and motor (the new ones replace them) and the NECTARs staged in the CAD (the simulator draws the pieces the robot holds) |
| `model_0.glb` | Component 0, the FLOWER extractor, drawn deployed |
| `model_1.glb` | Component 1, the roller's motor, carriage and float plates (not the roller), drawn down |
| `model_2.glb` | Component 2, what turns on the turret: the goBILDA bearing's inner race and its 176T gear (the hood joins it later), facing forward. It turns about +Z through the inner race's centre, (−0.05189, 0.00400) m (with the launcher 0.8 in forward). The designer's launcher under it is fixed: it shoots up into the hood |
| `model_3.glb` | Component 3, the transfer's feeder: two 72 mm goBILDA Gecko wheels, their shaft and 24T pulley, along X on the left under the flywheels (Y 2.87 in, z 3.30 in, swung in). It only spins about its axle (it is belted to the left flywheel, so it spins whenever the flywheels do); `extractor_poses.json` gives the axis and which way feeds |
| `model_4.glb` | Component 4, the intake roller: its shaft, wheels and pulleys. It spins about its axle (+Y through X 8.62 in, z 3.4 in at rest) and floats with component 1 |
| `model_5.glb`, `model_6.glb` | Components 5 and 6, the launcher's left (+Y) and right (−Y) flywheel axles, each two 96 mm Gecko wheels with their shaft, hub and 41T pulley. The left one's shaft is the transfer's longer one, with its spacers and the 16T that drives the feeder. Axles along X at Y +3.643 / −3.328 in, z 6.646 in |
| `model_7.glb` | Component 7, the transfer's sprung pad opposite the feeder: plate and foam, hinged along X at its foot (Y −1.90 in, z 1.62 in). It swings out about 29° for a NECTAR |
| `config.json` | FTC robot, no rotations, eight components, and the Limelight as a camera: lens on the centreline 5.58 in ahead of the origin and 14.25 in up (where the goBILDA mount puts it, from `real_parts.limelight_lens_in()`), pitched 45° up (`[y: −45, z: 0]`), Limelight 3A, 640 × 480, 54.5° |
| `extractor_poses.json` | The poses and axes of every component: the extractor's pose deployed and stowed, and each other component's axis and travel |

**Frame:** +X forward, +Y left, +Z up, metres, origin on the floor under the chassis frame's centre (7.56 in behind the
front face). That's the point the simulator's `RobotAssets` assumes Pedro tracks; if Pedro's pose is elsewhere,
change `CENTRE_BACK_IN` in `build_model.py` and rebuild.

**The extractor's pose:** it turns about its own shaft, an axis along +Y through (0.2530, 0, 0.1143) m (2.4 in ahead
of the face, 4.5 in up). Front up is a rotation about +Y by −angle: 0° deployed, 146° stowed. For an angle θ the component pose is that rotation about the
pivot, i.e. translation = pivot − R·pivot. `extractor_poses.json` has both ends.

**The roller's pose:** a translation of (0, 0, rise), with rise from 0 (down, as drawn) to 0.033 m (1.3 in). It lifts
when a NECTAR passes under it.

The rear FLOWER scorer is dropped (FLOWER scorer chat, 6 Oct 2026).

**Rebuild:** `VENDOR_PKL=vendor_mesh.pkl python3 cad/advantagescope/build_model.py keep.pkl front_mesh.pkl [addons_mesh.pkl [transfer_mesh.pkl]]`. One source per argument:
- `keep.pkl`: `python3 cad/full-robot/build.py --mesh Robot.step keep.pkl` (the mentor's CAD lined up and edited as
  the full-robot STEP has it);
- `front_mesh.pkl`: `cad/intake-b/build.py` with `MESH_OUT` set;
- `addons_mesh.pkl` (the real odometry pods, without `VENDOR_PKL`): `cad/robot-addons/build.py` with `POD_DIR` and
  `MESH_OUT`;
- `transfer_mesh.pkl`: `cad/transfer/build.py` with `MESH_OUT` set;
- `VENDOR_PKL`: `python3 cad/full-robot/real_parts.py <example chassis STEP> <LIMELIGHT3ACAD_STEP.stp> vendor_mesh.pkl
  [VENDOR_DIR]` (with `VENDOR_DIR` it adds the camera's goBILDA mount).

**The same robot as a STEP:** `cad/full-robot/` builds one STEP of the whole robot as drawn, in this model's frame
(millimetres). It is too big for git; the team's Drive holds it as two zipped files in `biobuzz/`:
`BIOBUZZ-1-mentor-robot.step` and `BIOBUZZ-2-our-parts.step` (`cad/full-robot/ONSHAPE.md`).
