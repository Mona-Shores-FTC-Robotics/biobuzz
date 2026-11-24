"""Corner wheels, prototype 2 (mentor's idea, 23 Nov 2026; servo fixed 24 Nov 2026): a TPU flap wheel on a vertical
axle at each front corner, turned inward by a continuous-rotation servo, and the printed bracket that holds the servo.

    pip install cadquery
    python3 cad/corner-wheels/parts.py

Writes, next to this file:
  flap_wheel_left.* / flap_wheel_right.*   the wheels, boss down as they print
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
SCREW_GRIP = 12.0            # wheel material each M4 x 16 clamps under its head: 4 mm into the hub's thread
HUB_HOLES = [(8 * math.cos(math.radians(a)), 8 * math.sin(math.radians(a))) for a in (45, 135, 225, 315)]

# ---- the brackets -------------------------------------------------------------------------------------------------
PLATE_T = 0.25 * IN                   # 6.35
PLATE_Z0 = SERVO_Z0                   # 68.21 (2.69 in): the plate's underside on the flange
SERVO_CUT_L, SERVO_CUT_W = 40.8, 20.6  # the body passes up through it
SERVO_HOLE_L, SERVO_HOLE_W = 48.0, 10.0
EAR_T = 4.0
EAR_TOP = 3.65 * IN                   # the left ear stays under the stick arm's swing over its band (Z 3.7 at X 8.5)
FACE_Y = 5.35                         # the intake side channel's outer face, |Y| in
FRONT_FACE_X = 7.87                   # the right short channel's front face, X in
# |Y| outboard; the right side is mirrored at the end. The right stick arm is 0.45 in further out than the left.
SIDES = {
    "left": dict(axle=(8.94, 6.85), band_y=6.15, plate_x0=7.60, inner_x1=8.50, plate_x1=10.38, plate_y1=7.45,
                 ear="side", ear_x=(7.60, 8.50)),
    "right": dict(axle=(8.94, 6.95), band_y=6.55, plate_x0=7.92, inner_x1=8.50, plate_x1=10.38, plate_y1=7.50,
                  ear="front", ear_y=(5.55, 7.15)),
}


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
    """The left wheel in its own frame: axle on Z, Z 0 the flaps' bottom, the boss on top. Seen from above it turns
    clockwise (its front moving inward) and each flap's tip trails its root; the right wheel is its mirror image."""
    total = FLAP_H + BOSS_H
    w = cq.Workplane("XY").circle(HUB_RING_OD / 2).circle(BORE / 2).extrude(total).union(_flaps())
    cb_depth = total - SCREW_GRIP
    for x, y in HUB_HOLES:
        w = w.cut(cq.Workplane("XY").center(x, y).circle(M4 / 2).extrude(total))
        w = w.cut(cq.Workplane("XY").center(x, y).circle(M4_HEAD_CB / 2).extrude(cb_depth))
    return w


