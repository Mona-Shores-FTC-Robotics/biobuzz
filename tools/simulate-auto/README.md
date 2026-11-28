# Simulate a saved auto locally

Save the candidate in Visualizer's connected BIOBUZZ checkout first. This command uses that
checkout's generated Java and existing `SimRunTest`; it needs no commit, push, PR, or GitHub CI.
Run it from a terminal with Python 3.9+ and the Java/Android SDK used by this project's Gradle build.
Prepare dependencies before going offline; the initial Gradle build may need internet.

On Windows, use `py -3` in place of `python3`:

```bash
python3 tools/simulate-auto/simulate_auto.py r-solo --partner partner-still
python3 tools/simulate-auto/simulate_auto.py l-partner --partner partner-r-launch-park --last-shot 8
python3 tools/simulate-auto/simulate_auto.py r-solo --study --seeds 5
```

The default is one seed of the auto alone. `--partner` also runs it with that stand-in;
`--study` retains the auto's curated partner scenarios. Existing timing/failure settings are
kept. Each run has a separate folder under `TeamCode/build/local-sim/`, containing its request,
saved source/Java, `result.json`, and `.wpilog` logs for AdvantageScope. Open the logs using the
team's existing layout. Files changed during a run mark its results stale and fail the command.

This does not export a `.pp`, deploy to the robot, or change a simulation request in source
control. A draft is simulatable once Visualizer has saved its generated Java; it still needs a
`BuiltAuto` OpMode before it can be selected on the Driver Station. A browser preview and the
Java simulation are different models; simulation results apply to the saved files identified
in that run, not subsequent editor changes.

## Compare the RightUnderHive LEFT reception

On `auto/l-right-under-hive-nectar-210`, which contains the c5d65866 master pair and the exported L draft, run
`python tools/simulate-auto/nectar_study.py TeamCode/build/nectar-comparison`.
It creates a Fruitport request and a best-case request, runs both through
`SimRunTest`, and writes `summary.csv` and the paired observations in
`seeds.csv`. Use `--mode prepare` to produce the
JSON alone or `--mode summarize` to rebuild the table from completed requests.
Each request folder contains per-seed results, shot and delivery records, and replay logs.

Both arms use the same R Auto, turret designs, speed, pickup and failure
settings, roll and even-numbered field-2 seeds. Before the simulated rocker
can leave its stop, its raised CELL must hold at least 8.00 POLLEN-equivalents
(1.00 per POLLEN, 1.65 per alliance NECTAR) and satisfy the existing torque and
dwell model. This study premise applies to TIP 1 too: three POLLEN and the
three starting NECTAR weigh 7.95. Sixty seeds cover the nominal Fruitport
case and ten seeds cover each stress case and the best case. The seed counts
can be changed with `--nominal` and `--stress`. The Fruitport shot model uses
the 39.2-inch observation and retains Belding's rows at closer and longer
ranges. The Fruitport FLOWER distribution has 13 full post-pick volleys in
20 observations. Fruitport did not measure a GARDEN gather. The best case
fills the FLOWER and GARDEN loads and keeps every shot
except an injected miss. Both profiles use the existing range of TIP times.

The human roll follows the first settled TIP when the zone is clear. Its
nominal release point (5.5, 100.18), speed (13 in/s) and delay (1 s) are study
inputs. The draft aims the intake at the roll at (40.7, 120.5) with a heading
of 210 degrees. The second earned NECTAR has no route in this study. The rows
track the first piece by identity through pickup, launch, first CELL entry and
presence in the tipping load. A late piece can travel into another volley.
The park flags are sampled at 30 s. The simulator counts completed TIPs through
the transition to TELEOP. A recorded FLOWER overlap identifies a frame
intersection in the model during the FLOWER approach. It is reported separately
from robot contact, centre-line crossing and HIVE contact.
