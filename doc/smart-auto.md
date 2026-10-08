# Smart Auto: one Autonomous that checks the robot and picks its own plan

**Status: design, 9 Oct 2026.** From a conversation with the mentor; nothing here is built yet. The decisions it
records are marked **Decided**; the rest is the proposal, for the mentor and students to change.

## The idea

The drive team should not have to make decisions at the field. So there is **one Autonomous**, *Smart Auto*. During
INIT it checks every system it depends on, works out what it can still do, shows that on one Driver Station screen,
and lets the drive team override anything with one button. With a healthy robot the drive team makes one choice, the
partner, then reads two lines and presses PLAY.

What can still go wrong is a person's choice: which partner was scouted, where the robot is placed, and whether to
trust a camera that is talking but wrong. The screen makes all three visible.

**Decided (mentor, 9 Oct 2026):**
- One OpMode chooses between a full plan and a backup plan by itself, and flags anything it can't decide.
- The backup runs on time alone: no camera, no turret (the robot turns to aim). 3 TIPs + PARK would be ideal;
  2 TIPs + PARK is acceptable. It counts only on TIPs it makes itself; a partner's shots are a bonus.
- The drive team picks what the partner does, from scouting: **Launch & Park** or **Just Park**. The partner starts
  on the other start from ours, so where we are placed says where it is. Until one is picked: NOT READY, and the
  robot light flashes white.
