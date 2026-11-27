"""Draws parts.png (the receiver mount: as it prints, with a sensor on a goBILDA wall, a section through its snap arms,
its footprint on the hole pattern, and the fit test) and floor_sled_screwed.png (option B and a section through a screw).

    pip install cadquery trimesh manifold3d matplotlib
    python3 cad/beam-mount/parts.py && python3 cad/beam-mount/floor_sled.py && (cd cad/beam-mount && python3 render.py)
"""
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import trimesh

import floor_sled
import parts

HERE = os.path.dirname(os.path.abspath(__file__))
LIGHT = np.array([0.4, -0.6, 0.7]) / np.linalg.norm([0.4, -0.6, 0.7])


def raster(meshes, elev, azim, size=520):
    """A small z-buffer renderer: [(mesh, rgb)] seen from (elev, azim) degrees, flat-shaded, as an RGB image.
    matplotlib's 3D sorts whole faces and draws overlapping parts wrongly, so this draws them pixel by pixel."""
    e, a = np.radians(elev), np.radians(azim)
    view = np.array([np.cos(e) * np.cos(a), np.cos(e) * np.sin(a), np.sin(e)])     # toward the viewer
    right = np.cross([0, 0, 1], view); right /= np.linalg.norm(right)
    up = np.cross(view, right)
    tris = [(m.triangles, m.face_normals, rgb) for m, rgb in meshes]
    allp = np.concatenate([t.reshape(-1, 3) for t, _, _ in tris])
    uv = np.c_[allp @ right, allp @ up]
    lo, span = uv.min(0), (uv.max(0) - uv.min(0)).max() * 1.08
    lo -= (span - (uv.max(0) - uv.min(0))) / 2
    img = np.ones((size, size, 3)); zb = np.full((size, size), -np.inf)
    light = 0.6 * view + 0.4 * up
    light /= np.linalg.norm(light)
    for t, n, rgb in tris:
        shade = 0.35 + 0.65 * np.abs(n @ light)
        px = (np.stack([t @ right, t @ up], -1) - lo) / span * (size - 1)
        dz = t @ view
        for (p, z, s) in zip(px, dz, shade):
            x0, y0 = np.floor(p.min(0)).astype(int); x1, y1 = np.ceil(p.max(0)).astype(int)
            if x1 < x0 or y1 < y0:
                continue
            xs, ys = np.meshgrid(np.arange(x0, x1 + 1), np.arange(y0, y1 + 1))
            d = (p[1, 0] - p[0, 0]) * (p[2, 1] - p[0, 1]) - (p[2, 0] - p[0, 0]) * (p[1, 1] - p[0, 1])
            if abs(d) < 1e-9:
                continue
            w1 = ((xs - p[0, 0]) * (p[2, 1] - p[0, 1]) - (p[2, 0] - p[0, 0]) * (ys - p[0, 1])) / d
            w2 = ((p[1, 0] - p[0, 0]) * (ys - p[0, 1]) - (xs - p[0, 0]) * (p[1, 1] - p[0, 1])) / d
            w0 = 1 - w1 - w2
            inside = (w0 >= -1e-6) & (w1 >= -1e-6) & (w2 >= -1e-6) & (xs >= 0) & (ys >= 0) & (xs < size) & (ys < size)
            zz = w0 * z[0] + w1 * z[1] + w2 * z[2]
            xi, yi, zi = xs[inside], ys[inside], zz[inside]
            near = zi > zb[yi, xi]
            zb[yi[near], xi[near]] = zi[near]
            img[yi[near], xi[near]] = s * np.asarray(rgb)
    return img[::-1]


def show(ax, img, title):
    ax.imshow(img); ax.set_axis_off(); ax.set_title(title, fontsize=10)


