"""Sweep the unified front (cad/intake-b/build.py): the roller's float and the extractor's fold, against the robot CAD
and against each other. Run in a folder holding occ.pkl (occ.py) and front_mesh.pkl (build.py with MESH_OUT set).

    python3 front_sweep.py [stow_deg]

Robot: the 0.1 in occupancy grid. New parts against each other: exact mesh intersection (manifold3d), so sliding fits
that only touch don't count. Prints every pose with a clash, and the outlines in the AdvantageScope frame
(+X forward, +Y left, +Z up, inches, chassis centre on the floor)."""
import pickle, sys, math, collections, numpy as np, trimesh, manifold3d as mf
M = pickle.load(open("front_mesh.pkl", "rb"))
occ, names = pickle.load(open("occ.pkl", "rb"))
C, F, FACE, IN = -59.62, -151.75, 207.73, 25.4
FLOAT = 1.3 * IN
EXS = (F + 4.5 * IN, FACE + 2.4 * IN)                        # the extractor's shaft (y, z)
STOW = float(sys.argv[1]) if len(sys.argv) > 1 else 150.0
SKIP_ROBOT = ("72mm Steel Shaft", "1611", "48mm Gecko Wheel", "Intake <1> / 240mm", "Intake <1> / 5000", "Intake <1> / 5103",
              "Intake <1> / 5203", "Red Nectar", "Blue Nectar")    # replaced parts and game pieces
TOUCH = {("float_plate", "side_plate"), ("float_link", "side_plate"), ("float_guide", "side_plate"), ("roller_shaft", "side_plate"),
         ("float_plate", "float_stop"), ("float_link", "float_stop"), ("motor_carriage", "carriage_guides"),
         ("extractor_shaft", "side_plate"), ("extractor_shaft", "extractor_bearing"), ("extractor_gear", "servo_gear"),
         ("roller_bearing", "side_plate")}
def page(v): v = np.asarray(v); return np.c_[(C - v[:, 0]) / IN, (v[:, 1] - F) / IN, (FACE - v[:, 2]) / IN]
def asc(v):   # STEP mm -> AdvantageScope inches
    v = np.asarray(v); return np.c_[(v[:, 2] - FACE) / IN + 7.56, (v[:, 0] - C) / IN, (v[:, 1] - F) / IN]
def tm(m):
    t = trimesh.Trimesh(np.asarray(m["v"]), np.asarray(m["f"]), process=True); t.merge_vertices(); return t
def man(t): return mf.Manifold(mf.Mesh(vert_properties=np.asarray(t.vertices, np.float32), tri_verts=np.asarray(t.faces, np.uint32)))
def samp(t): return np.vstack([trimesh.sample.sample_surface(t, max(300, int(t.area / 1.3)))[0], t.vertices])
T = {n: tm(m) for n, m in M.items()}
MAN = {}
for n, t in T.items():
    try: MAN[n] = man(t)
    except Exception as e: print("not manifold:", n, e)
P = {n: samp(t) for n, t in T.items()}
grp = {n: m["grp"] for n, m in M.items()}
key = lambda n: n.split(" ")[0].rsplit("_", 1)[0] if n.split(" ")[0][-2:] in ("_R", "_L") else n.split(" ")[0]
def short(n): return n.split(" ")[0]
def pose(n, t_float, ang):
    """4x4 transform of part n for a roller rise t_float (mm) and an extractor angle (deg, + folds up)."""
    X = np.eye(4)
    if grp[n] == "float": X[1, 3] = t_float
    if grp[n] == "hook":
        a = math.radians(ang); c, s = math.cos(a), math.sin(a)
        R = np.array([[1, 0, 0], [0, c, s], [0, -s, c]])                   # y' = y c + z s about EXS (forward points rise)
        X[:3, :3] = R; X[:3, 3] = np.array([0, EXS[0], EXS[1]]) - R @ np.array([0, EXS[0], EXS[1]])
    return X
