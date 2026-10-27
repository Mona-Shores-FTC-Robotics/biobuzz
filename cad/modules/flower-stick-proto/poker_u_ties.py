"""How the poker L is zip-tied to the intake's front channel: four ties. Two through the roof's 4.5 mm tie holes, 16 mm either
side of the centre (down the front one, under the channel's top, up the rear one, closed on top) and one through the front wall each side (in
through the wall hole at 32 and the lip hole behind it, back out at 24, closed on the outside). -> poker_u_ties.png"""
import os, numpy as np, matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle, Circle, FancyBboxPatch
HERE = os.path.dirname(os.path.abspath(__file__))
U, AL, TIE, TIE2 = "#e0552b", "#c8c8c8", "#1f5fbf", "#2a9d3a"
fig, axs = plt.subplots(1, 3, figsize=(17, 5.8), gridspec_kw={"width_ratios": [1.1, 1, 1.1]})
# A: from the side, cut 16 mm from the centre, through the roof's tie holes: a roof tie
ax = axs[0]; ax.set_aspect("equal")
ax.add_patch(Rectangle((-48, -12), 48, 12, fc=AL, ec="k", lw=0.8)); ax.add_patch(Rectangle((-45.5, -12.5), 43, 10, fc="white", ec="none"))   # the channel
for x in (-15.8, -32.2): ax.add_patch(Rectangle((x - 2.25, -2.6), 4.5, 2.7, fc="white", ec="none"))                                        # the web's 4.5 mm holes
ax.add_patch(Circle((-21.3, -24), 17.8, fc="#5b7fd6", ec="k", lw=0.6, alpha=0.8)); ax.text(-21.3, -24, "motor", ha="center", va="center", fontsize=8, color="white")
ax.add_patch(Rectangle((-38, 0), 42.3, 4, fc=U, ec="k", lw=0.8)); ax.add_patch(Rectangle((0.3, -12), 4, 16, fc=U, ec="k", lw=0.8))         # roof and wall
for x in (-15.8, -32.2): ax.add_patch(Rectangle((x - 2.25, -0.1), 4.5, 4.2, fc="white", ec="none"))                                        # the roof's tie holes
ax.add_patch(Rectangle((-36, 4), 40.3, 44, fc="none", ec="#a03a18", lw=1, ls=":")); ax.text(-16, 9, "the buttress is 9.5 mm inboard of this cut", fontsize=7, color="#a03a18")
path = [(-15.8, 8), (-15.8, 4), (-15.8, -4), (-32.2, -4), (-32.2, 4), (-32.2, 8)]
ax.plot(*zip(*path), color=TIE, lw=3, solid_capstyle="round"); ax.plot([-15.8, -32.2], [8, 8], color=TIE, lw=3); ax.add_patch(FancyBboxPatch((-26.5, 7), 5, 3, boxstyle="round,pad=0.3", fc=TIE, ec="k", lw=0.5))
ax.text(-24, 14, "tie 1: down the front tie hole, under the channel's top,\nup the rear tie hole, closed on top (tie 2 the same, 16 mm the other side)", ha="center", fontsize=8, color=TIE)
ax.text(-24, -7.5, "between the top and the motor", ha="center", fontsize=7, color="white")
ax.set_xlim(-50, 20); ax.set_ylim(-44, 22); ax.set_xlabel("mm, + ahead of the channel's lip"); ax.set_ylabel("mm, up from the channel's top")
ax.set_title("A. A roof tie from the side, cut 16 mm from the centre through the tie holes", fontsize=9)
# B: the front wall from the front: the wall ties
ax = axs[1]; ax.set_aspect("equal")
ax.add_patch(Rectangle((-40, -12), 80, 16, fc=U, ec="k", lw=0.8)); ax.add_patch(Rectangle((-9.5, -21.6), 19, 45.7, fc="#b8431f", ec="k", lw=0.8))   # the wall and the poke in front of it
ax.text(0, -26, "the poke", ha="center", fontsize=8)
for y in (-32, -24, -16, 16, 24, 32): ax.add_patch(Circle((y, -8), 2.15, fc="white", ec="k", lw=0.6)); ax.text(y, 2, f"{abs(y)}", ha="center", fontsize=7)
for s, c in ((1, TIE), (-1, TIE2)):
    ax.plot([32 * s, 24 * s], [-8, -8], color=c, lw=3, solid_capstyle="round")                       # the outside run, wall hole to wall hole
    ax.plot([32 * s, 24 * s], [-8, -8], color=c, lw=1, ls=(0, (2, 2)), alpha=0.9)
    ax.add_patch(FancyBboxPatch((28 * s - 2.5, -14.5), 5, 3, boxstyle="round,pad=0.3", fc=c, ec="k", lw=0.5))
