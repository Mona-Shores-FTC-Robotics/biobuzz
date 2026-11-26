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
ASK_CROSS = 'If your preloads miss, do you drive off your start before 10 s, or stay on it until 16 s?'
ASK_LEAVE = 'Does your Auto LEAVE and PARK every time?'

# Our plan per archetype. Sim numbers: sim-results branch, latest run of each Auto's
# sim-request (L-NoCross auto/l-no-cross 25 Nov, R-NoCross claude/fresh-sim-7 23 Nov), mean of 5 seeds.
ARCHETYPES = {
    'RIGHT-SHOOT-PARK': {
        'label': 'RIGHT-SHOOT-PARK', 'standIn': 'partner-r-launch-park (lastShotS 1-5; fails: misses)',
        'auto': 'L-NoCross', 'weStart': 'LEFT_START',
        'init': ['Wait: as drawn, "waiting for TIP 1 until 9 s"',
                 'if no TIP 1: CROSS AND FIX IT only if they promise to drive off their start before 10 s, or stay on it until 16 s, when their preloads miss; otherwise WAIT FOR PARTNER FIX',
                 'Endgame: PARK FIRST (quals); TIPS FIRST in playoffs only if our shots land'],
        'ask': [ASK_SIDE, ASK_CROSS, ASK_RESHOOT, ASK_PARK],
        'sim': '40 pts when they hit, 28 when they miss (reality-check sims, 26 Nov 2026).',
        'watch': 'CROSS AND FIX IT puts us in the right lane from about 9 to 15.3 s: a partner leaving RIGHT_START between about 10.3 and 16 s hits us. Raising our partner wait moves that window later by the same amount.',
        'crossWindow': {'oursInRightLaneS': [9.0, 15.3], 'partnerLeavesHitsS': [10.3, 16.0],
                        'shiftsWithPartnerWait': True, 'source': 'partner reality-check sims, 26 Nov 2026'}},
    'LEFT-SHOOT-PARK': {
        'label': 'LEFT-SHOOT-PARK', 'standIn': 'partner-l-tip-park / partner-l-launch-park',
        'auto': 'R-NoCross', 'weStart': 'RIGHT_START',
        'init': ['Wait: nothing to set (our preloads make TIP 1; they shoot after it)',
                 'if no TIP 2: WAIT FOR PARTNER (default)',
                 'Endgame: PARK FIRST (quals); TIPS FIRST in playoffs only if our shots land'],
        'ask': [ASK_SIDE, 'Do you wait for the LEFT CELL to come up before you shoot?', ASK_PARK],
        'sim': '40 pts, no contact.',
        'watch': 'Their CELL is down until our TIP 1, so their shot depends on ours.'},
    'LEAVE-PARK': {
        'label': 'LEAVE-PARK', 'standIn': 'partner-leave-park',
        'auto': 'R-Solo (with #759), else R-NoCross', 'weStart': 'RIGHT_START',
        'init': ['R-Solo: nothing to set',
                 'R-NoCross: if no TIP 2: CROSS AND FIX IT (36 pts, no contact beside a LEAVE-PARK partner)',
                 'Endgame: PARK FIRST (their park is our SWARM)'],
        'ask': [ASK_SIDE, ASK_LEAVE, ASK_PARK],
        'sim': 'R-Solo 36 pts, 0 contacts (with #759). R-NoCross on CROSS AND FIX IT 36 pts, no contact.',
        'watch': 'SWARM needs their LEAVE and PARK too.'},
    'STILL': {
        'label': 'STILL', 'standIn': 'partner-still (corner) / partner-zone-still (LOADING ZONE)',
        'auto': 'R-Solo or R-NoCross', 'weStart': 'RIGHT_START',
        'init': ['R-NoCross: if no TIP 2: CROSS AND FIX IT (28 pts, no contact beside a partner in the corner)',
                 'Endgame: PARK FIRST'],
        'ask': ['Do you have an Auto at all?', 'If you won\'t move, sit in the corner past the LOADING ZONE tape.'],
        'sim': 'In the corner: R-NoCross on CROSS AND FIX IT 28 pts, no contact. In the LOADING ZONE: 33 pts, contact at 26.5 s at our RIGHT_PARK.',
        'watch': 'A still partner in the LOADING ZONE touches our RIGHT_PARK: ask it to sit in the corner.'},
    'CROSSER': {
        'label': 'CROSSER', 'standIn': None,
        'auto': 'L-NoCross on WAIT FOR PARTNER FIX, or R-NoCross', 'weStart': 'the start it leaves free',
        'init': ['Take whichever start it leaves free',
                 'L-NoCross: if no TIP 1: WAIT FOR PARTNER FIX; R-NoCross: if no TIP 2: WAIT FOR PARTNER. Never CROSS AND FIX IT',
                 'Endgame: PARK FIRST'],
        'ask': [ASK_SIDE, 'Where are you at 10 s and 20 s, and where do you park?'],
        'sim': 'No stand-in: never seen as a partner. Its route needs video.',
        'watch': 'Contact risk: it uses both ends of the field in AUTO.'},
}

