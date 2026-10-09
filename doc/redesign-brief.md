# Clean-sheet redesign brief (9 Oct)

What a redesigned robot has to do, from the simulator and body-designs chats. Every number is from 60-seed simulator
runs unless marked *estimate*; the intake's grab rate, interval, speed limit and the bounces are placeholders, not
measurements. Nothing here is measured on a robot.

## Strategy order (set by the user, 9 Oct)

1. **Solo 4 TIPs** (partner does nothing or parks), if any robot can do it. The user doubts it can; the simulator is
   checking feasibility first, because if it's possible it outranks everything else.
2. **Keep the floor:** 3 TIPs in about 55 of 60 with a partner that only shoots preloads (L-Quals does this today).
   No redesign may lose it.
3. **The Sister pair, the main target:** both robots ours, choreography planned together. At early events about 10
   robots can top quals; two of ours with a rehearsed joint Auto is a large edge in alliance play.
4. Low priority: a partner running a simple Auto of its own (too varied to plan for). One look later.

The design follows from these in that order: robot abilities are scored in the simulator first, mechanisms come after.

## The goals

1. **The Sister five-TIP Auto, nearly every time.** Today: 5 TIPs in 16 of 60 runs (93.3 points).
2. **A 4-TIP Auto with a partner that only shoots its preloads.** Today: no such route exists. With that partner our
   best is L-Quals, 3 TIPs in 55 of 60 (73.7 points), TIP 3 at 23.6 s.

## Why goal 1 falls short today

The 16 runs that make five TIPs land them at 4.1, 8.8, 14.8, 21.4 and 27.9 s (median). The last one that still
counts is 29.5 s. The 44 that don't:

| Cause | Runs |
|---|---|
| R catches only 1 to 3 of TIP 1's 8 spilled pieces, not the 4 the plan needs, so it takes the 4-TIP plan | 34 |
| L's seated 8-piece stream for TIP 2 hits the HIVE frame with one shot (a route fix, not a robot fix) | 4 |
| TIP 4 fired short (6 or 7 pieces at the CELL) | 2 |
| Committed to five and failed (a miss, a late TIP 4, parking) | 4 |

So the catch after TIP 1 decides it. It is 1.5 s standing at the spill plus 2.5 s chasing, and yields 2 or 3 of 8:
most of the spill rolls past the robot or to the wall.

## What moves the numbers (one change at a time)

Sister five, fifth TIPs out of 60 (baseline 16):

| Change | Fifth TIPs |
|---|---|
| Intake takes a piece every 0.17 s (today 0.35 s) | 23 |
| Intake takes pieces arriving at up to 120 in/s (today 60) | 20 |
| Both | 27 |
| Grab 100% instead of 85% | 14 |
| Fire rate 0.1 s a shot | 14 |
| Turret aims instantly | 16 |
| Drive 65 or 80 in/s (routes timed for 50; would need retiming) | 12, 10 |
| Holding 5 or 6 (and G407 forbids it) | 5, 3 |
| Shooting on the move | 9 |

The catch itself (body-designs chat, "R holds 4 when TIP 2 raises the right CELL", baseline 21 of 60):

| Change | Holds 4 |
|---|---|
| Reach ahead of the intake mouth: flaps 5 / 6 / 7 / 8 in forward | 35 / 36 / 43 / 45 |
| A piece every 0.5 / 0.35 / 0.2 s | 13 / 21 / 29 |
| An intake as wide as the frame (15.24 in) | 23 |
| Standing further back and sweeping toward the audience side (no hardware) | 32 |
| 8 in reach plus a 0.2 s intake | 50 |
| The mentor's 9 Oct front (flush 9.76 in mouth), any variant | 0 to 6 |

About 10 runs end with 3 whatever the front is: pieces lost over the centre line (G402), which no front fixes.

## What the design must do

1. **Reach 6 to 8 in ahead of the intake mouth**, as a V or flaps that form a pocket for the spill. At the start the
   robot fits the 18 in cube (R102), so this means flaps that fold out after START, within 18 x 24 in (R105), held
   mechanically. An 18 in robot plus 6 in of flaps is exactly 24 in.
2. **An intake that clears a piece every 0.17 to 0.2 s** and takes pieces arriving fast (about 120 in/s). Time it on a
   rig first.
3. **Hold at most 4 pieces, physically** (G407: herding counts as control).
4. **Never catch a piece in the air** (G409: only after it touches something), and never wait under the HIVE.
5. Everything the current robot already does: the turret is fast enough (it never added more than 0.36 s of waiting),
   firing rate is not the limit, and the drive speed stays about where the routes are timed.

*Estimate:* with 8 in of reach and a 0.2 s intake, R holds 4 after the catch in about 50 of 60. Once the stream's lost
shot is fixed in the route, that suggests the five-TIP Auto in roughly 40 to 45 of 60. The simulator should confirm
this before anyone builds to it.

