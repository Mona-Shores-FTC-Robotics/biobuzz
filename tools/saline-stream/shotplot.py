#!/usr/bin/env python3
"""shotplot.py CLIP T0 T1 KYMO_TXT OUT_PNG: draw the kymograph's blob tracks (linked as shotlist.py does) that rise or
fall through at least 80 rows on the window's median frame of the HIVE band (x 440-800, y 40-470), labelled with the
track's start time; a rising track that ends high is a shot that stayed in, one that comes back down bounced out."""
import sys, numpy as np
sys.path.insert(0, __file__.rsplit('/', 1)[0]); import frames
from PIL import Image, ImageDraw
clip, t0, t1, kymo, out = sys.argv[1], float(sys.argv[2]), float(sys.argv[3]), sys.argv[4], sys.argv[5]
x0, x1, y0, y1 = 440, 800, 40, 470
a, ts = frames.read(clip, t0, t1, fps=10)
med = np.median(a[:, y0:y1, x0:x1], axis=0).astype(np.uint8)
im = Image.fromarray(med).convert('L').convert('RGB').resize(((x1-x0)*2, (y1-y0)*2), Image.LANCZOS); d = ImageDraw.Draw(im)
rows = [tuple(map(float, l.split())) for l in open(kymo) if l.strip() and l.strip()[0].isdigit()]
frames_ = {}
for t, x, y, ar in rows:
    if t0 <= t <= t1: frames_.setdefault(round(t, 3), []).append((x, y, ar))
tracks, active = [], []
for t in sorted(frames_):
    dd_ = frames_[t]; used = set()
    for tr in active:
        lt, lx, ly, _ = tr[-1]; vx, vy = (lx - tr[-2][1], ly - tr[-2][2]) if len(tr) > 1 else (0, 0)
        best = None
        for j, (x, y, ar) in enumerate(dd_):
            if j in used: continue
            dist = np.hypot(x - lx - vx, y - ly - vy)
            if dist < 22 and (best is None or dist < best[0]): best = (dist, j)
        if best: j = best[1]; tr.append((t, *dd_[j])); used.add(j)
    for j, (x, y, ar) in enumerate(dd_):
        if j not in used: tr = [(t, x, y, ar)]; tracks.append(tr); active.append(tr)
    active = [tr for tr in active if t - tr[-1][0] < 0.04]
n = 0
for tr in tracks:
    ys = [p[2] for p in tr]
    if len(tr) < 5 or max(ys) - min(ys) < 80: continue
    n += 1; h = (tr[0][0] - t0) / (t1 - t0)
    col = (int(255*min(1, 2*h)), int(255*(1-abs(2*h-1))), int(255*min(1, 2*(1-h))))
    pts = [((p[1]-x0)*2, (p[2]-y0)*2) for p in tr]
    d.line(pts, fill=col, width=2); d.ellipse([pts[0][0]-5, pts[0][1]-5, pts[0][0]+5, pts[0][1]+5], outline=col, width=2)
    d.text((pts[-1][0]+4, pts[-1][1]), '%.2f-%.2f' % (tr[0][0], tr[-1][0]), fill=col)
    print('track %.2f-%.2f  x %.0f->%.0f  rows %.0f->%.0f (min %.0f)  n=%d' % (tr[0][0], tr[-1][0], tr[0][1], tr[-1][1], tr[0][2], tr[-1][2], min(ys), len(tr)))
d.text((6, 6), '%s %.1f-%.1f s, %d tracks' % (clip.rsplit('/', 1)[-1], t0, t1, n), fill=(255, 255, 0)); im.save(out)
