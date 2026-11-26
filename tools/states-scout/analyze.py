"""Per-team AUTO summary from fetch.py's cache: rates, TIP share, consistency.

Writes tools/states-scout/out/analysis.json. A team's own AUTO TIPs are not scored per robot,
so its TIP share is a ridge least-squares fit (an OPR) of autoHiveTips over every played match
of every event any States team went to this season, all teams in them included.
"""
import glob, json, os, statistics as st
HERE = os.path.dirname(os.path.abspath(__file__))
CACHE = os.path.join(HERE, 'cache')
stats = json.load(open(os.path.join(HERE, 'out', 'stats.json')))


def tip_opr(lam=0.6):
    eqs = []  # (teams, tips)
    for f in glob.glob(os.path.join(CACHE, 'matches-*.json')):
        for m in json.load(open(f)):
            if not m.get('hasBeenPlayed') or not m.get('scores'):
                continue
            for al in ('Red', 'Blue'):
                s = m['scores'].get(al.lower())
                if not s or 'autoHiveTips' not in s:
                    continue
                ts = [t['teamNumber'] for t in m['teams'] if t['alliance'] == al and not t.get('noShow')]
                if ts:
                    eqs.append((ts, s['autoHiveTips']))
    idx = {}
    for ts, _ in eqs:
        for t in ts:
            idx.setdefault(t, len(idx))
    n = len(idx)
    # normal equations (A^T A + lam I) x = A^T b, solved by Gauss-Seidel (sparse, stdlib only)
    ata = [dict() for _ in range(n)]
    atb = [0.0] * n
    for ts, b in eqs:
        ii = [idx[t] for t in ts]
        for i in ii:
            atb[i] += b
            for j in ii:
                ata[i][j] = ata[i].get(j, 0) + 1
    x = [0.0] * n
    for _ in range(400):
        for i in range(n):
            s = atb[i] - sum(v * x[j] for j, v in ata[i].items() if j != i)
            x[i] = s / (ata[i].get(i, 0) + lam)
    return {t: x[i] for t, i in idx.items()}, len(eqs)


def main():
    opr, neq = tip_opr()
    out = {'event': stats['event'], 'fetched': stats['fetched'], 'equations': neq, 'teams': {}}
    for t, d in stats['teams'].items():
        ms = d['matches']
        n = len(ms)
        def rate(k, sub=ms):
            return round(sum(1 for m in sub if m[k]) / len(sub), 2) if sub else None
        recent = [m for m in ms if m['event'] == ms[-1]['event']] if ms else []
        own = [3 * m['leave'] + 5 * m['park'] for m in ms]
        al = [m['allianceAuto'] for m in ms]
        solo_tips = [m['allianceTips'] for m in ms if m['partner'] is not None and opr.get(m['partner'], 0) < 0.2]
        out['teams'][t] = {
            'name': d['name'], 'city': d['city'], 'ours': d['ours'], 'matches': n,
            'events': [e for e in dict.fromkeys(m['eventName'] for m in ms)],
            'autoOpr': d['autoOpr'], 'autoOprRank': d['autoOprRank'], 'totOpr': d['totOpr'],
            'leave': rate('leave'), 'park': rate('park'),
            'leaveRecent': rate('leave', recent), 'parkRecent': rate('park', recent), 'recentEvent': recent[0]['eventName'] if recent else None,
            'tipShare': round(opr.get(int(t), 0), 2),
            'tipsWithWeakPartner': round(st.mean(solo_tips), 2) if solo_tips else None, 'weakPartnerMatches': len(solo_tips),
            'allianceAuto': {'best': max(al), 'median': st.median(al), 'worst': min(al)} if al else None,
            'ownLeavePark': {'best': max(own), 'median': st.median(own), 'worst': min(own)} if own else None,
            'allianceTipsDist': {str(k): sum(1 for m in ms if m['allianceTips'] == k) for k in range(4)},
        }
    json.dump(out, open(os.path.join(HERE, 'out', 'analysis.json'), 'w'), indent=1)


if __name__ == '__main__':
    main()
