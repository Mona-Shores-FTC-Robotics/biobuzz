# Benched Autos

The active Belding lineup is **L-NoCross, R-NoCross and R-Solo**. R-Solo-Nudge is
a planning draft under `drafts/`; the Infraviolet pair is future work. A draft or a
partner stand-in has no selectable OpMode.

## Benched for Belding, 5 Nov 2026

| Auto | Why it is benched | Open the preserved drawing |
|---|---|---|
| R-Partner-Preload | We are not supporting a LEFT-only preload shooter. Negotiate a RIGHT shooting start or a park/nudge arrangement instead. | [Partner-Preload on the bench](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=autos-experiment/r-partner-preload.pp) |
| R-Solo-Staging | Collecting a partner's staged preloads is outside the selected baseline. | [Solo-Staging on the bench](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=autos-experiment/r-solo-staging.pp) |

Their latest `.pp`, generated Java, OpMode and simulation request from master
`4409b98` were copied to the existing `autos-experiment` branch **before** removal
from the active lineup. The immutable archive is
[bench commit 7e0c95a](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/commit/7e0c95a).
No disabled duplicate classes remain in the competition build.

Earlier experiments are listed in
[the bench branch's inventory](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/autos-experiment/TeamCode/autos/BENCH.md).
That branch is for experiments, not the Belding deployment lineup. Do not merge
the entire bench branch into master to restore one Auto. Promote only the chosen
drawing, generated class, OpMode and simulation request in a reviewed PR, updating
names and shared spots as needed.

The old staging collision remains a reason to qualify it before promotion: its
partner's staged row near `PARTNER_STAGED_POLLEN` can be swept by our approach to
`LEFT_LAUNCH_OFFSET` before collection. Bench preservation is not qualification.
