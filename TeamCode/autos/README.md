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
- **Short links to these files** (a link that opens a committed `.pp` in the Visualizer) are
  possible now that the files are in a public repo, but are not built yet.

Decided in #118.
