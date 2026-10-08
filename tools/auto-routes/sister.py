"""Sister: both robots ours (mentor, 8 Oct 2026, overnight: "the maximum number of tips + 2 park ... using two copies of
our own robot ... imagine that we pick our sister team for the playoffs"). The Rigid V on both.

A TIP needs 8 pieces in the raised CELL (TIP 1: 4, its CELL holds 3 NECTAR from the start). TIPs alternate CELLs, and a
CELL opens toward its own end, so the left CELL is fired at from the left end and the right CELL from the right end.
Each robot holds 4. The plan, one robot an end, pieces carried across when an end runs out:

    TIP 1  R: its preloads at the right CELL from R_PRE, then stands at S_CATCH for the spill.
    TIP 2  L: its preloads and the far FLOWER's 4 at the left CELL once TIP 1 raises it (a turret streams them seated
           at the FLOWER; a fixed launcher fires the preloads from FAR_FLOWER_TURN and the FLOWER's 4 from L_N).
    TIP 3  R: TIP 1's catch the moment the right CELL rises, then the GARDEN's 4; L: TIP 2's spill, caught on the way
           down the lane, 3 of it (the 4th is kept for TIP 4).
    TIP 4  R: the wall FLOWER's 4 (or the GARDEN's, if TIP 3 came without them), carried up the left side to R_N;
           L: TIP 3's spill and the kept piece, carried up the lane.
    PARK   R at the LOADING ZONE's near end, L at its far end.

sister-right + sister-left for a turret, sister-right-fixed + sister-left-fixed for a fixed launcher (each loses to the
other on the other body).

No TIP 5 with 2 PARK. A TIP counts for AUTO if it completes before TELEOP (§10.5 B, so up to 38 s), but its pieces
must be in by about 29.5 s, fired from the right end; PARK is judged at 30 s at the left end, and the only pieces left
then are TIP 4's spill, landing at the left end at 24-27 s.

TIP 2 is L's 8 exactly. When 1 hits the HIVE (4 of 60 on the turret) R rescues it from R_N with TIP 1's catch, and L
parks.

G409 (no catching a spill before it touches something else; a VERBAL WARNING, a YELLOW CARD if STRATEGIC): no robot
stands within reach of a spill in the air. At y 28-30 under the right CELL the V met falling pieces in 54 of 60.

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
# R_N x 30 (L turns at L_N, its V to x 41.35), y 104 (clean in the scan; at y 100 R missed 1-3 of 4; L parks along
# y 127, its front 2.2 in above R's V at y 114.4).
R_S, R_N = (40, 22, 90), (30, 104, 90)
L_N, L_TURN, L_S = (55, 116, 270), (57, 36, 90), (59, 22, 90)


def right(name="sister-right", rescue_ms=6500, catch_at=(57.5, 21, 90)):
    r = Route(name, R_START, speed=50)
    r.pt("S_CATCH", *S_CATCH).pt("GARDEN_IN", 9.5, 20.56, 270).pt("GARDEN", 9.5, 10.96, 270)
    r.pt("R_S", *R_S).pt("R_N", *R_N).pt("PARK", 10.5, 95, 90)
    seat(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    # TIP 1: the preloads from the start; to S_CATCH for its spill.
    # Preloads from R_PRE, 12 in out (from the start 15 of 240 shots hit the HIVE and TIP 1 came late in 8 of 60;
    # the scan's y 18-24 is clean); the drive is inside the flywheel's 2 s spin-up.
    r.pt("R_PRE", 57, 20, 90)
    r.add(r.action("SpinUp"), r.go("R_PRE", heading=90))
    r.at = "R_PRE"
    if catch_at is not None:
        # G409 (no catching a spill before it touches something else): at S_CATCH (y 28) the V met pieces in the air
        # in 1 run of 4; held at R_PRE and driven in once it landed, it caught nothing (the pieces bounced off a moving
        # intake). Standing still, further back.
        r.pt("S_CATCH", *catch_at)
    r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000), r.go("S_CATCH", heading=90))
    r.at = "S_CATCH"
    retry = [r.wait("TIP 1 missed: catch", when=["IntakeFull"], ms=1500),
             fire(r, "TIP 1 missed: fire the catch", "Tip", ms=3000)]
    r.add(r.wait("TIP 1", when=["Tip"], ms=4000, no=retry),
          r.wait("Catch TIP 1's spill", when=["IntakeFull"], ms=1500))
    n0 = len(r.cards)
    if rescue_ms is None:
        r.add(r.wait("TIP 2 (L): the right CELL up", when=["RightCellUp"], ms=10000))
    # TIP 3: the catch the moment L's TIP 2 brings the right CELL up, then the GARDEN's 4 (L adds TIP 2's spill).
    r.add(fire(r, "TIP 1's catch at the right CELL", "Empty", ms=2000),
          r.go("GARDEN_IN", turn_after=0.1, turn_by=0.7))
    r.at = "GARDEN_IN"
    r.add(r.go("GARDEN", heading=270))
    r.at = "GARDEN"
    r.add(r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500), r.go("R_S", turn_after=0.2, turn_by=0.8))
    r.at = "R_S"
    # TIP 3 can be done before R gets here (L's 3 and TIP 1's catch; on the fixed launcher, R then waited at R_S and
    # TIP 4 came up short in 4 of 60): then the GARDEN's 4 go straight up the left side for TIP 4, no wall FLOWER.
    r.pt("R_W", 30, 40, 90)
    early = [r.go("R_W", heading=90)]
    r.at = "R_W"
    early.append(r.go("R_N", heading=90))
    def normal():
        r.at = "R_S"
        cards = [fire(r, "The GARDEN's 4 (TIP 3)", "Tip", ms=2500),
                 # TIP 4: the wall FLOWER's 4 (sure; TIP 3's spill gave R 0-2), carried up the left side to R_N.
                 r.go("WALL_FLOWER_TURN", turn_after=0.2, turn_by=0.8)]
        r.at = "WALL_FLOWER_TURN"
        cards.append(r.go("WALL_FLOWER", heading=180))
        r.at = "WALL_FLOWER"
        cards += [r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=3000),
                  r.go("R_N", ctrl=[(30, 80)], turn_after=0.3, turn_by=0.9)]
        return cards

    # R often gets here mid-TIP, with neither CELL up: the right CELL up, TIP 3; else, the left up, TIP 4.
    done = r.wait("TIP 3 done?", when=["LeftCellUp"], ms=50, yes=early, no=normal(),
                  yes_label="Yes: the GARDEN's 4 to TIP 4", no_label="No: the GARDEN's 4 to TIP 3")
    which = r.wait("The right CELL up", when=["RightCellUp"], ms=1500, yes=normal(), no=[done])
    r.at = "R_N"
    r.add(which, r.wait("The left CELL up", when=["LeftCellUp"], ms=4000),
          fire(r, "R's 4 at the left CELL (TIP 4, with L)", "Empty", ms=2000))
    if rescue_ms is not None:
        # TIP 2 is L's 8 exactly, and 1 of them hit the HIVE in 4 of 60: L then had nothing left, came down the lane
        # anyway and met R waiting at S_CATCH. Not up by rescue_ms (the latest TIP 2 was 5.2 s after the catch): R
        # takes TIP 1's catch up the left side to R_N and fires at the left CELL until it tips, clear of L's lane.
        yes = r.cards[n0:]
        del r.cards[n0:]
        r.at = "S_CATCH"
        # Out to x 30 first: a curve straight up met the HIVE frame's foot (x 46, y 51-90).
        no = [r.go("R_W", heading=90)]
        r.at = "R_W"
        no.append(r.go("R_N", heading=90))
        r.at = "R_N"
        no.append(fire(r, "No TIP 2: TIP 1's catch at the left CELL (TIP 2)", "Empty", ms=3000))
        r.add(r.wait("TIP 2 (L): the right CELL up", when=["RightCellUp"], ms=rescue_ms, yes=yes, no=no))
    # PARK: from R_N it is a short drive into the LOADING ZONE's near end.
    r.add(r.go("PARK", heading=90, park=True))
    return r


def seated_stream(r, stream_ms):
    """TIP 2 with a turret: seated at the far FLOWER before TIP 1; once the left CELL rises, the preloads and the
    FLOWER's 4 streamed."""
    r.add(r.go("FAR_FLOWER", heading=90))
    r.at = "FAR_FLOWER"
    return [r.wait("Seated", when=["Empty"], ms=400),
            # Seated until TIP 1 however late (R's retry makes it): streaming before it fires at the other CELL.
            r.wait("TIP 1 (R): the left CELL up", when=["LeftCellUp"], ms=15000), r.action("StreamOn"),
            r.wait("Preloads and the far FLOWER's 4, streamed (TIP 2)", when=["Tip"], ms=stream_ms),
            r.action("StreamOff"), r.go("L_N", turn_after=0.3, turn_by=1.0)]


