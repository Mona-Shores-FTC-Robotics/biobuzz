"""3 TIPs with a partner that cannot shoot (mentor, 8 Oct 2026): "we need to work on the staged autos, or a just-park
auto from our teammate, where we can tip 3. can a flower first turret type get us there?"

The Rigid V with its turret, from the south start in front of the right CELL (the Stages start):

    TIP 1   our 4 preloads at the right CELL, from the start.
    TIP 2   north through TIP 1's spill up the lane, into the far FLOWER's seat (38 in from the left CELL, up after
            TIP 1): fire what the catch brought, take the FLOWER's 4, back out to N_FIRE and fire them standing.
    TIP 3   south through TIP 2's spill down the lane, round into the wall FLOWER's seat (43 in from the right CELL, up
            after TIP 2): fire the catch, take the wall FLOWER's 4, fire them from there.
    PARK    48 in north of the wall FLOWER, at the LOADING ZONE (10.5, 95).

The FLOWER seats put the V's face 4.59 in from the FLOWER's centre (the CAD's seat), the centre 12.15 in out.

    python3 alone.py      writes the variants into experiments/
"""
import autogen
from autogen import Route
from helpers import FAR_FLOWER_AT, WALL_FLOWER_AT, fire

S_START = (59, 8.06, 90)
SEAT_IN, IN_IN, TURN_IN = 12.15, 17.95, 20.45  # the V's centre from the FLOWER: seated, straight back, turn room
# S_FIRE: arrived at facing south, 2.5 in west of the lane: a fixed launcher turns the robot there to face the right
# CELL, and from x 57.5 the V's tips swung over the centre line (18 of 20 runs).
S_CATCH, S_FIRE, N_FIRE = (57.5, 28, 90), (55, 24, 270), (57.5, 119, 270)
PARK = (10.5, 95, 90)


def seat(r, name, at, heading):
    import math
    c, s = math.cos(math.radians(heading)), math.sin(math.radians(heading))
    for suffix, d in (("", SEAT_IN), ("_IN", IN_IN), ("_TURN", TURN_IN)):
        r.pt(name + suffix, round(at[0] - d * c, 2), round(at[1] - d * s, 2), heading)