ax.annotate("tie 3: in at 32, through the wall and\nthe lip hole behind it, back out at 24,\nhead on the outside, pulled tight", (28, -8), (10, -38), fontsize=8, color=TIE, arrowprops=dict(arrowstyle="->", color=TIE), ha="center")
ax.annotate("tie 4: the mirror of it", (-28, -8), (-28, -38), fontsize=8, color=TIE2, arrowprops=dict(arrowstyle="->", color=TIE2), ha="center")
ax.text(0, 8, "the lip's holes sit right behind the wall's holes: the tie wraps the 4 mm of lip between two holes", ha="center", fontsize=7)
ax.set_xlim(-45, 45); ax.set_ylim(-46, 27); ax.set_xlabel("mm from the channel's centre"); ax.set_ylabel("mm, up from the channel's top")
ax.set_title("B. The two wall ties: the front wall seen from the front", fontsize=9)
# C: side section at 28 mm from the centre: the wall tie in profile, and the alternative round the lip's edge
ax = axs[2]; ax.set_aspect("equal")
ax.add_patch(Rectangle((-48, -12), 48, 12, fc=AL, ec="k", lw=0.8)); ax.add_patch(Rectangle((-45.5, -12.5), 43, 10, fc="white", ec="none"))   # the channel
ax.add_patch(Rectangle((-2.5, -10.15), 2.5, 4.3, fc="white", ec="none"))                                                                    # the lip's hole
ax.add_patch(Circle((-21.3, -24), 17.8, fc="#5b7fd6", ec="k", lw=0.6, alpha=0.8)); ax.text(-21.3, -24, "motor", ha="center", va="center", fontsize=8, color="white")
ax.add_patch(Rectangle((-38, 0), 42.3, 4, fc=U, ec="k", lw=0.8)); ax.add_patch(Rectangle((0.3, -12), 4, 16, fc=U, ec="k", lw=0.8))         # roof and wall
ax.add_patch(Rectangle((0.3, -10.15), 4, 4.3, fc="white", ec="none"))                                                                        # the wall's hole
for px in (-32.2, -15.8): ax.add_patch(Rectangle((px - 1.95, -2), 3.9, 2, fc="#1f5fbf", ec="k", lw=0.5))
ax.text(-24, 6, "pegs", ha="center", fontsize=7, color="#1f5fbf")
ax.plot([9, -3.5], [-8, -8], color=TIE, lw=3, solid_capstyle="round"); ax.add_patch(FancyBboxPatch((6, -10.5), 4, 5, boxstyle="round,pad=0.3", fc=TIE, ec="k", lw=0.5))
ax.annotate("tie 3 in profile: through the wall and the lip,\nturns inside (toward you), back out the next hole", (-3.5, -8), (12, 14), fontsize=8, color=TIE, arrowprops=dict(arrowstyle="->", color=TIE))
ax.plot([9, -3.5, -3.5, 9], [-8, -8, -14, -14], color="#888", lw=2, ls="--"); ax.annotate("if the bend inside is too tight (3.5 mm to the motor):\nin the hole, down round the lip's bottom edge, back out", (-3.5, -14), (12, -30), fontsize=8, color="#555", arrowprops=dict(arrowstyle="->", color="#888"))
ax.set_xlim(-50, 55); ax.set_ylim(-44, 22); ax.set_xlabel("mm, + ahead of the channel's lip"); ax.set_ylabel("mm, up from the channel's top")
ax.set_title("C. A wall tie from the side, cut 28 mm from the centre", fontsize=9)
fig.suptitle("Zip-tying the poker L: four ties. Two through the roof's small tie holes beside the buttress, then one through the wall each side. Heads on top and outside; tails cut flush.", fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "poker_u_ties.png"), dpi=120); print("poker_u_ties.png")
