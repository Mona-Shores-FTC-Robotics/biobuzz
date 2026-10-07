# The transfer: intake roller to the launcher (v3, 7 Oct 2026)

> **This reworks the mentor's launcher**, not just adds to it: the whole launcher and turret move forward
> 0.8 in, his flywheel motors move out and up, the plates, blocks and standoffs that held them under the
> flywheels go, and so do his launcher's front cross-channel and front U-beams. Show him this before anything is
> ordered. The list is under "Changes it needs in the mentor's CAD".

The lane from our intake roller into the mentor's flywheels, drawn in the robot CAD's frame so `dhs-transfer.step` lands
in place in Onshape beside `cad/intake-b/dhs-intake-b.step`. `build.py` holds every number. Frame: +X forward, +Y left,
+Z up, inches, origin on the floor under the chassis centre (7.56 in behind the front face). Earlier versions are in git
history.

**Build and service come first.** Where a feature cost a lot of parts or a hard pit repair, it went: one driven feeder
against a sprung pad instead of two feeders on swing arms with gears; a ceiling on pins in slots instead of links; a
twisted polycord drive instead of miter gears. See "Building and servicing it".

**How it works:** the intake roller pushes each ball up the ramp onto a flat lane of driven compliant wheels (the
mentor's offset-wheel design, scaled to the lane's height) under a flat sprung ceiling that presses it onto them. The
lane pushes it in between the feeder (left, driven) and a sprung foam pad (right), under the flywheels; it stops against
a backstop that sets it on the launch column (X -2.045, under the turret's axis). Stopped, the feeder and pad hold
it below the flywheels' reach, and the rest queue nose to tail behind it. Feeding runs the feeder: it drives the ball,
pinched against the pad, straight up into the flywheels. One feed, one ball.

**The count is the lane's length.** Balls queue nose to tail from the backstop to the intake roller's axle (X
8.62, the roller's 2 in vector wheels), and a ball is held once its centre is behind it. The team's rule: at most 4 pieces, at most 3 of
them NECTAR (a lane that took 4 NECTAR would take 5 POLLEN). Every legal load must fit (the longest, 3 NECTAR +
1 POLLEN, needs 12.26 in from the backstop to the axle) and every illegal one must not (the shortest, 5 POLLEN, needs
12.60; 4 NECTAR 12.67). The backstop sits mid-window, 12.43 in behind the axle (X -3.81), on ±0.2 in slots:
set it on the robot with real balls. No sensor, no software count.

## Numbers

| | |
|---|---|
| Lane | flat, ball-bottom at z 1.3, centreline Y 0. Eight shafts 0.8 in apart (X 5.3 to -0.3), each with 1 in of 24 mm compliant wheels to one side, alternating, so neighbours overlap and leave pockets; printed hub pulleys and one polycord belt. One Yellow Jacket along X outside the right wall drives the first shaft through a quarter-twist polycord loop. Walls (1/8 in polycarbonate) at |Y| 1.87, top z 2.6, under the front drive motors; a NECTAR clears their encoder caps by 0.06 |
| Ramp | X 8.0 z 0.05 to X 5.7 z 1.3 |
| Ceiling | flat, 1/16 in polycarbonate with 0.5 in soft foam (polyethylene or EVA, 2–3 lb/ft³), X 5.3 to -0.15, on four pins (shoulder screws) riding vertical slots in printed posts on the walls' top edges (X 5.35 and 1.35). Foam face 2.6 in over the lane: a POLLEN presses the foam 0.2; a NECTAR lifts it 0.82. Bands about 1–2 lbf preload, hooked on the posts |
| Feeder | one: two goBILDA 72 mm Gecko wheels (softest durometer) on a fixed shaft along X at Y 2.87, z 3.25, over X -2.99..-1.10 (the flywheels' span), in bearings in the launcher's front and rear channels. Its tread's inner edge at Y 1.45: a POLLEN presses 0.10 into it, a NECTAR 0.15 |
| Pad | opposite the feeder: 1/8 in aluminium with 0.5 in soft foam, hinged along X at its foot (Y -1.75, z 1.45), banded inward onto a stop (±0.1 in slots). The foam face rests at Y -1.05: a POLLEN presses it 0.2 and centres at Y 0.15; a NECTAR swings it back 0.77 and centres at −0.21. The band's light preload lets both sizes enter the stopped feeder under the lane's push |
| Feeder drive | one Yellow Jacket (1150 RPM) outboard on the left (Y 6.7, z 5.3), under the moved flywheel motor, one HTD5 belt straight to the feeder shaft: fixed centres, no tensioner |
| Backstop | X -3.81, ±0.2 in slots (see the count, above); a short floor between the feeder and the pad at the lane's height |

## Building and servicing it

| Module | Comes off with | What's in it |
|---|---|---|
| Ceiling | unhook four bands, lift | plate, foam, four shoulder-screw pins |
| Feeder | belt off, two bearing-block screws a channel | wheels, shaft, two bearings, pulley |
| Pad | two hinge-block screws | plate, foam, hinge rod, band |
| Feeder motor | two bracket screws, belt off | motor, pulley, bracket |
| Lane | polycord off, two wall-bracket screws a side | walls, eight shafts, wheels, hub pulleys |

Two motors in the transfer (lane, feeder), plus the launcher's two flywheel motors, moved. Off the shelf: goBILDA motors,
REX shafts, bearings, Gecko wheels, HTD5 pulleys and belts. To choose: the 24 mm compliant wheels. Made: polycarbonate
and aluminium plates, printed posts, blocks, pulleys and brackets, polycord, foam.

## Changes it needs in the mentor's CAD

`cad/full-robot/build.py` and the "launcher" group in `build.py` apply these; the mentor decides how to make them.

- **The launcher and turret move forward 0.8 in** (the lane's length sets the piece limit). Checked: the
  moved launcher touches nothing on the chassis or our front.
- **The flywheel motors move out and up**, along X at Y ±6.7, z 7.0, on brackets on the launcher frame's side
  channels, belted to his 41T pulleys. Their old place under the flywheels (with the 7x11 plates, 1-hole channels,
  mini quad blocks, dual blocks, 16T pulleys, belts and the standoffs and spacers that held them) is where the feeder
  and the pad go.
- **The launcher's left front 3-hole channel becomes 5-hole**, reaching down to carry the feeder shaft's front bearing.
- The launcher's front cross-channel, its two dual blocks and the two U-beams under it go (the lane's balls run where
  they are).
- The old intake's 11-hole channel goes up 30 mm.
- His pinwheel, its servo and his two star wheels go (our extractor and this lane replace them).
- His right flywheel already has a pivot shaft under it (at about Y −3.2, z 4.8): likely the start of his sprung
  flywheel arm. It's kept and clear.

## Checked

`tools/robot-cad/transfer2_check.py`, against the mentor's Robot.step (7 Oct, lined up by the rails, with the edits
above) and our front, by exact mesh intersection: nothing in the transfer or the launcher changes touches the robot or
the front; a NECTAR and a POLLEN rolled along the lane into the feeder and driven up the column touch nothing but the
wheels, the ceiling, the feeder, the pad and the flywheels; and the pad swung back for a NECTAR touches nothing.

## Open

- **The flywheels don't touch a POLLEN** (3.19 in gap; a POLLEN is 2.80). The mentor's sprung flywheel arm is the fix.
- The feeder grips a ball up to centre z 3.99 (POLLEN) / 4.22 (NECTAR); the flywheels take a
  NECTAR from 5.77, so the ball covers the last 1.5 in on its speed. One driven side gives less grip than two.
- 24 mm compliant wheels: part number to choose.
