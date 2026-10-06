# The transfer: intake roller to turret (concept A)

The transfer chat's design (issue #164, `doc/transfer.md` on `spike/164-transfer`), drawn in the robot CAD's frame, so
`dhs-transfer.step` lands in place in Onshape beside `cad/intake-b/dhs-intake-b.step` (import it the same way:
`cad/robot-addons/README.md`). `build.py` holds every number; the frame it's drawn in is the transfer chat's (+X
forward, +Y left, +Z up, inches, origin on the floor under the chassis centre).

**How it works:** the roller pushes each piece up a 22° ramp onto a 4.2 in lane, 0.9 in off the tiles. Two polycord
strands on the floor carry it back to a J-wheel (two 48 mm gecko wheels) sitting on a fixed J-curve. Stopped, the wheel
holds the queue. Running, it throws each piece round the J and straight up the turret axis through the hollow turret
bearing. The lane holds 4 POLLEN or 3 NECTAR, never 5. One new motor (the J); the lane runs off the roller's shaft.

## What's drawn

| Group | Parts |
|---|---|
| Fixed | the ramp; the lane floor (1/16 in polycarbonate, slotted for the strands); two lane walls (1/16 in polycarbonate, X −5.1..7.2, up to 5.0, notched to 3.4 over the front drive motors' encoder caps and to 4.7 under the raised 11-hole channel, up to 6.6 at the chute); two strand shafts and four 0.75 in pulleys; two polycord floor strands; the outer J and chute (printed, 3 pieces); the arm's pivot stubs and its stop; the J motor, its belt to the left pivot stub, and its cradle; the countershaft (6.0, 4.0) with two pulleys and the lower polycord loop to the front strand shaft; three mounting strips; the turret bearing, for reference |
| Float (rises with the roller, 0 to 1.3 in) | the 24 mm pulley on the roller's shaft, in the roller's gap at Y +2.35..+2.9, and the upper polycord loop to the countershaft |
| Arm (lifts up to 1.2 in for a NECTAR, about the pivot at (0.85, 3.29)) | the J-wheel's two gecko wheels and shaft, the two printed arms (outside the walls), the 1:1 HTD5 drive outside the left arm |

**This CAD's own choices** (the transfer doc leaves them open; to confirm with the transfer chat):
- **Mounting:** three 1/8 in aluminium strips under the lane floor (X 3.78, 1.89 and −2.84), each tabbed up to both
  rails' inner faces on their lower hole row (1.24 in up). The strips at 1.89 and −2.84 share the outer wheel plates'
  standoff bolts. The walls sit on the strips.
- **The J motor** lies along Y over the left rail at (X −2.4, Z 3.7), in a printed cradle on the rail's top, and drives
  the left pivot stub through a 16T–16T HTD5 belt. The doc's box (Y 2.6..4.6) is too narrow for a motor along Y, so it
  reaches Y 7.2, inside the outer wheel plate.
- **The arms are outside the walls,** with the J shaft passing through an arc slot in each wall.

## Checked against the robot CAD and the front

`tools/robot-cad/transfer_sweep.py` (the robot on its 0.1 in grid; the new parts by exact mesh intersection):

| | |
|---|---|
| At rest | clear of the robot, except where the strips and the motor cradle sit on the rails |
| Against the front (`cad/intake-b/`), with the roller at 0, 0.65 and 1.3 in and the extractor at 0, 75 and 150° | clear |
| The J-wheel lifting 0 to 1.2 in | clear |
| The raised 11-hole channel | 0.05 in over the walls' notch |

Before the lane goes in: raise the old intake's 11-hole channel 8 mm and remove its two pattern spacers; the robot's
"Launcher Concept" makes way for the turret.

## Parts

From `doc/transfer.md`, plus the mounts:

| Part | Source | Qty |
|---|---|---|
| J motor, Yellow Jacket 1620 RPM | goBILDA 5203-2402-0003 | 1 |
| 48 mm gecko wheels | as for the roller | 2 |
| 8 mm REX shafts: J shaft 132 mm, strand shafts 125 and 110 mm, pivot stubs, countershaft 30 mm | | |
| Flanged bearings, 8 mm REX bore | goBILDA 1611-0514-4008 | 8 |
| HTD5 16T pulleys and 9 mm belts: the arm drive (2.5 in centres) and the motor to the pivot stub (about 3.3 in) | goBILDA 3417 / 3412 | 4 pulleys, 2 belts |
| 3/16 in polycord: two floor strands (about 14 in), the lane drive's two loops (about 7.5 and 9 in) | | 4 loops |
| Printed: the ramp, the outer J and chute, two arms, the arm stop, the motor cradle, four strand pulleys, four V-groove pulleys | PETG | |
| 1/16 in polycarbonate: the floor and two walls | | |
| 1/8 in aluminium strips, 1 in wide, about 10 in, bent up at the ends | | 3 |
| Surgical tubing for the arm (about 1 lbf preload) | | 6 in |
| Turret, 105 mm bore (the launcher's) | goBILDA 3208-0004-0001 | 1 |
