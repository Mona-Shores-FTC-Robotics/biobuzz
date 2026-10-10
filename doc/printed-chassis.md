# A printed clean-sheet robot (concept, 10 Oct 2026, draft for review)

Issue #172. A robot as easy as possible to build, service and change, printing whatever printing does better and buying
the rest. It is drawn from the Auto strategy in [redesign-brief.md](redesign-brief.md), not the other way round.
Nothing here is built or measured; this is the concept to agree before much is drawn.

## Printer and material assumptions (revise when the mentors confirm)

| | Assumed | Matters for |
|---|---|---|
| Bed | 256 × 256 × 256 mm (Bambu P1S / X1C class) | every printed part fits it; nothing longer than about 250 mm is printed in one piece |
| Default material | **PETG** | brackets, pods, modules |
| Rollers and grippy faces | **TPU 95A** (prints on this class of printer) | intake wheels, lane rollers, feeder tread option |
| Jigs and fit coupons | PLA+ | drilling and cutting jigs, test fits |
| Nylon-CF | **only if the mentors say it's available** | the two parts that most want it are named below; each has a PETG fallback |
| Inserts | M3 and M4 brass heat-set | every screw into a printed part |

Parts where material choice matters are marked **(material)** below.

## What the Auto asks of the robot

From the brief, in its order:

1. **Fixed flaps reaching 3.4 in ahead of the face**, the longest that never touch a wall, the HIVE's feet or the centre
   line. They do as well as scheduled 8 in flaps (28 / 30 fifth TIPs of 60) with nothing to actuate.
2. **A dead front.** Restitution 0 against 0.4 is 33 against 5 fifth TIPs: the front's bounce matters more than its
   shape. Every surface a spilled piece can hit is foam-faced.
3. **A faster intake:** a piece every 0.17 to 0.2 s, taking pieces arriving at up to about 120 in/s.
4. **At most 4 pieces, by geometry** (G407), with no sensor and no software count.
5. **Everything the current robot does:** turret, two flywheels, an independent feeder, the FLOWER extractor, the
   fixed forward Limelight, drive speed about where the routes are timed (50 in/s).
6. **R102 / R105:** inside 18 in at the start, 18 × 24 × 29 after START.

## The one decision the brief makes for us: an 18.0 in outline with the flaps fixed

The simulator's fixed flaps deploy at 1 s on today's 15.12 in frame. A clean sheet can do better: **make the body
short enough that the flaps fit inside 18 in from the start**, so there is no fold, latch or servo at all.

- Flap tips 3.4 in ahead of the face → **body 14.6 in, face to back, flap tip to back 18.0 in.**
- That is the same outer length as today's robot with its V (17.96), so every route's wall and HIVE clearances are
  unchanged; what changes is that the flaps reach 0.6 in further ahead of the intake.
- Width stays 18 in over the flap roots, about 15.2 in over the wheels.

