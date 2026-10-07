# Run from a scratch folder: python3 transfer2_check.py <mentor's Robot.step>. Caches base_mesh.pkl there.
# Clash check: transfer v2 + our front vs the mentor's new Robot.step (aligned and edited as cad/full-robot does), and
# NECTAR/POLLEN swept along the lane and popped from the cup. Exact mesh intersection (manifold3d). Inches, robot frame.
import sys, os, re, math, pickle, importlib.util, numpy as np, trimesh
sys.path.insert(0, '/home/user/biobuzz/cad/full-robot')
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
FR = load('fr', '/home/user/biobuzz/cad/full-robot/build.py'); TR = FR.TR; IB = FR.IB
C, F, FACE, IN = FR.C, FR.F, FR.FACE, FR.IN
import cadquery as cq
from OCP.TopLoc import TopLoc_Location
from OCP.gp import gp_Trsf, gp_Vec
def cad_mesh(shape):
    v, f = shape.tessellate(0.3, 0.5)
    if not f: return None
    v = np.array([(p.x, p.y, p.z) for p in v])
    return trimesh.Trimesh(np.c_[(v[:, 2] - (FACE - 7.56 * IN)) / IN, (v[:, 0] - C) / IN, (v[:, 1] - F) / IN], f, process=True)
cache = 'base_mesh.pkl'
if os.path.exists(cache): base = pickle.load(open(cache, 'rb'))
else:
    leaves, bs = FR.read_team(sys.argv[1])
    from OCP.Bnd import Bnd_Box
    from OCP.BRepBndLib import BRepBndLib
    def bbox(k, l):
        b = Bnd_Box(); BRepBndLib.Add_s(bs[k].wrapped.Moved(l), b); return b.Get()
    rails = [bbox(k, l) for p, k, l, c in leaves if p and p[-1].startswith('1107-0015-0384')]
    dz, dx = FACE - max(b[5] for b in rails), (C + 136.0) - max(b[3] for b in rails)
    def mv(v): t = gp_Trsf(); t.SetTranslation(gp_Vec(*v)); return TopLoc_Location(t)
    base = []
    for path, k, loc, col in leaves:
        p = ' / '.join(path)
        if FR.SKIP.search(p) or re.search(r'Screw|screw|Nut|2800-|2802-|2829-|CAGE|Washer|text', p): continue
        loc = mv((dx, 0, dz)).Multiplied(loc)
        if FR.RAISE.search(p): loc = mv((0, TR.CHAN_RAISE * IN, 0)).Multiplied(loc)
        b = bbox(k, loc)
        xm = ((b[2] + b[5]) / 2 - (FACE - 7.56 * IN)) / IN; ym = ((b[0] + b[3]) / 2 - C) / IN
        if FR.FRONT_OF_LAUNCHER.search(p) and xm > -2.0:
            if 'U Beam' in p: loc = mv((math.copysign(FR.WIDEN_IN * IN, ym), 0, 0)).Multiplied(loc); b = bbox(k, loc)
            else: continue
        # only what's near the lane
        X0, X1 = (b[2] - (FACE - 7.56 * IN)) / IN, (b[5] - (FACE - 7.56 * IN)) / IN
        Y0, Y1 = (b[0] - C) / IN, (b[3] - C) / IN; Z0, Z1 = (b[1] - F) / IN, (b[4] - F) / IN
        if X1 < -8 or X0 > 9.5 or Y1 < -5 or Y0 > 5 or Z0 > 9: continue
        m = cad_mesh(cq.Shape.cast(bs[k].wrapped.Moved(loc)))
        if m is not None: base.append((p, m))
    pickle.dump(base, open(cache, 'wb'))
print(len(base), 'mentor parts near the lane')
def mesh_of(wp, to_cad=True):
    shp = TR.to_cad(wp) if to_cad else (wp.val() if hasattr(wp, 'val') else wp)
    return cad_mesh(shp)
tr = {n: mesh_of(wp) for n, (wp, col, kind) in TR.fixed.items()}
front = {}
for title, g, d in IB.GROUPS:
    for n, (wp, col, kind) in d.items():
        if 'STAND-IN' in n: continue
        m = cad_mesh(wp.val() if hasattr(wp, 'val') else wp)
        if m is not None: front[n] = m
import manifold3d as mf
def man(m): return mf.Manifold(mf.Mesh(vert_properties=np.asarray(m.vertices, np.float32), tri_verts=np.asarray(m.faces, np.uint32)))
def vol(a, b):
    if (a.bounds[1] < b.bounds[0]).any() or (b.bounds[1] < a.bounds[0]).any(): return 0.0
    try: return (man(a) ^ man(b)).volume()
    except Exception: return -1
OK_TOUCH = re.compile(r'1107-0015-0384')   # brackets bolt to the rails
print('--- transfer vs mentor robot')
for n, m in tr.items():
    for p, bm in base:
        v = vol(m, bm)
        if v > 1e-4 and not (('bracket' in n) and OK_TOUCH.search(p)): print(f'{v:8.4f} in3  {n[:50]:50s} x {p[-60:]}')
print('--- transfer vs our front')
for n, m in tr.items():
    for fn, fm in front.items():
        v = vol(m, fm)
        if v > 1e-4: print(f'{v:8.4f} in3  {n[:50]:50s} x {fn[:50]}')
print('--- transfer parts vs each other (feeders/wheels/walls)')
names = list(tr)
for i in range(len(names)):
    for j in range(i + 1, len(names)):
        v = vol(tr[names[i]], tr[names[j]])
        a, b = names[i], names[j]
        pair = a + ' | ' + b
        if re.search(r'shaft|jackshaft', a + b) and re.search(r'wheels|pulley|feeder_(front|rear) |miter|feeder_jackshaft.*pulley', pair) and (a.split(' ')[0].split('_')[-1] == b.split(' ')[0].split('_')[-1] or 'jackshaft' in pair or 'miter' in pair): continue
        if re.search(r'belt', pair) and re.search(r'pulley|jackshaft|shaft', pair): continue
        if v > 2e-3: print(f'{v:8.4f} in3  {a[:45]:45s} x {b[:45]}')
# ball sweeps
print('--- balls along the lane and up from the cup')
everything = [(p, m) for p, m in base] + [('front: ' + n, m) for n, m in front.items()] + [('transfer: ' + n, m) for n, m in tr.items()]
def sweep(R, pts, label, ignore=re.compile(r'^$')):
    hits = {}
    for (x, z) in pts:
        s = trimesh.creation.icosphere(subdivisions=3, radius=R - 0.02); s.apply_translation((x, TR.LANE_Y, z))
        for p, m in everything:
            if ignore.search(p): continue
            v = vol(s, m)
            if v > 2e-3: hits[p] = max(hits.get(p, 0), v)
    for p, v in sorted(hits.items(), key=lambda kv: -kv[1]): print(f'  {label}: {v:7.4f} in3  {p[-70:]}')
    if not hits: print(f'  {label}: clear')
for R, nm in ((TR.RN, 'NECTAR'), (TR.RP, 'POLLEN')):
    lane = [(x, TR.line(x) + R + 0.02) for x in np.linspace(TR.LANE_TOP[0] + 0.3, 5.6, 24)]
    sweep(R, lane, nm + ' on the lane', re.compile(r'ceiling|lane_wheels|feeder_front|ramp'))
    seat = TR.SEAT_N if R > 1.6 else TR.SEAT_P
    up = [(TR.CUP_X, z) for z in np.linspace(seat + 0.02, 5.7, 12)]
    sweep(R, up, nm + ' popped up', re.compile(r'feeder_(front|rear)\b|96mm Gecko'))
