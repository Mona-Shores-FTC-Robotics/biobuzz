"""A 3D look at a layout's left corner: the printed mount, goBILDA's servo (its STEP), the wheels, the intake channel
and the front mecanum, and the arm down. python3 take2/mount_view.py 2a_upright|2b_flat <out.png>"""
import os, sys, numpy as np, matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE); sys.path.insert(0, os.path.dirname(HERE))
import cadquery as cq, layouts as L, geometry as G, check, json   # noqa: E402
name, out = sys.argv[1], sys.argv[2]
boxes, mount = L.LAYOUTS[name]
b = boxes("left")
def mat(M): return cq.Matrix([list(map(float, r)) for r in M[:3]])
servo_step = cq.importers.importStep(check.SERVO_STEP).val()
wx, wy, wz = b["wheel"][1]
if name == "2a_upright":
    lo = b["servo"][1]
    M = np.array([[0, 0, 1, lo[0] + 27.2], [0, -1, 0, wy], [1, 0, 0, wz + 10.0], [0, 0, 0, 1.0]])
else:
    top = b["wheel"][1][2] + b["wheel"][4]
    M = np.array([[-1, 0, 0, wx - 10.0], [0, -1, 0, wy], [0, 0, -1, top + 10.5 + 16.9], [0, 0, 0, 1.0]])
servo = servo_step.transformGeometry(mat(M))
_, c, ax, r, h = b["wheel"]
wheel = cq.Solid.makeCylinder(r, 2 * h, cq.Vector(*(c - ax * h)), cq.Vector(*ax))
channel = cq.Workplane("XY").box(48.2, 12.0, 100.0, centered=False).translate((167.9, 123.8, 72.6)).val()
mec = cq.Solid.makeCylinder(48.0, 38.0, cq.Vector(145.0, 141.0, 48.0), cq.Vector(0, 1, 0))
hj = json.load(open(os.path.join(HERE, name, "clearance.json")))["hump"]
import parts   # noqa: E402
parts._HUMP = (hj["height_mm"], hj["leaves_line_u_mm"], *hj["full_u_mm"], hj["back_on_line_u_mm"])
armenv = check.envelope(False).transformGeometry(mat(check.placement("left", 162.0)))
items = [(channel, "#9aa3ad", .35, "intake channel (grey, holed in real life)"), (mec, "#e0c04a", .25, "front mecanum"),
         (mount("left").val(), "#2a5fd0", .95, "printed mount"), (servo, "#222222", .9, "goBILDA Speed servo"),
         (wheel, "#f08a24", .6, "SWYFT wheel" + ("s" if name == "2a_upright" else " (2.5 in)")),
         (armenv, "#2fa84f", .25, "printed arm, down")]
fig = plt.figure(figsize=(15, 7.5))
for k, (el, az) in enumerate(((22, -50), (18, 35))):
    a3 = fig.add_subplot(1, 2, k + 1, projection="3d")
    for shp, col, al, lab in items:
        v, f = shp.tessellate(0.5, 0.3)
        V = np.array([[p.x, p.y, p.z] for p in v]) / G.IN
        a3.add_collection3d(Poly3DCollection(V[np.array(f)], facecolor=col, alpha=al, edgecolor="none", label=lab))
    a3.set_xlim(5.5, 11.5); a3.set_ylim(3.5, 9.5); a3.set_zlim(0, 6); a3.set_box_aspect((1, 1, 1))
    a3.view_init(el, az); a3.set_xlabel("X forward, in"); a3.set_ylabel("Y left, in"); a3.set_zlabel("up, in")
    if k == 0:
        from matplotlib.patches import Patch
        a3.legend(handles=[Patch(color=cc, label=ll) for _, cc, _, ll in items], loc="upper left", fontsize=8)
fig.suptitle(f"{name}: the left corner, mount in blue (bolts into the channel's outer face; the servo's ears screw to its front plate)")
plt.tight_layout(); plt.savefig(out, dpi=100)
