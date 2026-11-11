# Drafts

Planning Autos have a `.pp` here and its generated class, with no competition OpMode.
Simulator drafts also have a request in `TeamCode/sim-requests/`. The isolated
partner-nudge experiment has Utility trials for bring-up. The nudge
Utility's runtime speed limits are not modeled by AutoSim. The Visualizer lists
these under "Drafts", and Save to GitHub saves back here.

## R-Two-Tip: one robot, two TIPs

R-Solo made its second TIP in 0 of 7 runs at the Belding Qualifier. This draft keeps R-Solo's steps and
its 0.75 s CELL check, fires the LEFT volleys from measured rows of the speed table, and shortens the
last leg so master's guard lets the third volley go. Mentor session, 10 Nov 2026; not yet run on a
robot. It runs from the Utility **Drive: R-Two-Tip** (a 30 s trial), not from the Autonomous menu.

Open it: <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-two-tip.pp>

**Why R-Solo's second TIP never came: it never fired eight pieces at the LEFT CELL.** The launcher's
wheels dip once for each ball, so the match logs give each volley's count
(`read_logs.py volleys`; within one ball of the video on the nine volleys also counted on video, never
under). The LEFT CELL starts empty and needs 8.

| Belding, pieces fired at the LEFT CELL | Runs | Second TIP |
|---|---|---|
| R-Solo, both LEFT volleys together: 0, 3, 4, 6, 6, 6 to 7, 7 | 7 | 0 of 7 |
| L-NoCross, 8 fired (preloads, then the SCORING FLOWER load) | 4 | 3 of 4 |
| L-NoCross, second volley fired nothing | 1 | 0 of 1 |

- **The short loads were mostly Infrared's.** Its WALL FLOWER volleys fired 0, 2, 4, 2 and 1 pieces;
  Ultraviolet's fired 4 every time (six volleys). After the SCORING FLOWER, Infrared fired 0, 4, 3, 4,
  2, 0, 4, 4 and Ultraviolet 1 (Q-1's wedge), 4, 4.
- **Ultraviolet got one clean try at the whole route** (Q-1, where the SCORING pick wedged). Q-27 was
  held up by an opponent, and Q-7 and Q-31 took the timed-out route.
- A piece that reaches a raised CELL stays in (mentor, 10 Nov 2026). Every counted Auto shot was aimed
  within 0.8° by the pose, fired standing still, with the wheels at or above target, so the logs do not
  show which shots went wide or short.
- R-Solo's LEFT volleys were also fired from `LEFT_LAUNCH_CLOSE` (36 in), below every measured row of
  the speed table, at an extrapolated 1459 to 1480 ticks/s. Belding cannot say how much that cost,
  because eight pieces never went up from there.

**What changed from R-Solo:**

- The first LEFT volley is at `LEFT_LAUNCH_CATCH` (the 42.4 in row, where L-NoCross made its second
  TIPs). The second is at `LEFT_LAUNCH_OFFSET`, right beside the SCORING FLOWER (the 45.7 in row): the
  hop from the pick is 1.1 s instead of 1.3 s and the park from there 1.8 s instead of 2.1 s, which is
  the time the guard needs.
- The drive from the WALL FLOWER turns the short way (180° to 273°, done by 60% of the drive) and faces
  the HIVE for the rest. R-Solo's turns 270° and took 3.5–4.6 s against 2.9 s drawn.
- The timed-out route is R-Solo's (back to `RIGHT_LAUNCH_CLOSE`, launch, park): it made the TIP both
  times it ran at Belding. Its turn starts a little later so no corner reaches the centre line.
- Only team spots. The preview's CELL-up route is 28.9 s with 1.1 s spare and no guard park (R-Solo's:
  28.9 s with the guard parking); its worst case is 29.7 s (R-Solo: 31.7 s).

**In the simulator set to Belding** (`BeldingCalibration`, 120 seeds each; TeamCode/README.md,
"Simulating an Auto"), one robot, master's endgame guard:

| R-Two-Tip, with the loads of | Two TIPs of 120 | Second TIP at |
|---|---|---|
| Infrared as at Belding | 9 | 28.8 s |
| Ultraviolet as at Belding | 56 | 28.5 s |
| Ultraviolet, driving 10% / 20% slower | 56 / 58 | 29.1 / 29.9 s |
| a robot whose every load is four | 86 | 28.6 s |
| the same, driving 28% slower | 0 | |

