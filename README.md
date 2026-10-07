# BIOBUZZ (Mona Shores FTC 19429 & 20245): `claude/simulator`

The qualifier Autos, the simulator that scores them, and their logs. `master` is robot code only.

## Latest

Run **7 Oct 2026 18:30 UTC**, 60 simulated runs each, on the baseline robot: the Rigid V with the FLOWER extractor,
a turret and the transfer ([what that is](doc/simulator.md), [what the simulator knows](doc/what-the-simulator-knows.md)).

| Auto | Points | 3 TIPs | PARK | Log | Route |
|---|---|---|---|---|---|
| **ShootsRight** (`qual-right-v`): partner fires its 4 from the right start | **69.5** | 43 of 60 | 58 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v/Qual-PartnerShootsRight_RigidV_2026-10-07_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v/Qual-PartnerShootsRight_RigidV_2026-10-07_typical.wpilog) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-ShootsRight) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-right-v.pp) |
| **Stages, angled partner** (`qual-stages-angled-v`): partner can't shoot | **52.2** | 2 TIPs in 55 | 55 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-angled-v/Qual-PartnerStages-Angled_RigidV_2026-10-07_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-angled-v/Qual-PartnerStages-Angled_RigidV_2026-10-07_typical.wpilog) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Angled) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-angled-v.pp) |
| **Stages, wall partner** (`qual-stages-wall-v`): partner can't shoot | **52.3** | 2 TIPs in 54 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-wall-v/Qual-PartnerStages-Wall_RigidV_2026-10-07_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-stages-wall-v/Qual-PartnerStages-Wall_RigidV_2026-10-07_typical.wpilog) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Wall) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-wall-v.pp) |
| **ShootsRight, firing from the FLOWER seats** (experiment, not yet the baseline) | **72.0** | 50 of 60 | 60 | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-seatfire-west/Qual-PartnerShootsRight-SeatFire_RigidV_2026-10-07_best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/qual-right-v-seatfire-west/Qual-PartnerShootsRight-SeatFire_RigidV_2026-10-07_typical.wpilog) | [route](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/TeamCode/src/test/resources/auto-builder/experiments/qual-right-v-seatfire-west.pp) |

**In short.** Both robots PARK in every pairing. TIP 3 comes only from static loads fired in time (a FLOWER's 4, the
GARDEN's 4), and the intake is what limits every Auto. Firing from the extractor's seat while it feeds (seat fire)
makes TIP 2 two seconds sooner and is the way to 3 TIPs when the partner can't shoot; it goes into all three Autos
once its log looks right. Known: the angled Auto clips the HIVE frame in 5 runs when TIP 1 fails. Everything behind
these numbers: [doc/simulator.md](doc/simulator.md).

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
