"""Sweep the transfer (cad/transfer/build.py) against the robot CAD, the unified front (cad/intake-b/) and itself:
the J-wheel's arm lifting 0 to 1.2 in, the roller (and the lane-drive pulley on its shaft) rising 0 to 1.3 in, the
FLOWER extractor folding 0 to 150 deg. Run in a folder holding occ.pkl, front_mesh.pkl and transfer_mesh.pkl."""
import pickle, math, collections, numpy as np, trimesh, manifold3d as mf
occ, names = pickle.load(open("occ.pkl", "rb"))
T = pickle.load(open("transfer_mesh.pkl", "rb")); Fm = pickle.load(open("front_mesh.pkl", "rb"))
C, F, FACE, IN = -59.62, -151.75, 207.73, 25.4
Z0 = FACE - 7.56 * IN
def cad(X, Z): return (F + Z * IN, Z0 + X * IN)            # robot-frame (X, Z) inches -> CAD (y, z) mm
import importlib.util, os
_s = importlib.util.spec_from_file_location("tb", os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "cad", "transfer", "build.py"))
PV = (0.9, 3.732)                                       # the arm's pivot (cad/transfer/build.py: 60 mm at 20 deg from the axle)
PIVOT, EXS = cad(*PV), (F + 4.5 * IN, FACE + 2.4 * IN)
SKIP = ("Launcher Concept", "Intake <1> / 48mm", "Intake <1> / 240mm", "Intake <1> / 5000", "Intake <1> / 5103", "Intake <1> / 5203",
        "Intake <1> / 1201-0043", "Pattern Spacer", "Nectar", "Pollen", "Intake <1> / 11 Hole")   # replaced, removed, pieces; the 11-hole channel is raised 8 mm (checked below)
TOUCH = {("lane_wall", "strand_shaft"), ("lane_wall", "countershaft"), ("lane_floor", "lane_wall"), ("lane_floor", "ramp"), ("ramp", "lane_wall"),
         ("strand_shaft", "strand_pulley"), ("floor_strand", "strand_pulley"), ("lane_floor", "floor_strand"), ("lane_floor", "strand_pulley"),
         ("J_shaft", "J_wheels"), ("J_shaft", "J_arm"), ("J_shaft", "J_pulley"), ("J_belt", "J_pulley"), ("J_belt", "J_motor_shaft"), ("J_arm", "J_motor_shaft"),
         ("J_arm", "right_pivot_block"), ("right_pivot_block", "lane_wall"), ("J_motor_shaft", "J_motor"), ("J_motor_bracket", "J_motor"),
         ("J_motor_bracket", "lane_wall"), ("J_motor_shaft", "lane_wall"), ("countershaft", "countershaft_pulleys"), ("lane_shaft_pulley", "strand_shaft"),
         ("lane_drive_lower_loop", "countershaft_pulleys"), ("lane_drive_lower_loop", "lane_shaft_pulley"), ("lane_drive_upper_loop", "countershaft_pulleys"),
         ("lane_drive_upper_loop", "lane_drive_pulley_roller"), ("lane_drive_pulley_roller", "roller_shaft"), ("outer_J_and_chute", "lane_wall"),
         ("outer_J_and_chute", "lane_floor"), ("arm_stop", "lane_wall"), ("arm_stop", "J_arm"), ("arm_stop", "J_motor_bracket"), ("band_post", "lane_wall"),
         ("band_post", "J_motor_bracket"), ("lane_hanger", "lane_wall"), ("ramp_bracket", "ramp"), ("ramp_bracket", "side_plate"), ("queue_lid", "lane_wall"), 
         ("queue_lid", "outer_J_and_chute"), ("lane_drive_upper_loop", "roller_wheels"), ("lane_drive_upper_loop", "countershaft"),
         ("turret_bearing_REFERENCE", "turret_channel_X-5.6_REFERENCE"), ("turret_bearing_REFERENCE", "turret_channel_X+0.4_REFERENCE"),
         ("turret_channel_X-5.6_REFERENCE", "outer_J_and_chute"), ("turret_channel_X-5.6_REFERENCE", "lane_wall"), ("turret_bearing_REFERENCE", "outer_J_and_chute"),
         ("turret_bearing_REFERENCE", "lane_wall")}   # the upper loop re-aligns as the roller rises
key = lambda n: n.split(" ")[0].rstrip("0123456789").rstrip("_").removesuffix("_L").removesuffix("_R") if not n.startswith("lane_strip") else "lane_strip"
def k2(n):
    b = n.split(" ")[0]
    for p in sorted({a for t in TOUCH for a in t}, key=len, reverse=True):
        if b.startswith(p): return p
    return b
def tm(m):
    t = trimesh.Trimesh(np.asarray(m["v"]), np.asarray(m["f"]), process=True); t.merge_vertices(); return t
