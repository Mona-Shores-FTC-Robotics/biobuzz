"""Drive the robot's front (cad/intake-b/, extractor deployed) into the FLOWER (flower.py) and report what touches
first, for a range of sideways offsets and headings. Run in a folder holding front_mesh.pkl.

    python3 flower_seat.py

The block's curved tip should be first, on the grey uprights, with the FLOWER's centre 4.59 in ahead of the face.
Anything else touching first means the robot stops short or hits the FLOWER."""
import pickle, math, numpy as np, trimesh, manifold3d as mf, sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__))); import flower
C, F, FACE, IN = -59.62, -151.75, 207.73, 25.4
def asmesh(m):
    v = np.asarray(m["v"], float); A = np.c_[(v[:, 2] - FACE) / IN + 7.56, (v[:, 0] - C) / IN, (v[:, 1] - F) / IN]
    t = trimesh.Trimesh(A, np.asarray(m["f"])); t.merge_vertices(); return t
def man(t): return mf.Manifold(mf.Mesh(vert_properties=np.asarray(t.vertices, np.float32), tri_verts=np.asarray(t.faces, np.uint32)))
M = pickle.load(open("front_mesh.pkl", "rb"))
R = {n.split(" ")[0]: man(asmesh(m)) for n, m in M.items() if m["grp"] in ("fixed", "vee", "float", "hook") and "STAND-IN" not in n}
def first_contact(dy, yaw_deg):
    """Step the FLOWER toward the robot (equivalently, the robot forward) until something touches."""
    for d in np.arange(8.0, 3.5, -0.02):
        hits = []
        for fn, ft in flower.parts(d).items():
            ft = ft.copy(); ft.apply_translation([0, dy, 0])
            ft.apply_transform(trimesh.transformations.rotation_matrix(math.radians(-yaw_deg), [0, 0, 1]))   # robot yawed +yaw == FLOWER yawed -yaw about the robot
            fm = man(ft)
            for n, rm in R.items():
                if (fm ^ rm).volume() > 1e-5: hits.append((fn.replace("flower_", ""), n))
        if hits: return round(d, 2), sorted(set(hits))
    return None, []
print("FLOWER centre ahead of the face at first contact (ideal: 4.59, the tip on the uprights)")
for dy in (0.0, 0.5, 0.75, 1.0, 1.25):
    for yaw in (0.0, 2.0, -2.0):
        d, h = first_contact(dy, yaw)
        print(f"  offset {dy:+.2f} in, heading {yaw:+.0f} deg: d = {d}  {h}")
