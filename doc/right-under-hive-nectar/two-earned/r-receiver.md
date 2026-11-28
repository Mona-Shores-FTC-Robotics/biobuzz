# Two earned NECTAR in the RightUnderHive pair

The 210°/12.5 s L draft and R receiving at RIGHT scored **35/120** three-TIP matches at current Fruitport rates. Master scored **25/120** on the same seeds. Eleven seeds gained a third TIP and one lost it. Both pairs parked both robots without robot contact or a robot crossing the centre line on 120/120. Each missed the two-TIP floor on the same six seeds. This draft falls short of the 100/120 target.

## What changed

L fires its four preloads at LEFT, turns to 210°, and receives NECTAR #1. The human rolls NECTAR #2 from (5.5, 95) toward R’s intake near (58.4, 26.2) at 23 in/s, one second after TIP 2 and no earlier than 19.4 s after PLAY. Both robots must be clear of the LOADING ZONE. The 86.8 in roll reaches R about 32° off the intake axis. R uses its ordinary RIGHT pose and four-POLLEN volley. Its intake then waits for its alliance NECTAR until 25.5 s, rechecks RIGHT up, and uses the counted LaunchOneAimed with a 1.2 s feed backstop and 1.6 s card timeout. The normal park path follows. Both robots held at most four pieces. The long roll and intake response are unmeasured.

## Paired Java simulation

The nominal and historical rows use seeds 1 through 120. Stress rows use seeds 1 through 20. Both arms use the same rolls, Fruitport shots, FLOWER assumption and eight-POLLEN-equivalent threshold on both fitted fields. A third TIP counts with the last shot out by 30 s and the CELL settled by 38 s. Rates in the historical row use the logged 19429 FLOWER loads. The variable-roll row uses Gaussian angle SD of 2° on both rolls and speed SD of 1 and 2 in/s respectively.

| Case | Master | Two-NECTAR draft |
|---|---:|---:|
| Current Fruitport | 25/120 | 35/120 |
| Historical Fruitport FLOWER loads | 19/120 | 24/120 |
| Best case | 20/20 | 20/20 |
| Both NECTAR absent | 2/20 | 2/20 |
| Both NECTAR five seconds late | 2/20 | 2/20 |
| Only NECTAR #2 absent | 4/20 | 4/20 |
| Only NECTAR #2 five seconds late | 4/20 | 4/20 |
| Only NECTAR #1 absent | 2/20 | 2/20 |
| Only NECTAR #1 five seconds late | 2/20 | 3/20 |
| One forced R miss on TIP 2 | 2/20 | 3/20 |
| One forced R miss on TIP 3 | 1/20 | 2/20 |
| NECTAR #2 rolled eight degrees wide | 5/20 | 4/20 |
| Both rolls vary in speed and direction | 4/20 | 7/20 |

Of 120 nominal matches, NECTAR #2 was released in 84, caught in 66, fired in 54, scored in 49 and present when TIP 3 began in 22. Both earned pieces were present at the intended TIP in 18 matches. Master had zero such matches. Five runs in each arm had a G409 in-air spill contact. The mentor’s centre-line limit applies to robot bodies.

**Fruitport seed 4** demonstrates the intended rescue. The pair attempted eight POLLEN at each target, with one missed shot on each. L launched NECTAR #1 at 13.36 s. R received NECTAR #2 at 23.67 s and fired it at 25.92 s. Both cells tipped with seven POLLEN and one earned NECTAR. TIP 3 settled at 29.44 s. L finished parked at 26.74 s and R at 28.62 s, without contact, a robot centre-line crossing or G409.

A single NECTAR can cover one missing POLLEN. Two missing POLLEN leave the CELL at 7.65 equivalents. A literal 100% match guarantee does not follow from the measured rates, even with perfect delivery.

## Independent drive caps

The same 23 in/s reception was tested on seeds 1 through 12 with one drive capped at 40 in/s.

| Shots | Robot capped | At least 2 TIPs | 3 TIPs | Both parks | Robot contact | Robot crossed |
|---|---|---:|---:|---:|---:|---:|
| Best | L | 12/12 | 1/12 | 12/12 | 0/12 | 0/12 |
| Best | R | 12/12 | 12/12 | 12/12 | 0/12 | 0/12 |
| Fruitport | L | 11/12 | 0/12 | 12/12 | 0/12 | 0/12 |
| Fruitport | R | 11/12 | 1/12 | 12/12 | 0/12 | 0/12 |
