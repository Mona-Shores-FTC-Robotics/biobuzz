"""Pull one FLOWER (am-5855 Flower Assembly) and its four staged POLLEN out of FIRST's field CAD as meshes.

    FIRST's field CAD, STEP: https://ftc-resources.firstinspires.org/ftc/field/field-cad-step (a zip of one STEP)
    python3 -I tools/robot-cad/flower_from_field_cad.py field-cad-step.step flower_cad.pkl

Writes {part name: trimesh} in a FLOWER frame: inches, origin on the tile top under the bottom POLLEN's centre, +x from
the robot toward the wall, +y left (facing the wall), +z up. The numbers in flower.py's docstring were read off this on
16 Oct 2026. About 30 s. Needs the OCP that comes with cadquery.
"""
import sys, re, pickle, numpy as np, trimesh
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TDocStd import TDocStd_Document
from OCP.TCollection import TCollection_ExtendedString
from OCP.XCAFDoc import XCAFDoc_DocumentTool
from OCP.TDF import TDF_LabelSequence, TDF_Label
from OCP.TDataStd import TDataStd_Name
from OCP.TopLoc import TopLoc_Location
from OCP.BRepMesh import BRepMesh_IncrementalMesh
from OCP.TopExp import TopExp_Explorer
from OCP.TopAbs import TopAbs_FACE
from OCP.BRep import BRep_Tool
from OCP.TopoDS import TopoDS
doc = TDocStd_Document(TCollection_ExtendedString("doc"))
r = STEPCAFControl_Reader(); r.SetNameMode(True); r.ReadFile(sys.argv[1]); r.Transfer(doc)
st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())
def name(l):
    a = TDataStd_Name(); return a.Get().ToExtString() if l.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
def mesh(shp):
    BRepMesh_IncrementalMesh(shp, 0.2, False, 0.3, True)
    V, F = [], []; ex = TopExp_Explorer(shp, TopAbs_FACE)
    while ex.More():
        f = TopoDS.Face_s(ex.Current()); loc = TopLoc_Location(); t = BRep_Tool.Triangulation_s(f, loc)
        if t is not None:
            tr = loc.Transformation(); n0 = len(V)
            for i in range(1, t.NbNodes() + 1):
                p = t.Node(i).Transformed(tr); V.append((p.X(), p.Y(), p.Z()))
            rev = f.Orientation().name == "TopAbs_REVERSED"
            for i in range(1, t.NbTriangles() + 1):
                a, b, c = t.Triangle(i).Get(); F.append((n0 + a - 1, n0 + c - 1, n0 + b - 1) if rev else (n0 + a - 1, n0 + b - 1, n0 + c - 1))
        ex.Next()
    return np.array(V) / 25.4, np.array(F)
parts = []
def walk(l, loc, path, depth):
    if st.IsReference_s(l):
        ref = TDF_Label(); st.GetReferredShape_s(l, ref)
        walk(ref, loc.Multiplied(st.GetLocation_s(l)), path + [name(l)], depth); return
    p = path + [name(l)]; s = " / ".join(p)
    if "Flower Assembly <2>" in s and not st.IsAssembly_s(l):
        parts.append((s, mesh(st.GetShape_s(l).Moved(loc))))
    elif "Pollen" in s and not st.IsAssembly_s(l):
        v, f = mesh(st.GetShape_s(l).Moved(loc)); c = v.mean(0)
        if 20 < c[0] < 27 and 64 < c[2] < 72: parts.append((s, (v, f)))
    if st.IsAssembly_s(l):
        seq = TDF_LabelSequence(); st.GetComponents_s(l, seq)
        for i in range(1, seq.Length() + 1): walk(seq.Value(i), loc, p, depth + 1)
roots = TDF_LabelSequence(); st.GetFreeShapes(roots)
for i in range(1, roots.Length() + 1): walk(roots.Value(i), TopLoc_Location(), [], 0)
# frame: field X -> y (left when facing the wall at +Z), field Z -> x (toward the wall), field Y -> z (up)
pol = [(s, vf) for s, vf in parts if "Pollen" in s]
bottom = min(pol, key=lambda t: t[1][0][:, 1].mean())
c = bottom[1][0].mean(0); print("bottom POLLEN centre (field X, Y, Z):", c.round(3))
out = {}
for s, (v, f) in parts:
    w = np.c_[v[:, 2] - c[2], -(v[:, 0] - c[0]), v[:, 1] - 0.0]
    m = trimesh.Trimesh(w, f, process=True); n = s.split(" / ")[-1]
    k = n; j = 2
    while k in out: k = f"{n} #{j}"; j += 1
    out[k] = m
pickle.dump(out, open(sys.argv[2], "wb"))
for k, m in out.items():
    b = m.bounds; print(f"{k[:48]:48s} x {b[0][0]:6.2f}..{b[1][0]:6.2f}  y {b[0][1]:6.2f}..{b[1][1]:6.2f}  z {b[0][2]:6.2f}..{b[1][2]:6.2f}")
