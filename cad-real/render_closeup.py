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
# looking back at the left front corner from ahead of it, to its left, a little above the tile
v = np.array([-0.78, -0.58, -0.16]); v /= np.linalg.norm(v)
right = np.cross(v, [0, 0, 1.0]); right /= np.linalg.norm(right); up = np.cross(right, v)
lo, hi = np.array([3.6, 2.0, -0.2]), np.array([13.5, 9.5, 9.0])          # inches, robot frame
polys, cols = [], []
for g, t in tris:
    if g.startswith("old stick"): continue
    keep = np.all((t > lo) & (t < hi), axis=(1, 2))
    if not keep.any(): continue
    tt = t[keep]; polys.append(tt); cols += [col(g)] * len(tt)
T = np.concatenate(polys); cols = np.array(cols)
depth = T.mean(1) @ v; o = np.argsort(-depth)                          # far first
P2 = np.stack([T @ right, T @ up], -1)[o]
n = np.cross(T[:, 1] - T[:, 0], T[:, 2] - T[:, 0]); n /= np.linalg.norm(n, axis=1, keepdims=True) + 1e-12
shade = 0.55 + 0.45 * np.abs(n @ np.array([0.3, 0.5, 0.8]) / np.linalg.norm([0.3, 0.5, 0.8]))
import matplotlib.colors as mc
rgb = np.array([mc.to_rgb(c) for c in cols]) * shade[:, None]
fig, ax = plt.subplots(figsize=(13, 10), dpi=110)
ax.add_collection(PolyCollection(P2, facecolors=np.clip(rgb[o], 0, 1), edgecolors="none"))
ax.autoscale(); ax.set_aspect("equal"); ax.axis("off")
from matplotlib.patches import Patch
ax.legend(handles=[Patch(color=c, label=k) for k, c in COL.items() if k != "old stick"], loc="lower right", fontsize=9)
ax.set_title("The left front corner, stick down, from ahead and to the left, low: the printed arm (green) over the corner servo, frame and SWYFT wheels (orange) and the front mecanum (yellow)", fontsize=10)
fig.tight_layout(); fig.savefig(sys.argv[2])
print(len(T), "triangles")
