# Three AUTO TIPs: can one robot do it, and what must a partner do (11 Nov 2026, study)

A study for the mentor, run in the simulator set to the Belding Qualifier (`BeldingCalibration`;
TeamCode/README.md, "Simulating an Auto"), 120 seeds a row, both Belding HIVE fields, master's
endgame guard. It builds on [`one-robot-two-tips.md`](one-robot-two-tips.md) and
[`belding-auto-review.md`](belding-auto-review.md). No robot code changes here.

## Where this ended (12 Nov 2026): read this first

**Verdict (mentor, 12 Nov): the drawings here are not being pursued.** The study's findings stand;
its Autos do not. Everything below this section is the working record, in the order it was done, and
parts of it are overtaken: where a number here disagrees with one further down, this section and
"Rerun on master's calibration" win.

**What was asked.** Can one of our robots make three AUTO TIPs alone, and if not, what is the least a
partner must do. Later: a better paired Auto than Infraviolet.

**What was found, and is worth keeping:**

- **Three TIPs take every POLLEN on our side of the field:** 19 landed of 20 launched (both preload
  sets, both FLOWERs, the GARDEN). There is no spare piece, so every pickup must deliver 4.
- **One robot as it ran at Belding cannot do it:** about 42 s of work.
- **Full FLOWER loads are the biggest lever on two TIPs:** 52 of 120 with Belding's logged loads,
  83 with full ones, nothing else changed. Then shots that stay in.
- **A shorter set feed buys no TIP.** The balls land when they land. The feed's length matters only
  as the backstop when the shot counter misses; the logs support about 3.0 s (a ball cut off in 1
  volley of 32) where 3.5 s is set.
- **A volley is about 2.5 s when the count ends it** (0.3 s wait, 0.78 s gate to first ball, three
  gaps of 0.47 s). A third TIP needs about 1.8 s. The 0.78 s before the first ball is where that
  would come from, not the spacing.
- **A CELL is shot only from its own end, within about 26 degrees of square.** The simulator scores
  by range only and let a 62 degree shot through.
- **There is one square spot in front of a CELL.** Two robots there need two spots side by side (one
  angled), or a hand-over by the clock that costs a second and breaks when the pace changes.
- **A robot that drives under the HIVE as a CELL tips takes the spill** (G409).
- **Run the stress cases** (a preload volley missed, a robot 20% slow or fast). Every contact the
  pair had in them was invisible in normal runs.

**Why the pair was dropped.** L-Pair-Flowers / R-Pair-Garden never crosses the field with a load,
which is why it has no contact and no spill. It pays with one overworked robot and one idle one: L
drives about 300 in and fires three volleys (12 pieces), R fires two (8, four of them from the GARDEN,
the pickup that works worst) and stands loaded for 10 s. Its third TIP needs a 1.8 s volley in the
Visualizer's preview. Infraviolet has made three TIPs on the robots; this has never driven.

**The drawings, and what each is good for:**

| Drawing (`TeamCode/autos/drafts/`) | Can a robot run it? | Keep for |
|---|---|---|
| `l-pair-flowers` + `r-pair-garden` | Yes, real cards | The blend with Clockwise; the stress-tested fallbacks and park roads |
| `r-pair-garden-sweep` | No (`SweepRow` is simulator-only) | What a GARDEN gather that takes 4 would buy |
| `r-two-tip-with` + `partners/partner-l-timed-fill-hide-park` | Yes, real cards | Not pursued either (mentor, 12 Nov). The partner is drawn wrong: it leaves the HIVE to the south and parks at `RIGHT_PARK`. Real partners that hide under the HIVE back out near the end and park on the left side, their own, so our robot's park at `LEFT_PARK` and its last lane are where the partner would be. Scored on the first calibration only |
| `r-three-tip`, `r-three-tip-stream`, `r-three-tip-still`, `l-three-tip` | No (simulator-only cards, a turret, a side shot) | The solo budget: what a different robot would need |

**If this is picked up again:**

1. Start from robot data, not the simulator: FLOWER loads per robot, whether the count ends the
   feed, gate to first ball. The checks are in `doc/robot-todo.md` section 7d.
2. Check the simulator against a real result first. It gives Infraviolet no third TIP in 24 runs,
   and the robots have made three. Its Belding rows carry Belding's short loads, the old counter's
   3.5 s feeds and a TIP lag with no fast case, so they read low for the robots as they are now.
3. Rerun before quoting: `TeamCode/README.md`, "Simulating an Auto", lists every knob used here
   (`BIOBUZZ_AUTO_STUDY="A,B@50"`, `BIOBUZZ_BELDING_ROBOT`, `BIOBUZZ_BELDING_WHOLE_FEED`,
   `BIOBUZZ_SIM_LAUNCH_ALL_FEED_S`, `BIOBUZZ_SIM_PRELOADS_MISS`, `BIOBUZZ_SIM_TYPICAL_LAUNCH_ALL`).
   Count contacts from each run's timeline (`BIOBUZZ_AUTO_TIMELINE=all`); a run without it hides them.
4. Say which clock a time is on and use the names in `TeamCode/autos/timing-terms.md`. This document
   predates them.
