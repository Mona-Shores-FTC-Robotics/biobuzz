"""Printable mount for the lane beam-break (issue #213): one Adafruit 2168 half on any goBILDA channel face or plate.

    pip install trimesh manifold3d
    python3 cad/beam-mount/parts.py         # writes beam_mount.stl next to this file

The beam looks through one of the face's 14 mm holes. The mount bolts on with two M4 screws through the 4.5 mm
holes 16 mm apart beside it (the ring around the next 14 mm hole: every goBILDA channel and plate has them), screwed
in from the lane side into nuts held in the print, so nothing but two button heads sits in the lane.

The sensor lies flat on the plate, LED down through the window. Its own screw, through the hole in its body, holds it
down into a nut in the plate's underside. Short walls along its sides square it up; both ends are open, so the wires
leave whichever end they come out of.

Millimetres. Mount frame: origin on the beam axis at the face the print sits on (the wall's outer face), +Z away from
the lane, +Y along the sensor from the LED toward its screw. Flat on the bed as it prints: plate down, no supports.
"""
import os

import trimesh
from trimesh.creation import box, cylinder

HERE = os.path.dirname(os.path.abspath(__file__))

# ---- the sensor. Not yet measured with calipers: from TheTapeWaster's holders (Thingiverse 2772303, CC BY) and their
# photos, which agree: the LED looks out of a big face 5 mm from one end, the body's screw hole is 5 mm from the
# other end on the same centre line. Adafruit gives 20 x 10 x 8, the datasheet 20 x 10 x 9.
BODY_L = 20.0
BODY_W = 10.0
LED_FROM_END = 5.0           # LED centre from the nearer end
LED_TO_SCREW = 10.0          # LED centre to the body's screw hole
FIT = 0.3                    # clearance per side between the side walls and the body
# ---- the sensor's screw: M3 assumed. If the body's hole is smaller, change these to M2.5 (2.9 clear, 5.0 AF nut)
S_CLEAR = 3.4
S_NUT_AF = 5.5
S_NUT_T = 2.6                # pocket depth from the underside: a 2.4 mm M3 nut, trapped against the goBILDA wall
# ---- goBILDA's pattern (measured off the robot CAD's 1107 rails: 14.0 mm holes, 4.5 mm holes at +-8 mm) ----
SCREW_X = -16.0              # the screw pair: the ring of the neighbouring 14 mm hole, 24 mm along
SCREW_Y = 8.0                # ...16 mm apart
M4_CLEAR = 4.4
NUT_AF = 7.0                 # M4 nut across flats
NUT_T = 3.4                  # pocket depth: a plain 3.2 mm M4 nut, flush or just under the top
# ---- the print ----
PLATE_T = 5.0                # with a 2.5 mm goBILDA wall, an M4 x 8 button head reaches through the nut
APERTURE = 6.0               # the beam's window through the plate: a 5 mm LED dome fits, and it shades sunlight
WALL = 2.0                   # side walls
WALL_H = 4.0                 # ...this tall: they only square the body up, the screw holds it
MARGIN = 2.0                 # plate beyond the body's ends and the nut pockets


def slab(x0, x1, y0, y1, z0, z1):
    b = box(extents=[x1 - x0, y1 - y0, z1 - z0])
    b.apply_translation([(x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2])
    return b


def z_hole(x, y, z0, z1, d, sections=48):
    c = cylinder(radius=d / 2, height=z1 - z0, sections=sections)
    c.apply_translation([x, y, (z0 + z1) / 2])
    return c


def hex_pocket(x, y, z0, z1, af):
    return z_hole(x, y, z0, z1, af / 0.866, sections=6)      # across corners


def beam_mount():
    y0 = -LED_FROM_END - FIT                     # the body's ends
    y1 = LED_TO_SCREW + LED_FROM_END + FIT       # (LED and screw are each 5 mm from an end)
    sx = BODY_W / 2 + FIT                        # the side walls' inner faces
    plate = slab(SCREW_X - NUT_AF / 2 - 3.0, sx + WALL,
                 min(y0 - MARGIN, -SCREW_Y - NUT_AF / 2 - MARGIN), max(y1 + MARGIN, SCREW_Y + NUT_AF / 2 + MARGIN),
                 0, PLATE_T)
    walls = [slab(x0, x1, y0, y1, PLATE_T, PLATE_T + WALL_H) for x0, x1 in ((-sx - WALL, -sx), (sx, sx + WALL))]
    holes = [z_hole(0, 0, -1, PLATE_T + 1, APERTURE),
             z_hole(0, LED_TO_SCREW, -1, PLATE_T + 1, S_CLEAR),
             hex_pocket(0, LED_TO_SCREW, -1, S_NUT_T, S_NUT_AF)]
    for y in (-SCREW_Y, SCREW_Y):
        holes.append(z_hole(SCREW_X, y, -1, PLATE_T + 1, M4_CLEAR))
        holes.append(hex_pocket(SCREW_X, y, PLATE_T - NUT_T, PLATE_T + 1, NUT_AF))
    return plate.union(trimesh.util.concatenate(walls)).difference(trimesh.util.concatenate(holes))


def main():
    m = beam_mount()
    assert m.is_watertight
    m.export(os.path.join(HERE, "beam_mount.stl"))
    lo, hi = m.bounds
    print(f"beam_mount.stl  {hi[0] - lo[0]:.1f} x {hi[1] - lo[1]:.1f} x {hi[2] - lo[2]:.1f} mm  {m.volume / 1000:.1f} cm3")


if __name__ == "__main__":
    main()
