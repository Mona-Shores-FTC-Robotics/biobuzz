"""The whole robot as an AdvantageScope model: Robot_BIOBUZZ/{model.glb, model_0.glb, model_1.glb, config.json}.

AdvantageScope's robot frame (as TeamCode's simulator RobotAssets uses it): +X forward, +Y left, +Z up, metres,
origin on the floor under the point Pedro tracks (here: the chassis frame's centre, 7.56 in behind the front face).

    python3 cad/advantagescope/build_model.py ROBOT_MESH_PKL ADDON_MESH_PKL [POD_MESH_PKL [TRANSFER_MESH_PKL]]

ROBOT_MESH_PKL is tools/robot-cad/slim.py's keep.pkl (the team's robot STEP, 143 MB, in Drive). ADDON_MESH_PKL is
cad/intake-b/build.py's MESH_OUT; TRANSFER_MESH_PKL (optional, 4th) is cad/transfer/build.py's. Components: model_0 is the FLOWER extractor, drawn deployed; model_1 is the roller,
its motor and carriage, drawn down (it floats straight up to 1.3 in); model_2 is the turret, facing forward; model_3 is the
transfer's J-wheel on its arms, at rest.
"""
import json, math, os, pickle, re, sys
import numpy as np, trimesh, fast_simplification

IN, M = 25.4, 0.0254
C, F, FACE = -59.62, -151.75, 207.73                 # robot CAD (mm): centre x, floor y, front face z
CENTRE_BACK_IN = 7.56                                 # chassis centre, inches behind the front face: half the rails'
                                                      # 384 mm (15.12 in) length, midway between the wheel axles (1.89, 13.23)
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "Robot_BIOBUZZ")
NAME = "BIOBUZZ Robot"

def cad_to_as(v):
    """Robot CAD mm (x left, y up, z forward) -> AdvantageScope m (X forward, Y left, Z up)."""
    v = np.asarray(v, float)
    return np.c_[(v[:, 2] - (FACE - CENTRE_BACK_IN * IN)) / 1000, (v[:, 0] - C) / 1000, (v[:, 1] - F) / 1000]

def colour_of(name):
    for pat, c in ((r"Nectar", (0.82, 0.23, 0.18)), (r"Gecko", (0.35, 0.66, 0.31)), (r"3606|roller", (0.22, 0.24, 0.27)),
                   (r"belt|5000|5103|5203|motor|body|Gearbox", (0.19, 0.2, 0.23)), (r"Shaft|shaft|REX", (0.82, 0.84, 0.86))):
        if re.search(pat, name): return c
    return (0.67, 0.7, 0.74)

def mesh(v, f, rgb, alpha=1.0, decimate=None):
    v, f = np.asarray(v, np.float32), np.asarray(f, np.int32)
    if decimate and len(f) > 400:
        m = trimesh.Trimesh(v, f, process=True); m.merge_vertices(digits_vertex=4)
        tgt = max(40, int(len(m.faces) * decimate))
        if tgt < len(m.faces):
            try: v, f = fast_simplification.simplify(m.vertices.astype(np.float32), m.faces.astype(np.int32), target_reduction=1 - tgt / len(m.faces))
            except Exception: v, f = m.vertices, m.faces
    t = trimesh.Trimesh(cad_to_as(v), f, process=False)
    t.visual = trimesh.visual.TextureVisuals(material=trimesh.visual.material.PBRMaterial(
        baseColorFactor=[int(255 * c) for c in rgb] + [int(255 * alpha)], metallicFactor=0.2, roughnessFactor=0.6,
        alphaMode="BLEND" if alpha < 1 else "OPAQUE", doubleSided=True))
    return t

# AdvantageScope's loader (OptimizeGeometries.ts) drops any mesh without a NORMAL attribute, so every export
# below passes include_normals=True; without it the whole robot drew blank (6 Oct 2026).
def merged(meshes):
    """One mesh per colour keeps the file small and quick to draw."""
    by = {}
    for m in meshes: by.setdefault(tuple(m.visual.material.baseColorFactor), []).append(m)
    s = trimesh.Scene()
    for i, (col, ms) in enumerate(by.items()):
        s.add_geometry(trimesh.util.concatenate(ms), geom_name=f"part_{i}")
    return s

def as_in_to_cad(p):
    """AdvantageScope inches (X forward, Y left, Z up) -> robot CAD mm."""
    p = np.asarray(p, float)
    return np.c_[C + p[:, 1] * IN, F + p[:, 2] * IN, FACE - CENTRE_BACK_IN * IN + p[:, 0] * IN]

