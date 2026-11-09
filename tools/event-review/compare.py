#!/usr/bin/env python3
"""Two events side by side: the review's headline numbers and cards, per robot, with the change.

  python3 tools/event-review/compare.py data/belding data/<event> --out <dir>

Reads two dataset folders (the shape review.py writes) and writes <dir>/compare.json (one row per
number: section, label, robot, a, b, change) and <dir>/compare.html (one page in the review's
style). Numbers only: no row says whether a change is good or bad.
"""
import argparse
import json
import os
import statistics as st

TEAMS = {'20245': 'Ultraviolet', '19429': 'Infrared'}

# (section, label, unit, dataset, row filter, field). Labels are the review page's own.
ROWS = [
    ('Results', 'Avg score', 'pts', 'results', {}, 'avg_np'),
    ('Results', 'Avg auto', 'pts', 'results', {}, 'avg_auto'),
    ('Results', 'Avg teleop', 'pts', 'results', {}, 'avg_teleop'),
    ('Results', 'OPR', 'pts', 'results', {}, 'opr_total'),
    ('Results', 'Qualification rank', '#', 'results', {}, 'qual_rank'),
    ('Results', 'Teams at the event', '', 'results', {}, 'teams'),
    ('Results', 'World rank, total OPR', '#', 'world_ranks', {'measure': 'Total'}, 'rank'),
    ('Pieces launched', 'Auto pieces per match', '', 'pieces', {}, 'auto_pollen'),
    ('Pieces launched', 'TeleOp pieces per match', '', 'pieces', {}, '_teleop'),
    ('Cycle time', 'Launch, press to moving again', 's', 'feed_summary', {}, 'launch_span_s'),
    ('Cycle time', 'First TeleOp volley', 's', 'feed_summary', {}, 'first_volley_s'),
    ('Cycle time', 'First piece out, full volleys', 's', 'volley_pieces', {}, 'p1'),
    ('Cycle time', 'Each later piece, full volleys', 's', 'volley_pieces', {}, 'later'),
    ('Cycle time', 'Cycle, filled up', 's', 'cycle_group_summary', {'group': 'Filled up'}, 'cycle_median_s'),
    ('Cycle time', 'Cycle, launched before full', 's', 'cycle_group_summary', {'group': 'Shot before full'}, 'cycle_median_s'),
    ('Floor pickup', 'Floor pieces per minute', '', 'floor_pickup', {}, 'per_min'),
    ('Floor pickup', 'Last launch to first piece', 's', 'floor_pickup', {}, 'first_piece_s'),
    ('Floor pickup', 'Between floor pieces', 's', 'floor_pickup', {}, 'between_s'),
    ('Floor pickup', 'Last launch to lane full', 's', 'floor_pickup', {}, 'to_full_s'),
    ('FLOWER picks', 'Pieces per Auto pick', '', 'flower_picks', {'mode': 'Auto'}, 'per_pick'),
    ('FLOWER picks', 'Auto pick time', 's', 'flower_picks', {'mode': 'Auto'}, 'pick_s'),
    ('FLOWER picks', 'TeleOp presses that reached a FLOWER', '%', 'flower_picks', {'mode': 'TeleOp'}, 'reached_of'),
    ('Driving', 'Full stick, driving forward', 'in/s', 'drive_speed', {}, 'fwd_full_in_s'),
    ('Driving', 'Fastest speed held ½ s', 'in/s', 'drive_speed', {}, 'held_half_s_in_s'),
    ('Driving', 'Driving to launch, full', 'in/s', 'drive_trips', {}, 'full_moving_in_s'),
    ('Launching', 'Auto-drive volleys', '', 'launch_methods', {'method': 'Auto-drive'}, 'volleys'),
    ('Launching', 'Auto-drive launch time', 's', 'launch_methods', {'method': 'Auto-drive'}, 'press_to_moving_s'),
    ('Launching', 'Auto-drive presses that launched', '%', 'launch_methods', {'method': 'Auto-drive'}, 'presses_launched_of'),
    ('Launching', 'Manual volleys', '', 'launch_methods', {'method': 'Operator FIRE'}, 'volleys'),
    ('Launching', 'Manual launch time', 's', 'launch_methods', {'method': 'Operator FIRE'}, 'press_to_moving_s'),
    ('Launching', 'Presses cancelled by a moving stick', '%', 'stick_cancels', {}, '_cancel_share'),
    ('Battery', 'Typical voltage in a match', 'V', 'robots', {}, 'teleop_median_v'),
    ('Battery', 'Time under 9 V', '%', 'robots', {}, 'pct_below_9v'),
]


def load(d, name):
    p = os.path.join(d, name + '.json')
    return json.load(open(p)) if os.path.exists(p) else []


def share(s):
    """'14 of 36' -> 38.9 (percent)."""
    try:
        a, b = s.split(' of ')
        return round(100.0 * int(a) / int(b), 1) if int(b) else None
    except (AttributeError, ValueError):
        return None


def value(d, ds, flt, field, robot):
    rows = [r for r in load(d, ds) if r.get('robot') == robot and all(r.get(k) == v for k, v in flt.items())]
    if not rows:
        return None, None
    if ds == 'pieces':   # per match: mean over the event's matches
        xs = [(r['teleop_pollen'] + r['nectar_shot']) if field == '_teleop' else r[field] for r in rows]
        return round(st.mean(xs), 1), len(xs)
    r = rows[0]
    if field == '_cancel_share':
        robots = [x for x in load(d, 'robots') if x['robot'] == robot]
        if not robots or not robots[0].get('spot_presses'):
            return None, None
        return round(100.0 * r['cancels'] / robots[0]['spot_presses'], 1), robots[0]['spot_presses']
    v = r.get(field)
    if isinstance(v, str):
        return share(v), int(v.split(' of ')[1]) if ' of ' in v else None
    return v, None


