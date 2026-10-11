"""goBILDA's own models of the bought parts, for the exported STEP and the views. build.py's checks still use each part's
envelope (its placeholder); these only dress it.

    python3 cad/printed-chassis/vendor.py      # fetches the STEP zips into .cache/vendor/ (not in git) and caches each model

Each model is cached in inches, in its own frame (MODELS below): origin at the point build.py places by (a motor's
gearbox face on its shaft's axis, a wheel's centre, a channel's corner), local Z along its axis, local X along a stated
edge. Its colours come from the vendor's file, one compound per colour. build.py places a model with dress(); without
the cache (no network), the export falls back to the placeholders and says so.
"""
import io, json, os, re, sys, urllib.request, zipfile
import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))
CACHE = os.path.join(HERE, ".cache", "vendor")
URL = "https://www.gobilda.com/content/step_files/{}.zip"

# key: (SKU, file in its zip (regex), origin (mm, vendor frame), local Z (vendor), local X (vendor), what the frame is)
# Read from each file (vendor.py --probe prints a model's extents); goBILDA's frames differ part to part.
MODELS = {
    "5203-2402-0014": ("5203-2402-0014", r"\.step$", (-37.17, 101.74, -11.08), (0, 1, 0), (1, 0, 0), "gearbox face on the shaft's axis; Z out along the shaft"),
    "5203-2402-0005": ("5203-2402-0005", r"\.step$", (-37.17, 92.86, -11.08), (0, 1, 0), (1, 0, 0), "the same, one-stage gearbox"),
    "5203-2402-0003": ("5203-2402-0003", r"\.step$", (-37.17, 92.86, -11.08), (0, 1, 0), (1, 0, 0), "the same"),
    "5203-2402-0001": ("5203-2402-0001", r"\.step$", (-37.17, 92.86, -11.08), (0, 1, 0), (1, 0, 0), "the same"),
    "mecanum_A": ("3213-3606-0002", r"3606-0000-0096 assembly\.step$", (-49.45, 15.30, 3.195), (0, 0, 1), (1, 0, 0), "the wheel's centre; Z along the axle. Front left and back right"),
    "mecanum_B": ("3213-3606-0002", r"3606-0100-0096 assembly\.step$", (-8.96, -4.27, 0.13), (0, 0, 1), (1, 0, 0), "the same, the other hand: front right and back left"),
    "3417-4008-0024": ("3417-4008-0024", r"\.step$", (0, 0, 6.0), (0, 0, 1), (1, 0, 0), "24T pulley: the middle of its teeth, on its axis"),
    "3417-4008-0016": ("3417-4008-0016", r"\.step$", (0, 0, 0), (0, 0, 1), (1, 0, 0), "16T pulley: the same"),
    "3632-4008-0048": ("3632-4008-0048", r"\.step$", (0, 0, 0), (0, 1, 0), (1, 0, 0), "48 mm Gecko wheel's centre"),
    "2000-0025-0002": ("2000-0025-0002", r"\.step$", (-10.0, 0, 11.05), (0, 0, 1), (1, 0, 0), "servo: the case's spline face on the spline's axis; Z out along the spline, X toward the case's far end"),
    "2000-0025-0003": ("2000-0025-0003", r"\.step$", (-10.0, 0, 11.05), (0, 0, 1), (1, 0, 0), "the same"),
    "3110-0001-0002": ("3110-0001-0002", r"0.*96mm DRIVE WHEELS.*\.step$", (-5.5, 0, -48.0), (0, 0, 1), (1, 0, 0), "odometry pod: its back plate's face, centred, at the floor; X out from the plate, the way the wheel rolls"),
    "1121-0014-0360": ("1121-0014-0360", r"\.step$", (-24.0, 0, -45.5), (0, 0, 1), (1, 0, 0), "low-side channel: the web's outer face, its end, one edge; X into the flanges, Y along it"),
    "1121-0009-0240": ("1121-0009-0240", r"\.step$", (-24.0, 0, -45.5), (0, 0, 1), (1, 0, 0), "the same"),
    "1120-0009-0240": ("1120-0009-0240", r"\.step$", (-24.0, 0, -45.5), (0, 0, 1), (1, 0, 0), "U-channel: a flange's outer face, its end, the open edge; X across, Y along, Z to the web"),
    "1123-0048-0336": ("1123-0048-0336", r"\.step$", (-24.0, 0, -45.5), (0, 0, 1), (1, 0, 0), "pattern plate: one face, its end, one edge; X through it, Y along it"),
    "1123-0048-0144": ("1123-0048-0144", r"\.step$", (-24.0, 0, -45.5), (0, 0, 1), (1, 0, 0), "the same"),
    "1501-0006-0560": ("1501-0006-0560", r"\.step$", (0, -28.0, 0), (0, 1, 0), (1, 0, 0), "M4 standoff: one end, on its axis"),
    "1501-0006-0480": ("1501-0006-0480", r"\.step$", None, (0, 1, 0), (1, 0, 0), "the same (origin: its lower end, found on load)"),
    "1501-0006-0400": ("1501-0006-0400", r"\.step$", None, (0, 1, 0), (1, 0, 0), "the same"),
    "1501-0006-0240": ("1501-0006-0240", r"\.step$", None, (0, 1, 0), (1, 0, 0), "the same"),
}

