# Every spill guide on every current Auto

Run **6 Oct 2026 02:56 UTC** on `claude/simulator`, after the HIVE calibration fix (slow tiles used to calibrate a
different HIVE). One runner for every cell (`AutoStudyTest`, the same as the README's baselines): 20 matches each,
seeds 1–20, the same partners, **normal tiles / slow tiles** (pieces roll to a stop 3× sooner).
`python3 tools/auto-routes/shape_matrix.py 20` reruns it all.

Each is its own simulated robot: the Flat Intake's 14.5 in chassis and 14 in intake (the build team's option 3), plus

- **Rigid V**: two fixed flaps, 4 in tall, from the front corners out to 18 in wide (1.75 in out and forward);
- **Large / small right hook**: one arm (9.5 / 8 in) and a crossbeam on the side toward the centre line, swung
  down as our CELL tips and up when the robot drives off.

Each runs its own copy of each Auto: the same plan, with the robot sliding to the hook spot as TIP 2 starts for
the hooks (and holding there 1 s after TIP 2 settles).

| Robot | Qual-PartnerShootsRight | Qual-PartnerStages, angled partner | Qual-PartnerStages, wall partner |
|---|---|---|---|
| **Flat Intake** (the baseline, no guide) | 64.8 / 57.3 pts · TIP 3 11 / 5 · PARK 11 / 5 · G409 0 / 0 | 56.0 / 54.8 · TIP 2 20 / 19 · PARK 20 / 19 · G409 1 / 1 | 51.0 / 50.0 · TIP 2 20 / 19 · no PARK · G409 0 / 0 |
| **Rigid V** | **71.0 / 69.8** · TIP 3 **16 / 15** · PARK 16 / 15 · G409 0 / 0 | 56.0 / 56.0 · TIP 2 20 / 20, ~1.5 s sooner · PARK 20 / 20 · G409 1 / 0 | **no clean route yet** (see below) |
| Large right hook (dropped) | 64.3 / 60.5 · TIP 3 11 / 8 · PARK 9 / 6 · G409 **7 / 19** | 55.0 / 54.5 · TIP 2 19 / 19 · PARK 20 / 18 · G409 **14 / 12** | 51.0 / 50.0 · TIP 2 20 / 19 · G409 2 / 0 |
| Small right hook (the Ramp Hook for now) | 63.3 / 59.8 · TIP 3 10 / 7 · PARK 9 / 7 · G409 **13 / 19** | 56.0 / 54.8 · TIP 2 20 / 19 · PARK 20 / 19 · G409 **15 / 12** | 51.0 / 50.0 · TIP 2 20 / 19 · G409 4 / 0 |

G409 is the number of runs (of 20) with at least one touch of a falling spilled piece: anything above 0 is a
foul risk in a real match.

**What it says**

- **The Rigid V is the only guide that helps, and only where TIP 3 is within reach.** With the partner that
  shoots, it turns TIP 3 from 11 / 5 runs into 16 / 15, and keeps PARK and zero G409. With a partner that can't
  shoot, TIP 3 is out of reach for every shape (TIP 2 comes at 19–22 s); the V brings TIP 2 about 1.5 s sooner
  and holds more pieces for TELEOP (2.8 against 1.5), but scores the same.
- **The hooks don't pay on the Flat Intake.** (Since folded into the Ramp Hook, simulated as the small one.) They score the same or less, and falling pieces touch them in most runs:
  12–19 of 20 on slow tiles. They keep TIP 2's spill on our half, which the points above don't count; that's
  their case, and G409 is the problem to solve first.
- **Slow tiles hurt the plain robot most** (ShootsRight 64.8 → 57.3), because pieces stop before they reach the
  intake. The rigid V barely drops (71.0 → 69.8): its flaps reach the pieces that stop short.

**Open**

- **Rigid V with the wall partner.** 18 in across its flaps, it doesn't fit the west lane between the HIVE frame's
  foot bar (x 45) and where that partner parks (x 28): 17 in. Through the tunnel instead it touches the partner at
  11.5 s (47.0 / 50.0, TIP 2 16 / 19). It needs its own route, or the partner to park elsewhere.
- The large and small hooks crossed the centre line once each on slow tiles with the angled partner (26.7 s).
- Everything here rests on the simulator's guesses; [what the simulator knows](what-the-simulator-knows.md). The
  rigid V's gain is pieces bouncing off its flaps into the intake: the first thing to try with cardboard.

**To watch:** the published Autos' logs (README) are the plain robot. The shapes' own logs from the earlier shape
study are in `sim-review/shape-matches-advantagescope.zip` (with their layout, which shows each shape).

**Next, once bounce and roll are fixed** (mentor, 6 Oct 2026): the Rigid V's width (flap tips at 18, 20, 22 in)
and angle (30°, 45°, 60°), 4–6 variants; Side Rails both, left only and right only (the centre-line side, to keep
NECTAR off the other half), each lined up outside the 90% Drop Zone and out as the TIP starts; the Ramp Hook once
its ramp is designed. Front Pen and the funnel flaps are dropped (README, "Names").
