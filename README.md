# BIOBUZZ (Mona Shores FTC 19429 & 20245): `claude/simulator`

The qualifier Autos, the simulator that scores them, and their logs. `master` is robot code only.

## Latest

Run **7 Oct 2026 19:50 UTC**, 60 simulated runs each, on the baseline robot: the Rigid V with the FLOWER extractor,
a turret and the transfer ([what that is](doc/simulator.md), [what the simulator knows](doc/what-the-simulator-knows.md)).

| Auto | Points | 3 TIPs | PARK | Log | Route |
|---|---|---|---|---|---|
| **L-Quals** (`l-quals`): the qualifier Auto from the drive team's **left** start (red (59, 133.69)); no turret needed. Copes with any partner: TIP 3 from the wall FLOWER's 4 and the GARDEN's 4 fired from x 45; if the partner's TIP 1 has not come in 7 s, we fire our preloads at the right CELL, make TIP 2 and PARK (53.7 against a partner that never shoots) | **73.7** | 54 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/l-quals/L-Quals_RigidV_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/l-quals/L-Quals_RigidV_2026-10-08_typical.wpilog) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/L-Quals) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/l-quals.pp) |
| **Stages, angled partner** (`qual-stages-angled-v`): partner can't shoot | **65.0** | 35 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-angled-v/Qual-PartnerStages-Angled_RigidV_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-angled-v/Qual-PartnerStages-Angled_RigidV_2026-10-08_typical.wpilog) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Angled) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-angled-v.pp) |
| **Stages, wall partner** (`qual-stages-wall-v`): partner can't shoot | **53.3** | 2 TIPs in 56 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-wall-v/Qual-PartnerStages-Wall_RigidV_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-wall-v/Qual-PartnerStages-Wall_RigidV_2026-10-08_typical.wpilog) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Wall) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-wall-v.pp) |
| **A whole match** (`match`): our L-Quals pair on red against our R-Quals pair on blue, all four robots, the other alliance's pieces, spills and traffic. The table's points are red's (L-Quals); blue (R-Quals) scores 76.0; no collisions in 60 runs | **74.7** | 57 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/match-l-vs-r/Match-LQuals-vs-RQuals_RigidV_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/match-l-vs-r/Match-LQuals-vs-RQuals_RigidV_2026-10-08_typical.wpilog) | [L-Quals](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/L-Quals) · [R-Quals](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/R-Quals) |
| **ShootsRight, flower first** (`qual-right-v-flower-first-carry-settle-b2`, the body-designs chat; **needs the turret**, it fires from the FLOWER's seat, the turret slewing at the meeting's 240 deg/s since 9 Oct; an option, not the baseline): to the far FLOWER first, the preloads fired from its seat once TIP 1 is up, its 4 fired from N_FIRE, the GARDEN's 4 for TIP 3, then it waits at S_FIRE facing the HIVE, catches TIP 3's spill as it rolls south and carries it to a north PARK (partner parked at the zone's south end) | **73.7** | 55 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-flower-first-carry-settle-b2/Qual-PartnerShootsRight-FlowerFirst_RigidV_2026-10-09_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-flower-first-carry-settle-b2/Qual-PartnerShootsRight-FlowerFirst_RigidV_2026-10-09_typical.wpilog) | [route](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/qual-right-v-flower-first-carry-settle-b2.pp) |
| **R-Quals** (`r-quals`): the qualifier Auto from the drive team's **right** start (red (59, 8.06)); no turret needed. 3 TIPs on its own: TIP 1 from the preloads, its spill caught and fired, the far FLOWER's 4 for TIP 2 (fired until the right CELL is up, so a partner's TIP 2 ends it), its spill caught and fired from x 45, the wall FLOWER's 4 for TIP 3, PARK. With a partner that only parks: TIP 3 in 38 of 60. The partner must be out of its way: one that can't shoot starts at the west start (24, 132.25); one shooting from the standard left start is clear of it by about 7 s | **74.0** | 55 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/r-quals/R-Quals_RigidV_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/r-quals/R-Quals_RigidV_2026-10-08_typical.wpilog) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/R-Quals) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/r-quals.pp) |
| **Sister** (`sister-right-fixed` + `sister-left-fixed`, the body-designs chat, 8 Oct): **two of our robots**, the playoff best case with a sister team; no turret needed. One works each end of the HIVE and carries pieces across when its end runs out: TIPs at about 4, 12, 19 and 27 s, both PARK. A 5th TIP was looked at and not tried: only TIP 4's spill is left, at the wrong end, too late (`doc/unified-design.md`) | **93.0** | 4 TIPs in 53 of 60 | 120 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/sister/Sister-4TIPs_RigidVFixedTurret_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/sister/Sister-4TIPs_RigidVFixedTurret_2026-10-08_typical.wpilog) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/sister-right-fixed.pp) · [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/sister-left-fixed.pp) |
| **Sister five** (`sister5i-right` + `sister5h-left`, the body-designs chat, 9 Oct; **needs the turret**): the Sister pair going for a 5th TIP. After topping up TIP 1's catch, R checks what it holds: 4 aboard, it carries on to 5 TIPs and parks if time allows; fewer, it falls back to the 4-TIP plan with PARK. The chat measured 95.6 on 40 runs; on today's simulator the 10 runs that stall at 2 or 3 TIPs cost more than the 5th TIP earns, so Sister (4 TIPs) still scores more | **92.3** | 5 TIPs in 17 of 60, 4 in 33 | 64 of 120 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/sister5/Sister-5TIPs_RigidV_2026-10-09_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/sister5/Sister-5TIPs_RigidV_2026-10-09_typical.wpilog) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/sister5i-right.pp) · [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/sister5h-left.pp) |
| **ShootsRight, firing from the FLOWER seats** (**needs the turret**; shelved, 7 Oct: not realistic from that FLOWER's seat as the extractor is drawn; an extractor that approaches the far FLOWER from its right side is the open idea) | **72.7** | 52 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-seatfire-west/Qual-PartnerShootsRight-SeatFire_RigidV_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-seatfire-west/Qual-PartnerShootsRight-SeatFire_RigidV_2026-10-08_typical.wpilog) | [route](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/qual-right-v-seatfire-west.pp) |

**Which qualifier Auto to run** (mentor, 8 Oct): partner can't shoot → **R-Quals**, with the partner starting near
its park, away from the standard left start. Partner shoots reliably → either. Partner says it shoots but might not →
**L-Quals** (54 of 60 if it shoots; 2 TIPs in 53 if it doesn't). Read every 3-TIP count as ±4: the alone and
whole-match rows moved by that much when a rounding knife edge in the HIVE-side contact was fixed (8 Oct). A partner that can't shoot should not be paired with
L-Quals: it waits until 9.2 s for a TIP 1 that never comes and gets 2 TIPs at most. The full version:
[doc/unified-design.md](doc/unified-design.md), "The qualifier Autos: which to run".

**In short.** The simulator now has the event stream's physics ([issue 167](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/167),
[168](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/168)): the HIVE dwells 0.25–3.4 s before it
tips, a volley fires a shot every 0.2 s (not 0.45), a spill heads for the alliance wall, NECTAR rolls freer. The
fast volley loads a CELL past its weight so it dwells least, and every Auto gained. ShootsRight (8 Oct, the
body-designs chat's route, adopted) makes 3 TIPs in 54 of 60 with the turret held still and still scores 53.7 when
the partner never fires; seat fire is shelved and the turret is not on Auto's critical path. The angled Auto's GARDEN load is back (TIP 3 in 35 after the contact fix; 23 before); the wall Auto
still parks after TIP 2's spill. The robot holds what the transfer's lane fits (4 POLLEN, or 3 NECTAR and a
POLLEN; adopted 7 Oct, no Auto's numbers moved). Both robots PARK in every run. No robot drives through a wall (the simulator
flags it, like the HIVE frame and a FLOWER). Known: the angled Auto clips the HIVE frame in the 4 runs TIP 1 fails; R-Quals, in 1 run of 60, is cut by the
endgame guard as it leaves the wall FLOWER and the park path drags it through the FLOWER (the guard should back out first).
Everything behind these numbers: [doc/simulator.md](doc/simulator.md).

## Watch a match

**Set up once** (Windows): install [AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope/releases)
v27 alpha 6 or newer, [Git](https://git-scm.com/download/win) and [Android Studio](https://developer.android.com/studio);
open AdvantageScope, **+** → **3D Field** → pick **2026-2027 Field**, wait for it, close it. Then run the update below
and do **File → Import Layout…** → `Downloads\advantagescope-layout.json`.

**Every time** (one command; seconds, unless the asset code changed), in PowerShell:
```powershell
$b = "claude/simulator"; if (Test-Path $HOME\biobuzz) { git -C $HOME\biobuzz fetch -q origin $b; git -C $HOME\biobuzz checkout -q $b; git -C $HOME\biobuzz pull -q origin $b } else { git clone -q -b $b https://github.com/Mona-Shores-FTC-Robotics/biobuzz.git $HOME\biobuzz }; powershell -ExecutionPolicy Bypass -File $HOME\biobuzz\tools\advantagescope\setup-advantagescope.ps1 -Open l
```
That pulls the branch, refreshes the robot model, downloads the table's logs to **`Downloads\biobuzz-logs`**, and
opens AdvantageScope on the L-Quals best log. `-Open r`, `match`, `flowerfirst`, `angled`, `wall` or `seatfire` for
the others, `-Typical` for the typical run, or `-Open <path to a .wpilog>`. Once the clone exists, a menu does the
same: `tools\advantagescope\watch-biobuzz.cmd` (double-click it, or a shortcut to it on the desktop); it pulls first,
so its choices are always this table's. Press **Space**; AUTO starts 1 s in. It tells you if the layout
changed (then **File → Import Layout…** once). The robot is the team's CAD (`cad/advantagescope/`); the partner is
the green ghost.

## Try a change

Open an Auto with its **ours** link, change it, **Save to GitHub** (once: a
[fine-grained token](https://github.com/settings/personal-access-tokens/new), biobuzz only, Contents read and write,
Actions read), robot **rigid V**. The [Simulate Auto](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/actions/workflows/simulate-auto.yml)
workflow runs it and the dialog shows every run with **Download WPILOG**.

## More

[The long version: robot, Autos, research, branches](doc/simulator.md) ·
[what the simulator knows and guesses](doc/what-the-simulator-knows.md) ·
[route scripts and every study's numbers](tools/auto-routes/README.md) ·
[who is working on what](doc/threads.md) ·
[next meeting's checklist](doc/next-meeting-checklist.md) ·
[Visualizer](https://mona-shores-ftc-robotics.github.io/Visualizer/) ·
[`sim-results`](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/tree/sim-results) ·
[Panels](http://192.168.43.1:8001) (robot Wi-Fi) ·
[the SDK's own README](doc/ftc-sdk-readme.md)
