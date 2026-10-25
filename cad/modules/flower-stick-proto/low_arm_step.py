"""The low arm as a STEP for the Fresh CAD assembly (low_arm.py's geometry): two arm channels on a bracket off each chassis
side rail, the pivot pins, the shaft with its two 39 mm wheels, the wide mountain blade with ears, and the 1.2 in poker on
the intake's top channel. Deployed and stowed, each in the "flowerextractordown" export's own coordinates, so either
file inserts at the Fresh CAD origin and lands on the robot.

    python3 cad/modules/flower-stick-proto/low_arm_step.py    # step/low_arm_deployed.step, step/low_arm_stowed.step

The channels are drawn as plain bars with their 24 mm holes (goBILDA 5-hole lowside U-channel, 144 x 32 x 13 mm); the
bracket is a block from the rail's front end to the pivot, to be replaced by whatever goBILDA bracket reaches.
"""
import os, sys, json, math, importlib.util, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); IN = 25.4
A = json.load(open(os.path.join(HERE, "page", "low_arm.json")))
sys.path.insert(0, "/home/user/biobuzz/cad/full-robot")
sp = importlib.util.spec_from_file_location("fr", "/home/user/biobuzz/cad/full-robot/build.py"); FR = importlib.util.module_from_spec(sp); sp.loader.exec_module(FR)
C, F, FACE = FR.C, FR.F, FR.FACE; DX, DZ = -115.62, 71.73                      # the export's alignment (list.txt)
FACE_L, FLOOR_U, ACROSS0 = 5.35, 4.75, 2.21                                    # the export's frame: ahead = l - 5.35, up = u - 4.75, left = 2.21 - f
px, pz = A["pivot"]; L = A["L"]; H = A["hole"]; ang = math.radians(A["ang_dep_deg"]); sx, sz = A["shaft_dep"]; lip = A["lip_ahead"]
def mm(v): return v * IN
def place_y(shape, y0mm):
    """shift a shape along y so its lowest y is y0mm"""
    return shape.translate((0, y0mm - shape.val().BoundingBox().ymin, 0))
parts = []                                                                      # (name, solid) in the face frame, mm: X ahead, Y left, Z up
# the arms: a bar along the arm with 5 holes, pivot at hole 1, shaft at hole 4; standing on edge 13 mm thick in y
for sg, (y0, y1) in zip((1, -1), (A["arm_y"], [-A["arm_y"][1], -A["arm_y"][0]])):
    # the channel runs from one hole behind the pivot to 12 mm past the shaft hole (cut there), and its lower corner at the tip is
    # cut off parallel to the tile when deployed, 0.1 in up: a 32 mm channel ending at a shaft 0.77 in up would otherwise dig in
    along = mm(4 * H) + 12.0
    bar = cq.Workplane("XZ").center(along / 2 - mm(H), 0).rect(along, mm(A["arm_w"])).extrude(mm(y1 - y0) / 2, both=True)
    for k in range(4): bar = bar.cut(cq.Workplane("XZ").circle(7.0).extrude(mm(y1 - y0), both=True).translate((mm(k * H), 0, 0)))
    bar = bar.rotate((0, 0, 0), (0, 1, 0), -math.degrees(ang)).translate((mm(px), 0, mm(pz)))
    bar = bar.intersect(cq.Workplane("XY").box(500, 500, 500, centered=(True, True, False)).translate((0, 0, mm(0.1))))
    bar = place_y(bar, mm(y0))
    parts.append((f"arm_{'left' if sg > 0 else 'right'}", bar))
    # the bracket: from the rail's front end to the pivot, outboard of the arm
    by0, by1 = (A["arm_y"][1], 5.35) if sg > 0 else (-5.35, -A["arm_y"][1])
    br = cq.Workplane("XY").box(mm(1.25), mm(by1 - by0), mm(1.89), centered=(False, False, False)).translate((mm(-0.5), mm(by0), mm(1.22)))
    parts.append((f"bracket_{'left' if sg > 0 else 'right'}", br))
    pin = place_y(cq.Workplane("XZ").circle(4.0).extrude(mm(1.0), both=True).translate((mm(px), 0, mm(pz))), mm(3.5 if sg > 0 else -5.5))
    parts.append((f"pivot_pin_{'left' if sg > 0 else 'right'}", pin))
shaft = place_y(cq.Workplane("XZ").circle(4.0).extrude(156, both=True).translate((mm(sx), 0, mm(sz))), -156); parts.append(("shaft_312_REX", shaft))
for sg in (1, -1):
    wy0, wy1 = A["wheel_y"]; w = place_y(cq.Workplane("XZ").circle(mm(A["wheel_r"])).extrude(mm(wy1 - wy0) / 2, both=True).translate((mm(sx), 0, mm(sz))), mm(wy0 if sg > 0 else -wy1))
    parts.append((f"wheel_{'left' if sg > 0 else 'right'}", w))
# the blade with ears: its file has x behind the lip, y across, z up from its bottom (0.52 up); turn it round and place it
blade = cq.importers.importStep(os.path.join(HERE, "step", "mountain_wide_ears.step")).rotate((0, 0, 0), (0, 0, 1), 180).translate((mm(lip), 0, mm(0.52)))
parts.append(("blade_mountain_wide_ears", blade))
# the poker, standing on the intake's top channel (lowered intake: web top 5.61 up); its file: x ahead of the channel face, z up from the web
poker = cq.importers.importStep(os.path.join(HERE, "step", "poker_stand.step")).translate((0, 0, mm(5.61)))
parts.append(("poker_stand_1.2in", poker))
for n, sh in parts:
    bb = sh.val().BoundingBox(); print(f"  {n:26s} ahead {bb.xmin / IN:6.2f}..{bb.xmax / IN:6.2f}  left {bb.ymin / IN:6.2f}..{bb.ymax / IN:6.2f}  up {bb.zmin / IN:5.2f}..{bb.zmax / IN:5.2f}")
def to_raw(shape):
    """face frame (mm: ahead, left, up) -> the export's raw coordinates: (ahead, up, -left) plus the offsets"""
    s = shape.rotate((0, 0, 0), (1, 0, 0), -90)
    return s.translate((FACE_L * IN + C - DX, FLOOR_U * IN + F, ACROSS0 * IN + FACE - 7.56 * IN - DZ))
def assembly(moving_rot_deg, name):
    asm = cq.Assembly(name=name)
    for n, s in parts:
        if n.startswith(("arm", "shaft", "wheel", "blade")) and moving_rot_deg:
            s = s.rotate((mm(px), 0, mm(pz)), (mm(px), 1, mm(pz)), -moving_rot_deg)   # about the pivot axis (y); CadQuery's sign: +y axis, right-hand rule
        asm.add(to_raw(s), name=n, color=cq.Color("orange" if "blade" in n or "poker" in n else "gray"))
    out = os.path.join(HERE, "step", f"{name}.step"); asm.save(out); return out
print(assembly(0, "low_arm_deployed"))
print(assembly(A["stow_deg"] - A["ang_dep_deg"], "low_arm_stowed"))
# a check in the face frame: the blade's lip and the shaft, and the stowed foremost point
bb = blade.val().BoundingBox(); print(f"blade in the face frame: ahead {bb.xmin / IN:.2f}..{bb.xmax / IN:.2f}, left {bb.ymin / IN:.2f}..{bb.ymax / IN:.2f}, up {bb.zmin / IN:.2f}..{bb.zmax / IN:.2f}")
