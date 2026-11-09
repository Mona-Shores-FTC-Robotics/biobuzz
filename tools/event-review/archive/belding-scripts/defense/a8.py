import sys,os; sys.path.insert(0,'.')
from lib import *
import numpy as np
for team in ('19429','20245'):
    R=[]
    for f in files(team):
        d=load(f); a,b=d['_tele']
        shots=S(d['/Launcher/Shots']); X=S(d['/Odometry/PedroInches/X']); Y=S(d['/Odometry/PedroInches/Y'])
        ed=[]
        for key,bit in (('/DriverStation/Joystick0/ButtonValues',2),('/DriverStation/Joystick1/ButtonValues',1)):
            prev=0
            for t,v in d[key]:
                if (v>>bit)&1 and not (prev>>bit)&1: ed.append(t)
                prev=v
        ed.sort()
        lost=ev(d,'Pinpoint lost')
        for t,e in ev(d,'Lane: FULL',a,b):
            s0=shots.at(t); nxt=[tt for tt,v in shots.win(t,b) if v>s0]
            pr=[x for x in ed if x>=t]
            if not nxt: continue
            t1=nxt[0]
            xs=X.win(t,t1); ys=Y.win(t,t1); dist=sum(math.hypot(xs[j][1]-xs[j-1][1],ys[j][1]-ys[j-1][1]) for j in range(1,len(xs)))
            R.append((t1-t,(pr[0]-t) if pr else np.nan,dist,bool(lost)))
    R=np.array(R,float)
    print(team,'n',len(R),'median FULL->shot %.1f'%np.median(R[:,0]),' FULL->first Square/opCircle press %.1f'%np.nanmedian(R[:,1]),
          ' median dist %.0f in'%np.median(R[:,2]),' median avg speed %.1f in/s'%np.median(R[:,2]/R[:,0]),' excl lost-pinpoint match: n %d median press %.1f'%((R[:,3]==0).sum(),np.nanmedian(R[R[:,3]==0][:,1])))
