#!/usr/bin/env python3
"""Reads the robot's match logs (.wpilog) without AdvantageScope: for a Claude session, or anyone
with Python. Standard library only.

  python3 tools/pull-logs/read_logs.py summary LOG.wpilog [...]   per run: build, battery, knobs,
                                                                   events, every feed (gate open)
  python3 tools/pull-logs/read_logs.py turret LOG.wpilog [...]   per burst: timing, signal, before/after, delta
  python3 tools/pull-logs/read_logs.py keys LOG.wpilog            every key and its sample count
  python3 tools/pull-logs/read_logs.py series LOG.wpilog KEY [...] a key's samples, time in s
  python3 tools/pull-logs/read_logs.py picks LOG.wpilog [...]     every FLOWER pick: how the push
                                                                   ended, how deep, pieces, pose gap
  python3 tools/pull-logs/read_logs.py launcher LOG.wpilog [...]  every counted shot: speed, pose, nearest
                                                                   team spot, range; then shots per spot
                                                                   and speed, for the speed table (#80)
  python3 tools/pull-logs/read_logs.py volleys LOG.wpilog [...]   every Auto LaunchAll: the balls it fired,
                                                                   counted from the launcher wheels' dips,
                                                                   beside the shot counter's number
  python3 tools/pull-logs/read_logs.py steps LOG.wpilog [...]     every Auto card, the same table for every Auto:
                                                                   start, length, pieces held, and for a volley
                                                                   the gate wait, gate to last ball and balls
  python3 tools/pull-logs/read_logs.py dsbattery FILE [...]       each Driver Hub's own battery over the day,
                                                                   then every match log given beside it: the DS
                                                                   battery at its start, lowest, at its end

Logs reach GitHub on logs/<date>-<robot network> branches, sent by Pull-MatchLogs.ps1. A session
fetches them with:  git fetch origin 'refs/heads/logs/*:refs/remotes/origin/logs/*'
and reads one with:  git show origin/logs/<branch>:<file>.wpilog.gz > /tmp/x.wpilog.gz
(the logs go up gzipped; this reads .wpilog and .wpilog.gz alike)

A Driver Hub on USB at the pull adds two files per hub per day beside the robot's logs:
<date>_DriverHub-<serial>.dslog.txt (the Driver Station app's own log, that day's lines) and
<date>_DriverHub-<serial>.battery.txt (Android's battery history, one snapshot per pull). Give
dsbattery all of them and the day's .wpilog files: it sorts them by name.
"""
import datetime
import gzip
import bisect
import math
import re
import struct
import sys


def read(path):
    """{key: [(seconds, value), ...]} for every entry in a .wpilog (format: WPILib DataLog v1.0)."""
    b = (gzip.open if path.endswith('.gz') else open)(path, 'rb').read()
    if b[:6] != b'WPILOG':
        raise ValueError(path + ' is not a .wpilog')
    i = 12 + struct.unpack_from('<I', b, 8)[0]
    entries, data = {}, {}
    while i < len(b):
        h = b[i]
        i += 1
        il, sl, tl = (h & 3) + 1, ((h >> 2) & 3) + 1, ((h >> 4) & 7) + 1
        eid = int.from_bytes(b[i:i + il], 'little'); i += il
        size = int.from_bytes(b[i:i + sl], 'little'); i += sl
        ts = int.from_bytes(b[i:i + tl], 'little'); i += tl
        p = b[i:i + size]; i += size
        if eid == 0:
            if p and p[0] == 0:  # start record: id, name, type
                e = struct.unpack_from('<I', p, 1)[0]
                o = 5
                n = struct.unpack_from('<I', p, o)[0]; o += 4
                name = p[o:o + n].decode(errors='replace'); o += n
                n = struct.unpack_from('<I', p, o)[0]; o += 4
                typ = p[o:o + n].decode(errors='replace')
                entries[e] = (name, typ)
                data.setdefault(name, [])
            continue
        if eid not in entries:
            continue
        name, typ = entries[eid]
        try:
            if typ == 'double': v = struct.unpack('<d', p)[0]
            elif typ == 'float': v = struct.unpack('<f', p)[0]
            elif typ == 'int64': v = struct.unpack('<q', p)[0]
            elif typ == 'boolean': v = bool(p[0])
            elif typ in ('string', 'json'): v = p.decode(errors='replace')
            else: continue
        except (struct.error, IndexError):
            continue
        data[name].append((ts / 1e6, v))
    return data


def at(series, t):
    v = None
    for ts, x in series:
        if ts > t:
            break
        v = x
    return v


def low(series, a, b):
    xs = [x for t, x in series if a <= t <= b and isinstance(x, (int, float))]
    return min(xs) if xs else float('nan')


KNOBS = ['LauncherSubsystem.targetTicksPerSecond', 'FeederSubsystem.feedPower', 'FeederSubsystem.standbyPower',
         'FeederSubsystem.reversePower', 'LaunchCards.launchAllFeedS', 'PrefeedSubsystem.inPower',
         'PrefeedSubsystem.gateClearSeconds', 'PrefeedSubsystem.gateClearPower', 'LauncherSubsystem.leftReversed',
         'LauncherSubsystem.rightReversed', 'LauncherSubsystem.toleranceTicksPerSecond']
EVENT_WORDS = ['battery', 'launch', 'fed', 'fault', 'play', 'stall', 'never moved', 'auto:', 'timed out']


