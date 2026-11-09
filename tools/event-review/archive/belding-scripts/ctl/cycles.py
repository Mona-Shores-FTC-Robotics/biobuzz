import sys, glob, re, json, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]
NAMES = {'19429': ['Q4','Q13','Q19','Q26','Q32','Playoff 1','Upper final','Lower final','Final'],
         '20245': ['Q1','Q7','Q14','Q27','Q31','Playoff 1','Upper final','Final']}
def downs(series, bit):
    out = []; prev = 0
    for t, v in series:
        b = (int(v) >> bit) & 1
        if b and not prev: out.append(t)
        prev = b
    return out
allv = []; per_match = []
for team in ('19429', '20245'):
    for i, p in enumerate(sorted(glob.glob(f'{root}/{team}/matches/*.gz'))):
        d = R.read(p); m = d['/DriverStation/RobotMode']
        te = next(t for t, v in m if v == 'teleop'); end = d['/Robot/BatteryVolts'][-1][0]
        sh = d['/Launcher/Shots']; balls = []; last = R.at(sh, te) or 0
        for t, v in sh:
            if te < t <= end and v > last: balls.append(t); last = v
        vol = []
        for t in balls:
            if vol and t - vol[-1][-1] <= 1.5: vol[-1].append(t)
            else: vol.append([t])
        sq = downs(d['/DriverStation/Joystick0/ButtonValues'], 2); fire = downs(d['/DriverStation/Joystick1/ButtonValues'], 1)
        ev = d['/Events']
        gates = [t for t, e in ev if e.startswith('launch spot: gate open')]
        arrives = [t for t, e in ev if e.startswith('launch spot: arrived')]
        prev_end = te
        for k, v in enumerate(vol):
            f = v[0]
            g = max([x for x in gates if f - 3 <= x <= f], default=None)
            row = {'robot': team, 'match': NAMES[team][i], 'balls': len(v), 'first': f - te}
            if g is not None:
                a = max([x for x in arrives if g - 3 <= x <= g], default=g)
                s = max([x for x in sq if x <= a], default=a)
                row.update(kind='Square', away=s - prev_end, drive=a - s, settle=g - a, feed=f - g)
            else:
                fp = max([x for x in fire if f - 6 <= x <= f], default=None)
                if fp is None:
                    row.update(kind='other', away=None)
                else:
                    row.update(kind='FIRE', away=fp - prev_end, drive=0, settle=0, feed=f - fp)
            row['launch'] = v[-1] - v[0]
            row['cycle'] = None if k == 0 else f - vol[k - 1][0]
            row['first_volley'] = k == 0
            allv.append(row); prev_end = v[-1]
        per_match.append({'robot': team, 'match': NAMES[team][i], 'volleys': len(vol), 'balls': len(balls)})
json.dump({'volleys': allv, 'per_match': per_match}, open(sys.argv[3], 'w'), indent=1)
for team in ('19429', '20245'):
    vs = [v for v in allv if v['robot'] == team]
    print('====', team, 'volleys', len(vs), {k: sum(1 for v in vs if v['kind'] == k) for k in ('Square', 'FIRE', 'other')})
    cyc = [v['cycle'] for v in vs if v['cycle'] is not None]
    print(' cycle (first ball to next first ball) median %.1f s, p25 %.1f p75 %.1f, n %d' % (st.median(cyc), sorted(cyc)[len(cyc)//4], sorted(cyc)[3*len(cyc)//4], len(cyc)))
    for kind in ('Square', 'FIRE'):
        sub = [v for v in vs if v['kind'] == kind and not v['first_volley']]
        if not sub: continue
        print(' ', kind, 'n', len(sub), ' '.join('%s %.2f' % (k, st.median(v[k] for v in sub)) for k in ('away', 'drive', 'settle', 'feed', 'launch', 'cycle')), 'balls/volley %.1f' % st.mean(v['balls'] for v in sub))
    print(' first volley from TeleOp start median %.1f s' % st.median(v['first'] for v in vs if v['first_volley']))
