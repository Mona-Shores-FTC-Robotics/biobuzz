import sys,os; sys.path.insert(0,'.')
from lib import *
import collections, numpy as np, pickle
RES=pickle.load(open('resid.pkl','rb'))
for team in ('19429','20245'):
    A,P=RES[team]; L=[]
    for fi,f in enumerate(files(team)):
        d=load(f); a,b=d['_tele']; p=P[fi]
        hits=[t for t,e in ev(d,'push:|jolt:')]
        arr={}
        for t,e in ev(d,'launch spot: (let go|sticks moved)',a,b):
            if 'fired by button' not in e: continue
            g=re.search(r'gate open at ([\d.]+) s for ([\d.]+) s',e); cut='(cut by' in e
            sh=int(re.search(r'(\d+) shots counted',e).group(1)); held=float(re.search(r'held ([\d.]+) s',e).group(1)) if 'held' in e else None
            g0=t-held-float(re.search(r'arriving at \S+ in ([\d.]+)',e).group(1)) + float(g.group(1)) if held is not None else None
            # residual during feed
            m=(p['t']>=g0)&(p['t']<=t)&~p['bad'] if g0 else None
            rmax=np.nanmax(p['res'][m]) if m is not None and m.any() else np.nan
            L.append((float(g.group(2)),cut,sh,e.split(' after')[0][13:],any(g0-0.5<=h<=t for h in hits),rmax))
    print(team,'button volleys',len(L),'cut',sum(1 for x in L if x[1]),' feed<1.0s',sum(1 for x in L if x[0]<1.0),' feed>=2.5s',sum(1 for x in L if x[0]>=2.5))
    print('  feed length vs shots: <1s',[x[2] for x in L if x[0]<1],' 1-2.5',[x[2] for x in L if 1<=x[0]<2.5],' >=2.5',[x[2] for x in L if x[0]>=2.5])
    print('  push/jolt during feed',sum(1 for x in L if x[4]),' residual>20 during feed',sum(1 for x in L if x[5]>20))
    print('  end kind of short (<1s) feeds',collections.Counter(x[3] for x in L if x[0]<1))
