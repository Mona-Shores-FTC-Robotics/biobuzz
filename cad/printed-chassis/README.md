# The printed clean-sheet robot: layout v1 (10 Oct 2026)

Issue #172. The concept and its reasoning are in [doc/printed-chassis.md](../../doc/printed-chassis.md). This is the
first CAD of it: every module at its real size and place, from goBILDA's own STEP files where a part is bought, checked
for clashes. **Screws, gear teeth, springs and the lane's drive belt are not drawn yet** (v2). Nothing here is built.

    python3 cad/printed-chassis/build.py          # checks, then writes printed-chassis.step, stl/, parts.md, views/
    CHECK_ONLY=1 python3 cad/printed-chassis/build.py

Frame: +X forward, +Y left, +Z up, inches, origin on the floor under the body's centre; the STEP is in millimetres.

![Front right](views/three-quarter.png)
![From the left](views/side.png)

## What the checks say

| Check | Result |
|---|---|
| Starting outline (R102) | **17.75 × 17.75 in**, 14.8 in tall: designed 1/8 in a side inside the 18 in cube, for print error, soft faces, screw heads and cables (v2's screws must stay inside it too). The flap tips are the front, the rear wheels the back. Nothing deploys but the extractor. |
| Extractor down (R105) | 20.19 in front to back, of 24 |
| Width behind the face | 15.24 in (the simulator's frame); only the flaps are wider, at the front |
| Static clashes | none (shafts in their own hubs, belts on their own pulleys, and bolted faces excepted, by name in `ALLOWED`) |
| The roller rising 1.3 in on its swing arms | clear at 0, 0.33, 0.65, 1.0 and 1.3 in; the axle moves at most 0.04 in forward, so it rises nearly straight up |
| The extractor, every 10° from down to stowed, roller down and up | clear |
| A NECTAR and a POLLEN on their path (the mouth's edges where they meet the stars, the lane, column, ring bore, hood) | touch only what should: roller, stars, floor, lane rollers, ceiling, feeder, pad, flywheels, hood lid |
| The 4-piece count | roller axle to backstop 12.43 in, inside the 12.26 to 12.60 window (`cad/transfer`'s). The backstop sits on ±0.2 in slots, which reach 12.23 to 12.63, so it's set on the robot with real pieces |
| Printed parts within 180 mm each way | all fit (room for a brim on a 210 mm bed, and less warp); the largest is half the top plate, 175 × 102 mm. The tray, the top plate and the lane walls are each printed in two halves and bolted |

## In Onshape

`printed-chassis.step` is the whole robot in one file (about 2 MB, millimetres, origin on the floor under the body's
centre, +X forward, +Z up). It's for looking, measuring and checking fits: the parts arrive as solids with no
features, so anything to be edited is changed in `build.py` and re-exported.

1. **Create → Document → Import** `printed-chassis.step`. Leave **Flatten assembly off**.
2. The assembly arrives in groups, as `cad/full-robot`'s do. Right-click **FRAME - fix** → **Fix**, then lock each
   **MOVES** group (the lock icon at the right of its row) so its parts move together.
3. Add the mates the groups' names give (`cad/full-robot/ONSHAPE.md`, section 2, explains Revolute mates):

| Group | Mate | Limits |
|---|---|---|
| MOVES 1 intake arms | Revolute about the arms' pivot (a round edge of `intake_pivot_L` on the Y axis) | 0 to 13° (the roller rises 1.3 in) |
| MOVES 2 extractor | Revolute about the stub shafts | 0 (down) to 146° (stowed, as drawn) |
| MOVES 3 to 6 wheels | Revolute on each wheel's shaft | none |
| MOVES 7 and 8 star wheels | Revolute on each star's vertical shaft | none |

Colours: blue is printed, grey is bought, yellow is a soft face (TPU or foam).

## Layout

| | Where | Notes |
|---|---|---|
| Body | X −7.05 to 7.3 (14.35 in), 15.24 in wide over the pods | the 1/4 in for the start margin comes off the back, so the flaps keep their 3.4 in |
| Flaps | root at the front corners, tips at X 10.7, Y ±8.875 (3.4 in ahead, 1.255 in out) | PETG-CF, 6 mm, with 3 mm of TPU printed onto the inner face and tip; they are also the extractor's side plates |
| Rails | goBILDA 1121 low-side, 336 mm, webs at \|Y\| 5.0 | low-side so the feeder's wheels pass over the flanges |
| Wheels | 96 mm mecanum, axles X 5.41 and −5.16, flush with the face and the back | wheelbase 10.57 in |
| Drive motors | each face-mounted on its pod's inner plate, belted 1:1 down to its wheel | front ones at z 6.22, over the lane (340 mm belt); rear ones straight above the axle (225 mm belt) |
| Roller | goBILDA 48 mm Gecko wheels, as the mentor's, ±4.9 in, axle 1.0 ahead of the face, 2.4 off the tiles | no vector wheels: his star wheels centre the pieces |
| Star wheels (the mentor's) | two 3.5 in flexible stars lying flat, 2.36 in behind the face, ±3.15 in (tips 2.80 apart, as his), at z 3.05: the balls' middles where the ramp has lifted them | each driven from above by a continuous servo through a one-way bearing, hung from the front cross channel. **Drive parts TBD**: his CAD draws none, so the servo, shaft and one-way bearing are placeholders |
| Ramp and mouth floor | the whole mouth's width (±4.45, inside the rails), in halves | so a piece taken in off-centre climbs to the lane's height too, where the stars reach it |
| Roller arms | pivot X 2.6, z 4.05; motor on the right arm at X 3.41, z 6.35, 410 mm belt to the roller | the arm rises over the front wheel; the pivot is level with the roller's mid-float, so it rises straight up |
| Lane | five TPU-roller shafts, walls at \|Y\| 1.86, ceiling of free rollers | the ball moves at the tread's speed, not half of it (below) |
| Launch column | X −2.37, the first ball against the backstop at X −4.13 | |
| Flywheels | 96 mm, axles along X at Y ±2.85, z 6.65; each in a printed cassette with its motor (315 mm belt) | |
| Feeder | 72 mm Geckos at Y 2.87, z 3.30; own 1620 RPM motor ahead of it (225 mm belt) | stopping it is the gate (the 8 Oct motor plan) |
| Turret | goBILDA 3208-0004-0001 kit on a PETG-CF top plate (two halves) at z 8.85, its drive gear 40° left of straight back so its lobe stays inside the back; the Speed servo belted 1:1 to the 64T's shaft, encoder A on that shaft, B on a 36T meshing the 64T (1178°, an OctoQuad on I2C bus 2), all under the plate as `cad/modules/turret-kit` | the kit is an envelope from goBILDA's STEP |
| Electronics | tray (two halves) on the front cross channel at z 9.1, notched round the turret kit's corner: both hubs stacked, the battery beside them, the Limelight mast at the front | **high**: see "Open" |

### What changed from the concept, and why

- **The lane's ceiling is free rollers, not a driven top run.** A ball pressed on a moving tread under a ceiling that
  can't push back rides at the tread's speed; under today's still foam it rolls at half. Free rollers give the same
  speed as a driven top run with no drive, no reversing gears and no belt to a floating part.
- **The roller's motor rides its arm.** A motor on the frame at the pivot needed a concentric bearing round its own
  shaft; on the arm, its belt to the roller keeps its length by itself. The lane still takes its drive from the
  roller, by `cad/intake-b`'s round belt and idler (not drawn yet).
- **The turret is goBILDA's kit** (the user, 10 Oct), not a printed ring: with `cad/modules/turret-kit`'s servo and
  two-encoder drive, its drive gear pointing back so it clears the tray.
- **The flaps are PETG-CF with TPU printed onto their faces** (the dual-material printer), not foam on a backer. A flap
  that yields at its root on a hit (the simulator chat's idea) doesn't fit here: the flap also carries the extractor's
  stub, so it has to stay stiff; only its face and tip are soft.
- **The roller is the mentor's: 48 mm Geckos, no vector wheels, and his two flexible star wheels centre the pieces**
  (the user, 10 Oct: he likes them; geometry from his 9 Oct Robot.step). His stars sit at floor height because his
  pieces stay on the floor; here they're lifted to the lane's height by a ramp the whole mouth wide, so the stars sit
  at the balls' middles. The front cross channel now rests on the front pods (no uprights), so the mouth is open to
  its full width.
- **The rails are low-side channel (as today's), not 1120.** A 48 mm deep flange put the rail in the feeder's wheels.
- **All four pods are belted**, the motor above the wheel. Direct drive at axle height can't cross the lane at the
  front, and the rear ones match the front.
- **The front drive motors can't come out with their pod's four screws alone**: their belt is short enough to slip off,
  but the front cross channel sits over them. v2 has to make that path, or accept it.

## Bought parts (sources)

goBILDA part numbers are from goBILDA's site (Oct 2026). Counts are for one robot.

| Part | Source | Count | For |
|---|---|---|---|
| 1121-0013-0336 low-side U-channel, 336 mm | goBILDA | 2 | rails |
| 1120-0009-0240 U-channel, 240 mm | goBILDA | 1 | rear cross member |
| 3213-3606-0002 96 mm mecanum wheel set | goBILDA | 1 set | drive |
| 5203 Yellow Jacket, the drive's current ratio | goBILDA | 4 | drive (the CAD draws the two-stage length) |
| 5203-2402-0003 Yellow Jacket, 1620 RPM | goBILDA | 2 | roller and lane; feeder |
| 5203-2402-0001 Yellow Jacket, 6000 RPM | goBILDA | 2 | flywheels |
| 3417-4008-0024 24T HTD5 pulley, 8mm REX | goBILDA | 18 | every belt is 1:1 (2 of them the turret's) |
| 3412-0009-0340 / -0225 / -0410 / -0315 HTD5 belts | goBILDA | 2 / 3 / 1 / 2 | front drive / rear drive and feeder / roller / flywheels |
| 1611-0514-4008 flanged bearing, 8mm REX | goBILDA | about 30 | wheels, roller, lane, flywheels, feeder, extractor |
| 8mm REX shafts and standoffs | goBILDA | stacks | as `parts.md` |
| 2000-0025-0002 Torque servo | goBILDA | 1 | extractor |
| 2000-0025-0003 Speed servo | goBILDA | 1 | turret (continuous) |
| 3632-4008-0048 48 mm Gecko wheels | goBILDA | about 10 | the roller, as the mentor's |
| 3.5 in OD flexible star wheels, 7 mm hex bore | as the mentor's (vendor TBD) | 2 | centre pieces into the lane |
| Continuous servos, one-way bearings, 7 mm hex shafts | as the mentor's (TBD) | 2 each | drive the star wheels |
| 1120-0010-0264 U-channel, 264 mm | goBILDA | 1 | front cross member, on the front pods (replaces the 216 mm one and its printed uprights) |
| 3632-0014-0072 72 mm Gecko, softest | goBILDA | 2 | feeder |
| 96 mm flywheels | as the mentor's launcher | 4 | |
| 608 bearings | any | 8 | ceiling rollers |
| 3208-0004-0001 gear-driven turret kit | goBILDA | 1 | turret |
| 2303-4008-0036 36T mod 0.8 pinion, servo hub, 3412-0009-0295 belt | goBILDA | 1 each | turret drive and encoder B (`cad/modules/turret-kit`) |
| REV-11-1271 Thru-Bore encoder, OctoQuad | REV, Digital Chicken Labs | 2, 1 | turret angle |
| Foam, 1/2 in | any | | the pad; the flaps only if the drop test says TPU bounces |
| M3 / M4 heat-set inserts, M4 socket heads | any / goBILDA | | v2 counts them |

Every printed part is listed in `parts.md` with its material. The STLs in `stl/` are **v1 layout shapes, not ready to
print**: no screw holes, inserts, bores or ribs yet. They show size and place, and that each fits the bed.

## Open (v2, and before anything is printed)

1. **Screws, inserts and service paths**, drawn and checked as `cad/intake-b` and `cad/transfer` do
   (`tools/robot-cad/fastener_check.py`).
2. **The electronics are high** (hubs and battery at z 9 to 11). Look for a lower home for the battery.
3. **Fit the kit's footprint and the turret-kit drive plate onto the printed back third**: the kit's own mounting pattern
   in the top plate, and `cad/modules/turret-kit`'s servo, belt and encoder cradles in place of the envelopes here.
4. **The lane's round-belt drive and idler, the roller's spring, the ceiling's pins and bands, the pad's hinge**: as
   `cad/transfer`, redrawn on this frame.
5. **The hood's real shape**, from the launcher rig.
6. **CHECK items**: the drive motors' ratio, the hubs' and battery's sizes, the servo boxes, the pulleys' flange width.
7. **Rig tests** (doc/printed-chassis.md): the drop test (TPU, bare PETG, foam), the roller's grab rate on the swing arms, the
   free-roller lane's speed.
