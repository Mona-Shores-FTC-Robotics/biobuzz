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

## The user's decisions, 6 Oct 2026 (evening)

Passed on by the Flower Extracter / robot CAD chat.

1. **The FLOWER extractor stays at the front.**
   - It's on its own shaft, 2.4 in ahead of the face and 4.5 in up.
   - The roller floats straight up 1.3 in, so a NECTAR passes under it.
   - Drawn and swept in `cad/intake-b/` on `claude/robotics-meeting-notes-lq2y55`, commit 70b2561.
   - A rear extractor was weighed and dropped: entering from the rear reverses the J, so the robot couldn't shoot
     while extracting.
2. **Shoot while extracting.** The turret fires from the seat while the extractor feeds roller → lane → J → turret.
   The Autos are to be built around that, in simulation.
3. **Simple, reliable hardware and ideas** over clever ones.
4. **The Limelight is fixed, facing forward:** no pan servo, not on the turret.
5. **The FLOWER scorer (the NECTAR-capping cage) is shelved,** to revisit later.
6. **The transfer gets fully CADed next,** by the CAD chat, from the transfer chat's design.

The V is unchanged: its roots moved 4 mm forward, its tabs are lower, and the tips are still 2.84 in ahead. The
starting length is 17.96 in.

## Who does what

| Part | Owner (chat) | What it decides |
|---|---|---|
| **Rigid V** | Intake Design | The V's angle, length, height and shape within the envelope, and the intake behind it. The CAD's drawn V (17.8 in tips, 2.8 in ahead, `cad/intake-b/`) is the starting point. |
| **FLOWER extractor** | Flower Extracter | The old hook redesigned for FLOWERs only: no walls, two arms for rigidity, far lower than 8 in. It must stow inside the 18 in start cube with the V fitted. |
| **FLOWER scorer** | Pivoting arm nectar scorer | **Shelved** (the user, 6 Oct evening). Before that: **the rear scorer is dropped for now** (no wall rollers, bumper or servo panel: it takes no space). The chat is now looking at a **front NECTAR-capping assist**: a cage over the FLOWER's top while the extractor holds the robot on the FLOWER, so the turret can cap from that seat. |
| **Transfer, intake to turret** | Intake-to-turret transfer (session_019KDmb4SvV5VjdaM2USg71K) | How pieces get from the roller to the turret at any turret angle, holding up to 4 (G407), NECTAR and POLLEN. The first idea to weigh: feed through the turret's rotation axis, with a single-file floor channel as the magazine. |
| **Simulator and routes** | FTC BIOBUZZ robot body designs (this branch) | Runs every candidate through the three qualifier Autos (60 runs), and draws each Auto for the unified robot. |
| **Simulator physics, baselines** | Claude/Simulator Baseline | Makes the Rigid V robot the baseline. Models the extractor and scorer when their geometry exists. |

## The baselines (claude/simulator, 6 Oct 2026)

The three qualifier baselines are the drawn V on these routes, published from `claude/simulator` only:

| Baseline | Route | Points | 3 TIPs (of 60) | Notes |
|---|---|---|---|---|
| `qual-right-v` | `qual-right-o3-rigid-v-park` | **71.2** | **48** | PARK 58, G409 8 runs, no problems |
| `qual-stages-angled-v` | `qual-stages-angled-rigid-v-18-30-t555` | 51.6 | – | TIP 2 in 53, PARK 55, G409 14. 4 problem runs, all where TIP 1 failed (see below). |
| `qual-stages-wall-v` | `qual-stages-wall-rigid-v-18-30-sweep90-t555` | **55.3** | **26** | G409 16, no problems |

Refitted for the 15.12 in body (front 7.56 in ahead of centre), 6 Oct 2026, 60 runs. **The angled Auto's 4 HIVE-frame
clips** come only when TIP 1 fails (2 of our 4 preloads miss). The route then goes back through the tunnel holding 4,
and with the turn at x 55.5 the flaps clip the west foot bar at 26.7 s. The simulator chat is giving the route a
branch for when TIP 1 hasn't happened; the clips vanish at the launcher's target accuracy.

