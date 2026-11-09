"""FLOWER picks (issue #507) and floor pickup (issue #508).

FLOWER pick, from the `FLOWER pick:` events (opmodes/GrabRoutine):
- an attempt is a `start` or a `refused` line; Auto when the start line says `Auto`.
- reached: the IN phase ended (`IN x s (...), then DWELL`).
- phases: Auto from the `done` line (line-up, prep, in, dwell, back); TeleOp from the phase lines,
  dwell = its own `DWELL x s` or `let go in DWELL, x s in` line, else IN's end to the stop, back 0 (TeleOp picks are cut by the sticks), pick time = the
  stop line's "after x s". Means over the picks that reached a FLOWER.
- TeleOp presses that did not reach: `(sticks moved)` -> stick, `(released)` -> early, refused.
- pieces: `lane: X in` events from the pick's start to its end + 0.5 s (sensor_pieces). Auto's
  `pieces` is reconciled per pick with the next volley (README, "Auto pieces per FLOWER pick").

Floor piece (#508): a `lane: X in` or `arrived with the lane counted full` event in TeleOp
outside every FLOWER pick window. Tiles use the robot's front (centre + 9 in along heading), red
view (blue turned half a turn about the field centre), column A = x 0, row 1 = y 0; a point past
a wall counts in the edge tile.
"""
import math
import re
import statistics as st

from .logs import R

FIELD = R.FIELD_SIZE_IN   # FieldFrame.FIELD_SIZE_INCHES, as tools/pull-logs/read_logs.py states it
FRONT_IN = 9.0
IN_RE = re.compile(r'lane: \w+ in\b')
OVF = 'arrived with the lane counted full'


def picks(m):
    ev = [(t, e) for t, e in m.events if e.startswith('FLOWER pick:')]
    out, i = [], 0
    while i < len(ev):
        t, e = ev[i]
        if e.startswith('FLOWER pick: refused'):
            out.append(dict(t=t, end=t, auto=False, refused=True, reached=False, how='refused'))
            i += 1
            continue
        if not e.startswith('FLOWER pick: start'):
            i += 1
            continue
        p = dict(t=t, auto=', Auto,' in e, refused=False, reached=False, how=None, line_up=0.0, prep=0.0,
                 push=None, dwell=None, back=0.0, total=None, end=None, t_in_end=None)
        j = i + 1
        while j < len(ev) and not ev[j][1].startswith(('FLOWER pick: start', 'FLOWER pick: refused')):
            tj, ej = ev[j]
            mm = re.search(r'LINE_UP ([\d.]+) s, then', ej)
            if mm:
                p['line_up'] = float(mm.group(1))
            mm = re.search(r'PREP ([\d.]+) s, then', ej)
            if mm:
                p['prep'] = float(mm.group(1))
            mm = re.search(r'IN ([\d.]+) s \(.*?\), then DWELL', ej)
            if mm:
                p['push'], p['reached'], p['t_in_end'] = float(mm.group(1)), True, tj
            mm = re.search(r'DWELL ([\d.]+) s \(.*?\), then BACK', ej)
            if mm:
                p['dwell_line'] = float(mm.group(1))
            mm = re.search(r'let go in DWELL, ([\d.]+) s in', ej)
            if mm:
                p['dwell_line'] = float(mm.group(1))
            mm = re.search(r'done, .*Last FLOWER pick: ([\d.]+) s · line-up ([\d.]+), prep ([\d.]+), in ([\d.]+).*dwell ([\d.]+), back ([\d.]+)', ej)
            if mm:
                p.update(total=float(mm.group(1)), line_up=float(mm.group(2)), prep=float(mm.group(3)),
                         push=float(mm.group(4)), dwell=float(mm.group(5)), back=float(mm.group(6)), end=tj, how='done')
            mm = re.search(r'stopped in (\w+) after ([\d.]+) s \((.*?)\)', ej)
            if mm:
                p.update(total=float(mm.group(2)), end=tj, how=mm.group(3))
                if p['t_in_end'] is not None:
                    # The dwell ended at its own line (full, time, or let go), else at the stop.
                    p['dwell'] = p.get('dwell_line', tj - p['t_in_end'])
            j += 1
        if p['end'] is None:
            p['end'] = p['t'] + 4
        lo, hi = p['t'], p['end'] + 0.5
        p['sensor_in'] = sum(1 for tt, x in m.events if lo <= tt <= hi and IN_RE.match(x))
        p['sensor_ovf'] = sum(1 for tt, x in m.events if lo <= tt <= hi and OVF in x)
        out.append(p)
        i = j
    return out


def reconcile(m, ps, vols):
    """Auto pieces per pick: the larger of the sensor's arrivals (with `counted full`) and the next
    volley's launches less what was still in the lane, capped by the lane's space. The lane starts
    with 4 preloads and the first volley launches all 4 (the drive team loads 4 and every Auto's
    first volley launched them at Belding); after that it follows the #506 detector's counts."""
    vs = sorted((v['open_s'], v['new']) for v in vols if v['log'] == m.log and v['phase'] == 'auto')
    lane, out = 4, []
    for k, p in enumerate([p for p in ps if p['auto'] and not p['refused']]):
        prev = [v for v in vs if v[0] < p['t']]
        nxt = [v for v in vs if v[0] > p['t']]
        if k == 0 and prev:
            lane = 0                       # the preload volley launched all 4
        else:
            lane = max(0, lane - (prev[-1][1] if prev else 0))
        sens = p['sensor_in'] + p['sensor_ovf']
        nl = nxt[0][1] if nxt else None
        est = max(0, min(max(sens, (nl - lane) if nl is not None else 0), 4 - lane))
        out.append(est)
        lane = min(4, lane + est)
    return out