def left(name="sister-left", tips=4, stream_ms=2600, settle_ms=500, catch3_ms=2000, creep=(58, 40, 90), keep1=True,
         tip2_wait_ms=4000, bail_ms=4500, l_s=L_S, fixed=False):
    r = Route(name, L_START, speed=50)
    r.pt("L_N", *L_N).pt("L_TURN", *L_TURN).pt("L_S", *l_s).pt("PARK_L", 10.5, 120, 270)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)

    def lane_down(label):
        # Down the lane facing the right end through the spill (it lands just past L_N), the turn below the HIVE
        # frame's feet (y 51-90) at L_TURN (x 57: its corners to 70.65, inside the centre line), then across to L_S.
        r.at = "L_N"
        cards = [r.wait(label, when=["IntakeFull"], ms=settle_ms),
                 r.go("L_TURN", ctrl=[(57.5, 100), (57.5, 50)], turn_after=0.88, turn_by=1.0)]
        r.at = "L_TURN"
        cards.append(r.go("L_S", heading=90))
        r.at = "L_S"
        return cards

    # TIP 2: L's preloads and the far FLOWER's 4 at the left CELL once TIP 1 raises it.
    r.add(r.action("SpinUp"), r.go("FAR_FLOWER_TURN", ctrl=[(59, 118.34)], turn_after=0.3, turn_by=0.9))
    r.at = "FAR_FLOWER_TURN"
    if fixed:
        # A fixed launcher can't fire seated (it turns the robot, and the V into the FLOWER): the preloads from
        # FAR_FLOWER_TURN as the left CELL rises, the far FLOWER's 4 from L_N until the TIP (as qual-shoots-right-v-fixed).
        r.add(r.wait("TIP 1 (R): the left CELL up", when=["LeftCellUp"], ms=15000),
              fire(r, "Preloads at the left CELL", "Empty", ms=3000), r.go("FAR_FLOWER", heading=90))
        r.at = "FAR_FLOWER"
        r.add(r.wait("The far FLOWER's 4", when=["IntakeFull"], ms=3000), r.go("L_N", turn_after=0.3, turn_by=1.0))
        r.at = "L_N"
        r.add(fire(r, "The far FLOWER's 4 (TIP 2)", "Tip", ms=4000))
    else:
        r.add(*seated_stream(r, stream_ms))
    r.at = "L_N"

    # TIP 3: TIP 2's spill caught on the way down the lane, fired with R's catch and the GARDEN.
    if bail_ms is None:
        r.add(r.wait("TIP 2 started", when=["RightCellUp"], ms=tip2_wait_ms))
    n0 = len(r.cards)
    r.add(*lane_down("TIP 2's spill lands"))
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
    if bail_ms is not None:
        # No TIP 2 by bail_ms (R is rescuing it from R_N): straight to PARK from L_N, out of R's way.
        yes = r.cards[n0:]
        del r.cards[n0:]
        r.add(r.wait("TIP 2 started", when=["RightCellUp"], ms=bail_ms, yes=yes, no_label="No TIP 2: park"))
    # PARK: from L_N still facing the right end, along y 127 toward x 0 (above R at R_N) into the LOADING ZONE's far end.
    r.add(r.go("PARK_L", ctrl=[(44, 127), (24, 127)], heading=270, park=True))
    return r


def variants():
    # The best pairs, turret and fixed launcher (the steps to them are in git, 8-9 Oct 2026). The fixed launcher's TIP 2
    # comes about 3 s later (8.7 s against 11.6 median), so R waits longer before it rescues one.
    return [right(), left(),
            right("sister-right-fixed", rescue_ms=9000), left("sister-left-fixed", fixed=True)]


if __name__ == "__main__":
    for r in variants():
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(r.name)
