# Read the STEP with names and placements; list holes on the parts at the robot's front.
import pickle, time
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TDocStd import TDocStd_Document
from OCP.TCollection import TCollection_ExtendedString
from OCP.XCAFDoc import XCAFDoc_DocumentTool
from OCP.TDF import TDF_LabelSequence, TDF_Label
from OCP.TDataStd import TDataStd_Name
from OCP.TopLoc import TopLoc_Location
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
from OCP.TopExp import TopExp_Explorer
from OCP.TopAbs import TopAbs_FACE
from OCP.BRepAdaptor import BRepAdaptor_Surface
from OCP.GeomAbs import GeomAbs_Cylinder
from OCP.TopoDS import TopoDS
t0=time.time()
doc=TDocStd_Document(TCollection_ExtendedString("doc"))
r=STEPCAFControl_Reader(); r.SetNameMode(True)
r.ReadFile('robot.step'); r.Transfer(doc)
st=XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())
print('read', round(time.time()-t0), 's')
def name(l):
    a=TDataStd_Name()
    return a.Get().ToExtString() if l.FindAttribute(TDataStd_Name.GetID_s(), a) else '?'
IN=25.4
leaves=[]
def walk(l, loc, path):
    if st.IsReference_s(l):
        ref=TDF_Label(); st.GetReferredShape_s(l, ref)
        walk(ref, loc.Multiplied(st.GetLocation_s(l)), path+[name(l)]); return
    if st.IsAssembly_s(l):
        seq=TDF_LabelSequence(); st.GetComponents_s(l, seq)
        for i in range(1, seq.Length()+1): walk(seq.Value(i), loc, path)
        return
    shp=st.GetShape_s(l).Moved(loc)
    leaves.append((path, shp))
roots=TDF_LabelSequence(); st.GetFreeShapes(roots)
for i in range(1, roots.Length()+1): walk(roots.Value(i), TopLoc_Location(), [])
print('leaves', len(leaves))
# units: find bbox of everything
out=[]
for path,shp in leaves:
    b=Bnd_Box(); BRepBndLib.Add_s(shp,b); x0,y0,z0,x1,y1,z1=b.Get()
    out.append((path,(x0,y0,z0,x1,y1,z1),shp))
xs=[o[1] for o in out]; import numpy as np; A=np.array(xs)
print('overall mm?', A[:,:3].min(0).round(1), A[:,3:].max(0).round(1))
pickle.dump([(p,bb) for p,bb,_ in out], open('leaves_bb.pkl','wb'))
# holes on parts touching the front 2.5 in of the robot (CAD z > front-2.5in) and below 9 in
front=A[:,5].max()
sel=[o for o in out if o[1][5] > front-2.5*IN and o[1][1] < A[:,1].min()+9*IN]
print('front parts', len(sel))
holes=[]
for path,bb,shp in sel:
    nm=' / '.join(path[-2:])
    if any(k in nm for k in ('roller','text','Gecko','e-clip','shim')): continue
    ex=TopExp_Explorer(shp, TopAbs_FACE)
    while ex.More():
        f=TopoDS.Face_s(ex.Current()); s=BRepAdaptor_Surface(f)
        if s.GetType()==GeomAbs_Cylinder:
            c=s.Cylinder(); rad=c.Radius()
            if 1.4 < rad < 4.6:
                ax=c.Axis(); p=ax.Location(); d=ax.Direction()
                # centre the hole along its face extent
                fb=Bnd_Box(); BRepBndLib.Add_s(f,fb); cx=[(fb.Get()[i]+fb.Get()[i+3])/2 for i in range(3)]
                holes.append((nm, round(rad*2,2), tuple(round(v,3) for v in cx), (round(d.X(),2),round(d.Y(),2),round(d.Z(),2))))
        ex.Next()
pickle.dump(holes, open('front_holes.pkl','wb'))
print('holes', len(holes), 'time', round(time.time()-t0))
