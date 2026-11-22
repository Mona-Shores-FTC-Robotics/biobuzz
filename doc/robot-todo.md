# Robot to-do: everything waiting on the real robot

**This is the only list of what waits on the robot.** A finished item comes off this list (delete
the line in a PR) and its result goes to the issue or PR in its source link, not here. A PR that
needs a robot check adds a line here, in the group with the same setup, instead of writing a new
session doc. Long procedures stay in their session doc or PR; this list links to them.

Built 3 Nov 2026 for the mentor's lab visit, from `doc/robot-session-1.md` to `-5.md`, the open
`needs:robot` issues, and the robot checks in PRs merged since 29 Oct. **No session doc checkbox
was ever ticked**, so nothing here is assumed done. Where the repo has evidence that an item already
ran, the line says *likely done* and why: strike it if so.

Trimmed 4 Nov against the earlier 20245 logs. The 5 Nov review subsequently found
R-Solo running build `733501e`, with the fourth-shot cutoff active and the park settling
at about 28.5 s. Earlier claims that nothing after PR #422 had run are superseded;
this is not a claim that every unchecked item below has passed.

## The new launch speed table (18 Nov, measured on 20245)

`LaunchSpeedTable` now holds 12 rows measured on 20245 on 18 Nov (35 to 74 in), and both robots'
`robotOffsetTicksPerSecond` lines are gone from `RobotDifferences`.

- [ ] **20245: one volley at three ranges** (about 40, 51 and 63 in), turret LOCKED, Circle, speed
      table ON, no trim. **Good:** 4 of 4 at each. **Stop:** a range misses the same way twice →
      note the range and which way, and its speed then.
- [ ] **Right-start Autos fire their preloads from `RIGHT_AUTO_HIVE`** (19 Nov): R-Solo,
      R-Clockwise, R-LeftUnderHive and R-NoCross now drive `RIGHT_START` → `RIGHT_AUTO_HIVE` (about
      13 in, 1.1 s) as they spin up, and fire there, since the preloads from `RIGHT_START` went long.
      Run R-Solo on 20245 and push the log. **Good:** the `launcher: speed table` line says about
      40 in and 1495 ticks/s, the gate opens as the drive ends, 4 of 4 go in. **Stop:** short or
      long → note which, and the range and speed the line gave.
- [ ] **Volley history on the Driver Station** (19 Nov): in an Auto the Match page lists each
      volley above the step trace (`1) 2.0 s · 4 shots · 40 in camera · 1495 ticks/s · dip 180`)
      under a `Volleys: …` summary; in Basic Drive the same list is on the Robot page, under
      "Last shot". At STOP every volley goes to the match log as a `volley …` event. **Good:** one
      line per volley, the shot count matches what left the robot. **Stop:** one volley split in
      two (shots more than 1.5 s apart: `VolleyHistory.VOLLEY_GAP_S`) or two run together → note it.
- [ ] **Speed table during a moving feed** (`TeleOpConfig.speedTableDuringFeed`, 20 Nov, off,
      not driven). Off, the launch speed is held from gate open to feed end: on 19 Nov 19429 held
      1507 ticks/s from 48 in while the driver drove to 52–55 in (table: 1530–1550). Basic Drive,
      loaded, film the shots: hold Circle and drive slowly away from the CELL from about 45 to 60 in,
      five volleys with it off, then five with it on in Panels. **Good:** with it on the log has
      `speed table … during the feed` lines, the far balls land no shorter than the near ones, and
      the shot count matches the video. **Stop:** shots left uncounted with it on (the detector
      reseeds on each change) → leave it off and note the count against the video.
- [ ] **19429, when it is back:** the same three volleys. Its old −25 offset matches the new rows
      within about 5 ticks/s from 42 to 55 in, so it starts at 0. **Stop:** long or short at every
      range by the same amount → that amount is its new `RobotDifferences` offset line.

## Tonight (18 Nov): 19429, in this order

Each step stands alone: what to run, what good looks like, when to stop. Push the logs
(`tools/pull-logs`) after each step, so a session can read them. Nothing below has run on a robot.

1. **Feed jam.** On 17 Nov from about 18:59 to 19:25, 19429 fed for 3.5 s with the gate open, the
   feeder at full power and the launcher at speed, and fired **0** balls, three times (R-OutsideLane
   twice, Basic Drive once). Load 4 POLLEN, run **Bench: Launch Test**, fire 3 volleys, and watch
   the gate, the feeder wheel and the prefeed. **Good:** every volley sends all 4 out (the shot
   counter may say fewer: it misses shots, being fixed). **Stop:** a volley fires 0 or 1 → find
   what binds before any Auto runs; nothing after this step means anything until the feed works.
- [ ] Feed jam checked
2. **Pinpoint cable.** 19429 logged dozens of `Pinpoint: FAULT_BAD_READ` events on the 17 Nov
   evening. Reseat the Pinpoint's cable at both ends, run **Check: Hardware**, then Basic Drive for
   2 minutes of normal driving. **Good:** no `Pinpoint: FAULT_BAD_READ` in the log. **Stop:**
   faults again → swap the cable.
- [ ] Pinpoint cable checked
3. **Lane sensor with the black bottom plate.** Levels are `leaveBelow` 0.09 / `presentAbove`
   0.11 (`RobotDifferences`). With the plate the empty lane read 0.052 (17 Nov, one run). In Basic
   Drive: lane empty, intake on, 1 minute; then 4 POLLEN in one at a time; leave one parked at the
   sensor. Then INIT R-Solo with the 4 preloads loaded. **Good:** no "Lane sensor reads … empty"
   INIT warning; the lane count goes 1, 2, 3, 4; FULL only with 4 in. **Stop:** the warning shows,
   or FULL with fewer than 4 → push the logs; a session sets both levels from them.
