# R-Clockwise / L-Clockwise: a paired Auto with fixed fallbacks

Successors to R-Infraviolet and L-Infraviolet. Mentor session, 11 Nov 2026. **Not yet run on a
robot:** Infraviolet stays on the Autonomous menu until Clockwise has driven on both robots.

Select **Autonomous → R-Clockwise** on the RIGHT_START robot and **L-Clockwise** on the LEFT_START
robot. They are a pair: run one only beside the other, with the two INIT menus set to match (each
page says what the partner's must show).

Open the pair together:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-clockwise.pp,l-clockwise.pp>

**What the simulator says, set to the Belding Qualifier** (24 seeds a row; the full table is below):

- Both robots park and nothing touches in every run, with either robot 20% slow and with either
  robot's preloads missed, for both pickup choices.
- At least one TIP comes in every normal run, and two in 18 of 24 when R takes the WALL FLOWER
  (4 of 24 when R takes the GARDEN).
- **Three TIPs never come.** With Belding's volleys and dwell, TIP 2 is seen at 20 s at the
  earliest, and R would have to start across by 17.9 s to shoot and still park.
- **A missed R preload volley still gets TIP 1, in 23 of 24 runs, but never TIP 2.** L crosses
  and shoots its preloads at RIGHT from the right end; that TIP comes at 19.8 to 22.2 s, too late
  for a second.

## The plan

| | R-Clockwise | L-Clockwise |
|---|---|---|
| TIP 1 | Shoot the preloads at RIGHT at once, from RIGHT_START | Drive to LEFT_LAUNCH_CATCH and wait |
| First pickup | The WALL FLOWER, or the GARDEN | |
| TIP 2 | Outside lane to LEFT_PAIR_OFFSET; shoot at LEFT | Shoot the preloads at LEFT once it is up |
| Second pickup | The SCORING FLOWER, then LEFT_LAUNCH_CATCH | Under the HIVE; the GARDEN, or the WALL FLOWER |
| TIP 3 | Under the HIVE to RIGHT_PAIR_CATCH; shoot at RIGHT | RIGHT_LAUNCH_OFFSET; shoot at RIGHT |
| Park | Outside lane to RIGHT_PARK | Outside lane to LEFT_PARK |

**The loop.** Under the HIVE is one-way, left to right. The outside lane is one-way, right to
left. No path of either Auto, fallbacks and parks included, drives a lane the other way; the
simulator checks every run (`"loop": "clockwise"`).

**The one variation.** R's first pickup is the WALL FLOWER (as drawn) or the GARDEN, and L takes
the other. It is one row of each robot's INIT menu, **Pickup**, and it changes one leg per robot:
the drive to the pickup, the grab, and the drive on to the launch spot. Every stop below is the
same for both.

**The parks are fixed.** R always ends on RIGHT_PARK and L on LEFT_PARK. Only the path in differs.

## The stops: one question each, "which CELL is up by this time?"

| Stop | Question | Yes | No |
|---|---|---|---|
| **L1:** L at LEFT_LAUNCH_CATCH, preloads | LEFT up by T1? | Shoot at LEFT | Cross under the HIVE, shoot at RIGHT from RIGHT_LAUNCH_CATCH to finish TIP 1, then park |
| **R2:** R at LEFT_PAIR_OFFSET, first load | LEFT up by T2? | Shoot at LEFT | Park |
| **R3:** R at LEFT_LAUNCH_CATCH, SCORING FLOWER load | RIGHT up by T3? | Cross, shoot at RIGHT, park | Shoot at LEFT to finish TIP 2, then park |
| **L2:** L at RIGHT_LAUNCH_OFFSET, pickup load | RIGHT up by T4? | Shoot at RIGHT, park | Park |

L finishes TIP 1 and R finishes TIP 2. Neither robot needs to know why a TIP is late.

**A CELL is scored only from its own end of the field** (the two CELLs open away from each
other), so L1's "no" is a crossing, not a shot from the left spot. The first draft of this pair
had that shot; it hit the HIVE in every simulated run, and the rule is now in CLAUDE.md's hard
constraints. After the crossing and the volley there is no time left for L's pickup, so that row
ends in the park.

**Three clock checks sit behind the stops.** Each asks only "is there still time?", and none looks
at the HIVE:

- **TS**, after R's first LEFT volley: go for the SCORING FLOWER only before TS. Later than that
  the trip ends with nothing left to shoot, and it would meet L in the lane; R parks instead.
- **T3b**, on R3's "no": the LEFT volley starts only before T3b, so the park after it still fits.
- **T3c**, on R3's "yes": R crosses only before T3c, the latest start from which the crossing, the
  volley and the park from the right side all end by 30 s.

## The times

All on the match clock, all rows of the INIT menu. The values are the simulator's, set to
Belding; **every one is a robot check** (`doc/robot-todo.md` §3).

| Row | Value | Where it comes from |
|---|---|---|
| **T1** LEFT up by | 9.5 s | TIP 1 from R's preloads was seen at 6.5 to 8.8 s (median 6.7; Belding Q-4, about 7.5 s) |
| **T2** LEFT up by | 21 s | Covers most of the rescue: L leaves at T1, is across at 13.9 s and has shot by 16.8 s; that TIP was seen at 18.7 to 22.2 s |
| **TS** scoring FLOWER by | 19.4 s | R's LEFT volley ends at 16.6 s (18.6 s at 20% slow), or 20.3 s after a GARDEN pickup, which is too late |
| **T3** RIGHT up by | 22.3 s | R is back at LEFT_LAUNCH_CATCH at 22.1 s; a TIP 2 from the first eight pieces came at 20.0 to 21.2 s |
| **T3b** LEFT shot by | 22.8 s | The volley (4.0 s), the park from there (2.2 s) and the guard's 1 s margin end at 30 s |
| **T3c** cross by | 17.9 s | The crossing (3.6 s), the volley (4.0 s), the park from RIGHT_PAIR_CATCH (3.5 s) and the margin |
| **TR** park at | 26.8 s | R leaves LEFT_LAUNCH_CATCH for RIGHT_PARK: the latest the guard allows, and L has passed |
| **T4** RIGHT up by | 19.2 s | L's volley must end, and L pass the left end, before R parks at TR |
| **TL** park at | 22.7 s | L leaves RIGHT_LAUNCH_OFFSET for LEFT_PARK once R has left the SCORING FLOWER |
| **TLr** park after the crossing at | 24.6 s | L1's "no" only: L leaves RIGHT_LAUNCH_CATCH once R, which shot late at LEFT, has parked from the left spot (by 26.6 s) |
| **TF** FLOWER at | 15.5 s | L's WALL FLOWER leg only: R, coming north from the GARDEN, has passed the FLOWER |

The goal asked for one value per pickup choice. One set serves both: the only time that differs
by pickup, TF, belongs to L's WALL FLOWER leg alone.

## Two crossings the clock keeps apart

The lane study found two places where the two routes meet however the paths are drawn. Nothing on
the robots tells one where the other is, so the times above keep them apart.

- **R's park from the left side crosses L's way to LEFT_PARK.** LEFT_PARK is beyond RIGHT_PARK,
  so L passes RIGHT_PARK on its outside while R, parking from the left end, comes in across that
  line. L goes first: it leaves at TL and is parked by 26.2 s (27.4 s at 20% slow). R stands at
  LEFT_LAUNCH_CATCH until TR and crosses at about 27.6 s. When R parks from the right side, in the
  normal plan, both drive the lane the same way and nothing crosses.
- **L's lane passes within 2 in of a robot at the SCORING FLOWER.** RIGHT_PARK and
  SCORING_FLOWER_STAGING leave a 17 in gap for a 16 in robot. R is at the FLOWER from 17.8 to
  20.6 s (to 22.9 s at 20% slow), and L reaches it no sooner than 24.4 s.
- With R on the GARDEN, R's lane north passes the WALL FLOWER where L picks. L waits at the lane's
  end until TF.

**How much slack that is.** In the simulator the separation held with either robot from 20% fast
to 40% slow, except L: at 30% slow L touched R in 2 of 8 runs, and at 40% slow in 7 of 8, with L
not parked in 2. A real drive is not the simulator's (Belding's 270° curve to the LEFT spots took
3.5 to 4.6 s against a drawn 2.9 s), so these margins are the first thing to check on the field.

