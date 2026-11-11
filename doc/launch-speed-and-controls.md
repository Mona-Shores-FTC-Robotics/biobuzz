# Launching faster, and controls that are easier to learn

For the mentor to decide. Nothing here changes a binding or a gate: it is the measurement from the
Belding Qualifier (7 Nov 2026), the changes it points to, a simpler layout, and a draft guide for
the drive teams. Code as on master after #532 (Circle's camera anchor) and #534 (a stick already
moving no longer cancels Square, #495; the launcher keeps spinning 3 s after Square, #497; a
launcher wheel cut for reading no speed is retried, #496). Section 2 replays those rules over the
Belding presses to say what they already recover and what is left.

**Short version.** At Belding the median time from the first press to the first piece leaving was
3.4 s on 19429 and 2.75 s on 20245. Very little of it was the aim gate or the speed gate. It went
to Square presses that did not take (19429), to the drive to the spot and to the 0.7 to 0.9 s
between the gate opening and the first piece reaching the wheels (both robots). The launcher was
at speed at the first press in 17 of 84 launches. Even a perfect launch, launcher at speed and
robot aimed, took 0.73 s to put a piece out.

Since Belding, #534 has fixed the largest loss (a moving stick cancelling Square). Replayed over
the Belding presses, its 3 s launcher grace reaches only 14 of 79 launches, so the open questions
are the launcher staying on, the feed starting before the robot has stopped, and who fires.

## 1. Where the time goes

### How it was measured

All 17 match logs on `events/2026-11-belding`, TeleOp only, with
`tools/event-review/archive/belding-scripts/ctl/launch_time.py`.

- **A launch attempt** is a chain of intent: the driver's Square, the driver's Circle, or the
  operator's FIRE, with presses less than 1.5 s apart counted as one attempt. Its clock starts at
  the first press. (The operator's launcher toggle is reported separately, as "launcher at speed
  at the first press": a toggle 40 s earlier is not the start of this launch.)
- **The first piece leaving** is the first dip in the launcher wheels' speed after the feed gate
  opened (the #506 detector replayed over the log, or the robot's own counter, whichever is
  first). It was not checked against video; the gate-to-first-piece time it gives (0.71 and
  0.77 s) matches the event review's 0.8 s.
- **Every loop from the first press to the gate opening is given to one wait**: what the feed was
  waiting for in that loop. A launcher spinning up while the robot drives to a spot counts as the
  drive, since the drive is what the feed was waiting for; the hidden spin-up is in section 2.
- Three of 19429's launches are set aside as faults, not waits: Q-32 (a launcher wheel the code
  shut off, 10 s) and two in M-10 where the first piece came 3 s after the gate opened.

### Result

| | 19429 (9 matches) | 20245 (8 matches) |
|---|---|---|
| Attempts | 39 | 67 |
| Attempts that launched | 32 (29 analysed) | 55 |
| First press to first piece, median | **3.39 s** | **2.75 s** |
| Middle half of launches | 1.36 to 5.56 s | 2.13 to 3.49 s |
| Launcher at speed at the first press | 10 of 29 | 7 of 55 |
| Launched with one press | 6 (median 1.05 s) | 9 (median 2.16 s) |
| Launched after several presses | 23 (median 3.75 s) | 46 (median 2.82 s) |

Mean seconds per launch, and the number of launches in which that wait was the biggest:

| Wait | 19429 s | biggest in | 20245 s | biggest in |
|---|---|---|---|---|
| Square press that never arrived (sticks, let go, refused) | 1.57 | 13 | 0.41 | 4 |
| Between presses | 0.57 | 2 | 0.19 | 1 |
| Drive to the spot (Square) | 0.38 | 3 | 0.78 | 19 |
| Driver aiming with Circle, no FIRE yet | 0.30 | 2 | 0.02 | 1 |
| Launcher spin-up | 0.10 | 0 | 0.18 | 3 |
| Speed gate after a change of target | 0.09 | 0 | 0.15 | 2 |
| Settle check at the spot | 0.11 | 0 | 0.23 | 2 |
| Aim gate | 0.04 | 0 | 0.00 | 0 |
| FIRE held, held back by a Square volley | 0.12 | 0 | 0.00 | 0 |
| **Feed: gate open to first piece** | 0.72 | 9 | 0.92 | 23 |

