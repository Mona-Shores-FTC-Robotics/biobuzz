"""The 15 Oct concept, frame by frame: Block A on the sled drives into the FLOWER steep face first, with its ramp
facing the robot. Once the block's top corner is under the bottom POLLEN, the ball rides onto the flat top and rolls
down the ramp, straight out of the FLOWER perpendicular to the wall, into the intake; the stack drops one and the
next ball follows. Writes stack_sequence.png: six schematic frames in the FLOWER's slice from the field CAD.

    python3 cad/modules/flower-stick-proto/stack_sequence.py

Schematic, not simulated: the ball positions are placed by hand to show the intended mechanism. The 2-D simulator
(tools/ramp-hook/ramp.py with this block's profile, see --sim) reaches frame 3 on its own because its block cannot
stall; what it shows after that is in the page's text."""
import argparse, os, sys
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Circle, Polygon, Rectangle
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", "..", ".."))
R = 1.4
# Block A as printed (cad/ramp-hook parts.py, block.py --shaft 0.770 --bottom 0.52 --top 1.30): x behind the leading
# tip (toward the robot), z above the tiles. Steep face first: a 0.12 in land, the face leaning 0.2 in over the rest of
# its height (17 deg from vertical), the 0.5 in flat top, the 41 deg ramp down to the sharp edge on the robot's side.
BOTTOM, TOP, LAND, SLANT, FLAT, DEPTH = 0.52, 1.30, 0.12, 0.2, 0.5, 1.4
PROFILE = [(0.0, BOTTOM), (0.0, BOTTOM + LAND), (SLANT, TOP), (SLANT + FLAT, TOP), (DEPTH, BOTTOM)]
ROD_BEHIND_TIP = 12 / 25.4
PIN = 0.35            # a lifted POLLEN is pinned here by the uprights' inner corners (field CAD)
# frames: (tip x in the FLOWER frame, ball centres, caption, arrows)
D = 2 * R
FRAMES = [
    (-3.0, [(0, R), (0, R + D), (0, R + 2 * D), (0, R + 3 * D)], "1. approach: steep face first, ramp toward the robot", [("tip", 0.9)]),
    (-0.88, [(PIN, R), (PIN, R + D), (PIN, R + 2 * D), (PIN, R + 3 * D)], "2. contact: the face meets the ball below its middle\n(the real block stalls here; a wiggle gets it past)", [("tip", 0.6)]),
    (0.85, [(PIN, TOP + R), (PIN, TOP + R + D), (PIN, TOP + R + 2 * D), (PIN, TOP + R + 3 * D)], "3. under it: the ball is up on the flat top", [("tip", 0.5)]),
    (1.75, [(-0.75, R), (PIN, 4.1), (PIN, 4.1 + D), (PIN, 4.1 + 2 * D)], "4. out: it rolls down the ramp, straight away from the wall;\nthe stack drops", [("ball", 0), ("down", 1)]),
    (1.75, [(-3.6, R), (-0.75, R), (PIN, 4.1), (PIN, 4.1 + D)], "5. the next ball follows the same way", [("ball", 0), ("ball", 1), ("down", 2)]),
    (1.75, [(-9.8, R), (-7.0, R), (-4.2, R), (-1.4, R)], "6. all four out, rolling toward the intake", [("ball", 1), ("ball", 2), ("ball", 3)]),
]


def draw_flower(ax):
    ax.add_patch(Rectangle((-8, -0.3), 11.2, 0.3, color="#8b96a1"))
    ax.add_patch(Rectangle((-2.40, 0), 0.86, 0.35, color="#3b4148"))      # ring, in front of the hole
    ax.add_patch(Rectangle((1.54, 0), 0.93, 0.35, color="#3b4148"))       # ring, behind
    ax.add_patch(Rectangle((1.18, 0.35), 1.28, 3.75, color="#56636f"))    # upright
    ax.add_patch(Rectangle((-2.41, 3.90), 0.87, 1.35, color="#3b4148"))   # lintel
    ax.add_patch(Rectangle((1.54, 3.90), 1.06, 1.35, color="#3b4148"))    # bracket, behind the hole
    ax.add_patch(Rectangle((-2.25, 4.25), 1.05, 3.0, color="#5fae58", alpha=.4))  # pipe (beside the slice)
    ax.add_patch(Rectangle((2.71, -0.3), 0.5, 7.6, color="#8b96a1"))     # wall


def main():
    ap = argparse.ArgumentParser(); ap.add_argument("--sim", action="store_true", help="also run the 2-D simulator with this block and print where its balls end up")
    a = ap.parse_args()
    fig, axes = plt.subplots(2, 3, figsize=(13.5, 8.4), sharex=True, sharey=True)
    for ax, (tipx, balls, cap, arrows) in zip(axes.flat, FRAMES):
        draw_flower(ax)
        pts = [(tipx - x, z) for x, z in PROFILE]
        ax.add_patch(Polygon(pts, closed=True, color="#d9711c"))
        ax.add_patch(Circle((tipx - ROD_BEHIND_TIP, 0.77), 0.16, color="#8b96a1", ec="#17212b", lw=0.8))
        for (x, z) in balls:
            ax.add_patch(Circle((x, z), R, facecolor="#d8c21a", edgecolor="#17212b", alpha=.75))
        for kind, v in arrows:
            if kind == "tip":
                ax.annotate("", (tipx + v, 0.25), (tipx - 0.3, 0.25), arrowprops=dict(arrowstyle="->", color="#d9711c", lw=2))
            elif kind == "ball":
                x, z = balls[v]; ax.annotate("", (x - 1.9, z), (x - R - 0.1, z), arrowprops=dict(arrowstyle="->", color="#17212b", lw=1.5))
            elif kind == "down":
                x, z = balls[v]; ax.annotate("", (x, z - R - 0.9), (x, z - R - 0.1), arrowprops=dict(arrowstyle="->", color="#17212b", lw=1.5))
        ax.set_xlim(-7.5, 3.3); ax.set_ylim(-0.3, 7.3); ax.set_aspect("equal"); ax.grid(alpha=.2)
        ax.set_title(cap, fontsize=9.5)
    for ax in axes[1]:
        ax.set_xlabel("inches ahead of the FLOWER's centre (robot side is negative)", fontsize=9)
    for ax in axes[:, 0]:
        ax.set_ylabel("inches above the tiles", fontsize=9)
    axes[0][0].text(-7.2, 6.4, "wall at the right; the robot pushes from the left", fontsize=8, color="#56636f")
    fig.suptitle("The 15 Oct concept, frame by frame: Block A on the sled, steep face first, ramp toward the robot (schematic, in the field-CAD FLOWER's slice)", fontsize=10.5)
    fig.tight_layout()
    out = os.path.join(HERE, "stack_sequence.png"); fig.savefig(out, dpi=120); print(out)
    if a.sim:
        sys.path.insert(0, os.path.join(ROOT, "tools", "ramp-hook")); import ramp
        stop = ramp.use_cad_flower() + 0.57        # the curved tip passes the uprights' forward points and meets their inner corners
        tr = []
        r = ramp.run(ring=0.35, drive=4.0, tip_depth=stop, e=0.3, mu=0.4, profile=PROFILE, trace=tr, t_max=5.0)
        print("simulator (drive 4 in/s, mu 0.4): last out %s, last fed %s, left in %d" % r)
        t, tip, balls = tr[-1]
        print("  ends with the balls at " + ", ".join(f"({ramp.CX - x:.2f}, {z:.2f})" for x, z in balls))


if __name__ == "__main__":
    main()
