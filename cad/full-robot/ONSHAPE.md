# The full robot in Onshape: fixed, mated, moving

The robot comes as two STEP files in the same frame, so each stays a manageable size:

- `BIOBUZZ-1-mentor-robot.step`: the mentor's robot, every screw included, with our edits to it (the launcher 0.8 in
  forward, the raised channel, the parts our design replaces taken out). Its turret ring and flywheels are their own
  groups.
- `BIOBUZZ-2-our-parts.step`: the front, the transfer, the launcher's new parts, the pods and the Limelight on its
  mount, with goBILDA's and WCP's own models of most bought parts.

STEP carries shapes and positions but no mates, so both files arrive in Onshape with no joints. To make setting them
up quick, each file is already split into what moves: a **FRAME** group and one group per moving body, named with the
mate it needs. You fix the two FRAMEs, lock each moving group, then add nine mates. (`build.py` without `--mentor` or
`--ours` writes the same robot as one file.)

## 1. Import

1. Unzip both files (7-Zip on Windows; a double-click on a Mac).
2. **Create → Document**, then **Import** both `.step` files. Leave "Flatten assembly" off. They're large, so the
   import takes several minutes.
3. Each file gets its own Assembly tab. Do sections 2 and 3 below in each tab: every mate is between parts of the same
   file (MOVES 5 to 7 in the mentor's, the rest in ours).
4. Then make a new Assembly tab, **Insert** both, leave each at the origin and fix them. They share one frame, so the
   two halves land together, and their mates come with them.

**Fewer tabs.** Onshape makes a tab for every distinct part it imports, which is a lot of tabs here. Two things help:
- In the import dialog, choose to split the assembly into multiple documents. The parts then go into their own
  documents, and this one keeps the assemblies. (Leave "Flatten assembly" off: it would merge the FRAME and MOVES
  groups.)
- The tab manager (bottom left) lists every tab, searches them and makes folders: put the Part Studios in a "parts"
  folder and keep the Assembly tabs on top.

## 2. Everything that doesn't move: two clicks per group

- Right-click each **FRAME** (one per file) → **Fix**. The robot's frame is now fixed in place.
- Make each **MOVES** group rigid: hover its row in the instance list and click the small **lock** icon at the right
  end. Without it, each part in the group turns on its own. (Onshape's **Group** tool, selecting every part under the
  group, does the same.)

That replaces adding a Fastened mate to every part.

**How a Revolute works.** Its two mate connectors are the two halves of a hinge: connector 1 on the part that turns,
connector 2 on the part that holds still (FRAME). Onshape snaps them together and lets the first spin about their
blue (Z) arrow. Pick two circles **on the same axis and in the same plane**, or the group jumps to meet the second one.
If it flips over, click **Flip primary axis**; if it only turns about its axis, drag it back. If the moving group hides the
surface you need, click the **eyeball** on its row to hide it, pick on FRAME, then show it again. To check a mate, click
each connector under it: one should highlight the moving group, the other FRAME.

## 3. The nine mates

Use the **Revolute** or **Slider** mate. For a Revolute, click a round edge on the moving group, then a round edge on
the same axis in FRAME (or in the group it rides on): Onshape puts the joint on that axis. For a Slider, click a flat
face that faces up on each side: the slide is along the face's normal.

| Group | Mate | Click | Limits |
|---|---|---|---|
| MOVES 1 roller carriage | Slider, to FRAME | the top face of the motor carriage, then the top face of a float stop | 0 to 1.3 in (33 mm), up |
| MOVES 2 intake roller | Revolute, to MOVES 1 | the roller shaft's end, then a roller bearing | none |
| MOVES 3 FLOWER extractor | Revolute, to FRAME | a stub shaft's end, then its bearing in the side plate | 0 to 146° (0 is down) |
| MOVES 4 servo gear | Revolute, to FRAME | the servo gear's hub, then the servo's spline | none |
| MOVES 5 turret ring | Revolute, to FRAME | from below: the inner race's inner edge, then the outer race's inner edge (1 mm apart in height) | none |
| MOVES 6 flywheel left | Revolute, to FRAME | the Hyper Hub's end circle, then the 8 mm spacer circle touching it (or the 41T pulley's end, then the 12.5 mm spacer) | none |
| MOVES 7 flywheel right | Revolute, to FRAME | the same, right side | none |
| MOVES 8 feeder | Revolute, to FRAME | the feeder shaft's end, then the hole it runs in | none |
| MOVES 9 sprung pad | Revolute, to FRAME | the pad hinge rod, for both: set the second one's owner to the pad | 0 to about 29° (a NECTAR) |

Then one **mate relation**: **Gear** between the MOVES 3 and MOVES 4 mates, ratio 1, reversed. The servo and the
extractor then turn together, as their 1:1 gears do.

If a mate points the wrong way (the carriage slides down, the extractor folds backward), flip it with the arrows in
the mate dialog.

## 4. Animate

Right-click a mate in the Mates list → **Animate**. Set the start, end and number of steps:

- MOVES 3 from 0 to 146° folds the extractor up, and the servo gear turns with it.
- MOVES 1 from 0 to 1.3 in shows the roller rising for a NECTAR.
- MOVES 2, 6, 7 and 8 spin.

Onshape doesn't simulate contact, so a wheel won't push a ball by itself. To show a ball moving, insert POLLEN or
NECTAR (FIRST's models from the field CAD), put a **Slider** on it along the lane, and animate that. To show the
feeder driving a ball up, give the ball a vertical **Slider** in the column and a **Rack and pinion** relation to the
MOVES 8 feeder's Revolute.
