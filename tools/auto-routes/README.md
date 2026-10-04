# Auto routes from code

`autogen.py` writes an Auto Builder `.pp` from a few lines of Python and exports it with the
Auto Builder's own exporter, so many route ideas can be tried in the simulation quickly. The `.pp`
it writes is still the source of truth: open it in the Auto Builder to see or change the route.

## The candidates (4 Oct 2026, on the filmed spill)

Average alliance AUTO points over 20 simulated runs, on the spill fitted to the 3 Oct films: it pours
off the lowered CELL's lip, first touches the tiles about 42 in out from the wall and bounces back
toward it (`TeamCode/README.md`, "The spill, filmed"). The robots catch it where it comes back to them
(`helpers.catch_spill`).

"Designed for" is the robot each Auto was written for, on normal tiles / tiles with 3× the friction.
"One launcher" is the robot the build team is building now (4 Oct): one two-wheel launcher for both
pieces and an 18 in intake (the simulator's "spring hood, full-width intake"), normal tiles.

| Match | Script | Auto | Designed for | One launcher | Needs (as designed) |
|---|---|---|---|---|---|
| Our two sister robots | `recycle3.py` | recycle3 | 79 / 83 | 58 | catapult (triangle cup), 18 in front intake, piece counter, webcam pickup |
| Partner fires its preloads | `three_tip_adaptive.py` | three-tip-adaptive | 76 / 75 | **74** | two spring hoods, front intake, webcam pickup |
| Our two sister robots | `recycle5.py` | recycle5 | 76 / 82 | 46 | recycle3, and slats that let the left robot's launcher throw straight back |
| Partner fires its preloads from the right start | `right_partner.py` | left-tunnel | 74 / 76 | 51 | catapult, front intake |
| Partner can't shoot, stages its preloads | `three_tip_adaptive.py` (`staged=True`) | staged-three-tip | 74 / 67 | 56 | two spring hoods, webcam pickup |
| Qualification: we work the HIVE alone, the partner fires its preloads | `solo_tunnel.py` | solo-tunnel | 70 / 72 | 56 | two spring hoods, front intake |
| Our two sister robots | `recycle4.py` | recycle4 | 72 / 46 | — | not redesigned for the filmed spill: its right robot stages its catch against the wall |

On one launcher only three-tip-adaptive still makes 3 TIPs (18 of 20 runs): a flywheel can't spin up
before the match and fires one piece at a time, too slow for Autos that fire a whole load the moment a
CELL rises. Two runs in 20 flag a problem: recycle5 on slow tiles (the robots touch at 27.1 s) and
solo-tunnel on one launcher (it brushes a FLOWER at 26.9 s).

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

| Auto | Together | Our robot | The other robot |
|---|---|---|---|
| recycle3 | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/recycle3) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle3-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle3-left.pp) |
| recycle4 | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/recycle4) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle4-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle4-left.pp) |
| recycle5 | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/recycle5) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle5-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle5-left.pp) |
| three-tip-adaptive | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/three-tip-adaptive) | [three-tip-adaptive](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/three-tip-adaptive.pp) | partner: [partner-preloads-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) |
| left-tunnel | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/left-tunnel) | [left-tunnel](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/left-tunnel.pp) | partner: [partner-preloads-right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) |
| staged-three-tip | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/staged-three-tip) | [staged-three-tip](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/staged-three-tip.pp) | partner: [partner-leave-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-leave-park.pp) |
| solo-tunnel | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/solo-tunnel) | [solo-tunnel](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/solo-tunnel.pp) | partner: [partner-preloads-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) |

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
