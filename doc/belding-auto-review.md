# Belding Autos: what each run did, and where a second branch pays

Every Auto run at the Belding Qualifier (7 Nov 2026), rebuilt from the match log and the stream
video together, then checked against the official AUTO scores (FTCScout, saved in
`tools/event-review/data/belding/ftcscout/`). It ends with the changes worth making, each run in
the simulator first. Mentor review, 9 Nov 2026.

> **Corrected from the films, 11 Nov 2026** (`TeamCode/autos/timing-terms.md`, which has every TIP's
> times). Three things below are wrong or loose, and are left as written:
> - **M-3's second TIP was the partner's.** Eight of our balls sat in the LEFT CELL for 7 s without
>   moving; it went when 10104 drove under the HIVE and added three.
> - **"Field time" is not our clock.** In the playoffs our PLAY was 0.9 to 4.0 s after the field's
>   start. On our own clocks the second TIPs settled 3.8 s (M-7, 19429) and 5.0 s (M-10, 20245) after
>   our last ball left: the usual TIP lag, not a late TIP.
> - **A TIP time "at about N s" is the fast, visible end of the swing.** The rocker creeps from the
>   moment the last ball lands. Q-4's "about 7.5 s" settled at 8.25 s; Q-32's preload TIP settled at
>   9.25 s. The TIP lag is 3.6 to 4.3 s in the four films measured end to end, and up to 6.7 s over
>   all of them, where this review says 4.0 to 4.4 s.

R-Solo ran in qualifiers, and L-NoCross ran in the playoffs and in Ultraviolet's Q-14.
Infrared is 19429 and Ultraviolet is 20245. Times are seconds after our PLAY. Logs from before
12 Nov 2026 call the non-WALL FLOWER "FAR"; here it is the SCORING FLOWER (the Autos' leg, drawn for red; on
a blue run it is the blue AUDIENCE FLOWER), and the other one is the WALL FLOWER.
For red, the LEFT CELL is the SCORING-side CELL and the RIGHT CELL is the AUDIENCE-side CELL.

## How it was read

- **Logs.** `read_logs.py summary` gives the card lines, the wait outcomes and the feeds. The
  `/Hive/<alliance>/State` and `/Hive/<alliance>/Assumed` series give what the camera *saw* and
  what it only *assumed*.
- **Video.** The scoreboard's 2:01 → 0:08 switch falls at 76.1–76.6 s in every clip. In the
  qualifiers our PLAY is that switch minus 30.45 s. In the playoffs our PLAY was late, so it was
  placed from the indicator light: green at the first `Lane: FULL`, +0.9 s. That gives M-7 20245
  at +1.0 s on the field clock and M-10 20245 at about +2.5 s, in line with PR #517's light-sync
  (+0.9 to +3.4 s). A TIP is plain on the stream: the full CELL swings down and dumps its pieces.
  Single balls entering a CELL are not readable at 720p, so the video settles *when each TIP
  happened*, and from that, which CELL was up at each volley.
- **Official scores** give our own LEAVE and AUTO PARK (by station), and the alliance's AUTO TIPs.
- **Taken as known** (from the logs, and confirmed on video): the shot counter undercounts, worst
  right after a FLOWER pick. Lane counts run low. `Lane: FULL` can also mean a wedge or pieces
  pushed against a wall.

### One thing the logs get wrong: an "assumed" TIP after every turn away

In almost every run the HIVE tracker logs `TRANSITION` about 0.4 s after the robot turns away from
a CELL it has just fired at. It then *assumes* the flip about 3.3 s later (`Assumed` = 1). On video,
most of these TIPs never happened:

- Q-7 and Q-31: the preload volley did not TIP.
- R-Solo's second volley: the official count is 1 TIP every time.
- M-3: the LEFT CELL was still up when the robot came back.

