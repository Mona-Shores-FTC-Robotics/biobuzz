"""Vendor CAD for the whole-robot builds: the goBILDA odometry pods (cut from the example chassis STEP, with their
parts and colours) and the Limelight 3A (Limelight's own STEP, downloads.limelightvision.io/cad/LIMELIGHT3ACAD_STEP.stp).
Shapes come back in the team CAD's frame (mm: x across, y up, z forward)."""
import math, re
import cadquery as cq
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TDocStd import TDocStd_Document
from OCP.TCollection import TCollection_ExtendedString
from OCP.XCAFDoc import XCAFDoc_DocumentTool, XCAFDoc_ColorSurf, XCAFDoc_ColorGen
from OCP.TDF import TDF_LabelSequence, TDF_Label
from OCP.TDataStd import TDataStd_Name
from OCP.TopLoc import TopLoc_Location
from OCP.Quantity import Quantity_Color
from OCP.gp import gp_Trsf, gp_Vec, gp_Ax3, gp_Pnt, gp_Dir

def read_leaves(path, keep=None):
    """[(path names, cq.Shape placed, colour)] for every leaf part whose path matches `keep` (a regex), colours kept."""
    doc = TDocStd_Document(TCollection_ExtendedString("d"))
    r = STEPCAFControl_Reader(); r.SetNameMode(True); r.SetColorMode(True); r.ReadFile(path); r.Transfer(doc)
    st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main()); ct = XCAFDoc_DocumentTool.ColorTool_s(doc.Main())
    def name(l):
        a = TDataStd_Name(); return a.Get().ToExtString() if l.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
    def colour(l):
        q = Quantity_Color(); s = st.GetShape_s(l)
        for kind in (XCAFDoc_ColorSurf, XCAFDoc_ColorGen):
            if ct.GetColor(s, kind, q): return (q.Red(), q.Green(), q.Blue())
        return None
    out = []
    def walk(l, loc, names, col):
        if st.IsReference_s(l):
            ref = TDF_Label(); st.GetReferredShape_s(l, ref)
            walk(ref, loc.Multiplied(st.GetLocation_s(l)), names + [name(l)], colour(l) or col); return
        if st.IsAssembly_s(l):
            seq = TDF_LabelSequence(); st.GetComponents_s(l, seq)
            for i in range(1, seq.Length() + 1): walk(seq.Value(i), loc, names, col)
            return
        if keep is None or keep.search(" / ".join(names)):
            out.append((names, cq.Shape.cast(st.GetShape_s(l).Moved(loc)), colour(l) or col or (0.6, 0.62, 0.66)))
    roots = TDF_LabelSequence(); st.GetFreeShapes(roots)
    for i in range(1, roots.Length() + 1): walk(roots.Value(i), TopLoc_Location(), [], None)
    return out

def read_leaves_inst(path, keep=None):
    """As read_leaves, but [(path names, base shape, TopLoc_Location, colour)] with one base shape per distinct part, so
    an assembly built from them keeps repeated parts as instances (the pods' screws, the two pods themselves)."""
    doc = TDocStd_Document(TCollection_ExtendedString("d"))
    r = STEPCAFControl_Reader(); r.SetNameMode(True); r.SetColorMode(True); r.ReadFile(path); r.Transfer(doc)
    st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main()); ct = XCAFDoc_DocumentTool.ColorTool_s(doc.Main())
    def name(l):
        a = TDataStd_Name(); return a.Get().ToExtString() if l.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
    def colour(l):
        q = Quantity_Color(); s_ = st.GetShape_s(l)
        for kind in (XCAFDoc_ColorSurf, XCAFDoc_ColorGen):
            if ct.GetColor(s_, kind, q): return (q.Red(), q.Green(), q.Blue())
        return None
    out, base = [], {}
    def walk(l, loc, names, col):
        if st.IsReference_s(l):
            ref = TDF_Label(); st.GetReferredShape_s(l, ref)
            walk(ref, loc.Multiplied(st.GetLocation_s(l)), names + [name(l)], colour(l) or col); return
        if st.IsAssembly_s(l):
            seq = TDF_LabelSequence(); st.GetComponents_s(l, seq)
            for i in range(1, seq.Length() + 1): walk(seq.Value(i), loc, names, col)
            return
        if keep is None or keep.search(" / ".join(names)):
            k = l.Tag()
            if k not in base: base[k] = cq.Shape.cast(st.GetShape_s(l))
            out.append((names, base[k], loc, colour(l) or col or (0.6, 0.62, 0.66)))
    roots = TDF_LabelSequence(); st.GetFreeShapes(roots)
    for i in range(1, roots.Length() + 1): walk(roots.Value(i), TopLoc_Location(), [], None)
    return out

