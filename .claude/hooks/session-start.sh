#!/bin/bash
#
# Installs the gh CLI so sessions can read issues and pull requests.
#
# Why this exists: sessions fired by a scheduled Routine get neither the GitHub
# MCP tools (their tool list is Bash/Read/Write/Edit/Glob/Grep and nothing else)
# nor gh. That leaves a scheduled standup able to read the repo and git log but
# not the board - half its inputs, and the half that matters. This closes that
# gap for every session, scheduled or not.
#
# Deliberately NOT doing a Gradle warm-up here. The first Android build pulls
# the whole SDK and takes minutes, and most sessions never build - they read
# issues, edit docs, or plan. Paying that on every session start to help a
# minority of them is the wrong trade.
#
set -euo pipefail

# Local machines have their own gh and their own package manager. Only act in
# the remote containers, which are the ones that ship without it.
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

if command -v gh >/dev/null 2>&1; then
  exit 0
fi

# gh is in the container's configured apt sources (2.45.0 at time of writing),
# which is the only route that works: github.com release downloads return 403
# through the session proxy, so the usual "curl the tarball" install fails.
# Takes about six seconds.
if ! apt-get install -y -qq gh </dev/null >/dev/null 2>&1; then
  apt-get update -qq >/dev/null 2>&1 || true
  if ! apt-get install -y -qq gh </dev/null >/dev/null 2>&1; then
    # Never fail the session over this. A session without gh is degraded, not
    # broken - it still has git, and interactive sessions have the MCP tools.
    echo "session-start: could not install gh; continuing without it" >&2
    exit 0
  fi
fi

exit 0
