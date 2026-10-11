# Layout decisions

One line per decision: what was chosen, why, and the test that would reopen it. A decision here is settled; don't
reopen it without its test failing or new information that answers "reopen if". Newest at the bottom of each section.
`build.py`'s checks and `budget.py`'s view settle "does it fit?"; this file holds what only people decide.

## Fixed (not options)

| What | Value | From |
|---|---|---|
| Start outline | 17.75 in over everything (R102 18, 1/8 in a side for print error and soft faces) | CAD chat, 10 Oct |
| After START | at most 18 x 24 x 29 in (R105) | game manual |
| Pieces held | at most 4 (G407): roller axle to backstop 12.26 to 12.60, backstop on ±0.2 in slots | Auto strategy, cad/transfer |
| Flaps | fixed, 3.4 in ahead of the face; their faces at restitution 0.1 or lower | simulator, 10 Oct |
| Motors | 8: four drive, intake (it also drives the lane), two flywheels, feeder. None spare: anything new is a servo | rules, 8 Oct motor budget |
| Field frame | Pedro's: inches, +X forward, +Y left, +Z up | CLAUDE.md |
| Printed parts | within 180 mm each way | home printers |
| Every part | shows what it bolts to, through real holes (goBILDA's 8 mm grid, named inserts) | the mentor, via the CAD chat |

## Decided

| Date | Decision | Why | Reopen if |
|---|---|---|---|
| 10 Oct | Ladder frame of goBILDA 1121 low-side rails, printed modules on it | stiff and stock where it matters, printed where it's quick to change | — |
| 10 Oct | goBILDA for drive pods, flywheel holders, motor mounts, turret mount | proven, nothing to wait on | — |
| 10 Oct | The mentor's star wheels (SWYFT 3.5 in, cut to 16 flaps, servo-driven through one-way clutches), not vector wheels | his choice, proven on his robot | the star-wheel flex test fails once mounted |
| 10 Oct | goBILDA 3208-0004-0001 turret kit, servo-driven, two encoders | stock ring, known, covers the routes' 1080° | — |
| 10 Oct | Flywheels fixed under the turret; the turret turns only the hood | wires never cross the ring | — |
| 10 Oct | Flap faces swappable (three countersunk M3) | the drop test can come later, on the full robot | — |
| 11 Oct | The feeder is never driven from a flywheel; it has its own motor | the user and the 8 Oct motor budget: an independent feeder | — |
| 11 Oct | Feeder: a pair of 48 mm wheels under the flywheels, parallel to them; the right one free, in the pad's place | the mentor: one wheel leaves a 0.25 in stretch undriven before the flywheels | the handoff check fails, or the rig shows a pair jams |
| 11 Oct | ~~One spring a side for both pinches~~ → **four springs**: each flywheel side and each feeder wheel opens on its own | the user: a NECTAR waiting in the feeder would hold the flywheels open under a POLLEN being shot | — |
| 11 Oct | The feeder stays put; stopping its motor is the gate (no gate servo) | the motor can sit anywhere a belt reaches | — |
| 11 Oct | Each feeder wheel on its own arm, pivoting on a shaft 1.87 in under it; on the left that shaft is also the drive: the motor, fixed to the frame, belts to it (belt A, behind the back plates) and a belt up the arm (belt B) turns the wheel; both 215 mm | both belts keep their length as the arm swings, so the motor never moves; the wheel moves 0.41 in out and 0.05 in down | — |
| 11 Oct | Layout v3: re-plan top-down around a space budget; the mentor's built robot is the reference for each mechanism's job and key dimensions, not its parts | patching his packaging left no home for the feeder motor | the mentor prefers his packaging (then v2 stands) |
| 11 Oct | Our own corners on his track and wheelbase, mirrored: front motors straight over their axles, five holes up; rear motors a hole behind and three up | frees the space ahead of the launcher; identical left and right for spares, two robots and Pedro's tuning | — |
| 11 Oct | A front cross channel ties the rails; the Limelight's mast stands on it, centred | closes "front of frame"; a centred camera | — |
| 11 Oct | The battery stands against the right rail, ahead of the rear cross channel | the feeder's drive is behind the module on the left; 0.6 kg 1.95 in right about balances the feeder's motor 3.6 in left | the mentor's battery is a different size |
| 11 Oct | The turret's drive gear points forward (as his); its servo swings 30° and encoder B 270° about the gear's axis | searched every 15°, each alone, against the front drive motors, the flywheels open and shut, and the launch column: anywhere else one of them hits; belt and mesh unchanged | — |
| 11 Oct | His launcher frame re-housed: the flywheels' pivot hardware dropped (the sprung slides replace it); the drive plate redrawn around what it carries; the rear lower cross channel, its blocks and the column's back guides raised 0.9 in above the feeder's drive; the side channels end at X −4.98, clear of our rear belts; the feeder bridge trimmed clear of the battery and the feeder's drive | each the smallest change that clears the budget; the feeder pair holds the ball where the back guides no longer reach | the rig shows the ball needs a back guide below z 4.96 |
| 11 Oct | Rear drive motors at a 275 mm belt's own centres, 0.06 in forward (0.45 mm of tension in slotted holes) | inside the 17.75 in outline | — |
| 11 Oct | The user: no more waiting on the mentor. This design competes with his, with carte blanche to change anything, judged on scoring the sister tip-5 Auto (or beating it), speed of build and serviceability | his packaging can't fit everything | — |
| 11 Oct | The flywheel sides: each shaft in two printed bearing blocks sliding 0.41 in in slots in the plates; the rear block one piece with a yoke over to the motor, so the motor rides along and the belt keeps its length; an extension spring and a stop screw each | the simplest real mechanism with the motion the checks test; no rails | the rig shows the blocks bind (then PTFE tape, or a parallelogram) |
| 11 Oct | The feeder arms: a compression spring each from a lug on the rear block; a tab on the arm rests on the block's foot, an M3 set screw sets the rest gap | anywhere inboard of the arm is the ball's path or belt B | — |
| 11 Oct | A printed launcher tray across the rails' bottom flanges replaces his feeder bridge; the feeder's blocks bolt to it | the bridge couldn't hold the new blocks | — |
| 11 Oct | **The lane is passive**: its rollers free on bearings; the intake roller throws each piece back, and the queue pushes the last one in | a drive from the intake crosses the star wheels on both sides, or stretches its belt 23% as the roller floats; no motor is spare | the rig: four pieces fed by the roller don't all reach the backstop on the flat lane (then a drive, or a 2° slope) |
| 11 Oct | The star's clutch hub screws to the servo's goBILDA horn; the round shaft carries the star through a printed hex adapter | no room between the hub and the servo for a separate coupler | — |
| 11 Oct | The extractor: a 1:1 printed mod-1 gear pair, inboard of the right flap, from the servo's horn to the right stub | — | — |
| 11 Oct | **The whole front is soft and swappable**: TPU (or foam) pads on the flap roots' front faces and a TPU skin on the ramp, as well as the flaps' faces | the simulator (11 Oct): the spill bounces off the body's front and the mouth, not the flaps; 0.4 to 0.1 there is worth 6 to 9 points; with it and the fast intake, v3 scores Sister 94.0 and Sister five 96.7 (27 fifth TIPs of 60), against the mentor's robot's 88.0 and 81.2 | Monday's drop test: no material we can fit reaches 0.1 |
| 11 Oct | The intake rate is the second lever (3 to 5 points; 13 to 27 fifth TIPs): the roller stays full-width at 1620 RPM | the simulator | the intake bench test misses a piece every 0.17 s |

## Open

1. Software: the Autos' start poses are drawn for the mentor's longer body; redraw them for v3 (14.35 in) so it backs onto the wall.
2. Rig values: the four springs' rates and preloads, the rest gaps, the feeder motor's speed, the flap faces' material.
3. Confirm on real parts: the goBILDA channel hole rows, the battery's and hubs' sizes, the servos' spline positions.
4. Holes to mark from his real parts: the launcher's feet, the hood on the turret, the flywheel springs' anchors.
