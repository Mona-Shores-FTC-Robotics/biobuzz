#!/usr/bin/env python3
"""Two events side by side, per robot: what got better, what got worse, what stayed the same.

  python3 tools/event-review/compare.py <event A datasets> <event B datasets> --out <dir> [--config <pair.json>]

Reads two dataset folders (the shape review.py writes: data/<name>/ or <out>/data/) and writes
<dir>/compare.json (one row per number and robot: a, b, change, verdict) and <dir>/compare.html
(the comparison page in the review's style, self-contained: d3 and the logos inlined; publish this one).

Each number has a direction (METRICS: +1 higher is better, -1 lower is better, 0 neither), so a
row's verdict is better, worse or same (a change under the number's `same` band). A number the
pair config marks `not_comparable` is shown, never judged: the controls or the counting changed.

The pair config is events/compare-<A code>-<B code>.json by default (the codes from each folder's
event.json). Keys: `not_comparable` {metric key: why}, `robot_not_comparable` {robot: {metric
key: why}} (one robot's setting changed what the number means), `notes` {metric key: a line under the
number}, `changes` [what changed on the robots between the events, one line each], `title`.
Everything else on the page, the insights included, is computed from the two folders, so the
same command compares the next pair of events.
"""
import argparse
import base64
import json
import math
import os
import statistics as st

HERE = os.path.dirname(os.path.abspath(__file__))
TEAMS = {'20245': 'Ultraviolet', '19429': 'Infrared'}


class Ev:
    """One event's datasets, loaded on demand."""

    def __init__(self, d):
        self.d, self._c = d, {}
        self.meta = self.load('event') or {}
        if isinstance(self.meta, list):
            self.meta = {}
        self.name = self.meta.get('name') or os.path.basename(os.path.normpath(d))
        self.code = self.meta.get('code')

    def load(self, name):
        if name not in self._c:
            p = os.path.join(self.d, name + '.json')
            self._c[name] = json.load(open(p, encoding='utf-8')) if os.path.exists(p) else []
        return self._c[name]

    def rows(self, ds, robot, **flt):
        return [r for r in self.load(ds) if r.get('robot') == robot and all(r.get(k) == v for k, v in flt.items())]

    def one(self, ds, robot, field, **flt):
        r = self.rows(ds, robot, **flt)
        return r[0].get(field) if r else None


def share(s):
    """'14 of 36' -> (38.9, 36)."""
    try:
        a, b = s.split(' of ')
        return (round(100.0 * int(a) / int(b), 1), int(b)) if int(b) else (None, None)
    except (AttributeError, ValueError):
        return None, None


def mean_of(rows, f):
    xs = [f(r) for r in rows]
    xs = [x for x in xs if isinstance(x, (int, float))]
    return round(st.mean(xs), 2) if xs else None


# --- value functions: (event, robot) -> value or (value, n) ----------------------------------------
def field(ds, f, **flt):
    return lambda e, r: e.one(ds, r, f, **flt)


def pct_of(ds, f, **flt):
    return lambda e, r: share(e.one(ds, r, f, **flt))


def per_match(ds, f):
    def g(e, r):
        rows = e.rows(ds, r)
        return (mean_of(rows, f), len(rows)) if rows else None
    return g


def best_match(e, r):
    rows = e.rows('pieces', r)
    return max(x['total'] for x in rows) if rows else None


def tele_pieces(x):
    return x['teleop_pollen'] + x['nectar_shot']


def world_pct(measure):
    return lambda e, r: e.one('world_ranks', r, 'top_pct', measure=measure)


def nectar_own(e, r):
    x = e.rows('nectar', r)
    if not x or not x[0].get('own_nectar_in'):
        return None
    x = x[0]
    return round(100.0 * x['own_nectar_launched'] / x['own_nectar_in'], 1), x['own_nectar_in']


def nectar_opp(e, r):
    x = e.rows('nectar', r)
    if not x or not x[0].get('opposing_at_sensor'):
        return None
    x = x[0]
    return round(100.0 * x['opposing_rejected'] / x['opposing_at_sensor'], 1), x['opposing_at_sensor']


def prespun(e, r):
    rows = e.rows('launch_methods', r)
    a = sum(share(x.get('prespun_of'))[0] * share(x.get('prespun_of'))[1] / 100 for x in rows if share(x.get('prespun_of'))[1])
    n = sum(share(x.get('prespun_of'))[1] for x in rows if share(x.get('prespun_of'))[1])
    return (round(100.0 * a / n, 1), n) if n else None


