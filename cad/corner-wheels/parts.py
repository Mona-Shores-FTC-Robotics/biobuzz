"""Corner wheels, prototype 2 (mentor's idea, 23 Nov 2026; servo fixed 24 Nov 2026): a TPU flap wheel on a vertical
axle at each front corner, turned inward by a continuous-rotation servo, and the printed bracket that holds the servo.

    pip install cadquery
    python3 cad/corner-wheels/parts.py

Writes, next to this file:
  flap_wheel.*                             the wheel, one file for both corners (the right one goes in upside down)
  bracket_left.* / bracket_right.*         the brackets, plate down as they print
  corner_module_left.step / _right.step    each corner assembled in the robot frame: bracket, servo, hub, wheel, screws

Robot frame (cad/advantagescope/README.md), millimetres in the files, inches in the comments: +X forward, +Y left,
+Z up, origin on the floor under the middle of the four drive axles.

How a corner goes together, from the floor up (the 31 Oct CAD; doc/corner-wheels.md has the clearances):
- The wheel: flaps Z 0.60..1.65 in (a POLLEN's equator, Z 1.40, inside them), its boss to Z 1.85, axle at X 8.94,
  Y +6.85 (left) / -6.95 (right).
- A goBILDA 1908 Series Servo Hub (25 Tooth Spline, 32mm Diameter), 8 mm, on the boss: four M4 x 16 socket head
  screws come up through the wheel from counterbores in its underside into the hub's threaded holes on the 16 mm
  circle. Its counterbored face (the servo screw's head) faces the wheel.
- The servo, a goBILDA 2000 Series Dual Mode Servo (or any standard servo with the same 40 x 20 body and 48 x 10 mm
  flange holes), upside down, spline into the hub. A standard servo's flange is near its spline end, so upside down the
  flange is near the bottom and the body stands up above it: flange Z 2.53..2.69 in, body up to Z 3.76 in. Its body
  runs FORWARD from the spline on both corners (the spline 10 mm from the body's back end), so nothing reaches back
  over the drive wheel; the flange's front tab ends at X 10.40, inside the 18 in starting cube's X 10.43.
- The bracket's plate, Z 2.69..2.94 in, sits on top of the flange: the body passes up through its cut-out and four M3
  screws come up through the flange's slots into the plate. Its ear stands up from the plate and bolts with two M4 to
  the intake's side channel's outer face (left, Y +5.35, X 7.60..8.50) or the front face of the short channel ahead of
  the right drive wheel (right, X 7.87).

The servo and hub in the assemblies are envelopes drawn from goBILDA's STEP files (2000-0025-0002, 1908-0025-0032,
gobilda.com), not the files themselves: swap goBILDA's in Onshape if you want the detail.
"""
import math
import os

import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))
IN = 25.4

# ---- the stack, robot Z in mm -------------------------------------------------------------------------------------
FLAP_Z0 = 0.60 * IN                  # 15.24
FLAP_Z1 = 1.65 * IN                  # 41.91
BOSS_H = 5.0
HUB_Z0 = FLAP_Z1 + BOSS_H            # 46.91: the hub's counterbored face, on the wheel's boss
HUB_H = 8.0                          # goBILDA 1908: 32 mm across, 8 mm overall
HUB_Z1 = HUB_Z0 + HUB_H              # 54.91: the hub's spline side
CAP_GAP = 0.5                        # between the hub and the servo's top cap
# The servo, from goBILDA's STEP (servo frame, mm): body X -20..20, Y -10..10; case bottom Z -27.2; flange Z 0..4
# (its holes 48 x 10 apart, slotted); top cap to Z 12.8; spline r 3.05 at X -10 (10 mm from the end), to Z 16.9.
SERVO_TOP = 12.8
SERVO_BOTTOM = -27.2
FLANGE_T = 4.0
FLANGE_HALF_L = 27.15
SPLINE_X = -10.0
SERVO_Z0 = HUB_Z1 + CAP_GAP + SERVO_TOP   # 68.21: robot Z of the servo frame's Z 0 (the flange's body-side face)