def alone(name, tip1_catch=1500, tip2_settle=500, tip2_land=0, far_fire_at="n_fire", wall_ms=3000, fire3=(57.5, 24, 90),
          after_tip2="lane", fire_g=None, fire1_ms=2500, tip2_ms=2500, park_ctrl=((30, 40),), tip1_retry=False, speed=50, garden=False, tip3_ms=2500,
          skip_nfire=False, lane_fire=None):
    r = Route(name, S_START, speed=speed)
    r.pt("S_CATCH", *S_CATCH).pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE).pt("PARK", *PARK)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    seat(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)

    # TIP 1: our preloads at the right CELL from the start, then to S_CATCH before the TIP, facing the HIVE, and
    # catch its spill as it rolls south toward the wall (it rolled past a robot that left after the TIP).
    r.add(r.action("SpinUp"), fire(r, "Fire the preloads (TIP 1)", "Empty", ms=fire1_ms), r.go("S_CATCH"))
    r.at = "S_CATCH"
    # tip1_retry: two of the four preloads can miss (shot-hive, 3 of 60): no TIP in 4 s, catch what fell in front of
    # the HIVE and fire it at the right CELL, rather than going north with the right CELL still down.
    retry = [r.wait("TIP 1 missed: catch", when=["IntakeFull"], ms=1500),
             fire(r, "TIP 1 missed: fire the catch", "Tip", ms=3000)] if tip1_retry else []
    r.add(r.wait("TIP 1", when=["Tip"], ms=4000, no=retry),
          r.wait("Catch TIP 1's spill", when=["IntakeFull"], ms=tip1_catch))
    # Up the lane (x 57.5, between the HIVE frame's feet at x 46 and 95), the control points stacked at its top so
    # the robot's back is past the west foot's end (y 90) before it bends west, then into the far FLOWER's seat.
    # The extractor comes down on the way.
    r.add(r.go("FAR_FLOWER_TURN", ctrl=[(57.5, 100), (57.5, 118), (57.5, 118)], turn_by=0.5))
    r.at = "FAR_FLOWER_TURN"
    # TIP 2, the mentor's hold: the catch fired standing at FAR_FLOWER_TURN (30 in from the left CELL, up after TIP 1)
    # before the seat; into the seat for the FLOWER's 4, not firing while it extracts (a LaunchAll there fired each
    # piece as the extractor brought it); back out to N_FIRE and fire them standing.
    r.add(fire(r, "Fire the catch at the left CELL", "Empty", ms=2000), r.go("FAR_FLOWER", heading=90))
    r.at = "FAR_FLOWER"
    r.add(r.wait("The far FLOWER's 4", when=["IntakeFull"], ms=3000))
    if skip_nfire:
        return skip_n_fire(r, fire3, wall_ms, park_ctrl, garden, tip3_ms, tip2_ms, tip2_settle, lane_fire=lane_fire)
    if far_fire_at == "n_fire":
        r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0))
        r.at = "N_FIRE"
    else:  # "turn": fire them where it backs out to, 30 in from the left CELL, a second sooner than N_FIRE
        r.add(r.go("FAR_FLOWER_TURN", heading=90))
        r.at = "FAR_FLOWER_TURN"
    if after_tip2 == "west_garden":
        return west_garden(r, fire3, fire_g)
    r.add(fire(r, "Fire the far FLOWER's 4 (TIP 2)", "Tip", ms=tip2_ms))
    if tip2_settle:
        r.add(r.wait("TIP 2's spill lands", when=["IntakeFull"], ms=tip2_settle))
    # TIP 3: south through TIP 2's spill down the lane, then round into the wall FLOWER's seat.
    if far_fire_at == "n_fire":
        r.add(r.go("S_FIRE", ctrl=[(57.5, 100)], heading=270))
    else:  # onto the lane first, turning to face south on the way
        r.add(r.go("S_FIRE", ctrl=[(57.5, 112), (57.5, 100)], turn_after=0.0, turn_by=0.35))
    r.at = "S_FIRE"
    # TIP 2's catch fired standing at S_FIRE, on the way: from the wall FLOWER's side the shots crossed the HIVE
    # (3 of 8 "shot-hive"). Then the same hold at the wall FLOWER, its 4 fired from FIRE3, south of the HIVE.
    r.pt("FIRE3", *fire3)
    r.add(fire(r, "Fire TIP 2's catch at the right CELL", "Empty", ms=2000), r.go("WALL_FLOWER_TURN", turn_after=0.2, turn_by=0.8))
    r.at = "WALL_FLOWER_TURN"
    r.add(r.go("WALL_FLOWER", heading=180))
    r.at = "WALL_FLOWER"
    r.add(r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=wall_ms), r.go("FIRE3", turn_after=0.3, turn_by=0.9))
    r.at = "FIRE3"
    r.add(fire(r, "Fire the wall FLOWER's 4 (TIP 3)", "Tip", ms=tip3_ms))
    if garden:
        # Eight pieces left the right CELL at 91-95% in some runs: the GARDEN's 4 if it is still up, while there is time.
        r.pt("GARDEN_IN", 9.5, 20.56, 270).pt("GARDEN", 9.5, 10.96, 270)
        more = [r.go("GARDEN_IN", turn_after=0.2, turn_by=0.8)]
        r.at = "GARDEN_IN"
        more.append(r.go("GARDEN", heading=270))
        r.at = "GARDEN"
        more += [r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500), r.go("FIRE3", turn_after=0.3, turn_by=0.9)]
        r.at = "FIRE3"
        more.append(fire(r, "Fire the GARDEN's 4 (TIP 3)", "Tip", ms=2500))
        r.add(r.wait("TIP 3 done?", when=["LeftCellUp"], ms=50, no=more, yes_label="TIP 3", no_label="Not yet: the GARDEN"))
    r.add(r.go("PARK", ctrl=list(park_ctrl), turn_after=0.2, turn_by=0.8, park=True))
    return r


