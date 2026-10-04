"""Qualifier Autos for the two-wheel launcher robot (4 Oct 2026): one launcher for POLLEN and NECTAR,
an 18 in front intake. Three partners, one Auto each:

  qual-partner-shoots-left   partner starts left (north), fires its 4 preloads when the left CELL rises
  qual-partner-shoots-right  partner starts right (south) and fires its 4 preloads at once (TIP 1)
  qual-partner-parks         partner can't shoot: leaves its 4 preloads lined up at its side, parks

Run `python3 qual.py [runs] [shoots-left|shoots-right|parks ...]` to export them and simulate each with its
partner on normal and slow tiles (AUTO_BUILDER_DIR as for autogen.py).

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
from helpers import *

D = "spring hood, full-width intake"

# The right (south) and left (north) ends, drawn for RED. A turning robot's corners reach 12.7 in: turn
# only at x <= 58 (the centre line is 70.75) and clear of the HIVE frame's feet (y 51.3-90.2).
S_START, N_START = (59, 9.5, 90), (59, 132.25, 270)
S_SHOOT, N_SHOOT = (57.5, 14, 90), (57.5, 127.5, 270)  # 43 in from each CELL's aim point, head-on
S_CATCH, N_CATCH = (57.5, 28, 90), (57.5, 113.5, 270)  # just short of where a spill lands, facing it
TUNNEL_S, TUNNEL_N = 38, 103.5  # a robot square in the tunnel may turn only outside these


def ends(r):
    r.pt("S_SHOOT", *S_SHOOT).pt("N_SHOOT", *N_SHOOT)
    r.pt("S_CATCH", *S_CATCH).pt("N_CATCH", *N_CATCH)
    # Where the robot settles after a webcam pickup, 3 in back toward its wall, still facing the HIVE.
    r.pt("S_BACK", S_CATCH[0], S_CATCH[1] - 3, 90).pt("N_BACK", N_CATCH[0], N_CATCH[1] + 3, 270)
    r.pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    # AUTO PARK: one corner in the LOADING ZONE (x 0-11, y 94.3-117.9), below the partner parked at its far end.
    r.pt("PARK", 13, 86, 90)
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


def south_tail(r, tag=""):
    """From N_CATCH while TIP 2 goes over: south through its spill as it lands and the tunnel, firing
    what we caught from the catch spot; then the GARDEN's 4 and the wall FLOWER's 4 for TIP 3, each
    fired where it is picked up; then PARK, beside the partner in the LOADING ZONE."""
    r.at = "N_CATCH"
    out = [r.wait(f"TIP 2 settles{tag}", when=["RightCellUp"], ms=2500), tunnel(r, "S_CATCH"),
           fire(r, f"Fire TIP 2's spill{tag}", "Empty", ms=2000)]
    r.at = "S_CATCH"
    out += [r.go("GARDEN_IN", turn_by=0.6), r.go("GARDEN", heading=270),
            r.wait(f"The GARDEN{tag}", when=["IntakeFull"], ms=1500),
            r.go("FIRE_GARDEN", heading=270), fire(r, f"Fire the GARDEN{tag}", "Empty", ms=2500)]
    r.at = "FIRE_GARDEN"
    wall = [*flower(r, "WALL_FLOWER", f"The wall FLOWER{tag}", ms=2300)]
    r.at = "WALL_FLOWER"
    wall += [r.go("FIRE_WALL", heading=180), fire(r, f"Fire the wall FLOWER (TIP 3){tag}", "LeftCellUp", ms=2500),
             r.go("PARK", ctrl=[(26, 64)], heading=90, turn_after=0.3)]
    # Not a park path: that makes the endgame guard cut the fire short to leave time to drive there, and
    # TIP 3 (20) is worth more than PARK (5). Driven only once the fire is over; parked if it gets there by 30 s.
    r.at = "FIRE_GARDEN"
    done = [r.go("PARK", ctrl=[(34, 36), (30, 76)], heading=90, park=True)]
    out.append(r.wait(f"TIP 3?{tag}", when=["Tip"], ms=600, yes=done, no=wall, yes_label="Yes: PARK", no_label="No: the wall FLOWER"))
    return out


def shoots_left(name="qual-partner-shoots-left"):
    """Partner starts beside the left CELL (partners.preloads_side) and fires its 4 at it when it rises.

    TIP 1: our 4 preloads. TIP 2: the partner's 4 plus what we catch of TIP 1's spill, driving north
    through it as it lands and on through the tunnel; if that wasn't enough, the far FLOWER's 4, fired
    from beside it. TIP 3: TIP 2's spill caught the same way going south, the GARDEN's 4, then the wall
    FLOWER's 4 if still needed, each fired where it is picked up."""
    r = Route(name, S_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("FIRE_GARDEN", *FIRE_GARDEN).pt("FIRE_WALL", *FIRE_WALL).pt("FIRE_FAR", *FIRE_FAR)
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000), r.go("S_CATCH"),
          r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500),
          tunnel(r, "N_SHOOT"), fire(r, "Fire the catch at the left CELL", "Empty", ms=2200))
    r.at = "N_SHOOT"
    tipped = [r.go("N_CATCH", heading=270), *south_tail(r)]
    r.at = "N_SHOOT"
    more = [*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)]
    r.at = "FAR_FLOWER"
    more += [r.go("FIRE_FAR", heading=90), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500)]
    r.at = "FIRE_FAR"
    more += [r.go("N_CATCH", turn_by=0.7), *south_tail(r, " (B)")]
    r.at = "N_SHOOT"
    r.add(r.wait("TIP 2?", when=["Tip"], ms=1000, yes=tipped, no=more, yes_label="Yes", no_label="No: the far FLOWER"))
    return r


