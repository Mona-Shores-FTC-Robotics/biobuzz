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

## The FLOWER extractor and the floating roller (unified design, 6 Oct 2026)

The hook is no longer a spill catcher (the Rigid V is; `doc/unified-design.md` on `claude/biobuzz-robot-body-designs-hi386c`).
It is a FLOWER extractor. Drawn in `cad/intake-b/`, whose README has the parts.

**The roller floats straight up 1.3 in** (Intake Design chat: a NECTAR is 3.62 in and stiff, so a fixed roller at 2.4 in
refuses it).
- **It can't swing on arms.** Its rear is 0.06 in from the front uprights, so it can't move back. A pivot on the motor
  shaft (77.5 mm straight above the axle) would swing it 2.5 in sideways for a 1.3 in rise. A pivot behind it lands among
  the wheels, drive belts and uprights.
- **So it rides in vertical slots.** Its shaft rises in slots in the side plates, and its bearings sit in outboard
  float plates.
- **The motor rides on the same carriage,** so the belt keeps its length. The carriage slides on the left upright's
  front face.
- **0.85 in of rise** passes a NECTAR that gives 0.4 in; the slots allow 1.3 in, for one that doesn't give.

**The extractor turns about a fixed axis,** 2.4 in ahead of the face and 4.5 in up, on **two stub shafts**, one from each
side plate's bearing in to its arm.
- **Arms:** two 1/8 in aluminium arms, 15 mm wide, **4.2 in left and right of centre: outside the FLOWER**, whose widest
  part (the black bracket under its posts) is ±2.35 in. Nothing crosses the middle above the block, so the FLOWER's
  bracket, posts and uprights pass between the arms as the robot drives in. The arms and the block's cross shaft make a U.
  (Until 7 Oct the arms were 1.8 in out on one full-width shaft. That hit the FLOWER's bracket 1.3 in before the block
  could seat: see "Seated on a FLOWER".)
- **Block:** the FLOWER block as before, its back edge 2.5 in ahead of the roller's front, on a 226 mm cross shaft.
- **Only the narrow block reaches the floor,** so it doesn't corral.
- **Range:** 0 (down) to 146° (folded up in front of the roller). 146° keeps the block inside the 18 in start and the left
  arm clear of the roller motor when the roller floats.
- **Drive:** a 1:1 printed gear pair in the gap between the roller's right end and the side plate. The shaft's gear is a
  sector, so nothing sticks out ahead when stowed.

**Checked** (`tools/robot-cad/front_sweep.py`):
- The roller rising 0 to 1.3 in, and the extractor every 5° from 0 to 146° with the roller down, half up and fully up:
  clear of the robot and of every new part.
- Starting length 17.96 in, deployed 20.96, at most 22.6 while swinging. 17.8 in across.
- Driving into the FLOWER (`tools/robot-cad/flower_seat.py`, the FLOWER of `tools/robot-cad/flower.py`): see below.
- The extractor is about 430 g as drawn, mostly the shaft on its own axis. Worst servo load about 1.3 kg·cm.

| Outline (X fwd, Y left, Z up, in; chassis centre) | X | Y | Z |
|---|---|---|---|
| Roller and motor, down | 7.56..9.50 | ±7.93 | 2.40..7.97 |
| Roller and motor, up 1.3 | 7.56..9.50 | ±7.93 | 3.70..9.27 |
| Extractor, down | 9.26..13.40 | arms ±4.2 (stubs to ±7.76) | 0.61..5.56 |
| Extractor, stowed | 8.90..10.39 | same | 3.44..9.57 |

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

**Corrected 7 Oct 2026.** Until then this section said the FLOWER's centre was 7.09 in ahead of the face. That had the
grey uprights on the wrong side of the FLOWER's centre: they stand 1.25 in *beyond* it (toward the wall), at the back of
the bottom ring's hole. So the robot sat about 2.5 in short in the simulator, as the user saw in the logs.

With the extractor down, the block's curved tip nests between the grey uprights and stops on them. So:
- **Depth:** the FLOWER's centre is **4.59 in ahead of the front face** (X = 12.15 in from the chassis centre), and the
  face is **7.30 in from the wall**. The robot sets this itself: drive in until the block stalls on the uprights.
- **Sideways:** the robot ends on the FLOWER's centreline when it arrives square; the curved tip draws it in.
- **Heading:** set by the drive.
- **Nothing else touches.** At the seat, the roller's front is 0.3 in from the FLOWER's bracket, and the roller clears its
  bottom ring (0.43 in tall) and green posts. The arms and stub shafts are outside the FLOWER's width.

**Room for error** (`tools/robot-cad/flower_seat.py`: the robot driven in at each offset and heading until something
touches):

| Robot off the FLOWER's centreline | Heading off | First contact | FLOWER's centre ahead of the face |
|---|---|---|---|
| 0 to 1.25 in | 0 or ±2° | the block, on an upright | 4.42–4.58 (seated) |
| 1.25 in | −2° (turned away) | an arm collar on the FLOWER's bracket | 5.08 (short) |

