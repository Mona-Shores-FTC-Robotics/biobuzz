# A printed clean-sheet robot (concept agreed 10 Oct 2026; CAD in `cad/printed-chassis/`)

Issue #172. A robot as easy as possible to build, service and change, printing whatever printing does better and buying
the rest. It is drawn from the Auto strategy in [redesign-brief.md](redesign-brief.md), not the other way round.
Nothing here is built or measured; this is the concept to agree before much is drawn.

## Printers and materials (the user and the mentors, 10 Oct)

The mentors print at home, one printer is dual-material, and any reasonably priced filament is fine: PETG, TPU and
PETG-CF or nylon-CF are all available. The design keeps cost sensible and says where a part needs the dearer one.

| | Rule | Why |
|---|---|---|
| Part size | **Every printed part within 180 mm each way** (the user, 10 Oct) | fits the smallest common home bed (210 mm) with room for a brim, and big flat PETG plates warp less. Bigger parts are printed in pieces and bolted: the tray, the launcher's top plate, the lane walls |
| Default material | **PETG**, 4 walls, 40% gyroid | brackets, the lane, the tray: cheap and tough |
| Stiff parts | **PETG-CF** (or nylon-CF) **(material)** | only where a part must not flex: the flaps (they carry the extractor's stubs), the extractor's arms, the launcher's top plate (the turret's base) |
| Faces a piece hits | **TPU 95A printed onto the rigid part** on the dual-material printer | the flaps' inner faces and tips (below); also the lane's rollers |
| Jigs and fit coupons | PLA or PLA+ | |
| Inserts | M3 and M4 brass heat-set | every screw into a printed part |

## What the Auto asks of the robot

From the brief, in its order:

1. **Fixed flaps reaching 3.4 in ahead of the face**, the longest that never touch a wall, the HIVE's feet or the centre
   line. They do as well as scheduled 8 in flaps (28 / 30 fifth TIPs of 60) with nothing to actuate.
2. **A dead front.** Restitution 0 against 0.4 is 33 against 5 fifth TIPs: the front's bounce matters more than its
   shape. Every surface a spilled piece can hit gets a soft face (TPU, or foam if the drop test asks).
3. **A faster intake:** a piece every 0.17 to 0.2 s, taking pieces arriving at up to about 120 in/s.
4. **At most 4 pieces, by geometry** (G407), with no sensor and no software count.
5. **Everything the current robot does:** turret, two flywheels, an independent feeder, the FLOWER extractor, the
   fixed forward Limelight, drive speed about where the routes are timed (50 in/s).
6. **R102 / R105:** inside 18 in at the start, 18 × 24 × 29 after START.

## The one decision the brief makes for us: a 17.75 in outline with the flaps fixed

The simulator's fixed flaps deploy at 1 s on today's 15.12 in frame. A clean sheet can do better: **make the body
short enough that the flaps fit inside 18 in from the start**, so there is no fold, latch or servo at all.

- Flap tips 3.4 in ahead of the face → **body 14.35 in, face to back, flap tip to back 17.75 in.** The design keeps 1/8 in a
  side inside the 18 in cube (the user, 10 Oct) for print error, soft faces, screw heads, cables and sag, measured over
  everything; that 1/4 in comes off the back, so the flaps keep their reach. Across, the tips are 17.75 apart (1.255 in
  outside the frame instead of 1.38).
- That is the same outer length as today's robot with its V (17.96), so every route's wall and HIVE clearances are
  unchanged; what changes is that the flaps reach 0.6 in further ahead of the intake.
- Width stays 18 in over the flap roots, about 15.2 in over the wheels.

**The flaps' geometry** (body-designs chat, from the simulator's flap model): each flap is hinged at a front corner of
the frame, on the face at the frame's side; its tip is 3.4 in ahead of the **face** (not the mouth) and 1.38 in outside
the frame's side, splayed about 22° outward. On the 15.24 in wide frame the tips are 18.0 in across. The 4 in height is
a placeholder. Nothing in the routes reads the body length; the footprint checks follow the outline. The catch spots sit
0.26 in further from the pieces on a 14.6 in body.

