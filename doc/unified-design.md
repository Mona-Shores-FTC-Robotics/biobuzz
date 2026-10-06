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

## The Rigid V: decided

**The V as drawn:** tips 17.8 in apart, 2.84 in ahead of the face, 4 in tall, both flaps fixed. The fallback is 45°,
if G409 touches get called on a real field. Decided by the Intake Design chat, 6 Oct 2026, from its angle sweep
(`doc/intake-design.md` on `spike/160-intake-design`).

60 runs on ShootsRight. The angle is measured from straight ahead, so a steeper V is shorter:

| V | Points | 3 TIPs (of 60) | G409 runs |
|---|---|---|---|
| **31° (as drawn)** | **69.9** | **47** | 11 |
| 35° | 68.3 | 43 | 6 |
| 45° | 66.9 | 40 | 4 |
| 60° | 64.3 | 34 | 4 |
| 16 in tips | 67.0 | 40 | 6 |

The angle makes no real difference with the angled partner (49.6–51.8). Going to 45° costs about 3 points for about a
third of the touches.

## The Rigid V so far

Measured on the V as drawn: tips 17.8 in apart, 2.8 in ahead, 31° from straight ahead. 60 runs on each guide's own
routes, run 6 Oct 2026 22:00 UTC; Intake Design is running the angle.

| Question | Answer |
|---|---|
| **Flap height** | **Keep 4 in.** At 2.5 or 2.2 in, ShootsRight drops from 69.5 to 66.8–67.2 points (3 TIPs in 43 → 35–36 runs of 60), and the wall partner's Auto drops from 55.7 to 52.0–52.3 (3 TIPs in 21 → 12–13). G409 halves on ShootsRight (8 → 3–4 runs) but not on the wall partner's Auto. Pieces bounce and fall, not only roll, so the taller flap earns its keep. |
| **Flap bounce** | **Up to 0.3 is fine** (the same as 0.1). At 0.5 the wall partner's Auto drops to 52.3 (3 TIPs in 12 instead of 21), while ShootsRight holds. Measure the bare aluminium, or fit the foam as cheap insurance. |
| **One flap or none, wall partner's west lane** | **Don't use the west lane.** There, the left flap blocks it (32.7–33.0 points), and right-only or no flaps get 47.3. The V's own route, sweeping up the row, gets **55.7, with 3 TIPs in 21 of 60**. Swappable plates aren't needed. |
| **Holding TIP 1's spill at the drop zone** | **No.** On the wall partner's Auto it scores 58.0 against 55.7, with the same 21 TIP 3s, but G409 goes from 18 to 51 runs. |
| **Centre line** | The drawn V, like the long V, reaches over it at about 8.6 s on both Stages routes, in the turn out of the tunnel. The fix is the route turning 2 in further west (N_TURN at x 55.5; `guide_routes.py`, `turning_west`). With it, no log crosses, and the score holds: angled partner 52.3, wall partner **56.3, with 3 TIPs in 23 of 60**. |

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

## Limelight mount (from the Limelight Localization chat, 6 Oct 2026)

**Keep 19429's measured mount.** It was measured on the robot and checked against taped field positions
(README § "HIVE tracking and camera localization, end to end (#157)" on `feat/157-hive-and-pose`). It is also
`CameraMount`'s default in code.

**Mount.** Model frame: +x forward, +y left, +z up, inches, origin on the floor under the chassis centre.

| | Value |
|---|---|
| Lens position | x **4.0**, y **0**, z **14.0**. On the centreline, 3.6 in behind the front face. |
| Pitch | **45° up.** AdvantageScope rotations `[y: −45, z: 0]`. |
| Yaw | **0** |
| Camera | Limelight 3A, 640 × 480. Field of view 54.5° across, 42° tall. At 45° pitch it sees 24°–66° above horizontal. |

**Where it sees both CELLs.** Ranges are from the lens to each tag row, with the robot facing the HIVE. Row heights are
measured: UP 50.2 in, DOWN 35.0 in.

| Robot at | UP row | DOWN row | Both rows in view? |
|---|---|---|---|
| Firing spot (57.5, 24) | 30 in, 50° up | 32 in, 33° up | Yes |
| Firing spot (57.5, 114–119) | 24–29 in, 51–56° up | 25–30 in, 35–40° up | Yes |
| Start spot (y ≈ 4) | in view | 21° up, below the frame | No. Measured, and expected. |

**Not checked yet: the tunnel.** Send the tunnel's poses (where the robot sits and which way it faces) and I'll run
the same check.

**Clearance (assumed: the turret isn't drawn yet).**
- **Field of view.** Ahead of the lens, nothing may rise above the camera's lowest ray, within 27° either side of
  straight ahead. That means the turret, a carried FLOWER, the scorer at any angle, and the extractor. The limit is
  z ≤ 14.0 + 0.445 · (x − 4.0), giving 14.0 in at the lens, 15.6 in at the front face, and 16.1 in at the roller's
  front.
  - Below that line the view is clear. The stowed extractor tops out at 8.8 in, well under it.
- **Behind the lens, anything goes.** Only the camera body and the USB-C cable need room, and the cable leaves toward
  the back of the robot, away from the turret.
- **If the turret must sweep through this space,** move the camera rather than tilt it down. Keep the 45° pitch:
  lowering it loses the UP row at the firing spots. Moving the lens back changes the ranges above, so tell me the new
  position and I'll re-check.

## FLOWER extractor (from the Flower Extracter chat, 6 Oct 2026)

Full write-up: `doc/robot-cad.md` on `claude/robotics-meeting-notes-lq2y55`.

**Concept.** The extractor pivots on the roller's own shaft:
- **Arms:** two 1/8 in aluminium arms, 1.8 in each side of centre, each on a flanged bearing (goBILDA
  1611-0514-0008) on the roller's 8 mm shaft. The roller has two short gaps there, about 10 mm each.
- **The FLOWER block:** carried on a cross shaft between the arms. Its back edge is 2.5 in ahead of the roller's
  front, so POLLEN come off it straight into the roller.
- **No walls.** A POLLEN passes between the arms, which have 3.5 in clear.
- **It stays within the roller's width,** so the front corners are left to the V.

**Outlines.** Robot frame, inches: x right of centre, up from the tiles, forward from the front face.

| | x | Up | Forward |
|---|---|---|---|
| Pivot (the roller axle) | 0 | 3.35 | 1.0 |
| Deployed (0°) | −1.86..+1.86 | 0.70..3.78 | 0.55..5.80. 20.9 in long overall, within R105's 24. |
| The block, deployed | ±1.36 | 0.70..1.35 | 4.44..5.84 |
| Stowed (125°, folded over the roller) | −1.86..+1.86 | 2.73..8.80 | −0.11..1.63. Within the roller's reach; the start stays 17.96 in long. |

**Travel:** 0 to 125°. Checked every 5°: clear of the robot, the V and side plates, the roller motor and belt, and
the drive pods. Past about 140° the block hits the intake's upper cross-channel.

**Still to design:** the drive, a servo above the roller on the right driving the right arm through a short link,
with hard stops at 0° and 125°.

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
