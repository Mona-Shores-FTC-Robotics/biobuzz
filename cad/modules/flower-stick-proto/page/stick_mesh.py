"""The mentor's flower stick from the October 25 CAD: the arms, the servo, the shaft across the tips and the printed
engagement part, as meshes in the robot's frame (inches ahead of the intake face, left, up)."""
import sys, re, json, importlib.util, numpy as np, trimesh
sys.path.insert(0, '/home/user/biobuzz/cad/full-robot')
import cadquery as cq
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
FR = load('fr', '/home/user/biobuzz/cad/full-robot/build.py')
C, F, FACE, IN = FR.C, FR.F, FR.FACE, FR.IN
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
FLOOR_UP, FACE_FWD = 18.25, 5.65
want = re.compile(r"SWYFT|Part 4$|312mm Steel Shaft|3400-0014-0032|11 Hole Mini U-Channel")
out = {}
for path, shp, loc, col, key in FR.placed_team(sys.argv[1], raw=True):
    p = " / ".join(path)
    if not want.search(p) or "Chassis" in p and "Mini U-Channel" in p: continue
    b = Bnd_Box(); BRepBndLib.Add_s(shp.wrapped.Moved(loc), b)
    if b.IsVoid(): continue
    x0, y0, z0, x1, y1, z1 = b.Get()
    up = (FLOOR_UP - (y1 - F) / IN, FLOOR_UP - (y0 - F) / IN)
    if "Mini U-Channel" in p and up[1] < 15: continue
    try: v, f = cq.Shape.cast(shp.wrapped.Moved(loc)).tessellate(0.1, 0.3)
    except Exception as e: print("skip", p, e); continue
    v = np.array([(q.x, q.y, q.z) for q in v])
    m = trimesh.Trimesh(np.c_[FACE_FWD - (v[:, 2] - (FACE - 7.56 * IN)) / IN, (v[:, 0] - C) / IN, FLOOR_UP - (v[:, 1] - F) / IN], f, process=False)
    name = re.sub(r"[^A-Za-z0-9]+", "_", path[-1])[:40]; k = name; i = 2
    while k in out: k = f"{name}_{i}"; i += 1
    out[k] = m; print("ok", k, (m.bounds).round(2).tolist(), flush=True)
for k, m in out.items(): m.export(f"stick_{k}.stl")
json.dump({k: m.bounds.tolist() for k, m in out.items()}, open("stick_parts.json", "w"))
print("done", len(out))
