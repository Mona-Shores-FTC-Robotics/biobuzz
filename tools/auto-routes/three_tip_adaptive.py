from autogen import *
from helpers import waits, fire, flower, flower_points, FAR_FLOWER_AT, WALL_FLOWER_AT

def adaptive(name="three-tip-adaptive", speed=50):
    r = Route(name, (59, 9.5, 90), speed=speed)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180).pt("LEFT_SHOT", 40, 116, 301)
    flower_points(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    r.pt("RIGHT_PLUNGE_IN", 55, 24, 270).pt("RIGHT_PLUNGE", 55, 10.5, 270).pt("RIGHT_SHOT", 36, 30, 49)
    r.pt("GARDEN", 8.5, 11, 270).pt("PARK", 15, 89.5, 90)  # near end of the LOADING ZONE; the partner takes the far end
    right_cps = [(34, 92), (12, 60), (22, 30)]
    tip1 = r.wait("Tip 1?", when=["Tip"], ms=2000, yes_label="Yes", no_label="No",
                  no=[r.action("LaunchOne"), r.wait("Tip 1 (4th POLLEN)", when=["Tip"], ms=1500)])
    r.add(r.action("LaunchOne"), r.action("LaunchOne"), r.action("LaunchOne"), tip1,
          *flower(r, "WALL_FLOWER", "Collect at WALL_FLOWER", ms=1500),
          r.go("LEFT_SHOT", turn_after=0.4, turn_by=1.0),
          r.action("LaunchAll"),
          *flower(r, "FAR_FLOWER", "Collect at FAR_FLOWER", ms=1500),
          r.go("LEFT_SHOT", turn_after=0.4, turn_by=1.0))
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
    r.at = "RIGHT_SHOT"
    r.add(r.go("PARK", ctrl=[(22, 40), (22, 80)], park=True))  # x 22: clear of the wall FLOWER
    return r

if __name__ == "__main__":
    adaptive().write()
    study("ThreeTipAdaptiveAuto@50;ThreeTipAdaptiveAuto,PartnerLeaveParkAuto@50;ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50", runs=10)
