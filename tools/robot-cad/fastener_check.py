"""Check every screw drawn in cad/intake-b, cad/transfer (and what they build on) against the parts it holds, the rest of
our parts and the mentor's Robot.step, lined up as the full STEP has it.

    python3 tools/robot-cad/fastener_check.py Robot.step     # caches all_base_mesh.pkl: the mentor's whole robot as meshes

For each screw:
- the shank runs only through holes: it may touch only the part it threads into (a part it should clamp but hits
  means a missing or misplaced hole; a part it shouldn't meet at all is a clash);
- the head and the nut clear everything (a flat head sits in its countersink, so only its key is checked);
- a hex key reaches the head: a 6 mm cylinder (the key and the hand's margin) 40 mm out from the head along the axis;
- a tapped hole gives it at least 1.5 diameters of thread.
Moving parts are checked where they're drawn (roller down, extractor down) and, for the extractor, stowed too."""
import sys, os, re, math, pickle, importlib.util, numpy as np, trimesh, trimesh.transformations as tt
import manifold3d as mf
import cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', '..', 'cad', 'full-robot'))
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
FR = load('fr', os.path.join(HERE, '..', '..', 'cad', 'full-robot', 'build.py')); IB = FR.IB
import fasteners as FA
C, F, FACE, IN = FR.C, FR.F, FR.FACE, FR.IN
def to_model(v): v = np.asarray(v, float); return np.c_[(v[:, 2] - (FACE - 7.56 * IN)) / IN, (v[:, 0] - C) / IN, (v[:, 1] - F) / IN]
def cad_mesh(shape):
    v, f = shape.tessellate(0.2, 0.3)
    if not f: return None
    return trimesh.Trimesh(to_model([(p.x, p.y, p.z) for p in v]), f, process=True)
cache = 'all_base_mesh.pkl'
if os.path.exists(cache): base = pickle.load(open(cache, 'rb'))
else:
    base = []
    for path, shp, loc, col, key in FR.placed_team(sys.argv[1]):
        p = ' / '.join(path)
        if re.search(r'Screw|screw|Nut|2800-|2802-|2829-|CAGE|Washer|text', p): continue
        m = cad_mesh(cq.Shape.cast(shp.wrapped.Moved(loc)))
        if m is None: continue
        base.append((p, m))
    pickle.dump(base, open(cache, 'wb'))
ours, grp = {}, {}
for title, g, d in IB.GROUPS:
    for n, (wp, col, kind) in d.items():
        m = cad_mesh(wp.val() if hasattr(wp, 'val') and len(wp.vals()) == 1 else cq.Compound.makeCompound(wp.vals()))
        if m is not None: ours[n], grp[n] = m, g
TR = FR.TR
for title, g, d in TR.GROUPS:
    for n, (wp, col, kind) in d.items():
        m = cad_mesh(TR.to_cad(wp))
        if m is not None: ours[n], grp[n] = m, 'fixed'
# the Limelight's goBILDA mount (with VENDOR_DIR and LL_STEP, as the full build takes them)
if os.environ.get("VENDOR_DIR") and os.environ.get("LL_STEP"):
    RP = FR.RP; rig = RP.limelight_rig()
    from OCP.TopLoc import TopLoc_Location
    back = RP.to_model(C, F, FACE).Inverted()
    for key, fname, label in (("mast", RP.LL_MAST[1], "Limelight mast"), ("bracket", "1111-0001-0001", "Limelight bracket"), ("beam", "1102-0009-0072", "Limelight beam")):
        leaves = RP._vendor(fname, os.environ["VENDOR_DIR"])
        sh = cq.Compound.makeCompound([cq.Shape.cast(s_.wrapped.Moved(TopLoc_Location(back.Multiplied(rig[key])))) for n_, s_, c_ in leaves])
        ours[label], grp[label] = cad_mesh(sh), 'fixed'
    cam = cq.Compound.makeCompound([s_ for n_, s_, c_ in RP.limelight_in_cad(os.environ["LL_STEP"], C, F, FACE)])
    ours["Limelight 3A"], grp["Limelight 3A"] = cad_mesh(cam), 'fixed'
    for n, (wp, col, kind) in RP.limelight_fasteners({}, C, F, FACE).items():
        ours[n], grp[n] = cad_mesh(wp.val()), 'fixed'
def man(m): return mf.Manifold(mf.Mesh(vert_properties=np.asarray(m.vertices, np.float32), tri_verts=np.asarray(m.faces, np.uint32)))
MAN = {}
def vol(a, b, kb=None):
    if (a.bounds[1] < b.bounds[0]).any() or (b.bounds[1] < a.bounds[0]).any(): return 0.0
    try:
        mb = MAN.get(kb) if kb else None
        if mb is None:
            mb = man(b)
            if kb: MAN[kb] = mb
        return (man(a) ^ mb).volume()
    except Exception: return -1