Built by `baselines_v.py`; design "rigid V" (= the drawn V).

**Two fixes the simulator chat made, which change these from the numbers in the next sections:**
- **Partner shoots (ShootsRight):** the routes were fitted for the old 14.5 in body. With the drawn V's 15.12 in body,
  every run touched the far FLOWER. Refitted for the real body, it scores 71.2.
- **Wall partner:** the row sweep's start point and its 180° turn put the V over the parked partner. The robots collided
  in most runs, so 55.7 and 56.3 were not legal scores. Starting 2 in short of the row, with a tighter turn, there are
  no collisions.

**From now on, a route with any problem is disqualified.** That means a collision, or a HIVE or FLOWER hit: the
study line's PROBLEMS, and `problemRuns` in DeepDive's `cases.csv`. The numbers below were measured before this
check.

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

**The tunnel (the robot on x 57.5, from the qualifier routes).** Tag faces: an AUDIENCE CELL's tags face the
audience (−y), and a SCORING CELL's face the scoring table (+y). So the camera sees a CELL only when the robot is
facing that CELL's tags and they are 24–66° above the lens. Robot centre y:

| Pass | Sees | Blind |
|---|---|---|
| North-bound, facing 90° | RED_AUDIENCE: the UP row up to y ≈ 38, the DOWN row up to y ≈ 47 | y ≈ 47 → the turn at 104. RED_SCORING's tags face away. |
| After the turn, facing 270° | RED_SCORING: the DOWN row from N_TURN (54° up), the UP row from y ≈ 106 (69° up at 104, just above the frame) | — |
| South-bound, facing 270° | RED_SCORING: the UP row down to y ≈ 106, the DOWN row down to y ≈ 98 | y ≈ 98 → the turn after 38. RED_AUDIENCE's tags face away. |

So **each pass is about 55–60 in blind, about 1.2 s at 50 in/s, plus the turn**, and the Pinpoint carries it. That's
expected and fine.

These are calculated from the frame edges. Near the edges the tags are seen steeply from below, so detection may
end a few inches sooner than the table says. The blue HIVE, 26 in to the side, is outside the 27° half-width
throughout.

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

**Decided (the user, 6 Oct 2026): the Limelight is fixed, facing forward.** No pan servo, and not on the turret; the mount above stands.