5. The other two studies of the same days: Clockwise (biobuzz#535, `TeamCode/autos/clockwise.md`) and
   the Square launch spots (biobuzz#536).

**The target:** in the same runs, 2 TIPs in at least 90 of 120 and 3 TIPs in at least 60 of 120.
Counts are TIPs complete by 30 s; a TIP that completes in the transition is not counted (see "How it
was run"). Differences under 5 of 120 are not differences.

## The answer

- **One robot as ours ran at Belding cannot make three TIPs.** The work adds up to about 42 s
  (the budget, below). In the simulator the three-TIP route makes 2 TIPs in 57 of 120 and no third,
  and the guard parks it after its third volley.
- **A partner that leaves its 4 preloads on the floor does not make the second TIP more likely,
  only earlier:** 57 of 120 at 26.0 s, against 58 of 120 at 28.5 s for R-Two-Tip alone. A robot
  fits three volleys in 30 s with or without the row, and the LEFT CELL needs all 8 of the two it
  gets. **Shot accuracy is what is missing, not pieces:** 0.98 of shots staying in gives 95 of 120
  with nothing else changed.
- **Three TIPs alone take a different robot, but not one that launches while moving.** Firing only
  while standing still, with a turret, 0.98 of shots staying in, a feed of 0.3 s a shot and 60 in/s or
  more, the solo route makes **2 TIPs in 96 and 3 TIPs in 84 of 120** (70 in/s). At Belding's 0.47 s
  a shot it misses by half a second (third TIP at 30.6 s). With launching while moving the 0.47 s feed
  is enough (96 and 87), but a moving shot that accurate is not something to plan on.
- **A pair of ours does it with no turret and no new mechanism.** One robot waits at the LEFT launch
  spot and fires its preloads as the LEFT CELL comes up, then the SCORING FLOWER's 4, then takes the
  WALL FLOWER's 4 to the RIGHT CELL; the other makes the first TIP, gathers the GARDEN beside its
  start and waits loaded. Real cards only, every volley from a stop, 50 in/s: **2 TIPs in 97 and
  3 TIPs in 85 of 120**, if the feed ends on the fourth ball, 0.98 of shots stay in, and the GARDEN
  gives 4. Infraviolet under the same assumptions makes 108 and 0.
- **For scouting: how early the partner's RIGHT TIP must come.** For three TIPs, the RIGHT CELL must be
  down by about **10 s** if the partner also brings 4 more to the RIGHT CELL later, and by about
  **7 s** if all it does is fire its preloads (and then only with our 0.3 s feed and 70 in/s). For
  two TIPs it can be as late as 12 s.
- **What is realistic:** the accuracy work and the shot counter are on the fix list and are what the
  second TIP needs anyway. The turret, a 0.3 s feed and a reliable GARDEN pickup are the new work.
- **The least a partner must do for two reliable TIPs is fire its 4 preloads into the far CELL
  once it is up, then park.** Beside that partner, our Belding robot on R-Two-Tip makes **2 TIPs in
  109 of 120**, the second at 23.0 s. A partner on a 9 s timer does as well as one that watches
  the HIVE. Of our Belding partners only 5292 and 10104 launched and scored in AUTO at all; nine of
  our ten qualifier partners launched nothing.
- **No Belding-style partner makes a third TIP possible for the robot as it ran at Belding.** It would have to make a whole TIP of 8
  by itself and early. No alliance at the event made two AUTO TIPs without one of our robots on it.
- **The one measurement that would most change the answer: count the balls that stay in from the
  LEFT launch spots.** Every result here turns on that share, and it has never been counted.

### Each rung, against the target

| Rung, as the goal sets it | Its drawing | 1 / 2 / 3 TIPs of 120 | Target (90 and 60) |
|---|---|---|---|
| **1: solo with a 360° turret and the allowed what-ifs**, partner only leaves its 4 preloads | R-Three-Tip-Stream | 120 / 107 / 99 | **Met** |
| **2: the smallest set of changes** | R-Three-Tip-Stream: turret, launching while moving, stay in 0.98, feed 0.47 s a shot, 70 in/s | 120 / 96 / 87 | **Met** |
| **3: the Belding robot, better pathing only, beside a Belding-style partner** | R-Two-Tip-With, partner fires its preloads into the far CELL | 120 / 109 / 0 | **2 TIPs met (109). 3 TIPs cannot be:** 0 of 120 in every pairing, as at Belding and with every shot staying in |

| **Solo, no moving shots** (added after the mentor's review) | R-Three-Tip-Still: turret, stay in 0.98, feed 0.3, 70 in/s | 120 / 96 / 84 | **Met** |
| **A pair of ours, real cards, no turret** (added after the mentor's review) | L-Pair-Flowers + R-Pair-Garden-Sweep: feed ending on the fourth ball, stay in 0.98, 50 in/s, the GARDEN giving 4 | 120 / 97 / 85 | **Met** |

Every "met" row is the simulator's, on the placeholders listed under "What the simulator rests on".

**Can today's robot run the rung 1 and rung 2 drawings? No, and this is why it cannot.** They meet the
target in the calibrated simulator, which is all a study can show. On the robot each of these is
missing, and without any one of them the simulator's third TIP is gone:

| The drawing needs | The robot today | Third TIPs of 120 without it |
|---|---|---|
| A turret that tracks a CELL while the robot drives | The turret ran locked forward at Belding | 0 |
| A shot fired while moving that stays in as often as a standing one | Nothing fires on the move; the kit waits for the robot to slow | 0 with no moving launch; 28 with an unled one |
| 0.98 of shots staying in | 0.92 at best, inferred, never counted | 13 |
| A feed that ends on the fourth ball, 0.5 s a shot or less | The counter undercounts after a pick, so the feed runs its whole 3.5 s | 0 |
| A card that picks up a row of 4 loose pieces in about 2.5 s | No such card; GARDEN gathers took 0 to 2 of 4 | 0 at half the sweep speed |
| Paths driven at 60 in/s or more | The Autos are drawn at 50 | 23 at 50 in/s (smallest set) |

So there is no drawing the robot can run today that meets the target, for rung 1 or rung 2, and no
drawing could be: the robot's own third TIP is out of reach by the bound below.

**The baseline the rungs are measured against, the robot as it ran at Belding:** R-Three-Tip, 117 / 57 /
0. That is not a drawing waiting to be improved. No drawing can make its third TIP (a bound with no
driving puts it at 30.4 s; at three times the drive speed with every shot in it is still 120 / 118 /
0), and its second TIP is 57 of 120 because all 8 LEFT pieces must stay in at 0.92.

## After the mentor's review: no moving shots, pairs, and what to scout

Three questions from the mentor on reading the study (11 Nov), each answered with runs. Everything
here fires **only while the robot is still** (`BIOBUZZ_AUTO_FIRE_STILL=1`), with the Belding calibration,
120 seeds, TIPs by 30 s. "Stay in 0.98" and the feed are set as in rung 2; picks are as at Belding.

### Is launching while moving necessary? No

[R-Three-Tip-Still](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-three-tip-still.pp,partners/partner-l-stage-leave-park.pp)
is the stream route with every volley fired from a stop: at the end of the partner's row, on the
SCORING FLOWER as its pieces come in, in the lane, and at the corner. The turret is what makes
that work, since the robot never turns or drives to a launch spot.

| Solo, turret, stay in 0.98, no moving shots | 1 TIP | 2 TIPs | 3 TIPs | TIP 2 at | TIP 3 at | Target |
|---|---|---|---|---|---|---|
| Feed 0.47 s a shot, 50 in/s | 120 | 96 | 0 | 17.4 s | | no |
| Feed 0.47, 60 in/s | 120 | 98 | 1 | 16.6 s | 30.7 s | no |
| Feed 0.47, 70 in/s | 120 | 96 | 15 | 16.6 s | 30.6 s | no |
| Feed 0.47, 70 in/s, picks fixed | 120 | 107 | 17 | 16.6 s | 30.6 s | no |
| **Feed 0.3, 60 in/s** | 120 | 98 | 66 | 15.3 s | 29.6 s | **met** |
| **Feed 0.3, 70 in/s** | 120 | 96 | 84 | 16.7 s | 28.7 s | **met** |
| Feed 0.3, 70 in/s, picks fixed | 120 | 107 | 94 | 16.7 s | 28.6 s | **met** |
| Feed 0.47, 70 in/s, turret locked | 120 | 0 | 0 | | | no |

Standing still costs about 2 s over the whole Auto. A 0.3 s feed pays for it; Belding's 0.47 s does
not, by about half a second. The locked-turret row is 0 because this route never points the robot at
a CELL; without a turret the robot is back to driving to a launch spot for each volley (rung 2's
"without the turret" row: 107 and 0).

### A better pair than Infraviolet

Infraviolet has the right idea, five loads between two robots, and it made three TIPs once on the
practice field. In the Belding-calibrated simulator it makes none, for one reason: its LEFT CELL waits
for a load driven up from the WALL FLOWER, so the second TIP lands at 18 to 20 s, and its own
deadlines (L gives up on the RIGHT CELL at 20 s, R at 22.5 s) then park both robots. They were set
when a TIP came within a second of the last ball; at Belding every CELL sat about 4 s first.

| Infraviolet | 1 TIP | 2 TIPs | 3 TIPs | TIP 2 at | TIP 3 at |
|---|---|---|---|---|---|
| As at Belding | 117 | 22 | 0 | 20.5 s | |
| Feed 0.47 ending on the fourth, stay in 0.98 | 120 | 108 | 0 | 18.2 s | |
| Feed 0.3, stay in 0.98, 60 in/s | 120 | 108 | 26 | 16.5 s | 27.3 s |

The new pair moves the second TIP earlier by having 4 pieces already beside the LEFT CELL when it
comes up, and by keeping the second robot loaded and waiting at the RIGHT CELL:
[L-Pair-Flowers and R-Pair-Garden](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/l-pair-flowers.pp,drafts/r-pair-garden.pp).
**It uses only cards the robot and the Visualizer already have** (`LaunchAll`, `PickFlower`,
`GardenGather`, the CELL-up waits), every volley is fired from a stop at a team launch spot, and
**neither robot needs a turret.**

| Time, one run (seed 5, row sweep) | L-Pair-Flowers | R-Pair-Garden |
|---|---|---|
| 0 to 2.5 s | Drives 1.1 s from `LEFT_START` to `LEFT_LAUNCH_OFFSET`. Waits | Volley 1: 4 preloads at the RIGHT CELL from `RIGHT_START` |
| 2.5 to 9 s | Waits for the LEFT CELL | Gathers the GARDEN (4.5 to 6.9 s), drives to `RIGHT_PAIR_CATCH` (61, 13), 6 degrees off square to the RIGHT CELL, waits loaded |
| 6.2 s | **TIP 1.** Volley 2: 4 preloads at the LEFT CELL (to 7.6 s) | |
| 7.6 to 12.5 s | SCORING FLOWER pick, back to `LEFT_LAUNCH_OFFSET` | waits |
| 12.5 to 14.0 s | Volley 3: the FLOWER's 4 at the LEFT CELL | |
| 14.0 to 22.1 s | **TIP 2 dwell:** wall lane to the WALL FLOWER, picks 4 (full at 20.1 s), drives south to `RIGHT_LAUNCH_OFFSET` (41, 20), beside R's spot | |
| 18.7 s | | **TIP 2.** Volley 4: the GARDEN's 4 at the RIGHT CELL (to 20.1 s) |
| 20.1 s on | | Leaves straight under the HIVE and parks at `LEFT_PARK`, the far end |
| 22.1 to 23.9 s | Volley 5: the WALL FLOWER's 4 at the RIGHT CELL, 21 degrees off square from 44 in | |
| 23.9 s on | Drives up the wall lane and parks at `RIGHT_PARK`, the near end | |
| 28.1 s | **TIP 3** (29.0 s on average) | |

Those are one run's times; the averages are in the table below.

### Rerun on master's calibration (12 Nov): read these numbers first

Master moved while this study ran, and the branch now has it merged. Three things changed what the
pair scores, so **the counts in this section replace every other count for the pair in this
document**; the rest are from the earlier calibration and are kept for the comparisons they make.

- **FLOWER loads are drawn from the Belding logs** (`BeldingCalibration.load`, biobuzz#528): the
  pieces a volley after a pick actually fired. 19429's loads were often short (WALL 0, 2, 4, 2, 1);
  20245's were full but once. The earlier calibration had one pick in seven come back empty.
- **Stay-in by range is 0.70 / 0.95 / 0.90** (36 / 42 / 54 in), where it was 0.65 / 0.92 / 0.82.
- **`RIGHT_PARK` is 3 in deeper** (y 98.7); L's park in the drawing moved with it.

The pair as drawn (the blend), 120 runs, counted by 30 s on the robot's clock:

| Pace and loads | 2 TIPs | 3 TIPs, own GARDEN gather | 3 TIPs, GARDEN gives 4 | Both parked | Robots touch | G409 |
|---|---|---|---|---|---|---|
| As at Belding, logged loads | 52 | 0 | 0 | 120 | 0 | 3 to 8 |
| As at Belding, every FLOWER load full | 83 | 0 | 0 | 120 | 0 | 3 to 8 |
| Shot counter and stay in 0.98, logged loads | 63 | 6 | 30 | 120 | 0 | 0 |
| **Shot counter and stay in 0.98, every FLOWER load full** | **108** | 17 | **88** | 120 | 0 | 0 |

So the target (two TIPs in 90 and three in 60, of 120) is met only in the last row, with a GARDEN
that gives 4. **The pair now needs four things, not three:** a feed that ends on the fourth ball,
shots that stay in, a GARDEN gather that takes 4, and **FLOWER picks that deliver 4 to the launcher
every time**, which only 20245 did at Belding. The loads are the biggest single lever on two TIPs:
52 to 83 at Belding's pace with nothing else changed.

The stress runs again, 60 each, logged loads: with either preload volley missed, or either robot 20%
slow or fast, at both paces, both robots park and nothing touches in all 14 rows, and a missed R
preload volley still gets TIP 1 in 60 of 60. A missed preload volley still costs TIP 2 in every run.
**At Belding's pace R is hit by a spilled piece (G409) in 8 of 60 normal runs and 18 of 60 with R
slow:** it leaves under the HIVE as the RIGHT CELL goes down. With the shot counter it is 0.

Three cautions on the third-TIP counts, from the timing standard that is now binding
(`TeamCode/autos/timing-terms.md`):

- **Which clock.** "By 30 s" here is the robot's own clock, from its PLAY. In Belding's playoffs our
  PLAY came 0.9 to 4.0 s after the field's start, and the field's clock is the one that scores.
- **The TIP lag has a slow tail the simulator does not.** From last ball leaves to CELL settled, a
  CELL at its tipping weight (this pair lands exactly 8) took 3.4 to 6.5 s on film, median 3.9; the
  simulator's is 3.9 to 4.5 s. The pair's third TIP settles at 29.0 s on average.
- So read the three-TIP counts as upper bounds. The two-TIP counts are not at risk from either:
  TIP 2 settles at about 19 s with the shot counter and 22 s at Belding's pace.

**Words.** This document was written before the standard and uses "volley time" for the whole
`LaunchAll` step (the standard's "volley"), "feed" as the standard does, and "dwell" for the part of
the TIP lag before the swing. Where it says a TIP comes "4.75 s after the deciding piece", read
"TIP lag, about 4.1 s from last ball leaves, up to 6.7 s".

### The blend with R-Clockwise / L-Clockwise (11 Nov)

The mentor asked this study and the Clockwise session (biobuzz#535) whose pair is better. Both
sessions' answer: neither outright, and the pair above is now the blend. Run on the same footing by
each session on its own branch (feed ends on the fourth ball at 0.47 s a shot, stay in 0.98, the
robot's own GARDEN gather, 120 runs, by 30 s), before the blend:

| | This pair | Clockwise |
|---|---|---|
| 2 or more TIPs | 97 | 79 |
| 3 TIPs | 12 to 20 | 15 (40 with every FLOWER load full) |
| A robot hit by a spilled piece under the HIVE (G409) | 0 | 35 to 50 |
| Both parked, no contact | 120 | 120 |
| A missed preload volley | not handled | TIP 1 still comes in 23 of 24 |
| Run with a slow robot or a missed preload volley | no | yes |

**Kept from this pair:** the division of the field. L fills the LEFT CELL alone and takes both
FLOWERs; nobody crosses the field with a load, so nobody drives under a CELL as it tips, and both
robots' LEFT accuracy is not needed for TIP 2.

**Taken from Clockwise:**

- **Two spots side by side at the right end, not one shared by the clock.** R stands on Clockwise's
  `RIGHT_PAIR_CATCH` (61, 13; 6 degrees off square) and L on the team's `RIGHT_LAUNCH_OFFSET`
  (41, 20; 21 degrees off square, the angled spot Belding's Square volleys used most: 65 balls out,
  none counted in). That gives back the second the shared spot cost.
- **A TIP 1 fallback, in two parts.** If the RIGHT CELL is still up when R comes back from the GARDEN
  (before 16 s), R's preloads did not tip it: R fires the GARDEN's pieces at it at once and parks. If
  the LEFT CELL is not up by 12 s, L drives the wall lane to the right end and fires its preloads at
  the RIGHT CELL if it is still up, then waits until 24 s so R has parked, and parks.
- **Its stress runs:** either preload volley missed, either robot 20% slow or 20% fast.

**Not taken:** the TIP 2 rescue (Clockwise's R fires a spare load at LEFT; here R never goes to that
end loaded), the one-way loop (its session found the contacts were at parks, not head-on in a lane),
and the INIT menu. Its `LEFT_PAIR_OFFSET` is 32.8 degrees off square and is not used.

| Of 60 runs each (own GARDEN gather) | TIP 1 | 2 TIPs | 3 TIPs by 30 s | Both parked | Robots touch | G409 |
|---|---|---|---|---|---|---|
| Belding's pace, normal | 60 | 30 | 0 | 60 | 0 | 2 |
| Belding's pace, L's preloads miss | 59 | 0 | 0 | 60 | 0 | 0 |
| Belding's pace, R's preloads miss | 60 | 0 | 0 | 60 | 0 | 4 |
| Belding's pace, L or R 20% slow or fast (4 rows) | 60 | 30 | 0 | 60 | 0 | 0 to 8 |
| Shot counter, normal | 60 | 49 | 9 | 60 | 0 | 0 |
| Shot counter, L's preloads miss | 60 | 0 | 0 | 60 | 0 | 0 |
| Shot counter, R's preloads miss | 60 | 0 | 0 | 60 | 0 | 0 |
| Shot counter, L or R 20% slow or fast (4 rows) | 60 | 49 | 6 to 11 | 60 | 0 | 0 |

What the stress runs found on the way, each fixed in the drawing: R's exit under the HIVE passed L's
stations at the left end, which is only safe once L has gone south, so after a rescue volley R takes
a different way (under the HIVE, sideways along y 100.5, into `LEFT_PARK` from the south); L's
give-up park and its lane south clipped a robot already on `LEFT_PARK`; and a first version of L's
fallback left late and met R in the lane. **What is still open:** a missed preload volley costs TIP 2
in every run, since nothing here makes up the four pieces; at Belding's pace R is hit by a spilled
piece in up to 8 of 60 runs with R slow (it drives under the HIVE as a CELL tips); R's rescue road
has 2 in on each side between the foot bar's end and L's station; and a dead robot on a start spot
was not run.

**How the pair got here** (mentor's review, 11 Nov, in order):

1. The first drawing had L fire its last 4 at the RIGHT CELL from the wall lane, (26, 40), 62
   degrees off square. The simulator scores a shot by range only and let it through. The mentor's
   verdict was that it never goes in, and the Square launch spots work (biobuzz#536) agrees: the
   steepest shot ever fired in a log is about 33 degrees and its spots are capped at 26. **Every
   volley is now fired from the CELL's own end of the field.**
2. It also had R follow L up the wall lane to park. At Belding's volley time L runs 3 s later and
   the robots touched in 66 of 120 runs, as the mentor saw in the Visualizer. **The wall lane and
   `RIGHT_PARK` are L's; R leaves under the HIVE to `LEFT_PARK`.**
3. L's last volley moved to the audience wall, 24 degrees off square at 54 in (commit `f6fd8daa`),
   then, since nothing has measured that shot, onto R's square spot in turn by the clock, which cost
   a second and was only safe at one pace:

| With the shot counter, stay in 0.98, 50 in/s, of 120 | 2 TIPs | 3 TIPs by 30 s (GARDEN gives 4 / own gather) | TIP 3 at | Robots touch |
|---|---|---|---|---|
| L on its own spot at the wall (24 degrees, 54 in), R square | 97 | 82 / 20 | 28.7 s | 0 |
| One square spot, L drives in as soon as it is full | 97 | 73 / 16 | 29.0 s | 35 / 14 |
| One square spot, L waits for the clock | 97 | 53 / 12 | 29.8 s | 0 |
| **The blend: `RIGHT_PAIR_CATCH` and `RIGHT_LAUNCH_OFFSET` side by side (as drawn now)** | 97 | **79 / 15** | 29.0 s | 0 |

| Of 120 runs, both R drawings, the blend | At Belding's pace | With the shot counter |
|---|---|---|
| The two robots touch | 0 | 0 |
| A robot touches the HIVE frame or crosses the centre line | 0 | 0 |
| A robot is not parked at 30 s | 0 | 0 |
| A robot is hit by a spilled piece before it lands (G409) | 0 to 2 (R) | 0 |

**No stay-in share by angle exists**, for any spot: the logs count balls leaving, and the calibration
is by range from near-square shots. The pair's last volley is 21 degrees off square, and its two LEFT
volleys from `LEFT_LAUNCH_OFFSET` are 27. The simulator also flags every FLOWER pick as "drives into
a FLOWER"; that is the pick itself reaching the holder, and master's Autos get the same flag.

**In the Visualizer** (swept over both sliders on both robots, 11 Nov, for the shared square spot
drawing; not swept again for the blend, which is about a second quicker):

| Volley time | Intake full 0 s | 0.5 s | 1.0 s | 1.9 s (default) |
|---|---|---|---|---|
| 1.5 s | 3 TIPs | 3 | 3 | 3 |
| 2.0 s | 3 TIPs | 3 | 2 | 2 |
| 2.2 s | 2 TIPs | 2 | 2 | 2 |
| 2.5 s to 4.0 s (default) | 2 TIPs | 2 | 2 | 2 |

So the shared square spot needed a volley of 2.0 s or less. Belding's balls left 0.47 s apart after a
first ball at 0.75 s, which is 2.2 s to the fourth ball even with a feed that stops on it: not quite
enough. A 1.5 s volley is 0.3 s a shot.

**A shorter set feed does not buy a TIP** (mentor's question, 11 Nov). The Belding logs: 0.78 s from
gate open to the first ball, 0.47 s between balls, so the fourth leaves at 2.19 s; the counter saw
all four in 9 of 38 volleys and every other feed ran its 3.5 s. The last ball the launcher saw had
left by 2.8 s in 29 of 32 volleys and by 3.1 s in 31. Run with the counter failing after the first
volley, as at Belding, balls 0.47 s apart, 120 runs (`BIOBUZZ_SIM_LAUNCH_ALL_FEED_S`):

| Set feed | 2 TIPs, Belding's accuracy / 0.98 | 3 TIPs by 30 s, angled spot | 3 TIPs by 30 s, shared square spot | Robots touch, shared spot |
|---|---|---|---|---|
| 3.5 s (today) | 59 / 97 | not run | 0 / 0 | 8 / 7 |
| 3.1 s | 59 / 97 | 0 / 0 | 0 / 0 | 11 / 21 |
| 2.8 s | 59 / 97 | 0 / 0 | 0 / 0 | 14 / 21 |
| ends on the fourth ball (2.2 s) | 58 / 97 | 27 / 82 | 19 / 53 | 4 / 0 |

Two TIPs do not move: the balls land when they land, and only the card ends sooner. The third TIP
needs the 2.2 s volley, which only the counter gives. And the shared square spot is only safe at the
pace its clock times were set for: with a longer volley R is still on the spot when L drives in. The
simulator's balls always leave on time, so it does not show the cost of a short feed, a ball left in
the robot (3 of 32 volleys at 2.8 s).

| The pair, real cards, no turret | 1 TIP | 2 TIPs | 3 TIPs | TIP 2 at | TIP 3 at | Target |
|---|---|---|---|---|---|---|
| **Feed ends on the fourth ball (0.47 s a shot), stay in 0.98, 50 in/s, the GARDEN gives 4** | 120 | 97 | 79 | 19.2 s | 29.0 s | **met** |
| the same, but the GARDEN by the robot's own routine as the simulator runs it (it takes 2) | 120 | 97 | 15 | 19.2 s | 29.0 s | no |
| the same, with the row sweep at 3 in/s | 120 | 97 | 32 | 19.2 s | 28.1 s | no |
| Belding's feed (the whole 3.5 s) | 120 | 97 | 0 | 22.1 s | | no |
| Feed 0.3 s a shot | 120 | 97 | 84 | 17.9 s | 26.0 s | **met** |
| 40 in/s (first drawing; not rerun, and with 1.3 s to spare at 50 in/s the new one will do worse) | 120 | 97 | 84 | 19.5 s | 28.7 s | not known |
| 60 in/s | 120 | 97 | 85 | 19.0 s | 27.1 s | **met** |
| FLOWER picks fixed | 120 | 108 | 94 | 19.2 s | 27.8 s | **met** |
| Stay in 1.0 | 120 | 108 | 108 | 19.2 s | 27.8 s | **met** |
| Stay in 0.96 | 120 | 80 | 61 | 19.2 s | 27.8 s | no |
| Belding's accuracy | 120 | 58 | 28 | 19.3 s | 28.0 s | no |
| Everything as at Belding | 120 | 58 | 0 | 22.2 s | | no |

The top two rows and the last row are from the drawing as it is now (11 Nov: the blend). **Every other row is from the first drawing**, whose last
volley was the side shot and whose ending was 0.7 s quicker: read those as upper bounds. The margin is
the thing to notice: TIP 3 comes 1.0 s before the buzzer on average, so anything that costs a second
(a slower drive, a slow pick) takes third TIPs away.

**The pair needs three things, and no new mechanism:**

1. **A feed that ends on the fourth ball.** With the whole 3.5 s feed the third TIP never fits. This is
   the shot counter, already on the fix list.
2. **0.98 of shots staying in.** The same accuracy work as everything else.
3. **4 pieces out of the GARDEN.** The "gives 4" rows use the simulator's `SweepRow` in R-Pair-Garden-Sweep,
   a placeholder. The robot's own `GardenGather`, as the simulator runs it, takes 2, and in practice it
   took 0 to 2. R has 12 s of waiting beside the GARDEN, so it can afford a slow gather or a second pass.

Drive speed hardly matters (40 to 60 in/s all meet the target). With a turret on L and its volleys
fired while the FLOWER feeds, an earlier version of this pair had 5 s to spare instead of 2
(98 and 88, third TIP at 24.6 s); that version used simulator-only cards, so the Visualizer could not
preview it, and it is not kept.

### What to scout: how early the RIGHT TIP has to come

Two kinds of partner, with the partner's preload volley delayed a second at a time. The first TIP
comes in 120 of 120 runs in every row of both tables. "RIGHT CELL
down" is when the first TIP completes; a partner's last preload leaves about 4.5 s before that.

**A partner that only fires its preloads at the RIGHT CELL and parks; we do the rest alone**
([L-Three-Tip](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/l-three-tip.pp,partners/partner-r-tip-far-park.pp):
the SCORING FLOWER burst, then the WALL FLOWER, then the GARDEN):

| RIGHT CELL down at | Feed 0.47, 60 in/s: 2 / 3 TIPs | TIP 3 at | Feed 0.3, 70 in/s: 2 / 3 TIPs | TIP 3 at |
|---|---|---|---|---|
| 6.1 to 6.4 s | 98 / 30 | 30.2 s | 99 / 89 | 28.0 s |
| 7.2 to 7.4 s | 98 / 2 | 31.0 s | 99 / 81 | 29.0 s |
| 8.2 to 8.4 s | 98 / 0 | | 98 / 50 | 30.0 s |
| 9.2 to 9.4 s | 98 / 0 | | 98 / 4 | 30.8 s |
| 10.2 to 12.4 s | 98 / 0 | | 98 / 0 | |

**A partner that also brings 4 more to the RIGHT CELL when it comes back up** (as R-Pair-Garden does;
feed 0.47, 50 in/s; measured on the earlier turret version of the pair, which had 3 s more to spare, so
take about 3 s off each line for the pair as it is now: RIGHT CELL down by about 7 s):

| RIGHT CELL down at | 2 / 3 TIPs | TIP 3 at |
|---|---|---|
| 6.4 s | 98 / 88 | 24.6 s |
| 7.4 s | 98 / 88 | 25.6 s |
| 9.4 s | 98 / 88 | 27.6 s |
| 10.4 s | 98 / 86 | 28.6 s |
| 11.4 s | 98 / 53 | 29.6 s |
| 12.4 s | 98 / 34 | 30.4 s |

**The rules of thumb for a scout:**

- Every second the first TIP is late is a second off the end. The third TIP comes about 18 s after the
  first with a partner that brings a second load, and about 22 to 24 s after it when we do the rest.
- **Three TIPs with a partner that brings a second load:** RIGHT CELL down by 10 s, which is its last
  preload out by about 5.5 s with at least 3 staying in.
- **Three TIPs with a preloads-only partner:** RIGHT CELL down by 7 s (last preload out by about 2.5 s),
  and only if we have the 0.3 s feed and 70 in/s. Not viable with a 0.47 s feed.
- **Two TIPs:** RIGHT CELL down by 12 s is enough (98 of 120 at every delay tried).
- Count how many preloads stay in. At Belding the two TIPs that came late (M-7 red, M-9) were the two
  where exactly 3 stayed; the CELL then sat about 6 s.
- **Against those lines, on the field's clock** (our PLAY was 1.3 to 4.0 s late in the playoffs, so add
  that to the table in rung 3): 10104's RIGHT CELL was down at about 6.5, 8.0 and 11.1 s; 5292's at
  about 9.1, 9.3, 9.3 and 13.2 s. Both would do for a second-load plan most of the time; only 10104,
  twice, was early enough for a preloads-only plan. Neither brought a second load.

### Which drawings the Visualizer can preview

The Visualizer's preview only knows the robot's real cards. A drawing that uses a simulator-only
card previews wrongly there (the robot appears never to fire, or never to pick up), though it runs
in the Java simulator.

| Drawing | Cards | Previews in the Visualizer |
|---|---|---|
| R-Two-Tip-With, and its partners | real | yes |
| L-Pair-Flowers + R-Pair-Garden | real | yes: 2 TIPs at the default volley time, 3 at 2.2 s |
| R-Pair-Garden-Sweep | `SweepRow` | the path, not the GARDEN pickup |
| R-Three-Tip, R-Three-Tip-Stream, R-Three-Tip-Still, L-Three-Tip | `SweepRow`, `StreamOn` | the paths only; the preview shows 1 TIP |

## The budget (step 0)

Everything here is from the Field Setup Guide's HIVE calibration as the simulator holds it
(`HiveCalibration`), the field model's staged pieces, and the Belding calibration.

### Pieces

| | What it holds | To TIP it |
|---|---|---|
| RIGHT CELL at the start | 3 NECTAR (4.95 POLLEN weights; a NECTAR weighs 1.65) | 3 POLLEN landed (the Guide: tips on 3 NECTAR + 3 POLLEN, holds on 3 + 2) |
| LEFT CELL after TIP 1 | nothing: a CELL dumps everything when it goes down | 8 POLLEN landed (the Guide: tips on 8, holds on 7). At Belding, Field 1 tipped on 7 two times in three; Field 2 needed 8 |
| RIGHT CELL after TIP 2 | nothing | 8 POLLEN again |

So three TIPs take **19 POLLEN landed** (17 on a field that tips light) from **20 launched**: five
full loads of 4, with one miss allowed, and only in the first volley.

| Source one robot can reach | Pieces | Where (red, inches) | Time to take it |
|---|---|---|---|
| Our preloads | 4 POLLEN | in the robot | none |
| The partner's preloads, left on the floor | 4 POLLEN | where we choose, touching its start | a sweep: 1.3 to 2.6 s in the simulator, a placeholder |
| WALL FLOWER | 4 POLLEN | (2.7, 47.4), on our alliance wall | 3.6 s at Belding (push 0.9, dwell 1.8 to 2.0, back 0.6) |
| SCORING FLOWER | 4 POLLEN | (47.4, 138.8), on the far wall | 3.6 s; 2 picks in 14 came back with nothing |
| GARDEN | 4 POLLEN | along the audience wall from our corner, (1.5 to 10.2, 1.5) | master's typical 5 s; in practice the gather took 0 to 2 of 4 (#508) |
| TIP 1's spill | 3 NECTAR + 3 or 4 POLLEN | scattered over our half within seconds (at 15 s, anywhere from y 4 to y 139) | cannot be planned on |
| Human NECTAR | 1 per TIP (G426.A) | the LOADING ZONE, about 2 s after each TIP | a loose-piece pick; worth 1.65 |

That is exactly 20 POLLEN: both sets of preloads, both FLOWERs and the GARDEN. **Three TIPs use
every POLLEN on our side of the field.** The spill and the human NECTAR are the only spare weight.

### Time

Each TIP waits out a dwell: at Belding the CELL sat 3.6 to 4.2 s after the piece that took it over,
then swung for 0.6 to 1.1 s. Call it 4.75 s from the deciding piece to the TIP. The CELLs take
turns, so the chain is fixed:

```
deciding preload lands     2.7 s   (Belding: the preload volley ended at 2.6 to 4.2 s)
TIP 1                      7.5 s   (video: 5 to 8 s)
8 pieces into LEFT         takes L seconds
TIP 2                      7.5 + L + 4.75
8 pieces into RIGHT        takes R seconds
TIP 3                      17.0 + L + R, by 30 s
```

So **L + R is at most 13 s**: thirteen seconds to land sixteen pieces, in two bursts, each starting
the moment its CELL comes up. The driving and the picking have to happen before TIP 1 and during the
two dwells.

What the Belding robot needs for the same sixteen pieces, one thing at a time:

| | Belding | Seconds for 3 TIPs |
|---|---|---|
| Five volleys | 2.6 to 4.2 s for the preloads; 3.6 to 4.0 s after a pick (the feed ran its whole set time) | about 18 |
| Four pickups | two FLOWER picks at 3.6 s, a GARDEN gather at 5 s, the partner's row | about 14 |
| Driving between them | R-Two-Tip's three-volley route drives 9.2 s; five volleys at both ends drive more | at least 10 |
| **Total** | | **about 42 s** |

**As the robots ran at Belding, three TIPs miss 30 s by about 12 s, before any piece misses.** The
simulator agrees: the route below, run as the Belding robot, parks after its third volley because
the cards left would end at 40 s.

What would have to be true for it to fit:

- **Volleys that overlap the driving** (launching while moving), or the 18 s of volleys has nowhere
  to go.
- **A volley of about 2 s:** 0.3 to 0.5 s a shot, ending on the fourth. Belding's balls left 0.47 s
  apart after a first ball at 0.75 s, so the spacing is there; the 3.5 s set feed is what costs.
- **Every piece staying in:** 16 of 16 after the preloads on a field that needs 8. At Belding's
  0.92 that is about 1 run in 4 even with unlimited time; at 0.98 it is 3 in 4.
- **A loose row picked up in about 2 s, twice** (the partner's and the GARDEN's).

## Is there one spot for both CELLs?

No. The CELLs open away from each other: the RIGHT CELL toward the audience wall, the LEFT CELL
toward the far wall. `BothCellsSpotScanTest` fires 40 POLLEN from each spot of a grid along our
alliance wall at each CELL, at the speed the range needs:

| Spot (x 16 to 28) | RIGHT CELL keeps | LEFT CELL keeps |
|---|---|---|
| y 40 | 100% | 0 to 5% |
| y 46 | 65 to 100% | 0 to 3% |
| y 52 to 88 | 0 to 70%, falling to 0 by y 58 | 0 to 30%, from y 88 |
| y 94 | 0% | 45 to 100% |
| y 100 | 0 to 5% | 88 to 100% |

From between the CELLs a ball arrives side-on and hits the HIVE. **The shortest track is about
60 in along the wall lane, from about (20, 40) to about (20, 100)**, both in measured range (44.9 and
42.8 in). That lane also holds the WALL FLOWER and ends at the LOADING ZONE. The shares are the
simulated CELL's (the field CAD's opening and a 75° launch); nobody has fired at a CELL from its
side.

The route below does not use the lane for both CELLs, because the pieces are not there: the
SCORING FLOWER and the partner are at the far wall, the GARDEN in the audience corner. It fires at
the LEFT CELL from the far-wall side and at the RIGHT CELL from the lane and the audience corner.

## Rung 1: the drawing

Two drafts of one route (`TeamCode/autos/drafts/`), beside the stand-in
`partners/partner-l-stage-leave-park`:

- [R-Three-Tip](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-three-tip.pp,partners/partner-l-stage-leave-park.pp):
  every volley a `LaunchAll` from a stop. What the Belding robot could run, given a card for the row.
- [R-Three-Tip-Stream](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-three-tip-stream.pp,partners/partner-l-stage-leave-park.pp):
  the same stops with the launcher streaming between them, drawn at 70 in/s. The what-if robot's.

**The partner** starts on the far wall at (26, 133.8), west of the SCORING FLOWER, with its 4 POLLEN
in a row along its field side (x 35.4, y 129.6 to 138.0), each touching it (G304). At PLAY it
drives to the LOADING ZONE, 1.4 s, away from the row and from every path of ours. It launches
nothing.

**Our robot, R-Three-Tip-Stream, with every what-if on** (times from one run; the turret's angle is
from straight ahead, negative to the robot's right):

| Time | Where | What | Turret |
|---|---|---|---|
| 0 to 1.9 s | `RIGHT_START` | Volley 1: 4 preloads at the RIGHT CELL | −3° |
| 1.9 to 4.5 | under the HIVE, x 58 to 65, to (35.4, 120) facing the far wall | drive; the turret swings to the LEFT CELL as it passes y 91 | −150° |
| 4.5 to 6.1 | along the row | sweep the partner's 4 | |
| 6.1 to 7.1 | (35.4, 129) | **TIP 1 dwell:** wait for the LEFT CELL (TIP 1 at 6.1 s on average) | −150° |
| 7.1 to 8.0 | to `SCORING_FLOWER_STAGING` | Volley 2 on the move: the row's 4 at the LEFT CELL | −150° to −160° |
| 8.0 to 11.1 | on the SCORING FLOWER | pick, and Volley 3 as the pieces come in: 4 at the LEFT CELL | −165° |
| 11.1 to 13.6 | wall lane to `WALL_FLOWER_STAGING` | **TIP 2 dwell** (TIP 2 at 14.1 s): drive | |
| 13.6 to 16.5 | on the WALL FLOWER | still the dwell: pick 4 | |
| 16.5 to 17.9 | (26, 40) in the lane | Volley 4: 4 at the RIGHT CELL, standing | −152° |
| 17.9 to 19.2 | to (22, 8) along the audience wall | drive | |
| 19.2 to 21.8 | along the GARDEN | sweep its 4; the launcher holds fire beyond 60 in | −127° to −133° |
| 21.8 to 23.4 | to `RIGHT_LAUNCH_CORNER` | Volley 5 on the move: 4 at the RIGHT CELL | −121° |
| 23.4 to 26.6 | to `RIGHT_PARK` | **TIP 3 dwell** (TIP 3 at 26.9 s): park | |

The turret works between straight ahead and 165° to the right. **It does not need unlimited
travel:** the run with today's 393° window scores the same.

| | 1 TIP | 2 TIPs | 3 TIPs | TIP 2 at | TIP 3 at |
|---|---|---|---|---|---|
| R-Two-Tip alone (the 10 Nov drawing) | 119 | 58 | 0 | 28.5 s | |
| **R-Three-Tip, the Belding robot, partner leaves 4** | 117 | 57 | 0 | 26.0 s | |
| **R-Three-Tip-Stream, every what-if on** | 120 | **107** | **99** | 14.1 s | 26.9 s |

The what-ifs, and the number each is set to: turret with unlimited travel; 70 in/s; feed 0.3 s a
shot ending on the fourth; FLOWER picks with no extra dwell and none short; 0.98 staying in from
every range; launching while moving, led for the robot's speed.

### Why the Belding robot cannot, shown by a run

The 57 of 120 is luck (8 of 8 have to stay in). The 0 of 120 is time, and no luck changes it:

| R-Three-Tip, the Belding robot, partner leaves 4 | 1 TIP | 2 TIPs | 3 TIPs | TIP 2 at |
|---|---|---|---|---|
| As at Belding | 117 | 57 | 0 | 26.0 s |
| **Every shot stays in and every pick comes back full; the timing as at Belding** | 120 | 117 | **0** | 26.1 s |
| the same, with the guard as the Belding build ran it (a volley before the park) | 120 | 117 | 0 | 26.1 s |

The robot's third volley ends at 21.9 s. From there the cards to the next volley are 10.2 s (the
drive to the WALL FLOWER, the pick, the drive to the lane), the volley 4.0 s and the park 3.8 s:
39.9 s, so the guard parks. Even without a guard, the fourth volley would end at about 36 s and the
RIGHT CELL would hold 4 of the 8 it needs. **No drawing fixes this for the Belding robot:** the second
TIP cannot come before about 22 s (8 pieces, two volleys of 4 s and a pickup between them, after TIP 1
at 7.5 s, then the 4.75 s dwell), and a third needs its deciding piece in by about 25 s.

### No drawing can: a bound that does not depend on the route

Take the best value of everything measured at Belding, give the robot no driving at all, and let both
reloads be a loose row at the simulator's fastest sweep:

```
TIP 1, the earliest seen in 17 runs                               5.0 s
volley of 4 (3.6) + reload (1.3) + volley of 4 (3.6)              8.5 s   last LEFT piece at 13.5 s
shortest dwell and swing (3.6 + 0.6)                              4.2 s   TIP 2 at 17.7 s
volley of 4 (3.6) + reload (1.3) + volley of 4 (3.6)              8.5 s   last RIGHT piece at 26.2 s
shortest dwell and swing                                          4.2 s   TIP 3 at 30.4 s
```

That is past 30 s before the robot has driven an inch, and only one of the two reloads can be a row:
the other is a FLOWER pick (3.6 s) or the GARDEN (5 s), which puts it at 32.7 s or later. **So the
limit is the Belding volley, not the drawing.** A run agrees: the same robot driving three times as
fast, with every shot staying in and every pick full and quick, under any guard.

| R-Three-Tip, turret locked, partner leaves 4 | 1 TIP | 2 TIPs | 3 TIPs | TIP 2 at |
|---|---|---|---|---|
| 150 in/s, every shot in, every pick full with no extra dwell | 120 | 118 | **0** | 23.1 s |
| the same, the guard as the Belding build ran it | 120 | 118 | 0 | 23.1 s |
| the same, and the guard planning on 1 s cards so it never parks early | 120 | 118 | 0 | 23.1 s |

In that last run the fourth volley starts at 24.3 s and ends at 27.8 s with 4 of the 8 the RIGHT CELL
needs, and there is no time for a fifth.

## Rung 2: what would have to change

### One change at a time, on the Belding robot (R-Three-Tip, partner leaves 4)

| Change | 2 TIPs of 120 | 3 TIPs | TIP 2 at |
|---|---|---|---|
| As at Belding | 57 | 0 | 26.0 s |
| **Stay in 0.98 from every range** (0.65 / 0.92 / 0.82 by range at Belding) | **95** | 0 | 26.0 |
| the same, 0.96 | 79 | 0 | 26.1 |
| FLOWER picks: no extra dwell, none short | 64 | 0 | 26.0 |
| Turret, unlimited travel | 58 | 0 | 25.9 |
| Drive 70 in/s | 54 | 0 | 24.8 |
| Feed 0.47 s a shot, ends on the fourth | 54 | 0 | 21.7 |
| Feed 0.3 s a shot, ends on the fourth | 55 | 0 | 20.3 |
| Launching while moving (turret, led), Belding's feed | 21 | 0 | 19.5 |

Only accuracy moves the second TIP (the picks add 7; the rest are within 5 of 57). Faster feeds and driving bring it earlier, which is what a third
TIP needs, but add none. Launching while moving with Belding's 3.5 s feed loses TIPs: the robot has
left before the feed ends.

### One what-if taken away from all of them (R-Three-Tip-Stream)

| | 2 TIPs of 120 | 3 TIPs | TIP 3 at |
|---|---|---|---|
| **All what-ifs** | 107 | 99 | 26.9 s |
| without launching while moving (`LaunchAll` from each stop, turret) | 107 | **0** | |
| without the turret (locked, so no launching while moving either) | 107 | **0** | |
| without the faster feed (0.875 s a shot, whole feed) | 55 | **0** | |
| without the accuracy (Belding's) | 42 | **13** | 27.4 |
| without the faster drive (50 in/s) | 108 | 91 | 29.2 |
| without the pick fixes | 96 | 89 | 27.0 |
| without unlimited turret travel (393° window) | 107 | 99 | 26.9 |
| feed 0.47 s a shot instead of 0.3 | 107 | 97 | 28.5 |
| stay in 0.96 instead of 0.98 | 87 | 74 | 27.1 |
| stay in 0.95 | 83 | 66 | 27.1 |
| stay in 1.0 | 119 | 119 | 26.9 |
| shots not led for the robot's motion | 33 | 28 | 27.5 |
| the row sweep at 3 in/s instead of 6 | 105 | 24 | |

**Launching NECTAR** is on in every row (the real robot's design takes and launches it). With it off
the all-what-ifs run makes 107 and 90, against 107 and 99: a stray NECTAR from the spill in a volley is
worth 1.65. Whether the human enters NECTAR makes no difference (107 and 101 without): the route
never goes to the LOADING ZONE for it, and the budget shows why it cannot afford to.

Ranked by what the third TIP loses without it: **launching while moving (and so the turret), the
feed, the accuracy**, then drive speed, then the picks. Unlimited turret travel adds nothing.

### The smallest set that meets the target

| Turret + launching while moving + stay in 0.98, and | 2 TIPs of 120 | 3 TIPs | TIP 3 at |
|---|---|---|---|
| feed 0.47 s a shot, 50 in/s | 96 | 23 | |
| feed 0.3 s a shot, 50 in/s | 97 | 80 | 29.2 s |
| **feed 0.47 s a shot, 70 in/s** | **96** | **87** | 28.5 s |
| feed 0.47 s a shot, 60 in/s | 97 | 66 | 29.4 s |
| feed 0.47 s a shot, 70 in/s, stay in 0.96 | 78 | 62 | 28.6 s |

Picks as at Belding in all of these. The set needs 60 in/s or more, or a 0.3 s feed, on top of the
turret, the moving launch and the accuracy; with neither, the third TIP comes after 30 s. At 0.96
staying in, the second TIP drops to 78 and the set fails the target whatever the third does.

### The value each number has to reach

Stepped one at a time on the smallest set (turret, launching while moving, stay in 0.98, feed 0.47 s
a shot, 70 in/s, row sweep 6 in/s). None of these was tuned to get the answer; this is where the
answer changes. 2 TIPs / 3 TIPs of 120:

| Number | Fails | On the line | Meets the target | Value needed |
|---|---|---|---|---|
| Shots that stay in | 0.96: 78 / 62 | 0.97: 89 / 75 | 0.98: 96 / 87; 0.99: 102 / 101 | **0.98** (0.92 by inference at Belding) |
| Drive speed | 50 in/s: 96 / 23; 55: 96 / 47 | 60: 97 / 66 | 65: 96 / 81; 70: 96 / 87 | **60 in/s, 65 for margin** (the Autos are drawn at 50) |
| Seconds a shot, feed ending on the fourth | 0.75: 97 / 0 | 0.6: 96 / 59 | 0.47: 96 / 87; 0.3: 107 / 97 | **0.5 s or less** (0.47 at Belding once the first ball is out; the set feed made it 0.875) |
| Row sweep, a placeholder | 3 in/s: 94 / 0; 4: 97 / 24 | | 5: 97 / 71; 6: 96 / 87; 8: 94 / 85 | **5 in/s or more: a row of 4 in about 2.5 s** (never timed) |
| The lead on a moving shot, a placeholder | none: 33 / 28 (with every what-if on) | | perfect: 107 / 99 | **A moving shot that stays in as often as a standing one** (never tried) |

### What each would take

| | To build or tune | On the list as | Before the next event? |
|---|---|---|---|
| **Stay in 0.98** | Count what stays in from each launch spot; measure the speed-table rows at the LEFT spots themselves; film a volley at 240 fps for the slow first ball and the fourth that falls off the lip; reseat the launcher encoder cables | `robot-todo` §7d (the new counting item, and the 36 in row); fix list "Film a volley at 240 fps", "Reseat both launcher encoder cables" (#496), "Turn RIGHT_LAUNCH_CORNER's heading" (#516) | **Yes.** It is also the whole of the second TIP |
| **Feed ends on the fourth ball** | The break-beam at the launcher throat; confirm on video that feeds end at the 4th counted shot | fix list "Add a break-beam at the launcher throat", "Confirm on video that Auto feeds now end at the 4th counted shot"; `robot-todo` §7c | **Yes** |
| **FLOWER picks** | Find why the SCORING pick stops short; stop two pieces wedging | fix list "Find why Infrared's SCORING FLOWER pick stops short" (#507), "Stop two pieces wedging side by side at the intake" | Yes, and worth 7 of 120 on the second TIP |
| **A loose row in about 2 s** | A card that creeps along a row with the intake on; a stagger guide so four touching pieces feed one at a time | not on a list; the nearest is "Drill Infrared's floor pickup" (#508, TeleOp) and the GARDEN gather's 0 to 2 of 4. New `robot-todo` §7d item | The drill, yes. Four of four from the GARDEN, against a wall, is unproven |
| **60 to 70 in/s** | The drivetrain's tuned top speed is 71 in/s; the Autos are drawn at 50. Raise Pedro's path speed and re-time every path | fix list "Make the 270° Auto curves match their timing" | Partly: straight legs yes, the curves took 3.5 to 4.6 s against 2.9 |
| **Turret that tracks while driving** | Unlock the turret (it ran locked forward at Belding), with its zero and its tracking proven in motion | the turret's own bring-up; not on the Belding fix list | **No** |
| **Launching while moving** | Lead the shot for the robot's speed and for the range changing through the volley; a gate that opens on the move; then count what stays in | none. The kit's rule today is that a launch waits for the robot to slow | **No.** Nothing on the robot does this, and its accuracy is unknown |

## Rung 3: the least a partner must do

### What our partners did in AUTO at Belding

From the stream video of all 17 of our runs and the official scores. Times are seconds after our
PLAY. "Far-side start" is `LEFT_START`'s wall; "audience start" is `RIGHT_START`'s.

| Match | Partner | Started | Moved | Launched | TIPs in AUTO | LEAVE / PARK |
|---|---|---|---|---|---|---|
| Q-1 | 18975 | far-side start, by the SCORING FLOWER | 1.0 to 4 s, to the LOADING ZONE | no (likely; half hidden) | 1, ours, at 5.0 to 7.5 s | 3 / 5 |
| Q-4 | 7911 | audience wall, beside our start (likely) | never | none seen after 7 s; before that the stream showed the other field | 1, ours (likely), at about 7 s | 0 / 0 |
| Q-7 | 18092 | far-side start, 2 POLLEN at its front | 1.5 to 4.5 s, to the alliance wall; **its 2 POLLEN rolled out and stayed by the FLOWER** | no | 1, ours, at 20.5 to 22 s (the timed-out route) | 3 / 5 |
| Q-13 | 18975 | as Q-1 | 1.0 to 4 s, to the LOADING ZONE | no | 1, ours, at 4.5 to 7.0 s | 3 / 5 |
| Q-14 | 9133 | audience start (we ran L-NoCross) | 3.9 to 8 s, to the alliance wall | **about 4, from 0.5 to 3.4 s, at the RIGHT CELL; none stayed in (likely)** | 0 | 3 / 5 |
| Q-19 | 17283 | on the alliance wall, in the LOADING ZONE, **4 POLLEN in it** | never | no; the 4 were still in it at 31 s | 1, ours, at 5.2 to 6.2 s | 0 / 5 |
| Q-26 | 20862 | on the alliance wall, in the corner | never | no | 1, ours, at 4.8 to 6.0 s | 0 / 0 |
| Q-27 | 15547 | far-side start, by the corner; looked loaded | 0.5 to 2.0 s, a short slide into the corner | no | 1, ours, at 5.4 to 7.4 s | 0 / 5 |
| Q-31 | 5292 | far-side start (we had the audience start) | 1.5 to 4.5 s, to the corner | no | 1, ours, at 19.6 to 21.4 s (the timed-out route) | 3 / 5 |
| Q-32 | 15397 | far-side wall, near the corner; at least 1 POLLEN in it | never | no | 1, ours, at 8.0 to 9.8 s | 0 / 0 |
| M-3 | 10104 | audience start | sat to 19 s, then under the HIVE | 4, last out at 1.5 s; 3 or 4 stayed | TIP 1 at 3.7 to 5.2 s (theirs); TIP 2 at 26.3 s (ours, likely finished by one late ball of theirs) | 3 / 5 |
| M-4 | 5292 | audience start | from 7 s, to the alliance wall | about 6, last out at 4.8 s; 3 or 4 stayed | TIP 1 at 5.7 to 7.0 s (theirs) | 3 / 5 |
| M-7 red | 10104 | audience start | from 14.5 s; under the HIVE at the end | 4, last out at 1.5 s; 3 stayed | TIP 1 at **8.0 to 9.8 s**: the CELL sat 6 s on 3 POLLEN | 3 / 0 |
| M-7 blue | 5292 | audience start | from 5.6 s | about 6, last out at 2.5 s; 4 stayed (likely) | TIP 1 at 4.7 to 6.0 s (theirs); TIP 2 at 19.5 s (ours) | 3 / 5 |
| M-9 | 5292 | audience start | to the alliance wall by 8 s | 6 or 7, last out at 3.0 s; 3 stayed, 3 or 4 on the floor | TIP 1 at **9.5 to 10.3 s**: the CELL sat 6.5 s on 3 POLLEN | 3 / 5 |
| M-10 red | 10104 | audience start | under the HIVE at 21.5 s, then parked | 4, last out at 1.0 s; 3 or 4 stayed | TIP 1 at 3.7 to 5.2 s (theirs); TIP 2 at 22.5 s (ours) | 3 / 5 |
| M-10 blue | 5292 | audience start | from 5.5 s | 6 or 7, last out at 2.5 s; 3 or 4 stayed | TIP 1 at 4.1 to 5.3 s (theirs) | 3 / 5 |

Syncs: Q-4 is from the scoreboard only (the robot's light was hidden) and M-9 was set by eye; the
rest were checked on two events. Q-32's times are ±0.4 s.

### The kinds of partner we met

| Kind | Who | What it can give us |
|---|---|---|
| **Does not move** (4 of 10 qualifiers) | 7911, 17283, 20862, 15397 | Its preloads, set on the floor beside it before the match. 17283 sat all AUTO with 4 POLLEN in it |
| **Drives to park, launches nothing** (5 of 10) | 18975 (twice), 18092, 15547, and 5292 when we took its start | The same, and it leaves on its own. 18092 dropped 2 by accident |
| **Fires its preloads and misses** (1 of 10) | 9133 | Nothing in AUTO; its 4 lay under the HIVE |
| **Fires its preloads at the RIGHT CELL and TIPs it** (the playoffs) | 5292, 10104 | TIP 1 at 3.7 to 10.3 s. Late (after 8 s) twice in seven, when only 3 stayed in |

Across the event's 44 matches, an alliance made 0 AUTO TIPs 63 times, 1 TIP 22 times and 2 TIPs
3 times. All three of the 2-TIP Autos were ours, with 5292 or 10104.

### Our Belding robot beside each kind

The robot as it was at Belding, turret locked, master's guard, only the drawing changed.

| Partner does | Our Auto | 2 TIPs of 120 | 3 TIPs | TIP 2 at |
|---|---|---|---|---|
| Nothing, or only parks | R-Two-Tip | 58 | 0 | 28.5 s |
| Leaves its 4 preloads on the floor and parks | R-Three-Tip | 57 | 0 | 26.0 s |
| Fires its preloads at the RIGHT CELL | L-NoCross (as at Belding: 3 of 5) | 55 | 0 | 22.8 s |
| **Fires its preloads at the LEFT CELL once it is up, then parks** | **R-Two-Tip-With** | **109** | 0 | 23.0 s |
| the same, and our shots stay in at 0.98 | R-Two-Tip-With | 119 | 0 | 20.0 s |
| the same, on a 9 s timer instead of watching the HIVE | R-Two-Tip-With | 109 | 0 | 23.0 s |
| Makes the LEFT TIP alone (preloads + SCORING FLOWER) | R-NoCross | 58 | 0 | 23.0 s |
| Is our other robot | the Infraviolet pair | 22 | 0 | 20.5 s |

The first three rows are the same result for the same reason: the LEFT CELL gets exactly 8 pieces
and needs all of them. **What makes the second TIP reliable is a partner whose 4 preloads go into the
LEFT CELL**, so it gets 12 shots for the 8 it needs.

**The drawing:**
[R-Two-Tip-With beside partner-l-fill-hide-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-two-tip-with.pp,partners/partner-l-fill-hide-park.pp).
A partner that can shoot does it from directly in front of a CELL, so the stand-in starts on
`LEFT_START`. That is where `LEFT_LAUNCH_CATCH` is, so our route fires both LEFT volleys from
`LEFT_LAUNCH_OFFSET` instead (45.7 in, a measured row), which is clear of a robot on `LEFT_START`.
The partner fires its 4 at the LEFT CELL (once it sees it up, or at 9 s on a timer), then drives
straight under the HIVE and waits there, as some Belding partners did, until 23 s. It then leaves by
the audience side and parks at `RIGHT_PARK`; we park at `LEFT_PARK`, next to our last launch spot.
No two robots touch in any of the 120 runs, with either partner. We TIP the RIGHT CELL with our
preloads at 7.2 s; the partner's 4 go in at about 6.5 to 9.3 s (watching) or 9.0 to 11.7 s (timer);
our WALL FLOWER volley follows at 13 to 17 s and the SCORING FLOWER's at 23 to 26 s if it is still
needed. Alone, this drawing makes 2 TIPs in 61 of 120.

Two things this drawing gives up: if our preloads do not TIP, it parks instead of driving under the
HIVE to fire at the RIGHT CELL again (the partner may be there), and a partner under the HIVE is in
the way of anything else that wants that lane. (An earlier version of this section had the partner
start further along the far wall; no Belding partner shoots from there.)

### The asks

- **For two reliable TIPs:** "Start in front of the far CELL (`LEFT_START`). At 9 s, fire your
  preloads at it. Then drive under the HIVE and wait there until 23 s, and park at the near end of
  the LOADING ZONE." With Belding's accuracy that is 109 of 120.
  - **Who could have met it:** 5292 and 10104 launch and score, but from the audience start at the
    RIGHT CELL; they would need to start in front of the other CELL and add a 9 s wait, which nobody
    has seen them do. 9133 launches but put none in. The other seven launched nothing.
  - **With 5292 or 10104 as they are** (they take the audience start and TIP the RIGHT CELL), we
    run L-NoCross and the second TIP is a coin flip, as it was (3 of 5).
- **"Leave your 4 and park"** is what nine of ten qualifier partners could have done, and it is
  worth asking for only once the robot's shots stay in: at 0.98 it is 95 of 120, at Belding's
  accuracy it changes nothing.
- **For three TIPs:** no ask to a Belding-style partner does it. With the Belding robot the second
  TIP lands at 23 s at the earliest in every pairing, and the third needs 8 more pieces in the RIGHT
  CELL by about 25 s. The partner would have to fill the LEFT CELL by itself by about 12 s (preloads
  and a FLOWER pick, all 8 in) while we hold 4 for the RIGHT CELL and fetch 4 more. R-NoCross beside
  exactly that stand-in makes 0 of 120, and our own two robots as the Infraviolet pair make 0.

### Why no pairing makes a third TIP, shown by runs

Every pairing, run twice, both with the Belding calibration on (its dwell, its HIVE fields, its
volley and pick times), 120 seeds: first as the target asks, with Belding's own accuracy and picks;
then with every shot staying in and every FLOWER pick full, so luck is gone and only time is left.

| Pairing | As at Belding: 1 / 2 / 3 TIPs | TIP 2 at | Every shot in, every pick full: 1 / 2 / 3 TIPs | TIP 2 at |
|---|---|---|---|---|
| R-Two-Tip-With, partner fires its preloads at the LEFT CELL | 120 / 109 / **0** | 23.0 s | 120 / 120 / **0** | 19.4 s |
| R-NoCross, partner makes the LEFT TIP alone | 120 / 58 / **0** | 23.0 s | 120 / 120 / **0** | 22.9 s |
| L-NoCross, partner fires its preloads at the RIGHT CELL | 119 / 55 / **0** | 22.8 s | 120 / 120 / **0** | 22.9 s |
| The Infraviolet pair (both robots ours) | 117 / 22 / **0** | 20.5 s | 120 / 120 / **0** | 20.4 s |

No pairing makes a third TIP in any of its 240 runs.

**Why, beyond these four drawings.** A Belding-style partner gives at most its 4 preloads, so our
robot still has to land 15 of the 19 pieces: its preloads and three more loads. Unlike rung 1, a
bound with no driving does not rule that out (it gives TIP 3 at 28.8 s), so the reason is the field's
distances. The best case, with Belding's shortest times and the partner's 4 going into the LEFT CELL
the moment it is up:

```
our preloads, TIP 1 at the earliest we made it                      6.0 s
WALL FLOWER: drive 2.1 + pick 3.6, from the end of the preloads     robot loaded at 8.3 s
drive to a LEFT spot 2.9, volley 3.6                                last LEFT piece at 14.8 s
shortest dwell and swing 4.2                                        TIP 2 at 19.0 s
SCORING FLOWER: drive 1.3 + pick 3.6                                robot loaded at 19.7 s
drive to a RIGHT spot 3.4, volley 3.6                               4 of 8 in the RIGHT CELL at 26.7 s
```

The RIGHT CELL still needs 4 more, a pick and a volley away, and AUTO ends 3.3 s later. That is the
19.4 s second TIP in the first row of the table, and the same wall in every pairing.

The earliest second TIP is 19.4 s. The RIGHT CELL then needs 8 pieces by about 25 s, and a Belding
volley of 4 takes 4 s after the robot is there with them: two volleys from one robot do not fit, and
one volley from each robot needs both standing at a RIGHT spot, loaded, at 19.4 s, which no drawing
here reaches (our robot is at the LEFT CELL finishing TIP 2, or still picking). So **for three TIPs
the partner would have to do what the rung 2 what-if robot does**; "fire your preloads" is not enough,
and nothing a Belding partner did comes close.

### The Belding matches that missed a second TIP

- **The seven R-Solo qualifiers with a first TIP from the preloads (Q-1, Q-4, Q-13, Q-19, Q-26,
  Q-27, Q-32): 0 second TIPs.** R-Two-Tip, with the LEFT volleys from the measured spots, makes it in
  58 of 120: about 10 more points a match. In Q-19 the partner sat with 4 POLLEN in it, and in Q-27
  and Q-32 it looked loaded: up to 12 pieces went unused. None of those partners could launch, so
  the far-CELL ask was not open to them, and leaving them on the floor would not have helped at
  Belding's accuracy.
- **M-4, M-7 red, M-10 blue (L-NoCross beside 5292 or 10104): no second TIP.** The LEFT CELL held 4
  or 5 at the end. With the roles swapped (we take the audience start and TIP 1, they put their
  preloads into the LEFT CELL) the simulator says 109 of 120 against 55. That needs 5292 or 10104 to
  shoot the far CELL on a timer.
- **Q-7 and Q-31:** the preloads did not TIP, the timed-out route did. Nothing here changes that.

## The measurements that would confirm it

In order of how much each could change the answer:

1. **Balls that stay in from `LEFT_LAUNCH_CATCH`, `LEFT_LAUNCH_OFFSET` and `RIGHT_START`:** five
   volleys of four from each, filmed, fed by a pick. The calibration's 0.92, 0.89 and 0.82 are
   inferred from TIPs. This decides the second TIP alone (57 of 120 at 0.92, 95 at 0.98) and the
   third with everything else (13 against 99).
2. **A row of 4 loose POLLEN at a creep:** how many, and how long, in the open and against a wall.
   The route's two sweeps rest on a placeholder; at half the creep speed the third TIP falls from
   99 to 24.
3. **The feed with a shot counter:** the time from the gate opening to the fourth ball, after a
   pick. Belding's spacing says about 2.2 s.
4. **How many landed POLLEN tip a CELL on the practice HIVE, and the dwell:** as in
   `one-robot-two-tips.md`. A dwell of 6 s (seen twice at Belding on 3 POLLEN) ends any three-TIP plan.
5. **A shot while the robot rolls,** once the turret tracks while driving.

1, 2 and 5 are new items in `doc/robot-todo.md` §7d; 3 is §7c and the fix list; 4 is in
`one-robot-two-tips.md`.

## Every row

Each row is 120 runs. Counts are runs with at least that many TIPs complete by 30 s; times are the mean
time of that TIP over the runs that made it before TELEOP (so a time past 30 s means some came in the
transition and are not in the count). The target is met where 2 TIPs is 90 or more and 3 TIPs is 60 or
more.

| Row | 1 TIP | 2 TIPs | 3 TIPs | TIP 1 at | TIP 2 at | TIP 3 at | Target |
|---|---|---|---|---|---|---|---|
| R-Two-Tip alone, the Belding robot | 119 | 58 | 0 | 7.5 s | 28.5 s |  | no |
| R-Three-Tip, the Belding robot, partner leaves 4 | 117 | 57 | 0 | 7.2 s | 26.0 s |  | no |
| the Belding robot's timing, every shot stays in, every pick full | 120 | 117 | 0 | 7.0 s | 26.1 s |  | no |
| the Belding robot at 150 in/s, every shot in, every pick full and quick | 120 | 118 | 0 | 7.0 s | 23.1 s |  | no |
| + turret, unlimited travel | 117 | 58 | 0 | 7.2 s | 25.9 s |  | no |
| + drive 70 in/s | 117 | 54 | 0 | 7.2 s | 24.8 s |  | no |
| + feed 0.47 s a shot, ends on the fourth | 117 | 54 | 0 | 6.6 s | 21.7 s |  | no |
| + feed 0.3 s a shot, ends on the fourth | 117 | 55 | 0 | 6.3 s | 20.3 s |  | no |
| + FLOWER picks: no extra dwell, none short | 117 | 64 | 0 | 7.2 s | 26.0 s |  | no |
| + stay in 0.98 | 120 | 95 | 0 | 7.0 s | 26.0 s |  | no |
| + stay in 0.96 | 119 | 79 | 0 | 7.0 s | 26.1 s |  | no |
| + launching while moving (turret, led) | 118 | 21 | 0 | 7.6 s | 19.5 s |  | no |
| the Belding robot, not launching NECTAR | 117 | 54 | 0 | 7.2 s | 26.1 s |  | no |
| R-Three-Tip-Stream, all what-ifs | 120 | 107 | 99 | 6.1 s | 14.1 s | 26.9 s | **met** |
| all, without the turret (locked, `LaunchAll` route) | 120 | 107 | 0 | 6.1 s | 19.1 s |  | no |
| all, turret travel 393° | 120 | 107 | 99 | 6.1 s | 14.1 s | 26.9 s | **met** |
| all, 50 in/s | 120 | 108 | 91 | 6.1 s | 15.0 s | 29.2 s | **met** |
| all, Belding's feed (0.875 s a shot, whole feed) | 120 | 55 | 0 | 7.0 s | 18.9 s | 31.8 s | no |
| all, feed 0.47 s a shot | 120 | 107 | 97 | 6.4 s | 14.9 s | 28.5 s | **met** |
| all, Belding's picks | 120 | 96 | 89 | 6.1 s | 14.1 s | 27.0 s | **met** |
| all, Belding's accuracy | 120 | 42 | 13 | 6.8 s | 14.3 s | 27.4 s | no |
| all, stay in 0.95 | 120 | 83 | 66 | 6.3 s | 14.2 s | 27.1 s | no |
| all, stay in 0.96 | 120 | 87 | 74 | 6.3 s | 14.2 s | 27.1 s | no |
| all, stay in 1.0 | 120 | 119 | 119 | 6.1 s | 14.1 s | 26.9 s | **met** |
| all, without launching while moving (`LaunchAll` route, turret) | 120 | 107 | 0 | 6.1 s | 19.3 s |  | no |
| all, shots not led for the robot's motion | 120 | 33 | 28 | 6.1 s | 14.8 s | 27.5 s | no |
| all, row sweep at 3 in/s | 120 | 105 | 24 | 6.1 s | 15.7 s | 29.7 s | no |
| all, not launching NECTAR | 120 | 107 | 90 | 6.1 s | 14.1 s | 27.2 s | **met** |
| all, no human NECTAR entered | 120 | 107 | 101 | 6.1 s | 14.1 s | 27.0 s | **met** |
| turret + moving + stay in 0.98 + feed 0.47, 50 in/s | 120 | 96 | 23 | 6.4 s | 15.5 s | 30.5 s | no |
| turret + moving + stay in 0.98 + feed 0.3, 50 in/s | 120 | 97 | 80 | 6.1 s | 15.0 s | 29.2 s | **met** |
| turret + moving + stay in 0.98 + feed 0.47, 60 in/s | 120 | 97 | 66 | 6.4 s | 15.1 s | 29.4 s | **met** |
| turret + moving + stay in 0.98 + feed 0.47, 70 in/s | 120 | 96 | 87 | 6.4 s | 14.9 s | 28.5 s | **met** |
| turret + moving + stay in 0.96 + feed 0.47, 70 in/s | 120 | 78 | 62 | 6.6 s | 14.9 s | 28.6 s | no |
| smallest set, stay in 0.97 | 120 | 89 | 75 | 6.4 s | 14.9 s | 28.5 s | no |
| smallest set, stay in 0.99 | 120 | 102 | 101 | 6.4 s | 14.8 s | 28.5 s | **met** |
| smallest set, 55 in/s | 120 | 96 | 47 | 6.4 s | 15.3 s | 30.0 s | no |
| smallest set, 65 in/s | 120 | 96 | 81 | 6.4 s | 15.0 s | 29.0 s | **met** |
| smallest set, feed 0.6 s a shot | 120 | 96 | 59 | 6.6 s | 16.0 s | 29.6 s | no |
| smallest set, feed 0.75 s a shot | 120 | 97 | 0 | 6.8 s | 17.6 s | 32.1 s | no |
| smallest set, row sweep 3 in/s | 120 | 94 | 0 | 6.4 s | 16.4 s |  | no |
| smallest set, row sweep 4 in/s | 120 | 97 | 24 | 6.4 s | 15.7 s | 30.4 s | no |
| smallest set, row sweep 5 in/s | 120 | 97 | 71 | 6.4 s | 15.2 s | 29.2 s | **met** |
| smallest set, row sweep 8 in/s | 120 | 94 | 85 | 6.4 s | 14.5 s | 27.7 s | **met** |
| R-Two-Tip, partner only parks | 119 | 58 | 0 | 7.5 s | 28.6 s |  | no |
| L-NoCross, partner fires its preloads at the RIGHT CELL | 119 | 55 | 0 | 7.4 s | 22.8 s |  | no |
| R-Two-Tip-With alone | 119 | 57 | 0 | 7.5 s | 28.4 s |  | no |
| R-Two-Tip-With, partner fires its preloads at the LEFT CELL once it is up | 120 | 109 | 0 | 7.6 s | 23.0 s |  | no |
| R-Two-Tip-With, partner fires at the LEFT CELL at 9 s | 120 | 109 | 0 | 7.6 s | 23.0 s |  | no |
| the same pair, our shots stay in at 0.98 | 120 | 119 | 0 | 7.0 s | 20.0 s |  | no |
| R-NoCross, partner makes the LEFT TIP alone | 120 | 58 | 0 | 7.5 s | 23.0 s |  | no |
| R-Two-Tip-With + that partner, every shot in, every pick full | 120 | 120 | 0 | 7.0 s | 19.4 s |  | no |
| R-NoCross + a partner that makes the LEFT TIP, every shot in, every pick full | 120 | 120 | 0 | 7.0 s | 22.9 s |  | no |
| L-NoCross + a partner that fires at the RIGHT CELL, every shot in, every pick full | 120 | 120 | 0 | 7.0 s | 22.9 s |  | no |
| the Infraviolet pair, every shot in, every pick full | 120 | 120 | 0 | 7.0 s | 20.4 s |  | no |
| the Infraviolet pair | 117 | 22 | 0 | 7.2 s | 20.5 s |  | no |

## How it was run

```bash
# One row: the route, the partner, the drive speed; the design; then the what-ifs.
BIOBUZZ_AUTO_CALIBRATION=belding BIOBUZZ_AUTO_RUNS=120 \
  BIOBUZZ_AUTO_STUDY="RThreeTipStreamAuto,PartnerLStageLeaveParkAuto@70" BIOBUZZ_AUTO_DESIGNS="real robot" \
  BIOBUZZ_AUTO_TURRET_TRAVEL_DEG=0 \
  BIOBUZZ_AUTO_DESIGN_SET="shotIntervalS=0.3,compensatesMotion=true" \
  BIOBUZZ_BELDING_WHOLE_FEED=0 BIOBUZZ_SIM_TYPICAL_LAUNCH_ALL=1.5 \
  BIOBUZZ_BELDING_PICK_DWELL=0 BIOBUZZ_BELDING_PICK_SHORT=0 BIOBUZZ_SIM_TYPICAL_PICK_FLOWER=3 \
  BIOBUZZ_BELDING_STAY_CLOSE=0.98 BIOBUZZ_BELDING_STAY_CATCH=0.98 BIOBUZZ_BELDING_STAY_START=0.98 \
  ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*' -i

# The Belding robot rows: nothing but the first line, with
#   BIOBUZZ_AUTO_STUDY="RThreeTipAuto,PartnerLStageLeaveParkAuto@50"
#   BIOBUZZ_AUTO_DESIGNS="real robot, turret locked forward"
# Rung 3: BIOBUZZ_AUTO_STUDY="RTwoTipWithAuto,PartnerLFillHideParkAuto@50", the same design.
# No moving shots: add BIOBUZZ_AUTO_FIRE_STILL=1 and drop compensatesMotion. The pair:
#   BIOBUZZ_AUTO_STUDY="LPairFlowersAuto,RPairGardenSweepAuto@50" BIOBUZZ_AUTO_DESIGNS="real robot, turret locked forward"
#   (no FIRE_STILL needed: every volley is a LaunchAll from a stop)
# A late first TIP: a request's partner entry with "timing": {"startDelayS": 3} (TeamCode/sim-requests/l-three-tip.json).
# The spot scan: BIOBUZZ_SIM_BOTH_CELLS=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*BothCellsSpotScanTest*' -i
```

- **Counts are by 30 s.** Each STUDY line is followed by `by 30 s: TIP1 … TIP2 … TIP3 …`, and the
  tables use that line. The STUDY line itself also counts a TIP that completes in the transition
  (§10.5 B); the manual says transition scoring "may be subject to penalties", so nothing here plans
  on it. The two differ only where a TIP lands near 30 s: "all, at 50 in/s" is 91 by 30 s and 101
  with the transition; "feed 0.47, 50 in/s" is 23 and 89.
- **A what-if sets the guard's planning time too.** The guard plans with master's typical times
  (`LaunchAll` 4 s, `PickFlower` 4 s). A row with a faster feed sets `BIOBUZZ_SIM_TYPICAL_LAUNCH_ALL`
  (1.5 s for 0.3 s a shot, 2.5 s for 0.47) and a row with the pick fixes sets `_PICK_FLOWER=3`, as
  the constants would be retuned on such a robot.
- **Drive speed:** the stream draft is drawn at 70 in/s, so its exported path times are for 70; the
  "50 in/s" rows drive it at 50.
- Seeds 1 to 120: odd seeds are Belding's Field 1, even seeds Field 2.

## What the simulator rests on, and does not know

Added for this study, in test code only (TeamCode/README.md, "Simulating an Auto"):

| | What it does | Rests on |
|---|---|---|
| `SweepRow` card | The robot creeps along a row of loose pieces with the intake on | **Placeholder:** a 6 in/s creep (three quarters of a POLLEN's width every `intakeIntervalS`, 0.35 s, itself a placeholder) and an 0.85 grab chance. Not tuned to the answer: at 3 in/s the third TIP falls to 24 of 120 |
| Launching while moving | `StreamOn` fires at the raised CELL whenever it is aimed and within 60 in; `compensatesMotion` leads the shot | **Placeholder:** the lead is perfect and a moving shot stays in as often as a standing one from that range. Unled, 28 of 120 |
| The stand-ins | `partner-l-stage-leave-park` (row along its side), `partner-l-fill-hide-park`, `partner-l-timed-fill-hide-park`, `partner-r-tip-far-park` | A stand-in shoots as our robot does (0.85 from 50 in) |
| The guard's typical times | `BIOBUZZ_SIM_TYPICAL_*` | Set by hand to match each what-if |
| Counts by 30 s | the `by 30 s` line | |
| `CollectSeen` reaching a piece | The real robot's intake takes on contact; the chase stopped 4 in short | Not used in the final routes |

Not modelled:

- **A pick that returns 1 to 3 pieces.** A FLOWER pick comes back full or, 2 times in 14 at the
  SCORING FLOWER, with nothing. Belding's picks averaged 2.0 (Infrared) and 3.2 (Ultraviolet) by the
  lane sensor, which undercounts; the true share of full picks is not known.
- **Shots from the side of a CELL: the solo drawings still have one.** `R-Three-Tip`, `R-Three-Tip-Stream`
  and `R-Three-Tip-Still` fire a volley at the RIGHT CELL from the wall lane, (26, 40), 62 degrees off
  square, and the spot scan below recommends that lane for both CELLs. The mentor's verdict (11 Nov) is
  that such a shot never goes in, so **their third-TIP counts are not to be believed until they are
  redrawn with every volley within about 26 degrees of square**; the pair above has been. The stay-in share is by range only. The lane spot (26, 40) is
  40 in from the RIGHT CELL at an angle nobody has fired from.
- **The GARDEN against the wall.** The sweep takes its 4 in 2.6 s; the robot's own gather routine
  took 0 to 2 in practice.
- **Other robots.** No opponent crosses under the HIVE (Q-27 cost 4 s that way), and the route's
  first drive goes there.
- **A dwell longer than 4.2 s.** Twice at Belding a CELL sat about 6 s on 3 POLLEN.
- **The first ball's 0.75 s** after the gate opens, and accuracy changing with battery or a piece's
  place in the volley.

## Open questions

- **Is a TIP that completes in the transition an AUTO TIP without penalty?** Taken as no. If yes,
  the 50 in/s rows meet the target as well.
- **May a partner start on the far wall west of the SCORING FLOWER with its preloads on the tiles
  beside it?** Taken as yes: G304 allows preloads on the tiles touching the robot, and the simulator's
  start rule (touching a wall, in our half) allows the spot. Ask at the next event's question box.
- **Q-4 before 7 s** was not on the stream, so whether 7911 launched then is not known. The official
  LEAVE 0 and one TIP say it did not matter.
