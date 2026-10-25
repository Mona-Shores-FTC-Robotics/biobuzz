"""The FLOWER stick blade, v6 (19 Oct): a shallower nose, so the push is light; Carter's lane reaching up to the peak with an
oval front; the 8 mm hex carried in two end bosses outside the ball's path, since a low blade has no room for it in the
middle. The top is a height field, so every feature is a formula here and nothing is a boolean except the bore.

    python3 cad/modules/flower-stick-proto/blade6.py --name v6            # writes stl/v6.stl and v6.png

Frame: x inches behind the lip (toward the robot), y across (centre 0), z above the tiles; the bottom is flat at --bottom.
"""
import argparse, math, os
import numpy as np
import trimesh
IN = 25.4
HERE = os.path.dirname(os.path.abspath(__file__))
ap = argparse.ArgumentParser()
ap.add_argument("--edge", type=float, default=0.56, help="height of the lip's edge, inches (the ball is first met here)")
ap.add_argument("--nose", type=float, default=40.0, help="the nose's angle from horizontal, degrees (the push is sin/(cos - mu sin) times the stack's weight)")
ap.add_argument("--peak", type=float, default=0.95, help="peak height, inches; it must sit past the pinned POLLEN (0.35 behind the stop)")
ap.add_argument("--tail", type=float, default=12.0, help="the tail's slope down to the back edge, degrees")
ap.add_argument("--bottom", type=float, default=0.52, help="the blade's flat bottom, inches above the tiles (the ring's top is 0.35)")
ap.add_argument("--width", type=float, default=5.4)
ap.add_argument("--rise", type=float, default=0.3, help="Carter's lane: the surface rises this much at the edges relative to the centreline (0: none)")
ap.add_argument("--lane-r", type=float, default=0.0, help="the lane's radius across; 0: one curve edge to edge")
ap.add_argument("--lane-start", type=float, help="where the lane begins on the centreline, inches behind the lip (default: just behind the peak)")
ap.add_argument("--oval", type=float, default=1.2, help="the lane's front is a half-oval: at the edges it begins this much further back than at the centre")
ap.add_argument("--lead", type=float, default=1.2, help="the lane reaches full depth this far behind where it begins")
ap.add_argument("--boss-h", type=float, default=1.45, help="the end bosses' top, inches above the tiles (0: none)")
ap.add_argument("--boss-in", type=float, default=1.75, help="the bosses start this far from the centre (the ball's path is |y| < 1.4)")
ap.add_argument("--boss-len", type=float, default=1.0, help="the bosses' length along x, centred on the shaft")
ap.add_argument("--shaft-x", type=float, help="the shaft's centre, inches behind the lip (default: the bore's front corner under the peak)")
ap.add_argument("--shaft-z", type=float, default=0.77, help="the shaft's centre height, inches")
ap.add_argument("--hex", type=float, default=7.15, help="hex bore across the flats, mm. goBILDA 8 mm REX is 7.0 across the flats (rounded corners on an 8 mm circle): 7.15 is snug")
ap.add_argument("--hex-angle", type=float, default=0.0, help="the hex's rotation about the shaft, degrees: 0 flats top and bottom, 30 corners top and bottom")
ap.add_argument("--vee", type=float, default=0.0,
                help="a V nose: the lip's middle reaches this much deeper than its sides, inches, so it seats on the uprights' lobes "
                     "(their faces slant from the points at |y| 1.42 to the inner corners at |y| 0.61, 0.43 in deeper); 0: a straight lip")
ap.add_argument("--vee-outer", type=float, default=1.42, help="the V begins this far from the centre (the lobes' points)")
ap.add_argument("--nx", type=int, default=110, help="grid points along the blade (fewer: a lighter mesh for the page's viewer)")
ap.add_argument("--ny", type=int, default=73, help="grid points across")
ap.add_argument("--name", default="v6")
a = ap.parse_args()