def cyl_mesh(p0_cad, axis, dia, length):
    """A cylinder from p0 (CAD mm) along axis (CAD), as a model-frame mesh."""
    p0 = to_model([p0_cad])[0]; a = to_model([np.add(p0_cad, axis)])[0] - p0; a /= np.linalg.norm(a)
    c = trimesh.creation.cylinder(radius=dia / 2 / IN, height=length / IN, sections=24)
    c.apply_translation((0, 0, length / IN / 2))
    c.apply_transform(trimesh.geometry.align_vectors([0, 0, 1], a)); c.apply_translation(p0)
    return c
EXS = ((IB.EXS_Z - (FACE - 7.56 * IN)) / IN, (IB.EXS_Y - F) / IN)
def posed(m, g, stow):
    if g == 'hook' and stow:
        m = m.copy(); m.apply_transform(tt.rotation_matrix(-math.radians(IB.STOW), [0, 1, 0], [EXS[0], 0, EXS[1]]))
    return m
TOL = 2e-4   # in^3: about 3 mm^3
own = lambda n, j: n.startswith(('screw_' + j + '_', 'nut_' + j + '_'))
problems = 0
for sn, I in FA.INFO.items():
    j, d, L = I['joint'], I['d'], I['L']
    g = grp.get(sn, 'fixed')
    for stow in ((False, True) if g == 'hook' else (False,)):
        tag = ' (stowed)' if stow else ''
        shank = posed(cyl_mesh(I['head'], I['axis'], d - 0.6, L), g, stow)
        cyl_axis = (to_model([I['head']])[0], to_model([tuple(I['head'][k] + I['axis'][k] * L for k in range(3))])[0])
        hd, hh = FA.HEAD[d]
        top = I['head'] if I.get('flat') else tuple(I['head'][k] - I['axis'][k] * hh for k in range(3))
        head = posed(cyl_mesh(top, I['axis'], hd - 0.4, hh - 0.2), g, stow)
        key = posed(cyl_mesh(top, tuple(-c for c in I['axis']), 6.0, 40.0), g, stow)
        into = re.compile(I['into']) if I['into'] else None
        notes = []
        engaged = 0.0
        for n, m in list(ours.items()) + [('mentor: ' + p, m) for p, m in base]:
            if own(n, j): continue
            mm = posed(m, grp.get(n, 'fixed'), stow)
            v = vol(shank, mm, None if stow else n)
            if into and into.search(n) and re.search(r"\(print|\(\d+/\d+ in (5052 )?aluminium", n):   # a heat-set insert's or a tap's drawn hole:
                lo_, hi_ = m.bounds                                                  # the thread is the shank's length inside the part
                pts_ = np.linspace(cyl_axis[0], cyl_axis[1], 41)
                engaged = max(engaged, ((pts_ >= lo_) & (pts_ <= hi_)).all(1).mean() * L)
            if v > TOL:
                if into and into.search(n): engaged += v / (math.pi * ((d - 0.6) / 2 / IN) ** 2) * IN
                elif any(n.startswith(t) for t in I['through']): notes.append(f'no hole in {n.split(" ")[0]}')
                else: notes.append(f'shank hits {n.split(" / ")[-1][:40]}')
            v = 0.0 if I.get('flat') else vol(head, mm)
            if v > TOL: notes.append(f'head hits {n.split(" / ")[-1][:40]}')
            v = vol(key, mm)
            if v > TOL: notes.append(f'no key access ({n.split(" / ")[-1][:30]})')
        if not I['nut'] and into and not I.get('modelled') and engaged < (I.get('min_engage') or 1.5 * d) - 0.3: notes.append(f'only {engaged:.1f} mm of thread')
        if I.get('service') and notes and all(n.startswith('no key access') for n in notes):
            print(f'{sn.split(" (")[0]:34s} M{d}x{L}{tag}: service order: {I["service"]}'); continue
        if notes:
            problems += 1
            print(f'{sn.split(" (")[0]:34s} M{d}x{L}{tag}: ' + '; '.join(sorted(set(notes))))
for nn in FA.NUTS:
    if nn not in ours: continue
    hits = [n.split(' / ')[-1][:40] for n, m in list(ours.items()) + [('mentor: ' + p, m) for p, m in base]
            if not own(n, nn.split('_', 1)[1].rsplit('_', 1)[0]) and vol(ours[nn], m, n) > TOL]
    if hits: problems += 1; print(f'{nn.split(" (")[0]:34s} nut hits: ' + '; '.join(sorted(set(hits))))
print(f'{len(FA.INFO)} screws, {problems} with problems')
