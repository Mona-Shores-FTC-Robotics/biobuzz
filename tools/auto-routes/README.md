# Auto routes from code

`autogen.py` writes an Auto Builder `.pp` from a few lines of Python and exports it with the
Auto Builder's own exporter, so many route ideas can be tried in the simulation quickly. The `.pp`
it writes is still the source of truth: open it in the Auto Builder to see or change the route.

## The qualifier Autos

Two Autos, for the two partners we expect most in qualification. **The robot, since 5 Oct 2026 17:11
UTC: the Flat Intake** (`RobotDesign.flatIntake`, "flat intake"; the build team's option 3, "o3" in file names,
speed 50, no side walls): about 14.5 in square, a 14 in intake across the front (5 in tall, takes a piece
only on contact), the launcher near the back (the piece leaves 4 in behind the centre, 12 in up, at
75°). Alliance AUTO points over 60 runs (seeds 1–60); how many of the 60 made 3 TIPs; and G409, the runs where our robot
touched a spilled piece before it reached the tiles (must be 0). On the simulator as it stands since 6 Oct 2026
12:00 UTC (each TIP 0.58–1.12 s; pieces roll as filmed: [rolling](../../doc/rolling.md)). Links to watch them and
their logs: [the root README](../../README.md#latest).

| Partner | Our Auto (file, script) | Partner's Auto | Points | 3 TIPs | G409 | Updated (UTC) |
|---|---|---|---|---|---|---|
| Can shoot: fires its 4 preloads from the right start at once, parks | Qual-PartnerShootsRight (`qual-right-o3`, `qual_right.py`) | `partner-preloads-right` | **54.8** | **10** (PARK 13) | **0** | 6 Oct 2026 13:40 |
| Can't shoot, starts angled with its 4 POLLEN on the tiles beside it, drives straight forward to PARK | Qual-PartnerStages (`qual-stages-angled`, `qual_right.py`) | `partner-angled-park` | **51.6** | 0 (TIP 2 53, PARK 55) | **0** | 6 Oct 2026 12:55 |
| Can't shoot, against the wall with its 4 POLLEN on the tiles beside it, drives straight forward | Qual-PartnerStages (`qual-stages-wall`, `qual_right.py`) | `partner-stage19-side-park` | **47.3** | 0 (TIP 2 49, no PARK) | **0** | 6 Oct 2026 12:55 |

`DESIGN="flat intake" python3 qual_right.py 60 qual-right-o3 qual-stages-angled qual-stages-wall` exports
and simulates them. The full-width 18 in robot's `qual-right-v3` (62.8, TIP 3 in 35 of 60, G409 11 runs; 6 Oct 2026 12:55 UTC) stays as
the "what a wider intake buys"; qual.py's older Autos (`qual-partner-*`) are in [DEPRECATED.md](DEPRECATED.md).
The research routes (the side walls' G409-safe versions, `g409.py`; the shapes, `qual_shapes.py`; preloads
staged in a hook, `qual_stage.py`) and their numbers: [doc/robot-shapes-and-walls.md](../../doc/robot-shapes-and-walls.md),
"The research routes".

**Rules for both** (mentor review):

- **Every shot is straight on**: on the CELL's axis (x 57.5), within the launcher's band (ShotMapTest, both
  pieces score 5 in 6). With the launcher near the back (since 5 Oct 15:49): y 17–33 for the right CELL,
  109–125 for the left; before, 13–29 and 113–129. Pieces picked up anywhere are carried there.
- **Waiting for a TIP, face the HIVE** (its camera on it).
- **The tunnel is the road**: square through it under the HIVE (x 57.5), turning only clear of the frame.
- **Let a spill land before driving into it** (G409: "A ROBOT may not catch or deflect a SCORING ELEMENT
  released by a TIPPED HIVE unless and until that SCORING ELEMENT contacts anything else besides that
  ROBOT"). The simulator logs each touch as a `sim: G409` event, and AutoStudyTest's STUDY line ends
  with `G409 x.x (N runs)`.
- **After the last TIP, PARK** (LOADING ZONE, x 0–11, y 94–118), never instead of a TIP: no park path
  after a fire that may still be going (the endgame guard would cut the fire short).
- **The partner only fires from its start, then parks.** It can't tell whether the HIVE has tipped.

**Retuned 6 Oct 2026 for pieces rolling as filmed** (`RETUNE` in `qual_right.py`, every variant and its numbers
in the comments there). With the old route ShootsRight fell to 53.5, TIP 3 in 2 of 20: driving through a spill,
the 14 in intake takes few pieces and the chassis bats the rest 30–50 in away, and the sweep finds nothing.
Waiting longer, the webcam pickup, going round the west side and leaving the tunnel straight for the wall FLOWER
did no better or touched falling pieces. What works: TIP 3 from pieces that sit still (the wall FLOWER, then the
GARDEN), the wait before a spill timed from the TIP's start (it first lands 1.1–1.4 s after, whatever the TIP's
length), and standing a little further back while it falls (y 119). The Rigid V keeps the old sweep: its flaps
catch the spill (64.1 on it, 59.8 on the Flat Intake's route). Decisions on 60 runs: 20 couldn't tell 9 TIP 3s
from 12.

**Mentor review of the logs** (6 Oct 2026 13:40 UTC, `RETUNE` and the `-catch` STAGES in `qual_right.py`): one
smooth path into the wall FLOWER arrives 0.7 s sooner and is now the baseline (54.8, TIP 3 in 10, PARK 13; the
FLOWER load fires sooner, so TIP 3 comes more often and PARK less). Keeping the catch for the GARDEN load (one
load of 4, then the FLOWER's 4: 8 POLLEN, exactly the tipping weight) fell to 51.7, TIP 3 in 4: the catch fired as
its own load is what gives margin. Letting the GARDEN fire run instead of cutting it for PARK: 53.7, TIP 3 in 10
but no PARK. For the Stages Autos, catching TIP 1's spill at the drop zone first, standing or not, then filling
up to 4 or firing three loads: all 16 variants below the current routes (angled 48.5–49.8 against 51.6; wall
46.3–47.3 against 47.3, TIP 2 up to 54 of 60 but 3–5 s later). The study's new "intake misses per run" says why:
15 pieces a run too high, 16–25 while the intake is busy.

### The Rigid V baselines (6 Oct 2026 21:15 UTC on)

`baselines_v.py` exports `qual-right-v`, `qual-stages-angled-v` and `qual-stages-wall-v` from the body-designs
branch's best routes for the V (`park_first.py`, `guide_routes.py`: PARK first on ShootsRight, the tunnel turn at
x 55.5, the wall partner's row swept square) and runs them on the `rigid V` design. Two fixes on the way in, 60 runs
each: the routes are fitted for the drawn V's 15.24 in body (`FRONT_IN_V`; fitted for 14.5 in, the body overlapped
the far FLOWER's tube by 0.17 in at the pickup in every run; ShootsRight 69.5 → **71.2**, TIP 3 in 43 → 48); and the
wall partner's row sweep starts 2 in short of the first piece instead of 4, turning 70% of the way there (the V's
corners over the parked partner: collisions in 60, then 5, then 0 of 60; 56.0 → 55.7 → 53.3, **55.3** on the
corrected body, 15.12 in long; turning at 45–60% cost 2–5 points more). Angled partner **51.6**.

**The FLOWER extractor** (6 Oct 2026 23:05 UTC, mentor review of the logs on the CAD model: the body was driving into the
FLOWER). The V takes a FLOWER with the CAD's extractor, so `FLOWER_FACE_IN` (`qual_right.py`) puts the face 4.59 in from
the FLOWER's centre instead of 2.2 (`baselines_v.FLOWER_FACE_V`; 7.09 until 7 Oct 2026, the CAD chat's sign error): both
FLOWERs' points sit 2.39 in further out. The
simulator swings the extractor down on the approach (`RobotDesign.extractorSeatIn`, 0.5 s placeholder) and the
FLOWER gives up pieces only once it is down and seated. ShootsRight 71.2 → **72.2**, TIP 3 in 48 → 51 (G409 8 → 14
runs: the wall FLOWER's load fires sooner and more spills get touched); the Stages Autos unchanged (51.6, 55.3: they
reach the far FLOWER only when the row fails to TIP).

**Turret and transfer** (7 Oct 2026 00:20 UTC). The unified design's launcher is a turret fed through its axis by the
transfer, so the `rigid V` design is now `Launcher.TURRET` (it aims without turning the robot) with
`RobotDesign.transferFeedS` 0.5 s (a placeholder): a piece is launchable 0.5 s after the intake took it. Both cost
time on every load: ShootsRight 72.2 → **70.2** (TIP 3 in 51 → 45), angled **51.9**, wall 55.3 → **51.7** (TIP 3 in
26 → 17: its last load was already late). With the seat corrected to 4.59 in (7 Oct 01:00 UTC; the FLOWER points
2.5 in closer, the problem check's FLOWER 2.35 in wide): **69.8** / **51.9** / **51.7**. With the transfer's own feed
figure, 0.35 s, and the FLOWER visits reworked after the mentor's review (7 Oct 02:10 UTC: the extractor down from the
start of the path in and up only 6 in clear, every departure from a FLOWER straight back 10 in before turning,
`autogen.FLOWER_BACK_OUT_IN`): **70.2** / **51.9** / **51.7**, seat fire **72.3** (G409 14: backing out of the far
FLOWER crosses where TIP 2's spill lands).

**Both robots PARK** (mentor, 7 Oct 2026 16:30 UTC: "partner and us should basically always park"; the wall pairing had
the partner parked on our spot and no PARK for us). The wall partner now parks at the LOADING ZONE's top, (18, 112):
body x 9-27, y 103-121, a corner in the zone. Our row sweep passes it with its west edge just east of x 27, the row
5.5 in left of the centre line (`guide_routes.ROW_X_OFFSET`, `baselines_v.ROW_X_OFFSET_V`; on the row itself, the
sweep's start sat over the partner: collisions in 10 of 10), and after TIP 2's spill is fired we PARK at the usual
(10.5, 95 since 8 Oct 2026; 13, 87.44 then) instead of loading the GARDEN (`tail(garden="none")`; with the GARDEN the park came too late, PARK in 26
of 60, and it made TIP 3 in only 17). The fallback (the row fails to TIP 2, the far FLOWER does) parks by one path
straight down the tunnel from N_FIRE with the heading held until south of the HIVE's feet (`baselines_v.FALLBACK_PARK_CTRL`):
as a tunnel run plus a park card the guard cut the run between the feet and the lead-in turned the robot there, and
from the north no path fits between the parked partner and the west foot. 60 runs: **52.3**, TIP 2 in 54, PARK 60 of
60 for both robots, G409 9, no problems (51.7 with no PARK before).

**The launch exit on the turret axis** (7 Oct 2026 18:30 UTC, the robot-CAD chat: the launch column runs up the turret
axis at X −2.845 in; the old −4 was the earlier launcher's). `exitForwardIn` −2.845 on `rigid V`, the exit height still
the 12 in placeholder: **69.5** / **52.3** / **52.3**, seat fire **72.0**, within a point of before each way.

**Off the walls** (7 Oct 2026 17:30 UTC; mentor, on the flower-first log: "we shouldn't be riding through the wall").
The simulator never stopped a robot at a wall, and nothing said when one went through: now a corner or V tip more
than 0.5 in outside the field is a problem (`AutoSim.WALL_SLACK_IN`, "DRIVES INTO A WALL at", with the corner and the
pose in the timeline), like the HIVE frame and a FLOWER. At 10 runs it flagged all four Autos: the GARDEN point
(8.5, 9.56) put the V's tips 0.6 in through the south wall, so the GARDEN now stands off by the face, the flaps and
0.6 in (`qual_right.garden_y`, `FLAP_AHEAD_IN`; 10.96 on the V); ShootsRight's turn from the sweep into the GARDEN
swung the tips through the wall (the V reaches 13.65 in to a tip), so the sweep runs on y 12 and the approach loops
out to y about 15.5, turns there and slides south square (`tail`, `turn_after=0.15, turn_by=0.6`); and the wall
Auto's row sweep ended at face y 139, tips at 141.8, so its last step is 138 (`guide_routes.SWEEP_FACE_Y`). 60
runs, no wall problems: **72.3** / **52.7** / **53.0**, seat fire **72.7**; ShootsRight gained from the longer loop
(TIP 3 in 50 of 60, 43 before: the GARDEN's 4 now arrive more often).

**The dwell before a TIP** (7 Oct 2026 18:00 UTC, issue #167: eleven event TIPs sat 0.25–3.4 s, median about 2 s,
after the threshold POLLEN settled before the rocker moved; the simulator had none). `FieldSim.FILMED_TIP_DWELL_SECONDS`,
drawn per TIP, shorter the further past the threshold the load is (a volley: at most 0.5 s). TIP 1 comes at 5.1–5.3 s
(4.5–4.8 before: the partner's volley overshoots), TIP 2 1.2–1.8 s later than before. What that did to the routes:
- **ShootsRight**: the GARDEN is reached at 25.5 s and its shots end at 27–28.4 s, so TIP 3 comes after the buzzer
  and the unguarded "TIP 3 coming?" ending lost PARK in 4 runs of 10. The park path now follows the GARDEN fire
  directly, so the endgame guard cuts the fire for it, and it is drawn with the heading held at 270
  (`baselines_v.park_from_garden`): a path facing 90 turned the robot half round in the corner, the V's tips
  through the west wall. The guard's cut at the GARDEN still missed PARK in 6 of 60 (the drive from there, off the
  drawn path and from rest, needs 0.3 s more than the path's estimate), so `AutoKit.GUARD_MARGIN_S` is 1.0 s (0.5
  before). 60 runs: **56.7**, TIP 3 in 3 (50 on the instant TIP), PARK 60 of 60, no problems. The GARDEN leg now
  mostly loads the CELL for TELEOP (67%).
- **The GARDEN moved to x 9.5** on the V (`qual_right.GARDEN_X_IN`, `baselines_v.GARDEN_X_V`): at 8.5 its tips sat
  0.4 in through the west wall, and any yaw there was a wall hit.
- **The angled Auto** drops the GARDEN (`shape_matrix.STAGES["angled"]`, `garden: "none"` like the wall Auto): with
  TIP 2 at 20 s the GARDEN came at 26 s, too late to fire, it never made TIP 3 (0 of 60), and the guard's cut-short
  park from it turned into the west wall. PARK straight after TIP 2's spill is fired: **52.7**, PARK 52 of 60 (the
  8 are TIP 1 failures: the catch branch runs long and clips the HIVE frame, the known problem).
- **The wall Auto**: **52.7**, TIP 2 in 54, PARK 60 of 60. **Seat fire**: **72.2**, TIP 3 in 52, PARK 58 of 60: the
  one route the dwell barely touches, because it fires as the pieces come instead of driving to a spot and waiting.
`BIOBUZZ_AUTO_TIP_DWELL=0` runs a study without the dwell, for before/after.

**The event's piece physics** (7 Oct 2026 19:50 UTC, issue #168, from the same Saline clips: `doc/saline-piece-physics.md`).
A shot every 0.2 s in a volley (`RobotDesign.shotIntervalS`; 0.45 before, a guess; "spring hood, slow feed" keeps
it), the landing kick along the piece's throw within 60° (`FieldSim.FILMED_BOUNCE_SCATTER_SPREAD_RAD`, the size
refitted to 0.1: the films' 24 in at 0.5 s, most of the spill within 16 in of the alliance wall at 3 s as the
event shows), and NECTAR rolling at 1.5 in/s² (POLLEN 4.0). The fast volley is what matters: four shots in 0.6 s
put a CELL a full POLLEN past its weight, so it dwells 0.5 s, not 2–3; TIP 1 at 4.3–4.6 s, TIP 2 at 12.7 / 16.6 /
18.2 / 11.5 s. 60 runs: ShootsRight **74.0**, TIP 3 in 56 (3 on the slow volley), PARK 60 of 60; the angled Auto
back on `garden: "two"` **61.3**, TIP 3 in 24 (the GARDEN leg lost on the slow volley, it pays now; with the GARDEN
the wall Auto still turned into the west wall at the guard's cut, so it keeps `garden: "none"`): **53.3**; seat
fire **72.7**, TIP 3 in 52. A fixed launcher now out-TIPs seat fire on the right start: the turret is not what Auto
needs.

**All four robots** (8 Oct 2026; mentor: see more in one view, and realistic interaction). A study spec names the
other alliance after a `|`: `LQualsAuto,PartnerPreloadsRightHighAuto|RQualsAuto,PartnerLeftVAuto@50` runs our
L-Quals pair on red against our R-Quals pair on blue (each blue Auto is the red drawing turned half a turn, as the
generated class does for BLUE). The STUDY line adds "the other alliance N pts"; the log carries four robots
(`/Odometry/OpponentA3d`, `OpponentB3d`, drawn as ghosts in the layout); collisions are checked between any two
robots, and the human player enters NECTAR for both alliances. `AutoSim.alsoRunOpponent` is the API.

**PARK deep in the zone** (8 Oct 2026; mentor: "parks too close to the park zone"). Our PARK was (13, 87.44): only a
corner's tip 0.7 in inside the LOADING ZONE (x 0-11, y 94.3-117.9). Now (10.5, 95) (`qual.py`'s base point, the fit
adds the V's shortfall), the frame y 87.4-102.6, 8 in inside; the partners park at the zone's far end so both fit
(the zone is 23.6 in long, two robots 33): the right partner at (10.5, 116) (110 before), the wall partner at
(18, 114) (112), the angled at (11, 115) as it was; 2.4-4.4 in between the bodies. 60 runs: 74.0 / 61.0 / 53.3, unchanged within noise.

**Firing from the extractor's seat** (`seat_fire.py`, `qual_right.SEAT_FIRE`; mentor, 6 Oct: "the robot shoots while
extracting", one Auto first). At a FLOWER the robot streams shots (StreamOn) while the extractor feeds, instead of
waiting for 4 and driving to the firing spot; a TIP or 3–3.5 s ends it (not "Empty": with nothing held on arrival that
is true at once, which lost TIP 3 in every run of the first try). A frame-fixed launcher could not do it at all: to
aim it turned the seated robot off the FLOWER (34.6; hence the turret). Two endings, 60 runs on the turret robot:
- **west** (`qual-right-v-seatfire-west`): TIP 2 from the far FLOWER's seat at 11.1 s (13.0 before), then down the
  west side clear of the spill to the wall FLOWER, its 4 fired from the seat, then south and round into the GARDEN
  (bending west at once clipped the FLOWER's bracket on the way out) for its 4, fired from S_FIRE: **73.0** on the
  corrected seat (72.0 on the 7.09 seat), TIP 3 in 53 of 60 at 24.6 s, PARK 60 of 60, G409 7, no problems; **72.3**
  with the back-outs and the 0.35 s feed. Beats the baseline's 70.2.
- **catch** (`qual-right-v-seatfire-catch`): TIP 2 from the seat, then to N_FIRE to catch its spill as the baseline
  does: 62.5, TIP 3 in 22, G409 21 (it drives into the spill as it falls; waiting at the seat first lost the catch
  altogether, 55.2, TIP 3 in 0: the seat is 15 in from where the spill lands).

**Shot accuracy** (6 Oct 2026, 60 runs): `BIOBUZZ_AUTO_SPREAD` scales the launcher's shot-to-shot spread (1 = the
placeholder, 0 = none), `BIOBUZZ_AUTO_AIM_DEG` is how closely the robot must face the CELL before firing (2 by
default) and `BIOBUZZ_AUTO_FIRE_STILL=1` fires only once it is still. Today's launcher: ShootsRight 94% of 17.6
shots, the Stages Autos 89% of 13.4. Spread 0: 96% / 92%. Aim 0.5°, still, spread 0: 96% / 92–93%. So the spread
and the aim are not where the misses come from; the study line now says what is (hit the HIVE; short, long, wide;
a shot still in the air when AUTO ends counts as not scored, which is all of ShootsRight's 4% with no spread; the
Stages Autos' one miss a run is the 4th preload, fired into the CELL the 3rd just tipped, at every firing distance
tried, y 113.5–122).
Angled TIP 2: 53 of 60 today, 58 with no spread; wall 49 and 50 (its failures are the row pickup, not shots).

**Everything below is how the routes were tuned before that, kept as a record.** Those numbers are from before
6 Oct 2026 12:00 UTC: a fixed 1.0 s TIP and pieces that stopped rolling too soon, so they read high. Where two
are given, the second is "slow tiles", a what-if for the rolling friction, since dropped.

### On the Flat Intake (the baseline; was "option 3")

Moving to the Flat Intake is a smaller body (every spot where its front meets something moves 1.75 in:
`qual_right.fit`) and a 14 in intake instead of 16.2. Run 5 Oct 2026 16:20–17:11 UTC, 20 runs,
normal / slow tiles (`O3` and `STAGES` in `qual_right.py`).

**Qual-PartnerShootsRight** (`qual-right-o3`): v3 moved for the body, 64.5 / 60.8, 3 TIPs in 12 / 9,
no G409. Nothing tried beat it:

| Change from qual-right-o3 | 3 TIPs | G409 runs |
|---|---|---|
| No sweep: straight into the GARDEN (one path or two) | 6 / 4 | 0 |
| Sweep lane y 8.5 / 12 / 14–18 (v3's is y 10) | 10 / 9, 8 / 9, 7 / 7–9 | 0 |
| Wait 0 / 150 / 300 ms instead of 500 before driving into TIP 2's spill | 12 / 10, 12 / 9, 11 / 10 | 14 / 1, 1 / 0, 0 |
| Fire TIP 2's spill from y 22; a longer GARDEN fire | 12 / 10, 12 / 9 | 0 |

TIP 3 comes at about 27.5 s when it comes: the narrower intake catches less of each spill, so the
sweep through TIP 1's leftovers is what makes it, and there is no time left to add another source.

**Qual-PartnerShootsRight, after TIP 3** (mentor, 5 Oct): it went back to the GARDEN after TIP 3 had
been fired, because the right CELL is still up for a moment after the last shot. `qual-right-o3` now waits
up to 0.8 s for the TIP to start (`tip_ms`): PARK 11 / 9 (was 6 / 3), TIP 3 11 / 8 (was 12 / 9; 0.8–2.2 s
all alike), 64.8 / 61.3 points.

**Qual-PartnerStages, with a realistic partner** (mentor, 5 Oct: it can't set pieces down; its 4 POLLEN
start on the tiles touching it, G304, and it only drives forward). Two ways to stand it, both tried, each
with two first halves ("chase": TIP 1's spill north through the tunnel, then the row; "west": a lane west
of the HIVE to the row, then the far FLOWER):

| Partner | Our plan | TIP 2 | 3 TIPs | Our PARK | Points | G409 runs |
|---|---|---|---|---|---|---|
| **Angled** (B: back-right corner on the wall at x 32, aimed at the LOADING ZONE) | **chase, PARK** (`qual-stages-angled`) | 20 / 19 | 0 / 0 | 20 / 19 | **56.0 / 54.8** | 1 / 0 |
| Angled (parked at 14, 106, before it moved to 14, 109) | west, PARK | 19 / 18 | 0 / 0 | 1 / 1 | 50.3 / 49.3 | 0 |
| Angled | chase, a third load instead of PARK | 20 / 19 | 1 / 0 | 0 | 51.9 / 50.0 | 1 / 0 |
| **Against the wall** (A: at x 19, parks at y 100, on our PARK spot) | **west, no PARK** (`qual-stages-wall`) | 20 / 20 | 0 / 0 | — | **51.0 / 51.0** | 0 |
| Against the wall | chase, no PARK | 18 / 18 | 0 / 0 | — | 49.0 / 49.0 | 0 (robots touch at 11 s) |

What it took to get all 4 of the row: come at it side-on, all 4 against the intake at once. Driven into
end-on, the intake takes one while the body shoves the rest ahead (2–3 of 4). With A, the parked partner,
its row and our 14.5 in robot only just fit: we turn at the lane's top and slide west at y 118. Parking
round it took too long and the endgame guard's cut-short park drove into the HIVE frame, so with A we stay.
TIP 2 comes at about 21 s either way, too late for TIP 3 (8 more pieces by 30 s).

### Qual-PartnerShootsRight v3 (the full-width robot, before the Flat Intake)

TIP 1 (4.6 s) is the partner's 4 on the 3 NECTAR. TIP 2 (13.3 s): our preloads when the left CELL
rises, then the far FLOWER's 4. TIP 3 (about 26.5 s): TIP 2's spill, caught driving south through the
tunnel once it has landed, then TIP 1's leftovers and the GARDEN. Each step from v1
(`qual-partner-shoots-right`), 20 runs, normal / slow tiles:

| Change | Points | 3 TIPs | G409 |
|---|---|---|---|
| v1 | 72 / 74 | 16 / 18 | 5.3 / 1.6 |
| Fire TIP 2's spill from y 24, not y 28 (28 is at the edge of the shot map: 1–2 of 4 missed) | 74 / 76 | 18 / 20 | |
| Go to the GARDEN by a sweep west along y 10, intake first: TIP 1's NECTAR and POLLEN lie there (2–8 pieces at 17 s; NECTAR is 1.65 POLLEN) | 75 / 76 | 19 / 20 | |
| v2: no TIP 3 yet (the right CELL still up) after the GARDEN's shots: back to the GARDEN, look again, fire what it holds | 75.8 / 76 | 20 / 20 | 5.4 / 1.6 |
| **v3**: wait 500 ms more after TIP 2 settles, so its spill is on the tiles before we drive in | **72.5 / 71.8** | **18 / 17** | **0 / 0** |
| v3 with the conservative intake (5 Oct 14:13: 16.2 in wide, 5 in tall, on contact; every row above had the old one, 18 in and grabbing up to 3 in out) | 70.3 / 67.5 | 17 / 15 | 0.1 / 0 |
| ... and the build team's launcher (5 Oct 15:49: near the back, the piece leaving 4 in behind the centre and 12 in up instead of 4 in ahead and 17 in up) | 69.8 / 68.0 | 17 / 16 | 0.1 / 0.1 |

v2 reached TIP 2's landing 0.1–0.2 s before the last pieces did and drove into 4–5 of them each match
(G409's example C: positioning so falling pieces hit the robot "with an advantageous vector"). Waiting
for them to land costs about 3 points. v2's third load relies on §10.5: a TIP that completes in the 8 s
after AUTO still counts, so it fires up to 28.6 s.

Tried and dropped (on v2, before G409 was counted): leaving for the spill sooner (0–0.4 s after TIP 2
starts: 0–15 / 20), firing from y 16–21, a tunnel lane at x 55 or 60, a webcam pickup facing north after
the GARDEN (the leftovers lie behind the robot), and standing at the catch spot for TIP 2's spill or TIP
3's (catches no more than driving through: the pieces land 2–12 in in front of the intake and scatter
within half a second). Trials in `qual_right.py` (`TRIALS`).

**G409: waiting further back** (run 5 Oct 2026 15:13 UTC). The side-walls session measured where a
spill first lands (`SideWallSpillTest` on `claude/dazzling-maxwell-je04gu`): a plain robot on the CELL's
axis, facing the HIVE, is clear of it with its front face 35 in or less from that wall (36: 2 TIPs in
200 touch it; 38: a third). v3 waits for TIP 2 at N_FIRE, front face 36.5 in. Moved to 35 in (58.0,
115.5), with the extra wait before driving into the spill swept (`qual_right.py`, `G409`):

| v3 variant | Points | 3 TIPs | G409 per run |
|---|---|---|---|
| v3: N_FIRE y 114, wait 500 ms | 70.3 / 67.5 | 17 / 15 | 0.1 / 0 |
| N_FIRE at 35 in, wait 0 ms | 73.8 / 71.3 | 19 / 17 | 1.8 / 0 |
| ... 150 ms | 72.8 / 66.3 | 19 / 13 | 0.1 / 0 |
| ... 300 ms (`qual-right-v3-n35-300`) | 72.3 / 65.5 | 19 / 13 | **0 / 0** |
| ... 500 ms | 71.5 / 65.3 | 19 / 13 | 0 / 0 |

Waiting at 35 in lets 200 ms of the wait come out and keeps zero touches, but it trades TIP 3 on slow
tiles (13 against 15) for normal ones (19 against 17). With no wait the robot still drives into falling
pieces on its way south: standing back fixes the waiting, not the drive through.

### The builders' prototype (5 Oct 2026 CAD)

`RobotDesign.buildersPrototype()`, "builders' prototype (5 Oct CAD)": the build team's CAD read with the
pieces in it as a scale, so every number is ±15% and will move as they build. What it changes from the
design above: about 15 × 15 in including the wheels, an intake about 8 in wide (a roller between the
front wheels), and the two flywheels at the back (about 3 in behind the centre, 8 in up), still firing
forward over the robot. Not modelled yet: the pinwheel at its right-front corner that takes POLLEN out
of a FLOWER (it still takes them with its intake), and the launch angle (the spring hood's 75°).

Where it scores straight on (ShotMapTest, both pieces 5 in 6): the right CELL from y 17–29 (was
13–25), the left from y 113–125 (was 113–129). So v3's firing spots still work. Its smaller body moves
every spot where the front meets something: the start against the wall, the FLOWER, the GARDEN, and
PARK, 1.5 in each (`qual_right.right(robot="proto")`, `qual-right-v3-proto`). Run 5 Oct 2026 15:00 UTC,
20 runs, normal / slow tiles:

| Robot | Points | 3 TIPs |
|---|---|---|
| The design above (18 in, intake 16.2 in; with its launcher as on 15:49) | 69.8 / 68.0 | 17 / 16 |
| The prototype, if its intake were 13.5 in (90% of its frame) | 62.8 / 62.5 | 11 / 10 |
| **The prototype (intake 8 in)** | **57.3 / 54.8** | **6 / 3** |
| **Option 3, now the Flat Intake** (`RobotDesign.flatIntake`, about 14.5 in, intake about 14 in with funnel wheels at the front corners; launcher as the design above; `qual-right-v3-option3`, run 15:49) | **64.5 / 60.8** | **12 / 9** |

Why: a narrower intake catches less of each spill and of TIP 1's leftovers, so TIP 3 comes later
(about 29 s) or not at all, and the robot is often still busy at 30 s and misses PARK. Without the
route changes for its size it starts off the wall and its front never reaches the FLOWER. The intake's
width is worth asking the build team about before it is fixed.

The commands these Autos use (`CollectSeen`, `LaunchAll`, `IntakeFull`, `LeftCellUp`, ...) exist only in
the simulator so far; its launcher's 2 s spin-up and 0.2 s a shot are the event's best robot's (the Saline
stream, issue #168), its intake's 0.35 s a piece is unmeasured. How pieces bounce and roll after a spill lands
is checked against the event's tracked pieces (doc/saline-piece-physics.md); film a TIP of our own to check it closer.

## Open them in the Visualizer

Each link opens the latest pushed `.pp` from this branch in the Visualizer: no login, nothing to
download. It opens as a copy (the team's file is never changed from the browser); to change an
Auto, edit the `.pp` and push, or rerun its script. A push shows up within about 5 minutes. 

To watch a pair together, use the **Together** link: it opens both robots at once in the
Visualizer's multi-path mode, from the pairs in `TeamCode/autos/pairs.json`. Or click **Team Autos**
in the Visualizer's top bar, type the branch (`claude/simulator`), and pick a pair or up to 4 Autos.
**Reload latest** there fetches them again after a push, and **Copy link** shares the view.

| Auto (files) | Together | Our robot | The other robot |
|---|---|---|---|
| Qual-PartnerShootsRight (`qual-right-o3`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-ShootsRight) | [qual-right-o3](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-right-o3.pp) | partner: [partner-preloads-right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) |
| Qual-PartnerStages, angled partner (`qual-stages-angled`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Angled) | [qual-stages-angled](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-angled.pp) | partner: [partner-angled-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-angled-park.pp) |
| Qual-PartnerStages, partner against the wall (`qual-stages-wall`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-Stages-Wall) | [qual-stages-wall](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-stages-wall.pp) | partner: [partner-stage19-side-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-stage19-side-park.pp) |
| What a wider intake buys: Qual-PartnerShootsRight v3 on the full-width robot (`qual-right-v3`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Qual-PartnerShootsRight-v3) | [qual-right-v3](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/qual-right-v3.pp) | partner: [partner-preloads-right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) |

Older Autos, for earlier robots and not re-run: [DEPRECATED.md](DEPRECATED.md).

The links read `TeamCode/autos/` on `claude/simulator`. The simulator and these Autos stay on this
branch, never `master`, so the links always name it.

Also kept: `partners.py` (the reference partners every study runs
against), `snapshots.py` (pictures of the field, from `SnapshotTest`), and `helpers.py`.

```
AUTO_BUILDER_DIR=../visualizer python3 tools/auto-routes/recycle3.py
```

Each script writes its `.pp` into `TeamCode/autos/`, exports the Java next to the other generated
Autos (the simulator's, in `TeamCode/src/test/.../generated/`), and runs `AutoStudyTest`. The
`.pp` files left in `src/test/resources/auto-builder/` are test fixtures. Each current Auto's simulated log
is in [the root README's table](../../README.md#latest), made by the Simulate Auto
workflow.

## Experiments

Ideas that lost are in `experiments/`, with their `.pp` in `auto-builder/experiments/` and no
Java committed: see `experiments/README.md`.