tn, tt = math.tan(math.radians(a.nose)), math.tan(math.radians(a.tail))
xp = (a.peak - a.edge) / tn
xe = xp + (a.peak - a.bottom) / tt
W, hw = a.width, a.width / 2
hex_flat = a.hex / IN / 2                      # half across the flats, inches
hex_corner = hex_flat / math.cos(math.pi / 6)  # half across the corners
# the hex's extent up and along the blade for its angle (0: flats top and bottom, corners fore and aft)
_ang = math.radians(a.hex_angle)
hex_up = max(abs(hex_corner * math.sin(math.radians(60 * k) + _ang)) for k in range(6))        # corners at 0, 60, ... from the x axis: flats top and bottom
hex_along = max(abs(hex_corner * math.cos(math.radians(60 * k) + _ang)) for k in range(6))
shaft_x = a.shaft_x if a.shaft_x is not None else xp + hex_along      # the bore's front corner under the sides' peak (the middle's is further in)
lane_start = a.lane_start if a.lane_start is not None else xp + 0.05
Rg = a.lane_r if a.lane_r > 0 else (hw * hw + a.rise * a.rise) / (2 * a.rise) if a.rise > 0 else 0

VEE_SLOPE = 0.43 / 0.81                       # the lobes' faces: 0.43 in deeper over 0.81 in inward
vee_inner = a.vee_outer - a.vee / VEE_SLOPE if a.vee > 0 else a.vee_outer     # the flat tip's half-width
def x_lip(y):                                  # where the lip is, in x behind the sides' lip: the middle reaches a.vee further in (negative)
    ay = abs(y)
    if a.vee <= 0 or ay >= a.vee_outer: return 0.0
    if ay <= vee_inner: return -a.vee
    return -(a.vee_outer - ay) * VEE_SLOPE
def profile(x):
    if x <= 0: return a.edge
    if x <= xp: return a.edge + x * tn
    return max(a.bottom, a.peak - (x - xp) * tt)
def lane(x, y):
    if a.rise <= 0: return 0.0
    ay = abs(y)
    c = a.rise if ay >= Rg else min(a.rise, Rg - math.sqrt(Rg * Rg - ay * ay))
    xs = lane_start + a.oval * (1 - math.sqrt(max(0.0, 1 - (ay / hw) ** 2)))   # the half-oval front
    t = 0.0 if x <= xs else 1.0 if x >= xs + a.lead else (x - xs) / a.lead
    return c * t
def top(x, y):
    xl = x - x_lip(y)                             # distance behind the local lip
    z = profile(xl) + lane(xl, y)
    if a.boss_h > 0 and abs(y) >= a.boss_in and abs(x - shaft_x) <= a.boss_len / 2: z = max(z, a.boss_h)
    return max(z, a.bottom + 0.002)               # never zero thickness: the knife edge is 0.05 mm, and the slivers past the local tail end are cut away

# the solid from the height field: a grid over the plan, top and bottom sheets, four walls
X_0, X_END = -a.vee, xe
xs = sorted(set(np.round(np.linspace(X_0, X_END, a.nx), 4)) | {round(xp, 4), round(xp - a.vee, 4), round(lane_start, 4), round(shaft_x - a.boss_len / 2, 4), round(shaft_x + a.boss_len / 2, 4), 0.0})
xs = [x for x in xs if X_0 <= x <= X_END]
ys = sorted(set(np.round(np.linspace(-hw, hw, a.ny), 4)) | {-a.boss_in, a.boss_in, -a.boss_in - 1e-3, a.boss_in + 1e-3, -vee_inner, vee_inner, -a.vee_outer, a.vee_outer})
ys = [y for y in ys if -hw <= y <= hw]
nx, ny = len(xs), len(ys)
V, F = [], []
def vid(i, j, which):   # which: 0 top, 1 bottom
    return which * nx * ny + i * ny + j
for which in (0, 1):
    for i, x in enumerate(xs):
        for j, y in enumerate(ys):
            V.append([x, y, top(x, y) if which == 0 else a.bottom])
for i in range(nx - 1):
    for j in range(ny - 1):
        t00, t10, t11, t01 = vid(i, j, 0), vid(i + 1, j, 0), vid(i + 1, j + 1, 0), vid(i, j + 1, 0)
        F += [[t00, t01, t11], [t00, t11, t10]]                       # top, normal up
        b00, b10, b11, b01 = vid(i, j, 1), vid(i + 1, j, 1), vid(i + 1, j + 1, 1), vid(i, j + 1, 1)
        F += [[b00, b11, b01], [b00, b10, b11]]                       # bottom, normal down
