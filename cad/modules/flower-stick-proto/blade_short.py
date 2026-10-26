"""The wide mountain blade with its tail cut off 2.0 in behind the lip (page/tailcut_tall.js: the model feeds every case at
every speed on the coach's arms down to 2.0; 1.6 starts dropping the slow ones). Cut from the printed mesh of mountain_wide
itself (stl/mountain_wide.stl), so the bore is the same 7.15 mm hex that keys to the REX shaft. (The first version was cut from
step/mountain_wide.step, whose bore is the loose 8.6 mm hex meant for the eared blade: it dropped straight through the shaft.)
-> stl/mountain_wide_2in.stl

    TAIL=2.0 python3 cad/modules/flower-stick-proto/blade_short.py
"""
import os, numpy as np, trimesh
HERE = os.path.dirname(os.path.abspath(__file__)); IN = 25.4
TAIL = float(os.environ.get("TAIL", "2.0")); NAME = f"mountain_wide_{TAIL:g}in".replace(".", "p")
m = trimesh.load(os.path.join(HERE, "stl", "mountain_wide.stl"))
lip_x = m.bounds[1][0]                                           # the lip is at the mesh's +x end, the tail at x = 0
cut = m.slice_plane([lip_x - TAIL * IN, 0, 0], [1, 0, 0], cap=True)
assert cut.is_watertight and cut.is_volume, "the cut mesh is not closed"
cut.export(os.path.join(HERE, "stl", NAME + ".stl"))
b = cut.bounds[1] - cut.bounds[0]
sec = cut.section(plane_origin=cut.centroid, plane_normal=[0, 1, 0]); p, _ = sec.to_2D()
holes = [(round(np.ptp(np.array(r.coords)[:, 0]), 2), round(np.ptp(np.array(r.coords)[:, 1]), 2)) for poly in p.polygons_full for r in poly.interiors]
print(f"{NAME}: {b[0]:.0f} x {b[1]:.0f} x {b[2]:.0f} mm, {cut.volume / 1000:.0f} cm3; bore (wide x tall, mm) {holes}")
