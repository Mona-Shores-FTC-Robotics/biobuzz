"""The print job: one plate with the parts to test next, laid out to fit a 220 x 220 mm bed, plus a top-view picture.

    python3 cad/modules/flower-stick-proto/print_job.py v8  # writes stl/print_job_v8.stl and print_job_v8.png

The two blades sit side by side (flat side down, their 90 mm depth along x), the hex-fit coupon under them. Everything
is in millimetres.
"""
import os
import numpy as np
import trimesh
HERE = os.path.dirname(os.path.abspath(__file__))
GAP = 8.0
import sys
JOB = sys.argv[1] if len(sys.argv) > 1 else "v8"
PARTS = {"three": [("v7_lane", "v7: the shape that works, snug hole"), ("v8_lane", "v8: V nose"), ("v9_lane", "v9: lower peak")],
         "hedge": [("v8_lane", "v8: V nose, peak 1.10"), ("v9_lane", "v9: straight lip, peak 1.00"), ("hex_fit_coupon_7_7.1_7.2_7.3_7.4", "hex-fit coupon for REX: 7.0 to 7.4")],
         "v8": [("v8_lane", "v8: V nose, Carter's lane"), ("v8", "v8 plain (control)"), ("hex_fit_coupon_7_7.1_7.2_7.3_7.4", "hex-fit coupon for REX: 7.0 to 7.4")],
         "v7": [("v7_lane", "v7 with Carter's lane"), ("v7", "v7 plain (control)"), ("hex_fit_coupon_7_7.1_7.2_7.3_7.4", "hex-fit coupon for REX: 7.0 to 7.4")],
         "v5": [("v5_lane", "v5 with Carter's lane"), ("v5", "v5 plain (control)"), ("hex_fit_coupon_7_7.1_7.2_7.3_7.4", "hex-fit coupon for REX: 7.0 to 7.4")],
         "v6": [("v6_lane", "v6: 50 deg nose, Carter's lane"), ("v5_lane", "v5: 60 deg nose, Carter's lane"), ("hex_fit_coupon_7_7.1_7.2_7.3_7.4", "hex-fit coupon for REX: 7.0 to 7.4")],
         "wide": [("mountain_wide", "the mountain blade with Carter's lane, 7.9 in wide"), ("poker_hang", "the poker, hanging"), ("poker_stand", "the poker, standing")],
         "low": [("mountain_wide_ears", "the wide mountain blade with ears and a short tail, for the low arm"), ("poker_u", "the poker U: over the intake's front channel, four zip ties"), ("hex_fit_coupon_7_7.1_7.2_7.3_7.4", "hex-fit coupon (skip if the last one fit)")],
         "next": [("mountain_wide", "the wide mountain blade with Carter's lane, 7.15 mm hex: for the sled test now"), ("poker_u", "the poker U: sits over the intake's front channel; four zip ties through its flange holes")]}[JOB]
ms, labels = [], []
if JOB in ("wide", "low", "next"):      # the blade alone on the first row, the two pokers on their sides on the second
    m = trimesh.load(os.path.join(HERE, "stl", PARTS[0][0] + ".stl")); m.apply_translation(-m.bounds[0]); ms.append(m); labels.append((PARTS[0][1], m.bounds))
    y = m.bounds[1][1] + GAP; x = 0.0
    if JOB == "next": y = 0.0; x = m.bounds[1][0] + GAP     # the U beside the blade: the plate fits a 220 mm bed
    for name, label in PARTS[1:]:
        c = trimesh.load(os.path.join(HERE, "stl", name + ".stl"))
        if name in ("poker_hang", "poker_stand", "poker_hang_1in", "poker_stand_1in", "poker_hang_2.1in", "poker_stand_2.1in"): c.apply_transform(trimesh.transformations.rotation_matrix(-np.pi / 2, [1, 0, 0]))
        if name == "poker_u": c.apply_transform(trimesh.transformations.rotation_matrix(-np.pi / 2, [0, 1, 0]))   # standing on its back wall: the roof vertical, the poke on top; support under the front wall's lip
        c.apply_translation(-c.bounds[0]); c.apply_translation([x, y, 0]); ms.append(c); labels.append((label, c.bounds)); x = c.bounds[1][0] + GAP
else:
    x = 0.0
    for name, label in PARTS[:2]:
        m = trimesh.load(os.path.join(HERE, "stl", name + ".stl")); m.apply_translation(-m.bounds[0]); m.apply_translation([x, 0, 0])
        ms.append(m); labels.append((label, m.bounds)); x = m.bounds[1][0] + GAP
    row_h = max(m.bounds[1][1] for m in ms)
    c = trimesh.load(os.path.join(HERE, "stl", PARTS[2][0] + ".stl"))
    if JOB == "three": c.apply_transform(trimesh.transformations.rotation_matrix(np.pi / 2, [0, 0, 1]))
    c.apply_translation(-c.bounds[0]); c.apply_translation([0, row_h + GAP, 0])
    ms.append(c); labels.append((PARTS[2][1], c.bounds))
plate = trimesh.util.concatenate(ms)
out = os.path.join(HERE, "stl", f"print_job_{JOB}.stl"); plate.export(out)
b = plate.bounds; print(f"{out}: {b[1][0]-b[0][0]:.0f} x {b[1][1]-b[0][1]:.0f} x {b[1][2]-b[0][2]:.0f} mm, {len(ms)} parts, {plate.volume / 1000:.0f} cm3")

import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
# the picture: the plate from above-front, lit, so the blade's nose, lane and ears read as shapes, not as a flat strip
fig = plt.figure(figsize=(9, 7.5)); ax = fig.add_subplot(111, projection="3d")
for m, (label, bb) in zip(ms, labels):
    tri = m.vertices[m.faces]; nrm = m.face_normals; shade = 0.5 + 0.5 * np.clip(nrm @ np.array([0.3, -0.5, 0.8]), 0, 1)
    ax.add_collection3d(Poly3DCollection(tri, facecolors=np.c_[0.88 * shade, 0.45 * shade, 0.12 * shade, np.ones(len(tri))], edgecolors="none"))
    ax.text((bb[0][0] + bb[1][0]) / 2, bb[1][1] + 6, bb[1][2] + 4, label, fontsize=8, ha="center")
ax.plot([-8, b[1][0] + 8, b[1][0] + 8, -8, -8], [-8, -8, b[1][1] + 8, b[1][1] + 8, -8], [0, 0, 0, 0, 0], color="#888", lw=0.8, ls="--")
span = max(b[1][0] + 16, b[1][1] + 16); ax.set_xlim(-8, -8 + span); ax.set_ylim(-8, -8 + span); ax.set_zlim(0, span * 0.5)
ax.view_init(elev=55, azim=-40); ax.set_box_aspect((1, 1, 0.5)); ax.set_axis_off()
ax.set_title(f"print_job_{JOB}.stl: {b[1][0]-b[0][0]:.0f} x {b[1][1]-b[0][1]:.0f} mm, {len(ms)} parts, flat side down" + (", support under the U's front lip only" if JOB == "next" else ", no supports"), fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(HERE, f"print_job_{JOB}.png"), dpi=120); print(f"print_job_{JOB}.png")
