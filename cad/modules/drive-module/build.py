"""The mentor's drive base as one module, as he drew it on 9 Oct: the 13-hole rails, the rear 9-hole channel and its
Mini Quad Blocks, the four 96 mm mecanum wheel assemblies (shaft, hub, bearings, pattern plates, 24T pulleys) and their
belts, and the four drive motors on their mounts. His 9 Oct file dropped the motor assemblies, so they come from his
8 Oct file (MOTORS_FROM), each front one moved with its wheel. Fasteners left out. Grouped by corner (FL, FR, BL, BR) plus his frame; prints each corner's axle.

    MOTORS_FROM=<his 8 Oct Robot.step> python3 cad/modules/drive-module/build.py <his 9 Oct Robot.step>

Writes drive-module.step.gz next to this file: millimetres, robot frame (+X forward, +Y left, +Z up, origin on the floor
under his chassis centre, his front face at X 7.56 in)."""
import os, re, sys, gzip, shutil, importlib.util
import cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "..", "full-robot"))
spec = importlib.util.spec_from_file_location("fr", os.path.join(HERE, "..", "..", "full-robot", "build.py"))
FR = importlib.util.module_from_spec(spec); spec.loader.exec_module(FR)
RP, C, F, FACE = FR.RP, FR.C, FR.F, FR.FACE
KEEP = re.compile(r"^(\(restored\) )?Chassis <1>")
IN = FR.IN
FRAME = re.compile(r"Chassis <1> / (13 Hole Lowside|9 Hole Lowside|Mini Quad Block)")   # his rails and rear cross piece, for reference
CORNERS = (("FL", "FL corner: front left (wheel, shaft, hub, bearings, plates, pulleys, belt, motor and its mount)"),
           ("FR", "FR corner: front right"), ("BL", "BL corner: back left"), ("BR", "BR corner: back right"),
           ("frame", "his frame, for reference: 13-hole rails, rear 9-hole channel, Mini Quad Blocks"))
DROP = re.compile(r"Intake <1>|Launcher|Nectar|Pollen|[Ss]crew|Nut|[Ww]asher|2800-|2802-|2812-|2806-|2829-|CAGE|e-clip|shim|_9\d{4}A\d{3}| text")

if __name__ == "__main__":
    groups = {k: cq.Assembly(name=t) for k, t in CORNERS}
    n = 0; axles = {}
    for path, shp, loc, col, key in FR.placed_team(sys.argv[1], raw=True):
        p = " / ".join(path)
        if not KEEP.search(p) or DROP.search(p): continue
        b = cq.Shape.cast(shp.wrapped.Moved(loc)).BoundingBox()
        x, y = ((b.zmin + b.zmax) / 2 - (FACE - 7.56 * IN)) / IN, ((b.xmin + b.xmax) / 2 - C) / IN
        k = "frame" if FRAME.search(p) else ("F" if x > 0 else "B") + ("L" if y > 0 else "R")
        if k != "frame" and "72mm Steel Shaft" in p: axles[k] = (x, y, ((b.ymin + b.ymax) / 2 - F) / IN)
        groups[k].add(shp, name=FR.unique(groups[k], FR.part_name(path[-1])), loc=cq.Location(loc), color=cq.Color(*col)); n += 1
    top = cq.Assembly(name="the mentor's drive base, 9 Oct (motors from 8 Oct): robot frame, mm")
    for k, _ in CORNERS: top.add(groups[k], loc=cq.Location(RP.to_model(C, F, FACE)))
    for k in ("FL", "FR", "BL", "BR"): print(k, "axle (X, Y, z) in: (%.3f, %.3f, %.3f)" % axles[k])
    out = os.path.join(HERE, "drive-module.step"); top.save(out)
    with open(out, "rb") as a, gzip.open(out + ".gz", "wb", 9) as b: shutil.copyfileobj(a, b)
    os.remove(out); print("parts", n, "wrote", out + ".gz", os.path.getsize(out + ".gz") // 1_000_000, "MB")
