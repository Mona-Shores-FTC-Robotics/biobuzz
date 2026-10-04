"""Autos for the robot being built (4 Oct 2026): one two-wheel flywheel launcher fixed to the frame,
POLLEN and NECTAR, an 18 in front intake. Its numbers are all guesses until the build team measures
them (AutoStudyTest's "two-wheel launcher" designs sweep spin-up and shot interval).

What one launcher changes (study, 4 Oct 2026): a volley of 4 takes 3 shot intervals, so the time
between shots, not spin-up, decides how many TIPs fit. Spin-up happens once, at the start (the
launcher keeps spinning after that), and anywhere from 1 to 3 s moves the score by a point at most.
So these Autos fire from where they already are or stand next to (ShotMapTest: the 75 deg hood
scores from 13-29 in off the CELL's wall, x 13-61), fire until empty rather than wait for a CELL to
settle, and use the sources at our end of the field for TIP 3.
"""
import sys
from helpers import *

R_HOME, L_HOME = (57.5, 10, 90), (59, 131.75, 270)


def fire_until(r, label, conds, ms):
    """LaunchAll until any of `conds` (one row each: the first that comes true ends it) or `ms`."""
    for c in conds:
        if c not in r.conds: r.conds.append(c)
    if "LaunchAll" not in r.actions: r.actions.append("LaunchAll")
    rows = [{"when": [c], "cards": []} for c in conds] + [{"afterMs": int(ms), "cards": []}]
    return {"id": r._id("w"), "kind": "firstOf", "label": label, "rows": rows, "alongside": "LaunchAll"}


def catch_still(r, label, spot, still_ms=1800, seen_ms=1500, fire_ms=1800):
    """As helpers.catch_spill, but stand still facing the HIVE while the spill lands (CollectSeen's
    look-around would turn the intake away from it), then pick up what lies near with the webcam."""
    x, y, h = r.points[spot]
    back = spot + "_BACK"
    if back not in r.points:
        r.pt(back, x, y - 3 if y < 70.75 else y + 3, h)
    out = [r.go(spot, turn_by=0.8), r.wait(label + ": it lands", when=["IntakeFull"], ms=still_ms)]
    if seen_ms:
        out.append(r.wait(label + ": what lies near", when=["IntakeFull"], ms=seen_ms, alongside="CollectSeen"))
    r.at = spot
    return out + [r.go(back, turn_after=0.4, turn_by=1.0)]


def west_tip3(r, fire_ms, shot_w, shot_g):
    """TIP 3 from the two POLLEN sources at our end, without the tunnel: from the left home down the
    west side (wide of a partner parked at the far-left end of the LOADING ZONE and of the HIVE
    frame's foot bar) to the wall FLOWER, fire from beside it, then the GARDEN, fire from beside it."""
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("SHOT_W", *shot_w).pt("SHOT_G", *shot_g)
    r.at = "L_HOME"
    # Square to the field to (30, 100), then straight down x 30: a turning robot's corners reach
    # 12.7 in, so at x 30 they clear the foot bar (x 45-47, y 51-90) and, below y 98, the partner.
    r.pt("W_MID", 30, 100, 270)
    out = [r.go("W_MID", ctrl=[(57.5, 112), (40, 100)], heading=270),
           r.go("WALL_FLOWER_TURN", ctrl=[(30, 62)], turn_after=0.2, turn_by=0.9),
           r.go("WALL_FLOWER", heading=180),
           r.wait("Collect at the wall FLOWER", when=["IntakeFull"], ms=2300),
           r.go("SHOT_W", turn_after=0.3, turn_by=0.9), fire(r, "Fire the wall FLOWER (TIP 3)", "Empty", ms=fire_ms)]
    r.at = "SHOT_W"
    tipped = [r.go("SHOT_G")]  # before the GARDEN's paths, so no path in the file joins SHOT_G to itself
    r.at = "SHOT_W"
    garden = [r.go("GARDEN_IN", turn_after=0.2, turn_by=0.7), r.go("GARDEN"),
              r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1500),
              r.go("SHOT_G", turn_after=0.3, turn_by=0.9), fire(r, "Fire the GARDEN (TIP 3)", "LeftCellUp", ms=fire_ms)]
    r.at = "SHOT_W"
    out.append(r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=100, yes=tipped, no=garden,
                      yes_label="Yes", no_label="No: the GARDEN"))
    r.at = "SHOT_G"
    out.append(r.go("PARK", ctrl=[(24, 60)], park=True))
    return out


