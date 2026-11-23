"""The lane-floor beam's emitter sled: one Adafruit 2168 half inside the floor channel, under the ball's last spot.

    pip install cadquery
    python3 cad/beam-mount/floor_sled.py     # writes floor_sled.step, .stl and .3mf next to this file

Where it goes (the new transfer, mentor's Onshape export of 23 Nov 2026): the 11-hole goBILDA Mini U-Channel that
is the lane floor under the ball's last spot, behind the gate. That channel lies open side down, so its inside is
a pocket 27.0 mm wide and 9.5 mm tall under the floor the ball rolls on. The sled fills that pocket and holds the
emitter LED up, looking through the channel's 14 mm hole at the ball's spot; the receiver looks down from the top
plate (beam_mount, parts.py). Nothing sticks out below the channel's edges, and **nothing is on the floor the ball
rolls on**: the sled holds by the channel's side walls.

How it holds: the channel's side walls have 4 mm holes every 8 mm along it, 4.0 mm above their bottom edges, one
level with each 14 mm hole. The sled has four pins, two on each side, each on a flexible arm (a strip of the sled
joined only at one end, like a diving board): one pair level with the beam's 14 mm hole, the other 32 mm along the
channel. Push the sled straight up into the channel: the pins' sloped tops let the channel's edges bend the arms in,
and at the holes they spring back out and click in. Two pins in each side wall, 32 mm apart, so it cannot rock or
slide; the walls hold it sideways and the floor above. The holes place the LED on the 14 mm hole's centre: no
adjusting. The open side's pins (the 8 mm gap to the next channel) have square undersides, so a pull does not free
them; the other side's are ramped both ways. To take it out: push the open side's two pins in through their holes
(two small screwdrivers) and pull the sled down.

Backup, if a printed sled ever comes loose: an M3 x 10 button head through the open side's wall hole halfway between
the two pairs of pins (16 mm along), into an M3 nut dropped into the slot in the sled's top before it goes in. Its
head is outside the channel's wall, nowhere near the ball. It needs a short hex key in the 8 mm gap beside the
channel; unused, the slot is just an empty pocket.

The sled reaches from the beam's 14 mm hole toward the gate, and the wires leave at that end (mentor, 23 Nov 2026:
in the transfer CAD the wires go toward the gate, the orange part). WIRES_TOWARD_GATE False turns it the other way.

Millimetres. Sled frame: origin on the beam axis at the floor's underside (the sled's top face), X across the
channel (+X the side pressed against the next channel, -X the open side), Y along it (-Y toward the gate, where
the wires leave), Z up (into the lane). It is drawn wires toward +Y and mirrored at the end. It prints upside down, top face on the bed, no supports.
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
WALL_T = 2.5                 # the side walls' thickness
SIDE_HOLE_D = 4.0            # the side walls' holes: 4 mm, every 8 mm, one level with each 14 mm hole...
SIDE_HOLE_Z = -5.48          # ...their centres 5.48 mm below the floor's underside (4.02 above the walls' edges)
SIDE_HOLE_PITCH = 8.0
# ---- the print ----
SIDE_FIT = 0.2               # per side between the sled and the channel's walls
DROP = 0.2                   # the sled's bottom this far above the channel's bottom edges
PIN_D = 3.6                  # the pins, in the 4 mm holes
PIN_OUT = 1.0                # beyond the sled's side: 0.2 to the wall, 0.8 into its hole
PIN_RAMP = 0.6               # the domed pin's outer face: half this height above and below its centre
ARM_T = 1.4                  # the flexible arms carrying the pins
ARM_GAP = 1.4                # behind each arm: room for it to bend in past the pin's 0.8 mm and some
PIN_YS = (0.0, 32.0)         # the two pairs of pins: level with the beam's 14 mm hole, and 32 mm (4 holes) along
ARM_LEN = 12.0               # each pin sits this far from its arm's root (1.5 % strain in PETG as it bends 1 mm)
ARM_TIP = 4.0                # and its arm runs this far past the pin to its free end
BACKUP_Y = 16.0              # the backup screw: the open side's wall hole halfway between the pairs of pins
M3_CLEAR = 3.4
NUT_AF = 5.5
NUT_T = 2.6
NUT_X = 6.9                  # the nut slot's inner face from the beam axis: an M3 x 10 from outside the wall reaches
WIRES_TOWARD_GATE = True     # the sled and its wires run from the beam's hole toward the gate (-Y); False: away
RIB_H = 0.2                  # two crush ribs across the pocket's floor press the body up against the floor
RIB_W = 1.0
WIRE_W = 6.0                 # wire slot out of the wire end
END_MARGIN = 2.0             # sled beyond the LED end of the body

H = POCKET_H - DROP
W = POCKET_W - 2 * SIDE_FIT


def box(x0, x1, y0, y1, z0, z1):
    return cq.Workplane("XY").box(x1 - x0, y1 - y0, z1 - z0, centered=False).translate((x0, y0, z0))


def pin(side, square_under):
    """The pin on side +1 or -1: a round pin out of the arm's face, its top ramped so pushing the sled up cams it in;
    its underside square (holds) or ramped too (comes out with a pull)."""
    face = side * W / 2
    tip = face + side * PIN_OUT
    r = PIN_D / 2
    rod = (cq.Workplane("YZ").workplane(offset=face if side > 0 else tip).center(0, SIDE_HOLE_Z)
           .circle(r).extrude(PIN_OUT + 0.05))
    lo_tip = -r if square_under else -PIN_RAMP / 2
    profile = [(face, SIDE_HOLE_Z - r), (tip, SIDE_HOLE_Z + lo_tip), (tip, SIDE_HOLE_Z + PIN_RAMP / 2),
               (face, SIDE_HOLE_Z + r)]
    wedge = (cq.Workplane("XZ").polyline(profile).close().extrude(r + 0.1, both=True))
    return rod.intersect(wedge)


def floor_sled():
    floor = H - BODY_T
    assert floor >= 0.8, f"the sensor is {BODY_T} mm thick: only {floor:.1f} mm of floor is left under it"
    by0 = -LED_FROM_END - FIT                 # the pocket for the body, LED end
    by1 = BODY_L - LED_FROM_END + FIT         # ...wire end
    y0 = by0 - END_MARGIN
    y1 = PIN_YS[1] + ARM_TIP
    part = box(-W / 2, W / 2, y0, y1, -H, 0)
    bx = BODY_W / 2 + FIT
    part = part.cut(box(-bx, bx, by0, by1, -BODY_T, 0.1))                     # the body, LED up against the floor
    part = part.cut(box(-WIRE_W / 2, WIRE_W / 2, by1 - 0.1, y1 + 0.1, -BODY_T, 0.1))   # the wires out the end
    for ry in (by0 + 3.0, by1 - 3.0):
        part = part.union(box(-bx, bx, ry - RIB_W / 2, ry + RIB_W / 2, -BODY_T, -BODY_T + RIB_H))
    # the arms: a slot behind each, open at the sled's end; the front pair free at the LED end, the back pair at the
    # far end, each growing from the sled ARM_LEN from its pin
    arms = ((PIN_YS[0], y0 - 0.1, PIN_YS[0] + ARM_LEN), (PIN_YS[1], PIN_YS[1] - ARM_LEN, y1 + 0.1))
    for pin_y, a0, a1 in arms:
        for side in (1, -1):
            inner = side * (W / 2 - ARM_T)
            back = inner - side * ARM_GAP
            part = part.cut(box(min(inner, back), max(inner, back), a0, a1, -H - 0.1, 0.1))
            p = pin(-1, square_under=side < 0)
            if side > 0:
                p = p.mirror("YZ")
            part = part.union(p.translate((0, pin_y, 0)))
    assert arms[0][2] < arms[1][1], "the two pairs of arms overlap"
    # the backup screw on the open side (-X), between the arms' roots: a hole along X and a nut slot open at the top
    part = part.cut(cq.Workplane("YZ").workplane(offset=-W / 2 - 0.1).center(BACKUP_Y, SIDE_HOLE_Z)
                    .circle(M3_CLEAR / 2).extrude(W / 2 - NUT_X + 1.0))
    part = part.cut(box(-NUT_X - NUT_T, -NUT_X, BACKUP_Y - NUT_AF / 2 - 0.15, BACKUP_Y + NUT_AF / 2 + 0.15,
                        SIDE_HOLE_Z - NUT_AF / 0.866 / 2 - 0.2, 0.1))
    assert arms[0][2] < BACKUP_Y - NUT_AF / 2 and BACKUP_Y + NUT_AF / 2 < arms[1][1], "the backup nut slot hits an arm"
    assert NUT_X - 1.0 > WIRE_W / 2, "the backup screw runs into the wire slot"
    if WIRES_TOWARD_GATE:
        part = part.mirror("XZ")                  # Y to -Y: the X sides, and so the open side's pins, stay put
    assert bx < W / 2 - ARM_T - ARM_GAP, "the arm slots run into the sensor pocket"
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
