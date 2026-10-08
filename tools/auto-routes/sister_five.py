"""Sister five: sister.py's pair with a fifth TIP and no PARK (mentor, 8 Oct 2026, watching sister's 4 TIPs: "at 23.3
seconds the right robot has shot their 4 balls at tip 4. it seems like they could grab the 4 at wall flower and shoot
those while the sister robot waits for the tip and collects 3/4 and then drives through the tunnel and shoots those for
tip 5"). In playoffs only the score counts: 5 TIPs (100) + LEAVE beats 4 TIPs + 2 PARK (96).

TIPs 1-4 are sister.py's. Then, with TIP 4 raising the right CELL:

    R  the wall FLOWER's 4 if it is still there (sister.py takes it for TIP 4 unless TIP 3 came early), else what is
       left of TIP 3's spill at the right end, picked up off the floor; fired at the right CELL from R_F5.
    L  waits at L_N for TIP 4, catches its spill (it lands just short of L_N), drives straight down the lane under the
       HIVE without turning, and fires back over its shoulder (the turret) from L_F5.

Robots stop at 30 s: TIP 5's pieces must be in by then; the TIP itself counts if it completes before TELEOP (§10.5 B).

Results so far (8 Oct 2026, "rigid V" on both, 20 runs each; sister.py's pair: 4 TIPs + 2 PARK in 19 of 20, 94.0 pts
a match on average):

    R order + fills              L          5 TIPs  4   3   <=2  collide  mean pts
    branch, fill + top-up        keeps 1       1   17   1    1     18      84.5
    direct, fill + top-up        all to T3     5    8   2    5     13      79.5
    swap, fill + top-up          all to T3     5    5   5    5     11      76.5
    direct, fill + top-up        keeps 1       6    2   5    7      8      73.5

What decides it:
- Without the GARDEN, TIP 3 comes up one or two short in a quarter to a third of runs (R's TIP 1 catch is often 3,
  and filling it off the floor while waiting for TIP 2 didn't reach 4), and those matches end at 2 TIPs.
- TIP 5 gets R's 4 and L's catch of TIP 4's spill (2-3 in its 1.2 s), and ends 1-3 short; R's top-up off the floor
  at the end adds little.
- R's floor pickups at the right end meet L coming down the lane (collisions at 23-29 s).
- Pieces are not spent evenly: sister.py's TIP 3 gets 9.7 POLLEN-worth on average (1-2 more than it needs, some after
  the rocker has started), TIP 4 8.4. A count of what is in the CELL would let R hold back the extra for TIP 5.

    python3 sister_five.py     writes them into experiments/
"""
import autogen
from autogen import Route
from alone import seat, S_CATCH
from helpers import WALL_FLOWER_AT, FAR_FLOWER_AT, fire
from sister import R_START, L_START, R_S, R_N, L_N, L_TURN, L_S, seated_stream

# TIP 5's firing spots at the right end (the right CELL is clean from y <= 30, x 42-66): R toward the GARDEN side, L in
# the lane, their Vs (8.9 in either side) about 3 in apart.
R_F5, L_F5 = (42, 22, 90), (61, 30, 270)
# Where R looks for pieces on the floor: TIP 1's spill rests about x 45-65, y 18-40 (R_C1, before TIP 2); TIP 3's leftovers
# at the right end after L has caught its share (R_C5, before L arrives in the lane).
R_C1, R_C5 = (50, 24, 90), (48, 26, 90)