def autodrive_share(e, r):
    rows = {x['method']: x['volleys'] for x in e.rows('launch_methods', r)}
    n = sum(rows.values())
    return (round(100.0 * rows.get('Auto-drive', 0) / n, 1), n) if n else None


def median_of(ds, f):
    def g(e, r):
        xs = [x[f] for x in e.rows(ds, r) if isinstance(x.get(f), (int, float))]
        return (round(st.median(xs), 1), len(xs)) if xs else None
    return g


def slip_matches(e, r):
    rows = e.rows('drive_slip', r)
    return (sum(1 for x in rows if x.get('slip_from_s') is not None), len(rows)) if rows else None


# (key, section, label, unit, value fn, better, same band, sub). Same band: a change smaller than it
# is "same" (a number: absolute; a string 'N%': relative to the first event's value).
METRICS = [
    ('opr', 'Results', 'OPR', 'pts', field('results', 'opr_total'), +1, '5%', 'points added per match, quals'),
    ('avg_np', 'Results', 'Average score', 'pts', field('results', 'avg_np'), +1, '5%', 'alliance, no penalties'),
    ('avg_auto', 'Results', 'Average Auto', 'pts', field('results', 'avg_auto'), +1, '5%', 'alliance'),
    ('avg_teleop', 'Results', 'Average TeleOp', 'pts', field('results', 'avg_teleop'), +1, '5%', 'alliance'),
    ('avg_tips', 'Results', 'TIPs per match', '', field('results', 'avg_tips'), +1, '5%', 'alliance'),
    ('high_np', 'Results', 'Best score', 'pts', field('results', 'high_np'), +1, '5%', 'alliance'),
    ('qual_rank', 'Results', 'Qualification rank', '#', field('results', 'qual_rank'), -1, 0.5, None),
    ('world_total', 'Results', 'World, total OPR', 'top%', world_pct('Total'), -1, '10%', 'share of teams ahead'),
    ('world_auto', 'Results', 'World, Auto OPR', 'top%', world_pct('Auto'), -1, '10%', 'share of teams ahead'),
    ('world_teleop', 'Results', 'World, TeleOp OPR', 'top%', world_pct('TeleOp'), -1, '10%', 'share of teams ahead'),
    ('auto_pieces', 'Pieces launched', 'Auto pieces per match', '', per_match('pieces', lambda x: x['auto_pollen']), +1, '5%', 'mean'),
    ('tele_pieces', 'Pieces launched', 'TeleOp pieces per match', '', per_match('pieces', tele_pieces), +1, '5%', 'mean, POLLEN and NECTAR'),
    ('best_pieces', 'Pieces launched', 'Most pieces in one match', '', best_match, +1, '5%', None),
    ('bps_full', 'Cycle', 'Balls per second, cycles that filled up', '/s', field('cycle_group_summary', 'balls_per_s', group='Filled up'), +1, '8%', 'balls launched ÷ cycle time'),
    ('bps_early', 'Cycle', 'Balls per second, launched before full', '/s', field('cycle_group_summary', 'balls_per_s', group='Shot before full'), +1, '8%', None),
    ('cycle_full', 'Cycle', 'Cycle, filled up', 's', field('cycle_group_summary', 'cycle_median_s', group='Filled up'), -1, '5%', 'median, volley to volley'),
    ('cycle_early', 'Cycle', 'Cycle, launched before full', 's', field('cycle_group_summary', 'cycle_median_s', group='Shot before full'), -1, '5%', 'median'),
    ('balls_full', 'Cycle', 'Balls per filled-up volley', '', field('cycle_group_summary', 'balls_per_volley', group='Filled up'), +1, '8%', 'median'),
    ('first_volley', 'Cycle', 'First TeleOp volley', 's', field('feed_summary', 'first_volley_s'), -1, '8%', 'median from TeleOp start'),
    ('launch_span', 'Launching', 'Launch, press to moving again', 's', field('feed_summary', 'launch_span_s'), -1, '5%', 'median volley'),
    ('p1', 'Launching', 'First piece out', 's', field('volley_pieces', 'p1'), -1, 0.05, 'gate open to the launcher, full volleys'),
    ('later', 'Launching', 'Ball to ball (the transfer)', 's', field('volley_pieces', 'later'), -1, 0.05, 'median, each later piece'),
    ('volley4', 'Launching', 'Four-piece volley, gate to last ball', 's', field('volley_pieces', 'total'), -1, 0.15, 'median'),
    ('spin_wait', 'Launching', 'Spin-up wait per match', 's', per_match('matches', lambda x: x['spinup_wait_s']), -1, 1.0, 'waiting for launch speed'),
    ('launcher_on', 'Launching', 'Launcher spinning', '%', median_of('matches', 'launcher_on_pct'), 0, 5, 'median share of the match'),
    ('prespun', 'Launching', 'Manual volleys already at speed', '%', prespun, +1, 5, 'pre-spun at the press'),
    ('autodrive', 'Launching', 'TeleOp volleys by auto-drive (Square)', '%', autodrive_share, 0, 5, 'the rest are manual'),
    ('manual_time', 'Launching', 'Manual launch, press to moving', 's', field('launch_methods', 'press_to_moving_s', method='Operator FIRE'), -1, '5%', None),
    ('floor_rate', 'Floor pickup', 'Floor pieces per minute', '/min', field('floor_pickup', 'per_min'), +1, '5%', 'TeleOp, collecting'),
    ('floor_first', 'Floor pickup', 'Last launch to first piece', 's', field('floor_pickup', 'first_piece_s'), -1, '8%', 'median'),
    ('floor_between', 'Floor pickup', 'Between floor pieces', 's', field('floor_pickup', 'between_s'), -1, '8%', 'median'),
    ('floor_full', 'Floor pickup', 'Last launch to lane full', 's', field('floor_pickup', 'to_full_s'), -1, '8%', 'median'),
    ('flower_auto', 'FLOWER picks', 'Pieces per Auto pick', '', field('flower_picks', 'per_pick', mode='Auto'), +1, '5%', None),
    ('flower_time', 'FLOWER picks', 'Auto pick time', 's', field('flower_picks', 'pick_s', mode='Auto'), -1, '5%', 'mean'),
    ('flower_tele', 'FLOWER picks', 'TeleOp presses that reached a FLOWER', '%', pct_of('flower_picks', 'reached_of', mode='TeleOp'), +1, 5, None),
    ('unjam', 'Feeder', 'Feeder reversed (unjam) per match', 's', per_match('unjam', lambda x: x['reverse_s']), -1, 0.5, 'mean seconds'),
    ('stuck', 'Feeder', 'Gate opens with a piece that did not go', '', per_match('unjam', lambda x: x['stuck_opens']), -1, 0.5, 'per match: piece at the lane sensor, none launched'),
    ('nectar_own', 'NECTAR', 'Own NECTAR launched', '%', nectar_own, +1, 5, 'of own NECTAR taken in'),
    ('nectar_opp', 'NECTAR', 'Enemy NECTAR rejected', '%', nectar_opp, +1, 5, 'of enemy NECTAR at the sensor'),
    ('drive_fwd', 'Driving', 'Full stick, driving forward', 'in/s', field('drive_speed', 'fwd_full_in_s'), +1, '5%', 'median'),
    ('drive_held', 'Driving', 'Fastest speed held ½ s', 'in/s', field('drive_speed', 'held_half_s_in_s'), +1, '5%', None),
    ('slip', 'Driving', 'Matches where the drivetrain lost grip', '', slip_matches, -1, 0.5, 'odometry speed per wheel speed fell and stayed low'),
    ('volts', 'Battery', 'Typical voltage in TeleOp', 'V', field('robots', 'teleop_median_v'), +1, 0.15, 'median'),
    ('under10', 'Battery', 'Time under 10 V', '%', field('robots', 'pct_below_10v'), -1, 1.5, None),
    ('lowest_v', 'Battery', 'Lowest voltage', 'V', field('robots', 'lowest_v'), +1, 0.3, None),
]