**To confirm with the simulator and body-designs chats:** is the 3.4 in measured from the face (as today's 2.8 in V is)
or from the intake's mouth, and does a 14.6 in body with fixed 3.4 in flaps from t = 0 score the same as the run that
deploys them at 1 s.

## Layout

Same order as today, front to back, because the lane's length is the 4-piece count and the turret has to sit over the
lane's end:

```
  flap tips ─┐                                                        18.0 in
             ▼
     ╲  flap (foam)    roller on swing arms     lane (driven top and bottom)       turret + flywheels      ╱
      ╲═══════════╗   ◯  ───────────────────────────────────────────────  ▣ launch column        ║
       front face ╚═══┴═══ wheel ══════════════════════════════════════════ wheel ════════════════╝
                   ◄─3.4─►◄──────────────────────── 14.6 body ──────────────────────────────────►
```

| Region | What's there |
|---|---|
| Front 3.4 in | The two fixed flaps and the FLOWER extractor (stowed in front of the roller, as today) |
| Front of body | The intake roller on two swing arms, foam-faced front bulkhead |
| Middle | The lane: 4 pieces nose to tail, roller axle to backstop 12.26 to 12.60 in (the window `cad/transfer/` found) |
| Back third | The launch column, the feeder and pad, the flywheels under a printed turret, the electronics on top |
| Corners | Four drive pods, one per wheel, each a module |

## The frame: a stock ladder, printed everything else

Long, stiff members are what printing does worst and goBILDA does best, and nothing over 250 mm prints in one piece. So:

- **Bought:** two goBILDA U-channel side rails and two cross channels (a ladder, as the Strafer kit is), with goBILDA's
  hole pattern, so nothing is drilled.
- **Printed: every module that bolts to the ladder.** Each comes off with at most four screws, all M4 socket heads
  (one 3 mm key) from above or outside, and none hides another's screws:

| Module | What it is | Comes off with |
|---|---|---|
| Drive pod ×4 | wheel on its own shaft in two bearings, its motor and belt, all in one printed housing | 4 screws; swap a whole corner in the pit |
| Front | front bulkhead with foam face, flap roots, extractor stubs and servo | 4 screws |
| Intake | roller, its swing arms, motor | 2 pivot screws + motor plug |
| Lane | walls, rollers, top belt, ramp | 4 screws, lifts out upward |
| Launcher | turret ring and rollers, flywheels and motors, feeder, pad, backstop | 4 screws |
| Electronics tray | Control Hub, Expansion Hub, OctoQuad, switch, battery cradle | 4 screws, plugs stay on the tray |

## What printing changes, mechanism by mechanism

Reused from today's design where it's sound; changed where printing makes it simpler or the brief asks for more.

| Mechanism | Today (`cad/intake-b/`, `cad/transfer/`) | Clean sheet | Why |
|---|---|---|---|
| **Flaps** | Rigid V, 1/8 in aluminium, tips 2.8 in out | **Fixed printed flaps, tips 3.4 in out, 1/2 in EVA foam on a clip-on backer** | Brief items 1 and 2. The foam backer clips off, so the drop test can try foams without reprinting. |
| **Front face** | bare channel and plates | **Foam across the whole face below 4 in**, same clip-on backer | A spilled piece that hits the face should die there, not bounce away. |
| **Intake roller** | Floats straight up 1.3 in in slots, motor on a sliding carriage | **Roller on two printed swing arms about the lane's first shaft; its belt runs through the pivot**, so the belt never changes length and nothing slides | Today's slots exist only because the old uprights stood behind the roller. Without them a swing arm is the standard, simpler answer. |
| **Roller wheels** | WCP vector wheels + printed inserts | **WCP vector wheels if they test better, else printed TPU vector wheels** | Printing makes the bore, the hex and the spacing ours; a test decides which grips better. |
| **Roller speed** | 1150 RPM × 2 in ≈ 120 in/s surface | **About 150 to 170 in/s surface** (a 1620 RPM motor or a larger roller) | The brief wants pieces arriving at 120 in/s taken; the surface must outrun them. |
| **Lane** | 5 shafts of rollers under a **still** foam ceiling: the ball rolls at half the tread's speed (about 24 in/s) | **Driven top and bottom** (printed TPU rollers below, a round-belt top run above, from the same motor): the ball moves at full tread speed | At 0.17 s a piece, 3.6 in pieces need at least 21 in/s with no gap; today's lane has no margin. Same motor, twice the speed. |
| **4-piece count** | Lane length, backstop on slots | **Same**: roller axle to backstop 12.43 in, backstop on ±0.2 in slots, set with real balls | It works and needs nothing. |
| **Feeder, gate, pad** | Feeder on a yoke belted to the left flywheel, gate servo, sprung pad | **Same principle, printed yoke and pad, own motor** (the 8 Oct motor budget: the turret is a servo, so the feeder gets a motor) | Unchanged decision; printing only makes the brackets. |
| **Turret** | goBILDA 176T kit + 64T, servo, two Thru-Bore encoders on a 64T and a 36T (1178° window) | **Printed ring gear on four V-groove bearing rollers** (stock 625/608 bearings), servo-driven, same two-encoder trick with **ratios we choose** (e.g. pinions differing by one tooth for a much wider window) | The kit dictated the 64/36 compromise; a printed ring doesn't. The ring is the one part most worth **nylon-CF (material)**; PETG fallback is thicker. |
| **Flywheels** | Two stock 96 mm wheels, 6000 RPM Yellow Jackets | **Same, bought**; printed motor brackets and guard | Stock wheels are balanced and durable; printing them isn't worth it. |
| **FLOWER extractor** | Arms on stubs, cross shaft, printed gear pair, servo | **Same geometry**, arms printed instead of cut aluminium **(material: nylon-CF, or PETG at 6 mm)** | The drawn geometry is checked against the FLOWER; only the build method changes. |
| **Drive** | goBILDA mecanum, belted, wheels hung on the rail plus an added outer plate | **Same wheels and motors, in four printed pods**, wheel supported both sides | One corner out with four screws instead of a frame strip-down. |
| **Limelight** | goBILDA mast on the launcher | **Printed mast on the electronics tray**, same lens position and 45° pitch | The camera position is localization data: keep it where the code expects. |

## Bought vs printed (first cut; the full list with sources comes with the CAD)

| Bought | Printed |
|---|---|
| goBILDA U-channel ×4 (ladder) | 4 drive pods |
| 4 goBILDA mecanum wheels, 4 drive motors (as today) | front bulkhead, flaps, foam backers |
| Intake motor (1620 RPM Yellow Jacket, or 1150 with a larger roller) | roller swing arms, roller hubs or TPU vector wheels |
| 2 flywheel motors (6000 RPM), 2 × 96 mm flywheels | lane walls, TPU lane rollers, pulleys, ramp |
| Feeder motor; feeder Gecko wheels | feeder yoke, pad, backstop, launcher frame |
| Turret servo, extractor servo, gate servo | turret ring gear, roller carriers, encoder pinions |
| 2 REV Thru-Bore encoders, OctoQuad | extractor arms, gear pair, servo brackets |
| goBILDA REX shafts, bearings, collars, belts, pulleys | electronics tray, battery cradle, Limelight mast |
| 625/608 bearings for the turret rollers | drilling and fit jigs (PLA+) |
| EVA / polyethylene foam, round belt, M3/M4 screws, inserts | |

## How it meets the brief

| Brief | How |
|---|---|
| Fixed 3.4 in flaps, nothing to actuate | 14.6 in body + 3.4 in flaps = 18.0 in at the start; no deploy |
| Low-bounce front | foam on every face a spill can reach, on clip-on backers for the drop test |
| 0.17 to 0.2 s intake, 120 in/s arrivals | faster roller surface; a lane driven top and bottom at twice today's ball speed |
| ≤ 4 pieces, physically | lane length, as `cad/transfer/` sets it |
| Turret, flywheels, feeder, extractor | kept; built from printed parts and stock motion parts |
| R102 / R105 | 18 × 18 at the start; deployed, only the extractor reaches forward (about 21 in front to back, as today) |
| Easy service | six modules, each off with four screws or fewer, one key size |

## Open questions

1. **Simulator / body-designs:** where the 3.4 in is measured from, and whether a 14.6 in body with the flaps fixed from
   t = 0 scores as the deploy-at-1 s run does.
2. **Mentors:** printer, materials (TPU? nylon-CF?), and whether they agree to a goBILDA ladder rather than an all-printed
   frame.
3. **Drop test** (brief, "Measure it first"): EVA vs polyethylene foam on a printed backer, so the flap backer is drawn
   for the winner.
4. **Rig tests before the CAD is trusted:** the swing-arm roller's grab rate on POLLEN and NECTAR, the driven-top lane's
   speed, and a printed ring's backlash against the two-encoder decode tolerance.

## Next

Once this is agreed: `cad/printed-chassis/build.py` in the style of `cad/*/build.py` (robot frame, inches, every screw),
a clash check, and the parts list with sources.
