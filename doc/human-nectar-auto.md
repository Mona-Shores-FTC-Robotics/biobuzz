# Human NECTAR in AUTO: where, when, and is it worth it (4 Nov 2026, study)

A study for the mentor's decision, backed by simulator runs. No robot code changes here. The rules are as the team
read them and the Q&A confirmed them (`doc/human-nectar.md` in branch history, commit `f9831183`;
`doc/game/qa-review.md` on `claude/simulator`). Our human player may enter one NECTAR per TIP of our HIVE (G426.A,
Q11). They release it in the air, its first touch is the LOADING ZONE tile (G427.C), they may roll it in a useful
direction (Q7), they never touch it again, and they never signal the robot.

## The answer

- **NECTAR #2 has no use in AUTO.** It comes after TIP 2, which falls at 16–28 s in every run below, and no third
  TIP fits after that. The human should **keep #2 for TELEOP**: a NECTAR does not have to go in the moment it is
  earned (G426 note). It also should not go in while our robots park in the zone (LEFT_PARK and RIGHT_PARK are both
  in it, and G427.C applies).
- **NECTAR #1 earns no extra points of its own.** The robot holds 4 pieces, so a NECTAR takes a POLLEN's place in
  the TIP-2 volley. What it adds is weight: 1N+7P is 8.65 POLLEN-weights where 8P is 8.0. That gives a one-POLLEN
  miss margin only if the TIP point lies between 7.3 and 7.65, and the Field Setup Guide cases only bound it between
  7.3 and 8.0. The **cost is time**: about 2–3 s to grab it, which pushes TIP 2 later.
- **So it is worth it only if our AUTO shots really miss.** In the simulator, at 99 % of shots scoring, the NECTAR
  changes nothing except a TIP 2 that comes 2 s later. At 93 % it lifts R-Solo's TIP 2 from 15/30 to 22/30 runs
  (+4.6 points a match on average), and L-Partner's from 7/20 to 12/20 (+5 points). At 76 % and below, TIP 1 itself fails too often for a NECTAR to matter.
  **Measure our AUTO shot percentage from the match logs first.** That one number makes the decision.
