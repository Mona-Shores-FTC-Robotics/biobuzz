"""Four takes on the poker U (poker_u.py's frame and holes, so any of them ties to the channel the same way):

  classic   the U as drawn, with the poke's edges rounded and the team on the buttress
  lite      the roof opened on the channel's bearing holes, twin rib buttresses, a hollow-backed poke: the same strength
            where the load goes, a third less plastic
  clip      lite, plus a 0.8 mm lip under each wall that snaps under the channel's flange edges, so it clicks on and stays
            put while the ties go in (PETG flexes enough; pull the walls apart to remove)
  adj       two parts: a U with a tall serrated mast on its front wall, and a poke block that bolts to the mast through
            two slots with 2 x M4 x 16 and nuts, so the poke's height sets in 2 mm steps over +-12 mm. One print covers both
            channel heights the CAD gives (5.6 and 6.55 in up), and the test can tune the poke.

    python3 cad/modules/flower-stick-proto/poker_u_variants.py     # stl/poker_u_<name>.stl, poker_u_variants.png

Millimetres; x ahead of the front flange's face, y across from the channel's centre, z up from the web top. WEB_UP as in
poker_u.py (5.61 in by default); adj is built for the middle of the two readings and reaches both.
"""
import os, cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__)); IN = 25.4
CHAN_D, FLANGE_H, CLR = 48.0, 12.0, 0.3
ROOF_T, WALL_T, HALF_L = 4.0, 4.0, 52.0
HOLE_Y, HOLE_Z, HOLE_D = 40.0, -8.0, 4.3
REACH, POKE_W = 1.3 * IN, 19.0
WEB_UP = float(os.environ.get("WEB_UP", "5.61"))
POKE_LO, POKE_HI = (5.7 - WEB_UP) * IN, (7.5 - WEB_UP) * IN
x_in, x_out = CLR, CLR + WALL_T
def box(x0, x1, y0, y1, z0, z1): return cq.Workplane("XY").box(x1 - x0, y1 - y0, z1 - z0, centered=False).translate((x0, y0, z0))
def tie_holes(part):
    for y in (HOLE_Y, -HOLE_Y):
        part = part.cut(cq.Workplane("YZ").circle(HOLE_D / 2).extrude(CHAN_D + 30, both=True).translate((-CHAN_D / 2, y, HOLE_Z)))
    return part
def u_body(roof_holes=False, lips=False):
    roof = box(-CHAN_D - CLR - WALL_T, x_out, -HALF_L, HALF_L, 0, ROOF_T)
    front = box(x_in, x_out, -HALF_L, HALF_L, -FLANGE_H, ROOF_T)
    back = box(-CHAN_D - CLR - WALL_T, -CHAN_D - CLR, -HALF_L, HALF_L, -FLANGE_H, ROOF_T)
    u = roof.union(front).union(back)
    if roof_holes:   # the channel's 14 mm bearing holes, 24 mm pitch, on its centreline: opened in the roof as 12 mm holes
        for y in (-24, 0, 24): u = u.cut(cq.Workplane("XY").circle(6.0).extrude(ROOF_T + 2).translate((-CHAN_D / 2, y, -1)))
        for y in (-48, 48):
            for x in (-CHAN_D / 2 - 12, -CHAN_D / 2 + 12): u = u.cut(cq.Workplane("XY").circle(3.0).extrude(ROOF_T + 2).translate((x, y, -1)))
    if lips:   # snap lips: a 2 mm foot under each wall reaching 0.8 mm in under the flange's edge, with a lead-in below
        for xa, xb in ((x_in - 0.8, x_out), (-CHAN_D - CLR - WALL_T, -CHAN_D - CLR + 0.8)):
            u = u.union(box(xa, xb, -HALF_L + 6, HALF_L - 6, -FLANGE_H - 2.0, -FLANGE_H + 0.01))
            lead = (x_in - 0.8, x_in, 1) if xa > -10 else (-CHAN_D - CLR, -CHAN_D - CLR + 0.8, -1)
            la, lb, sg = lead
            u = u.cut(cq.Workplane("XZ").polyline([(la if sg > 0 else lb, -FLANGE_H - 2.0), (lb if sg > 0 else la, -FLANGE_H - 2.0), (lb if sg > 0 else la, -FLANGE_H - 1.0)] if False else [(la, -FLANGE_H - 2.01), (lb, -FLANGE_H - 2.01), (la if sg > 0 else lb, -FLANGE_H - 1.0)]).close().extrude(HALF_L, both=True))
    return u
