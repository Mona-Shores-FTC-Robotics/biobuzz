"""Draws parts.png: the mount alone, the mount with a sensor on a goBILDA wall, and its footprint on the hole pattern.

    pip install cadquery trimesh matplotlib
    python3 cad/beam-mount/parts.py && python3 cad/beam-mount/render.py      # draws the STL parts.py wrote
"""
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import trimesh

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
    """The sensor as a plain block on the plate, LED end over the window, plus its LED dome and screw head."""
    y0 = -parts.LED_FROM_END
    body = slab(-parts.BODY_W / 2, parts.BODY_W / 2, y0, y0 + parts.BODY_L, parts.PLATE_T, parts.PLATE_T + 9)
    head = cyl(0, parts.LED_TO_SCREW, parts.PLATE_T + 9, parts.PLATE_T + 11.5, 5.5)
    return body, head


def main():
    m = trimesh.load(os.path.join(HERE, "beam_mount.stl"))
    fig = plt.figure(figsize=(15, 5.5))
    yellow, grey = (0.95, 0.75, 0.2), (0.72, 0.74, 0.8)
    show(fig.add_subplot(1, 3, 1), raster([(m, yellow)], 40, -60), "beam_mount.stl, as it prints")
    body, head = sensor()
    show(fig.add_subplot(1, 3, 2), raster([(wall(), grey), (m, yellow), (body, (0.2, 0.2, 0.2)), (head, (0.85, 0.85, 0.9))], 35, -60),
         "On a goBILDA wall: sensor in, its screw on top\n(the lane is on the far side of the wall)")
    ax = fig.add_subplot(1, 3, 3)
    for s in trimesh.intersections.mesh_plane(m, plane_normal=[0, 0, 1], plane_origin=[0, 0, 0.5]):
        ax.plot(s[:, 0], s[:, 1], "k", lw=1)
    for cx in (0.0, -24.0):
        ax.add_patch(plt.Circle((cx, 0), 7.0, fill=False, color="tab:blue"))
        for dx in (-8, 8):
            for dy in (-8, 8):
                ax.add_patch(plt.Circle((cx + dx, dy), 2.25, fill=False, color="tab:blue"))
    ax.plot(0, 0, "r+", ms=14)
    ax.set_aspect("equal"); ax.grid(alpha=0.3)
    ax.set_title("Underside (black) over goBILDA's holes (blue)\n+ = beam; hex = the sensor screw's nut", fontsize=10)
    plt.tight_layout()
    plt.savefig(os.path.join(HERE, "parts.png"), dpi=90)


if __name__ == "__main__":
    main()
