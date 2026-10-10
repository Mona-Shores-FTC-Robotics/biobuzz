"""Front flaps for the mentor's robot as he drew it on 9 Oct: a foam-faced flap each side of his intake, hinged in front
of his front uprights (his 5-hole low-side channels). Folded across the face at START, held by a servo latch (the robot
stays 16.8 in long, his corner wheel being the front-most part; R102); after START the servo lets go and a torsion
spring swings each flap out 112 deg against a stop, to splay 22 deg outward and reach 3.9 in ahead of his face (inside
18 x 24 in; R105). Reset by hand before each match. The right one is a low fence that passes under his corner wheel. Robot frame, inches: X forward from the centre, Y left, z up; his frame from his intake's uprights.

    python3 cad/modules/flaps/build.py       # writes flaps-deployed.step, flaps-folded.step and stl/ next to this file

The simulator (body-designs chat, 10 Oct): on his front, flaps take a Sister five pairing of his robots from 59 to 80
points (84 with foam) and the runs ending at 2 TIPs or fewer from 38 to 4 of 60.
tools/robot-cad/module_check.py flaps / flaps_folded checks it against his Robot.step.
"""
import math, os, importlib.util
import cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("transfer", os.path.join(HERE, "..", "..", "transfer", "build.py"))
TR = importlib.util.module_from_spec(spec); spec.loader.exec_module(TR)
MM = 1 / 25.4

# ---- his front, measured from his 9 Oct Robot.step (lined up by these uprights) ----
UP_X = (5.66, 7.58)          # the uprights' fore-aft extent: front faces at X 7.56, plus 0.02 clearance
UP_Y = (4.88, 5.37)          # |Y|: inner flange edge to the web's outer face (5.35), plus 0.02 clearance
UP_Z0 = 2.85                 # the uprights' bottoms
WEB_ROW_Z = 3.48             # a row of 4 mm holes in the web below the roller carriage's V-groove bearings (z 4.17 up)
WEB_COLS_X = (5.98, 7.24)    # two of its columns, clear of the V-rail inside the channel (X 6.32-6.90)
# ---- the module ----
HINGE = (8.12, 5.80)         # (X, |Y|) of each flap's vertical hinge: outboard of the upright, ahead of its face (folded,
                             # the foam faces back and clears the roller and the uprights' faces at X 7.56)
SPLAY = 22.0                 # deployed: degrees outward of straight ahead
FOLD = -90.0                 # folded: pointing across the face, toward the centre
FLAP_L = 3.67                # hinge to tip: 3.4 in ahead of the hinge, 1.37 in further out
PLATE_T, FOAM_T = 3 * MM, 0.5
FLAP_Z = {"L": (0.25, 4.25), "R": (0.25, 2.0)}   # the right one clears the corner wheel (its bottom at z 2.22)
SERVO = (40 * MM, 20 * MM, 37 * MM)              # a goBILDA-size servo: long, wide, tall; spline 10 mm from one end
SPLINE_FROM_END = 10 * MM
LATCH = (7.45, 6.30, 2.10)   # the latch servo's spline (X, |Y|) and its body's bottom: upside down behind the hinge, below the
                             # corner wheel's servo (z 3.9 up) and behind the corner wheel (X 8.29 on)

