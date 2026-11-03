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
  is not an OpMode. Its name ends with the time the `.pp` was saved, Michigan time
  (`R-Simple 10-31|12:22AM`), from the generated class's `SAVED`: after a Sloth Load, the Driver
  Station shows which save is on the robot.

## Partner stand-ins (`partner-*.pp`)

What a teammate might do, drawn as Autos so the simulator can run ours beside them and the
Visualizer can draw the pair: `partner-r-still` and `partner-l-still` (sits on the start),
`partner-park-1` and `-2` (starts by the loading zone, waits, parks: two of them, where partners
were seen to start and park), `partner-r-launch-park` and `partner-l-launch-park` (preloads, then
park: the common partner, and the one that tips the HIVE for us). They have generated classes but
no OpMode, so they never show on the Driver Station, and their starts and parks are their own
(`PARTNER_START`, `PARTNER_PARK`), not team spots. To model a teammate, open one, drag its start
and park to where that team starts and parks, set the wait to when they park, and save. A
stand-in's wait is on `CameraBlind`, which never fires in the simulator, so its time limit is the
whole story.

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
| `PARTNER_STAGED_POLLEN` | Where a partner on `LEFT_START` sets its four preloads down for us: a drawing until a partner says where they stage. |

Names are side (or thing) first, then role, then variant, so a side's spots sort together. An Auto
that needs a spot not in this list is drawn on the `autos-experiment` branch until the spot earns
its place here. The partner stand-ins above are exempt: their spots are a teammate's, not ours.

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
