# Robot session 2: the Autos drive their paths, and the log comes to the laptop by itself

Two questions, in this order: **does the robot drive the path the Visualizer drew**, scoring or
not, and **can we read what it did afterwards without touching adb**. Everything else waits.
Go down the list; each step has a stop line.

Follows [robot-session-1.md](robot-session-1.md). Assumes master as of 25 Oct 2026 (#212 merged),
which has the Auto runtime (#189), the match log (#112), the taped red start (`StartPositions`,
24 Oct) and the pull-logs tool (#214).

## What is true before you start

- **Only 20245 can follow a path tonight.** Its Foresight block was pasted on 17 Oct. 19429's is
  empty, so on 19429 every path Auto stops at INIT with a Foresight error. That is the robot
  saying "not tuned", not a bug: 19429 drives paths after the Foresight Tuner (#175) and its block
  is pasted into `pedro/robots/Robot19429.java`. Hold Still, Basic Drive and the log work on both.
- **The Autos now fire for real.** `SpinUp` and `LaunchAll` spin the launcher and run the feeder
  (#194). "Even if it can't shoot" means the wheels will spin and the feeder will run with nothing
  in it. Hands clear. If the launcher or feeder motors are not plugged in, every RobotOpMode fails
  at INIT, because devices are required; Validate Hardware says which.
- **Placeholders left:** `IntakeFull`, `LeftCellUp`, `RightCellUp`. A wait on one runs to its
  limit. `Tip` and `CameraBlind` are real: with no HIVE in view, `Tip` waits run to their limits
  too. The Driver Station lists the placeholders at INIT.
- The red start is taped and measured: centre (55, 9.7), facing up the field (90°). Place it as
  **back flat on the audience wall, left wheels on the tile seam two tiles from the red-side
  wall.** Every right-side Auto (R-Simple, R-Quals, Sister-Right) starts there. The left-side
  Autos start at (59, 133.69, 270°), which nobody has taped; leave them.

## Before leaving the laptop

- [ ] Master pulled; one **full TeamCode install** on each robot you will use (the first deploy of
      the night; Sloth Load after that).
- [ ] AdvantageScope installed, with the FTC assets bundle (the 2026-27 field) and
      `cad/advantagescope/Robot_BIOBUZZ` in its assets folder. `TeamCode/README.md` § AdvantageScope.
- [ ] Open `tools/pull-logs/README.md` once and read "At a meeting". That is the whole workflow.
- [ ] Open the Autos you will run so you know where they go:
      `https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=r-simple.pp` and `#gh=r-quals.pp`.
- [ ] Battery charged. Field tiles with the red start taped.

## At the robot, in order

1. [ ] **Join the robot's Wi-Fi on the laptop. Start the watcher** by double-clicking
   `tools\pull-logs\watch-logs.cmd` (its own window, not Android Studio's terminal) and leave
   that window open:

   It says `connected.` and how many logs are on the robot. If it says nothing answers, the laptop
   is not on the robot's network.
   *Stop line: `connected.`*

2. [ ] **Activate `robot_20245`, run Validate Hardware.** Fix what it reports.
       *Stop line: nothing reported.*

3. [ ] **Basic Drive, two minutes.** Drive a lap. Cycle to the Robot page (Share) and find the
       **Log** section: a file name and a rising "loops written". Then **stop the OpMode and look
       at the laptop**: within ten seconds the watcher says `pulled …` then `opening in
       AdvantageScope`. Drag `/Odometry/Robot` onto a 2D Field and pick the 2026-27 FTC field.
       The lap you drove is on the screen.
       *Stop line: the lap is on the laptop with nobody having typed adb.* **Logging is done.**

4. [ ] **Auto: Hold Still,** red (Circle (B)), PLAY. The start check runs against the taped spot.
       It does not move. Stop it.
       *Stop line: no FAULT.*

5. [ ] **Auto: R-Simple, red, three times** from the taped start. Each run: INIT, Circle (B),
       PLAY. The launcher spins up and the feeder runs (the "Fire" card), then it drives
       START → PARK (10.5, 95) and holds. Part of the robot must be in the LOADING ZONE tape.
       - After each run the log opens on the laptop. Read the final pose off `/Odometry/Robot`
         (or the Match page before you stop) into the table.
       - Three endings within a couple of inches of each other is a pass. Where they end against
         (10.5, 95) is the next nudge in the Visualizer, not a code change.
       *Stop line: leaves the start and parks, 3 of 3.* **Paths are done.**

6. [ ] **Auto: R-Quals, red, once.** Eleven paths with waits between them, about the full 30 s:
       START → S_CATCH → the far FLOWER (47, 127) → N_FIRE (57.5, 119) → S_FIRE (55, 24) →
       LANE_FIRE (45, 26) → the wall FLOWER (15, 47) → FIRE3 → PARK (10.5, 95). With no HIVE
       seen, every `Tip` wait runs to its limit, and the endgame guard may cut to PARK early;
       the Match page's trace lines say which, and the same lines are in the log's `/Events`.
       - Watch for the one thing R-Simple cannot show: a turn into a path from a different
         heading, and the long drive up the field and back.
       - Afterwards, in AdvantageScope, put `/Odometry/Robot` on the field and `/Events` on the
         timeline, and step card by card. Does the robot reach each point before the next card?
       *Stop line: it completes and parks without a FAULT.*

7. [ ] **Loop Time Baseline**, one minute. Post the numbers on #175's table.

8. [ ] **If 19429 is there:** Activate `robot_19429`, Validate Hardware, Basic Drive one lap,
       stop, and confirm the laptop pulled its log into a *different* folder
       (`biobuzz-logs\<19429's network>`). Then run one path Auto and confirm it stops at INIT
       with the Foresight message. That is the expected result; the fix is the tuner, another night.

## Record these (post them on #214 and #175)

| Fact | Value |
|---|---|
| Battery at rest, 20245 | |
| Watcher: seconds from OpMode stop to AdvantageScope open | |
| Log size of one R-Simple run (KB) | |
| R-Simple final pose, runs 1–3 (drawn PARK 10.5, 95) | |
| R-Quals: completed / guard parked early / FAULT, and the last trace line | |
| R-Quals: first point it missed by more than 3 in, if any | |
| Loop Time Baseline: hz / mean ms / max ms | |
| 19429 log landed in its own folder? | |
| Anything that stopped you, and at which step | |

## If something fails

- **Watcher: "Nothing answers at 192.168.43.1:5555."** The laptop is on another network, or the
  hub is off. Nothing in the robot code is involved.
- **Watcher connected but "No .wpilog files".** Nothing has run since the install, or the Robot
  page's Log section shows a WARN with the reason the file could not open. Post the reason.
- **Sloth Load, then the watcher says "lost the robot".** Expected: Sloth disconnects adb after
  every deploy. The watcher reconnects on its next tick.
- **AdvantageScope opens but the field is blank.** Pick the 2026-27 FTC field in the Field tab's
  settings (the FTC assets bundle has it) and drag `/Odometry/Robot`, not `/Odometry/PedroInches`.
- **A path Auto FAULTs at INIT naming Foresight.** You are on 19429, or 20245's block was
  overwritten. Check the active config first.
- **A FAULT at INIT naming a command or trigger.** The Auto uses a name `AutoRegistration` does
  not know. Post the message; code fix, not a robot fix.
- **R-Simple drives somewhere else.** The path is drawn from the start pose: re-place the robot on
  the tape and run again before changing anything. If it is consistently off, the taped spot and
  `StartPositions.RED_AUDIENCE` disagree; measure, don't edit tonight.
- **It parks short or long.** A nudge in the Visualizer, next time.
- **The launcher must not run tonight** (not mounted safely). There is no switch for that yet.
  Stop, and make it an issue: a Panels toggle that makes the launch cards no-ops is the honest
  fix, not unplugging a motor (a missing device fails INIT).

## What this unlocks

Step 5 passing says every right-side Auto in `TeamCode/autos/` drives on 20245 and the only
open questions are mechanism timing and the guard's margins, both of which the log now answers
after every run. Step 3 passing means every later session reviews its runs from a file instead
of memory. The next session is 19429's Foresight tuner, so both robots can run the same Autos.
