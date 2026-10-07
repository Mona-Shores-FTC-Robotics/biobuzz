# The Saline Preview Event, Day 2: what a real BIOBUZZ event says about the simulator

The first BIOBUZZ event with real matches: 28 teams at Saline High School, 2–3 Oct 2026 (FTC Events code
`USMISAS`). Day 2 is the 9-hour FIRST in Michigan stream at <https://www.youtube.com/watch?v=lr6iMORuxMs>
(Day 1 is <https://youtube.com/live/PxsqeKP_bp8>); the results are at
<https://ftc-events.firstinspires.org/2026/USMISAS>. Reviewed 7 Oct 2026, from the official results pages and
the stream's own captions (the commentary). **The video itself could not be opened from the review container**
(YouTube refused it as a bot after the captions were fetched), so nothing below is measured off a frame; the
list at the end says where in the stream to frame-step for the numbers the simulator still lacks.

The point of the review: [what the simulator knows, and what it guesses](what-the-simulator-knows.md) rests on
the game manual, two films of a TIP and guesses. Saline is the first check against robots nobody on the team
built.

## What the event scored

Qualification: 35 matches, 70 alliance scores (whole match).

| | Points |
|---|---|
| Lowest alliance | 14 |
| Lower quartile / median / upper quartile | 36 / 48 / 62 |
| Highest | 126 (Q9, CyBugs + Infinity Tech) |

Playoffs: six alliances, double elimination. Infinity Tech + CyBugs won the final 113–76 over Team KRASH +
Team KUDOS; their playoff scores were 128, 107 and 113.

**Rankings, the columns the simulator cares about.** FTC Events gives each team an *Auto Points* average and an
*Avg Tips* average over its five matches. The Auto column is alliance AUTO points in the team's matches (two
robots), so it is the number the simulator's `AUTO points` is meant to predict.

| Rank | Team | Avg match | Avg TIPs | Auto points |
|---|---|---|---|---|
| 1 | 13684 Infinity Tech | 105.6 | 3.80 | 28.0 |
| 2 | 10644 CyBugs | 101.6 | 3.60 | 32.0 |
| 3 | 15465 Team KRASH | 55.8 | 1.60 | 15.2 |
| 4 | 10538 Team KILTS | 64.4 | 2.20 | 22.4 |
| 8 | 10136 Frost RoboFalcons | 63.6 | 1.80 | 28.4 |
| 14 | 7305 Clague GearCats | 58.6 | 1.80 | 20.8 |
| — | median of 28 teams | 49.2 | 1.40 | 14.8 |
| — | lowest | 32.6 | 0.40 | 6.4 |

Six teams averaged 20 or more AUTO points; none averaged under 6.

**AUTO scores the commentators read out**, decoded with the manual's values (TIP 20, LEAVE 3, AUTO PARK 5, the
same ones `AutoSim` scores with). Most were read with 5–15 s of AUTO left, so they are lower bounds.

| Match | Read out | Stream | What it is |
|---|---|---|---|
| Q7 | blue 8, red 5 | 2:30:32 | a LEAVE + PARK; a PARK |
| Q9 | blue 20, red 8 | 2:42:08 | one TIP; one LEAVE + PARK |
| Q16 | blue 28 | 4:05:13 | Infinity Tech: TIP, LEAVE, PARK ("getting the tip … going to get over and park") |
| Q18 | blue 28, red 20 | 4:16:13 | a TIP each side, one robot parked |
| Q21 | blue 20, red 8 | 4:36:05 | |
| Q22 | 11–11 | 4:41:33 | LEAVE + PARK + LEAVE, each side |
| Q23 | blue 36, red 8 | 4:46:15 | TIP + both robots LEAVE + PARK |
| Q26 | red 31, blue 11 | 5:01:45 | TIP + 8 + 3 |
| Q28 | blue 16, red 0 | 5:13:27 | both robots LEAVE + PARK |
| Q29 | red 40 | 5:24:39 | one TIP and a 20-point penalty (checked on video), not two TIPs |
| Q30 | red 16, blue 8 | 5:30:31 | |
| Playoff M6 | blue 16, red 11 | 7:50:24 | |
| Final M10 | 36–36 | 8:42:29 | a TIP and two LEAVE + PARK on each side |

So at Saline a good AUTO was **one TIP plus parking, 28–36 points for the alliance**. No alliance made two TIPs
in AUTO in 45 matches; the one 40 was a TIP and a penalty. Three never came close. A typical alliance scored 8–16: robots that left and parked, or did
nothing.

## Against the simulator

The README's baseline table and `sim-results` give, for our Autos with a simulated partner, median alliance AUTO
points of **51–76** in the qualifier Autos (1.8–2.95 TIPs, the first at about 4.6 s), 56–63 for
`three-tip-adaptive`, and 80–92 for the two-robot `duo-lz` pairs. Saline's best was 36 (plus a penalty once) and its best
teams average 28–32. The gap has three parts, and they matter differently.

1. **The simulated partner is a Saline top-four robot.** The baselines' partners fire their preloads and
   TIP (`PartnerStage19SideParkAuto`, `partner-preloads-*.pp`) at 40 in/s. At Saline the median *team*
   brought 14.8 AUTO points to its alliance, and the commentary describes most partners shooting a few
   preloads that miss, driving to the LOADING ZONE, or not moving ("15301 is not moving", Q22). The
   partner that matches Saline is the one we already have, `partner-leave-park.pp` (8 points), with
   "fires its preloads first" as the optimistic case, not the default. Reporting *our robot's own TIPs*
   next to the alliance total would make the two separable in `result.json`.
