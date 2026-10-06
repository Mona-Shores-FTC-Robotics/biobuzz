# The whole robot for AdvantageScope

`Robot_BIOBUZZ/` is the team's robot from its Onshape CAD, with the decided front (`cad/intake-b/`): the outer wheel
plates, the 14 in roller and its motor, the odometry pods, the Rigid V plates, and the FLOWER extractor as an
articulated component. It is the robot the simulator's logs draw: `tools/advantagescope/setup-advantagescope.ps1` installs it
(the root README's *How to watch*), the layout uses it for `/Odometry/Robot3d`, and a log of the baseline design
("rigid V") poses its extractor (`AutoSim.putShape`: stowed through AUTO). The generated model of every other simulated
design is **BIOBUZZ Robot (designs)**, from `RobotAssets`; pick it in that row's model menu for a log of another design.

| File | What |
|---|---|
| `model.glb` | The robot and everything fixed. The old intake roller is left out: the new one replaces it |
| `model_0.glb` | Component 0, the FLOWER extractor, drawn deployed |
| `config.json` | FTC robot, `disableSimplification` (without it AdvantageScope decimates the model and drops its meshes: the robot came out blank), no rotations, one component, and the Limelight as a camera: lens on the centreline 4.0 in ahead of the origin and 14.0 in up, pitched 45° up (`[y: −45, z: 0]`), Limelight 3A, 640 × 480, 54.5° |
| `extractor_poses.json` | The extractor's component pose, deployed and stowed |

**Frame:** +X forward, +Y left, +Z up, metres, origin on the floor under the chassis frame's centre (7.56 in behind the
front face). That's the point the simulator's `RobotAssets` assumes Pedro tracks; if Pedro's pose is elsewhere,
change `CENTRE_BACK_IN` in `build_model.py` and rebuild.

**The extractor's pose:** it turns about the roller's axle, an axis along +Y through (0.2174, 0, 0.0850) m. Front up is
a rotation about +Y by −angle: 0° deployed, 125° stowed. For an angle θ the component pose is that rotation about the
pivot, i.e. translation = pivot − R·pivot. `extractor_poses.json` has both ends.

**Complete for now.** The rear FLOWER scorer is dropped (FLOWER scorer chat, 6 Oct 2026), so there is no component 1. A front
NECTAR-capping assist is being looked at; it would be added as a component when its outline exists.

**Rebuild:** `python3 cad/advantagescope/build_model.py keep.pkl intakeb_mesh.pkl [addons_mesh.pkl]`. The first
comes from `tools/robot-cad/slim.py` run on the robot's STEP (in Drive), the second from `cad/intake-b/build.py` with
`MESH_OUT` set, the third (for the real odometry pods) from `cad/robot-addons/build.py` with `POD_DIR` and `MESH_OUT`.
