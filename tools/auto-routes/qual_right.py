"""The qualifier baselines, worked on apart from qual.py (which another session edits): Qual-PartnerShootsRight
(the variants tried to make TIP 3 come in every run without fouling G409) and, for the build team's option 3
(the baseline robot since 5 Oct 2026), Qual-PartnerStages. The baselines: qual-right-o3 and qual-stages-o3
on option 3; qual-right-v3 on the full-width robot.

    DESIGN="builders' option 3 (5 Oct CAD)" python3 qual_right.py [runs] [variant ...]

exports each variant (into auto-builder/experiments, except WINNERS: TeamCode/autos) and simulates it with its
partner (partners.preloads_right, or partners.stage_exit for STAGES) on normal and slow tiles, on DESIGN
(default qual.D, the full-width robot) (AUTO_BUILDER_DIR as for autogen.py).
"""
import os
import sys
import autogen
from qual import *


def go_park(r, park=True, ctrl=((28, 24), (24, 70)), turn_by=0.65):
    """Cards from where r is to PARK: one path up the west side, or, when the route sets r.park_via (a list
    of point names), through those first (round a parked partner)."""
    via = getattr(r, "park_via", None)
    if not via:
        return [r.go("PARK", ctrl=list(ctrl), heading=90, turn_by=turn_by, park=park)]
    return [r.go(p, heading=90) for p in via] + [r.go("PARK", heading=90, park=park)]


def tail(r, spill_at="S_CATCH", garden="two", leftovers=False, settle=True, tag="", fire_y=None, extra=0, lane_x=None, sweep_y=12, third=False, garden_ms=2500, stand=0, seen=0, catch3=False, tip_ms=0, park=True):
    """From N_FIRE (facing the HIVE) once TIP 2 has started: south through its spill and the tunnel,
    fire it straight on from `spill_at`; the GARDEN's 4, fired from S_FIRE; then, if TIP 3 hasn't come,
    what lies near our end (webcam) fired straight on; PARK."""
    if fire_y is not None:
        r.pt("S_FIRE", S_FIRE[0], fire_y, 90)
    out = []
    if stand:  # stand still where we fired (the catch spot), intake running, while the spill lands
        out.append(r.wait(f"Catch TIP 2's spill{tag}", when=["IntakeFull"], ms=stand))
    if seen:  # then the webcam pickup of what lies near, and back to N_BACK
        r.at = "N_CATCH"
        out += catch(r, f"Pick up TIP 2's spill{tag}", "N", ms=seen)
        settle = False
    if settle is not False and settle != 0:  # True: until the right CELL is up; a number: at most that many ms
        out.append(r.wait(f"TIP 2 settles{tag}", when=["RightCellUp"], ms=2500 if settle is True else settle))
    if extra:
        out.append(r.wait(f"It lands{tag}", when=["IntakeFull"], ms=extra))
    if lane_x is not None:  # down the tunnel on x = lane_x, nearer the centre line, back to x 57.5 to fire
        r.pt("N_LANE", lane_x, N_FIRE[1] - 2, 270)
        out.append(r.go("N_LANE", heading=270))
    out += [tunnel(r, spill_at), fire(r, f"Fire TIP 2's spill{tag}", "Empty", ms=2000)]
    r.at = spill_at
    if garden == "two":
        out += [r.go("GARDEN_IN", turn_by=0.6), r.go("GARDEN", heading=270)]
    elif garden == "one":  # one path: round the corner into the GARDEN, turned by the time it is lined up
        out += [r.go("GARDEN", ctrl=[(8.5, 30)], turn_by=0.7)]
    else:  # "sweep": west along the south, intake first, through what TIP 1's spill left there, then the GARDEN
        sy = sweep_y
        r.pt("SWEEP_E", 57.5, sy, 180).pt("SWEEP_W", 22, sy, 180)
        out += [r.go("SWEEP_E", turn_by=1.0), r.go("SWEEP_W", heading=180),
                r.go("GARDEN", ctrl=[(8.5, sy + 4)], turn_by=0.8)]
    out += [r.wait(f"The GARDEN{tag}", when=["IntakeFull"], ms=1500),
            r.go("S_FIRE", turn_after=0.3, turn_by=1.0), fire(r, f"Fire the GARDEN{tag}", "Empty", ms=garden_ms)]
    r.at = "S_FIRE"
    if third:
        return out + third_load(r, tag, catch3=catch3, tip_ms=tip_ms)
    if catch3:  # no third load and no PARK: stand at the catch spot for TIP 3's spill, for TELEOP
        return out + [r.go("S_CATCH", heading=90), r.wait(f"Catch TIP 3's spill{tag}", when=["IntakeFull"], ms=8000)]
    if not park:  # stay where we fired (a partner is parked on our PARK spot)
        return out
    park = go_park(r)
    if not leftovers:
        return out + park
    # Not TIP 3 yet: what lies in front of the right CELL (missed shots, TIP 2's spill), with the
    # webcam, then back to S_FIRE and fired straight on; PARK only once that fire is over.
    more = [*catch(r, f"Leftovers{tag}", "S", ms=leftovers), r.go("S_FIRE", heading=90),
            fire(r, f"Fire the leftovers{tag}", "LeftCellUp", ms=2000)]
    r.at = "S_FIRE"
    more += go_park(r, park=False)
    r.at = "S_FIRE"
    out.append(r.wait(f"TIP 3?{tag}", when=["Tip"], ms=700, yes=park, no=more,
                      yes_label="Yes: PARK", no_label="No: leftovers"))
    return out


