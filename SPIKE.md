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

Run A first. B is only meaningful if A boots.

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
