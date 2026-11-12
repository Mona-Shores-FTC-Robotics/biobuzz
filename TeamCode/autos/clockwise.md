# R-Clockwise / L-Clockwise: a paired Auto with fixed fallbacks

Successors to R-Infraviolet and L-Infraviolet. Mentor session, 11 to 12 Nov 2026. **Not yet run on
a robot:** Infraviolet stays on the Autonomous menu until Clockwise has driven on both robots.

Select **Autonomous → R-Clockwise** on the RIGHT_START robot and **L-Clockwise** on the LEFT_START
robot. They are a pair: run one only beside the other, with the two INIT menus set to match (each
page says what the partner's must show).

Open the pair together:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-clockwise.pp,l-clockwise.pp>

**Every volley is from a spot the drivers' Square goes to** (the wall row of
`localization/LaunchSpots`; mentor, 12 Nov 2026): one set of spots for TeleOp and Auto, so a
TeleOp volley is data for the Auto and the speed table is tuned in one place. `ClockwiseAutoTest`
fails if a volley of the pair is from anywhere else.

**What the Java simulator says, set to the Belding Qualifier** (24 seeds a row; tables below):

- Both robots park and the robots do not touch in every run, with either robot 20% slow and with
  either robot's preloads missed, for both pickup choices.
- At least one TIP comes in every normal run, and two in 9 of 24 when R takes the WALL FLOWER (3
  of 24 when R takes the GARDEN).
- **Three TIPs never come.** R finishes its SCORING FLOWER pick at about 20.1 s and would have to
  start across by 16.2 s.
- **The simulator reads low against the robots.** The mentor has run the Infraviolet pair on the
  robots and scored three TIPs; this simulator gives Infraviolet none in 24 runs. So these counts
  are a floor for comparing drawings, not a forecast. The robots' logs decide.

## The plan

| | R-Clockwise | L-Clockwise |
|---|---|---|
| TIP 1 | Shoot the preloads at RIGHT at once, from RIGHT_START | Slide 2.4 in to LEFT_WALL_HIVE and wait |
| First pickup | The WALL FLOWER, or the GARDEN | |
| TIP 2 | Outside lane to LEFT_WALL_NEAR; shoot at LEFT | Shoot the preloads at LEFT once it is up |
| Second pickup | The SCORING FLOWER | Under the HIVE; the GARDEN, or the WALL FLOWER |
| TIP 3 | Wait 2.3 s, back straight out and under the HIVE onto RIGHT_START; shoot at RIGHT | RIGHT_WALL_NEAR; shoot at RIGHT |
| Park | Outside lane to RIGHT_PARK | Outside lane to LEFT_PARK |

**The loop.** Under the HIVE is one-way, left to right. The outside lane is one-way, right to
left. No path of either Auto, fallbacks and parks included, drives a lane the other way; the
simulator checks every run (`"loop": "clockwise"`).

**The one variation.** R's first pickup is the WALL FLOWER (as drawn) or the GARDEN, and L takes
the other. It is one row of each robot's INIT menu, **Pickup**, and it changes one leg per robot.

**The parks are fixed.** R always ends on RIGHT_PARK and L on LEFT_PARK. Only the path in differs.

**R drives backward with a load** (mentor, 11 Nov 2026: it can). From the WALL FLOWER it turns the
short way as it goes up the outside lane. From the SCORING FLOWER it backs straight out, under the
HIVE and onto the wall at RIGHT_START without turning at all.

**R waits 2.3 s at the SCORING FLOWER before it backs under the HIVE**, so that TIP 2's spill has
landed. In a what-if with a short feed (below), the first drawing drove into pieces still falling
(G409) in 35 of 120 runs and 50 of 120 with full loads; with the wait it was 0 of 120 in both
(measured on the route before the Square spots). The films in `timing-terms.md` put the CELL
settling 3.4 to 6.5 s after the last ball leaves, so a fixed wait is a first value, not a
measurement: `doc/robot-todo.md` has the video check.

## The stops: one question each, "which CELL is up by this time?"

