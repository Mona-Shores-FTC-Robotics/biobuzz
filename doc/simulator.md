# The simulator, the Autos and what we've learned

The long version of the [README](../README.md): the baseline robot and its three qualifier Autos, the research behind
them, the branches, and how to watch a simulated match. Numbers are 60-run averages unless they say otherwise.

## Qualifier Autos: the baseline

**The baseline robot: the Rigid V** (mentor, 6 Oct 2026 21:15 UTC: one robot, iterated from here; the Flat Intake
baseline is retired, [DEPRECATED](../tools/auto-routes/DEPRECATED.md)). It is the build team's 6 Oct CAD with the
Rigid V as drawn on it ([the decision](unified-design.md); `AutoStudyTest.drawnV`, named `rigid V`):

- a **15.12 in long × 15.24 in wide** body, the roller 1.94 in ahead of it;
- a **13.8 in intake** across the front, taking a loose piece only when it touches (POLLEN is 2.8 in across,
  NECTAR 3.6 in), one every 0.35 s, 85% of the time;
- **two fixed flaps from the front corners, tips 17.8 in apart and 2.8 in ahead, 4 in tall**: the V. Pieces
  bounce off them into the mouth (flap bounce: the body's, up to 0.3 measured fine);
- a **turret** launcher near the back (it aims without turning the robot), fed by the **transfer** (roller, lane,
  J-wheel, turret axis): a piece takes 0.35 s (the transfer's design figure, untimed) from the intake to the
  launcher, and nothing fires before it has arrived;
- **the FLOWER extractor** (the CAD's: on its own shaft 2.4 in ahead of the face, swinging 146° in 0.5 s, a
  placeholder): it comes down as the robot starts its path in to a FLOWER (down before the turn in finishes), takes
  the stack once seated, the face 4.59 in from the FLOWER's centre, and stays down until the robot has backed 6 in
  clear; every route leaves a FLOWER straight back before turning (mentor review, 7 Oct). The routes stop at the
  seat, so the mechanism meets the FLOWER, not the body.

**Firing from the FLOWER's seat** (mentor, 6 Oct: "the robot shoots while extracting"), tried on ShootsRight first:
the turret streams shots while the extractor feeds, instead of taking 4 and driving to the firing spot. With TIP 2
fired from the far FLOWER and the robot then going down the west side to fire the wall FLOWER's 4 from its seat and
the GARDEN's 4 after, `qual-right-v-seatfire-west` scores **72.3** (TIP 2 at 11.0 s instead of 13.1, TIP 3 in 51 of
60 at 24.6 s, PARK in all 60, G409 14, no problem runs) against the baseline's 70.2 on the same robot. Firing from the
seat and then going back to the old catch spot scored 62.5: the robot arrives as the spill falls (G409 in 21 runs). Not yet the
baseline: adopting it, and carrying it into the Stages Autos, is the next decision (`tools/auto-routes/seat_fire.py`).
Watch it: [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-seatfire-west/Qual-PartnerShootsRight-SeatFire_RigidV_2026-10-07_best.wpilog) ·
[typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-seatfire-west/Qual-PartnerShootsRight-SeatFire_RigidV_2026-10-07_typical.wpilog) ·
[the route](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/qual-right-v-seatfire-west.pp).
The FLOWER scorer is shelved (6 Oct).

Points are average alliance AUTO points over 60 simulated runs (seeds 1–60; 3 TIPs, LEAVE and PARK; a perfect run
is 76). **G409** is how many runs our robot touched a spilled piece before it reached the tiles (the rule: don't
catch or deflect a TIP's spill). Numbers run **7 Oct 2026 19:50 UTC** on the simulator as it stands (each TIP
0.58–1.12 s, pieces rolling as filmed). Each log downloads as `<Auto>_RigidV_<date simulated>_best` or `_typical`.

| Auto | Partner | Points | TIPs | Our PARK | G409 runs | Watch the route (Visualizer) | `.pp` files | Simulated `.wpilog` |
|---|---|---|---|---|---|---|---|---|
| **L-Quals** (`l-quals`, from the drive team's left start; `qual-right-v` before 8 Oct 2026) | Fires its 4 preloads from the right start at once (TIP 1), then parks | **75.0** | 3 TIPs in 58 of 60 | 60 of 60 | 0 | [**Together**](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/L-Quals) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/l-quals.pp) · [partner](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right-high.pp) | [qual-right-v.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/l-quals.pp) · [partner-preloads-right-high.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/partner-preloads-right-high.pp) | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/l-quals/L-Quals_RigidV_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/l-quals/L-Quals_RigidV_2026-10-08_typical.wpilog) |
| **Qual-PartnerStages, angled partner** (`qual-stages-angled-v`) | Can't shoot: starts angled, back corner on the wall behind the left CELL, its 4 POLLEN on the tiles along its side; drives to the LOADING ZONE and parks square against the alliance wall at (11, 115), above our PARK at (10.5, 95) (mentor, 7 Oct: the old path was too steep and parked on top of our spot; 8 Oct: we park 8 in inside the zone, not a corner's tip) | **61.0** | 3 TIPs in 23 of 60 | 60 of 60 | 16 | [**Together**](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Angled) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-angled-v.pp) · [partner](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-angled-park.pp) | [qual-stages-angled-v.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/qual-stages-angled-v.pp) · [partner-angled-park.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/partner-angled-park.pp) | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-angled-v/Qual-PartnerStages-Angled_RigidV_2026-10-07_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-angled-v/Qual-PartnerStages-Angled_RigidV_2026-10-07_typical.wpilog) |
| **Qual-PartnerStages, partner against the wall** (`qual-stages-wall-v`) | Can't shoot: back against the wall behind the left CELL, its 4 POLLEN on the tiles along its side; drives straight forward and parks at the top of the LOADING ZONE | **53.3** | 2 TIPs in 56 of 60 | 60 of 60 | 18 | [**Together**](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Wall) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-wall-v.pp) · [partner](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-stage19-side-park.pp) | [qual-stages-wall-v.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/qual-stages-wall-v.pp) · [partner-stage19-side-park.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/partner-stage19-side-park.pp) | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-wall-v/Qual-PartnerStages-Wall_RigidV_2026-10-07_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-wall-v/Qual-PartnerStages-Wall_RigidV_2026-10-07_typical.wpilog) |

