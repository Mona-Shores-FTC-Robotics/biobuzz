"""The FLOWER stub, first proposal: a to-scale side and top view on today's robot front.

    python3 cad/flower-stub/proposal.py        # writes proposal.png next to this file

Robot frame, inches: +X forward, +Y left, +Z up, origin on the floor under the middle of the drive axles (as
doc/corner-wheels.md). Every robot number is from the 24 Nov robot STEP as read by the corner-wheel session, or from
the October FLOWER-stick notebook; none is measured on the robot yet. The stub's own numbers (the CONSTANTS below) are
the proposal.
"""
import math, os
import numpy as np
import matplotlib; matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Polygon, Circle, Rectangle

HERE = os.path.dirname(os.path.abspath(__file__))
HOLE = 8 / 25.4                       # goBILDA hole pitch, 8 mm

# ---- the robot (24 Nov STEP, approximate at the front) ----------------------------------------------------------
FACE = 8.5                            # the intake's front face
BOX_FRONT, BOX_BACK = 10.43, -7.57    # the 18 in starting cube (R102)
KICKER = (8.5, 9.8, 5.7, 7.5, 0.375)  # Pollen Kicker on the intake, at pick height: X, X, Z, Z, half-width (rides)
CW_AXLE, CW_R = (9.22, 5.95), 1.15    # corner wheel (prototype 3), 2.3 in
CW_SERVO = (8.83, 9.61, 4.49, 6.62, 2.56, 3.80)   # X, X, |Y|, |Y|, Z, Z
CW_BRACKET_TOP = 4.75

# ---- the stub (the proposal) ---------------------------------------------------------------------------------------
PIVOT = np.array([9.1, 1.8])          # X, Z: 0.6 in (2 holes) ahead of the face, 1.8 in (6 holes) up
ARM_Y = (4.10, 4.20)                  # each arm, a flat pattern bar, inboard of the corner module
ARM_HOLES = 10                        # pivot to shaft, 80 mm
SHAFT_Z, WHEEL_R = 0.77, 0.77         # 8 mm REX shaft on two 39 mm wheels
WHEEL_Y = (3.25, 3.75)                # outside the FLOWER (2.40 each side) plus 0.8 in of aim
BLADE_HALF_W = 2.7                    # 5.4 in: the FLOWER (4.8 in, manual Fig. 9-12) plus one hole each side
SHAFT_BACK = 0.53                     # lip to shaft, the notebook's mountain blade
TAIL_CUT = 2.0
STOW_DEG = 83
SERVO = (8.6, 9.6, 4.35, 5.95, 4.85, 6.35)        # stick servo block above the corner module: X, X, |Y|, |Y|, Z, Z

L = ARM_HOLES * HOLE
shaft = np.array([PIVOT[0] + math.sqrt(L * L - (PIVOT[1] - SHAFT_Z) ** 2), SHAFT_Z])
lip = shaft[0] + SHAFT_BACK
dep = math.atan2(shaft[1] - PIVOT[1], shaft[0] - PIVOT[0])
tt = math.tan(math.radians(12)); xp = 0.289
prof = [(0, 0.52), (0, 0.6), (xp, 1.1)] + [(x, max(0.52, 1.1 - (x - xp) * tt)) for x in np.linspace(xp, TAIL_CUT, 12)] + [(TAIL_CUT, 0.52)]
blade = np.array([[lip - x, z] for x, z in prof])


def rot(P, a, c=PIVOT):
    co, si = math.cos(a), math.sin(a); q = np.asarray(P, float) - c
    return c + np.c_[co * q[:, 0] - si * q[:, 1], si * q[:, 0] + co * q[:, 1]]


def arm(a):
    d = np.array([math.cos(a), math.sin(a)]); n = np.array([-d[1], d[0]]) * 0.25
    p0, p1 = PIVOT - d * HOLE * 1.0, PIVOT + d * (L + 0.2)
    return np.array([p0 + n, p1 + n, p1 - n, p0 - n])


