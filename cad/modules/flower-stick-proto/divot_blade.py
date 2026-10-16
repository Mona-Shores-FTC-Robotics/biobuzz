"""The divot blade: the students' 15 Oct refinement of the FLOWER block (issue #179).

Testing on 15 Oct found that one 2.72 in convex block is hard to get under the POLLEN: it has to enter the FLOWER's
2.79 in hole, so it has to be centred to about 1/32 in. Two blocks side by side worked every time, because the POLLEN sat
in the valley between their two curved fronts, which centred it on the blocks. This part makes that deliberate: one wide
blade with a concave divot in the middle of its front, sized to the POLLEN, so the POLLEN seats in the divot and the
blade doesn't need to enter the hole at all. The side profile is the tested block's: bottom 0.52 in off the tiles, top
1.30, a flat top, then a slope down to the back edge that the POLLEN roll out over.

    python3 cad/modules/flower-stick-proto/divot_blade.py                 # the defaults: 5.4 in wide, shaft at 0.77 in
    python3 cad/modules/flower-stick-proto/divot_blade.py --width 4.5 --shaft 0.945 --bottom 0.70

Writes stl/divot_blade_w<width>_shaft<height>.stl, flat side down, millimetres, plus a PNG of three views.
"""
import argparse, math, os
import numpy as np, trimesh
from trimesh.creation import box, cylinder, revolve

HERE = os.path.dirname(os.path.abspath(__file__))
IN = 25.4

ap = argparse.ArgumentParser()
ap.add_argument("--width", type=float, default=5.4, help="overall width, inches (two of the tested blocks side by side: 5.44)")
ap.add_argument("--depth", type=float, default=1.6, help="shoulders' front edge to the back edge, inches")
ap.add_argument("--flat", type=float, default=0.6, help="flat top behind the front, inches, before the slope down to the back")
ap.add_argument("--bottom", type=float, default=0.52, help="blade bottom above the tiles, inches")
ap.add_argument("--top", type=float, default=1.30, help="blade top above the tiles, inches (under a POLLEN's centre, 1.4)")
ap.add_argument("--shaft", type=float, default=0.77, help="shaft centre above the tiles, inches (the arm wheels' radius)")
ap.add_argument("--divot-r", type=float, default=1.45, help="divot radius in plan, inches (a POLLEN is 1.4)")
ap.add_argument("--divot-chord", type=float, default=1.8, help="divot width across the front, inches")
ap.add_argument("--slant", type=float, default=0.2, help="the front leans back this much at the top, inches")
ap.add_argument("--hex", type=float, default=8.3, help="hex bore across the flats, mm (8 mm hex shaft)")
a = ap.parse_args()

W, D, F = a.width * IN, a.depth * IN, a.flat * IN
H = (a.top - a.bottom) * IN
R, chord = a.divot_r * IN, a.divot_chord * IN
divot_depth = R - math.sqrt(R * R - (chord / 2) ** 2)
ROD_BACK = 12.0                                  # shaft centre behind the divot's deepest point, as the tested block
rod_x = D - divot_depth - ROD_BACK                # from the back edge
rod_z = (a.shaft - a.bottom) * IN
hex_r = a.hex / 2 / math.cos(math.pi / 6)
LAND = 0.12 * IN                                  # the front is vertical for this much at the bottom, then leans back

# side profile: back edge at x 0, front at x D; flat top from D-F to D, slope from the back edge up to it
side = [(0, 0), (D - F, H), (D, H), (D, 0)]
body = trimesh.creation.extrude_polygon(trimesh.path.polygons.Polygon(side), W)
body.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))   # (x, y, z) -> (x, -z, y): y runs -W..0
body.apply_translation([0, W, 0])                                                         # ...so now 0..W
# the shoulders' front leans back SLANT at the top: cut with a wedge
lean = a.slant * IN
wedge = trimesh.creation.extrude_polygon(trimesh.path.polygons.Polygon([(D + 1, LAND), (D + 1, H + 1), (D - lean * (H + 1 - LAND) / (H - LAND), H + 1)]), W + 2)
wedge.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0])); wedge.apply_translation([0, W + 1, 0])
body = body.difference(wedge)
# the divot: a cone-ish cut, radius R at the bottom land, R + lean at the top, so it leans back like the shoulders
cut = revolve([[0, -1.0], [R, -1.0], [R, LAND], [R + lean, H + 1], [0, H + 1]], sections=160)
cut.apply_translation([D - divot_depth + R, W / 2, 0])
body = body.difference(cut)
# the hex bore, flats top and bottom
bore = cylinder(radius=hex_r, height=W + 4, sections=6)
bore.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
bore.apply_translation([rod_x, W / 2, rod_z])
body = body.difference(bore)

under = rod_z - a.hex / 2
top_at_rod = H if rod_x >= D - F else H * rod_x / (D - F)
over = top_at_rod - a.hex / 2
ahead = D - divot_depth - rod_x - hex_r
if under < 2 or over < 3 or ahead < 3:
    raise SystemExit(f"bore too close to a face: {under:.1f} mm under, {over:.1f} over, {ahead:.1f} ahead of the divot")
assert body.is_watertight
os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
name = f"divot_blade_w{a.width:.1f}_shaft{a.shaft:.3f}"
body.export(os.path.join(HERE, "stl", name + ".stl"))
print(f"stl/{name}.stl: {W:.0f} x {D:.0f} x {H:.0f} mm; divot {a.divot_chord:.2f} in wide, {divot_depth / IN:.2f} in deep; "
      f"bore {under:.1f} mm under, {over:.1f} over, {ahead:.1f} ahead of the divot; {body.volume / 1000:.0f} cm3")

# three views
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
m = body.copy(); m.apply_translation(-m.bounds[0]); v = m.vertices / IN; f = m.faces
L = np.array([0.3, -0.6, 0.75]); L /= np.linalg.norm(L); k = 0.35 + 0.65 * np.clip(m.face_normals @ L, 0, 1)
col = np.c_[np.outer(k, [0.95, 0.55, 0.12]), np.ones(len(k))]
fig = plt.figure(figsize=(14, 5))
for i, (el, az, t) in enumerate(((28, -55, "3D: the divot in the front, the slope behind"), (0, -90, "Side: hex bore, flat top, slope"), (90, -90, "Top: the divot takes the POLLEN"))):
    ax = fig.add_subplot(1, 3, i + 1, projection="3d")
    ax.add_collection3d(Poly3DCollection(v[f], facecolors=col, edgecolors="none"))
    e = v.max(0); ax.set_xlim(0, e[0]); ax.set_ylim(0, e[1]); ax.set_zlim(0, e[2]); ax.set_box_aspect(e)
    ax.view_init(el, az); ax.set_title(t, fontsize=10); ax.tick_params(labelsize=7)
e = v.max(0)
fig.suptitle(f"{name}: {e[1]:.2f} in wide, {e[0]:.2f} in front to back, {e[2]:.2f} in tall; bottom {a.bottom:.2f} in, top {a.top:.2f}", fontsize=11)
fig.tight_layout(); fig.savefig(os.path.join(HERE, name + ".png"), dpi=110)
