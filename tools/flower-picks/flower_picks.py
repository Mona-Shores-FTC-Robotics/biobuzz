#!/usr/bin/env python3
"""Every Autonomous FLOWER pick, counted from the robot's log, joined to the count seen on video.

The question it answers: how many pieces does each Auto FLOWER pick bring, on each robot and each
FLOWER stick version? The bar is 4 every pick. TeleOp picks are left out.

  python3 tools/flower-picks/flower_picks.py logs   [--practice] [--out doc/flower-picks/log-picks.csv]
      Fetches every event match log named in tools/advantagescope/replay/matches.json from its
      events/* branch (and, with --practice, every Auto log on the logs/* branches and in the
      events' practice/ folders), and writes one row per Auto PickFlower card.
  python3 tools/flower-picks/flower_picks.py report [--csv doc/flower-picks/log-picks.csv]
                                                    [--video doc/flower-picks/video-counts.json]
      Joins the log rows to the video counts, prints the reconciled count per pick, the 4/4 rate
      and the 0-4 distribution per robot and per stick version, and writes doc/flower-picks/picks.json
      (what the study page is built from).

Fetch the branches first:
  git fetch origin 'refs/heads/events/*:refs/remotes/origin/events/*' 'refs/heads/logs/*:refs/remotes/origin/logs/*'

What the log columns mean (each is one way of counting; none is the count on its own):
  carried     /Lane/Count when the pick started: pieces already in the lane (the lane sensor merges
              touching pieces, so it can read low)
  lane_after  /Lane/Count when the pick card ended
  spikes      intake-current spikes over 1.35 A during the push and dwell: about one per piece
  next_dips   balls the next LaunchAll fired, by the launcher wheels' dips (read_logs.wheel_dips:
              within one of the video at Belding and never under it)
  next_counter the shot counter over the same volley (undercounts after a pick)
  log_gain    next_dips - carried: the log's best guess at what this pick added
  powered     fraction of the pick's stick-out time the stick servo was powered (/FlowerStick/Powered):
              1.0 = held out, low = it went limp (LIMP_EXTENDED) during the push
  roller      mean /FlowerStick/RollerPower while out: non-zero = the active (roller) stick

Stick version is read from the log, never assumed: "active" when the stick's roller is configured
(FlowerStickSubsystem.roller true or RollerPower non-zero), else "passive"; "hold" when the servo
stayed powered through the push (powered >= 0.9), else "limp".

The video counts live in doc/flower-picks/video-counts.json, keyed "<EVENT> <MATCH> <TEAM> <pick#>",
written by a person or a session looking at the clip (tools/match-review). Each entry: in_seen
(pieces seen going in at the FLOWER, or null), out_seen (pieces seen leaving in the next volley, or
null), left_on_flower, dropped, count (the reconciled pieces this pick added), confidence
(sure / likely / questionable), note, clip (match-review command or time) .
"""
import argparse
import csv
import json
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
sys.path.insert(0, os.path.join(ROOT, 'tools', 'pull-logs'))
import read_logs as rl  # noqa: E402

MATCHES = os.path.join(ROOT, 'tools', 'advantagescope', 'replay', 'matches.json')
DOC = os.path.join(ROOT, 'doc', 'flower-picks')
CACHE = os.path.join(ROOT, 'TeamCode', 'build', 'flower-picks')

FIELDS = ['source', 'event', 'match', 'team', 'alliance', 'log', 'auto', 'build', 'pick', 'flower', 'start_s',
          'card_s', 'push_end', 'outcome', 'carried', 'lane_after', 'spikes', 'next_dips', 'next_counter',
          'next_lane_after', 'log_gain', 'powered', 'roller', 'stick', 'hold', 'version']


def version(date, stick, hold):
    """The stick version a pick ran on: the robots' first sticks (no roller) until 19429's new stick
    with a roller (17 Nov 2026) and 20245's SWYFT pivot (20 Nov); then active (roller) or passive,
    each limp (LIMP_EXTENDED: the pulse cut once out) or hold (powered out, from 7edec46c on 20245)."""
    era = 'first stick' if date < '2026-11-17' else 'Fruitport stick'
    return '%s, %s, %s' % (era, stick, hold)


def git_show(ref, path, out):
    if os.path.exists(out):
        return True
    r = subprocess.run(['git', '-C', ROOT, 'show', '%s:%s' % (ref, path)], capture_output=True)
    if r.returncode:
        return False
    os.makedirs(os.path.dirname(out), exist_ok=True)
    with open(out, 'wb') as f:
        f.write(r.stdout)
    return True


def frac_true(series, a, b):
    """Fraction of [a, b] that a boolean series was true, by its samples."""
    xs = [bool(v) for t, v in series if a <= t <= b]
    return sum(xs) / len(xs) if xs else float('nan')


def mean(series, a, b):
    xs = [float(v) for t, v in series if a <= t <= b and isinstance(v, (int, float, bool))]
    return sum(xs) / len(xs) if xs else float('nan')


