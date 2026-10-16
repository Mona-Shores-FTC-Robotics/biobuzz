"""The full-width ramp: the v2 blade's profile (thin lip, 45 deg nose, ridge, slope down to a floor plate) as a bent
sheet the width of the intake, with a roller over the plate. For the viewer and the section figure, not for a printer:
at this size it is bent aluminium or polycarbonate, or grid plates.

    python3 cad/modules/flower-stick-proto/full_ramp.py            # writes stl/full_ramp.stl and the scene STLs

The profile and the roller come from tools/ramp-hook/full_ramp_sim.py, so what the simulator ran is what is drawn.
The scene puts the ramp at its stop in a simplified FLOWER (tools/robot-cad/flower.py, measured from the field CAD)
with the four POLLEN where the simulator's trace has them the instant the lip reaches the stop, in the FLOWER's frame:
inches, origin on the tile under the bottom POLLEN's centre, +x toward the wall, z up."""
import importlib.util, json, math, os
import numpy as np, trimesh
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", "..", ".."))
IN = 25.4


def load(name, path):
    spec = importlib.util.spec_from_file_location(name, path); m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m); return m


sim = load("full_ramp_sim", os.path.join(ROOT, "tools", "ramp-hook", "full_ramp_sim.py"))
flower = load("flower", os.path.join(ROOT, "tools", "robot-cad", "flower.py"))
WIDTH, SHEET, PLATE_LEN = 12.0, 0.10, 6.0


def ramp_mesh():
    """The sheet, x behind the lip (negative: toward the robot is -x in the FLOWER frame, so build in +x then flip)."""
    top = [(x, z) for x, z in sim.PROFILE[1:] if x <= PLATE_LEN] + [(PLATE_LEN, sim.PLATE)]   # the lip is the sheet's cut edge
    bottom = [(x, z - SHEET) for x, z in reversed(top)]
    poly = top + bottom
    from shapely.geometry import Polygon
    m = trimesh.creation.extrude_polygon(Polygon(poly), WIDTH)          # extruded along +z of the polygon's plane
    m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))   # (x, z) -> (x, _, z), width along -y
    m.apply_translation([0, WIDTH / 2, 0])
    m.apply_transform(np.diag([-1, 1, 1, 1]))                            # behind the lip is toward the robot: -x
    m.fix_normals()
    return m


def roller_mesh():
    r = trimesh.creation.cylinder(radius=sim.ROLLER_R, height=WIDTH, sections=32)
    r.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    return r


def scene():
    """Ramp at the stop, roller, simplified FLOWER (cut at 8 in), four POLLEN from the simulator's trace."""
    stop = 1.18
    ramp = ramp_mesh(); ramp.apply_translation([stop, 0, 0])
    roller = roller_mesh(); roller.apply_translation([stop - sim.ROLLER_X, 0, sim.PLATE + 2 * sim.R - sim.SQUEEZE + sim.ROLLER_R])
    parts = flower.parts(0.0)
    xc = 7.56                                            # flower.parts puts the centre at 7.56 + d in the robot's frame
    fl = []
    for k, m in parts.items():
        if k == "field_wall":
            continue
        m = m.copy(); m.apply_translation([-xc, 0, 0])
        if k.startswith("flower_post"):
            m = m.slice_plane([0, 0, 8.0], [0, 0, -1], cap=True)
        fl.append(m)
    flower_mesh = trimesh.util.concatenate(fl)
    trace_path = os.path.join(ROOT, "tools", "ramp-hook", "full_ramp_trace.json")
    balls = []
    if os.path.exists(trace_path):
        d = json.load(open(trace_path))
        frames = [f for f in d["trace"] if f[1] <= d["tip_stop"] + 1e-6]
        f = frames[0] if frames else d["trace"][-1]      # the instant the lip reaches the stop: the bottom POLLEN on the ridge
        for (xs, zs) in f[2]:
            b = trimesh.creation.icosphere(subdivisions=3, radius=sim.R)
            b.apply_translation([d["cx"] - xs, 0, zs]); balls.append(b)       # sim x -> FLOWER x: x_f = CX - x_sim
    return ramp, roller, flower_mesh, (trimesh.util.concatenate(balls) if balls else None)


if __name__ == "__main__":
    os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
    ramp, roller, fl, balls = scene()
    for name, m in (("full_ramp", ramp), ("scene_roller", roller), ("scene_flower", fl), ("scene_pollen", balls)):
        if m is None:
            continue
        mm = m.copy(); mm.apply_scale(IN)
        path = os.path.join(HERE, "stl", f"{name}.stl"); mm.export(path)
        b = mm.bounds
        print(f"{os.path.relpath(path)}: {b[1][0]-b[0][0]:.0f} x {b[1][1]-b[0][1]:.0f} x {b[1][2]-b[0][2]:.0f} mm, "
              f"x {b[0][0]:.0f}..{b[1][0]:.0f}, z {b[0][2]:.0f}..{b[1][2]:.0f}, watertight {mm.is_watertight}")
