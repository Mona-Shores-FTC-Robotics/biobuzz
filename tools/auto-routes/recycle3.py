"""Family A, "recycle" (mentor, 2 Oct 2026): sister robots at their own ends, aiming at 5 TIPs with a
front intake and a catapult, no special equipment.

Pieces and time decide it. A TIP needs about 8 POLLEN-weights; a NECTAR weighs 1.65, so the right end,
which keeps the 3 NECTAR, needs about 6 pieces a TIP and the left end needs 8 POLLEN. The robot holds 4
(G407), so after TIP 1 each TIP is two volleys: one held ready for the moment our CELL rises, and one
fetched while it is up. TIPs alternate ends, so for 5 by 30 s each fetch must take about 5 s.
  - Catch a spill where you stand when you can (left alone, 2-3 of 8 roll over the centre line), and
    hold it for your CELL's next rise; otherwise sweep it off the wall, east, stopping as soon as full.
  - right: preloads (TIP 1), catching its spill (topped up along the wall) | fire it; the GARDEN, fire
    (TIP 3) | sweep TIP 1's and TIP 3's spills until full, wait aimed; fire, sweep on east, fire (TIP 5)
  - left: preloads + the far FLOWER (TIP 2), catching its spill | wait west of the HIVE (2.5 s nearer
    the wall FLOWER than home, and the catapult is as good 50 deg off axis); fire; the wall FLOWER,
    fire (TIP 4). TIP 2's spill alone comes 1-2 pieces short of TIP 4, so the 7 s trip is unavoidable.
No park: a 5th TIP (20) is worth more than PARK (5), and there's no time for both.
"""
import sys
from helpers import *

R0, L0 = (59, 9.5, 90), (59, 132.25, 270)
CAT = "clump catapult 72 deg, triangle cup, full-width intake"


