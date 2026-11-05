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

## Belding lineup update (5 Nov 2026)

Master's selected lineup is R-Solo, L-NoCross (formerly L-Partner), and R-NoCross
(formerly R-Partner-Tip). R-Solo-Nudge remains a draft with its pushed-partner stand-in;
the coordinated Infraviolet pair is later work.

Two more Autos are benched for Belding, preserved here exactly as they were on master
at commit `4409b98` (drawing, generated Java, OpMode and simulation request):

- **R-Partner-Preload** (`r-partner-preload`): no dedicated LEFT-preload partner arrangement
  is being supported at Belding. Negotiate a RIGHT preload start or a park/nudge arrangement.
- **R-Solo-Staging** (`r-solo-staging`): the staged-preload arrangement is outside the selected
  baseline; keep R-Solo as the reference instead.

These are preserved experiments, not enabled competition choices on master. Open them from
`autos-experiment` to resume work. This branch is an archive/bench and may retain older master
names; it is not the Belding deployment branch. Update it from master and resolve those names
before promoting another experiment.

## Earlier bench work

- **The four from before:** `r-simple`, `r-quals`, `l-simple`, `l-quals`, as last run on master.
- **The partner stand-ins:** `partner-r-still`, `partner-l-still`, `partner-park-1`, `partner-park-2`,
  `partner-r-launch-park`, `partner-l-launch-park`, for simulating a teammate beside one of ours.
  No OpModes; their spots are a teammate's, not the team's.
- **Earlier experiments:** `r-brady`, `r-quals-locked`, `r-quals-pollen-only`, `r-launch-park`,
  `l-launch-park`, `sister-left-fixed`, `sister-right-fixed`.

The 3-TIP swap drafts went back to master's `TeamCode/autos/drafts/` (#442), where Team Autos lists
them under "Drafts"; this branch gets them from master like everything else.
