"""Battery, launcher duty and the launch-spot press tallies (archive/belding-scripts/pw/gen.py).

matches, robots, shot_recovery, mechanisms, energy, volley_steps, and the press tallies that
spot_outcomes starts from. Windows: Auto start (Driver Station autonomous) to the last battery
sample above 1 V; TeleOp from the Driver Station's teleop.
"""
import re
import statistics as st

r2 = lambda x: None if x is None else round(x, 2)

OUTCOMES = {'let go': 'Let go at the spot', 'sticks moved': 'Sticks took over at the spot',
            'let go early': 'Let go before arriving', 'sticks moved early': 'Sticks cancelled it on the way',
            'refused': 'Refused'}
BANDS = [('Under 10 V', 0, 10), ('10 to 11 V', 10, 11), ('11 to 12 V', 11, 12), ('Over 12 V', 12, 99)]


def build(by_team):
    matches, robots, recov, volley, outcomes = [], [], [], [], []
    amps = {'Launcher (both wheels, at speed)': [], 'Feeder (running)': [], 'Intake (running)': []}
    ah_all = []
    for team, ms in by_team.items():
        allv, pushes, sticks, presses, ref_pose, ref_side = [], [], 0, 0, 0, 0
        arrive, gate, first, gaps, ready, spun = [], [], [], [], [], []
        oc = {}
        for m in ms:
            a, te = m.auto_start, m.tele_start
            bv = [(t, v) for t, v in m['/Robot/BatteryVolts'] if v > 1]
            end = bv[-1][0]
            plays = [float(re.search(r'battery ([\d.]+) V', e).group(1)) for t, e in m.events if e.startswith('PLAY: battery')]
            tv = [v for t, v in bv if te <= t <= end]
            allv += [v for t, v in bv if a <= t <= end]
            ev = [(t, e) for t, e in m.events if t >= te]
            ups = [t for t, e in ev if e.startswith('launcher: launch speed')]
            ats = m['/Launcher/AtSpeed']
            wait = 0
            for t in ups:
                n = ats.first(t + 1e-9, 1e9, bool)
                if n:
                    wait += n - t
            tgt = m['/Launcher/TargetTicksPerSecond']
            on = tot = 0
            prev = None
            for t, v in tgt:
                if prev and a <= prev[0] <= end:
                    dt = t - prev[0]
                    tot += dt
                    on += dt if prev[1] > 0 else 0
                prev = (t, v)
            matches.append({'robot': team, 'match': m.name, 'order': m.order,
                            'init_v': r2(bv[0][1]), 'auto_play_v': r2(plays[0]) if plays else None,
                            'teleop_play_v': r2(plays[1]) if len(plays) > 1 else None,
                            'teleop_median_v': r2(st.median(tv)), 'teleop_low_v': r2(min(tv)),
                            'spinups': len(ups), 'spinup_wait_s': round(wait, 1), 'launcher_on_pct': round(100 * on / tot)})
            L, Rr, indip = m['/Launcher/LeftCurrentAmps'], m['/Launcher/RightCurrentAmps'], m['/Launcher/InDip']
            for t, v in L:
                if a <= t <= end and (tgt.at(t) or 0) > 0 and ats.at(t) and not indip.at(t):
                    amps['Launcher (both wheels, at speed)'].append(abs(v) + abs(Rr.at(t) or 0))
            for k, name in (('/Feeder/CurrentAmps', 'Feeder (running)'), ('/Intake/CurrentAmps', 'Intake (running)')):
                amps[name] += [abs(v) for t, v in m[k] if a <= t <= end and abs(v) > 0.3]
            ah = 0
            for k in ('/Launcher/LeftCurrentAmps', '/Launcher/RightCurrentAmps', '/Intake/CurrentAmps', '/Feeder/CurrentAmps'):
                s = [(t, abs(v)) for t, v in m[k] if a <= t <= end]
                ah += sum((s[j + 1][0] - s[j][0]) * s[j][1] for j in range(len(s) - 1)) / 3600
            ah_all.append(ah)
            batt = m['/Robot/BatteryVolts']
            for t, rms in m['/Launcher/RecoverMs']:
                if a <= t <= end:
                    recov.append((batt.at(t), rms))
            sh = m['/Launcher/Shots']
            for t, e in ev:
                if e.startswith('push:'):
                    pushes.append(float(re.search(r'push: ([\d.]+) s', e).group(1)))
                if e.startswith('launch spot: start'):
                    presses += 1
                if e.startswith('launch spot: refused'):
                    presses += 1
                    if 'pose not on the field' in e:
                        ref_pose += 1
                    elif 'other alliance' in e:
                        ref_side += 1
                if e.startswith('launch spot: arrived'):
                    arrive.append(float(re.search(r'in ([\d.]+) s', e).group(1)))
                if e.startswith('launch spot: gate open'):
                    gate.append(float(re.search(r'\(([\d.]+) s after arriving', e).group(1)))
                mm = re.match(r'launch spot: (let go early|let go|sticks moved early|sticks moved|refused)', e)
                if mm:
                    k = OUTCOMES[mm.group(1)]
                    n = re.search(r'(\d+) shots counted', e)
                    o = oc.setdefault(k, [0, 0])
                    o[0] += 1
                    o[1] += int(n.group(1)) if n else 0
                    if mm.group(1) == 'sticks moved early':
                        sticks += 1
                if e.startswith('fire:') and ('→ fired' in e or 'WAITING' in e or 'SPINNING' in e):
                    s0 = sh.at(t) or 0
                    n = sh.first(t + 1e-9, 1e9, lambda v: v > s0)
                    if n and n - t < 5:
                        (ready if '→ fired' in e else spun).append(n - t)
            for t, e in m.events:
                if e.startswith('launch spot: gate open') or e.startswith('auto: LaunchAll') or ('fire:' in e and '→ fired' in e):
                    s0 = sh.at(t) or 0
                    inc, last = [], s0
                    for tt, v in sh.between(t + 1e-9, t + 4 - 1e-9):
                        if v > last:
                            inc.append(tt)
                            last = v
                    if inc:
                        first.append(inc[0] - t)
                        gaps += [b - c for c, b in zip(inc, inc[1:])]
        for k, (n, s) in oc.items():
            outcomes.append({'robot': team, 'outcome': k, 'presses': n, 'shots': s})
        for j, (step, xs) in enumerate((('Drive to the spot', arrive), ('Speed and settle check', gate),
                                        ('Feed to first piece', first), ('Each next ball', gaps))):
            volley.append({'robot': team, 'step': step, 'order': j + 1, 'seconds': r2(st.median(xs)) if xs else None,
                           'samples': len(xs)})
        robots.append({'robot': team, 'teleop_median_v': r2(st.median(allv)),
                       'pct_below_10v': round(100 * sum(v < 10 for v in allv) / len(allv), 1),
                       'pct_below_9v': round(100 * sum(v < 9 for v in allv) / len(allv), 1), 'lowest_v': r2(min(allv)),
                       'pushes': len(pushes), 'push_s': round(sum(pushes), 1), 'spot_presses': presses,
                       'cancelled_by_sticks': sticks, 'refused_pose': ref_pose, 'refused_other_half': ref_side,
                       'fire_ready_s': r2(st.median(ready)) if ready else None, 'fire_ready_n': len(ready),
                       'fire_spinup_s': r2(st.median(spun)) if spun else None, 'fire_spinup_n': len(spun)})
    rec = [{'battery': b, 'order': i + 1, 'shots': len(x), 'median_ms': round(st.median(x))}
           for i, (b, lo, hi) in enumerate(BANDS) for x in [[r for v, r in recov if v and lo <= v < hi]] if x]
    mech = [{'mechanism': k, 'amps': r2(st.median(v)) if v else None} for k, v in amps.items()]
    energy = [{'measure': 'Launcher, feeder and intake per match', 'median_ah': round(st.median(ah_all), 2),
               'max_ah': round(max(ah_all), 2)}]
    # Auto and TeleOp shots per match, from the counter the robot ran (the page uses the detector's: pieces).
    for row in matches:
        m = next(x for x in by_team[row['robot']] if x.order == row['order'])
        sh = m['/Launcher/Shots']
        s = lambda x: sh.at(x) or 0
        row['auto_balls'] = int(s(m.auto_end) - s(m.auto_start))
        row['teleop_balls'] = int(s(m['/Robot/BatteryVolts'].t[-1]) - s(m.tele_start))
    return {'matches': matches, 'robots': robots, 'shot_recovery': rec, 'mechanisms': mech, 'energy': energy,
            'volley_steps': volley, 'spot_outcomes': outcomes}
