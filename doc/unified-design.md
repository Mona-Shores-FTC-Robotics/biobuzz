# One robot: Rigid V, FLOWER extractor, FLOWER scorer

**Decision (mentor, 6 Oct 2026, 21:15 UTC).** The robot catches spills with a **fixed Rigid V**, not a hook. The hook
stops being a spill catcher. It becomes a **FLOWER extractor** and nothing else. All chats now work toward one
robot: Rigid V + FLOWER extractor + NECTAR/POLLEN FLOWER scorer, inside one envelope.

## Why

On the current (filmed) physics, 60 runs a case, each guide on a route drawn for it
([the report](../sim-review/body-evaluation.html), [online](https://claude.ai/artifact/Q1AFHpfZZ9L16yzNccWvQv)):

- **Partner shoots:** the long Rigid V (18 in, 30°) gets 68.8 points, 3 TIPs in 41 of 60, PARK 57. The Ramp Hook gets
  66.3, 35 and 51.
- **G409:** falling pieces touch the hook in 28–51 runs of 60 on every Auto, wherever it waits and whether or not it
  comes down at TIP 2. About half the touches land on the robot's own face. The V is touched in 0–9.
- **Staging partner:** the hook's one advantage was its route (fire TIP 1 from S_FIRE, hold the spill at the drop
  zone). Given the same route, the V made 3 TIPs with the wall partner in its first test match. Full numbers to follow.

## Archived: the hook as a spill catcher

Kept for the record and not developed further: the hook spots and spot sweeps, the hook at TIP 2, the dual and
late hooks, hook staging of preloads, and the hook's G409 work. Their routes stay in
`TeamCode/src/test/resources/auto-builder/experiments/` and `tools/auto-routes/` (`qual_shapes.py`'s small hook,
`keep4.py`, `qual_stage.py`, `guide_routes.py`'s `stages_hook*`). [Ramp hook](ramp-hook.md)'s spill half is
superseded; its FLOWER half carries on below.

## Who does what

| Part | Owner (chat) | What it decides |
|---|---|---|
| **Rigid V** | Intake Design | The V's angle, length, height and shape within the envelope, and the intake behind it. The CAD's drawn V (17.8 in tips, 2.8 in ahead, `cad/intake-b/`) is the starting point. |
| **FLOWER extractor** | Flower Extracter | The old hook redesigned for FLOWERs only: no walls, two arms for rigidity, far lower than 8 in. It must stow inside the 18 in start cube with the V fitted. |
| **FLOWER scorer** | Pivoting arm nectar scorer | The NECTAR/POLLEN FLOWER scorer, in the same envelope. |
| **Simulator and routes** | FTC BIOBUZZ robot body designs (this branch) | Runs every candidate through the three qualifier Autos (60 runs), and draws each Auto for the unified robot. |
| **Simulator physics, baselines** | Claude/Simulator Baseline | Makes the Rigid V robot the baseline. Models the extractor and scorer when their geometry exists. |

## The envelope

- **R102:** 18 × 18 × 18 in at the start.
- **R105:** 18 × 24 in once started.
- **The body as drawn:** 15.12 × 15.24 in, plus the roller 1.94 in ahead.
- **The V takes the front corners.** The extractor and scorer must fit around it, or fold.
- **Every part states its stowed and deployed outline,** so the simulator can draw and collide it.

## Rules every chat keeps

- **The Pedro field frame, in inches.** It is the only frame (CLAUDE.md).
- **Numbers come from the simulator at 60 runs on the three qualifier Autos**, each part on a route drawn for it,
  or from a cardboard or field test. Never from a guess.
- **PARK is non-negotiable in quals.** No route trades it away.
