"""The poker: an L over the intake's front channel (the flat 9-hole lowside U-channel across the front above the intake wheels,
web on top, the only channel there): a wall down the front lip and a roof flat on the web. Final form, 27 Oct 2026, after the
first night on 20245:

- No back wall: the first print's stopped the intake sliding to its rest and was broken off.
- 10 mm relief holes in the roof over the two screws standing up out of the web (the pattern's rows 8 mm from the lips, 32 mm
  to one side of the channel's centre), cut both sides so the L fits either way round.
- Four pegs under the roof, into the web's 4.5 mm holes around the channel's middle bearing hole, so the L can only go on
  centred and cannot slide: the first print went on one hole off centre, dodging the screw heads.
- 80 mm long, so the intake's round hard stop under the lip, moved inboard to clear the stick's servo, sits beside it.

Fastening: zip ties, for keeps. Two wide ones down through the roof's 12 mm holes over the channel's bearing holes (down one,
through the channel, up the next, closed on top; the motor leaves room under the web there), and if wanted two thin ones through
the front wall's holes and the lip's 4 mm holes 32 mm either side of the centre (in through the wall and the lip, under the
lip's edge, back out). The ball's push goes into the poke, down the buttress into the roof and the front wall, onto the lip, in
compression; the pegs take the sideways load and the ties only keep the L from lifting.

    python3 cad/modules/flower-stick-proto/poker_u.py                 # stl/poker_u.stl, step/poker_u.step (web top 5.61 up)
    WEB_UP=6.55 NAME=poker_u_hi python3 .../poker_u.py                 # the one on the robots: the channel's top 6.55 in up
    HALF_L=36 NAME=poker_u_72 python3 .../poker_u.py                   # a 72 mm one, if a stop lands closer than 60 mm from centre

Heights follow the channel: WEB_UP is its web top above the tile (intake lowered) and the poke spans 5.7 to 7.5 in whatever
that is, 1.3 in ahead of the lip's face (page/pokez_scan.js: 24/24 at every speed). Millimetres; x ahead of the front lip's
face, y across from the channel's centre, z up from the web top. The hole positions are read off goBILDA's own mesh of the
channel: the lip's 4 mm holes 8 mm apart from its centre, the web's 4.5 mm holes at (8.2, 8.2) from each bearing hole, the bearing holes 24 apart.
"""
import os, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); os.makedirs(os.path.join(HERE, "step"), exist_ok=True); os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
IN = 25.4
CHAN_D, FLANGE_H, CLR = 48.0, 12.0, 0.3                     # the channel: 48 mm deep, the lip 12 mm down from the web top; 0.3 mm clearance
WALL_T = 4.0
HALF_L = float(os.environ.get("HALF_L", "40"))             # half the L's length along the channel: 40 (80 mm), so the intake's round stops under the lip can come inboard to 60 mm from centre, clear of the servo
ROOF_T = 4.0
RELIEF = [(-8.0, y) for y in (32.0, -32.0)] + [(-40.0, y) for y in (32.0, -32.0)]   # 10 mm relief holes centred on the screws standing out of the web: the pattern's rows 8 mm in from each lip, 32 mm along, both sides
HOLE_Y, HOLE_Z, HOLE_D = 8.0 * int((HALF_L - 8.0) // 8.0), -8.0, 4.3   # the lip's holes: 8 mm pitch from the channel's centre, 8 mm below the web top, 4 mm; the outermost pair that fits
PEGS = [(-24.0 + dx, dy) for dx in (-8.2, 8.2) for dy in (-8.2, 8.2)]   # the web's 4.5 mm holes around the middle bearing hole
PEG_D, PEG_H = 3.9, 2.0                                     # the pegs: 0.3 mm of play in the hole, 2 mm into a 2.5 mm web, never through it
ROOF_HOLES = [y for y in (-48.0, -24.0, 0.0, 24.0, 48.0) if abs(y) <= HALF_L - 8.0]   # the roof holes over the channel's bearing holes that fit
WEB_UP = float(os.environ.get("WEB_UP", "5.61"))            # the channel's top above the tile with the intake down, inches: measure it
REACH, POKE_W, POKE_LO, POKE_HI = 1.3 * IN, 19.0, (5.7 - WEB_UP) * IN, (7.5 - WEB_UP) * IN   # the poke: 1.3 in ahead of the face, 0.75 wide, 5.7 to 7.5 up
BUTTRESS_BACK = -36.0                                       # the buttress runs from the poke's top back to here on the roof
x_in, x_out = CLR, CLR + WALL_T                             # the front wall, outside the lip
roof_z0, roof_z1 = 0.0, ROOF_T
roof = cq.Workplane("XY").box(CHAN_D + CLR + WALL_T, 2 * HALF_L, ROOF_T, centered=(False, True, False)).translate((-CHAN_D, 0, roof_z0))
front = cq.Workplane("XY").box(WALL_T, 2 * HALF_L, FLANGE_H + roof_z1, centered=(False, True, False)).translate((x_in, 0, -FLANGE_H))
poke = cq.Workplane("XY").box(REACH - x_out, POKE_W, POKE_HI - POKE_LO, centered=(False, True, False)).translate((x_out, 0, POKE_LO))
buttress = cq.Workplane("XZ").polyline([(BUTTRESS_BACK, roof_z1 - 0.01), (x_out + 0.01, roof_z1 - 0.01), (x_out + 0.01, POKE_HI)]).close().extrude(POKE_W / 2, both=True)
part = roof.union(front).union(poke).union(buttress)
for x, y in PEGS:   # the locating pegs under the roof, chamfered to find their holes
    part = part.union(cq.Workplane("XY").circle(PEG_D / 2).extrude(-PEG_H).translate((x, y, roof_z0)).faces("<Z").chamfer(0.6))
for y in (HOLE_Y, -HOLE_Y):   # the lip ties, through the front wall only
    part = part.cut(cq.Workplane("YZ").circle(HOLE_D / 2).extrude(20, both=True).translate((x_in + WALL_T / 2, y, HOLE_Z)))
for x, y in RELIEF:
    part = part.cut(cq.Workplane("XY").circle(5.0).extrude(ROOF_T + 2).translate((x, y, roof_z0 - 1)))
# three 12 mm holes in the roof over the channel's 14 mm bearing holes (its centreline, 24 mm pitch), for the roof ties
for y in ROOF_HOLES:
    part = part.cut(cq.Workplane("XY").circle(6.0).extrude(ROOF_T + 2).translate((-CHAN_D / 2, y, roof_z0 - 1)))
v = part.val(); assert len(part.solids().vals()) == 1 and v.isValid()
NAME = os.environ.get("NAME", "poker_u")
cq.exporters.export(part, os.path.join(HERE, "stl", NAME + ".stl"), tolerance=0.05, angularTolerance=0.2)
cq.exporters.export(part, os.path.join(HERE, "step", NAME + ".step"))
bb = v.BoundingBox(); print(f"stl/{NAME}.stl: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3")
