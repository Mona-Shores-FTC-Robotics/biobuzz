"""goBILDA's own CAD for the parts we buy, so the robot drawings show the real motor, servo, pulleys, bearings, wheels
and collars instead of cylinders and boxes.

goBILDA publishes a STEP for every SKU at https://www.gobilda.com/content/step_files/<sku>.zip. Put the zips, as
downloaded, in `cad/gobilda/step/` (gitignored: they are goBILDA's files, a few MB each) or a folder GOBILDA_DIR
names. Without them the builders fall back to their stand-in solids, so a checkout with no downloads still builds.

    python3 cad/gobilda/parts.py 5203-2402-0005      # prints the STEP's parts and their bounding boxes (mm)

Each SKU's STEP has its own origin and axis; AXES records where the shaft axis (or the servo's spline) is in it, read
off with the command above, so a builder can say "this bore, on this line" with `on_axis`.
"""
import os, sys, zipfile, functools, importlib.util
import cadquery as cq
from OCP.gp import gp_Trsf, gp_Ax3, gp_Pnt, gp_Dir
from OCP.TopLoc import TopLoc_Location

HERE = os.path.dirname(os.path.abspath(__file__))
URL = "https://www.gobilda.com/content/step_files/{sku}.zip"
DIR = os.environ.get("GOBILDA_DIR", os.path.join(HERE, "step"))   # cad/gobilda/step/ is gitignored
_spec = importlib.util.spec_from_file_location("real_parts", os.path.join(HERE, "..", "full-robot", "real_parts.py"))
_rp = importlib.util.module_from_spec(_spec); _spec.loader.exec_module(_rp)
read_leaves = _rp.read_leaves

def available(sku):
    return bool(DIR) and os.path.exists(os.path.join(DIR, sku + ".zip"))

def step_path(sku):
    """The STEP inside <GOBILDA_DIR>/<sku>.zip, unzipped next to it on first use."""
    z = os.path.join(DIR, sku + ".zip"); out = os.path.join(DIR, sku)
    if not os.path.isdir(out):
        with zipfile.ZipFile(z) as f: f.extractall(out)
    for root, _, files in os.walk(out):
        for n in files:
            if n.lower().endswith((".step", ".stp")): return os.path.join(root, n)
    raise FileNotFoundError(f"no STEP in {z}")

@functools.lru_cache(maxsize=None)
def load(sku):
    """[(part name, cq.Shape, (r, g, b))] in the STEP's own frame, mm, colours kept."""
    return [(names[-1] if names else sku, s, None if col == (0.6, 0.62, 0.66) else col) for names, s, col in read_leaves(step_path(sku))]

def moved(parts, trsf):
    loc = TopLoc_Location(trsf)
    return [(n, cq.Shape.cast(s.wrapped.Moved(loc)), col) for n, s, col in parts]

def on_axis(sku, point, direction, x_dir, origin=(0, 0, 0), axis=(0, 0, 1), x_ref=(1, 0, 0)):
    """The SKU's parts placed so that its line (origin, axis) lies on (point, direction) and its x_ref turns to x_dir.
    origin/axis/x_ref are in the STEP's frame (from AXES); point/direction/x_dir in the caller's."""
    fr = gp_Ax3(gp_Pnt(*origin), gp_Dir(*axis), gp_Dir(*x_ref))
    to = gp_Ax3(gp_Pnt(*point), gp_Dir(*direction), gp_Dir(*x_dir))
    t = gp_Trsf(); t.SetDisplacement(fr, to)
    return moved(load(sku), t)

def trsf(sku, point, direction, x_dir):
    a = AXES[sku]
    fr = gp_Ax3(gp_Pnt(*a["origin"]), gp_Dir(*a["axis"]), gp_Dir(*a["x_ref"]))
    to = gp_Ax3(gp_Pnt(*point), gp_Dir(*direction), gp_Dir(*x_dir))
    t = gp_Trsf(); t.SetDisplacement(fr, to); return t

def placed(sku, point, direction, x_dir, col=None):
    """[(name suffix, cq.Workplane placed, colour, base cq.Shape, gp_Trsf)] of the SKU with its reference face at point and
    its length along direction, or None when its STEP isn't downloaded. Parts without a colour of their own get col.
    Export the base shape with the transform as its location (not the placed copy): then a STEP holds one body however
    many times the part is used."""
    if not available(sku): return None
    t = trsf(sku, point, direction, x_dir); ps = load(sku); multi = len(ps) > 1
    return [(f" / {k:02d} {n}" if multi else "", cq.Workplane("XY").add(s.moved(cq.Location(t))), c or col or (0.6, 0.62, 0.66), s, t)
            for k, (n, s, c) in enumerate(ps)]

# Each STEP's frame, read with the command above: origin = a reference face on the part's axis, axis = the direction the
# part's length runs from it, x_ref = a side direction (which way a screw or the long side of a body points).
AXES = {
    "2910-0920-4008": dict(origin=(-28.85, -1.06, 18.85), axis=(0, -1, 0), x_ref=(1, 0, 0)),    # clamping collar: face, 9 mm long; the screw along x_ref
    "1611-0514-4008": dict(origin=(0, 5.0, 0), axis=(0, -1, 0), x_ref=(1, 0, 0)),               # flanged bearing: flange's outer face; flange 1.0, body (14 mm) to 4.8
    "3417-4008-0024": dict(origin=(0, 0, 0), axis=(0, 0, 1), x_ref=(1, 0, 0)),                  # 24T pulley: one face, 12 mm long
    "3613-4008-0048": dict(origin=(0, -8.0, 0), axis=(0, 1, 0), x_ref=(1, 0, 0)),               # 48 mm gecko wheel: one face, 16 mm wide
    "2000-0025-0002": dict(origin=(-10.0, 0, 12.8), axis=(0, 0, 1), x_ref=(1, 0, 0)),          # servo: the case's top face at the spline; spline out 4.1, body back 40; x_ref = the long side (30 mm); ears 10.3 back, holes at +-24, +-5
    "5203-2402-0005": dict(origin=(-37.17, 92.86, -11.08), axis=(0, 1, 0), x_ref=(1, 0, 0)),   # Yellow Jacket: the gearbox's mounting face (a 0.25 lip over the r18 body); shaft out 23.5, body back 107.7; M4 threads on a 16 mm square
}

if __name__ == "__main__":
    for sku in sys.argv[1:]:
        parts = load(sku); print(sku, step_path(sku), len(parts), "parts")
        allb = cq.Compound.makeCompound([s for _, s, _ in parts]).BoundingBox()
        print(f"  all: x {allb.xmin:.2f}..{allb.xmax:.2f}  y {allb.ymin:.2f}..{allb.ymax:.2f}  z {allb.zmin:.2f}..{allb.zmax:.2f}")
        for n, s, col in parts:
            b = s.BoundingBox()
            print(f"  {n[:60]:60s} x {b.xmin:8.2f}..{b.xmax:8.2f} y {b.ymin:8.2f}..{b.ymax:8.2f} z {b.zmin:8.2f}..{b.zmax:8.2f}  rgb {tuple(round(c, 2) for c in col)}")