def unpack(v):
    if isinstance(v, tuple):
        return v
    return v, None


def verdict(a, b, better, band):
    if not isinstance(a, (int, float)) or not isinstance(b, (int, float)):
        return None
    d = b - a
    lim = abs(a) * float(band[:-1]) / 100 if isinstance(band, str) else band
    if abs(d) <= lim:
        return 'same'
    if not better:
        return 'changed'
    return 'better' if d * better > 0 else 'worse'


def build(A, B, cfg):
    nc = cfg.get('not_comparable', {})
    rnc = cfg.get('robot_not_comparable', {})
    notes = cfg.get('notes', {})
    out = []
    for key, sec, label, unit, fn, better, band, sub in METRICS:
        for robot in TEAMS:
            va, na = unpack(fn(A, robot))
            vb, nb = unpack(fn(B, robot))
            ch = round(vb - va, 2) if isinstance(va, (int, float)) and isinstance(vb, (int, float)) else None
            why = rnc.get(robot, {}).get(key) or nc.get(key)
            v = 'not comparable' if why else verdict(va, vb, better, band)
            out.append(dict(key=key, section=sec, label=label, unit=unit, robot=robot, a=va, b=vb, change=ch,
                            n_a=na, n_b=nb, better=better, verdict=v, sub=sub, why=why, note=notes.get(key)))
    return out


