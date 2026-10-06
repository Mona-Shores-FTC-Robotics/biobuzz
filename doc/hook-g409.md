# The ramp hook and G409: can it catch a spill without touching it in the air?

> **Archived.** Mentor decision, 6 Oct 2026: the robot catches spills with a fixed Rigid V and the hook is
> redesigned as a FLOWER extractor only (`doc/unified-design.md` on `claude/biobuzz-robot-body-designs-hi386c`).
> This study is the simulator's answer to why: kept as the record, not as current work.

Issue #162, branch `spike/162-hook-g409` off `spike/160-intake-design`. Simulated only, 6 Oct 2026 17:00–17:30 UTC:
no robot was attached and nothing here was measured on one. Every number comes from `AutoStudyTest` on the
simulator as it stands on that branch (TIPs 0.58–1.12 s, pieces rolling as filmed:
[what the simulator knows](what-the-simulator-knows.md)).

**The short version.** No deploy timing, travel time or arm shape makes the hook legal where it waits now:
the wait spot itself is inside the landing. Moving the robot back until nothing is touched (16 in, its front
21.5 in from the wall) costs every point the hook earned: ShootsRight falls from 65.2 to 54.3, TIP 3 from
32 runs of 60 to none, below the plain Flat Intake (54.8, TIP 3 in 10) and far below the Rigid V (64.1,
TIP 3 in 33, G409 in 3). The hook catches the spill only by standing in it.

## The question

The chosen intake ([intake-design.md](intake-design.md), "The design to model") carries the ramp hook,
simulated as the 8 in hook: one arm on the side toward the centre line and a 4 in tall crossbeam across the
robot's width, swung down as our CELL starts to TIP (`RobotDesign.flapsDeploy`, `sideWallsDeployS` 0,
`sideWallsTravelS` 0.3). On its own Autos it scores 65.2 on ShootsRight with TIP 3 in 32 runs of 60, but a
falling spilled piece touches the robot in 29 of 60 runs there, 40 on the angled Stages Auto and 22 on the
wall one. G409 forbids catching or deflecting a spilled piece before it has touched anything else, so that
count must be 0 before the hook may be used in a spill.

## How the runs were made

- Runner: `AutoStudyTest` through `./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*'`, the same
  Autos, partners and settings as [shape-matrix.md](shape-matrix.md):
  `QualRightO3SmallHookAuto,PartnerPreloadsRightAuto@50`, `QualStagesAngledSmallHookAuto,PartnerAngledParkAuto@50`,
  `QualStagesWallSmallHookAuto,PartnerStage19SideParkAuto@50`; `BIOBUZZ_AUTO_PARTNER_DESIGN="spring hood"`,
  `BIOBUZZ_AUTO_PARTNER_SPEED=40`, `BIOBUZZ_AUTO_FRICTION=1`, 60 runs (seeds 1–60) a cell.
- Design: `DHS CAD intake, 14 in roller, ramp hook` (`AutoStudyTest.intakeOptions`). Each setting below is that
  design with one or two numbers changed, named from the environment: `BIOBUZZ_AUTO_DESIGNS="DHS CAD intake,
  14 in roller, ramp hook; deploy 0.9 s, arm 6 in, no crossbeam"` (`AutoStudyTest.variant`; the settings are
  `deploy`, `travel`, `arm`, `crossbeam` / `no crossbeam`, `from tip N`).
- The wait spot: the Autos' hook spot `N_HOOK` is (63.35, 111.25) facing 270, from `qual_shapes.O3_SHAPES`:
  the chassis front 37.5 in from the north wall, the crossbeam 8 in further out at 45.5 in. The Visualizer's
  exporter (node, python) is not on this machine, so the Autos were not regenerated. Instead the simulator moves
  the spot: `BIOBUZZ_AUTO_SHIFT="63.35,111.25:0,dy"` shifts every straight path that starts or ends there by
  `dy` inches toward the wall (`AutoSim.shifted`), the exported Autos untouched. A real change goes through
  the Visualizer and `qual_shapes.py`.
