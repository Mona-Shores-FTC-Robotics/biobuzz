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

## The FLOWER extractor (unified design, 6 Oct 2026)

The hook is no longer a spill catcher (the Rigid V is; `doc/unified-design.md` on `claude/biobuzz-robot-body-designs-hi386c`).
It is now a FLOWER extractor, and replaces the high-hinged one-arm hook in `cad/intake-b/`.

**Concept: it pivots on the roller's own shaft.**
- Two 1/8 in aluminium arms, 1.8 in left and right of centre, on round-bore flanged bearings (goBILDA 1611-0514-0008)
  riding the roller's REX shaft, in two short gaps in the roller.
- A short cross shaft carries the FLOWER block, its back edge 2.5 in ahead of the roller's front, so the
  POLLEN come off it straight into the roller.
- No walls, two arms, and it stays inside the roller's width: the corners are the V's.

**Outlines** (x right of centre, up, forward of the face; inches; pivot at the roller axle, 1.0 forward and 3.35 up):

| | Across | Up | Forward |
|---|---|---|---|
| Deployed (0°) | ±1.86 | 0.70–3.78 | 0.55–5.80 (20.9 in overall) |
| Stowed (125°, over the roller) | ±1.86 | 2.73–8.80 | −0.11–1.63 |

**Checked** every 5° from 0 to 125° (`tools/robot-cad/extract_sweep.py`): clear of the robot, the V, the side plates, the
roller motor and the pods. At 2.0 in out the left arm met the roller motor, so the arms are at 1.8 in, in 1/8 in
aluminium. Past about 140° the block meets the intake's upper cross-channel.

**Drawn** in `cad/intake-b/`: the drive is a 1:1 printed gear pair from a servo over the roller on the right, and
the hard stops act on a tab on the servo's gear. Clear every 5° from 0 to 125°, stowed 2.06 in ahead of the face,
142 g, worst servo load about 1.3 kg·cm.

## The robot's origin, and the odometry pods from it

**The origin** for the simulator, the AdvantageScope model and Pedro is the chassis centre, on the floor:
- midway between the side rails;
- **7.56 in behind the front face**, which is half the rails' 384.06 mm (15.12 in) length and midway between the wheel
  axles (1.89 and 13.23 in behind the face).

The robot is 15.12 in long and 15.24 in wide (over the wheel shafts). The STEP's bounding box reads 15.7 in long only
because a NECTAR pokes out of the front and OCC's boxes for the mecanum rollers are loose.

**The odometry pods' wheels from that origin** (+X forward, +Y left, inches; `cad/robot-addons/`):

| Pod | Measures | Wheel centre X | Wheel centre Y |
|---|---|---|---|
| Left rail, on its adapter | forward motion (Pinpoint's X pod) | +0.71 | +6.46 (left) |
| Right rail, between the wheels | sideways motion (Pinpoint's Y pod) | −0.94 | −6.37 (right) |

With goBILDA's Pinpoint convention (the X pod's offset is how far left of the centre it sits; the Y pod's is how far
forward), that's an X-pod offset of about +6.46 in and a Y-pod offset of about −0.94 in. Check the signs against
`pedro/robots/Robot<team>.java` and the Pinpoint's docs before tuning, and measure the real pods: these are from CAD.

## Seated on a FLOWER (for the scorer and the routes)

With the extractor down, the block's curved tip nests between the FLOWER's grey uprights and stops on their inside
corners, 1.25 in short of the FLOWER's centre. So:
- **Depth:** the front face is **7.09 in** from the FLOWER's centre and **9.80 in** from the wall. In the model frame
  (origin at the chassis centre), the FLOWER's centre is at X = 14.65 in.
- **Sideways:** the robot sits on the FLOWER's centreline.
- **Heading:** set by the drive, not by the extractor or the wall.

From there the launcher's exit (turret axis 10.73 in behind the face, exit 2.5 in ahead of it) is about 15.3 in from the
FLOWER's centre. Moving the block closer to the roller shortens that: a 1.0 in gap gives 13.8 in, and 0 gives 12.8 in.
That costs extraction margin, untested.

Emptying, in `tools/ramp-hook/extractor.py` (ramp.py's 2-D model with the real block and seat): all 4 POLLEN reach the
roller about 0.77 s after the block meets the uprights.

A cage over the FLOWER's top that blocks the Limelight only while seated at a FLOWER is acceptable (the user, 6 Oct
2026). Stowed or driving, everything stays under the Limelight's keep-clear ceiling.

## Room for the transfer (issue #164, `doc/transfer.md` on `spike/164-transfer`)

The transfer's lane runs down the centreline (Y ±2.35, X −5.1..7.2, below Z 5.0). In the robot CAD, the things in its way:
- **The old intake's 11-hole cross-channel** (X 5.03..5.51, Z 4.43..6.32): raise it 8 mm on the front uprights' back faces
  and remove the two pattern spacers on top. That leaves a NECTAR 0.22 in of clearance. The extractor is unaffected: its
  over-travel stop is the 9-hole channel on top of the uprights.
- **The front drive motors' encoder caps** (X 2.36..3.93, |Y| 1.87..2.59, Z 3.48..5.05): the lane walls drop to 3.4 in there.
- **The extractor's servo gear and down-stop** reach X 7.20 at Y −2.0..−2.3, Z 4.6..5.0: the lane walls end at X 7.0.
- **The CAD's "Launcher Concept"** is replaced by the turret.
- **The lane's drive pulley** on the roller shaft at Y +2.6 needs a third, 0.6 in gap in the roller.
