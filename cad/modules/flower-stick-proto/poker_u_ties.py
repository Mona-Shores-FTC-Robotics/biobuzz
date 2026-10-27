"""How the poker L is zip-tied to the intake's front channel: four ties. Two down the roof holes (each down an outer hole,
under the channel's top inside the cavity, up the middle hole, closed on top) and one through the front wall each side (in
through the wall hole at 32 and the lip hole behind it, back out at 24, closed on the outside). -> poker_u_ties.png"""
import os, numpy as np, matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle, Circle, FancyBboxPatch
HERE = os.path.dirname(os.path.abspath(__file__))
U, AL, TIE, TIE2 = "#e0552b", "#c8c8c8", "#1f5fbf", "#2a9d3a"
fig, axs = plt.subplots(1, 3, figsize=(17, 5.8), gridspec_kw={"width_ratios": [1.1, 1, 1.1]})
# A: looking from the front, cut through the bearing holes (24 mm behind the lip): the roof ties
ax = axs[0]; ax.set_aspect("equal")
ax.add_patch(Rectangle((-60, -2.5), 120, 2.5, fc=AL, ec="k", lw=0.8))                        # the web
for y in (-24, 0, 24): ax.add_patch(Rectangle((y - 7, -2.6), 14, 2.7, fc="white", ec="none"))   # its bearing holes
ax.add_patch(Rectangle((-40, 0), 80, 4, fc=U, ec="k", lw=0.8))                                 # the roof
for y in (-24, 0, 24): ax.add_patch(Rectangle((y - 6, -0.1), 12, 4.2, fc="white", ec="none"))    # the roof holes
ax.add_patch(Rectangle((-60, -40), 120, 33.8, fc="#5b7fd6", ec="k", lw=0.6, alpha=0.8)); ax.text(0, -10, "intake motor, 3.7 mm under the channel's top", ha="center", fontsize=8, color="white")
ax.add_patch(Rectangle((-60, -12), 2.5, 9.5, fc=AL, ec="k", lw=0.6)); ax.add_patch(Rectangle((57.5, -12), 2.5, 9.5, fc=AL, ec="k", lw=0.6))
for s, c in ((-1, TIE), (1, TIE2)):
    y0 = 24 * s
    path = [(y0 - 3 * s, 7), (y0 - 3 * s, 4), (y0 - 3 * s, -3.8), (0 + 3 * s, -3.8), (3 * s, 4), (3 * s, 7)]   # down the outer hole, under the web, up the middle
    ax.plot(*zip(*path), color=c, lw=3, solid_capstyle="round")
    ax.plot([y0 - 3 * s, 3 * s], [7, 7], color=c, lw=3); ax.add_patch(FancyBboxPatch((3 * s - 2.5, 6), 5, 3, boxstyle="round,pad=0.3", fc=c, ec="k", lw=0.5))
ax.text(-24, 12, "tie 1: down the left hole,\nunder the top, up the middle,\nclosed on top", ha="center", fontsize=8, color=TIE); ax.text(24, 12, "tie 2: down the right hole,\nunder the top, up the middle,\nclosed on top", ha="center", fontsize=8, color=TIE2)
ax.text(-12, -5.2, "the ties run in the gap between the top and the motor", ha="center", fontsize=7, color="k")
ax.set_xlim(-44, 44); ax.set_ylim(-14, 18); ax.set_xlabel("mm from the channel's centre"); ax.set_ylabel("mm, up from the channel's top")
ax.set_title("A. The two roof ties: cut through the channel 24 mm behind the lip, seen from the front", fontsize=9)
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
fig.suptitle("Zip-tying the poker L: four ties. Roof ties first (they seat the pegs), then the wall ties. Heads outside and on top; tails cut flush.", fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "poker_u_ties.png"), dpi=120); print("poker_u_ties.png")
