from autogen import *
from helpers import waits, fire, flower, flower_points, FAR_FLOWER_AT, WALL_FLOWER_AT

def adaptive(name="three-tip-adaptive", speed=50, staged=False, stops=None, back=None, row_in=(34.6, 114), one_launcher=True):
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
    # TIP 1's spilled NECTAR lies where the spill lands, 30-50 in out from our wall (3 Oct 2026 films),
    # not against it: look for it from RIGHT_LOOK, facing the HIVE, and take it with the webcam.
    r.pt("RIGHT_LOOK", 50, 27, 72).pt("RIGHT_LOOK_BACK", 49, 24, 72).pt("RIGHT_SHOT", 36, 30, 49)
    r.pt("GARDEN", 8.5, 11, 270).pt("PARK", 15, 89.5, 90)  # near end of the LOADING ZONE; the partner takes the far end
    right_cps = [(34, 92), (12, 60), (22, 30)]
    # All 4 preloads, and away the moment the last is in the air: this Auto doesn't catch the TIP 1
    # spill, so it has nothing to wait for (mentor review).
    # 5.5 s: Empty ends it as soon as the last one is away; 4 s cut a slow launcher (3 s spin-up, 0.6 s/shot) off
    # before its 4th shot, and TIP 1 never came.
    r.add(fire(r, "Fire all 4 preloads (TIP 1)", "Empty", ms=5500))
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
    if one_launcher and not staged:
        return one_launcher_tip3(r)
    r.add(r.go("LEFT_SHOT", turn_after=0.4, turn_by=1.0),
          r.action("LaunchAll"),
          *flower(r, "FAR_FLOWER", "Collect at FAR_FLOWER", ms=1500),
          r.go("LEFT_SHOT", turn_after=0.4, turn_by=1.0))
    # Branch A: left CELL still up (we make TIP 2). Branch B: a partner already made TIP 2.
    r.at = "LEFT_SHOT"
    a = [fire(r, "Fire until it tips (TIP 2)", "RightCellUp", ms=2500),
         r.go("RIGHT_LOOK", ctrl=right_cps), r.wait("Spilled NECTAR", when=["IntakeFull"], ms=1500, alongside="CollectSeen"),
         r.go("RIGHT_LOOK_BACK", turn_after=0.4, turn_by=1.0)]
    r.at = "RIGHT_LOOK_BACK"
    a += [r.go("RIGHT_SHOT"), r.action("LaunchAll"),
         r.go("GARDEN"), r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1000),
         r.go("RIGHT_SHOT"), fire(r, "Fire until it tips (TIP 3)", "LeftCellUp", ms=2500)]
    r.at = "LEFT_SHOT"
    b = [r.go("RIGHT_SHOT", ctrl=right_cps), r.action("LaunchAll"),
         r.go("RIGHT_LOOK"), r.wait("Spilled NECTAR (B)", when=["IntakeFull"], ms=1500, alongside="CollectSeen"),
         r.go("RIGHT_LOOK_BACK", turn_after=0.4, turn_by=1.0)]
    r.at = "RIGHT_LOOK_BACK"
    b += [r.go("RIGHT_SHOT"), fire(r, "Fire (B)", "Empty", ms=2000)]
    r.at = "RIGHT_SHOT"
    more = [r.go("GARDEN"), r.wait("Collect in the GARDEN (B)", when=["IntakeFull"], ms=1000),
            r.go("RIGHT_SHOT"), fire(r, "Fire the TIP 3 volley, then park", "Empty", ms=2000)]
    b.append(r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=1500, yes=[], no=more, yes_label="Yes: park", no_label="No: the GARDEN"))
    r.add(r.wait("Did a partner make TIP 2?", when=["RightCellUp"], ms=50, yes=b, no=a,
                 yes_label="Yes: right CELL up", no_label="No: TIP 2 is ours"))
    r.at = "RIGHT_SHOT"
    r.add(r.go("PARK", ctrl=[(22, 40), (22, 80)], park=True))  # x 22: clear of the wall FLOWER
    return r

