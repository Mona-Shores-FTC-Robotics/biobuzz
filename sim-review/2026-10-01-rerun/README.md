# Simulated Autos for review: 2026-10-01-rerun

One folder per run: the `.wpilog` and the Auto Builder `.pp` files that made it. Red alliance,
each seed a typical run, not the best one, unless the row says it shows a miss. Points are AUTO only.

In AdvantageScope: open a log, then **File → Import Layout** with `sim-review/advantagescope-layout.json`.
AUTO starts 10 s into each log and the robots stand still before and after it. `/Match/Clock`
reads like "AUTO 21.5 s, 8.5 left"; drag it onto a line graph's discrete fields to see it on the timeline.

TIP times are match time; on AdvantageScope's timeline add 10 s.

| Folder | What to watch | Our robot | Partner | Seed | AUTO points | TIPs at | Parked (us / partner) |
|---|---|---|---|---|---|---|---|
| `1-playoff-sisters-stay-home-catapult-triangle` | lean-opp, catapult volleys only (triangle cup): each robot catches its own spill and fires it back. TIPs at 2, 9 and 18 s. | clump catapult 72 deg, triangle cup | same as ours | 1 | **66** | 2.1, 9.5, 18.0 s | no / no |
| `2-playoff-sisters-two-spring-hoods-a-miss` | lean-opp with two spring hoods, in a run that goes wrong: TIP 2 comes late and there is no TIP 3 (2 of 10 seeds). | two spring hoods, 24 in catcher | same as ours | 3 | **46** | 3.9, 16.9 s | no / no |
| `3-playoff-sisters-both-park` | duo-lz: each robot owns one end of the HIVE, 3 TIPs, and both park. | two spring hoods, 24 in catcher | same as ours | 1 | **76** | 3.8, 12.3, 24.5 s | yes / yes |
| `4-qual-tunnel-partner-fires-preloads` | solo-tunnel: fires all 4, catches each spill, drives under the HIVE both ways, the GARDEN for TIP 3, parks. | two spring hoods, 24 in catcher | spring hood at 40 in/s | 2 | **76** | 3.8, 11.1, 25.8 s | yes / yes |
| `5-qual-tunnel-partner-only-leaves` | solo-tunnel when the partner only leaves: picks up its staged preloads, the far FLOWER once, TIP 2 at 25 s, parks from the left through a tight gap. | clump catapult 72 deg, 24 in catcher | spring hood at 40 in/s | 2 | **56** | 2.1, 25.2 s | yes / yes |
| `6-qual-tunnel-catapult-triangle` | solo-tunnel with the triangle-cup catapult: the fastest 3 TIPs (2, 9, 22 s) and both park. | clump catapult 72 deg, triangle cup | spring hood at 40 in/s | 1 | **76** | 2.1, 8.9, 22.3 s | yes / yes |
| `7-qual-three-tip-adaptive-at-its-cap` | three-tip-adaptive (the legacy reference, angled shots): 3 TIPs and park is 76, its ceiling whatever the partner does. | two spring hoods, 24 in catcher | spring hood at 40 in/s | 1 | **76** | 4.5, 12.2, 23.5 s | yes / yes |
| `8-qual-three-tip-adaptive-partner-only-leaves` | three-tip-adaptive with a leave-only partner: TIP 3 at 28 s, too late to park. | two spring hoods, 24 in catcher | spring hood at 40 in/s | 1 | **71** | 4.5, 16.9, 28.1 s | no / yes |

Rebuild this set: `BIOBUZZ_REVIEW=2026-10-01-rerun ./gradlew :TeamCode:testDebugUnitTest --tests '*ReviewPackageTest*' -i`. The runs are listed in
`ReviewPackageTest.RUNS`.
