# Front flaps (add-on for the mentor's robot)

Two foam-faced flaps that turn his flush 9.76 in intake mouth into a funnel after START. Simulator only (body-designs
chat, 10 Oct, a Sister five pairing of two of his robots, 60 runs): his bare front averages 59 points with 38 runs
ending at 2 TIPs or fewer; with flaps, 80 and 4; with foam-faced flaps, 84 and 3. His narrow mouth still limits how many
of TIP 1's spill R keeps, and his routes would need retuning for his length; see `doc/redesign-brief.md`.

Checked on 10 Oct against his 9 Oct Robot.step with `tools/robot-cad/module_check.py flaps_folded` and `flaps`: no
clashes with his robot folded or swung out (the only contact, swung out, is a POLLEN staged in his CAD).

- `flaps-folded.step`, `flaps-deployed.step`: the module in his Robot.step's own frame (mm), at START and after it.
  Insert into his Onshape document at the origin.
- `stl/`: the printed parts, PETG.

## How it works

- Each flap hinges on a vertical M4 shoulder screw in a printed bracket on the outside of his front upright, 0.6 in
  ahead of its face. At START it lies folded across his face, foam toward the roller, and his robot is still 16.8 in long
  (his corner wheel is the front-most part), inside the 18 in cube (R102).
- A servo behind the hinge, mounted upside down in the bracket, holds a printed horn across a notch in the flap's knuckle.
  After START the Auto turns it 90°, and a torsion spring at the hinge swings the flap out 112° against a stop on the
  bracket's foot: splayed 22° outward, its tip 3.9 in ahead of his face. The robot is then 19.8 in long and 16 in wide,
  inside 18 x 24 in (R105).
- A printed wedge in front of each upright's face turns a piece arriving at the flap's root into the mouth.
- The left flap is full height (z 0.25 to 4.25 in). The right one is a fence 2 in tall that passes under his corner wheel
  (its bottom at z 2.22).
- Fold them back by hand before each match.

## Parts

| Part | Qty | Notes |
|---|---|---|
| `flap_L`, `flap_R` (printed, PETG) | 1 each | 3 mm plate with a hinge knuckle and the latch notch |
| `flap_bracket_L`, `flap_bracket_R` (printed, PETG) | 1 each | on the upright's web; cradles the servo; the stop and the wedge |
| `flap_latch_horn_L`, `flap_latch_horn_R` (printed, PETG) | 1 each | on the servo's spline |
| 0.5 in EVA or polyethylene foam | about 4 x 4 in | contact-cemented to each flap's inner face |
| Standard-size servo (goBILDA 2000-0025-0003 or similar) | 2 | the latches; two servo ports |
| M4 shoulder screw, 5 mm shoulder, with lock nut | 2 | the hinge pins |
| Torsion spring, about 120° travel, to suit the knuckle | 2 | or a rubber band round two posts |
| goBILDA M4 screws and nylon lock nuts | 4 | brackets to the uprights' webs (row of 4 mm holes 0.63 in above the uprights' bottoms), nuts inside the channel below the roller carriage's V-groove bearings |
| M3 screws | 4 | servos into the cradles |

## Before trusting it

- The latch geometry (the horn, the notch, the spring's preload) is drawn as an envelope: fit it by hand.
- Check the flaps swing out fully within about 0.3 s, so they're open before the first spill lands.
- Check the corner wheel still turns freely over the right fence, with the roller floated up too.