def right5(name="sister5b-right", rescue_ms=6500, catch_at=(57.5, 21, 90), collect_ms=2500, mode="branch", fill1_ms=0,
           top5_ms=0):
    """mode: "branch" sister.py's: from the GARDEN back to R_S to see whether TIP 3 still needs its 4.
             "direct" (mentor: "they should just go to the left side and shoot as soon as it can"): the GARDEN's 4
                      straight up the left side to TIP 4; the wall FLOWER's to TIP 5.
             "swap"   the wall FLOWER first, on the way up the left side, to TIP 4; the GARDEN's 4, next to TIP 5's
                      firing spot, on the way back down."""
    r = Route(name, R_START, speed=50)
    r.pt("S_CATCH", *S_CATCH).pt("GARDEN_IN", 9.5, 20.56, 270).pt("GARDEN", 9.5, 10.96, 270)
    r.pt("R_S", *R_S).pt("R_N", *R_N).pt("R_F5", *R_F5).pt("R_W", 30, 40, 90).pt("R_PRE", 57, 20, 90)
    r.pt("PARK", 10.5, 95, 90).pt("R_C5", *R_C5)
    seat(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_CATCH", *catch_at)
    r.add(r.action("SpinUp"), r.go("R_PRE", heading=90))
    r.at = "R_PRE"
    r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000), r.go("S_CATCH", heading=90))
    r.at = "S_CATCH"
    retry = [r.wait("TIP 1 missed: catch", when=["IntakeFull"], ms=1500),
             fire(r, "TIP 1 missed: fire the catch", "Tip", ms=3000)]
    r.add(r.wait("TIP 1", when=["Tip"], ms=4000, no=retry))
    if fill1_ms:
        # R stands idle from its catch until TIP 2 (about 4 s): about 4 of TIP 1's spill lie around it. Topped up
        # off the floor to 4, TIP 3 needs no GARDEN (one short in a third of runs without it).
        r.pt("R_C1", *R_C1)
        r.at = "S_CATCH"
        fill = [r.go("R_C1", heading=90)]
        r.at = "R_C1"
        fill += [r.wait("TIP 1's spill off the floor", when=["IntakeFull"], ms=fill1_ms, alongside="CollectSeen"),
                 r.go("S_CATCH", heading=90)]
        r.at = "S_CATCH"
        r.add(r.wait("Catch TIP 1's spill", when=["IntakeFull"], ms=1500, no=fill, no_label="Not full: off the floor"))
    else:
        r.add(r.wait("Catch TIP 1's spill", when=["IntakeFull"], ms=1500))
    n0 = len(r.cards)
    r.add(fire(r, "TIP 1's catch at the right CELL", "Empty", ms=2000))
    if mode != "swap":
        r.add(r.go("GARDEN_IN", turn_after=0.1, turn_by=0.7))
        r.at = "GARDEN_IN"
        r.add(r.go("GARDEN", heading=270))
        r.at = "GARDEN"
        r.add(r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500))
    if mode == "branch":
        r.add(r.go("R_S", turn_after=0.2, turn_by=0.8))

    def tip4_at_rn(ms=4000):
        r.at = "R_N"
        return [r.wait("The left CELL up", when=["LeftCellUp"], ms=ms),
                fire(r, "R's 4 at the left CELL (TIP 4, with L)", "Empty", ms=2000)]

    def wall_flower_from(here, ctrl=None):
        r.at = here
        cards = [r.go("WALL_FLOWER_TURN", ctrl=ctrl or [], turn_after=0.2, turn_by=0.8)]
        r.at = "WALL_FLOWER_TURN"
        cards.append(r.go("WALL_FLOWER", heading=180))
        r.at = "WALL_FLOWER"
        cards.append(r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=3000))
        return cards

    def tip5_fire():
        r.at = "R_F5"
        cards = [r.wait("TIP 4: the right CELL up", when=["RightCellUp"], ms=8000),
                 fire(r, "R's 4 at the right CELL (TIP 5, with L)", "Empty", ms=3000)]
        if top5_ms:
            # About 5 s left: what lies at the right end (TIP 3's spill, less L's catch), picked up and fired too.
            cards.append(r.go("R_C5", heading=90))
            r.at = "R_C5"
            cards += [r.wait("Top-up off the floor", when=["IntakeFull"], ms=top5_ms, alongside="CollectSeen"),
                      r.go("R_F5", heading=90)]
            r.at = "R_F5"
            cards.append(fire(r, "The top-up at the right CELL (TIP 5)", "Tip", ms=4000))
        return cards

    # Early: TIP 3 came without the GARDEN's 4, so they go to TIP 4 and the wall FLOWER is TIP 5's.
    r.at = "R_S"
    early = [r.go("R_W", heading=90)]
    r.at = "R_W"
    early.append(r.go("R_N", heading=90))
    early += tip4_at_rn()
    early += wall_flower_from("R_N", ctrl=[(30, 80)])
    early.append(r.go("R_F5", turn_after=0.2, turn_by=0.8))
    early += tip5_fire()

    def normal():
        # The GARDEN's 4 to TIP 3, the wall FLOWER's to TIP 4; TIP 5 from TIP 3's spill left at the right end.
        r.at = "R_S"
        cards = [fire(r, "The GARDEN's 4 (TIP 3)", "Tip", ms=2500)]
        cards += wall_flower_from("R_S")
        cards.append(r.go("R_N", ctrl=[(30, 80)], turn_after=0.3, turn_by=0.9))
        cards += tip4_at_rn()
        r.at = "R_N"
        cards.append(r.go("R_C5", ctrl=[(30, 60)], heading=90))
        r.at = "R_C5"
        cards.append(r.wait("TIP 3's spill off the floor", when=["IntakeFull"], ms=collect_ms, alongside="CollectSeen"))
        cards.append(r.go("R_F5", heading=90))
        cards += tip5_fire()
        return cards

    if mode == "direct":
        # Up the left side (x 30, clear of the HIVE frame's foot) to R_N; fire the moment TIP 3 raises the left CELL.
        r.at = "GARDEN"
        tail = [r.go("R_W", turn_after=0.1, turn_by=0.8)]
        r.at = "R_W"
        tail.append(r.go("R_N", heading=90))
        tail += tip4_at_rn(ms=10000)
        tail += wall_flower_from("R_N", ctrl=[(30, 80)])
        tail.append(r.go("R_F5", turn_after=0.2, turn_by=0.8))
        tail += tip5_fire()
        r.add(*tail)
    elif mode == "swap":
        r.at = "S_CATCH"
        tail = [r.go("R_W", heading=90)]
        tail += wall_flower_from("R_W")
        tail.append(r.go("R_N", ctrl=[(30, 80)], turn_after=0.3, turn_by=0.9))
        tail += tip4_at_rn(ms=10000)
        r.at = "R_N"
        tail.append(r.go("GARDEN_IN", ctrl=[(30, 60)], turn_after=0.2, turn_by=0.8))
        r.at = "GARDEN_IN"
        tail.append(r.go("GARDEN", heading=270))
        r.at = "GARDEN"
        tail += [r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500), r.go("R_F5", turn_after=0.2, turn_by=0.8)]
        tail += tip5_fire()
        r.add(*tail)
    if mode == "branch":
        done = r.wait("TIP 3 done?", when=["LeftCellUp"], ms=50, yes=early, no=normal(),
                      yes_label="Yes: the GARDEN's 4 to TIP 4", no_label="No: the GARDEN's 4 to TIP 3")
        which = r.wait("The right CELL up", when=["RightCellUp"], ms=1500, yes=normal(), no=[done])
        r.at = "R_S"
        r.add(which)
    if rescue_ms is not None:
        yes = r.cards[n0:]
        del r.cards[n0:]
        r.at = "S_CATCH"
        no = [r.go("R_W", heading=90)]
        r.at = "R_W"
        no.append(r.go("R_N", heading=90))
        r.at = "R_N"
        no += [fire(r, "No TIP 2: TIP 1's catch at the left CELL (TIP 2)", "Empty", ms=3000),
               r.go("PARK", heading=90, park=True)]
        r.add(r.wait("TIP 2 (L): the right CELL up", when=["RightCellUp"], ms=rescue_ms, yes=yes, no=no))
    return r