So the robot can arrive up to about **1 in to either side and 2° off** and still seat. Once off-centre, the block meets
the FLOWER's bottom POLLEN on its side, not square. Whether it still empties the FLOWER that far off is for the
cardboard test: `tools/ramp-hook/` simulates the centred case only.

From there the launcher's exit (turret axis 10.73 in behind the face, exit 2.5 in ahead of it) is about 12.8 in from
the FLOWER's centre, not the 15.3 given earlier.

Emptying, in `tools/ramp-hook/extractor.py` (ramp.py's 2-D model with the real block and seat): all 4 POLLEN reach the
roller about 0.77 s after the block meets the uprights.

A cage over the FLOWER's top that blocks the Limelight only while seated at a FLOWER is acceptable (the user, 6 Oct
2026). Stowed or driving, everything stays under the Limelight's keep-clear ceiling.

## A wider FLOWER bar (study, 8 Oct 2026)

The mentor asked (through the body-designs chat) whether the extractor could become a full-width bar, so the robot
needn't line up so exactly with a FLOWER. A note first: that request described the arms as 1.8 in out on the roller
shaft. That is the pre-7 Oct extractor; today's arms are 4.2 in out on their own shaft (above).

**What limits the offset today.** The narrow block nests in the FLOWER's 2.79 in hole between the grey uprights.
Off-centre, it still seats on one upright. What stops it is the FLOWER's black bracket (±2.35 in, 3.55 to 4.15 in up)
hitting an arm. So the limit is the arm spacing, not the block.

**The study.** Swap the block for a straight bar with the block's profile (front 0.7 to 1.35 in up, 0.5 in flat top,
1.4 in deep), arm to arm on the cross shaft, and move the arms out. Its bottom passes over the bottom ring (0.43 in),
and its front stops on both uprights at the same depth as the block, under the bottom POLLEN, so the emptying slice
(`tools/ramp-hook/extractor.py`) is the block's. Built as `EX_X=<in> EX_BAR=1 python3 cad/intake-b/build.py` (study
switches only; the default build is unchanged). Each variant was driven into the FLOWER at each offset and at 0 and ±3°
(`flower_seat.py`'s method) and swept 0 to 146° with the roller at every float height (`front2_check.py`).

| Arms from centre | Seats (square) | Seats (3° off) | Sweep and stow | Start size |
|---|---|---|---|---|
| 4.2 in, narrow block (today) | ±1.5 in | ±1 in | clear | unchanged |
| **5.6 in, bar** | **±3.0 in** | **±2.5 in** | **clear** | **unchanged** |
| 6.2 in, bar | ±3.5 in | ±3 in | right arm hits the servo bracket stowed | unchanged |
| 6.5 in, bar | ±4.0 in | ±3.5 in | right arm hits the servo and its bracket; a cross-shaft screw hits the motor carriage | unchanged |
| 7.0 in, bar | ±4.5 in | ±3.5 in | hits the servo, its gear and bracket, and the roller motor's belt and carriage | unchanged |

In every case the first contact past the limit is still the FLOWER's bracket on an arm. The V plates are never reached.

**Recommendation: the compromise, arms at 5.6 in with a bar.** It doubles the room for error (±1.5 → ±3 in) and needs
nothing else to move. Past about 6 in the arms run into the extractor's drive on the right and the roller motor on the
left, so a true full-width bar (±4.5 in and more) means moving the servo drive outboard of the side plate, which is a
redesign of the front. Since 7 in of offset can't be reached without that, ±3 in plus the drive lining up on
`HiveFieldPoints` (`poseReferenced()`) is the realistic target.

What it costs:
- **Mass and servo:** about +45 g (a printed bar about 11 in long, 40% infill, and a 287 mm cross shaft). That's about
  +0.6 kg·cm at worst, so roughly 2 kg·cm of the Torque servo's 25.
- **Stiffness:** off-centre, the FLOWER pushes one end of the bar, and the twist reaches the hard stop (on the right)
  through the cross shaft. With the aluminium REX standoff alone that's roughly 3 mm of give under 30 N. With the
  printed bar on it, about 1 mm. A steel REX shaft cut to 287 mm, with clamping collars outside the arms in place of
  the tapped-end screws, halves that again.
- **Rules:** the bar is low and full-width only while the extractor is down at a FLOWER. Check the control/herding
  rules for pieces it might push while lowered.
- **Not lost:** the narrow block also drew the robot onto the centreline. The bar doesn't need to, since it empties
  the FLOWER anywhere along it.
- **To test:** the cardboard rig with the FLOWER 2 to 3 in off the bar's centre. Do all four POLLEN still come out
  and reach the roller? The vector wheels centre them from there.

## Room for the transfer (issue #164, `doc/transfer.md` on `spike/164-transfer`)

The transfer is `cad/transfer/` (v4): a ramp, a lane of five roller shafts under a sprung ceiling, and a feeder against
a sprung pad under the mentor's flywheels. `cad/transfer/README.md` describes it and the changes it needs in the
mentor's CAD. It fits: `tools/robot-cad/transfer2_check.py` checks it against the mentor's Robot.step and our front
by exact mesh intersection, and it is clear (README, "Checked").
