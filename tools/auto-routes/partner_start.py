"""Where should a partner that only fires its preloads and parks start: the right end (right, from our
drive station), whose CELL is raised at the start, or the left end (left)?

Right: it fires the moment it can, which is what most such partners do anyway, and makes TIP 1 with the
3 NECTAR already in the CELL. We start at the left and keep all 4 preloads for TIP 2 (left-first).
Left: it has to wait for our TIP 1 to raise the left CELL, by watching it or by a timer, and adds its 4
to our TIP 2 (three-tip-adaptive, ours from the right)."""
import sys
from helpers import *
from three_tip_adaptive import adaptive


def partner_right(name="partner-preloads-right"):
    """Starts in front of the right CELL, fires at once, parks toward the far-left end of the LOADING
    ZONE like the other reference partners (mentor review), leaving the near end for us."""
    r = Route(name, (59, 9.5, 90), speed=40, folder=PP_DIR + "/partners")
    r.pt("PARK_P", 10.5, 110, 90)
    # Intake off: it only fires its preloads, so it has no reason to sweep up the TIP 1 spill on its
    # way to park (it used to, and left us nothing at the right end).
    r.add(r.action("SpinUp"), r.action("IntakeOff"), fire(r, "Fire the preloads", "Empty", ms=4500),
          r.go("PARK_P", ctrl=[(26, 20), (26, 100)], park=True))  # x 26: clear of the HIVE frame's foot bar
    return r


def partner_right_misses(name="partner-leave-right"):
    """A right partner whose preloads never score: it leaves and parks, nothing more."""
    r = Route(name, (59, 9.5, 90), speed=40, folder=PP_DIR + "/partners")
    r.pt("PARK_P", 10.5, 110, 90)
    r.add(r.go("PARK_P", ctrl=[(26, 20), (26, 100)], park=True))
    return r


def partner_left_timer(name="partner-preloads-left-timer", wait_s=5.0):
    """Starts in front of the left CELL, fires on a timer (no camera), parks. Empty never fires while it holds its preloads, so the wait is a plain timer."""
    # West of the left CELL, out of the way of our robot's FLOWER pickup in front of it.
    r = Route(name, (30, 132.25, 300), speed=40, folder=PP_DIR + "/partners")
    r.pt("PARK_P", 16, 122, 270)
    r.add(r.action("SpinUp"), *waits(r, "Wait for our TIP 1 (timer)", "Empty", wait_s),
          fire(r, "Fire the preloads", "Empty"), r.go("PARK_P", park=True))
    return r


