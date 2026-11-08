# Belding Qualifier (USMIBEQ), 6–7 Nov 2026

Both robots' match logs from the event, kept for good. 20245 won the event (rank 1, 5–0, Inspire 2nd);
19429 was the finalist alliance (rank 14, 3–2, Inspire 3rd). Results: https://ftcscout.org/events/2026/USMIBEQ

This branch is an archive: never merged, never based on `master`. The files are the gzipped `.wpilog`s
exactly as `tools/pull-logs` sent them to the throwaway `logs/2026-11-0{6,7}-<robot>-RC` branches.
Read one with `python3 tools/pull-logs/read_logs.py summary <file>.wpilog.gz`, or gunzip it for AdvantageScope.

- `<team>/matches/`: one file per match, Auto and TeleOp in the same file.
- `<team>/practice/`: everything else from Friday afternoon and Saturday (practice field, queue re-inits, pit drives).
  Friday before noon (the night before, at home) stays on the `logs/` branches.

Start is the FTCScout match start, Eastern time. File names are the hub's clock at INIT: **19429's ran about
11 minutes fast** all day; 20245's was within a couple of minutes.

## 19429

| Match | Start | Alliance | Partner | Opponents | Result | Log |
|---|---|---|---|---|---|---|
| Q4 | 11:25 | Red | 7911 | 23410 / 26153 | W 38–25 | [`2026-11-07_11-36-28_R-Solo_11-05_8_09PM`](19429/matches/2026-11-07_11-36-28_R-Solo_11-05_8_09PM.wpilog.gz) |
| Q13 | 13:10 | Blue | 18975 | 5292 / 8733 | L 76–93 | [`2026-11-07_13-20-32_R-Solo_11-05_8_09PM`](19429/matches/2026-11-07_13-20-32_R-Solo_11-05_8_09PM.wpilog.gz) |
| Q19 | 13:50 | Blue | 17283 | 9133 / 15547 | W 64–24 | [`2026-11-07_14-01-52_R-Solo_11-05_8_09PM`](19429/matches/2026-11-07_14-01-52_R-Solo_11-05_8_09PM.wpilog.gz) |
| Q26 | 14:29 | Red | 20862 | 22368 / 31969 | L 64–65 | [`2026-11-07_14-41-09_R-Solo_11-05_8_09PM`](19429/matches/2026-11-07_14-41-09_R-Solo_11-05_8_09PM.wpilog.gz) |
| Q32 | 15:10 | Blue | 15397 | 5980 / 11531 | W 54–48 | [`2026-11-07_15-20-08_R-Solo_11-05_8_09PM`](19429/matches/2026-11-07_15-20-08_R-Solo_11-05_8_09PM.wpilog.gz) |
| Playoff M4 (Upper R2) | 16:28 | Red | 5292 | 9476 / 18092 | W 86–49 | [`2026-11-07_16-39-36_L-NoCross_11-05_8_03PM`](19429/matches/2026-11-07_16-39-36_L-NoCross_11-05_8_03PM.wpilog.gz) |
| Playoff M7 (Upper final) | 16:54 | Blue | 5292 | 20245 / 10104 | L 92–126 | [`2026-11-07_17-04-49_L-NoCross_11-05_8_03PM`](19429/matches/2026-11-07_17-04-49_L-NoCross_11-05_8_03PM.wpilog.gz) |
| Playoff M9 (Lower final) | 17:21 | Red | 5292 | 22368 / 31969 | W 82–57 | [`2026-11-07_17-32-26_L-NoCross_11-05_8_03PM`](19429/matches/2026-11-07_17-32-26_L-NoCross_11-05_8_03PM.wpilog.gz) |
| Playoff M10 (Final) | 17:41 | Blue | 5292 | 20245 / 10104 | L 98–109 | [`2026-11-07_17-52-07_L-NoCross_11-05_8_03PM`](19429/matches/2026-11-07_17-52-07_L-NoCross_11-05_8_03PM.wpilog.gz) |

9 match logs, 61 practice logs.

## 20245

| Match | Start | Alliance | Partner | Opponents | Result | Log |
|---|---|---|---|---|---|---|
| Q1 | 11:03 | Blue | 18975 | 16464 / 32190 | W 93–32 | [`2026-11-07_11-01-21_R-Solo_11-05_8_09PM`](20245/matches/2026-11-07_11-01-21_R-Solo_11-05_8_09PM.wpilog.gz) |
| Q7 | 11:57 | Red | 18092 | 9476 / 17283 | W 98–41 | [`2026-11-07_11-55-58_R-Solo_11-05_8_09PM`](20245/matches/2026-11-07_11-55-58_R-Solo_11-05_8_09PM.wpilog.gz) |
| Q14 | 13:20 | Blue | 9133 | 32975 / 31969 | W 83–40 | [`2026-11-07_13-19-36_L-NoCross_11-05_8_03PM`](20245/matches/2026-11-07_13-19-36_L-NoCross_11-05_8_03PM.wpilog.gz) |
| Q27 | 14:40 | Red | 15547 | 8733 / 20863 | W 76–26 | [`2026-11-07_14-39-52_R-Solo_11-05_8_09PM`](20245/matches/2026-11-07_14-39-52_R-Solo_11-05_8_09PM.wpilog.gz) |
| Q31 | 15:04 | Blue | 5292 | 23410 / 23411 | W 101–32 | [`2026-11-07_15-04-17_R-Solo_11-05_8_09PM`](20245/matches/2026-11-07_15-04-17_R-Solo_11-05_8_09PM.wpilog.gz) |
| Playoff M3 (Upper R2) | 16:23 | Red | 10104 | 22368 / 31969 | W 105–56 | [`2026-11-07_16-22-23_L-NoCross_11-05_8_03PM`](20245/matches/2026-11-07_16-22-23_L-NoCross_11-05_8_03PM.wpilog.gz) |
| Playoff M7 (Upper final) | 16:54 | Red | 10104 | 5292 / 19429 | W 126–92 | [`2026-11-07_16-53-50_L-NoCross_11-05_8_03PM`](20245/matches/2026-11-07_16-53-50_L-NoCross_11-05_8_03PM.wpilog.gz) |
| Playoff M10 (Final) | 17:41 | Red | 10104 | 5292 / 19429 | W 109–98 | [`2026-11-07_17-40-34_L-NoCross_11-05_8_03PM`](20245/matches/2026-11-07_17-40-34_L-NoCross_11-05_8_03PM.wpilog.gz) |

8 match logs, 50 practice logs.

## Notes

- 19429, 11:36:22 (hub): R-Solo was started with **no alliance set** (PLAY: UNKNOWN) and stopped a second later;
  Q4's real run began six seconds after. Check the DS shows RED/BLUE before PLAY.
- 20245 `14-36-14` and `16-22-16`: INIT then stop, no PLAY; harmless re-inits before Q27 and Playoff M3.
