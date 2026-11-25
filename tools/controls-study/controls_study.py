#!/usr/bin/env python3
"""Which buttons the driver and the operator pressed in each match, and what each press did.

  python3 tools/controls-study/controls_study.py report LOG.wpilog.gz [...]     per match: counts, fires,
                                                                               spin-ups, trims, idle, cycles
  python3 tools/controls-study/controls_study.py timeline LOG.wpilog.gz         every press in TeleOp, in order
  python3 tools/controls-study/controls_study.py json OUT.json LOG.wpilog.gz [...]   everything, for a page

For coaching the drive team: what each pad did, not who held it (the log does not name the student).

Where the presses come from. Every loop the match log writes each gamepad's state whenever it changed
(logging/GamepadLog: /DriverStation/Joystick0 = gamepad 1, the driver; Joystick1 = gamepad 2, the
operator). Buttons are an SDL bit mask, sticks and triggers six axes. So every button is observable,
mapped or not, and a press is a rising edge. A trigger counts as pressed past 0.3 (controls.json's
triggerPressed); a stick counts as moved past STICK_MOVED.

Which action a press was. A press is mapped to a ControlScheme Action with the layout of the build that
ran (the log's GitCommit): Action.java (which pad) and ControlScheme.java (which input) are read from that
commit with `git show`, then the match TeleOp's rules from ControlScheme.layout(Input, boolean, boolean)
are applied: BENCH_ONLY out, FINE_TRIM on the operator's Circle where the build has MATCH_ONLY, the turret's
controls out when the turret is locked forward, LAUNCHER_TRIM_RESET on D-pad down where the build has
LOCKED_TURRET_ONLY. Belding's build (7 Nov 2026) is not Fruitport's: there the operator's Circle was FIRE
and the driver's Circle the chassis aim. The clone must hold the commit: `git fetch --unshallow origin`.

What the press did comes from the TeleOp's own lines in /Events (fire lines with their gates, launch spot,
FLOWER pick, GARDEN gather, launcher spin-up/idle, trims, the resets' rumble lines) and from the series
(/FeedGate/Feeding, /Launcher/Shots, /Launcher/TargetTicksPerSecond, /Controls/TrimDeg and
LauncherTrimTps, /FlowerStick/Out). Cycles are read_logs.py's (cycle_rows), with every fire press as a press.
"""
import json
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', 'pull-logs'))
import read_logs as R  # noqa: E402

REPO = os.path.abspath(os.path.join(HERE, '..', '..'))
CONTROLS = 'TeamCode/src/main/java/org/firstinspires/ftc/teamcode/controls/'

# SDL gamepad buttons, as logging/GamepadLog indexes them, by the names ControlScheme's Input uses.
BITS = {0: 'CROSS', 1: 'CIRCLE', 2: 'SQUARE', 3: 'TRIANGLE', 4: 'SHARE', 5: 'PS', 6: 'OPTIONS',
        7: 'L3', 8: 'R3', 9: 'L1', 10: 'R1', 11: 'DPAD_UP', 12: 'DPAD_DOWN', 13: 'DPAD_LEFT',
        14: 'DPAD_RIGHT', 20: 'TOUCHPAD'}
TRIGGER_PRESSED = 0.3
STICK_MOVED = 0.15
HOLD_S = 0.5            # BasicDriveTeleOp.RESET_HOLD_MS: the resets and the turret zero
PADS = ('DRIVER', 'OPERATOR')
# Not in ControlScheme, but known: Display cycles the DS pages on gamepad 1's Share.
EXTRA = {('DRIVER', 'SHARE'): 'DS_PAGE'}
FIRE_ACTIONS = {('DRIVER', 'CHASSIS_AIM'), ('OPERATOR', 'FIRE'), ('DRIVER', 'LAUNCH_SPOT')}


# ---- the layout that ran ------------------------------------------------------------------------

_layout_cache = {}


def _git_show(commit, path):
    try:
        return subprocess.run(['git', '-C', REPO, 'show', '%s:%s' % (commit, path)], check=True,
                              capture_output=True, text=True).stdout
    except (subprocess.CalledProcessError, FileNotFoundError):
        return None


def _enum_set(src, name):
    m = re.search(name + r'\s*=\s*Collections\.unmodifiableSet\(EnumSet\.of\(([^;]*?)\)\);', src, re.S)
    return set(re.findall(r'Action\.([A-Z_]+)', m.group(1))) if m else set()


