# The whole robot for AdvantageScope

`Robot_BIOBUZZ/` is the team's robot from its Onshape CAD, with the decided front (`cad/intake-b/`): the outer wheel
plates, the odometry pods, the Rigid V plates, and two articulated components: the FLOWER extractor, and the floating
roller with its motor. Copy the folder into AdvantageScope's custom assets folder and pick "BIOBUZZ Robot".

| File | What |
|---|---|
| `model.glb` | The robot and everything fixed. The old intake roller and motor are left out: the new ones replace them |
| `model_0.glb` | Component 0, the FLOWER extractor, drawn deployed |
| `model_1.glb` | Component 1, the roller, its motor and carriage, drawn down |
| `config.json` | FTC robot, no rotations, two components, and the Limelight as a camera: lens on the centreline 4.0 in ahead of the origin and 14.0 in up, pitched 45° up (`[y: −45, z: 0]`), Limelight 3A, 640 × 480, 54.5° |
| `extractor_poses.json` | The extractor's component pose, deployed and stowed, and the roller's |

**Frame:** +X forward, +Y left, +Z up, metres, origin on the floor under the chassis frame's centre (7.56 in behind the
front face). That's the point the simulator's `RobotAssets` assumes Pedro tracks; if Pedro's pose is elsewhere,
change `CENTRE_BACK_IN` in `build_model.py` and rebuild.

**The extractor's pose:** it turns about its own shaft, an axis along +Y through (0.2530, 0, 0.1143) m (2.4 in ahead
of the face, 4.5 in up). Front up is a rotation about +Y by −angle: 0° deployed, 150° stowed. For an angle θ the component pose is that rotation about the
pivot, i.e. translation = pivot − R·pivot. `extractor_poses.json` has both ends.

**The roller's pose:** a translation of (0, 0, rise), with rise from 0 (down, as drawn) to 0.033 m (1.3 in). It lifts
when a NECTAR passes under it.

The rear FLOWER scorer is dropped (FLOWER scorer chat, 6 Oct 2026). A front NECTAR-capping assist is being looked at; it
would be component 2 when its outline exists.

**Rebuild:** `python3 cad/advantagescope/build_model.py keep.pkl front_mesh.pkl [addons_mesh.pkl]`. The first
comes from `tools/robot-cad/slim.py` run on the robot's STEP (in Drive), the second from `cad/intake-b/build.py` with
`MESH_OUT` set, the third (for the real odometry pods) from `cad/robot-addons/build.py` with `POD_DIR` and `MESH_OUT`.
