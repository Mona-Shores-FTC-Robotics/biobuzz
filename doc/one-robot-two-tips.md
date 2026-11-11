# Two TIPs from one robot: what the robot needs (10 Nov 2026, study)

A study for the mentor, run in the simulator set to the Belding Qualifier (`BeldingCalibration`;
TeamCode/README.md, "Simulating an Auto"). The Auto is the R-Two-Tip draft
(`TeamCode/autos/drafts/README.md`), alone, with master's endgame guard, 120 seeds a row. No robot
code changes here. The target is two TIPs in 3 runs out of 4.

## The answer

- **As the robots shot at Belding, one robot makes two TIPs in half its runs** (60 of 120). No
  drawing does better: the two LEFT volleys are 8 pieces and the LEFT CELL needs all 8.
- **3 runs in 4 takes two things together: 0.98 of the shots from the LEFT launch spots staying in
  (0.92 at Belding), and a SCORING FLOWER pick that always comes back full.** That gives 94 of 120.
  With 0.96 and the pick fixed it is 85 of 120, just short.
- **The shot counter adds no TIPs, but it is what keeps them when the robot runs slow**, and it
  brings the second TIP in from 28.5 s to 27.7 s.
- **Drive time, and accuracy from the start, add almost nothing.** A turret added nothing either.

## The ranking

One change at a time, then together. "Stay in" is the share of launched pieces that stay in the CELL.

| | Change | Two TIPs of 120 | Gain |
|---|---|---|---|
| | As at Belding | 60 | |
| 1 | **Shot accuracy from the LEFT spots:** 0.98 stay in | 84 | +24 |
| | the same, 0.96 | 76 | +16 |
| 2 | **Flower picking:** the SCORING FLOWER pick always comes back full (2 of 14 did not) | 67 | +7 |
| 3 | Shot accuracy from `RIGHT_START`: 0.92 (0.82) | 62 | +2 |
| 4 | Drive time: 10% faster | 61 | +1 |
| 5 | **Shot counter:** a volley ends at its fourth shot | 59 | −1, but see below |

| Together | Two TIPs of 120 |
|---|---|
| LEFT 0.96 + picks | 85 |
| LEFT 0.96 + picks + start 0.92 | 88 |
| LEFT 0.96 + picks + start 0.92 + counter | 87 |
| **LEFT 0.98 + picks** | **94** |
| LEFT 0.98 + picks + start 0.92 | 99 |

| Driving 28% slower | Two TIPs of 120 |
|---|---|
| As at Belding | 0 |
| With the shot counter | 62 |

Why the order comes out this way:

- **Accuracy at the LEFT CELL compounds.** Eight pieces in a row have to stay in: 0.92 gives all
  eight about half the time, 0.96 about 7 times in 10, 0.98 about 17 in 20. Nothing else is raised
  to the eighth power.
- **A short pick is rare but total.** When the SCORING FLOWER pick comes back with nothing, the
  second TIP is gone whatever the aim.
- **The first TIP is already there** 19 times in 20, because three of the four preloads are enough.
- **Time is not what is missing**, as long as the third volley runs at all. Master's guard skips it
  when the robot is late; the counter takes about a second off each volley after a pick, which is
  the slack. Without it the Auto as drawn holds to 20% slower and loses everything at 28%.

## What each would take

| | To build or tune | Already on the list as |
|---|---|---|
| **1. LEFT accuracy** | Measure the speed-table rows from the LEFT spots themselves (they were measured on the right and assumed the same): hold Square at `LEFT_LAUNCH_CATCH` and `LEFT_LAUNCH_OFFSET`, trim, write the rows. Check the aim there with the turret locked (the right corner spot was 5° off). Then the launcher holding its speed through a volley: the first ball is slow out of the gate (0.8 s) and at 1580 ticks/s the fourth fell off the CELL's lip, so film a volley at 240 fps and look at the wheel speed across the four shots. Reseat the launcher encoder cables. | `robot-todo` §7d; fix list "film a volley at 240 fps", "reseat both launcher encoder cables" (#496), "turn RIGHT_LAUNCH_CORNER's heading" |
| **2. Flower picking** | Find why the SCORING pick stops 4.6 in short with pieces left (Infrared, Q-4), and stop two pieces wedging side by side at the intake (Ultraviolet, Q-1): a stagger guide or a short reverse when the intake stalls with the lane not full. | #507; fix list "stop two pieces wedging" |
| **5. Shot counter** | Fit the break-beam at the launcher throat on the free digital channel and count shots from it; the feed then ends when the fourth ball leaves. | fix list "add a break-beam at the launcher throat" (#213's channel); `robot-todo` §7c |
| 3. Start accuracy | The same launcher work as 1; nothing separate. | |
| 4. Drive time | Nothing worth doing for this Auto. | |

## The measurements that would confirm it

Each replaces a number the ranking rests on. In order of how much it could change the ranking:

1. **Balls that stay in from `LEFT_LAUNCH_CATCH` and `LEFT_LAUNCH_OFFSET`.** Five volleys of four
   from each, filmed from the side, after a FLOWER pick as in the Auto (not hand-fed). Count what
   stays in. The calibration's 0.92 is inferred from L-NoCross's 3 second TIPs in 5, not counted, and
   `LEFT_LAUNCH_OFFSET` (0.89 by range) has never been fired from on the left. 20 balls a spot tells
   0.85 from 0.95; telling 0.96 from 0.98 takes about 100.
2. **SCORING FLOWER picks that come back full.** Twenty picks on each robot, counting the pieces in
   the lane afterwards. The calibration has 2 short in 14.
3. **How many landed POLLEN tip the LEFT CELL on the practice HIVE.** Place 6, 7, then 8 by hand, five
   times each, and note whether and when it tips. The calibration has Field 1 tipping on 7 two times in
   three and Field 2 on 8.
4. **The time from the last ball to the TIP.** From the same films. The calibration has the CELL
   sitting 3.6 to 4.2 s, which puts the second TIP at 28.5 s; a slower CELL puts it past 30 s.
5. **When each volley and each pick ends in a full run** (`read_logs.py summary` on five runs of
   Drive: R-Two-Tip): the second volley's end, whether the guard let the third volley go, and the
   feed's length against the last ball leaving.
6. **Balls that stay in from `RIGHT_START`**, from the same runs' films: the calibration's 0.82.

## How it was run

```bash
BIOBUZZ_AUTO_CALIBRATION=belding BIOBUZZ_AUTO_STUDY="RTwoTipAuto@50" \
  BIOBUZZ_AUTO_DESIGNS="real robot, turret locked forward" BIOBUZZ_AUTO_RUNS=120 \
  BIOBUZZ_BELDING_STAY_CATCH=0.98 BIOBUZZ_BELDING_PICK_SHORT=0 \
  ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*' -i
```

The what-if knobs: `BIOBUZZ_BELDING_STAY_CATCH` / `_START` / `_CLOSE` (a launch range's share that
stays in; the LEFT spots follow `_CATCH`), `BIOBUZZ_BELDING_PICK_SHORT` (the share of SCORING picks
that come back with nothing), `BIOBUZZ_BELDING_WHOLE_FEED=0` (the shot counter works), and the
number after `@` (drive speed, in/s). Seeds 1 to 120; a row moves by about ±5 of 120 from one set of
seeds to another, so differences under 5 are not differences.

**What the simulator does not know:** how the launcher's accuracy changes with battery or with a
piece's place in the volley, picks that come back with one to three pieces, and anything another
robot does.
