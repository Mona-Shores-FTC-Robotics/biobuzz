"""corners_and_stick.step, the mentor's scope (Hub, 26 Nov): the whole chassis, the Limelight and its tower, the hubs, the
intake front, the FLOWER stick's kept parts (servo, hub, tip shaft, mountain) with the printed arms on them, the corner
servos, frames and wheels as their own named parts, and the old aluminium arms as a group to hide. In the STEP's own frame.
Left out: the launcher, the transfer, game pieces, fasteners, belts, sprockets, motors, the battery, the mecanum wheels'
insides (each wheel is a plain cylinder)."""
import sys, re, pickle, numpy as np, cadquery as cq
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
from OCP.STEPCAFControl import STEPCAFControl_Reader, STEPCAFControl_Writer
from OCP.STEPControl import STEPControl_AsIs
from OCP.TCollection import TCollection_ExtendedString
from OCP.TDataStd import TDataStd_Name
from OCP.TDF import TDF_Label, TDF_LabelSequence
from OCP.TDocStd import TDocStd_Document
from OCP.TopLoc import TopLoc_Location
from OCP.XCAFDoc import XCAFDoc_DocumentTool, XCAFDoc_ColorType
from OCP.Quantity import Quantity_Color, Quantity_TOC_RGB
from OCP.Interface import Interface_Static
CAD = "/home/user/biobuzz/cad/flower-printed-arms"
sys.path.insert(0, CAD)
import geometry as G, check, parts
src, out = sys.argv[1], sys.argv[2]
raw = pickle.load(open("tris.pkl", "rb"))
axles = [t.reshape(-1, 3) for n, t in raw if "Wheel Assembly" in n and "Shaft" in n]
c = np.array([(p.min(0) + p.max(0)) / 2 for p in axles]).mean(0)
floor = c[1] + 48.0
R2S = np.array([[0, 1, 0, c[0]], [0, 0, -1, floor], [-1, 0, 0, c[2]], [0, 0, 0, 1.0]])   # robot mm -> STEP mm
def to_robot(p):
    p = np.atleast_2d(p); return np.c_[-(p[:, 2] - c[2]), p[:, 0] - c[0], -(p[:, 1] - floor)]
def mat(M): return cq.Matrix([list(map(float, r)) for r in M[:3]])
def robot_shape(s): return s.transformGeometry(mat(R2S))

DROP = re.compile(r"(?i)fixed wheel launcher|pollen|nectar|screw|\bnut\b|washer|shcs|bhcs|fhcs|zip|battery|_9\d{4}A\d{3}|"
                  r"belt|sprocket|motor|gearbox|yellowjacket|transfer|gecko|1201-0043|3312-|1222-0001|wheel assembly|5000-000|0096 text")
OLD = re.compile(r"9 Hole Mini U-Channel|1105-0003-0080|3400-0014-0032|8x14x4mm Bearing")
KEPT = re.compile(r"Servo - Speed \(SWYFT\)|Compact Servo Block|Servo Hub Shaft|Pattern Spacer - 16mm|384mm Steel Shaft|"
                  r"312mm Steel Shaft|Flower Ramp|Servo Shaft \(GB\)")
groups = {}
def add(g, n, s): groups.setdefault(g, []).append((n, s))
wheels = {}
def classify(path, lo, hi):
    n = " / ".join(path); leaf = path[-1] if path else "?"
    m = re.search(r"Wheel Assembly <\d>", n)
    if m: return "wheel:" + m.group(0)
    if DROP.search(n): return None
    if OLD.search(leaf) and hi[0] > 11.0 * 25.4 or (leaf.startswith("Part 1") and lo[0] > 11.5 * 25.4 and hi[2] < 1.3 * 25.4):
        return "old stick, for reference (hide)"
    if KEPT.search(leaf): return "flower stick: kept parts (servo, hub, tip shaft, mountain)"
    if "Limelight" in n: return "limelight"
    if lo[2] > 6.0 * 25.4 and 1.4 * 25.4 < lo[0] < 4.5 * 25.4 and abs(lo[1] + hi[1]) < 60: return "limelight tower"
    if n.startswith("Hubs"): return "control hub and expansion hub"
    if "Intake" in n: return "intake"
    return "chassis"
doc = TDocStd_Document(TCollection_ExtendedString("doc"))
r = STEPCAFControl_Reader(); r.SetNameMode(True); r.ReadFile(src); r.Transfer(doc)
st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())
def name(label):
    a = TDataStd_Name(); return a.Get().ToExtString() if label.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
