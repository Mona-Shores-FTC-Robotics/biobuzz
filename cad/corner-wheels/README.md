# Corner wheels: prototype 3, fitted to the robot's own CAD

These parts put a TPU flap wheel on a vertical axle at each front corner, ahead of the front drive wheel, turned inward
by a CR servo. They rake a POLLEN at the robot's corner, or one pinned on a wall there, into the intake mouth. Why, and
the rules: [`doc/corner-wheels.md`](../../doc/corner-wheels.md).

**Prototype 3 (24 Nov 2026)** is placed on the mentor's STEP export of the whole robot as it is now, not the 31 Oct
model:

- The wheel sits **right in front of the front drive wheel**. The servo above it stays **inboard of the FLOWER stick's
  arm**, spline down, its body **lying side to side, inboard of the spline**: nothing reaches back toward the
  intake, where the real robot has no room (mentor, 24 Nov 2026).
- The printed bracket bolts to the **front flange of the intake's side channel**, the 5 Hole Lowside U-Channel the
  intake's Viper slide runs inside. Its two M4 nuts sit in the gap ahead of the slide, so nothing reaches the slide.
- It **wraps the channel on three sides**, all one part:
  - **Front:** a 5 mm tab over the whole front flange, two M4 through it (slots: 2 mm up and down, 0.5 mm sideways), and bumps in the flange's other three holes.
  - **Outer face:** a wing with four bumps in the web's holes.
  - **Bottom:** a belly under the channel's open end, two M4 up into the Dual Block there.
  The screws hold it on; the seven bumps stop it turning or sliding when the wheel takes a hit.
