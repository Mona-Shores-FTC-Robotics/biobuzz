import sys,os; sys.path.insert(0,'.')
from lib import *
import collections, numpy as np, pickle
RES=pickle.load(open('resid.pkl','rb'))
for team in ('19429','20245'):
    A,P=RES[team]; rows=[]
    for fi,f in enumerate(files(team)):
        d=load(f); a,b=d['_tele']; p=P[fi]
        shots=S(d['/Launcher/Shots']); sp=S(d['/Drive/OdometrySpeedInPerSec']); X=S(d['/Odometry/PedroInches/X']); Y=S(d['/Odometry/PedroInches/Y'])
        hits=[(t,e[:4]) for t,e in ev(d,'push:|jolt:')]
        lost=ev(d,'Pinpoint lost'); 
        for t,e in ev(d,'Lane: FULL',a,b):
            s0=shots.at(t)
            nxt=[tt for tt,v in shots.win(t,b) if v>s0]
            if not nxt: rows.append(dict(f=os.path.basename(f)[:22],t=t-a,wait=None)); continue
            t1=nxt[0]
            sq=[x for x in ev(d,'launch spot: (start|refused)',t,t1)]
            fires=ev(d,'fire:',t,t1)
            spd=[v for _,v in sp.win(t,t1)]
            dist=0; xs=X.win(t,t1); ys=Y.win(t,t1)
            for j in range(1,len(xs)): dist+=math.hypot(xs[j][1]-xs[j-1][1],ys[j][1]-ys[j-1][1])
            m=(p['t']>=t)&(p['t']<=t1)&~p['bad']
            rr=p['res'][m]; rr=rr[~np.isnan(rr)]
            resid_s=(rr>20).sum()/max(1,len(rr))*(t1-t)
            rows.append(dict(f=os.path.basename(f)[:22],t=round(t-a,1),wait=t1-t,hits=sum(1 for h,_ in hits if t<=h<=t1),
               sq=len([x for x in sq if 'start' in x[1]]),ref=len([x for x in sq if 'refused' in x[1]]),fires=len(fires),
               moving=np.mean(np.array(spd)>5) if spd else None,dist=dist,lost=bool(lost and lost[0][0]<t1),resid=resid_s,x=X.at(t)))
    W=[r for r in rows if r['wait'] is not None]
    print('\n',team,'FULL events',len(rows),'with a later shot',len(W),'median wait %.1f'%np.median([r['wait'] for r in W]),'mean %.1f'%np.mean([r['wait'] for r in W]))
    print('  waits',sorted(round(r['wait'],1) for r in W))
    long=[r for r in W if r['wait']>=8]
    print('  long(>=8s)',len(long),'with push/jolt',sum(1 for r in long if r['hits']),' with pinpoint lost',sum(1 for r in long if r['lost']),
          ' median moving frac %.2f'%np.median([r['moving'] for r in long]),' median dist %.0f in'%np.median([r['dist'] for r in long]),
          ' with >=1 refused press',sum(1 for r in long if r['ref']),' median resid>20 s %.2f'%np.median([r['resid'] for r in long]))
    for r in long: print('   ',r['f'][11:],r['t'],'wait %.1f'%r['wait'],'hits',r['hits'],'sq',r['sq'],'ref',r['ref'],'fire',r['fires'],'mov %.2f'%r['moving'],'dist %.0f'%r['dist'],'lost',r['lost'],'x %.0f'%r['x'],'res %.1f'%r['resid'])