- **If we pursue it:** start with **R-Solo** (it does not depend on a partner's timing), then L-Partner when the
  partner's last shot is 8 s or earlier. Use a drive-in grab of a NECTAR dropped in the middle of the zone, taken
  after the first LEFT volley. Do not use a waiting catch, and do not stay at a launch spot. The details are below.

## Where #1 can be taken (R-Solo, and L-Partner)

R-Solo's TIP 1 lands at about 4.7 s, so #1 can be on the tile from about 6–7 s. Its sequence: preload at the RIGHT
CELL (0–3.7 s) → WALL FLOWER pick (5.5–8.8 s, **robot full: 4**) → LEFT_LAUNCH_OFFSET volley (11.1–14.1 s, empty)
→ SCORING FLOWER pick → LEFT volley → TIP 2 (~24.2 s).

| Slot | Possible? | Cost |
|---|---|---|
| A stop on the way from WALL_FLOWER_STAGING to LEFT_LAUNCH_OFFSET (the brief's idea) | **No.** The robot is full (4 POLLEN) after the WALL FLOWER pick. It only works if the pick stops at 3, which no card can do today. | — |
| **After the first LEFT volley, before the SCORING FLOWER**: drive to (26, 106) facing the wall, then in to (12, 106), then on to SCORING_FLOWER_STAGING. The PickFlower then tops up to 4 (1N+3P). | Yes | **+2.8 s** to the next pick; **TIP 2 +2.0 s** (24.2 → 26.2 s) |
| A waiting catch: park at (10, 96) facing +y, and the human rolls it along the wall on cue (the old doc's technique) | Yes | **+5.1 s**; TIP 2 at 27.8 s, 2 s from the end of AUTO. Too costly. |
| Stay at LEFT_LAUNCH_OFFSET while the human rolls it there | Geometrically possible, but worse (see below) | ~4–5 s, with the most risk |

**L-Partner** has the same slot: after its first LEFT volley from LEFT_LAUNCH_CATCH (57.5, 123.5), drive to the
zone, then on to the SCORING FLOWER. It costs TIP 2 **+1.7–2.0 s**. With a partner whose last shot is at 12 s, that
TIP 2 lands at 28.0 s instead of 26.3 s, which is the case to worry about. L-Partner's request runs the POLLEN-only
design. Taking NECTAR also assumes the robot launches NECTAR in AUTO, which the 3–4 Nov shots suggest it does.

## Simulator results

Settings for every run: the "real robot, turret locked forward" design (no turret, launches NECTAR), red, 50 in/s
for R-Solo. The study variants were Autos in the test source set only, not committed, built from master's generated
R-Solo and L-Partner with the grab or catch added. The simulator's sim-only `IntakeOn` card kept the intake running
on the drive in. The human entry was the simulator's default (dropped mid-zone 2 s after the TIP), or `roll` + `cue`
for the catch.

**R-Solo + a still partner** (seeds 1–20 at spread 1; 1–30 at the others). `BIOBUZZ_AUTO_SPREAD` scales the
launcher's spread; "shots" is the share of shots that scored.

| Spread (shots) | Master R-Solo | + drive-in grab | + waiting catch |
|---|---|---|---|
| 1 (99 %) | TIP 2 20/20 @ 24.2 s, 48.0 pts | 20/20 @ 26.2 s, 48.0 pts (NECTAR fired 10/20) | 20/20 @ 27.8 s, 48.0 pts (caught 16/20) |
| 2 (93 %) | 15/30 @ 24.0 s, 36.7 pts | **22/30 @ 26.3 s, 41.3 pts** | 22/30 @ 28.7 s, 41.3 pts |
| 3 (76 %) | 1/30, 20.7 pts | 3/30, 22.0 pts | 4/30, 22.7 pts |
| 4 (66 %) | TIP 1 only 13/30, 16.7 pts | 2/30 TIP 2, 18.0 pts | 16.7 pts |

**L-Partner** (SimRunTest on its own request's six partner cases, 20 seeds each):

| Partner | Master L-Partner | + drive-in grab (NECTAR fired 13–14/20) |
|---|---|---|
| last shot at 2 / 5 / 8 / 12 s, spread 1 | TIP 2 20/20 @ 16.3 / 19.3 / 22.3 / 26.3 s, 56 pts | 20/20 @ 18.3 / 21.0 / 24.0 / 28.0 s, 56 pts |
| misses / dead | no TIP 2, 36 / 28 pts | the same |
| last shot at 2 / 5 / 8 s, spread 2 | TIP 2 7/20 each, 43 pts | **12/20 each, 48 pts** (TIP 2 @ 19.3 / 22.4 / 25.4 s) |
| last shot at 12 s, spread 2 | 7/20 @ 26.3 s, 43 pts | 10/20 @ 29.1 s, 46 pts: TIP 2 lands in AUTO's last second |

The drive-in grab was blind: it drove to a fixed point, and the NECTAR went into the volley only 50 % (R) to 70 % (L)
of the time. A webcam-steered grab should do better, so the drive-in numbers at spread 2 are a floor.

**Missing in the simulator and on the robot:**
- A grab card for a loose piece, e.g. `PickNectar`: `GrabRoutine`'s push, dwell and back-out, steered by
  `PieceVisionSubsystem` onto our NECTAR in the LOADING ZONE, giving up after a set time when nothing is seen. The
  simulator needs the same card; today a study can only fake it with the sim-only `IntakeOn` and a fixed point.
- A trigger that ends a wait as soon as a piece is taken ("Held"/"HoldsNectar"). Without it, the catch variant
  waits its full 2.5 s.
- NECTAR's HSV range in `PieceVisionSubsystem` is still unmeasured (−1).
- A check that the SCORING FLOWER pick stops at 4 when it starts already holding a NECTAR (`IntakeFull`). In the
  simulator it does.

## Staying at a launch spot while the human rolls it there (the mentor's idea)

It is geometrically possible, but it costs more than driving over to grab it.
- The zone is x 0–11, y 94–118. The nearest release, about (6, 117), is **36 in** from LEFT_LAUNCH_OFFSET (41, 121.5)
  and **52 in** from LEFT_LAUNCH_CATCH. The roll is straight out from the wall, not along it.
- The intake and the launcher both face the front. At LEFT_LAUNCH_OFFSET the robot faces 296°, and to face the roll
  it must turn to about 187°: **about 110° each way**, and it cannot fire while turned.
- The roll covers about 27 in to the intake. At 15 in/s with the simulator's NECTAR rolling resistance (1.5 in/s²)
  that takes about 2 s. Add about 1 s for the human to see the robot turned and release, about 0.5 s for each turn,
  and the intake time: **about 4–5 s**, against 2–3 s for the drive-in grab.
- Its risks are worse too. A 27–36 in roll across open tiles has to stop in a 14 in intake (the one filmed entry,
  Saline P3, stopped 11 in out from a 17–20 in/s roll, so distance control is unknown). The roll line also crosses
  the robots' own path into LEFT_LAUNCH_OFFSET and LEFT_PARK (14.5, 117.4).
- Only a rear intake or a turret would let the robot wait at the spot without turning, and the mentor has decided
  against a turret.

## Recommendation

1. **Now:** the human player keeps NECTAR #2 for TELEOP, and enters #1 in AUTO only once an Auto takes it. A NECTAR
   left in the zone stays in the way of both PARK spots.
2. **Measure first:** our AUTO shot percentage, from the 3–4 Nov match logs and the next event. If it is about 97 %
   or better, stop here: the NECTAR costs 2 s and buys nothing. If it is about 90–95 %, go on to step 3.
3. **Build, in order:** the `PickNectar` grab card on the robot, and the same card in the simulator; the NECTAR HSV
   range; then an R-Solo variant on `autos-experiment` with the grab between the first LEFT volley and the SCORING
   FLOWER. Re-run the spread-2 study with it. L-Partner gains as much (7/20 → 12/20 at spread 2) but comes second:
   with a partner whose last shot is at 12 s, its TIP 2 moves to 29.1 s. Skip the grab there, for example with an
   INIT option keyed to the partner's last shot.
4. **Drop the waiting catch and the stay-at-spot roll.** Both cost twice as much time as the drive-in grab.

## Practice drill for the human player (the drop the drive-in grab wants)

This replaces the roll-along-the-wall drill in `doc/human-nectar.md`. The grab needs a NECTAR that lies **still near
one known spot** (the middle of the zone, about 5 in off the wall) by about 10 s, not a roll that has to be timed.
1. Tape a 4 in square in the middle of the LOADING ZONE. Someone calls "TIP" at random. The human releases the
   NECTAR in the air, low, over the square, and never touches it again. Do 20 a person.
2. Record for each: the time from the call to release, where it came to rest (in or out of the square, and how far
   from its centre), and whether it rolled out of the zone.
3. Targets: released within 2 s of the call, and 19 of 20 at rest inside the square. One that rolls out of the zone
   is a fail.
4. With a robot, once `PickNectar` exists: 20 drops, counting clean grabs and the time from the start of the card to
   holding the NECTAR. Send the numbers to the simulator: they replace its fixed mid-zone drop.

## Reproducing

The Android SDK was installed in the session (`platforms;android-34`, `build-tools;34.0.0`), and Gradle ran the
studies. Examples:
```
BIOBUZZ_AUTO_STUDY="RSoloAuto,PartnerStillAuto@50" BIOBUZZ_AUTO_DESIGNS="real robot, turret locked forward" \
  BIOBUZZ_AUTO_RUNS=30 BIOBUZZ_AUTO_SPREAD=2 ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*' -i
# catch variant: add BIOBUZZ_AUTO_HUMAN_ENTRY=roll BIOBUZZ_AUTO_HUMAN_CUE=1
```
The variants' `kit.sequence` changes are in the table above. Each inserts `IntakeOn` and two path cards
(LEFT_LAUNCH → (26, 106, 180°) → (12, 106, 180°) → SCORING_FLOWER_STAGING) after the first LEFT `LaunchAll`.
`SimRunTest` does not read `BIOBUZZ_AUTO_SPREAD`, so the L-Partner spread-2 runs used a local one-line patch to it,
which is not committed.
