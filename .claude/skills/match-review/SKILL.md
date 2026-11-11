---
name: match-review
description: Review a match from its robot log and its match video, lined up - what the robot did, when, and whether the log is telling the truth - and use or add to the event's video-evidence gallery. Use for "what happened in Q-14?", "review the match", "check the video", "look at the logs and the replay", "match video", "replay", "did it really shoot 4?", "was the lane really full?", "we want to improve X, use the gallery", "add this to the gallery", "video evidence", any claim from a log that should be checked against video, or "the fix list".
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
- **The scoreboard** switches 2:01 → 0:08 (the transition) at the field's end of Auto. Our PLAY
  is often later than the field's start: the log's Auto PLAY was switch − 30.45 s in most Belding
  qualifiers, but 1–4 s later in the playoffs and about 2 s later in Q-14. So the switch alone is
  never the sync; the helper uses it only to know where to look for the light. "scoreboard only"
  in the sync line means: find PLAY by eye and rerun with `--play-at <seconds into video.mp4>`.
  A late PLAY also means the robot's own 30 s runs past the field's Auto: check what it was doing
  at the switch.
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
- Logs from before 12 Nov 2026 call the SCORING FLOWER "FAR" (`read_logs.py` prints SCORING for both). Always write **SCORING** (the other is WALL).

## 4. Read the video honestly

- The stream camera sometimes shows the other field, or people stand in front: write
  **CAN'T TELL**, never a guess.
- Use 10–30 fps frames and 2× lanczos crops around the robot. Caption stills with Pillow
  (ffmpeg's `drawtext` breaks on `:`), on a light background.
- Old-style clips (made before 9 Nov 2026) have black boxes and a burned-in countdown in their
  first ~40 s; the clean sources the helper downloads do not. In Belding's M-7 and M-10 our robots
  played each other: both teams' files are the same footage, each synced to its own light.

## 5. The gallery: the event's video evidence

Each event has a gallery of captioned stills, each lined up with a log: the `gallery` link for the
event in `tools/match-review/videos.json` (Belding: "Belding Video Evidence"). It is a claude.ai
artifact on the mentor's account; a session reads and writes it with the `ArtifactData` tool,
collection `photos`, one document per still.

**Asked to improve something** ("we want better FLOWER picks, use the gallery"): read the gallery
first, then go to video only for what it lacks.

```
ArtifactData list  url=<gallery>  collection=photos  out_dir=<scratchpad>/gallery
```

Each document: `order` (sort position), `match`, `robot` (`19429`, `20245` or `Both`), `topic`,
`title` (one plain sentence), `caption` (log times and what the picture shows), `url`
(`/_blob/<id>`), `source`. Filter by `topic` and `match`; topics are what was looked at
("Shot counter", "Full lane calls", "FLOWER picks", "Intake jams", "Auto", ...). A caption is a
claim checked once; when the change you are planning rests on it, re-check it with step 1 before
building on it. To see a picture, `Artifact read` with `url=<gallery>` and `path=<id>` saves it
locally.

**Adding stills**, when the mentor asks or agrees (a write is visible to everyone who can open the
gallery):

1. Make each one with the helper (a captioned JPEG, at most 2000 px wide; aim under 300 KB):
   ```
   python3 tools/match-review/match_review.py still USMIBEQ Q-14 20245 --auto 23.0 23.5 23.9 24.4 \
       --crop 600,140,340,320 --scale 1.5 --cols 2 \
       --title "Q-14 20245, Auto volley 2: log counts 2 shots, video shows 4" \
       --note "piece 1 rising" --note "piece 2" --note "piece 3" --note "piece 4"
   ```
   Look at it before uploading.
2. Upload: `Artifact` publish with `url=<gallery>`, `asset: true`, `file_paths: [the JPEGs]`. Use
   each result's `/_blob/<id>` url verbatim.
3. Read the collection, then one `ArtifactData batch` of `set`s, one per still: a short slug
   `doc_id` (`q14-auto-volley2`), an `order` next to the stills on the same topic and unused, an
   existing `topic` when one fits, `source: "match-review helper"`. Never change or delete
   another document unless asked.

The gallery starts private. Others see it only once the mentor shares it (Share menu), and
viewers must be signed in to Claude: the pictures load from its database. A new event gets a new
gallery only when the mentor asks for one; add its link to `videos.json` in a PR.

## 6. The fix list: what the team is doing about it

Each event can also have a fix list: the `fixlist` link for the event in
`tools/match-review/videos.json` (Belding: "Belding Fix List"). It is a claude.ai artifact on the
mentor's account, read and written with `ArtifactData`, collection `items`, one document per action.

```
ArtifactData list  url=<fixlist>  collection=items  out_dir=<scratchpad>/fixlist
```

Each document: `title` (an action: what to do), `detail` (**Do**, **Why** with the evidence,
**Done when**), `area` (`Hardware`, `Software`, `Drive practice`, `Field and scoring`), `robot`,
`status` (`todo` = not done, `done` = resolved, `skipped`), `priority` (true = Top 5), `order`,
`link` and `linkLabel` (its issue or PR).

- **Before reviewing a match,** read the open items: a review that confirms, refutes or narrows one
  says which, by title.
- **Write only when the mentor asks or agrees.** A new item is an action, never a finding: a verb
  first in `title`, and `detail` in the Do / Why / Done when shape. Mark an item `done` or
  `skipped` only on the mentor's word or with merged evidence (its PR), and say why in `detail`.
  Read the document first and pin each write to its `version`.

## Where the video comes from

The replay tool (`tools/advantagescope/replay`, "Prepare All Matches") uploads each match's
clean stream video to the team shared drive, `2026-2027 FTC Biobuzz\Replay\sources\
USMIBEQ_<match>_<team>.mp4`, with a `.json` beside it (`{"url", "from": stream seconds at file
start}`). That drive is members-only: a cloud session cannot download from it, the Google Drive
connector downloads only files under 10 MB, and YouTube refuses cloud containers (bot check).
So a mentor copies an event's files to a **link-shared** folder and lists it in
`tools/match-review/videos.json`; the helper downloads from there. No folder for the event yet:
ask the mentor for one (`tools/match-review/README.md` says how) rather than trying another source.
