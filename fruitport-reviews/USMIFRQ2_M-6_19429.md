# USMIFRQ2 M-6, 19429 (Blue, Alliance 4 with 8513), playoff

Log: `events/2026-11-fruitport:19429/matches/2026-11-21_16-59-40_R-Solo_11-19_10_35PM.wpilog.gz`.
Build Fruitport @ 7edec46c31, Auto R-Solo (blue run). Battery 13.27 V at INIT.
Times are seconds after our PLAY: `A` = Auto PLAY (log 183.34 s), `T` = TeleOp PLAY (log 221.53 s).
The score at the TeleOp buzzer frame was red 116, blue 96 (we lost).

## Sync

- **Method:** set by eye with `--play-at 39.18` (Auto PLAY = 39.18 s into video.mp4). The helper could not find the light by itself
  (the robot starts at the back of the field, partly behind the red HIVE end). The first guess from the scoreboard (38.45 s) was 0.73 s early.
- **Evidence 1 (our light):** a crop of the robot at the blue start shows the blue light going out at 39.18 s in the file.
  Scoreboard switch 68.90 s, so switch minus PLAY = 29.72 s: our PLAY was about 0.7 s after the field's start, which is normal for a playoff.
- **Evidence 2 (Auto volley 2):** balls leave at about A18.1, A18.9 and A19.5 on video. The counter has them at A18.24, 18.70 and 19.24, so the video is within about 0.1 to 0.3 s.
- **Evidence 3 (TeleOp light):** green on video until T18.5, changing at T18.6. The log's Lane FULL ends at T18.54. This checks the TeleOp offset.
- **Confidence:** good, to about ±0.2 s.

## Auto

| Card (A s) | Log says | Video shows | Agree? |
|---|---|---|---|
| SpinUp + path to RIGHT_AUTO_HIVE (0.0–1.4) | Drives up from the blue right start. Gate opens on the approach, 0.10 s ahead of launch speed. | Drives up behind the red CELL end of the HIVE. | yes |
| LaunchAll 1 (1.4–4.6) | Fed for the full 3.52 s and did not end at a 4th shot. Counter 2 (A3.10, A4.20), dips "?". Lane 4→2. Battery dipped to 10.86 V. | 2 balls seen rising to the blue CELL (A3.0–3.4 and A4.2–4.4). The robot is half hidden behind the red CELL, so a third is CAN'T TELL, but the lane count still read 2 afterward. | yes: 2 out, 2 stayed in |
| path to WALL_FLOWER_STAGING (4.6–6.4) | — | The robot leaves the HIVE. | yes |
| PickFlower (6.4–9.8) | Picked the blue WALL FLOWER and stalled 0.8 in short. Lane 2→3, Lane FULL from A8.92. | Not checked closely. | — |
| path BACKWARD to LEFT_AUTO_HIVE (9.8–13.5), Wait for TIP or CELL up (13.5–14.3) | Crossed to the left end. The wait timed out ("750 ms passed"), meaning the CELL was not seen up. | Not checked closely. | — |
| path to RIGHT_AUTO_HIVE (14.3–17.2), RIGHT CELL still up? (17.2–17.4) | CellUp after 0.10 s. | The robot is back under the near end. | yes |
| LaunchAll 2 (17.4–20.9) | Counter 3 (A18.24/18.70/19.24), dips 1. Lane 3→0. | 3 balls go into the blue CELL, which already held 2. The CELL starts to swing at about A20.6 and has tipped by A22.4. The log reads BLUE_SCORING DOWN at A22.55. TIP lag is about 3 s (last ball leaves about A19.5, CELL settled about A22.4). | counter yes, dips undercount (1 vs 3) |
| path to RIGHT_PARK (20.9–) | Ends at (127.0, 42.4, 180°), still from about A25. | At A29.6 the robot is still at the right side wall beside partner 8513, green light on. Whether it scored PARK: CAN'T TELL (the scoreboard does not show it per robot). | position yes |

Score at the end of Auto (A29.6): 28–28.

## TeleOp

| When (T s) | Log says | Video shows | Agree? |
|---|---|---|---|
| T0.93 | Lane FULL (green) | Green light seen. | yes |
| T14.7–18.8 | Driver's Circle. Gate open 3.6 s with 1 piece in the lane. **The launcher wheels fell from 1540 to about 260 ticks/s at T18.6**, the lane went 1→0, and 0 shots were counted. | At T18.6 the light changes from green. No ball arc is seen. At T19.0 two pieces are lying beside the robot that were not there at T18.9. | no shot, matches. What stalled the wheels: CAN'T TELL from outside. |
| T27.98–29.05 | 3 shots counted (target 1526, 48 in by camera) | Not counted on video. | — |
| T38.77 | 1 shot. Then at about T39.1 the wheels dropped to 80–120 ticks/s during a 0.3 s feed. | Not checked. | — |
| T60.0–61.2 | 4 shots counted (1516, 49–50 in by camera) | 4 balls seen rising to the blue CELL by the scoreboard (T59.9–61.2). | yes |
| T71.1–72.0 | 3 counted | Not counted on video. | — |
| T89.7–90.6 | 3 counted | Not counted on video. | — |
| T99.2–99.7 | 2 counted (one dip only 10 ticks/s) | Not counted on video. | — |
| T116.7–117.1 | 2 counted | Not counted on video. | — |
| Endgame | The pose is still moving at T119.5–121: (91.7, 117.6) → (111.2, 92.6). | The clock reads 0:00 at T120.5. Where our robot is and whether it parked: CAN'T TELL. | — |

There were no `Pinpoint` faults in this log. Counted TeleOp shots: 18.

## Log vs video disagreements

- **A17.4–20.9, Auto volley 2:** the wheel-dip count says 1 ball. The video shows 3 leaving and the counter says 3. The dip count is wrong here (still `..._auto_volley2_tip`).
- No disagreement where the video could see. Volley 1's "only 2 out" is consistent between the counter, the lane count and the video.

## Notable

- **Auto volley 1 left 2 of the 4 preloads in the robot.** The feed ran its full 3.5 s without reaching a 4th shot. That came after a slow spin-up (at speed after 1610 ms) and a sag to 10.86 V. The two pieces were fired in volley 2.
- **The launcher wheels collapsed twice during TeleOp feeds:** at T18.6 (to about 260 ticks/s, 0 shots, during a 3.6 s feed) and at about T39.1 (to about 80). This looks like a piece stalling the wheels. It is worth checking whether 19429's feed jams.
- The left-end trip at A9.8–17.2 (7.4 s) found no CELL up and came back. The right-end volley then tipped the CELL with 3 balls.

## Stills

- `USMIFRQ2_M-6_19429_auto_volley2_tip.jpg`: Auto volley 2. Balls go into the blue CELL at A18.1 and A18.9, the CELL starts to swing at A20.6, and it has tipped by A22.4 (the log reads DOWN at A22.55).
- `USMIFRQ2_M-6_19429_teleop_wheel_stall.jpg`: T18.4–19.0. The Lane FULL green ends at T18.6 as the wheels fall from 1540 to 260 ticks/s with no shot. Two pieces appear beside the robot at T19.0.