## The INIT menu

Gamepad 1's D-pad: **up / down selects a row, left / right changes it.** The page shows the
selected row, what the partner's menu must show, and every row's value.

| Row | R-Clockwise | L-Clockwise |
|---|---|---|
| Pickup | WALL_FLOWER (drawn), GARDEN | GARDEN (drawn), WALL_FLOWER |
| Shot trigger | CAMERA (drawn), TIMER | the same |
| Times | T2, TS, T3, T3b, TR, T3c | T1, TLr, TF, T4, TL |
| Partner line | "Partner runs L-Clockwise: Pickup GARDEN · Shot trigger CAMERA" | "Partner runs R-Clockwise: Pickup WALL_FLOWER · Shot trigger CAMERA" |

**Shot trigger TIMER** is for a robot that cannot see the HIVE. Each stop then fires its "yes"
row at its time and never takes the "no" row. The drawn times are deadlines, late enough to cover
a rescue, so on the timer a stop shoots late: set T1, T2 and T3 to the times the TIPs are expected
before PLAY. Both robots must show the same trigger.

## Spots

Team spots, unchanged: RIGHT_START, LEFT_START, WALL_FLOWER_STAGING, GARDEN_STAGING,
SCORING_FLOWER_STAGING, LEFT_LAUNCH_CATCH, RIGHT_LAUNCH_CATCH (L1's "no"), RIGHT_LAUNCH_OFFSET,
RIGHT_PARK, LEFT_PARK.

Two launch spots are the pair's own, because two robots stand at one end at once and
LEFT_LAUNCH_CATCH and LEFT_LAUNCH_OFFSET overlap (as do the RIGHT ones):

| Spot | Pose | Range to its CELL | Clear of the partner |
|---|---|---|---|
| LEFT_PAIR_OFFSET | (36.0, 121.5, 302.8°) | 46.9 in | 2.8 in from L at LEFT_LAUNCH_CATCH |
| RIGHT_PAIR_CATCH | (63.0, 18.0, 99.9°) | 43.3 in | 2.5 in from L at RIGHT_LAUNCH_OFFSET |

Neither is in `spots.json`: they become team spots through the Visualizer's *Save team spots*
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
| R 20% slow | 0 | 9 (9) | 24 | 24 | 0 | 0 | 0 |
| L 20% slow | 0 | 18 (18) | 24 | 24 | 0 | 0 | 0 |
| R's preload volley missed | 0 | 0 (0) | 23 | 24 | 0 | 0 | 0 |
| L's preload volley missed | 0 | 4 (2) | 21 | 24 | 0 | 0 | 0 |

| R takes the GARDEN, L the WALL FLOWER | Three TIPs | Two TIPs or more (by 30 s) | One or more | Both parked | Robot contact | HIVE contact | Against the loop |
|---|---|---|---|---|---|---|---|
| Normal | 0 | 4 (4) | 24 | 24 | 0 | 0 | 0 |
| R 20% slow | 0 | 5 (5) | 24 | 24 | 0 | 0 | 0 |
| L 20% slow | 0 | 3 (3) | 24 | 24 | 0 | 0 | 0 |
| R's preload volley missed | 0 | 0 (0) | 23 | 24 | 0 | 0 | 0 |
| L's preload volley missed | 0 | 0 (0) | 21 | 24 | 0 | 0 | 0 |

Run from L's side (`sim-requests/l-clockwise.json`, L as the first robot), the same rows give the
same counts but for the luck of the draw in three: L's preloads missed, 6 (2 by 30 s); R on the
GARDEN, normal 2 and R slow 6.

L's preloads are missed on the row where L waits for LEFT to come up; in the three runs with no
TIP 1 from R, L crosses and its volley at RIGHT is the one that goes wide, so those runs have no
TIP (21 of 24).

### Against the goal

| Done when | WALL FLOWER choice | GARDEN choice |
|---|---|---|
| 1. Three TIPs in at least 50% of runs | **Short: 0 of 24** | **Short: 0 of 24** |
| 2. Two TIPs or more in 100%, forced fallbacks included | **Short:** normal 18 of 24; R's preloads missed 0 of 24 (one TIP in 23); L's preloads missed 4 of 24 | **Short:** normal 4 of 24; 0 of 24 in both fallbacks (one TIP in 23 and 21) |
| 3. Both robots parked in 100% | Met: 120 of 120 | Met: 120 of 120 |
| 4. No contact and no lane against the loop in 100%, either robot 20% slow | Met: 0 in 120 | Met: 0 in 120 |

### Why three TIPs never come

- A TIP sits out a 3.6 to 4.2 s dwell at Belding, and a volley after a pickup runs its whole feed,
  about 3.9 s.
- TIP 1 is seen at 6.7 s. The eighth piece reaches LEFT at about 16 s, so TIP 2 is seen at 20.0 s
  at the earliest.
- R's volley at RIGHT then needs the crossing (3.6 s), the volley (4.0 s) and the park from the
  right side (3.5 s, RIGHT_PARK is at the far end) plus the guard's margin: R must start across by
  17.9 s. It is still picking the SCORING FLOWER then.
- Master's guard never lets a volley take the park's time, so R parks from the left. L's load
  alone cannot tip the RIGHT CELL (it takes seven or eight launched pieces).
- With every load full (`BIOBUZZ_BELDING_ROBOT=full`), two TIPs come in 21 of 24 normal runs,
  every run that has a TIP 1, and three TIPs still in none.

### Why two TIPs are not certain

- **A late TIP 1 leaves no time for TIP 2.** R's preloads stay in 0.90 of the time from
  RIGHT_START, and in 3 of 24 runs too few do. L then crosses and finishes TIP 1 from the right
  end, at 18.7 to 19.8 s (19.8 to 22.2 s with all four of R's preloads wide). R shoots its first
  load at LEFT at once, but its SCORING FLOWER load could not be shot and parked by 30 s (TS), so
  LEFT gets four pieces and needs seven or eight. One TIP in 23 of 24; the other run saw LEFT up
  after T2 and R had parked.
- **Half the two-TIP runs need the R3 rescue.** Of the 18, nine came from the first eight pieces
  (TIP 2 at 20.0 to 21.2 s) and nine from R's SCORING FLOWER load at LEFT (27.2 to 28.4 s). A slow
  R is back at the stop after T3b, skips the volley and parks: 9 of 24.
- **With L's preloads missed** LEFT gets only R's two loads, and both must be full and stay in:
  4 of 24, three of them complete by 30 s.
- **The GARDEN gives two pieces, not four.** The simulated gather takes 2 of 4, as the robot's do
  (0 to 2). R's first LEFT volley then cannot make eight, and R leaves the GARDEN too late for the
  SCORING FLOWER (TS), so it parks after one LEFT volley.

### What would change it

The simulator was not tuned. These are the things the numbers point at, in the order the study
met them:

1. **An earlier T1.** L waits to 9.5 s for a TIP that comes at 6.7 s when it comes; every second
   off T1 is a second on the rescue. Belding's logs can say how late a real TIP 1 runs.
2. **Loads of four from both FLOWERs.** With every load full, two TIPs follow every TIP 1.
3. **A shorter way to the park after the RIGHT volley**, or a guard that fires the last volley
   and gives up the park. Either moves T3c later than 17.9 s; neither is this Auto's to decide.
4. **A GARDEN gather that takes four**, before the GARDEN choice is worth choosing.

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
- **The simulator** (test code): `"loop"`, `"picks"`, `"partnerPicks"`, `"oursFails"` and
  `"ourSpeed"` in a request, `"fails": "misses preloads"` for a partner, and each row's
  `tipsBy30`, HIVE contact and against-the-loop time for both robots (TeamCode/README.md,
  "Simulating an Auto").
- **The Visualizer:** a route choice, camera-or-timer stops, times on the menu, the partner note,
  and `scripts/save-auto.mjs`, which saved these files (Visualizer `docs/auto-format.md`, "A
  paired Auto").

The `.pp` files were assembled by a script from the route's points and saved with the
Visualizer's own layout and export. They have not been opened in the browser in this session.

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
`robotsCollidedAt`, `hitHiveAt`, `partnerHitHiveAt`, `againstLoopAt`, `partnerAgainstLoopAt`.
