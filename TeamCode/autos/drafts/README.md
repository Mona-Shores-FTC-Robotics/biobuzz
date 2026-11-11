# Drafts

Planning Autos have a `.pp` here and its generated class, with no competition OpMode.
Simulator drafts also have a request in `TeamCode/sim-requests/`. The isolated
partner-nudge experiment has Utility trials for bring-up. The nudge
Utility's runtime speed limits are not modeled by AutoSim. The Visualizer lists
these under "Drafts", and Save to GitHub saves back here.

## R-Two-Tip: R-Solo's steps, fired from the measured LEFT spot

R-Solo made its second TIP in 0 of 7 runs at the Belding Qualifier. This draft keeps R-Solo's
steps and its 0.75 s CELL check, and changes where the LEFT volleys are fired from. Mentor
session, 10 Nov 2026; not yet run on a robot. It runs from the Utility **Drive: R-Two-Tip**
(a 30 s trial, as the Infraviolet pair had before it was promoted), not from the Autonomous menu.

Open it: <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-two-tip.pp>
(with our other robot: add `,l-infraviolet.pp`).

**Why R-Solo's second TIP never came.** Both of its LEFT volleys are fired from
`LEFT_LAUNCH_CLOSE`, 36 in from the CELL. `LaunchSpeedTable`'s measured rows run from 42.4 to
58.8 in, so at 36 in the launcher gets a speed nobody has fired: the first row's slope carried
5 in and then held.

| Belding Auto volleys at the LEFT CELL | Spot (range) | Launch speed in the log | Second TIP |
|---|---|---|---|
| R-Solo, 7 runs | `LEFT_LAUNCH_CLOSE` (36 in) | 1459–1480 ticks/s, extrapolated | 0 of 7 |
| L-NoCross, 5 runs | `LEFT_LAUNCH_CATCH` (42 in) | 1521–1528 ticks/s, the measured row | 3 of 5 |

On the Q-13 stream (19429, R-Solo's third volley, Auto 23.4–25.2 s), balls drop back to the floor
in front of the HIVE at 24.1–24.3, 24.5–24.6 and 25.1–25.2 s. Twelve runs is a small sample, and
the two Autos differ in other ways, so this is the likely cause, not a proven one.

**What changed from R-Solo:**

- Both LEFT volleys are at `LEFT_LAUNCH_CATCH`, the spot L-NoCross made its second TIPs from.
- The drive from the WALL FLOWER turns the short way (180° to 273°, done by 60% of the drive)
  and faces the HIVE for the rest. R-Solo's turns 270° and took 3.5–4.6 s against 2.9 s drawn.
- The timed-out route is R-Solo's (back to `RIGHT_LAUNCH_CLOSE`, launch, park): it made the TIP
  both times it ran at Belding. Its turn starts a little later so no corner reaches the centre line.
- Only team spots. The preview's CELL-up route is 28.9 s, as R-Solo's; its worst case is 30.2 s
  (R-Solo: 31.7 s).

**What it cannot do alone: spare shots.** A robot holds 4 pieces and fits three volleys in 30 s,
so it launches 12. The first TIP takes the 4 preloads. The LEFT CELL starts empty and needs about
8 POLLEN (`doc/robot-todo.md`, PR #519), which is every piece of the other two volleys. No
drawing gives a lone robot a ninth piece at the LEFT CELL: the SCORING FLOWER is the only source
near it, and a third trip does not fit. Catching the first TIP's spill (3 NECTAR, 4.95 POLLEN
weights) was tried in the simulator and took about one piece a run.

**Where the spare shots come from: our other robot's four preloads.** L-Infraviolet, a competition
Auto that has run on both robots, drives to `LEFT_LAUNCH_CATCH`, launches its preloads once the
LEFT CELL is up (before 8 s) and crosses south to the GARDEN, so it has left the spot before this
Auto arrives there. The LEFT CELL then gets 12 pieces (its 4, our WALL FLOWER's 4, our SCORING
FLOWER's 4) where 8 are needed, and the second TIP comes off the first two of those volleys.
A partner team that starts on the left, waits for the LEFT CELL and launches its preloads
(`partners/partner-l-launch-park`) gives the same result. Simulated with `AutoStudyTest`, 20 seeds
each, turret locked forward:

| Shots that score | R-Solo alone | R-Two-Tip alone | L-Infraviolet + R-Infraviolet | L-Infraviolet + R-Two-Tip |
|---|---|---|---|---|
| 92 % (spread 1) | second TIP 20 of 20 | 19 of 20 | 20 of 20, 51 points | 20 of 20, 56 points, at 16.7 s |
| 79 % (spread 2) | 8 of 20, 36 points | 7 of 20, 35 points | 8 of 20, 36 points | **16 of 20, 52 points**, at 19.8 s |
| 58 % (spread 3) | 2 of 20, 30 points | | 1 of 20, 23 points | 6 of 20, 42 points |

- Our robot driving 20 % slower (40 in/s): the same 20 of 20 and 16 of 20.
- With the stand-in left partner in place of L-Infraviolet: 20 of 20, 16 of 20 and 6 of 20, and
  16 of 20 at Belding Field 2's tip point.
- The 4 of 20 it loses at spread 2 are the runs where our preloads did not make the first TIP:
  the timed-out route then makes one TIP, as R-Solo's did in Q-7 and Q-31.
- L-Infraviolet's own third-TIP try (GARDEN, then the RIGHT CELL) still runs; it has only its own
  4 pieces there, so it is a bonus, not part of the plan.
- The simulator does not know that the 36 in speed is unmeasured, so alone the two Autos look the
  same there; the Belding table above is the evidence for the spot.
- The request in `TeamCode/sim-requests/r-two-tip.json` uses the stand-in: a request may name
  only stand-ins or its own pair's other half as partners.

**The park bends south** (control point (30, 86)) so the robot comes into `RIGHT_PARK` from the
field side, clear of L-Infraviolet already on `LEFT_PARK`. With the straight park the two touched
at about 25 s in every run; with the bend, in 3 of 80.

**Not settled:**

- `LEFT_LAUNCH_CATCH` overlaps a robot sitting on `LEFT_START` (the simulator: contact at 11.7 s
  with a dead partner there). A partner with no Auto has to start somewhere else on the wall, or
  the 36 in row gets measured and R-Solo's `LEFT_LAUNCH_CLOSE` is used instead.
- With L-Infraviolet the two robots touched in 3 of 80 simulated runs, at 23.6–25.6 s, as we
  park beside it. Dry-run both parks before a full run.
- The stand-in left partner is clear when the first TIP comes (16 of 16 runs). When it does not
  come, that partner is still on its start, under our launch spot.
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