for j in range(ny - 1):                                               # the nose (x = 0) and the back edge (x = xe)
    F += [[vid(0, j, 1), vid(0, j + 1, 1), vid(0, j + 1, 0)], [vid(0, j, 1), vid(0, j + 1, 0), vid(0, j, 0)]]
    F += [[vid(nx - 1, j, 1), vid(nx - 1, j, 0), vid(nx - 1, j + 1, 0)], [vid(nx - 1, j, 1), vid(nx - 1, j + 1, 0), vid(nx - 1, j + 1, 1)]]
for i in range(nx - 1):                                               # the two sides (y = -hw, y = +hw)
    F += [[vid(i, 0, 1), vid(i, 0, 0), vid(i + 1, 0, 0)], [vid(i, 0, 1), vid(i + 1, 0, 0), vid(i + 1, 0, 1)]]
    F += [[vid(i, ny - 1, 1), vid(i + 1, ny - 1, 0), vid(i, ny - 1, 0)], [vid(i, ny - 1, 1), vid(i + 1, ny - 1, 1), vid(i + 1, ny - 1, 0)]]
body = trimesh.Trimesh(vertices=np.array(V) * IN, faces=np.array(F), process=True)
body.update_faces(body.nondegenerate_faces()); body.merge_vertices()
if not body.is_winding_consistent: body.fix_normals()
assert body.is_watertight, "the height-field solid is not closed"
if body.volume < 0: body.invert()
if a.vee > 0:
    # cut away what is in front of the V lip and behind the local tail end, on each side
    from trimesh.creation import extrude_polygon
    from shapely.geometry import Polygon
    for sgn in (1, -1):
        # in front of the local lip: nothing. The middle's lip is at -vee; the slants run to the sides' lip at 0
        front = Polygon([(X_0 - 1, sgn * vee_inner), (-a.vee, sgn * vee_inner), (0, sgn * a.vee_outer), (0, sgn * (hw + 1)), (X_0 - 1, sgn * (hw + 1))])
        back = Polygon([(xe, 0), (xe + 1, 0), (xe + 1, sgn * (hw + 1)), (xe, sgn * (hw + 1))])   # a straight back edge: the V is only at the nose
        for poly in (front, back):
            cutter = extrude_polygon(Polygon([(x * IN, y * IN) for x, y in poly.exterior.coords]), 3 * IN); cutter.apply_translation([0, 0, -1 * IN])
            body = body.difference(cutter)
    body.update_faces(body.nondegenerate_faces()); body.merge_vertices()
    assert body.is_watertight, "the V cuts did not leave a closed solid"
# the bore: a hex prism along y, flats top and bottom
bore = trimesh.creation.cylinder(radius=hex_corner * IN, height=(W + 1) * IN, sections=6)
bore.apply_transform(trimesh.transformations.rotation_matrix(_ang + math.pi / 6, [0, 0, 1]))   # +30 deg: flats top and bottom at angle 0
bore.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
bore.apply_translation([shaft_x * IN, 0, a.shaft_z * IN])
# where the top is too thin over the bore (under 0.8 mm), open it: the shaft's top flat becomes part of the surface
skin_centre = min(top(x, y) for x in np.linspace(shaft_x - hex_along, shaft_x + hex_along, 9) for y in np.linspace(-hw, hw, 25)) - (a.shaft_z + hex_up)
opened = False
if skin_centre * IN < 0.8 and a.boss_h > 0:
    slot = trimesh.creation.box(extents=[2 * hex_along * IN, 2 * (a.boss_in - 0.02) * IN, 2 * IN])
    slot.apply_translation([shaft_x * IN, 0, (a.shaft_z + hex_up + 1.0) * IN])
    bore = bore.union(slot); opened = True
