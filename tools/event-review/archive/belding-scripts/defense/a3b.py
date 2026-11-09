import sys; sys.path.insert(0,'.')
from lib import *
for f in files()[:3]+files('20245')[:3]:
    d=load(f); a,b=d['_tele']
    T=[t for t,v in d['/Drive/OdometrySpeedInPerSec']]; sp=[v for t,v in d['/Drive/OdometrySpeedInPerSec']]
    cf=d['/Drive/CommandForward']; cs=d['/Drive/CommandStrafe']; ct=d['/Drive/CommandTurn']
    print(len(T),len(cf),len(cs),len(ct), T[100]==cf[100][0])
    lastcmd=-9; n=0; nn=0; tot=0; mx=[]
    for i,t in enumerate(T):
        if not (a<=t<=b): continue
        tot+=1
        f_,s_,tu=cf[i][1],cs[i][1],ct[i][1]
        if f_!=f_: nn+=1; continue
        cmd=math.hypot(f_,s_)+abs(tu)
        if cmd>0.1: lastcmd=t
        if cmd<0.1 and t-lastcmd>0.5:
            n+=1; mx.append(sp[i])
    mx.sort()
    print(f[:22],'tele samples',tot,'path',nn,'idle',n,'idle speed p50/p99/max',mx[len(mx)//2] if mx else None, mx[int(len(mx)*.99)] if mx else None, mx[-1] if mx else None)