def right(name="recycle3-right"):
    r = Route(name, R0, speed=50)
    aim = (58, 56.85)

    def aimed(x, y):  # heading from (x, y) to the right CELL's aim point
        import math
        return round(math.degrees(math.atan2(aim[1] - y, aim[0] - x)), 1)

    r.pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270)
    # WAIT: beside home, already aimed (45 in, 24 deg off axis). Spills rest on home itself (x 44-68,
    # y 1-8, against the wall); driving onto them bulldozes them over the centre line.
    r.pt("WAIT", 40, 16, 66)
    # The sweep: east along the wall, intake first, centre y 10 (the 18 in intake reaches y 1-19),
    # squared up at x 30, west of the pieces (turning into them bats them over the centre line).
    # The last stop goes straight in to x 59.5 (the frame reaches 68.5, short of the centre line at
    # 70.75), but nothing turns east of x 57.5 (a turning robot's corners reach 12.7 in).
    stops = (37, 41.5, 46, 50.5, 55, 59.5)
    r.pt("SWEEP_0", 30, 10, 0)
    for n, x in enumerate(stops, 1):
        r.pt(f"SWEEP_{n}", x, 10, 0)
        # Where to wait once full: just back from the stop, off the wall, aimed at the CELL; the pieces
        # still ahead stay where they are.
        hx = min(x - 2, 55)
        r.pt(f"HOLD_{n}", hx, 14, aimed(hx, 14))

    def to(p, frm, **kw):
        r.at = frm
        return r.go(p, **kw)

    def sweep(label, n, then):
        """From the previous stop (or SWEEP_0), on to stop n and beyond; at the first stop where we're
        full, then(n) (which also gets n = 4 if we never fill)."""
        out = [r.go(f"SWEEP_{n}", heading=0)]
        r.at = f"SWEEP_{n}"
        if n == len(stops):
            return out + [r.wait(f"{label} ({n})", when=["IntakeFull"], ms=500), *then(n)]
        yes = then(n)
        r.at = f"SWEEP_{n}"
        no = sweep(label, n + 1, then)
        return out + [r.wait(f"{label} ({n})", when=["IntakeFull"], ms=350, yes=yes, no=no,
                             yes_label="Full", no_label="Not yet")]

    def hold(n):  # back off the stop, turning to aim (once back west of x 57.5)
        r.at = f"SWEEP_{n}"
        return r.go(f"HOLD_{n}", turn_after=0.5 if stops[n - 1] > 57.5 else 0.0, turn_by=1.0)

    def again(at):
        """Not tipped yet: whatever the webcam sees from here, and fire again, while time lasts."""
        out = []
        for k in (1, 2):
            r.at = at
            out.append(r.wait("Tipped? (5)", when=["LeftCellUp"], ms=50, yes=[], no=[
                r.wait(f"Pick up more (5.{k})", when=["IntakeFull"], ms=1500, alongside="CollectSeen"),
                fire(r, f"Fire once more (5.{k})", "LeftCellUp", ms=1200)], yes_label="Yes", no_label="No: more"))
        return out

    def tip5(m):  # full again (or out of stops): fire for TIP 5
        return [hold(m), fire(r, "Fire again (TIP 5)", "LeftCellUp", ms=1500), *again(f"HOLD_{m}")]

    def tip5_load(n):
        """Full after TIP 3's spill: wait aimed, fire when our CELL rises (TIP 4), then sweep on east
        from where we stopped; the pieces still there are the rest of the TIP 1 and TIP 3 spills."""
        out = [hold(n), *waits(r, "Our CELL up (5)", "RightCellUp", 14.0), fire(r, "Fire (5)", "Empty", ms=400)]
        if n == len(stops):
            r.at = f"HOLD_{n}"
            return out + [r.wait("Pick up the rest (5)", when=["IntakeFull"], ms=1800, alongside="CollectSeen"),
                          fire(r, "Fire again (TIP 5)", "LeftCellUp", ms=1200), *again(f"HOLD_{n}")]
        r.at = f"HOLD_{n}"
        out.append(r.go(f"SWEEP_{n}", turn_by=0.6))
        r.at = f"SWEEP_{n}"
        return out + sweep("Sweep the rest (5)", n + 1, tip5)

    # TIP 1: the preloads onto the 3 NECTAR; catch the spill where we stand, and hold it for TIP 2.
    r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000),
          r.wait("TIP 1?", when=["Tip"], ms=2500), r.wait("TIP 1: catch the spill", when=["IntakeFull"], ms=2000))
    # Then up to 40 in, 12 deg off axis (a clump with NECTAR in it bounces out from home, 47 in, and
    # from head-on at 40 in). If the catch isn't full (the spill sometimes spreads west), sweep the
    # wall first: there are 5 s to spare.
    r.pt("SHOOT_R", 50, 18, 82).pt("SWEEP_W", 18, 10, 0)

    def to_shoot(n):
        r.at = f"SWEEP_{n}"
        return [r.go("SHOOT_R", ctrl=[(min(stops[n - 1], 55), 20)], turn_after=0.3, turn_by=0.9)]

    r.at = "START"
    full = [r.go("SHOOT_R", turn_by=0.8)]
    r.at = "START"
    top_up = [r.go("SWEEP_W", ctrl=[(55, 26), (24, 26)], turn_after=0.3, turn_by=0.9)]
    r.at = "SWEEP_W"
    top_up += sweep("Top up along the wall (3)", 1, to_shoot)
    r.add(r.wait("Caught 4?", when=["IntakeFull"], ms=400, yes=full, no=top_up, yes_label="Yes", no_label="No: sweep"))
    r.at = "SHOOT_R"
    r.add(*waits(r, "Our CELL up (3)", "RightCellUp", 14.0), fire(r, "Fire the spill (3)", "Empty", ms=600))
    # TIP 3: the GARDEN, fired from WAIT. What's left of TIP 1's spill stays on the floor for TIP 5.
    r.add(to("GARDEN_IN", "SHOOT_R", ctrl=[(52, 26), (30, 24)], turn_after=0.2, turn_by=0.8), r.go("GARDEN"),
          r.wait("The GARDEN", when=["IntakeFull"], ms=2000),
          to("WAIT", "GARDEN", ctrl=[(24, 14)], turn_after=0.3, turn_by=0.9),
          fire(r, "Fire the GARDEN (TIP 3)", "Tip", ms=2500))
    # TIP 5: let TIP 3's spill land, then sweep it (and TIP 1's leftovers) along the wall.
    r.add(r.wait("TIP 3's spill lands", when=["IntakeFull"], ms=1000), to("SWEEP_0", "WAIT", turn_by=0.8))
    r.at = "SWEEP_0"
    r.add(*sweep("Sweep the spills (5)", 1, tip5_load))
    return r


