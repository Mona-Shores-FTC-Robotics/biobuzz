# Two TIPs from one robot: what the robot needs (10 Nov 2026, study)

A study for the mentor, from the Belding Qualifier's match logs and the simulator set to them
(`BeldingCalibration`; TeamCode/README.md, "Simulating an Auto"). The Auto is the R-Two-Tip draft
(`TeamCode/autos/drafts/README.md`), alone, with master's endgame guard, 120 seeds a row. No robot
code changes here. The target is two TIPs in 3 runs out of 4.

## The answer

- **What stopped the second TIP at Belding was pieces that never got launched.** R-Solo's two LEFT
  volleys never fired the 8 pieces the LEFT CELL needs; L-NoCross made the TIP in 3 of the 4 runs
  where 8 were fired.
- **A robot whose load after every FLOWER pick reaches the launcher makes two TIPs in 86 of 120
  runs**, close to 3 in 4, with nothing else changed. Ultraviolet as it ran at Belding: 56. Infrared: 9.
- **Then shot accuracy:** with 0.99 of the LEFT shots staying in (0.95 at Belding) it is 106 of 120.
- **The shot counter adds no TIPs but keeps them when the robot runs slow.** Drive time and
  accuracy from the start add nothing measurable. A turret was not needed.

## What the logs show

The launcher's wheels dip once for each ball, so every volley's count is in the match log
(`python3 tools/pull-logs/read_logs.py volleys LOG`). On the nine Auto volleys that were also
counted on video the dips were within one ball of the video, never under; the shot counter was
under on seven.

| Pieces fired by the volley after a pick (a lane holds 4) | Infrared | Ultraviolet |
|---|---|---|
| WALL FLOWER | 0, 2, 4, 2, 1 | 4, 4, 4, 4, 4, 4 |
| SCORING FLOWER | 0, 4, 3, 4, 2, 0, 4, 4 | 1, 4, 4 |

- R-Solo's two LEFT volleys together fired 0, 3, 4, 6, 6, 6 to 7 and 7 pieces: second TIP 0 of 7.
- L-NoCross fired 8 at the LEFT CELL in four runs and made the second TIP in three; in the fifth its
  second volley fired nothing.
- Every counted Auto shot was aimed within 0.8° by the pose, fired with the robot standing still,
  with the wheels at or above their target. A piece that reaches a raised CELL stays in (mentor); the
  logs do not show which shots went wide or short.
- The four measured rows of the speed table fit one arc (a pitch of about 65°) to within 1%. That
  arc puts the speed for the 36 in spots at about 1513 ticks/s, with 1458 to 1573 allowed by the fit;
  the table gave 1459 to 1480. So the 36 in speed was at the low edge, not clearly wrong, and Belding
  cannot say more: eight pieces never went up from there.

## The ranking

"Loads" is how many pieces of a pick reach the launcher; "stay in" is the share of launched pieces
that stay in the CELL.

| | Change | Two TIPs of 120 |
|---|---|---|
| | Infrared as at Belding | 9 |
| | Ultraviolet as at Belding | 56 |
| 1 | **Flower picking and feeding: every load is four** | **86** |
| 2 | **Shot accuracy from the LEFT spots: 0.99 stay in** (0.95), on Ultraviolet's loads | 68 |
| | the same on Infrared's loads | 13 |
| 3 | Shot accuracy from `RIGHT_START`: 0.97 (0.90), on Ultraviolet's loads | 56 |
| 4 | Drive time: 10% faster, on Ultraviolet's loads | 56 |
| 5 | **Shot counter:** a volley ends at its fourth shot, on Ultraviolet's loads | 56, the second TIP 0.8 s sooner |

| Together | Two TIPs of 120 |
|---|---|
| Every load four + LEFT 0.99 | 106 |
| Every load four + LEFT 0.99 + start 0.97 | 112 |

| Driving 28% slower, every load four | Two TIPs of 120 |
|---|---|
| As drawn | 0 |
| With the shot counter | 88 |

A row moves by about ±5 of 120 from one set of seeds to another.

