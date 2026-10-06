# Intake option (b): 14 in roller, high-hinged ramp hook, Rigid V plates

The design the intake study chose (`doc/intake-design.md`, "The design to model", branch `spike/160-intake-design`),
drawn in the robot CAD's own frame so `dhs-intake-b.step` lands in place in Onshape (see `cad/robot-addons/README.md`
for importing). Three sub-assemblies:

1. **Chassis and roller (fixed).**
   - The outer wheel plates from `cad/robot-addons/`, extended forward and up. They also carry the roller and the
     hook's hinge.
   - A 14 in roller of 48 mm gecko wheels (13.8 in of wheels), its bottom 2.4 in off the tiles, its axle 1.0 in in
     front of the face, so its front is 1.94 in out.
   - The roller's motor sits inboard over the roller on the left, on a printed bracket on the left front upright. Its
     belt runs inside the left plate.
   - The hook's servo is inboard over the roller on the right, on a printed bracket on the right front upright.
   - The wheel-plate standoffs, 80 mm wheel shafts, bearings and odometry pods, as in `cad/robot-addons/`.
2. **The ramp hook** (turns about the hinge, 0 to 150°). A hub on the hinge, 6.0 in up and 1.0 in in front of the
   face. One sloping arm runs to the corner block, then the front shaft, FLOWER block, collars, clips, two curtains
   and a side panel under the arm.
3. **The Rigid V's two plates** (optional): 1/8 in aluminium, from each side plate's outer face out and forward
   to 1.75 in, 0.25 to 4 in off the tiles, bolted to the side plates.

## Checked against the robot CAD

| | |
|---|---|
| Hook, every 5° from down to 150° | clear of the robot, the roller, the plates, motor, servo and V plates |
| Hook down, front to back | 24.0 / 24 in (the FLOWER block's back edge 7.48 in out; lane from the roller 5.5 in) |
| Stowed at 150°, front to back | 17.3 / 18 in (the hook 2.22 in out, the roller 1.94) |
| Stowed, height | 14.5 / 18 in |
| Across, with the V plates | 17.8 / 18 in |
| V plates | clear of the front wheels, the belt drive and the stowed hook |
| Hook weight, balance point | about 314 g, 7.3 in from the hinge |
| Servo load, worst (at about 32° of fold) | about 5.9 kg·cm, against about 25 for the goBILDA Torque servo |

## Where it differs from the request, and why

- **The servo is inboard of the right plate**, not outside it: outside, it would make the robot 18.4 in wide with
  the V plates.
- **The arm is 7.15 in out, not 7.25.** With 13.8 in of roller wheels that leaves room for a 14 mm hub between
  the servo and the plate.
- **Both curtains end 4.7 in from centre.** As the hook folds they would sweep through the tops of the two tall
  front towers (4.8 to 5.3 in out, 14.3 in tall). The right one leaves 2.45 in to the arm, too narrow for a
  POLLEN to get out.
- **The roller's belt drive is inside the left plate.** Outside, the left V plate would cut through it.
- **The V plates start at the side plates' outer face** (7.68 in from centre) and end 8.89 in out, not 9.0, so the
  robot stays under 18 in. They stop 0.25 in off the tiles.

## Before anything is cut or printed

The robot's designer has to agree, because the old roller behind the face goes. The servo, motor, pod and V-plate
mounting holes are placeholders, so drill them to match the real parts. Check the REX-shaped hole in the corner
block (`REX_AF` in `cad/robot-addons/build.py`) on a test print. The STLs are where the parts sit on the robot, so
lay each one flat before printing.
