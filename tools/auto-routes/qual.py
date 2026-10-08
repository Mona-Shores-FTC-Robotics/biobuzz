"""Qualifier Autos for the two-wheel launcher robot (4 Oct 2026): one launcher for POLLEN and NECTAR,
an 18 in front intake. Three partners, one Auto each:

  qual-partner-shoots-left   partner at the standard left start fires its 4 preloads when the left CELL
                             rises, then parks (partners.preloads_left)
  qual-partner-parks-left    partner at the standard left start only drives and parks (partners.park_left);
                             the same route as qual-partner-shoots-left
  qual-partner-shoots-right  partner at the right start fires its 4 preloads at once (TIP 1), parks

Both left partners park west under the far FLOWER, out of the tunnel's north exit. Every shot is
straight on, on the CELL's axis (mentor review, 4 Oct), and we wait for a TIP facing the HIVE, its
camera on it.

Run `python3 qual.py [runs] [shoots-left|parks-left|shoots-right ...]` to export them and simulate each
with its partner on normal and slow tiles (AUTO_BUILDER_DIR as for autogen.py).

The plan is a shuttle through the tunnel under the HIVE (centre x 57.5, square to the field: the
shortest way between the CELLs), recycling each spill into the next TIP:

  - A TIP needs about 8 POLLEN-weights (NECTAR is 1.65); TIP 1 needs 3 POLLEN on the 3 NECTAR.
  - Each spill lands about 42 in out from its CELL's wall and scatters (3 Oct 2026 films). It is the
    nearest source of pieces for the CELL that rises next, which is at the other end: so catch it
    where it lands (webcam, CollectSeen) and carry it through the tunnel.
  - The robot holds 4 (G407), so TIP 3 on an empty CELL is two loads: one carried through the tunnel,
    one from the nearest source at that end.
"""
import sys
import autogen
from helpers import *

D = "spring hood, full-width intake"

# The right (south) and left (north) ends, drawn for RED. A turning robot's corners reach 12.7 in: turn
# only at x <= 58 (the centre line is 70.75) and clear of the HIVE frame's feet (y 51.3-90.2).
S_START, N_START = (59, 9.5, 90), (59, 132.25, 270)
S_SHOOT, N_SHOOT = (57.5, 14, 90), (57.5, 127.5, 270)  # 43 in from each CELL's aim point, head-on
S_CATCH, N_CATCH = (57.5, 28, 90), (57.5, 113.5, 270)  # just short of where a spill lands, facing it
# Straight on, out of the tunnel's north exit: the left CELL scores from y 113 (ShotMapTest; 109 is too
# close for the hood), and our edge (y 123) stays just below a partner at the standard left start (its
# edge at 123.25).
N_LOW = (57.5, 114, 270)
# Where we turn round after coming north through the tunnel: a turning robot's corners reach 12.7 in, so
# at y 104 they clear the HIVE frame (y 90.2) and a partner at the left start or in its lane (y 118.5).
# Then straight back into N_LOW.
N_TURN = (57.5, 104, 270)
TUNNEL_S, TUNNEL_N = 38, 103.5  # a robot square in the tunnel may turn only outside these


def ends(r):
    r.pt("S_SHOOT", *S_SHOOT).pt("N_SHOOT", *N_SHOOT).pt("N_LOW", *N_LOW).pt("N_TURN", *N_TURN)
    r.pt("S_CATCH", *S_CATCH).pt("N_CATCH", *N_CATCH)
    # Where the robot settles after a webcam pickup, 3 in back toward its wall, still facing the HIVE.
    r.pt("S_BACK", S_CATCH[0], S_CATCH[1] - 3, 90).pt("N_BACK", N_CATCH[0], N_CATCH[1] + 3, 270)
    r.pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    # AUTO PARK: one corner in the LOADING ZONE (x 0-11, y 94.3-117.9), below the partner parked at its far end.
    # (13, 86) until 8 Oct 2026: only a corner's tip reached into the LOADING ZONE (y 94.3-117.9; mentor: "parks too
    # close to the park zone"). Now the body sits 8 in inside: the fit adds half the body's shortfall from 18 in, so
    # the V's centre lands at (10.5, 95), its frame y 87.4-102.6, x 2.9-18.1. The partners park at the zone's far end.
    r.pt("PARK", 10.5, 93.56, 90)
    flower_points(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)


def catch(r, label, end, ms=2500, land_ms=0):
    """At the catch spot of `end` ("S" or "N"), take what the spill puts near us with the webcam, then
    back to that end's BACK point (the next path starts there). Drives to the catch spot first unless
    already on it."""
    out = [] if r.at == f"{end}_CATCH" else [r.go(f"{end}_CATCH", turn_by=0.8)]
    # The webcam pickup looks once and gives up if nothing is on the tiles yet: give the spill time to land.
    if land_ms:
        out.append(r.wait(f"{label}: it lands", when=["IntakeFull"], ms=land_ms))
    out.append(r.wait(label, when=["IntakeFull"], ms=ms, alongside="CollectSeen"))
    r.at = f"{end}_CATCH"
    return out + [r.go(f"{end}_BACK", turn_after=0.4, turn_by=1.0)]


