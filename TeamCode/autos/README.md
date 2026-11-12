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
| L-Infraviolet + R-Infraviolet (a pair) | <https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/l-infraviolet.pp,r-infraviolet.pp> |

The Belding work order is **L-NoCross, R-NoCross, then R-Solo-Nudge**. R-Solo stays the
existing reference. **L-Infraviolet / R-Infraviolet** is a coordinated three-TIP pair, run on
both robots and promoted to competition Autos; run one only with the partner on the other
([its notes](infraviolet.md)).
L-NoCross was L-Partner; R-NoCross was R-Partner-Tip. This lineup change only renames
those routines: their paths, waits and fallback behavior have not been tuned yet.
In particular, L-NoCross's existing no-TIP fallback still crosses to RIGHT; replace
that in the Visualizer before qualifying it as a true no-cross routine. R-NoCross's
CELL-up timeout also still proceeds to launch. Since 10 Nov L-NoCross's no-TIP route checks
the CELL before each volley ("TIP seen?" at RIGHT_LAUNCH_CLOSE, "RIGHT CELL still up?" after the
WALL FLOWER pick), so it no longer fires at a down CELL. See `doc/robot-todo.md` for the checks.

Inside the Visualizer, the same list is **View → Team Autos: the latest from biobuzz, together…**
(pick a branch, tick up to four Autos, Reload latest). A pull request's draft opens with its branch
in the link: `#team=auto/r-solo/r-solo.pp`.

