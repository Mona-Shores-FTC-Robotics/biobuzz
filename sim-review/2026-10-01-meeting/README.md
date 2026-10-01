# Simulated Autos for review: 2026-10-01-meeting

One folder per run: the `.wpilog` and the Auto Builder `.pp` files that made it. Red alliance,
seed 1 (a typical run, not the best one). Points are AUTO only.

In AdvantageScope: open a log, then **File → Import Layout** with `sim-review/advantagescope-layout.json`.
AUTO starts 10 s into each log and the robots stand still before and after it. `/Match/Clock`
reads like "AUTO 21.5 s, 8.5 left"; drag it onto a line graph's discrete fields to see it on the timeline.

TIP times are match time; on AdvantageScope's timeline add 10 s.

| Folder | What to watch | Our robot | Partner | AUTO points | TIPs at | Parked (us / partner) |
|---|---|---|---|---|---|---|
| `1-playoff-sisters-stay-home-catapult` | Both sister robots stay at their own end, catch their spill and fire it back (lean-opp). | clump catapult 72 deg, 24 in catcher | same as ours | **86** | 4.0, 13.4, 15.5, 27.4 s | no / no |
| `2-playoff-sisters-stay-home-two-spring-hoods` | The same plan with two spring-hood launchers each. | two spring hoods, 24 in catcher | same as ours | **66** | 4.5, 14.9, 24.1 s | no / no |
| `3-playoff-sisters-stay-home-loose-catapult` | The catapult again, with a volley that comes apart in the air. | clump catapult 72 deg, loose clump | same as ours | **46** | 4.0, 18.0 s | no / no |
| `4-playoff-sisters-both-park` | duo-lz: each robot owns one end of the HIVE, and both park. | two spring hoods, 24 in catcher | same as ours | **56** | 4.5, 14.9 s | yes / yes |
| `5-qual-solo-partner-fires-preloads-left` | Our solo Auto (three-tip-adaptive); the partner fires its 4 preloads when the left CELL rises, then parks. | two spring hoods, 24 in catcher | spring hood at 40 in/s | **76** | 4.5, 13.8, 27.0 s | yes / yes |
| `6-qual-solo-partner-only-leaves` | Our solo Auto; the partner only leaves and parks. | two spring hoods, 24 in catcher | spring hood at 40 in/s | **71** | 4.5, 19.8, 32.0 s | no / yes |
| `7-qual-solo-partner-fires-left-after-5s` | Our solo Auto; the partner starts left, waits 5 s, fires its preloads and parks. | two spring hoods, 24 in catcher | spring hood at 40 in/s | **76** | 4.5, 13.8, 26.8 s | yes / yes |
| `8-qual-partner-tips-right-we-go-left` | The partner starts right and makes TIP 1; we start left and take the left CELL. | clump catapult 72 deg, 24 in catcher | spring hood at 40 in/s | **76** | 4.5, 13.6, 25.6 s | yes / yes |

Rebuild this set: `BIOBUZZ_REVIEW=2026-10-01-meeting ./gradlew :TeamCode:testDebugUnitTest --tests '*ReviewPackageTest*' -i`. The runs are listed in
`ReviewPackageTest.RUNS`.