def summary(path):
    d = read(path)
    md = lambda k: (d.get('/RealMetadata/' + k) or [(0, '?')])[0][1]
    print('=' * 78)
    print(path.split('/')[-1])
    print('  build %s | %s | %s | battery at INIT %s' % (md('Build'), md('OpMode'), md('RobotConfig'), md('BatteryAtInit')))
    for k in KNOBS:
        v = d.get('/RealMetadata/Tunable/' + k)
        if v:
            print('  %-45s %s' % (k, v[0][1]))
    for ts, e in d.get('/Events', []):
        if any(w in str(e).lower() for w in EVENT_WORDS):
            print('   %7.1f s  %s' % (ts, str(e)[:150]))
    g = d.get('/FeedGate/Feeding', [])
    L, R = d.get('/Launcher/LeftTicksPerSecond', []), d.get('/Launcher/RightTicksPerSecond', [])
    T, F = d.get('/Launcher/TargetTicksPerSecond', []), d.get('/Feeder/TicksPerSecond', [])
    S, V = d.get('/Launcher/Shots', []), d.get('/Robot/BatteryVolts', [])
    windows, start = [], None
    for ts, v in g:
        if v and start is None: start = ts
        if not v and start is not None: windows.append((start, ts)); start = None
    if start is not None: windows.append((start, g[-1][0]))
    print('  feeds (gate open): %d' % len(windows))
    for a, b in windows:
        print('   %7.1f s %4.1f s | target %5s | at open L %5.0f R %5.0f | lowest L %5.0f R %5.0f | feeder %5.0f | shots %s | battery low %.2f V'
              % (a, b - a, at(T, a), at(L, a) or 0, at(R, a) or 0, low(L, a, b), low(R, a, b), at(F, a) or 0,
                 (at(S, b) or 0) - (at(S, a) or 0), low(V, a, b)))


FLOWER_FACING = {'red WALL': math.pi, 'red SCORING': math.pi / 2, 'blue WALL': 0.0, 'blue AUDIENCE': -math.pi / 2}


def flower_name(name):
    """A FLOWER's name as the logs print it now. Logs before 12 Nov 2026 called the two non-WALL
    FLOWERs "FAR": red's is on the scoring-table wall (SCORING), blue's on the audience wall (AUDIENCE)."""
    return {'red FAR': 'red SCORING', 'blue FAR': 'blue AUDIENCE'}.get(name, name)


def spikes(amps, a, b, above=1.35, gap_s=0.08):
    """Rising edges of the intake current over `above` A between a and b: about one per piece."""
    n, last, high = 0, -9.0, False
    for t, x in amps:
        if t < a or t > b:
            continue
        if x > above and not high:
            if t - last > gap_s:
                n += 1
            high, last = True, t
        elif x <= above:
            high = False
    return n


def picks(paths):
    """One row per FLOWER pick, from the pick's own event lines (opmodes/GrabRoutine), the intake
    current, the lane count and the camera's last gap to odometry:

      push end   reached | path done N in short | stalled N in short | timed out N in short
      depth      robot centre to the FLOWER's centre when the push ended, inches, as odometry had it
                 (the pick's own contact value, from its start line, plus what the push end line says
                 is short; older logs: the start-of-run contactStandoffIn)
      spikes     intake-current spikes during the push and dwell: about one per piece
      lane       /Lane/Count at the push's start -> half a second after the pick (newer logs only)
      along      camera minus odometry at the pick, along the push, inches: negative means the robot
                 was really further from the FLOWER than odometry said, so "reached" stopped short
      side       the same gap across the push (positive: the robot's left)

    The gap is the camera's last fix, wherever the robot was then, and the camera itself reads
    about 3.5 % long (doc/robot-session-4.md), so it is a hint about the sign and size of a pose
    error, not a measurement of it.
    """
    print('%-19s %7s %-13s %-22s %-24s %5s %3s %-6s %6s %6s  %s' % (
        'log', 't', 'flower', 'how', 'push end', 'depth', 'spk', 'lane', 'along', 'side', 'outcome'))
    for path in paths:
        d = read(path)
        ev = [(t, str(e)) for t, e in d.get('/Events', []) if 'FLOWER pick' in str(e) or 'flower pick' in str(e)]
        if not ev:
            continue
        amps, lane = d.get('/Intake/CurrentAmps', []), d.get('/Lane/Count', [])
        gx, gy = d.get('/Drift/GapXIn', []), d.get('/Drift/GapYIn', [])
        contact = float((d.get('/RealMetadata/Tunable/FlowerPicker.contactStandoffIn') or [(0, 12.0)])[0][1])
        i = 0
        while i < len(ev):
            t, e = ev[i]
            m = re.search(r'start, (.*?) FLOWER \(([\d.]+), ([\d.]+)\), from \(.*?\)(.*)', e)
            if not m:
                i += 1
                continue
            flower, how = flower_name(m.group(1)), m.group(4)
            # Newer logs end the start line with ", contact 7.0 in": the push depth as it was set then,
            # which Panels may have changed since INIT. Older logs fall back to the start-of-run value.
            cm = re.search(r', contact ([\d.]+) in', how)
            pick_contact = float(cm.group(1)) if cm else contact
            how = re.sub(r', contact [\d.]+ in', '', how)
            if ', in and out' in how:  # FlowerPicker.inAndOut: two pushes; the push end and depth are the second's
                how = re.sub(r', in and out \(.*?\)', '', how).strip(', ') + ', in+out'
            how = how.strip(', ') or '-'
            t_in = t_dwell = t_back = t_end = None
            push_end, outcome = '-', '?'
            j = i + 1
            while j < len(ev) and 'start,' not in ev[j][1] and 'refused' not in ev[j][1]:
                tj, ej = ev[j]
                if 'then IN' in ej and t_in is None:  # an in-and-out pick goes IN twice: the first
                    t_in = tj
                mm = re.search(r'IN [\d.]+ s \((.*?)\), then DWELL', ej)
                if mm:
                    push_end, t_dwell = mm.group(1), tj
                if re.search(r'DWELL [\d.]+ s.*, then BACK', ej):
                    t_back = tj
                if 'done,' in ej:
                    outcome, t_end = 'done', tj
                mm = re.search(r'stopped in (\w+) after [\d.]+ s \((.*?)\)', ej)
                if mm:
                    outcome, t_end = 'stopped in %s (%s)' % (mm.group(1), mm.group(2)), tj
                j += 1
            i = j
            if t_in is None:
                continue
            a, b = t_in, t_back or t_end or t_in + 4
            mm = re.search(r'([\d.]+) in short', push_end)
            depth = pick_contact + (float(mm.group(1)) if mm else 0.0) if push_end != '-' else float('nan')
            l0, l1 = at(lane, t_in), at(lane, (t_end or b) + 0.5)
            lane_s = '-' if l0 is None else '%d->%d' % (l0, l1 if l1 is not None else l0)
            x, y, f = at(gx, t_in), at(gy, t_in), FLOWER_FACING.get(flower)
            if x is None or f is None:
                along_s = side_s = '-'
            else:
                along_s = '%+6.1f' % (x * math.cos(f) + y * math.sin(f))
                side_s = '%+6.1f' % (-x * math.sin(f) + y * math.cos(f))
            print('%-19s %7.1f %-13s %-22s %-24s %5.1f %3d %-6s %6s %6s  %s' % (
                path.split('/')[-1][:19], t, flower, how[:22], push_end, depth, spikes(amps, a, b),
                lane_s, along_s, side_s, outcome))


