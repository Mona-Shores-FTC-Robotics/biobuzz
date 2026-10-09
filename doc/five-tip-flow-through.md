# Five TIPs: recovering whole spills with a flow-through intake

**Status: concept, 9 Oct 2026**, from a conversation with the mentor about two of our robots in playoffs
(`doc/unified-design.md`, "Two of our robots"). Nothing is built. This brief is for the Intake Design, transfer,
turret and simulator chats.

## Why

Five TIPs need 4 + 8 + 8 + 8 + 8 = 36 pieces. Our half holds 20 that are always there (both robots' preloads, the two
FLOWERs, the GARDEN); the rest must come back out of the spills. Human NECTAR can't be entered during AUTO (mentor).
Measured today (`tools/auto-routes/sister5.py`): a robot catching a spill standing still gets about 2; picking a spill
up off the floor with the webcam gets 0-3 in 3 s. So two robots each kept to their own end get 2-3 TIPs, and the
4-TIP pair works only because one robot carries pieces across the field.

If each robot could recover a whole spill (about 8) at its own end, each end feeds itself and nobody crosses.
`tools/auto-routes/five_tip_budget.py` times that plan: with a turret that streams (fires while intaking), TIP 5's
shots are away by 29.5 s in nearly every match; with each second load picked up separately, in 2 of 3. Its step times
are partly guesses.

## The concept (mentor, 9 Oct 2026)

