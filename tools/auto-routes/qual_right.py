"""The qualifier baselines, worked on apart from qual.py (which another session edits): Qual-PartnerShootsRight
(the variants tried to make TIP 3 come in every run without fouling G409) and, for the Flat Intake (the baseline
robot since 5 Oct 2026, the build team's option 3; "o3" in file names), Qual-PartnerStages. The baselines:
qual-right-o3, qual-stages-angled and qual-stages-wall on the Flat Intake; qual-right-v3 on the full-width robot.

    DESIGN="flat intake" python3 qual_right.py [runs] [variant ...]

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


def tail(r, spill_at="S_CATCH", garden="two", leftovers=False, settle=True, tag="", fire_y=None, extra=0, lane_x=None, sweep_y=12, third=False, garden_ms=2500, stand=0, seen=0, catch3=False, tip_ms=0, park=True, wall_flower=None, west=None, back_y=None, flower_in=None):
    """From N_FIRE (facing the HIVE) once TIP 2 has started: south through its spill and the tunnel,
    fire it straight on from `spill_at`; the GARDEN's 4, fired from S_FIRE; then, if TIP 3 hasn't come,
    what lies near our end (webcam) fired straight on; PARK."""
    if fire_y is not None:
        r.pt("S_FIRE", S_FIRE[0], fire_y, 90)
    out = []
    if back_y:  # back off from where we fired as TIP 2 starts, clear of a fast TIP's spill, while it lands
        r.pt("N_WAIT", r.points["N_FIRE"][0], back_y, 270)
        out.append(r.go("N_WAIT", heading=270))
        r.at = "N_WAIT"
    if west:  # leave as TIP 2 starts, down the west side clear of where its spill falls (x 49-67, SpillLandingTest),
        # straight to the wall FLOWER: its 4 (they sit still), then the GARDEN's 4, fired straight on, for TIP 3
        r.pt("WEST_VIA", west, 96, 225)
        out += [r.go("WEST_VIA", turn_by=0.8), r.go("WALL_FLOWER_TURN", ctrl=[(west - 6, 62)], heading=180),
                r.go("WALL_FLOWER", heading=180)]
        r.at = "WALL_FLOWER"
        if SEAT_FIRE:  # its 4 fired from the seat as they come, then south first and round into the GARDEN (bending
            # west at once, via (8.5, 30), the body clipped the FLOWER's bracket on the way out: every run, 7 Oct 2026)
            out += seat_fire_cards(r, f"Extract and fire the wall FLOWER{tag}", "Tip", ms=3000)
            out += [r.go("GARDEN", ctrl=[(r.points["WALL_FLOWER"][0], 28)], turn_by=0.7)]
        else:
            out += [r.wait(f"The wall FLOWER{tag}", when=["IntakeFull"], ms=2300),
                    r.go("S_FIRE", turn_after=0.3, turn_by=1.0), fire(r, f"Fire the wall FLOWER{tag}", "Empty", ms=garden_ms)]
            r.at = "S_FIRE"
            out += [r.go("GARDEN_IN", turn_by=0.6), r.go("GARDEN", heading=270)]
        out += [
                r.wait(f"The GARDEN{tag}", when=["IntakeFull"], ms=1500), r.go("S_FIRE", turn_after=0.3, turn_by=1.0),
                r.wait(f"Fire the GARDEN (TIP 3){tag}", when=["LeftCellUp"], ms=2500, alongside="LaunchAll")]
        r.at = "S_FIRE"
        return out + go_park(r, park=False)
    if stand:  # stand still where we fired (the catch spot), intake running, while the spill lands
        out.append(r.wait(f"Catch TIP 2's spill{tag}", when=["IntakeFull"], ms=stand))
    if seen:  # then the webcam pickup of what lies near, and back to N_BACK
        r.at = "N_CATCH"
        out += catch(r, f"Pick up TIP 2's spill{tag}", "N", ms=seen, land_ms=extra if settle is False else 0)
        settle = False
        extra = 0
    if settle is not False and settle != 0:  # True: until the right CELL is up; a number: at most that many ms
        out.append(r.wait(f"TIP 2 settles{tag}", when=["RightCellUp"], ms=2500 if settle is True else settle))
    if extra:
        out.append(r.wait(f"It lands{tag}", when=["IntakeFull"], ms=extra))
    if lane_x is not None:  # down the tunnel on x = lane_x, nearer the centre line, back to x 57.5 to fire
        r.pt("N_LANE", lane_x, N_FIRE[1] - 2, 270)
        out.append(r.go("N_LANE", heading=270))
    if wall_flower == "direct":  # out of the tunnel straight to the wall FLOWER, topping up what the spill gave
        r.pt("TUNNEL_OUT", 57.5, 44, 270)
        out += [tunnel(r, "TUNNEL_OUT"), r.go("WALL_FLOWER_TURN", turn_after=0.2, turn_by=0.8), r.go("WALL_FLOWER", heading=180),
                r.wait(f"The wall FLOWER{tag}", when=["IntakeFull"], ms=2300)]
        r.at = "WALL_FLOWER"
        out += [r.go("S_FIRE", turn_after=0.3, turn_by=1.0), fire(r, f"Fire the spill and the wall FLOWER{tag}", "Empty", ms=garden_ms)]
        r.at = "S_FIRE"
        out += [r.go("GARDEN_IN", turn_by=0.6), r.go("GARDEN", heading=270),
                r.wait(f"The GARDEN{tag}", when=["IntakeFull"], ms=1500), r.go("S_FIRE", turn_after=0.3, turn_by=1.0),
                r.wait(f"Fire the GARDEN (TIP 3){tag}", when=["LeftCellUp"], ms=2500, alongside="LaunchAll")]
        r.at = "S_FIRE"
        return out + go_park(r, park=False)
    out += [tunnel(r, spill_at), fire(r, f"Fire TIP 2's spill{tag}", "Empty", ms=2000)]
    r.at = spill_at
    park_guard = wall_flower == "first-park"  # the 6 Oct 12:55 baseline's ending: PARK guaranteed, the GARDEN fire cut for it
    if park_guard:
        wall_flower = "first"
    if wall_flower in ("first", "garden-first"):
        # TIP 3 from pieces that sit still. "first": the wall FLOWER's 4, fired, then the GARDEN's 4, fired; PARK
        # not guaranteed (the GARDEN's fire comes at ~28 s: a park path made the endgame guard cut it, and TIP 3
        # (20) is worth more than PARK (5)). "garden-first" (mentor, 6 Oct): keep what the tunnel run caught,
        # fill up at the GARDEN (nearer), fire 4, then the wall FLOWER's 4, fired; no sensor needed.
        if wall_flower == "garden-first":
            out += [r.go("GARDEN_IN", turn_by=0.6), r.go("GARDEN", heading=270),
                    r.wait(f"Fill up at the GARDEN{tag}", when=["IntakeFull"], ms=1500), r.go("S_FIRE", turn_after=0.3, turn_by=1.0),
                    fire(r, f"Fire the catch and the GARDEN{tag}", "Empty", ms=garden_ms)]
            r.at = "S_FIRE"
        last = wall_flower == "garden-first"
        if SEAT_FIRE:  # the wall FLOWER's 4 fired from the seat as they come; then straight round into the GARDEN
            out += wall_flower_in(r, tag, flower_in)[:-1]
            r.at = "WALL_FLOWER"
            # Not "Empty": with nothing held on arrival it is true at once. A TIP ends it (TIP 3 under way); else
            # 3 s covers the 4 (one per 0.5 s out of the FLOWER, 0.5 s through the transfer, fired as they arrive).
            out += seat_fire_cards(r, f"Extract and fire the wall FLOWER{tag}{' (TIP 3)' if last else ''}", "Tip", ms=3000)
            if last:
                return out + go_park(r, park=False)
            garden = "one"
        else:
            out += wall_flower_in(r, tag, flower_in)
            r.at = "WALL_FLOWER"
            out += [r.go("S_FIRE", turn_after=0.3, turn_by=1.0),
                    r.wait(f"Fire the wall FLOWER{tag}{' (TIP 3)' if last else ''}", when=["LeftCellUp" if last else "Empty"],
                           ms=2500 if last else garden_ms, alongside="LaunchAll")]
            r.at = "S_FIRE"
            if last:
                return out + go_park(r, park=False)
    if garden == "none":  # no GARDEN load: PARK straight after TIP 2's spill is fired (the wall partner, 7 Oct 2026:
        # with the GARDEN the park came too late, PARK in 26 of 60; TIP 3 from it came in 17)
        return out + go_park(r)
    if garden == "two":
        out += [r.go("GARDEN_IN", turn_by=0.6), r.go("GARDEN", heading=270)]
    elif garden == "one":  # one path: round the corner into the GARDEN, turned by the time it is lined up
        out += [r.go("GARDEN", ctrl=[(8.5, 30)], turn_by=0.7)]
    else:  # "sweep": west along the south, intake first, through what TIP 1's spill left there, then the GARDEN
        sy = sweep_y
        r.pt("SWEEP_E", 57.5, sy, 180).pt("SWEEP_W", 22, sy, 180)
        # Into the GARDEN by a loop out to y about 15.5: the V's tips reach 13.65 in from the centre, so the turn from
        # 180 to 270 happens up there (between 15% and 60% of the path), then the robot slides south square into the
        # GARDEN. Turning on the sweep line swung the tips through the south wall (7 Oct 2026). sweep_y is 12 on the
        # V (baselines_v).
        out += [r.go("SWEEP_E", turn_by=1.0), r.go("SWEEP_W", heading=180),
                r.go("GARDEN", ctrl=[(12, sy + 8)], turn_after=0.15, turn_by=0.6)]
    out += [r.wait(f"The GARDEN{tag}", when=["IntakeFull"], ms=1500),
            r.go("S_FIRE", turn_after=0.3, turn_by=1.0),
            fire(r, f"Fire the GARDEN{tag}", "Empty", ms=garden_ms) if wall_flower != "first" or park_guard
            else r.wait(f"Fire the GARDEN (TIP 3){tag}", when=["LeftCellUp"], ms=2500, alongside="LaunchAll")]
    r.at = "S_FIRE"
    if park_guard:
        return out + go_park(r)
    if wall_flower == "after":  # TIP 3 not started: the wall FLOWER's 4 instead of another GARDEN pass
        return out + wall_flower_load(r, tag, tip_ms=tip_ms)
    if wall_flower == "first":  # both static loads fired: toward PARK (no park path: the guard would cut the fire)
        return out + go_park(r, park=False)
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


def wall_flower_in(r, tag, how):
    """Cards from S_FIRE to against the wall FLOWER and its 4 POLLEN: the helper's two legs (turn on the way to
    _TURN, then straight in), or "one": a single path that turns in its first part and arrives square, with no
    stop at _TURN (mentor, 6 Oct: the stop-and-slide into the FLOWER looks slow)."""
    if how != "one":
        return [*flower(r, "WALL_FLOWER", f"The wall FLOWER{tag}", ms=2300)]
    t, here = r.points["WALL_FLOWER_TURN"], r.points[r.at]
    return [r.go("WALL_FLOWER", ctrl=[(t[0], here[1]), (t[0], t[1])], turn_from=here[2], turn_after=0.15, turn_by=0.6),
            r.wait(f"The wall FLOWER{tag}", when=["IntakeFull"], ms=2300)]


def wall_flower_load(r, tag="", tip_ms=800):
    """From S_FIRE once the GARDEN's shots are away: TIP 3 under way, PARK; if not, the wall FLOWER's 4
    (pieces in a FLOWER sit still, unlike a spill), fired straight on, then toward PARK (no park path
    after a fire that may still be going: the endgame guard would cut it short)."""
    r.at = "S_FIRE"
    park_now = go_park(r)
    r.at = "S_FIRE"
    more = [*flower(r, "WALL_FLOWER", f"The wall FLOWER{tag}", ms=2300)]
    r.at = "WALL_FLOWER"
    more += [r.go("S_FIRE", turn_after=0.3, turn_by=1.0),
             r.wait(f"Fire the wall FLOWER (TIP 3){tag}", when=["LeftCellUp"], ms=2500, alongside="LaunchAll")]
    r.at = "S_FIRE"
    more += go_park(r, park=False)
    return [r.wait(f"TIP 3 coming?{tag}", when=["Tip"], ms=tip_ms, yes=park_now, no=more,
                   yes_label="TIP 3: PARK", no_label="Not yet: the wall FLOWER")]


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
FRONT_IN = {"baseline": 9.0, "proto": 7.5, "option3": 7.25}  # RobotDesign.buildersPrototype 15 in; "option3": flatIntake, 14.5 in
# How far from a FLOWER's centre the front face stops to take its POLLEN: 2.2 in with the intake mouth against the
# ~2 in tube (helpers.FLOWER_PICKUP_IN for the 18 in robot: 9 + 2.2); 4.59 in with the CAD's FLOWER extractor seated on
# it (doc/robot-cad.md "Seated on a FLOWER"; baselines_v.py sets it for the Rigid V). The FLOWER points move by the
# difference from the 18 in robot's 11.2 in; the start, the GARDEN and PARK only by the front face's.
FLOWER_FACE_IN = {"baseline": 2.2, "proto": 2.2, "option3": 2.2}
# How far a robot's guides reach ahead of its face (the V's flap tips: 2.8 in on the drawn V, baselines_v sets it): the
# GARDEN point keeps them WALL_CLEAR_IN off the south wall (7 Oct 2026: the tips poked 0.6 in through it).
FLAP_AHEAD_IN = {"baseline": 0.0, "proto": 0.0, "option3": 0.0}
WALL_CLEAR_IN = 0.6


# The GARDEN's x: the robot's half width plus what its guides reach aside, off the west wall by WALL_CLEAR_IN (8.5 for
# the drawn robots; 9.5 on the V, whose tips reach 8.89 in aside: at 8.5 they sat 0.4 in through the wall, and the
# endgame guard's park from there turned them further in, 7 Oct 2026).
GARDEN_X_IN = {"baseline": 8.5, "proto": 8.5, "option3": 8.5}


def garden_y(robot):
    """The nearest the robot's centre may be to the south wall at the GARDEN: its face plus its guides plus clearance."""
    return FRONT_IN[robot] + FLAP_AHEAD_IN[robot] + WALL_CLEAR_IN