def shoots_right(name="qual-partner-shoots-right"):
    """Partner starts right, in front of the right CELL, and fires its 4 at once: TIP 1 (with the 3
    NECTAR). We start left. TIP 2: our 4 preloads and the far FLOWER's 4, both fired from beside the
    FLOWER; TIP 3: as qual-partner-shoots-left (TIP 2's spill caught going south, the GARDEN, the wall
    FLOWER), with TIP 1's spill, the partner's 4 and the NECTAR, lying about our end as well."""
    r = Route(name, N_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("FIRE_GARDEN", *FIRE_GARDEN).pt("FIRE_WALL", *FIRE_WALL).pt("FIRE_FAR", *FIRE_FAR)
    # Beside the far FLOWER while the partner makes TIP 1, spun up; fire our 4 when the left CELL rises.
    r.pt("FAR_START", 57.5, 119.3, 270)
    r.add(r.action("SpinUp"), r.go("FAR_START", heading=270), r.go("FIRE_FAR", turn_by=0.8),
          r.wait("TIP 1 (the partner)", when=["LeftCellUp"], ms=9000),
          fire(r, "Fire the preloads at the left CELL", "Empty", ms=2500))
    r.at = "FIRE_FAR"
    r.add(r.go("FAR_FLOWER", heading=90), r.wait("The far FLOWER", when=["IntakeFull"], ms=2300))
    r.at = "FAR_FLOWER"
    r.add(r.go("FIRE_FAR", heading=90), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500))
    r.at = "FIRE_FAR"
    r.add(r.go("N_CATCH", turn_by=0.7), *south_tail(r))
    return r


def parks(name="qual-partner-parks"):
    """Partner can't shoot: it leaves its 4 preloads in a row at its side (x 34.6, y 128.6-137) and
    parks. TIP 1: our 4 preloads. TIP 2: what we catch of TIP 1's spill (driving through it as it
    lands), then the partner's row, fired from below it; the far FLOWER if still short. TIP 3: as
    qual-partner-shoots-left."""
    r = Route(name, S_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("FIRE_GARDEN", *FIRE_GARDEN).pt("FIRE_WALL", *FIRE_WALL).pt("FIRE_FAR", *FIRE_FAR)
    r.pt("ROW_IN", 34.6, 114, 90).pt("ROW_BACK", 34.6, 116, 90)  # below the row, facing it; then where CollectSeen leaves us
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000), r.go("S_CATCH"),
          r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500),
          tunnel(r, "N_SHOOT"), fire(r, "Fire the catch at the left CELL", "Empty", ms=2200))
    r.at = "N_SHOOT"
    r.add(r.go("ROW_IN", turn_by=0.7), r.wait("The partner's row", when=["IntakeFull"], ms=2500, alongside="CollectSeen"))
    r.at = "ROW_IN"
    r.add(r.go("ROW_BACK", heading=90), fire(r, "Fire the row (TIP 2)", "Tip", ms=2500))
    r.at = "ROW_BACK"
    tipped = [r.go("N_CATCH", turn_by=0.7), *south_tail(r)]
    r.at = "ROW_BACK"
    more = [*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)]
    r.at = "FAR_FLOWER"
    more += [r.go("FIRE_FAR", heading=90), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500)]
    r.at = "FIRE_FAR"
    more += [r.go("N_CATCH", turn_by=0.7), *south_tail(r, " (B)")]
    r.at = "ROW_BACK"
    r.add(r.wait("TIP 2?", when=["Tip"], ms=300, yes=tipped, no=more, yes_label="Yes", no_label="No: the far FLOWER"))
    return r


QUALS = {  # our Auto, its partner (the qualifier partners), as AutoStudyTest specs
    "shoots-left": ("QualPartnerShootsLeftAuto", "PartnerPreloadsSideAuto"),
    "shoots-right": ("QualPartnerShootsRightAuto", "PartnerPreloadsRightAuto"),
    "parks": ("QualPartnerParksAuto", "PartnerLeaveParkAuto"),
}


def write_all():
    import partners
    partners.preloads_side().write()
    shoots_left().write()
    shoots_right().write()
    parks().write()


if __name__ == "__main__":
    write_all()
    which = sys.argv[2:] or list(QUALS)
    for f in ("1", "3"):
        study(";".join(f"{QUALS[w][0]},{QUALS[w][1]}@50" for w in which), runs=int(sys.argv[1]) if len(sys.argv) > 1 else 20,
              designs=D, extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                                    "BIOBUZZ_AUTO_FRICTION": f})
