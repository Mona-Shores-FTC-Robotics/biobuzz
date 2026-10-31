# Auto sources (`.pp` files)

Every Autonomous drawn in our [Visualizer fork](https://mona-shores-ftc-robotics.github.io/Visualizer/)
has its `.pp` file committed here, in the same PR as the Java generated from it. The Java goes in
`opmodes/auto/generated/` (package `org.firstinspires.ftc.teamcode.opmodes.auto.generated`).

**New to the Visualizer?** Start with [How to plan an Auto](how-to-plan-an-auto.md).

- **The `.pp` is the source; the Java is generated.** Change an Auto by opening its `.pp` in the
  Visualizer, editing it, and exporting again (Export → Export Auto (Java)). Never edit the
  generated Java by hand: the next export overwrites it.
- **The file name is the one the Java names.** A generated class says where it came from:
  `public static final String SOURCE = "hive-rush.pp";`. That file goes here, under exactly that
  name. `AutoSourcesTest` fails CI when it is missing.
- **Commit both together.** A PR that changes an Auto shows the path change as a diff of the
  `.pp`, and a reviewer can open that exact file in the Visualizer (the upload button in its top
  bar).
- **The file carries the robot's size and motion settings.** They time the preview and the park
  guard's seconds in the Java, so the same `.pp` exports the same Java on any laptop.
- **Each Auto gets a five-line OpMode** in `opmodes/auto/`, `<Class>OpMode extends BuiltAuto`
  (see `BuiltAuto`'s javadoc), which is what the Driver Station lists. The generated class itself
  is not an OpMode.

## Team spots (`spots.json`)

`spots.json` here is the team's list of named spots (PARK, LAUNCH_LEFT…): poses every Auto can
use, so nobody types or places them again. They work like PathPlanner's linked waypoints.

- **Using one.** In the Visualizer, pick it from a step's spot dropdown (under "Team spots"), or
  select a path and press 🔗 on its end in the Path panel, then pick the spot: the end jumps to it
  and stays on it. The `.pp` keeps a copy of the pose and lists the name in `auto.teamSpots`.
- **Making one.** 🔗 on a path's end → *Make team spot*, or tick *Also a team spot* when naming a
  new spot from a field click.
- **Locked by default.** Dragging a team spot's end snaps it back. Press its lock (on the step, or
  in the Path panel) to unlock it and move it. The move is a change in that browser until saved.
- **Saving.** *Save team spots…* writes `spots.json` and re-exports every Auto here that uses a
  changed spot, `.pp` and Java, in one commit on a new `spots/…` branch, then opens a pull
  request. Once it is merged, every Auto and the robot code use the new spot. Saving needs the
  same GitHub token as Save to GitHub.
- **Never edit `spots.json` by hand.** Change spots in the Visualizer, so the Autos that use them
  are re-exported in the same commit. A hand edit changes no Auto until each is opened and
  exported again.
- **Starts are team spots named `START_…`.** The builder's start buttons are the team's
  `START_` spots (`START_RIGHT` → *Right start*); make one to add a start. A start measured on the
  field is also in `localization/StartPositions` (`TEAM_SPOTS` names its spot), and
  `TeamSpotsTest` fails while the two differ: measure, then change both. `START_LEFT` is where the
  left-side Autos were drawn to start, not measured yet, so the robot treats it as "start (as
  drawn)".
- Poses are drawn for RED, in Pedro's frame (inches, degrees), like everything in an Auto; the
  robot turns them for BLUE.

## Parking when time runs short

The endgame guard (`autokit/AutoKit.guarded`) gets the robot to PARK by 30 s:

- **Before each drive or grab it asks: can I do this, then still park from where it leaves me?**
  If not, the robot parks now, from the spot it is on. A wait or a launch already running is cut
  when the park needs the time; a drive, a pick or a gather never is.
- **Each spot has its own park path,** made by the Visualizer's builder: a straight line to the
  PARK spot, shared by every drive to that spot. Press **P** on a drive step to see it (everything
  else dims) and bend it with a control point. The drive just before Park needs none.

## Opening a committed Auto from a link

Once a `.pp` is pushed, anyone with a laptop and a browser can open it, no login needed:

