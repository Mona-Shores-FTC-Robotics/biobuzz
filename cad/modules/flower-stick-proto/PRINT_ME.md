# Print now: the poker L, final (for whoever prints)

**Fitted on 20245, 28 Oct 2026: it seated flat on its pegs and came out perfectly centred (Dustin).** The tie went in through the wall at the top and out the roof hole, as drawn.

**LOCKED 28 Oct 2026: this is the part on the robot. Backups come from the same file, unchanged.** Print `stl/print_job_u_only.stl`
(`poker_u_hi.stl`, the zip-tie version). Do not print `poker_u_hi_bolt.stl` or any earlier plate for backups.

**`stl/print_job_u_only.stl`**: one part, `stl/poker_u_hi.stl` (71 x 80 x 46 mm, 48 cm3), standing on its front wall's back.
The final poker after the first night on 20245, with the lower nose, the one that worked, plus what that night taught:

- **No back wall.** The first print's stopped the intake sliding to its rest and had to be broken off.
- **Relief holes in the roof over the two screws** standing up out of the channel's top (10 mm, cut both sides so the part
  fits either way round), so the roof sits flat.
- **Four pegs under the roof** that drop into the channel's small holes around its middle bearing hole. The part can only go on
  centred and cannot slide. The first print went on one hole off centre, dodging the screw heads; this one cannot.
- **80 mm long**, so the intake's round hard stop, moved inboard, sits beside it.

`poker_u_holes.png` is the part's footprint on the channel's own hole pattern; every hole and peg was checked against goBILDA's
mesh of the channel (zero overlap).

**To fit it:**

1. Intake lowered. Set the L on the front channel, poke forward, the four pegs into the four small holes around the channel's
   middle bearing hole (the 5th of 9, four each side). It drops on with no play sideways. If it rocks, a peg is on a screw head:
   turn it end for end.
2. The two screw heads come up through the relief holes and the three roof holes sit over bearing holes. If not, it is one hole
   over: lift and move it.
3. Two zip ties (`poker_u_ties.png`), one each side. From the outside, push the tie in through the wall hole 24 mm from the
   centre and the lip hole behind it, angled up and back; its tip finds the big bearing hole inside and comes out the 12 mm
   roof hole above it. Close on top, tail cut flush. Each tie wraps the channel's front top corner and the L's corner with
   it. Nothing goes out the back of the channel: a tie there fouls the intake's slides.
4. Check the hard stop beside it is clear of the L's end. Any gap is fine.
5. Run it. If it works, print the spares from the same file and change nothing.

**Or bolt it:** `stl/poker_u_hi_bolt.stl` is the same part with the front relief hole replaced by a 4.5 mm hole. The front
screw already standing out of the channel's top comes out, the L goes on (pegs), and a screw 4 mm longer goes back through the
roof into whatever it held: the L is then bolted to the intake with no new holes in the channel, the pegs take the sideways
load and one wide tie across the roof holes keeps the far side down. Print this one if an M4 4 mm longer than that screw is
in the kit tonight; print the first if not.

**The roof stops 10 mm short of the channel's back edge** (38 of its 48 mm). The stars' servo mount has tabs standing over
that edge, and whatever touches the part when the intake bottoms out should touch the steel stops, not this. The rear screw
sits in a notch open to the back. If something still crosses the roof in Onshape, say how far from the front lip and how high
above the channel's top, and the roof gets cut around it.

![the plate](print_job_u_only.png)

(The earlier plates: `print_job_next2.stl` is the 2.0 in blade with these two L's, `print_job_next.stl` the 3.0 in blade with
them, `print_job_both.stl` both blades with them.)

**Settings:** PETG (not PLA), 0.2 mm layers, 5 walls, 30 % infill, 5 top and bottom layers, support under the U's
front lip only, both U's (the blade needs none), brim optional. Millimetres: if it comes in tiny, scale by 25.4.

Thank you!

---

# Later, for the arm build: the blade with ears (not yet)

