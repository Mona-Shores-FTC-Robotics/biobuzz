import sys,os; sys.path.insert(0,'.')
from lib import *
import numpy as np, pickle, collections
RES=pickle.load(open('resid.pkl','rb'))
for team in ('19429','20245'):
    A,P=RES[team]
    rows=[]; endc=collections.Counter(); presses=0; starts=0
    for fi,f in enumerate(files(team)):
        d=load(f); a,b=d['_tele']; p=P[fi]
        cf=S(d['/Drive/CommandForward']); cs=S(d['/Drive/CommandStrafe']); ct=S(d['/Drive/CommandTurn']); sp=S(d['/Drive/OdometrySpeedInPerSec'])
        H=S(d['/Odometry/PedroInches/HeadingDeg'])
        bv=d['/DriverStation/Joystick0/ButtonValues']; prev=0
        for t,v in bv:
            if a<=t<=b and (v>>2)&1 and not (prev>>2)&1: presses+=1
            prev=v
        hits=[t for t,e in ev(d,'push:|jolt:')]
        faults=[t for t,e in ev(d,'Pinpoint: FAULT')]
        E=ev(d,'launch spot: (start|sticks moved|let go|timed out|pose lost|refused)',a,b)
        for i,(t,e) in enumerate(E):
            if e.startswith('launch spot: start'):
                starts+=1
                # end
                nxt=[(t2,e2) for t2,e2 in E[i+1:i+2]]
                if not nxt: continue
                t2,e2=nxt[0]
                kind=re.match(r'launch spot: (sticks moved early|sticks moved after|let go early|let go after|timed out|pose lost)',e2)
                kind=kind.group(1) if kind else e2[:30]
                endc[kind]+=1
                # pre-press command: last non-nan sample within 0.3 s before
                pre=[(tt,math.hypot(cf.at(tt),cs.at(tt)),abs(ct.at(tt))) for tt,_ in cf.win(t-0.3,t-0.001) if cf.at(tt)==cf.at(tt)]
                prem=max((x+0*y for _,x,y in pre),default=float('nan')); pret=max((y for _,x,y in pre),default=float('nan'))
                post=[(math.hypot(cf.at(tt),cs.at(tt)),abs(ct.at(tt))) for tt,_ in cf.win(t2+0.001,t2+0.3) if cf.at(tt)==cf.at(tt)]
                postm=max((x for x,y in post),default=float('nan')); postt=max((y for x,y in post),default=float('nan'))
                near=any(t-1<=h<=t2+1 for h in hits)
                nf=any(t-1<=h<=t2+1 for h in faults)
                m=(p['t']>=t-0.3)&(p['t']<=t2+0.3)&~p['bad']
                rmax=np.nanmax(p['res'][m]) if m.any() else float('nan')
                hs=[v for _,v in H.win(t-0.2,t2+0.2)]
                hj=max(abs(((hs[j]-hs[j-1]+180)%360)-180) for j in range(1,len(hs))) if len(hs)>1 else 0
                rows.append(dict(kind=kind,dur=t2-t,pre=prem,pret=pret,post=postm,postt=postt,near=near,fault=nf,rmax=rmax,spd=sp.at(t),hj=hj))
    print('\n',team,'Square rising edges in TeleOp',presses,'launch-spot starts',starts, dict(endc))
    early=[r for r in rows if r['kind']=='sticks moved early']
    q=[r for r in early if r['dur']<=0.1]
    print(' sticks-moved-early',len(early),' within 0.1 s',len(q))
    def frac(L,fn): return '%d/%d'%(sum(1 for r in L if fn(r)),len(L))
    for name,L in (('early<=0.1s',q),('early>0.1s',[r for r in early if r['dur']>0.1]),('others',[r for r in rows if r['kind']!='sticks moved early'])):
        print('  ',name,'pre-press translate cmd>0.2:',frac(L,lambda r:r['pre']>0.2),' pre turn>0.2:',frac(L,lambda r:r['pret']>0.2),
              ' pre either>0.2:',frac(L,lambda r:r['pre']>0.2 or r['pret']>0.2),
              ' post cmd>0.2:',frac(L,lambda r:r['post']>0.2 or r['postt']>0.2),
              ' push/jolt ±1s:',frac(L,lambda r:r['near']),' pinpoint fault ±1s:',frac(L,lambda r:r['fault']),
              ' residual>20in/s:',frac(L,lambda r:r['rmax']>20),' speed@press median %.1f'%np.median([r['spd'] for r in L]) if L else '',
              ' heading step>5deg:',frac(L,lambda r:r['hj']>5))
    pickle.dump(rows,open('sq_%s.pkl'%team,'wb'))
