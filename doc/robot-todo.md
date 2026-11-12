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

## Belding work order (agreed 5 Nov)

The old night-by-night plan is superseded by this order. R-Solo remains the working
reference. Keep both turrets locked; do not spend this session tuning benched Autos.

1. **L-NoCross** (formerly L-Partner). Qualify it alongside a reliable RIGHT preload
   shooter, whether that partner stays clear or parks. **Before qualification:** its
   current no-TIP branch still crosses to RIGHT; draw a LEFT-side recovery/park in the
   Visualizer. Then test CELL-up recognition, the two LEFT volleys, the scoring FLOWER
   pickup, and park, including a partner that never makes TIP 1.
2. **R-NoCross** (formerly R-Partner-Tip). Qualify the right-side actions, then run it
   together with L-NoCross as the coordinated two-TIP baseline. Check the five-second
   CELL-up wait: the current timeout still falls through to LaunchAll. Resolve that in
   the drawing before relying on missed-TIP recovery. Waiting for the teammate is
   acceptable; optimize only after the pair is reliable.
3. **R-Solo-Nudge** (`autos/drafts/r-solo-nudge.pp`). Open it together with
   `autos/partners/partner-push-start.pp` to see the pushed robot. Physically measure
   the push, legal starting contact, PARK overlap, clearance and time cost (§3).
   Target two TIPs and both AUTO PARKs; the current draft is not qualified and has no
   OpMode. A match partner with no motion still uses an initialized OpMode.
4. **L-Infraviolet / R-Infraviolet.** Competition Autos: run on both robots and promoted
   from Utility trials. Keep refining the pair; a dead partner is not covered (§3).

