"""The divot blade, v2 (16 Oct): Carter's divot, a wedge nose, and every depth taken from FIRST's field CAD.

Why v2. On 15 Oct the blocks couldn't be seated against the back of the FLOWER without working them back and forth. A
static force balance says why: the bottom POLLEN is pinched between the blade's front and the FLOWER's uprights, and a
steady push lifts it only if the blade's contact normal is tilted up by more than twice the friction angle (33 deg at
mu 0.3, 44 at 0.4). The tested block's front leans 17 deg and its top corner meets the POLLEN at 4 deg: it jams until
wiggled. v2's nose leans NOSE_DEG (45: lifts for mu up to 0.41 sliding, 1.0 rolling; tape the nose with UHMW or
packing tape and it's comfortable).

The shape, from the field CAD (tools/robot-cad/flower_from_field_cad.py), x from the bottom POLLEN's centre toward
the wall: the uprights' forward-most points are at x 1.18 (|y| 1.42), their inner corners at (1.61, +-0.61), so a
lifted POLLEN is pinned at x +0.35; the base ring's top is 0.35 in, its hole 3.08 across. So:
  - the SHOULDERS (|y| from LANE/2 out) are the stop: their nose's bottom edge lands on the uprights' forward points,
    and they're tall enough (SH_TOP) to hold the 8 mm hex shaft with margin;
  - the CENTRE LANE (|y| < LANE/2, where the POLLEN is) is lower (LANE_TOP, under the POLLEN's centre at 1.40) with a
    shallow divot in its nose that centres the POLLEN. It has NO flat top: ramp.py with the CAD FLOWER (sim_variants,
    16 Oct) keeps all 4 POLLEN in when a flat reaches back to the pinned POLLEN, and empties 4/4 at 4 and 12 in/s when
    the slope starts at the ridge. So from the ridge (x 0.73 at the stop) it runs down at RUN_DEG for RUN_LEN, just
    enough to skin over the shaft, then at SLOPE_DEG to the back edge. The pinned POLLEN (0.35) is 0.38 in down it;
  - the hex bore sits 0.69 in behind the stop, under the shoulders' nose and flat (8 mm of plastic) and the lane's run
    (about 2 mm: a skin, the shoulders take the load).
Blade frame: x from the back edge toward the FLOWER, y across, z from the tiles.

    python3 cad/modules/flower-stick-proto/divot_blade.py                         # sled: 39 mm wheels, shaft 0.77 in
    python3 cad/modules/flower-stick-proto/divot_blade.py --shaft 0.945 --bottom 0.70   # 48 mm wheels (tops +0.18)

Writes stl/divot_blade_v2_<...>.stl flat side down in millimetres, and a PNG of three views.
"""
import argparse, math, os
import numpy as np, trimesh
from trimesh.creation import cylinder

HERE = os.path.dirname(os.path.abspath(__file__))
IN = 25.4
ap = argparse.ArgumentParser()
ap.add_argument("--width", type=float, default=5.4)
ap.add_argument("--lane", type=float, default=2.0, help="the centre lane's width, inches")
ap.add_argument("--bottom", type=float, default=0.52, help="blade bottom above the tiles, inches (the ring is 0.35)")
ap.add_argument("--shaft", type=float, default=0.77, help="shaft centre above the tiles, inches")
ap.add_argument("--lane-top", type=float, help="centre lane's top, inches (default: shaft + 0.28, at least 3 mm over the bore)")
ap.add_argument("--sh-top", type=float, help="shoulders' top, inches (default: lane top + 0.20)")
ap.add_argument("--nose-deg", type=float, default=45.0, help="nose lean from vertical, both lane and shoulders")
ap.add_argument("--land", type=float, default=0.08, help="vertical land at the bottom of the nose, inches")
ap.add_argument("--stop", type=float, default=1.18, help="uprights' forward points behind the POLLEN's centre, inches")
ap.add_argument("--pin", type=float, default=0.35, help="a lifted POLLEN's centre, pinned by the uprights' corners, inches")
ap.add_argument("--run-deg", type=float, default=8.0, help="the lane's first slope behind the ridge, over the shaft")
ap.add_argument("--run-len", type=float, default=0.45, help="its length, inches")
ap.add_argument("--slope-deg", type=float, default=38.0)
ap.add_argument("--divot", choices=("scoop", "spoon"), default="scoop",
                help="scoop: a vertical arc cut into the nose, concave across the blade only (reading 1 of Carter's sketch); "
                     "spoon: a bowl hollowed into the nose face, concave across and up the face (reading 2)")