body = body.difference(bore)
assert body.is_watertight
under = (a.shaft_z - hex_up - a.bottom) * IN
over_boss = (a.boss_h - a.shaft_z - hex_up) * IN if a.boss_h > 0 else 0
os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
# the same frame as divot_blade.py's parts: the nose at +x, the back edge at x = 0, the flat bottom on z = 0
body.apply_transform(trimesh.transformations.rotation_matrix(math.pi, [0, 0, 1]))
body.apply_translation([-body.bounds[0][0], 0, -body.bounds[0][2]])
body.export(os.path.join(HERE, "stl", a.name + ".stl"))
e = body.bounds[1] - body.bounds[0]
FW = lambda ang, mu: math.sin(math.radians(ang)) / (math.cos(math.radians(ang)) - mu * math.sin(math.radians(ang)))
edge_ang = 90 - math.degrees(math.asin((1.4 - a.edge) / 1.4))
print(f"stl/{a.name}.stl: {e[1]:.0f} x {e[0]:.0f} x {e[2]:.0f} mm, {body.volume / 1000:.0f} cm3")
assert skin_centre * IN >= 1.2 or opened, f"only {skin_centre * IN:.1f} mm of plastic over the bore somewhere across the width"
print(f"  profile: edge {a.edge:.2f}, {a.nose:.0f} deg nose to the peak {a.peak:.2f} at {xp:.2f} behind the lip, {a.tail:.0f} deg tail to the back edge at {xe:.2f}; "
      f"peak {1.18 + a.vee - xp - 0.35:.2f} in past the pinned POLLEN at the centre; back edge {1.18 - xe:.2f} in from the FLOWER's centre")
if a.vee > 0: print(f"  V nose: the middle {2 * vee_inner:.2f} in of the lip is {a.vee:.2f} in deeper than the sides, the slants from |y| {vee_inner:.2f} to {a.vee_outer:.2f} lie on the uprights' lobes")
print(f"  push to lift the stack: {FW(a.nose, 0.4):.1f}x its weight on the face, {FW(edge_ang, 0.4):.1f}x at first contact (rolling, uprights mu 0.4)")
if a.rise > 0: print(f"  lane: {a.rise:.2f} in up at the edges, radius {Rg:.1f}; begins {lane_start:.2f} behind the lip at the centre, {lane_start + a.oval:.2f} at the edges (oval front), full depth after {a.lead:.1f} in")
print(f"  shaft: {a.hex} mm hex (flats) turned {a.hex_angle:.0f} deg, at {shaft_x:.2f} behind the sides' lip ({shaft_x + a.vee:.2f} behind the middle's), {a.shaft_z:.2f} high; {under:.1f} mm under it; centre skin over it {skin_centre * IN:.1f} mm" + (" -> opened, the top flat is flush with the surface" if opened else "") + (f"; end bosses {a.boss_in:.2f}..{hw:.2f} from the centre, {over_boss:.1f} mm over the bore" if a.boss_h > 0 else "; no bosses"))

import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
m = body.copy(); m.apply_translation(-m.bounds[0]); v = m.vertices / IN; f = m.faces
Lt = np.array([0.3, -0.6, 0.75]); Lt /= np.linalg.norm(Lt); k = 0.35 + 0.65 * np.clip(m.face_normals @ Lt, 0, 1)
col = np.c_[np.outer(k, [0.95, 0.55, 0.12]), np.ones(len(k))]
fig = plt.figure(figsize=(14, 5))
for i, (el, az, tt_) in enumerate(((28, -55, "3D: the nose, the peak, the lane, the end bosses"), (0, -90, f"Side: {a.nose:.0f} deg nose, peak {a.peak:.2f}, {a.tail:.0f} deg tail"), (90, -90, "Top: the lane's oval front, the bosses"))):
    ax = fig.add_subplot(1, 3, i + 1, projection="3d")
    ax.add_collection3d(Poly3DCollection(v[f], facecolors=col, edgecolors="none"))
    ee = v.max(0); ax.set_xlim(0, ee[0]); ax.set_ylim(0, ee[1]); ax.set_zlim(0, ee[2]); ax.set_box_aspect(ee)
    ax.view_init(el, az); ax.set_title(tt_, fontsize=10); ax.tick_params(labelsize=7)
fig.suptitle(f"{a.name}: {e[1] / IN:.2f} in wide, {e[0] / IN:.2f} in front to back; nose {a.nose:.0f} deg, peak {a.peak:.2f}, lane {a.rise:.2f}, bosses to {a.boss_h:.2f}", fontsize=11)
fig.tight_layout(); fig.savefig(os.path.join(HERE, a.name + ".png"), dpi=110)
