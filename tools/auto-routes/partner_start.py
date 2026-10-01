"""Where should a partner that only fires its preloads and parks start: the south end (right, from our
drive station), whose CELL is raised at the start, or the north end (left)?

South: it fires the moment it can, which is what most such partners do anyway, and makes TIP 1 with the
3 NECTAR already in the CELL. We start at the north and keep all 4 preloads for TIP 2 (north-first).
North: it has to wait for our TIP 1 to raise the north CELL, by watching it or by a timer, and adds its 4
to our TIP 2 (three-tip-adaptive, ours from the south)."""
import sys
from helpers import *
from three_tip_adaptive import adaptive


def partner_south(name="partner-preloads-south"):
    """Starts in front of the south CELL, fires at once, parks at the south end of the LOADING ZONE."""
    r = Route(name, (59, 9.5, 90), speed=40, folder=PP_DIR + "/partners")
    r.pt("PARK_P", 10, 86, 90)
    r.add(r.action("SpinUp"), fire(r, "Fire the preloads", "Empty", ms=4500),
          r.go("PARK_P", ctrl=[(30, 20), (30, 75)], park=True))
    return r


def partner_south_misses(name="partner-leave-south"):
    """A south partner whose preloads never score: it leaves and parks, nothing more."""
    r = Route(name, (59, 9.5, 90), speed=40, folder=PP_DIR + "/partners")
    r.pt("PARK_P", 10, 86, 90)
    r.add(r.go("PARK_P", ctrl=[(30, 20), (30, 75)], park=True))
    return r


def partner_north_timer(name="partner-preloads-north-timer", wait_s=5.0):
    """Starts in front of the north CELL, fires on a timer (no camera), parks. Empty never fires while it holds its preloads, so the wait is a plain timer."""
    # West of the north CELL, out of the way of our robot's FLOWER pickup in front of it.
    r = Route(name, (30, 132.25, 300), speed=40, folder=PP_DIR + "/partners")
    r.pt("PARK_P", 16, 122, 270)
    r.add(r.action("SpinUp"), *waits(r, "Wait for our TIP 1 (timer)", "Empty", wait_s),
          fire(r, "Fire the preloads", "Empty"), r.go("PARK_P", park=True))
    return r


def north_first(name="north-first", speed=50, tip4=False):
    """Ours, when the partner makes TIP 1: everything three-tip-adaptive does, mirrored to start north.
    If the north CELL hasn't risen by 6 s, the partner missed: drive south and make TIP 1 ourselves.
    tip4: after TIP 3, catch its spill, carry it north and top up from what TIP 2 left there."""
    r = Route(name, (59, 132.25, 270), speed=speed)
    r.pt("N_HOME", 59, 131.75, 270)
    flower_points(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    r.pt("N_EXIT", 57.5, 108, 270).pt("S_EXIT", 57.5, 34, 270).pt("S_HOME", 57.5, 10, 90)
    r.pt("SOUTH_SHOT", 36, 30, 49).pt("SOUTH_PLUNGE_IN", 55, 24, 270).pt("SOUTH_PLUNGE", 55, 10.5, 270)
    r.pt("GARDEN", 8.5, 11, 270).pt("PARK", 16, 120, 90).pt("PARK2", 16, 122, 90)
    # Fallback: the partner missed TIP 1. Ours, then TIP 2 at the north, then park.
    r.at = "START"
    fallback = [r.go("N_EXIT", heading=270), r.go("S_EXIT", heading=270), r.go("S_HOME"),
                fire(r, "Fire until it tips (our TIP 1)", "LeftCellUp", ms=3000),
                r.wait("Catch the TIP 1 spill", when=["IntakeFull"], ms=2200),
                r.go("N_EXIT", heading=90), r.go("N_HOME"),
                fire(r, "Fire at the north CELL", "Empty", ms=2000),
                *flower(r, "FAR_FLOWER", "Collect at the FLOWER (fallback)", ms=1800),
                *leave_flower(r, "FAR_FLOWER", "N_HOME"), fire(r, "Fire until it tips (TIP 2, fallback)", "RightCellUp", ms=2500),
                r.go("PARK", ctrl=[(30, 120)]), r.go("PARK2", park=True)]
    r.at = "START"
    plan = []
    plan += [fire(r, "Fire the preloads", "Empty"),
             *flower(r, "FAR_FLOWER", "Collect at the FLOWER", ms=1800),
             *leave_flower(r, "FAR_FLOWER", "N_HOME"), fire(r, "Fire until it tips (TIP 2)", "RightCellUp", ms=2500),
             r.wait("Catch the TIP 2 spill", when=["IntakeFull"], ms=2200),
             r.go("N_EXIT", heading=270), r.go("S_EXIT", heading=270), r.go("SOUTH_SHOT"),
             r.action("LaunchAll"),
             r.go("SOUTH_PLUNGE_IN"), r.go("SOUTH_PLUNGE"),
             r.wait("The TIP 1 spill at the wall", when=["IntakeFull"], ms=900),
             r.go("SOUTH_SHOT"), fire(r, "Fire (TIP 3)", "Empty", ms=2000)]
    r.at = "SOUTH_SHOT"
    more = [r.go("GARDEN"), r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1000),
            r.go("SOUTH_SHOT"), fire(r, "Fire until it tips (TIP 3)", "LeftCellUp", ms=2500)]
    r.at = "SOUTH_SHOT"
    if tip4:
        # Go stand in the roll path as soon as the TIP 3 volley is away; the spill lands 1.0-1.45 s after the TIP starts.
        plan += [r.go("S_HOME"),
                 r.wait("Catch the TIP 3 spill", when=["IntakeFull"], ms=2500, yes=[], no=more, yes_label="Caught", no_label="No TIP: the GARDEN")]
        r.at = "S_HOME"
        plan += [r.go("S_EXIT", heading=90), r.go("N_EXIT", heading=90), r.go("N_HOME"),
                 fire(r, "Fire at the north CELL (TIP 4)", "Empty", ms=2000),
                 r.wait("What TIP 2 left", when=["IntakeFull"], ms=1500),
                 fire(r, "Fire until it tips (TIP 4)", "RightCellUp", ms=2500)]
    else:
        plan += [r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=1500, yes=[], no=more, yes_label="Yes", no_label="No: the GARDEN")]
        r.at = "SOUTH_SHOT"
        plan += [r.go("PARK", ctrl=[(34, 60), (34, 126)], heading=90), r.go("PARK2", park=True)]  # square, between the HIVE frame and the partner
    r.add(r.action("SpinUp"),
          r.wait("North CELL up (partner's TIP 1)?", when=["LeftCellUp"], ms=6000, yes=plan, no=fallback,
                 yes_label="Yes", no_label="No: the partner missed"))
    return r


if __name__ == "__main__":
    partner_south().write()
    partner_south_misses().write()
    partner_north_timer().write()
    north_first().write()
    study("NorthFirstAuto,PartnerPreloadsSouthAuto@50;NorthFirstAuto,PartnerLeaveSouthAuto@50;"
          "ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50;ThreeTipAdaptiveAuto,PartnerPreloadsNorthTimerAuto@50;"
          "ThreeTipAdaptiveAuto,PartnerLeaveParkAuto@50",
          runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood, 24 in catcher|two spring hoods, 24 in catcher|clump catapult 72 deg, 24 in catcher",
          extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})