def third_load(r, tag="", wait_full=1100, catch3=False, tip_ms=0):
    """From S_FIRE once the GARDEN's shots are away. A TIP under way or done (the right CELL no longer
    up): PARK. If not: back to the GARDEN (what the sweep left in it) and look again there; still no
    TIP, so carry what it holds back to S_FIRE and fire it. A TIP that completes in the 8 s after AUTO
    still counts (Competition Manual §10.5), so shots away just before 30 s can still make it. No park
    path after that fire (the endgame guard would cut it short), only a path there, parked if it
    arrives by 30 s."""
    r.at = "S_FIRE"
    park_here = go_park(r)
    if catch3:  # instead of PARK: stand at the catch spot for TIP 3's spill, for TELEOP
        park_here = [r.go("S_CATCH", heading=90), r.wait(f"Catch TIP 3's spill{tag}", when=["IntakeFull"], ms=6000)]
    r.at = "S_FIRE"
    go = r.go("GARDEN", ctrl=[(8.5, 30)], turn_by=0.7)
    r.at = "GARDEN"
    done = go_park(r, ctrl=[(30, 22), (26, 70)], turn_by=0.5)
    if catch3:
        done = [r.go("S_CATCH", turn_after=0.3, turn_by=1.0), r.wait(f"Catch TIP 3's spill (B){tag}", when=["IntakeFull"], ms=6000)]
    r.at = "GARDEN"
    more = [r.wait(f"The GARDEN again{tag}", when=["IntakeFull"], ms=wait_full), r.go("S_FIRE", turn_after=0.3, turn_by=1.0),
            r.wait(f"Fire the GARDEN again{tag}", when=["LeftCellUp"], ms=2500, alongside="LaunchAll")]
    r.at = "S_FIRE"
    more += go_park(r, park=False)
    there = r.wait(f"Still no TIP 3?{tag}", when=["RightCellUp"], ms=20, yes=more, no=done,
                   yes_label="No TIP: fire the GARDEN", no_label="TIP 3: PARK")
    check = r.wait(f"No TIP 3 yet?{tag}", when=["RightCellUp"], ms=20, yes=[go, there], no=park_here,
                   yes_label="No TIP: the GARDEN", no_label="TIP 3: PARK")
    if not tip_ms:
        return [check]
    # The TIP starts a moment after the last shot lands, so the right CELL is still up just after the
    # fire: wait up to tip_ms for the TIP itself (mentor, 5 Oct: after TIP 3, PARK, don't go for more).
    r.at = "S_FIRE"
    park_now = go_park(r)
    return [r.wait(f"TIP 3 coming?{tag}", when=["Tip"], ms=tip_ms, yes=park_now, no=[check],
                   yes_label="TIP 3: PARK", no_label="Not yet: look again")]