ap.add_argument("--divot-r", type=float, default=2.0, help="scoop: radius in plan, inches")
ap.add_argument("--divot-chord", type=float, default=1.8, help="width across the nose, inches (both)")
ap.add_argument("--divot-depth", type=float, default=0.15, help="spoon: depth into the face at its middle, inches")
ap.add_argument("--hex", type=float, default=8.3, help="hex bore across the flats, mm")
a = ap.parse_args()

lane_top = a.lane_top if a.lane_top else a.shaft + a.hex / 2 / IN + 0.12
sh_top = a.sh_top if a.sh_top else lane_top + 0.20
t = math.tan(math.radians(a.nose_deg)); ts = math.tan(math.radians(a.slope_deg))
# x here is FLOWER x (from the POLLEN's centre toward the wall); the nose bottom edge is at the stop
nose_bot = a.bottom + a.land
lane_ridge = a.stop - (lane_top - nose_bot) * t
assert lane_ridge - a.pin >= 0.3, f"the lane's ridge is only {lane_ridge - a.pin:.2f} in past the pinned POLLEN: lower --lane-top"
tr = math.tan(math.radians(a.run_deg))
lane_knee = (lane_ridge - a.run_len, lane_top - a.run_len * tr)
lane_back = lane_knee[0] - (lane_knee[1] - a.bottom) / ts
sh_ridge = a.stop - (sh_top - nose_bot) * t
rod_x = lane_ridge - 0.24                                 # the bore under the lane's run and the shoulders' nose/flat
hex_half = a.hex / 2 / IN / math.cos(math.pi / 6)
skin = (lane_top - (lane_ridge - (rod_x - hex_half)) * tr) - (a.shaft + a.hex / 2 / IN)
sh_junction = rod_x - 0.30
sh_back = sh_junction - (sh_top - a.bottom) / ts
back = min(lane_back, sh_back)
def prof_lane():
    return [(lane_back, a.bottom), lane_knee, (lane_ridge, lane_top), (a.stop, nose_bot), (a.stop, a.bottom)]
def prof_sh():
    return [(sh_back, a.bottom), (sh_junction, sh_top), (sh_ridge, sh_top), (a.stop, nose_bot), (a.stop, a.bottom)]
def slab(profile, y0, y1):
    m = trimesh.creation.extrude_polygon(trimesh.path.polygons.Polygon([((x - back) * IN, (z - a.bottom) * IN) for x, z in profile]), (y1 - y0) * IN)
    m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))    # y runs -(y1-y0)..0
    m.apply_translation([0, y1 * IN, 0]); return m
W, L = a.width, a.lane
lane = slab(prof_lane(), -L / 2, L / 2)
left = slab(prof_sh(), L / 2, W / 2)
right = slab(prof_sh(), -W / 2, -L / 2)
body = lane.union(left).union(right)
R, chord = a.divot_r, a.divot_chord
if a.divot == "scoop":
    dd = R - math.sqrt(R * R - (chord / 2) ** 2)
    cut = cylinder(radius=R * IN, height=(lane_top - a.bottom + 1) * IN, sections=160)
    cut.apply_translation([(a.stop - back - dd + R) * IN, 0, (lane_top - a.bottom) / 2 * IN])
else:
    # an ellipsoid sunk --divot-depth into the middle of the lane's nose face: its rim stays inside the face (0.9 of the
    # face's half-length) and reaches --divot-chord across the blade
    dd = a.divot_depth
    face_len = math.hypot(a.stop - lane_ridge, lane_top - nose_bot)
    rc = 0.8                                             # semi-axis along the face's normal, inches
    rim = math.sqrt(1 - ((rc - dd) / rc) ** 2)           # fraction of a semi-axis at which the face cuts the ellipsoid
    ra, rb = 0.9 * face_len / 2 / rim, chord / 2 / rim
    cut = trimesh.creation.icosphere(subdivisions=4); cut.apply_scale([ra * IN, rb * IN, rc * IN])
    th = math.radians(a.nose_deg)                        # local x along the face (up it), local z its outward normal
    T = np.eye(4); T[:3, :3] = np.array([[-math.sin(th), 0, math.cos(th)], [0, 1, 0], [math.cos(th), 0, math.sin(th)]]).T
    cut.apply_transform(T)
    mx, mz = (a.stop + lane_ridge) / 2, (nose_bot + lane_top) / 2
    cut.apply_translation([(mx - back + (rc - dd) * math.cos(th)) * IN, 0, (mz - a.bottom + (rc - dd) * math.sin(th)) * IN])
