# FLOWER stick prototype: fixed, on the mentor's robot (issue #179)

A quick test of the FLOWER extractor before we build the folding one (`cad/intake-b/` §3). It's the ramp-hook FLOWER
block (`cad/ramp-hook/`) on an 8 mm REX shaft across his front. The shaft is held at both ends by goBILDA Hyper Hubs on
goBILDA grid plates, and the plates bolt to holes his front uprights already have: **no drilling, no cutting.** It
doesn't fold.

![The stick on his robot, and seated on a FLOWER](proto-3d.png)

![Side and top view, on his robot, with a FLOWER seated](proto.png)

Checked on 10 Oct against his 9 Oct Robot.step with `tools/robot-cad/module_check.py flower_stick` and
`flower_stick_seated`. Nothing clashes: the only contact is a POLLEN staged in his CAD. Seated on a FLOWER
(`tools/robot-cad/flower.py`), the FLOWER touches only the block, with its lower bracket 0.17 in ahead of his roller.

| | |
|---|---|
| Shaft | X 11.02, z 0.96 (X 11.33 with the forward reach) |
| Block tip | X 11.49: 3.93 in ahead of his face |
| FLOWER seated | its centre 2.53 in ahead of his face (X 10.09) |
| Robot length | **19.52 in. Breaks the 18 in start (R102)**; deployed it's inside 18 × 24 in (R105) |
| Lowest part | the B plates and hubs, 0.49 in off the tiles |
| Under his corner wheel | the right B plate's top is z 2.06; the wheel's bottom is 2.22 |

**This is a practice-field part only.** It can't start a match. The folding version is the one that does.

## Parts

All goBILDA, all checked on gobilda.com on 10 Oct.

| Part | SKU | Qty |
|---|---|---|
| Grid plate 7 × 7 (56 × 56 mm), plate A | 1116-0056-0056 | 2 |
| Grid plate 5 × 17 (40 × 136 mm), plate B | 1116-0040-0136 | 2 |
| Hyper Hub, 8mm REX bore, clamping | 1310-0016-4008 | 2 |
| 8mm REX shaft, 264 mm, uncut | 2106-4008-2640 | 1 |
| Clamping collar, 8mm REX, 9 mm long | 2910-0920-4008. Any 8mm REX collar from stock works: the discontinued 10 mm 2910-1020-4008, or a 2920 set-screw collar. They only back up the block's set screws. | 2 |
| M4 × 12 socket head | 2800-0004-0012 | 12 |
| M4 × 8 socket head (B into the hubs) | 2800-0004-0008 | 8 |
| M4 nylon-insert lock nut | 2812-0004-0007 | 12 |
| M3 self-tapping set screws, about 6 mm (2 per block) | any | 6 |

**Print** (PETG, 5 walls, 40 % infill; settings as in `cad/ramp-hook/README.md`):
- `stl/block_060.stl`, `block_065.stl`, `block_070.stl`: the same block with its bottom 0.60, 0.65 or 0.70 in off
  the tiles (top 1.25, 1.30, 1.35). The shaft hole is at the same height in all three, so **swapping blocks is the
  height adjustment.**
- `cad/ramp-hook/fit_coupon.stl` first, so the bore fits the shaft.
- `block_070` has only 2.5 mm of plastic under the shaft. Print it, but expect it to be the one that cracks.

## Build

Grid positions count goBILDA's 8 mm holes. On each upright's web, use the two **4 mm holes 0.63 in above the
upright's bottom end**: these are at X 5.98 and X 7.24, z 3.48. The flaps module uses the same two holes; you can't
fit both at once.

**Confirm on the robot:** these holes come from the 9 Oct Robot.step. His built chassis (tall fixed uprights, slider
rails for a floating intake) may differ. Check that the holes are there and that a nut fits behind each one before
bolting anything. Every joint below names its holes or the threaded part it goes into; nothing floats.

1. **Plate A on each upright's web, outside face.** Put A's top-row holes 1 and 5 on the web's two holes. A hangs
   below the upright (to z 1.43), and its front edge is 0.47 in past his face. Fit 2 × M4 × 12 from outside, with
   lock nuts inside the channel, flats up and down. His Dual Block sits about 0.3 mm below the nuts.
2. **Plate B outside A,** lengthwise front to back. Line up B's top two rows on A's bottom two rows, with B's rear
   column on A's column 2. Fit 4 × M4 × 12 (A's columns 2 and 7), nuts on A's inside face. B now runs forward under
   the corner wheel.
3. **Hubs.** Put each Hyper Hub on B's inside face, on B's 2nd row from the bottom, 2nd column from the front.
   Fit 4 × M4 × 8 from outside into the hub's tapped holes. Don't clamp the hubs yet.
4. **Shaft and block.** Slide the shaft through one hub, then a collar, the block (curved face forward), a collar,
   and into the other hub. Centre the block. Clamp both hubs. Tighten the block's 2 M3 set screws onto the shaft's
   flats, square to the tiles. Push the collars against the block's sides and clamp them. They're backups for the
   set screws, and they sit just outside the FLOWER's grey uprights.
5. **Check on a tile:** block bottom 0.60 to 0.70 in, top 1.25 to 1.35 in, flat top level. The B plates, hubs and
   shaft must all be 0.45 in or more off the tiles. Spin the corner wheel by hand over the right B plate.

**Adjustments built in:**
- **Height:** swap the printed block (0.60 / 0.65 / 0.70). Rocking the block a few degrees on its set screws also
  trims the front edge's height.
- **Reach:** move B forward one column on A, and the hub stays on B's same holes. The block goes 8 mm forward
  (`REACH=1 python3 build.py`). That puts the FLOWER's bracket 0.49 in ahead of his roller instead of 0.17, at the
  cost of 8 mm more roll back to the roller. Checked clear too.
- **Sideways:** loosen the collars and set screws and slide the block along the shaft.

