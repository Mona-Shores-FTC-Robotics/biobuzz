"""The front of the October 25 robot CAD as one mesh (the robot's frame: inches ahead of the intake face, left, up),
limited to parts within 7 in behind the face, |y| < 7, under 12 in up: for a look at the mentor's flower mechanism."""
import sys, os, re, json, importlib.util, numpy as np, trimesh
sys.path.insert(0, '/home/user/biobuzz/cad/full-robot')
import cadquery as cq
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
FR = load('fr', '/home/user/biobuzz/cad/full-robot/build.py')
C, F, FACE, IN = FR.C, FR.F, FR.FACE, FR.IN
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
FLOOR_UP, FACE_FWD = 18.25, 5.65
skip = re.compile(r"[Ss]crew|Nut|Washer|Rivet|shim|e-clip|pins|Standoff|Snap Ring")
meshes, names = [], []
for path, shp, loc, col, key in FR.placed_team(sys.argv[1], raw=True):
    p = " / ".join(path)
    if skip.search(p) or ("Launcher Concept <1>" in p and "V2" not in p): continue
    b = Bnd_Box(); BRepBndLib.Add_s(shp.wrapped.Moved(loc), b)
    if b.IsVoid(): continue
    x0, y0, z0, x1, y1, z1 = b.Get()
    ahead = (FACE_FWD - (z1 - (FACE - 7.56 * IN)) / IN, FACE_FWD - (z0 - (FACE - 7.56 * IN)) / IN); left = ((x0 - C) / IN, (x1 - C) / IN); up = (FLOOR_UP - (y1 - F) / IN, FLOOR_UP - (y0 - F) / IN)
    if ahead[1] < -7 or left[0] > 7 or left[1] < -7 or up[0] > 12: continue
    try:
        v, f = cq.Shape.cast(shp.wrapped.Moved(loc)).tessellate(0.5, 0.8)
    except Exception as e:
        print("skip", p[-60:], e, flush=True); continue
    if not f: continue
    print("ok", len(meshes), p[-60:], flush=True)
    v = np.array([(q.x, q.y, q.z) for q in v])
    fwd = (v[:, 2] - (FACE - 7.56 * IN)) / IN; lf = (v[:, 0] - C) / IN; upv = (v[:, 1] - F) / IN
    m = trimesh.Trimesh(np.c_[FACE_FWD - fwd, lf, FLOOR_UP - upv], f, process=False)     # (ahead, left, z)
    grp = "intake" if "Intake" in p else "launcher" if "Launcher" in p else "chassis" if "Chassis" in p else "loose"
    meshes.append((grp, m)); names.append((grp, p, [round(x, 2) for x in ahead + left + up]))
print(len(meshes), "parts")
for grp in ("intake", "chassis", "launcher", "loose"):
    ms = [m for g, m in meshes if g == grp]
    if ms: trimesh.util.concatenate(ms).export(f"front_{grp}.stl"); print(grp, len(ms), sum(len(m.faces) for m in ms), "faces")
json.dump(names, open("front_names.json", "w"))