# How far each robot's front face is from its centre: points where the front meets something (the
# start wall behind, a FLOWER, the GARDEN) move by the difference from the 18 in robot the route was
# drawn for, and PARK by as much, so a corner still reaches the LOADING ZONE. The firing spots stay: the prototype scores straight on from y 17-29 and 113-125 (ShotMapTest).
FRONT_IN = {"baseline": 9.0, "proto": 7.5, "option3": 7.25}  # RobotDesign.buildersPrototype 15 in, buildersOption3 14.5 in


class Sized(Route):
    """A Route the Visualizer draws at the robot's size (2 x FRONT_IN, square) instead of 18 in."""
    size = 18.0

    def doc(self):
        d = super().doc()
        d["settings"]["rWidth"] = d["settings"]["rHeight"] = self.size
        return d


def right(name, robot="baseline", n_fire=None, **kw):
    """As qual.shoots_right, with tail(**kw) after TIP 2, for `robot` (FRONT_IN)."""
    d = 9.0 - FRONT_IN[robot]
    r = Sized(name, (N_START[0], N_START[1] + d, N_START[2]), speed=50)
    ends(r)
    r.size = 2 * FRONT_IN[robot]
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *(n_fire or N_FIRE))
    if d:
        for k in ("FAR_FLOWER", "FAR_FLOWER_IN", "FAR_FLOWER_TURN"):
            x, y, h = r.points[k]
            r.pt(k, x, y + d, h)  # the FLOWER is north of us, facing 90
        for k in ("GARDEN", "GARDEN_IN"):
            x, y, h = r.points[k]
            r.pt(k, x, y - d, h)  # the GARDEN is at the south wall, facing 270
        x, y, h = r.points["PARK"]
        r.pt("PARK", x, y + d, h)  # a corner must reach into the LOADING ZONE (y 94.3-117.9)
    r.add(r.action("SpinUp"), r.go("N_FIRE", heading=270),
          r.wait("TIP 1 (the partner)", when=["LeftCellUp"], ms=9000),
          fire(r, "Fire the preloads at the left CELL", "Empty", ms=2500))
    r.at = "N_FIRE"
    r.add(*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300))
    r.at = "FAR_FLOWER"
    r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500))
    r.at = "N_FIRE"
    r.add(*tail(r, **kw))
    return r


VARIANTS = {  # name: tail options. 20 runs each, normal / slow tiles: TIP 3 in how many, AUTO points
    # qual-partner-shoots-right (qual.py): 16 / 18, 72 / 74.
    # TIP 2's spill fired from S_FIRE (y 24) instead of S_CATCH (y 28): 18 / 20, 74 / 76.
    "qual-right-sfire": {"spill_at": "S_FIRE"},
    # ... and the GARDEN approached by a sweep west along y 10: 19 / 20, 75 / 76.
    "qual-right-sweep10": {"spill_at": "S_FIRE", "garden": "sweep", "sweep_y": 10},
    # ... and a third load from the GARDEN while TIP 3 hasn't started: 20 / 20, 75.8 / 76.
    "qual-right-v2": {"spill_at": "S_FIRE", "garden": "sweep", "sweep_y": 10, "third": True, "garden_ms": 1800},
    # ... and, for G409, 500 ms more after TIP 2 settles before driving into its spill, so the spill
    # is on the tiles first (v2 touched 4-5 falling pieces a TIP). The baseline.
    "qual-right-v3": {"spill_at": "S_FIRE", "garden": "sweep", "sweep_y": 10, "third": True, "garden_ms": 1800,
                      "extra": 500},
}
V2 = VARIANTS["qual-right-v2"]
PROTO = {"qual-right-v3-proto": {**VARIANTS["qual-right-v3"], "robot": "proto"},
         "qual-right-v3-option3": {**VARIANTS["qual-right-v3"], "robot": "option3"}}
