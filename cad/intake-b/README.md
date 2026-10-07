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
   - **Roller:** 2 in vector wheels (WCP-0353/0354), its bottom 2.4 in off the tiles at rest, its axle 1.06 in in front
     of the face, its back 0.06 in clear of the uprights.
   - **It centres what it picks up.** Behind the roller everything but the lane's middle 3.7 in is the robot's face, so
     a piece taken in off-centre has to be moved sideways by the roller itself. Each half is six vector wheels whose
     rollers push a piece back and toward the middle; the face (and the ramp brackets in front of it) is the fence it
     slides along. Pulling in, a WCP-0353 pushes to the robot's left, so the 0353s go on the right half and the 0354s
     on the left. A ball's centre can't pass 6.16 in (the side plates), so the wheels from 0.4 to 6.4 in each side
     cover every one; inside 0.4 in a 0.8 in 48 mm gecko pulls straight in, where a piece already clears the lane's
     walls. Their 1/2 in hex bores take a printed insert on the 8mm REX shaft.
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
     each on its stub's inner end, held by an M4 screw and washer in the shaft's tapped end, with goBILDA spacers from
     the arm out to the bearing (on the right, either side of the gear). Nothing near the roller is wider than 12 mm, so
     the floated roller passes under the stubs. Nothing crosses the middle above the block, so the FLOWER passes
     between the arms.
   - **The left arm is relieved over the roller's motor** (11.25 mm wide there, 15 elsewhere): stowed, it lies over
     the motor, and the relief clears the real Yellow Jacket by 1.75 mm at every float height.
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

`tools/robot-cad/front2_check.py Robot.step` checks against the mentor's current Robot.step, lined up as the full STEP
has it, by exact mesh intersection: the roller at six float heights, the extractor every 10° from down to stowed, and
the transfer. It's clear except shafts in their own hubs. (`tools/robot-cad/front_sweep.py` is the older check against
the earlier robot CAD, on its 0.1 in grid; its results below are from before the vector roller.)

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
| Roller and motor, down | 7.62..9.61 | ±7.93 | 2.40..8.03 |
| Roller and motor, up 1.3 | 7.62..9.61 | ±7.93 | 3.70..9.33 |
| Extractor, down | 9.26..13.40 | arms ±4.2 (stubs to ±7.76) | 0.61..5.56 |
| Extractor, stowed (146°) | 8.90..10.39 | same | 3.44..9.57 |

## Parts to order (goBILDA; check stock and pack sizes on gobilda.com)

| Part | goBILDA | Qty |
|---|---|---|
| Roller motor | 5203-2402-0005 Yellow Jacket, 5.2:1, 1150 RPM (or -0014, 13.7:1, 435 RPM, if the cardboard test wants a slower roller) | 1 |
| Extractor servo | 2000-0025-0002 Dual Mode Servo (25-2, Torque) | 1 |
| Flanged bearings, 8 mm REX bore, 14 mm OD: 2 for the roller (in the float plates), 2 for the extractor's shaft, 4 for the wheels | 1611-0514-4008 (2-pack) | 8 bearings |
| 8 mm REX shafts | roller 400 mm, two extractor stubs about 105 mm, extractor cross shaft 226 mm, wheels 80 mm ×4 | |
| Roller drive, 1:1 (1150 RPM at the roller, about 114 in/s at the wheels' surface) | 3417-4008-0024 pulley, 24T HTD5, 8mm REX bore, ×2; 3412 Series belt, 9 mm, 55T (275 mm pitch length) | 2 + 1 |
| Step-down option, 2:3 (about 767 RPM, 76 in/s) | 3417-4008-0016 (16T) on the motor instead, same 24T on the roller and the same 55T belt | 1 |
| Roller wheels | WCP-0353 (×6) and WCP-0354 (×6), 2 in vector wheels; one 48 mm gecko (3632-4008-0048) in the middle; a printed 1/2 in hex to 8mm REX insert in each vector wheel | 12 + 1 |
| M4 standoffs, 56 mm | | 8 |
| M4 shoulder screws, 5 mm shoulder, low head: the motor carriage (2) and the right float plate (1) | | 3 |
| 8 mm REX clamping collars, 2 at the block | 2910-1020-4008 | 2 |
| The extractor's arms: an M4 × 10 button head and a 12 mm washer each, and 8 mm bore spacers (10 mm OD) from the arm to the bearing, about 82 mm a side | | 2 + 2 stacks |
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
- The servo, motor, pod and V-plate mounting holes are placeholders, so drill them to match the real parts.
- The STLs are where the parts sit on the robot, so lay each one flat before printing.
