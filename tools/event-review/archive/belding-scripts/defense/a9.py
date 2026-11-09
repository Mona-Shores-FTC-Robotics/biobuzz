import sys,os; sys.path.insert(0,'.')
from lib import *
import numpy as np, pickle, random
RES=pickle.load(open('resid.pkl','rb'))
random.seed(1)
for team in ('19429','20245'):
    A,P=RES[team]; F=[]; base=[]; ndur=0; nf=0; strl=[]
    for fi,f in enumerate(files(team)):
        d=load(f); a,b=d['_tele']; p=P[fi]
        sp=S(d['/Drive/OdometrySpeedInPerSec']); fd=S(d['/FeedGate/Feeding']); cf=S(d['/Drive/CommandForward'])
        hits=[t for t,e in ev(d,'push:|jolt:')]
        faults=[t for t,e in ev(d,'Pinpoint: FAULT')]
        lo,hi=d['_auto'][0] if d['_auto'] else a, b
        ndur+=hi-lo; nf+=len([t for t in faults if lo<=t<=hi])
        strl.append(len([t for t in faults if lo<=t<=hi]))
        def feat(t):
            c=cf.at(t)
            return (sp.at(t-0.5) or 0, bool(fd.at(t)), any(abs(h-t)<=1.5 for h in hits), c!=c)
        for t in faults:
            if lo<=t<=hi: F.append(feat(t))
        for _ in range(400):
            t=random.uniform(lo,hi); base.append(feat(t))
    F=np.array(F,float); B=np.array(base,float)
    print(team,'faults AUTO+TeleOp',len(F),'per match',strl,' rate %.3f/s'%(nf/ndur))
    print('  speed before fault median %.1f vs random %.1f; >20in/s %.2f vs %.2f'%(np.median(F[:,0]),np.median(B[:,0]),(F[:,0]>20).mean(),(B[:,0]>20).mean()))
    print('  feed gate open %.2f vs %.2f; push/jolt within 1.5s %.2f vs %.2f; under path %.2f vs %.2f'%(F[:,1].mean(),B[:,1].mean(),F[:,2].mean(),B[:,2].mean(),F[:,3].mean(),B[:,3].mean()))
    # jolts that follow a fault
    J=0;JF=0
    for f in files(team):
        d=load(f)
        faults=[t for t,e in ev(d,'Pinpoint: FAULT')]
        for t,e in ev(d,'jolt:'):
            J+=1; JF+= any(0<=t-x<=2.0 for x in faults)
    print('  jolts (whole log)',J,'within 2 s after a fault',JF)
