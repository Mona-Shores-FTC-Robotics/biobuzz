# Print job: FLOWER stick blocks (for a friend with a printer)

**File:** `stl/print_plate_both_blocks.stl`, one plate of 153 × 69 mm, everything already flat side down.
It holds:
- **Block A** (left, 19.8 mm tall): for the robot's current small wheels.
- **Block B** (middle, 16.5 mm tall): for 48 mm wheels.
- **The hex fit test** (right, four cubes): hex holes 8.1, 8.3, 8.5 and 8.7 mm across the flats. The notches on
  top count them: 1 notch is 8.1, up to 4 notches for 8.7. The blocks' holes are 8.3, the 2-notch cube.

**Units are millimetres.** If the parts come in tiny (or 25 times too big), the slicer read them as inches: scale
by 25.4 (or 1/25.4).

**Settings:**
- **Material:** PETG, or nylon. Not PLA: it cracks when the robot drives the block into the field element.
- **Layers:** 0.2 mm.
- **Walls:** 5. This matters more than infill, because the wall under the shaft hole is only about 2 mm.
- **Infill:** 40 %. **Top and bottom layers:** 5.
- **Supports:** none. Each block has a hex hole straight through, 8.3 mm across the flats, flats top and bottom. It
  bridges fine at this size.
- **Brim:** optional. Add one if your bed's adhesion is iffy.

**The shaft is 8 mm hex.** Try it in each cube. If the 2-notch cube is snug but slides, the blocks will fit. If only
the 3- or 4-notch cube fits, the blocks' holes will be tight: file them open, or ask for blocks with a bigger hole.

Thank you!
