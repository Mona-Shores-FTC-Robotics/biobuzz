# Handoff: 20245 camera calibration (31 Oct 2026)

Disposable: delete this file when the work lands. Status belongs in an issue (CLAUDE.md); this is
only so a new session can resume. Skill: `.claude/skills/localization-triage/` (read its SKILL.md first).

## Decision made
19429 is being rebuilt as an exact copy of 20245. Until 19429 is tested, **both robots share one
set of `CameraMount` values**, taken from 20245. No per-robot code. No values pushed yet: nothing
below has been tested on the robot.

## What 20245 showed (robot_20245, master @ 2ad67dd016, Limelight pipeline 0 "DecodeApriltags")
- Limelight pipeline is fine: marker size 82.55 mm (3.25 in), Full 3D yes, 36h11, 640x480@90.
  Exposure 500 vs 1000 made no real difference (heights +1.9 vs +2.2). Gain 15. Do not go to 300.
- **Height:** camera reads the raised tag row about +1.9..+2.0 in above the map (49.8) at
  CameraMount 14.0 in / 45 deg, at every distance (49-59 in) and heading. Tape: row is 49.1-49.5.
  Lens tape: 14 5/16 +/- 0.25. Phone on case: 45.0 deg. pitchDeg 42 fixed height but pushed the
  tags 4 in too far; mountUpIn 12 fixed height with no horizontal change but is physically wrong, so
  rejected. Cause unknown; it does NOT affect position. It cuts the height gate's margin to ~1.1 in
  (maxTagHeightErrorIn 3.0). Leave it. Optional: ruler on the printed tag's black square (3.25 in?).
- **Horizontal at the red start** (camera minus odometry, 4 placements): (-1.10,-0.54), (-1.2,-0.6),
  (-0.9,-0.8), (-1.0,-0.5). Mean (-1.05,-0.6), repeatable +/-0.15: systematic, not placement.
  Both HIVEs show x about -1.1, so not HIVE spacing. Camera jitter 0.01-0.06 in.
- **Front-back:** wall to wheelbase centre = R 1.9 + W/2 (W 11.31) = 7.55 +/- 0.15. Declared start
  y is 7.7: fine. Camera reads ~7.1, so the lens is ~3.55 in ahead of centre, not 4.0
  (tape B 9.2-9.5 gives 3.5-3.85, consistent).
- Robot centre = middle of the wheelbase (Robot20245.java comment; pods 6.5 in right, 1.0 in forward).

## Priority (plan hub, 31 Oct evening): launcher first; 0 dedicated localization time. Turret is
LOCKED, so aim = robot heading + position; the heading check (square to a FLOWER, read Panels)
matters more than the 1.2 in. Do the steps below only with idle robot time.

## The calibration block (branch claude/funny-dirac-7k2k37; CI green on the first two commits,
NOT yet run on a robot: the first use tests the button as much as the camera)
Vision: HIVE & Pose, robot on the RED AUDIENCE marks, PLAY, hold still. The "Camera calibration"
section averages by itself (50 still frames) and shows "Camera vs spot ... READY · R1 applies".
Press R1 once: it writes CameraMount.mountForwardIn/mountLeftIn for that run and logs them
(match-log Metadata "CameraCalibration/Red_audience"). Expected from 31 Oct: fwd ~3.4, left ~+1.05.
The "Camera ... in from odometry" line should then drop from ~1.2 to under ~0.3.
Then drive to the RED FAR WALL marks (tile C6, back flat on the far wall, frame's left side on the
C-D seam, facing the audience), press R2 until the Spot line says "Red far wall", place the robot on
those marks, hold still, R1 once. "Spots so far" then says AGREE (commit the two numbers to
CameraMount) or DIFFER (a HIVE point or the marks, not the mount: do not commit, tell the session).
Never turn on the spot: the camera only sees tags facing a HIVE. R2 also offers the two blue spots.
`CameraCalibrationTest` closes the sign loop through CameraMount and CellFix.

## Manual fallback if the button is not on the robot
1. Check Panels CameraMount is back at 4 / 0 / 14 / 45 / 0 (it resets on restart) and the Limelight
   exposure/gain are what you want (write them down).
2. Panels: mountForwardIn = 3.55. Read "Where the robot is". Camera y should go from ~7.1 to ~7.5.
   If it moves DOWN the sign is wrong: set 4.0 and tell the session.
3. Sideways: Panels mountLeftIn = +1.05; camera x should go from ~54.6 toward 55.6. (Or plumb line
   from the lens centre: mountLeftIn = (right wheel face to mark - left wheel face to mark) / 2.)
4. Once both move the camera toward odometry/truth, ask the session to make ONE edit to the shared
   CameraMount defaults (update its "measured on 19429" comment), run the tests, and open a PR if
   wanted. Do not touch mountUpIn or pitchDeg for the height.
5. If the lens measures centred and mountLeftIn does not help: suspect the pod offsets
   (xPodOffset -6.5, yPodOffset 1.0 in Robot20245.java); re-measure from the wheelbase centre.
6. 19429 after its rebuild: copy 20245's Limelight pipeline (the download/upload arrows beside the
   pipeline name in the Limelight UI), same exposure/gain, then the same 5-minute start check.

## Where things are
- Logs: Drive `...\Logs\20245-RC\`; 13:48 HiveAndPose is also on branch `logs/2026-10-31`
  (throwaway, never merge). Two later HiveAndPose logs are still on the hub, unpulled.
- Logs over ~6 MB crash the Drive connector; use a throwaway branch (SKILL.md section 0).