def solo_catch(name="solo-west", speed=50, still_ms=1800, seen_ms=1500, fire_ms=1800, shot_a=(34, 20, 60), shot_g=(22, 22, 50), tip_ms=2500, tip3="west", shot_w=(22, 31, 60), folder=None):
    """solo-west (tip3="west", the default): solo-tunnel's plan for a partner that fires its preloads,
    reworked for one launcher (4 Oct 2026). 73 points over 40 runs at 0.25 s/shot (solo-tunnel: 69).

    TIP 1: fire the 4 preloads from the start; catch the TIP 1 spill where the 3 Oct films put it
      (CATCH_R), standing still while it lands, then the webcam for what lies near.
    TIP 2: through the tunnel, fire it at the left CELL (the partner's preloads are already in it)
      until the CELL starts to go over (Tip). If it doesn't by tip_ms the catch came up short: top up
      at the far FLOWER, fire again and park from the left end.
    TIP 3: down the west side to the wall FLOWER, fire from beside it, then the GARDEN, fire from
      beside it (8 POLLEN, about a TIP's worth; no trip back through the tunnel). Park.

    tip3="tunnel" is solo-catch (experiments): catch the TIP 2 spill at CATCH_L, back through the
    tunnel, fire it on the way to the GARDEN, then the GARDEN. TIP 3 came at about 27 s, too late
    in a quarter of the runs (70.5 points)."""
    r = Route(name, (59, 9.5, 90), speed=speed, folder=folder)
    r.pt("L_HOME", *L_HOME)
    r.pt("CATCH_R", *CATCH_R).pt("CATCH_L", *CATCH_L)
    r.pt("L_EXIT", 56.5, 103, 90).pt("L_EXIT_BACK", 56.5, 103, 270).pt("R_EXIT_BACK", 57.5, 34, 270)
    r.pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    r.pt("PARK", 15, 90, 90)

    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4500),
          *catch_still(r, "TIP 1 spill", "CATCH_R", still_ms, seen_ms))
    r.add(r.go("L_EXIT", heading=90), r.go("L_HOME", turn_by=0.4))
    # Fire until the left CELL starts to go over (Tip: the swing has started; the spill lands about
    # 1.15 s later, time to reach CATCH_L). Not by tip_ms: the TIP 1 catch came up short, so top up
    # at the far FLOWER, fire again and park from this end; TIP 3 is out of reach.
    r.at = "L_HOME"
    if tip3 == "west":
        yes = west_tip3(r, fire_ms, shot_w, shot_g)
    else:
        yes = catch_still(r, "TIP 2 spill", "CATCH_L", still_ms, seen_ms)
        # TIP 3 needs two loads (about 8 POLLEN's weight). The first is the TIP 2 spill; fire it on the
        # way to the GARDEN, from a spot the 75 deg hood scores from (ShotMapTest: y 13-29, x 13-61), and
        # fire the GARDEN's 4 from just outside it: no drive back to the right home.
        r.pt("SHOT_A", *shot_a).pt("SHOT_G", *shot_g)
        yes += [r.go("L_EXIT_BACK", heading=270), r.go("R_EXIT_BACK", heading=270),
                r.go("SHOT_A", turn_after=0.3, turn_by=0.9), fire(r, "Fire the TIP 2 spill (TIP 3)", "Empty", ms=fire_ms)]
        r.at = "SHOT_A"
        tipped = [r.go("SHOT_G")]  # so the park path starts where the robot is either way
        r.at = "SHOT_A"
        garden = [r.go("GARDEN_IN", turn_by=0.6), r.go("GARDEN"),
                  r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1500),
                  r.go("SHOT_G", turn_after=0.3, turn_by=0.9), fire(r, "Fire the GARDEN (TIP 3)", "LeftCellUp", ms=fire_ms)]
        yes.append(r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=100, yes=tipped, no=garden, yes_label="Yes", no_label="No: the GARDEN"))
        r.at = "SHOT_G"
        yes.append(r.go("PARK", ctrl=[(20, 60)], park=True))


    r.at = "L_HOME"
    flower_points(r, "FLOWER_L", FAR_FLOWER_AT, 90)
    no = [*flower(r, "FLOWER_L", "Top up at the far FLOWER", ms=1800), *leave_flower(r, "FLOWER_L", "L_HOME"),
          fire(r, "Fire again (TIP 2)", "RightCellUp", ms=2500)]
    r.at = "L_HOME"
    r.pt("PARK_L", 15, 89, 270)
    # solo-tunnel's park from the left end, below a partner parked at the far-left end.
    no.append(r.go("PARK_L", ctrl=[(59, 104), (59, 104), (32, 110), (32, 110), (32, 86), (32, 86)], heading=270, park=True))
    r.add(r.wait("Fire at the left CELL (TIP 2)", when=["Tip"], ms=tip_ms, alongside="LaunchAll",
                 yes=yes, no=no, yes_label="It's going over", no_label="Not yet: top up"))
    return r


