# Drafts

Planning Autos on master: a `.pp` here, its generated class and its request in
`TeamCode/sim-requests/`. The Infraviolet pair has Utility trials for bring-up, not
competition Autonomous entries. The Visualizer's Team Autos lists them
under "Drafts", and Save to GitHub saves back here.

## L-Infraviolet / R-Infraviolet: coordinated three-TIP draft

**One candidate: L takes the garden early; R takes both flowers.** The previous
Infraviolet assignment is [preserved in history](../BENCH.md#previous-infraviolet-assignment),
not another selectable draft. NoCross and Solo remain unchanged.

Open the candidate pair from its PR branch:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=feat/infraviolet-pair/drafts/l-infraviolet.pp,drafts/r-infraviolet.pp>.
After merging, use `master` in that link. Sloth Load the branch and select
**Utility → Drive: L-Infraviolet / Drive: R-Infraviolet**. The trials request STOP at
30 s; they are not competition Autonomous entries.

![Infraviolet routes and shooting positions](../../../doc/infraviolet-layout.svg)

| Phase | L-Infraviolet | R-Infraviolet |
|---|---|---|
| TIP 1 | Move to LEFT_LAUNCH_CATCH; **wait for LEFT CELL up**, then shoot preloads | Shoot RIGHT preloads, then collect WALL FLOWER |
| TIP 2 | Immediately cross south under HIVE; gather GARDEN | Wall lane to LEFT_LAUNCH_CLOSE; wait for LEFT up and shoot the wallflower load |
| Reload | Turn out of garden to the existing RIGHT_LAUNCH_OFFSET | At LEFT_CLOSE, confirm TIP 2 settled; collect SCORING FLOWER, then turn forward into the south crossing |
| TIP 3 | Wait for RIGHT up, contribute four from the offset, then leave | Contribute four from local RIGHT_PAIR_CLOSE |
| Park | Wall lane to far LEFT_PARK, passing outside a robot already in near park | Clear above L's offset, then wall lane to near RIGHT_PARK |

Both crossings under HIVE go south, at different stages. L's garden collection
runs during R's LEFT work. L does not wait for TIP 2 before collecting, and the
scoring flower belongs only to R. Loose pieces encountered in transit are not
credited as a substitute for either four-piece pickup. There is no robot-to-robot
position signal: CELL-up gates establish scoring state, not partner clearance.

### Reused paths and the RIGHT shooting space

The opening LEFT_CATCH path is copied from L-NoCross. R's opening wallflower path,
wallflower-to-LEFT_CLOSE curve control point and LEFT_CLOSE-to-scoring-flower path
come from R-Solo. The wallflower exit turns north over the first 12% of travel
instead of continuing the old, slower heading change. L's empty crossing uses
R-Solo's under-HIVE controls and the straight R-NoCross garden approach. R uses the
upper controls of that corridor after its new forward flower exit. These are reused
drawing elements, not a claim that the newly combined routine has run on hardware.

Shared `spots.json` is unchanged. L keeps **RIGHT_LAUNCH_OFFSET (41, 20, 68.7°)**,
which the mentor reports is reliable. R's one new shooting point is
**RIGHT_PAIR_CLOSE (59.5, 31, approximately 97.2°)**: about **30.0 in** from turret
axis to the raised target row, approximately **9.4°** off straight-on. Its shot
needs qualification; no launcher tuning is changed here.

The old far LEFT offset is removed: with L already gone, R uses the existing
LEFT_CLOSE, about 37 in and straight-on. The original RIGHT offset and RIGHT_CATCH
body footprints overlap. Moving catch right makes the stationary positions fit,
but leaves too little space for a loaded turn beside L. This candidate instead
moves R toward a closer shot, allowing its approach and exit to clear L even if L
remains at the offset. The modeled centre-line margin there is only about 1 in along the approach (about 2 in at the shot):
check actual robot dimensions, tracking error and both alliance orientations.

### Loaded motion

Keep the normal short flower/garden backing-out routines; the mentor tunes those
as separate events. L turns forward out of the garden, R turns north promptly out
of the wallflower, and R turns forward out of the scoring flower before crossing.
The long loaded under-HIVE leg faces south throughout. Compound path sections are
one continuous drive, not a stop at every control point or junction.

There is still a short backward component while R turns into the final shot,
roughly six inches projected along its heading in the drawing. It is not a long
backward crossing, but retention during that final alignment must be tested. The
path drawing does not impose a special low runtime speed there. Intake holding
power and pickup backing-out settings are unchanged.

### Deadlines and parks

| Gate | Latest accepted match time | Timeout / late arrival |
|---|---:|---|
| L: LEFT up for preloads | Before 8 s | Far park; no garden or RIGHT volley |
| R: LEFT up for wallflower volley | Before 14 s | Near park from LEFT_CLOSE |
| R: RIGHT up, still at LEFT_CLOSE | Before 16 s | Near park without going to the scoring flower |
| L: RIGHT up at offset | Before 20 s | Far park without final volley |
| R: RIGHT up at close shot | Before 22.5 s | Near park without final volley |

Timeout rows come first: arriving at or after a deadline parks even if the CELL-up
trigger is true. R checks TIP 2 while still facing the HIVE at LEFT_CLOSE, before
driving to the scoring flower. The current exporter does not support a clock-only
branch, and the flower staging heading points away from the camera target. L's
volleys and R's LEFT volley use a 1 s readiness timeout; R's final volley uses 0.5 s
to reserve its longer park. Qualify that shorter readiness window on the robot.
Once feeding starts, existing AutoKit protection allows the four-shot completion
or 3.5 s feed backstop; **one second is not the volley duration**. A late failed
pickup or stalled drive can still consume the park margin. Keep the 30 s trial stop.

### Desk checks and the next robot test

The local paired model uses 16.2 in square bodies plus the drawn protrusion,
animated pickup motion, and 20 ms samples. Nominal, slower, missed-shot and
mismatched-speed runs had no sampled overlaps. R's final approach and park also
clear L held stationary at the RIGHT offset. Driven paths clear the HIVE and
centre line; the inherited wall-touching starts overlap the larger model by 0.4 in.

**A failed L robot still sitting at its start is not protected:** R's reused LEFT
approach/scoring-flower area overlaps it. This is a coordinated pair for two
functioning robots, not a recovery Auto for a dead partner. Stop the trial when a
robot fails or loses its route. No clock or CELL-up event proves a shared area clear.

| Timing scenario | TIPs modeled | L finishes | R finishes |
|---|---:|---:|---:|
| Existing timing facts: initial 5.1 s launch, later 4 s, flower 3.7 s, garden 5 s | 2 | 24.8 s | 19.7 s |
| Faster scenario: initial 3.3 s, later 2.5 s, flower 3 s, garden 4.21 s, driving 20% faster | 3 | 21.7 s | 27.4 s |
| Faster mechanisms, L driving at 0.8× instead | 3 | 25.5 s | 27.4 s |
| Faster mechanisms, R driving at 0.8× instead | 2 | 21.9 s | 18.5 s |

The faster scenario's third TIP settles around **26.3 s**. It is a planning
scenario, not a measured paired result. Three TIPs needs all five four-piece
volleys to land. The local analysis budgets the full feeding time despite short
readiness limits, and uses the registered/measured garden time; the browser
preview can look faster because it currently truncates those durations.

Bring-up: verify L's first-tip wait, run each route empty, test the final RIGHT
positions and R's turn with L stationary, qualify the new close shot and loaded
exits, then run the full pair. Measure R seeing TIP 2 settled at LEFT_CLOSE before
**16 s** and ready to fire RIGHT before **22.5 s**; measure L ready at its offset
before **20 s**. Promote to competition OpModes only after three TIPs, both AUTO
parks and both LEAVEs are demonstrated. Details: [robot-todo §3](../../../doc/robot-todo.md).

### Same-day run order

Use this single candidate pair; the roomier catch-distance alternative was screened
out because its loaded approach took longer and its park crossed an occupied L
shooting footprint. It is not added to the Utility list.

1. Sloth Load this PR branch on both robots.
2. Select **Drive: L-Infraviolet** and **Drive: R-Infraviolet** in Utilities.
3. Place both at their normal LEFT_START / RIGHT_START marks with four preloads;
   choose the same alliance and start together. Both trials stop at 30 s.
4. Film the complete run. Check the first-tip wait, both parks, and whether the
   fourth piece survives R's last aiming turn. Stop if either robot stalls.

The full simulator initially truncated feeding at readiness timeouts; its registry
now protects an active feed and uses the robot's feed backstop/shot-count cutoff.
This correction changes simulated outcomes, not robot feed behavior. A regression
check exercises the short-timeout wallflower volley. R's missed-tip fallback now
passes below L's far park after clearing the HIVE, and its wallflower-to-LEFT
heading uses explicit quarter turns rather than an ambiguous half-turn reversal.

## R-Solo-Nudge, for a partner with no Auto

The revised draft uses the current R-Solo scoring positions and CELL-up branch, with
a short push after the WALL FLOWER pick. The partner drawing and our path are edited
together. The old 12.5 in diagonal push and its old simulation scores are superseded.

1. Partner starts nominally at **(8, 84), heading 90°**, square toward the loading
   zone, motionless for AUTO. Its 16 in drawn width touches the alliance wall and its
   front feature starts about 1.1 in outside the loading zone. Physical placement
   must use the actual robot's wall contact and envelope, not an assumed frame size.
2. Our robot curves from WALL_FLOWER_STAGING to **PARTNER_CONTACT (10.5, 67.8, 270°)**.
   It finishes turning over the first 60% and holds that heading on the final approach.
3. A deliberate **300 ms hold** lets the follower brake before pushing. The following
   straight path ends at **PARTNER_PUSH_END (10.5, 72, 270°)**: 4.2 in of our travel,
   including the initial contact gap, rather than a long diagonal shove. The drawn
   body contact model translates the partner about 3.4 in, leaving part of its body
   in the loading zone. It does not attempt to earn the partner LEAVE.
4. One continuous curve peels east and slightly south, then goes north to
   LEFT_LAUNCH_CLOSE. Its three control points **(30,68), (36,68), (26,116)** shape the
   curve; none is a robot stop. The separate PUSH_CLEAR stop is removed.
5. Continue Solo's shooting sequence, with our final park at **LEFT_PARK**, the far
   end of the zone. The partner occupies the near end. The timeout branch and spot
   parks also route around that occupied space.

The hold and short stroke are a controlled-contact draft, **not a numerical robot
speed or force cap**. The current Auto drawing has no per-path runtime speed setting;
changing its preview velocity would not enforce a gentle push on the robot. Before
an automated push, measure the contact gap, rolling resistance and actual approach
speed with the real partner. See `doc/robot-todo.md` §3. Two TIPs and both parks remain
the goal, not a newly measured result; the browser's conservative endgame preview
can skip the last volley, and its guard differs from the robot's.

The simulation request uses the locked-turret design and retains
`PartnerPushStartAuto` with `"pushable": true`. Its push-speed fraction remains a
placeholder, so simulation timing is not a measurement of contact behavior.

Open both the draft and its partner together:
<https://mona-shores-ftc-robotics.github.io/Visualizer/#team=master/drafts/r-solo-nudge.pp,partners/partner-push-start.pp>.
The first file is our editable draft; the second draws the partner placement beside it.
The simulation request retains `PartnerPushStartAuto` and `"pushable": true`.
There is no R-Solo-Nudge OpMode yet: this is a planning draft, not a qualified competition Auto.
