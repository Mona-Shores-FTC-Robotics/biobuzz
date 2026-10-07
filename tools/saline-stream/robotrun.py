#!/usr/bin/env python3
"""robotrun.py CLIP T_START T1 CORNERS_JSON XMIN XMAX [YMIN YMAX]: a robot's position at 20 fps against the pre-START
background (the frame 0.5 s before T_START). Pixels in the window that differ from the background by more than 80
(RGB sum) are the robot; its floor contact is the bottom (97th percentile row) and centre column of that silhouette,
turned into field inches by the homography. Prints t, px, py, field x, y and the speed over 0.25 s."""
import sys, json, numpy as np
sys.path.insert(0, __file__.rsplit('/', 1)[0]); import frames, fieldmap
clip, ts0, t1, cj, xmin, xmax = sys.argv[1], float(sys.argv[2]), float(sys.argv[3]), sys.argv[4], int(sys.argv[5]), int(sys.argv[6])
ymin, ymax = (int(sys.argv[7]), int(sys.argv[8])) if len(sys.argv) > 8 else (240, 560)
H = fieldmap.frame_to_field(json.load(open(cj)))
bg, _ = frames.read(clip, ts0 - 0.55, ts0 - 0.45, fps=20); bg = bg[0][ymin:ymax, xmin:xmax].astype(np.int16)
a, ts = frames.read(clip, ts0, t1, fps=20)
pos = []
for f, t in zip(a, ts):
    diff = np.abs(f[ymin:ymax, xmin:xmax].astype(np.int16) - bg).sum(-1) > 80
    ys, xs = np.nonzero(diff)
    if len(ys) < 300: pos.append((t, np.nan, np.nan, np.nan, np.nan)); continue
    mx = np.median(xs); sel = np.abs(xs - mx) < 100; ys, xs = ys[sel], xs[sel]
    cx = xmin + np.median(xs); cy = ymin + np.percentile(ys, 97)
    fx, fy = fieldmap.apply(H, [(cx, cy)])[0]; pos.append((t, cx, cy, fx, fy))
print('%6s %5s %5s %6s %6s %6s' % ('t', 'px', 'py', 'fx', 'fy', 'v in/s'))
for k in range(len(pos)):
    t, cx, cy, fx, fy = pos[k]
    v = np.hypot(fx - pos[k-5][3], fy - pos[k-5][4]) / 0.25 if k >= 5 else np.nan
    print('%6.2f %5.0f %5.0f %6.1f %6.1f %6.1f' % (t, cx, cy, fx, fy, v))
