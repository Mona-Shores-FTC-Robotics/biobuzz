"""The wide mountain blade with its tail cut off 2.0 in behind the lip (page/tailcut_tall.js: the model feeds every case at
every speed on the coach's arms down to 2.0; 1.6 starts dropping the slow ones). Everything else is mountain_wide: 7.9 in wide,
Carter's lane, the 7.15 mm hex bore. -> step/mountain_wide_2in.step, stl/mountain_wide_2in.stl

    TAIL=2.0 python3 cad/modules/flower-stick-proto/blade_short.py
"""
import os, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); IN = 25.4
TAIL = float(os.environ.get("TAIL", "2.0")); NAME = f"mountain_wide_{TAIL:g}in".replace(".", "p")
body = cq.importers.importStep(os.path.join(HERE, "step", "mountain_wide.step"))
bb0 = body.val().BoundingBox()
body = body.intersect(cq.Workplane("XY").box(TAIL * IN, 400, 100, centered=(False, True, False)).translate((bb0.xmin, 0, bb0.zmin)))   # x: behind the lip
v = body.val(); assert len(body.solids().vals()) == 1 and v.isValid()
cq.exporters.export(body, os.path.join(HERE, "step", NAME + ".step")); cq.exporters.export(body, os.path.join(HERE, "stl", NAME + ".stl"), tolerance=0.05, angularTolerance=0.2)
bb = v.BoundingBox(); print(f"{NAME}: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3 (full blade {bb0.xlen:.0f} deep)")
