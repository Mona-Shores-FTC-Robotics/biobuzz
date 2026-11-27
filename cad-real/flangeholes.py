"""The intake side walls' holes near the corners, robot frame (mm)."""
import sys, re, pickle, numpy as np
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TCollection import TCollection_ExtendedString
from OCP.TDataStd import TDataStd_Name
from OCP.TDF import TDF_Label, TDF_LabelSequence
from OCP.TDocStd import TDocStd_Document
from OCP.TopLoc import TopLoc_Location
from OCP.XCAFDoc import XCAFDoc_DocumentTool
import cadquery as cq
from OCP.BRepAdaptor import BRepAdaptor_Surface
raw = pickle.load(open("tris.pkl", "rb"))
axles = [t.reshape(-1, 3) for n, t in raw if "Wheel Assembly" in n and "Shaft" in n]
c = np.array([(p.min(0) + p.max(0)) / 2 for p in axles]).mean(0); floor = c[1] + 48.0
def to_robot(p):
    p = np.atleast_2d(p); return np.c_[-(p[:, 2] - c[2]), p[:, 0] - c[0], -(p[:, 1] - floor)]
doc = TDocStd_Document(TCollection_ExtendedString("doc"))
r = STEPCAFControl_Reader(); r.SetNameMode(True); r.ReadFile(sys.argv[1]); r.Transfer(doc)
st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())
def name(l):
    a = TDataStd_Name(); return a.Get().ToExtString() if l.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
def walk(label, loc, p):
    if st.IsReference_s(label):
        ref = TDF_Label(); st.GetReferredShape_s(label, ref); walk(ref, loc.Multiplied(st.GetLocation_s(label)), p + [name(label)]); return
    if st.IsAssembly_s(label):
        cs = TDF_LabelSequence(); st.GetComponents_s(label, cs)
        for i in range(1, cs.Length() + 1): walk(cs.Value(i), loc, p)
        return
    n = " / ".join(p)
    if re.search(r"(?i)screw|nut|washer|wheel assembly|launcher|pollen|nectar", n): return
    sh = st.GetShape_s(label).Moved(loc); b = Bnd_Box(); BRepBndLib.Add_s(sh, b)
    if b.IsVoid(): return
    x0, y0, z0, x1, y1, z1 = b.Get()
    q = to_robot(np.array([[x, y, z] for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)])); lo, hi = q.min(0), q.max(0)
    for s in (1, -1):
        if lo[1] <= s * 136.2 + 1 and hi[1] >= s * 136.2 - 1 and hi[0] > 180 and lo[0] < 290 and lo[2] < 110:
            holes = set()
            for f in cq.Shape.cast(sh).Faces():
                if f.geomType() != "CYLINDER": continue
                a = BRepAdaptor_Surface(f.wrapped).Cylinder(); d = a.Axis().Direction(); L = a.Location()
                rr = to_robot(np.array([[L.X(), L.Y(), L.Z()]]))[0]; dd = to_robot(np.array([[d.X(), d.Y(), d.Z()]]))[0] - to_robot(np.zeros((1, 3)))[0]
                if abs(dd[0]) > 0.99 and a.Radius() < 3: holes.add((round(rr[1], 1), round(rr[2], 1), round(a.Radius(), 2)))
            print("SIDE", s, n[-90:], "x", lo[0].round(1), hi[0].round(1), "y", lo[1].round(1), hi[1].round(1), "z", lo[2].round(1), hi[2].round(1))
            for h in sorted(holes): print("   xhole Y", h[0], "Z", h[1], "r", h[2])
free = TDF_LabelSequence(); st.GetFreeShapes(free)
for i in range(1, free.Length() + 1): walk(free.Value(i), TopLoc_Location(), [])
