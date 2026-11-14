"""A shoulder for 19429's FLOWER stick, right arm: covers the bare shaft between the blue wall and the arm's wheel.

    pip install cadquery
    python3 cad/stick-shoulder-19429/parts.py     # writes the STEP and STL files next to this file

On 20245 the white wall sits against the wheel hub. On 19429 the blue wall sits well inboard of the right arm, leaving
bare 8 mm shaft that the FLOWER's lip catches. This is a hood over the wheel and that gap, screwed to the arm at one end
and to the blue wall at the other. It replaces 19429's present blue shoulder on that arm, which goes to 20245 as it is.

It comes in two halves that telescope: the arm half's inner shell slides inside the wall half's outer shell. Each half
screws to its own end, so the overlap takes up whatever the arm-to-wall distance really is (SPAN_MIN..SPAN_MAX): no
measurement is needed to print it. Once the distance is measured, ONE_PIECE = True makes a single part of exactly SPAN.

Every size below is a guess from photos (14 Nov 2026) and the Onshape model, not a measurement. The README says which
matter and how to check them.

Millimetres. Frame: the working pose (stick down, wheel on the floor). Origin on the shaft axis at the arm plate's inner
face. +X forward (toward the FLOWER), +Z up, +Y along the shaft from the arm toward the blue wall (inboard). For the
right arm that is the robot's own frame, so these files are for the RIGHT arm; mirror in Y for a left one.
Angles in the XZ plane are degrees from +X (forward) toward +Z (up).
"""
import math
import os

import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))

# ---- the stick (guesses: photos of 14 Nov, the Onshape model's 312 mm 8 mm REX shaft and 200 mm Flower Ramp) ----
WHEEL_D = 48.0               # the arm's wheel, O-ring included. Photos: about 6 shaft diameters. The hood clears this
SPAN = 84.0                  # arm plate's inner face to the blue wall's outer face: clamp + wheel (~40) + bare shaft (~44)
SPAN_MIN, SPAN_MAX = 72.0, 96.0   # what the two halves cover; SPAN is in the middle
ARM_ANGLE = 30.0             # the arm above the floor in the working pose; it runs back and up from the shaft
# ---- the hood ----
CLEAR = 3.0                  # wheel to hood, all round
SHELL = 3.2                  # each shell's wall
SLIDE = 0.4                  # between the two shells (per side) so they slide
A_FRONT = -25.0              # the hood's front-bottom edge: below the shaft, but above the floor (see floor_gap())
A_BACK_PAST_ARM = 12.0       # the hood runs this far past the arm's line, over its top edge
CAP = 4.0                    # the end caps against the arm and the wall
# ---- the arm screws: a slot along the arm's centre line, so any 8 mm-pitch goBILDA holes there fit ----
ARM_SLOT_R = (36.0, 62.0)    # slot from/to, measured along the arm from the shaft axis
ARM_SLOT_OFFSET = 0.0        # sideways from the arm's centre line, + toward the top of the arm
ARM_TAB_W = 18.0
# ---- the wall screws: two holes; the printed blue wall is drilled through them on the robot ----
WALL_HOLES_R = (40.0, 54.0)  # along the arm's line from the shaft axis (the wall runs back roughly parallel to the arm)
WALL_TAB_W = 18.0
M4_CLEAR = 4.5
ONE_PIECE = False


def r_in():
    return WHEEL_D / 2 + CLEAR


def a_arm():
    return 180.0 - ARM_ANGLE


def floor_gap():
    """The front-bottom edge's height above the floor: the lowest point of the hood ahead of the wheel."""
    r_out = r_in() + 2 * SHELL + SLIDE
    return WHEEL_D / 2 + r_out * math.sin(math.radians(A_FRONT))


def sector(r0, r1, a0, a1, y0, y1, n=48):
    """An arc of shell between radii r0..r1 and angles a0..a1 (degrees), from y0 to y1 along the shaft."""
    angs = [math.radians(a0 + (a1 - a0) * i / n) for i in range(n + 1)]
    pts = [(r1 * math.cos(a), r1 * math.sin(a)) for a in angs] + [(r0 * math.cos(a), r0 * math.sin(a)) for a in reversed(angs)]
    return place(cq.Workplane("XZ").polyline(pts).close().extrude(y1 - y0), y0)


def place(wp, y0):
    """Workplane("XZ") extrudes toward -Y: move the solid so it starts at y0."""
    bb = wp.val().BoundingBox()
    return wp.translate((0, y0 - bb.ymin, 0))