def skip_n_fire(r, fire3, wall_ms, park_ctrl, garden, tip3_ms, tip2_ms, tip2_settle, check_ms=1500, lane_fire=None):
    """Baseline (ii): a left partner's 4 and our catch tip TIP 2 while we take the far FLOWER's 4 (11.4-12.5 s), and
    standing at N_FIRE then fired at nothing for 2.5 s. If the right CELL is up: let the spill fall, back straight down
    the lane facing north (a fixed launcher fires at the right CELL from S_FIRE without turning), the FLOWER's 4 there;
    else N_FIRE as before. Then the wall FLOWER's 4 (TIP 3) and the GARDEN if the right CELL is still up."""
    r.pt("S_FIRE_N", S_FIRE[0], S_FIRE[1], 90).pt("LANE_TOP", 57.5, 108, 90)
    r.at = "FAR_FLOWER"
    # Onto the lane's top first, then straight down it: one curve from the seat entered the HIVE frame's feet (y 51-90)
    # at x 54 and clipped the west foot (18 of 20).
    yes = [r.wait("TIP 2's spill falls", when=["Empty"], ms=600), r.go("LANE_TOP", heading=90)]
    r.at = "LANE_TOP"
    yes.append(r.go("S_FIRE_N", ctrl=[(57.5, 40)], heading=90))
    r.at = "FAR_FLOWER"
    no = [r.go("N_FIRE", turn_after=0.3, turn_by=1.0)]
    r.at = "N_FIRE"
    # Until the right CELL is up, not a new TIP: a TIP that started just after the check below came during the drive
    # here, and "Tip" then waited out its 4 s for one that had already happened.
    no += [fire(r, "Fire the far FLOWER's 4 (TIP 2)", "RightCellUp", ms=tip2_ms),
           r.wait("TIP 2's spill lands", when=["IntakeFull"], ms=tip2_settle),
           r.go("S_FIRE_N", ctrl=[(57.5, 100)], heading=270)]
    r.add(r.wait("TIP 2 already?", when=["RightCellUp"], ms=check_ms, yes=yes, no=no, yes_label="Yes: down the lane",
                 no_label="No: fire from N_FIRE"))
    r.at = "S_FIRE_N"
    r.pt("FIRE3", *fire3)
    if lane_fire is not None:  # from S_FIRE a shot in some runs hit the HIVE (shot-hive): on to a clean spot first
        r.pt("LANE_FIRE", *lane_fire)
        r.add(r.go("LANE_FIRE", heading=lane_fire[2]))
        r.at = "LANE_FIRE"
    r.add(fire(r, "Fire what we hold at the right CELL", "Empty", ms=2000),
          r.go("WALL_FLOWER_TURN", turn_after=0.2, turn_by=0.8))
    r.at = "WALL_FLOWER_TURN"
    r.add(r.go("WALL_FLOWER", heading=180))
    r.at = "WALL_FLOWER"
    r.add(r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=wall_ms), r.go("FIRE3", turn_after=0.3, turn_by=0.9))
    r.at = "FIRE3"
    r.add(fire(r, "Fire the wall FLOWER's 4 (TIP 3)", "Tip", ms=tip3_ms))
    if garden:
        r.pt("GARDEN_IN", 9.5, 20.56, 270).pt("GARDEN", 9.5, 10.96, 270)
        more = [r.go("GARDEN_IN", turn_after=0.2, turn_by=0.8)]
        r.at = "GARDEN_IN"
        more.append(r.go("GARDEN", heading=270))
        r.at = "GARDEN"
        more += [r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500), r.go("FIRE3", turn_after=0.3, turn_by=0.9)]
        r.at = "FIRE3"
        more.append(fire(r, "Fire the GARDEN's 4 (TIP 3)", "Tip", ms=2500))
        r.add(r.wait("TIP 3 done?", when=["LeftCellUp"], ms=50, no=more, yes_label="TIP 3", no_label="Not yet: the GARDEN"))
    r.add(r.go("PARK", ctrl=list(park_ctrl), turn_after=0.2, turn_by=0.8, park=True))
    return r


