# Simulated Autos for review: 2026-10-01-rerun

One folder per run: the `.wpilog` and the Auto Builder `.pp` files that made it. Red alliance,
each seed a typical run, not the best one, unless the row says otherwise. Points are AUTO only.

In AdvantageScope: open a log, then **File → Import Layout** with `sim-review/advantagescope-layout.json`.
AUTO starts 10 s into each log and the robots stand still before and after it. `/Match/Clock`
reads like "AUTO 21.5 s, 8.5 left"; drag it onto a line graph's discrete fields to see it on the timeline.

TIP times are match time; on AdvantageScope's timeline add 10 s.

| Folder | What to watch | Our robot | Partner | Seed | AUTO points | TIPs at | Parked (us / partner) |
|---|---|---|---|---|---|---|---|
| `1-playoff-sisters-stay-home-catapult-triangle` | lean-opp, catapult volleys only (triangle cup): each robot catches its own spill and fires it back; when a volley falls short the left robot gathers loose pieces with the webcam; both park. | clump catapult 72 deg, triangle cup | same as ours | 1 | **76** | 2.1, 9.5, 18.1 s | yes / yes |
| `2-playoff-sisters-two-spring-hoods-a-miss` | lean-opp with two spring hoods, in a run that goes wrong: TIP 2 comes late and there is no TIP 3 (about half the seeds). Neither robot lobs at the far CELL any more. | two spring hoods, 24 in catcher | same as ours | 3 | **51** | 3.9, 17.3 s | no / yes |
| `3-playoff-sisters-both-park` | duo-lz: each robot owns one end of the HIVE; 4 TIPs and both park (4 of 10 seeds; the rest stop at 2 TIPs). | two spring hoods, 24 in catcher | same as ours | 2 | **96** | 3.8, 12.2, 21.0, 27.5 s | yes / yes |
| `4-qual-tunnel-partner-fires-preloads` | solo-tunnel: fires all 4, catches each spill, drives under the HIVE both ways, the GARDEN for TIP 3, parks. | two spring hoods, 24 in catcher | spring hood at 40 in/s | 2 | **76** | 3.8, 11.4, 26.2 s | yes / yes |
| `5-qual-tunnel-partner-only-leaves` | solo-tunnel when the partner only leaves: the partner sets its preloads in a row at its side and drives straight to park; we drive up the row intake first, fire, TIP 2 at about 17 s, and park from the left through a tight gap. | clump catapult 72 deg, 24 in catcher | spring hood at 40 in/s | 2 | **56** | 2.1, 16.7 s | yes / yes |
| `6-qual-tunnel-catapult-triangle` | solo-tunnel with the triangle-cup catapult: the fastest 3 TIPs (2, 9, 22 s) and both park. | clump catapult 72 deg, triangle cup | spring hood at 40 in/s | 1 | **76** | 2.1, 8.8, 23.3 s | yes / yes |
| `7-qual-three-tip-adaptive-at-its-cap` | three-tip-adaptive (the legacy reference: FLOWERs, round the outside, angled shots): fires all 4 and leaves at once; 3 TIPs and park is 76, its ceiling whatever the partner does. | two spring hoods, 24 in catcher | spring hood at 40 in/s | 2 | **76** | 3.8, 11.2, 22.7 s | yes / yes |
| `8-qual-three-tip-adaptive-partner-only-leaves` | three-tip-adaptive with a leave-only partner: the two FLOWERs are reliable, so it makes 3 TIPs and parks in 7 of 10 seeds, where the tunnel route (folder 5) stops at 2. | two spring hoods, 24 in catcher | spring hood at 40 in/s | 2 | **76** | 3.8, 16.0, 27.8 s | yes / yes |
| `9-qual-flower-feed` | Fire and feed: we start left and wait at the far FLOWER, lined up angled with the intake at the back; when the partner's TIP 1 raises our CELL we fire our 4 while the FLOWER's 4 feed in behind them. TIP 2 at 8 s, never more than 4 held. | two spring hoods, 24 in catcher, intake at back | spring hood at 40 in/s | 2 | **76** | 4.5, 8.0, 24.0 s | yes / yes |
| `10-qual-four-tip-attempt-best-case` | Chasing 4 TIPs with a preloads-only partner that holds them for TIP 3. Its best run of 10: TIPs at 4, 13 and 23 s, back with pieces for TIP 4 at 28 s, 2-3 s short. In the other 9 the TIP 1 catch comes up short and TIP 2 fails. | two spring hoods, 24 in catcher, turret | spring hood at 40 in/s | 1 | **71** | 3.8, 13.2, 22.9 s | no / yes |

Rebuild this set (the runs are listed in `ReviewPackageTest.RUNS`). Windows PowerShell:

```powershell
$env:BIOBUZZ_REVIEW = "2026-10-01-rerun"
.\gradlew.bat :TeamCode:testDebugUnitTest --tests "*ReviewPackageTest*" -i
Remove-Item Env:BIOBUZZ_REVIEW
```

macOS/Linux: `BIOBUZZ_REVIEW=2026-10-01-rerun ./gradlew :TeamCode:testDebugUnitTest --tests '*ReviewPackageTest*' -i`
