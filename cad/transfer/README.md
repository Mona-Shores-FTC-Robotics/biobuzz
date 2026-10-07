# The transfer: intake roller to the launcher (v3, 7 Oct 2026)

> **This reworks the mentor's launcher**, not just adds to it: the whole launcher and turret move forward
> 0.8 in, his flywheel motors move out and up, the plates, blocks and standoffs that held them under the
> flywheels go, and so do his launcher's front cross-channel and front U-beams. Show him this before anything is
> ordered. The list is under "Changes it needs in the mentor's CAD".

The lane from our intake roller into the mentor's flywheels, drawn in the robot CAD's frame so `dhs-transfer.step` lands
in place in Onshape beside `cad/intake-b/dhs-intake-b.step`. `build.py` holds every number. Frame: +X forward, +Y left,
+Z up, inches, origin on the floor under the chassis centre (7.56 in behind the front face). Earlier versions (the
J-wheel, then a two-feeder cup) are in git history; the cup was dropped because a ball resting on top of two wheels is
held on only by its weight, so the feeders could flick it but not drive it.

**How it works:** the intake roller pushes each ball up the ramp onto a flat lane of driven compliant wheels (the
mentor's offset-wheel design, scaled to the lane's height) under a flat sprung ceiling that presses it onto them. The
lane pushes it in between two feeders under the flywheels, which grip it by its sides; it stops against a backstop that
sets it on the launch column (X -2.045, under the turret's axis). Stopped, the feeders hold it below the
flywheels' reach, and the rest queue nose to tail behind it. Feeding runs the feeders: they drive the ball, gripped,
straight up into the flywheels. One feed, one ball.

**The count is the lane's length.** Balls queue nose to tail from the backstop to the intake roller's axle (X
8.56), and a ball is held once its centre is behind it. The team's rule: at most 4 pieces, at most 3 of
them NECTAR (a lane that took 4 NECTAR would take 5 POLLEN). Every legal load must fit (the longest, 3 NECTAR +
1 POLLEN, needs 12.26 in from the backstop to the axle) and every illegal one must not (the shortest, 5 POLLEN, needs
12.60; 4 NECTAR 12.67). The backstop sits mid-window, 12.43 in behind the axle (X -3.87), on ±0.2 in slots:
set it on the robot with real balls. No sensor, no software count.

## Numbers

| | |
|---|---|
| Lane | flat, ball-bottom at z 1.3, centreline Y 0. Eight shafts 0.8 in apart (X 5.3 to -0.3), each with 1 in of 24 mm compliant wheels to one side, alternating, so neighbours overlap and leave pockets; printed hub pulleys and one belt, from one Yellow Jacket outside the right wall. Walls (1/8 in polycarbonate) at |Y| 1.87, top z 2.6, under the front drive motors; a NECTAR clears their encoder caps by 0.06 |
| Ramp | X 8.0 z 0.05 to X 5.7 z 1.3 |
| Ceiling | flat, 1/16 in polycarbonate with 0.5 in soft foam (polyethylene or EVA, 2–3 lb/ft³), X 5.3 to -0.15, on four parallel links (1.2 in; posts at X 6.05 and 2.2). Foam face 2.6 in over the lane: a POLLEN presses the foam 0.2; a NECTAR lifts it 0.82. Bands about 1–2 lbf preload |
| Feeders | two goBILDA 72 mm Gecko wheels a side (softest durometer), over X -2.99..-1.10 (the flywheels' span), on sprung arms: each feeder and its shaft hang from a front and a rear arm (16 mm) on a pivot straight above its axis. A hard stop (±0.1 in slots) sets the POLLEN squeeze, 0.10 a side; a band of about 1 lbf preload holds it there; a NECTAR swings both out 0.41 in (41°), so both sizes enter the stopped feeders under the lane's push and the ball stays on the column. Axles at rest along X at Y ±2.72, z 3.10; tops 0.26 under the flywheels. Gripped from where it enters up to centre z 3.85 (POLLEN) / 4.84 (NECTAR); the flywheels take a NECTAR from 5.77 |
| Feeder drive | one Yellow Jacket (1150 RPM) per feeder, outboard under the moved flywheel motor (Y ±6.7, z 5.3), belted (HTD5) to a jackshaft on the front arm's pivot; a 20T–20T mod 0.8 gear pair on the arm turns the feeder, so the swing never changes the belt. Run both at the same speed |
| Backstop | X -3.87, ±0.2 in slots (see the count, above); a short floor between the feeders at the lane's height |

## Changes it needs in the mentor's CAD

`cad/full-robot/build.py` and the "launcher" group in `build.py` apply these; the mentor decides how to make them.

- **The launcher and turret move forward 0.8 in** (the lane's length sets the piece limit). Checked: the
  moved launcher touches nothing on the chassis or our front.
- **The flywheel motors move out and up**, along X at Y ±6.7, z 7.0, on brackets on the launcher frame's side
  channels, belted to his 41T pulleys. Their old place under the flywheels (with the 7x11 plates, 1-hole channels,
  mini quad blocks, dual blocks, 16T pulleys, belts and the standoffs and spacers that held them) is where the feeders go.
- **The launcher's front 3-hole channels become 5-hole**, reaching down, slotted for the feeder shafts' swing; they and
  the rear channels carry the feeder arms' pivots.
- The launcher's front cross-channel, its two dual blocks and the two U-beams under it go (the lane's balls run where
  they are).
- The old intake's 11-hole channel goes up 30 mm.
- His pinwheel, its servo and his two star wheels go (our extractor and this lane replace them).
- His right flywheel already has a pivot shaft under it (at about Y −3.2, z 4.8): likely the start of his sprung
  flywheel arm. It's kept and clear of the feeders.

## Checked

`tools/robot-cad/transfer2_check.py`, against the mentor's Robot.step (7 Oct, lined up by the rails, with the edits
above) and our front, by exact mesh intersection: nothing in the transfer or the launcher changes touches the robot or
the front; a NECTAR and a POLLEN rolled along the lane into the feeders and driven up the column touch nothing but the
wheels, the ceiling, the feeders and the flywheels; and the feeders and their arms swung out for a NECTAR touch nothing.

## Open

- **The flywheels don't touch a POLLEN** (3.19 in gap; a POLLEN is 2.80). The mentor's sprung flywheel arm is the fix.
- 24 mm compliant wheels and the mod 0.8 gears: part numbers to choose (goBILDA if they make them).
