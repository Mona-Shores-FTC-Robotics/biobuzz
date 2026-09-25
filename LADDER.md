# Ladder: add one library at a time until it breaks

The spike went top-down — start from the full set, remove things, see what helps. It found
that removing Panels fixes the boot, and then stalled, because removing a library changes
timing as well as behaviour and the two are hard to tell apart.

This goes bottom-up instead. Rung 0 is the stock SDK and nothing else. Each rung adds exactly
one library to the rung before it. The first rung that fails names the library, and the rung
before it proves everything underneath is fine.

**The fault is the pair.** Panels with Dashboard removed boots; Dashboard with Panels removed
boots. Neither is broken alone. See "Where this stands".

## The rungs

Each is a commit on `claude/ecstatic-mendel-0f68tz`. `git checkout <hash>`, install with the
**TeamCode** run configuration (a full install, not Sloth Load — the dependency set changes
between rungs), wait 90 s, record the result.

| Rung | Commit | Adds | Boots? | Notes |
|---|---|---|---|---|
| **0** | `1167036` | stock SDK only | | |
| **1** | `e4bd21b` | + Sloth runtime and Load plugin | | |
| **2** | `f2ad429` | + Panels (no Dashboard) | not run | superseded — see below |
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

> **History note.** This section first recorded a rung 2 pass. Rung 2 was not what ran — the
> install was a hand-edit of the working tree with only `com.acmerobotics.slothboard:dashboard`
> commented out, so Panels *and* the rest of the stack were present. The conclusion is the same
> and the evidence is stronger, but the commit named was wrong.

**Result, 24 Sep 2026: with FTC Dashboard removed and Panels present, the hub boots.** Sampling
`netstat` once a second showed `:::8001` and `:::8080` both `LISTEN` on every sample, no gaps. In
the failing configuration 8080 is up ~4 s of each ~40 s cycle and 8001 ~9 s; here both are steady.

Put that beside step D from `SPIKE.md`, which removed **Panels** and kept **Dashboard**, and also
booted:

| Panels | Dashboard | Result |
|---|---|---|
| yes | yes | crash loop |
| yes | **no** | **boots** |
| **no** | yes | **boots** |
| no | no | (not needed) |

**Neither library is broken on its own. The two of them together are.** That is a pairwise
interaction, and it is a far better lead than "Panels is the problem" — which is what the spike
concluded and which this contradicts.

One detail still to confirm: whether team source was compiling in that run. It does not change
the pairing, but it changes how small the reproduction is.

**Rung 3 (`d129b48`) is still the install worth doing**, because it pins the reproduction to
three lines on an APK with no team code and nothing else in it:

```groovy
implementation("dev.frozenmilk.sinister:Sloth:0.3.2")
implementation("com.bylazar.sloth:fullpanels:0.3.2+1.0.13")
implementation("com.acmerobotics.slothboard:dashboard:0.3.2+0.6.0")
```

If rung 3 crash-loops, that is the whole bug report. If it passes, something further up the
ladder is needed to trigger the pair, and rungs 4–7 find it the same way.

Both artifacts declare `org.nanohttpd:nanohttpd-websocket:2.3.1` and both exclude the core
`nanohttpd`, expecting the SDK's bundled `fi.iki.elonen` classes — which is exactly the class in
the crash stack. Neither declares a dependency on the other. That is as far as the POMs go; the
rest needs the maintainers.

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
