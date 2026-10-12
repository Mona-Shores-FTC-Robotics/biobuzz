# Buildability audit (12 Oct 2026)

The question: if someone built this robot from these files, would it go together and work? Three independent reviews,
one per area, each finding measured on the loaded model and goBILDA's/REV's/SWYFT's own STEP files. The existing checks
passed throughout; every item below is something they missed. Status: **open** until fixed in `build.py` and checked.

## Why the checks missed these

- Clashes were tested on envelopes (simplified shapes), and a broad `ALLOWED` list let whole families of parts overlap
  (motor and its mount, ceiling and pieces, spring and anchor). Real collisions hid there.
- Joints were claimed in text (`CAPTIVE`, `DEVICE`): "on a hub", "e-clips", "the shaft's bearing in it". Nothing
  checked that the hub, clip or bearing existed, fitted, or was in the BOM.
- Nothing checked that a moving part has a stop and a spring, that a printed part is one piece, or that a belt can be
  tensioned.

## Drivetrain and frame

| # | Finding | Fix |
|---|---|---|
| D1 | **Nothing drives the wheels.** The 3213 mecanum has a plain 14 mm bore and a 32 mm screw pattern; no hub is drawn or bought, and the stack (56 mm between plates) has 2.8 / 1.26 mm either side of the wheel | The mentor's way (a 14 mm-bore pulley bolted to the wheel's pattern), or a 1309 hub with longer standoffs |
| D2 | Socket heads on the motor faces and plate-to-rail screws sit under the real pulleys (12 mm wide, not 15) | Button heads there; spacers so each pulley sits where drawn |
| D3 | Rear belts slack (need 77.5 mm centres, have 76.95) and their slots only move the motor closer | Vertical slots, motor up to +2 mm |
| D4 | Wheel shaft drawn 65 mm, listed as the 80 mm SKU; no room for its e-clip or a collar; spacers not drawn | The 80 mm shaft, e-clip inside the rail, collar outside, every spacer drawn |
| D5 | Two of four quad-block standoffs a side go into untapped holes | Only the block's tapped holes |
| D6 | The "2.5 mm" washer stack is 1.6 mm: the outer plate pulls 0.9 mm toward the wheel | Three washers or a 2.5 mm spacer |
| D7 | Rear cut plate: 0.6 mm of metal beside the axle hole | Extend the plate |
| D8 | Rear motor cantilevered off a 2.5 mm plate | Tie the plate's top to the rear cross channel or launcher frame |
| D9 | Rear cross channel touches neither rail (3.9 mm gaps); text still says "pattern brackets (CHECK)" | Gussets carry it (now drawn); fix the text |
| D10 | BOM and parts list stale (4 bearings listed, 26 drawn) | Regenerate from the full hardware model |

## Intake, extractor, stars, ramp, lane

| # | Finding | Fix |
|---|---|---|
| I1 | **A NECTAR can't get under the sprung ceiling**: hinged at X 2.9 with rollers ahead of and just behind the hinge, it would have to lift 1.02 in on a 0.25 in lever | Hinge ahead of every roller, or a floating ceiling on links |
| I2 | Ceiling rollers too small for their 608 bearings; the ceiling has no sides for their shafts; no band anchors | Rollers sized to their bearings, drawn cheeks, shafts and band hooks |
| I3 | **Swing arms: no rest stop, no spring.** Gravity drops the roller to 1.9 in (not 2.4) onto a printed arm on a channel edge; upward, the intake motor hits a drive motor at lift 1.6 | Adjustable stop screw, a spring for grip, a soft up-stop |
| I4 | **The intake motor's mount plate sits inside its gearbox**; the motor also hits its belt and the right arm (hidden by `ALLOWED`) | Move the motor out by the plate's thickness, re-plane the pulleys, bore the arm |
| I5 | Extractor stubs only 1.5 mm into the flaps, the flap 20° off square for the bearing; the arms only half wrap the stubs | Square bosses on the flaps, round bosses on the arms, longer stubs |
| I6 | The ramp goes 0.3 in below the tiles; its lip 0.05 in above them | Start at 0.15-0.25 in with a chamfered lip |
| I7 | The stars never touch a centred POLLEN (tips 2.80 apart, the ball 2.80 wide) | Narrow the gap 0.2 in, or a rig test first |
| I8 | Star shaft: one real bearing (the clutch carries no radial load), nothing stops it sliding down | A second bearing, a collar |
| I9 | Printed extractor gears overstressed at the servo's stall (about 80 MPa against PETG-CF's 50) | Module 1.5, a wider face, or metal gears |
| I10 | The intake pivot's shoulder screw threads into the moving arm: the swing backs it out | Thread it into the bracket; the arm turns on a bushing |
| I11 | Stale text (roller shaft "cut to 371 mm"; it's 269) | Fix |

## Launcher and electronics

| # | Finding | Fix |
|---|---|---|
| L1 | **The flywheel motor bracket isn't attached to the yoke** (no top flange, "M4 through the bracket's top" has nothing to go through); its foot drags on the fixed channel | A bracket with a top flange under the yoke, foot clear of the channel |
| L2 | **Only the rear end of each flywheel side and feeder arm is sprung**: the front opens freely and the shaft tilts about 6.5° | One rigid carriage per side, or springs at both ends; tie the feeder plates together |
| L3 | **Printed parts in loose pieces**: the feeder's spring lug and its stop tab touch nothing, so the spring and the stop act on nothing | Join them; a check that every printed part is one solid |
| L4 | The feeder's front pivot block is narrower (12.7 mm) than its bearing (14) and its flange cuts into the lane wall | A wider, thicker block; notch the wall |
| L5 | Feeder arm walls 1.1 mm around their bearing pockets | 24 mm bosses |
| L6 | Feeder motor cantilevered 4.2 in off 6 mm of PETG; its screw holes break out of the block's top | A taller block and a cradle from the belly plate |
| L7 | Flywheel spring drawn inside its anchor; no hook pins | A slot and a pin |
| L8 | Battery shelf on one narrow row of standoffs; its halves not tied together | A second row; tie the halves |
| L9 | Feeder and flywheel shafts end flush or inside their bearings: no room for retention | Longer shafts, collars, flanged bearings |
| L10 | Flywheel guides 5 mm tall with a 1 mm tie, carrying the yoke and motor | Thicker guides |
| L11 | The grounding strap belongs on the Expansion Hub's spare XT30 (the Control Hub's second one powers the Expansion Hub) | Move it |
| L12 | No main power switch (FTC requires one), no OctoQuad, no Pinpoint, no harness or cable routes | Draw and buy them; route the cables |

Belts, gears and the turret checked out: every belt matches its centres, both turret meshes are right, no wire crosses
the ring. The four fixed-centre belts have no tensioning (see D3 for the rear drive).

## Missing hardware (beyond the above)

Bearings, collars, e-clips, REX spacers and shims, wheel hubs or pulleys, one-way clutches, shoulder screws and
bushings, set screws, springs and their pins, stop screws, servo horns, bands, cables, cable ties: every one to be
drawn as a part where it goes and counted in the BOM by part number. The bearings and collars are started (21f8c6c2).

## New checks this calls for

1. Clashes on the real vendor models, with only named, specific contacts allowed (a shaft in its bearing's bore),
   not families of parts.
2. Every moving part has a hard stop and a return (spring or gravity) at each end of its travel.
3. Every printed part is one solid.
4. Every claimed joint's hardware exists as a part, fits, and is in the BOM.
5. Every belt can be tensioned to its exact length.
6. Every shaft is held axially in both directions.
