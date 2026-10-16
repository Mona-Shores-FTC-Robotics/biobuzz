# Print job: FLOWER stick blocks (for a friend with a printer)

**Newest first: `stl/print_plate_divot_blade.stl`** (v2, 16 Oct). One wide blade, 137 x 49 x 19 mm, flat side down: a
wedge nose with a scoop in the middle, a low centre lane and taller shoulders either side. It replaces the two blocks
below, which the team tested on 15 Oct. If the bed is under 140 mm, turn it 45 degrees. Same settings as below; the hex
hole is the same 8.3 mm across the flats. The skin over the hole in the middle lane is only 1.5 mm thick: that's by
design, the shoulders carry the load.

---

**File:** `stl/print_plate_both_blocks.stl`, one plate of 81 × 69 mm, everything already flat side down.
It holds:
- **Block A** (left, 19.8 mm tall): for the robot's current small wheels.
- **Block B** (right, 16.5 mm tall): for 48 mm wheels.

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

**The shaft is 8 mm hex.** The hole is 8.3 mm across the flats, so the shaft should slide in snugly. If it's tight,
file the hole open.

Thank you!
