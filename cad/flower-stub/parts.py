"""The FLOWER stub: a short, narrow, held FLOWER pick on fixed structure (mentor, 25 Nov 2026). Makes every printed part,
an assembly in both positions, and checks the clearances against the corner modules, the 18 in cube and the intake.

    pip install cadquery
    python3 cad/flower-stub/parts.py        # writes the parts next to this file and prints the checks

What it is (doc/flower-stub.md has the why):
- A U that swings on fixed structure: two printed arms, 10 goBILDA holes (80 mm) from pivot to foot shaft, joined by an
  8 mm REX shaft that carries the foot (the October notebook's mountain blade, 5.4 in wide) and two 39 mm wheels. The
  wheels set the foot's height on the tile, so the push into the FLOWER goes down the arms into the pivots, not the servo.
- Each pivot is an 8 mm REX stub turning in a 14 mm flanged bearing in a printed mount. The mount bolts to the front flange
  of the intake's side channel through the free holes above the corner-wheel bracket (Z 5.066 up). Nothing touches the
  intake carriage or its slides, and nothing is added to Tim's corner module.
- One held servo, on the left mount above the corner module, drives the U through a crank, a link and a lever: a
  four-bar, nearly a parallelogram, kept 39 deg from its dead points at both ends.

Robot frame, inches, as doc/corner-wheels.md: +X forward, +Y left, +Z up, origin on the floor under the middle of the drive
axles. Parts are built in that frame (mm in the files) on the LEFT side; the right side is the mirror (Y -> -Y).
Every robot number is from the 24 Nov robot STEP as read by the corner-wheel session, or from the October FLOWER-stick
notebook; the FLOWER is the manual's Figure 9-12. None is measured on the robot: doc/robot-todo.md has the ruler check.
"""
import math
import os

import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))
IN = 25.4
HOLE = 8 / IN

# ---- the robot (not measured) ----------------------------------------------------------------------------------------
FACE = 8.50                     # the intake's front face; nothing of ours goes behind it inside |Y| 4.84
BOX_FRONT, BOX_HALF_W = 10.43, 9.0
FLANGE = dict(x0=8.51, y0=4.87, y1=5.36, hole_y=5.115, holes_z=(5.066, 5.696, 6.326))   # side channel's front flange
KICKER = dict(x0=8.50, x1=9.80, half_w=0.375, z0=5.70)                                  # rides with the intake
# Tim's corner module (claude/corner-module-wall-wip, cad/corner-wheels/parts.py), left side
CW = dict(shelf=(8.50, 9.82, 4.375, 6.74, 2.66, 3.64),      # shelf and its cage walls, one box
          servo=(8.83, 9.61, 4.49, 6.62, 2.56, 3.80),
          tab=(8.50, 8.70, 4.90, 5.40, 3.40, 4.70),
          wing=(7.74, 8.50, 5.36, 5.60, 2.66, 4.75),
          hub=(9.22, 5.95, 16 / IN, 1.85, 2.16),             # x, y, r, z0, z1
          wheel=(9.22, 5.95, 29.2 / IN + 0.1, 0.60, 1.65))   # + 0.1 in for the swept flaps