2. **Shots that go in and come out.** The simulator's launcher scores about 5 shots in 6 (`launched` /
   `scored` in `result.json`), with a speed spread of 1.5 % and 0.8° guessed, and a miss there is a shot that
   never goes in, not one that comes back out. The commentary
   calls bounce-outs constantly: "bounced in and out" (Q3, 2:03:54), "two of them went in and bounced right
   back out" (Q15, 3:57:38), "all three of those are going to bounce" (Q26, 5:03:14), "both of those pollen's
   going to bounce out" (P2, 7:24:41), "that one's going to bounce right out" (P6, 7:50:17), and backspin
   that "bounced right back inside" (5:37:11) or "right back out" (5:47:48). Also "a little bit too much
   oomph" and "wide right". Nothing in the simulator loses pieces at that rate; `PLACEHOLDER_HIVE_RESTITUTION`
   (0.2) and the spread are the knobs, and both are guesses. This is the one place the stream can give a
   number without a tape measure: count in/out for one team's volleys over a few matches.
3. **Teams shooting at the wrong CELL.** "Shots on the red side, shots on the blue side, but they are
   unfortunately on that lower side of the hive" (P4, 7:34:05). Our Autos check which CELL is up
   (`HiveTipped`, `three-tip-adaptive`'s branch), so this is a point for the design, not a correction.

What the simulator got right, as far as the commentary can tell:

- **The calibration.** Frost RoboFalcons "putting three pollen up and in. They're going to get a tip" in AUTO
  (Q26, 5:01:19): a fresh CELL (3 NECTAR) tipped on the third POLLEN, as §12.3 and `HiveCalibration` say.
- **One TIP from the preloads is routine for a good robot**; the simulator's TIP 1 at 4–5 s is the same
  shape of AUTO as Infinity Tech's and CyBugs' (TIP, then park), only faster than anything seen. Nobody
  at Saline collected more pieces and TIPped again within AUTO.
- **Waiting for the TIP.** Robots held pieces until their CELL came up ("Looks going to wait for the tip",
  P4 7:34:38), and human players held NECTAR until a TIP (the rules briefing at 1:05:01), which is what
  `AutoSim.humanNectar` does 2 s after each TIP.
- **Pieces are pushed and scattered.** TELEOP scoring was mostly "low hanging fruit": pieces on the
  tiles in the middle of the field and under the HIVE, which is where the simulator's spills end up.

Things the simulator does not model that the stream shows:

- **Shooting from the far side.** Frost RoboFalcons "shooting the back side of that hive" (P2, 7:22:36;
  P6, 7:49:38). `ShotMapTest` finds no scoring spot beside the HIVE and only wedges in front of each
  opening; whether a shot from behind the frame into the raised CELL is real, or the commentator meant the
  far end of the field, needs the video.
- **Pieces leaving the field** ("pollen comes flying out of the field. Good block by our FTA", 5:14:59).
  The simulator's walls keep everything in.
- **Robots tangling** ("tied up with some of their wiring … traveling together for the next 35 seconds",
  4:17:56). Two robots cannot overlap in the simulator and never stick.
- **Defence**, everywhere in TELEOP. Irrelevant to AUTO, which is all the simulator plays.

## Where to frame-step

YouTube steps one frame at a time with `,` and `.` while paused; the stream is 60 fps at 720p or more. The
camera is fixed and wide, so a TIP's swing time is measurable (first movement to the stop, the number
`HiveTracker.Tuning.tipSeconds` still lacks) and a spill's first touch can be placed against the tile seams
(23.6 in apart) to a few inches. AUTO TIPs are the clean ones: at most two robots moving, no defence.

| Stream time | Match | What happens |
|---|---|---|
| 4:04:58 → 4:05:13 | Q16 | Infinity Tech TIPs the blue CELL in AUTO, then parks |
| 4:22:51 → 4:23:07 | Q19 | Team KUDOS TIPs the blue CELL in AUTO |
| 4:30:19 → 4:30:30 | Q20 | CyBugs TIPs the blue CELL in AUTO |
| 4:55:50 → 4:56:00 | Q25 | Infinity Tech TIPs in AUTO ("and they park, too") |
| 5:01:19 → 5:01:35 | Q26 | Frost RoboFalcons: three POLLEN, TIP (the calibration check) |
| 5:24:22 → 5:24:40 | Q29 | CyBugs' AUTO TIP, red (the 40 on the board includes a penalty) |
| 7:28:30 → 7:29:00 | Playoff M3 | Infinity Tech + CyBugs' AUTO TIP (128–41 match) |
| 8:42:29 → 8:42:45 | Final M10 | a TIP on each side in AUTO |
| 2:17:10, 2:27:07, 2:37:59, 4:17:43, 5:31:43 | Q4, Q6, Q8, Q18, Q30 | TELEOP TIPs, for more swings to average |

For the bounce-out count, Infinity Tech's volleys are the easiest to follow: Q4 (from 2:16:18), Q9 (2:41:48),
Q16 (4:04:58), Q25 (4:55:50), Q30 (5:30:10) and the playoffs from 7:28.

## What to do with this

- Give the README's baselines a Saline column: the same Autos with `partner-leave-park.pp`, and our own
  TIPs separated from the alliance total. The routes do not change; the claim does.
- Add bounce-out to the guessed table in [what-the-simulator-knows.md](what-the-simulator-knows.md), and
  measure it from the stream (a laptop job, no field needed).
- Measure the swing time from the AUTO TIPs above and put it in `HiveTracker.Tuning.tipSeconds` and the
  simulator's fit; [`spill-test.md`](spill-test.md) stays the plan for where pieces come to rest, which a
  wide camera cannot give.
- Check the "back side of the hive" shots on video before `ShotMapTest`'s wedges are taken as the only
  places to score from.
