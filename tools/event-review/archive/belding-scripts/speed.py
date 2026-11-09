import sys,math,glob,statistics as st,json
sys.path.insert(0,'/home/user/biobuzz/tools/pull-logs')
import read_logs as R
FROZEN={'2026-11-07_13-20-32':[(225.6,233.6)],'2026-11-07_17-32-26':[(41.8,106.2)]}
out={}
for team in ('19429','20245'):
  sp_all=[];dist=[];moving=[];tele_s=[]
  for f in sorted(glob.glob(f'bel/{team}/matches/*.gz')):
    key=f.split('/')[-1][:19]
    d=R.read(f); ev=d['/Events']
    bd=[t for t,v in ev if str(v).startswith('OpMode init: Basic Drive')]
    if not bd: continue
    play=[t for t,v in ev if t>bd[0] and str(v).startswith('PLAY:')]
    stop=[t for t,v in ev if t>bd[0] and str(v).startswith('OpMode stopped: Basic Drive')]
    t0=play[0]; t1=min(stop[0] if stop else 1e9, t0+120)
    X=d['/Odometry/PedroInches/X'];Y=d['/Odometry/PedroInches/Y']
    pts=[(t,x,R.at(Y,t)) for t,x in X if t0<=t<=t1]
    fr=FROZEN.get(key,[])
    # resample every 0.1 s
    sp=[];D=0;mv=0;n=0
    i=0;last=None
    grid=[t0+k*0.1 for k in range(int((t1-t0)/0.1))]
    pos=[(g,R.at(X,g),R.at(Y,g)) for g in grid]
    for (ta,xa,ya),(tb,xb,yb) in zip(pos,pos[1:]):
      if any(a<=tb and ta<=b for a,b in fr) or None in (xa,xb,ya,yb): continue
      v=math.hypot(xb-xa,yb-ya)/(tb-ta)
      if v>110: continue
      sp.append(v); D+=v*(tb-ta); n+=1; mv+= v>6
    sp_all+=sp; dist.append(D/12); moving.append(mv/n if n else 0); tele_s.append(t1-t0)
  mv_sp=[v for v in sp_all if v>6]
  s=sorted(sp_all)
  out[team]=dict(top_in_s=round(s[int(.99*len(s))],1),moving_median_in_s=round(st.median(mv_sp),1),
    feet_per_match=round(st.median(dist)),pct_moving=round(100*st.median(moving)),matches=len(dist),tele_s=[round(x) for x in tele_s])
print(json.dumps(out,indent=1))
