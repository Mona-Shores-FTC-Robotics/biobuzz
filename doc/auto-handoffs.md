# Auto handoff notes, 16 Nov 2026

On 16 Nov 2026 the mentor set the Auto baseline (R-Solo, R-NoCross, L-NoCross, R-Clockwise,
L-Clockwise), retired the Infraviolet pair, and asked every chat then working on an Auto to stop
and write down what a fresh chat would need. These are those notes, as each chat sent them, lightly
tidied. Each later change to an Auto starts in a fresh chat from master with the note that
concerns it. Simulated numbers are the Java simulator's on master `d0f6363` unless a note says
otherwise; none is a robot's.

The baseline itself: the Autos launch from their start, an `AUTO_HIVE` spot or an `AUTO_OFFSET`
spot (`localization/AutoLaunchSpots`), and the turret is 2.68 in right of centre.

## Contingency Paths (the Simple builder)

**Unmerged:** nothing. One stray Visualizer branch, `claude/gracious-sagan-pjv1b8-deploy-fix`, can
be deleted.

**Keep:**
- Every volley after an Auto's first sits behind a CELL check: "Wait for CELL up" whose Otherwise
  parks, or, inside an Otherwise that goes on, skips the steps below. Kit side: `AutoKit.checkPlan`
  and the in-block volley self-check, tested in `AutoKitTest`. In the sources: `r-solo.pp` (0.75 s
  checks), `r-no-cross.pp` (until 22 s), `l-no-cross.pp` (until 23 s, with a nested "TIP seen?" then
  "RIGHT CELL up?" route). It fixed volleys at a down CELL seen at Belding (L-NoCross Q-14, M-7).
- The wait the drive team sets in INIT carries which TIP it waits for (`tip: 1|2|3` on the time
  row, exported as `AutoOption.matchSeconds(name, s, tip)`); the DS says "waiting for TIP n until …".
- Every `.pp` stays builder steps only: the Visualizer has no other editor. Only `rejoin` and a
  command beside a wait (other than Spin up) show as "not a builder step". A redrawn Auto should
  open with none of those.
- A fresh export of an Auto last exported before 13 Nov gains `kit.guarded(label, park, seconds, …)`
  wrappers around a list with no park card whose last card is a wait that parks. That is correct,
  not a regression.
- Saving in the builder adds spot parks (`parkFrom`) for drives whose end is pinned, and drops
  hand-made `parkFrom` entries the builder would not make.

**Open, with the mentor (none blocks the baseline):** Match-view switches for Choose and "If
before"; an explicit tick on a choice in place of the reserved name "Priority"; friendlier leg
names than `THREE_TIP`, and "Pickup" to "First pickup" in Clockwise; removing "intake full" from the
Wait dropdown; whether the alliance should be editable.

**Robot:** the CELL checks and the "waiting for TIP n" wording have never run on a robot; both are in
`doc/robot-todo.md`. The simulator's curated studies never hold a CELL down at a check, so the
timed-out routes are covered only by unit tests.

## Infraviolet re-export

**Unmerged:** biobuzz#567, closed unmerged on the mentor's word; nothing in it is worth keeping.
Its branch `claude/reexport-infraviolet` can be deleted.

**Keep:**
- Every `.pp` under `TeamCode/autos` was exported with the Visualizer's `scripts/export-auto.mjs`
  (Visualizer main `281679a`) and compared with the committed generated Java. The five baseline
  Autos and the Clockwise variants were identical: no stale generated Java in the baseline.
- Stale drafts on master: `LPairFlowersAuto`, `LUnderHiveAuto`, `ROutsideLaneAuto`,
  `RPairGardenAuto`, `RPairGardenSweepAuto`, `RSoloNudgeAuto`, `RTwoTipAuto`, `RTwoTipWithAuto` lack
  the guard wrappers a fresh export adds, so the middle of each runs with no endgame guard. It
  matters only if one is run on a robot before its next save.
- Six partner stand-ins differ from a fresh export only by a new empty `OPTIONS` constant.

## R-Clockwise, L-Clockwise

**Unmerged:** nothing. The pair's `.pp` files were assembled by a script and saved with the
Visualizer's `scripts/save-auto.mjs`; the script is gone, so the `.pp` files on master are the source.