FIELD_SIZE_IN = 141.5  # FieldFrame.FIELD_SIZE_INCHES: blue's spots are red's turned half a turn about the centre
SPOT_NEAR_IN = 8.0     # a shot this close to a team spot counts as taken from it
SPOTS_JSON = __import__('os').path.join(__import__('os').path.dirname(__file__), '..', '..', 'TeamCode', 'autos', 'spots.json')


def team_spots():
    """[(name, x, y)] for every team spot we launch from (a START or a LAUNCH spot) in
    TeamCode/autos/spots.json, red as drawn plus blue turned. Other spots are left out: a "shot"
    counted at a FLOWER or a staging spot is a dip with no launch behind it."""
    import json
    spots = json.load(open(SPOTS_JSON))['spots']
    out = []
    for name, xyh in sorted(spots.items()):
        if 'START' not in name and 'LAUNCH' not in name:
            continue
        x, y = xyh[0], xyh[1]
        out.append(('RED ' + name, x, y))
        out.append(('BLUE ' + name, FIELD_SIZE_IN - x, FIELD_SIZE_IN - y))
    return out


SHOT_RE = re.compile(r'shot \d+: target (\d+) ticks/s, dip (\d+), range (?:(\d+) in \(([^)]*)\)|unknown)')
SPOT_START_RE = re.compile(r'launch spot: start .*? → (\S+) .*CELL (\d+) in from the spot')
SPOT_END_RE = re.compile(r'launch spot: .*(?: early after | after arriving at |refused)')


def launcher(paths):
    """One row per counted shot (/Launcher/Shots going up), from any OpMode: the target speed in
    force, the pose, the nearest team spot (within SPOT_NEAR_IN, red or blue), and the range the
    TeleOp's shot line or the launch-spot press gave, if either logged one. Then the shots grouped
    by spot and speed, with an empty hits column for the video count: doc/robot-todo.md's
    "Launcher distances" rows, ready for LaunchSpeedTable (a row is the middle of the speeds that
    went 11/12 or better at a spot, at that spot's range)."""
    spots = team_spots()
    groups = {}
    print('%-34s %8s  %-28s %6s  %-26s %s' % ('log', 't s', 'pose (x, y, heading)', 'target', 'nearest spot', 'range'))
    for path in paths:
        d = read(path)
        shots = d.get('/Launcher/Shots', [])
        target = d.get('/Launcher/TargetTicksPerSecond', [])
        xs, ys, hs = (d.get('/Odometry/PedroInches/' + k, []) for k in ('X', 'Y', 'HeadingDeg'))
        events = d.get('/Events', [])
        name = path.split('/')[-1][:34]
        last = None
        for t, n in shots:
            if last is not None and n > last:
                x, y, h = at(xs, t), at(ys, t), at(hs, t)
                tps = at(target, t)
                spot, off = '', float('nan')
                if x is not None and y is not None:
                    best = min(spots, key=lambda s: math.hypot(s[1] - x, s[2] - y))
                    off = math.hypot(best[1] - x, best[2] - y)
                    spot = best[0] if off <= SPOT_NEAR_IN else ''
                # The TeleOp's shot line just after the count, else the press in progress.
                rng = ''
                press = None
                for te, v in events:
                    v = str(v)
                    if te > t + 0.2:
                        break
                    m = SPOT_START_RE.search(v)
                    if m:
                        press = '%s in (launch spot %s)' % (m.group(2), m.group(1))
                    elif SPOT_END_RE.search(v):
                        press = None
                    m = SHOT_RE.search(v)
                    if m and te >= t - 0.2 and m.group(3):
                        rng = '%s in (%s)' % (m.group(3), m.group(4))
                rng = rng or press or ''
                where = '' if x is None else '(%.1f, %.1f, %.0f°)' % (x, y, h if h is not None else float('nan'))
                print('%-34s %8.2f  %-28s %6s  %-26s %s' % (name, t, where,
                      '?' if tps is None else '%.0f' % tps,
                      (spot + ' (%.1f off)' % off) if spot else 'none within %.0f in' % SPOT_NEAR_IN, rng))
                key = (spot or '(no spot)', '?' if tps is None else '%.0f' % tps)
                g = groups.setdefault(key, [0, set()])
                g[0] += 1
                if rng:
                    g[1].add(rng.split(' in')[0])
            last = n
    print()
    print('%-30s %7s %6s  %-10s %s' % ('spot', 'target', 'shots', 'range in', 'hits (from video)'))
    for (spot, tps), (count, ranges) in sorted(groups.items()):
        print('%-30s %7s %6d  %-10s %s' % (spot, tps, count, '/'.join(sorted(ranges)) or '?', '___ of %d' % count))


