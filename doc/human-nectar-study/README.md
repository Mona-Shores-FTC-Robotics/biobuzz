# Human-player NECTAR study: scratch files (16 Nov 2026)

Working files from the "Two TIPs for sure" chat, kept so a fresh chat can pick the idea up after the
Auto baseline lands. Nothing here is built or run by CI. The draft itself is
`TeamCode/autos/drafts/r-solo-human-player.pp`; its notes are in `TeamCode/autos/drafts/README.md`
and its robot checks in `doc/robot-todo.md` section 3.

## What is here

| File | What it is |
|---|---|
| `gen.py` | Builds a variant `.pp` from `drafts/r-two-tip.pp`: the wait at the handoff (ms), which LEFT spot the first volley is from, the handoff pose. Its paths point at the chat's scratch folder; change them before running. |
| `variants/hp-w0.pp`, `hp-w5.pp` | First LEFT volley from `LEFT_LAUNCH_CATCH`, no wait and a 0.5 s wait at the handoff. |
| `variants/hp-o0.pp`, `hp-o5.pp` | Both LEFT volleys from `LEFT_LAUNCH_OFFSET`, no wait and a 0.5 s wait. `hp-o0` is the draft as committed. |
| `variants/r-solo-human-player-2.pp` | The second chance: after the SCORING FLOWER pick the robot turns its intake toward the LOADING ZONE at (38, 121.5, 185°), waits up to 1.5 s for a full lane, then turns to shoot. Drawn on the first drawing (CATCH, 1 s wait). Exported once, **never simulated**. |
| `study.sh`, `batch1.sh` | How the runs were made: `AutoStudyTest` with the Belding calibration and an aimed roll. |
| `results-60-seeds.txt` | The summary line of each run: baseline R-Two-Tip and five variants, 19429's and 20245's loads, volleys ending on the fourth shot (`wf0`) and as long as at Belding (`wf1`). `RSoloHumanPlayerAuto` in this file is the first drawing (CATCH, 1 s wait), not the committed draft. |
| `trace-3-seeds-first-drawing.txt` | Three traced runs of the first drawing: the NECTAR is taken, and the guard parks after the second volley. |

The variants are hand-assembled inputs. To use one, save it the way the app does:
`node scripts/save-auto.mjs <file.pp> <biobuzz checkout> drafts` in the Visualizer repo.

## The roll the simulator used

`BIOBUZZ_AUTO_HUMAN_ENTRY=aim BIOBUZZ_AUTO_HUMAN_AIM_SPOT="20,88,110" BIOBUZZ_AUTO_HUMAN_AIM_SPEED=7`:
released at (5.5, 95) about 1 s after the first TIP, a 12 in roll to the intake mouth of a robot at
the handoff, at rest before the robot arrives. The simulator does not model a person judging the
pick. To model a late roll, set `BIOBUZZ_AUTO_HUMAN_AIM_AT_S`; this was not run.

## Found on the way, and written nowhere else

**The lane count cannot say "not full".** On 20245, 14 Nov (`logs/2026-11-14-20245-RC`):

| Run | Pick | `/Lane/Count` after the pick | Balls the next volley fired (wheel dips) | `/Lane/Full` on the drive after |
|---|---|---|---|---|
| R-Clockwise 13:36 | WALL FLOWER | 2 | 4 | true throughout |
| R-Clockwise 13:36 | SCORING FLOWER | 3 | 4 | true throughout |
| R-Solo 13:05 | WALL FLOWER | 1, then 2 | 5 | true for 0.3 s only |

- The tracker ends a piece only when brightness falls below `leaveBelow` (0.05); between touching
  balls it fell only to 0.15 to 0.18, so several balls counted as one.
- `/ColorSensor/Alpha` shows three humps on the first pick and two on the second. It sits at 1.00
  once balls bunch up and changes about every 130 ms, while all four arrive within about 1.6 s.
- The intake motor goes from 0.3 A at 2600 ticks/s idle to 2 to 9 A and as low as 200 ticks/s with
  balls coming through: separate spikes for the first one or two, then one long load. It tells
  "nothing came in" from "something came in", not a count.
- The star wheels and prefeed are servos, with nothing to read back. The feeder peaked at about
  1.25 A on both picks with no shape.
- A beam across the lane counts touching balls only if it is above or below their mid-height
  (issue #213; branches `claude/intake-beam-break` and `claude/beam-break-bench`).

Three picks on one robot: a description, not a calibration.

## Rules, as read from the manual at TU09

- One NECTAR for each TIP of our own HIVE (G426.A), so one before the second TIP.
- Entered by hand, by our drive team, and it must touch the LOADING ZONE tile before a robot or a
  field element (G427). Never touched once it is on a tile (G425.B).
- A no-show robot's four POLLEN are placed in the LOADING ZONE against the wall (G304 note).
- A robot light flashing faster than 5 Hz invites scrutiny (Section 12); nothing bars a light.
