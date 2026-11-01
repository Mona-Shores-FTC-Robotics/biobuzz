#!/usr/bin/env python3
"""Reads the robot's match logs (.wpilog) without AdvantageScope: for a Claude session, or anyone
with Python. Standard library only.

  python3 tools/pull-logs/read_logs.py summary LOG.wpilog [...]   per run: build, battery, knobs,
                                                                   events, every feed (gate open)
  python3 tools/pull-logs/read_logs.py keys LOG.wpilog            every key and its sample count
  python3 tools/pull-logs/read_logs.py series LOG.wpilog KEY [...] a key's samples, time in s

Logs reach GitHub on logs/<date>-<robot network> branches, sent by Pull-MatchLogs.ps1. A session
fetches them with:  git fetch origin 'refs/heads/logs/*:refs/remotes/origin/logs/*'
and reads one with:  git show origin/logs/<branch>:<file>.wpilog.gz > /tmp/x.wpilog.gz
(the logs go up gzipped; this reads .wpilog and .wpilog.gz alike)
"""
import gzip
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