# G409, 5 Oct (from the side-walls session's SideWallSpillTest: a plain robot facing the HIVE on its
# axis, x 58.0, is clear of the spill with its front face 35 in or less from that wall): N_FIRE, where we
# fire at the left CELL and wait for TIP 2, moved from front face 36.5 in (y 114) to 35 in (y 115.5),
# and the extra wait before driving into TIP 2's spill swept.
G409 = {f"qual-right-v3-n35-{ms}": {**VARIANTS["qual-right-v3"], "n_fire": (58.0, 115.5, 270), "extra": ms}
        for ms in (0, 150, 300, 500)}
TRIALS = {  # catching our TIPs' spills standing still (5 Oct); 20 runs, normal / slow tiles, TIP 3 and points
    # At TIP 2 we already fire from the catch spot (y 114). Waiting there, intake running, before the
    # tunnel: 1 s 17 / 16 (71.5 / 70.5), 1.5 s 17 / 17, 2.5 s 14 / 17; it catches 2.3-2.9 of the 8, no
    # more than driving through (3.0), and the wait costs TIP 3 and PARK.
    **{f"qual-right-stand{ms}": {**V2, "stand": ms} for ms in (1000, 1500, 2500)},
    # The webcam pickup there instead (1.5 / 2.5 s): 11-15 / 20.
    **{f"qual-right-seen{ms}": {**V2, "seen": ms} for ms in (1500, 2500)},
    # TIP 3's spill instead of PARK, for TELEOP: standing at S_CATCH it catches 1.1 by 30 s (holds 1.4
    # at TELEOP, v2 1.8 / 2.4 parked); 70 / 71 points. With the third load as well it isn't there.
    "qual-right-catch3": {**V2, "catch3": True},
    "qual-right-stay3": {**V2, "third": False, "catch3": True},
}
# Option 3 as the baseline robot (mentor, 5 Oct 2026: the build team's 14.5 in low chassis with a 14 in
# intake is closer to what they are building than the full-width robot): v3's route moved for its smaller
# body, then tuned on it. 20 runs, normal / slow tiles: TIP 3 in how many, AUTO points; G409 runs.
O3V3 = {**VARIANTS["qual-right-v3"], "robot": "option3"}
O3 = {
    # v3 moved for the body: 12 / 9, 64.5 / 60.8, no G409; parks 6 / 3 (after TIP 3 it went back to the
    # GARDEN: the right CELL is still up just after the last shot). Nothing below beats its TIP 3.
    "qual-right-o3-v3": O3V3,
    # 300 ms instead of 500 before driving into TIP 2's spill: 11 / 10; none (extra 0): 12 / 10 but
    # G409 in 14 / 1 runs; 150 ms: 12 / 9 with one G409 run.
    "qual-right-o3-x300": {**O3V3, "extra": 300},
    "qual-right-o3-x0": {**O3V3, "extra": 0},
    "qual-right-o3-x150": {**O3V3, "extra": 150},
    # The sweep through TIP 1's spill is what makes TIP 3: without it ("two", "one") 6 / 4. Its lane:
    # y 8.5 10 / 9, y 12 8 / 9, y 14-18 7 / 7-9; y 10 (v3's) is best.
    "qual-right-o3-two": {**O3V3, "garden": "two"},
    "qual-right-o3-one": {**O3V3, "garden": "one"},
    "qual-right-o3-x0-one": {**O3V3, "extra": 0, "garden": "one"},
    "qual-right-o3-sw85": {**O3V3, "sweep_y": 8.5},
    **{f"qual-right-o3-sw{y}": {**O3V3, "sweep_y": y} for y in (12, 14, 16, 18)},
    # Firing the spill from y 22, a longer GARDEN fire (2.3 s): 12 / 10, 12 / 9.
    "qual-right-o3-sfire": {**O3V3, "fire_y": 22},
    "qual-right-o3-g1": {**O3V3, "garden_ms": 2300},
    # Mentor, 5 Oct: after TIP 3 it went back to the GARDEN (the right CELL is still up for a moment
    # after the last shot). Wait for the TIP first, up to tip_ms.
    # 800-2200 ms all alike: TIP 3 11 / 8, parks 11 / 9, 64.5-64.8 / 60.8-61.3.
    **{f"qual-right-o3-tip{ms}": {**O3V3, "tip_ms": ms} for ms in (800, 1200, 1600, 2200)},
    # The baseline (mentor, 5 Oct: after TIP 3, PARK): wait up to 800 ms for TIP 3 before another load.
    "qual-right-o3": {**O3V3, "tip_ms": 800},
}