- R-Solo with every load four: 0 of 120. Master's guard parks it after its second volley, which ends
  half a second too late for the park to fit after a third.
- Other one-robot drawings tried: both LEFT volleys at `LEFT_LAUNCH_CATCH` (as many TIPs, but none
  at 10% slower); the preloads from `RIGHT_LAUNCH_CATCH` (no more TIPs, and its park from that spot
  crosses the Red HIVE); the first LEFT volley from `LEFT_LAUNCH_OFFSET` (no time gained). Catching
  the first TIP's spill (3 NECTAR) took about one piece a run.

**What the robot would need** to make two TIPs in 3 runs of 4 is in
[`doc/one-robot-two-tips.md`](../../../doc/one-robot-two-tips.md). In short: every load after a pick
reaching the launcher gets it to 86 of 120 by itself; with 0.99 of the LEFT shots staying in, 106.

**Not settled:**

- `LEFT_LAUNCH_CATCH` overlaps a robot sitting on `LEFT_START`. A partner with no Auto has to start
  somewhere else on the wall.
- `LEFT_LAUNCH_OFFSET` has not been fired from on the left.
- The robot checks are in [robot-todo §3](../../../doc/robot-todo.md).

## R-Solo-Nudge, for a partner with no Auto

The angled draft targets both partner AUTO PARK and LEAVE: a **16 x 16 in** chassis
starts with one corner touching the alliance wall, heading **70 degrees** (20 degrees
toward the field interior from straight upfield). It replaces the square, park-only
placement and the earlier long diagonal shove. The partner stays motionless for AUTO.

Red-frame setup, inches and degrees:

| Point | x | y | Heading |
|---|---:|---:|---:|
| Partner centre | 10.2537 | 82.0000 | 70 |
| Our PARTNER_CONTACT | 11.7538 | 64.1930 | 250 |
| Our PARTNER_PUSH_END | 13.8060 | 69.8312 | 250 |
| Test-only PARTNER_RELEASE | 17.8060 | 63.8312 | 250 |

The drawn 15.44 x 16 in pusher overlaps 8.5 in of the partner's rear face, with
0.5 in initial gap. Its six-inch stroke nominally translates the partner 5.5 in:
**1.88 in off the wall and 5.17 in toward the loading zone**. This calculation assumes
translation without rotation. Offset contact can turn a real chassis: neither
LEAVE nor PARK is proven until the placement and push work on the floor. Use the
actual solid bumper faces and measured chassis envelope, not just a robot's advertised size.

The full Solo drawing curves to contact after the WALL FLOWER pickup, waits 300 ms,
pushes, then follows one continuous curve that first peels east/south and continues
to LEFT_LAUNCH_CLOSE. It retains the current Solo scoring sequence and our far
LEFT_PARK. The pushed partner occupies the near part of the loading zone.

### First floor test: Drive: Partner Nudge

`drafts/partner-nudge-test.pp` draws **only the push and retreat**. Its generated
class is run by the Utility **Drive: Partner Nudge**, never the competition Auto menu.
The wrapper caps both paths with Pedro's `maxPathSpeed`, a fraction of the
robot's top path speed: `PartnerNudgeTest.pathSpeed` in Panels, default **0.2**
(about 14 in/s on 20245, not yet measured), read once at PLAY. Changing the
drawing's preview speed does not change it. Allowed ranges are 0.1–0.5 of top
speed and a 1–15 s timeout; invalid values refuse motion.

It is not Pedro's `maxVelocityConstraint` / `maxAccelerationConstraint`. On 6 Nov
the old 4 in/s, 8 in/s² cap never moved the robot. From rest, Pedro's acceleration
cap targets one loop's worth of speed (about 0.16 in/s), and our tuned coast
feedforward has no static term. So the wheels got about 0.2 % power, and the robot
never started. That cap also stayed applied after a run stopped mid-path, and it
slowed every later path until the app restarted. `DriveSubsystem` now clears
Pedro's Follower whenever path control ends (`PathCapReleaseTest`).

