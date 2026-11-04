# The official Q&A, read against our plan (14 Oct 2026, updated 4 Nov 2026)

What the [FTC Game Q&A board for BIOBUZZ](https://game-qa.firstinspires.org/boards/2027/FTC/QA) says that
changes, confirms or leaves open what our Autos, the simulator and the TeleOp map assume. First read on 14 Oct
2026 (29 questions, 21 answered); brought up to date on 4 Nov 2026: 46 questions on the board, 38 answered, with
Team Updates 05 to 07 (manual TU07, `biobuzz-competition-manual-TU07.pdf` here). Q24 was withdrawn from the board
after Team Update 06. `python3 tools/game-qa/qa.py` prints the board as it is today; re-run it before relying on a
line below that says **pending**. Changes 1 to 3 are from the first read; section 4 is what came after.

A Q&A answer is an official ruling for the season (Competition Manual §1.7.3). It outranks our reading of the
manual, the simulator's assumptions and anything another team was seen doing. Where an answer says the
REFEREES decide, plan for the strict reading.

## What changes

### 1. A G409 touch is a violation every time, and a routine one is STRATEGIC (Q17, Q23)

Q17: *"A ROBOT that deflects SCORING ELEMENTS from a tipped HIVE is in violation of G409 … if the ROBOT actions
aren't deemed to be STRATEGIC the violation would be a VERBAL WARNING."* Q23, on G407/G408 but the same logic:
a violation that is routine *"as part of the ROBOT design and/or strategy is likely to be perceived as
deliberately violating … this is likely to be perceived as STRATEGIC."*

**The penalty ladder, corrected 22 Oct 2026** (manual TU05, Table 10-4 and §10.6.1). The three rules escalate
differently, and the first draft of this review wrongly gave G409 the G407 penalty:

| Rule | Not STRATEGIC | STRATEGIC |
|---|---|---|
| G407 (a fifth piece) | VERBAL WARNING | MAJOR FOUL (20 points to the opponents) and a YELLOW CARD, once per match |
| G408 (opponent NECTAR) | VERBAL WARNING | YELLOW CARD, once per match; no points |
| G409 (a falling piece) | VERBAL WARNING | YELLOW CARD, once per match; no points |

A VERBAL WARNING carries no points, but warnings for the same thing in one match, or across matches, are
the stated trigger for "STRATEGIC" (§10.6.1 A and B). YELLOW CARDS add up: a second one in the same tournament
phase is a RED CARD, which disqualifies the team for that match (0 match points and 0 ranking points in
qualifications; in playoffs cards go to the whole alliance). So a G409 or G408 habit costs no points on the
first strategic call and the whole match on the second.

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
- **The V-pocket question** drafted in `doc/five-tip-flow-through.md` will not get a ruling: the Q&A does not
  review designs (Q29, Q34). Its second half (a momentary fifth during pass-through) is answered by Q23: yes, a
  violation. For the first half, see section 4: we decide it ourselves, on the strict reading.

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

## 4. Since 14 Oct 2026: Team Updates 05 to 07 and answers up to Q47 (read 4 Nov 2026)

### 4.1 Change 2 is in the manual now (Team Update 05, 15 Oct)

The G407 orange box: *"REPEATED instances of a ROBOT CONTROLLING 5 or more SCORING ELEMENTS are likely to be
perceived as STRATEGIC, even if the ROBOT has a 'keep 4 and discard the rest' sorting mechanism."* G408's box says
the same of a sorter that returns opponent NECTAR. Q29, which asked about lifting intakes, active expulsion and
colour rejection after intake, was answered with this and Q23. Nothing in change 2 moves.

### 4.2 A piece under the intake counts, and nobody will rule on our V (Q34, Q29)

Q34: *"a 'SCORING ELEMENT [that] is fully supported by or stuck in, on, or under the ROBOT' is CONTROLLED by the
ROBOT. This would include SCORING ELEMENTS stuck under an intake."* Both Q29 and Q34 add that the Q&A does not
give *"design reviews of a ROBOT system for legality"*. So the V-pocket question is ours to decide, and we take the
strict reading:

- **Once 4 are aboard, anything resting against the roller, under the floating roller (it rises 1.3 in, so a
  POLLEN fits under it) or stuck in the V counts toward the 4.** The robot backs off, or the mouth is shaped so
  extra pieces roll free. It does not sit on them.
- For the simulator: a run in which the robot holds 4 and has another piece touching the roller or under it
  is a G407 run. Today's "held" plus "on the floor in reach" columns don't separate those.
- For the bench: with 4 in the lane, roll a fifth into the mouth and see whether it sticks.

### 4.3 Waiting beside a spill is legal (Q35)

Q35 asked whether an Auto may wait with its intake on *"just out of range of the point where tipped game elements
hit the field"*. Answer: *"As long as the ROBOT does not 'catch or deflect SCORING ELEMENTS released by a TIPPED
HIVE unless and until that SCORING ELEMENT contacts something else besides that ROBOT.' (e.g., the TILES) per G409,
no rule has been violated."* So the wall-side catch spot (`doc/spill-first-robot.md`'s coverage scan: face 34 to
36 in from the end wall, 0 of 30 runs touched) is legal, and change 1 still governs: the only risk is a touch.

