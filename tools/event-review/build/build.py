import json, os, sys
D = sys.argv[1]; out = sys.argv[2]
ids = ['matches','robots','shot_recovery','mechanisms','energy','volley_steps','spot_outcomes','field_opr','top_scores','results','cycle_steps','cycle_groups','cycle_group_summary','pieces','nectar','world_ranks','square_issues','drive_speed','feed_summary','feed_volleys','feed_nectar','drive_trips','launch_methods','volley_pieces','launch_volleys','stick_cancels','floor_pickup','floor_tiles','flower_picks']
data = {i: json.load(open(f'{D}/{i}.json')) if __import__('os').path.exists(f'{D}/{i}.json') else [] for i in ids}
page = open(f'{D}/index.html').read().replace('<meta name="dash-one-writer">', '')
_ev = f'{D}/event.json'
title = ((json.load(open(_ev)).get('name') if __import__('os').path.exists(_ev) else '') or 'Belding Qualifier') + ' review'
shim = '''<script>
window.__DATA = %s;
(function(){
  const store = {}; const draws = [];
  for (const [k, v] of Object.entries(window.__DATA)) store[k] = {status: 'ok', data: v, columns: v.length ? Object.keys(v[0]) : [], meta: [], refreshing: false};
  window.dash = {
    colors: ['#2F6FE4','#E0353D','#149E87','#D99A00','#7C8697','#B5835A','#8E5BD6','#3AA0C8'],
    data: (id) => store[id] || {status: 'missing', data: [], columns: []},
    calc: (id, o) => { const rows = o.inputs.map((i) => store[i].data); const r = o.fn(...rows); store[id] = {status: 'ok', data: r, columns: r.length ? Object.keys(r[0]) : []}; },
    loader: () => {}, onData: (fn) => draws.push(['__on', fn]), refresh: () => {}, params: () => ({}), setParams: () => {}, resetParams: () => {}, setLink: () => {},
    draw: (name, fn) => draws.push([name, fn]),
  };
  window.__runDraws = () => { for (const [n, fn] of draws) { if (n === '__on') { fn(); continue; } document.querySelectorAll(`[data-draw="${n}"]`).forEach((el) => fn(el)); } };
})();
</script>''' % json.dumps(data)
html = f'''<!doctype html><html lang="en"><head><meta charset="utf-8"><title>{title}</title>
<style>
:root {{ --color-fg:#1d1f24; --color-fg-muted:#5d6370; --color-bg:#ffffff; --color-panel:#f7f8fa; --color-border-line:#dfe2e8; --color-ok:#1f8a4c; --color-warn:#b7791f; --color-bad:#c53030;
  --cds-font-size-caption:11.5px; --cds-font-size-body:14px; --cds-font-size-heading:18px; --cds-font-size-title:30px; --cds-font-weight-medium:600;
  --cds-gap-xs:4px; --cds-gap-sm:8px; --cds-gap-md:14px; --cds-gap-lg:20px; --cds-gap-xl:30px; --cds-pad-xs:4px; --cds-pad-sm:8px; --cds-pad-md:12px; --cds-pad-lg:16px; --cds-radius:8px;
  --cds-chart-muted:#b9bfca; --cds-chart-grid:#e8eaef; --cds-chart-axis:#a3a9b5; --cds-chart-reference:#5d6370; --cds-chart-status-critical:#c53030;
  --cds-surface-popover:#fff; --cds-border:#dfe2e8; --cds-shadow-popover:none; --cds-text-secondary:#5d6370; --cds-dur-slow:0s; --cds-ease-out:linear;
  --font-anthropic-sans: "Helvetica Neue", Arial, "Liberation Sans", sans-serif; --font-anthropic-serif: Georgia, "DejaVu Serif", "Liberation Serif", serif; }}
html, body {{ margin:0; background:#0A0C12; color:var(--color-fg); font-family:var(--font-anthropic-sans); font-size:14px; }}
#dash-root {{ padding: 0; }}
.snap {{ font-size: 11.5px; color: var(--color-fg-muted); margin: 0 0 12px; }}
.bz .ask, .bz tr {{ break-inside: avoid; }}
.bz section > h2 {{ break-after: avoid; }}
.bz details > *:not(summary) {{ display: block; }}
@page {{ size: Letter; margin: 0; }}
.bz .kpi, .bz .duo, .bz .team, .bz .ask {{ break-inside: avoid; }} .bz svg {{ break-inside: avoid; }}
.bz .sec-head {{ break-after: avoid; }}
</style>
<script src="d3.min.js"></script>
{shim}
</head><body><div id="dash-root">{page}</div>
<script>__runDraws(); document.querySelectorAll('details').forEach((d) => d.open = true); window.__done = true;</script>
</body></html>'''
open(out, 'w').write(html); print(len(html))