# ---- the wheel ----------------------------------------------------------------------------------------------------
WHEEL_OD = 2.5 * IN          # 63.5: the size that fits (2 in fits with room; 3 in hits the stick and the 18 in cube)
FLAP_H = FLAP_Z1 - FLAP_Z0   # 26.7
HUB_RING_OD = 30.0
FLAPS = 8
FLAP_T = 2.2                 # thin enough to fold under a ball pinned on a wall, thick enough to throw it
FLAP_SWEEP_DEG = 35          # each flap curves back against the spin, so it wipes rather than stabs
BORE = 8.3                   # clears the hub's centre and the servo screw's head
M4 = 4.4
M4_HEAD_CB = 7.6             # counterbore for an M4 socket head (7.0)
CB_DEPTH = 10.7              # counterbored from both faces this deep; an M4 x 30 from below reaches 4 mm into the hub
HUB_HOLES = [(8 * math.cos(math.radians(a)), 8 * math.sin(math.radians(a))) for a in (45, 135, 225, 315)]

# ---- the bracket ---------------------------------------------------------------------------------------------------
# One printed part per corner, bolted to the outside of the light-blue goBILDA pattern plate beside the front drive
# wheel (96 x 192 mm, the 31 Oct CAD's "1123-0096-0192" on the right; the left assumed to be its mirror image). A wall
# lies flat on the plate's outer face, flush into its top-front corner, and runs straight forward past the plate's end;
# a shelf off the wall's inner side, at the servo's flange, takes the servo; a gusset ties the two. goBILDA holes sit on
# an 8 mm grid 4 mm in from a plate's edges, so with the wall pushed into that corner its holes meet the plate's.
PLATE_T = 0.25 * IN                   # the shelf, 6.35 mm
PLATE_Z0 = SERVO_Z0                   # 68.21 (2.69 in): the shelf's underside on the flange
SERVO_CUT_L, SERVO_CUT_W = 40.8, 20.6  # the body passes up through it
SERVO_HOLE_L, SERVO_HOLE_W = 48.0, 10.0
SIDE_PLATE_FACE = 7.72                # |Y| of the light-blue plate's outer face, in
SIDE_PLATE_FRONT = 7.55               # X of its front end, in
SIDE_PLATE_TOP = 4.75                 # Z of its top edge, in
WALL_T = 5.0                          # mm, outboard of the plate's face
LEG_L = 40.0                          # mm of wall on the plate, behind its front end: five hole columns
WALL_TOP_FWD = PLATE_Z0 + 28.0        # mm: ahead of the plate the wall stops just above the servo's top (Z 3.79 in)
SHELF_X0 = 7.65                       # in: 0.27 in clear of the mecanum's rollers at the shelf's height
SHELF_X1 = 10.40                      # in: the 18 in starting cube's front is X 10.43
SHELF_IN = 6.45                       # |Y| in: the shelf's inner edge, outboard of the stick arm's band (|Y| <= 6.32)
GUSSET_W, GUSSET_H = 7.0, 12.0        # mm, on top of the shelf along the wall
AXLE = (8.94, 6.95)                   # in, |Y|; both corners alike


def _flaps():
    r0, r1 = HUB_RING_OD / 2 - 1.0, WHEEL_OD / 2
    flaps = None
    for k in range(FLAPS):
        a0 = 2 * math.pi * k / FLAPS
        out, inn = [], []
        for i in range(9):
            t = i / 8
            r = r0 + (r1 - r0) * t
            a = a0 + math.radians(FLAP_SWEEP_DEG) * t * t
            nx, ny = -math.sin(a), math.cos(a)
            out.append((r * math.cos(a) + nx * FLAP_T / 2, r * math.sin(a) + ny * FLAP_T / 2))
            inn.append((r * math.cos(a) - nx * FLAP_T / 2, r * math.sin(a) - ny * FLAP_T / 2))
        f = cq.Workplane("XY").polyline(out + inn[::-1]).close().extrude(FLAP_H)
        flaps = f if flaps is None else flaps.union(f)
    return flaps


