"""Sister: both robots ours (mentor, 8 Oct 2026, overnight: "the maximum number of tips + 2 park ... using two copies of
our own robot ... imagine that we pick our sister team for the playoffs"). The Rigid V with its turret on both.

A TIP needs 8 pieces in the raised CELL (TIP 1: 4, its CELL holds 3 NECTAR from the start). TIPs alternate CELLs, and a
CELL opens toward its own end, so the left CELL is fired at from the left (north) end and the right CELL from the right
(south) end. Each robot holds 4. The plan, one robot an end, pieces carried across when an end runs out:

    TIP 1  R: its preloads at the right CELL from the right start.
    TIP 2  L: seated at the far FLOWER, its preloads and the FLOWER's 4 streamed into the left CELL as it rises.
    TIP 3  R: TIP 1's catch the moment the right CELL rises, then the GARDEN's 4.
    TIP 4  L: TIP 2's catch, held at N_FIRE until the left CELL rises; R: TIP 3's catch, carried up the left side.
    TIP 5  R: the wall FLOWER's 4; L: TIP 4's catch, carried down the lane.
    PARK   R at the LOADING ZONE's near end, L at its far end.

    python3 sister.py      writes them into experiments/
"""
import autogen
from autogen import Route
from alone import seat, S_CATCH, N_FIRE
from helpers import FAR_FLOWER_AT, WALL_FLOWER_AT, fire

R_START, L_START = (59, 8.06, 90), (59, 133.69, 270)


# Firing spots from a scan of the turret V (60 spots, 10 runs each, 8 Oct 2026): the right CELL is clean from y <= 30
# (x 42-66; at y 36 only x 36-42), the left CELL from y >= 110 (and x 30-36 at y 104). Each robot has its own, clear
# of the other's V (its tips 8.9 in either side).
# R_N x 30 (L turns at L_N, its V to x 41.35), y 104 (clean in the scan; at y 100 R missed 1-3 of 4; L parks west along
# y 127, its front 2.2 in above R's V at y 114.4).
R_S, R_S2, R_N = (40, 22, 90), (36, 30, 90), (30, 104, 90)
L_N, L_TURN, L_S = (55, 116, 270), (57, 36, 90), (59, 30, 90)


def right(name="sister-right", tips=4):
    r = Route(name, R_START, speed=50)
    r.pt("S_CATCH", *S_CATCH).pt("GARDEN_IN", 9.5, 20.56, 270).pt("GARDEN", 9.5, 10.96, 270)
    r.pt("R_S", *R_S).pt("R_S2", *R_S2).pt("R_N", *R_N).pt("PARK", 10.5, 95, 90)
    seat(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    # TIP 1: the preloads from the start; to S_CATCH for its spill.
    # Preloads from R_PRE, 12 in out (from the start 15 of 240 shots hit the HIVE and TIP 1 came late in 8 of 60;
    # the scan's y 18-24 is clean); the drive is inside the flywheel's 2 s spin-up.
    r.pt("R_PRE", 57, 20, 90)
    r.add(r.action("SpinUp"), r.go("R_PRE", heading=90))
    r.at = "R_PRE"
    r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000), r.go("S_CATCH", heading=90))
    r.at = "S_CATCH"
    retry = [r.wait("TIP 1 missed: catch", when=["IntakeFull"], ms=1500),
             fire(r, "TIP 1 missed: fire the catch", "Tip", ms=3000)]
    r.add(r.wait("TIP 1", when=["Tip"], ms=4000, no=retry),
          r.wait("Catch TIP 1's spill", when=["IntakeFull"], ms=1500),
          # TIP 3: the catch the moment L's TIP 2 brings the right CELL up, then the GARDEN's 4 (L adds TIP 2's spill).
          r.wait("TIP 2 (L): the right CELL up", when=["RightCellUp"], ms=10000),
          fire(r, "TIP 1's catch at the right CELL", "Empty", ms=2000),
          r.go("GARDEN_IN", turn_after=0.1, turn_by=0.7))
    r.at = "GARDEN_IN"
    r.add(r.go("GARDEN", heading=270))
    r.at = "GARDEN"
    r.add(r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500), r.go("R_S", turn_after=0.2, turn_by=0.8))
    r.at = "R_S"
    r.add(r.wait("The right CELL up", when=["RightCellUp"], ms=3000),
          fire(r, "The GARDEN's 4 (TIP 3)", "Tip", ms=2500))
    if tips >= 4:
        # TIP 4: the wall FLOWER's 4 (sure; TIP 3's spill gave R 0-2), carried up the left side to R_N.
        r.add(r.go("WALL_FLOWER_TURN", turn_after=0.2, turn_by=0.8))
        r.at = "WALL_FLOWER_TURN"
        r.add(r.go("WALL_FLOWER", heading=180))
        r.at = "WALL_FLOWER"
        r.add(r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=3000),
              r.go("R_N", ctrl=[(30, 80)], turn_after=0.3, turn_by=0.9))
        r.at = "R_N"
        r.add(r.wait("The left CELL up", when=["LeftCellUp"], ms=4000),
              fire(r, "The wall FLOWER's 4 at the left CELL (TIP 4, with L)", "Empty", ms=2000))
    # PARK: from R_N it is a short drive into the LOADING ZONE's near end.
    r.add(r.go("PARK", heading=90, park=True))
    return r


