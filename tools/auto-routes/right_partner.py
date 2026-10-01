"""A partner that does one thing: starts at the right start, fires its 4 preloads at once (TIP 1,
with the 3 NECTAR already in the right CELL), then parks or just sits. Can we make 4 TIPs around it?

Ours, left-tunnel, starts left and keeps solo-tunnel's rules (fire everything, catch each spill,
shoot head-on, through the tunnel under the HIVE, each source once):
  TIP 2 (left): our 4 preloads into the CELL the partner raised, then the far FLOWER, fire.
  TIP 3 (right): catch the TIP 2 spill, tunnel right, fire; pick up what TIP 1 left there
    (CollectSeen), fire; the GARDEN if it still hasn't tipped.
  Park at the near end of the LOADING ZONE; the partner takes the far-left end.
Result (1 Oct 2026): no TIP 4. TIP 2 needs the far FLOWER (12-13 s), so TIP 3 lands at about 26 s.
If the left CELL hasn't risen by 6 s, the partner missed: tunnel right and make TIP 1 ourselves."""
import sys
from helpers import *
from partner_start import partner_right

L_HOME, R_HOME = (59, 131.75, 270), (57.5, 10, 90)
# The GARDEN rather than CollectSeen for TIP 3: the partner drove its park path through the TIP 1
# spill, and searching for leftovers cost 2.5 s and found little.
GARDEN_FIRST = True


def partner_right_sit(name="partner-preloads-right-sit"):
    """Fires its preloads from the right start and stays there, intake off."""
    r = Route(name, (59, 9.5, 90), speed=40, folder=PP_DIR + "/partners")
    r.add(r.action("SpinUp"), r.action("IntakeOff"), fire(r, "Fire the preloads", "Empty", ms=4500))
    return r


