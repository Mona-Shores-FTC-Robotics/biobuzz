# TIPs measured off the Saline Preview stream

Frame-stepped on the Day 2 stream (<https://www.youtube.com/watch?v=lr6iMORuxMs>, 60 fps) by a mentor on 7 Oct
2026, reading the player's `currentTime` for each frame. Times are seconds into the stream; a frame is 0.017 s.
What each number feeds is in [saline-preview-day2.md](saline-preview-day2.md).

## Playoff M3: Infinity Tech + CyBugs (red) vs Hornet Hackers + CyberSmiths (blue), 128–41

| Event | Stream time | After START |
|---|---|---|
| AUTO starts (first movement on the field) | 26916.245 | 0 |
| First piece leaves Infinity Tech's launcher | 26919.181 | **2.94 s** |
| Third POLLEN lands in the raised red CELL (3 NECTAR + 3 POLLEN: the §12.3 threshold); a fourth shot, a little late, hits the rim and stays out | 26920.362 | 4.12 s |
| Rocker first seen moving | 26923.628 | **7.38 s** |

The simulated spring hood's first shot is at about 2.5 s (2 s spin-up + 0.45 s a shot), so the simulator's
launcher is slightly quicker off the line than Infinity Tech, the event's top-ranked robot.
Three shots in 1.18 s is one every 0.4 s, close to the simulated 0.45 s.

**The wait.** 3.27 s passed between the third POLLEN landing and the rocker's first movement. The simulator has no
such wait: its rocker moves the instant the torque crosses the threshold, and its TIP 1 starts at about 4.6 s
against 7.4 s here. To confirm on the frames: nothing else entered the CELL in that gap, and whether the three
POLLEN were rolling toward the back skin during it (the calibration is done with pieces against the back skin,
so a piece landing near the front gives less torque until it rolls back).
