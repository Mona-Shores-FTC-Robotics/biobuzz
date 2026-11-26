"""Strip the robot STEP to the front corners, the intake front and the FLOWER stick, in the STEP's own frame."""
import sys, numpy as np, cadquery as cq
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TCollection import TCollection_ExtendedString
from OCP.TDataStd import TDataStd_Name
from OCP.TDF import TDF_Label, TDF_LabelSequence
from OCP.TDocStd import TDocStd_Document
from OCP.TopLoc import TopLoc_Location
from OCP.XCAFDoc import XCAFDoc_DocumentTool
import pickle
src, out = sys.argv[1], sys.argv[2]
raw = pickle.load(open("tris.pkl", "rb"))
axles = [t.reshape(-1, 3) for n, t in raw if "Wheel Assembly" in n and "Shaft" in n]
c = np.array([(p.min(0) + p.max(0)) / 2 for p in axles]).mean(0)
floor = c[1] + 48.0
def to_robot(p):
    p = np.atleast_2d(p); return np.c_[-(p[:, 2] - c[2]), p[:, 0] - c[0], -(p[:, 1] - floor)] / 25.4
doc = TDocStd_Document(TCollection_ExtendedString("doc"))
r = STEPCAFControl_Reader(); r.SetNameMode(True); r.ReadFile(src); r.Transfer(doc)
st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())
def name(label):
    a = TDataStd_Name(); return a.Get().ToExtString() if label.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
keep, seen = [], 0
import re
SKIP = re.compile(r"(?i)pollen|nectar|screw|nut\b|washer|shcs|bhcs|fhcs|zip|battery|control hub|expansion hub|switch|limelight|camera|viper|gecko|1201-0043|5103 gearbox|5000-000|gearbox bearing|roller (core|cover|axle|shim|e-clip)|1600-0307|launcher|0096 core|3411-|htd5|1222-0001|hub \d|motor|Part 2|0096 text|\b28\d\d-000|_9\d{4}A\d{3}")
def wanted(n, lo, hi):
    if SKIP.search(n): return False
    stick = ("Intake Copy 2" in n and hi[0] > 4.5) or any(k in n for k in ("312mm Steel Shaft", "Servo Hub Shaft", "Flower Ramp", "Servo - Speed (SWYFT)", "Compact Servo Block", "8x14x4mm Bearing"))
    front = hi[0] > 5.0 and lo[2] < 9.0 and lo[0] > 1.0
    return stick or front
def walk(label, loc, p):
    global seen
    if st.IsReference_s(label):
        ref = TDF_Label(); st.GetReferredShape_s(label, ref)
        walk(ref, loc.Multiplied(st.GetLocation_s(label)), p + [name(label)]); return
    if st.IsAssembly_s(label):
        comps = TDF_LabelSequence(); st.GetComponents_s(label, comps)
        for i in range(1, comps.Length() + 1): walk(comps.Value(i), loc, p)
        return
    shape = st.GetShape_s(label).Moved(loc); seen += 1
    b = Bnd_Box(); BRepBndLib.Add_s(shape, b)
    if b.IsVoid(): return
    x0, y0, z0, x1, y1, z1 = b.Get()
    corners = to_robot(np.array([[x, y, z] for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)]))
    n = " / ".join(p)
    if wanted(n, corners.min(0), corners.max(0)): keep.append((n, shape))
free = TDF_LabelSequence(); st.GetFreeShapes(free)
for i in range(1, free.Length() + 1): walk(free.Value(i), TopLoc_Location(), [])
print(len(keep), "of", seen, "parts kept")
# a flat XCAF document: one named part per kept solid, written without pcurves (Onshape doesn't need them)
from OCP.Interface import Interface_Static
from OCP.STEPCAFControl import STEPCAFControl_Writer
from OCP.STEPControl import STEPControl_AsIs
from OCP.TDataStd import TDataStd_Name as _N
d2 = TDocStd_Document(TCollection_ExtendedString("out"))
st2 = XCAFDoc_DocumentTool.ShapeTool_s(d2.Main())
root = st2.NewShape()
_N.Set_s(root, TCollection_ExtendedString("corners_and_stick"))
for n, sh in keep:
    lab = st2.AddShape(sh, False)
    _N.Set_s(lab, TCollection_ExtendedString(n.split(" / ")[-1]))
    comp = st2.AddComponent(root, lab, TopLoc_Location())
    _N.Set_s(comp, TCollection_ExtendedString(n.replace(" / ", "__")[-110:]))
st2.UpdateAssemblies()
Interface_Static.SetIVal_s("write.surfacecurve.mode", 0)
w = STEPCAFControl_Writer(); w.SetNameMode(True)
w.Transfer(d2, STEPControl_AsIs); w.Write(out)
