"""v10: the self-tripping nose. Two printed parts and one 4 mm pin.

The body is v7 with Carter's lane (step/v7_lane.step) with its nose cut off at a hinge line 0.32 in behind the lip,
0.96 in up. The nose piece carries the lip and the 60 deg face across the full width and a hub around the pin in the
middle. When the lip stops on the posts and the robot keeps driving, the push comes in below the pin and turns the
piece nose-up; it stops turning when the lip's bottom corner has risen to the pin's height (54 deg, the body 0.22 in
further in) and falls flat again when the robot backs out. In the model the tilt and the extra reach together feed
every case; neither alone does. No toe: nothing reaches between the posts, so the aim is v7's. The posts' corners pass
through gaps in the hinge at |y| 1.2 to 1.6, so nothing on the body reaches them. TOE_L > 0 adds a stick ahead of the
lip (the model does not need it).

    python3 cad/modules/flower-stick-proto/toe_blade.py     # step/v10_body.step, step/v10_nose.step, stl/..., v10.png
"""
import math, os
import numpy as np
import cadquery as cq
IN = 25.4
HERE = os.path.dirname(os.path.abspath(__file__))
Z0 = 0.0                                             # the STEP is in the field frame: the blade bottom at z 0.52 in
PIN_X, PIN_Z, PIN_D = 0.32, 0.96 - Z0, 4.1 / IN     # the hinge: 4 mm rod, a 4.1 mm hole
HUB_R, CLEAR_R = 0.15, 0.16                          # the knuckles' radius and the clearance around them
HUB_HW, EAR_Y = 1.2, 1.6                             # the nose's hub spans |y| < 1.2; the body's ears are |y| > 1.6
W, HW = 5.4, 2.7
BOTTOM, EDGE, PEAK = 0.52, 0.60, 1.10
TOE_L, TOE_HW, TOE_T = 0.0, 0.25, 0.10                  # no toe; a 0.5 in wide stick if TOE_L is set
HEEL_X = 0.45
TRIP_DEG, ADVANCE = 54, 0.22

def cyl_y(r, y0, y1, x, z):                          # a cylinder along y from y0 to y1, axis at (x, z), inches
    return cq.Workplane("XZ").circle(r * IN).extrude(-(y1 - y0) * IN).translate((x * IN, y0 * IN, z * IN)) if False else \
        cq.Workplane("XZ", origin=(x * IN, y0 * IN, z * IN)).circle(r * IN).extrude(-(y1 - y0) * IN)
