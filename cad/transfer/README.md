# The transfer: intake roller to the launcher (v4, 8 Oct 2026)

> **This reworks the mentor's launcher**, not just adds to it: the whole launcher and turret move forward 0.8 in, his
> flywheel motors move out and up, and the plates, blocks and standoffs that held them under the flywheels go, as do
> his launcher's front cross-channel and front U-beams. Show him this before anything is ordered. The list is under
> "Changes it needs in the mentor's CAD".

The lane from our intake roller into the mentor's flywheels, drawn in the robot CAD's frame so `dhs-transfer.step` lands
in place in Onshape beside `cad/intake-b/dhs-intake-b.step`. `build.py` holds every number. Frame: +X forward, +Y left,
+Z up, inches, origin on the floor under the chassis centre (7.56 in behind the front face). Earlier versions are in git
history.

**Build and service come first.** Where a feature cost a lot of parts or a hard pit repair, it went: one driven feeder
against a sprung pad instead of two feeders on swing arms with gears; a ceiling on pins in slots instead of links.

**How it works:**
1. The intake roller pushes each ball up the ramp onto a flat lane of five shafts of small rollers (the mentor's
   offset-roller design, scaled to the lane's height), under a flat sprung ceiling that presses it onto them.
2. The lane pushes it in between the feeder (left, driven) and a sprung foam pad (right), under the flywheels, until it
   stops against a backstop that sets it on the launch column (X -2.045, under the turret's axis).
3. Stopped, the feeder and pad hold it below the flywheels' reach, and the rest queue nose to tail behind it.
4. Feeding runs the feeder: it drives the ball, pinched against the pad, straight up into the flywheels. One feed, one
   ball.

**The count is the lane's length.** Balls queue nose to tail from the backstop to the intake roller's axle (X 8.62),
and a ball counts as held once its centre is behind that axle. The team's rule is at most 4 pieces, at most 3 of them
NECTAR (a lane that took 4 NECTAR would take 5 POLLEN):
- every legal load must fit: the longest, 3 NECTAR + 1 POLLEN, needs 12.26 in from the backstop to the axle;
- every illegal one must not: the shortest, 5 POLLEN, needs 12.60 in; 4 NECTAR needs 12.67 in.

The backstop sits mid-window, 12.43 in behind the axle (X -3.81), on ±0.2 in slots: set it on the robot with real
balls. No sensor, no software count.

![The transfer from the left](views/side.png)
![The transfer from above](views/top.png)

(`views/` is drawn by `tools/robot-cad/transfer_views.py` from the build's meshes; rerun it after a change.)

## What changed in v4, and why

| v3 | v4 | Why |
|---|---|---|
| A motor drove the lane, and another the feeder | **Two goBILDA Super Speed servos** (2000-0025-0004) in continuous rotation | FTC allows 8 DC motors. Drive (4), intake roller, flywheels (2) and two transfer motors made 9. With servos the robot has 7, which leaves one for the turret. Neither motor fitted anywhere without clashing, either. |
| 8 lane shafts, 0.8 in apart | **5 shafts, 1.4 in apart** | Fewer parts, same support: a ball rides 0.14 in lower between shafts at most. |
| 1/8 in polycarbonate walls on made brackets | **1/4 in polycarbonate walls on goBILDA REX standoffs** (1516-4008-0800) to the drive rails' own holes | No made brackets. The walls come off from inside the lane, with four flat-head screws each. |
| Feeder shaft in bearings in the mentor's channels | **Two small 1/4 in aluminium bearing plates** bolted to the channels' existing holes | His launcher channels lean 5.4°, so their bearing holes miss the feeder's axis. v3 needed holes drilled in goBILDA parts. |
| A 5-hole channel replacing his front 3-hole | **His 3-hole stays**: the feeder shaft passes under it | One less change to his launcher. |
| Belts drawn as two strips, lengths unchecked | **Every belt is a goBILDA length on fixed centres**, drawn wrapped | No tensioners: the centres are set for the belt. |
| No screws | **Every screw, nut and standoff drawn and checked** | |
| Ramp on brackets that didn't hold it; floor hangers that didn't reach anything | **The ramp slides into slots in the walls; the floor sits on a printed bridge bolted behind the launcher's rear channels** | |

## Numbers

| | |
|---|---|
| Lane | Flat, ball-bottom at z 1.3, centreline Y 0. **Shafts:** five, 1.4 in apart, X 5.3 to -0.3, each carrying one 24 mm × 1 in printed TPU roller on one side, alternating. **Bearings:** goBILDA 1611 flanged, pressed into both walls. **Drive:** the shafts chain in pairs on 3/16 in polycord loops (one printed three-groove pulley per shaft); the lane servo drives shaft 3 (X 1.1). **Walls:** 1/4 in polycarbonate, inner faces at \|Y\| 1.86, z 0.3 to 2.6, X -0.8 to 7.4, under the front drive motors; a NECTAR clears their encoder caps by 0.05. |
| Lane servo | A goBILDA 2000-0025-0004 Super Speed in continuous rotation, powered at 6 V from a REV Servo Power Module (230 RPM). Right side, in the gap between the right wall and the rail: its tabs sit on two 43 mm standoffs from the rail's web (tab face at Y -3.57), its spline points at the lane (X 1.81, z 2.42). A printed pulley on the spline, 40 mm pitch diameter, drives shaft 3's 16 mm groove by polycord: 2.5:1 up, 575 RPM at the lane shafts. **Lane speed:** 575 RPM × π × 24 mm rollers = 28.4 in/s of roller tread, so about 14.2 in/s of ball (the ball rolls under the still ceiling at half the tread's speed). |
| Ramp | X 8.0 z 0.05 to X 5.7 z 1.3, 1/16 in polycarbonate. Its edges sit 0.1 in deep in slots routed in the walls' inner faces. |
| Ceiling | Flat, 1/16 in polycarbonate with 0.5 in soft foam (polyethylene or EVA, 2–3 lb/ft³), X 5.3 to -0.15. **Pins:** a printed pin block screwed up into the ceiling's edge at each corner (M3, from below; the foam is cut away round the heads) reaches out over the wall; an M3 shoulder screw (4 mm shoulder × 10 mm) through the slot of a printed post on the wall's outer face threads into a heat-set insert in the block's end. Two posts a side, X 5.35 and 2.5, each held by a flat-head screw from inside the lane; the slots are 0.85 in long. **Clearance:** the foam face is 2.6 in over the lane, so a POLLEN presses the foam 0.2 and a NECTAR lifts it 0.82. **Bands:** about 1–2 lbf preload, hooked on the posts. |
| Feeder | One: two goBILDA 72 mm Gecko wheels (softest durometer) on a goBILDA 120 mm REX shaft along X at Y 2.87, z 3.25, over X -2.99 to -1.10 (the flywheels' span). Its tread's inner edge is at Y 1.45: a POLLEN presses 0.10 into it, a NECTAR 0.15. |
| Feeder drive | The feeder servo sits 47.5 mm above the feeder shaft, 3° inboard, ahead of the launcher. A goBILDA 1910 servo hub on its spline carries a 3411 24T hub-mount pulley, belted (215 mm) to a 3417 24T on the feeder shaft. Its tabs sit on two 34 mm standoffs from the front bearing plate. |
| Pad | Opposite the feeder: 1/8 in aluminium with 0.5 in soft foam, hinged along X at its foot (Y -1.90, z 1.62). **Hinge:** two printed knuckles with REX bores, bolted to the plate's foot, on a goBILDA 2106-4008-0640 steel REX shaft (64 mm) that turns in round holes in two printed blocks on the feeder floor, held by its e-clips outside the blocks. **Stop:** a printed block outboard of the front knuckle, below the hinge, screwed from under the floor through a ±0.1 in slot (the slot sets the POLLEN squeeze); the band pulls the pad's top in, so the knuckle's foot swings out onto it. **At rest:** the foam face is at Y -1.05, so a POLLEN presses it 0.2 and centres at Y 0.15. **For a NECTAR:** it swings back 0.77 at the ball's centre height (z about 3.0), about 29° (atan2(0.77, 3.0 - 1.62) = 29.2°), and the NECTAR centres at Y -0.21. |
| Floor and backstop | **Floor:** 1/8 in polycarbonate between the feeder and the pad, at the lane's height, resting on the shelf of a printed bridge bolted behind the launcher's two rear channels. **Backstop:** printed, an L, its foot screwed through the floor into the shelf in ±0.2 in slots. |
| Flywheel motors | Moved out and up, along X at (Y 6.92, z 7.02) left and (Y -6.71, z 7.03) right, faces at X -2.65 pointing back. **Brackets:** 1/8 in aluminium, bent, on the launcher's side channels' top flanges. **Belts:** from their 16T pulleys to his 41T: goBILDA 315 mm (left) and 320 mm (right). |

## Parts

**goBILDA** (the build counts them; the fasteners are `tools/robot-cad/fastener_list.py`'s):

| Part | Count | What |
|---|---|---|
| 2000-0025-0004 | 2 | Super Speed servo: the lane and the feeder, set to continuous rotation with the 3102 programmer |
| 1516-4008-0800 | 8 | 80 mm REX standoff: the walls to the rails |
| 1501-0006-0430 | 2 | 43 mm M4 standoff: the lane servo |
| 1501-0006-0340 | 2 | 34 mm M4 standoff: the feeder servo |
| 1611-0514-4008 | 12 | flanged bearing: five lane shafts × 2, the feeder × 2 |
| 2106-4008-1440 | 5 | 144 mm REX shaft (e-clips): the lane |
| 2106-4008-1200 | 1 | 120 mm REX shaft (e-clips): the feeder |
| 2106-4008-0640 | 1 | 64 mm REX shaft (e-clips): the pad's hinge |
| 2910-1020-4008 | 1 | 8mm REX clamping collar: the feeder shaft, against its front bearing |
| 3632-0014-0072 | 2 | 72 mm Gecko wheel, softest durometer: the feeder |
| 1910-0025-0816 | 1 | servo hub: the feeder servo |
| 3411-0014-0024 | 1 | 24T HTD5 hub-mount pulley: the feeder servo |
| 3417-4008-0024 | 1 | 24T HTD5 pulley: the feeder |
| 3412-0009-0215 | 1 | HTD5 belt, 215 mm: the feeder |
| 3417-4008-0016 | 2 | 16T HTD5 pulley: the moved flywheel motors |
| 3412-0009-0315, -0320 | 1 each | HTD5 belts: the flywheels |
| 8mm REX spacers | stacks | the lane shafts' outer ends, the feeder shaft |

**Other bought parts:**
- 3/16 in polycord: 5 welded loops, each cut 8% short;
- foam, for the ceiling and the pad;
- four M3 shoulder screws (4 mm shoulder × 10 mm), for the ceiling's pins;
- a REV Servo Power Module, to run the lane servo at 6 V;
- M4 heat-set inserts, for the printed parts, and M3 ones for the ceiling's pin blocks;
- bands.

**Made:**
- **Cut:** the two walls (1/4 in polycarbonate, with routed slots and countersinks); the ramp, ceiling and floor
  (polycarbonate); the two feeder bearing plates (1/4 in aluminium, the rear one tapped M4); the pad plate (1/8 in
  aluminium); the two flywheel motor brackets (1/8 in aluminium, bent).
- **Printed** (STLs in `stl/`): the five TPU rollers, the lane spacers and pulleys, the servo pulley, the ceiling
  posts and pin blocks, the pad's knuckles, hinge blocks and stop, the feeder bridge and the backstop.

## Building and servicing it

| Module | Comes off with | What's in it |
|---|---|---|
| Ceiling | Unhook four bands, lift | plate, foam, four shoulder-screw pins |
| A lane wall | Four flat heads from inside the lane (ceiling off) | wall, its five bearings, the ramp (slides out) |
| Lane shafts | A wall off, polycord off | shaft, roller, spacers, pulley, e-clips |
| Lane servo | The right wall off, two tab screws | servo, pulley |
| Feeder | Belt off; the front plate's two nuts; the rear plate's two screws from behind the rear channel | wheels, shaft, both plates and bearings, pulley |
| Feeder servo | Two tab screws | servo, hub, pulley |
| Pad | Two hinge-block screws from under the floor | plate, foam, knuckles, hinge rod, blocks, band (the stop stays on the floor) |

The standoffs stay on the rails. They go on during the chassis build, before the outer wheel plates and the pods,
because their screws go in from outside the rails. The fastener check's 28 "service order" notes are these and the
other orders in the table above, and every other screw has a key path.

## Changes it needs in the mentor's CAD

`cad/full-robot/build.py` and the "launcher" group in `build.py` apply these; the mentor decides how to make them.

- **The launcher and turret move forward 0.8 in.** The lane's length sets the piece limit. Checked: the moved launcher
  touches nothing on the chassis or our front.
- **The flywheel motors move out and up**, along X beside the launcher frame, on brackets on its side channels, belted
  to his 41T pulleys (above). They used to sit under the flywheels, with the 7x11 plates, 1-hole channels, mini quad
  blocks, dual blocks, 16T pulleys, belts and the standoffs and spacers that held them. The feeder and the pad go there
  now.
- The launcher's front cross-channel, its two dual blocks and the two U-beams under it go (the lane's balls run where
  they are).
- The old intake's 11-hole channel goes up 30 mm.
- His pinwheel, its servo and his two star wheels go (our extractor and this lane replace them).
- His right flywheel already has a pivot shaft under it (at about Y −3.2, z 4.8). It's likely the start of his sprung
  flywheel arm. It's kept and clear.

## Software

The lane and the feeder are servos in continuous-rotation mode, so `TeamCode` drives them as `CRServo`s. Both must be
switched to continuous mode with goBILDA's 3102 programmer before they go in. The robot then has seven DC motors.

## Checked

`tools/robot-cad/transfer2_check.py` runs against the mentor's Robot.step (7 Oct, lined up by the rails, with the
edits above) and our front, by exact mesh intersection:
- **Clashes:** nothing in the transfer or the launcher changes touches the robot, the front or each other, except
  parts meant to touch (shafts in bearings, belts on pulleys, plates on what they bolt to).
- **Balls:** a NECTAR and a POLLEN rolled along the lane into the feeder and driven up the column touch nothing but
  the rollers, the ceiling, the feeder, the pad and the flywheels.
- **Pad:** swung back for a NECTAR, it touches nothing.

`tools/robot-cad/front2_check.py` sweeps the front's roller (rising) and extractor (every 10°) against all of it:
clear.

`tools/robot-cad/fastener_check.py` covers every screw in the front, the transfer and the Limelight mount, 132 in
all. For each it checks that the shank passes only through holes, the head and nut clear everything, a key reaches the
head, and a tapped hole gives enough thread. Result: 0 problems; 28 screws need another part off first (service orders), as listed
above.

## Open

- **The flywheels don't touch a POLLEN** (3.19 in gap; a POLLEN is 2.80). The mentor's sprung flywheel arm is the fix.
- **The feeder can't throw the ball into the flywheels (the mentor's decision).** The feeder grips a ball up to centre
  z 3.99 (POLLEN) and 4.22 (NECTAR); the flywheels take a NECTAR from 5.77. The ball has to coast the last ~1.7 in on
  the speed the feeder gives it, and a servo can't give it that much. Two ways out:
  - **(a) close the hand-off gap**, so the feeder hands the ball straight into the flywheels with no coast. Then a servo
    works, and the drawn servo and belt stay.
  - **(b) a DC motor on the feeder**, once the mentor moves one of his launcher channels to make room for it. The robot
    has 7 of the 8 DC motors FTC allows, so this uses the last one.
- **The lane servo is limited by torque, not speed.** Each ball under the foam ceiling takes about 1 lbf to drive
  (friction about 0.6 × roughly 1.5 lbf of foam preload, plus the ball's weight). At the 12 mm roller radius that is
  about 0.5 kg·cm per ball, so four queued balls need about 2 kg·cm at the lane shafts. The servo stalls at 4.7 kg·cm at
  6 V, which is about 1.9 kg·cm at the shafts after the 2.5:1 step-up. A full lane will crawl or stall.
  - Mitigation: keep the foam preload light (about 1 lbf), and keep the TPU rollers slippery enough to slip under a
    stalled queue. Even then it is marginal.
  - The lane servo is a stopgap. Three options for the team:
    - **(a) gear the mentor's two flywheels together** and drive both from one motor, with a gear pair between the
      shafts. This frees a DC motor for the lane and the feeder. It also locks the two wheels to the same speed, so the
      shot gets no sidespin. This is the preferred option.
    - **(b) drive the lane from the roller shaft.** This needs a sprung idler, because the roller floats 1.3 in, which
      stretches the cord about 18%.
    - **(c) close the hand-off gap** for the feeder (as above).
- **The TPU rollers:** print one and try it on a ball before printing five; durometer and wall count matter.
- **The rail holes:** the mentor's rails sit 0.4 mm out of level along their length in his CAD. Check the standoff holes
  line up on the robot.
