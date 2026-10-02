"""Family A, "recycle" with staging (mentor, 2 Oct 2026): recycle3, but a robot with a long wait ahead
of it sets the 4 it caught down on the tiles in front of where it catches, fetches 4 more meanwhile, and
then fires twice in quick succession when its CELL rises: what it carried, then (after driving a few
inches onto the row) what it staged. A robot only ever holds 4 (G407); a staged row is free storage.

Staging pays only when the wait is longer than the fetch:
  - right, TIP 1 -> TIP 2 (5.6 s; the GARDEN takes about 5): stage the catch, fetch the GARDEN.
  - right, TIP 3 -> TIP 4 (about 6 s): stage the catch against the wall, where the webcam's
    CollectSeen won't take it, collect TIP 3's other leftovers, and sweep the row up after TIP 4.
  - left, TIP 2 -> TIP 3 (about 1.5 s once it is free; the wall FLOWER takes 7): hold, as recycle3.
Setting down takes the reversed intake 0.25 s a piece, and the pieces roll 1.5 in (guesses: film one).
"""
import math
import sys
from helpers import *
from recycle3 import R0, CAT

AIM = (58, 56.85)


def aimed(x, y):  # heading from (x, y) to the right CELL's aim point
    return round(math.degrees(math.atan2(AIM[1] - y, AIM[0] - x)), 1)


def ahead(x, y, d):  # d in further along the line to the aim point
    h = math.atan2(AIM[1] - y, AIM[0] - x)
    return round(x + d * math.cos(h), 1), round(y + d * math.sin(h), 1)