- [ ] Lane sensor checked with the plate
4. **Camera mount: set from the Belding and Fruitport logs (22 Nov 2026), check only.** Both
   robots' forward/left came from `mountfit.py` (the localization-triage skill), which splits the
   camera from the field: 20245's are the shared `CameraMount` values, 19429's are its
   `RobotDifferences` line. Nothing to measure: Sloth Load, run any Auto and TeleOp as usual, pull
   the logs, and a session runs `mountfit.py` on them. **Good:** each robot's camera error within
   about ±0.3 in, and the start check's camera-minus-start about 1 in to the robot's left or less.
   **Stop:** a robot's error past ±0.5 in, or larger than before → push the logs, change nothing.
   The launch speed comes from the camera's range, so a mount change moves each speed by a few
   ticks/s (20245's forward moved 0.81 in): watch the first volleys at the Auto spots.
- [ ] Camera mounts checked in the next logs (Sloth Load first)
5. **Shooting at the Auto spots,** after the mount is in. The launch speed comes from the camera's
   range to the CELL, so a mount change moves every speed. HIVE upright with the CELL being shot
   **up**, 4 POLLEN each time, phone filming the CELL. Fire 5 volleys from each spot by running
   the Auto that fires there, stopping it after that volley:
   - `RIGHT_START`: R-Solo's preloads
   - `RIGHT_AUTO_HIVE`: R-Solo's second volley
   - `LEFT_AUTO_HIVE`: L-NoCross's preloads
   - `LEFT_AUTO_OFFSET`: R-LeftUnderHive's LEFT volley (set the LEFT CELL up by hand)

   Count balls in per volley from the video. **Good:** 4 of 4 in most volleys at every spot.
   **Stop:** one spot long or short every time → note it, with the robot; a session turns it into
   an offset for that robot (`RobotDifferences`) or a fix to one row of `LaunchSpeedTable`. Do
   not change speeds at the robot.
- [ ] Spot shooting: hits per volley written down for each spot
6. **Turret firing arc, both robots — optional.** The arc is ±90° on both robots
   (`TurretSubsystem.arcLeftDeg` / `arcRightDeg`), the mentor's judgement on 18 Nov 2026, not a
   hand measurement. Measure it only to check or widen that. Robot
   on its home mark, **Bench: Turret** in INIT (it reads the angle from the mark; nothing moves until
   PLAY), turret limp and turned by hand, a phone filming from above. Turn it left until a ball
   leaving the launcher would touch the robot (frame, intake, light, cables, the launcher's own
   side), back off 5°, and read the angle. The same to the right. Write down left and right for
   each robot. **Good:** both read 90° or more (90° stays). **Stop:** either side under 90° → tell
   the mentor; that robot gets its line in `RobotDifferences`.
- [ ] (Optional) Firing arc measured: 19429 left __° right __°, 20245 left __° right __°
7. **Circle follows the turret mode, both robots** (code in the Circle PR, not on the robot
   tonight). Basic Drive, HIVE upright with our CELL **up**, 4 POLLEN each time, phone filming the
   CELL. Driver's R1 to **LOCKED** (two rumble blips, Match page reads LOCKED): from three spots (the right launch spot, mid-field facing
   about 60° off the CELL, and near the far wall with the CELL behind-left), the driver holds Circle
   and does nothing else. **Good:** each time the robot turns onto the CELL by itself, the launcher
   spins up while it turns, and all 4 go once it is on (the match log's `fire (driver's Circle, aim
   then fire)` line, then the shot lines). Letting go mid-turn stops the turn and fires nothing; the
   right stick moved mid-turn hands the turn back and fires nothing. Then R1 to **AIM** (one blip): Circle
   fires as before (the turret points). The operator's Circle and Triangle do nothing in either mode,
   and the Controls page lists no operator fire or turret mode. In INIT, Triangle held 1 s on either
   pad still discards a handoff (start fresh). **Stop:** it fires before it is on the CELL, or keeps turning
   after Circle is let go → push the log.
- [ ] Circle LOCKED: turned on and fired 4 from all three spots; AIM unchanged
8. **Operator fine trim.** Basic Drive: the operator taps R1 (launch trim +25) and D-pad left (aim trim
   L 1.5°), then holds Circle and taps them again: +10 and 0.5° per press (Match page: LAUNCH TRIM +35,
   AIM TRIM L 2.0°), one/two rumble blips as before, and Circle fires nothing. **Stop:** Circle fires,
   or the step does not change → push the log.
- [ ] Fine trim: smaller steps with Circle held, no fire
9. **Backward with a load (bottom plate).** R-Solo's WALL FLOWER leg (`BACKWARD to LEFT_AUTO_HIVE`) drives backward
   since 20 Nov 2026, promoted from R-Solo-Backwards after it ran clean on 20245 with its plate (mentor). The promoted R-Solo
   is master's, with its move up to `RIGHT_AUTO_HIVE` for the preloads, plus that one leg: the combination has not run yet.
   R-Clockwise's two outside lanes drive backward too since 20 Nov 2026 (promoted from R-Clockwise-Backwards), each after a
   short wait on the match clock so L has left `LEFT_AUTO_HIVE`. Basic Drive,
   4 POLLEN in the lane, phone filming the intake low from the side. Drive **backward** at full stick, with a hard start and
   a hard stop, 3 times each:
   - from `WALL_FLOWER_STAGING` north to `LEFT_AUTO_HIVE` (R-Solo's backward leg)
   - from `GARDEN_STAGING` north up the outside lane to `LEFT_AUTO_HIVE` (R-Clockwise, Pickup GARDEN)
   - from `WALL_FLOWER_STAGING` north up the outside lane to `LEFT_AUTO_HIVE` (R-Clockwise, Pickup WALL_FLOWER)

   **Good:** the lane still holds 4 after every run, and the video shows none leave; then run **R-Solo** alone from
   `RIGHT_START`, `LEFT_START` empty, and **R-Clockwise** (each Pickup) with L-Clockwise on the other
   robot, and push the logs. **Stop:** any piece leaves → that robot does not run R-Solo or R-Clockwise until its
   plate is fixed; push the logs and the video.
- [x] Backward with a load, 19429 and 20245: all 4 POLLEN stay in, no issue (mentor, 20 Nov 2026)
- [x] Backward with a load, 20245: R-Solo-Backwards ran clean with the plate (mentor, 20 Nov 2026)
- [ ] Promoted R-Clockwise (backward outside lanes, waits at 9.8 / 9.9 s), each Pickup, beside L-Clockwise: no contact at `LEFT_AUTO_HIVE`, push the logs
- [ ] R-Clockwise on Priority THREE_TIP (20 Nov 2026): its new backward leg under the HIVE, `BACKWARD under the HIVE from
  the scoring FLOWER to RIGHT_AUTO_FIX_TIP`, with its SCORING FLOWER load. **Good:** it clears the HIVE's frame, the
  lane holds its load, it fires at RIGHT and parks by 30 s. **Stop:** it touches the frame or a piece leaves → run
  Priority PARK until the leg is redrawn; push the logs and a video
- [ ] L-Clockwise's park, `LEFT_AIMED_PARK` (13.0, 119.0, 345°), set from AdvantageScope only (20 Nov 2026): put L on it
  by hand. **Good:** a referee would call it parked in the LOADING ZONE without a second look, and with LEFT up
  **Vision: HIVE & Pose** reads LEFT up from there. **Stop:** either is in doubt → move the spot in the Visualizer and
  re-export; push a photo
- [ ] Promoted R-Solo (preloads from `RIGHT_AUTO_HIVE`, backward WALL FLOWER leg), alone from `RIGHT_START`, on 20245: push the logs
- [x] Promoted R-Solo on 19429: ran clean (log `2026-11-19_23-13-43_R-Solo`, build `08b0290`): the backward leg 3.2 s, 1.4 in off
  its end, and the volley after it fired 5 balls by the launcher's dips
- [ ] R-LeftUnderHive with L-LeftUnderHive (20 Nov 2026: `wall lane north BACKWARD to LEFT_AUTO_OFFSET` and `BACKWARD to
  RIGHT_AUTO_CLOSE`, after a wait at the SCORING FLOWER until 18.6 s): balls fired after each backward leg (launcher
  dips), no contact near `LEFT_AUTO_OFFSET` or on the way down to `RIGHT_AUTO_CLOSE`; push the logs
10. **19429's turret hunts: tune its hold** (20 Nov 2026). 19429 runs 20245's turret values and never had
   Bench: Turret. Its Basic Drive logs (`read_logs.py hold`) show it reversing 1.18 (18 Nov) and 1.38
   (19 Nov) times a second holding with the robot still, against 20245's 0.48. At power 1.0 the two
   match (154 and 146 deg/s), and neither drifts at power 0, so the stop power is fine. Panels open,
   pushing the logs after each:
   1. ~~By hand~~: done, it turns freely (mentor, 20 Nov 2026). The floor is not it either: from rest
      both robots start at power 0.145 within about 33 ms (18 and 19 Nov logs). It is the coast:
      once its power cuts, 19429's turret runs on 3.3° (20245: 3.4°), more than the ±2° band, so it
      crosses the band and is kicked back.
   2. **Floor: done.** Bench: Turret, 19429 turns steadily at +0.06 (10 to 12 deg/s) and -0.065
      (10 to 13 deg/s), smoother than 20245 (mentor, 20 Nov 2026): `minPower` 0.065 for 19429 in
      `RobotDifferences`; the 0.005 between the two ways is too small to move the stop power. Run Basic Drive with it
      first and push the log; only if it still hunts go on to 3.
   3. **kP: 0.008** for 19429 (mentor, 20 Nov 2026, in Panels), in `RobotDifferences`. Push a Basic
      Drive log with it: still holding, driving past the CELL, and a big swing (CELL from one side to the
      other) to check it still gets there quickly. Only if it hunts, go on to 4.
   4. **Basic Drive, AIM, robot still near the CELL, then driving past it:** in Panels set
      `TurretSubsystem.kD` 0.0015 (it cuts the power about 3° early at the 40 to 60 deg/s the turret
      comes in at, onto a still target only). Still swings past → 0.002; stops short and creeps in by
      small hops → 0.001. Leave `kP` and `holdToleranceDeg`.
   **Good:** `read_logs.py hold` on the new log reads under 0.6 reversals a second, and the shots
   still go in → the values that did it get one `RobotDifferences` line for 19429 with the date.
   **Stop:** nothing helps and the turret felt free by hand → push the logs and say so.
- [ ] 19429 turret: floor 0.065 (done), kP 0.008 (done), kD ___, reversals/s before 1.18 (18 Nov) / 1.38 (19 Nov),
      after 0.75 to 1.29 at kP 0.008 (00:18 logs, only 59 s still: too little to judge; kP 0.005 read 0.38 to 0.41
      but sat 3° off). Next: a longer still hold, then `kD` 0.0015 if it still hunts.
11. **Circle turns the CELL into the firing arc, and Circle in LOCKED** (20 Nov 2026, not yet on a
    robot). Basic Drive.
   - Turret **AIM**, `TeleOpConfig.circleTurnsIntoArc` on (the default): drive so the CELL is behind
     the robot, loaded, and press Circle. **Good:** the robot turns the short way until the CELL is about
     10° inside the arc's edge (`arcTurnMarginDeg`), the turret swings onto it, and the volley goes, on
     both robots; the log has `arc turn: … turning` then `arc turn: aim inside the firing arc`. As
     the turn starts the light gives one solid orange pulse of about 0.4 s
     (`IndicatorSubsystem.ARC_TURN_PULSE_MS`), over the green if the lane is full, then goes back to
     green or dark; no rumble. The light stays dark while driving with the CELL behind. Pushing the
     right stick during the turn hands it back (`arc turn cancelled: right stick`); then the light
     flashes orange while Circle is held behind the arc (19429 does not shoot; 20245 shoots). Ten
     presses from all round the CELL. **Stop:** it turns the long way, swings past and back, or the
     drivers find it fights them → switch `circleTurnsIntoArc` off in Panels (the old steady light
     and refusal) and note which press. The orange is goBILDA's chart value
     (`IndicatorSubsystem.ORANGE` 0.333): if it reads red or yellow, set it in Panels and put the
     number in a PR.
   - Turret **LOCKED**, the CELL 10 to 40° off: hold Circle. **Good:** the robot turns onto the CELL and
     stops on it, and the volley goes. The chassis aim now never asks for less than
     `TeleOpConfig.chassisAimMinTurn` (0.2, a starting point: 0.14 did not turn 19429, 0.28 did): if it
     stops short, raise it in Panels; if it swings past the CELL and back, lower it. Put the value that
     worked in a PR.
- [x] Orange light on 19429 (19 Nov 2026, 00:18 logs): orange whenever the CELL went behind, flashing on both
      refused presses, off when it came back. Still to confirm by eye that 0.333 reads orange, not red or yellow.
- [x] LOCKED Circle on 19429 with `chassisAimMinTurn` 0.2: all 6 presses turned onto the CELL and stopped
      0.4 to 1.3° off it (19 Nov 2026, 00:18 logs). Leave 0.2 unless it swings past.
- [ ] Orange light on 20245; LOCKED Circle on 20245

## Belding work order (agreed 5 Nov)

The old night-by-night plan is superseded by this order. R-Solo remains the working
reference. Keep both turrets locked; do not spend this session tuning benched Autos.

1. **L-NoCross** (formerly L-Partner). Qualify it alongside a reliable RIGHT preload
   shooter, whether that partner stays clear or parks. When TIP 1 does not come, the INIT
   row `if no TIP 1` picks CROSS AND FIX IT (as drawn: cross to RIGHT and fix it) or WAIT FOR PARTNER FIX
   (stay on `LEFT_AUTO_HIVE` until 23 s, for a partner that fixes its own miss, such as
   R-NoCross). Test CELL-up recognition, the two LEFT volleys, the scoring FLOWER
   pickup, and park, including a partner that never makes TIP 1, once on each `if no TIP 1` choice.
   **Partner wait:** drawn to give up at 9 s (was 8.5), and the INIT line reads "waiting for
   TIP 1 until 9 s"; D-pad left and right move that second. In the run with a partner that
   never makes TIP 1, check the no-TIP route still parks in time, starting half a second later.
2. **R-NoCross** (formerly R-Partner-Tip). Qualify the right-side actions, then run it
   together with L-NoCross as the coordinated two-TIP baseline. Its CELL-up wait runs
   until 20 s on the match clock ("waiting for TIP 2 until 20 s" in INIT); then its
   `if no TIP 2` row picks WAIT FOR PARTNER (as drawn: stay, watch for TIP 2 until 23 s,
   fire if it comes, park) or CROSS AND FIX IT (wall side to `LEFT_AUTO_OFFSET`, fire at
   the LEFT CELL if it is still up, park on `RIGHT_PARK`). Check CROSS AND FIX IT's crossing
   clears the HIVE's feet and a partner parked on `LEFT_PARK`, and that both park by 30 s.
3. **R-Clockwise / L-Clockwise.** The coordinated pair (§3). The Infraviolet pair was retired
   on 16 Nov 2026.

- [ ] **After loading the lineup cleanup:** the Driver Station's Autonomous menu has
      exactly L-NoCross, R-NoCross, R-Solo, L-Clockwise and R-Clockwise (with their
      save timestamps); neither Infraviolet Auto nor a Drive: L/R-Infraviolet Utility is listed. Preload,
      Staging, the old Partner names and R-Solo-Nudge are absent. Check INIT on each,
      including alliance, measured start and the available D-pad options. A stale name
      after Sloth Load needs a clean restart/reload check before any field test.
- [ ] **R-Solo reference:** repeat the current three-volley run; record the TIPs and
      park on video and retain its build/settings with the log.

### Retained R-Solo setup and measurement notes from master

The numbered items below are the previous R-Solo reference procedure, retained in
full with the mentor's latest measurements. They do not replace the Belding work
order above or reactivate benched Autos. Names in these historical notes refer to
the pre-cleanup lineup.

1. Full TeamCode install, then Check: Hardware and the Pinpoint cable (§0, §1).
2. ~~Launcher PIDF (§7c)~~ done on 20245, 4 Nov: F 14.65, P 40, I 0.2, D 0, now the defaults in
   `LauncherSubsystem`. Load master before item 3 so the robot runs them without Panels. Still
   open in §7c: the shot count against video, feed pace, the tired battery.
3. ~~Launch speeds~~ measured on 20245, 4 Nov, now `LaunchSpeedTable`'s rows with the table on
   (§7d; ranges from the turret axis since 5 Nov, so 3.4 in more than first written): 42.4 in → 1520
   (RIGHT_LAUNCH_CATCH), 45.7 → 1560 (RIGHT_LAUNCH_OFFSET), 53.9 → 1570 (RIGHT_START; 1600 since 5 Nov: at 1580 R-Solo's first volley put 3 of 4 in on video, the
   fourth low off the front lip), 58.8 → 1635 (near the GARDEN). 1600 confirmed 5 Nov: R-Solo's
   start volley scored 4 (mentor). Left after the load: Square from
   LEFT_LAUNCH_OFFSET, where TIP 2 in every R Auto but R-Partner-Tip is fired (at 1595 it went
   long), and from each right spot once. **Good:** each scores 3 or 4 of 4 at the table's speed
   (the Match page's "Last launch spot" line shows it). **Stop:** a spot misses → note its range,
   the speed and long or short; the table switches off in Panels (`LaunchSpeedTable.enabled`).
4. R-Solo five times or more, the rebuilt one (#439, on master): TIP 1 from the start, the wall FLOWER, then a CellUp check at LEFT_LAUNCH_CATCH (since 5 Nov; it was LEFT_LAUNCH_OFFSET) (at
   most 0.75 s). Up: LEFT volley, scoring FLOWER, LEFT volley (TIP 2), park RIGHT_PARK. Down:
   RIGHT_LAUNCH_OFFSET, fire, park. Count TIP 1, TIP 2 and the park on each run, and note when
   TIP 2's last ball leaves: the preview says about 27 s, and the check adds 0.6–1 s it does not
   show, so the park may be skipped for TIP 2. Run half with `LaunchCards.launchOnApproach` on and
   half off, and judge it only on the volley after the scoring FLOWER: the checked volley stops and
   settles either way. Not clearly as good on → set it false for the event. Same runs:
   `FlowerPicker.stick.lowerWithinIn` 24 (§3's WALL FLOWER stick line, a number, not code). Every pick card
   is now limited to 4.5 s (was 8; logged picks took 1.8–3.7 s): note any pick that hits the limit.
   **CellUp now needs the camera** (5 Nov): an assumed HIVE position no longer passes it, and a
   mid-tip sighting counts only with the robot under 10 in/s and 30°/s (`HiveSubsystem.Tuning`).
   On video, per run: was the LEFT CELL really up at the check, and which way did the Auto go? The
   match log's `CellUp after` line (or the wait running its 0.75 s) says which. **Good:** up → it
   fires LEFT, down → the other way, every run. **Stop:** up but it went the other way → the
   camera did not see the CELL settle in 0.75 s: note the time from arrival to the HIVE line in the
   log; the wait may need longer. A TIP missed while driving shows as no `HIVE (RED)` TRANSITION
   line around it: note it with the speed.

**Deferred for Belding:** dedicated LEFT-preload partner support, staged-preload
collection, active turrets, and sophisticated runtime shoot-versus-park decisions.
R-Partner-Preload and R-Solo-Staging are preserved on `autos-experiment`; their
old checks are not gates for this lineup. See `TeamCode/autos/BENCH.md`.

Each line: what to do · **Good:** what passing looks like · **Stop:** when to stop and what to
record · source. `S4 §2` means `doc/robot-session-4.md`, "At the robot", step 2.

20245 throughout unless a line says 19429. The selectable Autos are R-Solo, R-NoCross and L-NoCross.
Benched Autos are not deployment choices.
Starts: **Red audience** (`RIGHT_START`, tile C1) for the R- Autos, **Red far wall** (`LEFT_START`,
tile C6) for L-NoCross. Place by the marks, never by the camera.

## 0. Before leaving the laptop

- [ ] Master pulled. A **full TeamCode install** if this robot has had none since 2 Nov (the
      indicator light added `res/xml`); Sloth Load after that. [S4 Before], [S5 Before]
- [ ] **20245 rewired 6 Nov:** indicator lights on **Expansion Hub servo port 0**, turret servo on
      **Control Hub servo port 4** (`robot_20245.xml`). A **full TeamCode install** (a Sloth Load
      keeps the old ports), re-Activate `robot_20245`, then **Check: Hardware**.
- [ ] **19429 rewired 6 Nov:** FLOWER stick on **Control Hub servo port 0**, indicator lights on
      **port 1**, turret servo on **port 4** (`robot_19429.xml`; 20245's layout but the lights on
      the Control Hub). A **full TeamCode install**, re-Activate `robot_19429`, then
      **Check: Hardware**, and the light looks and turret-sits-still checks in §2 on this robot too.
- [ ] **19429 rewired 13 Nov: two intake star-wheel servos and the turret moved.** Right star wheel
      on **Control Hub servo port 4** (`intakeStarsRight`), left on **port 5** (`intakeStars`),
      turret servo on **Expansion Hub servo port 5** (`robot_19429.xml`). A **full TeamCode
      install**, re-Activate `robot_19429`, then **Check: Hardware** (it lists `intakeStarsRight`).
      Then Basic Drive, PLAY, intake on: **both** stars turn inward. If the right one turns
      outward, flip `IntakeSubsystem.starsRightReversed` in Panels (the default `false` is a guess
      that it is mounted mirror-image to the left), see it turn inward, and put the value that
      worked in a PR (the shared field, or a 19429 line in `RobotDifferences`). Eject: both back
      out. Turret: still sits at home after PLAY, as before.
- [ ] **Both robots' FLOWER sticks now hold out, powered, instead of going limp**
      (`FlowerStickSubsystem.LIMP_EXTENDED` off, the shared value, 22 Nov). A limp stick is lifted
      by the FLOWER: on 20245 at Fruitport the held stick's Auto picks fired 4 every time (9 of 9),
      the limp one's second pick 0 to 4. **20245** held all of Fruitport's afternoon: nothing to
      check but the servo (below). **19429 has never held**, so before Basic Drive: Bench: FLOWER
      Stick, stick out on the tile, robot still, 10 s: the pivot servo is not buzzing or hot, and the
      roller still turns. If the servo strains, raise `EXTENDED` (0.44) a hair toward `RETRACTED`
      (1.00) until it is quiet with the wheels still on the tile, and put the number in a PR; if no
      position works, add a 19429 line in `RobotDifferences` setting `LIMP_EXTENDED = true`. Then
      R-Solo on 19429: the first (WALL) pick fires 4 (all season limp, 3 of 15 picks did), and
      `/FlowerStick/Powered` stays 1 while `/FlowerStick/Out` is 1. Same servo check on 20245 (out
      at 0.85).
- [ ] **20245's FLOWER stick pivot becomes a SWYFT servo** (the goBILDA Torque servo had a
      mechanical problem, mentor, 20 Nov 2026), same port, Control Hub servo port 0
      (`robot_20245.xml`). The shared `FlowerStickSubsystem.RETRACTED` 0.6 / `EXTENDED` 0.0 were
      measured on the goBILDA, so **before Basic Drive:** Bench: FLOWER Stick, step to the in and
      out stops on the SWYFT servo and put the two numbers in a PR (they are the shared values;
      19429 keeps its own line in `RobotDifferences`). Also check the swing finishes inside
      `SETTLE_MS` (1.5 s),
      and whether the SWYFT's range is the 270 degrees `SERVO_RANGE_DEG` assumes (only the bench's
      and the logs' degrees read it). Then Basic Drive's operator Square swings it between them.
- [ ] **19429 rewired 17 Nov: the new FLOWER stick.** Pivot servo on **Expansion Hub servo
      port 5** (`flowerStick`), turret servo moved to **Expansion Hub servo port 4** (the pivot
      was found in port 5, the turret's, when Bench: Turret moved the stick), roller
      (continuous-rotation servo) on **Control Hub servo port 0** (`flowerRoller`)
      (`robot_19429.xml`). The turret servo was found in the +5V POWER header, not a
      servo port, and unplugged; it was refitted in **port 4** on 18 Nov (the item below). A **full TeamCode install**, re-Activate `robot_19429`,
      then **Check: Hardware** (it lists `flowerRoller`). The stick's positions were
      measured again on 20 Nov in Bench: FLOWER Stick (RETRACTED 1.00, EXTENDED 0.44, a 19429 line in
      `RobotDifferences`); check Basic Drive's operator Square swings it between them. Then Basic Drive, PLAY: stick in,
      roller still; stick down, the roller spins at once (from the request, not once it lands) and
      the DS FLOWER stick line says "roller on"; stick up, it stops. If it turns the wrong way for
      the pick, flip `FlowerStickSubsystem.rollerReversed` in Panels (`true` since 17 Nov,
      seen on 19429) and put the value that worked in a PR. Then a few FLOWER picks: does the spinning
      roller help, and does it pull pieces back out while the robot backs away with the stick
      still down? Match log: `/FlowerStick/RollerPower`.
- [ ] **Both robots: the turret servo becomes a goBILDA 25-3 Speed in continuous mode, on
      Expansion Hub servo port 5** (the SWYFT positional servo comes off; set continuous mode with
      the goBILDA programmer). **19429:** same port as now. **20245:** the turret servo moves from
      Control Hub servo port 4 to **Expansion Hub servo port 5**; its Control Hub port 4 stays
      empty, kept for a right star wheel like 19429's (`robot_20245.xml`). The turret's encoder is a
      **REV Through Bore Encoder in the intake's encoder socket** (beside Expansion Hub motor
      port 0; the intake's own encoder cable stays out, and the Cypher Max comes out of the
      feeder's socket). 20245 was wired this way on 17 Nov, 19429 on 18 Nov (servo on
      **Expansion Hub servo port 4**, since port 5 is its FLOWER stick's), and both robots'
      turrets are unlocked (`lockedForward` off, a line each in `RobotDifferences`).
      **19429, first time:** Bench: Turret before Basic Drive (signs, stop power, floor, top
      speed run on 20245's shared values); **Stop:** it runs away or holds the wrong way → set
      `TurretSubsystem.lockedForward` true in Panels and note what it did. With the turret's encoder there the intake's position
      hold on a full lane, its wedge clear and its stall event are off: watch a full lane hold
      its pieces on the power hold alone (`IntakeSubsystem.holdPowerWhenFull`). The configs on this branch
      already say this, so **install it only on a robot that is wired this way**: a positional
      servo sent a continuous servo's pulses swings to an end. A **full TeamCode install**,
      re-Activate, then **Check: Hardware**, then the Bench: Turret items in §2.
- [ ] **Panels 1.0.20 (FullPanels bump):** a **full TeamCode install** on each robot (a Sloth Load
      keeps the old Panels). **Good:** Panels opens at `192.168.43.1:8001`, the field and graphs
      update, the Driver Station still lists every `robot_*` config after a Sloth Load, and
      restarting the Robot Controller app brings Panels back without a `BindException`.
      **Stop:** any of those fails → back to `fullpanels:1.0.19` and note it in the PR. [PR #548]
- [ ] Charged batteries, more than one; swap at 12.0 V. [S4 Before]
- [ ] Tape measure, phone level app. Four POLLEN per FLOWER reload, and someone to reload. [S4], [S5]
- [ ] The HIVE on the field, red side, RIGHT CELL up (match start), tags on. [S3 Before], [S4 Before]
- [ ] Panels open (`192.168.43.1:8001`); robot Wi-Fi + REV Hardware Client so `pull-logs.cmd`
      works. Pull logs after each group: every check below ends in a match log. [S2 §1]
- [ ] A taped spot about 3 ft out from the red WALL FLOWER, square to the wall. [S5 Before]
- [ ] **Dry-run the competition loop once:** in Chrome, Visualizer → File → Save to repo folder…,
      pick this checkout, save R-Solo under its own name, Sloth Load. **Good:** the Driver Station's
      R-Solo name ends with that save's time; a fresh Chrome window later only asks to allow the
      folder, not to pick it. **Stop:** Chrome refuses or forgets the folder → note the Chrome
      version and do Save to GitHub instead. → [Visualizer PR #44]. [autos README](../TeamCode/autos/README.md#open-the-autos-on-master)
- [ ] **Event-loop dry run, laptop Wi-Fi off:** open the Visualizer once with Wi-Fi on the night before,
      then offline change one step in one of the three selectable Autos, **Export → Export Auto (Java)** and
      **File → Save to computer**, drop both into the checkout (`generated/` and `TeamCode/autos/`),
      Sloth Load over USB. **Good:** the Visualizer opens and exports offline, the robot runs the
      change. **Stop:** the offline copy does not open → open it online once more and re-check; note
      what failed. Commit the two files on an `auto/<name>` branch afterwards. [PR #374]

## 1. Hardware is all there (5 min)

- [ ] **Activate `robot_20245`, run Check: Hardware.** **Good:** nothing reported. **Stop:**
      anything reported; fix it before the rest. [S3 §2], [S4 Before]
- [ ] **Pinpoint `FAULT_BAD_READ`:** every Belding match had them (19429 2.3–2.7 a minute enabled
      on 6–7 Nov, 20245 1.0–1.6, up from 0.03–0.22 before 6 Nov), almost none with nothing running,
      and two held 7 s and 64 s; the hub's read was cut short in those, so the Pinpoint was not
      answering on I2C bus 1 (it is alone there). Most likely the cable or its connectors.
      One change per session, then 5 minutes of Basic Drive with the launcher spinning, counting
      `Pinpoint: FAULT_BAD_READ` events: (1) a new short 4-pin cable, strain-relieved within 2 in of
      both ends, routed away from motor leads; (2) the Pinpoint on bus 3 instead (its XML only).
      **Good:** none in 5 minutes. **Stop:** still there after both → the Pinpoint itself; note it
      on the hub photo (§8). [PR #503]
      On 19429 the Pinpoint was declared lost in Q13 and the lower final; that refused 13
      launch-spot presses and 9 FLOWER picks. Check the mount and connector on both robots, not only the cable.
- [ ] **20245: backLeft's encoder read 0 in all 8 Belding matches.** The turret's encoder belongs
      in the encoder socket beside **Expansion Hub** motor port 3 (`feeder`,
      `DeviceNames.TURRET_ENCODER_HOST`); backLeft is **Control Hub** motor port 3. Check that the
      turret encoder did not go into backLeft's socket, and that backLeft's own encoder is plugged in.
      **Good:** Check: Hardware, then a Basic Drive log with all four wheel speeds moving. [#500]
- [ ] **Both robots: launcher wheel encoder cables.** A wheel read no speed while spinning in one
      Belding match each (19429 Q32 right wheel, 20245 Q27), and the code shut it off: about 29 s and
      23 s with no shots. Reseat and strain-relieve both launcher encoder cables. **Good:** a 5-minute
      Bench: Launch Test with no "reads no speed" event. [#496]
- [ ] **A launcher wheel that drops out comes back** (`EncoderWatch`, #496). Bench: Launch Test,
      launcher spinning: pull one wheel's encoder cable at the hub, wait 5 s, plug it back.
      **Good:** the Match page warns within about half a second and that wheel stops; it kicks
      for half a second about every 2.5 s while unplugged; within about 3 s of plugging back the
      warning clears, the log has `launcher: reads speed again, wheel restored`, and the launcher
      reads READY with no spin-down. **Stop:** the pulsing wheel does anything alarming → Panels
      `LauncherSubsystem.encoderRetryAfterMs` 0 (the old rule: cut until spun down).
- [ ] **Both Control Hubs: which IMU chip, and how the hub is mounted** (logo and USB directions,
      for the SDK's orientation), before the IMU is logged. A Driver Station config scan shows the chip
      (BHI260AP or BNO055). Then one bench bump test with the IMU logged, to see what a hit looks like. [#498]

## 2. Any Auto, INIT only, no PLAY (about 20 min; camera connected, HIVE upright)

**INIT names the start the robot is on** ([PR #372]; the 10 in match radius is untested). Pick
the Auto, place the robot on the marks, INIT, press **no** alliance button. 5 frames settle a match,
about a second. Written out on branch `claude/eager-pasteur-f4trol` (`doc/robot-session-5.md`, last
section, not on master); this is that section.

- [ ] **Auto INIT holds the FLOWER stick in and the gate closed** (this PR). Stick out by hand and
      gate open, preloads behind it, INIT any Auto. **Good:** within a second the stick swings in
      and the gate closes, then both stay still until PLAY (G304.H), and the preloads stay behind
      the plate. Then INIT Basic Drive with the stick out by hand: **nothing moves** (G403). On
      19429 too: its stick is in at 0.06, not 20245's 0.6.
- [ ] **Auto INIT zeroes the turret and holds it at its home angle** (the Auto-INIT turret PR).
      Turret on its home mark, INIT any Auto. **Good:** the turret turns to `aimOffsetDeg` from the
      mark (20245 5°, 19429 5°; a locked-forward turret holds the mark) and
      holds there, still, until PLAY; the Robot page says "zeroed at the Auto's INIT"; PLAY does not
      move it again. Turned 20° off the mark by hand and INIT again: it holds 20° off the right place
      (the zero is wherever it sat), so re-seat it on the mark and INIT once more, and it is right.
      Then INIT Basic Drive after the Auto: the turret does **not** move in INIT (G403), and keeps
      the Auto's zero.
- [ ] **L-NoCross on Red far wall.** Same pass and fail.
- [ ] **R-Solo on Red far wall.** **Good:** "On Red far wall, not this Auto's start · … or move to
      Red audience", "No alliance" above it. Press Circle (red): "move to" still says Red audience.
- [ ] **L-NoCross on Red audience.** **Good:** "On Red audience, not this Auto's start · … or move to
      Red far wall", and the plain "Not on the … start" line does **not** also show, even after 10 s.
- [ ] **Off every start:** R-Solo one tile out from Red audience, same heading, then L-NoCross one
      tile out from Red far wall. **Good:** no "not this Auto's start" line; after 10 s, "Not on the
      Red audience start" (resp. "Red far wall").
- [ ] If the field allows: **R-Solo on Blue far wall.** **Good:** `BLUE (vision)`. (Blue audience
      wall is computed, not measured: say if it matches the wrong start.)

**Stop:** on its own start an Auto shows "No alliance" for more than 5 s with tags in view. Save that
run's match log: the 10 in radius is too tight for placement plus camera error. Read the start-check
gap with `tools/pull-logs/read_logs.py`; don't widen past 11.8 in (half of 23.6) without the logs.
Result, with the gap on each own-start run, → [PR #372].

- [ ] **INIT menu, L-NoCross on Red far wall:** gamepad 1 D-pad right three times (the first
      row, the partner wait, is selected).
      **Good:** `▶ waiting for TIP 1 ◀▶ until 12 s`, amber; left three times
      turns it back green, as drawn (until 9 s). D-pad down selects the second row,
      `if no TIP 1: CROSS AND FIX IT`; right shows `if no TIP 1: WAIT FOR PARTNER FIX`, amber; right again goes back.
      Nothing else on the robot reacts to gamepad 1's D-pad
      in INIT. **Stop:** a line does not change, or something else moves → save the match log.
      [PR #391]; the menu is the Clockwise PR's.
- [ ] **L-NoCross on WAIT FOR PARTNER FIX, with no partner** (Red far wall, `if no TIP 1: WAIT FOR PARTNER FIX`, the
      HIVE with RIGHT up and nobody shooting at it). **Good:** the robot drives to
      `LEFT_AUTO_HIVE`, the wait gives up at 9 s, it stays put (no crossing), and it parks on
      `LEFT_PARK` after 23 s with its preloads still in. **Stop:** it crosses under the HIVE → save
      the match log; the INIT pick did not reach the Auto.
- [ ] **INIT menu, R-Clockwise on Red audience, then L-Clockwise on Red far wall** (INIT only).
      **Good:** R shows three rows (waiting for TIP 2 until 23.2 s, Pickup WALL_FLOWER,
      Priority THREE_TIP), L two (waiting for TIP 1 until 9.5 s, Pickup GARDEN). D-pad down walks the rows and wraps;
      right on Pickup shows the other pickup; right on the time adds 1 s; everything changed is amber
      and bold. Read the two robots' Pickup rows to each other: WALL_FLOWER pairs with GARDEN.
      The page lists every row on its own line with `▶` at the selected one, which also shows
      `◀▶`; no line wraps. On 14 Nov 2026 the page showed only the
      selected row, ending `· ▲▼ row 1 of 3`, which wrapped and confused the drive team.
      **Stop:** a line wraps, or the pointer does not follow D-pad up and down → photograph it.
      **Stop:** a row does not change, or the two pages disagree → photograph both screens. **Not yet seen on a robot:** at Belding every L-NoCross wait gave up at the drawn
      8.0 s, and no log can say what INIT showed (the next item). Show the drive team the line too.
- [ ] **The chosen INIT options reach the match log** (same run: set the wait to 12 s,
      PLAY, let it run a few seconds, STOP). Belding's logs lost every `AutoOptions` line: PLAY
      wrote 160 tunables into a 160-slot loop. **Good:** `read_logs.py series <log>
      /RealMetadata/AutoOptions` prints `… waiting for TIP 1 until 12 s (drawn 9 s)`, and
      the log's last events have no `metadata lines` drop. **Stop:** it is missing → save the log.
      [PR #529](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/529)
- [ ] **INIT with the Limelight unplugged** (the warning line itself was logged 4 Nov 00:43; what is
      left is what the drive team sees). **Good:** light
      flashes white, "Camera: not answering" at the top of the Match page with what to do, the page
      fits without scrolling; right after a restart, "no frames yet" until the first frame.
      [PR #353], [PR #281], [S4 §9]
- [ ] **INIT with the colour sensor unplugged.** **Good:** its warning shows, the light flashes white.
      [PR #354]
- [ ] **Light looks:** Basic Drive INIT steady red, X → steady blue; off after PLAY; intake until a
      piece sits under the colour sensor → green after about 1 s and the roller stops. **Stop:** a
      wrong colour → set it in Panels → IndicatorSubsystem, note the value. [S4 §9], [PR #353]
- [ ] **19429 (20245 done 17 Nov, except step 6): the turret's continuous servo, on Bench:
      Turret, before anything else drives it.** Needs the 25-3 Speed fitted and the config installed (§0). The bench's javadoc is the
      checklist; in order, and put each value in a PR (`TurretSubsystem`, or a `RobotDifferences`
      line if the two robots differ):
      1. **Encoder**, in INIT, nothing powered: Cross with the turret on its mark, then one full
         turn counter-clockwise by hand. **Good:** `encoder turns` 2.75, `angle` +360. `angle`
         falls → flip `direction`. `angle` near 1440 or 90 → `countsPerEncoderTurn` is the other
         encoder's (8192 REV Through Bore, 2048 Cypher Max). Nothing moves → the cable is not in
         the intake's socket. (19429's `direction` has never been checked; -1 is 20245's.)
      2. **`servoStopPower`**: PLAY. The turret stands still at power 0, or creeps: step the power
         until it is still and note it.
      3. **`servoDirection`**: one R1 press. `angle` rises → +1; falls → -1 (the default, 20245's
         with its earlier servo).
      4. **`minPower`**: the smallest step from stopped that turns it steadily, each way (0.07 on
         another servo, 24 Oct).
      5. **`topSpeedDps`**: power +1.0 then -1.0, read `fastest`. 196 is the data sheet's. Much
         slower than 150 → tell a mentor before going on (it must out-turn the drivers).
      6. **The loop**: Triangle, Square. Push the turret off its target: it comes back and stops
         without hunting. D-pad up four times from 0: at 180 the next step goes on to -135 the
         short way. **Stop:** it runs away → a sign from steps 1 or 3 is wrong. Tune `kP` (0.03)
         and `kD` (0.001) in Panels; none of the loop's values has run on a robot.
      **20245, 17 Nov:** one turn 359.9° and 358.3°, `direction` -1; stop power 0;
      `servoDirection` +1; `minPower` 0.09; top speed 180°/s both ways (peak 194). All in the
      code. Its first encoder cable was an OctoQuad cable, which gave the hub one signal only:
      the count wandered by about 200 over a turn. Use the encoder's own 6-pin to 4-pin hub cable.
      Step 6, the loop, same day: `kP` 0.035, `kD` 0.006, `holdToleranceDeg` 2, floor back at
      0.09 (0.06 buzzes without moving). A 45° step turns to the angle and glides in, no
      overshoot. Then in Basic Drive, holding straight ahead: those rocked back and forth (its
      loop is slower than the bench's), and `kP` 0.02, `kD` 0.003 hold cleanly. In the code.
      Also found there: the intake reverses its motor, which flipped the turret's count read
      through its socket; fixed (`TurretSubsystem.hostSign`). Not yet run: tracking.
      **19429, 18 Nov (mentor: still 20245's numbers, tuning to do):** with `aimOffsetDeg` 5 the
      turn at PLAY (R-Solo 22:27 log) passed the target by 3.4°, came back, and stopped 1.9° short
      by 0.5 s, inside `holdToleranceDeg` 2, so the loop called it held. Steps 1–6 on 19429; for
      step 6 also read `/Turret/ErrorDeg` settling after a 5° step: **Good:** under 1°.
- [ ] **Square's wall spots on 19429, and 20245's launch trim, after 17 Nov's changes.**
      `wallGapIn` is 0.04 for both robots (found on 20245: 0 pushed into the audience wall and
      never settled, 1 was too much): on 19429 press Square from the tile row against the audience
      wall and against the scoring table's wall. **Good:** it arrives, settles and fires. 20245
      now launches 50 ticks/s slower at every range, in TeleOp and the Autos
      (`LaunchSpeedTable.robotOffsetTicksPerSecond`, its `RobotDifferences` line): its LAUNCH TRIM
      starts at 0. Check its shots land from a wall spot and a close spot, and in an Auto. 20245's turret is
      unlocked in the code (`RobotDifferences`); 19429's stays locked until it is rewired.
- [ ] **20245: straight ahead holds at the aim trim** (17 Nov). Basic Drive, turret straight
      (not aiming): D-pad left on gamepad 2. **Good:** the turret steps 1.5° left and holds, AIM
      TRIM on the Match page says L 1.5°; D-pad down brings it back to centre. Triangle to aim and
      back: it returns to the trim, not centre. A Square volley lands shifted by the trim. Circle
      (chassis aim) still puts the shot on the CELL plus the trim, once, not twice.
- [ ] **20245: does the turret need an aim offset?** `TurretSubsystem.aimOffsetDeg` is 0 again:
      4.5° counter-clockwise was tried on 17 Nov and the operator trimmed 4.5 back to shoot
      straight, so the first reading was not a fixed offset. To find one: shots from a wall spot,
      a close spot and each side, trimming to centre and resetting between spots. The same trim
      everywhere → that is the offset (Panels first, then its `RobotDifferences` line). A trim
      that changes with where the robot is → the pose, or the turret's axis (`turretForwardIn`,
      `turretLeftIn`). Also set the turret on the mark three times and see how much the aim
      moves between settings.
- [ ] **Both robots: the turret holds straight ahead in a match OpMode** (still `lockedForward`).
      Basic Drive, turret on its mark in INIT (limp, nothing powered), PLAY. **Good:** the Robot
      page says "locked straight ahead: holding 0°", a push by hand is resisted and comes back,
      spinning the robot leaves the turret on the nose, and a feed does not move it. **Stop:**
      "DROPPED" on the Robot page → the note says which sign or cable; back to the bench.
- [ ] **Both robots: unlock and track** (`TurretSubsystem.lockedForward` false in Panels, the
      mentor's call, only after the two items above). Basic Drive, pose known, turret mode AIM.
      **Good:** the turret stays on our CELL while the robot spins on the spot both ways and
      drives past the HIVE; it goes all the way round with no stop; `/Turret/ErrorDeg` in the log
      stays within a few degrees in a normal turn. A turn it cannot keep up with (faster than
      `topSpeedDps`) leaves it behind and it catches up after. Lagging in every turn →
      `topSpeedDps` is set too high or `feedforward` too low; leading → the other way.
      Then pull logs: the session reads the error against the turn rate.

## 3. The selected Autos, on the field (HIVE upright, preloads in, Panels open)

Use the work order above to select checks; the detailed mechanism checks below are not all Belding gates. Run each from its start, alliance from §2.

- [ ] **Autos keep the park (`belding-park`: `BuiltAuto.parkAfterVolley` on):** R-NoCross and
      R-Solo once each, loaded, HIVE upright. **Good:** R-NoCross parks every run; when its second
      volley ends late the Match page and the log say `… no time to park after it, parking now`
      and it parks from RIGHT_LAUNCH_CATCH; R-Solo still fires its third volley and parks by about
      29 s. **Stop:** R-Solo parks instead of its third volley every run → note the time it gave
      and tell a mentor (Panels `BuiltAuto.parkAfterVolley` off is the old rule).
- [ ] **Loop-time cuts, before and after:** Loop Time Baseline and one R-Solo run on the selected build,
      then the same on branch `claude/adoring-dirac-3ag180`, then 2 min of Basic Drive on it (intake
      to full, drive, fire four). Compare with `tools/pull-logs/read_logs.py series`:
      `/Robot/LoopMs` (median, p95), `/Loop/PageMs` (p95), `/Loop/Subsystem/ColorSensorSubsystemMs`,
      `FeederSubsystemMs`, `LauncherSubsystemMs`, `IntakeSubsystemMs` (means). **Good:** PageMs p95
      well under 8 ms; Launcher, Intake and Feeder means each lower by about the current reads they
      dropped (Feeder stays near 3.6 ms: that is the Expansion Hub's bulk read, not the feeder);
      ColorSensor lower while `/Intake/HoldingForFull` is 1 (`/ColorSensor/Watching` 0 there). The
      lane still goes full (indicator green, "Wait for IntakeFull" ends on the sensor), full clears
      in the first loop of a feed, and every volley still feeds all four. **Stop:** full late or
      stuck → `ColorSensorSubsystem.readIdleEveryMs` 50 in Panels (reads as before) and note the
      log; Panels field too jumpy → `FieldLayers.drawEveryMs` 100.
- [ ] **Panels disabled costs the loop nothing:** on the branch, run the Panels disable OpMode, then
      Basic Drive 1 min. The match log has the event "Panels disabled: no field view and no Panels
      telemetry this run", and `/Loop/PageMs` p95 is near the Match page's ~0.1 ms (no 7–8 ms field
      sends). Re-enable Panels and confirm the field and graphs come back. **Stop:** the event is
      missing with Panels off → `PanelsSwitch` cannot read the setting; note it in the PR.
- [ ] **Each selected Auto once:** L-NoCross and R-NoCross, then the two together.
      **Good:** no FAULT or robot/HIVE contact; verify the missed-TIP behavior before
      calling either routine qualified. See the work order above for the intended result.
- [ ] **L-NoCross: the two CELL checks in its Otherwise route**, drawn and exported 10 Nov
      ([the change, step by step](belding-auto-review.md#1-l-nocross-check-the-cell-before-each-volley-in-the-otherwise-route-about-3-points-a-match-in-playoffs-fairly-sure)).
      As exported, every route ends on LEFT_LAUNCH_CATCH and goes on with the steps below the wait,
      so the one endgame guard watches all of it: "RIGHT CELL still up?" seen up fires at the RIGHT
      CELL and then drives to LEFT_LAUNCH_CATCH if the guard lets it (else it parks from
      RIGHT_LAUNCH_CLOSE, the bent park). New drives, never driven on a robot: RIGHT_LAUNCH_CLOSE →
      LEFT_LAUNCH_CATCH (the existing curve reversed, turning over its second half, drawn 3.0 s) and
      WALL_FLOWER_STAGING → RIGHT_LAUNCH_CLOSE (straight, drawn 1.8 s): time both, and watch the
      first under the HIVE. In the simulator it scores as master does with every curated partner
      and parks every run; with a partner whose TIP lands while we cross, the 4 preloads go into
      the up LEFT CELL instead of the down RIGHT one (0–1 at a down CELL, master 4), with no time
      left for a second volley.
      Run it against three partner set-ups, each with the TIP wait timing out:
      - a partner that TIPs the RIGHT CELL a second or two after our 8 s limit (tip it by hand);
      - no TIP at all;
      - our own RIGHT volley not tipping it.

      **Good:**
      - "TIP seen?" ends on `LeftCellUp` within 0.75 s whenever the RIGHT CELL went down, and the
        robot goes back to LEFT_LAUNCH_CATCH.
      - "RIGHT CELL still up?" sends the WALL FLOWER's pieces to whichever CELL is up.
      - No volley at a down CELL.

      **Stop:** a check times out with its CELL in plain view and settled. Note how long the camera
      took in the log and in the PR.
- [ ] **Every volley after the first waits for its CELL to be seen up** (12 Nov, R-Solo, R-NoCross
      and L-NoCross; the first volley, into the RIGHT CELL the manual starts up, is the one with no
      check). New checks, none run on a robot:
      - R-Solo: "RIGHT CELL still up?" (0.75 s) before the volley back at RIGHT_LAUNCH_CLOSE, and
        "LEFT CELL still up?" (0.75 s) before the third volley.
      - R-NoCross: "RIGHT CELL back up?" before the third volley, waiting until 22 s on the match clock.
      - L-NoCross: "RIGHT CELL up?" (0.75 s, after "TIP seen?") before its first RIGHT volley;
        "LEFT CELL up?" and "LEFT CELL back up?" before each LEFT volley, waiting until 23 s.

      A check that times out fires nothing: the robot parks, or on L-NoCross's no-TIP route
      drives back to LEFT_LAUNCH_CATCH. A drive into a checked volley no longer opens the gate on
      the way in, so that volley starts from a stopped robot. Each check is a Wait step with an
      Otherwise in the Visualizer; all three Autos open in the builder. Run each Auto with the HIVE left alone, then with the
      checked CELL held down by hand at each check in turn.

      **Good:** with the CELL up, each check ends on `CellUp` (or `RightCellUp`) within about
      0.1 s and the volley fires; held down, the match log has the check `timed out`, no ball
      leaves, and the robot parks in time. **Stop:** a check times out with its CELL up, in plain
      view and settled → save the match log and note how long the camera took; a volley that used
      to fit now starts too late to finish → note the times of the drive's end and the gate.
- [ ] **The HIVE tracker counts only what the camera sees** (branch `claude/pensive-mccarthy-0wkna8`):
      one R-Solo run and two minutes of TeleOp, filmed, turning away from the HIVE and back several
      times, with one TIP made by hand while the robot sits still facing it. **Good:** no
      `HIVE (…): TRANSITION` line as the robot turns away, only `UNSEEN`; after each turn back the
      HIVE line names the CELL the video shows up; the hand TIP reads `TRANSITION`, then the other
      CELL; the Robot page's "at least N TIPs" is never above the video's count. In TeleOp with no
      CELL in view, the `speed table … (odometry, <CELL>)` line names our CELL at the robot's end of
      the field. **Stop:** the hand TIP reads `UNSEEN` before the camera settles it → note the
      seconds from `TRANSITION` to the settled line (`HiveTracker.Tuning.confirmSeconds`, a number).
- [ ] **R-Solo's second TIP: count what lands.** Film 5 R-Solo runs from the side, and count the
      pieces that land in the LEFT CELL from V2 and V3. At Belding it never TIPped (0 of 7), while
      the simulator TIPs it every time.
      - **It reaches 7 less than about one time in four:** draw R1 (park after V2, dropping the
        SCORING FLOWER leg; [review](belding-auto-review.md#3-dont-add-a-second-branch-to-r-solo-yet-first-find-out-why-its-second-tip-never-comes-no-change-the-open-question-is-worth-up-to-20)).
      - **Otherwise:** keep R-Solo as it is.
- [ ] **RIGHT_PARK moved 3 in toward LEFT_PARK (y 95.7 → 98.7), unseen: check it on the tape.**
      At Belding the scoreboard gave AUTO PARK to every LEFT_PARK end (6 of 6) but to RIGHT_PARK
      4 of 6 (not in Q-4, red, or Q-31, blue), from the same logged pose each time (14.1–14.5,
      95.6–96.1, or its blue turn); by the field model its centre was only 1.4 in inside the zone's
      edge. The move was made from those numbers, with no robot on a field (R-Solo, R-NoCross and
      the R-Two-Tip draft re-exported; in the simulator both still park and touch no partner on
      LEFT_PARK). Put the robot on RIGHT_PARK and look at the tape from above, then put a second
      robot on LEFT_PARK. **Good:** clearly over the line, and a hand's width between the two
      robots (the model leaves about 2.7 in for 16 in frames). **Stop:** still on the line, or the
      two touch → move RIGHT_PARK again in the Visualizer and Save team spots, by what the tape
      shows. [PR #373], [#516]
- [ ] **Stick down on the drive in:** spotters at WALL_FLOWER_STAGING and SCORING_FLOWER_STAGING,
      R-Solo, two picks each. **Good:** the stick swings down about 18 in before
      the robot stops and touches nothing before the push. **Stop:** it touches → Panels
      `FlowerPicker.stick.lowerWithinIn` 12, write it back. [S5 seam §2], [PR #378]
      Belding stream (7 Nov): at the WALL FLOWER it swings from 12–17 in out, is down 4–5 in before
      the stop and reaches the FLOWER's foot with no visible knock; the SCORING approach is hidden behind
      the FLOWER and wall, so only the SCORING FLOWER still needs spotters.
- [ ] **The stick lowers earlier at the WALL FLOWER:** the 3–4 Nov Auto picks read `prep done on
      the way in` 0.52–0.55 s at the WALL FLOWER (SCORING FLOWER 0.80–0.84), under the 0.7 s this check
      wanted, so its Stop applies: Panels `FlowerPicker.stick.lowerWithinIn` 24, then the stick line above.
      **Good:** X ≥ 0.7 at both FLOWERs and the stick still touches nothing. Write 24 back if so.
      [S5 seam §4], [PR #378]
      Belding (24, master's value since 5 Nov): WALL 0.62–0.73 s in 11 Auto picks (4 reach 0.7), FAR
      0.94–1.29 s; on video the WALL stick lands 0.2–0.4 s before the stop, touching nothing seen.
- [ ] **Time GARDEN gathers in Autos** (R-NoCross and L-LeftUnderHive, or L-Clockwise with Pickup
      GARDEN, GARDEN loaded with its 4 POLLEN, two runs each; count the next volley). No Auto gather
      has run on the 19 Nov defaults (dwell 150 ms, `intoCornerDeg` 15, `stallMs` 400):
      `AutoRegistration.GARDEN_GATHER_TYPICAL_S` 3.0 rests on 19429's TeleOp button gathers of 18 Nov,
      2.26 to 2.50 s without their line-up (`logs/2026-11-18-19429-RC`). **Good:** every
      `Last GARDEN gather: N s` line is under 3.0 (about 2.5). **Stop:** one over 3.0 → write the line
      down and raise the number to cover it, and `gardenGatherS` in `TeamCode/autos/timing.json`. [S5], [PR #384], [PR #378], [PR #422], [PR #553]
- [ ] **GARDEN gather, stick down on the drive in (13 Nov; never run):** spotter at the GARDEN
      corner watching the stick and both walls, L-LeftUnderHive, or L-Clockwise with Pickup GARDEN (they
      arrive facing the GARDEN), then R-NoCross (it turns half a turn evenly on its way in). The
      stick may come down early only on a drive that ends facing the GARDEN, once the robot has held
      that facing 0.2 s (`GardenGather.autoStick.facingHoldMs`), never while it turns. **Good:** on the L Autos
      the log says `GARDEN gather: stick down on the drive in, N in to go` with N near 24, then
      `prep done on the way in, 0.7 s ago` or more and `PREP 0.2 s` or less, and the stick touches
      no wall and no ball before the drive in. On R-NoCross there is no `stick down on the drive in`
      line at all: the card lowers it standing, `PREP 0.7 s`, as before. **Stop:** the stick comes
      down while the robot is still turning, or touches a wall → Panels `lowerOnDriveIn` off (the
      card lowers it itself) and write down the heading and where it touched; it only touches the
      floor ahead of a ball → `approachLowerWithinIn` 12.
- [ ] **R-NoCross and R-Clockwise: draw the turn to GARDEN_STAGING finished early (Visualizer):**
      both turn from 90° to 270° evenly over the whole drive, so the robot is 22° off with 6 in to go
      and the stick stays up until the card (0.7 s standing). Draw the heading turned to 270° by
      about halfway, held from there, and export. The turn passes through facing the side wall, so
      it must be over while the robot is still well out from it. **Good:** the robot faces the
      GARDEN from about 24 in out, the log says `stick down on the drive in`, and the gather starts
      with `PREP 0.2 s` or less. **Stop:** the faster turn swings the robot off its line or toward
      the wall → the old drawing, and the 0.7 s stays.
- [ ] **Drive: L-LeftUnderHive-StrangeGarden, the GARDEN along the audience wall (20 Nov 2026; never run):**
      the Utility draft ([its notes](../TeamCode/autos/drafts/README.md#l-leftunderhive-strangegarden-the-garden-the-other-way-in-20-nov-2026-never-run)),
      first alone (HIVE upright, LEFT up so it takes the preloads route: tip it by hand before 10 s),
      GARDEN loaded with its 4 POLLEN, phone filming the corner from above, spotter at the HIVE's
      bottom end; then beside the normal R-LeftUnderHive. Three runs. **Good:** the turn to face the red-side wall
      happens below the HIVE, clear of its foot bar and the centre line; the log says `GARDEN gather
      along the wall: stick down on the drive in, N in to go` (N near 24), then `over to the wall …
      (already there), then pushing in`, the drive in ends `stalled … pushing` in the corner, and
      `Last GARDEN gather along the wall` is under 2.0 s with the lane full (4) every run; the robot
      backs out along the wall without scraping it as it turns toward `RIGHT_AUTO_HIVE`, and the stick
      (up 250 ms after the hand-over) drags no POLLEN out. Write down the leg's time, the path card
      `under the HIVE, onto the audience wall` to `RIGHT_AUTO_HIVE`, beside L-LeftUnderHive's from the
      same session (simulator: 8.6 s against 9.0). **Stop:** the stick lands on the wall → Panels
      `GardenGather.autoStick.approachLowerWithinIn` 12; it pushes the row ahead instead of taking it →
      `GardenGather.alongWall.pushPower` 0.6; it misses the corner piece → `alongWall.intoCornerDeg` 10;
      the exit drives off the wall line before it is clear of the corner → note the pose at the hand-over
      (`alongWall.handOverBackIn` 6 backs further before the path takes over).
- [ ] **A volley before the park,** from any Auto run whose trace shows `going for it, not
      parking` (pull its log). **Good:** that volley's last ball left before 30 s on the match clock
      (the launcher's shot count), and the robot ended AUTO at the launch spot, touching no wall
      (LEAVE). **Stop:** the last ball left after 30 s → note the volley's path end → gate open → last
      ball times: `AutoRegistration.LAUNCH_ALL_LAST_PIECE_S` (4.0) is short. [PR #422]
- [x] **Fixed by the PIDF tune (#444), mentor 5 Nov.** **Launcher overshoot after a drive:** 4 Nov 00:48 the launcher ran to about 1680 ticks/s of
      1595 ±60 after each drive to `LEFT_LAUNCH_OFFSET`, and `AtSpeed` stayed false about 0.7 s, so
      the gate opened 0.98–1.12 s after arrival where the settle alone took 0.38–0.57 s. Graph
      `/Launcher/LeftTicksPerSecond`, `RightTicksPerSecond` and `AtSpeed` in Panels over an R-Solo
      run. **Good:** at speed by the time the robot settles. **Stop:** it overshoots again → it is
      the launcher's speed loop (a tuning issue for its own PR), and each volley costs ~0.5 s until
      then. [PR #422]
- [ ] **CellUp with the CELL already up:** L-NoCross at `LEFT_AUTO_HIVE` with the left CELL
      tipped up by hand first (or a partner's tip over before the wait starts). **Good:** the wait ends
      at once and the Auto fires, not at its limit (9 s on the match clock). Then with the
      CELL **down** and nobody tipping: it waits out its limit and fires anyway (the Auto's own choice for now). **Stop:** it waits with
      the CELL up, or fires at once with it down → save the match log; check which CELL the robot
      aims at from its end of the field (`AutoRegistration.aimedLeft`). [PR #377]
      *Belding (7 Nov):* no L-NoCross run started its wait with the CELL up. R-Solo Q-27 (20245) did: the red CELL tipped about 6 s in and stayed up, and its wait ended "CellUp after 0.02 s". The CELL-down half ran three times (Q-14 20245, M-7 20245, M-9 19429): video shows the CELL down at each 8 s limit, and the Auto took its no-TIP route.
- [ ] **L-NoCross's TIP wait on the match clock:** the wait set to 12 s in INIT, nobody tips.
      **Good:** the trace shows `Wait for TIP or CELL up: 12.0 s on the match clock` about 12 s into
      the match, then the no-TIP route. **Stop:** it ends at 9 s or counts from its arrival → save
      the log. [PR #391]
      *Belding (7 Nov):* every L-NoCross run had the limit at the drawn 8 s (nobody entered a later shot). The three timeouts ended "8.0 s on the match clock" 7.00–7.12 s after the wait began, so the limit is on the match clock. The 11 s setting was not tried.
- [ ] **Early park from L-NoCross's no-TIP route:** nobody tips, the wait set late (16 s or more)
      so time runs short at RIGHT_LAUNCH_CATCH or WALL_FLOWER_STAGING.
      **Good:** "parking from the spot the robot is on", the park from RIGHT_LAUNCH_CATCH bending west
      of the Red HIVE, parked by 30 s. **Stop:** it touches the HIVE → note the pose; bend that park
      (P on the step) in the Visualizer. [PR #391]
      *Belding (7 Nov):* the bent RIGHT_LAUNCH_CATCH park never ran. Q-14 (20245) parked early from SCORING_FLOWER_STAGING: it reached LEFT_PARK (127.2, 24.2) at the field's 30 s, still slowing, against the partner by the wall and nowhere near the HIVE. M-7 (20245) parked early from LEFT_LAUNCH_CATCH with 1.4 s left of a 2.9 s park: it was still at 24 in/s at the field's 30 s, and the disable stopped it about 9 in short of LEFT_PARK. Our PLAY was 0.9–2.6 s after the field's start, so our 30 s ends that much after the field's.
- [ ] **A launch's time limit never cuts a feed;** the Auto graphs the launcher in Panels. **Good:**
      every launch feeds its full `LaunchCards` time; launcher graph live. [PR #333], [PR #334]
- [x] **Fixed by the PIDF tune (#444), mentor 5 Nov.** **Launch on the approach: fix the launcher overshoot first.** It is a separate issue. After a
      dip the wheels overshot to about 1680 tps and kept AtSpeed false for about 0.7 s (3 and 4 Nov
      R-Solo logs), and the approach's gate waits for AtSpeed too. **Good:** `at_speed` in Panels
      stays 1 through the drive into LEFT_LAUNCH_OFFSET. **Stop:** still overshooting → judge the
      next two lines only on runs where the trace shows `LaunchAll opened the gate on the approach`.
      [PR #430]
- [ ] **Launch on the approach: hit rate against firing still.** R-Solo, three runs with
      `LaunchCards.launchOnApproach` on (the default) and three with it off. Count the balls that
      score from each LaunchAll after a drive. From the logs, time each arrival → first ball (was
      1.9–2.0 s at LEFT_LAUNCH_OFFSET). **Good:** the same hit rate, with about 0.5 s or more saved
      per LaunchAll after a drive. The late-turning drive from SCORING_FLOWER_STAGING shows no
      `opened the gate on the approach` line. **Stop:** more misses on → `PathEndSettle.launchWithinIn`
      3, again; still worse → `launchOnApproach` false. Write back the one that held. If it stays
      on, the measured LaunchAll time goes into `volleyS` in `TeamCode/autos/timing.json` and the
      Visualizer's LaunchAll time: both still plan the 4 s measured before #430.
      [PR #430]
- [ ] **Early gate: open ahead of launch speed (`LaunchCards.gateLeadS`, 0 = off, the default).**
      R-Solo (its preloads fire from rest at PLAY), filmed from the side so the first ball's flight
      shows. Three runs at 0, three at 0.3, three at 0.5, Panels → LaunchCards. Count the balls that
      score from each LaunchAll, the first ball of each volley above all. From the logs: PLAY → gate
      (was 0.42–0.70 s on 19429, 0.52–1.35 s on 20245 at Belding) and the `opened the gate, N s ahead
      of launch speed` lines; `/Launcher/SecondsToSpeed` against when `/Launcher/AtSpeed` really came.
      **Good:** the same hit rate, the volley's last ball about the lead sooner → write back the
      largest lead that held, never past 0.58 s (the fastest gate-to-first-ball at Belding). **Stop:**
      the first ball short or long → back to the lead below it; if it is short at 0.3 too, leave it
      0. A first ball that left within about 0.1 s of the gate was already against the wheels: it
      says nothing about the lead, but count it as a miss if it missed.
- [ ] **The looser settle (aimed and slowing, under 6 in/s):** read the `settled X s after path end`
      lines from the same runs with `launchOnApproach` off. **Good:** X under 0.3 s (was 0.38–0.57),
      and hits as good as before. **Stop:** misses → `PathEndSettle.slowSpeedInPerSec` 3.
      [PR #430]
- [ ] **Clockwise pair: R-Clockwise beside L-Clockwise.**
      [The plan, the stops, the times and the simulator's tables](../TeamCode/autos/clockwise.md). Not yet
      driven as a pair. Run with the DS AUTO
      timer on. Every volley is from a spot the drivers' Square goes to, and the guard
      plans a volley at 2.5 s (4.0 until 12 Nov), for every Auto. In this order, since each step
      decides whether the next is worth running:
      - **Volleys from the six launch spots, in TeleOp first:** eight pieces each from
        RIGHT_START, LEFT_WALL_HIVE, LEFT_WALL_NEAR and RIGHT_WALL_NEAR (hold Square; about 50 in)
        and from RIGHT_CLOSE_HIVE and LEFT_CLOSE_HIVE (hold Square from the middle tile row in front of them; about 32 in), counted on
        video. **Good:** 7 or 8 of 8 stay in from each. **Stop:** a spot holds fewer → note which
        and by how much; the pair shoots from all six.
      - **How long a volley really is**, from `read_logs.py steps` on every Auto run tonight:
        the whole card, the gate wait, and gate to last ball. **Good:** the card is 2.5 s or less
        in most volleys. **Stop:** most run about 4 s (the counter missed a ball and the feed ran
        its 3.5 s) → `AutoRegistration.LAUNCH_ALL_TYPICAL_S` and `LAUNCH_ALL_LAST_PIECE_S` go back
        up, or a late volley eats the park in every Auto.
      - **Each robot alone, HIVE in place, no partner, the new paths:** R's outside lane from the
        WALL FLOWER and from the GARDEN onto LEFT_CLOSE_HIVE (in front of the HIVE, on L's road:
        L must be gone); LEFT_CLOSE_HIVE to the SCORING FLOWER; back under the HIVE from
        the half turn from the SCORING FLOWER to LEFT_CLOSE_HIVE and the park from there; and
        from LEFT_CLOSE_HIVE under the HIVE nose-first to RIGHT_CLOSE_HIVE (it turns to the HIVE
        after it is out from under it) and the outside lane from there. L's 2.4 in
        slide to LEFT_WALL_HIVE; under the HIVE to RIGHT_START and its park from there; under the
        HIVE to the GARDEN and on to RIGHT_WALL_NEAR; under the HIVE to LANE_END and on to
        WALL_FLOWER_STAGING (it turns west just past the end of the HIVE's foot bar: watch that
        corner), and the FLOWER to RIGHT_WALL_NEAR. **Good:** no touch of the HIVE's feet, a
        FLOWER or a wall but at the spot itself; write each drive's seconds beside the drawn
        ones. **Stop:** a corner touches → note the pose and bend the path in the Visualizer.
      - **Arriving on a wall.** Three of the four wall-row spots end against a wall with the robot still
        rolling (a path card never settles). **Good:** the launch starts within about 0.5 s of
        arriving and the pose has not jumped. **Stop:** the robot bounces off or the Pinpoint
        faults → note the speed it arrived at.
      - **Turning with a load.** On 12 Nov (20245) pieces left the intake on both backward legs;
        every loaded leg is now nose-first. Four POLLEN in the lane: R from the WALL FLOWER up the
        outside lane onto LEFT_CLOSE_HIVE (it turns north at once, then round to aim), R from the
        SCORING FLOWER to LEFT_CLOSE_HIVE (a half turn in 13 in), and L from GARDEN_STAGING and
        from the WALL FLOWER to RIGHT_WALL_NEAR, at Auto speed. **Good:** four still in the lane
        at each end, five runs of each. **Stop:** a piece leaves → note where on the path, and
        whether braking or the turn did it.
      - **The two parks, both robots.** RIGHT_PARK and LEFT_PARK leave 2.7 in between parked
        robots. Park R first, then run L's park; then the other order. **Good:** neither touches
        the other coming in (L turns east of the park zone and drives straight in; R comes in
        along RIGHT_PARK's own line). **Stop:** a touch → note which robot was moving and where.
      - **R's view of the HIVE at each stop:** `LeftCellUp` on LEFT_CLOSE_HIVE (270°),
        `RightCellUp` at RIGHT_CLOSE_HIVE having backed in (90°), and `RightCellUp` from
        LEFT_CLOSE_HIVE (270°). L: `LeftCellUp` on LEFT_WALL_HIVE and `RightCellUp` on
        RIGHT_WALL_NEAR (66.9°). **Good:** each within 0.3 s of arriving with that CELL up.
        **Stop:** the camera does not settle the CELL → note which tags it does see.
      - **The spill after a TIP, on video, with pieces in both CELLs:** how long after the last
        ball leaves the last spilled piece is still falling, and where the spill lands at each
        end. R does not wait before backing under the HIVE from the SCORING FLOWER (in the simulator
        it drove into falling pieces in about a third of the runs where it crossed), and L waits to
        22.5 s (TLr) on RIGHT_START after a rescue volley. **Good:** nothing still falling when
        either moves, and no piece lands on a robot standing on a wall spot. **Stop:** a piece
        does → write the seconds and where; the waits change in the Visualizer.
      - **The times, from `read_logs.py steps` on each run's log:** `LeftCellUp` for L (T1
        9.5 s; the Belding films have it as late as 9.98 s, so expect to raise it); R on
        LEFT_CLOSE_HIVE and its volley's end (T2 15.5 s, TS 17.6 s; drawn 13.2 and 17.0 s); R's
        SCORING FLOWER pick's end (T3b 22.2 s; drawn 21.3 s); R at LEFT_CLOSE_HIVE (T3 23.2 s; drawn 23.1 s); L on
        RIGHT_WALL_NEAR (T4 22 s) and each robot parked (TL 22.7 s). **Good:**
        each within 0.5 s of the drawn. Otherwise set the row in INIT for the next run and write
        the measured value on the PR; the drawn values change only in the Visualizer.
      - **The look, and the Priority row.** After the SCORING FLOWER R turns to LEFT_CLOSE_HIVE and
        asks for RIGHT up until 23.2 s. With L's preloads and R's first load in LEFT (TIP 2 comes):
        on Priority THREE_TIP R goes under the HIVE nose-first and shoots at RIGHT, and is not
        parked at 30 s; on PARK it waits to 26.4 s and parks with its load. With TIP 2 held back
        (hold L's preloads): R shoots at LEFT and parks. **Good:** the trace shows the row taken,
        the last ball at RIGHT leaves before 30 s on the DS timer, and the robot is off the walls
        when AUTO ends. **Stop the run by hand** if L is still in the lane when R leaves
        LEFT_CLOSE_HIVE for its park. Write when R reached LEFT_CLOSE_HIVE, when `RightCellUp`
        fired, and when its last ball left: T3 (23.2 s) and T3b (22.2 s) are set from the simulator.
      - **The fallbacks, on purpose, both robots:** hold R's preloads back (L1 "no": at 9.5 s L
        crosses, shoots from RIGHT_START and parks at 22.5 s; R parks at 15.5 s with its load),
        then L's. **Good:** the trace shows those rows, and both robots park.
      - **The other pickup:** Pickup GARDEN on R and WALL_FLOWER on L. **Good:** L stands at the
        LANE_END until 14.0 s (TF) and R has passed the WALL FLOWER by then.
      - **Shot trigger TIMER on both,** with T1 8 and T2 13 set in INIT, the camera covered.
        **Good:** each stop shoots at its time; nothing waits on the HIVE.
- [ ] **Drive times for the planning numbers** ([#504], the Belding timing analysis): a Utility
      that follows a Pedro path, empty and again with 4 POLLEN in the lane, a
      charged battery (note `/Robot/BatteryVolts`), three runs each. (a) A straight 90 in run at a
      constant heading, (b) the same 90 in with a 180° turn spread along it, (c) R-Solo's
      `WALL_FLOWER_STAGING` to `LEFT_LAUNCH_CLOSE` (it turns 270° on the way). Read each run's
      `auto: path` to `path end` seconds and `/Drive/OdometrySpeedInPerSec`. **Good:** (a) peaks
      55–63 in/s and takes about 2.4 s; (b) and (c) cruise 20–30 in/s, (c) 3.6–4.3 s, as at
      Belding: then the simulator's `TURN_SHARE_RAD_PER_S` (130°/s) stands. A loaded run more than
      0.3 s slower than the empty one means the planning numbers need a loaded value.
      **What these runs decide** (10 Nov, from the 17 Belding logs): the Visualizer's drawn times
      are within 0.1 to 0.6 s, on the slow side, for every kind of Auto drive but one: the 90 to
      100 in curves from `WALL_FLOWER_STAGING` to the left launch spots, drawn 2.9 s, driven in a
      median 4.1 s (R-Solo, 9 runs) and 4.3 s (L-NoCross, 2). Timing every drive by the
      simulator's shared-wheels rule (130°/s) in the Visualizer was tried and not kept: it put
      those curves at 4.35 s but made the rest worse (51 in with a 90° turn: 2.3 s against 1.8 s
      driven; 13 in with a 180° turn: 1.8 s against 1.2 s; 92 in with its 180° turn in the first
      fifth: 3.8 s against 2.6 to 2.9 s). A 180° turn bunched at the start of a 92 in drive cost
      nothing, so the slow curves may be their sharp bend, not their turn: (b) against (c) tells
      which, and so whether turning the short way (the R-Two-Tip draft) would help.
      **Also:** the two Belding runs that crawled for 4 s about 10 in short of `LEFT_LAUNCH_CLOSE`
      / `LEFT_LAUNCH_CATCH` (20245, 14:39 and 16:53 hub time) look blocked: check the match video.

- [ ] **The lane pair: R-LeftUnderHive and L-LeftUnderHive (R-OutsideLane and L-UnderHive until 20 Nov), on the
      Autonomous menu since 16 Nov** (they were the Utilities Drive: R-OutsideLane and Drive: L-UnderHive). Redrawn that day onto the
      Autos' launch spots and never driven: where the steps below say L4, read `LEFT_AUTO_HIVE` for
      L and `LEFT_AUTO_OFFSET` for R; R5 is `RIGHT_AUTO_OFFSET` and R4 `RIGHT_AUTO_HIVE`.
      [Routes and the simulator's numbers](../TeamCode/autos/drafts/README.md#the-lane-pair-r-outsidelane-and-l-underhive).
      In this order:
      - **Each alone, no pieces:** it drives its route and parks. **Stop:** it touches the HIVE, a
        FLOWER it is not picking, or crosses the centre line → save the log.
      - **R alone, four preloads:** note the match second its LEFT volley ends. Past 15.5 s it parks
        from the left spot (expected today); before 15.5 s it goes on to the SCORING FLOWER and R5.
      - **Both together,** four preloads each. Count the pieces that stay in from L4 (L's preloads)
        and from the same spot by R (its WALL FLOWER load), and the TIPs.
      - **The Pickup row:** in INIT each trial shows `▶ Pickup ◀▶: WALL_FLOWER` (R) or `GARDEN` (L);
        D-pad left and right change it and the line turns amber. **Leave both as drawn.** The swap
        (R `GARDEN`, L `WALL_FLOWER`) waits on a quicker GARDEN gather: the robots meet at the south
        end. To try it, run R alone on `GARDEN` first and note the second it leaves the corner.
        **Good:** by about 9.5 s. Later → do not run the swap as a pair.
      - **Does R keep its four going up the lane?** It drives intake first now, turning to the
        HIVE only at the end. Count what its LEFT volley fires. **Good:** 4. **Stop:** 3 → note
        where the ball left (the film), and whether it was in the last turn into the spot.
      - **Time R's leg from the WALL FLOWER to the left spot** (`auto: path wall lane north` to
        its `path end`). Drawn about 3 s; the simulator says 4.4 s. **Good:** under 3.6 s, and the
        third volley fits on a 2.5 s volley. Over 4 s → R parks after its LEFT volley; say so.
      - **Does L clear L4 before R gets there?** Both fire from L4. Note the match second L leaves
        it and the second R arrives (simulated: R within reach at 12.0 s, there at 13.2 s). **Stop:**
        R arrives while L is still on the spot or just leaving → note both seconds; the fix is a
        short wait for R, which costs its third volley.
      - **R's park from L4:** it squares up and drives into `RIGHT_PARK` straight. **Stop:** it
        comes in at an angle beside a parked robot → save the log.
      - **If R goes on while L is on the right side:** R5 and R4 overlap, and R passes the GARDEN
        as L leaves it. **Stop the run** if they are closing, and note the match second.
      - Film each paired run from the side.
## 4. Basic Drive at one FLOWER (Panels → FlowerPicker; tape measure)

The pick-depth session. Procedure and tables: [S5 At the robot]. The student version of the same
measurement is [#255].

- [ ] **Pose on the field:** face the HIVE, relocalize. **Good:** no "refused". [S5 §1]
- [ ] **Touch point and stick reach, by tape.** **Good:** touch point within 1 in of 12.4 in; stick
      reach ahead of the face written down (must be < `retractAfterBackIn` 6). **Stop:** off by more
      → measure the FLOWER's position first. [S5 §2]
- [ ] **`contactStandoffIn` 7.0 (the default since 4 Nov), then 8.0 and 9.0: three picks each,**
      reloading between, in the R-Solo Auto where the start pose is known. The red WALL FLOWER
      stalled at 10.0 to 10.4 in at both 5.0 and 7.0 (3 Nov), so expect the same stall at 8.0, and at
      9.0 a pose 1 in short may end `reached` again. **Good:** every push ends `stalled`, 1.0-1.3 s,
      the lane reads FULL within about 1.2 s, robot square. Read the value from each pick's start
      line (`read_logs.py picks`). [S5 §3-5]
- [ ] **Pose 2 in long, then 4 in long,** at the best value; then **9.5, three picks** if time.
      **Good:** 2 in long changes nothing. [S5 §6–7]
      **Stop (whole block):** `timed out`; skew > a few degrees (try `pushSpeed` 0.4); the stick
      swinging up near the FLOWER. Results → the PR that changes the default. [S5]
- [ ] **TeleOp, stick already down, press pick.** **Good:** no 0.7 s pause; stick up, as before.
      [S5 seam §5], [PR #378]
- [ ] **Pick from about 55 in, and from the line-up spot turned 90°.** **Good:** it reaches; it
      turns before it pushes. [S4 §7], [PR #281]
- [ ] **Ten picks from loaded FLOWERs:** pieces left behind. **Good:** none left, no waiting on an
      empty FLOWER (the dwell also ends on `full` since #384). **Stop:** tune `dwellQuietMs`. [S4 §7]
- [ ] **A wedged intake backs itself out** (`WedgeClear`, on; `IntakeSubsystem.clearWedge`, #507).
      At Belding the roller stalled in 8 of the 17 match logs (under 50 ticks/s at 7–10 A; it runs
      at about 2400 and under 3.4 A), the worst for 13 s with an empty lane (Infrared Q26). Now a
      stall of 1 s with nothing under the lane sensor backs the roller and stars out for 0.25 s
      (the prefeed follows the stars, as in a reject), at most 3 times in a row. Bench, intake on,
      lane empty: roll in pairs of POLLEN side by side, 20 pairs, and hold one piece against the
      roller by hand once. Then with the lane full (4 in, the light green) push a 5th at the mouth
      for 3 s. **Good:** each wedge logs `intake: wedged … backing out, clear N` and the pair then
      goes in; held by hand it tries 3 times and stops; the full lane never backs out and keeps all
      4; a FLOWER pick in R-Solo still ends as before. **Stop:** it backs out with pieces in a
      healthy lane, or throws a piece clear of the robot → Panels `clearWedge` off, and note the
      `/Intake/TicksPerSecond` and `/Intake/CurrentAmps` at that moment. The 0.25 s (`wedgeBackMs`)
      is a guess: write back what frees a pair without throwing it. This is the code half of the
      fix list's "stop two pieces wedging"; the guide or sprung wall at the pinch is still open.
- [ ] **FLOWER pick: a 1 s dwell, ten picks on 20245 first** (Panels `FlowerPicker.dwell.dwellMs` 1000,
      from 2000; a number, no code; `routine` MASTER). Do this before the three-routines line
      below if PR #550 is not on the robot yet: it is the cheap way to a faster pick, and the
      biggest single part of what `FAST` does. Why: of a Belding Auto pick's 3.6 s (median 3.68, 23
      picks, match logs), 2.0 s is the dwell, and on 20245 it is mostly waiting: the FLOWER is empty
      within about 0.8 s of contact (Q-27, film) and the 4th piece comes in as the robot backs out;
      of the pieces that do arrive in a dwell, half are in by 0.5 s after contact and nine in ten
      by 1.4 s (78 Auto picks, intake current). So a 1.0 s cap gives up some late pieces: that is
      what this counts. Expected pick: about 2.6 s.
      *Setup:* as the line below (empty lane, 4 POLLEN in the FLOWER, reload every pick). R-Solo
      from the Red audience marks, five runs, so five picks at the WALL FLOWER and five at the
      SCORING FLOWER; the last five runs at 2000 on the same night are the baseline if there is no
      20-pick baseline yet.
      *Write down, per pick:* pieces the next volley fired (by eye or phone; `read_logs.py steps`
      for the wheel-dip count, never the lane count or the shot counter), pick time (`Last FLOWER
      pick: N s`), how the dwell ended (`DWELL N s (dwellMs)` or `(full)`), and pieces left on the
      floor or in the FLOWER.
      **Good:** 4 fired from at least 9 of the 10, nothing left behind, pick time about 1 s under
      the 2000 runs → go on to 20 at each FLOWER (18 of 20), then write 1000 into `FlowerPicker`,
      re-measure `AutoRegistration.PICK_FLOWER_TYPICAL_S`, and tell whoever owns the Autos that
      each pick is about 1 s shorter. **Stop:** 3 or fewer from more than one pick → try 1400 (the
      nine-in-ten mark), ten more; still short → leave 2000 and say so here. On 19429 only after its
      stick check (§7): its dwell brought nothing in 7 of 13 Belding picks, so a shorter one loses
      little there but proves nothing either.
- [ ] **The lane count after a pick or a gather: does it say what is in the lane?** Reported
      by two Auto chats on 16 Nov (`doc/auto-handoffs.md`): on three picks of 14 Nov (20245)
      `/Lane/Count` read 2, 3 and 1 where the volleys then fired 4, 4 and 5, and `/Lane/Full` was
      right on two of the three. The colour sensor refreshes about every 130 ms and saturates, so
      it cannot count balls that touch; the beam break of [#213] is the fix. Until then nothing
      decides on "the lane is not full" from the count. Ten picks at a FLOWER, on each robot:
      write the Robot page's lane count and whether it shows full right after the pick, then the
      pieces the next volley fires, by eye. **Good:** full is shown on every pick that fired 4.
      **Stop:** full missed on a 4 → note how many of ten; that is how often a "lane full" wait
      would time out.
- [ ] **FLOWER pick: three routines, 20 picks at each FLOWER on each robot** (Panels
      `FlowerPicker.routine`: `MASTER`, the default and the Belding pick; `FAST`; `MULTI_PUSH`;
      PR #550). The pick's start line in the match log names the routine.
      Where a Belding pick's 3.66 s went (median, 25 Auto picks, both robots, match logs): waiting
      to slow after the drive in 0.13 · push moving 0.66 (8.8 in) · push stopped before the stall is
      called 0.27 · dwell 2.01 · back-out 0.63 (9.7 in; 6 in at 0.44).
      `FAST` takes the waiting out: no wait to slow, the dwell is 1.0 s from contact, and the pick
      ends 6 in into the back-out, still rolling, the stick lifting on its own once clear.
      **Expected about 2.2 s**, 1.5 s under MASTER. `MULTI_PUSH` is FAST with two pushes: dwell
      0.6 s, back off 3 in with the stick down and the intake on, push again, dwell at most 0.5 s.
      **Expected about 3.0 s.** Why two pushes: at Belding the last piece came in as the robot
      backed off, not in the dwell (20245: 0.1 to 0.4 s into the back-out in 6 of 10 match Auto
      picks, the FLOWER empty within about 0.8 s of contact on Q-27's film; 19429: nothing in the
      dwell in 7 of 13, a piece 0.6 to 0.7 s into the back-out in 11, the POLLEN still in the
      FLOWER through the dwell on Q-13's film). Neither expected time has been run on a robot.
      *Setup:* an empty lane before every pick, 4 POLLEN in the FLOWER, someone to reload. R-Solo
      from the Red audience marks, 10 runs per robot per routine (10 picks at each FLOWER), then 10
      more at each FLOWER in Basic Drive with the pick button held to the end, firing every load
      before the next pick. MASTER first: it is the baseline, and nobody has counted 20 on either
      robot. Five picks of a new routine before committing to its 20.
      *Write down, per pick:* **pieces the next volley fired** (count the balls leaving, by eye or
      phone; `read_logs.py steps` gives the wheel-dip count, never the lane count or the shot
      counter); **pick time** (`Last FLOWER pick: N s`); **stall depth** of each push (`IN … (stalled
      N in short)`); pieces left on the floor or in the FLOWER; for MULTI_PUSH the `BACK_OFF N s`
      line (0.6 is its timeout).
      *Watch, on FAST and MULTI_PUSH, the hand-over:* the Auto's next drive starts with the robot
      about 3 to 4 in short of the staging spot and still rolling back. **Good:** it flows into the
      drive, no swerve, and the stick is out of the FLOWER before the robot turns or strafes.
      **Stop:** the stick drags on the FLOWER or the drive hooks → `handOverBackIn` 0 (backs all the
      way, as MASTER) and say so here. Lower than 6 only after the stick's reach is measured
      (§4, "Touch point and stick reach").
      **Good:** `FAST`: 4 fired from at least 18 of 20 at each FLOWER, no worse than that robot's
      MASTER count, pick time at least 1 s under MASTER's. `MULTI_PUSH`: 4 from at least 18 of 20,
      more often than MASTER and FAST on that robot, pick time no more than MASTER's. Then set the
      winner as `routine`'s default, re-measure `AutoRegistration.PICK_FLOWER_TYPICAL_S` (4.0 is
      MASTER's), replace `MULTI_PUSH_MOVE_MS` (0.65 s, an estimate) from the logs, and tell whoever
      owns the Autos how much shorter a pick is. **Stop:** FAST loses a piece MASTER gets →
      `fastDwellMs` 1400, ten more; the push goes in crooked → `fastSettledInPerSec` 8; MULTI_PUSH's
      second push knocks a piece away or wedges one at the mouth → `multiBackOffIn` 2, then 5, five
      picks each. If a routine is no better than MASTER on a robot, leave MASTER and say so here
      (on 19429 see its stick check in §7 first).
- [ ] **Let go mid-push, then mid-dwell.** **Good:** mid-dwell backs out, stick up once ~18 in clear;
      operator Square while waiting raises it at once. [PR #273], [PR #280]
- [ ] **GARDEN into the corner,** driver Triangle near the red GARDEN, three times. **Good:** pressed
      into both walls, intake in the corner; note the `towardWallIn` that works (0 = off).
      [S4 §6], [PR #279]
- [ ] **Drive speeds, the drivers' call:** Basic Drive, a few minutes each driver: normal across
      the field, diagonals included, then hold L1 (slow) to line up on a FLOWER. Turbo is gone
      (5 Nov): normal is now full power 1.0 (the old turbo, but with the acceleration ramp on), slow
      is 0.76 (the old normal), and R1 does nothing. Change them live in Panels → TeleOpConfig if
      they ask. **Good:** the drivers keep full power as normal and slow is slow enough to line up;
      write the values they keep in `TeleOpConfig`, pull the log. **Stop:** normal feels sluggish
      off the line compared with the old turbo → the ramp is the difference (turbo skipped it);
      tell a mentor. [PR #425]
- [ ] **Faster acceleration ramp (21 Nov 2026, both robots, TeleOp only):** `DriveSubsystem.accelPerSec`
      2 → 8 and `turnAccelPerSec` 4 → 8, after a mentor's test showed it a huge improvement. An Auto
      keeps 2 and 4 (`autoAccelPerSec`, `autoTurnAccelPerSec`): at 8 an Auto turned and moved on oddly
      before its last volley; pull that run's log if it is ever repeated. Hold Circle
      on a CELL a few times, run one FLOWER grab, and slam the stick forward once with four pieces
      in the lane. **Good:** the chassis aim settles without hunting, the grab is clean, nothing
      shifts or rocks. **Stop:** the aim overshoots or wobbles → lower `turnAccelPerSec` in Panels
      first (4 was the old value); the robot lurches or pieces shift → lower `accelPerSec`.

## 5. Pose and camera (Basic Drive / Vision: HIVE & Pose)

- [ ] **Does the camera see a TIP's swing before it settles?** (20 Nov 2026, for R-NoCross's late
      volley: firing as TIP 2 swings, with half the guard margin, took R from 6 to 11 of 12 in the
      simulator.) Every match log now has `/Hive/Cell/<CELL>/RowHeightIn`, `/Tags` and `/State`, one
      sample per camera frame that saw the CELL; nothing to set up. **At the lab:** the robot still on
      `RIGHT_AUTO_HIVE` facing the RIGHT CELL, LEFT up and loaded; tip the HIVE by hand, slowly, five
      times, filming it. **After Fruitport:** the same from every match where a robot stood watching a
      TIP (R-NoCross's late wait, L-NoCross's wait for TIP 1). Run
      `python3 tools/pull-logs/read_logs.py cells LOG.wpilog`: per settle, how long before it the row
      was first seen and first moved, and the robot's top speed (still is under about 1 in/s).
      **Good:** "moved lead" (or "seen lead" for a CELL rising into view) of 1 s or more in most
      TIPs, never in a log without a TIP. Then a swing trigger can be built. **Stop:** the lead is
      under 0.5 s or the height wanders as much with nothing tipping → the camera cannot tell; leave
      R-NoCross as it is.
- [ ] **Camera calibration:** done from the Belding and Fruitport logs (22 Nov 2026, `mountfit.py`); see
      the camera-mount check in the 19429 list above. No R1 run needed.
- [ ] **Continuous fixes, on the home field, before States** (22 Nov 2026). Every run already scores them:
      `FusionShadow` takes the moving frames beside the real pose, and each stop after a drive logs a
      "fusion shadow: stop N ... camera X in from the real pose, Y in from the shadow" line, with one
      summary line when the OpMode stops. **Session 1, off (as shipped):** Sloth Load, then two full
      practice matches per robot, Auto and TeleOp, driven the way the drive team drives at an event
      (FLOWER picks, GARDEN, volleys from both ends, a wall push or two); pull the logs. **Good:** in
      the summary lines the shadow's arrival median is under half the real pose's, "over 1 in further"
      at no more than about 1 stop in 30, and the shadow's worst no worse than the real pose's worst plus
      1 in. **Session 2, on:** set `LocalizationTuning.continuousFixes` true in Panels (or a
      `RobotDifferences` line for one robot), the same two matches. **Good:** the real pose's arrival
      median now matches session 1's shadow (about 0.3-0.5 in), no Auto path or FLOWER pick misses
      that session 1 did not, nothing odd on the field drawing near the HIVE while a CELL tips.
      **Stop:** any stop with the real pose 4 in or more off on arrival that session 1 did not have, a
      path that ends somewhere visibly wrong, or the pose jumping on the field drawing → turn it off,
      push the logs. Both good → the shared value goes to true in `LocalizationTuning`, in a PR with
      both sessions' summary lines.
      [handoff](../.claude/handoff/20245-camera-calibration.md), [PR #360]
- [ ] **Heading-aware camera reset:** lift the robot, turn ~15°, set it down off the mark, still,
      hold D-pad down. **Good:** heading corrected, field-centric drives right again. [handoff], [PR #360]
- [ ] **Marks in Panels:** grey crosses on the starts and reset corners; park on 3–4 marks over a
      match-length drive and note `shadow_offset_in`. [PR #361]
- [ ] **Panels drawing:** X then B in INIT turns the outline blue → red; each FieldLayers switch
      shows and hides its robot. [PR #356], [PR #307]
- [x] **Done on red at all six spots (mentor, 5 Nov); blue and the left corner at Belding (7 Nov);
      the right corner's heading is in §7d.** **Go to launch spot (driver Square, hold) vs chassis aim (Circle):** pose set (corner or
      camera reset), the drivers try both from around the field over a practice, firing from where
      each leaves them. It goes to the nearest of six spots (the two starts and the four
      CATCH/OFFSET spots); press it once near each start: it stops `LaunchSpotDrive.startOffWallIn`
      (3) off the wall, aimed at the CELL. From beside the HIVE, watch the straight path's clearance
      from the frame (let go if it heads for a leg). **Good:** arrives aimed (`launch spot: arrived
      … in N s, X in and Y° off`), shots land, never touches the wall; the drivers say which they
      prefer. Compare the match log's `launch spot:` events with its `chassis aim:` ones.
      **Stop:** it refuses with the pose set, or clips the HIVE or the wall →
      `TeleOpConfig.launchSpot` off and note where. [PR #423]
- [ ] **Square also fires (`TeleOpConfig.goToSpotAlsoFires`, on):** loaded, hold Square from
      15–30 in off a spot; then let go mid-feed; then, loaded, hold Square 6 s at a spot (since
      6 Nov it feeds for as long as Square is held, no 3.5 s cutoff). **Good:** launcher spins up
      as it drives, feeds once there (`launch spot: gate open … at speed, settled`), all four leave; the end line
      says `fired by button`, shots counted ≈ 4; let go stops the feeder at once and idles the
      launcher; held 6 s, the gate stays open all 6 s. **Stop:** it feeds before it is aimed, or
      feeds after let go → `goToSpotAlsoFires` off. [PR #423]
- [ ] **The operator cannot hurt a Square volley (`belding-plus`, `LauncherOwner`, 7 Nov):**
      (a) operator spin up, wait for the rumble, then the driver holds Square: the gate opens on
      arrival with no spin-up wait, and the launcher idles when Square is let go. (b) Square from
      idle, operator presses the toggle mid-drive: the launcher keeps spinning (`launcher: toggle
      during Square's volley: kept spinning`), the volley fires at speed, and it idles when Square
      is let go. (c) Square from idle, operator holds FIRE before the robot arrives: nothing feeds
      early (`fire: held back`), the volley opens at the spot as usual. (d) No Square: operator
      toggle up, FIRE, toggle down, as before. **Stop:** a toggle press during Square idles the
      launcher, FIRE feeds before arrival, or the launcher keeps spinning after Square is let go
      → run `Belding`'s Basic Drive and tell a mentor.
