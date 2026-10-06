# The 6 Oct 2026 CAD against the simulated Flat Intake

Read 6 Oct 2026 from `DHS Robot Copy.step` (the team's Drive folder) with `tools/cad/stepread.py`: the assembly
tree with every part's bounding box. Axes from the CAD: the wheels' lowest point is the floor, the roller intake
is the front. Sizes are bounding boxes, good to about 0.1 in; angles and clearances need the CAD itself.

| | CAD | Simulator's Flat Intake |
|---|---|---|
| Footprint | **15.2 in wide × 15.7 in long** | 14.5 × 14.5 |
| Height | 14.7 in (the intake's vertical column reaches the top) | body 6 in, which pieces bounce off |
| Intake | one horizontal roller across the front: a 240 mm shaft with seven 48 mm gecko wheels spanning 7.5 in; the mouth between the side plates is **9.4 in** | contact anywhere across a **14 in** mouth |
| Roller height | wheel bottom 2.8 in above the floor, top 4.7 in | takes a piece whose top is at or under 5 in |
| Piece path | along the floor down the centre line to the launcher at the back | the same idea |
| Launcher | rear; two 96 mm gecko wheels pinch the ball and throw it up into a printed hood; exit about 10 in up, 3 in behind the centre | 12 in up, 4 in behind, 75° |
| Drive | 96 mm mecanum, belt-driven | not modelled |

**To raise with the designer**

1. The mouth is 9.4 in, not 14: the simulated Flat Intake has been more generous than the design. "Beside the
   mouth" misses go up and catches go down with the real mouth. The vectored rollers flanking a single opening
   (mentor, 6 Oct) are not in this CAD.
2. The roller clears the floor by 2.84 in and a POLLEN is 2.8 in tall: as drawn the gecko wheels barely touch a
   POLLEN on the tiles (NECTAR, 3.6 in, gets a 0.8 in squeeze). Lower shaft, bigger wheels, or a ramp?
3. The launcher belt as drawn gears the flywheel down: a 16-tooth pulley on the 312 rpm motor drives a 41-tooth
   on the wheel shaft, about 120 rpm at a 96 mm wheel. A placeholder motor, or the pulleys the other way round?

**In the simulator:** not yet a design. Adding one, "DHS CAD (6 Oct)", with these numbers and running the three
Autos and the shape matrix on it would show what the real mouth costs against the Flat Intake. CAD numbers count
as measured.