def shoots_right_v(name, garden=True, tip1_wait_ms=6000, tip2_ms=4000, fire3=(45, 26, 90), ending="lane"):
    """Baseline (iii), a partner that shoots its preloads from the right start (TIP 1 is its 4): ours from the north
    start into the left CELL, fired from FAR_FLOWER_TURN as soon as it rises (a fixed launcher reaches it from there,
    30 in), then the far FLOWER's 4 from N_FIRE (TIP 2, about 4 s sooner than qual-alone's); TIP 3 as qual-alone-p4-lane
    (TIP 2's spill down the lane, the wall FLOWER), the GARDEN if the right CELL is still up and there is time; PARK."""
    r = Route(name, (59, 133.69, 270), speed=50)
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE).pt("PARK", *PARK).pt("FIRE3", *fire3)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    seat(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.add(r.action("SpinUp"), r.go("FAR_FLOWER_TURN", ctrl=[(59, 118.34)], turn_after=0.3, turn_by=0.9))
    r.at = "FAR_FLOWER_TURN"
    r.add(r.wait("TIP 1 (the partner's preloads)", when=["LeftCellUp"], ms=tip1_wait_ms),
          fire(r, "Fire our preloads at the left CELL", "Empty", ms=3000), r.go("FAR_FLOWER", heading=90))
    r.at = "FAR_FLOWER"
    r.add(r.wait("The far FLOWER's 4", when=["IntakeFull"], ms=3000), r.go("N_FIRE", turn_after=0.3, turn_by=1.0))
    r.at = "N_FIRE"
    if ending == "west":  # TIP 3 from two sure sources, the wall FLOWER's 4 and the GARDEN's 4, both fired from FIRE3
        return west_garden(r, fire3, fire3)
    # Until the TIP itself (the dwell is up to 3.4 s): leaving on a 2.5 s timer drove into the lane ahead of the spill.
    r.add(fire(r, "Fire the far FLOWER's 4 (TIP 2)", "Tip", ms=tip2_ms),
          r.wait("TIP 2's spill lands", when=["IntakeFull"], ms=500),
          r.go("S_FIRE", ctrl=[(57.5, 100)], heading=270))
    r.at = "S_FIRE"
    r.add(fire(r, "Fire TIP 2's catch at the right CELL", "Empty", ms=2000),
          r.go("WALL_FLOWER_TURN", turn_after=0.2, turn_by=0.8))
    r.at = "WALL_FLOWER_TURN"
    r.add(r.go("WALL_FLOWER", heading=180))
    r.at = "WALL_FLOWER"
    r.add(r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=3000), r.go("FIRE3", turn_after=0.3, turn_by=0.9))
    r.at = "FIRE3"
    r.add(fire(r, "Fire the wall FLOWER's 4 (TIP 3)", "Tip", ms=1500))
    if garden:
        r.pt("GARDEN_IN", 9.5, 20.56, 270).pt("GARDEN", 9.5, 10.96, 270)
        more = [r.go("GARDEN_IN", turn_after=0.2, turn_by=0.8)]
        r.at = "GARDEN_IN"
        more.append(r.go("GARDEN", heading=270))
        r.at = "GARDEN"
        more += [r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500), r.go("FIRE3", turn_after=0.3, turn_by=0.9)]
        r.at = "FIRE3"
        more.append(fire(r, "Fire the GARDEN's 4 (TIP 3)", "Tip", ms=2500))
        r.add(r.wait("TIP 3 done?", when=["LeftCellUp"], ms=50, no=more, yes_label="TIP 3", no_label="Not yet: the GARDEN"))
    r.add(r.go("PARK", ctrl=[(30, 40)] if fire3[0] > 35 else [(24, 40)], turn_after=0.2, turn_by=0.8, park=True))
    return r


def west_garden(r, fire3, fire_g=None):
    """TIP 3 without TIP 2's spill: the far FLOWER's 4 fired at N_FIRE and gone at once (no wait for the TIP, so its
    random dwell overlaps the drive, not a wait), down the west side (x 30: clear of the HIVE frame's west foot at x
    45-47 and of a partner parked at the zone's far end) into the wall FLOWER's seat for its 4, fired from FIRE3
    (south of the HIVE, the right CELL up by then); then the GARDEN's 4, which are always there, fired from FIRE3 for
    TIP 3; PARK. Eight pieces that do not depend on a catch, and no wait whose length depends on a dwell."""
    r.pt("FIRE3", *fire3).pt("GARDEN_IN", 9.5, 20.56, 270).pt("GARDEN", 9.5, 10.96, 270)
    gname = "FIRE3" if fire_g is None else "FIRE_G"
    if fire_g == "garden_in":
        gname = "GARDEN_IN"
    elif fire_g is not None:
        r.pt("FIRE_G", *fire_g)
    r.add(fire(r, "Fire the far FLOWER's 4 (TIP 2)", "Empty", ms=2000),
          r.go("WALL_FLOWER_TURN", ctrl=[(30, 112), (30, 64)], turn_after=0.15, turn_by=0.7))
    r.at = "WALL_FLOWER_TURN"
    r.add(r.go("WALL_FLOWER", heading=180))
    r.at = "WALL_FLOWER"
    r.add(r.wait("The wall FLOWER's 4", when=["IntakeFull"], ms=3000), r.go("FIRE3", turn_after=0.3, turn_by=0.9))
    r.at = "FIRE3"
    r.add(fire(r, "Fire the wall FLOWER's 4 at the right CELL", "Empty", ms=2000),
          r.go("GARDEN_IN", turn_after=0.2, turn_by=0.8))
    r.at = "GARDEN_IN"
    r.add(r.go("GARDEN", heading=270))
    r.at = "GARDEN"
    r.add(r.wait("The GARDEN's 4", when=["IntakeFull"], ms=1500))
    if gname != "GARDEN_IN":
        r.add(r.go(gname, turn_after=0.3, turn_by=0.9))
    else:
        r.add(r.go(gname, heading=270))
    r.at = gname
    r.add(fire(r, "Fire the GARDEN's 4 (TIP 3)", "Tip", ms=2500),
          # From (45, 26) the (24, 40) curve swung the V into the HIVE frame (20 of 20); (30, 40) is the lane route's.
          # (FIRE_G too: a straight park from (45, 26) clipped the west foot's corner, 60 of 60.)
          r.go("PARK", ctrl=([(30, 40)] if r.points[gname][0] > 35 else [(24, 40)]) if gname in ("FIRE3", "FIRE_G") else [],
               turn_after=0.2, turn_by=0.8, park=True))
    return r