- The camera can be disabled by hand when it is connected but its vision is wrong.
- The turret's angle comes from an absolute encoder read through an OctoQuad (workstream #81).
- The flywheel is not checked during INIT (spinning it up before PLAY isn't allowed). A failsafe for a broken
  flywheel encoder is for later.

## The plans

| Plan | When | What it does | Expect |
|---|---|---|---|
| **SMART** | The camera sees the HIVE and where the robot is | The route for our start and the partner (below). Waits react to the HIVE, as today | 3 TIPs + PARK |
| **BACKUP** | The camera is broken or disabled, or someone forced it | Backup-L or Backup-R: every wait is a timer, aiming by turning the robot, the TIPs made by us alone | 2 TIPs + PARK, a 3rd if a partner's shots land |
| **PARK ONLY** | The Pinpoint is missing or not ready | From the known start (the camera's start check, or ◀ ▶) into the LOADING ZONE, by the drive wheels' encoders if they are wired; else a timed drive | PARK; LEAVE only if neither proves reliable |
| **NOT READY** | No partner or alliance picked, no side (with no camera), or the robot is far off its start | Won't move at PLAY; the screen says what to do | — |

All the qualifier routes aim by turning the robot (they are measured on "rigid V, fixed turret"), so **a broken
turret never changes the plan**: with no trustworthy turret angle the turret is held forward and the routes run
unchanged.

The two baseline routes follow the baseline rule in `doc/unified-design.md`: no TIP relies on a spill. L-Quals already
does; R-Quals today catches TIP 1's spill for TIP 2 and needs a spill-free version (open item 3).

## The partner

The one choice a person makes before every match, from scouting: what the partner does (mentor, 9 Oct 2026: "Just
Park or Launch & Park for now ... if we are right, we know they are starting in the [other] start spot"). A toggles
it; there is no default, so until it is picked the top line says NOT READY, the Partner row is red and the
light flashes white. The camera says which start we are on; with no camera, D-pad ◀ ▶ say it.

| We start | Launch & Park | Just Park |
|---|---|---|
| **Right** | R-Quals | R-Quals: TIP 1 is ours at once anyway |
| **Left** | L-Quals: waits for the partner's TIP 1, makes it itself by 9.2 s if it never comes | **Not a plan: NOT READY.** A Just Park partner gives no TIP 1, so we start on the right and make it ourselves (mentor, 9 Oct 2026). On the left, the robot is on the wrong start or the partner was mis-picked |

**Each behaviour has its own colour** (mentor, 9 Oct 2026: "a separate color ... for each specific one? so the kids get
used to it"): Launch & Park in **violet**, Just Park in **cyan**, on the Driver Station, the scouting sheet and any card
the drive coach holds. Never an alliance colour (red, blue) or a status colour (green, amber, red); cyan is the nearest
to blue, so it is only ever used next to the word "Partner", never on the robot light. A new behaviour gets a new
colour from the same rule.

In BACKUP the partner changes nothing: Backup-L or Backup-R counts only on TIPs it makes itself. Scouting still decides details the two words hide: a Just Park partner
that waits at the standard left start is the case `doc/r-quals-partner-timing.md` covers. A partner-specific Auto (one
built for a scouted team) is a new entry in the list.

## The checks (INIT, nothing moves)

The rules allow INIT to hold motors and servos still (R103.B), and the robot must be motionless once INIT is done
(G304.H), so every check reads; none moves anything.

| Row | Healthy | Not healthy | How it's read |
|---|---|---|---|
| **Camera** | ON: sees HIVE tags | BROKEN: not connected, or no HIVE tag for 3 s. DISABLED: by a person. ⚠ SUSPECT: tags seen, but the fixes match no start (off by more than a few inches, or scattered) | `robot.vision.isConnected()`, the `CameraBlind` test, `StartCheck` |
| **Start Pose** | Left or right, seen by the camera, within 1 in: "in position" | No camera: picked by ◀ ▶, ⚠ "no camera to check it". A ◀ ▶ pick the camera disagrees with: ✖, NOT READY. 1-3 in off: ⚠ "nudge it". Over 3 in: ✖ "reposition", NOT READY. Much further (about 12 in, or scattered fixes) the camera itself is suspect. The tolerances are to be set from a field test | `StartCheck` against both candidate starts |
| **Alliance** | From the camera (today's `MatchSetup`), the word in its own colour | From X / B | Unchanged |
| **Pinpoint** | Ready | Not found or not ready → PARK ONLY | The localizer's status |
| **Turret** | The absolute encoder reads home: the starting configuration puts the turret at a known angle, so its reading is a check of the encoder itself | No signal: ✖, held forward, the robot aims. Not home (say 12°): ⚠, either it was left turned or the encoder slipped; a person turns it home, and if it still reads off, re-zeroes the encoder | `turret.health()` (asked of the turret chat: it must read the home angle in INIT, without moving) |
| **Battery** | 13.0 V or more | ⚠ below 13.0 V, "swap if there is time"; ✖ below 12.5 V, "swap it". A warning only; the thresholds are to be measured where shots start falling short | The hub's voltage sensor |

**A suspect camera is never disabled by the robot.** It turns the row amber and suggests the button; a person
decides. **Disabled means fully off**: no start check or alliance proposal, no TIP detection, and no tag fixes into Pedro's
filter. A camera that is wrong but trusted would drag the pose, and every path after it.

## The decision, in order

```
Pinpoint not ready                  → PARK ONLY
camera ON and sees where we are     → SMART  (unless Y forced BACKUP)
otherwise                           → BACKUP
partner, alliance or side unknown   → NOT READY until a button picks it
the route                           → from our start and the partner (the table above)
turret not healthy                  → held forward (every plan already aims by turning)
```

It is one pure function, `plan = decide(health, overrides)`: no hardware, unit-tested at a laptop with a test per
row of the table above. The screen and the log both print the same `plan.reasons()`.

## Overrides: one button each, either gamepad, before PLAY only

| Button | Does | Shown as |
|---|---|---|
| X / B | Blue / red alliance (as today) | "(you)" |
| A | The partner: Launch & Park ⇄ Just Park | The Partner row |
| D-pad ◀ / ▶ | Left / right start, needed only with no camera | "(you)" |
| Y | Toggle SMART ⇄ BACKUP | "(you)" |
| Hold D-pad ▲ 1 s | Camera disabled ⇄ back to detection (held, so a bump can't do it) | "(you)" |
| D-pad ▼ | Clear every override: back to what was detected. The partner stays | — |
| Hold LB + RB 2 s | Lock ⇄ unlock (below) | "LOCKED" on the top line |

An override always wins, and is always marked "(you)". An alliance that disagrees with the camera (X while the camera
sees the red side) is allowed, but its row turns amber and says what the camera sees; a side that disagrees with it
is NOT READY, since the robot can't be on both starts. Every binding is labelled, so
the Controls page lists them with no extra work.

## Lock: nothing changes by accident while waiting

The drive team can sit at the field for 1-10 minutes before a match starts, holding gamepads. So the screen locks
(mentor, 9 Oct 2026: "students often sit at the podium 1-10 minutes ... they could accidentally push a button").

- **Lock and unlock are one chord: hold both bumpers (LB + RB) for 2 s.** That's hard to do by accident, and no
  setting uses either bumper.
- **Only a READY plan can be locked** (mentor, 9 Oct 2026). Locking says "this is final"; holding LB + RB while NOT
  READY only flashes "Can't lock: NOT READY". Unlocking always works.
- **It locks itself** once the plan is READY and no button has been pressed for 30 s. The top line counts down
  ("locks in 12 s") so it is never a surprise.
- **Locked, every override button does nothing**, X / B included. A press only flashes "LOCKED: hold LB + RB 2 s to
  change", so a student who presses something sees that nothing changed and why.
- **The plan is frozen while locked.** The checks keep running and their rows keep updating; if one changes (the
  camera loses the HIVE because someone walks in front of it), the row turns amber with "changed since lock" and
  the top line adds "CHECK". It does not change the plan by itself: a person unlocks and decides. A locked SMART
  plan whose camera then fails is still safe, because every SMART wait has a time limit.
- **Reset** is ▼ (clear every override, back to what was detected), which works only unlocked. A full reset is
  stopping the OpMode and pressing INIT again, as today.
- **Pairing a gamepad doesn't change anything.** The Driver Station pairs gamepads with Start + A and Start + B,
  and B is the red-alliance button. Any press while Start is held is ignored.
- **Every override, lock and unlock is written to the match log** with its time, so "who changed the partner?" has an
  answer afterwards.

The top line when locked:

```
RED ALLIANCE · READY · LOCKED
```

## The screens

**One page during Smart Auto's INIT** (mentor, 9 Oct 2026: "the other 2 pages really arent adding anything"; "the
checks screen is a bit redundant ... everything on the check page is already on your face now in an orderly way").
The checks grid and the line under it say what a verbose page would. Share does nothing during this INIT; if deeper
telemetry is needed later it goes behind Share. The CONTROLS and ROBOT pages stay for TeleOp, as today.

The Driver Station draws telemetry with Android's basic HTML: bold, `<big>`, `<small>`, font colours and monospace
(`<tt>`), but **no tables and no control of width**, so nothing can be justified across the screen. Columns are made
with monospace text padded by non-breaking spaces (plain spaces collapse). Each value takes its status colour
(green, amber, red), so a glance reads the state.

**MATCH: light, for the drive team.** One line first: the alliance, big and in red or blue (mentor: "that is really
important to get right"), then the verdict. Then the two choices, and every check on one line, each name in its
colour. Only the checks have dots; on the lines above, the coloured words say enough (mentor: "i dont know that the
bullet points add anything for the first 4").

```
RED ALLIANCE · READY · locks in 24 s
─────────────────────────────────────────────
Partner    Launch & Park · from the left start: launches its preloads, then parks
Plan       SMART · R-Quals: reacts to the HIVE · 3 TIPs + PARK

● Camera     4 tags      ● Pinpoint   ready
● Start Pose 0.6 in      ● Turret     home
● Battery    13.3 V
Hold LB + RB 2 s to lock now
```

The checks are an aligned grid (monospace), each with its dot, its name and one short value in its colour. The line
under the grid says what to do. When NOT READY, it lists what is still needed that no red line above already shows,
each with its button: with the camera down, for example, "To be READY: side (◀/▶)" (the alliance line above is
already red with its X/B). When READY, it names the worst warning, if any:

```
● Battery    12.4 V
battery 12.4 V: swap it
```

**Every button sits next to what it changes**, in its gamepad colour, so there is no button list to read (the mentor
called the old list "definitely the weakest spot"): "(X/B)" after the alliance, "(A)" after Partner, "(Y)" after Plan.
The alliance keeps two buttons, not one toggle: a press always means the same colour, so nobody needs to know the
current state, and a double-tap can't land on the wrong one (X is blue and B is red on our gamepads, as in TeleOp).
The rarer ones appear only when they apply: ◀ ▶ in the line under the grid when there is no camera to see the
start, "hold ▲" when the camera is suspect. When locked the buttons do nothing, so none are shown. One hint line
holds the rest: "▼ clears your changes" once someone has changed something, "Hold LB + RB 2 s to lock now" when ready,
how to unlock when locked.

```
RED ALLIANCE (X/B) · READY · locks in 24 s
─────────────────────────────────────────────
Partner (A)  Launch & Park · from the left start: launches its preloads, then parks
Plan (Y)     SMART · R-Quals: reacts to the HIVE · 3 TIPs + PARK
...
Hold LB + RB 2 s to lock now
```

When it is NOT READY, the top line says just that ("RED ALLIANCE · NOT READY"): the red line below it already says
what and which button, so the top line doesn't repeat it (mentor: "having the pick A twice is not needed").

The buttons are still labelled bindings, so TeleOp's CONTROLS page lists them under "Before PLAY" with no extra
work.

Rows say what a person can check by eye: "sees the HIVE (4 AprilTags)", the tags in view right now, not "14 fixes"
(a fix is one tag sighting turned into a position; the start check wants 5 that agree). Counts like fixes go on the
Robot page.

## The robot light (later)

The screen helps only the person holding the Driver Station. A light on the robot lets a partner, the field staff or
a coach in the stands catch what the drive team missed (mentor, 9 Oct 2026: "having things flashing to alert the
audience (and team members) in case our drive team is not on the ball").

**Decided (mentor, 9 Oct 2026):** one light, and it shows the alliance. Anyone can check the alliance against the
field without knowing our code, and a wrong alliance is the costliest mistake (the whole Auto runs turned about). How
it lights says whether all is well, so red never means "error" (the mentor: "i definitley wouldnt use red to show
error here because of red alliance shenanigans").

| Light | Means |
|---|---|
| Solid red or blue | Ready, on that alliance |
| Red or blue, flashing | That alliance, but NOT READY, or something changed since the lock |
| Red and blue, alternating | Mismatch: the camera sees one alliance and the buttons chose the other. The buttons still win (the camera may be the thing that's wrong); the alliance line turns amber, "camera sees blue", and the top line adds CHECK |
| White, flashing | No alliance, or no partner picked yet |

With the camera off there is nothing to disagree with a wrong button, and the solid colour is the only safeguard: a
blue glow on the red side. Limits from the manual: lighting faster than 5 Hz invites scrutiny, so nothing passes
2 Hz, and a powered light can't be the alliance sign, so the light sits beside the required sign. A second, status
light (Smart or Backup) would add little the screen doesn't show; add one only for a reason. What the light shows
during the match is a later design.

## At PLAY and after

- **The plan locks at PLAY** and is written to the match log as one event with its reasons, so a bad match can be
  explained afterwards from the `.wpilog`.
- **The Match page keeps the alliance and the plan on top during AUTO** ("RED ALLIANCE", "SMART"), above the
  route's own lines.
- **A camera lost mid-match** costs nothing worse than a backup: every SMART wait already has a time limit, and on
  its timeout branch the route carries on. This is a rule for every SMART route: no wait without a time limit that
  leads somewhere sensible.

## Insurance: two emergency OpModes

If Smart Auto's own code throws during INIT, the Driver Station shows the error and the match is lost. So the DS list
also has **Emergency Park L** and **Emergency Park R**, in their own group at the bottom: no checks, no camera, no
screen, just the start pose and a path into the LOADING ZONE. They share no code with Smart Auto beyond the
drivetrain, which is the point. Normally nobody touches them.

## How it fits the code

| Piece | Where | Who |
|---|---|---|
| Candidate starts: `StartCheck` judged against both starts, returning the one confirmed | `localization/StartCheck`, `RobotOpMode` (today: one declared start) | Mentor (substrate) |
| Measured start poses (L and R per alliance) | `localization/StartPositions`, still empty | On a robot |
| Health accessors: `vision.health()`, the Pinpoint's readiness, `turret.health()`, battery | Each subsystem | Mentor (hardware), turret chat for the turret |
| Camera DISABLED switch, including no fixes into the filter | `vision/`, `localization/` | Mentor |
| `decide(health, overrides)`, the partner table, and their unit test | `opmodes/auto/` | Students |
| The INIT screen (`Display` showing only it during Smart Auto's INIT) | `opmodes/auto/`, `controls/Display` | Students; the page cycle is mentor |
| Smart Auto itself: picks one of four generated routes at PLAY and runs it as `BuiltAuto` does | `opmodes/auto/` | Students |
| The routes: L-Quals, R-Quals (spill-free), Backup-L, Backup-R, the emergency parks | `TeamCode/autos/*.pp`, exported from the Visualizer | Students with the simulator chat |

## Later: richer partners

The mentor expects more than two partner behaviours (9 Oct 2026: "our partner does more than just shoot preloads and
park"; "some sort of timing number, or a delay ... to avoid crashing into each other"; "under hive vs. around hive ...
for pathing with two autos working together"). The principle that keeps this screen robust as it grows: **the field
input stays one pick.** Everything richer about a partner lives in a scouted profile, made in the pits, never typed at
the field.

- **More behaviours** are richer profiles ("takes the far FLOWER", "scores TIP 2"), each naming the Auto of ours that
  goes with it. The screen's list gets longer; each entry keeps its own colour. Picking by team number (the number on
  the partner robot in front of the drive team) is the likely form once there are many.
- **A delay** belongs to the profile, not to a dial at the field: a number entered under match pressure is the kind of
  mistake this design removes. If a field adjustment proves necessary, it is a few fixed steps (say +0, +2, +4 s),
  shown big, and locked with everything else.
- **Lanes** ("under the HIVE", "around the HIVE") are part of route design: two Autos are compatible when their lanes
  and times don't overlap, and the simulator's 4-robot mode proves a pair safe before it becomes a profile. The
  screen only ever shows the result.

## Open items

1. **The simulator needs a camera-down mode** (Tip never fires, no start check) before Backup-L and Backup-R can be
   measured. Asked of the simulator chat.
2. **Backup-L and Backup-R**: timer-only, aiming by turning, TIP 1 from our preloads at once, TIP 2 from FLOWERs that
   are always full. Measure at 60 runs against each partner in the list, plus late and never-moving versions of each.
3. **R-Quals without relying on a spill.** The baseline rule (`doc/unified-design.md`): no TIP may depend on catching
   pieces that fall out of the HIVE. Today's R-Quals waits under the HIVE to catch TIP 1's spill and fires it for
   TIP 2, so it breaks the rule. The pieces always there: our 4, the partner's 4 if it launches, two FLOWERs and the
   GARDEN. With a Launch & Park partner that is 20, exactly 3 TIPs; with Just Park, 16, so 2 TIPs, never 3 (today's
   R-Quals, catching, gets 3 in 38 of 60). **For the mentor:** keep the rule for Just Park and accept 2 TIPs, or let
   R-Quals catch for its 3rd TIP on top of a spill-free 2 (a missed catch then costs only the 3rd).
4. **PARK ONLY without the Pinpoint.** The start is still known (the camera's start check needs no Pinpoint; with no
   camera, ◀ ▶ give it); what's lost is odometry while driving. Preferred: Pedro localizing from the drive motors'
   encoders, enough for one short path. Needs the encoders wired on both robots (check at the next meeting) and a
   second localizer in `Constants.createLocalizer` (mentor). Fallback: a timed drive. Measure which reaches the
   LOADING ZONE reliably, or settle for LEAVE.
5. **The flywheel encoder failsafe** (later): what Auto does if the flywheel never reports reaching speed.
6. **Battery thresholds**: measure where the launcher's shots start to fall short.
