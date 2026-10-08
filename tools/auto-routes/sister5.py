"""Sister5: two of our robots for 5 TIPs (mentor, 9 Oct 2026: "maybe TIP 5 could be done with no park? in playoffs, it
would be worth more points"; "i also am not sure it makes sense for the left robot to come across the field").

Each robot keeps to its own end and feeds on its own end's spills, so nobody crosses after the start. Every TIP after
TIP 2 is 4 pieces held, fired the moment the CELL rises, then 4 picked up off the floor nearby (the webcam's
CollectSeen) and fired:

    TIP 1  R: its preloads at the right CELL.
    TIP 2  L: its preloads and the far FLOWER's 4 (as sister.py).
    TIP 3  R: the GARDEN's 4, held until TIP 2 raises the right CELL, then 4 of TIP 1's spill off the floor.
    TIP 4  L: 4 of TIP 2's spill, held until TIP 3 raises the left CELL, then 4 more of it off the floor.
    TIP 5  R: the wall FLOWER's 4, held until TIP 4, then 4 of TIP 3's spill off the floor. No PARK.
    PARK   L, at the LOADING ZONE's far end, beside it.

5 TIPs + 1 PARK is 111 AUTO points against 96 for 4 + 2 (TIP 20, PARK 5, LEAVE 3), and in playoffs only the score
counts. A TIP counts for AUTO if it completes before TELEOP (§10.5 B); its shots must be away by 30 s.

    python3 sister5.py     writes them into experiments/
"""
import autogen
from autogen import Route
from alone import seat
from helpers import FAR_FLOWER_AT, WALL_FLOWER_AT, fire
from sister import seated_stream, R_START, L_START

R_PRE, R_S = (57, 20, 90), (40, 22, 90)
GARDEN_IN, GARDEN = (9.5, 20.56, 270), (9.5, 10.96, 270)
L_N, PARK_L = (55, 116, 270), (10.5, 120, 270)
# Where each end's spill lands: TIP 1's and TIP 3's at the right end about x 50-67, y 38-41 (8 Oct logs), TIP 2's just
# past L_N toward the HIVE. Each robot faces it from there to pick up.
R_C, L_C = (55, 28, 90), (55, 108, 270)


def collect(r, label, ms, spot):
    """To spot (facing where the spill lands), up to 4 off the floor with the webcam (within 36 in), and back to fire
    from where we were."""
    here = r.at
    cards = [r.go(spot)]
    r.at = spot
    cards += [r.wait(label, when=["IntakeFull"], ms=ms, alongside="CollectSeen"), r.go(here)]
    r.at = here
    return cards


def right5(name="sister5-right", collect_ms=2500, tip2_ms=12000, tip4_ms=12000):
    r = Route(name, R_START, speed=50)
    r.pt("R_PRE", *R_PRE).pt("R_S", *R_S).pt("R_C", *R_C).pt("GARDEN_IN", *GARDEN_IN).pt("GARDEN", *GARDEN)
    seat(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    # TIP 1, then the GARDEN's 4 while L makes TIP 2.
    r.add(r.action("SpinUp"), r.go("R_PRE", heading=90))
    r.at = "R_PRE"
    r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000), r.go("GARDEN_IN", turn_after=0.2, turn_by=0.8))
    r.at = "GARDEN_IN"
    r.add(r.go("GARDEN", heading=270))
    r.at = "GARDEN"
    r.add(r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500), r.go("R_S", turn_after=0.2, turn_by=0.8))
    r.at = "R_S"
    # TIP 3: the GARDEN's 4 the moment TIP 2 raises the right CELL, then TIP 1's spill off the floor.
    r.add(r.wait("TIP 2 (L): the right CELL up", when=["RightCellUp"], ms=tip2_ms),
          fire(r, "The GARDEN's 4 (TIP 3)", "Empty", ms=2000),
          *collect(r, "TIP 1's spill off the floor", collect_ms, "R_C"),
          fire(r, "TIP 1's spill (TIP 3)", "Empty", ms=2000),
          # TIP 5: the wall FLOWER's 4, held while L makes TIP 4.
          r.go("WALL_FLOWER_TURN", turn_after=0.2, turn_by=0.8))
    r.at = "WALL_FLOWER_TURN"
    r.add(r.go("WALL_FLOWER", heading=180))
    r.at = "WALL_FLOWER"
    r.add(r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=3000), r.go("R_S", turn_after=0.3, turn_by=0.9))
    r.at = "R_S"
    r.add(r.wait("TIP 4 (L): the right CELL up", when=["RightCellUp"], ms=tip4_ms),
          fire(r, "The wall FLOWER's 4 (TIP 5)", "Empty", ms=2000),
          *collect(r, "TIP 3's spill off the floor", collect_ms, "R_C"),
          fire(r, "TIP 3's spill (TIP 5)", "Empty", ms=3000))
    return r


