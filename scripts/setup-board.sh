#!/usr/bin/env bash
#
# One-time setup of the GitHub labels and milestones the season board runs on.
#
# Run this once, from a machine where the `gh` CLI is installed and authenticated
# (it is NOT available in Claude Code remote containers, which is why this is a
# script you run rather than something already applied):
#
#     ./scripts/setup-board.sh
#
# Idempotent: re-running updates colours and descriptions rather than failing.
#
set -euo pipefail

REPO="${REPO:-Mona-Shores-FTC-Robotics/biobuzz}"

command -v gh >/dev/null || { echo "gh CLI not found. Install it, or run this from your laptop." >&2; exit 1; }

label() {
  local name="$1" color="$2" desc="$3"
  if gh label list --repo "$REPO" --limit 200 --json name --jq '.[].name' | grep -qxF "$name"; then
    gh label edit "$name" --repo "$REPO" --color "$color" --description "$desc" >/dev/null
    echo "  updated  $name"
  else
    gh label create "$name" --repo "$REPO" --color "$color" --description "$desc" >/dev/null
    echo "  created  $name"
  fi
}

echo "Labels — priority (exactly one per issue)"
label "P0-blocking"  "b60205" "Something else cannot start until this lands. Cap: 3 open."
label "P1-now"       "d93f0b" "This milestone."
label "P2-next"      "fbca04" "Next milestone."
label "P3-later"     "c5def5" "Real, but not scheduled."

echo "Labels — area (one or more)"
for a in drivetrain autonomous teleop vision mechanism hardware-config build-ci docs tooling; do
  label "area:$a" "1d76db" "Subsystem or part of the project this touches."
done

echo "Labels — type (exactly one)"
label "type:feature"  "0e8a16" "New robot behaviour or capability."
label "type:bug"      "d73a4a" "Something does not work."
label "type:tuning"   "5319e7" "A value to be measured on a robot and committed."
label "type:chore"    "cfd3d7" "Maintenance, config, or cleanup."
label "type:decision" "e99695" "A choice to be made before work proceeds."
label "type:spike"    "bfd4f2" "Time-boxed investigation. Never merged - converted to issues."

echo "Labels — access (exactly one): what is needed to do this"
label "desk-ok"     "c2e0c6" "Laptop only. Can be done at home."
label "needs:robot" "f9d0c4" "Requires a physical robot. Only two exist - scarce."
label "needs:field" "f7c6c7" "Requires the field and game elements too. Scarcer still."

echo "Labels — ownership (exactly one): who may take this"
label "good-first-task" "7057ff" "Safe for someone who has never opened a PR. One file, obvious answer, CI proves it."
label "student-ready"   "0075ca" "A student can own this end to end; a mentor reviews."
label "needs-pairing"   "006b75" "Student drives, mentor sits with them."
label "mentor-only"     "b60205" "Locked versions, CI, or ahead of the team's knowledge. A growing count here is a warning sign."

echo "Labels — status (as needed)"
label "blocked"         "000000" "Waiting on something else. Say what in the issue."
label "needs-decision"  "e99695" "Blocked on a human choice."
label "awaiting-review" "fbca04" "PR open, waiting on a reviewer. 48h SLA for student PRs."
label "on-robot-now"    "ff8c00" "Someone is physically on this robot right now. Do not double-book."
label "meeting-brief"   "ededed" "Orchestrator output - the meeting log."
label "robot:19429"     "d4c5f9" "Only where the two robots genuinely diverge. Default is both, unlabelled."
label "robot:20245"     "d4c5f9" "Only where the two robots genuinely diverge. Default is both, unlabelled."

# ---------------------------------------------------------------------------
# Milestones. Capability-anchored: the description says what the robot must be
# able to DO, not just when it is due.
#
# DATES ARE PLACEHOLDERS. Replace them with the real Michigan FTC league
# schedule when it is published, and verify the capabilities against the
# BIOBUZZ Competition Manual.
# ---------------------------------------------------------------------------

milestone() {
  local title="$1" due="$2" desc="$3"
  local num
  num=$(gh api "repos/$REPO/milestones?state=all&per_page=100" --jq ".[] | select(.title==\"$title\") | .number" || true)
  if [ -n "$num" ]; then
    gh api -X PATCH "repos/$REPO/milestones/$num" -f due_on="$due" -f description="$desc" >/dev/null
    echo "  updated  $title"
  else
    gh api -X POST "repos/$REPO/milestones" -f title="$title" -f due_on="$due" -f description="$desc" >/dev/null
    echo "  created  $title"
  fi
}

echo "Milestones (dates are placeholders - replace with the real league schedule)"
milestone "M0 - Foundation"           "2026-10-05T00:00:00Z" "Hardware config landed; ValidateHardware passes on both robots; CI green with real tests; agent scaffolding in place."
milestone "M1 - It drives"            "2026-10-19T00:00:00Z" "Constants.create() returns a real Follower; drivetrain tuned; a driver can drive both robots under Ivy teleop."
milestone "M2 - It scores"            "2026-11-16T00:00:00Z" "Repeatable POLLEN launch into a CELL; intake rejects wrong-colour NECTAR; park auto; drivers practising."
milestone "M3 - League Meet 1"        "2026-12-07T00:00:00Z" "Pose-driven aiming (distance to velocity); one scoring auto per robot; teleop cycle time logged."
milestone "M4 - League Meet 2"        "2027-01-18T00:00:00Z" "Re-aim after a HIVE TIP; per-alliance-position autos; failure modes documented."
milestone "M5 - League Championship"  "2027-02-15T00:00:00Z" "Both robots competition-reliable; endgame FLOWER routine; no unexplained resets."
milestone "M6 - States"               "2027-04-12T00:00:00Z" "Freeze: tuning, repair and driver practice only. No new mechanisms."
milestone "Backlog"                   "2027-06-30T00:00:00Z" "Not scheduled. An issue with no milestone is invisible to the standup, so park it here instead."

echo
echo "Done. Next: seed the backlog, then set branch protection on master to"
echo "require review from Code Owners so .github/CODEOWNERS has teeth."