- [ ] **Match page: loop time and Pinpoint lines (mentor, 14 Nov 2026):** Basic Drive, Match page.
      **Good:** under `N PIECES`, two more huge lines: `LOOP 21 ms` (whole milliseconds, the last
      loop) and `PINPOINT OK` in green. Unplug the Pinpoint's cable for a second and plug it back:
      `PINPOINT LOST` in red, then `PINPOINT CHECK` in amber until a corner or camera reset, then
      `PINPOINT 1 FAULT` in amber for the rest of the OpMode. All five lines and an alert fit the
      screen with none cut off. **Stop:** a line is cut off at the bottom, or the Pinpoint line
      stays green through the unplug → photograph the screen and save the match log.
- [ ] **Operator's glance page, trim rumble, D-pad trim reset, no intake toggle (`belding-plus`,
      7 Nov):** Basic Drive, Match page, read from the operator's seat and from the coach's spot.
      **Good:** under the header and the alliance line, three lines in text about twice ordinary
      size (three nested `<big>`): `TRIM 0`, then `IDLE` / `SPIN` (amber) / `READY` (green), then
      `N PIECES`, green while the sensor sees the lane full; a fourth, amber, only when
      something is wrong (`INTAKE OFF`, `ROBOT-CENTRIC`, `SQUARE REFUSED` while Square is held,
      `FLOWER STICK OUT`, `NO PINPOINT`). Everything else is on the Robot page (Share twice).
      Operator R1: `TRIM +25` in green and one rumble blip; L1: `TRIM -25` in red, two blips; D-pad down: back to
      `TRIM 0` with a long buzz. The intake runs from PLAY with nobody touching R2, and no R2 press
      on either pad stops it; with `TeleOpConfig.intakeAlwaysOn` off it runs only while R2 is held.
      Note every green count under 4 (`3 PIECES` in green): that is the tracker missing a piece.
      **Stop:** the three lines are no bigger than the header (the Driver Station ignored the
      nesting) → note it, and try `<h1>` in `Display.huge`.
