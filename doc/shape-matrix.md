# Every spill guide on every current Auto

Run **6 Oct 2026 12:15 UTC** on `claude/simulator`, on the simulator as it now stands: each TIP takes 0.58–1.12 s
([tip timing](tip-timing.md)) and spilled pieces roll as far as videos show ([rolling](rolling.md)). One runner
for every cell (`AutoStudyTest`, the same as the README's baselines): 20 matches each, seeds 1–20, the same
partners. `python3 tools/auto-routes/shape_matrix.py 20` reruns it all (about 1.5 minutes).

Each is its own simulated robot: the Flat Intake's 14.5 in chassis and 14 in intake (the build team's option 3), plus

- **Rigid V**: two fixed flaps, 4 in tall, from the front corners out to 18 in wide (1.75 in out and forward);
- **Ramp Hook**: simulated as the 8 in hook until the ramp is designed: one arm and a crossbeam on the side toward
  the centre line, swung down as our CELL tips and up when the robot drives off.

Each runs its own copy of each Auto: the same plan, with the robot sliding to the hook spot as TIP 2 starts for
the hook (and holding there 1 s after TIP 2 settles).

| Robot | Qual-PartnerShootsRight | Qual-PartnerStages, angled partner | Qual-PartnerStages, wall partner |
|---|---|---|---|
| **Flat Intake** (the baseline, no guide) | 53.5 pts · TIP 3 2 · PARK 2 · G409 0 | 54.8 · TIP 2 19 · PARK 19 · G409 2 | 49.0 · TIP 2 18 · no PARK · G409 1 |
| **Rigid V** | **66.0** · TIP 3 **12** · PARK 12 · G409 1 | 55.8 · TIP 2 19, ~1.3 s sooner · PARK 19 · G409 2; drives into the HIVE frame in 1 run | **no clean route yet** (see below) |
| **Ramp Hook** (as the 8 in hook) | 57.0 · TIP 3 5 · PARK 4 · G409 **10** | 53.5 · TIP 2 18 · PARK 18 · G409 **16**; crosses the centre line in 1 run | 49.0 · TIP 2 18 · G409 **6** |

The full-width 18 in robot (16.2 in intake, no guide) on its own ShootsRight route, for comparison: 64.0 · TIP 3 12 ·
G409 2.

G409 is the number of runs (of 20) with at least one touch of a falling spilled piece: anything above 0 is a
foul risk in a real match.

**What it says**

- **The Rigid V is the only guide that helps, and only where TIP 3 is within reach.** With the partner that
  shoots, it turns TIP 3 from 2 runs into 12. Its lead grew when pieces started rolling as filmed (it was 71.0
  against 64.8 before): pieces now roll past the 14 in mouth, and the flaps reach them. With a partner that
  can't shoot, TIP 3 is out of reach for every shape; the V brings TIP 2 about 1.3 s sooner but scores the same.
- **The Flat Intake's ShootsRight route needs retuning.** It was tuned when spills stopped short; now its sweep
  misses them, and TIP 3 comes in 2 of 20.
- **The Ramp Hook doesn't pay as the 8 in hook:** it scores less, and falling pieces touch it in half the runs
  or more. Its case is keeping TIP 2's spill on our half, which the points don't count; G409 is the problem to
  solve first, with the ramp's own shape.

**Open**

- **Rigid V with the wall partner.** 18 in across its flaps, it doesn't fit the west lane between the HIVE frame's
  foot bar (x 45) and where that partner parks (x 28): 17 in. Through the tunnel instead it touches the partner at
  10.7 s in every run (47.0, TIP 2 16). It needs its own route, or the partner to park elsewhere.
- The Rigid V on the angled partner's route drove into the HIVE frame in 1 run of 20 (26.7 s).
- Everything here rests on the simulator's guesses; [what the simulator knows](what-the-simulator-knows.md). The
  rigid V's gain is pieces bouncing off its flaps into the intake: the first thing to try with cardboard.

**To watch:** the README links the three baselines' logs (Flat Intake) and the Rigid V's ShootsRight; any log
shows its own robot with the usual layout.

**Next** (mentor, 6 Oct 2026; bounce and roll are now checked against video): the Rigid V's width (flap tips at 18, 20, 22 in)
and angle (30°, 45°, 60°), 4–6 variants; Side Rails both, left only and right only (the centre-line side, to keep
NECTAR off the other half), each lined up outside the 90% Drop Zone and out as the TIP starts; the Ramp Hook once
its ramp is designed. Front Pen and the funnel flaps are dropped (README, "Names").
