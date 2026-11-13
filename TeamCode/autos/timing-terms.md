# Volley and TIP timing: one set of words, measured from the Belding films

Every session, doc, PR and tool uses these names for the moments of a volley and a TIP, and says
which moment a time is counted from. `CLAUDE.md` makes this binding.

Why it exists: on 11 Nov 2026 three sessions and the Visualizer's preview were found counting "the
wait before a TIP" from three different moments, and using "dwell" and "volley time" for intervals
of different lengths. The numbers below were then re-derived from the Belding Qualifier's films
(7 Nov 2026): 18 Auto TIPs in 15 match logs, each video lined up to its robot's log by our light
and a second event, to ±0.05 to 0.1 s ("How it was measured", at the end).

## The eight moments

| # | Moment | What happens | Where it is read |
|---|---|---|---|
| 1 | **Launch starts** | The `LaunchAll` step begins | Log: `auto: command LaunchAll` |
| 2 | **Gate opens** | The launcher is at speed and settled; the feed begins | Log: `/FeedGate/Feeding` goes to 1 |
| 3 | **First ball leaves** | | Log: the first wheel dip after the gate opens |
| 4 | **Last ball leaves** | The fourth piece, or the last one the robot held | Log: the last wheel dip before the gate closes. A partner's: video |
| 5 | **Feed ends** | The gate closes and the step ends: just after the last ball if the launcher counted four shots, otherwise when the set 3.5 s runs out | Log: `/FeedGate/Feeding` goes to 0 |
| 6 | **Swing plain** | The loaded CELL is plainly dropping | Video only. It is not sharp: see below |
| 7 | **CELL settled** | The other CELL is up and still | Video |
| 8 | **CELL read up** | A camera that is facing the HIVE reports the CELL up, not assumed: the `LeftCellUp` / `RightCellUp` / `CellUp` trigger | Log |

**Moment 6 is soft, so nothing is timed from it.** The rocker does not snap over. It starts to creep
as the last ball lands, creeps for 1 to 3 s, then swings fast. Seven reviewers placed "the swing
starts" anywhere from 0.4 to 4.8 s after the last ball left, depending on what they counted as
started. Moment 7 was clear in every film.

## The four intervals

| Name | From → to | Measured at Belding |
|---|---|---|
| **Spin-up wait** | 1 → 2 | 0.05 to 0.8 s with the launcher already running (1.2 to 1.4 s twice); 0.4 to 1.4 s at PLAY, from rest |
| **Feed** | 2 → 5 | Gate to first ball 0.6 to 0.95 s; gate to the fourth ball 2.0 to 2.5 s (3.0 twice); about 0.45 s between balls. Then the feed ends 0.1 s after the last ball if four shots were counted, 1.2 to 1.5 s after it if the set 3.5 s ran out |
| **Volley** | 1 → 5 | The spin-up wait plus the feed: 2.2 to 3.5 s when the count ends it, 3.8 to 4.2 s when the set time does |
| **TIP lag** | 4 → 8 | Depends on the load: the next table |

### The TIP lag depends on how far past its tipping weight the CELL is

Last ball leaves → CELL settled (4 → 7), by what was in the CELL when it tipped:

| Load | Films | 4 → 7 |
|---|---|---|
| **At its tipping weight**: four POLLEN on the three pieces a raised CELL starts with, or eight in an empty CELL | 13 | 3.4 to 6.5 s, median 3.9. Nine of 13 within 4.3 s, eleven within 5.0 s; the slowest two 5.4 and 6.5 s |
| **Well past it**: nine or more pieces | 3 | 2.2 to 2.5 s |

A camera facing the HIVE read the CELL up 0.14 to 0.23 s after it settled (six films). So:

- **TIP lag, CELL at its tipping weight: about 4.1 s, and up to 6.7 s.** The four measured end to
  end, a waiting robot watching a partner's preloads: 3.57, 3.96, 4.07 and 4.25 s.
- **TIP lag, CELL well past its weight: about 2.5 s.**
- **A limit of last ball + 5.5 s** (`PARTNER_TIP_LAG_S`) catches 11 of the 13 at-weight TIPs.
  Last ball + 4.5 s, used until 12 Nov 2026, caught 9; + 6.7 s catches all 13.

A CELL at its weight can also hold. Eight of our own balls sat in the LEFT CELL for 7 s in M-3
without moving, and it went only when the partner added three. Six pieces (three POLLEN on the
three it starts with) sagged the CELL a pixel in Q-31 and held.

### Smaller facts from the same films

- **Flight:** a ball lands in the CELL 0.6 to 0.75 s after it leaves the launcher, from the start
  spots and the left launch spots alike.
- **Fast part of the swing:** from plainly dropping to settled, 1.1 to 2.0 s (0.5 s once).
- **The robot's logged TRANSITION is not a sighting of the swing.** It comes 0.4 to 0.9 s after
  the robot starts a drive, whatever the HIVE is doing: before any movement (Q-1), after the CELL
  had settled (Q-7, Q-31), and in every log at least once when nothing tipped. Do not time from it.
