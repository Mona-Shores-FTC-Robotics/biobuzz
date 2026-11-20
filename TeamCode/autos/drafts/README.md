# Drafts

Planning Autos have a `.pp` here and its generated class, with no competition OpMode. The Visualizer lists
them under "Drafts", and Save to GitHub saves back here. Since 19 Nov 2026 this folder is kept small
(mentor): it holds L-Assist, which runs from the Utility menu as **Drive: L-Assist**, and the RightUnderHive
pair, parked until a super FLOWER pick exists (no OpMode), the ThreeTip pair, which runs from the Utility menu as a trial pair, **Drive: R-ThreeTip** beside **Drive: L-ThreeTip**,
and R-Solo-19429 and R-Clockwise-19429, each with one loaded leg driven backward, which run from the Utility menu as
**Drive: R-Solo-19429** and **Drive: R-Clockwise-19429** on a robot with a bottom plate (brought back 20 Nov 2026).

**Removed on 19 Nov 2026**, with their generated classes, sim requests, the nudge test's code
(`NudgeContactDrive`, `NudgeTestRun`) and the stand-ins only they used. All are in Git history at
`f2c9bb7`; `git checkout f2c9bb7 -- TeamCode/autos/drafts/<name>.pp` and its class in
`opmodes/auto/generated/` bring one back:

- Drafts: `l-clockwise-backward`, `l-clockwise-close`, `l-clockwise-flower`, `l-clockwise-garden`, `l-clockwise-next`, `l-pair-flowers`, `l-three-tip` (likewise reused), `l-under-hive-fix`, `l-under-hive-park-shot` (became l-under-hive.pp), `partner-nudge-test`, `r-clockwise-19429` (rebuilt on 20 Nov 2026, below), `r-clockwise-backward`, `r-clockwise-close`, `r-clockwise-flower`, `r-clockwise-garden`, `r-clockwise-next`, `r-outside-lane-fix`, `r-pair-garden`, `r-pair-garden-sweep`, `r-solo-19429` (brought back on 20 Nov 2026, below), `r-solo-human-player`, `r-solo-nudge`, `r-three-tip` (the name now holds the new ThreeTip draft below), `r-three-tip-still`, `r-three-tip-stream`, `r-two-tip`, `r-two-tip-with`.
- Stand-ins in `partners/`: `partner-l-fill-hide-park`, `partner-l-stage-leave-park`, `partner-l-timed-fill-hide-park`, `partner-r-launch-stay`, `partner-r-tip-far-park`.

## R-Solo-19429: R-Solo with its WALL FLOWER leg driven backward

`r-solo-19429.pp` is master's `r-solo.pp` with one leg's heading changed (18 Nov 2026), so the robot no longer turns
round just to keep the load in: the WALL FLOWER leg to `LEFT_AUTO_HIVE` turns 180 to 268.1 in its first 35%, then drives
backward. Driving backward with a load needs a bottom plate under the intake: on 20245's first Clockwise drive (12 Nov),
with no plate, pieces left the intake on both backward legs. 19429 has a plate (18 Nov); 20245 is getting one.

