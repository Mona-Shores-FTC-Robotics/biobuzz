# Every spill guide on every current Auto

> **Archived, 6 Oct 2026: the hook as a spill catcher.** The robot catches spills with a Rigid V. The hook is being
> redesigned as a FLOWER extractor only. See [One robot](unified-design.md).

Run **6 Oct 2026 13:40 UTC** on `claude/simulator`, on the simulator as it now stands: each TIP takes 0.58–1.12 s
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
| **Flat Intake** (the baseline, no guide) | 54.8 pts · TIP 3 10 · PARK 13 · G409 0 | 51.6 · TIP 2 53 · PARK 55 · G409 0 | 47.3 · TIP 2 49 · no PARK · G409 0 |
| **Rigid V** | **64.1** · TIP 3 **33** · PARK 33 · G409 3 | 51.9 · TIP 2 54 · PARK 55 · G409 2; into the HIVE frame in 5 runs | **no clean route yet** (see below) |
| **Ramp Hook** (as the 8 in hook) | **66.3** · TIP 3 **35** · PARK 51 · G409 **30** | 50.9 · TIP 2 52 · PARK 51 · G409 **41**; crosses the centre line in 5 | 47.3 · TIP 2 49 · G409 **19** |

All counts are runs of 60. The full-width 18 in robot (16.2 in intake, no guide) on its own ShootsRight route, for
comparison: 62.8 · TIP 3 35 · G409 11. TIP 1 with a partner that can't shoot is our 4 preloads alone: it fails in
5 of 60 with the angled partner (two misses), from the launcher's guessed spread.

G409 is the number of runs (of 60) with at least one touch of a falling spilled piece: anything above 0 is a
foul risk in a real match.

## The Rigid V's width and angle

