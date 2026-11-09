#!/usr/bin/env python3
"""One command from a folder of match logs to the event review: every dataset, the page and the PDF.

  python3 tools/event-review/review.py --event USMIFRQ --logs <dir> --out <dir> [--live]

<dir> holds 19429/ and 20245/, each with the robot's match logs (.wpilog or .wpilog.gz) directly
or in a matches/ subfolder (the layout of the events/<date>-<name> branches; practice/ is never
read). The event's own settings are tools/event-review/events/<EVENT>.json (README: "The event config");
an event without one runs on the defaults and says so.

Writes <out>/data/*.json (every dataset, the shape of data/belding/), <out>/data/ftcscout/ (the
FTCScout responses, reused on the next run), <out>/review-web.html (the public page),
<out>/review.html and <out>/review.pdf (print), and <out>/report.txt (what was built from what).

--live   for a page during the event: refetches FTCScout every run (falls back to what was
         saved, then to nothing, if the network is down), skips the PDF, and never fails on a
         dataset with too little data yet: it comes out empty and the page shows a dash.
--offline  use only saved FTCScout files.
"""
import argparse
import json
import os
import shutil
import subprocess
import sys
import time
import traceback

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

from review_lib import logs, shots, volleys, battery, launching, pieces, driving, pickup, ftc, schedule  # noqa: E402

TEAMS = {'19429': 'Infrared', '20245': 'Ultraviolet'}
# Every dataset the page and build.py read. A dataset that cannot be made is written as [].
DATASETS = ['matches', 'robots', 'shot_recovery', 'mechanisms', 'energy', 'volley_steps', 'spot_outcomes', 'field_opr',
            'top_scores', 'results', 'cycle_steps', 'cycle_groups', 'cycle_group_summary', 'pieces', 'nectar', 'world_ranks',
            'square_issues', 'drive_speed', 'feed_summary', 'feed_volleys', 'feed_nectar', 'drive_trips', 'launch_methods',
            'volley_pieces', 'launch_volleys', 'stick_cancels', 'floor_pickup', 'floor_tiles', 'flower_picks']


def load_config(event, path=None):
    path = path or os.path.join(HERE, 'events', '%s.json' % event)
    cfg = json.load(open(path)) if os.path.exists(path) else {}
    cfg.setdefault('event', event)
    cfg.setdefault('season', 2026)
    cfg.setdefault('teams', TEAMS)
    cfg['_path'] = path if os.path.exists(path) else None
    return cfg


class Report:
    def __init__(self):
        self.lines = []

    def __call__(self, *a):
        s = ' '.join(str(x) for x in a)
        self.lines.append(s)
        print(s, flush=True)


def load_matches(logdir, cfg, fs, say, max_per_team=None):
    """{team: [Match]} named and ordered from the schedule; logs that are not a match are dropped."""
    names = cfg.get('match_names', {})
    try:
        fmatches = fs.matches() if fs else []
        tz = (fs.event() or {}).get('timezone') if fs else None
    except ftc.FetchError as e:
        say('  FTCScout schedule unavailable (%s); naming matches from the config or by order' % e)
        fmatches, tz = [], cfg.get('timezone')
    by_team = {}
    for team in cfg['teams']:
        paths = logs.find_logs(logdir, team)
        if max_per_team is not None:
            paths = paths[:max_per_team]
        if not paths:
            say('  %s: no match logs' % team)
            by_team[team] = []
            continue
        ours = ftc.our_matches(fmatches, team) if fmatches else []
        lognames = [os.path.basename(p)[:19] for p in paths]
        got = schedule.assign(lognames, ours, tz or cfg.get('timezone')) if ours else {}
        ms = []
        for p in paths:
            ln = os.path.basename(p)[:19]
            over = names.get('%s/%s' % (team, ln))
            if over == 'skip':
                say('  %s %s: skipped (config)' % (team, ln))
                continue
            m = logs.Match(p, team)
            if m.auto_start is None or m.tele_start is None:
                say('  %s %s: no Auto and TeleOp in this log, not a match; skipped' % (team, ln))
                continue
            if over:
                m.name = over
            elif ln in got:
                x, al = got[ln]
                m.name, m.alliance = ftc.match_label(x, cfg.get('playoff_names', {})), al
            elif ours:
                say('  %s %s: matches no %s match on FTCScout; skipped (name it in the config to keep it)' % (team, ln, team))
                continue
            else:
                m.name = 'M%d' % (len(ms) + 1)
            if m.alliance is None:
                m.alliance = m.log_alliance
                if over and fmatches:
                    x = next((x for x, al in ours if ftc.match_label(x, cfg.get('playoff_names', {})) == over), None)
                    if x is not None:
                        m.alliance = next(al for xx, al in ours if xx is x)
            ms.append(m)
        for i, m in enumerate(ms):
            m.order = i + 1
        say('  %s: %s' % (team, ', '.join('%s=%s' % (m.log[11:], m.name) for m in ms) or 'none'))
        for m in ms:
            lost = [t for t, e in m.events if e.startswith('Pinpoint lost')]
            if lost and m.log not in cfg.get('frozen_pose', {}).get(team, {}):
                say('    %s %s: Pinpoint lost at %s s; not in the config\'s frozen_pose yet'
                    % (team, m.name, ', '.join('%.1f' % t for t in lost)))
        by_team[team] = ms
    return by_team


