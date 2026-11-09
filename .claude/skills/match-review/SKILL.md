---
name: match-review
description: Review a match from its robot log and its match video, lined up - what the robot did, when, and whether the log is telling the truth. Use for "what happened in Q-14?", "review the match", "check the video", "look at the logs and the replay", "match video", "replay", "did it really shoot 4?", "was the lane really full?", or any claim from a log that should be checked against video.
---

# Match review: log and video together

The log says what the robot thought; the video says what happened. Read both, lined up, and
when they disagree, the video wins. Everything below was checked on video at Belding (Nov 2026).

## 1. Get the match, lined up

```
python3 tools/match-review/match_review.py sync USMIBEQ Q-1 20245
```

Event code, match as `tools/advantagescope/replay/matches.json` writes it (`Q-1`, `M-7`), team.
`matches.json` maps match ↔ team ↔ log ↔ alliance. The helper fetches the log (an `events/*`
branch, else the `logs/*` branch named in `matches.json`), downloads the clean video, finds the
sync and prints its evidence. Output: `TeamCode/build/match-review/<EVENT>_<MATCH>_<TEAM>/`
(`log.wpilog.gz`, `video.mp4`, `sync.json`). Then:

```
python3 tools/match-review/match_review.py frames USMIBEQ Q-1 20245 --auto 9.5 14.2 --span 1.2 --fps 20 --crop 600,150,320,240 --sheet
python3 tools/match-review/match_review.py frames USMIBEQ Q-1 20245 --teleop 30 --scale 0.5 --sheet   # whole field, to find the robot
python3 tools/match-review/match_review.py clip   USMIBEQ Q-1 20245 --teleop 5 --len 6 --crop 320,100,640,400
```

Times are seconds after that period's PLAY in the log. Crops are 1280×720 pixels, scaled 2× by
lanczos by default. `--sheet` writes a captioned contact sheet (Pillow) on a light background:
read it with the Read tool. Look at a whole-field frame first to choose the crop.

## 2. Check the sync before believing it

The sync line names how it was found. Accept it only after **two** agreeing events:

- **Our light** (the method that counts). The indicator light shows the alliance colour until the
  driver's PLAY (= the log's Auto PLAY, the first `PLAY: <colour> alliance` event), goes off, and
  turns green at every `Lane: FULL` — the first about 0.9 s after PLAY, from the preloads. The
  helper prints when the light left the colour and the green it measured against the log's; in
  video the green comes on about 0.1 s after the log's FULL.
- **A second event**, by eye: a later green, a ball in the air at a counted shot, or the scoreboard
  clock (it reads 2:30 at the field's Auto start). The helper prints candidate times.
- **The scoreboard** switches 2:01 → 0:08 (the transition) at the field's end of Auto. In
  qualifiers the log's Auto PLAY ≈ switch − 30.45 s. **In the Belding playoffs our PLAY was 1–4 s
  later than that**, so the switch alone is wrong there; the helper uses it only to know where to
  look for the light. "scoreboard only" in the sync line means: find PLAY by eye and rerun with
  `--play-at <seconds into video.mp4>`.
- TeleOp PLAY is `PLAY: field forward = driver forward`; the helper places it by the log's
  offset from Auto PLAY. Check it on a TeleOp green before reading TeleOp closely.

## 3. Read the log honestly

`python3 tools/pull-logs/read_logs.py summary|picks|launcher|series|keys log.wpilog.gz`.

- **The shot counter undercounts**, worst in Auto volleys right after a FLOWER pick (logged 2,
  video 4–5). Count shots on video.
- **Lane counts run low**: the lane colour sensor merges two touching pieces.
- **`Lane: FULL`** means a piece sat under the entry sensor for 1 s. That is a full lane, or two
  pieces wedged at the intake, or the robot pushing intake-first into pieces at a wall. Look before
  calling it any of them.
- The logs call the SCORING FLOWER "FAR". Always write **SCORING** (the other is WALL).

## 4. Read the video honestly

- The stream camera sometimes shows the other field, or people stand in front: write
  **CAN'T TELL**, never a guess.
- Use 10–30 fps frames and 2× lanczos crops around the robot. Caption stills with Pillow
  (ffmpeg's `drawtext` breaks on `:`), on a light background.
- Old-style clips (made before 9 Nov 2026) have black boxes and a burned-in countdown in their
  first ~40 s; the clean sources the helper downloads do not. In Belding's M-7 and M-10 our robots
  played each other: both teams' files are the same footage, each synced to its own light.

## Where the video comes from

The replay tool (`tools/advantagescope/replay`, "Prepare All Matches") uploads each match's
clean stream video to the team shared drive, `2026-2027 FTC Biobuzz\Replay\sources\
USMIBEQ_<match>_<team>.mp4`, with a `.json` beside it (`{"url", "from": stream seconds at file
start}`). That drive is members-only: a cloud session cannot download from it, the Google Drive
connector downloads only files under 10 MB, and YouTube refuses cloud containers (bot check).
So a mentor copies an event's files to a **link-shared** folder and lists it in
`tools/match-review/videos.json`; the helper downloads from there. No folder for the event yet:
ask the mentor for one (`tools/match-review/README.md` says how) rather than trying another source.
