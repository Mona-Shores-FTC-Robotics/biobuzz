"""The viewer's FLOWER (page/flower3d.b64) from FIRST's field CAD: the base plate and the collar as extruded outlines
with their measured openings, the two posts and the four pipes as the CAD's own meshes (pipes cut at 12 in), and the
wall. Needs flower_cad.pkl from tools/robot-cad/flower_from_field_cad.py."""
import pickle, base64, trimesh
from shapely.geometry import Polygon, Point
from scipy.spatial import ConvexHull
d = pickle.load(open("flower_cad.pkl", "rb"))
M = {k: (v if isinstance(v, trimesh.Trimesh) else trimesh.Trimesh(*v, process=False)) for k, v in d.items()}
def hull_at(m, z):
    s = m.section(plane_origin=[0, 0, z], plane_normal=[0, 0, 1]); v = s.vertices[:, :2]; h = ConvexHull(v); return Polygon(v[h.vertices])
def extrude(poly, z0, z1):
    e = trimesh.creation.extrude_polygon(poly, z1 - z0); e.apply_translation([0, 0, z0]); return e
bodies = [extrude(hull_at(M["am-5859: Flower Layer X"], 0.2).difference(Point(0, 0).buffer(1.52, 64)), 0.0, 0.35)]
pb = hull_at(M["am-5858: Flower Layer B"], 4.2)
bodies += [extrude(pb.difference(Point(0, 0).buffer(1.93, 64)), 3.90, 4.10), extrude(pb.difference(Point(0, 0).buffer(1.71, 64)), 4.10, 4.25)]
for k, m in M.items():
    if "Peanut Support" in k: bodies.append(m.copy())
    if "HIPS Pipe" in k: bodies.append(m.slice_plane([0, 0, 12.0], [0, 0, -1]))
bodies.append(trimesh.creation.box(extents=[0.5, 14, 12]).apply_translation([2.71 + 0.25, 0, 6]))
new = trimesh.util.concatenate(bodies); new.export("flower3d_new.stl")
open("flower3d.b64", "w").write(base64.b64encode(open("flower3d_new.stl", "rb").read()).decode())
print(len(new.faces), "faces")
