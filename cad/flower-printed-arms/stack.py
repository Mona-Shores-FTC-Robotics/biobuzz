"""The FLOWER stick's stacks along its two axes, the pivot and the tip, as the 31 Oct CAD has them, with the printed
arm in the channel's place: what sits where across the robot (Y), so the hubs, bearings and shafts can be checked
against the printed arm.

    python3 cad/flower-printed-arms/stack.py     # writes render_stack.png

Every part within 20 mm of an axis, from cad/advantagescope/Robot_BIOBUZZ (model_1.glb: the stick; model.glb: the rest
of the robot, the parts that touch the axis). This is the CAD of 31 Oct, not today's robot: 20245's 25 Nov photo shows
a second plate over the arm's tip carrying the roller on a standoff, which the CAD doesn't have.
"""
import os
import sys

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
import trimesh  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from geometry import ARM_T, ARM_Y, PIVOT_XZ, STOWED_DIR, TIP_U  # noqa: E402

MODEL = os.path.join(HERE, "..", "advantagescope", "Robot_BIOBUZZ")
NAMES = {
    "Servo - Speed (SWYFT)": "SWYFT servo (stick drive)",
    "Compact Servo Block (GB)": "goBILDA Compact Servo Block",
    "Servo Hub Shaft - 16mm (GB)": "Servo Hub Shaft, 16 mm",
    "32mm Steel Shaft - 8mm REX (GB)": "8 mm REX shaft, 32 mm (left stub)",
    "Sonic Hub - 8mm  REX  (GB)": "Sonic Hub, 8 mm REX",
    "Mini Quad Block (GB) ": "Mini Quad Block",
    "8t Press Fit Sprocket - 8mm REX Bore (GB)": "8T press-fit sprocket, 8 mm REX bore",
    "8x14x4mm Bearing": "8x14x4 bearing",
    "312mm Steel Shaft - 8mm REX (GB)": "8 mm REX shaft, 312 mm",
    "3400-0014-0032": "tip roller",
    "Flower Ramp": "mountain (Flower Ramp)",
    "Servo Block (GB)": "Servo Block (robot centre)",
    "Part 1": "Part 1 (robot centre)",
}


def axis_parts(axis_xz, robot=True):
    out = []
    for f in ("model_1.glb", "model.glb") if robot else ("model_1.glb",):
        s = trimesh.load(os.path.join(MODEL, f))
        for node in s.graph.nodes_geometry:
            if "Mini U-Channel" in node:
                continue                                      # the arms: drawn as the printed arm
            T, g = s.graph[node]
            m = s.geometry[g].copy()
            m.apply_transform(T)
            v = m.vertices * 1000
            r = np.hypot(v[:, 0] - axis_xz[0], v[:, 2] - axis_xz[1])
            near = r < 20
            if near.sum() < 10 or (f == "model.glb" and r.min() > 12):
                continue
            leaf = node.split(" / ")[-1]
            base = leaf.rsplit(" <", 1)[0]
            label = NAMES.get(base, NAMES.get(base.strip(), base))
            out.append((label, float(v[near, 1].min()), float(v[near, 1].max()), f == "model_1.glb"))
    return sorted(out, key=lambda p: -p[1])


def main():
    pivot = PIVOT_XZ
    tip = PIVOT_XZ + TIP_U * STOWED_DIR
    fig, axs = plt.subplots(2, 1, figsize=(12, 9), dpi=110)
    for ax, (title, axis) in zip(axs, (("Pivot axis", pivot), ("Tip axis", tip))):
        parts = axis_parts(axis, robot=(title == "Pivot axis"))   # the robot is static: only the pivot stays put
        rows = [("printed arm, left", *sorted(ARM_Y["left"]), "arm"),
                ("printed arm, right", *sorted(ARM_Y["right"]), "arm")] + \
               [(n, a, b, "stick" if st else "robot") for n, a, b, st in parts]
        colours = {"arm": "#2a5fd0", "stick": "#f0901f", "robot": "#9a9a9a"}
        for i, (n, a, b, kind) in enumerate(rows):
            ax.barh(i, b - a, left=a, color=colours[kind], height=0.6)
            ax.text(b + 3, i, f"{n}   (Y {a:.0f} .. {b:.0f})", va="center", fontsize=7.5)
        for side in ("left", "right"):
            for y in ARM_Y[side]:
                ax.axvline(y, color="#2a5fd0", lw=0.6, ls=":")
        ax.axvline(0, color="#444", lw=0.6)
        ax.set_yticks([])
        ax.set_xlim(-175, 330)
        ax.invert_yaxis()
        ax.set_xlabel("Y, mm (+ left, seen from behind the robot; 0 the robot's middle)")
        ax.set_title(f"{title}: blue the printed arm (12 mm, the channel's band), orange the stick's parts, "
                     f"grey the robot's", fontsize=9)
    fig.suptitle("The FLOWER stick's stacks in the 31 Oct CAD, the printed arm in the channel's place", fontsize=10)
    fig.tight_layout()
    fig.savefig(os.path.join(HERE, "render_stack.png"))


if __name__ == "__main__":
    main()
