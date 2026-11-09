import sys; sys.path.insert(0,'.')
from lib import *
import pickle
out={}
for f in files():
    d=load(f); a,b=d['_tele']
    T=[t for t,v in d['/Drive/OdometrySpeedInPerSec']]
    sp=[v for t,v in d['/Drive/OdometrySpeedInPerSec']]
    cf=S(d['/Drive/CommandForward']); cs=S(d['/Drive/CommandStrafe']); ct=S(d['/Drive/CommandTurn'])
    W=[S(d['/Drive/WheelTicksPerSec/'+k]) for k in ('FrontLeft','FrontRight','BackLeft','BackRight')]
    H=S(d['/Odometry/PedroInches/HeadingDeg'])
    lastcmd=-9; eps=[]; cur=None
    for i,t in enumerate(T):
        if not (a<=t<=b): continue
        f_,s_,tu=cf.v[i],cs.v[i],ct.v[i]  # same timestamps
        nan = f_!=f_
        cmd = 9 if nan else math.hypot(f_,s_)+abs(tu)
        if cmd>0.1: lastcmd=t
        wheels=sum(abs(w.v[min(i,len(w.v)-1)]) for w in W)/4
        shove = (not nan) and cmd<0.1 and t-lastcmd>0.5 and sp[i]>6 and wheels<150
        if shove:
            if cur is None: cur=[t,t,sp[i]]
            cur[1]=t; cur[2]=max(cur[2],sp[i])
        elif cur:
            if cur[1]-cur[0]>=0.2: eps.append(tuple(cur))
            cur=None
    out[f]=eps
    print(f[:22],len(eps),[(round(x-a,1),round(y-x,2),round(m)) for x,y,m in eps])
pickle.dump(out,open('shoves.pkl','wb'))
