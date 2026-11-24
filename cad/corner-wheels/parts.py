"""Corner wheels, prototype 3 (mentor's idea, 23 Nov 2026; fitted to the robot's own STEP, 24 Nov 2026): a TPU flap wheel
on a vertical axle at each front corner, turned inward by a continuous-rotation servo, and the printed bracket that
holds the servo.

    pip install cadquery
    python3 cad/corner-wheels/parts.py

Writes, next to this file:
  flap_wheel.*                             the wheel, one file for both corners (the right one goes in upside down)
  bracket_left.* / bracket_right.*         the brackets (mirror images)
  corner_module_left.step / _right.step    each corner assembled in the robot frame: bracket, servo, hub, wheel, screws

Robot frame (cad/advantagescope/README.md), millimetres in the files, inches in the comments: +X forward, +Y left,
+Z up, origin on the floor under the middle of the four drive axles. Built for the right corner and mirrored for the
left (the left FLOWER-stick arm assumed moved out to match the right).

How a corner goes together, from the floor up (cad/corner-wheels/README.md has the clearances):
- The wheel: flaps Z 0.60..1.65 in (a POLLEN's middle, Z 1.40, inside them), bosses 0.40..1.85, axle at X 9.22,
  |Y| 5.95, right in front of the front drive wheel.
- A goBILDA 1908 Series Servo Hub (25 Tooth Spline, 32mm Diameter), 8 mm, on the boss: four M4 x 30 socket head
  screws come up through the wheel from counterbores in its underside into the hub's threaded holes on the 16 mm
  circle.
- The servo, a goBILDA 2000 Series Dual Mode Servo (or any standard servo with the same 40 x 20 body and 48 x 10 mm
  flange holes), upside down, spline into the hub, its body pointing back. Its flange is near its spline end, so
  upside down the flange is near the bottom (Z 2.53..2.69 in) and the body stands up above it, to Z 3.76 in.
- The bracket: a shelf on the flange (Z 2.69..2.94; four M3 up through the flange's slots), right beside the intake's
  side channel, a short bar at Z 2.94..3.30 (above a POLLEN's 2.80), and a tab flat on the FRONT flange of that channel,
  bolted with two M4 x 12 through its holes at Z 3.81 and 4.44 in, nuts behind (ahead of the Viper slide).

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
WHEEL_OD = 2.3 * IN          # 58.4: its front X 10.35 stays inside the 18 in cube (X 10.43 from the back wall) with the
                             # axle far enough forward for the servo to clear the spinning mecanum by 0.25 in
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
# From the mentor's robot STEP of 24 Nov 2026 (both front corners as built). One printed part per corner. A tab lies flat
# on the FRONT flange of the intake's side channel (the 5 Hole Lowside U-Channel the intake's Viper slide runs inside)
# and bolts through two of its holes, nuts behind in the gap ahead of the slide. A short bar from the tab, just above a
# POLLEN's top, joins a shelf on the servo's flange, right beside the channel. The servo sits right in front of the
# front drive wheel and inboard of the FLOWER stick's arm, spline down, body pointing back; the wheel spins under it.
PLATE_T = 0.25 * IN                   # the shelf, 6.35 mm
PLATE_Z0 = SERVO_Z0                   # 68.21 (2.69 in): the shelf's underside on the flange
SERVO_CUT_L, SERVO_CUT_W = 40.8, 20.6  # the body passes up through it
SERVO_HOLE_L, SERVO_HOLE_W = 48.0, 10.0
AXLE = (9.22, 5.95)                   # in, |Y|: right in front of the front drive wheel (|Y| 5.44..6.90); the servo
                                      # |Y| 5.56..6.34, 0.26 in inboard of the stick's right arm (|Y| 6.60..7.08)
FLANGE_X = 8.50                       # in: the side channel's front flange, its front face
FLANGE_Y = 5.12                       # |Y| in: the middle of that flange, where its holes are
FLANGE_HOLES_Z = (3.806, 4.436)       # in: two of its holes (4 mm, every 8 mm from Z 3.176), 16 mm apart; the nuts clear
                                      # the Dual Block inside the channel (to Z 3.33)
TAB_T = 4.0                           # mm
TAB_Y = (4.95, 5.40)                  # |Y| in: over the 12 mm flange (4.89..5.36)
TAB_TOP = 4.70                        # in
BAR_X = (8.50, 9.00)                  # in
BAR_Z = (2.94, 3.30)                  # in: its underside 0.14 in above a POLLEN's top (2.80)
SHELF_X = (7.74, 9.97)                # in: around the servo's flange (its tabs X 7.76..9.89)
SHELF_Y = (5.38, 6.52)                # |Y| in: just outboard of the side channel's web (5.36)


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


def bracket(side):
    """The bracket in the robot frame (mm), |Y| outboard as +Y (the left side as built; the caller mirrors the right)."""
    I = IN
    z0 = PLATE_Z0
    shelf = (cq.Workplane("XY").workplane(offset=z0)
             .center((SHELF_X[0] + SHELF_X[1]) / 2 * I, (SHELF_Y[0] + SHELF_Y[1]) / 2 * I)
             .rect((SHELF_X[1] - SHELF_X[0]) * I, (SHELF_Y[1] - SHELF_Y[0]) * I).extrude(PLATE_T)
             .edges("|Z").fillet(2.0))
    ax, ay = AXLE[0] * I, AXLE[1] * I
    cx = ax + SPLINE_X                                   # the servo body's middle, behind the spline
    shelf = shelf.cut(cq.Workplane("XY").workplane(offset=z0).center(cx, ay).rect(SERVO_CUT_L, SERVO_CUT_W)
                      .extrude(PLATE_T))
    for sx in (-1, 1):
        for sy in (-1, 1):                               # M3 self-tapping up through the flange's slots
            shelf = shelf.cut(cq.Workplane("XY").workplane(offset=z0)
                              .center(cx + sx * SERVO_HOLE_L / 2, ay + sy * SERVO_HOLE_W / 2).circle(1.25).extrude(PLATE_T))
    # the bar: from the tab out to the shelf, its top meeting the shelf's top
    bar = cq.Workplane("XY").box((BAR_X[1] - BAR_X[0]) * I, (SHELF_Y[0] + 0.3 - TAB_Y[0]) * I,
                                 (BAR_Z[1] - BAR_Z[0]) * I, centered=False).translate(
        (BAR_X[0] * I, TAB_Y[0] * I, BAR_Z[0] * I))
    step = cq.Workplane("XY").box((BAR_X[1] - BAR_X[0]) * I, 0.3 * I, (BAR_Z[1] * I) - z0, centered=False).translate(
        (BAR_X[0] * I, SHELF_Y[0] * I, z0))              # where the bar comes down onto the shelf
    # the tab, flat on the channel's front flange, two M4 holes (slotted 2 mm up and down for the CAD's error)
    tab = cq.Workplane("XY").box(TAB_T, (TAB_Y[1] - TAB_Y[0]) * I, (TAB_TOP - BAR_Z[0]) * I, centered=False).translate(
        (FLANGE_X * I, TAB_Y[0] * I, BAR_Z[0] * I))
    for hz in FLANGE_HOLES_Z:
        tab = tab.cut(cq.Workplane("YZ").center(FLANGE_Y * I, hz * I).slot2D(M4 + 4.0, M4, 90).extrude(TAB_T)
                      .translate((FLANGE_X * I, 0, 0)))
    # a gusset behind the tab's upper part, on top of the bar, clear of the holes' nuts (they are on the other side)
    gz, gx = BAR_Z[1] * I, FLANGE_X * I + TAB_T
    gusset = (cq.Workplane("XZ").polyline([(gx, gz), (gx + 9.0, gz), (gx, gz + 14.0)]).close()
              .extrude(-(TAB_Y[1] - TAB_Y[0]) * I).translate((0, TAB_Y[0] * I, 0)))
    return shelf.union(bar).union(step).union(tab).union(gusset)


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
    """One corner assembled in the robot frame, mm, |Y| outboard (the caller mirrors the right): part name -> shape."""
    ax, ay = AXLE[0] * IN, AXLE[1] * IN
    parts = {}
    parts["bracket"] = bracket(side)
    # Upside down (half a turn about X), then half a turn about Z so the body points back; spline over the axle.
    parts["servo"] = (servo_envelope().rotate((0, 0, 0), (1, 0, 0), 180).rotate((0, 0, 0), (0, 0, 1), 180)
                      .translate((ax + SPLINE_X, ay, SERVO_Z0)))
    parts["hub"] = hub_envelope().translate((ax, ay, HUB_Z0))
    parts["wheel"] = flap_wheel().translate((ax, ay, FLAP_Z0 - BOSS_H))
    screws = None
    head_top = FLAP_Z0 - BOSS_H + CB_DEPTH
    for x, y in HUB_HOLES:                                    # M4 x 30 socket head, from below into the hub
        s = cylinder(2.0, head_top, head_top + 30.0, ax + x, ay + y).union(
            cylinder(3.5, head_top - 4.0, head_top, ax + x, ay + y))
        screws = s if screws is None else screws.union(s)
    cx = ax + SPLINE_X
    for sx in (-1, 1):
        for sy in (-1, 1):                                    # M3 x 10, up through the flange into the shelf
            s = cylinder(1.5, SERVO_Z0 - FLANGE_T, SERVO_Z0 + 6.0, cx + sx * SERVO_HOLE_L / 2, ay + sy * SERVO_HOLE_W / 2)
            screws = screws.union(s)
    for hz in FLANGE_HOLES_Z:                                 # M4 x 12 through the tab and the flange, nut behind
        s = (cq.Workplane("YZ").center(FLANGE_Y * IN, hz * IN).circle(2.0).extrude(12.0)
             .translate((FLANGE_X * IN + TAB_T - 12.0, 0, 0)))
        screws = screws.union(s)
    parts["screws"] = screws
    return parts


def export(shape, name):
    for ext in ("step", "stl", "3mf"):
        cq.exporters.export(shape, os.path.join(HERE, f"{name}.{ext}"))


# The mentor's Onshape Robot assembly frame, from his STEP export of 24 Nov 2026 (mm): its X runs along the drive axles
# (robot +Y), its -Y is up and its -Z forward. The middle of the four drive axles is at ONSHAPE_AXLES_MID and the floor
# 48 mm below them (a 96 mm mecanum), so robot (X, Y, Z) mm -> Onshape (Y + x0, y0 + 48 - Z, z0 - X).
ONSHAPE_AXLES_MID = (21.7132, 219.5608, 343.5960)


def to_onshape(shape):
    """A shape in the robot frame (mm) moved into the Onshape Robot assembly's own frame."""
    from OCP.BRepBuilderAPI import BRepBuilderAPI_Transform
    from OCP.gp import gp_Trsf
    x0, y0, z0 = ONSHAPE_AXLES_MID
    t = gp_Trsf()
    t.SetValues(0.0, 1.0, 0.0, x0,
                0.0, 0.0, -1.0, y0 + 48.0,
                -1.0, 0.0, 0.0, z0)
    return cq.Shape.cast(BRepBuilderAPI_Transform(shape.wrapped, t, True).Shape())


