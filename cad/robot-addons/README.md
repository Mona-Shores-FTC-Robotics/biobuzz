# Robot add-ons: outer wheel plates, odometry pods, ramp hook

Bolt-on parts for the team's robot CAD ("DHS Robot Copy" in Onshape). Nothing on that robot moves: every part bolts to
holes it already has. `dhs-addons.step` is built in the robot CAD's own coordinates, so in Onshape it lands in place.

- **Outer wheel plates.** Each wheel is held only by the inside rail today. A 1/8 in aluminium plate down each side, just
  outside the belts and pulleys, carries a bearing on each wheel shaft. Four M4 standoffs (56 mm) tie it to the rail's
  existing holes, between the wheels where no belt runs. The 72 mm wheel shafts become 80 mm so they reach the bearing.
- **Odometry pods.** Two goBILDA 4-bar pods (3110-0001-0002), as in the example chassis: a strafe pod bolted to the
  right rail between the wheels, a forward pod on a printed adapter on the left rail. Both wheels touch the floor.
- **The ramp hook** (`doc/ramp-hook.md`), now on two hinges, one in front of each front wheel. A printed bracket on
  each front upright holds the axle's inner end; the outer plate holds its outer end. A stepped arm on each side lets
  the tall part clear the wheel as the hook folds. The servo (goBILDA Torque) sits outside the right plate.

This replaces the single right-hand hinge in `cad/ramp-hook/`. The FLOWER block's shape is unchanged.

## Checked against the robot CAD

| | |
|---|---|
| Collisions, every 3° from down to stowed (90°) | none: robot, plates, brackets, servo and pods |
| Hook down, front to back | 23.9 / 24 in |
| Stowed, front to back | 17.5 / 18 in |
| Across, with the servo | 17.4 / 18 in |
| Lowest point of the hook | 0.7 in (the two shaft collars 0.61 in: check the real collars) |
| Pod wheels | on the floor |

The robot CAD is 143 MB and stays in Drive, not git. The checks ran on a 0.1 in grid of it.

## Parts

| Part | Qty | Make |
|---|---|---|
| `outer_plate_R` / `_L`, 1/8 in aluminium, 48 mm tall, with a raised ear at the front for the servo | 1 + 1 | Cut |
| M4 standoffs, 56 mm (a stock length, or e.g. 48 + 8) | 8 | Buy |
| 8 mm REX shaft, 80 mm (replaces each 72 mm wheel shaft) | 4 | Buy |
| 8 mm REX flanged bearing, 14 mm OD: 4 in the plates at the wheels, 2 in the plates at the hinge, 2 in the brackets | 8 | Buy |
| `hinge_bracket_R` / `_L` (bolts to the front upright's front face, two M4) | 1 + 1 | Print |
| `hinge_hub_R` / `_L` | 1 + 1 | Print |
| `riser_R` / `_L`, `corner_block_R` / `_L` | 2 + 2 | Print |
| `flower_block` | 1 | Print |
| Curtain clips (front shaft) and side clips (arm shafts) | 4 + 8 | Print |
| `pod_adapter_L` | 1 | Print |
| 8 mm REX shafts: front 312 mm; arm bottom 192 mm ×2; arm top 144 mm ×2; left hinge axle 88 mm; right hinge stub 48 mm | 7 | Buy |
| goBILDA 8 mm REX servo shaft, 25-tooth, 36 mm | 1 | Buy |
| goBILDA 2000 Series servo, Torque | 1 | Buy |
| 8 mm REX clamping collars | 2 | Buy |
| goBILDA 4-bar odometry pod 3110-0001-0002 | 2 | Buy |
| 1/16 in polycarbonate: curtains and side panels | 4 | Cut |

## Before anything is cut or printed

1. Confirm with the robot's designer that the front uprights and the rails stay where they are.
2. Drill the servo's mounting holes in the right plate's ear to match the mount you use (not drawn).
3. Check the pods' mounting holes against the rail (right) and the adapter (left), and goBILDA's mounting height.
4. The STLs are where the parts sit on the robot, not turned for printing. Lay each one flat in the slicer.

## Bringing it into Onshape

1. **Import.** In the document with your copy of the robot, use **Insert → Import** (the **+** at the bottom left)
   and pick `dhs-addons.step`. Onshape converts it into a Part Studio and an assembly.
2. **Insert into the robot's assembly.** In the robot's assembly tab, **Insert** the add-ons assembly. To get it
   exactly where it belongs, fasten its origin to the robot assembly's origin with a **Fastened** mate. Both are
   in the same coordinates. Check: each hinge bracket should sit flat on the front face of its upright.
3. **Let the hook turn.** The hook is its own sub-assembly. Delete any fasten that holds it and add a **Revolute**
   mate between the hinge hub's bore and the bracket's bore (both on the hinge axis). In the mate, turn on
   **Limits** and set 0° and 90°.
4. **Animate it.** Right-click the revolute mate in the mate list and choose **Animate**. The hook folds up and down.
   Watch the front wheels and the uprights as it passes them.
5. **Check for clashes.** Use the assembly's interference check with the hook at 0°, 45° and 90°.

Onshape's menus change from time to time. If a name above doesn't match, the feature itself (Import, Insert,
Fastened/Revolute mate, Limits, Animate) is still there under a nearby menu.

## Rebuilding

`build.py` makes everything from the numbers at its top: the hinge position, the plate offset, the arm's step, the
shaft heights. Change a number, run it, and re-import. With `POD_DIR` pointing at `pod1.brep` / `pod2.brep` (cut from
the example robot), it also loads the real pods, for checks.
