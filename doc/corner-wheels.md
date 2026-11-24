# Corner wheels: do they fit, are they legal, and the centre-mounted stick

The idea (mentor, 23 Nov 2026): stop using the FLOWER stick for GARDEN and corner gathers. Put a small wheel on
a vertical axle at each front corner of the intake, spinning inward, so a POLLEN that touches the robot's corner,
or sits pinned against a wall there, gets raked into the intake mouth. The intake is not widened.

**Short answer: yes, a 2.5 in wheel fits at each corner, ahead of the front drive wheels.** Drive each one with
its own CR servo on the Expansion Hub. It is legal if the wheels stop when the robot holds 4 and stay off during
FLOWER picks. The centre-mounted stick does not fit before States, and the study says it isn't needed.
**19429 would have 9 servos with both wheels, one over R503's 8**; its options are in section 4. The code is in
and off by default (section 5).

Prototype parts: [`cad/corner-wheels/`](../cad/corner-wheels/README.md). The build team's 10-minute hand test is
in [`doc/robot-todo.md`](robot-todo.md) ("Corner wheels").

**Update, 24 Nov 2026 (prototype 3): the corner module is now fitted to the robot's own CAD,** the mentor's STEP export
of the whole robot as built. It replaces the 31 Oct model for the corner. Below, "How a corner goes together, on the
robot's CAD" has the result. Sections 1 to 3 otherwise stand as first worked out on the 31 Oct model. The current
CAD changes three things:

- **The wheel sits right in front of the drive wheel** (the mentor's call), its servo inboard of the stick's right
  arm (|Y| 6.60..7.08). The left arm (|Y| 5.99..6.47 today) moves out to match.
- **The wheel is 2.3 in, not 2.5.** The axle had to come forward for the servo to clear the spinning mecanum.
- **The module bolts to the intake side channel's front flange,** clear of the Viper slide inside it, and a wing
  and a belly under it lock it on three sides: the wheel will be hit hard and often.
- **The servo is a SWYFT Speed** (mentor's choice: more robust), in continuous mode.

## Where the numbers come from

- **The geometry** comes from the AdvantageScope model of the team's Onshape CAD (`cad/advantagescope/`, built
  31 Oct 2026). The Onshape API refused a fresh read today ("API limit exceeded", HTTP 402).
- **What that CAD doesn't show:** the 23 Nov transfer, 19429's 17 Nov roller stick, and the blue/white GARDEN
  guides on both sticks.
- **Frame:** robot frame in inches, +X forward, +Y left, +Z up, origin on the floor under the middle of the drive
  axles.
- **Ball height** is Z 0 to 2.9.
- **Clearances** are the closest any part of one comes to the other, with the stick swept 0 to 162° in 1° steps.

## 1. Fit

### The space at each front corner (at ball height)

| | Left | Right |
|---|---|---|
| Frame side | Y +7.63 | Y −7.77 (15.4 in across) |
| Front drive wheel | Y 5.42..6.88, front at X 7.57 | Y −5.44..−6.90, front at X 7.55 |
| Intake opening (side rails' inner faces) | Y +4.87 | Y −4.87 (9.7 in; the roller is 7.6 in wide) |
| Ahead of the drive wheel (X > 7.57) | empty | a short 2-hole channel at X 7.39..7.87, Z 1.73..4.42 |
| What's above | intake side channels from Z 2.86, Viper slides to Z 9.6 | the same |
| FLOWER stick arm, down | Y 5.40..5.88, from X 9.56 forward | Y −5.84..−6.32, from X 9.56 forward |
| FLOWER stick swing, lowest Z over the arm's band | 4.6 at X 7.8, 3.6 at X 8.8, 2.6 at X 9.8 | about the same |

The brief's 13.8 in is the distance across the front drive wheels' outer faces (±6.89), so its 0.8 in strip is
the space beside the drive wheel. The wheels don't have to fit in that strip. **Ahead of the drive wheels, the
corners are open at ball height**, and that's where the wheels go.

### Wheel size

The axle sits 0.12 in ahead of the drive wheel. The wheel spans Z 0.60..1.65, so it meets a POLLEN at its equator
(Z 1.40).

| Wheel | Fits? | Front reaches | Closest to the stick swing | Why not |
|---|---|---|---|---|
| 2 in | **yes** | X 9.69 | 0.83 in or more | reaches less far than 2.5 in |
| **2.5 in** | **yes: recommended** | X 10.19 | 0.56 in or more | |
| 3 in | **no** | X 10.69 | −0.15 in: hits the arm | it also passes the 18 in starting cube's front, X 10.43 (R102; the robot's back is at X −7.57) |

**Where the 2.5 in wheel goes:**

| | Left | Right |
|---|---|---|
| Axle | **X 8.94, Y +6.85** | **X 8.94, Y −6.95** |
| Rim past the frame side | 0.47 in | 0.43 in |
| Rim ahead of the drive wheels | 2.62 in | 2.62 in |

The robot is then 16.3 in wide, inside both 18 in limits (R102 at the start, R105 after it).

**Clearances, 2.5 in design (inches):**

| | Left | Right |
|---|---|---|
| Wheel to the stick, anywhere in its swing | 0.75 | 0.56 |
| Wheel to the frame | 0.34 | **0.08** (the 2-hole channel's bottom, Z 1.73, over the wheel's top, Z 1.65) |
| Servo body to the stick | 0.58 | 0.24 |
| Hub to the stick | 0.88 | 0.66 |
| Bracket to the stick | 0.23 | 0.25 |

**How a corner goes together, on the robot's CAD** (prototype 3, 24 Nov 2026; `cad/corner-wheels/README.md` has the
parts list):

