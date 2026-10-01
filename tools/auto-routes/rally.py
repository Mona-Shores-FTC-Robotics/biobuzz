"""Rally: each robot stays at its own end for the whole AUTO and never parks. A raised CELL that has
emptied needs about 8 POLLEN and a robot holds 4, so after each volley that doesn't tip it, the robot
strafes along the line of spilled pieces stopped in front of it (west, never across the centre line),
refills and fires again. lean-opp fetched from the GARDEN or a FLOWER instead: about 6 s against 1.5."""
import sys
from helpers import *


def cycle(r, k, home, sweep, ours, other, heading):
    """Wait for our CELL, fire; if it hasn't tipped, sweep the spill line and fire again; catch the spill."""
    refill = [r.go(sweep, heading=heading), r.wait(f"Refill from the line ({k})", when=["IntakeFull"], ms=1200),
              r.go(home, heading=heading), fire(r, f"Fire again ({k})", other, ms=2500)]
    r.at = home
    return [r.wait(f"Our CELL up ({k})", when=[ours], ms=9000),
            fire(r, f"Fire ({k})", "Empty", ms=2000),
            r.wait(f"Did it tip? ({k})", when=[other], ms=900, yes=[], no=refill,
                   yes_label="Yes: stay and catch", no_label="No: refill from the line"),
            r.wait(f"Catch the spill ({k})", when=["IntakeFull"], ms=2000)]


def right(name="rally-right", cycles=4):
    r = Route(name, (59, 9.5, 90), speed=50, folder=PP_DIR + "/experiments")
    r.pt("HOME_R", 59, 10, 90).pt("SWEEP_R", 45, 10, 90)
    r.add(r.action("LaunchOne"), r.action("LaunchOne"), r.action("LaunchOne"),
          r.wait("TIP 1", when=["LeftCellUp"], ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2500))
    for k in range(1, cycles + 1):
        r.at = "START" if k == 1 else "HOME_R"
        if k == 1:
            r.add(r.go("HOME_R", heading=90))
        r.add(*cycle(r, k, "HOME_R", "SWEEP_R", "RightCellUp", "LeftCellUp", 90))
    return r


def left(name="rally-left", cycles=4):
    r = Route(name, (59, 132.25, 270), speed=50, folder=PP_DIR + "/experiments")
    r.pt("FLOWER_L", 47.4, 127.8, 90).pt("HOME_L", 59, 131.75, 270).pt("SWEEP_L", 45, 131.75, 270)
    r.add(r.action("SpinUp"),
          r.wait("TIP 1", when=["LeftCellUp"], ms=7500),
          fire(r, "Fire the preloads", "Empty"),
          r.go("FLOWER_L"),
          r.wait("Collect at the FLOWER", when=["IntakeFull"], ms=2500),
          r.go("HOME_L"),
          fire(r, "Fire (TIP 2)", "RightCellUp", ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2000))
    for k in range(1, cycles + 1):
        r.at = "HOME_L"
        r.add(*cycle(r, k, "HOME_L", "SWEEP_L", "LeftCellUp", "RightCellUp", 270))
    return r


if __name__ == "__main__":
    # An experiment that lost (72-82 points against lean-opp's 64-105): with a 24 in catcher there is little
    # left in the line on our side of the centre line, so the refill comes back empty. The Java is not committed.
    right().write()
    left().write()
    study("RallyRightAuto,RallyLeftAuto@50;LeanOppRightAuto,LeanOppLeftAuto@50", runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood, 24 in catcher|two spring hoods, 24 in catcher|clump catapult 72 deg, 24 in catcher")
