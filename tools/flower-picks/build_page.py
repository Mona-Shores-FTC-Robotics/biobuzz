#!/usr/bin/env python3
"""Builds the FLOWER pick study page (one HTML file) from doc/flower-picks/picks.json.

  python3 tools/flower-picks/flower_picks.py report      # writes picks.json
  python3 tools/flower-picks/build_page.py [--out FILE]  # writes the page (default TeamCode/build/flower-picks/study.html)

The findings text is doc/flower-picks/findings.html (hand-written, kept beside the data); everything
else on the page is computed here from picks.json, so a re-run after a practice updates the numbers.
"""
import argparse
import html
import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
DOC = os.path.join(ROOT, 'doc', 'flower-picks')

CSS = """
:root {
  /* Layout: a lab-notebook study, summary bars first, then the pick ledger. */
  --bg: #f5f7f4; --panel: #ffffff; --fg: #1d2420; --muted: #5d6a62; --line: #d6ddd7;
  --accent: #2f6b4f; --four: #2f6b4f; --three: #8fae5d; --two: #d9a441; --one: #d0743c; --zero: #b0413e;
  --q: #b0413e; --chip: #e8eee9;
  --display: "Barlow Semi Condensed", "Arial Narrow", system-ui, sans-serif;
  --body: "IBM Plex Sans", system-ui, -apple-system, sans-serif;
  --mono: "IBM Plex Mono", ui-monospace, Menlo, monospace;
}
@media (prefers-color-scheme: dark) { :root:not([data-theme="light"]) {
  --bg: #121614; --panel: #1a201d; --fg: #e3e9e5; --muted: #9aa8a0; --line: #2c3530;
  --accent: #7cc39b; --four: #5fb386; --three: #9cbc68; --two: #d9a441; --one: #dc8452; --zero: #d0605c;
  --q: #e07b77; --chip: #24302a; color-scheme: dark } }
:root[data-theme="dark"] {
  --bg: #121614; --panel: #1a201d; --fg: #e3e9e5; --muted: #9aa8a0; --line: #2c3530;
  --accent: #7cc39b; --four: #5fb386; --three: #9cbc68; --two: #d9a441; --one: #dc8452; --zero: #d0605c;
  --q: #e07b77; --chip: #24302a; color-scheme: dark }
body { background: var(--bg); color: var(--fg); font: 15px/1.55 var(--body); }
.wrap { max-width: 1100px; margin: 0 auto; padding-block: 28px 64px;
  padding-left: max(16px, env(safe-area-inset-left, 0px)); padding-right: max(16px, env(safe-area-inset-right, 0px));
  display: grid; gap: 28px; }
h1, h2, h3 { font-family: var(--display); text-wrap: balance; margin: 0; letter-spacing: .01em; }
h1 { font-size: 2.1rem; font-weight: 600; }
h2 { font-size: 1.35rem; font-weight: 600; }
h3 { font-size: 1.05rem; font-weight: 600; }
p { margin: 0; max-width: 70ch; }
.lede { color: var(--muted); }
.eyebrow { font: 600 .72rem/1 var(--mono); text-transform: uppercase; letter-spacing: .12em; color: var(--accent); }
section { display: grid; gap: 14px; }
.groups { display: grid; gap: 10px; }
.grp { display: grid; grid-template-columns: minmax(0, 15rem) minmax(0, 1fr) 7.5rem; gap: 12px; align-items: center;
  background: var(--panel); border: 1px solid var(--line); border-radius: 6px; padding: 10px 12px; }
.grp .name { font-weight: 600; min-width: 0; }
.grp .name small { display: block; color: var(--muted); font-weight: 400; font-size: .8rem; }
.bar { display: flex; height: 22px; border-radius: 3px; overflow: hidden; background: var(--chip); min-width: 0; }
.bar span { display: block; height: 100%; color: #fff; font: 600 .72rem/22px var(--mono); text-align: center; overflow: hidden; }
.rate { font: 600 1.25rem/1 var(--display); text-align: right; font-variant-numeric: tabular-nums; }
.rate small { display: block; font: .75rem var(--mono); color: var(--muted); margin-top: 3px; }
.legend { display: flex; flex-wrap: wrap; gap: 14px; font: .78rem var(--mono); color: var(--muted); }
.legend i { display: inline-block; width: 12px; height: 12px; border-radius: 2px; vertical-align: -2px; margin-right: 5px; }
.findings { background: var(--panel); border: 1px solid var(--line); border-radius: 6px; padding: 18px 20px; display: grid; gap: 10px; }
.findings ul { margin: 0; padding-left: 1.2rem; display: grid; gap: 6px; max-width: 80ch; }
.tablewrap { overflow-x: auto; border: 1px solid var(--line); border-radius: 6px; background: var(--panel); }
table { border-collapse: collapse; width: 100%; font-size: .84rem; font-variant-numeric: tabular-nums; }
th, td { text-align: left; padding: 6px 9px; border-bottom: 1px solid var(--line); vertical-align: top; }
th { font: 600 .7rem/1.2 var(--mono); text-transform: uppercase; letter-spacing: .06em; color: var(--muted); position: sticky; top: 0; background: var(--panel); }
td.n { text-align: right; }
td.note { min-width: 22rem; color: var(--muted); }
.cnt { display: inline-block; min-width: 1.6rem; text-align: center; font: 600 .85rem/1.5 var(--mono); border-radius: 3px; color: #fff; }
.c4 { background: var(--four); } .c3 { background: var(--three); } .c2 { background: var(--two); } .c1 { background: var(--one); } .c0 { background: var(--zero); } .cx { background: var(--muted); }
.conf { font: .72rem var(--mono); padding: 1px 6px; border-radius: 3px; background: var(--chip); white-space: nowrap; }
.conf.questionable { color: var(--q); font-weight: 600; }
.filters { display: flex; flex-wrap: wrap; gap: 8px; }
.filters button { font: .8rem var(--body); border: 1px solid var(--line); background: var(--panel); color: var(--fg); border-radius: 4px; padding: 4px 10px; cursor: pointer; }
.filters button[aria-pressed="true"] { background: var(--accent); color: var(--bg); border-color: var(--accent); }
.filters button:focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
.qlist { display: grid; gap: 8px; margin: 0; padding-left: 1.2rem; }
code { font: .82em var(--mono); background: var(--chip); padding: 1px 4px; border-radius: 3px; }
footer { color: var(--muted); font-size: .8rem; }
@media (max-width: 640px) { .grp { grid-template-columns: minmax(0, 1fr) 5.5rem; } .grp .bar { grid-column: 1 / -1; grid-row: 2; } }
"""

