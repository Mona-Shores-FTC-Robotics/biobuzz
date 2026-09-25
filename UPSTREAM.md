# Upstream report: Panels and FTC Dashboard cannot coexist under Sloth 0.3.2

Draft for the Dairy Foundation Discord `#help` (Oscar maintains both Sloth variants) and,
if it wants a written home, an issue on `Dairy-Foundation/Sloth` or `ftcontrol/ftcontrol-panels`.

Paste from the line below. It is written to stand alone — a maintainer should not need this
repository to reproduce it.

---

**Sloth 0.3.2: installing both `com.bylazar.sloth:fullpanels` and
`com.acmerobotics.slothboard:dashboard` crash-loops the Robot Controller on a `BindException`**

Each artifact works perfectly on its own. With both installed the RC never reaches
`Robot Status: running`: about 3.5 s after the SDK's web servers come up, a NanoHTTPD server
throws `BindException` on a bare thread, the RC dies, `FtcAccessPointService` relaunches it ~10 s
later, and it repeats until the battery is pulled. The Driver Station shows **no heartbeat and
empty OpMode lists**.

**Environment**
- REV Control Hub v1.0 (`REV3328`), Android SDK 25, RC app 12.0.0
- FTC SDK 12.0.0
- `dev.frozenmilk.sinister:Sloth:0.3.2` + `dev.frozenmilk:Load:0.3.2` Gradle plugin
- `com.bylazar.sloth:fullpanels:0.3.2+1.0.13`
- `com.acmerobotics.slothboard:dashboard:0.3.2+0.6.0`

**Reproduction** — a stock `FtcRobotController` fork with **no team-authored Java compiling at
all** (Java source sets pointed at an empty directory), so nothing of ours is in the APK:

```groovy
dependencies {
    implementation project(':FtcRobotController')
    implementation("dev.frozenmilk.sinister:Sloth:0.3.2")
    implementation("com.bylazar.sloth:fullpanels:0.3.2+1.0.13")
    implementation("com.acmerobotics.slothboard:dashboard:0.3.2+0.6.0")
}
```

Full install (not a Sloth hot reload — the dependency set changes), then watch for ~90 s.

**Result matrix**, all four measured on the same hub:

| Panels | Dashboard | Result |
|---|---|---|
| yes | yes | crash loop |
| yes | no | boots, holds a heartbeat |
| no | yes | boots, holds a heartbeat |
| no | no | boots |

It **survives a cold boot** — battery out, battery in — so it is not an install-time restart
race, and it is not stale Sloth payload (`/storage/emulated/0/FIRST/dairy/sloth` was cleared
and it still reproduces).

**The crash**

```
21:07:35.724  CoreRobotWebServer      started port=8080
21:07:35.733  TooTallWebSocketServer  Started WebSocket server on port 8081
21:07:35.740  RobotCore               Robot Status: stopped, scanning for USB devices
21:07:39.300  AndroidRuntime          FATAL EXCEPTION: Thread-13
                                      java.net.BindException: Address already in use
                                        at java.net.PlainSocketImpl.socketBind(Native Method)
                                        at java.net.ServerSocket.bind(ServerSocket.java:331)
                                        at fi.iki.elonen.NanoHTTPD$ServerRunnable.run(NanoHTTPD.java:1763)
                                        at java.lang.Thread.run(Thread.java:761)
21:07:40.008  RobotCore               Robot Status: stopped, internal error
```

The gap from `scanning for USB devices` to the fatal is metronomic: **3.56 s** and **3.51 s** on
two consecutive cycles.

**The contested port is 8001, and the collision is inside one process**

Sampling `netstat` once a second for 40 s, filtered to `:8001` and `:8080`, returned 13 lines
from 40 samples — 4 with both ports, 5 with 8001 alone, 31 with neither. So 8001 is up for ~9 s
of each ~40 s cycle: it appears about when 8080 does, outlives 8080 by ~5 s (the gap between the
fatal and the process kill), and goes away when the process dies. `adb shell ps` at the same time
showed **exactly one** `com.qualcomm.ftcrobotcontroller` process, so there is no second instance
and no external squatter. Something binds 8001, and something in the same process then asks for
8001 again.

With Dashboard removed and Panels alone, 8001 and 8080 are both simply held, steadily, with no
gaps.

**One possibly relevant observation.** Sinister runs its load pass twice per boot — the second
fired by `SlothTeamCodeLoader` after the main boot completes — and both Panels plugin scanners
appear in each pass:

```
21:08:05.583  running scanners for load          <- pass 1
21:08:05.585    running scanner com.bylazar.configurables.Plugin
21:08:10.311    running scanner com.bylazar.opmodecontrol.Plugin
21:08:10.671  ...booted
21:08:10.674  SlothTeamCodeLoader: Processing TeamCode Load
21:08:10.704  running scanners for load          <- pass 2
21:08:10.706    running scanner com.bylazar.configurables.Plugin
21:08:10.768    running scanner com.bylazar.opmodecontrol.Plugin
```

The fatal follows pass 2, and lands ~200 ms after the JIT compiles
`com.bylazar.panels.plugins.PluginsManager.init`. We could not get further than that from the
outside — whether the second pass re-initialises a server that pass 1 already stood up, and why
Dashboard's presence is required to trigger it, needs someone who knows the internals.

**What we ruled out**
- Not our code — no team-authored Java compiles in the reproduction.
- Not an install race — reproduces from a cold boot.
- Not stale Sloth payload — cleared, still reproduces.
- Not a second process — `ps` shows one.
- Not an outdated version — `fullpanels 0.3.2+1.0.13` is the only Sloth-variant build published,
  and upstream Panels is at 1.0.13.
- Not a duplicate Panels or Dashboard pulled in transitively — Pedro `core`/`revhub`/`ivy` declare
  neither.
- Not AdvantageScope Lite, which we had previously blamed for an 8080 collision; it is not in
  this APK and 8080 binds cleanly.

**Happy to run anything on the hub.** We have adb access and can reproduce on demand in about
40 seconds.