def auto_picks(path):
    """One dict per Auto PickFlower card in the log at path (seconds from Auto PLAY)."""
    d = rl.read(path)
    cards, t0 = rl._auto_steps(d)
    if not cards:
        return []
    md = lambda k: (d.get('/RealMetadata/' + k) or [(0, '?')])[0][1]
    events = [(t - t0, str(v)) for t, v in d.get('/Events', [])]
    sh = lambda key: [(t - t0, v) for t, v in d.get(key, [])]
    lane, amps = sh('/Lane/Count'), sh('/Intake/CurrentAmps')
    out_, powered, roller = sh('/FlowerStick/Out'), sh('/FlowerStick/Powered'), sh('/FlowerStick/RollerPower')
    left, right = d.get('/Launcher/LeftTicksPerSecond', []), d.get('/Launcher/RightTicksPerSecond', [])
    target, shots = d.get('/Launcher/TargetTicksPerSecond', []), d.get('/Launcher/Shots', [])
    roller_cfg = str(md('Tunable/FlowerStickSubsystem.roller')).lower()

    volleys, n = [], 0
    for kind, name, a, b in cards:
        if kind == 'command' and name.startswith('LaunchAll'):
            n += 1
            tgt = rl.at(target, t0 + a + 1.0) or rl.at(target, t0 + a)
            speeds = [(ta, (x + y) / 2) for (ta, x), (tb, y) in zip(left, right) if t0 + a - 0.45 <= ta <= t0 + b + 0.1]
            dips = None if n == 1 or not tgt or not speeds else len(
                [t for t in rl.wheel_dips(speeds, tgt) if a - 0.05 <= t - t0 <= b + 0.1])
            counted = (rl.at(shots, t0 + b) or 0) - (rl.at(shots, t0 + a) or 0)
            volleys.append((a, b, dips, counted, rl.at(lane, b)))

    rows, k = [], 0
    for kind, name, a, b in cards:
        if not (kind == 'command' and name.startswith('PickFlower')):
            continue
        if b - a < 0.5:  # a pick the endgame guard cut as it started: no push happened
            continue
        k += 1
        ev = [v for t, v in events if a - 0.05 <= t <= b + 0.05 and 'FLOWER pick' in v]
        m = next((re.search(r'start, (.*?) FLOWER', v) for v in ev if 'start,' in v), None)
        flower = rl.flower_name(m.group(1)) if m else '?'
        pe = next((re.search(r'IN [\d.]+ s \((.*?)\), then DWELL', v) for v in ev if 'then DWELL' in v), None)
        stop = next((re.search(r'stopped in (\w+) after [\d.]+ s \((.*?)\)', v) for v in ev if 'stopped in' in v), None)
        outcome = 'done' if any('done,' in v for v in ev) else ('stopped in %s (%s)' % stop.groups() if stop else '?')
        stick_out = [t for t, v in out_ if a <= t <= b and v]
        sa, sb = (stick_out[0], stick_out[-1]) if stick_out else (a, b)
        pw = frac_true(powered, sa, sb)
        rp = mean(roller, sa, sb)
        nxt = [v for v in volleys if v[0] > a]
        nd, nc, nl = (nxt[0][2], nxt[0][3], nxt[0][4]) if nxt else (None, None, None)
        carried = rl.at(lane, a)
        active = roller_cfg == 'true' or (rp == rp and abs(rp) > 0.05)
        rows.append({
            'auto': md('OpMode'), 'build': str(md('Build'))[-10:], 'pick': k, 'flower': flower,
            'start_s': round(a, 2), 'card_s': round(b - a, 2), 'push_end': pe.group(1) if pe else '-',
            'outcome': outcome, 'carried': carried, 'lane_after': rl.at(lane, b),
            'spikes': rl.spikes(amps, a, b), 'next_dips': nd, 'next_counter': nc, 'next_lane_after': nl,
            'log_gain': None if nd is None or carried is None else nd - carried,
            'powered': None if pw != pw else round(pw, 2), 'roller': None if rp != rp else round(rp, 2),
            'stick': 'active' if active else 'passive',
            'hold': '?' if pw != pw else ('hold' if pw >= 0.9 else 'limp'),
        })
    return rows


def cmd_logs(args):
    rows = []
    m = json.load(open(MATCHES))
    for e in m['events']:
        ref = 'origin/' + e['logs']
        for x in e['matches']:
            f = x['log'].split('/')[-1]
            local = os.path.join(CACHE, e['event'], '%s_%s_%s.wpilog.gz' % (e['event'], x['match'], x['team']))
            if not git_show(ref, '%s/matches/%s' % (x['team'], f), local):
                print('no log on %s for %s %s %s' % (ref, e['event'], x['match'], x['team']), file=sys.stderr)
                continue
            for r in auto_picks(local):
                r.update(source='match', event=e['event'], match=x['match'], team=x['team'],
                         alliance=x['alliance'], log=f, version=version(f[:10], r['stick'], r['hold']))
                rows.append(r)
    if args.practice:
        rows += practice_rows(m)
    os.makedirs(os.path.dirname(args.out), exist_ok=True)
    with open(args.out, 'w', newline='') as fh:
        w = csv.DictWriter(fh, fieldnames=FIELDS)
        w.writeheader()
        for r in rows:
            w.writerow({k: r.get(k) for k in FIELDS})
    print('%d Auto picks -> %s' % (len(rows), os.path.relpath(args.out, ROOT)))