# Fire from the extractor's seat (mentor, 6 Oct 2026: "the robot shoots while extracting"): at a FLOWER, instead of
# waiting for 4 and driving to the firing spot, stream shots while the extractor feeds (StreamOn; the simulator fires
# each piece once it has come through the transfer, RobotDesign.transferFeedS). Tried on ShootsRight first
# (seat_fire.py); set by that script, never by hand. "catch": after TIP 2 from the far FLOWER's seat, to N_FIRE at once
# to catch its spill there as the baseline does; "west": instead down the west side to the wall FLOWER (tail's `west`),
# fired from its seat, then the GARDEN (the spill is left alone).
SEAT_FIRE = False


def seat_fire_cards(r, label, until, ms=3500):
    """Stream shots from where the robot stands (seated on a FLOWER) until `until` or `ms`."""
    return [r.action("StreamOn"), r.wait(label, when=[until], ms=ms), r.action("StreamOff")]


def flower_shift(robot):
    """How much further forward (toward the FLOWER) `robot` stops than the 18 in robot the routes were drawn for."""
    return 9.0 + 2.2 - FRONT_IN[robot] - FLOWER_FACE_IN[robot]


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
    df = flower_shift(robot)
    r = Sized(name, (N_START[0], N_START[1] + d, N_START[2]), speed=50)
    ends(r)
    r.size = 2 * FRONT_IN[robot]
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *(n_fire or N_FIRE))
    if df:
        for k in ("FAR_FLOWER", "FAR_FLOWER_IN", "FAR_FLOWER_TURN"):
            x, y, h = r.points[k]
            r.pt(k, x, y + df, h)  # the FLOWER is north of us, facing 90
        for k in ("GARDEN", "GARDEN_IN"):
            x, y, h = r.points[k]
            if k == "GARDEN":
                x, y = max(x, GARDEN_X_IN[robot]), max(y - d, garden_y(robot))
            else:
                y -= d
            r.pt(k, x, y, h)  # the GARDEN is at the south wall, facing 270
        x, y, h = r.points["PARK"]
        r.pt("PARK", x, y + d, h)  # a corner must reach into the LOADING ZONE (y 94.3-117.9)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    if df:
        for k in ("WALL_FLOWER", "WALL_FLOWER_IN", "WALL_FLOWER_TURN"):
            x, y, h = r.points[k]
            r.pt(k, x - df, y, h)  # the wall FLOWER is west of us, facing 180
    r.add(r.action("SpinUp"), r.go("N_FIRE", heading=270),
          r.wait("TIP 1 (the partner)", when=["LeftCellUp"], ms=9000),
          fire(r, "Fire the preloads at the left CELL", "Empty", ms=2500))
    r.at = "N_FIRE"
    if SEAT_FIRE:  # the far FLOWER's 4 fired from the seat as they come; TIP 2's spill lands while still seated
        # (the seat is at the edge of where it falls, x 49-67; driving through it as it fell touched it in 21 of 60
        # runs); then to N_FIRE for the tail, which does not wait again.
        r.add(*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)[:-1])
        r.at = "FAR_FLOWER"
        r.add(*seat_fire_cards(r, "Extract and fire the far FLOWER (TIP 2)", "Tip"))
        kw = dict(kw)
        if SEAT_FIRE == "west":  # straight from the seat down the west side: the tail starts here
            kw.update(west=kw.get("west") or 35, settle=False, extra=0)
            r.add(*tail(r, **kw))
            return r
        r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0))
    else:
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
# The Flat Intake (was "option 3") as the baseline robot (mentor, 5 Oct 2026: the build team's 14.5 in low chassis with a 14 in
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
    # The baseline until 6 Oct 2026 12:00 UTC (mentor, 5 Oct: after TIP 3, PARK): wait up to 800 ms for TIP 3 before
    # another load. With pieces rolling as filmed: 53.5, TIP 3 in 2, PARK 2.
    "qual-right-o3-sweep": {**O3V3, "tip_ms": 800},
}
# Retuned for pieces rolling as filmed (6 Oct 2026, doc/rolling.md): the wait before driving into TIP 2's
# spill timed from the TIP's start (the films: first touch 1.1-1.4 s after it starts, whatever the TIP's
# length) instead of the CELL settling plus 500 ms; the intake runs, so pieces rolling our way come in.
RETUNE = {f"qual-right-o3-t{ms}": {**O3V3, "tip_ms": 800, "settle": False, "extra": ms} for ms in (1500, 2000, 2500, 3000)}
# The same, then the webcam pickup (CollectSeen) chases the spill for `seen` ms instead of driving straight
# through it (straight through, the chassis bats the pieces it misses 30-50 in away).
RETUNE.update({f"qual-right-o3-seen{ms}": {**O3V3, "tip_ms": 800, "settle": False, "extra": 1300, "seen": ms}
               for ms in (2000, 3000, 4000)})
