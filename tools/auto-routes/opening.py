"""opening.py: R's opening alone (TIP 1's preloads, the catch, the sweep off the floor), to find a catch spot and sweep
that reliably end with 4 held before TIP 2 (mentor, 9 Oct 2026: "maybe we should shift to the left a little bit and do
our little sweep in a different angling and timing to explore if we can reliably pickup 4?").

Sister five decides on R's count before TIP 2 ("Holding 4? Then 5 TIPs"): with 4 it makes 5 TIPs in 16 of 20, and it
reaches 4 in only 20 of 60. Each route here is sister5l-right up to that decision, then R holds where it is; paired
with sister5h-left as usual, so L's TIP 2 and its spill are the real ones. openscore.py reads what R holds at TIP 2.

OPENING_OUT=<dir> writes the .pp files and generated Java there instead of the repository (a sweep makes dozens).
"""
import os
import autogen
from autogen import Route
from helpers import fire, FAR_FLOWER_AT
from alone import seat
from sister import R_START, L_START, L_N, L_TURN, L_S, seated_stream

R_PRE = (57, 20, 90)
R_C1 = (50, 24, 90)


def opening(name, catch=(57.5, 21, 90), pre=R_PRE, sweep=R_C1, catch_ms=1500, fill_ms=2500, sweep_first=None,
            collect=True):
    """catch: where R stands for TIP 1's spill. pre: where it fires its preloads (None: from the catch spot).
    sweep: where it drives to pick up off the floor if not full after catch_ms (heading is the third value).
    sweep_first: a point driven through on the way to sweep (to angle the approach). collect: the webcam chase
    (CollectSeen) alongside the wait; False just waits at sweep with the intake on."""
    r = Route(name, R_START, speed=50)
    r.pt("S_CATCH", *catch).pt("R_C1", *sweep)
    r.add(r.action("SpinUp"))
    if pre is not None:
        r.pt("R_PRE", *pre)
        r.add(r.go("R_PRE", heading=pre[2]))
        r.at = "R_PRE"
        r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000), r.go("S_CATCH", heading=catch[2]))
    else:
        r.add(r.go("S_CATCH", heading=catch[2]))
        r.at = "S_CATCH"
        r.add(fire(r, "Preloads at the right CELL (TIP 1)", "Empty", ms=3000))
    r.at = "S_CATCH"
    retry = [r.wait("TIP 1 missed: catch", when=["IntakeFull"], ms=1500),
             fire(r, "TIP 1 missed: fire the catch", "Tip", ms=3000)]
    r.add(r.wait("TIP 1", when=["Tip"], ms=4000, no=retry))
    fill = []
    if sweep_first is not None:
        r.pt("R_SW", *sweep_first)
        fill.append(r.go("R_SW", heading=sweep_first[2]))
        r.at = "R_SW"
    fill.append(r.go("R_C1", heading=sweep[2]))
    r.at = "R_C1"
    fill.append(r.wait("TIP 1's spill off the floor", when=["IntakeFull"], ms=fill_ms,
                       alongside="CollectSeen" if collect else None))
    r.at = "S_CATCH"
    r.add(r.wait("Catch TIP 1's spill", when=["IntakeFull"], ms=catch_ms, no=fill, no_label="Not full: off the floor"))
    r.add(r.wait("Holding 4?", when=["IntakeFull"], ms=20000))
    return r


def tip2(name, ln=L_N, settle_ms=500, ctrl=((57.5, 100), (57.5, 50)), fill=None, fill_ms=1500, through=None,
         stream_ms=2600):
    """L's catch of TIP 2's spill (8 POLLEN at the far end), as sister5h-left up to L_S, then L holds. Run it as robot 1
    (with sister5l-right as the partner) so the log's held count is L's; openscore.py --at "Holding 4?".
    ln: where L waits for the spill (facing it). ctrl: the lane path's control points. through: a point driven to
    first, into the spill (heading its third value). fill: where L then chases what's on the floor (CollectSeen) for
    up to fill_ms, unless already full, before the lane."""
    r = Route(name, L_START, speed=50)
    r.pt("L_N", *ln).pt("L_TURN", *L_TURN).pt("L_S", *L_S)
    seat(r, "FAR_FLOWER", FAR_FLOWER_AT, 90)
    r.add(r.action("SpinUp"), r.go("FAR_FLOWER_TURN", ctrl=[(59, 118.34)], turn_after=0.3, turn_by=0.9))
    r.at = "FAR_FLOWER_TURN"
    r.add(*seated_stream(r, stream_ms))
    r.at = "L_N"
    r.add(r.wait("TIP 2's spill lands", when=["IntakeFull"], ms=settle_ms))
    start = "L_N"
    if through is not None:
        r.pt("L_IN", *through)
        r.add(r.go("L_IN", heading=through[2]))
        r.at = start = "L_IN"
    if fill is not None:
        r.pt("L_F", *fill)
        r.add(r.go("L_F", heading=fill[2]))
        r.at = "L_F"
        r.add(r.wait("TIP 2's spill off the floor", when=["IntakeFull"], ms=fill_ms, alongside="CollectSeen"))
        start = "L_F"
    r.at = start
    r.add(r.go("L_TURN", ctrl=list(ctrl), turn_after=0.88, turn_by=1.0))
    r.at = "L_TURN"
    r.add(r.go("L_S", heading=90))
    r.at = "L_S"
    r.add(r.wait("Holding 4?", when=["IntakeFull"], ms=20000))
    return r


if __name__ == "__main__":
    import sys
    out = os.environ.get("OPENING_OUT")
    if out:
        os.makedirs(out, exist_ok=True)
        autogen.GEN_DIR = out
    for r in [opening("opening-5l"), tip2("tip2-5h")]:
        r.folder = out or autogen.EXPERIMENTS
        r.write()
        print(r.name)