# The States rule (mentor, 26 Nov 2026): R-Solo beside every partner, which starts LEFT, LEAVEs and is
# parked or out of the left end by about 9 s. L-NoCross only beside a partner seen on video starting
# RIGHT and tipping (verifiedRightTipper). Exceptions are a plan id in overrides.json.
ASK_LEFT = 'Please start LEFT, LEAVE, and be parked or out of the left end by about 9 s. Can you?'
PLANS = {
    'R-SOLO': {
        'auto': 'R-Solo', 'weStart': 'RIGHT_START',
        'init': ['Partner starts LEFT and is parked or out of the left end by about 9 s',
                 'R-Solo: nothing else to set',
                 'Endgame: PARK FIRST (quals); TIPS FIRST in playoffs only if our shots land'],
        'ask': [ASK_LEFT, ASK_LEAVE, ASK_PARK],
        'sim': 'Beside a LEAVE-PARK partner 36 pts, 0 contacts (needs #759 on the robot). A partner still in the LOADING ZONE touches our RIGHT_PARK: ask it to sit in the corner past the tape.',
        'why': 'Default: not seen on video starting RIGHT and tipping.'},
    'L-NOCROSS': {
        'auto': 'L-NoCross', 'weStart': 'LEFT_START',
        'init': ['Wait: as drawn, "waiting for TIP 1 until 9 s"',
                 'if no TIP 1: CROSS AND FIX IT only if they promise to drive off their start before 10 s, or stay on it until 16 s, when their preloads miss; otherwise WAIT FOR PARTNER FIX',
                 'Endgame: PARK FIRST (quals); TIPS FIRST in playoffs only if our shots land'],
        'ask': [ASK_CROSS, ASK_RESHOOT, ASK_PARK],
        'sim': '40 pts when they hit, 28 when they miss. Our cross is in the right lane about 9-15.3 s; a partner leaving RIGHT_START at 10.3-16 s hits us.',
        'why': 'Seen on video starting RIGHT and tipping.'},
    'L-NOCROSS-WAIT': {
        'auto': 'L-NoCross', 'weStart': 'LEFT_START',
        'init': ['Wait: as drawn, "waiting for TIP 1 until 9 s"',
                 'if no TIP 1: WAIT FOR PARTNER FIX (never cross: it sits on our right launch spot)',
                 'Endgame: PARK FIRST'],
        'ask': [ASK_SIDE, ASK_PARK],
        'sim': 'Not simulated with this partner.',
        'why': 'Exception: it waits on RIGHT_AUTO_HIVE 7-19 s, so no R- Auto beside it.'},
    'R-NOCROSS': {
        'auto': 'R-NoCross', 'weStart': 'RIGHT_START',
        'init': ['Partner starts LEFT and shoots at the LEFT CELL after our TIP 1',
                 'if no TIP 2: WAIT FOR PARTNER (default)',
                 'Endgame: PARK FIRST (quals); TIPS FIRST in playoffs only if our shots land'],
        'ask': ['Will you start LEFT and shoot at the LEFT CELL?', ASK_PARK],
        'sim': '40 pts, no contact beside a left shooter; 3 of 3 on video, no contact.',
        'why': 'Exception: a left shooter (seen on video beside our R-NoCross).'},
}


def plan_for(arch, o):
    if o.get('plan'):
        return o['plan']
    return 'L-NOCROSS' if o.get('verifiedRightTipper') else 'R-SOLO'


