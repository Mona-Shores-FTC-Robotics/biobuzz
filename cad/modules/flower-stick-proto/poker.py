"""The poker: a printed bracket that hooks over the intake's top channel and puts a 0.75 in wide plate 1.2 in ahead of the
intake face, where it meets the second ball of a front-to-back stack as the lip meets the posts and pushes it back onto
the stack. Its position comes from the channel, not from whoever bolts it on: the flange sits on the channel's top web
(two M4 bolts through the web's holes, slotted 14 mm so any 8 mm-pitch pair works), the block hangs down the channel's
front face (one M4 through the face), and the block's front is the poker.

    python3 cad/modules/flower-stick-proto/poker.py     # stl/poker_hang.stl, stl/poker_stand.stl, step/, poker.png

"hang": the block hangs below the web, for the intake where the October 25 CAD has it (web top 7.51 in up: the poker
then spans 5.3 to 7.5 in). "stand": the block rises above the web, for the intake lowered 1.8 in for the FLOWER (web
top 5.73: the poker spans 5.7 to 7.9). Which one is right depends on where the intake sits while the stick feeds.
Millimetres; x forward (ahead of the channel's front face), y across, z up from the channel's web top.
"""
import os, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); os.makedirs(os.path.join(HERE, "step"), exist_ok=True)
REACH, W, H = 30.5, 19.0, 56.0          # 1.2 in ahead of the face, 0.75 in wide, 2.2 in tall (5.3 to 7.5 in up)
CH_DEPTH, CH_FACE = 48.0, 12.0          # goBILDA low-side U-channel: 48 mm deep, 12 mm tall front face
FL_T = 6.0                              # the flange on the web
def build(mode):
    flange = cq.Workplane("XY").box(CH_DEPTH, W, FL_T, centered=(False, True, False)).translate((-CH_DEPTH, 0, 0))
    for sx in (-8.0, 8.0):               # slots 14 mm long along the channel's depth, M4 clearance
        flange = flange.cut(cq.Workplane("XY").slot2D(14.0, 4.4, angle=0).extrude(FL_T + 2).translate((-CH_DEPTH / 2 + sx, 0, -1)))
    if mode == "hang":
        block = cq.Workplane("XY").box(REACH, W, H + FL_T, centered=(False, True, False)).translate((0, 0, -H))
        face_bolt_z = -CH_FACE / 2       # through the channel's front face
    else:
        block = cq.Workplane("XY").box(REACH, W, H, centered=(False, True, False)).translate((0, 0, 0))
        face_bolt_z = None               # standing up there is no channel face behind the block; a web bolt pair holds it
    part = flange.union(block)
    if face_bolt_z is not None:          # a counterbored M4 from the front, through the block into the channel face
        part = part.cut(cq.Workplane("YZ").circle(2.2).extrude(REACH + 2).translate((-1, 0, face_bolt_z)))
        part = part.cut(cq.Workplane("YZ").circle(4.2).extrude(REACH - 8).translate((8, 0, face_bolt_z)))
    # a gusset between the flange and the block, outside the channel: the corner above the web, 6 mm tall, 10 mm back
    gus = cq.Workplane("XZ").polyline([(0, FL_T), (-10, FL_T), (0, FL_T + 10)]).close().extrude(W / 2, both=True)
    if mode == "hang": part = part.union(gus)
    v = part.val(); assert len(part.solids().vals()) == 1 and v.isValid(), mode
    cq.exporters.export(part, os.path.join(HERE, "stl", f"poker_{mode}.stl"), tolerance=0.05, angularTolerance=0.2)
    cq.exporters.export(part, os.path.join(HERE, "step", f"poker_{mode}.step"))
    bb = v.BoundingBox(); print(f"stl/poker_{mode}.stl: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3")
for m in ("hang", "stand"): build(m)
