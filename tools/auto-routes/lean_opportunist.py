"""lean_duo.py with one decision per cycle: after firing, if the CELL has not tipped within 1.5 s,
the robot fetches fresh pieces from the source nearest its end (south: the GARDEN; north: the far
FLOWER), comes back and fires again; if it has tipped, it stays in the roll path to catch."""
import sys
from helpers import *


def south(name="lean-opp-south", cycles=4):
    r = Route(name, (59, 9.5, 90), speed=50)
    r.pt("HOME_S", 59, 10, 90).pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    r.add(r.action("LaunchOne"), r.action("LaunchOne"), r.action("LaunchOne"),
          r.wait("TIP 1", when=["LeftCellUp"], ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2500))
    for k in range(cycles):
        r.at = "HOME_S"
        fetch = [r.go("GARDEN_IN", ctrl=[(30, 22)]), r.go("GARDEN"),
                 r.wait(f"Collect in the GARDEN ({k + 1})", when=["IntakeFull"], ms=1200),
                 r.go("HOME_S", ctrl=[(20, 18)], heading=90),
                 fire(r, f"Fire again ({k + 1})", "LeftCellUp", ms=2500)]
        r.at = "HOME_S"
        r.add(*waits(r, f"South CELL up ({k + 1})", "RightCellUp", 8.0),
              fire(r, f"Fire ({k + 1})", "Empty", ms=2000),
              r.wait(f"Did it tip? ({k + 1})", when=["LeftCellUp"], ms=1500, yes=[], no=fetch,
                     yes_label="Yes: stay and catch", no_label="No: fetch from the GARDEN"),
              r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2000))
    return r


def north(name="lean-opp-north", cycles=4):
    r = Route(name, (59, 132.25, 270), speed=50)
    r.pt("FLOWER_N", 47.4, 130.5, 90).pt("HOME_N", 59, 131.75, 270)
    r.add(r.action("SpinUp"),
          *waits(r, "TIP 1", "LeftCellUp", 7.5),
          fire(r, "Fire the preloads", "Empty"),
          r.go("FLOWER_N"),
          r.wait("Collect at the FLOWER", when=["IntakeFull"], ms=2500),
          r.go("HOME_N"),
          fire(r, "Fire (TIP 2)", "Empty", ms=2000),
          r.wait("Until it tips", when=["RightCellUp"], ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2000))
    for k in range(cycles):
        r.at = "HOME_N"
        fetch = [r.go("FLOWER_N"), r.wait(f"Collect at the FLOWER ({k + 1})", when=["IntakeFull"], ms=1500),
                 r.go("HOME_N"), fire(r, f"Fire again ({k + 1})", "RightCellUp", ms=2500)]
        r.at = "HOME_N"
        r.add(*waits(r, f"North CELL up ({k + 1})", "LeftCellUp", 8.0),
              fire(r, f"Fire ({k + 1})", "Empty", ms=2000),
              r.wait(f"Did it tip? ({k + 1})", when=["RightCellUp"], ms=1500, yes=[], no=fetch,
                     yes_label="Yes: stay and catch", no_label="No: fetch from the FLOWER"),
              r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2000))
    return r


if __name__ == "__main__":
    south().write()
    north().write()
    study("LeanOppSouthAuto,LeanOppNorthAuto@50;LeanSouthAuto,LeanNorthAuto@50", runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood|two spring hoods, 24 in catcher")
