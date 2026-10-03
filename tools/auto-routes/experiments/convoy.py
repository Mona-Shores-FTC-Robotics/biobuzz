"""Both robots work whichever CELL is up, together, and travel between the ends on separate lanes:
our robot through the tunnel under the HIVE (centre x 59, square to the field), the partner up and
down the west corridor (centre x 22). Both catch each spill (one in its roll path, one beside it),
carry it to the other end and fire it there, so every TIP gets about 8 pieces at once."""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))  # autogen, helpers
import autogen
autogen.DEFAULT_FOLDER = autogen.EXPERIMENTS  # an archived experiment writes its .pp there
import sys
from helpers import *

R_HOME, L_HOME = (57.5, 10, 90), (57.5, 131.75, 270)
# Through the tunnel square to the field; turn only clear of the HIVE, 13 in from the centre line.
R_EXIT, L_EXIT = (57.5, 34, 90), (57.5, 108, 90)
R_SIDE, L_SIDE = (32, 14, 60), (32, 128, 300)


def tunnel(cycles=4, name="convoy-tunnel"):
    """Our robot: in the spill's roll path at each end, through the tunnel between them."""
    r = Route(name, (59, 9.5, 90), speed=50, folder=PP_DIR + "/experiments")
    r.pt("R_HOME", *R_HOME).pt("L_HOME", *L_HOME).pt("R_EXIT", *R_EXIT).pt("L_EXIT", *L_EXIT)
    r.add(r.action("LaunchOne"), r.action("LaunchOne"), r.action("LaunchOne"),
          r.wait("TIP 1", when=["LeftCellUp"], ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2000))
    up, other = "LeftCellUp", "RightCellUp"
    for k in range(cycles):
        left = k % 2 == 0
        if left:
            r.add(r.go("R_EXIT"), r.go("L_EXIT", heading=90), r.go("L_HOME"))
        else:
            r.add(r.go("L_EXIT"), r.go("R_EXIT", heading=90), r.go("R_HOME"))
        r.add(fire(r, f"Fire ({k + 1})", "Empty", ms=2000),
              r.wait(f"Until it tips ({k + 1})", when=[other], ms=2500),
              r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2000))
        up, other = other, up
    return r


def corridor(cycles=4, name="convoy-corridor"):
    """The partner: beside the roll path at each end, up and down the west corridor."""
    r = Route(name, (38, 9.5, 90), speed=50, folder=PP_DIR + "/experiments")
    r.pt("R_SIDE", *R_SIDE).pt("L_SIDE", *L_SIDE)
    r.add(fire(r, "Fire the preloads at the right CELL", "Empty", ms=4000),
          r.wait("TIP 1", when=["LeftCellUp"], ms=3000),
          r.go("R_SIDE"),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2000))
    up, other = "LeftCellUp", "RightCellUp"
    for k in range(cycles):
        left = k % 2 == 0
        if left:
            r.add(r.go("L_SIDE", ctrl=[(22, 30), (20, 100)]))
        else:
            r.add(r.go("R_SIDE", ctrl=[(20, 100), (22, 30)]))
        r.add(fire(r, f"Fire ({k + 1})", "Empty", ms=2000),
              r.wait(f"Until it tips ({k + 1})", when=[other], ms=2500),
              r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2000))
        up, other = other, up
    return r


if __name__ == "__main__":
    # An experiment, kept for the record: 26-30 points. Only the robot in the roll path catches, so
    # the CELL at the far end gets about 4 pieces and rarely tips. The exported Java is not committed.
    tunnel().write()
    corridor().write()
    study("ConvoyTunnelAuto,ConvoyCorridorAuto@50", runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood|two spring hoods, full-width intake",
          extra_env={"BIOBUZZ_AUTO_TIMELINE": sys.argv[3]} if len(sys.argv) > 3 else None)
