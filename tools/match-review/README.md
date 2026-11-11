# Match review

One match's robot log and match video, lined up, for a Claude session (or anyone with Python
3.9+, ffmpeg and curl; Pillow for contact sheets). The procedure, and what the logs get wrong,
is the `match-review` skill: `.claude/skills/match-review/SKILL.md`.

```
python3 tools/match-review/match_review.py sync   USMIBEQ Q-1 20245
python3 tools/match-review/match_review.py frames USMIBEQ Q-1 20245 --auto 9.5 --span 1.2 --crop 600,150,320,240 --sheet
python3 tools/match-review/match_review.py clip   USMIBEQ Q-1 20245 --teleop 5 --len 6
```

| Step | What it does |
|---|---|
| `fetch` | The log from an `events/*` branch (`<team>/matches/<file>`), else the `logs/*` branch `matches.json` names; the video and its `.json` from the event's link-shared Drive folder. |
| `sync` | Finds the scoreboard's transition (2:01 → 0:08), looks around 30.45 s before it for our light leaving the alliance colour with the lane-full green at the log's times, prints both and the green it measured, and writes `sync.json`. `--play-at S` overrides. |
| `frames` | PNGs at log times (`--auto`/`--teleop`, seconds after that PLAY), `--span` s around each at `--fps`, cropped and lanczos-scaled; `--sheet` adds a captioned sheet. |
| `clip` | A short mp4 from a log time. |
| `still` | One captioned JPEG for the event's gallery (`--title`, a `--note` per time), at most 2000 px wide. Needs Pillow. |

Output: `TeamCode/build/match-review/<EVENT>_<MATCH>_<TEAM>/` (ignored by git). The light
finder is the replay tool's (`Replay.ps1`, `BiobuzzLight`), in Python.

## Adding an event's videos (mentor)

A cloud session cannot read the team shared drive, YouTube, or Drive files over 10 MB through
the connector. It can download a **link-shared** Drive file. After an event:

1. On a laptop, run the replay tool's **BIOBUZZ Prepare All Matches** so every match's clean
   source is in `2026-2027 FTC Biobuzz\Replay\sources\` (`<EVENT>_<match>_<team>.mp4` and `.json`).
2. Copy that event's `.mp4` and `.json` files into a folder in your own Drive, keeping the names.
   Share the folder: *General access → Anyone with the link → Viewer*.
3. Add the event to `videos.json` here, with the folder id (the part of its URL after
   `/folders/`), in a PR:
   `"USMIFRQ": {"name": "Fruitport Qualifier", "folder": "<folder id>"}`
4. Check: `python3 tools/match-review/match_review.py fetch <EVENT> <match> <team>` downloads it.
5. Optional: an evidence gallery for the event. Ask a session to make one (or copy Belding's,
   https://claude.ai/artifact/4NCKpRLVnWVvFTKtNzgEWS), and add its link as `"gallery"` on the
   event in `videos.json`. Sessions read and add to it as the `match-review` skill says. Share it
   from its Share menu so others can see it; viewers sign in to Claude.

The event and its matches must already be in `tools/advantagescope/replay/matches.json`
(`maintain/find_matches.py`). Folder ids live here, not in `matches.json`, because
`find_matches.py` rewrites each event's entry with a fixed set of keys.
