"""Solo feasibility probe (the CAD chat's redesign brief, 9 Oct 2026): can one robot make 4 TIPs in AUTO with a
partner that only parks, if it fires as it collects whole spills (a flow-through intake: StreamOn while CollectSeen
sweeps the landed spill, never holding more than 4)?

    TIP 1   our 4 preloads at the right CELL from the right start.
    TIP 2   wait for TIP 1's spill to land at the right end, then sweep it with the stream on at the left CELL
            (raised after TIP 1): every piece collected is fired as it arrives.
    TIP 3   TIP 2's spill lands at the left end: drive there, sweep it, streaming at the right CELL.
    TIP 4   TIP 3's spill at the right end: back, sweep, stream at the left CELL.
    No PARK: 4 TIPs are worth more than the drive to the LOADING ZONE.

A probe, not a route to run: it measures what collecting rate, launch range and drive speed a 4th TIP would take
(BIOBUZZ_AUTO_DESIGN_SET). Written by the simulator chat; the body-designs chat owns real routes.

    python3 flow_solo.py      writes the variants into experiments/
"""
import autogen
from autogen import Route
from helpers import fire

S_START = (59, 8.06, 90)
# Sweep spots: short of where a spill lands (its first touch about 40 in out from the end wall, then it rolls toward
# the wall), facing the HIVE so the pieces roll toward the robot and the webcam sees them; x 55 so a turn there keeps
# the V's corners off the centre line and lets it turn on the spot to look around (x 50). Clear of the landing (G409): the front at y 29.
SWEEP_R, SWEEP_L = (50, 20, 90), (50, 121.5, 270)
# Between the ends up the west side at x 22, clear of the HIVE frame's west foot at x 46 (its bar runs y 39-103 and a
# turning V reaches 12.7 in): out to the west wall first, then straight north or south.
W_S, W_N = (22, 22, 90), (30, 112, 90)  # W_N east of a partner parked at (10.5, 118)


def sweep(r, label, passes=3, ms=3000):
    """Chase the landed spill with the stream on until the HIVE tips: CollectSeen ends when it sees nothing (pieces
    still in the air, or none in view), so it is restarted up to `passes` times, each pass `ms` long."""
    card = None
    for k in range(passes, 0, -1):
        card = r.wait("%s, pass %d" % (label, k), when=["Tip"], ms=ms, alongside="CollectSeen", no=[card] if card else None)
    return card


def flow_solo(name, land_ms=1800, sweep_ms=3000, speed=50):
    r = Route(name, S_START, speed=speed)
    r.pt("SWEEP_R", *SWEEP_R).pt("SWEEP_L", *SWEEP_L).pt("W_S", *W_S).pt("W_N", *W_N)
    r.add(r.action("SpinUp"), fire(r, "Fire the preloads (TIP 1)", "Empty", ms=3000),
          r.go("SWEEP_R"), r.wait("TIP 1", when=["Tip"], ms=4000),
          r.wait("TIP 1's spill lands", when=["IntakeFull"], ms=land_ms),
          r.action("IntakeOn"), r.action("StreamOn"),
          sweep(r, "Sweep TIP 1's spill, streaming at the left CELL (TIP 2)", ms=sweep_ms),
          r.action("StreamOff"))
    r.at = "SWEEP_R"
    r.add(r.go("W_S"), r.go("W_N"), r.go("SWEEP_L"),
          r.wait("TIP 2's spill lands", when=["IntakeFull"], ms=land_ms),
          r.action("StreamOn"),
          sweep(r, "Sweep TIP 2's spill, streaming at the right CELL (TIP 3)", ms=sweep_ms),
          r.action("StreamOff"))
    r.at = "SWEEP_L"
    r.add(r.go("W_N"), r.go("W_S"), r.go("SWEEP_R"),
          r.wait("TIP 3's spill lands", when=["IntakeFull"], ms=land_ms),
          r.action("StreamOn"),
          sweep(r, "Sweep TIP 3's spill, streaming at the left CELL (TIP 4)", ms=sweep_ms),
          r.action("StreamOff"))
    return r


VARIANTS = {"flow-solo": {}, "flow-solo-65": {"speed": 65}}

if __name__ == "__main__":
    for n, kw in VARIANTS.items():
        r = flow_solo(n, **kw)
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(n)