| Stop | Question | Yes | No |
|---|---|---|---|
| **L1:** L at LEFT_WALL_HIVE, preloads | LEFT up by T1? | Shoot at LEFT | Cross under the HIVE, shoot at RIGHT from RIGHT_START to finish TIP 1, then park |
| **R2:** R at LEFT_WALL_NEAR, first load | LEFT up by T2? | Shoot at LEFT | Park |
| **R3:** R with the SCORING FLOWER load | RIGHT up by T3? | Shoot at RIGHT, park | Shoot at LEFT to finish TIP 2, then park |
| **L2:** L at RIGHT_WALL_NEAR, pickup load | RIGHT up by T4? | Shoot at RIGHT, park | Park |

L finishes TIP 1 and R finishes TIP 2. Neither robot needs to know why a TIP is late.

**A CELL is scored only from its own end of the field** (CLAUDE.md, hard constraints), so L1's
"no" is a crossing, not a shot from the left spot.

**R3 is asked at one end or the other, and the clock picks which.** When the SCORING FLOWER pick
ends, R asks only the clock:

- **Before T3c: go for TIP 3.** R waits for the spill, backs under the HIVE and asks on
  RIGHT_START: RIGHT up by T3r? Yes, shoot at RIGHT and park by the outside lane. No, park the
  same way with the load.
- **After T3c, before T3b: finish TIP 2.** R goes to LEFT_WALL_HIVE and asks: RIGHT up by T3? Yes,
  TIP 2 is done and it parks. No, it shoots its load at LEFT and parks.
- **After T3b: park straight from the SCORING FLOWER.** The volley and the park after it would not
  fit.

Setting **T3c to 0 in INIT** keeps R on the left.

**One more clock check**, TS, sits after R's first LEFT volley: go for the SCORING FLOWER only
before TS. Later than that R parks from LEFT_WALL_NEAR at once, before L comes up the lane.

## The times

All on the robot's match clock (from its own PLAY), all rows of the INIT menu. The values are the
Java simulator's, set to Belding; **every one is a robot check** (`doc/robot-todo.md` §3).

| Row | Value | Where it comes from |
|---|---|---|
| **T1** LEFT up by | 9.5 s | TIP 1 from R's preloads was read up at 6.5 to 8.8 s in the simulator. The Belding films have it settled as late as 9.98 s after our PLAY, so this is likely short |
| **T2** LEFT up by | 15.5 s | R is at LEFT_WALL_NEAR at 12.0 s. A volley begun later ends after 19.3 s, and R's park would then cross the lane as L comes up it |
| **TS** scoring FLOWER by | 16.9 s | R's LEFT volley (launch starts to feed ends) is over at 15.8 s. Later than 16.9 s, the pick and the park after it meet L in the lane |
| **T3c** cross by | 16.2 s | The wait for the spill (2.3 s), the crossing (3.3 s), a volley as the guard plans it (4.0 s), the park from RIGHT_START (3.2 s) and the guard's 1 s margin end at 30 s |
| **T3r** RIGHT up by, on RIGHT_START | 21.8 s | The volley, that park and the margin |
| **TRr** park from the right at | 24.0 s | L has left RIGHT_WALL_NEAR for its park (TL) |
| **T3b** LEFT rescue by | 18.9 s | The drive to LEFT_WALL_HIVE (2.5 s), the volley (4.0 s), the park from there (3.5 s) and the margin |
| **T3** RIGHT up by, at LEFT_WALL_HIVE | 21.7 s | R is there by 21.5 s when it left the FLOWER by T3b |
| **TR** park from LEFT_WALL_HIVE at | 26.4 s | The latest the guard allows; L has turned in to LEFT_PARK |
| **T4** RIGHT up by | 19.2 s | L's volley must end, and L pass the left end, before R parks |
| **TL** park at | 22.7 s | L leaves RIGHT_WALL_NEAR for LEFT_PARK once R has left the SCORING FLOWER |
| **TLr** park after the crossing at | 22.5 s | L1's "no" only: the TIP its volley makes was read up at 19.6 to 20.2 s, and its park goes through where that spill lands |
| **TF** FLOWER at | 15.5 s | L's WALL FLOWER leg only: R, coming north from the GARDEN, has passed the FLOWER |

