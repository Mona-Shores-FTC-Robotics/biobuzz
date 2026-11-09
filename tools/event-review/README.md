# Event review

The post-event review page: what both robots did at an event, from the official results and the
robots' own match logs. The first one is the Belding Qualifier (7 Nov 2026):
- public page: https://claude.ai/artifact/DVKzXZx26BWwodMMvVPUNu
- live dashboard: https://claude.ai/artifact/8gfTh8rVJV3aKC1yroG2Pk (Claude login needed)

Match logs reach GitHub through `tools/pull-logs` (Belding's are on `events/2026-11-belding`).

## After an event: one command

```
python3 tools/event-review/review.py --event USMIFRQ --logs <logs> --out out/fruitport --save-as fruitport
python3 tools/event-review/compare.py tools/event-review/data/belding tools/event-review/data/fruitport --out out/fruitport
```

`<logs>` holds `19429/` and `20245/`, each with that robot's match logs (`.wpilog` or `.wpilog.gz`)
directly or in `matches/`, the layout of an `events/<date>-<name>` branch (`practice/` is never read).
`review.py` names each log from FTCScout's schedule (the log's start time against the matches the
robot played; hub clocks drift, so the robot's logs are matched as a set), builds every dataset,
then the pages and the PDF:

| Output | What |
|---|---|
| `out/<event>/data/*.json` | Every dataset, the shape of `data/belding/` |
| `out/<event>/data/ftcscout/` | FTCScout's responses, reused on the next run (delete to refetch) |
| `out/<event>/data/shot-detector.json` | One row per gate opening from the #506 detector |
| `out/<event>/review-web.html` | The public page (publish this) |
| `out/<event>/review.html`, `review.pdf` | Print page and the one-long-page PDF (needs node + playwright) |
| `out/<event>/report.txt` | Which log is which match, and any dataset that could not be built and why |
| `data/<name>/` | With `--save-as <name>`: the datasets kept in the repo for `compare.py` |
| `compare.json`, `compare.html` | Two events side by side, per robot, with the change; numbers only |

Stdlib Python only (3.9+). FTCScout is fetched once and saved; if the network blocks
`api.ftcscout.org`, save its responses (the error names each file and URL) and pass `--offline`.

**During the event** (a live page): add `--live`. It refetches FTCScout every run (and falls back
to the saved copy, then to nothing, if that fails), skips the PDF, and puts "match logs so far"
in the title line. Rerun it as logs arrive; the same logs give the same datasets. A dataset with
too little data comes out empty and its numbers show a dash; nothing else fails. With 1 match per
robot the first-volley card and the Manual launch card are blank (no Manual volley yet); with 3,
only the Manual card; from 6 the page is complete. A robot with no logs yet blanks its own
numbers only. `report.txt` says which datasets were empty.

## The event config

`events/<EVENT CODE>.json`; an event without one runs on defaults (`events/USMIBEQ.json` is Belding's).

| Key | What |
|---|---|
| `name`, `date`, `match_day`, `world_as_of`, `world_source` | The page's title line and footer (else FTCScout's event name and end date) |
| `teams` | `{"19429": "Infrared", "20245": "Ultraviolet"}` |
| `playoff_names` | FTCScout playoff series number to the name the page uses (`"7": "Upper final"`); else `Playoff M7` |
| `match_names` | `"<team>/<log start>": "Q4"` to name a log by hand, or `"skip"` to leave it out; wins over the schedule |
| `frozen_pose` | `{team: {log: [[from_s, to_s]]}}`, log seconds where odometry froze: dropped from driving speed and trips |
| `pose_excluded` | Same shape: floor pieces whose position is not drawn on the tile map (`1e9` = to the end) |
| `exclude.first_volley`, `exclude.leave` | `[[team, match]]` left out of "First TeleOp volley" and the full-volley "moves off" time |
| `notes` | Why each exclusion is there, in a sentence |

`report.txt` lists every `Pinpoint lost` event a log has that the config does not cover yet: the
place to start looking for a frozen stretch (the pose holds still while the robot drives; it ends at
the next `Pinpoint: READY` that moves the pose).

## Does it reproduce Belding?

```
python3 tools/event-review/test_reproduce.py --logs <belding logs> --dry-run
```

runs the pipeline on Belding's 17 match logs with Belding's saved FTCScout responses
(`data/belding/ftcscout/`) and compares every dataset with `data/belding/` (never edited: it is the
baseline). Without `--logs` it fetches the logs from `events/2026-11-belding`. `--dry-run` also
builds live pages from the first 1, 3 and 6 logs per robot, reruns each to check the output is
identical, builds one with a robot and FTCScout missing, and renders every page to check it has
no script errors (`build/check_render.js`, when node + playwright are there). Tolerances and
known differences are in `reproduce_rules.py`, each with its reason.

| Result | Datasets |
|---|---|
| Exact | cycle_group_summary, cycle_groups, cycle_steps, drive_speed, drive_trips, energy, feed_nectar, feed_summary, feed_volleys, field_opr, floor_tiles, launch_methods, launch_volleys, matches, mechanisms, nectar, pieces, results, robots, shot_recovery, spot_outcomes, stick_cancels, top_scores, volley_pieces, volley_steps, world_ranks |
| Within tolerance | flower_picks (20245 TeleOp push 1.05 vs 1.04: the same mean, rounded the other way) |
| Not exactly | floor_pickup: `first_piece_s`, `to_full_s` (and the undrawn `collecting_share`) differ by 0.2–0.7 s; issue #508's script is not in the session record, so these follow its written definitions. Its counts, rate and `between_s` reproduce. |
| Not made | square_issues' undrawn hand-read fields (how soon the Pinpoint came back, refusals a later build would allow). The drawn ones reproduce. |
| Inputs, not logs | `world_ranks` is FTCScout's season quick-stats at the time of the run (Belding's: 8 Nov, saved in `data/belding/ftcscout/quick-stats-*.json`); a run today gives today's ranks. Awards come from FTCScout's awards list. |

