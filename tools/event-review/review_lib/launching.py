"""Cycle time, launching, feed timing and the auto-drive press datasets.

Built on volleys.build() (the TeleOp volley table), the #506 detector's gate openings
(shots.volleys) and the launch-spot / FIRE events. Each function says which page numbers it makes.
"""
import collections
import re
import statistics as st

from .volleys import md, nn, downs, SQUARE, CIRCLE
from .shots import old_shots

NAMES = {'Square': 'Auto-drive', 'FIRE': 'Operator FIRE'}


def cycle_sets(V, team):
    """The cycles the chart uses: TeleOp volleys after the first, with a press to time from."""
    c = [v for v in V if v['robot'] == team and v.get('away') is not None and not v['first_volley'] and v['cycle'] is not None]
    c2 = [v for v in V if v['robot'] == team and v.get('away') is not None and not v['first_volley'] and v.get('cycle2') is not None]
    return c, c2


def cycles(V, teams):
    """cycle_groups, cycle_group_summary, cycle_steps (Cycle time section)."""
    rows, launch, summ, steps = [], [], [], []
    for t in teams:
        c, c2 = cycle_sets(V, t)
        for g, full in (('Filled up', True), ('Shot before full', False)):
            sub = [v for v in c if bool(v.get('full_before_press')) == full]
            sub2 = [v for v in c2 if bool(v.get('full_before_press')) == full]
            tfp = [v['tfp2'] for v in sub2]
            n = nn(tfp)
            rows.append(dict(robot=t, group=g, step='To first piece', order=1, phase='collect', seconds=md(tfp), samples=n))
            if full:
                col = [(v['collect'] - v['to_first_piece']) if v['to_first_piece'] is not None and v['collect'] is not None else None for v in sub]
                back = [v['return'] for v in sub]
            else:
                col = [(v['last_in'] - v['to_first_piece']) if v['last_in'] is not None and v['to_first_piece'] is not None else None for v in sub]
                back = [v['last_to_launch'] for v in sub]
            nsub = nn([v['to_first_piece'] for v in sub])
            rows.append(dict(robot=t, group=g, step='Collecting', order=2, phase='collect', seconds=md(col), samples=nsub))
            rows.append(dict(robot=t, group=g, step='Drive back with pieces', order=3, phase='collect', seconds=md(back), samples=nsub))
            launch.append(dict(robot=t, group=g, step='Launch, press to moving again', order=4, phase='launch',
                               seconds=md([v['launch_span'] for v in sub2]), samples=n))
            k = collections.Counter(v['kind'] for v in sub)
            summ.append(dict(robot=t, group=g, cycles=len(sub), cycle_median_s=md([v['cycle'] for v in sub]),
                             balls_per_volley=round(st.mean(v['balls'] for v in sub), 1) if sub else None,
                             share=round(len(sub) / len(c), 2) if c else None,
                             lost_4th_by_shot=sum(1 for v in sub if not v['present_at_press']) if full else None,
                             auto_drive=k.get('Square', 0), fire=k.get('FIRE', 0)))
        # The earlier, filled-up-only breakdown (kept, not drawn): from the previous volley's last shot.
        sub = [v for v in c if v.get('full_before_press')]
        sq = [v for v in sub if v['kind'] == 'Square'] or sub
        for i, (name, xs, phase) in enumerate((
                ('To first piece', [v['to_first_piece'] for v in sub], 'collect'),
                ('Fill up', [(v['collect'] - v['to_first_piece']) if v['to_first_piece'] is not None and v['collect'] is not None else None for v in sub], 'collect'),
                ('Hold full, drive back', [v['return'] for v in sub], 'collect'),
                ('Drive to spot', [v['drive'] for v in sq], 'launch'), ('Settle check', [v['settle'] for v in sq], 'launch'),
                ('Gate to first piece', [v['feed'] for v in sq], 'launch'), ('Rest of volley', [v['launch'] for v in sub], 'launch'))):
            steps.append(dict(robot=t, step=name, order=i + 1, seconds=md(xs), samples=nn(xs), phase=phase))
    return {'cycle_groups': rows + launch, 'cycle_group_summary': summ, 'cycle_steps': steps}


