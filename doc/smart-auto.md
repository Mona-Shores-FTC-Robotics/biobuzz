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
- The drive team picks the partner from the behaviours scouting knows: **Just Park, Left Launch & Park, Right Launch
  & Park**. Until one is picked: NOT READY, and the robot light flashes white. The partner fixes our start, so nobody
  picks a side.
- The camera can be disabled by hand when it is connected but its vision is wrong.
- The turret's angle comes from an absolute encoder read through an OctoQuad (workstream #81).
- The flywheel is not checked during INIT (spinning it up before PLAY isn't allowed). A failsafe for a broken
  flywheel encoder is for later.

## The plans

| Plan | When | What it does | Expect |
|---|---|---|---|
| **SMART** | The camera sees the HIVE and where the robot is | The partner's route (below). Waits react to the HIVE, as today | 3 TIPs + PARK |
| **BACKUP** | The camera is broken or disabled, or someone forced it | Backup-L or Backup-R, by the partner: every wait is a timer, aiming by turning the robot, the TIPs made by us alone | 2 TIPs + PARK, a 3rd if a partner's shots land |
| **PARK ONLY** | The Pinpoint is missing or not ready (no path following) | A timed drive into the LOADING ZONE | PARK, if a timed drive can be made reliable; else LEAVE only |
| **NOT READY** | No partner or no alliance picked, or the robot is on the wrong start for that partner | Won't move at PLAY; the screen says what to do | — |

All the qualifier routes aim by turning the robot (they are measured on "rigid V, fixed turret"), so **a broken
turret never changes the plan**: with no trustworthy turret angle the turret is held forward and the routes run
unchanged.

The two baseline routes follow the baseline rule in `doc/unified-design.md`: no TIP relies on a spill. L-Quals already
does; R-Quals today catches TIP 1's spill for TIP 2 and needs a spill-free version (open item 3).

## The partner

The one choice a person makes before every match, from scouting. It fixes our start (two robots can't share one) and
so our route:

| Partner | What it does | We start | SMART route | BACKUP route |
|---|---|---|---|---|
| **Just Park** | Doesn't launch; parks | Right | R-Quals: TIP 1 is ours at once | Backup-R |
| **Left Launch & Park** | Launches its preloads from the left start, then parks | Right | R-Quals | Backup-R |
| **Right Launch & Park** | Launches its preloads from the right start, then parks | Left | L-Quals | Backup-L |

D-pad ◀ ▶ step through the list. There is no default: until a partner is picked the verdict line says "NOT READY:
press ◀ ▶ to pick the partner" and the light flashes white. A partner-specific Auto (one built for a scouted team)
is a new entry here, with its route. Scouting still decides details the list hides: a Just Park partner that waits at
the standard left start is the case `doc/r-quals-partner-timing.md` covers.

## The checks (INIT, nothing moves)

The rules allow INIT to hold motors and servos still (R103.B), and the robot must be motionless once INIT is done
(G304.H), so every check reads; none moves anything.

| Row | Healthy | Not healthy | How it's read |
|---|---|---|---|
| **Camera** | ON: sees HIVE tags | BROKEN: not connected, or no HIVE tag for 3 s. DISABLED: by a person. ⚠ SUSPECT: tags seen, but the fixes match no start (off by more than a few inches, or scattered) | `robot.vision.isConnected()`, the `CameraBlind` test, `StartCheck` |
| **Start** | The start the partner implies, confirmed by the camera, within 1 in: "in position" | On the other start: ✖ "this partner needs us on the right start", NOT READY (the robot is misplaced, or the partner is mis-picked). 1-3 in off: ⚠ "nudge it". Over 3 in: ✖ "reposition", NOT READY. Much further (about 12 in, or scattered fixes) the camera itself is suspect. No camera: ⚠ "no camera to check it". The tolerances are to be set from a field test | `StartCheck` against both candidate starts |
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
partner or alliance not picked      → NOT READY until a button picks it
robot on the wrong start            → NOT READY (the partner fixes our start)
the route                           → from the partner and the plan (the table above)
turret not healthy                  → held forward (every plan already aims by turning)
```

It is one pure function, `plan = decide(health, overrides)`: no hardware, unit-tested at a laptop with a test per
row of the table above. The screen and the log both print the same `plan.reasons()`.

## Overrides: one button each, either gamepad, before PLAY only

| Button | Does | Shown as |
|---|---|---|
| X / B | Blue / red alliance (as today) | "(you)" |
| D-pad ◀ / ▶ | Step through the partners | The Partner row |
| Y | Toggle SMART ⇄ BACKUP | "(you)" |
| Hold D-pad ▲ 1 s | Camera disabled ⇄ back to detection (held, so a bump can't do it) | "(you)" |
| D-pad ▼ | Clear every override: back to what was detected. The partner stays | — |
| Hold LB + RB 2 s | Lock ⇄ unlock (below) | "LOCKED" on the verdict line |

An override always wins, and is always marked "(you)". One that disagrees with the camera (X while the camera sees
the red side) is allowed, but its row turns amber and says what the camera sees. Every binding is labelled, so
the Controls page lists them with no extra work.

## Lock: nothing changes by accident while waiting

The drive team can sit at the field for 1-10 minutes before a match starts, holding gamepads. So the screen locks
(mentor, 9 Oct 2026: "students often sit at the podium 1-10 minutes ... they could accidentally push a button").

- **Lock and unlock are one chord: hold both bumpers (LB + RB) for 2 s.** That's hard to do by accident, and no
  setting uses either bumper.
- **It locks itself** once the plan is READY and no button has been pressed for 30 s. The verdict line counts down
  ("locks in 12 s") so it is never a surprise.
- **Locked, every override button does nothing**, X / B included. A press only flashes "LOCKED: hold LB + RB 2 s to
  change", so a student who presses something sees that nothing changed and why.
- **The plan is frozen while locked.** The checks keep running and their rows keep updating; if one changes (the
  camera loses the HIVE because someone walks in front of it), the row turns amber with "changed since lock" and
  the verdict line adds "CHECK". It does not change the plan by itself: a person unlocks and decides. A locked SMART
  plan whose camera then fails is still safe, because every SMART wait has a time limit.
- **Reset** is ▼ (clear every override, back to what was detected), which works only unlocked. A full reset is
  stopping the OpMode and pressing INIT again, as today.
- **Pairing a gamepad doesn't change anything.** The Driver Station pairs gamepads with Start + A and Start + B,
  and B is the red-alliance button. Any press while Start is held is ignored.
- **Every override, lock and unlock is written to the match log** with its time, so "who changed the partner?" has an
  answer afterwards.

The verdict line when locked:

```
● READY · LOCKED
```

## The screen

The Match page during INIT. The Driver Station renders HTML (`controls/Display`): bold, `<big>`, `<small>` and font
colours. Repeated spaces collapse, so rows are dots and labels, not padded columns. Each row's value takes the row's
colour (green, amber, red), so a glance down the page reads the state. **The alliance comes first**, big and bold
in red or blue (mentor, 9 Oct 2026: "that is really important to get right"), then the verdict.

Healthy, no buttons pressed:

```
● RED ALLIANCE
● READY · locks in 24 s
─────────────────────────────────────────────
● Partner    Left Launch & Park · launches from the left start, then parks
● Plan       SMART · R-Quals: reacts to the HIVE · 3 TIPs + PARK
● Camera     sees the HIVE (4 AprilTags)
● Start      right start · in position (0.6 in)
● Pinpoint   ready
● Turret     at home (0.4°)
● Battery    13.3 V
◀▶ partner · Y backup · hold ▲ camera off · ▼ reset · hold LB+RB lock
```

The camera is down:

```
● RED ALLIANCE (you)
● NOT READY: press ◀ ▶ to pick the partner
─────────────────────────────────────────────
● Partner    not picked: press ◀ ▶
● Plan       BACKUP: on timers · 2 TIPs + PARK
● Camera     not connected
● Start      waits on the partner
● Pinpoint   ready
● Turret     at home (0.4°)
● Battery    12.8 V: swap if there is time
```

Rows say what a person can check by eye: "sees the HIVE (4 AprilTags)", the tags in view right now, not "14
fixes" (a fix is one tag sighting turned into a position; the start check wants 5 that agree). Whether the position
is confirmed shows on the Start row. Counts like fixes go on the Robot page, for whoever is debugging.

The two lines above the rule are the only ones the drive team must read: the alliance, then READY or NOT READY. When
it is NOT READY, that line names the one thing to do. Nothing on them repeats a row below: the partner, the plan and
its route, and the start each have their own row. Nothing else shares the page during INIT.

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
| Red and blue, alternating | Mismatch: the camera sees one alliance and the buttons chose the other. The buttons still win (the camera may be the thing that's wrong); the alliance line turns amber, "camera sees blue", and the verdict line adds CHECK |
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
| Candidate starts: `StartCheck` judged against both starts, returning the one confirmed (Smart Auto compares it with the partner's) | `localization/StartCheck`, `RobotOpMode` (today: one declared start) | Mentor (substrate) |
| Measured start poses (L and R per alliance) | `localization/StartPositions`, still empty | On a robot |
| Health accessors: `vision.health()`, the Pinpoint's readiness, `turret.health()`, battery | Each subsystem | Mentor (hardware), turret chat for the turret |
| Camera DISABLED switch, including no fixes into the filter | `vision/`, `localization/` | Mentor |
| `decide(health, overrides)`, the partner table, and their unit test | `opmodes/auto/` | Students |
| The INIT screen | `opmodes/auto/`, using `controls/Display` | Students |
| Smart Auto itself: picks one of four generated routes at PLAY and runs it as `BuiltAuto` does | `opmodes/auto/` | Students |
| The routes: L-Quals, R-Quals (spill-free), Backup-L, Backup-R, the emergency parks | `TeamCode/autos/*.pp`, exported from the Visualizer | Students with the simulator chat |

## Open items

1. **The simulator needs a camera-down mode** (Tip never fires, no start check) before Backup-L and Backup-R can be
   measured. Asked of the simulator chat.
2. **Backup-L and Backup-R**: timer-only, aiming by turning, TIP 1 from our preloads at once, TIP 2 from FLOWERs that
   are always full. Measure at 60 runs against each partner in the list, plus late and never-moving versions of each.
3. **A spill-free R-Quals** (the baseline rule): TIP 2 from the wall FLOWER carried up the left side, as the sister
   Autos do.
4. **PARK ONLY without the Pinpoint**: can a timed drive reach the LOADING ZONE reliably, or is it LEAVE only?
5. **The flywheel encoder failsafe** (later): what Auto does if the flywheel never reports reaching speed.
6. **Battery thresholds**: measure where the launcher's shots start to fall short.
