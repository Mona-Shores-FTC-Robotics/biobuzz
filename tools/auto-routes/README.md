# Auto routes from code

`autogen.py` writes an Auto Builder `.pp` from a few lines of Python and exports it with the
Auto Builder's own exporter, so many route ideas can be tried in the simulation quickly. The `.pp`
it writes is still the source of truth: open it in the Auto Builder to see or change the route.

| Script | Writes |
|---|---|
| `duo_lz.py` | `duo-lz-south.pp`, `duo-lz-north.pp` |
| `three_tip_adaptive.py` | `three-tip-adaptive.pp` |
| `partners.py` | `partners/partner-leave-park.pp`, `partners/partner-preloads-park.pp` |
| `lean_duo.py` | `lean-south.pp`, `lean-north.pp` |
| `lean_opportunist.py` | `lean-opp-south.pp`, `lean-opp-north.pp` |
| `snapshots.py` | pictures of the field at chosen moments, from `SnapshotTest` |
| `home_duo.py`, `convoy.py`, `rally.py`, `solo_shuttle.py`, `four_tip_adaptive.py` | experiments that lost, in `experiments/` (their Java is not committed) |

```
AUTO_BUILDER_DIR=../visualizer python3 tools/auto-routes/duo_lz.py
```

Each script writes its `.pp` into `TeamCode/src/test/resources/auto-builder/`, exports the Java
next to the other generated Autos, and runs `AutoStudyTest` (10 runs on the spring-hood robot).
Results and what they mean: TeamCode/README.md, "Autos on the spring-hood robot".
