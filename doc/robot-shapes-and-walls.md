# Side walls and robot shapes at the spill

Moved here from the repository README on 6 Oct 2026, unchanged, when the research branches were merged into
`claude/simulator` (`claude/dazzling-maxwell-je04gu`, then `claude/biobuzz-robot-body-designs-hi386c`). The
numbers are as they were run, with the dates given; the summary is in the README, "What we've learned".
Links to `README.md` sections below mean the repository README as it was then; the content is all here.

## Side walls (from branch `claude/dazzling-maxwell-je04gu`)

A mentor's idea (5 Oct 2026): walls down both sides of the robot that slide 6 in forward (a "long U",
18 × 24 in, R105's limit) as our CELL starts to TIP, so the spill doesn't scatter while the robot waits
just short of where it lands. Each wall has a one-way flap at the bottom that lets POLLEN in and keeps
NECTAR out. G409 is the constraint throughout: the robot and its walls must not touch a spilled piece
before it reaches the tiles. Numbers run 5 Oct 2026 03:50–13:20 UTC on this branch.

**In a qualifier Auto** (20 runs, normal / slow tiles; `tools/auto-routes/README.md`, "G409-safe versions"):

| Auto | Robot | Points | 3 TIPs | G409 | Simulated `.wpilog` |
|---|---|---|---|---|---|
| Qual-PartnerShootsLeft as it was | no walls | 70.3 / 72.3 | 18 / 19 | 9.6 / 5.6 a run | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-partner-shoots-left/latest-best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-partner-shoots-left/latest-typical.wpilog) |
| **Qual-PartnerShootsLeft, G409-safe** (`qual-partner-shoots-left-g409-300-s8n4-t700`) | **walls out at the TIP** | **71.5 / 69.8** | **19 / 18** | **0** | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-partner-shoots-left-g409-300-s8n4-t700/designs/spring-hood-full-width-intake-side-walls-out-at-the-tip/latest-best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-partner-shoots-left-g409-300-s8n4-t700/designs/spring-hood-full-width-intake-side-walls-out-at-the-tip/latest-typical.wpilog) |
| Qual-PartnerShootsLeft, G409-safe | no walls | 68.3 / 68.8 | 17 / 17 | 0 | |
| Qual-PartnerShootsRight v3 (above; v2 + 500 ms) | no walls | 72.5 / 71.8 | 18 / 17 | 0 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v2-g409-500/latest-best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v2-g409-500/latest-typical.wpilog) |

So the walls are worth about 3 points where the robot waits for a spill over open tiles (ShootsLeft),
and nothing to v3, whose waiting spot puts falling pieces onto the walls.

**On a single TIP** (`SideWallSpillTest`; 40 TIPs, robot parked facing the HIVE, the spill measured 3 s
later): the long U gathers 33% of the spill standing and 39% creeping 8 in forward once it has landed,
against 13% / 18% with no walls; a wide U (24 in mouth, no forward reach) is no better than no walls.
Park with the arm tips about 38 in from the wall, a few inches short of where pieces first hit the
tiles (44–48 in): further forward the spill lands on the robot (G409). Pieces lying between the arms
of a parked robot aren't CONTROL; pushing them forward with the U is herding, and counts toward 4.

![Where a red TIP's spill first touches the floor, on the Visualizer's field](sim-review/spill-window.png)