def partner_left_v(name="partner-left-v"):
    """A partner that shoots its preloads, from the standard left start: fires them at the left CELL when it rises
    (after our TIP 1), then gets out of our lane before we come north (about 8 s): forward to y 124, west along it
    (clear of the far FLOWER and of our way into its seat, which we reach later) into the LOADING ZONE's far end
    (10.5, 118), clear of our PARK. partners.preloads_left parks under the far FLOWER and onto our PARK."""
    r = Route(name, (59, 132.25, 270), speed=40)
    r.pt("DOWN", 59, 124, 270).pt("PARK_P", 10.5, 118, 270)
    r.add(r.action("SpinUp"), r.action("IntakeOff"), r.wait("Left CELL up", when=["LeftCellUp"], ms=8000),
          fire(r, "Fire the preloads at the left CELL", "Empty", ms=3000), r.go("DOWN", heading=270))
    r.at = "DOWN"
    r.add(r.go("PARK_P", ctrl=[(30, 124)], heading=270, park=True))
    return r


def partner_park_only(name="partner-park-only"):
    """A partner that only parks, keeping its 4 preloads, from the standard left start: straight south off the wall,
    then west along y 116, clear of the far FLOWER (partners.park_left's lane at y 127.5 drives into it now), into
    the LOADING ZONE's far end (10.5, 118), clear of our V's tips parked at (10.5, 95)."""
    r = Route(name, (59, 132.25, 270), speed=40)
    r.pt("DOWN", 59, 116, 270).pt("PARK_P", 10.5, 118, 270)
    r.add(r.go("DOWN", heading=270))
    r.at = "DOWN"
    r.add(r.go("PARK_P", ctrl=[(30, 116)], heading=270, park=True))
    return r


# TIP 3 fired from S_FIRE: the park path from there clipped the HIVE frame's west foot (8 of 10 runs); from (45, 26)
# it does not. TIP 2's spill: 0 ms before driving into it touched it falling (G409 in 8 of 10); 500-1000 ms is best.
VARIANTS = {f"qual-alone-flower-f45-s{ms}": {"tip2_settle": ms, "fire3": (45, 26, 90)} for ms in (500, 1000)}
VARIANTS["qual-alone-west-garden"] = {"after_tip2": "west_garden", "fire3": (45, 26, 90)}
# All four preloads fired (3000 ms: the 2500 ms card ended a shot early, so one miss lost TIP 1). A longer TIP 1
# catch (2500/3000 ms) lost more to TIP 3's clock than it gained at TIP 2 (12/20, 6/20 against 16/20).
VARIANTS["qual-alone-p4-lane"] = {"tip2_settle": 500, "fire3": (45, 26, 90), "fire1_ms": 3000}
# Baseline (i): a missed TIP 1 recovered (two preloads can hit the HIVE; then the run went late and the guard parked
# the robot sideways out of the wall FLOWER's seat).
VARIANTS["qual-alone-p4-lane-r"] = {"tip2_settle": 500, "fire3": (45, 26, 90), "fire1_ms": 3000, "tip1_retry": True}
VARIANTS["qual-left-partner-v-fixed"] = {"tip2_settle": 500, "fire3": (45, 26, 90), "fire1_ms": 3000, "tip1_retry": True,
                                         "skip_nfire": True, "garden": False, "tip3_ms": 2500, "tip2_ms": 4000,
                                         "lane_fire": (45, 26, 90)}
VARIANTS["qual-alone-p4-west"] = {"after_tip2": "west_garden", "fire3": (25, 28, 90), "fire_g": (25, 28, 90),
                                  "fire1_ms": 3000}

if __name__ == "__main__":
    for n, kw in VARIANTS.items():
        r = alone(n, **kw)
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(n)
    for r in (partner_park_only(), partner_left_v(), shoots_right_v("qual-shoots-right-v-fixed"),
              shoots_right_v("qual-shoots-right-v-fixed-west45", fire3=(45, 26, 90), ending="west")):
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(r.name)