- **Loads come first because a short load cannot be made up.** Eight pieces have to go up, and a
  robot fires at most eight at the LEFT CELL.
- **Accuracy comes second because all eight then have to stay in:** 0.95 a shot is all eight two
  times in three, 0.99 nine times in ten.
- **The first TIP is already there** 19 times in 20: three of the four preloads are enough.
- **Time is not what is missing** while the third volley runs. Master's guard skips it when the
  robot is late; the counter takes about a second off each volley after a pick. The second TIP lands
  at about 28.5 s, so a CELL that sits longer than Belding's did would push it past 30 s.

## What each would take

| | To build or tune | Already on the list as |
|---|---|---|
| **1. Loads (Infrared first)** | Find where Infrared's pieces go between the FLOWER and the launcher: film five WALL FLOWER picks and the drive and volley after each, from the side, and count pieces in the lane at each stage. The known causes: the SCORING pick stopping 4.6 in short (Q-4); two pieces wedging side by side at the intake (Q-1, Ultraviolet); a jam on the feed side with the lane full and the gate open (M-10, Infrared). | #507; fix list "stop two pieces wedging", "film a volley at 240 fps" |
| **2. LEFT accuracy** | Measure the speed-table rows from the LEFT spots themselves (they were measured on the right and assumed the same): hold Square at `LEFT_LAUNCH_CATCH` and `LEFT_LAUNCH_OFFSET`, trim, write the rows. Check the aim there with the turret locked (the right corner spot was 5° off). Reseat the launcher encoder cables. | `robot-todo` §7d; fix list "reseat both launcher encoder cables" (#496) |
| **5. Shot counter** | Fit the break-beam at the launcher throat and count shots from it. Until then, the wheel-dip count in `read_logs.py volleys` is the better count for reviews. | fix list "add a break-beam at the launcher throat"; `robot-todo` §7c |
| 3, 4. Start accuracy, drive time | Nothing worth doing for this Auto. | |

## The measurements that would confirm it

1. **Pieces fired after each pick, on each robot.** Ten WALL FLOWER picks and ten SCORING FLOWER
   picks, each followed by its drive and volley, filmed; `read_logs.py volleys` on the same logs.
   This replaces the Belding lists above (5 and 8 volleys for Infrared, 6 and 3 for Ultraviolet).
2. **Balls that stay in from `LEFT_LAUNCH_CATCH` and `LEFT_LAUNCH_OFFSET`**, from the same films:
   which go wide, which fall short. The calibration's 0.95 comes from 3 second TIPs in 4, and
   `LEFT_LAUNCH_OFFSET` has not been fired from on the left.
3. **How many landed POLLEN tip the LEFT CELL on the practice HIVE.** Place 6, 7, then 8 by hand,
   five times each.
4. **The time from the last ball to the TIP**, from the same films (the calibration: 3.6 to 4.2 s
   over its weight).
5. **Whether the third volley runs** in five runs of Drive: R-Two-Tip: the second volley's end time
   and the guard's line in the log.

## How it was run

```bash
BIOBUZZ_AUTO_CALIBRATION=belding BIOBUZZ_AUTO_STUDY="RTwoTipAuto@50" \
  BIOBUZZ_AUTO_DESIGNS="real robot, turret locked forward" BIOBUZZ_AUTO_RUNS=120 \
  BIOBUZZ_BELDING_ROBOT=full BIOBUZZ_BELDING_STAY_CATCH=0.99 \
  ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*' -i
```

The what-if knobs: `BIOBUZZ_BELDING_ROBOT` (`19429`, `20245`, `both` or `full`: whose loads),
`BIOBUZZ_BELDING_STAY_CATCH` / `_START` / `_CLOSE` (a launch range's share that stays in; the LEFT
spots follow `_CATCH`), `BIOBUZZ_BELDING_WHOLE_FEED=0` (the shot counter works), and the number
after `@` (drive speed, in/s).

**What the simulator does not know:** why a load comes up short (it only draws how many pieces
reach the launcher), how accuracy changes with battery or a piece's place in the volley, and
anything another robot does.