def gate_rows(ms):
    """Every feed-gate opening with at least one counted shot (old counter): gate open to first
    shot, shot to shot, the lane at the opening (archive/belding-scripts/gate.py)."""
    rows = []
    for m in ms:
        tele = m.tele_init if m.tele_init is not None else 1e9
        feed = m['/FeedGate/Feeding']
        sh = m['/Launcher/Shots']
        inc = [t for (t0, a), (t, b) in zip(sh, list(sh)[1:]) if b > a]
        prev = 0
        for i, (t, v) in enumerate(feed):
            if not (v and not prev):
                prev = v
                continue
            prev = v
            g = t
            end = feed.first(g + 1e-9, 1e9, lambda x: not x) or g + 5
            s = [x for x in inc if g <= x <= end + 0.3]
            if not s:
                continue
            rows.append(dict(team=m.team, log=m.log, m=m, phase='auto' if g < tele else 'tele', open=g, first=s[0] - g,
                             n=len(s), gaps=[b - a for a, b in zip(s, s[1:])], lane=m['/Lane/Count'].at(g - 0.02),
                             slots=[int(m['/Lane/Slot%d' % k].at(g - 0.02) or 0) for k in (1, 2, 3, 4)]))
    return rows


def feed(ms_by_team, V, cfg):
    """feed_summary (Cycle time cards), feed_volleys, feed_nectar (kept)."""
    summary, fv, fnec = [], [], []
    excl = {tuple(x) for x in cfg.get('exclude', {}).get('first_volley', [])}
    for team, ms in ms_by_team.items():
        rows = gate_rows(ms)
        f = [r['first'] for r in rows]
        g = [y for r in rows for y in r['gaps']]
        nm = len({r['log'] for r in rows})
        mg = st.median(g) if g else 0
        over = sum(x > 1 for x in f)
        allv = [v for v in V if v['robot'] == team and v.get('launch_span') is not None]
        fvs = [v['press'] for v in V if v['robot'] == team and v['first_volley'] and 'press' in v and (team, v['match']) not in excl]
        summary.append(dict(robot=team, first_median_s=round(st.median(f), 2) if f else None, next_median_s=round(mg, 2) if g else None,
                            volleys=len(f), over_1s=over, over_1s_pct=round(over / len(f), 3) if f else None,
                            slower_than_next=sum(x > mg for x in f),
                            extra_per_match_s=round(sum(max(0, x - mg) for x in f) / nm, 1) if nm else None,
                            launch_span_s=md([v['launch_span'] for v in allv]),
                            first_volley_s=round(st.median([round(x, 1) for x in fvs]), 1) if fvs else None))
        for r in rows:
            s = r['slots']
            kind = 'front' if s[0] in (2, 3) else 'behind' if any(x in (2, 3) for x in s) else 'none'
            fv.append(dict(robot=team, phase='Auto' if r['phase'] == 'auto' else 'TeleOp', first_s=round(r['first'], 3),
                           lane=int(r['lane'] or 0), shots=r['n'], nectar=kind))
        # Seconds per piece by type, from the lane's front slot as each counted shot left.
        per = collections.defaultdict(list)
        for r in rows:
            m = r['m']
            sh = m['/Launcher/Shots']
            inc = [t for (t0, a), (t, b) in zip(sh, list(sh)[1:]) if b > a]
            gopen = r['open']
            s = [t for t in inc if gopen <= t <= gopen + 0.3 + r['first'] + sum(r['gaps']) + 0.05][:r['n']]
            prev = gopen
            for i, t in enumerate(s):
                piece = int(m['/Lane/Slot1'].at(t - 0.03) or 0)
                kind = 'POLLEN' if piece == 1 else 'NECTAR' if piece in (2, 3) else None
                if kind:
                    per[('First out' if i == 0 else 'Each later piece', kind)].append(t - prev)
                prev = t
        if any(k[1] == 'NECTAR' for k in per):
            for o, (pos, piece) in enumerate((('First out', 'POLLEN'), ('First out', 'NECTAR'),
                                              ('Each later piece', 'POLLEN'), ('Each later piece', 'NECTAR'))):
                x = per.get((pos, piece), [])
                if x:
                    fnec.append(dict(robot=team, piece=piece, position=pos, order=o, n=len(x),
                                     median_s=round(st.median(x), 2), over_1s=sum(y > 1 for y in x)))
    return {'feed_summary': summary, 'feed_volleys': fv, 'feed_nectar': fnec}


