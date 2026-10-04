# Auto routes from code

`autogen.py` writes an Auto Builder `.pp` from a few lines of Python and exports it with the
Auto Builder's own exporter, so many route ideas can be tried in the simulation quickly. The `.pp`
it writes is still the source of truth: open it in the Auto Builder to see or change the route.

## The candidates (3 Oct 2026, on the filmed spill)

Average alliance AUTO points over 20 simulated runs, on normal tiles / tiles with 3× the friction, with
the spill fitted to the 3 Oct films: it pours off the lowered CELL's lip and lands about 4 ft out from the
wall, not against it (`TeamCode/README.md`, "The spill, filmed"). The robots catch it standing just short
of there (`helpers.catch_spill`). Before the films, the recycle Autos scored 82–85 by catching and
sweeping a spill at the wall that the real HIVE doesn't put there.

| Match | Script | Auto | Points | Needs |
|---|---|---|---|---|
| Our two sister robots | `recycle5.py` | recycle5 | 78 / 77 | recycle3, and slats that let the left robot's launcher throw straight back |
| Partner fires its preloads from the right start | `right_partner.py` | left-tunnel | 74 / 76 | catapult, front intake |
| Our two sister robots | `recycle3.py` | recycle3 | 72 / 82 | catapult (triangle cup), 18 in front intake, piece counter, webcam pickup |
| Partner fires its preloads | `three_tip_adaptive.py` | three-tip-adaptive | 72 / 73 | two spring hoods, front intake, webcam pickup |
| Qualification: we work the HIVE alone, the partner fires its preloads | `solo_tunnel.py` | solo-tunnel | 68 / 72 | two spring hoods, front intake |
| Partner can't shoot, stages its preloads | `three_tip_adaptive.py` (`staged=True`) | staged-three-tip | 65 / 67 | two spring hoods, webcam pickup |
| Our two sister robots | `recycle4.py` | recycle4 | 48 / 46 | not redesigned for the filmed spill: its right robot stages its catch against the wall |

The simulated robots always know the HIVE's state: `LeftCellUp`, `RightCellUp` and `Tip` read the
simulated HIVE directly, whichever way the robot faces. A real robot gets the same answers from the
CELLs' AprilTags (`vision/HiveTracker`). Of the names these Autos use, the robot's `AutoRegistration`
has only `Tip` so far: `LeftCellUp`, `RightCellUp`, `IntakeFull`, `Empty`, `SpinUp`, `LaunchAll`,
`CollectSeen` and `SetDown` exist only in the simulator until they are built on the robot.

## One launcher: the robot being built (4 Oct 2026)

The robot being built has one two-wheel flywheel launcher fixed to the frame (POLLEN and NECTAR) and
an 18 in front intake. `AutoStudyTest`'s **"two-wheel launcher"** designs model it. **Nothing on it is
measured yet.** The working values are the spring hood's guesses (2.0 s spin-up from rest, 75° fixed
pitch, NECTAR within 1% of POLLEN's speed, intake 0.35 s per piece, 85% grab), except 0.25 s between
shots, the build team's estimate. Variants sweep spin-up (1–3 s) and shot interval (0.15–0.6 s).

Average alliance AUTO points over **40** runs, partner `partner-preloads-park` (one spring hood, 40 in/s),
normal tiles / 3× friction:

| Auto | Script | 0.25 s/shot | 0.45 s/shot | TIP 3 (0.25, normal tiles) |
|---|---|---|---|---|
| **solo-west** | `one_launcher.py` | **73 / 73.5** | 65 / 66.5 | 34/40 at 25.6 s |
| three-tip-adaptive (re-tuned) | `three_tip_adaptive.py` | 69.5 / 75 | 66 / 65.5 | 27/40 at 25.1 s |
| three-tip-adaptive before | (git history) | 68.5 / 70.5 | 66.5 / 70 | 25/40 |
| solo-tunnel (unchanged) | `solo_tunnel.py` | 69 / 69.5 | 56 / 56 | 26/40 |

- **The time between shots decides the ranking; spin-up doesn't.** Spin-up happens once at the
  start. From 1 to 3 s it moves solo-west and three-tip-adaptive by a point at most. Shot interval:
  solo-west scores 73 / 71 / 70 at 0.15 / 0.25 / 0.35 s, then **56 at 0.6 s**. Three-tip-adaptive
  scores 74 at 0.35 and 57 at 0.6. Measure the shot interval first.
- **With a partner that only leaves** (`partner-leave-park`), solo-west makes 53 and solo-tunnel 56:
  keep solo-tunnel (or staged-three-tip) for that partner.
- left-tunnel and staged-three-tip were not re-tuned. On one launcher at 0.25 s/shot they score 54 and 56.

What changed for one launcher:
- **solo-west** (from solo-tunnel): catch the TIP 1 spill where it lands (CATCH_R), standing still
  while it falls. CollectSeen's look-around had turned the intake away and caught 2. Carry it
  through the tunnel for TIP 2, and fire until the CELL starts to go over (`Tip`); if it doesn't,
  top up at the far FLOWER. For TIP 3, go down the west side to the wall FLOWER and then the GARDEN,
  firing each load from beside its source, with no trip back through the tunnel.
- **three-tip-adaptive**: fire until empty at the left CELL instead of a fixed wait. Decide at the
  far FLOWER whether TIP 2 has happened, not back at LEFT_SHOT. Go straight down the west side and
  fire from SHOT (57.5, 20), below the right CELL. Then drive up the middle of the TIP 1 spill intake
  first, with the webcam after it, and fire again; the GARDEN if TIP 3 still hasn't come. 5.5 s, not
  4 s, to fire the preloads: a slow launcher was cut off before its 4th shot and TIP 1 never came
  (shared with staged-three-tip). Its last card is a park path, so the endgame guard can cut the
  wait before it.

Where the 75° hood scores from (`ShotMapTest`, both kinds 5 of 6 or better): 13–29 in off the
raised CELL's wall, x 13–61, and to 33 in for x ≤ 37. The catch spots (`CATCH_R`, `CATCH_L`) are not
shooting spots.

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
| solo-west | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/solo-west) | [solo-west](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/solo-west.pp) | partner: [partner-preloads-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) |
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