def layout(commit, locked, launch_spot=True):
    """({(pad, input): action}, {action: label}, note) for the match TeleOp of that build."""
    key = (commit, locked, launch_spot)
    if key in _layout_cache:
        return _layout_cache[key]
    note = 'layout from %s' % commit
    action, scheme = _git_show(commit, CONTROLS + 'Action.java'), _git_show(commit, CONTROLS + 'ControlScheme.java')
    if action is None or scheme is None:
        note = 'commit %s not in this clone (git fetch --unshallow origin): layout from the working tree' % commit
        action = open(os.path.join(REPO, CONTROLS, 'Action.java')).read()
        scheme = open(os.path.join(REPO, CONTROLS, 'ControlScheme.java')).read()
    pads, labels = {}, {}
    for name, pad, label in re.findall(r'^\s+([A-Z_]+)\(Pad\.(DRIVER|OPERATOR),\s*"((?:[^"\\]|\\.)*)"', action, re.M):
        pads[name] = pad
        labels[name] = label.encode().decode('unicode_escape').replace(' ', ' ')
    default_pick = re.search(r'DEFAULT_FLOWER_PICK\s*=\s*Input\.([A-Z_]+)', scheme).group(1)
    base = scheme[scheme.index('public static Map<Action, Input> layout(Input flowerPick) {'):]
    base = base[:base.index('\n    }\n')]
    amap = {}
    for act, inp in re.findall(r'map\.put\(Action\.([A-Z_]+),\s*(?:Input\.)?([A-Za-z_0-9]+)\)', base):
        if inp == 'DEFAULT_FLOWER_PICK':
            inp = default_pick
        if inp.isupper():
            amap[act] = inp
    for act in _enum_set(scheme, 'BENCH_ONLY') | _enum_set(scheme, 'NOT_BOUND'):
        amap.pop(act, None)
    if 'MATCH_ONLY' in scheme:
        amap['FINE_TRIM'] = 'CIRCLE'
    if locked:
        for act in _enum_set(scheme, 'TURRET_ACTIONS'):
            amap.pop(act, None)
        if 'LOCKED_TURRET_ONLY' in scheme:
            amap['LAUNCHER_TRIM_RESET'] = 'DPAD_DOWN'
    if not launch_spot:
        amap.pop('LAUNCH_SPOT', None)
    by_input = {(pads[a], i): a for a, i in amap.items() if a in pads}
    by_input.update(EXTRA)
    labels['DS_PAGE'] = 'DS page (Share; Display, not ControlScheme)'
    _layout_cache[key] = (by_input, labels, note)
    return _layout_cache[key]


# ---- presses --------------------------------------------------------------------------------------

def pad_intervals(bits, axes, a, b):
    """{input: [(down, up)]} between a and b, from one pad's logged button mask and axes."""
    states = sorted([(t, 'b', v) for t, v in bits] + [(t, 'a', v) for t, v in axes if isinstance(v, tuple)])
    cur_b, cur_a = 0, (0.0,) * 6
    down, out = {}, {}

    def held(bm, ax):
        h = {name for bit, name in BITS.items() if bm >> bit & 1}
        if len(ax) >= 6:
            if ax[4] > TRIGGER_PRESSED: h.add('L2')
            if ax[5] > TRIGGER_PRESSED: h.add('R2')
            if (ax[0] ** 2 + ax[1] ** 2) ** 0.5 > STICK_MOVED: h.add('LEFT_STICK')
            if abs(ax[2]) > STICK_MOVED: h.add('RIGHT_STICK_X')
            if abs(ax[3]) > STICK_MOVED and abs(ax[3]) > abs(ax[2]): h.add('RIGHT_STICK_Y')
        return h

    prev = set()
    for t, kind, v in states:
        if kind == 'b':
            cur_b = int(v)
        else:
            cur_a = v
        if t > b:
            break
        now = held(cur_b, cur_a)
        tt = max(t, a)
        for name in now - prev:
            down[name] = tt
        for name in prev - now:
            if down.get(name) is not None:
                if t >= a:
                    out.setdefault(name, []).append((down[name], tt))
                down[name] = None
        prev = now
    for name in prev:
        if down.get(name) is not None:
            out.setdefault(name, []).append((down[name], b))
    return out


# ---- outcomes ---------------------------------------------------------------------------------------

_FIRE_LINE = re.compile(r"^fire(?: \((?P<who>[^)]*)\))?: launcher (?P<speed>\d+)/(?P<target>\d+) ticks/s(?P<idle> \(idle\))?, "
                        r"aim (?P<aim>[^ ]+)(?: \((?P<src>[^)]*)\))?.*?turret (?P<turret>[A-Z]+).*?gates (?P<gates>.*?) → (?P<outcome>.*)$")
_GATE = re.compile(r'(speed|aim|arc|nectar|NECTAR)\s+(BLOCK|PASS)', re.I)


def _near(events, t, prefix, within=0.35, before=0.05):
    for te, e in events:
        if t - before <= te <= t + within and e.startswith(prefix):
            return te, e
    return None


def _changes(series, a, b):
    out, last = [], None
    for t, v in series:
        if t > b:
            break
        if t < a:
            last = v
            continue
        if v != last:
            out.append((t, last, v))
            last = v
    return out


def _gate_open_after(feeding, t0, t1):
    for t, v in feeding:
        if t < t0:
            continue
        if t > t1:
            return None
        if v:
            return t
    return None


def _feeding_at(feeding, t):
    return bool(R.at(feeding, t))


