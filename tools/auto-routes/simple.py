"""Two simple Autos for testing paths on the first robot (mentor, 15 Oct 2026):

- L-Simple (l-simple), from the left start: L-Quals' opening exactly (out to FAR_FLOWER_TURN, wait for TIP 1, our
  preloads at the left CELL, the far FLOWER's 4 fired from N_FIRE: 8 shots), then PARK at the LOADING ZONE's north end
  (PARK_L, as Sister's left robot). With L-Quals' usual partner, which parks there too, it would meet it: pair it with
  R-Simple, which parks at the south end.
- R-Simple (r-simple), from the right start: the preloads at the right CELL straight away (4 make a TIP), PARK at the
  zone's south end (alone.PARK, as R-Quals). No waiting.

Built from alone.py's pieces, so a change there shows up here too.

    python3 simple.py      exports both (TeamCode/autos and their generated classes)

Simulate them with batch/both.sh as a pair (L-Simple and R-Simple are each other's partner), or each with its qualifier
Auto's partner: L-Simple with PartnerPreloadsRightSouthAuto (parks at the zone's south end, leaving PARK_L free),
R-Simple with PartnerLeftVAuto.
"""
import autogen
from alone import FAR_FLOWER_AT, N_FIRE, PARK, S_START as R_START, Route, fire, seat

L_START = (59, 133.69, 270)  # the drive team's left start (red)
PARK_L = (10.5, 120, 270)
PARK_L_CTRL = [(44, 127), (24, 127)]  # Sister's left robot's way to PARK_L: north of the HIVE frame's foot


def l_simple(name="l-simple", tip1_wait_ms=7000):
    """L-Quals' opening, card for card (alone.shoots_right_v with with_rescue's TIP 1 branch: the same points, waits
    and timeouts), then PARK_L instead of L-Quals' TIP 3. A TIP 1 that hasn't come in tip1_wait_ms: it fires anyway
    (L-Quals' rescue branch is left out)."""
    r = Route(name, L_START, speed=50)
    r.pt("N_FIRE", *N_FIRE).pt("PARK_L", *PARK_L)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    r.add(r.action("SpinUp"), r.go("FAR_FLOWER_TURN", ctrl=[(59, 118.34)], turn_after=0.3, turn_by=0.9))
    r.at = "FAR_FLOWER_TURN"
    r.add(r.wait("TIP 1 (the partner's preloads)", when=["LeftCellUp"], ms=tip1_wait_ms),
          fire(r, "Fire our preloads at the left CELL", "Empty", ms=3000), r.go("FAR_FLOWER", heading=90))
    r.at = "FAR_FLOWER"
    r.add(r.wait("The far FLOWER's 4", when=["IntakeFull"], ms=3000), r.go("N_FIRE", turn_after=0.3, turn_by=1.0))
    r.at = "N_FIRE"
    r.add(fire(r, "Fire the far FLOWER's 4 (TIP 2)", "Empty", ms=2000),
          r.go("PARK_L", ctrl=PARK_L_CTRL, heading=270, park=True))
    return r


def r_simple(name="r-simple"):
    r = Route(name, R_START, speed=50)
    r.pt("PARK", *PARK)
    # Away at once along x 30, more than 25 in off the HIVE's axis: outside the falling spill (G409, doc/g409-routes.md).
    r.add(r.action("SpinUp"), fire(r, "Fire the preloads at the right CELL (TIP 1)", "Empty", ms=4000),
          r.go("PARK", ctrl=[(30, 40)], turn_after=0.2, turn_by=0.8, park=True))
    return r


ROUTES = [l_simple, r_simple]

if __name__ == "__main__":
    for build in ROUTES:
        r = build()
        r.folder = autogen.AUTOS_DIR
        r.write()
        print("wrote", r.name)