## Check on the robot before you build

These need the real robot; none are guesses in the drawing.

- [ ] His CAD has two loose **M4 hex screws just below each upright, at X 6.0, z 1.6 and 2.2**, heads out to |Y| 5.46,
      with nothing visible for them to hold. They fall exactly on plate A's column-1 holes. If they're on the robot,
      swap each for one 4 mm longer, through A. Their hex heads then sit at B's rear edge, so use button heads
      (2802 series). If they aren't, ignore this.
- [ ] Room inside each upright for the lock nuts on the two z 3.48 holes (his Dual Block sits about 0.3 mm below).
      If one won't seat, use a plain M4 hex nut, which is thinner.
- [ ] The bottom of the right corner wheel is 0.16 in above the right B plate. Check it at full travel.
- [ ] Is anything mounted on the outside of either upright's web that the CAD doesn't show? Look for wires and
      the flaps.
- [ ] Measure the robot's front-to-back length with the stick on. It should be about 19.5 in.

## Test on the practice field

A FLOWER loaded with 4 POLLEN, staged the way the match stages them. Drive in under driver control, slowly, until the
block stops on the grey uprights. Stay there, with the intake roller running. Film from the side at 60 fps or more.

| Set | Approach | Runs |
|---|---|---|
| A | centred, square | 20 |
| B | 1 in off-centre, left | 10 |
| C | 1 in off-centre, right | 10 |
| D | 2° yawed, CCW | 10 |
| E | 2° yawed, CW | 10 |

For each run, record:
- the block used (060 / 065 / 070), the reach (0 / 1) and the set;
- **POLLEN out**: 0 to 4, counted at the roller;
- **time**, from the block touching the uprights to the last POLLEN at the roller. Read it off the video. The model
  says about 1.2 s;
- whether the robot ended centred (the curved front should pull it in), and anything that caught: the ring's lip,
  the collars on the uprights, a POLLEN wedged under the bracket.

Run set A with `block_065` first. If set A empties at least 19 of 20, run B to E. If it doesn't, try 070 and then 060
before changing anything else. Then try the forward reach on whichever block did best.

- **Size rules:** fixed, the robot is about 19.5 in long, so it can't start a match (R102, 18 in). Once moving it's
  within 18 × 24 in (R105). The folding version (`cad/intake-b/`) stows to 17.96 in.
- **What to bring back:** the table, and a measurement of the real FLOWER's pocket edge (the base plate's
  thickness). That's the number `doc/ramp-hook.md` says decides a short block against a long one.

## The pivoting build with wheels (what the lab built, 10 Oct)

The fixed plates were hard to get to the right height without a drill, so the lab built a pivot instead. Two pattern
beams swing from a cross shaft at the robot's end. Small wheels at their lower ends roll on the tiles, outside the
FLOWER, and carry the lower shaft and the block. **The wheels set the height:** the shaft sits at the wheels' radius.

A hand push into a FLOWER loaded with 4 POLLEN (slow-mo, filmed from above) brought at least 2 POLLEN out. They then
piled up against the chassis, because that end has no intake. Counts and timing from a side view are still to come.

To print a block for this build, give `block.py` the wheels' diameter. The lab's shaft and wheel bores are **plain
8 mm hex**, not REX, so the block's bore is hex by default: 8.3 mm across the flats, flats top and bottom. A hex
shaft can't turn in it, so the block needs no set screws.

    python3 cad/modules/flower-stick-proto/block.py --wheel-mm 48 --bottom 0.70
    python3 cad/modules/flower-stick-proto/block.py --shaft 0.77 --bottom 0.52 --top 1.30   # the lab's 39 mm wheels

- **The wheels are about 1.55 in (39 mm) across.** That's from a tape stood upright against a wheel (10 Oct): its
  top reads about 1.54 in. So the shaft sits about 0.77 in off the tiles, too low for the standard block. (An
  earlier reading of 1.0 in was a misread of a blurry photo.)
  - **Print `stl/block_hex_shaft0.770_bottom0.52.stl`:** bottom 0.52 in, top 1.30, the shape of the mentor's first
    sketch, which emptied all 4 POLLEN in `ramp.py`. It clears the 0.43 in ring by only about 0.1 in (0.07 to 0.12
    if the shaft is really 0.75 to 0.80).
  - **Better: fit wheels about 48 mm (1.9 in) across.** That raises the shaft to about 0.95 in, so the standard
    block (`stl/block_hex_shaft0.945_bottom0.70.stl`) fits with 0.27 in of clearance over the ring.
- **Wheels much over 52 mm put the shaft too high.** The 0.65 in block can't keep its top under 1.35 in with the
  bore inside it, and `block.py` refuses. Ask for a block that hangs below the shaft.
- Print the fit coupon from `cad/ramp-hook/` first. Its bores are round, so on a hex shaft read it across the
  flats; or print one block and check the fit.

## 15 Oct test: two blocks side by side empty it; one block fights you

Carter and a mentor pushed the wheeled sled into a loaded FLOWER by hand with the two printed blocks (A and B) side by
side on the shaft. Three videos: the FLOWER emptied, all 4 POLLEN out in the last run, in about a second once the blocks
were under. **The concept works.** Two findings:

![Two blocks at the FLOWER](test-2026-10-15/two-blocks-at-the-flower.jpg)
![Three moments of a run: in, under, out](test-2026-10-15/sled-run-frames.jpg)

- **One block on its own is hard to get under the POLLEN.** It's 2.72 in wide with a convex front, and it has to enter
  the FLOWER's 2.79 in hole, so it has to be centred to about 1/32 in. By hand that's luck.
- **Two blocks together go under easily.** The POLLEN sits in the valley between their two curved fronts. The valley
  centres the POLLEN on the blocks instead of the blocks on the hole, and the blocks never enter the hole.

### Why it couldn't be seated against the back (16 Oct)

