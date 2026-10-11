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
| 11 Oct | One spring a side carries both that side's flywheel and feeder wheel (a slide, 0.41 in) | one spring, slide and stop to tune a side; a NECTAR opens both pinches the same | a NECTAR waiting in the stopped feeder spoils a POLLEN's shot on the rig, and stopping the lane until each shot clears costs too much time |
| 11 Oct | The feeder stays put; stopping its motor is the gate (no gate servo) | the motor can sit anywhere a belt reaches | — |
| 11 Oct | The feeder's motor rides the left module, under the flywheel's motor, belted from behind the module (reserved: `feeder_motor`, `feeder_drive`) | no room ahead of the feeder: anything at the feeder shaft's front end sits in a NECTAR's path into the column | step 2 can't fit the battery and this drive together |
| 11 Oct | Layout v3: re-plan top-down around a space budget; the mentor's built robot is the reference for each mechanism's job and key dimensions, not its parts | patching his packaging left no home for the feeder motor | the mentor prefers his packaging (then v2 stands) |
| 11 Oct | Our own corners on his track and wheelbase, mirrored: front motors straight over their axles, five holes up; rear motors a hole behind and three up | frees the space ahead of the launcher; identical left and right for spares, two robots and Pedro's tuning | — |
| 11 Oct | A front cross channel ties the rails; the Limelight's mast stands on it, centred | closes "front of frame"; a centred camera | — |
| 11 Oct | The battery stands against the right rail, ahead of the rear cross channel | the feeder's drive is behind the module on the left; 0.6 kg 1.95 in right about balances the feeder's motor 3.6 in left | the mentor's battery is a different size |
| 11 Oct | The turret's drive gear points forward (as his); its servo swings 30° and encoder B 270° about the gear's axis | searched every 15°, each alone, against the front drive motors, the flywheels open and shut, and the launch column: anywhere else one of them hits; belt and mesh unchanged | — |
| 11 Oct | His launcher frame re-housed: the flywheels' pivot hardware dropped (the sprung slides replace it); the drive plate redrawn around what it carries; the rear lower cross channel, its blocks and the column's back guides raised 0.9 in above the feeder's drive; the side channels end at X −4.98, clear of our rear belts; the feeder bridge trimmed clear of the battery and the feeder's drive | each the smallest change that clears the budget; the feeder pair holds the ball where the back guides no longer reach | the rig shows the ball needs a back guide below z 4.96 |
| 11 Oct | Rear drive motors at a 275 mm belt's own centres, 0.06 in forward (0.45 mm of tension in slotted holes) | inside the 17.75 in outline | — |

## Open, in the order they get decided (hardest to move first)

1. The sprung slides' hardware (goBILDA or MGN9 rails), springs and stops; the feeder motor's bracket on the left module.
2. The turret drive plate's mounting screws and the servo's bracket (drawn as windows in the plate).
3. Confirm with the mentor: the re-housed frame, the corners, the battery's place and size.
