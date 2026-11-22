# USMIFRQ2 M-7, 20245 (Red, Alliance 1 with 10255), playoff

Log: `events/2026-11-fruitport:20245/matches/2026-11-21_16-58-32_R-NoCross_11-18_9_06PM.wpilog.gz`.
Build Fruitport @ 7edec46c31, Auto R-NoCross (red). Battery 13.54 V at INIT.
`A` = seconds after Auto PLAY (log 129.41 s). `T` = seconds after TeleOp PLAY (log 167.48 s).

## Sync

- **Method:** our light, found by the helper. Auto PLAY is 38.95 s into video.mp4. Scoreboard switch minus PLAY is 29.92 s, so our PLAY was about 0.5 s after the field's start.
- **Evidence 1:** the first green measured 1.23–1.33 s after PLAY. The log says 1.12–1.30.
- **Evidence 2 (Auto greens):** green on until A13.0, off at A13.1, back on at A14.3. The log has FULL off at A12.96 and on at A14.06, so the video runs about 0.1–0.2 s behind the log, as expected.
- **Evidence 3 (volley 1):** balls reach the CELL at about A2.1, 2.5, 2.8 and 3.1. The counter has them at A1.91, 2.31, 2.79 and 3.29.
- **TeleOp check:** green appears at T8.6 (log FULL at T8.43) and goes off between T10.2 and T10.4 (log T10.23).
- **Confidence:** high, about ±0.2 s.

## Auto

| Card (A s) | Log says | Video shows | Agree? |
|---|---|---|---|
| SpinUp + path to RIGHT_AUTO_HIVE (0.0–0.8) | (55.6, 7.7) → (59.7, 21.5) | Drives up to the near red end. | yes |
| LaunchAll 1 (0.8–3.3) | Ended at the 4th counted shot after 2.38 s. Counter 4. Lane 4→0. | **4 balls** go into the near red CELL (A2.1–3.4). **The CELL tips:** it swings from about A6.0 and has settled by A7.5–8.0. Red goes 0 → 20 between A6 and A8. TIP lag (last ball leaves about A3.3, CELL settled) is about 4.2–4.7 s. | yes |
| path to WALL_FLOWER_STAGING (3.3–4.8) | — | Drives to the side wall. | yes |
| PickFlower (4.8–7.9) | Picked the red WALL FLOWER and stalled 1.3 in short. Lane 0→2, Lane FULL from A7.31. | Green light at A8–10. Pieces got: **4**, judged by the 4 fired in volley 2 with no other intake. The lane count read low (2, then 3 at A13.0). | count is low |
| path to RIGHT_AUTO_HIVE (8.0–9.5) | — | Back at the near end. | yes |
| Wait for TIP or CELL up (9.5–17.9) | CellUp after 8.38 s. RED_AUDIENCE was read UP at A17.89. | The partner's TIP at the far red CELL swings from about A14 and settles about A16.5–17.0. That swing turns our near CELL back up, and red reaches 40 by A19. | yes. The read came about 1 s after the visible settle. |
| LaunchAll 2 (17.9–20.1) | Ended at the 4th counted shot after 2.14 s. Counter 4, dips 4. | **4 balls** leave (A18.75, 19.25, 19.75, 20.15) toward the near red CELL. | yes |
| Guard → park (20.1–) | "no time to park after it" (the next volley plus the park would end at 33.6 s), so it parks from the spot it is on. Ends at (14.4, 99.1, 0°) = RIGHT_PARK by about A25. | From about A23.5 the robot sits still in the top-left corner under the red end of the HIVE until A29.6. Whether it scored PARK: CAN'T TELL from the scoreboard. | position yes |

Score at the end of Auto (A29.6): red 56, blue 36.

## TeleOp

Counted shots by volley (log), against video where it was checked:

| When (T s) | Log says | Video shows | Agree? |
|---|---|---|---|
| T8.43–10.23 | Lane FULL | Green T8.6–10.2 (the robot is on the left side, near the wall). | yes |
| T9.0 Circle | Gate open 0.8 s, 0 shots (the gates first blocked on speed and aim) | Not checked. | — |
| T13.8 | 3 counted at T15.0–16.0 from (50.5, 121.6), 41 in by camera | Not counted on video. | — |
| T22.6 | Counter 1, 2 dips (T23.6, T24.4) | Not counted. | — |
| T32.8 | 4 counted (T33.3–34.9), 39–40 in | Not counted. | — |
| T49.3 | Square to LEFT_CLOSE_HIVE, 4 counted (T51.4–52.8) | Not counted. | — |
| T61.6 | 4 counted (T62.9–64.3) from (55.6, 11.8), 49 in | **4 balls** seen rising from the robot to the near red CELL (T63.0–64.2). | yes |
| T69.7 | Square to LEFT_CLOSE_FAR, 2 counted | Not counted. | — |
| T83.1 | 4 counted (T84.4–85.8) | Not counted. | — |
| T94.7 | Counter 1, 2 dips | Not counted. | — |
| T103.5 | 3 counted | Not counted. | — |
| T112.3 | 1 counted | Not counted. | — |
| T120.0 Circle | At T121.5, "outside the firing arc, fed anyway: the arc is not enforced on this robot". Gate 0.6 s, 0 shots. | The clock reads 0:00 at T120.3. From T120.2 to T121.6 the robot sits in the bottom-left corner beside its partner, and no ball is seen leaving. | yes, no shot |

- `Pinpoint: FAULT_BAD_READ` at T41.5, T52.8 and T71.4. PinpointTrust stayed TRUSTED all match, so these were blips, not a lost pose.
- At the end, the pose is still moving at T118–121.5: (20.1, 8.1) → (33.3, 17.4). Whether it parked: CAN'T TELL.

## Log vs video disagreements

- **A4.8–13.0, the FLOWER pick:** the lane count says 2 (then 3). Volley 2 fired 4 on video and the counter says 4, so the lane count ran low, as is known.
- No other disagreements where the video could see.

## Notable

- This was a clean Auto: 4 + 4 into the near CELL, our TIP at about A8, the partner's TIP at about A17, and a park. Score 56–36 after Auto.
- **A Circle press 1.2 s after the TeleOp buzzer fed the gate**, because the arc is not enforced on 20245. Nothing left the robot this time, but a press at the buzzer can still feed after time. The drive team should know.

## Stills

- `USMIFRQ2_M-7_20245_auto_volley1_tip.jpg`: Auto volley 1. A ball enters at A2.5, the 4th is rising at A3.4, the CELL swings at A6.5 and has settled by A8.0.
- `USMIFRQ2_M-7_20245_auto_volley2.jpg`: Auto volley 2 after CellUp at A17.9. 4 balls leave at A18.75, 19.25, 19.75 and 20.15 (log 4).
