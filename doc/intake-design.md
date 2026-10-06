# The intake: what the simulator says to build first

For the build team's mentor, 6 Oct 2026 (issue #160, branch `spike/160-intake-design` off `claude/simulator`).
Simulated only: no robot was attached and nothing here was measured on one. Every number says where it comes
from; the ones marked **guess** are the ones the cardboard tests below replace.

**The short version**

1. **The roller's bite on a POLLEN is marginal, and it decides 15 points.** Two reads of the STEP put the
   roller's bottom at 2.84 in (bounding boxes, [cad-6-oct.md](cad-6-oct.md)) and **2.53 in** (from the floor the
   wheel covers and the CAD's resting NECTARs share, [robot-cad.md](robot-cad.md); the better reference). A
   POLLEN is 2.8 in tall: at 2.84 the roller never touches it and the two Autos that live on POLLEN lose about 15
   points; at 2.53 it bites 0.27 in, which the simulator takes and a real wheel may not. Lower the roller (or
   use bigger wheels) before anything else: a bite of 0.4 in is the guess used below, a roller bottom at
   **2.4 in**, and the cardboard rig finds the real number.
2. **Once the roller bites, the mouth width and the vectored rollers are the only intake choices that score,
   and only on the Auto that sweeps spills.** On ShootsRight, 14 in against the CAD's 9.4 in is 53.0 against
   51.4 and TIP 3 in 5 runs of 60 against 2; vectored rollers on the 14 in mouth make it 55.1 and TIP 3 in 10,
   where the simulated Flat Intake already was. With the Rigid V's flaps feeding the mouth, the vectored 14 in
   intake is the best robot this simulator has produced: **67.0 points, TIP 3 in 40 of 60** (the Rigid V on
   the Flat Intake: 64.1 and 33). On the two Stages Autos every option is within a point of every other.
3. **Two things the README blamed on the intake are not the intake.** The "busy" misses (16–25 a run) were
   mostly pieces waiting their turn in a FLOWER; the throat itself is busy 0–1 times a run on ShootsRight and
   9–12 on the Stages Autos, and queuing those changes the Stages Autos by 0.3 points. The "too high" misses
   (14–17 a run) are pieces passing the front with their top above 8 in: a spill still in the air where the
   robot waits for it, which no intake may catch (G409). Neither a taller bite window nor a faster throat
   (0.25 to 0.7 s a ball, no change) is worth building for.
4. **Build in cardboard first:** the roller height test (which bottom height lifts a POLLEN and a NECTAR off the
   tiles, and how fast), the mouth-edge test (is a ball at the edge of a 14 in mouth pulled in or batted
   aside), and only then the vectored rollers (does a ball held against them wait, or squirt out sideways).

## The four intake numbers the simulator uses

The simulator's robot (`RobotDesign`, test code under `TeamCode/src/test/.../logging/`) now describes an intake
with these, so a design question becomes a run:

| Parameter | What it means in the simulation | The CAD as drawn | Source |
|---|---|---|---|
| Mouth width (`intakeWidthIn`) | A piece touching the front face is taken only within this width, centred; outside it, "beside" | **9.4 in** between the side plates | STEP, bounding boxes, about 0.1 in |
| Roller bottom (`rollerBottomIn`) | A piece is bitten only if its top is above this; a piece on the tiles that is shorter is "low" | **2.53 in** above the tiles (the 2.84 in read in cad-6-oct.md used a higher floor reference) | STEP, measured from the floor the mecanum covers and resting NECTARs share ([robot-cad.md](robot-cad.md)) |
| Roller diameter (`rollerDiameterIn`) | A piece whose centre is above the roller's axle (bottom + half the diameter) is pushed away, "height" | **1.89 in** (48 mm gecko wheels) | STEP |
| Time per piece (`intakeIntervalS`) | One piece into the robot per this many seconds; the next one arriving sooner is "interval" | **0.35 s** | **Guess** (the Flat Intake's placeholder: 4 pieces in about 1 s) |
| Holds at the mouth (`intakeHoldsAtMouth`) | Vectored rollers: a piece touching the mouth while the throat is busy is held against the rollers and fed through one per interval, instead of bouncing off; held pieces count toward G407's 4 | no | The mentor's 6 Oct idea; the hold is as sure as the plain intake's grab (85%, **guess**) |

The rest of the intake model is unchanged from the README: a piece is taken only when it touches the front,
85% of the time, not above 60 in/s relative to the robot, both guesses. The launcher is the CAD's exit (10 in up,
3 in behind the centre, STEP) at the Flat Intake's guessed 75° and 0.45 s a shot.

