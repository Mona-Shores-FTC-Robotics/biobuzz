import sys,math,glob,statistics as st,json
sys.path.insert(0,'/home/user/biobuzz/tools/pull-logs')
import read_logs as R
FROZEN={'2026-11-07_13-20-32':[(225.6,233.6)],'2026-11-07_17-32-26':[(41.8,106.2)]}
res={}
for team in ('19429','20245'):
  trips={'full':[],'empty':[]}
  for f in sorted(glob.glob(f'bel/{team}/matches/*.gz')):
    key=f.split('/')[-1][:19]; d=R.read(f); ev=d['/Events']
    bd=[t for t,v in ev if str(v).startswith('OpMode init: Basic Drive')]
    play=[t for t,v in ev if t>bd[0] and str(v).startswith('PLAY:')][0]
    stop=[t for t,v in ev if t>bd[0] and str(v).startswith('OpMode stopped: Basic Drive')]
    t1=min(stop[0] if stop else 1e9,play+120)
    L=[(t,v) for t,v in d['/Lane/Count'] if play<=t<=t1]
    feed=[(t,v) for t,v in d['/FeedGate/Feeding'] if play<=t<=t1]
    opens=[t for (a,x),(t,y) in zip(feed,feed[1:]) if y and not x]
    closes=[t for (a,x),(t,y) in zip(feed,feed[1:]) if x and not y]
    full_at=[t for (a,x),(t,y) in zip(L,L[1:]) if y>=4 and x<4]
    first_in=[t for (a,x),(t,y) in zip(L,L[1:]) if x==0 and y>=1]
    segs=[]
    for t in full_at:
      o=next((g for g in opens if g>t),None)
      if o and o-t<40: segs.append(('full',t,o))
    for c in closes:
      if (R.at(d['/Lane/Count'],c+0.05) or 0)>0: continue
      fi=next((g for g in first_in if g>c),None)
      if fi and fi-c<40: segs.append(('empty',c,fi))
    X,Y=d['/Odometry/PedroInches/X'],d['/Odometry/PedroInches/Y']
    for kind,a,b in segs:
      if any(x<=b and a<=y for x,y in FROZEN.get(key,[])): continue
      vs=[];D=0;t=a
      while t+0.1<=b:
        xa,ya,xb,yb=R.at(X,t),R.at(Y,t),R.at(X,t+0.1),R.at(Y,t+0.1); t+=0.1
        v=math.hypot(xb-xa,yb-ya)/0.1
        if v>80: v=float('nan')
        vs.append(v)
      vv=[v for v in vs if v==v]
      if len(vv)<5: continue
      held=max(min(vv[i:i+5]) for i in range(len(vv)-4)) if len(vv)>=5 else float('nan')
      trips[kind].append(dict(dur=b-a,avg=sum(vv)*0.1/(b-a),moving=st.median([v for v in vv if v>6]) if any(v>6 for v in vv) else 0,held=held,dist=sum(vv)*0.1))
  res[team]={k:dict(n=len(v),avg=round(st.median(x['avg'] for x in v),1),moving=round(st.median(x['moving'] for x in v),1),held=round(st.median(x['held'] for x in v),1),dist=round(st.median(x['dist'] for x in v)),dur=round(st.median(x['dur'] for x in v),1)) for k,v in trips.items()}
print(json.dumps(res,indent=1))
