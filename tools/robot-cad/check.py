import os
import pickle, numpy as np, collections, trimesh
M=pickle.load(open(os.environ.get('MESH','addons_mesh.pkl'),'rb'))
occ,names=pickle.load(open('occ.pkl','rb'))
C,F,FACE=-59.62,-151.75,207.73; HY,HZ=F+2.2*25.4,FACE+0.8*25.4
def page(v): v=np.asarray(v); return np.c_[(C-v[:,0])/25.4,(v[:,1]-F)/25.4,(FACE-v[:,2])/25.4]
def samp(m,dens=0.002):
    msh=trimesh.Trimesh(np.asarray(m['v']),np.asarray(m['f']),process=False)
    return np.vstack([trimesh.sample.sample_surface(msh,max(300,int(msh.area/(dens*645))))[0],msh.vertices])
# occupancy of fixed add-ons (real pods, not stand-ins), excluding things the hook is mounted on by design
add_occ={}
for n,m in M.items():
    if m['grp'] in ('fixed','pod') and 'STAND-IN' not in n:
        for k in map(tuple,np.floor(page(samp(m))/0.1).astype(np.int32)): add_occ.setdefault(k,n)
# static: pods vs robot and vs other add-ons
for n,m in M.items():
    if m['grp']=='pod':
        P=page(samp(m)); hits=collections.Counter()
        for k in map(tuple,np.floor(P/0.1).astype(np.int32)):
            i=occ.get(k)
            if i is not None: hits[names[i].split(' / ')[-1][:28]]+=1
            j=add_occ.get(k)
            if j is not None and j!=n: hits['ADDON '+j[:28]]+=1
        print('static', n[:40], hits.most_common(4), 'lowest point above floor (in)', round(P[:,1].min(),3))
# sweep: hook parts rotate about the hinge axis (x), front lifting
H={n:samp(m) for n,m in M.items() if m['grp']=='hook'}
OK_ROBOT=('72mm Steel Shaft',)
for a in range(0,91,3):
    t=np.radians(a); c,s=np.cos(t),np.sin(t); res=collections.Counter(); fwd=-1e9; low=1e9
    for n,P in H.items():
        dy,dz=P[:,1]-HY,P[:,2]-HZ
        Q=np.c_[P[:,0],HY+dy*c+dz*s,HZ-dy*s+dz*c]
        pg=page(Q); fwd=max(fwd,-pg[:,2].min()); low=min(low,pg[:,1].min())
        for k in map(tuple,np.floor(pg/0.1).astype(np.int32)):
            i=occ.get(k)
            if i is not None and not any(o in names[i] for o in OK_ROBOT): res[(n.split(' ')[0],names[i].split(' / ')[-1][:26])]+=1
            j=add_occ.get(k)
            if j is not None and not (('hinge_hub' in n) and any(w in j for w in ('hinge_stub','servo_shaft','hinge_axle'))): res[(n.split(' ')[0],'ADDON '+j.split(' ')[0])]+=1
    print(a, 'fwd of face %.2f in, lowest %.2f in'%(fwd,low), res.most_common(4) if res else 'clear')
