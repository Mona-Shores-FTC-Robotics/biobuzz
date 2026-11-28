# Human player playbook (for the Michigan State Championship, 11–12 Dec 2026)

You manage our 5 NECTAR. A NECTAR in our robot's load weighs 1.65 POLLEN toward a TIP
(`doc/three-tips.md`). A CELL tips somewhere between 7.3 and 8 POLLEN-weights, so two full loads of
8 POLLEN tip it only if every ball goes in. Put one NECTAR in those 8 and it is 8.65: **the TIP still
comes even if one ball misses.** Each TIP is 20 points.

## The rules (manual TU10)

**You may:**
- Enter **one NECTAR each time our HIVE tips**, in AUTO or TELEOP (G426.A). You do not have to enter it
  right away (G426, note): save it for the right moment.
- Enter **all the rest once the field timer shows 1:00 or less** (G426.B).
- Re-enter one of ours that left the field, at any time (G426 note, G427).
- Release it by hand, low, so it lands on a tile **inside our LOADING ZONE** (G427). A gentle roll is
  fine (Q&A, in `doc/human-nectar-auto.md`).

**You may not:**
- Enter one early: that is a MINOR FOUL per NECTAR (G426). Keep the unused ones where the referee can
  see them, and keep count out loud.
- Let it touch a robot or a field element before the tile (G427.C), use a tool (G427.A), or enter
  POLLEN or the other alliance's NECTAR (G427.B). Hand those to the field staff (G427 note). Each is a
  MINOR FOUL.
- Touch a robot, a ball already on a tile or in a robot, or the field (G425: a MAJOR FOUL and a YELLOW
  CARD if it helps us). Never reach in to fix a ball.
- Take anything off the field (G428), or score anything by hand (G427 note, G202).
- Be the drive coach: a coach may not touch NECTAR (G424).

## In AUTO, with R-RightUnderHive and L-RightUnderHive: roll the NECTAR to L

Only when our two robots run this pair. Each TIP earns one NECTAR, and L asks for each one. **Watch L's light:**
it flashes our alliance colour when L wants the NECTAR, and flashes **faster** when it is about to give up and leave.

1. **NECTAR 1, about 7 to 9 s in.** RIGHT tips (TIP 1). L, at the LEFT end near you, turns its intake toward the
   LOADING ZONE and its light starts flashing. **Roll one NECTAR straight at L's intake** (about 3 ft away), along the
   floor, as soon as the light starts. L fires its own 4 balls while the NECTAR rolls; it then catches it and shoots it.
2. **NECTAR 2, about 19 to 22 s in.** LEFT tips (TIP 2). L is now at the RIGHT end, about 6 ft away, and turns toward
   you again, light flashing. **Roll the second NECTAR firmly at L's intake**: it has to cover the 6 ft in about 3 s.
   It is TIP 3's spare: with it, TIP 3 still comes if one ball misses.

If you can't roll in time, or a robot is in the way, **keep the NECTAR**: L gives up, leaves as planned, and the
NECTAR is still yours for TELEOP. The usual rules hold: release low, onto a tile inside the LOADING ZONE, never onto
a robot. These rolls are practice targets, not yet measured: practise both until 9 of 10 reach L, and tell the
programmers how fast you roll (`doc/robot-todo.md`, "The NECTAR roll reaches L").

## Where

Our LOADING ZONE is the taped box against our wall, on the **second tile from the left corner** as
you face the field (red A5, blue F2). It is 23 in along the wall and 11 in deep (§9.3). It is on the
**LEFT CELL's half**. So it feeds the robot that owns the left end in the TeleOp plan ("one end per
robot", `doc/drive-team-cycle-playbook.md`). Agree before the match which of our robots that is.

**Drop it in the middle of the box, about 5 in off the wall, so it lies still.** A still ball is easy
to drive over. A ball that rolls out is still legal, but the driver has to hunt for it.

## When

1. **AUTO: hold them.** Our Autos do not pick up NECTAR, and a robot parks in this box at 30 s. A ball
   there can cost us the 5-point PARK.
2. **TeleOp, after each TIP of our HIVE:** wait for the left robot to finish firing and turn to reload.
   Drop it as it comes back toward the left end, so the NECTAR is one of its next 4. Spend AUTO's
   NECTAR the same way, once the robot has left the box.
3. **Never drop while a robot is in the box, or about to drive through it.** It would hit the robot
   before the tile (G427.C). Wait 2 s and go.
4. **At 1:00 on the field timer:** the rest may all go in. Enter them **one at a time**, one per
   reload, not as a pile. **Stop at 0:15.** Our robots park in this box at the end (PARK, 5 points
   each, §10.5.4).

## How many

At Fruitport (21 Nov) about 1 of our own NECTAR a match reached each robot: 13 in 15 match logs, all in
TeleOp. Only 3 of them were picked up near the LOADING ZONE, and the colour gate of the time threw out
most of them. Both robots now keep our own NECTAR (#709). **Target for States: every NECTAR we earn goes in, and the left robot fires at least 2 of
them a match.** That is a first target. Raise it after the first few matches.

## What to say (short, every time)

| Who | When | Says |
|---|---|---|
| You | our HIVE tips | "TIP, one NECTAR." |
| Left driver | turning to reload | "Reloading left." That is your cue. |
| You | it lands | "NECTAR in." |
| You | the box is busy | "Zone busy." Then "NECTAR in" when it is in. |
| You | field timer 1:00 | "One minute, N left." |

Never wave at or signal the robot. You talk to the driver only.

## The 10-minute drill (beside the cycle drill)

Run it during the driver's 20-minute cycle drill at the **left** end. Tape a 23 × 11 in box on the
wall, second tile from the left corner. You need 5 NECTAR of our colour and the tally line below.

1. **Dry drops (2 min, no robot).** 10 drops into the middle of the box. Count the ones that stop in
   the box and lie still.
2. **With the robot (6 min, the driver's three 2-minute rounds).** The coach calls "TIP" at random,
   3 times a round, plus "one minute" once in round 3. Each time, run the whole routine: wait for
   "Reloading left" and a clear box, drop, and call "NECTAR in". Another student picks up the loose ones
   between rounds.
3. **Tally (2 min).** Fill in the line. To check what was fired, run
   `python3 tools/pull-logs/read_logs.py series <the log> /Events | grep "NECTAR in"`.
   It lists every NECTAR the lane took in, with the time.

**The count to record:**

```
Date ____  Human ____  Dry: still in box __/10
Drops __   Clean __   Picked up on the next load __   Fired __   Late (over 3 s after the cue) __
```

A **clean** drop lands on the box's tile first, touches no robot, and nobody reaches in. **Pass:** every
drop clean, and 8 of 10 picked up on the next load. A drop that is not clean would be a foul in a
match, so work on that first.
