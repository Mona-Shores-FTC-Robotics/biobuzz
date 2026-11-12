# Volley and TIP timing: one set of words

Every session, doc, PR and tool uses these names for the moments of a volley and a TIP, and says
which moment a time is counted from. `CLAUDE.md` makes this binding.

Why it exists: on 11 Nov 2026 three sessions and the Visualizer's preview were found counting "the
wait before a TIP" from three different moments, and using "dwell" and "volley time" for intervals
of different lengths. A preview that was 1.3 s early was reported as 3 s early.

## The eight moments

| # | Moment | What happens | Where it is seen |
|---|---|---|---|
| 1 | **Launch starts** | The `LaunchAll` step begins | Match log: the command starts |
| 2 | **Gate opens** | The launcher is at speed and settled; the feed begins | Match log: the gate |
| 3 | **First ball leaves** | | Match log: the first wheel dip; video |
| 4 | **Last ball leaves** | The fourth piece, or the last one the robot held | Match log: the last wheel dip (`read_logs.py volleys`); video |
| 5 | **Feed ends** | The gate closes and the step ends: at the last ball if the launcher counted four shots, otherwise when the set feed time runs out | Match log: the command ends |
| 6 | **Swing starts** | The rocker leaves its stop | Video; the camera's TRANSITION, read late |
| 7 | **CELL settled** | The other CELL is up and still | Video |
| 8 | **CELL read up** | The camera reports the CELL up: the `LeftCellUp` / `RightCellUp` / `CellUp` trigger | Match log |

## The four intervals

| Name | From → to | Measured | Source |
|---|---|---|---|
| **Spin-up wait** | 1 → 2 | From rest: median 1.1 s (0.9 to 2.1). After a drive with the launcher running: about 0.4 to 1.1 s of settle and speed. The robot feeds anyway after 2.5 s | `timing.json` `spinUpS`; `LaunchCards.maxSpinUpWaitS` |
| **Feed** | 2 → 5 | Ends on the fourth counted shot, else 3.5 s. At Belding most volleys counted fewer than four, so the feed ran the whole 3.5 s | `LaunchCards.launchAllEndAfterShots`, `launchAllFeedS` |
| **Volley** | 1 → 5 | After a drive: 3.6 to 4.6 s, median 4.0 (2 to 4 Nov). Belding, 38 volleys: 2.2 to 4.9 s | `timing.json` `volleyS`; `AutoRegistration.LAUNCH_ALL_TYPICAL_S` |
| **TIP lag** | 4 → 8 | 4.0, 4.2, 4.4 s at Belding; 4.5 is used, erring long | `timing.json` `partnerTipLagS`; `AutoRegistration.PARTNER_TIP_LAG_S` |

Inside the feed: the first ball leaves 0.8 to 0.9 s after the gate opens, and the last ball 1.7 to
2.9 s after it (2 to 4 Nov). So a feed that runs its whole 3.5 s ends about 1.2 s after its last ball.

The TIP lag has four parts, named only when a part is meant:

| Part | From → to | At Belding |
|---|---|---|
| **Flight** | A ball leaves → it lands | About 0.5 s |
| **TIP pause** | The ball that takes the CELL over lands → 6 | 1.8 to 2.4 s (see "Not yet agreed") |
| **Swing** | 6 → 7 | 1.0 to 1.4 s |
| **Camera settle** | 7 → 8 | 0.3 to 0.5 s |

## The rules

1. **A time about a TIP is counted from "last ball leaves" (4).** It is the one moment that is
   physical, in both the log and the video, and that does not move when the shot counter fails.
   "Feed ends" (5) moves by about 1.2 s with the counter, so nothing is measured from it.
2. **Use the names above.** "Volley" is always 1 → 5, the whole step. The time to the last ball is
   "gate to last ball", never "volley time".
3. **Never write "dwell" alone.** The code uses it for three things: the TIP pause, a FLOWER pick's
   dwell and a GARDEN gather's dwell. Say which.
4. **A time outside these names states both ends:** "from the gate opening to the first ball", not
   "the delay".
5. **Say which tool produced a time.** The Visualizer's preview and the Java simulator are different
   models. Counts of TIPs, parks and contacts, and whether an Auto fits in 30 s, come from the Java
   simulator; a preview's time is labelled as the preview's.

## Where each number lives, and what it counts

| Number | File | Counts |
|---|---|---|
| `spinUpS` | `timing.json` (preview) | Spin-up wait from rest |
| `volleyS` | `timing.json` (preview) | Volley after a drive, with the whole set feed |
| `tipStartS` | `timing.json` (preview) | **5 → 6: from the feed's end, the one exception to rule 1** |
| `tipSwingS` | `timing.json` (preview) | 6 → 8: the swing and the camera settle |
| `partnerTipLagS` | `timing.json` (preview) | TIP lag |
| `LAUNCH_ALL_TYPICAL_S` | `AutoRegistration` (robot's guard) | Volley, as planned |
| `LAUNCH_ALL_LAST_PIECE_S` | `AutoRegistration` (robot's guard) | 1 → 4, as planned |
| `PARTNER_TIP_LAG_S` | `AutoRegistration` (robot) | TIP lag; `PartnerWaitTest` keeps it equal to `partnerTipLagS` |
| `launchAllFeedS`, `launchAllEndAfterShots` | `LaunchCards` (robot) | Feed |
| `tipDwellRange`, `DWELL_S` | `FieldSim`, `BeldingCalibration` (simulator) | TIP pause |

## Not yet agreed

- **The TIP pause has two figures.** `BeldingCalibration.DWELL_S` is 3.6 to 4.2 s; the breakdown of
  the same Belding films in `README.md` (`partnerTipLagS`) gives 1.8 to 2.4 s. Both reproduce a TIP
  lag near 4.2 s in their own model, so they split the lag differently. Until one is re-derived from
  the films, quote the TIP lag, which both agree on, and not the pause.
- **The preview counts a TIP from the feed's end** (`tipStartS`), so `tipStartS` and `volleyS` must
  change together. Counting from the last ball in the preview would remove that; it is a Visualizer
  change, not made yet.