def study(path):
    d = R.read(path)
    md = lambda k: (d.get('/RealMetadata/' + k) or [(0, None)])[0][1]  # noqa: E731
    ev = [(t, str(e)) for t, e in d.get('/Events', [])]
    w = R._teleop_window(d)
    res = {'log': os.path.basename(path), 'commit': md('GitCommit'), 'build': md('Build'),
           'config': md('RobotConfig')}
    if w is None:
        res['error'] = 'no TeleOp PLAY'
        return res
    a, b = w
    tev = [(t, e) for t, e in ev if a - 0.05 <= t <= b]
    lf = md('Tunable/TurretSubsystem.lockedForward')
    locked = (lf == 'true') if lf in ('true', 'false') else not any(e.startswith('turret at PLAY: AIM') for _, e in ev)
    ls = md('Tunable/TeleOpConfig.launchSpot')
    by_input, labels, note = layout(res['commit'], locked, ls != 'false')
    res.update({'teleop_s': round(b - a, 1), 'turret_locked_forward': locked, 'layout_note': note,
                'alliance': next((e.split()[1] for t, e in ev if e.startswith('PLAY: ') and 'alliance' in e), None)})

    presses = []   # dicts: pad, input, action, down, up
    for i, pad in enumerate(PADS):
        j = '/DriverStation/Joystick%d/' % i
        iv = pad_intervals(d.get(j + 'ButtonValues', []), d.get(j + 'AxisValues', []), a, b)
        for inp, spans in iv.items():
            act = by_input.get((pad, inp))
            if inp in ('LEFT_STICK', 'RIGHT_STICK_X', 'RIGHT_STICK_Y'):
                act = act or 'UNBOUND'
            for down, up in spans:
                presses.append({'pad': pad, 'input': inp, 'action': act or 'UNBOUND', 'down': down, 'up': up})
    presses.sort(key=lambda p: p['down'])

    feeding = d.get('/FeedGate/Feeding', [])
    shots = d.get('/Launcher/Shots', [])
    target = d.get('/Launcher/TargetTicksPerSecond', [])
    atspeed = d.get('/Launcher/AtSpeed', [])
    stick_out = d.get('/FlowerStick/Out', [])

    # Per press: what it did.
    for p in presses:
        t, act, pad = p['down'], p['action'], p['pad']
        p['held'] = round(p['up'] - p['down'], 2)
        if (pad, act) in (('DRIVER', 'CHASSIS_AIM'), ('OPERATOR', 'FIRE')):
            line = None
            for te, e in tev:
                if t - 0.05 <= te <= t + 0.35 and _FIRE_LINE.match(e):
                    line = e
                    break
            m = _FIRE_LINE.match(line) if line else None
            p['launcher_spinning'] = bool(R.at(target, t - 0.02) or 0) > 0
            p['at_speed'] = bool(R.at(atspeed, t - 0.02))
            p['already_feeding'] = _feeding_at(feeding, t - 0.02)
            opened = None if p['already_feeding'] else _gate_open_after(feeding, t - 0.02, p['up'] + 0.05)
            p['gate_wait'] = None if opened is None else round(opened - t, 2)
            p['shots'] = int((R.at(shots, p['up'] + 0.4) or 0) - (R.at(shots, t) or 0))
            if m:
                g = dict((k.lower(), v) for k, v in _GATE.findall(m.group('gates')))
                p['fire_line'] = {'outcome': m.group('outcome'), 'gates': g, 'speed': int(m.group('speed')),
                                  'target': int(m.group('target')), 'idle': bool(m.group('idle')),
                                  'aim': m.group('aim'), 'aim_src': m.group('src'), 'turret': m.group('turret')}
            ref = [e for te, e in tev if t - 0.05 <= te <= p['up'] + 0.1 and
                   (e.startswith('fire: refused') or e.startswith('fire: held back') or e.startswith('fire: outside')
                    or e.startswith('arc turn: aim'))]
            if ref:
                p['notes'] = ref
            if act == 'CHASSIS_AIM' and locked:
                c = _near(tev, t, 'chassis aim: pressed')
                p['chassis_aim'] = bool(c)
        elif act == 'LAUNCH_SPOT':
            s = _near(tev, t, 'launch spot: ')
            end = [e for te, e in tev if t <= te <= p['up'] + 0.3 and re.match(r'launch spot: (let go|sticks moved|refused)', e)]
            p['spot'] = (s[1][len('launch spot: '):] if s else None)
            if end:
                p['spot_end'] = end[0][len('launch spot: '):]
            p['shots'] = int((R.at(shots, p['up'] + 0.6) or 0) - (R.at(shots, t) or 0))
            opened = _gate_open_after(feeding, t, p['up'] + 0.05)
            p['gate_wait'] = None if opened is None else round(opened - t, 2)
        elif act == 'LAUNCHER_TOGGLE':
            s = _near(tev, t, 'launcher: ')
            p['launcher'] = s[1][len('launcher: '):] if s else None
            p['spinning_after'] = (R.at(target, t + 0.3) or 0) > 0
        elif act in ('LAUNCHER_TRIM_UP', 'LAUNCHER_TRIM_DOWN', 'LAUNCHER_TRIM_RESET'):
            tr = d.get('/Controls/LauncherTrimTps', [])
            p['trim'] = (R.at(tr, t - 0.02), R.at(tr, t + 0.3))
        elif act in ('TRIM_LEFT', 'TRIM_RIGHT', 'TRIM_RESET'):
            tr = d.get('/Controls/TrimDeg', [])
            p['trim'] = (R.at(tr, t - 0.02), R.at(tr, t + 0.3))
        elif act == 'FLOWER_STICK':
            p['stick'] = (R.at(stick_out, t - 0.02), R.at(stick_out, t + 0.3))
        elif act in ('FLOWER_PICK', 'GARDEN_GATHER'):
            name = 'FLOWER pick: ' if act == 'FLOWER_PICK' else 'GARDEN gather: '
            s = _near(tev, t, name)
            p['started'] = s[1][len(name):] if s else None
            end = [e for te, e in tev if t <= te <= p['up'] + 0.3 and (name + 'Last') in e or (t <= te <= p['up'] + 0.3 and e.startswith(name + 'let go'))]
            if end:
                p['ended'] = end[0][len(name):]
        elif act in ('RESET_HEADING', 'RESET_LEFT_CORNER', 'RESET_RIGHT_CORNER', 'CAMERA_RELOCALIZE', 'TURRET_ZERO'):
            p['acted'] = p['held'] >= HOLD_S
            r = [e for te, e in tev if p['down'] <= te <= p['up'] + 0.1 and (e.startswith('rumble:') or e.startswith('pose:')
                 or e.startswith('gyro reset') or e.startswith('turret zeroed'))]
            if r:
                p['events'] = r
        elif act == 'TURRET_LOCK':
            s = _near(tev, t, 'turret ')
            p['mode'] = s[1] if s else None

    res['presses'] = presses
    res['summary'] = summarise(d, res, presses, tev, a, b, labels, by_input)
    return res


