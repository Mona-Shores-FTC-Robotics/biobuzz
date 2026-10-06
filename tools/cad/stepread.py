"""Reads a STEP assembly and prints its tree with each part's bounding box in inches, for checking a CAD
design against the simulator's robot (doc/cad-6-oct.md). Needs the OpenCascade bindings: pip install cadquery-ocp
(a few hundred MB). A 140 MB STEP takes about 100 s to read.

    python3 tools/cad/stepread.py "DHS Robot Copy.step" 7 > tree.txt      # 7: how deep into sub-assemblies

Axes are the CAD's own: find the floor from the wheels' lowest point and the front from the intake.
"""
import sys, time
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TDocStd import TDocStd_Document
from OCP.TCollection import TCollection_ExtendedString
from OCP.XCAFDoc import XCAFDoc_DocumentTool
from OCP.TDF import TDF_Label, TDF_ChildIterator
from OCP.TDataStd import TDataStd_Name
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
from OCP.TopLoc import TopLoc_Location
from OCP.IFSelect import IFSelect_RetDone
t0=time.time()
doc=TDocStd_Document(TCollection_ExtendedString("doc"))
rd=STEPCAFControl_Reader(); rd.SetNameMode(True)
st=rd.ReadFile(sys.argv[1]); assert st==IFSelect_RetDone, st
rd.Transfer(doc)
print("read in %.0f s"%(time.time()-t0), flush=True)
shapes=XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())
IN=25.4
def name(l):
    n=TDataStd_Name(); return n.Get().ToExtString() if l.FindAttribute(TDataStd_Name.GetID_s(), n) else "?"
def bbox(shape):
    b=Bnd_Box(); BRepBndLib.Add_s(shape,b,False)
    if b.IsVoid(): return None
    lo,hi=b.CornerMin(),b.CornerMax(); return [v/IN for v in (lo.X(),lo.Y(),lo.Z(),hi.X(),hi.Y(),hi.Z())]
def children(label):
    it=TDF_ChildIterator(label, False); out=[]
    while it.More(): out.append(it.Value()); it.Next()
    return out
out=[]
def walk(label, loc, depth, limit):
    nm=name(label)
    if shapes.IsReference_s(label):
        ref=TDF_Label(); shapes.GetReferredShape_s(label, ref)
        walk(ref, loc.Multiplied(shapes.GetLocation_s(label)), depth, limit); return
    sh=shapes.GetShape_s(label).Moved(loc)
    bb=bbox(sh)
    if bb: out.append((depth, nm, bb))
    if depth<limit and shapes.IsAssembly_s(label):
        for c in children(label): walk(c, loc, depth+1, limit)
roots=[l for l in children(shapes.BaseLabel()) if shapes.IsFree_s(l)]
print("roots:",len(roots))
for r in roots: walk(r, TopLoc_Location(), 0, int(sys.argv[2]))
for depth,nm,bb in out:
    sz=[bb[3]-bb[0],bb[4]-bb[1],bb[5]-bb[2]]
    print("  "*depth+f"{nm[:50]:50s} x {bb[0]:7.2f}..{bb[3]:7.2f}  y {bb[1]:7.2f}..{bb[4]:7.2f}  z {bb[2]:7.2f}..{bb[5]:7.2f}  size {sz[0]:.1f} x {sz[1]:.1f} x {sz[2]:.1f} in")
