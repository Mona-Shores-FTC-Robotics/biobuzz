---
name: spike-to-issue
description: Convert a mentor prototype — a spike branch, a closed PR, a scratch OpMode, or a description of something proven on the robot — into one or more student-ready GitHub issues, keeping the discovered knowledge and discarding the code. Use for "turn this into an issue", "hand this off", "convert my spike", "make this student-ready".
---

# Spike → issue

This is the mechanism that keeps fast prototyping from crowding students out. It has to be **easier
to hand off a spike than to polish and merge it**. Optimise for that.

## 1. Identify the spike

A branch name, PR number, diff, or prose description. **Read the actual diff if one exists** — do not
work from the description alone.

## 2. Extract the knowledge, not the code

Produce four lists:

- **Proven** — measured numbers, APIs that work, geometry that fits, timings.
- **Disproven** — approaches tried and rejected. *This is the part that always gets lost*, and the
  house documentation style demands it.
- **Open questions.**
- **Constraints touched** — locked versions? robot config? the `pedro/` package? CI?

## 3. Decide the split

A spike is usually **2–4 issues, not 1**. The typical shape:

- a `type:decision` issue, if a choice remains open
- a `desk-ok` issue — constants, structure, tests
- a `needs:robot` issue — validate and tune on hardware

**Split along the `desk-ok` / `needs:robot` seam.** That seam is what lets work run in parallel
across more people than there are robots.

## 4. Hold each issue to the student-ready bar

Every issue must have:

1. **The first file to open**, named.
2. **An existing file named as the pattern to copy.**
3. **"Done" stated as something observable** — a CI check, a telemetry value, a robot behaviour.
   Never "it works".
4. The exact verification command or OpMode.
5. If it needs the robot: which robot, and roughly how long on it.

## 5. Label and scope

Priority, area, type, access, ownership, milestone. Add `robot:19429` / `robot:20245` **only** if the
two robots genuinely diverge here.

## 6. Record the spike's fate

Each issue gets an **Origin** section crediting the spike branch, stating what it proved, and saying
plainly that **its code is not to be merged**. Then the spike branch is deleted.

If the knowledge belongs in a doc rather than an issue, propose the doc edit instead.

> **Exception worth checking first:** if the "spike" is actually finished work whose PR was closed by
> a *branch rename*, it is not a spike at all. Reopen it as a PR. Check whether the branch SHA
> matches the closed PR's head before treating anything as throwaway.

## 7. Refuse to implement

If asked to also build it, decline and point at the guardrail. Opening a PR here defeats the purpose.
