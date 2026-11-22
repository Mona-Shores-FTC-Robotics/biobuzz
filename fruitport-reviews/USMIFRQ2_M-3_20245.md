# USMIFRQ2 M-3 (playoff), 20245 (Red, Alliance 1, with 10255): log against video

Log: `events/2026-11-fruitport:20245/matches/2026-11-21_16-34-53_R-NoCross_11-18_9_06PM.wpilog.gz` (Auto R-NoCross; INIT options: wait for TIP 2 until 20 s, if no TIP 2 = WAIT FOR PARTNER; TeleOp Basic Drive). Final overlay: **Red 196, Blue 71**. Both red robots shown parked; blue has one park ✗.

## Sync

- **Method:** our light, by eye. The helper found no green in the first 3 s (the first Auto FULL is at 8.29 s), so it fell back to "scoreboard only" (38.52 s). Rerun with `--play-at 38.90`.
- **Evidence:**
  1. Our robot (orange FLOWER stick, blue turret, at the near wall next to 11531's green robot) shows red until 38.75 s and is dark at 39.00 s. It moves at 39.25 s. So **Auto PLAY = 38.90 s**.
  2. The four volley-1 balls leave at about PLAY + 1.7, 2.1, 2.6 and 3.05 s. The log counts shots at 1.73, 2.10, 2.62 and 3.06 s.
  3. Field: the overlay switch is at 68.97 s, so switch − PLAY = 30.07 s. Our PLAY was about 0.4 s after the field's start.
- **TeleOp:** PLAY = 77.06 s from the log offset. Checked on the 11.37 s FULL: green on video at about 11.64 to 11.94 s, against the log's 11.37 to 11.84 s plus 0.1. Checked again on volley 2: balls near the robot at about 12.4, 12.8, 13.2 and 13.7 s against logged shots at 12.37, 12.78, 13.21 and 13.65 s.
- **Confidence:** high, about ±0.1 s (TeleOp ±0.15 s).

## Auto

| Card (Auto s) | Log says | Video shows | Agree? |
|---|---|---|---|
| SpinUp + path to RIGHT_AUTO_HIVE (0.0–0.75) | Arrives at (59.2, 20.9) | It moves forward from the near wall at once | Yes |
| LaunchAll (0.7–3.1) | 4 shots counted (1.73, 2.10, 2.62, 3.06) | **4 balls** into the near red CELL (still `..._sync_volley1`) | Yes |
| (CELL) | HIVE RED UNSEEN from 4.0 s | Overlay: red 0 → 20 and the CELL icon 0 → 1 at about 7 to 8 s. The near red CELL is seen tipping at about 7 s and stays tipped to the end of Auto (still `..._cell_wait`). TIP lag about 4 s from the last ball. Most likely our TIP: our 4 balls were in that CELL, and 10255 was not tracked. | — |
| Path to WALL_FLOWER_STAGING + PickFlower red WALL (3.1–8.3) | Stick down on the drive in. Stalled 1.5 in short. **Lane 0 → 2**, then 3 at 12.6 s. FULL 8.29 s. | Robot goes to the red side-wall FLOWER. Pieces taken: CAN'T TELL (too small). | — |
| Path to RIGHT_AUTO_HIVE (8.3–9.9) | 1.58 s | It returns near the start, green-lit | Yes |
| Wait for TIP or CELL up (9.9–19.8) | Times out at 20.0 s on the match clock. **HIVE RED UNSEEN all through** (4.0–24.4 s). | The near red CELL stays tipped and the robot sits still (green from the FULL at 13.59 s). Red's CELL count stays 1, so no second red TIP. | Yes |
| RIGHT CELL up by 23 s? (19.8–22.8) | Times out | The CELL is still tipped | Yes |
| Path to RIGHT_PARK (22.8–30) | Ends at (14.5, 98.7) at 25.85 s; **3 pieces still in the lane** | It drives to by the far red CELL near the red drivers' wall, green-lit, about 25 to 27 s. In the park zone: CAN'T TELL. | Yes (position) |

- The second load (3 pieces, by the lane count) never fired. That is what R-NoCross's "WAIT FOR PARTNER" does when nothing brings the RIGHT CELL back up. The near red CELL our volley tipped stayed tipped, so the Auto had no up CELL to shoot at, and it parked.
- Red's Auto score: 20 at 7 to 8 s, 23 at 27 s, 36 at 29 s. What made the +3 and +13: CAN'T TELL.

## TeleOp

| When (TeleOp s) | Log says | Video shows | Agree? |
|---|---|---|---|
| 0.90 Lane FULL (carried from Auto) | Cleared at 3.17 s | not checked | — |
| Volley 1 (Circle), 3.55–4.88 | 4 shots, all "with the lane counted empty" (the 3 Auto leftovers plus 1) | Our robot is hidden behind the blue CELLs at the far end. Balls are seen rising over the far red CELL at about 3.5 to 4.8 s; exact count: CAN'T TELL. | CAN'T TELL (count) |
| 11.37 Lane FULL | 11.37–11.84 | Green about 11.64–11.94 s | Yes |
| Volley 2, 12.37–13.65 | 4 shots | **About 4 balls** at about 12.4, 12.8, 13.2 and 13.7 s, from by the near wall (moderate confidence: small in frame) | Yes |
| 15.9–17.0 | RED NECTAR in, then "ejected" | not checked | — |
| Volleys 3–10 (27.9, 41.5, 48.5, 56.8, 71.9, 93.0, 105.0, 115.9 s) | 3, 3, 1, 4, 4, 4, 4, 3 shots | not counted (time) | — |
| 32.1–35.3, 74–81 | Picks at the blue WALL and blue AUDIENCE FLOWERs (lane 0 → 1, 0 → 3) | not checked | — |
| 87.9, 91.2 | "lane: ejected" twice | not checked | — |
| **After 120 s** | Shot 35 at **121.38 s**. Circle "fired" again at **127.46 s**. The log runs to 128.45 s. | The overlay clock is 0:00 from TeleOp 119.8 s. The robot rotates at about 120.6 to 121.2 s, and a ball rises near the far red CELL at about 121.5 s. That it is ours is likely, but CAN'T TELL for certain. The 127.46 s press: not checked. | Likely: a shot after the buzzer |
| Endgame | Ends at (87.2, 124.7), on the **blue** half, far end | The robot is by the far blue CELLs at 0:00. The overlay shows both red robots parked. | — |

- **Shots:** 35 logged in 11 volleys. Volley 2 (about 4) was counted on video; volley 1 is hidden.
- **Pinpoint:** `FAULT_BAD_READ` once (TeleOp 79.40 s), READY 0.05 s later. Not lost.
- A GARDEN gather was refused at TeleOp 127.23 s ("no field corner within 45 in"), after the buzzer.

## Log vs video disagreements

1. **Sync helper:** "scoreboard only" at 38.52 s. The real PLAY is 38.90 s (light and four shots).
2. **The robot's Auto ran its full 30 s while the field's Auto ended 0.4 s earlier.** The robot was parking (path end 25.85 s), so the overlap did nothing.
3. None found between the log and the video in Auto beyond the helper's sync. The log's CELL state (RIGHT not up to 23 s) matches the tipped CELL on video.

## Notable

- The Auto did what R-NoCross is drawn to do: a 4-ball volley, a TIP (overlay +20 at about 7 s), a WALL FLOWER pick, then a wait for a partner TIP that never came, then a park. **It carried 3 pieces into the park unfired.** If our own volley tipping the near CELL is the usual case, the wait for TIP 2 can never pay off without the partner's help. Is that the plan the drive team wants? A mentor question for the INIT choice: WAIT FOR PARTNER or CROSS AND FIX IT.
- **The TeleOp kept firing after the buzzer** (shot at 121.38 s, Circle at 127.46 s), as in Q-42 (20245). The Basic Drive TeleOp is not stopped at 120 s, and nothing blocks a feed after it.
- The robot ended TeleOp on the blue half (87, 125). The overlay still shows both red parks checked.

## Stills

- `USMIFRQ2_M-3_20245_sync_volley1.jpg`: red in INIT (−0.15 s) and off at PLAY (+0.15 s), then the four volley-1 balls at about 1.85, 2.25, 2.75 and 3.2 s. The log's shots are at 1.73, 2.10, 2.62 and 3.06 s.
- `USMIFRQ2_M-3_20245_cell_wait.jpg`: Auto 3, 7, 15 and 22 s. At 3 s the near red CELL is up and holds our balls. It tips at about 7 s and stays down while the robot waits (Wait for TIP or CELL up times out at 20 s, RIGHT CELL up by 23 s times out), then the robot parks.
