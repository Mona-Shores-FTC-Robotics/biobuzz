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
   where the simulated Flat Intake already was. With the Rigid V's plates feeding the mouth it is worth more:
   the drawn intake and V score **69.9 points, TIP 3 in 47 of 60** on ShootsRight (the Rigid V on the Flat
   Intake: 64.1 and 33), because the plates' tips reach past the roller's front; vectored rollers on the old V
   were 67.0. On the two Stages Autos every option is within a point of every other.
3. **Two things the README blamed on the intake are not the intake.** The "busy" misses (16–25 a run) were
   mostly pieces waiting their turn in a FLOWER; the throat itself is busy 0–1 times a run on ShootsRight and
   9–12 on the Stages Autos, and queuing those changes the Stages Autos by 0.3 points. The "too high" misses
   (14–17 a run) are pieces passing the front with their top above 8 in: a spill still in the air where the
   robot waits for it, which no intake may catch (G409). Neither a taller bite window nor a faster throat
   (0.25 to 0.7 s a ball, no change) is worth building for.
4. **The design, drawn and fit-checked** (section below; `cad/intake-b/` on `claude/robotics-meeting-notes-lq2y55`):
   a 13.8 in roller of 48 mm gecko wheels 1.0 in ahead of the front face with its bottom at 2.4 in, carried by the
   outer wheel plates, the ramp hook hinged 6 in up on those plates and stowed folded over the top, and the Rigid V
   as two fixed corner plates. 17.3 in long at the start, 24.0 with the hook down, 17.8 wide. **Since 21:15 UTC the hook is
   no longer a spill catcher** (the mentor; [unified-design.md](unified-design.md)): the roller, plates, drive and V
   plates stand; the hook becomes the FLOWER extractor, designed in its own chat around the V.
