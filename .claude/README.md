# `.claude/` — agent configuration

## `settings.json`

Two jobs: stop remote sessions stalling on permission prompts for calls they make every single time,
and make the never-edit paths structurally impossible to edit rather than merely documented.

### Why the deny list stops where it does

It covers `pedro/procedures/**` but **not** `pedro/Constants.java` or `pedro/Tuning.java` — nor
`pedro/RobotConstants.java` and `pedro/robots/**`, which hold the per-robot tuned values and have
no upstream counterpart at all.

That asymmetry is load-bearing. `procedures/` is a verbatim copy of the Pedro Pathing Quickstart and
gets re-copied wholesale on upgrade, so a local edit there is silently destroyed later.
`Constants.java` and `Tuning.java` are upstream *stubs* that exist precisely so teams fill them in —
they are ours, and they are the critical path. Denying the whole `pedro/` package would block the
most important work in the repo.

### Why the write side is not allow-listed

`mcp__github__issue_write`, `push_files`, `create_pull_request`, `merge_pull_request`,
`Bash(git commit:*)` and `Bash(git push:*)` are deliberately absent.

Those are the state-changing calls, and the permission prompt on them is the *point* — it is the last
look before an agent files fourteen issues or pushes to a shared branch. The friction costs a few
seconds maybe twice a week, which is cheap next to the alternative.

### `gh` is not installed

Remote Claude Code containers for this repo have `git`, `java` and `gradle` but **no `gh` CLI**
(verified — the only `gh` on the filesystem is an X11 keyboard layout). A `gh` on your own laptop
is a different thing and works fine; this is only about what a session can reach. Any
allow-list entry for `gh ...` would be dead weight. All GitHub access goes through the GitHub MCP
tools, which is why the read side of those is allow-listed above.

## `hooks/`

| Hook | What it does |
|---|---|
| `session-start.sh` | Installs `gh` in remote containers, which ship without it. |
| `name-branch-before-push.sh` | Blocks `git push` from a `claude/*` branch that isn't on GitHub yet, and tells the session to rename it `<prefix>/<issue>-<slug>` first. |

Why the branch hook exists: sessions get `claude/<random-words>` branches before they have read the
request, so the name can't describe the work, and renaming every PR by hand was a chore nobody
remembered. Before the first push, renaming is free. After it, the branch may carry an open PR, so
the hook steps aside and GitHub's rename button stays the way (see CONTRIBUTING.md).

The hook runs before the whole Bash command, so `git branch -m … && git push` is still blocked;
the rename has to be its own call. Pushes through the GitHub MCP tools (`push_files`,
`create_branch`) are not checked. They take the branch name as an argument, so the name is
chosen there.

## `skills/`

| Skill | Use it for |
|---|---|
| `season-standup` | Survey repo + issues + PRs, produce the meeting brief. The orchestrator's routine. |
| `spike-to-issue` | Turn a proven mentor prototype into student-ready issues, keeping the knowledge and discarding the code. |
| `first-pr-coach` | Walk a student from "never used git" to a green PR — **without writing the code for them**. |
