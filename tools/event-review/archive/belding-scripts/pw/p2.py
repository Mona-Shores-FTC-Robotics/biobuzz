import sys, glob, os, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]
allv = []; rec = []; spin = []
def at(series, t):
    v = None
    for ts, x in series:
        if ts > t: break
        v = x
    return v
for team in ('19429', '20245'):
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d = R.read(p)
        m = d['/DriverStation/RobotMode']; a = next(t for t, v in m if v == 'autonomous'); end = d['/Robot/BatteryVolts'][-1][0]
        bv = [(t, v) for t, v in d['/Robot/BatteryVolts'] if a <= t <= end and v > 1]
        allv += [(team, v) for _, v in bv]
        tgt = d['/Launcher/TargetTicksPerSecond']; atsp = d['/Launcher/AtSpeed']
        # time with launcher commanded on during match
        on = 0; tot = 0; prev = None
        for (t, v) in tgt:
            if prev and a <= prev[0] <= end:
                dt = t - prev[0]; tot += dt; on += dt if prev[1] > 0 else 0
            prev = (t, v)
        # at-speed fraction while on, split by battery bucket
        for (t, r) in d.get('/Launcher/RecoverMs', []):
            if a <= t <= end: rec.append((team, at(d['/Robot/BatteryVolts'], t), r))
        for (t, r) in d.get('/Launcher/RiseMs', []):
            if a <= t <= end: spin.append((team, at(d['/Robot/BatteryVolts'], t), r))
        print(team, os.path.basename(p)[11:19], f'launcher on {100*on/tot:4.0f}% of match', f'shots {max(v for _,v in d["/Launcher/Shots"]):.0f}')
for team in ('19429', '20245', None):
    vs = [v for tm, v in allv if team is None or tm == team]
    print(team or 'both', 'samples', len(vs), 'median %.2f' % st.median(vs), ' '.join(f'<{th}V {100*sum(v<th for v in vs)/len(vs):.1f}%' for th in (8.5, 9, 10, 11)))
def buckets(rows, name):
    for lo, hi in ((0, 10), (10, 11), (11, 12), (12, 20)):
        xs = [r for _, v, r in rows if v and lo <= v < hi]
        if xs: print(f'{name} battery {lo}-{hi} V: n={len(xs):3d} median {st.median(xs):6.0f} ms  p90 {sorted(xs)[int(.9*len(xs))]:6.0f}')
buckets(rec, 'shot recover'); buckets(spin, 'spin-up rise')
