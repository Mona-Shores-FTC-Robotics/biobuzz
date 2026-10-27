# The unified front: floating roller, FLOWER extractor, Rigid V plates

The front of the robot as the threads decided it on 6 Oct 2026 (`doc/unified-design.md` on
`claude/biobuzz-robot-body-designs-hi386c`, `doc/intake-design.md` on `spike/160-intake-design`). It's drawn in the
robot CAD's own frame, so `dhs-intake-b.step` lands in place in Onshape (see `cad/robot-addons/README.md` for
importing). Four sub-assemblies:

1. **Fixed.**
   - The outer wheel plates from `cad/robot-addons/`, extended forward. They carry the roller's slots and the extractor's
     shaft.
   - The extractor's servo, gears and hard stops, over the roller on the right, on a printed bracket on the right
     front upright.
   - The wheel-plate standoffs, 80 mm wheel shafts, bearings and odometry pods, as in `cad/robot-addons/`.
2. **The roller and its motor, which float straight up 1.3 in** so a NECTAR (3.62 in, stiff) passes under the roller.
   - **Roller:** 13.8 in of 48 mm gecko wheels, its bottom 2.4 in off the tiles at rest, its axle 1.0 in in front of
     the face. It has one gap, at 2.35-2.9 in left of centre, for the transfer's lane pulley.
   - **Slots, not arms.** The roller can't move back: its rear is 0.06 in from the front uprights. So its shaft rises in
     vertical slots in the side plates, and its bearings sit in two outboard float plates that slide on the plates'
     outer faces.
   - **The motor rides along.** It sits 77.5 mm above the roller, as before, on a printed carriage, so the belt never
     changes length. The carriage slides on the left upright's front face, on two low-head M4 shoulder screws in slots.
     A bridge over the motor's pulley joins it to the left float plate.
   - **Return:** gravity and a light spring hold it down on two printed stops. The spring is the POLLEN bite force, to be
     set on the rig; it isn't drawn.
   - **Travel:** 0.85 in passes a NECTAR if it gives 0.4 in; the slots allow 1.3 in, which passes one that doesn't give
     at all.
3. **The FLOWER extractor, about a fixed axis** 2.4 in ahead of the face and 4.5 in up, on two stub shafts (8 mm REX)
   from the side plates. It turns 0 (down) to 146° (folded up in front of the roller).
   - Two 1/8 in aluminium arms, 15 mm wide, **4.2 in each side of centre: outside the FLOWER** (±2.35 at its widest),
     clamped to the stubs by REX holes and collars. Nothing crosses the middle above the block, so the FLOWER passes
     between the arms.
   - A 226 mm cross shaft carries the FLOWER block, its back edge 2.5 in ahead of the roller's front. Seated, the
     FLOWER's centre is 4.59 in ahead of the face, and the robot can arrive about 1 in off-centre and 2° off and still
     seat (`doc/robot-cad.md`, "Seated on a FLOWER").
   - No walls and nothing at the floor but the narrow block, so it doesn't corral spilled pieces.
   - Driven through a 1:1 printed gear pair (module 1.5, 34 teeth, 51 mm centres) in the gap between the roller's
     right end and the side plate. The shaft's gear is a sector, with teeth only where they mesh, so nothing sticks
     out ahead when stowed.
   - Hard stops: a tab on the servo's gear meets a printed block 1° past each end.
4. **The Rigid V's two plates.** 1/8 in aluminium, 0.25 to 4 in off the tiles. Each runs from its side plate's front
   corner (7.68 in from centre, 1.55 in ahead of the face) to 8.89 in out and 2.8 in ahead, which is as far forward as
   the 18 in starting size allows. Their tabs are lower than before, under the float plates.
   - The simulator (60 runs each) keeps them 4 in tall and fixed. Lower flaps cost 2-3 points; one flap or none on
     the wall partner's lane lose to the V's own route.
   - Bare aluminium is fine unless a POLLEN-drop test shows bounce above about 0.3. If it does, face the plates with
     about 1/4 in of closed-cell foam.

## Checked against the robot CAD

`tools/robot-cad/front_sweep.py`: the robot on its 0.1 in grid; the new parts against each other by exact mesh
intersection, so parts that only slide against each other don't count.

