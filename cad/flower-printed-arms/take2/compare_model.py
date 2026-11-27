"""One .glb for the Take 1 / Take 2b viewer, robot frame in metres, stick drawn DOWN (162 deg).
Node names: ctx|<group>|<part> (still), swing|kept|<part> and swing|old|<part> (turn with the stick, both takes),
t1|arm|..., t1|corner|..., t2b|arm|..., t2b|corner|... (each take's own; the arms turn)."""
import sys, os, json, base64, pickle, numpy as np, trimesh, cadquery as cq
SP = "/tmp/claude-0/-home-user-biobuzz/600e543d-5764-51c2-b842-9cc1c9117758/scratchpad"
CAD = "/home/user/biobuzz/cad/flower-printed-arms"
sys.path.insert(0, CAD); sys.path.insert(0, CAD + "/take2")
import geometry as G, check, layouts as L
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TCollection import TCollection_ExtendedString
from OCP.TDataStd import TDataStd_Name
from OCP.TDF import TDF_Label, TDF_LabelSequence
from OCP.TDocStd import TDocStd_Document
from OCP.TopLoc import TopLoc_Location
from OCP.XCAFDoc import XCAFDoc_DocumentTool
raw = pickle.load(open(SP + "/realcad/tris.pkl", "rb"))
axles = [t.reshape(-1, 3) for n, t in raw if "Wheel Assembly" in n and "Shaft" in n]
c = np.array([(p.min(0) + p.max(0)) / 2 for p in axles]).mean(0); floor = c[1] + 48.0
S2R = np.array([[0, 0, -1, c[2]], [1, 0, 0, -c[0]], [0, -1, 0, floor], [0, 0, 0, 1.0]])   # STEP mm -> robot mm
scene = trimesh.Scene(); used = {}
def add(name, mesh):
    k = used.get(name, 0); used[name] = k + 1
    scene.add_geometry(mesh, node_name=name if k == 0 else f"{name} {k + 1}", geom_name=f"g{len(scene.geometry)}")
def mesh_of(shape, M=None, tol=0.6):
    v, f = shape.tessellate(tol, 0.4)
    if not f: return None
    V = np.array([[p.x, p.y, p.z] for p in v])
    if M is not None: V = V @ M[:3, :3].T + M[:3, 3]
    return trimesh.Trimesh(V / 1000.0, f, process=False)
# the robot, from the stripped STEP
doc = TDocStd_Document(TCollection_ExtendedString("d"))
r = STEPCAFControl_Reader(); r.SetNameMode(True); r.ReadFile(SP + "/realcad/corners_and_stick_full.step"); r.Transfer(doc)
st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())
def name(l):
    a = TDataStd_Name(); return a.Get().ToExtString() if l.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
SWING = ("384mm Steel Shaft", "312mm Steel Shaft", "Flower Ramp")
def walk(label, loc, p):
    if st.IsReference_s(label):
        ref = TDF_Label(); st.GetReferredShape_s(label, ref); walk(ref, loc.Multiplied(st.GetLocation_s(label)), p + [name(label)]); return
    if st.IsAssembly_s(label):
        cs = TDF_LabelSequence(); st.GetComponents_s(label, cs)
        for i in range(1, cs.Length() + 1): walk(cs.Value(i), loc, p)
        return
    grp = p[1] if len(p) > 1 else "?"; part = p[-1]
    if grp.startswith(("corner", "printed")): return
    m = mesh_of(cq.Shape.cast(st.GetShape_s(label).Moved(loc)), S2R, tol=1.0)
    if m is None: return
    if grp.startswith("old stick"): key = "swing|old|" + part
    elif grp.startswith("flower stick") and part.startswith(SWING): key = "swing|kept|" + part
    else: key = "ctx|" + grp.split(" (")[0].split(":")[0] + "|" + part
    add(key, m)
free = TDF_LabelSequence(); st.GetFreeShapes(free)
for i in range(1, free.Length() + 1): walk(free.Value(i), TopLoc_Location(), [name(free.Value(i))])
def mat(M): return cq.Matrix([list(map(float, r)) for r in M[:3]])
# foot skids: the same on both takes
foot = cq.importers.importStep(CAD + "/foot_skid.step").val()
servo_step = cq.importers.importStep(check.SERVO_STEP).val()
for side, Ls in (("left", "L"), ("right", "R")):
    add(f"swing|kept|foot skid {Ls}", mesh_of(foot, check.foot_placement(side, 162.0)))
    # take 1
    arm1 = trimesh.load(CAD + "/arm.stl"); arm1.apply_transform(check.placement(side, 162.0)); arm1.apply_scale(0.001)
    add(f"t1|arm|printed arm {Ls} (67 mm hump)", arm1)
    cs = check.corner_solids(side, real_wheel=True)
    for k, lab in (("servo", "goBILDA Speed servo"), ("wheel", "2 SWYFT 3.5 in wheels"), ("screw", "wheel screw and washer (guess)"), ("frame", "goBILDA 1802 servo frame (simplified)")):
        add(f"t1|corner|{lab} {Ls}", mesh_of(cs[k]))
    # take 2b
    arm2 = trimesh.load(CAD + "/take2/2b_flat/arm.stl"); arm2.apply_transform(check.placement(side, 162.0)); arm2.apply_scale(0.001)
    add(f"t2b|arm|printed arm {Ls} (28 mm hump)", arm2)
    b = L.boxes_2b(side); wx, wy, wz = b["wheel"][1]; top = wz + b["wheel"][4]
    sy = 1.0 if side == "left" else -1.0
    M = np.array([[-1, 0, 0, wx - 10.0], [0, -sy, 0, wy], [0, 0, -1, top + 10.5 + 16.9], [0, 0, 0, 1.0]])
    add(f"t2b|corner|goBILDA Speed servo, spline down {Ls}", mesh_of(servo_step.transformGeometry(mat(M))))
    _, cc, ax, rr, hh = b["wheel"]
    add(f"t2b|corner|2.5 in wheel, flat {Ls}", mesh_of(cq.Solid.makeCylinder(rr, 2 * hh, cq.Vector(*(cc - ax * hh)), cq.Vector(*ax))))
    _, cc, ax, rr, hh = b["screw"]
    add(f"t2b|corner|wheel screw and washer (guess) {Ls}", mesh_of(cq.Solid.makeCylinder(rr, 2 * hh, cq.Vector(*(cc - ax * hh)), cq.Vector(*ax))))
    add(f"t2b|corner|printed corner mount {Ls}", mesh_of(L.mount_2b(side).val()))
glb = scene.export(file_type="glb")
json.dump({"glb_base64": base64.b64encode(glb).decode(), "pivot_m": [G.PIVOT_XZ[0] / 1000, G.PIVOT_XZ[1] / 1000]},
          open(SP + "/compare/compare-model.json", "w"))
print(len(scene.geometry), "meshes,", round(len(glb) / 1e6, 1), "MB glb")