def right5b(name="sister5-right-catch", collect_ms=3000):
    """Catches each spill as it falls, standing still clear of G409 (sister.py's S_CATCH at y 21: 3-4 a time), and
    tops up off the floor only while that spill is fresh. Waits for the next TIP itself: the right CELL is already
    up when R comes back, so waiting for it to be up passed at once."""
    r = Route(name, R_START, speed=50)
    r.pt("R_PRE", *R_PRE).pt("R_S", *R_S).pt("R_C", *R_C).pt("S_HOLD", 57.5, 21, 90)
    r.pt("GARDEN_IN", *GARDEN_IN).pt("GARDEN", *GARDEN)
    r.add(r.action("SpinUp"), r.go("R_PRE", heading=90))
    r.at = "R_PRE"
    r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000), r.go("S_HOLD", heading=90))
    r.at = "S_HOLD"
    # TIP 3: TIP 1's catch when TIP 2 raises the right CELL, then the GARDEN's 4.
    r.add(r.wait("TIP 1", when=["Tip"], ms=4000), r.wait("Catch TIP 1's spill", when=["IntakeFull"], ms=1500),
          r.wait("TIP 2 (L)", when=["Tip"], ms=12000), r.wait("The right CELL up", when=["RightCellUp"], ms=1500),
          fire(r, "TIP 1's catch (TIP 3)", "Empty", ms=2000), r.go("GARDEN_IN", turn_after=0.1, turn_by=0.7))
    r.at = "GARDEN_IN"
    r.add(r.go("GARDEN", heading=270))
    r.at = "GARDEN"
    r.add(r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500), r.go("S_HOLD", turn_after=0.2, turn_by=0.8))
    r.at = "S_HOLD"
    # Fired from S_HOLD, then stay to catch TIP 3's spill as it falls; top up from it while it's fresh.
    r.add(fire(r, "The GARDEN's 4 (TIP 3)", "Empty", ms=2000),
          r.wait("TIP 3", when=["Tip"], ms=4000), r.wait("Catch TIP 3's spill", when=["IntakeFull"], ms=1500),
          # TIP 5: TIP 3's catch when TIP 4 (L) raises the right CELL, then more of TIP 3's spill off the floor.
          r.wait("TIP 4 (L)", when=["Tip"], ms=12000), r.wait("The right CELL up", when=["RightCellUp"], ms=1500),
          fire(r, "TIP 3's catch (TIP 5)", "Empty", ms=2000),
          *collect(r, "TIP 3's spill off the floor", collect_ms, "R_C"),
          fire(r, "TIP 3's spill (TIP 5)", "Empty", ms=3000))
    return r


def right_block(name="spill-block", at=(60, 36, 90), wait_ms=6000):
    """An experiment, not an Auto: TIP 1 from R_PRE, then stand at `at` through its spill (the side as a wall along the
    centre line, x 70.75), to see whether that keeps the pieces that cross it on our half (spilltrack.py)."""
    r = Route(name, R_START, speed=50)
    r.pt("R_PRE", *R_PRE).pt("BLOCK", *at)
    r.add(r.action("SpinUp"), r.go("R_PRE", heading=90))
    r.at = "R_PRE"
    r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000), r.action("IntakeOff"), r.go("BLOCK", heading=90))
    r.at = "BLOCK"
    r.add(r.wait("Stand through the spill", when=["Empty"], ms=wait_ms))
    return r


def right_block_late(name="spill-block-late", hold=(57.5, 21, 90), at=(60, 36, 90), land_ms=1300):
    """An experiment: wait clear of the falling spill, then step beside the centre line once it has touched down
    (1.1-1.4 s after the TIP starts, doc/simulator.md), to stop the pieces rolling across it without a G409."""
    r = Route(name, R_START, speed=50)
    r.pt("R_PRE", *R_PRE).pt("HOLD", *hold).pt("BLOCK", *at)
    r.add(r.action("SpinUp"), r.go("R_PRE", heading=90))
    r.at = "R_PRE"
    r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000), r.action("IntakeOff"), r.go("HOLD", heading=90))
    r.at = "HOLD"
    r.add(r.wait("TIP 1", when=["Tip"], ms=4000), r.wait("The spill touches down", when=["Empty"], ms=land_ms),
          r.go("BLOCK", heading=90))
    r.at = "BLOCK"
    r.add(r.wait("Stand", when=["Empty"], ms=5000))
    return r


def left5(name="sister5-left", fixed=True, collect_ms=3000, tip3_ms=15000):
    r = Route(name, L_START, speed=50)
    r.pt("L_N", *L_N).pt("L_C", *L_C).pt("PARK_L", *PARK_L)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    # TIP 2, as sister.py.
    r.add(r.action("SpinUp"), r.go("FAR_FLOWER_TURN", ctrl=[(59, 118.34)], turn_after=0.3, turn_by=0.9))
    r.at = "FAR_FLOWER_TURN"
    if fixed:
        r.add(r.wait("TIP 1 (R): the left CELL up", when=["LeftCellUp"], ms=15000),
              fire(r, "Preloads at the left CELL", "Empty", ms=3000), r.go("FAR_FLOWER", heading=90))
        r.at = "FAR_FLOWER"
        r.add(r.wait("The far FLOWER's 4", when=["IntakeFull"], ms=3000), r.go("L_N", turn_after=0.3, turn_by=1.0))
        r.at = "L_N"
        r.add(fire(r, "The far FLOWER's 4 (TIP 2)", "Tip", ms=4000))
    else:
        r.add(*seated_stream(r, 2600))
        r.at = "L_N"
    # TIP 4: 4 of TIP 2's spill held (it lands around L_N) until TIP 3 raises the left CELL, then 4 more.
    r.add(r.wait("TIP 2's spill lands", when=["IntakeFull"], ms=800),
          *collect(r, "TIP 2's spill off the floor, held", collect_ms, "L_C"),
          r.wait("TIP 3 (R)", when=["Tip"], ms=tip3_ms), r.wait("The left CELL up", when=["LeftCellUp"], ms=1500),
          fire(r, "Held spill (TIP 4)", "Empty", ms=2000),
          *collect(r, "More of TIP 2's spill off the floor", collect_ms, "L_C"),
          fire(r, "TIP 2's spill (TIP 4)", "Empty", ms=2000),
          r.go("PARK_L", ctrl=[(44, 127), (24, 127)], heading=270, park=True))
    return r


def variants():
    return [right5(), right5b(), left5(), left5("sister5-left-turret", fixed=False),
            right_block(), right_block("spill-block-wall", at=(57.5, 21, 90)), right_block_late()]


if __name__ == "__main__":
    for r in variants():
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(r.name)