def tunnel(r, to):
    """From the BACK point at one end through the tunnel to `to` at the other end, square inside it,
    turning to face the far CELL only once clear of the HIVE frame."""
    a, b = r.points[r.at], r.points[to]
    length = abs(b[1] - a[1])
    clear = (TUNNEL_N - a[1]) / (b[1] - a[1]) if b[1] > a[1] else (a[1] - TUNNEL_S) / (a[1] - b[1])
    return r.go(to, turn_after=round(min(0.95, clear + 0.02), 2), turn_by=1.0)


# Every shot is straight on: on the CELL's axis (x 57.5), anywhere from the start spot out to the shot
# map's limit (the right CELL scores from y 13-29, the left from 113-129). It is the high-percentage
# shot, and the half second it takes to drive there is worth it. Static sources are carried here.
S_FIRE = (57.5, 24, 90)
N_FIRE = N_LOW


def south_tail(r, tag="", wall=True, leftovers=None):
    """From where we fired at the left CELL (facing the HIVE, camera on it) while TIP 2 goes over: south
    through its spill as it lands and the tunnel, firing what we caught; then the GARDEN's 4 and, if TIP
    3 hasn't come, the wall FLOWER's 4, each carried back to S_FIRE and fired straight on; then PARK."""
    out = [r.wait(f"TIP 2 settles{tag}", when=["RightCellUp"], ms=2500), tunnel(r, "S_CATCH"),
           fire(r, f"Fire TIP 2's spill{tag}", "Empty", ms=2000)]
    r.at = "S_CATCH"
    if leftovers == "before":  # what lies about our end (TIP 1's spill, with its NECTAR), with the webcam
        out += catch(r, f"Leftovers{tag}", "S", ms=1500)
        r.at = "S_BACK"
    out += [r.go("GARDEN_IN", turn_by=0.6), r.go("GARDEN", heading=270),
            r.wait(f"The GARDEN{tag}", when=["IntakeFull"], ms=1500),
            r.go("S_FIRE", turn_after=0.3, turn_by=1.0), fire(r, f"Fire the GARDEN{tag}", "Empty", ms=2500)]
    # wall=False (qual-partner-shoots-right): TIP 3 finishes on its own once the GARDEN's shots are away,
    # and the wall FLOWER trip never got back in time to add one there, so go straight to PARK (+5).
    # The left routes keep it: there it makes TIP 3 in 3 to 10 more runs of 20.
    if leftovers == "after":  # not TIP 3 yet: what lies about our end, then PARK
        r.at = "S_FIRE"
        more = [r.go("S_CATCH", heading=90), *catch(r, f"Leftovers{tag}", "S", ms=1500),
                fire(r, f"Fire the leftovers{tag}", "Empty", ms=2000)]
        r.at = "S_BACK"
        more.append(r.go("PARK", ctrl=[(28, 24), (24, 70)], heading=90))
        r.at = "S_FIRE"
        done = [r.go("PARK", ctrl=[(28, 24), (24, 70)], heading=90, park=True)]
        out.append(r.wait(f"TIP 3?{tag}", when=["Tip"], ms=1600, yes=done, no=more, yes_label="Yes: PARK", no_label="No: leftovers"))
        return out
    if not wall:
        r.at = "S_FIRE"
        return out + [r.go("PARK", ctrl=[(28, 24), (24, 70)], heading=90, park=True)]
    r.at = "S_FIRE"
    wall = [*flower(r, "WALL_FLOWER", f"The wall FLOWER{tag}", ms=2300)]
    r.at = "WALL_FLOWER"
    wall += [r.go("S_FIRE", turn_after=0.3, turn_by=1.0), fire(r, f"Fire the wall FLOWER (TIP 3){tag}", "LeftCellUp", ms=2500),
             r.go("PARK", ctrl=[(28, 24), (24, 70)], heading=90)]
    # Not a park path: that makes the endgame guard cut the fire short to leave time to drive there, and
    # TIP 3 (20) is worth more than PARK (5). Driven only once the fire is over; parked if it gets there by 30 s.
    r.at = "S_FIRE"
    done = [r.go("PARK", ctrl=[(28, 24), (24, 70)], heading=90, park=True)]
    out.append(r.wait(f"TIP 3?{tag}", when=["Tip"], ms=600, yes=done, no=wall, yes_label="Yes: PARK", no_label="No: the wall FLOWER"))
    return out