def _read(data, pattern):
    """The STEP in the zip as [(shape, (r, g, b))], one entry per colour, in the vendor's frame (mm)."""
    from OCP.STEPCAFControl import STEPCAFControl_Reader
    from OCP.TDocStd import TDocStd_Document
    from OCP.TCollection import TCollection_ExtendedString
    from OCP.XCAFDoc import XCAFDoc_DocumentTool, XCAFDoc_ColorTool, XCAFDoc_ColorSurf, XCAFDoc_ColorGen
    from OCP.TDF import TDF_LabelSequence, TDF_Label
    from OCP.TopLoc import TopLoc_Location
    from OCP.Quantity import Quantity_Color
    from OCP.TopExp import TopExp_Explorer
    from OCP.TopAbs import TopAbs_SOLID, TopAbs_FACE
    z = zipfile.ZipFile(io.BytesIO(data))
    name = next(n for n in z.namelist() if re.search(pattern, n, re.I) and not n.endswith("/"))
    tmp = os.path.join(CACHE, "_read.step")
    with open(tmp, "wb") as f: f.write(z.read(name))
    doc = TDocStd_Document(TCollection_ExtendedString("doc"))
    r = STEPCAFControl_Reader(); r.SetColorMode(True); r.SetNameMode(True); r.ReadFile(tmp); r.Transfer(doc)
    os.remove(tmp)
    st, ct = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main()), XCAFDoc_DocumentTool.ColorTool_s(doc.Main())
    def colour_of(obj):
        c = Quantity_Color()
        for t in (XCAFDoc_ColorSurf, XCAFDoc_ColorGen):
            ok = XCAFDoc_ColorTool.GetColor_s(obj, t, c) if isinstance(obj, TDF_Label) else ct.GetColor(obj, t, c)
            if ok: return (c.Red(), c.Green(), c.Blue())
        return None
    groups = {}
    def add(sh, inherited):
        ex = TopExp_Explorer(sh, TopAbs_SOLID)
        while ex.More():
            so = ex.Current(); c = colour_of(so) or inherited
            if c is None:                                   # colour on the faces only: the commonest face colour
                fe, seen = TopExp_Explorer(so, TopAbs_FACE), {}
                while fe.More():
                    fc = colour_of(fe.Current())
                    if fc: seen[fc] = seen.get(fc, 0) + 1
                    fe.Next()
                c = max(seen, key=seen.get) if seen else (0.6, 0.6, 0.62)
            groups.setdefault(tuple(round(v, 2) for v in c), []).append(so)
            ex.Next()
    def walk(lab, loc, inherited):
        c = colour_of(lab) or inherited
        if st.IsReference_s(lab):
            r2 = TDF_Label(); st.GetReferredShape_s(lab, r2)
            walk(r2, loc.Multiplied(st.GetLocation_s(lab)), c); return
        if st.IsAssembly_s(lab):
            seq = TDF_LabelSequence(); st.GetComponents_s(lab, seq)
            for i in range(1, seq.Length() + 1): walk(seq.Value(i), loc, c)
            return
        add(st.GetShape_s(lab).Moved(loc), c)
    roots = TDF_LabelSequence(); st.GetFreeShapes(roots)
    for i in range(1, roots.Length() + 1): walk(roots.Value(i), TopLoc_Location(), None)
    return [(cq.Compound.makeCompound([cq.Shape.cast(s) for s in v]), c) for c, v in groups.items()]

# The plane each model is symmetric about, (local axis, where along it), for placing its mirror image on the other side:
# local Y = 0 unless listed. The channels and plates run from 0 along Y, so they flip across their width instead.
SYMMETRY = {k: (2, 24 / 25.4) for k in ("1121-0014-0360", "1121-0009-0240", "1123-0048-0336", "1123-0048-0144")}
SYMMETRY["1120-0009-0240"] = (0, 24 / 25.4)

# Colours for files that carry none (goBILDA's motors, wheels and channels), by each solid's place in the local frame (inches)
ALUMINIUM, STEEL, BLACK = (0.78, 0.79, 0.81), (0.70, 0.71, 0.73), (0.13, 0.13, 0.14)
def _paint_motor(b):
    w = max(b.xlen, b.ylen)
    if w >= 1.46 and b.zmin < -1.5: return (0.98, 0.78, 0.10)      # the yellow jacket on the can
    if b.zmin < -2.5: return BLACK                                  # the can and its end cap
    if b.zmin > -0.7 and w < 0.4: return STEEL                      # the output shaft
    return (0.30, 0.30, 0.32)                                       # the gearbox
def _paint_mecanum(b):
    return BLACK if (b.center.x ** 2 + b.center.y ** 2) ** 0.5 > 1.2 else (0.62, 0.63, 0.66)   # rollers round a hub
