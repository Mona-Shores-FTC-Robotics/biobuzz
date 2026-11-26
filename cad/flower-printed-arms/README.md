# Printed FLOWER stick arms

**Status: for checking in Onshape, not for printing yet.** The corner wheel's servo and mount are placed from
photos, the mentor's description and the 26 Nov clip, not measured. Put `arm.step` (or `arm_hump40.step`) into the
robot's Onshape assembly first and look at it against the real corner (below).

The idea (mentor, 26 Nov 2026): keep the FLOWER stick's arms exactly where they are, but print them, so an arm can
ride over the corner wheel's servo, which today's flat goBILDA bar can't. The stick is now for FLOWER picks only
(no GARDEN gathers).

![Both arms, cut at their band, stick down](render_section.png)

## The stick, as a U

| Piece | What | Print / buy |
|---|---|---|
| `arm.step` / `.stl` | Each arm. **One part, print 2,** alike on both faces (the web is in the middle), so it looks the same on either side. Fit it with the hump away from the tile when the stick is down. Today's pivot hole (14 mm, for the left pivot's bearing and the servo hub) and tip hole, 240 mm apart (10 hole stations), with goBILDA's hub patterns round both. **The tip hole is keyed to the 8 mm REX shaft** (8.3 mm across the flats, corners rounded at 9.3 mm) so the shaft and the mountain on it can't turn; a clamping hub on the pattern is a backup. The roller still spins on its own bearings. Print the REX fit first (the repo's `hex_fit_coupon` on `spike/179-flower-stick-proto` is the same idea) | PETG, **on its side**, 4 walls, 40 % gyroid, no supports (the middle web bridges between the ribs). 2 per robot |
| `arm_hex_pivot.*` | The same, with a 1/2 in hex bore at the pivot, if the axle through both pivot bosses becomes hex | the same |
| `arm_hump40.*` | A 40 mm hump, for comparing in Onshape (smaller than the sized 53.5 mm one, which has a hole of slack) | — |
| `foot_skid.*` | Foot (a): a smooth puck in the tip roller's place, the roller's size, bolted to the arm so it never turns | PETG, arm face down. 2 per robot |
| the 312 mm 8 mm REX shaft | Stays, across the tips. It is the mountain's axle and the U's tip cross member | today's |
| `mountain_wide_2in` | Stays on the shaft, between hubs and spacers, as now | today's |
| the pivot axle | Through both arms' pivot bosses, driven by today's servo hub. 8 mm REX as now, or 1/2 in hex with `arm_hex_pivot` | today's |

**Why there's no printed cross piece.** With the stick down, anything spanning between the arms behind the mountain
sits 1.0 to 2.4 in off the tile, across the intake mouth: where the FLOWER is during a pick. The only room across the
tip that the FLOWER leaves free is where the shaft and the mountain already are. A printed beam as stiff as the 8 mm
steel shaft wouldn't fit there, and the mountain needs the shaft as its axle anyway (its hex bore).

## What changes on the arm

- **The hump.** Around hole stations 5 to 7 (counted from the pivot, 24 mm apart) the arm bows forward and up (seen
  with the stick down), so its lower edge passes over the corner wheel's servo and mount with 4 mm (half a goBILDA
  hole) to spare through the whole 0..162° swing. `parts.py` sizes it from the corner parts in `geometry.py`: today
  53.5 mm high, one goBILDA hole of slack included. It leaves the old line at hole 3.3 and is back on it by hole 10.1; the arm's centre line never bends
  tighter than a 22 mm radius, which sets how long the ramps are. The pivot and tip holes don't move.
- **The ends** stop 16 mm past the pivot and tip holes (the channel went 24). The arm then fits a Prusa
  250 × 210 mm bed, laid corner to corner, and its tip no longer pokes past the roller.
