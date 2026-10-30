const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ args: ['--use-gl=swiftshader','--enable-webgl','--ignore-gpu-blocklist'] });
  const p = await b.newPage({ viewport: { width: 1400, height: 1900 } }); const errs = []; p.on('pageerror', e => errs.push(String(e)));
  await p.setContent(require('fs').readFileSync('flower-stick-sim.html', 'utf8'), { waitUntil: 'networkidle' }); await p.waitForTimeout(5000);
  const keys = await p.evaluate(() => [...document.querySelectorAll('[data-preset]')].map(b => [b.dataset.preset, b.textContent]));
  for (const [k, label] of keys) {
    await p.locator(`[data-preset="${k}"]`).click(); await p.waitForTimeout(2200);
    await p.evaluate(() => { const s = document.getElementById('sim-scrub'); s.value = Math.round(s.max * 0.7); s.dispatchEvent(new Event('input')); }); await p.waitForTimeout(600);
    const d = await p.evaluate(() => { const rects = [...document.querySelectorAll('#sim-svg rect')].filter(e => e.getAttribute('fill') === '#e0552b'); const r = rects[0]; if (!r || r.style.display === 'none') return null; const m = /translate\(([-0-9.]+)/.exec(r.parentNode.getAttribute('transform') || ''); return { tip: (parseFloat(r.getAttribute('x')) + parseFloat(r.getAttribute('width')) + (m ? parseFloat(m[1]) : 0)) / 52 - 10, reach: parseFloat(r.getAttribute('width')) / 52 }; });
    const lipBox = await p.locator('#lip-box').isVisible();
    const txt = (await p.locator('#sim-result').textContent());
    const res = txt.split(' to the posts. ')[1] || txt;
    console.log(`${label.padEnd(36)} | poker: ${d ? 'reach ' + d.reach.toFixed(2) + ', tip ' + d.tip.toFixed(2) + ' in from centre' : 'none'} | lip box ${lipBox ? 'yes' : 'no'} | ${res.slice(0, 110)}`);
  }
  console.log('errors', errs.length ? errs : 'none'); await b.close();
})();