def left(name="recycle3-left"):
    r = Route(name, L0, speed=50)
    r.pt("HOME", 59, 131.75, 270)
    flower_points(r, "FLOWER_L", FAR_FLOWER_AT, 90)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    # TIP 2: the preloads, then the far FLOWER beside home.
    r.add(r.action("SpinUp"), *waits(r, "TIP 1", "LeftCellUp", 8.0),
          fire(r, "Fire the preloads", "Empty", ms=2000),
          *flower(r, "FLOWER_L", "The far FLOWER", ms=1800), *leave_flower(r, "FLOWER_L", "HOME"),
          fire(r, "Fire the FLOWER (TIP 2)", "Tip", ms=2500))
    # Catch TIP 2's spill where we stand (left alone, about 3 of its 8 roll over the centre line), and
    # hold it until TIP 3 raises our CELL again, waiting west of the HIVE: 42 in, 50 deg off axis
    # (CatapultVolleyTest.byAngle: 4 of 4 to 55 deg at 38-44 in), 2.5 s nearer the wall FLOWER than home.
    r.pt("SHOOT_W", 26, 112, 320)
    r.add(r.wait("TIP 2: catch the spill", when=["IntakeFull"], ms=2500))
    r.at = "HOME"
    r.add(r.go("SHOOT_W", ctrl=[(57.5, 114), (40, 110)], turn_after=0.2, turn_by=0.8),
          *waits(r, "Our CELL up (3)", "LeftCellUp", 10.0), fire(r, "Fire the spill (3)", "Empty", ms=400))
    # TIP 4: the wall FLOWER, straight down the west side and back.
    r.at = "SHOOT_W"
    r.add(r.go("WALL_FLOWER_TURN", ctrl=[(22, 90)], turn_after=0.2, turn_by=0.7),
          r.go("WALL_FLOWER", heading=180), r.wait("The wall FLOWER", when=["IntakeFull"], ms=1800))
    r.at = "WALL_FLOWER"
    r.add(r.go("SHOOT_W", ctrl=[(24, 70)], turn_after=0.3, turn_by=0.8),
          fire(r, "Fire the wall FLOWER (TIP 4)", "RightCellUp", ms=2500))
    # If it still hasn't tipped: what's left of TIP 2's spill, between home and the HIVE.
    r.pt("LOOK_L", 44, 112, 330).pt("SHOOT_L", 42, 118, 296)
    more = [r.go("LOOK_L", turn_by=0.5),
            r.wait("Collect TIP 2's spill", when=["IntakeFull"], ms=2500, alongside="CollectSeen")]
    r.at = "LOOK_L"
    more += [r.go("SHOOT_L", turn_by=0.6), fire(r, "Fire the spill (TIP 4)", "RightCellUp", ms=2500)]
    r.at = "SHOOT_W"
    r.add(r.wait("Tipped? (4)", when=["RightCellUp"], ms=300, yes=[], no=more, yes_label="Yes", no_label="No: the spill"))
    return r


if __name__ == "__main__":
    right().write()
    left().write()
    for f in ("1", "3"):
        study("Recycle3RightAuto,Recycle3LeftAuto@50", runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10, designs=CAT,
              extra_env={"BIOBUZZ_AUTO_FRICTION": f})
