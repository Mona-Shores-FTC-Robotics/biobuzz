#!/usr/bin/env python3
"""trails.py CLIP T_FRAME TRACKS_CSV OUT_PNG [min_path_in] [--ids a,b,c]: draw moving tracks' trails and ids on one frame."""
import sys, csv, numpy as np
from collections import defaultdict
sys.path.insert(0, __file__.rsplit('/', 1)[0]); import frames
from PIL import Image, ImageDraw
args = [a for a in sys.argv[1:]]; ids = None
if '--ids' in args: k = args.index('--ids'); ids = set(args[k+1].split(',')); del args[k:k+2]
clip, tf, csvp, out = args[0], float(args[1]), args[2], args[3]
minpath = float(args[4]) if len(args) > 4 else 8
a, ts = frames.read(clip, tf, tf + 0.06, fps=60)
im = Image.fromarray(a[0]).crop((120, 230, 1180, 560)); d = ImageDraw.Draw(im)
tr = defaultdict(list)
for r in csv.DictReader(open(csvp)): tr[(r['track'], r['kind'])].append((float(r['t']), float(r['px']), float(r['py']), float(r['fx_in']), float(r['fy_in'])))
cols = {'pollen': (0, 255, 0), 'red': (255, 0, 255), 'blue': (0, 255, 255)}
for (i, k), p in tr.items():
    if ids is not None and i not in ids: continue
    p = np.array(p); path = np.hypot(np.diff(p[:, 3]), np.diff(p[:, 4])).sum()
    if path < minpath: continue
    pts = [(x-120, y-230) for x, y in p[:, 1:3]]
    d.line(pts, fill=cols[k], width=2); d.ellipse([pts[0][0]-4, pts[0][1]-4, pts[0][0]+4, pts[0][1]+4], outline=cols[k], width=2)
    d.text((pts[-1][0]+4, pts[-1][1]-12), f'{i} {p[0,0]:.1f}-{p[-1,0]:.1f}', fill=(255, 255, 0))
d.text((4, 4), f'{tf:.2f}', fill=(255, 255, 0)); im.save(out)