- **The servo, a SWYFT Speed, is upside down, spline down into the hub.** A standard servo's flange sits near its
  spline end (the SWYFT, measured from its model in the robot's STEP: the case is 29.0 mm below the flange and
  11.2 mm above it, with the output boss and spline 5.7 mm more). So upside down, the flange is near the bottom and the
  body stands up above it: flange Z 2.56..2.66, body up to Z 3.80.
- **The wheel sits right in front of the front drive wheel,** with the servo above it, its body lying side to side, inboard
  of the spline, so nothing reaches back toward the intake (the real robot has no room there). Axle at X 9.22,
  |Y| 5.95; servo X 8.83..9.61, |Y| 4.49..6.62.
- **The printed bracket bolts to the front flange of the intake's side channel,** with two M4 × 12 through its holes
  at Z 3.81 and 4.44 and nuts behind, in the gap ahead of the Viper slide.
  - From that tab a shelf runs straight forward onto the servo's flange: four M4 self-tapping up through the
    flange's 5 mm holes, with a wall in front of the servo's body and one behind it.
  - It wraps the channel on three sides: the tab over the whole front flange, a wing on the outer face, and a belly
    under the channel's open bottom with two M4 up into the Dual Block there. Seven bumps sit in the channel's
    holes (shorter than its 2.5 mm wall, so nothing reaches the slide); the screws hold it on, the bumps stop it
    turning or sliding.
- **The wheel bolts to a goBILDA 1908 Series Servo Hub (25 Tooth Spline, 32mm Diameter)** with four M4 × 30 from
  counterbores in its underside. It is one file, the same either way up: the right corner's goes in upside down.
- **Clearances,** with the stick swept 162° and the mecanum spun a full turn, right corner:
  - servo 0.34 in from the stick, 1.25 in from the mecanum;
  - bracket 0.32 and 0.26 in;
  - wheel 1.28 and 0.52 in.
- **Size:**
  - The front is X 10.36, 17.93 in from the back wall; with the stick retracted, the wheels are the front of the
    robot.
  - Nothing goes past the frame's sides.
- **The left module is the right one mirrored,** assuming the left arm moves out to match the right. Today's left
  arm is where the left servo goes.

![Left corner module](corner-wheels/left-corner-module.png)
![Right corner module](corner-wheels/right-corner-module.png)

**Things to know:**

