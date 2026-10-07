# Stepping the Saline stream

How the TIPs in `doc/saline-tip-measurements.md` were measured from the Day 2 stream
(<https://www.youtube.com/watch?v=lr6iMORuxMs>). Needs ffmpeg, numpy and Pillow; no robot.

1. Pull the AUTO windows at 720p60 (one command, on a machine signed in to YouTube):
   `yt-dlp -f 298 --force-keyframes-at-cuts -o "saline-%(section_start)d.mp4" --download-sections "*4:04:50-4:06:00" ... URL`
2. `python3 tipscan.py saline-*.mp4` decodes each clip at 60 fps and writes a `.npz` of per-frame signals:
   the overlay clock and score boxes (frame difference), and the red/blue HIVE frame's colour centroid and
   change count. It prints clock ticks (START is the first tick minus 1 s), score changes and motion bursts.
3. `python3 tipdrive2.py saline-*.mp4` lists every AUTO score change with the HIVE motion before it and writes
   a 20 fps strip of the HIVEs over that window to `sheets/`. The strips are what the times were read from:
   the automatic onset/stop (`tipmeasure.py`) is a first guess that people and robots in the crop upset.

The pixel boxes at the top of `tipscan.py` are for this stream's framing (HIVEs around x 470–950, clock at
575–700 × 630–700 in 1280×720). Another stream needs them re-read off one frame.

## Pieces, shots and robots (`doc/saline-piece-physics.md`)

The same clips, read for the simulator's other guesses. Each script prints its own usage line at the top.
`saline-corners.json` holds the four floor corners of the field read by eye on each clip's pre-START frame
(`grid.py` draws a labelled pixel grid to read them); `fieldmap.py` turns them into a frame-to-inches homography.
`track.py CLIP T0 T1 corners.json out.csv` finds POLLEN and NECTAR by colour at 20 fps and links them into tracks;
`trails.py` draws them on a frame; `rollsteady.py` fits each track's steady roll for its deceleration (the table in
the document; `saline-rolling-tracks.csv` is its input). `kymo.py` and `shotplot.py` follow POLLEN in flight around
the HIVE at 60 fps to count shots; `robotrun.py` tracks a robot's silhouette against the pre-START frame;
`spillstats.py` and `nectarin.py` read a spill's first touch and spread and the LOADING ZONE. Positions of pieces
read 5–7 in too far from the camera (their height) and pieces in the air far more: see the document's first section.
