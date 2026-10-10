# 3D picture of cad/modules/flower-stick-proto on the mentor's robot. Run from the folder module_check.py cached
# raw_base_mesh.pkl in: python3 flower_stick_render.py out.png
import sys, pickle, re, numpy as np, importlib.util, matplotlib
matplotlib.use('Agg'); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
sys.path.insert(0, '/home/user/biobuzz/tools/robot-cad'); import flower
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
FS = load('fs', '/home/user/biobuzz/cad/modules/flower-stick-proto/build.py')
base = pickle.load(open('raw_base_mesh.pkl', 'rb'))
LIGHT = np.array([0.4, -0.5, 0.8]); LIGHT /= np.linalg.norm(LIGHT)
def shade(v, f, col, alpha=1.0):
    tri = v[f]; n = np.cross(tri[:, 1] - tri[:, 0], tri[:, 2] - tri[:, 0])
    n /= (np.linalg.norm(n, axis=1)[:, None] + 1e-12)
    k = 0.45 + 0.55 * np.abs(n @ LIGHT)
    c = np.clip(np.array(col)[None, :] * k[:, None], 0, 1)
    return tri, np.c_[c, np.full(len(c), alpha)]
def scene(ax, box, with_flower, elev, azim, title):
    tris, cols = [], []
    lo, hi = np.array(box[0]), np.array(box[1])
    for p, m in base:
        b = m.bounds
        if (b[1] < lo).any() or (b[0] > hi).any() or re.search(r'Pollen|POLLEN', p): continue
        mm = m
        pass
        t, c = shade(np.asarray(mm.vertices), np.asarray(mm.faces), (0.78, 0.79, 0.82), 0.55); tris.append(t); cols.append(c)
    for n, (wp, col, kind) in FS.parts.items():
        vv, ff = wp.val().tessellate(0.01, 0.2)
        v = np.array([(q.x, q.y, q.z) for q in vv]); f = np.array(ff)
        t, c = shade(v, f, col, 1.0); tris.append(t); cols.append(c)
    if with_flower:
        for n, m in flower.parts(FS.SEAT_D).items():
            if n == 'field_wall' or 'post' in n: continue
            t, c = shade(np.asarray(m.vertices), np.asarray(m.faces), (0.35, 0.75, 0.35), 0.45); tris.append(t); cols.append(c)
    T = np.concatenate(tris); C = np.concatenate(cols)
    ax.add_collection3d(Poly3DCollection(T, facecolors=C, edgecolors='none'))
    ax.set_xlim(lo[0], hi[0]); ax.set_ylim(lo[1], hi[1]); ax.set_zlim(lo[2], hi[2])
    ax.set_box_aspect(hi - lo); ax.view_init(elev, azim); ax.set_axis_off(); ax.set_title(title, fontsize=11)
out = sys.argv[1]
fig = plt.figure(figsize=(16, 7.5))
ax1 = fig.add_subplot(1, 2, 1, projection='3d')
scene(ax1, ([2.5, -8.2, 0], [12.5, 8.2, 9.0]), False, 22, -35, 'On his robot: front-right, from above')
ax2 = fig.add_subplot(1, 2, 2, projection='3d')
scene(ax2, ([4.5, -8.2, 0], [13.0, 8.2, 5.0]), True, 12, -60, 'Seated on a FLOWER (green), low view')
fig.text(0.5, 0.03, 'grey = his robot   silver = goBILDA grid plates + 264 mm REX shaft   gold = Hyper Hubs + collars   '
         'orange = the block   black = M4 screws', ha='center', fontsize=10)
fig.subplots_adjust(left=0, right=1, top=0.93, bottom=0.06, wspace=0)
fig.savefig(out, dpi=110)
