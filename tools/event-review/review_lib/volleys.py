"""TeleOp volleys from the robot's own shot counter, and what led up to each one.

This is the table the cycle, launch and first-volley numbers share. Rules, as at Belding:
- A volley is a run of counted shots (/Launcher/Shots, the counter the robot ran) less than
  1.5 s apart, in TeleOp (Driver Station teleop to the last battery sample).
- Auto-drive (Square): a `launch spot: gate open` event 0-3 s before the first shot. Its press is
  the last Square press at or before the `arrived` event 0-3 s before the gate opened.
  Manual (operator FIRE, Circle on gamepad 2): a Circle press 0-6 s before the first shot.
  Neither: 'other' (no press to time from).
- Times in a record are seconds from TeleOp start (`first`, `press`) or durations.
- Collecting is measured from the previous volley's last shot (the TeleOp start for the first):
  first `lane: X in` arrival, first `Lane: FULL`, last arrival, all before the press.
- Moving again: the first time after the first shot that odometry speed stays above 8 in/s for
  0.3 s. The launch is press to moving again; the next cycle's "to first piece" counts from it.
"""
import statistics as st

from .shots import old_shots

SQUARE, CIRCLE = 2, 1           # button bits: gamepad 1 Square (auto-drive), gamepad 2 Circle (FIRE)
VOLLEY_GAP_S = 1.5
MOVING_IN_S, MOVING_FOR_S = 8.0, 0.3


def downs(series, bit):
    out, prev = [], 0
    for t, v in series:
        b = (int(v) >> bit) & 1
        if b and not prev:
            out.append(t)
        prev = b
    return out


def moving_again(sp, t0):
    start = None
    for t, v in sp:
        if t < t0:
            continue
        if v > MOVING_IN_S:
            if start is None:
                start = t
            if t - start >= MOVING_FOR_S:
                return start
        else:
            start = None
    return None


def build(m):
    """The volley records for one match (see the module comment)."""
    te, end = m.tele_start, m.log_end
    if te is None:
        return []
    sh = m['/Launcher/Shots']
    balls, last = [], sh.at(te) or 0
    for t, v in sh:
        if te < t <= end and v > last:
            balls.append(t)
            last = v
    vol = []
    for t in balls:
        if vol and t - vol[-1][-1] <= VOLLEY_GAP_S:
            vol[-1].append(t)
        else:
            vol.append([t])
    sq = downs(m['/DriverStation/Joystick0/ButtonValues'], SQUARE)
    fire = downs(m['/DriverStation/Joystick1/ButtonValues'], CIRCLE)
    gates = [t for t, e in m.events if e.startswith('launch spot: gate open')]
    arrives = [t for t, e in m.events if e.startswith('launch spot: arrived')]
    out, prev_end = [], te
    for k, v in enumerate(vol):
        f = v[0]
        g = max([x for x in gates if f - 3 <= x <= f], default=None)
        row = {'robot': m.team, 'match': m.name, 'log': m.log, 'balls': len(v), 'first': f - te}
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
        out.append(row)
        prev_end = v[-1]
    _collecting(m, out)
    _launch_span(m, out)
    return out


def _collecting(m, vs):
    te = m.tele_start
    full = [t - te for t, e in m.events if e.startswith('Lane: FULL')]
    ins = [t - te for t, e in m.events if e.startswith('lane: ') and ' in, ' in e]
    pres = m['/Lane/Present']
    prev = 0.0
    for v in vs:
        if v['first_volley']:
            prev = 0.0
        if v.get('away') is None:
            prev = v['first'] + v['launch']
            continue
        start = prev
        press = v['first'] - v['feed'] - v['settle'] - v['drive']
        fl = [x for x in full if start < x <= press]
        fi = [x for x in ins if start < x <= press]
        v['press'] = press
        v['full_before_press'] = bool(fl)
        v['to_first_piece'] = (fi[0] - start) if fi else None
        v['collect'] = (fl[0] - start) if fl else None
        v['return'] = (press - fl[0]) if fl else None
        v['n_in'] = len(fi)
        v['last_in'] = (fi[-1] - start) if fi else None
        v['last_to_launch'] = (press - fi[-1]) if fi else None
        v['present_at_press'] = bool(pres.at(press + te))
        prev = v['first'] + v['launch']


def _launch_span(m, vs):
    te = m.tele_start
    sp = [(t - te, x) for t, x in m['/Drive/OdometrySpeedInPerSec']]
    ins = [t - te for t, e in m.events if e.startswith('lane: ') and ' in, ' in e]
    prev = None
    for v in vs:
        if 'feed' not in v:
            v['launch_span'] = None
            continue
        press = v['first'] - v['feed'] - v['settle'] - v['drive']
        mv = moving_again(sp, v['first'])
        v['press'] = press
        v['moving_again'] = mv
        v['launch_span'] = (mv - press) if mv else None
        if v['first_volley']:
            prev = None
        start = prev
        if start is not None and v.get('away') is not None:
            fi = [x for x in ins if start < x <= press]
            v['tfp2'] = (fi[0] - start) if fi else None
            v['cycle2'] = press - start
        else:
            v['tfp2'] = None
            v['cycle2'] = None
        prev = mv


def md(xs, nd=2):
    xs = [x for x in xs if x is not None]
    return round(st.median(xs), nd) if xs else None


def nn(xs):
    return len([x for x in xs if x is not None])
