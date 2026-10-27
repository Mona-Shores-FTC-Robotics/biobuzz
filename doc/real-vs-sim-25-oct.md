# The simulator against the first real match logs (20245, 25 Oct 2026)

On 25 Oct 2026 robot 20245 ran **R-Simple** and **R-Quals** from `master` (commit d11eb1af) with the real Auto
runtime (`autokit`, `BuiltAuto`, `AutoRegistration`) and wrote a match log of each run. This is the simulator's
view of those two Autos (`AutoSim`, the `sim-results` logs of 15 Oct and a re-run on this branch) held against
what the robot did, card by card: where each difference comes from, what was fed back into the simulator, and
what only the robot can answer next session. The short version is on
[issue #155](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/155).

> **Correction, 26 Oct 2026 (from the session that wrote the brief).** The robot's pose log runs at about 43 Hz, so the
> 100 ms windows this page first used held four samples and their peaks were noise. Recomputed with a centred 300 ms
> window on the same four logs (taken from that session's message; not re-read here): the 116 in straight peaked at
> **76 and 78 in/s** (Foresight's own 77), accelerated at 63 and 71 in/s² (averaged to 90% of peak) and braked at 48
> and 76 in/s²; R-Simple's curve, which holds heading 90° so part of it is strafe against a 43 in/s maximum, peaked
> at **56 and 57 in/s**, accelerated at about 30 in/s² and braked at 100 to 120 in/s². Everything from the Events
> timestamps and the pose at each card is unchanged: the 2.1 s straight, the end errors, the guard's decision, the
> parks. So **wherever this page says 93 in/s, 100 in/s², 60 in/s², or 73 in/s, read the figures in this note.** The
> columns headed "@93/100/60" below are the simulator at the overstated profile and are kept for the record; the
> corrected runs are in the study table. `driveSpeed` 1.5 in `timing.json` comes from card durations, not peaks, and
> stands.

**What the robot was.** The light chassis: drivetrain, Pinpoint and Limelight only. No turret, launchers,
transfer or FLOWER extractor. Foresight tuned on 17 Oct (`pedro/robots/Robot20245.java`: 77 in/s max forward,
43 in/s strafe, 40 in/s² natural deceleration). Battery 11.95 V at the end of the night. So every number below
about driving is the light robot's; the full robot will be slower to accelerate, longer to brake and will overshoot
more, and the measurement is repeated once the big mass goes on.

**What the Auto runtime was.** `SpinUp`, `LaunchAll`, `Empty`, `Tip` and `CameraBlind` are real on master;
`IntakeFull`, `LeftCellUp` and `RightCellUp` are placeholders that never fire. With no launcher nothing was fired
and no HIVE tipped, so on the night *every* wait ran to its time limit, including `Empty` and `Tip`. The real
timeline is the drawing's time limits plus the driving; the simulator's is its own mechanisms plus the driving.
The driving is the only thing the two can be compared on, and it is the thing this page is about.

**Where the numbers come from.** The real logs (`/Odometry/PedroInches/{X,Y,HeadingDeg}`, `/Events` with the
Auto's `auto: …` trace, `/RealMetadata/*`) are in the mentor's Drive, `2026-2027 FTC Biobuzz / Logs / 20245-RC /`:
`2026-10-25_19-53-14_Auto__R-Quals.wpilog`, `_19-54-37_` (R-Quals), `_19-44-00_` and `_19-49-17_` (R-Simple).
They were read with `tools/log-review/review.py` (branch `claude/sharp-euler-3t56jg`), which prints one row per
card with the pose at its start, where a path ended against its drawn point, how long it took and when the robot
first came within 2 in of the point. This session could not open the Drive files (the share link needs a sign-in
and the connector only hands a file over as text), so the real rows below are the review tool's figures as
posted on #155 and in its README, not a fresh read; the simulated rows and every simulator run are from this
session. The real card times are the 19:53 R-Quals run; the 19:54 run agrees with it to the figures given.

## R-Quals, card by card

The drawing that ran is master's `r-quals.pp` at d11eb1af: START (55, 9.7, 90°) on the taped audience spot,
PARK (14, 95) (moved off the wall that afternoon, PR #227). The simulator's 15 Oct logs ran this branch's
`r-quals.pp`: START (59, 8.06), PARK (10.5, 95). Every other point, every heading and the speed settings
(`maxVelocity` 50, `maxAcceleration` 45, `maxDeceleration` 45) are the same, so the first path (8.7 in on master,
10.1 in here) and the park path (76.3 in against 77.7 in) differ a little in length and nothing else does.

Times are match seconds. "Sim @50" is the simulator at the drawings' settings: seed 24, the README's typical run
of 15 Oct, reproduced exactly by this session's re-run. "Sim @93/100/60" is the same seed with the first, overstated
profile (below). A path's duration is from its `path` trace line to the next card's; the real robot's is what
Pedro reported, the simulator's is its own end rule (4 in short of the point, then settling in the wait).

| Card | Real (19:53) | Sim @50, seed 24 | Sim @93/100/60 (overstated), seed 24 | What differs, and why |
|---|---|---|---|---|
| `SpinUp` | 0.00 | 0.00 | 0.00 | — |
| wait **Fire the preloads (TIP 1)** | 0.00–3.01, **limit** (3 s) | 0.00–2.64, `Empty` | 0.00–2.64, `Empty` | mechanism: no launcher. The simulator's 2.64 s is its 2 s spin-up and 0.2 s a shot |
| path **START → S_CATCH** | 3.03–3.52, 0.49 s | 2.66–3.22, 0.56 s | 2.66–3.06, 0.40 s | physics and drawing: the real path is 8.7 in (START moved on 24 Oct), the simulated one 10.1 in; real ended within 1.2 in of the point |
| wait **TIP 1** | 3.52–7.53, **limit** (4 s) | 3.22–3.76, `Tip` after 0.54 s | 3.06–3.76, `Tip` after 0.70 s | mechanism: nothing was launched, so no TIP. In the simulator the preloads tip the HIVE at 4.1 s |
| wait **TIP 1 missed: catch** | 7.53–9.04, limit (1.5 s) | not run | not run | the rescue branch: real only, because TIP 1 never came |
| wait **TIP 1 missed: fire the catch** | 9.06–12.07, limit (3 s) | not run | not run | same |
| wait **Catch TIP 1's spill** | 12.11–13.63, limit (1.5 s) | 3.78–5.28, limit | 3.78–5.28, limit | both run to the limit: on the robot `IntakeFull` is a placeholder; in the simulator the catch wait is a wait for the spill, not the intake (`limits.json`) |
| path **S_CATCH → FAR_FLOWER_TURN**, 106.6 in straight | 13.65–15.79, **2.14 s**; ended **4.9 and 5.6 in past** the point (y 123.2 / 123.9, drawn 118.3), settled to 118.1 during the next wait | 5.30–8.16, **2.86 s**; "done" 4 in short, at the point by 8.26 s | 5.30–7.34, **2.04 s** | physics: the drawing's trapezoid takes 3.24 s, the robot 2.14 s (peak 76 to 78 in/s, about 63 to 71 in/s² from rest, 48 to 76 in/s² braking; the first 100 ms-window figures of 93 / 100 / 60 were noise, see the correction). With the measured profile the simulator is within 0.1 s. The overshoot is Pedro's end rule, which the simulator does not have (gap 1) |
| wait **Fire the catch at the left CELL** | 15.79–17.80, **limit** (2 s) | 8.16–8.46, `Empty` after 0.30 s | 7.34–7.64, `Empty` 0.30 s | mechanism: no launcher. The simulator fires its 1 caught piece |
| path **FAR_FLOWER_TURN → FAR_FLOWER**, 8.3 in | 17.82–18.27, 0.45 s | 8.48–8.94, 0.46 s | 7.66–7.98, 0.32 s | agree at @50 (a short path never reaches full speed, so the top speed hardly matters). At the measured acceleration the simulator is 0.13 s quicker than the robot: short paths are where Pedro's own braking and settling cost most (gap 2) |
| wait **The far FLOWER's 4** | 18.27–21.27, **limit** (3 s) | 8.94–10.62, `IntakeFull` after 1.68 s | 7.98–9.64, 1.66 s | mechanism: no extractor, `IntakeFull` a placeholder. `flowerS` 3.0 is the limit the drawing carries |
| path **FAR_FLOWER → N_FIRE**, 15.5 in with a 180° turn drawn piecewise over the last 70% | 21.29–22.46, 1.17 s; heading ended **20° and 15° short** of 270°, done by the end of the next wait | 10.64–11.72, 1.08 s; ends at 273° | 9.66–10.58, 0.92 s | physics: the simulator turns at its design's rate limit and reports the path done only once the heading is within 5° (1° when stopped); Pedro reported it done with the turn unfinished (gap 3) |
| wait **Fire the far FLOWER's 4 (TIP 2)** | 22.46–26.26, **cut by the guard** at 3.7 s left (limit 4 s) | 11.72–13.48, `RightCellUp` after 1.76 s | 10.58–12.36, 1.78 s | mechanism: `RightCellUp` a placeholder, so this wait would have run its 4 s; the guard ended it first because the rescue branch had spent 7.5 s. In the simulator TIP 2 comes at 12.3–13.5 s |
| wait **TIP 2's spill lands** … path **WALL_FLOWER → FIRE3**, wait **TIP 3** | never reached | 13.50–25.68 | 12.38–22.24 | the robot had 3.7 s left when the simulator's run is at its sixth card of twenty-three |
| path **park** | 26.26–30.07, from N_FIRE straight to PARK: within 2 in after **1.86 s**, final pose 0.1–0.3 in off | FIRE3 → PARK 25.70–28.00, 2.30 s | 22.26–24.0, 1.74 s | the real park was the guard's: the park path's start is FIRE3, 93 in away, and Pedro drove to the nearest point of the path instead, which is PARK itself. The simulator would have driven to FIRE3 first (gap 4) |
| end | Auto ends 30.07 | finished 28.0 s (2.0 s spare) | finished 24.0 s (6.0 s spare) | at the measured speed the simulator's R-Quals gains 4 s of slack, all of it from the four long paths |

Real path ends: every path ended within 1.2 in of its drawn point except the 106.6 in straight (above). Both
R-Simple runs started at (55.0, 9.7, 90°), exactly the declared start (read from the logs, 27 Oct).

## R-Simple, card by card

Master's `r-simple.pp` at d11eb1af: START (55, 9.7), one path START → PARK (14, 95) through a control point at
(30, 40), 95.3 in; the fire wait's limit is 4 s on master (3 s in this branch's file). The simulated R-Simple is
robot 2 of the Simple pair (`LSimpleAuto,RSimpleAuto@50`, seed 24, 15 Oct): START (59, 8.06), PARK (10.5, 95),
100.2 in.

| Card | Real (19:44 and 19:49) | Sim @50 | Sim @93/100/60 (overstated) | What differs |
|---|---|---|---|---|
| wait **Fire the preloads at the right CELL (TIP 1)** | **4.02 and 4.01 s: the 4 s limit** on both runs (`Empty` never fired; no launcher). The path started at 4.03 and 4.04 s | 0.00–2.64, `Empty` | 0.00–2.64 | mechanism |
| path **START → PARK**, 95 in curve | within 2 in of PARK **2.47 and 2.53 s** after the path started; parked 0.4 and 0.5 in from PARK (final (13.6, 95.1) and (13.5, 95.1) against (14, 95); heading 90° both) | 2.66–5.40, 2.74 s to a dead stop at the point | 2.66–4.70, 2.04 s | physics: the drawing's trapezoid previews 3.02 s (the Java's park seconds are 3.1). Peak 56 to 57 in/s, about 30 in/s² from rest, 100 to 120 in/s² braking (strafe against a 43 in/s maximum): slower off the line and slower at the peak than the straight, because Pedro slows for the curve. The simulator at the measured profile is now 0.5 s *faster* than the robot here, because it drives curves at full speed (gap 2) |
| end | parked, 0.4–0.5 in off | parked, 0.0 in off | parked | agree |

## What the differences are

**The robot's missing mechanisms** (every wait, both Autos): no launcher, no extractor, no piece counting, no
CELL tracking. Every real wait ran to its limit; the R-Quals rescue branch added 7.5 s (TIP 1 4.0 s, catch 1.5,
fire the catch 3.0, the spill catch 1.5) that the simulator never runs because its preloads tip the HIVE. This is
why the guard fired on the night and never fires in the simulator: at 22.46 s the robot was at its sixth card and
the simulator (@50, seed 24) was firing TIP 3. None of it is a simulator fidelity question; it is Levels 1 and 2 of
[#193](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/193).

**The simulator's physics** (the driving): the drawings say 50 in/s and 45 in/s² and the simulator drives
exactly that, so every long path is previewed about 1.5× slower than this robot drives it (the straight: 3.24 s
drawn, 2.86 s simulated, 2.14 s real). Short paths agree already. What the simulator has no model of at all: Pedro
ending a path early and the robot overshooting then settling (gap 1), Pedro slowing for a curve (gap 2), a heading
turn still unfinished at the path's end (gap 3), and the guard's park from a point far from the park path's start
(gap 4).

**The drawings' settings:** `maxVelocity` 50 / `maxAcceleration` 45 / `maxDeceleration` 45 in every `.pp`. They
set the Visualizer's preview and, through the export, the park guard's seconds (`kit.guarded("Auto", …, 2.7, …)`
for R-Quals' FIRE3 → PARK, 76.3 in; 3.1 for R-Simple's START → PARK), which the robot adds its 1.0 s
`GUARD_MARGIN_S` to: "park needs 3.7 s". They are **not raised here.** On the night the park started with 3.7 s
left, was within 2 in of PARK after 1.86 s and was still settling at the buzzer, 0.1–0.3 in off: the guard's
allowance was right for the night, and raising the drawn speed to the measured one would cut it to about 1.8 s +
1.0 s. How to separate the two uses of the same number is a Visualizer question, below.

## The simulator at the measured speed

The study spec now takes the profile as `@top/accel/brake` in in/s and in/s² (`AutoStudyTest.profile`;
`@50` still means 50 with 45 both ways), and `AutoSim.SimDrive` brakes at its own rate instead of the
acceleration. 60 runs each, the `rigid V` design, R-Quals with `PartnerLeftVAuto`, the Simple pair together:

| Spec | TIP 1 | TIP 2 | TIP 3 | Points | PARK | G409 |
|---|---|---|---|---|---|---|
| R-Quals @50 (the drawings; the README's row) | 60/60 at 5.8 s | 56/60 at 12.2 s | 54/60 at 26.3 s | 72.7 | 120/120 | 1 run |
| R-Quals @77/100/60 (Foresight's max forward, the overstated acceleration) | 60 at 5.7 | 56 at 11.2 | 55 at 23.3 | 73.0 | 120/120 | 1 run |
| R-Quals @93/100/60 (the overstated first profile) | 60 at 5.7 | 56 at 11.1 | 53 at 23.1 | 72.3 | 120/120 | 3 runs |
| R-Quals @77/65/55 (corrected: peak, acceleration and braking, 300 ms windows) | 60/60 at 5.7 s | 56/60 at 11.5 s | 51/60 at 24.5 s | 71.7 | 120/120 | 1 run |
| Simple pair @50 | 56/60 at 4.4 | 55/60 at 11.8 | — | 53.0 | 120/120 | 0 |
| Simple pair @93/100/60 (overstated) | 56 at 4.4 | 55 at 11.4 | — | 53.0 | 120/120 | 0 |
| Simple pair @57/30/110 (corrected curve) | 56/60 at 4.4 s | 55/60 at 11.9 s | — | 53.0 | 120/120 | 0 |

At the corrected profile R-Quals' TIP 3 moves from 26.3 s to 24.5 s and TIP 2 from 12.2 to 11.5 s, the score is 71.7 against 72.7 (TIP 3 in 51 runs against 54: within the ±4 the README reads these rows with), and every TIP still waits on a mechanism, not the drive: every TIP waits on a mechanism (the intake filling, the HIVE's dwell, the volley), not on the
drive, and the faster robot reaches TIP 2's spill a little earlier, where it touches a falling piece in 3 runs
instead of 1 (G409, `doc/g409-routes.md`: the catch spots were placed for the 50 in/s robot and want re-checking
at the real speed). The 1 "problem" in every R-Quals row is the partner's drawing (`partner-left-v` starts off
the wall), not R-Quals.

**The Visualizer's breakpoints** ([#155](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/155)
lists them for recycle5 and recycle3, both retired). Its typical-timing preview scales every path by the one
`driveSpeed` fact (`maxVelocity` × s, the accelerations × s²; `src/lib/timing/together.ts`), so the measured
1.5 is what the file now carries. R-Quals on master's drawing, the other facts as in `timing.json`, run through
the Visualizer's own `breakpoints()` (`src/lib/timing/together.ts`, this session, node):

| Fact | Now | At driveSpeed 1: loses TIP 3 if… | At driveSpeed 1.5 |
|---|---|---|---|
| `spinUpS` | 0.1 | above 0.91 s | no breakpoint in range |
| `volleyS` | 0.8 | above 1.0 s | above 2.99 s |
| `tipStartS` | 0.6 | above 1.14 s | none |
| `tipSwingS` | 0.7 | above 2.64 s | none |
| `intakeFullS` | 1.5 | above 1.9 s | none |
| `driveSpeed` | 1 → 1.5 | below 0.95 | below 0.95 |
| `tipLoad` / `startLoad` / `volleyLoad` | 8 / 5 / 4 | one POLLEN either way | the same |

At the drawn speed R-Quals' third TIP sat on five knife edges (a volley 0.2 s longer, the intake 0.4 s slower, a
spin-up 0.8 s longer each cost it). At the measured speed the preview finishes R-Quals at 22.4 s instead of
28.1 s and the only timing fact that can still cost TIP 3 is a volley over 3 s; the load facts (how many POLLEN a
volley lands and what tips a CELL) stay as sharp as ever. R-Simple has no breakpoints: one volley, one path.
(The preview's own path times are the same trapezoid `review.py` prints as ≈prev, plus a turn on the spot where
a path starts with the heading off; it does not slow for curves either.)

## Simulator fidelity gaps the logs expose

Each with the evidence and a proposed fix. The real logs are the truth; where they disagree with the simulator,
the simulator changes.

1. **Pedro reports a path done before the robot has stopped, and the robot overshoots.** Evidence: the 106.6 in
   straight ended 4.9 and 5.6 in past its point on two runs, "done" at 2.14 s, settled to 118.1 (0.2 in off)
   during the 2 s wait after it. The simulator's `SimDrive` does the opposite: it reports done 4 in *short*
   (`JOIN_IN`) and then creeps to the point, and never overshoots. Pedro 3.0.1's Foresight carries end
   constraints `parametricTConstraint` 0.025, `translationalConstraint` 0.1 in, `velocityConstraint` 0.1 in/s,
   `headingConstraint` 0.007 rad and `timeoutConstraint` 100 ms (the jar's
   defaults, `ForesightConfig`; `pedro/Constants.java` sets none of them), so "done" 5 in past the point and still
   moving means a condition other than "stopped at the point" ended it; the 100 ms timeout is the one to check.
   Fix: model the end as Pedro does it, a path done when the robot passes the point's parametric end at speed,
   then a braking-and-settling tail at `naturalForwardDeceleration` that the next card sees as motion (a fire
   step that needs the robot still would wait for it, as `FIRE_SPEED_IN_PER_S` already does). Until the real
   rule is confirmed, a measured overshoot of about 5 in at 77 in/s is the number to reproduce.
2. **One acceleration for everything, and no slowing for curves.** Evidence: the straight about 63 to 71 in/s² up and 48 to 76
   down at a 77 in/s peak; the curve about 30 up, 100 to 120 down, peak 56 to 57, partly strafe; at the measured profile the simulator drives R-Simple's
   curve 0.5 s faster than the robot. This session gave `SimDrive` a separate braking rate (and the study spec a
   third number); the curve is still driven at the straight's speed. Fix: a curvature-dependent speed cap in
   `SimDrive` (Pedro's own limit is the centripetal one, `maxVelocityConstraint` and the brake coefficients),
   fitted to R-Simple's curve once its speed trace is read at 20 ms (the log is about 43 Hz, so any short window is noisy).
3. **A heading turn drawn over the end of a path is not finished when Pedro says the path is.** Evidence:
   FAR_FLOWER → N_FIRE ended 20° and 15° short of 270° on the two runs, done by the end of the next wait. The
   simulator ends a path only once the heading is within 5° (1° if stopped), so it never shows this. Fix: in
   `SimDrive`, let "done" ignore the heading the way Pedro does and keep turning at the rate limit during the
   wait; a fire step then waits for the aim as it already does for the turret. The drawing's piecewise turn
   over the last 70% is also the kind of thing the Visualizer could start earlier (`review.py`'s reading).
4. **The endgame guard's park from far away.** Evidence: the guard cut the TIP 2 fire at N_FIRE, 93 in from the
   park path's start at FIRE3, and the robot was within 2 in of PARK 1.86 s later: Pedro drives toward its closest
   point on the path, and from N_FIRE that is PARK itself (the 1.86 s says so; `/Path/Full` in the log shows the
   planned path, `/Odometry` the driven one). `SimDrive.follow` instead drives a straight lead-in to the path's
   *start* and then the whole path, which from N_FIRE would be 93 in plus 76 in through the HIVE's west side;
   the README's "the park path drags it through the FLOWER" run is this. Fix: start the lead-in at the nearest
   point of the path (Pedro's rule), and let the guard's own seconds be the real cost of that drive rather than
   the park path's.
5. **The guard's allowance and the preview's speed are one number.** Evidence: the night's cut with 3.7 s left
   was right (within 2 in at 1.86 s, settling at the buzzer); with the measured speed drawn into the `.pp` it
   would have been 2.8 s. The Visualizer's preview also reserves only `path.seconds` (no `GUARD_MARGIN_S`, no
   rounding), so it shows "no guard" where the robot cuts. Fix: on the Visualizer side (below); on the simulator
   side nothing, it runs the real `AutoKit`.
6. **The simulator never runs the rescue branch.** Evidence: the real R-Quals spent 7.5 s in "TIP 1 missed";
   the simulator's preloads always tip. A run with the launcher off (every fire wait to its limit) would have
   predicted the night's timeline card for card; there is no switch for it. Fix: a `BIOBUZZ_AUTO_NO_LAUNCHER`
   (or a `RobotDesign` with no launcher) so a light-chassis session can be simulated as it is.
7. **The real logs cannot be read by the simulator's tools yet.** `review.py` reads a simulated log too (same
   keys; robot 2 needs `auto2:`/`PartnerPedroInches` mapped, done in this session's wrapper) but lives on another
   branch and the two robots' logs are compared by hand. Fix: bring `tools/log-review/` to this branch and give
   it a `--against <simulated.wpilog>` that prints the two timelines side by side.

## What only the robot can answer next session

Log, in the match log, at 20 ms (the pose log is about 43 Hz and short windows were noise):

- Pedro's own end decision: `follower.atParametricEnd()`, `distanceToEndpoint()` and the follower's mode each
  loop, so the end rule (gap 1) is read, not inferred. Also the five `ForesightConfig` constraint values the
  robot is actually running (`Constants.createAlgorithm`), once, in `/RealMetadata`.
- The drive's commanded powers and the Pinpoint velocity (`/Drive/*`), so acceleration, braking and the curve's
  speed cap (gap 2) come from the robot, not from differencing positions.
- The heading error at every `path` end and at the start of the next card (gap 3).

Time, with the launcher and extractor on (the four facts #155 asks for, none measured yet):

- `volleyS`: LaunchAll to empty, filmed; `spinUpS`: PLAY to at-speed from the launcher's telemetry.
- `tipStartS` and `tipSwingS`: film a TIP from the volley to the far CELL settled, with the HIVE state log.
- `intakeFullS`: a FLOWER's 4 with the extractor seated, 10 tries, and a spill catch (`limits.json` too).

Drive, with the full mass on: the same two paths (R-Quals' straight, R-Simple's curve) and R-Simple whole, so
`driveSpeed` is re-measured and the guard's seconds re-checked (`doc/next-meeting-checklist.md`).

Film: the 106 in straight's end from the side (how far past the point, how long to settle), one park cut by the
guard, and the FAR_FLOWER → N_FIRE turn.

## Visualizer questions

1. **Separate the guard's seconds from the preview speed.** Today one `.pp` setting (`maxVelocity`,
   `maxAcceleration`, `maxDeceleration`) sets both the preview and `parkSeconds` in the export, so the only way
   to preview at the measured speed without thinning the guard is the `driveSpeed` fact, which the export
   ignores. Options: a per-Auto guard margin (seconds, or a factor on the park path's time) in the `auto`
   section, written into `kit.guarded(...)` and shown in the preview; or the export keeping the drawing's
   settings for the guard while the preview uses `driveSpeed` (what happens now, by accident, but the preview
   then shows no guard where the robot has one).
2. **Preview the guard as the robot runs it:** reserve `parkSeconds + GUARD_MARGIN_S` (the rounding up to 0.1 s
   and the robot's 1.0 s), not `path.seconds`, so "cut short" appears in the preview where it will on the robot.
3. **The guard's park from where the robot is**, not from the park path's start, in the preview's time and in
   the simulator (gap 4).

## What changed on this branch for this

- `TeamCode/autos/timing.json`: `driveSpeed` 1.5, `"source": "measured, light chassis 25 Oct 2026"`, with how
  it was measured in its note. The other eleven facts are untouched: nothing else was measured on the night.
- `AutoSim.SimDrive`: a separate braking rate (`decel`; `AutoSim.speed(v, a, d)`), the trapezoid generalised to
  it; `AutoStudyTest`: a study spec's `@v/a/d`. `@50` runs exactly as before (the 15 Oct rows reproduce: R-Quals
  72.7, 54 of 60; seed 24's timeline identical).
- `doc/what-the-simulator-knows.md`: the driving row.
- Not changed: the `.pp` files' speed settings (the guard), the generated Java, the README's rows (they stay the
  @50 rows until the Autos are re-simulated at a speed measured on the full robot).

**Read from the logs on 27 Oct 2026** (`review.py` run on the mentor's machine against master's `r-simple.pp`, 19:44 and 19:49 R-Simple logs): the start pose matched the declared one, the fire wait ran to its 4 s limit (4.02 and 4.01 s), the park took 2.47 and 2.53 s to within 2 in and ended 0.4 and 0.5 in off, and the Autonomous period ran 30.1 s. The R-Quals figures above are still the review tool's as posted on #155 (not re-read here).
