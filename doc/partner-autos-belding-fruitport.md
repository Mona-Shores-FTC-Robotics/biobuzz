# What our partners did in AUTO: Belding and Fruitport

Every AUTO our alliance partners ran at the Belding Qualifier (USMIBEQ, 7 Nov 2026) and the Fruitport
Qualifier (USMIFRQ2, 21 Nov 2026): 32 partner runs, 20 in qualifiers and 12 in playoffs. It grounds the
simulator's partner stand-ins (`TeamCode/autos/partners/`) and the partner cases in
`TeamCode/sim-requests/*.json`. Mentor work, 26 Nov 2026.

## Sources, and what each one settles

- **FTCScout** (`api.ftcscout.org`, per-station `autoLeave` and `autoParkPoints`, alliance `autoHiveTips`):
  the official LEAVE (3 or 0) and AUTO PARK (5 or 0) of each partner, and the alliance's AUTO TIPs.
- **Stream video**, the clean match videos in `tools/match-review/videos.json`, read with
  `tools/match-review/match_review.py`. This is the only source for a partner's start, its shots, its moves and
  how close it came to us. Belding was read for `doc/three-tips.md` § "What our partners did in AUTO at
  Belding" and is copied from there. Fruitport was read for this page.
- **Our match logs** (`events/2026-11-belding`, `events/2026-11-fruitport`) place our robot. They do not see the
  partner, so a contact is never read from a log alone.
- **The Belding gallery** ("Belding Video Evidence") has three partner stills (Q-7, Q-14, Q-19) that agree with
  the table.
- No scouting data exists in the repo or on any branch.