def right(name="recycle4-right"):
    r = Route(name, R0, speed=50)
    r.pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    # One spot does everything: CATCH, home slid east until the robot's side (x 70.6, aimed 4 deg off
    # square) stops pieces rolling over the centre line. From it the robot catches a spill, sets the
    # catch down in a row just in front (about y 21), fires what it carries (47 in), rolls 6 in onto
    # the row, fires that (41 in), and backs straight off to catch the next spill. Nothing turns east
    # of x 57.5 (a turning robot's corners reach 12.7 in).
    cx, cy = 61, 10.5  # the back 1.5 in off the wall: touching it at the end costs LEAVE
    r.pt("CATCH", cx, cy, aimed(cx, cy))
    # Back to CATCH from wherever CollectSeen left us: via a point beside it (a path needs two ends).
    r.pt("CATCH_A", cx - 2, cy + 1, aimed(cx - 2, cy + 1))

    def to_catch():
        r.at = "CATCH_A"
        return r.go("CATCH", heading=aimed(cx, cy))
    r.pt("ONTO", *ahead(cx, cy, 3), aimed(*ahead(cx, cy, 3)))
    r.pt("COLLECT", *ahead(cx, cy, 6), aimed(*ahead(cx, cy, 6)))
    # The wall sweep: east along the wall, intake first, centre y 10 (the 18 in intake reaches y 1-19),
    # squared up at SWEEP_0, reached from above and west of where pieces lie (driving along the wall
    # to it pushes them out of the sweep's way, and turning into them bats them over the centre line).
    # The last stop goes straight in to x 59.5 (the frame reaches 68.5).
    stops = (30, 36, 42, 48, 54, 59.5)
    r.pt("SWEEP_0", 20, 10, 0)
    for n, x in enumerate(stops, 1):
        r.pt(f"SWEEP_{n}", x, 10, 0)

    def sweep(label, n, then):
        """From the previous stop (or SWEEP_0), on to stop n and beyond; at the first stop where we're
        full, then(n) (also called after the last stop if we never fill)."""
        out = [r.go(f"SWEEP_{n}", heading=0)]
        r.at = f"SWEEP_{n}"
        if n == len(stops):
            return out + [r.wait(f"{label} ({n})", when=["IntakeFull"], ms=500), *then(n)]
        yes = then(n)
        r.at = f"SWEEP_{n}"
        no = sweep(label, n + 1, then)
        return out + [r.wait(f"{label} ({n})", when=["IntakeFull"], ms=350, yes=yes, no=no,
                             yes_label="Full", no_label="Not yet")]

    def back_to_catch(n):
        """From stop n: back west off the pieces, turn there, and slide east along the wall to CATCH,
        the front edge passing just under the staged row."""
        r.at = f"SWEEP_{n}"
        return [r.go("CATCH", ctrl=[(42, 10), (48, 10)], turn_after=0.15, turn_by=0.55)]

    def set_down(label):
        return r.wait(label, when=["Empty"], ms=1500, alongside="SetDown")

    def twice(label, until):
        """Our CELL is up: fire what we carry from CATCH, drive onto the staged row (in two hops: in
        one go its end pieces get shoved aside), fire that, and back off to CATCH for the spill."""
        r.at = "CATCH"
        out = [fire(r, f"Fire what we carry ({label})", "Empty", ms=600), r.action("IntakeOn"),
               r.go("ONTO", heading=r.points["ONTO"][2]), r.wait(f"Onto the row ({label})", when=["IntakeFull"], ms=500)]
        r.at = "ONTO"
        out += [r.go("COLLECT", heading=r.points["COLLECT"][2]),
                r.wait(f"Pick up the row ({label})", when=["IntakeFull"], ms=700),
                fire(r, f"Fire the row ({label})", until, ms=2500)]
        r.at = "COLLECT"
        return out + [r.go("CATCH", heading=aimed(cx, cy))]

    def again(at):
        """Not tipped yet: whatever the webcam sees from here, and fire again, while time lasts."""
        out = []
        for k in (1, 2):
            r.at = at
            out.append(r.wait("Tipped? (5)", when=["LeftCellUp"], ms=50, yes=[], no=[
                r.wait(f"Pick up more (5.{k})", when=["IntakeFull"], ms=1500, alongside="CollectSeen"),
                fire(r, f"Fire once more (5.{k})", "LeftCellUp", ms=1200)], yes_label="Yes", no_label="No: more"))
        return out

    # TIP 1: the preloads onto the 3 NECTAR; slide to CATCH and catch the spill (it trickles in: full
    # by 4-5 s, or not at all).
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000), r.go("CATCH", heading=aimed(cx, cy)),
          r.wait("TIP 1?", when=["Tip"], ms=2500), r.wait("TIP 1: catch the spill", when=["IntakeFull"], ms=2600))
    # Not full? The rest may have stopped short, west of us or in front (on slow tiles it does):
    # what the webcam sees from LOOK, then back to CATCH.
    r.pt("LOOK", 54, 17, 180)  # off the wall (CollectSeen can't turn by it to look), facing west
    r.at = "CATCH"
    more = [r.go("LOOK", turn_after=0.55, turn_by=0.95),
            r.wait("Pick up the rest (3)", when=["IntakeFull"], ms=1800, alongside="CollectSeen")]
    r.at = "LOOK"
    more += [r.go("CATCH_A", turn_after=0.2, turn_by=0.8), to_catch()]
    r.at = "CATCH"
    r.add(r.wait("Caught 4?", when=["IntakeFull"], ms=50, yes=[], no=more, yes_label="Yes", no_label="No: look"))
    # Stage it in front, back off west square to the wall, and fetch the GARDEN while TIP 2 comes.
    r.at = "CATCH"
    r.add(set_down("Stage the catch (3)"),
          # Turning only once west of x 40: a turning robot's corners reach 12.7 in, and the row is in front.
          r.go("GARDEN_IN", ctrl=[(40, 10), (26, 16)], turn_after=0.45, turn_by=0.85), r.action("IntakeOn"))
    r.at = "GARDEN_IN"
    r.add(r.go("GARDEN"), r.wait("The GARDEN", when=["IntakeFull"], ms=2000))
    r.at = "GARDEN"
    r.add(r.go("CATCH", ctrl=[(24, 10), (44, 10)], turn_after=0.15, turn_by=0.5),
          *waits(r, "Our CELL up (3)", "RightCellUp", 14.0), *twice("TIP 3", "Tip"))
    # Not tipped (a poor catch at TIP 1: what it missed is mid-field, or over the centre line)? What
    # the webcam sees from here (it aims and fires from where it ends up); then the wall.
    r.at = "CATCH"
    wall = [r.go("SWEEP_0", ctrl=[(48, 26), (24, 30)], turn_after=0.3, turn_by=0.8)]
    r.at = "SWEEP_0"
    wall += sweep("Sweep for more (3)", 1, lambda n: back_to_catch(n) + [
        fire(r, "Fire again (TIP 3)", "Tip", ms=2500)])
    r.at = "CATCH"
    seen = [r.wait("Pick up what we see (3)", when=["IntakeFull"], ms=2500, alongside="CollectSeen"),
            fire(r, "Fire what we found (TIP 3)", "Tip", ms=2000),
            r.wait("Tipped now? (3)", when=["LeftCellUp"], ms=800, yes=[], no=wall, yes_label="Yes", no_label="No: the wall")]
    r.add(r.wait("Tipped? (3)", when=["LeftCellUp"], ms=800, yes=[], no=seen, yes_label="Yes", no_label="No: look"))
    # TIP 5. The wait for TIP 4 is long (about 6 s), so stage again: catch TIP 3's spill, set it down
    # against the wall (facing it, the row lies along the wall, where CollectSeen won't take it: it
    # stays off walls to keep LEAVE), and collect TIP 3's other leftovers mid-field with the webcam
    # from LOOK5. Wait at HOLD5 (52 in, 41 deg off axis), next to the wall sweep's start. When TIP 4
    # comes: fire, sweep the row (and anything else on the wall) east until full, fire from SHOOT5.
    r.pt("WALL_STAGE", 40, 13.6, 270).pt("LOOK5", 40, 26, 90)
    r.pt("HOLD5", 24, 18, aimed(24, 18)).pt("SHOOT5", 50, 17, aimed(50, 17))
    r.at = "CATCH"
    r.add(r.wait("TIP 3: catch the spill", when=["IntakeFull"], ms=2500),
          r.go("WALL_STAGE", ctrl=[(50, 16)], turn_after=0.35, turn_by=0.9), set_down("Stage the catch (5)"))
    r.at = "WALL_STAGE"
    r.add(r.go("LOOK5", turn_after=0.3, turn_by=0.9), r.action("IntakeOn"),
          r.wait("Pick up the leftovers (5)", when=["IntakeFull"], ms=3500, alongside="CollectSeen"))
    r.at = "LOOK5"
    r.add(r.go("HOLD5", turn_by=0.8),
          *waits(r, "Our CELL up (5)", "RightCellUp", 14.0), fire(r, "Fire what we carry (TIP 5)", "Empty", ms=600))
    r.at = "HOLD5"
    r.add(r.go("SWEEP_0", turn_by=0.7))

    def tip5(n):
        r.at = f"SWEEP_{n}"
        return [r.go("SHOOT5", ctrl=[(min(stops[n - 1], 50), 13)], turn_after=0.2, turn_by=0.8),
                fire(r, "Fire again (TIP 5)", "LeftCellUp", ms=1500), *again("SHOOT5")]

    r.at = "SWEEP_0"
    r.add(*sweep("Sweep the wall (5)", 1, tip5))
    return r


def left(name="recycle4-left"):
    """recycle3's left: it holds TIP 2's catch. Staging it instead (set down west of the HIVE, fetch the
    wall FLOWER meanwhile) measured worse, TIP 4 at 26.5 s against 24.4 s over 20 runs: TIP 3 now comes
    about 1.5 s after the left is free to go, less than setting down and picking up again cost."""
    from recycle3 import left as left3
    return left3(name)


if __name__ == "__main__":
    right().write()
    left().write()
    for f in ("1", "3"):
        study("Recycle4RightAuto,Recycle4LeftAuto@50", runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10, designs=CAT,
              extra_env={"BIOBUZZ_AUTO_FRICTION": f, "BIOBUZZ_AUTO_PER_SEED": "1"})
