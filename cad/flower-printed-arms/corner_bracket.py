"""A printed bracket for the corner-wheel servo, every screw driven from outside (mentor asked, 26 Nov 2026).

    python3 cad/flower-printed-arms/corner_bracket.py     # writes corner_bracket_left.* and corner_bracket_right.*

The servo stays where it is today (geometry.CORNER): goBILDA's Speed servo upright, flush against the intake side
channel's outer face, spline forward, the two SWYFT wheels on it. What changes is how it's held:

- **A plate** stands across the front of the intake side channel. Its back face sits flat on the channel's front
  flange; the servo slides in from behind through a cutout and its mounting ears land on the plate's front face. Four
  M4 x 16 from the front through the ears into M4 heat-set inserts in the plate (the plate is thickened behind them,
  outboard of the channel, where nothing is in the way).
- **A tongue** runs back from the plate's bottom, under the channel's open bottom, and two M4 x 10 button heads go up
  from underneath into the threaded holes of the goBILDA Dual Block inside the channel. No nuts behind the wall, nothing
  near the slide.
- **Optional:** two clearance holes in the plate line up with the flange's holes, for M4 x 12 with nuts if you can
  reach behind the flange; the bracket doesn't need them.

Where the channel is: the robot's STEP of 24 Nov 2026, as measured on branch claude/corner-module-wall-wip
(cad/corner-wheels/parts.py): front flange's front face X 8.50 in, its holes on |Y| 5.049 in every 8 mm up from Z 3.176
in, its outer face |Y| 5.363 in, the channel's bottom Z 2.86 in, the Dual Block's holes at X 7.867 and 8.182 in on
|Y| 5.048 in. **Check those on the robot before printing**: the left corner was mirrored from the right in that file.

Robot frame, millimetres (+X forward, +Y left, +Z up; origin on the floor under the middle of the drive axles). Built
for the left corner; the right one is its mirror image.
"""
import os
import sys

import cadquery as cq
import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from geometry import CORNER, IN, corner_boxes  # noqa: E402

# ---- the intake side channel (24 Nov STEP) -------------------------------------------------------------------------
FLANGE_FRONT_X = 8.50 * IN          # 215.9
FLANGE_Y = (4.895 * IN, 5.363 * IN)  # the front flange spans this |Y| (the channel's outer face at 5.363)
FLANGE_HOLE_Y = 5.049 * IN
FLANGE_HOLES_Z = [z * IN for z in (3.176, 3.491, 3.806)]
CHANNEL_BOTTOM_Z = 2.86 * IN        # 72.6: the channel's open bottom
BLOCK_HOLES_X = (7.867 * IN, 8.182 * IN)
BLOCK_HOLE_Y = 5.048 * IN
RAIL_END_X = 7.62 * IN              # the frame rail's end is at 7.55: the tongue stops short of it

# ---- the servo's ears (goBILDA 2000-0025-0003): 48 x 10 mm holes, 2.5 mm thick, 27.2 mm up its body from the bottom
EAR_FROM_BOTTOM = 27.2
EAR_HOLES = [(dz, dy) for dz in (-24.0, 24.0) for dy in (-5.0, 5.0)]   # about the body's middle (Z) and the spline (Y)

# ---- the bracket -----------------------------------------------------------------------------------------------------
CLEAR = 0.4                         # round the servo's body in the cutout
TONGUE_T = 5.0
INSERT_D, INSERT_DEPTH = 5.6, 8.0   # M4 heat-set inserts
M4 = 4.4
BUTTON_HEAD_CB = 8.0                # counterbore for the M4 button heads under the tongue


def servo_box():
    b = corner_boxes("left")
    return b["servo"][1], b["servo"][2], b["wheel"][1]


def bracket_left():
    lo, hi, wc = servo_box()
    body_x0 = lo[0]
    ear_x = body_x0 + EAR_FROM_BOTTOM                 # the ears' back face
    plate_x0, plate_x1 = FLANGE_FRONT_X, ear_x        # the plate fills the gap between the flange and the ears
    assert plate_x1 - plate_x0 > 3.0, "no room for the plate between the flange and the servo's ears"
    body_zmid = (lo[2] + hi[2]) / 2
    z0, z1 = body_zmid - 34.0, body_zmid + 34.0       # past the ear holes (±24) with 10 mm of plate
    y0, y1 = FLANGE_Y[0] + 1.0, hi[1] + 9.0           # inboard over the flange, outboard past the body
    plate = cq.Workplane("XY").box(plate_x1 - plate_x0, y1 - y0, z1 - z0, centered=False).translate((plate_x0, y0, z0))
    # thicken behind the ears, outboard of the channel, for the inserts
    boss = cq.Workplane("XY").box(INSERT_DEPTH + 2.0, y1 - FLANGE_Y[1], z1 - z0, centered=False) \
        .translate((plate_x1 - (INSERT_DEPTH + 2.0), FLANGE_Y[1] + 0.3, z0))
    body = plate.union(boss)
    # the cutout the servo's body passes through
    cut = cq.Workplane("XY").box(60.0, (hi[1] - lo[1]) + 2 * CLEAR, (hi[2] - lo[2]) + 2 * CLEAR, centered=False) \
        .translate((plate_x1 - 50.0, lo[1] - CLEAR, lo[2] - CLEAR))
    body = body.cut(cut)
    # the tongue under the channel's open bottom, to the Dual Block
    tongue = cq.Workplane("XY").box(plate_x0 - RAIL_END_X + plate_x1 - plate_x0, FLANGE_Y[1] - 0.5 - y0, TONGUE_T,
                                    centered=False).translate((RAIL_END_X, y0, CHANNEL_BOTTOM_Z - TONGUE_T))
    body = body.union(tongue)               # it meets the plate's back face inside the plate's height
    for bx in BLOCK_HOLES_X:
        body = body.cut(cq.Workplane("XY").circle(M4 / 2).extrude(40).translate((bx, BLOCK_HOLE_Y, z0 - 1)))
    # the servo's ear holes: inserts from the front, in the thickened part
    for dz, dy in EAR_HOLES:
        hz, hy = body_zmid + dz, wc[1] + dy
        body = body.cut(cq.Workplane("YZ").circle(INSERT_D / 2).extrude(-INSERT_DEPTH).translate((plate_x1, hy, hz)))
    # optional: clearance holes onto the flange's holes
    for hz in FLANGE_HOLES_Z:
        if z0 + 4 < hz < z1 - 4:
            body = body.cut(cq.Workplane("YZ").circle(M4 / 2).extrude(-20).translate((plate_x1 + 1, FLANGE_HOLE_Y, hz)))
    return body


def export(shape, name):
    for ext in ("step", "stl"):
        cq.exporters.export(shape, os.path.join(HERE, f"{name}.{ext}"), tolerance=0.05, angularTolerance=0.1)


def main():
    left = bracket_left()
    right = left.mirror("XZ")
    export(left, "corner_bracket_left")
    export(right, "corner_bracket_right")
    v = left.val().Volume() / 1000
    bb = left.val().BoundingBox()
    print(f"bracket: {v:.1f} cm^3; X {bb.xmin / IN:.2f}..{bb.xmax / IN:.2f} in, |Y| {bb.ymin / IN:.2f}..{bb.ymax / IN:.2f},"
          f" Z {bb.zmin / IN:.2f}..{bb.zmax / IN:.2f}")


if __name__ == "__main__":
    main()