**Qual-PartnerShootsRight:** we start at the left start facing the HIVE, spun up. The partner's 4 make TIP 1
(4.5 s); our preloads and the far FLOWER's 4 make TIP 2 (13.3 s). We back off as TIP 2 starts, wait 1.3 s from
its start for the spill to land, then drive south through it: the V's flaps bounce the spill into the mouth,
which is what a plain intake can't do. We fire what we caught, sweep west along the south wall through TIP 1's
leftovers into the GARDEN, fire that load (TIP 3, about 27 s), and PARK, whether or not TIP 3 has started yet:
shots already away still count if the TIP finishes within 8 s of AUTO ending (§10.5), and PARK is not traded away.

**Qual-PartnerStages** (the partner can't shoot or set pieces down; its 4 POLLEN start on the tiles touching it,
G304, and its only move is forward): we fire our preloads (TIP 1), drive north through the tunnel catching
TIP 1's spill, turning out of it at x 55.5 so the flaps stay off the centre line, fire the catch, take the row,
fire it, then the far FLOWER's 4 (TIP 2, about 19–21 s), then TIP 2's spill and the GARDEN, and PARK when we can.
- **Angled partner:** its POLLEN end up beside where we fire at the left CELL; the row is taken side-on.
- **Against the wall:** the row is swept square, the face stopping on each piece (4 of 4); the V keeps the rest
  in its mouth. It parks where we would, so we don't.

**TIP 3 with a partner that can't shoot** now comes with the wall partner in about a third of runs (the swept row
and the V's catch make TIP 2 by 19 s), not yet with the angled one.

