"""Build TeamCode/autos/states-partners.json from analysis.json and overrides.json.

  python3 tools/states-scout/fetch.py --refetch && python3 tools/states-scout/analyze.py && python3 tools/states-scout/build.py

Each team gets an archetype (overrides.json wins; else a guess from the scores, marked
"guessed"), and the archetype gives our Auto, its INIT settings and the queue questions.
"""
import json, os, time
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.normpath(os.path.join(HERE, '..', '..'))
OUT = os.path.join(ROOT, 'TeamCode', 'autos', 'states-partners.json')

ASK_SIDE = 'Which side do you start, and do you shoot your preloads right at the start?'
ASK_RESHOOT = 'If your preloads miss, do you shoot at that CELL again?'
ASK_PARK = 'Where do you park, and by when?'
ASK_LEAVE = 'Does your Auto LEAVE and PARK every time?'

# Our plan per archetype. Sim numbers: sim-results branch, latest run of each Auto's
# sim-request (L-NoCross auto/l-no-cross 25 Nov, R-NoCross claude/fresh-sim-7 23 Nov), mean of 5 seeds.
ARCHETYPES = {
    'R_SHOOT_PARK': {
        'label': 'Right-start shooter, then park',
        'auto': 'L-NoCross', 'weStart': 'LEFT_START',
        'init': ['Wait: shoots right at the start -> leave "waiting for TIP 1 until 9 s"; later -> D-pad right to 6 s after their second',
                 'if no TIP 1: CROSS AND FIX IT (default) - WAIT FOR PARTNER FIX only if they re-shoot',
                 'Endgame: PARK FIRST (quals); TIPS FIRST in playoffs only if our shots land'],
        'ask': [ASK_SIDE, ASK_RESHOOT, ASK_PARK],
        'sim': 'On time 48 pts, 1.6 TIPs, parked 100%. Missing: CROSS AND FIX IT 32 pts vs WAIT FOR PARTNER FIX 16. Dead: 28 vs 8. No contact in any run.',
        'watch': 'CROSS AND FIX IT drives under the HIVE to RIGHT_AUTO_FIX_TIP: if they are still near the right launch spot after ~12 s, pick WAIT FOR PARTNER FIX.'},
    'L_SHOOT_PARK': {
        'label': 'Left-start shooter, then park',
        'auto': 'R-NoCross', 'weStart': 'RIGHT_START',
        'init': ['Wait: nothing to set (our preloads make TIP 1; they shoot after it)',
                 'if no TIP 2: WAIT FOR PARTNER (default) - CROSS AND FIX IT only if they park out of the wall lane and are clear by 20 s',
                 'Endgame: PARK FIRST (quals); TIPS FIRST in playoffs only if our shots land'],
        'ask': [ASK_SIDE, ASK_PARK],
        'sim': 'On time 56 pts, 2.0 TIPs. Missing 36 pts, 1 TIP. With CROSS AND FIX IT beside a fast partner 56, no contact.',
        'watch': 'Their CELL is down until our TIP 1: they must wait for it, so ask "do you wait for the LEFT CELL to come up?"'},
    'LEAVE_PARK': {
        'label': 'LEAVE / park only',
        'auto': 'R-NoCross', 'weStart': 'RIGHT_START',
        'init': ['Ask them to start LEFT. Wait: nothing to set',
                 'if no TIP 2: WAIT FOR PARTNER (default)',
                 'Endgame: PARK FIRST (their park is our SWARM)'],
        'ask': ['Can you start on the left?', ASK_LEAVE, ASK_PARK],
        'sim': 'Beside a still partner 28 pts, 1 TIP, no contact (R-NoCross, partner-still). If they must start right: L-NoCross, wait D-pad left to 0, CROSS AND FIX IT (28 pts beside a dead right partner).',
        'watch': 'We get one TIP alone; SWARM needs their LEAVE and PARK too.'},
    'STILL': {
        'label': 'Still or dead',
        'auto': 'R-NoCross', 'weStart': 'RIGHT_START',
        'init': ['They sit on LEFT_START: R-NoCross, if no TIP 2: WAIT FOR PARTNER',
                 'If they can only sit on the right: L-NoCross, wait D-pad left to 0, if no TIP 1: CROSS AND FIX IT',
                 'Endgame: PARK FIRST'],
        'ask': ['Do you have an Auto at all? Will it LEAVE?', 'Which start do you want to sit on?'],
        'sim': 'R-NoCross beside a dead left partner 28 pts, 1 TIP, no contact. L-NoCross beside a dead right partner, CROSS AND FIX IT: 28 pts, 1 TIP, 2.2 in clear of it.',
        'watch': 'Never run R-Clockwise/R-LeftUnderHive halves beside a dead LEFT_START robot (known overlap).'},
    'CROSSER': {
        'label': 'Crosser / makes more than one TIP',
        'auto': 'L-NoCross or R-NoCross (the side they leave us)', 'weStart': 'their other side',
        'init': ['Never CROSS AND FIX IT: their route crosses ours',
                 'L-NoCross: if no TIP 1: WAIT FOR PARTNER FIX; R-NoCross: if no TIP 2: WAIT FOR PARTNER',
                 'Endgame: PARK FIRST'],
        'ask': [ASK_SIDE, 'Draw us your path: where are you at 10 s and 20 s, and where do you park?'],
        'sim': 'Not simulated as a stand-in yet: plan from their answer.',
        'watch': 'Contact risk: these teams use both ends of the field in AUTO.'},
}


