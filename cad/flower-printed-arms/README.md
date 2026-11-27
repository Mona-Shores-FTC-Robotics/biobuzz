# Printed FLOWER stick arms

**Status: ready for a first print of one arm** (mentor, 27 Nov: "I'm happy to try this"). The arm's pivot, tip and
bands are the mentor's robot STEP of 26 Nov; the corner servo frame is on the real holes; the wheels' height is the
lowest hole that keeps the servo above the mecanum's rollers, which the mentor confirmed (the wheels ride on top of the
POLLEN, about 2 in off the tile). Still guessed: the gap from the servo to the wheels (6 mm) and the wheels' front screw.

The idea (mentor, 26 Nov 2026): keep the FLOWER stick's arms exactly where they are, but print them, so an arm can
ride over the corner wheel's servo, which today's flat goBILDA bar can't. The stick is now for FLOWER picks only
(no GARDEN gathers).

![Both arms, cut at their band, stick down](render_section.png)

**Passive on both robots (mentor, 26 Nov).** These arms carry no chain: a chain runs straight between its sprockets,
along the old arm line, so it would hit the corner servo and wheels where today's arm does. 19429's active roller stick
goes passive too, which also brings it back to 8 servos with the corner wheels (R503; `doc/corner-wheels.md` section 4).
The code side is a `RobotDifferences` line turning `FlowerStickSubsystem.roller` off for 19429 and `flowerRoller` out of
its XML, in its own PR.

## The stick, as a U