## What the Square spots cost

The same request on the route before the Square spots (hand-made pair spots, and the team's
LEFT_LAUNCH_CATCH for R's rescue):

| R takes the WALL FLOWER, normal | Two TIPs or more | Both parked | Robot contact |
|---|---|---|---|
| Before (pair spots) | 18 of 24 | 24 | 0 |
| Now (Square spots) | 9 of 24 | 24 | 0 |

- **The TIP 2 rescue no longer fits at a 4.0 s volley.** LEFT_WALL_HIVE is on the far wall behind
  the SCORING FLOWER: 2.5 s from the FLOWER against 1.7 s to LEFT_LAUNCH_CATCH, and 3.5 s back to
  RIGHT_PARK against 2.3 s. R is ready at 20.1 s and T3b is 18.9 s, so R parks from the FLOWER.
  Nine of the old 18 two-TIP runs were that rescue.
- **It comes back with a shorter volley.** T3b is 30 s less the drive, the volley, the park and the
  margin: at a 2.5 s volley it is 20.4 s, after R is ready.
- **The shots are about 50 in** (LEFT_WALL_NEAR 51, LEFT_WALL_HIVE 50, RIGHT_START 50,
  RIGHT_WALL_NEAR 52, robot centre to the row), where the pair spots were 43 to 51. The
  calibration keeps about 0.91 of balls from there.
- **RIGHT_PARK moved** to (14.5, 98.7) on master (#516). The two parked robots are 2.7 in apart, so
  every way into either park now ends straight: L turns east of the park zone and drives in, and
  R's late park comes in along RIGHT_PARK's own line.

## Where the clock keeps the robots apart

Nothing on the robots tells one where the other is, so the times above keep them apart.

- **R's park crosses L's way to LEFT_PARK.** R parks either well before L comes up the lane (from
  LEFT_WALL_NEAR by T2 or TS, from the SCORING FLOWER at about 20.2 s) or after L has turned in
  (TR, from LEFT_WALL_HIVE).
- **A robot on LEFT_WALL_NEAR blocks the way into LEFT_PARK.** Its corner is inside the park's
  approach, so R never stands there late: T2 and TS send it to its park first.
- **R's park from the right side joins the lane L leaves by.** R waits until TRr.
- With R on the GARDEN, R's lane north passes the WALL FLOWER where L picks. L waits until TF.

The speed tolerance beyond 20% slow has not been rerun on this route. Before the Square spots the
first contact was with L 30% slow (2 of 8 runs).

## The INIT menu

Gamepad 1's D-pad: **up / down selects a row, left / right changes it.** The page shows the
selected row, what the partner's menu must show, and every row's value.

| Row | R-Clockwise | L-Clockwise |
|---|---|---|
| Pickup | WALL_FLOWER (drawn), GARDEN | GARDEN (drawn), WALL_FLOWER |
| Shot trigger | CAMERA (drawn), TIMER | the same |
| Times | T2, TS, T3c, T3r, TRr, T3b, T3, TR | T1, TLr, TF, T4, TL |
| Partner line | "Partner runs L-Clockwise: Pickup GARDEN · Shot trigger CAMERA" | "Partner runs R-Clockwise: Pickup WALL_FLOWER · Shot trigger CAMERA" |

**Shot trigger TIMER** is for a robot that cannot see the HIVE. Each stop then fires its "yes"
row at its time and never takes the "no" row: set T1, T2 and T3 to the times the TIPs are expected
before PLAY. Both robots must show the same trigger.

## Spots

Team spots: RIGHT_START, LEFT_START, WALL_FLOWER_STAGING, GARDEN_STAGING, SCORING_FLOWER_STAGING,
RIGHT_PARK, LEFT_PARK.

Launch spots, all from `LaunchSpots` (the wall row), drawn for red:

| Spot | Who shoots there | Pose |
|---|---|---|
| RIGHT_START | R's preloads; R's TIP 3 volley; L's rescue of TIP 1 | (55.6, 7.7, 90°), back on the audience wall |
| LEFT_WALL_NEAR | R's first load | (37.8, 130.7, 295.5°), a corner on the far wall, beside the SCORING FLOWER |
| LEFT_WALL_HIVE | L's preloads; R's rescue of TIP 2 | (59.9, 133.4, 270°), back on the far wall |
| RIGHT_WALL_NEAR | L's pickup load | (35.9, 10.6, 66.9°), a corner on the audience wall |

They are not in `spots.json` (but for RIGHT_START): `LaunchSpots` is where they are written, and
the `.pp` files carry the same numbers, which `ClockwiseAutoTest` compares. A spot moved in
`LaunchSpots` means these two Autos exported again.

## The simulator's tables

Java simulator, `"calibration": "belding"`, real robot with the turret locked forward, both
robots' Belding loads, master's endgame guard, 24 seeds a row. "By 30 s" counts only TIPs settled
at 30 s on the robot's clock.

| R takes the WALL FLOWER, L the GARDEN | Three TIPs | Two TIPs or more (by 30 s) | One or more | Both parked | Robot contact | HIVE contact | Against the loop | G409 |
|---|---|---|---|---|---|---|---|---|
| Normal | 0 | 9 (9) | 24 | 24 | 0 | 0 | 0 | 0 |
| R 20% slow | 0 | 9 (9) | 24 | 24 | 0 | 0 | 0 | 1 |
| L 20% slow | 0 | 9 (9) | 24 | 24 | 0 | 0 | 0 | 0 |
| R's preload volley missed | 0 | 0 (0) | 23 | 24 | 0 | 0 | 0 | 0 |
| L's preload volley missed | 0 | 0 (0) | 21 | 24 | 0 | 0 | 0 | 0 |

| R takes the GARDEN, L the WALL FLOWER | Three TIPs | Two TIPs or more (by 30 s) | One or more | Both parked | Robot contact | HIVE contact | Against the loop | G409 |
|---|---|---|---|---|---|---|---|---|
| Normal | 0 | 3 (3) | 24 | 24 | 0 | 0 | 0 | 0 |
| R 20% slow | 0 | 5 (5) | 24 | 24 | 0 | 0 | 0 | 0 |
| L 20% slow | 0 | 3 (3) | 24 | 24 | 0 | 0 | 0 | 0 |
| R's preload volley missed | 0 | 0 (0) | 23 | 24 | 0 | 0 | 0 | 0 |
| L's preload volley missed | 0 | 0 (0) | 21 | 24 | 0 | 0 | 0 | 0 |

No robot's corner crossed the field's centre line in any run. Run from L's side
(`sim-requests/l-clockwise.json`) the rows give 7, 7, 7, 0, 0 and 2, 3, 2, 0, 0 two-TIP runs, and
one G409 run in each of two rows.

**The one G409 run** (R 20% slow, R on the WALL FLOWER): TIP 2 settled at 21.8 s as R was parking
from the SCORING FLOWER, and one spilled piece touched it. Not fixed.

### Against the session's goal

| Done when | WALL FLOWER choice | GARDEN choice |
|---|---|---|
| 1. Three TIPs in at least 50% of runs | **Short: 0 of 24** | **Short: 0 of 24** |
| 2. Two TIPs or more in 100%, forced fallbacks included | **Short:** normal 9 of 24; 0 of 24 in both fallbacks (one TIP in 23 and 21) | **Short:** normal 3 of 24; 0 of 24 in both fallbacks |
| 3. Both robots parked in 100% | Met: 120 of 120 | Met: 120 of 120 |
| 4. No contact and no lane against the loop in 100%, either robot 20% slow | Met: 0 in 120 (one G409 run) | Met: 0 in 120 |

## What a third TIP needs

R's time in the simulator at Belding's numbers (R on the WALL FLOWER):

| R's step | Seconds | Ends at |
|---|---|---|
| Preload volley | 3.7 | 3.7 |
| Drive to the WALL FLOWER | 2.0 | 5.6 |
| Pick | 3.2 | 8.8 |
| Outside lane to LEFT_WALL_NEAR | 3.2 | 12.0 |
| Volley at LEFT | 3.8 | 15.8 |
| Drive to the SCORING FLOWER | 1.4 | 17.2 |
| Pick | 2.9 | 20.1 |

R must start across by 16.2 s (T3c) and is ready at 20.1 s: 3.9 s short, 2.3 s of it the wait for
the spill. The rest is in the two volleys and the two picks.

**What a shorter feed bought, on the route before the Square spots** (R on the WALL FLOWER, normal,
120 seeds; a what-if, not the robot): a shot counter that works, 0.47 s between shots (gate to last
ball about 1.4 s, a 2.4 s volley), 0.98 of balls staying in, the guard planning that volley, and
T3c, T3r, T3b and T4 moved to match.

| Loads | Three TIPs | Two or more | G409 |
|---|---|---|---|
| Belding's | 16 of 120 | 78 of 120 | 0 |
| Every FLOWER load full | 38 of 120 | 108 of 120 | 0 |

This has not been rerun on the Square spots.

- **L's load is the next limit.** RIGHT needs seven or eight launched pieces for TIP 3: R's four
  and L's pickup. The simulated GARDEN gather takes two.
- **Crossing costs the TIP 2 rescue.** T3c at 0 in INIT keeps the rescue and gives up the third TIP.
- **Giving up the parks does not buy a third TIP in the simulator** (120 seeds, before the Square
  spots): with no time kept for R's park, three TIPs 0 and two or more 53 of 120 (from 97); with
  neither robot parking, five three-TIP runs, all settled after 30 s, and the robots touched in 68.

## What was added to run it

- **The kit:** `kit.route(option, index, card)`, `kit.shotWhen` and `kit.shotLimit`, and
  `AutoOption.time`, `shotTrigger` and `partner`.
- **The INIT menu:** `InitOptions`, for every Auto.
- **`Always`:** a trigger that is always true, for the clock checks.
- **The rule that a CELL is scored only from its own end**, in CLAUDE.md's hard constraints and
  `how-to-plan-an-auto.md`, with `AutoRegistrationTest` failing any launch command but `LaunchAll`
  and `LaunchOne`.
- **The simulator** (test code): `"loop"`, `"picks"`, `"partnerPicks"`, `"oursFails"`,
  `"ourSpeed"` and `"ourLaunchS"` in a request, `"fails": "misses preloads"` for a partner, and
  each row's `tipsBy30`, `g409`, HIVE contact, centre-line crossing and against-the-loop time for
  both robots (TeamCode/README.md, "Simulating an Auto").
- **The log reader:** `tools/pull-logs/read_logs.py steps`, the same step table for every Auto's
  log.
- **The Visualizer:** a route choice, camera-or-timer stops, times on the menu, the partner note,
  and `scripts/save-auto.mjs`, which saved these files.

The `.pp` files were assembled by a script from the route's points and saved with the Visualizer's
own layout and export. They have not been edited in the browser. The four plain drafts in
`drafts/` (one pickup each, no menu) are the same routes.

## Run it again

```bash
python3 tools/simulate-auto/simulate_auto.py r-clockwise --study --seeds 24
```

or, for one request as the workflow runs it:

```bash
mkdir -p /tmp/clockwise && cp TeamCode/sim-requests/r-clockwise.json /tmp/clockwise/request.json
BIOBUZZ_SIM_RUN=/tmp/clockwise ./gradlew :TeamCode:testDebugUnitTest --tests '*SimRunTest*' --rerun
```

`result.json`'s `entries` hold one row per run: `autoTips`, `tipsBy30`, `parked`, `partnerParked`,
`robotsCollidedAt`, `hitHiveAt`, `partnerHitHiveAt`, `crossedAt`, `againstLoopAt`,
`partnerAgainstLoopAt`, `g409`.