- [ ] **Unassigned gamepad warning (`belding-plus`, 7 Nov):** INIT Basic Drive with only
      gamepad 1 assigned. **Good:** under the alliance, `Gamepad 2 (operator) not assigned` big and
      amber with `operator: hold Options, press Circle`, and the light flashing white; Options +
      Circle on the second pad clears both, and the light shows the alliance. Same in an Auto's
      INIT, in its list. The log has `gamepad 2 (operator): assigned (SONY_PS4)` and, at its first
      press, `first input`. **Stop:** the warning shows with both pads assigned (the SDK reports
      no gamepad ID before a pad's first input) → note when it clears, and tell a mentor.
- [ ] **Autos keep the park (`belding-plus`: `BuiltAuto.parkAfterVolley` on):** R-NoCross and
      R-Solo once each, loaded, HIVE upright. **Good:** R-NoCross parks
      every run; when its second volley ends late the Match page and the log say `… no time to
      park after it, parking now` and it parks from RIGHT_LAUNCH_CATCH; R-Solo still fires its
      third volley and parks by about 29 s; a full volley still ends at its 4th counted shot.
      **Stop:** R-Solo parks instead of its third volley every run → note the time it gave and tell a
      mentor (Panels `BuiltAuto.parkAfterVolley` off is the old rule).
- [ ] **GARDEN gather, no standing for the stick (16 Nov; never run):** button, GARDEN loaded, a
      spotter on the stick, 6 gathers, then R-NoCross once. On 14 Nov the robot stood 0.20 to
      0.71 s after its line-up waiting for the stick (34 gathers). Now the stick starts down 16 in
      from the line-up (`lowerWithinIn`, 10 before) and the drive in no longer waits for it
      (`stickDownMs` 0, 700 before): it finishes its swing while the robot drives in. **Good:**
      `PREP` in the log is under 0.1 s (up to 0.5 s where the robot was still turning: the stick
      now waits until it turns slower than 30°/s, `lowerMaxTurnDegPerSec`), the stick never comes
      down while the robot is swinging round, it is down and still before it reaches the pieces,
      and no stick on a wall. The button's line-up is also 3 in further back (`lineUpBackIn` 6). The tight case is R-NoCross, whose card lowers the stick as the drive
      in starts: by the 14 Nov logs it lands with 0 to 0.2 s to spare. **20 Nov, 20245, button:**
      at `stickDownMs` 0 the stick sometimes came down on top of the audience wall; 250 fixed it
      and is the default now. Still to run: 19429's button gathers and R-NoCross at 250. **Stop:** the stick lands on
      the pieces or is still swinging at the wall → `stickDownMs` 300; or keep 0 and start further
      back still: `lineUpBackIn` 9 for the button, and GARDEN_STAGING 3 in further from the
      GARDEN in the Visualizer for the Autos (about 0.04 s more per inch). The stick touches the
      side wall on the line-up → `lowerWithinIn` back to 10.
- [ ] **Corner gather on the button, all four corners (20 Nov; never run with the button):** the
      driver's Triangle now gathers in the nearest field corner, facing whichever of its two walls
      the robot already faces more nearly (`GardenGather.button.anyCorner`; driven by hand on 20 Nov
      it worked). The way in facing the side wall, along the audience wall, and the two corners
      without a GARDEN are red's measured gather mirrored, not measured. Pose referenced, Panels
      open, phone filming from above: at the red GARDEN, 3 gathers coming in facing the audience
      wall and 3 facing the side wall; then 2 each way in one of the other corners with 4 POLLEN
      placed along a wall. **Good:** the log names the way in (`red GARDEN corner, facing the side
      wall …`), the line-up turns at most 45°, the side rides the wall it runs along, the stick
      never lands on a wall, and the facing-the-side-wall gathers bring in as many as the other way.
      **Stop:** a mirrored way in sits off its wall or the stick hits the corner → write down the
      pose at the wall; Panels `GardenGather.button.anyCorner` off is the old button (the GARDEN only,
      facing the audience wall).
- [ ] **The Auto's GARDEN gather presses onto the wall first (20 Nov; branch
      `claude/eloquent-albattani-krkecl`, not yet run):** the `GardenGather` card now strafes from where its
      drive left it onto the side wall (`lineUp.autoPressOntoWall`, `lineUpOutIn` -2), then drives in along
      where that stopped, as the button does. Sloth Load the branch, run an Auto with a GARDEN gather (red,
      from GARDEN_STAGING) 3 times. **Good:** the log shows the square-up ("Auto, squaring up"), then "line-up
      stalled … against the wall" or the line-up's end, the drive in "path done", and the POLLEN come in.
      Time the card: `AutoRegistration.GARDEN_GATHER_TYPICAL_S` is 3.0 s, and the 850 ms dwell, `inSpeed`
      0.5 and the strafe may make it longer, which the endgame guard needs to know. **Stop:** the stick
      catches the wall on the strafe, or the card times out → `lineUp.autoPressOntoWall` off in Panels.
