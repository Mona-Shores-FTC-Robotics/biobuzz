# RightUnderHive paired replay

The draft improves slow-R parking. The exact-eight Fruitport results give no measured increase in three-TIP share over master. Use the strict rows for the mentor's target of 100 clean triples per 120.

| Java profile and seeds | Master clean triples | Draft clean triples | Master clean two or more | Draft clean two or more |
|---|---:|---:|---:|---:|
| Fruitport, eight weights, 1 to 120 | 71 | 71 | 114 | 114 |
| Fruitport, eight weights, 201 to 320 | 49 | 49 | 110 | 111 |

Both robots parked and remained on their half without contact in all 240 strict draft runs. The best-mechanism draft produced 20 triples in 20 seeds with the eight-weight gate active. Under the stock tipping model and master's human roll, master scored 91 and the draft 93 in the first 120. Of those two gains, the queue retry contributed one. Comparing the draft with its retry disabled on seeds 201 to 320 gave 86 in both arms on the stock model. A different legal human roll can reverse which of the earlier #788 and #792 routes scores best.

## How to reproduce

1. Use this PR's Java simulator in two independent checkouts. In the baseline checkout restore both generated RightUnderHive Java files and both matching drawings from commit 24589016e1a097d4aa91c7027534306e8c1c5816. Keep the same simulator, Fruitport calibration, robot designs and request in both.
2. Copy TeamCode/sim-requests/r-right-under-hive.json into a fresh output directory as request.json. For the primary 120 use the first partner entry. For the holdout, replace that entry's seeds with integers 201 through 320. For best case, set scenario to best and use seeds 1 through 20. Use requireEightPollenWeights true for each strict comparison. The unmodified request also exercises late and absent rolls, forced misses and slow partners.
3. Set BIOBUZZ_SIM_RUN to that absolute directory. Run the repository Gradle wrapper with the arguments below. Read the paired entry of result.json. Times are seconds from PLAY.

    ./gradlew :TeamCode:testDebugUnitTest --tests '*SimRunTest*' --rerun

4. Count a triple when the third TIP settled by 38, both last shots were out by 30, both partners parked, neither robot crossed and the robots never contacted. The accompanying CSV records the exact-eight outcomes and individual disqualifiers for each seed.

## What to review next

All six one-TIP development matches first settled RIGHT between 17.76 and 18.06 after L crossed to cover one missed R preload. LEFT finished with seven pieces in every case, four due to a missed L shot and two due to a missed R shot. A targeted experiment is to receive the first earned NECTAR on L's return to LEFT and fire it with a bounded feed while preserving the hard park deadline. Re-run the six affected seeds and the independent panel. Treat physical release timing, turret hold and feed timing as robot checks.