# ---- the stub (the design) -------------------------------------------------------------------------------------------
PIVOT = (9.10, 1.80)            # X, Z: 2 holes ahead of the face, 6 up
ARM_HOLES = 10
SHAFT_Z = 0.77                  # foot shaft height = wheel radius: the wheels stand on the tile
WHEEL_D = 39.0                  # mm
LIP_TO_SHAFT = 0.53             # the mountain blade's numbers (notebook): lip, 60 deg nose, 1.1 in peak, 12 deg tail
EDGE, PEAK, NOSE_DEG, TAIL_DEG, BOTTOM = 0.60, 1.10, 60.0, 12.0, 0.52
TAIL_CUT = 2.0                  # the tail ends 2.0 in behind the lip (the notebook feeds the same down to 1.6)
LANE_RISE, LANE_START, LANE_LEAD = 0.30, 0.90, 1.60   # Carter's lane: edges up 0.3 in, from 0.9 in behind the lip
FOOT_HALF_W = 2.70              # 5.4 in: the FLOWER's 4.8 in plus about a hole each side
# across the robot, left side, |Y| inches (centre outward)
Y_SPACER = (2.71, 2.95)
Y_WHEEL = (2.97, 3.37)
Y_ARM = (3.40, 3.65)
Y_WEB = (3.67, 3.95)
Y_LEVER = (3.97, 4.15)
Y_LINK = (4.17, 4.31)
Y_CRANK = (4.33, 4.49)
LEVER_R = 2 * HOLE              # 16 mm, both cranks
LEVER_OFFSET_DEG = -32.0        # lever = arm direction turned this far: keeps both ends 39 deg from the dead points
SERVO_AXIS = (9.375, 6.50)      # X, Z of the servo's spline: above the pivot, above the corner module
STOW_DEG = 83.0                 # arm angle stowed (deployed is set by the wheels on the tile)
HEX_AF = 7.15                   # mm, the notebook's snug hole for 8 mm REX (7 mm across the flats)
BEARING_OD = 14.0               # mm, goBILDA 8 mm REX flanged ball bearing
M4, M3_TAP = 4.4, 2.5

# derived
L = ARM_HOLES * HOLE
SHAFT = (PIVOT[0] + math.sqrt(L * L - (PIVOT[1] - SHAFT_Z) ** 2), SHAFT_Z)
LIP = SHAFT[0] + LIP_TO_SHAFT
ARM_DEP = math.degrees(math.atan2(SHAFT[1] - PIVOT[1], SHAFT[0] - PIVOT[0]))


def mm(*v):
    return tuple(x * IN for x in v)


def slab(pts, y0, y1):
    """A prism: the polygon pts (X, Z inches) between |Y| y0 and y1 on the left side."""
    w = cq.Workplane("XZ").polyline([mm(x, z) for x, z in pts]).close().extrude((y1 - y0) / 2 * IN, both=True)
    return w.translate((0, (y0 + y1) / 2 * IN, 0))


def disc(x, z, r, y0, y1):
    return cq.Workplane("XZ").center(*mm(x, z)).circle(r * IN).extrude((y1 - y0) / 2 * IN, both=True) \
        .translate((0, (y0 + y1) / 2 * IN, 0))


def hole_y(x, z, d_mm, y0=-10.0, y1=10.0):
    return cq.Workplane("XZ").center(*mm(x, z)).circle(d_mm / 2).extrude((y1 - y0) / 2 * IN, both=True) \
        .translate((0, (y0 + y1) / 2 * IN, 0))


def hex_y(x, z, y0=-10.0, y1=10.0):
    """The REX hole along Y, flats horizontal with the U deployed (so the foot, the arms and the lever all key alike)."""
    return cq.Workplane("XZ").center(*mm(x, z)).polygon(6, HEX_AF / math.cos(math.pi / 6)) \
        .extrude((y1 - y0) / 2 * IN, both=True).translate((0, (y0 + y1) / 2 * IN, 0))


def bar(p, q, w):
    dx, dz = q[0] - p[0], q[1] - p[1]; n = math.hypot(dx, dz); nx, nz = -dz / n * w / 2, dx / n * w / 2
    return [(p[0] + nx, p[1] + nz), (q[0] + nx, q[1] + nz), (q[0] - nx, q[1] - nz), (p[0] - nx, p[1] - nz)]


def at_angle(c, r, deg):
    return (c[0] + r * math.cos(math.radians(deg)), c[1] + r * math.sin(math.radians(deg)))