No branch was misled, because `CellUp`, `LeftCellUp` and `RightCellUp` accept only a sighting
(`AutoRegistration.cellUp`). But anyone reading `HIVE … (assumed)` lines in a log should treat them
as noise. (Since this review the tracker no longer starts or assumes a TIP when tags leave the
frame: it counts only a change between two settled sightings.)

## Every run

**Planned** times are the export's drive seconds plus `registry.typicalS` (LaunchAll 4.5,
PickFlower 3.7). R-Solo, CELL-up route: LaunchAll 0–4.5 · WALL drive to 6.6 · pick to 10.3 ·
drive to 13.2 · wait ≤ 0.75 · LaunchAll to 17.7 · SCORING drive to 19.0 · pick to 22.7 ·
drive to 24.0 · LaunchAll to 28.5 · park to 30.6. Its timed-out route: RIGHT_LAUNCH_CLOSE 3.2,
LaunchAll, park 2.8.

L-NoCross: drive to 1.0 · wait until 8.0 on the match clock. Then the CELL-up route: LaunchAll to
+4.5 · SCORING drive +1.3 · pick +3.7 · drive +1.3 · LaunchAll +4.5 · park 1.9. The Otherwise route:
RIGHT_LAUNCH_CLOSE 3.3 · LaunchAll · WALL drive 1.8 · pick · back to LEFT_LAUNCH_CATCH 2.9 · then the
CELL-up route's steps.

**Actual** gives each card's start and end. **V1, V2, V3** are the volleys, and the number in
brackets is the log's (low) shot count.

### R-Solo (qualifiers)