def one_launcher_tip3(r, fire_ms=1800, settle_ms=1500, seen_ms=1500):
    """After the wall FLOWER, for one launcher (4 Oct 2026; the robot being built). With one launcher a
    volley of 4 takes 3 shot intervals, so this version spends less time standing and driving:
    - fire at the left CELL until empty, then the far FLOWER, and decide there (not back at LEFT_SHOT)
      whether TIP 2 has happened; it usually has (about 12 s), so straight down the west side;
    - fire the far FLOWER's 4 from SHOT, straight below the right CELL (ShotMapTest: the 75 deg hood
      scores from y 13-29), then drive up the middle of the TIP 1 spill intake first in short steps
      (the webcam pickup pushed the NECTAR aside and took 2 pieces) and fire from SHOT again;
    - the GARDEN if TIP 3 still hasn't come.
    TIP 2 not yet at the far FLOWER: back to LEFT_SHOT and fire until it tips, as before."""
    r.pt("SHOT", 57.5, 20, 90).pt("SWEEP_1", 57.5, 30, 90).pt("SWEEP_2", 57.5, 38, 90).pt("SHOT_G", 22, 22, 50)
    r.pt("GARDEN_IN", 8.5, 22, 270)
    r.add(r.go("LEFT_SHOT", turn_after=0.4, turn_by=1.0),
          fire(r, "Fire at the left CELL (TIP 2)", "Empty", ms=fire_ms),
          *flower(r, "FAR_FLOWER", "Collect at FAR_FLOWER", ms=1500))
    # Straight back off the FLOWER, then down the west side still facing 90 (no turn needed: SHOT faces
    # 90 too): x 32 keeps the frame 4 in clear of a partner parked at the far-left end (x up to 19.5,
    # y 102-120) and of the HIVE frame's foot bar (x 45-47, y 51-90), and it crosses east only below it.
    r.pt("WEST", 32, 96, 90)

    def sweep(label):
        # The spill lands differently each time (x 36-77): where the straight sweep misses, the
        # webcam finds what's left.
        out = [r.go("SWEEP_1", heading=90), r.wait(f"{label} (1)", when=["IntakeFull"], ms=500),
               r.go("SWEEP_2", heading=90), r.wait(f"{label} (2)", when=["IntakeFull"], ms=700),
               r.wait(f"{label} (webcam)", when=["IntakeFull"], ms=seen_ms, alongside="CollectSeen")]
        r.at = "SWEEP_2"
        return out + [r.go("SHOT", heading=90), fire(r, f"Fire {label.split(' ', 1)[1]}", "Empty", ms=fire_ms)]

    def park():
        return r.go("PARK", ctrl=[(30, 30), (22, 80)], park=True)

    r.at = "FAR_FLOWER"
    b = [r.go("FAR_FLOWER_TURN", heading=90), r.go("WEST", ctrl=[(40, 110)], heading=90),
         r.go("SHOT", ctrl=[(30, 50), (34, 22)], heading=90), fire(r, "Fire the far FLOWER (TIP 3)", "Empty", ms=fire_ms)]
    r.at = "SHOT"
    b += sweep("Sweep the TIP 1 spill")
    r.at = "SHOT"
    tipped = [r.go("SHOT_G")]  # before the GARDEN's paths, so no path in the file joins SHOT_G to itself
    r.at = "SHOT"
    garden = [r.go("GARDEN_IN", turn_after=0.2, turn_by=0.7), r.go("GARDEN"),
              r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1000),
              r.go("SHOT_G", turn_after=0.3, turn_by=0.9), fire(r, "Fire the GARDEN (TIP 3)", "LeftCellUp", ms=fire_ms)]
    r.at = "SHOT"
    # The branch ends in its park path, not in this wait: the endgame guard never cuts a branch's
    # last card short (it takes it for the park), so a wait there ran into the park's time.
    b += [r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=settle_ms, yes=tipped, no=garden,
                 yes_label="Yes", no_label="No: the GARDEN")]
    r.at = "SHOT_G"
    b.append(park())
    # TIP 2 hasn't come: back to LEFT_SHOT with the far FLOWER's 4 and fire until it tips; then the
    # TIP 1 spill for TIP 3, as far as time allows.
    r.at = "FAR_FLOWER"
    a = [r.go("LEFT_SHOT", turn_after=0.4, turn_by=1.0), fire(r, "Fire until it tips (TIP 2)", "RightCellUp", ms=2500),
         r.go("SHOT", ctrl=[(34, 92), (12, 60), (22, 18)], turn_by=0.8)]
    r.at = "SHOT"
    a += sweep("Sweep the TIP 1 spill (A)")
    r.at = "SHOT"
    a.append(park())
    r.add(r.wait("Has TIP 2 happened?", when=["RightCellUp"], ms=50, yes=b, no=a,
                 yes_label="Yes: right CELL up", no_label="No: back to the left CELL"))
    return r


if __name__ == "__main__":
    adaptive().write()
    study("ThreeTipAdaptiveAuto@50;ThreeTipAdaptiveAuto,PartnerLeaveParkAuto@50;ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50", runs=10)
