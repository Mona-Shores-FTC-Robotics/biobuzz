# Event review

The post-event review page: what both robots did at an event, from the official results and the
robots' own match logs. The first one is the Belding Qualifier (7 Nov 2026):
- public page: https://claude.ai/artifact/DVKzXZx26BWwodMMvVPUNu
- live dashboard: https://claude.ai/artifact/8gfTh8rVJV3aKC1yroG2Pk (Claude login needed)

Match logs reach GitHub through `tools/pull-logs` (Belding's are on `events/2026-11-belding`).

## Rebuild a review from its datasets

```
python3 tools/event-review/build/assemble.py tools/event-review/data/belding out/belding
python3 tools/event-review/build/build_web.py out/belding out/belding/review-web.html   # public, self-contained page
python3 tools/event-review/build/build.py out/belding out/belding/review.html             # print page
node tools/event-review/build/print_long.js $PWD/out/belding/review.html out/belding/review.pdf
```

`review-web.html` is what the public page publishes; the PDF is one long page. Both work offline
except the public page, which loads d3 7.9.0 from cdnjs.

## What is here

| Path | What |
|---|---|
| `page/review.html` | The page: layout, styles and every chart. `LOGO_IR`, `LOGO_UV` and `FIELD_IMG` are filled in by `assemble.py`. |
| `page/field-dark.webp` | The BIOBUZZ field from the team's Visualizer, under the floor-pickup maps. |
| `build/` | Assemble, print page, public page, PDF. |
| `data/belding/*.json` | Every dataset the Belding page draws, exactly as published. |
| `data/belding/shot-detector/` | Per-volley shot counts from the improved detector (PR #506). |
| `data/belding/reconcile/` | FLOWER pick reconciliation and lane-full episodes. |
| `archive/belding-scripts/` | The one-off scripts the Belding numbers came from, as they were. Not a pipeline: paths point at a session's scratch folder. |
| `archive/what-to-work-on-ideas.md` | Findings held back from the review for the "what to work on" sheet. |

## Where each Belding number came from

Match logs read with `tools/pull-logs/read_logs.py`. Times are medians unless the page says mean.

| Dataset | Section | Source |
|---|---|---|
| `results` (`kpis` is computed in the page from it), `field_opr`, `top_scores` | Hero, Against the field | FTCScout REST API, event `USMIBEQ` (OPR and averages: qualification matches) |
| `world_ranks` | In the world this season | ftcstats.org, as of 8 Nov |
| `pieces` | Pieces launched per match | Improved shot detector (PR #506), Auto and TeleOp per match |
| `feed_summary`, `cycle_groups`, `cycle_group_summary`, `cycle_steps` | Cycle time | Volley to volley from the logs; each match's first volley excluded; IR Lower final excluded from first-volley timing (Pinpoint lost) |
| `volley_pieces`, `volley_steps` | Cycle time cards, Full volley | Reliable 4-shot TeleOp volleys, improved detector |
| `drive_speed`, `drive_trips` | Driving | Odometry speed vs stick; trips full vs empty |
| `launch_volleys`, `launch_methods` | Launching | Each TeleOp volley by method: auto-drive (Square) or manual (operator FIRE); pre-spin; press to driving away |
| `spot_outcomes`, `square_issues`, `robots` (refused) | Auto-drive press chart | Each Square press: launched, stick moving, let go early, refused |
| `stick_cancels` | Moving-stick card | Stick value at the press and after the cancel (`CANCEL_STICK` 0.2) |
| `floor_pickup`, `floor_tiles` | Floor pickup | Issue #508: lane `in` arrivals outside FLOWER picks; tiles red-view, front of robot; frozen-pose stretches excluded |
| `flower_picks` | FLOWER picks | Issue #507 phases and press outcomes. Auto pieces per pick reconciled (see below) |
| `matches`, `robots`, `shot_recovery`, `energy`, `mechanisms` | Battery | Battery sampled once a second |
| `nectar`, `feed_nectar`, `feed_volleys` | (kept, not drawn on the final page) | |

**Auto pieces per FLOWER pick** (3.2 UV / 2.0 IR): per pick, the larger of the entrance colour
sensor's arrivals (including "arrived with the lane counted full") and the next volley's launches
less what was left in the lane, capped by the lane's space. All 4 preloads launched in every match
(drive team). The sensor alone reads 2.4 / 1.2: pieces off a FLOWER arrive nose to tail and read as
one. `/Lane/Full` is not used: it can read full while the stick holds a piece under the sensor.

## Next event

The goal is one command from a folder of match logs to all of the above, plus an event-vs-event
comparison. Until then, the next review is rebuilt the way Belding's was: the tables above say
what each number is.
