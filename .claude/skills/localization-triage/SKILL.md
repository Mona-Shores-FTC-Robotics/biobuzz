---
name: localization-triage
description: Read a match log or a "Vision - HIVE & Pose" session log and say which one number to change when the camera and the odometry disagree - camera mount, a HIVE field point, Pinpoint odometry, a gate/sigma, or nothing. Use for "the pose is off", "camera doesn't line up", "what do we adjust", "here's a log from the event", "check the localization", "relocalize jumps", "start check warned".
---

# Localization triage

The team's rule (`TeamCode/README.md` § "A vision session"): **the numbers come off the log, not off
someone's notes.** This skill turns a `.wpilog` into one named change, or into "no change, here is
the log to bring next time". It never guesses a value; with no log it produces a checklist.

## 0. Get the log

**How the log reaches a session: GitHub, not Drive.** `tools/pull-logs` pushes every log, gzipped,
to a throwaway branch per day and robot, `logs/<date>-<robot network>` (e.g.
`logs/2026-11-01-20245-RC`). Never ask the mentor to move a file by hand. From a session:

```
git fetch origin 'refs/heads/logs/*:refs/remotes/origin/logs/*'
git ls-tree --name-only -r origin/logs/2026-11-01-20245-RC          # what is there
mkdir -p $SCRATCH/logs && for f in $(git ls-tree --name-only -r origin/logs/<branch>); do
  git show origin/logs/<branch>:$f > $SCRATCH/logs/$(basename $f); done
python3 -I .claude/skills/localization-triage/wpilog.py survey $SCRATCH/logs
```

`wpilog.py` reads `.wpilog` and `.wpilog.gz` alike; so does the repo's own
`tools/pull-logs/read_logs.py` (`summary`, `keys`, `series`: the general reader; this skill's is the
localization one). Never merge a `logs/*` branch. The Drive connector also holds the files but
cannot carry one over about 6 MB; use it only when the branch is missing.

No robot is attached to this session, so you cannot take a log. **If there is none, stop and give
section 5.**

