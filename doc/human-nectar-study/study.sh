#!/bin/bash
# usage: study.sh LABEL SPEC [ENV=VAL ...]   -> scratchpad/study/LABEL.log
S=/tmp/claude-0/-home-user-biobuzz/59b06a18-b061-5f2b-bc74-373e5c660fc0/scratchpad
mkdir -p $S/study; label=$1; spec=$2; shift 2
cd /home/user/biobuzz
for i in 1 2 3; do
env BIOBUZZ_AUTO_CALIBRATION=belding BIOBUZZ_AUTO_STUDY="$spec@50" BIOBUZZ_AUTO_DESIGNS="real robot, turret locked forward" BIOBUZZ_AUTO_RUNS=${RUNS:-120} "$@" \
  ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*' -i --max-workers=2 > $S/study/$label.log 2>&1 && break
grep -q "status code 429" $S/study/$label.log || break; sleep 40
done
echo "== $label: $(grep -c . $S/study/$label.log) lines; $(grep -m1 -E 'BUILD (SUCCESSFUL|FAILED)' $S/study/$label.log)"
