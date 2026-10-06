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

## The Rigid V so far

Measured on the V as drawn: tips 17.8 in apart, 2.8 in ahead, 31° from straight ahead. 60 runs on each guide's own
routes, run 6 Oct 2026 22:00 UTC; Intake Design is running the angle.

| Question | Answer |
|---|---|
| **Flap height** | **Keep 4 in.** At 2.5 or 2.2 in, ShootsRight drops from 69.5 to 66.8–67.2 points (3 TIPs in 43 → 35–36 runs of 60), and the wall partner's Auto drops from 55.7 to 52.0–52.3 (3 TIPs in 21 → 12–13). G409 halves on ShootsRight (8 → 3–4 runs) but not on the wall partner's Auto. Pieces bounce and fall, not only roll, so the taller flap earns its keep. |
| **Flap bounce** | **Up to 0.3 is fine** (the same as 0.1). At 0.5 the wall partner's Auto drops to 52.3 (3 TIPs in 12 instead of 21), while ShootsRight holds. Measure the bare aluminium, or fit the foam as cheap insurance. |
| **One flap or none, wall partner's west lane** | **Don't use the west lane.** There, the left flap blocks it (32.7–33.0 points), and right-only or no flaps get 47.3. The V's own route, sweeping up the row, gets **55.7, with 3 TIPs in 21 of 60**. Swappable plates aren't needed. |
| **Holding TIP 1's spill at the drop zone** | **No.** On the wall partner's Auto it scores 58.0 against 55.7, with the same 21 TIP 3s, but G409 goes from 18 to 51 runs. |
| **Centre line** | The drawn V, like the long V, reaches over it at about 8.6 s on both Stages routes, in the turn out of the tunnel. The fix is the route turning 2 in further west (N_TURN at x 55.5; `guide_routes.py`, `turning_west`). Being rerun. |

## One model of the whole robot

The user's ask (6 Oct 2026): **one CAD assembly, and one AdvantageScope model made from it**. It holds the decided Rigid V,
the FLOWER scorer, the FLOWER extractor, and the Limelight at a mount angle that keeps clear of the turret.

- **Assembly and model: the Flower Extracter chat.** It holds the CAD (`cad/intake-b/` on
  `claude/robotics-meeting-notes-lq2y55`) and the STEP-to-`robot.glb` pipeline. It assembles each part from its
  owner's outline, and exports the AdvantageScope model (the `Robot_BIOBUZZ` folder: the model file, its config, and
  the stowed and deployed poses).
- **Limelight mount: the Limelight Localization chat.** The pitch toward the HIVE's tags (CLAUDE.md: AprilTags only,
  pitched up at the HIVE), the position, and the turret's swept volume it has to stay clear of.
- **The simulator** switches to that model once it exists.

**The Flat Intake baseline is dropped** (mentor, 6 Oct 2026). Simulations now run only the Rigid V candidates, then
only the chosen V.

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