# --- insights: each returns a dict(title, text, robot?, tone) or None; computed, so they hold for any pair
def _r(rows, key, robot):
    return next((x for x in rows if x['key'] == key and x['robot'] == robot), None)


def _f(v, d=1):
    return ('%.' + str(d) + 'f') % v if isinstance(v, float) else str(v)


def insights(rows, A, B):
    out = []
    fa = A.one('results', '20245', 'field_avg_np') or A.one('results', '19429', 'field_avg_np')
    fb = B.one('results', '20245', 'field_avg_np') or B.one('results', '19429', 'field_avg_np')
    if fa and fb:
        mine = [(TEAMS[t], _r(rows, 'avg_np', t)) for t in TEAMS]
        ours = ', '.join('%s %+d%%' % (n, round(100 * (r['b'] - r['a']) / r['a'])) for n, r in mine if r and r['a'] and r['b'])
        out.append(dict(title='The whole field got better', tone='same',
                        text='The average alliance scored %s at %s and %s at %s: %+d%%. Ours: %s. A rank is against that field.'
                        % (_f(fa), A.name, _f(fb), B.name, round(100 * (fb - fa) / fa), ours)))
    lat = [x for x in rows if x['key'] == 'later']
    vals = [v for x in lat for v in (x['a'], x['b']) if isinstance(v, (int, float))]
    if len(vals) >= 3 and max(vals) - min(vals) <= 0.06:
        out.append(dict(title='The transfer never changes', tone='same',
                        text='Ball to ball inside a volley: %.2f–%.2f s on both robots at both events. The feed is not where '
                             'the time goes; the cycle around it is.' % (min(vals), max(vals))))
    own = {'spin_wait', 'unjam', 'stuck', 'best_pieces', 'slip'}   # these have a card of their own
    for t, n in TEAMS.items():
        judged = [x for x in rows if x['robot'] == t and x['verdict'] == 'better' and x['section'] != 'Results'
                  and x['key'] not in own and isinstance(x['a'], (int, float)) and isinstance(x['b'], (int, float))
                  and x['a'] > 0 and x['b'] > 0]
        top = sorted(judged, key=lambda x: -abs(math.log(x['b'] / x['a'])))[:3]
        if top:
            out.append(dict(title="%s's biggest gains" % n, robot=t, tone='better',
                            text='; '.join('%s %s → %s%s' % (x['label'], fmt(x['a'], x['unit']), fmt(x['b'], x['unit']),
                                                              ' (%.1f×)' % max(x['b'] / x['a'], x['a'] / x['b'])
                                                              if max(x['b'] / x['a'], x['a'] / x['b']) >= 1.4 else '')
                                           for x in top) + '.'))
    for t, n in TEAMS.items():
        w = [x for x in rows if x['robot'] == t and x['key'].startswith('world_') and isinstance(x['a'], (int, float))
             and isinstance(x['b'], (int, float)) and x['b'] <= x['a'] / 2]
        if w:
            x = max(w, key=lambda x: x['a'] / x['b'])
            out.append(dict(title='%s climbed the world list' % n, robot=t, tone='better',
                            text='%s: from %s to %s of teams worldwide, as FTCScout had it after each event.'
                            % (x['label'].replace('World, ', ''), fmt(x['a'], x['unit']), fmt(x['b'], x['unit']))))
    ad = [_r(rows, 'autodrive', t) for t in TEAMS]
    if all(x and isinstance(x['a'], (int, float)) and isinstance(x['b'], (int, float)) and x['a'] - x['b'] >= 25 for x in ad):
        out.append(dict(title='Auto-drive gave way to manual fire', tone='changed',
                        text='Share of TeleOp volleys launched by auto-drive (Square): %s. Manual volleys are timed from a '
                             'different press now, so manual launch times are shown, not judged.'
                        % '; '.join('%s %s%% → %s%%' % (TEAMS[x['robot']], _f(x['a'], 0), _f(x['b'], 0)) for x in ad)))
    sw = [(_r(rows, 'spin_wait', t), _r(rows, 'launcher_on', t)) for t in TEAMS]
    if all(a and b and a['a'] and a['b'] is not None and a['b'] < a['a'] / 4 for a, b in sw):
        out.append(dict(title='The launcher stopped waiting', tone='better',
                        text='Waiting for launch speed, per match: %s. The launcher now spins %s of the match (median), so a press finds it at speed.'
                        % ('; '.join('%s %s → %s s' % (TEAMS[a['robot']], _f(a['a']), _f(a['b'])) for a, _ in sw),
                           '/'.join('%s%%' % _f(b['b'], 0) for _, b in sw if b))))
    u = {t: _r(rows, 'unjam', t) for t in TEAMS}
    if all(u.values()) and all(isinstance(x['b'], (int, float)) for x in u.values()):
        tot = {t: sum(x['reverse_s'] for x in B.rows('unjam', t)) for t in TEAMS}
        nm = {t: len(B.rows('unjam', t)) for t in TEAMS}
        hi = max(tot, key=tot.get)
        lo = min(tot, key=tot.get)
        if tot[lo] and tot[hi] / max(tot[lo], 0.1) >= 2:
            worst = sorted(B.rows('unjam', hi), key=lambda x: -x['stuck_opens'])[:2]
            out.append(dict(title='%s fought its feeder' % TEAMS[hi], robot=hi, tone='worse',
                            text='%s reversed the feeder %.1f s over %d matches at %s (%s: %.1f s over %d). Worst: %s, where a piece sat '
                                 'at the lane sensor and none launched.'
                            % (TEAMS[hi], tot[hi], nm[hi], B.name, TEAMS[lo], tot[lo], nm[lo],
                               ' and '.join('%s (%d gate opens like that)' % (w['match'], w['stuck_opens']) for w in worst))))
    for t in TEAMS:
        x = B.rows('nectar', t)
        if x and x[0].get('own_nectar_in', 0) >= 3 and x[0].get('own_nectar_launched') == 0:
            r = _r(rows, 'nectar_own', t)
            if r and r['verdict'] == 'not comparable':
                out.append(dict(title='%s kept only POLLEN, as set' % TEAMS[t], robot=t, tone='same',
                                text='%s It spat out all %d of its own NECTAR it took in and all %d enemy NECTAR '
                                     'it saw: the reject did what it was set to do.'
                                     % (r['why'], x[0]['own_nectar_in'], x[0]['opposing_rejected'])))
                continue
            other = [B.rows('nectar', o)[0] for o in TEAMS if o != t and B.rows('nectar', o)]
            out.append(dict(title='%s launched none of its own NECTAR' % TEAMS[t], robot=t, tone='worse',
                            text='0 of %d it took in%s. It rejected all %d enemy NECTAR it saw, so the colour sorting '
                                 'worked; its own did not reach the launcher.' % (x[0]['own_nectar_in'],
                                                      ('; %s launched %d of %d' % (TEAMS[other[0]['robot']], other[0]['own_nectar_launched'], other[0]['own_nectar_in'])) if other else '',
                                                      x[0]['opposing_rejected'])))
    for t in TEAMS:
        for s in B.rows('drive_slip', t):
            if s.get('slip_from_s') is None:
                continue
            after = [x for x in B.rows('drive_slip', t) if x['order'] > s['order'] and x.get('slip_from_s') is None]
            low = [r for b, r in s['bins'] if b >= s['slip_from_s']]
            out.append(dict(title='%s\'s wheels slipped in %s' % (TEAMS[t], s['match']), robot=t, tone='worse',
                            text='From %d s into TeleOp the robot moved %d%% as far per wheel turn as it usually does (%.0f vs %.0f in/s per 1000 '
                                 'ticks/s)%s.' % (s['slip_from_s'], round(100 * st.median(low) / s['grip']), st.median(low), s['grip'],
                                                  ('; back to normal by %s' % after[0]['match']) if after else '')))
    for t in TEAMS:
        r = _r(rows, 'best_pieces', t)
        if r and isinstance(r['b'], (int, float)) and r['a'] and r['b'] > r['a']:
            m = max(B.rows('pieces', t), key=lambda x: x['total'])
            out.append(dict(title='New record: %d pieces' % r['b'], robot=t, tone='better',
                            text='%s launched %d pieces in %s, %d more than its best at %s.' % (TEAMS[t], r['b'], m['match'], r['b'] - r['a'], A.name)))
    return out


