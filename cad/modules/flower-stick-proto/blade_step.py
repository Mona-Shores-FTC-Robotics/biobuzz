"""STEP files of the blades for review in Onshape: the same formulas as blade6.py, built as real solids (CadQuery/OCCT)
instead of a mesh. The tail with Carter's lane is a B-spline surface interpolated through the height field, so it is
the printed STL to within a few hundredths of a millimetre.

    python3 cad/modules/flower-stick-proto/blade_step.py          # writes step/v6_lane.step, step/v5_lane.step, step/hex_fit_coupon.step

Frame as blade6.py: x inches behind the lip (toward the robot), y across, z above the tiles; the file is in mm with the
blade's flat bottom on z = 0 and the lip at x = 0.
"""
import math, os
import numpy as np
import cadquery as cq
from OCP.TColgp import TColgp_Array2OfPnt
from OCP.gp import gp_Pnt
from OCP.GeomAPI import GeomAPI_PointsToBSplineSurface
from OCP.GeomAbs import GeomAbs_C1
from OCP.BRepBuilderAPI import BRepBuilderAPI_MakeFace
from OCP.BRepPrimAPI import BRepPrimAPI_MakePrism
from OCP.gp import gp_Vec
IN = 25.4
HERE = os.path.dirname(os.path.abspath(__file__))
os.makedirs(os.path.join(HERE, "step"), exist_ok=True)

def blade(name, edge, nose, peak, tail, rise, lane_start, oval, lead, width=5.4, bottom=0.52, shaft_z=0.77, hex_af=7.15, hex_angle=0.0, lane_r=0.0, lane_mode="scaled"):
    # lane_mode: "scaled" (blade6.py: the cross-section scaled by the lead-in ramp) or "cut" (divot_blade.py: a ramped
    # cap with the trough cylinder cut out of it, i.e. the smaller of the two)
    tn, tt = math.tan(math.radians(nose)), math.tan(math.radians(tail))
    xp = (peak - edge) / tn; xe = xp + (peak - bottom) / tt; hw = width / 2
    Rg = lane_r if lane_r > 0 else ((hw * hw + rise * rise) / (2 * rise) if rise > 0 else 0)
    hex_corner = hex_af / IN / 2 / math.cos(math.pi / 6)
    ang = math.radians(hex_angle)
    hex_along = max(abs(hex_corner * math.cos(math.radians(30 + 60 * k) + ang)) for k in range(6))
    shaft_x = xp + hex_along
    def tail_z(x): return max(bottom, peak - (x - xp) * tt)
    def lane(x, y):
        if rise <= 0: return 0.0
        ay = abs(y); c = rise if ay >= Rg else min(rise, Rg - math.sqrt(Rg * Rg - ay * ay))
        xs = lane_start + oval * (1 - math.sqrt(max(0.0, 1 - (ay / hw) ** 2)))
        t = 0.0 if x <= xs else 1.0 if x >= xs + lead else (x - xs) / lead
        return c * t if lane_mode == "scaled" else min(c, rise * t)
    # the nose: a planar wedge from the lip to the peak, full width (the lane begins behind the peak)
    nose_prism = (cq.Workplane("XZ").polyline([(0, bottom * IN), (0, edge * IN), (xp * IN, peak * IN), (xp * IN, bottom * IN)]).close()
                  .extrude(hw * IN, both=True))
    # the tail: a B-spline surface through the height field, extruded down and clipped to the bottom, joined to the
    # nose, the bore cut. The result is checked against the printed mesh's volume; the first grid that gives a valid
    # solid within 3 % is kept.
    import trimesh
    want = trimesh.load(os.path.join(HERE, "stl", name + ".stl")).volume
    bore = (cq.Workplane("XZ").polygon(6, 2 * hex_corner * IN).extrude((width + 1) * IN, both=True)
            .rotate((0, 0, 0), (0, 1, 0), hex_angle).translate((shaft_x * IN, 0, shaft_z * IN)))
    body = None
    for nx, ny in ((141, 81), (281, 81), (201, 61), (281, 41), (141, 41)):
        try:
            xs_ = np.linspace(xp, xe, nx); ys_ = np.linspace(-hw, hw, ny)
            arr = TColgp_Array2OfPnt(1, nx, 1, ny)
            for i, x in enumerate(xs_):
                for j, y in enumerate(ys_):
                    arr.SetValue(i + 1, j + 1, gp_Pnt(x * IN, y * IN, (tail_z(x) + lane(x, y)) * IN))
            surf = GeomAPI_PointsToBSplineSurface(arr, 3, 8, GeomAbs_C1, 0.02).Surface()   # tolerance 0.02 mm
            face = BRepBuilderAPI_MakeFace(surf, 1e-4).Face()
            tail_solid = cq.Shape.cast(BRepPrimAPI_MakePrism(face, gp_Vec(0, 0, -(peak + 1) * IN)).Shape())
            clip = (cq.Workplane("XY").box((xe - xp) * IN + 10, width * IN + 10, (peak + 2) * IN, centered=(False, True, False))
                    .translate((xp * IN - 5, 0, bottom * IN)))
            tail_part = cq.Workplane("XY").add(tail_solid).intersect(clip)
            cand = nose_prism.union(tail_part, glue=True)
            if len(cand.solids().vals()) != 1: cand = nose_prism.union(tail_part, tol=0.01)
            cand = cand.cut(bore)
            ok = len(cand.solids().vals()) == 1 and cand.val().isValid()
            err = abs(cand.val().Volume() - want) / want if ok else None
        except Exception as e:
            ok, err = False, None
        print(f"  {name}: grid {nx} x {ny}: {'valid' if ok else 'invalid'}" + (f", volume off by {err * 100:.1f} %" if ok else ""))
        if ok and err < 0.03: body = cand; break
    assert body is not None, "no surface fit was good enough"
    solid = body.val()
    out = os.path.join(HERE, "step", name + ".step")
    cq.exporters.export(body, out)
    print(f"{out}: {solid.Volume() / 1000:.1f} cm3; bbox {[round(v, 1) for v in (solid.BoundingBox().xlen, solid.BoundingBox().ylen, solid.BoundingBox().zlen)]} mm")
    return solid.Volume()