def limelight():
    """The Limelight 3A where config.json's camera is (lens 4.0 in ahead, 14.0 in up, pitched 45 deg up), on a stand-in
    mount: a 16 mm beam between the two front towers' tops and a plate with a 45 deg printed wedge under the camera.
    The body is about 3.5 x 2.4 x 0.95 in; the mount is drawn only to show where it goes, below the camera's view."""
    lens, a = np.array([4.0, 0.0, 14.0]), math.radians(45)
    n, u = np.array([math.cos(a), 0, math.sin(a)]), np.array([-math.sin(a), 0, math.cos(a)])   # view direction, camera up
    W, H, D = 3.5 / 2, 2.4 / 2, 0.95 / 2
    c = lens - n * D
    body = np.array([c + sy * W * np.array([0, 1, 0]) + sh * H * u + sd * D * n for sy in (-1, 1) for sh in (-1, 1) for sd in (-1, 1)])
    glass = np.array([lens + n * s1 * 0.04 + np.array([0, sy * 0.35, 0]) + u * sh * 0.35 for s1 in (0, 1) for sy in (-1, 1) for sh in (-1, 1)])
    bot = c - H * u                                                  # the middle of the camera's bottom face
    edge = [bot + sd * D * n for sd in (-1, 1)]
    wedge = np.array([e + np.array([0, sy * 1.2, 0]) for e in edge for sy in (-1, 1)] +
                     [np.array([x, sy * 1.2, 12.25]) for x in (min(e[0] for e in edge), 5.4) for sy in (-1, 1)])
    def boxpts(lo, hi): return np.array([[x, y, z] for x in (lo[0], hi[0]) for y in (lo[1], hi[1]) for z in (lo[2], hi[2])])
    out = []
    for pts, rgb in ((body, (0.13, 0.14, 0.16)), (glass, (0.05, 0.05, 0.06)), (wedge, (0.18, 0.37, 0.62)),
                     (boxpts([4.2, -1.2, 12.13], [6.9, 1.2, 12.25]), (0.18, 0.37, 0.62)), (boxpts([6.3, -4.85, 11.5], [6.9, 4.85, 12.13]), (0.67, 0.7, 0.74))):
        h = trimesh.convex.convex_hull(pts)
        out.append(mesh(as_in_to_cad(h.vertices), h.faces, rgb))
    return out

