"""Lay blade STLs side by side on one print plate, flat side down, millimetres.

    python3 cad/modules/flower-stick-proto/plate.py stl/print_plate_v5.stl stl/v5.stl stl/v5_scoop.stl
"""
import sys, os, trimesh
out, *ins = sys.argv[1:]
ms, x = [], 0.0
for p in ins:
    m = trimesh.load(p); m.apply_translation(-m.bounds[0]); m.apply_translation([x, 0, 0]); ms.append(m); x = m.bounds[1][0] + 10.0
plate = trimesh.util.concatenate(ms); plate.export(out)
b = plate.bounds; print(f"{out}: {b[1][0]-b[0][0]:.0f} x {b[1][1]-b[0][1]:.0f} x {b[1][2]-b[0][2]:.0f} mm, {len(ins)} parts")