def fit(robot):
    """A Route class for `robot`: as right() does by hand, the start (backed against a wall) moves back
    by the difference in front face from the 18 in robot, and the points its front meets (a FLOWER,
    the GARDEN) and PARK move forward by as much."""
    import math
    d = 9.0 - FRONT_IN[robot]

    def move(p, sign):
        x, y, h = p
        return (round(x + sign * d * math.cos(math.radians(h)), 2), round(y + sign * d * math.sin(math.radians(h)), 2), h)

    class Fit(Sized):
        size = 2 * FRONT_IN[robot]

        def __init__(self, name, start, **kw):
            super().__init__(name, move(start, -1), **kw)

        def pt(self, name, x, y, h):
            if name.startswith(("FAR_FLOWER", "WALL_FLOWER", "GARDEN", "PARK")):
                x, y, h = move((x, y, h), 1)
            return super().pt(name, x, y, h)
    return Fit


def fitted(robot, build, name):
    """A qual.py route, `build(name)`, for `robot` without editing qual.py (see fit)."""
    import qual
    qual.Route = fit(robot)
    try:
        return build(name)
    finally:
        qual.Route = Route


def stages_v3(name, robot="option3", land=500, **kw):
    """qual.stages up to TIP 2, with `land` ms more after TIP 1 settles before driving north into its
    spill (G409, as v3 does for TIP 2), then tail(**kw) after TIP 2 instead of qual.south_tail."""
    r = fit(robot)(name, S_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE)
    r.pt("ROW_E", 50, 108, 180).pt("ROW_W", 29, 108, 180)
    lands = [r.wait("It lands", when=["IntakeFull"], ms=land)] if land else []
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000), r.go("S_CATCH"),
          r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500), *lands,
          tunnel(r, "N_TURN"), r.go("N_LOW", heading=270), fire(r, "Fire the catch", "Empty", ms=2200),
          r.go("ROW_E", turn_by=0.8), r.go("ROW_W", heading=180),
          r.wait("The row", when=["IntakeFull"], ms=600),
          r.go("N_LOW", turn_after=0.2, turn_by=0.9), fire(r, "Fire the row", "Tip", ms=2500))
    r.at = "N_LOW"
    flower_more = [*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)]
    r.at = "FAR_FLOWER"
    flower_more += [r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500)]
    r.at = "N_FIRE"
    flower_more += tail(r, tag=" (B)", **kw)
    r.at = "N_LOW"
    tipped = tail(r, **kw)
    r.add(r.wait("TIP 2?", when=["Tip"], ms=600, yes=tipped, no=flower_more, yes_label="Yes", no_label="No: the far FLOWER"))
    return r


