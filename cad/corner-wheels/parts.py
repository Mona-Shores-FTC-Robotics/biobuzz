"""Corner wheels, first prototype (mentor's idea, 23 Nov 2026): a TPU flap wheel on a vertical axle at each front
corner, spun inward by a continuous-rotation servo, and the printed bracket that hangs the servo there.

    pip install cadquery
    python3 cad/corner-wheels/parts.py      # writes flap_wheel.*, bracket_left.* and bracket_right.* next to this file

Where it goes (doc/corner-wheels.md, from the AdvantageScope model of the Onshape CAD, 31 Oct 2026; robot frame:
+X forward, +Y left, +Z up, inches, origin on the floor under the middle of the drive axles):

- Wheel axle at X 8.94, Y +6.85 (left) / -6.95 (right). The wheel is 2.5 in across and spans Z 0.60..1.65, so a
  POLLEN's equator (Z 1.40) meets it. Its rim reaches X 10.19, 2.62 in ahead of the front drive wheels, and
  0.47 in (left) / 0.43 in (right) past the frame's sides.
- Each bracket holds the servo upside down under a plate at Z 3.40..3.65, output spline down. The left one bolts to
  the outer face of the intake's side channel (the 5-hole Lowside U-Channel, Y +5.35) ahead of the front drive wheel;
  the right one to the front face of the short channel ahead of the right drive wheel (X 7.87).
- The plate's inner part (over the FLOWER stick arm's band) stops at X 8.50; only the part outboard of the arm reaches
  forward to the servo. Through the stick's whole 0..162 deg swing the CAD leaves at least
  0.24 in between the arm and the servo, the bracket or the wheel.

Millimetres. Each part's own frame is given where it is built. Sizes are constants at the top: check the servo and the
hub against the parts in hand before printing.
"""
import math
import os

import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))
IN = 25.4

# ---- the wheel -----------------------------------------------------------------------------------------------------
WHEEL_OD = 2.5 * IN          # 63.5: the size that fits (2 in fits with room; 3 in hits the stick and the 18 in cube)
FLAP_H = (1.65 - 0.60) * IN  # 26.7: Z 0.60..1.65 in, the POLLEN's equator inside it
HUB_OD = 30.0
FLAPS = 8
FLAP_T = 2.2                 # thin enough to fold under a ball pinned on a wall, thick enough to throw it
FLAP_SWEEP_DEG = 35          # each flap curves back against the spin, so it wipes rather than stabs
BOSS_H = 5.0                 # above the flaps, toward the servo: the hub bolts to its top face
BORE = 8.3                   # clears an 8 mm shaft or a servo hub's boss
M4 = 4.4
# Two goBILDA hub patterns, so it fits either: 4 holes on a 16 mm circle, and 4 on a 16 mm square.
HOLES = [(8 * math.cos(math.radians(a)), 8 * math.sin(math.radians(a))) for a in (45, 135, 225, 315)] + \
        [(sx * 8, sy * 8) for sx in (-1, 1) for sy in (-1, 1)]


def flap_wheel():
    """Frame: axle on Z, Z = 0 the bottom of the flaps (0.60 in off the tile on the robot), the boss on top.
    It prints boss down. This is the left wheel: seen from above it turns clockwise (its front moving inward, to -Y),
    and each flap's tip trails its root. The right wheel is its mirror image."""
    hub = cq.Workplane("XY").circle(HUB_OD / 2).circle(BORE / 2).extrude(FLAP_H + BOSS_H)
    r0, r1 = HUB_OD / 2 - 1.0, WHEEL_OD / 2
    flaps = None
    for k in range(FLAPS):
        a0 = 2 * math.pi * k / FLAPS
        pts_out, pts_in = [], []
        for i in range(9):
            t = i / 8
            r = r0 + (r1 - r0) * t
            a = a0 + math.radians(FLAP_SWEEP_DEG) * t * t   # a gentle curve, steepest at the tip
            nx, ny = -math.sin(a), math.cos(a)
            pts_out.append((r * math.cos(a) + nx * FLAP_T / 2, r * math.sin(a) + ny * FLAP_T / 2))
            pts_in.append((r * math.cos(a) - nx * FLAP_T / 2, r * math.sin(a) - ny * FLAP_T / 2))
        f = cq.Workplane("XY").polyline(pts_out + pts_in[::-1]).close().extrude(FLAP_H)
        flaps = f if flaps is None else flaps.union(f)
    w = hub.union(flaps)
    for x, y in HOLES:
        w = w.cut(cq.Workplane("XY").workplane(offset=FLAP_H + BOSS_H - 10).center(x, y).circle(M4 / 2).extrude(10))
    return w


# ---- the brackets ----------------------------------------------------------------------------------------------------
# Servo: a standard-size servo (goBILDA 2000 series, REV Smart Robot Servo, Axon): body 40.0 x 20.0, flange 54 long with
# holes 48 x 10, output spline on the body's centreline 10 mm from one end.
SERVO_L, SERVO_W = 40.6, 20.4                 # the cut-out the body drops through
SERVO_HOLE_L, SERVO_HOLE_W = 48.0, 10.0
SPLINE_FROM_END = 10.0
PLATE_T = 0.25 * IN                           # 6.35: Z 3.40..3.65 in
PLATE_Z0 = 3.40
EAR_T = 4.0