def fmt(v, unit):
    if v is None:
        return '—'
    if unit == '#':
        return '#%d' % v
    if unit == 'top%':
        return 'top %s%%' % (('%.1f' % v).rstrip('0').rstrip('.'))
    if isinstance(v, float):
        v = ('%.2f' % v).rstrip('0').rstrip('.')
    return '%s%s' % (v, '' if unit == '' else ('%' if unit == '%' else (unit if unit.startswith('/') else ' ' + unit)))


def b64(name):
    p = os.path.join(HERE, 'page', name)
    return 'data:image/png;base64,' + base64.b64encode(open(p, 'rb').read()).decode() if os.path.exists(p) else ''


def page(A, B, rows, ins, cfg):
    def pick(E):
        keep = {}
        for ds in ('results', 'pieces', 'unjam', 'drive_slip', 'volley_pieces', 'cycle_group_summary', 'world_ranks'):
            keep[ds] = E.load(ds)
        return {'name': E.name, 'date': E.meta.get('date', ''), 'code': E.code, 'd': keep}
    data = {'a': pick(A), 'b': pick(B), 'rows': rows, 'insights': ins, 'teams': TEAMS,
            'changes': cfg.get('changes', []), 'title': cfg.get('title') or '%s vs %s' % (A.name, B.name)}
    t = open(os.path.join(HERE, 'page', 'compare.html'), encoding='utf-8').read()
    t = t.replace('LOGO_IR', b64('logo-19429-dark.png')).replace('LOGO_UV', b64('logo-20245-dark.png'))
    t = t.replace('PAGE_TITLE', data['title'])
    d3 = open(os.path.join(HERE, 'page', 'd3.min.js'), encoding='utf-8').read()   # inlined: the page works offline
    t = t.replace('<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/7.9.0/d3.min.js"></script>',
                  '<script>' + d3.replace('</script', '<\\/script') + '</script>')
    return t.replace('/*DATA*/null', json.dumps(data, separators=(',', ':'), ensure_ascii=False).replace('</', '<\\/'))