5. **Build in cardboard first:** the roller height test (which bottom height lifts a POLLEN and a NECTAR off the
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

## The design to model: a full-width roller ahead of the face, with the Rigid V

> **Decision (mentor, 6 Oct 2026, 21:15 UTC; [unified-design.md](unified-design.md) on
> `claude/biobuzz-robot-body-designs-hi386c`): one robot, a fixed Rigid V, a FLOWER extractor and a FLOWER scorer.
> The hook is no longer a spill catcher.** What stands from the drawing below: the roller, its side plates and drive,
> and the V plates; **the V takes the front corners**, and the extractor and scorer fit around it or fold. The hook
> rows below are the record of what was drawn and checked; the hook itself becomes the FLOWER extractor (the
> Flower Extracter chat: two low arms, no walls, stowed inside the 18 in cube with the V fitted), so its hinge,
> servo and hard stops are that chat's to keep or drop, and the FLOWER block goes with it. This chat owns the V's
> angle, length, height and shape, and the intake behind it ("The V's angle" below).

This is the intake design this study recommends. **It is drawn:** `cad/intake-b/` on
`claude/robotics-meeting-notes-lq2y55` (`dhs-intake-b.step` in the robot's frame, `build.py` with every number at
the top, the printed parts as STL, and a README with the fit table), swept against the robot CAD every 5° of the
hook's fold. Decided 6 Oct 2026 (the user): 48 mm gecko wheels, and the hook is in stage 1. The numbers below
are the drawn ones; where the drawing moved from the first spec it says so. The simulator numbers behind it: the 14 in roller with the Rigid V scores 64.1
(the Flat Intake's V exactly), the Ramp Hook on the Flat Intake 66.3 ([shape-matrix.md](shape-matrix.md)), and
vectored rollers add 3 points on top of the V. Positions are in the robot's frame, inches: **x ahead of the
front face** (the front end of the side rails), **y to the robot's left** of the centre line (midway between the
rails), **z up from the tiles**. In the STEP (mm; x across with the right at −x, y up, z forward):
STEP x = −59.62 − 25.4·y, STEP y = −151.75 + 25.4·z, STEP z = 207.73 + 25.4·x.

**Why the roller moves ahead of the face** (checked): at today's roller line a 14 in roller runs into both front
uprights, both front wheels and the intake's plates. The front wheels are flush with the front face and outboard
of the rails (9.76 in apart), which is why the CAD's mouth is 9.4 in. With 48 mm wheels and its bottom at 2.4 in
the roller clears everything once its axle is **1.0 in ahead of the face**, so its front is 1.94 in out; with the
hook stowed over the top the robot is 17.3 in long at the start (18 allowed).

| Part | Where | Size and parts | Why |
|---|---|---|---|
| Roller shaft | x = +1.0, z = 3.35 at rest, along y; 8 mm REX, about 400 mm; **floats** in vertical slots, up to 1.3 in (bottom 2.4 to 3.7; 0.85 is what a NECTAR needs), the motor on the same carriage, gravity and a light spring onto a down stop at 2.4 | centred on the centre line | 48 mm wheels' bottom at **2.40** (the guessed bite), front at x = 1.94. A fixed roller cannot pass a NECTAR ("The roller floats" below). Checked at rest; the float to be redrawn |
| Roller wheels | **13.8 in** of 48 mm gecko wheels, y = −6.9 to +6.9 (0.1 in less than 14 so the hook's hub clears the roller's end) | 48 mm (1.89 in); a bigger wheel's front would leave the 18 in start cube | The 14 in mouth; the bite the cardboard rig confirms |
| Side plates | the outer wheel plates from `cad/robot-addons/`, inner faces at y = ±7.56 (15.1 in between), extended forward past the roller and up to carry the hinge at 6 in | 1/8 in aluminium | One plate per side does the wheels' outer bearing, the roller's bearing and the hook's hinge. Checked |
| Roller drive | goBILDA 5203 motor **inboard over the roller on the left**, on a printed bracket on the left front upright; belt and pulleys **inside** the left plate, in the 16 mm between the roller's end and the plate | | Outside the plate the left V plate cut through the belt. The right end is the hook's |
| Hook hinge | x = +1.0, **z = 6.0**, axis across | on the side plates, 14 mm OD flanged bearings on an 8 mm REX stub as the add-on hook has | Checked every 5° from 0 to 150°: clear of the robot, roller, plates, motor, servo and V plates, the FLOWER block passing the roller at about 90°. At 5 in the front shaft hit the front towers and the block hit the launcher |
| Hook arm (right), curtains and ramp | arm at **y = −7.15** (room for its 14 mm hub between the servo and the plate); deployed, the block's back edge at x = 7.48; the ramp across the front; **both curtains end 4.7 in from the centre** (y ±1.52 to ±4.7) because at 4.8–5.3 out they swept through the tops of the two 14.3 in front towers at 125–135° | as `cad/robot-addons/` draws them, re-hung from the 6 in hinge; about 314 g, balance 7.3 in from the hinge | Deployed: **24.0 of R105's 24**. **Stowed: folded back over the top to 150°**, 2.22 in ahead of the face (past the roller's 1.94) and 14.5 in tall: **17.3 of the 18 in cube**. The right curtain leaves 2.45 in to the arm, too narrow for a POLLEN to get out |
| Servo | **inboard of the right plate, over the roller**, on a printed bracket on the right front upright's front-face holes | goBILDA 2000 Torque; worst-case load about 5.9 kg·cm at 32° of fold, of its 25 | Outside the plate the robot was 18.4 in wide with the V plates. Checked |
| The corner below 4 in | free on both sides | | Nothing of the hinge, hub or servo is below 5.5 in, so a vectoring wheel or a Rigid V flap still fits at each front corner. Checked |
| FLOWER block | on the ramp's front edge, as today (version A in [ramp-hook.md](ramp-hook.md)) | bottom 0.7, top 1.35, 1.4 deep, curved front | The lane from the block to the roller is **5.5 in**, not 7.4: POLLEN roll down the ramp and under the roller, which bites them at 2.4 (a POLLEN on the 0.1 in plate has its top at 2.9). 19705's block on the mouth is the alternative if cardboard test 2b shows the roller alone empties a FLOWER faster; its back edge would then be about 2.5 in ahead of the roller's front (one POLLEN of lane) |
| Rigid V plates (optional group) | fixed plates from each side plate's **front corner** (y = ±7.68, x = +1.4) out and forward to tips at **y = ±8.89, x = +2.84**, about 41° off straight ahead; z = 0.25 to 4.0 | 1/8 in aluminium, bolted to the side plates | Tips 17.8 in apart, 0.9 in ahead of the roller's front. Moved forward 6 Oct after the user's review: ending at x = 1.75 they were behind the roller's front (1.94) and steered almost nothing. **The tips at 2.84 put the robot at 17.96 in of the 18 in start cube: fixed plates can reach no further.** Clear of the front wheels, the belt drive and the stowed hook. Rigid, no hinge: the flap-as-hook idea (option c) is dropped, because the checked hook leaves the corner free and a hinged flap would not reach a FLOWER anyway. Wider or longer flaps would have to fold out after the start (R105: 18 × 24 once the match starts); what they are worth is in "Wider and longer V" below |
| Hook hard stops | a tab on the hub opposite the arm meets two printed blocks on the right plate, 38–50 mm from the hinge axis, each 1° past its end of travel: the down stop behind the hub (the FLOWER's shove and the hook's weight hold the tab on it), the stowed stop at 150° on an ear of the plate over the roller (the hook leans past vertical, so its weight holds it) | printed, bolted to the right plate | The servo only moves the hook between the stops and never holds against them. The side panel under the arm starts 2.9 in out (was 2.3) to clear the stowed stop. The arm and its socket sweep a different arc from the tab and cannot reach either block |
| Lane to the launcher | under the roller, on the centre line, as the CAD has it | 4.5 in wide at least (NECTAR 3.6 plus clearance) | Unchanged |

**What the simulator scores for this design** (60 runs, the hook's own Autos `qual-*-small-hook`, the hook as
the 8 in hook, design "DHS CAD intake, 14 in roller, ramp hook"): ShootsRight **65.2, TIP 3 in 32 of 60**, PARK 50,
G409 in 29 runs; angled 49.4, TIP 2 in 48, G409 in 40 runs; wall 47.0, TIP 2 in 48, G409 in 22 runs. That is the
published Ramp Hook on the Flat Intake (66.3 / 35 / 30 runs) within noise: the 14 in roller gives this design the
Flat Intake's mouth. The G409 touches are the hook's, the same problem as before: falling pieces land on the
deployed hook in a third to two-thirds of runs, and the hook's deploy timing or shape has to fix that before it
is legal to use in a spill. With the V instead of the hook: 64.1, TIP 3 in 33, G409 in 3 runs.

**The drawn intake in the simulator** (design "DHS intake-b": 13.8 in of wheels, the contact plane 1.0 in ahead of
the face, roller at 2.4; 60 runs, 6 Oct 2026 18:40 UTC): ShootsRight 54.6, TIP 3 in 11; angled 50.9, TIP 2 in 52;
wall 47.3, TIP 2 in 49. The same as the 14 in roller at the face within noise: the 1 in of reach alone changes
nothing.

**Wider and longer V** (the drawn V and five it cannot be without a hinge; the Rigid V's Autos; the flap in the
simulator runs from the frame's front corner to the drawn tip, which the side plate's own front edge makes true):

| V tips | ShootsRight: points · TIP 3 · G409 runs | Angled: points · TIP 2 · G409 | Wall (broken route): points · TIP 2 · G409 |
|---|---|---|---|
| Flat Intake's V, 18 in wide, 1.75 ahead (README baseline) | 64.1 · 33 · 3 | 51.9 · 54 · 2 | no clean route |
| **As drawn: 17.8 in wide, 2.84 in ahead** | **69.9 · 47 · 11** | 50.4 · 51 · 18 | 42.0 · 39 · 10 |
| 20 in wide, 2.84 ahead | 68.7 · 44 · 12 | 50.8 · 52 · 6 | 41.7 · 38 · 6 |
| 22 in wide, 2.84 ahead | 70.3 · 48 · 14 | 50.9 · 52 · 5 | 42.0 · 39 · 5 |
| 20 in wide, 3.5 ahead (hinged) | 69.1 · 45 · 16 | 50.9 · 51 · 14 | 42.0 · 39 · 12 |
| 22 in wide, 3.5 ahead (hinged) | 70.7 · 49 · 16 | 49.6 · 48 · 8 | 43.0 · 42 · 8 |
| 18 in wide, 5 in ahead (hinged) | 72.0 · 52 · **31** | 50.9 · 52 · **40** | 43.0 · 42 · **38** |

The drawn V is the best robot the simulator has scored on ShootsRight without a hook: **69.9 points, TIP 3 in 47
of 60**, 6 points over the Flat Intake's V, because its tips reach 0.9 in past the roller's front instead of
stopping behind it. Wider buys nothing (within a point either way); longer buys 2 points for three times the
G409 touches. **So no hinged V**: the fixed plates as drawn are the design. What the drawn V does cost is G409 in
11 runs of 60 (the Flat Intake's V: 3), the plates reaching the spill where the robot waits for TIP 3, and on
these Autos every run crosses the centre line by the width of the tips at 7.5–9.7 s (the routes were drawn for an
18 in outline; a 0.5 in route shift). Both belong with the hook's G409 work: the waiting spot and the timing.

**The roller floats** (decided 6 Oct 2026, 22:30 UTC, after the transfer chat's flag, #164). A NECTAR is 3.62 in
across and the pieces are stiff pickleball-type balls; a fixed roller whose bottom is 2.4 in up leaves a 2.4 in
gap, and nothing gives the 1.2 in a NECTAR would need to pass. The roller would jam on it, not pull it in. The
simulator's bite rule only checked the piece's centre against the axle, so it let NECTAR through; it now refuses a
piece bigger than the gap plus 0.4 in of give (`rollerFloatIn`, miss "gap"; the 0.4 in is the same guess as the
POLLEN bite). What that is worth, the drawn intake fixed against floating, 60 runs:

| Roller | ShootsRight: points · TIP 3 | with the V: points · TIP 3 · G409 | Angled, with V: points · TIP 2 | Wall (broken route), with V: points · TIP 2 |
|---|---|---|---|---|
| Fixed at 2.4 in (NECTAR refused: "gap" 0.7–8 a run) | 52.2 · 4 | 67.8 · 42 · 7 | 46.8 · 40 | 30.7 · 5 |
| **Floating, rises 1.3 in** (gap 3.7 + 0.4) | **54.6 · 11** | **69.9 · 47 · 11** | 50.4 · 51 | 42.0 · 39 |

Every roller row above this one was run before the gap rule and so took NECTAR, as the floating roller does;
the simulator's "DHS intake-b" now floats, and "DHS intake-b, fixed roller" is the refusing one.

So (geometry by the CAD session, 6 Oct 23:00 UTC, which found my first version wrong: an arm pivoting on the
motor shaft, straight above the axle, swings the roller sideways, and the roller cannot move back at all, its
rear being 0.06 in ahead of the front uprights): **a vertical float.** The roller's bearings ride in vertical
slots in the side plates at x = +1.0; the rise the rule needs is **0.85 in** (a 3.62 in NECTAR less 0.4 in of
give: the bottom from 2.4 to 3.22), and the slot is cut to **1.3 in** (bottom to 3.7) because the 0.4 in of give is
a guess and the extra slot covers a NECTAR that gives nothing. The simulator scores any float of 0.82 in or more
the same, and models 0.85. Gravity and a light spring hold the roller on a down stop at 2.4; the spring is the
POLLEN bite force, set on the rig. **The motor rides on the same carriage**, 77.5 mm above the roller: an outboard
link plate on the left holds both shafts' bearings, both pass through slots in the side plate, and the printed
motor bracket slides on the left upright's M4 column on shoulder screws (motor top at 8.1 to 8.5 in, under the
Limelight). **The FLOWER extractor gets its own fixed shaft**, 8 mm, full width in the side plates at x = +2.3,
z = 4.5: 0.4 in ahead of the roller's front and above a NECTAR entering under it, arms at y ±1.8, the same block
and seat (tip 5.84 in ahead of the face), the servo at the shaft's right end; the roller then has no gaps but the
transfer's pulley at y +2.6, whose belt span changes 0.25 in over the float and gets a sprung idler. The start
length stays 17.96 in (the shaft and hubs end at x = +2.6, inside the V tips' 2.84).

**Drawn and swept clear** (the CAD session, 6 Oct 2026 23:30 UTC; `cad/intake-b/` commit 70b2561 on
`claude/robotics-meeting-notes-lq2y55`, `tools/robot-cad/front_sweep.py`; AdvantageScope model in
`cad/advantagescope/Robot_BIOBUZZ/`, model_0 the extractor 0–150°, model_1 the roller and motor rising 0–33 mm):

- **Roller:** 13.8 in of wheels, bottom 2.4 at rest, axle 1.0 in ahead of the face and 3.35 up, rising 1.3 in in
  vertical slots with its bearings in outboard float plates; the only gap in it the transfer's at y +2.35 to
  +2.9; gravity and a spring (not drawn; the POLLEN bite, set on the rig) onto printed down stops.
- **Motor:** on the carriage 77.5 mm above the roller (constant belt), the printed carriage sliding on the left
  upright's front face on shoulder screws **in holes 7.6 and 7.9 in up** (the lower holes are behind the motor;
  the README asks the team to check those holes exist), a bridge over the motor pulley tying it to the left
  float plate.
- **Extractor:** its own 8 mm REX shaft **2.4 in ahead of the face**, 4.5 up (1.5 mm more than 2.34, to clear the
  raised roller's wheels from the arm collars); arms at ±1.8, the same block and seat; stowed folded up in front
  of the roller at 150°; driven by the servo over the roller on the right through a 1:1 gear pair in the gap
  between the roller's right end and the side plate, the shaft's gear a sector so nothing sticks out stowed;
  worst servo load about 1.3 kg·cm.
- **V:** as drawn, its roots 4 mm forward (1.55 in ahead) and its tabs lower, to clear the float plates.
- **Checked clear:** the roller rising 0 to 1.3 in against the robot and every fixed part; the extractor every 5°
  from 0 to 150° with the roller down, half up and fully up.
- **Sizes:** starting **17.96 in** (V tips 2.84 out, stowed extractor 2.81, side plates 2.83); deployed 20.96
  (22.6 while swinging); 17.8 in across.

| Outline (x ahead of the chassis centre, y left, z up; the face at x 7.56) | x | y | z |
|---|---|---|---|
| Roller and motor, down | 7.56 to 9.50 | ±7.93 | 2.40 to 7.97 |
| Roller and motor, floated 1.3 | 7.56 to 9.50 | ±7.93 | 3.70 to 9.27 |
| Extractor, down | 9.26 to 13.40 | ±1.9 (shaft ±7.76) | 0.61 to 5.56 |
| Extractor, stowed 150° | 8.71 to 10.37 | same | 3.44 to 9.51 |

Still guesses, and the user has decided to build without the rig: the spring force (the POLLEN bite) and whether
a NECTAR gives 0.4 in; the slot covers zero give either way.

**Decisions that fix the front** (the user, 6 Oct 2026, via the CAD session): the FLOWER extractor stays at the
front as drawn (a rear extractor was dropped: entering at the back reverses the J, so the robot could not shoot
while extracting); the robot shoots while extracting, the pieces going roller, lane, J, turret, which is why the
roller has no gap but the transfer's and the extractor's block sits in front of the roller's centre; simple,
reliable hardware over clever, so the float is slots and a spring, not an arm; the Limelight is fixed facing
forward, above the motor carriage's 8.5 in; the NECTAR-capping cage is shelved. The transfer is being drawn next
(#164).

**The V's angle** (this chat's decision under [unified-design.md](unified-design.md); 60 runs, the Rigid V's Autos,
the drawn intake; the angle is the plate's from straight ahead, tips 17.8 in apart, so steeper is shorter):

| V | Tip ahead of the face | ShootsRight: points · TIP 3 · G409 runs | Angled: points · TIP 2 · G409 | Wall (broken route): points · TIP 2 · G409 |
|---|---|---|---|---|
| **31°, as drawn** | 2.84 in | **69.9 · 47 · 11** | 50.4 · 51 · 18 | 42.0 · 39 · 10 |
| 35° | 2.36 | 68.3 · 43 · 6 | 49.6 · 48 · 10 | 42.3 · 40 · 6 |
| 45° | 1.65 | 66.9 · 40 · 4 | 51.3 · 52 · 2 | 42.3 · 40 · 0 |
| 60° | 0.95 | 64.3 · 34 · 4 | 51.8 · 53 · 1 | 43.0 · 42 · 1 |
| 16 in tips, 2.84 ahead (41°) | 2.84 | 67.0 · 40 · 6 | 51.2 · 51 · 16 | 43.0 · 42 · 10 |

**Decision: the V as drawn.** Tips 17.8 in apart, 2.84 in ahead of the face (31° from straight ahead in the
simulator's flap, 41° off the side plate's own corner in the CAD), **4 in tall** (the body-designs chat's height
sweep: 2.2 and 2.5 in plates cost 2.5–3.5 points, because pieces bounce and fall into the upper part), 1/8 in
aluminium, flap bounce as modelled (restitution up to 0.3 scores the same; 0.5 costs the wall Auto 3 points).
Every 5° steeper costs about 1.5 points and 4 TIP 3s on ShootsRight while halving the G409 touches, so **45° is
the fallback** if the plates' touches on a falling spill turn out to be called on a real field: 3 points for a
quarter of the touches. Narrower tips lose as much as a steeper plate without the G409 gain. The angled Stages
Auto prefers the steeper plates by a point, within noise of the 60 runs.

**What the routes must do for this V** (the body-designs chat, `tools/auto-routes/guide_routes.py`,
`turning_west()`, TURN_X 55.5): turn out of the tunnel at x 55.5 instead of 57.5, or the tips reach over the
centre line at about 8.6 s on both Stages routes; 17.8–18 in is the widest V these routes allow (20 in still
crosses at about 21 s). With the wall partner the V sweeps up its row square to it (4 of 4), not face first
(1 of 4), and not through the west lane (55.7 against 47.3). Holding TIP 1's spill at the drop zone adds about
2 points and triples the touches: no.

**Checked in the drawing:** the ramp and FLOWER block passing the roller as the hook folds (about 90°); the V
plates against the stowed hook, the front wheels and the belt; the servo load; the lane between the curtains,
now 2.25 to 4.7 in out each side, 4.50 in clear for a NECTAR. **Still open:** whether the 5.5 in lane empties a
FLOWER in the 1.1–1.4 s `ramp.py` gives the 7.4 in one (cardboard test 2b), and, before anything is cut, the
robot's designer agreeing that the roller behind the face goes. The pod and V-plate holes are placeholders to
drill to the real parts.

**Parts** (`cad/intake-b/README.md`, "Parts to order", checked against goBILDA's listings by the CAD session):

| Part | goBILDA | Notes |
|---|---|---|
| Roller motor | 5203-2402-0005 Yellow Jacket, 5.2:1, 1150 rpm, inboard on the left, its body 5.7–7.1 in off the tiles | A 48 mm wheel is 5.94 in round, so 1150 rpm at the roller is about 114 in/s at the surface, about twice the robot's 50 in/s: the usual range for an intake that pulls a piece in while driving at it. If the cardboard rig shows gecko wheels throwing pieces instead of feeding them (cardboard test 1), the step-down below, then the 5203-2402-0014 motor (13.7:1, 435 rpm, 43 in/s) |
| Roller drive | **1:1**: two 3417-4008-0024 pulleys (24T HTD5, 8 mm REX bore) and a 3412 Series 9 mm belt, 55T (275 mm) | goBILDA's shortest 9 mm belt sets the centres at 77.5 mm, so the motor sits 77.5 mm straight above the roller's axle (the 67 mm first drawn does not exist in stock parts). Belt and pulleys inside the left plate, lowest point 2.47 in off the tiles, in the 16 mm beside the roller's end where no piece should be. **Step-down option 2:3**: a 3417-4008-0016 (16T) on the motor, the 24T on the roller, about 767 rpm and 76 in/s (1.5× the robot's speed); the same belt at 87.3 mm centres, which the motor bracket's slotted holes (9.8 mm) allow; the left plate's motor bearing hole then moves 9.8 mm up, or the motor's shaft runs without the outer bearing. 3:4 is not available in REX-bore pulleys |
| Bearings | 1611-0514-4008, flanged, 8 mm REX bore, 14 mm OD, 5 mm, 2-packs | 8: roller 2, hinge 1, motor 1, wheels 4. Not 1611-0514-0008 (round bore) |
| Hook servo | 2000-0025-0002 Dual Mode Servo (25-2, Torque) | 17.2 kg·cm at 4.8 V, 21.6 at 6 V (25.2 only at 7.4 V): on the hub's 5 V ports plan on about 17 against the hook's 5.9, a 3× margin |
| Roller wheels | 48 mm gecko wheels, 7 or 8 on an 8 mm REX shaft (13.8 in of wheels) | The ones in the team's CAD |
| Printed parts | motor bracket (M4 on a 16 mm square round a 14 mm boss), servo bracket, hinge hub, corner block, FLOWER block, clips | STLs in `cad/intake-b/stl/`, placed as on the robot: lay flat to print |

**Stage 2, after cardboard test 3: vectored rollers.** Two short angled rollers at the mouth's outer thirds,
toed in 30 to 45°, feeding a 4.5 in throat on the centre line, in the free corners below 4 in. The simulator
says 3 points with the V (67.0 against 64.1) and 2 on their own. Decide after the cardboard tests, not before.

**What changes in the simulator for this design** (`RobotDesign`): mouth 14 in, roller bottom 2.4, the FLOWER lane
5.5 in (a model for the block is not written yet; the intake takes FLOWER POLLEN at one per 0.5 s), the hook as
`flatIntakeWith(name, 8)` and the V as `flatIntakeWith(name, 0)`; a body of 15.12 × 15.24 once the routes are
refitted (`qual_right.fit`).

## Build these in cardboard first, and measure

> **Decided 6 Oct 2026 (the user): there is no time for cardboard before the build.** The numbers marked guess
> above (roller bottom 2.4 in, 1:1 roller speed, 0.35 s a piece, the 5.5 in lane) are what gets built, and the
> designer is shown the drawn intake rather than asked first. The tests below stay as the checklist for the real
> intake once it exists: the same measurements, taken on the robot, replace the guesses in `RobotDesign.dhsCad()`.
> The hook's touches on falling spill pieces (G409) are to be worked in the simulator, not on the bench.

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
