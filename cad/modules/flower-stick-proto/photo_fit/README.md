# Reading the blade lip off the 28 Oct photos

`solve_B.py` solves the camera pose (OpenCV solvePnP) from points on the poker's roof whose positions are known from the part
(corners, the two 12 mm holes, the relief holes, the rear screw), then back-projects the blade's tail end (known to be 50.8 mm
behind its lip) onto the tile plane. `overlay_B.py` draws the photo as a true top view at the roof's height and at the blade's
height, with the model's geometry on top. `solve_A.py` does the same on a second, more oblique photo.

Check: the poke tip, not used in the fit, comes out at x = 33.2 mm against a design 33.

Result: the blade's lip is **4.75 in (4.6 to 5.0) ahead of the intake face** (photo B; photo A says 4.4 to 5.0, from its focal
length), the coach's CAD says 4.67, and the model had 3.81. The poke tip is about 3.45 in behind the lip; the model's best is
2.5, so the poke wants about 2.25 in of reach, not 1.3. What moves the answer: the channel's height above the tile (6.55 in
assumed; plus or minus 0.5 in moves the lip about 0.1 in), the photo's focal length (small on the overhead photo).

An earlier version of this idea scaled the photo by eye without solving the camera and gave 1.6 and 2.7 in for the tip-to-lip gap;
that was wrong and is withdrawn.
