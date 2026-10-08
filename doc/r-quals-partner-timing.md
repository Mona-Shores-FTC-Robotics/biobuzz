# R-Quals and a late left partner; the whole match (8 Oct 2026)

Two items from the routes chat. All numbers are 60 simulated runs (seeds 1-60) on "rigid V, fixed turret", our
Auto at speed 50, in Pedro inches. Linked from [unified-design.md](unified-design.md), "The qualifier Autos".

## 1. R-Quals' partner-timing risk

R-Quals (`r-quals`, `alone.VARIANTS["qual-south-v"]`) seats at the far FLOWER at x 47.36 and fires TIP 2 from
N_FIRE (57.5, 119). A left partner at the standard left start (59, 132.25; an 18 in robot spans x 50-68, y
123.25-141.25) overlaps both, so a partner that is dead, never fires, or fires late collides with us.

### (a) A corner extractor

`RobotDesign.extractorLateralIn` (simulator, this branch): the extractor reaches a FLOWER this far off the robot's
centre line. Design **"rigid V, fixed turret, corner extractor"**: 7.3 in to the robot's **right** front corner.
(The request said left; facing north at the far FLOWER, the robot has to be west of it to sit at x 40, which is
its right. A left-corner extractor gives x 40 only if the robot seats facing east.) The robot seats at
(40.06, 126.64): its right side at x 47.68, the V's right tip at x 48.95, 1 in clear of the partner. The wall
FLOWER's seat moves the same way, to (14.86, 40.06) facing west.

Two routes (`tools/auto-routes/alone.py`, `side_seat`; run with `python3 side_seat.py`):

- `qual-south-v-side`: both volleys from SIDE_FIRE (40.06, 110), straight back from the seat. TIP 2's spill is
  not caught from there, so without a partner's 4 TIP 3 is short.
- **`qual-south-v-corner`**: TIP 1's catch fired from SIDE_FIRE, then the seat, then TIP 2 from **N_LOW (57.5,
  115.5) facing south** (114.5 since section 3), the lane's top far enough south that the back clears a partner at
  its start.
  The robot leaves the seat straight back and turns through west while still at y 108-112. From there, R-Quals as
  before.

3 TIPs / runs in which the robots collide:

| Left partner | R-Quals (no change) | `qual-south-v-side` | **`qual-south-v-corner`** |
|---|---|---|---|
| shoots (`partner-left-v`) | 56 / 0 | 49 / 0 | **56 / 0** |
| dead at the standard left start | 37 / **60** | 1 / 0 | **35 / 0** |
| dead at the west start (24, 132.25) | 38 / 0 | 1 / 0 | 36 / 0 |
| shoots 3 s late, then leaves west along y 124 | 56 / **56** | 49 / 56 | 56 / **56** |
| never shoots, leaves at 6 s | 38 / 20 | 1 / 23 | 36 / 23 |
| shoots 3 s late, **then stays at its start until 18 s** | 56 / **59** | 49 / 0 | **55 / 0** |
| never shoots, **stays at its start until 18 s** | 37 / **60** | 1 / 5 | **35 / 6** |

PARK 60 of 60 in every row. Our own problem runs (HIVE frame, FLOWER, wall): none on the corner route.

What this says:
- **The corner extractor fixes a partner that sits at the standard left start**, dead or holding: from colliding
  in every run to none, for 2-3 runs of 3 TIPs, and nothing lost with a partner that does its job (56 either way).
- **It does not fix a partner that leaves late.** A partner leaving the left start for the LOADING ZONE drives west
  along y 124 (body y 115-133), across the far FLOWER's seat and everything north of y 105 on our side. It meets
  us wherever we are between 7 and 11 s, seat or no seat. The 6 remaining collisions with the holding partner are
  runs where TIP 1 missed and we were still north at 18-20 s.
