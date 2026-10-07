#!/usr/bin/env python3
"""kymo.py CLIP T0 T1 SIDE OUT_PNG: 60 fps kymograph of moving POLLEN in the HIVE's half of the frame.
Yellow pixels that differ from the window's median frame are detected as blobs; each blob is drawn as a dot at
(time, image row) with its x shown by colour (dark=left of the band, bright=right). Horizontal lines mark the CELL
opening rows; a shot is a dot run rising from the robot's rows to the opening and vanishing (in) or coming back (out).
Prints every blob: t, px, py, area."""
import sys, numpy as np
sys.path.insert(0, __file__.rsplit('/', 1)[0]); import frames, blobs
from PIL import Image, ImageDraw
clip, t0, t1, side, out = sys.argv[1], float(sys.argv[2]), float(sys.argv[3]), sys.argv[4], sys.argv[5]
x0, x1 = (440, 800)
y0, y1 = 40, 470
a, ts = frames.read(clip, t0, t1, fps=60)
sub = a[:, y0:y1, x0:x1]
med = np.median(sub[::5], axis=0).astype(np.int16)
W = len(a) * 3; Hh = (y1 - y0)
im = Image.new('RGB', (W, Hh), (20, 20, 20)); d = ImageDraw.Draw(im)
for yy in (60, 130, 200, 260):  # rough: CELL top/bottom rows differ per clip; drawn as guides every 70 px
    d.line([(0, yy), (W, yy)], fill=(70, 70, 70))
print('t       px   py  area')
for i, f in enumerate(sub):
    moving = np.abs(f.astype(np.int16) - med).sum(-1) > 60
    m = blobs.masks(f)['pollen'] & moving
    tmp = np.zeros_like(f); tmp[m] = (255, 200, 0)
    for k, cx, cy, ar, w, h in blobs.blobs(tmp, (0, 0, x1-x0, y1-y0), min_area=12, max_area=900):
        shade = int(80 + 175 * cx / (x1 - x0))
        d.rectangle([i*3, cy-2, i*3+2, cy+2], fill=(shade, shade, 0) if ar < 300 else (shade, 60, 60))
        print('%6.3f %4.0f %4.0f %4d' % (ts[i], cx + x0, cy + y0, ar))
for k in range(int((t1 - t0) * 4) + 1):
    X = int(k * 0.25 * 60 * 3); d.line([(X, Hh-10), (X, Hh)], fill=(255, 255, 255))
    if k % 2 == 0: d.text((X+2, Hh-22), f'{t0 + k*0.25:.2f}', fill=(255, 255, 255))
im.save(out)