body = body.difference(cut)
bore = cylinder(radius=a.hex / 2 / math.cos(math.pi / 6), height=W * IN + 4, sections=6)
bore.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
bore.apply_translation([(rod_x - back) * IN, 0, (a.shaft - a.bottom) * IN])
body = body.difference(bore)
assert body.is_watertight
under = (a.shaft - a.bottom) * IN - a.hex / 2
over_sh = (sh_top - a.shaft) * IN - a.hex / 2
assert skin * IN >= 1.2, f"lane skin over the shaft only {skin * IN:.1f} mm: lengthen --run-len or flatten --run-deg"
os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
name = f"divot_blade_v2_{a.divot}_w{a.width:.1f}_shaft{a.shaft:.3f}"
body.export(os.path.join(HERE, "stl", name + ".stl"))
D = a.stop - back
print(f"stl/{name}.stl: {W * IN:.0f} x {D * IN:.0f} x {(sh_top - a.bottom) * IN:.0f} mm, {body.volume / 1000:.0f} cm3")
print(f"  lane (|y| < {L / 2}): bottom {a.bottom:.2f}, ridge ({lane_ridge:.2f}, {lane_top:.2f}) at the stop; nose {a.nose_deg:.0f} deg from x {a.stop:.2f}; "
      f"{a.run_deg:.0f} deg run to ({lane_knee[0]:.2f}, {lane_knee[1]:.2f}), then {a.slope_deg:.0f} deg to the back edge at {lane_back:.2f}; "
      f"{a.divot} {chord:.1f} wide, {dd:.2f} deep; the pinned POLLEN at {a.pin:.2f} is {lane_ridge - a.pin:.2f} in down from the ridge")
print(f"  shoulders: top {sh_top:.2f}; nose {sh_ridge:.2f}..{a.stop:.2f}, flat {sh_junction:.2f}..{sh_ridge:.2f}, back edge {sh_back:.2f}")
print(f"  bore: centre x {rod_x:.2f} ({a.stop - rod_x:.2f} behind the stop), z {a.shaft:.3f}; plastic {under:.1f} mm under, {skin * IN:.1f} mm of lane skin over its "
      f"back corner, {over_sh:.1f} mm over it in the shoulders")

import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
m = body.copy(); m.apply_translation(-m.bounds[0]); v = m.vertices / IN; f = m.faces
Lt = np.array([0.3, -0.6, 0.75]); Lt /= np.linalg.norm(Lt); k = 0.35 + 0.65 * np.clip(m.face_normals @ Lt, 0, 1)
col = np.c_[np.outer(k, [0.95, 0.55, 0.12]), np.ones(len(k))]
fig = plt.figure(figsize=(14, 5))
for i, (el, az, tt) in enumerate(((28, -55, f"3D: low centre lane with the {a.divot}, taller shoulders"), (0, -90, f"Side: {a.nose_deg:.0f} deg nose; lane ridge then {a.run_deg:.0f} and {a.slope_deg:.0f} deg; shoulders taller"), (90, -90, f"Top: lane, {a.divot}, shoulders"))):
    ax = fig.add_subplot(1, 3, i + 1, projection="3d")
    ax.add_collection3d(Poly3DCollection(v[f], facecolors=col, edgecolors="none"))
    e = v.max(0); ax.set_xlim(0, e[0]); ax.set_ylim(0, e[1]); ax.set_zlim(0, e[2]); ax.set_box_aspect(e)
    ax.view_init(el, az); ax.set_title(tt, fontsize=10); ax.tick_params(labelsize=7)
e = v.max(0)
fig.suptitle(f"{name}: {e[1]:.2f} in wide, {e[0]:.2f} in front to back; bottom {a.bottom:.2f}, lane top {lane_top:.2f}, shoulders {sh_top:.2f}", fontsize=11)
fig.tight_layout(); fig.savefig(os.path.join(HERE, name + ".png"), dpi=110)
