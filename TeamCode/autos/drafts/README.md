# Drafts

Planning Autos have a `.pp` here and its generated class, with no competition OpMode.
Simulator drafts also have a request in `TeamCode/sim-requests/`. The isolated
partner-nudge experiment has Utility trials for bring-up. The nudge
Utility's runtime speed limits are not modeled by AutoSim. The Visualizer lists
these under "Drafts", and Save to GitHub saves back here.

## The Clockwise lane study: four plain drafts

`r-clockwise-flower.pp` with `l-clockwise-garden.pp`, and `r-clockwise-garden.pp` with
`l-clockwise-flower.pp`: the Clockwise pair's two pickup choices, each drawn with one pickup and
no INIT menu, so the lanes, the fallback paths and the clock times could be simulated before the
menu, the route choice and the camera-or-timer stops were built. Their numbers and what they
showed are in [clockwise.md](../clockwise.md), "The simulator's tables"; the pair in `TeamCode/autos/`
is the one to run. No OpMode. Requests: `sim-requests/r-clockwise-flower.json` and
`r-clockwise-garden.json`.

Open a pair:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-clockwise-flower.pp,drafts/l-clockwise-garden.pp>

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

## The lane pair: R-OutsideLane and L-UnderHive

A second way at three TIPs beside the Clockwise pair: R keeps to the wall side of the HIVE and L
to the under-HIVE side. Mentor session, 12 Nov 2026. Drafts, never driven; each runs from the
Utility menu and stops itself at 30 s.

Open the pair: <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-outside-lane.pp,drafts/l-under-hive.pp>

| Utility | Route |
|---|---|
| **Drive: R-OutsideLane** | Preloads at RIGHT, WALL FLOWER, wall lane up and across to L4, LEFT up before 14 s, volley. **If that volley is done before 15.5 s:** SCORING FLOWER, wall lane back to R5, RIGHT up before 26 s, volley, park right. **Otherwise** park right from the left spot |
| **Drive: L-UnderHive** | L4, LEFT up before 9 s, preloads, under the HIVE to the GARDEN, R4, RIGHT up before 22 s, volley, back under the HIVE, park left |

Every volley is from a spot the drivers' buttons go to (`LaunchSpots`), as a new Auto must be: R's
preloads from `RIGHT_START`, and the rest from the close row: `LEFT_CLOSE_HIVE` (L4, in front of the
LEFT CELL), `RIGHT_CLOSE_HIVE` (R4) and `RIGHT_CLOSE_NEAR` (R5). They are local points in these
drafts, not team spots. A wait that runs out parks the robot.

**R's LEFT volley is from the close spot in front of the LEFT CELL,** L4, the spot L fires its
preloads from (mentor, 13 Nov 2026: in front of the HIVE, not the corner and not back at the wall).
L is meant to have fired and gone under the HIVE while R picks the WALL FLOWER and drives up, so R
does not wait for it:

- **R drives there intake first** (mentor, 13 Nov 2026): the fourth ball of a load rides on the
  floor, and backing up lets it squirt out. Out of the WALL FLOWER R turns north in the first fifth
  of the drive, comes round to face the way it drives, and turns to the HIVE only over the last of
  the way in, as R-Infraviolet does. The trip back from the SCORING FLOWER already faced the way it
  drives.
- **What that costs is not known.** It is three quarters of a turn where backing in was one quarter.
  The simulator shares the wheels between driving and turning and says 1.4 s: R on the spot at
  13.2 s, not 11.8 s. But at Belding a half turn bunched at the start of a 92 in drive cost nothing
  (2.6 to 2.9 s driven, where that rule says 3.8), while R-Solo's turn spread along this same curve
  took 4.1 s against 2.9 drawn. So the first turn is bunched at the start, and the robot's own time
  for this leg decides ([robot-todo §3](../../../doc/robot-todo.md), "Drive times for the planning numbers").
- **L leaves the spot about 2.7 s after it reads LEFT up,** and R comes within reach of it at 12.0 s.
  In 19 of 20 simulated runs L had gone before that. In 1 the first TIP was read late (8.8 s), L was
  still leaving, and the robots touched.
- **Tried and dropped:** a wait for L until 12 s (no contact, but R's LEFT volley then starts at
  13.5 s and the third volley never fits); a wider turn in (slower by 0.3 s, and it met L on its
  way to an early park); L1, the wall-row spot behind this one (a longer drive, and a turn there
  crosses the centre line); L2, the corner.
- **R drives the last stretch into `RIGHT_PARK` straight** along the park's own line (parked robots
  are 2.7 in apart).

**R falls back by itself** (mentor, 12 Nov 2026: no separate short Auto). The third volley needs
the pick, the drive back, the volley and the park, about 14 s on a 2.5 s volley, so R goes for it
only if its LEFT volley is done before 15.5 s. With today's 4 s volleys that volley ends at about
15.8 s and R parks, done at 17.5 s. On a 2.5 s volley it ends at about 13 s and R goes on.

**In the simulator as the robot runs today** (Belding calibration, 4 s volleys, 20 seeds, the pair):

| R's LEFT volley from | Two TIPs | Both parked | Robots touch |
|---|---|---|---|
| L4, intake first, as drawn now | 2 | 20 | 3 |
| L4, backing in, tried | 2 | 20 | 4 |
| L1 with a wait for L, tried | 4 | 20 | 0 |
| L2, tried | 3 | 20 | 2 |
| `LEFT_LAUNCH_OFFSET`, before | 4 | 20 | 2 |

- **The three contacts as drawn now:** one at the left spot at 12.0 s (L still leaving, above), and
  two at about 22 s on R's third trip, where R5 and R4 overlap.
- **Fewer two-TIP runs from L4 than from the spots further out** is the calibration keeping 0.70 of
  balls from 36 in, on thin evidence (below).
- **The third volley depends on that leg's real time.** The check is drawn at 15.5 s (was 15). The
  Visualizer's preview has the leg at about 3 s and R done at 28.8 s on a 2.5 s volley; if the leg
  takes the simulator's 4.4 s, R's LEFT volley ends at about 15.7 s and it parks. The endgame guard
  parks R whenever the third volley and the park do not both fit.
- **From `LEFT_LAUNCH_OFFSET`, R parked after its LEFT volley in 18 of 20.** In the other two a short FLOWER load ended that
  volley early, R went on, and it met L leaving the GARDEN at about 20 s.
- **On a planned 2.5 s volley both robots fire their last volleys,** and R comes in to R5 while L
  is on R4: the two spots overlap, and the robots touched in most runs. Not resolved yet.
- **Two TIPs came in 9 of 20 with L's preloads from `LEFT_LAUNCH_CATCH`.** The drop to 4 was the
  calibration keeping 0.70 of balls from 36 in, a figure from before a launcher speed was measured
  there. Count the volleys from L4 before judging it.
- **No third TIP in any run:** the RIGHT CELL gets R's four and the two or three L's GARDEN gather
  takes.

The earlier lane study (closed PR #533) is why this is not offered as safe beside any partner: a
left partner driving to the park zone or taking the SCORING FLOWER meets R at its left spot, and
the full R meets a robot left on `LEFT_START`. The robot checks are in
[robot-todo §3](../../../doc/robot-todo.md).
