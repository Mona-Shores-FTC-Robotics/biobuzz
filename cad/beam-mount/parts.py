"""Printable mount for one Adafruit 2168 beam-break half on any goBILDA channel face or plate (issue #213).
On the new transfer it holds the receiver on the top plate, looking down at the emitter in the floor (floor_sled.py).

    pip install cadquery
    python3 cad/beam-mount/parts.py         # writes beam_mount.step, .stl and .3mf next to this file

The beam looks through one of the face's 14 mm holes. The mount bolts on with two M4 screws through the 4.5 mm
holes 16 mm apart beside it (the ring around the next 14 mm hole: every goBILDA channel and plate has them), screwed
in from the lane side into nuts held in the print, so nothing but two button heads sits in the lane. The screw holes
are slots, +-3 mm toward and away from the window, with the nuts in matching tracks: line the beam up, then tighten.

The sensor lies flat on the plate, LED down through the window. Its LED end slides under a lip on the end wall and its
own screw, through the hole in its body, pulls the other end down into a nut in the plate's underside: held at both
ends, it sits flat on the plate (the first version held only the screw end, and on the robot its LED end stood
proud). Walls nearly its height, a close fit, cradle it on three sides: pushed against the end wall, its LED sits over
the window. The wire end is open.

Millimetres. Mount frame: origin on the beam axis at the face the print sits on (the wall's outer face), +Z away from
the lane, +Y along the sensor from the LED toward its screw. Flat on the bed as it prints: plate down, no supports.
"""
import os

import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))

# ---- the sensor: Adafruit's dimensioned drawing (cdn-shop.adafruit.com/product-files/2167/2167diagram.jpg, the same
# housing as the 2168 the team bought): 20 x 10, the LED and a 2.4 mm mounting hole on the same big face, 10 mm
# apart, each 5 mm from its end. Adafruit's page gives 20 x 10 x 8; the generic QT50CM datasheet says 9.
BODY_L = 20.0
BODY_W = 10.0
BODY_T = 8.0                 # Adafruit says 8, the datasheet 9: measure it. The lip at the LED end sits this high
LED_FROM_END = 5.0           # LED centre from the nearer end
LED_TO_SCREW = 10.0          # LED centre to the body's screw hole
FIT = 0.15                   # clearance per side between the side walls and the body: close, so it cannot rock
# ---- the sensor's screw: M2 (the body's hole is 2.4 mm, Adafruit's drawing). An M2 x 12 through the body (8) and the
# plate (5) ends 1 mm short of the underside, 1.2 mm into the nut
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
WALL = 2.0                   # side walls
WALL_H = 7.0                 # ...this tall: most of the body (8-9 mm), so a knock lands on plastic, not the sensor
END_STOP = True              # a wall across the LED end: push the body against it and the LED is over the window
LIP = 1.5                    # ...with a lip this far over the body's top, so the LED end is held down too
LIP_T = 1.2
LIP_GAP = 0.1                # the lip's underside this far above the body's top
MARGIN = 2.0                 # plate beyond the body's ends and the nut pockets


def box(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(x1 - x0, y1 - y0, z1 - z0, centered=False).translate((x0, y0, z0))


def z_hole(x, y, z0, z1, d):
    return cq.Workplane("XY").workplane(offset=z0).center(x, y).circle(d / 2).extrude(z1 - z0)


def hex_pocket(x, y, z0, z1, af):
    return cq.Workplane("XY").workplane(offset=z0).center(x, y).polygon(6, af / 0.866).extrude(z1 - z0)   # across corners


def x_slot(x, y, z0, z1, d, half_len):
    """A vertical slot of width d, +-half_len along X."""
    return cq.Workplane("XY").workplane(offset=z0).center(x, y).slot2D(2 * half_len + d, d).extrude(z1 - z0)


def beam_mount():
    y0 = -LED_FROM_END - FIT                     # the body's ends
    y1 = LED_TO_SCREW + LED_FROM_END + FIT       # (LED and screw are each 5 mm from an end)
    sx = BODY_W / 2 + FIT                        # the side walls' inner faces
    part = box(SCREW_X - SLIDE - NUT_AF / 2 - 3.0, sx + WALL,
               min(y0 - MARGIN, -SCREW_Y - NUT_AF / 2 - MARGIN), max(y1 + MARGIN, SCREW_Y + NUT_AF / 2 + MARGIN),
               0, PLATE_T)
    for x0, x1 in ((-sx - WALL, -sx), (sx, sx + WALL)):
        part = part.union(box(x0, x1, y0 - (WALL if END_STOP else 0), y1, PLATE_T, PLATE_T + WALL_H))
    if END_STOP:
        lip_z = PLATE_T + BODY_T + LIP_GAP
        part = part.union(box(-sx - WALL, sx + WALL, y0 - WALL, y0, PLATE_T, lip_z + LIP_T))
        part = part.union(box(-sx - WALL, sx + WALL, y0 - WALL, y0 + LIP, lip_z, lip_z + LIP_T))   # 1.5 mm overhang
    cuts = [z_hole(0, 0, -1, PLATE_T + 1, APERTURE),
            z_hole(0, LED_TO_SCREW, -1, PLATE_T + 1, S_CLEAR),
            hex_pocket(0, LED_TO_SCREW, -1, S_NUT_T, S_NUT_AF)]
    for y in (-SCREW_Y, SCREW_Y):
        cuts.append(x_slot(SCREW_X, y, -1, PLATE_T + 1, M4_CLEAR, SLIDE))
        cuts.append(box(SCREW_X - SLIDE - NUT_AF / 0.866 / 2, SCREW_X + SLIDE + NUT_AF / 0.866 / 2,     # the nut's track:
                        y - NUT_AF / 2 - 0.15, y + NUT_AF / 2 + 0.15, PLATE_T - NUT_T, PLATE_T + 1))  # flats on its sides
    for c in cuts:
        part = part.cut(c)
    return part


def main():
    part = beam_mount()
    solid = part.val()
    assert solid.isValid()
    cq.exporters.export(part, os.path.join(HERE, "beam_mount.step"))
    for ext in ("stl", "3mf"):
        cq.exporters.export(part, os.path.join(HERE, f"beam_mount.{ext}"), tolerance=0.01, angularTolerance=0.1)
    bb = solid.BoundingBox()
    print(f"beam_mount.step/.stl/.3mf  {bb.xlen:.1f} x {bb.ylen:.1f} x {bb.zlen:.1f} mm  {solid.Volume() / 1000:.1f} cm3")


if __name__ == "__main__":
    main()