# TIP 3 from what sits still: the GARDEN's 4 and the wall FLOWER's 4, either order, after TIP 2's spill fired
# from the tunnel run (whatever it caught); the wait before the spill timed from the TIP's start.
RETUNE.update({f"qual-right-o3-wf{o}-t{ms}": {**O3V3, "tip_ms": 800, "settle": False, "extra": ms, "third": False,
                                             "wall_flower": o, "garden": g}
               for o, g in (("after", "sweep"), ("first", "two")) for ms in (500, 1500)})
# ... or leaving as TIP 2 starts, down the west side at x `west` (no wait, no tunnel), to the wall FLOWER and the GARDEN.
RETUNE.update({f"qual-right-o3-west{x}": {**O3V3, "west": x} for x in (30, 36)})
# ... or through the tunnel (catching what it can of TIP 2's spill) and out of it straight to the wall FLOWER.
# Results, 6 Oct 2026, 20 runs (pieces rolling as filmed; the old baseline, qual-right-o3-sweep: 53.5, TIP 3 in 2):
# waits t1500-t3000 53.0-54.8, TIP 3 in 2-3 (waiting longer doesn't bring the spill to us); the webcam pickup
# (seen) 51.0-53.0, it catches nothing: the spill rolls out of its 36 in reach; the wall FLOWER after the GARDEN
# 53.3-54.3 (too late to fire); west (no tunnel) 56.0-58.0, TIP 3 in 5-7, but G409 in 4-20 runs; direct (tunnel,
# then straight to the wall FLOWER) 57.0-58.0 but drives into the HIVE frame; wfirst-t500 G409 in 14 runs.
# wffirst-t1500, the new baseline: 59.3, TIP 3 in 6, PARK 9, no G409.
# ... and TIP 2 fired from y 119 (as the Stages Autos: clear of a fast TIP's spill), with a shorter wait.
RETUNE.update({f"qual-right-o3-wffirst-t{ms}-n1190": {**O3V3, "tip_ms": 800, "settle": False, "extra": ms, "third": False,
                                                      "wall_flower": "first", "garden": "two", "n_fire": (57.5, 119, 270)}
               for ms in (1300, 1500)})
