# Working in this repo

Notes for whoever picks this up next — person or Claude. Short on purpose; the
detail lives in `TeamCode/README.md`, which is worth reading before you change
anything in `TeamCode/`.

## Branch names

`category/short-description`. What's in use:

```
ci/compile-check          tooling/sloth-run-config
deps/pedro-3.0.1          shooter/flywheel-speed-test
```

This matters more than it looks: the branch name is written into the merge
commit permanently, so `Merge pull request #13 from .../tooling/sloth-run-config`
is still readable a season later and a random name is not.

**If a Claude Code session starts you on an auto-generated branch** — something
like `claude/ecstatic-mendel-0f68tz` — rename it to fit the convention *before*
opening a pull request. Use GitHub's branch rename button (repo → Branches →
the pencil icon): it retargets any open PR to the new name. Deleting the old
branch and pushing a new one instead will close the PR and you'll have to open
a fresh one.

## master is protected

Everything reaches `master` through a pull request. No direct pushes.

## Verifying a change

The same three commands CI runs, and the bar before pushing:

```bash
./gradlew assembleDebug
./gradlew :TeamCode:lintDebug
./gradlew testDebugUnitTest
```

Two things to know about that third one: **there are no unit tests in this
repo yet**, so it passes without running anything. A green CI badge currently
means "it compiles and lints", not "it works". Anything touching robot
behaviour still has to be tried on a robot before you trust it.

All three need an Android SDK — `sdk.dir` in `local.properties`, which is
gitignored.

## Deploying to the robot

`TeamCode` for a full install, `Sloth Load` for a sub-second hot reload of
TeamCode classes only. Use the full install whenever you've touched a `.gradle`
file, a dependency, or anything in `FtcRobotController/` — otherwise the robot
quietly keeps running the old code. Details in `TeamCode/README.md`.

## House style

This repo writes down *why*, not just what — see the appcompat and Pedro 3
sections of `TeamCode/README.md` for the standard. When you make a judgement
call, leave the reasoning somewhere it'll be found: the commit message, the PR
body, or the README section next to the code.