def transfer():
    """The intake-to-turret transfer (transfer chat, issue #164, doc/transfer.md on spike/164-transfer) as placeholder
    solids from its envelope boxes, until it has real parts. AdvantageScope inches, drawn fixed."""
    from shapely.geometry import Polygon, Point
    from shapely.ops import unary_union
    PURPLE, GREEN, DARK, GREY = (0.55, 0.47, 0.75), (0.35, 0.66, 0.31), (0.19, 0.2, 0.23), (0.67, 0.7, 0.74)
    out = []
    def put(h, rgb): out.append(mesh(as_in_to_cad(h.vertices), h.faces, rgb))
    def boxm(lo, hi): return trimesh.creation.box(bounds=[lo, hi])
    def xz_solid(poly, y0, y1):
        """A shape drawn in the X-Z plane, extruded across Y from y0 to y1."""
        h = trimesh.creation.extrude_polygon(poly, y1 - y0)          # polygon in (X, Z), extruded along its +z
        v = h.vertices.copy(); h.vertices = np.c_[v[:, 0], v[:, 2] + y0, v[:, 1]]
        h.invert() if h.volume < 0 else None
        return h
    def ycyl(cx, cz, r, y0, y1):
        h = trimesh.creation.cylinder(radius=r, height=y1 - y0, sections=32)
        h.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0])); h.apply_translation([cx, (y0 + y1) / 2, cz]); return h
    # lane: floor at 0.9 (0.1 thick) and walls 0.06 thick at Y +-2.1, X -1.3..7.2, up to 5.0; notched to 3.4 over the
    # drive motors' encoder caps (X 2.3..4.0) and to 4.7 under the raised 11-hole channel (X 4.95..5.6)
    put(boxm([-1.3, -2.16, 0.8], [7.2, 2.16, 0.9]), PURPLE)
    for s in (-1, 1):
        y0, y1 = sorted((s * 2.1, s * 2.16))
        for x0, x1, top in ((-1.3, 2.3, 5.0), (2.3, 4.0, 3.4), (4.0, 4.95, 5.0), (4.95, 5.6, 4.7), (5.6, 7.2, 5.0)):
            put(boxm([x0, y0, 0.9], [x1, y1, top]), PURPLE)
    # ramp: from (8.0, 0.05) down to the lane floor at (5.8, 0.9)
    a, b = np.array([8.0, 0.05]), np.array([5.8, 0.9]); d = (b - a) / np.linalg.norm(b - a); nrm = np.array([-d[1], d[0]]) * 0.06
    put(xz_solid(Polygon([a, b, b + nrm, a + nrm]), -2.1, 2.1), PURPLE)
    # J-wheel (48 mm, 2 in wide) on its axle at (-1.32, 4.54), arms to the pivot at (0.85, 3.29)
    put(ycyl(-1.32, 4.54, 0.945, -1.0, 1.0), GREEN)
    for s in (-1, 1):
        ax, pv = np.array([-1.32, 4.54]), np.array([0.85, 3.29]); d = (pv - ax) / np.linalg.norm(pv - ax); nrm = np.array([-d[1], d[0]]) * 0.25
        put(xz_solid(Polygon([ax - nrm, pv - nrm, pv + nrm, ax + nrm]), s * 1.06 - 0.06, s * 1.06 + 0.06), GREY)
    # outer J and chute: a 3.64 in arc about the axle from the lane floor round to the back, then a wall at X -4.96 to 6.6
    c, r0, r1 = np.array([-1.32, 4.54]), 3.64, 3.70
    arc = lambda r: [c + r * np.array([math.cos(q), math.sin(q)]) for q in np.linspace(-math.pi / 2, -math.pi, 24)]
    shell = Polygon(arc(r1) + [np.array([-4.96 - 0.06, 6.6]), np.array([-4.96, 6.6])] + arc(r0)[::-1])
    put(xz_solid(shell.buffer(0), -2.2, 2.2), PURPLE)
    # countershaft pulley (24 mm) at (6.0, 4.0), Y +2.6; the J motor (any spot in its box: drawn along X in it)
    put(ycyl(6.0, 4.0, 0.47, 2.5, 2.7), DARK)
    h = trimesh.creation.cylinder(radius=0.73, height=4.7, sections=24)
    h.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 1, 0])); h.apply_translation([-1.6, 3.6, 2.4]); put(h, DARK)
    # turret bearing: a ring, 105 mm (4.13 in) inside, 6.6..7.8 up, centred on (-3.17, 0)
    ring = trimesh.creation.annulus(r_min=4.13 / 2, r_max=5.5 / 2, height=1.2, sections=48); ring.apply_translation([-3.17, 0, 7.2]); put(ring, GREY)
    return out

