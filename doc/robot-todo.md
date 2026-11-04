# Robot to-do: everything waiting on the real robot

**This is the only list of what waits on the robot.** A finished item comes off this list (delete
the line in a PR) and its result goes to the issue or PR in its source link, not here. A PR that
needs a robot check adds a line here, in the group with the same setup, instead of writing a new
session doc. Long procedures stay in their session doc or PR; this list links to them.

Built 3 Nov 2026 for the mentor's lab visit, from `doc/robot-session-1.md` to `-5.md`, the open
`needs:robot` issues, and the robot checks in PRs merged since 29 Oct. **No session doc checkbox
was ever ticked**, so nothing here is assumed done. Where the repo has evidence that an item already
ran, the line says *likely done* and why: strike it if so.

Each line: what to do · **Good:** what passing looks like · **Stop:** when to stop and what to
record · source. `S4 §2` means `doc/robot-session-4.md`, "At the robot", step 2.

20245 throughout unless a line says 19429. The Autos on master are R-Solo, R-Solo-Staging,
R-Partner-Preload, R-Partner-Tip and L-Partner (#375 removed R-Simple, R-Quals, L-Simple, L-Quals).
Starts: **Red audience** (`RIGHT_START`, tile C1) for the R- Autos, **Red far wall** (`LEFT_START`,
tile C6) for L-Partner. Place by the marks, never by the camera.

## 0. Before leaving the laptop

- [ ] Master pulled. A **full TeamCode install** if this robot has had none since 2 Nov (the
      indicator light added `res/xml`); Sloth Load after that. [S4 Before], [S5 Before]
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
      then offline change one step in one of the five Autos, **Export → Export Auto (Java)** and
      **File → Save to computer**, drop both into the checkout (`generated/` and `TeamCode/autos/`),
      Sloth Load over USB. **Good:** the Visualizer opens and exports offline, the robot runs the
      change. **Stop:** the offline copy does not open → open it online once more and re-check; note
      what failed. Commit the two files on an `auto/<name>` branch afterwards. [PR #374]

## 1. Hardware is all there (5 min)

- [ ] **Activate `robot_20245`, run Check: Hardware.** **Good:** nothing reported. **Stop:**
      anything reported; fix it before the rest. [S3 §2], [S4 Before]
- [ ] **Indicator light** on Control Hub servo port 4 and **intake encoder cable** plugged in.
      **Good:** Check: Hardware sees `indicator`; the log's `/Intake/TicksPerSecond` is not 0 and
      there is no "intake: encoder never moved". [S4 Before]

## 2. Any Auto, INIT only, no PLAY (about 20 min; camera connected, HIVE upright)

**INIT names the start the robot is on** ([PR #372]; the 10 in match radius is untested). Pick
the Auto, place the robot on the marks, INIT, press **no** alliance button. 5 frames settle a match,
about a second. Written out on branch `claude/eager-pasteur-f4trol` (`doc/robot-session-5.md`, last
section, not on master); this is that section.

- [ ] **R-Solo on Red audience.** **Good:** the Alliance line turns green `RED (vision)` within a
      few seconds, no "On …, not this Auto's start" line, start check confirms. **Fail:** it stays
      `NONE — press Square…`.
- [ ] **L-Partner on Red far wall.** Same pass and fail.
- [ ] **R-Solo on Red far wall.** **Good:** "On Red far wall, not this Auto's start · … or move to
      Red audience", "No alliance" above it. Press Circle (red): "move to" still says Red audience.
- [ ] **L-Partner on Red audience.** **Good:** "On Red audience, not this Auto's start · … or move to
      Red far wall", and the plain "Not on the … start" line does **not** also show, even after 10 s.
- [ ] **Off every start:** R-Solo one tile out from Red audience, same heading, then L-Partner one
      tile out from Red far wall. **Good:** no "not this Auto's start" line; after 10 s, "Not on the
      Red audience start" (resp. "Red far wall").
- [ ] If the field allows: **R-Solo on Blue far wall.** **Good:** `BLUE (vision)`. (Blue audience
      wall is computed, not measured: say if it matches the wrong start.)

**Stop:** on its own start an Auto shows "No alliance" for more than 5 s with tags in view. Save that
run's match log: the 10 in radius is too tight for placement plus camera error. Read the start-check
gap with `tools/pull-logs/read_logs.py`; don't widen past 11.8 in (half of 23.6) without the logs.
Result, with the gap on each own-start run, → [PR #372].

- [ ] **INIT options, L-Partner on Red far wall:** gamepad 1 D-pad up three times.
      **Good:** one line `partner's last shot ▲▼ 7 s, waiting until 11 s`, amber; down three times
      turns it back green, as drawn (last shot 4 s, waiting until 8 s); D-pad left and right change
      nothing (no Auto offers another park); nothing else on the robot reacts to gamepad 1's D-pad
      in INIT. **Stop:** the line does not change, or something else moves → save the match log.
      [PR #391]
- [ ] **No Pinpoint warning** at a normal Auto INIT. **Good:** none listed. [PR #369]
- [ ] **INIT with the Limelight unplugged** (and a low battery if one is handy). **Good:** light
      flashes white, "Camera: not answering" at the top of the Match page with what to do, the page
      fits without scrolling; right after a restart, "no frames yet" until the first frame.
      [PR #353], [PR #281], [S4 §9]
- [ ] **INIT with the colour sensor unplugged.** **Good:** its warning shows, the light flashes white.
      [PR #354]
- [ ] **Light looks:** Basic Drive INIT steady red, X → steady blue; off after PLAY; intake until a
      piece sits under the colour sensor → green after about 1 s and the roller stops. **Stop:** a
      wrong colour → set it in Panels → IndicatorSubsystem, note the value. [S4 §9], [PR #353]

## 3. The five Autos, on the field (HIVE upright, preloads in, Panels open)

These gate Belding. Run each from its start, alliance from §2.

- [ ] **Loop Time Baseline, then one Auto run.** `/Robot/LoopMs` median and p95 (2 Nov R-Quals:
      26.7 / 47.7 ms). → [PR #378]. [S5 seam §1]
- [ ] **The Autonomous list on the Driver Station shows exactly the five** and nothing named
      R-Simple, R-Quals, L-Simple or L-Quals. **Good:** five entries, the names above. **Stop:** an old
      name is listed → a full TeamCode install, then look again. [PR #375]
- [ ] **Each Auto once:** R-Solo, R-Solo-Staging, R-Partner-Preload, R-Partner-Tip, L-Partner.
      **Good:** runs to its park without a FAULT; on the lane under the HIVE (x 59–62) the robot
      clears the brace that R-Quals hit at x 57.5 on 29 Oct. **Stop:** it touches the HIVE leg or
      brace → stop running that Auto, note where (log pose); it is a Visualizer fix. [PR #374]
- [ ] **R-Solo-Staging's pile:** four POLLEN on the floor at `PARTNER_STAGED_POLLEN` (36, 124), the
      robot on Red audience as for any R- Auto, run it.
      **Good:** the robot drives to the pile, the intake takes four, "Wait for IntakeFull" ends on the
      sensor, not its 3 s limit. **Stop:** it misses or pushes them → measure where a real partner
      stages, then move the spot with Save team spots (it re-exports the Autos). The pile's place is a
      guess until a partner says. [PR #373], [PR #374]
- [ ] **One park** (parks moved 4.7–4.9 in, now 14.5 in off the wall). **Good:** part of the robot
      inside the LOADING ZONE. [PR #373]
- [ ] **Stick down on the drive in:** spotters at WALL_FLOWER_STAGING and SCORING_FLOWER_STAGING,
      R-Solo or R-Partner-Preload, two picks each. **Good:** the stick swings down about 18 in before
      the robot stops and touches nothing before the push. **Stop:** it touches → Panels
      `FlowerPicker.lowerWithinIn` 12, write it back. [S5 seam §2], [PR #378]
- [ ] **Push starts while still slowing (≤ 8 in/s).** **Good:** push ends `reached` or `stalled`,
      never `timed out`; no more square-ups than 2 Nov. **Stop:** rough push → `settledInPerSec` 5,
      one change at a time. [S5 seam §3], [PR #378]
- [ ] **The gap, from the logs:** per pick, `path end` → `PREP … then IN`. **Good:** 0.1–0.2 s when
      square (was 0.70–0.73); `prep done on the way in, X s ago` with X ≥ 0.7 on most picks.
      **Stop:** X < 0.7 → `lowerWithinIn` 24. [S5 seam §4], [PR #378]
- [ ] **Time one GARDEN gather** (R-Solo or R-Partner-Tip): the `Last GARDEN gather: N s` line
      replaces the 6.0 s guess in `AutoRegistration.GARDEN_GATHER_TYPICAL_S`; the same runs' `FLOWER
      pick: done` lines say how often a pick ends on `full`. Say whether the corner allows a stick
      approach for the gather. [S5], [PR #384], [PR #378]
- [ ] **The CellUp wait with a real tip:** L-Partner with a partner (or a hand) tipping, and
      R-Partner-Tip. **Good:** "Wait for TIP or CELL up" ends on the trigger, not its time limit.
      [PR #377]
- [ ] **CellUp with the CELL already up:** L-Partner at `LEFT_LAUNCH_CATCH` with the left CELL
      tipped up by hand first (or a partner's tip over before the wait starts). **Good:** the wait ends
      at once and the Auto fires, not at its limit (8 s on the match clock since #391). Then with the
      CELL **down** and nobody tipping: it waits out its limit and fires anyway (the Auto's own choice for now). **Stop:** it waits with
      the CELL up, or fires at once with it down → save the match log; check which CELL the robot
      aims at from its end of the field (`AutoRegistration.aimedLeft`). [PR #377]
- [ ] **TIP lag, from the same runs' logs:** seconds from the partner's (or R-Partner-Tip's own)
      last preload leaving the launcher (the feed's end) to the CellUp trigger. **Good:** 1.5–5 s,
      the simulator's range (it read 0.7–1.2 s with four preloads on the starting NECTAR); a few runs
      agree → that number replaces the 4 s estimate in `AutoRegistration.PARTNER_TIP_LAG_S` and
      `partnerTipLagS` in `TeamCode/autos/timing.json`, together. **Stop:** more than 5 s → the HIVE
      dwell is longer than filmed; record the load in the CELL. [PR #387], [branch claude/funny-goldberg-w0lyw7]
- [ ] **L-Partner's TIP wait on the match clock:** partner's last shot set to 7 s in INIT (waiting
      until 11 s), nobody tips.
      **Good:** the trace shows `Wait for TIP or CELL up: 11.0 s on the match clock` about 11 s into
      the match, then the no-TIP route. **Stop:** it ends at 8 s or counts from its arrival → save
      the log. [PR #391]
- [ ] **Early park from L-Partner's no-TIP route:** nobody tips, partner's last shot set late (11 s or more)
      so time runs short at RIGHT_LAUNCH_CATCH or WALL_FLOWER_STAGING.
      **Good:** "parking from the spot the robot is on", the park from RIGHT_LAUNCH_CATCH bending west
      of the Red HIVE, parked by 30 s. **Stop:** it touches the HIVE → note the pose; bend that park
      (P on the step) in the Visualizer. [PR #391]
- [ ] **PickFlower squares up** from a crooked arrival. **Good:** it turns square before it pushes.
      [PR #313]
- [ ] **A launch's time limit never cuts a feed;** the Auto graphs the launcher in Panels. **Good:**
      every launch feeds its full `LaunchCards` time; launcher graph live. [PR #333], [PR #334]

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

## 5. Pose and camera (Basic Drive / Vision: HIVE & Pose)

- [ ] **Loop time, named:** Basic Drive 2 min, intake on part of it, a couple of picks; then Loop
      Time Baseline. **Good:** `/Loop/Subsystem/<Name>Ms` names the slow update (2 Nov: 41 ms mean;
      colour sensor 5.5, drive 9.6, feeder 4.6). → the PR or issue for loop time. [S4 §5], [PR #365]
- [ ] **Camera calibration, R1 at two spots** (Vision: HIVE & Pose, Red audience then Red far wall).
      **Good:** "Spots so far: AGREE" → commit the two numbers to `CameraMount` (code 3.55 / 0.95; 2 Nov
      read 3.13 / 0.89). **Stop:** DIFFER → a HIVE point or the marks, don't commit.
      [handoff](../.claude/handoff/20245-camera-calibration.md), [PR #360]
- [ ] **Heading-aware camera reset:** lift the robot, turn ~15°, set it down off the mark, still,
      hold D-pad down. **Good:** heading corrected, field-centric drives right again. [handoff], [PR #360]
- [ ] **Lost-pose reset:** away from the start, still, both HIVEs in view, hold D-pad down 0.5 s.
      **Good:** "relocalized (…) whole pose (lost: X in off)". [PR #339]
- [ ] **Marks in Panels:** grey crosses on the starts and reset corners; park on 3–4 marks over a
      match-length drive and note `shadow_offset_in`. [PR #361]
- [ ] **Panels drawing:** X then B in INIT turns the outline blue → red; each FieldLayers switch
      shows and hides its robot. [PR #356], [PR #307]

## 6. Tape measure and level (camera geometry)

- [ ] **Tag size.** Pipeline 82.55 mm (*likely done: read on 31 Oct, per the handoff*); measure a
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

- [ ] **Bring-up, in #175's order:** Check: Hardware, Drive Motor Check (lifted), Mecanum, Pinpoint
      and Foresight tuners pasted into `Robot19429.java`, Tests procedure, Loop Time Baseline.
      **Good:** path Autos no longer stop at INIT on Foresight. [#175], [S2 §8]
- [ ] **Anything 19429 does differently** from 20245 (a motor turning the other way, a servo
      position, the intake sign): one line in `RobotDifferences.java` under `TEAM_19429`, with what
      was seen and the date, then Sloth Load. Never edit the shared value: that changes 20245 too.
- [ ] **Its log lands in its own folder** after a pull (`biobuzz-logs\<19429's network>`). [S2 §8]
- [ ] **Copy 20245's Limelight pipeline** and run the 5-minute start check. [handoff] step 6
- [ ] **Drive check table** (10 steps), on both robots. [#62]

## 8. If time: anyone, no driving

- [ ] **Photograph both robots' hubs** for `doc/wiring/`, ports against the XML. [#33]

## 9. From PRs merged 29 Oct–1 Nov, not in any session doc

Small checks the PRs asked for. Several likely ran at the 31 Oct–2 Nov sessions: strike what did.

- [ ] Turn exactly 180° in place at the red start: pose moves < 0.5 in. [PR #275] (*#330 re-measured
      the pods on 1 Nov after they moved: likely done*)
- [ ] Corner resets: D-pad left/right in a red corner reads ~(8.1, 133.4) / (8.1, 8.1); Triangle held
      1 s in INIT starts fresh. [PR #274], [PR #277] (*likely done: S4's intro says both work*)
- [ ] Corner reset is the gyro reset too: hold D-pad up with the robot turned about 45° (forward now
      wrong), then hold D-pad left squared in the red left corner. **Good:** the Match page says
      `forward reset`, and stick-up drives away from the red drivers without touching D-pad up.
- [ ] Reset holds are 0.5 s and buzz gamepad 1 (turret zero: gamepad 2). If a reset gives no buzz,
      the match log's `rumble:` line says the code sent it, and `Controls/Gamepad1` in Metadata
      names the pad type: swap in another controller (or charge this one) before changing code.
- [ ] Log contents: `/Drive/*`, `/Drift/*`, `/Vision/*` keys, a `push:` event at a wall, `jolt:` only
      at real knocks, Pinpoint ID in Metadata, a dwell reason on each pick. [PR #283], [PR #284],
      [PR #285], [PR #286]
- [ ] AdvantageScope 3D: the stick swings, the rocker tips the right way; `make-viewer-zip.cmd` and
      `install.cmd` on Windows. [PR #294], [PR #299]
- [ ] Intake sign: R2 about +2760 ticks/s, L2 about −1640. [PR #318]
- [ ] Star-wheel servo turned round on both robots (4 Nov 2026), `IntakeSubsystem.starsReversed` now
      true (`reversed` stays true): after a Sloth Load, R2 takes pieces in on 19429 and 20245, the
      roller reads positive ticks/s and the stars turn inward; L2 backs both out.
- [ ] Load 4, launcher at speed, hold FIRE: all 4 leave. [PR #320]
- [ ] R2 toggles the intake with its Panels switch on; feeder runs whenever intaking. [PR #321],
      [PR #327]
- [ ] Operator R2 toggles the intake (no Panels switch); driver R2 held runs it with the toggle
      off, either L2 held reverses it, and letting go returns to the toggle. Toggled on, load 4: at
      full the roller and the prefeed stop, the stars keep the 4th in, the Match page says "holding
      (lane full)", and the driver's R2 does not restart them; one shot and both start again.
      Sensor at slot 4: confirm `presentAbove`/`leaveBelow`/`fullHoldS` there, and that no piece
      merely pausing leaves it stuck "full" (if one does, `PrefeedSubsystem.stopWhenFull` off).
      (intake toggle + prefeed stop)
- [ ] `IntakeSubsystem.holdPowerWhenFull` is 0 (roller off when full, stars still turning; the drive
      team's choice after trying 0.2). With 4 loaded: back away fast and turn, toggle off and drive,
      and an Auto that idles after `IntakeFull`: the 4th stays in each time. If it rolls out, raise it
      in Panels (0.2 was tried) and check a 5th is not pulled in.
- [ ] Unjam with a ball on the launcher wheels: rolls back, launcher returns to speed. [PR #323]
- [ ] Launch Test on gamepad 1, operator layout. [PR #324]
- [ ] Turret locked: hold Circle after intaking, the chassis settles on the CELL. [PR #329]
- [ ] Controls page and printed sheet match what the robot does ("checklist coming separately").
      [PR #270]
- [ ] Pick routine, first-edition checks: "already lined up" when started lined up; an Auto pick
      logs "straight ahead (Auto)". GARDEN: measure red's gather and line-up poses for
      `GardenFieldPoints`. [PR #266], [PR #267]

## 10. Carried from sessions 1–3

Written for Autos #375 removed. Each line says what replaces it; strike what already ran.

- [ ] S1 §1–3: Check: Hardware, Pedro Tests procedure (hold/line/curve), Basic Drive directions →
      §1 and #62. [S1]
- [ ] S1 §4: **Auto: Hold Still**, red: no FAULT. Still on master. [S1]
- [ ] S1 §5–6, S2 §5–6, S3 §4–5: R-Simple / L-Simple / R-Quals runs, R-Simple TIP 1 in 3 of 3 with the
      speed and feed on #80, R-Quals' branch lines → now §3's five Autos; the turret question is
      answered (locked, #329). [S1], [S2], [S3]
- [ ] S1 §7, S2 §7, S3 §6: Loop Time Baseline → §3 and §5. [S1], [S2], [S3]
- [ ] S1 §8, S2 §1, §3, S3 §1, §7: pull a log without adb, see the lap in AdvantageScope, run
      `review.py` → *likely done: logs reach GitHub by `tools/pull-logs` since #335*. [S2], [S3]
- [ ] S3 §3: INIT-only placeholder lines → the five Autos' INIT, §2. [S3]
- [ ] S4 §8: re-run the Foresight Tuner on 20245 → *likely done: #331 pasted a re-tune on 1 Nov*. [S4]

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
[S2]: robot-session-2.md#at-the-robot-in-order
[S2 §1]: robot-session-2.md#at-the-robot-in-order
[S2 §8]: robot-session-2.md#at-the-robot-in-order
[S3]: robot-session-3.md#at-the-robot-in-order
[S3 Before]: robot-session-3.md#before-leaving-the-laptop
[S3 §2]: robot-session-3.md#at-the-robot-in-order
[S4]: robot-session-4.md#at-the-robot-in-order
[S4 Before]: robot-session-4.md#before-leaving-the-laptop
[S4 §1]: robot-session-4.md#at-the-robot-in-order
[S4 §2]: robot-session-4.md#at-the-robot-in-order
[S4 §3]: robot-session-4.md#at-the-robot-in-order
[S4 §4]: robot-session-4.md#at-the-robot-in-order
[S4 §5]: robot-session-4.md#at-the-robot-in-order
[S4 §6]: robot-session-4.md#at-the-robot-in-order
[S4 §7]: robot-session-4.md#at-the-robot-in-order
[S4 §9]: robot-session-4.md#at-the-robot-in-order
[S5]: robot-session-5.md
[S5 Before]: robot-session-5.md#before-leaving-the-laptop
[S5 At the robot]: robot-session-5.md#at-the-robot-in-order
[S5 §1]: robot-session-5.md#at-the-robot-in-order
[S5 §2]: robot-session-5.md#at-the-robot-in-order
[S5 §3–5]: robot-session-5.md#at-the-robot-in-order
[S5 §6–7]: robot-session-5.md#at-the-robot-in-order
[S5 seam §1]: robot-session-5.md#also-with-an-auto-on-the-field-the-drive-to-pick-seam-pr-378
[S5 seam §2]: robot-session-5.md#also-with-an-auto-on-the-field-the-drive-to-pick-seam-pr-378
[S5 seam §3]: robot-session-5.md#also-with-an-auto-on-the-field-the-drive-to-pick-seam-pr-378
[S5 seam §4]: robot-session-5.md#also-with-an-auto-on-the-field-the-drive-to-pick-seam-pr-378
[S5 seam §5]: robot-session-5.md#also-with-an-auto-on-the-field-the-drive-to-pick-seam-pr-378
[handoff]: ../.claude/handoff/20245-camera-calibration.md
[Visualizer PR #44]: https://github.com/Mona-Shores-FTC-Robotics/Visualizer/pull/44
[PR #262]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/262
[PR #266]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/266
[PR #267]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/267
[PR #270]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/270
[PR #273]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/273
[PR #274]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/274
[PR #275]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/275
[PR #277]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/277
[PR #279]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/279
[PR #280]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/280
[PR #281]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/281
[PR #283]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/283
[PR #284]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/284
[PR #285]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/285
[PR #286]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/286
[PR #294]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/294
[PR #299]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/299
[PR #307]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/307
[PR #313]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/313
[PR #318]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/318
[PR #320]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/320
[PR #321]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/321
[PR #323]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/323
[PR #324]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/324
[PR #327]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/327
[PR #329]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/329
[PR #333]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/333
[PR #334]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/334
[PR #339]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/339
[PR #353]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/353
[PR #354]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/354
[PR #356]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/356
[PR #360]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/360
[PR #361]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/361
[PR #365]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/365
[PR #369]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/369
[PR #372]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/372
[PR #373]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/373
[PR #374]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/374
[PR #377]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/377
[PR #378]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/378
[PR #384]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/384
[PR #387]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/387
[PR #391]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/pull/391
[#33]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/33
[#62]: https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/62
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
