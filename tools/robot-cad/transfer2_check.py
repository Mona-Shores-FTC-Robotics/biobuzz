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
    base = []
    for path, shp, loc, col, key in FR.placed_team(sys.argv[1]):     # the mentor's parts, lined up and edited as the full STEP has them
        p = ' / '.join(path)
        if re.search(r'Screw|screw|Nut|2800-|2802-|2829-|CAGE|Washer|text', p): continue
        m = cad_mesh(cq.Shape.cast(shp.wrapped.Moved(loc)))
        if m is None: continue
        lo, hi = m.bounds
        if hi[0] < -8 or lo[0] > 9.5 or hi[1] < -5 or lo[1] > 5 or lo[2] > 9: continue
        base.append((p, m))
    pickle.dump(base, open(cache, 'wb'))
print(len(base), 'mentor parts near the lane')
def mesh_of(wp, to_cad=True):
    shp = TR.to_cad(wp) if to_cad else (wp.val() if hasattr(wp, 'val') else wp)
    return cad_mesh(shp)
tr = {n: mesh_of(wp) for n, (wp, col, kind) in list(TR.fixed.items()) + list(TR.launcher.items())}
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
EXPECTED = re.compile(r'(feeder_shaft.*Lowside U-Channel)|(flywheel_belt.*41T)|(feeder_shaft.*launcher_front_channel)')   # shafts in their channels' bearings; belts on their pulleys
print('--- transfer vs mentor robot')
for n, m in tr.items():
    for p, bm in base:
        v = vol(m, bm)
        if v > 1e-4 and not (('bracket' in n) and OK_TOUCH.search(p)) and not EXPECTED.search(n + ' ' + p): print(f'{v:8.4f} in3  {n[:50]:50s} x {p[-60:]}')
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
        if re.search(r'belt|cord', pair) and re.search(r'pulley|jackshaft|shaft', pair): continue
        if re.search(r'pad_hinge', pair) or re.search(r'feeder_shaft.*(feeder_pulley|launcher_front_channel)|feeder \(.*feeder_shaft', pair): continue
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
    z = TR.FLOOR_Z + R + 0.02
    lane = [(x, z) for x in np.linspace(max(TR.COL_X, TR.BACKSTOP_X + R + 0.01), 5.6, 26)]
    sweep(R, lane, nm + ' along the lane into the feeder', re.compile(r'ceiling|lane_wheels|transfer: feeder \(|pad_(foam|plate)|feeder_floor \('))
    x_rest = max(TR.COL_X, TR.BACKSTOP_X + R + 0.01)     # it rests against the backstop if that's ahead of the column's centre
    up = [(x_rest, zz) for zz in np.linspace(z, 6.2, 14)]
    sweep(R, up, nm + ' driven up the column', re.compile(r'transfer: feeder \(|pad_(foam|plate)|96mm Gecko|feeder_floor \('))

# the pad swung back for a NECTAR: the plate and foam turn about the hinge (along X) until the face is 0.77 further out
print('--- pad swung back (NECTAR) vs the robot, our front and the rest of the transfer')
import trimesh.transformations as tt
hy, hz = TR.PAD_HINGE
ang = math.atan2(0.77, 3.0 - hz)                       # 0.77 at the ball's contact height (about z 3.0)
R_ = tt.rotation_matrix(-ang, [1, 0, 0], [0, hy, hz])   # -angle about +X moves the pad's top toward -Y
rest = [(n, m) for n, m in tr.items() if not n.startswith('pad_')]
for n in [k for k in tr if k.startswith('pad_plate') or k.startswith('pad_foam')]:
    m = tr[n].copy(); m.apply_transform(R_)
    for p, bm in base + [('front: ' + k, v) for k, v in front.items()] + [('transfer: ' + k, v) for k, v in rest]:
        v = vol(m, bm)
        if v > 1e-4: print(f'{v:8.4f}  {n[:40]} x {p[-60:]}')
    print('  swung', n[:30], 'Y', m.bounds[:, 1].round(2), 'z', m.bounds[:, 2].round(2))
