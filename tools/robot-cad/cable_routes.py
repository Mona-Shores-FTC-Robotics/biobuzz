# cable_routes.py ends.json occ.npy: shortest cable runs from the hubs' ports to every device through the occupancy grid (moving parts' sweeps and
# the balls' paths are blocked), preferring runs within 0.4 in of structure (they can be tied there)
import numpy as np, json, sys
from scipy import ndimage
from skimage.graph import MCP_Geometric
# occ.npy: the robot's occupancy at 0.1 in from LO (robot frame, inches), with every moving part's sweep and the balls'
# paths marked. ends.json: {hub: {port: [start, {device: end}]}}. Writes routes.json here.
occ = np.load(sys.argv[2]); P = 0.1; LO = np.array([-7.7, -9.0, 0.0]); N = np.array(occ.shape)
blk = ndimage.binary_dilation(occ, iterations=1)                     # a bundle is about 0.2 in across
X, Y, Z = [LO[k] + (np.arange(N[k]) + 0.5) * P for k in range(3)]
out = (X[:, None, None] < -7.5) | (np.abs(Y)[None, :, None] > 8.6) | (Z[None, None, :] < 0.3) | (Z[None, None, :] > 11.0)
d = ndimage.distance_transform_edt(~occ)
cost = np.where(d <= 4, 1.0, 3.0); cost[blk | out] = np.inf
def vox(p): return tuple(np.clip(np.round((np.asarray(p) - LO) / P - 0.5).astype(int), 0, N - 1))
def snap(p):
    i = np.array(vox(p)); best = None
    for r in range(1, 25):
        sl = tuple(slice(max(0, i[k] - r), min(N[k], i[k] + r + 1)) for k in range(3))
        sub = np.isfinite(cost[sl])
        if sub.any():
            idx = np.argwhere(sub) + [s.start for s in sl]
            best = idx[np.argmin(((idx - i) ** 2).sum(1))]; return tuple(best), r * P
    return None, None
J = json.load(open(sys.argv[1])); res = {}
for hub, runs in J.items():
    for port, (start, devs) in runs.items():
        s, sr = snap(start)
        m = MCP_Geometric(cost); m.find_costs([s])
        for name, tgt in devs.items():
            t, tr = snap(tgt)
            try: path = np.array(m.traceback(t))
            except Exception: print(f'{hub} {port} -> {name}: NO PATH'); continue
            pts = LO + (path + 0.5) * P
            L = np.linalg.norm(np.diff(pts, axis=0), axis=1).sum() + sr + tr
            res[f'{hub}|{port}|{name}'] = dict(len=round(float(L), 1), pts=pts[::5].round(2).tolist() + [pts[-1].round(2).tolist()], snap=[round(sr, 1), round(tr, 1)])
            print(f'{hub:4s} {port:14s} -> {name:28s} {L:5.1f} in  (snaps {sr:.1f}/{tr:.1f})', flush=True)
json.dump(res, open('routes.json', 'w'))