def pods_inst(example_step, pod_move):
    """As pods, but [(part name, base shape, TopLoc_Location, colour)] per pod, sharing base shapes between the pods."""
    parts = read_leaves_inst(example_step, re.compile(r"Odometery pod <\d>"))
    out = {}
    for n, (stem, _, d) in pod_move.items():
        k = stem[-1]; t = gp_Trsf(); t.SetTranslation(gp_Vec(*d)); mv = TopLoc_Location(t)
        out[n] = [(names[-1], shp, mv.Multiplied(loc), col) for names, shp, loc, col in parts if any(f"Odometery pod <{k}>" in p for p in names)]
    return out

def pods(example_step, pod_move):
    """{robot pod name: [(part name, shape, colour)]}: the example chassis' two pods, moved to ours by pod_move
    (cad/robot-addons/build.py's POD_MOVE: name -> (pod file stem 'pod1'/'pod2', bbox, translation))."""
    parts = read_leaves(example_step, re.compile(r"Odometery pod <\d>"))
    out = {}
    for n, (stem, _, d) in pod_move.items():
        k = stem[-1]
        out[n] = [(names[-1], s.translate(cq.Vector(*d)), col) for names, s, col in parts if any(f"Odometery pod <{k}>" in p for p in names)]
    return out

# The Limelight 3A in its own STEP (mm): lens centre at (79.5, 8.8, 16.2), looking along +y, its long side along x,
# mounting holes at x 47.5/111.5, z 1.2/41.2 (64 x 40 mm), back face at y -8.1.
LL_LENS, LL_BACK = (79.5, 8.8, 16.2), -8.1
def limelight_trsf(lens_in=(4.0, 0.0, 14.0), pitch_deg=45.0):
    """Camera STEP (mm) -> robot model frame (mm): the lens at lens_in (inches), looking forward and pitched up."""
    a = math.radians(pitch_deg)
    n = gp_Dir(math.cos(a), 0, math.sin(a)); u = gp_Dir(-math.sin(a), 0, math.cos(a))
    x = gp_Dir(0, -1, 0)                                         # the camera's x (its long side) runs to the robot's right
    to = gp_Ax3(gp_Pnt(*(v * 25.4 for v in lens_in)), u, x)      # main direction = the camera's z (up), x direction = its x
    fr = gp_Ax3(gp_Pnt(*LL_LENS), gp_Dir(0, 0, 1), gp_Dir(1, 0, 0))
    t = gp_Trsf(); t.SetDisplacement(fr, to); return t

def _trsf(cols, origin):
    """gp_Trsf from three column vectors (where the local x, y, z axes go) and where the local origin goes."""
    t = gp_Trsf()
    (a, b, c), (d, e, f), (g, h, i) = cols
    t.SetValues(a, d, g, origin[0], b, e, h, origin[1], c, f, i, origin[2])
    return t

def _mv(t, p):
    q = gp_Pnt(*p).Transformed(t); return (q.X(), q.Y(), q.Z())