**Which wait dominates.**

- **19429: getting a press to take.** 54% of the time before a launch went to Square presses that
  never arrived and the gaps between them. Of its 70 Square presses, 22 arrived, 29 were cancelled
  by a stick within 0.1 s, 18 were refused (13 for a pose not on the field after the Pinpoint
  faults in Q-13 and M-9, 5 for being past the centre line) and 1 was let go early. When one press
  was enough, the piece left in 1.05 s.
- **20245: the feed and the drive.** Gate open to first piece was the biggest wait in 23 of 55
  launches and the drive to the spot in 19. Presses that did not take cost 0.6 s a launch: of 140
  Square presses, 67 arrived, 33 were cancelled by a stick, 27 were let go before arriving (median
  hold 0.35 s: taps on a button that must be held) and 13 were refused.
- **On both robots the gates cost little.** The aim gate held a feed back in one launch. Launcher
  speed (spin-up and re-arm together) held it back 0.19 s a launch on 19429 and 0.33 s on 20245,
  because the Square drive hides most of the spin-up.

### "Aimed, and then how long?"

This is the wait the mentor described. With the robot already where it shoots from:

| From | To the first piece | n |
|---|---|---|
| Square arrived at the spot, 19429 | 0.99 s | 9 |
| Square arrived at the spot, 20245 | 1.24 s | 48 |
| FIRE pressed, launcher at speed, 19429 | 0.73 s | 11 |
| FIRE pressed, launcher spinning but not at speed, 19429 | 0.78 s | 8 |
| FIRE pressed, launcher idle, 19429 | 1.51 s | 6 |
| FIRE pressed, launcher idle or not at speed, 20245 | 1.34 to 1.43 s | 7 |

The floor is the feed: 0.71 s (19429) and 0.77 s (20245) from the gate opening to the first piece
at the wheels, against 0.49 s between later pieces. The feeder already runs at full power behind
the shut gate, so this is the plate opening and the first piece's travel (fix list: "Film a volley
at 240 fps to find why the first ball is slow").

### Other numbers the proposals use

- **Spin-up from rest to "at speed" in TeleOp:** 0.62 s median on 19429 (15 spin-ups, longest
  1.16 s) and 0.58 s on 20245 (17, longest 1.50 s).
- **What a spinning launcher draws:** about 3 A for both wheels at speed with the gate shut (2.8
  to 3.0 A median, both robots).
- **The launcher was already being left on:** it was asked for speed for 73 to 88% of TeleOp in 6
  of 19429's 9 matches, and for 87 to 93% in 2 of 20245's 8.
- **Lane full with the launcher idle:** up to 43 s a match (19429), 1 to 14 s (20245).
- **The speed table moved the target during a Square press** in 9 of 15 (19429) and 38 of 57
  (20245) presses that opened the gate: the press spins up for the spot's range, then the camera
  sees the CELL and the target changes.
