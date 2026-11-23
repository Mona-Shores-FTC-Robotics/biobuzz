"""The lane-floor beam's emitter sled: one Adafruit 2168 half inside the floor channel, under the ball's last spot.

    pip install cadquery
    python3 cad/beam-mount/floor_sled.py     # writes floor_sled.step, .stl and .3mf next to this file

Where it goes (the new transfer, mentor's Onshape export of 23 Nov 2026): the 11-hole goBILDA Mini U-Channel that
is the lane floor under the ball's last spot, behind the gate. That channel lies open side down, so its inside is
a pocket 27.0 mm wide and 9.5 mm tall under the floor the ball rolls on. The sled fills that pocket and holds the
emitter LED up, looking through the channel's 14 mm hole at the ball's spot; the receiver looks down from the top
plate (beam_mount, parts.py). Nothing sticks out below the channel's edges: the sled is 9.3 mm tall, 0.2 mm short
of them.

Two M3 x 8 button heads go down through the floor's 4 mm holes, 9.4 mm either side of the channel's centre line and
9.4 mm past the 14 mm hole toward the wire end, into M3 nuts slid into tunnels in the sled. The screw slots are
+-3 mm long along the channel, so the sled, and the LED with it, slides 3 mm either way before tightening. Those two
holes are the ones clear of the waiting ball: it sits 3.6 mm above their heads; the two on the gate side would touch
it.

Millimetres. Sled frame: origin on the beam axis at the floor's underside (the sled's top face), X across the
channel, Y along it from the LED toward the wire end, Z up (into the lane). It prints upside down, top face on the
bed, no supports (the nut tunnels bridge 5.8 mm). The emitter is clamped between two crush ribs on the pocket's
floor and the floor's underside, so it sits flat and cannot rattle.
"""
import os

import cadquery as cq

HERE = os.path.dirname(os.path.abspath(__file__))

# ---- the sensor (Adafruit 2168; not yet measured with calipers, see README) ----
BODY_L = 20.0
BODY_W = 10.0
BODY_T = 8.0                 # Adafruit says 8, the QT50CM datasheet 9: measure it. The sled's floor is what is left
LED_FROM_END = 5.0           # LED centre from the nearer end; the wires leave the other end
FIT = 0.3                    # clearance per side around the body
# ---- the channel (goBILDA 11-hole Mini U-Channel, measured off the CAD) ----
POCKET_W = 27.0              # inside width between the side walls
POCKET_H = 9.5               # floor underside to the side walls' bottom edges
WEB_T = 2.5                  # the floor's thickness
SCREW_DX = 9.4               # the floor's 4 mm holes used: 9.4 mm either side of the centre line...
SCREW_DY = 9.4               # ...and 9.4 mm from the 14 mm hole's centre toward the wire end
# ---- the print ----
SIDE_FIT = 0.2               # per side between the sled and the channel's walls
DROP = 0.2                   # the sled's bottom this far above the channel's bottom edges
SLIDE = 3.0                  # the screw slots allow this much either way along the channel
M3_CLEAR = 3.4
NUT_AF = 5.5                 # M3 nut across flats
NUT_TUNNEL_W = 5.8           # a nut slides in from the wire end and cannot turn
NUT_TUNNEL_H = 2.7           # a 2.4 mm nut
NUT_TUNNEL_TOP = 3.0         # tunnel roof this far below the top face: an M3 x 8 through the 2.5 mm floor ends 5.5 down
WIRE_W = 6.0                 # wire slot out of the wire end
END_MARGIN = 2.0             # sled beyond the LED end of the body
TAIL = 4.0                   # sled beyond the screw slots at the wire end
RIB_H = 0.3                  # two crush ribs across the pocket's floor: tightening the screws presses the body flat
RIB_W = 1.0                  # against the floor's underside even if it is a little thinner than BODY_T

H = POCKET_H - DROP
W = POCKET_W - 2 * SIDE_FIT


def box(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(x1 - x0, y1 - y0, z1 - z0, centered=False).translate((x0, y0, z0))


def slot(x, y, z0, z1, d, half_len):
    """A vertical slot of width d, +-half_len along Y."""
    return (cq.Workplane("XY").workplane(offset=z0).center(x, y)
            .slot2D(2 * half_len + d, d, angle=90).extrude(z1 - z0))


def floor_sled():
    floor = H - BODY_T
    assert floor >= 0.8, f"the sensor is {BODY_T} mm thick: only {floor:.1f} mm of floor is left under it"
    by0 = -LED_FROM_END - FIT                 # the pocket for the body, LED end
    by1 = BODY_L - LED_FROM_END + FIT         # ...wire end
    y0 = by0 - END_MARGIN
    y1 = max(by1, SCREW_DY + SLIDE + M3_CLEAR / 2) + TAIL
    # Z: 0 is the floor's underside; the sled hangs below it to -H
    part = box(-W / 2, W / 2, y0, y1, -H, 0)
    bx = BODY_W / 2 + FIT
    part = part.cut(box(-bx, bx, by0, by1, -BODY_T, 0.1))                     # the body, LED up against the floor
    part = part.cut(box(-WIRE_W / 2, WIRE_W / 2, by1 - 0.1, y1 + 0.1, -BODY_T, 0.1))   # the wires out the end
    for ry in (by0 + 3.0, by1 - 3.0):
        part = part.union(box(-bx, bx, ry - RIB_W / 2, ry + RIB_W / 2, -BODY_T, -BODY_T + RIB_H))
    for sx in (-SCREW_DX, SCREW_DX):
        part = part.cut(slot(sx, SCREW_DY, -NUT_TUNNEL_TOP - 0.1, 0.1, M3_CLEAR, SLIDE))
        part = part.cut(slot(sx, SCREW_DY, -H - 0.1, -NUT_TUNNEL_TOP - NUT_TUNNEL_H, M3_CLEAR, SLIDE))  # screw tip room
        part = part.cut(box(sx - NUT_TUNNEL_W / 2, sx + NUT_TUNNEL_W / 2, SCREW_DY - SLIDE - NUT_AF,
                            y1 + 0.1, -NUT_TUNNEL_TOP - NUT_TUNNEL_H, -NUT_TUNNEL_TOP))
    assert bx < SCREW_DX - NUT_TUNNEL_W / 2, "the nut tunnels run into the sensor pocket"
    assert SCREW_DX + NUT_TUNNEL_W / 2 < W / 2, "the nut tunnels break out of the sled's sides"
    return part


def export(part, name):
    solid = part.val()
    assert solid.isValid()
    for ext in ("step", "stl", "3mf"):
        kw = {"tolerance": 0.01, "angularTolerance": 0.1} if ext != "step" else {}
        cq.exporters.export(part, os.path.join(HERE, f"{name}.{ext}"), **kw)
    bb = solid.BoundingBox()
    print(f"{name}.step/.stl/.3mf  {bb.xlen:.1f} x {bb.ylen:.1f} x {bb.zlen:.1f} mm  {solid.Volume() / 1000:.1f} cm3")


def main():
    export(floor_sled(), "floor_sled")


if __name__ == "__main__":
    main()
