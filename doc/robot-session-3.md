# Robot session 3: R-Simple scores TIP 1, R-Quals branches on the real HIVE

Two questions, in this order: **does R-Simple put its preloads in the right CELL and TIP it, three
times out of three**, and **does R-Quals take the branch the real HIVE says**. Everything else
waits. Go down the list; each step has a stop line.

Follows [robot-session-2.md](robot-session-2.md) (25 Oct: both Autos drove their drawn paths on
20245, every mechanism a placeholder). Assumes master with these merged: the CELL triggers (#250),
the endgame guard inside blocks (#252), settled-before-fire (#253) and R-Quals' TIP 2 fire
waiting on `Tip` (#257), on top of the launcher
and feeder cards, the intake running through every Auto (#240), the turret in Basic Drive (#247)
and the match log's build, hardware and tunables (#243). If any of the three is not merged, stop
and run session 2's list instead: the log events below will not be there.

## What is true before you start

- **Only 20245 follows paths.** 19429 has no Foresight block yet (#175); on 19429 every path Auto
  stops at INIT with a Foresight error, as it should.
- **The launcher is real and fires.** `SpinUp` spins the wheels; `LaunchAll` waits for speed
  **and for the robot to settle** (still, and within `PathEndSettle.aimToleranceDeg` of the last
  path's end heading), then runs the feeder for `LaunchCards.launchAllFeedS`. `Empty` fires when a
  `LaunchAll` finishes its feed. There is no piece sensor (#164, #181): the robot does not know
  whether anything came out. Hands clear of the wheels from PLAY.
- **`LeftCellUp` and `RightCellUp` are real** (#250): true while the camera sees that CELL of our
  HIVE up. A HIVE out of view, or mid-TIP, is neither. They are true at once if that CELL is already
  up when the wait starts. **`IntakeFull` is the only placeholder left**: the roller runs the whole
  Auto and every catch wait runs to its limit.
- **Nothing aims the turret in an Auto.** It zeroes at PLAY (assumed at home) and then sits idle,
  servo unpowered: it points where it was left, and a shot's kick or a bump can turn it. R-Simple's
  aim is the chassis at START plus the turret wherever it is. Step 4 checks this; if it moves,
  that is the first thing to fix (a decision for Dustin, not tonight's code).
- **Every tunable is in the log** (#243): the launcher's speed, the feed times and the four
  settle knobs (`PathEndSettle`: 3°, 2 in/s, 5°/s, 1.0 s; starting points, not measured). Change
  one in Panels and the next run's log has the new value; no need to write it down, but **say in
  the table which run had which**.
- **The chassis is heavier than on 25 Oct** (launcher, turret, intake on). The 25 Oct speeds on
  #155 were the light chassis. Expect more overshoot at path ends; the settle events now measure it.

## Before leaving the laptop

- [ ] Master pulled; **one full TeamCode install** on 20245 (first deploy of the night; Sloth Load
      after that).
- [ ] AdvantageScope with the 2026-27 field and `cad/advantagescope/Robot_BIOBUZZ`, as in session 2.
- [ ] The log-review tool: `tools/log-review/` on `claude/sharp-euler-3t56jg` (not on master yet;
      check it out in a separate folder, do not commit to that branch). Its README says how to run
      it against a `.wpilog` and its `.pp`.
- [ ] The two drawings open: `https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=r-simple.pp`
      and `#gh=r-quals.pp`.
- [ ] Batteries charged. Red start taped: back on the audience wall, the robot's **left side**
      (its side plate, not the wheels) on the seam two tiles from the red-side wall:
      (55.17, 7.72, 90°), from a 16 in wide robot.
- [ ] Measure the robot's width across the outside of the side plates. If it isn't 16.0 in, the
      start's x is 47.17 + half of it (`StartPositions.RED_AUDIENCE`, the three right-side `.pp`
      files, re-exported).
- [ ] **A HIVE on the field with its AprilTags**, red side, in the match-start position (RIGHT CELL
      up). Without one every `Tip`, `LeftCellUp` and `RightCellUp` wait runs to its limit and step
      5 answers nothing.
- [ ] Enough preloads and FLOWER pieces for four runs, loaded as for a match.

## At the robot, in order

1. [ ] **Robot Wi-Fi, REV Hardware Client open, `pull-logs.cmd` works** (session 2, step 1).
       *Stop line: a log pulled without typing adb.*

2. [ ] **Activate `robot_20245`, run Validate Hardware.** The launcher, feeder, intake, turret and
       Limelight must all be present.
       *Stop line: nothing reported.*

3. [ ] **Auto: L-Quals, INIT only**, then **Auto: R-Quals, INIT only**, each on the tape,
       Circle (B), each stopped without PLAY. L-Quals' Placeholders line names **`IntakeFull`
       only** (L-Quals uses `LeftCellUp`, so it would be listed if #250 were missing). R-Quals'
       names `IntakeFull` only either way (since #257 it uses no CELL trigger), and its start check
       passes. (R-Simple uses no placeholder, so its INIT shows no such line at all.)
       *Stop line: only `IntakeFull` listed, no FAULT. If L-Quals lists `LeftCellUp`, #250 is not
       in this install: reinstall from master.*

4. [ ] **Auto: R-Simple, red, three times.** Preloads in, robot on the tape, INIT, Circle (B), PLAY.
       It spins up, fires at the right CELL from START, then drives START → PARK (14, 95) and holds.
       Before each run, **note where the turret points**; after each, note whether it moved.
       In the log's `/Events`, each run should read:
       - `wait Fire the preloads at the right CELL (TIP 1)`, then `…: Empty after N s`, where N is
         the spin-up plus `launchAllFeedS` (2.0 s), **not** `4000 ms passed`. A `4000 ms passed`
         means the launcher never reached speed or the robot never counted as settled.
       - No `LaunchAll fed before the robot settled` line: the robot stands still at START.
       - `path START to PARK`, then `path end (14.0, 95.0, 90°): Pedro done; mode …,
         atParametricEnd …, distanceToEndpoint … in; off end … in, heading …°, … in/s`, then
         `settled … s after path end …`.

       Count the pieces that land in the right CELL and whether the HIVE TIPs.
       *Stop line: TIP 1 in 3 of 3.* If a run misses, **stop and look before running again**:
       - the turret moved → the turret, not the launcher (see "If something fails");
       - pieces fall short or long with the turret still → the launcher's speed (#80), set in Panels
         and re-run; the log has the value of every run;
       - a piece jams or nothing comes out → the feed time or the feeder (#80).

       **R-Simple is done** when 3 of 3 TIP, with the speed and feed time that did it on #80.

5. [ ] **Auto: R-Quals, red, once**, HIVE in its match-start position. About the full 30 s.
       Watch the branches, in `/Events` and on the Match page:
       - **TIP 1** (the preloads from START, then `TIP 1` at S_CATCH): `TIP 1: Tip after N s` if
         the camera saw the HIVE move, else `4000 ms passed` and the "TIP 1 missed" branch. Note
         which, and whether the HIVE actually TIPped.
       - **The path into N_FIRE** (the 180° turn that ended 15–20° short on 25 Oct): its `path end`
         line's heading, and its `settled … s after path end` line. That number is the one the
         settle rule exists for.
       - **TIP 2's fire** (`Fire the far FLOWER's 4 (TIP 2)`, waits on `Tip` since #257): it should
         end `Tip after N s` once the HIVE swings, whether or not TIP 1 happened. `4000 ms passed`
         means no TIP was seen: note whether the HIVE moved and whether the camera could see it from
         N_FIRE. A `Tip after 0.0x s` means a TIP already under way settled just as the wait began;
         note it, since the launch was cut before it fed.
       - **The guard:** `parking now`, `cutting card short` or `not starting <path>`, and the time
         left when it did. On 25 Oct it cut TIP 2's fire at 3.7 s left.

       Any `LaunchAll fed before the robot settled` line is a settle that took longer than
       `maxSettleWaitS`: note which path end.
       *Stop line: it runs to PARK without a FAULT, and you can say which branch each wait took
       and why.*

6. [ ] **Loop Time Baseline**, one minute. Compare with 25 Oct (7.17 ms mean, 29.8 ms max, odometry
       5.79 ms): the launcher, turret and intake are now in the loop.

7. [ ] **Pull the logs; run `review.py` on the four runs** against `r-simple.pp` and `r-quals.pp`
       before leaving, if time allows. Otherwise put the four `.wpilog` files on the team Drive
       (2026-2027 FTC Biobuzz / Logs / 20245-RC) with the run number in the name.

## Record these, and where they go

| Fact | Value | Post on |
|---|---|---|
| Battery at rest, start and end | | #175 |
| Launcher speed (`targetTicksPerSecond`) and `launchAllFeedS` for each R-Simple run | | #80 |
| R-Simple runs 1–3: pieces in the right CELL / TIP yes or no | | #80 |
| R-Simple runs 1–3: the `Empty after N s` time | | #80 |
| R-Simple runs 1–3: turret moved during the run? (yes/no, by how much) | | #80 |
| R-Simple runs 1–3: park `path end` line (off end, heading) and the `settled … s` line | | #155 |
| R-Quals: TIP 1 branch taken, and did the HIVE TIP | | #155 |
| R-Quals: the N_FIRE path end's heading error, and its settle time | | #155 |
| R-Quals: the longest settle of the run, and which path | | #155 |
| R-Quals: TIP 2 fire's ending line (`Tip after …` / `4000 ms passed` / cut) | | #155 |
| R-Quals: the guard's line and the time left | | #155 |
| Any `fed before the robot settled` lines | | #155 |
| Which settle knob was changed in Panels, if any, and from which run | | #155 |
| Loop Time Baseline: hz / mean / max / odometry ms | | #175 |
| Anything that stopped you, and at which step | | here, as a PR to this file or a comment on #155 |

Pedro's `atParametricEnd` and `distanceToEndpoint` at each path end go on #155 too, all of them;
they say which of Pedro's end conditions ended each path, which the 25 Oct logs could not.

## If something fails

- **L-Quals' INIT lists `LeftCellUp` as a placeholder.** The install predates #250.
  Full TeamCode install from master.
- **The fire wait ends `4000 ms passed`, nothing fed.** Read the Robot page: launcher at speed?
  If it never reaches `targetTicksPerSecond`, that is #80, not the Auto. If it is at speed and
  still nothing fed, the robot never counted as settled: look for its `settled` line; raise
  `stillSpeedInPerSec` in Panels for one run to confirm, and record it.
- **`fed before the robot settled` after every path.** The tolerances are too tight for this
  chassis. Do not loosen them blind: post the `path end` and `settled` lines and decide from the
  numbers.
- **The turret turns during a run.** Nothing holds it in an Auto yet. Stop running R-Simple for
  score; note it on #80 and tell Dustin. Holding the turret at home in an Auto is a small code
  change, but it is a decision, not a guess at a meeting.
- **A TIP happened on the field but the log says `4000 ms passed`.** The camera did not see it.
  Note where the robot was and whether the HIVE was in its view (the Robot page's HIVE line, and
  `CameraBlind`); that is #173's tuning, not the Auto's.
- **R-Quals' TIP 2 fire ends at once (`Tip after 0.0x s`).** A TIP was settling as the robot
  arrived (see step 5). Record the line and what the HIVE was doing.
- **The guard parks earlier than on 25 Oct.** Expected if settling now adds time before each fire.
  Record the guard's line; the wait limits are the Visualizer's (#182).
- **A path Auto FAULTs at INIT naming Foresight.** You are on 19429, or the wrong config is active.

## What this unlocks

R-Simple at 3 of 3 means the first Auto scores for real, and #80 has the speed and feed that did
it. R-Quals' branch lines say whether the HIVE triggers can carry the Quals Autos, and the path-end
and settle lines give #155 the overshoot, the heading error and the settle time with the full mass
on: the numbers that set the settle tolerances, the wait limits (#182) and the simulator's
path-end model, measured instead of guessed.
