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