def event_name(d):
    p = os.path.join(d, 'event.json')
    return json.load(open(p)).get('name') if os.path.exists(p) else os.path.basename(os.path.normpath(d))


def build(a, b):
    out = []
    for sec, label, unit, ds, flt, field in ROWS:
        for robot in TEAMS:
            va, na = value(a, ds, flt, field, robot)
            vb, nb = value(b, ds, flt, field, robot)
            ch = round(vb - va, 2) if isinstance(va, (int, float)) and isinstance(vb, (int, float)) else None
            out.append(dict(section=sec, label=label, unit=unit, robot=robot, a=va, b=vb, change=ch, n_a=na, n_b=nb))
    return out


def fmt(v, unit):
    if v is None:
        return '—'
    if unit == '#':
        return '#%d' % v
    if isinstance(v, float):
        v = ('%.2f' % v).rstrip('0').rstrip('.')
    return '%s%s' % (v, '' if unit in ('', '#') else (' ' + unit if unit != '%' else '%'))


def html(rows, na, nb):
    secs = []
    for sec in dict.fromkeys(r['section'] for r in rows):
        body = []
        for label in dict.fromkeys(r['label'] for r in rows if r['section'] == sec):
            rs = {r['robot']: r for r in rows if r['section'] == sec and r['label'] == label}
            cells = []
            for robot in TEAMS:
                r = rs[robot]
                ch = '' if r['change'] is None else ('%+g' % r['change'])
                cells.append('<td class="%s">%s</td><td class="%s">%s</td><td class="ch">%s</td>'
                             % ('ir' if robot == '19429' else 'uv', fmt(r['a'], r['unit']),
                                'ir' if robot == '19429' else 'uv', fmt(r['b'], r['unit']), ch))
            body.append('<tr><th>%s</th>%s</tr>' % (label, ''.join(cells)))
        secs.append('<section><h2>%s</h2><div class="tb"><table><thead><tr><th></th>%s</tr><tr><th></th>%s</tr></thead><tbody>%s</tbody></table></div></section>'
                    % (sec, ''.join('<th colspan="3" class="%s">%s</th>' % ('ir' if t == '19429' else 'uv', n) for t, n in TEAMS.items()),
                       ''.join('<th>%s</th><th>%s</th><th>Change</th>' % (na, nb) for _ in TEAMS), ''.join(body)))
    return '''<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>%s vs %s</title>
<style>
:root { color-scheme: dark; --bg:#0A0C12; --panel:#11151F; --line:#232A3B; --fg:#EEF1F8; --muted:#8C95AA; --ir:#FF5B3A; --uv:#5C7DFF;
  --spectrum: linear-gradient(90deg,#E8263A 0%%,#FF7A2F 16%%,#FFC531 32%%,#3DD68C 50%%,#2E9BFF 68%%,#5C6CFF 84%%,#A45CFF 100%%); }
html, body { margin:0; background:var(--bg); color:var(--fg); font-family: system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif; }
.band { height:5px; background:var(--spectrum); }
main { max-width:1180px; margin:0 auto; padding:0 16px 28px; display:grid; gap:22px; }
h1 { margin:18px 0 0; font-size:clamp(24px,4vw,40px); font-weight:800; letter-spacing:-.02em; text-transform:uppercase; }
h2 { margin:0 0 8px; font-size:13px; letter-spacing:.16em; text-transform:uppercase; padding-top:8px; border-top:2px solid; border-image:var(--spectrum) 1; }
.tb { overflow-x:auto; background:var(--panel); border:1px solid var(--line); border-radius:12px; }
table { border-collapse:collapse; width:100%%; font-variant-numeric:tabular-nums; font-size:14px; }
th, td { padding:7px 10px; text-align:right; border-bottom:1px solid var(--line); white-space:nowrap; }
th:first-child { text-align:left; font-weight:600; white-space:normal; min-width:12rem; }
thead th { font-size:11.5px; color:var(--muted); font-weight:600; }
thead th.ir, td.ir { color:var(--ir); } thead th.uv, td.uv { color:var(--uv); }
td.ch { color:var(--muted); }
</style></head><body><div class="band"></div><main><h1>%s vs %s</h1>%s</main></body></html>''' % (na, nb, na, nb, ''.join(secs))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('a', help='first event\'s datasets (e.g. tools/event-review/data/belding)')
    ap.add_argument('b', help='second event\'s datasets')
    ap.add_argument('--out', required=True)
    x = ap.parse_args()
    rows = build(x.a, x.b)
    os.makedirs(x.out, exist_ok=True)
    na, nb = event_name(x.a), event_name(x.b)
    if na == nb:
        nb += ' (2)'
    json.dump({'a': na, 'b': nb, 'rows': rows}, open(os.path.join(x.out, 'compare.json'), 'w'), indent=1)
    open(os.path.join(x.out, 'compare.html'), 'w').write(html(rows, na, nb))
    print('wrote', os.path.join(x.out, 'compare.json'), os.path.join(x.out, 'compare.html'))


if __name__ == '__main__':
    main()
