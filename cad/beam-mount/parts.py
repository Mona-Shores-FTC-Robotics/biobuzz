"""Printable mount for one Adafruit 2168 beam-break half on any goBILDA channel face or plate (issue #213).
On the new transfer it holds the receiver on the top plate, looking down at the emitter in the floor (floor_sled.py).

    pip install cadquery
    python3 cad/beam-mount/parts.py         # writes beam_mount and beam_fit_test .step, .stl and .3mf next to this file

The beam looks through one of the face's 14 mm holes. The mount bolts on with two M4 screws through the 4.5 mm
holes 16 mm apart beside it (the ring around the next 14 mm hole: every goBILDA channel and plate has them), screwed
in from the lane side into nuts held in the print, so nothing but two button heads sits in the lane. The screw holes
are slots, +-3 mm toward and away from the window, with the nuts in matching tracks: line the beam up, then tighten.

The sensor lies flat on the plate, LED down through the window, and is captured without a screw (the robot showed a
1.5 mm lip and the body's M2 screw did not hold it well, 27 Nov 2026):
- **the LED end** slides under a 3 mm lip on the end wall. The side walls rise to the lip, so it prints as a bridge
  between them, no supports. Pushed against the end wall, the LED sits over the window.
- **the side walls** are the body's full height, with vertical crush ribs that grip its sides.
- **the wire end** snaps down past two springy arms, one in each side wall, each with a hook over the body's top
  edge. Each arm is a strip of wall joined at one end only and free under it (a slot through the plate), like the
  floor sled's pins: it bends in the plane of the layers, never across them. To take the sensor out, spread the two
  arms and lift the wire end.
- **an end wall** at the wire end stops it sliding out from under the lip, with a slot the full depth for the wires.
The body's M2 hole and the nut pocket under it stay, as an optional backup only.

beam_fit_test is a small print to try first: three short pockets, the same walls and ribs, the ribs gripping 0.15,
0.25 and 0.35 mm a side (one, two and three notches on the end). Keep the one the body presses into by hand and
cannot shake out of, and set RIB_CRUSH to it.

Millimetres. Mount frame: origin on the beam axis at the face the print sits on (the wall's outer face), +Z away from
the lane, +Y along the sensor from the LED toward its wires. Flat on the bed as it prints: plate down, no supports.
"""
import os

import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))

# ---- the sensor: Adafruit's dimensioned drawing (cdn-shop.adafruit.com/product-files/2167/2167diagram.jpg, the same
# housing as the 2168 the team bought): 20 x 10, the LED and a 2.4 mm mounting hole on the same big face, 10 mm
# apart, each 5 mm from its end. Adafruit's page gives 20 x 10 x 8; the generic QT50CM datasheet says 9.
BODY_L = 20.0
BODY_W = 10.0
BODY_T = 8.0                 # Adafruit says 8, the datasheet 9: measure it. The walls, lip and hooks follow it
LED_FROM_END = 5.0           # LED centre from the nearer end
LED_TO_SCREW = 10.0          # LED centre to the body's screw hole
FIT = 0.15                   # clearance per side between the walls and the body; the crush ribs take it up
# ---- the sensor's screw, an optional backup only: M2 (the body's hole is 2.4 mm, Adafruit's drawing). An M2 x 12
# through the body (8) and the plate (5) ends 1 mm short of the underside, 1.2 mm into the nut
S_CLEAR = 2.4
S_NUT_AF = 4.0
S_NUT_T = 2.2                # pocket depth from the underside: a 1.6 mm M2 nut, trapped against the goBILDA plate
# ---- goBILDA's pattern (measured off the robot CAD's 1107 rails: 14.0 mm holes, 4.5 mm holes at +-8 mm) ----
SCREW_X = -16.0              # the screw pair: the ring of the neighbouring 14 mm hole, 24 mm along
SCREW_Y = 8.0                # ...16 mm apart
M4_CLEAR = 4.4
NUT_AF = 7.0                 # M4 nut across flats
NUT_T = 3.4                  # pocket depth: a plain 3.2 mm M4 nut, flush or just under the top
SLIDE = 3.0                  # the screw slots allow this much either way along X
# ---- the print ----
PLATE_T = 5.0                # with a 2.5 mm goBILDA wall, an M4 x 8 button head reaches through the nut
APERTURE = 6.0               # the beam's window through the plate: a 5 mm LED dome fits, and it shades sunlight
WALL = 2.0                   # side walls and end walls
# ---- holding the sensor (no screw needed) ----
LIP = 3.0                    # the LED end's lip, this far over the body's top: a bridge between the side walls
LIP_T = 1.2
LIP_GAP = 0.1                # the lip's and the hooks' undersides this far above the body's top
RIB_CRUSH = 0.25             # each crush rib reaches this far into the body's side (beam_fit_test tries 0.15/0.25/0.35)
RIB_BASE = 1.2               # a rib's width where it meets the wall (a triangle in plan, full wall height)
RIB_YS = (-3.0, 1.5)         # the ribs along each fixed side wall, from the LED (LED end of the body at -5.15)
ARM_T = 1.4                  # the springy arms: wall strips this thick (X)...
ARM_LEN = 12.0               # ...this long (Y), joined at their LED end only: bending the hook 1.0 mm out is 1.5 % strain
ARM_GAP = 1.6                # room behind each arm to bend out past its hook
SLIT = 0.5                   # the slot through the plate that frees each arm from the plate and from its neighbours
HOOK = 1.0                   # each hook reaches this far over the body's top edge (past the wall's inner face)
HOOK_H = 2.0                 # the hook's height above its catch: a sloped top, so the body pushes the arms apart
HOOK_LEN = 5.0               # the hook's length along the arm, ending at the body's wire end
END_H = 4.0                  # the wire-end wall's height above the plate: stops the body sliding out from the lip
WIRE_SLOT = 6.0              # ...with a slot this wide, down to the plate, for the wires
MARGIN = 2.0                 # plate beyond the walls and the nut pockets
# ---- the fit test ----
FIT_TEST_CRUSH = (0.15, 0.25, 0.35)
FIT_TEST_LEN = 8.0           # each test pocket is this long, open at both ends
FIT_TEST_PLATE = 2.0


