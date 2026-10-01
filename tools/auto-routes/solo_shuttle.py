"""Solo shuttle: a 4-TIP Auto that needs nothing from the partner but LEAVE and PARK. Each spill
fills the robot where it stands; it carries those 4 through the tunnel under the HIVE to the CELL
that just rose (pieces are interchangeable), fires, tops up nearby, and fires again. The tunnel is
driven square to the field at x 57.5, and the robot turns only clear of the HIVE, 13 in from the
centre line."""
import sys
from helpers import *

S_HOME, N_HOME = (57.5, 10, 90), (57.5, 131.75, 270)
S_EXIT, N_EXIT = (57.5, 34, 90), (57.5, 108, 90)


def shuttle(name="solo-shuttle", speed=50):
    r = Route(name, (59, 9.5, 90), speed=speed, folder=PP_DIR + "/experiments")
    r.pt("S_HOME", *S_HOME).pt("N_HOME", *N_HOME).pt("S_EXIT", *S_EXIT).pt("N_EXIT", *N_EXIT)
    r.pt("FAR_FLOWER", 47.4, 127.8, 90).pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    tip1 = r.wait("TIP 1?", when=["Tip"], ms=2000, yes_label="Yes", no_label="No",
                  no=[r.action("LaunchOne"), r.wait("TIP 1 (4th POLLEN)", when=["Tip"], ms=1500)])
    r.add(r.action("LaunchOne"), r.action("LaunchOne"), r.action("LaunchOne"), tip1,
          r.wait("Catch the TIP 1 spill", when=["IntakeFull"], ms=2200))
    # North: TIP 2. Fire what we carried; if it didn't tip, the FLOWER beside us, and fire again.
    r.at = "START"
    r.add(r.go("N_EXIT", heading=90), r.go("N_HOME"))
    r.at = "N_HOME"
    top_n = [r.go("FAR_FLOWER"), r.wait("Collect at the FLOWER", when=["IntakeFull"], ms=1800),
             r.go("N_HOME"), fire(r, "Fire again (TIP 2)", "RightCellUp", ms=2500)]
    r.at = "N_HOME"
    r.add(fire(r, "Fire at the north CELL", "Empty", ms=2000),
          r.wait("TIP 2?", when=["RightCellUp"], ms=1200, yes=[], no=top_n, yes_label="Yes", no_label="No: the FLOWER"),
          r.wait("Catch the TIP 2 spill", when=["IntakeFull"], ms=2200))
    # South: TIP 3. Fire, then the GARDEN, fire again.
    r.at = "N_HOME"
    r.add(r.go("S_EXIT", heading=270), r.go("S_HOME"))
    r.at = "S_HOME"
    top_s = [r.go("GARDEN_IN", ctrl=[(30, 22)]), r.go("GARDEN"),
             r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1500),
             r.go("S_HOME", ctrl=[(20, 18)], heading=90), fire(r, "Fire again (TIP 3)", "LeftCellUp", ms=2500)]
    r.at = "S_HOME"
    r.add(fire(r, "Fire at the south CELL", "Empty", ms=2000),
          r.wait("TIP 3?", when=["LeftCellUp"], ms=1200, yes=[], no=top_s, yes_label="Yes", no_label="No: the GARDEN"),
          r.wait("Catch the TIP 3 spill", when=["IntakeFull"], ms=2200))
    # North again: TIP 4, from the TIP 3 spill plus what the TIP 2 spill left there.
    r.at = "S_HOME"
    r.add(r.go("N_EXIT", heading=90), r.go("N_HOME"))
    r.at = "N_HOME"
    r.add(fire(r, "Fire at the north CELL (TIP 4)", "Empty", ms=2000),
          r.wait("Pick up what TIP 2 left", when=["IntakeFull"], ms=1500),
          fire(r, "Fire until it tips (TIP 4)", "RightCellUp", ms=2500))
    return r


if __name__ == "__main__":
    # An experiment that lost: 48-67 points: three crossings of the field cost too much; TIP 3 lands at 26-31 s. The Java is not committed.
    shuttle().write()
    designs = sys.argv[2] if len(sys.argv) > 2 else "spring hood, 24 in catcher|two spring hoods, 24 in catcher|clump catapult 72 deg, 24 in catcher"
    study("SoloShuttleAuto,PartnerLeaveParkAuto@50;SoloShuttleAuto,PartnerPreloadsParkAuto@50;"
          "ThreeTipAdaptiveAuto,PartnerLeaveParkAuto@50;ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50",
          runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10, designs=designs,
          extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})
