#!/bin/bash
# both.sh <name> <right Auto> <left Auto> [SEEDS_MOD]: both robots the "rigid V" changed by SEEDS_MOD
# ("field=value;..." RobotDesign fields), seeds 1-60 in three threads; prints one summary line.
# Env: BIOBUZZ_JARS and BIOBUZZ_CLASSES as for build.sh; BIOBUZZ_BATCH_OUT (default /tmp/biobuzz-batch/runs);
# KEEP=1 keeps the .wpilog files (about 7 MB each), otherwise only their events (<seed>.wpilog.ev) stay.
# The simulator's own switches pass through: BIOBUZZ_AUTO_HUMAN_NECTAR=0, BIOBUZZ_AUTO_HUMAN_ENTRY=roll,
# BIOBUZZ_AUTO_HUMAN_BANK=3, BIOBUZZ_AUTO_HUMAN_CUE=1, BIOBUZZ_AUTO_ROBOT_RESTITUTION=0.1.
set -e
HERE=$(cd "$(dirname "$0")" && pwd); ROOT=$(cd "$HERE/../../.." && pwd)
: "${BIOBUZZ_JARS:?set BIOBUZZ_JARS}"
CLS=${BIOBUZZ_CLASSES:-/tmp/biobuzz-batch/classes}
BASE=${BIOBUZZ_BATCH_OUT:-/tmp/biobuzz-batch/runs}
CP=$CLS:$(ls "$BIOBUZZ_JARS"/*.jar | tr '\n' ':')
O=$BASE/$1; rm -rf "$O"; mkdir -p "$O"; cd "$ROOT/TeamCode"
for f in 1 21 41; do
    SEEDS_MOD="$4" java -cp "$CP" org.firstinspires.ftc.teamcode.logging.SeedsBoth "$2" "$3" "rigid V" $((f+19)) "$O" $f > "$O.$f.out" 2>&1 &
done
wait
cat "$O.1.out" "$O.21.out" "$O.41.out" > "$O.out"
for f in "$O"/*.wpilog; do python3 "$HERE/../wpilog.py" "$f" Events > "$f.ev" 2>/dev/null; done
[ -n "$KEEP" ] || find "$O" -name '*.wpilog' -delete
awk -v n="$1" '/^seed/{k++; p+=$4; t[$6]++; if ($0 ~ /COLLIDE/) c++}
    END{printf "%s: %d runs, mean %.1f pts | 5 TIPs %d, 4 %d, 3 %d, <=2 %d | collide %d", n, k, p/k, t[5], t[4], t[3], t[2]+t[1]+t[0], c}' "$O.out"
echo " | frame $(grep -h "AUTO points" "$O"/*.ev | grep -c "DRIVES INTO") wall $(grep -h "AUTO points" "$O"/*.ev | grep -c "INTO A WALL") G409 runs $(grep -l "G409: robot" "$O"/*.ev | wc -l)"
