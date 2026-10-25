"""The Limelight 3A at the planned camera position, in the frame of the mentor's "Robot Copy 1.step" (25 Oct), so it can be
inserted at the origin of his Onshape assembly; and a check of whether the stowed FLOWER stick sits in its view.

    python3 place_limelight.py <LIMELIGHT3ACAD_STEP.stp> [--parts parts.pkl] [--stick <dir of stick_*.stl>]

Writes, next to this script: limelight_in_robot_copy1.step, fov_side_view.png and numbers.txt. README.md explains the frames.

Frames (all the numbers below):
- model: build.py's AdvantageScope frame. +X forward, +Y left, +Z up, origin on the floor under the chassis centre, which
  is 7.56 in behind the intake uprights' front face (real_parts.to_model, back_in=7.56). The planned camera is given here:
  lens (4.0, 0, 14.0) in, pitched 45 deg up (build.py limelight_mount; real_parts.limelight_trsf).
- face: inches ahead of the intake uprights' front face, left of centre, above the floor. ahead = X - 7.56.
- copy1 raw: the coordinates in Robot Copy 1.step itself (mm). list_parts.py measured it, after build.placed_team's
  alignment (dx across, dz forward), as: forward_list = (z - (FACE - 7.56 IN)) / IN with the uprights' front face at
  forward_list 5.65, left = (x - C) / IN, up = 18.25 - (y - F) / IN. His file is rotated 180 deg about x compared with the
  frame the team CAD used to use (y down, z backward), so forward_list runs backward: ahead = 5.65 - forward_list.
"""
import sys, os, math, json, argparse, pickle, re
import numpy as np
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, ".."))                       # real_parts
sys.path.insert(0, os.path.join(HERE, "..", "..", "robot-addons"))  # build: C, F, FACE, IN (the team CAD's frame constants)
import build as A
import real_parts as RP
import cadquery as cq
from OCP.gp import gp_Trsf, gp_Pnt, gp_Dir
from OCP.TopLoc import TopLoc_Location
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib

C, F, FACE, IN = A.C, A.F, A.FACE, A.IN
ALIGN_DX, ALIGN_DZ = -159.25540601499378, -7.52062300179   # build.placed_team.align for Robot Copy 1.step (list.txt, 25 Oct)
FACE_FWD_LIST, FLOOR_UP_LIST = 5.65, 18.25                   # where list_parts.py's frame has the uprights' front face and the floor
BACK_IN = 7.56                                               # the chassis centre behind the face (model frame origin)
LENS_MODEL_IN, PITCH_DEG = (4.0, 0.0, 14.0), 45.0            # the plan (build.py limelight_mount)
FOV_H, FOV_V = 54.5, 42.0                                    # Limelight 3A, docs.limelightvision.io hardware comparison: "H:54.5 V:42"

def model_to_copy1():
    """gp_Trsf: model frame (mm) -> Robot Copy 1.step's own coordinates (mm). Rows: x_r = Y + (C - dx); y_r = -Z + (F + 18.25 IN);
    z_r = -X + (5.65 IN + FACE - dz). Rotation determinant +1 (a 180 deg turn about x, then the alignment offsets)."""
    t = gp_Trsf()
    t.SetValues(0, 1, 0, C - ALIGN_DX,
                0, 0, -1, F + FLOOR_UP_LIST * IN,
                -1, 0, 0, FACE_FWD_LIST * IN + FACE - ALIGN_DZ)
    return t

def copy1_to_face(p):
    """Robot Copy 1 raw mm -> (ahead of face, left, up) inches."""
    x, y, z = p
    fwd_list = (z + ALIGN_DZ - (FACE - BACK_IN * IN)) / IN
    return (FACE_FWD_LIST - fwd_list, (x + ALIGN_DX - C) / IN, FLOOR_UP_LIST - (y - F) / IN)

def mv(t, p):
    q = gp_Pnt(*p).Transformed(t); return (q.X(), q.Y(), q.Z())