- [ ] **Corner gather on 19429 (20 Nov; tuned on 20245 only):** 20245's settings of 20 Nov are the
      defaults: the line-up 14 in further back and 2 in into the wall (`lineUpOutIn` -2), going on when it
      stalls there (`lineUp.stallWithinIn`); the drive in along where the line-up stopped
      (`lineUp.followWallLine`), ending 3 in short of the wall (`extraDepthIn` -3) at `inSpeed` 0.5 so
      Pedro brakes; 4° into the corner; a dwell of 850 ms at 0.25 push. On 20245 the mentor called it
      perfect. The Auto's GARDEN card shares them (no line-up). On 19429: 3 gathers each way in at the red
      GARDEN, one after a lap, and one Auto with a GARDEN gather. **Good:** the drive in's log line says
      "path done", no bounce off the wall, `Lane/Count` rises. **Stop:** it rams ("stalled") → `extraDepthIn`
      -4; it stops short of the corner → `dwellPushPower` 0.35. 19429 went short at -3; it has its own
      `extraDepthIn` -1 in `RobotDifferences` (21 Nov), found in Panels, not yet from a log.
- [ ] **GARDEN gather, onto the side wall sooner (idea, 16 Nov; nothing changed):** the line-up
      spot is 3 in out from the side wall (`lineUpOutIn`, room to turn in the corner) and the drive
      in curves over to it. Try `lineUpOutIn` 1 with the button, 4 gathers. **Good:** the side is
      on the wall earlier in the film, and the volley count is no lower. **Stop:** the robot
      scrubs the wall on its line-up turn, or the stick touches it → back to 3.