**Keep (mentor's decisions):**
- Every loaded leg is driven nose-first. The robot may back a few inches out of a pick, then turns
  to face where it is going: driving backward, pieces left the intake (20245, 12 Nov). The
  backward-leg pair is kept only as drafts.
- R picks the WALL FLOWER first. "SCORING FLOWER first" collides with L shooting its preloads.
- No wait for TIP 2's spill.
- A third TIP need not settle by 30 s; the last ball out by 30 s is the limit. Priority
  `THREE_TIP` / `PARK` is an INIT row on R.
- The INIT menu is one adjustable time per robot plus the route choices, nothing else.
- R's park paths are single curves with control points, no waypoints. The one from the left end to
  `RIGHT_PARK` threads between the HIVE's leg wall (x 46 to 48.6, top at y 91.4) and L's parked
  robot: centre y above 99.4 while x is 38 to 56, below 101.4 once x is under 30. Control points
  (58, 100.6), (34, 100.6) from `LEFT_CLOSE_HIVE` do it (`drafts/r-clockwise-next.pp`).
- R's second volley is from in front of the HIVE, not the far-wall corner (14 Nov, after a run on
  20245).
- The mentor wants L's second volley from the offset spot beside the HIVE spot, not the wall corner
  (15 Nov). Not on master; `RIGHT_AUTO_OFFSET` is that spot now.

**Keep (findings):**
- Two robots on the right end's close row did not fit as drawn: they touched at about 24.5 s as R
  came out from under the HIVE while L finished its volley (R at (58.6, 43.4), L at (43, 29.3)).
  The AUTO spots, 2 in apart, are meant to fix it: re-simulate that moment.
- Third TIP in the simulator's best case: 24 of 24 with L waiting for TIP 2 until 22 s and every
  load four; 0 of 15 where L's GARDEN load was 2. With R 20% slow there is no third TIP.
- L's hold before the WALL FLOWER (15.5 s, when R takes the GARDEN) stays: removing it changes no
  TIP count and makes contact in 11 or 12 of 24 with R 20% slow.
- 20245, 14 Nov, R-Clockwise alone (`logs/2026-11-14-20245-RC`): preload volley 2.3 s, WALL FLOWER
  pick 3.5 s, outside lane nose-first 3.5 s, at the left end at 11.0 s, LEFT volley 2.3 s (4 balls),
  SCORING FLOWER pick 3.5 s, looking from `LEFT_CLOSE_HIVE` at 19.3 s: about 3 s ahead of the
  simulator, from one run.
- The pair has never run with both robots at once. Each half ran alone on 14 Nov; 19429's
  R-Clockwise parked at 15.5 s because it never saw LEFT come up after its own preloads.

**Open, with the mentor:**
- L's second volley at the offset spot (`drafts/r-clockwise-next.pp`, `l-clockwise-next.pp`): L
  starts it only if it sees TIP 2 by 21 s. Best case three TIPs in 15 of 24, contact in 2 of 24
  (23 of 24 with L 20% slow). He also suggested L leaving on time even if not done firing; the kit
  never cuts a feeding launch, so that is a kit rule change.
- One-line plain descriptions of each Auto, and where they should show.
- Priority `PARK` touches L at 27.6 to 27.7 s (`doc/auto-contingencies.md`); not fixed.
- R-Clockwise's drive to `GARDEN_STAGING` turns all the way in, so the gather's stick lands with
  0 to 0.2 s to spare: finish that turn early when the pair is redrawn.
- R-Two-Tip's last volley has no CELL check in front of it.

## Auto fallback paths

**Unmerged:** nothing; no Auto was changed.

**Keep:**
- A fallback has one 4-piece load, so it scores only where the up CELL is already 4 short of
  tipping: RIGHT, when TIP 1 did not come. Keep R-Solo's leg back to the right end (it made the TIP
  in Belding Q-7 and Q-31) and L-NoCross's crossing; both do exactly this.
- L-NoCross's no-TIP route drives 3 s back to the left only to park; alone, the park ends at
  29.3 s (Belding M-7 red ended that trip 9 in short). A throwaway variant that parks straight from
  the right-end spot once the match clock is past 21.5 s ended at 26.9 s, same points, the other
  eight curated cases unchanged. It needs the builder change below.
