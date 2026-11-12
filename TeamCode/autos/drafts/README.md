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

## R-Three-Tip and R-Three-Tip-Stream: three TIPs from one robot, a study's drawings

Not Autos to run. They are the route behind [`doc/three-tips.md`](../../../doc/three-tips.md)
(mentor session, 11 Nov 2026): the partner's 4 preloads and the SCORING FLOWER into the LEFT CELL,
then the WALL FLOWER and the GARDEN into the RIGHT CELL. Both use `SweepRow`, a card only the
simulator has; R-Three-Tip-Stream also uses the simulator's `StreamOn` and `StreamOff`.

- R-Three-Tip, every volley a `LaunchAll` from a stop:
  <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-three-tip.pp,partners/partner-l-stage-leave-park.pp>
- R-Three-Tip-Stream, the same stops with the launcher firing between them, drawn at 70 in/s:
  <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-three-tip-stream.pp,partners/partner-l-stage-leave-park.pp>

The stand-in `partners/partner-l-stage-leave-park` starts on the far wall west of the SCORING
FLOWER with its 4 POLLEN in a row along its field side, and drives to the LOADING ZONE at PLAY.

| In the simulator set to Belding, 120 seeds, TIPs by 30 s | 2 TIPs | 3 TIPs |
|---|---|---|
| R-Three-Tip, the Belding robot | 57 | 0 |
| R-Three-Tip-Stream, every what-if on | 107 | 99 |
| R-Three-Tip-Stream, the smallest set that meets the target (turret, launching while moving, 0.98 staying in, feed 0.47 s a shot, 70 in/s) | 96 | 87 |

As saved, the stream draft's request runs with Belding's own accuracy and feed; the what-if knobs
are in the study's "How it was run".

## L-Pair-Flowers and R-Pair-Garden: a three-TIP pair on real cards, no turret

From the mentor's review of [`doc/three-tips.md`](../../../doc/three-tips.md) (11 Nov 2026); not run
on a robot. Only `LaunchAll`, `PickFlower`, `GardenGather` and CELL-up waits, every volley from a stop
at a team launch spot, so the Visualizer previews it and neither robot needs a turret.

- **L** drives from `LEFT_START` to `LEFT_LAUNCH_OFFSET`, fires its 4 preloads when the LEFT CELL comes
  up, picks the SCORING FLOWER and fires again, then picks the WALL FLOWER. With a full intake before
  23 s it drives south to `RIGHT_LAUNCH_OFFSET` and fires at the RIGHT CELL; otherwise it parks. Every
  park of L's goes up the wall lane to `RIGHT_PARK`, the near end.
- **R** makes the first TIP with its preloads, gathers the GARDEN, and waits loaded on
  `RIGHT_PAIR_CATCH` (61, 13), beside L's spot, for the RIGHT CELL to come back up (until 25 s). It
  leaves straight under the HIVE and parks at `LEFT_PARK`, the far end.
- **If TIP 1 does not come:** R, finding the RIGHT CELL still up when it is back from the GARDEN
  (before 16 s), fires the GARDEN's pieces at it and parks by another way (under the HIVE, sideways
  along y 100.5, into `LEFT_PARK` from the south, since L is still at the left end). L, if the LEFT
  CELL is not up by 12 s, drives the wall lane to `RIGHT_LAUNCH_OFFSET`, fires its preloads at the
  RIGHT CELL if it is still up, waits until 24 s so R has parked, and parks.