fold = math.radians(STOW_DEG) - dep
blade_s = rot(blade, fold); shaft_s = rot([shaft], fold)[0]
flower_x = lip - 1.18                 # FLOWER centre when the lip meets its posts (notebook: posts 1.18 in past the ball)

fig, (ax, at) = plt.subplots(1, 2, figsize=(15, 7.2), gridspec_kw={"width_ratios": [1.15, 1]})

# ---------------- side view ----------------
ax.axhline(0, color="k", lw=1)
ax.add_patch(Rectangle((2.0, 0.95), FACE - 0.93 - 2.0, 1.91, fc="#ddd", ec="#888")); ax.text(4.2, 1.9, "frame rail", fontsize=7, ha="center")
ax.add_patch(Rectangle((6.60, 2.86), 1.91, 5.67, fc="#ccc", ec="#888")); ax.text(7.55, 8.8, "intake side\nchannel (fixed)", fontsize=7, ha="center")
ax.add_patch(Circle((5.67, 1.9), 1.04, fc="none", ec="#888", ls="--")); ax.text(5.67, 1.9, "drive\nwheel", fontsize=6, ha="center", va="center")
ax.add_patch(Circle((FACE - 0.94, 2.0), 0.94, fc="#bbb", ec="k", lw=0.6)); ax.text(FACE - 0.94, 3.25, "roller (rides,\napprox.)", fontsize=6, ha="center")
ax.add_patch(Rectangle((KICKER[0], KICKER[2]), KICKER[1] - KICKER[0], KICKER[3] - KICKER[2], fc="#e0552b", ec="k", lw=0.6))
ax.annotate("Pollen Kicker (rides;\nat its lowest)", (KICKER[1], 7.2), (10.6, 8.3), fontsize=7, arrowprops=dict(arrowstyle="->"))
ax.add_patch(Rectangle((CW_AXLE[0] - CW_R, 0.6), 2 * CW_R, 1.05, fc="#7fb3d5", ec="k", lw=0.6, alpha=0.7))
ax.add_patch(Rectangle((CW_SERVO[0], CW_SERVO[4]), CW_SERVO[1] - CW_SERVO[0], CW_SERVO[5] - CW_SERVO[4], fc="#7fb3d5", ec="k", lw=0.6, alpha=0.7))
ax.annotate("corner wheel + its servo (Tim)", (CW_SERVO[1], 3.2), (10.6, 3.0), fontsize=7, color="#1f5f8b", arrowprops=dict(arrowstyle="->", color="#1f5f8b"))
ax.add_patch(Rectangle((SERVO[0], SERVO[4]), SERVO[1] - SERVO[0], SERVO[5] - SERVO[4], fc="#9b59b6", ec="k", lw=0.6, alpha=0.8))
ax.annotate("stub servo, held\n(on the side channel,\nabove the corner module)", (SERVO[0], 5.6), (2.2, 7.2), fontsize=7, color="#6c3483", arrowprops=dict(arrowstyle="->", color="#6c3483"))
ax.plot([SERVO[0] + 0.5, PIVOT[0] - 0.25], [5.6, PIVOT[1] + 0.25], color="#6c3483", lw=1.5, ls="-.")
ax.text(8.0, 3.9, "link", fontsize=7, color="#6c3483")
# FLOWER
fx = flower_x
ax.add_patch(Rectangle((fx - 2.36, 0), 4.72, 0.43, fc="#777", alpha=0.5, zorder=0)); ax.add_patch(Rectangle((fx + 1.18, 0.35), 1.22, 3.75, fc="#999", alpha=0.3, zorder=0))
ax.add_patch(Rectangle((fx - 2.41, 3.55), 4.82, 0.35, fc="#555", alpha=0.4, zorder=0))
for sg in (1, -1): ax.add_patch(Rectangle((fx + sg * 1.72 - 0.525, 4.25), 1.05, 5.0, fc="#8a8", alpha=0.2, zorder=0))
for bz in (1.75, 4.6): ax.add_patch(Circle((fx - 0.1, bz), 1.4, fc="#e8d44a", ec="#aa9", alpha=0.25, zorder=0))
ax.text(fx + 0.6, 9.6, "FLOWER (behind)", fontsize=8, ha="center")
# stub deployed
ax.add_patch(Polygon(arm(dep), fc="#aaa", ec="k", lw=0.8))
ax.add_patch(Circle(tuple(shaft), WHEEL_R, fc="#444", alpha=0.35)); ax.add_patch(Polygon(blade, fc="#f4c20d", ec="k", lw=0.8))
ax.add_patch(Circle(tuple(PIVOT), 0.12, fc="k"))
# stowed
ax.add_patch(Polygon(arm(math.radians(STOW_DEG)), fc="none", ec="#555", ls="--"))
ax.add_patch(Polygon(blade_s, fc="none", ec="#b8860b", ls="--", lw=1.2)); ax.add_patch(Circle(tuple(shaft_s), WHEEL_R, fc="none", ec="#555", ls="--"))
ax.axvline(BOX_FRONT, color="#c0392b", lw=1, ls=":"); ax.text(BOX_FRONT + 0.05, 10.6, "18 in cube front", fontsize=7, color="#c0392b", rotation=90, va="top")
ax.axvline(FACE, color="#444", lw=0.6, ls=":")
ax.annotate(f"arm: {ARM_HOLES} holes pivot to shaft\n{-math.degrees(dep):.0f}° down, wheels on the tile", (PIVOT[0] + 1.3, 1.3), (2.2, 5.2), fontsize=8, arrowprops=dict(arrowstyle="->"))
ax.annotate(f"foot: lip {lip - FACE:.1f} in ({(lip - FACE) / HOLE:.0f} holes)\nahead of the intake face", (lip, 0.6), (11.6, 1.6), fontsize=8, arrowprops=dict(arrowstyle="->"))
ax.text(9.0, -0.75, "pivot: 2 holes ahead of the face, 6 up", fontsize=7, ha="center")
ax.set_xlim(1.5, 14.5); ax.set_ylim(-1.1, 11); ax.set_aspect("equal"); ax.grid(True, lw=0.3, alpha=0.4)
ax.set_title("SIDE (left side, looking in). Solid: down for a pick. Dashed: stowed.", fontsize=10)
ax.set_xlabel("in, forward of the drive axles"); ax.set_ylabel("in, up from the tile")

