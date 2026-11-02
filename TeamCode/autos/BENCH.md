# The bench: `autos-experiment`

Autos that are not on master: the ones we are not running yet, or any more. This branch is master
plus their files (`.pp`, generated Java, OpMode), kept merged up with master so each still compiles
against the current robot code.

- **Work on one:** open it from this branch in the Visualizer (Team Autos, branch
  `autos-experiment`), edit, save back to this branch. Simulate from here too.
- **Run one on the robot:** check out `autos-experiment` in Android Studio and Sloth Load.
- **Promote one:** say it is ready. Its three files go to an `auto/<name>` branch and a pull
  request to master; from then on it is in the main loop (one draft branch per Auto, merge when
  better).
- **Demote one** the same way, the other direction.

An Auto here may use spots that are not the team's (`spots.json`); one promoted to master uses
only the team's.
