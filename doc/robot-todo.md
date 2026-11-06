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
4. **L-Infraviolet / R-Infraviolet.** Refine one coordinated three-TIP pair after the
   baseline. The A/B drafts remain candidate routes; do not register placeholders.

- [ ] **After loading the lineup cleanup:** the Driver Station's Autonomous menu has
      exactly L-NoCross, R-NoCross and R-Solo (with their save timestamps). Preload,
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
- [ ] **Pinpoint `FAULT_BAD_READ`:** logged 14 times in the 3–4 Nov TeleOps (none in an Auto).
      Reseat the Pinpoint's I2C cable and check its strain relief. **Good:** a 5-minute Basic Drive
      with none. **Stop:** still there → swap the cable, note it on the hub photo (§8).

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
      **Good:** one line `partner's last shot ▲▼ 7 s, waiting until 11 s`, amber; down three times
      turns it back green, as drawn (last shot 4 s, waiting until 8 s); D-pad left and right change
      nothing (no Auto offers another park); nothing else on the robot reacts to gamepad 1's D-pad
      in INIT. **Stop:** the line does not change, or something else moves → save the match log.
      [PR #391]
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
- [ ] **19429: the turret re-homes from its encoder** (`TurretSubsystem.rehomeFromEncoder`, on for
      19429). Needs `degreesPerUnit` first (below). Basic Drive: in INIT twirl the turret three
      turns, put it on its mark, PLAY. **Good:** it swings off, then within about a second comes
      back to the mark; the Robot page's "Turret re-home" line shows the corrections. **Stop:** it
      swings further off → `degreesPerUnit`'s sign is wrong. "out of reach" → one more full turn
      by hand, on the mark, run again.
- [ ] **19429: before the turret is unlocked** (`lockedForward` false, the mentor's call). On
      **Bench: Turret** after PLAY: Square at home, step at least 0.1 away: the bench shows
      `degreesPerUnit` from the encoder (19429's 2.75 encoder ratio is applied); step both ways.
      First the ratio: Circle (limp), Cross at the home mark, turn the turret by hand to a 45° mark
      (no further: winding a position servo past its range is what moves home): `angle` ≈ 45°,
      else 19429's `turnsPerTurretTurn` (2.75) is wrong. Then set `degreesPerUnit`, the real
      limits (where the turret stops turning, less a margin) and unlock.
- [ ] **20245: the turret servo's mode and its encoder socket.** On the 24 Oct bench its servo was
      in continuous mode; the code now sends a position, and keeps it at 0.5 (stop, or centre) until
      this is known. **Bench: Turret**, PLAY, one D-pad press: the turret steps and stops → position
      mode, find its straight-ahead position like 19429's; it keeps turning → continuous mode:
      **stop the OpMode**, and either reprogram it to position mode or tell a mentor. Also: which
      cable is in the motor port 3 encoder socket (the feeder's, or the Cypher Max's).

## 3. The selected Autos, on the field (HIVE upright, preloads in, Panels open)

Use the work order above to select checks; the detailed mechanism checks below are not all Belding gates. Run each from its start, alliance from §2.

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
- [ ] **One park** (parks moved 4.7–4.9 in, now 14.5 in off the wall). **Good:** part of the robot
      inside the LOADING ZONE. [PR #373]
- [ ] **Stick down on the drive in:** spotters at WALL_FLOWER_STAGING and SCORING_FLOWER_STAGING,
      R-Solo, two picks each. **Good:** the stick swings down about 18 in before
      the robot stops and touches nothing before the push. **Stop:** it touches → Panels
      `FlowerPicker.lowerWithinIn` 12, write it back. [S5 seam §2], [PR #378]
- [ ] **The stick lowers earlier at the WALL FLOWER:** the 3–4 Nov Auto picks read `prep done on
      the way in` 0.52–0.55 s at the WALL FLOWER (FAR FLOWER 0.80–0.84), under the 0.7 s this check
      wanted, so its Stop applies: Panels `FlowerPicker.lowerWithinIn` 24, then the stick line above.
      **Good:** X ≥ 0.7 at both FLOWERs and the stick still touches nothing. Write 24 back if so.
      [S5 seam §4], [PR #378]
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
- [ ] **The CellUp wait with a real tip:** L-NoCross with a partner (or a hand) tipping
      (R-NoCross's ended on the trigger after 3.1 s on 3 Nov). **Good:** "Wait for TIP or CELL up" ends on the trigger, not its time limit.
      [PR #377]
- [ ] **CellUp with the CELL already up:** L-NoCross at `LEFT_LAUNCH_CATCH` with the left CELL
      tipped up by hand first (or a partner's tip over before the wait starts). **Good:** the wait ends
      at once and the Auto fires, not at its limit (8 s on the match clock since #391). Then with the
      CELL **down** and nobody tipping: it waits out its limit and fires anyway (the Auto's own choice for now). **Stop:** it waits with
      the CELL up, or fires at once with it down → save the match log; check which CELL the robot
      aims at from its end of the field (`AutoRegistration.aimedLeft`). [PR #377]
- [ ] **TIP lag, from the same runs' logs:** seconds from the partner's (or R-NoCross's own)
      last preload leaving the launcher (the feed's end) to the CellUp trigger. **Good:** 1.5–5 s,
      the simulator's range (it read 0.7–1.2 s with four preloads on the starting NECTAR); a few runs
      agree → that number replaces the 4 s estimate in `AutoRegistration.PARTNER_TIP_LAG_S` and
      `partnerTipLagS` in `TeamCode/autos/timing.json`, together. **Stop:** more than 5 s → the HIVE
      dwell is longer than filmed; record the load in the CELL. [PR #387], [branch claude/funny-goldberg-w0lyw7]
- [ ] **L-NoCross's TIP wait on the match clock:** partner's last shot set to 7 s in INIT (waiting
      until 11 s), nobody tips.
      **Good:** the trace shows `Wait for TIP or CELL up: 11.0 s on the match clock` about 11 s into
      the match, then the no-TIP route. **Stop:** it ends at 8 s or counts from its arrival → save
      the log. [PR #391]
- [ ] **Early park from L-NoCross's no-TIP route:** nobody tips, partner's last shot set late (11 s or more)
      so time runs short at RIGHT_LAUNCH_CATCH or WALL_FLOWER_STAGING.
      **Good:** "parking from the spot the robot is on", the park from RIGHT_LAUNCH_CATCH bending west
      of the Red HIVE, parked by 30 s. **Stop:** it touches the HIVE → note the pose; bend that park
      (P on the step) in the Visualizer. [PR #391]
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
- [ ] **Short nudge, first in Basic Drive with the actual partner.** The revised draft
      and `partner-push-start.pp` now agree: partner nominally (8, 84), heading 90°,
      touching the alliance wall and outside the loading zone; our contact pose
      (10.5, 67.8), heading 270°. Place by the real frame/wall contact and measure the
      contact gap; the drawing and simulator use different frame dimensions.
      **Good:** the partner rolls straight toward the zone, a short push puts its
      body partly over the tape, our loaded robot retains all four pieces, and the
      east/south peel-off releases contact before curving north. Record the actual
      push distance, approach speed, whether the partner turns, and time cost.
      The 300 ms hold and 4.2 in path are not a numerical speed/force cap: test the
      contact behavior before loading the full automated draft. If the first
      contact is too hard, add a real local speed limit rather than changing only
      preview velocity. Then qualify the two-TIP sequence and separate near/far
      AUTO parks. The old 12 in diagonal-push procedure is superseded. The draft
      still has no selectable OpMode. [autos draft README](../TeamCode/autos/drafts/README.md#r-solo-nudge-for-a-partner-with-no-auto)

- [ ] **Infraviolet pair: L early garden, R both flowers.**
      [Candidate, reused paths, cutoffs and timing](../TeamCode/autos/drafts/README.md#l-infraviolet--r-infraviolet-coordinated-three-tip-draft).
      Sloth Load `feat/infraviolet-pair`; run **Utility → Drive: L-Infraviolet / Drive: R-Infraviolet**
      (automatic STOP at 30 s). The previous assignment is preserved in Git history.
      - Confirm L moves to LEFT_CATCH but never shoots before R's TIP 1 settles.
        Missing TIP 1 must send L to far park without collecting the garden.
      - Dry-run both south crossings and the separate parks. L must pass a robot
        already in RIGHT_PARK without touching it. Stop if either robot stalls;
        a dead L at its start obstructs R's reused LEFT approach.
      - Keep L at the existing RIGHT offset; drive R's final turn and park around
        it, checking actual frame/poker and centre-line clearance. The drawing's
        minimum centre-line margin near the shot is about 0.9 in, not a tracking tolerance.
      - Qualify R's new local RIGHT_PAIR_CLOSE shot (60.5, 31, about 99.4 degrees;
        turret range about 30 in). L's shared RIGHT offset is unchanged. Check
        camera visibility and the shot line with both robots in position.
      - Test loaded exits after the normal pickup retreat. The long under-HIVE
        crossing is forward; R's final shot alignment still has a short backward
        component. Tune/test retention there before trusting the fourth piece.
      - Measure R's scoring-flower readiness before **17 s** and final RIGHT
        readiness before **22 s**; L's final RIGHT readiness before **20 s**.
        Test missed TIP 2 and late arrivals: skip the reload/volley and take the
        assigned park. The 1 s launch timeouts limit readiness, not an active feed.
      - Run the complete pair, including mismatched drive speeds and missed volleys.
        Three TIPs plus both parks needs faster-than-current timing in the model.
        Confirm both LEAVEs at AUTO end and both parks on video before promoting
        the Utility trials to competition OpModes.

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
- [x] **Done on red at all six spots (mentor, 5 Nov); the two CORNER spots and blue are checked
      in §7d and §7e.** **Go to launch spot (driver Square, hold) vs chassis aim (Circle):** pose set (corner or
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
      15–30 in off a spot; then press operator FIRE during a press; then let go mid-feed; then
      operator launcher toggle to idle mid-press. **Good:** launcher spins up as it drives, one
      volley once there (`launch spot: gate open … at speed, settled`), all four leave; the end line
      says `fired by button`, shots counted ≈ 4; FIRE takes the shot (`fired by … operator`); let go
      stops the feeder at once; an idled launcher stays idle. **Stop:** it feeds before it is
      aimed, or feeds after let go → `goToSpotAlsoFires` off. [PR #423]
- [ ] **Every press is a drift sample:** across a practice, pull the logs and read each press's
      `launch spot: drift at …` line (camera − odometry at a known spot, settled, never applied) and
      its `CELL N in` range beside the shots it fired. **Good:** gaps stay under
      `TeleOpConfig.spotDriftWarnIn` (3, not measured) through a 2-minute TeleOp, and the range/shot
      pairs add to #80's speed-vs-distance table beside §7d's rows. **Stop:** gaps grow
      over the match → the evidence for TeleOp relocalization; set `spotDriftWarnIn` from what the
      drivers can act on. [PR #423]
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
- [ ] **19429's prefeed does not respond** (6 Nov, late; left for 7 Nov). It is a goBILDA Torque
      servo in continuous mode on **Control Hub servo port 2** (`robot_19429.xml`). Check in order:
      the cable is in port 2 the right way round (black/brown to the hub's "-" side); the servo
      runs on a servo tester or another port; it is still in **continuous** mode (the servo
      programmer was in use for the turret that night); Check: Hardware lists `prefeed`.
      **Good:** it spins inward in Basic Drive's intake. Without it, pieces do not reach the feeder.
- [ ] **19429's intake encoder cable is not installed** (seen 6 Nov; note left for the builders).
      Every Basic Drive log says "intake: encoder never moved"; the roller runs, but nothing can
      see a jam. Plug it into the Expansion Hub's encoder socket 0, beside the intake's power port
      0, then run the intake in Basic Drive. **Good:** that line is gone from the next log. Check
      20245's too: its picks logged the same line.

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
- [ ] **Is the shot count good enough to end a feed?** Since 5 Nov each wheel's dip is measured from
      that wheel's own speed just before, 60 down (it was 70 below target, and the wheels settle
      about 40 apart, so R-Solo 23:41 counted 2 of the 4 shots its video shows). Replayed over the
      3–4 Nov logs it never counts more than 4 in an Auto volley; only video can say it is right.
      After the PIDF and feed pace above, fire 10
      volleys of 4 the way an Auto does (`LaunchAll`, its whole 3.5 s feed), with a phone filming
      the launcher. Per volley, write down the count (Launch Test page, or `/Launcher/Shots` in the
      log) and the balls that left (video). The two mistakes cost different things: a count too low
      only keeps the fixed feed, a count too high would end a feed with a ball still in the robot.
      **Good:** no volley counted more than left, and at least 8 of 10 counted exactly 4. Then
      `LaunchAll` can end its feed at the 4th counted shot, with the 3.5 s feed kept as the backstop:
      0.5–1.5 s back per volley (the last ball leaves 1.7–3.0 s after the gate opens). **Stop:** any
      over-count: note the volley's time and leave the feed fixed; a piece sensor (#164, #181) is
      the other way to know the robot is empty. Read off the same 10 volleys how long the gate's
      opening took to the first dip: a long one means the first ball is not waiting at the wheels.
      [#433; mentor, 4 Nov 2026]
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

**The loop, at each spot:** with `LaunchSpeedTable.enabled` off (the Match page says
"Speed table: OFF"), hold Square. The robot drives to the nearest launch spot and fires one volley.
On a miss, the operator trims the speed up or down, then Square again, until it scores. The Match
page's "Last launch spot" line shows the spot's CELL range and the launch speed: write both down.
Take the middle of the speeds that score 4/4, not the first one that does. The trim carries from
press to press.

Measured 4 Nov 2026 (20245, tuned PIDF) and in `LaunchSpeedTable` with the table on: 42.4 in → 1520,
45.7 → 1560, 53.9 → 1600, 58.8 → 1635 (ranges from the turret axis since 5 Nov; first written from
the wheels' centre as 39, 42.3, 50.5, 55.4). What is left here is the corner spot for Square.

- [ ] **Square's corner spots:** `RIGHT_LAUNCH_CORNER` (30.2, 10.7, 54°) is where 20245 scored the
      58.8 in / 1635 row (55.4 from the wheels' centre) on 4 Nov (about 50 shots in that evening's log); `LEFT_LAUNCH_CORNER`
      (30.2, 130.8, 302.6°) is its place mirrored across the axle line, aimed at the LEFT CELL,
      never fired from. RIGHT's 54° points the turret about 7° right of the CELL by the field model
      (61° would aim it): compare 54° and 61° on video once. Square drives to
      either when it is the nearest spot. **Good:** from near each corner, Square ends there and
      scores 3–4 of 4; the drive in from the GARDEN side touches nothing (it is one straight path).
      **Stop:** the left one goes long or short → note the range on the Match page; the left corner
      is also where a partner may leave its POLLEN pile. [mentor, 5 Nov]

- [ ] **Camera range = pose range, from the turret axis** (since 5 Nov both are measured from the
      turret, and the table's rows were converted to match). Any Basic Drive with the camera on a
      CELL: graph `/Launch/CameraRangeIn` and `/Launch/PoseRangeIn` from the match log (an Auto
      writes both on its `launcher: speed table` line). **Good:** within about 1–2 in at the launch
      spots, and shots score as before at the same speeds. **Stop:** a steady gap of 3 in or more →
      the turret offset (`TurretSubsystem.turretForwardIn/LeftIn`, CAD from 24 Oct, never taped)
      or the camera mount; tape the turret axis from the wheels' midpoint first.

## 7e. Blue alliance (never run on blue yet; HIVE and FLOWERs on the blue side)

Everything blue is red turned half a turn about the field centre, in code and unit-tested
(`LaunchSpotsTest.blueIsRedTurnedHalfATurn`, `NectarRejectTest`), but no robot has run it.

- [ ] **The alliance reads BLUE before PLAY.** Basic Drive with no Auto before it **defaults to
      red** ("RED (default)" on the Match page) unless vision proposes blue or someone presses B.
      The NECTAR reject takes the alliance at PLAY, so a blue robot left on the red default spits
      out **our own** NECTAR and keeps the red. **Good:** "BLUE (vision)" or "BLUE" after B, every
      time; the drive team checks it before PLAY. **Stop:** vision proposes red on the blue side →
      save the log.
- [ ] **NECTAR reject on blue:** intake a red NECTAR → it is spat out; a blue NECTAR and POLLEN →
      kept. **Good:** both, three times each.
- [ ] **Square on blue, each spot once:** from near each blue spot, hold Square. **Good:** it drives
      to the blue mirror of the red spot, faces the blue CELL it serves, and the volley scores.
      **Stop:** it goes to the wrong half or faces away → note the pose; never across the centre
      line (it refuses there).
- [ ] **Corner reset on blue** (the reset corners are measured on red and turned): hold the reset
      button at a blue corner. **Good:** the pose on the Robot page matches the corner, field-centric
      forward is right.
- [ ] **One Auto on blue** (R-Solo from blue's audience start). **Good:** it mirrors the red run.

## 8. If time: anyone, no driving

- [ ] **Photograph both robots' hubs** for `doc/wiring/`, ports against the XML. [#33]
- [ ] **Launcher: the exit angle and the exit height**, robot off: a protractor on the launch path
      where the ball leaves the wheels, and a tape from the floor. **Good:** both written in the PR
      that replaces the simulator's placeholders (`FieldSim.SETPOINT_PITCH_DEG` 75°, exit height
      17 in). **Stop:** if the angle changes with the wheel speed (a flexible hood), say so instead.
      [#429]
- [ ] **HIVE: count the pieces in the CELL at each TIP** (no robot: our practice videos and online
      match videos). For each TIP: the POLLEN and NECTAR in the CELL just before it moved, whether it
      tipped on a landing piece (and how many landed in the last second), or on a piece placed or
      rolling in. **Good:** 20 TIPs or more, each with its count. **Stop:** a CELL that tips on fewer
      than 7 POLLEN weights (NECTAR is 1.65) from hits is the number the simulator's hit strength
      (`FieldSim.PLACEHOLDER_IMPULSE_SCALE`, a placeholder) is refitted to; record each count in the
      PR. [#429]

## 9. From PRs merged 29 Oct–1 Nov, not in any session doc

Small checks the PRs asked for. Several likely ran at the 31 Oct–2 Nov sessions: strike what did.

- [ ] **Auto: Hold Still**, red, through PLAY: no FAULT (1 Nov it was INIT only). [S1]
- [ ] Turn exactly 180° in place at the red start: pose moves < 0.5 in. [PR #275] (*#330 re-measured
      the pods on 1 Nov after they moved: likely done*)
- [ ] Reset holds are 0.5 s and buzz gamepad 1 (turret zero: gamepad 2). If a reset gives no buzz,
      the match log's `rumble:` line says the code sent it, and `Controls/Gamepad1` in Metadata
      names the pad type: swap in another controller (or charge this one) before changing code.
- [ ] AdvantageScope 3D: the stick swings, the rocker tips the right way; `make-viewer-zip.cmd` and
      `install.cmd` on Windows. [PR #294], [PR #299]
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
- [ ] `IntakeSubsystem.holdPowerWhenFull` is 0 (roller off when full, stars still turning; the drive
      team's choice after trying 0.2). With 4 loaded: back away fast and turn, toggle off and drive,
      and an Auto that idles after `IntakeFull`: the 4th stays in each time. If it rolls out, raise it
      in Panels (0.2 was tried) and check a 5th is not pulled in.
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