def guess(a):
    if a['matches'] == 0:
        return 'STILL', 'no matches this season'
    leave = a['leaveRecent'] if a['leaveRecent'] is not None else a['leave']
    tip = a['tipShare']
    if leave < 0.5:
        return 'STILL', 'LEAVE in under half its last event\'s matches'
    if tip >= 1.0 and a['allianceTipsDist'].get('2', 0) + a['allianceTipsDist'].get('3', 0) >= a['matches'] / 3:
        return 'CROSSER', 'TIP share over 1 and 2+ TIPs in a third of its matches'
    if tip < 0.25:
        return 'LEAVE_PARK', 'TIP share under 0.25'
    return 'R_SHOOT_PARK', 'tips with its preloads; side not seen (only the RIGHT CELL is up at the start, so a preload TIP is usually from the right)'


def main():
    an = json.load(open(os.path.join(HERE, 'out', 'analysis.json')))
    ov = json.load(open(os.path.join(HERE, 'overrides.json')))['teams']
    teams = []
    for t, a in sorted(an['teams'].items(), key=lambda kv: int(kv[0])):
        if a['ours']:
            continue
        arch, why = guess(a)
        o = ov.get(t, {})
        if 'archetype' in o:
            arch, why = o['archetype'], o.get('source', 'scouted')
        hit = max(0.0, min(1.0, a['tipShare']))
        teams.append({
            'team': int(t), 'name': a['name'], 'city': a['city'],
            'archetype': arch, 'archetypeFrom': 'scouted' if 'archetype' in o else 'guessed', 'why': why,
            'side': o.get('side'), 'lastPreloadS': o.get('lastPreloadS'), 'note': o.get('note'),
            'tipRate': round(hit, 2), 'leave': a['leave'], 'park': a['park'],
            'leaveRecent': a['leaveRecent'], 'parkRecent': a['parkRecent'], 'recentEvent': a['recentEvent'],
            'matches': a['matches'], 'events': a['events'],
            'allianceAuto': a['allianceAuto'], 'autoOpr': a['autoOpr'], 'autoOprRank': a['autoOprRank'],
            'allianceTipsDist': a['allianceTipsDist'],
            'hardToPlan': arch in ('CROSSER', 'STILL') or (a['leaveRecent'] or 0) < 0.8 or (a['parkRecent'] or 0) < 0.6,
        })
    doc = {
        '_doc': 'Written by tools/states-scout/build.py; refresh with fetch.py --refetch, analyze.py, build.py. '
                'tipRate is the team\'s own AUTO TIPs per match, a least-squares share of its alliances\' TIPs (FTCScout scores TIPs per alliance). '
                'leave/park are its own, per robot, all season; *Recent are its last event. allianceAuto is its alliance\'s AUTO points (best/median/worst).',
        'event': an['event'], 'eventName': 'Michigan FiM Championship NW (Midland, 10-12 Dec 2026)',
        'built': time.strftime('%Y-%m-%d'), 'dataFetched': an['fetched'],
        'archetypes': ARCHETYPES, 'teams': teams,
    }
    json.dump(doc, open(OUT, 'w'), indent=1)
    from collections import Counter
    print(Counter(x['archetype'] for x in teams), 'hard:', [x['team'] for x in teams if x['hardToPlan']])


def page():
    """tools/states-scout/out/states-partners.html: page.html with the catalog inlined."""
    data = open(OUT).read()
    html = open(os.path.join(HERE, 'page.html')).read().replace('/*DATA*/null', data)
    open(os.path.join(HERE, 'out', 'states-partners.html'), 'w').write(html)


if __name__ == '__main__':
    main()
    page()
