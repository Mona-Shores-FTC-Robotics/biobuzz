"""three-tip-adaptive, but after TIP 3 it goes for TIP 4 instead of parking."""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))  # autogen, helpers
import autogen
autogen.DEFAULT_FOLDER = autogen.EXPERIMENTS  # an archived experiment writes its .pp there
from autogen import *
from helpers import waits, fire

def adaptive(name="four-tip-adaptive", speed=50):
    r = Route(name, (59, 9.5, 90), speed=speed, folder=PP_DIR + "/experiments")
    r.pt("WALL_FLOWER", 13.7, 47.4, 180).pt("LEFT_SHOT", 40, 116, 301).pt("FAR_FLOWER", 47.4, 127.8, 90)
    r.pt("RIGHT_PLUNGE_IN", 55, 24, 270).pt("RIGHT_PLUNGE", 55, 10.5, 270).pt("RIGHT_SHOT", 36, 30, 49)
    r.pt("GARDEN", 8.5, 11, 270)
    right_cps = [(34, 92), (12, 60), (22, 30)]
    tip1 = r.wait("Tip 1?", when=["Tip"], ms=2000, yes_label="Yes", no_label="No",
                  no=[r.action("LaunchOne"), r.wait("Tip 1 (4th POLLEN)", when=["Tip"], ms=1500)])
    r.add(r.action("LaunchOne"), r.action("LaunchOne"), r.action("LaunchOne"), tip1,
          r.go("WALL_FLOWER"),
          r.wait("Collect at WALL_FLOWER", when=["IntakeFull"], ms=1500),
          r.go("LEFT_SHOT"),
          r.action("LaunchAll"),
          r.go("FAR_FLOWER"),
          r.wait("Collect at FAR_FLOWER", when=["IntakeFull"], ms=1500),
          r.go("LEFT_SHOT"))
    # Branch A: left CELL still up (we make TIP 2). Branch B: a partner already made TIP 2.
    r.at = "LEFT_SHOT"
    a = [fire(r, "Fire until it tips (TIP 2)", "RightCellUp", ms=2500),
         r.go("RIGHT_PLUNGE_IN", ctrl=right_cps), r.go("RIGHT_PLUNGE"),
         r.wait("Spilled NECTAR", when=["IntakeFull"], ms=400),
         r.go("RIGHT_SHOT"), r.action("LaunchAll"),
         r.go("GARDEN"), r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1000),
         r.go("RIGHT_SHOT"), fire(r, "Fire until it tips (TIP 3)", "LeftCellUp", ms=2500)]
    r.at = "LEFT_SHOT"
    b = [r.go("RIGHT_SHOT", ctrl=right_cps), r.action("LaunchAll"),
         r.go("RIGHT_PLUNGE_IN"), r.go("RIGHT_PLUNGE"),
         r.wait("Spilled NECTAR (B)", when=["IntakeFull"], ms=600),
         r.go("RIGHT_SHOT"), fire(r, "Fire (B)", "Empty", ms=2000)]
    r.at = "RIGHT_SHOT"
    more = [r.go("GARDEN"), r.wait("Collect in the GARDEN (B)", when=["IntakeFull"], ms=1000),
            r.go("RIGHT_SHOT"), fire(r, "Fire the TIP 3 volley, then park", "Empty", ms=2000)]
    b.append(r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=1500, yes=[], no=more, yes_label="Yes: park", no_label="No: the GARDEN"))
    r.add(r.wait("Did a partner make TIP 2?", when=["RightCellUp"], ms=50, yes=b, no=a,
                 yes_label="Yes: right CELL up", no_label="No: TIP 2 is ours"))
    # TIP 4 instead of PARK (20 points against 5): catch the TIP 3 spill where it lands, carry it
    # through the tunnel to the left CELL, top up at the FLOWER beside it.
    r.at = "RIGHT_SHOT"
    r.pt("R_CATCH", 57.5, 11, 90).pt("L_EXIT", 57.5, 108, 90).pt("L_HOME", 57.5, 131.75, 270)
    r.add(r.go("R_CATCH", heading=90),
          r.wait("Catch the TIP 3 spill", when=["IntakeFull"], ms=2200),
          r.go("L_EXIT", heading=90), r.go("L_HOME"),
          fire(r, "Fire at the left CELL", "Empty", ms=2000),
          r.go("FAR_FLOWER"),
          r.wait("Collect at FAR_FLOWER (TIP 4)", when=["IntakeFull"], ms=1500),
          r.go("L_HOME"),
          fire(r, "Fire until it tips (TIP 4)", "RightCellUp", ms=2500))
    return r

if __name__ == "__main__":
    # An experiment that lost: no TIP 4 even at 80 in/s: TIP 3 lands at 18-25 s and the robot is not in the roll path when it does. The Java is not committed.
    import sys
    adaptive().write()
    study("FourTipAdaptiveAuto,PartnerLeaveParkAuto@50;FourTipAdaptiveAuto,PartnerPreloadsParkAuto@50",
          runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood, full-width intake|two spring hoods, full-width intake|clump catapult 72 deg, full-width intake",
          extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})
