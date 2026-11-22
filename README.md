# Fruitport Qualifier (USMIFRQ2), 21 Nov 2026

Both robots' match logs from the event, kept for good. 20245 won the event on 10255's alliance (rank 4, 4–1,
Think 1st); 19429 was rank 14 (3–2, Think 2nd). Results: https://ftcscout.org/events/2026/USMIFRQ2

This branch is an archive: never merged, never based on `master`. The files are the gzipped `.wpilog`s
exactly as `tools/pull-logs` sent them to the throwaway `logs/2026-11-21-<robot>-RC` branches.
Read one with `python3 tools/pull-logs/read_logs.py summary <file>.wpilog.gz`, or gunzip it for AdvantageScope.

- `<team>/matches/`: one file per match, Auto and TeleOp in the same file.
- `<team>/practice/`: everything else from the day (practice field, queue re-inits, pit drives).

Start is the FTCScout match start, Eastern time. File names are the hub's clock at INIT: **19429's ran about
8 minutes fast** all day; 20245's was within about 25 s. Each match log was matched by its PLAY time
(INIT + the log's PLAY offset, less the hub's offset) against the FTCScout start, within 25 s, and by alliance.
Playoffs are named by FTCScout's playoff match number.

## 19429

| Match | Start | Alliance | Partner | Opponents | Result | Log |
|---|---|---|---|---|---|---|
| Q2 | 11:03 | Red | 20863 | 16204 / 5384 | L 61–63 | [`2026-11-21_11-01-37_R-Solo_11-19_10_35PM`](19429/matches/2026-11-21_11-01-37_R-Solo_11-19_10_35PM.wpilog.gz) |
| Q17 | 13:19 | Red | 20862 | 10031 / 38293 | W 89–51 | [`2026-11-21_13-23-03_R-Solo_11-19_10_35PM`](19429/matches/2026-11-21_13-23-03_R-Solo_11-19_10_35PM.wpilog.gz) |
| Q23 | 13:48 | Blue | 15510 | 8932 / 18092 | W 75–47 | [`2026-11-21_13-53-40_R-Solo_11-19_10_35PM`](19429/matches/2026-11-21_13-53-40_R-Solo_11-19_10_35PM.wpilog.gz) |
| Q28 | 14:13 | Blue | 22313 | 20245 / 11266 | W 107–87 | [`2026-11-21_14-20-15_R-Solo_11-19_10_35PM`](19429/matches/2026-11-21_14-20-15_R-Solo_11-19_10_35PM.wpilog.gz) |
| Q39 | 15:05 | Red | 14933 | 13702 / 11940 | L 81–106 | [`2026-11-21_15-10-02_R-Solo_11-19_10_35PM`](19429/matches/2026-11-21_15-10-02_R-Solo_11-19_10_35PM.wpilog.gz) |
| Playoff M1 | 16:21 | Red | 8513 | 11531 / 24371 | L 86–93 | [`2026-11-21_16-28-50_L-NoCross_11-18_3_40PM`](19429/matches/2026-11-21_16-28-50_L-NoCross_11-18_3_40PM.wpilog.gz) |
| Playoff M6 | 16:54 | Blue | 8513 | 13702 / 11228 | L 104–117 | [`2026-11-21_16-59-40_R-Solo_11-19_10_35PM`](19429/matches/2026-11-21_16-59-40_R-Solo_11-19_10_35PM.wpilog.gz) |

7 match logs, 27 practice logs.

## 20245

| Match | Start | Alliance | Partner | Opponents | Result | Log |
|---|---|---|---|---|---|---|
| Q4 | 11:15 | Red | 11531 | 24370 / 20862 | W 126–19 | [`2026-11-21_11-12-37_R-Solo_11-19_10_35PM`](20245/matches/2026-11-21_11-12-37_R-Solo_11-19_10_35PM.wpilog.gz) |
| Q11 | 11:58 | Blue | 6482 | 11940 / 8932 | W 135–59 | [`2026-11-21_11-58-05_R-Solo_11-19_10_35PM`](20245/matches/2026-11-21_11-58-05_R-Solo_11-19_10_35PM.wpilog.gz) |
| Q20 | 13:34 | Blue | 5383 | 22368 / 24371 | W 118–78 | [`2026-11-21_13-34-34_R-Solo_11-19_10_35PM`](20245/matches/2026-11-21_13-34-34_R-Solo_11-19_10_35PM.wpilog.gz) |
| Q28 | 14:13 | Red | 11266 | 19429 / 22313 | L 87–107 | [`2026-11-21_14-13-02_R-Solo_11-19_10_35PM`](20245/matches/2026-11-21_14-13-02_R-Solo_11-19_10_35PM.wpilog.gz) |
| Q42 | 15:20 | Red | 16220 | 5384 / 10031 | W 111–67 | [`2026-11-21_15-20-25_R-Solo_11-19_10_35PM`](20245/matches/2026-11-21_15-20-25_R-Solo_11-19_10_35PM.wpilog.gz) |
| Playoff M3 | 16:35 | Red | 10255 | 11531 / 24371 | W 219–91 | [`2026-11-21_16-34-53_R-NoCross_11-18_9_06PM`](20245/matches/2026-11-21_16-34-53_R-NoCross_11-18_9_06PM.wpilog.gz) |
| Playoff M7 | 17:00 | Red | 10255 | 31969 / 6482 | W 224–93 | [`2026-11-21_16-58-32_R-NoCross_11-18_9_06PM`](20245/matches/2026-11-21_16-58-32_R-NoCross_11-18_9_06PM.wpilog.gz) |
| Playoff M10 (Final) | 17:37 | Red | 10255 | 13702 / 11228 | W 229–107 | [`2026-11-21_17-35-45_R-NoCross_11-18_9_06PM`](20245/matches/2026-11-21_17-35-45_R-NoCross_11-18_9_06PM.wpilog.gz) |

8 match logs, 29 practice logs.

## Notes

- 19429 `15-10-02`: a Basic_Drive log shares Q39's INIT time with no PLAY in it; it is in `practice/`.
- The logs between matches that INIT and stop with no PLAY (19429 `12-44-57`, `14-16-46`; 20245 `13-08-06`,
  `13-08-53`, `13-30-32`, `13-33-05`, `16-19-38`) are queue re-inits. Those that do reach PLAY outside any
  match (19429 `12-41-01`, `12-45-22`, `13-08-21`; 20245 `12-42-54`, `12-47-14`, `12-56-31`, `13-09-03`,
  `16-08-51`, `16-16-27`) are practice runs.
