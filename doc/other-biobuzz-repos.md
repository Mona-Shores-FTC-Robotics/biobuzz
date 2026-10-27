# What other BIOBUZZ repos can teach us (survey, 26 Oct 2026)

> **Read this first (27 Oct).** Written on 26 Oct, before that day's work. Since then: section 2's
> recommendation (HIVE points from the field CAD, axle on the centre line) was done in #244; section
> 6's shot detection by velocity dip was built in #245. Section 1's R704.D event mode (no Panels,
> AutoTune or Dashboard Wi-Fi anywhere at a venue) is still open and unbuilt. Nothing else here has
> been acted on; every number is quoted from another team's code or docs, none measured by us.

Twenty-three public BIOBUZZ repos found by web search, shallow-cloned read-only into the session
scratchpad and read by five research passes; plus the Chief Delphi localization, Limelight, auto-aim
and build threads. Nothing from them was built or run. Every number below is quoted from their code
or docs; "CAD" means a team's reading of the official field CAD, not a tape measure.

**The honest headline:** almost no team has a number measured on a real robot. Most repos are a
Pedro Quickstart plus a goBILDA StarterBot. Three are worth our time: **12303** (deep docs, a
playbook, a physics shot model, a tested-on-paper turret design), **19725** (a working
"solve the HIVE angle" localizer with lab logs), and **GigglePickles 23511/nick-koko** (DECODE code,
but a servo turret with its encoder on a motor port, and a tested vision-into-odometry blend).

---

## 1. Rule R704.D: Panels and AutoTune at events (action needed)

Verified in the Competition Manual TU05 PDF, Section 12, p.141 (TU06 is current; the rule is unchanged
as far as the search shows):

> D. Software with access to the ROBOT CONTROLLER Wi-Fi network must limit the amount of data being
> streamed (i.e., continuous transmission of data) over Wi-Fi. Software may only stream robot control
> data, debugging data, and telemetry to and from the ROBOT using the FTC Driver Station Application.
> Additional logging/streaming services, such as those hosted by third party plugins and tools such as
> FTC Dashboard, FTControl Panels, and others are prohibited. No continuous video stream is allowed.

12303's fact-check file (`docs/research/R5-hardware.verify.md`) quotes official Q&A 26, answered
2026-10-19: "Wi-Fi streaming is not allowed anywhere in a competition venue (per R704.D), so Wi-Fi
streaming (even in the pit and practice field) is prohibited." I could not open the Q&A myself (it
needs a Lead Coach login), so that part is second-hand; the rule text is first-hand.

What it means for us: Panels (`:8001`), AutoTune (`:10158`) and the Limelight web UI over the
hub's Wi-Fi are all out at an event, pits included. Our CLAUDE.md has no event mode. What 12303 did:
a `PEDRO_AUTOTUNE` Gradle flag that drops `com.pedropathing:tuning` from the build, and a
`CompetitionCheck` that warns at INIT if `com.bylazar.panels.Panels`, `FtcDashboard` or AutoTune's
`Tuner` class is in the dex. Panels also has a persistent enable/disable preference. Decide before
the first event: a competition build, or a disable step on the checklist, and a Limelight tuning
path over USB.

## 2. HIVE geometry: our derived points vs two teams' CAD reads

Two independent CAD reads (12303 `FieldConstants.java`, 16751 `FieldAim.java`) put the HIVE axle
**on the field centre line**. Ours is derived at `AXLE_Y_IN = 72.29`, 1.5 in off centre
(`FIELD_CENTRE_INCHES` = 70.75).

12303's CAD numbers, red HIVE, relative to the axle (their pivot is (0, −12.74, 43.95)):

| Row | horizontal from axle | height |
|---|---|---|
| UP tag row | 13.10 in | 49.80 in |
| DOWN tag row | 11.58 in | 35.48 in |
| UP opening centre (aim point, = SDK cluster origin) | 16.50 in | 58.27 in |

Solving those for our rocker model gives a row 14.25 in out and 1.52 in below the axle in the CELL
frame; ours is 15.2 / 1.56 (from the measured 50.2 / 35.0 heights). Converted to our frame, CAD says:

| Point | ours (`ROW_CENTRE`) | CAD | Δ |
|---|---|---|---|
| AUDIENCE UP y | 58.4 (measured) | 57.65 | 0.75 |
| AUDIENCE DOWN y | 59.9 (derived) | 59.17 | 0.7 |
| SCORING UP y | 86.2 (derived) | 83.85 | 2.35 |
| SCORING DOWN y | 84.7 (derived) | 82.33 | 2.4 |
| heights | 50.2 / 35.0 | 49.8 / 35.5 | ~0.4 |

