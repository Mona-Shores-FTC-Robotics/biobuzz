# The official Q&A, read against our plan (14 Oct 2026)

What the [FTC Game Q&A board for BIOBUZZ](https://game-qa.firstinspires.org/boards/2027/FTC/QA) says that
changes, confirms or leaves open what our Autos, the simulator and the TeleOp map assume. Read on 14 Oct 2026:
29 questions, 21 answered. `python3 tools/game-qa/qa.py` prints the board as it is today; re-run it before relying
on a line below that says **pending**.

A Q&A answer is an official ruling for the season (Competition Manual §1.7.3). It outranks our reading of the
manual, the simulator's assumptions and anything another team was seen doing. Where an answer says the
REFEREES decide, plan for the strict reading.

## What changes

### 1. A G409 touch is a foul every time, and a routine one is STRATEGIC (Q17, Q23)

Q17: *"A ROBOT that deflects SCORING ELEMENTS from a tipped HIVE is in violation of G409 … if the ROBOT actions
aren't deemed to be STRATEGIC the violation would be a VERBAL WARNING."* Q23, on G407/G408 but the same logic:
a violation that is routine *"as part of the ROBOT design and/or strategy is likely to be perceived as
deliberately violating … this is likely to be perceived as STRATEGIC."* STRATEGIC is a MAJOR FOUL and a YELLOW
CARD: a TIP's worth of points, and the card.

Until now the simulator counted "G409 runs" as a risk to weigh against points, and the docs said the longer
waits or the 45° V were the fallback "if G409 gets called on a real field". That reading is gone:

- **A G409 touch is a problem run**, the same as a collision or a HIVE hit, and disqualifies a route. The
  simulator's `g409` count becomes a pass/fail, not a column to trade.
- **The retimed waits** (`doc/unified-design.md`, "Retimed for the 0.25 s shots": wall partner 700 ms, partner
  shoots 200 ms) touched the spill in 32 and 16 runs of 60. They are withdrawn as baselines; the longer waits
  stand until a retune under G409 = 0.
- **The V's angle** (31° at 11 touched runs against 45° at 4) is re-run under the same rule. Until then 45° is
  the safe reading; the Intake Design chat owns the re-run (`doc/intake-design.md`, `spike/160-intake-design`).
- **Standing still is not a defence.** Q17's case is a robot that is merely under the HIVE when it tips. The
  rule is about where the robot's surfaces are while pieces fall, not what it is doing. A catch spot has to be
  outside the landing area (the coverage scan in `doc/spill-first-robot.md` found face positions with 0 of 30
  touches; those are the kind to use).
- **TeleOp:** Q17 says *"regardless of ownership"*. Driving under either HIVE as it tips is a violation. The
  TeleOp map's Drive assists decision (#94) gets a stay-clear item keyed off `robot.hive`'s TIP state, and the
  Match page a cue.

### 2. No MOMENTARY exception for G407 or G408 (Q3, Q4, Q23; Q29 pending)

Q3: *"CONTROL of a fifth game object is a violation, and there is no MOMENTARY exception."* Q4: the same for an
opponent NECTAR, *"for any amount of time."* Q23: the G407 orange box's "momentarily controls 5, reverses
quickly" example is not an exception, only a case likely to be non-STRATEGIC once; a robot that does it
*routinely* as part of its design is STRATEGIC. Q29 (pending) asks exactly about lifting intakes, active
expulsion and colour-reject-after-intake; its answer will sharpen this, not loosen it.

What it undoes:

- **The flow-through back exit** (`doc/five-tip-flow-through.md` items 1-2; the transfer's out-the-back variant
  on `spike/164-transfer`): a fifth piece aboard for 0.2 s per piece, every stream, is a designed-in routine
  violation. Out. Streaming through the turret, with the lane holding at most 4 by geometry, is the legal form.
- **The intake's "stall and back off" step** (#94, Intake behaviour, carried over from #103): reversing a fifth
  piece out after taking it is a violation each time. The intake has to stop before a fifth piece is CONTROLLED:
  a full sensor that halts the roller, and a mouth that doesn't trap a fifth against a stopped roller.
- **#96's line** "Holding 5 for a moment and quickly reversing one out is not [STRATEGIC]" is now "is a
  violation; a VERBAL WARNING once, STRATEGIC when routine".
- **Opponent NECTAR**: intake-then-eject is a violation every time (Q4), so rejection has to happen before
  CONTROL. For us that means colour at the mouth (a sensor that stops the roller, or the webcam steering away)
  or the drivers avoiding it. `vision/PieceVisionSubsystem` needs a range per NECTAR colour, which #94 already
  flags. AUTO is unaffected: only our NECTAR is on our side, and G402 keeps us there.
- **The V-pocket question** drafted in `doc/five-tip-flow-through.md` is still worth asking (pieces resting in a
  stationary guide while the lane holds 4). Its second half (a momentary fifth during pass-through) is answered
  by Q23: yes, a violation.

### 3. Human NECTAR in AUTO is confirmed, and must be released, not pushed (Q11, Q6, Q7)

Q11: *"DRIVE TEAM members are permitted to enter a NECTAR onto the FIELD each time their ALLIANCE causes a TIP
for their corresponding HIVE, regardless of whether that TIP occurs in AUTO or TELEOP."* This is the source the
redesign brief and `doc/human-nectar.md` were waiting for. The 11.4.6 rules still apply during AUTO (no
signalling the robot, G401).

Q6: the drive team member *"must drop the NECTAR into the LOADING ZONE"*; there is *no* exception for the piece
being entered, so touching it once it is on the tile is G425. Q7: throwing or rolling it *"in an advantageous
direction"* is allowed if it is released before it touches the tile, touches the LOADING ZONE's tile first, and
doesn't end up scoring.

So the technique in `doc/human-nectar.md` changes in one place: not "lower it to the tile, then push it along
the wall" but an underhand roll released in the air inside the zone, so its first touch is the zone's tile and
it rolls along the wall into the waiting intake. The station idea (a robot waiting at the zone's far end, turret
on the CELL) stands, and Q7 says a directional roll into it is fine.

## Every answer, and what it means for us

| Q | Rule | The answer, in short | For us |
|---|---|---|---|
| 1 | R602 | USB webcams on a powered hub are legal however the hub is powered, COTS battery pack included | Fine for the piece webcam if the Control Hub's USB can't carry it |
| 2 | G407 | Blocking a launched shot with an extended part is *not* disallowed; CONTROL still bounds G407/G408 | See 8 |
| 3 | G407 | No MOMENTARY exception. A fifth piece is a violation; VERBAL WARNING if not STRATEGIC | Change 2 |
| 4 | G408 | No MOMENTARY exception for opponent NECTAR; immediate eject "would typically not be STRATEGIC" | Change 2 |
| 5 | G408 | Redirected to 23 | |
| 6 | G425 | The human must drop the NECTAR; no exception for the one being entered | Change 3 |
| 7 | G427 | Throwing or rolling it in an advantageous direction is allowed under three conditions | Change 3 |
| 8 | G419 | Shot blocking over an opponent is legal; damage, functional impairment, G420 still apply | The map's "Defence" item is real: an opponent may hold a plate up to 29 in over our launcher in TeleOp. The turret and moving away are the answer; G402 protects AUTO |
| 9 | G408 | Redirected to 4 | |
| 10 | G408 | No rule against LAUNCHING your own NECTAR or POLLEN into the opponent HIVE | Doesn't change "only our HIVE" (#95): it helps them, and impeding their TIP is G417.D |
| 11 | G401 | NECTAR may be entered after a TIP in AUTO as in TELEOP | Change 3 |
| 12 | §10.5.1 | No rule against launching before the HIVE comes to rest; the TIP scores only when the damper touches the frame, and a shot at a moving HIVE may stop it | The "target CELL settled UP" fire gate stands (#94, Fire control). The asker's "5 NECTAR or 8 POLLEN" is their number, not a ruling |
| 13 | G408 | Redirected to 4 | The specific cases (a piece landing on us before the floor, a pile with their NECTAR in it) stay unanswered |
| 14 | §10.5.5 | No new RP achievements; Table 10-2 values fixed. Championship and Premier events may change the number, type and distribution of SCORING ELEMENTS and RP thresholds | The start state (preloads, FLOWER and GARDEN contents) stays data in Smart Auto and the simulator, never a constant |
| 15 | G402 | **Pending**: what counts as AUTO interference | Our Autos never cross the centre line; watch it |
| 16 | §12.9 | No gamepad restrictions this season | #171 stands as is |
| 17 | G409 | Any deflection of a spilled piece is a violation; VERBAL WARNING if not STRATEGIC, regardless of HIVE ownership | Change 1 |
| 18 | I101 | No team size or age limit beyond STUDENT | None |
| 19 | | The answered-questions feed is Atom; "Additional Views" has a printable list | `tools/game-qa/qa.py` |
| 20 | G205 | Redirected to 2 and 8 | |
| 21 | §10.5.4 | LEAVE is judged at the end of AUTO: touching the wall then loses it | The simulator already scores LEAVE this way (`AutoSim`) and parks 1.5 in off the wall at (10.5, 95). The real Auto must do the same: SWARM is exactly 16 with both robots' LEAVE + PARK, so one wall touch costs the RP |
| 22 | R504 | **Pending**: a potentiometer taken out of a servo | None |
| 23 | G407 | Orange boxes don't carry the rule's weight; routine momentary over-4 or opponent-NECTAR control is STRATEGIC | Changes 1 and 2 |
| 24 | G402 | **Pending**: should AUTO interference be a foul per instance | Watch |
| 25 | R201 | **Pending**: a mirror in front of a camera | Could let the Limelight sit lower than its 45° mount needs; watch |
| 26 | R708 | **Pending**: HuskyLens 2 | None |
| 27 | R403 | **Pending**: robot sign font | None |
| 28 | §10.5.4 | **Pending**: does a partner pushing a partner off the wall earn LEAVE | Matters for a dead-partner plan; watch |
| 29 | G407 | **Pending**: lifting intake, active expulsion, colour-reject-after-intake | The closest question to our intake and V; watch it, and submit the V-pocket question |

## Our questions that nobody has asked yet

Drafts live in the docs named. None is on the board as of 14 Oct. Only a team account can submit.

1. **Pieces resting in a stationary guide (the V) while the lane holds 4** — `doc/five-tip-flow-through.md`,
   first paragraph of the draft only. Q29 may answer part of it.
2. **May the launcher spin before START?** Every simulated Auto pays the spin-up (`TeamCode/README.md`, "Two rules
   every Auto here assumes"). Also whether a catapult may start cocked.
3. **A NECTAR LAUNCHED into a robot-mounted deflector and dropping through a FLOWER's top ring: "placing"?**
   Plus the scoring volume's lower edge, and whether a deflector over a FLOWER blocks the other alliance —
   `doc/flower-backboard.md`. Shelved mechanism, low priority.
4. **A thin ramp driven into a FLOWER's retrieval opening under G415 and G418**, and the penalty for a NECTAR
   pushed out by accident — `doc/ramp-hook.md`. Relevant to the FLOWER extractor.
5. The passive-ramp G409 question in `doc/ramp-hook.md` is answered by Q17: a falling piece landing on any part
   of the robot is a violation. Not worth asking.

## Where this was applied

- `doc/unified-design.md`: "Rules every chat keeps" (three new rules), the problem-run rule, the V angle, the
  retimed waits, the human NECTAR line.
- `doc/five-tip-flow-through.md`: the flow-through back exit withdrawn; the draft question trimmed.
- `doc/spill-first-robot.md`: the G407 reading corrected; step 1 points at Q29.
- `doc/staged-preloads-test.md`, `doc/ramp-hook.md`, `doc/flower-backboard.md`: their rules checklists annotated.
- `TeamCode/README.md`: "Two rules every Auto here assumes" is now one rule and one ruling.
- `doc/redesign-brief.md` and `doc/human-nectar.md` (owned by `spike/172-printed-chassis`): human NECTAR
  confirmed, the roll technique corrected, the hard limits updated.
- Issue #94 (the TeleOp map): a comment for Intake behaviour, Jams, Drive assists, Defence and Fire control.
  Issue #96: a comment correcting the MOMENTARY line.

Not applied, for their owners: `doc/intake-design.md` on `spike/160-intake-design` (the V angle re-run at
G409 = 0), `doc/transfer.md` on `spike/164-transfer` (the out-the-back variant), and the simulator's study
output (a `g409 > 0` run joins `problemRuns`).
