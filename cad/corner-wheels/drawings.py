"""The corner-module pictures in doc/corner-wheels/ and their clearances, from the robot's own STEP export and this
folder's modules (parts.py).

    pip install cadquery trimesh matplotlib scipy
    python3 cad/corner-wheels/drawings.py path/to/Robot.step     # a few minutes; the robot STEP is not in git

The robot STEP is the mentor's Onshape export of the whole Robot assembly (24 Nov 2026, 178 MB). Its frame is turned to
the robot frame used everywhere here (inches, +X forward, +Y left, +Z up, origin on the floor under the middle of the
drive axles) from its own four drive axles. The FLOWER stick is every part reaching past X 8.7 (it is drawn down in the
CAD), swept 162 deg up about its pivot (the Servo Shaft, X 5.77, Z 7.59). The mecanum wheels are spun a full turn.
"""
import os
import pickle
import re
import sys
import tempfile

import cadquery as cq
import matplotlib
import numpy as np
import trimesh

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
from matplotlib.collections import PolyCollection  # noqa: E402
from matplotlib.patches import Patch  # noqa: E402
from scipy.spatial import cKDTree  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import parts as P  # noqa: E402

OUT = os.path.join(HERE, "..", "..", "doc", "corner-wheels")
PIVOT = (5.765, 7.585)                 # the stick's pivot, X and Z, inches
SWING_DEG = 162
COLOURS = {"bracket": "#4f81bd", "servo": "#404040", "hub": "#a6a6a6", "wheel": "#2e7d32", "screws": "#c00000"}


def read_step(path):
    """Every part's triangles (mm, the STEP's own frame), cached beside the STEP."""
    cache = path + ".tris.pkl"
    if os.path.exists(cache):
        return pickle.load(open(cache, "rb"))
    from OCP.BRep import BRep_Tool
    from OCP.BRepMesh import BRepMesh_IncrementalMesh
    from OCP.STEPCAFControl import STEPCAFControl_Reader
    from OCP.TCollection import TCollection_ExtendedString
    from OCP.TDataStd import TDataStd_Name
    from OCP.TDF import TDF_Label, TDF_LabelSequence
    from OCP.TDocStd import TDocStd_Document
    from OCP.TopAbs import TopAbs_FACE
    from OCP.TopExp import TopExp_Explorer
    from OCP.TopLoc import TopLoc_Location
    from OCP.TopoDS import TopoDS
    from OCP.XCAFDoc import XCAFDoc_DocumentTool
    doc = TDocStd_Document(TCollection_ExtendedString("doc"))
    r = STEPCAFControl_Reader()
    r.SetNameMode(True)
    r.ReadFile(path)
    r.Transfer(doc)
    st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())

    def name(label):
        a = TDataStd_Name()
        return a.Get().ToExtString() if label.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
    out = []

    def walk(label, loc, path_):
        if st.IsReference_s(label):
            ref = TDF_Label()
            st.GetReferredShape_s(label, ref)
            walk(ref, loc.Multiplied(st.GetLocation_s(label)), path_ + [name(label)])
            return
        if st.IsAssembly_s(label):
            comps = TDF_LabelSequence()
            st.GetComponents_s(label, comps)
            for i in range(1, comps.Length() + 1):
                walk(comps.Value(i), loc, path_)
            return
        shape = st.GetShape_s(label).Moved(loc)
        BRepMesh_IncrementalMesh(shape, 0.8, False, 0.5, True)
        tris = []
        ex = TopExp_Explorer(shape, TopAbs_FACE)
        while ex.More():
            f = TopoDS.Face_s(ex.Current())
            L = TopLoc_Location()
            tri = BRep_Tool.Triangulation_s(f, L)
            if tri is not None:
                T = L.Transformation()
                pts = np.array([[*tri.Node(i).Transformed(T).Coord()] for i in range(1, tri.NbNodes() + 1)])
                for i in range(1, tri.NbTriangles() + 1):
                    a, b, c = tri.Triangle(i).Get()
                    tris.append(pts[[a - 1, b - 1, c - 1]])
            ex.Next()
        if tris:
            out.append((" / ".join(path_), np.array(tris, np.float32)))
    free = TDF_LabelSequence()
    st.GetFreeShapes(free)
    for i in range(1, free.Length() + 1):
        walk(free.Value(i), TopLoc_Location(), [])
    pickle.dump(out, open(cache, "wb"))
    return out


