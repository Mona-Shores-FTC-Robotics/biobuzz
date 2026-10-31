---
name: localization-triage
description: Read a match log or a "Vision - HIVE & Pose" session log and say which one number to change when the camera and the odometry disagree - camera mount, a HIVE field point, Pinpoint odometry, a gate/sigma, or nothing. Use for "the pose is off", "camera doesn't line up", "what do we adjust", "here's a log from the event", "check the localization", "relocalize jumps", "start check warned".
---

# Localization triage

The team's rule (`TeamCode/README.md` § "A vision session"): **the numbers come off the log, not off
someone's notes.** This skill turns a `.wpilog` into one named change, or into "no change, here is
the log to bring next time". It never guesses a value; with no log it produces a checklist.

## 0. Get the log

**How the log reaches a session.** Logs sync to the team Drive (`tools/pull-logs/README.md`), and the
Google Drive connector can fetch them. Tested 31 Oct 2026 on a 68 KB log:
1. `search_files` with `title contains '2026-10-31' and title contains 'wpilog'` (names are
   `<date>_<time>_<OpMode>.wpilog`; `excludeContentSnippets: true`). A pull the user just ran
   appears once Drive for Desktop has synced, which can lag; search again before saying it is missing.
2. `download_file_content` on the id. The result is base64 JSON far too big for context, so the
   harness **saves it to a file** and says where. Never read that file with `Read`; decode it:
   `python3 -I` with `base64.b64decode(json.load(open(path))["content"])`, write the bytes to the
   scratchpad (`logs/<title>`), and run `wpilog.py` on that. Larger files are bigger JSON; the
   tested size is small, so say if one fails.
3. If the connector is unavailable, the fallback is a throwaway branch of this repo (not
   `master`, never merged) with the file uploaded on GitHub (*Add file > Upload files*, 25 MB).

The metadata (`RobotConfig`, `Build`, `Hardware/*`, `Tunable/*`, `StartCheck`) is stored as
`/RealMetadata/<key>` string channels; `wpilog.py` reads them into `info`.

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

- **Pinpoint offsets and encoder resolution** (`pedro/robots/Robot<team>.java`, `xPodOffset`
  marked provisional on 19429) come only from the Pinpoint tuner, on the robot. A log can show
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