1. **Flow-through, not reverse intake.** The front intakes continuously while pieces travel through the robot and out
   the back. The path holds at most 4, physically, so piece 1 is out before piece 5 gets in (mentor: "we need to
   continue intaking while ensuring the 1st is out of our robot before the 5th or 6th gets in"). That keeps it within
   G407 (no more than 4 CONTROLLED at once) however fast a spill comes.
2. **A back gate.** Open: pieces flow out the back. Closed: the next 4 stay aboard for the turret. A piece counter
   closes it after 4 have gone out.
3. **Stage against the end wall.** At the firing spots the robot faces the HIVE, so its back points at the end wall
   (at (57, 20) the back is about 11 in from it). Pieces leaving the back settle in a row in that gap, against the
   wall: a known place, clear of where spills land (35-47 in out from the end wall) and of any CELL's swing.
4. **Turn during the wait, then stream.** After a spill each end waits about 7 s for its CELL to rise again. The robot
   eases forward clear of the row (about 15 in: its corners swept staged pieces in the 5 Oct staging study), turns
   180°, and waits facing the row with the turret aimed back over it. When the CELL rises: fire the 4 aboard, creep
   into the row intaking, and stream the rest. No back intake is needed; this needs a turret that can aim behind the
   robot (at least ±180°). A fixed launcher can't do it.
5. **Each robot does this at its own end, taking turns.**

| TIP | End | Pieces |
|---|---|---|
| 1 | right | R's preloads |
| 2 | left | L's preloads and the far FLOWER, streamed seated (as today) |
| 3 | right | TIP 1's spill (staged 4 + 4 aboard), topped up by the GARDEN |
| 4 | left | TIP 2's spill, all of it |
| 5 | right | TIP 3's spill, topped up by the wall FLOWER; R doesn't PARK, L parks beside it |

5 TIPs + 1 PARK is 111 AUTO points against 96 for 4 TIPs + 2 PARK; in playoffs only the score counts.

## Rules to settle early

- **G407:** the staged row must stand free of the robot while it holds its own 4. Pieces pressed between robot and wall
  could be "stuck on" it (CONTROL), making 8. So: push out, ease forward an inch, and only back into them while taking
  them in. Worth asking in the official Q&A.
- **Herding is CONTROL** (glossary: pushing a piece to a desired location). Pieces must be intaken, not shoved along.
- **G409:** nothing caught before it touches the tiles: the robot's front stays 35 in or less from the end wall while
  a spill falls.

## The question that decides it: where does a spill go?

From the simulator (`spilltrack.py` on the sister5 logs, TIP 1 with nobody near, 10 runs): of the pieces traced out of
the right CELL, about 2 come to rest at our right end, and about 2.6 roll over the centre line onto the other
alliance's half, out of reach in AUTO (G402). If that is what a real spill does, no intake at one end can recover 8,
and the work goes to stopping pieces from leaving rather than to the intake.

Blocking the centre line, tried (`sister5.py`'s spill-block routes, intake off, 20 runs each, TIP 1's spill):

| Robot during the spill | Rest at our right end | Cross the centre line | G409 runs |
|---|---|---|---|
| Away (at the GARDEN) | 1.8 | 2.6 | 0 / 20 |
| Wall side, (57.5, 21) | 2.0 | 2.2 | 0 / 20 |
| Beside the centre line, (60, 36), standing through it | **2.7** | **1.2** | **20 / 20** |
| Wall side, then stepping to (60, 36) 1.3 s after the TIP starts | 1.2 | 2.2 | 20 / 20 |

Blocking helps only from inside the landing zone, where falling pieces touch the robot (G409) every run. **The
simulator's bounce is assumed, not measured**, so filming real spills (`doc/spill-test.md`) comes before any of this
is built on.

## Containment, tried (9 Oct 2026)

The mentor: "I really think we may need the hook or some containment type thing to get this to work consistently."
The Saline films (`doc/saline-piece-physics.md` § 5) say where to put it: a spill first touches 35-42 in out, and
nearly every piece then rolls toward the wall at 25-40 in/s; 12 of 28 were within 5 in of the wall 3 s later. The
simulator already throws them that way (`FieldSim.FILMED_BOUNCE_SCATTER_SPREAD_RAD`, fitted to those films). So
the catcher waits at the wall side, out of G409's reach, and lets the spill roll into it.

`sister5.py`'s `right_catch`: TIP 1 from R_PRE, then stand at (57.5, 21) facing the HIVE, intake on, through the
spill. TIP 1 spills 7 pieces (4 POLLEN, 3 NECTAR); later TIPs spill 8. Same route on every design, 20 runs each,
counted by `catchcount.py`:

| Design | Held (of 4 max) | On the floor at our end | **Kept in reach** | Across the centre line | Under the HIVE | G409 runs |
|---|---|---|---|---|---|---|
| rigid V (turret) | 2.6 | 1.7 | **4.3** | 1.6 | 0.3 | 0 / 20 |
| rigid V, fixed turret | 2.2 | 1.9 | **4.1** | 1.8 | 0.7 | 0 / 20 |
| rigid V 24 in across | 2.6 | 1.6 | **4.2** | 0.9 | 1.0 | 0 / 20 |
| rigid V 6 in deep | 2.5 | 1.9 | **4.4** | 1.5 | 0.9 | 0 / 20 |
| spring hood, full-width intake | 1.4 | 2.0 | 3.4 | 2.8 | 0.9 | 0 / 20 |
| … with side walls out at the TIP | 2.4 | 2.0 | **4.4** | 0.7 | 1.2 | 3 / 20 |
| spring hood, large right hook | 0.6 | 1.9 | 2.5 | 2.4 | 1.8 | 16 / 20 |
| flat intake, ramp hook | 0.7 | 2.5 | 3.2 | 2.6 | 1.1 | 6 / 20 |

"Kept in reach" is held plus on the floor at our end (our half, y < 51), which still has to be picked up.
"Under the HIVE" counts either half.

What it says:

- **The V already contains about as well as anything legal.** Every G409-clean design keeps 4.1-4.4 of 7.
  Widening the V to 24 in or adding side walls halves what crosses the centre line, but those pieces end up under the
  HIVE instead, not in the robot.
- **The hooks lose.** A hook down while the spill falls is touched by falling pieces (G409 in 16 of 20 runs for the
  large hook, 6 of 20 for the ramp hook), and it sits across the intake, so the robot takes in less (0.6-0.7).
- **Capacity isn't the limit here.** The robot held 4 in only some runs; on average 2.2-2.6. A pen holding more
  than 4 would not have added much at this spot. The limit is what reaches the robot at all.

**The budget with the best containment.** After TIP 2 our half holds 8 pieces that are always there (the GARDEN and
the wall FLOWER). TIPs 3-5 need 24, so 16 must come from the spills of TIPs 1-3 (7 + 8 + 8 = 23 pieces). That is 70%
of every spill, every match. The best design keeps 63% in reach and holds fewer than that. On average that is about
22 of the 24 needed, short before a single miss, and every floor pickup costs time. In the simulator, then,
containment doesn't make 5 TIPs consistent. It needs about 1.5 more pieces kept per spill, and none of these
designs gets close.

What could still change that, in order of how much it would move the number:

1. **Real spills cross the centre line less than the simulator's.** The right CELL's pieces sit at x 58-67, within
   4-13 in of the line (x 70.75), so a small sideways kick takes them over. The Saline tracks
   (`tools/saline-stream/saline-rolling-tracks.csv`) are 25 clean rolls chosen for deceleration, not whole spills,
   so they can't answer this. The filmed spill test (`doc/spill-test.md`, 20 TIPs, on the next-meeting list) settles
   it. Until then, treat crossing as unknown.
2. **The pieces under the HIVE are reachable.** That's about 1 a spill if the robot can reach under the frame
   from our half.
3. **G407 allows a pen.** It doesn't help at this catch spot (capacity wasn't binding), but it would let a robot
   gather a spill's floor pieces in one pass instead of two.

