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
import numpy as np
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
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
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
# Left out of the mentor's CAD: the old intake roller, its motor and mount, its guides on the towers (V-groove bearings and
# their cavities, and the roller's own bearings) and its spacers (our floating roller replaces them); his pinwheel, its
# servo, block and shaft (our FLOWER extractor replaces it); his two 3.5 in star wheels (the transfer's wheel lane
# replaces them); his staged NECTAR and POLLEN; and the launcher's front cross-channel with its two dual blocks (the
# lane's balls run through where it is; the 8-hole channel above still ties the frame).
SKIP = re.compile(r"Intake <1> / (48mm Gecko|240mm Steel|5000|5103|5203|Pattern Spacer|1201-0043|V-Groove|Cavity1|8x14x5mm Bearing|4\.5in OD|Servo|Compact Servo Block)"
                  r"|^3\.5in OD|Nectar|Pollen|Wheel Assembly <\d> / 72mm Steel Shaft"
                  r"|Launcher subassembly <\d> / (312rpm Motor|7x11 hole Aluminum Plate|1 Hole Lowside U-Channel|Mini Quad Block|16t HTD5 Pulley|\[LS\] HTD5 belt 68|Dual Block)")
# (the last line: the flywheel motors, their belts and the plates and blocks that held them under the flywheels, where the
# feeders go; cad/transfer/ draws the motors moved out and up)   # the drive wheels' shafts: our 80 mm ones (outer plates) replace them
RAISE = re.compile(r"Intake <1> / 11 Hole Lowside")    # up TR.CHAN_RAISE (21 mm): a lane NECTAR passes under it
LAUNCHER = re.compile(r"^Launcher Concept|^Dual Block \(GB\)")   # the launcher, turret and the two blocks tying its frame to the chassis
FRONT_OF_LAUNCHER = re.compile(r"^Launcher Concept <1> / (10 Hole Lowside U-Channel \(GB\)|Dual Block \(GB\)|5 Hole U Beam - 40mm \(GB\))\s*$")

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

def placed_team(robot_step):
    """The mentor's parts we keep, lined up and edited: [(path, base shape, TopLoc_Location, colour)]."""
    leaves, base = read_team(robot_step)
    def bbox(key, loc):
        b = Bnd_Box(); BRepBndLib.Add_s(base[key].wrapped.Moved(loc), b); return b.Get()
    # the mentor's exports move his assembly's origin: line it up by the drive rails (front ends at FACE, right one at C+136 mm)
    rails = [bbox(k, l) for pth, k, l, c in leaves if pth and pth[-1].startswith("1107-0015-0384")]
    dz, dx = FACE - max(b[5] for b in rails), (C + 136.0) - max(b[3] for b in rails)
    print(f"aligned by the rails: +{dx:.2f} mm across, +{dz:.2f} mm forward")
    placed_team.align = (dx, dz)
    tr = gp_Trsf(); tr.SetTranslation(gp_Vec(dx, 0, dz)); align = TopLoc_Location(tr)
    def moved(v): t = gp_Trsf(); t.SetTranslation(gp_Vec(*v)); return TopLoc_Location(t)
    out = []
    lean = os.environ.get("LEAN")                     # LEAN=1: leave out fasteners, for a single small download
    for path, key, loc, col in leaves:
        p = " / ".join(path)
        if SKIP.search(p): continue
        if lean and re.search(r"[Ss]crew|Nut|[Ww]asher|2800-|2802-|2812-|2806-|2829-|CAGE|e-clip|shim|_9\d{4}A\d{3}", p): continue
        loc = align.Multiplied(loc)
        if RAISE.search(p): loc = moved((0, TR.CHAN_RAISE * IN, 0)).Multiplied(loc)
        if FRONT_OF_LAUNCHER.search(p):
            b = bbox(key, loc); x_model = ((b[2] + b[5]) / 2 - (FACE - 7.56 * IN)) / IN; y_model = ((b[0] + b[3]) / 2 - C) / IN
            if x_model > -2.0:
                continue                                  # the front cross-channel, its two dual blocks and the two U-beams under it
        if re.search(r"Launcher subassembly <\d> / (64mm Standoff|27mm Standoff|\d+\.?\d*mm Spacer)", p):
            b = bbox(key, loc)
            if (b[1] + b[4]) / 2 - F < 4.3 * IN: continue   # the old motor's standoffs and spacers, where the feeders go
        if LAUNCHER.search(p): loc = moved((0, 0, TR.LAUNCHER_SHIFT * IN)).Multiplied(loc)   # forward: the lane's length sets the 3 NECTAR / 4 POLLEN limit
        out.append((path, base[key], loc, col, key))
    return out

def write_mesh(robot_step, out_pkl):
    """The kept, placed parts as meshes for cad/advantagescope/build_model.py: [(path, vertices (CAD inches), faces, colour)],
    the format tools/robot-cad/slim.py writes. Fasteners and tiny parts are left out, as slim.py does."""
    import pickle
    rows, cache = [], {}
    for path, shp, loc, col, key in placed_team(robot_step):
        p = " / ".join(path)
        if re.search(r"text|e-clip|shim|Screw|screw|Bearing|pins|Nut|nut|Washer|washer|Cavity|2800-|2802-|2829-|CAGE", p): continue
        if key not in cache:
            v, f = shp.tessellate(0.4, 0.5); cache[key] = (np.array([(q.x, q.y, q.z) for q in v]), np.array(f))
        v, f = cache[key]
        if not len(f): continue
        T = loc.Transformation(); M = np.array([[T.Value(i, j) for j in range(1, 5)] for i in range(1, 4)])
        vv = (v @ M[:, :3].T + M[:, 3]) / IN
        if np.linalg.norm(vv.max(0) - vv.min(0)) < 0.35: continue
        rows.append((p, vv, f, col))
    pickle.dump(rows, open(out_pkl, "wb")); print(len(rows), "meshes ->", out_pkl)

def main(robot_step, out, additions=False):
    """The whole robot in the model frame; or, with additions, only our parts, in the frame of the mentor's Robot.step,
    so they drop into his Onshape assembly at its origin (the edits to his parts are listed in cad/transfer/README.md)."""
    team = cq.Assembly(name="team robot CAD (the mentor's Robot.step, 7 Oct), replaced parts left out")
    kept = 0
    pods = RP.pods_inst(os.environ["EXAMPLE_STEP"], A.POD_MOVE) if os.environ.get("EXAMPLE_STEP") else {}
    for i, (path, shp, loc, col, key) in enumerate(placed_team(robot_step)):
        if additions: break
        team.add(shp, name=f"{i:04d} {path[-1] if path else '?'}"[:120], loc=cq.Location(loc), color=cq.Color(*col)); kept += 1
    if additions:
        dx, dz = placed_team.align; t = gp_Trsf(); t.SetTranslation(gp_Vec(-dx, 0, -dz))
        top = cq.Assembly(name="BIOBUZZ additions (the frame of the mentor's Robot.step: insert at the origin)", loc=cq.Location(TopLoc_Location(t)))
    else:
        top = cq.Assembly(name="BIOBUZZ robot (model frame: +X forward, +Y left, +Z up, mm)", loc=cq.Location(RP.to_model(C, F, FACE)))
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
                for k, (pn, shp, loc, col) in enumerate(parts): pod.add(shp, name=f"{k:02d} {pn}"[:120], loc=cq.Location(loc), color=cq.Color(*col))
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
    if sys.argv[1] == "--mesh": write_mesh(*sys.argv[2:4])
    elif sys.argv[1] == "--additions": main(*sys.argv[2:4], additions=True)
    else: main(*sys.argv[1:3])