RETUNE.update({f"qual-right-o3-direct-t{ms}": {**O3V3, "settle": False, "extra": ms, "wall_flower": "direct"} for ms in (1300, 1500)})
# (from y 119: 52.3-53.3, TIP 3 in 0-1: the longer drive costs more than the shorter wait saves.)
# The baseline since 6 Oct 2026: TIP 2's spill caught on the tunnel run (1.5 s after TIP 2 starts) and fired,
# then the wall FLOWER's 4 and the GARDEN's 4 (they sit still) for TIP 3, then PARK.
O3["qual-right-o3"] = RETUNE["qual-right-o3-wffirst-t1500"]  # (replaced below, after 60 runs)
# The old route (the sweep through TIP 1's leftovers, then a third GARDEN load) with the TIP-timed wait, for the Rigid V,
# whose flaps catch the rolling pieces the Flat Intake misses.
# Clear of the spills (6 Oct 2026, 60 runs: a fast TIP 2 threw a piece onto the Flat Intake waiting at y 114; the Rigid V's
# flaps, waiting at S_FIRE for TIP 3, reached into its spill): back off to y 119 as TIP 2 starts; fire the last loads from y 20.
RETUNE.update({"qual-right-o3-wffirst-t1500-b119": {**RETUNE["qual-right-o3-wffirst-t1500"], "back_y": 119},
               "qual-right-o3-wffirst-t1700-b119": {**RETUNE["qual-right-o3-wffirst-t1500"], "back_y": 119, "extra": 1700},
               "qual-right-o3-sweep-f20": {**O3["qual-right-o3-sweep"], "fire_y": 20},
               "qual-right-o3-sweep-f20-b119": {**O3["qual-right-o3-sweep"], "fire_y": 20, "back_y": 119}})