def left_tunnel(name="left-tunnel", speed=50):
    r = Route(name, (59, 132.25, 270), speed=speed)
    r.pt("L_HOME", *L_HOME).pt("R_HOME", *R_HOME)
    r.pt("L_EXIT", 56.5, 103, 90).pt("L_EXIT_BACK", 56.5, 103, 270).pt("R_EXIT_BACK", 57.5, 34, 270)
    r.pt("R_EXIT", 57.5, 34, 90)
    r.pt("R_BACK", 59, 11, 90).pt("L_BACK", 59, 131, 270)  # home again after CollectSeen (a path needs two ends)
    flower_points(r, "FLOWER_L", FAR_FLOWER_AT, 90)
    r.pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    r.pt("PARK", 15, 90, 90)  # the near end of the LOADING ZONE; the partner parks at the far-left end

    def catch(label):
        # The CELL has already gone over (a CellUp wait saw it): catch where we stand.
        return r.wait(f"{label}: catch the spill", when=["IntakeFull"], ms=1600)

    def to_right():
        return [r.go("L_EXIT_BACK", heading=270, turn_by=0.5), r.go("R_EXIT_BACK", heading=270),
                r.go("R_HOME", turn_by=0.5)]

    def to_left():
        return [r.go("R_EXIT", heading=90, turn_by=0.5), r.go("L_EXIT", heading=90), r.go("L_HOME", turn_by=0.4)]

    def park():  # from the left end (the fallback): down the middle and in below the partner
        r.at = "L_HOME"
        return r.go("PARK", ctrl=[(59, 104), (59, 104), (32, 110), (32, 110), (32, 86), (32, 86)], park=True,
                    turn_after=0.45, turn_by=0.9)  # turn once well west of the centre line

    # --- The plan: the partner made TIP 1.
    r.at = "START"
    plan = [fire(r, "Fire our preloads (TIP 2)", "Empty", ms=2500),
            *flower(r, "FLOWER_L", "Collect at the far FLOWER", ms=1800), *leave_flower(r, "FLOWER_L", "L_HOME"),
            fire(r, "Fire until it tips (TIP 2)", "RightCellUp", ms=3000),
            catch("TIP 2")]
    r.at = "L_HOME"
    plan += to_right() + [fire(r, "Fire at the right CELL", "Empty", ms=2000)]
    # Not tipped: what TIP 1 left at this end (the partner caught none of it), then the GARDEN once.
    r.at = "R_HOME"
    garden = [r.go("GARDEN_IN", ctrl=[(30, 22)]), r.go("GARDEN"),
              r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1500),
              r.go("R_BACK", ctrl=[(20, 18)]), fire(r, "Fire the GARDEN (TIP 3)", "LeftCellUp", ms=2500)]
    r.at = "R_HOME"
    gather = [r.wait("What TIP 1 left", when=["IntakeFull"], ms=2500, alongside="CollectSeen"),
              r.go("R_BACK", heading=90), fire(r, "Fire what we found (TIP 3)", "LeftCellUp", ms=2500)]
    r.at = "R_BACK"
    gather.append(r.wait("TIP 3 now?", when=["LeftCellUp"], ms=100, yes=[], no=garden,
                         yes_label="Yes", no_label="No: the GARDEN"))
    plan.append(r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=1000, yes=[], no=garden if GARDEN_FIRST else gather,
                       yes_label="Yes", no_label="No: the GARDEN" if GARDEN_FIRST else "No: pick up what TIP 1 left"))
    # No time for TIP 4 (TIP 3 lands at about 26 s): park from here, clear of the partner's park.
    r.at = "R_BACK"
    plan.append(r.go("PARK", ctrl=[(24, 24), (24, 85)], park=True))  # solo-tunnel's park

    # --- Fallback: the partner missed TIP 1. Ours, then TIP 2 at the left, then park.
    r.at = "START"
    fallback = [r.go("L_EXIT_BACK", heading=270), r.go("R_EXIT_BACK", heading=270), r.go("R_HOME", turn_by=0.5),
                fire(r, "Fire until it tips (our TIP 1)", "LeftCellUp", ms=3000), catch("TIP 1")]
    r.at = "R_HOME"
    fallback += to_left() + [fire(r, "Fire at the left CELL (fallback)", "Empty", ms=2000)]
    r.at = "L_HOME"
    fallback += [*flower(r, "FLOWER_L", "Collect at the far FLOWER (fallback)", ms=1800),
                 *leave_flower(r, "FLOWER_L", "L_HOME"),
                 fire(r, "Fire until it tips (TIP 2, fallback)", "RightCellUp", ms=2500), park()]

    r.at = "START"
    r.add(r.action("SpinUp"),
          r.wait("Left CELL up (the partner's TIP 1)?", when=["LeftCellUp"], ms=6000, yes=plan, no=fallback,
                 yes_label="Yes", no_label="No: the partner missed"))
    return r


if __name__ == "__main__":
    partner_right().write()
    partner_right_sit().write()
    left_tunnel().write()
    study("LeftTunnelAuto,PartnerPreloadsRightAuto@50;LeftTunnelAuto,PartnerPreloadsRightSitAuto@50;"
          "LeftTunnelAuto,PartnerLeaveRightAuto@50",
          runs=int(sys.argv[1]) if len(sys.argv) > 1 else 20,
          designs=sys.argv[2] if len(sys.argv) > 2 else "two spring hoods, 24 in catcher|clump catapult 72 deg, triangle cup",
          extra_env={"BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood"})