**Where the pieces are.** POLLEN 2.8 in across, 25 g; NECTAR 3.6 in, 41 g (the game manual). On the tiles a
POLLEN's centre is 1.4 in up and its top 2.8; a NECTAR's 1.8 and 3.6.

## The geometry that matters

**Roller height against the two pieces.** A single horizontal roller bites a ball when the roller's bottom is
below the ball's top (else it never touches) and the roller's axle is above the ball's centre (else it pushes
the ball away instead of pulling it under). With the CAD's 48 mm wheels (0.945 in radius):

| Roller bottom | POLLEN on the tiles (top 2.8, centre 1.4) | NECTAR on the tiles (top 3.6, centre 1.8) | Highest POLLEN centre it still pulls in |
|---|---|---|---|
| 2.84 in (the bounding-box read) | **not touched** | bitten 0.76 in | 3.8 in |
| 2.53 in (as drawn, the floor-referenced read) | bitten 0.27 in, marginal | bitten 1.07 in | 3.5 in |
| 2.4 in (**guess**, used below) | bitten 0.4 in | bitten 1.2 in | 3.35 in |
| 2.0 in | bitten 0.8 in | bitten 1.6 in | 2.95 in |

So with small wheels the window is narrow: low enough to squeeze a POLLEN, and a bouncing POLLEN whose centre is
more than 3.35 in up (its top above 4.75 in) is pushed away. Bigger wheels widen the window at both ends: a
4 in roller with its bottom at 2.4 in has its axle 4.4 in up and takes a POLLEN with its top anywhere up to
5.8 in; two rollers one above the other do the same. The simulation says the wider window is worth nothing in
these Autos (the "height" section below): the pieces it misses are far higher than any roller. Choose the wheel
for grip and for the bite, not for the window.

**The squeeze itself is a guess.** Nothing says 0.4 in is enough for a gecko wheel to pull a 25 g foam ball
across a tile; it depends on the wheel's grip, the roller's speed and what the ball rolls against on the far
side. That is the first cardboard test.

**Mouth width.** The CAD's 9.4 in is the gap between its side plates. The 15.2 in body could carry a 14 in
roller with 0.6 in plates each side, which is what "14 in roller" below means. Wider than the body needs flaps
(the Rigid V, out to 18 in: [shape-matrix.md](shape-matrix.md)); R105 allows 24 in across once the match starts.

**Throat width for vectored rollers.** One ball at a time: a NECTAR (3.6 in) plus clearance, about 4–4.5 in,
and the ball path behind it the same. A throat narrower than that jams on NECTAR; wider than about 5.5 in lets
two POLLEN (2.8 in each) wedge side by side. Those are geometry, not simulated: the simulation only says "one
piece per interval" through the throat.

**Time per ball.** Unmeasured everywhere. Below it is swept from 0.25 to 0.7 s on the vectored 14 in design to
see how much it matters; the honest number comes from the cardboard rig with a phone at 240 fps.

**The front corners are shared with the ramp hook.** From the CAD session's measurements
([robot-cad.md](robot-cad.md) on `claude/robotics-meeting-notes-lq2y55`): the add-on hook as drawn today puts
its hinge, hub and servo in front of the **right front wheel**, 0.8 in out and 2.2 in up, and stows upright in
front of that corner, 2.4 in deep and up to about 9 in high. That is exactly where a 14 in roller's right end, a
right-side vectoring wheel, or the Rigid V's right flap goes. So the intake and the hook are one design, with
three ways to share the corner:

- **Hook outboard of a 9.4–12 in mouth.** Keeps today's hook; gives up the 14 in mouth's 2 points on ShootsRight
  and the V's right flap. Cheapest.