def shoots_left(name="qual-partner-shoots-left", wall=True):
    """Partner at the standard left start, in front of the left CELL: either fires its 4 when the left
    CELL rises and parks (partners.preloads_left), or only parks (partners.park_left). Both park west,
    under the far FLOWER, so they never come into the tunnel's exit or where we fire from.

    TIP 1: our 4 preloads. TIP 2: the partner's 4 plus what we catch of TIP 1's spill, driving north
    through it as it lands and on through the tunnel; if that wasn't enough, the far FLOWER's 4, fired
    from beside it. TIP 3: TIP 2's spill caught the same way going south, the GARDEN's 4, then the wall
    FLOWER's 4 if still needed, each fired where it is picked up."""
    r = Route(name, S_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE)
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000), r.go("S_CATCH"),
          r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500),
          tunnel(r, "N_TURN"), r.go("N_LOW", heading=270), fire(r, "Fire the catch at the left CELL", "Empty", ms=2200))
    r.at = "N_LOW"
    tipped = south_tail(r, wall=wall)
    r.at = "N_LOW"
    more = [*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)]
    r.at = "FAR_FLOWER"
    more += [r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500)]
    r.at = "N_FIRE"
    more += south_tail(r, " (B)", wall=wall)
    r.at = "N_LOW"
    r.add(r.wait("TIP 2?", when=["Tip"], ms=1000, yes=tipped, no=more, yes_label="Yes", no_label="No: the far FLOWER"))
    return r


def shoots_right(name="qual-partner-shoots-right", wall=False, leftovers=None):
    """Partner starts right, in front of the right CELL, and fires its 4 at once: TIP 1 (with the 3
    NECTAR). We start left and wait straight on in front of the left CELL, facing the HIVE (its camera
    on it), spun up. TIP 2: our 4 preloads when the left CELL rises, then the far FLOWER's 4 carried
    back to the same spot; TIP 3: TIP 2's spill caught going south and the GARDEN's 4, then straight to
    PARK (the wall FLOWER trip never got back in time to help)."""
    r = Route(name, N_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE)
    r.add(r.action("SpinUp"), r.go("N_FIRE", heading=270),
          r.wait("TIP 1 (the partner)", when=["LeftCellUp"], ms=9000),
          fire(r, "Fire the preloads at the left CELL", "Empty", ms=2500))
    r.at = "N_FIRE"
    r.add(*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300))
    r.at = "FAR_FLOWER"
    r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500))
    r.at = "N_FIRE"
    r.add(*south_tail(r, wall=wall, leftovers=leftovers))
    return r


def stages(name="qual-partner-stages"):
    """Partner can't shoot: from (35, 132.25) it drives forward, sets its 4 POLLEN down in a row at about
    x 30-40, y 108 (west of our tunnel lane) and parks (partners.stage_exit). TIP 1: our 4 preloads.
    TIP 2: north through the tunnel through TIP 1's spill (with its NECTAR), turned round and fired
    straight on from N_LOW; then west along the row intake first, back to N_LOW and fired; the far
    FLOWER if still short. TIP 3: as qual-partner-shoots-left."""
    r = Route(name, S_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE)
    r.pt("ROW_E", 50, 108, 180).pt("ROW_W", 29, 108, 180)  # east of the row, then through it facing west (x 29: clear of the parked partner)
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000), r.go("S_CATCH"),
          r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500),
          tunnel(r, "N_TURN"), r.go("N_LOW", heading=270), fire(r, "Fire the catch", "Empty", ms=2200),
          r.go("ROW_E", turn_by=0.8), r.go("ROW_W", heading=180),
          r.wait("The row", when=["IntakeFull"], ms=600),
          r.go("N_LOW", turn_after=0.2, turn_by=0.9), fire(r, "Fire the row", "Tip", ms=2500))
    r.at = "N_LOW"
    flower_more = [*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)]
    r.at = "FAR_FLOWER"
    flower_more += [r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500)]
    r.at = "N_FIRE"
    flower_more += south_tail(r, " (B)")
    r.at = "N_LOW"
    r.add(r.wait("TIP 2?", when=["Tip"], ms=600, yes=south_tail(r), no=flower_more, yes_label="Yes", no_label="No: the far FLOWER"))
    return r


QUALS = {  # our Auto, its partner (the qualifier partners), as AutoStudyTest specs
    "stages": ("QualPartnerStagesAuto", "PartnerStageExitAuto"),
    "shoots-left": ("QualPartnerShootsLeftAuto", "PartnerPreloadsLeftAuto"),
    "parks-left": ("QualPartnerParksLeftAuto", "PartnerParkLeftAuto"),
    "shoots-right": ("QualPartnerShootsRightAuto", "PartnerPreloadsRightAuto"),
}


def write_all():
    import partners
    partners.preloads_left().write()
    partners.park_left().write()
    shoots_left().write()
    # The same route, for a partner at the left start that only drives and parks: its own file so the
    # Simulate Auto workflow keeps its results and logs apart.
    shoots_left("qual-partner-parks-left").write()
    shoots_right().write()
    partners.stage_exit().write()
    stages().write()


if __name__ == "__main__":
    write_all()
    which = sys.argv[2:] or list(QUALS)
    for f in autogen.FRICTIONS:
        study(";".join(f"{QUALS[w][0]},{QUALS[w][1]}@50" for w in which), runs=int(sys.argv[1]) if len(sys.argv) > 1 else 20,
              designs=D, extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                                    "BIOBUZZ_AUTO_FRICTION": f})
