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

Also kept: `solo_tunnel.py` (solo-tunnel), `partners.py` (the reference partners every study runs
against), `snapshots.py` (pictures of the field, from `SnapshotTest`), and `helpers.py`.

```
AUTO_BUILDER_DIR=../visualizer python3 tools/auto-routes/recycle3.py
```

Each script writes its `.pp` into `TeamCode/src/test/resources/auto-builder/`, exports the Java
next to the other generated Autos, and runs `AutoStudyTest`. To watch the candidates, build the
review package (`ReviewPackageTest`, see TeamCode/README.md) and open it in AdvantageScope.

## Experiments

Ideas that lost are in `experiments/`, with their `.pp` in `auto-builder/experiments/` and no
Java committed: see `experiments/README.md`.