### 4.4 AUTO interference is spelled out (Team Update 06, 22 Oct)

G402's penalty is unchanged, *"MAJOR FOUL per MATCH. MAJOR FOUL and YELLOW CARD per MATCH, if STRATEGIC"*: 20
points even when accidental. TU06 adds what counts: *"A. CONTROLLING SCORING ELEMENTS located on the opposing
ALLIANCE'S side of the field, and B. contact an opposing ROBOT on their side of the FIELD."* Pieces *"deflected by
another object"* into the other half *"will likely not be penalized"*. For us:

- Spill pieces that roll over the centre line are theirs in AUTO. No route chases one.
- The simulator's centre-line check on the whole outline, V tips included, is the right test. A V tip that
  touches a piece on the blue side is a 20-point foul, not a near miss.
- Q15 (what counts as AUTO interference) was answered by pointing at TU06.

### 4.5 LEAVE, PARK and a partner (Q28, Q47; Team Update 07, 29 Oct)

- **Q28, answered 19 Oct:** a partner pushed or pulled off the wall earns LEAVE. *"There is no mandatory method
  for the ROBOT to end up in the state where it is no longer in contact with the perimeter wall. If the ROBOT is in
  the required state at the end of AUTO, regardless of how it gets there (so long as no rules are violated), the
  state will be awarded."* It rules on LEAVE only. Nothing on the board asks about a pushed robot and PARK, in AUTO
  or at the end; the same reasoning reads across, but that is our inference.
- **Q47, pending:** does a robot earn LEAVE if a POLLEN is touching both it and the wall?
- **Team Update 07** restated G304's box: a robot may touch other POLLEN at the start, but they don't count as
  its preloads, and *"ROBOTS that are no-shows for their MATCH will have their POLLEN placed in the LOADING ZONE
  against the perimeter wall."* That was in the manual before; nothing of ours planned for it. **A no-show partner
  leaves 4 POLLEN in our LOADING ZONE**, where we park. Smart Auto offers only Launch & Park and Just Park
  (`doc/smart-auto.md`); a No-show choice and one simulator run of it are open work. SWARM can't be reached with a
  no-show partner (one robot's LEAVE + PARK + PARK is 13).

### 4.6 TeleOp: defence is allowed, hoarding is not (Q38, Q40, Q42)

- **Q42:** hitting a robot that is working a FLOWER is allowed: *"Defense, contacting opponents, and intentional
  interaction with an opponent are allowable actions in BIOBUZZ."* Only §11.4.5 limits it (no damage, attaching,
  tipping or entangling). Our extractor, out and seated at a FLOWER, has to take a hit.
- **Q40:** a plate that blocks an opponent's shot does not CONTROL the piece, because *"Deflecting SCORING ELEMENTS
  in a way that is not specifically directed is not considered CONTROLLING"*. So blocking shots never counts toward
  our 4. Aiming the deflection somewhere on purpose would.
- **Q38:** parking in front of pieces at a wall or in a corner so opponents can't reach them *"may be perceived as a
  violation of G411"*, a MAJOR FOUL and a YELLOW CARD per match. That bears on TeleOp only: in AUTO the opponents
  can't cross (G402).

### 4.7 Smaller rulings

- **Q36:** a NECTAR that leaves the field may be re-entered without waiting for another TIP, while the count of
  NECTAR on the field fits G426. The referees track the minimum held out; keep the NECTAR in the ALLIANCE AREA in
  plain view.
- **Q30:** a NECTAR outside the FLOWER's pipes but partly inside its scoring volume scores, and G418 has no
  MOMENTARY exception: pieces go in through the top only. The extractor takes POLLEN from the bottom, which G418
  allows; after 1:00 it must not push a NECTAR out (`doc/ramp-hook.md`).
- **Q46, pending:** the Field Assembly Guide calibrates a HIVE to TIP at 8 POLLEN; one event's HIVE drifted to 9.
  The simulator's 7.95 POLLEN-weights matches the calibration. A drifted HIVE costs a POLLEN a TIP, and the answer
  will say whether that is a field fault worth a replay.