# ---- the parts, deployed, left side ---------------------------------------------------------------------------------
def foot():
    tn, tt = math.tan(math.radians(NOSE_DEG)), math.tan(math.radians(TAIL_DEG))
    xp = (PEAK - EDGE) / tn                         # the peak, behind the lip
    tail = lambda u: max(BOTTOM, PEAK - (u - xp) * tt)
    ramp = lambda u: min(1.0, max(0.0, (u - LANE_START) / LANE_LEAD))
    us = [xp + (TAIL_CUT - xp) * i / 24 for i in range(25)]
    prof = [(LIP, BOTTOM), (LIP, EDGE), (LIP - xp, PEAK)] + [(LIP - u, tail(u) + LANE_RISE * ramp(u)) for u in us[1:]] \
        + [(LIP - TAIL_CUT, BOTTOM)]
    body = slab(prof, -FOOT_HALF_W, FOOT_HALF_W)
    # the lane: a long cylinder lying along the tail, its lowest line on the plain tail at the centre, so the edges
    # stay up by the ramped rise and the middle comes down to the tail: one curve across, no flats
    hw, rise = FOOT_HALF_W, LANE_RISE
    rg = (hw * hw + rise * rise) / (2 * rise)
    a = math.radians(TAIL_DEG)
    d = cq.Vector(-math.cos(a), 0, -math.sin(a)); n = cq.Vector(-math.sin(a), 0, math.cos(a))
    p0 = cq.Vector(*mm(LIP - xp, 0, PEAK)) + n * (rg * IN) - d * (1.0 * IN)
    body = body.cut(cq.Workplane().add(cq.Solid.makeCylinder(rg * IN, 6 * IN, p0, d)))
    body = body.cut(hex_y(*SHAFT))
    for y in (-1.2, 1.2):                           # two M3 set screws down onto the shaft's flat
        body = body.cut(cq.Workplane("XY").center(SHAFT[0] * IN, y * IN).circle(M3_TAP / 2).extrude(2 * IN)
                        .translate((0, 0, SHAFT[1] * IN)))
    return body


def arm():
    pts = bar(PIVOT, SHAFT, 0.60)
    body = slab(pts, *Y_ARM).union(disc(*PIVOT, 0.45, *Y_ARM)).union(disc(*SHAFT, 0.40, *Y_ARM))
    return body.cut(hex_y(*PIVOT)).cut(hex_y(*SHAFT))


def wheel():
    w = disc(*SHAFT, WHEEL_D / 2 / IN, *Y_WHEEL).cut(hole_y(*SHAFT, 8.4))
    return w


def spacer():
    return disc(*SHAFT, 7 / IN, *Y_SPACER).cut(hole_y(*SHAFT, 8.4))


def lever_pin(arm_deg):
    return at_angle(PIVOT, LEVER_R, arm_deg + LEVER_OFFSET_DEG)


def lever():
    pin = lever_pin(ARM_DEP)
    body = disc(*PIVOT, 0.32, *Y_LEVER).union(slab(bar(PIVOT, pin, 0.36), *Y_LEVER)).union(disc(*pin, 0.18, *Y_LEVER))
    return body.cut(hex_y(*PIVOT)).cut(hole_y(*pin, M4))


def link_len():
    return math.dist(lever_pin(ARM_DEP), crank_pin_for(ARM_DEP, LEVER_OFFSET_DEG + ARM_DEP))


def crank_pin_at(deg):
    return at_angle(SERVO_AXIS, LEVER_R, deg)


def crank_pin_for(arm_deg, guess, length=None):
    """Where the crank pin is for an arm angle: the circle about the servo axis met by the link's length from the lever
    pin, taking the meeting point nearest the guess (the branch the four-bar is built on)."""
    if length is None:
        return crank_pin_at(guess)
    p = lever_pin(arm_deg); best = None
    for i in range(3600):
        deg = guess - 90 + i * 0.05
        e = abs(math.dist(p, crank_pin_at(deg)) - length)
        if best is None or e < best[0]:
            best = (e, deg)
    return crank_pin_at(best[1]), best[1], best[0]


def link(arm_deg, crank_deg):
    a, b = lever_pin(arm_deg), crank_pin_at(crank_deg)
    body = slab(bar(a, b, 0.40), *Y_LINK).union(disc(*a, 0.20, *Y_LINK)).union(disc(*b, 0.20, *Y_LINK))
    return body.cut(hole_y(*a, M4)).cut(hole_y(*b, M4))