**Known on these routes:** G409 touches in 8–16 runs of 60 (the V's flaps at the edge of a falling spill; the Flat
Intake had 0), to be driven down as the V's angle is settled. The angled Auto clips the HIVE frame in 4 runs of
60, all runs where TIP 1 failed (2 of our 4 preloads missed) and the route re-entered the tunnel at the end with
the flaps 0.4 in from the foot bar; a launcher at the mentor's target accuracy removes the case, and the route
will get a no-TIP-1 branch. The wall Auto's row approach turns 70% of the way to the row (80% put a corner over
the parked partner in 5 runs; 45–60% cost 2–5 points).

**Not adopted:** holding TIP 1's spill at the drop zone before the tunnel run (about +2 points, G409 from 18 to
51 runs of 60); the 2.5 and 2.2 in flaps (ShootsRight 69.5 → 67, the wall Auto 55.7 → 52); flap bounce 0.5.
How the routes were tuned: [the qualifier Autos](../tools/auto-routes/README.md#the-qualifier-autos) and
[body evaluation](../sim-review/body-evaluation.html).

- **Together** plays both robots at once; **ours** / **partner** opens one robot's Auto. No login.
- **best** / **typical**: the highest-scoring and the median of the 60 runs, from the newest
  [Simulate Auto](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/actions/workflows/simulate-auto.yml)
  run of that Auto on the day in its name (a re-simulation another day adds a file with that day's date).
- **These three are the only Autos kept up to date.** Older ones, for earlier robots and never re-run:
  [tools/auto-routes/DEPRECATED.md](../tools/auto-routes/DEPRECATED.md).
- Robot shapes and side walls: [What we've learned](#what-weve-learned-research-simulated), below. The
  baselines above use neither.
- The simulator's launcher is the event's best robot's (2 s spin-up, 0.2 s a shot, from the Saline stream), its
  75° and the intake's 0.35 s a piece are guesses, and most of the commands these Autos use exist only in the
  simulator so far.

## What we've learned (research, simulated)

Each study's full write-up is linked; these are the conclusions, with the date they were run. All of it is
the simulator, nothing measured on a robot: [what the simulator knows, and what it guesses](what-the-simulator-knows.md).

**Checked against the first real event** (Saline Preview, 3 Oct 2026; reviewed 7 Oct). A good AUTO there was one
TIP and parking, 28–36 for the alliance; the best was 40; the simulator's baselines say 51–92 because their
partner TIPs and their shots never bounce back out. The calibration held: a fresh CELL tipped on the third
POLLEN. [What Saline says about the simulator](saline-preview-day2.md), with where to frame-step the stream
for the HIVE's swing time.

**Names** (6 Oct 2026). A **spill guide** is a shape on the robot that keeps landed pieces from scattering and
steers them to the intake; it must not touch a falling one (G409), so it is lined up outside the **Drop Zone**,
where a spill's pieces first touch down (the **90% Drop Zone** holds 9 in 10 first touches; the **full Drop Zone**
all of them). The guides being explored, each on the Flat Intake:

| Guide | What it is | Status |
|---|---|---|
| **Rigid V** | Two fixed flaps from the front corners out to 18 in wide | Leading. Next: its width (18, 20, 22 in) and angle |
| **Side Rails** | A wall down each side that slides forward; each side on its own, so only the centre-line side goes out (to keep NECTAR off the other half) | Open; not yet run on the Flat Intake |
| **Ramp Hook** | One arm and a crossbeam, swung down, with a ramp front that also empties a FLOWER; replaces the 9.5 and 8 in hooks | Open; simulated as the 8 in hook until the ramp is designed |
| Front Pen | Arms forward and a crossbeam: a pen in front of the intake | Dropped: pieces must fall into it (G409), and it holds more than 4 (G407) |
| Funnel flaps | Short flaps on a smaller chassis | Dropped: a weaker Rigid V |

- **A perfect launcher alone doesn't make the shots** (6 Oct, 60 runs each): with no shot-to-shot spread at all,
  the Autos' shots still score only 92–96%, and firing only once the robot is still and within 0.5° of the CELL
  changes nothing (`BIOBUZZ_AUTO_SPREAD`, `_AIM_DEG`, `_FIRE_STILL`). ShootsRight's remaining 4% are shots still
  in the air when AUTO ends; the Stages Autos' one miss a run was the 4th preload, fired 0.45 s after the 3rd
  tipped the CELL, into a CELL already swinging (at 0.2 s a shot and with the dwell, all four are in before it moves) (the firing distance makes no difference). So in the simulator a
  launcher with no spread scores every shot that matters. "Known positions, no disruption" (mentor) is the right requirement; the simulator says it is worth about
  2–4 points a match on its own, and the 8-POLLEN TIP 2 stays fragile until the shots are near 100%.
- **What to measure on the robot, in order**: [next meeting checklist](next-meeting-checklist.md).
- **The build team's 6 Oct CAD has a 9.4 in mouth, not 14** (read from the STEP, 6 Oct): the simulated Flat Intake
  has been more generous than the design, and two things in the CAD look worth a question (the roller clears the
  floor by 2.84 in, a POLLEN is 2.8 in tall; the launcher belt gears the flywheel down). [CAD vs. the
  simulator](cad-6-oct.md).
- **The intake is the limiter, and the simulator now says why** (6 Oct 13:40 UTC). In every Auto, 15 pieces a run
  reach the intake's front too high (bouncing, their top above the 5 in mouth) and 16–25 arrive while it is busy
  (one piece per 0.35 s; the rest bounce off the body). Catching TIP 1's spill at the drop zone before anything
  else, which should be the best load on the field (3 NECTAR in it), scored below the current Stages routes in all
  16 variants tried: the wait costs more than this intake catches. Vectored rollers that hold pieces against the
  front and feed one throat would turn the "busy" misses into a queue; the roller height decides the "too high"
  ones. Both are build-team numbers; [what the simulator knows](what-the-simulator-knows.md).
- **The Rigid V widens the Flat Intake's mouth, and that's the biggest win found** (every Auto, 60 runs,
  6 Oct 12:55 UTC). Two fixed flaps from the front corners out to 18 in wide turn the 14 in mouth into an 18 in
  one. In Qual-PartnerShootsRight: **64.1 points, TIP 3 in 33 of 60** (the Flat Intake: 55.0, 7; the full-width
  robot: 62.8, 35, G409 in 11 runs), no moving parts; G409 in 3 runs (its flaps at TIP 3's spill), still to fix.
  **With pieces rolling as filmed, a spill is only worth chasing with a guide:** the Rigid V keeps the route that
  sweeps the spills, while the Flat Intake's was retuned to take still pieces instead. With a partner that can't
  shoot it scores about the same as plain (TIP 3 is out of reach for every guide). The Ramp Hook (simulated as the
  8 in hook) on the retuned route scores **66.3, TIP 3 in 35 of 60**: it holds TIP 2's spill at its spot, so the
  catch is a real load; but falling pieces touch it in half the runs (G409), which is the problem to solve first. Worth a cardboard test: the flaps' angle and the bounce are guesses.
  [Every shape on every Auto](shape-matrix.md); the earlier study: [robot shapes](robot-shapes-and-walls.md).
  **To watch the Rigid V:** Qual-PartnerShootsRight on it, [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-o3-rigid-v/Qual-PartnerShootsRight_FlatIntakeRigidV_2026-10-06_best.wpilog)
  · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-o3-rigid-v/Qual-PartnerShootsRight_FlatIntakeRigidV_2026-10-06_typical.wpilog), with the usual layout.
- **Side Rails and hooks keep more of a spill, but falling pieces hit them** (G409) where they reach into the
  Drop Zone. Both rails out (the "long U") were touched in most TIPs; the hooks keep TIP 2's spill on our half
  but were touched in a third of runs or more on the Flat Intake. [Side walls and shapes](robot-shapes-and-walls.md).
- **A TIP takes about 0.5–1.2 s, most often about 1 s** (from video, 6 Oct): the simulator now draws each TIP's
  time from 0.58–1.12 s. The Autos now time their wait for a spill from the TIP's start, not from the CELL settling.
  [Tip timing](tip-timing.md).
- **Spilled pieces roll much further than the simulator had them** (from a match video, 6 Oct 12:00 UTC):
  NECTAR kept about 23 in/s for over a second and ran to the wall; a lone POLLEN slowed at about 3 in/s². The
  model now matches, and every number on this page was rerun with it: spills end up further away, harder to
  collect, which cost the Flat Intake most. [Rolling](rolling.md).
- **Where a spill lands:** first touch 35–47 in from the CELL's wall, 1.1–1.4 s after the TIP starts. A
  plain robot facing the HIVE is clear with its front face 35 in or less from that wall.
  [Spill window](../sim-review/spill-window.png); to check it on a real TIP: [filming a spill](spill-test.md).
- **Staging our preloads in a hook while we wait for TIP 1** (#159): costs points with a partner that
  fires at once (we only wait 3.6 s). [Staged preloads](staged-preloads-test.md).
- **A FLOWER backboard on the hook** (#158): the hook can't reach high enough (12 in short), and a NECTAR in
  a FLOWER only counts in the last 60 s of TELEOP. Not worth it yet. [FLOWER backboard](flower-backboard.md).
- **Decision (6 Oct): one robot, Rigid V + FLOWER extractor + FLOWER scorer.** The hook is no longer a spill
  catcher (archived). Owners and the envelope: [One robot](unified-design.md).
- **Ramp Hook vs Rigid V** (6 Oct, current physics, 60 runs, each guide on a route drawn for it; replaces the deep
  dive below): with the partner that shoots, the long Rigid V (18 in, 30°) does best (68.8, 3 TIPs in 41 of 60, PARK
  in 57, G409 in 6). With a staging partner the Ramp Hook does best, holding TIP 1's spill at the south end so TIP 2
  comes off the staged row (55.7 and 51.0 against the Flat Intake's 51.6 and 47.3), but falling pieces touch it in
  28-30 of 60 on every Auto. No guide gets 3 TIPs with a staging partner yet. [The report](../sim-review/body-evaluation.html)
  (also [online](https://claude.ai/artifact/Q1AFHpfZZ9L16yzNccWvQv)); matches to watch:
  `sim-review/body-evaluation-advantagescope.zip`; numbers: `sim-review/body-evaluation/*.csv`; routes:
  `tools/auto-routes/guide_routes.py`, `park_first.py`.
- **Keeping 4 of each spill, every qualifier partner, every body** (the deep dive, 6 Oct, on the physics before the
  filmed rolling and TIP times, 20 runs: superseded, to be rerun): picking up all 4 of
  TIP 2's spill leads to 3 TIPs in 87-96% of matches with a shooting partner (36-49% otherwise). The dual hook
  keeps 4 best where it can wait for the spill (left-start partner: 54 to 69 points) but falling pieces touch it in
  about 7 runs of 20; funnel flaps or a rigid V keep about 3.4 with few touches, best with the right-start partner.
  No body gets 3 TIPs with a staging or idle partner. [The report](../sim-review/deep-dive.html) (also
  [online](https://claude.ai/artifact/BojDToxGirRu37fVngdtjK)); matches to watch:
  `sim-review/deep-dive-advantagescope.zip`; how: `tools/auto-routes/keep4.py`, `DeepDiveTest`.
- **The Ramp Hook** (meeting, 6 Oct): the 8 in hook with a ramp on its front, driven into a FLOWER to empty
  it into the intake, and held out to deaden a spill. Cardboard prototypes Thursday. [Ramp hook](ramp-hook.md).

## Branches

- **`claude/simulator`** (this one): the simulator, the current Autos, the research above. Students use
  this branch for anything simulated.
- **`master`**: the robot code. The simulator is never merged there.
- **`sim-results`**: written by the Simulate Auto workflow; the logs this README links to. Don't edit it.
- The research branches (`claude/dazzling-maxwell-je04gu`, `claude/biobuzz-robot-body-designs-hi386c`,
  `spike/158-flower-backboard`, `spike/159-staged-preloads-hook`, `claude/robotics-meeting-notes-lq2y55`)
  were merged here on 6 Oct 2026 and are finished; new research starts from this branch and comes back here.

- **The threads**: which session and branch is doing what, and where they meet: [doc/threads.md](threads.md).

## How to watch them

**The route:** click **Together** in the table above. It opens in your browser; press **Space** to play.
Nothing to install.

**A simulated match** (both robots, every piece, the HIVE tipping): AdvantageScope on a Windows
laptop. Set it up once, then watch any log.

### Set up (once per laptop)

1. Install [AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope/releases) **v27.0.0-alpha-6
   or newer** (the Windows `.exe` under *Assets*), [Git](https://git-scm.com/download/win) and
   [Android Studio](https://developer.android.com/studio).
2. Open AdvantageScope, click **+** at the top → **3D Field**, pick **2026-2027 Field** in the field
   menu and wait until the field appears. Close AdvantageScope.
3. Open **PowerShell** (Start → type *PowerShell*), paste this, press Enter, and wait for **Done**
   (a few minutes the first time):
   ```powershell
   $branch = "claude/simulator"
   if (Test-Path $HOME\biobuzz) { git -C $HOME\biobuzz fetch origin $branch; git -C $HOME\biobuzz checkout $branch; git -C $HOME\biobuzz pull origin $branch } else { git clone -b $branch https://github.com/Mona-Shores-FTC-Robotics/biobuzz.git $HOME\biobuzz }
   powershell -ExecutionPolicy Bypass -File $HOME\biobuzz\tools\advantagescope\setup-advantagescope.ps1
   ```
4. Open AdvantageScope → **File → Import Layout…** → **Downloads** → `advantagescope-layout.json`.

### Watch a match

1. In the table above, click **best** or **typical** to download a log.
2. Drag the file into AdvantageScope.
3. Press **Space** to play. AUTO starts 1 s in.

What you see: our robot, **BIOBUZZ Robot**, built from the team's CAD (`cad/advantagescope/`): the chassis
with its outer wheel plates and odometry pods, the floating 14 in intake roller, the Rigid V plates, the FLOWER
extractor (swinging down as the robot nears a FLOWER, up as it leaves), the transfer (lane, J-wheel, chute) with
the held pieces single file on its lane, the turret bearing turning to the CELL while the launcher spins, and the
Limelight, which you can look through (right-click the 3D view → **Limelight**). The launcher itself and the roller's
float are not drawn yet. The partner is the green ghost. The baseline logs above are simulated with that robot
("rigid V"). A log of another design (the superseded Flat Intake ones, or a study) draws nothing right until
you pick **BIOBUZZ Robot (designs)** in the robot row's model menu: that model holds every simulated design
and the log says which to show. The picture never changes what happened: a log is one simulated match with
one robot.

**Another branch:** set `$branch` to it in step 3, run it again, then do step 4
again. It swaps in that branch's robot and field, removing the old ones.

**When the CAD changes:** rebuild the model (`cad/advantagescope/README.md`), commit the folder, and run
step 3 again. The model is committed so no laptop needs the CAD tools.

**If step 3 stops** with an Android SDK or licence error: open the `biobuzz` folder in Android Studio
once, let it finish syncing, and run step 3 again. What the script does:
[`tools/advantagescope/setup-advantagescope.ps1`](../tools/advantagescope/setup-advantagescope.ps1).

## Trying a change

Open the Auto with its **ours** link, change it, and press **Save to GitHub** (it asks once for a
[fine-grained token](https://github.com/settings/personal-access-tokens/new): biobuzz only, Contents read
and write, Actions read). Pick the robot **rigid V** (the baseline; any other name draws with **BIOBUZZ Robot (designs)**). The
[Simulate Auto](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/actions/workflows/simulate-auto.yml)
workflow runs it and the dialog shows every run, with **Download WPILOG**.

## Links

[Visualizer](https://mona-shores-ftc-robotics.github.io/Visualizer/) ·
[Simulate Auto runs](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/actions/workflows/simulate-auto.yml) ·
[`sim-results`](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/tree/sim-results) ·
[Panels](http://192.168.43.1:8001) (on the robot's Wi-Fi) ·
[route scripts and every design's scores](../tools/auto-routes/README.md) ·
[how the spill was filmed and fitted](../TeamCode/README.md#the-spill-filmed-3-oct-2026)
