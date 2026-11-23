# Drive coach checklist

One page for the drive coach, per match. Each line is here because it cost points at the Belding
Qualifier (7 Nov 2026); the evidence is in the Belding Fix List (`tools/match-review/videos.json`,
`fixlist`) and `doc/belding-auto-review.md`. Controls are not listed here: the Driver Station's
Controls page and the printed cheat sheet are the only place for those.

## In the queue, with the partner

- [ ] **Ask the partner when their last preload leaves**, in seconds after the start. Take their
      slowest honest answer. It is the one number we ask a partner.
- [ ] **Agree who parks where**: R- Autos end on `RIGHT_PARK`, L-NoCross on `LEFT_PARK`. If the
      partner's Auto parks on ours, ask them to take the other.
- [ ] **Plan on 8 POLLEN weights to TIP a CELL, not 7.** Belding's Field 1 tipped at about 7 and
      Field 2 at about 8.

## On the field, before the match

- [ ] Robot on the marks (wall and seam), never placed by the camera.
- [ ] Both gamepads assigned: no `Gamepad 2 (operator) not assigned` on the Driver Station.
- [ ] **A left-side Auto (L-NoCross): ask the partner "do you shoot your preloads right at the start?"**
      Yes: touch nothing; the Driver Station reads "waiting for TIP 1 until 9 s". Later: gamepad 1
      D-pad right, a second a press, to 6 s after the second they say they shoot. They don't shoot: D-pad left
      to 0, so the robot does not wait. Left on the default at Belding, the wait timed out 3 times.
- [ ] **L-NoCross: ask the partner "if your preloads miss, do you shoot at that CELL again?"**
      No, or not sure: touch nothing; the row reads `if no TIP 1: CROSS AND FIX IT` (L crosses under the
      HIVE and fixes TIP 1). Yes, or the partner is our R-NoCross: D-pad down to the `if no TIP 1` row,
      right once, `if no TIP 1: WAIT FOR PARTNER FIX` (L stays home and waits for the LEFT CELL until 23 s).
- [ ] **R-NoCross: play it safe, or fix it and risk contact?** It reads `if no TIP 2: WAIT FOR
      PARTNER` (safe: R stays home, waits for the partner's TIP 2 until 23 s, parks). To fix a
      TIP 2 the partner leaves one piece short: D-pad down, right once, `if no TIP 2: CROSS AND
      FIX IT`. At 20 s R then drives the wall side to the left end, puts its four in the LEFT CELL
      if it is still up, and parks on `RIGHT_PARK`, beside a partner on `LEFT_PARK`. Ask the
      partner where it parks and when; one in that lane, or still there after 20 s, means stay safe.
- [ ] **L-StagedSweep (draft, Utility menu `Drive: L-StagedSweep`): the robot starts empty, its 4 preloads on the
      tiles** (manual TU10 §10.3.4: preloads "placed on the TILES fully contained on its own ALLIANCE side of the
      FIELD contacting the ROBOT"; G304.G). Red, as drawn (blue is the same turned half a turn):
      - **Robot:** back against the far wall (the SCORING FLOWER's wall), facing the HIVE, its centre-line side
        about **3.5 in short of the centre-line seam** (3.1 in toward the FLOWER from the L-NoCross start), leaving
        **about 1 in** between its FLOWER-side frame and the FLOWER. It must not touch the FLOWER (G304.D).
      - **The 4 POLLEN:** in one row along the robot's **centre-line side**, each touching the frame and the one
        next to it, the row centred on the side (the last ball about 2 in from the wall). Every ball fully on our
        side of the centre-line seam (under half an inch to spare): none on or over it. Nothing in front of the robot: its first drive is
        straight ahead.
- [ ] **Every Auto: Endgame, PARK FIRST or TIPS FIRST?** The last row of the INIT menu reads
      `Endgame: PARK FIRST`. That keeps AUTO PARK (5 points, and it decides the SWARM RP whenever a partner
      misses its LEAVE or its end-of-match PARK) and fires a late volley only if the park still fits after
      it. TIPS FIRST (D-pad down to the row, right once) fires a late volley whose last ball is out by 30 s
      even if the park is lost: a TIP is 20 points. Choose:
      - **Quals: PARK FIRST**, unless the partner says its Auto both LEAVEs and PARKs every time (then SWARM
        is covered without ours) *and* our shots are landing today. A partner with no Auto, or one that
        does not park in AUTO: PARK FIRST, always.
      - **Playoffs (no ranking points): TIPS FIRST** when our shots are landing; the park is only 5 points.
      What is at stake in a qualification match (manual TU10 Tables 10-2, 10-3; Team Update 10, 19 Nov 2026:
      the State Championship's thresholds are the qualifiers'): win 3 RP, tie 1; SWARM 1 RP at 16 LEAVE + PARK
      points for the alliance (both LEAVEs and both end PARKs make exactly 16); POLLINATOR 1 and 2, 1 RP each,
      at 4 and 7 TIPs in the match. Playoffs award no RPs (§13.7).
      In the simulator (22 Nov 2026) TIPS FIRST gained a TIP only beside a partner that shoots late, and
      with real shot rates it lost the park in 20–40% of the runs where it mattered. R-Clockwise's Priority
      follows this row: TIPS FIRST is its THREE_TIP leg.
- [ ] No amber or red line on the Match page; the light shows our alliance.

## At the start

- [ ] **The driver presses PLAY on the field's start signal; the coach calls it.** At Belding our
      Autos started 1–4 s late in the playoffs, so our 30 s ran past the field's: it cost the
      AUTO PARK in M-7.

## During TeleOp

- [ ] **"Pinpoint lost" on the Match page:** call "drive by hand". Square and the FLOWER pick stay
      refused for the rest of the match (only a restart clears it), so one refused press is the
      last one. Restart the robot after the match.
- [ ] **At most one of the two robots on FLOWERS, and only in the last 30 s.** Not from Belding:
      it is the NCSSM simulator's lesson 4 (`doc/other-biobuzz-repos.md` § 10). Two robots on
      FLOWERS for the last minute was its worst plan tested (−68 points). The one exception is a
      partner that can't score reliably: ask them to defend, or at least to stay off our launch
      spot and our pieces (its defense section).

## Right after the match

- [ ] **Check our AUTO PARK and LEAVE marks on the scoreboard** before leaving the field. If a
      park we made is not marked, go to the question box at once, while it can still be fixed.
      Both Belding misscores were AUTO PARK calls (Q-4, M-7).
- [ ] **A HIVE that tipped early or held too long:** note the match and report it at the question
      box.
- [ ] Restart the robot if the Pinpoint was lost. Swap the battery.