| | |
|---|---|
| Roller and motor, rising 0 to 1.3 in | clear |
| Extractor, every 5° from 0 to 146°, with the roller down, half up and fully up | clear |
| Driving into the FLOWER, up to 1.25 in off-centre and ±2° | the block, on the FLOWER's uprights, is the first thing to touch |
| Deployed, front to back | 20.96 / 24 in (the block's tip 5.84 in out). Swinging, at most 22.6 in |
| Starting, front to back | 17.96 / 18 in (the V plates' tips 2.84 in out; the stowed extractor 2.81, the side plates 2.83) |
| Height | the stowed extractor 9.5 in; the motor's top 8.0 in at rest, 9.3 floated |
| Across | 17.8 / 18 in |
| Extractor | about 430 g as drawn, most of it the shaft (on its own axis) and steel collars; worst servo load about 1.3 kg·cm (1:1), against about 17 for the Torque servo at 5 V |

**Outlines** (AdvantageScope frame: +X forward, +Y left, +Z up, inches, chassis centre on the floor; face at X 7.56):

| | X | Y | Z |
|---|---|---|---|
| Roller and motor, down | 7.56..9.50 | ±7.93 | 2.40..7.97 |
| Roller and motor, up 1.3 | 7.56..9.50 | ±7.93 | 3.70..9.27 |
| Extractor, down | 9.26..13.40 | arms ±4.2 (stubs to ±7.76) | 0.61..5.56 |
| Extractor, stowed (146°) | 8.90..10.39 | same | 3.44..9.57 |

## Parts to order (goBILDA; check stock and pack sizes on gobilda.com)

| Part | goBILDA | Qty |
|---|---|---|
| Roller motor | 5203-2402-0005 Yellow Jacket, 5.2:1, 1150 RPM (or -0014, 13.7:1, 435 RPM, if the cardboard test wants a slower roller) | 1 |
| Extractor servo | 2000-0025-0002 Dual Mode Servo (25-2, Torque) | 1 |
| Flanged bearings, 8 mm REX bore, 14 mm OD: 2 for the roller (in the float plates), 2 for the extractor's shaft, 4 for the wheels | 1611-0514-4008 (2-pack) | 8 bearings |
| 8 mm REX shafts, stock lengths (2106-4008-LLL0): roller 432 mm (the bearings' outer faces are 399 mm apart; 384 ends inside them, so 432 with 16 mm spare each end), extractor stubs 104 mm ×2, extractor cross shaft 240 mm, wheels 80 mm ×4 | 2106-4008-4320, -1040, -2400, -0800 | 1 + 2 + 1 + 4 |
| Roller drive, 1:1 (1150 RPM at the roller, about 114 in/s at the wheels' surface) | 3417-4008-0024 pulley, 24T HTD5, 8mm REX bore, ×2; 3412 Series belt, 9 mm, 55T (275 mm pitch length) | 2 + 1 |
| Step-down option, 2:3 (about 767 RPM, 76 in/s) | 3417-4008-0016 (16T) on the motor instead, same 24T on the roller and the same 55T belt | 1 |
| 48 mm gecko wheels, 16 mm wide, for 13.8 in of roller: 6 left of the transfer gap, 14 right of it, packed from the ends | 3613-4008-0048 | 20 |
| 8 mm REX spacers to take up the 5.6 mm left of the gap and 11 mm right of it, and the 20 mm between each end wheel and its bearing | | |
| M4 standoffs, 56 mm | | 8 |
| M4 shoulder screws, 5 mm shoulder, low head: the motor carriage (2) and the right float plate (1) | | 3 |
| goBILDA 25T servo hub for the printed servo gear (the gear has a 6.4 mm hole for the spline; it must not be printed as a spline socket) | | 1 |
| M4 screws: 4 into the motor's face (M4 threads on a 16 mm square), 2 through the servo's rear ear and the bracket | | 6 |
| 8 mm REX clamping collars (20 mm OD, 9 mm long): 4 on the extractor's arms, 2 at the block, 2 outboard of the roller's bearings to hold its shaft | 2910-0920-4008 | 8 |
| A light extension spring (or two) to hold the roller down; its force is the POLLEN bite, set on the rig | | 1-2 |
| 1/8 in aluminium: side plates, float plates, V plates, extractor arms | | |

**The motor sits 77.5 mm above the roller's axle (6.4 in off the tiles at rest),** because goBILDA's shortest 9 mm
belt (55T) needs that centre distance with two 24T pulleys. The carriage holds both, so the belt keeps its length
however far the roller floats. The 2:3 option on the same belt needs about 87.3 mm; the carriage's motor holes would
move up 9.8 mm.

## Before anything is cut or printed

- The robot's designer has to agree, because the old roller and motor behind the face go.
- Check that the left upright's front face has M4 holes at 7.6 and 7.9 in up (8 mm apart, above the motor) for the
  carriage's shoulder screws. The holes lower down are behind the motor.
- The pod and V-plate mounting holes are placeholders, so drill them to match the real parts. The motor's four M4
  holes in the carriage and the servo's two in the bracket are from goBILDA's CAD.
- The STLs are where the parts sit on the robot, so lay each one flat before printing.
- The shafts are drawn as goBILDA's REX profile (8 mm round, six 7 mm flats) at stock lengths.

## goBILDA's parts, drawn from goBILDA's CAD

With the six STEPs in `cad/gobilda/step/` (`cad/gobilda/README.md`), `build.py` draws the real motor, servo, pulleys,
bearings, wheels and collars, writes the committed `dhs-intake-b.step` with their stand-ins (git can't take 80 MB) and
`dhs-intake-b-vendor.step` with the real geometry, for Drive. Placing the real parts changed four things:

- **The roller's bearings ride in the side plates' slots.** A 1611 bearing is 4 mm of 14 mm body plus a 1 mm flange,
  and a float plate is 3.2 mm, so the body passes through the float plate (flange outboard) and 0.6 mm into the side
  plate. The roller slot is 15 mm wide, not 10, and the bearing's body is what runs in it. The extractor's bearings sit
  flange inboard, body 0.6 mm proud of the side plate's outer face, where nothing slides.
- **The servo bolts by its ears.** goBILDA's servo has 2.5 mm mounting ears 10.3 mm behind its top face, holes
  4.5 mm at 24 mm from the spline and 5 mm either side. The rear ear slides into a 5 mm slot in the bracket's back
  block and takes two M4 through the block; the shelf still carries the body. The front ear is free (nothing to bolt
  to); the stand-in box was 3.4 mm shorter than the real body.
- **The motor's face is M4 on a 16 mm square**, with a 0.25 mm lip; the carriage's holes are M4 clearance, and the
  motor's own 23.5 mm shaft reaches 17.5 mm past the carriage's plate, through the pulley.
- **The printed servo gear has a 6.4 mm hole**, not a spline socket: it mounts on a goBILDA 25T hub.

The real parts clear every cut and printed part (checked by exact intersection, `tools/robot-cad/front_sweep.py`
style, on the placed geometry).
