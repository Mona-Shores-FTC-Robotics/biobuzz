"""Read the mentor's robot STEP: every leaf part's triangles (mm, STEP frame) with its assembly path, cached."""
import pickle, sys, numpy as np
from OCP.BRep import BRep_Tool
from OCP.BRepMesh import BRepMesh_IncrementalMesh
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TCollection import TCollection_ExtendedString
from OCP.TDataStd import TDataStd_Name
from OCP.TDF import TDF_Label, TDF_LabelSequence
from OCP.TDocStd import TDocStd_Document
from OCP.TopAbs import TopAbs_FACE
from OCP.TopExp import TopExp_Explorer
from OCP.TopLoc import TopLoc_Location
from OCP.TopoDS import TopoDS
from OCP.XCAFDoc import XCAFDoc_DocumentTool
path, out_pkl = sys.argv[1], sys.argv[2]
doc = TDocStd_Document(TCollection_ExtendedString("doc"))
r = STEPCAFControl_Reader(); r.SetNameMode(True); r.ReadFile(path); r.Transfer(doc)
st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())
def name(label):
    a = TDataStd_Name()
    return a.Get().ToExtString() if label.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
out = []
def walk(label, loc, p):
    if st.IsReference_s(label):
        ref = TDF_Label(); st.GetReferredShape_s(label, ref)
        walk(ref, loc.Multiplied(st.GetLocation_s(label)), p + [name(label)]); return
    if st.IsAssembly_s(label):
        comps = TDF_LabelSequence(); st.GetComponents_s(label, comps)
        for i in range(1, comps.Length() + 1): walk(comps.Value(i), loc, p)
        return
    shape = st.GetShape_s(label).Moved(loc)
    BRepMesh_IncrementalMesh(shape, 0.8, False, 0.5, True)
    tris = []
    ex = TopExp_Explorer(shape, TopAbs_FACE)
    while ex.More():
        f = TopoDS.Face_s(ex.Current()); L = TopLoc_Location()
        tri = BRep_Tool.Triangulation_s(f, L)
        if tri is not None:
            T = L.Transformation()
            pts = np.array([[*tri.Node(i).Transformed(T).Coord()] for i in range(1, tri.NbNodes() + 1)])
            for i in range(1, tri.NbTriangles() + 1):
                a, b, c = tri.Triangle(i).Get(); tris.append(pts[[a - 1, b - 1, c - 1]])
        ex.Next()
    if tris: out.append((" / ".join(p), np.array(tris, np.float32)))
free = TDF_LabelSequence(); st.GetFreeShapes(free)
for i in range(1, free.Length() + 1): walk(free.Value(i), TopLoc_Location(), [])
pickle.dump(out, open(out_pkl, "wb")); print(len(out), "parts")