**Shelved with the FLOWER scorer: the NECTAR-capping cage.** Kept for when it comes back. It may block the camera while seated at a FLOWER (the user's ruling, 6 Oct 2026). Stowed
or driving, it stays under the ceiling above. What it means for the code:
- **Localization: nothing to do.** With no tags in view, there's no seed, no relocalize and no would-relocalize, and
  the Pinpoint carries the pose. A partly blocked view is fine too, because `CellFix.fit` uses whichever tags remain.
- **TIP tracking: one change needed.** `HiveTracker` takes a settled CELL vanishing while the robot holds still as a
  TIP starting. That's exactly what the cage looks like when it drops while seated, so it would count a false TIP
  and assume the other CELL is up 2.5 s later. When the scorer subsystem is written, it must tell `robot.hive` that
  the view is blocked while the cage is down, and `HiveSubsystem` must not count a loss during that time.
  - The hook is a read-only accessor the scorer exposes (`viewBlocked()`), which `HiveSubsystem` reads. Add it in
    the same PR as the cage.

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

## Capping a FLOWER from the extractor's seat: open questions for the user

The seat: the robot on the FLOWER's centreline, the FLOWER's centre 7.09 in ahead of the face (X 14.65 in the model
frame).

- **Shot distance.** The launcher exit is about 15.3 in from the FLOWER's centre: turret axis 10.73 in behind the face,
  exit 2.5 in ahead of the axis. The scorer's shot sim gives about 51% capping with a cage at 16 in, and better
  closer. The only lever is the extractor block's gap to the roller, now 2.5 in: 1.0 in gives 13.8 in, and 0 gives
  12.8 in. That trades against extraction, which is untested at those gaps.
- **The Limelight.** A cage over the FLOWER's top, at about 21.9 in, would sit inside the camera's keep-clear zone,
  whose ceiling at the FLOWER is 18.7 in. That matters only while the cage is engaged.

## Transfer (from the intake-to-turret transfer chat, 6 Oct 2026)

**Owner:** the Intake-to-turret transfer chat (session_019KDmb4SvV5VjdaM2USg71K), issue #164. Full write-up, three
concepts, sketches and a cardboard checklist: `doc/transfer.md` on `spike/164-transfer`. Not yet on a robot.

**Concept: a floor lane that is the magazine, and a J-kicker up the turret axis.**
- The roller throws each piece up a 25° ramp into a 4.2 in wide lane along the centreline. The floor is 0.9 in up,
  with two polycord strands on it, driven off the roller's shaft.
- At the back, a 48 mm gecko wheel floats on a banded arm over a fixed J-curve.
  - Stopped, it is what the queue rests against.
  - Running, it kicks each piece straight up the turret axis, through a hollow bearing (goBILDA 105 mm ID turret),
    into the launcher's throat.
- So it works at every turret angle, with **one new motor** (Yellow Jacket 1620 rpm) and no sensor.

**Outline** (model frame: +x forward, +y left, +z up, inches, origin on the floor under the chassis centre):

| Part | x | y | z |
|---|---|---|---|
| Ramp | 5.8 .. 7.2 | −2.1 .. 2.1 | 0.25 .. 0.9 |
| Lane, keep-out inside the walls | −1.3 .. 7.2 | −2.35 .. 2.35 | 0.25 .. 5.0 |
| Lane drive belt, outside the left wall, from a pulley on the roller shaft at y +2.6 | 5.2 .. 9.0 | 2.35 .. 2.9 | 0.2 .. 3.9 |
| J-wheel, arms, pivot (full float) | −2.3 .. 0.8 | −2.6 .. 2.6 | 1.9 .. 6.3 |
| Outer J and chute, keep-out | −5.1 .. −1.0 | −2.2 .. 2.2 | 0.25 .. 6.6 |
| J motor (anywhere in this box) | −4.0 .. 2.0 | 2.6 .. 4.6 | 0.8 .. 4.0 |
| Turret bearing | centred on (−3.17, 0) | | bottom at 6.6 or higher |

**Key numbers**

| | |
|---|---|
| Hand-off to the launcher | On the turret axis (−3.17, 0) for NECTAR, and 0.4 in behind it for POLLEN. Crossing z 6.6 going up at about 70 in/s |
| Intake to ready to fire | 0.35 s |
| Fire command to the piece leaving the transfer | 0.15 s |
| Shot interval | **0.25 s**, set by the roller's power while firing (0.15–0.4 s possible). The launcher's recovery is the limit |
| Volley of 4 | about 0.9 s |
| Holds | 4 POLLEN, 3 NECTAR, 3–4 mixed, **never 5**: G407 by geometry. At the start, 4 preloads of any mix |

**What it needs from the others**
- **From the robot:** the centreline strip, 4.7 in wide from x −5.1 to 7.2, kept free below z 5.0. So the battery,
  the hubs and any cross-channels go to the sides or above 5.2 in.
- **From the launcher:**
  - its turret on the axis at x −3.17, with its bearing 6.6 in up or higher;
  - a throat with a mouth about 4 in across, centred on the axis.
- **Motor ports:** the transfer brings the count to 8 (4 drive, roller, J, flywheel, turret). A second flywheel motor
  means a servo-driven turret.

**Flag for the intake.** The pieces are stiff plastic balls. A NECTAR (3.62 in) is taller than the roller's axle
(3.35 in), with the roller's bottom at 2.4. So a fixed roller there can't take a NECTAR. It needs to float.

## The intake roller floats (Intake Design chat, 6 Oct 2026)

Flagged by the transfer chat (#164): a NECTAR (3.62 in) is taller than the fixed roller's axle (3.35 in), so a
fixed roller at 2.4 in can't take one. In the simulator that would cost 2 to 11 points across the Autos.

**The decision** (`doc/intake-design.md` on `spike/160-intake-design`):
- **The roller rises up to 1.3 in,** on arms pivoting about its motor shaft, 77.5 mm above the resting axle, so the
  belt length stays constant. A spring returns it to a down stop at 2.4 in.
- **The extractor gets a fixed pivot,** preferably on the motor shaft, with its arms outside the roller's ends.
- **The transfer's pulley stays on the roller shaft,** belted about the float pivot.
- **The CAD chat is redrawing.**

**In the simulator:** a gap rule (`RobotDesign.rollerFloatIn`, miss reason "gap") refuses a piece bigger than the gap
under the roller plus its float. The design to use is "DHS intake-b, floating roller". The scores already published for
the drawn intake and V are the floating roller's: the earlier runs let NECTAR through anyway.

## The transfer in the simulator (6 Oct 2026, 60 runs)

The three baselines on the drawn V, with the transfer's two effects separately and together (`doc/transfer.md` on
`spike/164-transfer`).
- **Shots:** every 0.25 s, against the baseline launcher's 0.45 s.
- **Lane capacity** (`RobotDesign.laneCapacity`): 4 POLLEN, 3 NECTAR, 3–4 mixed.

The routes are the baselines unchanged, so they're still timed for 0.45 s shots.

| Design | Partner shoots | Stages, angled partner | Stages, wall partner |
|---|---|---|---|
| Baseline | 71.2 · 3 TIPs in 48 · G409 8 | 51.6 · TIP 3 in 0 · 4 problem runs | 55.3 · 3 TIPs in 26 · G409 16 |
| 0.25 s shots | 69.2 · 42 · G409 12 | **56.6 · 3 TIPs in 14** · 2 problem runs | **58.0** · 26 · G409 26 |
| Lane capacity | 70.5 · 46 · G409 6 | 51.3 · 0 | 54.3 · 23 |
| Both (the transfer) | 68.5 · 40 · G409 13 | 54.3 · 7 | 58.0 · 26 · G409 25 |

**What it says:**
- **Faster shots win with a staging partner.** TIP 2 comes 1.6–1.9 s sooner (19.1 s → 17.2–17.5 s). For the first
  time the angled partner's Auto makes 3 TIPs (14 of 60), and the wall partner's gains 2.7 points.
- **Faster shots lose 2 points with the partner that shoots.** That route waits a fixed time after each volley for the
  spill. Firing sooner moves the TIP and its spill earlier against those waits, so it keeps less, and G409 rises. The
  route needs retiming for the faster launcher before this number means anything.
- **The lane's 3-NECTAR limit costs little:** 0.3–1.0 points.
- **G409 rises with the faster shots** (Stages wall 16 → 25–26). Spills come down while the robot is still close in.
  Retimed waits should bring it back down.

**Retimed for the 0.25 s shots** (`tools/auto-routes/retime.py`, 60 runs on "rigid V, transfer"). Only the
wait for TIP 2's spill to land changes:

| Auto | Wait | Points | 3 TIPs (of 60) | G409 runs | Tried |
|---|---|---|---|---|---|
| Partner shoots | **200 ms** after TIP 2 settles (was 500) | 70.5 (untuned 68.5; 0.45 s baseline 71.2) | 46 (40; 48) | 16 | 0: 68.5, G409 58 · 100: 70.2, G409 43 · 800: 65.5 · 1100: 64.5 |
| Angled partner | **1300 ms** from TIP 2's start (unchanged) | 54.3 | 7 | 19 | 700: 51.7 and PARK 24 · 1000: 53.4 and PARK 41 · 1600: 53.3 |
| Wall partner | **700 ms** from TIP 2's start (was 1300) | **60.7** (untuned 58.0; baseline 55.3) | **34** (26; 26) | 32 | 400: 53.3 · 550: 58.0 · 1000: 58.3 · 1600: 56.7 |

**With the transfer and its retimed waits, each Auto against the 0.45 s baseline:**
- **Partner shoots:** holds level, 70.5 against 71.2, within the 60-run noise.
- **Angled partner:** gains 2.7 points.
- **Wall partner:** gains 5.4 points, with 3 TIPs in 34 of 60 runs instead of 26.

**What it costs: G409.** A shorter wait means driving into a spill that is still landing. The wall partner's Auto
goes from 16 to 32 touched runs, and partner shoots from 8 to 16. If G409 gets called on a real field, the longer
waits are the fallback.

## Shoot while extracting, first runs (6 Oct 2026, 60 runs)

**Setup.**
- **Design** "rigid V, turret transfer": the transfer (0.25 s shots, lane capacity) feeding a **turret**, so the
  robot can face a FLOWER while the turret aims at the CELL.
- **Routes** (`tools/auto-routes/stream.py`): the retimed baselines, with every far-FLOWER visit turned into a stream.
  The robot drives in, turns StreamOn, fires each POLLEN as it comes out, and stays until TIP 2. The GARDEN and the
  wall FLOWER are too far from the CELL that needs them then (the simulator's range is 60 in), so they stay as they
  were.
- **Spread:** the same runs again at zero shot spread, the launcher's accuracy target.

| Auto | Turret, no streaming | Turret, streaming at the far FLOWER |
|---|---|---|
| Partner shoots | 71.8 · 3 TIPs in 50 · PARK 58 (zero spread: **75.3 · 58 · 60**) | **35.4 · TIP 2 in 4 of 60** (zero spread: 33.6 · 1) |
| Angled partner | 52.3 · TIP 3 in 5 · PARK 40 (zero spread: 55.8 · 3 · 49) | 49.4 · PARK 9 (zero spread: 51.9 · 11) |
| Wall partner | 58.7 · 3 TIPs in 30 (zero spread: **64.3 · 40**) | 58.3 · 30 (zero spread: 64.3 · 40); the far FLOWER is only its fallback |

**What it says:**
- **The turret helps on its own.** Partner shoots goes from 70.5 to 71.8 (75.3 at zero spread, with 3 TIPs in 58 of
  60), and the wall partner reaches 64.3 with 3 TIPs in 40 at zero spread.
- **Streaming from the far FLOWER doesn't work in the simulator yet.** The seat is off the CELL's axis (x 47 against
  58, about 14°) and about 45 in from it. From there about 1 shot in 4 is lost (61 of 80 scored with today's
  spread), and still about 1 in 4 at zero spread. So the loss is the seat's geometry, the angle or the path past the
  HIVE, not random scatter. TIP 2 needs all 8, so it rarely comes.
- **Before building Autos around it, test that seat on a field.** Shoot 20 POLLEN from the far FLOWER's extractor
  seat with the turret aimed. If they go in, the simulator's off-axis shot model needs fixing. If they don't,
  shooting while extracting needs a FLOWER on the CELL's axis, or a different seat.
- **The angled partner's Auto loses PARK with the turret** (40 of 60). That's a squeeze in its fallback branch: when
  TIP 2 doesn't come off the row, the far FLOWER puts TIP 2 at about 21 s and the park is cut at 27.7 s. A route fix.

## The envelope

- **R102:** 18 × 18 × 18 in at the start.
- **R105:** 18 × 24 in once started.
- **The body as drawn:** 15.12 in long × 15.24 in wide (CAD re-measure, 6 Oct 2026), plus the roller 1.94 in ahead.
- **The V takes the front corners.** The extractor and scorer must fit around it, or fold.
- **Every part states its stowed and deployed outline,** so the simulator can draw and collide it.

## Rules every chat keeps

- **The Pedro field frame, in inches.** It is the only frame (CLAUDE.md).
- **Numbers come from the simulator at 60 runs on the three qualifier Autos**, each part on a route drawn for it,
  or from a cardboard or field test. Never from a guess.
- **PARK is non-negotiable in quals.** No route trades it away.
