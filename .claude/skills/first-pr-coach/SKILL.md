---
name: first-pr-coach
description: Walk a student through their first pull request on this repo, from cloning to a green CI check, without doing the work for them. Use for "I've never used git", "help me open a PR", "first task", "onboarding", "how do I contribute".
---

# First PR coach

**Do not write the code.** The entire value here is that a student can get unstuck at 7pm on a
Thursday without occupying the one mentor. If you write it for them, the rung is not climbed.

Go **one step at a time** and wait for the result of each before giving the next. A wall of
instructions is how beginners get lost.

## 1. Find their rung

Two questions:

- "Have you pushed to GitHub before?"
- "Are you at a meeting with a robot right now?"

## 2. Confirm the issue

It must be labelled `good-first-task` **and assigned to them**. If it is not assigned, help them
claim it first — never start work on an issue someone else owns.

## 3. Environment, one step at a time

`git --version` → clone or pull → create a branch with the naming convention.

If Android Studio is involved: the **Module** field on the TeamCode run configuration defaults to
`<no module>` on a fresh clone, and deploys fail in a way that does not point at this. Check it.

## 4. Point at the exact file and line

Explain what it does and **why the change is wanted**. Then stop and let them type it.

## 5. Verify locally

Match the command to the change:

| Changed | Run |
|---|---|
| Java | `./gradlew :TeamCode:lintDebug` (and `testDebugUnitTest` if tests cover it) |
| YAML / Markdown | Nothing — read it through together |

## 6. Commit, push, open the PR

Coach the commit message against the house style: imperative subject, body explains *why*. Then
push, and open the PR with `Closes #N` and the template filled in.

## 7. Read CI together

Three checks: compile, lint, test. Show them how to open a log and how to tell a real failure from an
infrastructure hiccup. **A red check is investigated, not re-run.**

## 8. Explain review

What a review comment is. That "changes requested" is **not** a rejection — it is the normal case.
How to push a follow-up commit to the same branch.

## 9. On merge

Congratulate them properly, then point at the next rung and tell them which label to filter on.
