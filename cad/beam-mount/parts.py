"""Printable mounts for the lane beam-break (issue #213): one Adafruit 2168 half on any goBILDA channel face or plate.

    pip install trimesh manifold3d
    python3 cad/beam-mount/parts.py         # writes beam_mount_end.stl and beam_mount_face.stl next to this file

The beam looks through one of the face's 14 mm holes. The mount bolts on with two M4 screws through the 4.5 mm
holes 16 mm apart beside it (the ring around the next 14 mm hole: every goBILDA channel and plate has them), screwed
in from the lane side into nuts held in the print, so nothing but two button heads sits in the lane.

Millimetres. Mount frame: origin on the beam axis at the face the print sits on (the wall's outer face), +Z away from
the lane, +X toward the screws' far side. Each part is flat on the bed as it prints: plate down, no supports.

Nobody has measured the sensor yet. Adafruit gives 20 x 10 x 8 mm and the datasheet 20 x 10 x 9, neither says which face
the LED looks out of nor where its ears are. So there are two variants, and the pocket clamps the body (a slit collet
and a zip tie) instead of using the ears. Print both, keep the one the sensor fits, and correct the constants below.
"""
import os

import trimesh
from trimesh.creation import box, cylinder

HERE = os.path.dirname(os.path.abspath(__file__))

# ---- the sensor: UNMEASURED, from Adafruit's page and the QT50CM datasheet. Calipers at the next meeting. ----
BODY_L = 20.0                # long side
BODY_W = 10.0
BODY_H = 9.0                 # Adafruit says 8, the datasheet 9: the pocket takes 9, the zip tie takes up the rest
LED_FROM_END = BODY_L / 2    # face variant only: LED centre from the body's wire end. A guess; measure it
FIT = 0.3                    # pocket clearance per side; print, try the sensor, change to suit your printer
# ---- goBILDA's pattern (measured off the robot CAD's 1107 rails: 14.0 mm holes, 4.5 mm holes at +-8 mm) ----
SCREW_X = -16.0              # the screw pair: the ring of the neighbouring 14 mm hole, 24 mm along
SCREW_Y = 8.0                # ...16 mm apart
M4_CLEAR = 4.4
NUT_AF = 7.0                 # M4 nut across flats
NUT_T = 3.4                  # pocket depth: a plain 3.2 mm M4 nut, flush or just under the top
# ---- the print ----
PLATE_T = 5.0                # with a 2.5 mm goBILDA wall, an M4 x 8 button head reaches through the nut
APERTURE = 6.0               # the beam's window through the plate: a 5 mm LED dome fits, and it shades sunlight
WALL = 2.0                   # pocket walls
SLIT = 1.0                   # the collet's slits, so the zip tie can close the walls on the body
TIE_W, TIE_D = 3.5, 1.0      # zip tie groove round the pocket (a 2.5 mm tie)


def slab(x0, x1, y0, y1, z0, z1):
    b = box(extents=[x1 - x0, y1 - y0, z1 - z0])
    b.apply_translation([(x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2])
    return b


def z_hole(x, y, z0, z1, d, sections=48):
    c = cylinder(radius=d / 2, height=z1 - z0, sections=sections)
    c.apply_translation([x, y, (z0 + z1) / 2])
    return c


def plate(x1, y_half):
    """The base: from past the screws to x1, the screws' nut pockets and the beam's window cut out."""
    body = slab(SCREW_X - NUT_AF / 2 - 3.0, x1, -y_half, y_half, 0, PLATE_T)
    holes = [z_hole(0, 0, -1, PLATE_T + 1, APERTURE)]
    for y in (-SCREW_Y, SCREW_Y):
        holes.append(z_hole(SCREW_X, y, -1, PLATE_T + 1, M4_CLEAR))
        holes.append(z_hole(SCREW_X, y, PLATE_T - NUT_T, PLATE_T + 1, NUT_AF / 0.866, sections=6))   # hex: across corners
    return body, holes


def collet(px, py, h, open_end=None):
    """Walls round a px x py pocket on the plate, h tall, slit at the middle of each long side, with a tie groove.
    open_end '+y' leaves that end open for the wires."""
    ox, oy = px / 2 + WALL, py / 2 + WALL
    walls = slab(-ox, ox, -oy, oy, PLATE_T, PLATE_T + h)
    cuts = [slab(-px / 2, px / 2, -py / 2, py / 2 + (WALL + 1 if open_end == '+y' else 0), PLATE_T, PLATE_T + h + 1)]
    slit_z0 = PLATE_T + 2.0                      # slits stop short of the plate, so the walls stay attached
    long_x = px >= py
    if long_x:
        cuts += [slab(-SLIT / 2, SLIT / 2, -oy - 1, oy + 1, slit_z0, PLATE_T + h + 1)]
    else:
        cuts += [slab(-ox - 1, ox + 1, -SLIT / 2, SLIT / 2, slit_z0, PLATE_T + h + 1)]
    gz = PLATE_T + h - TIE_W - 1.0               # the groove, near the top of the walls
    ring = slab(-ox - 1, ox + 1, -oy - 1, oy + 1, gz, gz + TIE_W).difference(
        slab(-ox + TIE_D, ox - TIE_D, -oy + TIE_D, oy - TIE_D, gz - 1, gz + TIE_W + 1))
    cuts.append(ring)
    return walls, cuts


def beam_mount_end():
    """LED on the end of the body: the body stands on the plate along the beam, wires out the top."""
    px, py = BODY_W + 2 * FIT, BODY_H + 2 * FIT
    h = 12.0                                     # holds 12 of the body's 20 mm
    walls, cuts = collet(px, py, h)
    base, holes = plate(px / 2 + WALL, max(py / 2 + WALL, SCREW_Y + NUT_AF / 2 + 2.0))
    return base.union(walls).difference(trimesh.util.concatenate(holes + cuts))


def beam_mount_face():
    """LED on the body's big face: the body lies flat on the plate across the screws' line, wires out the +y end."""
    px, py = BODY_W + 2 * FIT, BODY_L + 2 * FIT
    h = BODY_H + 1.0
    walls, cuts = collet(px, py, h, open_end='+y')
    shift = BODY_L / 2 - LED_FROM_END            # puts the LED, not the body's middle, on the beam axis
    walls.apply_translation([0, shift, 0])
    for c in cuts:
        c.apply_translation([0, shift, 0])
    base, holes = plate(px / 2 + WALL, max(py / 2 + WALL + abs(shift), SCREW_Y + NUT_AF / 2 + 2.0))
    return base.union(walls).difference(trimesh.util.concatenate(holes + cuts))


def main():
    for name, make in (("beam_mount_end", beam_mount_end), ("beam_mount_face", beam_mount_face)):
        m = make()
        assert m.is_watertight, name
        path = os.path.join(HERE, name + ".stl")
        m.export(path)
        lo, hi = m.bounds
        print(f"{name}.stl  {hi[0] - lo[0]:.1f} x {hi[1] - lo[1]:.1f} x {hi[2] - lo[2]:.1f} mm  {m.volume / 1000:.1f} cm3")


if __name__ == "__main__":
    main()
