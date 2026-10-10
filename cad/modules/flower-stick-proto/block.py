"""The FLOWER block's STL for any shaft height, for builds where something other than the grid plates sets the shaft:
the pivoting arms whose wheels roll on the tiles put the shaft at the wheels' radius.

    python3 cad/modules/flower-stick-proto/block.py --wheel-mm 48            # shaft at a 48 mm wheel's radius
    python3 cad/modules/flower-stick-proto/block.py --shaft 0.945 --bottom 0.70

The block is cad/ramp-hook's (1.4 in deep, curved front, 0.5 in flat top), 0.65 in tall, its bore 12 mm behind the tip.
Writes stl/block_shaft<height>_bottom<height>.stl, flat side down, in millimetres.
"""
import argparse, importlib.util, os
HERE = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("ramp_parts", os.path.join(HERE, "..", "..", "ramp-hook", "parts.py"))
rh = importlib.util.module_from_spec(spec); spec.loader.exec_module(rh)
IN = rh.IN

ap = argparse.ArgumentParser()
g = ap.add_mutually_exclusive_group(required=True)
g.add_argument("--shaft", type=float, help="shaft centre above the tiles, inches")
g.add_argument("--wheel-mm", type=float, help="diameter of the wheels on the shaft that roll on the tiles, mm")
ap.add_argument("--bottom", type=float, default=0.70, help="block bottom above the tiles, inches (0.6 to 0.85)")
a = ap.parse_args()
shaft = a.shaft if a.shaft is not None else a.wheel_mm / 2 / IN
rh.BOTTOM, rh.TOP, rh.ROD_Z = a.bottom * IN, (a.bottom + 0.65) * IN, shaft * IN
under = (shaft - a.bottom) * IN - (rh.ROD_D + rh.FIT) / 2
over = (a.bottom + 0.65 - shaft) * IN - (rh.ROD_D + rh.FIT) / 2
if not 0.6 <= a.bottom <= 0.85: raise SystemExit(f"bottom {a.bottom} in is outside 0.6 to 0.85 (the ring is 0.43 tall)")
if under < 2.0 or over < 2.0:
    raise SystemExit(f"the bore leaves {under:.1f} mm below it and {over:.1f} mm above: under 2 mm. Move --bottom so the "
                     f"shaft ({shaft:.3f} in) sits nearer the block's middle ({a.bottom + 0.325:.3f} in)")
m = rh.ramp_block(); m.apply_translation([0, 0, -m.bounds[0][2]])
os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
path = os.path.join(HERE, "stl", f"block_shaft{shaft:.3f}_bottom{a.bottom:.2f}.stl")
m.export(path)
print(f"{os.path.relpath(path)}: shaft {shaft:.3f} in, bottom {a.bottom:.2f}, top {a.bottom + 0.65:.2f}; "
      f"{under:.1f} mm of plastic under the bore, {over:.1f} mm over it; watertight {m.is_watertight}")