**Where the spill lands** (above; `sim-review/spill-window.html` is the same picture to zoom into). A
CELL loaded with 8 POLLEN (the setup guide's other calibration case), 200 simulated TIPs, no robot. Each
piece is drawn at its true size where it first hits anything after leaving the CELL: the tiles, the
HIVE's feet, or a piece already down. A piece that lands on the pile and rolls into a robot is legal
under G409 (it has touched something else), so this is where G409 stops mattering. The dashed box bounds
every piece's footprint: x 45.6–69.5, y 35.1–47.5 in from the audience wall. The solid box bounds 90% of
them on each axis: x 47.4–66.2, y 38.1–45.4. The match-start load (3 NECTAR, then 3 POLLEN) lands the
same: every piece in x 46.2–72.0, y 35.4–47.0. The simulator lands them a little short of the 3 Oct films
(median 42 in against about 48).

**How close the plain robot can park** (`SideWallSpillTest.howCloseCanThePlainRobotPark`): the 18 in
robot, walls in, centred on the red CELL's axis (x 58), facing the HIVE, 200 TIPs at each distance.
Touches are G409 touches; "TIPs" counts the TIPs with at least one:

| Front face from the audience wall | 8 POLLEN: touches, TIPs | Match start: touches, TIPs |
|---|---|---|
| 30–35 in | 0, 0 | 0, 0 |
| 36 in | 2, 2 | 2, 2 |
| 37 in | 8, 8 | 19, 19 |
| 38 in | 53, 50 | 93, 75 |
| 40 in | 409, 180 | 463, 193 |
| 42 in | 1,223, 200 | 1,013, 200 |

**The long U** (`howCloseCanTheLongUPark`): the same robot with the side walls slid 6 in forward, out
from the TIP's start. "Kept" is the share of the spill lying in the same patch of floor in front of the
robot 3 s after the TIP (15 in behind its front-most point to 8 in past it, 24 in wide):

| Robot | Front face / arm tips | 8 POLLEN: kept, TIPs with a G409 touch | Match start: kept, TIPs |
|---|---|---|---|
| plain | 35 / – | 17%, 0 of 200 | 14%, 0 of 200 |
| long U | 30 / 36 | **41%, 0 of 200** | **36%, 0 of 200** |
| long U | 32 / 38 | 45%, 4 of 200 | 39%, 9 of 200 |
| long U | 34 / 40 | 49%, 58 of 200 | 43%, 89 of 200 |
| long U | 36 / 42 | 52%, 178 of 200 | 48%, 190 of 200 |

Thin arm tips can wait about 1 in nearer than a robot's front face (36 in against 35); with the front
face at 30–34 in, every touch is on the arms. Pictures of each step are in
`doc/portfolio/spill-window/`.

**Surround the landing instead** (`SideWallSpillTest.surroundTheLanding`, mentor, 5 Oct 2026): where the
spill lies 3 s after the TIP, measured from the centre of where it lands (58, 41 in from the wall), and
how much ends past the field's centre line (x > 70.75, the blue half), 200 TIPs each. "Right arm only"
is a solid shield on the robot's right side only (`RobotDesign.sideWallsOnly`), the robot across the
landing with its right side on the 100% drop box's right edge (x 69.5). Touches are G409 touches:

| Robot | 8 POLLEN: within 12 in, past centre, TIPs touched | Match start: same |
|---|---|---|
| no robot | 6%, 34%, 0 | 6%, 41%, 0 |
| plain, face 35, x 58 | 23%, 33%, 0 | 19%, 41%, 0 |
| long U, face 35 (arm tips 41), x 58 | 52%, 13%, 121 of 200 | 48%, 23%, 145 of 200 |
| long U, face 36 (tips 42), x 56.8 (the 90% box's centre) | 56%, 14%, 160 of 200 | 49%, 25%, 157 of 200 |
| long U, face 37 (tips 43), x 56.8 | 59%, 13%, 182 of 200 | 51%, 23%, 174 of 200 |
| long U, face 38 (tips 44), x 56.8 | 61%, 12%, 188 of 200 | 50%, 23%, 189 of 200 |
| plain, face 35, x 60.5 | 20%, 33%, 0 | 19%, 41%, 0 |
| **right arm only, face 35 (tip 41), x 60.5** | **38%, 13%, 0** | **39%, 18%, 20 of 200** |
| right arm only, face 37 (tip 43), x 60.5 | 39%, 12%, 7 of 200 | 40%, 14%, 52 of 200 |

Where the touches come from: an arm inside the drop box is under falling pieces. The long U's arms are
18 in apart, inside the 24 in wide box, so both are; the right arm alone sits on the box's edge, where
almost nothing falls. With no robot, a landed piece's main way out is toward the wall (36–39%), then
right (23–25%), left (20%) and back under the HIVE (18–19%).

**The best of them** (5 Oct 2026, `sim-review/body-shapes-shortlist.png`; numbers from
`BodyShapeSpillTest.rightHookAtTheSpill`, each robot where its card shows it, 200 TIPs a load; each cell is
8 POLLEN / match start). "Blue half": the share of the spill past the centre line 3 s after the TIP (no robot: 34% / 41%).

| Design | Kept a TIP | TIPs a falling piece touches it | Blue half |
|---|---|---|---|
| *No moving parts, 18 × 18 all match* | | | |
| 1 · Plain, 18 × 18 | 1.4 of 8 / 0.9 of 6 | 4 / 13 | 33% / 41% |
| 15 · Rigid V, 14 × 16 chassis, fixed flaps 2 in out, 2 in forward | 2.0 / 1.3 | 21 / 25 | 27% / 36% |
| *Fold out before the TIP, little lands on them* | | | |
| 3 · Funnel, 16 × 16, flaps 3 in out, 2 in forward | 1.9 / 1.2 | 5 / 20 | 28% / 35% |
| 6 · Short-chassis funnel, 18 wide × 15 long, flaps 3 in out, 3 in forward | 2.1 / 1.5 | 5 / 30 | 25% / 32% |
| **13 · Large right hook**, 18 × 14 chassis, one 10 in right arm + crossbeam | **2.8 / 2.5** | **7 / 25** | **10% / 11%** |
| *Reach into the spill: keep more, touched in many TIPs* | | | |
| 14 · Small right hook, 18 × 16 chassis, one 8 in right arm + crossbeam | 4.3 / 3.5 | 82 / 128 | 8% / 8% |
| 2 · Long U, walls 6 in forward | 3.6 / 2.4 | 177 / 171 | 13% / 24% |
| 8 · Front C, 18 × 12 chassis, 12 in arms + crossbeam | 4.7 / 2.8 | 180 / 180 | 9% / 20% |

- The right hooks keep the most of the spill on our half. The large one does it with almost no touches: its face and
  crossbeam sit halfway between the spill's 100% and 90% lines and its arm halfway between the match-start spill's
  right edges (centre x 61.6). The small one wraps the 90% boxes of both spills (face 37.5 in out, arm on x 69.2, an
  8 in arm, so a 16 in chassis): it keeps more than the Long U but the outer tenth of the spill lands on it.
- A rigid V inside 18 × 18 (nothing moves) keeps what the fold-out funnel keeps. Deeper or longer rigid Vs keep a
  little more but are touched in 49–97 TIPs (`body-shapes-shortlist.csv` has them all).
- Left out: the wide funnels (4, 5; no better than 3 and 6) and the long funnel (7; kept and touched like the Long U);
  they and the other ideas are in `body-shapes.png` and `body-shapes-ideas.png`.
- The simulator lands the spill about 6 in short of the 3 Oct films (median 42 in against about 48), and the roll
  after landing is a placeholder: every parking line here moves once `doc/spill-test.md` is run.

![The best of them, each design with its numbers](sim-review/body-shapes-shortlist.png)

**On option 3, the baseline robot** (`ShapeMatchTest`, 5 Oct 2026, after merging `claude/simulator`): the same
shapes on the build team's option 3 (14.5 in square, 14 in intake), through its own Auto, qual-right-o3, with the
same partner; 20 runs, normal / slow tiles. The rigid V's flaps run from the front corners out to 18 in wide (1.75 in
out and forward); the hooks have a 9.5 in arm (the longest R105 allows down: 14.5 + 9.5 = 24 in) or an 8 in one, and
a crossbeam across the 14.5 in chassis, placed on the same 95% lines.

| Design | AUTO points | TIP 3 in | Our robot PARKs | Runs with a G409 touch | TIP 2's spill on the blue half |
|---|---|---|---|---|---|
| 1 · Plain option 3 (qual-right-o3) | 64.8 / 57.3 | 11 / 5 of 20 | 11 / 5 | 0 / 0 | 31% / 32% |
| **15 · Rigid V** | **71.0 / 69.8** | **16 / 15** | **16 / 15** | **0 / 0** | 28% / 28% |
| 13 · Large right hook (9.5 in arm) | 64.3 / 60.5 | 11 / 8 | 9 / 6 | 7 / 19 | **10%** / 14% |
| 14 · Small right hook (8 in arm) | 63.3 / 59.8 | 10 / 7 | 9 / 7 | 13 / 19 | 8% / 16% |

- **The rigid V is the one to look at on option 3.** Its flaps turn the 14 in intake's mouth into an 18 in one, and
  the simulator's biggest lever is intake width: TIP 3 and PARK in 16 of 20 instead of 11, no G409 touches. It adds
  no moving parts. Worth a cardboard test before trusting: the flaps' angle and the pieces' bounce are guesses.
- The hooks still keep TIP 2's spill on our half, but on option 3 they cost points and are touched by falling
  pieces in a third of the runs or more.
- The plain robot's slow-tile number here (57.3) is below `claude/simulator`'s README (61.3); same code and seeds
  for every row of this table, so the comparison between rows holds.

**In the Qualifier Auto, on the 18 in robot** (`ShapeMatchTest`, 5 Oct 2026, before option 3 became the baseline;
these numbers were run before merging `claude/simulator`, whose changes move them a little): four of them through the whole of qual-right-v3 with its
partner, 20 runs each, normal tiles / tiles with 3x the friction. Each shape keeps qual-right-v3's route
(`tools/auto-routes/qual_shapes.py`), with the spots where the robot's front must reach something (the far FLOWER,
the GARDEN, PARK) moved for its shorter chassis. A hook (its arm and crossbeam, hinged at the bottom of the chassis'
front face) rides stowed, swung up 90 degrees inside the front of the frame. As TIP 2 starts it swings down (0.3 s)
while the robot slides about 4 in toward the centre line, and it swings back up as the robot drives off, 1 s after TIP 2
settles, not 0.5. Its arm is built on one side, the one facing the centre line at the end of the field it starts at:
the robot's left for qual-right-v3, on either alliance (blue runs it rotated half a turn). "Blue half": TIP 2's spill on the other
alliance's half 3 s after it starts.

