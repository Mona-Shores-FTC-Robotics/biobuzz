# RightUnderHive paired replay

On the stock Fruitport profile, the draft scored three clean TIPs in 93 of 120 matches (77.5%), up from 91 (75.8%) for master. The native heavy-field setting reduced that to 42 versus 39 in the same seeds, and 43 versus 41 in an independent 120. Both parked in every one of these runs. One heavy-field development run had robot contact in the draft. The two-TIP floor and three-TIP guarantee remain open.

All times are seconds after our PLAY in the Java simulator. A clean result requires both last balls out by 30, the counted TIPs settled by 38, both robots parked, no robot contact and neither robot crossing the centre line. The two final columns count matches with those violations independently.

| Profile | Seeds | Clean triples, master | Clean triples, draft | Clean 2+, master / draft | Both parks, master / draft | Robot contact, master / draft | Robot crossing, master / draft |
|---|---:|---:|---:|---:|---:|---:|---:|
| Fruitport, stock field mix | 1 to 120 | 91/120 (75.8%) | 93/120 (77.5%) | 119 / 120 | 120 / 120 | 1 / 0 | 0 / 0 |
| Best mechanisms, stock field mix | 1 to 20 | 20/20 (100.0%) | 20/20 (100.0%) | 20 / 20 | 20 / 20 | 0 / 0 | 0 / 0 |
| Fruitport, heavy field | 1 to 120 | 39/120 (32.5%) | 42/120 (35.0%) | 116 / 117 | 120 / 120 | 2 / 1 | 0 / 0 |
| Fruitport, heavy field holdout | 201 to 320 | 41/120 (34.2%) | 43/120 (35.8%) | 116 / 118 | 120 / 120 | 2 / 0 | 0 / 0 |
| Best mechanisms, heavy field | 1 to 20 | 7/20 (35.0%) | 7/20 (35.0%) | 20 / 20 | 20 / 20 | 0 / 0 | 0 / 0 |

The same two real-robot designs, routes for each arm, human roll directions and speeds, camera logic, calibration and available time apply across matching seeds. The best scenario gives full FLOWER and GARDEN loads, successful shots and counted volleys without feed jams. The field, piece placement, camera observations, TIP dwell and route decisions still run. The heavier field is a sensitivity case. Fruitport provides the 94% shot figure at 39.2 in but its logs do not locate its HIVE within the fitted band. The current Fruitport calibration fills a reached FLOWER pick and models a full four-shot post-pick volley in roughly nine out of ten, following the mentor's November 28 assumption for the repaired feed. [Per-seed replay for the primary comparisons](right-under-hive-paired-seeds.csv)

## Misses, late NECTAR and parks

Each scenario uses the stock Fruitport field mix, seeds 1 through 10. Both routes saw the same failure and human roll. The two figures in each column are master / draft.

| Scenario | Clean triples | Clean 2+ | Both parks | Robot contacts | Robot crossings |
|---|---:|---:|---:|---:|---:|
| N1 late, four seconds after TIP 1 | 7 / 6 | 10 / 10 | 10 / 10 | 0 / 0 | 0 / 0 |
| N1 absent | 4 / 4 | 10 / 10 | 10 / 10 | 0 / 0 | 0 / 0 |
| N2 slow and late | 5 / 5 | 10 / 10 | 10 / 10 | 0 / 0 | 0 / 0 |
| R preloads miss | 0 / 0 | 5 / 5 | 10 / 10 | 0 / 0 | 0 / 0 |
| L preloads miss | 0 / 0 | 8 / 8 | 10 / 10 | 0 / 0 | 0 / 0 |
| R GARDEN volley misses | 0 / 0 | 8 / 8 | 10 / 10 | 0 / 0 | 0 / 0 |
| L WALL FLOWER volley misses | 0 / 0 | 9 / 9 | 10 / 10 | 0 / 0 | 0 / 0 |
| R SCORING FLOWER volley misses | 0 / 0 | 10 / 10 | 10 / 10 | 0 / 0 | 0 / 0 |
| R slow | 0 / 0 | 1 / 10 | 1 / 10 | 1 / 0 | 0 / 0 |
| L slow | 7 / 7 | 10 / 10 | 10 / 10 | 0 / 0 | 0 / 0 |
| L dead | 0 / 0 | 0 / 0 | 0 / 0 | 10 / 10 | 0 / 0 |
| N2 absent | 5 / 5 | 10 / 10 | 10 / 10 | 0 / 0 | 0 / 0 |
| Both rolls absent | 2 / 2 | 10 / 10 | 10 / 10 | 0 / 0 | 0 / 0 |

