"""Two simple Autos for testing paths on the first robot (mentor, 15 Oct 2026; named by the mentor):

- L-Simple (l-simple), from the left start: SpinUp, out to FAR_FLOWER_TURN, the preloads at the left CELL, PARK at the
  LOADING ZONE's north end (PARK_L, as Sister's left robot).
- R-Simple (r-simple), from the right start: the preloads at the right CELL, wait for TIP 1 and its spill to land, the
  wall FLOWER's 4 fired from L-Quals' TIP 3 spot, PARK at the zone's south end (alone.PARK, as R-Quals).

Built from alone.py's pieces, so a change there shows up here too.

    python3 simple.py      exports both (TeamCode/autos and their generated classes)

Simulate them with batch/both.sh: L-Simple against PartnerPreloadsRightSouthAuto (shoots from the right start, parks at
the zone's south end, leaving PARK_L free), R-Simple against PartnerLeftVAuto (R-Quals' partner, from the left start).
"""
import autogen
from alone import FAR_FLOWER_AT, PARK, S_START as R_START, WALL_FLOWER_AT, Route, fire, seat

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


def flower_park(name="r-simple", tip1_ms=4000, land_ms=1600, fire_at=(45, 26, 90)):
    """From the right start: the preloads at the right CELL from the start, wait there for TIP 1 and for its spill to
    land (G409: the start is 63 in from the HIVE's centre line, outside the spill, and 1.6 s covers its last piece), then
    the wall FLOWER's 4, fired from FIRE (L-Quals' TIP 3 spot), PARK at the LOADING ZONE's south end."""
    r = Route(name, R_START, speed=50)
    r.pt("FIRE", *fire_at).pt("PARK", *PARK)
    seat(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.add(r.action("SpinUp"), fire(r, "Fire the preloads at the right CELL (TIP 1)", "Empty", ms=4000),
          r.wait("TIP 1", when=["Tip"], ms=tip1_ms),
          r.wait("TIP 1's spill lands", when=["IntakeFull"], ms=land_ms),
          r.go("WALL_FLOWER_TURN", turn_after=0.2, turn_by=0.8))
    r.at = "WALL_FLOWER_TURN"
    r.add(r.go("WALL_FLOWER", heading=180))
    r.at = "WALL_FLOWER"
    r.add(r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=3000), r.go("FIRE", turn_after=0.3, turn_by=0.9))
    r.at = "FIRE"
    r.add(fire(r, "Fire the wall FLOWER's 4", "Empty", ms=5000),
          r.go("PARK", ctrl=[(30, 40)], turn_after=0.2, turn_by=0.8, park=True))
    return r


ROUTES = [preloads_park, flower_park]

if __name__ == "__main__":
    for build in ROUTES:
        r = build()
        r.folder = autogen.AUTOS_DIR
        r.write()
        print("wrote", r.name)
