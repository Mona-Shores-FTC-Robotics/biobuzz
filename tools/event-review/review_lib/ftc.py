"""Official results from FTCScout's REST API (api.ftcscout.org/rest/v1), cached as JSON files.

Every response is saved under <out>/ftcscout/ the first time, and read from there afterwards, so a
review can be rebuilt offline and a blocked network only means copying those files in. Files:
  event.json, matches.json, teams.json        /events/<season>/<code>[/matches|/teams]
  awards-<team>.json                           /teams/<team>/awards?season=<season>
  quick-stats-<team>.json                      /teams/<team>/quick-stats?season=<season>  (world ranks)
"""
import json
import os
import ssl
import statistics as st
import urllib.request

API = 'https://api.ftcscout.org/rest/v1'


class FetchError(Exception):
    """A response is neither saved nor reachable."""


class FTCScout:
    def __init__(self, season, code, cache_dir, offline=False):
        self.season, self.code, self.dir, self.offline = season, code, cache_dir, offline
        os.makedirs(cache_dir, exist_ok=True)

    def get(self, name, path):
        f = os.path.join(self.dir, name)
        if os.path.exists(f):
            return json.load(open(f))
        if self.offline:
            raise FetchError('missing %s and --offline was given: save %s%s there' % (f, API, path))
        ctx = ssl.create_default_context(cafile=os.environ.get('SSL_CERT_FILE') or
                                         ('/root/.ccr/ca-bundle.crt' if os.path.exists('/root/.ccr/ca-bundle.crt') else None))
        try:
            req = urllib.request.Request(API + path, headers={'User-Agent': 'biobuzz-event-review/1 (FTC team 19429/20245)',
                                                              'Accept': 'application/json'})
            with urllib.request.urlopen(req, timeout=30, context=ctx) as r:
                data = json.loads(r.read().decode())
        except Exception as e:  # network blocked or host down: say which file to provide
            raise FetchError('could not fetch %s%s (%s). Save that response as %s and rerun.' % (API, path, e, f))
        json.dump(data, open(f, 'w'), indent=1)
        return data

    def event(self):
        return self.get('event.json', '/events/%s/%s' % (self.season, self.code))

    def matches(self):
        return self.get('matches.json', '/events/%s/%s/matches' % (self.season, self.code))

    def teams(self):
        return self.get('teams.json', '/events/%s/%s/teams' % (self.season, self.code))

    def awards(self, team):
        return self.get('awards-%s.json' % team, '/teams/%s/awards?season=%s' % (team, self.season))

    def quick_stats(self, team):
        return self.get('quick-stats-%s.json' % team, '/teams/%s/quick-stats?season=%s' % (team, self.season))


def match_label(x, playoff_names):
    """'Q4', or a playoff's name from the event config ('Upper final'), else 'Playoff M<series>'."""
    if x['tournamentLevel'] == 'Quals':
        return 'Q%d' % x['id']
    return playoff_names.get(str(x['series']), 'Playoff M%d' % x['series'])


def our_matches(matches, team):
    """The matches a team played, in start order: [(label-ready match dict, alliance)]."""
    out = []
    for x in matches:
        for t in x['teams']:
            if t['teamNumber'] == int(team) and x.get('hasBeenPlayed', True):
                out.append((x, t['alliance']))
    out.sort(key=lambda p: p[0].get('actualStartTime') or p[0].get('scheduledStartTime') or '')
    return out


AWARD_NAMES = {'Winner': 'Winner', 'Finalist': 'Finalist', 'Inspire': 'Inspire', 'Think': 'Think',
               'Connect': 'Connect', 'Innovate': 'Innovate', 'Control': 'Control', 'Motivate': 'Motivate',
               'Design': 'Design', 'Reach': 'Reach', 'Sustain': 'Sustain', 'JudgesChoice': "Judges' Choice",
               'DeansListFinalist': "Dean's List Finalist", 'DeansListSemiFinalist': "Dean's List Semi-Finalist"}
ORD = {1: '1st', 2: '2nd', 3: '3rd', 4: '4th', 5: '5th'}


