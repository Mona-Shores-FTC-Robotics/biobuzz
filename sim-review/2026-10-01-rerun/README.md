# Simulated Autos for review: 2026-10-01-rerun

One folder per run: the `.wpilog` and the Auto Builder `.pp` files that made it. Red alliance,
each seed a typical run, not the best one, unless the row says otherwise. Points are AUTO only.

In AdvantageScope: open a log, then **File → Import Layout** with `sim-review/advantagescope-layout.json`.
AUTO starts 10 s into each log and the robots stand still before and after it. `/Match/Clock`
reads like "AUTO 21.5 s, 8.5 left"; drag it onto a line graph's discrete fields to see it on the timeline.

TIP times are match time; on AdvantageScope's timeline add 10 s.

## Solo: the partner does nothing, or only leaves and parks

`1-solo/`. What our robot can do on its own. In `partner-leaves` the partner sets its 4 preloads on the tiles for us before it drives off; that is all it does.

| Folder | What to watch | Our robot | Partner | Seed | AUTO points | TIPs at | Parked (us / partner) |
|---|---|---|---|---|---|---|---|
| `alone/three-tip-adaptive` | Our robot alone: both FLOWERs and the GARDEN, angled shots. 3 TIPs in 8 of 10 seeds; parks. | two spring hoods, 24 in catcher | same as ours | 2 | **68** | 3.8, 16.0, 27.8 s | yes |
| `alone/solo-tunnel` | solo-tunnel alone: it counts on a partner for the second TIP's pieces, so on its own it makes 2 TIPs, late. | two spring hoods, 24 in catcher | same as ours | 2 | **48** | 3.8, 22.9 s | yes |
| `partner-leaves/solo-tunnel` | The partner sets its preloads in a row at its side and drives straight to park; we drive up the row intake first, fire, TIP 2 at about 17 s, and park from the left through a tight gap. | clump catapult 72 deg, 24 in catcher | spring hood at 40 in/s | 2 | **56** | 2.1, 16.7 s | yes / yes |
| `partner-leaves/three-tip-adaptive` | The same partner; three-tip-adaptive ignores its preloads but the two FLOWERs are reliable, so it makes 3 TIPs and parks in 7 of 10 seeds. | two spring hoods, 24 in catcher | spring hood at 40 in/s | 2 | **76** | 3.8, 16.0, 27.8 s | yes / yes |

## The partner fires one volley of preloads, then parks

`2-partner-fires-preloads/`. The most common partner in qualifications. `partner-starts-left` fires into the left CELL once our TIP 1 raises it; `partner-starts-right` fires at the start (TIP 1 is theirs), or holds its volley for later.

| Folder | What to watch | Our robot | Partner | Seed | AUTO points | TIPs at | Parked (us / partner) |
|---|---|---|---|---|---|---|---|
| `partner-starts-left/solo-tunnel` | solo-tunnel: fires all 4, catches each spill, drives under the HIVE both ways, the GARDEN for TIP 3, parks. | two spring hoods, 24 in catcher | spring hood at 40 in/s | 2 | **76** | 3.8, 11.4, 26.2 s | yes / yes |
| `partner-starts-left/solo-tunnel-catapult` | solo-tunnel with the triangle-cup catapult: the fastest 3 TIPs (2, 9, 22 s) and both park. | clump catapult 72 deg, triangle cup | spring hood at 40 in/s | 1 | **76** | 2.1, 8.8, 23.3 s | yes / yes |
| `partner-starts-left/three-tip-adaptive` | three-tip-adaptive (the legacy reference: FLOWERs, round the outside, angled shots): fires all 4 and leaves at once; 3 TIPs and park is 76, its ceiling whatever the partner does. | two spring hoods, 24 in catcher | spring hood at 40 in/s | 2 | **76** | 3.8, 11.2, 22.7 s | yes / yes |
| `partner-starts-right/flower-feed` | Fire and feed: the partner makes TIP 1; we wait at the far FLOWER, lined up angled with the intake at the back, and fire our 4 while the FLOWER's 4 feed in behind them. TIP 2 at 8 s, never more than 4 held. | two spring hoods, 24 in catcher, intake at back | spring hood at 40 in/s | 2 | **76** | 4.5, 8.0, 24.0 s | yes / yes |
| `partner-starts-right/left-tunnel-catapult` | The partner makes TIP 1; we start left, add our preloads and the far FLOWER for TIP 2, tunnel right, the GARDEN for TIP 3, park. 76 in 10 of 10 seeds. | clump catapult 72 deg, triangle cup | spring hood at 40 in/s | 1 | **76** | 4.5, 11.6, 25.8 s | yes / yes |
| `partner-holds-its-volley/four-tip-attempt-best-case` | Chasing 4 TIPs: the partner holds its preloads for TIP 3. Its best run of 10: TIPs at 4, 13 and 23 s, back with pieces for TIP 4 at 28 s, 2-3 s short. In the other 9 the TIP 1 catch comes up short and TIP 2 fails. | two spring hoods, 24 in catcher, turret | spring hood at 40 in/s | 1 | **71** | 3.8, 13.2, 22.9 s | no / yes |

## Choreography: two robots that both run our Autos

`3-choreography/`. Playoffs with a capable partner, or our two sister robots.

| Folder | What to watch | Our robot | Partner | Seed | AUTO points | TIPs at | Parked (us / partner) |
|---|---|---|---|---|---|---|---|
| `stay-home/catapult-triangle` | lean-opp, catapult volleys only (triangle cup): each robot catches its own spill and fires it back; when a volley falls short the left robot gathers loose pieces with the webcam; both park. | clump catapult 72 deg, triangle cup | same as ours | 1 | **76** | 2.1, 9.5, 18.1 s | yes / yes |
| `stay-home/two-spring-hoods-a-miss` | lean-opp with two spring hoods, in a run that goes wrong: TIP 2 comes late and there is no TIP 3 (about half the seeds). | two spring hoods, 24 in catcher | same as ours | 3 | **51** | 3.9, 17.3 s | no / yes |
| `each-owns-an-end/duo-lz-4-tips` | duo-lz: each robot owns one end of the HIVE; 4 TIPs and both park (4 of 10 seeds; the rest stop at 2 TIPs). | two spring hoods, 24 in catcher | same as ours | 2 | **96** | 3.8, 12.2, 21.0, 27.5 s | yes / yes |


Rebuild this set (the runs are listed in `ReviewPackageTest.RUNS`). Windows PowerShell:

```powershell
$env:BIOBUZZ_REVIEW = "2026-10-01-rerun"
.\gradlew.bat :TeamCode:testDebugUnitTest --tests "*ReviewPackageTest*" -i
Remove-Item Env:BIOBUZZ_REVIEW
```

macOS/Linux: `BIOBUZZ_REVIEW=2026-10-01-rerun ./gradlew :TeamCode:testDebugUnitTest --tests '*ReviewPackageTest*' -i`