def flap_wheel():
    """The wheel in its own frame: axle on Z, Z 0 its bottom face. The same either way up: a 5 mm boss above and below
    the flaps, and the M4 holes counterbored from both faces. Seen from above, this way up it turns clockwise (the
    left corner: its front moving inward) with each flap's tip trailing; upside down its flaps curve the other way,
    for the right corner."""
    total = FLAP_H + 2 * BOSS_H
    w = (cq.Workplane("XY").circle(HUB_RING_OD / 2).circle(BORE / 2).extrude(total)
         .union(_flaps().translate((0, 0, BOSS_H))))
    for x, y in HUB_HOLES:
        w = w.cut(cq.Workplane("XY").center(x, y).circle(M4 / 2).extrude(total))
        w = w.cut(cq.Workplane("XY").center(x, y).circle(M4_HEAD_CB / 2).extrude(CB_DEPTH))
        w = w.cut(cq.Workplane("XY").workplane(offset=total - CB_DEPTH).center(x, y).circle(M4_HEAD_CB / 2)
                  .extrude(CB_DEPTH))
    return w


def leg_holes():
    """The wall's M4 holes on the plate, robot frame (X, Z) in mm: the goBILDA grid, 4 mm in from the plate's top and
    front edges, 8 mm apart. Bolt through the ones that meet a 4 mm hole in the plate (three or four is plenty)."""
    holes = []
    for i in range(5):
        for j in range(6):
            x = SIDE_PLATE_FRONT * IN - 4.0 - 8.0 * i
            z = SIDE_PLATE_TOP * IN - 4.0 - 8.0 * j
            if z - 2.5 > PLATE_Z0:
                holes.append((x, z))
    return holes


def bracket(side):
    """The bracket in the robot frame (mm), built for the left (|Y| outboard as +Y); the caller mirrors the right."""
    face = SIDE_PLATE_FACE * IN
    front = SIDE_PLATE_FRONT * IN
    top = SIDE_PLATE_TOP * IN
    # the wall: on the plate (the leg, up to the plate's top), then straight forward to the shelf's front
    leg = cq.Workplane("XY").box(LEG_L, WALL_T, top - PLATE_Z0, centered=False).translate((front - LEG_L, face, PLATE_Z0))
    fwd = cq.Workplane("XY").box(SHELF_X1 * IN - front, WALL_T, WALL_TOP_FWD - PLATE_Z0, centered=False).translate(
        (front, face, PLATE_Z0))
    wall = leg.union(fwd)
    for x, z in leg_holes():
        wall = wall.cut(cq.Workplane("XZ").center(x, z).circle(M4 / 2).extrude(-WALL_T).translate((0, face, 0)))
    # the shelf, at the flange, from the wall in to SHELF_IN
    x0, x1, yi = SHELF_X0 * IN, SHELF_X1 * IN, SHELF_IN * IN
    shelf = (cq.Workplane("XY").workplane(offset=PLATE_Z0).center((x0 + x1) / 2, (yi + face) / 2)
             .rect(x1 - x0, face - yi).extrude(PLATE_T).edges("|Z").fillet(2.0))
    # it meets the wall across the gap between the plate's end and the shelf's back
    bridge = cq.Workplane("XY").box(x0 - front + 1.0, 8.0, PLATE_T, centered=False).translate((front - 0.5, face - 8.0, PLATE_Z0))
    ax, ay = AXLE[0] * IN, AXLE[1] * IN
    cx = ax - SPLINE_X                                   # the servo body's middle, forward of the spline
    shelf = shelf.union(bridge).cut(cq.Workplane("XY").workplane(offset=PLATE_Z0).center(cx, ay)
                                    .rect(SERVO_CUT_L, SERVO_CUT_W).extrude(PLATE_T))
    for sx in (-1, 1):
        for sy in (-1, 1):                               # M3 self-tapping up through the flange's slots
            shelf = shelf.cut(cq.Workplane("XY").workplane(offset=PLATE_Z0)
                              .center(cx + sx * SERVO_HOLE_L / 2, ay + sy * SERVO_HOLE_W / 2).circle(1.25).extrude(PLATE_T))
    z = PLATE_Z0 + PLATE_T
    gusset = (cq.Workplane("YZ").polyline([(face, z), (face - GUSSET_W, z), (face, z + GUSSET_H)]).close()
              .extrude(x1 - x0).translate((x0, 0, 0)))
    return wall.union(shelf).union(gusset)