def fire_presses(m):
    """Operator Circle (FIRE) holds of 0.5 s or more in TeleOp, with the launcher's state
    (archive/belding-scripts/ctl/fire2.py, classified as in the Belding cycle sheet)."""
    te, end = m.tele_start, m['/Robot/BatteryVolts'].t[-1]
    g2 = m['/DriverStation/Joystick1/ButtonValues']
    g1, gate, tgt, ats, lane = (m[k] for k in ('/DriverStation/Joystick0/ButtonValues', '/FeedGate/Feeding',
                                                '/Launcher/TargetTicksPerSecond', '/Launcher/AtSpeed', '/Lane/Count'))
    sh = m['/Launcher/Shots']
    out, prev, down = [], 0, None
    for t, v in g2:
        v = int(v)
        was, now = (prev >> CIRCLE) & 1, (v >> CIRCLE) & 1
        if now and not was:
            down = t
        if was and not now and down is not None:
            t0, t1 = down, t
            down = None
            if te <= t0 <= end and t1 - t0 >= 0.5:
                r = dict(at_speed=bool(ats.at(t0)), target=round(tgt.at(t0) or 0), lane=lane.at(t0),
                         square_held=bool((int(g1.at(t0) or 0) >> SQUARE) & 1))
                ga = gate.first(t0, t1 + 0.05, bool)
                r['gate_open_after'] = None if ga is None else round(ga - t0, 2)
                a2 = None if r['at_speed'] else ats.first(t0, t0 + 5, bool)
                r['at_speed_after'] = None if a2 is None else round(a2 - t0, 2)
                s0 = sh.at(t0) or 0
                balls = [tt for tt, x in sh.between(t0 + 1e-9, t1 + 1.0) if x > s0]
                r['first_ball'] = None if not balls else round(balls[0] - t0, 2)
                r['category'] = classify(r)
                out.append(r)
        prev = v
    return out


def classify(r):
    if r['lane'] is not None and r['lane'] == 0 and r['first_ball'] is None:
        return 'Robot empty'
    if r['gate_open_after'] is None and r['target'] > 0 and not r['at_speed'] and r['at_speed_after'] is None:
        return 'Wheel shut off'
    if r['gate_open_after'] is not None and r['gate_open_after'] > 2:
        return 'Wheel shut off'
    if r['first_ball'] is None:
        return 'Gate opened, no piece counted'
    if not r['at_speed']:
        return 'Launcher not at speed yet'
    if r['first_ball'] > 1.5:
        return 'Gate opened, piece slow to arrive'
    return 'Ready'


def launch(ms_by_team, V, spot):
    """launch_volleys (TeleOp volleys chart) and launch_methods (the two method cards)."""
    lv = []
    order = {(m.team, m.name): m.order for ms in ms_by_team.values() for m in ms}
    for v in V:
        if v['kind'] not in ('Square', 'FIRE') or 'feed' not in v:
            continue
        span = v.get('launch_span') or (v['feed'] + v['launch'] + v['drive'] + v['settle'])
        lv.append(dict(robot=v['robot'], match=v['match'], match_order=order[(v['robot'], v['match'])],
                       press=round(v['press'], 1), span=round(span, 1), t=round(v['first'], 1), method=NAMES[v['kind']]))
    lv.sort(key=lambda r: (r['robot'], r['match_order'], r['press']))
    # Pre-spin: the launcher at speed just before the press; what it saved, press to gate open,
    # pooled over both robots (auto-drive from its volleys, Manual from every FIRE hold).
    pre = {}
    sq_gate = {True: [], False: []}
    fp = {}
    for team, ms in ms_by_team.items():
        fp[team] = []
        for m in ms:
            te = m.tele_start
            A = m['/Launcher/AtSpeed']
            g1 = m['/DriverStation/Joystick0/ButtonValues']
            for v in [x for x in V if x['robot'] == team and x['log'] == m.log and 'feed' in x and x['kind'] in ('Square', 'FIRE')]:
                press = v['press'] + te
                at = bool(A.at(press - 0.02))
                pre.setdefault((team, v['kind']), []).append(at)
                if v['kind'] == 'Square':
                    sq_gate[at].append(v['first'] - v['feed'] - v['press'])
                else:
                    g = v['first'] - v['feed'] + te
                    aimed = any((int(g1.at(g + dt / 10) or 0) >> CIRCLE) & 1 for dt in range(-15, int(v['feed'] * 10) + 1))
                    pre.setdefault((team, 'aim'), []).append(aimed)
            fp[team] += fire_presses(m)
    allfp = [r for x in fp.values() for r in x]
    save = {'Square': round(st.median(sq_gate[False]) - st.median(sq_gate[True]), 1) if sq_gate[False] and sq_gate[True] else None}
    ga = {a: [r['gate_open_after'] for r in allfp if r['at_speed'] == a and r['gate_open_after'] is not None] for a in (True, False)}
    save['FIRE'] = round(st.median(ga[False]) - st.median(ga[True]), 1) if ga[False] and ga[True] else None
    lm = []
    for team in ms_by_team:
        for kind in ('Square', 'FIRE'):
            s = [v for v in V if v['robot'] == team and v['kind'] == kind]
            if not s:
                continue
            ps = pre.get((team, kind), [])
            r = dict(robot=team, method=NAMES[kind], volleys=len(s), pieces=sum(v['balls'] for v in s),
                     per_volley=round(st.mean(v['balls'] for v in s), 1),
                     press_to_last_s=round(st.median((v['drive'] or 0) + (v['settle'] or 0) + (v['feed'] or 0) + (v['launch'] or 0) for v in s), 1),
                     not_ready=None if kind == 'Square' else sum(1 for x in fp[team] if x['category'] == 'Launcher not at speed yet'),
                     press_to_moving_s=md([v['launch_span'] for v in s]),
                     prespun=sum(ps), prespun_of='%d of %d' % (sum(ps), len(ps)), prespin_save_s=save[kind])
            if kind == 'Square':
                r['presses_launched_of'] = '%d of %d' % (spot[team]['launched'], spot[team]['presses'])
            else:
                aim = pre.get((team, 'aim'), [])
                r['driver_aim_of'] = '%d of %d' % (sum(aim), len(aim))
            lm.append(r)
    lm.sort(key=lambda r: (-int(r['robot']), r['method']))
    return {'launch_volleys': lv, 'launch_methods': lm}