v6 = blade("v6_lane", edge=0.60, nose=50, peak=1.10, tail=12, rise=0.3, lane_start=(1.10 - 0.60) / math.tan(math.radians(50)) + 0.05, oval=1.2, lead=1.2)
v5 = blade("v5_lane", edge=0.60, nose=60, peak=1.10, tail=12, rise=0.3, lane_start=0.9, oval=0.0, lead=1.6, lane_mode="cut")
# the coupon
SIZES = [7.0, 7.1, 7.2, 7.3, 7.4]; L, H, T = 24.0 + 21.0 * (len(SIZES) - 1), 18.0, 12.0
bar = cq.Workplane("XY").box(L, T, H, centered=(False, False, False))
for i, af in enumerate(SIZES):
    x = 12.0 + i * 21.0
    bar = bar.cut(cq.Workplane("XZ").polygon(6, af / math.cos(math.pi / 6)).extrude(T + 2, both=True).translate((x, T / 2, H / 2)))
    for k in range(i + 1):
        bar = bar.cut(cq.Workplane("XY").box(1.6, T + 2, 1.6).translate((x - 1.0 * i + 2.0 * k, T / 2, H)))
cq.exporters.export(bar, os.path.join(HERE, "step", "hex_fit_coupon.step")); print("step/hex_fit_coupon.step")
# compare with the printed meshes
import trimesh
for n, v in (("v6_lane", v6), ("v5_lane", v5)):
    m = trimesh.load(os.path.join(HERE, "stl", n + ".stl")); print(f"  {n}: STEP {v / 1000:.1f} cm3 vs STL {m.volume / 1000:.1f} cm3")
