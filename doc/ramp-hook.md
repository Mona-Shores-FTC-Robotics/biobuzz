# The ramp hook: spill hook and FLOWER emptier in one arm

Exploring and simulating only. No robot was attached and nothing here was measured. Every robot number is a
placeholder for Thursday's cardboard to replace.

## Meeting notes, 6 Oct 2026

Present: Travis, Nathan, CJ and a mentor.

- **The design options and the simulation work.** We went through the body shapes on
  `claude/biobuzz-robot-body-designs-hi386c` (README "The best of them" and "On option 3"): the **rigid V**, the
  **front C**, the **large and small right hooks**, and the ones we did less with (the funnels, the long U).
  Travis understood the rigid V especially well. The students followed the reasoning, and the rules questions
  behind it: G409 touches from a falling spill, and G407's 4-piece limit inside the guides.
- **The new idea.** One student remembered a video of another team: a **thin ramp, rotated down and driven into a
  FLOWER**, emptied it very fast. The team suggested **combining it with the small hook**: give the hook's
  outside edge an inward ramp. Put it down and drive it into the FLOWER, and the POLLEN roll out and are guided
  into the intake. The same hook, held out while we wait for a TIP or pick up, deadens the pieces that land in it.
- **Hungry Hungry Hippos.** We talked about hippo-style grabbers and found no rule either way. No clear answer
  expected, so **the plan is the hook**.
- **Next:** cardboard prototypes on Thursday. The mentor will post the other team's video.

## The idea