def left5(name="sister5b-left", stream_ms=2600, settle_ms=500, catch3_ms=2000, creep=(58, 40, 90), keep1=True,
          bail_ms=4500, catch4_ms=1200, tip4_ms=8000):
    r = Route(name, L_START, speed=50)
    r.pt("L_N", *L_N).pt("L_TURN", *L_TURN).pt("L_S", *L_S).pt("PARK_L", 10.5, 120, 270).pt("L_F5", *L_F5)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    r.add(r.action("SpinUp"), r.go("FAR_FLOWER_TURN", ctrl=[(59, 118.34)], turn_after=0.3, turn_by=0.9))
    r.at = "FAR_FLOWER_TURN"
    r.add(*seated_stream(r, stream_ms))
    r.at = "L_N"
    n0 = len(r.cards)
    # TIP 3: TIP 2's spill caught on the way down the lane (sister.py's lane_down).
    r.add(r.wait("TIP 2's spill lands", when=["IntakeFull"], ms=settle_ms),
          r.go("L_TURN", ctrl=[(57.5, 100), (57.5, 50)], turn_after=0.88, turn_by=1.0))
    r.at = "L_TURN"
    r.add(r.go("L_S", heading=90))
    r.at = "L_S"
    if keep1:
        r.add(*[r.action("LaunchOne") for _ in range(3)])
    else:
        r.add(fire(r, "TIP 2's catch at the right CELL (TIP 3, with R)", "Empty", ms=2000))
    # TIP 4: TIP 3's spill, carried up the lane.
    r.add(r.wait("TIP 3", when=["LeftCellUp"], ms=6000))
    r.pt("L_C", *creep)
    r.add(r.wait("TIP 3's spill lands", when=["IntakeFull"], ms=800), r.go("L_C", heading=90))
    r.at = "L_C"
    r.add(r.wait("Catch TIP 3's spill", when=["IntakeFull"], ms=catch3_ms),
          r.go("L_N", ctrl=[(57.5, 50), (57.5, 104)], turn_after=0.85, turn_by=1.0))
    r.at = "L_N"
    r.add(fire(r, "TIP 3's catch at the left CELL (TIP 4, with R)", "Empty", ms=2000))
    # TIP 5: TIP 4's spill caught at L_N, straight down the lane facing the right end, fired back over the shoulder.
    r.add(r.wait("TIP 4", when=["Tip"], ms=tip4_ms),
          r.wait("Catch TIP 4's spill", when=["IntakeFull"], ms=catch4_ms),
          r.go("L_F5", ctrl=[(57.5, 100), (57.5, 50)], heading=270))
    r.at = "L_F5"
    r.add(fire(r, "TIP 4's catch at the right CELL (TIP 5, with R)", "Empty", ms=3000))
    yes = r.cards[n0:]
    del r.cards[n0:]
    r.at = "L_N"
    r.add(r.wait("TIP 2 started", when=["RightCellUp"], ms=bail_ms, yes=yes,
                 no=[r.go("PARK_L", ctrl=[(44, 127), (24, 127)], heading=270, park=True)], no_label="No TIP 2: park"))
    return r


def variants():
    return [right5(), right5("sister5b-right-direct", mode="direct"), right5("sister5b-right-swap", mode="swap"),
            right5("sister5b-right-direct-fill", mode="direct", fill1_ms=2500, top5_ms=2500),
            right5("sister5b-right-swap-fill", mode="swap", fill1_ms=2500, top5_ms=2500),
            right5("sister5b-right-fill", fill1_ms=2500, top5_ms=2500),
            left5(), left5("sister5b-left-all3", keep1=False)]


if __name__ == "__main__":
    for r in variants():
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(r.name)
