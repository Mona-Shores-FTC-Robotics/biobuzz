# Beam mount: one beam-break half on a goBILDA face

A printed holder for one half (emitter or receiver) of the Adafruit 2168 IR break beam, for the transfer's
beam-break (issue #213). It bolts to **any goBILDA channel face or plate** and looks through one of its 14 mm holes,
so it fits whatever lane walls the transfer ends up with, or the drive rails. Two prints per beam: one for the emitter
and one for the receiver, facing each other across the lane.

![Both variants, and their footprint on goBILDA's hole pattern](parts.png)

## What's here

| File | What it is | Print |
|---|---|---|
| `beam_mount_end.stl` | For a sensor whose LED looks out of the **end** of its body: the body stands on the plate, wires out the top. 30 x 27 x 17 mm | 2 per beam |
| `beam_mount_face.stl` | For a sensor whose LED looks out of a **big face**: the body lies flat, wires out the open end. 30 x 27 x 15 mm | 2 per beam |
| `parts.py` | Makes the STLs. Every size is a constant at the top | `pip install trimesh manifold3d` |
| `render.py` | Draws the picture above | `+ matplotlib` |

**The sensor hasn't been measured.** Adafruit gives 20 x 10 x 8 mm and the datasheet 20 x 10 x 9. Neither says which
face the LED looks out of or where the ears are. That's why there are two variants, and why the pocket grips the body
instead of using the ears. Print one of each first and keep the one the sensor fits.

## Fitting it

1. Pick the 14 mm hole the beam should pass through. On the transfer it's the one nearest **X 2.85 in, Z 1.96 in**
   (robot frame), just behind the pinch wheels. Do the same on the opposite wall, so the two holes face each other.
2. The screws go through the two 4.5 mm holes **16 mm apart, 16 mm to one side** of that hole. Those holes belong to
   the ring of the next 14 mm hole along, and every goBILDA face has them. The mount turns 180°, so the screws can go
   either side.
3. Put a plain M4 nut in each hex pocket. Then screw an **M4 x 8 button head** in from the lane side. That length
   suits a 2.5 mm goBILDA wall; for a thicker wall use (wall + 5.5 mm), rounded up. Only the two button heads end up
   in the lane.
4. Push the sensor into the pocket with its LED looking through the 6 mm window. Pull a 2.5 mm zip tie tight in the
   groove: the slit walls close on the body. Add a dab of hot glue for competition.
5. The emitter and receiver mounts face each other, one on each wall.

## Why it's shaped like this

- **The goBILDA pattern instead of a drawn wall.** The transfer's walls aren't in the CAD yet. On the robot CAD's 1107
  rails, a 14 mm hole sits right at the beam spot, with 4.5 mm holes at ±8 mm round it. That pattern is the same on
  every goBILDA face, so the mount doesn't need the wall's drawing, only one of its holes.
- **No locating boss.** The 10° beam doesn't need better than the screws' ±0.25 mm. A flat back prints without supports.
- **The 6 mm window** fits a 5 mm LED's dome and shades the receiver a little from sunlight.
- **Nuts in the print, screws from the lane side**: no nut sticks into the lane for a piece to catch on.

## After measuring the sensor

Change `BODY_L/W/H`, and for the face variant `LED_FROM_END` (the LED's centre from the body's wire end). Then
`python3 cad/beam-mount/parts.py` and print again. If the pocket is tight or loose, change `FIT`.
