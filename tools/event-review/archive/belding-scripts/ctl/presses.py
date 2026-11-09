import sys, glob, os, re, json, statistics as st, collections as C
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]
BIT = {'cross': 0, 'circle': 1, 'square': 2, 'triangle': 3, 'L1': 9, 'R1': 10, 'up': 11, 'down': 12, 'left': 13, 'right': 14}
NAMES = {'19429': ['Q4','Q13','Q19','Q26','Q32','Playoff 1','Upper final','Lower final','Final'],
         '20245': ['Q1','Q7','Q14','Q27','Q31','Playoff 1','Upper final','Final']}
def edges(series, bit, t0, t1):
    out = []; prev = 0
    for t, v in series:
        b = (int(v) >> bit) & 1
        if b and not prev and t0 <= t <= t1: out.append(['down', t])
        if prev and not b and t0 <= t <= t1: out.append(['up', t])
        prev = b
    return out
def holds(series, bit, t0, t1):
    hs = []; start = None; prev = 0
    for t, v in series:
        b = (int(v) >> bit) & 1
        if b and not prev: start = t
        if prev and not b and start is not None:
            if start >= t0 and start <= t1: hs.append((start, t))
            start = None
        prev = b
    if start is not None and t0 <= start <= t1: hs.append((start, t1))
    return hs
res = {}
for team in ('19429', '20245'):
    rows = []
    for i, p in enumerate(sorted(glob.glob(f'{root}/{team}/matches/*.gz'))):
        d = R.read(p); m = d['/DriverStation/RobotMode']
        te = next(t for t, v in m if v == 'teleop'); end = d['/Robot/BatteryVolts'][-1][0]
        g1 = d['/DriverStation/Joystick0/ButtonValues']; g2 = d['/DriverStation/Joystick1/ButtonValues']
        ev = [(t, e) for t, e in d['/Events'] if te <= t <= end]
        cnt = {}
        for who, g in (('drv', g1), ('op', g2)):
            for name, bit in BIT.items():
                cnt[f'{who}_{name}'] = sum(1 for k, t in edges(g, bit, te, end) if k == 'down')
        sq = holds(g1, BIT['square'], te, end)
        opx = [t for k, t in edges(g2, BIT['cross'], te, end) if k == 'down']
        opo = [t for k, t in edges(g2, BIT['circle'], te, end) if k == 'down']
        ups = [t for t, e in ev if e.startswith('launcher: launch speed')]
        idles = [t for t, e in ev if e.startswith('launcher: idle')]
        def near(ts, t, w=0.08): return any(abs(x - t) <= w for x in ts)
        sq_starts = [a for a, b in sq]; sq_ends = [b for a, b in sq]
        spot_ends = [t for t, e in ev if re.match(r'launch spot: (let go|sticks moved|refused)', e)]
        up_by = C.Counter(); idle_by = C.Counter()
        for t in ups:
            up_by['Square' if near(sq_starts, t) else 'Op Cross' if near(opx, t) else 'Op Circle' if near(opo, t) else 'other'] += 1
        for t in idles:
            idle_by['Square ended' if near(spot_ends, t) or near(sq_ends, t) else 'Op Cross' if near(opx, t) else 'other'] += 1
        # interactions
        x_during_sq = [t for t in opx if any(a < t < b for a, b in sq)]
        x_idled_during = [t for t in x_during_sq if near(idles, t)]
        o_during_sq = [t for t in opo if any(a < t < b for a, b in sq)]
        # was launcher already spinning when Square pressed (no spin-up at press)?
        tgt = d['/Launcher/TargetTicksPerSecond']
        sq_found_spinning = sum(1 for a in sq_starts if (R.at(tgt, a - 0.02) or 0) > 0)
        # idle then spin-up again soon
        regaps = []
        for t in idles:
            n = next((u for u in ups if u > t), None)
            if n: regaps.append(n - t)
        rows.append({'robot': team, 'match': NAMES[team][i], 'order': i + 1, **cnt,
                     'square_presses': len(sq), 'spinups': len(ups), 'idles': len(idles),
                     'up_square': up_by['Square'], 'up_op_cross': up_by['Op Cross'], 'up_op_circle': up_by['Op Circle'], 'up_other': up_by['other'],
                     'idle_square': idle_by['Square ended'], 'idle_op_cross': idle_by['Op Cross'], 'idle_other': idle_by['other'],
                     'op_cross_during_square': len(x_during_sq), 'op_cross_idled_mid_square': len(x_idled_during),
                     'op_circle_during_square': len(o_during_sq), 'square_found_spinning': sq_found_spinning,
                     'respin_within_3s': sum(1 for g in regaps if g < 3), 'respin_gaps': [round(g, 2) for g in regaps]})
    res[team] = rows
json.dump(res, open(sys.argv[3], 'w'), indent=1)
for team, rows in res.items():
    print('=====', team)
    keys = ['drv_square','drv_circle','drv_cross','drv_triangle','op_cross','op_circle','op_L1','op_R1','op_square','op_down','spinups','up_square','up_op_cross','up_op_circle','up_other','idles','idle_square','idle_op_cross','idle_other','op_cross_during_square','op_cross_idled_mid_square','op_circle_during_square','square_found_spinning','respin_within_3s']
    print('match       ' + ' '.join(k[:9].rjust(9) for k in keys))
    for r in rows: print(r['match'][:11].ljust(11), ' '.join(str(r[k]).rjust(9) for k in keys))
    print('TOTAL      ', ' '.join(str(sum(r[k] for r in rows)).rjust(9) for k in keys))
    gaps = sorted(g for r in rows for g in r['respin_gaps']); print('idle->next spin-up gaps median', st.median(gaps) if gaps else None, 'n', len(gaps), 'under 1s', sum(g < 1 for g in gaps), 'under 3s', sum(g < 3 for g in gaps))
