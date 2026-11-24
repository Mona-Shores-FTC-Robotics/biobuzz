"""The pictures in doc/corner-wheels/ and the clearances in doc/corner-wheels.md, from the AdvantageScope model of the
Onshape CAD (cad/advantagescope/Robot_BIOBUZZ) and this folder's corner modules (parts.py).

    pip install cadquery trimesh shapely matplotlib scipy
    python3 cad/corner-wheels/drawings.py     # about a minute

Robot frame, inches: +X forward, +Y left, +Z up, origin on the floor under the middle of the drive axles. The FLOWER
stick is swept 0..162 deg in 1 deg steps about its axis from components.json.
"""
import os
import sys
import tempfile

import cadquery as cq
import matplotlib
import numpy as np
import trimesh

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
from matplotlib.collections import PolyCollection  # noqa: E402
from matplotlib.patches import Patch  # noqa: E402
from scipy.spatial import cKDTree  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import parts as P  # noqa: E402

MODEL = os.path.join(HERE, "..", "advantagescope", "Robot_BIOBUZZ")
OUT = os.path.join(HERE, "..", "..", "doc", "corner-wheels")
IN = 0.0254

COLOURS = {"bracket": "#4f81bd", "servo": "#404040", "hub": "#a6a6a6", "wheel": "#2e7d32", "screws": "#000000"}
MOUNT = {"left": "Chassis <1> / Intake <1> / 5 Hole Lowside U-Channel (GB) <2>",
         "right": "Chassis <1> / 2 Hole Lowside U-Channel (GB) <1>"}
DRIVE_WHEEL = {"left": "Wheel Assembly <2>", "right": "Wheel Assembly <4>"}


def robot():
    """The fixed robot's parts (name, triangles in inches) and the FLOWER stick's swept points."""
    out = []
    s = trimesh.load(os.path.join(MODEL, "model.glb"))
    for node in s.graph.nodes_geometry:
        T, g = s.graph[node]
        m = s.geometry[g].copy()
        m.apply_transform(T)
        out.append((node, m.triangles / IN))
    st = trimesh.load(os.path.join(MODEL, "model_1.glb"))
    verts = []
    for node in st.graph.nodes_geometry:
        T, g = st.graph[node]
        m = st.geometry[g].copy()
        m.apply_transform(T)
        verts.append(m.vertices)
    verts = np.vstack(verts)
    ax = np.array([0.15449, 0.13883, 0.18462])
    d = np.array([0.00107, 1, 0])
    d /= np.linalg.norm(d)
    swept = []
    for a in range(0, 163):
        R = trimesh.transformations.rotation_matrix(np.radians(a), d, ax)
        swept.append((verts @ R[:3, :3].T + R[:3, 3]) / IN)
    return out, np.vstack(swept)


def triangles(shape, mirror):
    f = tempfile.mktemp(suffix=".stl")
    cq.exporters.export(shape, f, tolerance=0.1)
    t = trimesh.load(f).triangles / 25.4
    os.remove(f)
    if mirror:
        t = t * np.array([1, -1, 1])
    return t


def module(side):
    m = P.module(side)
    return {name: triangles(shape.val(), side == "right") for name, shape in m.items()}


def clearances(side, mod, fixed, swept):
    TS, TF = cKDTree(swept), cKDTree(fixed)
    for name, t in mod.items():
        p = t.reshape(-1, 3)
        print(f"{side:5s} {name:8s} stick {TS.query(p)[0].min():5.2f}  frame {TF.query(p)[0].min():5.2f}  "
              f"X {p[:, 0].min():5.2f}..{p[:, 0].max():5.2f}  Z {p[:, 2].min():4.2f}..{p[:, 2].max():4.2f}")


