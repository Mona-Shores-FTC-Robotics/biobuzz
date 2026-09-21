---
name: season-standup
description: Survey the biobuzz repo, issues, PRs and board, then produce the meeting brief for the next Monday/Thursday/Saturday meeting — priorities, PR triage, and one assigned task per student. Use for "standup", "meeting brief", "what should we work on", "plan the meeting", "season status".
---

# Season standup

Produce the meeting brief. **Never write robot code.** If the survey implies code must be written,
the output is an issue, not a commit.

## 1. Fix the clock

Determine today's date and which meeting is next:

| Meeting | Length | Brief shape |
|---|---|---|
| Mon 6–8pm | 2h | One block, one task each |
| Thu 6–8pm | 2h | One block, one task each |
| Sat 9am–3pm | 6h | **Three blocks with a midday checkpoint** |

Saturday is a different brief, not a longer one.

## 2. Read, in this order

Order matters — later reads are interpreted in light of earlier ones.

1. `CLAUDE.md`, then `TeamCode/README.md` — constraints before facts.
2. The pinned **Meeting Log** issue, last 2 comments — what was promised last time. This is the
   memory a cold session otherwise lacks.
3. **Open PRs**: number, author, `mergeable_state`, check status, age, linked issue.
4. **Open issues**: current milestone by priority, then anything `blocked` or `needs-decision`.
5. `git log` since the last brief — on `master` **and on branches with no open PR**. Work that
   bypassed the board is exactly how finished work gets stranded here; look for it every time.
6. The collaborator list, to know who exists.

## 3. Run the health checks

Report each as PASS/FAIL **naming the offending items**, not just a count.

| Check | Threshold |
|---|---|
| Open mentor-authored PRs | ≤ 2 |
| Open `student-ready` issues | ≥ 5 |
| Open `good-first-task` issues | ≥ 3 |
| Open `P0-blocking` issues | ≤ 3 |
| Issues in the current milestone with no assignee | 0 |
| Non-admin PRs unreviewed for >48h | 0 |
| `spike/` branches older than 72h, or any open PR from one | 0 |
| Branches with commits and no open PR or linked issue | 0 |
| `type:decision` issues past their decide-by date | 0 |

## 4. Propose priorities

**Top 3 only.** Each justified by *what it unblocks*, referencing the dependency chain. More than
three priorities is no priorities.

## 5. Assign one task per attending student

Match on three things:

- **Ownership label** vs. the student's rung on the ladder (see `.github/CONTRIBUTING.md`).
  **Never assign more than one rung above where they are.**
- **Access label** — there are two robots, so at most ~4 people can be hands-on at once. Everyone
  else needs `desk-ok` work.
- Whether the robot is actually available.

If a student has no suitable task, **say so loudly**. That is a backlog failure, not a scheduling
detail, and it means restocking outranks whatever else was planned.

## 6. Draft the mutations

List proposed label / assignee / milestone changes explicitly. **Do not apply them yet.**

## 7. Output, then stop for approval

Use this format exactly — it gets read on a phone in a parking lot.

```
## Meeting brief — <day, date, hours>

**Since last time:** <3 bullets max>

**Top 3 for today**
1. #NN <title> — unblocks <what>
2. ...

**PR triage**
- #NN <state>, <age> — <ONE verb: merge / close / review / rebase>

**Assignments**
| Person | Task | Robot? | Rung |

**Bench (desk-ok, grab one if you're free):** #NN, #NN, #NN

**Health checks**
- <check>: <value> PASS/FAIL → <offending items>

**Needs a decision from a human:** #NN — overdue by N days
```

On approval: post the brief as a comment on the Meeting Log issue, then apply the mutations.
