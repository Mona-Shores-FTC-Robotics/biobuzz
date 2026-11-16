S=/tmp/claude-0/-home-user-biobuzz/59b06a18-b061-5f2b-bc74-373e5c660fc0/scratchpad
export RUNS=60
for wf in 0 1; do for rb in 19429 20245; do
  $S/study.sh base-$rb-wf$wf RTwoTipAuto BIOBUZZ_BELDING_ROBOT=$rb BIOBUZZ_BELDING_WHOLE_FEED=$wf >/dev/null
  for sp in RSoloHumanPlayerAuto HPW0Auto HPW5Auto HPO5Auto HPO0Auto; do
    $S/study.sh $sp-$rb-wf$wf $sp BIOBUZZ_BELDING_ROBOT=$rb BIOBUZZ_BELDING_WHOLE_FEED=$wf BIOBUZZ_AUTO_HUMAN_ENTRY=aim BIOBUZZ_AUTO_HUMAN_AIM_SPOT="20,88,110" BIOBUZZ_AUTO_HUMAN_AIM_SPEED=7 >/dev/null
  done
done; done
for f in $S/study/*-wf?.log; do echo "$(basename $f .log): $(grep -E 'STUDY [A-Za-z0-9]+Auto@' $f | sed 's/real robot, turret locked forward//' | cut -c7-230)"; done
