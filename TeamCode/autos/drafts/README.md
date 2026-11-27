# Drafts

Planning Autos have a `.pp` here and its generated class, with no competition OpMode. The Visualizer lists
them under "Drafts", and Save to GitHub saves back here. Since 19 Nov 2026 this folder is kept small
(mentor): it holds no drafts now, only the notes below. The RightUnderHive pair left this folder for the Autonomous
menu on 27 Nov 2026 (its notes, below, stay here).

**Removed on 27 Nov 2026** (mentor: no longer needed), with their generated classes, Utility trials and sim
requests, all in Git history at `8bcae0b`: L-Assist (**Drive: L-Assist**), L-LeftUnderHive-StrangeGarden
(**Drive: L-LeftUnderHive-StrangeGarden**, outdated) and R-Solo-WallPark (**Drive: R-Solo-WallPark**, its wall park
now R-Solo's own).

**Removed on 19 Nov 2026**, with their generated classes, sim requests, the nudge test's code
(`NudgeContactDrive`, `NudgeTestRun`) and the stand-ins only they used. All are in Git history at
`f2c9bb7`; `git checkout f2c9bb7 -- TeamCode/autos/drafts/<name>.pp` and its class in
`opmodes/auto/generated/` bring one back:

- Drafts: `l-clockwise-backward`, `l-clockwise-close`, `l-clockwise-flower`, `l-clockwise-garden`, `l-clockwise-next`, `l-pair-flowers`, `l-three-tip` (likewise reused), `l-under-hive-fix`, `l-under-hive-park-shot` (became l-under-hive.pp), `partner-nudge-test`, `r-clockwise-19429` (rebuilt on 20 Nov 2026 as `r-clockwise-backwards`, below), `r-clockwise-backward`, `r-clockwise-close`, `r-clockwise-flower`, `r-clockwise-garden`, `r-clockwise-next`, `r-outside-lane-fix`, `r-pair-garden`, `r-pair-garden-sweep`, `r-solo-19429` (brought back on 20 Nov 2026 as `r-solo-backwards`, whose backward leg is now R-Solo's, below), `r-solo-human-player`, `r-solo-nudge`, `r-three-tip` (the name now holds the new ThreeTip draft below), `r-three-tip-still`, `r-three-tip-stream`, `r-two-tip`, `r-two-tip-with`.
- Stand-ins in `partners/`: `partner-l-fill-hide-park`, `partner-l-stage-leave-park`, `partner-l-timed-fill-hide-park`, `partner-r-launch-stay`, `partner-r-tip-far-park`.

## R-Solo-Backwards: promoted into R-Solo (20 Nov 2026)

`r-solo-backwards.pp` was master's `r-solo.pp` of 18 Nov with one leg's heading changed, so the robot no longer turned
round just to keep the load in: the WALL FLOWER leg to `LEFT_AUTO_HIVE` turns 180 to 268.9 in its first 35%, then drives
backward. It ran clean on 20245 with its bottom plate (mentor, 20 Nov 2026: "ran perfect"), and that leg is now R-Solo's
(`BACKWARD to LEFT_AUTO_HIVE` in `../r-solo.pp`). The draft itself was older than the 19 Nov move up to `RIGHT_AUTO_HIVE` for
the preloads, so only the leg was carried over, onto master's R-Solo, which keeps that move; the draft, its class, its
Utility trial and its sim request are gone (Git history). Driving backward with a load needs the bottom plate under the
intake (20245's first Clockwise drive, 12 Nov, with no plate, lost pieces on both backward legs); both robots have one.
The draft's simulator comparison (20 Nov 2026, Belding calibration, which also differed in the `RIGHT_AUTO_HIVE` move) had
TIP 2 0.4 s earlier in the best case and the fix TIP 0.8 s earlier, and every robot parked; on 18 Nov, on 19429's own loads,
the leg took 3.2 s instead of 4.2 s.

## R-Clockwise-Backwards: promoted into R-Clockwise (20 Nov 2026)

`r-clockwise-backwards.pp` was master's `r-clockwise.pp` with its outside lane from the GARDEN driven backward. On 20 Nov
2026 (mentor: "the backwards stuff works and it should be the norm") both of R-Clockwise's loaded outside lanes became
backward in `../r-clockwise.pp`, and the draft, its class, its Utility trial **Drive: R-Clockwise-Backwards** and its sim
request are gone (Git history):

- `outside lane BACKWARD from the GARDEN to LEFT_AUTO_HIVE` holds 270 until 82% of the way, then turns to 268.9.
- `outside lane BACKWARD from the FLOWER to LEFT_AUTO_HIVE` turns 180 to 270 in its first 15%, holds 270 until 82%, then
  turns to 268.9.
- Each lane waits first for a time on the match clock, **L clear of LEFT_AUTO_HIVE: go at 9.8 s** (WALL FLOWER) and
  **at 9.9 s** (GARDEN). Backward, R reaches `LEFT_AUTO_HIVE` up to 2.8 s sooner (the simulator's turn in place is
  slow), and with a late TIP 1 L is still on that spot firing its preloads until about 11.5 s: without the wait the
  simulator had them touch at 11.7 s in 3 of 24 runs, as 20245 and 19429 did on the robot on 18 Nov at 22:15 (PLAY late,
  TIP 1 late, R drove up against L).

**In the simulator** (20 Nov 2026, master's `sim-requests/r-clockwise.json` on both, 24 runs a case beside L-Clockwise,
TIPs settled by 38 s; the simulator never drops a load driving backward, so this is the time saved and nothing about the
plate):

| Case | R-Clockwise, nose-first | R-Clockwise, backward lanes |
|---|---|---|
| WALL FLOWER, best case | 2.92 TIPs | 2.96 |
| WALL FLOWER, best, R 20% slow | 2.08 | 2.04 |
| WALL FLOWER, normal | 1.54, 1 contact | 1.58, 0 contacts |
| WALL FLOWER, normal, L's preloads miss | 0.96, 1 contact | 1.00, 0 contacts |
| GARDEN, best case | 2.00, R parks 24/24 | 2.46 (third TIP in 11 of 24), R parks 0/24 |
| GARDEN, normal | 1.62, R parks 23/24 | 1.58, R parks 21/24, 0 contacts |
| GARDEN, normal, R 20% slow | 1.17 | 1.58 |
| GARDEN, normal, L 20% slow | 1.58, 5 contacts at 27.3 s | 1.58, 5 contacts |

R not parking in the GARDEN best case is the Priority row going for its third TIP (master's WALL FLOWER does the same in
the best case). Every other case and contingency matches master. No centre-line crossing or HIVE hit in any case.

## The RightUnderHive pair: R-RightUnderHive and L-RightUnderHive

**No longer a draft (27 Nov 2026):** the pair is in `TeamCode/autos/` and on the Autonomous menu as R-RightUnderHive
and L-RightUnderHive, redrawn for three TIPs with a fallback for each missed load ([the Auto README](../README.md)). The
notes below are from the draft as it was on 20 Nov 2026.

**Parked until a super FLOWER pick exists** (mentor, 19 Nov 2026): back in `drafts/` and off both Driver Station
menus, with no OpMode. To beat the other pairs it needs a FLOWER pick that loads more at once; it is also one less
Auto to test before Fruitport. It was on the Autonomous menu on 19 Nov (PRs #650 and #652). The notes below hold
for the drawing as it stands, with R's hold after a TIP-1 fix and L's guarded OFFSET volley. R's fix spot `R_LEFT_FIX` moved to
(61.5, 114.0), facing 268.4 degrees, where the turret aims from it: at (60.0, 114.0) and 271.3 degrees R grazed L
at `LEFT_AUTO_OFFSET` in 3 of 20 simulated runs. The notes below are from the draft, which ran from the Utility menu.

`r-right-under-hive.pp` and `l-right-under-hive.pp` are a pair (mentor, 19 Nov 2026). The side before
"UnderHive" names the robot that goes under the HIVE, so here R goes under it and L takes the outer lane. It is
the lane pair (R-LeftUnderHive / L-LeftUnderHive, where L goes under) turned round, for elims or a strong partner, not for qualifiers.

**Against the LeftUnderHive pair** (20 Nov 2026, master `3030a44`, both pairs in one request: `real robot` both sides,
Belding calibration, 20 seeds a row, TIPs settled by 38 s; normal / best case):

| Case | LeftUnderHive TIPs, points | RightUnderHive TIPs, points |
|---|---|---|
| As drawn | 1.90, 54 / **2.90, 74** | 1.70, 50 / 2.00, 56 |
| L 20% slow | 1.80, 52 / **2.95, 75** | 1.70, 50 / 2.00, 56 |
| R 20% slow | 1.85, 53 / **3.00, 76** | 1.70, 50 (R unparked in 1) / 2.00, 56 |
| R's preloads miss | 0.75, 31 / 1.00, 36 | 0.65, 29 / 1.00, 36 |
| L's preloads miss | 1.25, 41 / 1.95, 55 | 1.15, 39 / 2.00, 56 |
| L dead on `LEFT_START` | 0.95, 27 / 1.00, 28; **touches the dead L in 19 and 20 of 20** | 0.95, 27 / 1.00, 28; no contact |

LeftUnderHive is ahead in every case but two: RightUnderHive never touches a dead L (the lane pair's known `LEFT_START`
limit), and in the best case with L's preloads missing it has 2.00 TIPs to 1.95. It never reaches a third TIP; the
LeftUnderHive pair does in nearly every best-case run. So it stays a draft, for a partner who needs the outside lane.

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

## The ThreeTip pair: removed 20 Nov 2026

`r-three-tip.pp` and `l-three-tip.pp` (19 Nov 2026), with their classes and the Utility trials **Drive: R-ThreeTip** and
**Drive: L-ThreeTip**, were removed on 20 Nov 2026 (mentor: not practical). They split the same three TIPs as the
LeftUnderHive pair differently (R fired its preloads from `RIGHT_START`, gathered the GARDEN for LEFT, then took the
WALL FLOWER to RIGHT; L took the SCORING FLOWER under the HIVE to RIGHT), and lost to it in the simulator (20 Nov 2026,
Belding calibration, 10 seeds a case): best case TIP 2 / TIP 3 at 21.1 / 30.8 s against 19.1 / 29.0, 2 TIPs instead of
3 with R 20% slow, and 40 points (1.2 TIPs) against 56 (2.0) with normal misses. Both are in Git history at `7b09c29`.

## The lane pair: R-OutsideLane and L-UnderHive

**Renamed R-LeftUnderHive / L-LeftUnderHive on 20 Nov 2026** (`r-left-under-hive.pp`, `l-left-under-hive.pp`; mentor),
so both halves carry the pair's name. The notes below keep the names they were written under.

A second way at three TIPs beside the Clockwise pair: R keeps to the wall side of the HIVE and L
to the under-HIVE side. Mentor session, 12 Nov 2026.

**No longer a draft (16 Nov 2026):** the pair is in `TeamCode/autos/` and on the Autonomous menu as
R-OutsideLane and L-UnderHive, launching from the Autos' launch spots with its drives as single
curves ([the Auto README](../README.md)). The notes below are from the draft, which launched from
Square's close row (L4, R4, R5) and ran from the Utility menu; where they name those spots, the
Autos now use `LEFT_AUTO_OFFSET` (R) and `LEFT_AUTO_HIVE` (L) at the left end, and
`RIGHT_AUTO_HIVE` (L) and `RIGHT_AUTO_OFFSET` (R) at the right.

Open the pair: <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-left-under-hive.pp,l-left-under-hive.pp>

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
