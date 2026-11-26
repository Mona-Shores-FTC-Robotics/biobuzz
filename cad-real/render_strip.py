import re, numpy as np, matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.collections import PolyCollection
from frame import parts
src = open("strip.py").read()
SKIP = re.compile(re.search(r'SKIP = re.compile\(r"(.*)"\)', src).group(1))
IN = 25.4
STICK = re.compile(r"9 Hole Mini U-Channel|1105-0003-0080|384mm Steel|312mm Steel|Flower Ramp|3400-0014-0032|8t Press Fit|GT2-5 belt|Part 1|8x14x5mm Bearing - Round Bore|8x14x4mm Bearing|Part 4")
PIVOT = np.array([5.755, 7.58])
def kept(n, p):
    lo, hi = p.min(0), p.max(0)
    if SKIP.search(n): return False
    stick = ("Intake Copy 2" in n and hi[0] > 4.5) or any(k in n for k in ("312mm Steel Shaft", "Servo Hub Shaft", "Flower Ramp", "Servo - Speed (SWYFT)", "Compact Servo Block", "8x14x4mm Bearing"))
    return stick or (hi[0] > 5.0 and lo[2] < 9.0 and lo[0] > 1.0)
sel = []
for n, t in parts:
    t = t / IN; p = t.reshape(-1, 3)
    if not kept(n, p): continue
    moving = bool(STICK.search(n)) and (p[:, 0].max() > 8.0 or "GT2" in n or "Mini U-Channel" in n)
    col = "#2a5fd0" if "Mini U-Channel" in n and "9 Hole" in n else ("#e8892a" if moving else ("#222" if "Servo - " in n else "#9aa3ad"))
    sel.append((n, t, moving, col))
def rot(t, deg):
    a = np.radians(deg); R = np.array([[np.cos(a), np.sin(a)], [-np.sin(a), np.cos(a)]])
    q = t.copy(); xz = q[..., [0, 2]] - PIVOT; q[..., [0, 2]] = xz @ R.T + PIVOT; return q
fig, axs = plt.subplots(2, 2, figsize=(14, 11), dpi=110)
for row, (label, deg) in enumerate((("stick down (as drawn)", 0.0), ("stick stowed (swung back 162 deg)", -162.0))):
    for colix, (view, idx, xl) in enumerate((("side, from the left", (0, 2), "X forward (in)"), ("front, from ahead", (1, 2), "Y left (in)"))):
        ax = axs[row, colix]
        order = sorted(sel, key=lambda s: (s[1].reshape(-1, 3)[:, 1].mean() if view.startswith("side") else -s[1].reshape(-1, 3)[:, 0].mean()))
        for n, t, mv, c in order:
            tt = rot(t, deg) if mv else t
            polys = tt[:, :, list(idx)]
            pass
            ax.add_collection(PolyCollection(polys, facecolors=c, edgecolors="none", alpha=0.9 if mv or c != "#9aa3ad" else 0.35))
        ax.autoscale(); ax.set_aspect("equal"); ax.set_title(f"{view}, {label}", fontsize=10)
        ax.set_xlabel(xl if view.startswith("side") else "Y (in): the robot's left on the right, seen from ahead"); ax.set_ylabel("Z up (in)")
        ax.axhline(0, color="#555", lw=0.6)
fig.suptitle("corners_and_stick.step: the robot's front, the intake front and the FLOWER stick (blue: the arms; orange: what swings with them)", fontsize=10)
fig.tight_layout(); fig.savefig("corners_and_stick.png")