- The servo is a **SWYFT Speed** (mentor's choice: more robust), in continuous mode.

The modules are drawn for the **right** corner's stick arm (|Y| 6.60..7.08). **The left module is its mirror image**,
which assumes the left arm moves out to match the right. Today the left arm is at |Y| 5.99..6.47, right where the left
servo goes: **the left corner needs that arm moved first.**

![Right corner module](../../doc/corner-wheels/right-corner-module.png)

## Where it bolts

**To the intake side channel's front flange,** the narrow strip of the channel that faces forward, 12 mm wide, just
ahead of the front drive wheel. It has a row of 4 mm holes, 8 mm apart, from Z 3.18 in up.

- **The holes:** at |Y| 5.05 in. The tab bolts through two of them, **Z 3.81 and 4.44 in**, the 3rd and 5th from
  the bottom, and has a bump in the 1st, 2nd and 4th.
- **The bolts:** M4 × 12 from the front, with nuts behind the flange.
- **Why not the two lowest holes:** behind them a nut would sit only 0.02 in above the Dual Block inside the channel.
- **The bumps:** tapered cones, 3.8 mm across at the bracket to 2.4 mm at the tip, 1.8 mm tall, so they find a hole up to about 0.7 mm off (sand one flush if it still won't seat). The wing's four go into the web's holes at X 7.88 and 8.18 in, Z 3.48 and 4.13 in. The web is
  2.5 mm thick, so they stop short of the slide inside.
- **The belly:** the channel's bottom end is open, with the Dual Block's bottom face flush at Z 2.86 in. The frame rail
  under it stops at X 7.55, so the block's holes at X 7.87 and 8.18 (|Y| 5.05) are open from below. The CAD draws the
  8.18 hole tapped (3.3 mm) and the 7.87 one at 4.2 mm, so check which ones thread.

## Parts per robot

| Item | Qty | Notes |
|---|---|---|
| `flap_wheel` (print) | 2 | **One file for both corners**: the same either way up. **Left corner:** this way up (it turns clockwise from above, flaps trailing). **Right corner:** upside down. **TPU 95A**, 3 walls, 30 % infill, no supports. 2.3 in (58 mm) across, 36.7 mm tall |
| `bracket_left` / `bracket_right` (print) | 1 each | **PETG**, 4 walls, 40 % infill. Mirror images |
| SWYFT Speed servo, set to continuous rotation | 2 | 40 × 20 body, 48 × 10 mm flange holes (5 mm), 25-tooth spline |
| goBILDA 1908 Series Servo Hub (25 Tooth Spline, 32mm Diameter) | 2 | Counterbored face toward the wheel |
| M4 × 30 socket head | 8 | Wheel to hub, from below, heads in the wheel's counterbores |
| M4 × 10 self-tapping (or pan head into a 3.3 mm pilot) | 8 | Up through the servo's flange into the shelf |
| M4 × 12 and M4 nuts | 4 + 4 | Tab to the channel's front flange |
| M4 × 10 button head | 4 | Up through the belly into the Dual Block (two per corner, where the holes thread) |

## The stack, from the floor up (inches)

| Part | Z |
|---|---|
| Wheel flaps | 0.60..1.65 (a POLLEN's middle, 1.40, inside them); the wheel's bosses 0.40..1.85 |
| Hub | 1.85..2.16 |
| Servo boss and top cap, then flange (upside down) | 2.16..2.66 |
| Shelf | 2.66..2.91 |
| Servo body | up to 3.80 |
| Walls in front of and behind the servo's body | 2.66..3.64 |
| Belly, under the channel | 2.66..2.86 |
| Tab | 2.66..4.70 |
| Wing, on the channel's outer face | 2.66..4.75 |

**Positions:**
- **Axle:** X 9.22, |Y| 5.95, right in front of the drive wheel (|Y| 5.44..6.90).
- **Servo:** X 8.83..9.61, |Y| 4.49..6.62 with its flange, all ahead of the channel's front flange (X 8.50).
  Its inboard end is over the intake mouth's edge, at Z 2.56 and up: a POLLEN in the mouth clears it by about 0.14 in.
- **Wheel:** X 8.08..10.36, |Y| 4.81..7.09. Its inner edge reaches the intake mouth's edge (4.87); its outer edge
  stays inside the frame's side (7.77).

## Clearances in the robot's CAD

Measured with the stick swept 162° up from its down pose, and each mecanum spun a full turn. Right corner:

| | Stick's swing | Spinning mecanum |
|---|---|---|
| Servo | 0.34 in | 1.25 in |
| Bracket | 0.32 in | 0.26 in (the wing and belly, on the channel) |
| Wheel | 1.28 in | 0.52 in |
| Hub | 1.21 in | 1.00 in |

**Size:**
- **Length:** front X 10.36, which is **17.93 in from the back wall** (X −7.57). With the stick retracted it stays
  within X −0.16..6.82, so the corner wheels are the front of the robot.
- **Width:** nothing past the frame's sides.
- **The wheel is 2.3 in.** The axle had to come forward far enough for the servo's rear mounting tab to clear the
  spinning mecanum by 0.25 in, and a bigger wheel would then pass the 18 in cube's front.

## Check on the robot before printing

0. Take off the angled guide at each front corner that funnels pieces into the intake (the corner wheels do its job).
   It isn't in the robot's CAD, so the clearances here assume it's gone.
1. Measure from the back wall to the front of the drive wheels. In the CAD it's 15.14 in. The corner wheels add
   2.79 in beyond that, to 17.93 in.
2. Find the side channel's front-flange holes at 3.81 and 4.44 in up from the tile. Check that an M4 nut fits behind
   each, in front of the Viper slide.
3. Check which of the Dual Block's two front holes (X 7.87 and 8.18 in from the axle middle, under the channel)
   thread an M4.
4. Check the four web holes the bumps go in are open on the channel's outside (X 7.88 and 8.18 in, Z 3.48 and
   4.13 in), and the bumps don't touch the slide.
5. Set both SWYFT servos to continuous rotation with SWYFT's programmer before fitting.
6. Lower the stick slowly by hand past the servo before running it powered.

## Files

| File | What |
|---|---|
| `corner_module_left.step`, `corner_module_right.step` | Each corner assembled in the robot frame (inches in the comments, mm in the file): bracket, servo, hub, wheel, screws |
| `flap_wheel.*`, `bracket_*.*` | The printed parts |
| `parts.py` | Makes them. Every size is a constant at the top. `pip install cadquery` |
| `drawings.py` | The pictures and the clearances, from the robot's STEP (not in git: pass its path) |