### Replies from the other chats (8 Oct 2026)

- **Transfer** (`doc/transfer.md` § "Flow-through: out the back for a 5-TIP Auto", spike/164-transfer): a back exit
  is the lane continued, with the backstop turned into a gate. The cost is a third servo, an exit sensor and a
  3.8 × 3.8 in opening. But "at most 4 aboard" holds by geometry only while the queue is stopped. With the gate open,
  the path from the roller to the back face holds 6 POLLEN nose to tail. Pieces off a pile bunch, so a 5th piece is
  inside for about 0.2 s per piece. Streaming through the turret is clean (5 POLLEN don't fit in the 10.7 in to the
  launch column). Streaming out the back is clean only with spaced entry, which the shared motor can't afford. Their
  alternative needs nothing new built: shed pieces through the launcher at low flywheel speed, turret toward the
  wall. Whether that lob lands in the wall gap rather than over the wall is the simulator's question. Streaming while
  intaking is the design as drawn, at any turret angle, about 1.2 s for 8.
- **Simulator:** the films fix where a spill lands, how fast it spreads and that it heads for the wall. They don't
  measure sideways travel over the whole roll, and the wall bounce (`PLACEHOLDER_WALL_RESTITUTION` 0.5) is a guess.
  So treat "crosses the centre line" as unknown until the filmed spill test (`doc/spill-test.md`), not as a number.

### How much of the crossing is the simulator's randomness

The simulator chat suggested this check. Same catch, rigid V, 20 runs each, with the spill's random parts switched
off (`FieldSim.spillVariety`: per-piece roll and tile-slope variety; `FieldSim.bounceScatter`: the landing kick):

| Setting | Held | Across the centre line | Robot full (s after the TIP) |
|---|---|---|---|
| As fitted (variety 1, scatter 0.1) | 2.6 | 1.6 | 7.5 |
| No variety | 2.9 | 1.4 | 6.9 |
| No scatter | 3.3 | 1.4 | 5.0 |
| Neither | 3.2 | about 0 | 5.4 |
| Neither, rigid V 24 in | 3.1 | about 0 | 5.4 |

The region counts are net changes in floor pieces, and other pieces get knocked about, so a few go negative. Read
them as about 0. Without its random spread the spill stays on our half, and the robot holds 3.2 on average
(4 in most runs; "Robot full" counts a run that never fills as 9 s). So the centre-line loss in the table above is the part of the model nobody has
measured. If real spills behave like the no-spread case, the limit becomes the 4-piece G407 cap, and the flow-through
and pen questions below decide whether 5 TIPs work. The filmed spill test tells which world we are in.

### Draft question for the official Q&A (G407)

> During AUTO our robot has a rigid guide (a V-shaped frame) in front of its intake. Scoring elements from a spill
> roll across the tiles and come to rest inside the V, touching the guide, while the robot already holds 4 scoring
> elements in its internal path. The robot is not moving and the elements were not pushed there. Are the elements
> resting inside the guide CONTROLLED under G407 (for example as "stuck in/on" the robot or as herding), so that the
> robot would be in violation until they leave the guide? Does the answer change if the guide was lowered after the
> elements had already touched the tiles?

> A second question, if we build a flow-through path: our intake feeds a lane that carries scoring elements through
> the robot and out its back. While a stream passes through, a fifth element can be inside the robot's frame for
> about 0.2 s before the first one has fully left. Is a momentary count above 4 during continuous pass-through a
> G407 violation?

## Asks

- **Intake Design / transfer:** can a front-to-back path that holds at most 4, with a back gate and a piece counter,
  fit inside 18 in with the turret, the V and the extractor? What does it cost the transfer to the turret? How fast
  can pieces flow through, against how fast a spill arrives?
- **Turret:** can it aim behind the robot (at least ±180°), and keep streaming while the chassis creeps?
- **Simulator:** model the flow-through (front in, back out, at most 4 aboard, the gate), the wall row, and the turn
  and stream; then run the 5-TIP loop. Separately: where spills go, once real spills are filmed.
- **Next meeting:** film a few real TIPs from the side and from above (`doc/spill-test.md`).