# ------------------------------------------------------------------ the Driver Hub's own battery
#
# The robot cannot log it: in SDK 12.0 the Driver Station never sends its battery to the robot
# (the heartbeat carries time and robot state only). Two sources on the Driver Hub itself, both
# pulled by Pull-MatchLogs.ps1 over adb:
#
#   *.battery.txt  "dumpsys battery" and "dumpsys batterystats --history": Android's own record
#                  of every level change and plug-in since the last full charge. The main source.
#   *.dslog.txt    the Driver Station app's log. Its BatteryChecker (RobotCore 12.0) writes
#                  "percent remaining: N is charging: B" only while N < 30 (BATTERY_WARN_THRESHOLD;
#                  the class's LOG_THRESHOLD of 70 is never used), on every battery broadcast and
#                  every 5 min (FtcDriverStationActivity). So it is empty on a charged hub, and
#                  only a backstop for the history.

DS_LINE_RE = re.compile(r'^(\d\d)-(\d\d) (\d\d):(\d\d):(\d\d)\.(\d{3})\s.*?\bBatteryChecker\s*:\s*'
                        r'(?:\{[^}]*\}\s*)?percent remaining: (\d+) is charging: (true|false)')
DS_FILE_RE = re.compile(r'(\d{4})-(\d\d)-(\d\d)_DriverHub-(.+?)\.(dslog|battery)\.txt(?:\.gz)?$')
HIST_RE = re.compile(r'^\s*(0|\+[0-9dhms]+)\s+\(\d+\)\s+(.*)$')
DUR_RE = re.compile(r'^\+?(?:(\d+)d)?(?:(\d+)h)?(?:(\d+)m(?!s))?(?:(\d+)s)?(?:(\d+)ms)?$')
SNAPSHOT_HEADER = '=== '  # Pull-MatchLogs.ps1 starts every snapshot with a line like this