def servo_envelope():
    """The servo in its own frame (goBILDA's): case bottom Z -27.2, flange Z 0..4, top cap to 12.8, spline at X -10."""
    body = cq.Workplane("XY").box(40, 20, -SERVO_BOTTOM, centered=(True, True, False)).translate((0, 0, SERVO_BOTTOM))
    flange = cq.Workplane("XY").box(2 * FLANGE_HALF_L, 20, FLANGE_T, centered=(True, True, False))
    for sx in (-1, 1):
        for sy in (-1, 1):
            flange = flange.cut(cq.Workplane("XY").center(sx * SERVO_HOLE_L / 2, sy * SERVO_HOLE_W / 2)
                                .circle(1.7).extrude(FLANGE_T))
    cap = cq.Workplane("XY").box(40, 20, SERVO_TOP - FLANGE_T, centered=(True, True, False)).translate((0, 0, FLANGE_T))
    spline = cq.Workplane("XY").center(SPLINE_X, 0).circle(3.05).extrude(16.9 - SERVO_TOP).translate((0, 0, SERVO_TOP))
    return body.union(flange).union(cap).union(spline)


def hub_envelope():
    """The goBILDA 1908 hub in its own frame: Z 0 its counterbored face (toward the wheel), 8 mm tall, 32 mm across."""
    h = cq.Workplane("XY").circle(16.0).circle(2.5).extrude(HUB_H)
    h = h.cut(cq.Workplane("XY").circle(8.0).extrude(3.0))   # the servo screw's counterbore
    for x, y in HUB_HOLES:
        h = h.cut(cq.Workplane("XY").center(x, y).circle(1.7).extrude(HUB_H))   # M4 threaded
    return h


def cylinder(r, z0, z1, x, y):
    return cq.Workplane("XY").workplane(offset=z0).center(x, y).circle(r).extrude(z1 - z0)


def module(side):
    """One corner assembled in the robot frame, mm, |Y| outboard (mirrored by the caller for the right): a dict of
    part name -> shape."""
    ax, ay = AXLE[0] * IN, AXLE[1] * IN
    parts = {}
    parts["bracket"] = bracket(side)
    # Upside down (turned half a turn about X: Y and Z change sign), spline over the axle, body running forward.
    parts["servo"] = servo_envelope().rotate((0, 0, 0), (1, 0, 0), 180).translate((ax - SPLINE_X, ay, SERVO_Z0))
    parts["hub"] = hub_envelope().translate((ax, ay, HUB_Z0))
    parts["wheel"] = flap_wheel().translate((ax, ay, FLAP_Z0 - BOSS_H))
    screws = None
    head_top = FLAP_Z0 - BOSS_H + CB_DEPTH
    for x, y in HUB_HOLES:                                    # M4 x 30 socket head, from below into the hub
        s = cylinder(2.0, head_top, head_top + 30.0, ax + x, ay + y).union(
            cylinder(3.5, head_top - 4.0, head_top, ax + x, ay + y))
        screws = s if screws is None else screws.union(s)
    cx = ax - SPLINE_X
    for sx in (-1, 1):
        for sy in (-1, 1):                                    # M3 x 10, up through the flange into the plate
            s = cylinder(1.5, SERVO_Z0 - FLANGE_T, SERVO_Z0 + 6.0, cx + sx * SERVO_HOLE_L / 2, ay + sy * SERVO_HOLE_W / 2)
            screws = screws.union(s)
    parts["screws"] = screws
    return parts


def export(shape, name):
    for ext in ("step", "stl", "3mf"):
        cq.exporters.export(shape, os.path.join(HERE, f"{name}.{ext}"))


if __name__ == "__main__":
    export(flap_wheel(), "flap_wheel")
    for side in ("left", "right"):
        m = module(side)
        mirror = (lambda s: s.mirror("XZ")) if side == "right" else (lambda s: s)
        b = mirror(m["bracket"])
        bb = b.val().BoundingBox()
        export(b.translate((-bb.xmin, -bb.ymin, -bb.zmin)), f"bracket_{side}")   # plate down, as it prints
        asm = cq.Assembly(name=f"corner_module_{side}")
        colours = {"bracket": "steelblue", "servo": "gray20", "hub": "gray70", "wheel": "forestgreen", "screws": "black"}
        for name, shape in m.items():
            asm.add(mirror(shape), name=name, color=cq.Color(colours[name]))
        asm.export(os.path.join(HERE, f"corner_module_{side}.step"))
        print(f"corner_module_{side}.step; bracket {bb.xlen:.1f} x {bb.ylen:.1f} x {bb.zlen:.1f} mm")
