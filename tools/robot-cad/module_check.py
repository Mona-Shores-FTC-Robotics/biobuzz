# Run from a scratch folder: python3 module_check.py <mentor's Robot.step> [module]
# Clash check for an add-on module on the mentor's robot as he drew it: every part of his, lined up by his intake's front
# uprights and NOT edited (nothing left out or moved), against the module's parts. Our full robot moves his launcher
# forward (cad/transfer LAUNCHER_SHIFT); his doesn't, so a module drawn on his launcher is moved back by that much.
# Modules: "turret" (the servo turret drive and its two encoders, cad/transfer's turret_* parts and their screws).
# Exact mesh intersection (manifold3d), inches, robot frame. Caches raw_base_mesh.pkl here.
import sys, os, re, pickle, importlib.util, numpy as np, trimesh, manifold3d as mf
import cadquery as cq
sys.path.insert(0, '/home/user/biobuzz/cad/full-robot')
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
FR = load('fr', '/home/user/biobuzz/cad/full-robot/build.py'); TR = FR.TR
C, F, FACE, IN = FR.C, FR.F, FR.FACE, FR.IN
def cad_mesh(shape):
    v, f = shape.tessellate(0.3, 0.5)
    if not f: return None
    v = np.array([(p.x, p.y, p.z) for p in v])
    return trimesh.Trimesh(np.c_[(v[:, 2] - (FACE - 7.56 * IN)) / IN, (v[:, 0] - C) / IN, (v[:, 1] - F) / IN], f, process=True)
cache = 'raw_base_mesh.pkl'
if os.path.exists(cache): base = pickle.load(open(cache, 'rb'))
else:
    base = []
    for path, shp, loc, col, key in FR.placed_team(sys.argv[1], raw=True):
        p = ' / '.join(path)
        if re.search(r'Screw|screw|Nut|2800-|2802-|2829-|CAGE|Washer|text', p): continue
        m = cad_mesh(cq.Shape.cast(shp.wrapped.Moved(loc)))
        if m is not None: base.append((p, m))
    pickle.dump(base, open(cache, 'wb'))
module = sys.argv[2] if len(sys.argv) > 2 else 'turret'
MODULES = {'turret': (r'^(turret_|screw_turret_|nut_turret_)', TR.launcher, -TR.LAUNCHER_SHIFT)}
rx, group, dx = MODULES[module]
kit = {}
for n, (wp, col, kind) in group.items():
    if not re.search(rx, n): continue
    m = cad_mesh(TR.to_cad(wp))
    if m is None: continue
    m.apply_translation((dx, 0, 0)); kit[n] = m
lo = np.min([m.bounds[0] for m in kit.values()], 0) - 1; hi = np.max([m.bounds[1] for m in kit.values()], 0) + 1
near = [(p, m) for p, m in base if not ((m.bounds[1] < lo).any() or (m.bounds[0] > hi).any())]
print(f'{module} module: {len(kit)} parts, {len(near)} of his parts nearby (box X {lo[0]:.2f}..{hi[0]:.2f}, Y {lo[1]:.2f}..{hi[1]:.2f}, z {lo[2]:.2f}..{hi[2]:.2f})')
def man(m): return mf.Manifold(mf.Mesh(vert_properties=np.asarray(m.vertices, np.float32), tri_verts=np.asarray(m.faces, np.uint32)))
def vol(a, b):
    if (a.bounds[1] < b.bounds[0]).any() or (b.bounds[1] < a.bounds[0]).any(): return 0.0
    try: return (man(a) ^ man(b)).volume()
    except Exception: return -1
TOL = 2e-4
hits = 0
for n, m in kit.items():
    for p, b in near:
        v = vol(m, b)
        if v > TOL or v < 0:
            hits += 1; print(f'  {v:8.4f} in3  {n[:55]:55s} x {p.split(" / ")[-1][:50]}')
# what of his sits where the module goes: his turret drive (motor, gear) that the servo replaces
print('--- his parts within 1 in of the drive gear axis (what the kit replaces or meets)')
tg = np.array([TR.TG[0] + dx, TR.TG[1]])
for p, b in near:
    c = (b.bounds[0][:2] + b.bounds[1][:2]) / 2
    if np.linalg.norm(c - tg) < 1.0: print(f'  {p[-80:]}  z {b.bounds[0][2]:.2f}..{b.bounds[1][2]:.2f}')
print(f'{hits} clashes')
