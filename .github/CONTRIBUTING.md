# Contributing — Mona Shores FTC (19429 & 20245)

> **History note.** This file used to be the stock upstream FTC guide to contributing to the
> *FIRST* SDK itself — the one that opens "STOP! ... do not push this button." That was never aimed
> at us. This replaces it with how *this team* works.

If you have never contributed to a repo before, start at Rung 0 and work down. You cannot break
anything: CI compiles, lints and tests every pull request, and `master` is protected.

## The ladder

Each rung has a visible artifact, so you can see your own progress.

| Rung | What you do | You're done when |
|---|---|---|
| **0 — Exists** | GitHub account, added as a collaborator, repo cloned, Android Studio syncs. Do the once-per-laptop setup in `TeamCode/README.md` — the **Module** field defaults to `<no module>` and deploys fail confusingly if you skip it. | You've commented on the Meeting Log saying "cloned, synced" |
| **1 — Runs** | Run the **Validate Hardware** OpMode on a robot and report what it printed. No git, no editing. | An issue is closed with the output pasted in |
| **2 — Changes something and sees it** | Edit one telemetry string, hit **Sloth Load**, and watch your own words appear on the Driver Station in under a second. Then `git checkout -- .` | A mentor watched it happen. Nothing is committed |
| **3 — First PR** | Take a `good-first-task`. It's `desk-ok`, so you can do this at home at 9pm. Branch → edit → commit → push → PR → watch three checks go green → get a review → merge. | A merged PR with your name on it |
| **4 — First code PR** | Something in Java, where CI genuinely catches mistakes. | A merged PR touching a `.java` file — **with at least one requested change**, because taking a review is part of the rung |
| **5 — Owns an issue** | Take a `student-ready` issue end to end. Ask questions *in the issue*. A mentor reviews but doesn't touch your branch. | An issue assigned to you, closed by your own PR |
| **6 — Reviews** | Review another student's PR before a mentor does. Run it, read it, ask one question. | A submitted review |

Rung 2 exists for one reason: the gap between "I typed something" and "the robot said it" is about
one second on this project. That is worth feeling early.

## Proposing something

**You do not have to wait to be given work. Anyone can propose something the robot should do.**

Open an issue with the **"Idea — something the robot should do"** template. You do not need to know
how to build it, how long it takes, or whether it's even possible. If you drive the robot, you
already know things nobody else on the team knows.

### What happens to your idea

When we get to them, we read new proposals together and decide. One of four things happens, and we
always say which and why:

| Outcome | What it means |
|---|---|
| **Accepted** | It becomes real work — it gets a priority, an area and a milestone, and goes on the board |
| **Folded in** | Someone already proposed something close. Yours gets linked to theirs so both names are on it |
| **Parked** | Good idea, wrong month. It moves to the Backlog milestone and stays open |
| **Answered** | There's a reason it won't work, and we write that reason down |

**Parked is not a no.** It is the most common outcome for a good idea, and it means we come back to
it. Nothing gets closed without an explanation, ever.

### Help decide

If you want a hand in deciding *what* the team works on, say so and you'll get one. That is the
point of all this, not a reward for doing enough of the other stuff first.

## Finding something to work on

Filter the issue list by label.

- **`good-first-task`** — never opened a PR? Start here. One file, an obviously correct answer.
- **`student-ready`** — you can own this from start to finish.
- **`needs-pairing`** — grab a mentor first; you drive.
- **`mentor-only`** — locked dependency versions, CI, or something ahead of where the team is.

And check what it needs:

- **`desk-ok`** — laptop only. Do it at home.
- **`needs:robot`** — you need a physical robot. There are two, so these are scarce.
- **`needs:field`** — you need the field and game elements too. Scarcer still.

**Claim an issue by assigning yourself** before you start, so two people don't do the same work.

## How we work

- **An issue exists before a branch does.** Every PR says `Closes #N`.
- Branch names: `feat/ fix/ tune/ chore/ docs/ spike/` + a short description.
- **Never commit directly to `master`.**
- **If you rename a branch, its PR closes.** Open a new one immediately — this has already stranded
  finished work on this repo twice.
- Commit messages: imperative subject ("Add flywheel velocity telemetry"), and the body explains
  *why*, not *what*. The diff already says what.
- A red CI check is investigated, not re-run.

## Writing things down

The rule this team documents by:

> Record what was included, what was deliberately left out, and why — so the reasoning survives past
> whoever added it.

Write down the options you **rejected**, not just the one you chose. When you change an earlier
decision, add a note saying the old one was stale rather than quietly overwriting it.
`TeamCode/README.md` is the worked example.

## Getting unstuck

Ask in the issue — not in a DM. An answer in the issue helps the next person too.

If no mentor is free, a Claude session with the `first-pr-coach` skill will walk you through a first
PR step by step without doing it for you.