def main():
    ap = argparse.ArgumentParser(); ap.add_argument("ll_step"); ap.add_argument("--parts"); ap.add_argument("--stick")
    a = ap.parse_args()
    notes = []
    def say(*s):
        line = " ".join(str(v) for v in s); print(line); notes.append(line)

    cam2model = RP.limelight_trsf(LENS_MODEL_IN, PITCH_DEG)   # camera STEP mm -> model mm (the same call build.py's rig uses)
    m2r = model_to_copy1()
    cam2raw = m2r.Multiplied(cam2model)                        # camera STEP -> Robot Copy 1 raw
    lens_model = mv(cam2model, RP.LL_LENS); lens_raw = mv(m2r, lens_model)
    look_model = gp_Dir(0, 1, 0).Transformed(cam2model); look_raw = look_model.Transformed(m2r)
    lens_face = copy1_to_face(lens_raw)
    say(f"lens, model frame (in): {tuple(round(v / IN, 3) for v in lens_model)}; look dir {look_model.X():.4f}, {look_model.Y():.4f}, {look_model.Z():.4f}")
    say(f"lens, face frame (in ahead, left, up): {tuple(round(v, 3) for v in lens_face)}")
    say(f"lens, Robot Copy 1 raw (mm): {tuple(round(v, 2) for v in lens_raw)}; look dir in raw {look_raw.X():.4f}, {look_raw.Y():.4f}, {look_raw.Z():.4f}")
    # round trip: the model frame's forward/left/up must map to copy1's -z / +x / -y
    for name, v in (("+X fwd", (1, 0, 0)), ("+Y left", (0, 1, 0)), ("+Z up", (0, 0, 1))):
        d = gp_Dir(*v).Transformed(m2r); say(f"  model {name} -> raw ({d.X():+.0f}, {d.Y():+.0f}, {d.Z():+.0f})")

    # the camera, placed
    leaves = RP.read_leaves(a.ll_step)
    top = cq.Assembly(name="Limelight 3A at the planned camera position (frame of Robot Copy 1.step: insert at the origin)")
    bb = Bnd_Box()
    for names, shp, col in leaves:
        s = cq.Shape.cast(shp.wrapped.Moved(TopLoc_Location(cam2raw)))
        BRepBndLib.AddOptimal_s(s.wrapped, bb, False, False)
        n = re.sub(r":\d+$", "", names[-1] if names else "body")
        top.add(s, name=n, color=cq.Color(*(col or (0.6, 0.6, 0.6))))
    out_step = os.path.join(HERE, "limelight_in_robot_copy1.step"); top.save(out_step)
    say(f"wrote {out_step} ({os.path.getsize(out_step) // 1000} kB, {len(leaves)} bodies)")
    x0, y0, z0, x1, y1, z1 = bb.Get()
    corners = [copy1_to_face((x, y, z)) for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)]
    cam_box = tuple((min(c[i] for c in corners), max(c[i] for c in corners)) for i in range(3))
    say(f"camera body bounds, raw mm: x {x0:.1f}..{x1:.1f} y {y0:.1f}..{y1:.1f} z {z0:.1f}..{z1:.1f}")
    say(f"camera body bounds, face frame (in): ahead {cam_box[0][0]:.2f}..{cam_box[0][1]:.2f} left {cam_box[1][0]:.2f}..{cam_box[1][1]:.2f} up {cam_box[2][0]:.2f}..{cam_box[2][1]:.2f}")

    # the camera's outline in the side view: its STEP bbox corners through the transform, hull in (ahead, up)
    cb = Bnd_Box()
    for names, shp, col in leaves: BRepBndLib.AddOptimal_s(shp.wrapped, cb, False, False)
    cx0, cy0, cz0, cx1, cy1, cz1 = cb.Get()
    cam_side = [copy1_to_face(mv(cam2raw, (x, y, z))) for x in (cx0, cx1) for y in (cy0, cy1) for z in (cz0, cz1)]

    # bounding-box neighbours from list_parts.py's parts.pkl (its frame -> face frame), for a quick clash check
    if a.parts:
        rows = pickle.load(open(a.parts, "rb")); hits = []
        for path, fwd, left, up in rows:
            f = (FACE_FWD_LIST - fwd[1], FACE_FWD_LIST - fwd[0]); u = (FLOOR_UP_LIST - up[1], FLOOR_UP_LIST - up[0])
            if all(lo <= hi2 and lo2 <= hi for (lo, hi), (lo2, hi2) in zip((f, left, u), cam_box)):
                hits.append(" / ".join(path))
        say(f"mentor's parts whose bounding box overlaps the camera's: {len(hits)}" + ("".join("\n    " + h for h in hits) if hits else " (none)"))

    # ---- the stowed FLOWER stick in the view ----
    lens = np.array(lens_face); p = math.radians(PITCH_DEG)
    top_edge, bot_edge = PITCH_DEG + FOV_V / 2, PITCH_DEG - FOV_V / 2
    say(f"FOV assumed: H {FOV_H} deg, V {FOV_V} deg; pitched {PITCH_DEG}: vertical edges at {bot_edge:.1f} and {top_edge:.1f} deg elevation, "
        f"horizontal half-angle {FOV_H / 2:.2f} deg")
    def elev(ahead, up): return math.degrees(math.atan2(up - lens[2], ahead - lens[0]))
    blade = {"ahead": (-3.57, -0.31), "left": (-4.36, 3.51), "up": (16.58, 17.06)}   # Part 4, mesh bounds (stick_parts.json)
    shaft = {"ahead": (-1.01, -0.52), "up": (16.63, 17.05)}
    arms = {"ahead": (-3.22, 0.04), "left": 5.41, "up": (6.46, 17.85)}
    pivot = (-2.44, 7.9)
    say(f"blade bottom-front edge (ahead {blade['ahead'][1]}, up {blade['up'][0]}): elevation {elev(blade['ahead'][1], blade['up'][0]):.1f} deg")
    say(f"blade bottom-back edge  (ahead {blade['ahead'][0]}, up {blade['up'][0]}): elevation {elev(blade['ahead'][0], blade['up'][0]):.1f} deg")
    say(f"shaft centre (ahead {-0.76}, up {16.84}): elevation {elev(-0.76, 16.84):.1f} deg")
    # arms: the nearest they come to the optical axis, in azimuth (they are 5.4 in off the centreline, the camera is on it)
    n = np.array([math.cos(p), 0, math.sin(p)])
    best = None
    for ah in np.linspace(arms["ahead"][0], arms["ahead"][1], 40):
        for up in np.linspace(arms["up"][0], arms["up"][1], 120):
            v = np.array([ah, arms["left"], up]) - lens; along = v @ n
            if along <= 0: continue
            lat = abs(v[1]); vert = v @ np.array([-math.sin(p), 0, math.cos(p)])
            az, el = math.degrees(math.atan2(lat, along)), math.degrees(math.atan2(vert, along))
            if best is None or az < best[0]: best = (az, el, ah, up)
    say(f"arms: nearest point to the optical axis is {best[0]:.1f} deg off-axis horizontally (at ahead {best[2]:.2f}, up {best[3]:.2f}; "
        f"{best[1]:+.1f} deg above the axis); horizontal half-FOV is {FOV_H / 2:.2f} deg -> {'IN' if best[0] < FOV_H / 2 else 'out of'} view")
    # elevation sweep at the centreline: which elevations in the vertical FOV hit the blade's bottom face / front face
    def hits_blade(el):
        d = np.array([math.cos(math.radians(el)), math.sin(math.radians(el))])
        if d[1] <= 0: return False
        t = (blade["up"][0] - lens[2]) / d[1]; x = lens[0] + d[0] * t            # cross the bottom face plane
        if blade["ahead"][0] <= x <= blade["ahead"][1]: return True
        if d[0] > 0:                                                             # or the front face plane
            t = (blade["ahead"][1] - lens[0]) / d[0]; z = lens[2] + d[1] * t
            return blade["up"][0] <= z <= blade["up"][1]
        return False
    els = np.linspace(bot_edge, top_edge, 421); blocked = np.array([hits_blade(e) for e in els])
    say(f"centreline sweep: the blade blocks elevations {els[blocked].min():.1f}..{els[blocked].max():.1f} deg of the {bot_edge:.1f}..{top_edge:.1f} window "
        f"= {100 * blocked.mean():.0f}% of the vertical FOV at the centreline")

    # ray cast against the stick meshes (face frame), if given
    if a.stick:
        import trimesh, glob
        ms = [trimesh.load(f, force="mesh") for f in sorted(glob.glob(os.path.join(a.stick, "stick_*.stl")))]
        mesh = trimesh.util.concatenate(ms)
        az = np.radians(np.linspace(-FOV_H / 2, FOV_H / 2, 110)); el = np.radians(np.linspace(-FOV_V / 2, FOV_V / 2, 85))
        AZ, EL = np.meshgrid(az, el)
        u = np.array([-math.sin(p), 0, math.cos(p)]); l = np.array([0, 1, 0])
        # pinhole: direction = n + tan(az) * left + tan(el) * up (normalised)
        d = n[None, :] + np.tan(AZ).ravel()[:, None] * l[None, :] + np.tan(EL).ravel()[:, None] * u[None, :]
        d /= np.linalg.norm(d, axis=1)[:, None]
        o = np.repeat(lens[None, :], len(d), axis=0)
        hit = mesh.ray.intersects_any(o, d)
        say(f"ray cast ({len(d)} rays over the {FOV_H}x{FOV_V} deg FOV) against the stick meshes: {100 * hit.mean():.0f}% of the image blocked")
        locs, idx, _ = mesh.ray.intersects_location(o, d, multiple_hits=False)
        if len(locs): say(f"  nearest blocking surface {np.linalg.norm(locs - lens, axis=1).min():.2f} in from the lens")
        # what would clear it: move the camera back / down / forward, or stow the blade lower (by sweeping)
        def frac(lens_try, pitch=PITCH_DEG):
            pp = math.radians(pitch); nn = np.array([math.cos(pp), 0, math.sin(pp)]); uu = np.array([-math.sin(pp), 0, math.cos(pp)])
            dd = nn[None, :] + np.tan(AZ).ravel()[:, None] * l[None, :] + np.tan(EL).ravel()[:, None] * uu[None, :]
            dd /= np.linalg.norm(dd, axis=1)[:, None]
            return mesh.ray.intersects_any(np.repeat(np.array(lens_try)[None, :], len(dd), axis=0), dd).mean()
        for label, pts in (("camera moved back", [(lens[0] - b, 0, lens[2]) for b in (1, 2, 3, 4, 5, 6)]),
                           ("camera moved down", [(lens[0], 0, lens[2] - b) for b in (1, 2, 3, 4, 5)]),
                           ("camera moved forward", [(lens[0] + b, 0, lens[2]) for b in (1, 1.5, 2, 2.5, 3)])):
            say(f"  {label}: " + ", ".join(f"{abs(pt[0] - lens[0]) + abs(pt[2] - lens[2]):.1f} in -> {100 * frac(pt):.0f}%" for pt in pts))
        say("  camera pitched: " + ", ".join(f"{pd:.0f} deg -> {100 * frac(lens, pd):.0f}%" for pd in (30, 35, 40, 45, 50, 55, 60)))

    # ---- side-view sketch ----
    import matplotlib; matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    from matplotlib.patches import Polygon, Rectangle, Circle, Wedge
    from scipy.spatial import ConvexHull
    INK, MUTED, GRID = "#1f2328", "#59636e", "#d0d7de"
    CAM, FOVC, STICK, FRAME = "#1f6feb", "#1f6feb", "#bc4c00", "#8c959f"
    fig, ax = plt.subplots(figsize=(10, 7.2), dpi=140)
    ax.set_facecolor("white"); fig.patch.set_facecolor("white")
    R = 12.0
    for e, ls in ((bot_edge, "-"), (top_edge, "-")):
        ax.plot([lens[0], lens[0] + R * math.cos(math.radians(e))], [lens[2], lens[2] + R * math.sin(math.radians(e))], color=FOVC, lw=1.4, ls=ls)
    ax.add_patch(Wedge((lens[0], lens[2]), R, bot_edge, top_edge, color=FOVC, alpha=0.10, lw=0))
    ax.plot([lens[0], lens[0] + R * math.cos(p)], [lens[2], lens[2] + R * math.sin(p)], color=FOVC, lw=1, ls="--")
    pts = np.array([(c[0], c[2]) for c in cam_side]); h = ConvexHull(pts)
    ax.add_patch(Polygon(pts[h.vertices], closed=True, fc=CAM, ec=INK, lw=1, alpha=0.85, zorder=5))
    ax.plot(lens[0], lens[2], "o", ms=5, color="white", mec=INK, zorder=6)
    # the stick: blade and shaft on the centreline, arms and pivot servo at the side (hatched: out of the drawing plane)
    ax.add_patch(Rectangle((blade["ahead"][0], blade["up"][0]), blade["ahead"][1] - blade["ahead"][0], blade["up"][1] - blade["up"][0], fc=STICK, ec=INK, lw=1, zorder=4))
    ax.add_patch(Circle((-0.76, 16.84), 0.16, fc=STICK, ec=INK, lw=1, zorder=4))
    if a.stick:
        for f in sorted(glob.glob(os.path.join(a.stick, "stick_*.stl"))):
            m = trimesh.load(f, force="mesh"); v = m.vertices[:, [0, 2]]
            side = abs(m.bounds[:, 1]).max() > 3.5 and "Part_4.stl" not in f and "312mm" not in f
            if not side: continue
            hh = ConvexHull(v); ax.add_patch(Polygon(v[hh.vertices], closed=True, fc="none", ec=STICK, lw=1, hatch="////", alpha=0.6, zorder=3))
    ax.axvline(0, color=FRAME, lw=1.2); ax.axhline(0, color=INK, lw=1.5); ax.axhline(18.0, color=MUTED, lw=0.8, ls=":")
    ax.text(0.1, 1.0, "intake uprights'\nfront face", color=MUTED, fontsize=8, va="bottom")
    ax.text(-9.9, 18.1, "18 in height limit", color=MUTED, fontsize=8, va="bottom")
    ax.annotate(f"lens: {lens[0]:.2f} ahead, {lens[2]:.1f} up\npitched {PITCH_DEG:.0f}°, V FOV {FOV_V:.0f}° ({bot_edge:.0f}°..{top_edge:.0f}°)",
                (lens[0], lens[2]), (-9.8, 11.2), color=INK, fontsize=8.5, arrowprops=dict(arrowstyle="-", color=MUTED, lw=0.8))
    e_bf, e_bb = elev(blade["ahead"][1], blade["up"][0]), elev(blade["ahead"][0], blade["up"][0])
    ax.annotate(f"stowed blade (Part 4): bottom-front edge at {e_bf:.0f}°,\nbottom-back edge at {e_bb:.0f}° — over the lens",
                (blade["ahead"][1], blade["up"][0]), (0.6, 15.0), color=INK, fontsize=8.5, arrowprops=dict(arrowstyle="-", color=MUTED, lw=0.8))
    ax.annotate(f"shaft, {elev(-0.76, 16.84):.0f}° (on the axis)", (-0.76, 16.84), (0.8, 17.3), color=INK, fontsize=8.5, arrowprops=dict(arrowstyle="-", color=MUTED, lw=0.8))
    ax.text(-3.2, 7.0, f"arms (hatched: 5.4 in off centre,\n{best[0]:.0f}° off-axis, outside the\n±{FOV_H / 2:.0f}° horizontal FOV)", color=MUTED, fontsize=8, ha="right")
    ax.set_xlim(-10, 10); ax.set_ylim(-0.5, 19.5); ax.set_aspect("equal")
    ax.set_xlabel("inches ahead of the intake face  (robot forward →)", color=INK); ax.set_ylabel("inches above the floor", color=INK)
    ax.set_title("Limelight 3A at the planned mount vs the stowed FLOWER stick — side view, Robot Copy 1 (25 Oct)", color=INK, fontsize=10.5, loc="left")
    ax.grid(color=GRID, lw=0.5); ax.tick_params(colors=MUTED)
    for s in ax.spines.values(): s.set_color(GRID)
    out_png = os.path.join(HERE, "fov_side_view.png"); fig.savefig(out_png, bbox_inches="tight"); say(f"wrote {out_png}")
    open(os.path.join(HERE, "numbers.txt"), "w").write("\n".join(notes) + "\n")

if __name__ == "__main__":
    main()
