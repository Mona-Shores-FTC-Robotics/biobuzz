# Controls study: which buttons the drive team pressed

`controls_study.py` reads match logs and says, for each TeleOp, what each gamepad pressed and what each press
did. It is for coaching the drive team (driver and operator workload, Square let go early, fires held back,
jams, idle stretches), not for blame: the log names the gamepad, never the student.

```
python3 tools/controls-study/controls_study.py report   LOG.wpilog.gz [...]   # per match: counts, fires, Square, trims, resets, idle, cycles
python3 tools/controls-study/controls_study.py timeline LOG.wpilog.gz         # every press in order, with what it did
python3 tools/controls-study/controls_study.py json OUT.json LOG.wpilog.gz [...]  # everything, for a page or a notebook
```

Event logs: `git fetch origin 'refs/heads/events/*:refs/remotes/origin/events/*'`, then
`git show origin/events/2026-11-fruitport:20245/matches/<file>.wpilog.gz > x.wpilog.gz`.

- **Presses** come from the raw gamepad state the match log writes on every change
  (`/DriverStation/Joystick0` = gamepad 1, the driver; `Joystick1` = the operator), so every button is
  observable, bound or not. A trigger is pressed past 0.3; a stick moved past 0.15.
- **Actions** come from the control layout of the build that ran: `Action.java` and `ControlScheme.java` at
  the log's `GitCommit` (`git show`), with the match TeleOp's rules applied. Belding's build is not
  Fruitport's (the operator's Circle was FIRE there). A shallow clone needs `git fetch --unshallow origin`;
  without the commit it falls back to the working tree and says so.
- **Steering while driving** (`heading` in the summary): seconds with the right stick past 0.1 while the left stick is
  past 0.5 and the robot moves over 3 in/s, and the steering presses per run over 36 in. Read from the raw axes,
  since the event builds had no `/Controls/StickTurn`. The ceiling on what a heading lock could take over.
- **Outcomes** come from the TeleOp's own event lines and series (fire lines and their gates, launch spot,
  FLOWER pick, GARDEN gather, launcher, trims, resets) and cycles from `read_logs.py`'s `cycle_rows`.
- A launcher line tagged `(operator)` means a spin-up that was not Square's (usually the driver's Circle), not
  an operator press. Options held with Cross or Circle mid-TeleOp is the SDK's pad-pairing gesture: a
  controller dropped.
