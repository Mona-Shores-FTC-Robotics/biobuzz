# The Limelight 3A in the mentor's "Robot Copy 1" (25 Oct) — and the stowed FLOWER stick in its view

| File | What it is |
|---|---|
| `limelight_in_robot_copy1.step` | Limelight's own 3A model (4 bodies, 1.5 MB) at the planned camera position, in the exact frame of `Robot Copy 1.step`. In Onshape, insert it at the origin of that assembly and it lands on the robot. |
| `fov_side_view.png` | Side view: the camera, its vertical field of view, and the stowed stick. |
| `place_limelight.py` | Writes the two above and `numbers.txt`. It uses `real_parts.limelight_trsf` (the same call `build.py` places the camera with) and adds one more transform, model frame → Robot Copy 1. |
| `numbers.txt` | The script's printout (every number below). |

Rebuild: `python3 place_limelight.py LIMELIGHT3ACAD_STEP.stp [--parts parts.pkl] [--stick <dir of stick_*.stl>]`, with the
Limelight STEP from https://downloads.limelightvision.io/cad/LIMELIGHT3ACAD_STEP.stp and, optionally, `list_parts.py`'s
`parts.pkl` and `stick_mesh.py`'s meshes of the stick (both measured from Robot Copy 1; neither is in git).

## The camera position used

The plan is `build.py`'s (`limelight_mount` docstring, `real_parts.limelight_trsf` defaults): **lens 4.0 in ahead of the chassis
centre, on the centreline, 14.0 in above the floor, looking forward and pitched 45° up**, the camera the right way up with its
long side across the robot.