**`stl/print_job_low.stl`**, 179 x 272 x 40 mm, three parts (print this once the low arm exists; the eared blade's bore is
loose on purpose and will not key to the sled): `stl/mountain_wide_ears.stl` (the wide mountain
blade with Carter's lane, its tail cut short at 2.4 in, and a bolt ear at each end; 62 x 202 x 37 mm, 137 cm3) flat side
down, and `stl/poker_u.stl` (the poker U) with the hex coupon. Needs a
bed of at least 290 mm one way, or print the three one at a time. Millimetres. PETG. About 235 cm3. The blade's body
bore is a loose 8.6 mm hex and the ears' bores are 8.3 mm round on purpose: the shaft turns in the ears and only carries the
wheels; the blade is held by two M4 bolts per side through the arm into the blade's tail and ear. The U takes four zip ties
through the front channel's lip holes, as above.

![the plate from above](print_job_low.png)

**Settings:** PETG (not PLA), 0.2 mm layers, 5 walls, 40 % infill, 5 top and bottom layers, no supports, brim
optional. Millimetres: if it comes in tiny, scale by 25.4.

Thank you!

---

# Print job: the wide mountain blade and the poker (for the coach's tall arms; superseded by the low arm's plate above)

**Newest: `stl/print_job_wide.stl`**, 165 x 281 x 19 mm, three parts, flat side down. Needs a bed of at least 290 mm
one way (a 220 x 220 bed prints the three parts one at a time: `stl/mountain_wide.stl`, 201 x 76 x 15 mm;
`stl/poker_hang.stl`, 79 x 19 x 72 mm; `stl/poker_stand.stl`, 79 x 19 x 56 mm). Millimetres. PETG. About 200 cm3.

![the plate from above](print_job_wide.png)

- **mountain_wide** (top): the mountain blade with Carter's lane, the shape that emptied the FLOWER, at 7.9 in wide,
  the width of the robot's stick arms. Same 60° nose, 1.1 in peak, 12° tail, 7.15 mm hex (flats top and bottom), no end
  bosses. On the robot the hex is keyed so the blade lies flat when the arms are down (52° below horizontal).
- **poker_hang** and **poker_stand** (bottom, printed on their backs): the poker bracket. It hooks over the intake's
  top channel: the 6 mm flange sits on the channel's web with two M4 bolts through the web's holes (14 mm slots), the
  19 mm block runs down the channel's front face with one M4 through it, and the block's front is the poker, 30.5 mm
  (1.2 in) ahead of the intake face. Print both: which one fits depends on whether the intake is up (hang) or lowered
  1.8 in for the FLOWER (stand). Test on the sled first with a strip of plywood clamped at the same place. **Wait for the
  arm measurement before printing the pokers:** the reach is 1.2 in if the stick's shaft lands 3.3 in ahead of the intake
  face, 2.1 in if it lands 4.1 in ahead (`stl/poker_hang_2.1in.stl`, `stl/poker_stand_2.1in.stl`). The blade can go now.

**Settings:** PETG (not PLA), 0.2 mm layers, 5 walls, 40 % infill, 5 top and bottom layers, no supports, brim
optional. Millimetres: if it comes in tiny, scale by 25.4.

Thank you!

---

> The V nose (v8) is retired: the field saw no difference and the model agrees it cannot undo the front-to-back lock. The plate below is kept as the record of what was printed.

# Print job: FLOWER stick blades (for whoever prints)

**Newest: `stl/print_job_three.stl`** (24 Oct), 168 x 219 x 15 mm, three blades, flat side down. Needs a bed of
at least 220 x 220. Millimetres. PETG. About 240 cm3 of plastic. If the bed is smaller, print `stl/v7_lane.stl`,
`stl/v8_lane.stl` and `stl/v9_lane.stl` one at a time (each 137 x 74 to 83 x 12 to 15 mm).

![the plate from above](print_job_three.png)

Three blades, each one change from the one that works, all with Carter's lane:
- **v7** (top left): the shape that emptied the FLOWER on 19 Oct, now with a snug shaft hole. The baseline.
- **v8** (top right): v7 with a V front. The middle of the front edge juts 0.3 in further between the FLOWER's posts.
- **v9** (bottom, turned sideways): v7 with a lower peak, 1.0 in instead of 1.1, so the ball rides 0.1 in lower.

All three have the **7.15 mm hex, flats top and bottom**, which fit the REX shaft perfectly on the last print. v9's
hole is 0.03 in lower in the blade than the others' because the blade is thinner. No coupon needed any more.

**Settings:** PETG (not PLA), 0.2 mm layers, 5 walls, 40 % infill, 5 top and bottom layers, no supports, brim
optional. Millimetres: if it comes in tiny, scale by 25.4.

Thank you!

---


**`stl/print_job_dean_hedge.stl`** (24 Oct, 166 x 157 x 18 mm): v8 and v9 only, with the coupon. Superseded by the three-blade plate above.

(Details kept for reference.) 166 x 157 x 18 mm, three parts, flat side down. Fits a
220 x 220 bed. Millimetres. PETG. About 175 cm3 of plastic.

![the plate from above](print_job_dean_hedge.png)

Two different blades, each testing one idea about why the ball stops on the peak, plus the fit coupon:
- **v8 with Carter's lane** (left, 137 x 83 x 15 mm): the V front. The middle of the front edge reaches 0.3 in
  deeper between the FLOWER's posts, so the peak gets further under the ball. Same height as before.
- **v9 with Carter's lane** (right, 137 x 74 x 12 mm): a lower blade. The peak is 1.0 in instead of 1.1, so the
  ball's top is 0.1 in lower when it goes over, in case the collar above is what holds it. Straight front edge.
- **a hex-fit coupon** (bottom): skip it if the first one was printed.

Both have the **7.15 mm hex, flats top and bottom**, snug on the REX shaft (7.0 mm across the flats). Tap the shaft
in; file the flats a little if it will not go. v9's hole is 0.03 in lower than v8's (0.74 in above the bottom
instead of 0.77) because the blade is thinner; on the stick that drops the blade 0.03 in, which is fine.

**Settings:** PETG (not PLA), 0.2 mm layers, 5 walls, 40 % infill, 5 top and bottom layers, no supports, brim
optional. Millimetres: if it comes in tiny, scale by 25.4.

If the bed is under 200 mm: `stl/v8_lane.stl`, `stl/v9_lane.stl`, `stl/hex_fit_coupon_7_7.1_7.2_7.3_7.4.stl` one at
a time. STEP for review: `step/v8_lane.step`.

Thank you!

---


**`stl/print_job_dean_v8.stl`** (24 Oct, 175 x 157 x 18 mm): v8 lane, v8 plain, the coupon. Superseded by the hedge plate above, which pairs v8 with v9.

(Details kept for reference.) 175 x 157 x 18 mm, three parts, flat side down. Fits a
220 x 220 bed. Millimetres. PETG. About 180 cm3 of plastic.

![the plate from above](print_job_dean_v8.png)

What is on it:
- **v8 with Carter's lane** (left, 137 x 83 x 15 mm): v7 with a V-shaped front. The middle of the front edge reaches
  0.3 in deeper than the sides, and the slanted parts sit on the FLOWER's two posts, so the blade centres itself and
  gets further under the ball. Everything else is v7.
- **v8 plain** (right, same size): the control, no lane.
- **a hex-fit coupon** (bottom, 108 x 12 x 18 mm): five hex holes at 7.0, 7.1, 7.2, 7.3 and 7.4 mm across the flats,
  marked with one to five notches. Skip it if the first one was printed.

**The hex holes.** The shaft is goBILDA "8 mm REX": a round 8 mm shaft with six flats **7.0 mm across the flats**.
These are cut at **7.15 mm across the flats, flats top and bottom**: a snug hex. Tap the shaft in; if it will not go,
a few strokes of a flat file on the flats. Tell us which coupon hole is the snug one and the next blades get that
size.

**Settings:**
- **Material:** PETG, or nylon. Not PLA: it cracks when the robot drives the blade into the field element.
- **Layers:** 0.2 mm. **Walls:** 5. **Infill:** 40 %. **Top and bottom layers:** 5.
- **Supports:** none. The hex holes bridge fine at this size. **Brim:** optional, if the bed's adhesion is iffy.
- If the parts come in tiny (or 25 times too big), the slicer read them as inches: scale by 25.4 (or 1/25.4).

If the bed is under 200 mm: `stl/v8_lane.stl`, `stl/v8.stl` and `stl/hex_fit_coupon_7_7.1_7.2_7.3_7.4.stl` one at a
time. STEP solids for review: `step/v8_lane.step`, `step/v8.step`, `step/hex_fit_coupon.step`.

Thank you!

---

**`stl/print_job_dean_v7.stl`** (23 Oct, 162 x 157 mm): v7 lane, v7 plain, the coupon. v7 is v8 without the V front.
Still a good print if v8 is not wanted; the holes are the same.

**v6** (the 50 degree noses, 19 and 20 Oct) is retired: the ball sat on the peak. Its files are gone from the repo.

**Earlier: `stl/print_job_dean_v5.stl`** (16 Oct, 169 x 157 mm): v5 with the lane, v5 plain, a coupon with 8 mm hex
holes. Printed and tested on 19 Oct: it empties the FLOWER, but the hole was an 8 mm hex on a REX shaft (7 mm flats),
so the blade spun on the shaft and the push was hard. Superseded.

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

## Thursday test: the poke family (28 Oct 2026, revised for sideways error)

`stl/print_job_family.stl` prints five pokers: reach **1.3, 1.6, 1.9, 2.2 and 2.5 in**, each **36 mm (1.4 in) wide** instead of 19 mm,
top edge beveled 8 mm, bottom square, narrowing from 36 to 24 mm over the last 10 mm, the pegs, relief, wall holes (24 and 32 mm; the
ones at 16 are behind the wider poke), the counterbored M4 hole over the front screw and the roof cut short. 1.3 is the current reach
with the new nose and width: the control.

**Why wide.** We cannot drive in dead centre, so the poke has to work across the whole opening. `page/lat_grid.js` offsets the whole
robot sideways (the 7.9 in blade and the poker together) from minus 2.4 to plus 2.4 in, at 4 in/s, three stack leans and two
frictions (cases fed of 6; results in `page/lat_grid_results/`). The narrow 0.75 in poke works to about 0.8 in off centre and then
drops to 2 to 4 of 6. A 1.5 in poke is 6 of 6 out to 1.6 in off centre at the best reach; wider than that adds nothing (2.25 and 3.0
in are the same or slightly worse), and every shape fails at 2.4 in, because the blade itself no longer meets the FLOWER squarely.
Best reach in the model: 1.3 in if the blade's lip sits 3.8 in ahead of the face, 1.9 to 2.2 in if 4.7 in. At the wrong reach
the model drops to 2 to 4 of 6 whatever the width, so width fixes sideways error and reach still has to be right.

Fix the off-centre blade first (the FLOWER's axis was 2.4 in off the poker's in the ring photo), then deliberately test with the robot
pushed in 1 and 1.5 in to each side, not just centred. Run slow (about 3 in/s) and at speed, and note how many of four balls it frees.
Checks: the 18 in start box (stowed robot 16.7 in with the 1.3 in poke, so about 17.6 in with 2.2 and 17.9 in with 2.5: tape it);
one stow cycle with each (the model keeps the stowed blade 3.0 in or more from the 2.5 in poker, without the bevels); each poker is
about 75 to 125 cm3, so allow more print time and filament. Nothing here is lower than the locked poker (the stowed blade's lip
passes 2.8 mm under it).
