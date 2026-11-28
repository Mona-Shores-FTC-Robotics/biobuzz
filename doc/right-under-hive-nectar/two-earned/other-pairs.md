# Where to reuse a NECTAR receive

Master registers four coordinated pairs in `TeamCode/autos/pairs.json`. The turret and one-piece feed are reusable. The release direction, free intake slot, camera branch and two park paths determine whether a receive works on a particular pair. In the table, named times are exported match-clock checks after PLAY.

| Pair | First place to test | Condition that can spoil it | Evidence |
|---|---|---|---|
| RightUnderHive | L after four preloads at `LEFT_AUTO_OFFSET`, then after the WALL FLOWER volley at its RIGHT spot | A late TIP 1 sends L across for repair. Near and far park assignments must agree even when RIGHT rises late. | 40/120 qualifying triples in the dual-receive study. |
| LeftUnderHive | L at `RIGHT_AUTO_HIVE` after the GARDEN volley, behind `RightCellUp`. Its earlier `LEFT_AUTO_HIVE` preload spot can also accept a first roll in the model. | L must see LEFT by 10 s and RIGHT by 22 s. R uses `RIGHT_AUTO_CLOSE` and shares the GARDEN exit. Its 18.6 s lane release depends on L's departure. | Physical pilot below. |
| Clockwise | R after its camera-checked second volley at `LEFT_AUTO_HIVE`, while it has a free slot | L and R share `LEFT_AUTO_HIVE`. R leaves at 9.8 or 9.9 s depending on pickup assignment, needs LEFT by 15.5 s and has a 17.6 s SCORING FLOWER check. A catch on L's first stop can cost R its normal four. | A drawing proposal. Test WALL FLOWER and GARDEN assignments, TIPS FIRST, PARK FIRST and the clockwise loop before accepting it. |
| NoCross | On its matching pair, L visits `LEFT_AUTO_HIVE` for the camera-confirmed preload volley, then revisits it with the SCORING FLOWER. An additional first NECTAR at the first stop could release the later load for another CELL. | R normally stays on the right. If TIP 1 fails, R may cross to `LEFT_AUTO_OFFSET` and L may also run its 9 s repair branch. L's later CELL check ends at 23 s. | A drawing proposal. Run WAIT FOR PARTNER and CROSS AND FIX IT with the matching pair and with the supported stand-ins. |

### What each drawing can reuse

1. Author the receive at its own named scoring position. Check the near CELL with the camera, then fire the normal load first. The extra piece needs a free slot within the four-piece capacity. A human roll enters only after its owning TIP and a clear LOADING ZONE.
2. Use the gate-closed tracking approach through the turn and `CatchNectar` during the bounded wait. `NectarIn` means the lane identifies the alliance NECTAR. Give an absent roll a clock exit to the existing route, including its park fallback.
3. Use `LaunchOneAimed` after the CELL check. It requires a referenced pose, a live aligned turret hold and margin on both actual and demanded angles. The first counted shot ends the feed. Loss of hold closes the gate. The hardware backstop is 0.85 to 1.2 s. These checks can be shared across pairs without copying mechanism code.
4. Budget the last normal volley as well as the extra feed against both robots' park times. A retained piece can be fired from an eligible `LaunchAllAimed` park only after the park and a fresh near-CELL check. Keep each pair's original camera recovery if the desired CELL fails to rise.
5. Keep two distinct earned-piece IDs through release, receipt, scoring and spill. Compare identical Fruitport seeds with both deliveries, each withheld or late, failed preloads, partial/full FLOWER loads and either drive slowed. A success needs the last ball out by 30 s, settlement by 38 s, both parks, no robot contact and both robot frames on their half.

### LeftUnderHive pilot

A right-end-only receive used L's real GARDEN volley at `RIGHT_AUTO_HIVE`, a 24.5 s park cutoff and a fresh RIGHT-up check. It delivered a distinct second earned NECTAR on a best-case run. Fruitport seeds 1 through 12 gave 1/12 triples on both the pilot and master. Best was 12/12 on both. All 12 parked and stayed clear. The second earned ball often became available too late to affect the result.

Adding a first receive at L's existing LEFT preload spot gave 2/12 Fruitport triples against 1/12 for master. On held-out seeds 13 through 36 the raw counts were 8/24 and 3/24. The early drawing produced a contact on seed 26 at 20.98 s and four contacts in the first 12 slow-L runs. L's first roll approaches roughly 71° off the intake axis, beyond the previously proposed 35° bench drill. The hardware trial needs that actual angle, roll speed and capture count. The collision and the unmeasured intake behavior preclude a competition rollout from these results.

As a common starting point, a separate no-human strict-eight study ran the existing matched pairs on Fruitport seeds 1 through 20. LeftUnderHive and Clockwise each produced 1/20 qualifying triples and 16/20 at least two. NoCross produced 0/20 and 10/20. All six robots parked in all of their paired runs without contact or robot crossing. Clockwise used the clockwise loop and R's WALL FLOWER assignment. NoCross used the matching L robot and WAIT FOR PARTNER FIX.
