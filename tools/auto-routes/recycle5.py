"""Family A, "recycle" with a launcher that also throws straight back (mentor, 3 Oct 2026: slats that
flip it, instead of a turret). recycle4's right robot, whose routes already face its CELL; the left
robot uses the back throw where it would otherwise turn half round:
  - TIP 2: it picks up the far FLOWER facing north, away from its CELL, and throws back from right
    there (FLOWER_L_IN: 39 in, 16 deg off axis), then moves home to catch TIP 2's spill;
  - TIP 3 and TIP 4: it waits with its back to the CELL at BACK_W (37 in, 50 deg off axis; the CELL's
    opening faces its own end, so nothing nearer the wall FLOWER works), throws the catch back, takes
    the wall FLOWER with a 40 deg turn instead of a 140 deg one, and throws that back from BACK_W.
The robot turns whichever end is nearer the CELL; flipping the slats costs a guessed 0.3 s.
"""
import sys
from helpers import *
from recycle3 import right as right3

BOTH = "clump catapult 72 deg, triangle cup, full-width intake, shoots both ways"
# Both start 2 in further east than recycle3's (x 61: the side 0.5 in short of the centre line), the
# left robot 2 in further from the far FLOWER, the right one already where it catches.
R0, L0 = (61, 9.5, 90), (61, 132.25, 270)


def right(name="recycle5-right"):
    # recycle3's right robot (3 Oct 2026 films): recycle4's, which staged its catch against the wall,
    # catches nothing there now that the spill lands about 42 in out.
    return right3(name, R0)


def left(name="recycle5-left"):
    r = Route(name, L0, speed=50)
    flower_points(r, "FLOWER_L", FAR_FLOWER_AT, 90)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    # TIP 2: the preloads; the far FLOWER; back off it 6 in and throw it back from there.
    r.add(r.action("SpinUp"), *waits(r, "TIP 1", "LeftCellUp", 8.0),
          fire(r, "Fire the preloads", "Empty", ms=2000), *flower(r, "FLOWER_L", "The far FLOWER", ms=1800))
    r.at = "FLOWER_L"
    r.add(r.go("FLOWER_L_IN", heading=90), fire(r, "Throw the FLOWER back (TIP 2)", "Empty", ms=1500))
    # Turning to face the HIVE, to where TIP 2's spill lands (about 42 in out from our wall: 3 Oct 2026
    # films), and catch it (it lands about 1.5 s after the volley leaves); hold it for TIP 3 at BACK_W.
    # (Turn at MID, clear of the FLOWER and of the centre line; catch at x 57.5.)
    r.pt("MID", 50, 114, 270).pt("CATCH_L", *CATCH_L).pt("CATCH_L_BACK", CATCH_L[0], CATCH_L[1] + 3, CATCH_L[2])
    r.at = "FLOWER_L_IN"
    r.add(r.go("MID", turn_after=0.2, turn_by=0.9), r.go("CATCH_L", heading=270),
          r.wait("TIP 2: catch the spill", when=["IntakeFull"], ms=3000, alongside="CollectSeen"),
          r.go("CATCH_L_BACK", turn_after=0.4, turn_by=1.0))
    r.pt("BACK_W", 30, 108, 140)
    r.at = "CATCH_L_BACK"
    r.add(r.go("BACK_W", ctrl=[(57.5, 114), (42, 110)], turn_after=0.2, turn_by=0.8),
          *waits(r, "Our CELL up (3)", "LeftCellUp", 10.0), fire(r, "Throw the catch back (3)", "Empty", ms=600))
    # TIP 4: the wall FLOWER and back to BACK_W, throwing it back.
    r.at = "BACK_W"
    r.add(r.go("WALL_FLOWER_TURN", ctrl=[(24, 90)], turn_after=0.2, turn_by=0.7),
          r.go("WALL_FLOWER", heading=180), r.wait("The wall FLOWER", when=["IntakeFull"], ms=1800))
    r.at = "WALL_FLOWER"
    r.add(r.go("BACK_W", ctrl=[(24, 72)], turn_after=0.3, turn_by=0.8),
          fire(r, "Throw the wall FLOWER back (TIP 4)", "RightCellUp", ms=2500))
    # If it still hasn't tipped: what's left of TIP 2's spill, between home and the HIVE.
    r.pt("LOOK_L", 44, 112, 330)
    r.at = "BACK_W"
    more = [r.go("LOOK_L", turn_by=0.5),
            r.wait("Collect TIP 2's spill", when=["IntakeFull"], ms=2500, alongside="CollectSeen"),
            fire(r, "Fire the spill (TIP 4)", "RightCellUp", ms=2500)]
    r.add(r.wait("Tipped? (4)", when=["RightCellUp"], ms=300, yes=[], no=more, yes_label="Yes", no_label="No: the spill"))
    return r


if __name__ == "__main__":
    right().write()
    left().write()
    for f in ("1", "3"):
        study("Recycle5RightAuto,Recycle5LeftAuto@50", runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10, designs=BOTH,
              extra_env={"BIOBUZZ_AUTO_FRICTION": f, "BIOBUZZ_AUTO_PER_SEED": "1"})