# The Limelight's goBILDA mount, model frame (mm): a 1121 low-side U-channel mast (8 hole, 216 mm) standing on the
# mentor's 9-hole front channel, its web bolted to the front face of his 35-hole L-beam; a 1111 angle pattern bracket
# (one leg bent 45 deg) on the back of the mast's top, its bent leg rising backward; a 1102 flat beam (9 hole, 72 mm)
# across that leg; the camera bolted through its back's M4 holes (64 mm apart, goBILDA's 8 mm grid) to the beam's end
# holes, the right way up, looking forward and 45 deg up. The camera's numbers are the file's: its back at y -8.1, the
# hole rows at z 13.2 / 21.2 / 29.2, x 47.5 and 111.5; the lens at (79.5, 8.8, 16.2).
LL_MAST_X, LL_MAST_Z0 = 5.663 * 25.4, 6.319 * 25.4
LL_MAST = (168.0, "1121-0006-0168")          # 6-hole: the lens 14.2 in up, about config.json's 14
LL_DROP = 12.0                               # the bracket, down the mast from its top: on the mast's hole rows (4 mod 8 mm)
LL_BEAM_SHIFT = 0.69                         # the beam along the bracket's leg, onto its hole row (from the two files)
LL_SPACER = 6.0                              # the camera stands on two spacers: the beam's screw heads fit under it
def limelight_rig(mast_len=None, drop=None):
    """{piece: gp_Trsf (its STEP's frame -> model frame, mm)} for the mast, bracket, beam and camera."""
    r2 = math.sqrt(0.5)
    mast = _trsf(((1, 0, 0), (0, 0, 1), (0, -1, 0)), (0, 0, 0))                 # local x -> +X (flanges forward), y -> up, z -> -Y
    o = _mv(mast, (-24.0, 0.0, -21.5)); mast.SetTranslationPart(gp_Vec(LL_MAST_X - o[0], 0 - o[1], LL_MAST_Z0 - o[2]))
    mast_len = LL_MAST[0] if mast_len is None else mast_len; drop = LL_DROP if drop is None else drop
    zb = LL_MAST_Z0 + mast_len + 1.5 - drop                                           # the bend: its top hole on the mast's last grid hole
    br = _trsf(((-1, 0, 0), (0, -1, 0), (0, 0, 1)), (0, 0, 0))                  # local x -> -X (outer face on the mast's back), y -> -Y, z -> up
    o = _mv(br, (-24.0, 24.0, 0.0)); br.SetTranslationPart(gp_Vec(LL_MAST_X - o[0], -o[1], zb - o[2]))
    nrm = (r2, 0.0, r2)                                                          # the bent leg's outer face looks forward and up
    leg_c = _mv(br, (-6.8, 24.0, 20.8))                                          # that face's centre
    up_back = (-r2, 0.0, r2)
    # beam: local x (its length) -> +Y, y (through its holes) -> the face normal, z -> x cross y
    bx, by = (0, 1, 0), nrm; bz = (bx[1] * by[2] - bx[2] * by[1], bx[2] * by[0] - bx[0] * by[2], bx[0] * by[1] - bx[1] * by[0])
    beam = _trsf((bx, by, bz), (0, 0, 0))
    beam.SetTranslationPart(gp_Vec(*(leg_c[k] + up_back[k] * LL_BEAM_SHIFT for k in range(3))))
    top = _mv(beam, (0.0, 4.0 + LL_SPACER, 0.0))                                 # the spacers' tops, over the beam's centre hole
    # camera: its +y (out of the lens) -> forward-up; its file's +z is the camera's bottom, so -> down-forward (right way up)
    cy, cz = nrm, (-up_back[0], -up_back[1], -up_back[2]); cx = (cy[1] * cz[2] - cy[2] * cz[1], cy[2] * cz[0] - cy[0] * cz[2], cy[0] * cz[1] - cy[1] * cz[0])
    cam = _trsf((cx, cy, cz), (0, 0, 0)); o = _mv(cam, (79.5, -8.1, 21.2)); cam.SetTranslationPart(gp_Vec(top[0] - o[0], top[1] - o[1], top[2] - o[2]))
    return {"mast": mast, "bracket": br, "beam": beam, "camera": cam}

