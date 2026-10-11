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
| 11 Oct | The rails' holes as goBILDA's STEP has them (the CAD chat): stations every 8 mm from 8 mm off the end; web holes at ±8 and ±16 mm, only ±16 at every 24 mm (a 14 mm bore on the centreline there); flange row 8.0 mm from the web's face. Every rail screw re-placed on it | read from the vendor's model, not the mentor's | — |
| 11 Oct | The hood screws to the 176T gear's 64 mm-radius holes (the 56 mm-radius free ones are the fallback) | the CAD chat, from goBILDA's STEP; the 56 mm ones sit under the hood's tube | the part doesn't have them |
| 11 Oct | Soft faces printed as a TPU 95A gyroid lattice, 10-20% infill, no top skin (the ramp's skin with one thin top skin); Monday drops solid TPU, two lattice infills and memory foam on a backer; no grommets | the body-designs chat: a face that crushes locally beats a stiff face on a soft mount, for pieces this light | the drop test |
| 11 Oct | **Bought parts are drawn as their makers' own models** (goBILDA's and REV's STEP files, `vendor.py`); the checks keep using envelopes, and a real part that reaches past its envelope is checked against what's there | a teaching model has to be the real robot; and the boxes hid four mistakes, below | — |
| 11 Oct | The star servos stand on a plate under their tabs (4 M4), their cases down through a window to the clutch hubs; no shelf over their backs | a goBILDA servo mounts only by its tabs | — |
| 11 Oct | The extractor servo stands straight over the right stub on its own PETG-CF mount astride the flap's tall top (2 M3 through a cheek into the flap); a 30-tooth module 1 pair, 1:1, on the servo's horn | the servo is 1.5 in deep: where it was, its case went through the flap | — |
| 11 Oct | The forward odometry pod screws by its plate's side face (two tapped holes, 32 mm apart) through its adapter at the rail's 96 mm; the strafe pod (43 mm wide, more than the 42 mm between the lane walls' standoffs) outside the right rail between the wheels, its back plate on a pocketed 5 mm spacer, at the rail's 168 mm | the pods' own holes, on the web's +-16 mm rows (only at stations that are multiples of 24 mm) | the Pinpoint's offsets: measured on the robot |
| 11 Oct | The 80 mm wall standoffs are two 1501-0006-0400 on an M4 set screw; the 72 mm rack standoffs a 0480 and a 0240 | goBILDA's 1501 standoffs stop at 56 mm | — |
| 11 Oct | The hubs are REV's 29.6 mm thick (not 1 in); the rack's back wall is slotted over its standoffs' screws | the rack screws and the flywheel springs' anchors leave no room to move the hubs | — |
| 11 Oct | The Limelight drawn at its size (72 x 48 x 17 mm), on a 45° wedge on the mast's top | its docs' drawing | — |
| 11 Oct | Printed parts' edges rounded 0.8 mm (or chamfered where OCC can't round them); the extractor's gears and the FLOWER block drawn to their real profiles | they're the STLs that get printed | — |
| 11 Oct | **v3.1, the practicality pass.** Checked as a mechanic would, not only for clashes: can it come apart, is each part held, can it be made | the user: "a lot of weird things that don't actually practically work" | — |
| 11 Oct | The battery stands on the shelf over the rear drive motors and lifts straight out (a strap); a check proves the lift | low between the rails it was boxed in by the rear cross channel, the rear motors and the launcher: no battery swap between matches | — |
| 11 Oct | The hubs lie flat on a PETG-CF deck over the front (on the front cross channel and the launcher's front top channel); the Limelight on the deck's tongue, not a printed mast | the battery took the rack's place; the mast was a 0.4 in printed stick 4 in tall | the hubs' wiring wants them elsewhere |
| 11 Oct | Each star's shaft runs in a bearing in a thin arm of the front bracket over the star; the servo rises 0.18 in (0.04 in under the front drive motor) | it hung on the servo's spline through the clutch | — |
| 11 Oct | The rear motor plates are cut from 2.5 mm aluminium to the drawing | the "pattern plate (CHECK the SKU)" was an invented part | goBILDA has a plate with this pattern |
| 11 Oct | The intake: the swing arms kept (the user asked about the mentor's slides: a slide's two guides across 9.8 in rack and wear, and the pivots fit); the motor properly mounted (a plate on its face bridged to the right arm, its far end in a cradle on a standoff from the left arm), the arms PETG-CF | the motor's face was 0.6 in from the arm with the belt between: it wasn't mounted at all, and hung off one arm | the mentor's slides are built and proven |
| 11 Oct | Nothing floats: a check measures the gap at every claimed mounting. The front cross channel on taller front motor plates (1123-0048-0192) through printed end blocks; the rear cross channel on printed corner blocks; washers under the middle outer-plate standoffs; the extractor's servo horn; the lane's front shafts' hangers; the ceiling's hinge | each was 0.05 to 1.0 in from what it was said to bolt to ("goBILDA pattern brackets" that were never drawn) | — |
| 11 Oct | The flywheel sides' blocks run between the rails of printed guides inside his channels; his channels get rough windows | a hand-cut slot in aluminium can't be a sliding fit | the guides wear (then PTFE tape, or printed in nylon) |
| 11 Oct | **v3.2:** the shot goes over the hubs. The hood is drawn as a curved elbow on the turret's top (45° up, a 3 in bend; the robot 13.5 in tall); the ball's path out of it, grown by 0.25 in and revolved all the way round, is reserved (`shot_exit`) | the turret turns the hood all the way round and shots left flat at 10 to 11.5 in: aimed within about 75° of ahead, one went through the hubs and the Limelight. A voxel search found no room for a hub anywhere below that height, in any orientation, even with no room for plugs. (A flat lid high enough to clear made the robot 15.6 in tall; the user pointed out the hood will be curved.) | the rig's hood: any launch from about 15° up clears with a 3 in bend; a tighter or looser bend changes the height, and `shot_exit` checks it |
| 11 Oct | The hubs lie flat on a PETG deck (not PETG-CF: no carbon under them) on the front cross channel, either side of the Limelight, each under a printed roof on four posts (four screws); 0.6 in clear round every edge for plugs (`plugs_` reserves); REV's resistive grounding strap | the old deck put the hubs 0.16 in apart and 0.26 in from the turning hood, edges that carry plugs; their tops were bare to falling pieces and other robots' arms | — |
| 11 Oct | The Limelight between the hubs, on its own base on the front cross channel (2 M4 up from inside the channel); a check keeps the robot out of its 54.5 × 42° view | the hubs' plug room took the deck's tongue | — |

## Open

1. Software: the Autos' start poses are drawn for the mentor's longer body; redraw them for v3 (14.35 in) so it backs onto the wall.
2. Rig values: the four springs' rates and preloads, the rest gaps, the feeder motor's speed, the flap faces' material.
3. Confirm on real parts: the battery's and hubs' sizes, the servos' spline positions.
4. Holes to mark from his real parts: the launcher's feet, the hood on the turret, the flywheel springs' anchors.
5. The Limelight's new pose (between the hubs on the centreline, lens about 10.8 in up and 4.6 in ahead of the body's centre, 45° up) goes into `CameraMount`; measure it on the robot.
6. Confirm the servo horn's screw pattern (4 M3 on a 16 mm circle is drawn) before printing the extractor's gear.
7. The hood is still a stand-in (a curved elbow, 45° up on a 3 in bend): the launcher rig sets its shape. Put the rig's angle and bend in `LAUNCH_DEG` and `HOOD_BEND` and `shot_exit` says whether it clears the hubs (from about 15° up does, with a 3 in bend).
8. **The star wheel's joint to its shaft:** the SWYFT wheel's 7 mm hex bore can't take the 8 mm round shaft (the one-way clutch needs round). With the mentor's star in hand: a 1/2 in hex variant of the wheel and a printed adapter, or its bore drilled out to 8 mm and a printed clamp hub bolted to its face.
9. The front motor plates are now 1123-0048-0192 (192 mm): order those, not 0144.
