import json, sys, re
D, out = sys.argv[1], sys.argv[2]
src = open(__import__('os').path.join(__import__('os').path.dirname(__import__('os').path.abspath(__file__)), 'build.py')).read()
ids = eval(re.search(r"ids = (\[.*?\])", src, re.S).group(1))
data = {i: json.load(open(f'{D}/{i}.json')) if __import__('os').path.exists(f'{D}/{i}.json') else [] for i in ids}
page = open(f'{D}/index.html').read().replace('<meta name="dash-one-writer">', '')
_ev = f'{D}/event.json'
title = ((json.load(open(_ev)).get('name') if __import__('os').path.exists(_ev) else '') or 'Belding Qualifier') + ' review'
shim = re.search(r"shim = '''(.*?)''' %", src, re.S).group(1) % json.dumps(data, separators=(',', ':'))
html = f'''<title>{title}</title>
<meta name="claude:viewport-fit-sides" content="cover">
<style>
:root {{ color-scheme: dark; --color-fg:#EEF1F8; --color-fg-muted:#8C95AA; --color-bg:#0A0C12; --color-panel:#11151F; --color-border-line:#232A3B;
  --cds-font-size-caption:11.5px; --cds-font-size-body:14px; --cds-font-size-heading:18px; --cds-font-size-title:30px; --cds-font-weight-medium:600;
  --cds-gap-xs:4px; --cds-gap-sm:8px; --cds-gap-md:14px; --cds-gap-lg:20px; --cds-gap-xl:30px; --cds-pad-xs:4px; --cds-pad-sm:8px; --cds-pad-md:12px; --cds-pad-lg:16px; --cds-radius:8px;
  --cds-chart-muted:#5B6479; --cds-chart-grid:#1E2433; --cds-chart-axis:#5B6479; --cds-chart-reference:#8C95AA; --cds-chart-status-critical:#E8263A;
  --cds-surface-popover:#161B28; --cds-border:#232A3B; --cds-shadow-popover:0 6px 24px rgba(0,0,0,.5); --cds-text-secondary:#8C95AA; --cds-dur-slow:.2s; --cds-ease-out:ease-out;
  --font-anthropic-sans: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; --font-anthropic-serif: Georgia, serif; }}
html, body {{ background:#0A0C12; color:#EEF1F8; }}
body {{ font-family:var(--font-anthropic-sans); overflow-x:hidden; }}
#dash-root {{ margin:0; }}
</style>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/7.9.0/d3.min.js"></script>
{shim}
<div id="dash-root">{page}</div>
<script>
__runDraws();
let __w = innerWidth, __t;
addEventListener('resize', () => {{ clearTimeout(__t); __t = setTimeout(() => {{ if (Math.abs(innerWidth - __w) > 4) {{ __w = innerWidth; __runDraws(); }} }}, 150); }});
</script>
'''
open(out, 'w').write(html); print(len(html))