**Times.** Belding times are seconds after *our* PLAY, as in `three-tips.md`. Fruitport times are **field AUTO
seconds** from the scoreboard clock (2:30 at the field's start, 2:01 → 0:08 at 30 s). At Fruitport the
helper's light sync was wrong or missing in 8 of 15 videos, so it was not used. Times are ±0.5 s.

**Words.** "Left start" and "right start" are `LEFT_START` and `RIGHT_START` as each alliance sees them. The left
start is on the scoring-table wall in front of the LEFT CELL. The LOADING ZONE is at that same end of the
alliance wall. "Corner" is where the alliance wall meets the scoring-table wall, just past the LOADING ZONE.
**CAN'T TELL** means the video does not show it.

## Every partner run

L / P is the official LEAVE / AUTO PARK. "Near us" is from video only: a top-down overlap is not proof of contact.

### Belding (USMIBEQ)

| Match | Us | Partner | Start | Shots and TIP | Moves, and where it ended | Still / dead | L / P | Near us |
|---|---|---|---|---|---|---|---|---|
| Q-1 | 20245 R-Solo | 18975 | far side, by the SCORING FLOWER (left start) | none (likely; half hidden) | 1.0–4 s to the LOADING ZONE | no | 3 / 5 | CAN'T TELL |
| Q-4 | 19429 R-Solo | 7911 | audience wall, beside our start (likely) | none seen after 7 s; the stream showed the other field before that | never moved | still all AUTO | 0 / 0 | CAN'T TELL |
| Q-7 | 20245 R-Solo | 18092 | far side, 2 POLLEN at its front | none | 1.5–4.5 s to the alliance wall; its 2 POLLEN rolled out by the FLOWER | no | 3 / 5 | CAN'T TELL |
| Q-13 | 19429 R-Solo | 18975 | as Q-1 | none | 1.0–4 s to the LOADING ZONE | no | 3 / 5 | CAN'T TELL |
| Q-14 | 20245 L-NoCross | 9133 | right start | about 4, 0.5–3.4 s, at the RIGHT CELL; none stayed in. No TIP | 3.9–8 s to the alliance wall | no | 3 / 5 | ended beside it by the wall at 28.4 s |
| Q-19 | 19429 R-Solo | 17283 | in the LOADING ZONE, 4 POLLEN in it | none; the 4 were still in it at 31 s | never moved | still all AUTO | 0 / 5 | we passed at 12 s |
| Q-26 | 19429 R-Solo | 20862 | alliance wall, in the corner | none | never moved | still all AUTO | 0 / 0 | CAN'T TELL |
| Q-27 | 20245 R-Solo | 15547 | far side, by the corner; looked loaded | none | 0.5–2.0 s, a short slide into the corner | no | 0 / 5 | CAN'T TELL (the 11–16 s contact was with opponent 20863) |
| Q-31 | 20245 R-Solo | 5292 | far side (left start; we had the right start) | none | 1.5–4.5 s to the corner | no | 3 / 5 | CAN'T TELL |
| Q-32 | 19429 R-Solo | 15397 | far-side wall, near the corner; at least 1 POLLEN in it | none | never moved | still all AUTO | 0 / 0 | CAN'T TELL |
| M-3 | 20245 L-NoCross | 10104 | right start | 4, last out 1.5 s; TIP 1 (RIGHT) at 3.7–5.2 s | sat to 19 s, then under the HIVE | no | 3 / 5 | not near (gallery) |
| M-4 | 19429 L-NoCross | 5292 | right start | about 6, last out 4.8 s; TIP 1 at 5.7–7.0 s | from 7 s to the alliance wall | no | 3 / 5 | CAN'T TELL |
| M-7 | 20245 L-NoCross | 10104 | right start | 4, last out 1.5 s; TIP 1 late, 8.0–9.8 s | from 14.5 s; under the HIVE at the end | no | 3 / 0 | our park drive ran along its side (gallery "uv-m7-crawl") |
| M-7 | 19429 L-NoCross | 5292 | right start | about 6, last out 2.5 s; TIP 1 at 4.7–6.0 s | from 5.6 s | no | 3 / 5 | CAN'T TELL |
| M-9 | 19429 L-NoCross | 5292 | right start | 6 or 7, last out 3.0 s; TIP 1 late, 9.5–10.3 s | to the alliance wall by 8 s | no | 3 / 5 | CAN'T TELL |
| M-10 | 20245 L-NoCross | 10104 | right start | 4, last out 1.0 s; TIP 1 at 3.7–5.2 s | under the HIVE at 21.5 s, then parked | no | 3 / 5 | CAN'T TELL |
| M-10 | 19429 L-NoCross | 5292 | right start | 6 or 7, last out 2.5 s; TIP 1 at 4.1–5.3 s | from 5.5 s | no | 3 / 5 | CAN'T TELL |

### Fruitport (USMIFRQ2)

| Match | Us | Partner | Start | Shots and TIP | Moves, and where it ended | Still / dead | L / P | Near us (field s) |
|---|---|---|---|---|---|---|---|---|
| Q-2 | 19429 R-Solo | 20863 | in the corner, just past the LOADING ZONE tape | none seen; 2 yellow pieces sat beside it | never moved | still all AUTO, lights on (not dead) | **3** / 0 | we parked in the zone beside it, about half a robot width away, 29.2–30 s; no contact seen |
| Q-4 | 20245 R-Solo | 11531 | at or near the left start | none seen | 1.0–4 s to the corner and a little down the alliance wall, over the LOADING ZONE | no | 3 / 5 | corner to corner within inches at about 11 s; pressed against its side 27.5–30 s, likely contact, it did not move |
| Q-11 | 20245 R-Solo | 6482 | in the LOADING ZONE (a referee partly blocks the view) | CAN'T TELL; nothing seen leaving it | never moved | still all AUTO, lights on | 0 / 5 | parked beside it, apparently touching, 26.3–30 s |
| Q-17 | 19429 R-Solo | 20862 | scoring-table wall, behind the LEFT CELL toward the corner (at or near the left start) | none seen 0.5–7 s | never moved | still all AUTO, lights on | 0 / 0 | about 1.5 robot widths from us at LEFT_AUTO_HIVE 13–16 s; within half a width from 20 s and alongside 25–30 s; contact CAN'T TELL (CELLs block the view) |
| Q-20 | 20245 R-Solo | 5383 | left start | none seen 0–8 s | 1.0–4 s along the wall to the corner, just outside the LOADING ZONE | no | 3 / 0 | about 1.5 widths at 15–18 s; a few inches apart in the zone 20–30 s; no contact seen |
| Q-23 | 19429 R-Solo | 15510 | alliance wall, on the edge of the LOADING ZONE tape | none seen | at most a few inches 1–2 s | still from 2 s | 0 / 5 | 1.5 widths at about 8 s; parked side by side within inches 20–30 s; contact CAN'T TELL |
| Q-28 | 20245 R-Solo | 11266 | near the corner, about a tile from each wall (not on the left start) | none; about 4 balls stayed on it | 2–4 s about a tile to beside the LOADING ZONE | still from 4 s | 0 / 5 | within a robot width at 24 s; parked right beside it from 25 s; contact CAN'T TELL |
| Q-28 | 19429 R-Solo | 22313 | left start | none; balls stayed in it | 1–3 s into the LOADING ZONE | still from 3 s | 0 / 5 | within a robot width at 12 s and 22 s; parked touching or almost touching at 30 s; contact CAN'T TELL |
| Q-39 | 19429 R-Solo | 14933 | about the left start, 4 yellow preloads on top | CAN'T TELL (hidden by the CELL); none seen leaving | 1.2–2.5 s about a tile along the scoring-table wall toward the alliance wall; stopped behind the LEFT CELL | still from 2.5 s | 0 / 5 | within a robot width at 12 s; 1–2 widths 23–30 s; no contact seen |
| Q-42 | 20245 R-Solo | 16220 | in the corner, just above the LOADING ZONE tape | none; no balls seen on it | never moved | still all AUTO | **3** / 5 | 1.5 widths at 10.5 s; parked right against it 26–30 s; contact CAN'T TELL |
| M-1 | 19429 L-NoCross | 8513 | alliance wall at the audience corner (the right end) | 4 at the RIGHT CELL, 1.9 / 3.1 / 4.3 / 5.3 s; all missed or bounced out. No TIP | 10.4–13.3 s along the alliance wall, stopped under the HIVE's row | no | 3 / 5 | passed about half a width from us at 12.1–12.5 s while we crossed to RIGHT_AUTO_FIX_TIP; no contact seen |
| M-3 | 20245 R-NoCross | 10255 | left start | about 3–4 at 5.4–6.4 s, into the CELL beside it (which CELL: CAN'T TELL). No LEFT TIP | 15–16.5 s to the corner | no | 3 / 5 | adjacent from 25.3 s; contact CAN'T TELL |
| M-6 | 19429 R-Solo | 8513 | alliance wall at its LOADING ZONE end | none seen | a few inches at 2.5–2.9 s | still after that | 3 / 5 | about a robot width at 11.6–13.1 s; parked alongside from 24.6 s; contact CAN'T TELL |
| M-7 | 20245 R-NoCross | 10255 | left start | about 4 at 4.7–5.6 s; then 14.2–15.0 s into the LEFT CELL: **TIP 2**, swinging 15.3–17.7 s | 16–18 s to the corner | no | 3 / 5 | 1–2 widths from 23.6 s; no contact seen |
| M-10 | 20245 R-NoCross | 10255 | left start | 3–4 at 4.3–4.7 s; then 14.1–14.8 s into the LEFT CELL: **TIP 2**, settled about 20 s | 15.5–17.2 s to beside the LEFT CELL toward the corner | no | 3 / 5 | adjacent from 26.2 s; contact CAN'T TELL |

**Where video and FTCScout disagree:**
- Fruitport Q-2 (20863) and Q-42 (16220) are scored LEAVE 3, but on video neither moved.
- Belding Q-4 gave blue an AUTO PARK it did not earn (gallery "score-q4-blue-auto-park").
- Belding M-7 gave 20245 an AUTO PARK it reached after AUTO ended (gallery "score-m7-red-20245-auto-park").

For partner behaviour, the video wins.

## The partner catalog

Five archetypes. The States partner catalog (`TeamCode/autos/states-partners.json`, PR #757) tags teams with
these labels. Each archetype is described by where it is from 0 to 30 s, because that is what decides a collision.

| Archetype | Seen (of 32) | Qualifiers (of 20) | Where it is, 0 to 30 s | Stand-in |
|---|---|---|---|---|
| **LEAVE-PARK** | **11** | 10 | Starts at or near the left start (far side, by the SCORING FLOWER). Moves at 0.5–2 s, launches nothing, and is in the corner / LOADING ZONE by 3–4.5 s. Sits there to 30 s. One (14933) stopped a tile short, behind the LEFT CELL. | `partner-leave-park` (new): leaves `LEFT_START` at 1 s, at `PARTNER_PARK` (9.8, 117.4) by about 3 s |
| **STILL** | **9** | 9 | Never moves (lights on in every Fruitport case: a choice, not a breakdown). Where it sits: **corner** just past the LOADING ZONE, 4 (20862 Q-26, 15397, 20863, 16220); **in the LOADING ZONE**, 3 (17283, 6482, 15510); scoring-table wall behind the LEFT CELL, 1 (20862 Fruitport Q-17, about 1.5 robot widths from `LEFT_AUTO_HIVE`, so not on `LEFT_START`); audience wall beside our right start, 1 (7911, likely) | `partner-still` (the corner, 15, 133.8); `partner-zone-still` (new: in the LOADING ZONE, 8.2, 110) |
| **RIGHT-SHOOT-PARK** | **9** | 1 | Right start. Fires its preloads at the RIGHT CELL, last ball out 1.0–5.3 s (median 2.5). **7 hit**, TIP 1 settling 4–10 s; **2 missed** (9133, 8513). It stays on its start 0.5–18 s after its last shot (median about 4 s; 10104 sat 13–18 s), then drives along the alliance wall or under the HIVE and parks. | `partner-r-launch-park` (`lastShotS`, `"fails": "misses"`); it leaves straight after its last shot, earlier than the real ones |
| **LEFT-SHOOT-PARK** | **3** | 0 | Left start. A first volley at 4.3–6.4 s; in 2 of 3, a second volley at 14–15 s into the LEFT CELL (TIP 2, settled 17.7–20 s). On its start to 15–18 s, then to the corner. All three were 10255. | `partner-l-tip-park`, `partner-l-launch-park` |
| **CROSSER** | **0** | 0 | Not seen as a partner. (10104 drove under the HIVE late in 3 playoff runs, inside its own half of the lane, after its shots.) | none |

**Dead on a start, the case behind most simulated collisions: 0 of 32.**
- No partner sat on `LEFT_START` all AUTO. The nearest is Fruitport Q-17's 20862, behind the LEFT CELL. On video
  our robot at `LEFT_AUTO_HIVE` stayed about 1.5 robot widths from it, while a robot on `LEFT_START` overlaps
  `LEFT_AUTO_HIVE` (13.35 in between centres against a 16 in frame).
- No partner sat on `RIGHT_START` all AUTO.
- Every still partner sat on purpose, against the alliance wall, in or by the LOADING ZONE corner, with one
  exception.

`partner-l-broken-down` and `partner-r-broken-down` stay in the requests, labelled "never seen", so they do
not drive decisions.

**What the qualifiers look like** (States has the same mix to expect):
- Half of the 20 qualifier partners drove to the LOADING ZONE corner by about 4 s.
- Nearly half never moved, almost all in or by that corner.
- One fired preloads, and it missed.
- None of the 20 made a TIP.

The playoff partners were picked as shooters.

**Where they end AUTO.** Every LEAVE-PARK partner and all but two STILL partners end AUTO in or by the LOADING
ZONE corner, and the shooters end along the alliance wall or under the HIVE. That is where our park spots are (`RIGHT_PARK` 14.5, 98.7; `LEFT_PARK` 14.5, 117.4). In 12 of the 15
Fruitport runs, our robot parked within about a robot width of the partner. So the real place our Autos meet a
partner is **the park**, not `LEFT_AUTO_HIVE`.

## Stand-ins: what to keep, add and retire

| Stand-in | Archetype | Verdict |
|---|---|---|
| `partner-leave-park` (new) | LEAVE-PARK | **added**: the commonest partner |
| `partner-zone-still` (new) | STILL, in the LOADING ZONE | **added** |
| `partner-still` | STILL, in the corner | keep |
| `partner-r-launch-park` | RIGHT-SHOOT-PARK | keep; vary with `lastShotS` (1–5 s seen) and `"fails": "misses"` |
| `partner-l-tip-park` | LEFT-SHOOT-PARK | keep |
| `partner-l-launch-park` | LEFT-SHOOT-PARK, one volley | keep (10255 in M-3) |
| `partner-l-broken-down`, `partner-r-broken-down` | dead on a start | keep, labelled "never seen" |
| `partner-park-1` | sits on `LEFT_START` until 15 s, then parks | **retire**: never seen. Leavers moved at 0.5–2 s, and `partner-leave-park` replaces it |
| `partner-park-2` | sits at (38.3, 133.8) until 15 s, parks low | **retire**: never seen (used only by the `r-solo-wall-park` draft) |
| `partner-l-stage-park` | lays its 4 along the wall, then parks | **retire**: never seen (an ask from `three-tips.md` that no partner took) |
| `partner-push-start` | motionless and angled, for the nudge drafts | **retire**: the nudge drafts were removed 19 Nov 2026 |

The retirements are listed, not done here: the requests and the drafts that still name those stand-ins change with them.

## Simulator results against the realistic set

See the section below, written from the local runs (`tools/simulate-auto/simulate_auto.py <auto> --study
--seeds 5`, `"calibration": "belding"`).
