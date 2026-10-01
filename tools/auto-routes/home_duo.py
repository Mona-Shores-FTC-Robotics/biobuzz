"""Each robot stays at its own CELL: catch that CELL's spill, and when the CELL is up again,
fire, refill from the pile in front of it, fire until it tips. No piece crosses the field.

A spill hits the tiles about 1.3 s after the TIP starts and rolls toward the wall in front of the
CELL. A robot standing there, facing the CELL, catches about 4 and stops the rest just in front
of it; a short nudge forward picks those up.
"""
import sys
from helpers import *


def cycle(r, k, up, other_up, home, nudge_pt, heading, stream):
    if stream:
        return [*waits(r, f"CELL up ({k + 1})", up, 12.0),
                r.action("StreamOn"),
                r.go(nudge_pt, heading=heading), r.go(home, heading=heading),
                r.wait(f"Until it tips ({k + 1})", when=["Tip"], ms=3000),
                r.action("StreamOff"),
                r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2500)]
    return [*waits(r, f"CELL up ({k + 1})", up, 12.0),
            fire(r, f"Fire ({k + 1})", "Empty"),
            r.go(nudge_pt, heading=heading), r.wait(f"Refill ({k + 1})", when=["IntakeFull"], ms=400),
            r.go(home, heading=heading),
            fire(r, f"Fire until it tips ({k + 1})", other_up, ms=3000),
            r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2500)]


def right(name="home-right", cycles=3, nudge=10, stream=False, x=59, park=False):
    r = Route(name, (59, 9.5, 90), speed=50, folder=PP_DIR + "/experiments")
    r.pt("HOME_R", x, 10, 90).pt("NUDGE_R", x, 10 + nudge, 90)
    r.add(r.action("LaunchOne"), r.action("LaunchOne"), r.action("LaunchOne"),
          *([r.go("HOME_R", heading=90)] if x != 59 else []),
          r.wait("TIP 1", when=["LeftCellUp"], ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2500))
    for k in range(cycles):
        r.add(*cycle(r, k, "RightCellUp", "LeftCellUp", "HOME_R", "NUDGE_R", 90, stream))
    if park:
        r.pt("PARK_R", 14, 93, 330).pt("PARK_R2", 14, 95, 330)
        r.add(r.go("PARK_R", ctrl=[(24, 14), (10, 50)]), r.go("PARK_R2", park=True))
    return r


def left(name="home-left", cycles=3, nudge=10, stream=False, x=59, park=False):
    r = Route(name, (59, 132.25, 270), speed=50, folder=PP_DIR + "/experiments")
    r.pt("FLOWER_L", 47.4, 127.8, 90).pt("HOME_L", x, 131.75, 270).pt("NUDGE_L", x, 131.75 - nudge, 270)
    r.add(r.action("SpinUp"),
          *waits(r, "TIP 1", "LeftCellUp", 7.5),
          fire(r, "Fire the preloads", "Empty"),
          r.go("FLOWER_L"),
          r.wait("Collect at the FLOWER", when=["IntakeFull"], ms=2500),
          r.go("HOME_L"),
          fire(r, "Fire until it tips (TIP 2)", "RightCellUp", ms=3000),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2500))
    for k in range(cycles):
        r.add(*cycle(r, k, "LeftCellUp", "RightCellUp", "HOME_L", "NUDGE_L", 270, stream))
    if park:
        r.pt("PARK_L", 15, 126, 300).pt("PARK_L2", 15, 124, 300)
        r.add(r.go("PARK_L"), r.go("PARK_L2", park=True))
    return r


if __name__ == "__main__":
    # An experiment, kept for the record: the nudge refill and streaming did not beat lean_duo.py.
    right("home-right").write()
    left("home-left").write()
    right("home-stream-right", cycles=4, stream=True).write()
    left("home-stream-left", cycles=4, stream=True).write()
    study("HomeRightAuto,HomeLeftAuto@50;HomeStreamRightAuto,HomeStreamLeftAuto@50",
          runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "two spring hoods, 24 in catcher")
