# Holding 4 after TIP 1: positioning, intake and front shape (simulator, 9 Oct 2026)

Sister five decides on R's count before TIP 2 ("Holding 4? Then 5 TIPs"): with 4 it makes 5 TIPs in 16 of 20 runs,
and on today's V it reaches 4 in only 20 of 60. Mentor, 9 Oct: "we know how important capturing 4 like 60/60 is".

Every row below is R's opening alone (`tools/auto-routes/opening.py`: TIP 1's preloads, the catch, the sweep off the
floor, then R holds), paired with sister5h-left so TIP 1 and L are real, 60 seeds, scored by `openscore.py` on what R
holds when TIP 2 raises the right CELL. Design changes are `RobotDesign` fields on robot 1 only (rigid V otherwise).
No row had a G409.

## Positioning and sweep (today's V)

| Route | Holds 4 |
|---|---|
| sister5l-right's opening, catch at (57.5, 21) facing the HIVE | 21 |
| catch spot 6.5 in toward the audience wall (x 51) / 3.5 in (x 54) | 2 / 16 |
| catch spot toward the centre line (x 60, 61.5) | 20 / 20 |
| robot turned 15 deg either way at the catch (intake angled) | 2-5 |
| catch 3 in closer to the HIVE (y 24) | 15 |
| catch 3 / 5 / 7 in further back (y 18 / 16 / 14) | 27 / 29 / 26 |
| sweep toward the audience side (46, 30) instead of (50, 24) | 26 |
| sweep forward (57.5, 34) / forward and toward the centre line (60, 30) | 20 / 13 |
| sweep in from the side, either direction | 1-2 |
| no webcam chase during the sweep | 11 |
| sweep after standing 0.8 s / 2.5 s (now 1.5) | 2 / 19 |
| ... with y 18 and the (46, 30) sweep: 1.2 s / 1.5 s / 2.0 s | 15 / 31 / 28 |
| preloads fired from the catch spot | 21 |
| **back to y 16, sweep to (46, 30)** ("route" below) | **32** |

TIP 1's spill drifts toward the centre line in the simulator: about 2.7 of its 7 pieces cross it and are lost (G402),
1.2 roll under the HIVE.

## The intake (today's V and route), one placeholder at a time

| Change | Holds 4 |
|---|---|
| keeps 70% / 85% (today) / 100% of what reaches it | 16 / 21 / 22 |
| a piece every 0.5 s / 0.35 s (today) / 0.2 s | 13 / 21 / 29 |
| speed limit 40 / 60 (today) / 100 in/s | 21 / 21 / 24 |
| intake as wide as the frame (15.24 in) | 23 |

Speed matters; how sure the grab is matters little, because the pieces that are lost never reach the intake.

## The front shape

| Front | Holds 4 |
|---|---|
| today's V: 15.12 x 15.24 frame, flaps 1.27 out and 2.8 forward | 21 |
| one 6 in / 8.9 in wall on the centre-line side, out after the TIP | 24 / 25 |
| the same on the audience side / both sides | 19 / 23 |
| 14 x 14 frame, flaps 2 out and 4 forward | 23 |
| 12 x 14 frame, flaps 2 out and 6 forward | 28 |
| **flaps reaching further forward** on a shorter frame (full width, 1.38 out): 5 / 6 / 7 / 8 in | **35 / 36 / 43 / 45** |
| folding flaps (24 in wide, or 8.9 in forward) | 9 / 11 (they probably never opened: the simulator lowers them like the hook) |
| **the mentor's 9 Oct front** (CAD chat): 14.1 x 16.6 frame, 9.76 in mouth flush with the face, nothing ahead | **0** |
| ... taking a piece up to 3 in out / keeping everything / with a V / all of that, best route, fast intake | 4 / 1 / 5 / 5 |

## Combined

| | Holds 4 |
|---|---|
| today's V, route, a piece every 0.2 s (0.15 s and a sure grab add nothing) | 44 |
| flaps 6 in forward, route, 0.2 s | 46 |
| **flaps 8 in forward (10 in frame), 0.2 s, either route** | **50** |

## What it says

- **Reach ahead of the intake is the lever.** Pieces that are lost never reach the intake; a V reaching 6-8 in ahead
  of it makes a pocket that keeps them. Width doesn't help.
- **Then an intake that clears pieces fast** (0.2 s each). With today's V, the route and a 0.2 s intake reach 44 of 60
  with no chassis change at all.
