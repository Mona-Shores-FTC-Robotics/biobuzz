# The transfer: intake roller to the launcher (v3, 7 Oct 2026)

The lane from our intake roller into the mentor's flywheels, drawn in the robot CAD's frame so `dhs-transfer.step` lands
in place in Onshape beside `cad/intake-b/dhs-intake-b.step`. `build.py` holds every number. Frame: +X forward, +Y left,
+Z up, inches, origin on the floor under the chassis centre (7.56 in behind the front face). Earlier versions (the
J-wheel, then a two-feeder cup) are in git history; the cup was dropped because a ball resting on top of two wheels is
held on only by its weight, so the feeders could flick it but not drive it.

**How it works:** the intake roller pushes each ball up the ramp onto a flat lane of driven compliant rollers, under a
flat sprung ceiling that presses it onto them. The rollers push it in between two feeders under the flywheels, which
grip it by its sides; it stops against a backstop that sets it on the launch column (X -2.845, under the turret's
axis). Stopped, the feeders hold it below the flywheels' reach, and the next ball waits behind it. Feeding runs the
feeders: they drive the ball, gripped, straight up into the flywheels. One feed, one ball.

## Numbers

| | |
|---|---|
| Lane | flat, ball-bottom at z 1.3, centreline Y 0; seven shafts of 24 mm compliant rollers, 1.08 in apart, from X 5.3 to -1.18, belted from one Yellow Jacket outside the right wall. Walls (1/8 in polycarbonate) at |Y| 1.87, top z 2.6, under the front drive motors; a NECTAR clears their encoder caps by 0.06 |
| Ramp | X 8.0 z 0.05 to X 5.7 z 1.3 |
| Ceiling | flat, 1/16 in polycarbonate with 0.5 in soft foam (polyethylene or EVA, 2–3 lb/ft³), X 5.3 to -0.95, on four parallel links (1.2 in; posts at X 6.05 and 2.2). Foam face 2.6 in over the lane: a POLLEN presses the foam 0.2; a NECTAR lifts it 0.82. Bands about 1–2 lbf preload |
| Feeders | two goBILDA 72 mm Gecko wheels a side (softest durometer), shafts along X at Y ±2.72, z 3.21, over X −3.79..−1.90 (the flywheels' span), tops 0.15 under the flywheels. A POLLEN is squeezed 0.10 a side, a NECTAR 0.51. Gripped from where it enters (centre 2.70 / 3.11) up to 3.96 (POLLEN) / 4.95 (NECTAR); the flywheels take a NECTAR from 5.77 |
| Feeder drive | one Yellow Jacket (1150 RPM) per feeder, face-mounted on the launcher's front channel, straight onto the feeder shaft. Run them at the same speed |
| Backstop | X -4.67: the ball's back rests on it, so it sits centred on the column; a short floor between the feeders at the lane's height |
| Ball count | the lane takes 5 POLLEN or 4 NECTAR, so the G407 limit is kept by count: an IR break-beam across the lane at X 5.45, z 2.3 counts balls in and the code stops the intake at the limit |

## Changes it needs in the mentor's CAD

`cad/full-robot/build.py` and the "launcher" group in `build.py` apply these; the mentor decides how to make them.

- **The flywheel motors move out and up**, along X at Y ±6.7, z 7.0, on brackets on the launcher frame's side
  channels, belted to his 41T pulleys. Their old place under the flywheels (with the 7x11 plates, 1-hole channels,
  mini quad blocks, 16T pulleys, belts and the standoffs and spacers that held them) is where the feeders go.
- **The launcher's front 3-hole channels become 5-hole**, reaching down to carry the feeder shafts and motors.
- The launcher's front cross-channel and its two dual blocks go; its front U-beams move 0.25 in further apart.
- The old intake's 11-hole channel goes up 30 mm.
- His pinwheel, its servo and his two star wheels go (our extractor and this lane replace them).

## Checked

`tools/robot-cad/transfer2_check.py`, against the mentor's Robot.step (7 Oct, lined up by the rails, with the edits
above) and our front, by exact mesh intersection: nothing in the transfer or the launcher changes touches the robot or
the front, and a NECTAR and a POLLEN rolled along the lane into the feeders and driven up the column touch nothing but
the rollers, the ceiling, the feeders and the flywheels.

## Open

- **The flywheels don't touch a POLLEN** (3.19 in gap; a POLLEN is 2.80). The mentor's FeederConcept puts one flywheel
  on a sprung swing arm; the turret launcher needs the same.
- Pushing a NECTAR in between the stopped feeders takes a firm push (0.51 a side of squeeze). If the lane's rollers
  slip, put one feeder on a light sprung arm, or soften the ceiling's push less.
- 24 mm rollers and the break-beam: part numbers to choose.
