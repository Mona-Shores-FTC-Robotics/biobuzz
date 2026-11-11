# Three AUTO TIPs: can one robot do it, and what must a partner do (11 Nov 2026, study)

A study for the mentor, run in the simulator set to the Belding Qualifier (`BeldingCalibration`;
TeamCode/README.md, "Simulating an Auto"), 120 seeds a row, both Belding HIVE fields, master's
endgame guard. It builds on [`one-robot-two-tips.md`](one-robot-two-tips.md) and
[`belding-auto-review.md`](belding-auto-review.md). No robot code changes here.

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
- **Three TIPs alone take a different robot.** The smallest set that meets the target is all of
  these together: a turret that tracks the CELL while the robot drives; launching while moving; 0.98
  of shots staying in from every range; a feed that ends on the fourth ball at Belding's own spacing
  (0.47 s a shot); 60 in/s or more; and a row of loose pieces picked up in about 2 s, twice. That makes
  **2 TIPs in 96 and 3 TIPs in 87 of 120** at 70 in/s, the third at 28.5 s (97 and 66 at 60 in/s).
  Take any one away and the third TIP goes: without launching while moving 0 of 120, with Belding's
  accuracy 13, at 50 in/s 23.
- **None of that is realistic before the next event** except the accuracy work and the shot
  counter, which are already on the fix list and are what the second TIP needs anyway.
- **The least a partner must do for two reliable TIPs is fire its 4 preloads into the far CELL
  once it is up, then park.** Beside that partner, our Belding robot on R-Two-Tip makes **2 TIPs in
  109 of 120**, the second at 23.0 s. A partner on a 9 s timer does as well as one that watches
  the HIVE. Of our Belding partners only 5292 and 10104 launched and scored in AUTO at all; nine of
  our ten qualifier partners launched nothing.
- **No Belding-style partner makes a third TIP possible.** It would have to make a whole TIP of 8
  by itself and early. No alliance at the event made two AUTO TIPs without one of our robots on it.
- **The one measurement that would most change the answer: count the balls that stay in from the
  LEFT launch spots.** Every result here turns on that share, and it has never been counted.

### Each rung, against the target

| Rung | Best drawing | 2 TIPs / 3 TIPs of 120 | Meets the target? |
|---|---|---|---|
| 1, the Belding robot, partner leaves 4 | R-Three-Tip | 57 / 0 | **No, and it cannot.** By the budget: about 42 s of work. By a run: with every shot staying in and every pick full, the same robot makes 117 / 0. Its second TIP lands at 26.1 s, which leaves 4 s to put 8 pieces into the RIGHT CELL and wait out a 4.75 s dwell |
| 1 and 2, with the allowed what-ifs | R-Three-Tip-Stream | 107 / 99 with all of them; 96 / 87 with the smallest set | Yes, but only in the simulator, and on three placeholders (the row sweep, a perfect lead on a moving shot, stay-in by range alone) |
| 3, the Belding robot beside a Belding-style partner | R-Two-Tip-With | 109 / 0 | **Two TIPs yes, three no:** the second TIP lands at 23 s in the best pairing and the third needs 8 more pieces in by about 25 s |

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
[R-Two-Tip-With beside partner-l-fill-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-two-tip-with.pp,partners/partner-l-fill-park.pp).
It is R-Two-Tip with its drive up the wall lane pulled in (control point (30, 110)), so it stays
clear of the partner. The partner starts on the far wall at (30, 133.8), west of the SCORING
FLOWER and clear of `LEFT_LAUNCH_CATCH`, fires its 4 at the LEFT CELL from there (50 in), then
drives along the far wall to the LOADING ZONE. No two robots touch in any of the 120 runs. We TIP
the RIGHT CELL with our preloads at 7.6 s; the partner's 4 go in at about 6.5 to 9.3 s (watching)
or 9.0 to 11.8 s (timer); our WALL FLOWER volley follows at 13 to 17 s and the SCORING FLOWER's at
23 to 26 s if it is still needed.

### The asks

- **For two reliable TIPs:** "Start on the far wall beside the SCORING FLOWER. At 9 s, fire your
  preloads at the far CELL. Then drive along the far wall to the LOADING ZONE." With Belding's
  accuracy that is 109 of 120.
  - **Who could have met it:** 5292 and 10104 launch and score, but from the audience start at the
    RIGHT CELL; they would need a 9 s wait and an aim at the other CELL from the far wall, which
    nobody has seen them do. 9133 launches but put none in. The other seven launched nothing.
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
| R-Two-Tip, partner only parks | 119 | 58 | 0 | 7.5 s | 28.6 s |  | no |
| L-NoCross, partner fires its preloads at the RIGHT CELL | 119 | 55 | 0 | 7.4 s | 22.8 s |  | no |
| R-Two-Tip-With alone | 119 | 57 | 0 | 7.5 s | 28.4 s |  | no |
| R-Two-Tip-With, partner fires its preloads at the LEFT CELL once it is up | 120 | 109 | 0 | 7.6 s | 23.0 s |  | no |
| R-Two-Tip-With, partner fires at the LEFT CELL at 9 s | 120 | 109 | 0 | 7.6 s | 23.0 s |  | no |
| the same pair, our shots stay in at 0.98 | 120 | 119 | 0 | 7.0 s | 20.0 s |  | no |
| R-NoCross, partner makes the LEFT TIP alone | 120 | 58 | 0 | 7.5 s | 23.0 s |  | no |
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
# Rung 3: BIOBUZZ_AUTO_STUDY="RTwoTipWithAuto,PartnerLFillParkAuto@50", the same design.
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
| The stand-ins | `partner-l-stage-leave-park` (row along its side), `partner-l-fill-park`, `partner-l-timed-fill-park` | A stand-in shoots as our robot does (0.85 from 50 in) |
| The guard's typical times | `BIOBUZZ_SIM_TYPICAL_*` | Set by hand to match each what-if |
| Counts by 30 s | the `by 30 s` line | |
| `CollectSeen` reaching a piece | The real robot's intake takes on contact; the chase stopped 4 in short | Not used in the final routes |

Not modelled:

- **A pick that returns 1 to 3 pieces.** A FLOWER pick comes back full or, 2 times in 14 at the
  SCORING FLOWER, with nothing. Belding's picks averaged 2.0 (Infrared) and 3.2 (Ultraviolet) by the
  lane sensor, which undercounts; the true share of full picks is not known.
- **Shots from the side of a CELL.** The stay-in share is by range only. The lane spot (26, 40) is
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
