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
| 11 Oct | The turret's drive gear points forward (as his); its servo and encoder B swing 180° about the gear's axis, behind it | searched every 15°: anywhere else the drive's hardware meets a flywheel's shaft ends; this clears the front drive motors by 1.3 in, belt and mesh unchanged | — |

## Open, in the order they get decided (hardest to move first)

3. The launcher's frame around the column, inside the rear corners.
4. Its feet, the sprung slides' hardware, the screws.
