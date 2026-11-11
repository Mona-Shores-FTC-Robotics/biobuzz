"""Where the time goes in a TeleOp launch (Belding, 7 Nov 2026): doc/launch-speed-and-controls.md.

  python3 launch_time.py <logs root with 19429/matches and 20245/matches> out.json

A launch attempt = a chain of intent: driver Square (gamepad 1), driver Circle (gamepad 1),
operator FIRE (gamepad 2 Circle), merged when less than MERGE_S apart, plus the launcher spun up
before the chain (the operator's toggle) if it stayed spinning into it.
For each chain: did a piece leave, and when; and every loop from the first intent to the feed gate
opening is given to ONE wait, the thing the feed was waiting on in that loop.
"""
import collections
import json
import os
import re
import statistics as st
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', '..', '..'))
from review_lib import logs as LG  # noqa
LG.KEYS += ['/Controls/AimErrorDeg', '/Odometry/Referenced']
from review_lib import shots as SH  # noqa

A, B, X, Y = 0, 1, 2, 3   # Cross, Circle, Square, Triangle
MERGE_S = 1.5
ROOT = sys.argv[1]
NAMES = {}
for ev in json.load(open(os.path.join(HERE, '..', '..', '..', '..', 'advantagescope', 'replay', 'matches.json')))['events']:
    for r in ev['matches']:
        NAMES[(str(r['team']), r['log'].split('/')[-1][:19])] = r['match']


def holds(series, bit, a, b):
    out, down, prev = [], None, 0
    for t, v in series:
        x = (int(v) >> bit) & 1
        if x and not prev:
            down = t
        if prev and not x and down is not None:
            out.append((down, t))
            down = None
        prev = x
    if down is not None:
        out.append((down, series.t[-1]))
    return [(s, e) for s, e in out if e >= a and s <= b]


