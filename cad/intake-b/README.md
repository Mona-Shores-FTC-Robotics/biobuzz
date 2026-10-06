# The unified front: 14 in roller, FLOWER extractor, Rigid V plates

The front of the robot as the threads decided it on 6 Oct 2026 (`doc/unified-design.md` on
`claude/biobuzz-robot-body-designs-hi386c`, `doc/intake-design.md` on `spike/160-intake-design`). It's drawn in the
robot CAD's own frame, so `dhs-intake-b.step` lands in place in Onshape (see `cad/robot-addons/README.md` for
importing). Three sub-assemblies:

1. **Chassis and roller (fixed).**
   - The outer wheel plates from `cad/robot-addons/`, extended forward to carry the roller.
   - A 14 in roller of 48 mm gecko wheels (13.8 in of wheels in three segments), its bottom 2.4 in off the tiles, its
     axle 1.0 in in front of the face, so its front is 1.94 in out.
   - The roller's motor sits inboard over the roller on the left, on a printed bracket on the left front upright. Its
     1:1 belt runs inside the left plate.
   - The extractor's servo, gears and hard stops sit over the roller on the right, on a printed bracket on the right
     front upright.
   - The wheel-plate standoffs, 80 mm wheel shafts, bearings and odometry pods, as in `cad/robot-addons/`.
2. **The FLOWER extractor** (turns about the roller's axle, 0 to 125°). It replaces the ramp hook; the Rigid V catches
   spills now.
   - Two 1/8 in aluminium arms, 1.8 in each side of centre, on round-bore bearings riding the roller's shaft in two
     gaps in the roller.
   - A 104 mm cross shaft carries the FLOWER block, its back edge 2.5 in ahead of the roller's front, so the POLLEN
     come off it straight into the roller.
   - No walls. A POLLEN passes between the arms (3.5 in clear).
   - Driven through a 1:1 printed gear pair (module 1.5, 34 teeth, 51 mm centres) from a servo above the roller.
   - Hard stops: a tab on the servo's gear meets a printed block 1° past each end. The stops sit above the roller,
     out of the pieces' way.
3. **The Rigid V's two plates.** 1/8 in aluminium, 0.25 to 4 in off the tiles. Each runs from its side plate's front
   corner (7.68 in from centre, 1.4 in ahead of the face) to 8.89 in out and 2.8 in ahead, which is as far forward as
   the 18 in starting size allows.
   - The simulator (60 runs each) keeps them 4 in tall and fixed. Lower flaps cost 2-3 points; one flap or none on
     the wall partner's lane lose to the V's own route.
   - Bare aluminium is fine unless a POLLEN-drop test shows bounce above about 0.3. If it does, face the plates with
     about 1/4 in of closed-cell foam.

## Checked against the robot CAD

| | |
|---|---|
| Extractor, every 5° from 0 to 125° | clear of the robot, the roller, the plates, the motor, the servo, gears and stops, the pods and the V plates. The arm's gear just touches the robot's old intake roller behind the face, which this design removes |
| Deployed, front to back | 20.96 / 24 in (the block's tip 5.84 in out) |
| Starting, front to back | 17.96 / 18 in (the V plates' tips 2.84 in out; the stowed extractor 2.06, the roller 1.94) |
| Stowed extractor, height | 8.8 in |
| Across | 17.8 / 18 in |
| Extractor | about 142 g; worst servo load about 1.3 kg·cm (1:1), against about 17 for the Torque servo at 5 V |

## Parts to order (goBILDA; check stock and pack sizes on gobilda.com)

| Part | goBILDA | Qty |
|---|---|---|
| Roller motor | 5203-2402-0005 Yellow Jacket, 5.2:1, 1150 RPM (or -0014, 13.7:1, 435 RPM, if the cardboard test wants a slower roller) | 1 |
| Extractor servo | 2000-0025-0002 Dual Mode Servo (25-2, Torque) | 1 |
| Flanged bearings, 8 mm REX bore, 14 mm OD: 2 for the roller, 1 for the motor, 4 for the wheels | 1611-0514-4008 (2-pack) | 7 bearings |
| Flanged bearings, round 8 mm bore, 14 mm OD: the extractor's arms, riding the roller's shaft | 1611-0514-0008 (2-pack) | 2 |
| 8 mm REX shafts | roller 400 mm, extractor cross shaft 104 mm, wheels 80 mm ×4 | |
| Roller drive, 1:1 (1150 RPM at the roller, about 114 in/s at the wheels' surface) | 3417-4008-0024 pulley, 24T HTD5, 8mm REX bore, ×2; 3412 Series belt, 9 mm, 55T (275 mm pitch length) | 2 + 1 |
| Step-down option, 2:3 (about 767 RPM, 76 in/s) | 3417-4008-0016 (16T) on the motor instead, same 24T on the roller and the same 55T belt | 1 |
| 48 mm gecko wheels for 13.8 in of roller | | about 20 |
| M4 standoffs, 56 mm | | 8 |
| 8 mm REX clamping collars | | 2 |
| 1/8 in aluminium: side plates, V plates, extractor arms | | |

**The motor sits 77.5 mm above the roller's axle (6.4 in off the tiles),** because goBILDA's shortest 9 mm belt
(55T) needs that centre distance with two 24T pulleys. The 2:3 option on the same belt needs about 87.3 mm, so the
motor bracket's four bolt holes are slotted 9.8 mm upward. In that position, drill the left plate's motor-bearing
hole 9.8 mm higher, or leave the motor's short shaft without the outer bearing.

## Before anything is cut or printed

The robot's designer has to agree, because the old roller behind the face goes. The servo, motor, pod and V-plate
mounting holes are placeholders, so drill them to match the real parts. The STLs are where the parts sit on the robot, so
lay each one flat before printing.
