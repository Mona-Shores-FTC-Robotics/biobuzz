"""Check the unified front (cad/intake-b) against the mentor's current Robot.step, as the full STEP lines it up, and
against the transfer: the roller floating 0 to 1.3 in, the extractor every 10 deg from down to stowed.

    python3 tools/robot-cad/front2_check.py Robot.step       # caches front_base_mesh.pkl in the working folder

Exact mesh intersection (manifold3d) in the model frame (+X forward, +Y left, +Z up, inches). Parts that are meant to
touch (a shaft in its bearing, a plate on the plate it slides on) are listed in TOUCH."""
import sys, os, re, math, pickle, importlib.util, numpy as np, trimesh
import trimesh.transformations as tt
import manifold3d as mf
import cadquery as cq
sys.path.insert(0, '/home/user/biobuzz/cad/full-robot')
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
HERE = os.path.dirname(os.path.abspath(__file__))
FR = load('fr', os.path.join(HERE, '..', '..', 'cad', 'full-robot', 'build.py')); TR = FR.TR; IB = FR.IB
C, F, FACE, IN = FR.C, FR.F, FR.FACE, FR.IN
def cad_mesh(shape):
    v, f = shape.tessellate(0.3, 0.5)
    if not f: return None
    v = np.array([(p.x, p.y, p.z) for p in v])
    return trimesh.Trimesh(np.c_[(v[:, 2] - (FACE - 7.56 * IN)) / IN, (v[:, 0] - C) / IN, (v[:, 1] - F) / IN], f, process=True)
cache = 'front_base_mesh.pkl'
if os.path.exists(cache): base = pickle.load(open(cache, 'rb'))
else:
    base = []
    for path, shp, loc, col, key in FR.placed_team(sys.argv[1]):
        p = ' / '.join(path)
        if re.search(r'Screw|screw|Nut|2800-|2802-|2829-|CAGE|Washer|text', p): continue
        m = cad_mesh(cq.Shape.cast(shp.wrapped.Moved(loc)))
        if m is None or m.bounds[1][0] < 3.0: continue          # only what's near the front
        base.append((p, m))
    pickle.dump(base, open(cache, 'wb'))
print(len(base), 'mentor parts near the front')
front, grp = {}, {}
for title, g, d in IB.GROUPS:
    for n, (wp, col, kind) in d.items():
        if 'STAND-IN' in n: continue
        m = cad_mesh(wp.val() if hasattr(wp, 'val') else wp)
        if m is not None: front[n], grp[n] = m, g
tr = {n: cad_mesh(TR.to_cad(wp)) for n, (wp, col, kind) in list(TR.fixed.items()) + list(TR.launcher.items())}
def man(m): return mf.Manifold(mf.Mesh(vert_properties=np.asarray(m.vertices, np.float32), tri_verts=np.asarray(m.faces, np.uint32)))
def vol(a, b):
    if (a.bounds[1] < b.bounds[0]).any() or (b.bounds[1] < a.bounds[0]).any(): return 0.0
    try: return (man(a) ^ man(b)).volume()
    except Exception: return -1
EXS = ((IB.EXS_Z - (FACE - 7.56 * IN)) / IN, (IB.EXS_Y - F) / IN)          # the extractor's axis (X, Z)
def posed(n, rise, ang):
    m = front[n].copy()
    if grp[n] == 'float': m.apply_translation((0, 0, rise))
    if grp[n] == 'hook': m.apply_transform(tt.rotation_matrix(-math.radians(ang), [0, 1, 0], [EXS[0], 0, EXS[1]]))
    return m