def left(name="sister-left", tips=4, stream_ms=2600, settle_ms=500, catch3_ms=3000, creep=None, keep1=False):
    r = Route(name, L_START, speed=50)
    r.pt("L_N", *L_N).pt("L_TURN", *L_TURN).pt("L_S", *L_S).pt("PARK_L", 10.5, 120, 270)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)

    def lane_down(label):
        # Down the lane facing south through the spill (it lands just south of L_N), the turn north below the HIVE
        # frame's feet (y 51-90) at L_TURN (x 57: its corners to 70.65, inside the centre line), then across to L_S.
        r.at = "L_N"
        cards = [r.wait(label, when=["IntakeFull"], ms=settle_ms),
                 r.go("L_TURN", ctrl=[(57.5, 100), (57.5, 50)], turn_after=0.88, turn_by=1.0)]
        r.at = "L_TURN"
        cards.append(r.go("L_S", heading=90))
        r.at = "L_S"
        return cards

    # TIP 2: seated at the far FLOWER before TIP 1; once the left CELL rises, the preloads and the FLOWER's 4 streamed.
    r.add(r.action("SpinUp"), r.go("FAR_FLOWER_TURN", ctrl=[(59, 118.34)], turn_after=0.3, turn_by=0.9))
    r.at = "FAR_FLOWER_TURN"
    r.add(r.go("FAR_FLOWER", heading=90))
    r.at = "FAR_FLOWER"
    r.add(r.wait("Seated", when=["Empty"], ms=400),
          # Seated until TIP 1 however late (R's retry makes it): streaming before it fires at the other CELL.
          r.wait("TIP 1 (R): the left CELL up", when=["LeftCellUp"], ms=15000), r.action("StreamOn"),
          r.wait("Preloads and the far FLOWER's 4, streamed (TIP 2)", when=["Tip"], ms=stream_ms),
          r.action("StreamOff"), r.go("L_N", turn_after=0.3, turn_by=1.0))
    r.at = "L_N"
    # TIP 3: TIP 2's spill caught on the way down the lane, fired with R's catch and the GARDEN.
    r.add(r.wait("TIP 2 started", when=["RightCellUp"], ms=4000), *lane_down("TIP 2's spill lands"))
    if keep1:
        # 3 of the 4 (TIP 3 has R's catch and the GARDEN as well): the 4th rides to TIP 4. TIP 3's spill is mostly
        # NECTAR and the lane takes 3 NECTAR and a POLLEN, so a full catch is 3, and TIP 4 came up 7 of 8 in 9 of 60.
        r.add(*[r.action("LaunchOne") for _ in range(3)])
    else:
        r.add(fire(r, "TIP 2's catch at the right CELL (TIP 3, with R)", "Empty", ms=2000))
    if tips >= 4:
        # TIP 4: TIP 3's spill caught facing the HIVE at L_S, carried up the lane, turned at L_N, fired with R's.
        r.add(r.wait("TIP 3", when=["LeftCellUp"], ms=6000))
        frm = "L_S"
        if creep is not None:  # forward into the spill once it has landed, intake first
            r.pt("L_C", *creep)
            r.add(r.wait("TIP 3's spill lands", when=["IntakeFull"], ms=800), r.go("L_C", heading=90))
            r.at = frm = "L_C"
        r.add(r.wait("Catch TIP 3's spill", when=["IntakeFull"], ms=catch3_ms),
              r.go("L_N", ctrl=[(57.5, 50), (57.5, 104)], turn_after=0.85, turn_by=1.0))
        r.at = "L_N"
        r.add(fire(r, "TIP 3's catch at the left CELL (TIP 4, with R)", "Empty", ms=2000))
    # PARK: from L_N still facing south, west along y 127 (above R at R_N) into the LOADING ZONE's far end.
    r.add(r.go("PARK_L", ctrl=[(44, 127), (24, 127)], heading=270, park=True))
    return r


def variants():
    return [right(), left(), left("sister-left-creep", creep=(58, 40, 90), catch3_ms=2000),
            left("sister-left-keep1", creep=(58, 40, 90), catch3_ms=2000, keep1=True)]


if __name__ == "__main__":
    for r in variants():
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(r.name)