# ---------------- top view ----------------
for sg in (1, -1):
    at.add_patch(Rectangle((2.0, sg * 4.87 if sg > 0 else -5.36), FACE - 0.93 - 2.0, 0.49, fc="#ddd", ec="#888"))
    at.add_patch(Rectangle((6.60, sg * 4.87 if sg > 0 else -5.36), 1.91, 0.49, fc="#ccc", ec="#888"))
    at.add_patch(Rectangle((4.64, 5.92 if sg > 0 else -6.40), 2.07, 0.48, fc="none", ec="#888", ls="--"))
    at.add_patch(Circle((CW_AXLE[0], sg * CW_AXLE[1]), CW_R, fc="#7fb3d5", ec="k", lw=0.6, alpha=0.7))
    y0, y1 = (CW_SERVO[2], CW_SERVO[3]) if sg > 0 else (-CW_SERVO[3], -CW_SERVO[2])
    at.add_patch(Rectangle((CW_SERVO[0], y0), CW_SERVO[1] - CW_SERVO[0], y1 - y0, fc="#7fb3d5", ec="k", lw=0.6, alpha=0.7))
    a0, a1 = (ARM_Y if sg > 0 else (-ARM_Y[1], -ARM_Y[0]))
    at.add_patch(Rectangle((PIVOT[0] - HOLE, a0), shaft[0] - PIVOT[0] + HOLE + 0.2, a1 - a0, fc="#aaa", ec="k", lw=0.8))
    w0, w1 = (WHEEL_Y if sg > 0 else (-WHEEL_Y[1], -WHEEL_Y[0]))
    at.add_patch(Rectangle((shaft[0] - WHEEL_R, w0), 2 * WHEEL_R, w1 - w0, fc="#444", alpha=0.45))
