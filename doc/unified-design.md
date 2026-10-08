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
| **AdvantageScope** | AdvantageScope internals (session_01Ade3tLhGCfjXzSL7XmxAFn), issue #166 | What a simulated log shows inside the robot: each held piece on the transfer's path, and the model's moving parts (extractor, roller float, turret, J arm) as `/Internals/Components`. Draws what the simulator decided, never changes it. The layout `sim-review/advantagescope-layout-internals.json`; [doc/advantagescope-internals.md](advantagescope-internals.md). |

## The baselines (claude/simulator, 6 Oct 2026)

The three qualifier baselines are the drawn V on these routes, published from `claude/simulator` only:

| Baseline | Route | Points | 3 TIPs (of 60) | Notes |
|---|---|---|---|---|
| `qual-right-v` | `qual-right-o3-rigid-v-park` | **70.2** | **45** | PARK 58, G409 8 runs, no problems. Turret, 0.35 s transfer feed, the extractor seated (the face 4.59 in from the FLOWER) and every FLOWER left straight back, 7 Oct 02:10 UTC; 72.2 / 51 with a fixed launcher and no feed delay. Firing from the seats (`qual-right-v-seatfire-west`): 72.3, TIP 3 in 51 |
| `qual-stages-angled-v` | `qual-stages-angled-rigid-v-18-30-t555` | 51.6 | – | TIP 2 in 53, PARK 55, G409 14. 4 problem runs, all where TIP 1 failed (see below). |
| `qual-stages-wall-v` | `qual-stages-wall-rigid-v-18-30-sweep90-t555` | **52.3** | – | TIP 2 in 54, PARK 60 for both robots (the partner at the zone's top, our sweep east of it, no GARDEN load; 7 Oct 16:30 UTC), G409 9, no problems. 55.3 / 26 with the partner on our spot and no PARK for us |

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

## Shoot while extracting (6 Oct 2026, 60 runs)

**Setup.**
- **Design** "rigid V, turret transfer": the transfer (0.25 s shots, lane capacity) feeding a **turret**, so the
  robot can face a FLOWER while the turret aims at the CELL.
- **Routes** (`tools/auto-routes/stream.py`, the `-leave` routes): the retimed baselines, with every far-FLOWER visit
  turned into a stream. The robot drives in and **sits 0.4 s**, so it's seated before it fires. Then StreamOn: each
  POLLEN is fired as it comes out. It **leaves once the 4 are away**, 1.3 s later. The GARDEN and the wall FLOWER are
  out of the simulator's 60 in range of the CELL that needs them then.

**The first version lost 1 shot in 4, and it wasn't the seat.** That route turned StreamOn the moment the path
ended. The robot was still rolling into the FLOWER, and streaming fires while moving, carrying the robot's motion
into the shot. The first POLLEN went wide in every run; the other three, fired seated, all scored. The robot also
waited at the FLOWER for TIP 2, reached the spill about 2 s after the TIP and kept 2 of 4. Seated first and leaving
early fixed both.

| Auto | Turret, no streaming | Turret, streaming (seated, leave early) |
|---|---|---|
| Partner shoots | 71.8 · 3 TIPs in 50 · PARK 58. TIP 2 at 11.4 s, TIP 3 at 23.5 s | 70.9 · 47 · PARK 59. **TIP 2 at 9.5 s, TIP 3 at 21.6 s** |
| at zero spread | 75.3 · 58 · PARK 60 | 74.3 · 55 · PARK 60 |
| Angled partner | 52.3 · TIP 3 in 5 · PARK 40 | 51.6 · 5 · **PARK 11** |
| Wall partner | 58.7 · 30 (zero spread: 64.3 · 40) | the same; the far FLOWER is only its fallback |

**What it says:**
- **Streaming works and saves about 2 s,** with TIP 2 and TIP 3 both earlier. The score is level (70.9 against 71.8,
  within the noise) because the routes don't spend the time: after TIP 3 at 21.6 s the robot PARKs with about 6 s
  to spare. The next step is a route that uses it.
- **The angled partner's Auto loses PARK** in its fallback branch, the far FLOWER when TIP 2 doesn't come off the row.
  It needs a route fix before streaming is usable there.
- **The simulator's launch spin is fixed in the field frame** (`FieldSim.launch`, `p.wy = -12`). That is backspin
  only for a shot travelling along x; our shots travel along y. It wasn't the cause above (the misses stayed with
  spin off), but the simulator chat should fix it. `FieldSim.launchSpin` / `BIOBUZZ_AUTO_SPIN` expose it for testing.

