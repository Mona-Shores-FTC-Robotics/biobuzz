# Auto contingencies: where each robot goes when a TIP isn't seen (15 Nov 2026)

The mentor's ask after the 14 Nov field session: for R-Solo, R-Clockwise, L-Clockwise, R-OutsideLane and L-UnderHive,
what happens when a TIP is not seen, so that a robot always moves predictably and does not run into anything.

Sources:
- each Auto's generated Java on master `988db8b`, which is its decision tree;
- the Java simulator on that code: commit `2620c4a` of branch `claude/admiring-fermi-ewhefe`, results on `sim-results`
  under `<auto>/2620c4a…/`, Belding calibration, 5 to 24 seeds a case.

Counts of contacts and parks come from the simulator. Times are seconds on the robot's PLAY clock.

## What "not seen" means on the robot

`CellUp` / `LeftCellUp` / `RightCellUp` are true only when the camera has seen that CELL up (`AutoRegistration.cellUp`).
An assumed or half-seen CELL counts as down. So a TIP that happened but was not seen, a TIP that didn't happen, and a
camera that is down all look the same to the Auto: every check times out and the Auto takes its "otherwise" leg. INIT
warns when the camera isn't answering or sends no frames.

## The five Autos

### R-Solo (start RIGHT_START, volley at the RIGHT CELL)
| Check | If not seen | Sim |
|---|---|---|
| TIP / CellUp at LEFT_LAUNCH_CLOSE, 0.75 s (about 12.6 s) | Drives back down the field past the HIVE to RIGHT_LAUNCH_CLOSE (3.2 s). | Our preloads missed, partner out of the way: 5/5 park, no contact, done by 23.4 s. |
| RIGHT CELL still up?, 0.75 s | Parks to RIGHT_PARK from there, back up the wall side. | Same runs. |
| LEFT CELL still up? (after the SCORING FLOWER) | Parks. | |

The fallback is three long trips through the half of the field a partner uses. It is predictable, and it has room
only if the partner stays out of the way.

### R-Clockwise (paired with L-Clockwise)
| Check | If not seen | Sim |
|---|---|---|
| R2: LEFT up by 15.5 s, at LEFT_CLOSE_HIVE | Parks to RIGHT_PARK (it may wait about 4.5 s first). | Nothing tips (both preloads miss, L misses everything): 24/24 park, no contact, done by 17.9 s (GARDEN pick: 19.5 s). |
| Time for the SCORING FLOWER before 17.6 s; look at the HIVE before 22.2 s | Parks. | |
| R3: RIGHT up by 23.2 s (INIT option) | Fires at the LEFT CELL if it sees it up within 0.75 s (added 15 Nov; before, with no check), waits to 26.4 s, parks. | Normal WALL FLOWER case: R3 timed out in 2 of 24 seeds. |
| Priority = THREE_TIP and RIGHT up | Under the HIVE to RIGHT_CLOSE_HIVE, volley, then the park, which may not fit. | Normal WALL FLOWER case: **no park in 5 of 24** (all five took the third TIP at 23.1 s: 51 pts). |

### L-Clockwise (paired with R-Clockwise)
| Check | If not seen | Sim |
|---|---|---|
| L1: LEFT up by 9.5 s (INIT option), at LEFT_WALL_HIVE | Under the HIVE to RIGHT_START, volley at the RIGHT CELL, waits to 22.5 s, then drives the whole field to LEFT_PARK. | R's preloads missed: 24/24 park, no contact. |
| L2: RIGHT up by 22 s | Keeps its load, waits to 22.7 s, parks. | Nothing tips: 24/24 park, no contact. |

### R-OutsideLane (draft, paired with L-UnderHive)
| Check | If not seen | Sim |
|---|---|---|
| LEFT up before 14 s | Parks from LEFT_CLOSE_HIVE. | Nothing tips: 20/20 park, no contact, done by 16.5 s. |
| LEFT volley done before 15.5 s | Parks. | Took the park in 17 of 19 runs with a partner, so the SCORING FLOWER leg rarely runs. |
| RIGHT up before 26 s | Parks along the wall lane. | |

### L-UnderHive (draft, paired with R-OutsideLane)
| Check | If not seen | Sim |
|---|---|---|
| LEFT up before 9 s, at LEFT_CLOSE_HIVE | Parks at home. | Nothing tips, or R dead: 20/20 park, no contact, done by 10.9 s. |
| RIGHT up before 22 s | Parks under the HIVE to LEFT_PARK. | |

**When no TIP is seen, every one of the five parks with no contact.** That fallback is in good shape. The contacts are
in other cases, below.

## Where they do collide

