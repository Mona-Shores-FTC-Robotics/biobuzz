"""States partner catalog: every team at an event, and what its AUTO scored this season.

  python3 tools/states-scout/fetch.py [--event USMINWCMP] [--refetch]

Reads FTCScout's REST API (api.ftcscout.org/rest/v1), caches every response under
tools/states-scout/cache/ (git-ignored; --refetch drops the cache for events still running and
the team list), and writes tools/states-scout/out/stats.json: per team, every AUTO it played,
its own LEAVE and PARK (FTCScout scores those per robot), the alliance's AUTO TIPs and AUTO
points, and the partner. build.py turns that and archetypes.json into
TeamCode/autos/states-partners.json. Stdlib only.
"""
import argparse, json, os, ssl, statistics as st, sys, time, urllib.request

API = 'https://api.ftcscout.org/rest/v1'
SEASON = 2026
HERE = os.path.dirname(os.path.abspath(__file__))
CACHE = os.path.join(HERE, 'cache')
OURS = {19429, 20245}


def ctx():
    ca = os.environ.get('SSL_CERT_FILE') or ('/root/.ccr/ca-bundle.crt' if os.path.exists('/root/.ccr/ca-bundle.crt') else None)
    return ssl.create_default_context(cafile=ca)


def get(name, path, fresh=False):
    f = os.path.join(CACHE, name)
    if os.path.exists(f) and not fresh:
        return json.load(open(f))
    for attempt in range(4):
        try:
            req = urllib.request.Request(API + path, headers={'User-Agent': 'ftc-states-scout/1', 'Accept': 'application/json'})
            with urllib.request.urlopen(req, timeout=40, context=ctx()) as r:
                data = json.loads(r.read().decode())
            break
        except Exception as e:
            if attempt == 3:
                raise SystemExit('could not fetch %s%s: %s' % (API, path, e))
            time.sleep(2 ** attempt)
    os.makedirs(os.path.dirname(f), exist_ok=True)
    json.dump(data, open(f, 'w'))
    return data


STATION = {'One': '1', 'Two': '2'}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--event', default='USMINWCMP')
    ap.add_argument('--refetch', action='store_true')
    a = ap.parse_args()
    ev = get('event-%s.json' % a.event, '/events/%d/%s' % (SEASON, a.event), a.refetch)
    teams = get('teams-%s.json' % a.event, '/events/%d/%s/teams' % (SEASON, a.event), a.refetch)
    numbers = sorted(t['teamNumber'] for t in teams)
    out = {'event': a.event, 'eventName': ev['name'], 'fetched': time.strftime('%Y-%m-%d'), 'teams': {}}
    events_seen = {}
    for n in numbers:
        tev = get('team-%d-events.json' % n, '/teams/%d/events/%d' % (n, SEASON), a.refetch)
        info = get('team-%d.json' % n, '/teams/%d' % n)
        qs = get('team-%d-qs.json' % n, '/teams/%d/quick-stats?season=%d' % (n, SEASON), a.refetch)
        rows = []
        for e in tev:
            code = e['eventCode']
            if code == a.event:
                continue
            if code not in events_seen:
                meta = get('event-%s.json' % code, '/events/%d/%s' % (SEASON, code), a.refetch)
                fresh = a.refetch and not meta.get('finished', True)
                events_seen[code] = (meta, get('matches-%s.json' % code, '/events/%d/%s/matches' % (SEASON, code), fresh))
            meta, matches = events_seen[code]
            for m in matches:
                if not m.get('hasBeenPlayed') or not m.get('scores'):
                    continue
                me = [t for t in m['teams'] if t['teamNumber'] == n]
                if not me:
                    continue
                me = me[0]
                if me.get('noShow') or not me.get('onField', True):
                    continue
                al = me['alliance'].lower()
                s = m['scores'].get(al)
                if not s or 'autoLeave1' not in s:
                    continue
                k = STATION.get(me['station'])
                partner = [t['teamNumber'] for t in m['teams'] if t['alliance'] == me['alliance'] and t['teamNumber'] != n]
                pk = '2' if k == '1' else '1'
                rows.append({
                    'event': code, 'eventName': meta['name'], 'date': meta['start'],
                    'match': ('Q%d' % m['id']) if m['tournamentLevel'] == 'Quals' else 'P%d' % m.get('series', m['id']),
                    'alliance': al, 'partner': partner[0] if partner else None,
                    'leave': s['autoLeave' + k] > 0, 'park': s['autoParkPoints' + k] > 0,
                    'partnerLeave': s['autoLeave' + pk] > 0, 'partnerPark': s['autoParkPoints' + pk] > 0,
                    'allianceTips': s['autoHiveTips'], 'allianceAuto': s['autoPoints'],
                    'start': m.get('actualStartTime') or m.get('scheduledStartTime') or '',
                })
        rows.sort(key=lambda r: r['start'])
        out['teams'][str(n)] = {
            'name': info.get('name'), 'city': info.get('city'), 'ours': n in OURS,
            'autoOpr': round(qs['auto']['value'], 1) if qs and qs.get('auto') else None,
            'autoOprRank': qs['auto']['rank'] if qs and qs.get('auto') else None,
            'totOpr': round(qs['tot']['value'], 1) if qs and qs.get('tot') else None,
            'events': [e['eventCode'] for e in tev if e['eventCode'] != a.event],
            'matches': rows,
        }
        print(n, info.get('name'), len(rows), 'matches', file=sys.stderr)
    os.makedirs(os.path.join(HERE, 'out'), exist_ok=True)
    json.dump(out, open(os.path.join(HERE, 'out', 'stats.json'), 'w'), indent=1)


if __name__ == '__main__':
    main()