## Goal 2 is not reachable by a faster version of this robot

A 4th TIP with a preload-only partner needs 8 more pieces at a CELL by 29.5 s. The only pieces left by then are TIP 3's
spill at the far end, and the floor is empty by 21 to 25 s in every top-up tried. *Estimate:* it needs either a
partner that collects (which is the Sister pair) or a robot that recovers a whole spill in about 6 s while holding at
most 4 at a time (a flow-through intake that fires as it collects). That is a different robot, not a tuned one.

## Folding flaps on retuned routes (body-designs chat, 9 Oct, d0b812a)

Sister five, both robots with 8 in folding flaps (6 in ahead of the intake mouth; 8.88 in is the most R105 allows on a
15.12 in frame), routes retuned (`sister_five.py`), 60 runs each:

| Front, routes, intake | 5 TIPs | Points |
|---|---|---|
| Today's front and routes | 16 | 92.7 |
| Today's front, new routes (today's / faster intake) | 20 / 29 | |
| Folding 8 in, new routes, today's intake | 33 | 99.4 |
| Folding 8 in, new routes, 0.17 s and 120 in/s intake | 40 | 102.3 |

No run ended at 2 TIPs or fewer: R now tops up a short TIP 2 (a `TipOverdue` trigger, the left CELL up 7 s), which
takes the matches ending at 2 TIPs from 4 to 0. Roughly, the front is worth 13 fifth TIPs, the intake 7 to 9, the
routes about 4.

The flaps must **fold themselves** within about 0.3 s whenever, out, they would reach a wall, the HIVE frame or the
centre line (now or 0.4 s ahead), and while the extractor is down. Fixed 8 in flaps hit walls, the frame and the
centre line in every run. So the design needs pose-aware, fast-folding flaps. A slide-out intake of 3 to 6.9 in does
about as well as 6 in flaps; camera field of view and chase radius change nothing. The pieces R still misses are over
the centre line, against walls 35 to 50 in away, or pushed NECTAR.

## Solo 4 TIPs: no, for any robot the simulator can build (9 Oct)

Partner parks only. Today's robot gets 3 TIPs in 42 of 60 (TIP 3 at 27.2 s); with a 0.17 s / 120 in/s intake, 46;
with that and a 65 in/s drive, 51 (TIP 3 at 25.9 s, 3.6 s left, and a 4th needs 8 more pieces). The simulator then
built a flow-through robot (fires while it sweeps, pushes a 5th piece out the back so it never holds more than 4) and
ran a solo probe route: TIP 1 at 3.8 s, then nothing, in every seed. The limits are geometric, not the intake:

1. You can't stream into the far CELL from your own end: shots hit the HIVE or go long, even with a steeper arc. So
   firing while collecting only works at the end whose CELL is raised, which after your own TIP is the other end:
   pieces have to be carried across, 4 at a time (the R-Quals cycle).
2. A spill ends up against the end wall and around the wall FLOWER, where a front can't sweep it without hitting the
   FLOWER holder or the wall, and for the first 1.5 to 2 s it's still in the air (G409).
3. G407 makes every later TIP two trips.

So the solo brief is **3 TIPs + PARK** (R-Quals, about 72 points at best), and the floor with a preload-only partner
stays 3 TIPs in 55 of 60. The redesign's gain is in the Sister pair, where the other robot is at the raised CELL's end.

## Human-player NECTAR in AUTO: allowed by the text, unconfirmed (9 Oct)

The left end starts with 8 pieces to the right end's 15, and every lost Sister run is a left-side shortfall. G426.A
lets the drive team enter one NECTAR through the LOADING ZONE (at the left end) each time its HIVE TIPs, and nothing in
the manual (TU04, 8 Oct) limits that to TELEOP: G426 and G427 have no period limit, G401 (AUTO) forbids only
interacting with a ROBOT or an OPERATOR CONSOLE, and TU02's edit to G426 added "NECTAR does not have to be entered
immediately when a HIVE TIP occurs". The Saline stream shows a red NECTAR entered 2.3 s after an AUTO TIP. The
team's working assumption since 9 Oct has been that it can't be entered in AUTO (unified-design.md); that needs a
source or a Q&A answer before an Auto counts on it. If allowed, it gives the left end 2 NECTAR by TIP 3 and 3 by TIP 4,
about the shortfall.

## Hard limits (game manual, TU03)

- R102: start inside an 18 in cube (preloads may stick out). G304: start touching the wall.
- R105: after START, at most 18 x 24 x 29 in tall, held mechanically, no detached parts (G416).
- G407: never control more than 4 scoring elements.
- G409: no catching or deflecting a piece released by a TIP until it has touched something else.
- G417: affect the HIVE only by launching into an upward-facing CELL; no touching the rocker or frame.
- G402: stay on our side in AUTO. G418: FLOWERS only as the game allows.
