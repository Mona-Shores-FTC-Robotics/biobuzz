# Robot session 5: how deep the FLOWER pick pushes

One question: **does a push aimed 2 to 3 in deeper than today's 12 in take four POLLEN when the
pose is a few inches off?** 20245, Basic Drive, one FLOWER, no launcher. Everything else in this
file is what the code and the 29 Oct to 2 Nov match logs already say, so the session is measuring,
not deciding.

Follows [robot-session-4.md](robot-session-4.md). The mentor's observation (3 Nov 2026): with the
odometry a few inches off, the robot sometimes does not drive deep enough into the FLOWER, and the
pick takes nothing or fewer than four.

## What the code does today

`opmodes/FlowerPicker` (the pick) on `opmodes/GrabRoutine` (the sequence). The push is a Pedro path
straight in, from the line-up 24 in out (`approachStandoffIn`) to **`contactStandoffIn` 12.0 in from
the FLOWER's centre**, capped at `pushSpeed` 0.5 of top speed. It ends on the first of three things,
and the log says which, on the push's own line and again on the done line:

| The log says | What happened |
|---|---|
| `IN 0.79 s (reached)` | odometry put the robot within 0.5 in of the target |
| `IN 0.61 s (path done 0.7 in short)` | Pedro ended the path first, this far from the target |
| `IN 0.98 s (stalled 0.5 in short)` | less than 0.25 in of progress in 250 ms (`stallProgressIn`, `stallMs`): against the FLOWER |
| `IN 2.00 s (timed out 3.1 in short)` | `pushTimeoutMs` 2000 ran out |

So the words are already there (since 27 Oct; the sequence moved to `GrabRoutine` on 29 Oct). The
failure the mentor sees is the first row firing with the robot physically short of the FLOWER.