- **The robot's assumed "CELL up"** landed within 0.2 s of the real settle in Q-1 and Q-4, and
  0.8 s early in Q-32.
- **Moment 8 means a camera that is watching.** A robot that has driven off reads the CELL up when
  it next faces the HIVE, 4 to 7 s later; that is not a TIP lag.
- **The shot counter undercounts** (2 logged for 4 fired, several times); the wheel dips agreed
  with the video, except one single ball that made no dip (M-4).
- **Our PLAY is later than the field's start in a playoff,** by 0.9 to 4.0 s here. A time on the
  scoreboard is not a time on the robot's clock.

## The rules

1. **A time about a TIP is counted from "last ball leaves" (4), to "CELL settled" (7) or "CELL
   read up" (8).** Moment 4 is physical, is in the log and the video, and does not move when the
   shot counter fails. "Feed ends" (5) moves by more than a second with the counter, and "swing
   plain" (6) is soft, so nothing is measured from either.
2. **Quote the TIP lag with its load:** "at its tipping weight" or "well past it". One number for
   both is wrong by 1.5 s or more.
3. **Use the names above.** "Volley" is always 1 → 5, the whole step. The time to the last ball is
   "gate to last ball", never "volley time".
4. **Never write "dwell" alone, and never quote a pause before the swing.** The code uses "dwell"
   for a FLOWER pick's and a GARDEN gather's too. The rocker creeps from the moment the last ball
   lands, so "the pause" has no agreed start or end.
5. **A time outside these names states both ends:** "from the gate opening to the first ball", not
   "the delay".
6. **Say which clock and which tool.** The robot's clock starts at its own PLAY, the field's at the
   scoreboard's 2:30. The Visualizer's preview and the Java simulator are different models: counts
   of TIPs, parks and contacts, and whether an Auto fits in 30 s, come from the Java simulator; a
   preview's time is labelled as the preview's.

## Where each number lives, and what it counts