def flower_feed(name="flower-feed", back=False, speed=50):
    """Fire and feed (mentor idea, 1 Oct 2026): wait for the partner's TIP 1 already standing at the far
    FLOWER, then fire our 4 preloads while the intake pulls the FLOWER's 4 in behind them: one stop,
    never more than 4 held, TIP 2 from the pickup spot. Needs a launcher that aims at the CELL while
    the intake faces the FLOWER: a turret (back=False: intake at the front, on the FLOWER), or a
    launcher at the front and the intake at the back (back=True: the robot faces the CELL).
    Then as left-tunnel: catch the TIP 2 spill, tunnel right, the GARDEN for TIP 3, park."""
    r = Route(name, (59, 132.25, 270), speed=speed)
    r.pt("L_HOME", *L_HOME).pt("R_HOME", *R_HOME)
    r.pt("L_EXIT", 56.5, 103, 90).pt("L_EXIT_BACK", 56.5, 103, 270).pt("R_EXIT_BACK", 57.5, 34, 270)
    r.pt("R_EXIT", 57.5, 34, 90).pt("R_BACK", 59, 11, 90)
    g = 90 if back else 270  # into the GARDEN intake first
    r.pt("GARDEN_IN", 8.5, 22, g).pt("GARDEN", 8.5, 11, g)
    r.pt("PARK", 15, 90, 90)
    if back:
        # Back to the FLOWER, front (launcher) toward the CELL: no turn from the start.
        # Already aimed (mentor: "just line up a little angled to the FLOWER"): heading 281 points at the
        # raised left CELL, and the back of the robot is square to the FLOWER 11.2 in behind its centre,
        # so the launcher never has to turn it there (a turn on the spot would swing a corner into it).
        r.pt("FEED", 49.6, 127.8, 281)
        r.pt("FEED_IN", 59, 121, 281)  # turn here, clear of the FLOWER
        to_feed = [r.go("FEED_IN", turn_by=1.0), r.go("FEED", heading=281)]  # down first, then west under the FLOWER
        catch_at = "CATCH_B"
        r.pt(catch_at, 59, 121, 90)  # intake (at the back) toward the CELL, for the spill
    else:
        flower_points(r, "FEED", FAR_FLOWER_AT, 90)
        r.at = "START"
        to_feed = flower(r, "FEED", "At the FLOWER (already full)", ms=0)[:-1]
        catch_at = "CATCH"
        r.pt(catch_at, 59, 121, 270)  # intake toward the CELL, for the spill
    r.at = "START"
    plan = [*to_feed,
            r.wait("Left CELL up (the partner's TIP 1)?", when=["LeftCellUp"], ms=6000),
            # Fire and feed: the intake keeps taking the FLOWER's POLLEN as shots free up room. Stream:
            # LaunchAll would stop the moment the robot is empty, and it empties between pulls.
            r.action("StreamOn"),
            r.wait("Fire and feed (TIP 2)", when=["RightCellUp"], ms=6000),
            r.action("StreamOff")]
    r.at = "FEED"
    # Straight away from the FLOWER first, then turn once clear of it.
    plan += [r.go(catch_at, ctrl=[(47.36, 116)], turn_after=0.3, turn_by=0.8),
             r.wait("TIP 2: catch the spill", when=["IntakeFull"], ms=1600)]
    r.at = catch_at
    plan += [r.go("L_EXIT_BACK", heading=90 if back else 270), r.go("R_EXIT_BACK", heading=90 if back else 270),
             r.go("R_HOME", turn_by=0.5), fire(r, "Fire at the right CELL", "Empty", ms=2000)]
    r.at = "R_HOME"
    garden = [r.go("GARDEN_IN", ctrl=[(30, 22)]), r.go("GARDEN"),
              r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1500),
              r.go("R_BACK", ctrl=[(20, 18)]), fire(r, "Fire the GARDEN (TIP 3)", "LeftCellUp", ms=2500)]
    # First what TIP 1 left at this end (the partner leaves it), then the GARDEN if still not tipped.
    r.at = "R_HOME"
    gather = [r.wait("What TIP 1 left", when=["IntakeFull"], ms=2000, alongside="CollectSeen"),
              r.go("R_BACK", heading=90), fire(r, "Fire what we found (TIP 3)", "LeftCellUp", ms=2000)]
    r.at = "R_BACK"
    gather.append(r.wait("TIP 3 now?", when=["LeftCellUp"], ms=100, yes=[], no=garden,
                         yes_label="Yes", no_label="No: the GARDEN"))
    r.at = "R_HOME"
    # The GARDEN first: picking up what TIP 1 left first made TIP 3 later (28 s, not 24-25 s).
    plan.append(r.wait("TIP 3 yet?", when=["LeftCellUp"], ms=1000, yes=[], no=garden if GARDEN_FIRST else gather,
                       yes_label="Yes", no_label="No: the GARDEN" if GARDEN_FIRST else "No: what TIP 1 left"))
    r.at = "R_HOME"
    plan.append(r.go("PARK", ctrl=[(24, 24), (24, 85)], park=True))
    r.add(r.action("SpinUp"), *plan)
    return r
