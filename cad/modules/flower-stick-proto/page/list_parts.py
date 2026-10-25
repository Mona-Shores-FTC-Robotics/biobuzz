"""List the mentor's new Robot.step in the robot frame (inches: +X forward, +Y left, +Z up, origin under the chassis
centre at the floor): every leaf part's path and bounds, grouped by top-level assembly. Writes parts.pkl for later."""
import sys, os, re, pickle, importlib.util, numpy as np
sys.path.insert(0, '/home/user/biobuzz/cad/full-robot')
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
FR = load('fr', '/home/user/biobuzz/cad/full-robot/build.py')
C, F, FACE, IN = FR.C, FR.F, FR.FACE, FR.IN
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
rows = []
for path, shp, loc, col, key in FR.placed_team(sys.argv[1], raw=True):
    b = Bnd_Box(); BRepBndLib.Add_s(shp.wrapped.Moved(loc), b)
    if b.IsVoid(): continue
    x0, y0, z0, x1, y1, z1 = b.Get()
    # STEP mm -> robot inches: forward = (z - (FACE - 7.56 in)) / IN, left = (x - C) / IN, up = (y - F) / IN
    fwd = ((z0 - (FACE - 7.56 * IN)) / IN, (z1 - (FACE - 7.56 * IN)) / IN)
    left = ((x0 - C) / IN, (x1 - C) / IN)
    up = ((y0 - F) / IN, (y1 - F) / IN)
    rows.append((path, fwd, left, up))
pickle.dump(rows, open("parts.pkl", "wb"))
print(len(rows), "parts; align", FR.placed_team.align)
tops = {}
for path, fwd, left, up in rows:
    tops.setdefault(path[0], []).append((path, fwd, left, up))
for t, ps in sorted(tops.items(), key=lambda kv: -len(kv[1])):
    fx = (min(p[1][0] for p in ps), max(p[1][1] for p in ps)); ly = (min(p[2][0] for p in ps), max(p[2][1] for p in ps)); uz = (min(p[3][0] for p in ps), max(p[3][1] for p in ps))
    print(f"{len(ps):4d} {t[:60]:60s} fwd {fx[0]:6.2f}..{fx[1]:6.2f}  left {ly[0]:6.2f}..{ly[1]:6.2f}  up {uz[0]:6.2f}..{uz[1]:6.2f}")