def walk(label, loc, p):
    if st.IsReference_s(label):
        ref = TDF_Label(); st.GetReferredShape_s(label, ref)
        walk(ref, loc.Multiplied(st.GetLocation_s(label)), p + [name(label)]); return
    if st.IsAssembly_s(label):
        comps = TDF_LabelSequence(); st.GetComponents_s(label, comps)
        for i in range(1, comps.Length() + 1): walk(comps.Value(i), loc, p)
        return
    shape = st.GetShape_s(label).Moved(loc)
    b = Bnd_Box(); BRepBndLib.Add_s(shape, b)
    if b.IsVoid(): return
    x0, y0, z0, x1, y1, z1 = b.Get()
    q = to_robot(np.array([[x, y, z] for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)]))
    g = classify(p, q.min(0), q.max(0))
    if g is None: return
    if g.startswith("wheel:"):
        w = wheels.setdefault(g[6:], [q.min(0), q.max(0)]); w[0] = np.minimum(w[0], q.min(0)); w[1] = np.maximum(w[1], q.max(0)); return
    if g.startswith("control hub") and re.search(r"Control Hub|Expansion Hub", p[-1]):
        from OCP.BRepPrimAPI import BRepPrimAPI_MakeBox
        from OCP.gp import gp_Pnt
        shape = BRepPrimAPI_MakeBox(gp_Pnt(x0, y0, z0), gp_Pnt(x1, y1, z1)).Shape()
        add(g, re.sub(r" <\d+>", "", p[-1]) + " (its outer box)", shape); return
    add(g, re.sub(r" <\d+>", "", p[-1]), shape)
free = TDF_LabelSequence(); st.GetFreeShapes(free)
for i in range(1, free.Length() + 1): walk(free.Value(i), TopLoc_Location(), [])
# the mecanum wheels: plain cylinders on their axles
for k, (lo, hi) in sorted(wheels.items()):
    ctr = (lo + hi) / 2; rad = (hi[2] - lo[2]) / 2; w = 38.0
    cy = ctr[1] - np.sign(ctr[1]) * (hi[1] - lo[1]) / 2 + np.sign(ctr[1]) * 0   # the module's outer face
    s = cq.Solid.makeCylinder(rad, w, cq.Vector(ctr[0], (hi[1] if ctr[1] > 0 else lo[1]) - (w if ctr[1] > 0 else 0), rad), cq.Vector(0, 1, 0))
    add("mecanum wheels (plain cylinders)", f"mecanum wheel {'front' if ctr[0] > 0 else 'back'} {'left' if ctr[1] > 0 else 'right'}", robot_shape(s))
# the corners
for side, L in (("left", "L"), ("right", "R")):
    cs = check.corner_solids(side, real_wheel=True)
    b = G.corner_boxes(side)
    sv, tb = b["servo"], b["tabs"]
    servo = cq.Workplane("XY").box(*(sv[2] - sv[1]), centered=False).translate(tuple(sv[1]))
    ears = cq.Workplane("XY").box(3.5, sv[2][1] - sv[1][1], tb[2][2] - tb[1][2], centered=False).translate((float(G.CORNER["flange_x"] if "flange_x" in G.CORNER else sv[1][0] + G.CORNER["ear_x"]), float(sv[1][1]), float(tb[1][2])))
    spline = cq.Workplane("YZ").circle(3.0).extrude(6.0).translate((float(sv[2][0]), float(b["wheel"][1][1]), float(b["wheel"][1][2])))
    add(f"corner {L}", f"corner servo {L} (goBILDA 2000-0025-0003 Speed, simplified: body, ears, spline)", robot_shape(servo.union(ears).union(spline).val()))
    add(f"corner {L}", f"corner wheels {L} (2 x SWYFT 3.5 in, 1/2 in hex)", robot_shape(cs["wheel"]))
    add(f"corner {L}", f"corner wheel screw and washer {L} (guess)", robot_shape(cs["screw"]))
    f = b["frame"]; lo, hi = f[1], f[2]; wy = b["wheel"][1][1]; zc = G.CORNER["frame_z"]
    plate = cq.Workplane("XY").box(*(hi - lo), centered=False).translate(tuple(lo))
    plate = plate.cut(cq.Workplane("XY").box(20.0, 21.0, 41.0).translate((float(lo[0] + 3), float(wy), float(zc))))
    wall = lo[1] if side == "left" else hi[1]; into = 1.0 if side == "left" else -1.0
    for dz in (-24.0, 0.0, 8.0, 24.0):                 # its M4 edge holes, on the wall's hole column
        plate = plate.cut(cq.Workplane("XZ").circle(1.65).extrude(-10.0 * into).translate((float((lo[0] + hi[0]) / 2), float(wall), float(zc + dz))))
    add(f"corner {L}", f"corner servo frame {L} (goBILDA 1802-0043-0001, simplified: outline, window, edge holes)", robot_shape(plate.val()))
