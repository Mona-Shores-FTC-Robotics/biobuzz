# Holding 4 after TIP 1: positioning, intake and front shape (simulator, 9 Oct 2026)

Sister five decides on R's count before TIP 2 ("Holding 4? Then 5 TIPs"): with 4 it makes 5 TIPs in 16 of 20 runs,
and on today's V it reaches 4 in only 20 of 60. Mentor, 9 Oct: "we know how important capturing 4 like 60/60 is".

Every row below is R's opening alone (`tools/auto-routes/opening.py`: TIP 1's preloads, the catch, the sweep off the
floor, then R holds), paired with sister5h-left so TIP 1 and L are real, 60 seeds, scored by `openscore.py` on what R
holds when TIP 2 raises the right CELL. Design changes are `RobotDesign` fields on robot 1 only (rigid V otherwise).
No row had a G409.

## Positioning and sweep (today's V)

| Route | Holds 4 |
|---|---|
| sister5l-right's opening, catch at (57.5, 21) facing the HIVE | 21 |
| catch spot 6.5 in toward the audience wall (x 51) / 3.5 in (x 54) | 2 / 16 |
| catch spot toward the centre line (x 60, 61.5) | 20 / 20 |
| robot turned 15 deg either way at the catch (intake angled) | 2-5 |
| catch 3 in closer to the HIVE (y 24) | 15 |
| catch 3 / 5 / 7 in further back (y 18 / 16 / 14) | 27 / 29 / 26 |
| sweep toward the audience side (46, 30) instead of (50, 24) | 26 |
| sweep forward (57.5, 34) / forward and toward the centre line (60, 30) | 20 / 13 |
| sweep in from the side, either direction | 1-2 |
| no webcam chase during the sweep | 11 |
| sweep after standing 0.8 s / 2.5 s (now 1.5) | 2 / 19 |
| ... with y 18 and the (46, 30) sweep: 1.2 s / 1.5 s / 2.0 s | 15 / 31 / 28 |
| preloads fired from the catch spot | 21 |
| **back to y 16, sweep to (46, 30)** ("route" below) | **32** |

TIP 1's spill drifts toward the centre line in the simulator: about 2.7 of its 7 pieces cross it and are lost (G402),
1.2 roll under the HIVE.

## The intake (today's V and route), one placeholder at a time

| Change | Holds 4 |
|---|---|
| keeps 70% / 85% (today) / 100% of what reaches it | 16 / 21 / 22 |
| a piece every 0.5 s / 0.35 s (today) / 0.2 s | 13 / 21 / 29 |
| speed limit 40 / 60 (today) / 100 in/s | 21 / 21 / 24 |
| intake as wide as the frame (15.24 in) | 23 |

Speed matters; how sure the grab is matters little, because the pieces that are lost never reach the intake.

## The front shape

| Front | Holds 4 |
|---|---|
| today's V: 15.12 x 15.24 frame, flaps 1.27 out and 2.8 forward | 21 |
| one 6 in / 8.9 in wall on the centre-line side, out after the TIP | 24 / 25 |
| the same on the audience side / both sides | 19 / 23 |
| 14 x 14 frame, flaps 2 out and 4 forward | 23 |
| 12 x 14 frame, flaps 2 out and 6 forward | 28 |
| **flaps reaching further forward** on a shorter frame (full width, 1.38 out): 5 / 6 / 7 / 8 in | **35 / 36 / 43 / 45** |
| folding flaps (24 in wide, or 8.9 in forward) | 9 / 11 (they probably never opened: the simulator lowers them like the hook) |
| **the mentor's 9 Oct front** (CAD chat): 14.1 x 16.6 frame, 9.76 in mouth flush with the face, nothing ahead | **0** |
| ... taking a piece up to 3 in out / keeping everything / with a V / all of that, best route, fast intake | 4 / 1 / 5 / 5 |

## Combined

| | Holds 4 |
|---|---|
| today's V, route, a piece every 0.2 s (0.15 s and a sure grab add nothing) | 44 |
| flaps 6 in forward, route, 0.2 s | 46 |
| **flaps 8 in forward (10 in frame), 0.2 s, either route** | **50** |

## What it says

- **Reach ahead of the intake is the lever.** Pieces that are lost never reach the intake; a V reaching 6-8 in ahead
  of it makes a pocket that keeps them. Width doesn't help.
- **Then an intake that clears pieces fast** (0.2 s each). With today's V, the route and a 0.2 s intake reach 44 of 60
  with no chassis change at all.
- **The mentor's front, as the simulator sees it, doesn't catch a spill:** a 9.76 in mouth with nothing ahead of it.
  Its helper wheels may well be what the GARDEN corner needs (the simulator can't judge that), but they don't bring the
  spill to the mouth. The ask: keep his wheels, put reach ahead of the face, and make the intake fast.
- **50 of 60 is the best so far;** the other 10 end with 3. The rest is probably pieces over the centre line.

## Caveats

- A 10-12 in long chassis is a simulator shape: whether the drive, turret and lane fit is the CAD chat's question.
- The intake numbers (grab, interval, speed limit) and every bounce are placeholders until a rig is timed and filmed.
- These are R's opening only; the Sister five score with the best front is the next run.
