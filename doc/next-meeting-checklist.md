# To measure on the robot: the simulator's open numbers

Written 6 Oct 2026 from the simulator's studies (`doc/what-the-simulator-knows.md`). Each is a guess the
Autos' results rest on; a measurement replaces it. In the order the results move:

**Launcher, from the three firing spots** (`S_FIRE` 57.5, 24 facing 90°; `N_FIRE` 57.5, 119 facing 270°;
`N_LOW` 57.5, 114 facing 270°; Pedro inches):
1. **Accuracy standing still**: 20 POLLEN and 20 NECTAR from each spot, count what lands in the raised CELL.
   The simulator assumes about 5 in 6; the mentor's target is 99%. With no spread every shot that matters scores.
2. **Shots per second**: time a volley of 4. The simulator assumes 0.45 s a shot; faster brings the end-of-Auto
   loads in sooner (where TIP 3 lives).
3. **Spin-up**: from the launcher off to the first good shot. Assumed 2 s.

**Intake** (`doc/cad-6-oct.md`, `doc/intake-design.md` when it lands):
4. **Time per ball** at the throat: feed 4 POLLEN in a row, time them. Assumed 0.35 s; in the Autos 16–25
   pieces a run arrive while the intake is busy and bounce off.
5. **Does the roller lift a POLLEN off the tiles?** The CAD's roller bottom is 2.84 in up; a POLLEN is 2.8 in.
6. **Does a ball at the edge of the mouth get pulled in?** (vectored rollers) And how high a bouncing ball the
   rollers still catch: 15 pieces a run arrive with their top above 5 in.

**CAD questions for the designer** (`doc/cad-6-oct.md`): the 9.4 in mouth; the roller height; the launcher belt
(16T on a 312 rpm motor driving 41T on the wheel gears the flywheel down).

**Robot code**: `HiveTracker.Tuning.tipSeconds` is 0.8 s, the median of eleven event TIPs (7 Oct 2026); no
longer open.

**Already measured from video, no robot needed:** TIP time, rolling, bounce, where a spill lands.
