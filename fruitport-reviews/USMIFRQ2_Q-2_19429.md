# USMIFRQ2 Q-2, 19429 (Red), R-Solo, build Fruitport @ 70881ea228

Log: events/2026-11-fruitport, 19429, Auto PLAY 587.364 s, TeleOp PLAY 625.317 s (+37.953). Times below are seconds after that period's PLAY in the log unless marked "log".

## Sync

- **Method:** set by hand, `--play-at 41.5` (Auto PLAY = 41.50 s into video.mp4). The helper's "our light" sync (42.45 s) is **wrong**. Its light pixel was at (256,292), not on our robot (bottom of the field, about 600,390). It measured green 0.15 s after "PLAY", but the log's first Lane FULL is 0.93 s after PLAY, and switch − PLAY came out 28.45 s.
- **Evidence (two agreeing events):** (1) The light on our robot goes from red to off at about 41.5 s, and green comes on about 1.0 s later (log Lane FULL 0.93 s). (2) The preload volley's balls leave at the logged shot times. The 4th ball is in the air at Auto 3.1 s (log shot 4 at 3.18 s). Also, robot positions at 1, 4, 5, 13, 23 and 30 s fall on the logged path when mapped onto the field image. TeleOp check: TeleOp volley 4 balls rise at about 87.7, 88.1 and 88.5 s (log shots 87.82, 88.26, 88.70).
- Scoreboard switch at 70.90 s, so switch − PLAY = 29.4 s. Our PLAY was about 1 s after the field's start.
- **Confidence:** high, ±0.2 s.

## Auto

| card (log start) | log says | video shows | agree? |
|---|---|---|---|
| SpinUp + path to RIGHT_AUTO_HIVE (0.0) | 1.58 s drive, gate opened on the approach | Robot drives up from the start | yes |
| LaunchAll (1.6) | 4 counted, fed 2.16 s, ended at the 4th shot | 4 balls, the last in the air at 3.1 s | yes |
| (RIGHT CELL) | not logged as a TIP | RIGHT CELL starts to swing about 5 s, over and dumped by 6.5 s. **TIP from our preloads.** | n/a |
| path to WALL_FLOWER_STAGING (3.2), PickFlower red WALL (4.9) | stalled 2.6 in short, lane 0->0 | Robot at the WALL FLOWER 5-9 s. No pieces fired afterwards (below). | yes (pick got 0) |
| path BACKWARD to LEFT_AUTO_HIVE (8.7) | 4.23 s, crosses the field | Robot drives the length of the field to the left HIVE end | yes |
| Wait for TIP or CELL up (12.9) | CellUp after 0.03 s | n/a | n/a |
| LaunchAll (13.0) | 0 counted, "1 by dips", fed 3.52 s | **No ball leaves** in 13.0-16.2 s (empty robot) | counter right, dips wrong |
| path to SCORING_FLOWER_STAGING (16.6), PickFlower red SCORING (18.1) | stalled 1.0 in short, lane 0->1, Lane FULL | Robot at the SCORING FLOWER 18-21 s. Got at least 3 (next volley). | lane count low |
| LEFT CELL still up? (23.0) | CellUp after 0.07 s | LEFT CELL up | yes |
| LaunchAll (23.1) | 2 counted, "3 by dips", fed 3.51 s | **3 balls** (about 23.9, 24.2, 24.6 s) into the LEFT CELL. No TIP in Auto. | counter undercounts by 1 (dips right) |
| path to RIGHT_PARK (26.7) | runs to 30 s, still at (18.1, 99.8) from 30.0 s | Driving 26.7-30 s, reaches the left end's red park square on the left side wall by 30 s | parked: yes, robot stopped at 30.0 s |

Auto summary on video: 7 balls fired (4 + 0 + 3). One TIP (the RIGHT CELL, from the preloads). WALL FLOWER pick got nothing, and the SCORING pick got 3.

## TeleOp

