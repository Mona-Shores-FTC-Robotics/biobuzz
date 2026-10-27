"""How the poker L is zip-tied to the intake's front channel: one tie each side (two in all). In through the front wall's hole
24 mm from the centre and the lip hole behind it, from outside, angled up and back inside the channel, out through the bearing
hole and the 12 mm roof hole above it, closed on top. Optionally a second each side in from the back lip's hole and out the
same roof hole. -> poker_u_ties.png"""
import os, matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle, Circle, FancyBboxPatch
HERE = os.path.dirname(os.path.abspath(__file__))
U, AL, TIE, TIE2 = "#e0552b", "#c8c8c8", "#1f5fbf", "#2a9d3a"
fig, axs = plt.subplots(1, 2, figsize=(14, 5.6), gridspec_kw={"width_ratios": [1.25, 1]})
# A: from the side, cut 24 mm from the centre through the wall hole, the bearing hole and the roof hole
ax = axs[0]; ax.set_aspect("equal")
ax.add_patch(Rectangle((-48, -12), 48, 12, fc=AL, ec="k", lw=0.8)); ax.add_patch(Rectangle((-45.5, -12.5), 43, 10, fc="white", ec="none"))   # the channel
ax.add_patch(Rectangle((-31, -2.6), 14, 2.7, fc="white", ec="none"))                                                                        # the bearing hole
ax.add_patch(Rectangle((-2.5, -10.15), 2.5, 4.3, fc="white", ec="none")); ax.add_patch(Rectangle((-48, -10.15), 2.5, 4.3, fc="white", ec="none"))   # the lip holes, front and back
ax.add_patch(Circle((-21.3, -24), 17.8, fc="#5b7fd6", ec="k", lw=0.6, alpha=0.8)); ax.text(-21.3, -24, "motor", ha="center", va="center", fontsize=8, color="white")
ax.add_patch(Rectangle((-38, 0), 42.3, 4, fc=U, ec="k", lw=0.8)); ax.add_patch(Rectangle((0.3, -12), 4, 16, fc=U, ec="k", lw=0.8))         # roof and wall
ax.add_patch(Rectangle((-30, -0.1), 12, 4.2, fc="white", ec="none")); ax.add_patch(Rectangle((0.3, -10.15), 4, 4.3, fc="white", ec="none"))   # the roof hole and the wall hole
ax.plot([12, -3.5, -24, -24], [-8, -8, -3, 8], color=TIE, lw=3, solid_capstyle="round")
ax.add_patch(FancyBboxPatch((-27, 7), 5, 3, boxstyle="round,pad=0.3", fc=TIE, ec="k", lw=0.5))
ax.annotate("in from outside: through the wall hole and\nthe lip hole behind it, 8 mm below the top", (10, -8), (6, -30), fontsize=8, color=TIE, arrowprops=dict(arrowstyle="->", color=TIE))
ax.annotate("angled up and back inside; the tip finds\nthe 14 mm bearing hole and comes out the\n12 mm roof hole; close on top", (-24, 6), (-46, 16), fontsize=8, color=TIE, arrowprops=dict(arrowstyle="->", color=TIE))
ax.plot([-56, -45.5, -24, -24], [-8, -8, -3, 8], color=TIE2, lw=2, ls="--"); ax.text(-52, -14, "optional second tie:\nin from the back lip's hole,\nout the same roof hole", ha="center", fontsize=7, color=TIE2)
ax.text(-12, -5.5, "room above the motor", fontsize=7, color="k")
ax.set_xlim(-62, 24); ax.set_ylim(-44, 26); ax.set_xlabel("mm, + ahead of the channel's lip"); ax.set_ylabel("mm, up from the channel's top")
ax.set_title("A. One tie from the side, cut 24 mm from the centre (the same on the other side)", fontsize=9)
# B: the front wall from the front: where the ties go in
ax = axs[1]; ax.set_aspect("equal")
ax.add_patch(Rectangle((-40, 0), 80, 4, fc="#b8431f", ec="k", lw=0.8))                                                                     # the roof's front edge, seen from the front
ax.add_patch(Rectangle((-40, -12), 80, 12, fc=U, ec="k", lw=0.8)); ax.add_patch(Rectangle((-9.5, -21.6), 19, 45.7, fc="#b8431f", ec="k", lw=0.8))   # the wall and the poke
ax.text(0, -26, "the poke", ha="center", fontsize=8)
for y in (-32, -24, -16, 16, 24, 32): ax.add_patch(Circle((y, -8), 2.15, fc="white", ec="k", lw=0.6)); ax.text(y, 7, f"{abs(y)}", ha="center", fontsize=7)
for y, c in ((24, TIE), (-24, TIE2)):
    ax.add_patch(Circle((y, -8), 2.15, fc=c, ec="k", lw=0.6)); ax.annotate("", (y, 12), (y, -8), arrowprops=dict(arrowstyle="-", color=c, lw=0, alpha=0))
    ax.add_patch(FancyBboxPatch((y - 2.5, 12), 5, 3, boxstyle="round,pad=0.3", fc=c, ec="k", lw=0.5)); ax.text(y, 17.5, "head here,\non the roof", ha="center", fontsize=7, color=c)
ax.text(0, -35, "the tie goes in the hole at 24 each side; the holes at 16 and 32 are spares\n(and show the lip's holes through, which is the centring check)", ha="center", fontsize=8)
ax.set_xlim(-45, 45); ax.set_ylim(-40, 24); ax.set_xlabel("mm from the channel's centre"); ax.set_ylabel("mm, up from the channel's top")
ax.set_title("B. The front wall from the front: where each tie goes in", fontsize=9)
fig.suptitle("Zip-tying the poker L: two ties, one each side. In through the wall hole at 24 from the outside, up and out the roof hole, closed on top. Tails cut flush.", fontsize=10)
fig.tight_layout(); fig.savefig(os.path.join(HERE, "poker_u_ties.png"), dpi=120); print("poker_u_ties.png")
