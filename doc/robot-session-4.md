# Robot session 4: the camera's numbers, the loop's time, and the GARDEN into the corner

Three questions, in this order: **why the camera reads tags high and ranges long**, **which
subsystem makes the loop 16–24 ms**, and **does the GARDEN gather reach the corner**. Each step has
a stop line. 20245 only; no launcher needed.

Follows [robot-session-3.md](robot-session-3.md) and the 29 Oct session on 20245, whose 36 match
logs were analysed afterwards. What that session established:

- **Start, pods, picks and resets work.** The start is (54.06, 7.72, 90°), left wheels on the B–C
  seam, and the camera agrees to 0.3–0.5 in there. The strafe pod's offset was 3.22 in and is
  1.0 in. FLOWER picks land, the push is at 0.5, and cancelling keeps the stick down until clear.
  The corner resets (D-pad left and right) and start fresh (hold Triangle in INIT) work.
- **The camera reads tags high, more so with range:** about +0.2 in at 25–29 in, +2.0 in at 50 in.
  The lens-to-tag distance, which does not depend on pitch, reads **about 3.5 % long**. The CAD
  (`Robot10-30-26.step`) puts the Limelight at exactly 45°, the lens 15.2 in above the floor and
  3.6 in ahead of the wheelbase centre, centred. The code has 14.0 in and 4.0 in. A 3.5° pitch
  error is ruled out by the CAD and the fixed bracket. The leading suspect is the **tag size set in
  the Limelight's AprilTag pipeline**: a size that is off by about 3.5 % scales every range by the
  same amount.
- **The loop is slow with everything on:** Basic Drive 16–18.5 ms mean (p95 27–30 ms), Autos
  23–24 ms, against 7.14 ms for Loop Time Baseline. 11–15 ms of it is the subsystems' updates. The
  log now times each subsystem on its own (`/Loop/Subsystem/<Name>Ms`).
- **R-Quals hits something under the red HIVE** at about y 59–61 on the run up x 57.5: the heading
  jumped 18° and the robot bounced 5 in back. Don't run R-Quals until the brace is fixed. R-Simple
  is fine.
- **Batteries ran low.** Every run after 22:15 logged "battery low" (10.9–11.3 V at INIT). Swap
  batteries at 12.0 V; a sagging battery makes paths and picks look worse than they are.
- **"intake: encoder never moved"** came up during picks. The roller turned, so the intake motor's
  encoder cable is probably loose or missing; MotionWatch only reads it.

## Before leaving the laptop

- [ ] **Mount the goBILDA RGB Indicator Light** on 20245 where the drive team can see it from the
      field, and plug it into **Control Hub servo port 0** (the port `robot_20245.xml` names; 19429
      uses the same port when it gets one).
- [ ] Master pulled; a **full TeamCode install** on 20245, not a Sloth Load: the light added a
      device to `res/xml/robot_20245.xml` since 29 Oct. Then **Validate Hardware**.
- [ ] Charged batteries, more than one.
- [ ] A tape measure, and a phone level app or angle gauge.
- [ ] The HIVE on the field, red side, RIGHT CELL up (match start).

## At the robot, in order

1. [ ] **Tag size, at the Limelight's web page** (the Limelight Hardware Manager finds it with the
       camera on a laptop's USB; on the robot it is `172.29.0.1`, port 5801). Open the AprilTag
       pipeline and read its tag size, in mm. It should be **82.55** (3.25 in, the black square's edge, as
       `vision/BiobuzzTags.TAG_SIZE_INCHES` says). Then measure a printed tag on the practice HIVE,
       black square edge to edge.
       - Pipeline wrong: set it to 82.55 and save the pipeline.
       - Printed tag about 3.14 in: the tags were printed scaled. Reprint at 100 % and change
         nothing on the Limelight, because the competition field's tags are 3.25 in.
       - Both right: write that down; the HIVE's position on the practice field is next.

       *Stop line: pipeline and printed tag both 3.25 in, or the mismatch is known and fixed.*

