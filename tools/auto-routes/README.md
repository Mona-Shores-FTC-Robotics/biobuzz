# Auto routes from code

`autogen.py` writes an Auto Builder `.pp` from a few lines of Python and exports it with the
Auto Builder's own exporter, so many route ideas can be tried in the simulation quickly. The `.pp`
it writes is still the source of truth: open it in the Auto Builder to see or change the route.

## The qualifier Autos

Two Autos, for the two partners we expect most in qualification, for the two-wheel launcher robot (the
simulator's "spring hood, full-width intake", speed 50; no side walls). Alliance AUTO points over 20
runs, normal tiles / tiles with 3× the friction; how many of the 20 made 3 TIPs; and G409, spilled
pieces our robot touched before they reached the tiles (per run; must be 0). Links to watch them and
their logs: [the root README](../../README.md#qualifier-autos-the-baseline-no-walls).

| Partner | Our Auto (file, script) | Partner's Auto | Points | 3 TIPs | G409 | Updated (UTC) |
|---|---|---|---|---|---|---|
| Can shoot: fires its 4 preloads from the right start at once, parks | Qual-PartnerShootsRight v3 (`qual-right-v3`, `qual_right.py`) | `partner-preloads-right` | **72.5 / 71.8** | **18 / 17** | **0 / 0** | 5 Oct 2026 12:55 |
| Can't shoot: sets its 4 preloads down across the tunnel's north exit, parks | Qual-PartnerStages (`qual-partner-stages`, `qual.py`, another session) | `partner-stage-exit` | 58.5 / 62 (work in progress) | 7 / 11 | 10 / 5.5 | 5 Oct 2026 12:56 |

**G409-safe versions (`g409.py`, run 5 Oct 2026 03:50 UTC on `claude/dazzling-maxwell-je04gu`).**
The same routes, built by qual.py's and qual_right.py's own functions, with three changes made to the
built Auto: an "It lands" wait after each "TIP n settles" before the tunnel path; the catch spots moved
back from the landing (S_CATCH 8 in toward the right wall, N_LOW 4 in toward the left: at 4 in a
bouncing piece still reached a robot waiting at S_CATCH, and 8 in at N_LOW clips the far FLOWER); and
"TIP 3?: Yes: PARK" waits 700 ms before driving under TIP 3's landing. The design "... side walls out
at the TIP" is the walls, out as soon as our CELL starts to TIP and in when the robot drives off
(over 25 in/s) or turns. Walls that wait until the spill has landed keep no more pieces than no
walls (`SideWallSpillTest`). 20 runs, normal / slow tiles; zero G409 touches is the bar:

| Auto (experiments file) | Robot | 3 TIPs | Points | G409 touches a run |
|---|---|---|---|---|
| v2 + 500 ms (`qual-right-v2-g409-500`) | plain | 18 / 17 | 72.5 / 71.8 | **0 / 0** |
| v2 + 500 ms | walls out at the TIP | 20 / 19 | 75.8 / 74.3 | 1.8 / 0: falling pieces hit the walls at N_FIRE |
| ShootsLeft + 300 ms (`qual-partner-shoots-left-g409-300-s8n4-t700`) | **walls out at the TIP** | **19 / 18** | **71.5 / 69.8** | **0 / 0** |
| ShootsLeft + 300 ms | plain | 17 / 17 | 68.3 / 68.8 | 0 / 0 |
| Stages + 300 ms (`qual-partner-stages-g409-300-s8n4-t700`) | walls out at the TIP | 10 / 12 | 59.0 / 62.1 | 0.2 / 0.1 |
| Stages + 300 ms | plain | 1 / 4 | 50.0 / 52.7 | 0 / 0 |

So waiting costs v2 about 3 points (2 TIP 3s), and the walls win it back for ShootsLeft, where the
robot waits at S_CATCH with the walls out over open tiles: as G409-safe, it matches today's ShootsLeft
(18 / 19, 70.3 / 72.3, with 10.6 touches a run). Qual-PartnerStages loses most from waiting; with walls
it keeps today's numbers but not yet at zero touches. Not chased further here: qual.py and partners.py
are another session's. The sweep's other waits (0–1000 ms, 0 / 4 / 8 in back) are in the commit log of
this branch; `python3 g409.py 20 shoots-left stages --extra 300 --back 8 --north 4 --tip 700 --designs plain,early`
and `python3 g409.py 20 v2 --extra 500` rebuild and rerun the winners.

`python3 qual_right.py 20 qual-right-v3` and `python3 qual.py 20 stages` export and simulate them.
qual.py's left-start Autos (`shoots-left`, `parks-left`) are kept but no longer worked on.

**Rules for both** (mentor review):

- **Every shot is straight on**: on the CELL's axis (x 57.5), y 13–29 for the right CELL, y 113–129 for
  the left (109 is too close). Pieces picked up anywhere are carried there.
- **Waiting for a TIP, face the HIVE** (its camera on it).
- **The tunnel is the road**: square through it under the HIVE (x 57.5), turning only clear of the frame.
- **Let a spill land before driving into it** (G409: "A ROBOT may not catch or deflect a SCORING ELEMENT
  released by a TIPPED HIVE unless and until that SCORING ELEMENT contacts anything else besides that
  ROBOT"). The simulator logs each touch as a `sim: G409` event, and AutoStudyTest's STUDY line ends
  with `G409 x.x (N runs)`.
- **After the last TIP, PARK** (LOADING ZONE, x 0–11, y 94–118), never instead of a TIP: no park path
  after a fire that may still be going (the endgame guard would cut the fire short).
- **The partner only fires from its start, then parks.** It can't tell whether the HIVE has tipped.

### Qual-PartnerShootsRight v3

TIP 1 (4.6 s) is the partner's 4 on the 3 NECTAR. TIP 2 (13.3 s): our preloads when the left CELL
rises, then the far FLOWER's 4. TIP 3 (about 26.5 s): TIP 2's spill, caught driving south through the
tunnel once it has landed, then TIP 1's leftovers and the GARDEN. Each step from v1
(`qual-partner-shoots-right`), 20 runs, normal / slow tiles:

| Change | Points | 3 TIPs | G409 |
|---|---|---|---|
| v1 | 72 / 74 | 16 / 18 | 5.3 / 1.6 |
| Fire TIP 2's spill from y 24, not y 28 (28 is at the edge of the shot map: 1–2 of 4 missed) | 74 / 76 | 18 / 20 | |
| Go to the GARDEN by a sweep west along y 10, intake first: TIP 1's NECTAR and POLLEN lie there (2–8 pieces at 17 s; NECTAR is 1.65 POLLEN) | 75 / 76 | 19 / 20 | |
| v2: no TIP 3 yet (the right CELL still up) after the GARDEN's shots: back to the GARDEN, look again, fire what it holds | 75.8 / 76 | 20 / 20 | 5.4 / 1.6 |
| **v3**: wait 500 ms more after TIP 2 settles, so its spill is on the tiles before we drive in | **72.5 / 71.8** | **18 / 17** | **0 / 0** |

v2 reached TIP 2's landing 0.1–0.2 s before the last pieces did and drove into 4–5 of them each match
(G409's example C: positioning so falling pieces hit the robot "with an advantageous vector"). Waiting
for them to land costs about 3 points. v2's third load relies on §10.5: a TIP that completes in the 8 s
after AUTO still counts, so it fires up to 28.6 s.

Tried and dropped (on v2, before G409 was counted): leaving for the spill sooner (0–0.4 s after TIP 2
starts: 0–15 / 20), firing from y 16–21, a tunnel lane at x 55 or 60, a webcam pickup facing north after
the GARDEN (the leftovers lie behind the robot), and standing at the catch spot for TIP 2's spill or TIP
3's (catches no more than driving through: the pieces land 2–12 in in front of the intake and scatter
within half a second). Trials in `qual_right.py` (`TRIALS`).

The commands these Autos use (`CollectSeen`, `LaunchAll`, `IntakeFull`, `LeftCellUp`, ...) exist only in
the simulator so far, and its launcher (2 s spin-up, 0.45 s a shot) and intake (0.35 s a piece) numbers
are unmeasured. How pieces bounce and roll after a spill lands is the least-measured part of the
simulator: film a TIP to check it.

## The candidates (4 Oct 2026, on the filmed spill and its scatter)

Each Auto's name is who our alliance partner is and what it does, then our plan: **Sister** (our other
robot, full choreography between the two), **PartnerShoots** (fires its 4 preloads, then parks;
PartnerShootsRight: from the right start) or **PartnerStages** (can't shoot: lines its preloads up for
us, then parks). The files keep their older names, in brackets.

Average alliance AUTO points over 20 simulated runs, on the spill fitted to the 3 Oct films: it pours
off the lowered CELL's lip, first touches the tiles about 42 in out from the wall, and scatters across
the field (`TeamCode/README.md`, "The spill, filmed"). The robots wait to catch it short of the
landing (`helpers.catch_spill`); the routes were tuned before the scatter.

"Designed for" is the robot each Auto was written for, on normal tiles / tiles with 3× the friction.
"One launcher" is the robot the build team is building now (4 Oct): one two-wheel launcher for both
pieces and an 18 in intake (the simulator's "spring hood, full-width intake"), normal tiles.

| Match | Script | Auto (files) | Designed for | One launcher | Needs (as designed) |
|---|---|---|---|---|---|
| Partner fires its preloads | `three_tip_adaptive.py` | PartnerShoots-ThreeTip (`three-tip-adaptive`) | 76 / 76 | **59** | two spring hoods, front intake, webcam pickup |
| Partner fires its preloads from the right start | `right_partner.py` | PartnerShootsRight-LeftTunnel (`left-tunnel`) | 63 / 73 | 52 | catapult, front intake |
| Our two sister robots | `recycle5.py` | Sister-ThrowBack (`recycle5`) | 60 / 49 | 46 | recycle3, and slats that let the left robot's launcher throw straight back |
| Partner can't shoot, stages its preloads | `three_tip_adaptive.py` (`staged=True`) | PartnerStages-ThreeTip (`staged-three-tip`) | 59 / 59 | 56 | two spring hoods, webcam pickup |
| Our two sister robots | `recycle3.py` | Sister-Recycle (`recycle3`) | 58 / 55 | 49 | catapult (triangle cup), 18 in front intake, piece counter, webcam pickup |
| Qualification: we work the HIVE alone, the partner fires its preloads | `solo_tunnel.py` | PartnerShoots-Tunnel (`solo-tunnel`) | 55 / 57 | 56 | two spring hoods, front intake |
| Our two sister robots | `recycle4.py` | Sister-SetDown (`recycle4`) | 52 / 52 | — | not redesigned for the filmed spill: its right robot stages its catch against the wall |

Since the scatter every Auto lost points, the catapult Sister Autos most (76–79 before it): pieces spread
across the field are harder to collect than a tidy pile, and the routes were tuned for one. Only
PartnerShoots-ThreeTip on two launchers held (76). Problems flagged: Sister-ThrowBack's robots touch once
in 20 (27.2 s), and PartnerShoots-Tunnel brushes the far FLOWER in 1 of 20 (6 of 20 on one launcher):
when time runs short against the FLOWER, the endgame guard parks it from there.

The simulated robots always know the HIVE's state: `LeftCellUp`, `RightCellUp` and `Tip` read the
simulated HIVE directly, whichever way the robot faces. A real robot gets the same answers from the
CELLs' AprilTags (`vision/HiveTracker`). Of the names these Autos use, the robot's `AutoRegistration`
has only `Tip` so far: `LeftCellUp`, `RightCellUp`, `IntakeFull`, `Empty`, `SpinUp`, `LaunchAll`,
`CollectSeen` and `SetDown` exist only in the simulator until they are built on the robot.

## Open them in the Visualizer

Each link opens the latest pushed `.pp` from this branch in the Visualizer: no login, nothing to
download. It opens as a copy (the team's file is never changed from the browser); to change an
Auto, edit the `.pp` and push, or rerun its script. A push shows up within about 5 minutes. One
link per robot: the sister Autos have one for each end.

To watch a pair together, use the **Together** link: it opens both robots at once in the
Visualizer's multi-path mode, from the pairs in `TeamCode/autos/pairs.json`. Or click **Team Autos**
in the Visualizer's top bar, type the branch (`claude/simulator`), and pick a pair or up to 4 Autos.
**Reload latest** there fetches them again after a push, and **Copy link** shares the view.

| Auto (files) | Together | Our robot | The other robot |
|---|---|---|---|
| Qual-PartnerShootsRight v3 (`qual-right-v3`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-PartnerShootsRight-v3) | [qual-right-v3](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-right-v3.pp) | partner: [partner-preloads-right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) |
| Qual-PartnerStages (`qual-partner-stages`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-PartnerStages) | [qual-partner-stages](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-partner-stages.pp) | partner: [partner-stage-exit](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-stage-exit.pp) |
| Sister-Recycle (`recycle3`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Sister-Recycle) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle3-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle3-left.pp) |
| Sister-SetDown (`recycle4`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Sister-SetDown) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle4-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle4-left.pp) |
| Sister-ThrowBack (`recycle5`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Sister-ThrowBack) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle5-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle5-left.pp) |
| PartnerShoots-ThreeTip (`three-tip-adaptive`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerShoots-ThreeTip) | [three-tip-adaptive](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/three-tip-adaptive.pp) | partner: [partner-preloads-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) |
| PartnerShootsRight-LeftTunnel (`left-tunnel`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerShootsRight-LeftTunnel) | [left-tunnel](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/left-tunnel.pp) | partner: [partner-preloads-right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) |
| PartnerStages-ThreeTip (`staged-three-tip`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerStages-ThreeTip) | [staged-three-tip](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/staged-three-tip.pp) | partner: [partner-leave-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-leave-park.pp) |
| PartnerShoots-Tunnel (`solo-tunnel`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerShoots-Tunnel) | [solo-tunnel](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/solo-tunnel.pp) | partner: [partner-preloads-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) |

The links read `TeamCode/autos/` on `claude/simulator`. The simulator and these Autos stay on this
branch, never `master`, so the links always name it.

Also kept: `partners.py` (the reference partners every study runs
against), `snapshots.py` (pictures of the field, from `SnapshotTest`), and `helpers.py`.

```
AUTO_BUILDER_DIR=../visualizer python3 tools/auto-routes/recycle3.py
```

Each script writes its `.pp` into `TeamCode/autos/`, exports the Java next to the other generated
Autos (the simulator's, in `TeamCode/src/test/.../generated/`), and runs `AutoStudyTest`. The
`.pp` files left in `src/test/resources/auto-builder/` are test fixtures. To watch the candidates, build the
review package (`ReviewPackageTest`, see TeamCode/README.md) and open it in AdvantageScope. Or download each pair's
simulated log from [the root README's table](../../README.md#our-best-autos), made by the Simulate Auto
workflow.

## Experiments

Ideas that lost are in `experiments/`, with their `.pp` in `auto-builder/experiments/` and no
Java committed: see `experiments/README.md`.
