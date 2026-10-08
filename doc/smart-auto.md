# Smart Auto: one Autonomous that checks the robot and picks its own plan

**Status: design, 9 Oct 2026.** From a conversation with the mentor; nothing here is built yet. The decisions it
records are marked **Decided**; the rest is the proposal, for the mentor and students to change.

## The idea

The drive team should not have to make decisions at the field. So there is **one Autonomous**, *Smart Auto*. During
INIT it checks every system it depends on, works out what it can still do, shows that on one Driver Station screen,
and lets the drive team override anything with one button. With a healthy robot nobody presses anything: they read
one green line and press PLAY.

What can still go wrong is a person's choice: which start the robot is placed on, and whether to trust a camera that
is talking but wrong. The screen makes both visible.

**Decided (mentor, 9 Oct 2026):**
- One OpMode chooses between a full plan and a backup plan by itself, and flags anything it can't decide.
- The backup runs on time alone: no camera, no turret (the robot turns to aim), the side picked by a person.
  3 TIPs + PARK would be ideal; 2 TIPs + PARK is acceptable.
- No partner assumption anywhere. The backup counts only on TIPs it makes itself; a partner's shots are a bonus.
- The camera can be disabled by hand when it is connected but its vision is wrong.
- The turret's angle comes from an absolute encoder read through an OctoQuad (workstream #81).
- The flywheel is not checked during INIT (spinning it up before PLAY isn't allowed). A failsafe for a broken
  flywheel encoder is for later.

## The plans

| Plan | When | What it does | Expect |
|---|---|---|---|
| **SMART** | The camera sees the HIVE and confirms one of our starts | That side's qualifier Auto (L-Quals or R-Quals). Waits react to the HIVE, as today | 3 TIPs + PARK |
| **BACKUP** | The camera is broken or disabled, or someone forced it | That side's backup route: every wait is a timer, aiming by turning the robot, the TIPs made by us alone | 2 TIPs + PARK, a 3rd if a partner's shots land |
| **PARK ONLY** | The Pinpoint is missing or not ready (no path following) | A timed drive into the LOADING ZONE | PARK, if a timed drive can be made reliable; else LEAVE only |
| **NOT READY** | It doesn't know a side or an alliance | Won't move at PLAY; the screen says which button to press | — |

All the qualifier routes aim by turning the robot (they are measured on "rigid V, fixed turret"), so **a broken
turret never changes the plan**: with no trustworthy turret angle the turret is held forward and the routes run
unchanged.

The two baseline routes follow the baseline rule in `doc/unified-design.md`: no TIP relies on a spill. L-Quals already
does; R-Quals today catches TIP 1's spill for TIP 2 and needs a spill-free version (open item 3).

## The checks (INIT, nothing moves)

The rules allow INIT to hold motors and servos still (R103.B), and the robot must be motionless once INIT is done
(G304.H), so every check reads; none moves anything.

| Row | Healthy | Not healthy | How it's read |
|---|---|---|---|
| **Camera** | ON: sees HIVE tags | BROKEN: not connected, or no HIVE tag for 3 s. DISABLED: by a person. ⚠ SUSPECT: tags seen, but the fixes match no start (off by more than a few inches, or scattered) | `robot.vision.isConnected()`, the `CameraBlind` test, `StartCheck` |
| **Start** | L or R, confirmed by the camera | Unknown until a side button is pressed | `StartCheck` against both candidate starts |
| **Alliance** | From the camera (today's `MatchSetup`) | From X / B | Unchanged |
| **Pinpoint** | Ready | Missing or not ready → PARK ONLY | The localizer's status |
| **Turret** | Absolute angle read and inside its limits | No encoder, or angle out of range → held forward | `turret.health()` (asked of the turret chat) |
| **Battery** | ≥ 12.5 V | ⚠ below 12.5 V, ✖ below 12.0 V (thresholds to be measured) | The hub's voltage sensor |

**A suspect camera is never disabled by the robot.** It turns the row amber and suggests the button; a person
decides. **Disabled means fully off**: no side or alliance proposal, no TIP detection, and no tag fixes into Pedro's
filter. A camera that is wrong but trusted would drag the pose, and every path after it.

## The decision, in order

```
Pinpoint not ready                  → PARK ONLY
camera ON and a start confirmed     → SMART  (unless Y forced BACKUP)
otherwise                           → BACKUP
side unknown or alliance unknown    → NOT READY until a button picks it
turret not healthy                  → held forward (every plan already aims by turning)
```

It is one pure function, `plan = decide(health, overrides)`: no hardware, unit-tested at a laptop with a test per
row of the table above. The screen and the log both print the same `plan.reasons()`.

## Overrides: one button each, either gamepad, before PLAY only

| Button | Does | Shown as |
|---|---|---|
| X / B | Blue / red alliance (as today) | "(you)" |
| D-pad ◀ / ▶ | Left / right start | "(you)" |
| Y | Toggle SMART ⇄ BACKUP | "(you)" |
| Hold D-pad ▲ 1 s | Camera disabled ⇄ back to detection (held, so a bump can't do it) | "(you)" |
| D-pad ▼ | Clear every override: back to what was detected | — |
| Hold LB + RB 2 s | Lock ⇄ unlock (below) | "LOCKED" on the first line |

An override always wins, and is always marked "(you)". One that disagrees with the camera (▶ while the camera sees
the left start) is allowed, but its row turns amber and says what the camera sees. Every binding is labelled, so
the Controls page lists them with no extra work.

## Lock: nothing changes by accident while waiting

The drive team can sit at the field for 1-10 minutes before a match starts, holding gamepads. So the screen locks
(mentor, 9 Oct 2026: "students often sit at the podium 1-10 minutes ... they could accidentally push a button").

- **Lock and unlock are one chord: hold both bumpers (LB + RB) for 2 s.** That's hard to do by accident, and no
  setting uses either bumper.
- **It locks itself** once the plan is READY and no button has been pressed for 30 s. The first line counts down
  ("locks in 12 s") so it is never a surprise.
- **Locked, every override button does nothing**, X / B included. A press only flashes "LOCKED: hold LB + RB 2 s to
  change", so a student who presses something sees that nothing changed and why.
- **The plan is frozen while locked.** The checks keep running and their rows keep updating; if one changes (the
  camera loses the HIVE because someone walks in front of it), the row turns amber with "changed since lock" and
  the first line adds "CHECK". It does not change the plan by itself: a person unlocks and decides. A locked SMART
  plan whose camera then fails is still safe, because every SMART wait has a time limit.
- **Reset** is ▼ (clear every override, back to what was detected), which works only unlocked. A full reset is
  stopping the OpMode and pressing INIT again, as today.
- **Pairing a gamepad doesn't change anything.** The Driver Station pairs gamepads with Start + A and Start + B,
  and B is the red-alliance button. Any press while Start is held is ignored.
- **Every override, lock and unlock is written to the match log** with its time, so "who changed the side?" has an
  answer afterwards.

The first line when locked:

```
● SMART · RIGHT START · RED                 READY · LOCKED
```

## The screen

The Match page during INIT. The Driver Station renders HTML (`controls/Display`): bold, `<big>`, `<small>` and font
colours. Repeated spaces collapse, so rows are dots and labels, not padded columns.

Healthy, no buttons pressed:

```
● SMART · RIGHT START · RED                      READY
─────────────────────────────────────────────
● Camera     sees the HIVE (4 AprilTags)
● Start      right · 0.8 in off
● Alliance   red
● Pinpoint   ready
● Turret     142.3° · in range
● Battery    13.1 V
D-pad ◀▶ side · Y backup · hold ▲ camera off · ▼ reset
```

The camera is down:

```
● BACKUP · PICK A SIDE                       NOT READY
─────────────────────────────────────────────
● Camera     not connected
● Start      press ◀ or ▶
● Alliance   red (you)
● Pinpoint   ready
● Turret     142.3° · in range
● Battery    12.8 V
Backup runs on timers: 2 TIPs + PARK, more if the partner's shots land.
```

Rows say what a person can check by eye: "sees the HIVE (4 AprilTags)", the tags in view right now, not "14
fixes" (a fix is one tag sighting turned into a position; the start check wants 5 that agree). Whether the position
is confirmed shows on the Start row. Counts like fixes go on the Robot page, for whoever is debugging.

The first line is the only one the drive team must read: the plan, the side, the alliance, and READY or NOT READY in
green or red. Rows below explain it. Nothing else shares the page during INIT.

## At PLAY and after

- **The plan locks at PLAY** and is written to the match log as one event with its reasons, so a bad match can be
  explained afterwards from the `.wpilog`.
- **The Match page's first line keeps the plan during AUTO** ("SMART · RIGHT · RED"), above the route's own lines.
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
| Candidate starts: `StartCheck` judged against both of a side pair, returning the one confirmed | `localization/StartCheck`, `RobotOpMode` (today: one declared start) | Mentor (substrate) |
| Measured start poses (L and R per alliance) | `localization/StartPositions`, still empty | On a robot |
| Health accessors: `vision.health()`, the Pinpoint's readiness, `turret.health()`, battery | Each subsystem | Mentor (hardware), turret chat for the turret |
| Camera DISABLED switch, including no fixes into the filter | `vision/`, `localization/` | Mentor |
| `decide(health, overrides)` and its unit test | `opmodes/auto/` | Students |
| The INIT screen | `opmodes/auto/`, using `controls/Display` | Students |
| Smart Auto itself: picks one of four generated routes at PLAY and runs it as `BuiltAuto` does | `opmodes/auto/` | Students |
| The routes: L-Quals, R-Quals (spill-free), Backup-L, Backup-R, the emergency parks | `TeamCode/autos/*.pp`, exported from the Visualizer | Students with the simulator chat |

## Open items

1. **The simulator needs a camera-down mode** (Tip never fires, no start check) before Backup-L and Backup-R can be
   measured. Asked of the simulator chat.
2. **Backup-L and Backup-R**: timer-only, aiming by turning, TIP 1 from our preloads at once, TIP 2 from FLOWERs that
   are always full. Measure at 60 runs against every partner type: on time, late, never shoots, never moves.
3. **A spill-free R-Quals** (the baseline rule): TIP 2 from the wall FLOWER carried up the left side, as the sister
   Autos do.
4. **PARK ONLY without the Pinpoint**: can a timed drive reach the LOADING ZONE reliably, or is it LEAVE only?
5. **The flywheel encoder failsafe** (later): what Auto does if the flywheel never reports reaching speed.
6. **Battery thresholds**: measure where the launcher's shots start to fall short.
