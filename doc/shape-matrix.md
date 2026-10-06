# Every spill guide on every current Auto

Run **6 Oct 2026 12:55 UTC** on `claude/simulator`, on the simulator as it now stands: each TIP takes 0.58–1.12 s
([tip timing](tip-timing.md)) and spilled pieces roll as far as videos show ([rolling](rolling.md)). One runner
for every cell (`AutoStudyTest`, the same as the README's baselines): 60 matches each, seeds 1–60, the same
partners. `python3 tools/auto-routes/shape_matrix.py 60` reruns it all (about 2.5 minutes).

Each is its own simulated robot: the Flat Intake's 14.5 in chassis and 14 in intake (the build team's option 3), plus

- **Rigid V**: two fixed flaps, 4 in tall, from the front corners out to 18 in wide (1.75 in out and forward);
- **Ramp Hook**: simulated as the 8 in hook until the ramp is designed: one arm and a crossbeam on the side toward
  the centre line, swung down as our CELL tips and up when the robot drives off.

Each runs its own copy of each Auto. The routes were retuned for pieces rolling as filmed (6 Oct 2026): the Flat
Intake's ShootsRight takes TIP 3 from pieces that sit still (the wall FLOWER and the GARDEN); the **Rigid V's keeps
the old route** that sweeps the spills, because its flaps catch them (64.1 on it against 59.8 on the Flat Intake's
route); the Ramp Hook follows the Flat Intake's route, sliding to its hook spot as TIP 2 starts and holding 0.5 s
longer. Both Stages routes wait 1.3 s from TIP 2's start and fire TIP 2 from y 119.

| Robot | Qual-PartnerShootsRight | Qual-PartnerStages, angled partner | Qual-PartnerStages, wall partner |
|---|---|---|---|
| **Flat Intake** (the baseline, no guide) | 55.0 pts · TIP 3 7 · PARK 28 · G409 0 | 51.6 · TIP 2 53 · PARK 55 · G409 0 | 47.3 · TIP 2 49 · no PARK · G409 0 |
| **Rigid V** | **64.1** · TIP 3 **33** · PARK 33 · G409 3 | 51.9 · TIP 2 54 · PARK 55 · G409 2; into the HIVE frame in 5 runs | **no clean route yet** (see below) |
| **Ramp Hook** (as the 8 in hook) | 60.3 · TIP 3 15 · PARK 59 · G409 **30** | 50.9 · TIP 2 52 · PARK 51 · G409 **41**; crosses the centre line in 5 | 47.3 · TIP 2 49 · G409 **19** |

All counts are runs of 60. The full-width 18 in robot (16.2 in intake, no guide) on its own ShootsRight route, for
comparison: 62.8 · TIP 3 35 · G409 11. TIP 1 with a partner that can't shoot is our 4 preloads alone: it fails in
5 of 60 with the angled partner (two misses), from the launcher's guessed spread.

G409 is the number of runs (of 60) with at least one touch of a falling spilled piece: anything above 0 is a
foul risk in a real match.

**What it says**

- **The Rigid V is the only guide that clearly helps, and only where TIP 3 is within reach.** With the partner
  that shoots, TIP 3 in 33 of 60 against the Flat Intake's 7, about level with the full-width robot but with far
  fewer G409 touches (3 against 11). With pieces rolling as filmed, a spill is worth chasing only with something
  that widens the mouth: driven through by the plain 14 in intake, it is batted aside.
- **With a partner that can't shoot**, TIP 3 is out of reach for every shape; the V brings TIP 2 about 1 s sooner
  but scores the same.
- **The Ramp Hook (as the 8 in hook) gets TIP 3 more often than plain (15 of 60) but falling pieces touch it in
  half the runs or more.** G409 is the problem to solve first, with the ramp's own shape.

**Open**

- **Rigid V with the wall partner.** 18 in across its flaps, it doesn't fit the west lane between the HIVE frame's
  foot bar (x 45) and where that partner parks (x 28): 17 in. Through the tunnel instead it touches the partner at
  10.7 s in every run (42.0, TIP 2 in 38 of 60). It needs its own route, or the partner to park elsewhere.
- The Rigid V on the angled partner's route drives into the HIVE frame in 5 runs of 60 (26.7 s), and touches
  TIP 3's spill with a flap in 3 runs of 60 on ShootsRight (about 27.5 s, waiting for TIP 3).
- Everything here rests on the simulator's guesses; [what the simulator knows](what-the-simulator-knows.md). The
  rigid V's gain is pieces bouncing off its flaps into the intake: the first thing to try with cardboard.

**To watch:** the README links the three baselines' logs (Flat Intake) and the Rigid V's ShootsRight; any log
shows its own robot with the usual layout.

**Next** (mentor, 6 Oct 2026; bounce and roll are now checked against video): the Rigid V's width (flap tips at 18, 20, 22 in)
and angle (30°, 45°, 60°), 4–6 variants; Side Rails both, left only and right only (the centre-line side, to keep
NECTAR off the other half), each lined up outside the 90% Drop Zone and out as the TIP starts; the Ramp Hook once
its ramp is designed. Front Pen and the funnel flaps are dropped (README, "Names").