- **Hook hinge above the roller, on the side plate.** The roller runs full width underneath; the hook swings down
  in front of the roller's right end and stows above it. Needs the hinge at about 5 in up instead of 2.2.
- **Hook as the right flap.** The Rigid V's right flap and the hook are the same plate: rigid out at 45° for the
  V, driven down as the ramp. The V's gain (flaps feeding the mouth) and the hook's are then one mechanism, which
  is the combination the simulator scores best (67.0 with vectored 14 in). The most work.

**The FLOWER block belongs on the intake.** What it needs, however it is mounted (same source): bottom 0.7 in
off the tiles, top 1.35 in, about 1.4 in deep with a curved front, driven against the FLOWER's grey uprights,
and a clear lane behind it for 4 POLLEN to roll to the roller. Team 19705 mounts the block on the intake's own
mouth with almost no lane and lets the roller pull the POLLEN out. That fits a 14 in roller at 2.4 in better
than a 9.4 in mouth: the block sits under the roller's centre and the POLLEN come out straight into the bite.
The simulator doesn't model the block; it takes FLOWER POLLEN with the intake at one per 0.5 s (a guess), so a
block that empties a FLOWER faster is worth measuring on the cardboard rig too: 19705's reel shows 4 in about 1 s.

**Rules.** At most 4 pieces controlled (G407): a ball held against the rollers counts, so the mouth may hold
only 4 minus what is inside. Never touch a falling spilled piece (G409); the counts below say in how many
runs of 60 any part of the robot did.

## What each option buys in the simulator

The three qualifier Autos, 60 runs each (seeds 1–60), the same partners, runner and settings as
[shape-matrix.md](shape-matrix.md) (`AutoStudyTest`, `BIOBUZZ_AUTO_PARTNER_DESIGN="spring hood"`, speed 40,
friction 1). Run 6 Oct 2026 15:38 UTC on this branch, the as-drawn rows again at 16:50. **Every option runs on
the Flat Intake's 14.5 in body**, which the Autos are drawn for; only the intake and launcher are the CAD's. The
CAD's own body (15.12 × 15.24 in, [robot-cad.md](robot-cad.md); the STEP's 15.7 in box includes a NECTAR
poking out the front) was checked with the vectored 14 in intake: 55.3 / 50.5 / 47.3 on the three Autos, the
same as on the 14.5 in body, but every run logs "drives into a FLOWER" because the FLOWER and GARDEN spots are
fitted to a 14.5 in front (`qual_right.fit` moves them 0.3 in for this body; a route job, not an intake one).
The "as drawn" rows below use the roller as the 2.84 in read has it, because that is the reading that changes
the answer; at the 2.53 in read the simulator's bite-or-not rule makes "as drawn" identical to the 2.4 in row
(rerun: 51.4 / 49.5 / 46.7 plain, 60.2 / 49.8 / 38.3 with the V). The published baselines were not rerun; the
Flat Intake's row is quoted from the README.

