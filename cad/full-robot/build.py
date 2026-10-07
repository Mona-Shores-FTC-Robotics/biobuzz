"""The whole robot as drawn, in one STEP: the team's CAD (DHS Robot Copy, from Drive) with the parts we replaced left out,
plus the unified front (cad/intake-b/), the transfer (cad/transfer/), goBILDA's odometry pods and Limelight's own 3A, on
a stand-in mount. In the AdvantageScope model's frame: +X forward, +Y left, +Z up, origin on the floor under the chassis
centre (7.56 in behind the front face). Millimetres.

    EXAMPLE_STEP=<example chassis STEP> LL_STEP=<LIMELIGHT3ACAD_STEP.stp> \
        python3 cad/full-robot/build.py <robot.step> <out.step>

Left out of the team's CAD: the old intake's roller, motor, its motor mount and the two pattern spacers, and the staged
NECTARs. The designer's "Launcher Concept" (the goBILDA turret over two pairs of flywheels) is kept as drawn; the
transfer's own turret reference parts are left out in its favour. The old intake's 11-hole channel is raised
8 mm, as the transfer needs."""
import math, os, re, sys, time
import cadquery as cq
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TDocStd import TDocStd_Document
from OCP.TCollection import TCollection_ExtendedString
from OCP.XCAFDoc import XCAFDoc_DocumentTool, XCAFDoc_ColorSurf, XCAFDoc_ColorGen
from OCP.TDF import TDF_LabelSequence, TDF_Label
from OCP.TDataStd import TDataStd_Name
from OCP.TopLoc import TopLoc_Location
from OCP.Quantity import Quantity_Color
from OCP.gp import gp_Trsf, gp_Vec
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "intake-b")); sys.path.insert(0, os.path.join(HERE, "..", "robot-addons"))
import importlib.util
import real_parts as RP
def load(name, path):
    spec = importlib.util.spec_from_file_location(name, path); m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m); return m
IB = load("intake_b", os.path.join(HERE, "..", "intake-b", "build.py"))
TR = load("transfer", os.path.join(HERE, "..", "transfer", "build.py"))
A = IB.A
C, F, FACE, IN = A.C, A.F, A.FACE, A.IN
SKIP = re.compile(r"Intake <1> / (48mm Gecko|240mm Steel|5000|5103|5203|Pattern Spacer|1201-0043)|Nectar|Pollen")
RAISE = re.compile(r"Intake <1> / 11 Hole Lowside")

def read_team(path):
    t0 = time.time()
    doc = TDocStd_Document(TCollection_ExtendedString("doc"))
    r = STEPCAFControl_Reader(); r.SetNameMode(True); r.SetColorMode(True)
    r.ReadFile(path); r.Transfer(doc)
    st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main()); ct = XCAFDoc_DocumentTool.ColorTool_s(doc.Main())
    def name(l):
        a = TDataStd_Name()
        return a.Get().ToExtString() if l.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
    def colour(*labels):
        q = Quantity_Color()
        for l in labels:
            s = st.GetShape_s(l)
            for kind in (XCAFDoc_ColorSurf, XCAFDoc_ColorGen):
                if ct.GetColor(s, kind, q): return (q.Red(), q.Green(), q.Blue())
        return None
    leaves, base = [], {}
    def walk(l, loc, path, col):
        if st.IsReference_s(l):
            ref = TDF_Label(); st.GetReferredShape_s(l, ref)
            walk(ref, loc.Multiplied(st.GetLocation_s(l)), path + [name(l)], colour(l) or col); return
        if st.IsAssembly_s(l):
            seq = TDF_LabelSequence(); st.GetComponents_s(l, seq)
            for i in range(1, seq.Length() + 1): walk(seq.Value(i), loc, path, col)
            return
        key = l.Tag()                                     # one entry per distinct part, so repeated parts stay instances
        if key not in base: base[key] = cq.Shape.cast(st.GetShape_s(l))
        leaves.append((path, key, loc, colour(l) or col or (0.67, 0.7, 0.74)))
    roots = TDF_LabelSequence(); st.GetFreeShapes(roots)
    for i in range(1, roots.Length() + 1): walk(roots.Value(i), TopLoc_Location(), [], None)
    print("team CAD:", len(leaves), "parts,", len(base), "distinct, read in", round(time.time() - t0), "s")
    return leaves, base

