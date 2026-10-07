# The full robot in Onshape: fixed, mated, moving

`BIOBUZZ-robot.step` arrives in Onshape with no joints. STEP can carry shapes and positions but not mates. To make
that quick, the file is already split into what moves: its top level is **FRAME** and one group per moving body,
named with the mate it needs. You fix one thing, make the groups rigid, then add nine mates.

## 1. Import

1. **Create → Document**, then **Import** `BIOBUZZ-robot.step`. Leave "Flatten assembly" off. The file is large,
   so the import takes several minutes.
2. Open the Assembly tab it makes. The list on the left shows `FRAME …` and `MOVES 1 …` to `MOVES 9 …`.

## 2. Everything that doesn't move: two clicks per group

- Right-click **FRAME** → **Fix**. The whole robot frame is now one fixed piece.
- Right-click each **MOVES** group → **Make rigid**. Each one now moves as a single piece.

That replaces adding a Fastened mate to every part.

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
| MOVES 5 turret ring | Revolute, to FRAME | the bearing's inner race edge, then its outer race edge | none |
| MOVES 6 flywheel left | Revolute, to FRAME | the flywheel shaft's end, then its bearing | none |
| MOVES 7 flywheel right | Revolute, to FRAME | the same, right side | none |
| MOVES 8 feeder | Revolute, to FRAME | the feeder shaft's end, then the hole it runs in | none |
| MOVES 9 sprung pad | Revolute, to FRAME | the pad hinge rod, for both: set the second one's owner to the pad | 0 to about 26° (a NECTAR) |

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
NECTAR (FIRST's models from the field CAD), put a **Slider** on it along the lane, and animate that. Add a
**Rack and pinion** relation to a lane wheel's Revolute if you want them to move together.
