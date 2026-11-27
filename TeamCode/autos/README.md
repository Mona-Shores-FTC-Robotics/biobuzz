# Auto sources (`.pp` files)

Every Autonomous drawn in our [Visualizer fork](https://mona-shores-ftc-robotics.github.io/Visualizer/)
has its `.pp` file committed here, in the same PR as the Java generated from it. The Java goes in
`opmodes/auto/generated/` (package `org.firstinspires.ftc.teamcode.opmodes.auto.generated`).

**New to the Visualizer?** Start with [How to plan an Auto](how-to-plan-an-auto.md).

## Local event work (Belding)

Keep this checkout on the event branch in Android Studio, then connect that same folder using
Visualizer's **Workspace** button. Open the auto and a partner there; **Save** or Ctrl+S writes
the source and generated Java into this checkout, including affected autos when team spots move.
No push, PR, CI run, or merge is needed for each local edit. You run Sloth Load yourself.

After testing a saved candidate, use Workspace's **Mark saved auto tested**. Its checkpoint
keeps the auto workspace together, including the other autos that share spots; Restore first
backs up the experiment, then restores those related files. These laptop checkpoints live under
`TeamCode/.auto-workspace/` and are ignored by Git. Commit/push successful working sources on
the event branch for a shared backup, then bring the successful changes into master through a PR.

For detailed local simulations and AdvantageScope logs, see
[`tools/simulate-auto`](../../tools/simulate-auto/README.md). Drafts need their `BuiltAuto` OpMode
prepared before selection on the Driver Station. The full Visualizer workflow is documented in
[local-workspace.md](https://github.com/Mona-Shores-FTC-Robotics/Visualizer/blob/main/docs/local-workspace.md).

## Open the Autos on master

One link each, always the latest on `master` (the Visualizer reads the file from GitHub):

| Auto | Open it |
|---|---|
| L-NoCross | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/l-no-cross.pp> |
| R-Solo | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-solo.pp> |
| R-NoCross | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-no-cross.pp> |
| L-Clockwise + R-Clockwise (a pair) | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-clockwise.pp,l-clockwise.pp> |
| R-LeftUnderHive + L-LeftUnderHive (a pair) | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-left-under-hive.pp,l-left-under-hive.pp> |
| R-RightUnderHive + L-RightUnderHive (a pair) | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/r-right-under-hive.pp,l-right-under-hive.pp> |

The Belding work order is **L-NoCross, R-NoCross, then R-Solo-Nudge**. R-Solo stays the
existing reference. The Infraviolet pair was retired on 16 Nov 2026 (mentor); its drawings and
notes are in Git history.
**R-LeftUnderHive / L-LeftUnderHive** (`r-left-under-hive.pp`, `l-left-under-hive.pp`; R-OutsideLane and L-UnderHive until
20 Nov 2026, renamed so both halves carry the pair's name, mentor) is a second pair, on the Autonomous menu since 16 Nov 2026 to be
run against the Clockwise pair (mentor; briefly taken off on 18 Nov and put back the same day: the
mentor rates it the team's best Auto). R keeps to the wall side of the HIVE both ways and L to the
under-HIVE side, so their roads do not cross. Never driven. Its volleys: R's preloads from
`RIGHT_AUTO_HIVE`, its LEFT volley from `LEFT_AUTO_OFFSET` (it comes round the outside lane, beside L on
`LEFT_AUTO_HIVE`), its last from `RIGHT_AUTO_OFFSET`; L's preloads from `LEFT_AUTO_HIVE` and its
pickup load from `RIGHT_AUTO_HIVE`. Since 19 Nov 2026 L is the former trial L-UnderHive-ParkShot (mentor):
it parks at its own `LEFT_AUTO_PARK` (14.5, 120.5, 322.6), partly in the LOADING ZONE and turned to face the
LEFT CELL, clear of R at `RIGHT_PARK`. When its RIGHT-up wait gives up at 22 s it parks there, then fires
`LaunchAllAimed` behind a check, **LEFT up before 27.5 s**: the turret aimed by `TurretAimer`, its last piece
out by 30 s, else it stays parked (19429 on 18 Nov had parked with 2 POLLEN in the lane and LEFT up). If TIP 1
has not come by 9 s, it crosses under the HIVE to `RIGHT_AUTO_FIX_TIP`, fires at RIGHT and parks at
`LEFT_AUTO_PARK` (L-Clockwise's rescue). Leave the Pickup rows as drawn (R `WALL_FLOWER`, L `GARDEN`):
with them swapped the robots touched at about 12 s in 2 of 3 simulated runs. In the simulator R's
LEFT volley ends after 15.5 s, so R parks from the left spot and the pair makes one TIP; the
robot's own time for that leg decides ([its notes](drafts/README.md#the-lane-pair-r-outsidelane-and-l-underhive),
written when it was a draft). Since 20 Nov 2026 R drives its two loaded legs backward instead of turning round to
drive them nose-first (mentor: both robots have a bottom plate): `wall lane north BACKWARD to LEFT_AUTO_OFFSET` holds
270 up the wall lane, and `BACKWARD to RIGHT_AUTO_CLOSE` holds 90 down it from the SCORING FLOWER and ends on the
spot's 63.8° (the nose-first leg ended at 61°). Backward, that leg is about 3.0 s in the simulator instead of 5.3 s, so R
first holds at the SCORING FLOWER until 18.6 s (`L has left the GARDEN`): without the hold it came down the lane as L left
the GARDEN and touched it in 3 of 20 runs (6 with L 20% slow). With it, 0 contacts, every robot parked, the same TIPs and
points as nose-first and about 0.7 more balls scored a run (20 Nov 2026, `sim-requests/r-left-under-hive.json`). In
the best case (`"scenario": "best"`) R then has time for a third TIP: 2.0 TIPs a run become 2.9 (2.95 with L 20% slow),
56 points 74-75, every robot parked and no contact.

**R-RightUnderHive / L-RightUnderHive** (`r-right-under-hive.pp`, `l-right-under-hive.pp`) is the third pair, on the
Autonomous menu since 27 Nov 2026 (mentor; a draft from 19 Nov, [its notes](drafts/README.md#the-rightunderhive-pair-r-rightunderhive-and-l-rightunderhive)).
R goes under the HIVE, always backward up and down the corridor (x 59 to 61, so it never turns there and never crosses the
centre line); L keeps to the outer lane. Never driven. Three TIPs: R's preloads, fired from `RIGHT_START` facing the
alliance wall (the turret over its right shoulder), tip RIGHT; L's preloads from `LEFT_AUTO_OFFSET` and R's GARDEN load
(the corner `GardenGather`) from R's own `R_LEFT_CATCH` (61.4, 122.9, 268.9°) tip LEFT; L's WALL FLOWER load from
`RIGHT_AUTO_OFFSET` and R's SCORING FLOWER load from R's own `R_RIGHT_CATCH` (59.2, 18.5, 95.7°) tip RIGHT again. R's two
spots are 42.4 in from the row, where the Belding table scores better than at the old `R_LEFT_FIX` (33.6 in). R parks
nose first on `RIGHT_PARK_WALL` (facing 90°); L parks on `LEFT_PARK`. The fallbacks, each behind a CELL check with a
match-clock limit that leaves its park:

- **No TIP 1 by 9.5 s (L):** L takes its preloads down the outer lane to `RIGHT_AUTO_OFFSET`, fires at RIGHT if it is
  still up, picks the WALL FLOWER and brings that load back to LEFT. R waits at `R_LEFT_CATCH` with the GARDEN load
  until LEFT is up (at most 22.3 s).
- **No TIP 2 by 21.5 s (L):** L takes its WALL FLOWER load back up the lane and fires at LEFT if it is still up.
- **R's GARDEN gather short (lane not full):** R keeps the SCORING FLOWER load at the left end and tops LEFT up with it.
- **INIT row `FLOWER load` (R):** TIP 3, as drawn; SURE TIP 2 always checks LEFT after the pick and tops it up. Pick SURE
  TIP 2 beside a partner whose shots are weak.
- **R's park from the left end** waits to 22.6 s, then goes nose first down the corridor and round the HIVE's foot, so it
  never meets L in the outer lane; L's lane paths run at x 33 to pass a parked R.

In the simulator (27 Nov 2026, `sim-requests/r-right-under-hive.json`, 10 seeds a case, corner-wheel GARDEN model): best
case, every shot in and either robot 20% slow, 3 TIPs and 76 points in every run; any one preload, GARDEN or FLOWER volley
missed, 2 TIPs and 56 (16 POLLEN left, so 2 is the ceiling); both robots parked, no contact and nothing over the centre
line in all of them. With Belding shot rates, 2 or more TIPs in 10 of 10 smooth runs; with R's preloads missed it is the
weak case. A dead L on `LEFT_START` is touched by R at `R_LEFT_CATCH` (the pair's known limit, like the other pairs').
L parks R and R parks L (the mentor's variant) scored the same with R parked about 1 s later, so it was not drawn.

**Known limit: our L dead on `LEFT_START`** (accepted for Fruitport, 19 Nov 2026). Two team spots overlap a robot
left on `LEFT_START` (62.3, 133.8): `LEFT_AUTO_HIVE` by 3 in and `SCORING_FLOWER_STAGING` by about 1.7 in, with
16.2 in frames. So R-Clockwise (its LEFT volley from `LEFT_AUTO_HIVE`) and R-LeftUnderHive (its SCORING FLOWER pick)
touch a dead L in every simulated run; no approach route avoids it. Moving `LEFT_AUTO_HIVE` about 3 in toward the
HIVE would, but it is a team spot every left-end Auto uses, so it waits until after Fruitport.

**L-Clockwise / R-Clockwise** is the coordinated pair: both robots on a one-way loop, with a fixed
fallback at each stop, and a pickup the drive teams choose in INIT. Each robot has run its half
alone; the pair has not yet run together
([its notes](clockwise.md)). Its volleys are from the
Autos' launch spots, as every Auto's are. Since 20 Nov 2026 R drives both its loaded outside lanes backward, each
after a match-clock wait (9.8 / 9.9 s); L drives nose-first with a load (driven backward its FLOWER leg gained nothing
in the simulator, [clockwise.md](clockwise.md)). The older version
with backward legs, which has time for a third TIP, was the draft pair `r-clockwise-backward.pp` and
`l-clockwise-backward.pp`, removed with the other drafts on 19 Nov 2026 (in Git history at `f2c9bb7`).
L-NoCross was L-Partner; R-NoCross was R-Partner-Tip. This lineup change only renames
those routines: their paths, waits and fallback behavior have not been tuned yet.
**When TIP 1 does not come, the drive team picks what L-NoCross does** (INIT row `if no TIP 1`,
17 Nov 2026, mentor): **CROSS AND FIX IT** (as drawn) crosses under the HIVE to
`RIGHT_AUTO_FIX_TIP` and fixes TIP 1 itself, for a partner that will not shoot at its CELL
again; **WAIT FOR PARTNER FIX** stays on `LEFT_AUTO_HIVE` and waits for the LEFT CELL until 23 s, for a
partner that fixes its own miss. Beside R-NoCross pick WAIT FOR PARTNER FIX: R-NoCross fixes a missed
TIP 1 itself, since its CELL check at `RIGHT_AUTO_HIVE` fires whenever the RIGHT CELL is still up,
and with both robots fixing it the simulator had them collide at 12.5 s and make one TIP.
**When TIP 2 has not come by its wait, R-NoCross's INIT row `if no TIP 2` picks: play it safe,
or fix it and risk contact** (17 Nov 2026, mentor). TIP 2 is the partner's. **WAIT FOR PARTNER**
(as drawn, so R-NoCross does not cross unless the drive team picks it) stays on
`RIGHT_AUTO_HIVE` watching for TIP 2 until 23 s, the latest that still fires and parks (volley
2.5 s, park 2.9 s, margin 1.0 s), fires if it comes, and parks with its load if not. **CROSS AND
FIX IT** drives the wall side to `LEFT_AUTO_OFFSET` at 20 s, fires its FLOWER load at the LEFT
CELL if it is still up, and parks on `RIGHT_PARK`: a partner that lands 7 of the 8 a TIP needs
leaves the LEFT CELL one piece short, and R's four tip it. The wait gives up at 20 s (INIT:
"waiting for TIP 2 until 20 s"), the latest that still lets the guard fire at the left end and
park (crossing 3.3 s, volley 2.5 s, park 1.8 s, margin 1.0 s). With the Belding calibration
CROSS AND FIX IT turned 36 points into 56 in 2 of 3 seeds beside L-NoCross, a fast and a typical
partner (TIP 2 at 28.6 to 29.8 s). Its park drops below a partner on `LEFT_PARK` before sliding
left onto `RIGHT_PARK`, its turn done in the first 40% of the drive so it slides in square, 2.0 in
clear of a parked partner (17 Nov 2026: swinging in while turning it touched L-NoCross at 28.8 s,
and still turning on the lower curve it passed 0.8 in from it). WAIT FOR PARTNER never touched a partner and stayed at 36: no late TIP 2 came for it to
catch. A TIP that completes after 30 s is the open question in `CLAUDE.md`; the
guard asks only that the last piece is out by 30 s with the park after it.

**Every volley after an Auto's first sits behind a CELL check** (R-Solo, R-NoCross and L-NoCross,
12 Nov), drawn as ordinary builder steps: Drive to the launch spot, **Wait for CELL up** with an
Otherwise that parks, then Launch all. Inside an Otherwise that goes on (L-NoCross's no-TIP route)
the check's Otherwise drives back and "skips the steps below" instead. Either way a CELL that is
not seen up means no volley. Two volleys at Belding went at a down CELL (Q-14, M-7). The first
volley, into the RIGHT CELL the manual starts up, has no check. Draw a new volley the same way;
the endgame guard counts such a check as its volley, so the park is kept as before. A check that
waits on the match clock stops early enough for the volley and the park after it (23 s in
L-NoCross). R-NoCross's check after the GARDEN gather ("RIGHT CELL back up?") waits until 26 s
(mentor, 21 Nov 2026; was 22), so on the Endgame row's TIPS FIRST a late GARDEN volley
still goes, its last piece out by 28.5 s, without the park. The guard never cuts an Auto's last
wait, so 26 s is also the latest that, with the CELL never up, still parks by 28.9 s (park 2.9 s,
the guard's 1.0 s margin). On PARK FIRST (TIPS FIRST is the default), the volley is refused as it starts if its last
piece out and the park would not fit by 30 s, and the robot parks. See `doc/robot-todo.md` for the robot checks.

Inside the Visualizer, the same list is **View → Team Autos: the latest from biobuzz, together…**
(pick a branch, tick up to four Autos, Reload latest). A pull request's draft opens with its branch
in the link: `#team=auto/r-solo/r-solo.pp`.

**Drafts** (`drafts/`, no competition OpMode; a draft may have an isolated test Utility) are listed in Team Autos under their own heading, and
open beside a partner stand-in the same way. Since 19 Nov 2026 `drafts/` is kept to a few drafts (mentor: a
small, manageable set); every other draft, and the stand-ins only they used, are in Git history at `f2c9bb7`
(`git checkout f2c9bb7 -- TeamCode/autos/drafts/<name>.pp` and its generated class brings one back).
Edit and **Save to GitHub and simulate…** as for any Auto: the path stays in `drafts/`, and the fast
lane takes a `.pp` in `drafts/` or `partners/` (Visualizer
[#67](https://github.com/Mona-Shores-FTC-Robotics/Visualizer/pull/67) lists the drafts).

**The polish loop.** Open the Auto from master, change it, **Save to GitHub and simulate…** under
the same name: that makes or updates its `auto/<name>` branch and pull request, the simulator runs
on it, and a mentor merges it once the robot has driven it. A change to a spot is **Save team
spots…** instead: its own pull request, and every Auto that uses the spot is re-exported in it.
Offline, **Save to repo folder…** writes the same two files (the `.pp` here and its Java in
`generated/`) straight into this checkout on the laptop; commit them on an `auto/<name>` branch
afterwards (or do Save to GitHub once online), so master has the Auto the robot ran.

**The competition loop.** The night before, with Wi-Fi: open the Visualizer once (it keeps an
offline copy of itself), open the Autos, and pick this checkout once in **File → Save to repo
folder…** (Chrome or Edge; the folder that holds `TeamCode`). At the event, no internet needed:
change the Auto, **Save to repo folder** (the browser asks once to allow the folder), then Sloth
Load from Android Studio over USB. The Files panel opens the checkout's Autos from the laptop, and
the team spots come from its `spots.json` while GitHub is out of reach. Commit the changed `.pp`
and Java on an `auto/<name>` branch when back online.

- **The `.pp` is the source; the Java is generated.** Change an Auto by opening its `.pp` in the
  Visualizer, editing it, and exporting again (Export → Export Auto (Java)). Never edit the
  generated Java by hand: the next export overwrites it.
- **The file name is the one the Java names.** A generated class says where it came from:
  `public static final String SOURCE = "hive-rush.pp";`. That file goes here, under exactly that
  name. `AutoSourcesTest` fails CI when it is missing.
- **Commit both together.** A PR that changes an Auto shows the path change as a diff of the
  `.pp`, and a reviewer can open that exact file in the Visualizer (the upload button in its top
  bar).
- **The file carries the robot's size and motion settings.** They time the preview and the park
  guard's seconds in the Java, so the same `.pp` exports the same Java on any laptop.
- **Each Auto gets a five-line OpMode** in `opmodes/auto/`, `<Class>OpMode extends BuiltAuto`
  (see `BuiltAuto`'s javadoc), which is what the Driver Station lists. The generated class itself
  is not an OpMode. Its name ends with the time the `.pp` was saved, Michigan time
  (`R-Simple 10-31|12:22AM`), from the generated class's `SAVED`: after a Sloth Load, the Driver
  Station shows which save is on the robot.

## The bench

R-Partner-Preload and R-Solo-Staging are on `autos-experiment`, with their latest
drawings, generated Java, OpModes and simulation requests preserved. They have no
files or selectable OpModes in the active lineup. See [BENCH.md](BENCH.md) for the
reasons, exact snapshot and links to reopen them. Earlier experiments remain on that
branch as well. To bring one back, promote its files in a normal PR; adding a drawing
alone does not register it on the Driver Station.

Drafts remain separate: no OpMode until we deliberately prepare one for a robot test.
The Team Autos top-level list therefore matches the Driver Station's competition Autos.

## Partner stand-ins (`partners/`)

`partners/` holds the Autos a teammate might run beside one of ours, for the simulator only: a
`.pp` each, its generated class in `opmodes/auto/generated/`, and no OpMode, so the Driver Station
never lists one. The Visualizer's Team Autos list reads the folder's own files and skips
subfolders, so none of these shows up as an Auto of ours. To open one, give the link the whole
path: `https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=TeamCode/autos/partners/partner-l-launch-park.pp`
(a branch goes in front: `#gh=master/TeamCode/autos/partners/…`). Their spots are a teammate's
(`PARTNER_START`, `PARTNER_PARK`), not the team's. `AutoSourcesTest`
accepts a `SOURCE` here as it does in the folder itself.

| Stand-in | What it does |
|---|---|
| `partner-leave-park` | **LEAVE-PARK**, the commonest partner at Belding and Fruitport (11 of 32): leaves `LEFT_START` at 1 s, launches nothing, and is at `PARTNER_PARK` (9.8, 117.4), the top of the LOADING ZONE, by about 3 s. |
| `partner-zone-still` | **STILL** in the LOADING ZONE (3 of 32): never moves, back to the alliance wall at (8.2, 110). Its frame overlaps `RIGHT_PARK`'s. |
| `partner-still` | **STILL** in the corner (4 of 32). A partner that is not going to move: it starts where we ask it to, out of the way, back on the side wall in the corner by our alliance wall at (15, 133.8), and sits there for the whole of AUTO. One spot, ours to pick, so there is no right-side twin (mentor, 3 Nov 2026). |
| `partner-r-broken-down`, `partner-l-broken-down` | A partner that meant to run an Auto and broke down: sits on the right or left start for the whole of AUTO. The "dead" runs use it. **Never seen** in 32 partner runs: their runs are labelled so, and do not drive decisions. |
| `partner-park-1`, `partner-park-2` | Sits on the left start until 15 s, then parks (two different starts and park spots). Never seen: leavers left at 0.5–2 s; `partner-leave-park` replaces them. |
| `partner-r-launch-park`, `partner-l-launch-park` | Spins up, waits for its own CELL to be up (at most 12 s), fires all four preloads at it, parks. The right CELL is up at the start, so the right one fires at once; the left one fires once our first TIP raises the left CELL. |
| `partner-l-tip-park` | The left launch-park, then the scoring FLOWER, four more at the left CELL (eight pieces: a TIP), then parks. |
| `partner-push-start` | A motionless 16 x 16 in partner at (10.254, 82), heading 70°, one corner on the wall. Drawn for the nudge drafts (removed 19 Nov 2026); `SimRequestsTest` previews it alone. |
| `partner-l-stage-park` | Starts against the wall facing the HIVE with its four POLLEN on the tiles along its left side (the simulator places them, `AutoStudyTest.alongLeftSide`, touching the frame as G304 asks), waits 2 s, drives off along the wall and parks, leaving them. |

A stand-in is drawn and exported like any Auto (Visualizer, or `node scripts/export-auto.mjs`
from a Visualizer checkout); a new one goes here with its class, and into the curation below.
What real partners did, archetype by archetype and with how often each was seen, is
`doc/partner-autos-belding-fruitport.md`: a new stand-in matches something seen there.

To change a stand-in's start position or route, open its `.pp`, edit it, then **Save to GitHub
and simulate…**. The save keeps the source in `partners/` and creates or updates its own
`TeamCode/sim-requests/<name>.json` to preview the stand-in alone. These requests are valid just
like a competition Auto's or a draft's; they do not require an OpMode or put the stand-in on the
Driver Station. Requests for our Autos still list it under `partners` to simulate it beside us.

### Which partners an Auto is simulated with

Each Auto's `TeamCode/sim-requests/<name>.json` lists its runs: the Auto alone, then each
stand-in as a partner (`"partners"`; the request format is in `SimRunTest`'s javadoc). The
"Simulate Auto" workflow runs every entry and publishes each one. **The simulator acts as our
driver:** an entry gives the one number we ask a partner, `"lastShotS"` ("when does your last
preload leave?"). That times the stand-in's launch, and sets our partner wait as the driver would
in INIT: the last shot plus the TIP lag (`partnerTipLagS` in `timing.json`, 5.5 s). Each Auto with a
partner runs it **on time**, **missing** (`"fails": "misses"`: every shot wide, no TIP, it still
parks on schedule) and **dead** (a broken-down stand-in on its start). Nothing in between: "later than
they said" and "breaks down partway" are not modelled (mentor, 3 Nov 2026). The curation, the mentor's to correct:

| Auto | Alone | On time | Missing | Dead |
|---|---|---|---|---|
| L-NoCross | yes | `partner-r-launch-park`, last shot at 2, 5, 8 and 12 s: our partner wait runs to 6, 9, 12 and 16 s (seen: 1.0–5.3 s) | the same, said 5 and 8 s; and missed, staying on its start to 10.3 or 12.3 s (`startDelayS`) | `partner-r-broken-down`, said 8 s: we wait to 12 s, then take the Otherwise branch (never seen) |
| R-NoCross | yes | `partner-l-tip-park` at 50 in/s; `partner-leave-park`; `partner-still`; `partner-zone-still` | the same | `partner-l-broken-down` (never seen) |
| R-Solo | yes | `partner-leave-park`, `partner-still`, `partner-zone-still`, `partner-l-tip-park`; `partner-park-1` (never seen) | | `partner-l-broken-down` (never seen) |
| R-Solo-Nudge (draft, `drafts/`) | yes | `partner-push-start`, pushable: pushed off the wall into the LOADING ZONE | | |

**The results table** in the workflow's job summary has a row per seed of each run: points, our
launches and the partner's (and how many went at a down CELL), the TIPs, **the partner's last shot
and our first CellUp** (the TIP lag, seed by seed), how our first wait ended, the first collision,
and who parked. For an Auto with a partner wait, a run's name ends with the second that wait runs to.

- **The two launch-park stand-ins fire from PLAY only on the right.** The right CELL is up at the
  start, so `partner-r-launch-park`'s last shot lands where `lastShotS` says. The left one waits
  for its CELL (our TIP raises it), so its last shot comes no sooner than that: the "Partner's
  last shot" column says when it did.
- **L-NoCross's dead-partner run no longer touches the partner in the simulator.** Its Otherwise
  branch crosses to `RIGHT_AUTO_FIX_TIP`, straight below the under-HIVE lane with its back 18 in
  from the wall, 2.2 in clear of a robot left on `RIGHT_START` (16 Nov 2026). Not driven yet.
- **The raw knobs are an override for studies.** An entry's `"timing"` is four knobs, each
  optional: `startDelayS` (seconds after PLAY before its first card), `speed` (its drive profile,
  as a spec writes it after `@`), `launchS` (seconds per launch volley: a four-piece launch is the
  1 s spin-up and then three of these) and `pickS` (seconds it stands at a FLOWER with its intake
  on). One given wins over what `lastShotS` derives.

**A partner we push** (`"pushable": true` on an entry): while the stand-in is not driving, our robot moves it
along with itself on contact instead of overlapping it, at `AutoSim.PUSH_SPEED_FRACTION` (half, a placeholder)
of its path's speed. Translation only: it keeps its heading, and a wall stops it. It is judged for LEAVE and
AUTO PARK where it ends, so a push off the wall into the LOADING ZONE scores both. LEAVE is ruled (Q&A Q28, 19 Oct
2026: "regardless of how it gets there (so long as no rules are violated), the state will be awarded"); PARK for
a pushed robot is not asked yet, and reads the same way. The results table adds the partner's LEAVE, how far it
went and how long we pushed.

Partner runs use `partnerSeeds` (five) so each job stays under about five minutes; the Auto alone
keeps its ten.

The thinking behind the curation, what a wait is really waiting for, the one number to ask a
partner for, and which partner states get simulated, is in `doc/partner-timing.md`.

**The Visualizer's Save & Simulate must keep `partners` and `partnerSeeds`** when it rewrites the
request (it writes the rest from its dialog): the curation lives in the request, nowhere else.

## Team spots (`spots.json`)

`spots.json` here is the team's list of named spots: poses every Auto can use, so nobody types or
places them again. They work like PathPlanner's linked waypoints. **Every Auto on master uses only
these spots**, so a reviewer reads steps and triggers, never coordinates:

| Spot | What it is |
|---|---|
| `RIGHT_START`, `LEFT_START` | The measured starts (`localization/StartPositions`). |
| `RIGHT_AUTO_HIVE`, `LEFT_AUTO_HIVE` | **Where an Auto launches**, with the start and the OFFSET spot: in front of that CELL, 40 in from its raised row, turned to aim the turret, half an inch short of the centre line on the left. It stands in front of that end's start, so it is for a robot that knows the start is empty. (`localization/AutoLaunchSpots`, 16 Nov 2026; nothing has been launched from these yet.) |
| `RIGHT_AUTO_OFFSET`, `LEFT_AUTO_OFFSET` | **Where an Auto launches** beside a robot on the HIVE spot, about 29° off square on our wall's side: on the left 44 in out and 2 in from that robot; on the right 52 in out, 4 in from a robot on `RIGHT_AUTO_FIX_TIP`. It also clears a robot left on that end's start. |
| `RIGHT_AUTO_FIX_TIP` | **Where a left-start Auto fires at the RIGHT CELL after crossing**, when the partner's TIP did not come: square to the wall, in line with the CELL, its back edge 18 in from the audience wall, so it clears any robot still on `RIGHT_START` (none may start more than 18 in deep) and stays out of the partner's lanes. 35 in from the row, where volleys have hit the lip. |
| `WALL_FLOWER_STAGING`, `SCORING_FLOWER_STAGING` | Where a Pick flower step starts (on the FLOWER's line, 18–24 in out). |
| `GARDEN_STAGING` | Where a Garden gather step starts; the gather pose itself is the robot code's (`GardenFieldPoints`). |
| `RIGHT_PARK`, `LEFT_PARK` | 14.5 in off the alliance wall, facing 0 (the intake toward the field), in the LOADING ZONE. |
| `PARTNER_STAGED_POLLEN` | Where a partner on `LEFT_START` sets its four preloads down for us: a drawing until a partner says where they stage (`partners/partner-l-stage-park` is the simulator's guess; see "Partner stand-ins"). |

The eight old launch spots (`LAUNCH_CATCH`, `LAUNCH_CLOSE`, `LAUNCH_OFFSET` and `LAUNCH_CORNER` at each end) are no
longer team spots (16 Nov 2026). A draft drawn on one keeps
it as a point of its own, where it was; older notes and match reviews that name one mean these places:
LEFT CATCH (57.5, 123.5, 273.4°), CLOSE (59.9, 117.5, 270°), OFFSET (41, 121.5, 296.4°), CORNER (30.2, 130.8, 302.6°);
RIGHT CATCH (57.5, 18, 91.9°), CLOSE (56.1, 26, 90°), OFFSET (41, 20, 68.7°), CORNER (30.2, 10.7, 54°).

**Which launch spot a volley uses** is the Auto's to work out. HIVE is preferred; a pair uses one
each; a robot coming under the HIVE shoots from HIVE, one coming round the outside lane from
OFFSET. A left-start Auto moves up from `LEFT_START` to `LEFT_AUTO_HIVE` for its first volley; a
right-start Auto moves up from `RIGHT_START` to `RIGHT_AUTO_HIVE` while it spins up and fires its
preloads there (mentor, 19 Nov 2026: from `RIGHT_START` they went long). The drivers' Square has its own twelve
spots (`localization/LaunchSpots`), which no Auto uses.

Names are side (or thing) first, then role, then variant, so a side's spots sort together. This list
is the spots several Autos share. An Auto may also have spots of its own (mentor, 17 Nov 2026): pick
*+ New spot (click the field)…* in a step's spot dropdown. It may launch from one too, as long as the
turret, locked, aims at the CELL at that end from there, the frame stays inside the field and short of the
centre line (`AutoLaunchSpots.launchSpotProblem`, which `AutoVolleySpotsTest` holds every Auto to). The
Visualizer turns a spot a Launch all fires from to face its CELL as you make or move it, so there is
nothing to do by hand. A spot earns its place here when a second Auto wants it.

- **Using one.** In the Visualizer, pick it from a step's spot dropdown (under "Team spots"), or
  select a path and press 🔗 on its end in the Path panel, then pick the spot: the end jumps to it
  and stays on it. The `.pp` keeps a copy of the pose and lists the name in `auto.teamSpots`.
- **Making one.** 🔗 on a path's end → *Make team spot*, or tick *Also a team spot* when naming a
  new spot from a field click.
- **Locked by default.** Dragging a team spot's end snaps it back. Press its lock (on the step, or
  in the Path panel) to unlock it and move it. The move is a change in that browser until saved.
  When other Autos use the spot, unlocking asks first: move it for every Auto, or give this Auto a
  new spot of its own (`RIGHT_2_START`, `LEFT_AUTO_HIVE_2`) where the old one is, and move that.
  A new `_START` spot is a start and a new `_AUTO_` spot an Auto launch spot as soon as it is
  saved; the Visualizer turns an `_AUTO_` spot to aim at its CELL as it moves.
- **Saving.** Saving the Auto you moved a spot in takes the spot with it: *Save to GitHub and
  simulate…* or a Workspace save writes the Auto, `spots.json`, its Java twin
  `localization/generated/TeamSpots.java` and every other Auto here that uses the spot,
  re-exported (`.pp` and Java), in one commit. *Save team spots…* does the same for spots alone, on
  a new `spots/…` branch with its own pull request. Once it is merged, every Auto and the robot
  code (`StartPositions`, `AutoLaunchSpots`, which read `TeamSpots`) use the new spot. Saving to
  GitHub needs the same token as Save to GitHub.
- **Never edit `spots.json` or `TeamSpots.java` by hand.** Change spots in the Visualizer, so the
  Autos that use them and the robot code change in the same commit. `TeamSpotsTest` fails while the
  two files differ. A moved Auto launch spot is still checked by `AutoLaunchSpotsTest` (aimed, its
  range, two robots to an end), and a few simulator tests name a spot's numbers.
- **Starts are team spots named `…_START`.** The builder's start buttons are the team's
  `_START` spots (`RIGHT_START` → *Right start*); make one to add a start. A start measured on the
  field is the one `localization/StartPositions` uses (every `_START` team spot is a start, read
  from `TeamSpots`): measure, then move the spot in the Visualizer. Both `RIGHT_START` and
  `LEFT_START` are measured (31 Oct 2026): back flat on the wall, the frame's left side on a seam's
  teeth. To move a start, press *Unlock … start to move it* under the start buttons, then set it in
  the START panel.
- Poses are drawn for RED, in Pedro's frame (inches, degrees), like everything in an Auto; the
  robot turns them for BLUE.

## A drive straight into a launch or a pick

Put `LaunchAll` or `PickFlower` directly after the drive to its spot, with no step between. The
robot then gets ready on the way in: it lowers the FLOWER stick over the last 18 in, and opens the
launch gate in the last few inches once it faces the HIVE and the launcher is at speed. So never
draw a separate stick step or an early fire step. A drive that turns onto the HIVE only at the stop
fires once the robot is aimed and slowing, as it did before (`LaunchCards`, `PathEndSettle`;
mentor, 4 Nov 2026). If the guard parks instead of launching, the gate closes again.

## Parking when time runs short

The endgame guard (`autokit/AutoKit.guarded`) puts a TIP before the park, and otherwise gets the
robot to PARK by 30 s (mentor, 4 Nov 2026, quals and playoffs alike):

- **First, before each step it asks: can this, and every step up to the next Launch all, be done
  with that volley's last ball out before 30 s?** It adds the steps' times (a drive's drawn time, a
  pick's or gather's typical time, a wait's "at most") and then `AutoRegistration.LAUNCH_ALL_LAST_PIECE_S`
  (2.5 s from the Launch all's start to its last ball; 4.0 until 12 Nov 2026). If yes, the robot goes, and nothing parks or
  cuts it until that volley has fired. Why: an AUTO TIP is 20 points and counts toward the
  POLLINATOR RPs; AUTO PARK is 5. AUTO PARK does count toward the SWARM RP (both robots' LEAVE +
  PARK points, AUTO and end of match, 16 or more of 26: manual TU10 Tables 10-2 and 10-3), but
  both robots leaving and both parking at the end already make 16, so our AUTO PARK decides SWARM
  only when the partner misses its LEAVE or its end-of-match park; with a dead partner SWARM is out
  of reach either way. A TIP complete before TELEOP starts is assessed as AUTO (§10.5 B), but
  §10.5 also says achievements scored in the AUTO-to-TELEOP transition "may be subject to
  penalties": an open question, not in `doc/game/qa-review.md`. So the guard asks only that the
  last ball be out by 30 s, and nothing here plans on a TIP that finishes after 30 s. The robot then ends AUTO at the launch spot, off the walls,
  so LEAVE is kept. A step it cannot time (a block whose rows run their own steps, a wait with a
  command alongside) ends the look-ahead with no.
- **No switch for a partner with no Auto.** If the partner does not move in AUTO, our AUTO PARK is
  what makes SWARM (our LEAVE 3 + AUTO PARK 5 + both end-of-match parks 10 = 18; 13 without it).
  When SWARM matters in that match, the drive team runs a short Auto that leaves and parks (a
  nudge) instead of a scoring one; when it does not, the TIP-first guard as above. There is no INIT
  option to make the guard protect the park (mentor, 4 Nov 2026).
- **If not: can I do this, then still park from where it leaves me?** If not, the robot parks now,
  from the spot it is on. A wait or a launch that has not started feeding is cut when the park needs
  the time; a drive, a pick, a gather or a feed under way never is.
- **Each spot has its own park path,** made by the Visualizer's builder: a straight line to the
  PARK spot, shared by every drive to that spot. Press **P** on a drive step to see it (everything
  else dims) and bend it with a control point. The drive just before Park needs none.

- **Practice: time the whole plan.** Panels → `BuiltAuto.endgameGuard` off (and the Driver Station's
  30 s timer off) runs every card whatever the clock says, so the match log shows how long the whole
  Auto really takes, and says once where the guard would have parked. The Driver Station shows it in
  red while it is off; it is back on after a restart. Never in a match.
- **What the log says.** `going for it, not parking` is the guard choosing a volley over the park
  (once per volley, at the first moment the park alone would have stopped it); `not starting …`,
  `parking now` and `cutting card short` are the park rule, as before.

## When the TIP does not come (a Wait's Otherwise)

A Wait step's **Otherwise** steps run only if the wait times out. Its **then** picks what comes
after them:

- **go on with the steps below**: the robot carries on with the steps under the wait, as if the
  TIP had come (L-NoCross: back to `LEFT_AUTO_HIVE`, then the rest of the plan).
- **park**: the Otherwise ends with its own Park step, and the steps under the wait run only when
  the TIP comes. In the `.pp` and the Java the wait is then the Auto's last step, each of its two
  outcomes holding the rest of its own route.

Either way, every drive in an Otherwise has a park path from its spot (P on the step), as the
main list's drives do, so a guard that parks early parks from where the robot is. A long one may
need a bend: L-NoCross's park from `RIGHT_AUTO_FIX_TIP` goes back under the Red HIVE, clear of `RIGHT_PARK`.

## Changing an Auto at the field (INIT options)

The drive team changes an Auto in INIT with **gamepad 1's D-pad**, no new export needed
(`opmodes/auto/InitOptions`): **up / down selects a row, left / right changes it.** An Auto with one
row has it selected already.

| Row | Left / Right changes | Offered by |
|---|---|---|
| The partner wait | the second the wait for the partner's TIP gives up, on the match clock, 1 s a press. The page reads "waiting for TIP 1 until 9 s": the TIP is the one the builder's *It waits for* names (an Auto that names none reads "partner's TIP"). Left to 0 does not wait | a Wait step set to *until the match clock reads …* with *The drive team can change it in INIT* ticked (one per Auto) |
| Park | the PARK spot: `LEFT_PARK` or `RIGHT_PARK` | a Park step with *Drive team may pick in INIT* ticked; no Auto on master does (below) |
| A route choice (Clockwise's Pickup, R's Priority (it follows the Endgame row, no row of its own), L-NoCross's `if no TIP 1`, R-NoCross's `if no TIP 2`) | the leg the robot drives | a *Choose in INIT* step (`clockwise.md`); in a wait's Otherwise, each leg may end with a check whose every outcome parks (L-NoCross) |

- **Nothing touched, the Auto runs as drawn.** The INIT page shows the options on one line, green
  as drawn and amber once changed; PLAY locks them, and the match log records them
  (`AutoOptions`).
- **The park: agree it in the queue, don't switch it.** Each Auto parks on its own side's spot
  (R- Autos `RIGHT_PARK`, L-NoCross `LEFT_PARK`); if a partner's Auto parks there, ask them to
  take the other. The INIT park choice exists but no Auto offers it (mentor, 3 Nov 2026): the two
  spots are 21.7 in apart on one wall, two parked robots have about 3.7 in between them, and a
  route to the other spot passes the partner sitting on ours (R-Solo's went within 3 to 8 in of
  `RIGHT_PARK`). The builder's collision check knows the field, not a partner. Offering it again
  needs each park path its own bend to the other spot, and that check. With a choice offered,
  every park path (the Park step's and each spot's, P) is driven to the picked park, the same path
  with its end moved, and the endgame guard reserves that park's time.
- **The partner wait on the match clock.** "Until 8 s" ends at 8 s into the match however late
  the robot got there, unlike "at most 7 s", which starts counting when the wait begins. The
  robot fires on the camera's `CellUp`; the number is only when it stops waiting. The drive team
  asks a partner one thing, "do you shoot your preloads right at the start?", and moves the number
  only if the answer is not yes: up to 6 s after the second they say they shoot, or down to 0 if
  they do not shoot (mentor, 12 Nov 2026; until then the driver entered the partner's last shot
  and the page showed it with the wait).
- **Where the drawn 9 s comes from:** a partner's preloads are away by about 3.5 s, and
  `AutoRegistration.PARTNER_TIP_LAG_S` (5.5 s, `partnerTipLagS` below) is the TIP lag a wait
  allows after that ([timing-terms.md](timing-terms.md)).
- Today: L-NoCross offers its wait for the partner's TIP (drawn: until 9 s). No Auto offers another
  park.

## Timing: two sets of numbers

The preview and the robot time an Auto from two places. Measured values go in both, with where
they came from.

| Numbers | Live in | Used by |
|---|---|---|
| Each command's typical seconds (`LaunchAll`, `PickFlower`, `GardenGather`…), and `LaunchAll`'s seconds to its last ball (`LAUNCH_ALL_LAST_PIECE_S`, the robot's only) | `AutoRegistration` in the robot code, copied to `TeamCode/auto-registry.json` by `AutoRegistrationTest` (`UPDATE_AUTO_REGISTRY=1`), and into each `.pp`'s `registry.typicalS` when the Visualizer loads the robot's commands | The endgame guard on the robot (a step goes if it and the steps up to the next volley's last ball fit; else a grab starts only if its typical time and the park fit), the simulator (which reads them from `AutoRegistration`), and the Visualizer's Instant preview |
| How the robot and the HIVE behave: drive speed, spin-up, a volley, time to full, a TIP's start and swing, the CELL loads | `timing.json` here (`version`, then each fact's `value`, `source` and `note`) | The Visualizer's Typical preview and its **Timing facts** dialog, which reads this file from the branch Team Autos last listed. It never changes the robot or the export |

- **A fact's `source` says how sure it is**: `measured, <robot>, <dates>`, or `estimate`. The
  `note` is one line, shown under the fact in the dialog; the longer story goes below.
- **Where the measured facts came from** (20245, the 29 Oct to 3 Nov 2026 logs on the `logs/*`
  branches; each note in the file is the one-line version):
  - `driveSpeed` 1.0: logged drive time against the export's `kit.path` seconds. 2 Nov R-Quals
    (12.76 V) ran its three drives at 0.98, 1.05 and 1.25, and R-Simple's park at 0.83. 1 Nov
    R-Simple, 19 runs of one 3.1 s path: 0.88 to 1.11, median 1.0. The 1.5 of 25 Oct was the light
    chassis.
  - `spinUpS` 1.1: 83 spin-ups from rest to launch speed (1500 to 1625 ticks/s), median 1.1 s,
    middle half 1.0 to 1.3, all 0.9 to 2.1 (slowest on a sagging battery). The first LaunchAll at
    PLAY waited 0.94 to 1.36 s for it.
  - `volleyS` 2.5 (plan, mentor 12 Nov 2026): a LaunchAll that ends on its fourth ball. In the
    Belding films the fourth ball left 2.0 to 2.5 s after the gate opened (0.6 to 0.95 s to the
    first, about 0.45 s between), and a counted volley took 2.2 to 3.5 s with its spin-up wait.
    **The robot does not do this every time yet:** when the launcher counts fewer than four shots
    the feed runs its set 3.5 s, and the endgame guard plans on 4.0 s
    ([timing-terms.md](timing-terms.md), "Still to change"). The figure before, measured with the
    set feed running out:
    4.0: LaunchAll after a drive with the launcher at speed, the settle, the wait for
    speed and `LaunchCards`' set 3.5 s feed. Every LaunchAll with the 3.5 s feed after a drive in the
    2 to 4 Nov logs (8: R-Quals 2 Nov, R-Solo 3 Nov 17:30 and 17:59, R-Solo-Staging 4 Nov 00:48):
    3.60 to 4.64 s, median 4.04. The slowest two (4.45, 4.64 s) were 4 Nov 00:48, where the gate
    opened 0.98 to 1.12 s after the drive: the settle took 0.38 to 0.57 s, and the launcher
    overshot to about 1680 ticks/s (target 1595 ±60) and was not at speed for about 0.7 s. The last
    ball left 1.7 to 2.9 s after the gate opened (the launcher's shot count; 2.2 to 2.85 s on 4 Nov
    00:48). The robot's own numbers are `AutoRegistration.LAUNCH_ALL_TYPICAL_S` (4.5, which covers
    all but those two and the first volley at PLAY, 4.45 to 4.83 s) and `LAUNCH_ALL_LAST_PIECE_S`
    (4.0 from the card's start to the last ball: 2.1 to 3.97 s after a drive). All measured before a LaunchAll right
    after a path opened its gate on the approach and the settle became "aimed and slowing" (4 Nov
    2026), which should take most of the settle off: re-measure from the next match logs before
    lowering them.
  - `cellCheckS` 0.75: the limit for a "Wait for TIP or CELL up" that only checks, on arrival at a
    launch spot, that the CELL is up. At `LEFT_LAUNCH_OFFSET` the camera saw the CELL up 0.33 and
    0.44 s after the drive ended when the approach had turned the camera away (from
    `SCORING_FLOWER_STAGING`, turning about 200°/s to the end: 3 Nov 17:59, 4 Nov 00:48), and 0.22 s
    when the approach already faced the HIVE. 0.75 covers those with room for a slower settle. The
    Visualizer does not know this fact yet (it drops unknown facts when it loads this file); it is
    here for the Visualizer's next change to the builder's wait. A wait for a partner's TIP is not a
    check, and keeps its own limit.
  - `intakeFullS` 1.9 (estimate): the last piece's current spike came a median 1.5 s after the push
    started (middle half 1.2 to 1.9; 33 picks that took pieces), and the one pick that filled the
    lane latched full 0.44 s after its last spike, 1.43 s in. No Auto waits on `IntakeFull`, so
    this fact times nothing in a preview today.
  - `pickFlowerS` 3.6 (measured, 15 Nov 2026): a `PickFlower` step, start to end, the median over
    every Auto log on `events/2026-11-belding` and `logs/*` (each run counted once). Picks: 107; the
    lane read full a median 2.6 s after the step began in the 61 that filled (1.5 to 4.7 s), and 43
    never read full. `gardenGatherS` 2.5 (19 Nov 2026): the five TeleOp button gathers of 18 Nov on
    19429 that ran to the end with today's `GardenGather` defaults took 2.26 to 2.50 s without their
    line-up, which an Auto's gather does not do (`logs/2026-11-18-19429-RC`, `_Basic_Drive_4` to `_6`).
    It was 4.4, the median of the ten Auto gathers of 3 to 14 Nov, nine with the old 1.5 s dwell. No
    Auto gather has run on today's values, so re-measure. The Visualizer's Match view has a slider for
    each, shown only for an Auto with that step; a step never runs past its own time limit. The
    robot's guard plans 4.0 and 3.0 s (`AutoRegistration.PICK_FLOWER_TYPICAL_S`,
    `GARDEN_GATHER_TYPICAL_S`).
  - `tipStartS` 2.6 and `tipSwingS` 1.6 (measured): together, the 4.2 s from a LaunchAll's end to
    the camera's `CellUp`, for a CELL at its tipping weight and a volley that ends on its fourth
    ball. From the Belding films ([timing-terms.md](timing-terms.md)): the other CELL settled a
    median 3.9 s after the last ball left and a watching camera read it 0.2 s later, 4.1 s in all;
    a counted feed ends about 0.1 s after its last ball. With the 4.0 s volley these were 1.4 and
    1.6, because a set feed runs 1.2 s past its last ball.
    - **These two go with `volleyS`.** If `volleyS` comes down because the feed ends on its fourth
      ball, raise `tipStartS` by the same amount (done 12 Nov 2026: 4.0 to 2.5, and 1.4 to 2.6): the
      last ball to `CellUp` stays about 4.1 s.
    - **They are the middle, not the limit.** An at-weight TIP took up to 6.7 s from the last ball,
      and one on nine or more pieces about 2.5 s. The preview has one value for all of them.
    - Before 11 Nov 2026 these were 1.0 and 0.7 (1.7 s together), from the two TIPs our own preload
      volleys made on 3 Nov (the camera first read TRANSITION 0.91 and 1.08 s after the LaunchAll
      ended, a reading the Belding films show comes from the robot driving off) and two hand-tipped
      swings (0.43 and 0.57 s).
  - `partnerTipLagS` 5.5 (measured): the give-up limit of a partner wait, from the partner's last
    preload leaving its launcher to our `CellUp`. From the Belding films, re-measured 11 Nov 2026
    ([timing-terms.md](timing-terms.md)): 3.57, 3.96, 4.07 and 4.25 s in the four films where a
    waiting robot watched a partner's preloads and the partner's launcher could be seen. Over all
    13 TIPs on a CELL at its tipping weight, the last ball to the CELL settled took 3.4 to 6.5 s
    (median 3.9), and a watching camera read it 0.2 s later. 5.5 s catches 11 of the 13; the 4.5 s
    used until 12 Nov 2026 caught 9; 6.7 s would catch all of them. The wait ends as soon as
    `CellUp` fires, so the longer limit costs time only when the TIP is not coming.
- **The words for these times** (launch starts, gate opens, last ball leaves, feed ends, TIP lag)
  and which moment each fact counts from are in [timing-terms.md](timing-terms.md).
- **Change `timing.json` by hand, in a PR**, or in the dialog, which keeps a change on that laptop
  until it is copied in. Unknown facts are dropped when it loads.
- **`partnerTipLagS` is also the robot's** (`AutoRegistration.PARTNER_TIP_LAG_S`: the partner wait
  ends at the partner's last shot plus it). Change both in one PR; `PartnerWaitTest` fails while
  they differ, and catches a copy from the dialog that dropped it.
- **After changing a typical time in `AutoRegistration`**, update `auto-registry.json` (the test
  above) and load the robot's commands into every `.pp` here, then export again: the Java does not
  change, the `.pp`'s copy does.

## Opening a committed Auto from a link

Once a `.pp` is pushed, anyone with a laptop and a browser can open it, no login needed:

| Link | Opens |
|---|---|
| `https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=hive-rush.pp` | the latest `hive-rush.pp` on `master` |
| `https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=a1b2c3d/hive-rush.pp` | exactly that commit, forever |
| `https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/some-branch/hive-rush.pp` | the latest on a branch, e.g. a PR's |

- **In a PR that changes an Auto,** link the branch form so reviewers see the path, not just the Java.
- **For "this is the Auto we ran"** (a match, a meeting note), use the commit form: it never changes.
- A branch can show the previous push for up to 5 minutes; a commit is always exact.
- It opens as a copy with a "From biobuzz …" bar. The viewer's own work is set aside, not saved
  over. To change the Auto, edit the `.pp` here and push, as above.

The file format, including the `auto` section, is described in the Visualizer's
[`docs/auto-format.md`](https://github.com/Mona-Shores-FTC-Robotics/Visualizer/blob/main/docs/auto-format.md).

## Why here, and what was left out

- **`TeamCode/autos/`, not next to the Java in `src/main/java`.** Outside the source tree nothing
  in Gradle, lint or the APK ever sees these files. Next to the Java would pair them visually but
  depends on the Android build ignoring non-Java files there.
- **Not checked: that the Java still matches its `.pp`.** Regenerating in CI would need Node and
  the Visualizer repo in this build. To check by hand, from a Visualizer checkout:
  `node scripts/export-auto.mjs <path>/TeamCode/autos/hive-rush.pp /tmp/out`, then compare with
  the committed class.
- **`FieldFrameTest` skips `opmodes/auto/generated/`.** Generated Autos contain `mirrorAroundPoint(70.75, 70.75)`
  (the half turn to the other alliance) and wall coordinates such as 141.5; they come from the Visualizer's own 141.5 in field, not
  from someone typing a field size.
- **Short links need the repo public.** They read the file from GitHub without a login; if
  biobuzz is made private, they stop working for everyone.

Decided in #118.
