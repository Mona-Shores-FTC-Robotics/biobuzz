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
| Starting outline (R102) | **18.00 × 18.00 in**, 14.8 in tall. The flap tips are the front, the pods the back. Nothing deploys but the extractor. |
| Extractor down (R105) | 20.44 in front to back, of 24 |
| Width behind the face | 15.24 in (the simulator's frame); only the flaps are wider, at the front |
| Static clashes | none (shafts in their own hubs, belts on their own pulleys, and bolted faces excepted, by name in `ALLOWED`) |
| The roller rising 1.3 in on its swing arms | clear at 0, 0.33, 0.65, 1.0 and 1.3 in; the axle moves at most 0.04 in forward, so it rises nearly straight up |
| The extractor, every 10° from down to stowed, roller down and up | clear |
| A NECTAR and a POLLEN on their path (lane, column, ring bore, hood) | touch only what should: rollers, ceiling, feeder, pad, flywheels, hood lid |
| The 4-piece count | roller axle to backstop 12.43 in, inside the 12.26 to 12.60 window (`cad/transfer`'s) |
| Printed parts on a 210 × 210 × 200 mm bed | all fit; the largest is the tray, 160 × 208 mm |

## Layout

| | Where | Notes |
|---|---|---|
| Body | X −7.3 to 7.3 (14.6 in), 15.24 in wide over the pods | |
| Flaps | root at the front corners, tips at X 10.7, Y ±9.0 (the simulator's 3.4 in flap) | PETG-CF, 6 mm, with 3 mm of TPU printed onto the inner face and tip; they are also the extractor's side plates |
| Rails | goBILDA 1121 low-side, 336 mm, webs at \|Y\| 5.0 | low-side so the feeder's wheels pass over the flanges |
| Wheels | 96 mm mecanum, axles X ±5.41, the front ones flush with the face | wheelbase 10.82 in |
| Drive motors | each face-mounted on its pod's inner plate, belted 1:1 down to its wheel | front ones at z 6.22, over the lane (340 mm belt); rear ones straight above the axle (225 mm belt) |
| Roller | 2 in vector wheels, ±6.5 in, axle 1.0 ahead of the face, 2.4 off the tiles | as `cad/intake-b`'s, 0.06 in further back |
| Roller arms | pivot X 2.6, z 4.05; motor on the right arm at X 3.41, z 6.35, 410 mm belt to the roller | the arm rises over the front wheel; the pivot is level with the roller's mid-float, so it rises straight up |
| Lane | five TPU-roller shafts, walls at \|Y\| 1.86, ceiling of free rollers | the ball moves at the tread's speed, not half of it (below) |
| Launch column | X −2.37, the first ball against the backstop at X −4.13 | |
| Flywheels | 96 mm, axles along X at Y ±2.85, z 6.65; each in a printed cassette with its motor (315 mm belt) | |
| Feeder | 72 mm Geckos at Y 2.87, z 3.30; own 1620 RPM motor ahead of it (225 mm belt) | stopping it is the gate (the 8 Oct motor plan) |
| Turret | goBILDA 3208-0004-0001 kit on a PETG-CF top plate at z 8.85, drive gear pointing back; servo and both encoders under the plate, as `cad/modules/turret-kit` | the kit is an envelope from goBILDA's STEP |
| Electronics | tray on the front cross channel at z 9.1: both hubs stacked, the battery beside them, the Limelight mast at the front | **high**: see "Open" |

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
| 1120-0008-0216 U-channel, 216 mm | goBILDA | 1 | front cross member |
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
| WCP-0353 ×6, WCP-0354 ×6 2 in vector wheels, 48 mm gecko | WCP, goBILDA | | roller, as `cad/intake-b` |
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
3. **The turret drive under the top plate**, from `cad/modules/turret-kit` (servo, belt, encoder cradles), in place of
   the envelopes drawn here.
4. **The lane's round-belt drive and idler, the roller's spring, the ceiling's pins and bands, the pad's hinge**: as
   `cad/transfer`, redrawn on this frame.
5. **The hood's real shape**, from the launcher rig.
6. **CHECK items**: the drive motors' ratio, the hubs' and battery's sizes, the servo boxes, the pulleys' flange width.
7. **Rig tests** (doc/printed-chassis.md): the drop test (TPU, bare PETG, foam), the roller's grab rate on the swing arms, the
   free-roller lane's speed.
