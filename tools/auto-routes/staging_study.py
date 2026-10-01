"""Where should a partner that doesn't shoot stage its 4 preloads for us? (Mentor, 1 Oct 2026.)

The partner starts at the left end at x = 19, 24 or 36 (y 132.25, facing the HIVE) and sets its 4
preloads in a row touching it (G304): along its field side, or across its front. Then it either
stays put (it can't move), drives one tile forward (all it can do: from x 19 that lands in the
LOADING ZONE, so it parks), or drives to the far-left end of the LOADING ZONE and parks. A front
row is only tried with a partner that stays, since any other would drive through it.

Ours is three-tip-adaptive with the partner's row in place of the wall FLOWER, taken in one straight
drive with the 24 in catcher. AutoStudyTest.stagedFor reads the row's position from the partner's class name."""
import sys
from helpers import *
from three_tip_adaptive import adaptive

STARTS = (19, 24, 36)
Y0, GAP = 132.25, 9 + 1.4 + 0.2


def partner(x, side, does):
    name = f"partner-stage-{x}-{side}-{does}"
    r = Route(name, (x, Y0, 270), speed=40, folder=PP_DIR + "/partners/staging")
    if does == "stay":
        # Nothing to do; it holds no pieces, so IntakeFull never comes.
        r.add(r.wait("Stay put", when=["IntakeFull"], ms=29500))
    elif does == "forward":
        r.pt("FWD", x, Y0 - 24, 270)  # one tile
        r.add(r.go("FWD", heading=270, park=True))
    else:
        r.pt("PARK_P", 10.5, 108, 270)  # the far-left end of the LOADING ZONE, off the wall
        r.add(r.go("PARK_P", ctrl=[(x, 118)], heading=270, park=True))
    return r


def ours(x, side):
    if side == "side":
        # Our frame 1.5 in clear of the partner's side; the row (y 128-136.5) runs through the outer
        # part of the 24 in intake. Two stops, two pieces each; the second leaves our front 1.8 in
        # short of the far FLOWER's holder.
        cx = x + 19.5
        stops, back = ((cx, 119.5, 900), (cx, 125, 900)), (cx, 108)
    else:
        # Straight at the row (y 121.6); stop with our front 1 in short of the partner's. All 4 lie
        # across the intake at once.
        stops, back = ((x, Y0 - 9 - 1 - 9, 1600),), (x, 100)
    return adaptive(f"staged-three-tip-{x}-{side}", staged=True, stops=stops, back=back)


CASES = [(x, side, does) for x in STARTS for side in ("side", "front") for does in ("stay", "forward", "park")
         if side == "side" or does == "stay"]


def cls(name):
    return "".join(w.capitalize() for w in name.split("-")) + "Auto"


if __name__ == "__main__":
    for x in STARTS:
        for side in ("side", "front"):
            ours(x, side).write()
    for c in CASES:
        partner(*c).write()
    designs = sys.argv[1] if len(sys.argv) > 1 else "two spring hoods, 24 in catcher"
    runs = int(sys.argv[2]) if len(sys.argv) > 2 else 20
    study(";".join(f"{cls(f'staged-three-tip-{x}-{side}')},{cls(f'partner-stage-{x}-{side}-{does}')}@50" for x, side, does in CASES),
          runs=runs, designs=designs, extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})
