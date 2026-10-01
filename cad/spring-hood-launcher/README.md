# Spring-hood launcher

A launcher that throws POLLEN and NECTAR at the same speed from one wheel setting. Why it works,
and the physics behind the numbers: TeamCode/README.md, "Choosing a launcher for POLLEN and
NECTAR". This folder is how to build it.

**Nothing here has been built yet.** The first prototype is the test: bench it as below and feed
what you measure back into `LauncherDesignStudyTest`.

## Files

| File | What it is |
|---|---|
| `stl/*.stl` | The seven printed parts, already in print orientation. No supports needed. |
| `spring-hood-launcher-printed.step` | The printed parts in their assembled positions, for Onshape. Add goBILDA's own STEP files for the rest. |
| `launcher.py` | Generates everything above from a few numbers and checks every part against every other, hood closed and fully open. |
| `ballpath.py` | Sweeps POLLEN and NECTAR along the feed, the wrap and the exit; fails if they touch anything but the wheels and the hood. |
| `glb.py` | Writes the 3D model the build page shows. |

Regenerate after changing a number:

```
pip install cadquery==2.8.0
python launcher.py <goBILDA STEP folder> out     # STLs, STEP, GLB, report.json with the checks
python ballpath.py
```

The goBILDA folder holds goBILDA's STEP files, one unzipped folder per SKU, from
`https://www.gobilda.com/content/step_files/<SKU>.zip`. Without it (`-`), simple shapes of the same
size stand in. The collision check always uses those simple shapes.

## Print (PETG)

| Part | Qty | Size, mm | Notes |
|---|---|---|---|
| `cheek-motor-side` | 1 | 244 × 156 × 29 | Outside face down. 5 walls, 40% infill. Needs a 250 mm bed. |
| `cheek-plain-side` | 1 | 244 × 156 × 29 | As above. |
| `hood-large-half` | 1 | 125 × 143 × 104 | Side plate down; the shell prints straight up. |
| `hood-small-half` | 1 | 125 × 143 × 34 | As above. |
| `arm-upper-x2` | 2 | 55 × 84 × 24 | Flat side down, 100% infill. |
| `arm-lower-x2` | 2 | 55 × 84 × 24 | The one with spring pegs. Flat side down, 100% infill. |
| `gap-gauge` | 1 | 61 × 20 × 8 | Sets the 2.4 in gap. |

The bearing bores are 14.1 mm. Print a cheek corner first if your printer runs tight.

## goBILDA parts

"Kit" means the FTC Starter Kit (3200-4008-2627) has it, unless your StarterBot already uses it.

| Part | SKU | Qty | From |
|---|---|---|---|
| Yellow Jacket motor, 1:1, 6000 rpm | 5203-2402-0001 | 1 | buy |
| 30T steel gear, MOD 0.8, 8 mm REX | 2303-4008-0030 | 2 | kit |
| GripForce Gecko wheel, 72 mm, 30A | 3613-0014-0072 | 2 | kit (may be your feeder) |
| Hyper Hub, 16 mm pattern | 1310-0016-4008 | 2 | kit |
| Hyper Hub, 32 mm pattern | 1313-1632-4008 | 2 | buy |
| Steel flywheel, 82 mm | 3628-0032-0082 | 4 | buy |
| Flanged bearing, 8 mm REX | 1611-0514-4008 | 2 | kit |
| 8 mm REX shaft, 240 mm | 2106-4008-2400 | 2 | kit has 1 |
| 8 mm REX collar | 2920-0001-4008 | 4 | kit |
| U-channel, 8 hole, 216 mm | 1120-0008-0216 | 1 | kit |
| 8 mm ID spacers, 12 / 4 / 3 mm | 1522-0010-0120 / -0040 / -0030 | 1 / 2 / 1 | kit / buy / buy |
| Shim, 8 mm ID, 0.5 mm | 2807-0811-0500 | 2 | kit |
| 4 mm ID spacer, 24 mm | 1502-0006-0240 | 8 | buy |
| 4 mm ID spacer, 16 mm | 1502-0006-0160 | 4 | buy |
| Extension spring, 8 mm OD, 8 kg max | 2915-0001-0002 | 2 | buy |
| M4 socket head: 35 / 30 / 25 / 16 / 12 / 10 mm | 2800-0004-00xx | 8 / 4 / 8 / 11 / 4 / 8 | 12 and 10 in the kit |
| M4 nylock nut | 2812-0004-0007 | 31 | kit has 30 |
| M4 washer | 2801-0004-0008 | 20 | kit |

