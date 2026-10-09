# A spill-first robot? The decision behind a fifth TIP

**Status: open question, 8 Oct 2026.** Mentor: "with our current robot design, or one with a rigid V of some sort, we
can probably get to 4 tips, which is really strong and probably good enough. In order to get to 5 tips, I think you
need a specifically designed robot to handle the ability to catch 8 from the spill, or really catch 4 with some sort
of through and stage type situation ... maybe there's a new robot design to be thought about at this stage." Nothing
is built yet, so changing direction is on the table. This brief sets out what the simulator says, what it can't say,
and three cheap steps that decide it. Background: `doc/five-tip-flow-through.md`.

## What a fifth TIP needs

TIPs 3-5 need 24 pieces. After TIP 2 our half holds 8 that are always there (the GARDEN, the wall FLOWER), so 16 must
come back out of the spills of TIPs 1-3: 7 + 8 + 8 = 23 pieces, **70% of every spill, every match**, with no human
NECTAR in AUTO and nobody crossing into the other half (G402).

## What the simulator says

**The ceiling is high enough.** `tools/auto-routes/coverage.py` traces every piece of TIP 1's spill over 30 runs with
our robot out of the way, then scans every place an 18 × 24 in robot could stand on our half for how many pieces
roll into it after touching the tiles:

| Footprint (robot away from the spill) | Pieces that roll into it, of 7 | G409 runs |
|---|---|---|
| 24 in across, 18 deep, x 46-70, front face at y 34 | 5.9 | 0 / 30 |
| Same, front face at y 36 | 6.0 | 0 / 30 |
| Same, front face at y 38 | 6.2 | 2 / 30 |
| Same, front face at y 40 | 6.3 | 19 / 30 |
| Any spot nearer the CELL | 7.0 | 30 / 30 |

With the simulator's random spread switched off, the y 36 footprint takes 5.7. The pieces reach it evenly across the
whole 24 in face. So a robot standing broadside at the wall side, its face 34-36 in from the end wall, is in the path
of about 84% of a spill without a G409. That clears 70%.

**No robot drawn so far keeps them.** The same spot, intake on, 20 runs each (`sister5.py`'s
`spill-catch-broadside`, robot centre (58, 25) facing the HIVE; `catchcount.py`). "Kept in reach" is held plus on the
floor at our end:

| Robot | Held | Resting at the face | Kept in reach | Across the centre line | Back toward the HIVE | G409 runs |
|---|---|---|---|---|---|---|
| Rigid V as drawn | 2.4 | 0.2 | 4.1 | 1.2 | 1.5 | 1 / 20 |
| Flat 24 in face (18 in frame, 3 in wings), 14 in intake | 1.1 | 0.3 | 2.9 | 1.9 | 2.3 | 0 / 20 |
| … 24 in intake | 1.6 | 0.3 | 3.2 | 1.9 | 1.9 | 0 / 20 |
| … 24 in intake, dead wings (restitution 0) | 1.6 | 0.4 | 3.2 | 1.9 | 1.9 | 0 / 20 |
| … 24 in intake, dead wings, sure and fast intake | 2.1 | 0.5 | 3.7 | 1.6 | 1.9 | 0 / 20 |
| Pocket: 15 in frame, walls 3 in forward, out from the start | 1.9 | 1.1 | 4.1 | 1.3 | 1.6 | 1 / 20 |
| Pocket: 12 in frame, walls 6 in forward | 2.0 | 1.6 | **4.8** | 0.8 | 1.1 | 3 / 20 |
| Pocket 3 in, sure and fast intake | 2.2 | 0.9 | 4.2 | 1.2 | 1.6 | 1 / 20 |
| Pocket 3 in, walls out only 1.5 s after the TIP | 1.5 | 0.8 | 3.5 | 2.3 | 1.3 | 0 / 20 |

