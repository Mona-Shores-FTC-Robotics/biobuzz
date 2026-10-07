#!/usr/bin/env python3
"""Scan a Saline stream clip for START (clock ticks), score changes and rocker motion.

Decodes the clip with ffmpeg at 60 fps, full resolution, and per frame records:
  clock      mean |diff| in the overlay clock box
  hclock     mean |diff| in the HIVE's own clock display
  rscore     mean |diff| in the red score box
  bscore     mean |diff| in the blue score box
  global     mean |diff| of the whole frame, downscaled (camera moves)
  red_cx, red_cy, red_n   centroid and count of "red frame" pixels in the red HIVE region
  blue_cx, blue_cy, blue_n  same for the blue HIVE
  red_xor, blue_xor       pixels whose red/blue-ness changed since the previous frame (rocker motion)
Writes <clip>.npz and prints clock ticks, score changes and motion bursts.
"""
import sys, subprocess, numpy as np

W, H = 1280, 720
CLOCK = (575, 630, 700, 700)      # overlay clock "2:30"
HCLOCK = (600, 150, 695, 180)     # the field clock on the HIVE itself
RSCORE = (465, 645, 535, 705)
BSCORE = (745, 645, 815, 705)
REDHIVE = (470, 30, 680, 230)
BLUEHIVE = (680, 30, 950, 260)


def crop(a, box):
    x0, y0, x1, y1 = box
    return a[y0:y1, x0:x1]


def redmask(a):
    r, g, b = a[..., 0].astype(int), a[..., 1].astype(int), a[..., 2].astype(int)
    return (r > 140) & (g < 100) & (b < 110) & (r - g > 70)


def bluemask(a):
    r, g, b = a[..., 0].astype(int), a[..., 1].astype(int), a[..., 2].astype(int)
    return (b > 120) & (r < 110) & (b - r > 60) & (b - g > 20)


def scan(path):
    cmd = ['ffmpeg', '-v', 'error', '-i', path, '-f', 'rawvideo', '-pix_fmt', 'rgb24', '-']
    p = subprocess.Popen(cmd, stdout=subprocess.PIPE, bufsize=10 ** 8)
    n = W * H * 3
    prev = None
    rows = []
    i = 0
    while True:
        buf = p.stdout.read(n)
        if len(buf) < n:
            break
        a = np.frombuffer(buf, np.uint8).reshape(H, W, 3)
        rm = redmask(crop(a, REDHIVE))
        bm = bluemask(crop(a, BLUEHIVE))
        ys, xs = np.nonzero(rm)
        bys, bxs = np.nonzero(bm)
        row = dict(i=i, red_n=len(xs), red_cx=xs.mean() if len(xs) else np.nan, red_cy=ys.mean() if len(ys) else np.nan,
                   blue_n=len(bxs), blue_cx=bxs.mean() if len(bxs) else np.nan, blue_cy=bys.mean() if len(bys) else np.nan)
        if prev is not None:
            pa, prm, pbm = prev
            for k, box in (('clock', CLOCK), ('hclock', HCLOCK), ('rscore', RSCORE), ('bscore', BSCORE)):
                row[k] = np.abs(crop(a, box).astype(int) - crop(pa, box).astype(int)).mean()
            row['global'] = np.abs(a[::8, ::8].astype(int) - pa[::8, ::8].astype(int)).mean()
            row['red_xor'] = int((rm ^ prm).sum())
            row['blue_xor'] = int((bm ^ pbm).sum())
        else:
            for k in ('clock', 'hclock', 'rscore', 'bscore', 'global', 'red_xor', 'blue_xor'):
                row[k] = 0
        rows.append(row)
        prev = (a, rm, bm)
        i += 1
    p.wait()
    keys = list(rows[0].keys())
    out = {k: np.array([r[k] for r in rows], dtype=float) for k in keys}
    np.savez(path.replace('.mp4', '.npz'), **out)
    return out


def ticks(sig, thresh, min_gap=30):
    idx = np.nonzero(sig > thresh)[0]
    out = []
    for k in idx:
        if not out or k - out[-1] > min_gap:
            out.append(int(k))
    return out


def report(path, d):
    fps = 60.0
    t = lambda k: k / fps
    print(f'== {path}: {len(d["i"])} frames')
    for name, thr in (('clock', 8), ('hclock', 12), ('rscore', 8), ('bscore', 8)):
        tk = ticks(d[name], thr)
        print(f'{name:7s} changes ({len(tk)}): ' + ' '.join(f'{t(k):.3f}' for k in tk[:80]))
    # camera motion: frames where the global diff is large
    g = d['global']
    print(f'global diff: median {np.median(g):.2f}, p95 {np.percentile(g, 95):.2f}, max {g.max():.2f}')
    for name in ('red_xor', 'blue_xor'):
        s = d[name]
        base = np.median(s)
        # bursts: runs of frames above 3x median, merged with 15-frame gaps
        hi = s > max(3 * base, 150)
        runs = []
        k = 0
        while k < len(hi):
            if hi[k]:
                j = k
                while j < len(hi) and (hi[j] or hi[k:j].sum() and (j - np.nonzero(hi[:j])[0][-1]) <= 15):
                    j += 1
                runs.append((k, j))
                k = j
            else:
                k += 1
        runs = [(a, b) for a, b in runs if b - a >= 10]
        print(f'{name} median {base:.0f}; bursts ≥10 frames: ' + ' '.join(f'{t(a):.2f}-{t(b):.2f}(peak {s[a:b].max():.0f})' for a, b in runs[:40]))


if __name__ == '__main__':
    for path in sys.argv[1:]:
        d = scan(path)
        report(path, d)
