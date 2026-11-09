"""Fill page/review.html's image placeholders and copy an event's datasets into an output folder.

  python3 tools/event-review/build/assemble.py data/belding out/belding
then
  python3 tools/event-review/build/build_web.py out/belding out/belding/review-web.html   (public, self-contained page)
  python3 tools/event-review/build/build.py out/belding out/belding/review.html             (print page for the PDF)
  node tools/event-review/build/print_long.js $PWD/out/belding/review.html out/belding/review.pdf
"""
import base64, os, shutil, sys
here = os.path.dirname(os.path.abspath(__file__))
page_dir = os.path.join(here, '..', 'page')
src, out = sys.argv[1], sys.argv[2]
os.makedirs(out, exist_ok=True)
for f in os.listdir(src):
    if f.endswith('.json'):
        shutil.copy(os.path.join(src, f), out)
b64 = lambda p: base64.b64encode(open(os.path.join(page_dir, p), 'rb').read()).decode()
m = open(os.path.join(page_dir, 'review.html')).read()
m = m.replace('LOGO_IR', 'data:image/png;base64,' + b64('logo-19429-dark.png'))
m = m.replace('LOGO_UV', 'data:image/png;base64,' + b64('logo-20245-dark.png'))
m = m.replace('FIELD_IMG', 'data:image/webp;base64,' + b64('field-dark.webp'))
# The event's own words (event.json, written by review.py); without one the page stays Belding's.
ev_path = os.path.join(src, 'event.json')
if os.path.exists(ev_path):
    import json
    ev = json.load(open(ev_path))
    for old, key in (('<h1>Belding Qualifier</h1>', 'name'), ('FTC BIOBUZZ · Nov 7, 2026 ·', 'date'),
                     ('event USMIBEQ;', 'code'), ('as of Nov 8', 'world_as_of'), ("both robots' Saturday match logs", 'match_day'),
                     ('world ranks from FTCStats (ftcstats.org', 'world_source')):
        v = ev.get(key)
        if not v:
            continue
        new = {'name': '<h1>%s</h1>', 'date': 'FTC BIOBUZZ · %s ·', 'code': 'event %s;', 'world_as_of': 'as of %s',
               'match_day': "both robots' %s match logs", 'world_source': 'world ranks from %s'}[key] % v
        m = m.replace(old, new)
open(os.path.join(out, 'index.html'), 'w').write(m)
shutil.copy(os.path.join(page_dir, 'd3.min.js'), out)
print('assembled', out)