| event (TeleOp s) | log says | video shows | agree? |
|---|---|---|---|
| volley 1 (11.2) | 1 shot. Second Circle at 12.3 fired nothing. | One ball toward the LEFT CELL at about 11.0 s. Robot partly out of the crop: exact count CAN'T TELL. | likely |
| volley 2 (35.1-35.6) | 2 shots | Balls at about 35.0 and 36.0 s. Exact count CAN'T TELL at this resolution. | likely |
| GARDEN gathers (42.3, 55.0) | stopped (sticks moved), RED_NECTAR rejected at 49.8 | Robot at the blue GARDEN corner 42-60 s | yes |
| Pinpoint FAULT_BAD_READ (57.5, 73.5) | READY again within 35 ms both times | Nothing visible | n/a |
| volley 3 (66.6) | 1 shot, arc turn first | One ball at about 66.4 s. Partner robot right beside ours. | likely |
| volley 4 (87.8-88.7) | 3 shots, from (45, 117) | **3 balls** into the LEFT CELL (87.7, 88.1, 88.5 s). **LEFT CELL tips** 90.5-92.5 s (TIP lag about 2-3 s). | yes |
| FLOWER picks blue AUDIENCE (99.2, 100.9) | both stopped by the driver in LINE_UP/IN | Robot at the audience wall | yes |
| Lane FULL (105.8) | tracker 4 | Robot's light green at 106 s | yes |
| volley 5 (110.2-110.8) | 2 shots. Circles at 111.9 and 113.0 fed nothing. Lane ejected at 113.3. | At least 3 balls in the air 110.0-111.1 s, likely 4 (lane was full with 4). Exact count CAN'T TELL. | counter likely undercounts |
| endgame park (119-122) | drives from the audience wall to (13.1, 113.2), still by 122 s | **Still driving at 0:00** (120.2 s). Stops on the red park square at about 121.8 s. | see Notable |

No contact problems, tipping of the robot, or stuck periods seen.

## Log vs video disagreements

- **Helper sync wrong by 0.95 s** (42.45 vs 41.5): its light pixel was not on our robot.
- **Auto volley 2 (13.0-16.6, log 600.3-603.9):** "1 ball by dips" but no ball leaves on video. The counter's 0 is right. The WALL FLOWER pick (4.9 s) got nothing.
- **Auto volley 3 (23.1-26.7, log 610.5-614.1):** counter 2, video 3, dips 3.
- **Auto SCORING FLOWER pick (18.1 s, log 605.5):** lane count 0->1, but it fed 3 balls in the next volley.
- **TeleOp volley 5 (110.2, log 735.5):** counter 2. Video shows at least 3 balls, likely 4.

## Notable

- The Auto's R-Solo crosses the whole field (4.2 s drive) after a WALL FLOWER pick that got nothing, and then fires an empty volley (3.5 s of feed, 13.0-16.6 s). That is about 8 s of Auto spent on 0 points. The pick's push ended 2.6 in short, which looks like a stall.
- TIPs by our robot: RIGHT CELL in Auto (preloads), LEFT CELL in TeleOp (volley 4).
- **Late endgame park:** the field clock reads 0:00 while the robot is still mid-field (120.2 s). It reaches the park square about 1.5 s after. Whether it counted as parked is the referee's call; on video it was not in the square at 0:00.
- Pinpoint FAULT_BAD_READ twice (57.5, 73.5 TeleOp). Each recovered in about 35 ms with no visible effect.

## Stills

- `USMIFRQ2_Q-2_19429_auto_tip.jpg`: Auto 3.1-6.5 s. The 4th preload ball in the air (log shot 3.18 s), then the RIGHT CELL swings and dumps.
- `USMIFRQ2_Q-2_19429_teleop_left_tip.jpg`: TeleOp 88.4-92.5 s. Volley 4 (log 3 shots, 87.8-88.7) goes in, and the LEFT CELL tips.
- `USMIFRQ2_Q-2_19429_late_park.jpg`: TeleOp 119.4-121.8 s. The robot is still driving to the park square after the clock reads 0:00.