def left_first(name="left-first", speed=50, tip4=False):
    """Ours, when the partner makes TIP 1: everything three-tip-adaptive does, mirrored to start left.
    If the left CELL hasn't risen by 6 s, the partner missed: drive right and make TIP 1 ourselves.
    tip4: after TIP 3, catch its spill, carry it left and top up from what TIP 2 left there."""
    r = Route(name, (59, 132.25, 270), speed=speed)
    r.pt("L_HOME", 59, 131.75, 270)
    flower_points(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    r.pt("L_EXIT", 57.5, 108, 270).pt("R_EXIT", 57.5, 34, 270).pt("R_HOME", 57.5, 10, 90)
    r.pt("RIGHT_SHOT", 36, 30, 49).pt("RIGHT_PLUNGE_IN", 55, 24, 270).pt("RIGHT_PLUNGE", 55, 10.5, 270)
    r.pt("GARDEN", 8.5, 11, 270).pt("PARK", 15, 89, 90).pt("PARK2", 15, 90, 90)  # near end; the partner takes the far-left end
    # Fallback: the partner missed TIP 1. Ours, then TIP 2 at the left, then park.
    r.at = "START"
    fallback = [r.go("L_EXIT", heading=270), r.go("R_EXIT", heading=270), r.go("R_HOME"),
                fire(r, "Fire until it tips (our TIP 1)", "LeftCellUp", ms=3000),
                r.wait("Catch the TIP 1 spill", when=["IntakeFull"], ms=2200),
                r.go("L_EXIT", heading=90), r.go("L_HOME"),
                fire(r, "Fire at the left CELL", "Empty", ms=2000),
                *flower(r, "FAR_FLOWER", "Collect at the FLOWER (fallback)", ms=1800),
                *leave_flower(r, "FAR_FLOWER", "L_HOME"), fire(r, "Fire until it tips (TIP 2, fallback)", "RightCellUp", ms=2500),
                r.go("PARK", ctrl=[(59, 104), (59, 104), (32, 110), (32, 110), (32, 86), (32, 86)], turn_after=0.45, turn_by=0.9),
                r.go("PARK2", park=True)]
    r.at = "START"
    plan = []
    plan += [fire(r, "Fire the preloads", "Empty"),
             *flower(r, "FAR_FLOWER", "Collect at the FLOWER", ms=1800),
             *leave_flower(r, "FAR_FLOWER", "L_HOME"), fire(r, "Fire until it tips (TIP 2)", "RightCellUp", ms=2500),
             r.wait("Catch the TIP 2 spill", when=["IntakeFull"], ms=2200),
             r.go("L_EXIT", heading=270), r.go("R_EXIT", heading=270), r.go("RIGHT_SHOT"),
             r.action("LaunchAll"),
             r.go("RIGHT_PLUNGE_IN"), r.go("RIGHT_PLUNGE"),
             r.wait("The TIP 1 spill at the wall", when=["IntakeFull"], ms=900),
             r.go("RIGHT_SHOT"), fire(r, "Fire (TIP 3)", "Empty", ms=2000)]
    r.at = "RIGHT_SHOT"
    more = [r.go("GARDEN"), r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1000),
            r.go("RIGHT_SHOT"), fire(r, "Fire until it tips (TIP 3)", "LeftCellUp", ms=2500)]
    r.at = "RIGHT_SHOT"
    if tip4:
        # Go stand in the roll path as soon as the TIP 3 volley is away; the spill lands 1.0-1.45 s after the TIP starts.
        plan += [r.go("R_HOME"),
                 r.wait("Catch the TIP 3 spill", when=["IntakeFull"], ms=2500, yes=[], no=more, yes_label="Caught", no_label="No TIP: the GARDEN")]
        r.at = "R_HOME"
        plan += [r.go("R_EXIT", heading=90), r.go("L_EXIT", heading=90), r.go("L_HOME"),
                 fire(r, "Fire at the left CELL (TIP 4)", "Empty", ms=2000),
                 r.wait("What TIP 2 left", when=["IntakeFull"], ms=1500),
                 fire(r, "Fire until it tips (TIP 4)", "RightCellUp", ms=2500)]
    else:
        plan += [r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=1500, yes=[], no=more, yes_label="Yes", no_label="No: the GARDEN")]
        r.at = "RIGHT_SHOT"
        plan += [r.go("PARK", ctrl=[(24, 40), (24, 80)], heading=90), r.go("PARK2", park=True)]
    r.add(r.action("SpinUp"),
          r.wait("Left CELL up (partner's TIP 1)?", when=["LeftCellUp"], ms=6000, yes=plan, no=fallback,
                 yes_label="Yes", no_label="No: the partner missed"))
    return r


if __name__ == "__main__":
    partner_right().write()
    partner_right_misses().write()
    partner_left_timer().write()
    left_first().write()
    study("LeftFirstAuto,PartnerPreloadsRightAuto@50;LeftFirstAuto,PartnerLeaveRightAuto@50;"
          "ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50;ThreeTipAdaptiveAuto,PartnerPreloadsLeftTimerAuto@50;"
          "ThreeTipAdaptiveAuto,PartnerLeaveParkAuto@50",
          runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood, 24 in catcher|two spring hoods, 24 in catcher|clump catapult 72 deg, 24 in catcher",
          extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})