- **With the stick down, the wheels can't feed.** The stick's arm then lies between each wheel and the mouth from
  X 9.6 forward, so a raked ball hits the arm. The wheels work with the stick up, which is the idea: the stick for
  FLOWERs, the wheels for the GARDEN and the corners.
- **FLOWER, nose-in:**
  - Centred on a FLOWER, the wheels' inner rims (|Y| 5.6) are 3.2 in clear of the FLOWER's sides. The FLOWER's
    half-width is 2.35 in, a placeholder until measured (`FieldSim`).
  - At the wall the wheels are 4.7 in back: R-Quals stops 12.15 in from the FLOWER's centre.
  - If a pick is more than about 3 in off centre, a spinning wheel touches the FLOWER's side (G418, below).
- **HIVE base, nose-in:** the wheel becomes the first thing to touch a foot bar. Foot bars are 2.2 in tall in the
  simulator, also a placeholder. So a nose-in stop at the HIVE base is 2.6 in further out than today, where the
  drive wheels touch first.
- **Robot differences:** the right corner differs from the left in the CAD. Its stick arm is 0.45 in further out,
  and it has the short channel ahead of the drive wheel. So each corner has its own bracket, and the right one bolts
  to that channel's front face. **Check both corners on each robot with a ruler before printing**: 19429's stick and
  guides are newer than this CAD.

### Driving the wheels

**Use a CR servo per corner.** It fits (clearances above), and the code already handles CR servos the way the
star wheels use them (`Kind.CR_SERVO`, `IntakeSubsystem`).

**Use two ports, not one on a Y cable.** The two wheels turn opposite ways, and one signal cannot reverse one of
two servos.

**A belt or bevel off the intake motor doesn't fit.**

- The roller's shaft is horizontal at Z 3.48 and ends at |Y| 4.84, right behind the drive wheels' tops (Z 3.8).
- A take-off would have to run 2 in outboard over the drive wheel, turn 90°, and drop to Z 1.6 ahead of it. That
  is the space the stick's arm swings through (lowest Z 4.6 to 2.6 there).

**Free servo ports** (from `robot_*.xml`):

| Robot | Control Hub | Expansion Hub |
|---|---|---|
| 19429 | none (0–5 all used) | **0, 1, 2, 3** |
| 20245 | 1, 4 (4 is kept for a second star wheel) | **1, 2, 3, 4** |

**Use Expansion Hub ports 1 and 2 on both robots.** That keeps one wiring for both. The Control Hub holds the
mission-critical servos, and the corner wheels aren't.

## 2. Rules

From the 2026-27 BIOBUZZ Competition Manual, TU10 (current), page numbers from its PDF.

### Size

- **R102 (p.121):** "the ROBOT must be fully self-contained within an 18 in. … wide, by 18 in. … long, by 18 in.
  … high volume" at the start. The wheels keep the robot inside it:
  - 16.3 in wide;
  - the front at X 10.19, and X 10.38 for the right servo's bracket, against the cube's X 10.43.
- **R105 (p.122):** "After the start of the MATCH, ROBOTS may expand beyond the STARTING CONFIGURATION but at all
  times must remain within a 18 in. … by 24 in. … by 29 in. … tall sizing volume", and "B. ROBOTS must be
  physically constrained to fit within these limits without the use of software." G416 (p.110) enforces it:
  - So a mechanism may reach past the frame after the start, up to the 18 × 24 in footprint.
  - The rule doesn't say which way the 24 in runs; Figure 12-1 may.
  - The fixed wheels never change the robot's size, so this doesn't limit them.

### CONTROL

- **CONTROL** (glossary, p.170) includes a ROBOT that "intentionally pushes a SCORING ELEMENT to a desired location
  or in a preferred direction (i.e., herding)". **An inward-spinning wheel touching a POLLEN is CONTROL.** It is not
  the excluded "bulldozing" (G407 note D: "inadvertent contact … while in the path of the ROBOT"), because the rake
  is deliberate.
