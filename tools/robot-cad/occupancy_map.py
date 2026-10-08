# Occupancy grid of the whole robot (inches, robot frame), with moving parts swept and the balls' paths reserved.
import pickle, numpy as np, math, re, importlib.util, trimesh
import os
S = os.environ.get('OCC_DIR', '.') + '/'   # holding robot2/keep5.pkl, addons/{front,transfer,vendor}_mesh.pkl; writes drv/occ.npy
C, F, FACE, IN = -59.62, -151.75, 207.73, 25.4
def model(v): v = np.asarray(v); return np.c_[(v[:, 2] - (FACE - 7.56 * IN)) / IN, (v[:, 0] - C) / IN, (v[:, 1] - F) / IN]
P = 0.1
LO = np.array([-7.7, -9.0, 0.0]); HI = np.array([10.5, 9.0, 18.0])
N = np.ceil((HI - LO) / P).astype(int); occ = np.zeros(N, bool)
def mark(v, f, dil=0):
    m = trimesh.Trimesh(v, f, process=False)
    try:
        vx = m.voxelized(P)
        try: vx = vx.fill()
        except Exception: pass
        pts = vx.points
    except Exception: return
    idx = np.floor((pts - LO) / P).astype(int)
    ok = (idx >= 0).all(1) & (idx < N).all(1); idx = idx[ok]
    occ[idx[:, 0], idx[:, 1], idx[:, 2]] = True
keep = pickle.load(open(S + 'robot2/keep5.pkl', 'rb')); fm = pickle.load(open(S + 'addons/front_mesh.pkl', 'rb')); tm = pickle.load(open(S + 'addons/transfer_mesh.pkl', 'rb'))
vend = pickle.load(open(S + 'addons/vendor_mesh.pkl', 'rb'))
s = importlib.util.spec_from_file_location('ib', '/home/user/biobuzz/cad/intake-b/build.py'); IB = importlib.util.module_from_spec(s); s.loader.exec_module(IB)
s = importlib.util.spec_from_file_location('tr', '/home/user/biobuzz/cad/transfer/build.py'); TR = importlib.util.module_from_spec(s); s.loader.exec_module(TR)
EXS = ((IB.EXS_Z - (FACE - 7.56 * IN)) / IN, (IB.EXS_Y - F) / IN)
for p, v, f, c in keep:
    if not re.search(r'Nectar|Pollen', p): mark(model(np.asarray(v) * IN), np.asarray(f))
for n, m in vend.items():
    v = np.asarray(m['v']); v = model(v) if np.abs(v).max() > 60 else v; mark(v, np.asarray(m['f']))
SW = re.compile(TR.FEEDER_SPINS + '|' + TR.FEEDER_SWINGS + r'|^feeder_belt')
for n, m in tm.items():
    if len(m['f']) == 0: continue
    v = model(m['v']); f = np.asarray(m['f']); mark(v, f)
    if SW.search(n):                                   # the feeder swung out too
        a = TR.ARM_IN - TR.ARM_OUT; c_, s_ = math.cos(a), math.sin(a); dy, dz = v[:, 1] - TR.PIVOT[0], v[:, 2] - TR.PIVOT[1]
        mark(np.c_[v[:, 0], TR.PIVOT[0] + dy * c_ - dz * s_, TR.PIVOT[1] + dy * s_ + dz * c_], f)
for n, m in fm.items():
    if len(m['f']) == 0 or 'STAND-IN' in n: continue
    v = model(m['v']); f = np.asarray(m['f'])
    if m['grp'] == 'float':
        for r in np.arange(0, 1.31, 0.13): mark(v + [0, 0, r], f)
    elif m['grp'] == 'hook':
        for ang in range(0, int(IB.STOW) + 1, 6):
            a = -math.radians(ang); c_, s_ = math.cos(a), math.sin(a); x = v[:, 0] - EXS[0]; z = v[:, 2] - EXS[1]
            mark(np.c_[EXS[0] + c_ * x + s_ * z, v[:, 1], EXS[1] - s_ * x + c_ * z], f)
    else: mark(v, f)
# the balls' paths: along the lane, up the column through the flywheels and out the top
X, Y, Z = np.meshgrid(*[LO[k] + (np.arange(N[k]) + 0.5) * P for k in range(3)], indexing='ij')
R = TR.RN + 0.1
lane = (X > TR.BACKSTOP_X) & (X < 10.5) & (np.abs(Y) < TR.WALL_IN) & (Z > TR.FLOOR_Z) & (Z < TR.FLOOR_Z + 2 * TR.RN + 0.9)
col = ((X - TR.COL_X) ** 2 + Y ** 2 < (R + 0.4) ** 2) & (Z > TR.FLOOR_Z)
occ |= lane | col
np.save(S + 'drv/occ.npy', occ); print('occupied', occ.mean().round(3), N)
