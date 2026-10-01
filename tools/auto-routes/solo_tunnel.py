"""solo-tunnel: our robot alone, for qualification, written to the mentor review rules (1 Oct 2026).

- Fire all 4 preloads at the start.
- Stand still in front of a CELL that is tipping, intake toward it, until the spill fills the robot
  (IntakeFull) or about 1.5 s after the TIP; then go.
- Carry the spill through the tunnel under the HIVE (centre x 57.5, square to the field), never round
  past the wall FLOWER.
- Shoot head-on from the home spots, about 39 in from each opening.
- Visit each source once: what a leave-only partner staged, the far FLOWER, the GARDEN.
- Park well inside the LOADING ZONE, off the wall, at its right end, leaving the left end for the partner.

Two plans, chosen by whether the left CELL has tipped (TIP 2) when we have fired at it:
- Yes (the partner fired its preloads into it): back through the tunnel for TIP 3, then park.
- No (the partner only left): collect what it staged, fire, the far FLOWER if still needed, fire,
  catch TIP 2 and park from the left end. There is no time left for TIP 3.
Each plan ends in its own park path, so the endgame guard parks from wherever that plan is.
"""
import sys
from helpers import *

R_HOME, L_HOME = (57.5, 10, 90), (59, 131.75, 270)  # 59: clear of the far FLOWER, turns clear of the centre line
L_EXIT = (56.5, 103, 90)  # out of the HIVE (frame ends 90.2) before turning


