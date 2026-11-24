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
- The servo, a SWYFT Speed servo in continuous mode (40 x 20 body, 48 x 10 mm flange holes, 25-tooth spline), upside
  down, spline into the hub, its body pointing back. Its flange is near its spline end, so upside down the flange is
  near the bottom (Z 2.56..2.66 in) and the body stands up above it, to Z 3.80 in.
- The bracket: a shelf on the flange (Z 2.66..2.91; four M4 self-tapping up through the flange's 5 mm holes), right
  beside the intake's side channel; a short bar at Z 2.94..3.30 (above a POLLEN's 2.80); a tab flat on the FRONT flange
  of that channel, bolted with two M4 x 12 through its holes at Z 3.81 and 4.44 in, nuts behind (ahead of the Viper
  slide); a wing flat on the channel's outer face whose four bumps sit in the web's holes; and a belly under the
  channel's open bottom, two M4 x 10 up into the Dual Block there. So it wraps the channel on three sides.

The servo and hub in the assemblies are envelopes (the servo checked against the SWYFT model in the robot's STEP to
0.2 mm; the hub drawn from goBILDA's 1908-0025-0032), not the vendors' files: swap theirs in Onshape for the detail.
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
# The servo: a SWYFT Speed servo (mentor's choice, 24 Nov 2026), measured from its model in the robot's STEP (servo frame,
# mm): body X -20..20, Y -10..10; case bottom Z -29.0; flange Z 0..2.5 (its holes 48 x 10 apart, 5 mm: M4); top cap to
# Z 11.2; output boss r 6 to Z 12.7, where the hub seats; spline r 3 at X -10 (10 mm from the end), to Z 16.9. It must
# run in continuous rotation (check SWYFT's programmer) and has a 25-tooth spline for the goBILDA hub.
SERVO_TOP = 11.2
BOSS_TOP = 12.7                       # the hub's spline side seats here
SPLINE_TOP = 16.9
SERVO_BOTTOM = -29.0
FLANGE_T = 2.5
FLANGE_HALF_L = 27.0
SPLINE_X = -10.0
SERVO_Z0 = HUB_Z1 + BOSS_TOP              # 67.61: robot Z of the servo frame's Z 0 (the flange's body-side face)

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
PLATE_Z0 = SERVO_Z0                   # 67.61 (2.66 in): the shelf's underside on the flange
SERVO_CUT_L, SERVO_CUT_W = 40.8, 20.6  # the body passes up through it
SERVO_HOLE_L, SERVO_HOLE_W = 48.0, 10.0
AXLE = (9.22, 5.95)                   # in, |Y|: right in front of the front drive wheel (|Y| 5.44..6.90); the servo
                                      # |Y| 5.56..6.34, 0.26 in inboard of the stick's right arm (|Y| 6.60..7.08)
FLANGE_X = 8.50                       # in: the side channel's front flange, its front face
FLANGE_Y = 5.049                      # |Y| in: the line of that flange's holes (measured in the robot's STEP)
FLANGE_HOLES_Z = (3.806, 4.436)       # in: two of its holes (4 mm, every 8 mm from Z 3.176), 16 mm apart; the nuts clear
                                      # the Dual Block inside the channel (to Z 3.33)
TAB_T = 5.0                           # mm
TAB_Y = (4.90, 5.40)                  # |Y| in: over the whole 12 mm flange (4.895..5.36)
FLANGE_BUMPS_Z = (3.176, 3.491, 4.121)  # in: the flange's other holes under the tab get a bump each (behind them: the
                                      # Dual Block and the nuts' gap; the bumps stop inside the 2.5 mm flange)
TAB_TOP = 4.70                        # in
BAR_X = (8.50, 9.00)                  # in
BAR_Z = (2.94, 3.30)                  # in: its underside 0.14 in above a POLLEN's top (2.80)
SHELF_X = (7.74, 9.97)                # in: around the servo's flange (its tabs X 7.76..9.89)
SHELF_Y = (5.38, 6.52)                # |Y| in: just outboard of the side channel's web (5.36)
WEB_Y = 5.363                         # |Y| in: the side channel's outer face (its web, 2.5 mm thick; the slide is inside)
WING_T = 4.0                          # mm: the wing lies flat on that face, outboard of it
WING_X = (7.74, 8.50)                 # in: back to the front flange's corner; clear of the spinning mecanum
WING_Z = (2.66, 4.75)                 # in (its bottom is the shelf's underside)
WEB_BUMPS = ((7.876, 3.481), (7.875, 4.130), (8.181, 3.491), (8.181, 4.121))   # in: four of the web's holes (X, Z), 4.5 and 4 mm
BUMP_D, BUMP_TIP, BUMP_H = 3.8, 2.4, 1.8  # mm: tapered cones, so they find a hole up to about 0.7 mm off; shorter than the
                                      # channel's wall is thick, so nothing reaches the slide. Sand one off if it won't seat
BELLY_X = (7.62, 8.50)                # in: a tongue under the channel's open bottom, ahead of the frame rail's end (X 7.55)
BELLY_Y = (4.90, 5.40)                # |Y| in: under the channel (4.89..5.36)
BELLY_TOP = 2.86                      # in: the channel's bottom and the Dual Block's bottom face inside it
BLOCK_HOLES_X = (7.867, 8.182)        # in: two of the Dual Block's holes along Z, at |Y| 5.048; the CAD draws 8.182 tapped
BLOCK_HOLE_Y = 5.048                  # (3.3 mm) and 7.867 as 4.2 mm: an M4 x 10 button head goes up into each that threads


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
        for sy in (-1, 1):                               # M4 self-tapping (3.3 mm pilot) up through the flange's 5 mm holes
            shelf = shelf.cut(cq.Workplane("XY").workplane(offset=z0)
                              .center(cx + sx * SERVO_HOLE_L / 2, ay + sy * SERVO_HOLE_W / 2).circle(1.65).extrude(PLATE_T))
    # the bar: from the tab out to the shelf, its top meeting the shelf's top
    bar = cq.Workplane("XY").box((BAR_X[1] - BAR_X[0]) * I, (SHELF_Y[0] + 0.3 - TAB_Y[0]) * I,
                                 (BAR_Z[1] - BAR_Z[0]) * I, centered=False).translate(
        (BAR_X[0] * I, TAB_Y[0] * I, BAR_Z[0] * I))
    step = cq.Workplane("XY").box((BAR_X[1] - BAR_X[0]) * I, 0.3 * I, (BAR_Z[1] * I) - z0, centered=False).translate(
        (BAR_X[0] * I, SHELF_Y[0] * I, z0))              # where the bar comes down onto the shelf
    # the tab, flat on the channel's whole front flange from the belly up, two M4 slots (2 mm up and down, 0.5 mm sideways, for
    # the CAD's error) and bumps into the flange's other holes. Nothing stands in front of it, so both screw heads are clear.
    tab = cq.Workplane("XY").box(TAB_T, (TAB_Y[1] - TAB_Y[0]) * I, TAB_TOP * I - z0, centered=False).translate(
        (FLANGE_X * I, TAB_Y[0] * I, z0))
    for hz in FLANGE_HOLES_Z:
        tab = tab.cut(cq.Workplane("YZ").center(FLANGE_Y * I, hz * I).slot2D(M4 + 4.0, M4 + 1.0, 90).extrude(TAB_T)
                      .translate((FLANGE_X * I, 0, 0)))
    for hz in FLANGE_BUMPS_Z:
        tab = tab.union(cone((FLANGE_X * I, FLANGE_Y * I, hz * I), (-1, 0, 0)))
    # the wing: flat on the channel's outer face, wrapping its front corner to the tab; four bumps sit in the web's
    # 4.5 mm holes, so the bracket cannot turn or slide on the two front screws
    I = IN
    pts = [(WING_X[0] * I, z0), ((FLANGE_X * I) + TAB_T, z0), ((FLANGE_X * I) + TAB_T, WING_Z[1] * I),
           (WING_X[0] * I, WING_Z[1] * I)]
    wing = (cq.Workplane("XZ").polyline(pts).close().extrude(-WING_T).translate((0, WEB_Y * I, 0))
            .edges("|Y").fillet(1.5))
    for bx, bz in WEB_BUMPS:
        wing = wing.union(cone((bx * I, WEB_Y * I, bz * I), (0, -1, 0)))
    # the belly: a tongue under the channel's open bottom, flat on the Dual Block's bottom face, so the bracket wraps the
    # channel on three sides (outer face, front, bottom); M4 up into the block.
    belly = cq.Workplane("XY").box((BELLY_X[1] - BELLY_X[0]) * I, (BELLY_Y[1] - BELLY_Y[0]) * I, BELLY_TOP * I - z0,
                                   centered=False).translate((BELLY_X[0] * I, BELLY_Y[0] * I, z0))
    for hx in BLOCK_HOLES_X:
        belly = belly.cut(cylinder(M4 / 2, z0, BELLY_TOP * I, hx * I, BLOCK_HOLE_Y * I))
    return shelf.union(bar).union(step).union(tab).union(wing).union(belly)


def servo_envelope():
    """The servo in its own frame (SWYFT Speed): case bottom Z -29.0, flange Z 0..2.5, top cap to 11.2, output boss to
    12.7, spline at X -10 to 16.9."""
    body = cq.Workplane("XY").box(40, 20, -SERVO_BOTTOM, centered=(True, True, False)).translate((0, 0, SERVO_BOTTOM))
    flange = cq.Workplane("XY").box(2 * FLANGE_HALF_L, 20, FLANGE_T, centered=(True, True, False))
    for sx in (-1, 1):
        for sy in (-1, 1):
            flange = flange.cut(cq.Workplane("XY").center(sx * SERVO_HOLE_L / 2, sy * SERVO_HOLE_W / 2)
                                .circle(2.5).extrude(FLANGE_T))
    cap = cq.Workplane("XY").box(40, 20, SERVO_TOP - FLANGE_T, centered=(True, True, False)).translate((0, 0, FLANGE_T))
    boss = cq.Workplane("XY").center(SPLINE_X, 0).circle(6.0).extrude(BOSS_TOP - SERVO_TOP).translate((0, 0, SERVO_TOP))
    spline = cq.Workplane("XY").center(SPLINE_X, 0).circle(3.0).extrude(SPLINE_TOP - BOSS_TOP).translate((0, 0, BOSS_TOP))
    return body.union(flange).union(cap).union(boss).union(spline)


def hub_envelope():
    """The goBILDA 1908 hub in its own frame: Z 0 its counterbored face (toward the wheel), 8 mm tall, 32 mm across."""
    h = cq.Workplane("XY").circle(16.0).circle(2.5).extrude(HUB_H)
    h = h.cut(cq.Workplane("XY").circle(8.0).extrude(3.0))   # the servo screw's counterbore
    for x, y in HUB_HOLES:
        h = h.cut(cq.Workplane("XY").center(x, y).circle(1.7).extrude(HUB_H))   # M4 threaded
    return h


def cone(base, direction):
    """A locating bump: a cone from BUMP_D at the bracket's face to BUMP_TIP, BUMP_H into a hole."""
    return cq.Workplane("XY").add(cq.Solid.makeCone(BUMP_D / 2, BUMP_TIP / 2, BUMP_H, cq.Vector(*base),
                                                    cq.Vector(*direction)))


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
        for sy in (-1, 1):                                    # M4 x 10, up through the flange into the shelf
            s = cylinder(2.0, SERVO_Z0 - FLANGE_T, SERVO_Z0 + 6.0, cx + sx * SERVO_HOLE_L / 2, ay + sy * SERVO_HOLE_W / 2)
            screws = screws.union(s)
    for hz in FLANGE_HOLES_Z:                                 # M4 x 12 through the tab and the flange, nut behind
        s = (cq.Workplane("YZ").center(FLANGE_Y * IN, hz * IN).circle(2.0).extrude(12.0)
             .translate((FLANGE_X * IN + TAB_T - 12.0, 0, 0)))
        screws = screws.union(s)
    for hx in BLOCK_HOLES_X:                                  # M4 x 10 button head, up through the belly into the block
        s = cylinder(2.0, PLATE_Z0, PLATE_Z0 + 10.0, hx * IN, BLOCK_HOLE_Y * IN).union(
            cylinder(3.8, PLATE_Z0 - 2.2, PLATE_Z0, hx * IN, BLOCK_HOLE_Y * IN))
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


def to_onshape(shape, z_up=False):
    """A shape in the robot frame (mm) moved into the Robot assembly's frame: as the mentor's STEP export has it (-Y up),
    or, with z_up, as Onshape's API reports the assembly (-Z up, +Y forward; cad/advantagescope/from_onshape.py), which
    is the same frame turned a quarter turn about X."""
    from OCP.BRepBuilderAPI import BRepBuilderAPI_Transform
    from OCP.gp import gp_Trsf
    x0, y0, z0 = ONSHAPE_AXLES_MID
    t = gp_Trsf()
    if z_up:
        t.SetValues(0.0, 1.0, 0.0, x0,
                    1.0, 0.0, 0.0, -z0,
                    0.0, 0.0, -1.0, y0 + 48.0)
    else:
        t.SetValues(0.0, 1.0, 0.0, x0,
                    0.0, 0.0, -1.0, y0 + 48.0,
                    -1.0, 0.0, 0.0, z0)
    return cq.Shape.cast(BRepBuilderAPI_Transform(shape.wrapped, t, True).Shape())


if __name__ == "__main__":
    export(flap_wheel(), "flap_wheel")
    colours = {"bracket": "steelblue", "servo": "gray20", "hub": "gray70", "wheel": "forestgreen", "screws": "black"}
    both = cq.Assembly(name="Corner modules")
    both_z = cq.Assembly(name="Corner modules")
    for side in ("left", "right"):
        m = module(side)
        mirror = (lambda s: s.mirror("XZ")) if side == "right" else (lambda s: s)
        b = mirror(m["bracket"])
        bb = b.val().BoundingBox()
        export(b.translate((-bb.xmin, -bb.ymin, -bb.zmin)), f"bracket_{side}")   # plate down, as it prints
        asm = cq.Assembly(name=f"corner_module_{side}")
        sub = cq.Assembly(name=f"Corner module {side}")
        sub_z = cq.Assembly(name=f"Corner module {side}")
        for name, shape in m.items():
            asm.add(mirror(shape), name=name, color=cq.Color(colours[name]))
            sub.add(to_onshape(mirror(shape).val()), name=f"{name} {side}", color=cq.Color(colours[name]))
            sub_z.add(to_onshape(mirror(shape).val(), True), name=f"{name} {side}", color=cq.Color(colours[name]))
        asm.export(os.path.join(HERE, f"corner_module_{side}.step"))
        both.add(sub, name=f"Corner module {side}")
        both_z.add(sub_z, name=f"Corner module {side}")
        print(f"corner_module_{side}.step; bracket {bb.xlen:.1f} x {bb.ylen:.1f} x {bb.zlen:.1f} mm")
    # Both corners in the Onshape Robot assembly's own frame: insert at its origin and they land in place.
    both.export(os.path.join(HERE, "corner_modules_onshape.step"))
    both_z.export(os.path.join(HERE, "corner_modules_onshape_zup.step"))
    print("corner_modules_onshape.step, corner_modules_onshape_zup.step")
