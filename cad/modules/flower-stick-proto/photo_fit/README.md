# Reading the blade lip off the 28 Oct photos

`solve_B.py` solves the camera pose (OpenCV solvePnP) from points on the poker's roof whose positions are known from the part
(corners, the two 12 mm holes, the relief holes, the rear screw), then back-projects the blade's tail end (known to be 50.8 mm
behind its lip) onto the tile plane. `overlay_B.py` draws the photo as a true top view at the roof's height and at the blade's
height, with the model's geometry on top. `solve_A.py` does the same on a second, more oblique photo.

Check: the poke tip, not used in the fit, comes out at x = 33.2 mm against a design 33.

Result, and a conflict. The two overhead photos (A and B) put the blade's lip about **4.7 in** ahead of the intake face (4.6 to
5.0). The ring photo (`solve_C2.py`, `overlay_C.py`; the whole blade is visible, so its size calibrates the camera) puts it
about **3.1 in** (plus or minus 0.4; the poke tip, used as a check, reads 6 mm further ahead than the design). The model assumed
3.81, the coach's CAD says 4.67. The spread between photos (1.6 in) is far bigger than either fit's error, so the stick probably
sits differently in them: with the arms resting on the wheels, or with the blade against the FLOWER and the arms pushed back. The
pick happens in the second state, so the number that matters is the lip's distance from the face at the moment of the pick.
Tape measure, in both states, settles it; the simulator's box takes it.

An earlier version of this idea scaled the photo by eye without solving the camera and gave 1.6 and 2.7 in for the tip-to-lip gap;
that was wrong and is withdrawn.