- [ ] **GARDEN gather, the back out that stalls short (14 Nov; `backStallMs` never run):** twice on
      19429, with the battery sagging to 8 V, the back out stopped about an inch short of where it
      started and crept there, 2.4 and 3.7 s against 0.6 to 0.85 s for the other 30. The back out
      now ends once it has got no closer for 0.4 s (`GardenGather.back.backStallMs`). **Good:** no
      `Last GARDEN gather` line has `back` over 1.5 s, and a stalled one logs `back out stalled N
      in short, done there` with N under 2. **Stop:** it ends the back out early with the robot
      still well inside the corner → `backStallMs` 0 (off) and write down N.
- [ ] **GARDEN gather, the smaller knobs (same setup):** one at a time, 3 gathers each, counting
      the volley. (a) `stallMs` 150: the robot has stopped 0.27 to 0.47 s before the stall rule
      ends the drive in; 150 should take 0.1 s off. **Stop:** a drive in that ends before the robot
      reaches the corner → back to 250. (b) `dwellMs` 200 and 800 against 400. Keep the shortest
      that loses no piece; a physics model (13 Nov, unmeasured) says not to go under about 0.5 s
      at the wall in all. (c) With 3 pieces already in the lane, gather a fourth and watch the back
      out (mentor, 13 Nov: the fourth ball rides on the floor at the front and backing up can let
      it squirt out). **Stop:** the fourth is left on the floor twice → tell a mentor.
- [ ] **GARDEN gather, `driveIn.laneTowardWallIn` 5 (19 Nov; never run):** button, GARDEN loaded,
      19429 then 20245, 3 gathers on **each** alliance's GARDEN, phone filming from above. 19 Nov on
      19429 at 3: blue's side reached the side wall and gathered, red's sat 1 to 2 in off it and
      missed. **Good:** the side is on the side wall on both alliances before the nose reaches the
      audience wall, and `in` in the `Last GARDEN gather` line is no longer than at 3. **Stop:** the
      drive in is slow (as at 6 on 18 Nov) → 4. If red still sits off the wall at 5, the gap is not
      this knob: note the pose at the wall (red against blue) for a localization check.
