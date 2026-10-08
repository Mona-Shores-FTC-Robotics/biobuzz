"""Backup-L and Backup-R: Smart Auto's plan when the camera is down (doc/smart-auto.md). The rigid V with a fixed launcher.

Blind, the robot can't see the HIVE, so it can't tell whether the partner's TIP 1 happened. Each backup makes TIP 1
itself and TIP 2 from pieces that are always there, so it gets 2 TIPs + PARK whatever the partner does; a partner's
preloads that land are a bonus (mentor, 9 Oct 2026: "2 tips and park i could live with that"). Every wait is a time
limit or an intake trigger (Empty, IntakeFull), which work without the camera.

    Backup-R  TIP 1  our preloads at the right CELL, from R_PRE.
              TIP 2  the GARDEN's 4, then the wall FLOWER's 4, each fired at the left CELL from R_N (a partner that
                     launched from the left start has added its 4: then the GARDEN's 4 are enough).
    Backup-L  TIP 1  our preloads at the right CELL, fired once the right partner has left its start (it launches
                     from there and parks up x 26 by about 8 s); if its preloads already tipped it, ours are spent.
              TIP 2  the GARDEN's 4 from R_N, then the far FLOWER's 4 from L_N.

    python3 backup.py      writes them into experiments/
"""
import autogen
from autogen import Route
from alone import seat
from helpers import FAR_FLOWER_AT, WALL_FLOWER_AT, fire

R_START, L_START = (59, 8.06, 90), (59, 133.69, 270)
# R_N: the left CELL is clean from y >= 110, and at y 104 from x 30-36 (the 8 Oct scan); x 32 leaves 3.6 in between
# our V and a partner parked at the LOADING ZONE's far end (x up to 19.5).
# PARK at y 92 (the zone is y 94.3-117.9; partly inside is PARK): a partner parked at the far end faces us, and at
# y 95 the Vs met in 60 of 60. L_N at x 52: turning to aim at N_FIRE (x 57.5) swung the V over the centre line.
R_PRE, R_W, R_N, PARK = (57, 20, 90), (30, 40, 90), (32, 104, 90), (10.5, 92, 90)
L_N, PARK_L = (52, 119, 270), (10.5, 92, 270)
GARDEN_IN, GARDEN = (9.5, 20.56, 270), (9.5, 10.96, 270)


def garden_to_rn(r, hold_ms=0):
    """From where r is to the GARDEN, its 4, then up the left side (x 30, clear of the HIVE frame's foot at x 46) to
    R_N, and fire them at the left CELL."""
    r.pt("GARDEN_IN", *GARDEN_IN).pt("GARDEN", *GARDEN).pt("R_W", *R_W).pt("R_N", *R_N)
    r.add(r.go("GARDEN_IN", turn_after=0.2, turn_by=0.8))
    r.at = "GARDEN_IN"
    r.add(r.go("GARDEN", heading=270))
    r.at = "GARDEN"
    r.add(r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500), r.go("R_W", turn_after=0.1, turn_by=0.8))
    r.at = "R_W"
    if hold_ms:
        # A left partner that leaves late crosses y 124 to its park at about 9-11 s, past R_N: wait it out here.
        r.add(r.wait("A late left partner crosses to its park", when=["Empty"], ms=hold_ms))
    r.add(r.go("R_N", heading=90))
    r.at = "R_N"
    r.add(fire(r, "The GARDEN's 4 at the left CELL", "Empty", ms=2000))


def backup_right(name="backup-r", hold_ms=2500):
    r = Route(name, R_START, speed=50)
    r.pt("R_PRE", *R_PRE).pt("PARK", *PARK)
    seat(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.add(r.action("SpinUp"), r.go("R_PRE", heading=90))
    r.at = "R_PRE"
    r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000))
    garden_to_rn(r, hold_ms=hold_ms)
    r.add(r.go("WALL_FLOWER_TURN", turn_after=0.2, turn_by=0.8))
    r.at = "WALL_FLOWER_TURN"
    r.add(r.go("WALL_FLOWER", heading=180))
    r.at = "WALL_FLOWER"
    r.add(r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=3000),
          r.go("R_N", ctrl=[(30, 80)], turn_after=0.3, turn_by=0.9))
    r.at = "R_N"
    # Down first, then across: straight from R_N the V's corner passed under a partner already parked.
    r.add(fire(r, "The wall FLOWER's 4 at the left CELL (TIP 2)", "Empty", ms=2000),
          r.go("PARK", ctrl=[(28, 88)], heading=90, park=True))
    return r


def backup_left(name="backup-l", hold_ms=2500, fire1=(55, 30, 90)):
    r = Route(name, L_START, speed=50)
    r.pt("L_TURN", 57, 36, 90).pt("FIRE1", *fire1).pt("L_N", *L_N).pt("PARK_L", *PARK_L)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    # Down the lane facing the right end, turned below the HIVE frame's feet (y 51-90) at L_TURN, once the right
    # partner has launched and left its start.
    r.add(r.action("SpinUp"), r.wait("The right partner launches and leaves", when=["Empty"], ms=hold_ms),
          r.go("L_TURN", ctrl=[(57.5, 100), (57.5, 50)], turn_after=0.88, turn_by=1.0))
    r.at = "L_TURN"
    r.add(r.go("FIRE1", heading=90))
    r.at = "FIRE1"
    r.add(fire(r, "Preloads at the right CELL (TIP 1, or spent if the partner's tipped it)", "Empty", ms=3000))
    garden_to_rn(r)
    r.add(r.go("FAR_FLOWER_TURN", heading=90))
    r.at = "FAR_FLOWER_TURN"
    r.add(r.go("FAR_FLOWER", heading=90))
    r.at = "FAR_FLOWER"
    r.add(r.wait("The far FLOWER's 4", when=["IntakeFull"], ms=3000), r.go("L_N", turn_after=0.3, turn_by=1.0))
    r.at = "L_N"
    r.add(fire(r, "The far FLOWER's 4 at the left CELL (TIP 2)", "Empty", ms=2000),
          r.go("PARK_L", ctrl=[(40, 100), (25, 92)], heading=270, park=True))
    return r


def variants():
    return [backup_right(), backup_left()]


if __name__ == "__main__":
    for r in variants():
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(r.name)