# 60 runs (seeds 1-60): wffirst-t1500 55.8, TIP 3 in 11, G409 in 2 runs; with b119 55.0, TIP 3 in 7, no G409 (t1700-b119
# 54.8, 3). On the Rigid V the old sweep route 64.1, TIP 3 in 33, G409 in 3 runs (its flaps at TIP 3's spill near
# 27.5 s); sweep-f20 62.8, 30, G409 2; sweep-f20-b119 62.0, 28, G409 1; wffirst-t1500 59.8, 20, no G409.
# The Flat Intake's baseline: wffirst-t1500-b119. The Rigid V keeps the sweep (qual_shapes.ROUTE_OF).
O3["qual-right-o3"] = RETUNE["qual-right-o3-wffirst-t1500-b119"]
# Mentor review of the logs (6 Oct 2026): (1) the GARDEN load at ~28 s was being cut short for PARK: in "first" the
# last fire now waits for the TIP and PARK is not guaranteed; (2) "garden-first": keep the catch, fill up at the
# GARDEN, fire 4, then the wall FLOWER's 4 (no sensor needed); (3) one smooth path into the wall FLOWER.
BASE = RETUNE["qual-right-o3-wffirst-t1500-b119"]
RETUNE.update({"qual-right-o3-first-free": BASE,
               "qual-right-o3-garden-first": {**BASE, "wall_flower": "garden-first"},
               "qual-right-o3-garden-first-one": {**BASE, "wall_flower": "garden-first", "flower_in": "one"},
               "qual-right-o3-first-free-one": {**BASE, "flower_in": "one"},
               "qual-right-o3-first-one": {**BASE, "flower_in": "one", "wall_flower": "first-park"}})
