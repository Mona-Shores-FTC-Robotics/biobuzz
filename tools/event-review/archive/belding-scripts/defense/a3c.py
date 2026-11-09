import sys; sys.path.insert(0,'.')
from lib import *
import pickle
res={}
for f in files():
    d=load(f); a,b=d['_tele']
    T=[t for t,v in d['/Drive/OdometrySpeedInPerSec']]; sp=[v for t,v in d['/Drive/OdometrySpeedInPerSec']]
    cf=d['/Drive/CommandForward']; cs=d['/Drive/CommandStrafe']; ct=d['/Drive/CommandTurn']
    W=[d['/Drive/WheelTicksPerSec/'+k] for k in ('FrontLeft','FrontRight','BackLeft','BackRight')]
    pp=[t for t,e in ev(d,'Pinpoint: FAULT')]
    lastcmd=-9; eps=[]; cur=None
    for i,t in enumerate(T):
        if not (a<=t<=b): continue
        f_,s_,tu=cf[i][1],cs[i][1],ct[i][1]
        if f_!=f_: lastcmd=t; cur=None; continue
        cmd=math.hypot(f_,s_)+abs(tu)
        if cmd>0.1: lastcmd=t
        wh=sum(abs(w[i][1]) for w in W)/4
        if cmd<0.1 and t-lastcmd>0.4 and sp[i]>5:
            if cur is None: cur=[t,t,0,0,9e9]
            cur[1]=t; cur[2]=max(cur[2],sp[i]); cur[3]=max(cur[3],wh)
        elif cur:
            eps.append(cur); cur=None
    good=[(round(x-a,1),round(y-x,2),round(m),round(w),any(abs(p-x)<1 for p in pp)) for x,y,m,w,_ in eps if y-x>=0.1]
    res[f]=[(x,y,m) for x,y,m,w,_ in eps if y-x>=0.1]
    print(f[:22],good)
pickle.dump(res,open('shoves.pkl','wb'))