- [ ] **GARDEN gather, a harder drive in, and the two-step (18 Nov; never run):** button, GARDEN
      loaded, 19429, Panels open. First check Panels: GardenGather and FlowerPicker now show their
      knobs in folders (`driveIn`, `twoStep`, `dwell`, `steering`, `back`, …), and an edit in a
      folder changes the robot (`Tunable/GardenGather.driveIn.…`
      is in the log). Then 3 gathers each: (a) the new defaults, `driveIn.extraDepthIn` 10 and
      `driveIn.laneTowardWallIn` 3 (19429 ran 4 and 6 from Panels that day, and the drive in was
      slow); (b) `twoStep.twoStepIn` on: a path over to the side wall, then a full-power push down
      it (`twoStep.pushPower` 1.0) with nothing braking before the wall. **Good:** `in` in the
      `Last GARDEN gather` line is shorter than (a)'s, the event line says `over to the wall … then
      pushing in`, and the side stays on the wall. **Stop:** it hits the wall hard enough to bounce,
      the stick catches, or pieces are knocked out → `twoStep.pushPower` 0.7, then off. Write the
      winner back as the default. [PR: branch `claude/focused-albattani-ag687y`]
- [ ] **GARDEN gather, curve to the wall and quick out (`garden-gather` branch):** at the
      practice field with the TeleOp gather button (driver Triangle, held), GARDEN loaded, phone
      filming from above; then once in R-NoCross. It lines up where it always has, stick going
      down; then the drive in curves (`GardenGather.driveIn.curveToWall`): over to the side wall first,
      then down it into the corner. It aims 1 in past the gather pose (`extraDepthIn` 1), dwells 0.4 s (`dwellMs` 400), backs out at 0.8 speed
      (`backSpeed`) with the stick still down, straight up the side wall first then over
      (`curveBackOut`; off is a straight diagonal back), stops, and raises the stick 0.25 s later
      (`raiseAfterStopMs`). Try `curveBackOut` on and off and note which leaves fewer balls behind. Then `useStick` off: the
      stick stays in and the intake drives 2 in deeper (`noStickExtraDepthIn`) all the way to
      the wall; compare balls in with the stick and without. **Good:** the side is on the side wall well before the nose reaches
      the audience wall, the nose gets there (`GARDEN gather: IN … reached` or under 1 in short,
      against 2–4.5 in on 6 Nov), more balls come in than 6 Nov's 0–1, and the stick never drags
      a ball back out. **Stop:** it scrubs into the side wall and stalls early, or the curve
      swings wide → `curveToWall` off (the old diagonal); `raiseAfterStopMs` -1 is the old stick
      rule. Tune `dwellMs`, `backSpeed`, `laneTowardWallIn` (2.0 since 14 Nov, with `inSpeed` 1.0, `dwellPushPower` 1.0, `lowerWithinIn` 10, `lowerWithinDeg` 30, `settledInPerSec` 20, `maxRangeIn` 45: 8 gathers on 19429, 4 in a row) and `extraDepthIn` in Panels and
      write down what works.
- [ ] **Film the Auto volleys (decides `LaunchCards.launchAllFeedS`, 3.5):** 5–6 LaunchAlls,
      phone slow-motion side-on, the launcher and the Driver Station both in frame. Per volley,
      write down each ball's time after the gate opens and the launcher's counted shots.
      **Good:** no ball after 3.0 s in any → `launchAllFeedS` 3.05 in Panels, and the volleys
      where the count fell short show which way the shot counter misses.
- [ ] **Superseded 11 Nov by "Square launch spots" in §7f (Square no longer drives to
      the starts or the corners).** **Square's start and corner spots aim the turret (`belding-plus`, 7 Nov):** turret locked,
      hold Square from near each start and each corner (RIGHT_START now 89°, LEFT_START 267°,
      RIGHT_LAUNCH_CORNER 61° instead of 54°, LEFT_LAUNCH_CORNER 303° as before) and fire.
      **Good:** the volley goes into the CELL, centred; RIGHT_LAUNCH_CORNER scores at least as well
      as on 4 Nov. **Stop:** shots land to one side → note which side and by how much.
- [ ] **Square from past the centre line (`LaunchSpotDrive.maxPastCentreIn`, 24):** on 6 Nov the
      drivers pressed Square with the robot 3–4 in past the line and it refused every time. Hold
      Square with the robot about 6 in, then about 20 in, past the line, on red and on blue.
      **Good:** it drives to the wall spot in line with the robot (past the line that is the FAR one,
      `…_WALL_FAR`) and fires; past 24 in the Match page
      says `REFUSED: robot N in past the centre line`. **Stop:** the path heads into the HIVE frame
      or a partner → let go, note where, and lower `maxPastCentreIn` (0 is the old rule).
- [ ] **Circle is camera first (`ChassisAimTarget`), the fallback when the pose is off:** Basic
      Drive, loaded, turret locked. (a) Pose good: from 45–55 in, roughly facing our UP CELL, the
      driver holds Circle and the operator holds FIRE at once. (b) Pose off: knock the pose out
      (push the robot 6 in sideways by hand, no reset), D-pad up facing the opponents' wall, then
      the same. (c) Start Circle facing 60° away from the CELL, so it begins on odometry. (d)
      Mid-turn, a hand over the Limelight for about half a second. **Good:** the Robot page reads
      `Chassis aim: camera, RIGHT CELL` (or LEFT), the robot settles on the CELL in both (a) and
      (b), and FIRE waits until it is on target and then feeds (`fire: … WAITING: an enforced gate
      is closed` first, then the volley); in (c) it switches to `camera` once the CELL comes into
      view; in (d) it reads `camera not on it, aiming where it last saw it` and carries on, not
      swinging away. Every press logs `chassis aim: camera locked … ; odometry … (gap …)`: (b)'s gap
      shows the pose error the camera ignored. **Stop:** it turns to the DOWN CELL (the HIVE's state
      is wrong: note the log's HIVE line), it overshoots and hunts (lower `chassisAimKp`), or FIRE
      never opens on target (`TeleOpConfig.chassisAimGate` off, note the aim error the log shows).
- [ ] **Circle's anchor: where the camera last saw the CELL** (branch `claude/pensive-mccarthy-0wkna8`):
      Basic Drive, loaded, turret locked, filmed. (a) Circle on the CELL, then a hand over the
      Limelight and strafe about 24 in with Circle still held. (b) Circle on the CELL, let go, turn
      90° away and drive about 3 ft, then hold Circle again with the Limelight covered. (c) As (b)
      with the pose knocked out first (push the robot 6 in sideways by hand, no reset). (d) As (b),
      but a corner reset before the second press. **Good:** in (a) to (c) the Robot page reads
      `camera not on it, aiming where it last saw it`, the robot turns onto the CELL, and FIRE's
      shots land (the log's `shot N: … (where the camera last saw …)` range within about 2 in of a
      tape); in (d) it reads `odometry` or holds its heading, never the old point. **Stop:** it
      settles more than about 3° off the CELL in (a) → the frame behind the anchor was too old for
      a turning robot: note the turn rate, and lower `ChassisAimTarget.ANCHOR_MAX_AGE_MS` (120, a
      number).
- [ ] **Every press is a drift sample:** across a practice, pull the logs and read each press's
      `launch spot: drift at …` line (camera − odometry at a known spot, settled, never applied) and
      its `CELL N in` range beside the shots it fired. **Good:** gaps stay under
      `TeleOpConfig.spotDriftWarnIn` (3, not measured) through a 2-minute TeleOp, and the range/shot
      pairs add to #80's speed-vs-distance table beside §7d's rows. **Stop:** gaps grow
      over the match → the evidence for TeleOp relocalization; set `spotDriftWarnIn` from what the
      drivers can act on. [PR #423]
- [ ] **Pinpoint recovery mid-match** (Basic Drive, robot on the field, HIVE upright). Drive, then
      unplug the Pinpoint's I2C cable at the hub for about 3 s and plug it back. **Good:** within
      0.5 s the Match page says "Pinpoint not reading"; plugged back, "Pinpoint back"; face our HIVE
      a second → the alert goes, Square works, and the log has `Pinpoint trusted again: confirmed by
      the camera`. Repeat, confirming with a corner reset instead. **Stop:** never "Pinpoint back"
      after replugging (the bus does not come back without a restart: note it), or trusted with the
      robot visibly off → raise `PinpointTrust.AGREE_FRAMES`. Read `/Drift/GapXIn`/`GapYIn` across
      the episode (logged while not trusted now) for the full-position check. [PR #503]
- [ ] **Pinpoint drift over a 2-minute TeleOp:** Basic Drive for 2 min of ordinary driving, HIVE in
      view now and then; read `/Drift/GapXIn` and `/Drift/GapYIn` (camera minus odometry) over the
      run. **Good:** the gap stays within a few inches, so a launch spot driven late in the match is
      where it was early. **Stop:** it grows steadily → the launch-spot button needs a camera reset
      first; note the rate. [PR #423]

## 6. Tape measure and level (camera geometry)

- [ ] **Tag size.** Pipeline 82.55 mm (*19429: was 165.1, the DECODE size, set to 82.55 on 6 Nov;
      20245: read 82.55 on 31 Oct, check it again*); measure a
      printed tag's black square: 3.25 in. [S4 §1]
- [ ] **Lens to tag 35 from the Red audience start.** **Good:** tape and camera within 1 in (CAD
      ~57.5 in). [S4 §2]
- [ ] **Red HIVE rocker:** axle 70.75 in from the audience wall and 43.95 in up; raised row 49.76 in
      up and 57.65 in out; raised CELL 30°. **Good:** each within 0.5 in / 1°. [S4 §2]
- [ ] **Camera tilt:** 45° on the Limelight's face; tower plumb? [S4 §3]
- [ ] **Camera run at 22, 35, 50 in** from the HIVE, 10 s still each; pull the log. [S4 §4]
- [ ] **Robot width across the side plates** (16.0 in assumed) and both **reset corners** taped
      (`ResetPositions` uses 8.1 in wall-to-centre). [PR #262]
- [ ] **20245: intake reach past the frame, at the mouth**: from the frame's front face to where
      the intake first touches a POLLEN, in, and whether a GARDEN gather takes the corner POLLEN (count
      the next volley by `read_logs.py volleys`). 20245 has the passive FLOWER stick, so the simulator
      models its gather by what the mouth touches, with `RobotDesign.intakeReachIn` 0 (a placeholder):
      it takes 3, the square frame turned 15° into the corner leaving the mouth about 0.9 in short of
      the corner piece. 19429's roller stick fills the lane in the simulator (mentor, 19 Nov). **Good:**
      a number to set there; about 1 in would reach the corner piece. It moves every intake contact in
      the simulator, so change it in its own PR. [PR #644]

## 7. 19429, if it is at the lab

- [ ] **R-Solo on 19429:** its first path from the Red audience marks. 19429's drivetrain is tuned
      on its own values (6 Nov, PR #480: Check: Drive Motors, Pod Offsets, Foresight, Tests all
      passed), so a path that drifts on 19429 but not on 20245 is now worth reporting. [#175]
- [ ] **Anything 19429 does differently** from 20245 (a motor turning the other way, a servo
      position, the intake sign): one line in `RobotDifferences.java` under `TEAM_19429`, with what
      was seen and the date, then Sloth Load. Never edit the shared value: that changes 20245 too.
- [ ] **19429's FLOWER stick against 20245's, before judging any pick routine on 19429.** On the
      Belding stream 20245's stick empties a FLOWER within about 0.8 s of touching it (Q-27, red
      WALL); 19429's left the POLLEN standing in the FLOWER for a whole 2 s dwell (Q-13, blue WALL),
      and they came out only as it backed off. Same code, same numbers, and
      `FlowerStickSubsystem.EXTENDED` 0.0 was measured on 20245 only (19429's stick was rewired to
      Control Hub servo port 0 on 6 Nov and has no line in `RobotDifferences`). Both robots side by
      side at one loaded FLOWER, stick down, pushed in by hand: measure the stick tip's height off
      the tile, how far it reaches ahead of the frame, and where on the FLOWER's foot it lands;
      film both from the side. **Good:** the same within a quarter inch, and 19429's FLOWER empties
      while pressed. **Stop:** it differs → fix the stick or its down position on 19429 (a servo
      position that differs is one `RobotDifferences` line, with the date), then do §4's 20 picks.
      A stick that does not trip the FLOWER is not fixed by `MULTI_PUSH`, only hidden by it.
- [ ] **19429's stick: right-arm gap and the WALL FLOWER stall** (14 Nov; the "FLOWER stick: GARDEN
      polish" chat owns the guides and has the new right-arm guide's print files). On both robots:
      the arms' inner faces apart at the axle; each side's parts on the shaft, arm to wall, in order
      with lengths; with the stick down on a flat tile, the ramp nose's bottom edge off the tile
      (20245 about 0.44 in) and the little wheels' diameter with O-ring (1.45 in by the CAD). On
      19429 a side photo at a WALL FLOWER stall: what touches the FLOWER first. **Good:** 19429's
      nose as high as 20245's. **Stop:** under about 0.38 in → O-rings, the ramp's hex flat or the arm
      angle, before any pick tuning. Notes and photos: `cad/stick-shoulder-19429/`.
- [ ] **19429's Limelight pipeline:** Marker Size set to 82.55 mm on 6 Nov (it was 165.1, the
      DECODE size; Full 3D Targeting Yes). Later that night 20245's pipeline file was uploaded to
      19429's slot 0: identical but for its name. Power-cycle and check it held, then run the 5-minute start check and Vision: HIVE & Pose. [handoff] step 6
      First reading after the fix (6 Nov, red audience start, placement not confirmed): camera
      (54.3, 6.1) vs start (55.6, 7.7). y 1.6 in short means ranges ~3 % long, like 20245's; x 54.3 is
      the old wheels-on-seam placement. Tape lens to tag 35, measure a printed tag, level the camera.
- [ ] **Drive check table** (10 steps), on both robots. [#62]
- [ ] **19429's left launcher axle has play** (seen 6 Nov; a spacer is missing; not planned to
      fix, recorded in case launching gets worse). If 19429 shows launch trouble that does not
      follow the gains, check this axle before tuning: the left flywheel's speed ripple or
      dips larger or slower to recover than the right's, shots pulling to one side, or the left
      motor's current running high. Compare with 20245's left and right traces (4 Nov).
      **Good:** the left and right wheels recover alike after a shot. **Stop:** they do not →
      fit the spacer, then re-check the launcher in §7c.

## 7b2. Drive: Super FLOWER Pick (20245, the SCORING FLOWER, HIVE upright, phone filming the intake and the CELL)

**Shelved 18 Nov 2026 (mentor): the turret cannot shoot back over the robot.** The Utility and its R1
binding are gone; `opmodes/FlowerVolley` is kept. Nothing below can run until both come back.

One driver button, on trial: fire the load from against the SCORING FLOWER, then pick it
(`opmodes/FlowerVolley`; mentor's idea, 17 Nov 2026). Never run on a robot.

- [ ] **Drive: Super FLOWER Pick** (Utility menu: Basic Drive plus the driver's R1, held; 20245,
      whose turret aims. On a locked turret R1 does nothing). Four in the robot, four in the SCORING
      FLOWER, the robot anywhere within about 5 ft of it. Hold R1. **Good:** it drives to the
      line-up, the launcher spins up, the turret swings back to the LEFT CELL whatever the operator's
      turret mode, it pushes in, fires the four it came with, shuts the gate, stays until the lane is
      full (at most 1.5 s more) and backs out holding the FLOWER's four. Letting go before it touches
      the FLOWER stops it; letting go after backs it out; a stick stops it. **Stop:** the balls hit
      the robot or go wide of the CELL → the turret cannot shoot back over the robot (the dead
      zone): nothing else here can work until that is fixed. The Robot page says "Super FLOWER pick
      refused" → it says why (no pose, not the SCORING FLOWER). **Film it:** is a FLOWER piece
      inside the robot before the first ball has left (G407, four at most)? Panels →
      `TeleOpConfig.superPickFiresFlowerLoad` on fires all eight instead.

## 7c. Launcher tuning (Bench: Launch Test, Panels open)

Before §7d: the distance rows are only worth having once the wheels hold their speed. The match logs
of 3 and 4 Nov (20245) showed the hub's default velocity loop, untuned: after each ball's dip the
wheels overshot to ~1680 of 1595 (±60) and "at speed" stayed false ~0.7 s; a spin-up from rest took
0.9–2.1 s, slowest on a tired battery. The knobs are `LauncherSubsystem.velocityP/I/D/F` (NaN = the
hub's own), `compensateF` and `nominalVolts`; `FeederSubsystem.compensateVoltage` and `nominalVolts`.
The Launch Test page shows the gains, every spin-up's and dip's timing and the step test; Panels has
the same as `launcher_*` (graph `left_tps`, `right_tps`, `launch_target_tps`); the match log has
each spin-up and dip as an event, with the battery. Panels edits are lost on restart: write each
number down as it is found. [branch claude/launcher-tuning]

Tuned on 20245, 4 Nov 2026 (two Launch Test logs, 18:45 and 18:51): the hub's own gains (P 10 I 3 D
0 F 0, the same in every OpMode) spun up in about 0.98 s from rest; F 14.65, P 40, I 0.2, D 0 in
about 0.57 s, and after a shot's dip back at speed in a median 0.22 s (overshoot a median 25, 1
of 94 dips over 60). Those are `LauncherSubsystem`'s defaults now. On 19429, check the same way;
a difference is one `RobotDifferences` line.

- [ ] **FIRE from the 1000 idle** (Basic Drive; `TeleOpConfig.launcherIdleTicksPerSecond` 1000 since
      13 Nov). Spin up, toggle the launcher off so it idles at 1000, then press and hold the operator's
      FIRE. **Good:** the gate opens only once the wheels reach the launch speed (the `fire:` line, then
      `/Launcher/AtSpeed`), and the volley scores like one from rest. **Stop:** balls leave slow → note
      the log; set the idle back to 0 in Panels. Also note the battery at the end of a match with it.
      Before this PR, FIRE from an idle at speed could open the gate at the idle speed.
- [ ] **Measure the wheels**, for the surface speeds (in/s, the one unit that compares the feeder
      with the flywheels; ticks/s do not). Calipers on the flywheel: `LauncherSubsystem.wheelDiameterInches`.
      The flywheel module's belt (count teeth, or mark the wheel and turn the motor once by hand):
      `LauncherSubsystem.beltRatio`, wheel revolutions per motor revolution. Calipers on the feeder
      wheel: `FeederSubsystem.wheelDiameterInches` (a placeholder now, 48 mm from `doc/transfer.md`),
      and check the feeder wheel is on the motor shaft (ratio 1). Read both motor labels: flywheels
      believed goBILDA 5203 6000 rpm (28 ticks/rev), feeder 5203 435 rpm (384.5). **Good:** the
      Launch Test page shows both surface speeds instead of "unknown". **Stop:** write the numbers
      back with the robot and date.
- [ ] **Shot count only with the gate open and both wheels down.** A dip now counts only if it
      starts while the gate is open and both wheels are 70 under target (4 Nov log: the intake's
      NECTAR stalls, and the wheels wobbling while a ball pressed on them, counted as shots). Fire 3 volleys of 4, letting go
      of FIRE the moment the last ball passes the plate, then intake 4 NECTAR with the launcher
      spinning. **Good:** 12 shots counted for 12 fired, none while intaking. **Stop:** if a last
      ball goes uncounted, the gate shut before it reached the wheels: note how long after letting
      go its dip came (log), for a grace after the gate shuts.
- [ ] **Is the shot count good enough to end a feed?** `LaunchAll` already ends its feed at the 4th
      counted shot (3.5 s backstop). At Belding (7 Nov) the count read 47 of the 64 preloaded pieces
      in the first Auto volleys, so most volleys ran to the backstop. Since 8 Nov the detector uses
      both wheels' average, a duration window (a piece's dip is 35–180 ms) and a smaller count
      depth (55); replayed over the Belding logs it finds 59 of 64, and never more than 4 in a
      preloaded volley (#505). **Checked against the Belding video (9 Nov):** 18 volleys counted by
      hand, replayed through master's detector (`ShotCountVideoTest`, opt-in): none counted more than
      left the robot, including three with nothing fed (0 counted in 10.6 s of open gate); 11 of
      the 14 clearly counted volleys exact (the Belding rule: 9), the other three one short (two in
      TeleOp, where the loop is slower). Since 10 Nov the level seeds only once the wheels hold the
      target (it seeded on wheels still falling onto a new one): 12 of 14, none over, and a third
      less noise with the gate shut. The two left are a 36 ticks/s dip, no deeper than the noise,
      and a ball with no dip at all: only the throat beam counts those. Where the video has ball times (Q-7), each count comes
      0.13–0.17 s after its ball, never before. So `launchAllEndAfterShots` stays 4. What is left,
      on the robot with the new detector running live, after the PIDF and feed pace below: fire 10
      volleys of 4 the way an Auto does (`LaunchAll`), and 5 TeleOp Square volleys, with a phone
      filming the launcher. Per volley, write down the count (Launch Test page, or `/Launcher/Shots`
      in the log) and the balls that left (video). A count too low only keeps the 3.5 s feed, a
      count too high ends a feed with a ball still in the robot. **Good:** no volley counted more
      than left, and at least 9 of 10 Auto volleys counted exactly 4; the TeleOp ones are only
      for a Square that ends on the count, which waits for the throat beam (#505). **Stop:** any
      over-count: note the volley's time and set `LaunchCards.launchAllEndAfterShots` to 0 (fixed
      feed) until the throat beam (#505, #213) counts pieces out. Also note any volley with 5 in the
      robot (Q-7's 2nd had 5): it ends at the 4th count with the 5th still in. Read off the same
      volleys how long the gate's opening took to the first dip: a long one means the first ball is
      not waiting at the wheels. [#433, #505; mentor, 8 Nov 2026; video replay #515]
- [ ] **Feed pace**, once the PIDF is set: the aim is each ball arriving after the wheels have
      recovered from the one before. Bench: Launch Test, `FeederSubsystem.feedPower` 0.6, then 0.8,
      then 1.0, 12 balls each (three volleys of 4), fresh battery, from one launch spot. Each dip's
      log event and `/Launcher/ShotGapMs` beside `/Launcher/RecoverMs` give the gap between
      consecutive shots and the recovery after each (Panels: `launcher_shot_gap_ms`,
      `launcher_recover_ms`). **Good:** the power at which every gap is longer than the recovery
      before it, with the best hits per 12 (count on video). Too slow: balls hit the turret walls
      (0.5 on 1 Nov). **Stop:** per power, record hits / 12, mean gap, mean recover and the battery;
      write the winner into `feedPower` with the robot and date.
- [ ] **On a tired battery** (≤ 11.5 V resting): run the step test 5 times with `compensateF` off,
      then 5 with it on (`nominalVolts` = the battery F was tuned at, rounded). **Good:** with it on,
      the mean matches the fresh-battery one within ~0.1 s. **Stop:** record both means and the
      battery; leave it off if it does not help.
- [ ] **Feeder compensation**, from match logs, not by eye. Pull the logs of a fresh-battery run
      (≥ 12.5 V) and a tired one (≤ 11 V), three volleys each, `FeederSubsystem.compensateVoltage`
      off: per volley, the feeder's ticks/s (`/Feeder/TicksPerSecond`) and the shot spacing (time
      between `/Launcher/Shots` steps). Then the same with it on and `feedPower` 0.88 (`nominalVolts`
      12.0: 1.0 leaves it no headroom on a tired battery; 0.88 holds down to 10.6 V). **Good:** with
      it on, the feeder's speed and the shot spacing at the tired battery match the fresh one within
      ~5%, and shots still leave cleanly (0.5 was too slow on 1 Nov: balls hit the turret walls).
      **Stop:** record both runs' numbers; write `compensateVoltage` and `feedPower` back only if
      it helped. [4 Nov 00:48 log: 2260 → 2060 ticks/s as the battery fell 10.83 → 10.61 V]

## 7d. Launcher distances (Basic Drive, HIVE upright)

After §7c: the rows are only worth having once the wheels hold their speed. One
`LauncherSubsystem.targetTicksPerSecond` (1595) serves every distance today. On 4 Nov it went
4/4 from RIGHT_START and long from LEFT_LAUNCH_OFFSET on the third volley. Four rows fill
`LaunchSpeedTable` (#80). Measure them on the right side; the left spots are the same distances on
the other side of the field.

**The loop, at each spot:** with `LaunchSpeedTable.enabled` off (the Robot page says
"Speed table: OFF"), hold Square. The robot drives to the wall launch spot in line with it and fires while Square is held.
On a miss, the operator trims the speed up or down, then Square again, until it scores. The Match
page's "Last launch spot" line shows the spot's CELL range and the launch speed: write both down.
Take the middle of the speeds that score 4/4, not the first one that does. The trim carries from
press to press.

Measured 4 Nov 2026 (20245, tuned PIDF) and in `LaunchSpeedTable` with the table on: 42.4 in → 1520,
45.7 → 1560, 53.9 → 1600, 58.8 → 1635 (ranges from the turret axis since 5 Nov; first written from
the wheels' centre as 39, 42.3, 50.5, 55.4). What is left here is the right corner's heading.

- [ ] **Superseded 11 Nov: Square no longer drives to the corners (§7f).** **`RIGHT_LAUNCH_CORNER` faces about 5° right of the CELL.** At Belding (turret locked
      forward) the aim error held at +4.5° there on red (Q-7) and +5.5° on blue (Q-31), against 0°
      at `LEFT_LAUNCH_CORNER` (M-7, red) and within ±2.5° at the other spots; 3 and 2 shots counted
      of 4. Set its heading to about 59° in the Visualizer (Save team spots), then hold Square there
      once. **Good:** aim error under 2° on the Match page and 3–4 of 4 in. [mentor, 5 Nov]

- [ ] **A row at 36 in, for `LEFT_LAUNCH_CLOSE` and `RIGHT_LAUNCH_CLOSE`, and for the close
      row (34.6 to 35.7 in; Square drives there from the middle tile row, so the loop below can be run from it).** The table's rows start
      at 42.4 in, so both CLOSE spots fire at an extrapolated speed (1459–1480 ticks/s in the
      Belding logs). The four measured rows fit one arc, which puts 36 in at about 1513 ticks/s
      (1458 to 1573 allowed by the fit). Run the loop above at `RIGHT_LAUNCH_CLOSE` and write the
      speed into `LaunchSpeedTable` row 5. **Good:** 4 of 4 stay in the CELL at the row's speed.
      [`doc/one-robot-two-tips.md`]

- [ ] **Count the balls that stay in, spot by spot** (the number `doc/three-tips.md` and
      `doc/one-robot-two-tips.md` both turn on). Five volleys of four from `RIGHT_START`,
      `LEFT_LAUNCH_CATCH` and `LEFT_LAUNCH_OFFSET`, filmed from the side, fed by a FLOWER pick as in an
      Auto, not by hand. Write down pieces that stay in of 20 at each. The simulator has 0.82, 0.92 and
      0.89, inferred from TIPs, never counted. **Good:** 19 or 20 of 20 at the LEFT spots (0.98 gives two
      TIPs in 3 runs of 4 beside a partner that only leaves its preloads; 0.92 gives half).
      [mentor session, 11 Nov]

- [ ] **The road under the HIVE** (`doc/three-tips.md`, R-Pair-Garden's exit; draft PR #531): drive
      `RIGHT_PAIR_CATCH` (61, 13) to `LEFT_PARK` under the HIVE, slowly first, then at 50 in/s, 5 times.
      The curve leaves 3.3 in between the frame and our foot bar, and the same to the centre line.
      **Pass:** no touch and no crossing in 5 of 5. Write down the closest it came on each side.
- [ ] **Two robots at the right end, both shooting** (`doc/three-tips.md`, the pair's last two
      volleys): one on `RIGHT_PAIR_CATCH` (61, 13, 96.2), one on `RIGHT_LAUNCH_OFFSET`. 5 volleys of
      4 from each, one after the other. Count the ones that stay in. **Good:** the two frames do
      not touch, and the OFFSET spot (21 degrees off square) is within 1 ball in 20 of the other.
- [ ] **R's rescue road** (`doc/three-tips.md`, "The blend"): with a robot standing on
      `LEFT_LAUNCH_OFFSET`, drive `RIGHT_PAIR_CATCH` under the HIVE to (58.5, 100.5), sideways to
      (26, 100.5), and into `LEFT_PARK`. It has 2 in on each side between the foot bar's end and
      that robot. **Pass:** no touch in 5 of 5, slowly first.
- [ ] **A row of 4 loose POLLEN, picked up at a creep** (Basic Drive, intake on; `doc/three-tips.md`'s
      partner row). Lay 4 POLLEN touching in a line on open tiles and drive along the line at about
      6 in/s (a quarter stick), 10 times, then 10 times with the row against a wall as the GARDEN's is.
      Count the pieces in the lane each time and time first touch to full from the log
      (`read_logs.py picks`). The simulator's 1.3 to 2.6 s and 4 of 4 are a placeholder; the GARDEN
      gathers in practice took 0 to 2 (#508). **Good:** 4 of 4 in under 2.5 s, 9 times in 10.
      [mentor session, 11 Nov]

- [ ] **A shot while the robot rolls** (Basic Drive, only once the turret tracks a CELL while driving).
      Drive past `LEFT_LAUNCH_OFFSET` along the far wall at about 20 in/s and fire four on the way,
      5 passes each way, filmed. Count what stays in against the same spot standing still. The
      simulator's what-if assumes a moving shot stays in as often as a standing one once it is led for
      the robot's speed; unled, 1 in 4 of its runs made the second TIP. [mentor session, 11 Nov]

- [ ] **Camera range = pose range, from the turret axis** (since 5 Nov both are measured from the
      turret, and the table's rows were converted to match). Any Basic Drive with the camera on a
      CELL: graph `/Launch/CameraRangeIn` and `/Launch/PoseRangeIn` from the match log (an Auto
      writes both on its `launcher: speed table` line). **Good:** within about 1–2 in at the launch
      spots, and shots score as before at the same speeds. **Stop:** a steady gap of 3 in or more →
      the turret offset (`TurretSubsystem.turretForwardIn/LeftIn`, from the CAD, never taped)
      or the camera mount; tape the turret axis from the wheels' midpoint first.
- [ ] **Tape the turret's place on the robot (either robot; turret at home, tape measure).**
      `TurretSubsystem.turretLeftIn` went from −1.87 to −2.68 on 16 Nov from two CAD readings
      (the bearing 4.94 in from the right edge of the 15.236 in chassis; the CAD model's bearing
      axis at 2.67). From the outside of each side plate straight across to the centre of the
      turret ring: half the difference is how far right of the centre line it sits. From the
      front wheels' axle line back to the ring's centre, less half the wheelbase: how far behind.
      **Good:** 2.5 to 2.9 in right, 3.1 to 3.5 in behind. **Stop:** outside that → write both
      readings here; the number in `TurretSubsystem` changes to the tape's. Then, turret locked,
      Square to `RIGHT_START` and to `LEFT_WALL_HIVE` and count what stays in of 4 at each, as
      before the change: the aim moved by about 1° (0.8 in at the CELL). **Stop:** fewer stay in
      than before → note which side the misses fall.
- [ ] **The Autos' four launch spots, by hand first (Basic Drive, HIVE upright, a taped mark at
      each).** `AutoLaunchSpots`, drawn 16 Nov and never launched from: `LEFT_AUTO_HIVE`
      (61.4, 120.45, 268.9°), `LEFT_AUTO_OFFSET` (40.7, 121.1, 298.7°), `RIGHT_AUTO_HIVE`
      (59.2, 20.9, 96.1°), `RIGHT_AUTO_OFFSET` (32.1, 16.4, 61°); blue's are these turned half a
      turn. Tape each from the walls, place the robot on it at its heading, turret locked, and
      fire five volleys of four. Write the count that stays in of 20 at each, and the range the
      robot shows. **Good:** 18 or more of 20 from each HIVE spot (40 in, 2° and 6° off square) and
      from each OFFSET spot (44 in on the left, 52 in on the right, both about 29° off square), and nothing off the HIVE's lip from
      40 in. **Stop:** lip hits from a HIVE spot → note how many, and try 2 in further back; an
      OFFSET spot clearly worse than its HIVE spot → note which side the misses fall. That is the
      angle, and the spot moves back along its line (26° at 48 in on the left).
- [ ] **R-NoCross and L-NoCross on the Autos' launch spots (moved 16 Nov, not driven).**
      R-NoCross's two later volleys are from `RIGHT_AUTO_HIVE`; L-NoCross moves up to
      `LEFT_AUTO_HIVE` to wait for the TIP and fires its LEFT volleys there. When no TIP comes by
      its time (9 s), L-NoCross crosses under the HIVE to `RIGHT_AUTO_FIX_TIP` (55.3, 25.7, square,
      its back 18 in from the audience wall), fires its preloads at RIGHT, comes back under the
      HIVE to the SCORING FLOWER, picks, fires at LEFT from `LEFT_AUTO_HIVE` and parks. Run each
      alone, then L-NoCross with a robot left on `RIGHT_START` and no TIP. **Good:** L-NoCross
      stops about 2 in short of the robot on the start, touching neither it nor the HIVE on the
      way down or back; its four at RIGHT go in from 35 in; it parks by about 28 s alone. When it
      has to park early from the right end it goes back under the HIVE, not up the wall lane.
      **Stop:** pieces off the HIVE's lip from `RIGHT_AUTO_FIX_TIP` → count them (35 in is lip
      range) and try the launcher 25 to 50 slower; still driving at 30 s → write the time; it
      clips a HIVE leg on the park back under the HIVE → note where (that park path is new).
- [ ] **How close can we shoot? (Basic Drive, square on to the RIGHT CELL, tape from the wall.)**
      Five volleys of four each with the robot's back edge 18, 16 and 14 in from the audience
      wall (35, 37 and 39 in from the row), then with the launcher trimmed 25 and 50 slower at
      18 in. Write the count in of 20 and the count off the lip at each. `RIGHT_AUTO_FIX_TIP`
      stands at 18 in because nothing on the start can reach it there; this says what that costs.
- [ ] **R-Solo on the Autos' launch spots (moved 16 Nov, not driven).** Preloads from
      `RIGHT_START` as before; both LEFT volleys from `LEFT_AUTO_HIVE` and the volley back at the
      right end from `RIGHT_AUTO_HIVE`. The partner is expected in the corner, parked, not on
      `LEFT_START`: `LEFT_AUTO_HIVE` stands in front of that start. Run it alone. **Good:** each
      volley 3 or 4 of 4 in, nothing off the lip from 40 in, parked by about 27 s. **Stop:** a
      partner still on `LEFT_START` when R-Solo arrives → the two touch (the simulator has it at
      12.5 s); that match needs another Auto.
- [ ] **Two robots at one end.** One on `LEFT_AUTO_HIVE`, one on `LEFT_AUTO_OFFSET`, both at
      their headings. **Good:** about 2 in between the frames, HIVE's frame half an inch short of
      the centre line, OFFSET clear of the SCORING FLOWER, and each can fire without the other
      in its line. The same on the right. **Stop:** they touch, or one shoots across the other →
      note which and by how much.

## 7e. Blue alliance (HIVE and FLOWERs on the blue side)

Everything blue is red turned half a turn about the field centre, in code and unit-tested
(`LaunchSpotsTest.blueIsRedTurnedHalfATurn`, `NectarRejectTest`). Both robots ran on blue at
Belding (7 Nov): vision read BLUE in INIT and the Autos ran there. What follows is what those
matches did not show.

- [ ] **Corner reset on blue** (the reset corners are measured on red and turned): hold the reset
      button at a blue corner. **Good:** the pose on the Robot page matches the corner, field-centric
      forward is right.

## 7f. Drive practice from the Belding logs (Basic Drive, two drivers)

The cycle and press analysis of the 7 Nov match logs: `events/2026-11-belding` branch.

- [ ] **20245: turret aims from PLAY, driver's Circle fires, aim gate on** (branch
      `claude/relaxed-carson-rwuvvl`, 18 Nov). Basic Drive, HIVE upright: at PLAY the turret swings
      onto our CELL by itself (log: `turret at PLAY: AIM`). Driver holds Circle: it fires as the
      operator's FIRE does. Trim the aim 5° off and fire: it waits (log line `aim BLOCK`) until the
      trim is reset. Cover the camera and fire from a known pose: odometry's aim still lets it go.
      (Superseded by step 7 of tonight's list: the turret mode is now the driver's R1, and in LOCKED
      Circle turns onto the CELL and then fires.)
- [ ] **Square, Cross and Triangle pressed while still steering** (`FreshSticks`, #495). In 50 of
      51 Square presses cancelled within 0.1 s at Belding, the stick was already moving when Square
      went down. A stick already off centre at the press is now ignored until it centres once, or
      for `TeleOpConfig.staleStickCancelsAfterS` (0.6 s, not measured). Basic Drive: drive toward a
      launch spot and press Square without letting the stick go, ten times; then the same with
      Cross at a FLOWER. Then, holding Square with the stick centred, push the stick. **Good:** none
      of the ten ends `sticks moved early`, the robot takes over within about half a second of the
      stick coming back, and a stick pushed after the press hands the robot back at once.
      **Stop:** the robot fights a driver who meant to keep driving → lower
      `staleStickCancelsAfterS` (0 is the old rule) and note the value that felt right.
- [ ] **Drive by hand once the Pinpoint is lost** (the Match page shows "Pinpoint lost"). A corner
      or camera reset cannot clear it (`PinpointLoss`: only a restart does), so Square and the FLOWER
      pick stay refused for the rest of the match, even though their refusal text suggests a reset.
      At Belding, 19429's driver kept pressing Square after the loss (8 presses in 14 s in Q13).
      Practise: the first refused press means stop pressing it, drive and aim by hand, and restart
      the robot after the match. **Good:** at most one refused press after a loss.
- [ ] **Operator: spin up before FIRE** when the next shot is coming, so FIRE finds the launcher at
      speed (first ball 0.8 s instead of 1.5 s).
- [ ] **The launcher keeps spinning after Square** (`TeleOpConfig.launcherKeepSpinningAfterSquareS`,
      3 s, not measured; #497). Basic Drive, loaded: (a) hold Square, fire, let go, and press Square
      again within 2 s: the gate opens on arrival with no spin-up wait. (b) Let go of Square and the
      operator holds FIRE within 2 s: the first ball leaves at once, and the launcher then stays on
      until the operator's toggle. (c) Let go and touch nothing: `launcher: idle` in the log about
      3 s later, and the Match page reads IDLE. (d) Let go, operator toggle at once: it idles at
      once. **Good:** all four, and the drivers say whether 3 s is long enough for their cycle.
      **Stop:** they want it off between volleys → 0 in Panels is the old rule.
- [ ] **19429: a shorter route from full to the shot.** At Belding it drove a median 191 in with a
      full robot before shooting (20245: 97 in). Try filling nearer a launch spot, or firing from
      where it is. **Good:** a timed practice cycle under 20 s.

### Square launch spots (11 Nov, one button again 14 Nov; nothing here has been driven)

Square drives to one of the six spots on the half the robot is in, picked by its tile row
(`localization/LaunchSpots`): from the tile row against the end wall (audience or scoring table),
the nearest of the three wall spots, the middle one counted 6 in nearer
(`LaunchSpotDrive.wallCentreBiasIn`, in Panels); from the next row in, the nearest of the three close spots,
the middle one counted 6 in nearer (`LaunchSpotDrive.closeCentreBiasIn`, in Panels); from the two rows beside the
centre line, refused. The close spots are driven 4 in back toward their end wall
(`LaunchSpotDrive.closeBackIn`, in Panels; 14 Nov: close volleys were hitting the HIVE's lip).
Gather GARDEN is back on Triangle; R1 is free. Turret locked. Map and
reasons: the "Square Launch Spots" page (mentor's claude.ai artifacts). [PR #536]

- [ ] **Each wall spot, on red then blue:** pose set, loaded, hold Square from about 2 ft out in
      front of each of the six (`RIGHT_WALL_NEAR`, `RIGHT_START`, `RIGHT_WALL_FAR`, and the LEFT
      three; the RIGHT middle one is the measured start itself, since 12 Nov). **Good:**
      the log says `launch spot: start … → <the spot> … (nearest, N in)`, then `arrived`
      and `gate open … settled` within about 2 s; the HIVE spots sit with the whole back on the
      wall, the NEAR and FAR ones with one corner on it; 3 or 4 of 4 stay in. Write the count per
      spot. **Stop:** a press that never arrives or never settles (the pose is asking it to push
      into the wall) → `LaunchSpotDrive.wallGapIn` 1 in Panels and try again; note which spot.
- [ ] **The wall row stops 1 in out from the wall (13 Nov, `LaunchSpots.WALL_STAND_OFF_IN`):**
      Square to any of the six wall spots now stops 1 in toward the HIVE from where it is drawn.
      `LEFT_WALL_NEAR` is then about half an inch clear of the SCORING FLOWER, and a robot on
      `LEFT_WALL_HIVE` meets one on `LEFT_CLOSE_HIVE` edge to edge. The Autos are not changed:
      they launch from the spots as drawn. **Good:** it arrives and settles without pushing on the
      wall, and scores as before. **Stop:** it still presses the wall, or a defender can now get
      behind it → note which, and the number changes there.
- [ ] **Square's early gate (`TeleOpConfig.squareGateOnApproach`, on since 13 Nov; nothing driven):**
      the gate opens on the approach, within `PathEndSettle.launchWithinIn` (6 in) of the spot,
      aimed and at speed. Ten Square volleys with it on and ten off, same spots, filmed. The log says
      `gate open … (before arriving, N in out), on the approach` when it fired early. **Good:** the
      first ball leaves sooner after the press (`gate open X s after the press`; Belding's median was
      1.03 s) and 3 or 4 of 4 still stay in, as with it off. **Stop:** more misses with it on, or
      the robot still moving at the wall spot as the balls leave → try `launchWithinIn` 3 in Panels
      (it is the Autos' number too), else switch it off and note the counts.
- [ ] **LEFT_WALL_NEAR and LEFT_WALL_HIVE at the SCORING FLOWER:** NEAR's corner should rest
      against the FLOWER's far side and HIVE's side about 2 in from its near side. **Stop:** either
      drives into the FLOWER → note by how much; the spot's x moves in `LaunchSpots`.
- [ ] **The FAR spots are 5 to 9 in past the centre line** (fair in TELEOP). Hold Square from just
      past the HIVE on each side. **Good:** arrives and scores as the others do. **Stop:** the
      straight path clips the HIVE frame from where the drivers would press it → note where.
- [ ] **Both robots on neighbouring spots:** one on `RIGHT_START`, the other pressed to
      `RIGHT_WALL_NEAR`, then `RIGHT_WALL_FAR`; the same on the left. They are drawn 1 in apart.
      **Good:** no contact, both volleys score. **Stop:** they touch → note the gap the logs show
      (`arrived … X in … off`, `drift`), and the side spot's x moves out by that much.
- [ ] **The spot is the one the driver expects:** from six or so places on each of the two tile
      rows of our half, the driver says which spot they expect before pressing. **Good:** it goes
      there every time, and a press from the two rows beside the centre line says `SQUARE REFUSED`.
      **Stop:** a press goes somewhere unexpected → note where it was pressed from; the middle close
      spot's favour (`LaunchSpotDrive.wallCentreBiasIn` or `closeCentreBiasIn` in Panels, 12 in and 10 in;
      try them there first) or the row edge is the number to change.
- [ ] **The close spots (measure the 36 in row first, §7d: until then they fire at the table's held
      end speed, about 1460):** hold Square from the middle tile row in front of each of the three on
      both sides. **Good:** drives to that `…_CLOSE_…` spot, 3 in back from where it is drawn
      (`closeBackIn`), about 38 to 39 in from the CELL, 3 or 4 of 4 in, clear of the HIVE's lip.
      **Stop:** still hitting the lip, or now short → change `closeBackIn` in Panels and write the
      number that works here. The NEAR
      and FAR close spots overlap the HIVE one by about a third of a robot: never send both robots
      to the middle and a side one at once.
- [ ] **Superseded 16 Nov: the Autos gave CATCH up and it is no longer a team spot; they launch from `AutoLaunchSpots` (see "The Autos' four launch spots, by hand first" above).** **`RIGHT_LAUNCH_CATCH` against `RIGHT_CLOSE_HIVE`, before the Autos give CATCH up** (mentor,
      12 Nov; needs the 36 in row first, §7d). Neither button drives to CATCH now: drive there by
      hand (57.5, 18, facing the CELL; the Match page's "Last shot" line shows 43 in) and fire with
      FIRE; then Square from the middle tile row in front of the HIVE for `RIGHT_CLOSE_HIVE` (36 in). Ten volleys of 4
      from each, counting how many stay in. **Good:** one is clearly better; write both counts
      here. CATCH stays a team spot only if it scores more.
- [ ] **Gather GARDEN back on the driver's Triangle (14 Nov):** hold Triangle at the GARDEN.
      **Good:** the gather runs as it did; the Controls page and the cheat sheet show Triangle, and
      R1 does nothing.

## 8. If time: anyone, no driving

- [ ] **Feed path: gate open to first ball is about 0.85 s** on both robots (Belding median), the
      biggest single step of a volley. Look at whether the first ball can sit closer to the wheels
      before the gate opens. Mechanical; no code change.

- [ ] **Photograph both robots' hubs** for `doc/wiring/`, ports against the XML. [#33]
- [ ] **Launcher: the exit angle and the exit height**, robot off: a protractor on the launch path
      where the ball leaves the wheels, and a tape from the floor. **Good:** both written in the PR
      that replaces the simulator's placeholders (`FieldSim.SETPOINT_PITCH_DEG` 75°, exit height
      17 in). **Stop:** if the angle changes with the wheel speed (a flexible hood), say so instead.
      [#429]
- [ ] **Driver Hub battery in the logs:** at the end of the session, plug each Driver Hub into the
      laptop by USB and double-click `pull-logs.cmd`. **Good:** it says `Driver Hub <serial>: battery
      N % now`, the robot folder gets `<date>_DriverHub-<serial>.battery.txt` and `.dslog.txt`, and
      `read_logs.py dsbattery` on them gives a falling series from the OS history with sensible
      times (compare one with the Driver Hub's clock). Then once with the Driver Hub run below 30 %:
      the `.dslog.txt` has `BatteryChecker: percent remaining: N` lines, and none above 30 %.
      **Stop:** no Driver Hub found (`adb devices` on the laptop must list it: allow USB debugging on
      the hub's prompt), `dumpsys batterystats` refused or empty, the battery lines' format or the
      30 % threshold not as the reader expects, or the folder is `DriverHub` while on the robot's
      Wi-Fi (the SSID read) → note the exact output in the PR. [Driver Hub battery PR]

## 9. From PRs merged 29 Oct–1 Nov, not in any session doc

Small checks the PRs asked for. Several likely ran at the 31 Oct–2 Nov sessions: strike what did.

- [ ] **Auto: Hold Still**, red, through PLAY: no FAULT (1 Nov it was INIT only). [S1]
- [ ] Turn exactly 180° in place at the red start: pose moves < 0.5 in. [PR #275] (*#330 re-measured
      the pods on 1 Nov after they moved: likely done*)
- [ ] Reset holds are 0.5 s and buzz gamepad 1 (turret zero: gamepad 2). If a reset gives no buzz,
      the match log's `rumble:` line says the code sent it, and `Controls/Gamepad1` in Metadata
      names the pad type: swap in another controller (or charge this one) before changing code.
- [ ] AdvantageScope 3D: the stick swings, the rocker tips the right way; on a clean Windows account,
      `tools/advantagescope/replay/README.md` steps 1-2 for one Belding match: models, layout and the
      video's own lock. [PR #294], [PR #299]
- [ ] Star-wheel servo turned round on both robots (4 Nov 2026), `IntakeSubsystem.starsReversed` now
      true (`reversed` stays true): on 20245 the roller reads positive (4 Nov logs); left: the stars
      turn inward on 20245, and the same on 19429; L2 backs both out.
- [ ] R2 toggles the intake with its Panels switch on; feeder runs whenever intaking. [PR #321],
      [PR #327]
- [ ] Operator R2 toggles the intake (no Panels switch); driver R2 held runs it with the toggle
      off, either L2 held reverses it, and letting go returns to the toggle. Toggled on, load 4: at
      full the roller and the prefeed stop, the stars keep the 4th in, the Match page says "holding
      (lane full)", and the driver's R2 does not restart them; one shot and both start again.
      Sensor at slot 4: confirm `presentAbove`/`leaveBelow`/`fullHoldS` there, and that no piece
      merely pausing leaves it stuck "full" (if one does, `PrefeedSubsystem.stopWhenFull` off).
      (intake toggle + prefeed stop)
- [ ] **Spit out the other alliance's NECTAR** (`NectarReject`; Panels `ColorSensorSubsystem`:
      `rejectEnemyNectar`, `rejectBurstS` 0.5 and `rejectHoldOffS` 0.3, both guesses, the roller at
      `rejectRollerPower` 1.0 during a burst (0.6 left one at the mouth, 5 Nov),
      `rejectAfterReadings` 2, the cap `rejectMaxBursts` 3 in `rejectWindowS` 3). Basic Drive, red,
      intake on: feed in a blue NECTAR by hand, ten times, then a red NECTAR and POLLEN ten times
      each. **Good:** every blue one comes back out within about 0.2 s of reaching the sensor and
      lands clear of the roller (the whole lane backs out with it: the mentor's call, 5 Nov), and no red NECTAR or
      POLLEN is ever spat out; each burst is a `Lane: REJECT` event in the match log with its hue.
      Repeat as blue with a red NECTAR, then once during a FLOWER pick (the pick's dwell should not
      end early on the burst). **Tune:** bursts back to back on one piece, or the roller takes it
      straight back in after the hold-off → raise `rejectBurstS` or `rejectHoldOffS`; it is thrown
      much further than needed → lower `rejectBurstS`. A `REJECT gave up` or a `FULL with the other
      alliance's NECTAR` event means a burst could not clear it: film it. A POLLEN or our own NECTAR spat out → the event's hue says which range
      (`redNectarHueMin/Max`, `blueNectarHueMin/Max`) to narrow; until fixed, `rejectEnemyNectar` off.
      Since 10 Nov the red NECTAR / POLLEN boundary is 42° / 43° (it was 47° / 48°), from the
      Belding logs: a dim POLLEN at 46° was spat out, real red NECTAR read up to 39°. The gap is
      narrow, so in this check feed some POLLEN in slowly and half-covered (a dim reading) and note
      every hue the Robot page shows between 35° and 55° with what the piece really was.