| Design | AUTO points | TIP 3 in | Our robot PARKs | Runs with a G409 touch | TIP 2's spill on the blue half | Held at TELEOP |
|---|---|---|---|---|---|---|
| 1 · Plain (qual-right-v3) | 72.5 / 70.3 | 18 / 16 of 20 | 14 / 13 | 0 / 0 | 28% / 26% | 1.1 / 1.7 |
| 15 · Rigid V | 72.5 / 69.5 | 19 / 17 | 10 / 6 | 1 / 0 | 27% / 26% | 0.7 / 1.5 |
| **13 · Large right hook** | **71.0** / 67.8 | 19 / 16 | 4 / 3 | **1** / 20 | **8%** / 16% | 1.6 / 1.3 |
| 14 · Small right hook | 70.0 / 72.0 | 18 / 20 | 4 / 4 | 12 / 19 | 11% / 17% | 1.3 / 2.0 |

- On normal tiles the large hook does in a match what it does parked: it keeps TIP 2's spill on our half (8% crosses
  against 28%) and is touched in 1 run of 20. The rigid V changes nothing measurable.
- The hook costs PARK: holding the spill half a second longer leaves too little time to PARK in most runs (with the
  0.5 s wait it PARKs in 9 runs but 18% of the spill crosses). A route built around the hook would win that back.