- **The mentor's front, as the simulator sees it, doesn't catch a spill:** a 9.76 in mouth with nothing ahead of it.
  Its helper wheels may well be what the GARDEN corner needs (the simulator can't judge that), but they don't bring the
  spill to the mouth. The ask: keep his wheels, put reach ahead of the face, and make the intake fast.
- **50 of 60 is the best so far;** the other 10 end with 3. The rest is probably pieces over the centre line.

## Caveats

- A 10-12 in long chassis is a simulator shape: whether the drive, turret and lane fit is the CAD chat's question.
- The intake numbers (grab, interval, speed limit) and every bounce are placeholders until a rig is timed and filmed.
- These are R's opening only; the Sister five score with the best front is the next run.

# Today's frame with folding flaps, a slide intake, more vision, and Sister five retuned (9 Oct 2026, evening)

Everything below is the simulator, 60 seeded runs each; every intake number is a placeholder. New design fields
(commits dbaeb9d, b14802f, 1884c38): `flapsAfterStart` (flaps folded inside the frame at START, out from 1 s, R105),
`slideMaxIn` / `slideSpeedInPerS` (the intake head runs forward while intaking and nearly still, never over the
centre line, a wall or the HIVE frame), and the piece camera's field of view, range and chase radius. A robot with
folding flaps folds them itself whenever, out, they would reach over the centre line, a wall or the HIVE frame (now or
0.4 s ahead) or while the FLOWER extractor is down: the GARDEN, the FLOWER seats and turns near the HIVE.

## R's opening (holds 4 at TIP 2), today's 15.12 x 15.24 frame

Flap lengths are from the frame's front face; the intake mouth is 1.94 in ahead of it, so 8 in flaps reach 6 in ahead
of the mouth (8.88 is the most R105 allows: 24 in long).

| Front | Holds 4 | G409 runs |
|---|---|---|
| today's V (2.8 in) | 21 | 0 |
| folding flaps 6 / 8 / 8.88 in | 34 / 40 / 46 | 0 / 4 / 10 |
| folding 8 in, a piece every 0.2 s | 42 | 4 |
| folding 8 in, 0.17 s and 120 in/s | 44 | 4 |
| folding 8 in, the better route (catch at y 16, sweep to (46, 30)) | 48 | 0 |
| **folding 8 in, better route, 0.2 s** | **51** | 0 |
| slide intake 3 / 5 / 6.9 in (30 in/s) | 34 / 32 / 31 | 0 |
| slide 6.9 in at 60 in/s / with a 0.2 s intake | 30 / 36 | 0 |
| slide 5 in plus folding 8 in flaps | 46 | 4 |
| camera 180 deg / 360 deg (today 70) | 20 / 20 | 0 |
| camera 360 deg, chase radius 60 in (today 36) / today's camera, 60 in | 21 / 21 | 0 |
| camera 360 deg, 60 in, better route / with folding 8 in and 0.2 s | 35 (route alone 32) / 43 (without it 42) | 0 / 4 |

- **A slide is worth about what 6 in flaps are (31-34),** and a longer or faster slide adds nothing; on top of 8 in
  flaps it adds 6. Modelled simply: the head reaches but doesn't sweep sideways or push.
- **More vision crosses no threshold.** In the runs that end with 3, the missing pieces are over the centre line
  (2.5 a run, no robot may follow them), against the walls 35-50 in away (a chase can't take them without driving
  into the wall), or NECTAR pushed about the field rather than TIP 1's spill. A wider camera sees nothing more that
  R can reach in time.

## Sister five, both robots the same front (5 TIPs of 60)

Changes to the routes (`sister_five.py`):
- **R:** catches TIP 1 at (57.5, 16), sweeps to (46, 30).
- **R, TIP 2 top-up:** if the left CELL has stayed up 7 s, TIP 2 is one short. That's L's 8th shot bouncing off the full CELL, 4 of 60. R then fires one or two pieces at it from R_N and comes back to (34, 24), clear of L.
  - New trigger `TipOverdue`. TIP 2 normally comes 3.6–5.8 s after TIP 1; one short, 14 s or more.
- **L:** waits for TIP 2's spill at y 120–122 (was 116) and for TIP 3's at y 18–19 (was 22), so 8 in flaps stay out of a spill still falling (G409).
- **L, before parking:** waits 10.5 s for TIP 2 (was 4.5 s), so the top-up has time.

| Front, routes | Intake | 5 TIPs | 4 | 3 | <=2 | mean pts | collide | frame / wall / cross | G409 runs |
|---|---|---|---|---|---|---|---|---|---|
| today's V, today's routes (sister5l + sister5h) | today | 16 | 36 | 4 | 4 | 92.7 | 1 | 1 / 0 / 0 | 1 |
| today's V, today's routes | 0.17 s, 120 in/s | 28 | 25 | 3 | 4 | 97.0 | 1 | 0 | 6 |
| today's V, new routes | today | 20 | 32 | 7 | 1 | 94.5 | 1 | 1 / 0 / 0 | 0 |
| today's V, new routes | 0.17 s, 120 in/s | 29 | 20 | 9 | 2 | 96.0 | 0 | 2 / 0 / 0 | 1 |
| folding 8 in, today's routes (flaps fold for walls, frame, line) | today | 20 | 32 | 4 | 4 | 94.0 | 0 | 0 | 56 |
| folding 8 in, new routes, L at y 120 / 19 | today | 36 | 16 | 8 | 0 | 100.1 | 0 | 0 | 8 |
| folding 8 in, new routes, L at y 120 / 19 | 0.17 s, 120 in/s | 42 | 13 | 5 | 0 | 103.0 | 0 | 0 | 15 |
| folding 8 in, new routes, L at y 122 / 18 | today | 33 | 20 | 7 | 0 | 99.4 | 0 | 0 | 2 |
| **folding 8 in, new routes, L at y 122 / 18** | **0.17 s, 120 in/s** | **40** | 15 | 5 | **0** | **102.3** | **0** | **0** | **3** |

(sister5l-right/sister5h-left as published; the new routes are `right5(topup2_ms=(6000, 6000), catch_at=(57.5, 16,
90))` with R_C1 = (46, 30), and `left5(bail_ms=10500, ln=(55, 122, 270), ls=(59, 18, 90))`.)

- The front is worth about 13 fifth TIPs (20 to 33), the faster intake about 7-9 more, the routes about 4.
- The TIP 2 top-up ends the 2-TIP matches (4 of 60 to 0).
- Not fixed: today's V clips the HIVE's foot on R's park path from the TIP 5 pocket at 28.5 s (1-4 runs); folding flaps
  fold for it.

## Caveats

- Flaps that fold themselves near walls and the HIVE assume the robot knows its pose and can fold a flap in about
  0.3 s; nothing like it is drawn.
- The slide is modelled as a moving mouth, not a mechanism: no sideways sweep, no pushing.
- G409 here is the simulator's reading (a flap touching a piece still falling); the remaining runs are L's flaps at its
  TIP 2 wait.

# Buildable fronts and human-player NECTAR (9 Oct 2026, late)

Same Sister five routes as above (R `topup2_ms=(6000, 6000)`, catch at (57.5, 16), sweep to (46, 30); L `bail_ms=10500`,
waits at y 122 and y 18), both robots the same front, 60 runs each, simulator only.

## Fronts a team can build (the self-folding flaps above can't be)

| Front | Intake | 5 TIPs | <=2 | mean pts | frame / wall / cross | G409 runs |
|---|---|---|---|---|---|---|
| today's V (2.8 in) | today | 20 | 1 | 94.5 | 1 / 0 / 0 | 0 |
| today's V | 0.17 s, 120 in/s | 29 | 2 | 96.0 | 2 / 0 / 0 | 1 |
| **fixed flaps 3.4 in** (out from 1 s, never fold) | today | **28** | 2 | 97.3 | 1 / 0 / 0 | 0 |
| fixed flaps 3.4 in | 0.17 s, 120 in/s | 30 | 2 | 97.4 | 1 / 0 / 0 | 1 |
| fixed flaps 4 / 5 / 6 in | today | 29 / 30 / 33 | 0-2 | 97-101 | **every run** hits a wall and the HIVE frame (6 in: the centre line too) | 0-2 |
| scheduled 8 in flaps, 0.5 s servo (CatchOut / CatchIn cards) | today / 0.17 s | 28 / 31 | 0 / 1 | 96.4 / 97.6 | 0-2 / 0 / 0 | 2-3 |
| scheduled 8 in flaps, 1.0 s servo | today / 0.17 s | 18 / 21 | 0 | 94.2 / 94.9 | 2 / 0 / 0, 3-4 collisions | 2 |
| scheduled slide 5 / 6.9 in | today | 1 / 0 | 10-16 | 75-80 | 10-27 frame hits | 0-1 |

- **3.4 in is the longest fixed flap that stays clear** (4 in: R's GARDEN pickup puts it past the south wall, and the
  turns reach the HIVE's feet). It does as well as scheduled 8 in flaps on a 0.5 s servo, with nothing to actuate.
- A 1 s servo loses the catch: the flaps are still swinging when the spill lands.
- **The slide result isn't a slide result:** R's webcam chase with the head out drives it into the HIVE's foot. The
  chase now counts the slide's reach and the whole outline, but routing the slide (in for the chase, out for the
  stationary catch) is unfinished. On R's opening alone a slide is worth about 6 in flaps (above).

## Human-player NECTAR (G426.A: one per TIP of our HIVE, through the LOADING ZONE; Q&A pending)

The simulator's drive team drops each NECTAR in the middle of the LOADING ZONE (x 0-11, y 94-118) 2 s after the TIP;
a NECTAR weighs 0.091 lb, 1.65 POLLEN. Today's V unless stated.

| Plan | 5 TIPs | <=2 | collide | mean pts |
|---|---|---|---|---|
| no NECTAR (above) | 20 | 1 | 1 | 94.5 |
| NECTAR entered, routes unchanged | 19 | 2 | 1 | 92.2 |
| L, after its TIP 4 share, takes them down the lane to TIP 5 | 9 | 2 | 35 | 90.5 |
| L parks at a LOADING ZONE station after TIP 4 | 0 | 2 | 59 | 90.8 |
| R, after TIP 4 at R_N (25 in from the zone), takes them instead of the wall FLOWER (2.5 s) | 13 | 2 | 1 | 90.4 |
| ... waiting up to 4.5 s for the third | 11 | 2 | 1 | 88.7 |
| ... then the wall FLOWER to fill up | 19 | 2 | 1 | 91.7 |
| R takes them, 3.4 in fixed flaps, 0.17 s intake | 24 (30 without NECTAR) | 2 | 0 | 95.5 |

- **L can't get there:** R is at R_N, beside the zone, firing TIP 4 when L would go (35-59 collisions in 60).
- **R gets there at about 16 s and finds about one NECTAR:** two have been entered by then (after TIPs 1 and 2; the
  third lands 2 s after TIP 3, about 17.7 s), and the simulator's drop rolls them out of the zone or toward the field.
  R leaves with 0-2 in 26 of 27 runs, less than the wall FLOWER's 4 POLLEN.
- So in Sister five, as simulated, the NECTAR doesn't help: one per TIP is little, it arrives late, and taking it costs
  the time of a robot that is busy elsewhere. What would change that is the technique in doc/human-nectar.md (on
  claude/robotics-meeting-notes-lq2y55): the drive team holds the NECTAR and rolls it along the wall into a robot
  already waiting in the zone, on cue. The simulator's drop is a placeholder until that is practised and timed.

## Rolled NECTAR (the simulator chat's model, claude/simulator e507f53)

The drive team rolls each NECTAR along the wall from the far end of the LOADING ZONE, 5 in off it at about 12 in/s,
1 s after the TIP; "bank 3" holds the first three and rolls them 0.6 s apart after TIP 3. Uncaught, they stop 2-5 in
off the wall at y 75-90. Run on a scratch merge of that commit with this branch (not merged here), today's V, 60 runs.

| R after TIP 4 (at R_N) | one per TIP: 5 TIPs | bank 3: 5 TIPs |
|---|---|---|
| nothing (today's routes) | 24 | 24 |
| waits at (12, 96) facing up the wall to catch, 3 s | 1 | 3 |
| catches, then the wall FLOWER | 11 | 15 (14 at x 10) |
| CollectSeen at (14, 80) | 10 | 18 |
| **sweeps down the wall at x 10 to y 64, then the wall FLOWER** | **25** | **25** |

- **Nobody catches a roll:** R gets to the zone at 16-18 s, and every roll (bank included, about 14.5-17.4 s) has
  already gone past. The catch spot is the right place only if the drive team holds the NECTAR until R is there.
- **The sweep picks up 1-2 NECTAR on the way and costs nothing,** but R fills to its 4 at the wall FLOWER either
  way, so a NECTAR only replaces a POLLEN in the load. That doesn't make another TIP.
- So rolled NECTAR doesn't help Sister five either. It would help a robot that is short of pieces, or in TELEOP.

### Rolled on cue (claude/simulator f96529f)

`BIOBUZZ_AUTO_HUMAN_CUE=1`: the drive team holds every NECTAR owed until one of our robots sits still within 4 in of
(10, 96), then rolls them 0.6 s apart. R waits there after TIP 4, intake facing up the wall.

| R after TIP 4 | 5 TIPs of 60 |
|---|---|
| nothing (rolled, not on cue) | 24 |
| catches for 4.5 s (bank 3 on cue) | 23: R catches all 3 in 22 of 28 runs |
| catches for 3.5 s | 25 |
| catches for 4.5 s, then the wall FLOWER (bank 3 / one per TIP on cue) | 10 / 10 |

- **On cue the catch works, but it only breaks even:** 3 NECTAR (4.95 POLLEN-weights) in place of the wall FLOWER's
  4 POLLEN, for about the same time. Topping up to 4 at the FLOWER costs a TIP 5 in more than half the runs.
- The simulator chat's reading of the tipping rule says why: an emptied CELL needs 7.95 POLLEN-weights, and a 4-piece
  load of NECTAR is 6.6. So NECTAR can lighten a load only when a robot arrives short; it never adds one.

## How bouncy the robot's front is (CAD chat, 10 Oct 2026: foam facing or curtains)

The simulator gives every robot surface (frame, V, flaps) one restitution, `FieldSim.PLACEHOLDER_ROBOT_RESTITUTION`
= 0.1, a guess: nobody has measured it. Contacts slower than `RESTING_IN_PER_S` don't bounce at all. Sister five
routes, both robots the same, no NECTAR, 60 runs each; "R full" is R's TIP 1 floor pickup ending with 4 held.

| Front | restitution 0 (foam / curtain) | 0.1 (the simulator's guess) | 0.4 (bare polycarbonate) |
|---|---|---|---|
| today's V: 5 TIPs / R full | **28** / 38 | 21 / 32 | 4 / 12 |
| fixed flaps 3.4 in: 5 TIPs / R full | **33** / 42 | 28 / - | 5 / 13 |

- **Bounce off the front is a large share of what R misses**, in the simulator: going from 0.1 to 0 is worth 5-7 fifth
  TIPs in 60, and going to 0.4 loses nearly all of them. Mean points go 85-87 at 0.4, 95-97 at 0.1, 95-99 at 0.
- So the first thing to measure is the real front's restitution: drop a POLLEN onto the V's polycarbonate and onto a
  0.5 in EVA sample from a known height and film the rebound (rebound height / drop height = e squared). If the bare
  front is nearer 0.4 than 0.1, the simulator has been optimistic and damping is worth more than anything above.
- The model's limits: one value for all surfaces and angles, no "soak-up" time (a curtain that swings back), and
  a piece hit by a moving robot still leaves at the robot's speed whatever the restitution, so a damped face can't
  stop a piece the robot pushes.

## Two fronts for the other chats (10 Oct 2026)

Sister five, both robots the same, today's routes, 60 runs. Mean points / runs with 5, 4, <=2 TIPs / R holding 4
after its TIP 1 floor pickup. Restitution 0.1 (the simulator's guess) unless "foam" (0).

| Front | mean | 5 / 4 / <=2 | R full | notes |
|---|---|---|---|---|
| today's V | 95 | 21 / 33 / 1 | 32 | |
| **printed chassis: 14.6 x 15.24 body, 3.4 in flaps fixed from START (18.0 x 18.0)** | 95 | 22 / 31 / 2 | 37 | 4 runs a flap touches a POLLEN still falling (G409) |
| ... with foam | **98** | **32** / 18 / 1 | | no G409 |
| printed chassis, 1/8 in margin: 14.35 x 15.24 body, flap tips 1.255 out (17.75 x 17.75) | 97 | 28 / 26 / 2 | 40 | no G409 |
| ... with foam | 98 | 32 / 18 / 1 | 39 | 2 runs G409 |
| mentor's 9 Oct front (16.6 x 14.1, 9.76 in mouth), bare | 59 | 0 / 4 / 38 | 2 | |
| ... flaps 3.4 in ahead, 1.38 out (out after START: 20 in long) | 80 | 0 / 32 / 4 | 5 | R into the south wall every run (12-13 s) |
| ... flaps and foam | 84 | 3 / 37 / 3 | 10 | same |
| ... foam, no flaps | 65 | 0 / 11 / 27 | 1 | |

The mentor's numbers are rough: the routes were tuned for a 15 in robot, and the simulator hinges his flaps at the
frame's corners (his are drawn on the uprights, beside the mouth), so his face beside the mouth is still exposed.
