"""Qual-PartnerShootsRight, worked on apart from qual.py (which another session edits): the variants
tried to make TIP 3 come in every run without fouling G409, and the baseline, qual-right-v3.

    python3 qual_right.py [runs] [variant ...]

exports each variant (into auto-builder/experiments, except the winner) and simulates it with
partners.preloads_right on normal and slow tiles (AUTO_BUILDER_DIR as for autogen.py).
"""
import sys
import autogen
from qual import *


def tail(r, spill_at="S_CATCH", garden="two", leftovers=False, settle=True, tag="", fire_y=None, extra=0, lane_x=None, sweep_y=12, third=False, garden_ms=2500, stand=0, seen=0, catch3=False):
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
        return out + third_load(r, tag, catch3=catch3)
    if catch3:  # no third load and no PARK: stand at the catch spot for TIP 3's spill, for TELEOP
        return out + [r.go("S_CATCH", heading=90), r.wait(f"Catch TIP 3's spill{tag}", when=["IntakeFull"], ms=8000)]
    park = r.go("PARK", ctrl=[(28, 24), (24, 70)], heading=90, park=True)
    if not leftovers:
        return out + [park]
    # Not TIP 3 yet: what lies in front of the right CELL (missed shots, TIP 2's spill), with the
    # webcam, then back to S_FIRE and fired straight on; PARK only once that fire is over.
    more = [*catch(r, f"Leftovers{tag}", "S", ms=leftovers), r.go("S_FIRE", heading=90),
            fire(r, f"Fire the leftovers{tag}", "LeftCellUp", ms=2000)]
    r.at = "S_FIRE"
    more.append(r.go("PARK", ctrl=[(28, 24), (24, 70)], heading=90))
    r.at = "S_FIRE"
    out.append(r.wait(f"TIP 3?{tag}", when=["Tip"], ms=700, yes=[park], no=more,
                      yes_label="Yes: PARK", no_label="No: leftovers"))
    return out


def third_load(r, tag="", wait_full=1100, catch3=False):
    """From S_FIRE once the GARDEN's shots are away. A TIP under way or done (the right CELL no longer
    up): PARK. If not: back to the GARDEN (what the sweep left in it) and look again there; still no
    TIP, so carry what it holds back to S_FIRE and fire it. A TIP that completes in the 8 s after AUTO
    still counts (Competition Manual §10.5), so shots away just before 30 s can still make it. No park
    path after that fire (the endgame guard would cut it short), only a path there, parked if it
    arrives by 30 s."""
    r.at = "S_FIRE"
    park_here = [r.go("PARK", ctrl=[(28, 24), (24, 70)], heading=90, park=True)]
    if catch3:  # instead of PARK: stand at the catch spot for TIP 3's spill, for TELEOP
        park_here = [r.go("S_CATCH", heading=90), r.wait(f"Catch TIP 3's spill{tag}", when=["IntakeFull"], ms=6000)]
    r.at = "S_FIRE"
    go = r.go("GARDEN", ctrl=[(8.5, 30)], turn_by=0.7)
    r.at = "GARDEN"
    done = [r.go("PARK", ctrl=[(30, 22), (26, 70)], heading=90, turn_by=0.5, park=True)]
    if catch3:
        done = [r.go("S_CATCH", turn_after=0.3, turn_by=1.0), r.wait(f"Catch TIP 3's spill (B){tag}", when=["IntakeFull"], ms=6000)]
    r.at = "GARDEN"
    more = [r.wait(f"The GARDEN again{tag}", when=["IntakeFull"], ms=wait_full), r.go("S_FIRE", turn_after=0.3, turn_by=1.0),
            r.wait(f"Fire the GARDEN again{tag}", when=["LeftCellUp"], ms=2500, alongside="LaunchAll")]
    r.at = "S_FIRE"
    more.append(r.go("PARK", ctrl=[(28, 24), (24, 70)], heading=90))
    there = r.wait(f"Still no TIP 3?{tag}", when=["RightCellUp"], ms=20, yes=more, no=done,
                   yes_label="No TIP: fire the GARDEN", no_label="TIP 3: PARK")
    return [r.wait(f"No TIP 3 yet?{tag}", when=["RightCellUp"], ms=20, yes=[go, there], no=park_here,
                   yes_label="No TIP: the GARDEN", no_label="TIP 3: PARK")]


# How far each robot's front face is from its centre: points where the front meets something (the
# start wall behind, a FLOWER, the GARDEN) move by the difference from the 18 in robot the route was
# drawn for, and PARK by as much, so a corner still reaches the LOADING ZONE. The firing spots stay: the prototype scores straight on from y 17-29 and 113-125 (ShotMapTest).
FRONT_IN = {"baseline": 9.0, "proto": 7.5}  # proto: RobotDesign.buildersPrototype, 15 in


def right(name, robot="baseline", n_fire=None, **kw):
    """As qual.shoots_right, with tail(**kw) after TIP 2, for `robot` (FRONT_IN)."""
    d = 9.0 - FRONT_IN[robot]
    r = Route(name, (N_START[0], N_START[1] + d, N_START[2]), speed=50)
    ends(r)
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
PROTO = {"qual-right-v3-proto": {**VARIANTS["qual-right-v3"], "robot": "proto"}}
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
WINNER = "qual-right-v3"


def cls(name):
    return "".join(w.capitalize() for w in name.split("-")) + "Auto"


if __name__ == "__main__":
    runs = int(sys.argv[1]) if len(sys.argv) > 1 else 20
    which = sys.argv[2:] or list(VARIANTS)
    for w in which:
        r = right(w, **{**VARIANTS, **TRIALS, **PROTO, **G409}[w])
        r.folder = autogen.AUTOS_DIR if w == WINNER else autogen.EXPERIMENTS
        r.write()
    for f in ("1", "3"):
        study(";".join(f"{cls(w)},PartnerPreloadsRightAuto@50" for w in which), runs=runs, designs=D,
              extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                         "BIOBUZZ_AUTO_FRICTION": f})
