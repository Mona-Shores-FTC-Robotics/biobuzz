"""Two robots, each at its own CELL, as lean as it gets: catch your CELL's spill, and as soon as
your CELL is up again fire everything; repeat until the end. No AUTO PARK: the cycles use the whole
30 s, and parking in endgame still earns the SWARM points."""
import sys
from helpers import *


def south(name="lean-south", cycles=5, park=False):
    r = Route(name, (59, 9.5, 90), speed=50)
    r.pt("HOME_S", 59, 10, 90).pt("PARK_S", 14, 93, 330).pt("PARK_S2", 14, 95, 330)
    r.add(r.action("LaunchOne"), r.action("LaunchOne"), r.action("LaunchOne"),
          r.wait("TIP 1", when=["LeftCellUp"], ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2500))
    for k in range(cycles):
        r.add(*waits(r, f"South CELL up ({k + 1})", "RightCellUp", 8.0),
              fire(r, f"Fire ({k + 1})", "Empty", ms=2000),
              r.wait(f"Until it tips ({k + 1})", when=["LeftCellUp"], ms=2500),
              r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2000))
    if park:
        r.add(r.go("PARK_S", ctrl=[(24, 14), (10, 50)]),
              *waits(r, "North CELL up", "LeftCellUp", 3.0),
              fire(r, "Fire from the LOADING ZONE", "Empty"),
              r.go("PARK_S2", park=True))
    return r


def north(name="lean-north", cycles=5, park=False):
    r = Route(name, (59, 132.25, 270), speed=50)
    flower_points(r, "FLOWER_N", FAR_FLOWER_AT, 90).pt("HOME_N", 59, 131.75, 270).pt("PARK_N", 15, 126, 300).pt("PARK_N2", 15, 124, 300)
    r.add(r.action("SpinUp"),
          *waits(r, "TIP 1", "LeftCellUp", 7.5),
          fire(r, "Fire the preloads", "Empty"),
          *flower(r, "FLOWER_N", "Collect at the FLOWER", ms=2500),
          *leave_flower(r, "FLOWER_N", "HOME_N"),
          fire(r, "Fire (TIP 2)", "Empty", ms=2000),
          r.wait("Until it tips", when=["RightCellUp"], ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2000))
    for k in range(cycles):
        r.add(*waits(r, f"North CELL up ({k + 1})", "LeftCellUp", 8.0),
              fire(r, f"Fire ({k + 1})", "Empty", ms=2000),
              r.wait(f"Until it tips ({k + 1})", when=["RightCellUp"], ms=2500),
              r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2000))
    if park:
        r.add(r.go("PARK_N"),
              *waits(r, "North CELL up", "LeftCellUp", 3.0),
              fire(r, "Fire from the LOADING ZONE", "Empty"),
              r.go("PARK_N2", park=True))
    return r


if __name__ == "__main__":
    south().write()
    north().write()
    study("LeanSouthAuto,LeanNorthAuto@50;LeanSouthAuto,LeanNorthAuto@60", runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood|two spring hoods|spring hood, 24 in catcher|two spring hoods, 24 in catcher")
