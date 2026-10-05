# Auto routes from code

`autogen.py` writes an Auto Builder `.pp` from a few lines of Python and exports it with the
Auto Builder's own exporter, so many route ideas can be tried in the simulation quickly. The `.pp`
it writes is still the source of truth: open it in the Auto Builder to see or change the route.

## The qualifier Autos

Two Autos, for the two partners we expect most in qualification. **The robot, since 5 Oct 2026 17:11
UTC: the build team's option 3** (`RobotDesign.buildersOption3`, "builders' option 3 (5 Oct CAD)",
speed 50, no side walls): about 14.5 in square, a 14 in intake across the front (5 in tall, takes a piece
only on contact), the launcher near the back (the piece leaves 4 in behind the centre, 12 in up, at
75°). Alliance AUTO points over 20 runs, normal tiles / tiles with 3× the friction; how many of the 20
made 3 TIPs; and G409, spilled pieces our robot touched before they reached the tiles (per run; must be
0). Links to watch them and their logs: [the root README](../../README.md#qualifier-autos-the-baseline-no-walls).

| Partner | Our Auto (file, script) | Partner's Auto | Points | 3 TIPs | G409 | Updated (UTC) |
|---|---|---|---|---|---|---|
| Can shoot: fires its 4 preloads from the right start at once, parks | Qual-PartnerShootsRight (`qual-right-o3`, `qual_right.py`) | `partner-preloads-right` | **64.5 / 60.8** | **12 / 9** | **0 / 0** | 5 Oct 2026 17:11 |
| Can't shoot: sets its 4 preloads down across the tunnel's north exit, parks | Qual-PartnerStages (`qual-stages-o3`, `qual_right.py`) | `partner-stage-exit` | **54.0 / 56.0** | 0 / 0 (TIP 2 18 / 20, PARK 20 / 20) | **0 / 0** | 5 Oct 2026 17:11 |

`DESIGN="builders' option 3 (5 Oct CAD)" python3 qual_right.py 20 qual-right-o3 qual-stages-o3` exports
and simulates them. The full-width 18 in robot's `qual-right-v3` (69.8 / 68.0, 17 / 16, run 15:49) stays as
the "what a wider intake buys"; qual.py's own Autos (`qual-partner-*`) are another session's.

**Rules for both** (mentor review):

- **Every shot is straight on**: on the CELL's axis (x 57.5), within the launcher's band (ShotMapTest, both
  pieces score 5 in 6). With the launcher near the back (since 5 Oct 15:49): y 17–33 for the right CELL,
  109–125 for the left; before, 13–29 and 113–129. Pieces picked up anywhere are carried there.
- **Waiting for a TIP, face the HIVE** (its camera on it).
- **The tunnel is the road**: square through it under the HIVE (x 57.5), turning only clear of the frame.
- **Let a spill land before driving into it** (G409: "A ROBOT may not catch or deflect a SCORING ELEMENT
  released by a TIPPED HIVE unless and until that SCORING ELEMENT contacts anything else besides that
  ROBOT"). The simulator logs each touch as a `sim: G409` event, and AutoStudyTest's STUDY line ends
  with `G409 x.x (N runs)`.
- **After the last TIP, PARK** (LOADING ZONE, x 0–11, y 94–118), never instead of a TIP: no park path
  after a fire that may still be going (the endgame guard would cut the fire short).
- **The partner only fires from its start, then parks.** It can't tell whether the HIVE has tipped.

### On option 3 (the baseline)

Moving to option 3 is a smaller body (every spot where its front meets something moves 1.75 in:
`qual_right.fit`) and a 14 in intake instead of 16.2. Run 5 Oct 2026 16:20–17:11 UTC, 20 runs,
normal / slow tiles (`O3` and `STAGES` in `qual_right.py`).

**Qual-PartnerShootsRight** (`qual-right-o3`): v3 moved for the body, 64.5 / 60.8, 3 TIPs in 12 / 9,
no G409. Nothing tried beat it:

| Change from qual-right-o3 | 3 TIPs | G409 runs |
|---|---|---|
| No sweep: straight into the GARDEN (one path or two) | 6 / 4 | 0 |
| Sweep lane y 8.5 / 12 / 14–18 (v3's is y 10) | 10 / 9, 8 / 9, 7 / 7–9 | 0 |
| Wait 0 / 150 / 300 ms instead of 500 before driving into TIP 2's spill | 12 / 10, 12 / 9, 11 / 10 | 14 / 1, 1 / 0, 0 |
| Fire TIP 2's spill from y 22; a longer GARDEN fire | 12 / 10, 12 / 9 | 0 |

TIP 3 comes at about 27.5 s when it comes: the narrower intake catches less of each spill, so the
sweep through TIP 1's leftovers is what makes it, and there is no time left to add another source.

**Qual-PartnerStages** (`qual-stages-o3`): qual.py's Stages up to TIP 2, with 500 ms more for TIP 1's
spill to land before driving north into it, then TIP 2's spill and the GARDEN fired at the right CELL,
and PARK:

| Variant | TIP 2 | 3 TIPs | Our PARK | Points | G409 runs |
|---|---|---|---|---|---|
| qual.py's Stages, moved for the body | 20 / 17 | 2 / 1 | 0 / 0 | 53.0 / 49.0 | 18 / 10 |
| + 500 ms for TIP 1's spill, qual-right-v3's tail (a third load) | 18 / 20 | 0 / 0 | 0 / 0 | 48.9 / 51.0 | 0 |
| ... no third load: sweep, GARDEN, PARK | 18 / 20 | 0 / 0 | 17 / 20 | 53.3 / 56.0 | 0 |
| **... straight into the GARDEN, PARK** (`qual-stages-o3`) | 18 / 20 | 0 / 0 | **20 / 20** | **54.0 / 56.0** | **0** |

TIP 2 comes at about 21 s, too late for TIP 3 with this intake; PARK (+5) and the 2 pieces held for
TELEOP are what is left to earn.

### Qual-PartnerShootsRight v3 (the full-width robot, before option 3)

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
| v3 with the conservative intake (5 Oct 14:13: 16.2 in wide, 5 in tall, on contact; every row above had the old one, 18 in and grabbing up to 3 in out) | 70.3 / 67.5 | 17 / 15 | 0.1 / 0 |
| ... and the build team's launcher (5 Oct 15:49: near the back, the piece leaving 4 in behind the centre and 12 in up instead of 4 in ahead and 17 in up) | 69.8 / 68.0 | 17 / 16 | 0.1 / 0.1 |

v2 reached TIP 2's landing 0.1–0.2 s before the last pieces did and drove into 4–5 of them each match
(G409's example C: positioning so falling pieces hit the robot "with an advantageous vector"). Waiting
for them to land costs about 3 points. v2's third load relies on §10.5: a TIP that completes in the 8 s
after AUTO still counts, so it fires up to 28.6 s.

Tried and dropped (on v2, before G409 was counted): leaving for the spill sooner (0–0.4 s after TIP 2
starts: 0–15 / 20), firing from y 16–21, a tunnel lane at x 55 or 60, a webcam pickup facing north after
the GARDEN (the leftovers lie behind the robot), and standing at the catch spot for TIP 2's spill or TIP
3's (catches no more than driving through: the pieces land 2–12 in in front of the intake and scatter
within half a second). Trials in `qual_right.py` (`TRIALS`).

**G409: waiting further back** (run 5 Oct 2026 15:13 UTC). The side-walls session measured where a
spill first lands (`SideWallSpillTest` on `claude/dazzling-maxwell-je04gu`): a plain robot on the CELL's
axis, facing the HIVE, is clear of it with its front face 35 in or less from that wall (36: 2 TIPs in
200 touch it; 38: a third). v3 waits for TIP 2 at N_FIRE, front face 36.5 in. Moved to 35 in (58.0,
115.5), with the extra wait before driving into the spill swept (`qual_right.py`, `G409`):

| v3 variant | Points | 3 TIPs | G409 per run |
|---|---|---|---|
| v3: N_FIRE y 114, wait 500 ms | 70.3 / 67.5 | 17 / 15 | 0.1 / 0 |
| N_FIRE at 35 in, wait 0 ms | 73.8 / 71.3 | 19 / 17 | 1.8 / 0 |
| ... 150 ms | 72.8 / 66.3 | 19 / 13 | 0.1 / 0 |
| ... 300 ms (`qual-right-v3-n35-300`) | 72.3 / 65.5 | 19 / 13 | **0 / 0** |
| ... 500 ms | 71.5 / 65.3 | 19 / 13 | 0 / 0 |

Waiting at 35 in lets 200 ms of the wait come out and keeps zero touches, but it trades TIP 3 on slow
tiles (13 against 15) for normal ones (19 against 17). With no wait the robot still drives into falling
pieces on its way south: standing back fixes the waiting, not the drive through.

### The builders' prototype (5 Oct 2026 CAD)

`RobotDesign.buildersPrototype()`, "builders' prototype (5 Oct CAD)": the build team's CAD read with the
pieces in it as a scale, so every number is ±15% and will move as they build. What it changes from the
design above: about 15 × 15 in including the wheels, an intake about 8 in wide (a roller between the
front wheels), and the two flywheels at the back (about 3 in behind the centre, 8 in up), still firing
forward over the robot. Not modelled yet: the pinwheel at its right-front corner that takes POLLEN out
of a FLOWER (it still takes them with its intake), and the launch angle (the spring hood's 75°).

Where it scores straight on (ShotMapTest, both pieces 5 in 6): the right CELL from y 17–29 (was
13–25), the left from y 113–125 (was 113–129). So v3's firing spots still work. Its smaller body moves
every spot where the front meets something: the start against the wall, the FLOWER, the GARDEN, and
PARK, 1.5 in each (`qual_right.right(robot="proto")`, `qual-right-v3-proto`). Run 5 Oct 2026 15:00 UTC,
20 runs, normal / slow tiles:

| Robot | Points | 3 TIPs |
|---|---|---|
| The design above (18 in, intake 16.2 in; with its launcher as on 15:49) | 69.8 / 68.0 | 17 / 16 |
| The prototype, if its intake were 13.5 in (90% of its frame) | 62.8 / 62.5 | 11 / 10 |
| **The prototype (intake 8 in)** | **57.3 / 54.8** | **6 / 3** |
| **Option 3** (`RobotDesign.buildersOption3`, about 14.5 in, intake about 14 in with funnel wheels at the front corners; launcher as the design above; `qual-right-v3-option3`, run 15:49) | **64.5 / 60.8** | **12 / 9** |

Why: a narrower intake catches less of each spill and of TIP 1's leftovers, so TIP 3 comes later
(about 29 s) or not at all, and the robot is often still busy at 30 s and misses PARK. Without the
route changes for its size it starts off the wall and its front never reaches the FLOWER. The intake's
width is worth asking the build team about before it is fixed.

The commands these Autos use (`CollectSeen`, `LaunchAll`, `IntakeFull`, `LeftCellUp`, ...) exist only in
the simulator so far, and its launcher (2 s spin-up, 0.45 s a shot) and intake (0.35 s a piece) numbers
are unmeasured. How pieces bounce and roll after a spill lands is the least-measured part of the
simulator: film a TIP to check it.

## Open them in the Visualizer

Each link opens the latest pushed `.pp` from this branch in the Visualizer: no login, nothing to
download. It opens as a copy (the team's file is never changed from the browser); to change an
Auto, edit the `.pp` and push, or rerun its script. A push shows up within about 5 minutes. 

To watch a pair together, use the **Together** link: it opens both robots at once in the
Visualizer's multi-path mode, from the pairs in `TeamCode/autos/pairs.json`. Or click **Team Autos**
in the Visualizer's top bar, type the branch (`claude/simulator`), and pick a pair or up to 4 Autos.
**Reload latest** there fetches them again after a push, and **Copy link** shares the view.

| Auto (files) | Together | Our robot | The other robot |
|---|---|---|---|
| Qual-PartnerShootsRight (`qual-right-o3`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-ShootsRight-Option3) | [qual-right-o3](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-right-o3.pp) | partner: [partner-preloads-right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) |
| Qual-PartnerStages (`qual-stages-o3`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Option3) | [qual-stages-o3](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-o3.pp) | partner: [partner-stage-exit](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-stage-exit.pp) |
| What a wider intake buys: Qual-PartnerShootsRight v3 on the full-width robot (`qual-right-v3`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-PartnerShootsRight-v3) | [qual-right-v3](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-right-v3.pp) | partner: [partner-preloads-right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) |

Older Autos, for earlier robots and not re-run: [DEPRECATED.md](DEPRECATED.md).

The links read `TeamCode/autos/` on `claude/simulator`. The simulator and these Autos stay on this
branch, never `master`, so the links always name it.

Also kept: `partners.py` (the reference partners every study runs
against), `snapshots.py` (pictures of the field, from `SnapshotTest`), and `helpers.py`.

```
AUTO_BUILDER_DIR=../visualizer python3 tools/auto-routes/recycle3.py
```

Each script writes its `.pp` into `TeamCode/autos/`, exports the Java next to the other generated
Autos (the simulator's, in `TeamCode/src/test/.../generated/`), and runs `AutoStudyTest`. The
`.pp` files left in `src/test/resources/auto-builder/` are test fixtures. Each current Auto's simulated log
is in [the root README's table](../../README.md#qualifier-autos-the-baseline-no-walls), made by the Simulate Auto
workflow.

## Experiments

Ideas that lost are in `experiments/`, with their `.pp` in `auto-builder/experiments/` and no
Java committed: see `experiments/README.md`.
