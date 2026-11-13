# R-Clockwise / L-Clockwise: a paired Auto with fixed fallbacks

Successors to R-Infraviolet and L-Infraviolet. Mentor session, 11 to 12 Nov 2026. **Not yet run on
a robot:** Infraviolet stays on the Autonomous menu until Clockwise has driven on both robots.

Select **Autonomous → R-Clockwise** on the RIGHT_START robot and **L-Clockwise** on the LEFT_START
robot. They are a pair: run one only beside the other, with the two INIT menus set to match (each
page says what the partner's must show).

Open the pair together:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-clockwise.pp,l-clockwise.pp>

**Every volley is from a spot the drivers' buttons go to** (`localization/LaunchSpots`; mentor,
12 Nov 2026): the wall row (Square), but for R's third volley and its rescue of TIP 2, which are
from the close row (Triangle) because the drives to them are shorter. One set of spots for TeleOp
and Auto, so a TeleOp volley is data for the Auto and the speed table is tuned in one place.
`ClockwiseAutoTest` fails if a volley of the pair is from anywhere else.

**The robot's guard plans a volley at 2.5 s** (`AutoRegistration.LAUNCH_ALL_TYPICAL_S` and
`LAUNCH_ALL_LAST_PIECE_S`, 4.0 until 12 Nov 2026; the mentor's call, for every Auto). The times
below are set for that.

**With a load the robot drives nose-first** (mentor, 12 and 13 Nov 2026: every Auto on the
Autonomous menu does). The version with the backward legs, which is the one that has time to cross
for a third TIP, is kept as a draft: `drafts/r-clockwise-backward.pp` with
`drafts/l-clockwise-backward.pp`. On its first drive (20245,
alone) pieces left the intake on both legs it drove backward. After a pick it backs off the few
inches the pick itself does, then turns to face the way it is going. That costs time: the lane
from the WALL FLOWER is 4.4 s (3.2 s backward), and the road under the HIVE 6.2 s (2.7 s).

**What the Java simulator says** (24 seeds a row, Belding's volleys; tables below):

- Both robots park and the robots do not touch in every run, with either robot 20% slow and with
  either robot's preloads missed, for both pickup choices.
- **R looks before it crosses.** After the SCORING FLOWER it turns to the HIVE at the left end.
  RIGHT up means TIP 2 has come: it goes under the HIVE and shoots at RIGHT for TIP 3. RIGHT not up
  by 23.2 s: it shoots its load at LEFT to finish TIP 2, and parks.
- **The third volley may take the park's time** (mentor, 13 Nov 2026). A TIP counts as AUTO when
  the last ball is out by 30 s, so with the INIT row **Priority** on THREE_TIP (drawn) R shoots at
  RIGHT and ends AUTO there, not parked. On PARK it keeps its load and parks. R was not parked in
  8 of 24 normal runs with Belding's volleys, and in none with a working shot counter.
- Two TIPs come in 16 of 24 normal runs when R takes the WALL FLOWER, and none when R takes the
  GARDEN. Three TIPs: 0.
- R touches a spilled piece (G409) in 3 to 4 of 24 runs: it shoots its last load from the close
  row, where a spill lands.
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
| TIP 3 | Turn to the HIVE at LEFT_CLOSE_HIVE and look. RIGHT up: nose-first under the HIVE to RIGHT_CLOSE_HIVE, shoot at RIGHT. Not up: finish TIP 2 from where it stands | RIGHT_WALL_NEAR; shoot at RIGHT |
| Park | Outside lane to RIGHT_PARK | Outside lane to LEFT_PARK |

**The loop.** Under the HIVE is one-way, left to right. The outside lane is one-way, right to
left. No path of either Auto, fallbacks and parks included, drives a lane the other way; the
simulator checks every run (`"loop": "clockwise"`).

**The one variation.** R's first pickup is the WALL FLOWER (as drawn) or the GARDEN, and L takes
the other. It is one row of each robot's INIT menu, **Pickup**, and it changes one leg per robot.

**The parks are fixed.** R always ends on RIGHT_PARK and L on LEFT_PARK. Only the path in differs.

**R's loaded legs.** From the WALL FLOWER it turns north in the first eighth of the lane and
drives it nose-first, as R-Solo and Infraviolet do, turning to aim over the second half. From the
GARDEN the same. From the SCORING FLOWER to LEFT_CLOSE_HIVE it turns east, the way it is going,
then on round to the HIVE. The crossing is R-Infraviolet's road: forward out of the FLOWER, south
under the HIVE nose-first, and a half turn to aim at the end. L turns south out of the WALL FLOWER
before it turns to its launch spot.

**R does not wait for TIP 2's spill before it goes under the HIVE** (mentor, 12 Nov 2026: deal
with it if it shows on the field). An earlier drawing waited 2.3 s there. In a what-if with a short
feed, on the route before the Square spots, R drove into pieces still falling (G409) in 35 of 120
runs without the wait and 0 of 120 with it. The tables below show it in 2 to 5 of 24. `doc/robot-todo.md` has the video check. Another way round it, not drawn:
L takes the WALL FLOWER and R goes straight to the SCORING FLOWER, shoots it to make TIP 2, and
goes on under the HIVE to the GARDEN.

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

