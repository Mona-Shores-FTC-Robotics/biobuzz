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
