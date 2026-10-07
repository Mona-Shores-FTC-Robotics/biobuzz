#!/usr/bin/env python3
"""Homography between the stream frame and field inches, from the four floor corners of the field.
Field frame here: x along the audience-side (near) wall, left to right as the camera sees it, 0..141.5;
y from the near wall (0) to the far wall (141.5). Pedro's frame is the same square; which wall is red is per match."""
import numpy as np
FIELD = 141.5
def homography(src, dst):
    A = []
    for (x, y), (u, v) in zip(src, dst):
        A.append([x, y, 1, 0, 0, 0, -u*x, -u*y, -u]); A.append([0, 0, 0, x, y, 1, -v*x, -v*y, -v])
    _, _, vt = np.linalg.svd(np.asarray(A, float)); H = vt[-1].reshape(3, 3); return H / H[2, 2]
def frame_to_field(corners):
    """corners: dict near_left, near_right, far_right, far_left -> [px, py]."""
    src = [corners['near_left'], corners['near_right'], corners['far_right'], corners['far_left']]
    dst = [(0, 0), (FIELD, 0), (FIELD, FIELD), (0, FIELD)]
    return homography(src, dst)
def apply(H, pts):
    p = np.asarray(pts, float).reshape(-1, 2); q = np.c_[p, np.ones(len(p))] @ H.T
    return q[:, :2] / q[:, 2:3]
def inches_per_px(H, pt):
    a = apply(H, [pt, (pt[0]+1, pt[1]), (pt[0], pt[1]+1)]); return np.hypot(*(a[1]-a[0])), np.hypot(*(a[2]-a[0]))
