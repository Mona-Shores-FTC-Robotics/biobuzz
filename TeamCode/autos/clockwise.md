# R-Clockwise / L-Clockwise: a paired Auto with fixed fallbacks

Successors to R-Infraviolet and L-Infraviolet. Mentor session, 11 Nov 2026. **Not yet run on a
robot:** Infraviolet stays on the Autonomous menu until Clockwise has driven on both robots.

Select **Autonomous → R-Clockwise** on the RIGHT_START robot and **L-Clockwise** on the LEFT_START
robot. They are a pair: run one only beside the other, with the two INIT menus set to match (each
page says what the partner's must show).

Open the pair together:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-clockwise.pp,l-clockwise.pp>

**What the simulator says, set to the Belding Qualifier** (24 seeds a row; the full tables are below):

- Both robots park and nothing touches in every run, with either robot 20% slow and with either
  robot's preloads missed, for both pickup choices. No robot touches a spilled piece before it
  reaches the tiles (G409) in any run.
- At least one TIP comes in every normal run, and two in 18 of 24 when R takes the WALL FLOWER
  (3 of 24 when R takes the GARDEN).
- **Three TIPs never come at Belding's volley times.** R is ready to cross at about 19.4 s and
  must start by 16.2 s. With volleys of 2.4 s, three come in 16 of 120 runs with Belding's loads
  and 38 of 120 with every load full ("What a third TIP needs", below).
- **A missed R preload volley still gets TIP 1, in 23 of 24 runs, but never TIP 2.** L crosses
  and shoots its preloads at RIGHT from the right end; that TIP comes at 19.8 to 22.2 s.

## The plan

| | R-Clockwise | L-Clockwise |
|---|---|---|
| TIP 1 | Shoot the preloads at RIGHT at once, from RIGHT_START | Drive to LEFT_PAIR_CATCH and wait |
| First pickup | The WALL FLOWER, or the GARDEN | |
| TIP 2 | Outside lane to LEFT_PAIR_OFFSET; shoot at LEFT | Shoot the preloads at LEFT once it is up |
| Second pickup | The SCORING FLOWER | Under the HIVE; the GARDEN, or the WALL FLOWER |
| TIP 3 | Wait 2.3 s for TIP 2's spill to land, back straight out, under the HIVE to RIGHT_PAIR_CATCH; shoot at RIGHT | RIGHT_LAUNCH_OFFSET; shoot at RIGHT |
| Park | Outside lane to RIGHT_PARK | Outside lane to LEFT_PARK |

**The loop.** Under the HIVE is one-way, left to right. The outside lane is one-way, right to
left. No path of either Auto, fallbacks and parks included, drives a lane the other way; the
simulator checks every run (`"loop": "clockwise"`).

**The one variation.** R's first pickup is the WALL FLOWER (as drawn) or the GARDEN, and L takes
the other. It is one row of each robot's INIT menu, **Pickup**, and it changes one leg per robot:
the drive to the pickup, the grab, and the drive on to the launch spot. Every stop below is the
same for both.

**The parks are fixed.** R always ends on RIGHT_PARK and L on LEFT_PARK. Only the path in differs.

**R drives backward with a load** (mentor, 11 Nov 2026: it can). From the WALL FLOWER it turns the
short way, 123°, as it goes up the outside lane, where the first drawing turned 237°. From the
SCORING FLOWER it backs straight out and under the HIVE without turning, and arrives at the right
end already facing the HIVE. The first drawing made a forward U-turn there.

**R waits 2.3 s at the SCORING FLOWER before it backs under the HIVE.** TIP 2 dwells 3.6 to
4.2 s and then spills; a robot under the HIVE as it tips touches the falling pieces (G409). In a
what-if with short volleys, where R does cross, the first drawing touched a spilled piece in 35
of 120 runs with Belding's loads and 50 of 120 with full loads. With the wait, and both parks
drawn round the spill (R's right park goes west along the audience wall before it turns up the
lane; R's catch park goes west, north of the LEFT spill, first), it is 0 of 120 in both. The
simulator now counts G409 in every row (`g409` in `result.json`).

## The stops: one question each, "which CELL is up by this time?"

| Stop | Question | Yes | No |
|---|---|---|---|
| **L1:** L at LEFT_PAIR_CATCH, preloads | LEFT up by T1? | Shoot at LEFT | Cross under the HIVE, shoot at RIGHT from RIGHT_LAUNCH_CATCH to finish TIP 1, then park |
| **R2:** R at LEFT_PAIR_OFFSET, first load | LEFT up by T2? | Shoot at LEFT | Park |
| **R3:** R with the SCORING FLOWER load | RIGHT up by T3? | Shoot at RIGHT, park | Shoot at LEFT to finish TIP 2, then park |
| **L2:** L at RIGHT_LAUNCH_OFFSET, pickup load | RIGHT up by T4? | Shoot at RIGHT, park | Park |

L finishes TIP 1 and R finishes TIP 2. Neither robot needs to know why a TIP is late.

**A CELL is scored only from its own end of the field** (the two CELLs open away from each
other), so L1's "no" is a crossing, not a shot from the left spot. The first draft of this pair
had that shot; it hit the HIVE in every simulated run, and the rule is now in CLAUDE.md's hard
constraints. After the crossing and the volley there is no time left for L's pickup, so that row
ends in the park.

**R3 is asked at one end or the other, and the clock picks which.** R cannot shoot at RIGHT from
the left end or at LEFT from the right, and it cannot see the HIVE while it backs away from the
FLOWER. So when the SCORING FLOWER pick ends, R asks only the clock:

- **Before T3c: go for TIP 3.** R waits for the spill, backs under the HIVE and asks at
  RIGHT_PAIR_CATCH: RIGHT up by T3r? Yes, shoot at RIGHT and park by the outside lane. No, park
  the same way with the load. The "no" of the table is given up on this side: a robot fast enough
  to cross cannot also finish TIP 2.
- **After T3c: too late for TIP 3.** R turns to face the HIVE at LEFT_LAUNCH_CATCH and asks
  there: RIGHT up by T3? Yes, TIP 2 is done and it parks. No, it shoots its load at LEFT to
  finish TIP 2 (only before T3b, so the park still fits) and parks.

Setting **T3c to 0 in INIT** keeps R on the left: the pair then never tries for a third TIP and
always keeps the TIP 2 rescue.

**One more clock check**, TS, sits after R's first LEFT volley: go for the SCORING FLOWER only
before TS. Later than that the trip ends with nothing left to shoot and would meet L in the lane;
R parks instead.

## The times

All on the match clock, all rows of the INIT menu. The values are the simulator's, set to
Belding; **every one is a robot check** (`doc/robot-todo.md` §3).

| Row | Value | Where it comes from |
|---|---|---|
| **T1** LEFT up by | 9.5 s | TIP 1 from R's preloads was seen at 6.5 to 8.8 s (median 6.7; Belding Q-4, about 7.5 s) |
| **T2** LEFT up by | 21 s | Covers most of the rescue: L leaves at T1, is across at 13.9 s and has shot by 16.8 s; that TIP was seen at 18.7 to 22.2 s |
| **TS** scoring FLOWER by | 19.4 s | R's LEFT volley ends at 15.6 s (16.5 s at 20% slow, 18.0 s after a GARDEN pickup) |
| **T3c** cross by | 16.2 s | The wait for the spill (2.3 s), the crossing (3.3 s), the volley (4.0 s), the park from RIGHT_PAIR_CATCH (3.2 s) and the guard's 1 s margin end at 30 s |
| **T3r** RIGHT up by, at the right spot | 21.8 s | The volley, that park and the margin |
| **TRr** park from the right at | 24.0 s | L has left RIGHT_LAUNCH_OFFSET for its park (TL), so R does not meet it joining the lane |
| **T3** RIGHT up by | 22.3 s | R is at LEFT_LAUNCH_CATCH at 21.3 s; a TIP 2 from the first eight pieces came at 18.6 to 20.7 s |
| **T3b** LEFT shot by | 22.7 s | The volley (4.0 s), the park from there (2.3 s) and the margin |
| **TR** park at | 26.7 s | R leaves LEFT_LAUNCH_CATCH for RIGHT_PARK: the latest the guard allows, and L has passed |
| **T4** RIGHT up by | 19.2 s | L's volley must end, and L pass the left end, before R parks at TR |
| **TL** park at | 22.7 s | L leaves RIGHT_LAUNCH_OFFSET for LEFT_PARK once R has left the SCORING FLOWER |
| **TLr** park after the crossing at | 24.6 s | L1's "no" only: L leaves RIGHT_LAUNCH_CATCH once R, which shot late at LEFT, has parked from the left spot (by 26.6 s) |
| **TF** FLOWER at | 15.5 s | L's WALL FLOWER leg only: R, coming north from the GARDEN, has passed the FLOWER |

One set serves both pickup choices: the only time that differs by pickup, TF, belongs to L's
WALL FLOWER leg alone.

## Where the clock keeps the robots apart

The lane study found the places where the two routes meet however the paths are drawn. Nothing
on the robots tells one where the other is, so the times above keep them apart.

- **R's park from the left side crosses L's way to LEFT_PARK.** LEFT_PARK is beyond RIGHT_PARK,
  so L passes RIGHT_PARK on its outside while R, parking from the left end, comes in across that
  line. L goes first: it leaves at TL and is parked by 26.2 s (27.4 s at 20% slow). R stands at
  LEFT_LAUNCH_CATCH until TR and crosses at about 27.6 s.
- **L's lane passes within 2 in of a robot at the SCORING FLOWER.** RIGHT_PARK and
  SCORING_FLOWER_STAGING leave a 17 in gap for a 16 in robot. R is at the FLOWER from 17.0 to
  19.6 s (later at 20% slow), and L reaches it no sooner than 24.4 s.
- **R's park from the right side joins the lane L leaves by.** R waits until TRr, when L has
  gone, and follows it up the lane; nothing crosses.
- With R on the GARDEN, R's lane north passes the WALL FLOWER where L picks. L waits at the lane's
  end until TF.

**How much slack that is.** In the simulator the separation held with either robot from 20% fast
to 40% slow, except L: at 30% slow L touched R in 2 of 8 runs (R on the WALL FLOWER), and at 40%
slow in 7 of 8 for either pickup (8 seeds a row, on the route as it is now). A real drive is not the simulator's (Belding's 270° curve to
the LEFT spots took 3.5 to 4.6 s against a drawn 2.9 s), so these margins are the first thing to
check on the field.

## The INIT menu

Gamepad 1's D-pad: **up / down selects a row, left / right changes it.** The page shows the
selected row, what the partner's menu must show, and every row's value.

| Row | R-Clockwise | L-Clockwise |
|---|---|---|
| Pickup | WALL_FLOWER (drawn), GARDEN | GARDEN (drawn), WALL_FLOWER |
| Shot trigger | CAMERA (drawn), TIMER | the same |
| Times | T2, TS, T3c, T3r, TRr, T3, T3b, TR | T1, TLr, TF, T4, TL |
| Partner line | "Partner runs L-Clockwise: Pickup GARDEN · Shot trigger CAMERA" | "Partner runs R-Clockwise: Pickup WALL_FLOWER · Shot trigger CAMERA" |

**Shot trigger TIMER** is for a robot that cannot see the HIVE. Each stop then fires its "yes"
row at its time and never takes the "no" row. The drawn times are deadlines, late enough to cover
a rescue, so on the timer a stop shoots late: set T1, T2 and T3 to the times the TIPs are expected
before PLAY. Both robots must show the same trigger.

## Spots

Team spots, unchanged: RIGHT_START, LEFT_START, WALL_FLOWER_STAGING, GARDEN_STAGING,
SCORING_FLOWER_STAGING, LEFT_LAUNCH_CATCH (R's rescue, after L has left), RIGHT_LAUNCH_CATCH
(L1's "no"), RIGHT_LAUNCH_OFFSET, RIGHT_PARK, LEFT_PARK.

Three launch spots are the pair's own, because two robots stand at one end at once and
LEFT_LAUNCH_CATCH and LEFT_LAUNCH_OFFSET overlap (as do the RIGHT ones):

| Spot | Who | Pose | Range to its CELL | Clear of the partner |
|---|---|---|---|---|
| LEFT_PAIR_OFFSET | R | (40.0, 128.0, 294.4°) | 51.0 in | 3.0 in from L at LEFT_PAIR_CATCH; 3.2 in from the far wall |
| LEFT_PAIR_CATCH | L | (61.5, 123.5, 267.6°) | about 43 in | |
| RIGHT_PAIR_CATCH | R | (61.0, 13.0, 96.2°) | 48.0 in | 2.5 in from L at RIGHT_LAUNCH_OFFSET; its corner 0.9 in inside the field's centre line |

**LEFT_PAIR_OFFSET is 24.4° off square to the CELL.** The first drawing's (36.0, 121.5, 302.8°)
was 32.8° off, beyond the 26° or so the launch-spots work (#536) found a shot still goes in at.
The spot moved toward the CELL's centre line, and L moved 4 in east, to LEFT_PAIR_CATCH, to make
room. It is 4 in longer (51.0 against 46.9 in).

None is in `spots.json`: they become team spots through the Visualizer's *Save team spots*
once a robot has shot from them. Infraviolet's RIGHT_PAIR_CLOSE (30 in) is not used: the Belding
calibration keeps 0.70 of shots from the CLOSE spots and 0.95 from 42 in.

## The lane study

Both pickup choices were first drawn as plain drafts, with no menu, and simulated before anything
else was built: `drafts/r-clockwise-flower.pp` with `drafts/l-clockwise-garden.pp`, and
`drafts/r-clockwise-garden.pp` with `drafts/l-clockwise-flower.pp`. The pair's numbers match the
drafts' in every row.

**Simulator, Belding calibration** (`"calibration": "belding"`), real robot with the turret locked
forward, both robots' Belding loads, master's endgame guard, 24 seeds a row. "Two TIPs" and "three
TIPs" are the simulator's count, which takes a TIP that completes in the transition; "by 30 s"
counts only those complete at 30 s.

| R takes the WALL FLOWER, L the GARDEN | Three TIPs | Two TIPs or more (by 30 s) | One or more | Both parked | Robot contact | HIVE contact | Against the loop |
|---|---|---|---|---|---|---|---|
| Normal | 0 | 18 (18) | 24 | 24 | 0 | 0 | 0 |
| R 20% slow | 0 | 18 (18) | 24 | 24 | 0 | 0 | 0 |
| L 20% slow | 0 | 18 (18) | 24 | 24 | 0 | 0 | 0 |
| R's preload volley missed | 0 | 0 (0) | 23 | 24 | 0 | 0 | 0 |
| L's preload volley missed | 0 | 6 (4) | 21 | 24 | 0 | 0 | 0 |

| R takes the GARDEN, L the WALL FLOWER | Three TIPs | Two TIPs or more (by 30 s) | One or more | Both parked | Robot contact | HIVE contact | Against the loop |
|---|---|---|---|---|---|---|---|
| Normal | 0 | 3 (3) | 24 | 24 | 0 | 0 | 0 |
| R 20% slow | 0 | 4 (4) | 24 | 24 | 0 | 0 | 0 |
| L 20% slow | 0 | 3 (3) | 24 | 24 | 0 | 0 | 0 |
| R's preload volley missed | 0 | 0 (0) | 23 | 24 | 0 | 0 | 0 |
| L's preload volley missed | 0 | 0 (0) | 21 | 24 | 0 | 0 | 0 |

No robot's corner crossed the field's centre line, and no robot touched a spilled piece before
it reached the tiles (G409), in any run. Run from L's side (`sim-requests/l-clockwise.json`, L
as the first robot), the same rows give the same counts but for the luck of the draw: L's
preloads missed, 6 (5 by 30 s); R on the GARDEN, 2, 4 and 2 in the first three rows.

L's preloads are missed on the row where L waits for LEFT to come up; in the three runs with no
TIP 1 from R, L crosses and its volley at RIGHT is the one that goes wide, so those runs have no
TIP (21 of 24).

### Against the goal

| Done when | WALL FLOWER choice | GARDEN choice |
|---|---|---|
| 1. Three TIPs in at least 50% of runs | **Short: 0 of 24** | **Short: 0 of 24** |
| 2. Two TIPs or more in 100%, forced fallbacks included | **Short:** normal 18 of 24; R's preloads missed 0 of 24 (one TIP in 23); L's preloads missed 6 of 24 | **Short:** normal 3 of 24; 0 of 24 in both fallbacks (one TIP in 23 and 21) |
| 3. Both robots parked in 100% | Met: 120 of 120 | Met: 120 of 120 |
| 4. No contact and no lane against the loop in 100%, either robot 20% slow | Met: 0 in 120 (and no G409) | Met: 0 in 120 (and no G409) |

### What a third TIP needs

R's time, as the simulator has it at Belding's numbers (R on the WALL FLOWER):

| R's step | Seconds | Ends at |
|---|---|---|
| Preload volley | 3.7 | 3.7 |
| Drive to the WALL FLOWER | 2.0 | 5.6 |
| Pick | 3.2 | 8.8 |
| Outside lane to LEFT_PAIR_OFFSET | 3.1 (the first drawing: 4.0) | 12.0 |
| Volley at LEFT | 3.8 | 15.7 |
| Drive to the SCORING FLOWER | 1.2 | 17.0 |
| Pick | 2.4 to 2.7 | 19.4 to 19.6 |

R must start across by 16.2 s (T3c) and is ready at about 19.4 s: 3.2 s short, 2.3 s of it the
wait for TIP 2's spill. The redraw took a second off the outside lane and two off the move after
the SCORING FLOWER; the rest is in the volleys and the picks.

**What volley time buys** (R on the WALL FLOWER, normal, 120 seeds; a what-if, not the robot): a
shot counter that works, 0.47 s between shots for both robots (a 2.4 s volley), 0.98 of shots
staying in, the guard planning the volley at that length, and T3c, T3r, T3b and T4 moved to
match.

| Loads | Three TIPs | Two or more | G409 |
|---|---|---|---|
| Belding's | 16 of 120 | 78 of 120 | 0 |
| Every FLOWER load full | 38 of 120 | 108 of 120 | 0 |

An earlier sweep, before the wait for the spill (24 seeds a row), found nothing more below 2.4 s
and no third TIP at 2.8 s or longer with Belding's loads.

- **Shorter volleys get R across in time**, at about 2.4 s. Below that nothing more is gained:
  the next limit is elsewhere.
- **Crossing costs the TIP 2 rescue.** Once R is fast enough to cross, a short first eight at
  LEFT is no longer made up from the left end. T3c at 0 in INIT puts the rescue back and gives
  up the third TIP.
- **L's load is the next limit.** RIGHT needs seven or eight launched pieces for TIP 3: R's four
  and L's pickup. The simulated GARDEN gather takes two (the robot's take 0 to 2), so the third
  TIP came only in runs where L held more. A GARDEN gather that takes four, or L on a FLOWER,
  comes before any more volley time.
- **Eight of eight must stay in at LEFT** on the field that tips at eight (about 0.91 from
  LEFT_PAIR_OFFSET at 51 in, 0.95 from LEFT_PAIR_CATCH), or TIP 2 never comes and R has crossed.
- **A full lane read at the FLOWERs** (the pick's dwell ending on full) is not in this table: the
  simulator's picks are already 2.4 and 3.2 s, where Belding's ran 2.7 to 4.0 s (median 3.7 s).
  On the robot it is worth another second or so.
- **The old guard alone changes nothing.** Run with a volley allowed before the park
  (`BIOBUZZ_AUTO_BELDING_GUARD=1`), every row of both tables above is the same: T3c is this
  Auto's own check, set so the park fits.

### Why two TIPs are not certain

- **A late TIP 1 leaves no time for TIP 2.** R's preloads stay in 0.90 of the time from
  RIGHT_START, and in 3 of 24 runs too few do. L then crosses and finishes TIP 1 from the right
  end, at 18.7 to 19.8 s (19.8 to 22.2 s with all four of R's preloads wide). R shoots its first
  load at LEFT at once, but its SCORING FLOWER load could not be shot and parked by 30 s (TS), so
  LEFT gets four pieces and needs seven or eight. One TIP in 23 of 24; the other run saw LEFT up
  after T2 and R had parked.
- **Half the two-TIP runs need the R3 rescue.** Of the 18, nine came from the first eight pieces
  (TIP 2 at 18.6 to 20.7 s) and nine from R's SCORING FLOWER load at LEFT (26.7 to 27.9 s). A 20% slow
  R now makes the rescue too (18 of 24; 9 of 24 on the first drawing).
- **With L's preloads missed** LEFT gets only R's two loads, and both must be full and stay in:
  6 of 24, four of them complete by 30 s.
- **The GARDEN gives two pieces, not four.** R's first LEFT volley then cannot make eight, and R
  is back from the SCORING FLOWER too late to shoot again.

## What was added to run it

- **The kit:** `kit.route(option, index, card)` (one card of a leg, built only when picked),
  `kit.shotWhen` and `kit.shotLimit` (a stop that shoots by the camera or the timer), and
  `AutoOption.time`, `shotTrigger` and `partner`.
- **The INIT menu:** `InitOptions`, for every Auto. L-NoCross's partner wait and any Auto's PARK
  are rows of it.
- **`Always`:** a trigger that is always true, for the clock checks.
- **The rule that a CELL is scored only from its own end**, in CLAUDE.md's hard constraints and
  `how-to-plan-an-auto.md`, with `AutoRegistrationTest` failing any launch command but
  `LaunchAll` and `LaunchOne`.
- **The simulator** (test code): `"loop"`, `"picks"`, `"partnerPicks"`, `"oursFails"`,
  `"ourSpeed"` and `"ourLaunchS"` in a request, `"fails": "misses preloads"` for a partner, and
  each row's `tipsBy30`, `g409`, HIVE contact, centre-line crossing and against-the-loop time for both
  robots (TeamCode/README.md, "Simulating an Auto").
- **The Visualizer:** a route choice, camera-or-timer stops, times on the menu, the partner note,
  and `scripts/save-auto.mjs`, which saved these files (Visualizer `docs/auto-format.md`, "A
  paired Auto").

The `.pp` files were assembled by a script from the route's points and saved with the
Visualizer's own layout and export. They have not been edited in the browser.

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

The volley what-if is the same request with `"ourLaunchS"` and the partner's
`"timing": {"launchS": …}` set to the gap between shots, the four times in `"picks"`, and
`BIOBUZZ_BELDING_WHOLE_FEED=0 BIOBUZZ_SIM_VOLLEY_PLANNED_S=<volley seconds>` in the environment
(`BIOBUZZ_BELDING_ROBOT=full` for full loads).
