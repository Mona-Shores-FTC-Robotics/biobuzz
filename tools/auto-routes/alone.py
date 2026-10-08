"""3 TIPs with a partner that cannot shoot (mentor, 8 Oct 2026): "we need to work on the staged autos, or a just-park
auto from our teammate, where we can tip 3. can a flower first turret type get us there?"

The Rigid V with its turret, from the south start in front of the right CELL (the Stages start):

    TIP 1   our 4 preloads at the right CELL, from the start.
    TIP 2   north through TIP 1's spill up the lane, into the far FLOWER's seat (38 in from the left CELL, up after
            TIP 1): fire what the catch brought, take the FLOWER's 4, back out to N_FIRE and fire them standing.
    TIP 3   south through TIP 2's spill down the lane, round into the wall FLOWER's seat (43 in from the right CELL, up
            after TIP 2): fire the catch, take the wall FLOWER's 4, fire them from there.
    PARK    48 in north of the wall FLOWER, at the LOADING ZONE (10.5, 95).

The FLOWER seats put the V's face 4.59 in from the FLOWER's centre (the CAD's seat), the centre 12.15 in out.

    python3 alone.py      writes the variants into experiments/
"""
import autogen
from autogen import Route
from helpers import FAR_FLOWER_AT, WALL_FLOWER_AT, fire

S_START = (59, 8.06, 90)
SEAT_IN, IN_IN, TURN_IN = 12.15, 17.95, 20.45  # the V's centre from the FLOWER: seated, straight back, turn room
S_CATCH, S_FIRE, N_FIRE = (57.5, 28, 90), (57.5, 24, 270), (57.5, 119, 270)  # S_FIRE: arrived at facing south
PARK = (10.5, 95, 90)


def seat(r, name, at, heading):
    import math
    c, s = math.cos(math.radians(heading)), math.sin(math.radians(heading))
    for suffix, d in (("", SEAT_IN), ("_IN", IN_IN), ("_TURN", TURN_IN)):
        r.pt(name + suffix, round(at[0] - d * c, 2), round(at[1] - d * s, 2), heading)


def alone(name, tip1_catch=1500, tip2_settle=500, tip2_land=0, far_fire_at="n_fire", wall_ms=3000, fire3=(57.5, 24, 90)):
    r = Route(name, S_START, speed=50)
    r.pt("S_CATCH", *S_CATCH).pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE).pt("PARK", *PARK)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    seat(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)

    # TIP 1: our preloads at the right CELL from the start, then to S_CATCH before the TIP, facing the HIVE, and
    # catch its spill as it rolls south toward the wall (it rolled past a robot that left after the TIP).
    r.add(r.action("SpinUp"), fire(r, "Fire the preloads (TIP 1)", "Empty", ms=2500), r.go("S_CATCH"))
    r.at = "S_CATCH"
    r.add(r.wait("TIP 1", when=["Tip"], ms=4000),
          r.wait("Catch TIP 1's spill", when=["IntakeFull"], ms=tip1_catch))
    # Up the lane (x 57.5, between the HIVE frame's feet at x 46 and 95), the control points stacked at its top so
    # the robot's back is past the west foot's end (y 90) before it bends west, then into the far FLOWER's seat.
    # The extractor comes down on the way.
    r.add(r.go("FAR_FLOWER_TURN", ctrl=[(57.5, 100), (57.5, 118), (57.5, 118)], turn_by=0.5))
    r.at = "FAR_FLOWER_TURN"
    # TIP 2, the mentor's hold: the catch fired standing at FAR_FLOWER_TURN (30 in from the left CELL, up after TIP 1)
    # before the seat; into the seat for the FLOWER's 4, not firing while it extracts (a LaunchAll there fired each
    # piece as the extractor brought it); back out to N_FIRE and fire them standing.
    r.add(fire(r, "Fire the catch at the left CELL", "Empty", ms=2000), r.go("FAR_FLOWER", heading=90))
    r.at = "FAR_FLOWER"
    r.add(r.wait("The far FLOWER's 4", when=["IntakeFull"], ms=3000))
    if far_fire_at == "n_fire":
        r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0))
        r.at = "N_FIRE"
    r.add(fire(r, "Fire the far FLOWER's 4 (TIP 2)", "Tip", ms=2500))
    if tip2_settle:
        r.add(r.wait("TIP 2's spill lands", when=["IntakeFull"], ms=tip2_settle))
    # TIP 3: south through TIP 2's spill down the lane, then round into the wall FLOWER's seat.
    r.add(r.go("S_FIRE", ctrl=[(57.5, 100)], heading=270 if far_fire_at == "n_fire" else "linear"))
    r.at = "S_FIRE"
    # TIP 2's catch fired standing at S_FIRE, on the way: from the wall FLOWER's side the shots crossed the HIVE
    # (3 of 8 "shot-hive"). Then the same hold at the wall FLOWER, its 4 fired from FIRE3, south of the HIVE.
    r.pt("FIRE3", *fire3)
    r.add(fire(r, "Fire TIP 2's catch at the right CELL", "Empty", ms=2000), r.go("WALL_FLOWER_TURN", turn_after=0.2, turn_by=0.8))
    r.at = "WALL_FLOWER_TURN"
    r.add(r.go("WALL_FLOWER", heading=180))
    r.at = "WALL_FLOWER"
    r.add(r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=wall_ms), r.go("FIRE3", turn_after=0.3, turn_by=0.9))
    r.at = "FIRE3"
    r.add(fire(r, "Fire the wall FLOWER's 4 (TIP 3)", "Tip", ms=2500),
          r.go("PARK", ctrl=[(30, 40)], turn_after=0.2, turn_by=0.8, park=True))
    return r


def partner_park_only(name="partner-park-only"):
    """A partner that only parks, keeping its 4 preloads, from the standard left start: straight south off the wall,
    then west along y 116, clear of the far FLOWER (partners.park_left's lane at y 127.5 drives into it now), into
    the LOADING ZONE's far end (10.5, 118), clear of our V's tips parked at (10.5, 95)."""
    r = Route(name, (59, 132.25, 270), speed=40)
    r.pt("DOWN", 59, 116, 270).pt("PARK_P", 10.5, 118, 270)
    r.add(r.go("DOWN", heading=270))
    r.at = "DOWN"
    r.add(r.go("PARK_P", ctrl=[(30, 116)], heading=270, park=True))
    return r


# TIP 3 fired from S_FIRE: the park path from there clipped the HIVE frame's west foot (8 of 10 runs); from (45, 26)
# it does not. TIP 2's spill: 0 ms before driving into it touched it falling (G409 in 8 of 10); 500-1000 ms is best.
VARIANTS = {f"qual-alone-flower-f45-s{ms}": {"tip2_settle": ms, "fire3": (45, 26, 90)} for ms in (500, 1000)}

if __name__ == "__main__":
    for n, kw in VARIANTS.items():
        r = alone(n, **kw)
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(n)
    r = partner_park_only()
    r.folder = autogen.EXPERIMENTS
    r.write()
    print(r.name)