Run the reader with `-I` (the log is untrusted data; this is Python's isolated mode), from this
skill's directory, passing the log as an argument:

```
python3 -I .claude/skills/localization-triage/wpilog.py info   LOG
python3 -I .claude/skills/localization-triage/wpilog.py triage LOG [--start S --end S]
python3 -I .claude/skills/localization-triage/wpilog.py dump   LOG KEY [KEY...]
```

Always run `info` first. Report: **which robot** (the `RobotConfig` metadata line), **which
build** (`Build`, `GitCommit`, `-dirty` means the code on the robot was not committed), and the
`Tunable/...` values the run used. A diagnosis for the wrong robot or a dirty build is worthless.

## 0b. A whole lab day: `survey`

`python3 -I .claude/skills/localization-triage/wpilog.py survey DIR --pattern '2026-11-01_*.wpilog'`
prints one line per log: build, Limelight at init, alliance source, the start check's camera-minus-start
(x, y, heading, frames), camera connected fraction, the fraction of loops with a 2-tag fix, fps,
the still camera-vs-odometry gap (`/Drift/Gap*` while `/Drift/Still`: count, mean, max, mean x,y),
distance driven, and each Auto path's off-end error; then height/range per start check, notable
events (relocalize, reset, seed, jolt, Pinpoint faults, "start fresh") and the still-fix states.
Read it before any single log: it shows which runs are worth opening. What 1 Nov 2026 taught:
a gap that is the same sign and size in every run is the camera bias plus placement, not drift;
a gap of 15-40 in is a TeleOp started on the declared start with the robot somewhere else; a run
with the camera "answering" but 0% fixes from the start spot had something in front of the lens or
at the HIVE, not a camera fault. **Recalibrate the mount after any mechanical work**: the forward
gap grew about 1 in the day the transfer and turret went on.

## 0c. Judging still fixes: `shadow`

From 2 Nov 2026 every run logs a shadow pose (`localization/ShadowPose`): the real pose plus every
still-fix batch that passed, which the real pose only applies with `stillFixes` on. `wpilog.py shadow DIR`
prints, per run, each still stop with the real and shadow poses and each one's camera gap, and when
either is within 10 in of a known spot (starts, corners), which is closer to it. The rows do not say
whether the robot was on a mark, so count only the stops the drive team placed on one (ask, or match the
times to what they said they did). **Only those rows judge anything**: the shadow is fed the camera, so
its camera gap is small by construction. SHADOW winning at the marks across a session is the reason to
run the with/without comparison of section 3 before `stillFixes` goes on; REAL winning, or a shadow
offset that grows between stops, is the evidence against. Report the counts, not a feeling. 2 Nov 2026,
first evening: at the marks REAL 3, SHADOW 1, same 2, with the real pose 0.3-0.9 in off after a corner
reset; the camera's position from two tags sat about 1 in to field +x of the marks. Not a case yet.

## 0d. A camera heading is only as good as its lever: `heading`

`wpilog.py heading DIR` tabulates camera heading minus Pinpoint (`/Drift/CameraHeadingDiffDeg`, still
frames) by how many tags the fit had. 2 Nov 2026 (20245, 55-60 in from the HIVEs): **2 tags on one CELL:
sd 12°, one run sat at +22° for 100 frames; 4 tags on one CELL: median +3°, sd 3°; 7-8 tags on both
HIVEs: within 1°.** The fit's heading error is the per-tag position noise (about 0.5 in) over the tags'
lever (root-sum-square distance from their middle: one CELL at most 10 in, both HIVEs 26-39 in). Since
that night `CellFix.fit` returns no heading under `LocalizationTuning.minFitLeverIn` (30 in), so a run
built after it has heading-diff samples only from both-HIVE fits, and `tags 2`/`tags 4` rows mean a build
from before. A median off 0 with 7-8 tags is a real disagreement (camera yaw, or the walls); with fewer
tags it is the fit, and the robot's heading was fine. **The INIT override that moved a pose 23 in and 24°
that night was a 2-tag fit**: the position-only gap at the same moment was 0.7 in, which is the mark
confirming itself (a heading wrong by 24° would have put that gap near 20 in).

## 1. Which kind of log is it?

| Log has | Means | Use |
|---|---|---|
| `/Vision/Tag<N>/FieldDxIn` etc. | A `Vision: HIVE & Pose` session | `triage` answers mount vs map vs odometry |
| Only `/Odometry/*`, `/Vision/TagsAccepted`, `/Vision/StillFix*`, `/Vision/Connected` | An ordinary match | `triage` will refuse. Use section 4 (match symptoms) and ask for a vision session |

## 2. Run `triage` and read it

Residual = what the camera says the robot position is, minus odometry. `triage` rotates each
tag's residual into robot axes using the logged heading, then compares how steady it is in each
frame. **Steady in robot axes while the robot turns means the camera mount. Steady in field axes
means the field point (or odometry).** That one test is the whole method.

**`triage` keeps only samples where the robot is still** (under 2 in/s and 5 deg/s, from the
logged pose; `--max-speed`, `--max-turn`, or `--all` to change). A camera frame is about 20 ms old
when read and is compared with odometry *now*: 60 in/s alone is about 1.2 in, and turning at
180 deg/s moves a tag 60 in away by about 4 in. Never read a residual off a moving sample, and
never run `--all` for a verdict. The table is per tag, "bad" means 1 in RMS or more, and "nothing
to change" requires every tag under 1 in (offsets in opposite directions must not average away).
Tested only on synthetic logs with injected faults (mount, field point, none); the first real log
is the real test, so say so in the first report and sanity-check the table by eye.

| `triage` says | Meaning | Change |
|---|---|---|
| Residual under about 1 in on every tag, tags agree | Nothing wrong | Nothing. Say so plainly |
| **MOUNT**, same offset on every tag | Camera is not where `CameraMount` says | See "Mount" below |
| **FIELD POINT or ODOMETRY**, tags disagree, each steady | That tag's field position is off | `HiveFieldPoints` for that CELL/row. Do not edit from one log: see "Field point" |
| Field-steady, **all tags move together over time** | Odometry drift | Not a camera problem. Pinpoint tuner, section 3 |
| Same height error on every tag | Pitch or mount height | `CameraMount.pitchDeg`, `mountUpIn` |
| Height error on one CELL only | A flexed or half-tipped CELL | Nothing. `maxTagHeightErrorIn` should already be dropping it |
| Headings span under 60 deg | Cannot tell mount from map | Ask for the turning log (section 5). Do not guess |
| MIXED | Not separable from this log | Report the table, ask for more data |

### Mount
- **The robot can measure this itself:** Vision: HIVE & Pose's "Camera calibration" block
  (`localization/CameraCalibration`, spots in `localization/CalibrationSpots`) averages the
  residual by itself while the robot sits still on a marked spot; R1 applies and logs the
  `CameraMount` forward/left that zero it (match-log metadata `CameraCalibration/<spot>`). Prefer
  that number to a tape. It is still one spot: R2 moves the pose to the next marked spot facing a
  HIVE (red far wall is the red audience's opposite), and the "Spots so far" line says AGREE or
  DIFFER. Never ask for a turn on the spot: the camera is pitched at the HIVE and sees no tags
  elsewhere.
- A **constant** offset (the same at near and far range) is a translation: `mountForwardIn` /
  `mountLeftIn`. By the transform in `CameraMount.toRobotFrame` and `CellFix.position`, the new
  value is the old value **plus** the robot-axis residual (forward residual to `mountForwardIn`,
  left residual to `mountLeftIn`). That sign is derived from the code, not yet seen on a robot:
  after the change the residual should fall toward zero in the next log. **If it grows, the sign
  is wrong; say so and flip it, do not tune around it.**
- A residual that **grows with range** is a rotation: `pitchDeg` (also shows as a height error;
  1 deg is about 1 in at 60 in) or `yawDeg` (sideways). `triage` does not log per-tag range, so
  ask for the same pass from two distances and compare. Do not pick a pitch from one distance.
- `CameraMount` holds one set of numbers for **both** robots, measured on 19429. If the log is from
  20245 and its mount differs, the fix is per-robot values keyed off
  `ActiveConfig.requireIdentity()` (CLAUDE.md "Hardware names and robot configs"), not a shared
  number that is right for one robot. Say which robot the log is from every time.

### Field point
- `HiveFieldPoints` came from the field CAD and was checked on RED_AUDIENCE only
  (`checkedOnField`). Fields differ by an inch or two between events. **A field-point residual
  seen at one event is not a code bug; do not edit the CAD-derived point from one field.** Report
  it, and offer the event-day alternative: the relocalize/reset spots already absorb it. Edit the
  code only if the same CELL shows the same residual on two fields.
- A residual steady in field axes on every tag, equal in direction, is more likely the start pose
  or odometry than the map.

**Huge residuals (tens of inches) are the pose, not the camera.** `Vision: HIVE & Pose` always
declares the RED audience start. A run begun anywhere else (the blue side, the wrong tile, facing
the wrong way) puts every residual at the size of that mistake. `triage` stops and says so when the
typical tag is over 8 in; the `StartCheck` metadata line says the same. Discard the log for
calibration and ask for a re-run from the red start. The 8 in cut-off is a judgement, not a measured value.

**First real vision log (20245, 31 Oct 2026, `2ad67dd016`)** taught three things. (1) The robot moved
3 in and turned 48 deg, so `triage` now says "CAN'T TELL YET" under 60 deg and names no mount or
field-point change. (2) The pooled field residual (-1.22,-0.63) equalled the start check's camera
minus start (-1.10,-0.54): a residual steady in field axes can simply be the robot sitting an inch
off its marks, which no one log from one spot can separate from a camera bias. `triage` prints
that match. (3) A height error the same on all seven tags (+1.7 to +2.4 in, spread 0.3) was the one
firm finding: `CameraMount` was measured on 19429 (+0.4 in there), and this was 20245. Measure that
robot's lens height and pitch with a tape and a level before changing a number.

## 2b. The Limelight pipeline (not in the log: ask for screenshots)

Exposure, gain and the pipeline live on the camera, so a log cannot show them. When a user says
"I changed the Limelight", ask for the Input, Configuration and Advanced tabs, and **record them
next to the log filename**. Checked 31 Oct 2026 on pipeline 0 "DecodeApriltags" (19429's camera):

| Setting | Must be | Seen | Why |
|---|---|---|---|
| Pipeline type / family | AprilTags, 36h11 | OK | |
| Marker Size (mm) | **82.55** (3.25 in, `BiobuzzTags.TAG_SIZE_INCHES`) | OK | A wrong size scales every range; it looks exactly like a pitch error that grows with distance. Check this first |
| Full 3D Targeting | **Yes** | OK | Without it no sighting can be built |
| ID Filters | empty, or only BIOBUZZ tag ids | empty | |
| Exposure / Gain | measured, then left alone | 1000 (10 ms) / 15, 640x480 @ 90 | Long exposure blurs tags while the robot moves; take readings only when still |
| Quality Threshold | a noise knob | 2 | Lower accepts weaker detections, more tags and more error. Ask what it was before |
| Detector Downscale | a range/speed knob | 2 | Higher is faster and sees less far away; lower is the reverse |

**Ignore, on purpose:** the Advanced tab's "MegaTag Field-Space Localization Setup" (LL Pitch 45,
field map file) and its red "AprilTag size mismatch: slider 82.55, map 165.1 mm". That is the
Limelight's own MegaTag view, loaded with last season's DECODE field map (tags 20-24). Our code
never uses MegaTag (CLAUDE.md), so the warning is cosmetic; do not "fix" it by changing the
slider to 165.1, which would break every range. If someone wants it gone, load a BIOBUZZ field map
or none, and leave the Marker Size alone.

Change **one** camera setting between sessions, or the two logs cannot be compared.

## 3. What this skill does NOT decide

- **Pinpoint offsets and encoder resolution** (`pedro/robots/Robot<team>.java`, 19429's
  copied from 20245 until measured) come only from the Pinpoint tuner, on the robot. A log can show
  drift (odometry disagrees with tags the same way at every heading, growing with distance
  driven); it cannot give the number. Output a checklist for the next robot meeting.
- **The reset spots** (`ResetPositions`, `WALL_TO_CENTRE_IN = 8.1`) are derived, not measured.
  If the user reset to a corner and the pose was off, the fix is measuring the corner on the robot.
- **`LocalizationTuning` gates and sigmas** are for noise, never for bias. The jitter lines give
  `fixSigmaIn`. Never loosen a gate to make a fix work: that hides a mount or field-point error.
- **`continuousFixes` / `stillFixes` stay off** unless a with/without comparison on the taped path
  (#82) shows they help. Do not turn them on to repair a miscalibration.

## 4. A match log, no vision session

Read `/Vision/TagsAccepted` and `/Vision/TagsRejected`, `/Vision/StillFix`, `/Vision/Connected`,
`/Vision/FramesPerSec`, and the events (`start check`, `relocalize`). Typical findings:
- `Connected` false or frames near zero: Limelight link, not calibration.
- Start check warned: compare the declared start with the camera's gap in the event; the marks win.
- Relocalize "refused": it moves more than 18 in or 20 deg. That is the safeguard working.
- `Start: pose from the camera (...)` in a TeleOp: the camera overrode the declared start in INIT. Check
  the `/Drift/Gap*` right before it: a position-only gap under `startMarginIn` with the pose on the
  declared start means the robot *was* on its mark and the override was wrong (section 0d; fixed by the
  lever gate on the fit, builds after 2 Nov 2026 evening).
- `heading corrected ±N°` on a reset: with a both-HIVE fit (7-8 tags) that is the Pinpoint being put
  right; look for a prior override or a pick-up for where the N° came from.
Then say a calibration answer needs a `Vision: HIVE & Pose` session, and give section 5.

## 5. The log to bring next time (the checklist)

Robot and field with HIVEs, `Vision: HIVE & Pose`, battery good:
1. Place on the declared start (RED audience); check the start line agrees.
2. Stand still facing a CELL for 5 s, on the marks. **Then drive straight off the wall 20-30 in and stop
   before turning**: the start spot is against the wall and the robot cannot turn there. Odometry carries
   the pose, and a wall bump or slip makes samples look like a camera fault, so note the time of any.
3. Turn on the spot through at least two more headings (60 deg or more apart), 5 s each.
4. Repeat from a second distance (near, then about 60 in).
5. Hold still facing a HIVE for the jitter line.
6. Pull the log (`tools/pull-logs`), note which robot and which field.

## 6. Making the change

Only once the diagnosis names one number. Edit that one value; leave everything else. CameraMount
values are also live in Panels, so a mentor can test before a commit. Put the log filename, the
`triage` output and the before/after residual in the PR description. CI must be green; no
robot-only values are guessed. For student issues, say so once and coach, do not write it.