2. [ ] **Lens to tag 35, with a tape.** Robot at the red start (back on the audience wall, left
       wheels on the B–C seam). Measure from the centre of the Limelight's lens to the centre of
       tag 35 on the RIGHT CELL; the CAD expects about **57.5 in**. Note it, and the camera's range
       to tag 35 from **Vision: HIVE & Pose**'s Tags section.

       *Stop line: the tape and the camera agree within 1 in, or you have both numbers.*

   [ ] **The red HIVE's rocker, with a tape.** The rocker model (`localization/HiveFieldPoints`,
       from the field CAD) has never been taped: its one check, on 3 Oct, was itself a camera
       reading. A HIVE placed or built 2–3 in off, or a raised CELL resting at a different angle,
       makes the camera's ranges look long just as a tag-size error does. With the RIGHT CELL up
       (match start), measure:
       - the axle from the audience wall: the model says **70.75 in** (the field's centre line);
       - the axle above the tiles: **43.95 in**;
       - the centre of the raised tag row (between tags 35 and 36) above the tiles: **49.76 in**,
         and from the audience wall along the floor: **57.65 in**;
       - the raised CELL's angle from horizontal: **30°**.

       *Stop line: every number within 0.5 in (1° for the angle), or the ones that are not are
       written down.*

3. [ ] **The camera's tilt.** Phone level on the Limelight's front face: 45° means the bracket is
       as drawn. Then on the tower the camera hangs from: plumb or not, and which way.

4. [ ] **A fresh camera run.** INIT **Vision: HIVE & Pose** at the red start (hold Triangle in INIT
       if it shows a pose from an earlier run). Hold still, facing the HIVE, for about 10 s at each
       of three distances: about **22 in, 35 in and 50 in** from the HIVE, measured along the floor.
       The 22 in spot puts the tags above the image centre, which separates a tilt from a range
       error. Pull the log afterwards; the fit comes off it, and with steps 1–3 the camera's
       numbers (`CameraMount`, made per-robot if 19429 differs) go in as one change.

5. [ ] **Loop time, named.** **Basic Drive**, two minutes of normal driving with the intake on for
       part of it and a couple of FLOWER picks. Pull the log; `/Loop/Subsystem/<Name>Ms` says which
       update is expensive. Also run **Loop Time Baseline** once more for comparison. Since 29 Oct
       the motor currents are read ten times a second instead of every loop, and the launcher's
       and the turret's commands are sent only when they change (and every 250 ms). Expect
       Basic Drive near 10–12 ms with the intake on, down from 16–18.5. Also check that the intake
       still runs and stops normally, and that the Match page's intake current still updates.

6. [ ] **GARDEN into the corner** (`towardWallIn`, 2 in, new since 29 Oct). Hold driver Triangle
       near the red GARDEN, three times. The robot should finish pressed into the audience wall and
       the red wall with the intake in the corner. Tune `towardWallIn` in Panels → GardenGather (0
       turns it off) and note the value that works.

7. [ ] **FLOWER range and line-up.** The pick now reaches a FLOWER up to 60 in away (was 48, which
       refused 5 picks by 2–5 in on 29 Oct). The line-up now waits for the robot to face the
       FLOWER and to slow below 2 in/s before the push (half a second at most each). Try a pick
       from about 55 in away, and one from close to the line-up spot but turned 90°: the robot
       should turn before it pushes.

   [ ] **FLOWER dwell.** The dwell is 1.5 s, the longest phase of a pick, and the commonest abort
       on 29 Oct was a stick moved during it, by drivers who saw all the pieces were in. Now
       **letting go of Cross (or Triangle) during the dwell means done**: the robot backs out on its
       own and the stick comes up once clear. Try it on three picks and a gather; a stick still
       stops it. The default dwell is now 1000 ms (23 of the 33 dwells drivers ended early were
       over by 1.0 s). Try 800 in Panels → FlowerPicker and note the shortest that still gets the
       pieces when nobody lets go early.

8. [ ] **If time is left: the path-following tuning.** The Foresight block was tuned on 17 Oct,
       before the mass was added and before the strafe pod's offset was fixed. Re-run the Foresight
       Tuner on 20245 and paste its block into `pedro/robots/Robot20245.java`.

9. [ ] **The indicator light** (once the build team has mounted a goBILDA RGB Indicator Light
       on **Control Hub servo port 0**). Run **Validate Hardware**, then INIT **Basic Drive** and check
       the colours: flashing red on a low battery, orange with the Limelight unplugged, flashing
       yellow until the camera's first frame, yellow while the pose is not on the field, the
       alliance's colour once all is well, violet during a pick. If a colour is off, set it in
       Panels → IndicatorSubsystem and note the value.

## Record these, and where they go

| Fact | Value |
|---|---|
| Limelight pipeline tag size (mm) | |
| Printed tag, black square edge to edge (in) | |
| Lens to tag 35, tape (in) / camera's range (in) | |
| Red HIVE: axle from the audience wall / axle height / raised row height / raised row from the wall (in); raised CELL angle (°) | |
| Limelight face angle (°); tower plumb? | |
| The slowest `/Loop/Subsystem/<Name>Ms` and its mean | |
| Loop Time Baseline: hz / mean / max / odometry ms | |
| `towardWallIn` that reaches the corner | |
| Shortest `dwellMs` that still gets the pieces | |
| Battery at rest, start and end | |
| Indicator light positions that needed changing | |

Send the numbers and the match logs (pull them into the shared logs folder) to the session that
changes the code. The camera and loop-time fixes come off the logs, not off notes.
