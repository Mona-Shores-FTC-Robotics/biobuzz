from autogen import *
from helpers import waits, fire

def leave_park():
    r = Route("partner-leave-park", (24, 132.25, 270), speed=40, folder=PP_DIR + "/partners")
    r.pt("PARK_P", 16, 122, 270)
    r.add(r.go("PARK_P", park=True))
    return r

def preloads_park():
    r = Route("partner-preloads-park", (59, 132.25, 270), speed=40, folder=PP_DIR + "/partners")
    r.pt("PARK_P", 16, 122, 270)
    r.add(r.action("SpinUp"),
          *waits(r, "North CELL up", "LeftCellUp", 8.0),
          fire(r, "Fire the preloads", "Empty"),
          r.go("PARK_P", park=True))
    return r

if __name__ == "__main__":
    leave_park().write(); preloads_park().write()
    specs = []
    for ours in ("SoloTwoTipAuto@40", "SoloThreeTipAuto@50", "SpillThreeTipAuto@50"):
        a, sp = ours.split("@")
        for p in ("PartnerLeaveParkAuto", "PartnerPreloadsParkAuto"):
            specs.append(f"{a},{p}@{sp}")
    study(";".join(specs), runs=10)
