# Two earned NECTAR in RightUnderHive

The L receive at both ends scored **40 of 120** qualifying three-TIP matches at Fruitport rates. Master scored 17 and Claude's first-NECTAR drawing scored 25 on the same inputs. The target is 100. All four parked both robots in every run and kept their frames clear of each other and the centre line. Six matches in each of the first three arms finished with one TIP. A separate late-TIP-1 repair reduced those six to two while keeping the same 40 three-TIP matches.

| Matched arm | Fruitport, 3 TIPs | Fruitport, at least 2 | Best, 3 TIPs | Both parks, Fruitport | Robot contact or crossing |
|---|---:|---:|---:|---:|---:|
| Master at `db8befba` | 17/120 | 114/120 | 16/16 | 120/120 | 0/120 |
| Claude's L receive at LEFT, `0d01ff52` | 25/120 | 114/120 | 16/16 | 120/120 | 0/120 |
| L receives at LEFT and RIGHT, complementary parks | **40/120** | **114/120** | **16/16** | **120/120** | **0/120** |
| Same dual receive with the late-TIP-1 R repair | **40/120** | **118/120** | **16/16** | **120/120** | **0/120** |

The baseline drawings and Fruitport calibration at `db8befba` are byte-identical to those in master `89bf05bc`. The merged indicator and away-from-park export support add no scoring or field-physics action to these baseline routes. The original three arms share identical dual-roll inputs. The fourth arm was also checked against the third with the same conditional human target on both. The Java simulator ran seeds 1 through 120 on RED with the TIPS FIRST default and the Fruitport shot and FLOWER calibration from #785. At 39.2 inches the measured shot rate is 94%. Full picks are 9/10 pooled in the updated calibration. The SCORING FLOWER samples include an empty load. The study requires eight POLLEN-weights before the normal rocker motion can start. Both robot parks are judged at 30 s. The last ball must leave by 30 s and the third TIP must settle by 38 s, all relative to our PLAY. A robot crossing disqualifies the match. A loose game piece crossing does not. The simulator flagged early contact with a spilled piece under G409 in five of the dual-route matches, tracked separately from robot-to-robot contact.

**The two deliveries.** The human rolls the first NECTAR from (8, 98.3) at 16 in/s, 1 s after TIP 1 settles. L tracks LEFT through its 215Â° receive turn. The human rolls the second from (5.5, 95) at 24 in/s, 0.5 s after TIP 2 settles. L receives at its RIGHT scoring spot (32, 16.9), facing 108Â° after firing its WALL FLOWER load. The simulated aim spread is 2Â° standard deviation. These are drill settings. Each release waits for a clear LOADING ZONE and each ball has its own identity. The robots use their cameras, intake and clock. The human sends no signal. L takes the near park after its RIGHT shot. R takes the far park if it commits to that lane, and checks at 23.2 s whether it can still fire its volley and get there. The alternate parking paths cover a late CELL sighting as well.

**What the weight bought.** The first earned NECTAR was present when TIP 2 began in 72/120 matches. The second was present when TIP 3 began in 27/120. Both later CELLs had eight POLLEN plus their own extra NECTAR in eight matches. On Fruitport seed 9, L fired the two distinct NECTAR at 10.42 and 21.96 s. The last ball left R at 24.94 s, TIP 3 settled at 27.00 s, L parked at 24.40 s and R at 28.70 s. Neither robot held more than four pieces.

**Late TIP 1 repair.** When TIP 1 settles after 12.0 s, the human can aim the first earned roll at R's LEFT scoring position instead. The robot receives no human signal. On this branch the human releases from (8, 114.5) at 24 in/s, 1.75 s after the TIP, toward R at (56.3, 122.4, 206.5°). R gives up on the receive before it would compromise the park. In the matched 120-seed comparison this separate draft scored **40/120 three and 118/120 two or more**, with 120 double parks and zero robot contact or crossings. The correctly tagged NECTAR repaired seeds 36, 79 and 86. Seed 85 tipped with other POLLEN. On seeds 6 and 106 the intake rejected the aimed roll. Both the repair and its matched control also parked twice without contact or crossing in all 90 adverse runs each, and made three TIPs in all 16 best runs. Neither draft satisfies the two-TIP floor in every match.

[Late-TIP-1 repair request](../../../TeamCode/sim-requests/r-right-under-hive-dualrescue.json) · [Matched repair seeds](late-repair.csv)

**Why the floor still fails.** On seeds 6, 36, 79, 85, 86 and 106, only three of R's four preloads reached RIGHT. With the three initial NECTAR that gives 7.95 weights. L crossed to repair TIP 1, which settled at 17.26 to 17.56 s. The eight later POLLEN attempts at LEFT put seven in. The first earned NECTAR missed that load and the second was never earned. Separately, 28 of the dual draft's 80 three-TIP failures lost R's entire SCORING FLOWER load before the launcher in the simulator. Fruitport measured the volley output, so sensing an empty load at the pick still needs a robot check. In a grounded-spill retry, none of the 20 dry-pick seeds recovered a three-TIP finish. Turning toward those pieces also caused contact in 19 of the 20.

| Stress on the dual route | Three TIPs | At least two | Both parked |
|---|---:|---:|---:|
| Fruitport, first NECTAR withheld / five seconds late | 2/12 each | 11/12 each | 12/12 each |
| Fruitport, second NECTAR withheld / five seconds late | 3/12 each | 11/12 each | 12/12 each |
| Fruitport, both withheld | 1/12 | 11/12 | 12/12 |
| Best, one forced R POLLEN miss on TIP 2 / TIP 3 | 11/12 / 8/12 | 12/12 each | 12/12 each |
| Fruitport, R / L limited to 40 in/s | 0/16 / 5/16 | 15/16 each | 16/16 each |

Giving master both earned NECTAR directly in the raised CELL, with perfect transport, produced 52/120 three-TIP finishes. That diagnostic uses master's fixed strategy. Other strategies can change the result. On seed 32, both NECTAR were available promptly and both robots parked, but only six of the eight POLLEN attempts for RIGHT landed. Six plus a NECTAR weighs 7.65.

### Reproducing the comparison

The pair's request is [r-right-under-hive.json](../../../TeamCode/sim-requests/r-right-under-hive.json). Copy it to a fresh local folder as `request.json`. Set `BIOBUZZ_SIM_RUN` to that folder and run `./gradlew :TeamCode:testDebugUnitTest --tests '*SimRunTest*' --rerun-tasks`. The partner entry named `both earned rolls` contains the 120 nominal seeds. For the best sample set `"scenario": "best"` and use partner seeds 1 through 16. For a single forced miss in an otherwise best run use that setting and the corresponding named miss entry. The Fruitport stress cases keep the calibration and omit the best override. To compare master or Claude in a disposable checkout, restore only the two RightUnderHive drawings and their exported Java classes from the indicated revision. Keep the request, engine and calibration identical.

[Per-seed results](seeds.csv) Â· [All matched conditions](summary.csv) Â· [R receiving the second NECTAR](r-receiver.md) Â· [Other paired Autos](other-pairs.md)

On additional seeds 201–260 the fixed dual receive scored 24/60 three and 55/60 two or more. Both robots parked in all 60 without contact or a robot crossing.
