# USMIFRQ2 Q-4, 20245 (Red), R-Solo, build master @ 4bddcf49d2

Log: events/2026-11-fruitport, 20245/matches/2026-11-21_11-12-37. Auto PLAY 191.246 s, TeleOp PLAY 229.181 s (+37.935). Times are seconds after that period's PLAY in the log unless marked "log". Alliance partner: 11531.

## Sync

- **Method:** set by hand, `--play-at 41.2` (Auto PLAY = 41.20 s into video.mp4). The helper's "our light" sync (45.20 s) is **wrong by 4.0 s**. With it, the preload balls appear before PLAY and the robot "starts" at the time of the path to WALL_FLOWER_STAGING. switch − PLAY was 25.63 s.
- **Evidence (two agreeing events):** (1) The light on our robot shows red until 41.2 s, then is off, then green from 1.2 to 1.6 s (log Lane FULL at 1.08 s, cleared 1.70 s). (2) The preload balls leave at 1.9, 2.4, 2.9 and 3.3 s (log shots 2.07, 2.57, 3.06, 3.51; video about 0.15 s ahead of the counter, as usual). Auto volley 2's balls are at 13.8, 14.2, 14.7 and 15.2 s (log 13.83, 14.23, 14.69, 15.13). TeleOp check: volley 1 balls rise at 16.9, 17.3, 17.8 and 18.2 s (log shots 17.07, 17.47, 17.99, 18.38).
- Scoreboard switch at 70.83 s, so switch − PLAY = 29.6 s. Our PLAY was about 0.8 s after the field's start.
- **Confidence:** high, ±0.15 s.

## Auto

| card (log start) | log says | video shows | agree? |
|---|---|---|---|
| SpinUp + path to RIGHT_AUTO_HIVE (0.0) | 0.84 s | Short move up from the start | yes |
| LaunchAll (0.8) | 4 counted, fed 2.21 s, ended at the 4th shot | **4 balls** (1.9, 2.4, 2.9, 3.3 s) into the RIGHT CELL | yes |
| (RIGHT CELL) | not logged as a TIP | CELL swings 4.5-6.0 s and is over and dumped by 6.5 s. **TIP from our preloads.** | n/a |
| path to WALL_FLOWER_STAGING (3.5), PickFlower red WALL (5.2) | stalled 1.4 in short, lane 0->3, Lane FULL | Robot leaves the HIVE at 4.1 s. The pick got 4 (next volley). | lane count 1 low |
| path BACKWARD to LEFT_AUTO_HIVE (8.9), Wait for TIP or CELL up (12.5) | 3.56 s drive, CellUp 0.02 s | Robot at the left HIVE end by 13 s | yes |
| LaunchAll (12.5) | 4 counted, "4 by dips", fed 2.19 s | **4 balls** (13.8, 14.2, 14.7, 15.2 s) into the LEFT CELL. No TIP in Auto. | yes |
| path to SCORING_FLOWER_STAGING (15.1), PickFlower red SCORING (16.4) | stalled **4.7 in short**, settled 5.8 in off the path end, lane 0->0 | Got nothing (next volley empty) | yes |
| LEFT CELL still up? (21.3), LaunchAll (21.4) | 0 shots, fed 3.52 s | **No ball leaves** 21.4-25.0 s | yes |
| path to RIGHT_PARK (25.0) | still at (22.9, 100.8) from 30.0 s | Partner 11531 already sits on the red park square. Our robot drives in and stops against it at 28 s, and sits beside it at 30-31 s. Whether it is inside the square: **CAN'T TELL** (the partner covers the square). | n/a |

Auto summary on video: 8 balls (4 + 4 + 0). One TIP (RIGHT CELL, preloads). WALL pick got 4 and the SCORING pick got 0. The last volley card spent 3.6 s feeding an empty robot.

## TeleOp

