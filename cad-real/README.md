# The robot's front corners and FLOWER stick, from the mentor's STEP (scratch branch, not for merging)

- `corners_and_stick.step` (18.9 MB): 108 parts cut from `robotdhs.step` (the mentor's Onshape export, 26 Nov 2026,
  177 MB, not in git), **in the STEP's own frame**, so it lands in place when inserted at the origin in Onshape: the
  front of the chassis (the front mecanum modules' side plates, the rails, the Dual Blocks), the intake's front (side
  channels, plates, front shaft), the FLOWER stick (both 9 Hole Mini U-Channel arms, the tip plates, the 384 mm REX
  shaft, the mountain, the roller, the active drive's GT2 belt and 8T sprocket) and the stick's pivot (the SWYFT servo,
  its blocks and hub shafts). Left out: fasteners, the mecanum rollers and their lettering, the intake's rollers and
  motor, the electronics, the Viper slides.
- `corners_and_stick.png`: side and front views, stick down (as drawn) and stowed (swung back 162 deg about the pivot,
  X 5.76, Z 7.58 in).
- **The STEP has no corner wheels**: nothing sits ahead of the front mecanum at the corners.
- Scripts: `load.py` (the STEP to triangles), `frame.py` (into the robot frame, from the four drive axles), `strip.py`
  (the cut STEP), `render_strip.py` (the picture).
