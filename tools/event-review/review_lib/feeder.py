"""Feeder reverses (unjam), gate openings that launched nothing, and drivetrain slip, per match.

- Reverse: time the feeder ran against its feeding direction. The feeding direction is the sign
  /Feeder/Power holds longest in the match (the feeder stands by and feeds the same way; the sign
  itself depends on FeederSubsystem.reversed), so the other sign is the unjam.
- Gate openings that launched nothing: the #506 detector's openings with no shot. "Piece at the
  sensor" are those where /Lane/Present read a piece just after the gate opened: a piece was
  there and did not go, the jam the unjam is for (or a feed too short to reach it).
- Slip: odometry speed per wheel speed (in/s per 1000 ticks/s, the four wheels' mean |ticks/s|,
  while they turn faster than WHEEL_MIN), in SLIP_BIN_S bins of the TeleOp. A robot's own
  event median is its grip; a match slips from the first bin from which every bin to the end of the
  TeleOp (at least two) is under SLIP_SHARE of it: grip lost and not regained. Ratios are compared
  within one robot only: the two robots' wheel encoders do not read alike.
"""
import collections
import statistics as st

from . import driving

WHEELS = ['/Drive/WheelTicksPerSec/' + w for w in ('FrontLeft', 'FrontRight', 'BackLeft', 'BackRight')]
WHEEL_MIN = 800.0
SLIP_BIN_S = 20.0
SLIP_SHARE = 0.7
AT_OPEN_S = 0.05


def reverse(m):
    """(seconds reversed, times reversed) between Auto start and the log's end."""
    fp = [(t, v) for t, v in m['/Feeder/Power'] if m.auto_start <= t <= m.log_end]
    run = collections.Counter()
    for (t, v), (t2, _) in zip(fp, fp[1:]):
        if v:
            run[v > 0] += t2 - t
    if not run:
        return 0.0, 0
    fwd = max(run, key=run.get)
    n, prev = 0, False
    for _, v in fp:
        r = bool(v) and (v > 0) != fwd
        n += r and not prev
        prev = r
    return run.get(not fwd, 0.0), n


def slip_bins(m, cfg):
    """[(bin start s from TeleOp PLAY, in/s per k ticks/s)] for the TeleOp, frozen-pose stretches out."""
    t0, t1 = driving.window(m)
    if t0 is None:
        return []
    sp, ws = m['/Drive/OdometrySpeedInPerSec'], [m[k] for k in WHEELS]
    if not len(sp) or not all(len(w) for w in ws):
        return []
    fr = driving.frozen(cfg, m)
    acc = collections.defaultdict(lambda: [0.0, 0.0])
    for t, v in sp.between(t0, t1):
        if any(a <= t <= b for a, b in fr):
            continue
        wt = sum(abs(w.at(t) or 0) for w in ws) / 4
        if wt > WHEEL_MIN:
            b = int((t - t0) // SLIP_BIN_S)
            acc[b][0] += abs(v)
            acc[b][1] += wt
    return [(b * SLIP_BIN_S, round(1000 * a / w, 1)) for b, (a, w) in sorted(acc.items()) if w]


def build(ms_by_team, vols, cfg):
    unjam, slip = [], []
    for team, ms in ms_by_team.items():
        bins = {m.log: slip_bins(m, cfg) for m in ms}
        allr = [r for bs in bins.values() for _, r in bs]
        grip = st.median(allr) if allr else None
        for m in ms:
            rev_s, revs = reverse(m)
            vs = [v for v in vols if v['team'] == team and v['log'] == m.log]
            empty = [v for v in vs if not v['shots_new']]
            stuck = [v for v in empty if m['/Lane/Present'].at(v['open_s'] + AT_OPEN_S)]
            unjam.append(dict(robot=team, match=m.name, order=m.order, reverse_s=round(rev_s, 1), reverses=revs,
                              gate_opens=len(vs), empty_opens=len(empty), stuck_opens=len(stuck)))
            bs = bins[m.log]
            rs = [r for _, r in bs]
            start = None
            if grip:
                for i, (b, _) in enumerate(bs):
                    rest = rs[i:]
                    if len(rest) >= 2 and max(rest) < SLIP_SHARE * grip:
                        start = b
                        break
            slip.append(dict(robot=team, match=m.name, order=m.order, grip=round(grip, 1) if grip else None,
                             ratio=round(st.median(rs), 1) if rs else None, bins=bs, slip_from_s=start))
    return {'unjam': unjam, 'drive_slip': slip}
