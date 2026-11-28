# LeftUnderHive paired NECTAR pilot

Both the control and draft run the registered R-LeftUnderHive with L-LeftUnderHive, or its exported draft. R keeps its routes and timing. L keeps both normal volleys. It holds its existing shooting pose for an earned NECTAR after its LEFT preloads and after its RIGHT GARDEN volley. An alliance NECTAR in the lane and a second check that the near CELL is up trigger LaunchOneAimed. The action timeout is 1.6 s and its counted feed has a 1.2 s backstop. L parks on its existing path.

The final deadlines are 10.6 s on LEFT and 23.1 s on RIGHT. From (5.5, 95), the human aims the first roll at the intake of LEFT_AUTO_HIVE (61.4, 120.45, 268.9°) at 24 in/s, a second after TIP 1. The second roll to RIGHT_AUTO_HIVE (59.2, 20.9, 96.1°) uses 26 in/s, one second after TIP 2 and no earlier than 18.5 s after PLAY. Both releases require the entire zone clear. The first receipt approaches approximately 71° off the intake axis. No physical success rate has been measured for either roll.

### Paired Java results

Fruitport and best case use the same field seeds, eight equivalents per TIP and physical rolls on both arms. The missed-roll case banks both. Late releases are five seconds later than nominal. A drive cap of 40 in/s applies only to the named robot. Each programme counts three TIPs only with its last shot by 30 s and settlement by 38 s. The centre-line count is for robot bodies.

| Case | Arm | Seeds | 3 TIPs | ≥2 TIPs | Both parks | Robot contacts | Robot crossings | N1 at TIP 2 | N2 at TIP 3 | Both earned pieces in intended TIP |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| fruitport | Master | 36 | 4 | 29 | 36 | 0 | 0 | 0 | 0 | 0 |
| fruitport | Draft | 36 | 9 | 29 | 36 | 0 | 0 | 16 | 1 | 0 |
| best | Master | 36 | 36 | 36 | 36 | 0 | 0 | 0 | 0 | 0 |
| best | Draft | 36 | 36 | 36 | 36 | 0 | 0 | 18 | 2 | 0 |
| slowR | Master | 36 | 4 | 29 | 36 | 0 | 0 | 0 | 0 | 0 |
| slowR | Draft | 36 | 9 | 29 | 36 | 0 | 0 | 16 | 4 | 3 |
| slowL | Master | 36 | 4 | 29 | 36 | 0 | 0 | 0 | 0 | 0 |
| slowL | Draft | 36 | 7 | 29 | 36 | 0 | 0 | 15 | 0 | 0 |
| none | Master | 12 | 1 | 8 | 12 | 0 | 0 | 0 | 0 | 0 |
| none | Draft | 12 | 1 | 8 | 12 | 0 | 0 | 0 | 0 | 0 |
| late | Master | 12 | 1 | 8 | 12 | 0 | 0 | 0 | 0 | 0 |
| late | Draft | 12 | 1 | 8 | 12 | 0 | 0 | 0 | 0 | 0 |
| miss2 | Master | 12 | 0 | 8 | 12 | 0 | 0 | 0 | 0 | 0 |
| miss2 | Draft | 12 | 2 | 8 | 12 | 0 | 0 | 7 | 0 | 0 |
| miss3 | Master | 12 | 0 | 8 | 12 | 0 | 0 | 0 | 0 | 0 |
| miss3 | Draft | 12 | 0 | 8 | 12 | 0 | 0 | 6 | 0 | 0 |
| none2 | Master | 12 | 1 | 8 | 12 | 0 | 0 | 0 | 0 | 0 |
| none2 | Draft | 12 | 2 | 8 | 12 | 0 | 0 | 6 | 0 | 0 |
| none1 | Master | 12 | 1 | 8 | 12 | 0 | 0 | 0 | 0 | 0 |
| none1 | Draft | 12 | 1 | 8 | 12 | 0 | 0 | 0 | 0 | 0 |

### Earlier timing tests

Waiting until 12.4 s at LEFT and 24.5 s at RIGHT raised Fruitport triples from 3/24 to 8/24 on seeds 13–36, with contact at 20.98 s on seed 26. At 40 in/s on L, the draft made contact on seeds 1, 4, 11 and 12 and dropped from eight two-TIP matches to five on seeds 1–12. Delaying R’s GARDEN-exit departure from 18.6 to 20.2 s cleared those four contacts, but L lost its park on seed 4. Keeping R on master and shortening L’s LEFT deadline to 10.6 s prevented the contact and restored the two-TIP count on those seeds. At 23.9 s for the RIGHT receipt, L fired at 25.04 s and lost the park on slow-L seed 10.

One successful Fruitport run of the initial drawing was seed 17. L captured N1 at 9.98 s and fired at 10.88 s. It took N2 at 22.63 s and fired at 23.80 s. TIP 3 settled at 27.04 s with seven POLLEN and N2. Both robots parked with no robot contact or crossing.

All alternatives were exported by the Visualizer. The existing source intentionally has a park followed by a parked shot. The simulator overlay enables receipt accounting for the R/L roles of this pair.

### Paired floor checks

| Case | New below-two seeds | New missed parks | Contact seeds |
|---|---|---|---|
| fruitport | None | None | None |
| best | None | None | None |
| slowR | None | None | None |
| slowL | None | None | None |
| none | None | None | None |
| late | None | None | None |
| miss2 | None | None | None |
| miss3 | None | None | None |
| none2 | None | None | None |
| none1 | None | None | None |
