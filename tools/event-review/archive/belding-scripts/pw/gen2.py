import sys, glob, os, re, json, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root, sp, out = sys.argv[2], sys.argv[3], sys.argv[4]
t = json.load(open(f'{sp}/t.json')); m = json.load(open(f'{sp}/m.json'))
field = []
for x in t:
    s = x.get('stats')
    if not s: continue
    o = s['opr']
    field.append({'team': str(x['teamNumber']), 'ours': x['teamNumber'] in (19429, 20245),
                  'opr_total': round(o['totalPointsNp'], 1), 'opr_auto': round(o['autoPoints'], 1),
                  'opr_teleop': round(o['dcPoints'], 1), 'opr_auto_hive': round(o['autoHivePoints'], 1),
                  'opr_teleop_hive': round(o['dcHivePoints'], 1), 'opr_tips': round(o['tips'], 2)})
json.dump(field, open(f'{out}/field_opr.json', 'w'), indent=1)
al = []
for x in m:
    for c in ('red', 'blue'):
        ts = [str(tt['teamNumber']) for tt in x['teams'] if tt['alliance'].lower() == c]
        lvl = f"Q{x['id']}" if x['tournamentLevel'] == 'Quals' else f"Playoff M{x['series']}"
        al.append({'match': lvl, 'alliance': ' + '.join(ts), 'score': x['scores'][c]['totalPoints'],
                   'auto': x['scores'][c]['autoPoints'], 'ours': bool({'19429', '20245'} & set(ts)),
                   'penalties_committed': x['scores'][c]['penaltyPointsCommitted']})
al.sort(key=lambda r: -r['score'])
top = [dict(r, place=i + 1) for i, r in enumerate(al[:10])]
json.dump(top, open(f'{out}/top_scores.json', 'w'), indent=1)
awards = {19429: 'Finalist, Inspire 3rd', 20245: 'Winner, Inspire 2nd'}
res = []
for x in t:
    n = x['teamNumber']
    if n not in (19429, 20245): continue
    s = x['stats']
    files = sorted(glob.glob(f'{root}/{n}/matches/*.gz'))
    crashes = 0; parks = 0; arr = []
    for p in files:
        d = R.read(p)
        crashes += sum(1 for tt, e in d['/Events'] if 'CRASH' in e)
        a = next(tt for tt, v in d['/DriverStation/RobotMode'] if v == 'autonomous')
        ae = next(tt for tt, v in d['/DriverStation/RobotMode'] if tt > a and v != 'autonomous')
        if any('park' in e.lower() and e.startswith('auto: path') for tt, e in d['/Events'] if a <= tt <= ae): parks += 1
        for tt, e in d['/Events']:
            mm = re.search(r'arrived at [A-Z_]+ in [\d.]+ s, ([\d.]+) in and', e)
            if mm: arr.append(float(mm.group(1)))
    ours = [r for r in al if str(n) in r['alliance'].split(' + ')]
    res.append({'robot': str(n), 'qual_rank': s['rank'], 'teams': len(field), 'wins': s['wins'], 'losses': s['losses'],
                'awards': awards[n], 'matches': len(files), 'crashes': crashes, 'autos_to_park': parks,
                'penalties_committed': sum(r['penalties_committed'] for r in ours),
                'auto_hive_min': s['min']['autoHivePoints'], 'auto_hive_max': s['max']['autoHivePoints'],
                'arrive_median_in': round(st.median(arr), 1), 'arrive_within_2in_pct': round(100 * sum(a <= 2 for a in arr) / len(arr))})
json.dump(res, open(f'{out}/results.json', 'w'), indent=1)
# balls per match added to matches
mt = json.load(open(f'{out}/matches.json'))
for team in ('19429', '20245'):
    for i, p in enumerate(sorted(glob.glob(f'{root}/{team}/matches/*.gz'))):
        d = R.read(p); mo = d['/DriverStation/RobotMode']; sh = d['/Launcher/Shots']
        a = next(tt for tt, v in mo if v == 'autonomous'); ae = next(tt for tt, v in mo if tt > a and v != 'autonomous')
        te = next(tt for tt, v in mo if v == 'teleop'); end = d['/Robot/BatteryVolts'][-1][0]
        s = lambda x: R.at(sh, x) or 0
        row = next(r for r in mt if r['robot'] == team and r['order'] == i + 1)
        row['auto_balls'] = int(s(ae) - s(a)); row['teleop_balls'] = int(s(end) - s(te))
json.dump(mt, open(f'{out}/matches.json', 'w'), indent=1)
print(json.dumps(res, indent=1)); print(top[:4])