## Flower first (7 Oct 2026, 60 runs)

**The user's plan:** "path to the FLOWER immediately and then shoot all 8 from there". The route is ShootsRight,
`qual-right-v-flower-first-l<ms>` in `stream.py`:
- From the start, straight to the far FLOWER, seated with the face 4.59 in from its centre (the CAD's seat).
- Wait there for the partner's TIP 1.
- StreamOn: the 4 preloads, then the FLOWER's 4 as the lane frees up.
- Leave after `<ms>`, then the baseline's tail.

**Run on** "rigid V, turret transfer" (seat 4.59, the 0.5 s `transferFeedS` placeholder), partner on spring hood at 40.

| Leave after | Points | 3 TIPs | PARK | G409 runs | Zero spread |
|---|---|---|---|---|---|
| 1.8 s, 2.2 s | 36 | 0 (no TIP 2) | 60 | 0 | 36 |
| **2.6 s** | **71.3** | **48** | **59** | 20 | **73.3 · 52 · PARK 60** |
| 3.0 s | 69.3 | 42 | 59 | 8 | 70.7 · 44 |
| 3.4 s | 63.3 | 24 | 59 | 11 | 63.0 · 21 |

No run had a problem (collision, HIVE or FLOWER hit, centre line).

- **2.6 s is the leave timer.** On the 0.5 s feed the last FLOWER POLLEN is ready about 0.5 s after it leaves the
  FLOWER, so anything shorter leaves with it aboard and TIP 2 never comes. Waiting longer only costs the spill.
- **The earlier "flower-first scores 36" was the route, not the feed.** `stream.seated()` moved the FLOWER points
  on top of `baselines_v.FLOWER_FACE_V`, which already moves them, so the face stopped 9.48 in out and never seated.
  It now sets `FLOWER_FACE_V` instead.
- **This is the Auto's level with the baseline, about 2 s sooner,** like streaming. The time it frees after TIP 3
  is still unused.

**Streaming from N_FIRE on the same feed** (`-leave<ms>`, the robot fires the preloads first, then streams the
FLOWER): the 1.3 s leave also leaves with the 4th POLLEN aboard (37.9). 1.8 s gives **71.6 · 3 TIPs in 49 · PARK 59**,
level with flower-first; 2.3 s 68.9. The Stages routes don't move (angled 51.7, PARK 12; wall 58.7): the far
FLOWER is only their fallback.

**Extractor down before the FLOWER (mentor, 7 Oct 2026).** "You cannot deploy the flower extractor while at the
flower. You need to deploy it BEFORE you get there and then drive into it." The simulator kept it up while the robot held
4, so on flower first it swung down only after the first shot, already at the FLOWER. Now (`AutoSim.extractor`) it comes
down on any path that ends at a FLOWER, full or not, and the FLOWER's pieces stay in it until a shot makes room. One it
reaches before it is down blocks it (event `extractor blocked`): it takes nothing. Flower first is unchanged by it
(10 runs: 70.0, 3 TIPs in 7, PARK 10, TIP 2 at 7.4 s and TIP 3 at 20.1 s, against the plain route's 11.5 s and 23.6 s at
the same 70.0); the extractor is down from 1.0 s, with the robot seated from about 2.8 s. Whether a robot may sit
pushed into the FLOWER before it fires is a rules question still open.

**Mentor review of the flower-first log (7 Oct 2026):** shoot the 4 preloads, stop until the FLOWER's 4 are in,
turn and get lined up, then shoot those 4; and the turn away from the FLOWER ran through where TIP 2's spill drops.
`qual-right-v-flower-first-hold` (`stream.py`): the preloads fired one by one from the seat, the FLOWER's 4 collected,
back out and turn to N_FIRE, then fired there stopped. TIP 2 can only start once they're away, so the robot already
stands where the baseline waits for the spill. 10 runs: **70.0 · 3 TIPs in 7 · PARK 10 · G409 2**, against the
streaming flower first's 66.0 · 5 and preloads first's 72.0 · 8 on the same simulator. **60 runs** (the AdvantageScope chat): **70.6 · 3 TIPs in 47 · PARK 119 of 120**, against the streaming flower first's 65.3 on the same simulator. The GARDEN approach (through
the wall) is in the shared tail: the simulator chat's wall check and fix.

