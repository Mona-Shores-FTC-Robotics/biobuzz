"""lean_duo.py with one decision per cycle: after firing, if the CELL has not tipped within 1.5 s,
the right robot fetches from the GARDEN, once, and fires again; if it has tipped, it stays in the
roll path to catch. The left robot empties the far FLOWER for TIP 2 and never goes back; when a
volley doesn't tip its CELL and it caught nothing, it picks up loose pieces near home with the
webcam (CollectSeen) and fires again. Both park in the LOADING ZONE (mentor review: they used to
stand idle from about 19 s and never park); the endgame guard decides when."""
import sys
from helpers import *


def right(name="lean-opp-right", cycles=4):
    r = Route(name, (59, 9.5, 90), speed=50)
    r.pt("HOME_R", 59, 10, 90).pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    r.pt("PARK_R", 14, 90, 90)  # the near end of the LOADING ZONE
    r.add(fire(r, "Fire all 4 preloads (TIP 1)", "Empty", ms=4000),
          r.wait("TIP 1", when=["LeftCellUp"], ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2500))
    for k in range(cycles):
        r.at = "HOME_R"
        fetch = [r.go("GARDEN_IN", ctrl=[(30, 22)]), r.go("GARDEN"),
                 r.wait(f"Collect in the GARDEN ({k + 1})", when=["IntakeFull"], ms=1200),
                 r.go("HOME_R", ctrl=[(20, 18)], heading=90),
                 fire(r, f"Fire again ({k + 1})", "LeftCellUp", ms=2500)]
        r.at = "HOME_R"
        r.add(*waits(r, f"Right CELL up ({k + 1})", "RightCellUp", 8.0),
              fire(r, f"Fire ({k + 1})", "Empty", ms=2000),
              # The GARDEN once only: it holds 4 and is empty after (mentor review).
              r.wait(f"Did it tip? ({k + 1})", when=["LeftCellUp"], ms=1500, yes=[], no=fetch if k == 0 else [],
                     yes_label="Yes: stay and catch", no_label="No: fetch from the GARDEN" if k == 0 else "No: stay"),
              r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2000))
    r.at = "HOME_R"
    r.add(r.go("PARK_R", ctrl=[(24, 14), (18, 50)], park=True))
    return r


def left(name="lean-opp-left", cycles=4):
    r = Route(name, (59, 132.25, 270), speed=50)
    flower_points(r, "FLOWER_L", FAR_FLOWER_AT, 90).pt("HOME_L", 59, 131.75, 270)
    r.pt("GATHER_L", 57, 118, 270)  # clear of the far FLOWER and the centre line, to turn and look
    r.pt("PARK_L", 15, 118, 270)  # the far end of the LOADING ZONE, leaving the near end to the sister
    r.add(r.action("SpinUp"),
          *waits(r, "TIP 1", "LeftCellUp", 7.5),
          fire(r, "Fire the preloads", "Empty"),
          *flower(r, "FLOWER_L", "Collect at the FLOWER", ms=2500),
          *leave_flower(r, "FLOWER_L", "HOME_L"),
          fire(r, "Fire (TIP 2)", "Empty", ms=2000),
          r.wait("Until it tips", when=["RightCellUp"], ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2000))
    for k in range(cycles):
        r.at = "HOME_L"
        # The far FLOWER was emptied for TIP 2: never back to it (mentor review). If a volley doesn't
        # tip the CELL, stand and catch; the next spill comes to this end anyway.
        # Not tipped and nothing caught: look for loose pieces near home rather than stand empty.
        # Home is too close to the far FLOWER to turn and look, so it steps clear first.
        r.at = "HOME_L"
        gather = [r.go("GATHER_L", heading=270),
                  r.wait(f"Loose pieces ({k + 1})", when=["IntakeFull"], ms=3000, alongside="CollectSeen"),
                  r.go("HOME_L", heading=270), fire(r, f"Fire what we found ({k + 1})", "Empty", ms=2000)]
        r.at = "HOME_L"
        r.add(*waits(r, f"Left CELL up ({k + 1})", "LeftCellUp", 8.0),
              fire(r, f"Fire ({k + 1})", "Empty", ms=2000),
              r.wait(f"Did it tip? ({k + 1})", when=["RightCellUp"], ms=1500, yes=[],
                     no=[r.wait(f"Caught any? ({k + 1})", when=["Empty"], ms=100, yes=gather, no=[],
                                yes_label="No: gather", no_label="Yes: fire them")],
                     yes_label="Yes", no_label="No"),
              r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2000))
    r.at = "HOME_L"
    # Straight down first: the far FLOWER is beside home.
    r.add(r.go("PARK_L", ctrl=[(59, 116), (30, 116)], park=True))
    return r


if __name__ == "__main__":
    right().write()
    left().write()
    study("LeanOppRightAuto,LeanOppLeftAuto@50;LeanRightAuto,LeanLeftAuto@50", runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood|two spring hoods, 24 in catcher")