def crank(crank_deg):
    """Bolts to a goBILDA 1908 Series Servo Hub (25T spline, 32 mm): 4 x M4 on a 16 mm circle and a centre relief."""
    pin = crank_pin_at(crank_deg)
    body = disc(*SERVO_AXIS, 17 / IN, *Y_CRANK).union(slab(bar(SERVO_AXIS, pin, 0.40), *Y_CRANK)).union(disc(*pin, 0.20, *Y_CRANK))
    body = body.cut(hole_y(*SERVO_AXIS, 9.0)).cut(hole_y(*pin, M4))
    for k in range(4):
        body = body.cut(hole_y(*at_angle(SERVO_AXIS, 8 / IN, 45 + 90 * k), M4))
    return body


# the servo: a SWYFT / standard size, spline inboard, as the corner-wheel session measured it (case 11.2 mm on the
# spline side of the flange, 29.0 mm on the other, spline boss 5.7 mm more); 40.3 x 20.3 mm case, ears 48 x 10 mm holes
SPLINE_TIP_Y = Y_CRANK[1] + 8 / IN                  # the hub between crank and spline
FLANGE_Y = SPLINE_TIP_Y + (11.2 + 5.7) / IN
SERVO_CASE = (SERVO_AXIS[0] - 10.15 / IN, SERVO_AXIS[0] + 10.15 / IN,
              SPLINE_TIP_Y + 5.7 / IN, FLANGE_Y + 29.0 / IN,
              SERVO_AXIS[1] - 30.3 / IN, SERVO_AXIS[1] + 10.0 / IN)   # spline 10 mm from the case's top end