def main(robot_pkl, addon_pkl, pod_pkl=None, transfer_pkl=None):
    keep = pickle.load(open(robot_pkl, "rb"))
    add = pickle.load(open(addon_pkl, "rb"))
    base, turret = [], []
    for path, v, f, col in keep:                      # the team's robot, in inches as slim.py keeps it
        if re.search(r"Intake <1> / (48mm Gecko|240mm Steel|5000|5103|5203|Pattern Spacer|1201-0043)", path): continue   # the old roller and motor, replaced
        if re.search(r"Nectar|Pollen", path): continue   # game pieces staged in the CAD: the simulator draws the ones the robot holds
        lift = [0, 8.0, 0] if "Intake <1> / 11 Hole Lowside" in path else [0, 0, 0]   # raised 8 mm for the transfer's lane (doc/transfer.md)
        if path.startswith("Launcher Concept"): continue   # dropped (the mentor, 6 Oct): the turret's launcher replaces it
        base.append(mesh(np.asarray(v) * IN + lift, f, colour_of(path), decimate=0.08))
    for n, m in add.items():
        if m["grp"] in ("fixed", "vee") and "STAND-IN" not in n:
            base.append(mesh(m["v"], m["f"], m["col"], 0.6 if "polycarbonate" in n else 1.0))
    if pod_pkl:
        for n, m in pickle.load(open(pod_pkl, "rb")).items():
            if m["grp"] == "pod": base.append(mesh(m["v"], m["f"], m["col"], decimate=0.05))
    else:
        for n, m in add.items():
            if "STAND-IN" in n: base.append(mesh(m["v"], m["f"], m["col"]))
    base += limelight()
    tr = pickle.load(open(transfer_pkl, "rb")) if transfer_pkl else None
    if tr is None: base += transfer()                 # placeholder solids until cad/transfer/ exists
    else:
        base += [mesh(m["v"], m["f"], m["col"]) for n, m in tr.items() if m["grp"] == "fixed" and not n.startswith("turret_bearing")]
        turret += [mesh(m["v"], m["f"], m["col"]) for n, m in tr.items() if n.startswith("turret_bearing")]
    ext = [mesh(m["v"], m["f"], m["col"]) for n, m in add.items() if m["grp"] == "hook"]
    flt = [mesh(m["v"], m["f"], m["col"]) for n, m in add.items() if m["grp"] == "float"]
    if tr: flt += [mesh(m["v"], m["f"], m["col"]) for n, m in tr.items() if m["grp"] == "float"]
    jarm = [mesh(m["v"], m["f"], m["col"]) for n, m in tr.items() if m["grp"] == "arm"] if tr else []
    os.makedirs(OUT, exist_ok=True)
    merged(base).export(include_normals=True, file_obj=os.path.join(OUT, "model.glb"))
    merged(ext).export(include_normals=True, file_obj=os.path.join(OUT, "model_0.glb"))
    merged(flt).export(include_normals=True, file_obj=os.path.join(OUT, "model_1.glb"))
    merged(turret).export(include_normals=True, file_obj=os.path.join(OUT, "model_2.glb"))
    if jarm: merged(jarm).export(include_normals=True, file_obj=os.path.join(OUT, "model_3.glb"))
    # The Limelight (Limelight Localization chat, 19429's measured mount): lens on the centreline 4.0 in ahead of the
    # chassis centre and 14.0 in up, pitched 45 deg up, yaw 0; Limelight 3A, 640 x 480, 54.5 deg across.
    camera = {"name": "Limelight", "rotations": [{"axis": "y", "degrees": -45.0}, {"axis": "z", "degrees": 0.0}],
              "position": [round(4.0 * M, 5), 0.0, round(14.0 * M, 5)], "resolution": [640, 480], "fov": 54.5}
    # disableSimplification: AdvantageScope otherwise decimates a model and drops meshes by rendering mode, and
    # this one (plain part names, no NOSIMPLIFY) came out blank on the field (6 Oct 2026).
    config = {"name": NAME, "isFTC": True, "disableSimplification": True, "rotations": [], "position": [0, 0, 0], "cameras": [camera],
              "components": [{"zeroedRotations": [], "zeroedPosition": [0, 0, 0]} for _ in range(4 if jarm else 3)]}
    json.dump(config, open(os.path.join(OUT, "config.json"), "w"), indent=2)
    # the extractor's poses: it turns about its own shaft (+Y through PIVOT, 2.4 in ahead of the face and 4.5 in up);
    # front up = rotation about +Y by -angle
    pivot = np.array([(CENTRE_BACK_IN + 2.4) * M, 0.0, 4.5 * M])
    poses = {}
    for name, deg in (("deployed", 0.0), ("stowed", 150.0)):
        a = math.radians(-deg); R = np.array([[math.cos(a), 0, math.sin(a)], [0, 1, 0], [-math.sin(a), 0, math.cos(a)]])
        t = pivot - R @ pivot
        poses[name] = {"angle_deg": deg, "translation_m": [round(x, 5) for x in t],
                       "rotation_quaternion_wxyz": [round(math.cos(a / 2), 6), 0.0, round(math.sin(a / 2), 6), 0.0]}
    json.dump({"component": "model_0: FLOWER extractor", "pivot_m": [round(x, 5) for x in pivot], "axis": "+Y",
               "note": "pose = rotation about +Y by -angle about the pivot; 0 deg deployed (as drawn), 150 deg stowed",
               "poses": poses, "model_1": "the roller and its motor: translation [0, 0, rise] with rise 0 (down, as drawn) to 0.03302 m (1.3 in)",
               "model_2": "the turret (for now only its bearing; the launcher is to come): rotation about +Z by the yaw about (-0.080518, 0) m (X -3.17 in); positive yaw turns left; 0 = facing forward, as drawn",
               "model_3": "the transfer's J-wheel and arms: rotation about +Y by +angle about (0.018288, 0, 0.085344) m (the arm's pivot, X 0.72 in, Z 3.36 in); 0 at rest on its stops (the arm 30 deg above horizontal toward the rear); 36.8 deg is full float, the axle 0.99 in up (a NECTAR at the mouth lifts it about 0.92 in, about 34 deg; a POLLEN barely)"},
              open(os.path.join(OUT, "extractor_poses.json"), "w"), indent=2)
    for fn in ("model.glb", "model_0.glb", "model_1.glb", "model_2.glb") + (("model_3.glb",) if jarm else ()):
        s = trimesh.load(os.path.join(OUT, fn)); print(fn, os.path.getsize(os.path.join(OUT, fn)) // 1000, "kB, bounds (m)", np.round(s.bounds, 3).tolist())

if __name__ == "__main__":
    main(*sys.argv[1:])