- **The section** is a ribbed I: a 2.4 mm web in the middle, 3 mm walls round the edges, diagonal ribs, 12 mm deep
  (the channel's band), so both faces look alike. Solid round the pivot and the tip.

**Moving the arm outboard instead** (the mentor's first choice): with the servo flush against the intake's side wall
it fills |Y| 5.4 to 6.2 in, right under today's arm (5.4 to 6.3), so the arm would have to move out about an inch to
clear it, and the wheels on the servo's spline reach |Y| 7.5 in, so it would still meet their top. The hump clears
both without moving the pivot or the tip.

## Clearance through the swing

`check.py`, 0..162° in 3° steps plus 162°. The corner: the SWYFT wheel from its STEP (`vendor/`), two side by side
(25.4 mm), and the servo and its mount as boxes: the servo upright, flush against the intake's side wall, under the arm, just
ahead of the mecanum (mentor's arrows, 26 Nov; `geometry.CORNER`, from the CAD, not measured on the robot). Today's
straight arm meets the servo and the wheels' top from hole 5.4 to 8.2, as the mentor saw. The rest of the robot: the 31 Oct CAD (no corner wheels, no transfer).
`clearance.json` has every number.

| | Left | Right |
|---|---|---|
| Today's straight arm to the wheels and servo | **0: hits** | **0: hits** |
| Printed arm to the wheels | 14.3 mm (0.56 in) at 162° | 14.3 mm (0.56 in) at 162° |
| Printed arm to the wheels' screw and washer | 12.6 mm (0.50 in) at 162° | 12.6 mm (0.50 in) at 162° |
| Printed arm to the servo | 29.2 mm (1.15 in) at 162° | 29.2 mm (1.15 in) at 162° |
| Printed arm to the mount | 25.2 mm (0.99 in) at 162° | 25.2 mm (0.99 in) at 162° |
| Printed arm to the rest of the robot (31 Oct CAD) | 0.1 mm (0.00 in) at 159° | 9.1 mm (0.36 in) at 162° |
| Today's arm to the rest of the robot | 0.1 mm (0.00 in) at 162° | 3.8 mm (0.15 in) at 162° |

**Slack.** The hump is sized so it still clears the corner parts by 4 mm if they sit a goBILDA hole (8 mm) further forward, back, higher, or a mix (`parts.SIZING_SHIFTS`). Measured gaps with them moved:

| Corner parts moved | Left | Right |
|---|---|---|
| corner parts 8 mm forward, 0 mm up | 4.6 mm (screw) | 4.6 mm (screw) |
| corner parts 0 mm forward, 8 mm up | 6.3 mm (wheel) | 6.4 mm (wheel) |
| corner parts 8 mm forward, 8 mm up | 4.3 mm (wheel) | 4.4 mm (wheel) |
| corner parts 8 mm back, 0 mm up | 14.9 mm (wheel) | 14.9 mm (wheel) |
| corner parts 8 mm back, 8 mm up | 6.9 mm (wheel) | 6.9 mm (wheel) |

- **The wheels' screw and washer** (mentor, 26 Nov): drawn 20 mm across, 8 mm proud of the wheels' front face, on the axle. A guess at its size.
- **The rest of the robot:** the printed arm comes no closer than today's channel does. The 0.1–0.2 mm on the left is at the pivot end, beside the left pivot's hub and block, the same for both.
- **R102, stowed:** the printed pieces reach X 6.71 in (the cube's front is 10.43) and Z 16.42 in (18). The corner wheels as drawn reach 10.65 in with the screw, past the cube's front, but the mentor says the real ones sit behind the nose (1.7 in past the intake face), so they're drawn too far forward; the slack covers them 8 mm further back.
- **The Limelight:** no part of either printed arm enters its view (54.5° × 42°, pitched 45° up, from the 31 Oct CAD's camera) at any angle of the swing; the nearest is 16.7° outside the view's edge, the left arm at 56°.

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
| Stick mass (both arms, tip parts, shaft, mountain) | 460 g | 414 g |
| Holding torque, stowed (0°) | 4.0 kg·cm | 3.8 kg·cm |
| Holding torque, down for a pick (162°) | 6.3 kg·cm | 5.8 kg·cm |
| Worst, through the swing | 9.1 kg·cm at 116° | 8.1 kg·cm at 118° |
| Margin to 25 kg·cm stall | 2.7× | 3.1× |

Down, the feet rest on the tile, so the servo holds less than the 162° figure. The steel shaft (123 g) and the mountain (100 g) are over half the stick and sit at the tip; the printed arms save about 15 g of arm and the tip plates and rollers.

The FLOWER-stick servo is a SWYFT (the label in the 26 Nov clip reads "BALANCE"; the Hub says Torque). SWYFT's table
gives Balance 27.3 and Torque 33.5 with no unit; read as kg·cm. The margins above are against goBILDA Torque's
25 kg·cm stall, as the Hub asked.

## Print time and weight

PrusaSlicer 2.7, PETG, 0.4 nozzle, 0.2 mm layers, 4 walls, 40 % gyroid, a 250 × 210 bed (`slice.ini`; `slice-fast.ini`
is a faster printer). `print-estimates.json`.

| Part | PETG | Prusa MK3/MK4-class | Faster printer |
|---|---|---|---|
| arm (print 2) | 74 g | 4h 53m 46s | 3h 15m 41s |
| arm, hex pivot | 74 g | 4h 53m 46s | 3h 15m 35s |
| skid (print 2) | 6 g | 37m 39s | 31m 28s |

Today's aluminium arm is ESTIMATED at 75 g (weigh one).

## In Onshape

`arm.step` is in its own frame: the pivot hole on the origin, the tip hole at +X 240 mm, its faces on Z 0 and Z 12,
the hump toward +Y. Mate the pivot hole to the stick's axle and the tip hole to the shaft, in the channel's band
(the same part both sides), and the hump on the side away from the tile when the stick is down. Then swing
the stick and look at the corner servo and mount. If a stretch still touches, tell the session the hole station and
how much; `geometry.CORNER` takes it and every file here regenerates.

## The files

| File | What |
|---|---|
| `geometry.py` | The robot frame, the arm's line and swing, the corner parts (placed, not measured) |
| `parts.py` | Builds the parts; sizes the hump |
| `check.py` | The swing clearance check (`clearance.json`) |
| `torque.py` | Weight and holding torque (`torque.json`) |
| `render.py` | `render_*.png` |
| `vendor/` | SWYFT's STEP of the 3.5 in Intake Wheel (1/2 in hex), from the mentor |

```
pip install cadquery numpy trimesh scipy matplotlib
python3 cad/flower-printed-arms/parts.py && python3 cad/flower-printed-arms/check.py
python3 cad/flower-printed-arms/torque.py && python3 cad/flower-printed-arms/render.py
```

## Before printing

1. In Onshape, both corners: the arm (53.5 mm hump) against the real servo and mount, swung
   through.
2. Print one arm first; check the 14 mm holes take the bearing and the hubs (drill to 4.5 if an M4 binds), and that
   the axle and shaft run true.
3. 10 FLOWER picks on the bench with the printed arms, counting 4 of 4, against today's.