- **On slow tiles the spill lands about 4 in further out** (8 POLLEN median 46 in from the wall against 42), right
  on the large hook's crossbeam (46.6 in), so it is touched in every run. The 3 Oct films put the real landing at
  about 48 in, nearer the slow tiles than the normal ones: the hook's spot moves out with the landing, and
  `doc/spill-test.md` measures where that is before anyone builds one.
- The small hook, wrapped round the 90% box, is touched in most runs either way.
- Could the same hook, part-way up, also be a backboard that drops NECTAR into a FLOWER? No: it is 12 in too short,
  and a FLOWER is worth points only in the last 60 s. [`doc/flower-backboard.md`](doc/flower-backboard.md) (#158) has
  the rules, the geometry, what the simulator can't model, and a cardboard test.
- To watch them: [`shape-matches-advantagescope.zip`](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/claude/biobuzz-robot-body-designs-hi386c/sim-review/shape-matches-advantagescope.zip)
  (model `BIOBUZZ Robot (match shapes)`, layout, best and typical log of each design; its README.txt says how).
  Rerun: `python3 tools/auto-routes/qual_shapes.py` writes the routes; `BIOBUZZ_SHAPE_MATCHES=1 ./gradlew
  :TeamCode:testDebugUnitTest --tests '*ShapeMatchTest*'` runs them (`build/sim-logs/shape-matches.csv`).

**Staging our preloads in the hook** (`StagedPreloadsTest`, issue #159, 5 Oct 2026, mentor idea; simulator only).
qual-right-v3 waits at N_FIRE for the partner's TIP 1 before firing our preloads. The idea uses that wait. Lower the
hook, push the preloads out into it (`Outtake`), lift it, fetch the far FLOWER, fire the FLOWER at TIP 1, then pick
the preloads up again and fire them for TIP 2. The FLOWER alone can't TIP: it is 4 pieces and a TIP needs about 8. The
routes are `tools/auto-routes/qual_stage.py`. 20 runs each, normal tiles / 3x friction, partner as above:

| Route | AUTO points | TIP 2 at | TIP 3 in | Our robot PARKs | Runs with a G409 touch | Loose on our half at 30 s | Staged taken back (of 4) |
|---|---|---|---|---|---|---|---|
| 1 · qual-right-v3 (plain), no staging | **72.5** / 70.3 | 13.3 s / 13.3 | 18 / 16 of 20 | 14 / 13 | 0 / 0 | 8.6 / 9.6 | – |
| 13 · qual-right-v3-large-hook, no staging | 71.0 / 67.8 | 13.5 / 13.5 | 19 / 16 | 4 / 3 | 1 / 20 | 10.2 / 10.9 | – |
| Staged in the hook at N_FIRE, as first drawn | 43.3 / 71.3 | 14.4 (6 runs) / 14.0 | 6 / 19 | 1 / 5 | 0 / 20 | 6.5 / 11.8 | 3.4 / 3.95 |
| **Staged in the hook from y 120** | 67.3 / 66.3 | 15.9 (19 runs) / 16.0 | 18 / 16 | 0 / 0 | 7 / 20 | 10.7 / 10.9 | **4.0 / 4.0** |
| … quick (hook down on the drive out, 0.2 s settle) | 66.7 / 68.6 | 15.5 / 15.9 | 17 / 18 | 0 / 0 | 8 / 20 | 10.9 / 11.7 | 4.0 / 4.0 |
| Staged from y 120, plain robot (no hook) | 56.0 / 68.0 | 14.6 (14 runs) / 14.6 | 10 / 17 | 4 / 0 | 1 / 0 | 7.2 / 9.9 | 3.95 / 4.0 |
| Hook from y 120, outtake 10 in/s (not 20) | 68.6 / 67.4 | 16.1 / 16.0 | 18 / 17 | 0 / 0 | 3 / 20 | 9.9 / 10.7 | 4.0 / 4.0 |
| Hook from y 120, outtake 40 in/s | 64.8 / 69.4 | 15.7 (18 runs) / 16.0 | 17 / 19 | 0 / 0 | 8 / 20 | 11.3 / 11.3 | 3.95 / 4.0 |

- **With this partner it costs points.** TIP 1 comes at 4.6 s, so we wait only 3.6 s at N_FIRE. Staging takes 1.8 s.
  The FLOWER trip gets back about 3 s after TIP 1, and the staged 4 still have to be taken back before TIP 2. TIP 2
  comes about 2.5 s later than qual-right-v3-large-hook's, and there is no time left to PARK. The idea only pays
  with a partner whose TIP 1 comes several seconds later.
- **The hook holds them.** Pushed out at 20 in/s, all 4 stopped inside the hook's pocket in every run: 5 in past
  the chassis' face on normal tiles, under 2 in on slow ones. Without the hook, 3.6 of 4 stayed within the same
  10 in deep, chassis-wide patch. Lifting the hook never moved them, but the simulator cannot really answer that:
  it removes the hook the moment it starts to lift.
- **Where they lie matters more than the hook.** Staged from N_FIRE, they stop at y 96–101, where the left CELL
  swings up at TIP 1 and throws them (3 of 4 in a typical normal-tile run). On slow tiles they stop short of the
  swing, which is why that row is fine at 3x. Staged from y 120 they stop at y 104–110 and survive. Two more things
  had to change too. The robot turns round only at y 127, because a turn near them sweeps them with its corners.
  And it takes them back one at a time with the webcam, because driving through the cluster pushed it faster than
  the intake took it.
- No staged piece was still on the tiles when TIP 2 started, so TIP 2's spill never landed near them. Holding them
  for a later TIP would need a spot clear of both CELLs' swing and of TIP 2's landing.
- Every staging number is a placeholder: the outtake's speed and spread, the hook's 0.3 s swing, and the CELL's
  swing near the tiles. `doc/staged-preloads-test.md` lists what to measure, and the rules questions (is a piece
  lying in a lowered hook CONTROLLED, G407?).
- To watch them: [`staged-preloads-advantagescope.zip`](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/spike/159-staged-preloads-hook/sim-review/staged-preloads-advantagescope.zip)
  (the match-shapes model, `advantagescope-layout-staged.json` with a magenta ring round each staged piece, the best
  and typical log of four cases; its README.txt says how). Rerun: `python3 tools/auto-routes/qual_stage.py` writes
  the routes; `BIOBUZZ_STAGED_MATCHES=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*StagedPreloadsTest*'` runs
  them (`build/sim-logs/staged-preloads.csv`).

**Robot shapes at the spill** (`BodyShapeSpillTest.atTheLandingLine`, mentor, 5 Oct 2026): each shape parks with
its **chassis's front face on the spill's 100% line** (35 in from the wall; every 8 POLLEN piece lands beyond it, 90%
beyond 38 in), so its walls, flaps or ramps reach into where pieces land. 200 TIPs a spot. "Kept" is the pieces lying
3 s later in the same patch of floor for every shape (15 in behind the face to 8 in ahead, 24 in wide). G409 counts TIPs
where a falling piece touched the robot before the tiles: on the chassis (a foul), or on a guide only (perhaps not
called on a thin passive guide). "Over 4 inside" counts TIPs with more than 4 pieces between the guides at once
(G407). Flaps hinge at the front corners; a 16 in chassis leaves 2 in forward for 3 in out (22 × 18), since true 45°
flaps, 3 in out and 3 in forward, would be 19 × 22 and break R105.

Chassis face on the 100% line, 8 POLLEN (`sim-review/body-shapes.png` has 36.5 and 38 in too):

| Shape (after the start) | Kept a TIP (of 8) | G409 TIPs: chassis | G409 TIPs: guides only | Over 4 inside |
|---|---|---|---|---|
| plain 18 × 18 | 1.3 | 0 | 0 | – |
| **long U** (walls 6 in forward), 24 × 18 | **3.1** | 7 | **114** | 80 |
| (a) 16 + flaps 3 out, 2 fwd, 22 × 18 | 1.8 | 0 | 1 | 11 |
| (b) 16 + flaps 4 out, 2 fwd, 24 × 18 | 1.7 | 0 | 1 | 9 |
| (b) 15 + flaps 4.5 out, 3 fwd, 24 × 18 | 1.9 | 0 | 1 | 26 |
| (c) 18 wide × 15 long + flaps 3 out, 3 fwd, 24 × 18 | 2.0 | 0 | 1 | 30 |
| 16 + long flaps 1 out, 8 fwd, 18 × 24 | 2.8 | 10 | 185 | 172 |
| 18 + low ramps 6 fwd, 2.5 in tall, 18 × 24 | 2.4 | 7 | 108 | 66 |

- **Reaching forward keeps pieces; reaching sideways barely does.** The long U keeps 3.1 of 8 a TIP against the plain
  robot's 1.3. The flap shapes (a), (b) and (c) keep 1.7–2.0 whatever their width, and almost never touch a falling
  piece: their flaps reach only 2–3 in into the spill. A chassis cut shorter to allow 24 in of width isn't worth it.
- **What it costs:** guides that reach 6–8 in forward are under the falling spill. The long U's walls touch a falling
  piece in 114 of 200 TIPs (and its chassis in 7, pieces deflected off a wall), and more than 4 pieces sit between
  the walls in 80. Moving the face nearer, to 36.5 or 38 in, adds little kept and many chassis touches.
- **Low guides** (the short wedges beside a narrow "floating intake" roller in a photo): 2.5 in ramps 6 in forward
  keep 2.4 against the long U's 3.1 and are touched about as often. Pieces bounce over a low guide.
- **Match start** (3 NECTAR, then POLLEN until it tips: 6 pieces a TIP): long flaps 2.3 a TIP, long U 2.0, low ramps
  1.6, the flap shapes 1.1–1.3, plain 0.9; the NECTAR lands in the left part of the box. `sim-review/body-shapes-loads.png`
  shows where each load lands, where it lies 3 s later, and each shape's numbers for both.

![Each shape from above with its sizes, parked with its chassis face on the spill's 100% line](sim-review/body-shapes.png)

![The two loads: where the spill lands and where it lies 3 s later, and each shape's numbers for both](sim-review/body-shapes-loads.png)

![The shapes in 3D, as AdvantageScope draws them (the see-through box is what the simulator bounces pieces off)](sim-review/body-shapes-3d.png)

**To watch them in AdvantageScope** (no build needed): download
[`body-shapes-advantagescope.zip`](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/claude/biobuzz-robot-body-designs-hi386c/sim-review/body-shapes-advantagescope.zip),
copy its `Robot_BIOBUZZShapes` folder into `%APPDATA%\AdvantageScope\userAssets` (next to the HIVE assets from the
setup below), restart AdvantageScope, import its `advantagescope-layout-shapes.json`, and open
`body-all-shapes.wpilog`: every shape, face on the 100% line, one after another, 4.5 s each, on a typical TIP and then
on one where a piece lands on the long U's walls. The robot switches shape by itself and the Console names each one.
The simulator ran each shape (pieces bounce off its walls and flaps); it isn't one run redrawn. Rebuild with
`.\gradlew.bat :TeamCode:testDebugUnitTest --tests "*BodyShapeSpillTest*" --tests "*RobotAssetsTest*" --tests "*SpillLandingTest*"`,
then `python3 tools/spill-window/shapes.py` for the sheets. The guides' height, thickness and bounce are placeholders,
like the walls; nothing here is measured on a robot. How far pieces roll after landing is a guess too:
[`doc/spill-test.md`](doc/spill-test.md) is the field test that measures it.

`BodyShapeSpillTest.howCloseCanEachShapePark` asks the other question, how near each shape can park with no touch at
all (front-most point 35–37 in for every shape); kept there, measured from the front-most point, is 17% plain, 41% long
U, 22–30% flaps.

So the picture parks it at a front face of 35 in, centre (58, 26): the closest spot with no G409 touch in
either load, and the dashed box's near edge. Redraw it after the simulator changes:
`./gradlew :TeamCode:testDebugUnitTest --tests '*SpillLandingTest*'`, then
`python3 tools/spill-window/draw.py --face 35` (it needs the Visualizer checkout for the field image:
`AUTO_BUILDER_DIR`, or `../visualizer`).

**To watch these:**

1. Do the one-time setup below with this branch instead of `claude/simulator`; on this branch it also
   builds **BIOBUZZ Robot** (the Limelight as a camera view: right-click the 3D view → *Limelight*) and
   **BIOBUZZ Robot (side walls)**:
   ```powershell
   if (Test-Path $HOME\biobuzz) { git -C $HOME\biobuzz fetch origin claude/dazzling-maxwell-je04gu; git -C $HOME\biobuzz checkout claude/dazzling-maxwell-je04gu; git -C $HOME\biobuzz pull origin claude/dazzling-maxwell-je04gu } else { git clone -b claude/dazzling-maxwell-je04gu https://github.com/Mona-Shores-FTC-Robotics/biobuzz $HOME\biobuzz }
   powershell -ExecutionPolicy Bypass -File $HOME\biobuzz\tools\advantagescope\setup-hive-assets.ps1
   ```
2. Import [`advantagescope-layout-walls.json`](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/claude/dazzling-maxwell-je04gu/sim-review/advantagescope-layout-walls.json) (the usual
   layout with our robot drawn with its walls, sliding) instead of `advantagescope-layout.json`.
3. Open a walls log above. The Console says when the walls go out and in, and lists any `sim: G409`.
4. The single-TIP logs aren't published: `.\gradlew.bat :TeamCode:testDebugUnitTest --tests "*SideWallSpillTest*"`
   writes them to `TeamCode\build\sim-logs`: `side-walls-demo-walls` / `-plain` (the same TIP with and
   without walls) and `long-u-short-of-landing` / `long-u-at-landing` (where to park).



## The research routes

Moved here from `tools/auto-routes/README.md` on 6 Oct 2026, unchanged. The scripts are in `tools/auto-routes/`.

**G409-safe versions (`g409.py`, run 5 Oct 2026 03:50 UTC on `claude/dazzling-maxwell-je04gu`).**
The same routes, built by qual.py's and qual_right.py's own functions, with three changes made to the
built Auto: an "It lands" wait after each "TIP n settles" before the tunnel path; the catch spots moved
back from the landing (S_CATCH 8 in toward the right wall, N_LOW 4 in toward the left: at 4 in a
bouncing piece still reached a robot waiting at S_CATCH, and 8 in at N_LOW clips the far FLOWER); and
"TIP 3?: Yes: PARK" waits 700 ms before driving under TIP 3's landing. The design "... side walls out
at the TIP" is the walls, out as soon as our CELL starts to TIP and in when the robot drives off
(over 25 in/s) or turns. Walls that wait until the spill has landed keep no more pieces than no
walls (`SideWallSpillTest`). 20 runs, normal / slow tiles; zero G409 touches is the bar:

| Auto (experiments file) | Robot | 3 TIPs | Points | G409 touches a run |
|---|---|---|---|---|
| v2 + 500 ms (`qual-right-v2-g409-500`) | plain | 18 / 17 | 72.5 / 71.8 | **0 / 0** |
| v2 + 500 ms | walls out at the TIP | 20 / 19 | 75.8 / 74.3 | 1.8 / 0: falling pieces hit the walls at N_FIRE |
| ShootsLeft + 300 ms (`qual-partner-shoots-left-g409-300-s8n4-t700`) | **walls out at the TIP** | **19 / 18** | **71.5 / 69.8** | **0 / 0** |
| ShootsLeft + 300 ms | plain | 17 / 17 | 68.3 / 68.8 | 0 / 0 |
| Stages + 300 ms (`qual-partner-stages-g409-300-s8n4-t700`) | walls out at the TIP | 10 / 12 | 59.0 / 62.1 | 0.2 / 0.1 |
| Stages + 300 ms | plain | 1 / 4 | 50.0 / 52.7 | 0 / 0 |

So waiting costs v2 about 3 points (2 TIP 3s), and the walls win it back for ShootsLeft, where the
robot waits at S_CATCH with the walls out over open tiles: as G409-safe, it matches today's ShootsLeft
(18 / 19, 70.3 / 72.3, with 10.6 touches a run). Qual-PartnerStages loses most from waiting; with walls
it keeps today's numbers but not yet at zero touches. Not chased further here: qual.py and partners.py
are another session's. The sweep's other waits (0–1000 ms, 0 / 4 / 8 in back) are in the commit log of
this branch; `python3 g409.py 20 shoots-left stages --extra 300 --back 8 --north 4 --tip 700 --designs plain,early`
and `python3 g409.py 20 v2 --extra 500` rebuild and rerun the winners.

`python3 qual_shapes.py` writes qual-right-v3 for the spill shapes (a rigid V, a large and a small right hook:
`qual-right-v3-rigid-v`, `-large-hook`, `-small-hook`), each on its own robot design; `ShapeMatchTest` simulates
them against qual-right-v3 (the repository README, "In the Qualifier Auto").
`python3 qual_stage.py` writes qual-right-v3 with our preloads staged in the large hook while we wait for TIP 1
(`qual-stage-large-hook` as first drawn, `qual-stage-back-large-hook`, `-quick`, and `qual-stage-back-plain`). They
use the simulator-only `HookDown`, `HookUp` and `Outtake`. `StagedPreloadsTest` simulates them (the repository
README, "Staging our preloads in the hook").

