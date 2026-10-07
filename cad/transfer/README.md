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

The doc's "Build spec for CAD", with the changes the transfer chat confirmed on 6 Oct.

| Group | Parts |
|---|---|
| Fixed | the ramp (1/16 in polycarbonate) and its two printed brackets to the side plates; the lane floor (1/16 in polycarbonate, top at 0.9, slotted for the strand pulleys); two lane walls (**1/8 in** polycarbonate, inner faces at Y ±2.1, X −5.1..7.2, up to 5.0; cut to 3.4 over the front drive motors' encoder caps and to 4.4 under the raised 11-hole channel; up to 6.6 at the chute); three aluminium mounting strips to the rails; two 6 mm D-shafts and four 0.5 in pulleys; two polycord floor strands at Y ±0.5; the outer J and chute (printed, two halves); a lid over the queue; the arm's pivot stubs and the two 16T pulleys on the left one; two hard stops (on ±0.1 in slots) and two three-hole band posts; the J motor over the left rail with its cradle and its belt to the left pivot stub; the countershaft (6.0, 4.0) with two 16 mm pulleys and the lower loop to the front strand shaft (16 mm); the turret bearing and its two cross-channels, for reference (they're the launcher's) |
| Float (rises with the roller, 0 to 1.3 in) | the 32 mm pulley on the roller's shaft, in the roller's gap at Y +2.35..+2.9, and the upper polycord loop to the countershaft (2:1 up) |
| Arm (floats about the pivot until the axle is 0.95 in up: 34.4°) | the J-wheel's two gecko wheels and shaft, the two 1/8 in aluminium arms (60 mm, pivot at (0.72, 3.36), 30° above horizontal), the 16T pulley on the J shaft and the 40T belt to the pivot stub |

**This CAD's choices, confirmed by the transfer chat:**
- **Mounting:** three 1/8 in aluminium strips under the floor (X 3.78, 1.89 and −2.84), each tabbed up to both rails'
  inner faces on their lower hole row (1.24 in up). The strips at 1.89 and −2.84 share the outer wheel plates'
  standoff bolts. The walls sit on the strips, with ears down to 0.3 in for the strand shafts' bearings.
- **The J motor** lies along Y over the left rail at (X −2.4, Z 3.7), in a printed cradle on the rail's top. It drives
  the left pivot stub through a 16T–16T HTD5 belt, and the stub drives the J shaft through the arm's 40T belt. This
  keeps the motor's mass low and off the wall.
- **The arms are outside the walls,** with the J shaft passing through an arc slot in each wall.
- **The walls are one piece each side,** 1/8 in thick, so they carry the pivots and the countershaft without ribs.

**Where this CAD differs from the spec, and why:**
- **The float is 0.95 in of vertical rise at the axle (34.4° of arm rotation, about 1.42 in along the arc).** A NECTAR at
  the mouth needs 0.92 in of rise (the transfer chat's figure). Along the arc, 1.2 in rises only 0.85, because the arm turns
  as it lifts. At full float the wheel's top is at 6.44, 0.16 under the turret bearing.
- **The turret's front cross-channel is at X 0.85..1.35, not 0.0..0.5.** The J-wheel moves forward as well as up when
  it floats (perpendicular to the arm). At full float its front is at about X 0.65.
- **The lid over the pocket became a lid over the queue** (X 0.7..2.3 at z 5.0), because the floating wheel sweeps
  the space the spec gave the lid.
- **The band posts are forward of and above the pivot** (X 1.0..1.4, z 5.2..5.6), out of the arms' sweep.
- **The floor ends at the ramp's top (X 5.8).** The spec's X 7.2 would sit over the ramp.

## Checked against the robot CAD and the front

`tools/robot-cad/transfer_sweep.py` (the robot on its 0.1 in grid; the new parts by exact mesh intersection):

| | |
|---|---|
| At rest | clear of the robot, except where the strips and the motor's cradle bolt to the rails (the "Launcher Concept" gives way to the turret) |
| Against the front (`cad/intake-b/`), with the roller at 0, 0.65 and 1.3 in and the extractor at 0, 75 and 150° | clear |
| The J-wheel floating to full (axle 0.95 in up) | clear of the walls, stops, posts, lid and the turret's channels |
| The raised 11-hole channel | above the walls' cut-out to 4.4 |

Before the lane goes in: raise the old intake's 11-hole channel 8 mm and remove its two pattern spacers.

## Parts

From the doc's build spec (check goBILDA numbers before ordering; "to confirm" means the type is decided):

| Part | Source | Qty |
|---|---|---|
| J motor, Yellow Jacket 1620 RPM (only this speed: the piece must reach the launcher between z 6.6 and 10) | goBILDA 5203-2402-0003 | 1 |
| 48 mm gecko wheels | as for the roller | 2 |
| 8 mm REX shafts: J shaft about 135 mm, countershaft about 30 mm, two pivot stubs | | 4 |
| 6 mm D-shafts and 6 mm-bore flanged bearings, for the strand pulleys | goBILDA 2100 series, to confirm | 2 shafts, 4 bearings |
| Flanged bearings: 1611-0514-0008 (round bore) for each arm on its stub; 1611-0514-4008 (REX) for the left stub and the countershaft | goBILDA | 2 + 2 |
| HTD5 16T pulleys, 8 mm REX bore (J shaft, two on the left stub, motor) and 9 mm belts: 40T (arm, 60 mm) and the motor's (about 3.1 in centres) | 3417-4008-0016 (to confirm), 3412 series | 4 + 2 |
| 3/16 in 83A polycord: two floor strands (about 14.8 in), the upper (about 8.3 in) and lower (about 8.7 in) drive loops | | 4 loops |
| Printed (PETG): ramp brackets, outer J and chute (two halves), the J motor's cradle, two stops, two band posts, four 0.5 in strand pulleys, the 32 mm and three 16 mm V-groove pulleys | | |
| Polycarbonate: two walls (1/8 in); floor, ramp and queue lid (1/16 in) | | |
| 1/8 in aluminium: two arms (60 mm centres); three mounting strips, 1 in wide, about 10 in, bent up at the ends | | 2 + 3 |
| 1/4 in surgical tubing, about 1 lbf preload | | 2 short lengths |
| Turret, 105 mm bore, on two 1120-series channels (the launcher's) | goBILDA 3208-0004-0001 | 1 |