def limelight_fasteners(parts, C, F, FACE):
    """The mount's screws, nuts and the camera's spacers, into `parts` (team CAD frame, cad/fasteners.py's bolt()):
    the mast to the mentor's L-beam (his top row of holes, 7.894 in up) and the bracket to the mast (their hole rows at
    11.673 and 12.303 in), all at 16 mm either side of the centreline, nuts inside the mast; the flat beam to the
    bracket's leg (nuts under it); the camera on two 6 mm spacers at the beam's ends, M4 from under the beam into its
    4.8 mm threads (4 mm of thread: the camera is light)."""
    import os, sys
    sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
    import fasteners as FA
    back = to_model(C, F, FACE).Inverted(); rig = limelight_rig()
    def cad(p_mm): return _mv(back, p_mm)
    def cad_dir(d): q = gp_Dir(*d).Transformed(back); return (q.X(), q.Y(), q.Z())
    IN = 25.4
    for y in (-16.0, 16.0):
        FA.bolt(parts, f"ll_mast_{'L' if y > 0 else 'R'}", "Limelight mast to the L-beam", [cad((5.565 * IN, y, 7.894 * IN))], cad_dir((1, 0, 0)), 5.0, through=("mentor: ", "Limelight mast"))
        FA.bolt(parts, f"ll_bracket_{'L' if y > 0 else 'R'}", "Limelight bracket to the mast", [cad((5.565 * IN, y, z * IN)) for z in (11.673, 12.303)], cad_dir((1, 0, 0)), 5.0,
                through=("Limelight bracket", "Limelight mast"))
    b = rig["beam"]; n = gp_Dir(0, 1, 0).Transformed(b); nrm = (n.X(), n.Y(), n.Z())
    FA.bolt(parts, "ll_beam", "Limelight beam to the bracket's bent leg", [cad(_mv(b, (x, 4.0, 0.0))) for x in (-16.0, 16.0)], cad_dir(tuple(-c for c in nrm)), 6.5,
            through=("Limelight beam", "Limelight bracket"), service="under the camera: take the camera off first (its two screws, from under the beam's ends)")
    FA.bolt(parts, "ll_camera", "Limelight 3A to the beam, on spacers", [cad(_mv(b, (x, 0.0, 0.0))) for x in (-32.0, 32.0)], cad_dir(nrm), 4.0 + LL_SPACER,
            nut=False, tapped=4.8, min_engage=4.0, into="^Limelight 3A", through=("Limelight beam", "Limelight spacer"), modelled=True)
    for i, x in enumerate((-32.0, 32.0)):
        p0 = cad(_mv(b, (x, 4.0, 0.0)))
        sp = cq.Workplane(cq.Plane(origin=p0, xDir=FA._perp(cad_dir(nrm)), normal=cad_dir(nrm))).circle(3.5).circle(2.15).extrude(LL_SPACER)
        parts[f"Limelight spacer {i} (M4 spacer, 6 mm long, 7 mm OD)"] = (sp, (0.8, 0.82, 0.85), "buy")
    return parts

def fastener_parts(name, vdir):
    """goBILDA's own model of a screw or nut drawn by cad/fasteners.py: [(name, shape, TopLoc_Location, colour)], or None."""
    import os, sys
    sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
    import fasteners as FA
    if name in FA.SCREWS:
        sku, top, a = FA.SCREWS[name]; leaves = _vendor(sku, vdir)
        if not leaves: return None
        zmax = max(s.BoundingBox().zmax for n_, s, c in leaves)
        loc = place((0.0, 0.0, zmax), (0, 0, -1), top, a)
    elif name in FA.NUTS:
        sku, seat, a = FA.NUTS[name]; leaves = _vendor(sku, vdir)
        if not leaves: return None
        loc = place((0.0, 0.0, 0.0), (0, 1, 0), seat, a)
    else: return None
    return [(n_[-1] if n_ else sku, s, loc, c) for n_, s, c in leaves]

def limelight_lens_in(**kw):
    """The lens (inches, model frame) and the camera's view direction, as the mount puts them."""
    t = limelight_rig(**kw)["camera"]; p = _mv(t, LL_LENS); d = gp_Dir(0, 1, 0).Transformed(t)
    return tuple(v / 25.4 for v in p), (d.X(), d.Y(), d.Z())

