"""The whole robot as an AdvantageScope model: Robot_BIOBUZZ/{model.glb, model_0.glb, model_1.glb, config.json}.

AdvantageScope's robot frame (as TeamCode's simulator RobotAssets uses it): +X forward, +Y left, +Z up, metres,
origin on the floor under the point Pedro tracks (here: the chassis frame's centre, 7.56 in behind the front face).

    python3 cad/advantagescope/build_model.py ROBOT_MESH_PKL ADDON_MESH_PKL [POD_MESH_PKL]

ROBOT_MESH_PKL is tools/robot-cad/slim.py's keep.pkl (the team's robot STEP, 143 MB, in Drive). ADDON_MESH_PKL is
cad/intake-b/build.py's MESH_OUT. Components: model_0 is the FLOWER extractor, drawn deployed; model_1 is the roller,
its motor and carriage, drawn down (it floats straight up to 1.3 in).
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

def merged(meshes):
    """One mesh per colour keeps the file small and quick to draw."""
    by = {}
    for m in meshes: by.setdefault(tuple(m.visual.material.baseColorFactor), []).append(m)
    s = trimesh.Scene()
    for i, (col, ms) in enumerate(by.items()):
        s.add_geometry(trimesh.util.concatenate(ms), geom_name=f"part_{i}")
    return s

def main(robot_pkl, addon_pkl, pod_pkl=None):
    keep = pickle.load(open(robot_pkl, "rb"))
    add = pickle.load(open(addon_pkl, "rb"))
    base = []
    for path, v, f, col in keep:                      # the team's robot, in inches as slim.py keeps it
        if re.search(r"Intake <1> / (48mm Gecko|240mm Steel|5000|5103|5203|Pattern Spacer)", path): continue   # the old roller and motor, replaced
        base.append(mesh(np.asarray(v) * IN, f, colour_of(path), decimate=0.08))
    for n, m in add.items():
        if m["grp"] in ("fixed", "vee") and "STAND-IN" not in n:
            base.append(mesh(m["v"], m["f"], m["col"], 0.6 if "polycarbonate" in n else 1.0))
    if pod_pkl:
        for n, m in pickle.load(open(pod_pkl, "rb")).items():
            if m["grp"] == "pod": base.append(mesh(m["v"], m["f"], m["col"], decimate=0.05))
    else:
        for n, m in add.items():
            if "STAND-IN" in n: base.append(mesh(m["v"], m["f"], m["col"]))
    ext = [mesh(m["v"], m["f"], m["col"]) for n, m in add.items() if m["grp"] == "hook"]
    flt = [mesh(m["v"], m["f"], m["col"]) for n, m in add.items() if m["grp"] == "float"]
    os.makedirs(OUT, exist_ok=True)
    merged(base).export(os.path.join(OUT, "model.glb"))
    merged(ext).export(os.path.join(OUT, "model_0.glb"))
    merged(flt).export(os.path.join(OUT, "model_1.glb"))
    # The Limelight (Limelight Localization chat, 19429's measured mount): lens on the centreline 4.0 in ahead of the
    # chassis centre and 14.0 in up, pitched 45 deg up, yaw 0; Limelight 3A, 640 x 480, 54.5 deg across.
    camera = {"name": "Limelight", "rotations": [{"axis": "y", "degrees": -45.0}, {"axis": "z", "degrees": 0.0}],
              "position": [round(4.0 * M, 5), 0.0, round(14.0 * M, 5)], "resolution": [640, 480], "fov": 54.5}
    config = {"name": NAME, "isFTC": True, "rotations": [], "position": [0, 0, 0], "cameras": [camera],
              "components": [{"zeroedRotations": [], "zeroedPosition": [0, 0, 0]}, {"zeroedRotations": [], "zeroedPosition": [0, 0, 0]}]}
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
               "poses": poses, "model_1": "the roller and its motor: translation [0, 0, rise] with rise 0 (down, as drawn) to 0.03302 m (1.3 in)"},
              open(os.path.join(OUT, "extractor_poses.json"), "w"), indent=2)
    for fn in ("model.glb", "model_0.glb", "model_1.glb"):
        s = trimesh.load(os.path.join(OUT, fn)); print(fn, os.path.getsize(os.path.join(OUT, fn)) // 1000, "kB, bounds (m)", np.round(s.bounds, 3).tolist())

if __name__ == "__main__":
    main(*sys.argv[1:])
