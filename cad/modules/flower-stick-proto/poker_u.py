"""The poker: an L over the intake's front channel (the flat 9-hole lowside U-channel across the front above the intake wheels,
web on top, the only channel there): a wall down the front lip and a roof flat on the web. Final form, 27 Oct 2026, after the
first night on 20245:

- No back wall: the first print's stopped the intake sliding to its rest and was broken off.
- 10 mm relief holes in the roof over the two screws standing up out of the web (the pattern's rows 8 mm from the lips, 32 mm
  to one side of the channel's centre), cut both sides so the L fits either way round.
- Four pegs under the roof, into the web's 4.5 mm holes around the channel's middle bearing hole, so the L can only go on
  centred and cannot slide: the first print went on one hole off centre, dodging the screw heads.
- 80 mm long, so the intake's round hard stop under the lip, moved inboard to clear the stick's servo, sits beside it.
- The roof stops 10 mm short of the web's back edge (`ROOF_BACK`): the stars' servo mount has tabs standing over that edge.
  The rear screw's relief becomes a notch open to the back; the pegs, 32 mm back, keep 4 mm of roof behind them.
- The front wall's holes are the lip's holes: six, 16, 24 and 32 mm either side of the centre, so a tie goes wherever is
  easiest and a lip hole shows through each one.

Fastening: zip ties, for keeps. Two thin ones through the roof's 4.5 mm tie holes, 16 mm either side of the centre (down the
front hole, under the web inside the channel, up the rear hole, closed on top; the motor leaves room under the web there), and
two through the front wall, one each side (in through a wall hole and the lip's hole behind, back out the next pair, closed on
the outside). The buttress covers the roof's centre, so the tie holes sit beside it; the 12 mm holes over the bearing holes
are for seeing the channel's holes through. The ball's push goes into the poke, down the buttress into the roof and the front wall, onto the lip, in
compression; the pegs take the sideways load and the ties only keep the L from lifting.

    python3 cad/modules/flower-stick-proto/poker_u.py                 # stl/poker_u.stl, step/poker_u.step (web top 5.61 up)
    WEB_UP=6.55 NAME=poker_u_hi python3 .../poker_u.py                 # the one on the robots: the channel's top 6.55 in up
    HALF_L=36 NAME=poker_u_72 python3 .../poker_u.py                   # a 72 mm one, if a stop lands closer than 60 mm from centre
    SCREWS=1 WEB_UP=6.55 NAME=poker_u_hi_bolt python3 .../poker_u.py  # bolted: the web's front screw, 4 mm longer, goes through the roof and clamps it

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
ROOF_BACK = float(os.environ.get("ROOF_BACK", "38"))        # how far back from the front lip the roof reaches: 38 of the web's 48, leaving the rear 10 mm clear for the stars' servo mount tabs that stand over the web's back edge
RELIEF = [(-8.0, y) for y in (32.0, -32.0)] + [(-40.0, y) for y in (32.0, -32.0)]   # 10 mm relief holes centred on the screws standing out of the web: the pattern's rows 8 mm in from each lip, 32 mm along, both sides
PEGS = [(-24.0 + dx, dy) for dx in (-8.2, 8.2) for dy in (-8.2, 8.2)]   # the web's 4.5 mm holes around the middle bearing hole
PEG_D, PEG_H = 3.9, 2.0                                     # the pegs: 0.3 mm of play in the hole, 2 mm into a 2.5 mm web, never through it
ROOF_HOLES = [y for y in (-48.0, -24.0, 24.0, 48.0) if abs(y) <= HALF_L - 8.0]   # the 12 mm roof holes over the channel's bearing holes that fit, to see them through (the middle one would be under the buttress)
TIE_HOLES = [(x, y) for y in (-15.8, 15.8) for x in (-15.8, -32.2)]   # 4.5 mm holes for the roof ties, over the web's 4.5 mm holes beside the pegs: each tie goes down the front one, under the web, up the rear one; clear of the buttress
WEB_UP = float(os.environ.get("WEB_UP", "5.61"))            # the channel's top above the tile with the intake down, inches: measure it
REACH, POKE_W, POKE_LO, POKE_HI = 1.3 * IN, 19.0, (5.7 - WEB_UP) * IN, (7.5 - WEB_UP) * IN   # the poke: 1.3 in ahead of the face, 0.75 wide, 5.7 to 7.5 up
HOLE_Z, HOLE_D = -8.0, 4.3                                  # the lip's holes: 8 mm below the web top, 4 mm, 8 mm apart from the channel's centre
HOLE_YS = [y for k in range(1, 8) for y in (8.0 * k, -8.0 * k) if 8.0 * k <= HALF_L - 8.0 and 8.0 * k >= POKE_W / 2 + HOLE_D / 2 + 1.0]   # every lip hole the wall covers and the poke does not hide: 16, 24, 32 each side
BUTTRESS_BACK = -36.0                                       # the buttress runs from the poke's top back to here on the roof
x_in, x_out = CLR, CLR + WALL_T                             # the front wall, outside the lip
roof_z0, roof_z1 = 0.0, ROOF_T
roof = cq.Workplane("XY").box(ROOF_BACK + CLR + WALL_T, 2 * HALF_L, ROOF_T, centered=(False, True, False)).translate((-ROOF_BACK, 0, roof_z0))
front = cq.Workplane("XY").box(WALL_T, 2 * HALF_L, FLANGE_H + roof_z1, centered=(False, True, False)).translate((x_in, 0, -FLANGE_H))
poke = cq.Workplane("XY").box(REACH - x_out, POKE_W, POKE_HI - POKE_LO, centered=(False, True, False)).translate((x_out, 0, POKE_LO))
buttress = cq.Workplane("XZ").polyline([(BUTTRESS_BACK, roof_z1 - 0.01), (x_out + 0.01, roof_z1 - 0.01), (x_out + 0.01, POKE_HI)]).close().extrude(POKE_W / 2, both=True)
part = roof.union(front).union(poke).union(buttress)
for x, y in PEGS:   # the locating pegs under the roof, chamfered to find their holes
    part = part.union(cq.Workplane("XY").circle(PEG_D / 2).extrude(-PEG_H).translate((x, y, roof_z0)).faces("<Z").chamfer(0.6))
for y in HOLE_YS:   # the wall's holes on the lip's holes, through the front wall only
    part = part.cut(cq.Workplane("YZ").circle(HOLE_D / 2).extrude(20, both=True).translate((x_in + WALL_T / 2, y, HOLE_Z)))
SCREWS = bool(os.environ.get("SCREWS"))                      # SCREWS=1: the front screw standing out of the web goes through the roof (4.5 mm hole, a screw 4 mm longer) and clamps the L; the rear one is past the roof's end
for x, y in RELIEF:
    part = part.cut(cq.Workplane("XY").circle(2.25 if SCREWS and x > -ROOF_BACK + 5 else 5.0).extrude(ROOF_T + 2).translate((x, y, roof_z0 - 1)))   # a screw hole only where the roof fully covers it; the rear one is a notch either way
for x, y in TIE_HOLES:
    part = part.cut(cq.Workplane("XY").circle(2.25).extrude(ROOF_T + 2).translate((x, y, roof_z0 - 1)))
# 12 mm holes in the roof over the channel's 14 mm bearing holes (its centreline, 24 mm pitch), to see them through
for y in ROOF_HOLES:
    part = part.cut(cq.Workplane("XY").circle(6.0).extrude(ROOF_T + 2).translate((-CHAN_D / 2, y, roof_z0 - 1)))
v = part.val(); assert len(part.solids().vals()) == 1 and v.isValid()
NAME = os.environ.get("NAME", "poker_u")
cq.exporters.export(part, os.path.join(HERE, "stl", NAME + ".stl"), tolerance=0.05, angularTolerance=0.2)
cq.exporters.export(part, os.path.join(HERE, "step", NAME + ".step"))
bb = v.BoundingBox(); print(f"stl/{NAME}.stl: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3")