## Build

1. **Frame.** Press a bearing into each cheek from the inside, flange against the inside face.
   Stand both cheeks on the U-channel, open side down, feet inward: four M4 × 16 + nylock per
   foot, through the channel's outer rows of holes. Slide the second 240 mm shaft through the
   front holes as a tie rod, a collar each side of each cheek.
2. **Motor.** Four M4 × 12 through the motor-side cheek into the motor face, shaft pointing in.
   A 30T gear on the motor shaft, inside the cheek.
3. **Wheel shaft.** Gecko wheels onto the 16 mm hubs (M4 × 10 + washer, as on the StarterBot);
   two flywheels onto each 32 mm hub (M4 × 25 + nylock). Feed the shaft in from the plain side,
   threading on, in order: 3, 4, 12 mm spacers; flywheels and hub; Gecko hub; two Geckos; Gecko
   hub; flywheels and hub; 4 mm spacer; two 0.5 mm shims; the 30T gear; into the motor-side
   bearing. Tighten the hubs, mesh the gears, tighten the gear set screws.
4. **Arms.** Each pivot: M4 × 35 from outside the cheek, a 24 mm spacer through the arm's hub as
   the bushing, washer, nylock. The lower arm (with pegs) has its pegs toward the cheek.
5. **Hood.** Join the halves with three M4 × 16 + nylock through the seam tabs; sand the seam
   flush inside. Each pin: M4 × 30 from inside the hood, through the side plate and a 16 mm spacer
   in the arm's pin hub, washer, nylock.
6. **Stops and springs.** Adjustable stop: M4 × 35 through the slot with a 24 mm spacer over it as
   the bumper. Back stop: the same through the round hole. A spring from the lower arm's middle
   peg to the cheek's peg.

## Set up

1. **Gap.** Hood closed, the gauge between the Gecko tread and the middle of the hood. Slide the
   adjustable stop until the hood just touches it.
2. **Spring force.** With a luggage scale on the hood's back edge, pulled straight back: about
   2 lbf lifts it off the stop and about 6 lbf holds it 1 in open (the model's 8 N preload and
   0.77 N/mm). goBILDA does not publish the spring's rate, so measure: softer is the peg nearest the
   pivot, stiffer the outer one.
3. **Bench test.** At 2700 rpm (the gears are 1:1) the model predicts POLLEN 5.04 m/s and NECTAR
   4.98 m/s. Film at 240 fps beside a tape measure. Match the ratio first: NECTAR slow → softer
   peg; POLLEN slipping → stop in toward 2.2 in.

## Where the numbers come from

- goBILDA sizes are measured from goBILDA's own STEP files: Gecko 72 × 24 mm, flywheel 82 × 6 mm,
  hubs 22 mm long, bearing 14 mm with a 15 mm × 1 mm flange, motor 37.5 mm across with a 24 mm
  shaft, U-channel holes at ±16 mm every 8 mm.
- The 1:1 gear pair at 24 mm and the motor face (M4 on a 16 mm square around a 14 mm hole) are
  from goBILDA's product pages. The screw, spacer and collar SKUs are from goBILDA's DECODE
  Robot-in-3-Days and StarterBot parts lists.
- The spring, gap and arm travel come from the launcher model: `LauncherDesignStudyTest`.
