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

Follows the doc's "Build spec for CAD".

| Group | Parts |
|---|---|
| Fixed | the ramp (1/16 in polycarbonate) and its two printed brackets to the side plates; the lane floor (1/16 in polycarbonate, top at 0.9, slotted for the strand pulleys); two lane walls (1/16 in polycarbonate, inner faces at Y ±2.1, X −5.1..7.2, up to 5.0; cut to 3.4 over the front drive motors' encoder caps and to 4.4 under the raised 11-hole channel; up to 6.6 at the chute); two printed hangers from the raised 11-hole channel; two 6 mm D-shafts and four 0.5 in pulleys; two polycord floor strands at Y ±0.5; the outer J and chute (printed, two halves); a lid over the queue; the J motor (its shaft is the arm's left pivot), its printed face bracket and the right pivot block; two hard stops on slots and two band posts; the countershaft (6.0, 4.0) with two 16 mm pulleys and the lower loop to the front strand shaft (16 mm); the turret bearing and its two cross-channels, for reference (they're the launcher's) |
| Float (rises with the roller, 0 to 1.3 in) | the 32 mm pulley on the roller's shaft, in the roller's gap at Y +2.35..+2.9, and the upper polycord loop to the countershaft (2:1 up) |
| Arm (floats up to 1.2 in at the axle, about the pivot) | the J-wheel's two gecko wheels, its shaft and 16T pulley, the 40T belt to the motor's pulley, the two 1/8 in aluminium arms (60 mm) |

**Where this CAD differs from the spec, and why** (sent to the transfer chat):
- **The arm is at 20° above horizontal, not 30°.** At 30° the pivot is at (0.72, 3.36), and the J motor on it (37 mm,
  along +Y from Y 2.75) dips 0.22 in into the left rail's top (Z 2.85). At 20° the pivot is at **(0.90, 3.73)** and the
  motor clears the rail by 0.15 in. The axle stays at (−1.32, 4.54), and the 60 mm centres and 40T belt are unchanged.
  The spec's own torque check closes the arm onto its stop for both sizes below about 42°, so 20° keeps that, with
  more margin.
- **The turret's front cross-channel is at X 0.4..0.9, not 0.0..0.5.** The J-wheel moves forward as well as up when it
  floats (perpendicular to the arm). At full float it reaches X 0.30 at the channel's height.
- **The lid over the pocket became a lid over the queue,** X 0.45..2.3 at z 5.0. The floating wheel sweeps the space
  the spec gave the lid.
- **The band posts are forward of and above the pivot** (X 1.0..1.4, z 5.2..5.6), out of the arms' sweep.
- **The floor ends at the ramp's top (X 5.8).** The spec's X 7.2 would sit over the ramp.

## Checked against the robot CAD and the front

`tools/robot-cad/transfer_sweep.py` (the robot on its 0.1 in grid; the new parts by exact mesh intersection):

| | |
|---|---|
| At rest | clear of the robot (the hangers bolt to the raised 11-hole channel; the "Launcher Concept" gives way to the turret) |
| Against the front (`cad/intake-b/`), with the roller at 0, 0.65 and 1.3 in and the extractor at 0, 75 and 150° | clear |
| The J-wheel floating 0 to 1.2 in | clear of the walls, stops, posts, lid, motor and the turret's channels |
| The raised 11-hole channel | above the walls' cut-out to 4.4 |

Before the lane goes in: raise the old intake's 11-hole channel 8 mm and remove its two pattern spacers.

## Parts

From the doc's build spec (check goBILDA numbers before ordering; "to confirm" means the type is decided):

| Part | Source | Qty |
|---|---|---|
| J motor, Yellow Jacket 1620 RPM (only this speed: the piece must reach the launcher between z 6.6 and 10) | goBILDA 5203-2402-0003 | 1 |
| 48 mm gecko wheels | as for the roller | 2 |
| 8 mm REX shafts: J shaft about 130 mm, countershaft about 30 mm, the right pivot stub | | 3 |
| 6 mm D-shafts and 6 mm-bore flanged bearings, for the strand pulleys | goBILDA 2100 series, to confirm | 2 shafts, 4 bearings |
| Flanged bearings: 1611-0514-0008 (round bore) on each arm's pivot; 1611-0514-4008 (REX) for the countershaft | goBILDA | 2 + 1 |
| HTD5 16T pulleys, 8 mm REX bore (J shaft, motor shaft) and a 40T 9 mm belt | 3417-4008-0016 (to confirm), 3412 series | 2 + 1 |
| 3/16 in 83A polycord: two floor strands (about 14.8 in), the upper (about 8.3 in) and lower (about 8.7 in) drive loops | | 4 loops |
| Printed (PETG): ramp brackets, hangers, outer J and chute (two halves), motor bracket, right pivot block, two stops, two band posts, four 0.5 in strand pulleys, the 32 mm and three 16 mm V-groove pulleys | | |
| 1/16 in polycarbonate: floor, two walls, ramp, queue lid | | |
| 1/8 in aluminium: two arms, 60 mm centres | | 2 |
| 1/4 in surgical tubing, about 1 lbf preload | | 2 short lengths |
| Turret, 105 mm bore, on two 1120-series channels (the launcher's) | goBILDA 3208-0004-0001 | 1 |