# 60 runs (6 Oct 2026, mentor review): the baseline 55.0, TIP 3 in 7, PARK 28 (its GARDEN load cut for PARK at ~27 s);
# first-free (the GARDEN fire kept, PARK not guaranteed) 53.7, TIP 3 10, PARK 0: three TIP 3s (60 pts) for 28 PARKs
# (140); garden-first 51.7, TIP 3 4: the catch and the GARDEN as one load of 4, then the FLOWER's 4, is 8 POLLEN,
# exactly the tipping weight, so any miss costs TIP 3, while firing the catch as its own load adds margin; the one
# smooth path into the wall FLOWER arrives 0.7 s sooner (first-free-one TIP 3 at 27.3 against 28.0). The baseline:
# the review's ending plus the smooth path, first-one: 54.8, TIP 3 in 10, PARK 13 (the FLOWER load fires sooner, so
# TIP 3 comes more often and PARK less; mentor, 5 Oct: TIP 3 before PARK).
O3["qual-right-o3"] = RETUNE["qual-right-o3-first-one"]
RETUNE.update({f"qual-right-o3-sweep-t{ms}": {**O3V3, "tip_ms": 800, "settle": False, "extra": ms} for ms in (1300, 1500)})
O3.update(RETUNE)

def fit(robot):
    """A Route class for `robot`: as right() does by hand, the start (backed against a wall) moves back
    by the difference in front face from the 18 in robot, and the points its front meets (a FLOWER,
    the GARDEN) and PARK move forward by as much."""
    import math
    d = 9.0 - FRONT_IN[robot]
    df = flower_shift(robot)

    def move(p, sign, by=d):
        x, y, h = p
        return (round(x + sign * by * math.cos(math.radians(h)), 2), round(y + sign * by * math.sin(math.radians(h)), 2), h)

    class Fit(Sized):
        size = 2 * FRONT_IN[robot]

        def __init__(self, name, start, **kw):
            super().__init__(name, move(start, -1), **kw)

        def pt(self, name, x, y, h):
            if name.startswith(("FAR_FLOWER", "WALL_FLOWER")):
                x, y, h = move((x, y, h), 1, df)
            elif name.startswith(("GARDEN", "PARK")):
                x, y, h = move((x, y, h), 1)
                if name == "GARDEN":
                    x, y = max(x, GARDEN_X_IN[robot]), max(y, round(garden_y(robot), 2))
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
PARK_B = (11, 115)  # (14, 109) until 7 Oct 2026 (mentor: the path too steep, its corner 3 in from our PARK): now flatter and square against the alliance wall, body x 2-20, y 106-124, 8 in above our PARK


def wall_partner(name="partner-stage19-side-park"):
    r = Route(name, (19, 132.25, 270), speed=40)
    # Body y 103-121 at the LOADING ZONE's top (its corner (10, 117.9) in the zone), leaving the zone's bottom for our
    # PARK (13, 87.44; mentor, 7 Oct 2026: "partner and us should basically always park"). At y 100 it sat on our spot.
    # Our row sweep passes it with its west edge just east of x 28 (guide_routes.ROW_X_OFFSET).
    r.pt("PARK_P", 18, 112, 270)  # x 18: east edge 27, a corner (9, 103) in the zone
    r.add(r.go("PARK_P", heading=270, park=True))
    return r