- `from tip 2` (`RobotDesign.hookFromTip`): the hook stays up for TIP 1 and comes down from TIP 2 on. Added
  because the Stages Autos catch TIP 1's spill with the plain intake from a spot an 8 in arm reaches into.
- Which part touched, and when, is read from the runs' timelines (`BIOBUZZ_AUTO_TIMELINE=issues`): the
  simulator says "robot 1 touched" for the chassis and "robot 1's flap or wall touched" for the hook.

Where the spill lands, on the simulator (`SpillLandingTest`, 60 TIPs, fitted to the 3 Oct films): first touch
38 / 41 / 44 in from the wall (p10 / p50 / p90), 0.87 / 1.08 / 1.37 s after the TIP starts. One piece in ten
lands closer to the wall than 38 in.

Columns: points are alliance AUTO points; TIP 3 / TIP 2 are runs of 60 that made that TIP; G409 is runs of 60
with at least one touch of a falling spilled piece. The baseline row is the published number, rerun here.

## (a) Deploy timing and travel

Seconds after our CELL starts to TIP before the hook starts down, swinging for 0.3 s unless said; at the
Autos' hook spot.

| Deploy | ShootsRight: points · TIP 3 · G409 | Stages, angled: points · TIP 2 · G409 | Stages, wall: points · TIP 2 · G409 |
|---|---|---|---|
| 0 s (baseline) | 65.2 · 32 · **29** | 49.4 · 48 · **40** | 47.0 · 48 · **22** |
| 0.3 s | 64.8 · 31 · 29 | 50.3 · 51 · 52 | 47.7 · 50 · 23 |
| 0.6 s | 64.0 · 29 · 28 | 49.3 · 48 · 44 | 47.7 · 50 · 23 |
| 0.9 s | 60.3 · 19 · 20 | 50.0 · 50 · 15 | 47.0 · 48 · 21 |
| 1.2 s | 55.5 · 7 · 20 | 50.0 · 50 · 17 | 47.0 · 48 · 21 |
| 1.5 s | 54.8 · 6 · 20 | 50.0 · 50 · 16 | 47.0 · 48 · 21 |
| 1.8 s, 2.1 s | 54.7 · 6 · 20 | 50.0 · 50 · 16 | 47.0 · 48 · 21 |
| 0 s, swing 0.15 s | 64.8 · 31 · 29 | 49.4 · 48 · 40 | 47.0 · 48 · 21 |
| 0 s, swing 0.6 s | 64.8 · 31 · 29 | 49.4 · 48 · 40 | 47.0 · 48 · 24 |
| 0.9 s, swing 0.15 s | 62.6 · 25 · 23 | 49.5 · 49 · 29 | 47.7 · 50 · 20 |

What it says: from 0.9 s the hook's own touches are gone, and so is its gain (TIP 3 falls to the plain
robot's 6–7; a hook that waits for the spill to land catches a spill that has already scattered, the same
finding as the Side Rails'). The 16–21 runs that remain at any later deploy are the **chassis**: the robot's
front face at the hook spot, 37.5 in from the wall, is inside the landing, and pieces hit it in the air
(the timelines: on ShootsRight 23 chassis touches and 15 hook touches at "It lands", all at TIP 2). The
travel time changes nothing.

On the angled Stages Auto the hook's 40 runs are mostly **TIP 1**: 118 of its 132 touches are the hook,
swung down at the catch spot where the Auto waits for TIP 1's spill with the plain intake (the Flat Intake
has G409 0 there). Keeping the hook up for TIP 1 (`from tip 2`) takes that Auto to 50.0 · 50 · 16, the chassis
touches at TIP 2 again. ShootsRight and the wall Auto are unchanged by it.

## (c) Arm length and the crossbeam

At deploy 0, at the Autos' hook spot.