PAINT = {"5203-": _paint_motor, "mecanum_": _paint_mecanum, "112": lambda b: ALUMINIUM, "1501-": lambda b: ALUMINIUM}

def _frame(origin, zv, xv):
    """The vendor-to-local transform: origin to 0, zv to +Z, xv to +X, then millimetres to inches."""
    z, x = cq.Vector(*zv).normalized(), cq.Vector(*xv).normalized()
    pl = cq.Plane(origin=cq.Vector(*origin), xDir=x, normal=z)
    return pl.fG                                   # global (vendor) to the plane's local coordinates

def fetch(keys=None):
    os.makedirs(CACHE, exist_ok=True)
    zips = {}
    for key in keys or MODELS:
        sku, pat, origin, zv, xv, _ = MODELS[key]
        out = os.path.join(CACHE, key)
        if os.path.exists(os.path.join(out, "index.json")): continue
        if sku not in zips:
            zp = os.path.join(CACHE, sku + ".zip")
            if not os.path.exists(zp):
                print("fetching", sku); urllib.request.urlretrieve(URL.format(sku), zp)
            zips[sku] = open(zp, "rb").read()
        parts = _read(zips[sku], pat)
        if origin is None:                          # a standoff: its lower end along its axis
            b = cq.Compound.makeCompound([s for s, _ in parts]).BoundingBox()
            origin = (b.center.x, b.ymin, b.center.z)
        T = _frame(origin, zv, xv)
        parts = [(s.transformShape(T).scale(1 / 25.4), c) for s, c in parts]
        rule = next((f for pre, f in PAINT.items() if key.startswith(pre)), None)
        if rule and len(parts) == 1:
            g = {}
            for so in parts[0][0].Solids(): g.setdefault(rule(so.BoundingBox()), []).append(so)
            parts = [(cq.Compound.makeCompound(v), c) for c, v in g.items()]
        os.makedirs(out, exist_ok=True); index = []
        for i, (s, c) in enumerate(parts):
            fn = f"{i}.brep"; s.exportBrep(os.path.join(out, fn)); index.append({"file": fn, "colour": c})
        json.dump({"sku": sku, "parts": index}, open(os.path.join(out, "index.json"), "w"), indent=1)
        b = cq.Compound.makeCompound([cq.Shape.importBrep(os.path.join(out, e["file"])) for e in index]).BoundingBox()
        print(f"{key:16s} {len(index)} colours, local extents (in): X {b.xmin:6.2f} {b.xmax:6.2f}  Y {b.ymin:6.2f} {b.ymax:6.2f}  Z {b.zmin:6.2f} {b.zmax:6.2f}")

_LOADED, _BOX = {}, {}
def box_of(sh):
    if id(sh) not in _BOX or _BOX[id(sh)] is None:
        b = sh.BoundingBox(); _BOX[id(sh)] = [b.xmin, b.xmax, b.ymin, b.ymax, b.zmin, b.zmax]
    return _BOX[id(sh)]

_HULL = {}
def hull(sh, path=None):
    """Points on a shape's surface (its coarse mesh's vertices), once per shape; kept beside a cached model."""
    import numpy as np
    if id(sh) not in _HULL:
        if path and os.path.exists(path): _HULL[id(sh)] = np.load(path)
        else:
            v, _ = sh.tessellate(0.02, 0.5); _HULL[id(sh)] = np.array([(q.x, q.y, q.z) for q in v])
            if path: np.save(path, _HULL[id(sh)])
    return _HULL[id(sh)]

def placed_box(sh, loc):
    """The box round a shape placed at loc, from its hull's points (within the mesh's 0.02 in)."""
    import numpy as np
    t = loc.wrapped.Transformation(); M = np.array([[t.Value(i, j) for j in range(1, 5)] for i in range(1, 4)])
    p = hull(sh) @ M[:, :3].T + M[:, 3]
    lo, hi = p.min(0), p.max(0)
    return [lo[0], hi[0], lo[1], hi[1], lo[2], hi[2]]

def model(key):
    """[(shape in inches, local frame, colour)] for a cached model, or None."""
    if key not in _LOADED:
        p = os.path.join(CACHE, key, "index.json")
        if not os.path.exists(p): _LOADED[key] = None
        else:
            idx = json.load(open(p))
            _LOADED[key] = [(cq.Shape.importBrep(os.path.join(CACHE, key, e["file"])), tuple(e["colour"])) for e in idx["parts"]]
            if any("box" not in e for e in idx["parts"]):     # each compound's box in its own frame, once (the exact one is slow)
                for (sh, _), e in zip(_LOADED[key], idx["parts"]): e["box"] = box_of(sh)
                json.dump(idx, open(p, "w"), indent=1)
            for (sh, _), e in zip(_LOADED[key], idx["parts"]):
                _BOX[id(sh)] = e["box"]; hull(sh, os.path.join(CACHE, key, e["file"][:-5] + ".hull.npy"))
    return _LOADED[key]

if __name__ == "__main__":
    fetch(sys.argv[1:] or None)