def build_datasets(by_team, cfg, fs, say):
    """Every dataset, each made on its own: one that fails is [] and the report says why."""
    D = {k: [] for k in DATASETS}
    present = {t: ms for t, ms in by_team.items() if ms}

    def run(label, fn):
        try:
            r = fn()
            if r is not None:
                D.update(r)
        except Exception as e:  # too little data for this one, or a log without its keys
            say('  %s: not built (%s: %s)' % (label, type(e).__name__, e))
            if os.environ.get('REVIEW_DEBUG'):
                traceback.print_exc()

    vols, V = [], []
    for team, ms in present.items():
        for m in ms:
            vols += shots.volleys(m)
            V += volleys.build(m)
    D['_shot_detector'] = vols
    if present:
        run('battery and presses', lambda: battery.build(present))
        base = D.get('spot_outcomes', [])
        spot = {}

        def so():
            out, sp = launching.spot_outcomes(present, base, vols)
            spot.update(sp)
            return {'spot_outcomes': out}
        run('spot_outcomes', so)
        run('cycles', lambda: launching.cycles(V, list(present)))
        run('feed', lambda: launching.feed(present, V, cfg))
        run('launching', lambda: launching.launch(present, V, spot))
        run('volley_pieces', lambda: {'volley_pieces': launching.volley_pieces(present, V, vols, cfg)})
        run('stick_cancels', lambda: {'stick_cancels': launching.stick_cancels(present)})
        run('square_issues', lambda: {'square_issues': launching.square_issues(present, D['robots'])})
        run('pieces', lambda: pieces.build(present, vols))
        run('drive_speed', lambda: {'drive_speed': driving.speed(present, cfg)})
        run('drive_trips', lambda: {'drive_trips': driving.trips(present, cfg)})
        run('flower_picks', lambda: {'flower_picks': pickup.flower(present, vols)})
        run('floor', lambda: pickup.floor(present, V, cfg))
    if fs is not None:
        crashes = {t: sum(1 for m in ms for _, e in m.events if 'CRASH' in e) for t, ms in by_team.items()}
        parks = {t: sum(1 for m in ms if any('park' in e.lower() and e.startswith('auto: path')
                                             for tt, e in m.events if m.auto_start <= tt <= (m.auto_end or 1e9)))
                 for t, ms in by_team.items()}
        played = {t: len(ms) for t, ms in by_team.items()}
        run('FTCScout results', lambda: ftc.datasets(fs, cfg, list(cfg['teams']), crashes, parks, played))
    return D


def write(D, outdir):
    os.makedirs(outdir, exist_ok=True)
    for k, v in D.items():
        name = 'shot-detector.json' if k == '_shot_detector' else '%s.json' % k
        with open(os.path.join(outdir, name), 'w') as f:
            json.dump(v, f, indent=1)
            f.write('\n')


def event_meta(cfg, fs, say, by_team=None, live=False):
    """event.json: the page's title line and footer, from FTCScout and the config."""
    ev = {}
    try:
        ev = fs.event() if fs else {}
    except ftc.FetchError:
        pass
    meta = {'code': cfg['event'], 'name': cfg.get('name') or ev.get('name') or cfg['event'],
            'date': cfg.get('date') or ev.get('end') or '', 'world_as_of': cfg.get('world_as_of', ''),
            'match_day': cfg.get('match_day', ''), 'world_source': cfg.get('world_source', '')}
    if live and by_team is not None:
        # A live page says how much of the event it holds.
        n = ' · '.join('%s %d' % (cfg['teams'].get(t, t), len(ms)) for t, ms in by_team.items())
        meta['date'] = ('%s · match logs so far: %s' % (meta['date'], n)).strip(' ·')
    meta['matches_logged'] = {t: len(ms) for t, ms in (by_team or {}).items()}
    return meta


