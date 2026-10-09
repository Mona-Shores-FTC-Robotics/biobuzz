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

    direct, creep catches         keeps 1       1    0   2   17      0      51.5
    direct, creep catches         all to T3     2    7   3    8      0      69.5
    direct, fill; L tops up (5d)  all to T3     6    7   2    5      0      80.5   <- best clean so far
    direct, fill; L tops up       keeps 1       6    2   5    7      0      73.5

(5d: R fills its TIP 1 catch off the floor and fires TIP 5 from R_F5_WIDE, out of the lane; L tops TIP 5 up with
TIP 3's leftovers between L_F5 and the end wall. Creeping into a spill after it lands didn't help R: the pieces have
rolled past by then.)

Then, 40 runs each:

    sister5e (TIP 5 from mid-zone spots, R clears out)            5 TIPs 21, 4 5, 3 4, <=2 10    84.8
    sister5f (R's count decides: 4 held -> 5 TIPs, else sister's)  5 TIPs 15, 4 20, 3 4, <=2 1     93.4
    sister5g (+ R backs out of the GARDEN in one path)              5 TIPs 15, 4 19, 3 5, <=2 1     92.9
    sister5g + sister5h-left (L doesn't turn on the lane)           5 TIPs 15, 4 20, 3 4, <=2 1     93.4  (1 collision)
    sister5h-right (3.5 s top-up)                                   no change
    R_N at y 108 (mentor: R's TIP 4 shots look flat)               no change in the simulator (no bounce-outs there)

    sister5i (+ R parks after TIP 5)                               5 TIPs 15, 4 20, 3 4, <=2 1     95.6
    sister5i-worth (+ R decides on weight, HeldWorth4)              5 TIPs 19, 4 15, 3 4, <=2 2     96.5
    The plan is sister5i-right + sister5h-left, deciding on the piece count (mentor, 9 Oct 2026: "Worth 4 should be
    saved for later, no sensor plans atm"). HeldWorth4 needs a sensor that tells NECTAR from POLLEN (one at the lane
    mouth, or a distance sensor reading the queue's length, would do); it is kept for when there is one.
    (sister5i-worth: R goes for 5 in 25 of 40, makes it in 19, parks after TIP 5 in 20.)

Before parking and weight: R goes for 5 in 18 of 40 and makes it in 15; the rest is sister's plan at 91 (L doesn't park: it can't tell R switched).
The lever now is how often R's TIP 1 catch reaches 4.

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
L_T5 = (60, 20, 270)  # L's top-up: TIP 3's leftovers lie between L_F5 and the end wall
R_F5_WIDE = (38, 20, 90)  # 6 in between the Vs: at R_F5, R's floor pickups met L arriving in the lane
# Clean firing spots (the 8 Oct scan: the right CELL clean from y <= 30 at x 42-66). From L_F5 (61, 30) and R_F5_WIDE
# (38, 20), at the edges, shots hit the HIVE's frame: L lost 1-2 of TIP 5 that way in 5 of the 14 runs that reached it.
# Mid-zone instead, one robot at a time: R fires the moment TIP 4 raises the CELL, then clears out to the GARDEN side.
R_F5_MID, L_F5_MID, L_T5_MID, R_CLEAR = (46, 18, 90), (55, 26, 270), (55, 16, 270), (16, 26, 90)
# Where R looks for pieces on the floor: TIP 1's spill rests about x 45-65, y 18-40 (R_C1, before TIP 2); TIP 3's leftovers
# at the right end after L has caught its share (R_C5, before L arrives in the lane).
R_C1, R_C5 = (50, 24, 90), (48, 26, 90)
# Creeping into a spill once it has landed (L's TIP 3 catch, at (58, 40), fills almost at once): TIP 1's at the right
# end, TIP 4's at the left end (the same spot turned about the field's centre line).
R_C1_CREEP, L_C4 = (58, 40, 90), (58, 101.5, 270)
# L's TIP 4 top-up: from L_N4 toward the left end wall, where TIP 2's leftovers come to rest.
L_T4 = (55, 128, 90)


def right5(name="sister5b-right", rescue_ms=6500, catch_at=(57.5, 21, 90), collect_ms=2500, mode="branch", fill1_ms=0,
           top5_ms=0, creep1=False, f5=None, clear5=False, rn=R_N, back_out=False, park5=False, decide="IntakeFull",
           r5_wait_ms=0):
    """mode: "branch" sister.py's: from the GARDEN back to R_S to see whether TIP 3 still needs its 4.
             "direct" (mentor: "they should just go to the left side and shoot as soon as it can"): the GARDEN's 4
                      straight up the left side to TIP 4; the wall FLOWER's to TIP 5.
             "swap"   the wall FLOWER first, on the way up the left side, to TIP 4; the GARDEN's 4, next to TIP 5's
                      firing spot, on the way back down."""
    r = Route(name, R_START, speed=50)
    r.pt("S_CATCH", *S_CATCH).pt("GARDEN_IN", 9.5, 20.56, 270).pt("GARDEN", 9.5, 10.96, 270)
    r.pt("R_S", *R_S).pt("R_N", *rn).pt("R_F5", *R_F5).pt("R_W", 30, 40, 90).pt("R_PRE", 57, 20, 90)
    r.pt("PARK", 10.5, 95, 90).pt("R_C5", *R_C5)
    if f5 is not None:
        r.pt("R_F5", *f5)
    seat(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_CATCH", *catch_at)
    r.add(r.action("SpinUp"), r.go("R_PRE", heading=90))
    r.at = "R_PRE"
    r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000), r.go("S_CATCH", heading=90))
    r.at = "S_CATCH"
    retry = [r.wait("TIP 1 missed: catch", when=["IntakeFull"], ms=1500),
             fire(r, "TIP 1 missed: fire the catch", "Tip", ms=3000)]
    r.add(r.wait("TIP 1", when=["Tip"], ms=4000, no=retry))
    if creep1:
        # As L takes TIP 3's spill: once it has landed, forward into it intake first, then back to fire from S_CATCH.
        r.pt("R_C1", *R_C1_CREEP)
        r.add(r.wait("TIP 1's spill lands", when=["IntakeFull"], ms=800), r.go("R_C1", heading=90))
        r.at = "R_C1"
        r.add(r.wait("Catch TIP 1's spill", when=["IntakeFull"], ms=1500), r.go("S_CATCH", heading=90))
        r.at = "S_CATCH"
    elif fill1_ms:
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
    if mode != "auto":
        r.add(fire(r, "TIP 1's catch at the right CELL", "Empty", ms=2000))
    if mode not in ("swap", "auto"):
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
        if park5 and r5_wait_ms:
            # No TIP 4 within r5_wait_ms of getting here: TIP 5 is off; park while there is time.
            r.at = "R_F5"
            late = [r.go("PARK", ctrl=[(20, 30), (16, 70)], heading=90, park=True)]
            r.at = "R_F5"
            go5 = [fire(r, "R's 4 at the right CELL (TIP 5, with L)", "Empty", ms=3000),
                   r.go("PARK", ctrl=[(20, 30), (16, 70)], heading=90, park=True)]
            r.at = "PARK"
            return [r.wait("TIP 4: the right CELL up", when=["RightCellUp"], ms=r5_wait_ms, yes=go5, no=late,
                           no_label="No TIP 4: park")]
        cards = [r.wait("TIP 4: the right CELL up", when=["RightCellUp"], ms=8000),
                 fire(r, "R's 4 at the right CELL (TIP 5, with L)", "Empty", ms=3000)]
        if park5:
            # Mentor, 8 Oct 2026: "R robot literally has plenty of time to park even after TIP 5". Out of L's way
            # first (west, then up the left side clear of the wall FLOWER) and on to PARK, about 2.5 s.
            cards.append(r.go("PARK", ctrl=[(20, 30), (16, 70)], heading=90, park=True))
            r.at = "PARK"
        elif clear5:
            # Out of L's way: L comes down the lane to the middle of the clean firing area 3 s or more after this.
            r.pt("R_CLEAR", *R_CLEAR)
            cards.append(r.go("R_CLEAR", heading=90))
            r.at = "R_CLEAR"
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
    if mode == "auto":
        # R's own count decides (40 runs of sister5e: with 4 held after the top-up TIP 3 came in 18 of 18; with fewer, in
        # 12 of 22). Full: the direct route to 5 TIPs. Not full: sister.py's 4 TIPs and PARK, the GARDEN's 4 kept
        # for TIP 3 if it needs them.
        def to_garden():
            r.at = "S_CATCH"
            cards = [fire(r, "TIP 1's catch at the right CELL", "Empty", ms=2000),
                     r.go("GARDEN_IN", turn_after=0.1, turn_by=0.7)]
            r.at = "GARDEN_IN"
            cards.append(r.go("GARDEN", heading=270))
            r.at = "GARDEN"
            cards.append(r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500))
            return cards

        if back_out:
            # Mentor, 8 Oct 2026: "it should use a bezier curve to back out and go straight back along the outside of
            # the hive, i dont know why it turns around ... it also should make it not have to stop in the middle".
            # One path, still facing 270 (the end wall), backing up the left side: x <= 30 keeps the side of the V
            # 7 in off the HIVE frame's foot (x 46), x >= 16 at y 47 keeps it off the wall FLOWER (2.7, 47.4).
            r.pt("R_N", rn[0], rn[1], 270)
            five = to_garden() + [r.go("R_N", ctrl=[(18, 45), (30, 72)], heading=270)]
        else:
            five = to_garden() + [r.go("R_W", turn_after=0.1, turn_by=0.8)]
            r.at = "R_W"
            five.append(r.go("R_N", heading=90))
        five += tip4_at_rn(ms=10000)
        five += wall_flower_from("R_N", ctrl=[(30, 80)])
        five.append(r.go("R_F5", turn_after=0.2, turn_by=0.8))
        five += tip5_fire()

        def park_from_rn():
            r.at = "R_N"
            return [r.go("PARK", heading=90, park=True)]

        four = to_garden() + [r.go("R_S", turn_after=0.2, turn_by=0.8)]
        r.at = "R_S"
        early4 = [r.go("R_W", heading=90)]
        r.at = "R_W"
        early4.append(r.go("R_N", heading=90))
        early4 += tip4_at_rn() + park_from_rn()

        def normal4():
            r.at = "R_S"
            cards = [fire(r, "The GARDEN's 4 (TIP 3)", "Tip", ms=2500)]
            cards += wall_flower_from("R_S")
            cards.append(r.go("R_N", ctrl=[(30, 80)], turn_after=0.3, turn_by=0.9))
            cards += tip4_at_rn() + park_from_rn()
            return cards

        done4 = r.wait("TIP 3 done?", when=["LeftCellUp"], ms=50, yes=early4, no=normal4(),
                       yes_label="Yes: the GARDEN's 4 to TIP 4", no_label="No: the GARDEN's 4 to TIP 3")
        r.at = "R_S"
        four.append(r.wait("The right CELL up", when=["RightCellUp"], ms=1500, yes=normal4(), no=[done4]))
        r.at = "S_CATCH"
        r.add(r.wait("Holding 4? Then 5 TIPs", when=[decide], ms=50, yes=five, no=four,
                     yes_label="4 held: go for 5 TIPs", no_label="Fewer: 4 TIPs and PARK"))
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
          bail_ms=4500, catch4_ms=1200, tip4_ms=8000, creep4=False, top5_ms=0, f5=L_F5, t5=None, noturn4=False,
          top4_ms=0, top4_wait_ms=2500):
    r = Route(name, L_START, speed=50)
    r.pt("L_N", *L_N).pt("L_TURN", *L_TURN).pt("L_S", *L_S).pt("PARK_L", 10.5, 120, 270).pt("L_F5", *f5)
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
    if noturn4:
        # Up the lane still facing it (90), no turn at the end: the turret fires back over the shoulder.
        r.pt("L_N4", L_N[0], L_N[1] - 2, 90)
        r.add(r.wait("Catch TIP 3's spill", when=["IntakeFull"], ms=catch3_ms),
              r.go("L_N4", ctrl=[(57.5, 50), (57.5, 104)], heading=90))
        r.at = "L_N4"
    else:
        r.add(r.wait("Catch TIP 3's spill", when=["IntakeFull"], ms=catch3_ms),
              r.go("L_N", ctrl=[(57.5, 50), (57.5, 104)], turn_after=0.85, turn_by=1.0))
        r.at = "L_N"
    r.add(fire(r, "TIP 3's catch at the left CELL (TIP 4, with R)", "Empty", ms=2000))
    m0 = len(r.cards)
    if noturn4:
        # Turned to face TIP 4's spill (it rolls toward the left end wall) while the CELL dwells and the rocker swings.
        r.add(r.go("L_N", turn_by=1.0))
    r.at = "L_N"
    # TIP 5: TIP 4's spill caught at L_N, straight down the lane facing the right end, fired back over the shoulder.
    r.add(r.wait("TIP 4", when=["Tip"], ms=tip4_ms))
    if creep4:
        r.pt("L_C4", *L_C4)
        r.add(r.wait("TIP 4's spill lands", when=["IntakeFull"], ms=800), r.go("L_C4", heading=270))
        r.at = "L_C4"
        r.add(r.wait("Catch TIP 4's spill", when=["IntakeFull"], ms=catch4_ms),
              r.go("L_F5", ctrl=[(57.5, 50)], heading=270))
    else:
        r.add(r.wait("Catch TIP 4's spill", when=["IntakeFull"], ms=catch4_ms),
              r.go("L_F5", ctrl=[(57.5, 100), (57.5, 50)], heading=270))
    r.at = "L_F5"
    r.add(fire(r, "TIP 4's catch at the right CELL (TIP 5, with R)", "Empty", ms=3000))
    if top5_ms:
        # What lies between L and the end wall (TIP 3's spill, less L's catch): a straight creep toward the wall,
        # intake first, and fired too. R stays out of the lane at R_F5_WIDE.
        r.pt("L_T5", *(t5 or L_T5))
        r.add(r.go("L_T5", heading=270))
        r.at = "L_T5"
        r.add(r.wait("Top-up off the floor", when=["IntakeFull"], ms=top5_ms),
              fire(r, "The top-up at the right CELL (TIP 5)", "Tip", ms=4000))
    if noturn4 and top4_ms:
        # TIP 4 one short (6 of 60 on the simulator chat's build: L's catch of TIP 3's spill was 2-3): no TIP 4
        # within top4_wait_ms of L's shots, L creeps toward the left end wall through TIP 2's leftovers (it faces
        # them at L_N4), fires what it picks up, and parks; TIP 5 is off. A CELL still dwelling just gets more.
        five = r.cards[m0:]
        del r.cards[m0:]
        del five[1]  # the "TIP 4" wait: the TIP has already come on this branch
        r.pt("L_T4", *L_T4)
        r.at = "L_N4"
        short = [r.go("L_T4", heading=90)]
        r.at = "L_T4"
        short += [r.wait("TIP 4 short: off the floor", when=["IntakeFull"], ms=top4_ms),
                  fire(r, "The top-up at the left CELL (TIP 4)", "Tip", ms=2500),
                  r.go("PARK_L", ctrl=[(30, 128)], heading=270, park=True)]
        r.add(r.wait("TIP 4", when=["Tip"], ms=top4_wait_ms, yes=five, no=short,
                     no_label="No TIP 4: top it up and park"))
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
            right5("sister5c-right-direct", mode="direct", creep1=True, f5=R_F5_WIDE),
            right5("sister5c-right", creep1=True, f5=R_F5_WIDE),
            right5("sister5d-right-direct", mode="direct", fill1_ms=2500, f5=R_F5_WIDE),
            left5("sister5d-left-all3", keep1=False, top5_ms=1500),
            left5("sister5d-left", top5_ms=1500),
            right5("sister5e-right-direct", mode="direct", fill1_ms=2500, f5=R_F5_MID, clear5=True),
            # Mentor, watching seed 17: R's TIP 4 shots from R_N look too flat; 3-5 in further on for a steeper launch.
            right5("sister5f-right", mode="auto", fill1_ms=2500, f5=R_F5_MID, clear5=True),
            right5("sister5g-right", mode="auto", fill1_ms=2500, f5=R_F5_MID, clear5=True, back_out=True),
            right5("sister5h-right", mode="auto", fill1_ms=3500, f5=R_F5_MID, clear5=True, back_out=True),
            right5("sister5i-right", mode="auto", fill1_ms=2500, f5=R_F5_MID, back_out=True, park5=True),
            right5("sister5j-right", mode="auto", fill1_ms=2500, f5=R_F5_MID, back_out=True, park5=True,
                   r5_wait_ms=4000),
            left5("sister5j-left", keep1=False, top5_ms=1500, f5=L_F5_MID, t5=L_T5_MID, noturn4=True, top4_ms=1500),
            right5("sister5i-right-worth", mode="auto", fill1_ms=2500, f5=R_F5_MID, back_out=True, park5=True,
                   decide="HeldWorth4"),
            left5("sister5h-left", keep1=False, top5_ms=1500, f5=L_F5_MID, t5=L_T5_MID, noturn4=True),
            right5("sister5e-right-direct-rn108", mode="direct", fill1_ms=2500, f5=R_F5_MID, clear5=True, rn=(30, 108, 90)),
            left5("sister5e-left-all3", keep1=False, top5_ms=1500, f5=L_F5_MID, t5=L_T5_MID),
            left5("sister5c-left", creep4=True, catch4_ms=1000), left5("sister5c-left-all3", keep1=False, creep4=True, catch4_ms=1000),
            left5(), left5("sister5b-left-all3", keep1=False)]


if __name__ == "__main__":
    for r in variants():
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(r.name)
