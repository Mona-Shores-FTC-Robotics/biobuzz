"""The poker L's underside on the intake's front channel, from above: the channel's hole pattern (read off goBILDA's mesh of
the 9-hole lowside U-channel) with the L's footprint, its four locating pegs in the web's 4.5 mm holes around the middle
bearing hole, the relief over the two screws, the roof holes over the bearing holes and the lip ties. -> poker_u_holes.png"""
import os, matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle, Circle
HERE = os.path.dirname(os.path.abspath(__file__))
HALF_L, SCREW_Y = 40.0, -32.0                                   # the L; the screws standing out of the web, on one side only
fig, ax = plt.subplots(figsize=(13, 4.2)); ax.set_aspect("equal")
ax.add_patch(Rectangle((-120, -48), 240, 48, fc="#d9d9d9", ec="k", lw=0.8))                      # the web, x across (along the channel), y its depth, lip at y = 0
for k in range(-4, 5):                                                                           # the pattern, 24 mm per bearing hole
    c = 24 * k
    ax.add_patch(Circle((c, -24), 7, fc="white", ec="k", lw=0.5))
    for dx in (-8.2, 8.2):
        for dy in (-8.2, 8.2): ax.add_patch(Circle((c + dx, -24 + dy), 2.25, fc="white", ec="k", lw=0.4))
    for dx in (-8, 8):
        for dy in (-8, -40): ax.add_patch(Circle((c + dx, dy), 2.0, fc="white", ec="k", lw=0.4))
    for dy in (-10.3, -37.7): ax.add_patch(Rectangle((c - 2, dy - 4.35), 4, 8.7, fc="white", ec="k", lw=0.4))
    for dx in (-12, 12): ax.add_patch(Rectangle((c + dx - 2.7, -26), 5.4, 4, fc="white", ec="k", lw=0.4))
for k in range(-15, 16): ax.add_patch(Rectangle((8 * k - 2, -1.2), 4, 1.2, fc="white", ec="k", lw=0.3))   # the lip's holes, seen edge-on
for dy in (-8, -40): ax.add_patch(Circle((SCREW_Y, dy), 3.5, fc="#444", ec="k")); ax.text(SCREW_Y, -46, "screw heads", ha="center", fontsize=7, color="#444")
ax.add_patch(Rectangle((-HALF_L, -38), 2 * HALF_L, 38, fc="#e0552b", ec="#a03a18", lw=1.5, alpha=0.35))   # the L's roof, 38 of the web's 48 mm: the rear 10 mm clear for the stars' servo mount
for y in (-24, 24): ax.add_patch(Circle((y, -24), 6, fc="none", ec="#a03a18", lw=1.5, ls="--"))            # its 12 mm holes over the bearing holes: the ties come up through these
for y in (-24, 24): ax.annotate("", (y, -24), (y, -2.2), arrowprops=dict(arrowstyle="->", color="#1f5fbf", lw=2))   # the tie: in at the wall hole, out the roof hole
ax.add_patch(Rectangle((-9.5, -36), 19, 36, fc="none", ec="#a03a18", lw=1, ls=":")); ax.text(0, -33, "buttress", ha="center", fontsize=7, color="#a03a18")
for dy in (-8, -40):
    for sy in (SCREW_Y, -SCREW_Y): ax.add_patch(Circle((sy, dy), 5, fc="none", ec="green", lw=1.5))     # relief, both sides
for dx in (-8.2, 8.2):
    for dy in (-8.2, 8.2): ax.add_patch(Circle((dx, -24 + dy), 1.95, fc="#1f5fbf", ec="k", lw=0.5))     # the pegs
for y in (-32, -24, -16, 16, 24, 32): ax.add_patch(Circle((y, -2.2), 2.15, fc="none", ec="#a03a18", lw=1.5))
for y in (-24, 24): ax.text(y, 3, "wall holes on the lip's holes", ha="center", fontsize=7, color="#a03a18")
ax.text(0, -52, "orange: the 80 mm L's roof (38 of 48 mm, the back clear for the servo mount), its 12 mm holes over the bearing holes (blue arrows: the ties, in at the wall, out on top)   blue: the four pegs, in the web's 4.5 mm holes around the middle bearing hole\n"
        "green: relief over the screw heads (cut both sides, so the L fits either way round)   the wall's six holes on the lip's holes along the top edge", ha="center", fontsize=8)
ax.set_xlim(-125, 125); ax.set_ylim(-58, 8); ax.axis("off")
ax.set_title("poker_u on the intake's front channel, from above: mm from the channel's centre, the front lip at the top", fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "poker_u_holes.png"), dpi=130); print("poker_u_holes.png")