So the 6 in error we saw from behind the HIVE on 24 Oct is about a third explained by the axle
being off-centre and the radius being long. Two things to check with a tape at the next meeting:
is the axle on the centre line, and are the SCORING rows 13.1 in from it. If yes, derive the eight
points from CAD (axle at `FIELD_CENTRE_INCHES`, radius 14.25, drop 1.52) instead of from one
measured pair, and mark SCORING points measured once a tape confirms them.

Everything else agrees with ours: tags 30–45, 3.25 in, member offsets ±6.5 / ±2.75 in, pivot
43.95 in, ±30° (19725's fmap tag spacing is 3.74 / 5.51 / 3.74 in, the same). 12303's walls are at
±70.67 (141.34 in) against our 141.5.

Per 12303's R2 research: community code using a ~9.4 in UP/DOWN delta is wrong; the row delta is
14.3 in and the opening-centre delta 21.5 in. Our 15.2 in delta is the right order.

## 3. Localization: what the field is doing

**Nobody else fuses tag fixes continuously.** 16751 ("no relocalization from tags this season"),
CyberCoyotes, 12303 and smrc treat the tags as aim-only, or as a one-shot button fix. 12303 uses
them for one more thing worth copying: a low-passed **bearing bias** (camera bearing minus bearing
predicted from odometry and CAD tag positions; gain 0.15, clamp ±8°, learned only when the turret is
settled and the chassis is slow). Our `continuousFixes = false` default is in line with everyone.

**Team 19725 (Chief Delphi "solve the HIVE angle")** is the one team localizing from tags at any
HIVE angle, and the repo backs the claim: one fmap per alliance with the HIVE flat (θ = 0), MegaTag1
botpose with *snap-to-ground off*, then solve `A sinφ + B cosφ = C` (A = x', B = z' − h, C = −h,
h = 43.95) for the HIVE angle, accept a root only within ±35°, cross-check against the botpose pitch
(10° tolerance), un-rotate x and yaw, and feed an EKF with the Pinpoint. Lab log (Sept): far from
the tags the vision error was much larger than its reported stddev, so they scaled R by
(avgDist / 1 m)² and by 2/tagCount, and gate on Mahalanobis d² ≤ 16. Their own caveats: y is never
corrected (axis assumed exactly along y), h error maps linearly into x, the pitch check fails near
yaw ±90°, and it is "too noisy for aiming alone". It sidesteps our "never MegaTag" rule's reason
(a fixed map) rather than breaking it; worth knowing, not worth switching to now. Their UP/DOWN
classifier: |θ| ≤ 23° MIDDLE, 23–35° a settled state, per frame, no hysteresis.

**BiobuzzVision.java (iwa-robotics / PsychoGeck0)** is a 1500-line webcam solver nobody runs: grid
search of the HIVE angle from tag heights (robot-frame z is independent of x/y/heading) plus tag
surface normals, then the same SE(2) least-squares fit we do, with outlier dropping (residual > 3 in)
and RMS gate 2.5 in. Its gates are a reasonable reference for ours: fix only with 2+ tags and a
baseline ≥ 4 in, jump ≤ 24 in and ≤ 35°.

**GigglePickles (DECODE, tested):** MegaTag2 translation only, never heading ("enable heading only
after logging proves it is helpful"). Accept: Δ ≤ 24 in, Δheading ≤ 10°, within 10 in of the median
of the last 5 accepted. Apply: ≥ 12 loops apart, 2 consecutive accepts, speed ≤ 16 in/s and
≤ 60°/s. Blend α 0.35 stationary / 0.15 slow / 0.05 moving / 0.6 "bump recovery". Lesson in the
comments: 10 in gates rejected 45% of valid frames; MT2 gives a valid sample only ~5% of loops.
They apply the fix as pose-tracker offsets, not `setPose`, because `setPose` nudged the heading
state. Also: hold the turret's aim pose for 350 ms when odometry jumps > 5 in in one loop.

## 4. Tip detection

- **12303 `HiveTracker`:** operator "TIP!" button flips the state and locks vision out for 5 s; a
  press within 3 s of a camera-driven switch only confirms it. Vision switches after 500 ms of
  continuous disagreement. Any change pauses firing 1 s. Their key insight matches our HiveTracker
  lesson: after your own TIP, your tags face the other half of the field, so the camera at the
  firing spot cannot see its own tip; it sees the tags vanish.
- **smrc-pune:** operator button only, 1.5 s feed pause after a tip.
- **CyberCoyotes:** aim at the CELL whose tag has the highest `ty`, average tx/ty over that
  CELL's tags.
- **16751 `HiveMath`** (unit-tested, unwired): UP if the tag height is above the pivot, settled if
  the plane tilt is within 5° of 30°, else MOVING. Same shape as our `CellStateTracker`.
- **Jefferson-Cydogs:** keeps tags with |ftcPose.roll| < 90° (the SDK cluster library).

Timing facts for #167/#168 (12303's playbook from Saline film, and 14343's thread): dwell
0.25–3.4 s, swing ~0.75 s (up to 2.2 s), spill lands ~3 s after the threshold ball, in the zone you
shot from; 8 POLLEN tipped in 4.76 s from contact. 12303's playbook cites our own
`doc/saline-preview-day2.md`.

## 5. Turret (issues #169, #170, #198)

- **Encoder through a motor port (12303, same design as ours):** use **motor port 0 or 3**; those
  decode in hardware, ports 1/2 can miss counts from an 8192 CPR Thru-Bore. Never reset it, so the
  count survives Auto → TeleOp. Auto-zero: the first time the armed turret is still for 0.3 s, line
  the encoder up to the commanded angle. Re-zero with a warning if encoder and a steady command
  disagree by > 2° for 500 ms ("skipped teeth / wrong scale"). Their `TurretCalibrator` OpMode
  measures ratio, direction and ticks/deg from a centre mark and a 30° mark.
- **GigglePickles (servo turret + quad encoder on `back_left`'s port, tested):** commanded
  servo position plus a clipped encoder trim (`0.12 × error`, ±8°), a rate limiter, a 1° differential
  between the two servos as lash preload, ready = |error| ≤ 2° and |ω| ≤ 15°/s for 3 loops (3 bad
  loops clear it), nearest-equivalent wrap selection (try ±360k, keep in-range, nearest to the
  previous target) to avoid "wrap+clip sign flips near 180°". Their notes: CR servo mode was
  hard to tune, positional was reliable; avoid power = 0 at setpoint (micro-hunting); avoid instant
  `atSetPoint()` readiness.
- **12303 aim formula** is ours (#198): `wrap(bearing(turretAxis → aimPoint) − heading −
  TURRET_CENTER) + trim + visionBias`; turret axis offset from the tracked point is a named
  TODO-MEASURE, like ours.
- **CyberCoyotes:** `setDirection()` reverses motor and encoder together, so they carry a separate
  `INVERT_ENCODER_READING`. 1° ≈ 1 in of miss at 5 ft.

## 6. Launcher (#80: speed vs distance, #181/#213: counting)

No team has a measured table. The best starting points:

- **12303 physics-model table** (72 mm wheel, ~65° exit, exit speed = 0.40 × wheel surface
  speed, distance = turret axis → opening centre, linear interpolation, clamped):
  `{33,3600},{39,3525},{45,3550},{51,3625},{57,3725},{63,3825},{75,4050},{87,4300}` rpm, scaled by
  `0.40 / k_measured`. Reliable band 30–92 in. Their `ShotMapLogger` procedure: 5 distances × 0/20/40°
  off-axis × 10 shots, CSV to the hub.
- **Ready gate** (12303): within ±max(2.5%, 60 rpm) for 3 loops, because a 28-count encoder
  resolves only ~20–25 ticks/s. CyberCoyotes: ±100 rpm on both wheels held 0.15 s. MCB: −50/+100
  tps, 0.15 s settle. 19725: 10 consecutive frames inside max(2%, 40 tps).
- **Shot detection by velocity dip** (12303): a dip below 95% of target during one excursion counts
  one shot; ignore dips for 100 ms after a target change. Our counting issues could use this as a
  second opinion next to the intake current.
- **19725 flywheel controller, tuned 4 Sept (R² 0.995):** `duty = kS·sgn + kff·ω* + kp·e + ki·∫e`
  with kS = 0.029, kff = 3.83e-4, kp = 1.82e-3, ki = 2.59e-3. Two lessons: the duty-vs-speed line
  does not pass through the origin, so without kS the integral sat at −0.05; and a post-shot "boost"
  term only overshot because the output was already saturated at 1.0 during the drop.
- **16751:** a GG StarterBot oscillated at P = 40 because a 312 rpm motor had been fitted instead of
  the kit's 6000 rpm; P = 10 with F = 32767/top speed was smooth. Hub PIDF 40/0/0/12.5 for 6000 rpm
  1:1 appears in four repos as the working default.
- **Hood:** 12303 chose a fixed 60–65° hood ("consistency is worth more"); 14343 measured 44%
  bounce-out with a fixed solid hood and is adding powered counter-rollers; CyberCoyotes runs a
  hood wheel at 2.0× flywheel rpm "best value found so far". Chargers' 9-row hood+rpm table is from
  their own laptop simulator with a made-up grip curve; ignore it.

## 7. Strategy numbers (12303 playbook, Sept 19 – Oct 17, 176 matches, 11 US events)

- Where tip counts differed, the alliance with more TIPs won 102 of 104 matches. Two tips ≈ 78%
  win rate, three ≈ 90%. Median alliance score 35; 0.9 tips per match; 44% of alliances scored none.
  Saline winners averaged 3.6–3.8 tips.
- The starting up-CELL already holds 3 NECTAR, so the 3rd POLLEN tips it. An empty CELL tips with
  8 POLLEN (7 must not), or 3 NECTAR + 3 POLLEN; 1 NECTAR ≈ 1.65 POLLEN. Momentum is worth ≤ 0.25
  POLLEN: plan by count. (12737's thread says 4 POLLEN on the edge tipped it by momentum; 12303's
  calibration reading disagrees.)
- Their AUTO ladder: park (8) → tip & park from the head-on start with 4 preloads (28) → add a
  garden sweep → two-robot double tip. Promote a rung only after 8 of 10 good runs. Park by 23 s.
- TeleOp pace target: a 4-ball load every 15–20 s, so a tip every 30–40 s alone.

## 8. Smaller gotchas, each from one repo

- Pedro `tuning:1.0.2` throws a `TunerScanner` NPE at startup (Wyandotte). We are on 1.0.3-beta.1.
- After Stop, the Pinpoint reports a reset pose, so a direction-detect tuner that reads it after
  Stop always says "not reversed" (Wyandotte's PinpointTuner fix).
- An idle Pedro follower brakes the motors in every `update()`, which fights a hand push; use
  `follower.localizer.update()` for push tests (smrc).
- Limelight returns a zero pose, not null, when it has no fix; `getYaw()` without a unit multiplies
  degrees by 57.3 (16751's relocalization doc).
- Hive-Vision's HSV/Lab webcam colour pipelines measured 7–11% precision on match footage (16751),
  wilsobotics' tuned HSV got yellow 69% recall / 40% precision, red 80% / 26%. For #139, expect
  the webcam colour-blob route to be noisy; 16751 went to the Limelight neural detector instead.
- Pedro path end constraints have a 100 ms timeout; keep your own state timeouts (16751).
- `follower.setPose()` every loop nudges the heading state (GigglePickles); apply fixes as offsets.
- On a Skyline-height mount, a tag 31 in above the camera leaves the frame inside ~50 in (16751).
  12303 expects tag range to be limited to 8–10 ft by the 3.25 in tag; from their head-on start the
  UP tags are at 39° elevation, 63 in, 21° incidence, and they recommend ~40° camera pitch.

## 9. Code organisation ideas seen elsewhere

Worth a look: 12303's laptop `RouteSweepTest` (sweeps every Auto route against walls, FLOWERs,
HIVE legs, the partner and spill boxes); smrc's `Mechanism` base that catches a `RuntimeException`
in `update()`, marks the mechanism FAULTED and keeps the rest of the robot running; 16751's
`OdometryWatchdog` comparing pose sources over 0.5 s windows by distance and turn only; tarun101's
`AGENTS.md` that pins versions and tells a session to stop and ask if the Gradle file disagrees.
No repo uses Ivy beyond `sequential(follow(...), waitMs(...))` in an Auto, and none but
GigglePickles uses Panels tunables, so there is no better Ivy or Panels pattern out there to copy.

## Repos and threads read

Deep: BluePowerRobotics/Team19725_BIOBUZZ, craigmichaelmartin/ftc-12303,
nick-koko/GigglePickles_BioBuzz_2026 (DECODE), FTC16751/FtcRobotController_BioBuzz,
CyberCoyotes/2026-2027-BIOBUZZ, smrc-pune/biobuzz, iwa-robotics-houston/BioBuzz
(= PsychoGeck0/BioBuzz), Wyandotte-Robotics/ftc-14158-biobuzz, MarcusScott1616/Chargers-Biobuzz-FTC2026.
Glanced: Jefferson-Cydogs/FTC10615, brtucker42, tarun101/uprobotics-ftc-2026, 4329/biobuzz,
LIKS-CO, MCB-Robotics/21116, wilsobotics, FTC-3081, CodeRed2771, Limelight-Robotics/ftc-2027,
ftc-4991, spacey-sooty. Private or gone: niranradia/biobuzz-26, Shashanksaraswat123939/biobuzz-simulator.
Chief Delphi: "Idea about FTC Biobuzz localization?", "FTC BioBuzz Limelight Settings/FMAP",
"FTC auto-aiming", 14343 and 12737 build threads, 12527 & 16095 build thread.