# The staging partner (mentor, 5 Oct 2026): it can't shoot or set pieces down; its 4 POLLEN start on the
# tiles touching it (G304), and its only move is to drive forward and park. Two ways to stand it:
#   A  back against the wall behind the left CELL at x 19, its POLLEN in a row along its left side (x 29.6,
#      y 128.6-137; AutoStudyTest.stagedFor "PartnerStage19Side"), driving straight forward to park at y 100
#      (wall_partner): parked lower than partners.leave_park, so we fit between it and its row;
#   B  angled, its back-right corner on that wall, aimed straight at the far end of the LOADING ZONE, its
#      POLLEN along its left side (angled_partner, PartnerAngledParkAuto; AutoStudyTest.ANGLED_PARTNER).
# B's back-right corner on the wall (y 141.25), aimed at PARK_B; parked, no corner touches a wall (LEAVE)
# and one is in the LOADING ZONE; its corners clear the far FLOWER at the start, and parked reach x 26.7 and y 96.3 (our PARK is below).
ANGLED = (32.0, 128.53, 227.3)  # = AutoStudyTest.ANGLED_PARTNER
PARK_B = (14, 109)


def wall_partner(name="partner-stage19-side-park"):
    r = Route(name, (19, 132.25, 270), speed=40)
    r.pt("PARK_P", 19, 100, 270)  # a corner (x 10, y 109) in the LOADING ZONE; its top edge y 109
    r.add(r.go("PARK_P", heading=270, park=True))
    return r


def angled_partner(name="partner-angled-park"):
    r = Route(name, ANGLED, speed=40)
    r.pt("PARK_P", *PARK_B, ANGLED[2])
    r.add(r.go("PARK_P", heading=ANGLED[2], park=True))
    return r


def left_side_row(pose):
    """Where AutoStudyTest.alongLeftSide puts the 4 POLLEN: the row's middle and the robot's forward unit vector."""
    import math
    h = math.radians(pose[2])
    f, l = (math.cos(h), math.sin(h)), (-math.sin(h), math.cos(h))
    gap = 9 + 1.4 + 0.2
    return (pose[0] + gap * l[0], pose[1] + gap * l[1]), f


# North from our start, west of the HIVE frame's foot bar (x 45) and east of the parked partner: A's edge
# x 28, B's corners x 26.7.
LANE_X = {"A": 37, "B": 36}


def staged_row(r, partner, row_ms, west):
    """Cards: from where we are to the staged row and up to it side-on, all 4 against the intake at once
    (end-on, the body shoves the rest ahead: 2-3 of 4), leaving the robot at ROW_N, full."""
    import math
    if partner == "A":
        # The row runs along y (x 29.6, y 128.6-137). From the west, facing east, where the partner stood:
        # up the lane, turning to face east at its top (x 37, y 118: the corners, 10.25 in out, clear the
        # parked partner and the row), west at y 118 (between the partner's top edge, y 109, and the row,
        # y 127.2), north alongside, then east until the front touches the row.
        r.pt("LANE_N", LANE_X["A"], 118, 90).pt("LANE_R", LANE_X["A"] - 0.1, 118, 0).pt("TURN_A", 20.9, 118, 0)
        r.pt("ROW_S", 19.5, 132.8, 0).pt("ROW_N", 20.9, 132.8, 0)
        legs = lambda: [r.go("LANE_N", heading=90), r.go("LANE_R", turn_by=1.0), r.go("TURN_A", heading=0),
                        r.go("ROW_S", heading=0)]
        end_h = 0
    else:
        # The row runs along the angled partner's left side. From the field side, facing where the partner
        # stood: next to N_LOW, where we fire.
        (mx, my), f = left_side_row(ANGLED)
        h = math.radians(ANGLED[2])
        l = (-math.sin(h), math.cos(h))
        end_h = round((ANGLED[2] + 270) % 360, 1)  # facing -l
        r.pt("ROW_S", round(mx + 13 * l[0], 2), round(my + 13 * l[1], 2), end_h)
        r.pt("ROW_N", round(mx + 8.9 * l[0], 2), round(my + 8.9 * l[1], 2), end_h)
        r.pt("LANE_N", LANE_X["B"], 100, 90)
        legs = lambda: [r.go("LANE_N", heading=90), r.go("ROW_S", turn_by=0.9)]
    out = []
    if west:  # from the start: across to the lane, up it (straight legs: a curve cuts the corners)
        r.pt("LANE_S", LANE_X[partner], 22, 90)
        out += [r.go("LANE_S", heading=90), *legs()]
    elif partner == "A":  # from N_LOW, facing the left CELL: west at y 118 as above
        out += [r.go("TURN_A", turn_after=0.8, turn_by=1.0), r.go("ROW_S", heading=0)]
    else:  # from N_LOW, facing the left CELL
        out.append(r.go("ROW_S", turn_by=0.8))
    out += [r.go("ROW_N", heading=end_h), r.wait("The staged row", when=["IntakeFull"], ms=row_ms)]
    r.at = "ROW_N"
    return out