It starts at PARTNER_CONTACT, pushes six inches, then retreats four inches east and
six south at the same heading. Before advancing, the test requires Pedro done,
position within 0.3 in, heading within 3 degrees and measured speed at most 1 in/s.
It releases the drive at completion, Driver Station STOP, loss of pose reference,
or the default **8 s overall timeout**. These are commanded trajectory limits, not
a contact-force limit; a blocked robot can still press before the timeout.

The full R-Solo-Nudge now also has a Utility trial with scoped contact limits,
described below. Two TIPs plus both parks remain a goal. No 17/18 in presets
are added before proving this first size.

Setup and the repeatable test checklist live in
[robot-todo §3](../../../doc/robot-todo.md).

Open the isolated test and partner together:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/partner-nudge-test.pp,partners/partner-push-start.pp>.

Open the full planning draft and partner together:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-solo-nudge.pp,partners/partner-push-start.pp>.

The full draft's simulation still uses a placeholder push model and is not evidence
of the real chassis's friction, rotation or the Utility's speed limits.

### Full lab trial: Drive: R-Solo-Nudge

Both Utilities are available: **Drive: Partner Nudge** tests just the push/retreat;
**Drive: R-Solo-Nudge** runs preloads, both flower pickups, the angled push, the
remaining volleys and far park. For the full run, start our robot at RIGHT_START
with four preloads, not at the push-only contact mark. Use the same 16x16 partner
placement from the setup drawing and select the correct alliance.

The full trial splits the existing approach curve without changing its geometry,
brakes for 300 ms before the final approach, and caps only that short segment
and the push, at `PartnerNudgeTest.pathSpeed`. Contact endpoints require position/heading/speed
agreement. A contact segment blocked for six seconds stops the trial; the whole
trial stops at 30 seconds. Other scoring paths retain their normal constraints.
The generic simulator does not include these runtime speed limits; use this run's
log to establish whether two tips and park fit. This is a Utility, not a competition
Autonomous entry. The full trial reads `PartnerNudgeTest.pathSpeed` at INIT, the one
number for the contact speed: find it with the push-only test first.

## Lane study: R-OutsideLane and L-UnderHive

A study, not a candidate: would a pair in which each robot owns one lane be worth building?
R drives only the wall side of the HIVE, L only the under-HIVE side. Mentor session, 11 Nov 2026.
Three drafts, no OpModes, never run on a robot.

Open the pair: <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-outside-lane-short.pp,drafts/l-under-hive.pp>
(the full R: `drafts/r-outside-lane.pp`).

| Draft | Route |
|---|---|
| R-OutsideLane-Short | Preloads at RIGHT, WALL FLOWER, wall lane to `LEFT_LAUNCH_OFFSET`, LEFT up before 14 s, volley, park |
| R-OutsideLane | The same, then the SCORING FLOWER, wall lane back to `RIGHT_LAUNCH_CORNER`, RIGHT up before 26 s, volley, park |
| L-UnderHive | `LEFT_LAUNCH_CATCH`, LEFT up before 9 s, preloads, under the HIVE to the GARDEN, `RIGHT_LAUNCH_CLOSE`, RIGHT up before 22 s, volley, back under the HIVE, park |

A wait that runs out parks the robot. Only team spots.

**The answer: do not build it as a lane-ownership pair.** The lanes exist only beside the HIVE.
Every pickup, both parks and all the launch spots are in the two end zones, which both robots must
use, so neither Auto can promise a partner "stay out of our lane and we never meet".

Simulator, Belding calibration, turret locked forward, 20 seeds each:

| Pair | Three TIPs | Two or more | Both parked | Finished (median, latest) | Contact |
|---|---|---|---|---|---|
| R-OutsideLane-Short + L-UnderHive | 0 | 9 | 20 | R 17.5, 17.5 s; L 25.9, 29.0 s | 1 |
| The same, L 20% slower | 0 | 9 | 20 | L 26.6, 29.6 s | 1 |
| The same, R 20% slower | 0 | 9 | 20 | R 18.5 s | 0 |
| The same, L misses every shot | 0 | 0 | 20 | | 1 |
| The same, R misses every shot | 0 | 0 | 20 | L parks at 10.8 s holding its preloads | 0 |
| R-OutsideLane (full) + L-UnderHive | 0 | 9 | 20 | R 28.3, 28.7 s | 3 |
| R-Infraviolet + L-Infraviolet, same settings | 0 | 3 | 3 (R parks in 3) | R 28.5 s; L 24.0 s | 0 |