| Number | File | Counts | Against the films |
|---|---|---|---|
| `spinUpS` 1.1 | `timing.json` (preview) | Spin-up wait from rest | Agrees |
| `volleyS` 2.5 | `timing.json` (preview) | Volley that ends on its fourth ball: the plan (mentor, 12 Nov 2026) | A counted volley was 2.2 to 3.5 s; with the set feed running out, 3.8 to 4.2 s. The robot's feed and guard still use the longer figures (below) |
| `tipStartS` 2.6, `tipSwingS` 1.6 | `timing.json` (preview) | **5 → 6 and 6 → 8: from the feed's end, the one exception to rule 1** | 4.2 s together is the median at-weight TIP lag (4.1 s) plus the 0.1 s a counted feed runs past its last ball. Move them with `volleyS` |
| `partnerTipLagS` 5.5 | `timing.json` (preview) | TIP lag, as a give-up limit | Catches 11 of 13 at-weight TIPs |
| `PARTNER_TIP_LAG_S` 5.5 | `AutoRegistration` (robot) | TIP lag; `PartnerWaitTest` keeps it equal to `partnerTipLagS` | The same |
| `LAUNCH_ALL_TYPICAL_S`, `LAUNCH_ALL_LAST_PIECE_S` 2.5 | `AutoRegistration` (robot's guard) | Volley; and 1 → 4, as planned | The last ball left 2.6 to 3.4 s after a preload launch started, 2.1 to 4.5 s after a later one |
| `launchAllFeedS` 3.5, `launchAllEndAfterShots` 4 | `LaunchCards` (robot) | Feed | Agrees |
| `tipDwellRange`, `DWELL_S` 3.6 to 4.2 | `FieldSim`, `BeldingCalibration` (simulator) | From the ball that takes the CELL over landing to the rocker leaving its stop | See "Still to change" |

## Still to change

Recorded here so no session re-derives them; none is changed by the PR that added this page.

- **The simulator's Belding calibration treats an overloaded CELL like one at its weight**
  (`tipDwellOverloadedS` is set to 3.6 s). The films show 2.2 to 2.5 s to settled for nine or more
  pieces. It also has no slow tail: its last ball to `CellUp` is 3.9 to 4.5 s where the films run
  to 6.7 s. Changing it moves every simulator count, so it needs its own PR with before and after.
- **The robot does not yet fire a 2.5 s volley every time.** The preview and the endgame guard plan on
  one (`volleyS`; `LAUNCH_ALL_TYPICAL_S`, `LAUNCH_ALL_LAST_PIECE_S`). On the robot the feed still runs
  its set 3.5 s whenever the launcher counts fewer than four shots, which was most volleys at Belding.
  Until the count is reliable, a plan fits more into 30 s than the robot does.
- **L-Infraviolet's first wait stays at 8 s**, which gives up on the slower films: it is also what
  keeps L clear of R's arrival at the left launch spot (`infraviolet.md`).
- **Wait limits still set by the match clock.** A wait for our own TIP counts from the robot's own
  last ball best of all ("at most 6.7 s after my last ball"); the kit has no such limit yet, only
  "at most" from the wait's start and "until" on the match clock.
- **The preview counts a TIP from the feed's end.** Counting from the last ball, with the load,
  would remove the exception above. It is a Visualizer change.
- **`BeldingCalibration`'s comment still gives the old 4.0 to 4.4 s** as Belding's TIP lag. It
  changes with the calibration it describes.
- **The robot's TRANSITION on drive-off** is a `HiveTracker` artefact worth an issue: every log
  has false ones.

## How it was measured

- **Films:** the stream's clean match videos, 720p at 60 frames a second, one per robot per match
  (`tools/match-review`). Logs: the `events/2026-11-belding` branch.
- **Line-up:** our indicator light leaving the alliance colour at PLAY, confirmed by a second event
  in every match (the lane-full green, a later green, our robot's first movement at a logged path
  start, or balls in the air at logged wheel dips). Q-4's light was hidden; it was lined up from
  two of our robot's departures. No TIP was used to line anything up.
- **Log moments:** the gate from `/FeedGate/Feeding`, each ball from the launcher wheels' dips
  (`read_logs.py`'s `wheel_dips`), checked against balls seen arriving.
- **Video moments:** frame sheets at 10 to 30 frames a second, and the CELL's edge tracked frame
  by frame at 30 or 60, by seven reviewers working separately, two or three matches each.
- **Not measured:** a partner's last ball where its launcher was hidden (M-7, both alliances). The
  swing's start in Q-4, where the stream showed the other field until 6.9 s.

Seconds after our robot's PLAY. "At weight" is four POLLEN on the three starting pieces, or eight
in an empty CELL.

| Match, robot | TIP | Load | Last ball leaves (4) | Feed ends (5) | Swing plain (6) | Settled (7) | Read up (8), if watching | 4 → 7 |
|---|---|---|---|---|---|---|---|---|
| Q-26, 19429 | Our preloads, RIGHT | At weight | 2.70 | 3.95 | 4.82 | 6.15 | | 3.45 |
| Q-19, 19429 | Our preloads, RIGHT | At weight | 2.98 | 4.20 | 4.80 | 6.52 | | 3.54 |
| Q-27, 20245 | Our preloads, RIGHT | At weight | 3.41 | 3.53 | 5.9 | 7.00 | | 3.59 |
| Q-1, 20245 | Our preloads, RIGHT | At weight | 2.98 | 3.13 | about 5.2 | 7.23 | | 4.25 |
| Q-13, 19429 | Our preloads, RIGHT | At weight | 2.56 | 2.64 | 4.90 | 6.92 | | 4.36 |
| Q-4, 19429 | Our preloads, RIGHT | At weight | 2.85 | 4.01 | 7.74 | 8.25 | | 5.40 |
| Q-32, 19429 | Our preloads, RIGHT | At weight | 2.79 | 4.24 | 7.60 | 9.25 | | 6.46 |
| M-10, 19429 | Partner's preloads, RIGHT | At weight | 2.33 (video) | | 4.48 | 5.70 | 5.90 | 3.37 |
| M-4, 19429 | Partner's preloads, RIGHT | At weight | 3.37 (video) | | 5.75 | 7.10 | 7.33 | 3.73 |
| M-3, 20245 | Partner's preloads, RIGHT | At weight (four or five POLLEN) | 1.37 (video) | | 4.17 | 5.30 | 5.44 | 3.93 |
| M-10, 20245 | Partner's preloads, RIGHT | At weight (four or five POLLEN) | 1.23 (video) | | 3.73 | 5.28 | 5.48 | 4.05 |
| M-7, 19429 | Partner's preloads, RIGHT | At weight | Not seen; landed 2.95 | | about 4.9 | 6.47 | 6.65 | |
| M-7, 20245 | Partner's preloads, RIGHT | Three POLLEN on three | Not seen | | about 8.1 | 9.98 | The wait had ended at 7.90 | |
| M-7, 19429 | Our second volley, LEFT | At weight: eight | 17.41 | 17.50 | about 19.2 | 21.17 | | 3.76 |
| M-10, 20245 | Our second volley, LEFT | At weight: eight | 19.03 | 19.48 | about 22.6 | 24.03 | | 5.00 |
| Q-7, 20245 | Our second volley at RIGHT | Past it: six POLLEN on two or three | 19.93 | 20.89 | 20.70 | 22.10 | 22.25 | 2.17 |
| Q-31, 20245 | Our second volley at RIGHT | Past it: about seven POLLEN on three | 18.44 | 19.80 | 19.65 | 20.95 | | 2.51 |
| M-3, 20245 | Partner's three onto our eight, LEFT | Past it: about eleven | 25.3 (video) | | 26.07 | 27.67 | | 2.37 |

Volleys at a raised CELL that did not tip it: four in an empty LEFT CELL (five films); five (Q-26,
M-4); seven fired with at least five in (M-10, 19429; Q-19); eight in, held 7 s (M-3).
