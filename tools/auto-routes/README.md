# Auto routes from code

`autogen.py` writes an Auto Builder `.pp` from a few lines of Python and exports it with the
Auto Builder's own exporter, so many route ideas can be tried in the simulation quickly. The `.pp`
it writes is still the source of truth: open it in the Auto Builder to see or change the route.

## The candidates (3 Oct 2026)

Average alliance AUTO points over 20 simulated runs, on normal tiles / tiles with 3× the friction.

| Match | Script | Auto | Points | Needs |
|---|---|---|---|---|
| Our two sister robots | `recycle3.py` | recycle3 | 84 / 78 | catapult (triangle cup), 18 in front intake, piece counter, webcam pickup |
| Our two sister robots | `recycle4.py` | recycle4 | 87 / 67 | recycle3, and an intake that runs backwards to set pieces down |
| Our two sister robots | `recycle5.py` | recycle5 | 82 / 75 | recycle4, and slats that let the launcher throw straight back |
| Partner fires its preloads | `three_tip_adaptive.py` | three-tip-adaptive | 75 / 75 | two spring hoods, front intake |
| Partner fires its preloads from the right start | `right_partner.py` | left-tunnel | 73 / 76 | catapult, front intake |
| Partner can't shoot, stages its preloads | (`.pp` only) | staged-three-tip | 71 / 59 | two spring hoods, webcam pickup |

## Open them in the Visualizer

Each link opens the latest pushed `.pp` from this branch in the Visualizer: no login, nothing to
download. It opens as a copy (the team's file is never changed from the browser); to change an
Auto, edit the `.pp` and push, or rerun its script. A push shows up within about 5 minutes. One
link per robot: the sister Autos have one for each end.

To watch a pair together: open each link and click **Save as new file** in its banner, then
**Manage Multiple Paths Visualization** in the top bar, tick both, and Apply (up to 4 files play
at once). The saved copies are a snapshot: after an Auto changes, open its link and save again.

| Auto | Our robot | The other robot |
|---|---|---|
| recycle3 | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle3-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle3-left.pp) |
| recycle4 | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle4-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle4-left.pp) |
| recycle5 | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle5-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle5-left.pp) |
| three-tip-adaptive | [three-tip-adaptive](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/three-tip-adaptive.pp) | partner: [partner-preloads-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) |
| left-tunnel | [left-tunnel](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/left-tunnel.pp) | partner: [partner-preloads-right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) |
| staged-three-tip | [staged-three-tip](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/staged-three-tip.pp) | partner: [partner-leave-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-leave-park.pp) |
| solo-tunnel | [solo-tunnel](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/solo-tunnel.pp) | partner: [partner-preloads-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) |

The links read `TeamCode/autos/`, where the team's Autos live (as on `master`, #118). Once this
branch is merged, drop `claude/simulator/` from them to open `master`'s.

Also kept: `solo_tunnel.py` (solo-tunnel), `partners.py` (the reference partners every study runs
against), `snapshots.py` (pictures of the field, from `SnapshotTest`), and `helpers.py`.

```
AUTO_BUILDER_DIR=../visualizer python3 tools/auto-routes/recycle3.py
```

Each script writes its `.pp` into `TeamCode/autos/`, exports the Java next to the other generated
Autos (the simulator's, in `TeamCode/src/test/.../generated/`), and runs `AutoStudyTest`. The
`.pp` files left in `src/test/resources/auto-builder/` are test fixtures. To watch the candidates, build the
review package (`ReviewPackageTest`, see TeamCode/README.md) and open it in AdvantageScope.

## Experiments

Ideas that lost are in `experiments/`, with their `.pp` in `auto-builder/experiments/` and no
Java committed: see `experiments/README.md`.