- [ ] **19429 keeps its own alliance's NECTAR** (the 7 Nov `rejectAllNectar` line for 19429 is
      gone from `RobotDifferences`, so 19429 runs the shared reject: only the other alliance's
      NECTAR is spat out). Basic Drive on 19429, red then blue: feed in a red NECTAR, a blue NECTAR
      and a POLLEN, five times each. **Good:** our alliance's NECTAR and every POLLEN are kept, the
      other alliance's NECTAR comes back out, and the match log's Metadata lists no NECTAR line under
      `RobotDifferences`. **Stop:** our NECTAR spat out → narrow the NECTAR hue ranges as above; to
      lock 19429 out of NECTAR again for a match, Panels `rejectAllNectar` on.
- [ ] **Full-lane position hold:** Panels `IntakeSubsystem.positionHoldWhenFull` defaults on
      for both robots (19429's encoder wire was plugged in 6 Nov, so its off line is gone). The roller captures
      its encoder position once, stars continue inward and prefeed stops. Initial `positionHoldMaxPower` 0.3 and
      2-tick tolerance need robot confirmation. During normal practice, load 4 and back away:
      the 4th stays in and a loose 5th stays out. Match logs include `PositionHolding`,
      `HoldTargetTicks`, `HoldErrorTicks` and current. Feed/eject/reject release the hold.
      If correction is too weak, adjust the max in Panels; if the shaft holds but the ball
      slips, mechanical retention needs attention. Position hold off restores legacy roller
      power (`holdPowerWhenFull`); `starsPowerWhenFull` separately controls the stars (default 1).
- [ ] Unjam with a ball on the launcher wheels: rolls back, launcher returns to speed. [PR #323]
- [ ] Launch Test on gamepad 1, operator layout. [PR #324]
- [ ] Turret locked: hold Circle after intaking, the chassis settles on the CELL. [PR #329]
- [ ] Controls page and printed sheet match what the robot does ("checklist coming separately").
      [PR #270]
- [ ] GARDEN: measure red's gather and line-up poses for `GardenFieldPoints`. [PR #267]

## On the board, not for this visit

Lessons, spikes, design work and features still to build. Each needs the robot eventually; none is
one test at one meeting.

- Build first, then test: [#261] localized-at-a-glance on the DS, [#182] Auto wait limits in Panels,
  [#141] HIVE state in the match log, [#239] deleting confirmed logs from the hub, [#251] Auto stick
  cards (partly overtaken by #378).
- Hardware and design: [#213] beam-break lane sensor, [#169] turret OctoQuad, [#164] the transfer,
  [#172] clean-sheet chassis, [#158] spill hook spike, [#179] fixed stick spike.
- Measurement projects: [#155] timing facts (#384 measured some from logs), [#147] filming spills.
- HIVE lessons and demo: [#126] series, [#127], [#128], [#129], [#133], [#156].
- Workstream: [#78] Pedro tuning.

[S1]: robot-session-1.md#at-the-robot-in-order
[S2 §1]: robot-session-2.md#at-the-robot-in-order
[S2 §8]: robot-session-2.md#at-the-robot-in-order
[S3 Before]: robot-session-3.md#before-leaving-the-laptop
[S3 §2]: robot-session-3.md#at-the-robot-in-order
[S4]: robot-session-4.md#at-the-robot-in-order
[S4 Before]: robot-session-4.md#before-leaving-the-laptop
[S4 §1]: robot-session-4.md#at-the-robot-in-order
[S4 §2]: robot-session-4.md#at-the-robot-in-order
[S4 §3]: robot-session-4.md#at-the-robot-in-order
[S4 §4]: robot-session-4.md#at-the-robot-in-order
[S4 §6]: robot-session-4.md#at-the-robot-in-order
[S4 §7]: robot-session-4.md#at-the-robot-in-order
[S4 §9]: robot-session-4.md#at-the-robot-in-order
[S5]: robot-session-5.md
[S5 Before]: robot-session-5.md#before-leaving-the-laptop
[S5 At the robot]: robot-session-5.md#at-the-robot-in-order
[S5 §3-5]: robot-session-5.md#at-the-robot-in-order
[S5 §1]: robot-session-5.md#at-the-robot-in-order
[S5 §2]: robot-session-5.md#at-the-robot-in-order
[S5 §6–7]: robot-session-5.md#at-the-robot-in-order
[S5 seam §2]: robot-session-5.md#also-with-an-auto-on-the-field-the-drive-to-pick-seam-pr-378
[S5 seam §4]: robot-session-5.md#also-with-an-auto-on-the-field-the-drive-to-pick-seam-pr-378
[S5 seam §5]: robot-session-5.md#also-with-an-auto-on-the-field-the-drive-to-pick-seam-pr-378
[handoff]: ../.claude/handoff/20245-camera-calibration.md
[Visualizer PR #44]: https://github.com/Mona-Shores-FTC-Robotics/Visualizer/pull/44
[PR #262]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/262
[PR #267]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/267
[PR #270]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/270
[PR #273]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/273
[PR #275]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/275
[PR #279]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/279
[PR #280]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/280
[PR #281]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/281
[PR #294]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/294
[PR #299]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/299
[PR #307]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/307
[PR #321]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/321
[PR #323]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/323
[PR #324]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/324
[PR #327]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/327
[PR #329]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/329
[PR #333]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/333
[PR #334]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/334
[PR #353]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/353
[PR #354]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/354
[PR #356]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/356
[PR #360]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/360
[PR #361]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/361
[PR #372]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/372
[PR #373]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/373
[PR #374]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/374
[PR #375]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/375
[PR #377]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/377
[PR #378]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/378
[PR #384]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/384
[PR #387]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/387
[PR #391]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/391
[PR #422]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/422
[PR #423]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/423
[PR #536]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/536
[PR #425]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/425
[PR #427]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/427
[PR #430]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/430
[PR #492]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/492
[PR #644]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/644
[#33]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/33
[#62]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/62
[#429]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/429
[#78]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/78
[#126]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/126
[#127]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/127
[#128]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/128
[#129]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/129
[#133]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/133
[#141]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/141
[#147]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/147
[#155]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/155
[#156]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/156
[#158]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/158
[#164]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/164
[#169]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/169
[#172]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/172
[#175]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/175
[#179]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/179
[#182]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/182
[#213]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/213
[#239]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/239
[#251]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/251
[#255]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/255
[#261]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/261

[#504]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/504