def solo(name="solo-tunnel", speed=50, park=True):
    r = Route(name, (59, 9.5, 90), speed=speed)
    r.pt("R_HOME", *R_HOME).pt("L_HOME", *L_HOME).pt("L_EXIT", *L_EXIT)
    r.pt("L_EXIT_BACK", 56.5, 103, 270).pt("R_EXIT_BACK", 57.5, 34, 270)
    flower_points(r, "FLOWER_L", FAR_FLOWER_AT, 90)
    r.pt("ROW_IN", 34.6, 112, 90)  # below the partner's staged row (x 34.6, y 128.6-137), facing it
    r.pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    r.pt("R_BACK", 59, 11, 90)  # home again after the GARDEN (a path needs two ends)
    r.pt("PARK", 15, 90, 90)  # the near end of the LOADING ZONE; the partner takes the far end
    r.pt("PARK_L", 15, 89, 270)  # the same spot facing the other way: no turn beside the parked partner

    def catch(label, tipped=False):
        # The spill lands 1.0-1.45 s after the CELL is 5 deg off its stop, where the robot stands.
        # tipped: the CELL has already gone over (a RightCellUp/LeftCellUp wait saw it), so the Tip
        # trigger, which only sees a TIP that starts while it waits, would never come.
        out = [] if tipped else [r.wait(f"{label}: TIP?", when=["Tip"], ms=2500)]
        return out + [r.wait(f"{label}: catch the spill", when=["IntakeFull"], ms=1600)]

    # TIP 1, right CELL: all 4 preloads, then catch its spill (the 3 NECTAR come back).
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000), *catch("TIP 1"))

    # Through the tunnel to the left CELL and fire what we carry.
    r.at = "START"
    # Turn while x <= 58: a turning robot's corners reach 12.7 in, and the centre line is at 70.75.
    r.add(r.go("L_EXIT", heading=90), r.go("L_HOME", turn_by=0.4),
          fire(r, "Fire at the left CELL", "Empty", ms=2000))

    # Plan A, TIP 2 already: catch it, back through the tunnel, fire; the GARDEN once if no TIP 3.
    r.at = "L_HOME"
    early = [*catch("TIP 2", tipped=True),
             r.go("L_EXIT_BACK", heading=270, turn_by=0.5), r.go("R_EXIT_BACK", heading=270),
             r.go("R_HOME", turn_by=0.5), fire(r, "Fire at the right CELL", "Empty", ms=2000)]
    def park_right():
        r.at = "R_HOME"
        return [r.go("PARK", ctrl=[(24, 24), (24, 85)], park=True)] if park else []

    # Each branch ends in the park, so the endgame guard can step in between the GARDEN's steps.
    r.at = "R_HOME"
    garden = [r.go("GARDEN_IN", ctrl=[(30, 22)]), r.go("GARDEN"),
              r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1500),
              r.go("R_BACK", ctrl=[(20, 18)]), fire(r, "Fire again (TIP 3)", "LeftCellUp", ms=2500), *park_right()]
    early.append(r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=1000, yes=park_right(), no=garden,
                        yes_label="Yes", no_label="No: the GARDEN"))

    # Plan B, no TIP 2 yet: the partner only left, and set its preloads in a row along its field
    # side. Swing round to below the row and drive up it intake first (mentor review: face them
    # before you reach them), come back the same way, and fire whatever we got; then the far
    # FLOWER once if the CELL still hasn't tipped (straight up from below the row, no turn).
    r.at = "L_HOME"
    late = [r.go("ROW_IN", ctrl=[(57.5, 114)], turn_after=0.2, turn_by=0.8)]
    def home():  # back the way we came
        r.at = "ROW_IN"
        return [r.go("L_HOME", ctrl=[(57.5, 114)], turn_after=0.2, turn_by=0.8)]
    full, some = home(), home()
    r.at = "ROW_IN"
    none = [*flower(r, "FLOWER_L", "Collect at the far FLOWER", ms=1800), *leave_flower(r, "FLOWER_L", "L_HOME")]
    late.append(r.wait("Pick up the row", when=["IntakeFull"], ms=2500, alongside="CollectSeen",
                       yes=full, yes_label="Full: home", no_label="Time up",
                       no=[r.wait("Got any?", when=["Empty"], ms=100, yes=none, no=some,
                                  yes_label="None: the far FLOWER", no_label="Some: home")]))
    r.at = "L_HOME"
    late.append(fire(r, "Fire again (TIP 2)", "RightCellUp", ms=2500))
    r.at = "L_HOME"
    top_up = [*flower(r, "FLOWER_L", "Collect at the far FLOWER (2)", ms=1800), *leave_flower(r, "FLOWER_L", "L_HOME"),
              fire(r, "Fire once more (TIP 2)", "RightCellUp", ms=2500)]
    r.at = "L_HOME"
    late += [r.wait("TIP 2 now?", when=["RightCellUp"], ms=100, yes=[], no=top_up,
                    yes_label="Yes", no_label="No: the far FLOWER"),
             *catch("TIP 2", tipped=True)]
    if park:
        r.at = "L_HOME"
        # The gap between the end of the HIVE frame's foot bar (x 46, to y 90.2) and the partner
        # parked at the far-left end (x up to 19.5, y from 101) is about 7 in wide for our centre:
        # straight down, west above the bar, down the gap, west in. Doubled control points pull the
        # curve into the corners; it clears all three by about 0.6 in, which a real robot may not.
        late.append(r.go("PARK_L", ctrl=[(59, 104), (59, 104), (32, 110), (32, 110), (32, 86), (32, 86)],
                         heading=270, park=True))

    r.at = "L_HOME"
    # 2.5 s: a partner that fires its preloads when the left CELL rises tips it about 11 s in.
    r.add(r.wait("TIP 2 yet?", when=["RightCellUp"], ms=2500, yes=early, no=late,
                 yes_label="Yes: on to TIP 3", no_label="No: top up here"))
    return r


if __name__ == "__main__":
    solo().write()
    study("SoloTunnelAuto,PartnerLeaveParkAuto@50;SoloTunnelAuto,PartnerPreloadsParkAuto@50;"
          "ThreeTipAdaptiveAuto,PartnerLeaveParkAuto@50;ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50",
          runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood, 24 in catcher|two spring hoods, 24 in catcher|clump catapult 72 deg, 24 in catcher",
          extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})