def analyse(m):
    te, end = m.tele_play or m.tele_start, m.tele_stop or m.log_end
    if te is None:
        return []
    g1, g2 = m['/DriverStation/Joystick0/ButtonValues'], m['/DriverStation/Joystick1/ButtonValues']
    tgt, ats, feed, aim = m['/Launcher/TargetTicksPerSecond'], m['/Launcher/AtSpeed'], m['/FeedGate/Feeding'], m['/Controls/AimErrorDeg']
    L, Rr = m['/Launcher/LeftTicksPerSecond'], m['/Launcher/RightTicksPerSecond']
    sq = holds(g1, X, te, end)
    ci = holds(g1, B, te, end)
    fi = holds(g2, B, te, end)
    iv = sorted([(s, e, 'Square') for s, e in sq] + [(s, e, 'Circle') for s, e in ci] + [(s, e, 'FIRE') for s, e in fi])
    chains = []
    for s, e, k in iv:
        if s < te:
            continue
        if chains and s - chains[-1]['end'] <= MERGE_S:
            chains[-1]['end'] = max(chains[-1]['end'], e)
            chains[-1]['parts'].append((s, e, k))
        else:
            chains.append({'start': s, 'end': e, 'parts': [(s, e, k)]})
    # Square phases from the events
    ev = m.events
    arrived = [t for t, e in ev if e.startswith('launch spot: arrived')]
    refused = [t for t, e in ev if e.startswith('launch spot: refused')]
    early = [(t, e) for t, e in ev if e.startswith('launch spot:') and ' early after ' in e]
    vols = [v for v in SH.volleys(m) if v['open_s'] >= te]
    old = SH.old_shots(m)
    spin_edges = []   # launcher target 0 -> >0
    prev = 0
    for t, v in tgt:
        if v > 0 and not prev > 0:
            spin_edges.append(t)
        prev = v
    out = []
    for c in chains:
        s0, e0 = c['start'], c['end']
        kinds = [k for _, _, k in c['parts']]
        r = dict(team=m.team, match=NAMES.get((m.team, m.log), m.log), t=round(s0 - te, 1), first=kinds[0],
                 kinds=''.join(sorted(set(k[0] for k in kinds))), presses=len(kinds), held=round(e0 - s0, 2))
        # pre-spin: the launcher already asked for speed at the first press, continuously since when
        r['prespun'] = bool((tgt.at(s0 - 0.03) or 0) > 0)
        r['at_speed_at_press'] = bool(ats.at(s0 - 0.03))
        lead = 0.0
        if r['prespun']:
            edge = max([x for x in spin_edges if x <= s0], default=s0)
            lead = s0 - edge
        r['prespin_lead'] = round(lead, 1)
        r['wheel_at_press'] = round(((abs(L.at(s0) or 0)) + abs(Rr.at(s0) or 0)) / 2)
        # gate openings in the chain, and the first piece
        ops = [v for v in vols if s0 - 0.05 <= v['open_s'] <= e0 + 0.3]
        r['gate'] = round(ops[0]['open_s'] - s0, 2) if ops else None
        first = None
        for v in ops:
            cand = [v['open_s'] + d for d in v['dip_start_s']]
            o = [t for t in old if v['open_s'] <= t <= v['open_s'] + v['open_dur_s'] + 0.3]
            if cand or o:
                first = min(cand + o)
                r['feed'] = round(first - v['open_s'], 2)
                r['gate_used'] = round(v['open_s'] - s0, 2)
                break
        r['first_piece'] = round(first - s0, 2) if first else None
        r['pieces'] = sum(max(v['new'], v['old']) for v in ops)
        r['refused'] = sum(1 for t in refused if s0 - 0.05 <= t <= e0)
        r['early'] = sum(1 for t, _ in early if s0 - 0.05 <= t <= e0 + 0.1)
        r['sticks_early'] = sum(1 for t, e in early if s0 - 0.05 <= t <= e0 + 0.1 and 'sticks moved early' in e)
        r['letgo_early'] = sum(1 for t, e in early if s0 - 0.05 <= t <= e0 + 0.1 and 'let go early' in e)
        r['referenced'] = bool(m['/Odometry/Referenced'].at(s0)) if len(m['/Odometry/Referenced']) else None
        # per-loop attribution from s0 to the gate opening used (or to the chain's end if none opened)
        stop = (s0 + r['gate_used']) if first else (ops[0]['open_s'] if ops else e0)
        w = collections.Counter()
        ts = [t for t, _ in ats.between(s0, stop)]
        reached = r['at_speed_at_press']
        for a, b in zip(ts, ts[1:] + [stop]):
            dt = b - a
            if dt <= 0:
                continue
            speed = bool(ats.at(a))
            spinning = (tgt.at(a) or 0) > 0
            if not spinning:
                reached = False
            if speed:
                reached = True
            fire = any(s <= a < e for s, e in fi)
            square = any(s <= a < e for s, e in sq)
            circle = any(s <= a < e for s, e in ci)
            arr = any(s <= x <= a for s, e in sq if s <= a < e for x in arrived)
            ae = aim.at(a)
            sqh = next(((s, e) for s, e in sq if s <= a < e), None)
            arrives = sqh is not None and any(sqh[0] <= x <= sqh[1] for x in arrived)
            if square and not arrives:
                w['Square press that never arrived'] += dt
            elif square and not arr:
                w['drive to the spot'] += dt
                if not speed:
                    w['(of which launcher not yet at speed)'] += dt
            elif square:
                w['spin-up' if (not speed and not reached) else 'speed gate (re-arm)' if not speed else 'settle check at the spot'] += dt
            elif fire:
                if not speed:
                    w['spin-up' if not reached else 'speed gate (re-arm)'] += dt
                elif circle and ae is not None and ae == ae and abs(ae) > 3.0:
                    w['aim gate'] += dt
                else:
                    w['FIRE held, gate not open (other)'] += dt
            elif circle:
                w['driver aiming (Circle), no FIRE yet'] += dt
                if not speed:
                    w['(of which launcher not yet at speed)'] += dt
            else:
                w['between presses'] += dt
        r['waits'] = {k: round(v, 2) for k, v in w.items()}
        out.append(r)
    return out


def main():
    rows = []
    for team in ('19429', '20245'):
        for p in LG.find_logs(ROOT, team):
            m = LG.Match(p, team)
            rows += analyse(m)
    json.dump(rows, open(sys.argv[2], 'w'), indent=0)
    order = ['Square press that never arrived', 'between presses', 'drive to the spot',
             'driver aiming (Circle), no FIRE yet', 'spin-up', 'speed gate (re-arm)', 'settle check at the spot',
             'aim gate', 'FIRE held, gate not open (other)', 'feed']
    for team in ('19429', '20245'):
        t = [r for r in rows if r['team'] == team]
        ok = [r for r in t if r['first_piece'] is not None]
        # A wheel the code shut off, or a first piece 2.5 s after the gate: a fault, not a wait.
        fault = [r for r in ok if r['waits'].get('spin-up', 0) > 3 or (r.get('feed') or 0) > 2.5]
        ok = [r for r in ok if r not in fault]
        fp = sorted(r['first_piece'] for r in ok)
        print('%s: %d attempts, %d launched (%d faults set aside); intent to first piece median %.2f s, quartiles %.2f to %.2f'
              % (team, len(t), len(ok) + len(fault), len(fault), st.median(fp), fp[len(fp) // 4], fp[3 * len(fp) // 4]))
        tot, dom = collections.Counter(), collections.Counter()
        for r in ok:
            w = dict(r['waits'])
            w.pop('(of which launcher not yet at speed)', None)
            w['feed'] = r['feed']
            tot.update(w)
            dom[max(w, key=w.get)] += 1
        for k in order:
            print('  %-38s %.2f s per launch, biggest wait in %d' % (k, tot[k] / len(ok), dom[k]))


main()
