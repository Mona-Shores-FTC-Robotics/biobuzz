"""STEP files to drop into the team's Onshape robot: the printed arms with the stick down, and each front corner as this
folder models it (goBILDA's Speed servo, two SWYFT wheels, the screw and washer, and the mount, which is a guess).

    python3 cad/flower-printed-arms/export_onshape.py      # writes onshape/*.step

All in the robot frame (millimetres; +X forward, +Y left, +Z up; origin on the floor under the middle of the drive
axles). In Onshape, insert `stick_down_with_corners.step` and line it up by the arm: put the arm's pivot hole on the
stick's pivot axis and its flat face where the channel's face is, with the stick down. Everything else in the file then
sits where this folder assumes it does, so the corner parts can be compared with the real ones at a glance.
"""
import os
import sys

import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import corner_bracket  # noqa: E402
import parts  # noqa: E402
from check import corner_solids, placement, to_cq_matrix  # noqa: E402
from geometry import ARM_Y, SWING_DEG  # noqa: E402

OUT = os.path.join(HERE, "onshape")
COLOURS = {"arm": (0.16, 0.37, 0.82), "wheel": (0.95, 0.95, 0.92), "servo": (0.12, 0.12, 0.14),
           "mount": (0.76, 0.25, 0.18), "screw": (0.6, 0.6, 0.62)}


def corner(side):
    a = cq.Assembly(name=f"corner_{side}")
    for name, s in corner_solids(side, real_wheel=True).items():
        label = {"wheel": "SWYFT wheels x2", "servo": "goBILDA Speed servo 2000-0025-0003",
                 "mount": "mount (guess)", "screw": "wheel screw and washer (guess)"}.get(name, name)
        a.add(s, name=label, color=cq.Color(*COLOURS.get(name, (0.5, 0.5, 0.5))))
    br = corner_bracket.bracket_left()
    a.add(br if side == "left" else br.mirror("XZ"), name="printed corner bracket (proposed)",
          color=cq.Color(0.95, 0.75, 0.2))
    return a


def main():
    os.makedirs(OUT, exist_ok=True)
    arm = parts.arm().val()
    top = cq.Assembly(name="stick_down_with_corners")
    for side in ("left", "right"):
        M = placement(side, SWING_DEG)
        if side == "right":          # no mirror (a mirrored solid is inside out in STEP): the arm is alike on both
            M[1, 2], M[1, 3] = 1.0, ARM_Y["right"][1]   # faces, so the same part from the outer face in fills the band
        placed = arm.transformGeometry(to_cq_matrix(M))
        top.add(placed, name=f"printed arm {side} (down, 162 deg)", color=cq.Color(*COLOURS["arm"]))
        c = corner(side)
        top.add(c, name=f"corner {side}")
        c.save(os.path.join(OUT, f"corner_{side}.step"))
    top.save(os.path.join(OUT, "stick_down_with_corners.step"))
    for f in sorted(os.listdir(OUT)):
        print(f, os.path.getsize(os.path.join(OUT, f)) // 1024, "KB")


if __name__ == "__main__":
    main()