def stages_staged(name, partner="A", plan="chase", land=500, row_ms=1600, robot="option3", **kw):
    """Qual-PartnerStages with a realistic staging partner (A or B). plan "chase": TIP 1, its spill caught
    driving north through the tunnel and fired, then the staged row (as qual.stages). plan "west": TIP 1,
    then north along the west lane (clear of TIP 1's spill) to the staged row, fired, then the far FLOWER,
    fired (TIP 2 from 8 sure pieces). Then tail(**kw) after TIP 2; if TIP 2 hasn't come, the far FLOWER
    ("chase") or TIP 1's leftovers are not reachable in time, so tail anyway."""
    r = fit(robot)(name, S_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE)
    # A parks at (19, 100), on our PARK spot. Parking round it instead (up a lane east of it to the LOADING
    # ZONE's free top corner, 13, 116.5) took too long to finish by 30 s, and the endgame guard's cut-short
    # park then drove into the HIVE frame: with A, no PARK (tail(park=False)).
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000))
    if plan == "chase":
        lands = [r.wait("It lands", when=["IntakeFull"], ms=land)] if land else []
        r.add(r.go("S_CATCH"), r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500), *lands,
              tunnel(r, "N_TURN"), r.go("N_LOW", heading=270), fire(r, "Fire the catch", "Empty", ms=2200))
        r.at = "N_LOW"
        r.add(*staged_row(r, partner, row_ms, west=False))
        r.add(r.go("N_LOW", turn_after=0.2, turn_by=0.9), fire(r, "Fire the row", "Tip", ms=2500))
        r.at = "N_LOW"
        more = [*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)]
        r.at = "FAR_FLOWER"
        more += [r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500)]
        r.at = "N_FIRE"
        more += tail(r, tag=" (B)", **kw)
        r.at = "N_LOW"
        tipped = tail(r, **kw)
        r.add(r.wait("TIP 2?", when=["Tip"], ms=600, yes=tipped, no=more, yes_label="Yes", no_label="No: the far FLOWER"))
        return r
    # "west"
    r.add(*staged_row(r, partner, row_ms, west=True))
    r.add(r.go("N_LOW", turn_after=0.2, turn_by=0.9), fire(r, "Fire the row", "Empty", ms=2500))
    r.at = "N_LOW"
    r.add(*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300))
    r.at = "FAR_FLOWER"
    r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500))
    r.at = "N_FIRE"
    r.add(*tail(r, **kw))
    return r