The concept, as the lab built it: the block drives in **steep face first, ramp toward the robot**. Once the block's
top corner is under the bottom POLLEN, the ball rides onto the flat top and rolls down the ramp, straight out of the
FLOWER perpendicular to the wall, into the intake; the stack drops one and the next ball follows.
`stack_sequence.py` draws that frame by frame from the simulator (`stack_sequence.png`).

A static force balance explains the working back and forth. The bottom POLLEN is pinched between the block's steep
face and the FLOWER's uprights, and a steady push lifts it only if the contact normal is tilted up by more than twice
the friction angle: 33° at μ 0.3, 44° at μ 0.4. Block A's steep face leans 17° from vertical, so the push is tilted up
17°, and it lifts only if μ is under 0.15: it stalls at any realistic friction and goes on only when wiggled or hit.
The 2-D simulator never saw this because its blade is kinematic: it can't stall. The exit half of the concept, the
ramp out toward the robot, is right; the entry is what has to change. The levers, in order of effect: a wedge entry
instead of a wall (the v2 nose: 45° face, its edge at 0.60 in meets the ball at 35°; `v2_steep`, 55° with a sharp
0.52 in edge, 39°), a lower edge still (a sheet lip on the ring at 0.37 in: 47°), a slippery skin (UHMW tape), and a
roller, which removes the limit. v2 keeps the exit ramp (its 38° back slope) and replaces the wall with the wedge.

### The FLOWER, from FIRST's field CAD

The model in `tools/robot-cad/flower.py` was scaled off the manual's figure. On 16 Oct it was replaced with
measurements from FIRST's field CAD (`tools/robot-cad/flower_from_field_cad.py`), about the bottom POLLEN's centre on
the tiles:

| | manual's figure | field CAD |
|---|---|---|
| base ring top | 0.43 | **0.35** |
| hole | 2.79 | **3.08** |
| uprights' forward points behind the POLLEN's centre | 1.25 | **1.18** (at y ±1.42) |
| uprights' inner corners | | (1.61, ±0.61): a lifted POLLEN is pinned at **x +0.35** |
| bracket opening | 3.55 | **3.90** |
| green pipes | reach the base | **start 4.25 in up** |

So nothing in front of the uprights is in the blade's way, and the hard stop is the uprights' forward points.

### The divot blade v2 (`divot_blade.py`)

