# FLOWER pick study

How many pieces each Autonomous FLOWER pick gets, per robot and per stick version, counted from
the match log and on match video and reconciled into one number. The bar is 4 every pick.

| File | What it is |
|---|---|
| `log-picks.csv` | One row per Auto PickFlower card, from every event match log and every practice Auto log (`tools/flower-picks/flower_picks.py logs --practice`). |
| `video-counts.json` | The video count of every match pick that has video, with its confidence, note and clip time. Written by hand or by a session that watches the clip (`tools/match-review`). |
| `findings.html` | The findings and recommendation shown at the top of the study page. Hand-written. |
| `picks.json` | The two joined (`flower_picks.py report`). The page is built from it (`tools/flower-picks/build_page.py`). |

Re-run after a practice or an event:

```
git fetch origin 'refs/heads/events/*:refs/remotes/origin/events/*' 'refs/heads/logs/*:refs/remotes/origin/logs/*'
python3 tools/flower-picks/flower_picks.py logs --practice
python3 tools/flower-picks/flower_picks.py report
python3 tools/flower-picks/build_page.py
```

A new event's match picks show "log only (no video yet)" until they are added to `video-counts.json`.
The published page: https://claude.ai/artifact/LdkDaeQr8QBvjykSfubeBX (private to the mentor until shared).