def robot(path):
    """The robot in the robot frame (inches): fixed parts, the stick's parts, the front mecanum wheels."""
    raw = read_step(path)
    axles = [t.reshape(-1, 3) for n, t in raw if "Wheel Assembly" in n and "Shaft" in n]
    centres = np.array([(p.min(0) + p.max(0)) / 2 for p in axles])
    if len(centres) != 4 or np.ptp(centres[:, 0]) < np.ptp(centres[:, 2]) - 1e3:
        sys.exit("expected four drive axles along the STEP's X")
    c = centres.mean(0)
    floor = c[1] + 48.0                                   # 96 mm mecanum: the floor is 48 mm below the axles (STEP -Y is up)

    def to_robot(t):                                      # robot (fwd, left, up) = STEP (-Z, X, -Y), inches
        q = np.empty_like(t)
        q[..., 0] = -(t[..., 2] - c[2])
        q[..., 1] = t[..., 0] - c[0]
        q[..., 2] = -(t[..., 1] - floor)
        return q / 25.4
    fixed, stick, mec = [], [], []
    for n, t in raw:
        if re.search(r"(?i)nectar|pollen", n) or n.split(" / ")[0] == "Part 1":   # game pieces; the mentor's trial parts
            continue
        if "HTD5 belt 55" in n:                           # the rear belts are drawn out of place in the CAD
            continue
        t = to_robot(t)
        p = t.reshape(-1, 3)
        if p[:, 0].max() > 8.7 and p[:, 0].min() > -1:
            stick.append((n, t))
        elif "Wheel Assembly <2>" in n or "Wheel Assembly <4>" in n:
            mec.append((n, t))
        else:
            fixed.append((n, t))
    return fixed, stick, mec


def swept(stick):
    S = np.vstack([t.reshape(-1, 3) for _, t in stick])[::2]
    out = []
    for a in np.radians(np.arange(0, -SWING_DEG - 1, -1.0)):
        c, s = np.cos(a), np.sin(a)
        dx, dz = S[:, 0] - PIVOT[0], S[:, 2] - PIVOT[1]
        out.append(np.c_[PIVOT[0] + dx * c + dz * s, S[:, 1], PIVOT[1] - dx * s + dz * c])
    return np.vstack(out)


def spun(mec):
    M = np.vstack([t.reshape(-1, 3) for _, t in mec])
    out = []
    for a in np.radians(np.arange(0, 360, 6)):
        c, s = np.cos(a), np.sin(a)
        dx, dz = M[:, 0] - 5.672, M[:, 2] - 1.89
        out.append(np.c_[5.672 + dx * c + dz * s, M[:, 1], 1.89 - dx * s + dz * c])
    return np.vstack(out)[::3]


def module_tris(side):
    out = {}
    for name, shape in P.module(side).items():
        f = tempfile.mktemp(suffix=".stl")
        cq.exporters.export(shape.val(), f, tolerance=0.1)
        t = trimesh.load(f).triangles / 25.4
        os.remove(f)
        if side == "right":
            t = t * np.array([1, -1, 1])
        out[name] = t
    return out


def clearances(mod, sw, env):
    TS, TM = cKDTree(sw), cKDTree(env)
    res = {}
    for name, t in mod.items():
        p = t.reshape(-1, 3)
        res[name] = (TS.query(p)[0].min(), TM.query(p)[0].min(), p)
    return res


