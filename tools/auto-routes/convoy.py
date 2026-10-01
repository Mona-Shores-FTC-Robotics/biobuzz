"""Both robots work whichever CELL is up, together, and travel between the ends on separate lanes:
our robot through the tunnel under the HIVE (centre x 59, square to the field), the partner up and
down the west corridor (centre x 22). Both catch each spill (one in its roll path, one beside it),
carry it to the other end and fire it there, so every TIP gets about 8 pieces at once."""
import sys
from helpers import *

S_HOME, N_HOME = (59, 10, 90), (59, 131.75, 270)
S_SIDE, N_SIDE = (38, 12, 70), (38, 129, 290)


def tunnel(cycles=4, name="convoy-tunnel"):
    """Our robot: in the spill's roll path at each end, through the tunnel between them."""
    r = Route(name, (59, 9.5, 90), speed=50)
    r.pt("S_HOME", *S_HOME).pt("N_HOME", *N_HOME)
    r.add(r.action("LaunchOne"), r.action("LaunchOne"), r.action("LaunchOne"),
          r.wait("TIP 1", when=["LeftCellUp"], ms=2500),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2000))
    up, other = "LeftCellUp", "RightCellUp"
    for k in range(cycles):
        north = k % 2 == 0
        r.add(r.go("N_HOME" if north else "S_HOME", heading=90 if north else 270),
              fire(r, f"Fire ({k + 1})", "Empty", ms=2000),
              r.wait(f"Until it tips ({k + 1})", when=[other], ms=2500),
              r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2000))
        up, other = other, up
    return r


def corridor(cycles=4, name="convoy-corridor"):
    """The partner: beside the roll path at each end, up and down the west corridor."""
    r = Route(name, (38, 9.5, 90), speed=50)
    r.pt("S_SIDE", *S_SIDE).pt("N_SIDE", *N_SIDE)
    r.add(fire(r, "Fire the preloads at the south CELL", "Empty", ms=4000),
          r.wait("TIP 1", when=["LeftCellUp"], ms=3000),
          r.go("S_SIDE"),
          r.wait("Catch the spill", when=["IntakeFull"], ms=2000))
    up, other = "LeftCellUp", "RightCellUp"
    for k in range(cycles):
        north = k % 2 == 0
        if north:
            r.add(r.go("N_SIDE", ctrl=[(22, 30), (20, 100)]))
        else:
            r.add(r.go("S_SIDE", ctrl=[(20, 100), (22, 30)]))
        r.add(fire(r, f"Fire ({k + 1})", "Empty", ms=2000),
              r.wait(f"Until it tips ({k + 1})", when=[other], ms=2500),
              r.wait(f"Catch the spill ({k + 1})", when=["IntakeFull"], ms=2000))
        up, other = other, up
    return r


if __name__ == "__main__":
    tunnel().write()
    corridor().write()
    study("ConvoyTunnelAuto,ConvoyCorridorAuto@50", runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood|two spring hoods, 24 in catcher",
          extra_env={"BIOBUZZ_AUTO_TIMELINE": sys.argv[3]} if len(sys.argv) > 3 else None)