COLORS = ['var(--zero)', 'var(--one)', 'var(--two)', 'var(--three)', 'var(--four)']


def esc(x):
    return html.escape('' if x is None else str(x))


def group_row(label, sub, cs):
    n = len(cs)
    dist = [cs.count(i) for i in range(5)]
    segs = ''.join('<span style="width:%.2f%%;background:%s" title="%d pieces: %d picks">%s</span>' % (
        100 * d / n, COLORS[i], i, d, d if d else '') for i, d in enumerate(dist) if d)
    return ('<div class="grp"><div class="name">%s<small>%s</small></div><div class="bar" role="img" '
            'aria-label="%s">%s</div><div class="rate">%d%%<small>4/4 · %d of %d · mean %.1f</small></div></div>') % (
        esc(label), esc(sub), esc('distribution 0 to 4 pieces: ' + ', '.join(map(str, dist))), segs,
        round(100 * dist[4] / n), dist[4], n, sum(cs) / n)


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--out', default=os.path.join(ROOT, 'TeamCode', 'build', 'flower-picks', 'study.html'))
    a = p.parse_args()
    rows = json.load(open(os.path.join(DOC, 'picks.json')))
    findings = open(os.path.join(DOC, 'findings.html')).read() if os.path.exists(os.path.join(DOC, 'findings.html')) else ''
    match = [r for r in rows if r['source'] == 'match']
    prac = [r for r in rows if r['source'] != 'match' and r['count'] is not None]

    def counts(sel, src):
        return [r['count'] for r in src if sel(r) and r['count'] is not None]

    out = ['<title>FLOWER Pick Count</title>',
           '<meta name="claude:viewport-fit-sides" content="cover">',
           '<link rel="preconnect" href="https://fonts.googleapis.com">',
           '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Barlow+Semi+Condensed:wght@500;600'
           '&family=IBM+Plex+Mono:wght@400;600&family=IBM+Plex+Sans:wght@400;600&display=swap">',
           '<style>%s</style><div class="wrap">' % CSS]
    n_vid = sum(1 for r in match if r.get('confidence') in ('sure', 'likely', 'questionable'))
    out.append('<header style="display:grid;gap:8px"><div class="eyebrow">BIOBUZZ · teams 19429 &amp; 20245 · Auto FLOWER picks</div>'
               '<h1>How many pieces does each Auto FLOWER pick get?</h1>'
               '<p class="lede">Every Autonomous FLOWER pick with a match log and match video, Belding (7 Nov) and Fruitport '
               '(21 Nov), counted from the log and on video and reconciled into one number. The bar is 4 every pick. '
               '%d match picks, %d with a video count; practice picks (log only) are shown separately.</p></header>' % (len(match), n_vid))
    if findings:
        out.append('<section class="findings">%s</section>' % findings)

    legend = '<div class="legend">' + ''.join('<span><i style="background:%s"></i>%d pieces</span>' % (COLORS[i], i) for i in (4, 3, 2, 1, 0)) + '</div>'
    out.append('<section><div class="eyebrow">Match picks, log and video</div><h2>4/4 rate per robot and stick version</h2>%s<div class="groups">' % legend)
    for team in ('19429', '20245'):
        cs = counts(lambda r: r['team'] == team, match)
        if cs:
            out.append(group_row(team, 'all match picks', cs))
        for ver in sorted({r['version'] for r in match if r['team'] == team}, reverse=True):
            cs = counts(lambda r: r['team'] == team and r['version'] == ver, match)
            if cs:
                out.append(group_row('  ' + team, ver, cs))
    out.append('</div></section>')

    out.append('<section><div class="eyebrow">Practice picks, log only (no video)</div><h2>The same, from practice logs</h2>'
               '<p class="lede">Counted by the launcher wheels\' dips in the next volley less the pieces already in the lane. '
               'At Belding the dips were within one of the video and never under it, so read these as an upper bound.</p><div class="groups">')
    for team in ('19429', '20245'):
        for ver in sorted({r['version'] for r in prac if r['team'] == team}, reverse=True):
            cs = counts(lambda r: r['team'] == team and r['version'] == ver, prac)
            if cs:
                out.append(group_row(team, ver, cs))
    out.append('</div></section>')

    q = [r for r in match if r.get('confidence') == 'questionable' or r.get('count') is None]
    if q:
        out.append('<section><div class="eyebrow">For the mentor to check</div><h2>Questionable picks</h2><ol class="qlist">')
        for r in q:
            out.append('<li><b>%s %s %s, pick %s</b> (%s FLOWER): counted <b>%s</b>. %s <br><code>%s</code></li>' % (
                esc(r['event']), esc(r['match']), esc(r['team']), esc(r['pick']), esc(r['flower']),
                esc('?' if r['count'] is None else r['count']), esc(r.get('video_note') or ''), esc(r.get('video_clip') or '')))
        out.append('</ol></section>')

    out.append('<section><div class="eyebrow">Every match pick</div><h2>The ledger</h2>'
               '<div class="filters" role="group" aria-label="Filter picks">'
               '<button id="f-all" data-f="all" aria-pressed="true">All</button>'
               '<button id="f-19429" data-f="19429" aria-pressed="false">19429</button>'
               '<button id="f-20245" data-f="20245" aria-pressed="false">20245</button>'
               '<button id="f-short" data-f="short" aria-pressed="false">Under 4</button></div>'
               '<div class="tablewrap"><table id="ledger"><thead><tr>'
               '<th>Event</th><th>Match</th><th>Team</th><th>Pick</th><th>FLOWER</th><th>Stick</th>'
               '<th>Push end</th><th>Card s</th><th>Carried</th><th>Dips</th><th>Counter</th><th>Log gain</th>'
               '<th>Seen in</th><th>Seen out</th><th>Count</th><th>Confidence</th><th>Video note and clip</th></tr></thead><tbody>')
    for r in match:
        c = r['count']
        cls = 'cx' if c is None else 'c%d' % int(c)
        out.append('<tr data-team="%s" data-short="%s"><td>%s</td><td>%s</td><td>%s</td><td class="n">%s</td><td>%s</td><td>%s</td>'
                   '<td>%s</td><td class="n">%s</td><td class="n">%s</td><td class="n">%s</td><td class="n">%s</td><td class="n">%s</td>'
                   '<td class="n">%s</td><td class="n">%s</td><td><span class="cnt %s">%s</span></td><td><span class="conf %s">%s</span></td>'
                   '<td class="note">%s%s</td></tr>' % (
                       esc(r['team']), 'y' if c is None or int(c) < 4 else 'n',
                       'Belding' if r['event'] == 'USMIBEQ' else 'Fruitport', esc(r['match']), esc(r['team']), esc(r['pick']),
                       esc(r['flower']), esc(r['version']), esc(r['push_end']), esc(r['card_s']),
                       esc(_i(r['carried'])), esc(_i(r['next_dips'])), esc(_i(r['next_counter'])), esc(_i(r['log_gain'])),
                       esc(_i(r.get('in_seen'))), esc(_i(r.get('out_seen'))), cls, esc('?' if c is None else int(c)),
                       esc(r.get('confidence') or ''), esc(r.get('confidence') or ''), esc(r.get('video_note') or ''),
                       ('<br><code>%s</code>' % esc(r['video_clip'])) if r.get('video_clip') else ''))
    out.append('</tbody></table></div><p class="lede" style="font-size:.85rem">Carried: the lane count when the pick started. '
               'Dips: balls the next volley fired by the launcher wheels\' dips. Counter: the shot counter (it undercounts after a pick). '
               'Log gain: dips less carried. Seen in / out: pieces seen entering at the FLOWER and leaving in the next volley. '
               'Count: the reconciled pieces this pick added; the video wins where the two disagree. Clip times are seconds into that '
               'match\'s <code>video.mp4</code> from <code>tools/match-review</code>.</p></section>')
    out.append('<footer>Re-run: <code>python3 tools/flower-picks/flower_picks.py logs --practice</code>, then <code>report</code>, '
               'then <code>build_page.py</code>. Video counts: <code>doc/flower-picks/video-counts.json</code>.</footer></div>')
    out.append("""<script>
(function(){var bs=document.querySelectorAll('.filters button'),rows=document.querySelectorAll('#ledger tbody tr');
bs.forEach(function(b){b.addEventListener('click',function(){bs.forEach(function(x){x.setAttribute('aria-pressed',x===b?'true':'false')});
var f=b.dataset.f;rows.forEach(function(r){r.hidden=!(f==='all'||(f==='short'?r.dataset.short==='y':r.dataset.team===f))})})})})();
</script>""")
    os.makedirs(os.path.dirname(a.out), exist_ok=True)
    open(a.out, 'w').write('\n'.join(out))
    print('wrote', os.path.relpath(a.out, ROOT))


def _i(x):
    try:
        return int(float(x))
    except (TypeError, ValueError):
        return '' if x in (None, '') else x


if __name__ == '__main__':
    main()