- So **which left partners the corner route works with is a scouting question** (a partner runs the one Auto it
  has; we cannot ask it to wait, hold or take a path: mentor, 8 Oct 2026, "Rules every chat keeps"). It works with
  a partner whose Auto leaves the left start by about 6 s, or stays there (dead, or not moving until about 18 s):
  55 of 60 for one that fires late and stays, 35-36 for one that never fires. It does not work with one whose Auto
  leaves west along y 124 between about 6 and 12 s. Without the corner extractor even a partner that stays is a
  collision (59-60 of 60: at its start it is in our seat and N_FIRE).

### (b) Seeing the partner at about 6 s

At 5.2-7.7 s R-Quals drives up the lane (x 57.5) facing north, from y about 90 to 110: the partner's start is
straight ahead.

- **A forward distance sensor, the REV 2m Distance Sensor** (VL53L0X time of flight, I2C, Class 1 infrared laser:
  legal under R710). Mounted on the front, on the centre line, above piece height (6-8 in up, so spilled POLLEN
  and NECTAR on the lane don't answer). From y 100 the face is at y 107.6: a partner's south face (y 123-128,
  depending on its size) reads 15-21 in; with nobody there the beam reaches the north wall at 34 in. A threshold of
  about 26 in separates them. The far FLOWER (x 45-49.7) is 8-12 in off the beam at 30-35 in, at the edge of the
  sensor's roughly 25° cone, so test that it doesn't read it. Read it only in a window (5.5-7 s) and as the one I2C
  read it costs, timed with `LoopTimeBaseline`.
- **The webcam** (`PieceVisionSubsystem`, the SDK's colour-blob processor, on while intaking, so on during the
  climb): it finds pieces, not robots. A robot has no colour we can rely on (red NECTAR is red too); a size and
  height filter on the partner's alliance marker is possible but fragile. Not recommended.
- **The Limelight** is AprilTags only, pitched up at the HIVE; never a colour pipeline (CLAUDE.md).

What it would take: one sensor, its `DeviceNames` constant, an XML entry on each robot, a condition in
`AutoRegistration` (for example `LeftStartClear`), and a route branch. **What it would buy is limited**: it tells
us the partner is still there, not whether it will leave in the next 2 s. With the corner route, a partner that is
there and stays is already safe; one that leaves late is the hazard, and the only safe answer is to wait south of
y 105 until it has gone, then about 1.5 s for it to pass x 40 (a 2-3 s wait, which costs TIP 3 in most runs: a
1.4 s slower route made TIP 3 in 1-2 runs of 60 without a partner's 4). Scouting answers the same question
before the match, for free: when does the partner's Auto leave the left start, and which way. Nothing built: this
needs the robot, and a checklist.

### Recommendation

1. **Keep the rule from "The qualifier Autos"**: an unreliable shooter gets L-Quals (58 if it shoots, 53 with 2
   TIPs if not; its partner starts on the other side and never crosses our way).
2. **If the extractor team can put the extractor at a front corner** without losing its seat (the FLOWER sits
   2.4 in from the V's right tip in the simulator), R-Quals becomes `qual-south-v-corner`: the same 56 with a good
   partner, and safe beside a partner that is dead at the left start or whose Auto stays there. **Scout the left
   partner's Auto**: run the corner route only with one that is off its start by about 6 s or stays put; one that
   leaves west along y 124 later than that still collides (56 of 60): give that partner L-Quals' pairing instead.
   Like R-Quals, it fires TIP 1's catch with the far FLOWER's 4 for TIP 2 and TIP 2's catch toward TIP 3, so if it
   becomes the R-Quals baseline the rule "a baseline never relies on a spill" (mentor, 8 Oct 2026) applies to it as
   it does to R-Quals today.
3. **Don't build the sensor yet.** Scouting tells us before the match what the sensor would tell us at 6 s, and the
   sensor's only answer to a late leaver is waiting, which costs TIP 3.

Open for the mentor: the extractor's corner (right, as here, or left with the robot seated facing east), and
whether its offset moves the transfer.

## 2. The whole match: four robots

Built by the simulator chat (`AutoSim.alsoRunOpponent`, claude/simulator 7ff8611): a study spec names the other
alliance after a `|`; each blue Auto is the red drawing turned half a turn about (70.75, 70.75), as a generated
Auto does for BLUE. The log carries all four robots (`/Odometry/OpponentA3d`, `OpponentB3d`; AdvantageScope:
`setup-advantagescope.ps1 -Open match`). Tested here with `tools/auto-routes/match.py` (L-Quals with
`partner-preloads-right-high`, R-Quals with `partner-left-v`; partners "spring hood" at 40); this branch adds the
time the two alliances first collide (`Result.alliancesCollidedAt`) and `BIOBUZZ_AUTO_ALLIANCE=BLUE` (our pair on
blue).

| Red | Blue | Red pts | Red 3 TIPs | Blue pts | Blue 3 TIPs | PARK (both) | Robots collide | Red meets blue | Into the other half |
|---|---|---|---|---|---|---|---|---|---|
| L-Quals | none | 75.0 | 58 | - | - | 120/120 | 0 | - | 0 |
| R-Quals | none | 74.3 | 56 | - | - | 120/120 | 0 | - | 0 |
| L-Quals | R-Quals | 73.3 | 54 | 75.7 | 59 | 240/240 | 0 | 0 | 0 |
| R-Quals | L-Quals | 75.0 | 58 | 72.3 | 53 | 240/240 | 0 | 0 | 0 |
| L-Quals | L-Quals | 73.7 | 56 | 74.0 | 55 | 240/240 | 0 | 0 | 0 |
| R-Quals | R-Quals | 74.3 | 56 | 76.0 | 60 | 240/240 | 0 | 0 | 0 |
| none | L-Quals | - | - | 73.7 | 54 | 120/120 | 0 | - | 0 |
| none | R-Quals | - | - | 74.0 | 55 | 120/120 | 0 | - | 0 |

- **The blue versions work**: both Autos make 3 TIPs on blue, PARK every run, and never touch the HIVE frame, a
  FLOWER, a wall or the centre line.
- **No contact between the alliances** in any of 240 matches, and no robot reaches into the other half (G402):
  our Autos stay on their own side of the HIVE, the opponents' too.
- The 3-TIP counts move by up to 4 runs between rows. That is the simulator's sensitivity, not the other
  alliance: the same seeds run on blue alone give 56 of 60 runs identical to red for L-Quals, and the other 4 lose
  TIP 3 (54 against 58). Answered by the simulator chat, 8 Oct 2026: the field and the Autos are a half turn
  (`FieldSymmetryTest`; two things were not and are fixed: blue's human NECTAR stepped +y, red's way, and a piece
  resting against the HIVE's side counted as touching it on one alliance and not the other, a rounding knife edge),
  but a seed's variety is drawn in the field frame on purpose, since one field is shared by both alliances: the
  tile slopes, the spill kicks and each piece's rolling resistance fall on different pieces for blue. So the same
  seed is a different run for blue, from the same distribution; `BIOBUZZ_AUTO_VARIETY=0` turns the variety off,
  and then red and blue match to the hundredth of a second. Read the matches' 3-TIP counts as ±4.

## 3. A wide extractor bar, and what a seat-position error costs (8 Oct 2026)

Asked by the body-designs chat for the mentor ("having to line up as close as we do is a real problem"): a T-shaped
bar out past the V's tips, so a FLOWER seats anywhere across it. Simulator (this branch): `RobotDesign`'s
`extractorLateralIn` .. `extractorLateralMaxIn` is the range the FLOWER's centre may sit in, plus the 1.5 in seat
tolerance at each end. **"rigid V, fixed turret, wide bar"** is -7.3 .. 7.3 in, a placeholder until CAD sends the
widest bar that clears the V; down, the bar is a solid strip across the seat line (1.5 in tall, a placeholder) that
pieces bounce off. **`seatErrorIn`**: each time the extractor comes down the robot's real seat is off by a lateral
error drawn evenly in ±N in (its own random, so nothing else in the run changes), and the FLOWER gives up pieces only
if it still falls in the extractor's reach. The robot does not retry: it waits its 3 s and leaves with what it has.
`tools/auto-routes/seat_error.py` runs the matrix. 60 runs, "rigid V, fixed turret", partners "spring hood" at 40, on
the simulator as of claude/simulator dd6a0d7.

| Auto (partner) | Extractor | ±0 | ±1 | ±2 | ±4 in |
|---|---|---|---|---|---|
| L-Quals (`partner-preloads-right-high`) | centre block (today's, ±1.5) | 57 | 57 | 32 | 9 |
| | wide bar | 57 | 57 | 57 | 57 |
| R-Quals (`partner-left-v`) | centre block | 56 | 56 | 30 | 9 |
| | wide bar | 54 | 54 | 54 | 55 |
| `qual-south-v-corner` (`partner-left-v`) | corner (-7.3 ± 1.5) | 55 | 55 | 29 | 9 |
| | wide bar, FLOWER at the bar's end (-7.3) | 55 | 55 | 44 | 30 |
| | wide bar, FLOWER 1.5 in inside it (-5.8, `qual-south-v-corner-in`) | 55 | 55 | 55 | 44 |

(Runs with 3 TIPs of 60. PARK 59-60 of 60 everywhere; no robot collisions with `partner-left-v`.)

- **The centre block needs the seat within about 1.5 in.** ±1 costs nothing; ±2 (a quarter of the seats miss)
  costs about 25 runs of 3 TIPs on every route; ±4 leaves 9. A missed FLOWER loses that TIP's 4 pieces outright.
- **The wide bar makes a centre seat immune to ±4 in**, on both qualifier Autos (L-Quals 57 at every error, R-Quals
  54-55).
- **The corner route sits at the bar's end**, so half of any error falls off it (44 at ±2, 30 at ±4). Aimed 1.5 in
  inside the end (robot at x 41.56 instead of 40.06) it holds 55 to ±2 and 44 at ±4, but the V's right tip is then
  at x 50.45, 0.45 in inside an 18 in partner's footprint at the standard left start. The simulator's robot-robot
  check uses the frames, not the V's tips, so that overlap is not counted: a wider bar, past ±7.3, is what lets the
  corner route have both.
- **Deployed, the bar hurts nothing measurable**: G409 the same runs on every route (L-Quals 0, R-Quals 17, corner
  18-20 of 60, with or without the bar), PARK the same, no new problems. R-Quals 54 against 56 is inside the ±4
  noise (section 2). It is down only on the approach to a FLOWER and while seated.
- **Beside a partner at the far FLOWER**, unchanged: the bar sits ahead of the body, so the corner route's body
  and its clearance are the same. One fix from this study: N_LOW, where the corner route fires TIP 2, moves from y
  115.5 to **114.5**. At 115.5 the back cleared an 18 in partner at its start by 0.2 in, and one that turned 1° to
  aim met it in 10 of 60 runs (the earlier runs had the partner on our 15.12 in body); at 114.5, 1 of 60, 3 TIPs
  unchanged (55 with a good partner, 32 with a dead one, 54 with a 3 s-late one that holds).

The model's limits: the error is lateral only (a seat short or long of the FLOWER is not modelled), the robot's
body is not moved by it (only where the FLOWER sits against the extractor), and the bar's width and height are
placeholders. **Rerun with CAD's width**: set `wideBar.extractorLateralIn` / `extractorLateralMaxIn` in
`AutoStudyTest` and run `python3 seat_error.py`.

**What the mentor took from it** (8 Oct 2026): the bar's case is made, and no more modelling is needed for it.
- *Front-to-back* needs no tolerance: the robot drives into the FLOWER's back poles, which set the distance.
- *Canting*: a longer bar resists twisting, so the bar should be as wide as fits, touching both back poles across its
  whole lateral range.
- *Separate jobs*: the bar has one job, the FLOWER. The V no longer helps take a FLOWER, so it can be shaped for
  spills alone.
- *The open work is CAD's*: where the servo and the bar's arms go, the space they take on the robot, and keeping a
  good V. The V and spill catching are where most development is still needed, and they interact with the bar's
  packaging. Sent to the CAD chat. The simulator reruns when its outline arrives.

Since section 1 was run the simulator has changed (claude/simulator's transfer and plate-contact models): the corner
route with a dead partner at the left start now makes 3 TIPs in 30-32 of 60 (35 then), with a good partner 55-56.
