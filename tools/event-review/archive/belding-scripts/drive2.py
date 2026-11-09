import sys,math,glob,statistics as st,json,collections
sys.path.insert(0,'/home/user/biobuzz/tools/pull-logs')
import read_logs as R
FROZEN={'2026-11-07_13-20-32':[(225.6,233.6)],'2026-11-07_17-32-26':[(41.8,106.2)]}
S=[]
for team in ('19429','20245'):
  for f in sorted(glob.glob(f'bel/{team}/matches/*.gz')):
    key=f.split('/')[-1][:19]; d=R.read(f); ev=d['/Events']
    bd=[t for t,v in ev if str(v).startswith('OpMode init: Basic Drive')]
    play=[t for t,v in ev if t>bd[0] and str(v).startswith('PLAY:')][0]
    stop=[t for t,v in ev if t>bd[0] and str(v).startswith('OpMode stopped: Basic Drive')]
    t1=min(stop[0] if stop else 1e9,play+120)
    X,Y,H=d['/Odometry/PedroInches/X'],d['/Odometry/PedroInches/Y'],d['/Odometry/PedroInches/HeadingDeg']
    F,Sx,T=d['/Drive/CommandForward'],d['/Drive/CommandStrafe'],d['/Drive/CommandTurn']
    FC=d['/Drive/CommandFieldCentric'];L=d['/Lane/Count'];V=d['/Robot/BatteryVolts'];IA=d['/Intake/CurrentAmps']
    t=play
    while t+0.1<t1:
      a,b=t,t+0.1; t=b
      if any(x<=b and a<=y for x,y in FROZEN.get(key,[])): continue
      xa,ya,xb,yb=R.at(X,a),R.at(Y,a),R.at(X,b),R.at(Y,b)
      if None in (xa,ya,xb,yb): continue
      vx,vy=(xb-xa)/0.1,(yb-ya)/0.1; v=math.hypot(vx,vy)
      if v>110: continue
      h=math.radians(R.at(H,b) or 0)
      fwd=vx*math.cos(h)+vy*math.sin(h); left=-vx*math.sin(h)+vy*math.cos(h)
      f_,s_,tr=R.at(F,b),R.at(Sx,b),R.at(T,b)
      if f_ is None or f_!=f_: continue
      S.append(dict(team=team,v=v,fwd=fwd,left=left,cmd=math.hypot(f_,s_),turn=abs(tr or 0),lane=int(R.at(L,b) or 0),volt=R.at(V,b),intake=(R.at(IA,b) or 0)>2,fc=R.at(FC,b)))
json.dump(S,open('drive_samples.json','w'))
print(len(S))