- **Before T3b: go and look.** R turns to the HIVE at LEFT_CLOSE_HIVE (it must turn round to go
  under the HIVE nose-first anyway) and asks: RIGHT up by T3?
  - **Yes: TIP 2 has come.** With Priority on THREE_TIP, R goes under the HIVE, shoots at RIGHT
    and parks if there is time left. With Priority on PARK it waits for L to pass and parks with
    its load.
  - **No: TIP 2 has not come.** R shoots its load at LEFT, then parks.
- **After T3b: park straight from the SCORING FLOWER.** The look, a volley and the park after it
  would not fit.

**Priority** is read by the kit (`AutoOption.PRIORITY`): on THREE_TIP the guard asks of a volley
only that its last ball is out by 30 s; on PARK a volley must leave time for the park, as in every
other Auto.

**One more clock check**, TS, sits after R's first LEFT volley: go for the SCORING FLOWER only
before TS. Later than that R parks from LEFT_WALL_NEAR at once, before L comes up the lane.

## The times

All on the robot's match clock (from its own PLAY). Each robot has **one** of them on its INIT menu, its
wait to see a TIP (L: TIP 1, the time T1; R: TIP 2, the time T3); the rest are fixed in the drawing. The values are the
Java simulator's, set to Belding; **every one is a robot check** (`doc/robot-todo.md` §3).

| Row | Value | Where it comes from |
|---|---|---|
| **T1** LEFT up by | 9.5 s | TIP 1 from R's preloads was read up at 6.5 to 8.8 s in the simulator. The Belding films have it settled as late as 9.98 s after our PLAY, so this is likely short |
| **T2** LEFT up by | 15.5 s | R is at LEFT_WALL_NEAR at 13.2 s. A volley begun later ends late enough that R's park would cross the lane as L comes up it |
| **TS** scoring FLOWER by | 17.6 s | R's LEFT volley (launch starts to feed ends) is over at 17.0 s. Later than 17.6 s, the pick and the park after it meet L in the lane |
| **T3b** look by | 22.2 s | The drive to LEFT_CLOSE_HIVE (1.7 s), a volley there (2.5 s), the park (about 2.6 s) and the guard's 1 s margin end at 30 s |
| **T3** RIGHT up by, at LEFT_CLOSE_HIVE | 23.2 s | The crossing from there (about 4.3 s) and a volley's last ball (2.5 s) end at 30 s. Not up by then: R shoots at LEFT |
| **TR** park from LEFT_CLOSE_HIVE at | 26.4 s | L has turned in to LEFT_PARK |
| **T4** RIGHT up by | 19.2 s | L's volley must end, and L be parked, before R leaves LEFT_CLOSE_HIVE for its park, even when the volley runs its whole 3.5 s feed |
| **TL** park at | 22.7 s | L leaves RIGHT_WALL_NEAR for LEFT_PARK once R has left the SCORING FLOWER |
| **TLr** park after the crossing at | 22.5 s | L1's "no" only: the TIP its volley makes was read up at 19.6 to 20.2 s, and its park goes through where that spill lands |
| **TF** FLOWER at | 15.5 s | L's WALL FLOWER leg only: R, coming north from the GARDEN, has passed the FLOWER |

