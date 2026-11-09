"""The improved shot detector (PR #506), replayed offline over a match log, and the volleys it finds.

VENDORED: `ShotDetector` below is a line-for-line Python copy of LauncherSubsystem.ShotDetector in
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/subsystems/LauncherSubsystem.java, from
PR #506 (branch claude/launcher-shot-count, "Launcher: count shots from both wheels' average dip,
with a duration window"). The robot code is Java, so there is no shared Python module to import;
if the Java detector changes, change this copy to match and rerun test_reproduce.py. The Belding
match logs were recorded with the old counter, so the review replays the new one over them;
logs from robots running #506 also log its count as /Launcher/Shots.

`volleys(match)` gives one record per feed-gate opening while the launcher was spinning, in the
shape the #506 session sent for the Belding review (data/belding/shot-detector/<team>.json):
open_s, open_dur_s, lane_at_open, old, new, shots_new, dip_start_s, reliable, why.
"""
import math

NAN = float('nan')


class ShotDetector:
    LEVEL_WEIGHT = 0.1
    LEVEL_BAND_TPS = 30.0
    RESEED_MS = 300
    SETTLED_MS = 150
    RETARGET_TPS = 25.0
    START_BELOW_TPS = 10.0
    END_TPS = 20.0
    END_FRACTION = 0.25
    EACH_WHEEL_TPS = 25.0
    MIN_DIP_MS = 35
    MAX_DIP_MS = 180
    DEEP_SLACK_MS_PER_TPS = 1.0
    ABANDON_MS = 400
    MIN_SHOT_GAP_MS = 150

    def __init__(self):
        self.left_level = NAN
        self.right_level = NAN
        self.out_since = -1
        self.last_target_seen = NAN
        self.in_dip = False
        self.dip_start = 0
        self.deepest = 0.0
        self.left_deepest = 0.0
        self.right_deepest = 0.0
        self.shots = 0
        self.just_counted = False
        self.shot_yet = False
        self.last_shot = 0
        self.last_update = -1
        self.last_dip_start = None   # for the replay: when the counted shot's dip began, s
        self.dip_start_s = None
        self.clock_s = None          # the replay sets this to the loop's log time before update()

    def update(self, left, right, within, gate_open, target, count_dip, now):
        self.just_counted = False
        loop = 0 if self.last_update < 0 else now - self.last_update
        self.last_update = now
        if not math.isnan(self.last_target_seen) and abs(target - self.last_target_seen) > self.RETARGET_TPS:
            self.reset()
        self.last_target_seen = target
        if math.isnan(self.left_level) or math.isnan(self.right_level):
            if within:
                self._seed(left, right)
            return False
        dl = self.left_level - left
        dr = self.right_level - right
        d = 0.5 * (dl + dr)
        if self.in_dip and now - self.dip_start > self.ABANDON_MS:
            self.in_dip = False
            self._seed(left, right)
            return False
        if self.in_dip:
            self.deepest = max(self.deepest, d)
            self.left_deepest = max(self.left_deepest, dl)
            self.right_deepest = max(self.right_deepest, dr)
            if d <= max(self.END_TPS, self.END_FRACTION * self.deepest):
                self.in_dip = False
                lasted = now - self.dip_start
                longest = self.MAX_DIP_MS + loop + self.DEEP_SLACK_MS_PER_TPS * max(0.0, self.deepest - 2.0 * count_dip)
                if (self.deepest >= count_dip and self.MIN_DIP_MS <= lasted <= longest
                        and min(self.left_deepest, self.right_deepest) >= self.EACH_WHEEL_TPS):
                    self.shots += 1
                    self.just_counted = True
                    self.last_shot = now
                    self.shot_yet = True
                    self.last_dip_start = self.dip_start_s
        elif (d >= count_dip - self.START_BELOW_TPS and gate_open
              and (not self.shot_yet or now - self.last_shot >= self.MIN_SHOT_GAP_MS)
              and (self.out_since < 0 or now - self.out_since <= self.SETTLED_MS)):
            self.in_dip = True
            self.dip_start = now
            self.dip_start_s = self.clock_s   # the replay's own seconds, for timing
            self.deepest = d
            self.left_deepest = dl
            self.right_deepest = dr
        elif abs(d) <= self.LEVEL_BAND_TPS:
            self.left_level += self.LEVEL_WEIGHT * (left - self.left_level)
            self.right_level += self.LEVEL_WEIGHT * (right - self.right_level)
            self.out_since = -1
        elif self.out_since < 0:
            self.out_since = now
        elif now - self.out_since > self.RESEED_MS:
            self._seed(left, right)
        return self.just_counted

    def _seed(self, left, right):
        self.left_level = left
        self.right_level = right
        self.out_since = -1

    def reset(self):
        self.in_dip = False
        self.just_counted = False
        self.left_level = NAN
        self.right_level = NAN
        self.out_since = -1
        self.last_target_seen = NAN


