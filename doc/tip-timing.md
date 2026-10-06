# How long a TIP takes, from video

Measured 6 Oct 2026 from the videos in the team's Drive folder of spill videos (https://drive.google.com/drive/folders/1rOUkQG-33c22QKkhaK9vAbaH6GRpNq1T): a YouTube test run
(https://youtu.be/Cw-tVeKDDIo, 1280×720, 60 fps, a cleared field and an on-screen TIP counter) and the team's
3 Oct phone films (IMG_1957 and IMG_1960, 4K at 120 fps). Frames: `sim-review/tip-*.jpg`.

**A TIP takes about 0.5–1.2 s, most often about 1 s**, from the CELL visibly starting to swing to it hitting the
other stop. The simulator now draws each TIP's time from **0.58–1.12 s** (`FieldSim.FILMED_TIP_SECONDS`, the middle
90% of what was filmed; the HIVE is still calibrated at 1.0 s, `HiveCalibration.ASSUMED_TIP_SECONDS`).

| Video | Starts to swing | Hits the stop | Time | Frames |
|---|---|---|---|---|
| YouTube, TIP at 31 s | ~31.35 s | ~32.5 s | ~1.15 s | `sim-review/tip-youtube-tip-31s.jpg` |
| YouTube, TIP at 94 s | ~94.75 s | ~95.3 s | ~0.55 s | `sim-review/tip-youtube-tip-94s.jpg` |
| IMG_1957 (120 fps) | ~2.45 s | ~3.40 s | ~0.95 s | `sim-review/tip-img1957-tip.jpg` |
| IMG_1960 (120 fps) | ~5.05 s | ~6.10 s | ~1.05 s | `sim-review/tip-img1960-tip.jpg` |

**How it was read.** Every TIP has two parts: a slow creep as the CELL leaves its stop (a few degrees, hard to
see), then a swing that speeds up and slams into the other stop, rebounds once and settles about 0.2 s later.
Both cameras move (the phone is handheld; the YouTube shot zooms), so the CELL's angle was judged against the
HIVE's own stand, frame by frame, not against the picture; an automatic track of the CELL's pixels was fooled by
the camera and by pieces being shot in. So each start is ±0.1 s, and a little late: the creep begins before the
swing is visible. The YouTube video has three more TIPs (about 2, 56 and 125 s) not yet read the same way.

**Why they differ.** Not known. A CELL loaded well past its tipping weight probably swings faster, but these
videos don't show how many pieces were in each.

**Does it matter?** The simulator's definition (`FieldSim`: leaving the stop to reaching the other one) includes
the creep, so it compares with the longer end of the range. Every TIP set to 0.6 or 1.2 s
(`BIOBUZZ_AUTO_TIP_SECONDS`) against each drawn from the range, Qual-PartnerShootsRight, 20 runs, on the
simulator as of 6 Oct 2026 12:15 UTC (pieces rolling as filmed, [rolling](rolling.md)):

| TIP time | Flat Intake | Rigid V |
|---|---|---|
| 0.6 s | 52.3 pts, TIP 3 in 1, G409 in 1 run | 62.3 pts, TIP 3 in 9, G409 in 5 runs |
| Each drawn from 0.58–1.12 s (the setting) | 53.5, TIP 3 in 2, G409 0 | 66.0, TIP 3 in 12, G409 in 1 run |
| 1.2 s | 51.0, TIP 3 in 0, G409 0 | 65.0, TIP 3 in 12, G409 in 1 run |

- The rigid V's lead holds at every TIP time (+10 to +14 points).
- A fast TIP brings G409 touches back: the Autos wait a fixed 500 ms after the TIP settles before driving into
  the spill, tuned for 1.0 s. Waiting for the spill itself (the CELL settled plus a margin) would hold for any TIP.

**Not changed:** `HiveTracker.Tuning.tipSeconds` stays NaN. It is robot code (the real HIVE tracker uses it to
decide a TIP is over), so setting it from these videos is a mentor's call; 1.0 s would be a fair value.

To add a video: put it in the Drive folder, then frame it with `ffmpeg` (`-vf "crop=...,fps=20,tile=8x4"`) and
read the swing against the stand as above.
