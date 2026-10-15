# Auto sources (`.pp` files)

Every Autonomous drawn in our [Visualizer fork](https://mona-shores-ftc-robotics.github.io/Visualizer/)
has its `.pp` file committed here, in the same PR as the Java generated from it. The Java goes in
`opmodes/auto/generated/` (package `org.firstinspires.ftc.teamcode.opmodes.auto.generated`).

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
- **`FieldFrameTest` skips `opmodes/auto/generated/`.** Generated Autos contain `mirrorX(70.75)`
  and wall coordinates such as 141.5; they come from the Visualizer's own 141.5 in field, not
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
3. Check the Autos still run: `tools/auto-routes/batch/both.sh` (README there), or the simulator's published rows.

The limits are written into each exported Auto, so the robot runs whatever was last exported; changing them on the
robot without a re-export (Panels) is issue #182. Game timing (how long a TIP takes, when a spill lands)
is not in `limits.json`: that's the field, not the robot. `timing.json` beside it holds the Visualizer preview's
timing facts.