**Drafts** (`drafts/`, no competition OpMode; a draft may have an isolated test Utility) are listed in Team Autos under their own heading, and
open beside a partner stand-in the same way: R-Solo-Nudge with its pushed partner is
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-solo-nudge.pp,partners/partner-push-start.pp>.
Edit and **Save to GitHub and simulate…** as for any Auto: the path stays in `drafts/`, and the fast
lane takes a `.pp` in `drafts/` or `partners/` (Visualizer
[#67](https://github.com/Mona-Shores-FTC-Robotics/Visualizer/pull/67) lists the drafts).
R-Two-Tip, R-Solo's steps with the LEFT volleys from the measured launch spots, is a draft too
([its notes](drafts/README.md)).

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
| `partner-still` | A partner that is not going to move: it starts where we ask it to, out of the way, back on the side wall in the corner by our alliance wall at (15, 133.8), and sits there for the whole of AUTO. One spot, ours to pick, so there is no right-side twin (mentor, 3 Nov 2026). |
| `partner-r-broken-down`, `partner-l-broken-down` | A partner that meant to run an Auto and broke down: sits on the right or left start for the whole of AUTO. The "dead" runs use it. |
| `partner-park-1`, `partner-park-2` | Sits on the left start until 15 s, then parks (two different starts and park spots). |
| `partner-r-launch-park`, `partner-l-launch-park` | Spins up, waits for its own CELL to be up (at most 12 s), fires all four preloads at it, parks. The right CELL is up at the start, so the right one fires at once; the left one fires once our first TIP raises the left CELL. |
| `partner-r-launch-stay` | The right launch-park stand-in's spin-up, CELL wait, and preload volley, then stays at its start instead of driving to park. Select it locally to compare shoot-and-stay behavior. |
| `partner-l-tip-park` | The left launch-park, then the scoring FLOWER, four more at the left CELL (eight pieces: a TIP), then parks. |
| `partner-push-start` | A motionless 16 x 16 in partner at (10.254, 82), heading 70°, one corner on the wall. Matches the angled `drafts/r-solo-nudge` and the isolated `drafts/partner-nudge-test`. The Utility applies real speed limits; the full draft's push simulation is still a placeholder. |
| `partner-l-stage-park` | Starts against the wall facing the HIVE with its four POLLEN on the tiles along its left side (the simulator places them, `AutoStudyTest.alongLeftSide`, touching the frame as G304 asks), waits 2 s, drives off along the wall and parks, leaving them. |

A stand-in is drawn and exported like any Auto (Visualizer, or `node scripts/export-auto.mjs`
from a Visualizer checkout); a new one goes here with its class, and into the curation below.

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
in INIT: the last shot plus the TIP lag (`partnerTipLagS` in `timing.json`, 4.5 s). Each Auto with a
partner runs it **on time**, **missing** (`"fails": "misses"`: every shot wide, no TIP, it still
parks on schedule) and **dead** (a broken-down stand-in on its start). Nothing in between: "later than
they said" and "breaks down partway" are not modelled (mentor, 3 Nov 2026). The curation, the mentor's to correct:

| Auto | Alone | On time | Missing | Dead |
|---|---|---|---|---|
| L-NoCross | yes | `partner-r-launch-park`, last shot at 2, 5, 8 and 12 s: our partner wait runs to 6, 9, 12 and 16 s | the same, said 8 s | `partner-r-broken-down`, said 8 s: we wait to 12 s, then take the Otherwise branch |
| R-NoCross | yes | `partner-l-tip-park` at 50 in/s; `partner-still` (out of the way) | the same | `partner-l-broken-down` |
| R-Solo | yes | `partner-park-1` (no launches), `partner-still` (out of the way) | | `partner-l-broken-down` |
| R-Solo-Nudge (draft, `drafts/`) | yes | `partner-push-start`, pushable: pushed off the wall into the LOADING ZONE | | |

**The results table** in the workflow's job summary has a row per seed of each run: points, our
launches and the partner's (and how many went at a down CELL), the TIPs, **the partner's last shot
and our first CellUp** (the TIP lag, seed by seed), how our first wait ended, the first collision,
and who parked. For an Auto with a partner wait, a run's name ends with the second that wait runs to.

- **The two launch-park stand-ins fire from PLAY only on the right.** The right CELL is up at the
  start, so `partner-r-launch-park`'s last shot lands where `lastShotS` says. The left one waits
  for its CELL (our TIP raises it), so its last shot comes no sooner than that: the "Partner's
  last shot" column says when it did.
- **L-NoCross's dead-partner run collides.** Its Otherwise branch drives to `RIGHT_LAUNCH_CATCH`,
  10 in in front of `RIGHT_START`, where a dead partner sits; the mentor kept that spot for now
  (3 Nov 2026), and a "Right CELL up?" check before that launch is shelved until the basics work.
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
| `RIGHT_LAUNCH_CATCH`, `LEFT_LAUNCH_CATCH` | In front of that CELL, just outside where its spill lands, clear of a partner on the start. |
| `RIGHT_LAUNCH_OFFSET`, `LEFT_LAUNCH_OFFSET` | Off to the side of that CELL and angled at it, so a partner in front of the CELL is not disturbed. |
| `RIGHT_LAUNCH_CORNER`, `LEFT_LAUNCH_CORNER` | The long shot (about 55 in) from the corner of our wall, drawn for the TeleOp's go-to-launch-spot button, which stopped using them on 11 Nov 2026 (its spots are now `localization/LaunchSpots`' own, not team spots); no Auto uses them. RIGHT is where 20245 scored it on 4 Nov; LEFT is its place mirrored across the axle line, turned to aim the turret (which sits off-centre) at the LEFT CELL; not yet fired from. |
| `WALL_FLOWER_STAGING`, `SCORING_FLOWER_STAGING` | Where a Pick flower step starts (on the FLOWER's line, 18–24 in out). |
| `GARDEN_STAGING` | Where a Garden gather step starts; the gather pose itself is the robot code's (`GardenFieldPoints`). |
| `RIGHT_PARK`, `LEFT_PARK` | 14.5 in off the alliance wall, facing 0 (the intake toward the field), in the LOADING ZONE. |
| `PARTNER_STAGED_POLLEN` | Where a partner on `LEFT_START` sets its four preloads down for us: a drawing until a partner says where they stage (`partners/partner-l-stage-park` is the simulator's guess; see "Partner stand-ins"). |

Names are side (or thing) first, then role, then variant, so a side's spots sort together. An Auto
that needs a spot not in this list is drawn on the `autos-experiment` branch until the spot earns
its place here.

- **Using one.** In the Visualizer, pick it from a step's spot dropdown (under "Team spots"), or
  select a path and press 🔗 on its end in the Path panel, then pick the spot: the end jumps to it
  and stays on it. The `.pp` keeps a copy of the pose and lists the name in `auto.teamSpots`.
- **Making one.** 🔗 on a path's end → *Make team spot*, or tick *Also a team spot* when naming a
  new spot from a field click.
- **Locked by default.** Dragging a team spot's end snaps it back. Press its lock (on the step, or
  in the Path panel) to unlock it and move it. The move is a change in that browser until saved.
- **Saving.** *Save team spots…* writes `spots.json` and re-exports every Auto here that uses a
  changed spot, `.pp` and Java, in one commit on a new `spots/…` branch, then opens a pull
  request. Once it is merged, every Auto and the robot code use the new spot. Saving needs the
  same GitHub token as Save to GitHub.
- **Never edit `spots.json` by hand.** Change spots in the Visualizer, so the Autos that use them
  are re-exported in the same commit. A hand edit changes no Auto until each is opened and
  exported again.
- **Starts are team spots named `…_START`.** The builder's start buttons are the team's
  `_START` spots (`RIGHT_START` → *Right start*); make one to add a start. A start measured on the
  field is also in `localization/StartPositions` (`TEAM_SPOTS` names its spot), and
  `TeamSpotsTest` fails while the two differ: measure, then change both. Both `RIGHT_START` and
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
  (4.0 s from the Launch all's start to its last ball). If yes, the robot goes, and nothing parks or
  cuts it until that volley has fired. Why: an AUTO TIP is 20 points and counts toward the
  POLLINATOR RPs; AUTO PARK is 5. AUTO PARK does count toward the SWARM RP (both robots' LEAVE +
  PARK points, AUTO and end of match, 16 or more of 26: manual TU03 Tables 10-2 and 10-3), but
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
  TIP had come (L-NoCross: back to `LEFT_LAUNCH_CATCH`, then the rest of the plan).
- **park**: the Otherwise ends with its own Park step, and the steps under the wait run only when
  the TIP comes. In the `.pp` and the Java the wait is then the Auto's last step, each of its two
  outcomes holding the rest of its own route.

Either way, every drive in an Otherwise has a park path from its spot (P on the step), as the
main list's drives do, so a guard that parks early parks from where the robot is. A long one may
need a bend: L-NoCross's park from `RIGHT_LAUNCH_CATCH` bends west of the Red HIVE.

## Changing an Auto at the field (INIT options)

A partner tells us in the queue when they shoot. The drive team enters it in INIT with
**gamepad 1's D-pad**, no new export needed (`opmodes/auto/InitOptions`):

| D-pad | Changes | Offered by |
|---|---|---|
| Left / Right | the PARK spot: `LEFT_PARK` or `RIGHT_PARK` | a Park step with *Drive team may pick in INIT* ticked; no Auto on master does (below) |
| Up / Down | the partner's last shot (their answer to "when does your last preload leave?"), 1 s a press; our partner wait gives up the TIP lag after it, on the match clock | a Wait step set to *until the match clock reads …* with *The drive team can change it in INIT* ticked (one per Auto) |

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
  driver enters the partner's last shot and the wait ends `AutoRegistration.PARTNER_TIP_LAG_S`
  (4.5 s, `partnerTipLagS` below) after it: the shot's flight, the rocker's creep and swing, and the camera
  settling: 3.6 to 4.3 s in four films at the Belding Qualifier, longer in others ([timing-terms.md](timing-terms.md)). The page shows both numbers, never only the sum:
  "partner's last shot ▲▼ 8 s, waiting until 12.5 s" (mentor, 3 Nov 2026).
- Today: L-NoCross offers the partner's last shot (drawn: wait until 8.5 s, which reads as a last
  shot at 4 s). No Auto offers another park.

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
  - `volleyS` 4.0: LaunchAll after a drive with the launcher at speed, the settle, the wait for
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
    lane latched full 0.44 s after its last spike, 1.43 s in.
  - `tipStartS` 1.4 and `tipSwingS` 1.6 (measured): together, the 3.0 s from a LaunchAll's end to
    the camera's `CellUp`, for a CELL at its tipping weight and a feed that ran its set 3.5 s. From
    the Belding films ([timing-terms.md](timing-terms.md)): the other CELL settled a median 3.9 s
    after the last ball left and a watching camera read it 0.2 s later, 4.1 s in all; a set feed
    ends about 1.2 s after its last ball. The split puts the swing plainly under way 1.4 s after
    the feed ends and the CELL read up 1.6 s after that.
    - **These two go with `volleyS`.** If `volleyS` comes down because the feed ends on its fourth
      ball, raise `tipStartS` by the same amount: the last ball to `CellUp` stays about 4.1 s.
    - **They are the middle, not the limit.** An at-weight TIP took up to 6.7 s from the last ball,
      and one on nine or more pieces about 2.5 s. The preview has one value for all of them.
    - Before 11 Nov 2026 these were 1.0 and 0.7 (1.7 s together), from the two TIPs our own preload
      volleys made on 3 Nov (the camera first read TRANSITION 0.91 and 1.08 s after the LaunchAll
      ended, a reading the Belding films show comes from the robot driving off) and two hand-tipped
      swings (0.43 and 0.57 s).
  - `partnerTipLagS` 4.5 (measured): from the partner's last preload leaving its launcher to our
    `CellUp`. Re-measured from the Belding films on 11 Nov 2026 ([timing-terms.md](timing-terms.md)):
    3.57, 3.96, 4.07 and 4.25 s in the four films where a waiting robot watched a partner's
    preloads and the partner's launcher could be seen. Over all 13 TIPs on a CELL at its tipping
    weight, the last ball to the CELL settled took 3.4 to 6.5 s (median 3.9), and a watching camera
    read it 0.2 s later: 4.5 s catches 9 of the 13, 5.2 s catches 11, 6.7 s all of them. The partner
    wait ends this long after the last shot the driver enters. The 3 Nov simulations' 0.7 to 1.2 s
    (four preloads on the starting NECTAR) were short.
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
