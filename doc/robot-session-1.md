# Robot session 1: a mentor alone, 20245, about two hours

The goal is one sentence: **the robot leaves and parks on its own.** That is points on 7 Nov, and
every later session improves on it. Go down the list in order. Each step has a stop line; when you
hit it, go to the next step. Nothing here needs a student, a launcher or a camera.

Assumes the 17 Oct tuning of 20245 (#175: hardware check, Drive Motor Check, Mecanum, Pinpoint and
Foresight tuners, all on master). If the chassis has changed since, do #175 steps 1–5 first.

## Before leaving the laptop

- [ ] biobuzz #189 (the Auto runtime) is merged, or its branch `feat/115-auto-runtime` is checked out.
- [ ] One **full TeamCode install**, not a Sloth Load: this is the first deploy with the runtime.
      Sloth Load is fine for everything after.
- [ ] Battery charged. 20245 read 12.83 V on 17 Oct.
- [ ] Open the two Autos in the Visualizer so you can see where they start and park:
      `…/Visualizer/#gh=r-simple.pp` and `#gh=l-simple.pp` (once #189 is on master).
- [ ] Field tiles with the LOADING ZONE tape in place, not carpet.

## At the robot, in order

1. [ ] **Activate `robot_20245` on the hub, then run Validate Hardware.** Fix what it reports.
       *Stop line: nothing reported.*
2. [ ] **Pedro's Tests procedure** at `http://192.168.43.1:10158`: localization, odometry and pose,
       then hold, line and curve. Watch the Panels field view (`http://192.168.43.1:8001`) while it
       runs. If hold or line drifts, write down what it did and carry on; don't re-tune tonight.
       *Stop line: the dot follows the robot, and hold holds.*
3. [ ] **Basic Drive, five minutes.** Forward, strafe, turn. If strafe or turn goes the wrong way,
       flip `STRAFE_SIGN` / `TURN_SIGN` in `BasicDriveTeleOp` and Sloth Load; that is the expected
       first result. Does it strafe straight at full stick? Note yes or no.
       *Stop line: all three directions go the way the stick says.*
4. [ ] **Auto: Hold Still.** INIT, press **B** for red, PLAY. It should sit there. This proves the
       alliance choice, the start check and the Auto loop before any path is involved.
       *Stop line: no FAULT on the Driver Station.*
5. [ ] **Auto: R-Simple, red.** Place the robot at the red right start as the Visualizer draws it:
       centre at x 59, y 8.06, facing the HIVE (heading 90°). INIT, **B**, PLAY.
       - The Driver Station will show *Placeholders: LaunchAll, SpinUp, Empty* as a warning. That is
         correct: no launcher yet, so "Fire the preloads" waits its 4 s with nothing happening.
       - Then it drives START → PARK and stops. Expect it to take about 5 s after the wait.
       - Read the final pose off the Match page and write it in the table below against the drawn
         PARK (10.5, 95). Part of the robot must be inside the LOADING ZONE tape.
       - Run it three times. Same ending each time, within a couple of inches, is a pass.
       *Stop line: leaves and parks on its own, 3 of 3.* **This is the night's goal.**
6. [ ] **Auto: L-Simple, red,** from the left start: centre at x 59, y 133.69, facing the HIVE
       (heading 270°). Expect it to wait a lot: TIP 1 up to 7 s (the camera is not mounted, so no TIP
       is seen), then "Fire" 3 s, the FLOWER 3 s, "Fire" 2 s, with a drive between each. About 25 s
       in all, then PARK_L (10.5, 120). Same checks as step 5.
       *Stop line: completes once without touching anything.*
7. [ ] **Loop Time Baseline.** Post `loop_hz`, `loop_mean_ms` and `loop_max_ms` on #189 and in
       #175's table. The runtime PR needs these numbers.
8. [ ] If there is time: pull the newest `.wpilog` from `/sdcard/FIRST/logs/` and open it in
       AdvantageScope. One file, a pose trace, the Auto's event lines. Not required tonight.

## Record these (post them on #175)

| Fact | Value |
|---|---|
| Battery at rest | |
| Tests procedure: hold / line / curve | |
| Strafes straight at full stick on tiles? | |
| R-Simple final pose, runs 1–3 (drawn PARK is 10.5, 95) | |
| L-Simple final pose (drawn PARK_L is 10.5, 120) | |
| Loop Time Baseline: hz / mean ms / max ms | |
| Anything that stopped you, and at which step | |

## If something fails

- **Validate Hardware reports a device:** wiring or config, fix it first; nothing else is worth
  doing until it passes.
- **The dot does not follow the robot in step 2:** the Pinpoint values are wrong for this chassis
  (#176). Stop the Autos; the rest of the session is Basic Drive and step 7.
- **R-Simple drives somewhere else:** the path is drawn from the start pose, so a wrong start
  placement moves the whole path. Re-place and run again before changing anything.
- **It parks short or long:** that is a nudge in the Visualizer, not a code change. Note the pose
  and move PARK next time; don't edit on the night.
- **A FAULT at INIT naming a command or trigger:** the Auto uses a name `AutoRegistration` does not
  know. Post the message; that is a code fix, not a robot fix.

## What this unlocks

Step 5 passing means every Auto in `TeamCode/autos/` runs on this robot as a timed script today,
and each mechanism that lands later (launcher, intake, camera) makes them smarter without changing
them. Session 2 is the launcher on the rig; session 3 puts it on the robot and runs R-Quals.
