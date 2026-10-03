#!/bin/bash
#
# Blocks the first `git push` of a `claude/*` branch until the session renames it.
#
# Why this exists: the desktop app and cloud sessions create their branch as
# `claude/<random-words>` before the session has read the request, so the name
# can never say what the work is. Renaming afterwards meant a mentor clicking
# GitHub's rename button on every PR. Renaming *before the first push* is free -
# nothing on GitHub refers to the branch yet - and the session knows by then
# what it did, so it can pick the name itself.
#
# Exit 2 is the hook protocol's "block this call"; stderr goes back to Claude as
# the reason, so the session renames and pushes again with nobody involved.
#
# Deliberately NOT blocking a `claude/*` branch that is already on GitHub. It may
# carry an open PR, and renaming locally then pushing would create a second
# branch and strand that PR - the delete-and-repush failure CONTRIBUTING.md
# warns about. Those still get renamed with GitHub's button.
#
set -uo pipefail

branch=$(git branch --show-current 2>/dev/null) || exit 0
case "$branch" in
  claude/*) ;;
  *) exit 0 ;;
esac

# Already on GitHub: let it through.
if git show-ref --verify --quiet "refs/remotes/origin/$branch"; then
  exit 0
fi

cat >&2 <<EOF
Push blocked: '$branch' is a random session name and has never been pushed.
Rename it to describe the work, then push again - no need to ask the user.
Run the rename as its own command; this check runs before the whole command,
so 'git branch -m ... && git push' is still blocked:

  git branch -m <prefix>/<issue>-<slug>

prefix is one of feat/ fix/ tune/ chore/ docs/ spike/, issue is the number this
PR closes, slug is 2-5 lowercase hyphenated words (e.g. feat/42-intake-subsystem).
EOF
exit 2
