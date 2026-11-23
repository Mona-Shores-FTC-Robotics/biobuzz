#!/usr/bin/env python3
"""
Turns our robots' match logs into a match trace for the Visualizer's simulator (sim/, NCSSM's engine), so a real match
replays in its 3D view beside its video, and beside a simulated run of the same match.

  python3 tools/pull-logs/to_sim_trace.py OUT.json LOG.wpilog[.gz] [LOG ...] [--name Q28]

Each LOG is one robot's match log (Auto and TeleOp in one file, as the events/ branches keep them). The robot's alliance
comes from its "PLAY: RED/BLUE alliance" event; a second robot on the same alliance takes slot 1. Each log is aligned to
the match by its own PLAY (AUTO, match 0 to 30 s) and its own TeleOp start (match 38 s on), so a hub clock that ran
fast, or a late PLAY, does not shift it. Robots that left no log (partners, opponents) are not drawn.

What the trace carries, per frame at 10 Hz:
  - each robot's pose: /Odometry/PedroInches/X, Y, HeadingDeg, converted from Pedro's frame (origin at a corner,
    inches, CCW) to the simulator's (origin at the field centre, meters, X toward the blue wall, Z toward the audience):
    X = (x - 70.75) * 0.0254, Z = -(y - 70.75) * 0.0254, heading unchanged (the simulator's forward is (cos h, -sin h)
    in X, Z, which is Pedro's (cos h, sin h) in x, y);
  - its lane count (/Lane/Count) as held POLLEN (the lane's colours are not logged per slot);
  - its Auto card (the last "auto: command" event) as the tactic;
  - each HIVE's tilt, from the last state a camera saw (/Hive/<Alliance>/State with Assumed 0; carried forward): the
    match starts RIGHT_CELL_UP, which is the AUDIENCE CELL for red and the REAR one for blue, as each alliance's
    drivers see it; TRANSITION is drawn level.
Not carried: balls, FLOWER contents and the score, which the logs do not hold.

Standard library only.
"""
import json
import math
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import read_logs  # noqa: E402

CENTRE_IN = 70.75          # FieldFrame.FIELD_CENTRE_INCHES
M_PER_IN = 0.0254
TILT = math.pi / 6         # the simulator's HIVE.tiltLimit, 30 degrees
AUTO_S, TRANSITION_S, TELEOP_S = 30.0, 8.0, 120.0
HZ = 10
# HiveState ordinals (vision/HiveState.java)
RIGHT_CELL_UP, TRANSITION, LEFT_CELL_UP, UNSEEN = 0, 1, 2, 3
# The simulator's FLOWER sites (its src/sim/config.ts FIELD.flowers), so its view draws them.
FLOWER_SITES = [{'id': 'rear', 'x': -0.5942, 'z': -1.7282}, {'id': 'audience', 'x': 0.5942, 'z': 1.7282},
                {'id': 'red', 'x': -1.7282, 'z': 0.5942}, {'id': 'blue', 'x': 1.7282, 'z': -0.5942}]
FRAME_IN = 16.2            # RobotDesign.REAL_FRAME_IN
SETTLED_S, TRANSITION_S_SEEN = 0.75, 0.3


def step_at(series, t, default=None):
    """The last value at or before t (series sorted by time), else default."""
    lo, hi = 0, len(series)
    while lo < hi:
        mid = (lo + hi) // 2
        if series[mid][0] <= t:
            lo = mid + 1
        else:
            hi = mid
    return series[lo - 1][1] if lo else default


def mode_start(d, mode):
    for t, v in d.get('/DriverStation/RobotMode', []):
        if v == mode:
            return t
    return None


class RobotLog:
    def __init__(self, path):
        d = read_logs.read(path)
        self.path = path
        play = [str(v) for _, v in d.get('/Events', []) if re.match(r'PLAY: (RED|BLUE) alliance', str(v))]
        if not play:
            raise SystemExit(f'{path}: no "PLAY: <alliance> alliance" event')
        self.alliance = 'red' if 'RED' in play[0] else 'blue'
        self.auto0 = mode_start(d, 'autonomous')
        self.teleop0 = mode_start(d, 'teleop')
        if self.auto0 is None:
            raise SystemExit(f'{path}: no autonomous period')
        self.x, self.y = d['/Odometry/PedroInches/X'], d['/Odometry/PedroInches/Y']
        self.h = d['/Odometry/PedroInches/HeadingDeg']
        self.speed = d.get('/Drive/OdometrySpeedInPerSec', [])
        self.lane = d.get('/Lane/Count', [])
        self.cards = [(t, str(v)[len('auto: command '):].split()[0]) for t, v in d.get('/Events', [])
                      if str(v).startswith('auto: command ')]
        # Per HIVE, the moments (match time) this robot's camera saw it settle one way, debounced: a settled state
        # counts once it was seen for SETTLED_S in a row (single frames flicker), TRANSITION once seen TRANSITION_S_SEEN.
        self.hive = {}
        for a in ('Red', 'Blue'):
            states, assumed = d.get(f'/Hive/{a}/State', []), d.get(f'/Hive/{a}/Assumed', [])
            changes, run, run_from, current = [], None, None, None
            for t, v in states:
                v = int(v)
                if v == UNSEEN or step_at(assumed, t, 1):
                    run = None
                    continue
                if v != run:
                    run, run_from = v, t
                need = TRANSITION_S_SEEN if v == TRANSITION else SETTLED_S
                if v != current and t - run_from >= need:
                    current = v
                    changes.append((self.match_time(run_from), v))
            self.hive[a.lower()] = changes

    def match_time(self, log_t):
        """The inverse of log_time."""
        if self.teleop0 is None or log_t < self.teleop0:
            return log_t - self.auto0
        return AUTO_S + TRANSITION_S + (log_t - self.teleop0)

    def log_time(self, match_t):
        """The log's clock at match time match_t: AUTO and the transition from its PLAY, TeleOp from its TeleOp start."""
        if match_t < AUTO_S + TRANSITION_S or self.teleop0 is None:
            return self.auto0 + match_t
        return self.teleop0 + (match_t - AUTO_S - TRANSITION_S)