if __name__ == "__main__":
    export(flap_wheel(), "flap_wheel")
    colours = {"bracket": "steelblue", "servo": "gray20", "hub": "gray70", "wheel": "forestgreen", "screws": "black"}
    both = cq.Assembly(name="Corner modules")
    for side in ("left", "right"):
        m = module(side)
        mirror = (lambda s: s.mirror("XZ")) if side == "right" else (lambda s: s)
        b = mirror(m["bracket"])
        bb = b.val().BoundingBox()
        export(b.translate((-bb.xmin, -bb.ymin, -bb.zmin)), f"bracket_{side}")   # plate down, as it prints
        asm = cq.Assembly(name=f"corner_module_{side}")
        sub = cq.Assembly(name=f"Corner module {side}")
        for name, shape in m.items():
            asm.add(mirror(shape), name=name, color=cq.Color(colours[name]))
            sub.add(to_onshape(mirror(shape).val()), name=f"{name} {side}", color=cq.Color(colours[name]))
        asm.export(os.path.join(HERE, f"corner_module_{side}.step"))
        both.add(sub, name=f"Corner module {side}")
        print(f"corner_module_{side}.step; bracket {bb.xlen:.1f} x {bb.ylen:.1f} x {bb.zlen:.1f} mm")
    # Both corners in the Onshape Robot assembly's own frame: insert at its origin and they land in place.
    both.export(os.path.join(HERE, "corner_modules_onshape.step"))
    print("corner_modules_onshape.step")