def box(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box((x1 - x0) * IN, (y1 - y0) * IN, (z1 - z0) * IN, centered=False).translate((x0 * IN, y0 * IN, z0 * IN))

# ---- the body: v7 with the nose removed and ears left for the pin
v7 = cq.importers.importStep(os.path.join(HERE, "step", "v7_lane.step"))
bb = v7.val().BoundingBox(); print(f"v7_lane.step: x {bb.xmin:.1f}..{bb.xmax:.1f}, y {bb.ymin:.1f}..{bb.ymax:.1f}, z {bb.zmin:.1f}..{bb.zmax:.1f} mm")
body = v7
# everything ahead of the pin plane goes, except the ears (|y| > 1.6) which are rounded about the pin
body = body.cut(box(-2, PIN_X, -EAR_Y, EAR_Y, -1, 3))
for sgn in (1, -1):
    y0, y1 = (EAR_Y, HW + 0.1) if sgn > 0 else (-HW - 0.1, -EAR_Y)
    ear_cut = box(-2, PIN_X, y0, y1, -1, 3).cut(cyl_y(HUB_R, y0, y1, PIN_X, PIN_Z))
    body = body.cut(ear_cut)
# clearance for the nose's hub and its heel, in the middle
body = body.cut(cyl_y(CLEAR_R, -EAR_Y + 0.05, EAR_Y - 0.05, PIN_X, PIN_Z))
body = body.cut(box(PIN_X - 0.1, HEEL_X + 0.02, -HUB_HW - 0.05, HUB_HW + 0.05, -1, PIN_Z - HUB_R + 0.02))
pin_hole = cyl_y(PIN_D / 2, -HW - 1, HW + 1, PIN_X, PIN_Z)
body = body.cut(pin_hole)

# ---- the nose piece: the lip and face across the full width, the hub, the heel, the toe
tn = math.tan(math.radians(60))
# the face from the lip top up to where it meets the hub circle (tangent is close enough: the face passes 0.15 from the pin
# at x 0.29 by construction of v7), then the circle, then the underside back to the lip's foot
face_top = (0.29, EDGE + 0.29 * tn)                   # (0.29, 1.10)
prof = [(0, BOTTOM), (0, EDGE), face_top, (PIN_X, PIN_Z + HUB_R), (PIN_X, PIN_Z - HUB_R)]
wedge = cq.Workplane("XZ", origin=(0, HW * IN, 0)).polyline([(x * IN, z * IN) for x, z in prof]).close().extrude(W * IN)
hub = cyl_y(HUB_R, -HUB_HW, HUB_HW, PIN_X, PIN_Z)
heel = box(PIN_X, HEEL_X, -HUB_HW, HUB_HW, BOTTOM, PIN_Z - HUB_R + 0.02)
toe = box(-TOE_L, 0.05, -TOE_HW, TOE_HW, BOTTOM - TOE_T, BOTTOM + 0.03)   # overlaps the wedge so the union is one solid
nose = wedge.union(hub).union(heel)
if TOE_L > 0: nose = nose.union(toe)
# outboard, the wedge must clear the body's ears: nothing of the nose within the clearance circle for |y| > 1.55
for sgn in (1, -1):
    y0, y1 = (EAR_Y - 0.05, HW + 0.1) if sgn > 0 else (-HW - 0.1, -EAR_Y + 0.05)
    nose = nose.cut(cyl_y(CLEAR_R, y0, y1, PIN_X, PIN_Z))
nose = nose.cut(pin_hole)
pin = cyl_y(4.0 / IN / 2, -HW - 0.1, HW + 0.1, PIN_X, PIN_Z)

for name, part in (("v10_body", body), ("v10_nose", nose), ("v10_pin", pin)):
    v = part.val(); assert len(part.solids().vals()) == 1 and v.isValid(), name
    cq.exporters.export(part, os.path.join(HERE, "step", name + ".step"))
    cq.exporters.export(part, os.path.join(HERE, "stl", name + ".stl"), tolerance=0.02, angularTolerance=0.1)
    bb = v.BoundingBox(); print(f"{name}: {v.Volume() / 1000:.1f} cm3, {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm")

# ---- the picture: flat, and tripped (the nose turned 54 deg about the pin, the body 0.22 in further in)
import trimesh, matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
def load(n): return trimesh.load(os.path.join(HERE, "stl", n + ".stl"), force="mesh")
mb, mn, mp = load("v10_body"), load("v10_nose"), load("v10_pin")
def show(ax, meshes, elev, azim, title):
    for m, col in meshes:
        tri = m.vertices[m.faces] / IN
        pc = Poly3DCollection(tri, facecolor=col, edgecolor="none", alpha=1.0)
        n = m.face_normals; light = np.array([0.3, -0.5, 0.8]); light /= np.linalg.norm(light)
        shade = 0.55 + 0.45 * np.clip(n @ light, 0, 1)
        import matplotlib.colors as mc
        base = np.array(mc.to_rgb(col)); pc.set_facecolor(np.clip(base * shade[:, None], 0, 1))
        ax.add_collection3d(pc)
    ax.set_xlim(-2, 3.5); ax.set_ylim(-2.75, 2.75); ax.set_zlim(-0.5, 5.0); ax.set_box_aspect((5.5, 5.5, 5.5))
    ax.view_init(elev=elev, azim=azim); ax.set_axis_off(); ax.set_title(title, fontsize=10)
def tripped(m):
    m = m.copy(); a = math.radians(TRIP_DEG)
    T = trimesh.transformations.rotation_matrix(a, [0, 1, 0], point=[PIN_X * IN, 0, PIN_Z * IN])    # nose-up about the pin (the toe is at -x)
    m.apply_transform(T); return m
fig = plt.figure(figsize=(13, 5.5), dpi=140)
ax = fig.add_subplot(1, 2, 1, projection="3d"); show(ax, [(mb, "#2b7de9"), (mn, "#e0552b"), (mp, "#8a94a6")], 28, -35, "v10 flat: the body (blue), the nose piece (orange), the pin")
ax = fig.add_subplot(1, 2, 2, projection="3d")
mb2 = mb.copy(); mb2.apply_translation([-ADVANCE * IN, 0, 0]); mp2 = mp.copy(); mp2.apply_translation([-ADVANCE * IN, 0, 0])
mn2 = tripped(mn); mn2.apply_translation([-ADVANCE * IN, 0, 0])
show(ax, [(mb2, "#2b7de9"), (mn2, "#e0552b"), (mp2, "#8a94a6")], 28, -35, f"tripped: the posts hold the lip, the body drives {ADVANCE} in further, the nose turns {TRIP_DEG}°")
fig.savefig(os.path.join(HERE, "v10.png"), bbox_inches="tight", facecolor="white"); print("v10.png")