The small right hook (design 14: an 8 in arm and a crossbeam, hinged at the bottom of the chassis' front face) gets
two "inward ramps":

1. **A tongue on the crossbeam.** A thin plate, about 2 in wide, sticking out of the crossbeam at floor level and
   sloping down toward the robot. Driven into a FLOWER's retrieval opening, it slides under the bottom POLLEN. The
   column rolls out down the tongue, across the hook's floor and into the intake.
2. **The arm's inside wall leans in** over the hook's floor. A piece rolling into the wall is sent down into the
   tiles instead of back out across the hook. That's the "deadening".

![From above, from the side, and ramp.py's run](../sim-review/ramp-hook.svg)

Redraw it with `python3 tools/ramp-hook/draw.py` and rerun the tables with `python3 tools/ramp-hook/ramp.py`
(about 15 s on 4 cores, no build).

**Short answer.**

- **Physics:** it works in the model, and quickly: all 4 POLLEN at the intake in **1.1 to 1.4 s**, counted from
  an inch short of the FLOWER. The simulator's placeholder, `RobotDesign.flowerPullS`, gives 2.0 s, plus 0.35 s a
  piece through the intake. Two numbers decide it:
  - the tongue's **slope: 10 to 15°**;
  - **how far in its tip goes: at least 2.1 in** past the opening's edge.
- **Size:** on option 3 the tongue breaks R105 unless the arm gets shorter (14.5 + 8 + 2.65 = 25.1 in against 24).
- **Rules:** use it only before 1:00 left. After that a NECTAR can be at the bottom of a FLOWER, and pushing it
  out breaks G418.

## What the FLOWER gives us

- **It already matters.** The Qualifier Auto (qual-right-v3, README on the body-designs branch) makes TIP 2 from our
  preloads plus **the far FLOWER's 4 POLLEN**, and its FLOWER step is 2.3 s long. A faster FLOWER is time the route
  can spend on PARK, which the hooks cost.
- **4 POLLEN per FLOWER**, 16 on the field at the start (§10.3.1). Two of the FLOWERs (W and N) are on red's side
  in AUTO.
- **POLLEN only comes out of the bottom** (§9.7, G418.B *"only remove POLLEN from the bottom"*), through the
  retrieval opening: 3.55 in tall, above a bottom ring 0.43 in tall.

## The rules it touches

Quotes and section numbers are from Competition Manual TU03, as `doc/flower-backboard.md` on
`spike/158-flower-backboard` quotes them. Check later Team Updates and the Q&A before relying on them.

- **G418.B: POLLEN only, out of the bottom.** Emptying the column through the retrieval opening is exactly that.
  **NECTAR may never come out.** G410 keeps NECTAR out of FLOWERs until 1:00 left, so before then a FLOWER holds
  only POLLEN and the tongue is safe. After 1:00 the bottom piece may be a NECTAR (the Bottom NECTAR Bonus is
  for exactly that one), and the tongue would push it out. **Rule for drivers: no tongue in a FLOWER after 1:00.**
- **G418 intent and G415: contact is fine.** G418 allows *"contacting the FLOWER while attempting to enter or
  remove SCORING ELEMENTS"*. G415 forbids *"grabbing, grasping, attaching to"* field elements, but allows a
  concave shape that wraps round a FLOWER to align. A tongue that slides in and rests on the ring is probably on
  the right side of that, but it is the first question for the Q&A. The other team doing it is not a ruling.
- **G407: 4 pieces.** The hook's inside counts. Arrive empty, or with few enough pieces that 4 more stay legal
  (the README's "Over 4 inside" column counts this for spills). In the Qualifier Auto we reach the far FLOWER
  having fired, so we arrive empty.
- **G409: the spill.** Held down while we wait for a TIP, the hook is the spill hook. Touches are already counted
  for design 14 (README: touched in most runs). A leaning wall covers more floor from above, so expect a few
  more touches.
- **R105: 18 × 24 in.** See "Where the tongue goes".

**Questions for the Q&A:**

1. Is driving a thin robot ramp into a FLOWER's retrieval opening, so that the staged POLLEN roll out, allowed
   under G415 and G418?
2. If a NECTAR is at the bottom of a FLOWER and a robot's ramp pushes it out by accident, what's the penalty?

## Where the tongue goes

The tongue has to reach 2.65 in in front of the crossbeam's face: the tip 2.4 in past the opening's edge, plus the
ring's thickness (0.25 in, a guess). Two places it can go:

| | **A. On the crossbeam** (drawn) | **B. On the arm's outer side** |
|---|---|---|
| How you use it | Drive straight at the FLOWER | Strafe sideways into it (mecanum) |
| Where the POLLEN go | Down the tongue, straight back to the intake | Across the arm into the hook, then sideways along the intake toward the open left side |
| R105 on option 3 | 14.5 + 8 + 2.65 = **25.1 in > 24**: the arm has to be **≤ 6.85 in** | 14.5 + 2.65 = 17.15 in wide ≤ 18: fits with the 8 in arm |
| R105 on design 14 (18 wide × 16) | 16 + 8 + 2.65 = 26.65: the arm ≤ 5.35 in | Already 18 wide: doesn't fit |
| As the spill hook | Unchanged, but a shorter arm moves its line | The ramp over the arm is a way out for pieces rolling outward |

**A is the better FLOWER emptier, and B keeps the 8 in spill arm.** The 8 in was chosen to wrap the spill's
90% box, so a 6.85 in arm needs `BodyShapeSpillTest.rightHookAtTheSpill` rerun before we commit.
For Thursday, build A, and if there's time, a B mock-up as well.

## Physics

`tools/ramp-hook/ramp.py`, in the same style as `tools/flower-backboard/plate.py`. `FieldSim` can't answer this.
It takes a FLOWER's POLLEN one at a time and has no ring, window or ramp.

The model is a 2-D slice through the FLOWER's centre:

- **From the manual:** the 4 staged POLLEN (2.8 in, centres 1.4 / 4.3 / 7.2 / 10.1 in), the 0.43 in ring and the
  3.55 in opening.
- **The tongue:** straight and thin, resting on the ring's outer top corner. So inside the tube it rises toward its
  tip, and outside it runs down to the hook's floor plate.
- **The run:** the robot drives the tongue in from an inch out and stops with the tip a set depth in. The intake is
  8 in behind the tip.
- **The POLLEN:** hollow shells, with bounce (e) and friction (mu) swept because nobody has measured them.

Each cell below gives the time until the last POLLEN is out of the tube, then the time it reaches the intake.

**A. The tongue's slope** (ring 0.43 in, drive in at 12 in/s, tip 2.4 in in):

| ramp slope | e 0.25, mu 0.3 | e 0.4, mu 0.4 | e 0.55, mu 0.6 |
|---|---|---|---|
| 5° | 1.56 s / 1.74 s | 1.46 s / 1.65 s | 1.34 s / 1.51 s |
| 10° | 1.26 s / 1.40 s | 1.13 s / 1.26 s | 1.22 s / 1.36 s |
| 15° | 1.03 s / 1.15 s | 1.01 s / 1.13 s | 1.12 s / 1.25 s |
| 20° | 1 stays in | 1 stays in | 1 stays in |

**B. How far in, how fast** (slope 10°, e 0.4, mu 0.4):

| tip past the opening's edge | drive 6 in/s | drive 12 in/s | drive 24 in/s |
|---|---|---|---|
| 1.5 in | 4 stay in | 4 stay in | 4 stay in |
| 1.8 in | 4 stay in | 1.50 s / 1.66 s | 4 stay in |
| 2.1 in | 1.40 s / 1.55 s | 1.10 s / 1.25 s | 0.97 s / 1.12 s |
| 2.4 in | 1.42 s / 1.56 s | 1.13 s / 1.26 s | 1.01 s / 1.15 s |
| 2.7 in | 1.43 s / 1.55 s | 1.13 s / 1.25 s | 1.02 s / 1.14 s |

**C. If the ring isn't what we think** (slope 10°, 12 in/s, tip 2.4 in): 1.26 to 1.44 s to the intake for every ring height
from 0 to 0.6 in. The ring doesn't change the answer, because the tongue rests on it either way.

**D. The simulator's FLOWER pull, for comparison:** 4 × 0.5 s = 2.0 s with the robot held against the tube, plus
the intake's 0.35 s a piece.

**E. The arm's inside wall** (a POLLEN rolling outward hits it, e 0.4):

| wall | speed back across the hook | hop after, at 60 in/s |
|---|---|---|
| vertical | 40% of what it came in with | 0 in |
| leaning in 15° | 18% | 0.1 in |
| leaning in 30° | 3%: it stops at the wall | 0.3 in |
| leaning in 45° | −18%: it's pressed into the wall's foot | 0.4 in |

What the tables say:

- **Why 20° fails.** At 20° the tip is 1.45 in up, above the bottom POLLEN's centre (1.4 in). It pushes the POLLEN
  down and back instead of getting under it. 15° leaves only 0.2 in to spare, so **build 10 to 12°**.
- **Why 1.8 in fails.** A POLLEN pressed against the back of the tube has its centre 1.8 in past the opening's
  edge (with the tube's guessed 3.2 in inside). A tip that stops right there balances it, and it stays in. Past
  2.1 in the tip is under every POLLEN's centre and the depth stops mattering. **The tongue must reach past the
  middle of the tube.** If the tube is wider than we guessed, that depth grows with it: measure it.
- **Driving speed hardly matters** once the tip is deep enough. The bottom POLLEN is lifted by the tongue, and the
  rest drop down onto it.
- **A 30° lean stops a rolling POLLEN dead at the wall.** A vertical wall sends 40% of its speed back across the
  hook, toward the open side. The lean costs little height, and a 4 in wall leaned 30° still stands 3.5 in tall.

What the model can't tell us, and the video or cardboard can:

- the 3-D fit: does a 2 in tongue get through the window and under the POLLEN?
- drag along the tube wall;
- POLLEN that aren't round (§9.8);
- whether the robot shoves the FLOWER (it's bolted to the wall).

## When the video is posted

Things to read off it, frame by frame:

- How high the ramp's edge is, and its angle.
- How far in it goes.
- Whether the robot stops or keeps pushing.
- How long from first contact until the last POLLEN is out. Compare with tables A and B.
- Where the POLLEN go once they're out.

If their time is about 1 s, the model is in the right range.

## Thursday: cardboard checklist

Needs a FLOWER (or a tube of the real inside diameter with a 3.55 in window above a 0.43 in ring), 4 POLLEN,
cardboard and tape, a phone at 240 fps and a tape measure.

1. **Measure the FLOWER first:** its inside diameter, how thick the ring is, the window's width, and how far
   the bottom POLLEN sits from the window's edge. These replace the model's guesses.
2. **Tongue:** cut one 2 in wide and about 4 in long, out of a stiff card or a 1/16 in polycarb offcut. Tape it to
   a board at 10°, 12° and 15°, resting on the ring.
3. **Push it in by hand** to 1.5, 2.1 and 2.7 in, 5 times each, with the 4 POLLEN staged. Count how many come out
   and film it. Tables A and B predict nothing at 1.5 in, all 4 from 2.1 in, and 15° no faster than 12° by much.
4. **Floor and intake:** add 8 in of flat card behind the tongue and a stop where the intake would be. Time the
   last POLLEN from first contact until it reaches the stop.
5. **Wall:** stand a 4 in card wall at the side, vertical and then leaning in 30°. Roll POLLEN into it at a few
   speeds and film how far they come back.
6. **Hook A against hook B:** mock up both on a cardboard 14.5 in chassis, and check them against an 18 × 24 rectangle
   taped on the floor.

Bring the numbers back here. They replace the guesses in `ramp.py` (`TUBE_R`, `RING_T`, the tongue's size) and in
`RobotDesign.flowerPullS`.
