# Five TIPs: recovering whole spills with a flow-through intake

**Status: concept, 9 Oct 2026**, from a conversation with the mentor about two of our robots in playoffs
(`doc/unified-design.md`, "Two of our robots"). Nothing is built. This brief is for the Intake Design, transfer,
turret and simulator chats.

## Why

Five TIPs need 4 + 8 + 8 + 8 + 8 = 36 pieces. Our half holds 20 that are always there (both robots' preloads, the two
FLOWERs, the GARDEN); the rest must come back out of the spills. Human NECTAR can't be entered during AUTO (mentor).
Measured today (`tools/auto-routes/sister5.py`): a robot catching a spill standing still gets about 2; picking a spill
up off the floor with the webcam gets 0-3 in 3 s. So two robots each kept to their own end get 2-3 TIPs, and the
4-TIP pair works only because one robot carries pieces across the field.

If each robot could recover a whole spill (about 8) at its own end, each end feeds itself and nobody crosses.
`tools/auto-routes/five_tip_budget.py` times that plan: with a turret that streams (fires while intaking), TIP 5's
shots are away by 29.5 s in nearly every match; with each second load picked up separately, in 2 of 3. Its step times
are partly guesses.

## The concept (mentor, 9 Oct 2026)

1. **Flow-through, not reverse intake.** The front intakes continuously while pieces travel through the robot and out
   the back. The path holds at most 4, physically, so piece 1 is out before piece 5 gets in (mentor: "we need to
   continue intaking while ensuring the 1st is out of our robot before the 5th or 6th gets in"). That keeps it within
   G407 (no more than 4 CONTROLLED at once) however fast a spill comes.
2. **A back gate.** Open: pieces flow out the back. Closed: the next 4 stay aboard for the turret. A piece counter
   closes it after 4 have gone out.
3. **Stage against the end wall.** At the firing spots the robot faces the HIVE, so its back points at the end wall
   (at (57, 20) the back is about 11 in from it). Pieces leaving the back settle in a row in that gap, against the
   wall: a known place, clear of where spills land (35-47 in out from the end wall) and of any CELL's swing.
4. **Turn during the wait, then stream.** After a spill each end waits about 7 s for its CELL to rise again. The robot
   eases forward clear of the row (about 15 in: its corners swept staged pieces in the 5 Oct staging study), turns
   180°, and waits facing the row with the turret aimed back over it. When the CELL rises: fire the 4 aboard, creep
   into the row intaking, and stream the rest. No back intake is needed; this needs a turret that can aim behind the
   robot (at least ±180°). A fixed launcher can't do it.
5. **Each robot does this at its own end, taking turns.**

| TIP | End | Pieces |
|---|---|---|
| 1 | right | R's preloads |
| 2 | left | L's preloads and the far FLOWER, streamed seated (as today) |
| 3 | right | TIP 1's spill (staged 4 + 4 aboard), topped up by the GARDEN |
| 4 | left | TIP 2's spill, all of it |
| 5 | right | TIP 3's spill, topped up by the wall FLOWER; R doesn't PARK, L parks beside it |

5 TIPs + 1 PARK is 111 AUTO points against 96 for 4 TIPs + 2 PARK; in playoffs only the score counts.

## Rules to settle early

- **G407:** the staged row must stand free of the robot while it holds its own 4. Pieces pressed between robot and wall
  could be "stuck on" it (CONTROL), making 8. So: push out, ease forward an inch, and only back into them while taking
  them in. Worth asking in the official Q&A.
- **Herding is CONTROL** (glossary: pushing a piece to a desired location). Pieces must be intaken, not shoved along.
- **G409:** nothing caught before it touches the tiles: the robot's front stays 35 in or less from the end wall while
  a spill falls.

## The question that decides it: where does a spill go?

From the simulator (`spilltrack.py` on the sister5 logs, TIP 1 with nobody near, 10 runs): of the pieces traced out of
the right CELL, about 2 come to rest at our right end, and about 2.6 roll over the centre line onto the other
alliance's half, out of reach in AUTO (G402). If that is what a real spill does, no intake at one end can recover 8,
and the work goes to stopping pieces from leaving (a robot's side along the centre line as a wall? a catcher that
reaches the pieces' path?) rather than to the intake. **The simulator's bounce is assumed, not measured.** Filming
real spills (`doc/spill-test.md`) comes first.

## Asks

- **Intake Design / transfer:** can a front-to-back path that holds at most 4, with a back gate and a piece counter,
  fit inside 18 in with the turret, the V and the extractor? What does it cost the transfer to the turret? How fast
  can pieces flow through, against how fast a spill arrives?
- **Turret:** can it aim behind the robot (at least ±180°), and keep streaming while the chassis creeps?
- **Simulator:** model the flow-through (front in, back out, at most 4 aboard, the gate), the wall row, and the turn
  and stream; then run the 5-TIP loop. Separately: where spills go, once real spills are filmed.
- **Next meeting:** film a few real TIPs from the side and from above (`doc/spill-test.md`).