- **Circle was hardly used:** 14 presses on 19429, 2 on 20245. The aim came within 3° in 6 of the
  16, in 0 to 0.6 s. In two of 19429's Circle launches the operator's FIRE came 3.0 s after Circle
  went down. Circle has changed since (the camera anchor, #532), so its speed today is not in
  these logs.

## 2. Proposed changes, ranked by seconds per launch

Seconds are means per launch from the table above unless a row says otherwise.

### Already on master (#534), replayed over the Belding presses

| Change | What it recovers | What it leaves |
|---|---|---|
| A stick already moving at the press no longer cancels Square, Cross or Triangle (#495, `FreshSticks`) | In 58 of the 62 stick-cancelled presses the stick was back at centre within 0.6 s of the press, so the new rule would have kept them. Among presses that went on to launch: 0.80 s per Square launch on 19429 (6 of 22), 0.29 s on 20245 (21 of 67) | 20245's 27 taps let go before arriving, and the 31 refusals |
| The launcher keeps spinning 3 s after Square is let go (#497) | Re-presses inside one attempt find it at speed | **Most launches.** Only 6 of 18 (19429) and 8 of 61 (20245) launches began within 3 s of the last Square press ending; the median gap was 3.3 s and 9.0 s. The first shot of a cycle is still cold |
| A launcher wheel cut for reading no speed is retried (#496) | The 10 s fault set aside in 19429's Q-32 | |

### Still open, ranked by seconds per launch

| # | Change | Saves, 19429 | Saves, 20245 | How sure |
|---|---|---|---|---|
| 1 | Square opens the gate on the approach, as an Auto's LaunchAll already does, instead of after arriving and settling | 0.1 s sure, up to 0.6 s | 0.2 s sure, up to 0.8 s | The settle wait is measured. The rest needs a robot: do shots fired while slowing stay in? |
| 2 | The launcher is at launch speed all TeleOp, not for 3 s after Square | 0.2 s in a Square launch, 0.6 to 0.8 s in every other launch | 0.3 s, and 0.6 to 0.7 s | Sure. FIRE at speed 0.73 s, from idle 1.51 s; the 3 s grace reaches 13 to 33% of launches |
| 3 | Shorten gate open to first piece (hardware, on the fix list) | about 0.2 s | about 0.3 to 0.4 s | The gap to later pieces (0.49 s) is measured. What can be gained is not known until it is filmed |
| 4 | A tap on Square is told apart from a hold | none (1 tap) | up to 0.3 s | The 27 taps are measured; whether they were mistakes is not |
| 5 | The driver's Circle also fires once aimed and at speed (section 3) | not measurable from Belding | not measurable | Two launches with a 3.0 s wait for FIRE. Thin |
| 6 | A change of the speed table's target does not re-arm "at speed" when the wheels are already inside the new tolerance | under 0.1 s | under 0.15 s | Sure, and small |
| 7 | Loosen the aim gate or the 150 ms at-speed hold | 0.04 s | 0.0 s | Sure it is not worth it |

Notes on each.

1. **Feed on the approach.** After arriving, Square waits for the settle check (0.11 and 0.23 s a
   launch) and then the full feed lead (0.7 to 0.9 s). An Auto's `LaunchAll` opens the gate near
   the end of the path once aimed and at speed (`LaunchCards.launchOnApproach`, on at Belding);
   `SpotVolley` names the place where the same rule plugs in for Square. The first piece then
   reaches the wheels as the robot stops instead of 0.7 s after. It only works if the launcher is
   at speed before the robot arrives, which is change 2.
2. **Launcher always at speed.** In a Square launch the drive hides the spin-up, so the measured
   saving is small. It matters more than its row says: more defense means fewer clean Square
   drives and more shots from where the robot is, and every one of those pays 0.6 s of spin-up
   (1.2 to 1.5 s at worst) unless a Square press ended in the last 3 s. It costs about 3 A. The
   operators were already doing it by hand in half the matches, and the toggle's state is the
   thing they got wrong: 34 of 52 FIRE presses found the launcher idle or not at speed. Proposed
   as `TeleOpConfig.launcherAlwaysOn`, like `intakeAlwaysOn`, with the operator's Cross kept as
   the off switch; `launcherKeepSpinningAfterSquareS` then has nothing left to do.
3. **The feed lead.** Not software. It is in every launch on both robots and is 20245's single
   biggest wait, so it is worth the bench hour the fix list already asks for.
4. **Taps.** 27 of 20245's Square presses were let go before arriving, median hold 0.35 s. Either
   the driver expects a tap to work, or changed their mind. Teach "hold" first (the guide below)
   and count taps again at the next event; a latch is one more rule to learn.
5. **One decision.** See section 3.
6. **Retarget.** A new target more than 60 ticks/s from the wheels drops "at speed" until they get
   there and hold 150 ms. With the launcher always on this happens while driving, not at the spot,
   so change 2 removes most of it.
7. **Gates.** Leave them. They are not where the time goes, and without a record of which shots
   went in there is no evidence that 3° or 60 ticks/s is tighter than the shots need.

What the logs cannot say: whether shots fired earlier or from a moving robot still go in. Changes
1 and 5 each have a check in `doc/robot-todo.md` §7f before they are trusted.

## 3. A simpler layout

### What is hard to learn today

- **Three ways to make a shot happen, owned by two people.** Square (driver: drives, aims, spins
  up and fires). Circle (driver: aims only) plus FIRE (operator: fires, and spins up if idle). The
  launcher toggle (operator), whose state decides whether FIRE is fast or slow.
- **Nobody is sure who shoots.** On 19429, 15 of its 32 launches had both the driver's Square and
  the operator's FIRE pressed for the same shot, and those took a median 5 to 6.6 s. On 20245 the
  driver's Square made 48 of 55 launches and the operator's FIRE alone made 3.
- **The launcher toggle is invisible.** 10 FIRE presses found it idle, 24 found it not yet at
  speed.
- **Hold or tap is not obvious.** 27 Square presses on 20245 were let go before arriving.

### Proposal: the driver shoots, the operator keeps the robot ready

One rule for the driver: **hold a button to shoot, let go to stop.** Two buttons, the same rule.

| Driver | Today | Proposed |
|---|---|---|
| Circle (hold) | Turns the robot onto the CELL. The operator's FIRE shoots | **Shoot from here:** turns onto the CELL and feeds once aimed and at speed, for as long as it is held |
| Square (hold) | Drives to the nearest launch spot, then fires | **Go to a spot and shoot:** the same, feeding on the approach (change 1) |
| Everything else | | Unchanged |

| Operator | Today | Proposed |
|---|---|---|
| Launcher toggle (Cross) | Spin up or idle. Must be right before FIRE | **Launcher off / on.** On by itself from PLAY (change 2); pressed only to save a battery or stop a fault |
| FIRE (Circle, hold) | The trigger for Circle, and a way to shoot without the driver | **Fire now:** feeds at once at launch speed, aimed or not. For when the camera and the pose both have nothing, and for clearing the lane |
| Trim, unjam, FLOWER stick, turret | | Unchanged |

What it changes for the people: the driver decides every shot, with one hand habit. The operator
stops being a second trigger and watches the three things the Match page already shows them: trim,
READY, and the piece count.

What it costs:
- The driver cannot aim without shooting. If the team wants "line up, then wait for the HIVE", the
  operator's FIRE as the trigger has to stay; `TeleOpConfig.chassisAimGate` already makes that
  flow safe, and a switch could keep both for a practice.
- It is a new behaviour on Circle, so it needs the robot (§7f) before a match.

**A smaller step, if the mentor prefers:** change 2 only, and no change to who fires. With the
stick fix already merged, that removes the invisible toggle as well, and
leaves the Circle and FIRE question until practice logs have more than 16 Circle presses in them.

Decisions asked of the mentor:
1. Launcher on all TeleOp (change 2), replacing the 3 s grace: yes or no.
2. Square feeds on the approach (change 1): try it on the robot, or leave it.
3. Circle fires by itself: yes, behind a switch for a practice, or no.
4. Taps on Square: teach "hold", or latch.

## 4. Using the controls well (draft for the drive teams)

**Where it should live.** Which button does what is written once, in `ControlScheme`, and shown on
the Driver Station's Controls page and the printed cheat sheet; `CLAUDE.md` forbids a second copy.
This guide is the other half: when to use each button. It should be a second page of the printed
cheat sheet, generated by `CheatSheet` so its button names come from the layout and cannot go out
of date, with the one-line version of each rule as an `operator.note` / `driver.note` on the
Controls page. Until the layout decision is made it stays here as a draft; the button names below
are today's and would be filled in by the generator.

The draft, written for today's controls:

---

### How to shoot

**Driver: pick one of two buttons, and hold it.**

- **The robot knows where it is → hold Square.** It drives to the nearest launch spot, aims, and
  shoots by itself. Keep holding until the pieces are gone, then let go.
  - Hold it. A tap does nothing.
  - You can press while still steering: the robot takes over. Pushing a stick after that takes
    the robot back.
  - Use it when you are near a launch spot and nobody is in the way.
- **The robot is lost, blocked or far from a spot → hold Circle.** It turns the robot onto the
  CELL using the camera. You can keep driving with the left stick while it turns. The operator
  shoots.
  - Face the HIVE first so the camera can see our CELL.
  - Leave the right stick alone. Touching it cancels the aim until you let go of Circle.
  - Say "aimed" when the robot stops turning. Keep holding until the operator's shots are away.

**Which one?** Square is faster when it works, because it does everything. Circle works when
Square will not. If Square says no once, do not press it again: go to Circle.

**Operator: have the launcher ready before the driver gets there.**

- When the lane has pieces and the driver turns for the HIVE, spin the launcher up. Your light bar
  goes amber, then green with a buzz: green means ready.
- When the driver holds Square, do nothing. The robot shoots. Your FIRE waits for it. The
  launcher stays on for 3 s after Square is let go, then idles.
- When the driver holds Circle, hold FIRE. You can press early: it waits until the robot is on the
  CELL and the launcher is at speed, then feeds. Hold until the count on the Match page stops
  dropping.
- Shots landing short or long: trim the launcher, one press at a time (one buzz up, two down). The
  Match page shows the trim in amber until you reset it.
- A piece is stuck: hold unjam, then try again.

### When the robot is lost

The Match page tells you which kind of lost it is.

- **"SQUARE REFUSED", and no Pinpoint alert.** The robot does not know where it is yet, or it is
  too far onto the other side. Come back to our side. If it still refuses, do a corner reset:
  drive into our left or right corner, intake toward the opponents, and hold D-pad left or right
  for half a second until the pad buzzes. Square works again after that.
- **Field-centric driving feels turned.** Point the robot away from you and hold D-pad up for half
  a second. That only fixes steering, not where the robot thinks it is.
- **"Pinpoint lost".** Nothing fixes this until the robot is restarted after the match. Do not
  press Square or the FLOWER pick again. Drive by hand, face the HIVE, hold Circle, and let the
  operator shoot. Tell a mentor after the match.

### Habits that cost the most time at Belding

- Tapping Square instead of holding it (27 presses on 20245).
- Pressing Square again and again after a refusal (8 presses in 14 s in one match).
- FIRE with the launcher off: the first piece takes twice as long (1.5 s, not 0.7 s).
- Both people trying to shoot the same pieces. Agree before the match: Square means the driver
  shoots; Circle means the operator shoots.

---

If the layout in section 3 is adopted, the guide gets shorter: "hold Square near a spot, hold
Circle anywhere else, let go when the pieces are gone", and the operator's part loses the
spin-up and the FIRE rules.

## How this fits the Belding fix list

| Fix-list item | This review |
|---|---|
| Stop Square and Cross cancelling when the stick was already moving (#495) | Merged (#534). Replayed: it keeps 58 of the 62 cancelled presses. The 27 taps on 20245 are not covered |
| Keep the launcher spinning after Square so FIRE is ready (#497) | Merged as a 3 s grace (#534). Replayed: it reaches 6 of 18 launches on 19429 and 8 of 61 on 20245. On for all of TeleOp would reach every one, for about 3 A |
| Film a volley at 240 fps to find why the first ball is slow | Confirms: 0.71 and 0.77 s, the biggest single wait on 20245 (23 of 55 launches) |
| Centre the drive stick before pressing Square or Cross | No longer needed once #534 is on the robots |
| Practise driving by hand and the corner reset after a Pinpoint loss | Confirms. 13 of 19429's 18 refusals followed the Pinpoint faults. The guide separates "refused" (a reset helps) from "Pinpoint lost" (it does not) |
