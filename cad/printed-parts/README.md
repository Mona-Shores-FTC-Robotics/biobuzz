# Printed parts on the robots: the final versions

The print files for the printed parts on the competition robots, in one folder. Onshape's Robot
assembly is not up to date for these parts, so these files are what to print, not Onshape.
Millimetres throughout: a part that comes in tiny was read as inches, so scale it by 25.4.

| Part | Files | Where it is on the robot | Source |
|---|---|---|---|
| FLOWER poker, `poker_f17` | `poker_f17.stl`, `poker_f17.step` | The intake's front channel. Reaches 1.7 in ahead of the intake face. Held by the front M4 screw (2 mm longer than the one already there) and one zip tie through the wall hole and out the roof hole | `spike/179-flower-stick-proto`, `cad/modules/flower-stick-proto/` (locked 30 Oct 2026; its `PRINT_ME.md` has the full fitting notes) |
| FLOWER stick mountain, `mountain_wide_2in` | `mountain_wide_2in.stl` | The blade on the FLOWER stick's 312 mm shaft, between the arms. 201 x 51 x 15 mm | The same spike branch. Onshape's `Flower Ramp` part is the same file |
| 20245 GARDEN guides, right rev 6 and left rev 9 | **not here yet** | The ends of the FLOWER stick, beside the walls | `20245_GARDEN_guides_rev9.zip`, attached in the 19 Nov "Right GARDEN guide for robot 20245" chat |
| The walls (v14), one each side | **not here yet** | The ends of the mountain; both 20245 guides are made to fit these walls | The chat that made them |

## Print settings (the poker and the mountain)

PETG, not PLA. 0.2 mm layers, 5 walls, 30 % infill (40 % for the mountain), 5 top and bottom layers.
The mountain needs no supports. The poker needs support under the front lip only.

## Adding the missing parts

Put the rev 9 zip's two `*_print.stl` / `*_print.step` pairs and its `PRINT_SHEET.txt` here,
along with the v14 wall files. Update the table so it lists only what is on a robot now.
