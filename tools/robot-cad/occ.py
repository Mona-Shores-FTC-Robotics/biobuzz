# Robot occupancy grid (page frame, inches): x right, y up, z back from the front face. Voxel 0.1 in -> part name.
import pickle, numpy as np, trimesh, re
keep=pickle.load(open('keep.pkl','rb'))
X0, FLOOR, FRONT = -2.35, -5.97, 8.178
V=0.1; occ={}
names=[]
for i,(path,v,f,col) in enumerate(keep):
    P=np.c_[X0-v[:,0], v[:,1]-FLOOR, FRONT-v[:,2]]
    m=trimesh.Trimesh(P,f,process=False)
    n=int(min(200000, max(50, m.area/0.002)))
    pts,_=trimesh.sample.sample_surface(m,n)
    pts=np.vstack([pts,P])
    keys=np.unique(np.floor(pts/V).astype(np.int32),axis=0)
    names.append(path)
    for k in map(tuple,keys): occ.setdefault(k,i)
pickle.dump((occ,names),open('occ.pkl','wb'))
print(len(occ),'voxels')
