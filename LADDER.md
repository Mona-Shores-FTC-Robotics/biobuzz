# Ladder: add one library at a time until it breaks

The spike went top-down — start from the full set, remove things, see what helps. It found
that removing Panels fixes the boot, and then stalled, because removing a library changes
timing as well as behaviour and the two are hard to tell apart.

This goes bottom-up instead. Rung 0 is the stock SDK and nothing else. Each rung adds exactly
one library to the rung before it. The first rung that fails names the library, and the rung
before it proves everything underneath is fine.

**Rung 2 passed.** Panels alone boots and holds both ports steadily. The fault is Panels
**together with** FTC Dashboard. Rung 3 is the confirming install — see "Where this stands".

## The rungs

Each is a commit on `claude/ecstatic-mendel-0f68tz`. `git checkout <hash>`, install with the
**TeamCode** run configuration (a full install, not Sloth Load — the dependency set changes
between rungs), wait 90 s, record the result.

| Rung | Commit | Adds | Boots? | Notes |
|---|---|---|---|---|
| **0** | `1167036` | stock SDK only | | |
| **1** | `e4bd21b` | + Sloth runtime and Load plugin | | |
| **2** | `f2ad429` | + Panels (no Dashboard) | **YES** | 8001 and 8080 both held continuously, 24 Sep 2026 |
| **3** | `d129b48` | + FTC Dashboard (both present) | | |
| **4** | `7ddea32` | + Pedro core and revhub | | |
| **5** | `9c1fd59` | + Ivy | | |
| **6** | `f92dd26` | + CachingHardware | | |
| **7** | `7d3911c` | + AutoTune | | |
| **8** | `cddcd35` | + team source (the real build) | | |

No team-authored Java compiles until rung 8, so nothing of ours can be blamed for rungs 0–7.

## Where this stands: rung 2 passed, rung 3 is next

Every test the spike ran had FTC Dashboard present. Step D removed Panels and kept Dashboard.
**Panels without Dashboard was never tried**, and it is the most informative single install
available:

**Result, 24 Sep 2026: rung 2 boots.** Sampling `netstat` once a second showed `:::8001` and
`:::8080` both `LISTEN` on every sample with no gaps. In the failing configuration 8080 appears
for ~4 s of each ~40 s cycle and 8001 for ~9 s; here both are simply steady.

So Panels alone is not the fault. Panels binds 8001, keeps it, and the Robot Controller lives.

**Rung 3 (`d129b48`) adds FTC Dashboard and nothing else.** If it crash-loops, the reproduction
is two dependencies on an APK with no team code:

```groovy
implementation("dev.frozenmilk.sinister:Sloth:0.3.2")
implementation("com.bylazar.sloth:fullpanels:0.3.2+1.0.13")
implementation("com.acmerobotics.slothboard:dashboard:0.3.2+0.6.0")
```

That is small enough for a maintainer to reproduce in one sitting, which is the whole point of
the ladder. If rung 3 *passes*, the fault is further up and rungs 4–7 find it the same way.

## Running a rung

```powershell
git checkout <hash>
# install with the TeamCode run configuration, then:
adb -s 7b7ca46838f5cdab logcat -c
adb -s 7b7ca46838f5cdab shell am force-stop com.qualcomm.ftcrobotcontroller
Start-Sleep -Seconds 90
adb -s 7b7ca46838f5cdab logcat -d -v time -s AndroidRuntime:E RobotCore:V Sinister:V SlothTeamCodeLoader:V CoreRobotWebServer:V TooTallWebSocketServer:V
```

**Pass** = `RobotCore: Robot Status: running` and a heartbeat on the Driver Station.
**Fail** = `FATAL EXCEPTION` with `java.net.BindException`, and the cycle repeating.

If a rung fails, the port is worth grabbing while it loops:

```powershell
1..40 | % { adb -s 7b7ca46838f5cdab shell netstat -an | findstr ":8001 :8080"; Start-Sleep -Seconds 1 }
```

## Before the first install

Two things, once, because the Sloth README says a stale payload can make an install silently
not take — and it names this as a common 2026-27 issue for Pedro 3 teams:

```powershell
adb -s 7b7ca46838f5cdab shell rm -rf /storage/emulated/0/FIRST/dairy/sloth/*
```

And wire the missing setup step so it cannot recur: **Run → Edit Configurations → TeamCode →
Before launch → + → Run Gradle task**, project `:TeamCode`, task `removeSlothRemote`, dragged to
the **top** of the list. The repo ships `.run/Sloth Load.run.xml` with `deploySloth` and has
never had the other half.

## What the spike already established

Kept here so the ladder does not re-test it:

- Removing `com.bylazar.sloth:fullpanels` makes the hub boot. Reproducible from a cold boot.
- Port **8001** is bound and then requested again **inside a single RC process** — `ps` showed
  one process, and 8001's lifetime tracks it.
- AutoTune's `TunerScanner` costs **12.2 s** of boot. Unrelated to the crash; real on its own.
- Ruled out: AdvantageScope Lite (not in the APK), an external squatter (there is none), Pedro
  bundling its own Panels or Dashboard (it does not), and a newer `fullpanels` (`0.3.2+1.0.13`
  is the only published build).

Full detail in `SPIKE.md`, including two readings that were wrong and why.
