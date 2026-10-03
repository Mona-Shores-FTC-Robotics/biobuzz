# Experiments

Route ideas the simulation ranked below the candidates. Each script still runs (from here or the
folder above) and writes its `.pp` into `TeamCode/src/test/resources/auto-builder/experiments/`;
its generated Java is not committed, so export it again to run it.

| Script | What it tried | Why it's here |
|---|---|---|
| `duo_lz.py` | each robot owns one end of the HIVE, LOADING ZONE in between | 4 TIPs in 6 of 20 runs, mostly 2 (71 / 57 points) |
| `lean_duo.py`, `lean_opportunist.py` | each robot catches its own spill and fires it straight back, then parks | 69 / 57 points; the recycle Autos replaced them |
| `home_duo.py`, `rally.py`, `convoy.py` | stay home and stream; never park; both robots on whichever CELL is up | lost to lean-opp, then to recycle |
| `solo_shuttle.py`, `four_tip_adaptive.py`, `four_tip_solo.py` | 4 TIPs with one of our robots | never got past 2-3 TIPs |
| `partner_start.py` | where a preloads-only partner should start | answered: the right start (left-tunnel) |
| `staging_study.py` | where a non-shooting partner should stage its preloads | answered: in a row at its side (staged-three-tip) |

The fire-and-feed Autos (`flower_feed` in `../right_partner.py`) are here too, as
`auto-builder/experiments/flower-feed*.pp`.

Archived with them on 3 Oct 2026, and still in git history at commit `feb3b1e` (restore with
`git checkout feb3b1e -- <path>`): their generated Java, the sweeps of staging spots, and the
study tests that ran them, `AllianceAutoTest`, `BestAutosTest`, `DecisionStudyTest`,
`RobustnessTest` and `SpillStudyTest`. The review packages of 1 Oct 2026 (`sim-review/2026-10-01-*`)
are in history at the same commit.
