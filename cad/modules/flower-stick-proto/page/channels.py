"""The intake's two front channels from the export, as meshes in the face frame (ahead, left, up; inches), to see where
their holes really are."""
import sys, re, json, importlib.util, numpy as np, trimesh
sys.path.insert(0, '/home/user/biobuzz/cad/full-robot')
import cadquery as cq
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
FR = load('fr', '/home/user/biobuzz/cad/full-robot/build.py')
C, F, FACE, IN = FR.C, FR.F, FR.FACE, FR.IN
FLOOR_U, FACE_L, ACROSS0 = 4.75, 5.35, 2.21
out = {}
for path, shp, loc, col, key in FR.placed_team(sys.argv[1], raw=True):
    p = " / ".join(path)
    if not (p.startswith("Chassis") and "Intake" in p and (path[-1].startswith("9 Hole Lowside U-Channel") or path[-1].startswith("11 Hole Mini U-Channel") or path[-1].startswith("5000-0002-0001 body"))): continue
    try: v, f = cq.Shape.cast(shp.wrapped.Moved(loc)).tessellate(0.05, 0.2)
    except Exception as e: print("skip", p, e); continue
    v = np.array([(q.x, q.y, q.z) for q in v])
    # list frame: f = (z - (FACE - 7.56 IN)) / IN, l = (x - C) / IN, u = (y - F) / IN ; face frame: ahead = l - 5.35, left = 2.21 - f, up = u - 4.75
    fwd = (v[:, 2] - (FACE - 7.56 * IN)) / IN; l = (v[:, 0] - C) / IN; u = (v[:, 1] - F) / IN
    m = trimesh.Trimesh(np.c_[l - FACE_L, ACROSS0 - fwd, u - FLOOR_U], f, process=False)
    name = re.sub(r"[^A-Za-z0-9]+", "_", path[-1])[:40]; k = name; i = 2
    while k in out: k = f"{name}_{i}"; i += 1
    out[k] = m; m.export(f"chan_{k}.stl"); print("ok", k, m.bounds.round(2).tolist(), flush=True)
print("done", len(out))
