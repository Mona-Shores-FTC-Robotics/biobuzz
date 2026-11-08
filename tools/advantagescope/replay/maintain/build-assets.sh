#!/usr/bin/env bash
# Builds BIOBUZZ-replay-assets.zip: everything AdvantageScope needs to draw a BIOBUZZ match log, for laptops
# that have no repository, no Android Studio and no FIRST CAD.
#
#   tools/advantagescope/replay/maintain/build-assets.sh [out-dir]       (default: TeamCode/build/replay-assets)
#
# Needs bash, curl, unzip, python3 and a JDK 17+ (javac). The zip holds:
#   Robot_BIOBUZZ            the robot, as committed in cad/advantagescope/
#   Field3d_BIOBUZZHiveSim   AdvantageScope's 2026-2027 field with the HIVE cut out   } cut here by HiveAssets.java
#   Robot_BIOBUZZHive        the HIVE frame and its two rockers, as a "robot"          } from the pinned field bundle
# The zip is byte-for-byte reproducible (fixed timestamps, sorted entries), so the sha256 in replay.json can be
# checked by anyone who runs this. The Replay assets workflow runs it and publishes the zip as a release.
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
repo="$(cd "$here/../../../.." && pwd)"
out="${1:-$repo/TeamCode/build/replay-assets}"
pins="$here/../replay.json"
pin() { python3 -c "import json,sys; d=json.load(open(sys.argv[1])); print(d['fieldBundle'][sys.argv[2]])" "$pins" "$1"; }

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
mkdir -p "$out"

# 1. The stock field, from the pinned public bundle (or $FIELD_BUNDLE, a copy already downloaded).
bundle="${FIELD_BUNDLE:-$work/bundle.zip}"
if [ ! -f "$bundle" ]; then
  echo "Downloading $(pin url)"
  curl -sSLf --retry 4 -o "$bundle" "$(pin url)"
fi
echo "$(pin sha256)  $bundle" | sha256sum -c --quiet - || { echo "FAILED: the field bundle's sha256 is not the one in replay.json" >&2; exit 1; }
field3d="$(pin field3d)"
unzip -q -o "$bundle" "$field3d.zip" -d "$work"
mkdir -p "$work/stock"
unzip -q -o "$work/$field3d.zip" -d "$work/stock"

# 2. Cut the HIVE out (the code HiveAssetsTest runs, without Gradle).
# javac compiles only the classes HiveAssets reaches (pure Java: the frame and HIVE geometry, no SDK).
mkdir -p "$work/classes" "$work/built"
javac -nowarn -implicit:class -d "$work/classes" \
  -sourcepath "$repo/TeamCode/src/main/java:$repo/TeamCode/src/test/java:$here/stubs" "$here/BuildReplayAssets.java"
java -cp "$work/classes" org.firstinspires.ftc.teamcode.logging.BuildReplayAssets "$work/stock" "$work/built"

# 3. One reproducible zip.
python3 "$here/make_zip.py" "$out/BIOBUZZ-replay-assets.zip" \
  "$repo/cad/advantagescope/Robot_BIOBUZZ" "$work/built/Field3d_BIOBUZZHiveSim" "$work/built/Robot_BIOBUZZHive"
sha="$(sha256sum "$out/BIOBUZZ-replay-assets.zip" | cut -d' ' -f1)"
echo "$sha" > "$out/BIOBUZZ-replay-assets.zip.sha256"
echo "Wrote $out/BIOBUZZ-replay-assets.zip"
echo "sha256 $sha"