(The designs are `rigid V` with these `RobotDesign` fields changed: `frameIn`, `frameWidthIn` 18, `flapOutIn`,
`flapForwardIn` 0, `guideOutIn`/`guideForwardIn`/`intakeReachIn` 0, `intakeWidthIn`, `flapRestitution`,
`intakeGrabChance`, `intakeIntervalS`, `sideWallsSlideIn`, `sideWallsDeployS`.)

Why, from tracing the pieces at the face:

- **A flat face stops a piece's roll toward the wall but not its sideways roll.** The spill comes off the CELL with a
  drift toward the centre line, and pieces slide along the face, off its end and over the line. A damped face is not
  enough; it needs ends. A surround, not a bumper.
- **A pocket with ends helps,** and the deeper one most (4.8 kept, 1.6 resting in the pocket). But its arms reach to
  y 37, where falling pieces start to touch them (3 of 20).
- **About 1-2 pieces end up back toward the HIVE** whatever the shape. The robot's bounce is low in the simulator
  (restitution 0.1) but piece-on-piece is 0.5, so pieces arriving at a pile at the face can be knocked back; both
  numbers are placeholders.

So in the simulator, the best robot keeps about 4.8 of 7 (69%), right at the 70% line with no margin and before a
single miss. The ceiling says a better catcher could get to 84%; nothing modelled so far does.

## Sizing the moat: can anything hold a whole spill, every time?

Mentor, 8 Oct 2026: pieces hit each other as they land and go sideways or back, so a pocket that waits for them to roll
in never guarantees 8; "design a moat and figure out what the size of that moat has to be in order to have no
violations 100% of the time. And then back into, can our robot support that moat?" A moat is legal under G409 as long
as no piece touches it before it touches the tiles: pieces fall through its open middle, land, and hit its walls only
after. `tools/auto-routes/moat.py` sizes it on 199 simulated spills (TIP 1's 7 pieces at our end and TIP 2's 8 at the
other, turned to our end; 1492 pieces), from logs with our robot away.

**What 100% needs, in the simulator:**

- **The landing patch:** first touches fall in x 47.1-70.8, y 34.0-47.5 (24 in across, 13.5 in deep; 90% of them in
  x 49-67, y 38-44). Pieces land right up to the centre line.
- **Walls 7-9 in tall.** Pieces hit the tiles and bounce back up to about 6-8 in, travelling 10-15 in before touching
  again. The robot's face needs about 8.7 in, the side toward the centre line about 8 in, the other side about 6.5 in,
  and the HIVE side about 5 in. A lower wall is hopped.
- **Inside about 27.7 × 17.5 in:** x 45.1-72.8 (2 in over the centre line), y 32-49.5. At that size nothing falling
  touches the walls on the way down.

**That doesn't fit.** R105 caps the whole robot, body and moat together, at 18 × 24 in. The moat alone is bigger than
that, before any body. **A moat that holds every spill isn't possible under the rules.** How close the largest ones
that fit get (walls 8 in, body 18 in tall, `moat.py --fit`):

| Robot | Moat inside | Keeps of a spill | Every piece kept | Spills with a G409 |
|---|---|---|---|---|
| Today's 15.12 in body, 18 wide × 24 long | 17 × 8.4 | 63% | 0 of 199 | 28 of 199 |
| A 9.5 in body, 18 wide × 24 long | 17 × 14 | 72% | 0 of 199 | 10 of 199 |
| A 9 in body, 24 wide × 18 long | 23.5 × 8.4 | 87% | 70 of 199 | 26 of 199 |
| (No body: 24 × 18 of moat. Not a robot; the ceiling) | 23.5 × 17.5 | 99% | 180 of 199 | 1 of 199 |

Every moat that fits has to stand in or beside the landing patch, where falling pieces skim past at the height its
walls need. Lowering the walls to stop the G409s lets the bounces over (the 9 in body's moat with a 4 in HIVE-side wall:
86% kept, a G409 in 13 of 199). Walls that rise after the spill lands would remove that conflict, but the landings
spread over a few tenths of a second, so some pieces are bouncing while others are still falling.

