import sys, glob, os, statistics as st, bisect
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]
fv = []; base = []; per = []
for team in ('19429', '20245'):
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d = R.read(p); bv = [(t, v) for t, v in d['/Robot/BatteryVolts'] if v > 1]
        ts = [t for t, _ in bv]
        m = d['/DriverStation/RobotMode']; a = next(t for t, v in m if v == 'autonomous')
        te = next(t for t, v in m if v == 'teleop'); end = bv[-1][0]
        def near_min(t, w=1.5):
            xs = [v for tt, v in bv if t - w <= tt <= t + w]; return min(xs) if xs else None
        faults = [t for t, v in d['/Events'] if 'Pinpoint: FAULT_BAD_READ' in v and a <= t <= end]
        for t in faults:
            fv.append(near_min(t))
        base += [near_min(t) for t in ts if a <= t <= end]
        auto = sum(1 for t in faults if t < te)
        per.append((team, os.path.basename(p)[11:19], len(faults), auto, [round(t - te) for t in faults if t >= te]))
for r in per: print(r)
fv = [x for x in fv if x]; base = [x for x in base if x]
print('faults', len(fv), 'median min-V within 1.5 s of a fault %.2f' % st.median(fv), '| all match moments %.2f' % st.median(base))
for th in (9, 9.5, 10, 10.5):
    print(f'<{th} V: faults {100*sum(x<th for x in fv)/len(fv):.0f}%  vs all moments {100*sum(x<th for x in base)/len(base):.0f}%')