# Qual-PartnerStages for Option 3, with partners.stage_exit. 20 runs, normal / slow tiles: TIP 2, TIP 3,
# PARK (ours), AUTO points; G409 runs. TIP 2 comes at about 21 s, too late for a TIP 3 on Option 3.
V3 = VARIANTS["qual-right-v3"]
STAGES = {
    # qual.stages moved for the body: TIP 3 2 / 1, never parks, 53.0 / 49.0; G409 in 18 / 10 runs.
    "qual-stages-o3-plain": lambda name: fitted("option3", __import__("qual").stages, name),
    # 500 ms for TIP 1's spill to land, then v3's tail: no G409, but no TIP 3 and no PARK, 48.9 / 51.0;
    # no wait 51.0 / 49.0 with G409 in 14 / 10 runs; 300 ms 48.0 / 51.0.
    "qual-stages-o3-v3": lambda name: stages_v3(name, **V3),
    "qual-stages-o3-v3-l0": lambda name: stages_v3(name, land=0, **V3),
    "qual-stages-o3-v3-l300": lambda name: stages_v3(name, land=300, **V3),
    # No third load, so it parks: with the sweep 53.3 / 56.0 (parks 17 / 20); the leftovers instead 49.0 / 51.0.
    "qual-stages-o3-park": lambda name: stages_v3(name, **{**V3, "third": False}),
    "qual-stages-o3-park-left": lambda name: stages_v3(name, **{**V3, "third": False, "leftovers": 1500}),
    # Straight into the GARDEN and PARK: TIP 2 18 / 20, parks 20 / 20, 54.0 / 56.0, no G409. The baseline.
    "qual-stages-o3": lambda name: stages_v3(name, **{**V3, "third": False, "garden": "two"}),
    # A realistic staging partner (A, B), our first half "chase" or "west", the tail with a third load (no PARK)
    # or straight into the GARDEN and PARK.
    **{f"qual-stages-{p.lower()}-{plan}-{t}": (lambda name, p=p, plan=plan, t=t: stages_staged(
        name, partner=p, plan=plan, **({**V3} if t == "third" else {**V3, "third": False, "garden": "two"})))
       for p in "AB" for plan in ("chase", "west") for t in ("third", "park")},
    # The baselines, named for the partner: B (angled) "chase" and PARK; A (against the wall) "west" with no PARK.
    "qual-stages-angled": lambda name: stages_staged(name, partner="B", plan="chase", **{**V3, "third": False, "garden": "two"}),
    # The one G409 run (normal tiles) isn't TIP 1's spill: 700 / 900 ms before driving into it, still one.
    **{f"qual-stages-angled-l{ms}": (lambda name, ms=ms: stages_staged(name, partner="B", plan="chase", land=ms,
        **{**V3, "third": False, "garden": "two"})) for ms in (700, 900)},
    "qual-stages-wall": lambda name: stages_staged(name, partner="A", plan="west",
                                                   **{**V3, "third": False, "garden": "two", "park": False}),
    **{f"qual-stages-a-{plan}-stay": (lambda name, plan=plan: stages_staged(
        name, partner="A", plan=plan, **{**V3, "third": False, "garden": "two", "park": False})) for plan in ("chase", "west")},
}
def PARTNER_OF(w):
    kind = w.split("-")[2]
    return {"a": "PartnerStage19SideParkAuto", "wall": "PartnerStage19SideParkAuto", "b": "PartnerAngledParkAuto",
            "angled": "PartnerAngledParkAuto"}.get(kind, "PartnerStageExitAuto")


WINNERS = ("qual-right-v3", "qual-right-o3", "qual-stages-angled", "qual-stages-wall")  # exported into TeamCode/autos


def cls(name):
    return "".join(w.capitalize() for w in name.split("-")) + "Auto"


if __name__ == "__main__":
    runs = int(sys.argv[1]) if len(sys.argv) > 1 else 20
    which = sys.argv[2:] or list(VARIANTS)
    stages = [w for w in which if w in STAGES]
    which = [w for w in which if w not in STAGES]
    angled_partner().write()
    wall_partner().write()
    for w in stages:
        r = STAGES[w](w)
        r.folder = autogen.AUTOS_DIR if w in WINNERS else autogen.EXPERIMENTS
        r.write()
    for f in ("1", "3") if stages else ():
        study(";".join(f"{cls(w)},{PARTNER_OF(w)}@50" for w in stages), runs=runs, designs=os.environ.get("DESIGN", D),
              extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                         "BIOBUZZ_AUTO_FRICTION": f})
    for w in which:
        r = right(w, **{**VARIANTS, **TRIALS, **PROTO, **G409, **O3}[w])
        r.folder = autogen.AUTOS_DIR if w in WINNERS else autogen.EXPERIMENTS
        r.write()
    for f in ("1", "3") if which else ():
        study(";".join(f"{cls(w)},PartnerPreloadsRightAuto@50" for w in which), runs=runs, designs=os.environ.get("DESIGN", D),
              extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                         "BIOBUZZ_AUTO_FRICTION": f})
