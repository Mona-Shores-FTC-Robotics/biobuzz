# Auto routes from code

`autogen.py` writes an Auto Builder `.pp` from a few lines of Python and exports it with the
Auto Builder's own exporter, so many route ideas can be tried in the simulation quickly. The `.pp`
it writes is still the source of truth: open it in the Auto Builder to see or change the route.

## The qualifier Autos

`qual.py` writes one Auto for each kind of qualification partner, for the two-wheel launcher robot
(the simulator's "spring hood, full-width intake"); `python3 qual.py 20` exports them and simulates
each with its partner. Average alliance AUTO points over 20 runs, normal tiles / tiles with 3× the
friction, and how many of the 20 made 3 TIPs:

| Partner | Auto (file) | Partner's Auto | Points | 3 TIPs |
|---|---|---|---|---|
| At the standard left start: fires its 4 when the left CELL rises, parks | Qual-PartnerShootsLeft (`qual-partner-shoots-left`) | `partner-preloads-left` | 70 / 72 | 18 / 19 |
| At the standard left start: only drives and parks | Qual-PartnerParksLeft (`qual-partner-parks-left`, the same route) | `partner-park-left` | 57 / 56 | 10 / 12 |
| At the right start: fires its 4 at once, parks | Qual-PartnerShootsRight (`qual-partner-shoots-right`) | `partner-preloads-right` | **72 / 74** | 16 / 18 |

All three use one plan:

- **Every shot is straight on.** On the CELL's axis (x 57.5), from the start spot out to where the
  hood still scores (ShotMapTest): y 13–29 for the right CELL, y 113–129 for the left (109 is too close).
  Pieces picked up at the GARDEN or a FLOWER are carried there; the half second that costs buys the
  high-percentage shot. Waiting for a TIP, the robot faces the HIVE, its camera on it.
- **The tunnel is the road.** The robot drives square through the tunnel under the HIVE (x 57.5)
  between the two CELLs. Coming out north it turns round at y 104, where its corners (12.7 in when
  turning) clear both the HIVE frame and a partner still at the left start, then backs into y 114.
- **Drive through the spill as it lands, don't wait for it.** A spill first touches the tiles about 42 in
  out from its wall, about 1.1 s after the TIP, and has scattered out of reach about 1 s later. Driving
  through the tunnel with the intake running as the next CELL rises catches 0–4. Pausing first to let
  it land was worse (tried at 0.3, 0.6 and 0.9 s).
- **The static sources decide TIP 3.** The far FLOWER, wall FLOWER and GARDEN hold 4 POLLEN each. With a
  partner that shoots, TIP 2 is its 4 plus our catch (plus the far FLOWER if short), and TIP 3 is TIP 2's
  spill caught going south, the GARDEN and the wall FLOWER. With a partner that only drives, TIP 2 is our
  catch plus the far FLOWER: enough in 16 of 20 runs, and TIP 3 comes in about half. A route that carries
  the wall FLOWER north as well makes TIP 2 every time, but at 21.5 s, too late for TIP 3 (50 points).
- **Qual-PartnerShootsRight is the one to build first.** A partner that can shoot but do little else
  can fire its preloads from the right start at once; firing from the left start means watching for the
  left CELL to rise, which such a partner probably can't. It also parks every time: after the GARDEN's
  shots it drives straight to PARK, because TIP 3 finishes on its own and the wall FLOWER trip never got
  back in time to add one. (The left routes still go for the wall FLOWER: there it makes TIP 3 in 3–10
  more runs of 20.)
- **PARK when there is time, never instead of a TIP.** Each ends in the LOADING ZONE (below the partner) if
  it gets there by 30 s. A park path at the end would make the endgame guard cut the last fire short
  to leave time to drive there, and TIP 3 (20) is worth more than PARK (5).

**What we ask of the partner.** Start at the standard left start (or right, for Qual-PartnerShootsRight).
From the left start: if it can shoot, fire when the left CELL rises; then, straight away, back 4.75 in
off the wall and drive west under the far FLOWER (y 127.5), then down the wall to the far end of the
LOADING ZONE (`partners.preloads_left` / `park_left`). That lane keeps it out of the tunnel's exit and
where we fire from. A partner that waits at its start blocks the far FLOWER: one waiting until 10 s
collided with us in every run.

**What the intake needs (for build).** Re-running the three with different intakes (normal tiles):

| Intake | ShootsLeft | ParksLeft | ShootsRight |
|---|---|---|---|
| As simulated: 0.35 s a piece, grabs 85%, takes pieces moving up to 60 in/s | 70 | 57 | 68 |
| 0.12 s a piece | 73 | 60 | 72 |
| Grabs 95%, up to 100 in/s | 72 | 56 | 70 |
| 0.12 s a piece, but only pieces moving under 30 in/s | 73 | 55 | 70 |

Time per piece is what helps most: about 3 points everywhere, most of it TIP 3 coming more often. An
intake that takes a piece while it is still rolling matters most with a partner that only drives,
where the spill we catch going north is all TIP 2 has besides the far FLOWER: requiring slow pieces
there costs 5 points. Grab chance alone hardly matters.

The commands these Autos use (`CollectSeen`, `LaunchAll`, `IntakeFull`,
`LeftCellUp`, ...) exist only in the simulator so far, and its launcher numbers (2 s spin-up, 0.45 s a
shot) are unmeasured.

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