def garden_first(name="garden-first", speed=50, left_shot=(40, 116, 301), shot=(57.5, 20, 90),
                 sweep=((57.5, 30), (57.5, 38)), sweep_ms=(500, 900), fire_ms=1800,
                 west=((24, 30), (24, 92)), wall_first=False, shot_w=(22, 31, 60), settle_ms=1500, folder=EXPERIMENTS):
    """Leave the TIP 1 spill on the tiles for TIP 3: uncaught, its 3 NECTAR and 4 POLLEN (about
    224 g, more than a TIP needs) lie together in front of the right CELL, x 48-66, 25-48 in out.

    TIP 1: fire the 4 preloads from the start.
    TIP 2: the GARDEN's 4 POLLEN (nearer the start than the wall FLOWER, and quicker to take), up the
      west side, fire at the left CELL on top of the partner's preloads.
    TIP 3: back to the right end and drive up the middle of the spill intake first, in short steps
      (the intake takes a piece per 0.35 s; the webcam pickup pushed the NECTAR aside), back off
      and fire from behind it; again for the rest. Park."""
    r = Route(name, (59, 9.5, 90), speed=speed, folder=folder)
    r.pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    r.pt("LEFT_SHOT", *left_shot).pt("SHOT", *shot).pt("PARK", 15, 89.5, 90)
    for i, (x, y) in enumerate(sweep):
        r.pt(f"SWEEP_{i + 1}", x, y, 90)
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4500),
          r.go("GARDEN_IN", ctrl=[(30, 22)], turn_after=0.2, turn_by=0.8), r.go("GARDEN"),
          r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1500),
          # Turn only once well past the HIVE frame's foot bar (x 46, to y 90.2).
          r.go("LEFT_SHOT", ctrl=list(west), turn_after=0.3, turn_by=0.95),
          fire(r, "Fire at the left CELL (TIP 2)", "Empty", ms=2500))
    r.at = "LEFT_SHOT"
    if wall_first:
        # The swept spill is about 130 g (some of it ends against the centre line or under the lip), and
        # a TIP takes about 200: top up first with the wall FLOWER, on the way down the west side.
        flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
        r.pt("SHOT_W", *shot_w)
        r.add(r.go("WALL_FLOWER_TURN", ctrl=[(30, 95), (30, 62)], turn_after=0.3, turn_by=0.9),
              r.go("WALL_FLOWER", heading=180),
              r.wait("Collect at the wall FLOWER", when=["IntakeFull"], ms=2300),
              r.go("SHOT_W", turn_after=0.3, turn_by=0.9), fire(r, "Fire the wall FLOWER (TIP 3)", "Empty", ms=fire_ms),
              r.go("SHOT", ctrl=[(40, 16)], turn_by=0.6))
    else:
        r.add(r.go("SHOT", ctrl=[(34, 92), (12, 60), (22, 18)], turn_by=0.8))

    def sweep_spill(label):
        out = []
        for i, ms in enumerate(sweep_ms):
            out += [r.go(f"SWEEP_{i + 1}", heading=90), r.wait(f"{label} ({i + 1})", when=["IntakeFull"], ms=ms)]
        r.at = f"SWEEP_{len(sweep_ms)}"
        return out + [r.go("SHOT", heading=90)]
    r.add(*sweep_spill("Sweep up the TIP 1 spill"), fire(r, "Fire the spill (TIP 3)", "Empty", ms=fire_ms))
    r.at = "SHOT"
    more = sweep_spill("Sweep up the rest") + [fire(r, "Fire the rest (TIP 3)", "LeftCellUp", ms=fire_ms + 700)]
    r.at = "SHOT"
    r.add(r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=settle_ms, yes=[], no=more, yes_label="Yes", no_label="No: the rest of the spill"))
    r.at = "SHOT"
    r.add(r.go("PARK", ctrl=[(30, 30), (22, 80)], park=True))
    return r

if __name__ == "__main__":
    solo_catch().write()  # solo-west
    if "experiments" in sys.argv:  # the ideas that lost (tools/auto-routes/experiments/README.md)
        solo_catch("solo-catch", tip3="tunnel", folder=EXPERIMENTS).write()
        garden_first().write()
        garden_first("garden-wall", wall_first=True).write()
    study("SoloWestAuto,PartnerPreloadsParkAuto@50;ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50", runs=20,
          designs="two-wheel launcher|two-wheel launcher, 0.45 s/shot",
          extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})