at.add_patch(Rectangle((SERVO[0], SERVO[2]), SERVO[1] - SERVO[0], SERVO[3] - SERVO[2], fc="#9b59b6", ec="k", lw=0.6, alpha=0.5))
at.text(SERVO[0] - 0.1, 6.6, "stub servo (above the\ncorner wheel's)", fontsize=7, color="#6c3483", ha="right")
at.add_patch(Rectangle((FACE - 1.0, -4.84), 1.0, 9.68, fc="#bbb", ec="k", lw=0.6)); at.text(FACE - 0.5, 0, "roller", rotation=90, fontsize=7, ha="center", va="center")
at.add_patch(Rectangle((KICKER[0], -KICKER[4]), KICKER[1] - KICKER[0], 2 * KICKER[4], fc="#e0552b", ec="k", lw=0.6))
at.plot([shaft[0], shaft[0]], [-ARM_Y[1], ARM_Y[1]], color="k", lw=1.2)
at.add_patch(Rectangle((lip - TAIL_CUT, -BLADE_HALF_W), TAIL_CUT, 2 * BLADE_HALF_W, fc="#f4c20d", ec="k", lw=0.8))
at.add_patch(Rectangle((flower_x - 2.36, -2.40), 4.72, 4.80, fc="#777", alpha=0.35))
for sx in (1, -1):
    for sy in (1, -1): at.add_patch(Circle((flower_x + sx * 1.72, sy * 1.2), 0.5, fc="#8a8", alpha=0.6))
at.text(flower_x + 1.2, -3.0, "FLOWER", fontsize=8)
at.axvline(BOX_FRONT, color="#c0392b", lw=1, ls=":")
at.axhline(9.0, color="#c0392b", lw=0.6, ls=":"); at.axhline(-9.0, color="#c0392b", lw=0.6, ls=":")
at.annotate(f"foot {2 * BLADE_HALF_W:.1f} in wide ({2 * BLADE_HALF_W / HOLE:.0f} holes):\nthe FLOWER (4.8) + 1 hole each side;\nwheels outside it with 0.8 in of aim", (lip - 0.8, BLADE_HALF_W), (10.5, 6.3), fontsize=8, arrowprops=dict(arrowstyle="->"))
at.annotate("arms inboard of the\ncorner modules", (PIVOT[0] + 1.0, -ARM_Y[1]), (10.6, -7.6), fontsize=8, arrowprops=dict(arrowstyle="->"))
at.set_xlim(1.5, 14.5); at.set_ylim(-9.5, 9.5); at.set_aspect("equal"); at.grid(True, lw=0.3, alpha=0.4)
at.set_title("TOP (front to the right). Deployed.", fontsize=10); at.set_xlabel("in, forward of the drive axles"); at.set_ylabel("in, left of centre")

P = np.vstack([blade_s, [shaft_s + [WHEEL_R, 0]]])
fig.suptitle(f"FLOWER stub, proposal 1 (to scale; robot numbers from the 24 Nov STEP and the October notebook; FLOWER from manual Fig. 9-12). "
             f"Stowed: front {P[:, 0].max():.2f} in (cube {BOX_FRONT}), bottom {blade_s[:, 1].min():.1f} in up, top {max(blade_s[:, 1].max(), shaft_s[1] + WHEEL_R):.1f} in",
             fontsize=9.5)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "proposal.png"), dpi=120)
print(f"shaft {shaft.round(2)}, lip {lip:.2f} ({lip - FACE:.2f} ahead of face), arm {math.degrees(dep):.1f} deg; stowed blade "
      f"X {blade_s[:, 0].min():.2f}..{blade_s[:, 0].max():.2f}, Z {blade_s[:, 1].min():.2f}..{blade_s[:, 1].max():.2f}; shaft {shaft_s.round(2)}")
