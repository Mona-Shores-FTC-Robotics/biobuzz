# Turret encoder kit (add-on for the mentor's robot)

A servo drive for the goBILDA gear-driven turret (3208-0004-0001) plus two absolute encoders, so the turret knows its
angle at power-up with no homing, anywhere in a 1178° window. It bolts to his robot as he drew it on 9 Oct, under his
launcher's front 8-hole channel, and needs nothing else of ours. Checked on 10 Oct against his 9 Oct Robot.step with
`tools/robot-cad/module_check.py turret`: no clashes (the only contact is encoder B's 36T meshing his 64T, as it should).
His CAD has no motor on the turret's drive gear, so nothing of his is removed.

- `turret-kit.step`: the kit in his Robot.step's own frame (mm). Insert it into his Onshape document at the origin and
  it lands in place.
- `stl/`: the four printed parts, in mm, PETG.
- The design and the reasoning: `cad/transfer/` (README, "Turret drive"), `doc/motors-and-servos.md` ("The turret's
  encoders").

## How it works

The kit's 64T drive gear (already on his turret) gets its own 96 mm shaft, in a bearing in the kit's mount and one in a
new plate under the channel. A goBILDA Speed servo in continuous mode, standing in that plate, belts the shaft 1:1, so
the turret turns at about 40 RPM (240°/s). Encoder A (a REV Thru-Bore) rides the drive gear's shaft: 2.75 turns per
turret turn. Encoder B rides a 36T pinion meshing the 64T: 4.89 turns per turret turn. The pair of readings repeats
only every 3.27 turret turns (1178°), so the code decodes the turret's angle from them at power-up and keeps the turret
inside that window. Both encoders plug into an OctoQuad, which goes to the Control Hub's I2C bus 2.

## Parts

| Part | Qty | What for |
|---|---|---|
| goBILDA 2000-0025-0003 Speed servo | 1 | turns the turret (set to continuous mode with the goBILDA servo programmer) |
| goBILDA servo hub, 25T spline to 8mm REX | 1 | carries the servo's pulley (confirm the SKU) |
| goBILDA 3417-4008-0024 24T HTD5 pulley | 2 | servo and drive gear shaft |
| goBILDA 3412-0009-0295 HTD5 belt, 9 mm, 295 mm | 1 | servo to drive gear, fixed centres 87.5 mm |
| goBILDA 2106-4008-0960 8mm REX shaft, 96 mm | 1 | the drive gear's shaft, through encoder A (replaces the kit's motor shaft) |
| goBILDA 2106-4008-0800 8mm REX shaft, 80 mm | 1 | encoder B's shaft |
| goBILDA 2303-4008-0036 36T mod 0.8 steel pinion | 1 | encoder B's gear, meshing the 64T |
| goBILDA 1611-0514-4008 flanged bearing | 4 | two on each shaft |
| goBILDA 8mm REX spacers | a few | the drive gear's shaft, pulley up to the kit's mount |
| REV-11-1271 Thru-Bore Encoder (absolute PWM) | 2 | encoders A and B |
| OctoQuad FTC Edition (Digital Chicken Labs) | 1 | reads both encoders |
| REV 6-pin to 4-pin encoder cables | 2 | the ABS signal moved to pin 5 (issue #170) |
| 3/16 in aluminium plate, cut to `turret_drive_plate` | 1 | under the channel: the shaft's lower bearing, the servo, both encoders (DXF from the STEP) |
| Printed (PETG): `turret_enc_A_cradle`, `turret_enc_A_sleeve`, `turret_enc_B_tower`, `turret_enc_B_sleeve` | 1 each | encoder holders; the sleeves adapt the encoders' 1/2 in hex bore to 8mm REX |
| M4 heat-set inserts | 4 | in the cradle and the tower |
| goBILDA M4 socket head screws and nylon lock nuts | per the STEP | plate to the channel (2), servo tabs (4), cradle (2), tower (2) |

The OctoQuad's place on his robot isn't drawn: anywhere within the encoder cables' reach of the plate, out of the
ball path, with a short cable to the Control Hub.

## Build order

1. Print the four parts; press the heat-set inserts into the cradle and the tower.
2. Encoder B: tower on the plate (two screws from below), encoder in its cradle, sleeve in its bore; the 80 mm shaft
   through the plate's bearing, the encoder and the tower's top bearing; the 36T pinion on top, hub down; e-clips.
3. Servo into the plate's window, tabs on the plate, nuts underneath; hub and 24T pulley on the spline.
4. On the turret: take the kit's motor shaft out of the 64T drive gear; the 96 mm shaft through the kit's 1231 mount
   bearing, the gear, the spacers and the second 24T pulley.
5. Encoder A in its cradle under the plate, sleeve in its bore; cradle up to the plate (two screws from above).
6. Offer the plate up under the 8-hole channel's bottom flange so the 96 mm shaft passes the plate's bearing and
   encoder A, and the 36T meshes the 64T; two screws up through the flange, nuts inside the channel.
7. Belt on, cables to the OctoQuad, OctoQuad to I2C bus 2, servo to a servo port.

## Before trusting it

- Check the 36T to 64T mesh by hand for a little backlash and no binding, all the way round the turret.
- Time a 90° and a 180° turn with the launcher on (the simulator's routes assume at least about 160°/s).
- Read both encoders over a full turn and confirm the decode gives the same angle every time it powers up.
