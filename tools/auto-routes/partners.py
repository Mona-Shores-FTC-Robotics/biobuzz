from autogen import *
from helpers import waits, fire

def leave_park():
    """Leaves and parks, nothing more, but first sets its 4 preloads in a row along its field side
    for us to collect (AutoStudyTest.LEAVE_PARTNER_STAGED), so it drives straight off to park
    toward the far-left end of the LOADING ZONE, off the wall (mentor review)."""
    r = Route("partner-leave-park", (24, 132.25, 270), speed=40)
    r.pt("PARK_P", 10.5, 116, 270)  # the zone's far end (110 until 8 Oct 2026): we park deep at y 95, body to 102.6
    r.add(r.go("PARK_P", ctrl=[(24, 118)], heading=270, park=True))
    return r

def preloads_park():
    r = Route("partner-preloads-park", (59, 132.25, 270), speed=40)
    r.pt("PARK_P", 10.5, 116, 270)  # the far-left end of the LOADING ZONE, off the wall (111 until 8 Oct 2026: we park deep at y 95)
    r.add(r.action("SpinUp"),
          *waits(r, "Left CELL up", "LeftCellUp", 8.0),
          fire(r, "Fire the preloads", "Empty"),
          r.go("PARK_P", ctrl=[(59, 116), (30, 116)], park=True))  # right first, clear of the far FLOWER
    return r

# The way a partner at the standard left start parks: 4.75 in straight back off the wall, west under
# the far FLOWER (its edge clears the FLOWER below y 127.8), then down the wall into the far end of the
# LOADING ZONE. Its edge never comes south of y 118.5, out of the spot we fire from (our edge at 119)
# once it is west of x 40.
LEFT_LANE = (58, 127.5, 270)
LEFT_PARK_CTRL = [(12, 127.5)]


def _park_left(r):
    r.pt("LANE", *LEFT_LANE).pt("PARK_P", 10.5, 111, 270)
    return [r.go("LANE", heading=270), r.go("PARK_P", ctrl=LEFT_PARK_CTRL, heading=270, park=True)]


def preloads_left(name="partner-preloads-left"):
    """A partner that can shoot, at the standard left start in front of the left CELL: fires its 4 when
    the left CELL rises, then parks west under the far FLOWER (LEFT_PARK_CTRL)."""
    r = Route(name, (59, 132.25, 270), speed=40)
    r.add(r.action("SpinUp"),
          *waits(r, "Left CELL up", "LeftCellUp", 8.0),
          fire(r, "Fire the preloads", "Empty"),
          *_park_left(r))
    return r


def park_left(name="partner-park-left", wait_s=0.0):
    """A partner that only drives, at the standard left start: waits `wait_s`, then parks west under the
    far FLOWER (LEFT_PARK_CTRL). It keeps its 4 preloads."""
    r = Route(name, (59, 132.25, 270), speed=40)
    if wait_s:  # a pure timer: it holds its 4 preloads and never fires, so Empty never comes
        r.add(r.wait(f"Wait {wait_s:g} s", when=["Empty"], ms=int(wait_s * 1000)))
    r.add(*_park_left(r))
    return r


def stage_exit(name="partner-stage-exit", x=35.0, fwd_y=120.0):
    """A partner that can't shoot (4 Oct 2026 request): starts against the wall at (x, 132.25) facing the
    HIVE, drives straight forward, sets its 4 POLLEN down in a row across its front (y about 108, about
    x-5 to x+5: west of our tunnel lane for x 35), backs straight off and parks west under the far FLOWER
    into the far end of the LOADING ZONE. Done by about 4 s, before we come north."""
    r = Route(name, (x, 132.25, 270), speed=40)
    r.pt("STAGE", x, fwd_y, 270).pt("BACK", x, 127.5, 270).pt("PARK_P", 10.5, 111, 270)
    r.add(r.go("STAGE", heading=270),
          r.wait("Set the preloads down", when=["Empty"], ms=1500, alongside="SetDown"))
    r.at = "STAGE"
    r.add(r.go("BACK", heading=270), r.go("PARK_P", ctrl=[(12, 127.5)], heading=270, park=True))
    return r


def partner_right(name="partner-preloads-right"):
    """Starts in front of the right CELL, fires at once, parks toward the far-left end of the LOADING
    ZONE like the other reference partners (mentor review), leaving the near end for us."""
    r = Route(name, (59, 9.5, 90), speed=40)
    r.pt("PARK_P", 10.5, 116, 90)  # the zone's far end (110 until 8 Oct 2026): we park deep at y 95, body to 102.6; this body from 107
    # Intake off: it only fires its preloads, so it has no reason to sweep up the TIP 1 spill on its
    # way to park (it used to, and left us nothing at the right end).
    r.add(r.action("SpinUp"), r.action("IntakeOff"), fire(r, "Fire the preloads", "Empty", ms=4500),
          r.go("PARK_P", ctrl=[(26, 20), (26, 100)], park=True))  # x 26: clear of the HIVE frame's foot bar
    return r


if __name__ == "__main__":
    leave_park().write(); preloads_park().write()
    specs = []
    for ours in ("SoloTwoTipAuto@40", "SoloThreeTipAuto@50", "SpillThreeTipAuto@50"):
        a, sp = ours.split("@")
        for p in ("PartnerLeaveParkAuto", "PartnerPreloadsParkAuto"):
            specs.append(f"{a},{p}@{sp}")
    study(";".join(specs), runs=10)