def hive_phi(alliance, state):
    """The simulator's tilt (positive: REAR CELL raised) for an alliance's HiveState."""
    if state == TRANSITION:
        return 0.0
    right_up = state == RIGHT_CELL_UP
    # Red's drivers' right is the audience side; blue's is the rear.
    rear_up = (not right_up) if alliance == 'red' else right_up
    return TILT if rear_up else -TILT


def phase_and_clock(t):
    if t < AUTO_S:
        return 'auto', AUTO_S - t
    if t < AUTO_S + TRANSITION_S:
        return 'transition', AUTO_S + TRANSITION_S - t
    return 'teleop', AUTO_S + TRANSITION_S + TELEOP_S - t


def build(paths, name):
    robots = [RobotLog(p) for p in paths]
    slots = {'red': 0, 'blue': 0}
    ids = []
    for r in robots:
        ids.append(('R' if r.alliance == 'red' else 'B') + str(slots[r.alliance]))
        slots[r.alliance] += 1
    frames, tips = [], {'red': 0, 'blue': 0}
    last_state = {'red': RIGHT_CELL_UP, 'blue': RIGHT_CELL_UP}
    settled = dict(last_state)
    # Every log's debounced HIVE changes, merged in match time: the first robot to see a flip shows it.
    pending = {a: sorted(c for r in robots for c in r.hive[a]) for a in ('red', 'blue')}
    end = AUTO_S + TRANSITION_S + TELEOP_S
    for k in range(int(end * HZ) + 1):
        t = k / HZ
        phase, clock = phase_and_clock(t)
        rf = []
        for rid, r in zip(ids, robots):
            lt = r.log_time(t)
            x, y, h = step_at(r.x, lt, CENTRE_IN), step_at(r.y, lt, CENTRE_IN), step_at(r.h, lt, 0.0)
            card = step_at(r.cards, lt, '') if t < AUTO_S else ''
            n = int(step_at(r.lane, lt, 0) or 0)
            rf.append({'id': rid, 'x': round((x - CENTRE_IN) * M_PER_IN, 3), 'z': round(-(y - CENTRE_IN) * M_PER_IN, 3),
                       'h': round(math.radians(h), 3), 'speed': round((step_at(r.speed, lt, 0.0) or 0.0) * M_PER_IN, 2),
                       'carried': ['P'] * max(0, min(4, n)), 'driver': 'auto script' if t < AUTO_S else 'human',
                       'tactic': card, 'status': '', 'note': 'from match log', 'goalKey': '', 'goal': None, 'path': [],
                       'phase': '', 'side': None, 'load': 0, 'stall': None})
        hives = {}
        for a in ('red', 'blue'):
            while pending[a] and pending[a][0][0] <= t:
                _, state = pending[a].pop(0)
                if state != TRANSITION and state != settled[a]:
                    tips[a] += 1  # settled the other way: a TIP
                    settled[a] = state
                last_state[a] = state
            phi = hive_phi(a, last_state[a])
            hives[a] = {'phi': round(phi, 3), 'up': 'rear' if hive_phi(a, settled[a]) > 0 else 'audience', 'tips': tips[a], 'load': 0}
        frames.append({'t': round(t, 2), 'phase': phase, 'clock': round(clock, 2), 'score': {'red': 0, 'blue': 0},
                       'hives': hives, 'flowers': [[] for _ in FLOWER_SITES], 'robots': rf})
    return {'name': name, 'startedAt': '', 'settings': {
        'source': 'match logs (biobuzz tools/pull-logs/to_sim_trace.py)',
        **{rid: os.path.basename(r.path) for rid, r in zip(ids, robots)}},
        'flowerSites': FLOWER_SITES, 'robotSize': [FRAME_IN * M_PER_IN, FRAME_IN * M_PER_IN], 'frames': frames}


def main(argv):
    if len(argv) < 3:
        print(__doc__)
        return 2
    name = 'match'
    if '--name' in argv:
        i = argv.index('--name'); name = argv[i + 1]; del argv[i:i + 2]
    out, paths = argv[1], argv[2:]
    trace = build(paths, name)
    with open(out, 'w') as f:
        json.dump(trace, f, separators=(',', ':'))
    tips = trace['frames'][-1]['hives']
    print(f'{out}: {len(trace["frames"])} frames, robots {", ".join(trace["settings"][k] + " as " + k for k in trace["settings"] if k[0] in "RB" and k[1:].isdigit())}; '
          f'TIPs seen: red {tips["red"]["tips"]}, blue {tips["blue"]["tips"]}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
