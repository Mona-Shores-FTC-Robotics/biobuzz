"""Renders of the printed FLOWER arm on the robot: stowed, down, and close in at the corner wheel.

    python3 cad/flower-printed-arms/render.py    # writes render_*.png next to this file

Grey: the 31 Oct CAD robot (cad/advantagescope). Blue: the printed arm. Red outline: today's straight channel.
Orange: the corner wheel, servo and mount as geometry.py places them (PLACEHOLDERS until measured).
"""
import os
import sys

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
import trimesh  # noqa: E402
from mpl_toolkits.mplot3d.art3d import Poly3DCollection  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from check import ROBOT_GLB, corner_solids, placement  # noqa: E402
from geometry import ARM_T, ARM_W, END_PAST, IN, SWING_DEG, TIP_U  # noqa: E402

LIGHT = np.array([0.4, 0.5, 0.8]) / np.linalg.norm([0.4, 0.5, 0.8])


def shade(mesh, rgb):
    n = mesh.face_normals
    k = 0.45 + 0.55 * np.abs(n @ LIGHT)
    return np.clip(np.outer(k, rgb), 0, 1)


def robot_crop():
    s = trimesh.load(ROBOT_GLB)
    m = s.to_geometry() if hasattr(s, "to_geometry") else s.dump(concatenate=True)
    m.apply_scale(1000.0)
    c = m.triangles_center
    keep = (c[:, 1] > 20) & (c[:, 0] > -40)
    sub = m.submesh([np.nonzero(keep)[0]], append=True)
    try:
        sub = sub.simplify_quadric_decimation(face_count=60000)
    except Exception:
        pass
    return sub


def corner_meshes(side):
    out = []
    for name, s in corner_solids(side, real_wheel=True).items():
        v, f = s.tessellate(0.5)
        out.append(trimesh.Trimesh([[p.x, p.y, p.z] for p in v], f))
    return out


def placed(mesh, side, th):
    m = mesh.copy()
    m.apply_transform(placement(side, th))
    return m


def straight_outline(side, th):
    """Today's channel, as the outline of its band's outer face."""
    u = np.r_[-END_PAST, TIP_U + END_PAST, TIP_U + END_PAST, -END_PAST, -END_PAST]
    v = np.r_[-1, -1, 1, 1, -1] * ARM_W / 2
    M = placement(side, th)
    lines = []
    for z in (0.0, ARM_T):
        p = np.c_[u, v, np.full_like(u, z), np.ones_like(u)] @ M.T
        lines.append(p[:, :3])
    return lines


def draw(ax, meshes):
    for m, rgb, alpha in meshes:
        pc = Poly3DCollection(m.triangles, facecolors=shade(m, rgb), edgecolor="none", alpha=alpha)
        ax.add_collection3d(pc)


def view(name, angles, robot, arm, corner, lims, elev_azim, title):
    fig = plt.figure(figsize=(9, 7), dpi=110)
    ax = fig.add_subplot(111, projection="3d")
    items = [(robot, np.array([0.72, 0.72, 0.74]), 0.35)]
    items += [(c, np.array([0.95, 0.55, 0.15]), 1.0) for c in corner]
    for th in angles:
        items.append((placed(arm, "left", th), np.array([0.25, 0.5, 0.9]), 1.0))
    draw(ax, items)
    for th in angles:
        for ln in straight_outline("left", th):
            ax.plot(*ln.T, color="#d22", lw=1.0)
    (x0, x1), (y0, y1), (z0, z1) = lims
    ax.set_xlim(x0, x1)
    ax.set_ylim(y0, y1)
    ax.set_zlim(z0, z1)
    ax.set_box_aspect((x1 - x0, y1 - y0, z1 - z0))
    ax.view_init(*elev_azim)
    ax.set_xlabel("X (mm, forward)")
    ax.set_ylabel("Y (mm, left)")
    ax.set_zlabel("Z (mm)")
    ax.set_title(title, fontsize=10)
    fig.tight_layout()
    fig.savefig(os.path.join(HERE, f"render_{name}.png"))
    plt.close(fig)


