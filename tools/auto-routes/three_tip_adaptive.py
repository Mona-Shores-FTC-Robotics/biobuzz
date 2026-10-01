from autogen import *
from helpers import waits, fire, flower, flower_points, FAR_FLOWER_AT, WALL_FLOWER_AT

def adaptive(name="three-tip-adaptive", speed=50, staged=False, stops=None, back=None, row_in=(34.6, 114)):
    """staged: the partner doesn't shoot, so it sets its 4 preloads in a row at its side (x 34.6,
    y 128.6-137, AutoStudyTest.LEAVE_PARTNER_STAGED) and parks; we take that row instead of the wall
    FLOWER. It lies beside LEFT_SHOT, and we reach it through the tunnel under the HIVE.
    stops: where to stand, intake first (heading 90), and for how long (ms), to take the row in. The
    intake takes a piece every 0.35 s, so it stops rather than sweeps; an 18 in intake just reaches a
    row beside the partner with the frame 0.5 in clear of it. back: straight back to here
    before turning, so the corners don't swing into the partner. stops None: the partner has driven
    away, so stand below the row and let CollectSeen (the webcam) pick it up; that works better."""
    r = Route(name, (59, 9.5, 90), speed=speed)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180).pt("LEFT_SHOT", 40, 116, 301)
    flower_points(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    r.pt("RIGHT_PLUNGE_IN", 55, 24, 270).pt("RIGHT_PLUNGE", 55, 10.5, 270).pt("RIGHT_SHOT", 36, 30, 49)
    r.pt("GARDEN", 8.5, 11, 270).pt("PARK", 15, 89.5, 90)  # near end of the LOADING ZONE; the partner takes the far end
    right_cps = [(34, 92), (12, 60), (22, 30)]
    # All 4 preloads, and away the moment the last is in the air: this Auto doesn't catch the TIP 1
    # spill, so it has nothing to wait for (mentor review).
    r.add(fire(r, "Fire all 4 preloads (TIP 1)", "Empty", ms=4000))
    if staged:
        r.pt("TUNNEL", 57.5, 100, 90)
        # Through the tunnel square (x 57.5 clears both foot bars), across, then onto the row.
        r.add(r.go("TUNNEL", heading=90))
        if stops is None:
            r.pt("ROW_IN", *row_in, 90).pt("ROW_BACK", row_in[0], row_in[1] + 2, 90)  # ROW_BACK: about where CollectSeen leaves us
            r.add(r.go("ROW_IN", heading=90),
                  r.wait("Pick up the partner's row", when=["IntakeFull"], ms=2500, alongside="CollectSeen"))
            r.at = "ROW_BACK"
            stops = ()
        else:
            r.pt("ROW_BACK", *back, 90)
        for i, (x, y, ms) in enumerate(stops):
            r.pt(f"ROW_{i + 1}", x, y, 90)
            r.add(r.go(f"ROW_{i + 1}", heading=90))
            if ms:  # 0: a point on the way in, straight below the row
                r.add(r.wait(f"Take the partner's row ({i + 1})", when=["IntakeFull"], ms=ms))
        if r.at != "ROW_BACK":
            r.add(r.go("ROW_BACK", heading=90))
    else:
        r.add(*flower(r, "WALL_FLOWER", "Collect at WALL_FLOWER", ms=1500))
    r.add(r.go("LEFT_SHOT", turn_after=0.4, turn_by=1.0),
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