| Run | Actual | Branch taken, and why | Scored (official) | What went wrong |
|---|---|---|---|---|
| Q-1 · 20245 blue | V1 0–3.1 (4) · WALL drive 3.1–4.9 · pick 4.9–8.7 · drive 8.8–13.0 · V2 13.1–16.9 (2) · SCORING 16.9–18.1 · pick 18.1–21.4 · drive 21.4–22.7 · V3 22.7–26.3 (1) · park 26.3–28.5 | **CellUp** after 0.02 s: the preload volley had TIPped the RIGHT CELL, so the LEFT CELL was up and seen | LEAVE 3, PARK 5; alliance 1 AUTO TIP (ours, the preloads) | The second TIP never came: V2 and V3 put only 3 counted pieces at the LEFT CELL |
| Q-4 · 19429 red | V1 0–4.0 (2) · WALL 4.0–5.8 · pick 5.8–9.4 · drive 9.5–13.3 · V2 13.3–17.1 (0) · SCORING →18.3 · pick 18.3–21.8 · drive →23.1 · V3 23.1–26.8 (0) · park 26.8–29.0 | **CellUp** 0.02 s. Video: the RIGHT CELL TIPs at about 7.5 s, then the LEFT CELL stays up to 30 s | LEAVE 3, **PARK 0**; 1 TIP (ours) | No second TIP. The park path ended at 28.95 s on RIGHT_PARK, but it scored no PARK. The robot is hidden on the stream; `robot-todo.md` §3 has RIGHT_PARK only 1.4 in inside the LOADING ZONE |
| Q-7 · 20245 red | V1 0–2.9 (4) · WALL →4.7 · pick 4.7–8.4 · drive 8.5–13.0 · wait 0.77 · RIGHT_LAUNCH_CLOSE 13.8–16.6 · V 16.6–20.9 (2) · park 20.9–23.8 | **Timed out**: the camera rightly did not see the LEFT CELL up. Video: the preloads did not TIP (despite the log's assumed TIP at 3.5 s) | LEAVE 3, PARK 5; 1 TIP, **made by the timed-out branch's volley at about 20.5 s** | Nothing: the branch saved the TIP |
| Q-13 · 19429 blue | V1 0–2.6 (4) · WALL →4.5 · pick 4.5–8.1 · drive 8.1–12.2 · V2 12.3–16.1 (1) · SCORING →17.4 · pick 17.4–21.0 · drive →22.4 · V3 22.4–25.1 (4) · park 25.1–27.4 | **CellUp** 0.05 s | LEAVE 3, PARK 5; 1 TIP (ours) | No second TIP, even with 5 counted at the LEFT CELL |
| Q-19 · 19429 blue | V1 0–4.2 (2) · WALL →6.0 · pick 6.0–9.5 · drive 9.5–13.8 · V2 13.9–17.7 (0) · SCORING →18.9 · pick 18.9–22.6 · drive →24.1 · V3 **24.1–29.0** (2) · park from 29.0 | **CellUp** 0.02 s | LEAVE 3, **PARK 0**; 1 TIP (ours) | V3's gate opened 1.4 s after the drive, and the feed ran to 29.0 s, so the PARK was lost (the guard lets a started feed finish) |
| Q-26 · 19429 red | V1 0–3.9 (2) · WALL →5.7 · pick 5.7–9.5 · drive →13.0 · V2 13.1–16.9 (0) · SCORING →18.1 · pick 18.1–21.6 · drive →22.8 · V3 22.8–26.5 (1) · park 26.5– | **CellUp** 0.02 s | LEAVE 3, PARK 5; 1 TIP (ours) | No second TIP |
| Q-27 · 20245 red | V1 0–3.5 (4) · WALL →5.3 · pick 5.3–8.9 · drive **9.0–17.0** (planned 2.9) · V2 17.0–20.9 (2) · SCORING →22.0 · pick 22.0–25.6 · guard parks from SCORING_FLOWER_STAGING at 25.7 | **CellUp** 0.02 s; the guard then skipped the drive back to LEFT_LAUNCH_CLOSE (4.2 s left, 4.4 s needed) | LEAVE 3, **PARK 0**; 1 TIP (ours) | Video: our robot and blue's 20863 (an opponent) were in contact under the HIVE from 11 to 16 s, which cost about 4 s. The park from the SCORING staging spot stalled about 10 in short (PR #517) |
| Q-31 · 20245 blue | V1 0–3.3 (4) · WALL →5.1 · pick 5.1–8.7 · drive →12.5 · wait 0.76 · RIGHT_LAUNCH_CLOSE 13.2–15.6 · V 15.6–19.8 (2) · park 19.8– | **Timed out**. Video: the preloads did not TIP | LEAVE 3, PARK 5; 1 TIP, **made by the timed-out branch's volley at about 19.5 s** | Nothing: the branch saved the TIP |
| Q-32 · 19429 blue | V1 0–4.2 (3) · WALL →6.0 · pick 6.0–9.7 · drive →13.5 · V2 13.6–17.4 (1) · SCORING →18.6 · pick 18.6–22.3 · drive →23.5 · V3 23.5–27.3 (1) · park 27.3– | **CellUp** 0.02 s | LEAVE 3, PARK 5; 1 TIP (ours) | No second TIP |

### L-NoCross (playoffs, and Ultraviolet Q-14)

| Run | Actual | Branch taken, and why | Scored (official) | What went wrong |
|---|---|---|---|---|
| Q-14 · 20245 blue | drive 0–0.8 · wait **timed out at 8.0** · RIGHT_LAUNCH_CLOSE 7.9–10.7 · V 10.7–14.0 (0) · WALL →15.6 · pick 15.6–18.3 · drive →22.0 · **V 22.0–25.7 (2) at LEFT_LAUNCH_CATCH** · SCORING drive →26.8 · guard skips the pick and parks from that spot | **Timed out**: the partner (9133) never TIPped. Then "go on with the steps below" ran the LaunchAll at the LEFT CELL with no check | LEAVE 3, PARK 5; **0 AUTO TIPs** | Video: no TIP all Auto. Our 4 at the RIGHT CELL (which was up) did not TIP it, so the LEFT CELL stayed down, and **the 22–25.7 s volley went at a down CELL**. The log's "LEFT up (assumed)" at 18.2 s was false |
| M-3 · 20245 red | drive 0–0.8 · V1 5.5–9.0 (3) · SCORING →10.0 · pick 10.0–13.7 · drive →15.0 · V2 15.0–18.9 (2) · park 18.9– | **CellUp** after 4.66 s (the partner's TIP, confirmed on video) | LEAVE 3, PARK 5; **2 TIPs** (partner's, then a LEFT TIP at about 27–29 s field time, after our feed had ended) | — |
| M-4 · 19429 red | drive 0–0.9 · V1 7.4–10.9 (3) · SCORING →12.0 · pick 12.0–15.7 · drive →17.1 · V2 17.1–20.8 (0) · park 20.8– | **CellUp** after 6.46 s | LEAVE 3, PARK 5; 1 TIP (the partner's) | No second TIP |
| M-7 · 19429 blue | V1 6.7–8.9 (4) · pick 10.0–13.8 · V2 15.1–17.5 (4) · park 17.5– | **CellUp** after 5.65 s | LEAVE 3, PARK 5; **2 TIPs** (the second at about 23 s field time, after our V2) | — |
| M-7 · 20245 red | wait **timed out at 8.0** · RIGHT_LAUNCH_CLOSE 7.9–10.9 · **V 10.9–14.3 (2) at the RIGHT CELL** · WALL →15.8 · pick 15.8–19.5 · drive 19.6–24.5 · V 24.5–28.4 (3) at LEFT · the guard parks from the spot at 28.4 | **Timed out**: the partner (10104) TIPped late. Video, at +1.0 s sync: the RIGHT CELL swings at about 10.0–11.0 s field time (≈ 9–10 s on our clock), and our gate opened at 10.8 s | LEAVE 3, PARK 5 (PR #517 puts the robot about 9 in short at its own 30 s; the referee scored it); 1 TIP (the partner's) | **The first volley went at a CELL that had just gone down.** The 4 preloads were wasted. The LEFT CELL was up, but it got only the WALL FLOWER pick's pieces |
| M-9 · 19429 red | drive 0–0.8 · the Pinpoint was lost at 5.4 s · timed out at 8.0 · drove blind toward RIGHT_LAUNCH_CLOSE and stopped against a FLOWER | **Timed out**. The partner's TIP came about 1.5 s after the limit (PR #517) | LEAVE 3, **PARK 0**; 1 TIP (the partner's) | The Pinpoint loss. No volley was fired |
| M-10 · 19429 blue | V1 5.9–8.3 (4) · pick 9.4–13.2 · V2 14.7–19.4 (3) · park 19.4–21.3 | **CellUp** after 4.97 s | LEAVE 3, PARK 5; 1 TIP (the partner's) | No second TIP |
| M-10 · 20245 red | V1 5.5–9.0 (2) · pick 10.0–13.9 · V2 15.2–19.4 (2) · park 19.4–21.1 | **CellUp** after 4.75 s | LEAVE 3, PARK 5; **2 TIPs** (the second at about 26 s field time) | — |

### What the table adds up to

- **R-Solo: exactly 1 AUTO TIP in all 9 runs.** In 7 of them that TIP came from the preloads, so
  the RIGHT CELL went down at once. In the other 2 (Q-7, Q-31) the preloads did not TIP it, and the
  0.75 s check sent the robot back to fire at the RIGHT CELL, which then TIPped. That branch earned
  20 points twice and never misfired.
- **R-Solo's second TIP: 0 of 7.** It needs about 7 POLLEN weights landed in the LEFT CELL (PR #519:
  no CELL tipped under 7). R-Solo's LEFT-CELL pieces all come from two FLOWER picks, and the lane
  came back short (log counts 1–4).
- **L-NoCross's second TIP: 3 of 5** (M-3, M-7 blue, M-10 red). There the LEFT CELL gets 4 sure
  preloads plus one pick.
- **AUTO PARK missed in 4 of 17 runs:**
  - Q-19: the feed ran to 29.0 s.
  - Q-27: an opponent (20863) blocked the drive under the HIVE, and the early park from SCORING_FLOWER_STAGING stalled.
  - M-9: the Pinpoint was lost.
  - Q-4: the robot reached RIGHT_PARK but was judged out of the zone (that spot sits 1.4 in inside the
    edge; `robot-todo.md` §3).
- **LEAVE was scored in all 17 runs.**
- **The robot sat waiting for the partner's TIP for 4.7–6.5 s** in the five CELL-up runs. That
  time cannot be spent on a FLOWER pick: the lane already holds the 4 preloads and holds no more,
  and they must go into the LEFT CELL after the partner's TIP.

## Volleys at a down CELL

Of 39 volleys, 2 were fired at a CELL that was down. Both came from L-NoCross's Otherwise branch,
and both are cases the code already names as risks (`doc/partner-timing.md`, "One Otherwise per
Auto").

| Run | Volley | Why the CELL was down | The card that let it happen |
|---|---|---|---|
| Q-14 · 20245 | 22.0–25.7 s, LEFT CELL | No TIP all Auto: our own RIGHT volley did not TIP it | The wait's Otherwise is set to **"go on with the steps below"**, so its LaunchAll at LEFT_LAUNCH_CATCH runs whether or not the LEFT CELL is up |
| M-7 · 20245 | 10.9–14.3 s, RIGHT CELL | The partner TIPped the RIGHT CELL about 1–2 s after our 8.0 s limit, while we were driving to it | The Otherwise drives to RIGHT_LAUNCH_CLOSE and fires there **with no "RIGHT CELL still up?" check** |

R-Solo's 13 volleys at the LEFT CELL were all at a CELL that was up: it was sighted up on
arrival before V2, and no second TIP followed. The other volleys all went at an up CELL: R-Solo's preload
volleys, its two timed-out-route volleys at the RIGHT CELL, and L-NoCross's CELL-up volleys. Each
was checked against the video's TIP times.

## Recommendations, ranked by expected points per match

The simulator runs were made with throwaway local classes written the way the exporter writes
them. They are not committed, so nothing here touches `opmodes/auto/generated/`.

- **Partners.** Each run used the curated partners from `TeamCode/sim-requests/`, 5 seeds each.
- **Late partner.** I added three runs with `"timing": {"startDelayS": 4 | 6 | 8}` and no
  `lastShotS`: the partner fires late while our wait stays at the drawn 8 s, as at Belding, where
  the drive team never entered a number.
- **Where the simulator is optimistic.** It gives R-Solo 2 TIPs in every run (Belding: 0 of 7
  second TIPs). Its TIP lag is 0.4 s (Belding: 4.0–4.4 s, PR #517). And it always TIPs the RIGHT
  CELL with our L-NoCross preloads (Q-14 did not). So it tests the *logic* of a branch more
  than its points.

### 1. L-NoCross: check the CELL before each volley in the Otherwise route (about +3 points a match in playoffs; fairly sure)

**The change, for the mentor to draw.** All three checks are new waits ("Wait for …, at most
0.75 s", the existing `cellCheckS`). Each has steps on both rows, which the builder shows as
"then: park" on its Otherwise. Use only team spots; the two new drives are the reverses of
drives that already exist.

1. **After "to RIGHT_LAUNCH_CLOSE", add the wait "TIP seen?"** on `LeftCellUp`, at most 0.75 s.
   It has two outcomes:
   - **`LeftCellUp` seen**: the partner has just TIPped, so the RIGHT CELL is down. Drive
     RIGHT_LAUNCH_CLOSE → LEFT_LAUNCH_CATCH (the reverse of the existing curve, facing the HIVE at
     the end). Then run the CELL-up route's steps: LaunchAll, SCORING drive, Pick flower, back to
     LEFT_LAUNCH_CATCH, LaunchAll, Park.
   - **Timed out**: today's behaviour. LaunchAll at the RIGHT CELL, WALL drive, Pick flower, then
     **back to RIGHT_LAUNCH_CLOSE** (a new straight drive from WALL_FLOWER_STAGING), then step 2.

   It waits on `LeftCellUp` rather than on `RightCellUp`, so it changes course only on a
   *sighting* of the TIP. A slow camera falls back to today's behaviour, never away from an up CELL.
2. **At RIGHT_LAUNCH_CLOSE, after the WALL FLOWER pick, add the wait "RIGHT CELL still up?"** on
   `RightCellUp`, at most 0.75 s:
   - **Seen up**: our first volley did not TIP it (Q-14). LaunchAll at the RIGHT CELL with the
     WALL FLOWER's pieces, then Park.
   - **Timed out**: it TIPped. Drive to LEFT_LAUNCH_CATCH, LaunchAll, Park.

   This replaces today's unconditional drive back to LEFT_LAUNCH_CATCH and its LaunchAll, which is
   the step that fired at a down CELL in Q-14.
3. **Give each new drive its park path (P).** The drives from RIGHT_LAUNCH_CLOSE reuse the
   existing bent park west of the Red HIVE.

**Simulated** (5 seeds each):

| Partner | Today | With the checks |
|---|---|---|
| alone | 28 | 28 |
| last shot at 2, 5, 8 or 12 s | 56 | 56 |
| misses, said 8 s | 36 | 36 |
| dead on its start | 28 | 28 |
| **late: last shot at 7.6 s, wait left at 8 s** | **36** (4 fired at a down CELL) | **56** |
| **late: last shot at 9.6 s, wait left at 8 s** | **36** (4 fired at a down CELL) | **56** |
| late: last shot at 11.6 s, wait left at 8 s | 36 | 36 |

No collisions, and every run parked. Two runs, "dead" and "alone", now end with 4 launched instead
of 8, for the same points: the guard reaches the end before the last LEFT volley.

**Why "fairly sure."** The logic is checked. The gain depends on two things the simulator cannot
judge well:
- The late-partner gain needs the LEFT CELL to TIP from 4 preloads plus a pick, which happened in 3
  of 5 Belding runs.
- The Q-14 case (our RIGHT volley does not TIP) cannot be simulated, because the simulator always
  TIPs it. There the change fires the WALL FLOWER's pieces at the up RIGHT CELL instead of the down
  LEFT CELL. That needs the CELL to reach about 7 weights: it held 3 NECTAR plus the 0–4 of ours
  that went in.

**Expected value.** 2 of the 8 L-NoCross runs (Q-14, M-7 red) were in these cases, and M-9 would
have been one without its Pinpoint loss. At about 0.6 × 20 points each, that is about +3 points a
match.

### 2. Keep R-Solo's 0.75 s check exactly as it is (it was worth +20 in 2 of 9 matches)

It is the reason Q-7 and Q-31 scored their TIP. Its false TIPs (logged, not sighted) never fooled
it. Don't lengthen it, and don't replace it with a wait that trusts the tracker's assumption.

**How quickly the robot knew.** "Sighted" is the first `/Hive/<alliance>/Assumed` = 0 after the drive
to LEFT_LAUNCH_CLOSE began, measured against the moment that drive ended.

| Run | LEFT CELL sighted, relative to arrival | Wait ended after |
|---|---|---|
| Q-1 | −0.48 s | 0.02 s |
| Q-4 | −0.41 s | 0.02 s |
| Q-13 | −0.39 s | 0.05 s |
| Q-19 | −0.49 s | 0.02 s |
| Q-26 | −0.32 s | 0.02 s |
| Q-32 | −0.46 s | 0.02 s |
| Q-27 | −3.34 s (an opponent held it up near the HIVE) | 0.02 s |
| **Q-7** | never: 0–3 tags, no settled sighting | timed out at 0.77 s |
| **Q-31** | never: 0–2 tags, no settled sighting | timed out at 0.76 s |

When the CELL was up, the camera saw it before the robot stopped. The approach turns onto the HIVE
in its last half second, and the camera accepted 4–7 tags. When it was down, the camera never saw
it settled at all, so today "down" can only be known by waiting out the limit.

**Why not 0.1–0.2 s.** It would have worked at Belding, but it gains nothing and risks a lot:

- **Nothing to gain.** A shorter limit saves 0.55–0.65 s, and only when the check fails. In Q-7
  and Q-31 the fallback route then finished with 6–8 s to spare.
- **A false "no" is expensive.** It sends the robot to fire at the RIGHT CELL, which is down after
  our own TIP, and drops the SCORING FLOWER leg.
- **The early sighting depends on the path.** On 3–4 Nov, at LEFT_LAUNCH_OFFSET with an approach
  that turned the camera away until late, the sighting came 0.22–0.44 s *after* the drive ended
  (`timing.json`, `cellCheckS`).

About 0.4 s would still clear every measured case, but only for an approach that ends facing the
HIVE, and only after a few practice runs confirm it.

### 3. Don't add a second branch to R-Solo yet; first find out why its second TIP never comes (no change; the open question is worth up to 20)

R-Solo spends its last 15 s on two picks aimed at a second TIP. That got 0 TIPs in 7 runs, and it
cost Q-19 its PARK.

**Candidates simulated:**

| Candidate | Simulator mean | Change |
|---|---|---|
| R-Solo as drawn (alone, still partner, dead partner) | 48 | — |
| R1: park after V2, dropping the SCORING FLOWER leg | 28 | −20, because the simulator always gets the second TIP |
| R2: a "LEFT CELL still up?" check before V3 | 48 | no change |

R2 is harmless, but at Belding it would never have fired, since the LEFT CELL was always up at V3.

**Belding and the simulator disagree** by the full 20 points, so neither candidate can be judged
here. Belding's counts say the picks come back short. If that holds, the real choice is between
R-Solo's second TIP (rare) and a sure PARK (+5, lost in 2 of 9 runs this way). The way to settle it
is on the robot, not in a branch:
- Film 5 R-Solo practice runs from the side.
- Count the pieces that land in the LEFT CELL from V2 and V3.

If they reach 7 less than about one time in four, drop the SCORING FLOWER leg (R1). Otherwise keep
it.

The "different route when a pick comes back short" idea needs a trustworthy count first: the lane
count runs low, and `IntakeFull` latches on wedges.

### 4. Ask the partner for its last shot, and enter it in INIT (free; it helps only in some matches)

The drive team never used the D-pad option. At Belding it would not have saved M-7: the partner's
TIP came about 7 s after its shots, longer than any TIP lag we would set (PR #517). It might have
saved M-9's TIP, which came 1.5 s after the limit. It is still the cheapest protection, and with
recommendation 1 drawn, a late TIP stops costing the preloads.

### Drop

- **"Use the idle partner wait for a pick or a GARDEN gather."** It can't be done: the 4 preloads
  fill the lane until the partner's TIP lets them go.
- **The log's assumed-TIP lines, as evidence.** Read only `Assumed = 0` states, or the video.

### Not ranked: worth a look

- **Q-4's missing AUTO PARK.** The robot reached RIGHT_PARK, which is only 1.4 in inside the LOADING
  ZONE. That spot is already on `robot-todo.md` §3.
- **Late parks behind long feeds:**
  - Q-19: its feed ran to 29.0 s.
  - M-7 red: its feed ended at 28.4 s, and it parked from the spot.

  The guard lets a started feed finish by design (a TIP is worth 20, a PARK 5). Both feeds started
  late because the drive before them ran long: Q-19's gate opened 1.4 s after arrival, and M-7 red's
  drive from the WALL FLOWER took 4.9 s against 2.9 planned. Worth checking against the 4.5 s
  `LaunchAll` typical before the next event.