def spot_outcomes(ms_by_team, base, vols):
    """spot_outcomes with the #506 detector's pieces per press: shots = new count, shots_old = the
    robot's own count where they differ. Each press runs from its `launch spot: start` to its end."""
    cat = {'let go after': 'Let go at the spot', 'sticks moved after': 'Sticks took over at the spot',
           'let go early': 'Let go before arriving', 'sticks moved early': 'Sticks cancelled it on the way'}
    new = collections.Counter()
    for team, ms in ms_by_team.items():
        for m in ms:
            vs = [v for v in vols if v['team'] == team and v['log'] == m.log]
            start = None
            for t, e in m.events:
                if not e.startswith('launch spot:'):
                    continue
                body = e[len('launch spot: '):]
                if body.startswith('start'):
                    start = t
                    continue
                if body.startswith(('arrived', 'drift', 'gate open')) or body.startswith('refused'):
                    if body.startswith('refused'):
                        start = None
                    continue
                if start is None:
                    continue
                k = next((c for p, c in cat.items() if body.startswith(p)), None)
                if k:
                    new[(team, k)] += sum(x['new'] for x in vs if start - 0.05 <= x['open_s'] <= t + 0.05)
                start = None
    out, spot = [], {}
    for r in base:
        r = dict(r)
        k = (r['robot'], r['outcome'])
        if r['outcome'] != 'Refused':
            n = new.get(k, 0)
            if n != r['shots']:
                r['shots_old'] = r['shots']
                r['shots'] = n
        out.append(r)
    for team in ms_by_team:
        rs = [r for r in out if r['robot'] == team]
        spot[team] = dict(presses=sum(r['presses'] for r in rs),
                          launched=sum(r['presses'] for r in rs if r['outcome'] in ('Let go at the spot', 'Sticks took over at the spot')))
    return out, spot