| Case | Contacts | When | Why |
|---|---|---|---|
| Partner dead on (or still on) its start: R-Solo | 5/5 | 12.6 s | LEFT_LAUNCH_CLOSE is 16.3 in from LEFT_START, and the frame is 16.2 in square. |
| R-Solo with a partner that parks at 15 s | 5/5 | 12.6 s | The same spot, with the partner still on its start. |
| L dead on its start: R-Clockwise | 24/24 | 13.0 s | LEFT_CLOSE_HIVE is 17.6 in from LEFT_START. |
| R dead on its start: L-Clockwise | 24/24 | 13.7 s | It drives through R's start area. |
| Partner dead on its left start: R-OutsideLane | 20/20 | 13.0 s | LEFT_CLOSE_HIVE again. |
| R-OutsideLane / L-UnderHive as drawn | 3/20 | 12.0, 22.3 s | The pair has no Pickup choice: R always takes the WALL FLOWER, L the GARDEN (swapped or mismatched picks crossed paths and never added a TIP). |
| R- / L-Clockwise, R takes the WALL FLOWER | 2/24 | 12.1–12.3 s | A brush as both cross the left end. |
| L-UnderHive alone or with R dead | 0 | | Stays at home. |

All of these are cases where the partner is not where its own Auto would have it: dead, without an Auto, late, or
with the other pick. The pair halves time their moves by the clock, never by watching each other: "L has passed: park
at 26.4 s", "R has parked from the left spot: park at 22.5 s". Partners without an Auto are common at events.

## What the robot does on contact today

Nothing. A path card is never cut (`AutoKit.guarded`: "cutting a wait or a command but never a path"). A path ends
only when Pedro reaches its end, and `DriveSubsystem.pathDone` has no no-progress timeout. A robot blocked by a
partner or the HIVE keeps pushing until AUTO ends, and its park is lost. `ImpactWatch` only logs pushes. In the
simulator a contact is a counted event, and its robot still parks, so these tables understate what a real contact
costs.

## Changes to consider, most important first

1. **A blocked-path rule in the kit** (one change, every Auto). When a path card has been commanded to move but has
   made no progress for about 0.5 s (ImpactWatch's numbers are a start), end the path and let the guard park from where
   the robot is. The Visualizer and the simulator need the same rule. It needs a mentor decision because it breaks the
   kit's rule that a path is never cut.
2. **A "partner stays on its start" answer in INIT** for the R Autos. LEFT_LAUNCH_CLOSE and LEFT_CLOSE_HIVE sit about
   1 in from a robot left on LEFT_START. The options are another spot, or skipping that volley and parking, when the
   drive team says the partner has no Auto. Note the tension: the TeleOp's close spots moved 4 in back toward the wall
   on 14 Nov because close volleys hit the HIVE's lip, and moving the Autos' close spots the same way brings them closer
   to a partner on its start.
3. **Done (15 Nov): R-Clockwise's R3 "otherwise" leg checks the LEFT CELL before it fires** ("LEFT CELL still up?",
   0.75 s; not seen: it keeps the load and parks). Before, it fired with no check, against CLAUDE.md's rule.
4. **The lane pair's Pickup choices must match** (mentor, 15 Nov: the drive teams coordinate; no change). Swapped picks collide in 85–90% of runs. Either drop the swapped
   combination from INIT, or check the two robots' picks at the drive-team level (one card on the cart).
5. **R-OutsideLane's "LEFT volley done before 15.5 s"** takes the park in 17 of 19 runs with a partner, so the third
   leg is mostly dead weight. Loosen it or remove the leg.
6. **R-Clockwise Priority THREE_TIP gives up the park in about 1 run in 5.** That is the drawn trade (51 vs 48 pts),
   but the drive team should know it when choosing.

**Re-simulated with the R3 check (commit `b9743a3`):** every case unchanged from `2620c4a`. In the 16 of 24 runs
where R3 times out, LEFT is seen up and R fires as before. **Priority PARK** (simulated for the first time): R parks
in 24 of 24, but in all 5 runs where RIGHT came up and PARK changed what R did, R left LEFT_CLOSE_HIVE at 26.4 s and
touched L at 27.6–27.7 s. L was still parking (it finished 29.4–29.7 s), so "L has passed: park at 26.4 s" is too
early on those runs. The other 2 contacts are the 12.1–12.3 s brush at the left end that THREE_TIP has too.

Mentor, 15 Nov: 1 and 2 are left as they are; 3 is done; 4 is the drive teams' job; 5 is open; 6 stays (Priority
THREE_TIP). The simulator cases that produced them are now in each Auto's simulation request
(`TeamCode/sim-requests/`, the "contingency:" entries), so the next save of any of these Autos re-checks them.
