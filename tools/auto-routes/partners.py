from autogen import *
from helpers import waits, fire

def leave_park():
    """Leaves and parks, nothing more, but first sets its 4 preloads in a row against its front for
    us to collect (AutoStudyTest.LEAVE_PARTNER_STAGED). Slides west clear of them, then parks toward
    the far-left end of the LOADING ZONE, off the wall."""
    r = Route("partner-leave-park", (24, 132.25, 270), speed=40, folder=PP_DIR + "/partners")
    r.pt("SLIDE_P", 10.5, 131, 270).pt("PARK_P", 10.5, 111, 270).pt("PARK_P2", 10.5, 110, 270)
    r.add(r.go("SLIDE_P", heading=270), r.go("PARK_P", heading=270), r.go("PARK_P2", park=True))
    return r

def preloads_park():
    r = Route("partner-preloads-park", (59, 132.25, 270), speed=40, folder=PP_DIR + "/partners")
    r.pt("PARK_P", 10.5, 111, 270)  # the far-left end of the LOADING ZONE, off the wall
    r.add(r.action("SpinUp"),
          *waits(r, "Left CELL up", "LeftCellUp", 8.0),
          fire(r, "Fire the preloads", "Empty"),
          r.go("PARK_P", ctrl=[(59, 116), (30, 116)], park=True))  # right first, clear of the far FLOWER
    return r

if __name__ == "__main__":
    leave_park().write(); preloads_park().write()
    specs = []
    for ours in ("SoloTwoTipAuto@40", "SoloThreeTipAuto@50", "SpillThreeTipAuto@50"):
        a, sp = ours.split("@")
        for p in ("PartnerLeaveParkAuto", "PartnerPreloadsParkAuto"):
            specs.append(f"{a},{p}@{sp}")
    study(";".join(specs), runs=10)