def draw(side, mod, rob, swept):
    s = 1 if side == "left" else -1
    fig, (top, elev) = plt.subplots(1, 2, figsize=(13, 6.4), gridspec_kw={"width_ratios": [1, 1.15]})
    ylo, yhi = (3.6, 9.3) if s > 0 else (-9.3, -3.6)
    box = (5.3, 11.0)

    def near(t):
        c = t.mean(1)
        return (c[:, 0] > box[0]) & (c[:, 0] < box[1]) & (c[:, 1] > ylo) & (c[:, 1] < yhi) & (c[:, 2] < 6.5)

    # robot
    for name, t in rob:
        t = t[near(t)]
        if not len(t):
            continue
        col = "#f4a259" if name == MOUNT[side] else "#9e9e9e" if DRIVE_WHEEL[side] in name else "#cfcfcf"
        z = 3 if name == MOUNT[side] else 1
        low = t[t[:, :, 2].min(1) < 4.5]
        top.add_collection(PolyCollection(low[:, :, :2], facecolors=col, edgecolors="none", zorder=z))
        # elevation seen from outboard: only what is outboard of the intake's middle
        e = t[(t[:, :, 1].mean(1) * s) > 4.3]
        elev.add_collection(PolyCollection(e[:, :, [0, 2]], facecolors=col, edgecolors="none", zorder=z))
    # stick swing over the corner
    q = swept[(swept[:, 0] > box[0]) & (swept[:, 1] * s > 4.3) & (swept[:, 2] < 6.5)]
    elev.scatter(q[::5, 0], q[::5, 2], s=1, c="#e07b39", alpha=0.25, zorder=2, linewidths=0)
    qt = q[q[:, 2] < 4.0]
    top.scatter(qt[::5, 0], qt[::5, 1], s=1, c="#e07b39", alpha=0.25, zorder=2, linewidths=0)
    # the module
    order = ["wheel", "hub", "screws", "servo", "bracket"]
    for name in order:
        t = mod[name]
        top.add_collection(PolyCollection(t[:, :, :2], facecolors=COLOURS[name], edgecolors="none", alpha=0.9,
                                          zorder=5 + order.index(name)))
        elev.add_collection(PolyCollection(t[:, :, [0, 2]], facecolors=COLOURS[name], edgecolors="none", alpha=0.9,
                                           zorder=5 + order.index(name)))
    c = P.SIDES[side]
    ax_x, ax_y = c["axle"][0], s * c["axle"][1]
    # labels
    kw = dict(fontsize=8, zorder=20, bbox=dict(fc="white", ec="none", alpha=0.85),
              arrowprops=dict(arrowstyle="->", lw=1.1))
    if side == "left":
        ear_txt = ("EAR: 2 x M4 into the intake's side\nchannel, outer face (Y +5.35),\n"
                   "X 7.60..8.50, Z 2.69..3.65")
        top.annotate(ear_txt, xy=(8.05, 5.42), xytext=(5.5, 4.0), color="#c00000", **kw)
        elev.annotate(ear_txt, xy=(8.05, 3.3), xytext=(5.4, 4.9), color="#c00000", **kw)
        elev.annotate("intake side channel\n(5-hole Lowside U-Channel)", xy=(8.3, 5.0), xytext=(9.2, 6.0), **kw)
    else:
        ear_txt = ("EAR: 2 x M4 into the FRONT face\nof the short channel ahead of the\n"
                   "right drive wheel (X 7.87), Z 2.69..3.65")
        top.annotate(ear_txt, xy=(7.92, -6.3), xytext=(5.4, -4.2), color="#c00000", **kw)
        elev.annotate(ear_txt, xy=(7.95, 3.3), xytext=(5.4, 5.2), color="#c00000", **kw)
        elev.annotate("short 2-hole channel", xy=(7.6, 4.2), xytext=(8.6, 5.9), **kw)
    top.annotate("wheel axle\n(X %.2f, Y %+.2f)" % (ax_x, ax_y), xy=(ax_x, ax_y), xytext=(10.0, ax_y + s * 1.6), **kw)
    top.annotate("front drive wheel", xy=(6.3, s * 6.2), xytext=(5.4, s * 8.8), **kw)
    elev.annotate("servo body, up to Z 3.76", xy=(9.6, 3.6), xytext=(9.4, 4.6), **kw)
    elev.annotate("25T hub", xy=(9.5, 2.0), xytext=(10.1, 2.6), **kw)
    elev.annotate("wheel, Z 0.60..1.65", xy=(10.0, 1.1), xytext=(10.0, 0.25), **kw)
    for a in (top, elev):
        a.axvline(10.43, color="#7030a0", ls="--", lw=1)
        a.grid(True, lw=0.3, alpha=0.5)
        a.set_aspect("equal")
        a.set_xlim(*box)
    top.text(10.46, ylo + 0.1 if s > 0 else yhi - 0.4, "18 in cube front", color="#7030a0", fontsize=7, rotation=90)
    top.set_ylim(ylo, yhi)
    top.set_xlabel("X forward (in)")
    top.set_ylabel("Y left (in)")
    top.set_title("Top view (robot below Z 4.5)", fontsize=9)
    elev.set_ylim(0, 6.5)
    elev.axhline(0, color="k", lw=1)
    elev.set_xlabel("X forward (in)")
    elev.set_ylabel("Z up (in)")
    elev.set_title("Side view from outboard (robot outboard of |Y| 4.3)", fontsize=9)
    hs = [Patch(color="#f4a259", label="what the ear bolts to"), Patch(color="#9e9e9e", label="front drive wheel"),
          Patch(color="#cfcfcf", label="rest of the robot"), Patch(color="#e07b39", alpha=0.5, label="FLOWER stick's swing"),
          Patch(color=COLOURS["bracket"], label="bracket (PETG)"), Patch(color=COLOURS["servo"], label="servo"),
          Patch(color=COLOURS["hub"], label="goBILDA 1908 25T hub"), Patch(color=COLOURS["wheel"], label="TPU wheel")]
    fig.suptitle(f"{side.upper()} corner module, to scale (CAD of 31 Oct; corner_module_{side}.step)", fontsize=10)
    fig.tight_layout(rect=(0, 0.07, 1, 1))
    fig.legend(handles=hs, fontsize=8, loc="lower center", ncol=8, framealpha=0.9)
    fig.savefig(os.path.join(OUT, f"{side}-corner-module.png"), dpi=110)
    plt.close(fig)


def main():
    rob, swept = robot()
    fixed = np.vstack([t.reshape(-1, 3) for _, t in rob])
    os.makedirs(OUT, exist_ok=True)
    for side in ("left", "right"):
        mod = module(side)
        clearances(side, mod, fixed, swept)
        draw(side, mod, rob, swept)


if __name__ == "__main__":
    main()