def tab(r0, r1, width, offset, y0, y1):
    """A flat tab along the arm's line, from r0 to r1 from the shaft axis."""
    a = math.radians(a_arm())
    u = (math.cos(a), math.sin(a))                 # along the arm
    v = (-math.sin(a), math.cos(a))                # across it, toward the arm's top
    c = [(u[0] * r + v[0] * (offset + s * width / 2), u[1] * r + v[1] * (offset + s * width / 2))
         for r, s in ((r0, -1), (r1, -1), (r1, 1), (r0, 1))]
    return place(cq.Workplane("XZ").polyline(c).close().extrude(y1 - y0), y0)


def hole_along_arm(r, offset, y0, y1, d=M4_CLEAR):
    a = math.radians(a_arm())
    x = r * math.cos(a) - offset * math.sin(a)
    z = r * math.sin(a) + offset * math.cos(a)
    return place(cq.Workplane("XZ").center(x, z).circle(d / 2).extrude(y1 - y0), y0)


def slot_along_arm(r0, r1, offset, y0, y1, d=M4_CLEAR):
    s = tab(r0, r1, d, offset, y0, y1)
    return s.union(hole_along_arm(r0, offset, y0, y1, d)).union(hole_along_arm(r1, offset, y0, y1, d))


def arm_half(length):
    """Screws to the arm plate's inner face: end cap, tab with the slot, and the inner shell over the wheel."""
    ri, rm = r_in(), r_in() + SHELL
    a1 = a_arm() + A_BACK_PAST_ARM
    part = sector(ri, rm, A_FRONT, a1, 0, length)
    part = part.union(sector(ri, rm + SLIDE + SHELL, A_FRONT, a1, 0, CAP))       # the cap: full hood thickness
    part = part.union(tab(ri, ARM_SLOT_R[1] + 7, ARM_TAB_W, ARM_SLOT_OFFSET, 0, CAP))
    return part.cut(slot_along_arm(*ARM_SLOT_R, ARM_SLOT_OFFSET, -1, CAP + 1))


def wall_half(y_start, y_wall):
    """Screws to the blue wall's outer face: end cap, tab with two holes, and the outer shell."""
    ri, ro = r_in(), r_in() + 2 * SHELL + SLIDE
    a1 = a_arm() + A_BACK_PAST_ARM
    part = sector(ri + SHELL + SLIDE, ro, A_FRONT, a1, y_start, y_wall)
    part = part.union(sector(ri, ro, A_FRONT, a1, y_wall - CAP, y_wall))
    part = part.union(tab(ri, WALL_HOLES_R[1] + 7, WALL_TAB_W, 0, y_wall - CAP, y_wall))
    for r in WALL_HOLES_R:
        part = part.cut(hole_along_arm(r, 0, y_wall - CAP - 1, y_wall + 1))
    return part


def halves():
    """The two halves at the shortest span. The arm half's shell reaches nearly to the wall's cap at SPAN_MIN; the wall
    half's shell reaches nearly to the arm's cap at SPAN_MIN and still overlaps the arm half's by 15 mm at SPAN_MAX."""
    arm = arm_half(SPAN_MIN - CAP - 1.0)
    wall = wall_half(CAP + 1.0, SPAN_MIN)
    overlap_at_max = (SPAN_MIN - CAP - 1.0) - (SPAN_MAX - SPAN_MIN + CAP + 1.0)
    assert overlap_at_max >= 15.0, f"at SPAN_MAX the shells overlap only {overlap_at_max:.1f} mm"
    return arm, wall


def one_piece():
    part = arm_half(SPAN).union(wall_half(CAP, SPAN))
    return part


def export(part, name):
    solid = part.val()
    assert solid.isValid(), name
    cq.exporters.export(part, os.path.join(HERE, name + ".step"))
    cq.exporters.export(part, os.path.join(HERE, name + ".stl"), tolerance=0.01, angularTolerance=0.1)
    bb = solid.BoundingBox()
    print(f"{name}.step/.stl  {bb.xlen:.1f} x {bb.ylen:.1f} x {bb.zlen:.1f} mm  {solid.Volume() / 1000:.1f} cm3")


def main():
    gap = floor_gap()
    print(f"hood: inner radius {r_in():.1f} mm, outer {r_in() + 2 * SHELL + SLIDE:.1f} mm; "
          f"front edge {gap:.1f} mm above the floor")
    assert gap >= 6.0, "the hood's front edge would drag on the floor: raise A_FRONT"
    if ONE_PIECE:
        export(one_piece(), "shoulder_one_piece")
    else:
        arm, wall = halves()
        export(arm, "shoulder_arm_half")
        export(wall, "shoulder_wall_half")


if __name__ == "__main__":
    main()
