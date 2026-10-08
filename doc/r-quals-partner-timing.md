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
  115.5) facing south**, the lane's top far enough south that the back (y 123.06) clears a partner at its start.
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
| shoots 3 s late, **then holds at its start until 18 s** | 56 / **59** | 49 / 0 | **55 / 0** |
| never shoots, **holds at its start until 18 s** | 37 / **60** | 1 / 5 | **35 / 6** |

PARK 60 of 60 in every row. Our own problem runs (HIVE frame, FLOWER, wall): none on the corner route.

What this says:
- **The corner extractor fixes a partner that sits at the standard left start**, dead or holding: from colliding
  in every run to none, for 2-3 runs of 3 TIPs, and nothing lost with a partner that does its job (56 either way).
- **It does not fix a partner that leaves late.** A partner leaving the left start for the LOADING ZONE drives west
  along y 124 (body y 115-133), across the far FLOWER's seat and everything north of y 105 on our side. It meets
  us wherever we are between 7 and 11 s, seat or no seat. The 6 remaining collisions with the holding partner are
  runs where TIP 1 missed and we were still north at 18-20 s.
- So the corner extractor needs one instruction to the left partner: **"If you have not left your start by 6 s,
  stay there until 18 s, then park."** A late partner that holds is then harmless (55 of 60), one that never fires
  costs what a non-shooting partner always costs (35-36). Without the corner extractor that instruction does
  nothing (59-60 collisions: the partner at its start is in our seat and N_FIRE).

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
1.4 s slower route made TIP 3 in 1-2 runs of 60 without a partner's 4). The sensor is worth building only if
partners won't follow the "hold until 18 s" instruction. Nothing built: this needs the robot, and a checklist.

### Recommendation

1. **Keep the rule from "The qualifier Autos"**: an unreliable shooter gets L-Quals (58 if it shoots, 53 with 2
   TIPs if not; its partner starts on the other side and never crosses our way).
2. **If the extractor team can put the extractor at a front corner** without losing its seat (the FLOWER sits
   2.4 in from the V's right tip in the simulator), R-Quals becomes `qual-south-v-corner`: the same 56 with a good
   partner, and safe from a partner that is dead or holds at the left start. It must come with the "hold until
   18 s" instruction to the left partner; a partner that leaves late along y 124 still collides (56 of 60).
3. **Don't build the sensor yet.** It only helps against a partner that ignores the instruction, and then by
   waiting, which costs TIP 3.

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
