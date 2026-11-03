# Auto sources (`.pp` files)

Every Autonomous drawn in our [Visualizer fork](https://mona-shores-ftc-robotics.github.io/Visualizer/)
has its `.pp` file committed here, in the same PR as the Java generated from it. The Java goes in
`opmodes/auto/generated/` (package `org.firstinspires.ftc.teamcode.opmodes.auto.generated`).

**New to the Visualizer?** Start with [How to plan an Auto](how-to-plan-an-auto.md).

## Open the Autos on master

One link each, always the latest on `master` (the Visualizer reads the file from GitHub):

| Auto | Open it |
|---|---|
| L-Partner | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/l-partner.pp> |
| R-Solo | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-solo.pp> |
| R-Partner-Tip | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-partner-tip.pp> |
| R-Partner-Preload | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-partner-preload.pp> |
| R-Solo-Staging | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-solo-staging.pp> |

Inside the Visualizer, the same list is **View → Team Autos: the latest from biobuzz, together…**
(pick a branch, tick up to four Autos, Reload latest). A pull request's draft opens with its branch
in the link: `#team=auto/r-solo/r-solo.pp`.

**The polish loop.** Open the Auto from master, change it, **Save to GitHub and simulate…** under
the same name: that makes or updates its `auto/<name>` branch and pull request, the simulator runs
on it, and a mentor merges it once the robot has driven it. A change to a spot is **Save team
spots…** instead: its own pull request, and every Auto that uses the spot is re-exported in it.
Offline, **Save to repo folder…** writes the same two files (the `.pp` here and its Java in
`generated/`) straight into this checkout on the laptop; commit them on an `auto/<name>` branch
afterwards (or do Save to GitHub once online), so master has the Auto the robot ran.

**The competition loop.** The night before, with Wi-Fi: open the Visualizer once (it keeps an
offline copy of itself), open the Autos, and pick this checkout once in **File → Save to repo
folder…** (Chrome or Edge; the folder that holds `TeamCode`). At the event, no internet needed:
change the Auto, **Save to repo folder** (the browser asks once to allow the folder), then Sloth
Load from Android Studio over USB. The Files panel opens the checkout's Autos from the laptop, and
the team spots come from its `spots.json` while GitHub is out of reach. Commit the changed `.pp`
and Java on an `auto/<name>` branch when back online.

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
  is not an OpMode. Its name ends with the time the `.pp` was saved, Michigan time
  (`R-Simple 10-31|12:22AM`), from the generated class's `SAVED`: after a Sloth Load, the Driver
  Station shows which save is on the robot.

## The bench

Everything that is not one of the Autos we run lives on the `autos-experiment` branch (its
`BENCH.md` says what is there): the Autos from before and earlier experiments. So the Team Autos
list on `master` is exactly the Driver Station's list. To work on a benched Auto, open it from that
branch; to bring one back, say so and it comes over on an `auto/<name>` branch.

## Partner stand-ins (`partners/`)

`partners/` holds the Autos a teammate might run beside one of ours, for the simulator only: a
`.pp` each, its generated class in `opmodes/auto/generated/`, and no OpMode, so the Driver Station
never lists one. The Visualizer's Team Autos list reads the folder's own files and skips
subfolders, so none of these shows up as an Auto of ours. To open one, give the link the whole
path: `https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=TeamCode/autos/partners/partner-l-launch-park.pp`
(a branch goes in front: `#gh=master/TeamCode/autos/partners/…`). Their spots are a teammate's
(`PARTNER_START`, `PARTNER_PARK`), not the team's. `AutoSourcesTest`
accepts a `SOURCE` here as it does in the folder itself.

| Stand-in | What it does |
|---|---|
| `partner-r-still`, `partner-l-still` | Sits on the right or left start for the whole of AUTO. |
| `partner-park-1`, `partner-park-2` | Sits on the left start until 15 s, then parks (two different starts and park spots). |
| `partner-r-launch-park`, `partner-l-launch-park` | Spins up, waits for its own CELL to be up (at most 12 s), fires all four preloads at it, parks. The right CELL is up at the start, so the right one fires at once; the left one fires once our first TIP raises the left CELL. |
| `partner-l-tip-park` | The left launch-park, then the scoring FLOWER, four more at the left CELL (eight pieces: a TIP), then parks. |
| `partner-l-stage-park` | Starts against the wall facing the HIVE with its four POLLEN on the tiles along its left side (the simulator places them, `AutoStudyTest.alongLeftSide`, touching the frame as G304 asks), waits 2 s, drives off along the wall and parks, leaving them. |

A stand-in is drawn and exported like any Auto (Visualizer, or `node scripts/export-auto.mjs`
from a Visualizer checkout); a new one goes here with its class, and into the curation below.

### Which partners an Auto is simulated with

Each Auto's `TeamCode/sim-requests/<name>.json` lists its runs: the Auto alone, then each
stand-in at each timing (`"partners"`; the request format is in `SimRunTest`'s javadoc). The
"Simulate Auto" workflow runs every entry and publishes each one. The curation, seeded 3 Nov 2026
and the mentor's to correct:

| Auto | Alone | Beside |
|---|---|---|
| R-Solo | yes | `partner-l-still`; `partner-park-1` |
| R-Partner-Preload | yes | `partner-l-launch-park`, its four preloads away about 2, 5 and 9 s after its CELL rises (`launchS` 0.6, 1.6, 3.0) |
| R-Partner-Tip | yes | `partner-l-tip-park` at 35, 50 and 65 in/s (slow with a 4 s pick, fast with a 2 s one) |
| L-Partner | yes | `partner-r-launch-park` with its last preload away at about 2, 5, 8 and 12 s (`startDelayS` 0 / 1.5 / 4 / 8 with `launchS` 0.3 / 0.8 / 1.0 / 1.0): our 7 s CellUp wait fires for the first two and times out for the last two |
| R-Solo-Staging | yes | `partner-l-stage-park` (see the note below) |

The thinking behind the curation, what a wait is really waiting for, the one number to ask a
partner for, and which partner states get simulated, is in `doc/partner-timing.md`.

A partner entry's **timing** is four knobs, each optional: `startDelayS` (seconds after PLAY
before its first card), `speed` (its drive profile, as a spec writes it after `@`), `launchS`
(seconds per launch volley: a four-piece launch is the 1 s spin-up and then three of these) and
`pickS` (seconds it stands at a FLOWER with its intake on). A left partner fires only once its CELL
is up, so for R-Partner-Preload the timings count from our first TIP, not from PLAY. Partner runs
use `partnerSeeds` (five) so the whole job stays under about five minutes; the Auto alone keeps its
ten.

**The Visualizer's Save & Simulate must keep `partners` and `partnerSeeds`** when it rewrites the
request (it writes the rest from its dialog): the curation lives in the request, nowhere else.

**R-Solo-Staging needs a Visualizer pass.** A wall-touching left partner's row of POLLEN lies at
y 129 to 138 (16.2 in frame, 1.4 in POLLEN), and `partner-l-stage-park` leaves it at x 44.5 for
the intake of a robot at `PARTNER_STAGED_POLLEN` (36, 124, facing 0) to reach. The simulator keeps
the row where the partner left it until our own robot arrives at `LEFT_LAUNCH_OFFSET` (41, 121.5),
whose footprint covers the whole intake zone of `PARTNER_STAGED_POLLEN` and sweeps the row away
before we come for it. Either spot has to move (toward y 133 for the collect, or the launch away
from the row) before this run means anything; the result shows the sweep (3 Nov 2026).

## Team spots (`spots.json`)

`spots.json` here is the team's list of named spots: poses every Auto can use, so nobody types or
places them again. They work like PathPlanner's linked waypoints. **Every Auto on master uses only
these spots**, so a reviewer reads steps and triggers, never coordinates:

| Spot | What it is |
|---|---|
| `RIGHT_START`, `LEFT_START` | The measured starts (`localization/StartPositions`). |
| `RIGHT_LAUNCH_CATCH`, `LEFT_LAUNCH_CATCH` | In front of that CELL, just outside where its spill lands, clear of a partner on the start. |
| `RIGHT_LAUNCH_OFFSET`, `LEFT_LAUNCH_OFFSET` | Off to the side of that CELL and angled at it, so a partner in front of the CELL is not disturbed. |
| `WALL_FLOWER_STAGING`, `SCORING_FLOWER_STAGING` | Where a Pick flower step starts (on the FLOWER's line, 18–24 in out). |
| `GARDEN_STAGING` | Where a Garden gather step starts; the gather pose itself is the robot code's (`GardenFieldPoints`). |
| `RIGHT_PARK`, `LEFT_PARK` | 14.5 in off the alliance wall, facing 0 (the intake toward the field), in the LOADING ZONE. |
| `PARTNER_STAGED_POLLEN` | Where a partner on `LEFT_START` sets its four preloads down for us: a drawing until a partner says where they stage (`partners/partner-l-stage-park` is the simulator's guess; see "Partner stand-ins"). |

Names are side (or thing) first, then role, then variant, so a side's spots sort together. An Auto
that needs a spot not in this list is drawn on the `autos-experiment` branch until the spot earns
its place here.

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
- **Starts are team spots named `…_START`.** The builder's start buttons are the team's
  `_START` spots (`RIGHT_START` → *Right start*); make one to add a start. A start measured on the
  field is also in `localization/StartPositions` (`TEAM_SPOTS` names its spot), and
  `TeamSpotsTest` fails while the two differ: measure, then change both. Both `RIGHT_START` and
  `LEFT_START` are measured (31 Oct 2026): back flat on the wall, the frame's left side on a seam's
  teeth. To move a start, press *Unlock … start to move it* under the start buttons, then set it in
  the START panel.
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

## Timing: two sets of numbers

The preview and the robot time an Auto from two places. Measured values go in both, with where
they came from.

| Numbers | Live in | Used by |
|---|---|---|
| Each command's typical seconds (`LaunchAll`, `PickFlower`, `GardenGather`…) | `AutoRegistration` in the robot code, copied to `TeamCode/auto-registry.json` by `AutoRegistrationTest` (`UPDATE_AUTO_REGISTRY=1`), and into each `.pp`'s `registry.typicalS` when the Visualizer loads the robot's commands | The endgame guard on the robot (a grab starts only if its typical time and the park fit), and the Visualizer's Instant preview |
| How the robot and the HIVE behave: drive speed, spin-up, a volley, time to full, a TIP's start and swing, the CELL loads | `timing.json` here (`version`, then each fact's `value`, `source` and `note`) | The Visualizer's Typical preview and its **Timing facts** dialog, which reads this file from the branch Team Autos last listed. It never changes the robot or the export |

- **A fact's `source` says how sure it is**: `measured, <robot>, <dates>`, or `estimate`. The
  `note` is one line, shown under the fact in the dialog; the longer story goes below.
- **Where the measured facts came from** (20245, the 29 Oct to 2 Nov 2026 logs on the `logs/*`
  branches; each note in the file is the one-line version):
  - `driveSpeed` 1.0: logged drive time against the export's `kit.path` seconds. 2 Nov R-Quals
    (12.76 V) ran its three drives at 0.98, 1.05 and 1.25, and R-Simple's park at 0.83. 1 Nov
    R-Simple, 19 runs of one 3.1 s path: 0.88 to 1.11, median 1.0. The 1.5 of 25 Oct was the light
    chassis.
  - `spinUpS` 1.1: 83 spin-ups from rest to launch speed (1500 to 1625 ticks/s), median 1.1 s,
    middle half 1.0 to 1.3, all 0.9 to 2.1 (slowest on a sagging battery). The first LaunchAll at
    PLAY waited 0.94 to 1.36 s for it.
  - `volleyS` 4.0: LaunchAll after a drive with the launcher at speed, the settle (0.17 to 0.66 s)
    and `LaunchCards`' set 3.5 s feed; 4.17 s in 2 Nov R-Quals. The last of four balls left 2.3 to
    2.6 s after the gate opened (median 2.4; ten 1 Nov feeds, read from the wheels' dip per ball).
  - `intakeFullS` 1.9 (estimate): the last piece's current spike came a median 1.5 s after the push
    started (middle half 1.2 to 1.9; 33 picks that took pieces), and the one pick that filled the
    lane latched full 0.44 s after its last spike, 1.43 s in.
  - `tipSwingS` 0.7 (estimate): the camera saw two swings, both tipped by hand (1 Nov 23:16, 2 Nov
    16:51), 0.57 and 0.43 s from TRANSITION to settled. It sees a swing late.
- **Change `timing.json` by hand, in a PR**, or in the dialog, which keeps a change on that laptop
  until it is copied in. Unknown facts are dropped when it loads.
- **After changing a typical time in `AutoRegistration`**, update `auto-registry.json` (the test
  above) and load the robot's commands into every `.pp` here, then export again: the Java does not
  change, the `.pp`'s copy does.

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