TURN_CORRECTING = 0.1    # |right stick X| above this is the driver steering
TRANSLATING = 0.5        # left stick magnitude above this is the driver driving
MOVING_IN_S = 3.0        # odometry speed above this is the robot moving
LONG_RUN_IN = 36.0


def heading_corrections(d, a, b):
    """How much the driver steered while driving: the seconds of TeleOp with the right stick past
    TURN_CORRECTING while the left stick is past TRANSLATING and the robot moves, as a share of the
    seconds translating; and, per run translating more than LONG_RUN_IN inches, the steering presses
    (the right stick crossing TURN_CORRECTING) in it. Read from the driver's logged axes, sampled at
    each odometry speed sample. The ceiling on what a heading lock could take over."""
    import bisect
    axes = [(t, v) for t, v in d.get('/DriverStation/Joystick0/AxisValues', []) if isinstance(v, tuple) and len(v) >= 6]
    ts = [t for t, _ in axes]
    speed = [(t, v) for t, v in d.get('/Drive/OdometrySpeedInPerSec', []) if a <= t <= b and isinstance(v, (int, float))]
    trans_s = turn_s = 0.0
    runs, cur = [], None
    prev_t = None
    for t, v in speed:
        dt = 0.0 if prev_t is None else min(t - prev_t, 0.1)
        prev_t = t
        i = bisect.bisect_right(ts, t) - 1
        ax = axes[i][1] if i >= 0 else (0.0,) * 6
        moving = abs(v) > MOVING_IN_S
        trans = moving and (ax[0] ** 2 + ax[1] ** 2) ** 0.5 > TRANSLATING
        turning = abs(ax[2]) > TURN_CORRECTING
        if trans:
            trans_s += dt
            if turning:
                turn_s += dt
            if cur is None:
                cur = {'start': t, 'in': 0.0, 'corrections': 0, 'turning': turning}
                if turning:
                    cur['corrections'] = 1
            else:
                if turning and not cur['turning']:
                    cur['corrections'] += 1
                cur['turning'] = turning
            cur['in'] += abs(v) * dt
        elif cur is not None:
            runs.append(cur)
            cur = None
    if cur is not None:
        runs.append(cur)
    long_runs = [r for r in runs if r['in'] > LONG_RUN_IN]
    return {'translating_s': round(trans_s, 1), 'turning_while_translating_s': round(turn_s, 1),
            'share_of_translating': round(turn_s / trans_s, 3) if trans_s else None,
            'share_of_teleop': round(turn_s / (b - a), 3) if b > a else None,
            'long_runs': len(long_runs), 'corrections_in_long_runs': sum(r['corrections'] for r in long_runs),
            'corrections_per_long_run': round(sum(r['corrections'] for r in long_runs) / len(long_runs), 2) if long_runs else None}


def _count(presses, pad, act):
    return sum(1 for p in presses if p['pad'] == pad and p['action'] == act)


def _held_s(presses, pad, inp):
    return round(sum(p['up'] - p['down'] for p in presses if p['pad'] == pad and p['input'] == inp), 1)


def idle_stretches(presses, a, b, min_s=2.0):
    """[(start, end)] of the stretches with nothing held or moved on either pad, at least min_s long."""
    spans = sorted((p['down'], p['up']) for p in presses)
    out, cur = [], a
    for s, e in spans:
        if s - cur >= min_s:
            out.append((cur, s))
        cur = max(cur, e)
    if b - cur >= min_s:
        out.append((cur, b))
    return out


