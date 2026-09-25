# Spike: does the locked dependency set boot on its own?

**This branch is not for merging.** It exists to answer one question with a robot, because
nothing about it is visible from a desk. Delete the branch when the answer is written down.

## The question

Since 24 Sep 2026 the 19429 Control Hub will not start. The Driver Station says *no heartbeat*
and both OpMode lists are empty. Logcat shows the same cycle over and over:

```
CoreRobotWebServer      started port=8080
TooTallWebSocketServer  Started WebSocket server on port 8081
... ~3.6 s later ...
FATAL EXCEPTION: Thread-13
java.net.BindException: Address already in use
    at fi.iki.elonen.NanoHTTPD$ServerRunnable.run(NanoHTTPD.java:1763)
```

A third NanoHTTPD tries to bind a port that is already taken, dies on a bare thread, the
watchdog service ANRs, `FtcAccessPointService` relaunches the RC ten seconds later, repeat.
The trace names no library, only NanoHTTPD, so it does not say *who* the third server is.

Two things are already known and are not in dispute:

| Fact | Evidence |
|---|---|
| The crash happens with **no team OpMode registered** | Sloth's TeamCode load pass registered only `{Basic: Omni Linear OpMode}`. None of our five (`Validate Hardware`, three `Vision: …`, `Flywheel Speed Test`) appeared. |
| `f15c3cb` (old library set, zero team Java) and `06309a3` (current library set, no team Java yet) both **boot** | Tested on the hub, 24 Sep 2026. |

So the cause is either in the dependency set as master now has it, or in team source added
after `06309a3`. This spike separates those.

## The experiment

Two commits. Install each with the **TeamCode** run configuration (~40 s full install, *not*
Sloth Load — the dependency set changes between them). After the install finishes, **wait at
least 90 seconds** before calling it: the crash lands ~40 s after `RCActivity onCreate()`, and
a capture that stops sooner proves nothing.

| Step | Commit | Tree | If it boots | If it crashes |
|---|---|---|---|---|
| **A** | *Exclude all team-authored Java from the build* | Sloth + Pedro + AutoTune + Panels + Dashboard, no team code, **no** AdvantageScope Lite | The locked set is fine — the cause is back in our source, bisect it | It was never our code; the fix is in the dependency set |
| **B** | *Add AdvantageScope Lite back* | Step A **+** `page.j5155.AdvantageScope:lite:v26.0.0` | AdvScope Lite is **not** the 8080 collider — `CLAUDE.md`'s entry is wrong again | AdvScope Lite **is** the collider — #56 was right and something re-added it |
| **C** | *Step A minus AutoTune* | Sloth + Pedro core/revhub + Ivy + CachingHardware + Panels + Dashboard | AutoTune's `TunerScanner` is what pushes boot past the watchdog timeout | Boot is slow for some other reason; measure the next-longest scanner |

Run A first. B is only meaningful if A boots. **C was added after A ran — see below.**

## Result of step A, 24 Sep 2026

> **History note.** Everything above this section was written before the step-A capture and
> assumed a `BindException`. The capture does not support that, and the "if it crashes" column
> for step A is the wrong dichotomy. Read the table above as the plan that was made, not as a
> description of what is wrong.

**There is no `BindException` anywhere in the step-A capture.** `CoreRobotWebServer` binds 8080
and `TooTallWebSocketServer` binds 8081, once each, and both succeed. Nothing tries for a third
port. Whatever produced the earlier trace, it is not reproducible on this tree.

**Step A booted.** PID 19363 reached `Robot Status: running`, enumerated the whole DECODE
hardware map (`lf`, `rb`, `pinpoint`, `launcher_*`, `limelight`, …), discovered the Driver
Station at `192.168.43.21`, and sent `CMD_NOTIFY_OP_MODE_LIST`. It then ran for about a minute
until an `am force-stop` killed it — that kill is Android Studio starting the next install
(`dex2oat ... vmdl2146490354.tmp`), not a crash.

What the capture *does* show, twice:

| Fact | Evidence |
|---|---|
| The RC ANRs during boot, on the watchdog service | `ANR in com.qualcomm.ftcrobotcontroller / Reason: executing service …FtcRobotControllerWatchdogService`, at 20:50:49 and again at 20:52:17 |
| One thread is pinned at ~100% user CPU across the ANR window | `97% 19502/default threadp` (cycle 1), `94% 20405/default threadp` (cycle 2) |
| That thread is running AutoTune's scanner | `running scanner com.pedropathing.tuning.autotune.TunerScanner` 20:50:39.261 → `finished` 20:50:51.488 — **12.2 s**. In cycle 2 it starts at 20:52:08.068 and never logs `finished` before the process is killed 13 s later |
| It is the only non-Sloth scanner in the boot list | Sinister's `found scanner` lines: `TunerScanner` plus twelve `dev.frozenmilk.sinister.*` ones |
| The APK really was step A | Sloth registered only `{Basic: Omni Linear OpMode}`, `Test Gamepad`, `Test Hardware`, `Manual Control`, `Stop Robot` — none of our five |

So the failure mode to chase is **slow boot, not a port collision**: Sinister's boot runs on the
main thread and waits on its scanners, `FtcRobotControllerWatchdogService` has 20 s to return,
and `TunerScanner` alone eats 12–19 s of that. Add team code — more classes to scan, five more
OpModes to register — and the same boot has less headroom, which is a coherent account of *no
heartbeat with empty OpMode lists* that needs no third NanoHTTPD at all.

Two caveats worth keeping honest. The second cycle's 19 s is inflated: `dex2oat` for the
incoming install was competing for the same four cores. And an ANR on a service start is not
by itself fatal — cycle 1 ANR'd and still reached `running` — so the timeout is necessary but
maybe not sufficient, and the margin is what matters.

**`dev.frozenmilk.dairy:CachingHardware` is not implicated.** It appears nowhere in the
capture: no log tag, no class-load failure, and no registered Sinister scanner. It is a thin
wrapper around `DcMotor`/`Servo` that does nothing until an OpMode constructs one, and with no
team code in the APK nothing ever does.

### Confirming which build is actually on the hub

Bisecting by hand has already gone wrong once here, so check rather than assume:

- `git rev-parse --short HEAD` before you install.
- In logcat, `SlothTeamCodeLoader: hash discrepancy, application has changed` marks the first
  boot of a freshly installed APK. If you do not see it, the install did not take.

## What this spike deliberately does *not* change

- **Team source is not deleted.** `TeamCode/build.gradle` points `main.java.srcDirs` at a
  directory that does not exist. Every file is still on disk and still tracked; only the
  compiler stops seeing them. Deleting the `sourceSets` block restores the normal build.
- **`TeamCode/src/main/res/` is untouched**, including the bundled `robot_*.xml` configs.
  They are resources, not code, and the failure is a socket bind — removing them would change
  a second variable for no reason. If step A crashes, they are the next thing to rule out.
- **The locked versions are not touched.** Sloth `0.3.2` ⟂ Pedro AutoTune ⟂ the `0.3.2+…`
  Panels and Dashboard builds ⟂ `appcompat {strictly 1.2.0}` all stay as master has them.
  Bumping one to "see if it helps" would make the result uninterpretable.

## Rejected alternatives

- **`git bisect` across `06309a3..master` first.** Still the right tool if step A boots, and
  the history is branched so it must be `git bisect` and not a hand-ordered list of commits.
  But it costs a ~40 s install per step, and it is wasted entirely if the answer is that our
  code was never involved. Two installs settle that.
- **Deleting the team packages on this branch.** Same build output, but it loses the files,
  makes the diff unreadable, and makes going back a revert instead of deleting six lines.
- **Commenting out the Panels dependency.** Tried earlier in the week. Seven files import
  `com.bylazar.*`, so the build fails and the hub silently keeps the old APK — which is how a
  "fix" got tested that was never actually installed. Excluding all of our source at once
  avoids that class of mistake.
