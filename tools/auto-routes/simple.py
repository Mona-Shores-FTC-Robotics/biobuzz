"""Two simple Autos for testing paths on the first robot (mentor, 15 Oct 2026; named by the mentor). Both start from the
drive team's left start, despite R-Simple's name:

- L-Simple (l-simple): SpinUp, out to FAR_FLOWER_TURN, the preloads at the left CELL, PARK.
- R-Simple (r-simple): the same, but waits for TIP 1 (the partner's preloads) before firing, then the far FLOWER's
  4 fired from N_FIRE, PARK.

Both PARK in the LOADING ZONE's north end at PARK_L, as Sister's left robot does. Built from alone.py's pieces, so a
change there shows up here too.

    python3 simple.py      exports both (TeamCode/autos and their generated classes)

Simulate them with batch/both.sh against PartnerPreloadsRightSouthAuto: a partner shooting from the right start that
parks at the LOADING ZONE's south end, leaving PARK_L free.
"""
import autogen
from alone import FAR_FLOWER_AT, N_FIRE, Route, fire, seat

START = (59, 133.69, 270)  # the drive team's left start (red)
PARK_L = (10.5, 120, 270)
PARK_CTRL = [(44, 127), (24, 127)]  # Sister's left robot's way to PARK_L: north of the HIVE frame's foot


def preloads_park(name="l-simple"):
    r = Route(name, START, speed=50)
    r.pt("PARK_L", *PARK_L)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    r.add(r.action("SpinUp"), r.go("FAR_FLOWER_TURN", ctrl=[(59, 118.34)], turn_after=0.3, turn_by=0.9))
    r.at = "FAR_FLOWER_TURN"
    r.add(fire(r, "Fire the preloads at the left CELL", "Empty", ms=6000),
          r.go("PARK_L", ctrl=PARK_CTRL, heading=270, park=True))
    return r


def flower_park(name="r-simple", tip1_wait_ms=6000):
    r = Route(name, START, speed=50)
    r.pt("N_FIRE", *N_FIRE).pt("PARK_L", *PARK_L)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    r.add(r.action("SpinUp"), r.go("FAR_FLOWER_TURN", ctrl=[(59, 118.34)], turn_after=0.3, turn_by=0.9))
    r.at = "FAR_FLOWER_TURN"
    # Waits for the partner's TIP 1 at most tip1_wait_ms, then fires whatever happened.
    r.add(r.wait("TIP 1 (the partner's preloads)", when=["LeftCellUp"], ms=tip1_wait_ms),
          fire(r, "Fire the preloads at the left CELL", "Empty", ms=6000), r.go("FAR_FLOWER", heading=90))
    r.at = "FAR_FLOWER"
    r.add(r.wait("The far FLOWER's 4", when=["IntakeFull"], ms=3000), r.go("N_FIRE", turn_after=0.3, turn_by=1.0))
    r.at = "N_FIRE"
    r.add(fire(r, "Fire the far FLOWER's 4", "Empty", ms=5000),
          r.go("PARK_L", ctrl=PARK_CTRL, heading=270, park=True))
    return r


ROUTES = [preloads_park, flower_park]

if __name__ == "__main__":
    for build in ROUTES:
        r = build()
        r.folder = autogen.AUTOS_DIR
        r.write()
        print("wrote", r.name)