# Robot-frame numbers, inches, Y taken outboard (the right side's -Y). The two corners differ in the CAD: the right
# stick arm is 0.45 in further out (|Y| 5.84..6.32, the left 5.40..5.88), and a short 2-hole channel stands in front of
# the right drive wheel (X 7.39..7.87, |Y| 5.36..7.25, Z 1.73..4.42), so the right servo runs forward from its spline
# and the right bracket bolts to that channel's front face.
SIDES = {
    "left": dict(axle=(8.94, 6.85), band_y=6.15, plate_x0=7.30, inner_x1=8.50, plate_x1=9.80, plate_y1=7.45,
                 body="back", ear="side", ear_x=(7.60, 8.50)),
    "right": dict(axle=(8.94, 6.95), band_y=6.55, plate_x0=7.92, inner_x1=8.50, plate_x1=10.38, plate_y1=7.50,
                  body="forward", ear="front", ear_y=(5.55, 7.15)),
}
FACE_Y = 5.35                                 # the intake side channel's outer face
FRONT_FACE_X = 7.87                           # the right 2-hole channel's front face
EAR_Z0 = 2.95


def bracket(side):
    """Frame: robot inches converted to mm, origin at (X plate_x0, |Y| 5.35, Z 3.40): +X forward, +Y outboard, +Z up.
    The plate's underside is Z = 0 and the servo hangs under it, spline down at the axle. The left ear hangs down
    against the intake side channel's outer face (Y = 0); the right ear hangs down against the front face of the
    channel ahead of the right drive wheel. The right part is exported mirrored, so it is in the robot's own frame.
    Prints plate down, ear up, no supports."""
    c = SIDES[side]
    x0 = c["plate_x0"]

    def p(x, y):
        return ((x - x0) * IN, (y - FACE_Y) * IN)
    outline = [p(x0, FACE_Y), p(c["inner_x1"], FACE_Y), p(c["inner_x1"], c["band_y"]), p(c["plate_x1"], c["band_y"]),
               p(c["plate_x1"], c["plate_y1"]), p(x0, c["plate_y1"])]
    b = cq.Workplane("XY").polyline(outline).close().extrude(PLATE_T).edges("|Z").fillet(2.0)
    ax, ay = p(*c["axle"])
    off = SERVO_L / 2 - SPLINE_FROM_END
    cx = ax - off if c["body"] == "back" else ax + off
    b = b.cut(cq.Workplane("XY").center(cx, ay).rect(SERVO_L, SERVO_W).extrude(PLATE_T))
    for sx in (-1, 1):
        for sy in (-1, 1):
            b = b.cut(cq.Workplane("XY").center(cx + sx * SERVO_HOLE_L / 2, ay + sy * SERVO_HOLE_W / 2)
                      .circle(1.6).extrude(PLATE_T))                # M3 self-tapping, or drill 3.4 for bolts
    ear_h = (PLATE_Z0 - EAR_Z0) * IN + PLATE_T
    if c["ear"] == "side":
        ex0, ex1 = (c["ear_x"][0] - x0) * IN, (c["ear_x"][1] - x0) * IN
        w, mid = ex1 - ex0, (ex0 + ex1) / 2
        ear = cq.Workplane("XZ").center(mid, PLATE_T - ear_h / 2).rect(w, ear_h).extrude(-EAR_T)
        for d in (-8.0, 8.0):                                         # two M4 slots 16 mm apart, 8 mm of travel
            ear = ear.cut(cq.Workplane("XZ").center(mid + d, PLATE_T - ear_h / 2 + 1).slot2D(8 + M4, M4, 90)
                          .extrude(-EAR_T))
        gus = (cq.Workplane("YZ").polyline([(EAR_T, 0), (EAR_T + 8, 0), (EAR_T, -8)]).close().extrude(w)
               .translate((ex0, 0, 0)))
    else:
        fx = (FRONT_FACE_X - x0) * IN + 0.3                           # 0.3 mm proud of the face: a shim closes it
        ey0, ey1 = (c["ear_y"][0] - FACE_Y) * IN, (c["ear_y"][1] - FACE_Y) * IN
        w, mid = ey1 - ey0, (ey0 + ey1) / 2
        ear = (cq.Workplane("YZ").center(mid, PLATE_T - ear_h / 2).rect(w, ear_h).extrude(EAR_T)
               .translate((fx, 0, 0)))
        for d in (-8.0, 8.0):
            ear = ear.cut(cq.Workplane("YZ").center(mid + d, PLATE_T - ear_h / 2 + 1).slot2D(8 + M4, M4, 90)
                          .extrude(EAR_T).translate((fx, 0, 0)))
        gus = (cq.Workplane("XZ").polyline([(fx + EAR_T, 0), (fx + EAR_T + 8, 0), (fx + EAR_T, -8)]).close()
               .extrude(-w).translate((0, ey0, 0)))
    return b.union(ear).union(gus)


def placed(side):
    """The bracket in the robot frame (inches), for the clearance check and the drawings."""
    c = SIDES[side]
    s = 1 if side == "left" else -1
    return c, (c["plate_x0"], s * FACE_Y, PLATE_Z0), s


def export(shape, name):
    for ext in ("step", "stl", "3mf"):
        cq.exporters.export(shape, os.path.join(HERE, f"{name}.{ext}"))


if __name__ == "__main__":
    w = flap_wheel()
    export(w, "flap_wheel_left")              # spins clockwise seen from above; its flaps trail
    export(w.mirror("XZ"), "flap_wheel_right")  # spins the other way, so its flaps curve the other way
    for side in ("left", "right"):
        b = bracket(side)
        export(b if side == "left" else b.mirror("XZ"), f"bracket_{side}")
        bb = b.val().BoundingBox()
        print(f"bracket_{side}: {bb.xlen:.1f} x {bb.ylen:.1f} x {bb.zlen:.1f} mm, {b.val().Volume() / 1000:.1f} cm3")
    bb = w.val().BoundingBox()
    print(f"flap_wheel: {bb.xlen:.1f} x {bb.ylen:.1f} x {bb.zlen:.1f} mm, {w.val().Volume() / 1000:.1f} cm3")