def box(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box((x1 - x0) * IN, (y1 - y0) * IN, (z1 - z0) * IN, centered=False).translate(mm(x0, y0, z0))


def mount(with_servo):
    """The printed mount: a plate on the side channel's front flange (counterbored M4s through its free holes above the
    corner bracket), a bridge inboard above the corner module, and a web down to the pivot bearing. The left one also
    holds the servo: a wall with the case's window, the flange's ears screwed to it."""
    plate_x1 = FLANGE["x0"] + 0.44
    top = 7.30 if with_servo else 6.60
    body = box(FLANGE["x0"], plate_x1, FLANGE["y0"], (FLANGE_Y + 0.20) if with_servo else FLANGE["y1"], 4.80, top)
    body = body.union(box(FLANGE["x0"], 9.30, Y_WEB[0], FLANGE["y1"], 4.80, 5.25))                  # the bridge
    web = [(8.60, 5.25), (9.30, 5.25), (9.52, PIVOT[1]), (PIVOT[0], PIVOT[1] - 0.42), (8.70, PIVOT[1])]
    body = body.union(slab(web, *Y_WEB)).union(disc(*PIVOT, 0.42, *Y_WEB))
    body = body.cut(hole_y(*PIVOT, BEARING_OD))
    for z in FLANGE["holes_z"]:
        if z > top - 0.2:
            continue
        h = cq.Workplane("YZ").center(FLANGE["hole_y"] * IN, z * IN).circle(M4 / 2).extrude(1.0 * IN).translate((FLANGE["x0"] * IN - 1, 0, 0))
        cb = cq.Workplane("YZ").center(FLANGE["hole_y"] * IN, z * IN).circle(4.2).extrude(1.0 * IN).translate(((FLANGE["x0"] + 0.24) * IN, 0, 0))
        body = body.cut(h).cut(cb)
    if with_servo:
        x0, x1, _, _, z0, z1 = SERVO_CASE
        wall = box(FLANGE["x0"], x1 + 0.25, FLANGE_Y, FLANGE_Y + 0.20, z0 - 0.35, z1 + 0.35)
        wall = wall.cut(box(x0 - 0.01, x1 + 0.01, FLANGE_Y - 0.1, FLANGE_Y + 0.3, z0 - 0.01, z1 + 0.01))
        zc = (z0 + z1) / 2
        for dz in (-24, 24):
            for dx in (-5, 5):
                wall = wall.cut(cq.Workplane("XZ").center(SERVO_AXIS[0] * IN + dx, zc * IN + dz).circle(M3_TAP / 2)
                                .extrude(0.5 * IN, both=True).translate((0, (FLANGE_Y + 0.1) * IN, 0)))
        body = body.union(wall)
    return body


def mirror(w):
    return w.mirror("XZ")


def rot_about_pivot(w, deg):
    return w.rotate(cq.Vector(*mm(PIVOT[0], 0, PIVOT[1])), cq.Vector(*mm(PIVOT[0], 1, PIVOT[1])), -deg)


def shaft_solids():
    s = disc(*SHAFT, 4 / IN, -Y_ARM[1], Y_ARM[1])                    # foot shaft, 8 mm REX, through both arms
    for sg in (1, -1):
        p = disc(*PIVOT, 4 / IN, Y_ARM[0], Y_LEVER[1] if sg > 0 else Y_WEB[1])
        s = s.union(p if sg > 0 else mirror(p))
    return s


def moving_group():
    """Everything that swings with the arms (both sides)."""
    g = foot().union(shaft_solids())
    for part in (arm(), wheel(), spacer()):
        g = g.union(part).union(mirror(part))
    return g.union(lever())


# ---- checks ----------------------------------------------------------------------------------------------------------
def keepouts():
    k = {}
    for side, sg in (("left", 1), ("right", -1)):
        for name in ("shelf", "servo", "tab", "wing"):
            x0, x1, y0, y1, z0, z1 = CW[name]
            b = box(x0, x1, y0, y1, z0, z1)
            k[f"corner {name} ({side})"] = b if sg > 0 else mirror(b)
        for name in ("hub", "wheel"):
            x, y, r, z0, z1 = CW[name]
            c = cq.Workplane("XY").center(x * IN, y * IN).circle(r * IN).extrude((z1 - z0) * IN).translate((0, 0, z0 * IN))
            k[f"corner {name} ({side})"] = c if sg > 0 else mirror(c)
    k["Pollen Kicker (lowest)"] = box(KICKER["x0"], KICKER["x1"], -KICKER["half_w"], KICKER["half_w"], KICKER["z0"], KICKER["z0"] + 2)
    k["intake (behind the face)"] = box(4.0, FACE, -4.84, 4.84, 0.0, 14.0)
    return k


def dist(a, b):
    return a.val().distance(b.val()) / IN if hasattr(a.val(), "distance") else float("nan")


def main():
    print(f"arm {ARM_DEP:.1f} deg deployed, shaft ({SHAFT[0]:.2f}, {SHAFT[1]:.2f}), lip {LIP:.2f} = {LIP - FACE:.2f} in "
          f"({(LIP - FACE) / HOLE:.1f} holes) ahead of the face; stowed {STOW_DEG} deg")
    # the four-bar: build it at deployed (crank parallel to the lever), then follow it to stowed
    ln = link_len()
    sweep = [ARM_DEP + (STOW_DEG - ARM_DEP) * i / 10 for i in range(11)]
    crank_degs, guess = [], ARM_DEP + LEVER_OFFSET_DEG
    worst = 180
    for a in sweep:
        _, cd, err = crank_pin_for(a, guess, ln); guess = cd; crank_degs.append(cd)
        lp, cp = lever_pin(a), crank_pin_at(cd)
        lk = math.atan2(cp[1] - lp[1], cp[0] - lp[0])
        for c, ang in ((PIVOT, a + LEVER_OFFSET_DEG), (SERVO_AXIS, cd)):
            t = abs(math.degrees(lk) - ang) % 180; worst = min(worst, t, 180 - t)
        assert err < 0.005, f"four-bar does not close at {a:.0f} deg"
    print(f"four-bar: link {ln:.3f} in ({ln * IN:.1f} mm) between M4 pins; servo turns {crank_degs[-1] - crank_degs[0]:.0f} deg "
          f"for the arm's {STOW_DEG - ARM_DEP:.0f}; worst angle between link and a crank {worst:.0f} deg (0 = dead point)")

    out = {"foot": foot(), "arm": arm(), "wheel": wheel(), "spacer": spacer(), "lever": lever(),
           "link": link(ARM_DEP, crank_degs[0]), "crank": crank(crank_degs[0]),
           "mount_left": mount(True), "mount_right": mirror(mount(False))}
    for name, w in out.items():
        assert len(w.solids().vals()) == 1 and w.val().isValid(), name
    # print orientation: lay each part on its biggest flat face
    lay_y = ("arm", "wheel", "spacer", "lever", "link", "crank")
    for name, w in out.items():
        cq.exporters.export(w, os.path.join(HERE, f"{name}.step"))      # robot frame, for the assembly
        p = w
        if name in lay_y:
            p = p.rotate((0, 0, 0), (1, 0, 0), 90)
        bb = p.val().BoundingBox()
        p = p.translate((-bb.xmin, -bb.ymin, -bb.zmin))
        for ext in ("stl", "3mf"):
            cq.exporters.export(p, os.path.join(HERE, f"{name}.{ext}"))
        print(f"  {name}: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {w.val().Volume() / 1000:.1f} cm3")

    # assemblies and clearances through the whole swing
    fixed = out["mount_left"].union(out["mount_right"])
    k = keepouts()
    servo_case = box(*SERVO_CASE)
    k_fixed = dict(k); k_fixed.pop("intake (behind the face)")
    for name, kk in k_fixed.items():
        d = dist(fixed, kk)
        print(f"  mounts to {name}: {d:.2f} in")
    worst_by = {}
    front, top_lo = -1, None
    for a, cd in zip(sweep, crank_degs):
        g = rot_about_pivot(moving_group(), a - ARM_DEP)
        lk, ck = link(a, cd), crank(cd)
        bb = g.val().BoundingBox()
        front = max(front, bb.xmax / IN, lk.val().BoundingBox().xmax / IN, ck.val().BoundingBox().xmax / IN)
        for name, kk in k.items():
            for part, w in (("U", g), ("link", lk), ("crank", ck)):
                d = dist(w, kk)
                key = f"{part} to {name}"
                worst_by[key] = min(worst_by.get(key, 99), d)
        for part, w in (("U", g),):
            d = dist(w, fixed); worst_by[f"{part} to the mounts"] = min(worst_by.get(f"{part} to the mounts", 99), d)
        d = dist(lk, servo_case); worst_by["link to servo case"] = min(worst_by.get("link to servo case", 99), d)
    print("closest approach anywhere in the swing (in):")
    for key, d in sorted(worst_by.items(), key=lambda kv: kv[1]):
        flag = "  <-- TOUCHES" if d <= 0.001 else ("  <-- under 1/16 in" if d < 0.0625 else "")
        print(f"  {key}: {d:.3f}{flag}")
    print(f"front-most point in the swing: X {front:.2f} (cube {BOX_FRONT})")

    for label, a, cd in (("deployed", ARM_DEP, crank_degs[0]), ("stowed", STOW_DEG, crank_degs[-1])):
        g = rot_about_pivot(moving_group(), a - ARM_DEP)
        asm = cq.Assembly()
        asm.add(g, name="U", color=cq.Color(0.95, 0.76, 0.05))
        asm.add(out["mount_left"], name="mount_left", color=cq.Color(0.6, 0.35, 0.7))
        asm.add(out["mount_right"], name="mount_right", color=cq.Color(0.6, 0.35, 0.7))
        asm.add(link(a, cd), name="link"); asm.add(crank(cd), name="crank")
        asm.add(box(*SERVO_CASE), name="servo (reference)", color=cq.Color(0.2, 0.2, 0.2))
        asm.export(os.path.join(HERE, f"assembly_{label}.step"))
        bb = g.val().BoundingBox()
        print(f"{label}: U spans X {bb.xmin / IN:.2f}..{bb.xmax / IN:.2f}, Z {bb.zmin / IN:.2f}..{bb.zmax / IN:.2f}")
    return crank_degs


if __name__ == "__main__":
    main()
