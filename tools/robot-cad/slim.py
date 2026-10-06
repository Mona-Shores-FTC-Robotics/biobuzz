import trimesh, numpy as np, base64, json, re
s=trimesh.load('robot.glb'); IN=0.0254
X0, FLOOR, FRONT = -2.35, -5.97, 8.178
par=s.graph.transforms.parents
def chain(n):
    c=[n]
    while c[-1] in par: c.append(par[c[-1]])
    return c[::-1]
tot=0; keep=[]
for n in s.graph.nodes_geometry:
    T,g=s.graph[n]; m=s.geometry[g]
    path=' / '.join(chain(n)[2:])
    if re.search(r'text|e-clip|shim|Screw|screw|Bearing|pins|Nut|nut|Washer|washer|Cavity',path): continue
    v=trimesh.transform_points(m.vertices,T)/IN
    ext=v.max(0)-v.min(0)
    if np.linalg.norm(ext)<0.35: continue
    col=None
    try:
        vis=m.visual
        if hasattr(vis,'material') and vis.material is not None:
            bc=getattr(vis.material,'baseColorFactor',None)
            if bc is not None: col=np.array(bc[:3],float)/ (255 if np.max(bc)>1 else 1)
        if col is None and vis.kind=='vertex': col=vis.vertex_colors[:,:3].mean(0)/255
    except Exception: pass
    keep.append((path,v,m.faces,col)); tot+=len(m.faces)
print('kept',len(keep),'faces',tot)
cols=[k[3] for k in keep if k[3] is not None]; print('with colour',len(cols))
import pickle; pickle.dump(keep,open('keep.pkl','wb'))