def poke_block(x0=x_out, hollow=False):
    p = box(x0, REACH, -POKE_W / 2, POKE_W / 2, POKE_LO, POKE_HI)
    p = p.edges("|Z").edges(">X").fillet(4.0)
    if hollow: p = p.cut(box(x0 - 1, REACH - 6, -POKE_W / 2 + 3.5, POKE_W / 2 - 3.5, POKE_LO + 3.5, POKE_HI - 3.5))
    return p
def buttress_solid(back=-36.0):
    return cq.Workplane("XZ").polyline([(back, ROOF_T - 0.01), (x_out + 0.01, ROOF_T - 0.01), (x_out + 0.01, POKE_HI)]).close().extrude(POKE_W / 2, both=True)
def buttress_ribs(back=-40.0, t=3.0):
    r = None
    for y in (-POKE_W / 2, POKE_W / 2 - t):
        rib = cq.Workplane("XZ").polyline([(back, ROOF_T - 0.01), (x_out + 0.01, ROOF_T - 0.01), (x_out + 0.01, POKE_HI)]).close().extrude(t).translate((0, y + t, 0))
        r = rib if r is None else r.union(rib)
    return r
def finish(part, name):
    v = part.val(); assert v.isValid(), name
    cq.exporters.export(part, os.path.join(HERE, "stl", f"poker_u_{name}.stl"), tolerance=0.05, angularTolerance=0.2)
    cq.exporters.export(part, os.path.join(HERE, "step", f"poker_u_{name}.step"))
    bb = v.BoundingBox(); print(f"poker_u_{name}: {bb.xlen:.0f} x {bb.ylen:.0f} x {bb.zlen:.0f} mm, {v.Volume() / 1000:.0f} cm3, {len(part.solids().vals())} solid(s)")
    return part
# classic
classic = tie_holes(u_body().union(poke_block()).union(buttress_solid()))
try:
    txt = cq.Workplane("XZ").workplane(offset=POKE_W / 2).center(-14, ROOF_T + 7).text("MONA SHORES", 5, 0.8, kind="bold", halign="center", valign="center")
    classic = classic.union(txt)
