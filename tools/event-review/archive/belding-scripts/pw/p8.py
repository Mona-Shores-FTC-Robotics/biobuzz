import sys, glob, re, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root=sys.argv[2]
for team in ('19429','20245'):
    waits=[]; chains=0; pushes=[]
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d=R.read(p); m=d['/DriverStation/RobotMode']; te=next(t for t,v in m if v=='teleop')
        ev=[(t,e) for t,e in d['/Events'] if t>=te]
        fp=d['/Feeder/Power']
        for t,e in ev:
            if e.startswith('fire:') and ('WAITING' in e or 'SPINNING UP' in e):
                nxt=next((tt for tt,v in fp if tt>t and abs(v)>0.5),None)
                waits.append((nxt-t) if nxt and nxt-t<10 else None)
            if e.startswith('push:'):
                pushes.append(float(re.search(r'push: ([\d.]+) s',e).group(1)))
        # sticks-moved-early followed by another press within 1.5 s
        se=[t for t,e in ev if e.startswith('launch spot: sticks moved early')]
        st_=[t for t,e in ev if e.startswith('launch spot: start')]
        chains+=sum(1 for t in se if any(t<s<t+1.5 for s in st_))
    w=[x for x in waits if x is not None]
    print(team,'blocked FIRE presses',len(waits),'fed later in',['%.2f'%x for x in sorted(w)],'never fed',sum(x is None for x in waits))
    print(team,'sticks-moved-early followed by a new press within 1.5 s:',chains,'| pushes',len(pushes),'total %.1fs'%sum(pushes))
