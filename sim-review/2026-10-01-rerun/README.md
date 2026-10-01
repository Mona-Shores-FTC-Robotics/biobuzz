# Simulated Autos for review: 2026-10-01-rerun

One folder per run: the `.wpilog` and the Auto Builder `.pp` files that made it. Red alliance,
each seed a typical run, not the best one, unless the row says otherwise. Points are AUTO only.

In AdvantageScope: open a log, then **File → Import Layout** with `sim-review/advantagescope-layout.json`.
AUTO starts 10 s into each log and the robots stand still before and after it. `/Match/Clock`
reads like "AUTO 21.5 s, 8.5 left"; drag it onto a line graph's discrete fields to see it on the timeline.

TIP times are match time; on AdvantageScope's timeline add 10 s.

## The partner doesn't shoot: it stages its preloads for us

`1-partner-stages-preloads/`. A partner that can't fire sets its 4 preloads on the tiles touching it (G304) for us to collect, then drives to park. The orange outline in front of our robot is where its intake catches pieces (18 in wide, the robot's own width).

| Folder | What to watch | Our robot | Partner | Seed | AUTO points | TIPs at | Parked (us / partner) |
|---|---|---|---|---|---|---|---|
| `staged-three-tip` | The partner sets its 4 preloads in a row at its side and drives straight to park. We fire ours (TIP 1), go through the tunnel, pick up the row with the webcam, fire, then the far FLOWER for TIP 2; the GARDEN for TIP 3; park. 3 TIPs in 17 of 20 seeds. | two spring hoods, full-width intake | spring hood at 40 in/s | 3 | **76** | 3.9, 15.7, 27.9 s | yes / yes |
| `solo-tunnel` | The same partner with solo-tunnel: catches the TIP 1 spill, drives up the row intake first, fires, TIP 2 at about 17 s, and parks from the left through a tight gap. No time for TIP 3. | clump catapult 72 deg, full-width intake | spring hood at 40 in/s | 2 | **56** | 2.1, 16.7 s | yes / yes |

## The partner fires one volley of preloads, then parks

`2-partner-fires-preloads/`. The most common partner in qualifications. `partner-starts-left` fires into the left CELL once our TIP 1 raises it; `partner-starts-right` fires at the start (TIP 1 is theirs), or holds its volley for later.

| Folder | What to watch | Our robot | Partner | Seed | AUTO points | TIPs at | Parked (us / partner) |
|---|---|---|---|---|---|---|---|
| `partner-starts-left/solo-tunnel` | solo-tunnel: fires all 4, catches each spill, drives under the HIVE both ways, the GARDEN for TIP 3, parks. 3 TIPs in 4 of 10 seeds with this robot (the rest stop at 2). | two spring hoods, full-width intake | spring hood at 40 in/s | 3 | **76** | 3.9, 11.4, 27.0 s | yes / yes |
| `partner-starts-left/solo-tunnel-catapult` | solo-tunnel with the triangle-cup catapult: 3 TIPs and both park in 7 of 10 seeds. | clump catapult 72 deg, triangle cup, full-width intake | spring hood at 40 in/s | 1 | **76** | 2.1, 8.8, 23.3 s | yes / yes |
| `partner-starts-left/three-tip-adaptive` | three-tip-adaptive (the legacy reference: FLOWERs, round the outside, angled shots): fires all 4 and leaves at once; 3 TIPs and park in 9 of 10 seeds, 76, its ceiling whatever the partner does. | two spring hoods, full-width intake | spring hood at 40 in/s | 2 | **76** | 3.8, 11.2, 22.9 s | yes / yes |
| `partner-starts-right/flower-feed` | Fire and feed: the partner makes TIP 1; we wait at the far FLOWER, lined up angled with the intake at the back, and fire our 4 while the FLOWER's 4 feed in behind them. TIP 2 at 8 s, never more than 4 held. 3 TIPs in 3 of 10 seeds with an 18 in intake. | two spring hoods, full-width intake, intake at back | spring hood at 40 in/s | 2 | **76** | 4.5, 8.0, 24.0 s | yes / yes |
| `partner-starts-right/left-tunnel-catapult` | The partner makes TIP 1; we start left, add our preloads and the far FLOWER for TIP 2, tunnel right, the GARDEN for TIP 3, park. 76 in 10 of 10 seeds. | clump catapult 72 deg, triangle cup, full-width intake | spring hood at 40 in/s | 1 | **76** | 4.5, 11.6, 26.1 s | yes / yes |
| `partner-holds-its-volley/four-tip-attempt` | Chasing 4 TIPs: the partner holds its preloads for TIP 3. With an 18 in intake it never gets past 2 TIPs: the TIP 1 catch comes up short and TIP 2 fails. Kept to show why. | two spring hoods, full-width intake, turret | spring hood at 40 in/s | 3 | **51** | 3.8, 29.5 s | no / yes |

## Choreography: two robots that both run our Autos

`3-choreography/`. Playoffs with a capable partner, or our two sister robots.

| Folder | What to watch | Our robot | Partner | Seed | AUTO points | TIPs at | Parked (us / partner) |
|---|---|---|---|---|---|---|---|
| `stay-home/catapult-triangle` | lean-opp, catapult volleys only (triangle cup): each robot catches its own spill and fires it back; when a volley falls short the left robot gathers loose pieces with the webcam; both park. 3 TIPs in 5 of 10 seeds. | clump catapult 72 deg, triangle cup, full-width intake | same as ours | 2 | **76** | 2.1, 9.4, 18.8 s | yes / yes |
| `stay-home/two-spring-hoods-a-miss` | lean-opp with two spring hoods, in a run that goes wrong: no TIP 3 (6 of 10 seeds). | two spring hoods, full-width intake | same as ours | 1 | **56** | 3.8, 12.3 s | yes / yes |
| `each-owns-an-end/duo-lz-4-tips` | duo-lz: each robot owns one end of the HIVE; 4 TIPs and both park (2 of 10 seeds with an 18 in intake; most stop at 2 TIPs). | two spring hoods, full-width intake | same as ours | 6 | **96** | 4.1, 12.0, 14.6, 24.0 s | yes / yes |


Rebuild this set (the runs are listed in `ReviewPackageTest.RUNS`). Windows PowerShell:

```powershell
$env:BIOBUZZ_REVIEW = "2026-10-01-rerun"
.\gradlew.bat :TeamCode:testDebugUnitTest --tests "*ReviewPackageTest*" -i
Remove-Item Env:BIOBUZZ_REVIEW
```

macOS/Linux: `BIOBUZZ_REVIEW=2026-10-01-rerun ./gradlew :TeamCode:testDebugUnitTest --tests '*ReviewPackageTest*' -i`