| event (TeleOp s) | log says | video shows | agree? |
|---|---|---|---|
| GARDEN gather, red corner (3.2) | stopped (sticks moved), push into the wall | Robot at the near corner | yes |
| Lane FULL (11.3, tracker 2) | 2 counted | 3 in the lane by 14.8 (log POLLEN 3 at 14.8). Green on the robot about 11.3 s: partly out of frame, CAN'T TELL. | n/a |
| volley 1 (17.1-18.4), at the LEFT HIVE end | 4 shots | **4 balls** (16.9, 17.3, 17.8, 18.2 s) into the LEFT CELL | yes |
| FLOWER pick red SCORING (19.5) | lane 0->3, stopped in DWELL | Robot at the left FLOWER | yes |
| volley 2 (28.8-30.1), right end | 4 shots | Balls rise from our robot 28.2-30.0 s, about 4. 11531 is beside it. Exact count CAN'T TELL. | likely |
| FLOWER pick blue AUDIENCE (34.4) | stopped in IN, lane 1 | n/a | n/a |
| volley 3 (41.1-42.4), right end | 4 shots, 3 of them "with the lane counted empty", lane had 1 | Balls in the air 40.9-42.6 s. At least 3 distinct, and the robot is next to 20862. Exact count CAN'T TELL. | count uncertain |
| LEFT CELL | n/a | Loaded at 45 s, **tipped by 50 s**. Our last LEFT shots were at 18.4 s, so not our volley directly. Who tipped it: CAN'T TELL. | n/a |
| volley 4 (52.3-53.7), from (85.8, 128.4) | 4 shots | Not checked frame by frame | n/a |
| GARDEN blue corner (56.0), push 2.38 s at (128.6, 127.0) | pinned against the wall with the stick down | n/a | n/a |
| Pinpoint FAULT_BAD_READ (64.9) | READY 12 ms later | n/a | n/a |
| volley 5 (79.2-80.1) | 3 shots | Not checked frame by frame | n/a |
| Lane FULL (86.2), eject (86.9), FULL again (88.1) "tracker counts 0" | eject reset the count. Then 5 shots, all "with the lane counted empty" | At least 4 balls leave our robot 88.5-90.9 s (right end, 11531 beside it). Count CAN'T TELL. | lane tracker wrong after the eject |
| volley 7 (95.5) | 1 shot | Not checked | n/a |
| RED_NECTAR ejected (104.0) | count reset | n/a | n/a |
| volley 8 (108.6-110.1), from (41.3, 120.8), LEFT end | 3 shots, last dip 158 | Not checked frame by frame | n/a |
| endgame park (111-121.5) | drives to the blue end (133.6, 129.9, jolt at 113.7) and back, still at (10.8, 123.9) from 121.5 | **Still driving at 0:00** (120.2-120.6 s). Stopped by 121.4 s on the left side wall next to 11531. Inside the square: CAN'T TELL. | see Notable |

## Log vs video disagreements

- **Helper sync wrong by 4.0 s** (45.20 vs 41.2). Check its light pixel (548,176) against the robot in other runs.
- **WALL FLOWER pick (5.2 s, log 196.5):** lane count 0->3, but 4 balls fired in Auto volley 2. Counter and dips both said 4, and that is right.
- **After the TeleOp eject (86.9 s, log 316.1):** the lane tracker reads 0. The next Lane FULL says "tracker counts 0", and all 5 shots of volley 6 are logged "with the lane counted empty". The tracker does not recover after an eject while pieces are still inside.

## Notable

- The Auto's SCORING FLOWER pick (16.4 s) got nothing: "stalled 4.7 in short", settled 5.8 in off the path end. The Auto then spent 3.6 s on an empty LaunchAll. Same shape as Q-2 19429's empty WALL pick.
- The Auto's RIGHT_PARK was already taken by partner 11531, so our robot parked against it. A pairing question for whoever chooses partner parks.
- **Late endgame park:** robot still moving at 0:00, stopped about 1 s after. Same pattern as Q-2 19429.
- TIP: RIGHT CELL by our preloads in Auto. The LEFT CELL held our 4 Auto + 4 TeleOp balls and tipped around 45-50 s.

## Stills

- `USMIFRQ2_Q-4_20245_auto_volley2.jpg`: Auto 13.8-15.2 s. Four balls of Auto volley 2 into the LEFT CELL, each beside its logged shot time.
- `USMIFRQ2_Q-4_20245_auto_park.jpg`: Auto 26-31 s. Our robot drives to RIGHT_PARK and stops against partner 11531, already on the red square.
- `USMIFRQ2_Q-4_20245_late_park.jpg`: TeleOp 119.8-121.4 s. Still driving after the clock reads 0:00, stopped by 121.4 s.