![Carter's sketch: the two blocks today (top), one blade with a divot (middle), and its front and end views (bottom)](test-2026-10-15/sketch-divot-blade.jpg)

Carter's sketch, built with a wedge nose and every depth from the field CAD. The sketch's divot can be read two ways,
so both are built and both are on the print plate: a **scoop** cut into the front edge (the sketch's top view: an arc
in plan, 1.8 in across, 0.21 in deep), or a **spoon** hollowed into the nose face (the front view's trapezoid: a bowl,
1.8 in across, 0.15 in deep, concave across and up the face). Everything else is the same. Carter says which he drew;
the test runs both.

![The scoop](divot_blade_v2_scoop_w5.4_shaft0.770.png)
![The spoon](divot_blade_v2_spoon_w5.4_shaft0.770.png)
![Seated in the field CAD's FLOWER](seated_in_field_cad.png)

- **A 45° nose**, so a steady push lifts the stack (μ up to 0.41 sliding, 1.0 rolling). Tape the nose with UHMW or
  packing tape and it has margin to spare. About 1.6 lb of push lifts the four POLLEN.
- **A low centre lane** (2 in wide, top 1.05 in, under the POLLEN's centre at 1.40) with a shallow divot in its nose
  that centres the POLLEN. It has **no flat top**: `tools/ramp-hook/blade_variants.py` (ramp.py with the CAD FLOWER)
  keeps all 4 POLLEN in when a flat reaches back to the pinned POLLEN, and empties 4/4 at 2, 4 and 12 in/s, μ 0.3 or
  0.5, when the slope starts at the ridge. The pinned POLLEN sits 0.38 in down the lane.
- **Taller shoulders** (1.25 in) either side: they are the hard stop, on the uprights' forward points, and they hold
  the 8 mm hex shaft with 8 mm of plastic over it. The lane only skins over the shaft (1.5 mm): the shoulders take
  the load.
- Width 5.4 in, 1.93 in front to back; bottom 0.52 in (0.17 over the ring); 8 mm hex bore at the sled's 0.77 in.
- **Checked:** swept into the CAD FLOWER, it touches nothing until its shoulders meet the uprights at x 1.18; seated,
  it is clear of the ring, the bracket and the pipes.

**Print:** `stl/print_plate_divot_blade.stl`: both blades, 137 × 108 mm, flat side down, the scoop at one end and the
spoon at the other. PETG, 5 walls, no supports. Every size is a flag: `--divot scoop|spoon`, `--shaft 0.945 --bottom
0.70` for 48 mm wheels, `--width`, `--nose-deg`, `--lane`.

**Six ways to cut it (16 Oct):** all the same body, built by `divot_blade.py` with flags, `stl/v2_<name>.stl` and
`v2_<name>.png` each. The simulator sees the lane and the stop, not the hollow's shape, so it rates them all 4/4; the
hollows differ in how they centre the POLLEN and how a real push feels, which is what the test is for.

| name | flags | what changes |
|---|---|---|
| `v2_scoop` | `--divot scoop` | Carter's top view: an arc in the front edge, 1.8 wide, 0.21 deep |
| `v2_spoon` | `--divot spoon` | Carter's front view: a bowl in the nose face, 1.8 wide, 0.15 deep |
| `v2_vee` | `--divot vee --divot-chord 2.0 --divot-depth 0.3` | a V notch in plan, the strongest self-centring |
| `v2_tongue` | `--divot scoop --lane 1.2 --lane-ahead 0.4 --divot-chord 1.0 --run-len 1.0 --run-deg 3` | a 1.2 in lane reaching 0.4 in past the shoulders, between the uprights' lobes; its ridge 0.78 in past the pinned POLLEN |
| `v2_highride` | `--divot scoop --shaft 0.945 --bottom 0.70` | the scoop for 48 mm wheels, 0.35 in over the ring |
| `v2_plain` | `--divot none` | no hollow: the control |
| `v2_steep` | `--divot scoop --nose-deg 35 --land 0.01` | the scoop with a 55° nose (from horizontal) and a sharp 0.52 in edge: the jam-margin option |
| `v3_peak` | `--divot scoop --nose-deg 35 --land 0.01 --lane-top 1.28 --sh-top 1.28 --lane 5.0 --run-len 0.01 --run-deg 0 --slope-deg 45` | one mountain the full width: 55° up to a 1.28 in peak, 45° down; no flat, no lane, no shoulders |
| `v4_carry` | as `v3_peak` with `--divot none --slope-deg 14.4` | the mountain with a 14° tail to the ring: the ball rides the blade all the way to the Gecko |
| `v5` | `--divot none --nose-deg 30 --land 0.08 --lane-top 1.10 --sh-top 1.10 --lane 5.0 --run-len 0.01 --run-deg 0 --slope-deg 12 --hex 8.2` | the searched shape: 60° nose, 1.10 in peak, 12° tail, 0.54 in of margin |
| `v5_lane` | as `v5` with `--gutter-rise 0.3` | v5 with Carter's lane: one curve across the whole tail (12.3 in radius), 0.3 in up at both edges, from 0.9 in behind the lip, full depth after 1.6 in; no flats |

The write-up with the viewer of all six (and Block A) is the page linked from issue #179.

### Other teams, and into the intake (16 Oct)

Four YouTube Shorts (19705 WXYZ's Ri3D, CrossFire 9968, Hexadecimal Nibble, Eaglebots 13532) show three recipes: a thin
lip with a powered roller right behind it, a big passive wedge, and a servo arm that sweeps the base sideways with the
wall as the stop. All the wedges meet the ball low. The roller is the bigger idea: it pulls the ball back and up, so
the push only has to get the lip under it. For us it says where the intake's first roller has to be.

A full-width ramp (a 12 in sheet with a plate and a roller) was drawn and dropped the same day: it put a sheet in front
of the intake and left the ball on a shelf 2 in from the roller. What replaced it is the original concept with numbers:
the blade releases the ball straight into the intake's mouth, and the mouth sits at the FLOWER's face.

- Blade's stop: 1.18 in behind the FLOWER's centre (the uprights' forward points; the 5.4 in shoulders reach them).
- Blade's back edge: 0.5 in in front of the centre; the ball leaves the back slope there.
- Intake roller's rim: at least 2.5 in in front of the centre, the first spot clear of the bracket (2.41) and the pipes
  (2.25), which is 0.1 in in front of the ring's edge. About 2 in of open tile between.
- So the stick is about 3.7 in long beyond the mouth, the sled arms short, the blade's shaft about 2 in ahead of the
  roller's axle.
- `tools/ramp-hook/intake_sim.py` is the 2-D check (v2 lane profile, the bracket's lintel, a roller at the face):
  all four POLLEN leave and pass the roller at 2, 4 and 12 in/s, friction 0.3 and 0.5. It has no rolling resistance,
  so the 2 in of open tile is the part to doubt until it is tested. `intake_section.py` draws it.

Two things the field CAD rules out for a bigger ramp: a roller cannot sit inside the FLOWER (a ball on a 0.45 in lip
tops out at 3.25, the bracket's underside is 3.90), and a rising ramp cannot carry the ball to a crest (the lip stops
0.83 in past the pinned ball).

**The mentor's 16 Oct `Robot.step` answers that** (Google Drive, 152 MB; read with `cad/full-robot/build.py`'s
loader, but note the export is turned 180° about the left-right axis relative to the 9 Oct one, so `module_check.py`
places it upside down and backwards until its frame is re-derived from the wheels). In a frame with the floor at the
drive wheels' bottoms and x measured back from the intake uprights' front faces:

| what | where |
|---|---|
| first-stage roller | seven 48 mm GripForce Gecko wheels on a 240 mm REX shaft, axle 2.9 in high, wheel bottoms 1.94 in off the floor, rims flush with the uprights' front (0 to 1.9 in back) |
| its width | left −4.0 to +3.5 in: 7.5 in of wheel |
| intake opening | between the 5-hole lowside uprights at ±4.9 to 5.4 in: about 9.8 in |
| second stage | two 96 mm Gecko wheels, 6.3 to 10.1 in back, 1.7 to 3.6 in high |
| ahead of the face | the front omni wheels' rollers 0.3 in (at |y| 5.2 to 7.1), and a servo-driven 4.5 in disc at the front left (1.2 in ahead, |y| 4.0 to 8.4, 2.5 to 6.9 in high) |

So the mouth is the floor itself, at the front: the good case. For the FLOWER: the robot's face sits 2.5 in in front of
the FLOWER's centre (0.1 in off the ring and the bracket), the blade's stop is 3.7 in ahead of the face, its shaft
2.9 in ahead at 0.77 in high, which passes 0.66 in under the gecko row. The sled's arms can hang from the gecko shaft
itself on bearings (4.4 in from the gecko axle to the blade's shaft) or pivot on the uprights beside the outer wheels. The 4.5 in disc clears the
FLOWER only while the robot is within about 1 in of centred on it.

**v3, the mountain (`v3_peak`, `--nose-deg 35 --land 0.01 --lane-top 1.28 --sh-top 1.28 --lane 5.0 --run-len 0.01
--run-deg 0 --slope-deg 45`):** the v2 lane's flat run existed only to leave skin over the 8 mm shaft; with the 55°
nose the peak rises to 1.28 in at the same 0.30 in past the pinned POLLEN, the shaft gets 8.8 mm of plastic over it,
and the profile is one mountain the full 5.4 in width: 55° up, peak, 45° down to the back edge 0.12 in behind the
FLOWER's centre. No lane, no shoulders, so where the ball sits across the width does not matter, and the depth is set
by the nose meeting the uprights' forward points anywhere within ±1.3 in of centre. Exit speed from a 1.28 in peak is
about 19 in/s, enough for the 2 in of tile. The back slope's angle hardly matters (the speed comes from the drop); the
peak height is capped by the ridge rule (55°: 1.28; 60°: 1.44).


**The drive-in animation** (`tools/ramp-hook/animate.py` → `animation_runs.json`, embedded in the page): every blade at
4 and 12 in/s, friction 0.3 and 0.4, in the slice with a stall check (a ball pinched between the blade and the uprights
lifts only if the push at the blade contact is tilted up more than the friction angle, the ball rolling on the blade
and sliding on the uprights). Block A stalls on contact in every case (14° to 17°). v2 steep fails at 4 in/s: its
0.6 in run at 4° leaves the pinned ball on a near-level surface and only momentum at 12 in/s carries it over. v2 and
v3 empty the FLOWER at both speeds, the last ball past the Gecko about 2.5 s after the drive starts at 4 in/s and
1.5 s at 12 in/s. A flat anywhere behind the ridge is the enemy; v3 has none.

**v4, the carry (`v4_carry`, v3's flags with `--slope-deg 14.4`):** v3's ball comes off the back edge into the hole and
has to climb the 0.35 in ring on its own speed (the dip). v4 keeps v3's nose and peak and continues the back slope at
14° down to the blade's bottom 2.3 in in front of the FLOWER's centre, over the ring: the ball never touches the tiles
and reaches the Gecko still on the blade. 137 × 102 × 19 mm, 113 cm³.

**The simulator in the page** (`page/sim.js`, a port of `ramp.py` + `animate.py` to JavaScript; `page/anim2.js` draws
it, and `page/drive3d.js` the same run in 3D, over the FLOWER's section from the field CAD, `page/flower_section.json`, and the mentor's robot cut at the
centreline, `page/robot_section.json` from `page/section.py`): presets Block A, v2, v3, v4, sliders for edge height, nose angle, peak height, tail angle, drive
speed, friction and stop error, readouts of the peak's margin past the pinned ball, the first-contact angle and the
friction it beats, and where the tail ends. The Gecko is a soft drag wheel there (anything within its reach is pulled
toward the robot at its surface speed), not a rigid roller. Findings: v4 feeds all four in 2.1 s at 4 in/s and 1.2 s at
12; it tolerates the robot over-travelling (it cannot, the uprights stop it) but a stop 0.3 in short puts the ball on
the peak, which is the whole margin. A lower peak (1.1 in) with a 60° nose has 0.5 in of margin and feeds just as fast:
with the tail carrying the ball, peak height no longer buys anything, margin does.

**v5, searched (`v5`, `--nose-deg 30 --land 0.08 --lane-top 1.10 --sh-top 1.10 --lane 5.0 --run-len 0.01 --run-deg 0
--slope-deg 12`):** `page/opt.js` ran 882 mountain shapes (edge height 0.52 to 0.60, nose 45° to 70°, peak 0.9 to
1.5 in, tail 10° to 30°) through the browser simulator's physics in node, each in 12 scenarios: friction 0.3 and
0.45, drive 3 and 12 in/s, the robot stopping 0, 0.2 and 0.35 in short of the uprights. 89 shapes fed all four every
time; 4 of those leave enough plastic over the 8 mm shaft. All four are the same family: a 60° nose, a 1.10 in peak
(0.52 to 0.54 in past the pinned POLLEN) and a 10° to 12° tail. v5 is the fastest of them: 60° nose from a 0.60 in
edge, peak 1.10, 12° tail ending over the ring 1.85 in in front of the FLOWER's centre; all four past the Gecko in
2.6 s worst case. v4's 0.30 in margin fails the 0.35 in short-stop scenario; v5's 0.54 passes it. 137 × 77 × 15 mm.
Results in `page/opt_all.jsonl`.

**Carter's lane, and the sideways model (16 Oct).** Carter's divot was misread twice (a scoop in the front edge, a
bowl in the nose face). What he drew: the front edge stays plain; the ramp toward the intake has a rounded lane down
its middle, like the valley the two blocks made by accident, with no sharp edges. That is `v5_lane` (`--gutter-rise`,
`--gutter-r`, `--gutter-start`, `--gutter-lead` in `divot_blade.py`): material is added above the tail and a cylinder
tangent to the tail at the centreline is cut out of it, so the centreline profile and everything the search found are
unchanged. With `--gutter-r 0` (the default) the cylinder's radius is chosen so the curve spans the whole width, from
the centre up to `--gutter-rise` at both edges: no flats, no shoulders. A smaller radius gives a trough with flat
shoulders outside it, which the model found worse (below). To judge it, `page/sim.js` gained a sideways dimension: the ring's hole (±0.14 in) and the uprights' vee
hold the bottom POLLEN on the FLOWER's centreline until its centre is 0.85 in behind the lip; the four pipes
(0.525 in radius at ±1.72) hold the stack, which lets the upper balls lean ±0.85; a ball on the lane feels its
cross-slope as a sideways push, with rolling friction across the contact; ball-ball contacts are 3-D; the Gecko row
grabs a ball only inside the intake's mouth. Three disturbances: the robot parked off-centre, the robot turned, the
stack leaning. `page/lat_sweep.js` ran the plain tail and five lanes through offset 0/0.8/1.3 in, lean 0/0.3/0.6 in,
turn 0/8°, a 7.5, 4 or 3 in mouth (324 runs, `page/lat_sweep.jsonl`). What it says:

- With the 7.5 in Gecko row at the FLOWER's face, the plain v5 tail fed all four in every case. The wheels take the
  ball about 1.4 in after the FLOWER lets go of it: there is no stretch of tail for a lane to act on.
- Carter's lane as one full-width curve (0.3 in at the edges) does the same in every case, whether it starts 0.9 or
  0.5 in behind the lip and whatever its lead-in. A deeper full-width curve (0.5 in at the edges, 7.5 in radius) is
  also clean on the 7.5 in row and rescues a few leaning-stack runs with a 3 or 4 in mouth.
- A trough with flat shoulders (4 in radius, 0.4 in shoulders) misses or sticks when the robot is 0.8 to 1.3 in
  off-centre: the FLOWER is still holding the ball on its own line while the ball meets the shoulder's edge head-on.
  The flat is what hurts, not the depth.
- A leaning stack squirts the bottom ball sideways (its weight on an off-centre ball, released when the vee lets go)
  at 10 to 20 in/s, more than a 0.3 in curve holds (12 in/s). With a 3 in mouth no lane rescues all of that; with the
  7.5 in row nothing needs rescuing.

So on this robot the lane is neither needed nor harmful as a full-width curve; it would matter if the intake were
narrower or further back, or on the sled, where the balls roll out across the floor. Decision (16 Oct): the lane
blade is the design, since the curve costs nothing in the model and may help on the floor; v5 plain is printed beside
it as the control for the stick test. The model's weak point is how a real stack settles in its pipes (the balls are taken as rigid, and a
tilted stack settles into a zigzag against the pipes); the lean slider is an input, the squirt is its consequence.

**Will it survive the robot? (16 Oct)** The plastic around the 8 mm hex looks thin (2.2 mm under the bore, about
4 mm over) and that is fixed by geometry: the bottom clears the ring by 0.17 in and the peak is tuned, so 13.5 mm of
blade carries a 9.5 mm hex. It holds because the blade is 137 mm wide and every hit load spreads across that width:
the drivetrain's push into the posts (about 25 lb before the wheels slip) goes nose to shaft through 13 mm of plastic
over the full width, well under 1 MPa; a 20 lb hit on the tail tip bends the section at the bore at about 1.6 MPa
against PETG's 50; the ball-pinch torque on the hex corners is a few in·lb over 137 mm of engagement. What wears is
the hex corners (hence the coupon fit and the engagement length) and the nose edge (a consumable: print two sets,
inspect, swap). The weak link is the shaft, not the plastic: an 8 mm steel shaft on a 17 in span between outboard
arms, pushed with 25 lb at the blade, sees about 250 MPa, at yield, and takes a set; on the intake uprights (10 in
span) about 150 MPa, fine. So: support the shaft close to the blade (arms on the uprights, or a bearing block each
side of the blade on outboard arms), and once the shape is proven change the material (nylon or CF-PETG), not the
thickness.

**Dean's print job** is `print_job.py` → `stl/print_job_dean.stl` (169 × 157 mm: v5_lane, v5 plain, the hex coupon)
with `print_job_dean.png` as the picture in `PRINT_ME.md`.

**The shaft (20 Oct).** goBILDA's 8 mm REX is a round 8 mm shaft with six flats 7.0 mm across the flats. Every bore
we had printed was an 8 mm hex (8.2 to 8.3 mm across the flats), whose inscribed circle is larger than the shaft:
a round bearing. The 15 Oct blocks turned on the shaft, and on 19 Oct the v5 blade tilted nose-down under the push
until its underside met the ring, which is why it pushed hard (see the table in the next paragraph: 60° held firm is
5.6 W, tilted 4° it is 11 W, tilted 8° it stalls). The bore is now 7.15 mm across the flats (`--hex 7.15`, the
default in `divot_blade.py` and `blade6.py`), with `--hex-angle` for the hex's rotation about the shaft once the
mount on the robot is known. The coupon (`hex_coupon.py`) defaults to 7.0 to 7.4.

**v6: the push (19 Oct).** The v5 blades emptied the FLOWER on the stick but took a hard push to get under the ball.
The search had scored feeding and margin, never the push. The force balance for the bottom POLLEN pinched between
the blade's face (angle α from horizontal, the ball rolling on it) and the uprights (sliding, μ) is
F/W = sin α / (cos α − μ sin α) times the stack's weight: 60° needs 5.6 W at μ 0.4 (and stalls outright if the ball
slides on the face), 50° 2.3 W, 45° 1.7 W, 40° 1.3 W. `page/opt6.js` re-ran the search with the push as a column
(`page/opt6.jsonl`): the lightest shapes that feed all 12 scenarios are 45° with a 0.90 peak (1.7 W), then 50° with
a 1.00 peak (2.3 W). At 45° the peak it needs is too low: the ball sits on the peak when the robot stops 0.35 in short
(10/12), and a 0.90 peak leaves no room for the shaft (`page/v6_pick.js` models a hex standing proud of the ramp as
a step: it costs the feed). A first v6 (19 Oct, 50°, peak 1.00, 8° tail, end bosses carrying the shaft, its top flat
flush through the ramp) was 12/12 but clunky; once the shaft was found to be 7 mm across the flats and the twist
explained, **v6 became v5's body with only the nose changed: 50°, edge 0.60, peak 1.10, tail 12°**, no bosses, the
bore buried 2.7 mm. It is 10/12 in the model: the two misses are the robot creeping in at 3 in/s and stopping 0.35 in
short. The lane begins just behind the peak at the centre and 1.2 in further back at the edges (a half-oval front,
Carter's second ask), one curve edge to edge, 0.3 in at the edges. `blade6.py` builds the blade as a height field
(every feature a formula) and cuts only the bore. Dean's plate is `print_job.py v6` → `stl/print_job_dean_v6.stl`
(199 × 157 mm): v6 lane and v5 lane side by side, both bored for the REX, and the 7.0 to 7.4 coupon, so the stick
test compares the two noses held firm.

**22 Oct stick test: the 50° noses do not work; v5 does.** The three parts printed from the 19 Oct plate (the first
v6 with end bosses, its plain twin, the coupon) did not feed: the ball sits on the peak. The team's reading is that the
peak is too far back for the shallower nose, which is the margin arithmetic (the peak sits (peak − edge)/tan(nose)
behind the lip: 0.29 in for v5, 0.34 for the bossed v6, 0.42 for the 20 Oct v6), with the stop in practice evidently
shallower than the CAD's lobe points. A second suspect on the bossed v6 is the shaft's top flat opened flush through
the ramp right behind the peak: a flat shelf there is what the model had already shown stops the ball. The v5 blades
(19 Oct) emptied the FLOWER even while spinning on the shaft, so the next plate is v5 lane and v5 plain with the bore
fixed (7.15 mm across the flats). Also found while measuring the bores: trimesh's 6-gon starts with a corner on the
axis, so every bore to date was corners up and down; the scripts now turn the hex so `--hex-angle 0` is flats top and
bottom (2.2 mm of plastic over and under the bore on v5 instead of 1.1). Lesson for the search: the push force and
the margin pull against each other through the nose angle, and the field says the margin wins; a lighter push has to
come from the edge height, the face finish (a ball that slides on the face instead of rolling stalls at 60°), or a
lower peak, not from a shallower nose.

**v7 (23 Oct)** is one name for the blade that works: the v5 shape (60° nose from a 0.60 edge, 1.10 peak at 0.29
behind the lip, 12° tail), Carter's lane (full-width curve from 0.9 behind the lip), and the bore cut for the REX
(7.15 across the flats, flats top and bottom). `stl/v7.stl`, `stl/v7_lane.stl`, `step/v7_lane.step`, and Dean's
plate `print_job.py v7` → `stl/print_job_dean_v7.stl` (v7 lane, v7 plain, the 7.0 to 7.4 coupon). The v6 files
(both 50° designs) are removed; `blade6.py` stays as the height-field builder. The v5 files stay as the shape's
history. **Why the shaft is where it is:** the bore sits 0.53 in behind the lip, just behind the peak, because that
is the only place the blade is thick enough (14.7 mm at the peak, for a hex 7.15 across the flats and 8.26 across
the corners): the tail thins to a knife edge over the ring and the nose is a wedge. The nose load is close to the
shaft; the tail cantilevers behind it. A shaft further back needs the tail thicker there, which the ball's path
(above) and the ring (below) do not allow, or a different mount (bolted at the ends, where the blade can be thick).

**v8: the V nose (24 Oct).** On the stick, the v7 blades stop short: the ball is left balanced on the peak, pinned
against the post, and the lifted ball's top meets the collar's front bar (the design had zero headroom there: peak
1.10 + 2.8 = 3.90, the CAD's collar height). Dustin's suggestion, notches or a V so the blade drives further in. v8 is
v7 with a V-shaped lip that matches the uprights' lobes: the middle 1.7 in of the lip is 0.3 in deeper than the
sides, the slants from |y| 0.85 to 1.42 lie on the lobes' faces (which run from the points at |y| 1.42 to the inner
corners at |y| 0.61, 0.43 in deeper), and the outer lip still stops on the points. It seats on both lobes at once, so
it centres itself, and the centre's peak sits 0.84 in past the pinned POLLEN instead of 0.54. The whole mountain
follows the lip (`blade6.py --vee 0.3`: every section is the v7 profile measured from its own lip), so the peak
height, the push and the shaft are unchanged; the bore sits 0.75 behind the sides' lip with 1.4 mm of plastic over
it at the outer edges and 2.8 under. In the model (`page/v8_check.js`, the centre 0.3 deeper) it feeds all 16 cases
including a stop half an inch short, where v7 fails two. The V helps only if the lip on the posts is what stops the
sled; if the wheels hit the wall or the base plate first, that is the thing to fix. STEP: `step/v8_lane.step`,
`step/v8.step` (the surface is measured from the local lip, so both patches keep a rectangular domain; the fit is
within 4 % by volume because of the V's kinks).

**v9 and the hedge plate (24 Oct).** With the sled fully seated on the posts, the ball still sits on the peak, which
means it is not resting against the posts (that would put it 1.1 in past the peak) but is held an inch further
forward, where only the collar's front bar is. So the V nose (depth) and a lower peak (headroom) test two different
hypotheses, and the plate for Dean carries one of each: `v8_lane` (V nose, peak 1.10) and `v9_lane` (straight lip,
peak 1.00, 10° tail, shaft at 0.74 so 1.5 mm of plastic stays over the bore; a V on a 1.00 peak leaves under 1 mm
at the outer edges, so v9 has no V). Both feed all 16 model cases. `stl/print_job_dean_hedge.stl`. The collar's
height above the tile, 3.90 in the drawing, is still the number that decides the design; a to-spec collar gives
v8 zero headroom and v9 0.1 in.

**The three-blade plate (24 Oct)** is the print for the next test: `print_job.py three` → `stl/print_job_three.stl`
(168 × 219 mm): v7 lane (the baseline with the snug hole), v8 lane (the V), v9 lane (the lower peak), each one change
from v7. The 7.15 mm bore fit the REX perfectly on the first print with it, so the fit is settled and the coupon retired. (The V was first built inverted, the middle recessed instead of protruding; caught from the page's viewer
and fixed the same day.)

**The stack that locks itself (23 Oct).** Side by side, the photos of the stuck stacks say: balls staggered side to side
come out one after another; balls staggered front to back leave the bottom ball on the peak, with the sled pushed in as
far as it goes. The model had every stack coming out, so the FLOWER's section was checked again: the collar's front bar
(underside 3.90, top 4.25, inner edge at x -1.69, the opening 3.6 in across) had been drawn as a wall. As a bar
(`FIXED[6..8]` in `page/sim.js`, `cfg.collar` to move it) the model locks the same way the photos do: the second ball
leans toward the robot until it rests on the bar; when the blade lifts the bottom ball onto the peak the second ball
rides up onto the bar's corner and bridges the gap, on the bar on one side and the bottom ball on the other; the third
lies against the front pipes. Three balls make an arch, and the bottom ball cannot leave the peak toward the robot
without lifting the one above, which the bar and the pipes hold. End state: ball 1 at (0.35, 2.49), ball 2 at
(-0.58, 5.11) on the bar's corner, ball 3 at -0.86 (the pipes), the same positions as the photos
(`stuck_state.png`). A side-to-side lean never locks: the upper balls lean into the gaps between the pipes, where there
is no bar. Two more things it says: the ball sits *on* the peak only if the peak is under it, and with the lip at the
posts' points v7's peak is 0.54 in past the pinned ball, so in the photos the sled stopped about 0.35 in short of
where the drawing puts the points (something touches first); and the lock is timing, a fast push (8 in/s and up) gets
the bottom ball over before the arch forms. On the field it locks most of the time, in the model some of the time
(it is sensitive to speed and friction), so the model still misses something about how the stack settles.

What feeds, over 16 runs each (a straight stack and one settled front to back `[-0.29, 0.35, 0.35]`, μ 0.3/0.45,
3 and 6 in/s, the sled at the points and 0.35 short; `page/fix_scan.js`, `page/scan_stuck.js`, `page/fb_pick.js`):
v7 10, v8 (V, 0.3 deeper) 15, v9 (peak 1.0) 13, v7 reaching 0.6 deeper 16, peak 0.9 16, nose 65° 7, nose 55° 12,
tilt 6° nose-up after the stop 11, collar 0.3 higher 16, collar 0.3 lower 6. So it is the collar that makes the lock,
and reach past the ball (or a lower peak) is what we can do about it. The page's simulator now starts with v7, the
stack leaning front to back (the stack buttons: front to back / straight / side to side) and the stop 0.35 short,
which locks; v8 feeds it, v9 locks. If the three-blade test agrees, the next blade is a 0.6 in V or a 0.9 in peak.
The ring's hold on the bottom ball is now geometric (`play` in `sim.js`: the hole's radius minus the ball's
cross-section at the ring's height), which removed a false sideways escape. The 3-D viewer draws the V nose
(`drive3d.js`, `vee`), and the plan view too.

**The mentor's corner disc.** The servo-driven 4.5 in disc at the front right is for POLLEN in the wall corner by the
garden. It is a flat disc facing forward, spinning about a fore-aft axis, so its face sweeps a ball sideways out of
the corner toward the intake: 0.5 in thick, 1.2 in ahead of the face, 4.0 to 8.4 in off centre, 2.5 to 6.9 in high.
It clears the FLOWER only while the robot is within about 1 in of centred on it.

**The sled arms pivot outboard of the drive wheels**, 8.3 in off centre: the chassis is 16.0 in wide and the 18 in box
allows 9, so there is room past the front omni wheels (which end at 7.1). The blade's shaft runs 17 in across under
the Gecko row (0.77 in high; the wheels' bottoms are at 1.94). On the right the arm passes under the corner disc:
where the disc sits, 0.7 to 1.2 in ahead of the face, the arm is 1.8 to 2.0 in high and the disc's bottom is 2.5.

**Test it the same way:** 5 straight pushes, then 1 in off-centre, with each blade, counting POLLEN out and timing from the side. The
thing to watch is whether it seats with one steady push. If it still needs a wiggle, tape the nose first; if that
fixes it, the friction is higher than 0.4 and the next print gets a 50° nose.

## Files

- `build.py`: the module, in his robot's frame. It writes `flower-stick-proto.step` (insert at the origin of his
  Onshape assembly) and the block STLs. `BLOCK=060|065|070` and `REACH=0|1` pick the variant.
- `divot_blade.py`: the divot blade v2 (16 Oct): wedge nose, low lane, tall shoulders; every size a flag. `--gutter-*` adds Carter's lane on the tail (`v5_lane`).
- `stack_sequence.py`: the 15 Oct concept frame by frame (`stack_sequence.png`). `profiles_compare.py`: Block A, v2 and v3 at their stops with the pinned POLLEN. `intake_section.py`: v3 feeding the intake, from `tools/ramp-hook/intake_sim.py`'s trace (v3 profile, the Gecko as in the 16 Oct CAD).
- `seated_in_field_cad.png`: the blade seated in FIRST's field CAD FLOWER.
- `blade_step.py`: the same blades as STEP solids for review in Onshape (`step/`: v6_lane, v5_lane, the coupon); the tail with the lane is a B-spline surface through the height field, within 0.02 mm of the printed mesh.
- `blade6.py`: the v6 blade as a height field: nose, peak, tail, the lane with its oval front, the end bosses, the bore (opened flush where the ramp is thinner than the hex).
- `print_job.py three|v8|hedge|v7|v5`: the print plate, `stl/print_job_<job>.stl`, and its top view (older plates keep their `print_job_dean_` names).
- `hex_coupon.py`: a hex-fit coupon (`stl/hex_fit_coupon.stl`), three 8 mm hex holes at 8.1/8.2/8.3 mm across the flats, printed like the blades, to find the hole size that is snug on a given printer. The 15 Oct blocks turned on the shaft; their hole is 8.3 and their hex bearing length only 36 mm. The 137 mm blade bears on 3.8 times the length, and the fitted size goes in with `divot_blade.py --hex`.
- `block.py`: the block's STL for any shaft height (`--shaft`) or wheel size (`--wheel-mm`).
- `proto.png`: the picture above, drawn from his Robot.step.
- `block_template.pdf` (`template.py`): a 1:1 paper template for cutting the block from 3/4 in plywood, hardwood or HDPE when there is no printer.
- To re-check: `python3 tools/robot-cad/module_check.py Robot.step flower_stick_seated`, run from a scratch folder.
