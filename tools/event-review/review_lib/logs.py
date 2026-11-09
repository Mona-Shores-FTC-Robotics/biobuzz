"""Reads one robot's match logs into compact, indexed series, and finds the match's phases.

A match log (.wpilog or .wpilog.gz) holds the Auto OpMode and the TeleOp OpMode that continued it.
Only the keys the review uses are kept, as parallel lists, so `at()` is a bisect instead of a scan
and seventeen logs fit in memory together.
"""
import bisect
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', '..', 'pull-logs'))
import read_logs as R  # noqa: E402  (tools/pull-logs/read_logs.py: the one log reader)

# Every key a dataset reads. Anything else in the log is dropped after parsing.
KEYS = [
    '/DriverStation/RobotMode', '/DriverStation/Joystick0/ButtonValues', '/DriverStation/Joystick1/ButtonValues',
    '/Events', '/Robot/BatteryVolts',
    '/Launcher/Shots', '/Launcher/TargetTicksPerSecond', '/Launcher/LeftTicksPerSecond', '/Launcher/RightTicksPerSecond',
    '/Launcher/AtSpeed', '/Launcher/InDip', '/Launcher/Reversing', '/Launcher/RecoverMs',
    '/Launcher/LeftCurrentAmps', '/Launcher/RightCurrentAmps',
    '/Feeder/CurrentAmps', '/Feeder/Power', '/Intake/CurrentAmps', '/Intake/TicksPerSecond',
    '/FeedGate/Feeding', '/FeedGate/Position',
    '/Lane/Count', '/Lane/Full', '/Lane/Present', '/Lane/Rejecting',
    '/Lane/Slot1', '/Lane/Slot2', '/Lane/Slot3', '/Lane/Slot4',
    '/Odometry/PedroInches/X', '/Odometry/PedroInches/Y', '/Odometry/PedroInches/HeadingDeg',
    '/Drive/OdometrySpeedInPerSec', '/Drive/CommandForward', '/Drive/CommandStrafe', '/Drive/CommandTurn',
    '/Drive/CommandFieldCentric',
]
META = ['/RealMetadata/MatchId', '/RealMetadata/OpMode', '/RealMetadata/RobotConfig', '/RealMetadata/Build',
        '/RealMetadata/Tunable/LauncherSubsystem.toleranceTicksPerSecond',
        '/RealMetadata/Tunable/LauncherSubsystem.shotDipTicksPerSecond',
        '/RealMetadata/Tunable/ColorSensorSubsystem.pollenHueMin', '/RealMetadata/Tunable/ColorSensorSubsystem.pollenHueMax']


class Series:
    """One key's samples: times t (s from log start) and values v, in time order."""
    __slots__ = ('t', 'v')

    def __init__(self, pairs):
        self.t = [a for a, _ in pairs]
        self.v = [b for _, b in pairs]

    def __len__(self):
        return len(self.t)

    def __iter__(self):
        return zip(self.t, self.v)

    def at(self, t):
        """The last value at or before t (read_logs.at), None before the first sample."""
        i = bisect.bisect_right(self.t, t) - 1
        return self.v[i] if i >= 0 else None

    def between(self, a, b):
        """(t, v) with a <= t <= b."""
        i, j = bisect.bisect_left(self.t, a), bisect.bisect_right(self.t, b)
        return list(zip(self.t[i:j], self.v[i:j]))

    def first(self, a, b, pred):
        """First time in [a, b] whose value satisfies pred, else None."""
        i = bisect.bisect_left(self.t, a)
        while i < len(self.t) and self.t[i] <= b:
            if pred(self.v[i]):
                return self.t[i]
            i += 1
        return None

    def low(self, a, b):
        xs = [x for _, x in self.between(a, b) if isinstance(x, (int, float))]
        return min(xs) if xs else float('nan')


EMPTY = Series([])