- This is the blend with R-Clockwise / L-Clockwise (biobuzz#535): this pair's division of the field,
  Clockwise's two spots side by side and its TIP 1 fallback. With either preload volley missed and
  either robot 20% slow or fast, both robots park and nothing touches in every simulated run.
- `RIGHT_PAIR_CATCH` is not a team spot yet (it is Clockwise's), and nothing here has run on a robot.
- On master's calibration (12 Nov, logged FLOWER loads): two TIPs in 52 of 120 at Belding's pace and
  63 with the shot counter and 0.98 stay-in; 108, and three TIPs in 88, only when every FLOWER load
  is full and the GARDEN gives 4. `doc/three-tips.md` has the table.

Open the pair:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/l-pair-flowers.pp,drafts/r-pair-garden.pp>.
In the Match view, set each robot's "Volley time" to 2.2 s (a feed that ends on the fourth ball): the
HIVE row goes from 2 TIPs to 3.

| In the simulator set to Belding, 120 seeds, TIPs by 30 s, both turrets locked | 2 TIPs | 3 TIPs |
|---|---|---|
| Feed ending on the fourth ball, 0.98 staying in, 50 in/s, the GARDEN giving 4 (`r-pair-garden-sweep`) | 97 | 85 |
| The same with the robot's own `GardenGather` as the simulator runs it (takes 2) | 97 | 29 |
| Belding's whole 3.5 s feed | 97 | 0 |
| Belding's accuracy | 58 | 28 |
| Infraviolet, feed ending on the fourth ball, 0.98 staying in | 108 | 0 |

`r-pair-garden-sweep` is R-Pair-Garden with the simulator's `SweepRow` in place of `GardenGather`: what
the pair makes when the GARDEN pickup gets all 4. The pair needs the shot counter, the accuracy, and
that pickup; nothing else.

## L-Three-Tip: a partner makes the first TIP and we do the rest

A what-if drawing on simulator-only cards (`StreamOn`, `SweepRow`), so the Visualizer shows its path
but not its scoring. A turret robot waits by the SCORING FLOWER, fires 8 as the LEFT CELL comes up,
then takes the WALL FLOWER and the GARDEN itself, beside
`partners/partner-r-tip-far-park` (fires its preloads at the RIGHT CELL from the audience start and
parks at the far end of the LOADING ZONE). The study uses it to say how early a partner's first TIP has
to come: the RIGHT CELL down by about 7 s, and only with a 0.3 s feed and 70 in/s (99 and 89 of 120).

<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/l-three-tip.pp,partners/partner-r-tip-far-park.pp>

## R-Three-Tip-Still: the solo route with every volley from a stop

A what-if drawing on simulator-only cards, so the Visualizer shows its path but not its scoring.
R-Three-Tip-Stream's stops with no shot fired while moving (`BIOBUZZ_AUTO_FIRE_STILL=1`): 96 and 84 of
120 with a turret, 0.98 staying in, a 0.3 s feed and 70 in/s.

<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-three-tip-still.pp,partners/partner-l-stage-leave-park.pp>

## R-Two-Tip-With: two TIPs beside a partner that fills the far CELL

R-Two-Tip with both LEFT volleys from `LEFT_LAUNCH_OFFSET`, which is clear of a robot on `LEFT_START`,
and its park at `LEFT_PARK`. It is for a partner that starts on `LEFT_START`, fires its 4 preloads at
the LEFT CELL once it is up, hides under the HIVE until 23 s and parks at `RIGHT_PARK`
(`partners/partner-l-fill-hide-park`, or `partner-l-timed-fill-hide-park`, which fires at 9 s).
Mentor session, 11 Nov 2026; not run on a robot. If our preloads do not TIP, it parks: it does not
drive under the HIVE, where the partner may be.

Open the pair:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-two-tip-with.pp,partners/partner-l-fill-hide-park.pp>

| In the simulator set to Belding, 120 seeds, the Belding robot | 2 TIPs | Second TIP at |
|---|---|---|
| R-Two-Tip-With alone | 61 | 28.0 s |
| beside `partner-l-fill-hide-park` (fires when it sees the CELL up) | 109 | 23.0 s |
| beside `partner-l-timed-fill-hide-park` (fires at 9 s) | 109 | 23.0 s |

The LEFT CELL gets 12 shots for the 8 it needs, which is the whole gain. No two robots touch in
any run. What to ask a partner is in [`doc/three-tips.md`](../../../doc/three-tips.md), rung 3.

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
