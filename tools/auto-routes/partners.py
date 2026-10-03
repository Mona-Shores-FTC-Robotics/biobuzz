from autogen import *
from helpers import waits, fire

def leave_park():
    """Leaves and parks, nothing more, but first sets its 4 preloads in a row along its field side
    for us to collect (AutoStudyTest.LEAVE_PARTNER_STAGED), so it drives straight off to park
    toward the far-left end of the LOADING ZONE, off the wall (mentor review)."""
    r = Route("partner-leave-park", (24, 132.25, 270), speed=40)
    r.pt("PARK_P", 10.5, 110, 270)
    r.add(r.go("PARK_P", ctrl=[(24, 118)], heading=270, park=True))
    return r

def preloads_park():
    r = Route("partner-preloads-park", (59, 132.25, 270), speed=40)
    r.pt("PARK_P", 10.5, 111, 270)  # the far-left end of the LOADING ZONE, off the wall
    r.add(r.action("SpinUp"),
          *waits(r, "Left CELL up", "LeftCellUp", 8.0),
          fire(r, "Fire the preloads", "Empty"),
          r.go("PARK_P", ctrl=[(59, 116), (30, 116)], park=True))  # right first, clear of the far FLOWER
    return r

def partner_right(name="partner-preloads-right"):
    """Starts in front of the right CELL, fires at once, parks toward the far-left end of the LOADING
    ZONE like the other reference partners (mentor review), leaving the near end for us."""
    r = Route(name, (59, 9.5, 90), speed=40)
    r.pt("PARK_P", 10.5, 110, 90)
    # Intake off: it only fires its preloads, so it has no reason to sweep up the TIP 1 spill on its
    # way to park (it used to, and left us nothing at the right end).
    r.add(r.action("SpinUp"), r.action("IntakeOff"), fire(r, "Fire the preloads", "Empty", ms=4500),
          r.go("PARK_P", ctrl=[(26, 20), (26, 100)], park=True))  # x 26: clear of the HIVE frame's foot bar
    return r


if __name__ == "__main__":
    leave_park().write(); preloads_park().write()
    specs = []
    for ours in ("SoloTwoTipAuto@40", "SoloThreeTipAuto@50", "SpillThreeTipAuto@50"):
        a, sp = ours.split("@")
        for p in ("PartnerLeaveParkAuto", "PartnerPreloadsParkAuto"):
            specs.append(f"{a},{p}@{sp}")
    study(";".join(specs), runs=10)