# the printed stick, down (162 deg), on the STEP's pivot and arm bands
arm = cq.importers.importStep(f"{CAD}/arm.step").val()
foot = cq.importers.importStep(f"{CAD}/foot_skid.step").val()
for side, L in (("left", "L"), ("right", "R")):
    add("printed stick (new)", f"printed arm {L}", robot_shape(arm.transformGeometry(mat(check.placement(side, 162.0)))))
    add("printed stick (new)", f"foot skid {L}", robot_shape(foot.transformGeometry(mat(check.foot_placement(side, 162.0)))))
tip = G.arm_to_robot(240.0, 0.0, 0.0, 162.0)[0]
add("printed stick (new)", "REX shaft collar (replaces the 8T sprocket; goBILDA 2910 style, 18 mm OD, a guess)",
    robot_shape(cq.Solid.makeCylinder(9.0, 8.0, cq.Vector(tip[0], G.ARM_Y["left"][0] - 8.0, tip[2]), cq.Vector(0, 1, 0))))
for g, v in groups.items(): print(f"{len(v):4d}  {g}")
import os
if os.environ.get("GROUP_SIZES"):
    from OCP.STEPControl import STEPControl_Writer
    for g, v in groups.items():
        for n, sh in v:
            w0 = STEPControl_Writer(); Interface_Static.SetIVal_s("write.surfacecurve.mode", 0)
            w0.Transfer(getattr(sh, "wrapped", sh), STEPControl_AsIs); w0.Write("/tmp/claude-0/-home-user-biobuzz/600e543d-5764-51c2-b842-9cc1c9117758/scratchpad/one.step")
            print("SIZE", round(os.path.getsize("/tmp/claude-0/-home-user-biobuzz/600e543d-5764-51c2-b842-9cc1c9117758/scratchpad/one.step") / 1e6, 2), g, "|", n, flush=True)
    raise SystemExit
d2 = TDocStd_Document(TCollection_ExtendedString("out"))
st2 = XCAFDoc_DocumentTool.ShapeTool_s(d2.Main()); ct = XCAFDoc_DocumentTool.ColorTool_s(d2.Main())
COL = {"corner": (0.95, 0.55, 0.15), "printed": (0.2, 0.7, 0.3), "old": (0.6, 0.6, 0.65), "limelight": (0.1, 0.1, 0.1)}
root = st2.NewShape(); TDataStd_Name.Set_s(root, TCollection_ExtendedString("corners_and_stick"))
for g, items in groups.items():
    ga = st2.NewShape(); TDataStd_Name.Set_s(ga, TCollection_ExtendedString(g))
    for n, sh in items:
        lab = st2.AddShape(getattr(sh, "wrapped", sh), False); TDataStd_Name.Set_s(lab, TCollection_ExtendedString(n))
        for k, col in COL.items():
            if g.startswith(k): ct.SetColor(lab, Quantity_Color(*col, Quantity_TOC_RGB), XCAFDoc_ColorType.XCAFDoc_ColorSurf)
        comp = st2.AddComponent(ga, lab, TopLoc_Location()); TDataStd_Name.Set_s(comp, TCollection_ExtendedString(n))
    comp = st2.AddComponent(root, ga, TopLoc_Location()); TDataStd_Name.Set_s(comp, TCollection_ExtendedString(g))
st2.UpdateAssemblies()
Interface_Static.SetIVal_s("write.surfacecurve.mode", 0)
w = STEPCAFControl_Writer(); w.SetNameMode(True); w.SetColorMode(True)
w.Transfer(d2, STEPControl_AsIs); w.Write(out + ".raw")
num = re.compile(r"(\d\.\d{4})\d+")                 # 0.0001 mm is plenty; the digits after are most of the file
with open(out + ".raw") as fi, open(out, "w") as fo:
    for line in fi:
        fo.write(num.sub(r"\1", line) if not line.startswith(("FILE_", "ISO-")) else line)
import os; os.remove(out + ".raw")
