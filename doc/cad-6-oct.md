# The 6 Oct 2026 CAD against the simulated Flat Intake

Read 6 Oct 2026 from `DHS Robot Copy.step` (the team's Drive folder) with `tools/cad/stepread.py`: the assembly
tree with every part's bounding box. Axes from the CAD: the wheels' lowest point is the floor, the roller intake
is the front. Sizes are bounding boxes, good to about 0.1 in; angles and clearances need the CAD itself.

| | CAD | Simulator's Flat Intake |
|---|---|---|
| Footprint | **15.2 in wide × 15.7 in long** | 14.5 × 14.5 |
| Height | 14.7 in (the intake's vertical column reaches the top) | body 6 in, which pieces bounce off |
| Intake | one horizontal roller across the front: a 240 mm shaft with seven 48 mm gecko wheels spanning 7.5 in; the mouth between the side plates is **9.4 in** | contact anywhere across a **14 in** mouth |
| Roller height | wheel bottom 2.8 in above the floor, top 4.7 in (bounding boxes; measured from the floor the wheel covers and resting NECTARs share it is **2.53 in**: [robot-cad.md](robot-cad.md) on `claude/robotics-meeting-notes-lq2y55`) | takes a piece whose top is at or under 5 in |
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

**In the simulator:** `RobotDesign.dhsCad()`, "DHS CAD (6 Oct)", with these numbers (CAD numbers count as
measured), and the intake options on it through the three Autos and the Rigid V: [intake-design.md](intake-design.md).
With the roller at the 2.84 in read it takes NECTAR but no POLLEN off the tiles (question 2), which costs the two
Stages Autos about 15 points; at the 2.53 in read it bites a POLLEN by 0.27 in, marginal.
