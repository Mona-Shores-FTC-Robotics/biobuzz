# The threads: what each session and branch is doing

Kept by the `claude/simulator` session, which owns the baseline. Updated **6 Oct 2026 15:40 UTC** from the sessions'
transcripts. A thread's numbers count only once they are on `claude/simulator`.

| Thread | Branch | State | What it is doing | Depends on / conflicts with |
|---|---|---|---|---|
| **Baseline** (this one) | `claude/simulator` | the truth | The simulator, the three qualifier Autos on the **Rigid V as drawn** (since 6 Oct 21:15 UTC; the Flat Intake retired: 70.2 / 51.9 / 52.3 at 60 runs on the turret robot, both robots parking in every pairing with the CAD's extractor seated on the FLOWERs; firing from the seat, ShootsRight 72.3), the physics checked against video, the CAD read, the measurement checklist, and the CAD whole-robot model as the one AdvantageScope draws (`cad/advantagescope/`, merged from the CAD chat) | Everything below merges here |
| **Robot body designs** | `claude/biobuzz-robot-body-designs-hi386c` | merging `claude/simulator` (23 commits; conflicts in FieldSim, AutoStudyTest, BodyShape, qual_shapes resolved to the simulator's side) | Then `shape_matrix.py 60` with its Rigid V width/angle variants and Side Rails | Its 20-run deep-dive numbers and "2 of 20" hook claim are superseded; it must not re-add the dropped designs (front C, funnel flaps, dual hooks) or the slow-tiles runs |
| **Intake design** | `spike/<issue>-intake-design` (issue to open first) | starting; has read the code, nothing written | `RobotDesign` intake parameters (mouth width, roller height, time per piece, `intakeHoldsAtMouth`), a "DHS CAD (6 Oct)" design, the three Autos on each option, `doc/intake-design.md` for the mentor | Owns the intake model; body-designs and the baseline leave it alone. Its new fields will conflict with body-designs' merge if both touch `RobotDesign` |
| **FLOWER extractor / robot CAD** | `claude/robotics-meeting-notes-lq2y55` (merged here through 59903ad) | drawn; rebuilding on asks | The whole-robot AdvantageScope model (`cad/advantagescope/Robot_BIOBUZZ`): outer wheel plates, odometry pods, the Rigid V, the floating roller (up 1.3 in for a NECTAR) and the FLOWER extractor on its own shaft (0° down, 146° stowed, arms spread outside the FLOWER; seated, the face 4.59 in from the FLOWER's centre, `doc/robot-cad.md`). Asked for next: the Limelight body drawn, the baked-in red balls removed, the transfer outline when the transfer chat has one | The simulator deploys its extractor and seats the routes on its number (`RobotDesign.extractorSeatIn`); the swing time (0.5 s) is a placeholder until the servo is driven |
| **Pivoting-arm NECTAR scorer** | none stated (scratch three.js page, `fb_cad.py`) | searching four-bar linkages | A rear turret launcher at 10.73 in back, 8.98 in up on the CAD; emptying from the front, capping the FLOWER from the back via a V-notch; the front hood dropped (51% from 16.4 in against 100% from the back) | Assumes a back-mounted turret; the baseline's launcher is frame-fixed at the rear, 12 in up, 75°. Not in the simulator; no repo edits yet |
| **Hook preload staging** (#159) | `spike/159-staged-preloads-hook` | finished 6 Oct 13:12 | Staging our preloads in the hook while waiting for TIP 1; the official Q&A checked (17 questions, none answered; #3, #13, #17 overlap) and three questions drafted in `doc/staged-preloads-test.md` | Not merged. Costs points with a partner that fires at once (README); legality waits on the Q&A. Hardware item: reverse the intake with 4 POLLEN in, 10 times, count jams |

**Decisions every thread works under** (mentor, 6 Oct): the baseline is the Flat Intake with no guide and no
sensors; guides are the Rigid V, Side Rails (one side at a time) and the Ramp Hook; Front Pen, funnel flaps and the
dual hooks are dropped; one tile surface; 60 runs a cell; "known positions, no disruption" for shots.

**Where they meet next**
- Body designs merges here when its matrix is rerun on the current physics. Its README numbers replace none of the
  baseline's; they go in `doc/shape-matrix.md`'s exploration rows.
- Intake design merges here when `doc/intake-design.md` exists; if it changes the Flat Intake's intake, the three
  Autos are rerun and republished from here.
- The extractor's outer plates and the NECTAR scorer's turret both change the robot's footprint and launcher: a new
  STEP in the Drive folder, read with `tools/cad/stepread.py`, updates `doc/cad-6-oct.md` and the simulated design.
