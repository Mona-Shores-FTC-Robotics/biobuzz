// Loads a print page (build.py output) and reports script errors and which charts drew nothing.
//   node tools/event-review/build/check_render.js /abs/path/review.html
const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch();
  const p = await b.newPage({ viewport: { width: 1088, height: 1200 } });
  const errs = []; p.on('pageerror', (e) => errs.push(String(e)));
  await p.goto('file://' + process.argv[2]);
  await p.waitForFunction(() => window.__done === true, null, { timeout: 30000 });
  const blank = await p.evaluate(() => {
    const out = [];
    document.querySelectorAll('[data-draw]').forEach((el) => {
      const n = el.getAttribute('data-draw');
      const txt = el.textContent.trim();
      const empty = !el.querySelector('svg, div, span, p') && (txt === '' || txt === '—' || txt === '- of -');
      if (empty) {
        const sec = el.closest('section');
        out.push((sec ? sec.id : 'top') + ':' + n + (el.getAttribute('data-field') ? '.' + el.getAttribute('data-source') + '.' + el.getAttribute('data-field') : ''));
      }
    });
    return out;
  });
  console.log(JSON.stringify({ errors: errs, blank }));
  await b.close();
})();