except Exception as e: print("no text:", e)
finish(classic, "classic")
# lite
lite = tie_holes(u_body(roof_holes=True).union(poke_block(hollow=True)).union(buttress_ribs()))
finish(lite, "lite")
# clip
clip = tie_holes(u_body(roof_holes=True, lips=True).union(poke_block(hollow=True)).union(buttress_ribs()))
finish(clip, "clip")
# adj: the base U with a mast, and the poke block with its flange. Serrations: 0.8 mm teeth, 0.8 deep, 2 mm pitch, on a grid
# fixed to the mast (teeth at even millimetres from the wall's bottom); whatever faces the mast carries teeth on the odd grid,
# so they interleave with 0.2 mm to spare and the block steps 2 mm at a time
MID = (5.7 - (5.61 + 6.55) / 2) * IN                      # the poke's bottom at the middle setting, mm above the roof
MAST_T, MAST_W, MAST_TOP = 6.0, 44.0, MID + 2.0 * IN + 6   # the mast: from the wall's bottom up past the poke's top at the middle setting
BOLT_Y, SLOT_L = 13.5, 25.0                                 # the two M4s: 27 mm apart, in 25 mm slots; their heads clear the ribs at |y| 18..22
def teeth(x_face, depth, odd, z0, z1, width=MAST_W):
    """ridges on a face at x_face, sticking out `depth` (signed), on the even (odd=False) or odd grid, between z0 and z1"""
    tt = None; k0 = int((z0 + FLANGE_H) // 2)
    for k in range(k0 - 1, k0 + int((z1 - z0) / 2) + 2):
        z = -FLANGE_H + 2 * k + (1.0 if odd else 0.0)
        if z + 0.1 < z0 or z + 0.9 > z1: continue
        t = cq.Workplane("YZ").polyline([(-width / 2, z + 0.1), (width / 2, z + 0.1), (width / 2, z + 0.9), (-width / 2, z + 0.9)]).close().extrude(depth).translate((x_face, 0, 0))
        tt = t if tt is None else tt.union(t)
    return tt
mast = box(x_in, x_in + MAST_T, -MAST_W / 2, MAST_W / 2, -FLANGE_H, MAST_TOP)
for y in (-BOLT_Y, BOLT_Y): mast = mast.cut(cq.Workplane("YZ").slot2D(SLOT_L + 4.3, 4.3, 90).extrude(MAST_T + 2, both=True).translate((x_in + MAST_T / 2, y, MID + 20)))
ribs = None
for y0 in (-22.0, 18.0):
    rib = cq.Workplane("XZ").polyline([(-44.0, ROOF_T - 0.01), (x_in + 0.01, ROOF_T - 0.01), (x_in + 0.01, MAST_TOP)]).close().extrude(4.0).translate((0, y0 + 4.0, 0))
    ribs = rib if ribs is None else ribs.union(rib)
base = tie_holes(u_body(roof_holes=True).union(mast).union(teeth(x_in + MAST_T, 0.8, False, -FLANGE_H, MAST_TOP)).union(ribs))
finish(base, "adj")
FL_T = 6.0; x_fl = x_in + MAST_T + 0.8                     # the block's flange rides on the teeth, its own teeth in the mast's gaps
FL_LO, FL_HI = MID - 6, MID + 2.0 * IN + 6
flange = box(x_fl, x_fl + FL_T, -MAST_W / 2, MAST_W / 2, FL_LO, FL_HI)
for y in (-BOLT_Y, BOLT_Y):
    flange = flange.cut(cq.Workplane("YZ").circle(2.2).extrude(FL_T + 2, both=True).translate((x_fl + FL_T / 2, y, MID + 20)))
    flange = flange.cut(cq.Workplane("YZ").polygon(6, 8.3).extrude(3.4).translate((x_fl + FL_T - 3.4, y, MID + 20)))   # a hex pocket for the M4 nut, from the front, 7.2 across flats
BLOCK_REACH = float(os.environ.get("BLOCK_REACH", "1.1")) * IN     # the block's own reach, face of the flange to the poke's front
pokeb = flange.union(teeth(x_fl, -0.8, True, FL_LO, FL_HI)).union(box(x_fl + FL_T - 0.01, BLOCK_REACH, -POKE_W / 2, POKE_W / 2, MID, MID + 1.8 * IN).edges("|Z").edges(">X").fillet(4.0))
finish(pokeb, "adj_poke")
# shims between the mast and the block to push the poke out: a t mm core with teeth on both faces adds t + 0.8 mm of reach;
# the back face's teeth sit in the mast's gaps (odd grid), the front face's on the even grid, where the block's gaps are
for add in (2, 4):
    core = add - 0.8
    sh = box(0, core, -MAST_W / 2, MAST_W / 2, FL_LO, FL_HI)
    for y in (-BOLT_Y, BOLT_Y): sh = sh.cut(cq.Workplane("YZ").circle(2.2).extrude(core + 2, both=True).translate((core / 2, y, MID + 20)))
    finish(sh.union(teeth(0, -0.8, True, FL_LO, FL_HI)).union(teeth(core, 0.8, False, FL_LO, FL_HI)), f"adj_shim{add}")
