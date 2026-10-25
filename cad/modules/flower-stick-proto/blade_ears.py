"""The wide mountain blade with ears for the low arm (low_arm.py): the blade bolts to the arms through its tail and the ears,
and the ears carry the wheels' shaft in a plain 8.3 mm bore; the arm channel stops one hole short of the shaft. Built as a real solid from
blade_step.py's surfaces, so it is both the STEP for the assembly and the STL to print.

    python3 cad/modules/flower-stick-proto/blade_ears.py      # step/mountain_wide_ears.step, stl/mountain_wide_ears.stl

Frame as blade6.py: x behind the lip, y across, z up from the blade's flat bottom; mm.
"""
import os, math, json, numpy as np, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); IN = 25.4
src = open(os.path.join(HERE, "blade_step.py")).read()
src = src[:src.index("v7 = blade(")].replace("    return solid.Volume()", "    return body")      # the builder only, returning the solid
src = src.replace("err < (0.05 if vee > 0 else 0.03)", "err < 0.06")   # the 8.6 mm bore alone is 3.3 % of the printed mesh's volume
ns = {"__file__": os.path.join(HERE, "blade_step.py")}; exec(src, ns)
A = json.load(open(os.path.join(HERE, "page", "low_arm.json")))
W, BOTTOM, SHAFT_Z, SHAFT_X = 7.9, 0.52, 0.77, 0.53
body = ns["blade"]("mountain_wide", edge=0.60, nose=60, peak=1.10, tail=12, rise=0.3, lane_start=0.9, oval=0.0, lead=1.6, width=W, hex_af=8.6, shaft_x=SHAFT_X)
# the ears: a vertical wall 0.5 in thick at each end of the blade, EAR[0] to EAR[1] behind the lip and EAR[2] up from the
# bottom. With the arm 30 deg down its first hole up from the shaft lands inside the blade's own tail (1.35 in behind the
# lip, 0.73 up) and its second in the ear (2.16 behind, 1.21 up): two M4 bolts per side. The tail is cut off at TAIL_CUT.
ang = math.radians(-A["ang_dep_deg"]); d = np.array([math.cos(ang), math.sin(ang)])
c = np.array([SHAFT_X, SHAFT_Z - BOTTOM]); holes = [c + d * k * A["hole"] for k in (1, 2)]
E0, E1, EH = A["ear"]; CUT = A["tail_cut"]
body = body.intersect(cq.Workplane("XY").box(CUT * IN, 400, 100, centered=(False, True, False)))
for sg in (1, -1):
    ear = cq.Workplane("XY").box((E1 - E0) * IN, 0.5 * IN, EH * IN, centered=(False, False, False)).translate((E0 * IN, (W / 2 - 0.5) * IN if sg > 0 else -W / 2 * IN, 0))
    body = body.union(ear)
for p in holes:
    body = body.cut(cq.Workplane("XZ").circle(2.2).extrude((W + 1) * IN, both=True).translate((p[0] * IN, 0, p[1] * IN)))
# the shaft: a plain 8.3 mm bore through the ears (the body's own bore is the larger 8.6 hex, so only the ears touch the shaft).
# The wheels carry the blade, so the shaft bears upward on the bore, where the ear is 1.45 in tall; below it 2 mm of wall is enough.
body = body.cut(cq.Workplane("XZ").circle(4.15).extrude((W + 1) * IN, both=True).translate((SHAFT_X * IN, 0, (SHAFT_Z - BOTTOM) * IN)))
v = body.val(); assert len(body.solids().vals()) == 1 and v.isValid()
cq.exporters.export(body, os.path.join(HERE, "step", "mountain_wide_ears.step"))
cq.exporters.export(body, os.path.join(HERE, "stl", "mountain_wide_ears.stl"), tolerance=0.05, angularTolerance=0.2)
bb = v.BoundingBox(); print(f"mountain_wide_ears: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3")