- **G407 (p.107):** "A ROBOT may not simultaneously CONTROL more than 4 SCORING ELEMENTS." Its notes warn that
  repeated 5s are likely STRATEGIC (a MAJOR FOUL and a YELLOW CARD) "even if the ROBOT has a 'keep 4 and discard the
  rest' sorting mechanism".
  - **So the wheels must stop whenever the robot holds 4**, just as the intake does. A 5th ball raked against the
    mouth is CONTROL of 5.
- **G408 (p.108):** "A ROBOT may not CONTROL the opponent's NECTAR."
  - So the wheels run only while the intake takes in, and stop with it when `NectarReject` stops it.
  - They never run on their own as sweepers.

### Other rules

- **G418 (p.111):** POLLEN comes out of a FLOWER only from its bottom; "attempts to pull POLLEN through the side of
  the FLOWER" are likely STRATEGIC. **So the wheels stay off during FLOWER picks.**
- **G406 (p.106) and R201.C (p.124):** no gouging or tearing game pieces, and no "abrasive surfaces that scratch
  objects that rub across them". TPU flaps with rounded tips are fine; a hard plastic star with sharp points is not.
- **G417 (p.110):** the HIVE moves only by launching. Its examples list "A ROBOT accidentally bumping into a HIVE
  frame while attempting to pick up POLLEN" as likely not STRATEGIC.

## 3. A centre-mounted FLOWER stick off the Limelight tower

![Side view](corner-wheels/centre-stick-side.png)

**The geometry:**

- The tower is two 9-hole Lowside channels at X 1.69..2.20, |Y| ≤ 1.89, Z 6.2..15.6. The Limelight 3A sits on it
  at X 2.68..4.40, Z 14.0..15.7, pitched 45° up.
- Its view is 42° tall, so it sees 24° to 66° above level. Anything below the lens and ahead of it is out of view.

**What a centre stick would mean:**

- **The camera is not the problem.** A stick that comes down ahead of the robot is below the lens (Z 14.8), so
  it never blocks the tags. Stowed, it has to stay behind the lens (X < 2.7) or out to the side. Today's arms
  pass outside the camera's width (|Y| ≥ 5.4).
- **The arm is the problem:**
  - From a pivot on the tower's front (X 2.45, Z 8.0) to today's ramp tip (X 12.6, Z 0.5) is 12.6 in.
  - A straight arm at Y 0 runs through a servo block, the intake motor and the intake roller, which is why the
    arms are at the sides today.
  - Stowed straight up, it reaches Z 20.6, over the 18 in cube (R102). Stowed back under 18 in, it passes
    through the turret.
  - Only a cranked or folding arm could work: over the intake at about Z 8, then down. That is a new mechanism, not
    a remount.
- **The study says the mount isn't what fails.** The FLOWER pick study (`doc/flower-picks/`, 22 Nov) found:
  - 20245's passive stick, held out, got 4 on every pick seen clearly on video (6 of 8 got 4; the other two are
    questionable 3s).
  - 19429's active roller stick, run limp, got 4 on only 4 of 10.
  - The difference is passive against active and held against limp, not where the arms are.

**Verdict: don't build a centre stick before States.** Run the study's test first: 19429 with its stick held. If
it doesn't get 4 on at least 8 of 10 picks, put 20245's passive stick on 19429.

## 4. The servo budget: 19429 would have 9

**R503 (p.133):** "ROBOTS are limited to a total of 8 motors and 8 servos. A ROBOT may not have more than 8
motors and 8 servos from the allowable actuator lists per R501 and R502 for all MECHANISMS used in all
configurations." Its note adds that servos fitted for different events' configurations count together.

**Does the indicator light count?**

- R503 counts servos "from the allowable actuator lists per R502", and R502 covers servo actuators.
- The goBILDA RGB Indicator Light plugs into a servo port but is a light, not an actuator. So by the rule's text
  it does not count.
- The manual does not say so in words. Confirm with the inspector at check-in, or ask on the official Q&A,
  before 19429 relies on it.

**What each robot has today** (the robot XMLs, the light not counted):

