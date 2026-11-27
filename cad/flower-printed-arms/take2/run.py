"""Build one take-2 layout: its hump and arm, its printed mount (left and right), the clearance check, a picture.

    REAL_CAD_POINTS=<26 Nov STEP points .npy> python3 take2/run.py 2a_upright   # or 2b_flat
Writes take2/<layout>/: arm.step/.stl, mount_left/right.step/.stl, clearance.json, corner.png."""
import os, sys, json
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE); sys.path.insert(0, os.path.dirname(HERE))
import layouts as L, geometry as G, parts, check   # noqa: E402
import cadquery as cq                               # noqa: E402

name = sys.argv[1]
boxes, mount = L.LAYOUTS[name]
OUT = os.path.join(HERE, name); os.makedirs(OUT, exist_ok=True)
G.corner_boxes = boxes; check.corner_boxes = boxes          # every module reads the corner from here
parts.HERE = OUT; check.HERE = OUT
if name == "2c_straight":
    G.ARM_Y["left"] = L.LEFT_ARM_2C                          # the left arm out to the right arm's spacing
    parts._HUMP = (0.0, parts.HUMP_FROM, parts.HUMP_FROM + 1, parts.HUMP_FROM + 2, parts.HUMP_FROM + 3)   # straight
parts.SIZING_MARGIN = {"2b_flat": 5.0}.get(name, parts.SIZING_MARGIN)   # the flat wheel's top face is sampled coarser than check.py measures it
h, a, b, c, e = parts.hump()
print(f"{name}: hump {h:.1f} mm; leaves the line at u {a:.0f}, full {b:.0f} to {c:.0f}, back at {e:.0f}", flush=True)
parts.export(parts.arm(), "arm")
for side in ("left", "right"):
    for ext in ("step", "stl"):
        cq.exporters.export(mount(side), os.path.join(OUT, f"mount_{side}.{ext}"), tolerance=0.05, angularTolerance=0.1)
check.main()