**These times assume a 2.5 s volley.** A volley whose counter misses a ball feeds its whole 3.5 s
and runs about 4 s. In the simulator with Belding's volleys both robots still parked in every run,
but the margins that keep the robots apart are thinner than the table says.

## What the spots cost and bought

Two TIPs or more, R on the WALL FLOWER, normal, 24 seeds, Belding's volleys:

| Route | Two TIPs or more | Both parked | Robot contact |
|---|---|---|---|
| Hand-made pair spots, backward legs, rescue from LEFT_LAUNCH_CATCH, guard at 4.0 s | 18 of 24 | 24 | 0 |
| Wall row only, backward legs, guard at 4.0 s | 9 of 24 | 24 | 0 |
| Wall and close rows, backward legs, R crossing, guard at 2.5 s | 9 of 24 | 24 | 0 |
| Wall and close rows, nose-first legs, R staying left, guard at 2.5 s (now) | 16 of 24 | 24 | 0 |

- **Looking first keeps the TIP 2 rescue.** When R crossed blind, a short first eight at LEFT was
  not made up. Now R crosses only when it has seen RIGHT up, and otherwise shoots its SCORING
  FLOWER load at LEFT from the close row.
- **R 20% slow is too late to look** (T3b) and parks from the FLOWER: 8 of 24.
- **The wall-row shots are about 50 in** (LEFT_WALL_NEAR 51, LEFT_WALL_HIVE 50, RIGHT_START 50,
  RIGHT_WALL_NEAR 52, robot centre to the row); the close-row ones about 32 in. The calibration
  keeps about 0.91 of balls from the wall row and 0.70 from the close row, the second on thin
  evidence.
