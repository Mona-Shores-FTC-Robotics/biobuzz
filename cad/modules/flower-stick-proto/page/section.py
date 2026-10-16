"""Side-view section of the mentor's 16 Oct robot's front (the intake) at the centreline, as polylines in a frame of
inches behind the intake uprights' front faces (x) and height off the floor (z). Writes robot_section.json."""
import sys, os, re, json, importlib.util, numpy as np, trimesh
sys.path.insert(0, '/home/user/biobuzz/cad/full-robot')
import cadquery as cq
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
FR = load('fr', '/home/user/biobuzz/cad/full-robot/build.py')
C, F, FACE, IN = FR.C, FR.F, FR.FACE, FR.IN
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
parts = FR.placed_team(sys.argv[1], raw=True)
# frame: the export is turned 180 deg about the left-right axis; floor = drive wheels' bottoms ('up' 18.25 in the raw
# frame), the uprights' front at raw 'fwd' 5.65 (see list_parts.py)
FLOOR_UP, FACE_FWD = 18.25, 5.65
def raw(shp, loc):
    v, f = cq.Shape.cast(shp.wrapped.Moved(loc)).tessellate(0.15, 0.3)
    if not f: return None
    v = np.array([(p.x, p.y, p.z) for p in v])
    fwd = (v[:, 2] - (FACE - 7.56 * IN)) / IN; left = (v[:, 0] - C) / IN; up = (v[:, 1] - F) / IN
    return trimesh.Trimesh(np.c_[fwd - FACE_FWD, left, FLOOR_UP - up], f, process=False)   # (behind the face, left, height)
sections = {"centre": [], "gecko": []}
n = 0
for path, shp, loc, col, key in parts:
    p = " / ".join(path)
    if re.search(r"[Ss]crew|Nut|Washer|2800|2802|2812|text|e-clip|shim|Bearing|Spacer|pins|encoder", p): continue
    b = Bnd_Box(); BRepBndLib.Add_s(shp.wrapped.Moved(loc), b)
    if b.IsVoid(): continue
    x0, y0, z0, x1, y1, z1 = b.Get()
    left = ((x0 - C) / IN, (x1 - C) / IN); behind = ((z0 - (FACE - 7.56 * IN)) / IN - FACE_FWD, (z1 - (FACE - 7.56 * IN)) / IN - FACE_FWD)
    if behind[0] > 11.0 or behind[1] < -2.0: continue
    cut_centre = left[0] <= 0.0 <= left[1]
    cut_gecko = "Gecko" in p and left[0] <= -0.25 <= left[1]
    if not (cut_centre or cut_gecko): continue
    m = raw(shp, loc)
    if m is None: continue
    for label, y in (("centre", 0.0), ("gecko", -0.25)):
        if (label == "centre" and not cut_centre) or (label == "gecko" and not cut_gecko): continue
        try: lines = trimesh.intersections.mesh_plane(m, [0, 1, 0], [0, y, 0])
        except Exception: continue
        for a, c in lines:
            sections[label].append([[round(float(a[0]), 3), round(float(a[2]), 3)], [round(float(c[0]), 3), round(float(c[2]), 3)]])
    n += 1
print(n, "parts cut;", {k: len(v) for k, v in sections.items()}, "segments")
json.dump(sections, open("robot_section.json", "w"))