It runs from the Utility menu as a 30 s trial, **Drive: R-Solo-19429** (`RSolo19429Trial`, on `BackwardLoadTrial`).
On a robot without `IntakeSubsystem.holdsLoadDrivingBackward` (on for 19429 in `RobotDifferences`; 20245 gets its line
once its plate is on) it shows a red line in INIT and does not move. The backward drive in
[robot-todo](../../../doc/robot-todo.md) (tonight's step 9) has to pass first on that robot. Request:
`sim-requests/r-solo-19429.json`. It came off the Utility menu on 19 Nov 2026 with R-Clockwise-19429 (mentor: 19429 runs
plain R-Solo) and came back on 20 Nov 2026 (mentor: 20245 is getting a plate too).

**In the simulator** (20 Nov 2026, `sim-requests/r-solo-19429.json` against `r-solo.json`, both on the Belding
calibration, 5 runs a partner case, TIPs settled by 38 s). The simulator never drops a load driving backward, so this is
the time saved and nothing about the plate:

| Case | R-Solo | R-Solo-19429 |
|---|---|---|
| Best case (`"scenario": "best"`), partner out of the way | 2.00 TIPs, TIP 2 at 28.4 s | 2.00 TIPs, TIP 2 at 28.0 s |
| Belding loads, partner out of the way | 1.20 (TIP 2 in 1 of 5) | 1.00 |
| Our preloads miss, best case (the fix TIP) | 1.00, at 23.3 s | 1.00, at 22.5 s |

Every robot parks in both. The contacts in the partner-dead-on-`LEFT_START` and partner-parks-at-15 s cases are the same in
both: the known `LEFT_AUTO_HIVE` overlap with a robot on `LEFT_START`. On 18 Nov, on 19429's own loads
(`BIOBUZZ_BELDING_ROBOT=19429`), the leg took 3.2 s instead of 4.2 s, every load was 4 in 12 of 60 runs (6 of 60
nose-first), and neither version made a second TIP: R-Solo's second TIP on 19429 is short loads, not time.
It has not run on a robot.

## R-Clockwise-19429: R-Clockwise with the outside lane from the GARDEN driven backward

`r-clockwise-19429.pp` is master's `r-clockwise.pp` with one leg's heading changed: the outside lane from the GARDEN to
`LEFT_AUTO_HIVE` holds 270 (backward) until 82% of the way and then turns to the spot's 268.9, instead of turning round
to drive it nose-first. The drawn drive is 3.4 s instead of 3.8 s. It changes only **Pickup GARDEN**; with Pickup
WALL_FLOWER it is today's R-Clockwise. The other robot runs L-Clockwise beside it, unchanged. The first draft (18 Nov)
was made on that day's R-Clockwise; on 20 Nov 2026 it was rebuilt on master's, which has the R2 hold (#654), with the same
one change. It runs from the Utility menu as a 30 s trial, **Drive: R-Clockwise-19429** (`RClockwise19429Trial`, on
`BackwardLoadTrial`), under the same rule as R-Solo-19429 above: a bottom plate, and the backward drive in robot-todo's
step 9 passed first on that robot. Request: `sim-requests/r-clockwise-19429.json`.

**In the simulator** (20 Nov 2026, master's `sim-requests/r-clockwise.json` run on both, its 24 runs a case beside
L-Clockwise, TIPs settled by 38 s; the simulator never drops a load driving backward, so this is the time saved and
nothing about the plate). Every WALL FLOWER case, and every contingency, is identical in both. The GARDEN cases:

| Pickup GARDEN | R-Clockwise | R-Clockwise-19429 |
|---|---|---|
| Best case (`"scenario": "best"`) | 2.00 TIPs (T2 22.7 s), R parks 24/24 | 2.58 (third TIP in 14 of 24, T3 32.5 s), R parks 0/24 |
| Best, R 20% slow | 2.00, R parks 24/24 | 2.42 (third TIP in 10), R parks 0/24 |
| Best, L 20% slow | 2.00, both park | 2.17 (third TIP in 4), R parks 0/24, L 20/24 |
| Best, L's preloads miss | 1.00 | 2.00 (T2 30.4 s) |
| Normal | 1.62, R parks 23/24, 0 contacts | 1.58, R parks 21/24, 3 contacts |
| Normal, R 20% slow | 1.17, R parks 24/24 | 1.62, R parks 21/24, 1 contact |
| Normal, L 20% slow | 1.58, 5 contacts | 1.58, 8 contacts |
| Normal, L's preloads miss | 0.88, 0 contacts | 0.88, 3 contacts |

The time saved buys a third TIP in the best case, and R then goes for it rather than park (in the best case, the WALL
FLOWER pickup does the same on master: 2.92 TIPs, R parks 0/24). On normal loads it buys nothing, and R parks a little
less and touches L more often. No centre-line crossing or HIVE hit in any case.

## L-Assist: the far-end partner beside a near-end robot that tips both CELLs

`l-assist.pp` is master's `l-no-cross.pp` cut down (mentor, 19 Nov 2026, for 10255 RoboDux at Fruitport).
It does what RoboDux's partner did at Kentwood. From `LEFT_START` it drives to `LEFT_AUTO_HIVE` and waits
for TIP 1, which is RoboDux's preloads at RIGHT. Then it fires its preloads at LEFT and parks at `LEFT_PARK`
at once. If no TIP 1 comes by the wait's 9 s, it parks without firing. Its INIT menu is that one time. It runs from the Utility
menu as **Drive: L-Assist**; `sim-requests/l-assist.json` runs it beside L-NoCross's partners, since no
committed stand-in runs RoboDux's route.

It leaves the left end to RoboDux: under the HIVE to the left end by about 13 s, then its GARDEN 4 and its
SCORING FLOWER 4 at LEFT for TIP 2. Simulated beside a RoboDux stand-in, with both of our robots: 1.90 TIPs
(1.85 by 30 s), 0 contacts, and both robots parked in 20 of 20 runs.

## The RightUnderHive pair: R-RightUnderHive and L-RightUnderHive

**Parked until a super FLOWER pick exists** (mentor, 19 Nov 2026): back in `drafts/` and off both Driver Station
menus, with no OpMode. To beat the other pairs it needs a FLOWER pick that loads more at once; it is also one less
Auto to test before Fruitport. It was on the Autonomous menu on 19 Nov (PRs #650 and #652). The notes below hold
for the drawing as it stands, with R's hold after a TIP-1 fix and L's guarded OFFSET volley. R's fix spot `R_LEFT_FIX` moved to
(61.5, 114.0), facing 268.4 degrees, where the turret aims from it: at (60.0, 114.0) and 271.3 degrees R grazed L
at `LEFT_AUTO_OFFSET` in 3 of 20 simulated runs. The notes below are from the draft, which ran from the Utility menu.

`r-right-under-hive.pp` and `l-right-under-hive.pp` are a pair (mentor, 19 Nov 2026). The side before
"UnderHive" names the robot that goes under the HIVE, so here R goes under it and L takes the outer lane. It is
the lane pair turned round, for elims or a strong partner, not for qualifiers.

- **R owns every fix.** From `RIGHT_START` R fires its preloads at RIGHT from `RIGHT_AUTO_HIVE` (TIP 1), gathers
  the GARDEN and comes back to `RIGHT_AUTO_HIVE`. It waits there for RIGHT to be up (TIP 2 came, or TIP 1 never
  did), fires the GARDEN load at RIGHT, and goes up the corridor under the HIVE to park at `LEFT_PARK`.
- **If RIGHT is not up by 18 s** (adjustable in INIT), R goes up the corridor to its own spot `R_LEFT_FIX`
  (61.5, 114.0), fires at LEFT if LEFT is up (fixing TIP 2), and parks at `LEFT_PARK`.
- **Why 18 s and not 16:** at 18 s TIP 2 in the runs where L misses settles at about 30.7 s, which counts as AUTO
  (a TIP settling by 38 s, mentor, 19 Nov 2026). At 16 s it came at about 28.8 s, but with 92% of shots in R met L
  in 20 of 400 runs.
- **L fixes nothing.** From `LEFT_START` L waits at `LEFT_AUTO_HIVE` for TIP 1 (until 20 s) and fires its
  preloads at LEFT. Then it picks the SCORING FLOWER and fires that load from `LEFT_AUTO_OFFSET` if LEFT is still
  up. It goes on to the WALL FLOWER by the outer lane only before 22 s, then parks at `RIGHT_PARK`. After 22 s it
  parks straight from `LEFT_AUTO_OFFSET`.
- **Kept apart:**
  - R stays in the corridor under the HIVE (x 57 to 62), facing 90 degrees, until y is past 100.
  - R turns toward LEFT only west of x 58, so its frame never crosses the centre line.
  - R's fix spot keeps it clear of L at `LEFT_AUTO_OFFSET` and of a dead L on `LEFT_START`.
  - R's park routes clear the HIVE bar.
  - Each drive was checked against these limits by sampling its curve, then in the simulator.
- **No third TIP.** Each TIP takes 8 POLLEN in the CELL that is up (a CELL dumps its load when it goes down), and
  TIP 2 settles at about 24 s, after L's eighth piece in LEFT at about 19 s. Eight more in RIGHT, out by 30 s, do
  not fit.

**In the simulator** (19 Nov 2026, Belding calibration, 20 runs on field 2's seeds, the pair). TIP times are the
match clock at CELL settled; a TIP counts as AUTO when it settles by 38 s.

| Case | TIPs by 38 s | Both parked | Contacts | TIP 1 / TIP 2 |
|---|---|---|---|---|
| Every shot lands | 2.00 | 20 | 0 | 7.6 / 23.9 |
| R's preloads miss | 1.00 | 20 | 0 | 18.5 / none |
| L's preloads miss | 2.00 | 20 | 0 | 7.6 / 30.7 |
| L's FLOWER volley misses | 2.00 | 20 | 0 | 7.6 / 30.7 |
| R's GARDEN volley misses | 2.00 | 20 | 0 | 7.6 / 23.9 |
| Slow swing (TIP lag about 6.7 s) | 2.00 | 20 | 0 | 10.5 / 28.9 |
| L dead on its start | 1.00 | R | 0 | 7.6 / none |
| R dead on its start | 0 | L | 0 | none |
| 92% of shots in, Belding loads (19429 as R) | 1.85 | 20 | 0 | 7.7 / 27.6 |
| 92% of shots in, Belding loads (20245 as R) | 1.90 | 20 | 0 | 7.7 / 27.6 |

No run crossed the centre line or touched the HIVE.

## The ThreeTip pair: R-ThreeTip and L-ThreeTip

`r-three-tip.pp` and `l-three-tip.pp` are a draft pair for three TIPs from our two robots alone (19 Nov 2026), drawn
from the timing plan in which each TIP takes 8 POLLEN in the CELL that is up and a TIP counts when it settles by 38 s.
They run from the Utility menu as trials, **Drive: R-ThreeTip** (`RThreeTipTrial`) beside **Drive: L-ThreeTip**
(`LThreeTipTrial`), and never one without the other; not on the landing page (mentor, 19 Nov 2026).

- **TIP 1, R's preloads.** R fires them at RIGHT from `RIGHT_START` (a start is a legal launch spot).
- **TIP 2, LEFT gets 4 + 4.** L fires its preloads from `LEFT_AUTO_HIVE` when LEFT reads up. R gathers the GARDEN,
  takes the outer lane to its own spot `R_LEFT_WEST` (33, 98, 335.8), clear of L at the SCORING FLOWER, and fires the
  GARDEN 4 at LEFT.
- **TIP 3, RIGHT gets 4 + 4.** L picks the SCORING FLOWER and crosses under the HIVE to `RIGHT_AUTO_HIVE`. R picks the
  WALL FLOWER and fires from its own spot `R_RIGHT_WF` (27, 50, 18.7) beside it. Each fires at RIGHT once TIP 2 has
  read up (L by 22.5 s, R by 24 s), so the park after the volley still fits.
- **Parks:** R goes up the outer lane to `RIGHT_PARK`. L goes up the corridor and along y 119 to `LEFT_PARK`.
- **If TIP 1 has not come by 11 s** (L1, adjustable in INIT), L rescues it from `RIGHT_AUTO_FIX_TIP`. R fires its
  GARDEN load at LEFT if LEFT is up by 21 s, then parks; L holds until 22 s and parks.

**In the simulator** (19 Nov 2026, best case `"scenario": "best"`, 20 runs on field 2's seeds, TIPs settled by 38 s;
0 contacts, no centre-line crossings or HIVE hits, every robot that runs parked):

| Case | TIPs | TIP 1 / 2 / 3 |
|---|---|---|
| Best case | 3.00 | 7.5 / 21.6 / 31.3 |
| R's preloads miss | 1.00 | 22.0 |
| L's preloads miss | 1.00 | 7.5 |
| L's second volley misses | 2.00 | 7.5 / 21.6 |
| R's second volley misses | 1.00 | 7.5 |
| R's third volley misses | 2.00 | 7.5 / 21.6 |
| Slow swing | 2.00 | 10.4 / 24.3 |
| L dead / R dead | 1.00 / 1.00 | |
| 92% of shots in | 1.40 (3 TIPs in 1 run, 2 in 7) | |

- **Fragile by design:** each CELL gets exactly 8, so one missed piece in a TIP-2 volley loses TIP 2 and with it TIP 3.
  At 92% in, that happens in 13 of 20 runs. The pair is worth it only where shots and picks are near 100%.
- **Single failures at 1 TIP:** R's preloads, L's preloads, R's GARDEN volley. Nothing on the field can tell either robot
  early that LEFT will stay one load short, so neither can switch to fixing it in time.

## The lane pair: R-OutsideLane and L-UnderHive

A second way at three TIPs beside the Clockwise pair: R keeps to the wall side of the HIVE and L
to the under-HIVE side. Mentor session, 12 Nov 2026.

**No longer a draft (16 Nov 2026):** the pair is in `TeamCode/autos/` and on the Autonomous menu as
R-OutsideLane and L-UnderHive, launching from the Autos' launch spots with its drives as single
curves ([the Auto README](../README.md)). The notes below are from the draft, which launched from
Square's close row (L4, R4, R5) and ran from the Utility menu; where they name those spots, the
Autos now use `LEFT_AUTO_OFFSET` (R) and `LEFT_AUTO_HIVE` (L) at the left end, and
`RIGHT_AUTO_HIVE` (L) and `RIGHT_AUTO_OFFSET` (R) at the right.

Open the pair: <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-outside-lane.pp,l-under-hive.pp>

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

**Pickup, in the INIT menu** (mentor, 13 Nov 2026: the row R-Clockwise has). Each trial offers one
row, `Pickup`, on gamepad 1's D-pad left and right:

| | Drawn | The other choice |
|---|---|---|
| R-OutsideLane | `WALL_FLOWER` | `GARDEN`: the GARDEN in its own corner, then up the wall lane as before |
| L-UnderHive | `GARDEN` | `WALL_FLOWER`: under the HIVE, then across to the WALL FLOWER |

The two robots must show different pickups. Nothing checks that: the drive teams read their rows
to each other.

- **Leave both as drawn for now.** The swap (R `GARDEN`, L `WALL_FLOWER`) does not work with today's
  gather. In the simulator the gather keeps R in its corner for the card's whole 5.5 s, because it
  takes 2 or 3 pieces and waits for a fourth. R is still leaving when L comes across to the WALL
  FLOWER, and the robots touched in 17 of 20 runs. R then reaches the left spot at about 16.9 s,
  after its 14 s limit, and parks: one TIP.
- **What the swap needs:** R out of the corner by about 9.5 s, which is a gather of about 4 s or
  less. Then R's first pickup is 25 in from its start, where the WALL FLOWER is 50 in.
- **L's way to the WALL FLOWER leaves its lane.** It comes out from under the HIVE at the south end
  and crosses to the alliance wall, where R drives. Only the clock keeps them apart.
- **Both on `GARDEN`** touched in 17 of 20 (the same corner). **Both on `WALL_FLOWER`** ran as the
  drawn pairing does: L finds the FLOWER already picked.

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
- **R's time decides the third TIP; L has 4.6 s to spare.** A path study (13 Nov 2026, the
  Visualizer's timing, each shape checked in the simulator for the HIVE and the centre line) took
  0.7 s off R: it turns toward the SCORING FLOWER as it leaves the left spot (1.3 s, was 1.6), and
  drives back down on a straighter line with its two turns spread along it (3.0 s, was 3.4). Its
  other drives are at what the robot's top speed allows. In the preview the third volley now fires
  and its TIP settles at 30.0 s on a 2.5 s volley, 29.7 s on 2.4 s. A line 4 in nearer the HIVE was
  0.1 s quicker and clipped the HIVE's corner in the simulator while R turned.
- **The preview's 3 s for the intake-first leg is not measured** (above). If that leg takes the
  simulator's 4.4 s, R's LEFT volley ends at about 15.7 s and it parks after it.
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
