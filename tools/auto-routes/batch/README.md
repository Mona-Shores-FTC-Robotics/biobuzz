# Batch runs

The 60-seed numbers in `doc/tip1-catch.md` and the Sister five studies came from these. They run the simulator
straight from `javac`-compiled classes: seconds to build, no Gradle or Android SDK.

1. **Jars.** Put these in one folder and point `BIOBUZZ_JARS` at it:
   - `junit-4.13.2.jar` and `hamcrest-core-1.3.jar` (Gradle ships both, e.g. `/opt/gradle-*/lib`);
   - from the Gradle cache, the `classes.jar` of each library's `.aar`, renamed `pedro-core-3.0.1.jar`,
     `ivy-core-1.1.1.jar` and `ivy-pedro-1.1.1.jar`.
2. **Build:** `./build.sh`, plus any Autos exported somewhere else (e.g. `autogen.py`'s scratch folders), as extra
   arguments. The classes go to `BIOBUZZ_CLASSES` (default `/tmp/biobuzz-batch/classes`).
3. **Run:** `./both.sh sister5p Sister5pRightAuto Sister5pLeftAuto` runs both robots on the rigid V, seeds 1-60, and
   prints a line like

   ```
   <name>: 60 runs, mean <points> pts | 5 TIPs <n>, 4 <n>, 3 <n>, <=2 <n> | collide <n> | frame <n> wall <n> G409 runs <n>
   ```

   - The fourth argument changes the design for both robots: `RobotDesign` fields, e.g.
     `"flapForwardIn=3.4;flapOutIn=1.38;flapsAutoFold=false"`.
   - The simulator's switches pass through: `BIOBUZZ_AUTO_ROBOT_RESTITUTION`, `BIOBUZZ_AUTO_HUMAN_NECTAR=0`,
     `BIOBUZZ_AUTO_HUMAN_ENTRY=roll`, `BIOBUZZ_AUTO_HUMAN_BANK`, `BIOBUZZ_AUTO_HUMAN_CUE`.
   - Each run's timeline is in `<out>/<name>/<seed>.wpilog.ev`. `KEEP=1` keeps the `.wpilog` files too, for
     AdvantageScope or `../wpilog.py`; they're about 7 MB each.

`SeedsBoth` (both robots the same design) and `SeedsMod` (only robot 1 changed) are the Java mains behind it, in
`TeamCode/src/test/java/.../logging/`. They are not tests. The published rows (`TeamCode/README.md`) still come from
`AutoStudyTest`; this is for quick comparisons.
