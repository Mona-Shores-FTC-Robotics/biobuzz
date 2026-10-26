# Launcher module (the mentor's launcher with our changes, for the printed chassis)

`launcher-module.step.gz` (gunzip it to get the 87 MB STEP) holds the mentor's goBILDA "Launcher Concept", as placed
on our full robot, plus cad/transfer's launcher changes. Fasteners are left out (`LEAN=1`).
- **His Launcher Concept:** the goBILDA 3208-0004-0001 turret over two pairs of 96 mm Gecko flywheels, their channels
  and his dual blocks. It sits 0.8 in forward of where he drew it, with the flywheel modules 16 mm in.
- **cad/transfer's launcher changes:** the flywheel motors moved out and up, with 24T pulleys; the turret servo drive
  and its two encoders; and the feeder, gate, pad and backstop.

The file is in millimetres, in the robot frame: +X forward, +Y left, +Z up, origin on the floor under his chassis
centre, his front face at X 7.56 in. Rebuild it with:

    LEAN=1 MOTORS_FROM=<his 8 Oct Robot.step> python3 cad/modules/launcher-module/build.py <his 9 Oct Robot.step>

All figures below are in inches.

| | |
|---|---|
| Launch column (the turret's axis) | X -2.045, Y 0.158 |
| Turret drive gear (64T) | X 1.737, Y 0.158: it points forward (+X), 96 mm ahead of the axis |
| Envelope | X -5.71 to 4.55, Y -7.35 to 7.66, z 0.38 to 9.68. The low parts are the feeder floor, the pad's hinge blocks and the backstop. |
| Launcher frame | Two 5-hole low-side U-channels, one each side (\|Y\| 5.04 to 5.83, z 3.84 to 5.74, X -4.87 to 0.80), the rear 10-hole cross channel, and the 8-hole channel on top (z 6.99 to 8.88). Our robot leaves out his front 10-hole cross channel, its dual blocks and the U-beams under it, because the lane's pieces pass there. |

**Interface:** his 9 Oct CAD doesn't show one. The frame's bottom (z 3.84) is about 1 in above the drive rails' tops
(z 2.85), with nothing between them. Where it bolts down is a question for the mentor. On our robot the front drive
motors' mounts sit just ahead of the frame and clear it.
