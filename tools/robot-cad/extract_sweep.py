# FLOWER extractor concept: arms on the roller shaft (pivot = roller axle), block ahead of the roller. Page frame, inches.
import pickle, numpy as np, collections, sys, itertools
import sweep3 as S
occ,names=pickle.load(open('occ.pkl','rb'))
M=pickle.load(open('intakeb_mesh.pkl','rb'))
C,F,FACE=-59.62,-151.75,207.73
def page(v): v=np.asarray(v); return np.c_[(C-v[:,0])/25.4,(v[:,1]-F)/25.4,(FACE-v[:,2])/25.4]
import trimesh
fo={}
KEEP=lambda n: M[n]['grp'] in ('fixed','vee') and not n.startswith(('servo','stop_','hinge_bearing','servo_bracket')) and 'STAND-IN' not in n
for n,m in M.items():
    if not KEEP(n): continue
    msh=trimesh.Trimesh(np.asarray(m['v']),np.asarray(m['f']),process=False)
    P=np.vstack([trimesh.sample.sample_surface(msh,max(300,int(msh.area/1.3)))[0],msh.vertices])
    for k in map(tuple,np.floor(page(P)/0.1).astype(np.int32)): fo.setdefault(k,n.split(' ')[0])
PZ,PY=1.0,3.35                 # pivot: the roller axle, fwd and up
def build(gap=2.5, armx=2.0, B=None):
    B0=1.94+gap if B is None else B   # block back edge, in ahead of the face
    parts={}
    parts['block']=S.boxpts([B0,-1.36,0.7],[B0+1.4,1.36,1.35])
    parts['cross shaft']=S.boxpts([B0+0.77,-armx,0.86],[B0+1.09,armx,1.18])
    for s in (-1,1):
        a=np.array([PZ,PY]); b=np.array([B0+0.93,1.02]); n=int(np.linalg.norm(b-a)/0.05)
        seg=[a+(b-a)*t for t in np.linspace(0,1,n)]
        pts=[]
        for f,u in seg:
            for dx in (-0.0625,0.0625):
                for du in (-0.3,0.3): pts.append((f, s*armx+dx, u+du))
        parts[f'arm {s}']=np.array(pts)
        parts[f'hub {s}']=S.boxpts([PZ-0.45,s*armx-0.0625,PY-0.45],[PZ+0.45,s*armx+0.0625,PY+0.45])
    pts=np.vstack(list(parts.values())); lab=np.concatenate([[k]*len(v) for k,v in parts.items()])
    return pts,lab
def sweep(pts,lab,angles):
    out=[]
    for a in angles:
        t=np.radians(a); d=pts-[PZ,0,PY]; c,s=np.cos(t),np.sin(t)
        x=d[:,0]*c-d[:,2]*s+PZ; z=d[:,0]*s+d[:,2]*c+PY
        pg=np.c_[pts[:,1],z,-x]; h=collections.Counter()
        for k,l in zip(map(tuple,np.floor(pg/0.1).astype(np.int32)),lab):
            if l.startswith('hub'): continue           # the hubs ride the roller shaft by design
            i=occ.get(k)
            if i is not None: h[(l,names[i].split(' / ')[-1][:22])]+=1
            j=fo.get(k)
            if j is not None and not (l.startswith('hub')): h[(l,'NEW '+j)]+=1
        out.append((a,round(x.max(),2),round(z.max(),2),round(z.min(),2),h.most_common(3)))
    return out
for gap in ():
    pts,lab=build(gap)
    for r in sweep(pts,lab,range(0,181,10)): print(r)