def summarise(d, res, presses, tev, a, b, labels, by_input):
    s = {}
    buttons = [p for p in presses if p['input'] not in ('LEFT_STICK', 'RIGHT_STICK_X', 'RIGHT_STICK_Y')]
    for pad in PADS:
        mine = [p for p in buttons if p['pad'] == pad]
        counts = {}
        for p in mine:
            counts[p['action'] if p['action'] != 'UNBOUND' else 'UNBOUND ' + p['input']] = counts.get(
                p['action'] if p['action'] != 'UNBOUND' else 'UNBOUND ' + p['input'], 0) + 1
        s[pad.lower()] = {'button_presses': len(mine), 'counts': counts,
                          'stick_s': _held_s(presses, pad, 'LEFT_STICK'),
                          'turn_s': _held_s(presses, pad, 'RIGHT_STICK_X'),
                          'r2_s': _held_s(presses, pad, 'R2'), 'l2_s': _held_s(presses, pad, 'L2'),
                          'l1_s': _held_s(presses, pad, 'L1'),
                          'first_input': round(min((p['down'] for p in presses if p['pad'] == pad), default=b) - a, 1),
                          'any_input': any(p['pad'] == pad for p in presses)}

    # Fires: every Circle (driver) / FIRE (operator) press.
    fires = [p for p in presses if (p['pad'], p['action']) in (('DRIVER', 'CHASSIS_AIM'), ('OPERATOR', 'FIRE'))]
    fs = {'presses': len(fires), 'by_pad': {pad.lower(): sum(1 for p in fires if p['pad'] == pad) for pad in PADS},
          'fed': 0, 'never_fed': 0, 'shots': 0, 'blocked_at_press': {}, 'outcomes': {}, 'refused': [],
          'wait_s': [], 'idle_at_press': 0, 'not_at_speed_at_press': 0, 'during_feed': 0}
    for p in fires:
        fl = p.get('fire_line')
        fs['shots'] += p.get('shots', 0)
        if p.get('already_feeding'):
            fs['during_feed'] += 1
        elif p.get('gate_wait') is not None:
            fs['fed'] += 1
            fs['wait_s'].append(p['gate_wait'])
        else:
            fs['never_fed'] += 1
        if not p.get('launcher_spinning'):
            fs['idle_at_press'] += 1
        if not p.get('at_speed'):
            fs['not_at_speed_at_press'] += 1
        if fl:
            o = re.sub(r'\s*\(.*$', '', fl['outcome'])
            fs['outcomes'][o] = fs['outcomes'].get(o, 0) + 1
            for g, v in fl['gates'].items():
                if v.upper() == 'BLOCK':
                    fs['blocked_at_press'][g] = fs['blocked_at_press'].get(g, 0) + 1
        for n in p.get('notes', []):
            fs['refused'].append(round(p['down'] - a, 1))
            fs['outcomes']['note: ' + n.split(' (')[0][:60]] = fs['outcomes'].get('note: ' + n.split(' (')[0][:60], 0) + 1
    s['fires'] = fs

    # Square.
    spots = [p for p in presses if p['action'] == 'LAUNCH_SPOT']
    s['square'] = {'presses': len(spots), 'fired': sum(1 for p in spots if p.get('shots', 0) > 0),
                   'shots': sum(p.get('shots', 0) for p in spots),
                   'refused': sum(1 for p in spots if (p.get('spot') or '').startswith('refused')),
                   'let_go_early': sum(1 for p in spots if re.match(r'(let go|sticks moved) early', p.get('spot_end') or '')),
                   'arrived': sum(1 for p in spots if re.match(r'(let go|sticks moved) after arriving', p.get('spot_end') or ''))}

    # Launcher: who spun it up, and the operator's toggles.
    toggles = [p for p in presses if p['action'] == 'LAUNCHER_TOGGLE']
    s['launcher'] = {'toggles': len(toggles), 'spin_ups': sum(1 for p in toggles if p.get('spinning_after')),
                     'idles': sum(1 for p in toggles if not p.get('spinning_after')),
                     'spun_s': round(sum(min(e, b) - max(st, a) for st, e in R._windows(
                         [(t, (v or 0) > 0) for t, v in d.get('/Launcher/TargetTicksPerSecond', [])], a, b)), 1)}

    # Trims.
    lt = [p for p in presses if p['action'] in ('LAUNCHER_TRIM_UP', 'LAUNCHER_TRIM_DOWN', 'LAUNCHER_TRIM_RESET')]
    at = [p for p in presses if p['action'] in ('TRIM_LEFT', 'TRIM_RIGHT', 'TRIM_RESET')]

    def trim_summary(ps, series, up, dn):
        ch = _changes(d.get(series, []), a, b)
        vals = [v for _, _, v in ch if isinstance(v, (int, float))]
        rev, last_dir, last_t = 0, 0, None
        for p in ps:
            dirn = 1 if p['action'] == up else -1 if p['action'] == dn else 0
            if dirn and last_dir and dirn != last_dir and last_t is not None and p['down'] - last_t < 10:
                rev += 1
            if dirn:
                last_dir, last_t = dirn, p['down']
        return {'presses': len(ps), 'up': sum(1 for p in ps if p['action'] == up),
                'down': sum(1 for p in ps if p['action'] == dn),
                'reset': sum(1 for p in ps if p['action'].endswith('RESET')),
                'changes': len(ch), 'start': R.at(d.get(series, []), a), 'end': R.at(d.get(series, []), b),
                'max_abs': max((abs(v) for v in vals), default=0), 'reversals_within_10s': rev}
    s['launcher_trim'] = trim_summary(lt, '/Controls/LauncherTrimTps', 'LAUNCHER_TRIM_UP', 'LAUNCHER_TRIM_DOWN')
    s['aim_trim'] = trim_summary(at, '/Controls/TrimDeg', 'TRIM_RIGHT', 'TRIM_LEFT')
    s['fine_trim_held'] = _count(presses, 'OPERATOR', 'FINE_TRIM')

    # Holds and toggles.
    for act in ('RESET_HEADING', 'RESET_LEFT_CORNER', 'RESET_RIGHT_CORNER', 'CAMERA_RELOCALIZE', 'TURRET_ZERO'):
        ps = [p for p in presses if p['action'] == act]
        s[act.lower()] = {'presses': len(ps), 'acted': sum(1 for p in ps if p.get('acted'))}
    s['flower_stick'] = {'presses': _count(presses, 'OPERATOR', 'FLOWER_STICK'),
                         'out_changes': len(_changes(d.get('/FlowerStick/Out', []), a, b))}
    s['unjam'] = {'driver': _count(presses, 'DRIVER', 'DRIVER_UNJAM'), 'operator': _count(presses, 'OPERATOR', 'UNJAM'),
                  'driver_s': _held_s(presses, 'DRIVER', 'L2'), 'operator_s': _held_s(presses, 'OPERATOR', 'L2')}
    for act in ('FLOWER_PICK', 'GARDEN_GATHER'):
        ps = [p for p in presses if p['action'] == act]
        s[act.lower()] = {'presses': len(ps), 'started': sum(1 for p in ps if p.get('started', '') and p['started'].startswith('start')),
                          'refused': sum(1 for p in ps if (p.get('started') or '').startswith('refused'))}
    s['slow'] = {'presses': _count(presses, 'DRIVER', 'SLOW'), 'held_s': _held_s(presses, 'DRIVER', 'L1')}

    # Refusals, by reason (Square, FLOWER pick, GARDEN gather): the TeleOp's own words.
    reasons = {}
    for te, e in tev:
        m = re.match(r'(launch spot|FLOWER pick|GARDEN gather): refused, (.*?)(?:, from \(| \(|$)', e)
        if m:
            k = '%s: %s' % (m.group(1), m.group(2))
            reasons[k] = reasons.get(k, 0) + 1
    s['refusals'] = reasons

    # Square: how long the robot waited at the spot, and whether the launcher was spinning at the press.
    waits = []
    for p in spots:
        g = [e for te, e in tev if p['down'] <= te <= p['up'] + 0.1 and e.startswith('launch spot: gate open')]
        if not g:
            continue
        m = re.search(r'gate open ([\d.]+) s after the press(?: \(([\d.]+) s after arriving\))?', g[0])
        if m:
            waits.append({'t': round(p['down'] - a, 1), 'after_press': float(m.group(1)),
                          'after_arriving': float(m.group(2)) if m.group(2) else 0.0,
                          'on_approach': 'before arriving' in g[0],
                          'spinning_at_press': (R.at(d.get('/Launcher/TargetTicksPerSecond', []), p['down'] - 0.02) or 0) > 0})
    s['square_waits'] = waits

    # The SDK's pad assignment gesture (Options + Cross = gamepad 1, Options + Circle = gamepad 2) mid-TeleOp:
    # a controller dropped and was paired again.
    def overlaps(p, q):
        return p['pad'] == q['pad'] and p['down'] < q['up'] and q['down'] < p['up']
    opts = [p for p in buttons if p['input'] == 'OPTIONS']
    s['reassign'] = [{'t': round(o['down'] - a, 1), 'pad': o['pad'].lower(),
                      'as': 'gamepad 1' if q['input'] == 'CROSS' else 'gamepad 2'}
                     for o in opts for q in buttons if q['input'] in ('CROSS', 'CIRCLE') and overlaps(o, q)]
    # The longest stretch with the driver's sticks still (seconds from PLAY, length).
    sticks = sorted((p['down'], p['up']) for p in presses if p['pad'] == 'DRIVER' and p['input'] in ('LEFT_STICK', 'RIGHT_STICK_X'))
    gap, cur = (0.0, 0.0), a
    for st, e in sticks:
        if st - cur > gap[1]:
            gap = (round(cur - a, 1), round(st - cur, 1))
        cur = max(cur, e)
    if b - cur > gap[1]:
        gap = (round(cur - a, 1), round(b - cur, 1))
    s['longest_still_sticks'] = gap

    # Both pads firing at once: the operator's FIRE pressed while the driver held Square or Circle.
    drv = [p for p in presses if p['pad'] == 'DRIVER' and p['action'] in ('LAUNCH_SPOT', 'CHASSIS_AIM')]
    s['overlapping_fires'] = sum(1 for p in presses if p['pad'] == 'OPERATOR' and p['action'] == 'FIRE'
                                 and any(q['down'] < p['down'] < q['up'] for q in drv))

    # Fires pressed with the launcher idle: the press-to-gate wait then, and when it was already spinning.
    w_idle = [p['gate_wait'] for p in fires if p.get('gate_wait') is not None and not p.get('launcher_spinning')]
    w_spin = [p['gate_wait'] for p in fires if p.get('gate_wait') is not None and p.get('launcher_spinning')]
    s['fire_wait'] = {'idle': w_idle, 'spinning': w_spin}
    s['turret_lock'] = _count(presses, 'DRIVER', 'TURRET_LOCK') + _count(presses, 'OPERATOR', 'TURRET_LOCK')

    # Idle: nothing held or moved on either pad.
    idle = idle_stretches(presses, a, b)
    s['idle'] = {'total_s': round(sum(e - st for st, e in idle), 1), 'stretches': [(round(st - a, 1), round(e - st, 1)) for st, e in idle],
                 'longest_s': round(max((e - st for st, e in idle), default=0), 1)}
    # Operator idle: the operator's own pad.
    op = [p for p in presses if p['pad'] == 'OPERATOR']
    s['operator_quiet_s'] = round(sum(e - st for st, e in idle_stretches(op, a, b, 0.0)), 1)

    # Cycles, read_logs' split, with every fire press as a press.
    ev = d.get('/Events', [])
    pieces_in = [t for t, e in ev if R._PIECE_IN.search(str(e))]
    press_t = sorted(p['down'] for p in presses if (p['pad'], p['action']) in FIRE_ACTIONS)
    rows, last_end = R.cycle_rows(d.get('/FeedGate/Feeding', []), d.get('/Launcher/Shots', []), pieces_in, press_t,
                                  d.get('/Drive/OdometrySpeedInPerSec', []), a, b)
    for r in rows:
        # Who pressed the volley's fire, and whether the launcher was spinning then.
        press = r['end'] - r['feed'] - r['spin-up and aim']
        r['by'] = next(('%s %s' % (p['pad'].lower(), p['action']) for p in presses
                        if (p['pad'], p['action']) in FIRE_ACTIONS and abs(p['down'] - press) < 0.02), None)
        r['spinning_at_press'] = (R.at(d.get('/Launcher/TargetTicksPerSecond', []), press - 0.02) or 0) > 0
        r['at_speed_at_press'] = bool(R.at(d.get('/Launcher/AtSpeed', []), press - 0.02))
        r['t'] = round(r['start'] - a, 1)
    s['cycles'] = [{k: (round(v, 2) if isinstance(v, float) else v) for k, v in r.items() if k not in ('start', 'end')}
                   for r in rows]
    s['after_last_volley_s'] = round(b - last_end, 1)
    s['heading'] = heading_corrections(d, a, b)
    s['pieces'] = sum(r['shots'] for r in rows)
    return s


