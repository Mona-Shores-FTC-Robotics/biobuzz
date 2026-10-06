# The whole robot for AdvantageScope

`Robot_BIOBUZZ/` is the team's robot from its Onshape CAD, with the decided front (`cad/intake-b/`): the outer wheel
plates, the 14 in roller and its motor, the odometry pods, the Rigid V plates, and the FLOWER extractor as an
articulated component. Copy the folder into AdvantageScope's custom assets folder and pick "BIOBUZZ Robot".

| File | What |
|---|---|
| `model.glb` | The robot and everything fixed. The old intake roller is left out: the new one replaces it |
| `model_0.glb` | Component 0, the FLOWER extractor, drawn deployed |
| `config.json` | FTC robot, no rotations, one component. The Limelight camera goes in `cameras` when its mount is decided |
| `extractor_poses.json` | The extractor's component pose, deployed and stowed |

**Frame:** +X forward, +Y left, +Z up, metres, origin on the floor under the chassis frame's centre (7.56 in behind the
front face). That's the point the simulator's `RobotAssets` assumes Pedro tracks; if Pedro's pose is elsewhere,
change `CENTRE_BACK_IN` in `build_model.py` and rebuild.

**The extractor's pose:** it turns about the roller's axle, an axis along +Y through (0.2174, 0, 0.0850) m. Front up is
a rotation about +Y by −angle: 0° deployed, 125° stowed. For an angle θ the component pose is that rotation about the
pivot, i.e. translation = pivot − R·pivot. `extractor_poses.json` has both ends.

**To come:** component 1, the NECTAR/POLLEN FLOWER scorer (from the Pivoting arm nectar scorer chat), and the Limelight
camera (from the Limelight Localization chat).

**Rebuild:** `python3 cad/advantagescope/build_model.py keep.pkl intakeb_mesh.pkl [addons_mesh.pkl]`. The first
comes from `tools/robot-cad/slim.py` run on the robot's STEP (in Drive), the second from `cad/intake-b/build.py` with
`MESH_OUT` set, the third (for the real odometry pods) from `cad/robot-addons/build.py` with `POD_DIR` and `MESH_OUT`.