So, in the simulator: our robot can't carry a moat that guarantees a spill. The best that fits needs a robot only
9 in deep, a different robot from ours, and it keeps 87% while fouling G409 in about 1 spill in 8. The numbers rest on
the simulator's bounce heights, which are placeholders. The filmed spill test should measure two things that decide
it: the size of the landing patch, and how high pieces bounce off the tiles.

## What the simulator can't tell us

The simulator chat: the films fix where a spill lands, how fast it spreads and that it heads for the wall, but not
sideways travel over a whole roll; the wall, robot and piece-on-piece bounces are placeholders. Every number in the
second table depends on exactly those: how far pieces slide along a face, whether foam kills them, how they bounce
off each other in a pile. **A real foam pocket could do much better or no better.** The simulator can't decide this;
a mock-up can.

## The rule that decides the staging

The idea that makes a spill-first robot simple: catch at the firing spot. The catch spot (58, 25) is 5 in from where
TIP 1 is fired today, facing the same CELL. The robot takes 4 into its lane, stops the intake, and lets the rest of
the spill come to rest in the pocket. When TIP 2 raises our CELL again, it fires the 4, starts the intake and streams
the rest through the turret (the transfer chat: about 1.2 s for 8; streaming through the turret keeps the lane at 4
or fewer). No pass-through, no gate, no driving. TIP 1's spill alone (3 NECTAR, 4 POLLEN) weighs about 9 POLLEN.

That rests on one reading of G407. The glossary's CONTROL is "fully supported by or stuck in, on, or under the ROBOT,"
or herding ("moving the SCORING ELEMENT in a preferred direction with a flat or concave face"). A piece resting on the
tiles in a pocket of a robot that isn't moving is arguably none of those, but it sits against a stopped intake roller,
and a referee could call it "stuck in". G407's own examples help at the margin: a momentary 5th that is quickly
reversed is not STRATEGIC, and a non-strategic violation is a verbal warning. That's not a plan to build on. The
question is drafted in `doc/five-tip-flow-through.md`; it has to be asked before any CAD changes.

## What it would change on the robot

The turret, the lane and its 4-piece backstop, the flywheels and the FLOWER extractor stay. The front changes: the V
becomes a pocket, a frame face with walls that reach 3-6 in forward, soft (foam) where pieces strike, with an intake
across the whole face. Unknown and worth asking the simulator before anyone redraws CAD: what a pocket front costs the
qualification baselines (L-/R-Quals use the V to take FLOWERs and catch) and TeleOp cycling.

## Three steps that decide it, all before CAD changes

1. **Ask the official Q&A** the G407 pocket question (`doc/five-tip-flow-through.md`). A "no" ends the idea, or turns
   it into flow-through, which has its own G407 problem.
2. **At the next meeting, add a mock-up to the filmed spill test** (`doc/spill-test.md`): plywood or cardboard, 18 in
   wide and 18 in deep: an 18 in face with 6 in walls forward, foam on the inside faces. Stand it at the catch spot
   (x 49-67 in Pedro; face 31 in and wall tips 37 in from the end wall), not moving. For each TIP, count pieces resting inside the pocket 3 s after the TIP, pieces touching it before
   the tiles (G409), and pieces that end past the centre line. Ten TIPs is enough to see 4 from 6.
3. **The simulator chat:** once the films give the face and pile bounces, refit and rerun this brief's second table;
   and run the L-/R-Quals baselines on the pocket front, to price what it costs the qualification Autos.

**If the mock-up keeps 6 or more of 7 and the Q&A answer is friendly**, a spill-first front earns a fifth TIP and is
worth the redesign. **If either comes back badly**, the V and 4 TIPs (`sister.py`, 4 TIPs in 53 of 60 on the current
simulator) is the robot, and a strong one.
