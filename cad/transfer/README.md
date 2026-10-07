# The transfer: intake roller to the launcher (v2, 7 Oct 2026)

The lane from our intake roller to the mentor's flywheels, drawn in the robot CAD's frame so `dhs-transfer.step` lands
in place in Onshape beside `cad/intake-b/dhs-intake-b.step`. `build.py` holds every number. Frame: +X forward, +Y left,
+Z up, inches, origin on the floor under the chassis centre (7.56 in behind the front face). It replaces the J-wheel
design (concept A), which is in git history.

The layout is the mentor's: a wheel lane under a ceiling, and two feeder wheels across the robot, one ahead of and one
behind the ball, under the flywheels (his FeederConcept.step and screenshots). The numbers are ours, fitted to his
current Robot.step.

**How it works:** the intake roller pushes each ball up the ramp (22°) to the lane. Two driven wheel shafts carry it
up the lane (17°) under a sprung, foam-lined ceiling to the front feeder. It rolls over the stopped
front feeder and drops into the cup, sitting on both feeders under the flywheels (a NECTAR's centre at z 3.67,
a POLLEN's at 3.00; the flywheels start touching a NECTAR at 5.77). Feeding spins both feeders up into the cup: the
ball pops straight up into the flywheels. The front feeder's top moves forward while it does, so it holds the next ball
back; when the feeders stop, the lane rolls the next ball in. One feed, one ball.

## Numbers

| | |
|---|---|
| Lane centreline | Y 0. The front drive motors' encoder caps leave a NECTAR 0.06 in per side; the walls (1/8 in polycarbonate, inner faces at |Y| 1.87) stay under them (top at z 2.6) |
| Ramp | X 8.0 z 0.05 to X 5.8 z 0.9, then a static lip to the first lane wheel |
| Lane wheels | X 3.74 (32 mm compliant, two per shaft) and X 2.03 (48 mm Gecko, two per shaft), tops on the ball-bottom line; belted from one Yellow Jacket outside the right wall (miter gears) |
| Ceiling | 1/16 in polycarbonate with 0.5 in soft foam (polyethylene or EVA, 2–3 lb/ft³), from X 6.5 (over the ramp's top) to 0.95, on four parallel links (1.2 in, from posts on the walls' top edges at X 6.3 and 2.2, clear of the encoder caps) so it lifts evenly everywhere. Foam face 2.6 over the ball-bottom line at rest: a POLLEN presses the foam 0.2 and not the ball (the balls take under 0.25 in at 43 lbf). A NECTAR lifts it 0.82, anywhere along it, including at the ramp's top; the links reach level at 0.85. Bands: about 1–2 lbf preload, 2 lbf/in |
| Ball count | The lane is 11.4 in from the cup to the roller's grip and takes 5 POLLEN or 4 NECTAR, so the G407 limit is kept by count, not by geometry: an IR break-beam across the lane at X 5.55, z 2.4 (both sizes cross it) counts balls in, and the code stops the intake at the limit. It also tells "lane full" and "empty" |
| Feeders | the mentor's FeederWheel (66.7 mm foam, 48 mm wide), shafts across Y at X -0.545 and -5.145, z 1.56 (bottoms 0.25 off the tiles); the front one in the lane walls, the rear one in two plates bracketed to the rails |
| Feeder drive | one Yellow Jacket along X under the left flywheel motor, miter gears to a jackshaft into the lane, polycord to the front feeder and a crossed polycord to the rear one (they turn opposite ways). Drive both from the one motor so they turn at the same speed: a mismatch puts front/back spin on the ball that the flywheels can't remove |

## Changes it needs in the mentor's CAD

`cad/full-robot/build.py` applies these; the mentor decides how to make them for real.

- The old intake's 11-hole channel goes up 21 mm (a lane NECTAR's top is 5.14 under it).
- The launcher's front cross-channel (10-hole, X −0.63..−0.16, z 3.84..5.74) and its two dual blocks go: the balls
  run through where it is, rolling over the front feeder. The 8-hole channel above still ties the frame.
- The launcher's two front U-beams move 0.25 in further apart (a NECTAR rising over the front feeder grazes them).
- His pinwheel, its servo and his two 3.5 in star wheels go (our extractor and this lane replace them).

## Checked

`tools/robot-cad/transfer2_check.py` against the mentor's Robot.step (7 Oct, lined up by the rails) and our front, by
exact mesh intersection: no part of the transfer touches the robot or the front (the ramp brackets bolt to the side
plates; the wall brackets to the rails), and a NECTAR and a POLLEN swept along the lane and popped up from the cup touch
nothing but the lane, the ceiling and the feeders.

## Open

- **The flywheels don't touch a POLLEN.** Their gap is 3.19 in; a POLLEN is 2.80. The mentor's FeederConcept puts one
  flywheel on a sprung swing arm; the turret launcher needs the same.
- The ramp's static stretch (X 5.8 to the first wheel) is pushed only by the roller and the queue. If balls stall there,
  a third, smaller lane wheel goes in.
- 32 mm compliant wheel, miter gears and the break-beam: part numbers to choose.