def bracket(side):
    """The bracket in the robot frame (mm, |Y| outboard: the right one is mirrored by the caller)."""
    c = SIDES[side]
    x0 = c["plate_x0"]
    P = lambda x, y: (x * IN, y * IN)  # noqa: E731
    outline = [P(x0, FACE_Y), P(c["inner_x1"], FACE_Y), P(c["inner_x1"], c["band_y"]), P(c["plate_x1"], c["band_y"]),
               P(c["plate_x1"], c["plate_y1"]), P(x0, c["plate_y1"])]
    b = (cq.Workplane("XY").workplane(offset=PLATE_Z0).polyline(outline).close().extrude(PLATE_T)
         .edges("|Z").fillet(2.0))
    ax, ay = c["axle"][0] * IN, c["axle"][1] * IN
    cx = ax - SPLINE_X                                   # the body's middle, forward of the spline
    b = b.cut(cq.Workplane("XY").workplane(offset=PLATE_Z0).center(cx, ay).rect(SERVO_CUT_L, SERVO_CUT_W)
              .extrude(PLATE_T))
    for sx in (-1, 1):
        for sy in (-1, 1):                               # M3 self-tapping up through the flange's slots
            b = b.cut(cq.Workplane("XY").workplane(offset=PLATE_Z0).center(cx + sx * SERVO_HOLE_L / 2,
                                                                          ay + sy * SERVO_HOLE_W / 2)
                      .circle(1.25).extrude(PLATE_T))
    ear_h = EAR_TOP - PLATE_Z0
    if c["ear"] == "side":
        ex0, ex1 = c["ear_x"][0] * IN, c["ear_x"][1] * IN
        mid, w = (ex0 + ex1) / 2, ex1 - ex0
        y0 = FACE_Y * IN
        ear = cq.Workplane("XY").box(w, EAR_T, ear_h, centered=False).translate((ex0, y0, PLATE_Z0))
        for d in (-8.0, 8.0):                            # M4 slots 16 mm apart, 8 mm of travel up and down
            slot = (cq.Workplane("XZ").center(mid + d, PLATE_Z0 + PLATE_T + (ear_h - PLATE_T) / 2)
                    .slot2D(8 + M4, M4, 90).extrude(-EAR_T).translate((0, y0, 0)))
            ear = ear.cut(slot)
        gus = (cq.Workplane("YZ").polyline([(y0 + EAR_T, PLATE_Z0 + PLATE_T), (y0 + EAR_T + 8, PLATE_Z0 + PLATE_T),
                                            (y0 + EAR_T, PLATE_Z0 + PLATE_T + 8)]).close().extrude(w)
               .translate((ex0, 0, 0)))
    else:
        fx = FRONT_FACE_X * IN + 0.3                     # 0.3 mm proud of the face: a washer closes it
        ey0, ey1 = c["ear_y"][0] * IN, c["ear_y"][1] * IN
        mid, w = (ey0 + ey1) / 2, ey1 - ey0
        ear = cq.Workplane("XY").box(EAR_T, w, ear_h, centered=False).translate((fx, ey0, PLATE_Z0))
        for d in (-8.0, 8.0):
            slot = (cq.Workplane("YZ").center(mid + d, PLATE_Z0 + PLATE_T + (ear_h - PLATE_T) / 2)
                    .slot2D(8 + M4, M4, 90).extrude(EAR_T).translate((fx, 0, 0)))
            ear = ear.cut(slot)
        gus = (cq.Workplane("XZ").polyline([(fx + EAR_T, PLATE_Z0 + PLATE_T), (fx + EAR_T + 8, PLATE_Z0 + PLATE_T),
                                            (fx + EAR_T, PLATE_Z0 + PLATE_T + 8)]).close().extrude(-w)
               .translate((0, ey0, 0)))
    return b.union(ear).union(gus)


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
    c = SIDES[side]
    ax, ay = c["axle"][0] * IN, c["axle"][1] * IN
    parts = {}
    parts["bracket"] = bracket(side)
    # Upside down (turned half a turn about X: Y and Z change sign), spline over the axle, body running forward.
    parts["servo"] = servo_envelope().rotate((0, 0, 0), (1, 0, 0), 180).translate((ax - SPLINE_X, ay, SERVO_Z0))
    parts["hub"] = hub_envelope().translate((ax, ay, HUB_Z0))
    parts["wheel"] = flap_wheel().translate((ax, ay, FLAP_Z0))
    screws = None
    head_top = FLAP_Z0 + FLAP_H + BOSS_H - SCREW_GRIP
    for x, y in HUB_HOLES:                                    # M4 x 16 socket head, from below into the hub
        s = cylinder(2.0, head_top, head_top + 16.0, ax + x, ay + y).union(
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
    w = flap_wheel().rotate((0, 0, 0), (1, 0, 0), 180)          # boss down, as it prints
    export(w, "flap_wheel_left")
    export(w.mirror("XZ"), "flap_wheel_right")
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