TOUCH = re.compile(r'(float_(plate|link|guide|stop)|roller_shaft|roller_bearing|extractor_(stub|bearing)).*side_plate|side_plate.*(float_|roller_shaft|roller_bearing|extractor_(stub|bearing))'
                   r'|roller_shaft.*(roller_|motor_)|(roller_|motor_).*roller_shaft|float_(plate|link).*float_stop|float_stop.*float_(plate|link)|motor_carriage.*carriage_guides|carriage_guides.*motor_carriage'
                   r'|extractor_stub.*(extractor_bearing|extractor_gear|extractor_arm|arm_spacers|arm_screw)|(extractor_bearing|extractor_gear|extractor_arm|arm_spacers|arm_screw).*extractor_stub|arm_spacers.*(extractor_bearing|extractor_gear|extractor_arm)|(extractor_bearing|extractor_gear|extractor_arm).*arm_spacers|arm_screw.*extractor_arm|extractor_arm.*arm_screw|extractor_gear.*servo_gear|servo_gear.*extractor_gear'
                   r'|extractor_cross_shaft.*(flower_block|block_collar|extractor_arm)|(flower_block|block_collar|extractor_arm).*extractor_cross_shaft'
                   r'|motor_shaft.*(motor_pulley|roller_motor|motor_carriage)|(motor_pulley|roller_motor|motor_carriage).*motor_shaft|belt.*pulley|pulley.*belt'
                   r'|roller_bearing.*float_(plate|link)|float_(plate|link).*roller_bearing|wheel_shaft.*bearing|bearing.*wheel_shaft|standoff.*side_plate|side_plate.*standoff'
                   r'|rigid_v_plate.*side_plate|side_plate.*rigid_v_plate|servo_bracket.*extractor_servo|extractor_servo.*servo_bracket|extractor_stop.*servo_bracket|servo_bracket.*extractor_stop'
                   r'|roller_vector_insert.*roller_vector|roller_vector.*roller_vector_insert|cross_spacers.*(block_collar|extractor_arm|extractor_cross_shaft)|(block_collar|extractor_arm|extractor_cross_shaft).*cross_spacers'
                   r'|pod_adapter.*odometry|odometry.*pod_adapter|motor_carriage.*float_link|float_link.*motor_carriage|roller_motor.*motor_carriage|motor_carriage.*roller_motor')
import fasteners as FA
def held(a, b):
    """True when a is a screw (or its nut or washer) and b is a part it threads into or clamps."""
    for sn, I in FA.INFO.items():
        j = I['joint']
        if a.startswith('nut_' + j + '_') and b == sn: return True
        if a == sn or a.startswith('nut_' + j + '_'):
            if (I['into'] and re.search(I['into'], b)) or any(b.startswith(t) for t in I['through']): return True
    return re.match(r'(arm|cross)_washer_', a) is not None and re.match(r'screw_(arm_stub|cross_end)_|extractor_(arm|stub|cross)', b) is not None
ROBOT_OK = re.compile(r'1107-0015-0384|72mm Steel Shaft|1611-')        # the side plates bolt to the rails; shafts the add-ons replace
seen = {}
poses = [(r, a) for r in (0.0, 0.3, 0.6, 0.85, 1.05, 1.3) for a in range(0, int(IB.STOW) + 1, 10)] + [(r, IB.STOW) for r in (0.0, 0.3, 0.6, 0.85, 1.05, 1.3)]
for rise, ang in poses:
    P = {n: posed(n, rise, ang) for n in front}
    movers = [n for n in front if grp[n] in ('float', 'hook')]
    for n in movers:
        for p, bm in base:
            if ROBOT_OK.search(p): continue
            v = vol(P[n], bm)
            if v > 2e-3: seen.setdefault((n.split(' ')[0], p.split(' / ')[-1][:40]), []).append((rise, ang, round(v, 3)))
        for k in front:
            if k == n or (grp[k] == grp[n] and k < n): continue
            pair = n + ' | ' + k
            if TOUCH.search(pair) or held(n, k) or held(k, n): continue
            v = vol(P[n], P[k])
            if v > 2e-3: seen.setdefault((n.split(' ')[0], k.split(' ')[0]), []).append((rise, ang, round(v, 3)))
        for k, tm in tr.items():
            if k.startswith('lane_drive_belt'): continue      # it rides with the roller: tools/robot-cad/transfer2_check.py checks it at the roller's float
            v = vol(P[n], tm)
            if v > 2e-3: seen.setdefault((n.split(' ')[0], 'transfer: ' + k.split(' ')[0]), []).append((rise, ang, round(v, 3)))
for (a, b), hits in sorted(seen.items()):
    print(f'{a:28s} x {b:42s} at (rise, angle, in3): {hits[:4]}{" ..." if len(hits) > 4 else ""}')
if not seen: print('clear: every pose')
# the fixed parts against the robot, once
for n in [k for k in front if grp[k] in ('fixed', 'vee')]:
    for p, bm in base:
        if ROBOT_OK.search(p): continue
        v = vol(front[n], bm)
        if v > 2e-3: print(f'fixed {n.split(" ")[0]:28s} x {p.split(" / ")[-1][:40]}  {v:.3f} in3')
# roller clearance to the robot's face, at rest
r = [m for n, m in front.items() if n.startswith('roller_vector')]
xmin = min(m.bounds[0][0] for m in r); H = [posed(n, 0, IB.STOW) for n in front if grp[n] == 'hook']
print(f'stowed extractor front at X {max(m.bounds[1][0] for m in H):.3f} (start limit 10.40), top z {max(m.bounds[1][2] for m in H):.2f}')
print(f'roller back at X {xmin:.3f} (face 7.56), front at X {max(m.bounds[1][0] for m in r):.3f}, bottom z {min(m.bounds[0][2] for m in r):.3f}')
