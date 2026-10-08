"""Labelled side and top views of the transfer, from cad/transfer/build.py's meshes:

    MESH_OUT=transfer_mesh.pkl python3 cad/transfer/build.py && python3 tools/robot-cad/transfer_views.py transfer_mesh.pkl
"""
import pickle, numpy as np, re, sys, importlib.util
import matplotlib; matplotlib.use('Agg'); import matplotlib.pyplot as plt
from matplotlib.collections import PolyCollection
import os
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
sys.path.insert(0, os.path.join(ROOT, 'cad'))
s = importlib.util.spec_from_file_location('tr', os.path.join(ROOT, 'cad', 'transfer', 'build.py')); TR = importlib.util.module_from_spec(s); s.loader.exec_module(TR)
C, F, FACE, IN = TR.C, TR.F, TR.FACE, TR.IN
mesh = pickle.load(open(sys.argv[1], 'rb'))
def model(v): v = np.asarray(v); return np.c_[(v[:, 2] - (FACE - 7.56 * IN)) / IN, (v[:, 0] - C) / IN, (v[:, 1] - F) / IN]
LABELS = [('lane_wall_L', 'lane wall (1/4 in PC)'), ('lane_roller_0', 'TPU rollers, 5 shafts'), ('lane_drive_belt', 'round belt from the roller'),
          ('wall_standoff_R0', 'REX standoffs to the rail'), ('ceiling (', 'ceiling (foam under)'), ('ramp (', 'ramp (in wall slots)'),
          ('feeder (', 'feeder: 72 mm Geckos'), ('feeder_belt', '275 mm belt from the flywheel'), ('jack_pulley', 'jackshaft + 24T pinions'),
          ('feeder_arm_front', 'front swing arm'), ('feeder_arm_rear', 'rear swing arm'), ('gate_servo (', 'gate servo'),
          ('idler_hanger', 'idler on the 9-hole channel'), ('turret_motor (', 'turret motor'), ('turret_belt', '295 mm turret belt'),
          ('pad_plate', 'sprung pad'), ('backstop', 'backstop (slots: the count)'), ('feeder_bridge', 'printed bridge + floor'),
          ('flywheel_motor_L', 'flywheel motor, 6000 RPM'), ('flywheel_belt_L', '315 mm belt'), ('flywheel_motor_bracket_L', 'motor bracket'),
          ('ceiling_post_front_L', 'ceiling post')]
def draw(ax, axes, depth, view, flip=1):
    polys, cols, keys = [], [], []
    for n, m in mesh.items():
        if n.startswith(('screw_', 'nut_')) and view == 'top': continue
        v = model(m['v']); f = np.asarray(m['f'])
        if len(f) == 0: continue
        tri = v[f]
        for t in tri:
            polys.append(t[:, axes]); keys.append(t[:, depth].mean() * flip); cols.append(m['col'])
    o = np.argsort(keys)
    pc = PolyCollection([polys[i] for i in o], facecolors=[cols[i] for i in o], edgecolors='none', linewidths=0)
    ax.add_collection(pc)
    for key, text in LABELS:
        n = next((k for k in mesh if k.startswith(key)), None)
        if n is None: continue
        v = model(mesh[n]['v']); c = v.mean(0)[axes]
        ax.annotate(text, c, xytext=(c[0] + 0.6, c[1] + (0.9 if view == 'side' else 1.2)), fontsize=7, arrowprops=dict(arrowstyle='-', lw=0.5, color='0.3'), color='0.1')
    ax.set_aspect('equal'); ax.autoscale_view(); ax.grid(lw=0.3, alpha=0.4)
fig, ax = plt.subplots(figsize=(13, 6.5)); draw(ax, [0, 2], 1, 'side', flip=1)
ax.set_xlabel('X (in, forward)'); ax.set_ylabel('Z (in, up)'); ax.set_title('Transfer v4, from the left (+Y toward the viewer): the lane, the feeder and the moved flywheel motors')
ax.invert_xaxis(); fig.tight_layout(); fig.savefig(os.path.join(ROOT, 'cad', 'transfer', 'views', 'side.png'), dpi=130)
fig, ax = plt.subplots(figsize=(13, 7)); draw(ax, [0, 1], 2, 'top', flip=1)
ax.set_xlabel('X (in, forward)'); ax.set_ylabel('Y (in, left)'); ax.set_title('Transfer v4 from above (ceiling and flywheel parts drawn too)')
ax.invert_xaxis(); fig.tight_layout(); fig.savefig(os.path.join(ROOT, 'cad', 'transfer', 'views', 'top.png'), dpi=130)
print('ok')
