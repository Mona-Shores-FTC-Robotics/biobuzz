"""The FLOWER extractor (cad/intake-b/) emptying a FLOWER, with ramp.py's 2-D model.

    python3 tools/ramp-hook/extractor.py [trace.json]

The block's slice through the FLOWER's centre: a front face 0.7 to 1.35 in up, a 0.5 in flat top, sloping down to
its back edge 1.4 in behind the tip. Driven in until its tip meets the grey uprights. The roller's front is
5.84 - 1.94 = 3.9 in behind the tip. A POLLEN counts as taken once it reaches the roller. The trace is what the 3D
page plays.
"""
import json
import sys

import ramp

depth = ramp.use_manual_flower()
ramp.INTAKE_BEHIND_TIP = 5.84 - 1.94
ramp.wedge_profile = lambda *a: [(0.0, 0.7), (0.0, 1.35), (0.5, 1.35), (1.4, 0.7), (0.0, 0.7)]
tr = []
_, fed, _ = ramp.run(ring=0.43, tip_depth=depth, wedge=(0,), trace=tr)
print(f"all 4 POLLEN at the roller {fed:.2f} s after the block meets the uprights")
if len(sys.argv) > 1:
    frames = [[round(tip, 3)] + [round(c, 3) for p in ps for c in p] for _, tip, ps in tr]
    json.dump({"res": [round(fed, 3), round(fed, 3), 0], "dt": 0.02, "tipStop": round(ramp.FRONT - depth, 3),
               "frames": frames}, open(sys.argv[1], "w"))