def draw(side, mod, fixed, stick, mec, res):
    s = 1 if side == "left" else -1
    fig, (top, front, elev) = plt.subplots(1, 3, figsize=(17, 6.6), gridspec_kw={"width_ratios": [1.05, 1, 1.15]})
    yl = (4.2, 9.4) if s > 0 else (-9.4, -4.2)

    def keep(t, x=(5.4, 11.0), z=6.8):
        cc = t.mean(1)
        return t[(cc[:, 0] > x[0]) & (cc[:, 0] < x[1]) & (cc[:, 1] > yl[0]) & (cc[:, 1] < yl[1]) & (cc[:, 2] < z)]
    for group, col, z0 in ((fixed, "#d4d4d4", 1), (mec, "#9e9e9e", 2), (stick, "#c3b1e1", 2)):
        for n, t in group:
            t = keep(t)
            if not len(t):
                continue
            c = "#f4a259" if "5 Hole Lowside U-Channel" in n else col
            z = 3 if c == "#f4a259" else z0
            top.add_collection(PolyCollection(t[t[:, :, 2].min(1) < 4.8][:, :, :2], facecolors=c, edgecolors="none", zorder=z))
            fr = t[t[:, :, 0].min(1) > 7.6]                       # the front view: what is ahead of the drive wheel
            front.add_collection(PolyCollection(fr[:, :, 1:], facecolors=c, edgecolors="none", zorder=z))
            e = t[(t[:, :, 1].mean(1) * s) > 5.0]
            elev.add_collection(PolyCollection(e[:, :, [0, 2]], facecolors=c, edgecolors="none", zorder=z))
    order = ["wheel", "hub", "servo", "bracket", "screws"]
    for name in order:
        t = mod[name]
        for ax_, cols in ((top, [0, 1]), (front, [1, 2]), (elev, [0, 2])):
            ax_.add_collection(PolyCollection(t[:, :, cols], facecolors=COLOURS[name], edgecolors="none", alpha=0.92,
                                              zorder=6 + order.index(name)))
    kw = dict(fontsize=8, zorder=30, bbox=dict(fc="white", ec="none", alpha=0.88), arrowprops=dict(arrowstyle="->", lw=1.1))
    fy = s * P.FLANGE_Y
    front.annotate("TAB on the side channel's FRONT flange:\n2 x M4 x 12 through its holes at\nZ %.2f and %.2f in, nuts behind\n"
                   "(ahead of the Viper slide)" % P.FLANGE_HOLES_Z, xy=(fy, P.FLANGE_HOLES_Z[1]),
                   xytext=(s * 8.9, 5.3), color="#c00000", **kw)
    front.annotate("intake side channel\n(5 Hole Lowside U-Channel)", xy=(fy, 5.8), xytext=(s * 8.9, 6.5), **kw)
    front.annotate("belly under the channel, Z 2.66..2.86:\n2 x M4 x 10 up into the Dual Block", xy=(s * 5.05, 2.76), xytext=(s * 7.4, 4.6), **kw)
    top.annotate("tab on the front flange", xy=(P.FLANGE_X + 0.08, fy), xytext=(5.6, s * 4.5 if s > 0 else -4.6), color="#c00000", **kw)
    top.annotate("wheel axle\n(X %.2f, Y %+.2f)" % (P.AXLE[0], s * P.AXLE[1]), xy=(P.AXLE[0], s * P.AXLE[1]),
                 xytext=(9.6, s * 4.5), **kw)
    top.annotate("front drive wheel", xy=(6.4, s * 6.2), xytext=(5.5, s * 8.7), **kw)
    top.annotate("stick arm (drawn down;\nthe servo is 0.26 in\ninboard of it)", xy=(10.2, s * 6.84), xytext=(9.6, s * 8.6), **kw)
    elev.annotate("servo, body back,\nup to Z 3.80", xy=(8.7, 3.6), xytext=(6.0, 5.3), **kw)
    elev.annotate("goBILDA 1908 25T hub", xy=(9.3, 2.0), xytext=(9.9, 2.9), **kw)
    elev.annotate("%.1f in TPU wheel,\nflaps Z 0.60..1.65" % (P.WHEEL_OD / 25.4), xy=(9.9, 1.1), xytext=(9.95, 0.15), **kw)
    lines = ["Clearances (in), %s corner:" % side]
    for name in ("servo", "bracket", "wheel", "hub"):
        d_s, d_m, p = res[name]
        lines.append(f"  {name}: stick's swing {d_s:.2f}, spinning mecanum {d_m:.2f}")
    allp = np.vstack([r[2] for r in res.values()])
    lines.append(f"  front X {allp[:, 0].max():.2f}: {allp[:, 0].max() + 7.57:.2f} in from the back wall")
    lines.append(f"  out to |Y| {np.abs(allp[:, 1]).max():.2f}: inside the frame's side (|Y| 7.77)")
    elev.text(5.5, 6.55, "\n".join(lines), fontsize=7.5, va="top", zorder=30, bbox=dict(fc="white", ec="#888", alpha=0.95))
    for a in (top, elev):
        a.axvline(10.43, color="#7030a0", ls="--", lw=1)
        a.set_xlim(5.4, 11.0)
        a.set_xlabel("X forward (in)")
    for a in (top, front, elev):
        a.set_aspect("equal")
        a.grid(True, lw=0.3, alpha=0.5)
    top.set_ylim(*yl)
    top.set_ylabel("Y left (in)")
    top.set_title("Top view (robot below Z 4.8)", fontsize=9)
    front.set_xlim(*(yl[::-1] if s > 0 else yl))
    front.set_ylim(0, 7.0)
    front.set_xlabel("Y left (in)")
    front.set_ylabel("Z up (in)")
    front.set_title("From the front (robot ahead of X 7.6)", fontsize=9)
    elev.set_ylim(0, 6.8)
    elev.set_ylabel("Z up (in)")
    elev.set_title("From the side (robot outboard of |Y| 5.0)", fontsize=9)
    for a in (top, front, elev):
        a.axhline(0, color="k", lw=0.8) if a is not top else None
    hs = [Patch(color="#f4a259", label="what the tab bolts to"), Patch(color="#9e9e9e", label="front drive wheel"),
          Patch(color="#c3b1e1", label="FLOWER stick (down)"), Patch(color="#d4d4d4", label="rest of the robot"),
          Patch(color=COLOURS["bracket"], label="bracket (PETG)"), Patch(color=COLOURS["servo"], label="servo"),
          Patch(color=COLOURS["hub"], label="25T hub"), Patch(color=COLOURS["wheel"], label="TPU wheel"),
          Patch(color=COLOURS["screws"], label="screws")]
    fig.suptitle(f"{side.upper()} corner module on the robot's own CAD (STEP of 24 Nov 2026), to scale; "
                 f"purple dashed: the 18 in starting cube's front", fontsize=10)
    fig.tight_layout(rect=(0, 0.06, 1, 1))
    fig.legend(handles=hs, fontsize=8, loc="lower center", ncol=9, framealpha=0.9)
    fig.savefig(os.path.join(OUT, f"{side}-corner-module.png"), dpi=110)
    plt.close(fig)


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    fixed, stick, mec = robot(sys.argv[1])
    sw, env = swept(stick), spun(mec)
    os.makedirs(OUT, exist_ok=True)
    for side in ("right", "left"):
        mod = module_tris(side)
        res = clearances(mod, sw, env)
        for name, (d_s, d_m, p) in res.items():
            print(f"{side:5s} {name:8s} stick {d_s:.2f}  spinning mecanum {d_m:.2f}  X {p[:, 0].min():.2f}..{p[:, 0].max():.2f}")
        draw(side, mod, fixed, stick, mec, res)


if __name__ == "__main__":
    main()
