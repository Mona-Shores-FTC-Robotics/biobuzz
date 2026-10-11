# goBILDA hole patterns, read from goBILDA's own STEP files

Measured with the cylinder faces of goBILDA's STEP. Millimetres.

## 1121-0014-0360 low-side U-channel (48 x 12 mm, 360 mm long)

Wall thickness is 2.5 mm. Positions along the channel are measured from one end. The channel is symmetric, so either
end works. `1121-0014-0360_holes.csv` lists every hole.

- **Flanges (both):** one row of 4 mm holes, **8.0 mm from the web's outer face**, every 8 mm along the channel,
  **starting 8 mm from the end**. That's 44 holes per flange, from 8 to 352.
- **Web, at every 8 mm station from 8 to 352:**
  - At stations that are a multiple of 24 (24, 48, ...): 4 mm holes at ±16 mm from the web's centreline, plus a
    14 mm bore on the centreline.
  - At every other station: 4 mm holes at ±8 and ±16 mm from the centreline.
- The other cylinder faces in the STEP are the fillets of the lightening cutouts, not holes.

Other lengths follow the same rule, from 8 mm to (length - 8).

## 3208-0004-0001 gear-driven turret kit: the top of the 176T ring gear (2325-0105-0176)

In goBILDA's STEP the gear sits at z 13 to 20 (7 mm thick) on the bearing's inner race, centred on the turret axis.
Angles are measured from the drive gear's direction (the 64T is at 0°).

- **Twelve 4 mm holes on a 56 mm radius, every 30°.**
  - The six at 0°, 60°, 120°, 180°, 240° and 300° have 7.2 mm counterbores and carry the kit's M4 x 8 screws into
    the inner race.
  - **The six at 30°, 90°, 150°, 210°, 270° and 330° are free.** That's the pattern to screw a hood to (M4).
- Four more 4 mm holes on a 64 mm radius at 0°, 90°, 180° and 270°, among the lightening pockets. Confirm them on the
  part before relying on them.
- Before choosing screw lengths, check on the part whether the free holes continue into the race.