def guess(a):
    if a['matches'] == 0:
        return 'STILL', 'no matches this season'
    leave = a['leaveRecent'] if a['leaveRecent'] is not None else a['leave']
    tip = a['tipShare']
    if leave < 0.5:
        return 'STILL', 'LEAVE in under half its last event\'s matches'
    if tip >= 1.0 and a['allianceTipsDist'].get('2', 0) + a['allianceTipsDist'].get('3', 0) >= a['matches'] / 3:
        return 'CROSSER', 'TIP share over 1 and 2+ TIPs in a third of its matches'
    if tip < 0.4:
        return 'LEAVE-PARK', 'TIP share under 0.4: of 20 qualifier partners on our video none tipped, and low scorers ran LEAVE-PARK from the left'
    return 'RIGHT-SHOOT-PARK', 'tips with its preloads; side not seen (only the RIGHT CELL is up at the start, so a preload TIP is usually from the right)'


def likely_side(arch, hit, o):
    """(likely start side, confidence). Seen on video: high. Else from the archetype: in our 32 partner
    runs every LEAVE-PARK started left, and a preload TIP needs the RIGHT CELL, which alone is up at the start."""
    if o.get('side'):
        return o['side'], 'high'
    if arch == 'LEAVE-PARK':
        return 'LEFT', 'medium'
    if arch == 'RIGHT-SHOOT-PARK':
        return 'RIGHT', 'medium' if hit >= 0.6 else 'low'
    if arch == 'LEFT-SHOOT-PARK':
        return 'LEFT', 'medium'
    return None, 'none'


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
        side, conf = likely_side(arch, hit, o)
        shoots = 'none' if arch in ('LEAVE-PARK', 'STILL') else ('hits' if hit >= 0.6 else 'misses')
        teams.append({
            'team': int(t), 'name': a['name'], 'city': a['city'],
            'archetype': arch, 'archetypeFrom': 'scouted' if 'archetype' in o else 'guessed', 'why': why,
            'plan': plan_for(arch, o), 'planFixed': bool(o.get('plan')),
            'verifiedRightTipper': bool(o.get('verifiedRightTipper')),
            'verifyAtMidland': not o.get('verifiedRightTipper') and not o.get('plan') and arch == 'RIGHT-SHOOT-PARK' and side == 'RIGHT',
            'side': o.get('side'), 'likelySide': side, 'sideConfidence': conf, 'shoots': o.get('shoots', shoots), 'lastShotS': o.get('lastPreloadS'), 'note': o.get('note'),
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
                'likelySide/sideConfidence: seen on video = high; otherwise from the archetype (LEAVE-PARK: LEFT, medium; RIGHT-SHOOT-PARK: RIGHT, medium if tipRate 0.6+, else low). shoots: hits (own TIP rate 0.6+), misses (it tips, under 0.6) or none; lastShotS: its last preload, from video. Archetype names are the partner reality-check chat\'s. leave/park are its own, per robot, all season; *Recent are its last event. allianceAuto is its alliance\'s AUTO points (best/median/worst).',
        'event': an['event'], 'eventName': 'Michigan FiM Championship NW (Midland, 10-12 Dec 2026)',
        'built': time.strftime('%Y-%m-%d'), 'dataFetched': an['fetched'],
        'rule': 'R-Solo beside every partner (it starts LEFT, LEAVEs, is parked or out of the left end by about 9 s); L-NoCross only beside a partner seen on video starting RIGHT and tipping. Mentor, 26 Nov 2026.',
        'plans': PLANS, 'archetypes': ARCHETYPES, 'teams': teams,
    }
    json.dump(doc, open(OUT, 'w'), indent=1)
    from collections import Counter
    print(Counter(x['plan'] for x in teams), 'verify:', [x['team'] for x in teams if x['verifyAtMidland']])
    print(Counter(x['archetype'] for x in teams), 'hard:', [x['team'] for x in teams if x['hardToPlan']])


def page():
    """tools/states-scout/out/states-partners.html: page.html with the catalog inlined."""
    data = open(OUT).read()
    html = open(os.path.join(HERE, 'page.html')).read().replace('/*DATA*/null', data)
    open(os.path.join(HERE, 'out', 'states-partners.html'), 'w').write(html)


if __name__ == '__main__':
    main()
    page()
