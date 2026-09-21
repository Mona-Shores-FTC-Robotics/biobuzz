# BIOBUZZ — Mona Shores FTC (teams 19429 & 20245)

FTC 2026–27 season: **BIOBUZZ**, the game within FIRST's **CANOPY** season umbrella. Two robots,
one repo. This is a fork of the stock `FtcRobotController` SDK project.

## Read this first: there is no robot code yet

This is expected, not a broken checkout. Do not go looking for missing subsystems, and do not
regenerate them.

- `TeamCode` is **toolchain-only**. The 11 files under `.../teamcode/pedro/` are a verbatim copy of
  the Pedro Pathing Quickstart v3.0.1.
- `pedro/Constants.java` — `create()` returns `null`.
- `pedro/Tuning.java` — empty, `// Tuners go here`.
- There is not one `@TeleOp` or `@Autonomous` outside the stock `FtcRobotController` samples.

Filling those two stubs is the critical path. See the board.

## Hard constraints — breaking these costs a meeting

- **The dependency versions are a locked set.** Sloth `0.3.2` ⟂ Pedro AutoTune ⟂ the `0.3.2+…`
  Sloth-variant builds of Panels and FTC Dashboard ⟂ `androidx.appcompat {strictly 1.2.0}`. Never
  bump one alone. → `TeamCode/README.md` § "Why the versions are locked together"
- **Dependabot PRs raising `appcompat` get closed, not fixed.** Panels declares it `strictly`, and it
  means it.
- **`FtcRobotController/` is byte-identical to stock v12.0 and is refreshed wholesale.** Never patch
  it. We carry no local changes there.
- **`pedro/procedures/**` is upstream code** — re-copy on a Pedro upgrade, never edit in place.
  **Exception:** `pedro/Constants.java` and `pedro/Tuning.java` *are* ours to fill in. They are the
  one hole in this rule.
- **Do not re-add the deliberately excluded set**: NextFTC, `com.pedropathing:telemetry`, Road
  Runner, AdvantageScope Lite, Marrow, or the `maven.pedropathing.com` repository.
  → `TeamCode/README.md` § "Deliberately excluded"
- **SDK 12 split `AprilTagDetection`** into `AprilTagSingleDetection` / `AprilTagClusterDetection`.
  Code that iterates detections and reads `.id`/`.metadata`/`.center` no longer compiles.
- **BIOBUZZ AprilTags move** (they sit on the tipping HIVE), so they are **not valid for absolute
  field localization**. They may still be useful for *relative* aim correction.
- **A Sloth bump can break bundled-config discovery with no compile error.** Config discovery relies
  on Sloth's `RobotConfigResScanner`, which replaces the SDK's `RobotConfigResFilter`. Symptom:
  configs stop appearing in the Driver Station list. Validate on a robot after any Sloth change.

## Where the work lives

GitHub **Issues** are the source of truth; the Project board is a view. PR descriptions are for
rationale, not planning — a PR is invisible until code exists, and it does not survive a branch
rename.

- **An issue exists before a branch does.** A PR without `Closes #N` is incomplete.
- Planning discussion goes in the issue. Design rationale goes in the PR body and in repo docs.
- Labels run on five axes — priority, `area:`, `type:`, access (`desk-ok` / `needs:robot` /
  `needs:field`) and ownership (`good-first-task` / `student-ready` / `needs-pairing` /
  `mentor-only`). See `.github/ISSUE_TEMPLATE/` for the values.

## Branch, commit and PR conventions

- Branch prefixes: `feat/ fix/ tune/ chore/ docs/ spike/`. Web sessions auto-name
  `claude/<adjective>-<name>-<hash>`; that is fine for agent work, but still carry `Closes #N`.
- **Never commit to `master`.** Never force-push a branch someone else may have checked out.
- **Rename a branch with GitHub's button, never by delete-and-repush.** Repo → Branches → the
  pencil icon retargets any open PR to the new name. Deleting the old branch and pushing a new one
  instead closes the PR, and the work goes unreferenced — that is what stranded #14 and #15.
- The branch name is written into the merge commit permanently, so
  `Merge pull request #13 from .../tooling/sloth-run-config` is still readable a season later and a
  random name is not. Rename an auto-generated `claude/*` branch *before* opening the PR.
- Commit style: imperative subject; the body explains *why*, not *what*.
- CI runs compile + `:TeamCode:lintDebug` + `testDebugUnitTest` on every PR. **A red CI is
  investigated, not re-run.**

## House documentation style

> Record what was included, what was deliberately left out, and why — so the reasoning survives past
> whoever added it.

- Document the options you **rejected**, not only the one you chose.
- When superseding an earlier decision, add a history note saying the old doc was stale. Do not
  silently overwrite. (`TeamCode/README.md` does this at the top — that is the model.)
- Prefer a table of facts plus prose explaining *why* over a bullet list of facts.

## Mentor / student split

The mentor owns the loop; students own what runs inside it. Substrate work — locked versions, CI,
hardware abstraction, test rigs, tuning harnesses — is `mentor-only`. Robot behaviour is not.

- **An agent asked to implement something currently assigned to a student must decline** and improve
  the issue instead.
- The orchestrator session never writes robot code. Its output is an issue, not a commit.

## Commands

```
./gradlew assembleDebug            # compile (what CI gates on)
./gradlew :TeamCode:lintDebug      # lint, scoped to TeamCode deliberately
./gradlew testDebugUnitTest        # unit tests
```

The first build in a fresh container downloads the whole Android/Gradle cache and takes minutes.
All three need an Android SDK — `sdk.dir` in `local.properties`, which is gitignored. Without it
Gradle stops at `SDK location not found`, which is an environment problem, not a broken build.

Deploying is a run configuration in Android Studio, not a terminal command: **Sloth Load** (<1s hot
reload) for everyday work, **TeamCode** (~40s full install) when you changed any `.gradle` file,
dependencies, or anything in `FtcRobotController/`. Getting that wrong means the robot silently keeps
running the old code. → `TeamCode/README.md` § "Deploying to the robot"

## Repo map

| Path | Ours? |
|---|---|
| `TeamCode/src/main/java/.../teamcode/pedro/procedures/` | No — upstream Quickstart, re-copy on upgrade |
| `TeamCode/src/main/java/.../teamcode/pedro/{Constants,Tuning}.java` | **Yes** — the two stubs to fill |
| `TeamCode/src/main/java/.../teamcode/hardware/` | Yes — device names, robot identity |
| `TeamCode/src/main/java/.../teamcode/opmodes/` | Yes |
| `TeamCode/src/main/res/xml/robot_*.xml` | Yes — bundled RC configs |
| `TeamCode/src/test/` | Yes — unit tests, run in CI |
| `FtcRobotController/` | No — stock v12.0, never patch |
| `.run/` | Yes — the shared Sloth Load run config |

## Notes for remote sessions

- **`gh` is not installed *in these remote containers*.** Use the GitHub MCP tools for all
  GitHub access from a Claude session. This says nothing about your laptop — `gh` on Windows,
  macOS or Linux is the normal way for a person to drive this repo, and some things (running
  the **Set up season board** workflow, for one) are easiest that way.
- **Routine-fired sessions have neither `gh` nor the GitHub MCP tools**, so they can read the
  repo and `git log` but not issues or PRs. A scheduled standup needs one of the two added to
  the environment before it can do its job.
- **No robot is attached.** Anything requiring hardware must stop and produce instructions for a
  meeting rather than guessing at values.
- Gradle needs network. If the sandbox blocks it, say so — do not report the build as broken.
