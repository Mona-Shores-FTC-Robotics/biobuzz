"""Render corners_and_stick_full.step: iso, front and side, stick down, coloured by group (robot frame, inches)."""
import sys, pickle, numpy as np, matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.collections import PolyCollection
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TCollection import TCollection_ExtendedString
from OCP.TDataStd import TDataStd_Name
from OCP.TDF import TDF_Label, TDF_LabelSequence
from OCP.TDocStd import TDocStd_Document
from OCP.TopLoc import TopLoc_Location
from OCP.XCAFDoc import XCAFDoc_DocumentTool
from OCP.BRepMesh import BRepMesh_IncrementalMesh
from OCP.BRep import BRep_Tool
from OCP.TopExp import TopExp_Explorer
from OCP.TopAbs import TopAbs_FACE
from OCP.TopoDS import TopoDS
raw = pickle.load(open("tris.pkl", "rb"))
axles = [t.reshape(-1, 3) for n, t in raw if "Wheel Assembly" in n and "Shaft" in n]
c = np.array([(p.min(0) + p.max(0)) / 2 for p in axles]).mean(0); floor = c[1] + 48.0
def to_robot(p): return np.stack([-(p[..., 2] - c[2]), p[..., 0] - c[0], -(p[..., 1] - floor)], -1) / 25.4
doc = TDocStd_Document(TCollection_ExtendedString("d"))
r = STEPCAFControl_Reader(); r.SetNameMode(True); r.ReadFile(sys.argv[1]); r.Transfer(doc)
st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())
def name(l):
    a = TDataStd_Name(); return a.Get().ToExtString() if l.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
tris = []
def walk(label, loc, p):
    if st.IsReference_s(label):
        ref = TDF_Label(); st.GetReferredShape_s(label, ref); walk(ref, loc.Multiplied(st.GetLocation_s(label)), p + [name(label)]); return
    if st.IsAssembly_s(label):
        cs = TDF_LabelSequence(); st.GetComponents_s(label, cs)
        for i in range(1, cs.Length() + 1): walk(cs.Value(i), loc, p)
        return
    sh = st.GetShape_s(label).Moved(loc); BRepMesh_IncrementalMesh(sh, 1.0, False, 0.6, True)
    ex = TopExp_Explorer(sh, TopAbs_FACE); out = []
    while ex.More():
        f = TopoDS.Face_s(ex.Current()); L = TopLoc_Location(); tr = BRep_Tool.Triangulation_s(f, L)
        if tr is not None:
            T = L.Transformation(); pts = np.array([[*tr.Node(i).Transformed(T).Coord()] for i in range(1, tr.NbNodes() + 1)])
            out += [pts[[a - 1, b - 1, cc - 1]] for a, b, cc in (tr.Triangle(i).Get() for i in range(1, tr.NbTriangles() + 1))]
        ex.Next()
    if out: tris.append((p[1] if len(p) > 1 else p[0], to_robot(np.array(out))))
free = TDF_LabelSequence(); st.GetFreeShapes(free)
for i in range(1, free.Length() + 1): walk(free.Value(i), TopLoc_Location(), [name(free.Value(i))])
COL = {"corner": "#f08a24", "printed": "#2fa84f", "old stick": "#7b8fd6", "limelight": "#222222", "flower stick": "#c03a3a",
       "mecanum": "#e0c04a", "control hub": "#3b3b3b", "intake": "#a9b0b8", "chassis": "#c9ced4"}
def col(g): return next((v for k, v in COL.items() if g.startswith(k)), "#bbbbbb")
V = {"iso, from front-left": np.array([[0.70, -0.71, 0], [-0.30, -0.30, 0.90]]),
     "front, from ahead": np.array([[0, -1.0, 0], [0, 0, 1.0]]), "side, from the left": np.array([[1.0, 0, 0], [0, 0, 1.0]])}
D = {"iso, from front-left": np.array([0.64, 0.64, 0.42]), "front, from ahead": np.array([1.0, 0, 0]), "side, from the left": np.array([0, 1.0, 0])}
fig, axs = plt.subplots(1, 3, figsize=(21, 8), dpi=100)
for ax, (vn, P) in zip(axs, V.items()):
    polys, cols, depth = [], [], []
    for g, t in tris:
        if vn != "iso, from front-left" and g.startswith("old stick"): continue
        polys.append(t @ P.T); depth.append(t.mean(1) @ D[vn]); cols += [col(g)] * len(t)
    polys = np.concatenate(polys); depth = np.concatenate(depth); o = np.argsort(depth)
    ax.add_collection(PolyCollection(polys[o], facecolors=np.array(cols)[o], edgecolors="none"))
    ax.autoscale(); ax.set_aspect("equal"); ax.set_title(vn); ax.axis("off")
from matplotlib.patches import Patch
fig.legend(handles=[Patch(color=v, label=k) for k, v in COL.items()], loc="lower center", ncol=9)
fig.suptitle("corners_and_stick.step, stick down: printed arms (green), corner servo, frame and wheels (orange), old aluminium arms (blue, hideable; iso only)")
fig.tight_layout(rect=(0, 0.05, 1, 0.95)); fig.savefig(sys.argv[2])
print(len(tris), "parts")
