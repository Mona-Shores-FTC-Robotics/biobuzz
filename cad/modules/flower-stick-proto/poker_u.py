"""The poker: an L over the intake's front channel (the flat 9-hole lowside U-channel across the front above the intake wheels,
web on top, the only channel there): a wall down the front lip and a roof flat on the web, with relief holes where the two
screws stand up out of the web (26 Oct photo: in the pattern's outer rows, 32 mm to one side of the channel's centre; the
relief is cut both sides so the L fits either way round). No back wall: the first print's back wall stopped the intake sliding
to its rest (26 Oct 2026) and was broken off.

Fastening: the intake's round hard stops, moved inboard onto the roof, bolt through its goBILDA-pattern holes with their own
M4s and clamp the L to the channel (the best fastening: no ties, and the stops locate it). For a test without the stops, zip ties: two through the front wall's holes and the lip's 4 mm holes 40 mm either side of the channel's centre
(in through the wall and the lip, under the lip's edge, back out), and two through the roof's holes over the channel's bearing
holes (down one, through the channel, up the next, closed on top; the motor leaves 4 mm under the web there). The ball's push
goes into the poke, down the buttress into the roof and the front wall, onto the lip, in compression; the ties only keep the
L from lifting or sliding.

    python3 cad/modules/flower-stick-proto/poker_u.py                 # stl/poker_u.stl, step/poker_u.step (web top 5.61 up)
    WEB_UP=6.55 NAME=poker_u_hi python3 .../poker_u.py                 # for the channel's top 6.55 in up: the poke hangs lower
    HALF_L=36 NAME=poker_u_72 python3 .../poker_u.py                   # a 72 mm one, if the intake's hard stops on the web close in to 40 mm from centre

Heights follow the channel: WEB_UP is its web top above the tile (intake lowered, 5.61 in assumed) and the poke spans 5.7 to
7.5 in whatever that is, 1.3 in ahead of the lip's face (page/pokez_scan.js: 24/24 at every speed). Millimetres; x ahead of
the front lip's face, y across from the channel's centre, z up from the web top.
"""
import os, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); os.makedirs(os.path.join(HERE, "step"), exist_ok=True); os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
IN = 25.4
CHAN_D, FLANGE_H, CLR = 48.0, 12.0, 0.3                     # the channel: 48 mm deep, the lip 12 mm down from the web top; 0.3 mm clearance
WALL_T = 4.0
HALF_L = float(os.environ.get("HALF_L", "40"))             # half the L's length along the channel: 40 (80 mm), so the intake's round stops under the lip can come inboard to 60 mm from centre, clear of the servo
ROOF_T = 4.0
RELIEF = [(-24.0 + 15.0, y) for y in (32.0, -32.0)] + [(-24.0 - 15.0, y) for y in (32.0, -32.0)]   # 10 mm relief holes over the screws standing out of the web: the pattern's outer rows (14 and 16 mm from the centreline), 32 mm along, both sides
HOLE_Y, HOLE_Z, HOLE_D = 8.0 * int((HALF_L - 8.0) // 8.0), -8.0, 4.3   # the lip's holes: 8 mm pitch from the channel's centre, 8 mm below the web top, 4 mm; the outermost pair that fits
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
for y in (HOLE_Y, -HOLE_Y):   # the lip ties, through the front wall only
    part = part.cut(cq.Workplane("YZ").circle(HOLE_D / 2).extrude(20, both=True).translate((x_in + WALL_T / 2, y, HOLE_Z)))
# three 12 mm holes in the roof over the channel's 14 mm bearing holes (its centreline, 24 mm pitch): to see the channel's holes
# through and centre the L on them, and for the roof ties, down one and up the next
for x, y in RELIEF:
    part = part.cut(cq.Workplane("XY").circle(5.0).extrude(ROOF_T + 2).translate((x, y, roof_z0 - 1)))
# the goBILDA pattern through the roof: 4.5 mm holes at every pattern position the roof covers (the web's rows 8 and 16 mm
# either side of its centreline, 8 mm along), skipping the ones a bearing-hole opening or a relief already takes. So the
# intake's round hard stops, moved inboard, bolt down through the roof with their own M4s and clamp the L to the channel
# wherever they land; the zip ties are then only for a test without the stops.
PATTERN = []
for k in (range(-int((HALF_L - 6) // 8), int((HALF_L - 6) // 8) + 1) if os.environ.get("PATTERN_ROOF") else []):   # PATTERN_ROOF=1 to cut it; off by default, the stops sit beside the L, not on it
    pass
for k in (range(-int((HALF_L - 6) // 8), int((HALF_L - 6) // 8) + 1) if os.environ.get("PATTERN_ROOF") else []):
    y = 8.0 * k
    for dep in (-16.0, -8.0, 8.0, 16.0):
        x = -24.0 + dep
        if any(abs(y - ry) < 1e-6 and abs(dep) < 9 for ry in ROOF_HOLES): continue           # merges with a bearing-hole opening
        if any(abs(x - rx) < 6 and abs(y - ry) < 6 for rx, ry in RELIEF): continue          # a relief hole is already there
        PATTERN.append((x, y))
for x, y in PATTERN:
    part = part.cut(cq.Workplane("XY").circle(2.25).extrude(ROOF_T + 2).translate((x, y, roof_z0 - 1)))
for y in ROOF_HOLES:
    part = part.cut(cq.Workplane("XY").circle(6.0).extrude(ROOF_T + 2).translate((-CHAN_D / 2, y, roof_z0 - 1)))
v = part.val(); assert len(part.solids().vals()) == 1 and v.isValid()
NAME = os.environ.get("NAME", "poker_u")
cq.exporters.export(part, os.path.join(HERE, "stl", NAME + ".stl"), tolerance=0.05, angularTolerance=0.2)
cq.exporters.export(part, os.path.join(HERE, "step", NAME + ".step"))
bb = v.BoundingBox(); print(f"stl/{NAME}.stl: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3")
