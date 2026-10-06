# Option (b): a 14 in roller across the front, hook hinge high on the side plate. Page frame, inches: x right, y up, z back.
import pickle, numpy as np, itertools, collections
occ,names=pickle.load(open('occ.pkl','rb'))
def hits(P, skip=()):
    c=collections.Counter()
    for k in map(tuple,np.floor(P/0.1).astype(np.int32)):
        i=occ.get(k)
        if i is not None and not any(s in names[i] for s in skip): c[names[i].split(' / ')[-1][:30]]+=1
    return c
def cyl_x(x0,x1,y,z,r,n=40):
    t=np.linspace(0,2*np.pi,n,endpoint=False); xs=np.arange(x0,x1,0.05)
    return np.array([(x,y+r*np.cos(a),z+r*np.sin(a)) for x in xs for a in t])
R=0.945  # 48 mm wheels
# 1) at today's roller line (axle 0.95 behind the face), bottom at 2.4 in
print('roller at today\'s line, 14 in:', hits(cyl_x(-7,7,2.4+R,0.95,R), skip=('Gecko','240mm','Intake <1> / 8x14')).most_common(5))
# 2) pushed forward until clear
for zb in np.arange(0.9,-1.3,-0.1):
    h=hits(cyl_x(-7,7,2.4+R,zb,R), skip=('Gecko','240mm'))
    if not h: print('clear with the axle %.2f in %s the face (roller front %.2f in out)'%(abs(zb),'behind' if zb>0 else 'in front of',R-zb)); break

# 3) hook with its hinge high on the side plate, between the roller's end and the plate
import sweep3 as S
def hookb(px, py, pz, xarm=7.25, lane_back=7.48):
    B0=lane_back; parts={}
    parts['block']=S.boxpts([B0,-1.36,0.7],[B0+1.4,1.36,1.35])
    parts['front shaft']=S.boxpts([B0+0.77,-5.0,0.86],[B0+1.09,xarm,1.18])
    parts['curtains']=S.boxpts([B0+0.98,-5.0,1.3],[B0+1.05,xarm-0.3,3.5])
    parts['corner']=S.boxpts([B0+0.4,xarm-0.35,0.7],[B0+1.4,xarm+0.35,2.0])
    a=np.array([-pz,py]); b=np.array([B0+0.9,1.4]); n=int(np.linalg.norm(b-a)/0.06)
    seg=np.array([a+(b-a)*t for t in np.linspace(0,1,n)])
    parts['arm']=np.array([(f,xarm+dx,u+du) for f,u in seg for dx in (-0.16,0.16) for du in (-0.16,0.16)])
    parts['hinge']=S.boxpts([-pz-0.4,xarm-0.3,py-0.4],[-pz+0.4,xarm+0.3,py+0.4])
    pts=np.vstack(list(parts.values())); lab=np.concatenate([[k]*len(v) for k,v in parts.items()])
    return pts,lab
for py in (5.0, 6.0):
    pts,lab=hookb(7.25,py,-1.0)
    print(f'--- hinge {py} in up, 1.0 in in front of the face')
    for ang in range(0,181,10):
        t=np.radians(ang); d=pts-[1.0,0,py]; c,s=np.cos(t),np.sin(t)
        x=d[:,0]*c-d[:,2]*s+1.0; z=d[:,0]*s+d[:,2]*c+py
        P=np.c_[pts[:,1],z,-x]; h=collections.Counter()
        for k,l in zip(map(tuple,np.floor(P/0.1).astype(np.int32)),lab):
            i=occ.get(k)
            if i is not None: h[(l,names[i].split(' / ')[-1][:24])]+=1
        if ang%30==0 or h: print(ang,'fwd %.2f top %.2f'%(x.max(),z.max()), h.most_common(3) if h else 'clear')
