# Auto routes from code

`autogen.py` writes an Auto Builder `.pp` from a few lines of Python and exports it with the
Auto Builder's own exporter, so many route ideas can be tried in the simulation quickly. The `.pp`
it writes is still the source of truth: open it in the Auto Builder to see or change the route.

## The qualifier Autos

Two Autos, for the two partners we expect most in qualification, both for the two-wheel launcher robot
(the simulator's "spring hood, full-width intake", speed 50). Alliance AUTO points over 20 runs, normal
tiles / tiles with 3× the friction, and how many of the 20 made 3 TIPs. Each row says when its numbers
were last run (UTC); a newer commit to the route or the simulator can change them.

| Partner | Our Auto (file, script) | Partner's Auto | Points | 3 TIPs | Updated |
|---|---|---|---|---|---|
| Can shoot: fires its 4 preloads from the right start at once, parks | Qual-PartnerShootsRight v2 (`qual-right-v2`, `qual_right.py`) | `partner-preloads-right` | **75.8 / 76** | **20 / 20** | 5 Oct 2026 01:40 |
| Can't shoot: sets its 4 preloads down across the tunnel's north exit, parks | Qual-PartnerStages (`qual-partner-stages`, `qual.py`) | `partner-stage-exit` | 58.5 / 62 (work in progress) | 7 / 11 | 5 Oct 2026 01:40 |

**With side walls** (`claude/dazzling-maxwell-je04gu`, its design "spring hood, full-width intake,
side walls": walls down both sides that slide 6 in forward when our CELL starts to TIP, with one-way
flaps). Same 20 runs, normal / slow tiles, run 5 Oct 2026 01:45 UTC on that branch's commit `1a91443`:

| Auto | Plain: 3 TIPs, points | Walls: 3 TIPs, points |
|---|---|---|
| Qual-PartnerShootsRight v2 | 20 / 20, 75.8 / 76 | 20 / 20, 76 / 75.8 |
| Qual-PartnerShootsRight v1 | 16 / 18, 72 / 74 | 18 / 19, 74 / 75 |
| Qual-PartnerShootsLeft | 18 / 19, 70.3 / 72.3 | 19 / 19, 73 / 72.5 |
| **Qual-PartnerStages** | 7 / 11, 58.5 / 62 | **16 / 12, 69.3 / 63.8** |

The walls matter most where the route is short of pieces: Qual-PartnerStages on normal tiles gains 9
TIP 3s (+11 points). They add nothing to v2, which already makes 20 / 20.

**G409, in every Auto (5 Oct 2026).** That branch also flags a robot touching a spilled piece before it
reaches the tiles (G409). Every qualifier Auto does it, walls or not: 5–11 touches a run on normal tiles
(v2 5.4, ShootsLeft 10.6, Stages 11.0), 2–8 on slow ones, in nearly every run. The robot is under the
falling spill (e.g. Stages, TIP 1 at 5.9 s, waiting at the right CELL). Whether the referees would call
it depends on the manual's wording and on how high real pieces fall from the lip; check that before
these routes are built, because the fix is to wait further back while a CELL tips.

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

`python3 qual_right.py 20` and `python3 qual.py 20 stages` export and simulate them. On 40 more runs
(seeds 21–60) Qual-PartnerShootsRight v2 makes 3 TIPs in 38 / 40 on both tiles (v1: 32 / 38): in
one run the partner's preloads miss TIP 1; in the other the sweep finds nothing and the third load
finds the GARDEN empty. qual.py's left-start Autos
(`shoots-left`, `parks-left`) are kept but no longer worked on.

**Rules for both** (mentor review):

- **Every shot is straight on**: on the CELL's axis (x 57.5), y 13–29 for the right CELL, y 113–129 for
  the left (109 is too close). Pieces picked up anywhere are carried there.
- **Waiting for a TIP, face the HIVE** (its camera on it).
- **The tunnel is the road**: square through it under the HIVE (x 57.5), turning only clear of the frame.
- **After the last TIP, PARK** (LOADING ZONE, x 0–11, y 94–118), never instead of a TIP: no park path
  after a fire that may still be going (the endgame guard would cut the fire short).
- **The partner only fires from its start, then parks.** It can't tell whether the HIVE has tipped.

### Qual-PartnerShootsRight v2

TIP 1 (4.6 s) is the partner's 4 on the 3 NECTAR. TIP 2 (13.3 s): our preloads when the left CELL
rises, then the far FLOWER's 4. TIP 3 (26 s): TIP 2's spill caught driving south through the tunnel,
then TIP 1's leftovers and the GARDEN. What changed from v1 (`qual-partner-shoots-right`: 72 / 74,
16 / 18), each step on 20 runs, normal / slow tiles:

| Change | Points | 3 TIPs |
|---|---|---|
| v1 | 72 / 74 | 16 / 18 |
| Fire TIP 2's spill from y 24, not y 28 (28 is at the edge of the shot map: 1–2 of 4 missed) | 74 / 76 | 18 / 20 |
| Go to the GARDEN by a sweep west along y 10, intake first: TIP 1's NECTAR and POLLEN lie there (2–8 pieces at 17 s, NECTAR is 1.65 POLLEN) | 75 / 76 | 19 / 20 |
| No TIP 3 yet (the right CELL still up) once the GARDEN's shots are away: back to the GARDEN, look again, and fire what it holds | **75.8 / 76** | **20 / 20** |

The last step relies on §10.5: a TIP that completes in the 8 s after AUTO still counts, so the third
load, fired from 28.6 s, still makes TIP 3 (seed 16: at 30.0 s). That run doesn't PARK. Tried and
dropped: leaving N_FIRE for the spill sooner or later than when the right CELL is up (about 0.6 s
after TIP 2 starts): at 0, 0.2 or 0.4 s, 0–15 / 20; 0.15 or 0.3 s later, 16–18, firing from y 16–21, a tunnel lane at x 55 or 60, a webcam
pickup facing north after the GARDEN (the leftovers lie behind the robot, toward the wall).

**Standing where our TIP's spill lands (run 5 Oct 2026 01:20 UTC).** Every qualifier Auto fires the shot that tips a CELL from that
CELL's catch spot (y 114 for the left CELL, 28 in out from the wall), so the robot is there when the
spill lands, and drives off through the tunnel as it lands. Waiting there 1–2.5 s instead catches no
more (2.3–2.9 of TIP 2's 8 in 3 s, against 3.0 driving through) and costs TIP 3 (14–17 / 20); the
pieces land 2–12 in in front of the intake and within half a second scatter sideways or back under
the HIVE. Staying for TIP 3's spill instead of PARK catches 1.1 by 30 s. Trials in `qual_right.py`
(`TRIALS`). This rests on the simulated scatter (fitted to the 3 Oct films): if real pieces settle
in front of a waiting robot, waiting wins, so film a TIP with a robot standing at the catch spot.

**Why TIP 3 was short.** TIP 2's 8 POLLEN land 94–100 in up the field about 1 s after the TIP and
scatter; driving through, we catch 2–4. The rest roll east across the centre line (2.5 of the 8 on
average, up to 6) or south ahead of us.

**A side shield (mentor's idea, run 4–5 Oct 2026; superseded by the side walls above).** A wall on the robot's side toward the centre line, flush with the
frame's side and reaching 3 or 6 in past its front (R105 allows 6: 18 × 24 in). Driving south through
the tunnel, intake first, that is the robot's *left*. In the simulator it is `RobotDesign.shieldReachIn`
(designs "spring hood, full-width intake, 3 in shield" / "6 in shield"; "6 in shield right" puts it
on the other side, toward our wall, and doesn't help). TIP 2's spill pieces that cross the centre line,
of 8, normal / slow tiles: none 2.5 / 2.1, 3 in 1.2 / 1.0, 6 in 0.9 / 0.7. TIP 3, 20 runs:

| Route | No shield | 3 in shield | 6 in shield |
|---|---|---|---|
| v1 | 16 / 18 | 19 / 19 | 19 / 20 |
| v1, spill fired from y 24 (`qual-right-sfire`) | 18 / 20 | 20 / 20 | 20 / 20 |
| v2 | **20 / 20** | 19 / 20 | 20 / 19 |

So the shield buys what the route changes buy, but not on top of them: with v2 the misses move to
other seeds. It also costs a constraint: turning round at the far FLOWER its corner reaches 17.5 in
(6 in shield) and crosses the centre line, unless the robot turns through west
(`qual-right-v2-shield` does). Build it only if the route's third load turns out too slow on a real
robot.

The commands these Autos use (`CollectSeen`, `LaunchAll`, `IntakeFull`, `LeftCellUp`, ...) exist only
in the simulator so far, and its launcher (2 s spin-up, 0.45 s a shot) and intake (0.35 s a piece)
numbers are unmeasured. A faster intake or launcher gives the third load more margin.

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
| Qual-PartnerShootsRight v2 (`qual-right-v2`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-PartnerShootsRight-v2) | [qual-right-v2](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-right-v2.pp) | partner: [partner-preloads-right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) |
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
