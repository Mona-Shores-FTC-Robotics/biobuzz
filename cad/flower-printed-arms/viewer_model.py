"""Builds the 3D viewer's model: one .glb with every part named, in the robot frame (metres, +X forward, +Y left,
+Z up), the stick drawn stowed (0 deg) as cad/advantagescope's model_1.glb is.

    python3 cad/flower-printed-arms/viewer_model.py OUT.glb

Node names say what each part is and what moves:
- `stick/...`  turns with the stick about its pivot axis (the viewer's slider), today's parts from the CAD;
- `channel/...` today's goBILDA 11 Hole Mini U-Channels (turn with the stick; shown or hidden against the printed arms);
- `printed/...` the printed arms (turn with the stick);
- `pivot/...`  the robot's parts on the pivot axis (the stick's servo, its block, the left stub shaft and its hub);
- `corner/...` the corner wheels (SWYFT's STEP, two per side) and the placed servo and mount;
- `camera/...` the Limelight 3A (its view is drawn by the viewer: 54.5 x 42 deg, pitched 45 deg up);
- `robot/...`  the rest of the robot's front, thinned, for context.
"""
import os
import sys

import numpy as np
import trimesh

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from check import corner_solids, placement  # noqa: E402
from geometry import PIVOT_XZ  # noqa: E402

MODEL = os.path.join(HERE, "..", "advantagescope", "Robot_BIOBUZZ")
PIVOT_PARTS = ("Servo - Speed (SWYFT)", "Compact Servo Block", "32mm Steel Shaft", "Sonic Hub", "Mini Quad Block",
               "8t Press Fit Sprocket")


def nodes(path):
    s = trimesh.load(path)
    for node in s.graph.nodes_geometry:
        T, g = s.graph[node]
        m = s.geometry[g].copy()
        m.apply_transform(T)
        yield node, m


def solid_mesh(shape, scale=0.001):
    v, f = shape.tessellate(0.4, 0.3)
    return trimesh.Trimesh(np.array([[p.x, p.y, p.z] for p in v]) * scale, f)


def main(out):
    scene = trimesh.Scene()
    used = {}

    def add(name, mesh):
        k = used.get(name, 0)
        used[name] = k + 1
        scene.add_geometry(mesh, node_name=name if k == 0 else f"{name} {k + 1}", geom_name=f"g{len(used)}_{k}")

    for node, m in nodes(os.path.join(MODEL, "model_1.glb")):
        label = node.rsplit(" <", 1)[0]
        add(("channel/" if "Mini U-Channel" in node else "stick/") + label, m)
    robot = []
    for node, m in nodes(os.path.join(MODEL, "model.glb")):
        leaf = node.split(" / ")[-1]
        v = m.vertices * 1000
        on_axis = np.hypot(v[:, 0] - PIVOT_XZ[0], v[:, 2] - PIVOT_XZ[1]).min() < 25
        if "Limelight" in leaf:
            add("camera/Limelight 3A", m)
            continue
        if on_axis and any(leaf.startswith(p) for p in PIVOT_PARTS):
            add("pivot/" + leaf.rsplit(" <", 1)[0], m)
            continue
        c = m.triangles_center
        keep = (c[:, 0] > -0.05) & (np.abs(c[:, 1]) < 0.21) & (c[:, 2] < 0.32)
        if keep.any():
            robot.append(m.submesh([np.nonzero(keep)[0]], append=True))
    body = trimesh.util.concatenate(robot)
    try:
        body = body.simplify_quadric_decimation(face_count=70000)
    except Exception:
        pass
    add("robot/front of the robot (31 Oct CAD)", body)
    arm = trimesh.load(os.path.join(HERE, "arm.stl"))
    for side in ("left", "right"):
        a = arm.copy()
        a.apply_transform(placement(side, 0.0))
        a.apply_scale(0.001)
        if side == "right":
            a.invert()
        add(f"printed/printed arm, {side}", a)
        for name, s in corner_solids(side, real_wheel=True).items():
            add(f"corner/{'SWYFT wheels' if name == 'wheel' else name + ' (placed, not measured)'}, {side}",
                solid_mesh(s))
    scene.export(out)
    print(out, os.path.getsize(out) // 1024, "KB")


if __name__ == "__main__":
    main(sys.argv[1])