Points are alliance AUTO points; TIP 3 / TIP 2 are runs of 60 that made that TIP; G409 is runs of 60 with a
touch. Misses are per run, our robot: **low** a POLLEN under the roller; **height** a piece bouncing with its
centre above the axle; **beside** at the front face outside the mouth; **interval** the throat busy;
**flower** waiting in a FLOWER (not the intake's fault); **full** 4 held.

### Qual-PartnerShootsRight (partner fires its 4 and parks)

| Intake | Points | TIP 3 | PARK | G409 | low | height | beside | interval | flower |
|---|---|---|---|---|---|---|---|---|---|
| Flat Intake (README baseline: 14 in, 5 in tall, 0.35 s) | 54.8 | 10 | 13 | 0 | — | 15 | — | 16–25 incl. FLOWER | — |
| **CAD if the roller does not bite** (9.4 in, roller at the 2.84 in read; design "roller at 2.84 in") | 50.1 | 0 | 1 | 0 | 3.7 | 13.6 | 5.5 | 0.0 | 19.9 |
| CAD, roller at 2.4 in (= as drawn at the 2.53 in read) | 51.4 | 2 | 5 | 0 | 0 | 14.5 | 4.6 | 0.6 | 16.1 |
| CAD, 14 in roller | 53.0 | 5 | 12 | 0 | 0 | 15.4 | 1.9 | 1.1 | 16.1 |
| CAD, vectored 9.4 in | 52.1 | 3 | 9 | 0 | 0 | 14.6 | 4.6 | 0 (held) | 16.1 |
| **CAD, vectored 14 in** | **55.1** | **10** | 17 | 0 | 0 | 15.7 | 1.7 | 0 (held) | 16.1 |

### Qual-PartnerStages, angled partner (can't shoot)

| Intake | Points | TIP 2 | PARK | G409 | low | height | beside | interval | flower |
|---|---|---|---|---|---|---|---|---|---|
| Flat Intake (README baseline) | 51.6 | 53 | 55 | 0 | — | 15 | — | 16–25 incl. FLOWER | — |
| **CAD if the roller does not bite** | **34.2** | 2 | 54 | 0 | 19.4 | 11.3 | 10.9 | 0.2 | 10.0 |
| CAD, roller at 2.4 in (= 2.53 in read) | 49.5 | 48 | 54 | 1 | 0 | 15.5 | 7.0 | 9.1 | 7.1 |
| CAD, 14 in roller | 50.2 | 50 | 54 | 2 | 0 | 16.8 | 3.2 | 10.0 | 6.9 |
| CAD, vectored 9.4 in | 50.1 | 50 | 53 | 0 | 0 | 15.7 | 6.6 | 0 (held) | 6.9 |
| CAD, vectored 14 in | 50.5 | 51 | 54 | 1 | 0 | 17.2 | 2.9 | 0 (held) | 6.4 |

### Qual-PartnerStages, partner against the wall (can't shoot)

| Intake | Points | TIP 2 | G409 | low | height | beside | interval | flower |
|---|---|---|---|---|---|---|---|---|
| Flat Intake (README baseline) | 47.3 | 49 | 0 | — | 15 | — | 16–25 incl. FLOWER | — |
| **CAD if the roller does not bite** | **31.0** | 0 | 0 | 38.3 | 9.4 | 17.4 | 0.0 | 10.0 |
| CAD, roller at 2.4 in (= 2.53 in read) | 46.7 | 47 | 1 | 0 | 13.9 | 13.9 | 8.8 | 8.2 |
| CAD, 14 in roller | 47.0 | 48 | 1 | 0 | 14.9 | 6.5 | 12.5 | 8.2 |
| CAD, vectored 9.4 in | 47.0 | 48 | 1 | 0 | 14.1 | 12.7 | 0 (held) | 8.2 |
| CAD, vectored 14 in | 47.3 | 49 | 1 | 0 | 15.1 | 6.0 | 0 (held) | 8.1 |

### With the Rigid V on each

The Rigid V's own Autos (`qual-right-o3-rigid-v`, which keeps the sweep through the spills, and the two
`qual-stages-*-rigid-v`), fixed flaps from the front corners out to 18 in across, as in
[shape-matrix.md](shape-matrix.md). Its wall-partner route is the known broken one (the robots collide at
10.7 s in every run; the V doesn't fit the west lane), so that column compares the intakes with each other only.

| Intake, with the Rigid V | ShootsRight: points · TIP 3 · G409 | Angled: points · TIP 2 · G409 | Wall (broken route): points · TIP 2 |
|---|---|---|---|
| Flat Intake, Rigid V (README baseline) | 64.1 · 33 · 3 | 51.9 · 54 · 2 | no clean route |
| CAD if the roller does not bite (2.84 in) | 50.0 · 0 · 1 | 36.2 · 8 · 3 | 31.7 · 8 |
| CAD, roller at 2.4 in (as drawn at 2.53: 60.2 · 24 · 2) | 60.6 · 25 · 2 | 49.8 · 49 · 5 | 38.3 · 28 |
| CAD, 14 in roller | 64.1 · 33 · 3 | 50.5 · 51 · 6 | 41.7 · 38 |
| CAD, vectored 9.4 in | 62.8 · 30 · 2 | 49.8 · 49 · 5 | 39.0 · 30 |
| **CAD, vectored 14 in** | **67.0 · 40 · 5** | 50.8 · 52 · 5 | 42.7 · 41 |

Misses per run on ShootsRight with the V: "beside" drops to 0.5–0.6 with the 14 in mouth (the flaps feed the
mouth), "height" stays 16–17, and the throat is busy 7–11 times a run on the plain rollers, which the vectored
ones turn into held pieces: that is the 3 points and 7 TIP 3s between 64.1 and 67.0. With the V the vectored
14 in mouth also fills: "full" 1.6 a run, pieces turned away because 4 were already controlled (G407).

### Time per ball, on the vectored 14 in intake

`intakeIntervalS` swept on "CAD, vectored 14 in" (no V), the other numbers as above:

| Time per ball | ShootsRight: points · TIP 3 | Angled: points · TIP 2 | Wall: points · TIP 2 |
|---|---|---|---|
| 0.25 s | 55.1 · 10 | 50.7 · 51 | 47.3 · 49 |
| 0.35 s (the placeholder) | 55.1 · 10 | 50.5 · 51 | 47.3 · 49 |
| 0.50 s | 55.1 · 10 | 50.5 · 51 | 47.3 · 49 |
| 0.70 s | 55.1 · 10 | 50.5 · 51 | 47.3 · 49 |

Nothing: ShootsRight is identical to the decimal at every setting, the Stages Autos within 0.2 points. Once a
piece is held rather than lost, the throat's speed is hidden behind the drive time to the firing spot and the
TIP. So the time per ball is worth measuring for the TELEOP cycle, not for these Autos, and a slower, surer
throat costs nothing here.

### What the "height" misses are worth

Two bounds on the 14 in designs (no V): a 4 in roller at the same 2.4 in (axle 4.4 in up: a POLLEN is bitten
with its top up to 5.8 in), and a mouth that takes anything touching the front with its top up to 8 in (an
upper bound on any roller stack, not a design).

| Intake | ShootsRight: points · TIP 3 · height | Angled: points · TIP 2 · height | Wall: points · TIP 2 · height |
|---|---|---|---|
| 14 in roller (48 mm, from above) | 53.0 · 5 · 15.4 | 50.2 · 50 · 16.8 | 47.0 · 48 · 14.9 |
| 14 in roller, 4 in roller | 52.7 · 4 · 15.2 | 50.2 · 50 · 15.8 | 47.3 · 49 · 14.5 |
| 14 in roller, 8 in mouth | 52.5 · 4 · 13.8 | 50.5 · 51 · 14.1 | 47.3 · 49 · 13.6 |
| vectored 14 in (from above) | 55.1 · 10 · 15.7 | 50.5 · 51 · 17.2 | 47.3 · 49 · 15.1 |
| vectored 14 in, 4 in roller | 55.4 · 11 · 15.5 | 50.2 · 50 · 15.9 | 47.3 · 49 · 14.6 |
| vectored 14 in, 8 in mouth | 54.0 · 7 · 14.1 | 50.2 · 50 · 14.4 | 47.3 · 49 · 13.6 |

Nothing again: an 8 in mouth still logs 14 "height" misses a run, so those pieces pass the front with their
top above 8 in. They are spill pieces still in the air or on their first bounce, right where the robot waits
for a TIP (the Autos stand 35 in from the HIVE wall, where the spill lands), and a mouth that caught them would
be a G409 touch. **The "height" count is not a design lever; ignore it.** The small roller's narrow window
costs nothing measurable on the tiles.

**Reading it**

- **The roller height is the whole story for the two Stages Autos.** With a roller that does not bite a POLLEN,
  their row of 4 POLLEN and the far FLOWER's POLLEN are all "low": TIP 2 in 0–2 runs of 60 instead of 47–51,
  15 points gone. With one that does, every option is within a point of the Flat Intake. Nothing else in this
  study moves those two Autos, and the 0.27 in bite the CAD has at 2.53 in is the margin between the two.
- **On ShootsRight the mouth width buys TIP 3** (TIP 1's NECTAR and POLLEN scattered along the wall, and the
  GARDEN): 9.4 to 14 in is 2 points and 3 more TIP 3s; vectored rollers add 2 more points and 5 more TIP 3s,
  because the queued pieces arrive during the one sweep where the throat is actually busy.
- **The queue is not the win the README expected.** "interval" was overcounted: the FLOWER's pieces waiting their
  turn (one per 0.5 s through the retrieval opening) were logged as the intake being busy, and they are the
  same whatever the intake. The real busy count is 0–1 a run on ShootsRight and 9–12 on the Stages Autos, and
  turning those into held pieces changes the Stages Autos by 0.3 points: the row and the spill are still
  limited by 4 pieces, the route and the TIP, not the throat.
- **"height" is the same 14–17 a run on every option, including an 8 in mouth**: those pieces are not the
  intake's to take (see "What the height misses are worth").
- **With the Rigid V the intake choice matters most** (ShootsRight: 60.6, 64.1, 62.8, 67.0 for the four lowered
  options), because the flaps turn a sweep into a stream of pieces at the mouth, and that is where the queue
  earns its 3 points. The V's G409 touches (2–5 runs of 60) are the flaps', the same as the published V.
- **G409** stays at 0–2 runs of 60 on every plain option: the intake does not change where the robot waits.

## Build these in cardboard first, and measure

Needs: 4 POLLEN and 2 NECTAR (or the foam balls the team has), a few feet of field tile, the roller (the 48 mm
gecko wheels on their shaft, driven by any motor), a tape measure, scrap plywood or cardboard for the mouth and
side plates, and a phone at 240 fps on a stand looking along the roller.

**1. The roller height rig** (first; it decides whether the CAD works at all). The roller on two uprights with
slotted holes so its bottom can be set at 2.8, 2.6, 2.4, 2.2 and 2.0 in above the tile, a flat plate behind it
for the ball to run onto. Roll each piece at the roller at walking pace and push the rig onto a stationary one.
Measure:
- the lowest roller bottom at which a POLLEN on the tile is pulled under, and the same for a NECTAR; and the
  highest at which each still goes through without jamming (NECTAR squeezed 1.2 in may stall the motor);
- the time from first touch to the ball clear behind the roller, from the video, for each piece; this is
  `intakeIntervalS`, the guess every Auto in this repo uses as 0.35 s;
- what a POLLEN does when its centre is above the axle (toss one so it arrives bouncing): pulled in or batted
  away, and the height at which it changes. That is the "height" rule above.
Try the 48 mm wheels and then anything larger the team has (a 4 in compliant wheel), at the same bottom height.

**2. The mouth edge** (decides between 9.4 and 14 in, and whether flaps are needed). The same rig with side plates
at 9.4 in and at 14 in apart. Roll a POLLEN and a NECTAR at the front with the ball's centre 1 in inside the
plate, on the plate's edge, and 1 in outside it. Measure where the edge is in practice: the last position that
is pulled in, and whether a ball on the edge is deflected inward (good: the simulator's Rigid V gain is this
bounce) or outward.

**2b. The FLOWER block on the mouth** (with 2, same rig). Tape the block's profile (0.7 to 1.35 in up, 1.4 in
deep, curved front) under the roller's centre and drive the rig at a FLOWER, or a tube of the same inside
diameter with 4 POLLEN in it. Measure: whether the bottom POLLEN comes out under the block into the bite, the
time for all 4, and whether the block needs a lane behind it or the roller alone does the pulling.

**3. Vectored rollers** (only once 1 and 2 are known). Two short angled rollers, or a pair of gecko wheels on
angled axles, each side of a 4.5 in throat on the 14 in mouth. Hold the rig still and feed two POLLEN at once,
then a POLLEN and a NECTAR, then three. Measure: whether the second ball is held against the rollers while the
first goes through (the whole premise of `intakeHoldsAtMouth`) or squirts out the side; the time between the
first and second ball clearing the throat (the interval the queue runs at); how many wait before one escapes
(G407 needs the answer to be "never more than 4 minus what is inside").

**What to bring back:** five numbers, each with the piece it was measured on. Roller bottom height that bites,
time per ball through the roller, the mouth's effective edge, whether an edge ball deflects in or out, and
the queue's time per ball. They go into `RobotDesign.dhsCad()` as measured values and the matrix above is rerun
with `python3 tools/auto-routes/shape_matrix.py 60 intakes` (about 10 minutes; the designs are
`AutoStudyTest.intakeOptions`).
