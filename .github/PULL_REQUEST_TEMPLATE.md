Closes #

## What this changes

<!-- One or two sentences. What is different after this merges? -->

## Why this way

<!--
The house rule: record what you considered and rejected, not just what you chose.
If you tried something that did not work, say so — that is the part that saves the
next person. See TeamCode/README.md for the style.
-->

## Tested

- [ ] Compiles locally (`./gradlew assembleDebug`)
- [ ] Lint clean (`./gradlew :TeamCode:lintDebug`)
- [ ] Unit tests pass (`./gradlew testDebugUnitTest`)
- [ ] Ran on a robot — which: ( ) 19429 ( ) 20245 ( ) not applicable
- [ ] If any `.gradle` file changed: did a **full TeamCode install**, not Sloth Load

## Touches locked things?

- [ ] Dependency versions (Sloth / Pedro / AutoTune / Ivy / Panels / Dashboard / appcompat)
- [ ] `.github/workflows/` or `dependabot.yml`
- [ ] `TeamCode/src/main/res/xml/robot_*.xml` or `hardware/DeviceNames.java`
- [ ] `FtcRobotController/` (should be empty — that module is stock and refreshed wholesale)

> Any box above checked ⇒ needs a second admin review, and the version-lock reasoning in
> `TeamCode/README.md` must be updated in the same PR.

## For reviewers

<!-- What should they look at hardest? What are you unsure about? -->