Rules the pipeline encodes rather than numbers:
- **Pieces launched** use PR #506's detector, vendored in `review_lib/shots.py` (the robot's is
  Java). It reproduces all 160 Belding gate openings the #506 session sent, shot for shot.
- **Auto pieces per FLOWER pick**: per pick, the larger of the colour sensor's arrivals (with
  "counted full") and the next volley's launches less what was still in the lane, capped by the
  lane's space; the lane starts with 4 preloads, all launched in the first volley (the drive
  team's account at Belding), then follows the detector. This gives 32 / 26 (3.2 / 2.0).
- **NECTAR rejected** leaves out a reject whose hue was within 2° of the POLLEN range (the dim
  POLLEN read as red NECTAR in Q31).
- **First TeleOp volley** and **moves off** leave out the matches in the config's `exclude`.

## Rebuild a page from saved datasets

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
| `review.py`, `review_lib/` | The pipeline: logs and FTCScout to every dataset (one module per page section). |
| `events/` | One config per event. |
| `test_reproduce.py`, `reproduce_rules.py` | The Belding reproduction check and its tolerances. |
| `compare.py` | Two events side by side. |
| `build/` | Assemble, print page, public page, PDF, and `check_render.js` (script errors, blank charts). |
| `data/belding/*.json` | Every dataset the Belding page draws, exactly as published. `event.json` is its title line; `ftcscout/` the FTCScout responses it was built from. |
| `data/belding/shot-detector/` | Per-volley shot counts from the improved detector (PR #506). |
| `data/belding/reconcile/` | FLOWER pick reconciliation and lane-full episodes. |
| `archive/belding-scripts/` | The one-off scripts the Belding numbers came from, as they were. `review_lib/` is their port. |
| `archive/what-to-work-on-ideas.md` | Findings held back from the review for the "what to work on" sheet. |

## Where each Belding number came from

Match logs read with `tools/pull-logs/read_logs.py`. Times are medians unless the page says mean.

| Dataset | Section | Source |
|---|---|---|
| `results` (`kpis` is computed in the page from it), `field_opr`, `top_scores` | Hero, Against the field | FTCScout REST API, event `USMIBEQ` (OPR and averages: qualification matches) |
| `world_ranks` | In the world this season | FTCScout season quick-stats, as of 8 Nov |
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


## Comparing with Belding

Compare a new event against `data/belding-rebuilt/`, not `data/belding/`. It is Belding run through
this same pipeline, so both events use the same definitions (it differs from the published Belding
page only in the floor-pickup timings and the hand-read `square_issues` fields listed above):

```
python3 tools/event-review/compare.py tools/event-review/data/belding-rebuilt tools/event-review/data/<event> --out out/<event>
```
