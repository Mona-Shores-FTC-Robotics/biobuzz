"""Chasing 4 TIPs with a partner that can only fire its preloads (mentor question, 1 Oct 2026).

After TIP 1 every TIP needs about 8 pieces, and we carry 4: what we catch from the last spill, plus
4 more. Only three sources of 4 sit next to a shooting spot or cost no driving: the far FLOWER
(fire and feed from its pickup spot), the partner's preloads, and the leftovers of the last spill
at that end. So: TIP 1 is ours (the raised CELL already holds 3 NECTAR, so our preloads tip it);
TIP 2 is what we caught plus the far FLOWER, fired and fed at once; TIP 3 is what we caught plus the
partner's 4, fired when the right CELL rises again; TIP 4 is what we caught plus what TIP 2 left.
No park: a 4th TIP (20) is worth more than PARK (5).

Result (1 Oct 2026, two spring hoods with a turret): TIPs at about 4, 13 and 23 s, and back at the
left CELL with pieces for TIP 4 at about 28 s, 2-3 s short. A first try that spent the partner's 4
on TIP 2 and the leftovers on TIP 3 made 2 TIPs: the TIP 1 leftovers had rolled out of reach."""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))  # autogen, helpers
import autogen
autogen.DEFAULT_FOLDER = autogen.EXPERIMENTS  # an archived experiment writes its .pp there
import sys
from helpers import *

R_HOME, L_HOME = (57.5, 10, 90), (59, 131.75, 270)


def partner_late(name="partner-preloads-right-late"):
    """Starts at the right end beside our start tile, aimed at the right CELL, and holds its preloads
    until that CELL is up again (after TIP 2, about 12 s), then fires them and parks. A partner that
    can only fire its preloads can still do this: wait for the rocker, or wait on a timer."""
    r = Route(name, (36, 9.5, 65), speed=40, folder=PP_DIR + "/experiments")
    r.pt("PARK_P", 10.5, 110, 90)
    r.add(r.action("SpinUp"), r.action("IntakeOff"),
          *waits(r, "Right CELL down (TIP 1)", "LeftCellUp", 8.0),
          *waits(r, "Right CELL up again (TIP 2)", "RightCellUp", 16.0),
          fire(r, "Fire the preloads (into TIP 3)", "Empty", ms=3000),
          r.go("PARK_P", ctrl=[(26, 20), (26, 100)], park=True))
    return r


def four_tip_x(name="four-tip-x", speed=50, gather_ms=2500):
    """TIP 1 ours (preloads, right). TIP 2: what we caught plus the far FLOWER, fired and fed from the
    FLOWER (turret). TIP 3: what we caught plus the partner's 4, fired as the right CELL rose. TIP 4:
    what we caught plus what TIP 2 left at the left end. No park."""
    r = Route(name, (59, 9.5, 90), speed=speed)
    r.pt("R_HOME", *R_HOME).pt("L_HOME", *L_HOME).pt("L_BACK", 59, 131, 270)
    r.pt("L_EXIT", 56.5, 103, 90).pt("L_EXIT_BACK", 56.5, 103, 270)
    r.pt("R_EXIT", 57.5, 34, 90).pt("R_EXIT_BACK", 57.5, 34, 270)
    r.pt("CATCH", 59, 121, 270)
    flower_points(r, "FEED", FAR_FLOWER_AT, 90)
    # TIP 1.
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000),
          r.wait("TIP 1?", when=["Tip"], ms=2500), r.wait("TIP 1: catch the spill", when=["IntakeFull"], ms=1600))
    # TIP 2: tunnel left, straight to the FLOWER, fire what we caught and feed the FLOWER's 4 in behind.
    r.at = "START"
    r.add(r.go("L_EXIT", heading=90, turn_by=0.5))
    r.add(*flower(r, "FEED", "At the FLOWER", ms=0)[:-1],
          r.action("StreamOn"), r.wait("Fire and feed (TIP 2)", when=["RightCellUp"], ms=6000), r.action("StreamOff"))
    r.at = "FEED"
    r.add(r.go("CATCH", ctrl=[(47.36, 116)], turn_after=0.3, turn_by=0.8),
          r.wait("TIP 2: catch the spill", when=["IntakeFull"], ms=1600))
    # TIP 3: tunnel right; the partner's 4 are already in the CELL.
    r.at = "CATCH"
    r.add(r.go("L_EXIT_BACK", heading=270), r.go("R_EXIT_BACK", heading=270), r.go("R_HOME", turn_by=0.5),
          fire(r, "Fire (TIP 3)", "LeftCellUp", ms=2500),
          r.wait("TIP 3: catch the spill", when=["IntakeFull"], ms=1600))
    # TIP 4: tunnel left; what TIP 2 left lying here tops it up.
    r.at = "R_HOME"
    r.add(r.go("L_EXIT", heading=90, turn_by=0.5), r.go("L_HOME", turn_by=0.4),
          fire(r, "Fire (TIP 4)", "Empty", ms=2000))
    r.at = "L_HOME"
    gather = [r.wait("Leftovers (TIP 4)", when=["IntakeFull"], ms=gather_ms, alongside="CollectSeen"),
              r.go("L_BACK", heading=270), fire(r, "Fire the leftovers (TIP 4)", "RightCellUp", ms=2500)]
    r.add(r.wait("TIP 4 yet?", when=["RightCellUp"], ms=800, yes=[], no=gather, yes_label="Yes", no_label="No: the leftovers"))
    return r


if __name__ == "__main__":
    partner_late().write()
    four_tip_x().write()
    study("FourTipXAuto,PartnerPreloadsRightLateAuto@50",
          runs=int(sys.argv[1]) if len(sys.argv) > 1 else 20,
          designs=sys.argv[2] if len(sys.argv) > 2 else "two spring hoods, full-width intake, turret",
          extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})
