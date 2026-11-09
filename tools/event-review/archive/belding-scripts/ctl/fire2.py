import sys, glob, bisect, json, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]; team = sys.argv[3]
NAMES = {'19429': ['Q4','Q13','Q19','Q26','Q32','Playoff 1','Upper final','Lower final','Final'],'20245': ['Q1','Q7','Q14','Q27','Q31','Playoff 1','Upper final','Final']}
BTN = {0:'Cross',1:'Circle',2:'Square',3:'Triangle',9:'L1',10:'R1',11:'Up',12:'Down',13:'Left',14:'Right'}
class S:
    def __init__(s, xs): s.t=[t for t,_ in xs]; s.v=[v for _,v in xs]
    def at(s,t):
        i=bisect.bisect_right(s.t,t)-1; return s.v[i] if i>=0 else None
    def first(s,a,b,pred):
        i=bisect.bisect_left(s.t,a)
        while i<len(s.t) and s.t[i]<=b:
            if pred(s.v[i]): return s.t[i]
            i+=1
        return None
out=[]
for i,p in enumerate(sorted(glob.glob(f'{root}/{team}/matches/*.gz'))):
    d=R.read(p); te=next(t for t,v in d['/DriverStation/RobotMode'] if v=='teleop'); end=d['/Robot/BatteryVolts'][-1][0]
    g2=d['/DriverStation/Joystick1/ButtonValues']; g1=S(d['/DriverStation/Joystick0/ButtonValues'])
    gate=S(d['/FeedGate/Feeding']); tgt=S(d['/Launcher/TargetTicksPerSecond']); spd=S(d['/Launcher/LeftTicksPerSecond']); ats=S(d['/Launcher/AtSpeed']); lane=S(d['/Lane/Count'])
    sh=d['/Launcher/Shots']
    # all operator edges
    prev=0; downs={}
    for t,v in g2:
        v=int(v)
        for b,n in BTN.items():
            was=(prev>>b)&1; now=(v>>b)&1
            if now and not was: downs[b]=t
            if was and not now and b in downs:
                t0=downs.pop(b)
                if te<=t0<=end: out.append({'match':NAMES[team][i],'btn':n,'down':round(t0-te,2),'held':round(t-t0,2)})
        prev=v
    for r in out:
        if r['match']!=NAMES[team][i] or r['btn']!='Circle' or 'gate_open_after' in r: continue
        t0=r['down']+te; t1=t0+r['held']
        r['square_held']=bool((int(g1.at(t0) or 0)>>2)&1)
        r['launcher_target']=round(tgt.at(t0) or 0); r['launcher_speed']=round(spd.at(t0) or 0); r['at_speed']=bool(ats.at(t0))
        r['lane']=lane.at(t0)
        ga=gate.first(t0,t1+0.05,lambda v:bool(v)); r['gate_open_after']=None if ga is None else round(ga-t0,2)
        r['at_speed_after']=None if r['at_speed'] else (lambda x: None if x is None else round(x-t0,2))(ats.first(t0,t0+5,lambda v:bool(v)))
        s0=R.at(sh,t0) or 0; balls=[tt for tt,v in sh if t0<tt<=t1+1.0 and v>s0]
        r['balls_while_held']=len(set(balls)); r['first_ball']=None if not balls else round(balls[0]-t0,2)
json.dump(out,open(sys.argv[4],'w'),indent=1)
c=[r for r in out if r['btn']=='Circle']
print('operator presses by button:',{b:sum(1 for r in out if r['btn']==b) for b in BTN.values() if any(r['btn']==b for r in out)})
print('Circle holds (s):',sorted(r['held'] for r in c))
print('match      t      held  sq  lane tgt/spd  atSpd  atSpdAfter gateAfter firstBall balls')
for r in c: print(f"{r['match'][:10]:10s} {r['down']:6.1f} {r['held']:5.2f} {'Y' if r['square_held'] else '.':2s} {r['lane']!s:4s} {r['launcher_target']:4d}/{r['launcher_speed']:<4d} {str(r['at_speed'])[0]:5s} {r['at_speed_after']!s:9s} {r['gate_open_after']!s:8s} {r['first_ball']!s:8s} {r['balls_while_held']}")
