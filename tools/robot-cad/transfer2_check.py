# Run from a scratch folder: python3 transfer2_check.py <mentor's Robot.step>. Caches all_base_mesh.pkl there (the same
# cache tools/robot-cad/fastener_check.py writes).
# Clash check: the transfer (cad/transfer, v4) and the launcher changes vs the mentor's Robot.step (aligned and edited as
# cad/full-robot does), our front and each other; NECTAR/POLLEN swept along the lane and driven up the column; the pad
# swung back. Exact mesh intersection (manifold3d). Inches, robot frame. Screws are cad/fasteners.py's: the fastener
# check covers them, so they're left out here.
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
cache = 'all_base_mesh.pkl'
if os.path.exists(cache): base = pickle.load(open(cache, 'rb'))
else:
    base = []
    for path, shp, loc, col, key in FR.placed_team(sys.argv[1]):     # the mentor's parts, lined up and edited as the full STEP has them
        p = ' / '.join(path)
        if re.search(r'Screw|screw|Nut|2800-|2802-|2829-|CAGE|Washer|text', p): continue
        m = cad_mesh(cq.Shape.cast(shp.wrapped.Moved(loc)))
        if m is None: continue
        base.append((p, m))
    pickle.dump(base, open(cache, 'wb'))
base = [(p, m) for p, m in base if not (m.bounds[1][0] < -8.5 or m.bounds[0][0] > 9.5 or m.bounds[0][2] > 10)]
print(len(base), 'mentor parts near the transfer')
def mesh_of(wp, to_cad=True):
    shp = TR.to_cad(wp) if to_cad else (wp.val() if hasattr(wp, 'val') else wp)
    return cad_mesh(shp)
tr = {n: mesh_of(wp) for n, (wp, col, kind) in list(TR.fixed.items()) + list(TR.launcher.items()) if not n.startswith(('screw_', 'nut_'))}
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
# Meant to touch: what bolts to the mentor's parts, shafts in their bearings, belts on their pulleys.
ROBOT_OK = [(r'^wall_standoff_|^servo_standoff_', r'1107-0015-0384'),
            (r'^feeder_bearing_plate_', r'Launcher subassembly <2> / (3|5) Hole Lowside'),
            (r"^feeder_bearing_rear", r"Launcher subassembly <2> / 5 Hole Lowside"),
            (r'^flywheel_motor_bracket_', r'Launcher Concept <1> / 5 Hole Lowside U-Channel'),
            (r'^flywheel_belt_', r'41T HTD5 Pulley'), (r'^feeder_bridge', r'Launcher subassembly <(1|2)> / 5 Hole Lowside')]
def robot_ok(n, p): return any(re.search(a, n) and re.search(b, p) for a, b in ROBOT_OK)
def axle(n):
    """Which shaft a part rides on: ('lane', i), ('feeder',), ('servo',) or None."""
    m = re.match(r'lane_(roller|spacer|shaft|bearing|pulley|shaft_spacer)_(\d)', n)
    if m: return ('lane', m.group(2))
    if re.match(r'feeder( \(|_shaft|_spacers|_bearing_(rear|front) |_pulley \(|_shaft_spacer|_shaft_collar|_eclip)', n): return ('feeder',)
    if re.match(r'pad_(hinge \(|hinge_eclip|knuckle)', n): return ('pad hinge',)
    if re.match(r'(lane_servo|servo_pulley)', n): return ('servo',)
    if re.match(r'feeder_motor( \(|_pulley)', n): return ('feeder motor',)
    return None
