"""The mentor's goBILDA launcher as one drop-in module: his "Launcher Concept" (the goBILDA 3208-0004-0001 turret over
two pairs of 96 mm flywheels, their channels and the dual blocks that tie it to his chassis), with cad/transfer's changes:
the flywheel motors moved out and up, the modules 16 mm in, the turret's servo drive and encoders, and the feeder, gate,
pad and backstop. Placed as on our full robot (cad/full-robot placed_team: his launcher 0.8 in forward).

    MOTORS_FROM=... python3 cad/modules/launcher-module/build.py <his 9 Oct Robot.step>

Writes launcher-module.step next to this file: millimetres, robot frame (+X forward, +Y left, +Z up, origin on the floor
under his chassis centre, his front face at X 7.56 in), and prints the column's position, the envelope and the parts of
his chassis it touches (its interface)."""
import os, re, sys, importlib.util
import numpy as np, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "..", "full-robot"))
spec = importlib.util.spec_from_file_location("fr", os.path.join(HERE, "..", "..", "full-robot", "build.py"))
FR = importlib.util.module_from_spec(spec); spec.loader.exec_module(FR)
TR, RP, C, F, FACE, IN = FR.TR, FR.RP, FR.C, FR.F, FR.FACE, FR.IN
HIS = re.compile(r"^(Launcher Concept|Dual Block \(GB\))")
OURS_FIXED = re.compile(r"^(feeder|gate|pad|backstop)|^(screw|nut)_?.*(feeder|gate|pad|backstop)")
FAST = re.compile(r"[Ss]crew|Nut|[Ww]asher|2800-|2802-|2812-|2806-|2829-|CAGE|e-clip| text")

def robot_box(shape):
    b = shape.BoundingBox()
    return np.array([(b.zmin - (FACE - 7.56 * IN)) / IN, (b.xmin - C) / IN, (b.ymin - F) / IN]), \
           np.array([(b.zmax - (FACE - 7.56 * IN)) / IN, (b.xmax - C) / IN, (b.ymax - F) / IN])

if __name__ == "__main__":
    top = cq.Assembly(name="launcher module: the mentor's launcher with cad/transfer's changes (robot frame, mm)")
    his = cq.Assembly(name="the mentor's Launcher Concept (as placed on our robot)")
    ours = cq.Assembly(name="cad/transfer: flywheel motors, turret drive and encoders, feeder, gate, pad, backstop")
    boxes, rest = [], []
    for path, shp, loc, col, key in FR.placed_team(sys.argv[1]):
        p = " / ".join(path); s = cq.Shape.cast(shp.wrapped.Moved(loc))
        if HIS.search(p):
            his.add(shp, name=FR.unique(his, FR.part_name(path[-1])), loc=cq.Location(loc), color=cq.Color(*col))
            if not FAST.search(p): boxes.append((p, robot_box(s)))
        elif not FAST.search(p): rest.append((p, robot_box(s)))
    for d, rx in ((TR.launcher, None), (TR.fixed, OURS_FIXED)):
        for n, (wp, col, kind) in d.items():
            if rx and not rx.search(n): continue
            s = TR.to_cad(wp); ours.add(s, name=n, color=cq.Color(*col))
            if not FAST.search(n): boxes.append((n, robot_box(s)))
    where = cq.Location(RP.to_model(C, F, FACE))
    top.add(his, loc=where); top.add(ours, loc=where)
    top.save(os.path.join(HERE, "launcher-module.step"))
    lo = np.min([b[0] for _, b in boxes], 0); hi = np.max([b[1] for _, b in boxes], 0)
    print(f"column (turret axis): X {TR.COL_X:.3f}  Y {TR.TG[1]:.3f} in; drive gear at X {TR.TG[0]:.3f} (points +X, forward)")
    print(f"envelope: X {lo[0]:.2f}..{hi[0]:.2f}  Y {lo[1]:.2f}..{hi[1]:.2f}  z {lo[2]:.2f}..{hi[2]:.2f} in")
    print("interface (his non-launcher parts within 0.02 in of a module part):")
    seen = set()
    for pr, (rl, rh) in rest:
        for pm, (ml, mh) in boxes:
            if (rl <= mh + 0.02).all() and (ml <= rh + 0.02).all():
                k = (pr.split(" / ")[-1], pm.split(" / ")[-1])
                if k not in seen:
                    seen.add(k); print(f"  {k[0][:45]:45s} X {rl[0]:6.2f}..{rh[0]:6.2f} Y {rl[1]:6.2f}..{rh[1]:6.2f} z {rl[2]:5.2f}..{rh[2]:5.2f}  <-> {k[1][:55]}")