def to_model(C, F, FACE, back_in=7.56):
    """Team CAD mm (x across, y up, z forward) -> the model frame, mm (X forward, Y left, Z up, origin on the floor
    under the chassis centre, back_in inches behind the face)."""
    t = gp_Trsf(); t.SetValues(0, 0, 1, -(FACE - back_in * 25.4), 1, 0, 0, -C, 0, 1, 0, -F); return t

def limelight_mount_in_cad(vdir, C, F, FACE):
    """[(part name, base shape, TopLoc_Location, colour)] of the camera's goBILDA mount, in the team CAD's frame."""
    back = to_model(C, F, FACE).Inverted(); rig = limelight_rig(); out = []
    for key, fname, label in (("mast", LL_MAST[1], f"mast: goBILDA {LL_MAST[1]} low-side U-channel"),
                              ("bracket", "1111-0001-0001", "goBILDA 1111-0001-0001 angle pattern bracket (45 deg)"),
                              ("beam", "1102-0009-0072", "goBILDA 1102-0009-0072 flat beam (9 hole)")):
        leaves = _vendor(fname, vdir) if vdir else None
        if leaves is None: continue
        loc = TopLoc_Location(back.Multiplied(rig[key]))
        out += [(label, shp, loc, col) for names, shp, col in leaves]
    return out

def limelight_in_cad(ll_step, C, F, FACE):
    """[(part name, shape, colour)] of the Limelight 3A on its goBILDA mount, in the team CAD's frame."""
    t = TopLoc_Location(to_model(C, F, FACE).Inverted().Multiplied(limelight_rig()["camera"]))
    return [(names[-1] if names else "body", cq.Shape.cast(s.wrapped.Moved(t)), col) for names, s, col in read_leaves(ll_step)]

# ---- vendor parts in place of the drawn envelopes ----
# Each entry: the part names it replaces, the vendor STEP (under VENDOR_DIR, as goBILDA's and WCP's sites serve them),
# and where that STEP's own axis, centre (or mounting face) and width are, in its own frame (mm).
VENDOR = [
    # (name regex, file, kind, local axis, local reference point, local width or None)
    (r"5203-2402-0005", "5203-2402-0005 assembly.STEP", "motor", (0, 1, 0), (-37.15, 92.9, -11.05), None),     # +Y: body to shaft; the point is the gearbox face
    (r"312 RPM Yellow Jacket", "5203-2402-0019 assembly.STEP", "motor", (0, 1, 0), (-37.15, 101.7, -11.05), None),
    (r"1611-0514-4008", "1611-0514-4008.STEP", "round", (0, 1, 0), (0, 2.5, 0), 5.0),
    (r"3417-4008-0024", "3417-4008-0024.step", "round", (0, 0, 1), (0, 0, 6.0), 12.0),
    (r"16T HTD5", "3417-4008-0016.step", "round", (0, 0, 1), (0, 0, 0), 12.0),
    (r"72 mm Gecko", "3632-0014-0072.step", "round", (0, 1, 0), (0, 0, 0), 24.0),
    (r"48 mm gecko", "3632-4008-0048.step", "round", (0, 1, 0), (0, 0, 0), 16.0),
    (r"8mm REX clamping collar", "2910-1020-4008 assembly.STEP", "round", (0, 1, 0), (-10.8, -19.75, 19.1), 10.3),
    (r"WCP-0353", "WCP-0353.step", "round", (1, 0, 0), (0, 0, 0), 25.4),
    (r"WCP-0354", "WCP-0354.step", "round", (1, 0, 0), (0, 0, 0), 25.4),
]
_vcache = {}
def _vendor(fname, vdir):
    if fname not in _vcache:
        import glob, os
        hits = [h for h in glob.glob(os.path.join(vdir, "**", fname), recursive=True) if os.path.isfile(h)] or \
               [h for h in glob.glob(os.path.join(vdir, "**", fname + "*"), recursive=True) if h.lower().endswith((".step", ".stp"))]
        _vcache[fname] = read_leaves(hits[0]) if hits else None
    return _vcache[fname]