def moved(n, X): return (P[n] @ X[:3, :3].T) + X[:3, 3]
def robot_hits(n, X):
    h = collections.Counter()
    for k in map(tuple, np.floor(page(moved(n, X)) / 0.1).astype(np.int32)):
        i = occ.get(k)
        if i is not None and not any(s in names[i] for s in SKIP_ROBOT): h[names[i].split(" / ")[-1][:26]] += 1
    return h
def mesh_hits(movers, X_of, others):
    out = []
    for a in movers:
        ma = MAN.get(a)
        if ma is None: continue
        Xa = X_of(a); ma = ma.transform(Xa[:3, :].tolist())
        for b in others:
            if a == b or b not in MAN: continue
            if (key(a), key(b)) in TOUCH or (key(b), key(a)) in TOUCH: continue
            mb = MAN[b]; Xb = X_of(b)
            if not np.allclose(Xb, np.eye(4)): mb = mb.transform(Xb[:3, :].tolist())
            v = (ma ^ mb).volume()
            if v > 2.0: out.append((short(a), short(b), round(v)))
    return out
fixed = [n for n in M if grp[n] in ("fixed", "vee")]
flt = [n for n in M if grp[n] == "float"]
hk = [n for n in M if grp[n] == "hook"]
print("== fixed and V against the robot")
for n in fixed:
    h = robot_hits(n, np.eye(4))
    if h and not n.startswith(("standoff", "wheel_shaft", "odometry", "pod_adapter", "carriage_guides", "extractor_servo_bracket")):   # the bracket sits on the upright's face
        print("  ", short(n), h.most_common(3))
print("== the roller floating 0 to 1.3 in (against the robot and the fixed parts)")
for t in np.linspace(0, FLOAT, 6):
    X_of = lambda n: pose(n, t, 0.0)
    rh = {short(n): robot_hits(n, X_of(n)).most_common(2) for n in flt}
    rh = {k: v for k, v in rh.items() if v and not k.startswith(("roller_shaft", "roller_bearing", "motor_carriage"))}   # carriage: slides on the upright's face
    mh = mesh_hits(flt, X_of, fixed)
    print(f"  rise {t / IN:.2f} in:", rh or "", mh or ("clear" if not rh else ""))
print(f"== the extractor 0 to {STOW:.0f} deg, with the roller down, half up and up")
for ang in list(range(0, int(STOW) + 1, 5)) + [STOW]:
    line = []
    for t in (0.0, FLOAT / 2, FLOAT):
        X_of = lambda n: pose(n, t, ang)
        rh = {short(n): robot_hits(n, X_of(n)).most_common(2) for n in hk}
        rh = {k: v for k, v in rh.items() if v}
        mh = mesh_hits(hk, X_of, fixed + flt)
        if rh or mh: line.append((round(t / IN, 2), rh, mh))
    if line or ang % 25 == 0 or ang == STOW:
        A = np.vstack([asc(moved(n, pose(n, 0, ang))) for n in hk])
        print(f"  {ang:5.1f} deg  X {A[:, 0].min():.2f}..{A[:, 0].max():.2f}  Z {A[:, 2].min():.2f}..{A[:, 2].max():.2f} ", line or "clear")
def outline(names_, X_of):
    A = np.vstack([asc(moved(n, X_of(n))) for n in names_]); return A.min(0), A.max(0)
print("== outlines (X fwd, Y left, Z up)")
for label, ns, X_of in (("fixed + V", fixed, lambda n: np.eye(4)), ("roller and motor, down", flt, lambda n: pose(n, 0, 0)),
                        ("roller and motor, up 1.3", flt, lambda n: pose(n, FLOAT, 0)), ("extractor, down", hk, lambda n: pose(n, 0, 0)),
                        ("extractor, stowed", hk, lambda n: pose(n, 0, STOW))):
    lo, hi = outline(ns, X_of)
    print(f"  {label:26s} X {lo[0]:6.2f}..{hi[0]:6.2f}  Y {lo[1]:6.2f}..{hi[1]:6.2f}  Z {lo[2]:5.2f}..{hi[2]:5.2f}")