- R-NoCross, when the partner's TIP 2 does not come, stands 10.3 s at its right launch spot and
  parks at 24.1 s holding 4. No better fallback exists without crossing. Beside a left partner that
  shoots preloads only it scores 36 and R-Solo 56: a drive-team choice of Auto, not a drawing change.
- Trap for hand-written kit variants: when a wait (`firstOf`) is the last step of its list, each of
  its rows must be wrapped in `kit.guarded`, or the endgame guard does not watch that row and the
  park is lost.
- Contact with a partner left on its start is in `doc/auto-contingencies.md`. The `AUTO_HIVE`
  spots overlap such a partner; the `AUTO_OFFSET` spots clear it.

**Open, with the mentor:**
- Whether the builder gets a "then: park from here" choice on an Otherwise inside an Otherwise that
  goes on. The L-NoCross change needs it.
- A command beside a wait: no fallback use found. Suggestion: keep it in the engine, no new builder
  control.

**Robot:** nothing new. The simulator is optimistic about R-Solo's second TIP (0 of 7 at Belding).

## Two TIPs for sure: human-player NECTAR

**Unmerged:** branch `claude/r-solo-human-player` at `32bfe7cf`, no PR. It adds
`drafts/r-solo-human-player.pp` and its generated Auto, the trial OpMode `RSoloHumanPlayerTrial`
(Utility "Drive: R-Solo-HumanPlayer"), a sim request, a section in `drafts/README.md`, and an entry
in `doc/robot-todo.md` §3 that is not on master. The draft is R-Two-Tip with the WALL FLOWER leg
redrawn as the nose-first outside lane through `NECTAR_HANDOFF` (20, 88, facing 110°), no pause,
then both LEFT volleys from `LEFT_LAUNCH_OFFSET`. `LEFT_AUTO_OFFSET` is 0.5 in from that spot. Not
run on a robot. It restarts from the new R-Solo.

**Keep:**
- Rules, from the manual at TU09: one NECTAR per TIP of our HIVE (G426.A), so exactly one before
  the second TIP; released in the air, first touch on the LOADING ZONE tile before a robot (G427.C),
  never touched again (G425.B).
- No new robot code is needed: `BuiltAuto` runs the intake for the whole Auto and it stops itself
  when the lane reads full, so a NECTAR at the intake mouth on any leg is taken if there is room.
- The mentor dropped the robot light: the human player judges the pick (POLLEN left in or beside
  the WALL FLOWER means roll).
- The lane count cannot say "not full" (below, and `doc/robot-todo.md`).
- 20245 took 17 and launched 15 of our own NECTAR at Belding; NECTAR fed about 0.4 s slower a piece.
- A NECTAR is 1.65 POLLEN in the simulator: 7 POLLEN + 1 tips; 6 + 1 tips only a HIVE near seven.
- Simulated, Belding calibration, 60 seeds, the human always rolling: two TIPs in 6 of 60 against
  R-Two-Tip's 3 to 4 on 19429's loads, 33 of 60 against 33 on 20245's. The lane costs 0.7 to 1.6 s
  on the second TIP, which lands near 30 s with Belding-length volleys. A 1 s wait at the handoff
  made it later and added nothing.
- A saved sim request cannot set the human's roll, so the Simulate Auto workflow shows only what
  the lane costs.

**Open, with the mentor:**
- Whether the top-up is worth its time as drawn: it does not yet make two TIPs dependable.
- A second chance after the SCORING FLOWER pick: not drawn.
- Collecting a parked or no-show partner's four POLLEN by the LOADING ZONE: mentioned, not started.

**Robot:** the roll drill to a mark at (17, 96), runs with a short and a full load, a full load with
a roll anyway (G407), a NECTAR volley from the left spot, and the 6-plus-1 tipping test on the
practice HIVE. All in the branch's `doc/robot-todo.md` entry, not on master.

## Reported by two chats, in no other doc

**The lane count reads low after a pick or a gather.** On three picks of 14 Nov (20245) `/Lane/Count`
read 2, 3 and 1 where the volleys then fired 4, 4 and 5; `/Lane/Full` was right on two of the three.
The raw colour sensor refreshes about every 130 ms and saturates, so it cannot count balls that
touch. Issue #213 (a beam break, off mid-height) is the fix. Nothing should decide on "the lane is
not full" from the count.