def replay(m, count_dip=55.0, tolerance=60.0):
    """[(counted_at_s, dip_start_s)] for every shot the #506 detector counts over the whole log.

    It runs as LauncherSubsystem.update() does: only while spinning toward a target and not
    reversing; otherwise it resets (the count stays). One update per logged loop.
    """
    L, Rr, T = m['/Launcher/LeftTicksPerSecond'], m['/Launcher/RightTicksPerSecond'], m['/Launcher/TargetTicksPerSecond']
    G, REV = m['/FeedGate/Feeding'], m['/Launcher/Reversing']
    same = L.t == T.t == Rr.t
    det = ShotDetector()
    out = []
    for i, t in enumerate(L.t):
        left = abs(L.v[i])
        right = abs(Rr.v[i] if same else (Rr.at(t) or 0.0))
        target = T.v[i] if same else (T.at(t) or 0.0)
        gate = bool(G.v[i] if len(G.t) == len(L.t) else G.at(t))
        rev = bool(REV.v[i] if len(REV.t) == len(L.t) else REV.at(t))
        if rev or not target or target <= 0:
            det.reset()
            continue
        within = abs(left - target) <= tolerance and abs(right - target) <= tolerance
        det.clock_s = t
        if det.update(left, right, within, gate, target, count_dip, int(t * 1000)):
            out.append((t, det.last_dip_start))
    return out


def old_shots(m):
    """Times /Launcher/Shots went up (the counter the robot ran), one entry per shot."""
    out, last = [], None
    for t, v in m['/Launcher/Shots']:
        if last is not None and v > last:
            out += [t] * int(round(v - last))
        last = v
    return out


def reliability(r, d):
    """The #506 session's rules: why a volley's count is not trusted (empty list: reliable).
    d: the dips' start times after the gate opened (ms clock). "First shot later than 1.6 s" is
    judged on the dip start rounded to 0.1 s, which is what reproduces the Belding records."""
    why = []
    if r['new'] > 4:
        why.append('more than 4 shots')
    if any(b - a >= 0.8 for a, b in zip(d, d[1:])):
        why.append('a gap of 0.8 s or more between shots')
    if any(b - a < 0.3 for a, b in zip(d, d[1:])):
        why.append('two shots under 0.3 s apart')
    if d and round(d[0], 1) >= 1.6:
        why.append('first shot later than 1.6 s')
    if d and d[0] < 0.3:
        why.append('a dip starting under 0.3 s after the gate opened')
    return why


# LauncherSubsystem.shotDipTicksPerSecond's default in PR #506. Not read from the log: a log from a
# robot without #506 records the old counter's depth (60 at Belding), which this detector must not use.
COUNT_DIP = 55.0


def volleys(m, count_dip=COUNT_DIP, tolerance=None):
    """One record per gate opening while the launcher was spinning (see the module comment)."""
    tolerance = tolerance or float(m.meta.get('LauncherSubsystem.toleranceTicksPerSecond') or 60.0)
    new = replay(m, count_dip, tolerance)
    old = old_shots(m)
    T, lane = m['/Launcher/TargetTicksPerSecond'], m['/Lane/Count']
    out = []
    from .logs import windows
    opens = windows(m['/FeedGate/Feeding'])
    G = m['/FeedGate/Feeding']
    for k, (a, b) in enumerate(opens):
        b_last = G.t[max(0, G.t.index(b) - 1)] if b in G.t else b   # the last loop the gate was open
        if (T.at(a) or 0) <= 0:
            continue
        nxt = opens[k + 1][0] if k + 1 < len(opens) else 1e9
        # A shot belongs to this opening if its dip began while the gate was open; the old counter
        # counted at recovery, up to a dip's length after the gate shut.
        mine = [(c, s) for c, s in new if a <= s <= b]
        olds = [t for t in old if a <= t <= min(b + 0.3, nxt)]
        r = dict(team=m.team, log=m.log, phase=m.phase_of(a), open_s=round(a, 2), open_dur_s=round(b_last - a, 2),
                 lane_at_open=int(lane.at(a) or 0), old=len(olds), new=len(mine),
                 shots_new=[round(c - a, 2) for c, _ in mine], dip_start_s=[round(int(s * 1000) / 1000 - a, 2) for _, s in mine])
        why = reliability(r, [int(s * 1000) / 1000 - a for _, s in mine])
        r['reliable'] = not why
        if why:
            r['why'] = why
        out.append(r)
    return out
