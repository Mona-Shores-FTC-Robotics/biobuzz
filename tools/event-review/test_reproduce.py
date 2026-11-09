#!/usr/bin/env python3
"""Reproduction check: run review.py on the Belding logs and compare every dataset with the
published ones in data/belding/ (the baseline; never edited).

  python3 tools/event-review/test_reproduce.py [--logs DIR] [--dry-run]

--logs   folder with 19429/ and 20245/ Belding match logs. Default: fetched from the
         events/2026-11-belding branch into the scratch folder (git needed).
--dry-run  also builds the page from the first 1, 3 and 6 match logs per robot (and with one
         robot's logs missing), as a live page mid-event would, and checks each build succeeds.

Rows are matched on their identity fields (robot, match, step, ...). A numeric field passes if
it equals the baseline, or is within that dataset's tolerance (TOLERANCE below, with the reason).
Exit status 1 if a dataset the page draws is missing or a dry-run build fails; known,
explained differences (KNOWN) are reported but do not fail. Plain stdlib, no test framework.
"""
import argparse
import json
import os
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
BASE = os.path.join(HERE, 'data', 'belding')
sys.path.insert(0, HERE)
import review  # noqa: E402

# The fields that identify a row in each dataset; everything else is compared.
KEYS = {'matches': ('robot', 'match'), 'pieces': ('robot', 'match'), 'robots': ('robot',), 'results': ('robot',),
        'shot_recovery': ('battery',), 'mechanisms': ('mechanism',), 'energy': ('measure',),
        'volley_steps': ('robot', 'step'), 'spot_outcomes': ('robot', 'outcome'), 'field_opr': ('team',),
        'top_scores': ('place',), 'cycle_steps': ('robot', 'step'), 'cycle_groups': ('robot', 'group', 'step'),
        'cycle_group_summary': ('robot', 'group'), 'nectar': ('robot',), 'world_ranks': ('robot', 'measure'),
        'square_issues': ('robot',), 'drive_speed': ('robot',), 'feed_summary': ('robot',),
        'feed_nectar': ('robot', 'piece', 'position'), 'drive_trips': ('robot',), 'launch_methods': ('robot', 'method'),
        'volley_pieces': ('robot',), 'launch_volleys': ('robot', 'match', 'press'), 'stick_cancels': ('robot',),
        'floor_pickup': ('robot',), 'floor_tiles': ('robot', 'col', 'row'), 'flower_picks': ('robot', 'mode'),
        'feed_volleys': None}  # feed_volleys: compared as a list, in order

# Absolute tolerances where a field is a median or a mean of timings and the published number was
# computed with a slightly different sample set or rounding (stated per dataset).
TOLERANCE = {
    'default': 0.0,
}

# Fields the page does not draw (tools/event-review/README.md); a miss there is reported as minor.
DRAWN = {}


def drawn_fields():
    import re
    page = open(os.path.join(HERE, 'page', 'review.html')).read()
    out = {}
    for f in os.listdir(BASE):
        if not f.endswith('.json'):
            continue
        rows = json.load(open(os.path.join(BASE, f)))
        if not isinstance(rows, list):
            continue
        keys = {k for r in rows for k in r}
        out[f[:-5]] = {k for k in keys if re.search(r'[.\'"`]' + k + r'\b', page)}
    return out


def fetch_logs(dest):
    if all(os.path.isdir(os.path.join(dest, t, 'matches')) for t in ('19429', '20245')):
        return dest
    os.makedirs(dest, exist_ok=True)
    root = subprocess.run(['git', 'rev-parse', '--show-toplevel'], cwd=HERE, capture_output=True, text=True).stdout.strip()
    subprocess.run(['git', 'fetch', '-q', 'origin', 'events/2026-11-belding'], cwd=root, check=True)
    files = subprocess.run(['git', 'ls-tree', '-r', '--name-only', 'FETCH_HEAD'], cwd=root, capture_output=True, text=True).stdout.split()
    for f in files:
        if '/matches/' not in f:
            continue
        p = os.path.join(dest, f)
        os.makedirs(os.path.dirname(p), exist_ok=True)
        with open(p, 'wb') as o:
            o.write(subprocess.run(['git', 'show', 'FETCH_HEAD:' + f], cwd=root, capture_output=True, check=True).stdout)
    return dest


def render(html):
    """(errors, blank charts) from build/check_render.js, or None without node/playwright."""
    import shutil
    node = shutil.which('node')
    if not node or not os.path.exists(html):
        return None
    r = subprocess.run([node, os.path.join(HERE, 'build', 'check_render.js'), os.path.abspath(html)],
                       capture_output=True, text=True, cwd=os.path.join(HERE, 'build'))
    if r.returncode:
        return None
    return json.loads(r.stdout.strip().splitlines()[-1])


def show_render(label, html):
    r = render(html)
    if r is None:
        print('  %s: page not rendered (node or playwright missing)' % label)
        return True
    sec = {}
    for b in r['blank']:
        sec.setdefault(b.split(':')[0] or 'top', []).append(b.split(':', 1)[1])
    print('  %s: %s; blank: %s' % (label, 'script errors ' + '; '.join(r['errors']) if r['errors'] else 'no script errors',
                                    ', '.join('%s (%d)' % (k, len(v)) for k, v in sorted(sec.items())) or 'nothing'))
    return not r['errors']


def num(x):
    return isinstance(x, (int, float)) and not isinstance(x, bool)


