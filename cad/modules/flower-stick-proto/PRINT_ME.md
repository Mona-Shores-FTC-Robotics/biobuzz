# Print job: FLOWER stick blocks (for a friend with a printer)

**File:** `stl/print_plate_both_blocks.stl`, one plate of 153 × 69 mm, everything already flat side down.
It holds:
- **Block A** (left, 19.8 mm tall): for the robot's current small wheels.
- **Block B** (middle, 16.5 mm tall): for 48 mm wheels.
- **The fit coupon** (right, the small bar): four test holes for checking the shaft fit.

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

**The shaft is 8 mm hex.** The hole should slide on snugly. The coupon's holes are round (they're for a different
shaft), so they're only a rough guide. If the hex hole comes out tight, open it with a file, or scale the hole and
re-print.

Thank you!
