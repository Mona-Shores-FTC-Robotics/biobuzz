"""solo-tunnel: our robot alone, for qualification, written to the mentor review rules (1 Oct 2026).

- Fire all 4 preloads at the start.
- Stand still in front of a CELL that is tipping, intake toward it, until the spill fills the robot
  (IntakeFull) or about 1.5 s after the TIP; then go.
- Carry the spill through the tunnel under the HIVE (centre x 57.5, square to the field), never round
  past the wall FLOWER.
- Shoot head-on from the home spots, about 39 in from each opening.
- Visit each source once: the far FLOWER at the left end, the GARDEN at the right. (No CollectSeen:
  the robot caught each spill, so nothing is left lying, and beside the FLOWER it would chase pieces
  into the holder.)
- Park well inside the LOADING ZONE, off the wall, at its right end, leaving the left end for the partner.
"""
import sys
from helpers import *

R_HOME, L_HOME = (57.5, 10, 90), (59, 131.75, 270)  # 58.5: clear of the far FLOWER, turns clear of the centre line
R_EXIT, L_EXIT = (57.5, 34, 90), (56.5, 103, 90)  # out of the HIVE (frame ends 90.2) before turning


def solo(name="solo-tunnel", speed=50, park=True):
    r = Route(name, (59, 9.5, 90), speed=speed)
    r.pt("R_HOME", *R_HOME).pt("L_HOME", *L_HOME).pt("R_EXIT", *R_EXIT).pt("L_EXIT", *L_EXIT)
    r.pt("L_EXIT_BACK", 56.5, 103, 270).pt("R_EXIT_BACK", 57.5, 34, 270)
    flower_points(r, "FLOWER_L", FAR_FLOWER_AT, 90)
    r.pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    r.pt("R_BACK", 59, 11, 90)  # home again after CollectSeen wandered (a path needs two ends)
    r.pt("PARK", 15, 89.5, 90).pt("PARK2", 15, 90.5, 90)  # the near end of the LOADING ZONE; the partner takes the far end

    def catch(label, tipped=False):
        # The spill lands 1.0-1.45 s after the CELL is 5 deg off its stop, where the robot stands.
        # tipped: the CELL has already gone over (a RightCellUp/LeftCellUp wait saw it), so the Tip
        # trigger, which only sees a TIP that starts while it waits, would never come.
        out = [] if tipped else [r.wait(f"{label}: TIP?", when=["Tip"], ms=2500)]
        return out + [r.wait(f"{label}: catch the spill", when=["IntakeFull"], ms=1600)]

    # TIP 1, right CELL: all 4 preloads, then catch its spill (the 3 NECTAR come back).
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000), *catch("TIP 1"))

    # Through the tunnel to the left CELL: fire what we carry; if it hasn't tipped, the far FLOWER once.
    r.at = "START"
    # Turn while x <= 58: a turning robot's corners reach 12.7 in, and the centre line is at 70.75.
    r.add(r.go("L_EXIT", heading=90), r.go("L_HOME", turn_by=0.4),
          fire(r, "Fire at the left CELL", "Empty", ms=2000))
    # Not tipped: look west with the webcam for loose pieces (a partner that only leaves sets its
    # preloads out there); if it finds none, the far FLOWER, once.
    r.pt("L_LOOK", 38, 124, 180).pt("L_LOOK_BACK", 57.5, 121, 270)
    r.at = "L_HOME"
    look = r.go("L_LOOK", ctrl=[(59, 121)], turn_after=0.5, turn_by=1.0)  # south first: the FLOWER is beside home
    r.at = "L_LOOK"
    found = [r.go("L_LOOK_BACK", turn_after=0.3, turn_by=1.0), r.go("L_HOME", ctrl=[(59, 123)], heading=270)]
    r.at = "L_LOOK"
    none = [*flower(r, "FLOWER_L", "Collect at the far FLOWER", ms=1800), *leave_flower(r, "FLOWER_L", "L_HOME")]
    r.at = "L_LOOK"
    top_l = [look,
             r.wait("Loose pieces in view?", when=["IntakeFull"], ms=2500, alongside="CollectSeen",
                    yes=found, no=none, yes_label="Found: back", no_label="None: the far FLOWER"),
             fire(r, "Fire again (TIP 2)", "RightCellUp", ms=2500)]
    r.at = "L_HOME"
    r.at = "L_HOME"
    r.add(r.wait("TIP 2 yet?", when=["RightCellUp"], ms=1200, yes=[], no=top_l,
                 yes_label="Yes", no_label="No: top up"), *catch("TIP 2", tipped=True))

    # Back through the tunnel to the right CELL: fire, CollectSeen what TIP 1 left, then the GARDEN once.
    r.at = "L_HOME"
    r.add(r.go("L_EXIT_BACK", heading=270, turn_by=0.5), r.go("R_EXIT_BACK", heading=270), r.go("R_HOME", turn_by=0.5),
          fire(r, "Fire at the right CELL", "Empty", ms=2000))
    r.at = "R_HOME"
    garden = [r.go("GARDEN_IN", ctrl=[(30, 22)]), r.go("GARDEN"),
              r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1500),
              r.go("R_BACK", ctrl=[(20, 18)]), fire(r, "Fire again (TIP 3)", "LeftCellUp", ms=2500)]
    r.at = "R_HOME"
    r.at = "R_HOME"
    # TIP 1's spill was caught, so nothing is left on the tiles here: straight to the GARDEN.
    r.add(r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=1000, yes=[], no=garden,
                 yes_label="Yes", no_label="No: the GARDEN"))
    if park:
        r.at = "R_HOME"
        r.add(r.go("PARK", ctrl=[(24, 24), (24, 85)]), r.go("PARK2", park=True))
    return r


if __name__ == "__main__":
    solo().write()
    study("SoloTunnelAuto,PartnerLeaveParkAuto@50;SoloTunnelAuto,PartnerPreloadsParkAuto@50;"
          "ThreeTipAdaptiveAuto,PartnerLeaveParkAuto@50;ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50",
          runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10,
          designs=sys.argv[2] if len(sys.argv) > 2 else "spring hood, 24 in catcher|two spring hoods, 24 in catcher|clump catapult 72 deg, 24 in catcher",
          extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})
