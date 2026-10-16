"""The FLOWER stick blade's lane profile against the field-CAD FLOWER, in ramp.py's 2-D model (16 Oct 2026, issue #179).

    python3 tools/ramp-hook/blade_variants.py        # about 5 minutes on 8 cores

What it found: with the uprights pinning a lifted POLLEN 0.35 in behind the hole's centre (the field CAD), a lane whose
flat top reaches back to the POLLEN (V1, the first v2 draft) keeps all 4 in at a 4 in/s push; every lane whose slope
starts at the ridge empties 4/4 at 2, 4 and 12 in/s, friction 0.3 or 0.5. cad/modules/flower-stick-proto/divot_blade.py
builds V3. ramp.py's blade is kinematic, so this can't show the wedge jam the 15 Oct test hit (the static balance in
divot_blade.py's docstring does); it shows what happens once the POLLEN is lifted.
"""
import math, multiprocessing as mp, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__))); import ramp
STOP = ramp.use_cad_flower()
B = 0.52
def lane(top=1.05, nose_lean=0.45, flat=0.0, a1=38.0, len1=0.0, a2=38.0):
    """x from the nose's bottom edge toward the robot: a 0.08 land, a 45 deg nose, a flat, slope a1 for len1, slope a2 down."""
    p = [(0, B), (0, B + 0.08), (nose_lean, top), (nose_lean + flat, top)]
    x, z = nose_lean + flat, top
    if len1 > 0: x += len1; z -= len1 * math.tan(math.radians(a1)); p.append((x, z))
    x += (z - B) / math.tan(math.radians(a2)); p += [(x, B), (0, B)]
    return [q for i, q in enumerate(p) if i == 0 or q != p[i - 1]]
VARIANTS = {
    "V1 flat 0.33 then 38 deg (first draft)": lane(flat=0.33),
    "V2 38 deg from the ridge": lane(),
    "V3 8 deg for 0.45 then 38 (built)": lane(a1=8, len1=0.45),
    "V4 15 deg for 0.43 then 38": lane(a1=15, len1=0.43),
    "V6 10 deg all the way": lane(a1=10, len1=0.0, a2=10),
}
def go(j):
    name, drive, mu, prof = j
    r = ramp.run(ring=0.35, drive=drive, tip_depth=STOP, e=0.3, mu=mu, profile=prof, t_max=4.0); return name, drive, mu, r
if __name__ == "__main__":
    jobs = [(n, d, m, p) for n, p in VARIANTS.items() for d in (4.0, 12.0) for m in (0.3, 0.5)]
    with mp.Pool() as pool:
        for name, drive, mu, r in pool.imap(go, jobs):
            print(f"{name:40s} drive {drive:4.0f} in/s mu {mu}: out {4 - r[2]}/4" + (f", last out {r[0]:.2f} s" if r[0] else ""), flush=True)
