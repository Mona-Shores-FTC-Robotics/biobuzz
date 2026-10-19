# Print job: FLOWER stick blades (for Dean)

**Newest: `stl/print_job_dean_v6.stl`** (19 Oct), 199 x 157 x 24 mm, three parts, flat side down. Fits a 220 x 220
bed. Millimetres. PETG. About 210 cm3 of plastic.

![the v6 plate from above](print_job_dean_v6.png)

What is on it:
- **v6 with Carter's lane** (left, 137 x 95 x 24 mm): the blade we are going with. A shallower front (the first
  blades took too much push to get under the ball), the lane reaching up to the peak with an oval front, and two
  tall blocks at the ends that carry the shaft.
- **v6 plain** (right, same size): the control, no lane.
- **the hex-fit coupon** again (bottom, 66 x 12 x 18 mm), in case the first one was not printed. Skip it if it was.

**The hex holes** are 8.2 mm across the flats, flats top and bottom, through the two end blocks (13 mm of plastic
over the hole there). In the middle of the blade the shaft's top flat shows through the ramp, flush: that is meant.
If the coupon said a different hole size is snug on your printer, tell us and we resend with that size.

**Settings:**
- **Material:** PETG, or nylon. Not PLA: it cracks when the robot drives the blade into the field element.
- **Layers:** 0.2 mm. **Walls:** 5. **Infill:** 40 %. **Top and bottom layers:** 5.
- **Supports:** none. The hex holes bridge fine at this size. **Brim:** optional, if the bed's adhesion is iffy.
- If the parts come in tiny (or 25 times too big), the slicer read them as inches: scale by 25.4 (or 1/25.4).

If the bed is under 200 mm: `stl/v6_lane.stl`, `stl/v6.stl` (and `stl/hex_fit_coupon.stl`) one at a time.

Thank you!

---

**Earlier: `stl/print_job_dean_v5.stl`** (16 Oct, 169 x 157 mm): v5 with the lane, v5 plain, the coupon. Printed and
tested on 19 Oct: it empties the FLOWER but the push at the nose is too hard. Superseded by v6.

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
