#!/usr/bin/env python3
"""spillstats.py CLIP STOP SIDE CORNERS_JSON [--sheet OUT_PNG]
Spill statistics for one TIP whose rocker stopped at STOP (clip seconds), on the SIDE (red|blue) alliance:
  - pieces on the floor before the TIP (detections at STOP-1.0 s) are the baseline and are subtracted;
  - 'first touch' is read from the per-0.05 s count of new floor blobs: the first frame after STOP with >= 2 new blobs;
  - at first touch, +0.5 s, +1.0 s, +2.0 s and +3.0 s: the new blobs' distance from the HIVE wall (far wall, y=141.5)
    and their spread (distance from the first-touch centroid), and how many are within 4 in of any wall.
A piece in the air projects onto the floor too far from the camera (too near the far wall), so the first-touch
distance is a lower bound on the distance from the HIVE wall; the later frames are mostly rolling pieces."""
import sys, json, numpy as np
sys.path.insert(0, __file__.rsplit('/', 1)[0]); import frames, blobs, fieldmap
clip, stop, side, cj = sys.argv[1], float(sys.argv[2]), sys.argv[3], sys.argv[4]
H = fieldmap.frame_to_field(json.load(open(cj)))
REGION = (120, 280, 1180, 560)
a, ts = frames.read(clip, stop - 1.0, stop + 3.6, fps=20)
def floor_pts(f):
    d = blobs.blobs(f, REGION, min_area=20, max_area=700)
    pts = [(k, *fieldmap.apply(H, [(x, y)])[0]) for k, x, y, ar, w, h in d]
    # inside the field, and in the lane in front of the HIVE (|x - centre| < 50 in, more than 20 in from the near wall)
    return [(k, x, y) for k, x, y in pts if 2 < x < 139.5 and 20 < y < 141 and abs(x - 70.75) < 50]
base = floor_pts(a[0]) + floor_pts(a[5]) + floor_pts(a[10])
def new_pts(f):
    out = []
    for k, x, y in floor_pts(f):
        if not any(bk == k and np.hypot(bx - x, by - y) < 5 for bk, bx, by in base): out.append((k, x, y))
    return out
counts = [len(new_pts(f)) for f in a]
ft = None
for i, t in enumerate(ts):
    if t > stop - 0.3 and counts[i] >= 2 and counts[min(i+1, len(a)-1)] >= 2: ft = i; break
if ft is None: print('no spill detected'); sys.exit()
print('first touch (first frame with >=2 new floor blobs): %.2f s = %.2f s after the rocker stopped' % (ts[ft], ts[ft] - stop))
print('new blobs per frame from stop: ' + ' '.join(str(c) for c in counts[int((stop - ts[0]) * 20):]))
c0 = None
for dt in (0.0, 0.25, 0.5, 1.0, 2.0, 3.0):
    i = ft + int(round(dt * 20))
    if i >= len(a): break
    p = new_pts(a[i])
    if not p: print('  +%.2f s: none' % dt); continue
    xy = np.array([(x, y) for _, x, y in p]); dist_wall = 141.5 - xy[:, 1]
    if c0 is None: c0 = xy.mean(0)
    spread = np.hypot(*(xy - c0).T)
    near = np.array([min(x, y, 141.5 - x, 141.5 - y) < 4 for x, y in xy])
    print('  +%.2f s (%.2f): n=%2d  from HIVE wall: min %5.1f  median %5.1f  max %5.1f in | spread from landing centroid: median %5.1f  p90 %5.1f | against a wall %d/%d' % (
        dt, ts[i], len(p), dist_wall.min(), np.median(dist_wall), dist_wall.max(), np.median(spread), np.percentile(spread, 90), near.sum(), len(p)))
    print('     pieces: ' + ' '.join('%s(%.0f,%.0f)' % (k[0], x, y) for k, x, y in p))
if '--sheet' in sys.argv:
    from PIL import Image, ImageDraw
    out = sys.argv[sys.argv.index('--sheet') + 1]; tiles = []
    for dt in (0.0, 0.5, 3.0):
        i = ft + int(round(dt * 20))
        if i >= len(a): break
        im = Image.fromarray(a[i]).crop((120, 230, 1180, 560)); d = ImageDraw.Draw(im)
        for k, x, y in new_pts(a[i]):
            px, py = fieldmap.apply(np.linalg.inv(H), [(x, y)])[0]
            d.ellipse([px-120-9, py-230-9, px-120+9, py-230+9], outline={'pollen': (0, 255, 0), 'red': (255, 0, 255), 'blue': (0, 255, 255)}[k], width=2)
        d.text((4, 4), '%.2f s (+%.2f after first touch)' % (ts[i], dt), fill=(255, 255, 0)); tiles.append(im)
    sheet = Image.new('RGB', (tiles[0].width, tiles[0].height * len(tiles)))
    for j, t_ in enumerate(tiles): sheet.paste(t_, (0, j * t_.height))
    sheet.save(out)
