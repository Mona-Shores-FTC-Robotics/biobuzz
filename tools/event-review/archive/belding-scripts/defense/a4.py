import sys,os; sys.path.insert(0,'.')
from lib import *
import numpy as np, pickle
def prep(f):
    d=load(f); a,b=d['_tele']
    t=np.array([x for x,_ in d['/Odometry/PedroInches/X']])
    X=np.array([v for _,v in d['/Odometry/PedroInches/X']]); Y=np.array([v for _,v in d['/Odometry/PedroInches/Y']])
    H=np.radians(np.array([v for _,v in d['/Odometry/PedroInches/HeadingDeg']]))
    W=np.array([[v for _,v in d['/Drive/WheelTicksPerSec/'+k]] for k in ('FrontLeft','FrontRight','BackLeft','BackRight')]).T
    n=min(len(t),len(W)); t,X,Y,H,W=t[:n],X[:n],Y[:n],H[:n],W[:n]
    k=3
    vx=np.full(n,np.nan); vy=np.full(n,np.nan); om=np.full(n,np.nan)
    dt=t[2*k:]-t[:-2*k]
    fx=(X[2*k:]-X[:-2*k])/dt; fy=(Y[2*k:]-Y[:-2*k])/dt
    dh=np.angle(np.exp(1j*(H[2*k:]-H[:-2*k])))/dt
    h=H[k:-k]
    vx[k:-k]=np.cos(h)*fx+np.sin(h)*fy; vy[k:-k]=-np.sin(h)*fx+np.cos(h)*fy; om[k:-k]=dh
    tele=(t>=a)&(t<=b)
    bad=np.zeros(n,bool)
    for tt,e in ev(d,'Pinpoint'):
        bad|=(t>tt-0.7)&(t<tt+0.7)
    lost=[tt for tt,e in ev(d,'Pinpoint lost')]
    if lost:
        ready=[tt for tt,e in ev(d,'Pinpoint: READY') if tt>lost[0]]
        bad|=(t>=lost[0]-1)&(t<=(ready[0]+1 if ready else 1e9))
    return dict(d=d,t=t,v=np.c_[vx,vy,om],W=W,tele=tele,bad=bad,a=a,X=X,Y=Y,H=H)
R={}
for team in ('19429','20245'):
    P=[prep(f) for f in files(team)]
    V=np.vstack([p['v'][p['tele']&~p['bad']] for p in P]); Wm=np.vstack([p['W'][p['tele']&~p['bad']] for p in P])
    ok=~np.isnan(V).any(1)
    A,res,_,_=np.linalg.lstsq(Wm[ok],V[ok],rcond=None)
    pred=Wm[ok]@A; r=V[ok]-pred
    print(team,'fit coeffs (in/s per tick/s)\n',A.round(4))
    print(' R2 per axis',[round(1-r[:,i].var()/V[ok][:,i].var(),3) for i in range(3)])
    tr=np.hypot(r[:,0],r[:,1]); print(' translation residual p50/p90/p99',np.percentile(tr,[50,90,99]).round(1))
    for p in P:
        pr=p['W']@A; rr=p['v']-pr; p['res']=np.hypot(rr[:,0],rr[:,1]); p['pred']=pr
    R[team]=(A,P)
pickle.dump({k:(A,[dict(f=None,t=p['t'],res=p['res'],tele=p['tele'],bad=p['bad'],a=p['a'],v=p['v'],pred=p['pred']) for p in P]) for k,(A,P) in R.items()},open('resid.pkl','wb'))