| Link | Opens |
|---|---|
| `https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=hive-rush.pp` | the latest `hive-rush.pp` on `master` |
| `https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=a1b2c3d/hive-rush.pp` | exactly that commit, forever |
| `https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/some-branch/hive-rush.pp` | the latest on a branch, e.g. a PR's |

- **In a PR that changes an Auto,** link the branch form so reviewers see the path, not just the Java.
- **For "this is the Auto we ran"** (a match, a meeting note), use the commit form: it never changes.
- A branch can show the previous push for up to 5 minutes; a commit is always exact.
- It opens as a copy with a "From biobuzz …" bar. The viewer's own work is set aside, not saved
  over. To change the Auto, edit the `.pp` here and push, as above.

The file format, including the `auto` section, is described in the Visualizer's
[`docs/auto-format.md`](https://github.com/Mona-Shores-FTC-Robotics/Visualizer/blob/main/docs/auto-format.md).

## Why here, and what was left out

- **`TeamCode/autos/`, not next to the Java in `src/main/java`.** Outside the source tree nothing
  in Gradle, lint or the APK ever sees these files. Next to the Java would pair them visually but
  depends on the Android build ignoring non-Java files there.
- **Not checked: that the Java still matches its `.pp`.** Regenerating in CI would need Node and
  the Visualizer repo in this build. To check by hand, from a Visualizer checkout:
  `node scripts/export-auto.mjs <path>/TeamCode/autos/hive-rush.pp /tmp/out`, then compare with
  the committed class.
- **`FieldFrameTest` skips `opmodes/auto/generated/`.** Generated Autos contain `mirrorAroundPoint(70.75, 70.75)`
  (the half turn to the other alliance) and wall coordinates such as 141.5; they come from the Visualizer's own 141.5 in field, not
  from someone typing a field size.
- **Short links need the repo public.** They read the file from GitHub without a login; if
  biobuzz is made private, they stop working for everyone.

Decided in #118.

## Wait limits (`limits.json`)

Each Auto waits for the robot before moving on: a FLOWER's 4 (`IntakeFull`), a fire step (`Empty`), a catch. A wait
ends when the sensor says so, or at its **limit**. The limits for the baseline Autos (L-Quals, R-Quals, Sister,
L-Simple, R-Simple) are in one file, `limits.json`, each with where its value came from (`estimate`, `sim` or
`measured`) and how to time it:

| Limit | Now | What it is |
|---|---|---|
| `flowerS` | 3.0 s | a FLOWER's 4 with the extractor seated |
| `gardenS` | 1.5 s | the GARDEN's 4 |
| `catchS` | 1.5 s | standing at a catch spot after a TIP |
| `fireFirstS` | 3.0 s | the Auto's first fire: spin-up, aim and 4 shots |
| `fireS` | 2.0 s | any later fire of up to 4 pieces |

They start generous: a limit that's too long only wastes time, one that's too short leaves pieces behind. Without a
working full or empty sensor a limit is the whole wait, so tighten it once the robot is timed
(`doc/sensing-study.md`: R-Quals keeps its 3 TIPs with no full sensor if `flowerS` is 1.8 s, not at 2.2 s).

**To change one:**

1. Time it on the robot (10 tries; the slowest is what matters) and edit its `value` in `limits.json`, setting
   `source` to `measured` and noting the date and how.
2. Re-export the Autos that use it: from `tools/auto-routes/`, `python3 baselines_v.py 0` (L-Quals, R-Quals),
   `python3 simple.py` (L-Simple, R-Simple), and Sister's pair from `sister.py`. The `.pp` and generated Java change
   together; commit them with `limits.json` in one PR.
   **Careful since the 31 Oct 2026 merge of `master`:** `l-quals.pp`, `r-quals.pp`, `l-simple.pp` and `r-simple.pp`
   are now `master`'s, edited in the Visualizer and exported with the team spots and the measured start.
   `baselines_v.py` and `simple.py` write the older simulator drawings over them, so for those four change the wait
   in the Visualizer instead, and keep `limits.json` in step by hand.
3. Check the Autos still run: `tools/auto-routes/batch/both.sh` (README there), or the simulator's published rows.

The limits are written into each exported Auto, so the robot runs whatever was last exported; changing them on the
robot without a re-export (Panels) is issue #182. Game timing (how long a TIP takes, when a spill lands)
is not in `limits.json`: that's the field, not the robot. `timing.json` beside it holds the Visualizer preview's
timing facts.