def section(name="section"):
    """Side view from the left, cut at the arm's band: the corner parts where they reach into the band (Y within
    CLEARANCE of it), the straight channel and the printed arm down, and the printed arm's lower edge every 10 deg."""
    from scipy.spatial import ConvexHull
    import parts
    from geometry import ARM_Y, CLEARANCE, arm_to_robot, corner_boxes, sample_corner
    fig, axs = plt.subplots(1, 2, figsize=(12, 5.4), dpi=110)
    hm = parts.hump()
    for ax, side in zip(axs, ("left", "right")):
        y0, y1 = sorted(ARM_Y[side])
        p = sample_corner(side, 1.0)
        for k in corner_boxes(side):
            q = np.array(sample_part(side, k))
            q = q[(q[:, 1] > y0 - CLEARANCE) & (q[:, 1] < y1 + CLEARANCE)]
            if len(q) > 3:
                h = ConvexHull(q[:, [0, 2]])
                ax.fill(*q[h.vertices][:, [0, 2]].T, color="#f0901f", alpha=0.85, label=f"corner {k} in the band")
        for th, alpha in [(a, 0.12) for a in range(0, 162, 10)] + [(162.0, 1.0)]:
            out = np.array(parts._outline(hm, ARM_W / 2) + [parts._outline(hm, ARM_W / 2)[0]])
            r = arm_to_robot(out[:, 0], out[:, 1], 0 * out[:, 0], th)
            ax.plot(r[:, 0], r[:, 2], color="#2a5fd0", lw=1.6 if alpha == 1 else 0.8, alpha=alpha,
                    label="printed, down (162 deg)" if alpha == 1 else None)
            if alpha == 1:
                ax.fill(r[:, 0], r[:, 2], color="#2a5fd0", alpha=0.25)
        st = np.array(parts._outline((0.0, 0, 1, 2, 3), ARM_W / 2))
        st = np.vstack([st, st[:1]])
        r = arm_to_robot(st[:, 0], st[:, 1], 0 * st[:, 0], 162.0)
        ax.plot(r[:, 0], r[:, 2], color="#d22", lw=1.2, label="today's channel, down")
        for n in range(1, 12):
            q = arm_to_robot([24.0 * (n - 1)], [0.0], [0.0], 162.0)[0]
            ax.annotate(str(n), (q[0], q[2]), fontsize=7, ha="center", va="center", color="#123")
        ax.axhline(0, color="#555", lw=0.8)
        ax.set_aspect("equal")
        ax.set_xlim(120, 340)
        ax.set_ylim(-5, 200)
        ax.set_xlabel("X (mm, forward)")
        ax.set_ylabel("Z (mm, up)")
        ax.set_title(f"{side} arm, seen from the left, cut at its band (|Y| {abs(y0):.0f}..{abs(y1):.0f} mm)",
                     fontsize=9)
        h_, l_ = ax.get_legend_handles_labels()
        seen = {}
        for hh, ll in zip(h_, l_):
            seen.setdefault(ll, hh)
        ax.legend(seen.values(), seen.keys(), fontsize=7, loc="upper right")
    fig.suptitle("Numbers on the arm: hole stations from the pivot (24 mm apart). Orange: placeholder corner parts. "
                 "Faint blue: the printed arm every 10 deg of the swing.", fontsize=9)
    fig.tight_layout()
    fig.savefig(os.path.join(HERE, f"render_{name}.png"))
    plt.close(fig)


def sample_part(side, key, step=1.0):
    from geometry import corner_boxes
    import geometry
    saved = geometry.corner_boxes
    only = {key: corner_boxes(side)[key]}
    geometry.corner_boxes = lambda s: only
    try:
        return geometry.sample_corner(side, step)
    finally:
        geometry.corner_boxes = saved


def main():
    section()
    arm = trimesh.load(os.path.join(HERE, "arm.stl"))
    robot = robot_crop()
    corner = corner_meshes("left")
    big = ((-60, 340), (20, 240), (0, 460))
    view("stowed", [0.0], robot, arm, corner, big, (8, -70),
         "Left arm stowed (0 deg). Blue: printed. Red: today's channel. Orange: corner wheel (placeholder)")
    view("down", [SWING_DEG], robot, arm, corner, ((-60, 340), (20, 240), (0, 300)), (8, -70),
         "Left arm down (162 deg): the hump rides over the corner wheel and servo")
    view("corner", [SWING_DEG], robot, arm, corner, ((150, 300), (90, 240), (0, 150)), (18, -35),
         "At the corner wheel, stick down: 4 mm (half a hole) clear, through the whole swing")
    view("side", [0.0, 60.0, 120.0, SWING_DEG], robot, arm, corner, ((-60, 340), (20, 240), (0, 460)), (0, -90),
         "Side view from the left: 0, 60, 120, 162 deg")
    # the part alone, as printed
    fig = plt.figure(figsize=(10, 4), dpi=110)
    ax = fig.add_subplot(111, projection="3d")
    draw(ax, [(arm, np.array([0.25, 0.5, 0.9]), 1.0)])
    ax.set_xlim(-30, 270)
    ax.set_ylim(-60, 90)
    ax.set_zlim(-40, 50)
    ax.set_box_aspect((300, 150, 90))
    ax.view_init(55, -75)
    ax.set_axis_off()
    ax.set_title("The printed arm, as it lies on the bed (the web in the middle; both faces alike)", fontsize=10)
    fig.tight_layout()
    fig.savefig(os.path.join(HERE, "render_part.png"))
    plt.close(fig)


if __name__ == "__main__":
    main()
