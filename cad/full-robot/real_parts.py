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

def to_model(C, F, FACE, back_in=7.56):
    """Team CAD mm (x across, y up, z forward) -> the model frame, mm (X forward, Y left, Z up, origin on the floor
    under the chassis centre, back_in inches behind the face)."""
    t = gp_Trsf(); t.SetValues(0, 0, 1, -(FACE - back_in * 25.4), 1, 0, 0, -C, 0, 1, 0, -F); return t

def limelight_in_cad(ll_step, C, F, FACE):
    """[(part name, shape, colour)] of the Limelight 3A at its lens pose, in the team CAD's frame."""
    t = TopLoc_Location(to_model(C, F, FACE).Inverted().Multiplied(limelight_trsf()))
    return [(names[-1] if names else "body", cq.Shape.cast(s.wrapped.Moved(t)), col) for names, s, col in read_leaves(ll_step)]

if __name__ == "__main__":
    # Meshes of the vendor parts for cad/advantagescope/build_model.py (its VENDOR_PKL), in the team CAD's frame (mm):
    #   python3 real_parts.py <example chassis STEP> <LIMELIGHT3ACAD_STEP.stp> <out.pkl>
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
    pickle.dump(out, open(sys.argv[3], "wb")); print(len(out), "meshes")