def box(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(x1 - x0, y1 - y0, z1 - z0, centered=False).translate((x0, y0, z0))


def z_hole(x, y, z0, z1, d):
    return cq.Workplane("XY").workplane(offset=z0).center(x, y).circle(d / 2).extrude(z1 - z0)


def hex_pocket(x, y, z0, z1, af):
    return cq.Workplane("XY").workplane(offset=z0).center(x, y).polygon(6, af / 0.866).extrude(z1 - z0)   # across corners


def x_slot(x, y, z0, z1, d, half_len):
    """A vertical slot of width d, +-half_len along X."""
    return cq.Workplane("XY").workplane(offset=z0).center(x, y).slot2D(2 * half_len + d, d).extrude(z1 - z0)


def rib(x_face, side, y, z0, z1, crush):
    """A vertical crush rib on the wall face at x_face (side +1: the wall is at +X), its tip crush into the body."""
    tip = x_face - side * (FIT + crush)
    pts = [(x_face + side * 0.1, y - RIB_BASE / 2), (tip, y), (x_face + side * 0.1, y + RIB_BASE / 2)]
    return cq.Workplane("XY").workplane(offset=z0).polyline(pts).close().extrude(z1 - z0)


def hook(side, sx, y0, y1, z_catch):
    """The hook on the arm at side +1/-1: a flat catch HOOK over the body at z_catch, its top sloped back to the wall."""
    tip = side * (sx - HOOK)
    pts = [(side * (sx + ARM_T), z_catch), (tip, z_catch), (tip, z_catch + 0.4), (side * sx, z_catch + HOOK_H),
           (side * (sx + ARM_T), z_catch + HOOK_H)]
    return cq.Workplane("XZ").workplane(offset=-y1).polyline(pts).close().extrude(y1 - y0)


def beam_mount():
    y0 = -LED_FROM_END - FIT                     # the body's ends
    y1 = LED_TO_SCREW + LED_FROM_END + FIT       # (LED and screw are each 5 mm from an end)
    sx = BODY_W / 2 + FIT                        # the side walls' inner faces
    top = PLATE_T + BODY_T                       # the walls' tops: the body's top
    lip_z = top + LIP_GAP                        # the lip's and the hooks' undersides
    ye = y1 + WALL                               # the wire-end wall's outer face
    arm0 = ye - ARM_LEN                          # each arm's root
    xo = sx + ARM_T + ARM_GAP                    # the far side of the gap behind each arm (open outward)
    part = box(SCREW_X - SLIDE - NUT_AF / 2 - 3.0, xo + MARGIN,
               min(y0 - WALL - MARGIN, -SCREW_Y - NUT_AF / 2 - MARGIN), ye + SLIT + MARGIN, 0, PLATE_T)
    for side in (1, -1):
        def sb(xa, xb, ya, yb, za, zb):
            return box(min(side * xa, side * xb), max(side * xa, side * xb), ya, yb, za, zb)
        part = part.union(sb(sx, xo, y0 - WALL, arm0, PLATE_T, top))                 # the fixed wall, LED end
        part = part.union(sb(sx, xo, y0 - WALL, y0 + LIP, PLATE_T, lip_z + LIP_T))   # ...up to the lip
        for ry in RIB_YS:
            part = part.union(rib(side * sx, side, ry, PLATE_T, top, RIB_CRUSH))
        part = part.union(sb(WIRE_SLOT / 2, sx - SLIT, y1, ye, PLATE_T, PLATE_T + END_H))   # the wire-end wall
    part = part.union(box(-xo, xo, y0 - WALL, y0, PLATE_T, lip_z + LIP_T))                # LED end wall
    part = part.union(box(-sx, sx, y0, y0 + LIP, lip_z, lip_z + LIP_T))                   # the lip, bridged
    cuts = [z_hole(0, 0, -1, PLATE_T + 1, APERTURE),
            z_hole(0, LED_TO_SCREW, -1, PLATE_T + 1, S_CLEAR),
            hex_pocket(0, LED_TO_SCREW, -1, S_NUT_T, S_NUT_AF)]
    for side in (1, -1):
        def sb(xa, xb, ya, yb, za, zb):
            return box(min(side * xa, side * xb), max(side * xa, side * xb), ya, yb, za, zb)
        cuts.append(sb(sx - SLIT, sx, arm0, ye + SLIT, -1, top + 1))           # free the arm: inside it,
        cuts.append(sb(sx + ARM_T, xo, arm0, ye + SLIT, -1, top + 1))          # behind it,
        cuts.append(sb(sx - SLIT, xo, ye, ye + SLIT, -1, top + 1))             # and past its free end
    for y in (-SCREW_Y, SCREW_Y):
        cuts.append(x_slot(SCREW_X, y, -1, PLATE_T + 1, M4_CLEAR, SLIDE))
        cuts.append(box(SCREW_X - SLIDE - NUT_AF / 0.866 / 2, SCREW_X + SLIDE + NUT_AF / 0.866 / 2,     # the nut's track:
                        y - NUT_AF / 2 - 0.15, y + NUT_AF / 2 + 0.15, PLATE_T - NUT_T, PLATE_T + 1))  # flats on its sides
    for c in cuts:
        part = part.cut(c)
    for side in (1, -1):                                                      # the arms, standing on the bed
        arm = box(min(side * sx, side * (sx + ARM_T)), max(side * sx, side * (sx + ARM_T)), arm0, ye, 0, lip_z)
        part = part.union(arm.union(hook(side, sx, y1 - HOOK_LEN, y1, lip_z)))
    assert arm0 > max(RIB_YS) + RIB_BASE, "a crush rib sits on an arm"
    assert arm0 > y0 + LIP + 1.0, "the arms reach the lip"
    assert ARM_GAP > HOOK + 0.3, "the arms cannot bend out past their hooks"
    assert SCREW_X + SLIDE + NUT_AF / 0.866 / 2 < -xo - 0.5, "a nut track runs into the gap behind an arm"
    assert LED_TO_SCREW + S_NUT_AF / 2 < y1 and WIRE_SLOT / 2 < sx - SLIT, "the wire-end wall"
    return part


def beam_fit_test():
    """Three short pockets side by side, the receiver mount's side walls and ribs, one rib grip each."""
    sx = BODY_W / 2 + FIT
    pitch = 2 * (sx + WALL)
    part = None
    for i, crush in enumerate(FIT_TEST_CRUSH):
        cx = (i - 1) * pitch
        cell = box(cx - sx - WALL, cx + sx + WALL, 0, FIT_TEST_LEN, 0, FIT_TEST_PLATE)
        for side in (1, -1):
            xa, xb = cx + side * sx, cx + side * (sx + WALL)
            cell = cell.union(box(min(xa, xb), max(xa, xb), 0, FIT_TEST_LEN, FIT_TEST_PLATE, FIT_TEST_PLATE + BODY_T))
            cell = cell.union(rib(cx + side * sx, side, FIT_TEST_LEN / 2, FIT_TEST_PLATE, FIT_TEST_PLATE + BODY_T, crush))
        for n in range(i + 1):                                     # the notches: 1, 2 or 3, on the wall's top
            nx = cx + sx + WALL / 2
            cell = cell.cut(box(nx - 0.5, nx + 0.5, 1.0 + 1.6 * n, 1.8 + 1.6 * n, FIT_TEST_PLATE + BODY_T - 0.6, 99))
        part = cell if part is None else part.union(cell)
    return part


def export(part, name):
    solid = part.val()
    assert solid.isValid()
    assert len(part.solids().vals()) == 1, f"{name} is not one piece"
    cq.exporters.export(part, os.path.join(HERE, f"{name}.step"))
    for ext in ("stl", "3mf"):
        cq.exporters.export(part, os.path.join(HERE, f"{name}.{ext}"), tolerance=0.01, angularTolerance=0.1)
    bb = solid.BoundingBox()
    print(f"{name}.step/.stl/.3mf  {bb.xlen:.1f} x {bb.ylen:.1f} x {bb.zlen:.1f} mm  {solid.Volume() / 1000:.1f} cm3")


def main():
    export(beam_mount(), "beam_mount")
    export(beam_fit_test(), "beam_fit_test")


if __name__ == "__main__":
    main()