TOUCH = [(r'^lane_wall_', r'^lane_bearing_|^wall_standoff_|^ceiling_post_'),
         (r'^lane_cord_(\d)', r'^lane_pulley_'), (r'^servo_cord', r'^servo_pulley|^lane_pulley_2'),
         (r'^servo_standoff_', r'^lane_servo'), (r'^feeder_bearing_plate_', r'^feeder_bearing_'),
         (r'^feeder_belt', r'^feeder_(motor_|servo_)?pulley'), (r'^feeder_servo \(', r'^feeder_servo_(hub|standoff)'), (r'^feeder_servo_hub', r'^feeder_servo_pulley'), (r'^feeder_servo_standoff', r'^feeder_bearing_plate_front'), (r'^feeder_motor_standoff', r'^feeder_motor_block|^feeder_bearing_plate_front'), (r'^feeder_motor_block', r'^feeder_motor[ _]'),
         (r'^ceiling_pin_block', r'^ceiling \(|^ceiling_pin_|^ceiling_post'), (r'^ceiling_pin_(front|rear)', r'^ceiling_post'), (r'_glue \(', r'_foam \(|^ceiling \(|^pad_plate'),
         (r'^pad_knuckle', r'^pad_plate|^pad_hinge_block'), (r'^pad_stop', r'^pad_plate|^pad_knuckle_front|^feeder_floor'), (r'^pad_hinge_eclip', r'^pad_hinge_block'),
         (r'^flywheel_motor_bracket_(.)', r'^flywheel_motor_'), (r'^flywheel_motor_pulley', r'^flywheel_(motor|belt)'),
         (r'^ceiling \(', r'^ceiling_(pin|post)'), (r'^ceiling_pin', r'^ceiling_post'), (r'^ceiling_foam', r'^ceiling \('),
         (r'^pad_(plate|foam)', r'^pad_(plate|foam|hinge)'), (r'^pad_hinge', r'^pad_hinge_block'), (r'^pad_stop', r'^pad_hinge_block'),
         (r'^ramp \(', r'^lane_wall_'), (r'^feeder_floor \(', r'^feeder_bridge|^backstop|^pad_hinge_block|^pad_stop'), (r'^backstop', r'^feeder_bridge'), (r'^pad_hinge \(', r'^pad_hinge_block'), (r'^pad_plate', r'^pad_stop')]
def pair_ok(a, b):
    xa, xb = axle(a), axle(b)
    if xa and xa == xb: return True
    if (xa and xa[0] == 'lane' and re.match(r'lane_wall_', b)) or (xb and xb[0] == 'lane' and re.match(r'lane_wall_', a)): return True
    return any((re.search(p, a) and re.search(q, b)) or (re.search(p, b) and re.search(q, a)) for p, q in TOUCH)
print('--- transfer vs mentor robot')
for n, m in tr.items():
    for p, bm in base:
        v = vol(m, bm)
        if v > 1e-4 and not robot_ok(n, p): print(f'{v:8.4f} in3  {n[:50]:50s} x {p[-60:]}')
print('--- transfer vs our front')
for n, m in tr.items():
    for fn, fm in front.items():
        v = vol(m, fm)
        if v > 1e-4: print(f'{v:8.4f} in3  {n[:50]:50s} x {fn[:50]}')
print('--- transfer parts vs each other')
names = list(tr)
for i in range(len(names)):
    for j in range(i + 1, len(names)):
        a, b = names[i], names[j]
        if pair_ok(a, b): continue
        v = vol(tr[a], tr[b])
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
    sweep(R, lane, nm + ' along the lane into the feeder', re.compile(r'ceiling|lane_roller|transfer: feeder \(|pad_(foam|plate)|feeder_floor \('))
    x_rest = max(TR.COL_X, TR.BACKSTOP_X + R + 0.01)     # it rests against the backstop if that's ahead of the column's centre
    up = [(x_rest, zz) for zz in np.linspace(z, 6.2, 14)]
    sweep(R, up, nm + ' driven up the column', re.compile(r'transfer: feeder \(|pad_(foam|plate)|96mm Gecko|feeder_floor \('))

# the pad swung back for a NECTAR: the plate and foam turn about the hinge (along X) until the face is 0.77 further out
print('--- pad swung back (NECTAR) vs the robot, our front and the rest of the transfer')
import trimesh.transformations as tt
hy, hz = TR.PAD_HINGE
ang = math.atan2(0.77, 3.0 - hz)                       # 0.77 at the ball's contact height (about z 3.0)
R_ = tt.rotation_matrix(ang, [1, 0, 0], [0, hy, hz])    # +angle about +X moves the pad's top toward -Y (out)
rest = [(n, m) for n, m in tr.items() if not n.startswith('pad_')]
for n in [k for k in tr if k.startswith('pad_plate') or k.startswith('pad_foam')]:
    m = tr[n].copy(); m.apply_transform(R_)
    for p, bm in base + [('front: ' + k, v) for k, v in front.items()] + [('transfer: ' + k, v) for k, v in rest]:
        v = vol(m, bm)
        if v > 1e-4: print(f'{v:8.4f}  {n[:40]} x {p[-60:]}')
    print('  swung', n[:30], 'Y', m.bounds[:, 1].round(2), 'z', m.bounds[:, 2].round(2))