AUTO_FILE = re.compile(r'_(R|L)-[A-Za-z]+_.*\.wpilog(\.gz)?$')


def practice_rows(m):
    """Auto picks in practice logs (no video): every logs/* branch and every events/*/<team>/practice/."""
    match_logs = {x['log'].split('/')[-1] for e in m['events'] for x in e['matches']}
    refs = subprocess.run(['git', '-C', ROOT, 'for-each-ref', '--format=%(refname:short)',
                           'refs/remotes/origin/logs/', 'refs/remotes/origin/events/'],
                          capture_output=True, text=True).stdout.split()
    seen, rows = set(), []
    for ref in refs:
        files = subprocess.run(['git', '-C', ROOT, 'ls-tree', '-r', '--name-only', ref],
                               capture_output=True, text=True).stdout.split()
        for p in files:
            f = p.split('/')[-1]
            if not AUTO_FILE.search(f) or f in match_logs or f in seen:
                continue
            if ref.startswith('origin/events/') and '/practice/' not in p:
                continue
            seen.add(f)
            team = '19429' if '19429' in ref + p else '20245' if '20245' in ref + p else '?'
            local = os.path.join(CACHE, 'practice', f)
            if not git_show(ref, p, local):
                continue
            try:
                found = auto_picks(local)
            except Exception as ex:  # a truncated practice log: skip it, say so
                print('skipped %s: %s' % (f, ex), file=sys.stderr)
                continue
            for r in found:
                r.update(source='practice (log only)', event=ref.split('/', 1)[1], match='-', team=team,
                         alliance='-', log=f, version=version(f[:10], r['stick'], r['hold']))
                rows.append(r)
    return rows


def num(x):
    try:
        return int(float(x))
    except (TypeError, ValueError):
        return None


def cmd_report(args):
    rows = list(csv.DictReader(open(args.csv)))
    video = json.load(open(args.video)) if os.path.exists(args.video) else {}
    video = {k: v for k, v in video.items() if not k.startswith('_')}
    out = []
    for r in rows:
        key = '%s %s %s %s' % (r['event'], r['match'], r['team'], r['pick'])
        v = video.get(key, {})
        r = dict(r)
        r['key'] = key
        r.update({('video_' + k if k in ('note', 'clip') else k): v.get(k) for k in
                  ('in_seen', 'out_seen', 'left_on_flower', 'dropped', 'count', 'confidence', 'note', 'clip', 'stick_note')})
        if r.get('count') is None:
            g = num(r['log_gain'])
            r['count'] = None if g is None else max(0, min(4, g))
            r['confidence'] = 'log only' if r['source'] != 'match' else 'log only (no video yet)'
        out.append(r)
    with open(os.path.join(DOC, 'picks.json'), 'w') as fh:
        json.dump(out, fh, indent=1)

    def group(label, sel):
        cs = [r['count'] for r in out if sel(r) and r['count'] is not None]
        if not cs:
            return
        dist = [cs.count(i) for i in range(5)]
        print('%-44s n=%-3d 4/4 %3d%%  mean %.2f  dist 0..4 %s' % (
            label, len(cs), round(100 * dist[4] / len(cs)), sum(cs) / len(cs), dist))

    print('%-22s %-5s %-4s %-13s %-7s %-5s %4s %4s %4s %4s %5s  %-12s %s' % (
        'match', 'team', 'pick', 'flower', 'stick', 'hold', 'carr', 'dips', 'gain', 'vid', 'count', 'confidence', 'note'))
    for r in out:
        if r['source'] != 'match':
            continue
        print('%-22s %-5s %-4s %-13s %-7s %-5s %4s %4s %4s %4s %5s  %-12s %s' % (
            r['event'] + ' ' + r['match'], r['team'], r['pick'], r['flower'], r['stick'], r['hold'], r['carried'],
            r['next_dips'], r['log_gain'], r.get('out_seen'), r['count'], r['confidence'], (r.get('video_note') or '')[:70]))
    print()
    for src in ('match', 'practice (log only)'):
        for team in ('19429', '20245'):
            group('%s %s' % (src, team), lambda r: r['source'] == src and r['team'] == team)
            for ver in sorted({r['version'] for r in out}):
                group('  %s' % ver, lambda r: r['source'] == src and r['team'] == team and r['version'] == ver)
    print('\nwrote', os.path.relpath(os.path.join(DOC, 'picks.json'), ROOT))


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = p.add_subparsers(dest='cmd', required=True)
    a = sub.add_parser('logs')
    a.add_argument('--practice', action='store_true')
    a.add_argument('--out', default=os.path.join(DOC, 'log-picks.csv'))
    b = sub.add_parser('report')
    b.add_argument('--csv', default=os.path.join(DOC, 'log-picks.csv'))
    b.add_argument('--video', default=os.path.join(DOC, 'video-counts.json'))
    args = p.parse_args()
    {'logs': cmd_logs, 'report': cmd_report}[args.cmd](args)


if __name__ == '__main__':
    main()