def angled_partner(name="partner-angled-park"):
    r = Route(name, ANGLED, speed=40)
    r.pt("PARK_P", *PARK_B, 270)  # turns square on the way: angled, an 18 in body reaches 12.7 in to a corner
    r.add(r.go("PARK_P", park=True))
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
    elif partner == "A":  # from N_LOW, facing the left CELL: west at y 118 as above, the turn done by x 39
        # (a turning robot's corners reach 10.25 in; the parked partner's east edge is at x 28, the row at y 127)
        out += [r.go("TURN_A", turn_after=0.0, turn_by=0.45), r.go("ROW_S", heading=0)]
    else:  # from N_LOW, facing the left CELL
        out.append(r.go("ROW_S", turn_by=0.8))
    out += [r.go("ROW_N", heading=end_h), r.wait("The staged row", when=["IntakeFull"], ms=row_ms)]
    r.at = "ROW_N"
    return out


def stages_staged(name, partner="A", plan="chase", land=500, row_ms=1600, robot="option3", n_fire_y=None,
                  catch_ms=0, stand=0, topup=False, keep_last=False, **kw):
    """Qual-PartnerStages with a realistic staging partner (A or B). plan "chase": TIP 1, its spill caught
    driving north through the tunnel and fired, then the staged row (as qual.stages). plan "west": TIP 1,
    then north along the west lane (clear of TIP 1's spill) to the staged row, fired, then the far FLOWER,
    fired (TIP 2 from 8 sure pieces). Then tail(**kw) after TIP 2; if TIP 2 hasn't come, the far FLOWER
    ("chase") or TIP 1's leftovers are not reachable in time, so tail anyway."""
    r = fit(robot)(name, S_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", N_FIRE[0], n_fire_y or N_FIRE[1], N_FIRE[2])
    # A parks at (19, 100), on our PARK spot. Parking round it instead (up a lane east of it to the LOADING
    # ZONE's free top corner, 13, 116.5) took too long to finish by 30 s, and the endgame guard's cut-short
    # park then drove into the HIVE frame: with A, no PARK (tail(park=False)).
    # keep_last (6 Oct 2026): stop firing when the TIP starts. The 3rd preload tips the match-start CELL and the 4th,
    # 0.45 s later, meets a CELL already swinging (it hits the HIVE or goes long in every run): kept, it is one
    # more piece for the next load.
    r.add(fire(r, "Fire the preloads (TIP 1)", "Tip" if keep_last else "Empty", ms=4000))
    if plan == "chase":
        # Mentor, 6 Oct 2026 (no sensors in the baseline): catch TIP 1's spill at the drop zone, the wait timed from
        # the TIP's start (catch_ms; before: the CELL settled plus `land`), standing `stand` ms more as it rolls
        # back, then north through it and the tunnel. Then either fire the catch, the row and the FLOWER as three
        # loads, or (topup) fill up to 4 at the row first and fire once, then the FLOWER.
        if catch_ms:
            lands = [r.wait("TIP 1 starts", when=["Tip"], ms=3000), r.wait("It lands", when=["IntakeFull"], ms=catch_ms)]
            if stand:
                lands.append(r.wait("Catch TIP 1's spill", when=["IntakeFull"], ms=stand))
        else:
            lands = [r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500)] + ([r.wait("It lands", when=["IntakeFull"], ms=land)] if land else [])
        r.add(r.go("S_CATCH"), *lands, tunnel(r, "N_TURN"), r.go("N_LOW", heading=270))
        if not topup:
            r.add(fire(r, "Fire the catch", "Empty", ms=2200))
        r.at = "N_LOW"
        r.add(*staged_row(r, partner, row_ms, west=False))
        r.add(r.go("N_LOW", turn_after=0.2, turn_by=0.9), fire(r, "Fire the catch and the row" if topup else "Fire the row", "Tip", ms=2500))
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