| Frame | Lens | Look direction |
|---|---|---|
| Model frame (`build.py`: +X forward, +Y left, +Z up, origin on the floor under the chassis centre) | (4.00, 0, 14.00) in | (0.707, 0, 0.707) |
| Intake-face frame (ahead of the intake uprights' front face, left of centre, above the floor) | **3.56 in behind the face, 0 left, 14.00 up** | 45° up |
| `Robot Copy 1.step` coordinates (mm, his file's own axes) | **(99.64, −43.80, 257.16)** | (0, −0.707, −0.707) |

Camera body (bounding box, face frame): 4.83 to 3.09 in behind the face, ±1.42 in of centre, 13.01 to 14.75 in up. No part of
his CAD has a bounding box that overlaps it (checked against `parts.pkl`, all 3013 parts). The mount is not drawn: the goBILDA
mast of `build.py` stood on his old 9-hole front channel and 35-hole L-beam, and whether Robot Copy 1 still has those was not
checked.

### How the frames line up (what was assumed)

- **Chassis centre = 7.56 in behind the intake face.** That is `build.py`'s convention (`real_parts.to_model`, `back_in=7.56`;
  README: "origin on the floor under the chassis centre (7.56 in behind the front face)"), so "4.0 in ahead of the centre" is
  3.56 in behind the face. It still fits this robot: from `parts.pkl` the front mecanum axles are 1.9 in behind the face and
  the rear ones 13.2 in, midpoint 7.55 in. `CameraMount.java` has every offset at 0.0, so the CAD plan is the only number.
- **His file is rotated 180° about x** compared with the frame the team CAD used to be in (x across, y up, z forward): in
  Robot Copy 1, +x is robot-left, +y is down, +z is backward. `list_parts.py` reads it after `build.placed_team`'s alignment
  (dx = −159.255 mm across, dz = −7.521 mm forward, from `list.txt`) as `forward_list = (z − (FACE − 7.56·IN))/IN`, with the
  intake uprights' front face (his `Chassis / Intake / 5 Hole Lowside U-Channel`, the min-z side) at `forward_list` 5.65, and
  the floor at `up_list` 18.25 (the mecanum wheels' bottoms land at 0.00 ± 0.04). So, in inches:
  `ahead = 5.65 − forward_list`, `left = (x + dx − C)/IN`, `up = 18.25 − (y − F)/IN`, with `C, F, FACE, IN` from
  `cad/robot-addons/build.py`. The script's `model_to_copy1()` is the inverse of that; the rotation's determinant is +1 (a
  proper rotation, no mirror), and the script prints the round trip (model +X → his −z, +Y → +x, +Z → −y).
- The alignment constants, the 5.65 and the 18.25 were taken from the 25 Oct `list.txt` / `stick_mesh.py`, not re-derived
  (reading the 162 MB STEP again takes 130 s and gives the same file). If the mentor exports again, the origin may move:
  rerun `list_parts.py` and update `ALIGN_DX`, `ALIGN_DZ` in the script.

## Field of view assumed

**Limelight 3A: 54.5° horizontal × 42° vertical** — Limelight's hardware comparison table,
https://docs.limelightvision.io/docs/docs-limelight/getting-started/hardware-comparison ("H:54.5° V:42°"), read 25 Oct 2026.
(The 3G and 4 are 82° × 56.2°; the 3A is the narrowest.) Pitched 45° up, the vertical window is **24° to 66° elevation**; the
horizontal half-angle is 27.25°.

## Is the stowed FLOWER stick in the way? Yes — the blade sits right over the lens.

Stick as stowed in Robot Copy 1 (face frame, from `stick_mesh.py`'s meshes; the blade's bounding box in `list.txt` is looser,
16.36–17.78 up, because OCC pads curved faces — the mesh is the real surface):

| Part | ahead of face | left | up |
|---|---|---|---|
| Printed blade (Part 4) | −3.57 to −0.31 | −4.36 to +3.51 | 16.58 to 17.06 |
| Shaft (312 mm REX) | −1.01 to −0.52 (centre −0.76) | −5.94 to +6.35 | 16.63 to 17.05 (centre 16.84) |
| Arms (11-hole mini channels) | −3.22 to +0.04 | 5.41 to 5.89 each side | 6.46 to 17.85 |
| Arm pivot (servo) | −2.83 to −2.04 | −5.35 to −3.53 | 6.84 to 8.97 |

Elevation angles from the lens (3.56 in behind the face, 14.0 up):

| | elevation |
|---|---|
| Top of the vertical FOV | **66.0°** |
| Optical axis | 45.0° |
| Bottom of the vertical FOV | 24.0° |
| Stowed blade, bottom-front edge (−0.31 ahead, 16.58 up) | **38.4°** |
| Stowed blade, bottom-back edge (−3.57 ahead, 16.58 up) | **90.2°** (directly above the lens) |
| Shaft centre (−0.76 ahead, 16.84 up) | 45.4° (on the optical axis) |
| Arms | nearest point is 45.8° off-axis *horizontally* (they are 5.4 in off centre and only ~5 in away), outside the ±27.25° horizontal FOV: **not in view** |

So the blade covers **everything from 38.4° up to the top of the window: 66% of the vertical FOV at the centreline**, and it
is wider (−4.36 to +3.51 in) than the frustum is at that range (±1.7 in), so it blocks the full width. A ray cast of the
54.5° × 42° frustum against the stick's meshes puts **65% of the image behind the stick**, the nearest surface 2.8 in from
the lens. The HIVE's tags, which the 45° pitch is for, are in the blocked part. The arms and the pivot servo are not.

### What would clear it (ray-cast fraction still blocked, camera moved alone)

| Move | 1 in | 2 in | 3 in | 4 in | 5 in | 6 in |
|---|---|---|---|---|---|---|
| Camera **forward** (toward the face) | 39% | 4% | 0% | | | |
| Camera **down** | 42% | 26% | 14% | 6% | 0% | |
| Camera **back** | 81% | 62% | 41% | 23% | 10% | 1% |
| Pitch instead (at the planned spot): 30° → 29%, 35° → 41%, 40° → 53%, 50° → 76%, 60° → 100% | | | | | | |

- **Back** makes it worse before better: the blade is seen from below until the camera is ≥ 6 in back (lens 9.6 in behind the
  face, inside the V2 launcher, whose turret is 6–8 in behind the face up to 14.4 in).
- **Down**: the blade's bottom-front edge rises above 66° only when the lens is below 9.3 in (4.7 in lower); the 45° line then
  crosses the face at 12.9 in up, through the intake.
- **Forward** is the one that works: with the lens ≥ 2.5 in forward (≤ 1.06 in behind the face) the whole blade is above the
  66° edge (bottom-front edge at 70°+) and the shaft at 76°+. The camera body is then 2.3 to 0.6 in behind the face, 13.0 to
  14.75 up, 1.8 in under the blade, and in his CAD nothing is there on the centreline (his Viper-slide belt plates reach 14.7 in
  up but only at ±4.9–5.3 in left). Our own front (the floating roller and its plates, `cad/intake-b/`) lives there too, and
  was not checked against it.
- **Lowering the stowed blade** instead: it would have to drop under the 24° bottom edge, i.e. below 15.45 in at its front edge
  and below 14.0 in at its back edge — 2.6 in lower at the back — and it would then rest on the camera body (top at 14.75 in).
  Not workable with the camera where it is. There is also no sideways gap: the blade spans 4.36 in right to 3.51 in left and
  the arms are at 5.4 in, so no 2.9 in wide camera fits beside the blade inside the arms.

## Not verified (no robot, mentor's assembly not opened)

- Whether `Robot Copy 1` keeps the 9-hole front channel and 35-hole L-beam that `build.py`'s goBILDA mast bolts to.
- The intake face as the reference: the 5-hole low-side uprights under `Chassis / Intake` are taken as the same uprights the
  older `Robot.step` was aligned by (the comment in `build.placed_team`).
- The FOV is the datasheet figure; the lens' exact principal point in the STEP is `real_parts.LL_LENS` (79.5, 8.8, 16.2) mm,
  taken from the model's glass, not from Limelight.
