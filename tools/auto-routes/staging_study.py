"""Where should a partner that doesn't shoot stage its 4 preloads for us? (Mentor, 1 Oct 2026.)

The partner starts at the left end at x = 19, 24 or 36 (y 132.25, facing the HIVE) and sets its 4
preloads in a row touching it (G304): along its field side, or across its front. Then it either
stays put (it can't move), drives a tile and a half forward (all it can do: from x 19 that lands in
the LOADING ZONE, so it parks), or drives to the far-left end of the LOADING ZONE and parks. A front
row only for a partner that stays (any other would drive through it); a side row for one that moves.

Ours is three-tip-adaptive with the partner's row in place of the wall FLOWER, taken in one straight
drive with an 18 in intake (the frame's width). AutoStudyTest.stagedFor reads the row's position from the partner's class name."""
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
        # A tile and a half: from x 19 that ends in the LOADING ZONE (y 94-118), and far enough down
        # that it isn't beside the row when we come for it (one tile left it in our way).
        r.pt("FWD", x, Y0 - 36, 270)
        r.add(r.go("FWD", heading=270, park=True))
    else:
        r.pt("PARK_P", 10.5, 108, 270)  # the far-left end of the LOADING ZONE, off the wall
        r.add(r.go("PARK_P", ctrl=[(x, 118)], heading=270, park=True))
    return r


def ours(x, side, mover):
    if mover:
        # The partner has driven off by the time we get there (about 6 s): stand below its row and let
        # the webcam (CollectSeen) pick it up. An 18 in robot can't take a row beside a partner that is
        # still there: each piece's edge is 9.3 in out from our centre line, and our frame is 9 in, so
        # the frame's corner nudges it aside.
        return adaptive(f"staged-three-tip-{x}-{side}-moved", staged=True, row_in=(round(x + GAP, 1), 114))
    # Straight at a row across the partner's front (y 121.6); stop with our front 1 in short of the
    # partner's. All 4 lie across the intake at once.
    # In straight from below: a diagonal approach sweeps the row aside with the front edge.
    return adaptive(f"staged-three-tip-{x}-{side}", staged=True, stops=((x, 100, 0), (x, Y0 - 9 - 1 - 9, 1600)),
                    back=(x, 102))


# A partner that stays puts its row across its front; one that moves, along its side.
CASES = [(x, "front", "stay") for x in STARTS] + [(x, "side", does) for x in STARTS for does in ("forward", "park")]


def cls(name):
    return "".join(w.capitalize() for w in name.split("-")) + "Auto"


if __name__ == "__main__":
    for x in STARTS:
        ours(x, "front", False).write()
        ours(x, "side", True).write()
    for c in CASES:
        partner(*c).write()
    designs = sys.argv[1] if len(sys.argv) > 1 else "two spring hoods, full-width intake"
    runs = int(sys.argv[2]) if len(sys.argv) > 2 else 20
    study(";".join(f"{cls(f'staged-three-tip-{x}-{side}' + ('-moved' if does != 'stay' else ''))},{cls(f'partner-stage-{x}-{side}-{does}')}@50"
                   for x, side, does in CASES),
          runs=runs, designs=designs, extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})
