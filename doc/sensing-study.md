# How much the Autos need piece sensing (15 Oct 2026)

The mentor, 15 Oct: the intake probably can't be tracked robustly, but "full" can likely be sensed (motor current,
issue #181). How much do the baseline Autos lose with weaker sensing, and can time limits make up for it?

The Autos wait on two piece signals: **intake full** (`IntakeFull`: 4 held; FLOWER, GARDEN and catch waits) and
**launcher empty** (`Empty`: every fire step). The simulator gives both exactly. New design settings
(`RobotDesign.fullSenseDelayS`, `fullSenseMissChance`) make "full" late or missed; `countsPieces=false` removes both.
60 runs each, `tools/auto-routes/batch`, rigid V, measured 0.4 restitution. Points / runs with 3 TIPs (4 for Sister):

| Sensing | L-Quals | R-Quals | Sister | L-Simple + R-Simple |
|---|---|---|---|---|
| exact (today's assumption) | 73.7 / 55 | 72.7 / 54 | 89.0 / 45 | 53.0 |
| full 0.4 s late | 73.3 / 54 | 71.3 / 50 | 88.7 / 43 | 53.0 |
| full missed 1 time in 5 | 73.3 / 54 | 71.0 / 49 | 87.3 / 39 | 53.0 |
| **no full sensor** (empty still works) | 72.7 / 52 | **59.0 / 14** | **73.3 / 2** | - |
| no full sensor, FLOWER waits limited to 1.8 s | - | **72.0 / 52** | not tried yet | - |
| ... limited to 2.2 s | - | 69.0 / 44 | - | - |
| no sensing at all (neither) | 55.1 / 0 | 53.3 / 0 | 53.0 / 0 | 53.0 |

## What it says

- **A slow or flaky full sensor is fine.** 0.4 s late or missing 1 pickup in 5 costs 0-5 runs in 60.
- **No full sensor costs R-Quals and Sister a lot as they stand,** because their FLOWER waits are 3 s and a FLOWER takes
  1.66-1.68 s in the simulator: two FLOWERs and a spill wait run to their limits, about 3 s lost, and TIP 3 (TIP 4
  for Sister) misses its time. L-Quals barely notices.
- **Tuned time limits make up for it:** R-Quals with its FLOWER waits limited to 1.8 s and no full sensor scores 72.0 /
  52, against 72.7 / 54 with the exact sensor; with the sensor, 1.8 s costs nothing (72.7 / 54). But it's sensitive:
  at 2.2 s it's 69.0 / 44. So without a sensor the limit has to sit just above the real pickup time.
- **Launcher empty matters more than intake full.** Without it every fire step runs to its limit, the robot runs out
  of time and the endgame park is cut short (walls, the HIVE frame, the partner): 53-55 points everywhere. It is
  probably the easier one to get (count shots, or the flywheel's speed dipping on each one).
- The simple Autos are unaffected either way: their one FLOWER wait has time to spare.

## So, for the robot

1. **Measure the extractor's FLOWER time** (and its spread) as soon as it runs. If it's as steady as the simulator's,
   time limits alone can stand in for a full sensor; if it varies, the full sensor earns its place.
2. **Make sure the robot knows when the launcher is empty**: shot counting or flywheel dips.
3. **A full sensor from motor current (#181) only needs to be roughly right:** late by half a second or missing
   one in five is fine.
4. Not done yet: Sister with tuned FLOWER limits (expected to recover like R-Quals), and the GARDEN and catch waits.
