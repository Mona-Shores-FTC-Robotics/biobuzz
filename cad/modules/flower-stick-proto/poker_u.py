"""The poker: an upside-down U that sits over the intake's front channel (the flat 9-hole lowside U-channel across the front
above the intake wheels, web on top, 12 mm flanges down, the only channel there), with the poke and its buttress on top. Four
holes, two in the front wall and two in the back wall, sit on the channel's 4 mm flange holes 40 mm either side of its centre:
a zip tie through each (in through the wall and the flange, under the flange's bottom edge, back out) for the test; 4 mm pop
rivets or M4 button heads from inside for keeps, if the coach allows (the intake motor is 3.5 mm behind the front flange).

    python3 cad/modules/flower-stick-proto/poker_u.py     # stl/poker_u.stl, step/poker_u.step

The push from the ball goes into the poke, down the buttress into the roof, and through the front wall into the channel's front
flange, in compression; the ties only keep the U from lifting or sliding. Heights follow the channel: with its web top 5.61 in
above the tile (intake lowered) the poke spans 5.7 to 7.5 in, 1.3 in ahead of the flange's face, which is what the model wants
(page/pokez_scan.js: 24/24 at every speed; the folded blade passes 2.8 mm under it). Measure the web's height on the robot with
the intake down. Millimetres; x ahead of the front flange's face, y across from the channel's centre, z up from the web top.
"""
import os, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); os.makedirs(os.path.join(HERE, "step"), exist_ok=True); os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
IN = 25.4
CHAN_D, FLANGE_H, CLR = 48.0, 12.0, 0.3                     # the channel: 48 mm deep, flanges 12 mm down from the web top; 0.3 mm each side
ROOF_T, WALL_T, HALF_L = 4.0, 4.0, 52.0                     # the U: 4 mm roof and walls, 104 mm long (ties 80 mm apart)
HOLE_Y, HOLE_Z, HOLE_D = 40.0, -8.0, 4.3                    # the channel's flange holes: 8 mm pitch from its centre, 8 mm below the web top, 4 mm
REACH, POKE_W, POKE_LO, POKE_HI = 1.3 * IN, 19.0, (5.7 - 5.607) * IN, (7.5 - 5.607) * IN   # the poke: 1.3 in ahead of the face, 0.75 wide, 5.7 to 7.5 up
BUTTRESS_BACK = -36.0                                       # the buttress runs from the poke's top back to here on the roof
x_in, x_out = CLR, CLR + WALL_T                             # the front wall; the back wall mirrors it about the channel's depth
roof = cq.Workplane("XY").box(CHAN_D + 2 * CLR + 2 * WALL_T, 2 * HALF_L, ROOF_T, centered=(False, True, False)).translate((-CHAN_D - CLR - WALL_T, 0, 0))
front = cq.Workplane("XY").box(WALL_T, 2 * HALF_L, FLANGE_H + ROOF_T, centered=(False, True, False)).translate((x_in, 0, -FLANGE_H))
back = cq.Workplane("XY").box(WALL_T, 2 * HALF_L, FLANGE_H + ROOF_T, centered=(False, True, False)).translate((-CHAN_D - CLR - WALL_T, 0, -FLANGE_H))
poke = cq.Workplane("XY").box(REACH - x_out, POKE_W, POKE_HI - POKE_LO, centered=(False, True, False)).translate((x_out, 0, POKE_LO))
buttress = cq.Workplane("XZ").polyline([(BUTTRESS_BACK, ROOF_T - 0.01), (x_out + 0.01, ROOF_T - 0.01), (x_out + 0.01, POKE_HI)]).close().extrude(POKE_W / 2, both=True)
part = roof.union(front).union(back).union(poke).union(buttress)
for y in (HOLE_Y, -HOLE_Y):
    part = part.cut(cq.Workplane("YZ").circle(HOLE_D / 2).extrude(CHAN_D + 20, both=True).translate((-CHAN_D / 2, y, HOLE_Z)))
v = part.val(); assert len(part.solids().vals()) == 1 and v.isValid()
cq.exporters.export(part, os.path.join(HERE, "stl", "poker_u.stl"), tolerance=0.05, angularTolerance=0.2)
cq.exporters.export(part, os.path.join(HERE, "step", "poker_u.step"))
bb = v.BoundingBox(); print(f"stl/poker_u.stl: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3")