- **The close row is inside where the simulator lands a spill** (about 42 in from the row).
- **RIGHT_PARK moved** to (14.5, 98.7) on master (#516). The two parked robots are 2.7 in apart, so
  every way into either park now ends straight: L turns east of the park zone and drives in, and
  R's late park comes in along RIGHT_PARK's own line.

## Where the clock keeps the robots apart

Nothing on the robots tells one where the other is, so the times above keep them apart.

- **R's park crosses L's way to LEFT_PARK.** R parks either well before L comes up the lane (from
  LEFT_WALL_NEAR by T2 or TS, from the SCORING FLOWER at about 20.2 s) or after L has turned in
  (TR, from LEFT_CLOSE_HIVE).
- **A robot on LEFT_WALL_NEAR blocks the way into LEFT_PARK.** Its corner is inside the park's
  approach, so R never stands there late: T2 and TS send it to its park first.
- **R's park from the right side joins the lane L leaves by.** R is not at the right end before
  about 27 s, long after L has left (TL).
- With R on the GARDEN, R's lane north passes the WALL FLOWER where L picks. L waits until TF.

The speed tolerance beyond 20% slow has not been rerun on this route. Before the Square spots the
first contact was with L 30% slow (2 of 8 runs).

## The INIT menu

Gamepad 1's D-pad: **up / down selects a row, left / right changes it.** Three rows on R, two on L.

| Row | R-Clockwise | L-Clockwise |
|---|---|---|
| Waiting for TIP … until | **TIP 2** until 23.2 s (T3: RIGHT up, seen from LEFT_CLOSE_HIVE) | **TIP 1** until 9.5 s (T1: LEFT up) |
| Pickup | WALL_FLOWER (drawn), GARDEN | GARDEN (drawn), WALL_FLOWER |
| Priority | THREE_TIP (drawn), PARK | |

The two Pickup rows pair in order: R on WALL_FLOWER needs L on GARDEN, R on GARDEN needs L on
WALL_FLOWER. The page no longer says so; the drive teams read the rows to each other.

Every volley after the preloads waits for the camera to read its CELL up, with the wait's trigger
checked first: a CELL read up a moment after the time still gets its volley if the endgame guard says
it fits. There is no shot on a timer (mentor, 13 Nov 2026): a robot that cannot see the HIVE takes each
wait's Otherwise.

Both files are builder steps only (Visualizer `isSimple`), so they open and edit in the Simple
builder. The export guards each list that ends in a wait or a choice that parks, which the earlier
hand-made form did not. Simulated beside the earlier form (Belding, 24 runs, R on WALL_FLOWER,
Priority THREE_TIP): the same counts with both at speed, with L 20% slow, and with either preload
volley missed. With R 20% slow, R now crosses and shoots at RIGHT in 12 of 24 runs where it parked
before (both parked 12, was 22; G409 0, was 6).

## Spots

Team spots: RIGHT_START, LEFT_START, WALL_FLOWER_STAGING, GARDEN_STAGING, SCORING_FLOWER_STAGING,
RIGHT_PARK, LEFT_PARK.

Launch spots, all from `LaunchSpots`, drawn for red:

| Spot | Who shoots there | Pose |
|---|---|---|
| RIGHT_START | R's preloads; L's rescue of TIP 1 | (55.6, 7.7, 90°), back on the audience wall |
| LEFT_WALL_NEAR | R's first load | (37.8, 130.7, 295.5°), a corner on the far wall, beside the SCORING FLOWER |
| LEFT_WALL_HIVE | L's preloads | (59.9, 133.4, 270°), back on the far wall |
| RIGHT_WALL_NEAR | L's pickup load | (35.9, 10.6, 66.9°), a corner on the audience wall |
| RIGHT_CLOSE_HIVE | R's TIP 3 volley | (56.1, 25.3, 90°), the close row, open floor |
| LEFT_CLOSE_HIVE | R's rescue of TIP 2 | (59.9, 116.2, 270°), the close row, open floor |

They are not in `spots.json` (but for RIGHT_START): `LaunchSpots` is where they are written, and
the `.pp` files carry the same numbers, which `ClockwiseAutoTest` compares. A spot moved in
`LaunchSpots` means these two Autos exported again.

One more point is L-Clockwise's own, **LANE_END** (60.5, 34, 270°): where L comes out from under the
HIVE and holds for R to pass before the WALL FLOWER. It is a waypoint, not a place a drive team
puts the robot, so it is not a team spot; it has a name so the Visualizer keeps that drive's park.

## The simulator's tables

Java simulator, `"calibration": "belding"`, real robot with the turret locked forward, both
robots' Belding loads, master's endgame guard at 2.5 s, 24 seeds a row, `sim-requests/r-clockwise.json`.
"By 30 s" counts only TIPs settled at 30 s on the robot's clock. HIVE contact, a lane against the
loop and a corner over the centre line were 0 in every row of both settings.

**Belding's volleys** (the request as saved; Priority THREE_TIP):

| R takes the WALL FLOWER, L the GARDEN | Three TIPs | Two TIPs or more (by 30 s) | One or more | R parked | L parked | Robot contact | G409 runs |
|---|---|---|---|---|---|---|---|
| Normal | 0 | 16 (16) | 24 | 16 | 24 | 0 | 3 |
| R 20% slow | 0 | 8 (8) | 24 | 24 | 24 | 0 | 0 |
| L 20% slow | 0 | 16 (16) | 24 | 16 | 24 | 0 | 4 |
| R's preload volley missed | 0 | 0 (0) | 23 | 24 | 24 | 0 | 0 |
| L's preload volley missed | 0 | 3 (0) | 21 | 24 | 24 | 0 | 0 |

With R on the GARDEN every row has both robots parked, no contact and no second TIP: the nose-first
lane puts R's LEFT volley past TS, so it parks without the SCORING FLOWER.

**A shot counter that works** (0.47 s between shots for both robots, `BIOBUZZ_BELDING_WHOLE_FEED=0`),
R on the WALL FLOWER: two TIPs 16, 16 and 16 of 24 (normal, R slow, L slow); R parked 24, 19 and
24; no contact. With Priority on PARK, normal: 16 of 24, both parked 24.

The simulator still shows no third TIP: it has TIP 2 in under half the runs R could cross in, and
L's GARDEN load at two pieces. `sim-requests/l-clockwise.json` was run by CI, not here.

### Against the session's goal

| Done when | WALL FLOWER choice | GARDEN choice |
|---|---|---|
| 1. Three TIPs in at least 50% of runs | **Short: 0 of 24** | **Short: 0 of 24** |
| 2. Two TIPs or more in 100%, forced fallbacks included | **Short:** normal 16 of 24; R's preloads missed 0 of 24, L's 3 of 24 | **Short:** 0 of 24 in every row |
| 3. Both robots parked in 100% | **By choice, no:** R shoots its third volley and is not parked in 16 of 120 (Priority THREE_TIP). L 120 of 120 | Met: 120 of 120 |
| 4. No contact and no lane against the loop in 100%, either robot 20% slow | Met for robots, the HIVE and the loop: 0 in 120. **G409 in 7 of 120** | Met: 0 in 120 |

## What a third TIP needs

R's time in the simulator at Belding's numbers (R on the WALL FLOWER), beside its first drive
(20245 alone, 12 Nov 2026, on the backward legs):

| R's step | Simulator, now | Robot, backward legs |
|---|---|---|
| Preload volley | 3.7 | 3.2 |
| Drive to the WALL FLOWER | 2.0 | 2.0 |
| Pick | 3.2 | 3.2 (three pieces) |
| Outside lane to LEFT_WALL_NEAR | 4.4 nose-first | 3.0 backward |
| Volley at LEFT | 3.8 | 3.7 (three loaded, so the whole 3.5 s feed) |
| Drive to the SCORING FLOWER | 1.4 | 1.5 |
| Pick | 2.9 | 2.8 |
| Ready | 21.3 | 19.5 |
| Under the HIVE to RIGHT_CLOSE_HIVE | 6.2 nose-first | 2.4 backward |

Nose-first, R is at LEFT_CLOSE_HIVE at about 23.1 s in the simulator (about 21 s by the robot's
own times with a four-ball LEFT volley), the crossing from there is about 4.3 s, and the volley's
last ball leaves about 2 s after that: 27.5 to 29 s. The park does not fit after it, which is what
the Priority row decides. **Backing under the HIVE with a load** (2.4 s on the robot) would leave
time for both; a mechanical fix is being looked at.

**What a shorter feed bought, on the route before the Square spots** (R on the WALL FLOWER, normal,
120 seeds; a what-if, not the robot): a shot counter that works, 0.47 s between shots (gate to last
ball about 1.4 s, a 2.4 s volley), 0.98 of balls staying in, the guard planning that volley, and
the clock times moved to match.

| Loads | Three TIPs | Two or more | G409 |
|---|---|---|---|
| Belding's | 16 of 120 | 78 of 120 | 0 |
| Every FLOWER load full | 38 of 120 | 108 of 120 | 0 |

This has not been rerun on the present spots.

- **L's load is the next limit.** RIGHT needs seven or eight launched pieces for TIP 3: R's four
  and L's pickup. The simulated GARDEN gather takes two.
- **Crossing blind cost the TIP 2 rescue**; R now looks first.
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
- **The Visualizer:** a route choice, clock checks, timed holds,
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