def _frame(origin, axis, ref=None):
    a = gp_Dir(*axis)
    if ref is None:                                  # any direction square to the axis
        ref = (1, 0, 0) if abs(axis[0]) < 0.9 else (0, 1, 0)
    r = gp_Dir(*ref); x = gp_Dir(r.Crossed(a).Crossed(a).Reversed().XYZ()) if abs(r.Dot(a)) > 1e-6 else r
    return gp_Ax3(gp_Pnt(*origin), a, x)

def place(src_origin, src_axis, dst_origin, dst_axis, src_ref=None, dst_ref=None):
    t = gp_Trsf(); t.SetDisplacement(_frame(src_origin, src_axis, src_ref), _frame(dst_origin, dst_axis, dst_ref)); return TopLoc_Location(t)

def _axis_of(bb, tol=0.6):
    """The drawn cylinder's axis (0, 1 or 2) from its bounding box: the extent that isn't the diameter."""
    e = [bb.xlen, bb.ylen, bb.zlen]
    for i in range(3):
        j, k = [n for n in range(3) if n != i]
        if abs(e[j] - e[k]) < tol and abs(e[i] - e[j]) > tol: return i
    return None

def vendor_parts(name, shape, others, vdir):
    """[(part name, base shape, TopLoc_Location, colour)] of the vendor part(s) for the drawn part `name` (team CAD mm),
    or None to keep the drawing. `others` is {name: shape} of the parts drawn with it: a motor's shaft points to the
    pulley or shaft on its axis."""
    import os, sys
    sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
    import fasteners as FA
    if name in FA.PLACED:                            # a directional part its build placed itself
        fname, so, sa, sr, do, da, dr = FA.PLACED[name]; leaves = _vendor(fname, vdir)
        if leaves is None: return None
        loc = place(so, sa, do, da, sr, dr)
        return [(names[-1] if names else fname, shp, loc, col) for names, shp, col in leaves]
    for rx, fname, kind, la, lp, lw in VENDOR:
        if not re.search(rx, name): continue
        leaves = _vendor(fname, vdir)
        if leaves is None: return None
        bb = shape.BoundingBox(); i = _axis_of(bb)
        if i is None: return None
        c = [bb.center.x, bb.center.y, bb.center.z]; lo = [bb.xmin, bb.ymin, bb.zmin]; hi = [bb.xmax, bb.ymax, bb.zmax]
        ax = [0.0, 0.0, 0.0]; ax[i] = 1.0
        out = []
        if kind == "motor":
            best = None
            for n2, s2 in others.items():
                if n2 == name or not re.search(r"pulley|motor_shaft", n2): continue
                b2 = s2.BoundingBox(); c2 = [b2.center.x, b2.center.y, b2.center.z]
                off = math.hypot(*[c2[k] - c[k] for k in range(3) if k != i])
                if off < 3.0 and (best is None or abs(c2[i] - c[i]) < abs(best[i] - c[i])): best = c2
            if best is None: return None
            sgn = 1.0 if best[i] > c[i] else -1.0
            face = list(c); face[i] = hi[i] if sgn > 0 else lo[i]
            d = list(ax); d[i] = sgn
            locs = [place(lp, la, face, d)]
        else:
            w = hi[i] - lo[i]; n = max(1, round(w / lw)) if lw else 1
            locs = []
            for k in range(n):
                p = list(c); p[i] = lo[i] + (k + 0.5) * w / n
                locs.append(place(lp, la, p, ax))
        for k, loc in enumerate(locs):
            for names, shp, col in leaves:
                out.append((f"{k} {names[-1] if names else fname}", shp, loc, col))
        return out
    return None


def servo_parts(name, shape, IB, vdir):
    """The goBILDA 2000-0025-0002 servo for the front's drawn one: its H25T spline on the extractor gear's axis, pointing
    outboard (right), the top of its case at the drawn box's outboard face, the case running back from the spline."""
    if "2000-0025-0002" not in name: return None
    leaves = _vendor("2000-0025-0002.step", vdir)
    if leaves is None: return None
    bb = shape.BoundingBox()
    top = (bb.xmin, IB.SV_Y, IB.SV_Z)                 # the right side is -x: the outboard face is xmin
    loc = place((-10.0, 0.0, 12.8), (0, 0, 1), top, (-1, 0, 0), src_ref=(1, 0, 0), dst_ref=(0, 0, -1))
    return [(names[-1] if names else "servo", shp, loc, col) for names, shp, col in leaves]

