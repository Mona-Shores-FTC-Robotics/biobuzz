#!/usr/bin/env python3
"""Colour blobs of game pieces in a frame: POLLEN (yellow), red NECTAR, blue NECTAR. Pure numpy connected components."""
import numpy as np
def masks(a):
    r, g, b = a[..., 0].astype(int), a[..., 1].astype(int), a[..., 2].astype(int)
    yellow = (r > 150) & (g > 110) & (b < 110) & (r - b > 90) & (g - b > 50) & (r - g < 90)
    red = (r > 150) & (g < 95) & (b < 110) & (r - g > 90)
    blue = (b > 110) & (r < 90) & (b - r > 70) & (b - g > 25)
    return {'pollen': yellow, 'red': red, 'blue': blue}
def label(mask):
    """Two-pass union-find labelling (4-connectivity) on a boolean mask; returns labels (0 = background)."""
    h, w = mask.shape; lab = np.zeros((h, w), np.int32); parent = [0]; nxt = 1
    def find(i):
        while parent[i] != i:
            parent[i] = parent[parent[i]]; i = parent[i]
        return i
    for y in range(h):
        row = mask[y]
        if not row.any(): continue
        xs = np.flatnonzero(row)
        # runs
        starts = xs[np.r_[True, np.diff(xs) > 1]]; ends = xs[np.r_[np.diff(xs) > 1, True]]
        for s, e in zip(starts, ends):
            above = lab[y-1, s:e+1] if y else np.zeros(0, np.int32)
            ids = set(int(v) for v in above[above > 0])
            if not ids:
                parent.append(nxt); cur = nxt; nxt += 1
            else:
                roots = sorted(find(i) for i in ids); cur = roots[0]
                for r_ in roots[1:]: parent[r_] = cur
            lab[y, s:e+1] = cur
    flat = np.array([find(i) for i in range(nxt)], np.int32)
    return flat[lab]
def blobs(a, region, min_area=20, max_area=2500):
    """List of (kind, cx, cy, area, w, h) in frame coordinates for blobs inside region (x0,y0,x1,y1)."""
    x0, y0, x1, y1 = region; sub = a[y0:y1, x0:x1]; out = []
    for kind, m in masks(sub).items():
        lab = label(m)
        if lab.max() == 0: continue
        ids, counts = np.unique(lab[lab > 0], return_counts=True)
        ys, xs = np.nonzero(lab)
        vals = lab[ys, xs]
        order = np.argsort(vals); vals, ys, xs = vals[order], ys[order], xs[order]
        bounds = np.searchsorted(vals, ids)
        for k, (i, c) in enumerate(zip(ids, counts)):
            if c < min_area or c > max_area: continue
            sl = slice(bounds[k], bounds[k] + c); bx, by = xs[sl], ys[sl]
            out.append((kind, x0 + bx.mean(), y0 + by.mean(), int(c), int(bx.max()-bx.min()+1), int(by.max()-by.min()+1)))
    return out
