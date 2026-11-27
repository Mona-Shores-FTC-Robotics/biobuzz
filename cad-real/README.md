# The robot with the printed FLOWER stick and the corner wheels, from the mentor's STEP (scratch branch, not for merging)

`corners_and_stick.step` (28.5 MB; built with `REAL_MECANUM=1 python3 strip3.py ...`): cut from `robotdhs.step` (the mentor's Onshape export, 26 Nov 2026, 177 MB, not in
git), **in the STEP's own frame**, so it lands in place when inserted at the origin in Onshape. Stick down. Each part
is its own named part, under one named group each (hide a group to hide its parts):

| Group | What |
|---|---|
| chassis | The whole chassis: frame, side channels, plates, gussets (no motors, belts or fasteners) |
| mecanum wheels, front (from the robot CAD) | **The two front mecanum wheels as in the mentor's CAD:** side plates, hub core, all 40 roller covers, the mount and the shaft. Left out of them, to fit the file: what sits inside the rollers and the hub (roller cores, axles, e-clips, shims, the 1600-0307 parts), the drive pulleys and bearing bodies, the lettering and screws |
| mecanum wheels (plain cylinders) | The two back wheels, as cylinders of their size |
| chassis, away from the front corners (outer boxes) | Chassis parts ending more than about 4 in behind the front corner, each as its outer box (mentor: simplify far from the front, nothing near the intake, corners, stick or wheels) |
| intake | The intake's channels, plates and shafts (no rollers, motor or gearbox) |
| limelight, limelight tower | The Limelight and the channel and gussets it sits on |
| control hub and expansion hub | The two hubs as their outer boxes, and the plate and beams they're on |
| flower stick: kept parts | What stays from today's stick: its SWYFT servo, hub shafts and blocks, the tip shaft, the mountain |
| printed stick (new) | The two printed arms (PR #766's `arm.step`, 67 mm hump) on the real pivot and tip, the two foot skids, and a REX shaft collar where the 8T sprocket was |
| corner L, corner R | **Each part on its own, to drag:** "corner servo" (goBILDA Speed, simplified: body, ears, spline), "corner servo frame" (goBILDA 1802-0043-0001, simplified: outline, window, edge holes), "corner wheels" (2 SWYFT 3.5 in, from SWYFT's STEP), "corner wheel screw and washer" (a guess) |
| old stick, for reference (hide) | Today's aluminium arms, tip plates and tip bearings, as drawn |

Left out: the launcher, the transfer, the game pieces, fasteners, belts and sprockets (the active roller's GT2 belt
and 8T sprockets included), motors and gearboxes, the battery, the intake's rollers.

**The corners** are where PR #766 puts them: the servo frame's long edge on the intake's upright 5 Hole Lowside
U-Channel, its edge holes in the channel's front column (X 208 mm robot, the mentor's circled holes), on the lowest
hole that keeps the servo above the mecanum's rollers: axle 3.73 in, wheels' bottom 1.98 in off the tile.

`corners_and_stick.png`: iso, front and side, stick down. `corner_closeup.png`: the left front corner from ahead and to the left, low (the printed arm over the corner servo, frame and SWYFT wheels, beside the real front mecanum). Scripts: `strip3.py` (this file), `render3.py` (the
picture), `wallholes.py` and `flangeholes.py` (the intake channel's hole positions), `frame.py` (the robot frame,
from the four drive axles).