def awards_line(awards, code):
    """'Finalist, Inspire 3rd': alliance awards (Winner/Finalist) first, then judged ones with their place."""
    rows = [a for a in awards if a.get('eventCode') == code]
    alliance = [a for a in rows if a['type'] in ('Winner', 'Finalist')]
    judged = sorted((a for a in rows if a['type'] not in ('Winner', 'Finalist')), key=lambda a: a.get('placement') or 9)
    parts = [AWARD_NAMES.get(a['type'], a['type']) for a in alliance]
    parts += [('%s %s' % (AWARD_NAMES.get(a['type'], a['type']), ORD.get(a.get('placement'), ''))).strip() for a in judged]
    return ', '.join(parts) or '—'


def datasets(fs, cfg, ours, crashes, autos_to_park, matches_played):
    """results, field_opr, top_scores, world_ranks from FTCScout (+ two numbers from the logs)."""
    teams = fs.teams()
    matches = fs.matches()
    playoff_names = cfg.get('playoff_names', {})
    field = []
    for x in teams:
        s = x.get('stats')
        if not s:
            continue
        o = s['opr']
        field.append({'team': str(x['teamNumber']), 'ours': str(x['teamNumber']) in ours,
                      'opr_total': round(o['totalPointsNp'], 1), 'opr_auto': round(o['autoPoints'], 1),
                      'opr_teleop': round(o['dcPoints'], 1), 'opr_auto_hive': round(o['autoHivePoints'], 1),
                      'opr_teleop_hive': round(o['dcHivePoints'], 1), 'opr_tips': round(o['tips'], 2)})
    al = []
    for x in matches:
        if not x.get('scores'):
            continue
        for c in ('red', 'blue'):
            ts = [str(t['teamNumber']) for t in x['teams'] if t['alliance'].lower() == c]
            al.append({'match': match_label(x, playoff_names), 'alliance': ' + '.join(ts),
                       'score': x['scores'][c]['totalPoints'], 'auto': x['scores'][c]['autoPoints'],
                       'ours': bool(set(ours) & set(ts)), 'penalties_committed': x['scores'][c]['penaltyPointsCommitted']})
    al.sort(key=lambda r: -r['score'])  # stable: ties keep FTCScout's match order
    top = [dict(r, place=i + 1) for i, r in enumerate(al[:10])]
    played = [x for x in teams if x.get('stats')]
    med = lambda f: round(st.median(f(x['stats']) for x in played), 1)
    fm = {'field_avg_np': med(lambda s: s['avg']['totalPointsNp']), 'field_avg_auto': med(lambda s: s['avg']['autoPoints']),
          'field_avg_teleop': med(lambda s: s['avg']['dcPoints']), 'field_avg_tips': med(lambda s: s['avg']['tips'])}
    results = []
    for team in ours:
        x = next((x for x in teams if str(x['teamNumber']) == team and x.get('stats')), None)
        if x is None:
            continue
        s = x['stats']
        mine = [r for r in al if team in r['alliance'].split(' + ')]
        results.append({'robot': team, 'qual_rank': s['rank'], 'teams': len(field), 'wins': s['wins'], 'losses': s['losses'],
                        'awards': awards_line(fs.awards(team), fs.code), 'matches': matches_played.get(team, 0),
                        'crashes': crashes.get(team, 0), 'autos_to_park': autos_to_park.get(team, 0),
                        'penalties_committed': sum(r['penalties_committed'] for r in mine),
                        'auto_hive_min': s['min']['autoHivePoints'], 'auto_hive_max': s['max']['autoHivePoints'],
                        'avg_np': round(s['avg']['totalPointsNp'], 1), 'avg_auto': round(s['avg']['autoPoints'], 1),
                        'avg_teleop': round(s['avg']['dcPoints'], 1), 'avg_tips': round(s['avg']['tips'], 1),
                        'opr_total': round(s['opr']['totalPointsNp'], 1), 'high_np': s['max']['totalPointsNp'],
                        'name': cfg['teams'][team], **fm})
    world = []
    for team in ours:
        q = fs.quick_stats(team)
        for k, name, o in (('tot', 'Total', 1), ('auto', 'Auto', 2), ('dc', 'TeleOp', 3)):
            r = q[k]['rank']
            world.append({'robot': team, 'measure': name, 'order': o, 'rank': r, 'of': q['count'],
                          'top_pct': round(100 * r / q['count'], 1)})
    return {'results': results, 'field_opr': field, 'top_scores': top, 'world_ranks': world}
