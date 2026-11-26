import pickle, numpy as np
raw = pickle.load(open("tris.pkl", "rb"))
axles = [t.reshape(-1, 3) for n, t in raw if "Wheel Assembly" in n and "Shaft" in n]
c = np.array([(p.min(0) + p.max(0)) / 2 for p in axles]).mean(0)
floor = c[1] + 48.0
def to_robot(t):            # robot mm (fwd, left, up) = STEP (-Z, X, -Y)
    q = np.empty_like(t); q[..., 0] = -(t[..., 2] - c[2]); q[..., 1] = t[..., 0] - c[0]; q[..., 2] = -(t[..., 1] - floor); return q
parts = [(n, to_robot(t)) for n, t in raw]