| Piece | What | Print / buy |
|---|---|---|
| `arm.step` / `.stl` | Each arm. **One part, print 2,** alike on both faces (the web is in the middle), so it looks the same on either side. Fit it with the hump away from the tile when the stick is down. Today's pivot hole (14 mm, for the left pivot's bearing and the servo hub) and tip hole, 240 mm apart (10 hole stations), with goBILDA's hub patterns round both. **The tip hole is keyed to the 8 mm REX shaft** (8.3 mm across the flats, corners rounded at 9.3 mm) so the shaft and the mountain on it can't turn; a clamping hub on the pattern is a backup. The roller still spins on its own bearings. Print the REX fit first (the repo's `hex_fit_coupon` on `spike/179-flower-stick-proto` is the same idea) | PETG, **on its side**, 4 walls, 40 % gyroid, no supports (the middle web bridges between the ribs). 2 per robot |
| `arm_hex_pivot.*` | The same, with a 1/2 in hex bore at the pivot, if the axle through both pivot bosses becomes hex | the same |
| `foot_skid.*` | Foot (a): a smooth puck in the tip roller's place, the roller's size, bolted to the arm so it never turns | PETG, arm face down. 2 per robot |
| the 312 mm 8 mm REX shaft | Stays, across the tips. It is the mountain's axle and the U's tip cross member | today's |
| `mountain_wide_2in` | Stays on the shaft, between hubs and spacers, as now | today's |
| the pivot axle | Through both arms' pivot bosses, driven by today's servo hub. 8 mm REX as now, or 1/2 in hex with `arm_hex_pivot` | today's |

**Why there's no printed cross piece.** With the stick down, anything spanning between the arms behind the mountain
sits 1.0 to 2.4 in off the tile, across the intake mouth: where the FLOWER is during a pick. The only room across the
tip that the FLOWER leaves free is where the shaft and the mountain already are. A printed beam as stiff as the 8 mm
steel shaft wouldn't fit there, and the mountain needs the shaft as its axle anyway (its hex bore).

## What changes on the arm

- **The hump.** Between hole stations 3 and 9 (counted from the pivot, 24 mm apart) the arm steps forward and up
  (seen with the stick down), so its lower edge passes over the corner servo, its frame and the two wheels with 4 mm
  (half a goBILDA hole) to spare through the whole 0..162° swing. `parts.py` sizes it from the corner parts in
  `geometry.py`: today **67 mm (2.64 in)** high, one goBILDA hole of slack up and back included. It is a straight
  ramp up from hole 3, flat from hole 4.7 to 6.7, and a ramp down to the old line by hole 9.1, every corner rounded to
  23 mm on the centre line (a cosine wave can't climb that fast this near the pivot without bending tighter). The
  pivot and tip holes don't move, so the stick ends where today's does, down and stowed.
- **The ends** stop 16 mm past the pivot and tip holes (the channel went 24). The arm then fits a Prusa
  250 × 210 mm bed, turned 30°.
- **The section** is a ribbed I: a 2.4 mm web in the middle, 3 mm walls round the edges, diagonal ribs, 12 mm deep
  (the channel's band), so both faces look alike. Solid round the pivot and the tip.

**Strength.** It prints on its side, so the hump's bends lie in the layers and bending runs along the strands, not
across them. A 100 N (22 lb) push along the stick puts about 6 MPa in the hump's corners, against PETG's 50. The risk is
a hard hit, and springiness at the tip: lean on one arm's tip by hand before printing the pair. Stiffer, in order: 6
walls; PETG-CF; a 36 mm wide arm (`ARM_W`, then rerun).

**Another mounting spot doesn't help.** The hump, without slack, with the frame on the channel's other hole columns:
X 208 (today's) 61.5 mm; X 200 55.5 mm; X 192 50 mm. Each column back puts the servo further over the mecanum, whose
rollers then need it a hole higher, which costs the hump more than the column saved.

**Moving the arm outboard instead** (the mentor's first choice): with the servo flush against the intake's side wall
it fills |Y| 5.4 to 6.2 in, right under today's arm (5.4 to 6.3), so the arm would have to move out about an inch to
clear it, and the wheels on the servo's spline reach |Y| 7.5 in, so it would still meet their top. The hump clears
both without moving the pivot or the tip.

## Clearance through the swing

`check.py`, 0..162° in 3° steps plus 162°, run with the 26 Nov STEP's points (`REAL_CAD_POINTS`; the stick's own
parts left out). The corner, both sides (`geometry.CORNER`):

- **The servo frame** is goBILDA's 1802-0043-0001 (`vendor/`, a 6 mm plate, 59 × 43), its long edge on the intake's
  upright 5 Hole Lowside U-Channel, its edge holes in the channel's front column of holes (X 208, every 8 mm from
  Z 80.7; mentor's option A and his circled holes, 26 Nov). The servo (goBILDA Speed, `vendor/`) comes back through it
  to X 183.8, over the front mecanum's front, so it sits on the lowest hole that keeps its body above the rollers:
  its middle at Z 104.7 mm, the axle at **3.73 in**, the wheels' bottom **1.98 in** off the tile (the mentor: the
  wheels ride on top of the POLLEN), their top 5.48 in. Axle 0.85 in outboard of the channel.
- **The wheels:** two SWYFT 3.5 in (`vendor/`), side by side, 6 mm ahead of the servo's face (a guess), with a screw
  and washer on the front (20 mm across, 8 proud; a guess).

Today's straight arm meets the frame, the servo and the wheels from hole 4.7 to 8, worst 5 to 7, as the mentor saw.
`clearance.json` has every number.

| | Left | Right |
|---|---|---|
| Today's straight arm to the corner parts | **0: hits** | **0: hits** |
| Printed arm to the wheels | 10.4 mm (0.41 in) at 162° | 11.1 mm (0.44 in) at 162° |
| Printed arm to the wheels' screw and washer | 5.0 mm (0.20 in) at 162° | 5.6 mm (0.22 in) at 162° |
| Printed arm to the servo frame | 13.3 mm (0.52 in) at 162° | 13.3 mm (0.52 in) at 162° |
| Printed arm to the servo's body | 20.0 mm (0.79 in) at 162° | 20.0 mm (0.79 in) at 162° |
| Printed arm to the servo's ears | 17.0 mm (0.67 in) at 162° | 17.0 mm (0.67 in) at 162° |
| Printed arm to the rest of the robot | 0.2 mm at 153° | 5.0 mm (0.20 in) at 162° |
| Today's arm to the rest of the robot | 0.2 mm at 156° | 5.0 mm (0.20 in) at 162° |

**Slack.** The hump is sized to clear the corner parts by 4 mm with them a goBILDA hole (8 mm) higher, further back, or
both (`parts.SIZING_SHIFTS`). Not further forward: that's the wheels' gap to the servo, which only grows if the hub
does.

| Corner parts moved | Left | Right |
|---|---|---|
| 8 mm up | 4.8 mm (wheel) | 5.5 mm (frame) |
| 8 mm back | 11.5 mm (frame) | 11.5 mm (frame) |
| 8 mm back, 8 mm up | 3.7 mm (frame) | 3.7 mm (frame) |
| 8 mm forward (not sized for) | **0.0 mm (screw)** | 0.2 mm (screw) |

- **The rest of the robot:** the 0.2 mm on the left is the left pivot's bracket, flush against the arm's inner face
  near the pivot, the same for today's arm. Nothing else comes within 5 mm.
- **R102, stowed:** the printed pieces reach X 6.38 in (the cube's front is 10.43) and Z 16.74 in (18).

## The tip, the feet and the FLOWER

**What the tip touches during a pick** (the 31 Oct CAD, `FlowerFieldPoints`, `FlowerPicker`, the pick study):

- **The tile.** Down, the stick rests on its tip rollers. Their rim holds the mountain's nose 0.44 in off the tile,
  0.09 in over the FLOWER's 0.35 in lip. Both feet keep that rim exactly, or the nose catches the lip (19429's
  14 Nov stalls).
- **Not the FLOWER.** The rollers are 5.1 in or more out from the middle; the FLOWER's base is about 2.35 in each side
  (a placeholder in `FieldSim`). Only the mountain goes under it. A pick more than about 2.7 in off centre puts a
  foot against the FLOWER's side.
- **Nearly the wall.** At `FlowerPicker`'s 9.0 in contact standoff the wall is about 13.9 in ahead of the drive axles.
  Today's roller reaches 13.4 in: half an inch short. A pose that runs long closes that.

**Which foot:** (b), **today's roller, kept where it is** (no printed part; it stays on the shaft). It rolls on the
drive in and the back out, and 20245's held stick on rollers is the only version that met the bar (4 of 4 on every
clearly seen pick). (a), the skid, slides: PETG on foam tile, over the seams, with the servo holding it down. Print
it as a spare and try it on 10 picks against the roller before trusting it. **A wheel sticking out past the end is
not offered:** at the contact standoff it would reach the wall before the FLOWER seats and stop the push short.

## Weight and the servo

`torque.py`: the gravity moment about the pivot, both arms, through the swing (kg·cm).

| | Today | Printed |
|---|---|---|
| Stick mass (both arms, tip parts, shaft, mountain) | 460 g | 422 g |
| Holding torque, stowed (0°) | 4.0 kg·cm | 3.9 kg·cm |
| Holding torque, down for a pick (162°) | 6.3 kg·cm | 6.0 kg·cm |
| Worst, through the swing | 9.1 kg·cm at 116° | 8.2 kg·cm at 119° |
| Margin to 25 kg·cm stall | 2.7× | 3.1× |

Down, the feet rest on the tile, so the servo holds less than the 162° figure. The steel shaft (123 g) and the mountain (100 g) are over half the stick and sit at the tip; the printed arms save about 15 g of arm and the tip plates and rollers.

The FLOWER-stick servo is a SWYFT (the label in the 26 Nov clip reads "BALANCE"; the Hub says Torque). SWYFT's table
gives Balance 27.3 and Torque 33.5 with no unit; read as kg·cm. The margins above are against goBILDA Torque's
25 kg·cm stall, as the Hub asked.

## Print time and weight

PrusaSlicer 2.7, PETG, 0.4 nozzle, 0.2 mm layers, 4 walls, 40 % gyroid, a 250 × 210 bed, the arm turned 30° (`slice.ini`; `slice-fast.ini`
is a faster printer). `print-estimates.json`.

| Part | PETG | Prusa MK3/MK4-class | Faster printer |
|---|---|---|---|
| arm (print 2) | 78 g | 5h 2m | 3h 18m |
| arm, hex pivot | 78 g | 5h 2m | 3h 19m |
| skid (print 2) | 6 g | 38m | 31m |

Today's aluminium arm is ESTIMATED at 75 g (weigh one).

## In Onshape

`arm.step` is in its own frame: the pivot hole on the origin, the tip hole at +X 240 mm, its faces on Z 0 and Z 12,
the hump toward +Y. Mate the pivot hole to the stick's axle and the tip hole to the shaft, in the channel's band
(the same part both sides), and the hump on the side away from the tile when the stick is down. Then swing
the stick and look at the corner servo, its frame and the wheels. If a stretch still touches, tell the session the hole station and
how much; `geometry.CORNER` takes it and every file here regenerates.

## The files

| File | What |
|---|---|
| `geometry.py` | The robot frame, the arm's line and swing (26 Nov STEP), the corner parts on their holes |
| `parts.py` | Builds the parts; sizes the hump |
| `check.py` | The swing clearance check (`clearance.json`) |
| `torque.py` | Weight and holding torque (`torque.json`) |
| `render.py` | `render_*.png` |
| `vendor/` | SWYFT's 3.5 in Intake Wheel (1/2 in hex), goBILDA's Speed servo; the 1802 servo frame is read from goBILDA's site |

```
pip install cadquery numpy trimesh scipy matplotlib
python3 cad/flower-printed-arms/parts.py && REAL_CAD_POINTS=<26 Nov STEP points .npy> python3 cad/flower-printed-arms/check.py
python3 cad/flower-printed-arms/torque.py && python3 cad/flower-printed-arms/render.py
```

## Before printing

1. Print one arm. Check the 14 mm pivot hole takes the bearing and the hub, the tip's REX key takes the shaft without
   play (drill the pattern to 4.5 if an M4 binds), and lean on the tip by hand.
2. Fit it on the robot and swing the stick slowly by hand past the corner wheels: the screw on the wheels' front
   is the closest part (5 mm). If anything is closer than drawn, tell the session the hole station and how much;
   `geometry.CORNER` takes it and every file here regenerates.
3. 10 FLOWER picks on the bench with the printed arms, counting 4 of 4, against today's.