class Match:
    """One match log: its series, its phases and who and what it was.

    Times are seconds from the log start. auto_start / auto_end / tele_start are the Driver
    Station's mode changes (autonomous, then the next mode, then teleop); log_end is the last
    battery sample above 1 V. tele_init / tele_play / tele_stop are the TeleOp OpMode's own
    events (`OpMode init: <name> · continues match`, the PLAY after it, `OpMode stopped: <name>`).
    """

    def __init__(self, path, team):
        self.path = path
        self.team = team
        self.log = os.path.basename(path)[:19]       # 2026-11-07_11-36-28
        raw = R.read(path)
        self.s = {k: Series(raw.get(k, [])) for k in KEYS}
        self.meta = {k.split('/')[-1]: (raw.get(k) or [(0, None)])[0][1] for k in META}
        del raw
        self.events = [(t, str(e)) for t, e in self.s['/Events']]
        self.name = None       # match name, set by the schedule ("Q4", "Upper final")
        self.order = None      # 1.. in time order for this robot
        self.alliance = None   # 'Red' / 'Blue'
        self._phases()

    def __getitem__(self, k):
        return self.s.get(k, EMPTY)

    def _phases(self):
        mode = list(self['/DriverStation/RobotMode'])
        self.auto_start = next((t for t, v in mode if v == 'autonomous'), None)
        self.auto_end = next((t for t, v in mode if self.auto_start is not None and t > self.auto_start
                              and v != 'autonomous'), None)
        self.tele_start = next((t for t, v in mode if v == 'teleop'), None)
        bv = [(t, v) for t, v in self['/Robot/BatteryVolts'] if v > 1]
        self.log_end = bv[-1][0] if bv else (self.events[-1][0] if self.events else 0.0)
        inits = [(t, e) for t, e in self.events if e.startswith('OpMode init:') and 'continues match' in e]
        self.tele_init = inits[0][0] if inits else None
        self.tele_opmode = re.match(r'OpMode init: (.*?) ·', inits[0][1]).group(1) if inits else None
        self.tele_play = next((t for t, e in self.events if self.tele_init is not None and t > self.tele_init
                               and e.startswith('PLAY:')), None)
        self.tele_stop = next((t for t, e in self.events if self.tele_init is not None and t > self.tele_init
                               and e.startswith('OpMode stopped: ' + (self.tele_opmode or ''))), None)
        al = next((e for t, e in self.events if re.match(r'PLAY: (RED|BLUE) alliance', e)), None)
        self.log_alliance = None if al is None else al.split()[1].title()

    def phase_of(self, t):
        """'auto' or 'tele' by which OpMode was running (the TeleOp init splits them)."""
        return 'tele' if self.tele_init is not None and t >= self.tele_init else 'auto'

    def label(self):
        return '%s %s (%s)' % (self.team, self.name or '?', self.log)


def find_logs(root, team):
    """A robot's match logs, in time order: <root>/<team>/*.wpilog(.gz), or <root>/<team>/matches/."""
    out = []
    for d in (os.path.join(root, team, 'matches'), os.path.join(root, team)):
        if os.path.isdir(d):
            out = sorted(os.path.join(d, f) for f in os.listdir(d) if f.endswith(('.wpilog', '.wpilog.gz')))
            if out:
                break
    return out


def edges(series, bit=None):
    """Rising-edge times of a boolean series, or of one bit of a button bitmask."""
    out, prev = [], 0
    for t, v in series:
        b = (int(v) >> bit) & 1 if bit is not None else (1 if v else 0)
        if b and not prev:
            out.append(t)
        prev = b
    return out


def windows(series):
    """(start, end) of each stretch a boolean series was true; an open one ends at its last sample."""
    out, start = [], None
    for t, v in series:
        if v and start is None:
            start = t
        elif not v and start is not None:
            out.append((start, t))
            start = None
    if start is not None and len(series):
        out.append((start, series.t[-1]))
    return out
