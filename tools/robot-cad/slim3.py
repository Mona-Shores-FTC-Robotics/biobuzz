import pickle, numpy as np, re, fast_simplification, base64, json, collections, trimesh, sys
keep=pickle.load(open('keep.pkl','rb'))
X0, FLOOR, FRONT = -2.35, -5.97, 8.178
RATIO=float(sys.argv[1]) if len(sys.argv)>1 else 0.12
def cat(p):
    if 'Nectar' in p: return 'nectar'
    if 'Gecko' in p: return 'gecko'
    if re.search('belt|5000|5103|5203|motor|body|Gearbox',p): return 'motor'
    if 'CAGE' in p: return 'cage'
    if re.search('Shaft|shaft|REX',p): return 'steel'
    return 'alu'
out=collections.defaultdict(lambda: ([],[])); wheels=collections.defaultdict(list); tot=0
for path,v,f,col in keep:
    if re.search('3606|roller',path):
        w=re.search(r'Wheel Assembly <\d>',path); wheels[w.group(0) if w else path].append(v); continue
    m=trimesh.Trimesh(v,f,process=True); m.merge_vertices(digits_vertex=4)
    v,f=m.vertices,m.faces
    nf=len(f); target=max(40,int(nf*RATIO)) if nf>300 else nf
    if target<nf:
        try: v,f=fast_simplification.simplify(v.astype(np.float32),f.astype(np.int32),target_reduction=1-target/nf)
        except Exception as e: pass
    P=np.c_[X0-v[:,0], v[:,1]-FLOOR, FRONT-v[:,2]]
    V,F=out[cat(path)]; base=sum(len(a) for a in V); V.append(P); F.append(np.asarray(f)+base); tot+=len(f)
wl=[]
for k,vs in wheels.items():
    v=np.vstack(vs); P=np.c_[X0-v[:,0], v[:,1]-FLOOR, FRONT-v[:,2]]
    wl.append([*np.round(P.min(0),3).tolist(),*np.round(P.max(0),3).tolist()])
print('faces',tot,'wheels',wl)
pack={'wheels':wl}
for k,(V,F) in out.items():
    V=np.vstack(V); F=np.vstack(F)
    pack[k]={'v':base64.b64encode(np.round(V*100).astype(np.int16).tobytes()).decode(),'f':base64.b64encode(F.astype(np.uint32).tobytes()).decode()}
json.dump(pack,open('robot_pack.json','w'))
open('pack.js','w').write('const PACK='+json.dumps(pack)+';')
import os; print(os.path.getsize('robot_pack.json')/1e6,'MB')
