# Primitive hook (inches, hook frame: x fwd from chassis face, y right of intake centre, z up) swept on the real robot.
import pickle, numpy as np, trimesh, collections, sys, itertools
occ,names=pickle.load(open('occ.pkl','rb'))
def boxpts(lo,hi,step=0.08):
    lo,hi=np.array(lo,float),np.array(hi,float)
    axes=[np.arange(l,h+1e-9,step) if h-l>step else np.array([(l+h)/2]) for l,h in zip(lo,hi)]
    g=np.array(list(itertools.product(*axes)))
    on=np.zeros(len(g),bool)
    for i in range(3):
        if len(axes[i])>1: on|=np.isclose(g[:,i],axes[i][0])|np.isclose(g[:,i],axes[i][-1])
    return g[on] if on.any() else g
def build(GAP=7.4, SIDE=6.1, LEFT=5.0, CURT=3.5, WALL=4.0, D0=0.0, fwd=0.6, hp=1.57):
    B0=GAP; parts={}
    parts['block']=boxpts([B0,-1.36,0.7],[B0+1.4,1.36,1.35])
    parts['front shaft']=boxpts([B0+0.93-0.16,-LEFT,0.86],[B0+0.93+0.16,SIDE+0.2,1.18])
    parts['curtains']=boxpts([B0+0.98,-LEFT,1.3],[B0+1.05,SIDE-0.4,CURT])
    parts['corner block']=boxpts([B0+0.38,SIDE-0.4,0.7],[B0+1.4,SIDE+0.32,WALL+0.1])
    parts['side wall (bottom)']=boxpts([fwd,SIDE-0.16,0.7],[B0+0.5,SIDE+0.16,1.02])
    parts['side wall (upper)']=boxpts([max(fwd,D0),SIDE-0.2,1.0],[B0+0.5,SIDE+0.2,WALL])
    parts['hinge']=boxpts([fwd-0.4,SIDE-0.6,hp-0.5],[fwd+0.5,SIDE+0.6,hp+0.45])
    pts=np.vstack(list(parts.values())); lab=np.concatenate([[k]*len(v) for k,v in parts.items()])
    return pts,lab
def sweep(pts,lab,fwd,hp,angles=range(0,91,5)):
    out=[]
    for a in angles:
        t=np.radians(a); d=pts-[fwd,0,hp]; c,s=np.cos(t),np.sin(t)
        x=d[:,0]*c-d[:,2]*s+fwd; z=d[:,0]*s+d[:,2]*c+hp
        page=np.c_[pts[:,1],z,-x]
        keys=np.floor(page/0.1).astype(np.int32)
        hits=collections.Counter()
        for k,l in zip(map(tuple,keys),lab):
            i=occ.get(k)
            if i is not None: hits[(l,names[i].split(' / ')[-1][:34])]+=1
        out.append((a,round(x.max(),2),int((z<0.05).sum()),sorted({l for (l,_) in hits}), hits.most_common(2)))
    return out
if __name__=='__main__':
    kw=eval('dict('+sys.argv[1]+')')
    pts,lab=build(**kw)
    for r in sweep(pts,lab,kw.get('fwd',0.6),kw.get('hp',1.57)): print(r)