**Regenerated on the simulator chat's GARDEN fix and wall check (cfae8b2), 10 runs, no wall or other problems:**
flower first, hold 70.0 · 7 · PARK 10; preloads first 70.0 · 7 · PARK 10; wall stream 56.0 · PARK 10. **The angled
partner's stream routes are retired:** the far FLOWER is only that route's fallback, and streaming there leaves too
little time to PARK (leave 1.3 s: PARK 2 of 10; 1.8 s: 53.5, PARK 1 of 10; the plain route 55.0, PARK 8 of 10).
Use the plain `qual-stages-angled-v` for that pairing.

**On the HIVE's dwell before a TIP (claude/simulator 3e0813b), 60 runs:** flower first, hold **70.6 · 3 TIPs in 47 ·
PARK 59 · G409 15 · 1 problem run** (seed 1 drives into a FLOWER at 26.3 s, in the shared ending) against the simulator
chat's seat fire **72.3 · 52 · PARK 60 · G409 6 · none**. Seat fire is the better right-side Auto; the hold route's 10-run
lead (72.0 against 70.0) was noise.

**On the 0.2 s shot interval (claude/simulator 2698d62), 60 runs:** flower first, hold **74.0 · 3 TIPs in 56 · PARK 60
· G409 16 · no problem runs**, level with the plain right route's 74.0 and ahead of seat fire's 72.7 (the simulator
chat's figures). One fix on the way: when the partner missed TIP 1 (1 run in 60) the robot sat at the FLOWER all AUTO,
because LaunchOne waits until it has fired and the CELL up before TIP 1 is out of the seat's range. The preloads are
now fired from the seat only once the left CELL is up; without TIP 1 in 5 s the robot takes them to N_FIRE.

**Mentor review of the hold route (7 Oct 2026): PARK deeper, and use the time after TIP 3.**
`qual-right-v-flower-first-tip3` with `partner-preloads-right-high` (stream.py): after the GARDEN's shots, wait for TIP 3
(about 21.7 s), turn west at S_COLLECT (50, 24), let the spill land, take what the webcam sees of it (up to 3 s or 4
held), then up the west side (x 24) into PARK at (10.5, 95), the robot's frame about 8 in inside the zone; the partner
parks at the zone's far end (10.5, 116), 1.6 in clear. 60 runs, "rigid V": **74.0 · 3 TIPs in 56 · both PARK 60 ·
1.65 held into TELEOP · no problem runs** (the hold route: the same 74.0, 0 held). Two fixes to CollectSeen on the way
(AutoSim.approachHitsFrame): it judges walls and the centre line by the whole outline (V tips and flaps), not the
frame's corners, and also through the turn back to its starting heading; a piece by the centre line, collected facing
east, then the turn north for PARK swung the tips across. Still to try (mentor): carry TIP 3's spill under the HIVE and
fire it before PARK; or PARK straight away, deep, and fire from the LOADING ZONE.

**Mentor review of the TIP 3 route (7 Oct 2026, evening), `qual-right-v-flower-first-carry-settle-b2` with
`partner-preloads-right-south`** (stream.py `build_flower_first_north`):
1. *TIP 2: "1-3 inches further back, start moving toward the drop zone", no piece hitting us.* N_FIRE 2 in further
   back: no measurable change. Starting the drive south a fixed 0.6-1.8 s after TIP 2 starts, instead of when the
   CELL settles: 0.6 s touched the falling spill in 5 runs of 10, and every timing lost TIP 3 (16 of 20 against 19),
   so the route keeps the settle wait.
2. *TIP 3: "catching stuff but oriented the wrong way".* TIP 3's spill rolls south off the HIVE toward the wall,
   past S_FIRE. The robot now holds there facing the HIVE, intake running, for up to 1.5 s and catches what rolls in
   (3.6 pieces on average), instead of turning west to look for it.
3. *"Go shoot them on the opposite side, then park; the partner parks on its side."* Up the lane under the HIVE,
   with the control points held at x 58.5 so the robot's back clears the frame's west foot (x 45-47, to y 90), and
   west into the LOADING ZONE's north end (14.5, 116); the partner parks at the south end (10.5, 96). Firing the catch
   does not fit: TIP 3 comes at 21-22 s (25.6 s in a slow match), and catch, the lane, a fire and PARK need about 8 s.
   From N_UP the guard cut the fire every time; firing from the catch spot over the HIVE hit the HIVE. So the robot
   carries them into TELEOP.
60 runs, rigid V: **74.3 · 3 TIPs in 57 · both PARK 60 · 3.65 held into TELEOP · no problem runs** (the TIP 3 ending:
74.0 · 56 · 1.7 held).

**3 TIPs with a partner that cannot shoot (mentor, 8 Oct 2026: "can a flower first turret type get us there?"),
`qual-alone-flower-f45-s500` (tools/auto-routes/alone.py)**, from the south start: TIP 1 from our preloads, its spill
caught facing the HIVE; the catch fired at FAR_FLOWER_TURN, the far FLOWER's 4 held, then fired from N_FIRE (TIP 2);
TIP 2's spill caught down the lane and fired at S_FIRE; the wall FLOWER's 4 held, then fired from (45, 26) (TIP 3:
from beside the wall FLOWER the shots crossed the HIVE); PARK. 60 runs, rigid V, each partner:

| Partner | Points | 3 TIPs | PARK | Problem runs |
|---|---|---|---|---|
| parks only (`partner-park-only`, at the zone's far end) | 62.7 | 35 | 60 | 4 |
| Stages, angled | 62.7 | 35 | 60 | 4 |
| Stages, wall | 62.7 | 35 | 60 | 4 |
| the angled baseline, for comparison | 61.0 | 23 | 60 | 4 |

The partner makes no difference: the route never uses a staged row. TIPs at about 4, 15 and 26 s. The 4 problem runs
are TIP 1 failing (the HIVE tips at 23 s): the endgame guard's park, drawn from (45, 26), drags the V out through the
wall FLOWER, as the baseline's runs into the HIVE frame. `partners.park_left` drives into the far FLOWER and onto our
PARK, so the park-only partner here is new. A 4th TIP does not fit: TIP 3 comes at about 26 s. **It needs no turret** (every shot is from a standstill): on "rigid V, fixed turret", with S_FIRE moved to x 55 (a
fixed launcher turning there to face the right CELL swung the V's tips over the centre line from x 57.5), 20 runs:
67.0, 3 TIPs in 12, PARK 20, 1 problem run (a TIP 1 failure), with the park-only or the angled partner; 66.0 / 14 with
the turret. The Stages baselines with the turret fixed: angled 62.0 / 6, wall 56.0 / 0 (20 runs).

**Baseline (i), partner parks, fixed turret: `qual-alone-p4-lane-r`** (8 Oct 2026, 60 runs, "rigid V, fixed turret",
partner-park-only): 68.0, 3 TIPs in 43, PARK 60, no problem runs. Two changes from f45-s500: all four preloads fired
(the 2500 ms card ended a shot early, so one miss lost TIP 1), and a missed TIP 1 recovered (no TIP in 4 s: catch what
fell in front of the HIVE and fire it at the right CELL); the FLOWER drags were all late runs after a TIP 1 failure.
The misses left: TIP 2 (7) and TIP 3 (10), each with fewer than 8 pieces in the CELL (8 in it always tipped; counted
from the score events, 60 runs: TIP 2 misses 6-7 in, TIP 3 misses 3-7 in). The robot brings too few: TIP 2 when the
TIP 1 catch is short; TIP 3 when the lane's catch of TIP 2's spill is short or the wall FLOWER's volley is cut for
PARK; once (seed 1) a shot from S_FIRE hit the HIVE. (Until 8 Oct this said 8 pieces left the right CELL at 91-95%:
that counted shots fired, not pieces in.) Tried and no better:
- A longer TIP 1 catch (2500, 3000 ms): 12/20, 6/20; the time comes out of TIP 3.
- The GARDEN while TIP 1 dwells, fired for TIP 2: the left CELL reaches only 52% on 4.
- The west ending (wall FLOWER, then GARDEN, fired from (25, 28)): 14/20.
- Bodies (20 runs each, both routes): V 22 in, 24 in, 6 in deep: no more TIPs, and their outline crosses the centre
  line and the HIVE frame on routes drawn for the drawn V; V + dual Ramp Hook: 15/20; the moving turret: 15/20.
- A second load from TIP 2's spill at (55, 30) or (55, 34): 0/20. **Where TIP 2's spill goes**: 8 pieces land just
  north of the HIVE (x 50-67, y 93-98) at about 16.5 s, in front of the robot at N_FIRE; the lane drive scoops 4 (the
  lane's capacity), and the rest roll off, two of them over the centre line. Each CELL opens at its own end, so TIP 3
  is fired from the south while TIP 2's leftovers lie north: a hook would hold them where the robot is not. The lane
  caps any body at 4 per trip, so a hook pays only on a route that comes back for a second load, and here that trip
  costs what the wall FLOWER does.
- **A partner's staged preloads.** TIP 2 is decided by what we carry north (60 runs of p4-lane-r: 4 carried, TIP 2
  in 46 of 46; 3, 7 of 9; 2 or fewer, 0 of 5), so staged pieces near the left CELL looked like the fix. A partner
  that can't shoot can't set them down mid-match either (mentor, 8 Oct 2026): its preloads start on the tiles
  touching it (G304), so they sit at its start. A partner at the standard left start (59, 132.25) with a row across its
  front is boxed in (the row south, the far FLOWER west, the centre line east). From (30, 132.25), west of the far
  FLOWER, it slides off along the wall and parks at (10.5, 118), and the row stays at y 121.6, x 26-34; collecting it
  for TIP 3 (south of it, north through it, down the west side) costs 5.1 s against the lane's 2.6 s and picked up
  2 of the 4: 2, 1 and 2 TIPs in seeds 1-3. Dropped. (A route whose partner set its preloads down at the FLOWER
  mid-match reached 40/60; withdrawn, since no partner can.)
- **Faster** (20 runs each; DeepDive takes `Auto@<in/s>` for our drivetrain): 60, 70, 80 in/s gave 14, 12, 15 of 20
  (50: 43 of 60). The TIPs come earlier (TIP 3's median 26.6 s at 50, 23.9 s at 80) but TIP 3 still
  misses: the lane's catch is short as often. With the GARDEN as a fallback when the right CELL is still up after the wall FLOWER (`garden=True`): 16, 14,
  15 of 20 at 50, 60, 80; at 80 the GARDEN's 4 are collected by 27.4 s and the guard parks before they are fired, and
  the lane at 80 touched a falling piece (G409). Most of the match is spin-up (2 s), extraction (about 1.6 s a
  FLOWER), the 0.2 s shot interval and the HIVE's dwell, which speed does not change.

**Baselines (ii) and (iii), a partner that shoots its preloads, fixed turret** (8 Oct 2026, `alone.py`):
- **(ii), the partner at the left start:** `qual-alone-p4-lane-r` unchanged, with `partner-left-v` (fires its 4 at the
  left CELL as it rises after our TIP 1, then forward to y 124 and west along it to (10.5, 118), out of our lane before
  we come north; `partners.preloads_left` parks under the far FLOWER and onto our PARK). 60 runs: **71.7, 3 TIPs in
  49**, PARK 60, no problem runs; misses: TIP 3 (10, 3-7 pieces in), TIP 1 (1, two preloads hit the HIVE).
- **(ii), using the partner's pieces: `qual-left-partner-v-fixed`** (mentor, 8 Oct 2026: "a scenario that is easier"):
  the partner's 4 and our catch tip TIP 2 at 11-12.5 s, while we are in the far FLOWER's seat, and p4-lane-r then
  stood at N_FIRE firing at nothing for 2.5 s. Now: if the right CELL is up within 1.5 s of the FLOWER's 4, wait 0.6 s
  for the spill, onto the lane's top (57.5, 108) and straight down it facing north (one curve from the seat entered
  the HIVE frame's feet at x 54 and clipped the west foot, 18 of 20), fire the FLOWER's 4 from (45, 26) (from S_FIRE a
  shot hit the HIVE), then the wall FLOWER's 4 (TIP 3: 8 pieces). Otherwise N_FIRE, firing until the right CELL is up
  (not until a new TIP: one that started during the drive left it waiting 4 s). No GARDEN fallback: its check came
  inside the TIP's dwell and sent the robot to the GARDEN with TIP 3 on its way, and the guard turned it into the
  wall. 60 runs: **73.7, 3 TIPs in 54**, PARK 60; 2 problem runs (late runs cut at 26.5 s: the HIVE frame, a FLOWER).
- **One route from the south start: `qual-south-v`** (mentor, 8 Oct 2026: "realistically should only be 1 or 2
  autos"). `qual-alone-p4-lane-r`, the far FLOWER's 4 fired at N_FIRE until the right CELL is up (a left partner's 4
  and our catch can have tipped it already, and the card ends at once), the lane's catch fired from (45, 26).
  (`qual-left-partner-v-fixed` with a park-only partner: 1 of 60, its "TIP 2 already?" wait and late N_FIRE cost
  2.5 s.) 60 runs, rigid V with a fixed turret: partner shoots from the left **56** with 3 TIPs (PARK 60, 1 problem
  run); partner parks only **38** (PARK 60, no problems); a left partner still at the standard left start (59,
  132.25) collides (silent, dead: 47-60 of 60), because that start overlaps the far FLOWER's seat and N_FIRE. A
  partner dead at the west start (24, 132.25), against the wall west of the far FLOWER, does not: **38**, PARK 60, no
  problems (`partner-left-dead-west`). So an unreliable partner starts there. R-Quals (mentor's name; L-Quals is the
  ShootsRight baseline).

- **(ii) for a left partner that may not do its job: `qual-left-partner-v-safe`** (mentor, 8 Oct 2026). A left
  partner that is slow, never fires or never moves sits at its start (59, 132.25), beside the far FLOWER and over
  N_FIRE, or crosses y 124 on its way west: `qual-left-partner-v-fixed` collided with it in 45-56 of 60 runs (test
  partners `partner-left-silent`, `-dead`, `-slow-3000`). The safe route goes nowhere near it: the catch fired from LW
  (36, 104), down the west side for the wall FLOWER's 4, then the HIVE decides: the right CELL up (TIP 2 came), the
  wall FLOWER's 4 and the GARDEN's 4 at the right CELL (TIP 3); still the left, back to LW for TIP 2 and home. 60 runs,
  rigid V with a fixed turret:

  | Left partner | 3 TIPs | 2 TIPs | 1 TIP | Our PARK | Problem runs |
  |---|---|---|---|---|---|
  | shoots (`partner-left-v`) | 47 | 12 | 1 | 59 | 3 (HIVE frame) |
  | never shoots, parks | 0 | 53 | 7 | 59 | 3 |
  | never shoots, never moves | 0 | 53 | 7 | 59 | 3 |
  | shoots 3 s late | 48 | 11 | 1 | 5 | 55 (collides on its way west) |

  The cost against `qual-left-partner-v-fixed` (54 of 60 with a partner that does its job): 7 runs of 3 TIPs.

- **(iii), TIP 3 from sure pieces: `qual-shoots-right-v-fixed-west45`** (mentor, 8 Oct 2026: "i think we need to make
  it not luck"): the lane route's TIP 3 rested on catching TIP 2's spill, and missed the same 4 seeds on every body
  (the dual hook too: same 4). Now the far FLOWER's 4 are fired at N_FIRE and the robot leaves at once, down the west
  side for the wall FLOWER's 4 and then the GARDEN's 4, both volleys from (45, 26) (from (25, 28) a shot hit the HIVE),
  PARK by (30, 40) (a straight park from (45, 26) clipped the west foot's corner). 60 runs: **74.7, 3 TIPs in 58**,
  PARK 60, no problem runs, no TIP 3 miss; both misses are TIP 2 after the partner's preloads hit the HIVE (TIP 1
  late or never, and the left CELL never up for ours).
- **(iii) with a plan for a TIP 1 that never comes: `qual-shoots-right-v-fixed-west45-r`** (mentor, 8 Oct 2026: "we
  need to have a plan if it never comes"; "i dont expect the fallback to get to 3 tips"; "6s might be too soon like
  what if they are just a slow robot"). No left CELL up 7 s after we reach FAR_FLOWER_TURN (9.2 s into AUTO): down the
  lane, our preloads at the right CELL until the left CELL is up (a late TIP from the partner's pieces ends it too),
  the spill caught, back up, the catch and the far FLOWER's 4 at the left CELL (TIP 2), fired and gone; home down the
  lane facing south to (56, 36), turned there (clear of the HIVE frame's feet, the centre line and a partner dead at
  the right start), across to (45, 29) and PARK. No wall FLOWER. The GARDEN's volley, and PARK's start, are at
  (45, 29): at (45, 26) the V met a dead partner's corner. Test partners: `partner-right-silent` (never shoots,
  parks), `partner-right-dead` (never shoots, never moves), `partner-right-slow-<ms>` (fires that late).

  The wait, swept with a silent partner (60 runs each): 2 TIPs in 53 at every wait up to 10 s, none from 12 s; PARK
  and a clean drive home to 7 s (home down the west side: to 6 s). Past that the guard cuts the late fallback with
  the robot north or inside the HIVE's feet, and the park path, which starts south of the HIVE, crosses the frame.
  60 runs each at 7 s, rigid V with a fixed turret:

  | Partner | 3 TIPs | 2 TIPs | 1 TIP | Our PARK | Problem runs |
  |---|---|---|---|---|---|
  | shoots (`partner-preloads-right-high`) | 58 | 1 | 1 | 60 | 0 |
  | shoots 3 s late (TIP 1 at about 4.7 s) | 58 | 1 | 1 | 60 | 0 |
  | shoots 6 s late (TIP 1 at about 7.7 s) | 25 | 34 | 1 | 60 | 0 |
  | never shoots, parks | 0 | 53 | 7 | 60 | 0 |
  | never shoots, never moves | 0 | 53 | 7 | 60 | 0 |

- **(iii), the partner at the right start:** `qual-shoots-right-v-fixed` with `partner-preloads-right-high` (its 4 are
  TIP 1). We start north (59, 133.69), wait at FAR_FLOWER_TURN for the left CELL, fire our preloads there, the far
  FLOWER's 4 from N_FIRE until the TIP itself (up to 4 s: leaving on a 2.5 s timer drove into the lane ahead of the
  spill and caught nothing), TIP 2 at about 12.5 s; then TIP 2's spill down the lane, the wall FLOWER, and the GARDEN
  if the right CELL is still up. 60 runs: **72.0, 3 TIPs in 50**, PARK 60, no problem runs; misses: TIP 3 (8, 5-7
  pieces in), TIP 2 (2).
  The flower-first turret routes (`qual-right-v-flower-first-carry-settle-b2`, 57 of 60 on "rigid V") fire while
  seated, which a fixed launcher cannot: 0 of 60 and a FLOWER hit at about 4 s in every run. Firing the wall FLOWER's
  4 from (25, 28) instead of (45, 26), for a nearer GARDEN: 14 of 20.

**What a mechanism buys the fixed-turret baselines** (8 Oct 2026, 20 runs each, 3 TIPs; same routes):

| Body | (i) parks only | (ii) left partner | (iii) right partner |
|---|---|---|---|
| rigid V, fixed turret | 16 | 20 | 16 |
| ... 1 s spin-up (from 2 s) | 12 | 20 | 16 |
| ... 0.25 s extractor pull (from 0.5 s) | 17 | 20 | 16 |
| ... both | 12 | 20 | 16 |
| rigid V (moving turret) | 15 | 19 | 16 |

None moves the needle on these routes: every miss left is a TIP short of pieces (a short catch, a shot into the HIVE),
not short of time. (i) loses with the faster spin-up: TIP 1 comes earlier and the catch at S_CATCH, timed for it,
takes less. (iii)'s misses are identical in every body: TIP 3 rests on the lane's catch of TIP 2's spill.

**Still open:**
- ~~The baselines fitted to the 7.09 seat~~: claude/simulator now seats at 4.59 (`FLOWER_FACE_V` too); merged.
- **The endgame guard parks a seated robot sideways**: it follows the park path from its start, so a cut at the wall
  FLOWER's seat drags the V through the FLOWER whatever the path's middle (three control layouts, same hits). Needs a
  back-out first in `AutoKit.guarded` (sent to the simulator chat).
- **The wall partner's Auto has no PARK at all** (`QualStagesWallVAuto` and its stream variants: PARK in 0 of 60).
  PARK in quals is non-negotiable, so that route needs a PARK ending.

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
