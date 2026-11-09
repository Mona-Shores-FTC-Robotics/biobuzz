import sys, glob
sys.path.insert(0, sys.argv[1]); import read_logs as R
root=sys.argv[2]
def at(s,t):
    v=None
    for ts,x in s:
        if ts>t: break
        v=x
    return v
for p in sorted(glob.glob(f'{root}/19429/matches/*.gz')):
    d=R.read(p); te=next(t for t,v in d['/DriverStation/RobotMode'] if v=='teleop')
    for t,e in d['/Events']:
        if t>=te and e.startswith('fire:') and ('WAITING' in e or 'SPINNING' in e):
            tg=d['/Launcher/TargetTicksPerSecond']; sp=d['/Launcher/LeftTicksPerSecond']
            trace=' '.join('%.1f:%d/%d'%(dt, at(sp,t+dt) or 0, at(tg,t+dt) or 0) for dt in (0,0.3,0.6,1,1.5,2.5))
            nearby=[f'{tt-t:+.2f} {ee[:60]}' for tt,ee in d['/Events'] if t-0.5<tt<t+2.5 and tt!=t and not ee.startswith(('launcher: velocity','launcher: speed table'))]
            print(p[-45:-37], '%.0f'%(t-te), e.split('→')[-1][:45], '|', trace); [print('      ',n) for n in nearby[:5]]
