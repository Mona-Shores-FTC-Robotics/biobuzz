import sys; sys.path.insert(0,'.')
from lib import *
import statistics as st
for team in ('19429','20245'):
    P=[]
    for f in files(team):
        d=load(f); a,b=d['_tele']
        X=S(d['/Odometry/PedroInches/X']); sp=S(d['/Drive/OdometrySpeedInPerSec'])
        fl=S(d['/Drive/WheelTicksPerSec/FrontLeft']); fr=S(d['/Drive/WheelTicksPerSec/FrontRight'])
        pp=ev(d,'Pinpoint')
        for t,e in ev(d,'push:',a,b):
            m=re.match(r'push: ([\d.]+) s at command ([\d.]+), at \(([-\d.]+), ([-\d.]+), ([-\d.]+)°\), ([-\d.]+) in off a wall, stick (\w+)',e)
            dur,cmd,x,y,h,wall,stick=m.groups(); dur=float(dur); wall=float(wall)
            w=[abs(v) for tt,v in fl.win(t-dur,t)]+[abs(v) for tt,v in fr.win(t-dur,t)]
            xs=[v for tt,v in X.win(t-dur,t)]
            frozen = len(set(round(v,3) for v in xs))<=1
            pin=[e2[9:] for t2,e2 in pp if t-dur-0.6<=t2<=t+0.1]
            P.append((f[6:22],round(t-a,1),dur,wall,stick,round(st.mean(w)) if w else None,frozen,pin,x,y))
    print(team,len(P),'total',round(sum(p[2] for p in P),1))
    for p in P: print('  ',p)
