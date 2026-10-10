"""Brings the mentor's drive corners (cad/modules/drive-module on claude/robotics-meeting-notes-lq2y55: each corner's
wheel, shaft, hub, bearings, pattern plates, pulleys, belt, motor and the motor's mount on the rail, from his 8 and 9 Oct
files) into the printed chassis, each corner placed by its own axle. His rails and rear channel (the "frame" group) are
left out: this chassis draws its own rails.

    git show origin/claude/robotics-meeting-notes-lq2y55:cad/modules/drive-module/drive-module.step.gz | gunzip > /tmp/dm.step
    python3 cad/printed-chassis/drive_module.py /tmp/dm.step      # writes cad/printed-chassis/.cache/drive/ (not in git)
"""
import json, os, re, sys
import cadquery as cq
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
from OCP.BRepTools import BRepTools
from launcher_module import read_groups

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, ".cache", "drive")
HIS_AXLE_X = {1: 3.782, -1: -5.667}       # the CAD chat's read of his 9 Oct file (inches)
MOTOR = re.compile(r"^(Gearbox|5000-)")
WHEEL = re.compile(r"^(1600-0307|3606|[Rr]oller|1610-|.*[Rr]oller)")

def _side(parts):
    b = Bnd_Box()
    for _, sh in parts: BRepBndLib.Add_s(sh, b)
    x0, y0, z0, x1, y1, z1 = b.Get()
    return (1 if (x0 + x1) / 2 > 0 else -1, 1 if (y0 + y1) / 2 > 0 else -1)

if __name__ == "__main__":
    import importlib.util
    spec = importlib.util.spec_from_file_location("pc", os.path.join(HERE, "build.py")); pc = importlib.util.module_from_spec(spec)
    os.environ["NO_DRIVE_MODULE"] = "1"; os.environ["DM_LAYOUT"] = "1"; spec.loader.exec_module(pc)
    groups = read_groups(sys.argv[1])
    MIRROR = os.environ.get("NO_MIRROR") != "1"     # his right corners aren't mirrors of his left (FR's motor sits 0.74 in further
                                                    # forward, its shaft 0.1 in further out): build both robots' right corners as
                                                    # mirrors of his left ones, so all four are one design
    os.makedirs(OUT, exist_ok=True)
    index, seen = [], {}
    for g, parts in groups.items():
        if len(parts) < 20: continue                                     # the frame group: his rails and rear channel
        b = Bnd_Box()
        for _, sh in parts: BRepBndLib.Add_s(sh, b)
        x0, y0, z0, x1, y1, z1 = b.Get()
        sx = 1 if (x0 + x1) / 2 > 0 else -1; sy = 1 if (y0 + y1) / 2 > 0 else -1
        corner = ("F" if sx > 0 else "B") + ("L" if sy > 0 else "R")
        if MIRROR and sy < 0:
            keep = [(nm, sh) for nm, sh in parts if nm.startswith("1107-")]          # the rear cross channel both rear motors share
            left = next(p2 for g2, p2 in groups.items() if len(p2) >= 20 and _side(p2) == (sx, 1))
            parts = keep + [(nm, cq.Shape.cast(sh).mirror("XZ").wrapped) for nm, sh in left if not nm.startswith("1107-")]
        dx = (pc.AXLE[sx] - HIS_AXLE_X[sx]) * 25.4
        # ~240 roller and bearing parts: drawn as the wheel's envelope, a 96 mm cylinder between its side plates
        # (the rollers' own bounding boxes are loose), on the shaft's axis
        wb, sb = Bnd_Box(), Bnd_Box()
        for nm, sh in parts:
            if nm.startswith("3606-0000-0096"): BRepBndLib.Add_s(sh, wb)
            if nm.startswith("72mm Steel Shaft"): BRepBndLib.Add_s(sh, sb)
        _, b0, _, _, b1, _ = [v / 25.4 for v in wb.Get()]
        s0, _, t0, s1, _, t1 = [v / 25.4 for v in sb.Get()]
        env = cq.Solid.makeCylinder(48 / 25.4, b1 - b0, cq.Vector((s0 + s1) / 2 + dx / 25.4, b0, (t0 + t1) / 2), cq.Vector(0, 1, 0))
        BRepTools.Write_s(env.wrapped, os.path.join(OUT, f"{corner}_wheel.brep"))
        index.append({"name": "goBILDA 3606-0000-0096 96 mm mecanum wheel (drawn as its envelope)", "corner": corner, "file": f"{corner}_wheel.brep"})
        parts = [(nm, sh) for nm, sh in parts if not WHEEL.search(nm)]
        motor = [sh for nm, sh in parts if MOTOR.search(nm)]                # the Yellow Jacket's ~50 internal parts, as one
        comp = cq.Compound.makeCompound([cq.Shape.cast(sh) for sh in motor]).translate(cq.Vector(dx, 0, 0)).scale(1 / 25.4)
        BRepTools.Write_s(comp.wrapped, os.path.join(OUT, f"{corner}_motor.brep"))
        index.append({"name": "goBILDA 5203-2402-0014 Yellow Jacket, 435 RPM (his 2-stage 5203)", "corner": corner, "file": f"{corner}_motor.brep"})
        parts = [(nm, sh) for nm, sh in parts if not MOTOR.search(nm)]
        for nm, sh in parts:
            s = cq.Shape.cast(sh).translate(cq.Vector(dx, 0, 0)).scale(1 / 25.4)
            base = corner + "_" + re.sub(r"[^A-Za-z0-9_.-]+", "_", nm)[:50]; seen[base] = seen.get(base, 0) + 1
            fn = f"{base}_{seen[base]}.brep"
            BRepTools.Write_s(s.wrapped, os.path.join(OUT, fn))
            index.append({"name": nm, "corner": corner, "file": fn})
        print(corner, len(parts), "parts, moved", round(dx / 25.4, 3), "in")
    json.dump({"parts": index}, open(os.path.join(OUT, "index.json"), "w"), indent=1)
