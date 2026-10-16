# Print job: FLOWER stick blades (for a friend with a printer)

**Newest first: `stl/print_plate_v5.stl`** (v5, 16 Oct): **two blades on one plate, 191 x 137 mm**, flat side down.
They are the same blade twice: one with Carter's lane (the ramp curves gently down toward its middle, 0.3 in deep at
the edges, no flats), which is the one the team is going with, and one plain as the control. If the bed is under 200 mm, print `stl/v5.stl` and `stl/v5_lane.stl` one at a
time (137 x 90 x 15 mm each). Same settings as below; the hex hole is 8.3 mm across the flats, flats top and bottom,
with about 4 mm of plastic over it: no supports. (`stl/v5_scoop.stl`, an earlier reading of the sketch with a scoop in
the front edge, is superseded; skip it.)

**First, a five-minute print: `stl/hex_fit_coupon.stl`** (66 x 12 x 18 mm, flat side down). It has three 8 mm hex
holes, 8.1, 8.2 and 8.3 mm across the flats, marked with one, two and three notches. Push the 8 mm hex shaft into each
and tell us which one is snug with no rock. The two small blocks from the first print turned on the shaft, and we
think the 8.3 mm hole came out a little loose on your printer. The blade files above have 8.3 mm holes; once we know
which coupon hole fits, we will send the blades with that size (it is one number in the script), or you can print
them as they are and we will shim.

The older plates below are superseded; keep them only if already printed.

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
