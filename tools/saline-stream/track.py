#!/usr/bin/env python3
"""track.py CLIP T0 T1 CORNERS_JSON OUT_CSV
Detect coloured pieces at 20 fps in the field's floor band, link them into tracks (constant-velocity prediction,
same colour, gate 14 px + half the last step), convert to field inches with the clip's homography."""
import sys, json, csv, numpy as np
sys.path.insert(0, __file__.rsplit('/', 1)[0])
import frames, blobs, fieldmap
clip, t0, t1, cj, outcsv = sys.argv[1], float(sys.argv[2]), float(sys.argv[3]), sys.argv[4], sys.argv[5]
corners = json.load(open(cj)); H = fieldmap.frame_to_field(corners)
REGION = (120, 280, 1180, 560)
a, ts = frames.read(clip, t0, t1)
dets = [blobs.blobs(f, REGION) for f in a]
tracks, active = [], []
for fi, (t, d) in enumerate(zip(ts, dets)):
    cands = []
    for ti, tr in enumerate(active):
        pts = tr['pts']; lt, lx, ly, la = pts[-1]
        if len(pts) > 1 and fi - tr['last'] == 1:
            vx, vy = lx - pts[-2][1], ly - pts[-2][2]
        else: vx = vy = 0.0
        step = np.hypot(vx, vy); px, py = lx + vx, ly + vy
        for j, (k, x, y, ar, w, h) in enumerate(d):
            if k != tr['kind']: continue
            dd = np.hypot(x - px, y - py)
            if dd < 14 + 0.5 * step: cands.append((dd, ti, j))
    cands.sort(); used_t, used_d = set(), set()
    for dd, ti, j in cands:
        if ti in used_t or j in used_d: continue
        k, x, y, ar, w, h = d[j]; active[ti]['pts'].append((t, x, y, ar)); active[ti]['last'] = fi; used_t.add(ti); used_d.add(j)
    for j, (k, x, y, ar, w, h) in enumerate(d):
        if j not in used_d:
            tr = {'kind': k, 'pts': [(t, x, y, ar)], 'last': fi}; tracks.append(tr); active.append(tr)
    active = [tr for tr in active if fi - tr['last'] <= 2]
with open(outcsv, 'w', newline='') as f:
    w = csv.writer(f); w.writerow(['track', 'kind', 't', 'px', 'py', 'area', 'fx_in', 'fy_in'])
    for i, tr in enumerate(tracks):
        if len(tr['pts']) < 4: continue
        for t, x, y, ar in tr['pts']:
            fx, fy = fieldmap.apply(H, [(x, y)])[0]
            w.writerow([i, tr['kind'], f'{t:.3f}', f'{x:.1f}', f'{y:.1f}', ar, f'{fx:.1f}', f'{fy:.1f}'])
print('frames', len(a), 'tracks>=4', sum(len(t['pts']) >= 4 for t in tracks))