def slab(x0, x1, y0, y1, z0, z1):
    b = trimesh.creation.box(extents=[x1 - x0, y1 - y0, z1 - z0])
    b.apply_translation([(x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2])
    return b


def cyl(x, y, z0, z1, d):
    c = trimesh.creation.cylinder(radius=d / 2, height=z1 - z0, sections=48)
    c.apply_translation([x, y, (z0 + z1) / 2])
    return c


def wall():
    """A patch of goBILDA face, 2.5 mm thick, under the mount: 14 mm holes 24 mm apart, 4.5 mm holes at +-8 mm."""
    w = slab(-40, 14, -22, 26, -2.5, 0)
    for cx in (0.0, -24.0):
        w = w.difference(cyl(cx, 0, -4, 2, 14.0))
        for dx in (-8, 8):
            for dy in (-8, 8):
                w = w.difference(cyl(cx + dx, dy, -4, 2, 4.5))
    return w


def sensor():
    """The sensor as a plain block on the plate, LED end against the end wall, over the window."""
    y0 = -parts.LED_FROM_END
    return slab(-parts.BODY_W / 2, parts.BODY_W / 2, y0, y0 + parts.BODY_L, parts.PLATE_T, parts.PLATE_T + parts.BODY_T)


def section(ax, mesh, normal, origin, axes, colour="k"):
    for s in trimesh.intersections.mesh_plane(mesh, plane_normal=normal, plane_origin=origin):
        ax.plot(s[:, axes[0]], s[:, axes[1]], colour, lw=1)


def draw_parts():
    m = trimesh.load(os.path.join(HERE, "beam_mount.stl"))
    ft = trimesh.load(os.path.join(HERE, "beam_fit_test.stl"))
    fig = plt.figure(figsize=(15, 10.5))
    yellow, grey, dark = (0.95, 0.75, 0.2), (0.72, 0.74, 0.8), (0.2, 0.2, 0.2)
    show(fig.add_subplot(2, 3, 1), raster([(m, yellow)], 40, -60), "beam_mount.stl, as it prints")
    show(fig.add_subplot(2, 3, 2), raster([(wall(), grey), (m, yellow), (sensor(), dark)], 35, -60),
         "On a goBILDA wall, sensor in, LED down\n(lip over the LED end, hooks over the wire end)")
    show(fig.add_subplot(2, 3, 3), raster([(ft, yellow)], 45, -70),
         "beam_fit_test.stl: ribs 0.15 / 0.25 / 0.35 mm\n(1, 2, 3 notches)")
    y1 = parts.LED_TO_SCREW + parts.LED_FROM_END + parts.FIT
    ax = fig.add_subplot(2, 3, 4)
    section(ax, m, [0, 1, 0], [0, y1 - 2.0, 0], (0, 2))
    bw, bt = parts.BODY_W / 2, parts.BODY_T
    ax.add_patch(plt.Rectangle((-bw, parts.PLATE_T), 2 * bw, bt, color=dark, alpha=0.35))
    ax.set_aspect("equal"); ax.grid(alpha=0.3)
    ax.set_title("Across the wire end (2 mm in): the arms, free of the plate,\nand their hooks over the body (grey)", fontsize=10)
    ax = fig.add_subplot(2, 3, 5)
    section(ax, m, [1, 0, 0], [0, 0, 0], (1, 2))
    ax.add_patch(plt.Rectangle((-parts.LED_FROM_END, parts.PLATE_T), parts.BODY_L, bt, color=dark, alpha=0.35))
    ax.set_aspect("equal"); ax.grid(alpha=0.3)
    ax.set_title("Along the middle: the 3 mm lip over the LED end,\nthe window, the backup M2 nut, the wire-end wall", fontsize=10)
    ax = fig.add_subplot(2, 3, 6)
    section(ax, m, [0, 0, 1], [0, 0, 0.5], (0, 1))
    for cx in (0.0, -24.0):
        ax.add_patch(plt.Circle((cx, 0), 7.0, fill=False, color="tab:blue"))
        for dx in (-8, 8):
            for dy in (-8, 8):
                ax.add_patch(plt.Circle((cx + dx, dy), 2.25, fill=False, color="tab:blue"))
    ax.plot(0, 0, "r+", ms=14)
    ax.set_aspect("equal"); ax.grid(alpha=0.3)
    ax.set_title("Underside (black) over goBILDA's holes (blue)\n+ = beam; the arms' slots go right through", fontsize=10)
    plt.tight_layout()
    plt.savefig(os.path.join(HERE, "parts.png"), dpi=90)


def draw_screwed():
    m = trimesh.load(os.path.join(HERE, "floor_sled_screwed.stl"))
    f = floor_sled
    fig = plt.figure(figsize=(15, 5.5))
    show(fig.add_subplot(1, 2, 1), raster([(m, (0.07, 0.5, 0.56))], 35, -60),
         "floor_sled_screwed.stl, top face up (it prints this face down):\nfour blind 2.5 mm pilot holes, no nuts")
    ax = fig.add_subplot(1, 2, 2)
    section(ax, m, [0, 1, 0], [0, f.SCREW_Y, 0], (0, 2))
    w2 = f.POCKET_W / 2
    ax.add_patch(plt.Rectangle((-w2 - f.WALL_T, 0), 2 * w2 + 2 * f.WALL_T, f.FLOOR_T, color="0.7"))      # the floor
    for side in (-1, 1):
        ax.add_patch(plt.Rectangle((side * w2 - (f.WALL_T if side < 0 else 0), -f.POCKET_H), f.WALL_T, f.POCKET_H, color="0.7"))
        x = side * f.SCREW_X
        ax.add_patch(plt.Rectangle((x - 1.5, f.FLOOR_T - f.SCREW_L), 3.0, f.SCREW_L, color="tab:orange", alpha=0.8))
        ax.add_patch(plt.Rectangle((x - 2.85, f.FLOOR_T), 5.7, 1.65, color="tab:orange"))                 # button head
    ax.set_aspect("equal"); ax.grid(alpha=0.3)
    ax.set_title(f"Across the channel at a screw ({f.SCREW_Y} mm from the beam): an M3 x 8 (orange) through the\n"
                 f"2.5 mm floor bites {f.SCREW_L - f.FLOOR_T:.1f} mm into a {f.PILOT_DEPTH:.0f} mm pilot; "
                 f"{f.H - f.PILOT_DEPTH:.1f} mm of sled under it", fontsize=10)
    plt.tight_layout()
    plt.savefig(os.path.join(HERE, "floor_sled_screwed.png"), dpi=90)


def main():
    draw_parts()
    draw_screwed()


if __name__ == "__main__":
    main()
