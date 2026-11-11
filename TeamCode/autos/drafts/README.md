# Drafts

Planning Autos have a `.pp` here and its generated class, with no competition OpMode.
Simulator drafts also have a request in `TeamCode/sim-requests/`. The isolated
partner-nudge experiment has Utility trials for bring-up. The nudge
Utility's runtime speed limits are not modeled by AutoSim. The Visualizer lists
these under "Drafts", and Save to GitHub saves back here.

## R-Two-Tip: one robot, two TIPs, from the measured LEFT spot

R-Solo made its second TIP in 0 of 7 runs at the Belding Qualifier. This draft keeps R-Solo's steps and
its 0.75 s CELL check, and changes where the LEFT volleys are fired from. Mentor session, 10 Nov 2026;
not yet run on a robot. It runs from the Utility **Drive: R-Two-Tip** (a 30 s trial), not from the
Autonomous menu.

Open it: <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-two-tip.pp>

**Why R-Solo's second TIP never came.** Both of its LEFT volleys are fired from `LEFT_LAUNCH_CLOSE`,
36 in from the CELL. `LaunchSpeedTable`'s measured rows run from 42.4 to 58.8 in, so at 36 in the
launcher gets a speed nobody has fired: the first row's slope carried 5 in and then held.

| Belding Auto volleys at the LEFT CELL | Spot (range) | Launch speed in the log | Second TIP |
|---|---|---|---|
| R-Solo, 7 runs | `LEFT_LAUNCH_CLOSE` (36 in) | 1459–1480 ticks/s, extrapolated | 0 of 7 |
| L-NoCross, 5 runs | `LEFT_LAUNCH_CATCH` (42 in) | 1521–1528 ticks/s, the measured row | 3 of 5 |

On the Q-13 stream (19429, R-Solo's third volley, Auto 23.4–25.2 s), balls drop back to the floor in
front of the HIVE at 24.1–24.3, 24.5–24.6 and 25.1–25.2 s. Twelve runs is a small sample, and the two
Autos differ in other ways, so this is the likely cause, not a proven one.

**What changed from R-Solo:**

- The first LEFT volley is at `LEFT_LAUNCH_CATCH`, the spot L-NoCross made its second TIPs from
  (the 42.4 in row). The second is at `LEFT_LAUNCH_OFFSET`, right beside the SCORING FLOWER (the
  45.7 in row): the hop from the pick is 1.1 s instead of 1.3 s and the park from there 1.8 s
  instead of 2.1 s, which is the time the guard needs.
- The drive from the WALL FLOWER turns the short way (180° to 273°, done by 60% of the drive) and faces
  the HIVE for the rest. R-Solo's turns 270° and took 3.5–4.6 s against 2.9 s drawn.
- The timed-out route is R-Solo's (back to `RIGHT_LAUNCH_CLOSE`, launch, park): it made the TIP both
  times it ran at Belding. Its turn starts a little later so no corner reaches the centre line.
- The park from `LEFT_LAUNCH_OFFSET` bends a little south (control point (26, 92)).
- Only team spots. The preview's CELL-up route is 28.9 s with 1.1 s spare and no guard park
  (R-Solo's: 28.9 s with the guard parking); its worst case is 29.7 s (R-Solo: 31.7 s).

**In the simulator set to Belding** (`BeldingCalibration`, 60 seeds each; TeamCode/README.md,
"Simulating an Auto"), with master's endgame guard, one robot:

| | Two TIPs | Second TIP at |
|---|---|---|
| R-Solo | 0 of 60 | |
| **R-Two-Tip** | **31 of 60** | 28.6 s |
| R-Two-Tip driving 10% slower | 30 of 60 | 29.1 s |
| R-Two-Tip driving 20% slower | 31 of 60 | 29.8 s |
| R-Two-Tip driving 28% slower | 0 of 60 | |

- **It makes two TIPs in about half the runs (60 of 120 over more seeds), and that is the pieces.** A robot holds 4 and
  fits three volleys in 30 s. The first TIP takes the preloads; the LEFT CELL starts empty and needs 8
  POLLEN (7 on a field that tips light). So every piece of the two LEFT volleys has to stay in. Of
  the runs lost, about 1 in 9 missed the first TIP, about 1 in 9 had the SCORING FLOWER pick come back
  with nothing (as 2 of 14 did at Belding), and the rest had a LEFT piece miss.
- **The time holds to 20% slower.** Master's guard goes for the third volley only if the park still
  fits after it. With both LEFT volleys at `LEFT_LAUNCH_CATCH` the second volley ended half a second
  inside that limit and driving 10% slower lost every second TIP; with the last volley at
  `LEFT_LAUNCH_OFFSET` it does not. The second TIP still lands late, 27 to 30 s.
- Other one-robot drawings tried in it: both LEFT volleys at `LEFT_LAUNCH_CATCH` (as many TIPs, but none
  at 10% slower); the preloads from `RIGHT_LAUNCH_CATCH` (no more TIPs, and its park from that
  spot crosses the Red HIVE); the first LEFT volley from `LEFT_LAUNCH_OFFSET` (no time gained).
  Catching the first TIP's spill (3 NECTAR) took about one piece a run.

**What the robot would need** to make two TIPs in 3 runs of 4 is in
[`doc/one-robot-two-tips.md`](../../../doc/one-robot-two-tips.md), with the SCORING FLOWER picks that
came back short at Belding counted in (as drawn: 60 of 120). In short: 0.98 of the shots from the LEFT
spots staying in, and a pick that always comes back full, give 94 of 120; the shot counter keeps the
Auto working when the robot runs slow; drive time and a turret change little.

**Not settled:**

- `LEFT_LAUNCH_CATCH` overlaps a robot sitting on `LEFT_START`. A partner with no Auto has to start
  somewhere else on the wall, or the 36 in row gets measured and R-Solo's `LEFT_LAUNCH_CLOSE` is used.
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

## R-Two-Tip-With: two TIPs beside a partner that fills the far CELL

R-Two-Tip with its drive up the wall lane pulled in (control point (30, 110)), for a partner that
starts on the far wall at (30, 133.8), fires its 4 preloads at the LEFT CELL once it is up, and
parks along the far wall. Mentor session, 11 Nov 2026; not run on a robot.

Open the pair:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-two-tip-with.pp,partners/partner-l-fill-park.pp>

| In the simulator set to Belding, 120 seeds, the Belding robot | 2 TIPs | Second TIP at |
|---|---|---|
| R-Two-Tip-With alone | 57 | 28.4 s |
| beside `partner-l-fill-park` (fires when it sees the CELL up) | 109 | 23.0 s |
| beside `partner-l-timed-fill-park` (fires at 9 s) | 109 | 23.0 s |

The LEFT CELL gets 12 shots for the 8 it needs, which is the whole gain. No two robots touch in
any run. What to ask a partner, and which Belding partners could have done it, is in
[`doc/three-tips.md`](../../../doc/three-tips.md), rung 3.

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
