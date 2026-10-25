"""The poker, bolted to the intake's cross channel. What the export says about the intake's front: the top channel (a 9-hole
lowside U across the intake at the face line, 48 mm deep, web on top at 5.61 in with the intake lowered) has the intake
motor inside its cavity at the centre and the gearbox on one side, so nothing can go through its web near the middle; and
the intake's cross channel butts against that channel's back flange and stands 24.6 mm above the web, its 32 mm face toward
the FLOWER with the goBILDA pattern on it, and nothing behind its holes 1.89 in either side of centre. So the poker fastens
there: a back plate against the cross channel's face with two bosses into its 14 mm bearing holes at +-1.89 in and an M4 x 16
through each, nut inside the channel behind; a seat plate lying on the top channel's web with a lip over its front flange, the lip with two zip-tie holes in line with the
flange's own holes; and the poke rising off the front, 1.3 in ahead of the intake face, 0.75 in wide, 5.7 to 7.5 in up with the intake lowered, with a
wedge the full depth of the seat behind it, from the poke's top down to the back plate. Symmetric, nothing reaching sideways.

    python3 cad/modules/flower-stick-proto/poker_plate.py            # stl/poker_plate.stl, step/poker_plate.step (bolted)
    python3 cad/modules/flower-stick-proto/poker_plate.py --test     # stl/poker_test.stl: flat back, 14.5 mm holes, for tape or zip ties

Loads: the ball pushes the poke toward the robot: down the wedge, into the back plate, onto the cross channel's face, in
compression; the bolts only hold the bracket on. The tip-back lifts the front: the lip over the flange and the two bolts in
bending resist it; the seat on the web takes the rest. A side hit is two bosses in two holes 3.8 in apart. Millimetres;
x ahead of the top channel's front face, y across, z up from its web's top surface.
"""
import os, sys, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); os.makedirs(os.path.join(HERE, "step"), exist_ok=True)
TEST = "--test" in sys.argv        # the test nose: the same shape with a flat back and two 14.5 mm holes instead of the bosses, so four
                                   # zip ties hold it (two through the lip and the flat channel's front-flange holes, two through the back
                                   # plate and the standing channel's bearing holes); the bolted one waits on the coach
IN = 25.4
REACH, POKE_W, POKE_LO, POKE_HI = 33.0, 19.0, 2.3, 48.0     # the poke: 1.3 in ahead, 0.75 wide, web top +0.09 in to +1.9 in (5.7 to 7.5 up)
CH_D, T = 48.0, 6.0                                          # the top channel's depth; the plates' thickness
HALF_W = 58.0                                                # the bracket is 116 mm wide: the two bosses at +-48 plus 10 mm
LIP_T, LIP_DN = 4.0, 15.0                                    # the lip down the front flange
LIP_TIES = [(-1.63 * IN, -6.1), (-2.58 * IN, -6.1)]           # zip-tie holes in the lip, in line with the flat channel's front-flange holes (8 mm pitch, 6.1 mm below the web top), where the cavity behind is clear of the motor
BACK_X, BACK_H = -(CH_D - 0.5), 24.0                         # the back plate: against the cross channel's face (0.5 mm off the top channel's rear), 24 mm tall
BOSS_Y, BOSS_Z, BOSS_D, BOSS_L = 48.0, 8.6, 13.8, 5.0        # the cross channel's bearing holes: +-1.89 in, its centreline 8.6 mm above the web top
seat = cq.Workplane("XY").box(-BACK_X + 0.3, 2 * HALF_W, T, centered=(False, True, False)).translate((BACK_X, 0, 0))
back = cq.Workplane("XY").box(T, 2 * HALF_W, BACK_H, centered=(False, True, False)).translate((BACK_X, 0, 0))
lip = cq.Workplane("XY").box(LIP_T, 2 * HALF_W, LIP_DN + T, centered=(False, True, False)).translate((0.3, 0, -LIP_DN))
poke = cq.Workplane("XY").box(REACH, POKE_W, POKE_HI - POKE_LO, centered=(False, True, False)).translate((0, 0, POKE_LO))
wedge = cq.Workplane("XZ").polyline([(BACK_X, T - 0.01), (0, T - 0.01), (0, POKE_HI), (BACK_X, BACK_H)]).close().extrude(POKE_W / 2, both=True)
part = seat.union(back).union(lip).union(poke).union(wedge)
for (ty, tz) in LIP_TIES:                                                                          # ties go through the lip and the flange hole, round under the flange
    part = part.cut(cq.Workplane("YZ").circle(2.0).extrude(LIP_T + 2, both=True).translate((0.3 + LIP_T / 2, ty, tz)))
for sy in (BOSS_Y, -BOSS_Y):
    if TEST:
        part = part.cut(cq.Workplane("YZ").circle(7.25).extrude(T + 2, both=True).translate((BACK_X, sy, BOSS_Z)))         # a 14.5 mm hole in line with the channel's: a zip tie goes through both
        continue
    boss = cq.Workplane("YZ").circle(BOSS_D / 2).extrude(-BOSS_L).translate((BACK_X, sy, BOSS_Z)).edges("<X").chamfer(0.8)   # into the cross channel's hole
    part = part.union(boss)
    part = part.cut(cq.Workplane("YZ").circle(2.25).extrude(T + BOSS_L + 2, both=True).translate((BACK_X, sy, BOSS_Z)))   # M4 clearance, through
    part = part.cut(cq.Workplane("YZ").circle(4.0).extrude(3.0).translate((BACK_X + T - 3.0, sy, BOSS_Z)))                  # a 3 mm counterbore for the head, from the front
v = part.val(); assert len(part.solids().vals()) == 1 and v.isValid()
NAME = "poker_test" if TEST else "poker_plate"
cq.exporters.export(part, os.path.join(HERE, "stl", NAME + ".stl"), tolerance=0.05, angularTolerance=0.2)
cq.exporters.export(part, os.path.join(HERE, "step", NAME + ".step"))
bb = v.BoundingBox(); print(f"stl/{NAME}.stl: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3")
