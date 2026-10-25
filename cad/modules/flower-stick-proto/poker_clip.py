"""The poker as a saddle: no hardware. An upside-down U that drops over the intake's top channel (the 9-hole lowside
U-channel across the intake at the face line: 48 mm deep, 12 mm tall, web on top, flanges down), with the poke rising off
its front 1.3 in ahead of the intake face, 0.75 in wide, 5.7 to 7.5 in up with the intake lowered (page/pokez_scan.js: 24/24 at every speed; the folded blade's lip reaches 5.59, so the poke starts above it).

    python3 cad/modules/flower-stick-proto/poker_clip.py     # stl/poker_clip.stl, step/poker_clip.step

How it goes on: hook the front wall's lip under the channel's front flange, then rotate the saddle down so the back wall drops
behind the back flange and the peg drops into the web's centre 14 mm hole. It cannot slide forward (the back wall is behind
the back flange) and cannot lift at the front (the lip is under the front flange). The ball pushes the poke toward the robot,
which presses the front wall onto the front flange and the rear of the saddle onto the web: compression, nothing to loosen.
Millimetres; x ahead of the channel's front face, y across, z up from the web's top surface.
"""
import os, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); os.makedirs(os.path.join(HERE, "step"), exist_ok=True)
REACH, POKE_W = 33.0, 19.0                  # 1.3 in ahead of the face, 0.75 in wide
POKE_LO, POKE_HI = 2.3, 48.0                # the poke spans web top +0.09 in to +1.9 in: 5.7 to 7.5 up with the web top at 5.61, clear of the folded blade (5.59)
CH_D, CH_H, CLR = 48.0, 12.2, 0.3           # the channel: 48 deep, 12.2 tall; 0.3 mm clearance each side
W, T, LIP = 40.0, 4.0, 2.5                  # the saddle: 40 wide, 4 mm walls and top, a 2.5 mm lip under the front flange
PEG_D, PEG_H = 13.8, 6.0                    # one locating peg in the web's centre hole, 24 mm behind the front face
top = cq.Workplane("XY").box(CH_D + 2 * CLR + 2 * T, W, T, centered=(False, True, False)).translate((-(CH_D + CLR + T), 0, 0))
front = cq.Workplane("XY").box(T, W, CH_H + CLR + LIP + T, centered=(False, True, False)).translate((CLR, 0, -(CH_H + CLR + LIP)))
lip = cq.Workplane("XY").box(LIP + CLR, W, LIP, centered=(False, True, False)).translate((-LIP, 0, -(CH_H + CLR + LIP)))
lip = lip.edges("<Z and <X").chamfer(1.5)                                                         # a lead-in so it hooks on
back = cq.Workplane("XY").box(T, W, CH_H + CLR + T, centered=(False, True, False)).translate((-(CH_D + CLR + T), 0, -(CH_H + CLR)))
poke = cq.Workplane("XY").box(REACH, POKE_W, POKE_HI - POKE_LO, centered=(False, True, False)).translate((0, 0, POKE_LO))
peg = cq.Workplane("XY").circle(PEG_D / 2).extrude(PEG_H).translate((-24.0, 0, -PEG_H)).edges("<Z").chamfer(1.0)
peg = peg.cut(cq.Workplane("XY").box(PEG_D + 2, 1.2, PEG_H - 1.5, centered=(True, True, False)).translate((-24.0, 0, -PEG_H)))
gus = cq.Workplane("XZ").polyline([(0, T), (-18, T), (0, T + 18)]).close().extrude(POKE_W / 2, both=True)   # behind the poke, on the top
part = top.union(front).union(lip).union(back).union(poke).union(peg).union(gus)
v = part.val(); assert len(part.solids().vals()) == 1 and v.isValid()
cq.exporters.export(part, os.path.join(HERE, "stl", "poker_clip.stl"), tolerance=0.05, angularTolerance=0.2)
cq.exporters.export(part, os.path.join(HERE, "step", "poker_clip.step"))
bb = v.BoundingBox(); print(f"stl/poker_clip.stl: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3")