# Qual-PartnerStages for the Flat Intake, with partners.stage_exit. 20 runs, normal / slow tiles: TIP 2, TIP 3,
# PARK (ours), AUTO points; G409 runs. TIP 2 comes at about 21 s, too late for a TIP 3 on the Flat Intake.
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
    # Until 6 Oct 2026 12:00 UTC (with pieces rolling as filmed: 54.8, G409 in 2 runs).
    "qual-stages-angled-v1": lambda name: stages_staged(name, partner="B", plan="chase", **{**V3, "third": False, "garden": "two"}),
    # The one G409 run (normal tiles) isn't TIP 1's spill: 700 / 900 ms before driving into it, still one.
    **{f"qual-stages-angled-l{ms}": (lambda name, ms=ms: stages_staged(name, partner="B", plan="chase", land=ms,
        **{**V3, "third": False, "garden": "two"})) for ms in (700, 900)},
    # Until 6 Oct 2026 12:00 UTC (with pieces rolling as filmed: 49.0, G409 in 1 run).
    "qual-stages-wall-v1": lambda name: stages_staged(name, partner="A", plan="west",
                                                      **{**V3, "third": False, "garden": "two", "park": False}),
    # Retuned for pieces rolling as filmed (6 Oct 2026): the wait before TIP 2's spill timed from the TIP's start.
    **{f"qual-stages-angled-t{ms}": (lambda name, ms=ms: stages_staged(name, partner="B", plan="chase",
        **{**V3, "third": False, "garden": "two", "settle": False, "extra": ms})) for ms in (1300, 1500, 2000)},
    # ... and TIP 2 fired from further back (the left CELL scores from y 113-129): a fast TIP threw a POLLEN onto
    # us standing at y 113.5 (G409, 1-2 runs in 20).
    **{f"qual-stages-angled-t1300-n{int(y * 10)}": (lambda name, y=y: stages_staged(name, partner="B", plan="chase", n_fire_y=y,
        **{**V3, "third": False, "garden": "two", "settle": False, "extra": 1300})) for y in (117.5, 119)},
    **{f"qual-stages-wall-t1300-n{int(y * 10)}": (lambda name, y=y: stages_staged(name, partner="A", plan="west", n_fire_y=y,
        **{**V3, "third": False, "garden": "two", "park": False, "settle": False, "extra": 1300})) for y in (117.5, 119)},
    **{f"qual-stages-wall-t{ms}": (lambda name, ms=ms: stages_staged(name, partner="A", plan="west",
        **{**V3, "third": False, "garden": "two", "park": False, "settle": False, "extra": ms})) for ms in (1300, 1500, 2000)},
    **{f"qual-stages-a-{plan}-stay": (lambda name, plan=plan: stages_staged(
        name, partner="A", plan=plan, **{**V3, "third": False, "garden": "two", "park": False})) for plan in ("chase", "west")},
}
# The baselines since 6 Oct 2026: the wait before TIP 2's spill timed from the TIP's start (1.3 s; settle + 500 ms
# before) and TIP 2 fired from y 119 (113.5 before: a fast TIP threw a POLLEN onto us). Angled 55.0, TIP 2 in 19,
# PARK 20, no G409; wall 49.0, TIP 2 in 18, no G409 (t1300-n1190 above; y 117.5 still one G409 run each).
STAGES["qual-stages-angled"] = STAGES["qual-stages-angled-t1300-n1190"]
# (60 runs: angled 50.6 against 51.6, wall 48.0 against 47.3: within noise either way, not adopted.)
STAGES["qual-stages-angled-keep"] = lambda name: stages_staged(name, partner="B", plan="chase", n_fire_y=119, keep_last=True,
                                                               **{**V3, "third": False, "garden": "two", "settle": False, "extra": 1300})
STAGES["qual-stages-wall-keep"] = lambda name: stages_staged(name, partner="A", plan="west", n_fire_y=119, keep_last=True,
                                                             **{**V3, "third": False, "garden": "two", "park": False, "settle": False, "extra": 1300})
# Where TIP 2's shots miss from (6 Oct 2026, with no shot spread the angled Auto still loses 1.1 shots a run, hitting
# the HIVE or going long): the firing spot's distance, y 113.5 (the old spot) to 122.
STAGES.update({f"qual-stages-angled-n{int(y * 10)}": (lambda name, y=y: stages_staged(name, partner="B", plan="chase", n_fire_y=y,
    **{**V3, "third": False, "garden": "two", "settle": False, "extra": 1300})) for y in (113.5, 116, 122)})
# Mentor review (6 Oct 2026): catch TIP 1's spill at the drop zone first, both partners, then fill up and fire.
TAIL = {**V3, "third": False, "garden": "two", "settle": False, "extra": 1300}
for p, kind, extra in (("B", "angled", {}), ("A", "wall", {"park": False})):
    for c in (1300, 1500):
        for st in (0, 1000):
            for t in (False, True):
                STAGES[f"qual-stages-{kind}-catch{c}{'-stand' + str(st) if st else ''}{'-topup' if t else '-three'}"] = (
                    lambda name, p=p, c=c, st=st, t=t, extra=extra: stages_staged(
                        name, partner=p, plan="chase", n_fire_y=119, catch_ms=c, stand=st, topup=t, **{**TAIL, **extra}))
STAGES["qual-stages-wall"] = STAGES["qual-stages-wall-t1300-n1190"]


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
    for f in autogen.FRICTIONS if stages else ():
        study(";".join(f"{cls(w)},{PARTNER_OF(w)}@50" for w in stages), runs=runs, designs=os.environ.get("DESIGN", D),
              extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                         "BIOBUZZ_AUTO_FRICTION": f})
    for w in which:
        r = right(w, **{**VARIANTS, **TRIALS, **PROTO, **G409, **O3}[w])
        r.folder = autogen.AUTOS_DIR if w in WINNERS else autogen.EXPERIMENTS
        r.write()
    for f in autogen.FRICTIONS if which else ():
        study(";".join(f"{cls(w)},PartnerPreloadsRightAuto@50" for w in which), runs=runs, designs=os.environ.get("DESIGN", D),
              extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                         "BIOBUZZ_AUTO_FRICTION": f})