def box(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(x1 - x0, y1 - y0, z1 - z0).translate(((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2))

def flap(side, angle):
    """The flap and its foam, hinged at HINGE, pointing `angle` deg outward of straight ahead (folded: FOLD)."""
    s = 1 if side == "L" else -1
    z0, z1 = FLAP_Z[side]
    plate = box(0, FLAP_L, -PLATE_T / 2, PLATE_T / 2, z0, z1)
    foam = box(0.15, FLAP_L, -PLATE_T / 2 - FOAM_T, -PLATE_T / 2, z0 + 0.05, z1 - 0.05)   # the inner face: toward the intake
    knuckle = cq.Workplane("XY").circle(0.22).extrude(z1 - z0).translate((0, 0, z0))
    out = []
    for wp in (plate.union(knuckle), foam):
        if s < 0: wp = wp.mirror("XZ")
        wp = wp.rotate((0, 0, 0), (0, 0, 1), s * angle).translate((HINGE[0], s * HINGE[1], 0))
        out.append(wp)
    return out

def servo(side):
    """The latch: upside down behind the hinge, its horn (at the flap's z 1.85-2.0) across a notch in the flap's
    knuckle while folded; at START it turns 90 deg and the spring takes over."""
    s = 1 if side == "L" else -1
    x0, y0, z0 = LATCH
    body = box(x0 - (SERVO[0] - SPLINE_FROM_END), x0 + SPLINE_FROM_END, s * y0 - SERVO[1] / 2, s * y0 + SERVO[1] / 2, z0, z0 + SERVO[2])
    return body

def horn(side, latched):
    s = 1 if side == "L" else -1
    x0, y0, z0 = LATCH
    h = box(-0.15, 1.0, -0.12, 0.12, z0 - 0.25, z0 - 0.08)
    if not latched: h = h.rotate((0, 0, 0), (0, 0, 1), 90)
    h = h.translate((x0, y0, 0))
    return h.mirror("XZ") if s < 0 else h

def bracket(side):
    """Printed: a plate on the upright's web (two M4 through the web at WEB_ROW_Z, nuts inside the channel), a cradle
    round the latch servo, a foot carrying the hinge's lower pin and the deployed stop, and a wedge in front of the
    upright's face that turns a piece from the flap's root into the mouth."""
    s = 1 if side == "L" else -1
    z0, z1 = FLAP_Z[side]
    x0, y0, lz = LATCH
    plate = box(6.10, UP_X[1], UP_Y[1], UP_Y[1] + 0.20, 1.0, WEB_ROW_Z + 0.35)
    cradle = box(x0 - (SERVO[0] - SPLINE_FROM_END) - 0.08, x0 + SPLINE_FROM_END + 0.08, UP_Y[1], y0 + SERVO[1] / 2 + 0.10, lz + 0.6, lz + SERVO[2] + 0.08)
    cradle = cradle.cut(box(x0 - (SERVO[0] - SPLINE_FROM_END), x0 + SPLINE_FROM_END, y0 - SERVO[1] / 2, y0 + SERVO[1] / 2, lz, lz + SERVO[2]))
    foot = box(UP_X[1], HINGE[0] + 0.25, UP_Y[1], HINGE[1] + 0.25, z0 - 0.20, z0 - 0.02)
    wedge = (cq.Workplane("XY").polyline([(UP_X[1], UP_Y[0]), (UP_X[1], HINGE[1] - 0.24), (HINGE[0] - 0.26, HINGE[1] - 0.24)]).close()
             .extrude(z1 - z0).translate((0, 0, z0)))
    b = plate.union(cradle).union(foot).union(wedge)
    for x in WEB_COLS_X:
        b = b.cut(cq.Workplane("XZ").center(x, WEB_ROW_Z).circle(4.4 * MM / 2).extrude(-1).translate((0, UP_Y[1] - 0.1, 0)))
    return b.mirror("XZ") if s < 0 else b

def build(angle):
    p = {}
    for side in ("L", "R"):
        f, fm = flap(side, angle)
        p[f"flap_{side} (print, PETG 3 mm, a knuckle on an M4 shoulder-screw hinge pin, a torsion spring, a notch for the latch)"] = (f, (0.18, 0.37, 0.62), "print")
        p[f"flap_foam_{side} (0.5 in EVA or polyethylene foam, contact cement)"] = (fm, (0.95, 0.85, 0.3), "buy")
        p[f"flap_latch_servo_{side} (goBILDA 2000-0025-0003 or any standard-size servo: holds the flap folded until START)"] = (servo(side), (0.13, 0.15, 0.17), "buy")
        p[f"flap_latch_horn_{side} (print, PETG, on the servo's spline: across the knuckle's notch while folded)"] = (horn(side, angle == FOLD), (0.18, 0.37, 0.62), "print")
        p[f"flap_bracket_{side} (print, PETG: on the upright's web, cradles the servo; two M4 through the web, nuts inside the channel)"] = (bracket(side), (0.18, 0.37, 0.62), "print")
    return p
deployed, folded = build(SPLAY), build(FOLD)

HIS_ALIGN_MM = (0.27, 35.04)        # his 9 Oct Robot.step -> our robot CAD frame (cad/full-robot placed_team)
def his_frame(wp):
    return TR.to_cad(wp).translate(cq.Vector(-HIS_ALIGN_MM[0], 0, -HIS_ALIGN_MM[1]))

if __name__ == "__main__":
    os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
    for tag, parts in (("deployed", deployed), ("folded", folded)):
        assy = cq.Assembly(name=f"front flaps, {tag} (on the mentor's robot)")
        for n, (wp, col, kind) in parts.items():
            shp = his_frame(wp); assy.add(shp, name=n, color=cq.Color(*col))
            if kind == "print" and tag == "deployed": shp.exportStl(os.path.join(HERE, "stl", n.split(" ")[0] + ".stl"), 0.05, 0.2)
        assy.save(os.path.join(HERE, f"flaps-{tag}.step"))
    for tag, parts in (("folded", folded), ("deployed", deployed)):
        xs = [wp.val().BoundingBox() for wp, _, _ in parts.values()]
        print(tag, "front X %.2f" % max(b.xmax for b in xs), "|Y| %.2f" % max(max(abs(b.ymin), abs(b.ymax)) for b in xs))
