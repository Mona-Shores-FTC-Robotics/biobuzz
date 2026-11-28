# LeftUnderHive NECTAR replay study

Try L at its existing LEFT and RIGHT shooting poses. It launches each normal volley of POLLEN and has one opportunity for a counted, turret-aimed shot at the near CELL. At LEFT its receive window ends at 10.6 s from PLAY. At RIGHT the window ends at 23.1 s. A detected own-alliance NECTAR and the active CELL camera check must both permit the extra. R follows its original route. The timing and pickup rates require robot validation.

The paired Java comparison uses the Right and Left roles of LeftUnderHive, the RED alliance and L at 50 in/s. Both routes receive identical human rolls and matched seeds. The nominal rolls originate at (5.5, 95) at speeds of 24 and 26 in/s. The human aims at the intake of the LEFT and RIGHT HIVE launch spots respectively and releases one second after the corresponding TIP, subject to the simulator's zone check. The fast-roll comparison changes only the human's origin to (9, 94.5) and the speeds to 29 and 28 in/s. Its 24-seed screen overlaps the 120-seed development block. Human consistency on either roll still needs a field check.

| Case and seed block | Runs per route | Master clean 3 | Pilot clean 3 | Clean 2+, master / pilot | Both parked, master / pilot | Contact, master / pilot | Robot crossed, master / pilot |
|---|---:|---:|---:|---:|---:|---:|---:|
| best-1-20 | 20 | 20 | 20 | 20 / 20 | 20 / 20 | 0 / 0 | 0 / 0 |
| fruitport-1-120 | 120 | 55 | 68 | 118 / 118 | 120 / 120 | 0 / 0 | 0 / 0 |
| fruitport-201-120 | 120 | 59 | 71 | 119 / 119 | 120 / 120 | 0 / 0 | 0 / 0 |
| late-1-20 | 20 | 9 | 9 | 19 / 19 | 20 / 20 | 0 / 0 | 0 / 0 |
| late2-1-12 | 12 | 5 | 5 | 11 / 11 | 12 / 12 | 0 / 0 | 0 / 0 |
| none-1-20 | 20 | 6 | 6 | 19 / 19 | 20 / 20 | 0 / 0 | 0 / 0 |
| none2-1-12 | 12 | 5 | 5 | 11 / 11 | 12 / 12 | 0 / 0 | 0 / 0 |
| rmiss2-1-12 | 12 | 0 | 0 | 8 / 8 | 12 / 12 | 0 / 0 | 0 / 0 |
| rmiss3-1-12 | 12 | 0 | 0 | 11 / 11 | 12 / 12 | 0 / 0 | 0 / 0 |
| fast-roll-fruitport-1-120 | 120 | 54 | 69 | 118 / 118 | 120 / 120 | 0 / 0 | 0 / 0 |
| fast-roll-fruitport-1-24 | 24 | 8 | 13 | 22 / 22 | 24 / 24 | 0 / 0 | 0 / 0 |
| fast-roll-fruitport-201-120 | 120 | 59 | 76 | 119 / 119 | 120 / 120 | 0 / 0 | 0 / 0 |
| upper-band-1-120 | 120 | 0 | 8 | 115 / 115 | 120 / 120 | 0 / 0 | 0 / 0 |
| fast-roll-upper-band-201-36 | 36 | 0 | 10 | 36 / 36 | 36 / 36 | 0 / 0 | 0 / 0 |

Fruitport uses the measured shot model and the simulator's seed-dependent Belding HIVE fits. The upper-band run fixes the HIVE torque fit at 0.95 of the guide calibration band and keeps Fruitport shot rates. The official checks use eight POLLEN and three POLLEN with three NECTAR. Section 12.3 distinguishes a tossed final piece from a gently placed one. [FTC Event Field Setup Guide, section 12](https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide)

A clean TIP count requires both parks, both robots remaining on their half, zero robot contacts, the last shot from either robot out by 30 s, and the counted CELL settled by 38 s. A loose piece crossing is permitted. The CSV includes separate park, contact, crossing and timing fields for both arms. Seed blocks 1 to 120 and 201 to 320 are separate samples.

## Reproduce the LeftUnderHive study

