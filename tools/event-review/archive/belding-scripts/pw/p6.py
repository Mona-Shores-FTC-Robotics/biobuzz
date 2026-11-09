import sys, glob, os, re
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]
for team in ('19429','20245'):
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d=R.read(p); m=d['/DriverStation/RobotMode']; te=next(t for t,v in m if v=='teleop')
        ev=[(t,e) for t,e in d['/Events'] if t>=te]
        ups=[t for t,e in ev if e.startswith('launcher: launch speed')]
        idles=[t for t,e in ev if e.startswith('launcher: idle')]
        # what happened just before each idle / spin-up
        def why(t):
            prev=[e for tt,e in d['/Events'] if t-0.3<=tt<=t and not e.startswith('launcher')]
            return (prev[-1][:40] if prev else '?')
        # time from each spin-up to AtSpeed
        ats=d['/Launcher/AtSpeed']; waits=[]
        for t in ups:
            nxt=next((tt for tt,v in ats if tt>t and v),None)
            if nxt: waits.append(nxt-t)
        fires=sum(1 for t,e in ev if e.startswith('fire'))
        print(team, os.path.basename(p)[11:19], f'spin-ups {len(ups):2d} idles {len(idles):2d} wait-to-speed total {sum(waits):4.1f}s', 'fire presses', fires)
        if team=='20245' and p.endswith('17-40-34_L-NoCross_11-05_8_03PM.wpilog.gz'):
            for t,e in ev:
                if e.startswith(('launcher: launch speed','launcher: idle','fire','launch spot: let go','launch spot: start')): print('   %6.1f'%(t-te), e[:110])
