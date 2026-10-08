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