def pair_config(A, B, path):
    path = path or os.path.join(HERE, 'events', 'compare-%s-%s.json' % (A.code, B.code))
    return (json.load(open(path, encoding='utf-8')), path) if os.path.exists(path) else ({}, None)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('a', help='first event\'s datasets (e.g. tools/event-review/data/belding-rebuilt)')
    ap.add_argument('b', help='second event\'s datasets')
    ap.add_argument('--out', required=True)
    ap.add_argument('--config', help='pair config (default events/compare-<A>-<B>.json)')
    x = ap.parse_args()
    A, B = Ev(x.a), Ev(x.b)
    if A.name == B.name:
        B.name += ' (2)'
    cfg, cpath = pair_config(A, B, x.config)
    rows = build(A, B, cfg)
    ins = insights(rows, A, B)
    os.makedirs(x.out, exist_ok=True)
    json.dump({'a': A.name, 'b': B.name, 'config': cpath, 'rows': rows, 'insights': ins},
              open(os.path.join(x.out, 'compare.json'), 'w', encoding='utf-8'), indent=1, ensure_ascii=False)
    open(os.path.join(x.out, 'compare.html'), 'w', encoding='utf-8').write(page(A, B, rows, ins, cfg))
    tally = {t: {v: sum(1 for r in rows if r['robot'] == t and r['verdict'] == v) for v in ('better', 'worse', 'same')} for t in TEAMS}
    print('config:', cpath or 'none')
    for t, n in TEAMS.items():
        print('  %s: %d better, %d worse, %d same' % (n, tally[t]['better'], tally[t]['worse'], tally[t]['same']))
    print('wrote', os.path.join(x.out, 'compare.json'), os.path.join(x.out, 'compare.html'))


if __name__ == '__main__':
    main()