**Scored** (body-designs chat, 10 Oct, `doc/tip1-catch.md` 5d0f240 on `claude/biobuzz-robot-body-designs-hi386c`;
simulator only, Sister five, both robots on the outline, today's routes unchanged, 60 runs):

| Outline | Mean points | 5 / 4 / ≤2 TIPs | R holds 4 after TIP 1 |
|---|---|---|---|
| Today's, 15.12 × 15.24, V | 95 | 21 / 33 / 1 | 32 |
| **14.6 × 15.24, 3.4 in flaps fixed from START** | 95 | 22 / 31 / 2 | 37 |
| The same, foam-faced flaps and front (restitution 0) | **98** | **32** / 18 / 1 | 41 |

- Fixing the flaps from START costs nothing against deploying them at 1 s, and the routes need no change.
- **The 17.75 in outline costs nothing** (body-designs chat, same routes, 60 runs): 14.35 × 15.24 body, tips 3.4 in ahead
  and 1.255 in out, fixed from START. At restitution 0.1, 97 points, 5 / 4 / ≤2 TIPs in 28 / 26 / 2, R holds 4 in 40;
  at 0, 98, 32 / 18 / 1, 39. Frame hits 2, walls and centre line 0, G409 in 0 to 2 runs. Against the 18.0 outline's
  22 and 32, within run-to-run spread. (The simulator centres the frame, so its face moved 0.125 in back where ours
  stays put; far below what changes these results.)
- **Foam is the bigger win, +10 fifth TIPs**, and it also took the G409 risk to zero: with bare flaps, in 4 runs a flap
  or the robot touched a spilled POLLEN still in the air. The simulator's restitution is a guess; the drop test comes
  first.
- **Cross-check (simulator chat, `claude/simulator` 891e44b), on the Sister five routes as first drawn** (the table
  above uses the body-designs chat's retuned `sister_five.py`; neither was retuned for the 14.6 in body): 5 TIPs in 16 / 60 for both today's
  body and the 14.6 in one at today's intake; at a 0.17 s / 120 in/s intake, 27 on today's body against 22 on the
  14.6 in one. The catch spots were tuned for the drawn front, which on the shorter body is 0.26 in further from the
  route's pose, with the mouth (1.94 in ahead of the face in both) 0.6 in further behind the flap tips than behind the
  V's. So **the catch spots should be re-tuned for the body**; on the retuned routes above the loss doesn't show. The mouth's
  reach ahead of the face is ours to set in the CAD; the further forward, the closer to the simulated today's front.
  The simulator models the drive as a box, so it says nothing about wheelbase.
- **How hard can the flaps be?** (simulator chat, 891e44b, routes as first drawn, fifth TIPs of 60): at today's intake,
  restitution 0.1 (the baseline, TPU-like) 16, 0.4 (a hard plate) 10, 0.6 8; at the 0.17 s intake, 0.05 26, 0.1 27,
  0.4 25. A hard face costs about 6 at today's intake and 2 at the fast one, and softer than 0.1 buys nothing. So the
  flaps get a TPU face; the structure behind it can be as stiff as it likes.
- **How soft the face must be** (body-designs chat, on this 17.75 in outline, Sister five, 60 runs; 5 TIPs / R holds 4 /
  mean points): restitution 0: 32 / 39 / 98; 0.05: 30 / 37 / 98; 0.1: 28 / 40 / 97; 0.2: 16 / 29 / 91; 0.3: 4 / 18 / 87;
  0.4: 6 / 13 / 87. Most of the gain needs 0.1 or lower; half is left at 0.2, none from 0.3. The first lab drop tests
  (provisional, 10 Oct) put the mentor's black lane-bar foam at about 0.4 on both balls. So **a face measuring above
  about 0.15 isn't worth building**; the printed TPU face, bare polycarbonate and a bare printed face are dropped on
  Monday.

## Layout

Same order as today, front to back, because the lane's length is the 4-piece count and the turret has to sit over the
lane's end:

```
  flap tips ─┐                                                        17.75 in
             ▼
     ╲  flap (TPU)     roller on swing arms     lane (free-roller ceiling)         turret + flywheels      ╱
      ╲═══════════╗   ◯  ───────────────────────────────────────────────  ▣ launch column        ║
       front face ╚═══┴═══ wheel ══════════════════════════════════════════ wheel ════════════════╝
                   ◄─3.4─►◄──────────────────────── 14.35 body ─────────────────────────────────►
```

| Region | What's there |
|---|---|
| Front 3.4 in | The two fixed flaps and the FLOWER extractor (stowed in front of the roller, as today) |
| Front of body | The intake roller on two swing arms; the TPU-faced flaps' roots |
| Middle | The lane: 4 pieces nose to tail, roller axle to backstop 12.26 to 12.60 in (the window `cad/transfer/` found) |
| Back third | The launch column, the feeder and pad, the flywheels under a printed turret, the electronics on top |
| Corners | Four goBILDA drive corners: wheel between the rail and an outer pattern plate, motor on a pattern plate above |

## The frame: a stock ladder, printed everything else

**The recommendation, judged against the user's goals** (the user, 10 Oct: "I gave you my goals": the Sister five Auto
above all, then ease of building, serviceability and fast iteration). Mentors, push back here if you disagree.

- **Auto performance:** the drive's geometry, stiffness and wheel alignment decide how well Pedro follows a route. A
  goBILDA channel ladder is square and stiff out of the box; a printed frame over 15 in long would be spliced from
  several prints, and its alignment would depend on each print and joint.
- **Building:** the ladder is four bought channels and their brackets, every hole already there. Nothing long is printed,
  nothing is drilled.
- **Service:** every mechanism is a printed module bolted to the ladder; a broken one comes off on its own.
- **Iteration:** what changes from week to week (the flaps, the intake, the lane, the launcher's frame) is all printed,
  and reprinting one module doesn't touch the frame or anything else.
- **The cost:** a little weight against an all-printed frame, and the frame's hole pattern on a 24 mm grid sets where
  modules can bolt.

So:

- **Bought:** two goBILDA U-channel side rails and two cross channels (a ladder, as the Strafer kit is), with goBILDA's
  hole pattern, so nothing is drilled.
- **Printed: every module that bolts to the ladder.** Each comes off with at most four screws, all M4 socket heads
  (one 3 mm key) from above or outside, and none hides another's screws:

| Module | What it is | Comes off with |
|---|---|---|
| Drive corner ×4 (goBILDA) | wheel on an 80 mm shaft in the rail and the outer plate, its motor on a vertical pattern plate, belt between | the screws-and-service pass settles each part's way out (the front motors sit under the front cross channel) |
| Front | the two flaps (swappable soft faces), the extractor's stubs and servo | a few screws a flap, into the outer plate's front end |
| Intake | roller, its swing arms, motor | 2 pivot screws + motor plug |
| Lane | walls, rollers, ceiling, ramp | 4 screws (v2 settles its path out: the front drive motors cross over it) |
| Launcher | turret kit and its drive, flywheel cassettes and motors, feeder, pad, backstop | 4 screws |
| Electronics tray | Control Hub, Expansion Hub, OctoQuad, switch, battery cradle | 4 screws, plugs stay on the tray |

## What printing changes, mechanism by mechanism

Reused from today's design where it's sound; changed where printing makes it simpler or the brief asks for more.

| Mechanism | Today (`cad/intake-b/`, `cad/transfer/`) | Clean sheet | Why |
|---|---|---|---|
| **Flaps** | Rigid V, 1/8 in aluminium, tips 2.8 in out | **Fixed flaps, tips 3.4 in out: a rigid PETG-CF core (it carries the extractor's stub) with 3 mm of TPU printed onto its inner face and round its tip** | Brief items 1 and 2. In the simulator a hard flap (restitution 0.4) loses about 6 fifth TIPs of 60 at today's intake and 2 at the fast one, while going softer than 0.1 buys nothing (below). TPU is about there; foam on a clip-on backer is the fallback if the drop test says otherwise. |
| **Front face** | bare channel and plates | **Nothing to face**: the roller fills the face between the flaps (±6.5 in), and the flaps' roots cover the rest | A spilled piece that reaches the face meets the roller or a TPU face. |
| **Intake roller** | Floats straight up 1.3 in in slots, motor on a sliding carriage | **Roller on two printed swing arms pivoting behind the front wheels, level with its mid-float, so it rises straight up; its motor rides the right arm**, belted to the roller over the wheel | Today's slots exist only because the old uprights stood behind the roller. A swing arm needs no slots, carriage or slides, and its belt keeps its length. |
| **Roller wheels and centring** | WCP vector wheels + printed inserts | **The mentor's design: 48 mm Gecko wheels on the roller, and his two flexible star wheels, servo-driven through one-way bearings, centring pieces into the lane** (the user, 10 Oct) | The 8 Oct meeting dropped vector wheels, and the mentor likes his stars: flexible, so they grip both sizes, and one-way, so a piece can't push them back and roll out. Their drive parts are TBD until we have his. |
| **Roller speed** | 1150 RPM × 2 in ≈ 120 in/s surface | **About 150 to 170 in/s surface** (a 1620 RPM motor or a larger roller) | The brief wants pieces arriving at 120 in/s taken; the surface must outrun them. |
| **Lane** | 5 shafts of rollers under a **still** foam ceiling: the ball rolls at half the tread's speed (about 24 in/s) | **A ceiling of free-spinning rollers** over the same TPU rollers: the ball rides at the full tread speed | At 0.17 s a piece, 3.6 in pieces need at least 21 in/s with no gap; today's lane has no margin. Twice the speed with no extra drive (the CAD replaced the driven top run first proposed here). |
| **4-piece count** | Lane length, backstop on slots | **Same**: roller axle to backstop 12.43 in, backstop on ±0.2 in slots, set with real balls | It works and needs nothing. |
| **Feeder, gate, pad** | Feeder on a yoke belted to the left flywheel, gate servo, sprung pad | **Same principle, printed yoke and pad, own motor** (the 8 Oct motor budget: the turret is a servo, so the feeder gets a motor; never driven from a flywheel) | Unchanged decision; where its motor fits is open (README, Open 3). |
| **Turret** | goBILDA 3208-0004-0001 kit (176T ring, 64T drive gear, 105 mm bore), servo, two Thru-Bore encoders on the 64T and a 36T (1178° window) | **The same kit and drive, bought** (the user, 10 Oct), on a PETG-CF top plate, its drive gear pointing back; the servo and encoders as `cad/modules/turret-kit`; the flywheels stay fixed under it, sprung toward each other on the frame (the user, 10 Oct) | A stock ring is round, stiff and known; the encoders' window (1178°) covers the routes' 1080°. |
| **Flywheels** | Two stock 96 mm wheels, 6000 RPM Yellow Jackets | **Same, bought**; printed motor brackets and guard | Stock wheels are balanced and durable; printing them isn't worth it. |
| **FLOWER extractor** | Arms on stubs, cross shaft, printed gear pair, servo | **Same geometry**, arms printed instead of cut aluminium **(material: PETG-CF, 6 mm)** | The drawn geometry is checked against the FLOWER; only the build method changes. |
| **Drive** | goBILDA mecanum, belted, wheels hung on the rail plus an added outer plate | **All goBILDA** (the user, 10 Oct): the mentor's corner with a goBILDA outer pattern plate, motors on vertical pattern plates, last season's 435 RPM motors | Printed pods would carry the whole drivetrain's loads in plastic, and Pedro depends on the wheels staying aligned. |
| **Limelight** | goBILDA mast on the launcher | **Printed mast on the electronics tray**, same lens position and 45° pitch | The camera position is localization data: keep it where the code expects. |

## Bought vs printed (first cut; the full list with sources comes with the CAD)

| Bought | Printed |
|---|---|
| goBILDA channels, pattern plates, standoffs (ladder and drive corners) | odometry pod adapters |
| 4 goBILDA mecanum wheels, 4 drive motors (as today) | flaps with TPU faces |
| Intake motor (1620 RPM Yellow Jacket); 48 mm Gecko wheels; the mentor's star wheels, their servos and one-way bearings | roller swing arms, the mouth's ramp and floor, the star servos' brackets |
| 2 flywheel motors (6000 RPM), 2 × 96 mm flywheels | lane walls, TPU lane rollers, pulleys, ramp |
| Feeder motor; feeder Gecko wheels | feeder yoke, pad, backstop, launcher frame |
| goBILDA turret kit, turret servo, extractor servo | launcher top plate, encoder cradles (`cad/modules/turret-kit`) |
| 2 REV Thru-Bore encoders, OctoQuad | extractor arms, gear pair, servo brackets |
| goBILDA REX shafts, bearings, collars, belts, pulleys | electronics tray, battery cradle, Limelight mast |
| 608 bearings for the ceiling's rollers | drilling and fit jigs (PLA+) |
| Round belt, M3/M4 screws, inserts (foam only if the drop test asks) | |

## How it meets the brief

| Brief | How |
|---|---|
| Fixed 3.4 in flaps, nothing to actuate | 14.35 in body + 3.4 in flaps = 17.75 in at the start (1/8 in margin a side); no deploy |
| Low-bounce front | TPU printed onto every flap face a spill can reach; foam if the drop test says TPU bounces |
| 0.17 to 0.2 s intake, 120 in/s arrivals | faster roller surface; a free-roller ceiling, so the lane runs at twice today's ball speed |
| ≤ 4 pieces, physically | lane length, as `cad/transfer/` sets it |
| Turret, flywheels, feeder, extractor | kept; built from printed parts and stock motion parts |
| R102 / R105 | 18 × 18 at the start; deployed, only the extractor reaches forward (about 21 in front to back, as today) |
| Easy service | six modules, each off with four screws or fewer, one key size |

## Open questions

1. ~~Printer, materials, ladder frame, turret~~: settled (the user, 10 Oct; above).
2. **Drop test** (brief, "Measure it first"): a POLLEN and a NECTAR onto a printed TPU face, a bare PETG face and a 1/2 in
   EVA sample; restitution = sqrt(rebound / drop), ten drops each. The simulator chat turns the numbers into fifth TIPs.
3. **Rig tests before the CAD is trusted:** the swing-arm roller's grab rate on POLLEN and NECTAR, and the free-roller
   lane's speed.

## The CAD

Layout v1 is in [cad/printed-chassis/](../cad/printed-chassis/README.md): every module at its size and place, the
bought parts from goBILDA's STEP files, and checks for clashes (static, the roller's rise, the extractor's swing),
the pieces' path, the outline, the 4-piece count and every printed part's size. All pass. Its README lists what
changed from this concept, the bought parts with sources, and what v2 adds (screws, gear teeth, springs, the lane's
belt).
