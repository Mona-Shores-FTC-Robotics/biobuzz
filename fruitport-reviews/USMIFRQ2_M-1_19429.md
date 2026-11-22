# USMIFRQ2 M-1 (playoff), 19429 (Red, Alliance 4, with 8513): log against video

Log: `events/2026-11-fruitport:19429/matches/2026-11-21_16-28-50_L-NoCross_11-18_3_40PM.wpilog.gz` (Auto L-NoCross; INIT options: wait for TIP 1 until 9 s, if no TIP 1 = CROSS AND FIX IT; TeleOp Basic Drive). Final overlay: **76–76**, both red robots shown parked. The field camera is Field 1, rotated from Q-42: the red drivers are at the top left.

## Sync

- **Method:** our light, by eye. The helper's automatic sync was **wrong**. Its "light" at (908,288) is on the blue alliance's side. It put PLAY at 43.27 s, which would make our PLAY 4.7 s *before* the field's start: impossible.
- **Evidence:**
  1. Our robot (white and red, next to the far blue CELL at the start) shows red until 39.25 s and is dark at 39.50 s. It starts moving at 39.75 s (the log's path to LEFT_AUTO_HIVE starts at PLAY). So **Auto PLAY = 39.38 s** (±0.12).
  2. Green appears between PLAY + 1.1 and 1.3 s. The log's Lane: FULL is at 0.94 s (+0.1 expected), so the video is about 0.1 to 0.25 s late. It then stays green all Auto, as the log's FULL lasts to 32 s.
  3. The robot arrives at RIGHT_AUTO_FIX_TIP at about 12.8 s on video; the log's path end is 12.87 s.
  4. Field: the overlay switch is at 69.03 s, so switch − PLAY = 29.65 s. Our PLAY was about 0.8 s after the field's start, which fits a playoff.
- **TeleOp:** PLAY = 77.75 s from the log offset. Checked on the 20.77 s FULL (green on video at about 21.0 s) and on volley 1: balls leave at about 10.0, 10.5 and 10.95 s against logged shots at 9.98, 10.44 and 10.88 s.
- **Confidence:** good, about ±0.15 s.

## Auto

| Card (Auto s) | Log says | Video shows | Agree? |
|---|---|---|---|
| SpinUp + path to LEFT_AUTO_HIVE (0.0–0.8) | Arrives at (61.4, 120.5) | It moves a short way from its start by the far blue CELL | Yes |
| Wait for TIP or CELL up, with SpinUp (0.8–8.9) | No TIP; gives up at 9.0 s on the match clock. **Lane FULL the whole time** ("piece under the sensor 55.2 s", meaning a piece had sat under the sensor since INIT). | Sits still and green-lit. Red's tipped-CELL count stays at 0 all Auto, so **no TIP 1 came from the partner**. | Yes |
| Path under the HIVE to RIGHT_AUTO_FIX_TIP (8.9–12.9) | 4.01 s; ends at (55.3, 25.7) | Crosses under the HIVE to the other end and stops at about 12.8 s | Yes |
| RIGHT CELL up? | RightCellUp after 0.02 s | — | — |
| **LaunchAll (12.9–16.5)** | Gate open 3.5 s with the launcher at speed (L 1540, R 1520, no dip). **0 shots; lane 4 → 4.** Feeder and prefeed powered. | **Nothing leaves the robot** for the whole card (still `..._auto_nofire`) | Yes: the preloads never fired |
| Guard: no time for the next volley, parking now (16.5) | Path to the park starts at 16.5 s | The robot starts moving at about 17.3 s, 0.8 s after the logged start, then drives under the HIVE to behind the far red CELL | Roughly (late start on video) |
| Park (16.5–30) | Ends at (14.5, 117.4) at 22.0 s | From about 22 s it sits partly hidden behind the far red CELL and does not move again. Whether it was in the park zone: CAN'T TELL. | Yes (position) |

- Red's Auto score: 0 → 8 at 18 s, 11 at 24 s, 16 at 26 s. What scored it: CAN'T TELL. No red TIP in Auto.
- **The preloads were stuck.** In Auto the gate, feeder and prefeed all ran for 3.5 s and no ball reached the wheels. The lane sensor had a piece under it from INIT onward. In TeleOp the first Circle at 7.70 s fed nothing until the launcher reversed (unjam/eject, 8.21–8.60 s). Then 3 balls left at 9.98 to 10.88 s. So the Auto's 4 preloads were jammed at the gate or feeder, and a reverse cleared them. Only 3 left, so a 4th may have been ejected or still stuck: CAN'T TELL.

## TeleOp

| When (TeleOp s) | Log says | Video shows | Agree? |
|---|---|---|---|
| 0.91 Lane FULL (carried from Auto) | FULL until 8.21 s (cleared by the reverse) | not checked | — |
| Circle 5.52 / 7.70 / 8.64; reverse 8.21–8.60 | 5.52: spin-up. 7.70: "fired", but no shot. 8.64: blocked. | not checked | — |
| Volley 1, 9.98–10.88 | 3 shots, all "with the lane counted empty" | **3 balls** leave at about 10.0, 10.5 and 10.95 s, from by the near wall toward the red CELL (still `..._teleop_unjam`) | Yes (3) |
| 16.7–18.9, red WALL FLOWER | Reached; lane 0 → 1 (then 4 by 25.8) | not checked | — |
| 20.77 Lane FULL | FULL 20.77–22.59 | Green from about 21.0 s | Yes |
| Volley 2, 31.89–33.53 | 4 shots | Our robot is behind the blue CELLs at the far end. Count: CAN'T TELL. | CAN'T TELL |
| 35.0–37.9, red SCORING FLOWER | Reached; lane 0 → 4 | not checked | — |
| Volley 3, 43.00–44.43 | 4 shots | not counted | — |
| 52.9–68 | Repeated Circles blocked or "fired" with **no shots**. The launcher reverses at 57.1–60.6 and 65.3–67.9 ("lane: ejected"). Launcher speed falls to 0 to 360 in several feeds (158–168 log s). | not checked: a jam or eject period | — |
| 69.3–72.9, blue AUDIENCE FLOWER | Path done 1.6 in short; lane 1 → 3; two pushes at (86.7, 13–15) | not checked | — |
| Volley 4, 83.88–84.41 | 2 shots (lane had 3) | not counted | — |
| 86.4–103 | A BLUE NECTAR is rejected at the entrance, and "a reject did not clear it" twice (90.37 and 98.22 s). More reverses at 87.2–89.4, 94.2–97.2 and 102.5–103.7. Arc turn at 101.84 s. "not fed: unjam is held" at 103.28 s. **No shots after 84.4 s.** | not checked | — |
| Endgame | Drives until about 120 s; ends at (17.9, 100.4) | The overlay shows both red robots parked. Which check is ours: CAN'T TELL. | — |

- **Shots:** 13 logged in 4 volleys. Only volley 1 (3) was counted on video.
- **Pinpoint:** `FAULT_BAD_READ` twice (TeleOp 1.72 s and 48.65 s), each READY again within 0.05 s. Not lost.

## Log vs video disagreements

1. **Sync helper:** wrong light (on the blue side), so wrong PLAY (43.27 s against the real 39.38 s).
2. **Park path start:** the log starts the park path at 16.5 s, but the robot is seen moving only from about 17.3 s. Is that a slow start under the HIVE, or something holding the robot? CAN'T TELL. Every other event in Auto lines up within 0.1 to 0.2 s.
3. **TeleOp 7.70 s:** the log says "fired; gates clear", but no shot came until after the reverse at 8.2 to 8.6 s (log, consistent with the video).

## Notable

- **19429's 4 preloads never left in Auto: a jam, not a skipped volley.** The LaunchAll ran a full 3.5 s feed at speed with 0 dips. A piece sat under the lane sensor from INIT (55 s). A TeleOp reverse cleared it. Check the feed path or the gate on 19429 with 4 preloaded, sitting through a long INIT.
- The Auto's "if no TIP 1 = CROSS AND FIX IT" branch ran as drawn: no partner TIP by 9 s, so it crossed under the HIVE to RIGHT_AUTO_FIX_TIP. Because the preloads were stuck, the fix TIP never happened. The robot then parked at 16.5 s.
- TeleOp shows a lot of jamming or ejecting: about 30 of 120 s with reverses and blocked fires, and no shot after 84.4 s. A blue NECTAR stuck at the entrance is logged twice as "a reject did not clear it".
- The match ended 76–76.

## Stills

- `USMIFRQ2_M-1_19429_sync.jpg`: red in INIT (−0.2 s) and off at PLAY (+0.1 s), then green after the log's Lane FULL at 0.94 s (on at 1.3 s).
- `USMIFRQ2_M-1_19429_auto_nofire.jpg`: Auto 13.0 to 16.3 s at RIGHT_AUTO_FIX_TIP. The gate is open 3.5 s and no ball leaves. The log has 0 shots and the lane at 4 → 4.
- `USMIFRQ2_M-1_19429_teleop_unjam.jpg`: TeleOp 10.05, 10.55 and 11.05 s. After the reverse at 8.2 to 8.6 s, 3 balls leave. The log's shots are at 9.98, 10.44 and 10.88 s.
