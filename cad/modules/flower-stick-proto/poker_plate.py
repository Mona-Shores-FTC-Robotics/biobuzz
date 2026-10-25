"""The poker as a plate bolted down on the intake's top channel. What the export says about that channel (the 9-hole
lowside U across the intake at the face line, 48 mm deep, web on top at 5.61 in with the intake lowered): the intake motor
fills its cavity near the centre (body 0.4 in left to 2.0 right, up to 5.45, the web's underside at 5.13; encoder cap to 1.1
left), the intake's gearbox fills it on the right out to 3.3 in, the cross channel butts against its back flange and stands
1 in above the web, and two pattern spacers sit on the web top beyond 3.0 in either side. What is free: the web's top
between the spacers, and the cavity under the bearing holes 1.89 and 2.83 in from the centre on the motor's encoder side (the gearbox is on the other). So: the plate sits on the web
from the cross channel's face to the front flange, 2.6 in to the gearbox side and 3.1 in to the encoder side with a tab out to the second boss, two
bosses into those two holes with an M4 x 16 down each and a nut below, a lip turned down over the front flange, and the
poke off the front 1.3 in ahead of the intake face, 0.75 in wide, 5.7 to 7.5 in up with the intake lowered.

    python3 cad/modules/flower-stick-proto/poker_plate.py     # stl/poker_plate.stl, step/poker_plate.step

Loads: the ball pushes the poke toward the robot: the plate's rear edge bears on the cross channel's face (compression) and
the tip-back puts the bolts in tension through the web, with the lip bearing on the flange. A sideways hit works against two
bosses in two holes 24 mm apart, the lip along 5.7 in of flange and the plate's 5.7 in bearing on the web. Nuts go on from
below, between the intake wheels and the web. Millimetres; x ahead of the channel's front face, y across (+ left), z up from
the web's top surface.
"""
import os, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); os.makedirs(os.path.join(HERE, "step"), exist_ok=True)
IN = 25.4
REACH, POKE_W, POKE_LO, POKE_HI = 33.0, 19.0, 2.3, 48.0     # the poke: 1.3 in ahead, 0.75 wide, web top +0.09 in to +1.9 in (5.7 to 7.5 up)
CH_D, T = 48.0, 6.0                                          # the channel's depth; the plate's thickness
Y0, Y1 = -2.6 * IN, 3.1 * IN                                 # the plate's main body: 2.6 in to the gearbox side (+y) and 3.1 in to the encoder side (-y), short of the spacers
TAB_Y0, TAB_X0 = -3.05 * IN, -1.2 * IN                        # a tab out to the second boss, kept ahead of the spacer that sits on the web beyond 3.03 in
LIP_T, LIP_DN = 4.0, 15.0                                    # the lip down the front flange
BOSSES = [(-0.945 * IN, -1.89 * IN), (-0.945 * IN, -2.83 * IN)]   # the bearing holes with a clear cavity below (the right side has the gearbox under it)
BOSS_D, BOSS_H = 13.8, 6.0
plate = cq.Workplane("XY").box(CH_D - 0.5 + 0.3, Y1 - Y0, T, centered=(False, False, False)).translate((-(CH_D - 0.5), Y0, 0))   # 0.5 mm off the cross channel's face
plate = plate.union(cq.Workplane("XY").box(0.3 - TAB_X0, Y0 - TAB_Y0, T, centered=(False, False, False)).translate((TAB_X0, TAB_Y0, 0)))
lip = cq.Workplane("XY").box(LIP_T, Y1 - TAB_Y0, LIP_DN + T, centered=(False, False, False)).translate((0.3, TAB_Y0, -LIP_DN))
poke = cq.Workplane("XY").box(REACH, POKE_W, POKE_HI - POKE_LO, centered=(False, True, False)).translate((0, 0, POKE_LO))
gus = cq.Workplane("XZ").polyline([(0, T), (-24, T), (0, T + 24)]).close().extrude(POKE_W / 2, both=True)       # behind the poke
part = plate.union(lip).union(poke).union(gus)
for (bx, by) in BOSSES:
    boss = cq.Workplane("XY").circle(BOSS_D / 2).extrude(BOSS_H).translate((bx, by, -BOSS_H)).edges("<Z").chamfer(0.8)
    part = part.union(boss)
    part = part.cut(cq.Workplane("XY").circle(2.25).extrude(BOSS_H + T + 2).translate((bx, by, -BOSS_H - 1)))               # M4 clearance
    part = part.cut(cq.Workplane("XY").circle(4.0).extrude(T).translate((bx, by, T - 3.0)))                                  # a 3 mm counterbore for the head
v = part.val(); assert len(part.solids().vals()) == 1 and v.isValid()
cq.exporters.export(part, os.path.join(HERE, "stl", "poker_plate.stl"), tolerance=0.05, angularTolerance=0.2)
cq.exporters.export(part, os.path.join(HERE, "step", "poker_plate.step"))
bb = v.BoundingBox(); print(f"stl/poker_plate.stl: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3")