def mean(xs, nd=2):
    xs = [x for x in xs if x is not None]
    return round(sum(xs) / len(xs), nd) if xs else None


def flower(ms_by_team, vols):
    rows = []
    for team, ms in ms_by_team.items():
        A, T, recon = [], [], []
        for m in ms:
            ps = picks(m)
            A += [p for p in ps if p['auto']]
            T += [p for p in ps if not p['auto'] and (p['refused'] or (m.tele_init is not None and p['t'] >= m.tele_init))]
            recon += reconcile(m, ps, [v for v in vols if v['team'] == team])
        for mode, P in (('Auto', A), ('TeleOp', T)):
            if not P:
                continue
            R = [p for p in P if p['reached']]
            r = dict(robot=team, mode=mode, attempts=len(P), reached=len(R))
            if mode == 'Auto':
                r['pieces'] = sum(recon)
                r['per_pick'] = round(sum(recon) / len(R), 3) if R else None
                r['pick_s'] = mean([p['total'] for p in R])
            else:
                r['pieces'] = sum(p['sensor_in'] for p in R)
                r['per_pick'] = None
                r['pick_s'] = mean([p['total'] for p in R])
            r.update(line_up=mean([p['line_up'] for p in R]), prep=mean([p['prep'] for p in R]),
                     push=mean([p['push'] for p in R]), dwell=mean([p['dwell'] for p in R]),
                     back=mean([p['back'] for p in R]) if mode == 'Auto' else 0)
            if mode == 'Auto':
                r['sensor_pieces'] = sum(p['sensor_in'] for p in R)
            else:
                nr = [p for p in P if not p['reached']]
                r.update(stick=sum(1 for p in nr if p['how'] == 'sticks moved'),
                         early=sum(1 for p in nr if p['how'] == 'released'),
                         refused=sum(1 for p in nr if p['refused']), reached_of='%d of %d' % (len(R), len(P)))
            rows.append(r)
    rows.sort(key=lambda r: (r['mode'] != 'Auto', -int(r['robot'])))
    return rows


def floor(ms_by_team, V, cfg):
    fp, tiles = [], []
    for team, ms in ms_by_team.items():
        floor_n = flower_n = 0
        first, between, to_full, coll, tele_s = [], [], [], 0.0, 0.0
        grid = [[0] * 6 for _ in range(6)]
        nmatch = 0
        for m in ms:
            te = m.tele_start
            if te is None:
                continue
            nmatch += 1
            ps = picks(m)
            win = [(p['t'], p['end'] + 0.5) for p in ps if not p['refused']]
            arr = [t for t, e in m.events if t >= te and (IN_RE.match(e) or OVF in e)]
            fl = [t for t in arr if not any(a <= t <= b for a, b in win)]
            floor_n += len(fl)
            flower_n += len(arr) - len(fl)
            vs = [v for v in V if v['log'] == m.log and v['robot'] == team]
            ends = [te + v['first'] + v['launch'] for v in vs]
            starts = [te + v['first'] for v in vs]
            full = [t for t, e in m.events if e.startswith('Lane: FULL') and t >= te]
            end = m.log_end
            tele_s += end - te
            for k, e0 in enumerate(ends):
                nxt = starts[k + 1] if k + 1 < len(starts) else end
                f = [t for t in fl if e0 < t < nxt]
                if f:
                    first.append(f[0] - e0)
                between += [b - a for a, b in zip(f, f[1:])]
                fu = next((t for t in full if e0 < t < nxt), None)
                coll += (fu if fu else nxt) - e0
                if fu and not any(e0 < a < fu for a, _ in win):
                    to_full.append(fu - e0)
            X, Y, H = m['/Odometry/PedroInches/X'], m['/Odometry/PedroInches/Y'], m['/Odometry/PedroInches/HeadingDeg']
            excl = [tuple(x) for x in cfg.get('pose_excluded', {}).get(team, {}).get(m.log, [])]
            blue = (m.alliance or m.log_alliance) == 'Blue'
            for t in fl:
                if any(a <= t <= b for a, b in excl):
                    continue
                x, y, h = X.at(t), Y.at(t), H.at(t)
                if None in (x, y, h):
                    continue
                x += FRONT_IN * math.cos(math.radians(h))
                y += FRONT_IN * math.sin(math.radians(h))
                if blue:
                    x, y = FIELD - x, FIELD - y
                # A front point past a wall (the robot against it) counts in the edge tile.
                c, r = min(5, max(0, int(x // (FIELD / 6)))), min(5, max(0, int(y // (FIELD / 6))))
                grid[r][c] += 1
        if not nmatch:
            continue
        md = lambda xs: round(st.median(xs), 1) if xs else None
        fp.append(dict(robot=team, per_min=round(floor_n / (2.0 * nmatch), 1), first_piece_s=md(first), between_s=md(between),
                       to_full_s=md(to_full), collecting_share=round(coll / tele_s, 2) if tele_s else None,
                       floor_teleop=floor_n, flower_teleop=flower_n,
                       floor_share=round(floor_n / (floor_n + flower_n), 2) if floor_n + flower_n else None))
        for row in range(6, 0, -1):
            for c, col in enumerate('ABCDEF'):
                tiles.append(dict(robot=team, col=col, row=row, n=grid[row - 1][c]))
    fp.sort(key=lambda r: -int(r['robot']))
    return {'floor_pickup': fp, 'floor_tiles': tiles}