def man(t): return mf.Manifold(mf.Mesh(vert_properties=np.asarray(t.vertices, np.float32), tri_verts=np.asarray(t.faces, np.uint32)))
ALL = {**{n: dict(m, src="T") for n, m in T.items()}, **{n: dict(m, src="F") for n, m in Fm.items()}}
TM = {n: tm(m) for n, m in ALL.items()}; MAN = {}
for n, t in TM.items():
    try: MAN[n] = man(t)
    except Exception as e: print("not manifold:", n)
P = {n: np.vstack([trimesh.sample.sample_surface(t, max(300, int(t.area / 1.3)))[0], t.vertices]) for n, t in TM.items() if ALL[n]["src"] == "T"}
def page(v): return np.c_[(C - v[:, 0]) / IN, (v[:, 1] - F) / IN, (FACE - v[:, 2]) / IN]
def rot(c, deg):
    a = math.radians(deg); co, s = math.cos(a), math.sin(a); R = np.array([[1, 0, 0], [0, co, s], [0, -s, co]])
    X = np.eye(4); X[:3, :3] = R; X[:3, 3] = np.array([0, *c]) - R @ np.array([0, *c]); return X
def pose(n, rise=0.0, lift_deg=0.0, ext=0.0):
    g, src = ALL[n]["grp"], ALL[n]["src"]
    if g == "float": X = np.eye(4); X[1, 3] = rise; return X
    if src == "T" and g == "arm": return rot(PIVOT, lift_deg)
    if src == "F" and g == "hook": return rot(EXS, ext)
    return np.eye(4)
def robot_hits(n, X):
    h = collections.Counter()
    for k in map(tuple, np.floor(page(P[n] @ X[:3, :3].T + X[:3, 3]) / 0.1).astype(np.int32)):
        i = occ.get(k)
        if i is not None and not any(s in names[i] for s in SKIP): h[names[i].split(" / ")[-1][:26]] += 1
    return h
def clashes(movers, others, X_of):
    out = []
    for a in movers:
        if a not in MAN: continue
        ma = MAN[a].transform(X_of(a)[:3, :].tolist())
        for b in others:
            if b == a or b not in MAN or (k2(a), k2(b)) in TOUCH or (k2(b), k2(a)) in TOUCH: continue
            v = (ma ^ MAN[b].transform(X_of(b)[:3, :].tolist())).volume()
            if v > 2.0: out.append((a.split(" ")[0], b.split(" ")[0], round(v)))
    return out
tr = [n for n in T]; fr = [n for n in Fm]
print("== transfer against the robot (at rest)")
for n in tr:
    h = robot_hits(n, pose(n))
    if h and not n.startswith(("lane_hanger", "turret_channel")): print("  ", n.split(" ")[0], h.most_common(3))
    elif h: print("   (bolts to it)", n.split(" ")[0], h.most_common(2))
print("== transfer against itself and the front: roller rise 0 / 0.65 / 1.3 in, extractor 0 / 75 / 150 deg")
seen = set()
for rise in (0, 0.65 * IN, 1.3 * IN):
    for ext in (0, 75, 150):
        c = clashes(tr, [n for n in tr + fr], lambda n: pose(n, rise, 0, ext))
        for x in c:
            if x[:2] not in seen: seen.add(x[:2]); print(f"   rise {rise / IN:.2f} ext {ext}:", x)
print("   ", "clear" if not seen else "")
# J arm lift: 1.2 in of travel at the axle, perpendicular to the arm
a0 = math.degrees(math.atan2(4.54 - PV[1], -1.32 - PV[0])); a1 = a0 - math.degrees(1.2 / (60 / IN))   # 1.2 in along the arc
print(f"== the J arm lifting: {a0:.0f} -> {a1:.0f} deg about the pivot")
for f in np.linspace(0, 1, 7):
    lift = (a1 - a0) * f               # negative: the axle is behind the pivot, so lifting it turns rear points up
    c = clashes([n for n in tr if T[n]["grp"] == "arm"], [n for n in tr + fr if not (n in T and T[n]["grp"] == "arm")], lambda n: pose(n, 0, lift, 150))
    rh = {n.split(" ")[0]: robot_hits(n, pose(n, 0, lift)).most_common(2) for n in tr if T[n]["grp"] == "arm"}
    rh = {k: v for k, v in rh.items() if v}
    ax = np.array([0, *cad(-1.32, 4.54), 1.0]); q = pose(tr[[T[n]["grp"] for n in tr].index("arm")], 0, lift) @ ax
    print(f"   axle up {(q[1] - cad(-1.32, 4.54)[0]) / IN:+.2f} in:", c or "", rh or ("clear" if not c else ""))
# the raised 11-hole channel: bottom 4.75 in after the move; the walls' notch tops at 4.7 there
print("== the raised 11-hole channel (bottom 4.75, X 5.03..5.51) against the walls' notch (to 4.70, X 4.95..5.60): 0.05 in clear")