def limelight_mount():
    """The stand-in mount under the Limelight 3A at config.json's camera (lens 4.0 in ahead of the centre, 14.0 in up,
    pitched 45 deg up): a beam between the front towers' tops, a plate, and a printed 45 deg wedge under the camera's
    bottom face. The real mount is the Limelight chat's to draw. Robot frame, inches."""
    lens, a = cq.Vector(4.0, 0, 14.0), math.radians(45)
    n, u = cq.Vector(math.cos(a), 0, math.sin(a)), cq.Vector(-math.sin(a), 0, math.cos(a))
    back, low = (RP.LL_LENS[1] - RP.LL_BACK) / IN, (RP.LL_LENS[2] + 2.9) / IN     # lens to the back face; lens to the bottom face
    e0, e1 = lens - u * low - n * back, lens - u * low
    wedge = cq.Workplane("XZ").polyline([(e0.x, e0.z), (e1.x, e1.z), (5.4, 12.25), (min(e0.x, e1.x), 12.25)]).close().extrude(-2.4).translate((0, -1.2, 0))
    plate = cq.Workplane("XY").box(2.7, 2.4, 0.12).translate((5.55, 0, 12.19))
    beam = cq.Workplane("XY").box(0.6, 9.7, 0.63).translate((6.6, 0, 11.815))
    return [("Limelight wedge (print, stand-in)", wedge, (0.18, 0.37, 0.62)), ("Limelight plate (stand-in)", plate, (0.18, 0.37, 0.62)),
            ("Limelight beam between the front towers (stand-in)", beam, (0.67, 0.7, 0.74))]

def main(robot_step, out):
    to_model = RP.to_model(C, F, FACE)
    top = cq.Assembly(name="BIOBUZZ robot (model frame: +X forward, +Y left, +Z up, mm)", loc=cq.Location(to_model))
    team = cq.Assembly(name="team robot CAD (DHS Robot Copy), replaced parts left out")
    kept = 0
    leaves, base = read_team(robot_step)
    pods = RP.pods(os.environ["EXAMPLE_STEP"], A.POD_MOVE) if os.environ.get("EXAMPLE_STEP") else {}
    lift = TopLoc_Location(gp_Trsf()); tr8 = gp_Trsf(); tr8.SetTranslation(gp_Vec(0, 8.0, 0)); lift8 = TopLoc_Location(tr8)
    for i, (path, key, loc, col) in enumerate(leaves):
        p = " / ".join(path)
        if SKIP.search(p): continue
        if RAISE.search(p): loc = lift8.Multiplied(loc)
        team.add(base[key], name=f"{i:04d} {path[-1] if path else '?'}"[:120], loc=cq.Location(loc), color=cq.Color(*col)); kept += 1
    print("kept", kept)
    top.add(team)
    for title, g, d in IB.GROUPS:
        sub = cq.Assembly(name=f"front: {title}")
        for n, (wp, col, kind) in d.items():
            if "STAND-IN" in n and pods: continue
            sub.add(wp, name=n, color=cq.Color(*col))
        if title.startswith("chassis"):
            for n, parts in pods.items():
                pod = cq.Assembly(name=n)
                for k, (pn, shp, col) in enumerate(parts): pod.add(shp, name=f"{k:02d} {pn}"[:120], color=cq.Color(*col))
                sub.add(pod)
        top.add(sub)
    for title, g, d in TR.GROUPS:
        sub = cq.Assembly(name=f"transfer: {title}")
        for n, (wp, col, kind) in d.items():
            if n.startswith("turret_"): continue           # the transfer's turret references; the real turret is in the Launcher Concept
            sub.add(TR.to_cad(wp), name=n, color=cq.Color(*col))
        top.add(sub)
    ll = cq.Assembly(name="Limelight 3A (Limelight's STEP) and its stand-in mount")
    for n, wp, col in limelight_mount(): ll.add(TR.to_cad(wp), name=n, color=cq.Color(*col))
    if os.environ.get("LL_STEP"):
        cam = cq.Assembly(name="Limelight 3A (LIMELIGHT3ACAD_STEP.stp)")
        for k, (pn, shp, col) in enumerate(RP.limelight_in_cad(os.environ["LL_STEP"], C, F, FACE)):
            cam.add(shp, name=f"{k:02d} {pn}"[:120], color=cq.Color(*col))
        ll.add(cam)
    top.add(ll)
    t0 = time.time(); top.save(out); print("wrote", out, os.path.getsize(out) // 1_000_000, "MB in", round(time.time() - t0), "s")

if __name__ == "__main__":
    main(*sys.argv[1:3])