def wheel_dips(speeds, target, under=0.978, level=0.988, apart_s=0.28):
    """Times of the balls in [(seconds, wheel ticks/s)]: one ball is one sharp dip, a local low at
    least 2.2% under the target, coming from a level within 1.2% of the target held just before it,
    and at least 0.28 s after the last. Against the nine Belding Auto volleys that were also counted
    on video it was within one ball of the video every time and never under it; the shot counter was
    under on seven. A volley that starts from a standstill (the preloads) hides its dips in the
    spin-up: do not count that one this way."""
    out = []
    for t, v in speeds[1:-1]:  # a low needs a sample either side of it
        if v > target * under:
            continue
        if v > min(x for tt, x in speeds if abs(tt - t) <= 0.12) + 1e-9:
            continue
        before = sorted(x for tt, x in speeds if t - 0.40 <= tt <= t - 0.12)
        if len(before) < 3 or before[len(before) // 2] < target * level:
            continue
        if out and t - out[-1] < apart_s:
            continue
        out.append(t)
    return out


def volleys(paths):
    """Every LaunchAll card of each log's Autonomous: when it ran, the target speed, the balls it
    fired by the wheels' dips (wheel_dips), and what the shot counter said. The first volley of an
    Auto fires from a standstill and is not counted by dips."""
    print('%-34s %-13s %6s  %-14s %s' % ('log', 'volley (s)', 'target', 'balls by dips', 'counter'))
    for path in paths:
        d = read(path)
        events = [(t, str(v)) for t, v in d.get('/Events', [])]
        play = [t for t, v in events if re.match(r'PLAY: (RED|BLUE) alliance', v)]
        if not play:
            continue
        t0 = play[0]
        left, right = d.get('/Launcher/LeftTicksPerSecond', []), d.get('/Launcher/RightTicksPerSecond', [])
        target, shots = d.get('/Launcher/TargetTicksPerSecond', []), d.get('/Launcher/Shots', [])
        cards = [(t, v) for t, v in events if t0 <= t <= t0 + 33 and v.startswith('auto:')]
        starts = [t for t, v in cards if 'auto: command LaunchAll' in v]
        for i, ts in enumerate(starts):
            later = [t for t, v in cards if t > ts + 0.5]
            te = min(later[0] if later else ts + 5, ts + 5.5)
            tgt = at(target, ts + 1.0) or at(target, ts)
            speeds = [(ta, (a + b) / 2) for (ta, a), (tb, b) in zip(left, right) if ts - 0.45 <= ta <= te + 0.1]
            if not tgt or not speeds:
                continue
            counted = (at(shots, te) or 0) - (at(shots, ts) or 0)
            dips = 'from a standstill' if i == 0 else str(len([t for t in wheel_dips(speeds, tgt) if ts - 0.05 <= t <= te + 0.1]))
            print('%-34s %5.1f-%-5.1f  %6.0f  %-14s %.0f' % (path.split('/')[-1][:34], ts - t0, te - t0, tgt, dips, counted))


def _auto_steps(d):
    """[(kind, name, start, end)] for the cards of a log's Autonomous, seconds from PLAY, from the kit's
    own "auto: path|command|wait <name>" events. A card ends when the next starts; the last at 30 s or
    the last event of the Auto, whichever is first. Also returns PLAY's log time, or None with no Auto."""
    events = [(t, str(v)) for t, v in d.get('/Events', [])]
    play = [t for t, v in events if re.match(r'PLAY: (RED|BLUE) alliance', v)]
    if not play:
        return [], None
    t0 = play[0]
    starts = []
    for t, v in events:
        m = re.match(r'auto: (path|command|wait) (.*)', v)
        if m and t0 <= t <= t0 + 33:
            starts.append((m.group(1), m.group(2).strip(), t))
    if not starts:
        return [], t0
    last = max([t for t, v in events if t0 <= t <= t0 + 33] + [starts[-1][2]])
    out = []
    for i, (kind, name, t) in enumerate(starts):
        end = starts[i + 1][2] if i + 1 < len(starts) else min(max(last, t), t0 + 30)
        out.append((kind, name, t - t0, end - t0))
    return out, t0


def steps(paths):
    """The same table for every Auto, one row per card, so two Autos' logs compare step for step:

      start, s    seconds from PLAY to the card's start, and the card's length (to the next card's start)
      lane        /Lane/Count at the card's start -> at its end (pieces held; newer logs only)
      gate        LaunchAll only: seconds from the card's start to the feed gate opening (the spin-up
                  and settle wait)
      last ball   LaunchAll only: seconds from the gate opening to the last ball, by the wheels' dips
                  (wheel_dips); '-' for a volley from a standstill, whose dips hide in the spin-up
      balls       LaunchAll only: balls by the wheels' dips, then the shot counter's number
      note        a wait: what ended it; a pick: how the push ended

    Then one block per log of the numbers to compare between Autos: each pick's time and what it
    brought (the balls the next volley fired, by dips: the lane count is shown too, but it has read 4
    through a volley that fired four, so it is not the measure), each volley's length, its gate wait
    and its gate-to-last-ball, and each drive's seconds by name."""
    for path in paths:
        d = read(path)
        cards, t0 = _auto_steps(d)
        md = lambda k: (d.get('/RealMetadata/' + k) or [(0, '?')])[0][1]
        print('=' * 100)
        print('%s | %s | %s | build %s' % (path.split('/')[-1], md('OpMode'), md('RobotConfig'), md('Build')))
        if not cards:
            print('  no Autonomous cards in this log')
            continue
        events = [(t - t0, str(v)) for t, v in d.get('/Events', [])]
        lane = [(t - t0, v) for t, v in d.get('/Lane/Count', [])]
        gate = [(t - t0, v) for t, v in d.get('/FeedGate/Feeding', [])]
        left, right = d.get('/Launcher/LeftTicksPerSecond', []), d.get('/Launcher/RightTicksPerSecond', [])
        target, shots = d.get('/Launcher/TargetTicksPerSecond', []), d.get('/Launcher/Shots', [])
        print('  %6s %5s  %-8s %-38s %-6s %5s %9s %-7s %s' % ('start', 's', 'kind', 'card', 'lane', 'gate', 'last ball', 'balls', 'note'))
        picks_, volleys_, drives = [], [], []
        n_launch = 0
        for kind, name, a, b in cards:
            l0, l1 = at(lane, a), at(lane, b)
            lane_s = '-' if l0 is None else '%d->%d' % (l0, l1 if l1 is not None else l0)
            gate_s = ball_s = balls_s = ''
            note = ''
            if kind == 'command' and name.startswith('LaunchAll'):
                n_launch += 1
                opened = [t for t, v in gate if v and a - 0.05 <= t <= b + 0.05]
                g = opened[0] if opened else None
                tgt = at(target, t0 + a + 1.0) or at(target, t0 + a)
                speeds = [(ta, (x + y) / 2) for (ta, x), (tb, y) in zip(left, right) if t0 + a - 0.45 <= ta <= t0 + b + 0.1]
                counted = (at(shots, t0 + b) or 0) - (at(shots, t0 + a) or 0)
                dips = [] if n_launch == 1 or not tgt or not speeds else [t - t0 for t in wheel_dips(speeds, tgt) if a - 0.05 <= t - t0 <= b + 0.1]
                gate_s = '-' if g is None else '%.2f' % (g - a)
                ball_s = '-' if g is None or not dips else '%.2f' % (dips[-1] - g)
                balls_s = ('?' if n_launch == 1 else str(len(dips))) + '/%.0f' % counted
                volleys_.append((a, b - a, None if g is None else g - a, None if g is None or not dips else dips[-1] - g,
                                 None if n_launch == 1 else len(dips), counted))
            elif kind == 'command' and (name.startswith('PickFlower') or name.startswith('GardenGather')):
                ends = [v for t, v in events if a <= t <= b + 0.05 and ('then DWELL' in v or 'stopped in' in v)]
                m = re.search(r'\((.*?)\)', ends[0]) if ends else None
                note = m.group(1) if m else ''
                picks_.append((name, a, b - a, l0, l1))
            elif kind == 'wait':
                done = [v for t, v in events if a <= t <= b + 0.05 and v.startswith('auto: ' + name + ':')]
                note = done[-1].split(':', 2)[2].strip() if done else ''
            elif kind == 'path':
                if (kind, name, a, b) == cards[-1]:
                    note = 'the last card: its end is not logged, so this runs to 30 s'
                else:
                    drives.append((name, b - a))
            print('  %6.1f %5.2f  %-8s %-38s %-6s %5s %9s %-7s %s' % (a, b - a, kind, name[:38], lane_s, gate_s, ball_s, balls_s, note[:60]))
        print('  -- to compare between Autos --')
        for name, a, s, l0, l1 in picks_:
            nxt = [v for v in volleys_ if v[0] > a]
            fired = '-' if not nxt else '?' if nxt[0][4] is None else '%d' % nxt[0][4]
            print('  pick    %-14s at %5.1f s: %.2f s; the next volley fired %s; lane count %s' % (
                name, a, s, fired, '-' if l0 is None else '%d->%d' % (l0, l1 if l1 is not None else l0)))
        for a, s, g, lb, n, c in volleys_:
            print('  volley  at %5.1f s: %.2f s whole card; gate wait %s; gate to last ball %s; balls %s by dips, %.0f counted' % (
                a, s, '-' if g is None else '%.2f s' % g, '-' if lb is None else '%.2f s' % lb, '?' if n is None else n, c))
        for name, s in drives:
            print('  drive   %-40s %.2f s' % (name[:40], s))


def _text_lines(path):
    with (gzip.open(path, 'rt', encoding='utf-8', errors='replace') if path.endswith('.gz')
          else open(path, encoding='utf-8', errors='replace')) as f:
        for line in f:
            yield line.rstrip('\r\n')


def _duration_s(text):
    if text == '0':
        return 0.0
    m = DUR_RE.match(text)
    if not m:
        return None
    d, h, mi, s, ms = (int(g) if g else 0 for g in m.groups())
    return d * 86400 + h * 3600 + mi * 60 + s + ms / 1000.0


def ds_log_samples(path):
    """[(datetime, percent, plugged)] from a Driver Station log; the year is the file name's."""
    m = DS_FILE_RE.search(path.split('/')[-1])
    year = int(m.group(1)) if m else datetime.date.today().year
    out = []
    for line in _text_lines(path):
        mm = DS_LINE_RE.match(line)
        if mm:
            mo, dd, hh, mi, ss, ms, pct, chg = mm.groups()
            t = datetime.datetime(year, int(mo), int(dd), int(hh), int(mi), int(ss), int(ms) * 1000)
            out.append((t, int(pct), chg == 'true'))
    return out


def battery_history_samples(path):
    """[(datetime, percent, plugged)] from the pull's snapshots: "dumpsys battery" (the level at the
    pull, at the time in the snapshot's header) and "dumpsys batterystats --history".

    Each record's time is an offset from the snapshot's first record; a TIME: or RESET:TIME: record
    says what the wall clock was at its offset, so a record's wall time is the latest such clock
    plus the offset since it. Records before the first clock, or after a START (a reboot) until the
    next clock, have no wall time and are skipped. plugged is None until the history says."""
    out = []
    ref = None          # (offset s, datetime) of the latest TIME record in this snapshot
    plug = None
    pulled, powered = None, False   # the snapshot's own time, for "dumpsys battery" (level now)
    for line in _text_lines(path):
        if line.startswith(SNAPSHOT_HEADER):
            ref, plug, powered = None, None, False
            hm = re.match(r'=== (\d{4}-\d\d-\d\d \d\d:\d\d:\d\d)', line)
            pulled = datetime.datetime.strptime(hm.group(1), '%Y-%m-%d %H:%M:%S') if hm else None
            continue
        if pulled is not None:
            pm = re.match(r'\s+(?:AC|USB|Wireless|Dock) powered: (true|false)\s*$', line)
            if pm:
                powered = powered or pm.group(1) == 'true'
                continue
            lm = re.match(r'\s+level: (\d+)\s*$', line)
            if lm:
                out.append((pulled, int(lm.group(1)), powered))
                pulled = None
                continue
        m = HIST_RE.match(line)
        if not m:
            continue
        off = _duration_s(m.group(1))
        rest = m.group(2).strip()
        if off is None:
            continue
        if rest.startswith('RESET:TIME:') or rest.startswith('TIME:'):
            stamp = rest.split('TIME:', 1)[1].strip().split()[0]
            try:
                ref = (off, datetime.datetime.strptime(stamp, '%Y-%m-%d-%H-%M-%S'))
            except ValueError:
                ref = None
            if rest.startswith('RESET'):
                plug = None
            continue
        if rest.startswith('START') or rest.startswith('SHUTDOWN'):
            ref = None
            continue
        lm = re.match(r'(\d{3})\b', rest)
        if not lm:
            continue
        pm = re.search(r'\bplug=(\w+)', rest)
        if pm:
            plug = pm.group(1) != 'none'
        if ref is not None:
            out.append((ref[1] + datetime.timedelta(seconds=off - ref[0]), int(lm.group(1)), plug))
    return out


def _match_window(path, d):
    """(start, end) wall times of a match log: MatchId (or the file name) plus its last sample."""
    mid = (d.get('/RealMetadata/MatchId') or [(0, '')])[0][1]
    mm = re.search(r'(\d{4}-\d\d-\d\d_\d\d-\d\d-\d\d)', str(mid)) or re.search(
        r'(\d{4}-\d\d-\d\d_\d\d-\d\d-\d\d)', path.split('/')[-1])
    if not mm:
        return None
    start = datetime.datetime.strptime(mm.group(1), '%Y-%m-%d_%H-%M-%S')
    last = max((s[-1][0] for s in d.values() if s), default=0.0)
    return start, start + datetime.timedelta(seconds=last)


def _plug(p):
    return '?' if p is None else ('yes' if p else 'no')


def dsbattery(paths):
    """Each Driver Hub's battery through the day (changes only), then one row per match log and
    Driver Hub: the DS battery at the match's start, its lowest and at its end, beside the robot's
    own battery. The match's times are the robot's clock, which the Control Hub takes from the
    Driver Station, so the two line up to within a few seconds."""
    hubs = {}
    logs = []
    for p in sorted(paths):
        name = p.split('/')[-1]
        m = DS_FILE_RE.search(name)
        if m:
            src = 'OS history' if m.group(5) == 'battery' else 'DS log'
            samples = battery_history_samples(p) if m.group(5) == 'battery' else ds_log_samples(p)
            hubs.setdefault(m.group(4), []).extend((t, pct, plug, src) for t, pct, plug in samples)
        elif '.wpilog' in name:
            logs.append(p)
        else:
            print('skipped (neither a Driver Hub file nor a .wpilog): ' + name)
    if not hubs:
        print('No Driver Hub files (*_DriverHub-*.battery.txt / .dslog.txt) given: was a Driver Hub on '
              'USB at the pull?')
    series = {}
    for hub, samples in sorted(hubs.items()):
        # The history wins where both have a sample in the same second; snapshots overlap, so dedupe.
        seen, merged = set(), []
        for t, pct, plug, src in sorted(samples, key=lambda s: (s[0], s[3] != 'OS history')):
            key = (t.replace(microsecond=0), pct)
            if key in seen:
                continue
            seen.add(key)
            merged.append((t, pct, plug, src))
        series[hub] = merged
        print('=' * 78)
        print('Driver Hub %s: %d samples, %s to %s' % (
            hub, len(merged), merged[0][0].strftime('%Y-%m-%d %H:%M') if merged else '-',
            merged[-1][0].strftime('%H:%M') if merged else '-'))
        if merged and all(s[3] == 'DS log' for s in merged):
            print('  (DS log only: it records only below 30 %, so the hub was under 30 % for all of these)')
        print('  %-19s %5s %8s  %s' % ('time', 'DS %', 'plugged', 'from'))
        last = None
        for i, (t, pct, plug, src) in enumerate(merged):
            if last is not None and (pct, plug) == last and i != len(merged) - 1:
                continue
            last = (pct, plug)
            print('  %-19s %5d %8s  %s' % (t.strftime('%Y-%m-%d %H:%M:%S'), pct, _plug(plug), src))
    if not logs:
        return
    print()
    print('%-40s %8s %8s  %-14s %-26s %s' % ('match log', 'start', 'end', 'Driver Hub', 'DS % start / low / end',
                                           'robot V init / low'))
    for p in logs:
        d = read(p)
        win = _match_window(p, d)
        name = p.split('/')[-1][:40]
        volts = [x for _, x in d.get('/Robot/BatteryVolts', []) if isinstance(x, (int, float))]
        robot = '%s / %s' % ((d.get('/RealMetadata/BatteryAtInit') or [(0, '?')])[0][1],
                             '%.2f V' % min(volts) if volts else '?')
        if win is None:
            print('%-40s %8s %8s  %s' % (name, '?', '?', 'no match time in the log or its name'))
            continue
        a, b = win
        if not series:
            print('%-40s %8s %8s  %-14s %-26s %s' % (name, a.strftime('%H:%M:%S'), b.strftime('%H:%M:%S'),
                                                   '-', '-', robot))
        for hub, merged in sorted(series.items()):
            before = [s for s in merged if s[0] <= a]
            inside = [s for s in merged if a <= s[0] <= b]
            start = before[-1] if before else None
            end_s = ([s for s in merged if s[0] <= b] or [None])[-1]
            if start is None and not inside:
                ds = 'no sample yet'
            else:
                lows = [s[1] for s in inside] + ([start[1]] if start else [])
                ds = '%s / %d / %s' % ('?' if start is None else start[1], min(lows),
                                       '?' if end_s is None else end_s[1])
                if start is not None and (a - start[0]).total_seconds() > 1800:
                    ds += ' (start: %d min old)' % ((a - start[0]).total_seconds() // 60)
            print('%-40s %8s %8s  %-14s %-26s %s' % (name, a.strftime('%H:%M:%S'), b.strftime('%H:%M:%S'),
                                                   hub[:14], ds, robot))

def turret_bursts(data, settle_s=0.25):
    """Per-command movement from recorded absolute angles; never assume the angle is a delta.

    A settled sample needs 250 ms of recorded OFF time without a new burst or zero reference.
    SDK signal/enable values describe software commands, not an oscilloscope measurement.
    """
    prefix = '/TurretBurst/'
    def value(key, t, default=None):
        v = at(data.get(prefix + key, []), t)
        return default if v is None else v

    power = data.get(prefix + 'Power', [])
    angle = data.get(prefix + 'AngleDeg', [])
    if not power or not angle:
        return []
    segments, active = [], None
    previous = 0
    for t, p in power:
        direction = 1 if p > 0 else -1 if p < 0 else 0
        if active is not None and direction != previous:
            segments.append((active[0], t, active[1], True))
            active = None
        if direction and active is None:
            active = (t, direction)
        previous = direction
    if active is not None:
        segments.append((active[0], power[-1][0], active[1], False))
    angle_times = [t for t, _ in angle]
    rows = []
    for i, (start, end, direction, ended) in enumerate(segments):
        before = value('AngleDeg', start, float('nan'))
        after = value('AngleDeg', end, float('nan'))
        zero = value('ZeroCounts', start)
        same_zero = zero == value('ZeroCounts', end)
        manual = value('ManualSingleBurst', start, 0) and not value('Holding', start, 0)
        configured = value('ManualBurstMs', start, float('nan')) if manual else (
            value('HoldBurstMs', start, value('BurstOnMs', start, float('nan')))
            if value('Holding', start, 0) else value('BurstOnMs', start, float('nan')))
        next_start = segments[i + 1][0] if i + 1 < len(segments) else float('inf')
        settle_index = bisect.bisect_left(angle_times, end + settle_s)
        settled_at = angle_times[settle_index] if settle_index < len(angle) else float('inf')
        settled = (ended and next_start > settled_at and same_zero
                   and value('ZeroCounts', settled_at) == zero)
        settled_angle = angle[settle_index][1] if settled else float('nan')
        if not ended:
            reason = 'incomplete'
        elif value('Circle', end, 0):
            reason = 'cancel'
        elif value('R2', end, 0) <= 0.5 or (not value('Holding', end, 0)
                and not value('L1', end, 0) and not value('R1', end, 0)):
            reason = 'released'
        elif value('RawRequest', end, 0) and not value('AllowedRequest', end, 0):
            reason = 'blocked'
        else:
            reason = 'ended'
        rows.append(dict(start_s=start, configured_ms=configured,
                         on_ms=value('LastOnMs', end, (end - start) * 1000) if ended else (end-start)*1000,
                         pulse_us=value('PulseUs', start, 0),
                         pwm_enabled=value('PwmEnabledSdk', start, 0),
                         before_deg=before, after_deg=after,
                         drive_delta_deg=after-before if same_zero else float('nan'),
                         settled_delta_deg=settled_angle-before, settled=settled,
                         reason=reason, zero_changed=not same_zero,
                         battery_v=value('BatteryVolts', start, float('nan'))))
    return rows


def turret(paths):
    for path in paths:
        data = read(path)
        print(path)
        for k in ('Build', 'BenchVersion', 'RobotConfig'):
            values = data.get('/RealMetadata/' + k, [])
            if values:
                print('  %s: %s' % (k, values[-1][1]))
        rows = turret_bursts(data)
        if not rows:
            print('  No recorded turret drive commands. Check /TurretBurst/R2, L1, R1, Zeroed, RawRequest and AllowedRequest.')
            continue
        print('  SDK commands/readback only; settled = 250 ms recorded OFF, not proof of rest.')
        print('  #   start  set/ms   on/ms signal PWM   before -> after    drive-delta settle-delta  volts  end')
        for i, r in enumerate(rows, 1):
            print('%3d %7.2f %7.1f %7.1f %6.0f %3.0f %8.3f -> %8.3f %11.3f %12.3f %6.2f  %s%s' %
                  (i, r['start_s'], r['configured_ms'], r['on_ms'], r['pulse_us'], r['pwm_enabled'],
                   r['before_deg'], r['after_deg'], r['drive_delta_deg'], r['settled_delta_deg'],
                   r['battery_v'], r['reason'], ' ZERO CHANGED' if r['zero_changed'] else ''))
        print()


def main(argv):
    if len(argv) < 3:
        print(__doc__)
        return 1
    cmd, path = argv[1], argv[2]
    if cmd == 'summary':
        for p in argv[2:]:
            summary(p)
    elif cmd == 'keys':
        for k, v in sorted(read(path).items()):
            print('%6d  %s' % (len(v), k))
    elif cmd == 'picks':
        picks(argv[2:])
    elif cmd == 'launcher':
        launcher(argv[2:])
    elif cmd == 'volleys':
        volleys(argv[2:])
    elif cmd == 'steps':
        steps(argv[2:])
    elif cmd == 'dsbattery':
        dsbattery(argv[2:])
    elif cmd == 'turret':
        turret(argv[2:])
    elif cmd == 'series':
        d = read(path)
        for k in argv[3:]:
            for ts, v in d.get(k, []):
                print('%9.3f  %s  %s' % (ts, k, v))
    else:
        print(__doc__)
        return 1
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
