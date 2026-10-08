# BIOBUZZ (Mona Shores FTC 19429 & 20245): `claude/simulator`

The qualifier Autos, the simulator that scores them, and their logs. `master` is robot code only.

## Latest

Run **7 Oct 2026 19:50 UTC**, 60 simulated runs each, on the baseline robot: the Rigid V with the FLOWER extractor,
a turret and the transfer ([what that is](doc/simulator.md), [what the simulator knows](doc/what-the-simulator-knows.md)).

| Auto | Points | 3 TIPs | PARK | Log | Route |
|---|---|---|---|---|---|
| **ShootsRight** (`qual-shoots-right-v-fixed-west45-r`, the baseline; no turret needed): partner fires its 4 from the right start; TIP 3 from the wall FLOWER's 4 and the GARDEN's 4, fired from x 45; if the partner's TIP 1 has not come in 7 s, we fire our preloads at the right CELL, make TIP 2 and PARK (53.7 against a partner that never shoots) | **75.0** | 58 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-shoots-right-v-fixed-west45-r/Qual-PartnerShootsRight-Candidate_RigidV_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-shoots-right-v-fixed-west45-r/Qual-PartnerShootsRight-Candidate_RigidV_2026-10-08_typical.wpilog) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-ShootsRight) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-shoots-right-v-fixed-west45-r.pp) |
| **Stages, angled partner** (`qual-stages-angled-v`): partner can't shoot | **61.0** | 23 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-angled-v/Qual-PartnerStages-Angled_RigidV_2026-10-07_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-angled-v/Qual-PartnerStages-Angled_RigidV_2026-10-07_typical.wpilog) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Angled) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-angled-v.pp) |
| **Stages, wall partner** (`qual-stages-wall-v`): partner can't shoot | **53.3** | 2 TIPs in 56 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-wall-v/Qual-PartnerStages-Wall_RigidV_2026-10-07_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-wall-v/Qual-PartnerStages-Wall_RigidV_2026-10-07_typical.wpilog) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Wall) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-wall-v.pp) |
| **ShootsRight, flower first** (`qual-right-v-flower-first-carry-settle-b2`, the body-designs chat; **needs the turret**, it fires from the FLOWER's seat; an option, not the baseline): to the far FLOWER first, the preloads fired from its seat once TIP 1 is up, its 4 fired from N_FIRE, the GARDEN's 4 for TIP 3, then it waits at S_FIRE facing the HIVE, catches TIP 3's spill as it rolls south and carries it to a north PARK (partner parked at the zone's south end) | **74.3** | 57 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-flower-first-carry-settle-b2/Qual-PartnerShootsRight-FlowerFirst_RigidV_2026-10-07_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-flower-first-carry-settle-b2/Qual-PartnerShootsRight-FlowerFirst_RigidV_2026-10-07_typical.wpilog) | [route](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/qual-right-v-flower-first-carry-settle-b2.pp) |
| **Alone: 3 TIPs with a partner that cannot shoot** (`qual-alone-p4-lane-r`, the body-designs chat, 8 Oct; the partner only parks, `partner-park-only`, and the Stages partners score the same since the route ignores the staged row): all 4 preloads for TIP 1 (a missed TIP 1 is recovered), its spill caught and fired, the far FLOWER's 4 fired from N_FIRE for TIP 2, its spill caught and fired from x 55, the wall FLOWER's 4 for TIP 3, PARK. Fires from spots, no seat fire, so it **needs no turret**: 68.0 with the turret held still (TIP 3 in 43), 67.3 with it aiming (40), no problem runs either way | **68.0** | 43 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-alone-p4-lane-r/Qual-Alone-3TIPs_RigidV_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-alone-p4-lane-r/Qual-Alone-3TIPs_RigidV_2026-10-08_typical.wpilog) | [route](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/qual-alone-p4-lane-r.pp) |
| **Partner shoots from the left start** (the alone route `qual-alone-p4-lane-r` with `partner-left-v`, the body-designs chat, 8 Oct): the partner fires at the left CELL as it rises, then parks at the zone's far end via y 124, out of our lane; we run the alone route and its TIP 2 comes 3 s sooner. Needs no turret (measured with the turret held still) | **71.7** | 49 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-alone-p4-lane-r-left/Qual-PartnerShootsLeft_RigidV_2026-10-08_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-alone-p4-lane-r-left/Qual-PartnerShootsLeft_RigidV_2026-10-08_typical.wpilog) | [route](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/qual-alone-p4-lane-r.pp) · [partner](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/partner-left-v.pp) |
| **ShootsRight, firing from the FLOWER seats** (**needs the turret**; shelved, 7 Oct: not realistic from that FLOWER's seat as the extractor is drawn; an extractor that approaches the far FLOWER from its right side is the open idea) | **72.7** | 52 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-seatfire-west/Qual-PartnerShootsRight-SeatFire_RigidV_2026-10-07_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-seatfire-west/Qual-PartnerShootsRight-SeatFire_RigidV_2026-10-07_typical.wpilog) | [route](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/qual-right-v-seatfire-west.pp) |

**In short.** The simulator now has the event stream's physics ([issue 167](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/167),
[168](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/168)): the HIVE dwells 0.25–3.4 s before it
tips, a volley fires a shot every 0.2 s (not 0.45), a spill heads for the alliance wall, NECTAR rolls freer. The
fast volley loads a CELL past its weight so it dwells least, and every Auto gained. ShootsRight (8 Oct, the
body-designs chat's route, adopted) makes 3 TIPs in 58 of 60 with the turret held still and still scores 53.7 when
the partner never fires; seat fire is shelved and the turret is not on Auto's critical path. The angled Auto's GARDEN load is back (TIP 3 in 23); the wall Auto
still parks after TIP 2's spill. The robot holds what the transfer's lane fits (4 POLLEN, or 3 NECTAR and a
POLLEN; adopted 7 Oct, no Auto's numbers moved). Both robots PARK in every run. No robot drives through a wall (the simulator
flags it, like the HIVE frame and a FLOWER). Known: the angled Auto clips the HIVE frame in the 4 runs TIP 1 fails.
Everything behind these numbers: [doc/simulator.md](doc/simulator.md).

## Watch a match

**Set up once** (Windows): install [AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope/releases)
v27 alpha 6 or newer, [Git](https://git-scm.com/download/win) and [Android Studio](https://developer.android.com/studio);
open AdvantageScope, **+** → **3D Field** → pick **2026-2027 Field**, wait for it, close it. Then run the update below
and do **File → Import Layout…** → `Downloads\advantagescope-layout.json`.

**Every time** (one command; seconds, unless the asset code changed), in PowerShell:
```powershell
$b = "claude/simulator"; if (Test-Path $HOME\biobuzz) { git -C $HOME\biobuzz fetch -q origin $b; git -C $HOME\biobuzz checkout -q $b; git -C $HOME\biobuzz pull -q origin $b } else { git clone -q -b $b https://github.com/Mona-Shores-FTC-Robotics/biobuzz.git $HOME\biobuzz }; powershell -ExecutionPolicy Bypass -File $HOME\biobuzz\tools\advantagescope\setup-advantagescope.ps1 -Open right
```
That pulls the branch, refreshes the robot model, downloads the table's logs to **`Downloads\biobuzz-logs`**, and
opens AdvantageScope on the ShootsRight best log. `-Open angled`, `wall` or `seatfire` for the others, `-Typical`
for the typical run, or `-Open <path to a .wpilog>`. Press **Space**; AUTO starts 1 s in. It tells you if the layout
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