| Robot | Servos today | With both corner wheels |
|---|---|---|
| 19429 | 7: `intakeStars`, `intakeStarsRight`, `flowerRoller`, `prefeed`, `feedGate`, `turret`, `flowerStick` | **9: over the limit** |
| 20245 | 5: `intakeStars`, `prefeed`, `feedGate`, `turret`, `flowerStick` | 7 (8 if it ever takes the second star-wheel servo its port 4 is kept for) |

**19429's options.** These are the build team's and the mentor's to choose. The code handles each one.

- **(a) A passive FLOWER stick on 19429, as 20245 has.** That drops `flowerRoller`, so 6 + 2 = **8**.
  - The FLOWER pick study already points this way if 19429's held test misses 4 on 8 of 10 picks. 20245's held
    passive stick got 4 on every pick seen clearly on video.
  - Code: turn off the `FlowerStickSubsystem.roller` line in `RobotDifferences` and remove `flowerRoller`
    from `robot_19429.xml` and `DeviceNames.ROBOT_19429`.
- **(b) One servo for both wheels, with a crossed belt** (crossed because the wheels turn opposite ways): 7 + 1 =
  **8**. It doesn't fit as the CAD stands.
  - The two axles are 13.8 in apart across the front. At wheel height the belt would cross the intake mouth in
    front of the roller, where the POLLEN come in.
  - Above the roller's top (Z 4.42), it runs through the FLOWER stick's swing over each arm's band: the arm comes
    down to Z 4.6 at X 7.8 and Z 3.6 at X 8.8. It would also run through the intake's side channels (X 6.8..8.5,
    Z 2.86..8.53).
  - It would need a route behind the side channels, through holes in them, or another take-off.
  - Code: `rightServo = false`.
- **(c) One corner only:** 7 + 1 = **8**.
  - Which corner is the drivers' call: the one the GARDEN and wall gathers meet most.
  - Code: `rightServo = false`, with that servo named `cornerWheelLeft` wherever it sits, and its direction set on
    the bench.
- **Also possible: put 19429's two star-wheel servos back on one, as 20245 runs.** That gives 6 + 2 = 8.
  - 19429 split them onto two servos on 13 Nov, and the doc doesn't say why.
  - Code: turn off the `IntakeSubsystem.rightStarWheel` line.

## 5. The code: in, off by default

`CornerWheelSubsystem`, on `Robot`.

**Hardware:**

- Two optional CR servos, `DeviceNames.CORNER_WHEEL_LEFT` and `CORNER_WHEEL_RIGHT` (`DeviceNames.CORNER_WHEELS`,
  part of every competition robot's `RobotIdentity.optional`).
- They are asked for only once a robot's `RobotDifferences` line sets `CornerWheelSubsystem.fitted = true`, and
  the OpMode refuses to start if that robot's XML lacks one.
- Both XMLs carry them commented out, on Expansion Hub servo ports 1 (left) and 2 (right), free on both robots.
- **Check: Hardware** lists them with their switch.

**When they spin:** inward at `power` only when all of these hold (`CornerWheelSubsystem.stopFor`):

- after PLAY;
- while the OpMode's intake takes in: not a feed's assist, an eject, a reject or a jam clear;
- the lane doesn't read full and the lane tracker counts fewer than 4 (G407; the driver's R2 past a full lane
  never runs them);
- no wrong NECTAR may be in and none is being spat out (G408);
- the FLOWER stick is up (G418).

Otherwise each servo gets 0, one write per loop.

**Where to see why they're stopped:** the Robot page's "Corner wheels" line.

**Tests:**

- `InitMovesNothingTest` checks nothing moves before PLAY, even with every rule saying rake.
- `CornerWheelSubsystemTest` checks each stop rule, and that a fitted robot without the servo fails loudly.

**Bench: Corner wheels** (Utility) spins each wheel for the hand test and flips each one's direction (`leftReversed`,
`rightReversed`). It needs the servos in the XML, not the `RobotDifferences` line.

**Still to do:**

- An Auto GARDEN or corner gather that keeps the stick up.
- `LoopTimeBaseline` before and after, on the robot (`doc/robot-todo.md`).