**Geometry.** The frame is 16.2 in (`ResetPositions.WALL_TO_CENTRE_IN` 8.1, taped 30 Oct), so at
12.0 in the front face is 3.9 in from the FLOWER's centre and 6.6 in from the wall face (the centre
is 2.71 in off the wall, `FlowerFieldPoints`). The CAD seat was drawn with the face 4.59 in from the
centre (issue #251), i.e. the centre 12.7 in out.

## What the logs say: 29 Oct to 2 Nov, 173 pushes on 20245

Every pick, one line each, is in [flower-picks-2026-10-29-to-11-02.txt](flower-picks-2026-10-29-to-11-02.txt),
written by the new `python3 tools/pull-logs/read_logs.py picks <logs>` (pieces are intake-current
spikes, about one per piece; the newer logs also carry the lane count). Where a push ended:

| Session | reached | …took something | stalled | …took something | path done short | …took something | timed out |
|---|---|---|---|---|---|---|---|
| 29 Oct (Basic Drive, 8 logs) | 54 | 15 | 41 | 20 | 3 | 1 | 0 |
| 31 Oct (Basic Drive + L-Simple Autos) | 19 | 2 | 1 | 0 | 0 | | 0 |
| 1 Nov (Basic Drive, 3 Auto picks) | 25 | 9 | 2 | 2 | 0 | | 0 |
| 2 Nov (Basic Drive + R-Quals) | 14 | 1 | 6 | 1 | 8 | 3 | 0 |

"Took something" is at least one current spike during the push or dwell; the logs do not say
whether a FLOWER was loaded for a given pick, so a 0 can be an empty FLOWER (and on 2 Nov the lane
already held 2 to 3 for most picks).

What matters for the question:

1. **12.0 in is the touch point, not inside the FLOWER.** On 29 Oct, picks whose pose was right
   stalled at 12.2 to 12.5 in from the centre as odometry had it (`stalled 0.5 in short`, repeated).
   That is the face about 4.3 in from the centre, where the CAD seat put it. A target of 12.0 means a
   pose long by half an inch already ends "reached" with the stick short of the FLOWER.
2. **No push ever timed out**, in 173. Pushes took 0.5 to 1.25 s, stalls included, against a
   2000 ms timeout.
3. **The pose error ran the short way on 31 Oct to 2 Nov.** Those logs carry the camera's last gap to
   odometry; along the push it was −2 to −5 in for most picks (negative: the robot was really
   further from the FLOWER than odometry said, so "reached" stopped short). Of 29 "reached" pushes
   with the gap under −2 in, 4 took anything; of 26 within ±2 in, 8 did. The camera reads about 3.5 %
   long itself (session 4), so this is the sign and the size, not a measurement.
4. **On 29 Oct the error ran the other way** and the pick still worked: 41 pushes stalled short
   (odometry said 12.2 to 19 in, one 25; the robot was against the FLOWER), and 20 of them took pieces.
   A stall is a good way for a push to end.
5. **The 31 Oct L-Simple Autos missed sideways, not in depth.** 6 of 7 Auto picks "nothing came",
   every one "reached" at 12 in, with the camera's gap 5.6 to 9.8 in across the push and under 3 in
   along it. The Auto pushed beside the FLOWER. A separate problem (the Auto's start pose or path),
   not fixed by any depth.
6. The 2 Nov R-Quals run's two picks both "reached" and took nothing (gap along −0.2 and −3.2 in).

## The proposal: aim 2.5 in deeper, so a right pose ends by stall

`contactStandoffIn` **12.0 → 9.5** (10.0 and 9.0 are the other two values to try). Why those:

- **A right pose then stalls**, 2.9 in short of the target, at the same 12.4 in it stalls today. The
  push is a self-contained action: it ends when the robot is against the FLOWER, whatever the pose
  said. The log reads `stalled 2.9 in short`.
- **Grace for a long pose**: the robot still touches the FLOWER with the pose up to 2.9 in long
  (3.4 in at 9.0, 2.4 in at 10.0). Today it is about 0.4 in.
- **It cannot run the timeout out.** The push grows from 12 in to 14.5 in; at the 0.5 cap
  (35 in/s of 71) a 12 in push took 0.75 to 1.0 s, a stalled one up to 1.25 s. Against the FLOWER at
  about 0.75 s plus the 250 ms stall window, the push ends at about 1.0 to 1.1 s, 0.9 s inside
  `pushTimeoutMs`. The stall check is skipped for the path's first 500 ms (2 × `stallMs`), long before
  any contact. The one way to a timeout: the FLOWER gives and the robot creeps in faster than
  1 in/s (0.25 in per 250 ms) for two seconds; the log would say `timed out`.
- **Pushing harder, briefly.** For the 250 ms stall window Pedro drives at up to the 0.5 cap against
  the FLOWER (the target is 3 in away, so its controller asks for everything the cap allows); the
  dwell then holds at `dwellPushPower` 0.1 as today. Watch the wheels and the heading for that.
- **The face ends up 4.1 in from the wall** at the target (9.5 − 8.1 − 2.71 → 1.4 in short of the
  FLOWER's centre), inside the FLOWER's footprint, which is the point: a stall is the only way there.
- **What moves with it, and what does not.** `minAutoStartIn` 18 and `approachStandoffIn` 24 are
  both still well outside it. The Visualizer's `CONTACT_STANDOFF_IN` (`src/lib/simple/flowers.ts`,
  12 today, with 24 and 18) changes in the same step, or its picture of a pick is wrong. The
  simulator reads master's value (`RobotDesign.real`: seat = contact − 8.1), so `SimulatorSmokeTest`
  runs with the new seat the moment the default changes. The stick's lift-off point used to be
  `contactStandoffIn` + `retractAfterBackIn` from the centre, which a deeper target would have pulled
  2.5 in closer to the FLOWER whenever a pick ended early; it is now anchored to where the push
  actually ended (this PR), so the stick lifts 6 in out from the stall point however deep the target.
  The Auto's `PickFlower` card gains about 0.25 s per pick (the stall window) on its 4.5 s typical.

**The default stays 12.0 until the robot says otherwise.** Below is how it says it.

## Before leaving the laptop

- [ ] Master pulled with this PR merged. A **Sloth Load** is enough (no `res/` or Gradle change).
- [ ] Four POLLEN to reload the FLOWER after every pick, and someone to do it.
- [ ] A tape measure. Charged batteries (12.0 V or better; a sagging battery slows the push).
- [ ] Panels open at `192.168.43.1:8001`, **FlowerPicker** found: `contactStandoffIn` is the one
      knob this session turns. Nothing else in FlowerPicker changes today.
- [ ] A taped spot about 3 ft out from the red WALL FLOWER, square to the wall (the issue #255 spot).

## At the robot, in order

1. [ ] **Pose on the field.** Basic Drive, face the HIVE, operator D-pad left (relocalize). The Match
       page must not say "refused" when D-pad up is held.

2. [ ] **Measure the touch point once.** Stick down (A), intake off, push the robot by hand straight
       in until it is against the FLOWER. Tape from the wall face to the robot's back face and add
       8.1: that is the centre's distance from the wall; subtract 2.71 for its distance from the
       FLOWER's centre. The logs say about **12.4 in**. Write it down. Then, still there, measure how
       far the stick's tip is ahead of the front face: that number has never been written down, and
       the lift-off margin (`retractAfterBackIn` 6 in) has to be more than it.

       *Stop line: a touch point within an inch of 12.4. Further off, the FLOWER on this field is not
       where `FlowerFieldPoints` has it (2.71 in off the wall, 47.36 in along it), and the odometry
       question cannot be separated from that: measure the FLOWER's position first.*

3. [ ] **Baseline at 12.0: three picks** from the taped spot, hold driver D-pad up to the end of the
       back-out. After each, from the Match page's `Last FLOWER pick` line, record the push's word and
       seconds, and count the pieces by eye (the lane count on the Robot page is the check).

       | Pick | contact | push s | push ended | pieces in | notes |
       |---|---|---|---|---|---|
       | 1 | 12.0 | | | | |

4. [ ] **10.0: three picks.** Reload between picks. Good: every push ends `stalled 2.4 in short` (give
       or take half an inch), 1.0 to 1.3 s, four pieces each time.
5. [ ] **9.0: three picks.** Good: `stalled 3.4 in short`, 1.0 to 1.3 s, four pieces, and the
       robot stays square (it does not skew or climb while it pushes for that quarter second; the
       stick lifts only on the way out, well clear of the FLOWER).
6. [ ] **A pose a known distance long, at the value that looked best (three picks).** Relocalize at
       the HIVE, then lift the robot and set it down 2 in further from the FLOWER (lifted, the
       Pinpoint sees nothing, so odometry now says the robot is 2 in nearer than it is). Pick. Good:
       still `stalled`, still four. Then the same with 4 in, to find where it stops working: at 9.5 the
       geometry says about 3 in.
7. [ ] If time: **9.5, three picks** from the plain relocalized pose, since that is the proposed
       default.

*Stop lines:* a push that says `timed out` (then read how far the robot got: `read_logs.py picks`
gives the depth); a robot that skews more than a few degrees during the stall window (then try
`pushSpeed` 0.4 and note it, one change at a time); the stick swinging up anywhere near the FLOWER
(stop and read `retractAfterBackIn` against the stick reach measured in step 2).

**What "good" looks like, in one line:** at the chosen value, nine of nine pushes (steps 4 to 6)
end `stalled`, none `timed out`, four pieces each, and a pose 2 in long changes nothing.

## Record these, and where they go

- The touch point and the stick's reach ahead of the face → the `contactStandoffIn` and
  `retractAfterBackIn` comments in `FlowerPicker.java` (date, robot, how measured).
- The tables → the PR that changes the default. Pull the session's logs
  (`tools/pull-logs`), run `python3 tools/pull-logs/read_logs.py picks <that day's .wpilog files>`
  and paste its lines under the tables; it also shows the camera's gap at each pick.
- The new `contactStandoffIn` → `FlowerPicker.java` **and** the Visualizer's
  `CONTACT_STANDOFF_IN` in `src/lib/simple/flowers.ts`, same day. Then `SimulatorSmokeTest` on the PR.
- If the number is right but an Auto's pick still misses, read `side` in the picks table before
  touching depth again: the 31 Oct Autos missed by 6 to 10 in sideways.

## Also, with an Auto on the field: the drive-to-pick seam (PR #378)

A separate change, measured separately, same robot. On master before PR #378 every Auto pick's push
started **0.70 to 0.73 s after the drive ended** (0.96 s with a square-up), all of it the stick's
700 ms: the stick went down only when the pick card started, and the robot was already under 2 in/s
0.13 to 0.51 s after the path end. With the PR the stick comes down over the last 18 in of the drive
before a `PickFlower` card, and a grab waits only for 8 in/s (`settledInPerSec`), not 2. Expected:
0.1 to 0.2 s from path end to push when the drive ends square. R-Solo or R-Partner-Preload, two picks
each; Sloth Load is enough.

1. [ ] **Loop time first.** **Measure: Loop Time Baseline** before and after the Load, then one Auto
       run; `/Robot/LoopMs` median and p95 go in the PR (2 Nov R-Quals: 26.7 / 47.7 ms).
2. [ ] **Watch the stick on the drive in**, a spotter at WALL_FLOWER_STAGING and at
       SCORING_FLOWER_STAGING: it should swing down about 18 in before the robot stops, and touch
       nothing before the push. *Stop line: it touches anything → `FlowerPicker.lowerWithinIn` 12 in
       Panels (about 0.3 s less hidden), write the number back.* The Visualizer branch
       `claude/pick-stick-on-drive-in` shows the same moment as a "Stick down on the drive in" line.
3. [ ] **Watch the push start** while the robot is still slowing (up to 8 in/s). The push should still
       end `reached` or `stalled`, never `timed out`, and the pick should not start squaring up where
       2 Nov's did not. *Stop line: a rough push → `settledInPerSec` 5, one change at a time.*
4. [ ] **Read the gap** from the logs: per pick, the `path end` event to `PREP … then IN`, and the new
       lines `stick down on the drive in, X in to go` and `prep done on the way in, X s ago`. X under
       0.7 s on most picks means the last 18 in were quicker than the stick: `lowerWithinIn` 24.
5. [ ] **TeleOp check:** stick already down (toggle), press pick: no 0.7 s pause before the push. Stick
       up: as before.

→ The numbers and any changed knob go to PR #378. The GARDEN gather has no approach yet (its stick
hit the corner wall when swung early, 30 Oct); say whether the corner allows one.

## Also, with any Auto: INIT names the start the robot is on (PR #372)

PR #372 has an Auto's INIT match the camera against all four known starts: its own two (red and blue)
and the other side's two. On its own start it proposes the alliance as before. On the other side's
start the INIT page shows **"On <start>, not this Auto's start · pick the Auto that starts there, or
move to <start>"**. To tell the two same-facing audience-wall starts apart (23.6 in apart), the match
radius went from 18 to 10 in, so the risk is the robot's **own** start no longer matching: "No
alliance", and the Auto does not move at PLAY. Never tried on a robot. About 10 minutes; a Sloth Load
is enough. Any time the camera answers: Limelight connected, HIVE upright. 5 frames in a row settle
a match, about a second.

`RIGHT_START` is **Red audience** (C1, R-Solo and the R-Partner Autos); `LEFT_START` is **Red far
wall** (C6, L-Partner). Blue's are the same turned half a turn: Blue far wall and Blue audience wall.
Place by the marks (back flat on the wall, frame's left side on the seam), never by the camera.

1. [ ] **R-Solo on Red audience.** INIT, no alliance pressed. Good: red is proposed within a few
       seconds, no `WRONG_START` line, start check confirms.
2. [ ] **L-Partner on Red far wall.** The same. Good: red proposed, no `WRONG_START`.
3. [ ] **R-Solo on Red far wall** (the 2 Nov mistake, other way round). Good: "On Red far wall, not
       this Auto's start · … or move to Red audience", and "No alliance" above it. Then press Circle
       (red): the "move to" still says Red audience.
4. [ ] **L-Partner on Red audience.** Good: "On Red audience, not this Auto's start · … or move to Red
       far wall". The plain "Not on the … start" line must **not** also be there, even after 10 s.
5. [ ] **One blue start** if the field allows: R-Solo on Blue far wall. Good: blue proposed. (Blue
       audience wall is computed, not measured: if it matches the wrong start, say so.)
6. [ ] **Off every start**: R-Solo one tile out from Red audience, facing the same way. Good: no
       `WRONG_START`, and after 10 s the plain "Not on the Red audience start" line.

*Stop line:* step 1 or 2 shows "No alliance" for more than 5 s with tags in view. 10 in is then too
tight for placement plus the camera's error. Read the match log's start-check gap and the start match
events (`python3 tools/pull-logs/read_logs.py` on that run), and put the gap in the issue. Don't
widen the radius past 11.8 in (half of 23.6) without the logs.

→ Write the result on PR #372 (merged) or a new issue: which steps passed, and the gap on each own-start run.
