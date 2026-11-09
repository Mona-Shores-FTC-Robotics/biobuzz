import sys, glob, os, re, json, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root, out = sys.argv[2], sys.argv[3]
NAMES = {'19429': ['Q4','Q13','Q19','Q26','Q32','Playoff 1','Upper final','Lower final','Final'],
         '20245': ['Q1','Q7','Q14','Q27','Q31','Playoff 1','Upper final','Final']}
def at(s, t):
    v = None
    for ts, x in s:
        if ts > t: break
        v = x
    return v
r2 = lambda x: None if x is None else round(x, 2)
matches, robots, recov, volley, outcomes = [], [], [], [], []
amps = {'Launcher (both wheels, at speed)': [], 'Feeder (running)': [], 'Intake (running)': []}
ah_all = []
for team in ('19429', '20245'):
    files = sorted(glob.glob(f'{root}/{team}/matches/*.gz'))
    allv = []; pushes = []; sticks = 0; presses = 0; ref_pose = 0; ref_side = 0
    arrive = []; gate = []; first = []; gaps = []; ready = []; spun = []
    oc = {}
    for i, p in enumerate(files):
        d = R.read(p); m = d['/DriverStation/RobotMode']
        a = next(t for t, v in m if v == 'autonomous'); te = next(t for t, v in m if v == 'teleop')
        bv = [(t, v) for t, v in d['/Robot/BatteryVolts'] if v > 1]; end = bv[-1][0]
        plays = [float(re.search(r'battery ([\d.]+) V', e).group(1)) for t, e in d['/Events'] if e.startswith('PLAY: battery')]
        tv = [v for t, v in bv if te <= t <= end]
        allv += [v for t, v in bv if a <= t <= end]
        ev = [(t, e) for t, e in d['/Events'] if t >= te]
        ups = [t for t, e in ev if e.startswith('launcher: launch speed')]
        ats = d['/Launcher/AtSpeed']; wait = 0
        for t in ups:
            n = next((tt for tt, v in ats if tt > t and v), None)
            if n: wait += n - t
        tgt = d['/Launcher/TargetTicksPerSecond']; on = tot = 0; prev = None
        for t, v in tgt:
            if prev and a <= prev[0] <= end:
                dt = t - prev[0]; tot += dt; on += dt if prev[1] > 0 else 0
            prev = (t, v)
        matches.append({'robot': team, 'match': NAMES[team][i], 'order': i + 1,
                        'init_v': r2(bv[0][1]), 'auto_play_v': r2(plays[0]), 'teleop_play_v': r2(plays[1]),
                        'teleop_median_v': r2(st.median(tv)), 'teleop_low_v': r2(min(tv)),
                        'spinups': len(ups), 'spinup_wait_s': round(wait, 1), 'launcher_on_pct': round(100 * on / tot)})
        # currents
        L, Rr = d['/Launcher/LeftCurrentAmps'], d['/Launcher/RightCurrentAmps']
        for t, v in L:
            if a <= t <= end and (at(tgt, t) or 0) > 0 and at(ats, t) and not at(d['/Launcher/InDip'], t):
                amps['Launcher (both wheels, at speed)'].append(abs(v) + abs(at(Rr, t) or 0))
        for k, name in (('/Feeder/CurrentAmps', 'Feeder (running)'), ('/Intake/CurrentAmps', 'Intake (running)')):
            amps[name] += [abs(v) for t, v in d[k] if a <= t <= end and abs(v) > 0.3]
        ah = 0
        for k in ('/Launcher/LeftCurrentAmps', '/Launcher/RightCurrentAmps', '/Intake/CurrentAmps', '/Feeder/CurrentAmps'):
            s = [(t, abs(v)) for t, v in d[k] if a <= t <= end]
            ah += sum((s[j + 1][0] - s[j][0]) * s[j][1] for j in range(len(s) - 1)) / 3600
        ah_all.append(ah)
        # shot recovery vs battery
        for t, rms in d.get('/Launcher/RecoverMs', []):
            if a <= t <= end: recov.append((at(d['/Robot/BatteryVolts'], t), rms))
        sh = d['/Launcher/Shots']
        for t, e in ev:
            if e.startswith('push:'): pushes.append(float(re.search(r'push: ([\d.]+) s', e).group(1)))
            if e.startswith('launch spot: start'): presses += 1
            if e.startswith('launch spot: refused'):
                presses += 1
                if 'pose not on the field' in e: ref_pose += 1
                elif 'other alliance' in e: ref_side += 1
            if e.startswith('launch spot: arrived'): arrive.append(float(re.search(r'in ([\d.]+) s', e).group(1)))
            if e.startswith('launch spot: gate open'):
                gate.append(float(re.search(r'\(([\d.]+) s after arriving', e).group(1)))
            mm = re.match(r'launch spot: (let go early|let go|sticks moved early|sticks moved|refused)', e)
            if mm:
                k = {'let go': 'Let go at the spot', 'sticks moved': 'Sticks took over at the spot',
                     'let go early': 'Let go before arriving', 'sticks moved early': 'Sticks cancelled it on the way',
                     'refused': 'Refused'}[mm.group(1)]
                n = re.search(r'(\d+) shots counted', e)
                o = oc.setdefault(k, [0, 0]); o[0] += 1; o[1] += int(n.group(1)) if n else 0
                if mm.group(1) == 'sticks moved early': sticks += 1
            if e.startswith('fire:') and ('→ fired' in e or 'WAITING' in e or 'SPINNING' in e):
                s0 = at(sh, t) or 0; n = next((tt for tt, v in sh if tt > t and v > s0), None)
                if n and n - t < 5: (ready if '→ fired' in e else spun).append(n - t)
        for t, e in d['/Events']:
            if e.startswith('launch spot: gate open') or e.startswith('auto: LaunchAll') or ('fire:' in e and '→ fired' in e):
                s0 = at(sh, t) or 0; inc = []; last = s0
                for tt, v in sh:
                    if t < tt < t + 4 and v > last: inc.append(tt); last = v
                if inc: first.append(inc[0] - t); gaps += [b - c for c, b in zip(inc, inc[1:])]
    for k, (n, s) in oc.items(): outcomes.append({'robot': team, 'outcome': k, 'presses': n, 'shots': s})
    for j, (step, xs) in enumerate((('Drive to the spot', arrive), ('Speed and settle check', gate),
                                    ('Feed to first ball', first), ('Each next ball', gaps))):
        volley.append({'robot': team, 'step': step, 'order': j + 1, 'seconds': r2(st.median(xs)), 'samples': len(xs)})
    robots.append({'robot': team, 'teleop_median_v': r2(st.median(allv)), 'pct_below_10v': round(100 * sum(v < 10 for v in allv) / len(allv), 1),
                   'pct_below_9v': round(100 * sum(v < 9 for v in allv) / len(allv), 1), 'lowest_v': r2(min(allv)),
                   'pushes': len(pushes), 'push_s': round(sum(pushes), 1), 'spot_presses': presses,
                   'cancelled_by_sticks': sticks, 'refused_pose': ref_pose, 'refused_other_half': ref_side,
                   'fire_ready_s': r2(st.median(ready)) if ready else None, 'fire_ready_n': len(ready),
                   'fire_spinup_s': r2(st.median(spun)) if spun else None, 'fire_spinup_n': len(spun)})
bands = [('Under 10 V', 0, 10), ('10 to 11 V', 10, 11), ('11 to 12 V', 11, 12), ('Over 12 V', 12, 99)]
rec = [{'battery': b, 'order': i + 1, 'shots': len(x), 'median_ms': round(st.median(x))}
       for i, (b, lo, hi) in enumerate(bands) for x in [[r for v, r in recov if v and lo <= v < hi]] if x]
mech = [{'mechanism': k, 'amps': r2(st.median(v))} for k, v in amps.items()]
energy = [{'measure': 'Launcher, feeder and intake per match', 'median_ah': round(st.median(ah_all), 2), 'max_ah': round(max(ah_all), 2)}]
for name, rows in (('matches', matches), ('robots', robots), ('shot_recovery', rec), ('mechanisms', mech),
                   ('energy', energy), ('volley_steps', volley), ('spot_outcomes', outcomes)):
    json.dump(rows, open(f'{out}/{name}.json', 'w'), indent=1)
    print(name, len(rows))
print(json.dumps(robots, indent=1)); print(json.dumps(volley)); print(rec, mech, energy)
