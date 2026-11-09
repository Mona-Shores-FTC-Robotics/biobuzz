"""Which match each log is: the log's start time against FTCScout's schedule, or the config.

A log's name starts with the Control Hub's local start time (2026-11-07_11-36-28). Hub clocks
drift (19429's ran about 10 minutes fast at Belding), so a robot's logs are matched to its
matches as a whole: every clock offset one log-match pair suggests is tried, each log takes the
nearest unclaimed match within WINDOW_S after that offset, and the offset matching the most logs
(then the smallest total gap) wins. With as many logs as matches this is just time order.
Overrides in the event config (`match_names`: "<team>/<log>": "Q4" or "skip") always win.
"""
import datetime

WINDOW_S = 6 * 60


def log_time(log, tz):
    try:
        t = datetime.datetime.strptime(log[:19], '%Y-%m-%d_%H-%M-%S')
    except ValueError:
        return None
    if tz:
        try:
            from zoneinfo import ZoneInfo
            t = t.replace(tzinfo=ZoneInfo(tz))
        except Exception:
            t = t.replace(tzinfo=datetime.timezone.utc)
    else:
        t = t.replace(tzinfo=datetime.timezone.utc)
    return t.timestamp()


def match_time(x):
    s = x.get('actualStartTime') or x.get('scheduledStartTime')
    return datetime.datetime.fromisoformat(s.replace('Z', '+00:00')).timestamp() if s else None


def assign(logs, ours, tz):
    """logs: [log name]; ours: [(match, alliance)] in time order. Returns {log: (match, alliance)}."""
    lt = {l: log_time(l, tz) for l in logs}
    mt = [(match_time(x), x, al) for x, al in ours]
    mt = [p for p in mt if p[0] is not None]
    if not mt or not any(v is not None for v in lt.values()):
        return {}
    best = None
    offsets = {round(lt[l] - t) for l in logs if lt[l] is not None for t, _, _ in mt} or {0}
    for off in sorted(offsets):
        taken, got, gap = set(), {}, 0.0
        for l in sorted(logs):
            if lt[l] is None:
                continue
            c = [(abs(lt[l] - off - t), i) for i, (t, _, _) in enumerate(mt) if i not in taken]
            c = [p for p in c if p[0] <= WINDOW_S]
            if c:
                d, i = min(c)
                taken.add(i)
                got[l] = (mt[i][1], mt[i][2])
                gap += d
        key = (-len(got), gap)
        if best is None or key < best[0]:
            best = (key, got)
    got = best[1]
    # Matches must stay in time order: a log assigned out of order means the offset was wrong.
    order = [mt.index(next(p for p in mt if p[1] is got[l][0])) for l in sorted(got)]
    if order != sorted(order):
        return {}
    return got