def compare(name, got, want, tol, drawn):
    """(status, notes): status 'exact' | 'tolerance' | 'differs' | 'missing'."""
    if not got and want:
        return 'missing', ['not built']
    notes, worst = [], 'exact'
    if KEYS.get(name) is None:
        pairs = list(zip(got, want))
        if len(got) != len(want):
            notes.append('%d rows, baseline %d' % (len(got), len(want)))
            worst = 'differs'
    else:
        k = KEYS[name]
        g = {tuple(r.get(x) for x in k): r for r in got}
        pairs = []
        for r in want:
            key = tuple(r.get(x) for x in k)
            if key not in g:
                notes.append('row %s missing' % (key,))
                worst = 'differs'
            else:
                pairs.append((g[key], r))
        extra = set(g) - {tuple(r.get(x) for x in k) for r in want}
        if extra:
            notes.append('extra rows %s' % sorted(extra, key=str)[:4])
    for a, b in pairs:
        for f, wv in b.items():
            if KEYS.get(name) and f in KEYS[name]:
                continue
            gv = a.get(f, '<absent>')
            if gv == wv:
                continue
            t = tol.get(f, tol.get('*', 0.0))
            ok = num(gv) and num(wv) and abs(gv - wv) <= t + 1e-9
            tag = '' if f in drawn else ' (not drawn)'
            notes.append('%s%s: %s vs %s%s' % (rowid(name, b), f, gv, wv, tag))
            if ok:
                worst = worst if worst == 'differs' else 'tolerance'
            else:
                worst = 'differs'
    return worst, notes


def rowid(name, r):
    k = KEYS.get(name)
    return '' if not k else '[%s] ' % ' '.join(str(r.get(x)) for x in k)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--logs')
    ap.add_argument('--out')
    ap.add_argument('--dry-run', action='store_true')
    ap.add_argument('--no-pages', action='store_true')
    a = ap.parse_args()
    tmp = a.out or os.path.join(tempfile.gettempdir(), 'event-review-test')
    logs = a.logs or fetch_logs(os.path.join(tmp, 'belding-logs'))
    out = os.path.join(tmp, 'belding')
    args = ['--event', 'USMIBEQ', '--logs', logs, '--out', out, '--ftcscout', os.path.join(BASE, 'ftcscout'), '--offline', '--no-pdf']
    if a.no_pages:
        args.append('--no-pages')
    rc = review.main(args)
    drawn = drawn_fields()
    from reproduce_rules import TOL, KNOWN
    res = {}
    for f in sorted(os.listdir(BASE)):
        if not f.endswith('.json') or f in ('stickcancel.json', 'event.json'):
            continue
        name = f[:-5]
        want = json.load(open(os.path.join(BASE, f)))
        gp = os.path.join(out, 'data', f)
        got = json.load(open(gp)) if os.path.exists(gp) else []
        res[name] = compare(name, got, want, TOL.get(name, {}), drawn.get(name, set()))
    print('\n==== Reproduction of data/belding ====')
    fail = rc != 0
    if not a.no_pages:
        fail |= not show_render('full event page', os.path.join(out, 'review.html'))
    for status in ('exact', 'tolerance', 'differs', 'missing'):
        names = [n for n, (s, _) in res.items() if s == status]
        print('\n%s (%d): %s' % (status.upper(), len(names), ', '.join(names) or '-'))
        if status in ('tolerance', 'differs', 'missing'):
            for n in names:
                why = KNOWN.get(n)
                print('  %s%s' % (n, ('  -- ' + why) if why else ''))
                for note in res[n][1][:12]:
                    print('      ' + note)
                if len(res[n][1]) > 12:
                    print('      ... %d more' % (len(res[n][1]) - 12))
                if status == 'missing' or (status == 'differs' and n not in KNOWN):
                    fail = True
    if a.dry_run:
        print('\n==== Dry run: a live page part way through the event ====')
        for n in (1, 3, 6):
            d = os.path.join(tmp, 'live-%d' % n)
            r = review.main(['--event', 'USMIBEQ', '--logs', logs, '--out', d, '--ftcscout', os.path.join(BASE, 'ftcscout'),
                             '--offline', '--live', '--max-matches', str(n)])
            print('  %d match(es) per robot: %s' % (n, 'built' if r == 0 else 'FAILED'))
            fail |= r != 0
            fail |= not show_render('    page', os.path.join(d, 'review.html'))
            again = os.path.join(tmp, 'live-%d-again' % n)
            review.main(['--event', 'USMIBEQ', '--logs', logs, '--out', again, '--ftcscout', os.path.join(BASE, 'ftcscout'),
                         '--offline', '--live', '--max-matches', str(n), '--no-pages'])
            same = all(open(os.path.join(d, 'data', f), 'rb').read() == open(os.path.join(again, 'data', f), 'rb').read()
                       for f in os.listdir(os.path.join(d, 'data')) if f.endswith('.json'))
            print('    rerun on the same logs: %s' % ('identical datasets' if same else 'DIFFERENT datasets'))
            fail |= not same
        one = os.path.join(tmp, 'one-robot-logs')
        os.makedirs(one, exist_ok=True)
        link = os.path.join(one, '20245')
        if not os.path.exists(link):
            os.symlink(os.path.join(logs, '20245'), link)
        r = review.main(['--event', 'USMIBEQ', '--logs', one, '--out', os.path.join(tmp, 'live-one-robot'),
                         '--ftcscout', os.path.join(tmp, 'no-ftcscout'), '--offline', '--live', '--max-matches', '2'])
        print('  20245 only, 2 matches, no FTCScout: %s' % ('built' if r == 0 else 'FAILED'))
        fail |= r != 0
        fail |= not show_render('    page', os.path.join(tmp, 'live-one-robot', 'review.html'))
    print('\n%s' % ('FAIL' if fail else 'OK'))
    return 1 if fail else 0


if __name__ == '__main__':
    sys.exit(main())
