#!/usr/bin/env python3
"""rollsteady.py SPEC...  (SPEC = NAME:CSV:ids:CORNERS_JSON). The steady-roll deceleration of each track: the rolling
phase (from the last speed jump, a landing, bounce or kick, to rest), but the fit stops when the smoothed speed first falls under 5 in/s or drops by more
than 35% in 0.15 s (a collision or the final wobble), whichever is first. Reports v0, the fitted decel (+-1 sigma from
the residuals, combined with the homography sensitivity, corners +-6 px), how it ended, and the overall figures."""
import sys, csv, json, numpy as np
from collections import defaultdict
sys.path.insert(0, __file__.rsplit('/', 1)[0]); import fieldmap
rng = np.random.default_rng(1)
def phase(t, x, y):
    step = np.hypot(np.diff(x), np.diff(y)); v = step / np.diff(t); vs = np.convolve(v, np.ones(5)/5, 'same')
    start = 0
    for k in range(1, len(vs)):
        if vs[k-1] > 1.8*vs[k] + 4 or vs[k] > 1.8*vs[k-1] + 4: start = k
    end = len(vs); how = 'track ends'
    for k in range(start+3, len(vs)):
        if vs[k] < 5: end = k; how = 'rolled to rest'; break
        if k >= start+6 and vs[k] < 0.65 * vs[k-3]: end = k-3; how = 'hit something'; break
    return start, end, how, vs
def fit(t, x, y, start, end):
    step = np.hypot(np.diff(x), np.diff(y)); s = np.r_[0, np.cumsum(step)]
    tt = t[start:end+1] - t[start]; ss = s[start:end+1] - s[start]
    A = np.c_[tt, -0.5*tt**2]; sol, *_ = np.linalg.lstsq(A, ss, rcond=None)
    res = ss - A @ sol; n = len(tt); sigma2 = (res**2).sum() / max(n-2, 1); cov = sigma2 * np.linalg.inv(A.T @ A)
    return sol[0], sol[1], np.sqrt(cov[1, 1]), np.sqrt(sigma2), ss[-1], tt[-1]
out = []
print('| Spill | Track | Piece | Start, in from HIVE wall | v0 in/s | Rolled | Decel in/s² | How it ended |')
print('|---|---|---|---|---|---|---|---|')
for spec in sys.argv[1:]:
    name, csvp, ids, cj = spec.split(':'); corners = json.load(open(cj)); H = fieldmap.frame_to_field(corners)
    tr = defaultdict(list)
    for r in csv.DictReader(open(csvp)): tr[r['track']].append((float(r['t']), float(r['px']), float(r['py']), r['kind']))
    for i in ids.split(','):
        p = tr[i]; kind = p[0][3]; t = np.array([q[0] for q in p]); px = np.array([q[1] for q in p]); py = np.array([q[2] for q in p])
        f = fieldmap.apply(H, np.c_[px, py]); x, y = f[:, 0], f[:, 1]
        start, end, how, vs = phase(t, x, y)
        if end - start < 8: continue
        v0, a, sa, rms, dist, dur = fit(t, x, y, start, end)
        draws = []
        for _ in range(20):
            c2 = {k: [v[0] + rng.uniform(-6, 6), v[1] + rng.uniform(-6, 6)] for k, v in corners.items()}
            f2 = fieldmap.apply(fieldmap.frame_to_field(c2), np.c_[px, py]); draws.append(fit(t, f2[:, 0], f2[:, 1], start, end)[1])
        err = np.hypot(sa, np.std(draws))
        out.append((name, i, kind, a, err, dur))
        print('| %s | %s | %s | %.0f | %.0f | %.1f s, %.0f in | %.1f ± %.1f | %s |' % (name, i, {'pollen': 'POLLEN', 'red': 'red NECTAR', 'blue': 'blue NECTAR'}[kind], 141.5 - y[start], v0, dur, dist, a, err, how))
a = np.array([q[3] for q in out]); e = np.array([q[4] for q in out]); d = np.array([q[5] for q in out])
print('\nall n=%d: median %.1f, mean %.1f, sd %.1f, IQR %.1f-%.1f' % (len(a), np.median(a), a.mean(), a.std(ddof=1), *np.percentile(a, [25, 75])))
sel = d >= 1.5
print('rolls of 1.5 s or more n=%d: median %.1f, mean %.1f, sd %.1f, IQR %.1f-%.1f, weighted mean %.1f' % (sel.sum(), np.median(a[sel]), a[sel].mean(), a[sel].std(ddof=1), *np.percentile(a[sel], [25, 75]), (a[sel]/e[sel]**2).sum()/(1/e[sel]**2).sum()))
for k in ('pollen', 'red', 'blue'):
    b = np.array([q[3] for q in out if q[2] == k])
    if len(b): print('  %-6s n=%d median %.1f mean %.1f' % (k, len(b), np.median(b), b.mean()))
