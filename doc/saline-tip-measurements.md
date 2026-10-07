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
| Rocker about halfway through its swing | 26924.978 | 8.73 s (1.35 s into the swing) |
| Rocker resting on its far stop; the spill already leaving the CELL, coming down on the robot parked under it | 26925.828 | **9.58 s** (2.20 s swing) |

The simulated spring hood's first shot is at about 2.5 s (2 s spin-up + 0.45 s a shot), so the simulator's
launcher is slightly quicker off the line than Infinity Tech, the event's top-ranked robot.
Three shots in 1.18 s is one every 0.4 s, close to the simulated 0.45 s.

**The wait.** 3.27 s passed between the third POLLEN landing and the rocker's first movement. The simulator has no
such wait: its rocker moves the instant the torque crosses the threshold, and its TIP 1 starts at about 4.6 s
against 7.4 s here. To confirm on the frames: nothing else entered the CELL in that gap, and whether the three
POLLEN were rolling toward the back skin during it (the calibration is done with pieces against the back skin,
so a piece landing near the front gives less torque until it rolls back).

**The swing.** 2.20 s from first movement to the stop: 1.35 s to halfway, 0.85 s for the second half. An
accelerating swing, as a rocker past its balance point should be, so the simulator's 1.0 s (`ASSUMED_TIP_SECONDS`,
`HiveTracker.Tuning.tipSeconds` NaN) is the wrong size, not the wrong shape. One TIP so far; a second (Q26 at
5:01:19, Frost RoboFalcons, also exactly three POLLEN) before the constants change.

**What it adds up to.** The event's best robot had TIP 1 complete 9.6 s after START. The simulator has the same
TIP complete at about 5.6 s (start 4.6 s, swing 1.0 s). Of the 4 s difference, 3.3 s is the wait between the
third POLLEN landing and the rocker moving, 1.2 s the slower swing, and the launcher's slower start is offset by
the simulated spin-up. The wait is the part the simulator does not model at all, and it moves every "wait for the
TIP" branch, the spill catch spots and the chance of a third TIP inside AUTO.
