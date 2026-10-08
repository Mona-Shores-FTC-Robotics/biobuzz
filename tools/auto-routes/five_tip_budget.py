"""five_tip_budget.py: the 5-TIP time budget for two of our robots, as a Monte Carlo (9 Oct 2026). Not a simulation:
step times from the simulator's logs and a few guesses (marked), with the HIVE's random dwell.

The plan it times (doc/unified-design.md, "Two of our robots"): each robot keeps to its own end; every spill gives 4;
L takes 3 human NECTAR for TIP 4; R doesn't park. G407 holds a robot to 4 pieces, so each TIP's second load is set
down beside the firing spot and picked back up (staged), or streamed: fired while intaking, never more than 4 aboard.

    python3 five_tip_budget.py
"""
import random, statistics as st
ROCK, SPILL = 0.74, 0.8          # rocker motion; spill landed and 4 caught this long after the CELL is up (assumed)
FIRE4, STREAM8 = 0.9, 2.3        # fire 4 held; stream preloads + far FLOWER seated (turret)
SETDOWN, PICKUP = 1.0, 1.5       # set 4 down beside the firing spot (0.25 s each); pick the staged 4 back up (guess)
GARDEN_TRIP, WALL_TRIP = 4.6, 5.5   # firing spot -> GARDEN / wall FLOWER, take 4, back (logs)
NECTAR_TRIP = 4.0                # L: firing spot -> LOADING ZONE, 3 NECTAR, back (guess: ~40 in each way)
def run(fixed=False, pickup=None):
    pickup = PICKUP if pickup is None else pickup
    D = lambda: random.uniform(0.25, 3.4)
    # TIP 1: R's preloads, last shot in at 3.5 s.
    T1 = 3.5 + D() + ROCK
    # TIP 2: L streams 8 as the left CELL rises (fixed launcher: preloads, FLOWER, fire: ~3 s slower).
    T2 = T1 + (STREAM8 if not fixed else 5.3) + D() + ROCK
    # R: catch TIP 1's spill, set it down, fetch the GARDEN's 4; ready when back.
    r_ready = T1 + SPILL + SETDOWN + GARDEN_TRIP
    last3 = max(T2, r_ready) + FIRE4 + pickup + FIRE4
    T3 = last3 + D() + ROCK
    # L: catch TIP 2's spill, set it down, fetch 3 human NECTAR (entered after TIPs 1-2 by then), ready.
    l_ready = T2 + SPILL + SETDOWN + NECTAR_TRIP
    last4 = max(T3, l_ready) + FIRE4 + pickup + FIRE4
    T4 = last4 + D() + ROCK
    # R: catch TIP 3's spill, set it down, fetch the wall FLOWER's 4.
    r_ready5 = T3 + SPILL + SETDOWN + WALL_TRIP
    last5 = max(T4, r_ready5) + FIRE4 + pickup + FIRE4
    T5 = last5 + D() + ROCK
    return last5, T5, T4
for fixed in (False, True):
    xs = [run(fixed) for _ in range(20000)]
    last5 = [x[0] for x in xs]; T5 = [x[1] for x in xs]
    ok = sum(1 for l, t, _ in xs if l <= 29.5 and t <= 38) / len(xs)
    print(("fixed launcher" if fixed else "turret"), f"| TIP 4 median {st.median([x[2] for x in xs]):.1f} s | TIP 5's last shot median {st.median(last5):.1f} s, 90th pct {sorted(last5)[int(.9*len(last5))]:.1f} s | TIP 5 in time: {ok:.0%}")
    # Streamed: the second load is fired while the robot intakes it from the staged pile (no separate pick-up).
    xs = [run(fixed, pickup=0.0) for _ in range(20000)]
    ok = sum(1 for l, t, _ in xs if l <= 29.5) / len(xs)
    print(("fixed launcher" if fixed else "turret"), f"| streamed: TIP 5 in time: {ok:.0%}")