def save_step(assy, path):
    """Write a cq.Assembly as STEP with each distinct part defined once and placed as often as it's used (cadquery's own
    export writes every copy in full: five motors are five 17 MB motors). Names and colours are kept."""
    from OCP.XCAFDoc import XCAFDoc_ColorSurf
    from OCP.STEPCAFControl import STEPCAFControl_Writer
    from OCP.STEPControl import STEPControl_AsIs
    from OCP.Quantity import Quantity_TOC_RGB
    doc = TDocStd_Document(TCollection_ExtendedString("XmlOcaf"))
    st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main()); ct = XCAFDoc_DocumentTool.ColorTool_s(doc.Main())
    protos = {}
    def nm(label, text): TDataStd_Name.Set_s(label, TCollection_ExtendedString(text))
    def proto(shape, name, color):
        base = shape.Located(TopLoc_Location())
        key = (base.TShape().__hash__(), int(base.Orientation()), color)
        lab = protos.get(key)
        if lab is None:
            lab = st.AddShape(base, False, False); nm(lab, name)
            if color is not None: ct.SetColor(lab, Quantity_Color(*color, Quantity_TOC_RGB), XCAFDoc_ColorSurf)
            protos[key] = lab
        return lab
    def node(a):
        lab = st.NewShape(); nm(lab, a.name)
        for shp in a.shapes:
            w = shp.wrapped
            col = tuple(a.color.toTuple()[:3]) if a.color is not None else None
            c = st.AddComponent(lab, proto(w, a.name, col), w.Location()); nm(c, a.name)
        for ch in a.children:
            c = st.AddComponent(lab, node(ch), ch.loc.wrapped); nm(c, ch.name)
        return lab
    root = st.NewShape(); nm(root, assy.name)
    top = node(assy)
    c = st.AddComponent(root, top, assy.loc.wrapped); nm(c, assy.name)
    st.UpdateAssemblies()
    w = STEPCAFControl_Writer(); w.SetColorMode(True); w.SetNameMode(True)
    w.Transfer(doc, STEPControl_AsIs); w.Write(path)

if __name__ == "__main__":
    # Meshes of the vendor parts for cad/advantagescope/build_model.py (its VENDOR_PKL), in the team CAD's frame (mm):
    #   python3 real_parts.py <example chassis STEP> <LIMELIGHT3ACAD_STEP.stp> <out.pkl> [VENDOR_DIR, for the camera's goBILDA mount]
    import os, pickle, sys
    sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "robot-addons"))
    import importlib.util
    spec = importlib.util.spec_from_file_location("addons", os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "robot-addons", "build.py"))
    A = importlib.util.module_from_spec(spec); spec.loader.exec_module(A)
    out = {}
    def put(key, s, col, kind):
        v, f = s.tessellate(0.3, 0.5)
        out[key] = {"v": [(p.x, p.y, p.z) for p in v], "f": f, "col": col, "kind": kind}
    for n, parts in pods(sys.argv[1], A.POD_MOVE).items():
        for k, (pn, s, col) in enumerate(parts): put(f"{n} / {k:02d} {pn}", s, col, "pod")
    for k, (pn, s, col) in enumerate(limelight_in_cad(sys.argv[2], A.C, A.F, A.FACE)): put(f"Limelight 3A / {k:02d} {pn}", s, col, "camera")
    if len(sys.argv) > 4:
        for k, (pn, s, loc, col) in enumerate(limelight_mount_in_cad(sys.argv[4], A.C, A.F, A.FACE)):
            put(f"Limelight mount / {k:02d} {pn}", cq.Shape.cast(s.wrapped.Moved(loc)), col, "mount")
    pickle.dump(out, open(sys.argv[3], "wb")); print(len(out), "meshes")
