"""The space budget at a glance: top and side views of every part's box and every reserved volume, in about 20 s.

    python3 cad/printed-chassis/budget.py          # writes views/budget.png and prints what intrudes on a reserve

For deciding where things go before drawing them properly: change a constant in build.py, run this, look. It draws
bounding boxes, not shapes, so two boxes overlapping here is a question, not a clash; build.py's checks answer it.
"""
import os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import build as B
import matplotlib; matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle

GROUP = {"frame": "#888888", "corner": "#5a5a5a", "launcher": "#3a7bd5", "lane": "#2aa198", "intake": "#859900",
         "extractor": "#b58900", "elec": "#d33682", "odometry": "#6c71c4", "front": "#cb4b16"}

def colour(k, p):
    if k.startswith("lm_"): return "#9db8e6"                  # the mentor's launcher module, until step 2 re-houses it
    for g, c in GROUP.items():
        if p["module"].startswith(g): return c
    return "#999999"

def main():
    boxes = [(k, B.bb(p["shape"]), colour(k, p)) for k, p in B.PARTS.items()]
    res = [(nm, B.bb(w), what) for nm, (w, _, what) in B.RESERVED.items()]
    fig, axes = plt.subplots(2, 1, figsize=(11, 12), dpi=110)
    half = B.START_CUBE / 2
    for ax, (i, j), title in ((axes[0], (0, 2), "From above (X forward, Y left), inches"), (axes[1], (0, 4), "From the left (X forward, Z up), inches")):
        for k, b, c in boxes:
            ax.add_patch(Rectangle((b[i], b[j]), b[i + 1] - b[i], b[j + 1] - b[j], fc=c, ec=c, alpha=0.18, lw=0.6))
        for nm, b, what in res:
            ax.add_patch(Rectangle((b[i], b[j]), b[i + 1] - b[i], b[j + 1] - b[j], fc="none", ec="red", lw=1.6, hatch="//"))
            ax.annotate(nm, (b[i], b[j + 1]), color="red", fontsize=8, va="bottom")
        x0 = (B.FLAP_TIP[0] + B.BACK) / 2
        if j == 2: ax.add_patch(Rectangle((B.FLAP_TIP[0] - B.START_CUBE, -half), B.START_CUBE, B.START_CUBE, fc="none", ec="k", ls="--", lw=1))
        else:
            ax.add_patch(Rectangle((B.FLAP_TIP[0] - B.START_CUBE, 0), B.START_CUBE, B.START_CUBE, fc="none", ec="k", ls="--", lw=1))
            ax.plot([B.BACKSTOP_X, B.FACE + 1], [B.LANE_BOTTOM] * 2, "k-", lw=0.8)        # the lane's floor
            ax.plot([B.COL_X] * 2, [B.LANE_BOTTOM, 12.2], "k:", lw=0.8)                   # the launch column
        ax.set_title(title, fontsize=10); ax.set_aspect("equal"); ax.autoscale_view(); ax.grid(lw=0.3, alpha=0.5)
    handles = [Rectangle((0, 0), 1, 1, fc=c, alpha=0.4) for c in GROUP.values()] + [Rectangle((0, 0), 1, 1, fc="#9db8e6"), Rectangle((0, 0), 1, 1, fc="none", ec="red", hatch="//")]
    axes[0].legend(handles, list(GROUP) + ["mentor's launcher", "reserved"], fontsize=7, loc="upper left", ncol=4)
    fig.tight_layout()
    out = os.path.join(HERE, "views", "budget.png"); os.makedirs(os.path.dirname(out), exist_ok=True)
    fig.savefig(out); print("wrote", out)
    B.space_report()

if __name__ == "__main__":
    main()