1. Open an isolated checkout of Mona-Shores-FTC-Robotics/biobuzz at simulation commit `2deecf9ffb81270ea51b88c24f3cbeccaa89f0e7` from #795. The stock routes in this study match master `50046252c6156d39a9b121d709fd12bc266a6d90`. Use a configured FTC/Android build environment.
2. Copy the supplied Visualizer drawing from `candidate` to `TeamCode/autos`, and its companion Java file to `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/auto/generated`. The distinct pilot class lets the simulation run both versions in the same checkout.
3. Select a request in `requests`, copy it to a fresh output directory as `request.json`, and set `BIOBUZZ_SIM_RUN` to the absolute output directory. The nominal request is `fruitport-1-120.json`. For the independent sample select `fruitport-201-120.json`. For the upper-band request set **each** of `BIOBUZZ_BELDING_F1`, `BIOBUZZ_BELDING_F1_HOLDS` and `BIOBUZZ_BELDING_F2` to `0.95` before launching Gradle. For the default mix leave these three unset.
4. Run `./gradlew :TeamCode:testDebugUnitTest --tests '*SimRunTest*' --rerun`. The paired entries in `result.json` contain each route's output. The time fields are seconds from PLAY.

The study ZIP contains the individual match records and the earned-piece attribution CSV. The attribution CSV records how many uniquely identified earned NECTAR and POLLEN left each robot toward the raised CELL and how many were in the CELL at the swing. A blank CELL count means no such swing occurred. The launch count alone cannot establish that a NECTAR reached the CELL. The observer was enabled only for the matched mixed-field LeftUnderHive runs.

A trace from development seed 4 of the faster-roll test illustrates the mechanism. TIP 1 settled at 6.56 s. L detected the first earned NECTAR at 9.60 s and took its bounded shot at 9.62 s. It detected the second at 22.12 s and took its bounded shot at 22.14 s. TIP 2 settled with eight POLLEN and earned NECTAR 1 in LEFT. TIP 3 settled with seven POLLEN and earned NECTAR 2 in RIGHT. Both robots parked and the pair's last ball left at 24.06 s.

An exploratory Clockwise change let R receive N1 at LEFT after its WALL FLOWER volley. The paired score was 9 to 10 clean triples on seeds 1 to 24 and 12 to 12 on seeds 201 to 236. Both robots parked and avoided contact in all four tests. Ordinary NoCross on master had zero clean triples and 83 clean two-TIP matches in seeds 1 to 120. Its third attempt depends on a fast TIP 2 decision.

The ordinary master baselines for Clockwise, LeftUnderHive and NoCross are in `records/stock-pairs.csv`. Those runs use the ordinary human entry and the saved default partner settings. Use the matched control above for estimating the contribution of the LeftUnderHive route change.

## What changed on the same seeds

| Case | Master below three, pilot reached three | Master reached three, pilot below three |
|---|---:|---:|
| fruitport-1-120 | 15 | 2 |
| fruitport-201-120 | 14 | 2 |
| fast-roll-fruitport-1-120 | 18 | 3 |
| fast-roll-fruitport-1-24 | 5 | 0 |
| fast-roll-fruitport-201-120 | 19 | 2 |
| upper-band-1-120 | 8 | 0 |
| fast-roll-upper-band-201-36 | 10 | 0 |

For practice, log the earned release, L's first NECTAR detection, actual count at the throat and the time both frames reach park. The first roll approaches L from the side. A dry or late piece takes the clock branch, so both parks and the two-TIP floor can be checked even when a catch fails.

## Earned NECTAR in the intended CELL at the swing

| Pilot case | Runs traced | N1 in LEFT at TIP 2 | N2 in RIGHT at TIP 3 | Both |
|---|---:|---:|---:|---:|
| fruitport-1-120 | 120 | 47 | 8 | 4 |
| fruitport-201-120 | 120 | 55 | 6 | 4 |
| fast-roll-fruitport-1-120 | 120 | 62 | 11 | 6 |
| fast-roll-fruitport-1-24 | 24 | 11 | 2 | 2 |
| fast-roll-fruitport-201-120 | 120 | 69 | 12 | 9 |