Run **6 Oct 2026 15:44 UTC** on `claude/biobuzz-robot-body-designs-hi386c` (claude/simulator merged in, the baselines above
reproduced exactly), the same runner, 60 runs. Flap tips this far apart, each flap this many degrees from
straight ahead (45° is the Rigid V above). R105: wider than 18 in, the robot must stay within 18 in long, so wide
flaps can't reach far forward: 22 in fits only at 60°, 20 in at 45° or more. Wider than 18 in they can't be rigid
(R102's start cube): they would fold in for the start and swing out, which the simulator doesn't model.

| Rigid V | Qual-PartnerShootsRight | Stages, angled partner | Stages, wall partner (tunnel route) |
|---|---|---|---|
| 18 in, 30° | **67.3** · TIP 3 **41** · PARK 40 · G409 6 | 50.9 · TIP 2 51 · G409 8; **crosses the centre line at 7.6 s in all 60** | 41.0 · TIP 2 35 · G409 6; crosses in all 60 |
| 18 in, 45° (the Rigid V) | 64.1 · TIP 3 33 · PARK 33 · G409 3 | 51.9 · TIP 2 54 · G409 2; into the HIVE frame in 5 | 39.3 · TIP 2 30 · G409 0 |
| 18 in, 60° | 63.2 · TIP 3 31 · PARK 30 · G409 4 | 51.3 · TIP 2 52 · G409 0; into the HIVE frame in 5 | 40.7 · TIP 2 34 · G409 0 |
| 20 in, 45° | 66.8 · TIP 3 40 · PARK 38 · G409 10 | 51.6 · TIP 2 53 · G409 7; crosses in all 60 | 41.7 · TIP 2 37 · G409 6; crosses in all 60 |
| 20 in, 60° | 65.3 · TIP 3 36 · PARK 35 · G409 7 | 51.3 · TIP 2 52 · G409 1; crosses in all 60 | 39.0 · TIP 2 29 · G409 0; crosses in all 60 |
| 22 in, 60° | 67.0 · TIP 3 40 · PARK 40 · G409 13; **crosses the centre line and drives into the HIVE frame in all 60** | 51.3 · TIP 2 52 · G409 4; crosses and into the frame in all 60 | 42.0 · TIP 2 38 · G409 3; same |

Intake misses per run (the study line's, Rigid V cells): about 17 too high and 12–22 while the intake is busy, as
for the Flat Intake; "beside" (pieces off the intake's sides) drops from 1.8 on the Flat Intake to 0.4–2.4.

- **On ShootsRight a longer V helps a little more**: 18 in at 30° (flaps 3 in further forward) 67.3, TIP 3 in 41,
  against 64.1 and 33 at 45°, at the cost of 6 G409 runs against 3. Wider (20, 22 in) does about as well with more
  touches (7–13).
- **Every variant but 18 in at 45° and 60° breaks a rule on the Stages routes**: the flaps reach over the centre
  line at 7.6 s (the turn out of the tunnel), and at 22 in into the HIVE frame. Those routes were drawn for the
  45° V; a longer or wider V needs its own.
- **No V gets TIP 3 with a partner that can't shoot**, as above.

## Where the Ramp Hook waits

Run **6 Oct 2026 17:10 UTC**, the same runner, 60 runs. The Ramp Hook waits for TIP 2's spill with its face this far
from the north wall (37.5 in is its route above; the 90% Drop Zone is 32.0–46.6 in from the wall). Experiments:
`qual-right-o3-ramp-f*` and `qual-stages-angled-ramp-f*`.

| Face from the wall | ShootsRight | 3 TIPs | Both LEAVE + PARK | G409 runs | Kept 4 of TIP 2's spill | Stages, angled partner |
|---|---|---|---|---|---|---|
| 34.5 in | 63.3 | 28 | 44 | 58 | 24 | 51.0 · G409 36 |
| 35.5 in | 64.5 | 31 | 46 | 56 | 25 | 50.9 · G409 37 |
| 36.5 in | 66.2 | 35 | 50 | 44 | 27 | 50.9 · G409 37 |
| **37.5 in** (its route) | 66.3 | 35 | 51 | **30** | 28 | 50.9 · G409 41 |
| 38.5 in | **66.7** | **36** | **52** | 34 | **33** | 50.9 · G409 46 |
| 39.5 in | 64.9 | 31 | 51 | 48 | 24 | 51.0 · G409 51 |

- **No spot gets G409 down.** Every touch is TIP 2's spill still in the air (14.4–15.1 s, 1–7 in up, falling up to
  160 in/s): it lands on the robot's front face (8.7 in ahead of its centre) or on the hook (14 in ahead). Closer
  in, more falls on the robot; further out, more on the hook. Moving the robot can't fix it; the hook's shape or
  when it comes down might.
- 37.5 and 38.5 in are about level; the route stays at 37.5 (fewest touches).

## PARK first on ShootsRight

Run **6 Oct 2026 17:45 UTC**, 60 runs. On the Rigid V's route (`qual-right-o3-sweep`), when TIP 3 hasn't come 0.8 s
after the GARDEN's shots, the robot goes back to the GARDEN for a third load. That card is the route's last, so the
endgame guard never cuts it: the robot missed PARK in 25 of 60 runs, and the third load never made TIP 3.
`tools/auto-routes/park_first.py` PARKs instead (shots already away can still TIP within 8 s of AUTO's end):

| Rigid V, PARK first | Points | 3 TIPs | Both LEAVE + PARK | G409 runs |
|---|---|---|---|---|
| 18 in, 45° | 66.2 (was 64.1) | 33 | **58** (was 33) | 3 |
| 18 in, 30° | **68.8** (was 67.3) | **41** | 57 (was 40) | 6 |
| 20 in, 45° | 68.4 (was 66.8) | 40 | 57 (was 38) | 9 |

The Flat Intake's route (`qual-right-o3`, "first") has its own gap: PARK in 13 of 60.
Full comparison: [the report](../sim-review/body-evaluation.html).

**What it says**

- **The Rigid V is the only guide that clearly helps, and only where TIP 3 is within reach.** With the partner
  that shoots, TIP 3 in 33 of 60 against the Flat Intake's 7, about level with the full-width robot but with far
  fewer G409 touches (3 against 11). With pieces rolling as filmed, a spill is worth chasing only with something
  that widens the mouth: driven through by the plain 14 in intake, it is batted aside.
- **With a partner that can't shoot**, TIP 3 is out of reach for every shape; the V brings TIP 2 about 1 s sooner
  but scores the same.
- **The Ramp Hook (as the 8 in hook) now out-scores the Rigid V on ShootsRight (66.3, TIP 3 in 35 of 60):** held at
  its spot, TIP 2's spill is a real load, and the one smooth path into the wall FLOWER (6 Oct review) leaves time
  to fire the GARDEN. But falling pieces touch it in half the runs; G409 is the problem to solve first, with the
  ramp's own shape.
- **The intake is the limiter**: 15 pieces a run reach the front too high and 16–25 while the intake is busy
  (the study line's "intake misses per run"). See the README, "What we've learned".

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