# ---- output -----------------------------------------------------------------------------------------

def _fmt_counts(c, labels=None):
    return ', '.join('%s %d' % (k, v) for k, v in sorted(c.items(), key=lambda kv: -kv[1])) or 'none'


def report(paths):
    for path in paths:
        r = study(path)
        print('=' * 90)
        print('%s  (%s, %s)' % (r['log'], r.get('config'), r.get('build')))
        if 'error' in r:
            print('  ' + r['error'])
            continue
        s = r['summary']
        print('  TeleOp %.0f s, %s, turret %s; %s' % (r['teleop_s'], r['alliance'],
              'locked forward' if r['turret_locked_forward'] else 'aiming', r['layout_note']))
        for pad in ('driver', 'operator'):
            x = s[pad]
            print('  %-8s %3d presses, first input %4.1f s; stick %5.1f s, turn %5.1f s, R2 %5.1f s, L2 %4.1f s, L1 %4.1f s'
                  % (pad, x['button_presses'], x['first_input'], x['stick_s'], x['turn_s'], x['r2_s'], x['l2_s'], x['l1_s']))
            print('           ' + _fmt_counts(x['counts']))
        f = s['fires']
        w = sorted(f['wait_s'])
        print('  fire presses %d (driver %d, operator %d): fed %d, pressed mid-feed %d, never fed %d; %d shots counted'
              % (f['presses'], f['by_pad']['driver'], f['by_pad']['operator'], f['fed'], f['during_feed'], f['never_fed'], f['shots']))
        print('    launcher idle at press %d, not at speed %d; gate wait median %s s, worst %s s'
              % (f['idle_at_press'], f['not_at_speed_at_press'], w[len(w) // 2] if w else '-', w[-1] if w else '-'))
        print('    blocked at press: %s' % (_fmt_counts(f['blocked_at_press'])))
        print('    outcomes: %s' % _fmt_counts(f['outcomes']))
        q = s['square']
        print('  Square %d: fired %d (%d shots), arrived %d, let go early %d, refused %d'
              % (q['presses'], q['fired'], q['shots'], q['arrived'], q['let_go_early'], q['refused']))
        L = s['launcher']
        print('  launcher toggles %d (spin-up %d, idle %d); spinning %.0f of %.0f s'
              % (L['toggles'], L['spin_ups'], L['idles'], L['spun_s'], r['teleop_s']))
        for k in ('launcher_trim', 'aim_trim'):
            t = s[k]
            print('  %-13s %d presses (up/right %d, down/left %d, reset %d), %s → %s, reversals %d'
                  % (k, t['presses'], t['up'], t['down'], t['reset'], t['start'], t['end'], t['reversals_within_10s']))
        print('  resets: gyro %s, left corner %s, right corner %s, camera %s, turret zero %s (presses/acted)' % tuple(
            '%d/%d' % (s[k]['presses'], s[k]['acted']) for k in
            ('reset_heading', 'reset_left_corner', 'reset_right_corner', 'camera_relocalize', 'turret_zero')))
        print('  FLOWER pick %d (refused %d), GARDEN gather %d, FLOWER stick %d, slow %d (%.1f s), unjam driver %d / operator %d, turret mode %d'
              % (s['flower_pick']['presses'], s['flower_pick']['refused'], s['garden_gather']['presses'],
                 s['flower_stick']['presses'], s['slow']['presses'], s['slow']['held_s'], s['unjam']['driver'],
                 s['unjam']['operator'], s['turret_lock']))
        i = s['idle']
        print('  no input on either pad: %.1f s in stretches ≥2 s, longest %.1f s; driver sticks still longest %.1f s at %.1f s'
              % (i['total_s'], i['longest_s'], s['longest_still_sticks'][1], s['longest_still_sticks'][0]))
        if s['refusals']:
            print('  refused: ' + _fmt_counts(s['refusals']))
        if s['reassign']:
            print('  PAD RE-ASSIGNED mid-TeleOp (Options + Cross/Circle): ' +
                  ', '.join('%s at %.1f s as %s' % (x['pad'], x['t'], x['as']) for x in s['reassign']))
        if s['overlapping_fires']:
            print('  operator FIRE pressed while the driver held Square/Circle: %d' % s['overlapping_fires'])
        sq = s['square_waits']
        if sq:
            print('  Square volleys: gate %d on the approach, %d after arriving (waited %s s at the spot)'
                  % (sum(1 for x in sq if x['on_approach']), sum(1 for x in sq if not x['on_approach']),
                     ', '.join('%.2f' % x['after_arriving'] for x in sq if not x['on_approach'])))
        h = s['heading']
        print('  steering while driving: %.1f s of %.1f s translating (%s), %d runs over %.0f in with %s corrections each'
              % (h['turning_while_translating_s'], h['translating_s'],
                 '-' if h['share_of_translating'] is None else '%.0f%%' % (100 * h['share_of_translating']),
                 h['long_runs'], LONG_RUN_IN, h['corrections_per_long_run']))
        print('  cycles: %d volleys, %d pieces, %.1f s after the last' % (len(s['cycles']), s['pieces'], s['after_last_volley_s']))
        for c in s['cycles']:
            print('    %5.1f  to pieces %4.1f  pick %4.1f  loaded %4.1f  spin/aim %4.1f  feed %4.1f  %d pcs  %s%s'
                  % (c['t'], c['drive to pieces'], c['pick up'], c['drive loaded'], c['spin-up and aim'], c['feed'],
                     c['shots'], c['by'] or 'no press', '' if c['spinning_at_press'] else ' (launcher idle at press)'))


def timeline(path):
    r = study(path)
    print('%s: TeleOp presses, seconds from TeleOp PLAY' % r['log'])
    for p in r.get('presses', []):
        if p['input'] in ('LEFT_STICK', 'RIGHT_STICK_X', 'RIGHT_STICK_Y'):
            continue
        extra = {k: v for k, v in p.items() if k not in ('pad', 'input', 'action', 'down', 'up', 'held')}
        print('  %6.2f  %-8s %-10s %-20s held %5.2f  %s' % (p['down'] - _play(path),
              p['pad'].lower(), p['input'], p['action'], p['held'], json.dumps(extra, ensure_ascii=False)[:160]))


def _play(path, _cache={}):
    if path not in _cache:
        _cache[path] = R._teleop_window(R.read(path))[0]
    return _cache[path]


def dump(out, paths):
    rows = []
    for path in paths:
        r = study(path)
        if 'presses' in r:
            a = _play(path)
            for p in r['presses']:
                p['down'] = round(p['down'] - a, 2)
                p['up'] = round(p['up'] - a, 2)
        rows.append(r)
    with open(out, 'w') as f:
        json.dump(rows, f, indent=1, default=str)
    print('wrote %s (%d logs)' % (out, len(rows)))


def main(argv):
    if len(argv) < 3:
        print(__doc__)
        return 1
    if argv[1] == 'report':
        report(argv[2:])
    elif argv[1] == 'timeline':
        timeline(argv[2])
    elif argv[1] == 'json':
        dump(argv[2], argv[3:])
    else:
        print(__doc__)
        return 1
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