- **Q31:** any USB input device is allowed; whether it works is up to Android and the Driver Station.
- **Q22, Q25, Q26, Q32, Q33:** a salvaged servo potentiometer is a legal sensor; mirrors are judged by inspectors;
  HuskyLens 2 over I2C only; 2 in sign numbers pass; cooling a motor is fine if it stays at ambient.

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
| 15 | G402 | Answered by Team Update 06: controlling pieces or touching a robot on the opponent's side is disruption | Section 4.4 |
| 16 | §12.9 | No gamepad restrictions this season | #171 stands as is |
| 17 | G409 | Any deflection of a spilled piece is a violation; VERBAL WARNING if not STRATEGIC, regardless of HIVE ownership | Change 1 |
| 18 | I101 | No team size or age limit beyond STUDENT | None |
| 19 | | The answered-questions feed is Atom; "Additional Views" has a printable list | `tools/game-qa/qa.py` |
| 20 | G205 | Redirected to 2 and 8 | |
| 21 | §10.5.4 | LEAVE is judged at the end of AUTO: touching the wall then loses it | The simulator already scores LEAVE this way (`AutoSim`) and parks 1.5 in off the wall at (10.5, 95). The real Auto must do the same: SWARM is exactly 16 with both robots' LEAVE + PARK, so one wall touch costs the RP |
| 22 | R504 | A potentiometer taken out of a servo is a legal custom-circuit sensor (R607) | A fallback for turret angle sensing, not the plan |
| 23 | G407 | Orange boxes don't carry the rule's weight; routine momentary over-4 or opponent-NECTAR control is STRATEGIC | Changes 1 and 2 |
| 24 | G402 | Withdrawn from the board after Team Update 06 | Section 4.4 |
| 25 | R201 | Mirrors aren't pre-approved; nothing forbids them; inspectors judge shatter risk and edges on site | Could let the Limelight sit lower than its 45° mount needs |
| 26 | R708 | HuskyLens 2 is legal over I2C only, with a re-pinned cable; Wi-Fi streaming is banned anywhere in the venue | None |
| 27 | R403 | Redirected to Team Update 05's sign guidance | None |
| 28 | §10.5.4 | A partner pushed or pulled off the wall earns LEAVE; the state at the end of AUTO is what counts | Section 4.5. PARK not ruled |
| 29 | G407 | No design reviews; Q23 and Team Update 05 answer it | Section 4.1 |
| 30 | G418 | A NECTAR outside the pipes but in the scoring volume scores; no MOMENTARY exception to G418 | Section 4.7 |
| 31 | §12.9 | Any USB input device is allowed | None |
| 32 | R403 | 2 in sign numbers pass; scrutiny grows the further from 2.25 in | None |
| 33 | E510 | Cooling a hot motor is fine if it doesn't go below ambient | Pit practice |
| 34 | G407 | Pieces stuck under an intake are CONTROLLED; no design reviews | Section 4.2 |
| 35 | G409 | Waiting beside a spill with the intake on is legal if nothing touches the robot before the tiles | Section 4.3 |
| 36 | G426 | A NECTAR that left the field may be re-entered without another TIP | Section 4.7 |
| 37 | G405 | **Pending**: a defender deflecting shots out of the field | Watch |
| 38 | G411 | Shielding pieces from opponents may be G411 | Section 4.6 |
| 39 | R612 | **Pending**: custom slip rings | A yes would let the turret turn without a travel limit (#169, #170); watch |
| 40 | G407 | Undirected deflection of a shot is not CONTROL | Section 4.6 |
| 41 | R903 | **Pending**: controllers outside the operator console's size limit | None |
| 42 | §11.4.5 | Contacting a robot at a FLOWER is allowed defence | Section 4.6 |
| 43 | G418 | **Pending**: knocking an opponent's NECTAR out of a FLOWER with one of ours through the top | Endgame; watch |
| 44 | R105 | **Pending**: an arm held to 24 in by software though it could reach further | Our flaps and extractor are fixed by geometry; watch |
| 45 | R204 | **Pending**: a smooth pad lowered onto the tiles as a brake | Defence against shoving; watch |
| 46 | T201 | **Pending**: a HIVE that drifts to needing 9 POLLEN; field fault or not | Section 4.7 |
| 47 | §10.5.4 | **Pending**: LEAVE with a POLLEN touching both robot and wall | Section 4.5 |

## Our questions that nobody has asked yet

Drafts live in the docs named. None is on the board as of 4 Nov. Only a team account can submit.

1. ~~Pieces resting in a stationary guide (the V) while the lane holds 4~~ — not worth asking: the Q&A won't
   review a design (Q29, Q34). Decided on the strict reading in section 4.2.
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

Applied on 4 Nov 2026, with section 4:

- `doc/unified-design.md`: three more rules in "Rules every chat keeps" (pieces under the intake count, waiting
  beside a spill is legal, AUTO interference).
- `doc/smart-auto.md`: the No-show partner, as open work.
- `doc/spill-first-robot.md` and `doc/five-tip-flow-through.md`: the V-pocket question retired.
- `doc/human-nectar.md`: NECTAR re-entry after leaving the field.
- `doc/game/`: the TU07 manual added beside TU03.

Not applied, for their owners: `doc/intake-design.md` on `spike/160-intake-design` (the V angle re-run at
G409 = 0), `doc/transfer.md` on `spike/164-transfer` (the out-the-back variant), and the simulator's study
output (a `g409 > 0` run joins `problemRuns`), plus from section 4: a run that holds 4 with a fifth piece against
or under the roller counts as a G407 run, and a No-show partner run for Smart Auto.
