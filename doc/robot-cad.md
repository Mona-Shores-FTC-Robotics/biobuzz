# The robot CAD: measured facts, and what the front of the robot has to share

From the team's Onshape export "DHS Robot Copy" (STEP AP242, 143 MB, kept in Drive, not git), read 6 Oct 2026 with
`tools/robot-cad/`. Everything here is read off the CAD, not a robot. Inches unless marked.

## Frame

The STEP is in millimetres: **x across (the robot's right is −x), y up, z forward**. Three reference numbers:

| | mm in the STEP |
|---|---|
| Robot centre line (midway between the side rails) | x = −59.62 |
| Floor (mecanum roller covers' lowest point; the CAD's NECTARs rest at the same height) | y = −151.75 |
| Front face (front end of both side rails and the front uprights) | z = 207.73 |

Anything made in this frame lands in place when imported into the robot's Onshape assembly
(`cad/robot-addons/dhs-addons.step` is).

## Size

| | |
|---|---|
| Length, front face to back of the wheels | 15.12 (384 mm rails). The CAD's bounding box is 15.7 because a NECTAR pokes out of the front |
| Width over the wheel shafts | 15.24 |
| Room in front for anything deployed (R105, 24 long) | 8.88 |
| Room in front when starting (18) | 2.88 |

## Drivetrain

- 96 mm mecanum wheels, axles 1.89 up, front axle 1.89 behind the front face, rear axle 13.23 behind it.
- Each wheel is held only by the inside rail today. Its timing pulley is outboard of the wheel, the belt runs
  up and back to the motor. `cad/robot-addons/` adds an outer plate per side (56 mm standoffs, 80 mm shafts).
- The front wheels are flush with the front face: nothing on the robot is in front of them.

## Intake as drawn

| | |
|---|---|
| Roller | 7 × 48 mm gecko wheels on a 240 mm shaft |
| Roller bottom above the floor | **2.53** (the intake study's `doc/intake-design.md` reads 2.84; see below) |
| Roller front | flush with the front face |
| Opening between the side rails | 9.76 (9.4 between the intake's side plates) |

**The 2.53 against 2.84:** measured from the floor above, which both the mecanum roller covers and the CAD's resting
NECTARs agree on. 2.84 is 0.31 higher; that matches the gap between those floor contacts and something higher
up on the wheel, so the difference is probably the floor reference. It decides whether the roller touches a
POLLEN (2.8 tall) at all. Lowering the roller to about 2.4, as the study recommends, is right either way.

## Mounting holes at the front

- **Front uprights** (the intake's 5-hole lowside channels standing on each rail at the front corners): a
  column of M4 holes on their front face, 8 mm apart, from 3.17 up to about 8.5, at 5.04 right and left of
  centre. These carry the ramp hook's bracket in `cad/robot-addons/`.
- **Side rails, outer wall:** M4 holes in rows, repeating every 24 mm along the rail. The outer plates' standoffs
  use the ones between the wheels.

## What the front of the robot has to share

The intake, the ramp hook and anything vectoring at the corners all want the same 15 in of front face, and the
corners most of all. Today's add-on hook (`cad/robot-addons/`) puts its hinge, hub and servo in front of the
**right front wheel**, 0.8 in out and 2.2 in up, and stows standing up in front of that corner (2.4 in deep, up
to about 9 in). That is the spot a 14 in roller's right end, a vectoring wheel or the Rigid V's flap would use.
So the hook should be designed with the intake, not bolted on beside it. What the FLOWER block needs, however
it is mounted:

- its bottom 0.7 in off the tiles, its top 1.35 in, about 1.4 in deep, with the curved front (`doc/ramp-hook.md`);
- to be driven against the FLOWER's grey uprights;
- a clear lane behind it, about 8 in in the hook's model, for 4 POLLEN to roll to the intake. 19705's version
  puts the block on the intake's own mouth with almost no lane, and lets the intake pull the POLLEN out.

## Tools

`tools/robot-cad/`: run in a folder holding the robot's `robot.step` (from Drive).

1. `holes.py` reads the STEP with names and placements (2 min) and lists the holes at the front.
2. `slim.py` and `slim3.py` (after converting the STEP to `robot.glb` with `cascadio`) make the light mesh for
   the 3D pages.
3. `occ.py` makes a 0.1 in occupancy grid of the robot for collision checks.
4. `sweep3.py` swings a simplified hook through its fold against that grid; `check.py` does the same with the
   real add-on parts from `cad/robot-addons/build.py` (set `MESH_OUT` there, `MESH` here).

`pip install cadquery cascadio trimesh fast_simplification scipy rtree`

## Fit checks for the intake study (`doc/intake-design.md`, option b)

Asked by the intake session, 6 Oct 2026. `tools/robot-cad/optb.py`, on the robot's 0.1 in grid.

**A 14 in roller across the front fits only in front of the robot.** At today's roller line (axle 0.95 in
behind the face) a 14 in roller runs into both front uprights, both front wheels and the intake's own plates.
With its bottom at 2.4 in and 48 mm wheels, it is clear once its axle is **1.0 in in front of the face**, so the
roller's front is **1.94 in out**. That costs:

- **Starting size:** 15.12 + 1.94 = 17.06 in of the 18, before anything stows in front of it.
- **The lane to the FLOWER block:** with the hook down at 24 in, the block's back edge is 7.48 in out, so the lane
  from the roller's front is about 5.5 in (7.4 today).
- **Its side plates:** they sit in front of the front wheels, where the outer wheel plates (`cad/robot-addons/`,
  inner faces 7.56 in from the centre) already are. Extending those plates forward and up carries the roller
  (15.1 in between them).

**The hook's hinge high on the side plate works at 6 in up, not 5, and it stows over the top, not upright.**
Hinge 1.0 in in front of the face, the arm between the roller's end and the plate (7.25 in out), the FLOWER block
at 24 in. Swept 0 to 180° in 10° steps:

| Hinge height | Clashes | Stowed |
|---|---|---|
| 5 in | front shaft and curtains hit the two tall front towers (`3700-0145-0288`) at 120-130°; the block hits the launcher at 180° | no clear stop |
| **6 in** | **none from 0 to 150°** except the curtains' tops grazing a tower at 130° (lower them 0.3 in, or hinge at 6.5) | **150°: 1.55 in in front of the face (inside the roller's 1.94), 14.5 in tall** |

Standing upright (90°) the hook sticks 5.3-6.3 in out the front, so it has to fold past vertical to about 150°,
leaning back over the intake; 180° (flat back) hits the launcher. A servo has the travel (150° of 300°). The worst
gravity load is with the hook's balance point level, about 9.7 kg·cm for a 430 g hook 8.9 in out: the Torque
servo still has 2.5×. Everything at the corner below 4 in (a vectoring wheel, the Rigid V's flap) stays free:
the hinge, hub and servo are all above 5.5 in.

**Option (b) drawn and checked: `cad/intake-b/`.** The STEP of the roller, the high-hinged hook and the V plates,
checked every 5° from 0 to 150°: clear. 24.0 in down, 17.3 in stowed, 17.8 in across with the V plates. Where it
had to differ from the request (the servo inboard, the arm at 7.15 in, the curtains ending at 4.7 in, the belt
inside the left plate) is in its README.
