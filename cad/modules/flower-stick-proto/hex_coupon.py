"""A hex-fit coupon: one small bar with hex holes of the sizes given (default 8.1, 8.2, 8.3 mm across the flats), each
marked with one notch more than the last. Printed flat with the holes' axes horizontal and flats top and bottom, exactly like the blades,
so the hole that fits the shaft snugly on a given printer is the size to give the blade (`divot_blade.py --hex`).

    python3 cad/modules/flower-stick-proto/hex_coupon.py        # writes stl/hex_fit_coupon.stl
"""
import math, os
import trimesh
from trimesh.creation import box, cylinder
HERE = os.path.dirname(os.path.abspath(__file__))
import sys
SIZES = [float(v) for v in sys.argv[1:]] or [8.1, 8.2, 8.3]
L, H, T = 24.0 + 21.0 * (len(SIZES) - 1), 18.0, 12.0                       # mm: length, height, thickness along the holes' axes
bar = box(extents=[L, T, H]); bar.apply_translation([L / 2, T / 2, H / 2])
for i, af in enumerate(SIZES):
    x = 12.0 + i * 21.0
    hexhole = cylinder(radius=af / 2 / math.cos(math.pi / 6), height=T + 4, sections=6)
    hexhole.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))   # axis along y, flats top and bottom
    hexhole.apply_translation([x, T / 2, H / 2])
    bar = bar.difference(hexhole)
    for k in range(i + 1):                        # notches on the top edge: 1, 2, 3 for 8.1, 8.2, 8.3
        n = box(extents=[1.6, T + 2, 1.6]); n.apply_translation([x - 1.0 * i + 2.0 * k, T / 2, H])
        bar = bar.difference(n)
assert bar.is_watertight
os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
out = os.path.join(HERE, "stl", "hex_fit_coupon.stl" if SIZES == [8.1, 8.2, 8.3] else "hex_fit_coupon_" + "_".join(f"{v:g}" for v in SIZES) + ".stl"); bar.export(out)
b = bar.bounds; print(f"{out}: {b[1][0]-b[0][0]:.0f} x {b[1][1]-b[0][1]:.0f} x {b[1][2]-b[0][2]:.0f} mm; holes {SIZES} mm across the flats, " + "/".join(str(i + 1) for i in range(len(SIZES))) + " notches")