- **A two-TIP pair at best, never three.** The third TIP needs R's third load, and R reaches the
  right end at 25.0 s: master's guard parks it without firing in every run. The full R is the short
  one with more risk and no more TIPs.
- **It beats Infraviolet under this calibration** (9 of 20 against 3 of 20, and both robots always
  park), for two reasons that are not about lanes: R's LEFT volley is from `LEFT_LAUNCH_OFFSET`,
  where the calibration keeps about 0.94 of the pieces against 0.70 at `LEFT_LAUNCH_CLOSE`, and L
  waits to 9 s for the first TIP, which came at 6.5 to 8.8 s. Infraviolet's 8 s limit would have
  lost three of these runs.
- **L's GARDEN trip adds no TIP.** Its four pieces are half of a third TIP that R never completes.
- **R-OutsideLane-Short is done at 17.5 s**, with 12.5 s unused.

Each Auto alone, beside a stand-in (20 seeds; contact is the count of runs):

| R-OutsideLane-Short beside | Contact | Where |
|---|---|---|
| A partner dead on `LEFT_START`, one still by the wall, one that stages and parks | 0 | |
| A partner that launches and parks | 10 | R's last 20 in to `LEFT_LAUNCH_OFFSET`, as the partner drives to the park zone |
| A partner that parks at 15 s | 17 | R on `LEFT_LAUNCH_OFFSET`, in the partner's line from `LEFT_START` to the park zone |
| A partner that takes the SCORING FLOWER | 20 | `LEFT_LAUNCH_OFFSET` is beside that FLOWER |

| L-UnderHive beside | Contact | Where |
|---|---|---|
| A partner dead on `RIGHT_START` | 0 | No first TIP, so L parks at 10.8 s and never goes south |
| A partner that launches and parks | 0 | |
| A partner that launches and stays on `RIGHT_START` | 15 | L turning in to `RIGHT_LAUNCH_CLOSE`, 18.3 in from that start |

The full R also touches a partner dead on `LEFT_START` at the SCORING FLOWER in 19 of 20 runs, as
R-Infraviolet does in 20 of 20.

**Where the pair itself touches.** Once in 20: the first TIP came late, L was still firing from
`LEFT_LAUNCH_CATCH` when R reached `LEFT_LAUNCH_OFFSET`, and the two spots are 16.6 in apart. With
the full R, twice more at about 20 s, R driving south past L leaving the GARDEN. No run touched the
HIVE or crossed the centre line.

**Each robot at its stops** (pair with the short R, median and range over 20 seeds):

| R | s | L | s |
|---|---|---|---|
| Preloads away | 3.7 | At `LEFT_LAUNCH_CATCH` | 0.6 |
| At the WALL FLOWER | 5.6 | LEFT up, firing | 7.0 (6.5 to 9.0) |
| Leaves it | 8.8 | Leaves for the GARDEN | 9.4 (9.1 to 11.5) |
| At `LEFT_LAUNCH_OFFSET` | 11.9 | At the GARDEN | 13.0 |
| Volley over, leaves to park | 15.8 | Leaves it | 18.0 |
| Parked | 17.5 | At `RIGHT_LAUNCH_CLOSE` | 20.5 (20.3 to 22.6) |
| | | Second TIP, when it came | 20.4 (19.1 to 20.9) |
| | | Leaves to park | 22.0 to 25.1 |
| | | Parked | 25.9 (latest 29.0) |

**What it suggests instead** (not drawn, not simulated): the short R has 12.5 s spare and stands
beside the SCORING FLOWER. Picking it and firing a second LEFT volley when the second TIP has not
come is R-Two-Tip's ending, and it is the "R finishes TIP 2" fallback planned for the Clockwise pair.

**Limits of these numbers.** The simulator reports contact or none, not a distance, so there is no
closest-approach figure. "Misses every shot" is the simulator's one failure mode: there is no
"misses only its preloads". The stand-ins are the eleven in `partners/`. The `.pp` files were written
from the Infraviolet pair's field, robot and spots and exported by the Visualizer's own export script,
with no load problems or warnings; nobody has opened them in the app and nudged a path. The gaps a
robot must settle are in [robot-todo §3](../../../doc/robot-todo.md).
