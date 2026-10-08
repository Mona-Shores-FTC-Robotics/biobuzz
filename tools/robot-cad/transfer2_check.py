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
tr = {n: mesh_of(wp) for n, (wp, col, kind) in list(TR.fixed.items()) + list(TR.launcher.items()) + list(TR.elec.items()) if not n.startswith(('screw_', 'nut_'))}
front = {}; front_float = set(); front_hook = set()
for title, g, d in IB.GROUPS:
    for n, (wp, col, kind) in d.items():
        if 'STAND-IN' in n: continue
        m = cad_mesh(wp.val() if hasattr(wp, 'val') else wp)
        if m is not None:
            front[n] = m
            if g == 'float': front_float.add(n)
            if g == 'hook': front_hook.add(n)
import manifold3d as mf
def man(m): return mf.Manifold(mf.Mesh(vert_properties=np.asarray(m.vertices, np.float32), tri_verts=np.asarray(m.faces, np.uint32)))
def vol(a, b):
    if (a.bounds[1] < b.bounds[0]).any() or (b.bounds[1] < a.bounds[0]).any(): return 0.0
    try: return (man(a) ^ man(b)).volume()
    except Exception: return -1
# Meant to touch: what bolts to the mentor's parts, shafts in their bearings, belts on their pulleys.
ROBOT_OK = [(r'^wall_standoff_', r'1107-0015-0384'),
            (r'^flywheel_motor_bracket_', r'Launcher Concept <1> / 5 Hole Lowside U-Channel'),
            (r'^flywheel_belt_', r'41T HTD5 Pulley'), (r'^feeder_bridge', r'Launcher subassembly <(1|2)> / 5 Hole Lowside'),
            (r'^flywheel_shaft_L|^flywheel_spacers_L_(rear|front)', r'Launcher subassembly <2> / (8x14x5mm Bearing|8mm REX Hyper Hub|41T HTD5 Pulley|8mm Spacer|12\.5mm Spacer|1505-0032-0160|Sonic Hub|(3|5) Hole Lowside)'),
            (r'^idler_hanger', r'Intake <1> / 9 Hole Lowside'),
            (r'^flywheel_pulley_', r'Launcher subassembly <(1|2)> / (96mm Steel Shaft|12\.5mm Spacer|8x14x5mm Bearing)'),
            (r'^turret_motor_plate', r'Launcher Concept <1> / 8 Hole Lowside'), (r'^elec_plate', r'1103-0041-0328|1107-0013-0336'), (r'^pinpoint_plate', r'Launcher Concept <1> / 5 Hole Lowside U-Channel'), (r'^turret_gear_(shaft|bearing_mount)', r'1231-0048-0001|2302-0014-0064')]
def robot_ok(n, p): return any(re.search(a, n) and re.search(b, p) for a, b in ROBOT_OK)
def axle(n):
    """Which shaft a part rides on: ('lane', i), ('feeder',), ('servo',) or None."""
    m = re.match(r'lane_(roller|spacer|shaft|bearing|pulley|shaft_spacer|pinion|eclip)_(\d)', n)
    if m: return ('lane', m.group(2))
    if re.match(r'jack_', n): return ('jack',)
    if re.match(r'idler_(pulley|shaft|bearing|spacers)', n): return ('idler',)
    if re.match(r'flywheel_(shaft_L|spacers_L|feeder_pulley|shaft_eclip_L|pulley_L)|feeder_pivot_bearing', n): return ('flywheel L',)
    if re.match(r'turret_gear_(shaft|bearing|spacers|eclip|pulley)', n): return ('turret gear',)
    if re.match(r'turret_motor( \(|_pulley)', n): return ('turret motor',)
    if re.match(r'feeder( \(|_shaft|_spacers|_bearing_(front|outer) |_pulley \(|_shaft_spacer|_shaft_collar|_eclip)', n): return ('feeder',)
    if re.match(r'gate_(servo \(|horn)', n): return ('gate servo',)
    if re.match(r'pad_(hinge \(|hinge_eclip|knuckle)', n): return ('pad hinge',)
    if re.match(r'(lane_servo|servo_pulley)', n): return ('servo',)
    if re.match(r'feeder_motor( \(|_pulley)', n): return ('feeder motor',)
    return None