- [ ] **After loading the lineup cleanup:** the Driver Station's Autonomous menu has
      exactly L-NoCross, R-NoCross, R-Solo, L-Infraviolet and R-Infraviolet (with their
      save timestamps); the old Drive: L/R-Infraviolet Utilities are gone. Preload,
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
   `FlowerPicker.lowerWithinIn` 24 (§3's WALL FLOWER stick line, a number, not code). Every pick card
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
R-Solo-Nudge remains a draft; benched Autos are not deployment choices.
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
- [ ] **Both Control Hubs: which IMU chip, and how the hub is mounted** (logo and USB directions,
      for the SDK's orientation), before the IMU is logged. A Driver Station config scan shows the chip
      (BHI260AP or BNO055). Then one bench bump test with the IMU logged, to see what a hit looks like. [#498]

## 2. Any Auto, INIT only, no PLAY (about 20 min; camera connected, HIVE upright)

**INIT names the start the robot is on** ([PR #372]; the 10 in match radius is untested). Pick
the Auto, place the robot on the marks, INIT, press **no** alliance button. 5 frames settle a match,
about a second. Written out on branch `claude/eager-pasteur-f4trol` (`doc/robot-session-5.md`, last
section, not on master); this is that section.

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

- [ ] **INIT options, L-NoCross on Red far wall:** gamepad 1 D-pad up three times.
      **Good:** one line `partner's last shot ▲▼ 7 s, waiting until 11.5 s`, amber; down three times
      turns it back green, as drawn (last shot 4 s, waiting until 8.5 s); D-pad left and right change
      nothing (no Auto offers another park); nothing else on the robot reacts to gamepad 1's D-pad
      in INIT. **Stop:** the line does not change, or something else moves → save the match log.
      [PR #391]. **Not yet seen on a robot:** at Belding every L-NoCross wait gave up at the drawn
      8.0 s, and no log can say what INIT showed (the next item). Show the drive team the line too.
- [ ] **The chosen INIT options reach the match log** (same run: set the partner's last shot to 7 s,
      PLAY, let it run a few seconds, STOP). Belding's logs lost every `AutoOptions` line: PLAY
      wrote 160 tunables into a 160-slot loop. **Good:** `read_logs.py series <log>
      /RealMetadata/AutoOptions` prints `… partner's last shot 7 s, until 11.5 s (drawn 8.5 s)`, and
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
- [ ] **19429 SWYFT continuous-mode burst experiment** ([test PR #487](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/487)).
      **Position hold trial:** SWYFT stays in continuous mode. Zero forward, PLAY, Triangle/Y
      captures the CURRENT angle (not +5); hold R2 to enable feedback correction. D-pad up/down
      steps target +/-5 degrees. Start with one +5 then -5 test. It uses recorded 150 ms full-speed
      corrections, PWM OFF at least 350 ms afterward, then rate <=1 deg/s for 100 ms before another
      correction. Target band +/-1 degree is experimental, not demonstrated accuracy. R2 release
      immediately disables PWM; Circle/B cancels hold. Re-enable may correct drift. At target the
      servo is unpowered, so this restores angle after a push rather than providing a stiff brake.
      Record settle/overshoot and an external hand displacement with normal logs; do not judge it
      from just the angle while the drive command is ON. Startup/stop delay remains unexplained.
      **Load the corrected version:** telemetry must say `SWYFT v5: ENCODER HOLD / recorded`. The original version wrongly treated 1500 us as stop and drove home on release.
      SWYFT's [own guide](https://github.com/SWYFT-Robotics/swyft-servo-programmer-v2/blob/main/src/GuideWindow.xaml)
      says 1500 us homes even in continuous mode; stop requires PWM disable, and partial-speed
      control is unavailable. Only **Bench: Turret Burst**, not Basic Drive, Auto or ordinary
      Bench: Turret, while flashed continuous. Position-mode checks below are deferred; reflash
      position mode before returning to them. Servo remains on Control Hub servo port 4; Cypher Max
      on Expansion Hub motor port 3 encoder socket, 3V3. Programmer: continuous, range 255/255,
      signals 500/1500/2500 us. Install this branch, activate robot_19429. Confirm mechanical
      clearance within +/-30 degrees or reduce TurretBurstBench.travelEachWayDeg before PLAY.
      INIT: align forward, Cross/A with R2 released; hand-turn a measured small arc to check 2.75
      ratio and CCW-positive angle, then return home and zero again. PLAY leaves PWM disabled.
      Logging is automatic from INIT to STOP in FIRST/logs, using the normal async match logger.
      Screen Recording line must name a .wpilog, with no fault. Run a few manual single bursts,
      pausing at least a second after each; no angle transcription needed. STOP, run the existing
      tools/pull-logs/pull-logs.cmd on the deployment laptop, and tell the session to pull logs.
      It sends the bench log to the normal logs/<date>-19429-RC branch when internet is available.
      The session runs read_logs.py turret to see configured/actual duration, signal, input gating,
      before/after/delta, 250 ms OFF sample, battery, live setting changes and release interruptions.
      The latest photo confirmed manualBurstMs was still 125, last ON 126.4 ms, signal 2500 us;
      successive absolute angles tapered toward zero, so do not treat them as per-burst deltas.
      Manual now defaults to ONE 250 ms full-speed burst per press, capped at 500 ms: the 100 ms
      repeating test gave no visible motion (mentor; encoder rate briefly -1.2 then zero).
      Hold R2 + L1/R1, release to rearm. Watch Inputs R2/L1/R1, PWM enabled (SDK), and the latched
      burst count/last signal/last ON time. SDK enabled state confirms the API state, not a measured
      waveform. A short burst may not overcome startup/ramp behavior; that is unconfirmed.
      Compare one burst with manualBurstMs 250 then 500 only if clearance permits. Report whether
      the burst count rises, last signal is 500/2500 us, last ON time matches, and angle changes.
      Set manualSingleBurst false in Panels to return to repeating manual bursts.
      Verify CCW/CW (else flip servoDirection); release R2 must leave it where it is, not drive home.
      Triangle/Y now captures the current angle; use the position-hold trial above. D-pad changes by 5.
      Compare repeated +5/-5 approaches, error, overshoot and drift in Panels' turret_burst_* graphs.
      Starting timing 25 ms on / 250 ms PWM-off; tune burstOnMs/burstOffMs for SWYFT. On time is
      capped at 100 ms; off time is clamped to 100..10000 ms. Direction reversals first pause.
      Circle/B cancels hold and disables PWM; trigger release, pauses, and STOP also disable PWM.
      Verify no powered movement at INIT/PLAY or during pauses. The software window depends on
      correct encoder/sign and does not prevent coasting. No full revolution is required.
- [ ] **19429: the turret re-homes from its encoder** (`TurretSubsystem.rehomeFromEncoder`, on for
      19429). Needs `degreesPerUnit` first (below). Basic Drive: in INIT twirl the turret three
      turns, put it on its mark, PLAY. **Good:** it swings off, then within about a second comes
      back to the mark; the Robot page's "Turret re-home" line shows the corrections. **Stop:** it
      swings further off → `degreesPerUnit`'s sign is wrong. "out of reach" → one more full turn
      by hand, on the mark, run again.
- [ ] **19429: before the turret is unlocked** (`lockedForward` false, the mentor's call). On
      **Bench: Turret** after PLAY: Square at home, step at least 0.1 away: the bench shows
      `degreesPerUnit` from the encoder (the 2.75 encoder ratio is applied); step both ways.
      First the ratio: Circle (limp), Cross at the home mark, turn the turret by hand to a 45° mark
      (no further: winding a position servo past its range is what moves home): `angle` ≈ 45°,
      else `turnsPerTurretTurn` (2.75) is wrong. Then set `degreesPerUnit`, the real
      limits (where the turret stops turning, less a margin) and unlock.
- [ ] **20245: the turret servo's straight-ahead position.** The servo is in position mode, like
      19429's (mentor, 12 Nov 2026); it is sent 0.5, its centre, until its straight-ahead position
      is measured. **Bench: Turret**, PLAY: find it like 19429's (0.514). (Its motor port 3 encoder
      socket held the turret's encoder at Belding, not the feeder's: the Belding logs.)

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
- [ ] **L-NoCross: the two CELL checks in its Otherwise route**, once they are drawn and
      exported ([the change, step by step](belding-auto-review.md#1-l-nocross-check-the-cell-before-each-volley-in-the-otherwise-route-about-3-points-a-match-in-playoffs-fairly-sure)).
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
- [ ] **RIGHT_PARK is judged out of the LOADING ZONE one time in three.** At Belding the
      scoreboard gave AUTO PARK to every LEFT_PARK end (6 of 6) but to RIGHT_PARK 4 of 6 (not in
      Q-4, red, or Q-31, blue), from the same logged pose each time (14.1–14.5, 95.6–96.1, or its
      blue turn); by the field model its centre is only 1.4 in inside the zone's edge. Put the robot
      on RIGHT_PARK and look at the tape from above. **Good:** clearly over the line → leave it; on
      the line → move RIGHT_PARK toward LEFT_PARK's end in the Visualizer and Save team spots. [PR #373]
- [ ] **Stick down on the drive in:** spotters at WALL_FLOWER_STAGING and SCORING_FLOWER_STAGING,
      R-Solo, two picks each. **Good:** the stick swings down about 18 in before
      the robot stops and touches nothing before the push. **Stop:** it touches → Panels
      `FlowerPicker.lowerWithinIn` 12, write it back. [S5 seam §2], [PR #378]
      Belding stream (7 Nov): at the WALL FLOWER it swings from 12–17 in out, is down 4–5 in before
      the stop and reaches the FLOWER's foot with no visible knock; the SCORING approach is hidden behind
      the FLOWER and wall, so only the SCORING FLOWER still needs spotters.
- [ ] **The stick lowers earlier at the WALL FLOWER:** the 3–4 Nov Auto picks read `prep done on
      the way in` 0.52–0.55 s at the WALL FLOWER (SCORING FLOWER 0.80–0.84), under the 0.7 s this check
      wanted, so its Stop applies: Panels `FlowerPicker.lowerWithinIn` 24, then the stick line above.
      **Good:** X ≥ 0.7 at both FLOWERs and the stick still touches nothing. Write 24 back if so.
      [S5 seam §4], [PR #378]
      Belding (24, master's value since 5 Nov): WALL 0.62–0.73 s in 11 Auto picks (4 reach 0.7), FAR
      0.94–1.29 s; on video the WALL stick lands 0.2–0.4 s before the stop, touching nothing seen.
- [ ] **Time more GARDEN gathers** (R-Solo or R-NoCross): one Auto gather is logged (3 Nov
      18:06, 4.21 s), so `AutoRegistration.GARDEN_GATHER_TYPICAL_S` is 5.0 now; a few more
      `Last GARDEN gather: N s` lines confirm or replace it. The same runs' `FLOWER pick: done` lines
      say how often a pick ends on `full`. Say whether the corner allows a stick approach for the
      gather. [S5], [PR #384], [PR #378], [PR #422]
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
- [ ] **CellUp with the CELL already up:** L-NoCross at `LEFT_LAUNCH_CATCH` with the left CELL
      tipped up by hand first (or a partner's tip over before the wait starts). **Good:** the wait ends
      at once and the Auto fires, not at its limit (8.5 s on the match clock). Then with the
      CELL **down** and nobody tipping: it waits out its limit and fires anyway (the Auto's own choice for now). **Stop:** it waits with
      the CELL up, or fires at once with it down → save the match log; check which CELL the robot
      aims at from its end of the field (`AutoRegistration.aimedLeft`). [PR #377]
      *Belding (7 Nov):* no L-NoCross run started its wait with the CELL up. R-Solo Q-27 (20245) did: the red CELL tipped about 6 s in and stayed up, and its wait ended "CellUp after 0.02 s". The CELL-down half ran three times (Q-14 20245, M-7 20245, M-9 19429): video shows the CELL down at each 8 s limit, and the Auto took its no-TIP route.
- [ ] **L-NoCross's TIP wait on the match clock:** partner's last shot set to 7 s in INIT (waiting
      until 11.5 s), nobody tips.
      **Good:** the trace shows `Wait for TIP or CELL up: 11.5 s on the match clock` about 11.5 s into
      the match, then the no-TIP route. **Stop:** it ends at 8.5 s or counts from its arrival → save
      the log. [PR #391]
      *Belding (7 Nov):* every L-NoCross run had the limit at the drawn 8 s (nobody entered a later shot). The three timeouts ended "8.0 s on the match clock" 7.00–7.12 s after the wait began, so the limit is on the match clock. The 11 s setting was not tried.
- [ ] **Early park from L-NoCross's no-TIP route:** nobody tips, partner's last shot set late (11 s or more)
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
- [ ] **The looser settle (aimed and slowing, under 6 in/s):** read the `settled X s after path end`
      lines from the same runs with `launchOnApproach` off. **Good:** X under 0.3 s (was 0.38–0.57),
      and hits as good as before. **Stop:** misses → `PathEndSettle.slowSpeedInPerSec` 3.
      [PR #430]
- [ ] **Angled nudge with the dummy chassis — Utility `Drive: Partner Nudge`.**
      Sloth Load the nudge-test branch/PR; the three competition Autos stay as they are.
      [Setup drawing](nudge-test-setup.svg). Red frame: a **16 x 16 in** partner centre
      at **(10.254, 82.000), heading 70°**, one corner on the alliance wall and its
      whole body outside the loading zone. Place our centre at **(11.754, 64.193),
      heading 250°**, rear bumpers parallel, nominal **0.5 in gap and 8.5 in overlap**.
      Our rear bumper pushes; our front points away from the partner. Measure the
      dummy and our actual bumper envelope first. If they differ from the drawing,
      adjust the `.pp` geometry and export before trying contact; these are not size presets.
      Blue uses the same setup rotated 180° about field centre.
      - **6 Nov runs never moved** (4 in/s and 8 in/s² cap, too little power to start the robot).
        The cap also stayed on after STOP and stalled later paths until the app restarted. Both
        are fixed: a `maxPathSpeed` fraction, and the cap cleared whenever a path ends. Still,
        **after any nudge run, drive R-Solo once before trusting the next Auto.** [PR #492]
      - First run without the dummy to check direction, the six-inch stroke and retreat.
        Choose alliance in INIT, then PLAY. Default Panels `PartnerNudgeTest` settings:
        **pathSpeed=0.2** (fraction of top speed, 0.1–0.5), **timeoutSeconds=8**, captured at
        PLAY. STOP and re-INIT to repeat after resetting both robots. **Measure `pathSpeed`
        here:** the lowest value that starts the robot and runs the stroke. If 0.2 doesn't
        move it, try 0.3, then 0.35 (the FLOWER push's slowest known-good). Write the value
        that works in the log/notes and set it as the default in a PR.
      - Put the dummy back, with solid bumper contact and wheels free to roll. Run the
        same short test. **Good:** partner enters the loading zone and ends visibly
        off the wall, push is gentle, and retreat separates cleanly. The calculated
        1.88 in wall gap / 5.17 in upfield movement assumes no rotation: measure both.
        Record actual dimensions, wheel/brake behavior, final wall gap, park overlap,
        rotation and measured speed in the log/video; repeat from the same marks.
      - **Stop:** hard initial contact, wheel slip, snagging, unexpected rotation,
        or timeout. The test cancels and releases the drive at 8 s; it does not keep
        holding the push endpoint. A speed cap is not a force cap.
        Change one setup or speed variable at a time; do not lengthen a blocked push.
      - Once repeatable, try with our normal four-piece load and then integrate the
        measured maneuver in **Drive: R-Solo-Nudge**, the full Utility with scoped
        contact limits. Its preview is not a qualified robot run.
        Two TIPs and double park, followed by 17/18 in partner presets, come after this test.
      [Draft and Utility details](../TeamCode/autos/drafts/README.md#first-floor-test-drive-partner-nudge)

- [ ] **Infraviolet pair: L early garden, R both flowers.**
      [Assignment, reused paths, cutoffs and timing](../TeamCode/autos/infraviolet.md).
      Run **Autonomous → L-Infraviolet / R-Infraviolet** with the DS AUTO timer on
      (it stops them at 30 s). The previous assignment is preserved in Git history.
      - Confirm L moves to LEFT_CATCH but never shoots before R's TIP 1 settles.
        Missing TIP 1 must send L to far park without collecting the garden.
      - Dry-run both south crossings and the separate parks. L must pass a robot
        already in RIGHT_PARK without touching it. Stop if either robot stalls;
        a dead L at its start obstructs R's reused LEFT approach.
      - Keep L at the existing RIGHT offset; drive R's final turn and park around
        it, checking actual frame/poker and centre-line clearance. The drawing's
        minimum centre-line margin near the shot is about 1 in along the approach (about 2 in at the shot), not a tracking tolerance.
      - Qualify R's new local RIGHT_PAIR_CLOSE shot (59.5, 31, about 97.2 degrees;
        turret range about 30 in). L's shared RIGHT offset is unchanged. Check
        camera visibility and the shot line with both robots in position.
      - Test loaded exits after the normal pickup retreat. The long under-HIVE
        crossing is forward; R's final shot alignment still has a short backward
        component. Tune/test retention there before trusting the fourth piece.
      - Measure R final RIGHT readiness before **22.5 s**; L's before **20 s**.
        R collects the scoring flower while TIP 2 settles, then confirms RIGHT up
        at the final shot. Test late arrivals and missed TIP 2: skip the
        unconfirmed volley and take the
        assigned park. The 1 s launch timeouts (0.5 s for R's final volley)
        limit readiness, not an active feed; test the shorter final window.
      - Run the complete pair, including mismatched drive speeds and missed volleys.
        Full physics simulation at `63cdc72b` gets three TIPs plus both parks in
        one of three nominal seeds; the other two execute every volley but get
        two TIPs and both parks. Shot conversion/retention remains unqualified.
        Confirm both LEAVEs at AUTO end and both parks on video; the pair is
        already promoted, so log each run that shows otherwise.

- [ ] **Drive times for the planning numbers** ([#504], the Belding timing analysis): a Utility
      that follows a Pedro path, empty and again with 4 POLLEN in the lane, a
      charged battery (note `/Robot/BatteryVolts`), three runs each. (a) A straight 90 in run at a
      constant heading, (b) the same 90 in with a 180° turn spread along it, (c) R-Solo's
      `WALL_FLOWER_STAGING` to `LEFT_LAUNCH_CLOSE` (it turns 270° on the way). Read each run's
      `auto: path` to `path end` seconds and `/Drive/OdometrySpeedInPerSec`. **Good:** (a) peaks
      55–63 in/s and takes about 2.4 s; (b) and (c) cruise 20–30 in/s, (c) 3.6–4.3 s, as at
      Belding: then the simulator's `TURN_SHARE_RAD_PER_S` (130°/s) stands. A loaded run more than
      0.3 s slower than the empty one means the planning numbers need a loaded value.
      **Also:** the two Belding runs that crawled for 4 s about 10 in short of `LEFT_LAUNCH_CLOSE`
      / `LEFT_LAUNCH_CATCH` (20245, 14:39 and 16:53 hub time) look blocked: check the match video.

- [ ] **R-Two-Tip draft (`autos/drafts/r-two-tip.pp`): R-Solo's steps from the measured LEFT spot.**
      [Why, and the simulator runs](../TeamCode/autos/drafts/README.md#r-two-tip-r-solos-steps-fired-from-the-measured-left-spot).
      Run it from the Utility **Drive: R-Two-Tip** (it stops itself at 30 s).
      - Five runs alone, filmed from the side, partner off `LEFT_START`. Count the pieces that
        stay in the LEFT CELL from each LEFT volley, and the TIPs. **Good:** 8 of 8 stay in on
        most runs. This is
        also the count the Belding review asked for on R-Solo.
      - **The third volley must run.** Master's guard parks without it when the second volley
        ends after about 16.8 s (the calibrated simulator: 15.8 s). Read the card times from
        each run's log (`read_logs.py summary`). **Stop:** `parking now` after the second
        volley → note when that volley ended; the guard's numbers (`LAUNCH_ALL_TYPICAL_S`,
        `PICK_FLOWER_TYPICAL_S`) need measuring again.
      - **`LEFT_LAUNCH_OFFSET`, the last volley's spot,** has not been fired from on the left.
        Hold Square there first and count: **Good:** 3 or 4 of 4 stay in.
      - **Count what each volley fires** (`read_logs.py volleys` on each run's log, and the film):
        at Belding R-Solo's two LEFT volleys never fired 8 between them, and Infrared's loads
        came up short on both FLOWERs. **Good:** 4 fired after every pick. **Stop:** fewer →
        film where the pieces stop (FLOWER, intake, lane, feed) before anything else
        (`doc/one-robot-two-tips.md` lists the other measurements).
      - The short turn on the drive from the WALL FLOWER: time it (drawn 2.9 s) and check that no
        piece leaves the robot and the camera sights the LEFT CELL before the stop.
      [mentor session, 10 Nov]

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

## 5. Pose and camera (Basic Drive / Vision: HIVE & Pose)

- [ ] **Camera calibration, R1 at two spots** (Vision: HIVE & Pose, Red audience then Red far wall).
      **Good:** "Spots so far: AGREE" → commit the two numbers to `CameraMount` (code 3.55 / 0.95; 2 Nov
      read 3.13 / 0.89). **Stop:** DIFFER → a HIVE point or the marks, don't commit.
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
- [ ] **Operator's glance page, trim rumble, D-pad trim reset, no intake toggle (`belding-plus`,
      7 Nov):** Basic Drive, Match page, read from the operator's seat and from the coach's spot.
      **Good:** under the header and the alliance line, three lines in text about twice ordinary
      size (three nested `<big>`): `TRIM 0`, then `IDLE` / `SPIN` (amber) / `READY` (green), then
      `N PIECES`, green while the sensor sees the lane full; a fourth, amber, only when
      something is wrong (`INTAKE OFF`, `ROBOT-CENTRIC`, `SQUARE REFUSED` while Square is held,
      `FLOWER STICK OUT`, `NO PINPOINT`). Everything else is on the Robot page (Share twice).
      Operator R1: `TRIM +25` in amber and one rumble blip; L1: two blips; D-pad down: back to
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
- [ ] **GARDEN gather, curve to the wall and quick out (`garden-gather` branch):** at the
      practice field with the TeleOp gather button (driver Triangle, held), GARDEN loaded, phone
      filming from above; then once in R-NoCross. It lines up where it always has, stick going
      down; then the drive in curves (`GardenGather.curveToWall`): over to the side wall first,
      then down it into the corner. It aims 1 in past the gather pose (`extraDepthIn` 1), dwells 0.4 s (`dwellMs` 400), backs out at 0.8 speed
      (`backSpeed`) with the stick still down, straight up the side wall first then over
      (`curveBackOut`; off is a straight diagonal back), stops, and raises the stick 0.25 s later
      (`raiseAfterStopMs`). Try `curveBackOut` on and off and note which leaves fewer balls behind. Then `useStick` off: the
      stick stays in and the intake drives 2 in deeper (`noStickExtraDepthIn`) all the way to
      the wall; compare balls in with the stick and without. Then `twistOnBackOut` on (either way):
      after the dwell it turns 30° counterclockwise in place against the wall (`twistDeg`,
      `twistPower` 0.4), then backs out; note whether more balls roll in. **Good:** the side is on the side wall well before the nose reaches
      the audience wall, the nose gets there (`GARDEN gather: IN … reached` or under 1 in short,
      against 2–4.5 in on 6 Nov), more balls come in than 6 Nov's 0–1, and the stick never drags
      a ball back out. **Stop:** it scrubs into the side wall and stalls early, or the curve
      swings wide → `curveToWall` off (the old diagonal); `raiseAfterStopMs` -1 is the old stick
      rule. Tune `dwellMs`, `backSpeed`, `laneTowardWallIn` (0.5) and `extraDepthIn` in Panels and
      write down what works.
- [ ] **Film the Auto volleys (decides `LaunchCards.launchAllFeedS`, 3.5):** 5–6 LaunchAlls,
      phone slow-motion side-on, the launcher and the Driver Station both in frame. Per volley,
      write down each ball's time after the gate opens and the launcher's counted shots.
      **Good:** no ball after 3.0 s in any → `launchAllFeedS` 3.05 in Panels, and the volleys
      where the count fell short show which way the shot counter misses.
- [ ] **Square's start and corner spots aim the turret (`belding-plus`, 7 Nov):** turret locked,
      hold Square from near each start and each corner (RIGHT_START now 89°, LEFT_START 267°,
      RIGHT_LAUNCH_CORNER 61° instead of 54°, LEFT_LAUNCH_CORNER 303° as before) and fire.
      **Good:** the volley goes into the CELL, centred; RIGHT_LAUNCH_CORNER scores at least as well
      as on 4 Nov. **Stop:** shots land to one side → note which side and by how much.
- [ ] **Square from past the centre line (`LaunchSpotDrive.maxPastCentreIn`, 24):** on 6 Nov the
      drivers pressed Square with the robot 3–4 in past the line and it refused every time. Hold
      Square with the robot about 6 in, then about 20 in, past the line, on red and on blue.
      **Good:** it drives home to the nearest spot on our side and fires; past 24 in the Match page
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

## 7. 19429, if it is at the lab

- [ ] **R-Solo on 19429:** its first path from the Red audience marks. 19429's drivetrain is tuned
      on its own values (6 Nov, PR #480: Check: Drive Motors, Pod Offsets, Foresight, Tests all
      passed), so a path that drifts on 19429 but not on 20245 is now worth reporting. [#175]
- [ ] **Anything 19429 does differently** from 20245 (a motor turning the other way, a servo
      position, the intake sign): one line in `RobotDifferences.java` under `TEAM_19429`, with what
      was seen and the date, then Sloth Load. Never edit the shared value: that changes 20245 too.
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

## 7b. Bench: Wall Eject (any field wall, 4 POLLEN, phone filming from above)

Can an L robot set its preloads down against a wall, to collect later? Film every trial; the log
has each trial's settings and measured back-up.

- [ ] **Run Bench: Wall Eject** (Utility menu): intake against the wall, Cross runs one trial
      (backs off 1.5 in, ejects at roller 0.3 / stars 0.3 for 2.5 s, feeder backing out too). Try
      roller 0.2–0.5 (D-pad ▲▼), back-up 1–2 in (◀▶), feeder off (Square). **Good:** all four
      rest at the wall within one robot width, none rolled more than a few inches. **Stop:** after
      about 10 trials; note the best settings and whether they sat four wide or in a line.
      [this branch, claude/quirky-allen-etn5xp]
- [ ] **Then drive through them** with the intake on (Basic Drive) from the side and head-on.
      **Good:** 4 of 4 collected in one pass. **Stop:** record how many of 4, over 5 tries.

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
"Speed table: OFF"), hold Square. The robot drives to the nearest launch spot and fires while Square is held.
On a miss, the operator trims the speed up or down, then Square again, until it scores. The Match
page's "Last launch spot" line shows the spot's CELL range and the launch speed: write both down.
Take the middle of the speeds that score 4/4, not the first one that does. The trim carries from
press to press.

Measured 4 Nov 2026 (20245, tuned PIDF) and in `LaunchSpeedTable` with the table on: 42.4 in → 1520,
45.7 → 1560, 53.9 → 1600, 58.8 → 1635 (ranges from the turret axis since 5 Nov; first written from
the wheels' centre as 39, 42.3, 50.5, 55.4). What is left here is the right corner's heading.

- [ ] **`RIGHT_LAUNCH_CORNER` faces about 5° right of the CELL.** At Belding (turret locked
      forward) the aim error held at +4.5° there on red (Q-7) and +5.5° on blue (Q-31), against 0°
      at `LEFT_LAUNCH_CORNER` (M-7, red) and within ±2.5° at the other spots; 3 and 2 shots counted
      of 4. Set its heading to about 59° in the Visualizer (Save team spots), then hold Square there
      once. **Good:** aim error under 2° on the Match page and 3–4 of 4 in. [mentor, 5 Nov]

- [ ] **A row at 36 in, for `LEFT_LAUNCH_CLOSE` and `RIGHT_LAUNCH_CLOSE`.** The table's rows start
      at 42.4 in, so both CLOSE spots fire at an extrapolated speed (1459–1480 ticks/s in the
      Belding logs). The four measured rows fit one arc, which puts 36 in at about 1513 ticks/s
      (1458 to 1573 allowed by the fit). Run the loop above at `RIGHT_LAUNCH_CLOSE` and write the
      speed into `LaunchSpeedTable` row 5. **Good:** 4 of 4 stay in the CELL at the row's speed.
      [`doc/one-robot-two-tips.md`]

- [ ] **Camera range = pose range, from the turret axis** (since 5 Nov both are measured from the
      turret, and the table's rows were converted to match). Any Basic Drive with the camera on a
      CELL: graph `/Launch/CameraRangeIn` and `/Launch/PoseRangeIn` from the match log (an Auto
      writes both on its `launcher: speed table` line). **Good:** within about 1–2 in at the launch
      spots, and shots score as before at the same speeds. **Stop:** a steady gap of 3 in or more →
      the turret offset (`TurretSubsystem.turretForwardIn/LeftIn`, CAD from 24 Oct, never taped)
      or the camera mount; tape the turret axis from the wheels' midpoint first.

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

- [ ] **Square with the drive stick centred.** In 50 of 51 Square presses cancelled within 0.1 s
      at Belding, the stick was already moving when Square went down. Practise: stop steering,
      press and hold Square. **Good:** ten Square presses in a row, none "sticks moved early" in the
      log. The code side is #495.
- [ ] **Drive by hand once the Pinpoint is lost** (the Match page shows "Pinpoint lost"). A corner
      or camera reset cannot clear it (`PinpointLoss`: only a restart does), so Square and the FLOWER
      pick stay refused for the rest of the match, even though their refusal text suggests a reset.
      At Belding, 19429's driver kept pressing Square after the loss (8 presses in 14 s in Q13).
      Practise: the first refused press means stop pressing it, drive and aim by hand, and restart
      the robot after the match. **Good:** at most one refused press after a loss.
- [ ] **Operator: spin up before FIRE** when the next shot is coming, so FIRE finds the launcher at
      speed (first ball 0.8 s instead of 1.5 s). Until #497 decides the software side.
- [ ] **19429: a shorter route from full to the shot.** At Belding it drove a median 191 in with a
      full robot before shooting (20245: 97 in). Try filling nearer a launch spot, or firing from
      where it is. **Good:** a timed practice cycle under 20 s.

## 7g. Turret absolute zero: two Melonbotics encoders' analog outputs (turret by hand)

Two Melonbotics Through Bore Encoders on the 176T ring: A on the 64T gear, B on the 44T (the
mentor's CAD, 11 Nov 2026). Off on both robots until every value is in
(`TurretSubsystem.useAbsoluteZero`). [#169]

```
 64T encoder (A) 3-pin analog ──┐
                                ├─[ JST joiner board ]── 4-pin ──▶ Control Hub analog port "0-1"
 44T encoder (B) 3-pin analog ──┘                                   (A → input 0, B → input 1)

```

- [ ] **Wiring.** One joiner board per robot: both encoders' 3-pin analog cables into it, its 4-pin
      cable into the Control Hub's analog port **0-1**. Neither encoder's 4-pin cable is used: the
      turret's angle all match comes from the 64T's analog reading (`absAngleInMatch`, on). The
      Cypher Max comes out of the feeder's encoder socket and the feeder's own encoder goes back in
      (the turret no longer reads that socket). Full
      **TeamCode** install (the configs gained two analog inputs), activate the config, run
      **Check: Hardware**: `turretAbs64` and `turretAbs44` are AnalogInput.
- [ ] **Bench: Turret Absolute Setup**, the guided setup (turret by hand, nothing moves). It shows one
      step at a time and moves on by itself: wiggle the turret (both readings move: both cables
      in); turn it slowly about a full turn (each encoder's voltage range, 2 wraps each); let go,
      then turn it counter-clockwise about 45° (directions, and B's movement against A's checks the
      2.75 and 4.0 ratios and that A and B are not swapped on the joiner board); on the home mark,
      Cross (home readings); then the check: a quarter turn counter-clockwise reads about +90°,
      back on the mark about 0°, Cross. **Done:** it shows the `RobotDifferences` lines for this
      robot, also on Panels (copy them from the laptop) and in the Robot Controller log. Paste them,
      Sloth Load. Run it on the home mark five times and note the home readings' spread.
      **Stop:** a step that will not finish says why on the screen; "−90°" in the check means step
      3 was turned clockwise.
- [ ] **If the 64T's 4-pin cable is in the feeder's socket:** **Bench: Turret**, one full turret turn
      by hand reads 2.75 encoder turns. A Melonbotics encoder does not count 2048 per turn as the
      Cypher Max did, so expect another number until `TurretEncoder.COUNTS_PER_ENCODER_TURN` is set
      from it.
- [ ] **Bench: Turret Absolute.** In INIT turn the turret by hand through several turns, and push the
      robot round. **Good:** "Absolute pair", "1 · Count" (if its cable is in) and "2 · Absolute
      only" agree within a degree or two at every angle; "Points" changes by the robot's turn plus
      the turret's; with the turret aimed by eye at our raised CELL, "turret off by" reads within
      2°, and Panels' green line lands on the CELL. Watch "2 · Absolute only" sit still with the
      turret still (its noise) and follow a fast hand turn. If it jumps, put the 64T's 4-pin cable
      in the feeder's encoder socket and turn `absAngleInMatch` off for that robot.
      **Stop:** a source that jumps by tens of degrees → a wrap missed, or a value above wrong.

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
- [ ] **19429 keeps no NECTAR** (`ColorSensorSubsystem.rejectAllNectar`, on for 19429 only in
      `RobotDifferences`, 7 Nov). Basic Drive on 19429, red then blue: feed in a red NECTAR, a blue
      NECTAR and a POLLEN, five times each. **Good:** both NECTAR colours come back out on either
      alliance, every POLLEN is kept, and the match log's Metadata lists the line under
      `RobotDifferences`. **Stop:** a POLLEN spat out → narrow the NECTAR hue ranges as above; to
      let 19429 keep NECTAR again, Panels `rejectAllNectar` off (or delete its line).
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
[PR #425]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/425
[PR #427]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/427
[PR #430]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/430
[PR #492]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/492
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

### Full Solo-Nudge lab run

- [ ] Load master with the full nudge trial; select **Drive: R-Solo-Nudge**.
  Normal RIGHT_START, four preloads, the same angled 16x16 dummy placement, chosen
  alliance. Keep **Drive: Partner Nudge** available for push-only setup checks.
  Film one complete run and collect its log: fourth-shot completion, gentle contact,
  partner park/wall clearance, second tip and our park before the 30 s stop.
  Contact approach and push use `PartnerNudgeTest.pathSpeed`, read at INIT; set it
  first with **Drive: Partner Nudge**. A blocked contact stops after six seconds.
  After the run, drive R-Solo once to confirm normal paths are at full speed. [PR #492]
[#504]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/504
