# 19429 right-arm GARDEN guide: notes and photos only

**The part itself is designed in the "FLOWER stick: GARDEN polish" chat, which owns the GARDEN guides; its print files
went to the mentor on 14 Nov. Nothing here is to be printed.** That chat's new guide spans 19429's right-arm gap (about
87 mm from the arm's inner face to the blue wall's outer face, measured off the photos; the CAD has 38.6 mm), raised over
the hub, the wheel and the round 32 mm part, then sloping about 15° down to the orange ramp. It screws to the arm with
three M4 and to the wall by a tab and one M4 x 20 through the tab, the wall and the ramp. The standard right guide goes
to 20245 unchanged.

- `photos-2026-11-14/`: the mentor's photos of both robots' sticks (19429 blue guides and walls, 20245 white walls).
- `slant3d.py` / `slant3d.png`: an earlier sketch from another session of a face a ball rides up and is rolled
  inboard, kept behind the ramp's front edge. Reference only.

## Why 19429's WALL FLOWER picks stalled (14 Nov)

All 3 Auto WALL FLOWER picks on 19429 stopped 4.4–5.5 in short with nothing picked; 20245's stopped about 2.6 in short
with 2–3 pieces (`read_logs.py picks`, `logs/2026-11-14-*`). The guides are not the cause: checked against the field
FLOWER model, their undersides (1.12 in up or more) pass over the FLOWER's 0.35 in lip, and the ramp's nose reaches the
FLOWER 1.8 in before anything else. The nose is 0.44 in off the tile in the CAD, only 0.09 in over the lip, so a stick
sitting a tenth of an inch low catches it (worn O-rings, the ramp on another hex flat, a different arm angle).