## How the simulator selects its field

FIRST requires the tossed seventh POLLEN to hold and the tossed eighth to tip. With three NECTAR and one POLLEN already in the CELL, the tossed second POLLEN must hold and the tossed third must tip. A tip with the final POLLEN gently placed is preferred in both rows. The simulator uses its existing rocker torque model with piece positions and the 0.091/0.055 NECTAR/POLLEN weight ratio (about 1.65). Its torque fit places the calibration loads against the back. A match's incoming pieces can settle elsewhere. [FIRST Event Field Setup Guide, section 12.3](https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide)

The general simulator default is the midpoint (0.5) of the fitted torque interval. Named fixed fields are `guide` (0.5), `belding1` (0.05) and `belding2` (0.95). A request's `hiveField` wins, followed by the named field or numeric value in `BIOBUZZ_AUTO_HIVE_FIELD`. If both are absent, the Belding and Fruitport event calibrations select an event-specific point per seed. Even seeds use 0.5. Odd seeds 1, 7, 13 and every sixth seed thereafter use -0.3. Other odd seeds use -0.45. The event refit switches are `BIOBUZZ_BELDING_F2`, `BIOBUZZ_BELDING_F1_HOLDS` and `BIOBUZZ_BELDING_F1`, respectively. This is why the named heavy preset 0.95 and the stock event's even-seed value 0.5 give different results. Fruitport inherits the event's rocker fit, zero impact impulse and filmed dwell, while supplying shot and feed assumptions.

Each match restores its prior field value on exit, including after an exception, and each request restores the enclosing field override. The WPILOG records `SimHiveTipPointInBand` per match. A request with an explicit field also reports its name and numeric point in `result.json`.

## Run the comparison

1. Start both checkouts on this PR's simulator. For master, restore `TeamCode/autos/r-right-under-hive.pp`, `TeamCode/autos/l-right-under-hive.pp` and their generated `RRightUnderHiveAuto.java` and `LRightUnderHiveAuto.java` from `50046252c6156d39a9b121d709fd12bc266a6d90`. Keep the identical simulator and human policy in both checkouts.
2. Copy [the normal Fruitport request](../TeamCode/sim-requests/r-right-under-hive.json) to a fresh output folder as `request.json`. Its first partner entry requests seeds 1 through 120. Its other entries exercise missed shots, late or absent rolls, slow robots and partner failure. To repeat only the first panel, keep the first partner entry. Leave `BIOBUZZ_AUTO_HIVE_FIELD` and event refit environment variables unset.
3. For the fixed-field panel, copy [the Belding 2 sensitivity request](sim-requests/r-right-under-hive-belding2.json) to the output folder as `request.json`. Its `partnerSeeds` specify 1 through 120. Replace them with integers 201 through 320 for the independent holdout. For a best panel, set `scenario` to `best` and the partner seeds to 1 through 20. An individual partner entry's `seeds` takes precedence over `partnerSeeds`.
4. Set `BIOBUZZ_SIM_RUN` to the absolute output folder. Run the Gradle command below. Read the paired entry in `result.json` and compare the two routes by seed.

```sh
./gradlew :TeamCode:testDebugUnitTest --tests '*SimRunTest*' --rerun
```

The gate hold during the turn, the counted one-ball feed of at least 0.85 s and the human release timing still need robot measurements. [Robot checks](robot-todo.md)
