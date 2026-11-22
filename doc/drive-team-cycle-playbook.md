# TeleOp cycle playbook (for the drive team)

**Goal for States (10 Dec):** about 5.4 TeleOp TIPs a match. That is 12 full volleys from one robot,
one every **9.3 s**, each carrying **4 pieces**. At Fruitport (21 Nov) a cycle took 12.8 s on 20245 and
17.7 s on 19429, and a volley carried 3.1 and 2.6 pieces.

## The budget, per volley

A cycle runs from the end of one feed to the end of the next. Seconds:

| Phase | Budget | 20245 now | 19429 now |
|---|---|---|---|
| Drive to pieces (until the first piece is in) | 2.0 | 3.6 | 5.1 |
| Pick up (first piece to last) | 2.6 | 3.4 | 5.5 |
| Drive loaded (last piece to the fire press) | 2.0 | 3.1 | 3.9 |
| Spin-up and aim (press to gate open) | 0.4 | 0.4 | 0.5 |
| Feed (gate open) | 1.3 | 2.3 | 2.7 |
| **Cycle** (includes about 1 s of slack) | **9.3** | **12.8** | **17.7** |
| Pieces a volley | **4** | 3.1 | 2.6 |

Spin-up and aim is already on budget on both robots. The time is lost **driving, picking up and
firing partial loads**. (19429 was first measured at 2.2 s of spin-up and aim. That timed from
presses that fed nothing: 3 to 5 times a match the gate opened with the lane reading 0 and no ball
came out, either an empty lane or the jam in `doc/robot-todo.md`. The robot then went to pick up, and
the pickup was counted as spin-up.)

## Rule 1: fire 4, not 3

**Fire when the robot's light goes green.** Green means the lane is full. A green lane fired 4 or
more in 74% of 20245's Fruitport volleys and 47% of 19429's. A lane that was not green fired 4 at most
9% of the time. The number on the Match page (`3 PIECES`) is a guess. It matched the shots in only
about 4 volleys in 10, so go by the green, not the number. The lane beam (PR #706), once it is fitted,
makes green need both the colour sensor and the beam to agree.

- Never press fire at a lane that is not green and reads 0. On 19429 that fed nothing 3 to 5 times a
  match. If the lane has pieces and fire does nothing, that is the jam: unjam, then fire again.
- Two exceptions: the last 10 s of the match (fire what you have), and a lane that has held 3 for
  more than about 3 s with nothing coming in (fire, then go get 4).
- **Switch to try in practice:** `TeleOpConfig.fullLoadGate` in Panels. With it on, a fire press waits
  for green, and the Match page says `FIRE: WAITING FOR 4`. The intake keeps running, so you can press
  as the last piece comes in. Held 1 s, it fires what is there (`fullLoadGiveUpS`). In the last 10 s
  it never waits (`fullLoadOffLastS`). Off by default.

## Rule 2: one end per robot

The up CELL switches ends at every TIP, and a CELL is scored only from its own end. At Fruitport
20245 chased it across the field about 4 times a match. Instead, **each robot owns one end** and
picks up near it:

- **Our CELL up** (Match page `OUR CELL UP`, green): fire 4, reload near this end, fire 4. That is the TIP.
- **Our CELL down** (`OUR CELL DOWN: LOAD 4`): load to green, sit near your launch spot, and fire the
  moment it reads up. A volley at the down CELL does nothing toward the next TIP. The TeleOp aims at
  the near CELL whatever its state, so this is your call. `TeleOpConfig.holdFireAtDownCell` (off by
  default) makes a fire wait while the camera sees our CELL down. Held 1 s, it fires anyway.
- `OUR CELL UP? (3 s ago)` is the last thing the camera saw, kept 8 s. `OUR CELL ?` means it has not
  seen it lately. Look at the field: a partner's TIP can flip it unseen.
- **Only with a partner who scores.** If their CELL has been up for 25 s with no TIP, ours never comes
  back up. Cross, fill theirs from their end, and tell them.

**What to say in the queue:**
> "Let's split the HIVE. We'll take the [left/right] CELL all TeleOp and pick up on that half. You take
> the other. When yours is up, it's yours. We won't shoot at it, and we'll stay off your half. If you're
> stuck, we'll come fill yours. How many can you shoot in one go?"

If they shoot fewer than 3 a volley or can't hold an end, don't split. Chase, but only with full loads.

## The 20-minute cycle drill

Basic Drive, one alliance, pieces spread near one end, a stopwatch, and a laptop with the repo.

1. **Warm-up (3 min).** Pick up, wait for green, fire. Call out "green" every time.
2. **Three rounds of 2 min (about 5 min each, with the reset).** Drive the 2 min as in a match, at one
   end only. Then pull the log (`tools/pull-logs/pull-logs.cmd`) and run:
   `python tools/pull-logs/read_logs.py cycles <the log>`.
   It prints each cycle's split, the pieces each volley carried, and the budget line under the mean.
   Write the mean cycle and the pieces a volley on the sheet.
   - Round 1: full loads only (fire on green).
   - Round 2: also no standing still (the `still` column under 0.5 s).
   - Round 3: also the shortest drive between pickup and launch spot. Pick the pickups at this end that
     beat 2.0 s on the drives.
3. **Debrief (2 min).** Which phase is furthest over budget? That is the next practice's focus.

**Pass:** a round with 4 pieces a volley and a mean cycle under 11 s. **Then:** under 9.3 s.
