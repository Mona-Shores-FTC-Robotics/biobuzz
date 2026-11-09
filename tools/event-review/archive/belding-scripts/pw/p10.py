import sys, glob, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root=sys.argv[2]
def at(s,t):
    v=None
    for ts,x in s:
        if ts>t: break
        v=x
    return v
for team in ('19429','20245'):
    res={'fired':[], 'blocked':[]}; first_shot_spot=[]
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d=R.read(p); te=next(t for t,v in d['/DriverStation/RobotMode'] if v=='teleop'); sh=d['/Launcher/Shots']
        for t,e in d['/Events']:
            if t<te: continue
            if e.startswith('fire:'):
                k='blocked' if ('WAITING' in e or 'SPINNING' in e) else 'fired' if '→ fired' in e else None
                if not k: continue
                s0=at(sh,t) or 0; n=next((tt for tt,v in sh if tt>t and v>s0),None)
                res[k].append(n-t if n and n-t<5 else None)
            if e.startswith('launch spot: start'):
                s0=at(sh,t) or 0; n=next((tt for tt,v in sh if tt>t and v>s0),None)
                if n and n-t<8: first_shot_spot.append(n-t)
    for k,v in res.items():
        ok=[x for x in v if x is not None]
        print(team,k,'FIRE presses',len(v),'first ball within 5 s:',len(ok),'median %.2f s'%st.median(ok) if ok else '')
    print(team,'launch-spot press → first ball (presses that shot): median %.2f s, n=%d'%(st.median(first_shot_spot),len(first_shot_spot)))
