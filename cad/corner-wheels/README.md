# Corner wheels: first prototype

These parts put a TPU flap wheel on a vertical axle at each front corner, ahead of the front drive wheels, spun
inward by a CR servo. They rake a POLLEN at the robot's corner, or one pinned on a wall there, into the intake
mouth. Why, where they go, and the rules: [`doc/corner-wheels.md`](../../doc/corner-wheels.md).

**This is a prototype for the build team.** It was placed from the CAD of 31 Oct. Before you print, check both
corners on each robot with a ruler against the numbers below.

![Left corner](../../doc/corner-wheels/left-corner-top.png)

## The parts

| File | What it is | Print |
|---|---|---|
| `flap_wheel_left.*` | A 2.5 in (63 mm) wheel, 32 mm tall (26.7 mm of flaps and a 5 mm boss). Eight curved flaps, 2.2 mm thick. Seen from above it turns clockwise | **TPU 95A**, boss down, 3 walls, 30 % infill, no supports. 1 per robot |
| `flap_wheel_right.*` | Its mirror image. It turns the other way, so its flaps curve the other way | the same. 1 per robot |
| `bracket_left.*` | The servo plate and an ear with two M4 slots 16 mm apart. The ear bolts to the outer face of the intake's left side channel (the 5-hole Lowside U-Channel), ahead of the drive wheel | **PETG**, plate down, 4 walls, 40 %, no supports. 1 per robot |
| `bracket_right.*` | The right one bolts to the **front** face of the short 2-hole channel in front of the right drive wheel, and its servo runs forward from the spline. In the CAD the right stick arm is 0.45 in further out than the left, and that channel is in the way | the same. 1 per robot |
| `*.step` | The same parts as solids. Import these into Onshape, not the STLs | |
| `parts.py` | Makes them. Every size is a constant at the top | `pip install cadquery` |

## Where they sit

Robot frame, inches: +X forward, +Y left, +Z up, origin on the floor under the middle of the drive axles.

**The wheels:**

- **Axle:** X 8.94, Y +6.85 (left) and Y −6.95 (right).
- **Height:** the flaps span Z 0.60..1.65, so a POLLEN meets them at its equator (Z 1.40). Space the wheel with
  washers to get there.
- **Reach:** each rim comes 2.62 in ahead of the drive wheels and about 0.45 in past the frame's side.
- **Spin:** the front of each wheel moves inward. Fit the wheel so its flaps' tips trail.

**The servo and bracket:**

- The plate is Z 3.40..3.65. The servo hangs under it, upside down, spline down over the wheel.
- The servo is a standard size: goBILDA 2000 series, REV Smart Robot Servo or an Axon. Its body is 40 × 20 mm and
  its flange holes are 48 × 10 mm. Fix it with M3 self-tappers, or drill the holes to 3.4 for bolts.
- The wheel's boss takes a goBILDA hub. It has 4 × M4 holes on a 16 mm circle and 4 on a 16 mm square, so either
  pattern fits, and an 8.3 mm bore. A 25T servo hub, or the **Servo Hub Shaft - 16mm** with a hyper hub, joins it
  to the servo.

**Measured in the CAD**, with the stick swept through its whole 0..162° swing:

| | Left | Right |
|---|---|---|
| Wheel to the stick | 0.75 in | 0.56 in |
| Servo to the stick | 0.58 in | 0.24 in |
| Bracket to the stick | 0.21 in | 0.21 in |
| Wheel to the frame | 0.34 in | 0.08 in, under that short channel |

## Check before printing, on each robot

1. **Put the stick down and measure its arm's outer face from the robot's centre line.** The CAD says 5.88 in
   (left) and 6.32 in (right).
   - The servo's inner side sits at |Y| 6.45 (left) and 6.55 (right).
   - On 19429, also check the blue GARDEN guide.
2. **Look at what's in front of each drive wheel.** The left bracket wants the intake side channel's outer face
   clear from X 7.60 to 8.50, and Z 2.95 to 3.65.
   - The right bracket wants the short channel's front face there.
   - If it's missing, a bracket that bolts to the side channel like the left one is a one-line change in
     `parts.py` (`SIDES["right"]`).
3. **Lower the stick slowly by hand past the servo** before running it powered.

**Re-run `parts.py` after any change, and commit the outputs.**