| Shape | ShootsRight: points · TIP 3 · G409 | Stages, angled: points · TIP 2 · G409 | Stages, wall: points · TIP 2 · G409 |
|---|---|---|---|
| 8 in arm, crossbeam (baseline) | 65.2 · 32 · 29 | 49.4 · 48 · 40 | 47.0 · 48 · 22 |
| 7 in arm, crossbeam | 66.1 · 34 · 47 | 48.8 · 47 · 40 | 47.0 · 48 · 21 |
| 6 in arm, crossbeam | 66.6 · 36 · 57 | 49.3 · 48 · 40 | 47.0 · 48 · 21 |
| 8 in arm, no crossbeam | 59.3 · 16 · 21 | 49.7 · 49 · 35 | 47.0 · 48 · 22 |
| 7 in arm, no crossbeam | 59.0 · 15 · 21 | 49.3 · 48 · 34 | 47.0 · 48 · 21 |
| 6 in arm, no crossbeam | 58.7 · 14 · 21 | 49.6 · 49 · 27 | 47.0 · 48 · 21 |

The crossbeam is the catcher and the culprit at once: it is what the spill lands against (TIP 3 in 32–36 runs
with it, 14–16 without) and what the falling pieces hit (a shorter arm puts it deeper in the landing: 57 runs
at 6 in). Without it the hook's touches fall to the chassis floor of 21 and its gain halves. On the two
Stages Autos no shape scores differently from any other: the hook is worth nothing there (the plain
Flat Intake: 51.6 and 47.3).

## (b) Where the robot waits

The hook spot moved `dy` inches back toward the wall, with the hook up for TIP 1 (`from tip 2`). Front face
and crossbeam distances are from the wall.

| Spot (front · crossbeam) | Shape | ShootsRight: points · TIP 3 · G409 | Stages, angled: points · TIP 2 · G409 | Stages, wall: points · TIP 2 · G409 |
|---|---|---|---|---|
| as drawn (37.5 · 45.5 in) | 8 in, crossbeam | 65.2 · 32 · 29 | 50.0 · 50 · 16 | 47.0 · 48 · 21 |
| 8 in back (29.5 · 37.5) | 8 in, crossbeam | 52.8 · 1 · 29 | 50.1 · 50 · 1 | 47.0 · 48 · 2 |
| 8 in back | 6 in, crossbeam | 52.8 · 2 · 4 | 50.1 · 50 · 1 | 47.0 · 48 · 2 |
| 8 in back | 8 in, no crossbeam | 58.5 · 13 · **0** | 50.1 · 50 · 1 | 47.0 · 48 · 2 |
| 12 in back (25.5 · 33.5) | 8 in, crossbeam | 54.2 · 1 · 1 | 49.8 · 50 · **0** | 47.0 · 48 · 1 |
| 12 in back | 6 in, crossbeam | 54.3 · 2 · 1 | 49.8 · 50 · **0** | 47.0 · 48 · 1 |
| 12 in back | 8 in, no crossbeam | 56.3 · 5 · **0** | 49.8 · 50 · **0** | 47.0 · 48 · 1 |
| **16 in back (21.5 · 29.5)** | 8 in, crossbeam | 54.3 · 0 · **0** | 49.8 · 50 · **0** | 47.0 · 48 · **0** |
| 16 in back | 6 in, crossbeam | 54.3 · 0 · **0** | 49.8 · 50 · **0** | 47.0 · 48 · **0** |
| 16 in back | 8 in, no crossbeam | 54.6 · 1 · **0** | 49.8 · 50 · **0** | 47.0 · 48 · **0** |
| 20 in back (17.5 · 25.5) | 8 in, crossbeam | 55.3 · 1 · **0** | 49.7 · 50 · **0** | 47.0 · 48 · **0** |
| 20 in back | 8 in, no crossbeam | 54.9 · 0 · **0** | 49.7 · 50 · **0** | 47.0 · 48 · **0** |

What it says:

- **8 in back puts the crossbeam at 37.5 in from the wall, and it is still hit in 29 runs**: all 42 touches are
  the hook at "It lands", the one piece in ten that lands inside 38 in. The chassis, now 29.5 in out, is clear.
  And the catch is gone: TIP 3 in 1 run. The landing's p10 to p90 is only 6 in wide, so the crossbeam is either
  in it or behind it, never short of it with the spill still rolling into the hook.
- **Legal on all three Autos: 16 in back or more**, any shape, where the hook adds nothing (ShootsRight 54.3,
  TIP 3 never; the three Autos score as the plain Flat Intake does on its own routes, or a little under).
- The remaining single touches at 12 in back are the chassis at TIP 3 on the wall Auto (waiting at S_FIRE,
  as the Rigid V did) and at TIP 2 on ShootsRight: 1 run in 60 each, a foul risk, not zero.

## What it costs, and the recommendation

| Setting | Legal (G409 0 on all three)? | ShootsRight points · TIP 3 | Against the baseline hook (65.2 · 32) | Against the Rigid V (64.1 · 33 · G409 3) |
|---|---|---|---|---|
| Deploy at 0.9–1.5 s, as drawn | no (16–21 runs: the chassis) | 60.3–54.8 · 19–6 | −5 to −10 pts | −4 to −9 |
| No crossbeam, as drawn | no (21 runs: the chassis) | 59.3 · 16 | −6 | −5 |
| 8 in back, no crossbeam, from TIP 2 | no (ShootsRight 0, Stages 1 and 2) | 58.5 · 13 | −7 | −6 |
| 12 in back, no crossbeam, from TIP 2 | no (wall Stages 1) | 56.3 · 5 | −9 | −8 |
| **16 in back, from TIP 2 (any shape)** | **yes** | **54.3 · 0** | **−11** | **−10** |

**Recommendation.** Do not use the hook as a spill catcher. Every legal setting scores what the robot scores
with no guide at all, and every setting that scores stands in the landing. The Rigid V catches the same spill
from where the plain route already waits (G409 in 3 of 60; those 3 are its flaps at TIP 3, a route fix) and
out-scores every legal hook setting by 10 points on ShootsRight. That is the mentor's decision of 6 Oct 2026
(`doc/unified-design.md`): the Rigid V for spills, the hook redesigned as a FLOWER extractor and nothing else.

If the hook is ever tried for spills again, the simulator's reasons it fails are geometric, not timing: the
spill lands in a band 6 in wide and scatters from there, so a catcher is either in the band (touched in the
air) or behind it (missed). What would change the answer is a measured landing band wider or farther out than
the films' 38–44 in, or a hook low enough that pieces fly over its front (the ramp's 1 in lip, which the 4 in
plates here do not model).

## Sources

- Baseline, deploy, travel and shape rows: `AutoStudyTest` on `spike/162-hook-g409`, 6 Oct 2026, 60 runs a cell
  (`BIOBUZZ_AUTO_DESIGNS` variants of `DHS CAD intake, 14 in roller, ramp hook`).
- Spot rows: the same with `BIOBUZZ_AUTO_SHIFT=63.35,111.25:0,{8,12,16,20}`.
- Which part touched: `BIOBUZZ_AUTO_TIMELINE=issues` on the baseline and the spot runs.
- Where the spill lands: `SpillLandingTest` (`./gradlew :TeamCode:testDebugUnitTest --tests '*SpillLandingTest*' -i`).
- Hook spot: `tools/auto-routes/qual_shapes.py` `O3_SHAPES`; the Autos `QualRightO3SmallHookAuto`,
  `QualStagesAngledSmallHookAuto`, `QualStagesWallSmallHookAuto` (`N_HOOK` at 63.35, 111.25).
- Published comparisons: [shape-matrix.md](shape-matrix.md) (Flat Intake, Rigid V, Ramp Hook) and
  [intake-design.md](intake-design.md) (the 14 in roller with and without the hook).
- All of it rests on the simulator's guesses: [what the simulator knows](what-the-simulator-knows.md).