TOUCH = [(r'^(control_hub|expansion_hub|battery_cradle|switch_holder) ', r'^elec_plate'), (r'^(control_hub|expansion_hub)_cover', r'^elec_plate'), (r'^battery ', r'^battery_cradle'), (r'^pinpoint ', r'^pinpoint_plate'), (r'^power_switch', r'^switch_holder'),
         (r'^lane_wall_', r'^lane_bearing_|^wall_standoff_|^ceiling_post_|^jack_bearing_'),
         (r'^lane_cord_(\d)', r'^lane_pulley_'), (r'^lane_drive_belt', r'^jack_pulley|^idler_pulley'), (r'^lane_pinion_0', r'^jack_pinion'),
         (r'^idler_hanger', r'^idler_(bearing|shaft|spacers)'), (r'^feeder_arm_', r'^feeder_(pivot_)?bearing_'),
         (r'^feeder_belt', r'^feeder_pulley|^flywheel_feeder_pulley'), (r'^gate_tab', r'^feeder_arm_front|^gate_pin_arm'),
         (r'^gate_pushrod', r'^gate_pin_'), (r'^gate_horn', r'^gate_pin_horn|^gate_servo \('), (r'^gate_servo_bracket', r'^gate_servo \(|^wall_standoff_L0'),
         (r'^turret_motor_plate', r'^turret_gear_bearing_plate|^turret_motor \('), (r'^turret_belt', r'^turret_(gear|motor)_pulley'),
         (r'^ceiling_pin_block', r'^ceiling \(|^ceiling_pin_|^ceiling_post'), (r'^ceiling_pin_(front|rear)', r'^ceiling_post'), (r'_glue \(', r'_foam \(|^ceiling \(|^pad_plate'),
         (r'^pad_knuckle', r'^pad_plate|^pad_hinge_block'), (r'^pad_stop', r'^pad_plate|^pad_knuckle_front|^feeder_floor'), (r'^pad_hinge_eclip', r'^pad_hinge_block'),
         (r'^flywheel_motor_bracket_(.)', r'^flywheel_motor_'), (r'^flywheel_motor_pulley', r'^flywheel_(motor|belt)'), (r'^flywheel_belt_', r'^flywheel_pulley_'),
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
        if n.startswith('lane_drive_belt') and fn.startswith('roller_lane_pulley'): continue   # the belt on its pulley
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

# the roller floated (it rises 1.3) vs the transfer (the lane drive's idler, hanger and jackshaft above all)
print('--- the roller floated vs the transfer')
for f in (0.65, 1.3):
    for fn in sorted(front_float):
        m = front[fn].copy(); m.apply_translation((0, 0, f))
        for n, tm in tr.items():
            if n.startswith('lane_drive_belt'): continue
            v = vol(m, tm)
            if v > 1e-4: print(f'  float {f}: {v:8.4f} in3  {fn[:40]} x {n[:50]}')
# the lane drive's belt with the roller up: its runs vs everything but its pulleys
print('--- the lane drive belt with the roller up 1.3')
pts = TR.belt_path(TR.LD_LOOP(TR.ROLLER_AXLE[1] + 1.3)); segs = []
for a_, b_ in zip(pts, pts[1:] + pts[:1]):
    if math.hypot(b_[0] - a_[0], b_[1] - a_[1]) < 1e-4: continue
    segs.append(trimesh.creation.cylinder(radius=TR.LD_CORD / 2, segment=[(a_[0], TR.LD_Y, a_[1]), (b_[0], TR.LD_Y, b_[1])], sections=12))
bl, bh = trimesh.util.concatenate(segs).bounds
for p, m in everything:
    if re.search(r'lane_drive_belt|jack_pulley|idler_pulley|roller_lane_pulley', p): continue
    mm = m
    if p.startswith('front: ') and p[7:] in front_float: mm = m.copy(); mm.apply_translation((0, 0, 1.3))
    if (mm.bounds[1] < bl).any() or (bh < mm.bounds[0]).any(): continue
    if any(vol(sg, mm) > 1e-4 for sg in segs): print('  belt (roller up) hits', p[-70:])
# the feeder swung out (waiting) vs the robot, our front, the rest of the transfer; and its gap to a waiting ball
print('--- the feeder swung out (waiting)')
SWING = re.compile(TR.FEEDER_SPINS + '|' + TR.FEEDER_SWINGS + r'|^feeder_belt')
R_out = tt.rotation_matrix(TR.ARM_IN - TR.ARM_OUT, [1, 0, 0], [0, TR.PIVOT[0], TR.PIVOT[1]])
others = [(p, m) for p, m in base] + [('front: ' + k, v) for k, v in front.items()] + [('transfer: ' + k, v) for k, v in tr.items() if not SWING.search(k) and not re.match(r'gate_(pushrod|horn|pin_horn)', k)]   # the linkage: posed below
for n in [k for k in tr if SWING.search(k)]:
    m = tr[n].copy(); m.apply_transform(R_out)
    for p, bm in others:
        if re.search(r'flywheel_(shaft_L|spacers_L|feeder_pulley)|8x14x5mm Bearing', p) and re.search(r'pivot_bearing|feeder_arm_|feeder_belt', n): continue
        if n.startswith('gate_pin_arm') and re.search(r'gate_(horn|pushrod)', p): continue   # the linkage moves with it
        v = vol(m, bm)
        if v > 1e-4: print(f'  {v:8.4f}  {n[:45]} x {p[-60:]}')
fm = tr[next(k for k in tr if k.startswith('feeder ('))].copy(); fm.apply_transform(R_out)
for R, nm, yc in ((TR.RN, 'NECTAR', 0.0), (TR.RP, 'POLLEN on the right wall', -(TR.WALL_IN - TR.RP)), (TR.RP, 'POLLEN on the left wall', TR.WALL_IN - TR.RP)):
    ball = trimesh.creation.icosphere(subdivisions=3, radius=R); ball.apply_translation((TR.COL_X, yc, TR.FLOOR_Z + R))
    d = trimesh.proximity.signed_distance(ball, fm.vertices).max()
    print(f'  waiting {nm}: the swung-out feeder is {-d:.2f} in from it')

# the extractor swung from deployed to stowed vs the transfer (its left arm passes the lane drive's idler)
print('--- the extractor swinging vs the transfer')
EXS = ((IB.EXS_Z - (FACE - 7.56 * IN)) / IN, (IB.EXS_Y - F) / IN)
for ang in range(0, int(IB.STOW) + 1, 10):
    R_ = tt.rotation_matrix(-math.radians(ang), [0, 1, 0], [EXS[0], 0, EXS[1]])
    for hn in sorted(front_hook):
        m = front[hn].copy(); m.apply_transform(R_)
        for n, tm in tr.items():
            v = vol(m, tm)
            if v > 1e-4: print(f'  {ang:3d} deg: {v:8.4f} in3  {hn[:40]} x {n[:50]}')
# the gate's horn, pushrod and pins at the swung-out pose (the horn turned to put the pushrod on the moved pin)
print('--- the gate linkage swung out')
ho = TR.horn_tip(TR.PIN_OUT)
link = {'horn': TR.bar_xy(TR.GS_SPL, ho, 0.36, *TR.HORN_Z), 'pushrod': TR.bar_xy(ho, TR.PIN_OUT, 0.32, *TR.PR_Z),
        'horn pin': TR.cylz(*ho, 7 * TR.MM, TR.PR_Z[1], TR.PR_Z[1] + 0.12).union(TR.cylz(*ho, 4 * TR.MM, TR.HORN_Z[0], TR.PR_Z[1]))}
swung = [(n, tr[n].copy()) for n in tr if SWING.search(n)]
for n_, m_ in swung: m_.apply_transform(R_out)
others2 = [(p, m) for p, m in others if not re.search(r'gate_(horn|pushrod|pin_horn)|gate_servo \(', p)] + [('transfer (swung): ' + n_, m_) for n_, m_ in swung if not n_.startswith('gate_pin_arm')]
for nm_, wp_ in link.items():
    lm = mesh_of(wp_, to_cad=True)
    for p, bm in others2:
        v = vol(lm, bm)
        if v > 1e-4: print(f'  {nm_}: {v:8.4f} in3 x {p[-60:]}')
print(f'  horn {math.degrees(math.atan2(TR.HT[1] - TR.GS_SPL[1], TR.HT[0] - TR.GS_SPL[0])):.0f} deg in, {math.degrees(math.atan2(ho[1] - TR.GS_SPL[1], ho[0] - TR.GS_SPL[0])):.0f} deg out')