def volley_pieces(ms_by_team, V, vols, cfg):
    """volley_pieces: the full-volley chart (reliable 4-shot gate openings, #506 detector) and how
    long the robot stayed at the spot after the gate opened in full-lane TeleOp volleys."""
    excl = {tuple(x) for x in cfg.get('exclude', {}).get('leave', [])}
    out = []
    for team, ms in ms_by_team.items():
        f = [v for v in vols if v['team'] == team and v['reliable'] and v['new'] == 4]
        d = [v['dip_start_s'] for v in f]
        c = collections.Counter(v['phase'] for v in f)
        leave = []
        for m in ms:
            if (team, m.name) in excl:
                continue
            te = m.tele_start
            F, L = m['/FeedGate/Feeding'], m['/Lane/Count']
            opens = [t - te for (a, x), (t, y) in zip(F, list(F)[1:]) if y and not x and t > te]
            for v in [v for v in V if v['robot'] == team and v['log'] == m.log and v.get('moving_again') and 'feed' in v]:
                go = v['first'] - v['feed']
                g = min(opens, key=lambda o: abs(o - go)) if opens else None
                if g is None or abs(g - go) > 0.3:
                    continue
                if (L.at(g + te - 0.03) or 0) >= 4:
                    leave.append(v['moving_again'] - g)
        r = dict(robot=team, pieces=4, volleys=len(f))
        if d:
            r.update(p1=round(st.median(x[0] for x in d), 2), p2=round(st.median(x[1] - x[0] for x in d), 2),
                     p3=round(st.median(x[2] - x[1] for x in d), 2), p4=round(st.median(x[3] - x[2] for x in d), 2),
                     total=round(st.median(x[3] for x in d), 2),
                     later=round(st.median(x[i] - x[i - 1] for x in d for i in (1, 2, 3)), 2))
        r.update(leave_s=round(st.median(leave), 2) if leave else None, leave_n=len(leave), auto=c['auto'], tele=c['tele'])
        out.append(r)
    out.sort(key=lambda r: -int(r['robot']))
    return out


def stick_cancels(ms_by_team):
    """stick_cancels: each `launch spot: sticks moved early` (a press cancelled on the way)."""
    out = []
    for team, ms in ms_by_team.items():
        cs, moving, n, delays, helds, rep, cent25, mags = [], 0, 0, [], [], 0, 0, []
        for m in ms:
            g1 = m['/DriverStation/Joystick0/ButtonValues']
            F, S, T = m['/Drive/CommandForward'], m['/Drive/CommandStrafe'], m['/Drive/CommandTurn']
            mag = lambda t: max(abs(F.at(t) or 0), abs(S.at(t) or 0), abs(T.at(t) or 0))
            sq = lambda t: (int(g1.at(t) or 0) >> SQUARE) & 1
            starts = [t for t, v in m.events if v.startswith('launch spot: start')]
            for t, v in m.events:
                if not v.startswith('launch spot: sticks moved early'):
                    continue
                s0 = max([s for s in starts if s <= t], default=None)
                if s0 is None:
                    continue
                n += 1
                delays.append(t - s0)
                b = mag(s0 - 0.05)
                mags.append(b)
                moving += b > 0.2
                c = None
                for k in range(60):
                    tt = s0 + k * 0.02
                    if all(mag(tt + j * 0.05) <= 0.2 for j in range(6)):
                        c = k * 0.02
                        break
                if c is not None:
                    cs.append(c)
                    cent25 += c <= 0.25
                h = 0
                while sq(t + h) and h < 10:
                    h += 0.05
                helds.append(h)
                nxt = [s for s in starts if s > t + 0.05]
                rep += bool(nxt and nxt[0] - t < 3)
        if not n:
            continue
        r = dict(robot=team, cancels=n, moving_at_press=moving, cancel_delay=round(st.median(delays), 2),
                 centred_median=round(st.median(cs), 2) if cs else None, centred_025=cent25, held_median=round(st.median(helds), 2),
                 held_1s=sum(1 for h in helds if h >= 1.0), repress_3s=rep, stick_mag=round(st.median(mags), 2))
        for k, f in (('moving_of', 'moving_at_press'), ('centred_of', 'centred_025'), ('held_of', 'held_1s'), ('repress_of', 'repress_3s')):
            r[k] = '%d of %d' % (r[f], n)
        out.append(r)
    out.sort(key=lambda r: -int(r['robot']))
    return out


def square_issues(ms_by_team, robots):
    """square_issues: auto-drive presses and why they did not run (the press chart's counts).

    presses, sticks_cancelled and centre_refused are counts from the events; dropout_matches is
    the matches with a `Pinpoint lost` line. The Belding sheet's other fields (how soon the
    Pinpoint came back, refusals a later build would allow) were read by hand and are not made here.
    """
    out = []
    for team, ms in ms_by_team.items():
        rb = next(r for r in robots if r['robot'] == team)
        out.append(dict(robot=team, presses=rb['spot_presses'], sticks_cancelled=rb['cancelled_by_sticks'],
                        dropout_matches=sum(1 for m in ms if any(e.startswith('Pinpoint lost') for _, e in m.events)),
                        centre_refused=rb['refused_other_half']))
    return out