def build_pages(datadir, outdir, pdf, say):
    b = os.path.join(HERE, 'build')
    site = os.path.join(outdir, 'site')
    for cmd in ([sys.executable, os.path.join(b, 'assemble.py'), datadir, site],
                [sys.executable, os.path.join(b, 'build_web.py'), site, os.path.join(outdir, 'review-web.html')],
                [sys.executable, os.path.join(b, 'build.py'), site, os.path.join(outdir, 'review.html')]):
        r = subprocess.run(cmd, capture_output=True, text=True)
        if r.returncode:
            say('  page build failed: %s\n%s' % (' '.join(cmd[1:2]), r.stderr[-2000:]))
            return False
    shutil.copy(os.path.join(site, 'd3.min.js'), outdir)   # review.html loads it from beside itself
    say('  pages: %s, %s' % (os.path.join(outdir, 'review-web.html'), os.path.join(outdir, 'review.html')))
    if not pdf:
        return True
    node = shutil.which('node')
    if not node:
        say('  PDF skipped: node not found')
        return True
    r = subprocess.run([node, os.path.join(b, 'print_long.js'), os.path.abspath(os.path.join(outdir, 'review.html')),
                        os.path.abspath(os.path.join(outdir, 'review.pdf'))], capture_output=True, text=True, cwd=b)
    if r.returncode:
        say('  PDF skipped (playwright missing or failed): %s' % (r.stderr.strip().splitlines() or ['?'])[-1])
    else:
        say('  PDF: %s (%s)' % (os.path.join(outdir, 'review.pdf'), r.stdout.strip()))
    return True


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--event', required=True, help='FTC event code, e.g. USMIBEQ')
    ap.add_argument('--logs', required=True, help='folder with 19429/ and 20245/ match logs')
    ap.add_argument('--out', required=True, help='output folder')
    ap.add_argument('--config', help='event config (default events/<EVENT>.json)')
    ap.add_argument('--ftcscout', help='folder of saved FTCScout responses to use (default <out>/data/ftcscout)')
    ap.add_argument('--live', action='store_true', help='mid-event: refetch results, skip the PDF, tolerate gaps')
    ap.add_argument('--offline', action='store_true', help='use only saved FTCScout files')
    ap.add_argument('--no-pages', action='store_true', help='datasets only')
    ap.add_argument('--no-pdf', action='store_true')
    ap.add_argument('--save-as', metavar='NAME', help='also copy the datasets and FTCScout responses to '
                    'tools/event-review/data/NAME/ (the folder compare.py and the next review read)')
    ap.add_argument('--max-matches', type=int, help='read only each robot\'s first N match logs (dry runs)')
    a = ap.parse_args(argv)
    say = Report()
    t0 = time.time()
    cfg = load_config(a.event, a.config)
    say('event %s, config %s' % (a.event, cfg['_path'] or 'none (defaults)'))
    datadir = os.path.join(a.out, 'data')
    cache = a.ftcscout or os.path.join(datadir, 'ftcscout')
    fs = ftc.FTCScout(cfg['season'], a.event, cache, offline=a.offline)
    if a.live and not a.offline:
        fs = LiveFTCScout(fs, say)
    say('logs:')
    by_team = load_matches(a.logs, cfg, fs, say, a.max_matches)
    say('datasets:')
    D = build_datasets(by_team, cfg, fs, say)
    D['event'] = event_meta(cfg, fs, say, by_team, a.live)
    write(D, datadir)
    empty = [k for k in DATASETS if not D.get(k)]
    say('  wrote %d datasets to %s%s' % (len(DATASETS), datadir, ('; empty: ' + ', '.join(empty)) if empty else ''))
    if a.save_as:
        dest = os.path.join(HERE, 'data', a.save_as)
        if os.path.abspath(dest) == os.path.abspath(os.path.join(HERE, 'data', 'belding')):
            raise SystemExit('data/belding is the published baseline; save under another name')
        shutil.copytree(datadir, dest, dirs_exist_ok=True)
        say('  saved to %s' % dest)
    ok = True
    if not a.no_pages:
        say('pages:')
        ok = build_pages(datadir, a.out, pdf=not (a.live or a.no_pdf), say=say)
    say('done in %.0f s' % (time.time() - t0))
    with open(os.path.join(a.out, 'report.txt'), 'w') as f:
        f.write('\n'.join(say.lines) + '\n')
    return 0 if ok else 1


class LiveFTCScout:
    """Refetches the event's results every run; on failure uses the saved copy, then nothing."""

    def __init__(self, fs, say):
        self.fs, self.say, self.code = fs, say, fs.code

    def _get(self, name, fn):
        f = os.path.join(self.fs.dir, name)
        old = f + '.prev'
        if os.path.exists(f):
            os.replace(f, old)
        try:
            r = fn()
            if os.path.exists(old):
                os.remove(old)
            return r
        except ftc.FetchError as e:
            if os.path.exists(old):
                os.replace(old, f)
                self.say('  FTCScout %s: using the saved copy (%s)' % (name, str(e)[:80]))
                return json.load(open(f))
            raise

    def event(self):
        return self._get('event.json', self.fs.event)

    def matches(self):
        return self._get('matches.json', self.fs.matches)

    def teams(self):
        return self._get('teams.json', self.fs.teams)

    def awards(self, team):
        return self._get('awards-%s.json' % team, lambda: self.fs.awards(team))

    def quick_stats(self, team):
        return self.fs.quick_stats(team)   # a season snapshot: fetched once, kept


if __name__ == '__main__':
    sys.exit(main())
